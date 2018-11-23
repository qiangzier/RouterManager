package com.hzq.routermanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.hzq.router_annotation.facade.annotation.Autowired;
import com.hzq.router_annotation.facade.annotation.Route;
import com.hzq.routerapi.core.RouterManager;

@Route(path = "/test/main")
public class OtherActivity extends AppCompatActivity {

    @Autowired
    public String username;

    @Autowired
    public int userId;

    @Autowired
    public boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.text);

        parseIntent();

        StringBuilder sb = new StringBuilder("result = [");
        sb.append("username = ").append(username).append("\n");
        sb.append("userId = ").append(userId).append("\n");
        sb.append("isLogin = ").append(isLogin).append("\n]");
        textView.setText(sb.toString());

        IUserService userService = (IUserService) RouterManager.getInstance().navigation(IUserService.class);
        if(userService != null)
            userService.test("hhhhhhh");
    }

    private void parseIntent(){
        Intent intent = getIntent();
        if(intent != null) {
            username = intent.getStringExtra("username");
            userId = intent.getIntExtra("userId",0);
            isLogin = intent.getBooleanExtra("isLogin",false);
        }
    }
}
