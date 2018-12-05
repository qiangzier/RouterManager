package com.hzq.routermanager;

import android.util.Log;

import com.hzq.router_annotation.facade.annotation.Route;
import com.hzq.user.IUserService;

/**
 * Created by hezhiqiang on 2018/11/22.
 */

@Route(path = "/user1/info")
public class UserServiceImpl implements IUserService {

    public void test(String test) {
        Log.d("xxxx->",test);
    }
}
