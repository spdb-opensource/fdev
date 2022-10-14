package com.spdb.fdev.component.strategy.webhook.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.service.IBaseImageIssueService;
import com.spdb.fdev.component.service.IMpassArchetypeIssueService;
import com.spdb.fdev.component.strategy.webhook.WebhookEventStrategy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("merge_mpass_archetype")
@RefreshScope
public class MpassArchetypeMergeEventStrategyImpl implements WebhookEventStrategy {

    @Autowired
    private IMpassArchetypeIssueService iMpassArchetypeIssueService;

    private static final Logger logger = LoggerFactory.getLogger(MpassArchetypeMergeEventStrategyImpl.class);

    @Override
    public void eventExecutor(Map<String, Object> parse) throws Exception {
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        String state = (String) attributes.get(Dict.STATE);
        Integer iid = (Integer) attributes.get(Dict.IID);
        Integer projectId = (Integer) attributes.get(Dict.SOURCE_PROJECT_ID);
        String sourceBranch = (String) attributes.get(Dict.SOURCE_BRANCH);
        String targetBranch = (String) attributes.get(Dict.TARGET_BRANCH);
        logger.info("当前的项目的ID是{},sourceBranch: {}, targetBranch: {}, iid: {}", projectId, sourceBranch, targetBranch, iid);

        if (Dict.MERGED.equals(state)
                && StringUtils.isNotBlank(sourceBranch)
                && StringUtils.isNotBlank(targetBranch) && sourceBranch.contains("release-") && Dict.MASTER.equals(targetBranch)) {
            iMpassArchetypeIssueService.mergedCallBack(state, iid, projectId);
            logger.info("@@@@@@@@@ 发送merge callback 成功");
        }

        logger.info("合并回调执行成功");
    }
}


