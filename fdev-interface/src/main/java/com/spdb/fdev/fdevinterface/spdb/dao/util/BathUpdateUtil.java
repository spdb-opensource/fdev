package com.spdb.fdev.fdevinterface.spdb.dao.util;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量更新工具类
 */
public class BathUpdateUtil {

    private BathUpdateUtil() {
    }

    public static int bathUpdate(MongoTemplate mongoTemplate, String collectionName, List<BathUpdateOption> options) {
        return doBathUpdate(mongoTemplate, collectionName, options, true);
    }

    public static int doBathUpdate(MongoTemplate mongoTemplate, String collectionName, List<BathUpdateOption> options, boolean ordered) {
        DBObject command = new BasicDBObject();
        command.put(Dict.UPDATE, collectionName);
        List<BasicDBObject> updateList = new ArrayList<>();
        for (BathUpdateOption option : options) {
            BasicDBObject update = new BasicDBObject();
            update.put(Dict.Q, option.getQuery().getQueryObject());
            update.put(Dict.U, option.getUpdate().getUpdateObject());
            update.put(Dict.UPSERT, option.isUpsert());
            update.put(Dict.MULTI, option.isMulti());
            updateList.add(update);
        }
        command.put(Dict.UPDATES, updateList);
        command.put(Dict.ORDERED, ordered);
        Document documentResult = mongoTemplate.getDb().runCommand((Bson) command);
        return Integer.parseInt(documentResult.get(Dict.N).toString());
    }

}
