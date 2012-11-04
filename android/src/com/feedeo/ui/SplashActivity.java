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

import com.facebook.android.Facebook;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.DialogError;

public class SplashActivity extends Activity {
    private static final String LOGTAG = "SplashActivity";
    private static final long DELAY = 1500;

    private static final int LAUNCHAPP = 2;
    private static final int LONGTOAST = 3;
    private static final int DOLOGIN = 4;

    private Fb fb;
    private ImageButton mLoginButton;
    private String toasttxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        mLoginButton = (ImageButton) findViewById(R.id.login);
        mLoginButton.setEnabled(false); 
        Fb.setAppContext( getApplicationContext() );
        fb = Fb.getInstance(); 
        if ( fb.isSessionValid()  ) {
            handler.sendEmptyMessage(LAUNCHAPP);
        } else {
            handler.sendEmptyMessageDelayed(DOLOGIN, DELAY);
            mLoginButton.setBackgroundColor(Color.TRANSPARENT);
            mLoginButton.setImageResource(R.drawable.login_button);
            mLoginButton.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    handler.sendEmptyMessage(DOLOGIN);
                }
            });
        }
    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LAUNCHAPP:
                finish();
                Intent feedomIntent = new Intent().setClass(SplashActivity.this, FeedeoActivity.class);
                startActivity(feedomIntent);
                fb.saveSession();
                break;
            case LONGTOAST:
                longtoast(toasttxt);
                break;
            case DOLOGIN:
                mLoginButton.setEnabled(true);
                fb.fb().authorize(SplashActivity.this, Fb.getPermissions(), 0, new LoginDialogListener()); // disable signle-sign-on
                break;
            }
        }
    };

    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            handler.sendEmptyMessage(LAUNCHAPP);
        }

        public void onFacebookError(FacebookError error) {
            toasttxt = "Facebook error: " + error;
            handler.sendEmptyMessage(LONGTOAST);
        }
        
        public void onError(DialogError error) {
            toasttxt = "Error: " + error;
            handler.sendEmptyMessage(LONGTOAST);
        }

        public void onCancel() {
            toasttxt = "Action Canceled";
            handler.sendEmptyMessage(LONGTOAST);
        }
    }

    private void longtoast(String message) {
        Toast toast = Toast.makeText(this, message,
                    Toast.LENGTH_LONG);
        toast.show();
    }

    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fb.fb().authorizeCallback(requestCode, resultCode, data);
    }

}
