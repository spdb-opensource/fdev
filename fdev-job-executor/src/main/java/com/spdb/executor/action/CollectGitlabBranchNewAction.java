package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.spdb.executor.service.CollectGitlabBranchNewService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by xxx on 下午14:44.
 */
public class CollectGitlabBranchNewAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(CollectGitlabBranchNewAction.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private CollectGitlabBranchNewService collectGitlabBranchNewService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void execute(Context context) {
        logger.info("execute CollectGitlabBranchNew begin");
        try {
            collectGitlabBranchNewService.executeBranch();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("execute CollectGitlabBranchNew end");
    }
}
