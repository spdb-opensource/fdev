package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdevapp.spdb.dao.IServiceSystemDao;
import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ServiceSystemDaoImpl implements IServiceSystemDao {

    @Resource
    private MongoTemplate mongoTemplate;


    @Override
    public ServiceSystem add(ServiceSystem serviceSystem) {
        ObjectId id= new ObjectId();
        serviceSystem.set_id(id);
        serviceSystem.setId(id.toString());
        return mongoTemplate.save(serviceSystem);
    }

    @Override
    public List<ServiceSystem> query(ServiceSystem serviceSystem)throws Exception{
        List<ServiceSystem> list=new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String s = serviceSystem==null?"{}":objectMapper.writeValueAsString(serviceSystem);
        JSONObject jsonObject = JSONObject.fromObject(s);
        Iterator iterator = jsonObject.keySet().iterator();
        Criteria c=new Criteria();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = (String) jsonObject.get(key);
            c.and(key).is(value);
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<ServiceSystem> serviceSystem1 = mongoTemplate.aggregate(Aggregation.newAggregation(match), "service-system", ServiceSystem.class);
        serviceSystem1.forEach(doc ->{list.add(doc);});
        return list;
    }

}
