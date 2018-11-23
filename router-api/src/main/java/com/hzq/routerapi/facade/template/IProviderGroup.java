package com.hzq.routerapi.facade.template;

import com.hzq.router_annotation.facade.model.RouteMeta;

import java.util.Map;

/**
 * Created by hezhiqiang on 2018/11/20.
 */

public interface IProviderGroup {
    /**
     * Load providers map to input
     *
     * @param providers input
     */
    void loadInto(Map<String, RouteMeta> providers);
}
