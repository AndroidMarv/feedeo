package com.feedeo;

import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.facebook.android.FacebookError;

public class PromoteApp {
    public final static String LOGTAG = "PromoteApp";
    private final static Uri APPURI = Uri.parse("market://details?id=com.feedeo"); 
    private final static String APPURL = "https://play.google.com/store/apps/details?id=com.feedeo";
    //private final static String[] MYMAIL = { "feedeodevteam@gmail.com" };
    private final static String[] MYMAIL = { "flipflopapps@gmail.com" };

    public static void onMyWall() {
        final String PRMSG = " is using Feedeo, to watch videos on facebook. \n\n" + 
                               APPURL + " <-- Available on Android now!";
        Bundle params = new Bundle();
        params.putString("method", "POST");
        params.putString("format", "json");
	params.putString("link", APPURL);
        params.putString("message", PRMSG);
        params.putString("access_token", Fb.getInstance().fb().getAccessToken());
        Fb.getInstance().async().request("me/links", params, new PromoteRequestListener() );
    }

    public static void mailme(Context context) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("plain/text");
        email.putExtra(Intent.EXTRA_EMAIL, MYMAIL); 
        email.putExtra(Intent.EXTRA_SUBJECT, "Feedback/Feature/Bug");

        // Debug info
        String conf = context.getResources().getConfiguration().toString();
        StringBuffer buf = new StringBuffer();
        buf.append("VERSION.RELEASE {"+Build.VERSION.RELEASE+"} ");
        buf.append("VERSION.INCREMENTAL {"+Build.VERSION.INCREMENTAL+"} ");
        buf.append("VERSION.SDK {"+Build.VERSION.SDK_INT+"} ");
        buf.append("BOARD {"+Build.BOARD+"} ");
        buf.append("BRAND {"+Build.BRAND+"} ");
        buf.append("DEVICE {"+Build.DEVICE+"} ");
        buf.append("FINGERPRINT {"+Build.FINGERPRINT+"} ");
        buf.append("HOST {"+Build.HOST+"} ");
        buf.append("ID {"+Build.ID+"} ");
        buf.append("CONFIG {"+conf+"} ");

        // Normal message
        StringBuffer msg = new StringBuffer();
        msg.append("Hi:\nadd message here...\nThanks\n");
        msg.append(Fb.getInstance().getUserName());
        msg.append("\n\nSystem info (used for bug-fixing ):-\n\n");
        msg.append(buf);

        email.putExtra(Intent.EXTRA_TEXT, msg.toString());
        context.startActivity(email);
    }

    public static void review(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW).setData(APPURI));
    }

 // -- TODO -----------------------------------------------

    public static void checkForNewVersion (Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW ,APPURI);
        context.startActivity(intent); 
    }

    public static void onFriendsWall () {
        /* find friends who like to share many videos */
        /* tell them about it */
    }

    public static void myCreativityRatings () { // need a server component
    }

    public static void friendsCreativityRatings () { 
    }

 // ------------------------------------------------------

    public static class PromoteRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
        }

        public void onFacebookError(FacebookError error) {
            // toast("Could not share!");
        }
    }

}
