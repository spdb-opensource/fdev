package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.pojo.FeatureBranch;
import com.csii.pe.pojo.ProductTag;
import com.csii.pe.pojo.ReleaseBranch;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

/**
 * Created by xxx on 上午10:16.
 */
public class CollectGitlabBranchAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(CollectGitlabBranchAction.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void execute(Context context) {
        logger.info("execute CollectGitlabBranch begin");
        HashMap params = new HashMap();
        params.put(Dict.GROUP, new ArrayList<>());
        ArrayList stages = new ArrayList();
        stages.add(Dict.TASK_STAGE_ABORT);
        params.put(Dict.TASK_STAGE, stages);
        params.put(Dict.REST_CODE, "queryAbortTask");
        Map<String, List> result = null;
        List<Map> abort_tasks = null;
        try {
            result = (Map<String, List>) restTransport.submit(params);
        } catch (Exception e) {
            logger.error("已取消任务对应的feature分支统计出错！", e);
            throw new FdevException("已取消任务对应的feature分支统计出错！");
        }
        if(!CommonUtils.isNullOrEmpty(result)) {
            abort_tasks = result.get("list");
        }
        String isAll = context.getString("isAll");
        for (Map task : abort_tasks) {
            if (!CommonUtils.isNullOrEmpty(isAll) && "all".equals(isAll)) {
                if (CommonUtils.isNullOrEmpty(task.get(Dict.PROJECT_ID)) || CommonUtils.isNullOrEmpty(task.get(Dict.FEATURE_BRANCH)))
                    continue;
            } else {
                if (CommonUtils.isNullOrEmpty(task.get(Dict.PROJECT_ID)) || CommonUtils.isNullOrEmpty(task.get(Dict.FEATURE_BRANCH)) || CommonUtils.isThreeDayAgo((String) task.get(Dict.START_TIME)))
                    continue;
            }
            params = new HashMap();
            params.put(Dict.ID, task.get(Dict.PROJECT_ID));
            params.put(Dict.REST_CODE, "queryAppGitlabId");
            List<Map> apps = null;
            try {
                apps = (List<Map>) restTransport.submit(params);
            } catch (Exception e) {
                logger.error("任务：" + task.get(Dict.ID) + ",对应 project_id : " + task.get(Dict.PROJECT_ID) + "查询 gitlabId 出错!" + e.getMessage());
                continue;
            }
            if (apps.size() > 0) {
                FeatureBranch featureBranch = new FeatureBranch("", (String) task.get(Dict.PROJECT_ID), (String) task.get(Dict.ID), (String) task.get(Dict.FEATURE_BRANCH), (Integer) (apps.get(0).get(Dict.GITLAB_PROJECT_ID)), 0);
                try {
                    mongoTemplate.save(featureBranch);
                } catch (Exception e) {
                    printErrorMessage(e, featureBranch);
                }
            }
        }

        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "queryThreeDaysAgoNodes");
        List<Map> releaseNode = null;
        try {
            releaseNode = (List<Map>) restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("查询满足条件的投产窗口出错！", e);
            return;
        }
        Optional<List<Map>> optionalNode = Optional.ofNullable(releaseNode);
        optionalNode.ifPresent(nodeList -> nodeList.forEach(
                releaseN -> {
                    String release_node_name = (String) releaseN.get(Dict.RELEASE_NODE_NAME);

                    requstParams.put(Dict.RELEASE_NODE_NAME, release_node_name);
                    requstParams.put(Dict.REST_CODE, "queryBatchApplications");
                    List<Map> releaseApp = null;
                    try {
                        releaseApp = (List<Map>) restTransport.submit(requstParams);
                    } catch (Exception e) {
                        logger.error("根据投产窗口查询投产应用出错！" + e.getMessage());
                        return;
                    }
                    Optional<List<Map>> optionalApp = Optional.ofNullable(releaseApp);
                    optionalApp.ifPresent(
                            appList -> appList.forEach(
                                    releaseApplication -> {
                                        saveBranches(releaseApplication);
                                    }));
                }
        ));
        logger.info("execute CollectGitlabBranch end");
    }


    public boolean saveBranches(Map releaseApplication) {
        String application_id = (String) releaseApplication.get(Dict.APPLICATION_ID);
        String release_node_name = (String) releaseApplication.get(Dict.RELEASE_NODE_NAME);
        String release_branch = (String) releaseApplication.get(Dict.RELEASE_BRANCH);
        List<String> product_tag = (List<String>) releaseApplication.get(Dict.PRODUCT_TAG);
        Integer gitlab_project_id = (Integer) releaseApplication.get(Dict.GITLAB_PROJECT_ID);
        saveProductTag(product_tag, gitlab_project_id);
        ReleaseBranch releaseBranch = new ReleaseBranch(release_node_name, application_id, release_branch, gitlab_project_id, 0);
        try {
            mongoTemplate.save(releaseBranch);
        } catch (Exception e) {
            printErrorMessage(e, releaseBranch);
        }
        List<Map> releaseTask = null;
        try {
            releaseTask = queryTasks(release_node_name, application_id);
        } catch (Exception e) {
            logger.error("查询投产窗口下对应任务集合出错！" + e.getMessage());
        }
        if (CommonUtils.isNullOrEmpty(releaseTask)) {
            logger.info("{}下没有投产任务", application_id);
            return false;
        }
        List<Map> allTask = getAllTask(releaseTask);
        for (Map map : allTask) {
            String feature_branch = (String) map.get(Dict.FEATURE_BRANCH);
            String task_id = (String) map.get(Dict.ID);
            FeatureBranch featureBranch = new FeatureBranch(release_node_name, application_id, task_id, feature_branch, gitlab_project_id, 0);
            try {
                mongoTemplate.save(featureBranch);
            } catch (Exception e) {
                printErrorMessage(e, featureBranch);
            }
        }
        return true;
    }

    public void saveProductTag(List<String> product_tag, Integer gitlab_project_id) {
        Optional<List<String>> optionalTag = Optional.ofNullable(product_tag);
        optionalTag.ifPresent(
                tagList -> tagList.forEach(
                        tag ->
                        {
                            ProductTag productTag = new ProductTag(gitlab_project_id, tag, 0);
                            try {
                                mongoTemplate.save(productTag);
                            } catch (Exception e) {
                                printErrorMessage(e, productTag);
                            }
                        }
                )
        );
    }

    public List<Map> queryTasks(String release_node_name, String application_id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.RELEASE_NODE_NAME, release_node_name);
        map.put(Dict.APPLICATION_ID, application_id);
        map.put(Dict.REST_CODE, "queryTasksByExecutor");
        List<Map> releaseTaskMap = (List<Map>) restTransport.submit(map);
        return releaseTaskMap;
    }

    public List<Map> getAllTask(List<Map> releaseTasks) {
        List<String> ids = new ArrayList<>();
        // 遍历任务列表
        for (Map map : releaseTasks) {
            String task_id = (String) map.get("task_id");
            ids.add(task_id);
        }
        Map<String, Object> map = new HashMap<>();
        // 发task模块获取task详细信息
        map.put(Dict.IDS, ids);
        map.put(Dict.REST_CODE, "queryAllTasksByIds");
        List<Map> allTaskMap = Collections.emptyList();
        try {
            allTaskMap = (List<Map>) restTransport.submit(map);
        } catch (Exception e) {
            logger.error("根据任务id集合查询任务列表出错！" + e.getMessage());
        }
        return allTaskMap;
    }

    private void printErrorMessage(Exception e, Object object) {
        if (!e.getMessage().contains("E11000")) {
            logger.error("{}数据保存失败!" + e.getMessage(), object.toString());
        }
    }

}
