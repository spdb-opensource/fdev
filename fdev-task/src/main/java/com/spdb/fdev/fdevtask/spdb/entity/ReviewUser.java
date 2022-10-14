package com.spdb.fdev.fdevtask.spdb.entity;

import org.springframework.data.mongodb.core.mapping.Field;

public class ReviewUser {

    private String name;
    @Field("cid")
    private String cid;

    public ReviewUser() {

    }

    public String getName() {
        return name;
    }

    public ReviewUser(String name, String cid) {
        this.name = name;
        this.cid = cid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getcid() {
        return cid;
    }

    public void setcid(String cid) {
        this.cid = cid;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if(obj instanceof ReviewUser){
                ReviewUser newOne = (ReviewUser)obj;
                return newOne.getName().equalsIgnoreCase(this.name) && newOne.getcid().equals(this.cid);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
