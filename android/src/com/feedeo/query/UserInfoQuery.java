package com.feedeo;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ClassNotFoundException;

import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import org.json.JSONObject;
import org.json.JSONException;

import com.facebook.android.FacebookError;

public class UserInfoQuery {

    public static void loadUserInfo(Handler handler) {
        Bundle params = new Bundle();
        params.putString("fields", "name, picture");
        Fb.getInstance().async().request("me", params, new UserRequestListener(handler));
    }

    public static class UserRequestListener extends BaseRequestListener {
        private static final String tag = "UserRequestListener";
        private Handler handler;

        UserRequestListener (Handler handler) {
            this.handler = handler;
        }

        public void onComplete(final String response, final Object state) {
            //Log.d(tag, "    onComplete()");
       	    JSONObject jobj;
	    try {
	        jobj = new JSONObject(response);
		final String picURL = jobj.getJSONObject("picture").getJSONObject("data").getString("url").replaceFirst("^https", "http");
		Fb fb = Fb.getInstance();
                fb.setUserName ( jobj.getString("name") );
	        fb.setUserUID ( jobj.getString("id") );
                fb.setUserPicNotAvailable(); 
                handler.sendEmptyMessage(1); // User info update
	        	
                Bitmap userpic_bitmap = Utility.getBitmap(picURL);
                if ( null == userpic_bitmap ) return;

                try {
                    FileOutputStream fos = new FileOutputStream(fb.getUserPicFilename());
                    userpic_bitmap.compress(CompressFormat.JPEG, 100, fos);
		} catch (FileNotFoundException e) {
	            e.printStackTrace();
                }
                fb.setUserPicAvailable(); 
                handler.sendEmptyMessage(1); // User info update
            } catch (JSONException e) {
	        e.printStackTrace();
            } 
        }
    }
}
