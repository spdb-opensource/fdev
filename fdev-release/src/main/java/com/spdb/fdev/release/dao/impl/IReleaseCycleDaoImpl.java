package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IReleaseCycleDao;
import com.spdb.fdev.release.entity.ReleaseCycle;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class IReleaseCycleDaoImpl implements IReleaseCycleDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public ReleaseCycle save(ReleaseCycle releaseCycle) {
        try {
            return this.mongoTemplate.save(releaseCycle);
        } catch (DuplicateKeyException e) {
            if (e.getMessage().indexOf(Dict.RELEASE_NODE_NAME) >= 0) {
                throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "`投产大窗口名不能重复!" });
            }
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR);
        }
    }

    @Override
    public ReleaseCycle deleteByReleaseNodeName(String releaseNodeName) {
        Criteria criteria = Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNodeName);
        Query query = new Query(criteria);
        return this.mongoTemplate.findAndRemove(query, ReleaseCycle.class);
    }

    @Override
    public ReleaseCycle queryByReleaseNodeName(String releaseNodeName) {
        Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseNodeName));
        return this.mongoTemplate.findOne(query, ReleaseCycle.class);
    }

    @Override
    public ReleaseCycle update(ReleaseCycle releaseCycle) {
        Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(releaseCycle.getRelease_node_name()));
        Update update = Update.update(Dict.RELEASE_NODE_NAME, releaseCycle.getRelease_node_name()).set(Constants.T_1,
                releaseCycle.getT_1()).set(Constants.T_2,releaseCycle.getT_2())
                .set(Constants.T_3,releaseCycle.getT_3()).set(Constants.T_4,releaseCycle.getT_4())
                .set(Constants.T_5,releaseCycle.getT_5()).set(Constants.T_6,releaseCycle.getT_6())
                .set(Constants.T_7,releaseCycle.getT_7()).set(Constants.T_8, releaseCycle.getT_8())
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseCycle.class);
        return mongoTemplate.findOne(query, ReleaseCycle.class);
    }

    @Override
    public List<ReleaseCycle> queryAll() {
        return mongoTemplate.findAll(ReleaseCycle.class);
    }
}
