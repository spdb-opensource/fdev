package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.dao.ITestOrderFileDao;
import com.spdb.fdev.fdemand.spdb.entity.TestOrderFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class TestOrderFileDaoImpl implements ITestOrderFileDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<TestOrderFile> queryByIds(Collection<String> ids) throws Exception {
        return mongoTemplate.find(new Query(Criteria.where("id").in(ids)), TestOrderFile.class);
    }

    @Override
    public List<TestOrderFile> queryByTestOrderId(String id) throws Exception {
        return mongoTemplate.find(new Query(Criteria.where("test_order_id").is(id)), TestOrderFile.class);
    }

    @Override
    public void insert(TestOrderFile testOrderFile) throws Exception {
        ObjectId objectId = new ObjectId();
        testOrderFile.set_id(objectId);
        testOrderFile.setId(objectId.toString());
        mongoTemplate.insert(testOrderFile);
    }

    @Override
    public void insert(List<TestOrderFile> testOrderFiles) throws Exception {
        for (TestOrderFile testOrderFile : testOrderFiles) {
            ObjectId objectId = new ObjectId();
            testOrderFile.set_id(objectId);
            testOrderFile.setId(objectId.toString());
        }
        mongoTemplate.insert(testOrderFiles,TestOrderFile.class);
    }

    @Override
    public void delete(Collection<String> ids) throws Exception {
        mongoTemplate.remove(new Query(Criteria.where(Dict.ID).in(ids)), TestOrderFile.class);
    }

}
