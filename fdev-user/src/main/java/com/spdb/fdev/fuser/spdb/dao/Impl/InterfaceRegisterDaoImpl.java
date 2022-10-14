package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IInterfaceRegisterDao;
import com.spdb.fdev.fuser.spdb.entity.user.InterfaceRegister;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author liux81
 * @DATE 2021/12/7
 */
@Component
public class InterfaceRegisterDaoImpl implements IInterfaceRegisterDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void add(InterfaceRegister interfaceRegister) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        interfaceRegister.set_id(objectId);
        interfaceRegister.setId(id);
        mongoTemplate.save(interfaceRegister);
    }

    @Override
    public void update(InterfaceRegister interfaceRegister) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(interfaceRegister.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(interfaceRegister);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update up = new Update();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            up.set(key, value);
        }
        mongoTemplate.findAndModify(query,up,InterfaceRegister.class);
    }

    @Override
    public void delete(String id) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.STATUS,"deleted");
        User user = CommonUtils.getSessionUser();
        update.set("update_user",user.getId());
        update.set("update_time",CommonUtils.formatDate2(new Date(),CommonUtils.STANDARDDATEPATTERN));
        mongoTemplate.findAndModify(query,update,InterfaceRegister.class);
    }

    @Override
    public Map<String, Object> query(Map param) {
        LookupOperation roleLookUp = Aggregation.lookup(Dict.ROLE, "role_ids", Dict.ID, "roles");
        Criteria criteria = new Criteria();
        //接口路径、功能描述模糊搜索
        if(!CommonUtils.isNullOrEmpty(param.get("selectKey"))){
            String selectKey = (String) param.get("selectKey");
            Pattern pattern = Pattern.compile("^.*" + selectKey + ".*$");
            Criteria c1 = Criteria.where("interface_path").regex(pattern);
            Criteria c2 = Criteria.where("function_desc").regex(pattern);
            criteria.orOperator(c1,c2);
        }
        if(!CommonUtils.isNullOrEmpty(param.get("roleIds"))){
            List<String> roleIds = (List<String>) param.get("roleIds");
            criteria.and("role_ids").all(roleIds);
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.STATUS))){
            String status = (String) param.get(Dict.STATUS);
            criteria.and(Dict.STATUS).is(status);
        }
        MatchOperation match = Aggregation.match(criteria);
        Query query = Query.query(criteria);
        long count = mongoTemplate.count(query, InterfaceRegister.class);
        Aggregation aggregation;
        SortOperation sortOperation = Aggregation.sort(new Sort(Sort.Direction.DESC, "create_time"));
        if(!CommonUtils.isNullOrEmpty(param.get("currentPage")) && (int)param.get("currentPage") >= 0
                && !CommonUtils.isNullOrEmpty(param.get("pageSize")) && (int)param.get("pageSize") >= 0){
            int currentPage = (int) param.get("currentPage");
            int pageSize = (int) param.get("pageSize");
            int index = (currentPage - 1) * pageSize;
            SkipOperation skip = Aggregation.skip((long) index);
            LimitOperation limit = Aggregation.limit((long) pageSize);
            aggregation = Aggregation.newAggregation(roleLookUp,sortOperation, match, skip, limit);
        }else {
            aggregation = Aggregation.newAggregation(roleLookUp,sortOperation, match);
        }

        AggregationResults<InterfaceRegister> docs = mongoTemplate.aggregate(aggregation,"interface_register", InterfaceRegister.class);
        List<InterfaceRegister> records = new ArrayList<>();
        docs.forEach(doc -> {
            records.add(doc);
        });
        Map<String, Object> result = new HashMap<>();
        result.put("count",count);
        result.put("records",records);
        return result;
    }

    @Override
    public InterfaceRegister queryByInterface(String interfacePath) {
        Query query = Query.query(Criteria.where("interface_path").is(interfacePath).and(Dict.STATUS).is("using"));
        return mongoTemplate.findOne(query,InterfaceRegister.class);
    }

    @Override
    public InterfaceRegister queryById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query,InterfaceRegister.class);
    }
}
