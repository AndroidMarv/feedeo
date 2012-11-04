package com.feedeo;

import android.util.Log;

import java.net.URL;
import java.net.MalformedURLException;

class VideoSourceHelper {
    private static String LOGTAG = "VideoSourceHelper";

    private static RecognizedSources[] sources = {
        new RecognizedSources("youtube", VideoSource.YOUTUBE),
        new RecognizedSources("netflix", VideoSource.DEFAULT),
        new RecognizedSources("hulu", VideoSource.DEFAULT),
        new RecognizedSources("dailymotion", VideoSource.DEFAULT),
        new RecognizedSources("metacafe", VideoSource.DEFAULT),
        new RecognizedSources("vids.myspace", VideoSource.DEFAULT),
        new RecognizedSources("video.yahoo", VideoSource.DEFAULT),
        new RecognizedSources("vimeo", VideoSource.DEFAULT),
        new RecognizedSources("break", VideoSource.DEFAULT),
        new RecognizedSources("tv", VideoSource.DEFAULT),
        new RecognizedSources("veoh", VideoSource.DEFAULT),
        new RecognizedSources("videobash", VideoSource.DEFAULT),
        new RecognizedSources("ovguide", VideoSource.DEFAULT),
        new RecognizedSources("vevo", VideoSource.DEFAULT),
        new RecognizedSources("liveleak", VideoSource.DEFAULT) 
    };

    public static VideoSource findVideoSource (URL url) {
        String domain = url.getHost().toLowerCase();
        for (int i = 0; i < sources.length; i++) {
            if (domain.indexOf(sources[i].keyword) >= 0) {
                return sources[i].typeid;
            }
        }
        return VideoSource.UNKNOWN;
    }

    public static VideoSource findVideoSource (String link) {
        try { 
           URL url = new URL ( link );
           return VideoSourceHelper.findVideoSource(url);
        } catch (MalformedURLException ex) {
           Log.e(LOGTAG, "Error: " + ex.getMessage());
        } catch (NullPointerException ex) {
           Log.e(LOGTAG, "Error: " + ex.getMessage());
        }
        return VideoSource.UNKNOWN;
    }

    private static class RecognizedSources {
        public String keyword;
        public VideoSource typeid;

        public RecognizedSources(String keyword, VideoSource typeid) {
            this.keyword = keyword;
            this.typeid = typeid;
        }
    }
} 
