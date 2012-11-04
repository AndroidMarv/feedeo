package com.feedeo;

import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractQuery {
    abstract protected int getParallelQueries();
    abstract protected long getQueryBreakFactor();
    abstract protected String getTableName(); 
    abstract protected String getQueryType(); 
    abstract protected String getQuery(); 
    abstract protected String getResourseId(JSONObject rec) throws JSONException; 
    abstract protected String getVideoId(JSONObject rec) throws JSONException; 
    abstract protected String getOwnerId(JSONObject rec) throws JSONException; 
    abstract protected Iterator getIndexClausesIterator();
}
