package com.spdb.fdev.fdevinterface.spdb.dao.util;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * 封装更新参数
 */
public class BathUpdateOption {

    private Query query;
    private Update update;
    private boolean upsert = false;
    private boolean multi = false;

    public BathUpdateOption(Query query, Update update, boolean upsert, boolean multi) {
        this.query = query;
        this.update = update;
        this.upsert = upsert;
        this.multi = multi;
    }

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

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }
}
