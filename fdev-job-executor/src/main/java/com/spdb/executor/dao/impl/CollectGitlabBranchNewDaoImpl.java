package com.spdb.executor.dao.impl;

import com.csii.pe.pojo.*;
import com.spdb.executor.dao.CollectGitlabBranchNewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author xxx
 * @date 2021/3/31 17:14
 */
@Repository
public class CollectGitlabBranchNewDaoImpl implements CollectGitlabBranchNewDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void saveReleaseBranch(NewReleaseBranch releaseBranch) throws Exception {
        mongoTemplate.save(releaseBranch);
    }

    @Override
    public void saveTagBranch(NewProductTag productTag) throws Exception {
        mongoTemplate.save(productTag);
    }

    @Override
    public void saveFeatureBranch(NewFeatureBranch featureBranch) throws Exception {
        mongoTemplate.save(featureBranch);
    }
}
