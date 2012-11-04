package com.feedeo;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.lang.NullPointerException;
import java.util.Date;

public class DBHelper {
    public static final String LOGTAG = "DBHelper";

    public static final String DB_NAME = "posts";
    public static final int DB_VERSION = 3;

    public static final String DB_TABLE_VID = "videoposts";
    public static final String[] COLS1 = new String[] {
        "objectid", "canplay", "duration", "vid", "videolink" }; // VideoPost

    public static final String DB_TABLE_ANY = "anyposts"; // AnyPost - (uses selective fields)
    public static final String[] COLS2 = new String[] {
        "objectid", "thumbnail", "title", "source", "link", "nlikes", "createdtime", "type", "ownerid", "islikedbyme"
    };

    private SQLiteDatabase db;
    private final DBOpenHelper dbOpenHelper;

    // TYPES - only used within database
    private static final String VIDEO = "video";

    private static class DBOpenHelper extends SQLiteOpenHelper {

        private static final String DB_CREATE1 = "CREATE TABLE " 
            + DBHelper.DB_TABLE_VID
            +   "( objectid VARCHAR(48) UNIQUE, "
            +   " canplay INTEGER, duration INTEGER, vid TEXT, videolink TEXT, "
            +   " rowid1 INTEGER PRIMARY KEY AUTOINCREMENT); ";

        private static final String DB_CREATE2 = "CREATE TABLE " 
            + DBHelper.DB_TABLE_ANY
            +   "( objectid VARCHAR(48) UNIQUE, "
            +   " thumbnail TEXT, title TEXT, " 
            +   " source TEXT, link TEXT, "
            +   " nlikes LONG,  createdtime DATE, type TEXT, "
            +   " ownerid TEXT, islikedbyme INTEGER, "  
            +   " rowid2 INTEGER PRIMARY KEY AUTOINCREMENT); ";

        public DBOpenHelper(Context context, String dbName, int version) {
            super (context, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL(DBOpenHelper.DB_CREATE1);
                db.execSQL(DBOpenHelper.DB_CREATE2);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.e(LOGTAG, e.getMessage());
            } finally {
                db.endTransaction();
            } 
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.beginTransaction();
            try {
                db.execSQL("DROP TABLE IF EXISTS " + DBHelper.DB_TABLE_VID);
                db.execSQL("DROP TABLE IF EXISTS " + DBHelper.DB_TABLE_ANY);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            } 
            onCreate(db);
        }
    }

    // Database commands

    public DBHelper(Context context) {
        dbOpenHelper = new DBOpenHelper(context, "WR_DATA", 1);
        establishDb();
    }

    private void establishDb() {
        if (db == null) {
            db = dbOpenHelper.getWritableDatabase();
        }
    }

    public void cleanup() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    // Table videoposts

    public void insert(VideoPost vp) {
        ContentValues anyvals = createContentValuesforAnyPost(vp);
        ContentValues vidvals = createContentValuesforVideoPost(vp);

        db.beginTransaction();
        try {
          db.insert(DBHelper.DB_TABLE_ANY, null, anyvals);
          db.insert(DBHelper.DB_TABLE_VID, null, vidvals);
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        } 
    }

    public void update(VideoPost vp) {
        ContentValues anyvals = createContentValuesforAnyPost(vp);
        ContentValues vidvals = createContentValuesforVideoPost(vp);

        db.beginTransaction();
        try {
          db.update(DBHelper.DB_TABLE_ANY, anyvals, 
                         "objectid='" + vp.getObjectId() + "'", null);
          db.update(DBHelper.DB_TABLE_VID, vidvals, 
                         "objectid='" + vp.getObjectId() + "'", null);
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        } 
    }

    // Table anyposts

    public void insert(AnyPost ap) {
        ContentValues anyvals = createContentValuesforAnyPost(ap);
        db.insert(DBHelper.DB_TABLE_ANY, null, anyvals);
    }

    public void update(AnyPost ap) {
        ContentValues anyvals = createContentValuesforAnyPost(ap);
        db.update(DBHelper.DB_TABLE_ANY, anyvals,
                         "objectid='" + ap.getObjectId() + "'", null);
    }

    public AnyPost get(String objectid) {
        Cursor c = null;
        AnyPost ap = null;
        try {
            c = db.query(true, DBHelper.DB_TABLE_ANY, null, "objectid='" + objectid + "'", 
                    null, null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                AnyPost.Builder apb = AnyPost.GetBuilder();
                apb.objectid = c.getString(0);
                apb.picture = c.getString(1);
                apb.name = c.getString(2);
                apb.source = c.getString(3); 
                apb.link = c.getString(4); 
                apb.nlikes = c.getLong(5);
                try { 
                    apb.createdtime = new Date(c.getString(6)); 
                } catch (IllegalArgumentException ex) { 
                    Log.e(LOGTAG, "Error: " + ex.getMessage()); 
                }
                apb.type = c.getString(7);
                apb.ownerid = c.getString(8);
                apb.islikedbyme = ( c.getInt(9) == 1 ) ? (true) : (false);
                ap = apb.doBuild();
            }
        } catch (SQLException e) {
            Log.v (LOGTAG, e.getMessage() );
        } finally {
            if ( c != null && !c.isClosed() ) {
                c.close();
            }
        }
        
        if (ap == null || !ap.getType().equals(VIDEO)) return ap;

        VideoPost vp = null;
        try {
            c = db.query(true, DBHelper.DB_TABLE_VID, null, "objectid='" + objectid + "'", 
                    null, null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                vp = new VideoPost(ap);
                vp.setCanplay ( (c.getInt(1) == 1) ? (true) : (false) );
                vp.setDuration (c.getInt(2));
                vp.setExtraVideoId (c.getString(3));
                vp.setVideoLink (c.getString(4));
            }
        } catch (SQLException e) {
            Log.v(LOGTAG, e.getMessage() );
        } finally {
            if ( c != null && !c.isClosed() ) {
                c.close();
            }
        }
        return vp;
    }
    
    public void delete(String objectid) {
        // TODO do this as a single transaction
        db.delete(DBHelper.DB_TABLE_VID, "objectid='" + objectid + "'", null);
        db.delete(DBHelper.DB_TABLE_ANY, "objectid='" + objectid + "'", null);
    }

    // clear all data in case of logout
    public void deleteAll() {
        db.delete(DBHelper.DB_TABLE_VID, null, null);
        db.delete(DBHelper.DB_TABLE_ANY, null, null);
    }

// ------------------------------------------------------------------

/* 
    private void cleanContentValues(ContentValues cv) {
        Set<Entry<String,Object>> set = cv.valueSet();
        Iterator<Entry<String,Object>> itr = set.iterator();
        while (itr.hasNext()) {
            Entry entry = (Entry) itr.next();
            if ( entry.getValue() == null ) {
                cv.remove ( (String) entry.getKey());
            }
        }
    }
*/

    private ContentValues createContentValuesforVideoPost(VideoPost vp) {
        ContentValues vidvals = new ContentValues();
        vidvals.put("objectid", vp.getObjectId() );  // VideoPost
        vidvals.put("canplay", vp.getCanplay() ? (1) : (0) ); 
        vidvals.put("duration", vp.getDuration() );
        if ( null != vp.getExtraVideoId() )
          vidvals.put("vid", vp.getExtraVideoId() );
        if ( null != vp.getVideoLink() ) 
          vidvals.put("videolink", vp.getVideoLink().toExternalForm());
        return vidvals;
    }

    private ContentValues createContentValuesforAnyPost(AnyPost post) {
        ContentValues anyvals = new ContentValues();
        anyvals.put("objectid", post.getObjectId() );  
        if ( null != post.getThumbnail() )
          anyvals.put("thumbnail", post.getThumbnail() );
        if ( null != post.getName() )
          anyvals.put("title", post.getName() );
        if ( null != post.getSource() )
          anyvals.put("source", post.getSource() );
        if ( null != post.getLink() )
          anyvals.put("link", post.getLink() );
        anyvals.put("nlikes", post.getLikesCount() );

        String createdtime;
        try {
            createdtime = Utility.formatter.format(post.getCreatedTime());
            anyvals.put("createdtime", createdtime );
        } catch (NullPointerException ex) {
            Log.e(LOGTAG, "Date - " + post.getCreatedTime());
            Log.e(LOGTAG, "Error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Log.e(LOGTAG, "Date - " + post.getCreatedTime());
            Log.e(LOGTAG, "Error: " + ex.getMessage());
        }

        if ( null != post.getType() )
          anyvals.put("type", post.getType() );
        anyvals.put("ownerid", post.getOwnerId());
        anyvals.put("islikedbyme", post.getIsLikedByMe());
        return anyvals;
    }
}
