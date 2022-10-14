package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.spdb.dao.TableInfoDao;
import com.fdev.database.spdb.entity.TableInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;


@Repository
public class TableInfoDaoImpl implements TableInfoDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public TableInfo add(TableInfo tableInfo) {
        tableInfo.initId();
        return mongoTemplate.save(tableInfo);
    }

    @Override
    public void delete(TableInfo tableInfo) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = tableInfo ==null?"{}":mapper.writeValueAsString(tableInfo);
        Query query =new BasicQuery(json);
        mongoTemplate.remove(query, TableInfo.class);

    }

    @Override
    public List<TableInfo> query(TableInfo tableInfo) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = tableInfo ==null?"{}":mapper.writeValueAsString(tableInfo);
        Query query =new BasicQuery(json);
        return  mongoTemplate.find(query, TableInfo.class);
    }


}
