package com.hzq.routermanager;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hzq.routerapi.core.RouterManager;

/**
 * Created by hezhiqiang on 2018/11/23.
 */

public class SchemaFilterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        RouterManager.getInstance().build(data).navigation();
        finish();
    }
}
