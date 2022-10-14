package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.DataDictDao;
import com.fdev.database.spdb.dao.DictRecordDao;
import com.fdev.database.spdb.entity.DataDict;
import com.fdev.database.spdb.entity.DictRecord;
import com.fdev.database.util.CommonUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class DictRecordDaoImpl implements DictRecordDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private DataDictDao dataDictDao;

    @Override
    public void add(DictRecord dictRecord) {
        dictRecord.initId();
        mongoTemplate.save(dictRecord);
    }

    @Override
    public Map queryDictRecord(String sys_id, String database_type, String database_name, String field_en_name, int per_page, int page) throws Exception {
        Map<String, Object> result = new HashMap<>();
        DataDict dataDict = new DataDict();
        if(!CommonUtils.isNullOrEmpty(database_type)){
            dataDict.setDatabase_type(database_type);
        }
        if(!CommonUtils.isNullOrEmpty(database_name)){
            dataDict.setDatabase_name(database_name);
        }
        if(!CommonUtils.isNullOrEmpty(field_en_name)){
            dataDict.setField_en_name(field_en_name);
        }
        List<DataDict> dataDicts = dataDictDao.query(dataDict);
        Criteria c = new Criteria();
        if(!CommonUtils.isNullOrEmpty(sys_id)){
            c.and(Dict.SYS_ID).is(sys_id);
        }
        if(!CommonUtils.isNullOrEmpty(dataDicts)){
            List dictIds = new ArrayList();
            for(DataDict dataDict1 : dataDicts){
                dictIds.add(dataDict1.getDict_id());
            }
            c.and(Dict.DICT_ID).in(dictIds);
        } else {
            return result;
        }
        Query query = Query.query(c);
        Long total = mongoTemplate.count(query, "dictRecord");
        result.put(Dict.TOTAL, total);

        AggregationOperation match = Aggregation.match(c);
        AggregationOperation syslookup = Aggregation.lookup("sysInfo", "sys_id", "sys_id", "sysInfo");
        AggregationOperation dictRecordlookup = Aggregation.lookup("dataDict", Dict.DICT_ID, Dict.DICT_ID, "dataDict");
        Aggregation aggregation ;
        if(per_page == 0){
            aggregation = Aggregation.newAggregation(syslookup, dictRecordlookup, match, Aggregation.sort(Sort.Direction.DESC,Dict.RECORD_ID));
        } else {
            aggregation = Aggregation.newAggregation(
                    syslookup, dictRecordlookup, match, Aggregation.sort(Sort.Direction.DESC,Dict.RECORD_ID), Aggregation.skip(page > 1 ? (page - 1) * per_page : 0), Aggregation.limit(per_page));
        }
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, "dictRecord", Map.class);
        List<Map> mapList = new ArrayList<>();
        docs.forEach(doc -> {
            doc.put("sysInfo", ((List) doc.get("sysInfo")).get(0));
            doc.put("dataDict", ((List) doc.get("dataDict")).get(0));
            mapList.add(doc);   
        });
        result.put("dbs", mapList);
        return result;
    }

    @Override
    public List<DictRecord> query(DictRecord dictRecord) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = dictRecord ==null?"{}":mapper.writeValueAsString(dictRecord);
        Query query =new BasicQuery(json);
        return mongoTemplate.find(query, DictRecord.class);
    }

    @Override
    public void update(DictRecord dictRecord) {
        Query query = Query.query(Criteria.where(Dict.RECORD_ID).is(dictRecord.getRecord_id()));
        Update update = Update.update(Dict.IS_LIST_FIELD, dictRecord.getIs_list_field()).set(Dict.LIST_FIELD_DESC, dictRecord.getList_field_desc())
                .set(Dict.REMARK, dictRecord.getRemark()).set(Dict.LAST_MODI_TIME, dictRecord.getLast_modi_time())
                .set(Dict.LAST_MODI_USERNAMEEN, dictRecord.getLast_modi_userNameEn())
                .set(Dict.LAST_MODI_USERNAME, dictRecord.getLast_modi_userName());
        mongoTemplate.findAndModify(query, update, DictRecord.class);
    }
}
