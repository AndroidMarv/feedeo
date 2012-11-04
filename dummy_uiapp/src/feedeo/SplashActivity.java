package com.feedeo;

import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.graphics.Color;
import android.widget.ImageButton;

public class SplashActivity extends Activity {
    private static final String LOGTAG = "SplashActivity";
    private static final String APP_ID = "166792676772905";
    private static final String[] permissions = { "offline_access", "read_stream", "user_photos", "publish_stream" };
    private static final long DELAY = 1500;
    private static final String imagefname = "currentuser.jpg";

    private static final int LAUNCHAPP = 2;
    private static final int LONGTOAST = 3;
    private static final int DOLOGIN = 4;

    private ImageButton mLoginButton;
    private String toasttxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        handler.sendEmptyMessageDelayed(DOLOGIN, DELAY);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

        mLoginButton = (ImageButton) findViewById(R.id.login);
        mLoginButton.setBackgroundColor(Color.TRANSPARENT);
        mLoginButton.setImageResource(R.drawable.login_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                handler.sendEmptyMessage(DOLOGIN);
            }
        });
    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LAUNCHAPP:
                finish();
                Intent feedomIntent = new Intent().setClass(SplashActivity.this, FeedeoActivity.class);
                startActivity(feedomIntent);
                break;
            case LONGTOAST:
                longtoast(toasttxt);
                break;
            case DOLOGIN:
                handler.sendEmptyMessage(LAUNCHAPP);
                break;
            }
        }
    };

    private void longtoast(String message) {
        Toast toast = Toast.makeText(this, message,
                    Toast.LENGTH_LONG);
        toast.show();
    }

}
