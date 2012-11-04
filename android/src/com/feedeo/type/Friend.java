package com.feedeo;

import java.io.Serializable;

public class Friend implements Serializable {
    private String name;
    private String id;
    private String pic_square;

    public String getName() { return name; }
    public Friend setName(String name) { this.name = name; return this; }

    public String getId() { return id; }
    public Friend setId(String id) { this.id = id; return this; }

    public String getPicture() { return pic_square; }
    public Friend setPicture(String pic) { this.pic_square = pic; return this; }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof Friend) ) {
            return false;
        }
        Friend fr = (Friend) obj;
        return ( name.equals( fr.name ) && 
                 id.equals( fr.id ) && 
                 pic_square.equals( fr.pic_square ) );
    }
}
