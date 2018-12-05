package com.hzq.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hzq.router_annotation.facade.annotation.Route;

/**
 * Created by hezhiqiang on 2018/12/4.
 */

@Route(path = "/user/main")
public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
    }
}
