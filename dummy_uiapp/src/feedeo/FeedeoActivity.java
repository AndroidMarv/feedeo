package com.feedeo;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.ActionBar;

import android.view.MenuInflater;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;

public class FeedeoActivity extends SherlockActivity {
    private final String LOGTAG = "FeedeoActivity";
    private static final String STARTING_PAGE = "file:///android_asset/index.html";
    private static final int THEME = R.style.Theme_Sherlock_Light;

    private static final int IC_CHANNEL = 0;
    private static final int IC_SEARCH = 1;
    private static final int IC_STATE = 2;
    private static final int IC_PROMOTE = 3;
    private static final int IC_LOGOUT = 4;

    //private static PromoteApp promote = new PromoteApp();
    private WebView browser = null;
    private JsHandler jshandler = null; 
    private boolean downloading = true;
    private boolean singlemode = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(THEME);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG, "onCreate()");
        setContentView(R.layout.main);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.olive));
        browser = (WebView) findViewById(R.id.browser);
        setupBrowser();
        browser.loadUrl(STARTING_PAGE); // UI decides if we need to load data afresh
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOGTAG, "onCreateOptionsMenu()");
        MenuItem menuitem;

        if ( singlemode ) {
        menuitem = menu.add(0, IC_CHANNEL, 0, "My wall");
        menuitem.setIcon( R.drawable.ic_me );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM );
        } else {
        menuitem = menu.add(0, IC_CHANNEL, 0, "All friends");
        menuitem.setIcon( R.drawable.ic_everyone );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM );
        }

        menuitem = menu.add(0, IC_SEARCH, 1, "Search");
        menuitem.setIcon( R.drawable.ic_search );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM );

        if ( downloading ) {
        menuitem = menu.add(0, IC_STATE, 2, "Refresh");
        menuitem.setIcon( R.drawable.ic_refresh );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT );
        } else {
        menuitem = menu.add(0, IC_STATE, 3,"Stop");
        menuitem.setIcon( R.drawable.ic_stop );
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM  | MenuItem.SHOW_AS_ACTION_WITH_TEXT );
        }

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
        Log.d(LOGTAG, "onOptionsItemSelected( " + item + "," + item.getItemId() + " )");
        switch ( item.getItemId() ) {
        case IC_CHANNEL:
            singlemode = !singlemode;
            invalidateOptionsMenu();
            break;
        case IC_STATE:
            downloading = !downloading;
            invalidateOptionsMenu();
        case IC_SEARCH:
            //getSupportActionBar().setDisplayShowCustomEnabled(true);
            break;
        case IC_LOGOUT:
            break;
        }
        return true;
    }

    private void setupBrowser() {
        WebSettings webSettings = browser.getSettings();
        webSettings.setDomStorageEnabled(true); // localStorage
        webSettings.setDatabasePath("/data/data/"+this.getPackageName()+"/databases/"); // localStorage
        webSettings.setJavaScriptEnabled(true);
        jshandler = new JsHandler();
        browser.addJavascriptInterface(jshandler, "con");
    }

    public class JsHandler {
    	public void Log(String s) {
            Log.d(LOGTAG,s);
	}
    }
}
