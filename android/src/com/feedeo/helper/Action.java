package com.feedeo;

import android.util.Log;
import android.content.Intent;
import android.net.Uri;
import android.content.Context;
import android.os.Bundle;
import android.content.ActivityNotFoundException;

import com.facebook.android.FacebookError;

public class Action {
    private final static String LOGTAG = "Action";
    private final static int MAXLEN = 45;

    public static void sharebySMS(VideoPost vp, Context context) {
        String title = vp.getName();
        if ( title.length() > MAXLEN ) {
            title = title.substring(0, MAXLEN);
        }
        String msg = "Hey,\n\n"
                   + "Watch " + title + " ( " + vp.getVideoLink().toExternalForm() + " )" 
                   + "\n\n"
                   + "Video shared using Feedeo (android app)";
        Intent sms = new Intent(Intent.ACTION_VIEW);         
        sms.setData(Uri.parse("sms:"));
        sms.putExtra("sms_body", msg);
        try { 
            context.startActivity(sms);
        } catch (ActivityNotFoundException ex) {
            Log.e(LOGTAG, "SMS not available. " + ex.getMessage());
            Utility.longtoast("Error SMS not available on device!");
        }
    }

    public static void ShareonFB(VideoPost vp) {
        String msg = "Shared using Feedeo (android).";
        Bundle params = new Bundle();
        params.putString("link", vp.getVideoLink().toExternalForm());
        params.putString("name", msg);
        (vp.getAnyPostHelper()).share(params); // no callback, but we dont need it either
    }
}
