package com.feedeo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;

public class YouTubeUtility {
        static final String LOGTAG = "YouTubeUtility";
        static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?&video_id=";

        public static String extractVideoId(URL url) {

            // video id is in query -> www.youtube.com?v=VID&...
            {
            String[] lArgs = url.getQuery().split("&");
            for(int i=0; i<lArgs.length; i++) {
	        String[] lArgValStrArr = lArgs[i].split("=");
		if (lArgValStrArr != null) {
                    if (lArgValStrArr[0].equals("v")) {
                        return lArgValStrArr[1];
                    }
                }
            }
            }

            // video id is in path -> www.youtube.com/v/VID?..
            {
            String[] lArgs = url.getPath().split("/");
            int vidx = lArgs.length-2;
            if (vidx>0 && lArgs[vidx].equals("v")) {
                return lArgs[vidx+1];
            }
            }

            return null;
        }

        /* Pass me back the VideoInformation */
        public static Map<String,String> getVideoInformation(String pYouTubeVideoId)
			throws IOException, ClientProtocolException, UnsupportedEncodingException {
                //Log.d(LOGTAG, YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId);
		HttpClient lClient = new DefaultHttpClient();
		HttpGet lGetMethod = new HttpGet(YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId);
		HttpResponse lResp = null;
		lResp = lClient.execute(lGetMethod);
		ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
		String lInfoStr = null;
		lResp.getEntity().writeTo(lBOS);
		lInfoStr = new String(lBOS.toString("UTF-8"));
		String[] lArgs=lInfoStr.split("&");
		Map<String,String> lArgMap = new HashMap<String, String>();
		for(int i=0; i<lArgs.length; i++) {
			String[] lArgValStrArr = lArgs[i].split("=");
			if(lArgValStrArr != null){
				if(lArgValStrArr.length >= 2){
					lArgMap.put(lArgValStrArr[0], URLDecoder.decode(lArgValStrArr[1]));
				}
			}
		}
                return lArgMap;
        }      
	
	/**
	 * Calculate the YouTube URL to load the video.  Includes retrieving a token that YouTube
	 * requires to play the video.
	 * 
	 * @param pYouTubeFmtQuality quality of the video.  17=low, 18=high
	 * @param bFallback whether to fallback to lower quality in case the supplied quality is not available
	 * @param pYouTubeVideoId the id of the video
	 * @return the url string that will retrieve the video
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws UnsupportedEncodingException
	 */
	public static String calculateYouTubeUrl(Map<String,String> lArgMap, String pYouTubeFmtQuality, boolean pFallback) 
                 throws IOException, ClientProtocolException, UnsupportedEncodingException {

		String lUriStr = null;
		
		//Find out the URI string from the parameters
		
		//Populate the list of formats for the video
		String lFmtList = URLDecoder.decode(lArgMap.get("fmt_list"));
		ArrayList<Format> lFormats = new ArrayList<Format>();
		if(null != lFmtList){
			String lFormatStrs[] = lFmtList.split(",");
			
			for(String lFormatStr : lFormatStrs){
				Format lFormat = new Format(lFormatStr);
				lFormats.add(lFormat);
			}
		}
		
		//Populate the list of streams for the video
		String lStreamList = lArgMap.get("url_encoded_fmt_stream_map");
		if(null != lStreamList){
			String lStreamStrs[] = lStreamList.split(",");
			ArrayList<VideoStream> lStreams = new ArrayList<VideoStream>();
			for(String lStreamStr : lStreamStrs){
				VideoStream lStream = new VideoStream(lStreamStr);
				lStreams.add(lStream);
			}	
			
			//Search for the given format in the list of video formats
			// if it is there, select the corresponding stream
			// otherwise if fallback is requested, check for next lower format
			int lFormatId = Integer.parseInt(pYouTubeFmtQuality);
			
			Format lSearchFormat = new Format(lFormatId);
			while(!lFormats.contains(lSearchFormat) && pFallback ){
				int lOldId = lSearchFormat.getId();
				int lNewId = getSupportedFallbackId(lOldId);
				
				if(lOldId == lNewId){
					break;
				}
				lSearchFormat = new Format(lNewId);
			}
			
			int lIndex = lFormats.indexOf(lSearchFormat);
			if(lIndex >= 0){
				VideoStream lSearchStream = lStreams.get(lIndex);
				lUriStr = lSearchStream.getUrl();
			}
			
		}		
		//Return the URI string. It may be null if the format (or a fallback format if enabled)
		// is not found in the list of formats for the video
		return lUriStr;
	}

        public static int getVideoDuration(Map<String,String> lArgMap) {
                int duration = Integer.parseInt(lArgMap.get("length_seconds"));
                return duration;
        }

        public static String getThumbnailUrl(Map<String,String> lArgMap) {
                String url = lArgMap.get("thumbnail_url");
                return url;
        }

	public static int getSupportedFallbackId(int pOldId){
		final int lSupportedFormatIds[] = {13,  //3GPP (MPEG-4 encoded) Low quality 
										  17,  //3GPP (MPEG-4 encoded) Medium quality 
										  18,  //MP4  (H.264 encoded) Normal quality
										  22,  //MP4  (H.264 encoded) High quality
										  37   //MP4  (H.264 encoded) High quality
										  };
		int lFallbackId = pOldId;
		for(int i = lSupportedFormatIds.length - 1; i >= 0; i--){
			if(pOldId == lSupportedFormatIds[i] && i > 0){
				lFallbackId = lSupportedFormatIds[i-1];
			}			
		}
		return lFallbackId;
	}

        public static boolean checkForYouTubeApp(String videoID) { 
            // default youtube app 
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoID)); 
            List<ResolveInfo> list = Utility.mContext.getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY); 
            if (list.size() == 0) { 
                // default youtube app not present or doesn't conform to the standard we know 
                // use our own activity
                return false; 
            }
            return true; 
        }
}
