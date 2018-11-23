package com.hzq.routermanager;

import android.app.Application;

import com.hzq.routerapi.core.RouterManager;

/**
 * Created by hezhiqiang on 2018/11/23.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RouterManager.openDebug();
        RouterManager.init(this);
    }
}
