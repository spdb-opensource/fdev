package com.spdb.executor.service.impl;

import com.csii.pe.pojo.*;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.dict.ErrorConstants;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.action.CollectGitlabBranchNewAction;
import com.spdb.executor.dao.CollectGitlabBranchNewDao;
import com.spdb.executor.service.CollectGitlabBranchNewService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName CollectGitlabBranchNewServiceImpl
 * @DESCRIPTION
 * @Author xxx
 * @Date 2021/3/31 17:10
 * @Version 1.0
 */
@Service
public class CollectGitlabBranchNewServiceImpl implements CollectGitlabBranchNewService {
    private static Logger logger = LoggerFactory.getLogger(CollectGitlabBranchNewAction.class);

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private CollectGitlabBranchNewDao gitlabBranchNewDao;


    @Override
    public void executeBranch() throws Exception {
        //1.查询三天前的变更列表,拿到所有版本id
        List<Map<String, Object>> publishList = getPublishList();
        if (CommonUtils.isNullOrEmpty(publishList) || publishList.size() <= 0) {
            return;
        }
        List<String> versionIds = new ArrayList<>();
        for (Map publish : publishList) {
            if (!CommonUtils.isNullOrEmpty(publish.get(Dict.VERSIONIDS))) {
                List<String> ids = (List<String>) publish.get(Dict.VERSIONIDS);
                versionIds.addAll(ids);
            }
        }
        //去重
        versionIds.stream().distinct().collect(Collectors.toList());

        //2.版本id列表查询版本列表，获取release、tag;存库release_branch、product_tag、
        if (!CommonUtils.isNullOrEmpty(versionIds) && versionIds.size() > 0) {
            // 逻辑修改：替换交易为fservice/api/version/queryVersionBasicInfoByIds
            List<Map<String, Object>> versionList = getversionList(versionIds);
            if (!CommonUtils.isNullOrEmpty(versionList) && versionList.size() > 0) {
                for (Map version : versionList) {
                    String application_id = (String) version.get(Dict.SERVICEID);
                    String release_branch = (String) version.get(Dict.TARGETBRANCH);
                    // 获取应用详情
                    Map<String, Object> application = getApplicationEn(application_id);
                    Integer gitlab_project_id = (Integer) application.get(Dict.GITLABID);
                    //rel分支存库
                    NewReleaseBranch releaseBranch = new NewReleaseBranch(application_id, release_branch, gitlab_project_id, 0);
                    //存库
                    try {
                        gitlabBranchNewDao.saveReleaseBranch(releaseBranch);
                    } catch (Exception e) {
                        printErrorMessage(e, releaseBranch);
                    }
                    List<String> tagList = (List<String>) version.get(Dict.TAGS);
                    //tag分支存库
                    saveProductTag(tagList, gitlab_project_id);
                }
            }
        }

        //3.版本id列表查询任务列表，获取feature；存库feature_branch
        if (!CommonUtils.isNullOrEmpty(versionIds) && versionIds.size() > 0) {
            List<Map<String, Object>> tasksResult = getTaskList(versionIds);
            if (!CommonUtils.isNullOrEmpty(tasksResult) && tasksResult.size() > 0) {
                for (Map task : tasksResult) {
                    List<Map<String, String>> mountTaskList = (List<Map<String, String>>) ((Map<String, Object>) task.get("taskList")).get(Dict.MOUNTTASKLIST);
                    if (!CommonUtils.isNullOrEmpty(mountTaskList) && mountTaskList.size() > 0) {
                        for (Map mountTask : mountTaskList) {
                            String feature_branch = (String) mountTask.get(Dict.BRANCHNAME);
                            String application_id = (String) mountTask.get(Dict.RELATEDAPPLICATION);
                            String task_id = (String) mountTask.get(Dict.ID);
                            Map<String, Object> application = getApplicationEn(application_id);
                            Integer gitlab_project_id = (Integer) application.get(Dict.GITLABID);
                            NewFeatureBranch featureBranch = new NewFeatureBranch(application_id, task_id, feature_branch, gitlab_project_id, 0);
                            try {
                                gitlabBranchNewDao.saveFeatureBranch(featureBranch);
                            } catch (Exception e) {
                                printErrorMessage(e, featureBranch);
                            }

                        }
                    }
                }
            }
        }

    }

    public List<Map<String, Object>> getPublishList() {
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "queryThreeDaysAgoPublishList");
        List<Map<String, Object>> publishList = null;
        try {
            publishList = (List<Map<String, Object>>) restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("查询满足条件的变更列表出错！", e);
            return null;
        }
        return publishList;
    }

    public void saveProductTag(List<String> product_tag, Integer gitlab_project_id) {
        Optional<List<String>> optionalTag = Optional.ofNullable(product_tag);
        optionalTag.ifPresent(
                tagList -> tagList.forEach(
                        tag ->
                        {
                            NewProductTag productTag = new NewProductTag(gitlab_project_id, tag, 0);
                            try {
                                gitlabBranchNewDao.saveTagBranch(productTag);
                            } catch (Exception e) {
                                printErrorMessage(e, productTag);
                            }
                        }
                )
        );
    }

    public Map<String, Object> getApplicationEn(String relatedApplicationId) {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put(Dict.ID, relatedApplicationId);
        requestMap.put(Dict.REST_CODE, "queryApp");
        Map<String, Object> application = new HashMap<>();
        try {
            application = (Map<String, Object>) this.restTransport.submit(requestMap);
        } catch (Exception e) {
            logger.error("queryApp error@@@" + e.getMessage());
            throw new FdevException("调用应用模块queryApp交易失败!");
        }
        return application;
    }


    public List<Map<String, Object>> getversionList(List<String> versionIds) {
        //调用应用模块：根据版本id列表查询版本集合
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put(Dict.IDS, versionIds);
        requestMap.put(Dict.REST_CODE, "queryVersionBasicInfoByIds");
        List<Map<String, Object>> versionList = new ArrayList<Map<String, Object>>();
        try {
            versionList = (List<Map<String, Object>>) this.restTransport.submit(requestMap);
        } catch (Exception e) {
            logger.error("queryVersionBasicInfoByIds error@@@" + e.getMessage());
            throw new FdevException(ErrorConstants.QUERYVERSIONBYIDS_ERROR);
        }
        return versionList;
    }

    public List<Map<String, Object>> getTaskList(List<String> versionIds) {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put(Dict.VERSIONIDS, versionIds);
        requestMap.put(Dict.MOUNTSTATUS, 1);//挂载状态 0-意向挂载 1-已挂载
        requestMap.put(Dict.DELETE, 0);//0-未废弃 1-已废弃
        requestMap.put(Dict.REST_CODE, "queryTaskByVersionIds");
        List<Map<String, Object>> tasksResult = new ArrayList<Map<String, Object>>();
        try {
            tasksResult = (List<Map<String, Object>>) this.restTransport.submit(requestMap);

        } catch (Exception e) {
            logger.error("queryTaskByVersionIds error@@@" + e.getMessage());
            throw new FdevException(ErrorConstants.QUERYTASKBYVERSIONIDS_ERROR);
        }
        return tasksResult;
    }

    private void printErrorMessage(Exception e, Object object) {
        if (!e.getMessage().contains("E11000")) {
            logger.error("{}数据保存失败!" + e.getMessage(), object.toString());
        }
    }
}
