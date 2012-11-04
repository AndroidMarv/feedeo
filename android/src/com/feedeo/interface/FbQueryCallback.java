package com.feedeo;

interface FbQueryCallback {
    public void queryupdates();
    public void nextpostid(String fbid, String loid, String ownid);
    public void querycomplete(long nextstart_epoch);
}
