
package com.feedeo;

import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;

import java.net.URL;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.lang.ClassNotFoundException;
import com.feedeo.restfb.types.Post;

/* Currently has code to handle videos from
 *  - YouTube
 *  - FB
 */

public class VideoHelper {
//                implements OnBufferingUpdateListener, OnCompletionListener,
//                           OnPreparedListener, OnVideoSizeChangedListener 
    private static final String LOGTAG = "VideoHelper";
    private static final int STATUS_OK = 0;

    // VideoHelper fields
    private VideoPost post;
    private boolean canplay;
    private int duration;
    private VideoSource videosource;
    private URL videolink;
    private HelperCallback callback;
    private int stream;

    // YouTube fields
    private String vid;

    // State fields
    private boolean prepare1_done;

    public VideoHelper (VideoPost post) 
                    throws MalformedURLException {
        this.post = post;
        duration = post.getDuration();
        canplay = post.getCanplay();
        vid = post.getExtraVideoId();
        videolink = post.getVideoLink();
        if (vid != null) {
            videosource = VideoSourceHelper.findVideoSource (videolink.toExternalForm());
            prepare1_done = true;
        } else {
            prepare1_done = false;
        }
    }

    public void setcallback ( HelperCallback hc , int stream ) {
        this.callback = hc;
        this.stream = stream;
    }

    // STAGE 1 - Prepare 
    public void prepare1() {
        if (prepare1_done) return;
        try {
            PrepareStage1 ps1 = this.new PrepareStage1(callback, stream);
            ps1.execute();
        } catch (MalformedURLException ex) {
            Log.e (LOGTAG, "MalformedURLException in prepare1 " + getObjectId() + ". "); 
        }
    }

    // STAGE 2 - Prepare 
    public void prepare2() {
        boolean pduration = false;
        boolean pthumb = false;
        //if (canplay == false) return;
        //if (duration <= 0) pduration = true;
        //if (post.getThumbnail() == null) pthumb = true; 
        // Recover metadata information
    }

    public Intent launchVideoIntent() {
        Intent i;
        Uri uri;
        if (! getCanplay()) {
            // Try downloading here
            return null;
        }
        if ( videosource == VideoSource.YOUTUBE ) {
            if ( vid != null && YouTubeUtility.checkForYouTubeApp (vid) ) {
                uri = Uri.parse("vnd.youtube:" + vid);
                i = new Intent("android.intent.action.VIEW", uri);
            } else {
                uri = Uri.parse(videolink.toExternalForm()); 
                i = new Intent("android.intent.action.VIEW");
                i.setDataAndType(uri, "video/*");
            }
        } else { // DEFAULT or UNKNOWN 
            uri = Uri.parse(videolink.toExternalForm()); 
            i = new Intent("android.intent.action.VIEW");
            i.setDataAndType(uri, "video/*");
        }
        //Log.d(LOGTAG, "Intent: " + i);
        return i;
    }

    // GET / SET METHODS

    @Override public int hashCode() {
        return post.getObjectId().hashCode();
    }

    public VideoPost getMeta() {
        return post;
    }

    public String getObjectId() {
        return post.getObjectId();
    }

    public String getExtraVideoIdeoId() {
        return vid;
    }

    public int getDuration() {
        return duration;
    }

    public boolean getCanplay() {
        return canplay;
    }

    public URL getVideoLink() {
        return videolink;
    }

    // Separating logic that helps during creating part only,
    // for creation of VideoHelper class.
    // This also modifies the Post object passed to it.
    private class PrepareStage1 extends AsyncTask<Void,Void,Void> {
      // YouTube errors
      private static final int CANT_EXTRACT_VID = -1;
      private static final int NO_PERMISSION_ON_MOBILE = 150;
    
      private int pstream;
      private HelperCallback pcallback;
      private URL pvideolink; // Video Link
      private String pthumbnail;
      private VideoSource pvideosource;
      private String pvid;
      private int pduration;
      private int perrcode;

      public PrepareStage1( HelperCallback callback , int stream ) throws MalformedURLException { 
          pcallback = callback;
          pstream = stream;
          pvideolink = new URL(videolink.toString());
          pvideosource = VideoSource.UNKNOWN;
          pthumbnail = post.getThumbnail();
          pduration = post.getDuration();
          perrcode = STATUS_OK;
      }

      @Override 
      protected Void doInBackground(Void...unused) {
        try {
            pvideosource = VideoSourceHelper.findVideoSource( pvideolink );
            // youtube  
            if (pvideosource == VideoSource.YOUTUBE) {
	        pvid = YouTubeUtility.extractVideoId( pvideolink );
                if (null == pvid) {
                    Log.w(LOGTAG, "Unable to extract video id from url. ");
                } else {
                    perrcode = retreiveYouTubeMetadata();
                }
            }
        } catch ( MalformedURLException e ) {
            Log.e (LOGTAG, "MalformedURLException while preparing " + getObjectId() + ". "); 
            Log.e (LOGTAG, "    -> " + pvideolink);
        } catch ( IOException e ) {
            Log.e (LOGTAG, "IOException while preparing " + getObjectId() + ". " + e); 
        }
        return null;
      }

      // modifying post in UI thread, to be on the thread safe side
      @Override 
      protected void onPostExecute(Void unused) {
        videosource = pvideosource; // update local vars
        duration = pduration;
        canplay = (perrcode == STATUS_OK);
        vid = pvid;
        try {
            videolink = new URL (pvideolink.toString());
        } catch (MalformedURLException ex) {
            Log.e (LOGTAG, "MalformedURLException in onPostExecute " + ex.getMessage());
        }

        post.setThumbnail(pthumbnail);
        post.setDuration(pduration);
        post.setCanplay(canplay);
        post.setExtraVideoId(pvid);
        post.setVideoLink(videolink);

        prepare1_done = true;
        pcallback.uiupdate(post.getObjectId(), pstream); 
      }

      private int retreiveYouTubeMetadata() throws IOException {
        Map<String, String> videoinfo = YouTubeUtility.getVideoInformation(pvid);
        if (videoinfo.get("status").equals("fail")) {
            Log.w(LOGTAG, "Unable to get videoinfo. ");
            return Integer.parseInt(videoinfo.get("errorcode"));
        } 

        // TODO ... this contains data, such as what kind of video, etc to play.
        //      ... maybe we can use it later
        // Uri pvideolinkuri = Uri.parse (YouTubeUtility.calculateYouTubeUrl(videoinfo, "18", true));
        
        if (null == pthumbnail || pthumbnail.equals("")) {
            pthumbnail = YouTubeUtility.getThumbnailUrl(videoinfo);
        }

        pduration = YouTubeUtility.getVideoDuration(videoinfo);
        return STATUS_OK; 
      }
    }
}
