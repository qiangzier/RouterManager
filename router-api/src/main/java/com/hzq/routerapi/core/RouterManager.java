package com.hzq.routerapi.core;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.hzq.routerapi.exception.HandlerException;
import com.hzq.routerapi.exception.InitException;
import com.hzq.routerapi.exception.NoRouteFoundException;
import com.hzq.routerapi.log.DefaultLogger;
import com.hzq.routerapi.log.ILogger;
import com.hzq.routerapi.util.Consts;

/**
 * Created by hezhiqiang on 2018/11/20.
 */

public class RouterManager {

    private volatile static boolean hasInit = false;
    private volatile static boolean debuggble = false;
    private volatile static RouterManager instance = null;
    private static ILogger logger;
    private static Context mContext;
    private static Handler mHandler;


    private RouterManager(){}

    public static synchronized void init(Application application){
        if(!hasInit) {
            hasInit = true;
            mContext = application;
            mHandler = new Handler(Looper.getMainLooper());
            logger = new DefaultLogger();
            LogisticsCenter.init(mContext,logger);
        }
    }

    public static RouterManager getInstance() {
        if(!hasInit) {
            throw new InitException("RouterManager need be call $RouterManager.init()");
        } else {
            if(instance == null) {
                synchronized (RouterManager.class) {
                    if(instance == null) {
                        instance = new RouterManager();
                    }
                }
            }
        }
        return instance;
    }

    public Postcard build(String path) {
        if(TextUtils.isEmpty(path)) {
            throw new HandlerException("Parameter is invalid!");
        } else {
            return build(path,extractGroup(path));
        }
    }

    public Postcard build(Uri uri) {
        if(null == uri || TextUtils.isEmpty(uri.toString())) {
            throw new HandlerException(Consts.TAG + "Parameter invalid");
        } else {
            return new Postcard(uri.getPath(),extractGroup(uri.getPath()),uri,null);
        }
    }

    public Postcard build(String path,String group) {
        if(TextUtils.isEmpty(path)) {
            throw new HandlerException("Parameter is invalid!");
        } else {
            return new Postcard(path,group);
        }
    }

    public <T> T navigation(Class<? extends T> service) {
        try {
            Postcard postcard = LogisticsCenter.buildProvider(service.getName());

            if(postcard == null)
                return null;

            //装载数据
            LogisticsCenter.completion(postcard);
            return (T) postcard.getProvider();
        } catch (NoRouteFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object navigation(final Context context,final Postcard postcard,final int requestCode) {
        try {
            LogisticsCenter.completion(postcard);
        } catch (HandlerException e) {
            e.printStackTrace();
            return null;
        }

        final Context currentContext = context == null ? mContext : context;
        switch (postcard.getType()) {
            case ACTIVITY:
                final Intent intent = new Intent(currentContext,postcard.getDestination());
                intent.putExtras(postcard.getExtras());

                int flags = postcard.getFlags();
                if(flags != -1) {
                    intent.setFlags(flags);
                } else if(!(currentContext instanceof Activity)) { //如果当前上下文不是activity，则启动activity时需要new一个新的栈
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(requestCode,currentContext,intent,postcard);
                    }
                });
                break;
            case FRAGMENT:
                Class fragmentMeta = postcard.getDestination();
                try {
                    Object instance = fragmentMeta.getConstructor().newInstance();
                    if(instance instanceof Fragment) {
                        ((Fragment) instance).setArguments(postcard.getExtras());
                    } else if(instance instanceof android.support.v4.app.Fragment) {
                        ((android.support.v4.app.Fragment) instance).setArguments(postcard.getExtras());
                    }
                    return instance;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PROVIDER:
                return postcard.getProvider();
            default:
                break;
        }
        return null;
    }

    /**
     * 主线程执行启动activity
     * @param runnable
     */
    private void runInMainThread(Runnable runnable) {
        if(Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    private void startActivity(int requestCode,Context context,Intent intent,Postcard postcard) {
        if(requestCode >= 0) {
            if(context instanceof Activity) {
                ActivityCompat.startActivityForResult((Activity)context,intent,requestCode,postcard.getOptionsCompat());
            } else {
                //
            }
        } else {
            ActivityCompat.startActivity(context,intent,postcard.getOptionsCompat());
        }

        //兼容旧版本转场动画
        if(postcard.getEnterAnim() != -1 && postcard.getExitAnim() != -1 && context instanceof Activity) {
            ((Activity)context).overridePendingTransition(postcard.getEnterAnim(),postcard.getExitAnim());
        }
    }

    private String extractGroup(String path) {
        if(TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new HandlerException("Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }
        try {
            String defaultGroup = path.substring(1,path.indexOf("/",1)); //截取分组
            if(TextUtils.isEmpty(defaultGroup)) {
                throw new HandlerException("Extract the default group failed! There's nothing between 2 '/'!");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static void openDebug(){
        debuggble = true;
        if(logger != null)
            logger.showLog(debuggble);
    }

    public static boolean debuggable() {
        return debuggble;
    }

    public synchronized static void destory(){

    }
}
