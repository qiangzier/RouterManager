package com.hzq.routermanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hzq.routerapi.core.RouterManager;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    /**
     * @Autowired
    public String username;

     @Autowired
     public int userId;

     @Autowired
     public boolean isLogin;
     * @param view
     */
    public void startSecondAction(View view) {
        RouterManager.getInstance()
                .build("/test/main")
                .withString("username","qiangzi")
                .withInt("userId",1234)
                .withBoolean("isLogin",false)
                .navigation(this);
    }
}
