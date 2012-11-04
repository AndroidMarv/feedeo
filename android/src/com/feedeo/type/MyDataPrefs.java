package com.feedeo;

import android.util.Log;

// TODO - use enum bitflags later, instead of individual boolean expressions

public class MyDataPrefs {
   private static final String LOGTAG = "MyDataPrefs";

   // BASIC FIELDS
   
   //public boolean postedbymeonly;
   public boolean haslimit;
   public int limit;
   public int days;

   // DERIVED FIELDS

   public boolean hasfromdate;
   public long fromepoch;
   public boolean hastodate;
   public long toepoch;    

   // UPDATE FIELDS
   public void update_derived_fields() {
        long curepoch = System.currentTimeMillis()/1000;
        hastodate = false;
        toepoch = 0;
        hasfromdate = true;
        fromepoch = curepoch - days*24*60*60; // minus 3 months
   }

   public boolean need_refresh(MyDataPrefs old) {
        if ( old.limit != limit ) return true;
        if ( old.days  != days ) return true; 
        return false;
   }

   public boolean pass( AnyPost ap ) {
/*        if (hasfromdate || hastodate) {
            if (ap.getCreatedTime() == null) return false;

            long aptime = ap.getCreatedTime().getTime()/1000;
            if (hasfromdate) {
                if (aptime < fromepoch) return false; 
            }
            if (hastodate) {
                if (aptime > toepoch) return false;
            }
        }
*/        return true;
   }

}
