package com.feedeo;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ClassNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import com.facebook.android.FacebookError;

public class FriendsListQuery {
    private static final String LOGTAG = "FriendsListQuery";
    public Handler handler;
 
    public FriendsListQuery(Handler handler) {
        this.handler = handler;
    } 

    public void getFriendsList() {
        Bundle params = new Bundle();
        params.putString("fields", "name, id, picture");
        Fb.getInstance().async().request("me/friends", params,
                new FriendsRequestListener());
    }

    public void getFriendsRatings() {
        String query = "select uid, count(*) from links where source_id in (select uid2 from friend where uid1=me()) order by name";
        Bundle params = new Bundle();
        params.putString("method", "fql.query");
        params.putString("query", query);
        Fb.getInstance().async().request(null, params,
                new FriendsRatingsListener());
    }

    // -------------------------------------------------------


    public class FriendsRequestListener extends BaseRequestListener {
        @Override
        public void onComplete(final String response, final Object state) {
            //Log.d(LOGTAG, "onComplete() - FriendsRequestListener");
            try {
                JSONArray jarr = new JSONObject(response).getJSONArray("data");
                
                Collection<Friend> mFriendList = new ArrayList<Friend> (); 
                for ( int i = 0; i < jarr.length(); i++ ) {
                    JSONObject jobj = jarr.getJSONObject(i);
                    String picture = jobj.getJSONObject("picture").getJSONObject("data").getString("url");
                    Friend fr = new Friend();
                    fr.setName( jobj.getString("name") )
                      .setId( jobj.getString("id") )
                      .setPicture( picture );
                    mFriendList.add( fr );
                }
                Fb.getInstance().updateFriendList(mFriendList);
                handler.sendEmptyMessage(5); // success
            } catch ( JSONException e ) {
                Log.e(LOGTAG, e.toString());
            }            
        }

        public void onFacebookError(FacebookError error) {
            Log.e(LOGTAG, error.toString());
            handler.sendEmptyMessage(-1); // some error
        }
    }

    public class FriendsRatingsListener extends BaseRequestListener {
        @Override
        public void onComplete(final String response, final Object state) {
            handler.sendEmptyMessage(5); // success
        }

        public void onFacebookError(FacebookError error) {
            Log.e(LOGTAG, error.toString());
            handler.sendEmptyMessage(-1); // some error
        }
    }
}
