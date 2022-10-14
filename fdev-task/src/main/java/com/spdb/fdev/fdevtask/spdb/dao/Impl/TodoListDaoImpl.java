package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.DeleteResult;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.ITodoListDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTaskCollection;
import com.spdb.fdev.fdevtask.spdb.entity.ToDoList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class TodoListDaoImpl implements ITodoListDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public ToDoList save(ToDoList toDoList) {
        return mongoTemplate.save(toDoList);
    }

    @Override
    public ToDoList query(ToDoList toDoList) {
        ToDoList todo = mongoTemplate.findOne(Query.query(
                Criteria.where(Dict.PROJECT_ID).is(toDoList.getProject_id()).
                andOperator(Criteria.where("merge_id").is(toDoList.getMerge_id()))),
                ToDoList.class);
        return todo;
    }

    @Override
    public ToDoList update(ToDoList toDoList) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(toDoList);
        BasicDBObject taskcollectionkJson = BasicDBObject.parse(json);
        Iterator<String> it = taskcollectionkJson.keySet().iterator();
        Query query = Query.query(Criteria.where(Dict.ID).is(toDoList.getId()));
        Update update = Update.update(Dict.ID, toDoList.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskcollectionkJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key)) continue;
            update.set(key, value);
        }
       return mongoTemplate.findAndModify(query, update, ToDoList.class);
    }

    @Override
    public List<ToDoList> queryByParam(ToDoList toDoList) throws JsonProcessingException {
        List<ToDoList> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = toDoList == null ? "{}" : objectMapper.writeValueAsString(toDoList);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude("_id");
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<ToDoList> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "todo_list", ToDoList.class);
        docs.forEach(doc -> {
            result.add(doc);
        });
        return result;
    }

    /**
     * 删除todo待办
     *
     * @param toDoList
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public Long remove(ToDoList toDoList) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = toDoList == null ? "{}" : objectMapper.writeValueAsString(toDoList);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            c.and(key).is(value);
        }
        Query query = new Query(c);
        DeleteResult deleteResult = this.mongoTemplate.remove(query, ToDoList.class, "todo_list");
        return deleteResult.getDeletedCount();
    }
}
