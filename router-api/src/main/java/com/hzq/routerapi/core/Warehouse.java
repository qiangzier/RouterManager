package com.hzq.routerapi.core;

import com.hzq.router_annotation.facade.model.RouteMeta;
import com.hzq.routerapi.facade.template.IProvider;
import com.hzq.routerapi.facade.template.IRouteGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hezhiqiang on 2018/11/20.
 */

class Warehouse {
    // Cache route and metas
    static Map<String, Class<? extends IRouteGroup>> groupsIndex = new HashMap<>();
    static Map<String, RouteMeta> routes = new HashMap<>();
    // Cache provider
    static Map<Class, IProvider> providers = new HashMap<>();       //存储加载后的实例对象
    static Map<String, RouteMeta> providersIndex = new HashMap<>(); //存储映射表信息

    static void clear() {
        providers.clear();
        providersIndex.clear();
    }
}
