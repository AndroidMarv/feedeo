package com.feedeo;

import java.util.List;
import java.util.Iterator;
import android.util.Log;
import android.os.Bundle;

import com.facebook.android.FacebookError;

import com.feedeo.restfb.types.Post;
import com.feedeo.restfb.types.NamedFacebookType;

public class AnyPostHelper {
    private static final String LOGTAG = "AnyPostHelper";

    private AnyPost post;
    private HelperCallback callback;
    private int stream;

    public AnyPostHelper(AnyPost post) {
        this.post = post;
    }

    public void setcallback ( HelperCallback hc , int stream ) {
        this.callback = hc;
        this.stream = stream;
    }

    public AnyPost getData () {
        return post;
    }

    // the other option is to retreive likes using graph query
    public void findIsLikedByMe (Post p) { 
        if (null == Fb.getInstance().getUserUID() ||
            null == p.getLikes() ) return;
        List<NamedFacebookType> data = p.getLikes().getData();
        Iterator itr = data.iterator();
        boolean islikedbyme = false;
        while (itr.hasNext()) {
            NamedFacebookType nft = (NamedFacebookType) itr.next(); 
            String id = nft.getId();
            if ( id != null && id.equals( Fb.getInstance().getUserUID() ) ) {
                islikedbyme = true;
                break;
            }
        }
        post.setIsLikedByMe(islikedbyme);
    }

    public void like() {
        boolean liked = false;
        Bundle params = new Bundle();
        params.putString("access_token", Fb.getInstance().fb().getAccessToken());
        if ( post.getIsLikedByMe() ) {
            params.putString("method", "DELETE"); // unlike
            liked = false;
        } else {
            params.putString("method", "POST"); // like
            liked = true;
        }
        Fb.getInstance().async().request( "/" + post.getObjectId() + "/likes", params, new LikeRequestListener(liked) ); 
    }

    public void share(Bundle params) {
        params.putString("method", "POST");
        params.putString("format", "json");
        params.putString("access_token", Fb.getInstance().fb().getAccessToken());
        Fb.getInstance().async().request("me/links", params, new ShareRequestListener() );
    } 

 // ------------------------------------------------------

    public class LikeRequestListener extends BaseRequestListener {
        boolean liked;

        public LikeRequestListener(boolean liked) {
            this.liked = liked;
        }

        @Override
        public void onComplete(final String response, final Object state) {
            post.setIsLikedByMe(liked);
            if (null != callback)
                callback.uiupdate(post.getObjectId(), stream); 
        }

        public void onFacebookError(FacebookError error) {
            Utility.toast("Could not like!");
        }
    }

    public class ShareRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            // TODO  we could update new post in stream 0
        }

        public void onFacebookError(FacebookError error) {
            Utility.toast("Could not share!");
        }
    }
}
