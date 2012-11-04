package com.feedeo;

import android.util.Log;
import java.util.Date;
import java.net.MalformedURLException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.StringBuilder;
import java.lang.ref.WeakReference;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;
import com.feedeo.restfb.types.Post;

public class VideoPost extends AnyPost implements Serializable {
    private static final String LOGTAG = "VideoPost";
    private static final long serialVersionUID = 2981227327981029282L;

    // postToJson
    public static final String DURATION = "duration";
    public static final String CANPLAY = "canplay";

    private boolean canplay;
    private int duration;
    private URL videolink;

    // YouTube fields
    private String vid;

    public VideoPost (AnyPost post) { 
        super(post);
        canplay = true; // default
        URL url = null;
        try {
            url = new URL ( post.getLink() );
        } catch ( MalformedURLException ex ) {
            Log.e(LOGTAG, "post.getLink() raised MalformedURLException, using getSource()... " + ex.getMessage()); 
            Log.d(LOGTAG, "post.getSource() = " + post.getSource());
            try {
                url = new URL ( post.getSource() );
            } catch ( MalformedURLException ex1 ) {
                Log.e(LOGTAG, "error..." + ex1.getMessage()); 
                Log.e(LOGTAG, "post.getSource() raised MalformedURLException, nulled...");
                url = null; 
            }
        } finally {
            videolink = url;
        }
    }

    public VideoHelper getVideoHelper() {
        try { 
            VideoHelper vh = new VideoHelper(this);
            return vh;
        } catch (MalformedURLException e) {
            Log.e(LOGTAG, "MalformedURLException exception. " + e.getMessage());
        }
        return null;
    }
    
    public boolean checkfornull() {
        boolean ret = (vid == null) |
                      super.checkfornull();
        return ret;
    }

    public URL getVideoLink() { return videolink; }
    public void setVideoLink(URL videolink) { this.videolink = videolink; }
    public void setVideoLink(String str) {
        try {
            this.videolink = new URL ( str );
        } catch ( MalformedURLException e ) {
            Log.e(LOGTAG, "MalformedURLException exception. " + e.getMessage());
        }
    }

    public int getDuration() { return duration; }
    public void setDuration(int dur) { duration = dur; }

    public boolean getCanplay() { return canplay; }
    public void setCanplay(boolean canp) { canplay = canp; }

    public String getExtraVideoId() { return vid; }
    public void setExtraVideoId(String v) { vid = v; }

    public String toString() {
        StringBuilder vip = new StringBuilder();
        vip.append("vid:" + vid + ", ");
        vip.append( super.toString() );
        return vip.toString();
    }

    public JSONObject toJSON() { 
        try {
            JSONObject obj = super.toJSON()
            .put(DURATION, getDuration()) 
            .put(CANPLAY, getCanplay());
            return obj;
        } catch (JSONException e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
        return null;
    }


    // NOT IN USE

/*    private void readObject(ObjectInputStream s) 
                        throws IOException, ClassNotFoundException {
        //Log.d(LOGTAG, "readObject");
        s.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream s)
                        throws IOException {
        //Log.d(LOGTAG, "writeObject");
        s.defaultWriteObject();
    } */

}
