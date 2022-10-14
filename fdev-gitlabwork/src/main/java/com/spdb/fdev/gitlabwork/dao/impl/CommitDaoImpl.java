package com.spdb.fdev.gitlabwork.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.CommitDao;
import com.spdb.fdev.gitlabwork.entiy.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CommitDaoImpl implements CommitDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Commit> findUnStats() {
        return mongoTemplate.find(new Query().addCriteria(new Criteria(Dict.STATS).is(null)), Commit.class);
    }

    @Override
    public List<Commit> findAll() {
        return mongoTemplate.findAll(Commit.class);
    }

    @Override
    public void updateStats(Commit commit) {
        mongoTemplate.updateMulti(new Query() {{
            addCriteria(new Criteria(Dict.ID).is(commit.getId()));
        }}, new Update() {{
            set(Dict.STATS, commit.getStats());
        }}, Commit.class);
    }

    @Override
    public void upert(Commit commit) {
        mongoTemplate.upsert(new Query() {{
            addCriteria(new Criteria(Dict.ID).is(commit.getId()));
        }}, new Update() {{
            Field[] fields = Commit.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    set(field.getName(), field.get(commit));
                } catch (IllegalAccessException e) {
                    //这里不会报错的
                }
            }
        }}, Commit.class);
    }

    @Override
    public void upert(Collection<Commit> record) {
        log.info("------------------------开始：批量插入mongodb------------------------");
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Commit.class);
        List<Pair<Query, Update>> pairs = new ArrayList<>();
        for (Commit commit : record) {
            pairs.add(Pair.of(new Query() {{
                addCriteria(new Criteria(Dict.ID).is(commit.getId()));
            }}, new Update() {{
                Field[] fields = Commit.class.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        set(field.getName(), field.get(commit));
                    } catch (IllegalAccessException e) {
                        //这里不会报错的
                    }
                }
            }}));
            if (10000 == pairs.size()) {//每10000条进行一次upsert
                bulkOperations.upsert(pairs);
                bulkOperations.execute();
                pairs.clear();
            }
        }
        if (!pairs.isEmpty()) {
            bulkOperations.upsert(pairs);
            bulkOperations.execute();
        }
        log.info("------------------------结束：批量插入mongodb------------------------");
    }

    @Override
    public void upertCommittedTitle(Collection<Commit> record) {
        log.info("------------------------开始：批量插入mongodb------------------------");
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Commit.class);
        List<Pair<Query, Update>> pairs = new ArrayList<>();
        for (Commit commit : record) {
            pairs.add(Pair.of(new Query() {{
                addCriteria(new Criteria(Dict.ID).is(commit.getId()));
            }}, new Update() {{
                set("title", commit.getTitle());
            }}));
            if (10000 == pairs.size()) {//每10000条进行一次upsert
                bulkOperations.upsert(pairs);
                bulkOperations.execute();
                pairs.clear();
                System.gc();
            }
        }
        if (!pairs.isEmpty()) {
            bulkOperations.upsert(pairs);
            bulkOperations.execute();
        }
        log.info("------------------------结束：批量插入mongodb------------------------");
    }

}
