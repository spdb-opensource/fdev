package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.DictUseRecordDao;
import com.fdev.database.spdb.entity.DictUseRecord;
import com.fdev.database.util.CommonUtils;
import org.apache.commons.lang.StringUtils;
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
public class DictUseRecordDaoImpl implements DictUseRecordDao {

    @Resource
    private MongoTemplate mongoTemplate;


    @Override
    public void add(DictUseRecord dictUseRecord) {
        dictUseRecord.initId();
        mongoTemplate.save(dictUseRecord);
    }

    @Override
    public DictUseRecord queryByKey(DictUseRecord dictUseRecord) {
        Query query = Query.query(Criteria.where(Dict.SYS_ID).is(dictUseRecord.getSys_id())
                .and(Dict.DATABASE_TYPE).is(dictUseRecord.getDatabase_type())
                .and(Dict.DATABASE_NAME).is(dictUseRecord.getDatabase_name())
                .and(Dict.TABLE_NAME).is(dictUseRecord.getTable_name()));
        return  mongoTemplate.findOne(query, DictUseRecord.class);
    }

    @Override
    public List<DictUseRecord> query(DictUseRecord dictUseRecord) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = dictUseRecord ==null?"{}":mapper.writeValueAsString(dictUseRecord);
        Query query =new BasicQuery(json);
        return mongoTemplate.find(query, DictUseRecord.class);
    }

    @Override
    public Map<String, Object> queryUseRecordByPage(String sys_id, String database_type, String database_name, String table_name, int page, int per_page) {
        Map<String, Object> result = new HashMap<>();
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(sys_id)) {
            c.and(Dict.SYS_ID).is(sys_id);
        }
        if (StringUtils.isNotBlank(database_type)) {
            c.and(Dict.DATABASE_TYPE).is(database_type);
        }
        if (StringUtils.isNotBlank(database_name)) {
            c.and(Dict.DATABASE_NAME).is(database_name);
        }
        if (StringUtils.isNotBlank(table_name)) {
            c.and(Dict.TABLE_NAME).is(table_name);
        }
        Query query = new Query(c);
        Long total = mongoTemplate.count(query, DictUseRecord.class);
        result.put(Dict.TOTAL, total);

        AggregationOperation match = Aggregation.match(c);
        AggregationOperation syslookup = Aggregation.lookup("sysInfo", "sys_id", "sys_id", "sysInfo");
        Aggregation aggregation ;
        if(per_page == 0){
            aggregation = Aggregation.newAggregation(match, syslookup, Aggregation.sort(Sort.Direction.DESC,Dict.USERECORD_ID));
        } else {
            aggregation = Aggregation.newAggregation(match, syslookup, Aggregation.sort(Sort.Direction.DESC,Dict.USERECORD_ID),
                    Aggregation.skip(page > 1 ? (page - 1) * per_page : 0), Aggregation.limit(per_page));
        }
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, "dictUseRecord", Map.class);
        List<Map> mapList = new ArrayList<>();
        docs.forEach(doc -> {
            doc.put("sysInfo", ((List) doc.get("sysInfo")).get(0));
            if(CommonUtils.isNullOrEmpty(doc.get(Dict.NEW_FIELD))){
                doc.put(Dict.NEW_FIELD, new ArrayList<>());    //前端要求送值
            }
            mapList.add(doc);
        });
        result.put("dbs", mapList);
        return result;
    }

    @Override
    public DictUseRecord queryById(String useRecord_id) {
        Query query = Query.query(Criteria.where(Dict.USERECORD_ID).is(useRecord_id));
        return  mongoTemplate.findOne(query, DictUseRecord.class);
    }

    @Override
    public void updateUseRecord(DictUseRecord dictUseRecord) {
        Query query = Query.query(Criteria.where(Dict.USERECORD_ID).is(dictUseRecord.getUseRecord_id()));
        Update update = Update.update(Dict.TABLE_NAME, dictUseRecord.getTable_name())
                .set(Dict.ALL_FIELD, dictUseRecord.getAll_field())
                .set(Dict.NEW_FIELD, dictUseRecord.getNew_field())
                .set(Dict.IS_NEW_TABLE, "N")
                .set(Dict.REMARK, dictUseRecord.getRemark())
                .set(Dict.LAST_MODI_TIME, dictUseRecord.getLast_modi_time())
                .set(Dict.LAST_MODI_USERNAMEEN, dictUseRecord.getLast_modi_userNameEn())
                .set(Dict.LAST_MODI_USERNAME, dictUseRecord.getLast_modi_userName());
        mongoTemplate.findAndModify(query, update, DictUseRecord.class);
    }
}
