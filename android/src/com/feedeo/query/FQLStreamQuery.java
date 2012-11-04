package com.feedeo;

import android.content.Context;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

/* example of a query
 *
 *   fql?q=SELECT post_id FROM stream WHERE source_id=me() AND type IN ('80','128') AND created_time>(now()-30*24*60*60)
 *
 */

public class FQLStreamQuery extends FbQuery  {
    private final static String LOGTAG = "FQLStreamQuery";
    private final static String RESOURCE = "post_id";
    private final static String OWNER = "source_id";
    private final static String TABLE = "stream";
    private final static long BREAKFACTOR = 3*24*60*60; // 3 days
    private final static int MAX_PARALLEL_QUERIES = 1;

    public FQLStreamQuery(long _startep) {
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
        q.append ("SELECT " + RESOURCE + "," + OWNER + " FROM " + TABLE + " WHERE ");
        //q.append ("filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed') AND ");
        q.append ("type in ('80', '128') AND "); // 80 -> link, 128 -> uploaded video
        return q.toString(); 
    }

    @Override
    protected Iterator getIndexClausesIterator() {
        ArrayList<String> cl = new ArrayList<String>();
        cl.add("source_id=me()");
        return (cl.iterator());
    }
 
    @Override 
    protected String getResourseId(JSONObject rec) throws JSONException {
         return rec.getString(RESOURCE);
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
