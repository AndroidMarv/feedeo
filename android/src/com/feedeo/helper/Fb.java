package com.feedeo;

import com.facebook.android.Facebook;
import com.facebook.android.AsyncFacebookRunner;

import android.os.Message;
import android.os.Handler;
import android.content.Context;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

public class Fb {
    private static Fb instance;
    private static Context appcontext;

    private static final String LOGTAG = "Fb";
    private static final String APP_ID = "222008291244520";
    private static final String imagefname = "currentuser.jpg";
    private static final String[] permissions = { "offline_access", "read_stream", "user_photos", "publish_stream" };

    private static final String KEY = "feedeodata";
    private static final String USERNAME = "username";
    private static final String USERID = "userid";
    private static final String FRIENDLIST = "friendlist";

    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

    private String userName;
    private boolean userPicAvl;
    private String userUID;
    private String objectID;
    private String userPicFile;

    private Map<String, Friend> mFriendList;
    //private FriendsGetProfilePics model;

    public static void setAppContext(Context cxt) {
        appcontext = cxt;
    }

    public static Fb getInstance() {
        if ( appcontext == null ) {
            Log.e(LOGTAG, "No application context set");
            return null;
        }
        if ( instance == null ) {
            instance = new Fb();
       	    //restore session if one exists
        }
        return instance;
    }

    public static String[] getPermissions() { return permissions; }

    // ---- XXX ----

    private Fb() { 
        mFacebook = new Facebook(APP_ID);
        if ( mFacebook != null ) { 
            mAsyncRunner = new AsyncFacebookRunner(mFacebook);
            SessionStore.restore(mFacebook, appcontext);
            if ( isSessionValid() ) {
                restoreprefs();
            }
        }
        userPicFile = this.appcontext.getFilesDir() + "/" + Fb.imagefname; 
    }

    public void saveSession() {
        SessionStore.save(mFacebook, appcontext);
    }

    public void clearSession() { 
        SessionStore.clear(appcontext);
    }

    public Facebook fb() {
        return mFacebook;
    }

    public AsyncFacebookRunner async() {
        return mAsyncRunner;
    }

    public boolean isSessionValid() {
        return mFacebook.isSessionValid();       
    }

    public void logout( BaseRequestListener logoutlistener ) {
        clearSession();
        async().logout( appcontext, logoutlistener );
    }

    // ---- XX ----

    public void setUserName(String name) { userName = name; }
    public String getUserName() { return userName; }

    public void setUserUID(String uid) { userUID = uid; }
    public String getUserUID() { return userUID; }

    public void setObjectID(String oid) { objectID = oid; }
    public String getObjectID() { return objectID; }

    public String getUserPicFilename() { return userPicFile; }

    public boolean isUserInfoAvailable() { return (userUID != null); }
    public boolean isFriendListAvailable() { return (mFriendList != null); }
    public boolean isUserPicAvailable() { return userPicAvl; }

    public void setUserPicNotAvailable() { userPicAvl = false; }
    public void setUserPicAvailable() { userPicAvl = true; }

    public Map<String,Friend> getFriendList() { return mFriendList; }

    public void updateFriendList (Collection<Friend> mFriendListCollection) {
        Iterator itr = mFriendListCollection.iterator();
        if ( mFriendList == null ) {
            mFriendList = new HashMap<String, Friend>();
        }
        while ( itr.hasNext() ) {
            Friend fr = (Friend) itr.next();
            if ( !mFriendList.containsKey(fr.getId()) ) {
                mFriendList.put( fr.getId() , fr );
            } 
        }
    }

    public String getPictureByUserID (String usrid) {
        if (usrid.equals( userUID )) 
            return userPicFile;
        if ( mFriendList == null ) return null; 
        Friend fr = (Friend) mFriendList.get( usrid );
        if ( fr == null ) return null;
        return fr.getPicture();
    }

    // ---- XX ----

    public void loadUserInfo(final Handler mUIHandler) {
        UserInfoQuery.loadUserInfo(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // we get USERINFO_UPDATE 
                saveuserinfo(); 
                mUIHandler.sendEmptyMessage(msg.what);
            }
        });
    }

    public void loadFriendsList(final Handler mUIHandler) {
        new FriendsListQuery(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // we get FRIENDLIST_UPDATE
                savefriendlist(); 
                mUIHandler.sendEmptyMessage(msg.what);
            }
          }).getFriendsList(); 
    }

    // ---- XX ----

    private void saveuserinfo() {
       Editor editor = appcontext.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
       editor.putString (USERID, userUID);
       editor.putString (USERNAME, userName);
       editor.commit();        
    }

    // TODO move below friendslist to database 
    private void savefriendlist() {
       Editor editor = appcontext.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
       try {
           ArrayList<Friend> arrlist = new ArrayList<Friend> (mFriendList.values());        
           editor.putString(FRIENDLIST, ObjectSerializer.serialize(arrlist));
           editor.commit(); 
       } catch (IOException ex) {
           Log.e(LOGTAG, "IOExcepton while saving friendslist. " + ex);
       }
    }

    private void restoreprefs() {
       SharedPreferences misc = appcontext.getSharedPreferences(KEY, Context.MODE_PRIVATE);
       userUID = misc.getString( USERID, null);
       userName = misc.getString ( USERNAME, null);
       try {
           ArrayList<Friend> arrlist = (ArrayList<Friend>) 
                  ObjectSerializer.deserialize(misc.getString( FRIENDLIST , null)); 
           if (arrlist != null) {
               mFriendList = new HashMap<String, Friend>();
               Iterator itr = arrlist.iterator();
               while(itr.hasNext()) {
                   Friend fr = (Friend) itr.next();
                   mFriendList.put ( fr.getId(), fr );
               }
            }
       } catch (IOException ex) {
           Log.e(LOGTAG, "IOExcepton while loading preferences. " + ex);
       } catch (ClassNotFoundException ex) {
           Log.e(LOGTAG, "ClassNotFoundExcepton while loading preferences. " + ex);
       }
    }

    private void cleanupprefs() {
       Editor editor = appcontext.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
       editor.clear();
       editor.commit();
    }

}
