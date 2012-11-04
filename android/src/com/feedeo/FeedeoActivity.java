package com.feedeo;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
//import com.actionbarsherlock.view.ActionMode;

import android.util.DisplayMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Configuration;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.os.Message;
//import android.view.View.OnClickListener;
//import android.view.LayoutInflater;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.LinearLayout;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.LinearLayout;
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import android.widget.Toast;
//import android.widget.TextView;
//import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import com.facebook.android.FacebookError;
//import com.feedeo.MyWebView.SearchBarListener;

import com.feedeo.restfb.DefaultJsonMapper;

public class FeedeoActivity extends SherlockActivity {
    private final String LOGTAG = "FeedeoActivity";
    private static final int THEME = R.style.Theme_Sherlock_Light;

    // note: these are not public, so changes in value need to be 
    //     manually done in other java files/classes
    private static final int MODEL_STATE_UPDATE = 0; 
    private static final int USERINFO_UPDATE = 1;
    private static final int PREFERENCES = 4;
    private static final int FRIENDLIST_UPDATE = 5;
    private static final int SET_STREAM = 6;
    private static final int QUERY_EPOCH_UPDATE = 7;
    private static final int SOME_ERROR = -1;

    // menu options
    private static final int IC_CHANNEL = 0;
    private static final int IC_SEARCH = 1;
    private static final int IC_STATE = 2;
    private static final int IC_PROMOTE = 3;
    private static final int IC_LOGOUT = 4;

    private FrameLayout browserplaceholder = null;
    private MyWebView browser = null;
    private EditText searchtxt = null;
    private SearchBarListener searchlistener;

    private Fb fb;
    private FeedeoData mds;
    private boolean downloading = false;
    private boolean doinitialize = true;
    private boolean initfriendlist = true;
    private boolean inituserinfo = true;
    private int inittrigger = -1;
    private boolean logout = false;
    private boolean pendingdestroy = false;
    private int prevstate, curstate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.d(LOGTAG, "onCreate");
        setTheme(THEME);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        Fb.setAppContext( getApplicationContext() );
        fb = Fb.getInstance(); // load the class if its unloaded
        fb.loadUserInfo(mUICallbackHandler);
        fb.loadFriendsList(mUICallbackHandler);

       	Utility.mContext = (Context) this;
       	Utility.jsonMapper = new DefaultJsonMapper();
        prevstate = FeedeoData.LOGGEDOUT_NONE;
        curstate  = FeedeoData.LOGGEDOUT_NONE;

        mds = FeedeoData.getInstance();
        mds.setCallback(mUICallbackHandler);

        super.onCreate(savedInstanceState);
        initUI(0);

        searchlistener = new SearchBarListener();
   }
 
    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(LOGTAG, "onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d(LOGTAG, "onDestroy()");
        //browser.jsSnapshot();
        browser.jsSaveVideoList(); // workaround: when jsDone.. is not called
        browserplaceholder.removeView(browser);
        if (!is_downloading()) {
           if (logout) { 
               browser.releaseInstance();
               mds.releaseInstance();
               mds = null;
               browser = null;
           }
          /*  uncommending below is leading to bugs :- 
           *  start -> load few videos -> STOP -> restart -> NOT LOADING ANY videos 
           */  // browser = null;
        } else {
            pendingdestroy = true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newconfig) {
        if (browser != null) {
            browserplaceholder.removeView(browser);
        }
        super.onConfigurationChanged(newconfig);
        initUI(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.d(LOGTAG, "onCreateOptionsMenu()");
        MenuItem menuitem;

        if ( is_mystream() ) { // show the other option
        menuitem = menu.add(0, IC_CHANNEL, 0, "All friends");
        menuitem.setIcon( R.drawable.ic_everyone );
        menuitem.setEnabled( !initfriendlist );
        } else {
        menuitem = menu.add(0, IC_CHANNEL, 0, "My wall");
        menuitem.setIcon( R.drawable.ic_me );
        menuitem.setEnabled( !inituserinfo );
        }
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM );

        menuitem = menu.add(0, IC_SEARCH, 1, "Search");
        menuitem.setIcon( R.drawable.ic_search );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        menuitem.setOnActionExpandListener( searchlistener );
        menuitem.setActionView( R.layout.collapsible_edittext );
        searchtxt = (EditText) menuitem.getActionView();

        if ( is_downloading() ) {
        menuitem = menu.add(0, IC_STATE, 2,"Stop");
        menuitem.setIcon( R.drawable.ic_stop );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM  | MenuItem.SHOW_AS_ACTION_WITH_TEXT );
        } else {
        menuitem = menu.add(0, IC_STATE, 2, "Refresh");
        menuitem.setIcon( R.drawable.ic_refresh );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT );
        }
        menuitem.setEnabled( !doinitialize );

        menuitem = menu.add(0, IC_PROMOTE, 3, "Feedback");
        menuitem.setIcon( R.drawable.ic_feedback );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT );

        menuitem = menu.add(0, IC_LOGOUT, 4, "Logout");
        menuitem.setIcon( R.drawable.ic_logout );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT );

        return true; 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.d(LOGTAG, "MenuItem: " + item.getItemId() );
        switch ( item.getItemId() ) {
        case IC_CHANNEL:
            if ( is_mystream() ) { // we are concerned with last state
                mds.setDataStream ( FeedeoData.STREAM_ALL );
            } else {
                mds.setDataStream ( FeedeoData.STREAM_ME );
            }
            invalidateOptionsMenu();
            break;
        case IC_STATE:
            if ( is_downloading() ) {
                Utility.longtoast( "Stopping..." );
                mds.loadDataStop();
            } else {
                Utility.longtoast( "Refreshing..." );
                browser.jsPrepareForRefresh(); 
                mds.loadDataStart();
            }
            break;
        case IC_SEARCH:
            break;
        case IC_PROMOTE:
            browser.onFeedback();
            break;
        case IC_LOGOUT:
            Utility.toast("Logging out...");
            logout = true;
            browser.clearCache(true);
            fb.logout( new LogoutRequestListener () );
            mds.resetOnLogout();
            break;
        }

	return(true);
    }

/*    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
        case 0:
            // todo space to add code later
            break;
        }
        return dialog; 
    }
*/

    private final Handler mUICallbackHandler = new Handler() {
       public void handleMessage(Message msg) { 
           //Log.d(LOGTAG, "In handleMessage() --> " + msg);
           int arg1 = msg.arg1;
           int arg2 = msg.arg2;
           switch (msg.what) {
           case MODEL_STATE_UPDATE: { // state change
               prevstate = curstate;
               curstate = arg1;

               switch (curstate) {
               case FeedeoData.LOGGEDOUT_NONE: // * change logout text
                   actionbar_setstate( false);
                   break;
               case FeedeoData.LOGGEDIN_LOADING: // * update progress bar
                   actionbar_setstate( true);
                   if (prevstate != curstate) {
                       browser.jsStartLoading(); 
                   }
                   break;
               case FeedeoData.LOGGEDIN_NONE: // * update UI - done loading (hide progress bar)
                   actionbar_setstate( false);
                   switch (prevstate) {
                   case FeedeoData.LOGGEDOUT_NONE:
                       // by default browser widget things, this is not available in cache
                       break;
                   case FeedeoData.LOGGEDIN_LOADING:
                       browser.jsDoneLoading();
                       if (pendingdestroy) {
                           if (logout) { 
                               browser.releaseInstance();
                               mds.releaseInstance();
                               mds = null;
                               browser = null;
                           }
                       }
                       break;
                   }
                   break;
               }
 
               if (arg2 == 1) { // means there was a video sent
                   // also consumes (unused) objid's from last load
                   String objid = mds.nextInPipe(); 
                   while ( objid != null ) {
                       browser.jsUpdateVideo(objid);
                       objid = mds.nextInPipe();
                   }
               }
               return; }
           case USERINFO_UPDATE: { // user info updated
               showUserPic();
               inituserinfo = false;
               if ( doinitialize && inittrigger == USERINFO_UPDATE) { 
                   doinitialize = false; 
                   int ret = mds.setDataStream( FeedeoData.STREAM_ME );
                   if ( ret == FeedeoData.STREAM_ME ) { // ie. below was not called
                       browser.jsSetDataStream(FeedeoData.STREAM_ME);
                   }
               }
               invalidateOptionsMenu();
               return; }
           case PREFERENCES: {
               launchPreferencesDialog();
               return; }
           case FRIENDLIST_UPDATE: {
               //Log.d(LOGTAG, "Friend list update");
               initfriendlist = false;
               if ( doinitialize && inittrigger == FRIENDLIST_UPDATE) { 
                   doinitialize = false; 
                   int ret = mds.setDataStream( FeedeoData.STREAM_ALL );
                   if ( ret == FeedeoData.STREAM_ALL ) { // ie. below was not called
                       browser.jsSetDataStream(FeedeoData.STREAM_ALL);
                   }
               }
               invalidateOptionsMenu();
               return; }
           case SET_STREAM: {
               invalidateOptionsMenu();
               browser.jsSetDataStream(arg1);
               return; }
           case QUERY_EPOCH_UPDATE: {
               long epoch = mds.getStreamEpoch(arg1);
               browser.jsQueryEpochUpdate(arg1, epoch);
               return; }
           case SOME_ERROR: {
               // does nothing now
               return; }
           }
       }
    };

    // ------------------------------------------------    

   private void initUI(int val) {
        setContentView(R.layout.main);
        browserplaceholder = (FrameLayout) findViewById(R.id.browserplaceholder);
        if ( 0 == val ) {
            //getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.grey56));
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.olive));
            browser = MyWebView.newInstance();
            JsBrowserCallback jshandler = new JsBrowserCallback();
            browser.addJavascriptInterface(jshandler, "myapp"); 
        } else  {
            browser = MyWebView.getInstance();
        }
        browserplaceholder.addView(browser);
    }

    private void launchPreferencesDialog() { 
        Intent editIntent = new Intent().setClass(FeedeoActivity.this, EditPreferences.class);
        startActivity(editIntent);
    }

    private void showUserPic() {
        //Log.d(LOGTAG, "  ... in showUserPic()");
        if ( fb.isUserPicAvailable() ) {
             DisplayMetrics metrics = new DisplayMetrics();
             getWindowManager().getDefaultDisplay().getMetrics(metrics);
             BitmapDrawable upic = new BitmapDrawable(fb.getUserPicFilename());
             upic.setTargetDensity ( metrics );
             getSupportActionBar().setLogo ( upic ); 
             //getSupportActionBar().setDisplayShowTitleEnabled(false);
             getSupportActionBar().setTitle ("Feedeo");
        } else {
             if (fb.getUserName() != null) {
                 String name;
                 int spacePos = fb.getUserName().indexOf(" ");
                 if ( spacePos > 0 ) {
                     name = fb.getUserName().substring(0, spacePos);
                 } else {
                     name = fb.getUserName();
                 }
                 getSupportActionBar().setTitle ("Hi " + name);
             }
        }
    }

    private void actionbar_setstate(boolean state) {
        //Log.d(LOGTAG, "Downloading = " + downloading);
        if ( state != downloading ) {
            downloading = state;
            invalidateOptionsMenu();
        }
    }

    private boolean is_downloading() {
        return downloading; 
    }

    private boolean is_mystream() {
        return (mds.getDataStream() == FeedeoData.STREAM_ME);
    }

// -- Listeners -----------------------------------    

    private class LogoutRequestListener extends BaseRequestListener {
        public void onComplete(String response, final Object state) {
            finish(); // also exits application
        }

        public void onFacebookError(FacebookError e, final Object state) {
            Utility.longtoast ( "Unable to logout, check network connection" );
            super.onFacebookError(e,state);
        }
    }

    private class SearchBarListener implements TextWatcher, OnActionExpandListener { 
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            browser.onSearchUpdate(s.toString());
        }

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged (CharSequence s, int start, int count, int after) {
        }

        public boolean onMenuItemActionCollapse(MenuItem item) {
            searchtxt.removeTextChangedListener(this);
            browser.onSearchUpdate("");
            return true;
        }

        public boolean onMenuItemActionExpand(MenuItem item) {
            searchtxt.addTextChangedListener( searchlistener );
            browser.onSearchUpdate ( searchtxt.getText().toString() );
            return true;
        }
    }

// -- Javascript callback -------------------------    

    public class JsBrowserCallback {
	private static final String tag = "JsBrowserCallback";

    	public JsBrowserCallback() {
            Log.i(tag,"script handler created");
	}
	
    	public void Log(String s) {
            Log.i(tag,s);
	}
	
    	public void Info(String s) {
            Utility.longtoast(s);
       	}

        public void OpenSettings() {
            //Log.d(tag, "In OpenSettings()");
            mUICallbackHandler.sendEmptyMessage(PREFERENCES);
        }

        public String GetVideoMeta(String objid) {
            //Log.d(tag, "In GetVideoMeta()");
            if (mds == null) mds = FeedeoData.getInstance(); // workaround
            VideoPost vp = mds.getMeta(objid);
            String jstr = null;
            if ( vp != null ) {
                jstr = vp.toJSON().toString();
            }
            //Log.d(tag, "        <-- " + jstr);
            return jstr;
        }

	public void PlayVideo(String objid) {
            //Log.d(tag, "In PlayVideo()");
            // Launch the video
            VideoHelper helper = mds.getVideoHelper(objid);
            if (null != helper) {
                Intent i = helper.launchVideoIntent();
                if (null != i) {
                    try { 
                        startActivity (i);
                    } catch (Exception e) {
                        Log.e(tag, "Unable to play");
                    }
                }
            }
    	}

        public int AndroidAPIVer() {
            return Build.VERSION.SDK_INT;
        }

        public void Feedback(String cmd) {
            //Log.d(LOGTAG, "Feedback( " + cmd + " )");
            if ( cmd.equals("share") ) {
                PromoteApp.onMyWall();
            } else if ( cmd.equals("mail") ) {
                PromoteApp.mailme(Utility.mContext);
            } else if ( cmd.equals("review") ) {
                PromoteApp.review(Utility.mContext);
            }
            Utility.longtoast ( "Thanks!" );
        }

        public void SetSearchTerm(String searchTerm) {
            //WTApplication app = (WTApplication) context.getApplicationContext();
	    //app.setSearchTerm(searchTerm);
    	}

        public void DumpHTMLtoFile(String data) {
            try {
            String line;
            BufferedReader reader = new BufferedReader(new StringReader(data));
            //DataOutputStream stream = new DataOutputStream(new FileOutputStream(outputFile));
            while ((line = reader.readLine()) != null) {
                Log.d("FEEDEODATA", line);
            }
            reader.close();
            } catch (IOException e) {
                Log.e(tag,"Exception while writing the file");
            }
        }

        public void FetchMore() {
            mds.loadDataMore();
        }

        public void FetchNoMore() {
            mds.loadDataStop();
        }

        public int SetDataStream(int stream) {
            int ret = mds.setDataStream ( stream );
            if (ret == -2) {
                Utility.longtoast("Waiting for friend list!");
            }
            return ret;
        }

        // returns true if we should show the spinner
        public boolean LoadDataStream(int stream) {
            //Log.d(LOGTAG, "LoadDataStream( " + stream + " )");
            int ret = 0;
            if ( stream == FeedeoData.STREAM_ALL || stream == -1 ) {
                ret = mds.setDataStream ( FeedeoData.STREAM_ALL );
                if ( ret == FeedeoData.STREAM_ALL ) { // ie. below was not called
                    Message msg = new Message();
                    msg.what = SET_STREAM;
                    msg.arg1 = FeedeoData.STREAM_ALL;
                    mUICallbackHandler.sendMessage(msg);
                }
                inittrigger = FRIENDLIST_UPDATE;
            } else if ( stream == FeedeoData.STREAM_ME  ) {
                ret = mds.setDataStream ( FeedeoData.STREAM_ME );
                if ( ret == FeedeoData.STREAM_ME ) { // ie. below was not called
                    Message msg = new Message();
                    msg.what = SET_STREAM;
                    msg.arg1 = FeedeoData.STREAM_ME;
                    mUICallbackHandler.sendEmptyMessage(SET_STREAM);
                    mUICallbackHandler.sendMessage(msg);
                }
                inittrigger = USERINFO_UPDATE;
            }
            doinitialize = ( ret < 0 );
            return (doinitialize); // show a loader while we wait for friends list
        }

        public String GetUserPic(String objid) {
            return fb.getPictureByUserID ( objid );
        }

        public void SharebySMS(String objid) {
            VideoPost vp = mds.getMeta(objid);
            if (null != vp) {
                Action.sharebySMS(vp, Utility.mContext);
            } else {
                Log.e(tag, "GetMeta returned null.");
                Utility.longtoast("Error in data!");
            } 
        }

        public void ShareonFB(String objid) {
            VideoPost vp = mds.getMeta(objid);
            if (null != vp) {
                Action.ShareonFB (vp);
                Utility.toast("Share"); 
            } else { 
                Log.e(tag, "GetMeta returned null.");
                Utility.longtoast("Error in data!");
            } 
        }

        public void LikeonFB(String objid) {
            //Log.d (tag, "Like -> " + objid);
            AnyPostHelper helper = mds.getAnyPostHelper( objid );
            if (null != helper) {
                helper.like();
                AnyPost ap = helper.getData();
                if ( ap.getIsLikedByMe() )
                    Utility.toast("Unlike");
                else 
                    Utility.toast("Like"); 
            } else { 
                Log.e(tag, "GetMeta returned null.");
                Utility.longtoast("Error in data!");
            } 
        }

        public void ToastOwner(String ownid) {
            if ( initfriendlist ) return; 
            Friend owner = fb.getFriendList().get(ownid);
            if ( owner != null && owner.getName() != null ) {
                Utility.toast( owner.getName() );
            } 
        }
    }
}
