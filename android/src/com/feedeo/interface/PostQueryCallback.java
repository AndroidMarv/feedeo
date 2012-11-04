package com.feedeo;

interface PostQueryCallback {
    public void postquerycomplete(AnyPost apost);
    public void postqueryerror(String loid);
}
