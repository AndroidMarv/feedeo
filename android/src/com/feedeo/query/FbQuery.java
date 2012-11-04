package com.feedeo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.lang.InterruptedException;

import com.facebook.android.FacebookError;

/* Query examples,
 *
 */

/* The mechanism used to find out if this class has completed its work is approximate.
 */

public abstract class FbQuery extends AbstractQuery {
    private static final String LOGTAG = "FbQuery";

    protected FbQueryTask querymanager;
    private LinkedList<String> querylist;
    protected AtomicInteger qcount;

    private FbQueryCallback callback;
    private long startep;

    public FbQuery() {
        callback = null;
        querymanager = null;
        querylist = new LinkedList<String> ();
        qcount = new AtomicInteger(0);
    }

    public FbQuery(long _startep) {
        this();
        startep = _startep;
    }

    public void setCallback(FbQueryCallback callback) {
        this.callback = callback;
    }

    public synchronized void executeNextAsync() {
        if (callback == null) return;
        if (querylist.size() > 0) {
            //Log.d(LOGTAG, "Continue... " + querylist.size() + " queries left.");
        } else {
            preparequeries();
        }
        querymanager = this.new FbQueryTask();
        querymanager.execute();
    }

    public synchronized void stop() {
        if (querymanager != null) {
            querymanager.stop();
            querymanager = null;
        }
    }
    
    private void preparequeries() {
        StringBuilder q = new StringBuilder();
        q.append ( getQuery() );
        if (startep > 0) {
            long endep = startep - getQueryBreakFactor();
            q.append ( " created_time <= " + startep + " AND " );
            q.append ( " created_time >= " + endep + " AND " );
	}
        String basequery = q.toString();

        Iterator itr = getIndexClausesIterator();
        while ( itr.hasNext() ) {
            String query = basequery + itr.next();
            querylist.offer(query); 
            qcount.incrementAndGet();
        }
    }

    protected class FbQueryTask extends AsyncTask<Void, Void, Void> {
        private final Semaphore available; 
        private boolean stop;

        public FbQueryTask() {
            available = new Semaphore(getParallelQueries(), true);
            stop = false;
        }

        public void stop() {
            stop = true;
            this.cancel(true);
        }

        @Override
        protected Void doInBackground(Void...unused) {
            try {
                while ( querylist.size() > 0 && !isCancelled() && !stop ) {
                    available.acquire(); // limit number of new queries
                    if ( isCancelled() || stop ) break;
                    String query = querylist.poll();
                    //Log.d(LOGTAG, "Querying " + query); 

                    Bundle params = new Bundle();
                    params.putString("method", "fql.query");
                    params.putString("format", "json");
                    params.putString("access_token", Fb.getInstance().fb().getAccessToken());
                    params.putString("query", query);
                    Fb.getInstance().async().request(null, params, new FQLRequestListener());
               }
            } catch (InterruptedException e1) {
                Log.e(LOGTAG, "Thread was interrupted. " + e1.getMessage());
            }
            return null;
         }

        private class FQLRequestListener extends BaseRequestListener {
            @Override
            public void onComplete(final String response, final Object state) {
                //Log.d(LOGTAG, "onComplete()");
                callback.queryupdates();
                available.release();
                try {
                    JSONArray recs = new JSONArray(response);
                    //Log.d(LOGTAG, "onComplete( " + recs.length() + " ) - " + recs.toString());
                    for (int i = 0; i < recs.length(); i++) {
                        String fbres = getResourseId(recs.getJSONObject(i));
                        String fbvid = getVideoId(recs.getJSONObject(i));
                        String fbown = getOwnerId(recs.getJSONObject(i));
                        callback.nextpostid( fbres, fbvid , fbown );
                    }
                } catch (JSONException e) {
                    try {
                        JSONObject json = new JSONObject(response);
                        Log.e(LOGTAG, json.toString());
                    } catch (JSONException e1) {
                        Log.e(LOGTAG, e1.toString());
                    }
                } finally { 
                    checkandcomplete();
                }
            }

            public void onFacebookError(FacebookError error) {
                Log.e(LOGTAG, error.toString());
                available.release();
                checkandcomplete();
            }

            private void checkandcomplete() {
                if ( 0 == qcount.decrementAndGet() ) {
                    synchronized (FbQuery.this) {
                        querymanager = null;
                    }
                    startep = startep - getQueryBreakFactor() - 1;
                    callback.querycomplete(startep);
                }
            }
        }
    }
} 
