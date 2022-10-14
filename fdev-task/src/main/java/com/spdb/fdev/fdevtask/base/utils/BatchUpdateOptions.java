package com.spdb.fdev.fdevtask.base.utils;


import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


public class BatchUpdateOptions {

    private Query query;
    private Update update;
    private boolean upsert;
    private boolean mult;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public boolean isUpsert() {
        return upsert;
    }

    public void setUpsert(boolean upsert) {
        this.upsert = upsert;
    }

    public boolean isMult() {
        return mult;
    }

    public void setMult(boolean mult) {
        this.mult = mult;
    }


}
