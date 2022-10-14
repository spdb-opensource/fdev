package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.Dict;
import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.dao.AppDao;
import com.fdev.database.spdb.dao.DatabaseDao;
import com.fdev.database.spdb.entity.App;
import com.fdev.database.spdb.entity.Database;
import com.fdev.database.util.CommonUtils;
import com.fdev.database.util.DatabaseUtil;
import com.spdb.fdev.common.exception.FdevException;
import net.sf.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;


@Repository
public class DatabaseDaoImpl implements DatabaseDao {

    @Resource
    private MongoTemplate mongoTemplate;
    
    @Resource
    private AppDao appDao;

    @Override
    public Database add(Database database) {
        database.initId();
       return mongoTemplate.save(database);
    }

    @Override
    public List<Map>  query(Database database) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(database);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)){
                continue;
             } else if (Dict.DATABASE_NAME.equals(key)) {
                c.and(key).regex((String) value);
            } else if (Dict.TABLE_NAME.equals(key)) {
                c.and(key).regex(((String) value).toUpperCase());
            } else {
                c.and(key).is(value);
            }
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation applookup = Aggregation.lookup(Dict.APPINFO, Dict.APPID, Dict.ID, Dict.APPINFO);
        AggregationResults<Map> docs = mongoTemplate.aggregate(Aggregation.newAggregation(applookup, match), Dict.DATABASE, Map.class);
        List<Map> result = new ArrayList<>();
        docs.forEach(doc -> {
            if(((List) doc.get(Dict.APPINFO)).size()> 0) {
                doc.put(Dict.APPINFO, ((List) doc.get(Dict.APPINFO)).get(0));
                result.add(doc);
            }
        });
        return result;
    }

    @Override
    public Database update(Database database) throws Exception{
        Query query = Query.query(Criteria.where(Dict.ID).is(database.getId()));
        Update update = Update.update(Dict.APPID, database.getAppid()).set(Dict.DATABASE_TYPE, database.getDatabase_type())
                .set(Dict.DATABASE_NAME, database.getDatabase_name()).set(Dict.TABLE_NAME, database.getTable_name());
        return mongoTemplate.findAndModify(query, update, Database.class);
    }

    @Override
    public Map<String, Object> queryInfo(String appid, String database_type, String database_name, String table_name, String status, Map spdb_manager, int page, int per_page) throws Exception{
        Map<String, Object> maplist = new HashMap<>();
        //条件查询
        Criteria c = new Criteria();
        if(!CommonUtils.isNullOrEmpty(appid) || !CommonUtils.isNullOrEmpty(spdb_manager)){
            if(!CommonUtils.isNullOrEmpty(appid)){
                c.and(Dict.APPID).is(appid);
            }
            if(!CommonUtils.isNullOrEmpty(spdb_manager)){
                List<Map> spdb_managers = new ArrayList<>();
                spdb_managers.add(spdb_manager);
                App app = new App();
                app.setSpdb_managers(spdb_managers);
                List<App> apps = appDao.queryAppName(app);
                List<String> appids = new ArrayList<>();
                for(App app1 : apps){
                    appids.add(app1.getId());
                }
                if(!CommonUtils.isNullOrEmpty(appid)){
                    if(!appids.contains(appid)){
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"","应用下无相关负责人"});
                    }
                } else {
                    c.and(Dict.APPID).in(appids);
                }
            }
        }else{
            c.and(Dict.APPID).exists(true);
        }
        if(!CommonUtils.isNullOrEmpty(database_type)){
            c.and(Dict.DATABASE_TYPE).is(database_type);
        }
        if(!CommonUtils.isNullOrEmpty(database_name)){
            c.and(Dict.DATABASE_NAME).regex(database_name);
        }
        if(!CommonUtils.isNullOrEmpty(table_name)){
            c.and(Dict.TABLE_NAME).regex(table_name);
        }
        if(!CommonUtils.isNullOrEmpty(status)){
            c.and(Dict.STATUS).is(status);
        }
        Query query = Query.query(c);
        Long total = mongoTemplate.count(query, Database.class);
        maplist.put(Dict.TOTAL, total);
        
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation applookup = Aggregation.lookup(Dict.APPINFO, Dict.APPID, Dict.ID, Dict.APPINFO);
        Aggregation aggregation ;
        if(per_page == 0){
            aggregation = Aggregation.newAggregation(applookup, match, Aggregation.sort(Sort.Direction.DESC, Dict.ID));
        } else {
            aggregation = Aggregation.newAggregation(
                    applookup, match, Aggregation.sort(Sort.Direction.DESC, Dict.ID), Aggregation.skip(page > 1 ? (page - 1) * per_page : 0), Aggregation.limit(per_page));
        }
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.DATABASE, Map.class);
        List<Map> result = new ArrayList<>();
        docs.forEach(doc -> {
            if(((List) doc.get(Dict.APPINFO)).size()> 0) {
                doc.put(Dict.APPINFO, ((List) doc.get(Dict.APPINFO)).get(0));
                result.add(doc);
            }
        });
        maplist.put("dbs", result);
        return maplist;
    }

    @Override
    public List<Database> queryName(Database database) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = database ==null?"{}":mapper.writeValueAsString(database);
        Query query =new BasicQuery(json);
        if(CommonUtils.isNullOrEmpty(database.getAppid()))
            query.addCriteria(Criteria.where(Dict.APPID).exists(true));
        return mongoTemplate.find(query, Database.class);
    }

    @Override
    public List<Database> queryAll(Database database) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = database ==null?"{}":mapper.writeValueAsString(database);
        Query query =new BasicQuery(json);
        return mongoTemplate.find(query, Database.class);
    }

    @Override
    public void delete(Database database) throws Exception{
        Query query =Query.query(Criteria.where(Dict.ID).is(database.getId()));
        mongoTemplate.remove(query, Database.class);
    }

    @Override
    public void collectByTableName(Database database) throws Exception{
        //条件查询
        Criteria c = new Criteria();
        if(!CommonUtils.isNullOrEmpty(database.getDatabase_type())){
            c.and(Dict.DATABASE_TYPE).is(database.getDatabase_type());
        }
        if(!CommonUtils.isNullOrEmpty(database.getDatabase_name())){
            c.and(Dict.DATABASE_NAME).is(database.getDatabase_name());
        }
        if(!CommonUtils.isNullOrEmpty(database.getTable_name())){
            c.and(Dict.TABLE_NAME).is(database.getTable_name());
        }
        Query query = Query.query(c);
        List<Database> databases = mongoTemplate.find(query, Database.class);
        if(databases.size()>0){
            List<Database> databasesSet = DatabaseUtil.setDatabase(databases);
           for(Database database1 : databasesSet){
               database.setDatabase_type(database1.getDatabase_type());
               database.setDatabase_name(database1.getDatabase_name());
               database.setColumns(database1.getColumns());
               database.initId();
               mongoTemplate.save(database);
            }
        } 
    }

    @Override
    public void deleteBydb(Database database) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = database ==null?"{}":mapper.writeValueAsString(database);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            } else if(Dict.AUTOFLAG.equals(key)){
                c.and(key).ne(value);
            } else {
                c.and(key).is(value);
            }
        }
        Query query = Query.query(c);
        mongoTemplate.remove(query, Database.class);

    }

    @Override
    public void updateByTbNameAndAppid(Database database) {
        Query query = Query.query(Criteria.where(Dict.TABLE_NAME).is(database.getTable_name()).and(Dict.APPID).is(database.getAppid())
                .and(Dict.ID).is(database.getId()));
        Update update = Update.update(Dict.TBNUM, database.getTbNum()).set(Dict.AUTOFLAG, database.getAutoflag());;
        mongoTemplate.findAndModify(query, update, Database.class);
    }

    @Override
    public void updateById(Database database) {
        Query query = Query.query(Criteria.where(Dict.ID).is(database.getId()));
        Update update = Update.update(Dict.TBNUM, database.getTbNum());
        mongoTemplate.findAndModify(query, update, Database.class);
    }

    @Override
    public void Confirm(Database database) {
        Query query =Query.query(Criteria.where(Dict.ID).is(database.getId()));
        Update update = Update.update(Dict.STATUS, "1");
        mongoTemplate.findAndModify(query, update, Database.class);
    }

    @Override
    public List<Database> queryByAutoflag(Database database) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = database ==null?"{}":mapper.writeValueAsString(database);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            } else if(Dict.AUTOFLAG.equals(key)){
                c.and(key).ne(value);
            } else {
                c.and(key).ne(value);
            }
        }
        Query query = Query.query(c);
       return mongoTemplate.find(query, Database.class);
    }

    @Override
    public List<Database> queryTbName(Database database) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = database ==null?"{}":mapper.writeValueAsString(database);
        Query query =new BasicQuery(json);
        if(!CommonUtils.isNullOrEmpty(database.getAppid())){
            query.addCriteria(Criteria.where(Dict.APPID).nin(database.getAppid()));
        }
        return mongoTemplate.find(query, Database.class);
    }

}

