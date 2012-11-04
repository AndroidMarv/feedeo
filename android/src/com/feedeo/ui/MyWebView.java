package com.feedeo;

import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.JsResult;
import android.content.Context;
import android.util.Log;

public class MyWebView extends WebView { 
    private static final String LOGTAG = "MyWebView";
    private static final String STARTING_PAGE = "file:///android_asset/index.html";
    private static MyWebView instance;

    public static MyWebView newInstance() {
        instance = new MyWebView(Utility.mContext);
        return instance;
    }

    public static MyWebView getInstance() {
        if (instance == null) 
            instance = new MyWebView(Utility.mContext);
        return instance;
    }

    public void releaseInstance() {
//        this.removeJavascriptInterface("myapp");
//        this.destroy();
//        instance = null;
    }

    private MyWebView (Context context) {
        super(context);
        WebSettings webSettings = this.getSettings();
        webSettings.setDomStorageEnabled(true); // localStorage
        webSettings.setDatabaseEnabled(false);
        /* bugfix- enabling datastorage is giving problems (app not working)
         *  after logout.
         * webSettings.setDatabasePath("/data/data/"+Utility.mContext.getPackageName()+"/databases"); // localStorage
         */
        webSettings.setJavaScriptEnabled(true);
        this.setWebChromeClient(new MyWebChromeClient());
//        this.setWebViewClient(new WebViewClient(this.getApplicationContext()));
        this.loadUrl(STARTING_PAGE); 
    }

    public void onLoad() {
        try { 
            this.loadUrl("javascript:onLoad();");
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void jsPrepareForRefresh() {
        try { 
            this.loadUrl("javascript:jsPrepareForRefresh();");
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void jsStartLoading() {
        //Log.d(LOGTAG, "jsStartLoading()");
        try { 
            this.loadUrl("javascript:jsStartLoading();");
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }
 
    public void jsDoneLoading() {
        //Log.d(LOGTAG, "jsDoneLoading()");
        try { 
            this.loadUrl("javascript:jsDoneLoading();");
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void jsUpdateVideo(String objid) {
        //Log.d(LOGTAG, "jsUpdateVideo("+objid+")");
        try { 
            this.loadUrl("javascript:jsUpdateVideo('" + objid + "');");
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void jsSaveVideoList() {
        try {
            this.loadUrl("javascript:jsSaveVideoList()");
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void jsLogout() {
        try { 
            this.loadUrl("javascript:jsLogout();"); 
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void jsEnableCommunityFeed() {
        //Log.d(LOGTAG, "jsEnableCommunityFeed()");
        if (Fb.getInstance().getFriendList() == null) return;
        try { 
             this.loadUrl("javascript:jsEnableCommunityFeed();"); 
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void jsSetDataStream(int stream) {
        //Log.d(LOGTAG, "jsSetDataStream("+stream+")");
        try {
            this.loadUrl("javascript:jsSetDataStream("+stream+")");
        } catch (Exception e) {
            Log.e(LOGTAG, "Error ..." + e.getMessage());
        }
    }

    public void jsQueryEpochUpdate(int stream, long epoch) {
        try {
            this.loadUrl("javascript:jsQueryEpochUpdate(" + stream + "," + epoch + ")");
        } catch (Exception e) {
            Log.e(LOGTAG, "Error ..." + e.getMessage());
        }
    }

    public void jsSnapshot() {
        try { 
            this.loadUrl("javascript:jsSnapshot();"); 
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    public void onSearchUpdate(String txt) {
        try { 
            MyWebView.this.loadUrl("javascript:onSearchUpdate('" + txt + "');"); 
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    } 

    public void onFeedback() {
        try { 
            MyWebView.this.loadUrl("javascript:onFeedback()");
        } catch (Exception e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
    }

    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(LOGTAG, message);
            return true;
        }
    }
}
