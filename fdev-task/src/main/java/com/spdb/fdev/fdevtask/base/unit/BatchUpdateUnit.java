package com.spdb.fdev.fdevtask.base.unit;

import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.BatchUpdateOptions;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量更新组件
 */
@Component
public class BatchUpdateUnit {

    /**
     * 构造更新参数
     *
     * @param fdevTask
     * @return
     */
    public BatchUpdateOptions buildBatchUpdateOption(FdevTask fdevTask) {
        BatchUpdateOptions options = new BatchUpdateOptions();
        Query query = new Query()
                .addCriteria(Criteria.where(Dict.ID)
                        .is(fdevTask.getId()));
        options.setQuery(query);
        Update update = new Update();
        update.set("newDoc", fdevTask.getNewDoc());
        options.setUpdate(update);
        return options;
    }

    /**
     * 批量更新
     *
     * @param mongoTemplate 操作集
     * @param collName     表名
     * @param optionsList  批量对象
     * @param ordered      按顺序更新
     * @return
     */
    public int executeBatchUpdate( MongoTemplate mongoTemplate,
                                  String collName,
                                  List<BatchUpdateOptions> optionsList,
                                  boolean ordered) {
        BasicDBObject commad = new BasicDBObject();
        commad.put("update", collName);
        List<BasicDBObject> batchList = new ArrayList<>();
        optionsList.forEach(option -> {
            BasicDBObject update = new BasicDBObject();
            update.put("q", option.getQuery().getQueryObject());
            update.put("u", option.getUpdate().getUpdateObject());
            update.put("upsert", false);
            update.put("multi", option.isMult());
            batchList.add(update);
        });
        commad.put("updates", batchList);
        commad.put("ordered", ordered);
        Document document = mongoTemplate.getDb().runCommand(commad);
        return Integer.parseInt(document.get("n").toString());
    }
}
