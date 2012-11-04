package com.feedeo;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.Runnable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.Intent;

public class FeedeoData {
    private static final String LOGTAG = "FeedeoData";
    private static FeedeoData instance;

    // Object states
    public static final int LOGGEDOUT_NONE = 0;
    public static final int LOGGEDIN_LOADING = 1;
    public static final int LOGGEDIN_NONE = 2;

    private int state;
    private LocalDataManager ldm;
    private UpdateManager vim;
    private Handler mUIHandler; 

    // Streams
    private static final int MAX_STREAMS = 2;
    public static final int STREAM_ME = 0;
    public static final int STREAM_ALL = 1;

    private FbQueryManager[] qac;
    private long[] startep; // for different streams
    private List<ConcurrentLinkedQueue<String>> pipe; // communicates objectid b/w this and UI
    private int curstream = STREAM_ALL; // current stream - 0 ME , 1 ALL , 2 Group A , 3 ... so on

    public static FeedeoData getInstance() {
        if (instance == null) 
            instance = new FeedeoData();
        return instance;
    }

    public void releaseInstance() {
        instance = null;
    }

    private FeedeoData()
    {
        this.state = LOGGEDIN_NONE;
        this.mUIHandler = null;
        this.ldm = this.new LocalDataManager();
        this.vim = this.new UpdateManager();
        this.startep = new long[MAX_STREAMS];
        this.pipe = new ArrayList<ConcurrentLinkedQueue<String>> ();
        for (int i = 0 ; i < MAX_STREAMS ; i++) {
             this.pipe.add( new ConcurrentLinkedQueue<String> () ); 
        }
        this.qac = new FbQueryManager[MAX_STREAMS]; 
    }

    protected void finalize() {
        if (ldm != null)
            ldm.cleanup();
        ldm = null;
        pipe = null; 
        vim = null;
        qac = null;
        mUIHandler = null;
    }

// ------ public functions - get/set functions --------------------------

    public void setCallback(Handler newHandler) {
        if (state == LOGGEDIN_LOADING) {
            synchronized (mUIHandler) {
                this.mUIHandler = newHandler;
            }
        } else {
            // if we are not loading, there should be no concurrency/even might be null
            this.mUIHandler = newHandler;
        }
    }

    // returns previous stream
    public int setDataStream(int cur) {
        if (state == LOGGEDOUT_NONE) return -1;

        Fb fb = Fb.getInstance();
        if (cur == 1 && !fb.isFriendListAvailable() ) return -2; // not ready yet 
        if (cur == 0 && !fb.isUserInfoAvailable() ) return -3; // not ready yet

        int pcur = curstream;
        if (pcur != cur) {
            if (state == LOGGEDIN_LOADING) {
                loadDataStop(); // stop the current query
            }
            curstream = cur;
            sendStreamUpdateMessage();
        }
        return (pcur);
    }

    public int getDataStream() {
        return curstream;
    }

    public long getStreamEpoch(int stream) {
        if ( stream < 0 || stream >= MAX_STREAMS ) return 0;
        return startep[stream];
    }

    public String nextInPipe() {
        try {
            return pipe.get(curstream).remove();
        } catch (NoSuchElementException e) { 
            Log.e(LOGTAG, "nextInPipe() called on empty pipe. " + e.getMessage()); 
        }
        return null; 
    }

    public AnyPostHelper getAnyPostHelper(String objectId) {
       if (vim == null || state == LOGGEDOUT_NONE) return null; 
       return vim.getAnyPostHelper(objectId);
    }

    public VideoHelper getVideoHelper(String objectId) {
       if (vim == null || state == LOGGEDOUT_NONE) return null; 
       return vim.getVideoHelper(objectId);
    }

    public VideoPost getMeta(String objectId) {
       if (vim == null || state == LOGGEDOUT_NONE) return null; 
       VideoHelper vh = vim.getVideoHelper(objectId);
       if (vh != null) {
           VideoPost vp = vh.getMeta();
           return vp;
       }
       return null;
    }

    public synchronized void resetOnLogout() {
        //Log.d(LOGTAG, "resetOnLogout() - [ " + state + " ]" );
        if (state == LOGGEDOUT_NONE) return;
        if (state == LOGGEDIN_LOADING) {
            loadDataStop();
        }
        state = LOGGEDOUT_NONE;
        sendStateUpdateMessage();
        ldm.cleanrecords(); 
        ldm.cleanup(); 
    }

    // no local load
    public void loadDataStart() {
        //Log.d( LOGTAG, "loadDataStart() - [ " + state + " ]" );
        if (state == LOGGEDIN_LOADING) {
            loadDataStop();
        }

        if (state == LOGGEDIN_NONE) {
            startep[curstream] = System.currentTimeMillis()/1000;
            qac[curstream] = this.new FbQueryManager(curstream);
            qac[curstream].start();
        }
    }

    public void loadDataMore() {
        if (state == LOGGEDIN_LOADING) {
            return; // already loading data
        }
        if (state == LOGGEDIN_NONE) {
            if (startep[curstream] == 0L) {
                startep[curstream] = System.currentTimeMillis()/1000;
            }
            if (qac[curstream] == null) { // Lazy initialization
                qac[curstream] = this.new FbQueryManager(curstream);
            }
            qac[curstream].start();
        }
    }

    public void loadDataStop() {
        //Log.d( LOGTAG, "loadDataStop() - [ " + state + " ]" );
       if (state != LOGGEDIN_LOADING) return;
       qac[curstream].stop();
    }

// ------ Messaging ------------------------------------------------------

    private void sendStateUpdateMessage() {
        //Log.d(LOGTAG, "   sendStateUpdateMessage() - [ " + state + " ]" );
        Message msg = new Message();
        msg.what = 0; // state message
        msg.arg1 = state;
        synchronized (mUIHandler) {
            mUIHandler.sendMessage(msg);
        }
    }

    private void sendVideoMetaUpdateMessage() {
        //Log.d(LOGTAG, "   sendVideoMetaUpdateMessage() - [ " + state + " ]" );
        Message msg = new Message();
        msg.what = 0; // state message
        msg.arg1 = state;
        msg.arg2 = 1; // Meta info update message
        synchronized (mUIHandler) {
            mUIHandler.sendMessage(msg);
        }
    }

    private void sendStreamUpdateMessage() {
        //Log.d(LOGTAG, "    sendStreamUpdateMessage()");
        Message msg = new Message();
        msg.what = 6; // SET_STREAM
        msg.arg1 = curstream;
        synchronized (mUIHandler) {
            mUIHandler.sendMessage(msg); // SET_STREAM
        }
    }

    private void sendQueryEpochUpdateMessage() {
        Message msg = new Message();
        msg.what = 7; // QUERY_EPOCH_UPDATE
        msg.arg1 = curstream;
        synchronized (mUIHandler) {
            mUIHandler.sendMessage(msg); // SET_STREAM
        }
    }

// ------ Private misc ------------------------------------------

    private void addtopipe(String objid, int stream) {
        if (!pipe.get(stream).contains(objid)) 
            pipe.get(stream).add(objid);
    }

// ------ Local Data Manager ------------------------------------
    
    public class LocalDataManager {
        private static final String tag = "LocalDataManager";
        public static final String POSTS = "POSTS"; // Caching data

        private Map<String,Object> myhelper;
        private DBHelper dbhelper;

        LocalDataManager() {
            myhelper = new HashMap<String,Object>();
            dbhelper = new DBHelper( Utility.mContext );
        }

        // always do a get before add (to avoid clashes)
        public synchronized void add(Object obj) {
            //Log.d(tag, "    add()"); 
            if ( state == LOGGEDOUT_NONE ) return;
            if (!(obj instanceof AnyPost)) return;
            AnyPost ap = (AnyPost) obj;
            String objid = ap.getObjectId();
            //Log.d(tag, "        object -> " + ap);
            if ( myhelper.containsKey(objid) ) return;
            myhelper.put (objid, obj);
            if ( ap.checkfornull() == true ) {
                Log.w(tag, "    object has null's");
                //return;
            }
            if (obj instanceof VideoPost) {
                //Log.d(tag, "         add VideoPost");
                VideoPost mvp = (VideoPost) obj;
                dbhelper.insert(mvp); // this can be delayed
            } else if (obj instanceof AnyPost) {
                //Log.d(tag, "        add AnyPost");
                dbhelper.insert(ap);
            }
        }

        public synchronized void update(String objid) {
            //Log.d(tag, "    update()"); 
            if ( state == LOGGEDOUT_NONE ) return;
            if ( !myhelper.containsKey(objid) ) return;
            // Not called for any other object 
            VideoPost vp = (VideoPost) myhelper.get(objid);
            //Log.d(tag, "        object -> " + vp);
            if ( vp.checkfornull() == true ) {
                Log.w(tag, "    object has null's");
                //return;
            }
            dbhelper.update(vp);
        }

        public synchronized Object get(String objid) {
            if (state == LOGGEDOUT_NONE) return null;
            Object obj = myhelper.get(objid);
            //Log.d(tag, "    get (" + objid + ")");
            if (obj == null) {
                obj = dbhelper.get(objid);
		//Log.d(tag, "        got from db");
            }
            return obj;
        }

        // called on logout
        public synchronized void cleanrecords() {
           //Log.d(tag, "   cleanrecords() - [ " + state + " ]");
           assert (state != LOGGEDIN_NONE);
           dbhelper.deleteAll();
        }

        public synchronized void cleanup() {
            //Log.d(tag, "   cleanup() - [ " + state + " ]" );
            assert (state != LOGGEDIN_NONE);
            dbhelper.cleanup();
        }
    }

// --------------------------------------------------------------

    /* UpdateManager
     *  - finds conclusively if they can be played or not
     *    informs the results back to UI via a sendMessage
     *  - suggest downloading video players for media that can not be 
     *    played
     *  - keep check on any new video players added, and when they are
     *    check if any files can be played. 
     */
    public class UpdateManager implements HelperCallback {
        private static final String tag = "UpdateManager";

        UpdateManager() {
        }

        public void add(VideoHelper vhp, int stream) {
           // TODO if we start buffering helpers later, we should set the callback to null
           //      to avoid garbage collection problems.
            vhp.setcallback(this, stream); 
            vhp.prepare1();
        }

        public AnyPostHelper getAnyPostHelper(String objid) {
            Object obj = ldm.get(objid);
            if (obj instanceof AnyPost) {
                AnyPost ap = (AnyPost) obj;
                AnyPostHelper aph = ap.getAnyPostHelper();
                aph.setcallback(this, curstream);
                return aph;
            }
            return null;
        }

        public VideoHelper getVideoHelper(String objid) {
            Object obj = ldm.get(objid);
            if (obj instanceof VideoPost) {
                VideoPost mvp = (VideoPost) obj;
                // TODO if mvp is not prepare, return null, run prepare() and update UI from callback
                //      also check, issue warnings if get() is getting called multiple times with the
                //      same object id (We dont want parallel threads doing prepare on same object)
                VideoHelper vhp = mvp.getVideoHelper();
                vhp.setcallback(this, curstream);
                return vhp;
            }
            return null;
        }

        // Callback methods
        public void uiupdate(String objid, int stream) {
            // TODO update UI, save self again
            //Log.d(tag, "    received update from " + objid);
            addtopipe (objid, stream);
            if (stream == curstream) { 
                sendVideoMetaUpdateMessage();
            }
            ldm.update(objid);
        }
    }
 
// --------------------------------------------------------------

    public class FbQueryManager implements FbQueryCallback, PostQueryCallback {
        private static final String tag = "FbQueryManager";

        private int fcurstream;
        private PostQueryManager pqmgr; 
        private ArrayList<FbQuery> fquery;

        private boolean loading;
        private AtomicInteger fqcount;
        private AtomicInteger pqcount;

        private ConcurrentLinkedQueue<VideoPost> pipe2;
        private ConcurrentLinkedQueue<AnyPost>   pipe3;

        FbQueryManager(int curstream) {
            //Log.d(tag, "    create()");
            this.fcurstream = curstream;
            // this is an occasion for creating new pipe (of parent object) 
            // the whole UI gets refreshed, any prev updates are meaningless
            pipe2 = new ConcurrentLinkedQueue<VideoPost> ();
            pipe3 = new ConcurrentLinkedQueue<AnyPost> ();
            pqmgr = PostQueryManager.getInstance();
            pqcount = new AtomicInteger(0);
            fqcount = new AtomicInteger(0);
            fquery = new ArrayList<FbQuery> (); // TODO add query from local data manager

            long starttime = startep[fcurstream];
            if ( fcurstream == FeedeoData.STREAM_ME ) {
                fquery.add (new FQLStreamQuery(starttime));
            } else if ( fcurstream == FeedeoData.STREAM_ALL ) {
                fquery.add (new FQLLinksQueryAllFriends(starttime));
            }

            /* Links query is no longer requried, we are getting the same data 
             *  from stream query (and also more efficiently).
             *  fquery.add (new FQLLinksQuery(startep[fcurstream])); */
            //fquery.add (new FQLLikeQuery());
            //fquery.add (new FQLStreamQuery1(startep[fcurstream]));
            //fquery.add (new FQLLinksQuery1(startep[fcurstream]));

            for (int i=0; i<fquery.size(); i++) {
                fquery.get(i).setCallback(this);
            }
        }

        public synchronized void start() {
            //Log.d(tag, "    start()");
            state = LOGGEDIN_LOADING; 
            sendStateUpdateMessage();
            fqcount.set(fquery.size());
            for (int i=0; i<fquery.size(); i++) {
                fquery.get(i).executeNextAsync(); 
            }
            loading=true;
            watchdog_start();
            sendQueryEpochUpdateMessage();
        }

        // can cancel fql queries, but not individual post queries
        public synchronized void stop() {
            //Log.d(tag, "    stop()");
            if (!loading) return;
            loading=false;
            watchdog_cancel();
            for (int i=0; i<fquery.size(); i++) {
                fquery.get(i).stop();
            }
            state = LOGGEDIN_NONE;
            sendStateUpdateMessage();
        }

        // ---- The handler ---------------------------
        private static final int WATCHDOG_RESET = 1;
        private static final int ADD_SAVE = 2;
        private static final int ADD_NOSAVE = 3;
        private static final int SKIP_SAVE = 4;
        private static final int SKIP_NOSAVE = 5;
        private static final int MOREDATA = 6;

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (instance == null) return; //exiting
                if (loading) watchdog_reset(); // ! stop()
                //Log.d(tag, "    message = " + msg.what);
                switch(msg.what) {
                case ADD_NOSAVE:  
                case ADD_SAVE: { 
                    VideoPost mvp = pipe2.remove();
                    String objid = mvp.getObjectId();
                    if (msg.what == ADD_SAVE) ldm.add(mvp);
                    addtopipe (objid, fcurstream); // sends to UI pipe, even after stop()
                    vim.add (mvp.getVideoHelper(), fcurstream);
                    if (loading) {    // ! stop() 
                        sendVideoMetaUpdateMessage();
                    }
                    break; }
                case SKIP_NOSAVE:  
                case SKIP_SAVE: { 
                    AnyPost post = pipe3.remove();
                    String objid = post.getObjectId(); 
                    if (msg.what == SKIP_SAVE) ldm.add(post);
                    break; }
                case MOREDATA: {
                    synchronized(this) { // race with stop()
                        if (loading) { // ! stop()
                            fqcount.set(fquery.size());
                            for (int i=0; i<fquery.size(); i++) 
                                fquery.get(i).executeNextAsync(); 
                            sendQueryEpochUpdateMessage();
                        }
                    }
                    break; }
                }
            }
        };

        // ---- callback functions ---------------------

             // -- FbQuery --

        public void queryupdates() {
            //Log.d(tag, "    queryupdates()");
            handler.sendEmptyMessage(WATCHDOG_RESET);
        }

        public void nextpostid(String fbid, String objid, String ownid) {
            //Log.d(tag, "    nextpostid( " + fbid + " , " + objid + " , " + ownid + " )");
            Object obj = ldm.get(objid); 
            if (obj != null) {
                if (obj instanceof VideoPost) {
                    pipe2.add((VideoPost)obj); 
                    handler.sendEmptyMessage(ADD_NOSAVE);
                } else {
                    pipe3.add ((AnyPost)obj);
                    handler.sendEmptyMessage(SKIP_NOSAVE);
                }
            } else {
                pqcount.incrementAndGet();
                // doesnt exist locally, get it from net
                pqmgr.doasyncquery (fbid, objid, ownid, this);
            }
        }

        public void querycomplete(long nextstart_epoch) {
            //Log.d(tag, "    querycomplete()");
            startep[fcurstream] = nextstart_epoch;
            if ( 0 == fqcount.decrementAndGet() )
                checkcomplete();
        }

             // -- PostQueryManager --

        public void postquerycomplete(AnyPost apost) {
            //Log.d(tag, "    Anypost -> " + apost);
            if ("video".equals(apost.getType())) {
                VideoPost mvp = new VideoPost(apost);
                //Log.d(tag, "    add( " + mvp.getObjectId() + " )");
                pipe2.add(mvp); 
                handler.sendEmptyMessage(ADD_SAVE);
            } else {
                //Log.d(tag, "    skip( " + apost.getObjectId() + " )");
                pipe3.add (apost);
                handler.sendEmptyMessage(SKIP_SAVE);
            }
            if (0 == pqcount.decrementAndGet()) 
                checkcomplete();
        }

        public void postqueryerror(String loid) {
            handler.sendEmptyMessage(WATCHDOG_RESET);
            if (0 == pqcount.decrementAndGet()) 
                checkcomplete();
        }

        // -- Private functions --

        private void checkcomplete() {
            //Log.d(tag, "    checkcomplete()"); 
            if ( 0 == fqcount.get() && 0 ==  pqcount.get() ) {
               //Log.d(tag, "    -> " + fqcount.get() + ", " + pqcount.get()); 
               handler.sendEmptyMessage(MOREDATA);
            }
        }

        // -- WATCHDOG --
        private static final long LONGDELAY = 120*1000; // 120 secs to response
        private Handler watchdog = new Handler();;
        private Runnable dogwatchdog = new Runnable() {
            public void run() { 
                Log.w(tag, "    watchdog fired");
                stop(); 
            } 
        };

        private void watchdog_start() {
            watchdog.postDelayed(dogwatchdog, LONGDELAY);
        }
        
        private void watchdog_cancel() {
            watchdog.removeCallbacks(dogwatchdog);
        }

        private void watchdog_reset() {
            watchdog.removeCallbacks(dogwatchdog);
            watchdog.postDelayed(dogwatchdog, LONGDELAY);
        }
    }
}
