package com.feedeo;

import android.util.Log;
import java.util.Date;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.StringBuilder;
import java.lang.ref.WeakReference;
import org.json.JSONObject;
import org.json.JSONException;
import com.feedeo.restfb.types.Post;

public class AnyPost implements Serializable {
    private static final String LOGTAG = "AnyPost";
    private static final long serialVersionUID = 2981227327981029282L;

    // postToJson
    public static final String OBJECTID = "objectid"; // id to the video object
    public static final String THUMBNAIL = "thumbnail";
    public static final String TITLE = "title";
    public static final String LIKES = "likes";
    public static final String CREATEDTIME = "createdtime";
    public static final String OWNERID = "ownerid";
    public static final String ISLIKEDBYME = "islikedbyme";

    private String name;
    private String objectid;
    private String source;
    private String link;
    private String picture;
    private Long nlikes;
    private Date createdtime;
    private String type;
    private String ownerid;
    private boolean islikedbyme;

    public AnyPost (Post post) { 
        objectid = post.getObjectId();
        name = post.getName();
        source = post.getSource();
        link = post.getLink();
        picture = post.getPicture();
        Long l = post.getLikesCount();
        nlikes = (l == null)? (0L):(l); 
        createdtime = post.getCreatedTime();
        type = post.getType();
        if ( null == type ) type = "link";
        if ( source == null ) {
            VideoSource vs = VideoSourceHelper.findVideoSource(link);
            if ( vs != VideoSource.UNKNOWN) 
                type = "video";
        }
        if (type.equals("video")) {
            AnyPostHelper aph = getAnyPostHelper();
            aph.findIsLikedByMe(post); // no callback
        }
    }

    protected AnyPost(AnyPost post) { // called from subclasses
        objectid = post.getObjectId();
        name = post.getName();
        source = post.getSource();
        link = post.getLink();
        picture = post.getThumbnail();
        nlikes = post.getLikesCount();
        createdtime = post.getCreatedTime();
        type = post.getType();
        ownerid = post.getOwnerId();
        islikedbyme = post.getIsLikedByMe();
    }

    private AnyPost() { }

    public AnyPostHelper getAnyPostHelper() {
        AnyPostHelper aph = new AnyPostHelper(this);
        return aph;
    }

    public boolean checkfornull() {
        boolean ret = (name == null) |
                      (source == null) |
                      (link == null) |
                      (picture == null) |
                      (type == null) ;
        return ret;
    }

    public String getObjectId() {
        return objectid;
    }

    // workaround to object id being null at times
    public void setObjectIdifNull(String id) { 
        if (objectid == null || objectid.equals(""))
            objectid = id;
    }

    public String getOwnerId() {
        return ownerid;
    }

    public void setOwnerId(String ownerid) {
        this.ownerid = ownerid;
    }

    public String getSource() {
        return source;
    }
  
    public void setSource(String source) {
        this.source = source; 
    }

    public String getLink() {
        return link;
    }
  
    public void setLink(String source) {
        this.link = link; 
    }

    public String getThumbnail() {
        return picture;
    } 

    public void setThumbnail(String pic) {
        this.picture = pic; 
    }

    public String getName() {
        return name;
    }

    public long getLikesCount() {
        return nlikes;
    }

    public boolean getIsLikedByMe() {
        return islikedbyme;
    }

    public void setIsLikedByMe(boolean islikedbyme) {
         if (this.islikedbyme != islikedbyme) { // change in count
             if (islikedbyme) ++nlikes;
             else             --nlikes;
         }
         this.islikedbyme = islikedbyme;
    }

    public Date getCreatedTime() {
        return createdtime;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        StringBuilder anp = new StringBuilder();
        anp.append("objectid:" + objectid);
        anp.append(", name:" + name);
        anp.append(", source:" + source);
        anp.append(", picture:" + picture);
        anp.append(", likesc:" + nlikes);
        anp.append(", createtime:" + createdtime);
        anp.append(", type:" + type);
        anp.append(", ownerid:" + ownerid);
        return (anp.toString());
    }

    public JSONObject toJSON() { 
        try {
            JSONObject obj = new JSONObject() 
            .put(OBJECTID, getObjectId()) 
            .put(THUMBNAIL, getThumbnail()) 
            .put(TITLE, getName()) 
            .put(LIKES, getLikesCount()) 
            .put(CREATEDTIME, Utility.formatter.format(getCreatedTime()))
            .put(OWNERID, getOwnerId())
            .put(ISLIKEDBYME, getIsLikedByMe());
            return obj;
        } catch (JSONException e) {
            Log.e(LOGTAG,"Error ..." + e.getMessage());
        }
        return null;
    }

    public static Builder GetBuilder() {
        return new AnyPost.Builder();
    }

    public static class Builder {
        public String name;
        public String objectid;
        public String source;
        public String link;
        public String picture;
        public Long   nlikes;
        public Date   createdtime;
        public String type;
        public String ownerid;
        public boolean islikedbyme;

        public AnyPost doBuild() {
            AnyPost pv = new AnyPost();
            pv.name = name;
            pv.objectid = objectid;
            pv.source = source;
            pv.link = link;
            pv.picture = picture;
            pv.nlikes = nlikes;
            pv.createdtime = createdtime;
            pv.type = type;
            pv.ownerid = ownerid;
            pv.islikedbyme = islikedbyme;
            return pv;
        }
    }

    // NOT IN USE

/*    private void readObject(ObjectInputStream s) 
                        throws IOException, ClassNotFoundException {
        //Log.d(LOGTAG, "readObject");
        s.defaultReadObject();
        name = (String) s.readObject();
        objectid = (String) s.readObject();
        source = (String) s.readObject();
        link = (String) s.readObject();
        picture = (String) s.readObject();
        nlikes = s.readLong();
        createdtime = (Date) s.readObject();
    }

    private void writeObject(ObjectOutputStream s)
                        throws IOException {
        //Log.d(LOGTAG, "writeObject");
        s.defaultWriteObject();
        s.writeObject(name);
        s.writeObject(objectid);
        s.writeObject(source);
        s.writeObject(link);
        s.writeObject(picture);
        s.writeLong(nlikes);
        s.writeObject(createdtime);
    } */

}
