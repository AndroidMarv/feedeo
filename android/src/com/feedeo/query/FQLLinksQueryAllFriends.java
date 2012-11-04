package com.feedeo;

import android.content.Context;
import android.util.Log;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

/* example of a query
 *
 *   fql?q=SELECT link_id, url, image_urls FROM link WHERE owner=me() AND created_time > 1308501626 LIMIT 100
 *
 */

public class FQLLinksQueryAllFriends extends FbQuery  {
    private final static String LOGTAG = "FQLLinksQueryAllFriends";
    private final static String RESOURCE = "link_id";
    private final static String OWNER = "owner";
    private final static String TABLE = "link";
    private final static long BREAKFACTOR = 12*60*60; // last 12 hours
    private final static int MAX_PARALLEL_QUERIES = 12;

    public FQLLinksQueryAllFriends(long _startep) {
        super(_startep);
    }

    @Override
    protected int getParallelQueries() { return MAX_PARALLEL_QUERIES; }

    @Override
    protected String getTableName() { return TABLE; }

    @Override
    protected long getQueryBreakFactor() { return BREAKFACTOR; }

    @Override
    protected String getQueryType() { return "fql"; }

    @Override
    protected String getQuery() {
        StringBuilder q = new StringBuilder();
        q.append ("SELECT " + RESOURCE + ", " + OWNER + " FROM " + TABLE + " WHERE ");
        return q.toString(); 
    }

    @Override
    protected Iterator getIndexClausesIterator() {
        ArrayList<String> clarr = new ArrayList<String> ();
        Iterator itr = Fb.getInstance().getFriendList().values().iterator();
        Friend fr;
        while ( itr.hasNext() ) {
            StringBuilder cl = new StringBuilder();
            fr = (Friend) itr.next();
            cl.append( OWNER )
              .append( " IN (" )
              .append( fr.getId() );
            for (int i=1; i<4 && itr.hasNext(); i++) {
                fr = (Friend) itr.next();
                cl.append( ", " )
                  .append( fr.getId() );
            }
            cl.append( ")" );
            clarr.add( cl.toString() );
        }
        return (clarr.iterator());
    }
 
    @Override 
    protected String getResourseId(JSONObject rec) throws JSONException {
         StringBuilder id = new StringBuilder();
         id.append(rec.getString(OWNER))
                      .append("_")
                      .append(rec.getString(RESOURCE));
         return (id.toString());
    }

    @Override 
    protected String getVideoId(JSONObject rec) throws JSONException {
        return rec.getString(RESOURCE);
    }

    @Override
    protected String getOwnerId(JSONObject rec) throws JSONException {
        return rec.getString(OWNER);
    }
} 
