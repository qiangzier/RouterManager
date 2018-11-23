package com.hzq.routermanager;

import android.util.Log;

import com.hzq.router_annotation.facade.annotation.Route;

/**
 * Created by hezhiqiang on 2018/11/22.
 */

@Route(path = "/service/user/info",group = "user")
public class UserServiceImpl implements IUserService {

    public void test(String test) {
        Log.d("xxxx->",test);
    }
}
