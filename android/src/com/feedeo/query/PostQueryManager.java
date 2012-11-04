package com.feedeo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.os.Bundle;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.lang.InterruptedException;
import java.io.IOException;

import com.feedeo.restfb.types.Post;

import com.facebook.android.FacebookError;

public class PostQueryManager {
    protected final String LOGTAG = "PostQueryManager";
    private static PostQueryManager instance;
    private static final int MAX_AVAILABLE = 5;

    private final Semaphore available; 
    private PostQueryTask pqtask;

    public static PostQueryManager getInstance() {
        if (instance == null) {
            instance = new PostQueryManager();
        }
        return instance;
    }

    private PostQueryManager() {
        available = new Semaphore(MAX_AVAILABLE, true);
        pqtask = new PostQueryTask();
        pqtask.execute();
    }

    public void doasyncquery(String fbid, String loid, String ownid, PostQueryCallback callback) {
        PostQuery pq = new PostQuery(fbid, loid, ownid, callback);
        pqtask.add (pq);
    }

    private class PostQueryTask extends AsyncTask<Void,Void,Void> {
        private BlockingQueue<PostQuery> pqlist;

        public PostQueryTask() {
            pqlist = new LinkedBlockingQueue<PostQuery> ();
        } 

        public void add(PostQuery pq) {
            try { pqlist.put(pq); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        @Override
        protected Void doInBackground(Void...unused) {
            try {
                while (!isCancelled()) {
                    available.acquire(); // limit number of new queries
                    PostQuery pq = pqlist.take(); // Blocking call
                    if (!isCancelled() && pq != null) {
                        Bundle params = new Bundle();
                        params.putString("format", "json");
                        params.putString("access_token", Fb.getInstance().fb().getAccessToken());
                        Fb.getInstance().async().request(pq.fbid, params, new PostRequestListener(pq));
                    } else {
                        available.release();
                    }
                }
            } catch (InterruptedException e1) {
                Log.e(LOGTAG, "Thread was interrupted. " + e1.getMessage());
            }
            return null;
        }
    }

/*    private void setText(final String txt) {
        sendTextMessage(txt);
    } */

    private class PostRequestListener extends BaseRequestListener {
        private final String loid;
        private final String ownid;
        private final PostQueryCallback callback;

        PostRequestListener (PostQuery pq) {
            this.loid = pq.loid;
            this.ownid = pq.ownid;
            this.callback = pq.callback;
        }

        @Override
        public void onComplete(final String response, final Object state) {
            available.release();
            if (response.equals("false")) return;
            try {
                 //Log.d(LOGTAG, "onComplete -> " + response.toString());
                 JSONObject json = new JSONObject(response);
                 Post mypost = Utility.jsonMapper.toJavaObject(json.toString(), Post.class);
                 AnyPost apost = new AnyPost(mypost);
                 apost.setOwnerId (ownid);
                 apost.setObjectIdifNull (loid); // workaround - sometimes objectid is null
                 callback.postquerycomplete(apost);
            } catch (JSONException e) {
                 Log.e(LOGTAG, e.toString());
            }
        }

        public void onFacebookError(FacebookError error) {
              Log.e(LOGTAG, error.toString());
              callback.postqueryerror(loid);
        }
    }

    public class PostQuery {
        public String fbid;
        public String loid;
        public String ownid;
        public PostQueryCallback callback;

        PostQuery(String fbid, String loid, String ownid, 
                  PostQueryCallback callback) 
          { 
            this.fbid = fbid; 
            this.loid = loid; 
            this.ownid = ownid;
            this.callback = callback;
          }
    }

} 
