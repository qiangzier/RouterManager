package com.hzq.routerapi.log;

import android.util.Log;

/**
 * Created by hezhiqiang on 2018/11/22.
 */

public class DefaultLogger implements ILogger {

    private static boolean isShowLog = false;
    private static boolean isShowStackTrace = false;
    private static boolean isMonitorMode = false;

    private String defaultTag = "RouterManager";

    @Override
    public void showLog(boolean showLog) {
        isShowLog = showLog;
    }

    @Override
    public void showStackTrace(boolean showStackTrace) {
        isShowStackTrace = showStackTrace;
    }

    @Override
    public void debug(String tag, String message) {
        if(isShowLog) {
            Log.d(defaultTag + tag,message);

        }
    }

    @Override
    public void info(String tag, String message) {
        if(isShowLog) {
            Log.i(defaultTag + tag,message);
        }
    }

    @Override
    public void warning(String tag, String message) {
        if(isShowLog) {
            Log.w(defaultTag + tag,message);
        }
    }

    @Override
    public void error(String tag, String message) {
        if(isShowLog) {
            Log.e(defaultTag + tag,message);
        }
    }

    @Override
    public void monitor(String message) {
        if(isShowLog) {

        }
    }

    @Override
    public boolean isMonitorMode() {
        return isMonitorMode;
    }

    @Override
    public String getDefaultTag() {
        return defaultTag;
    }
}
