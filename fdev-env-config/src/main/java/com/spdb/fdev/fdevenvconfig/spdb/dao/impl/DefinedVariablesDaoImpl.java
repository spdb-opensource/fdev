package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IDefinedVariablesDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.DefinedVariables;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Repository
public class DefinedVariablesDaoImpl implements IDefinedVariablesDao {
    @Resource
    private MongoTemplate mongoTemplate;
    @Override
    public DefinedVariables save(DefinedVariables definedVariables){
        List<DefinedVariables> variablesList = new ArrayList<>();
        variablesList.add(definedVariables);
        return this.mongoTemplate.save(definedVariables);
    }

    @Override
    public DefinedVariables queryDefinedVariables(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.OBJECTID).is(new ObjectId(id)));
        List<DefinedVariables> list = mongoTemplate.find(query,DefinedVariables.class);
        return (list.get(0));
    }
}
