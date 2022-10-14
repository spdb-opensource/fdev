package com.spdb.fdev.pipeline.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.IRunnerClusterDao;
import com.spdb.fdev.pipeline.entity.Author;
import com.spdb.fdev.pipeline.entity.RunnerCluster;
import com.spdb.fdev.pipeline.service.IRunnerClusterService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RunnerClusterServiceImpl implements IRunnerClusterService {

    private static Logger logger = LoggerFactory.getLogger(RunnerClusterServiceImpl.class);

    @Autowired
    private IRunnerClusterDao runnerClusterDao;

    @Autowired
    private IUserService userService;

    @Value("${pipelineTemplate.white.list:}")
    private List<String> whiteList;

    @Value("${group.role.admin.id}")
    private String groupRoleAdminId;

    @Override
    public List<RunnerCluster> getAllRunnerCluster() {
        return runnerClusterDao.queryAllRunnerCluster();
    }

    @Override
    public String addRunnerCluster(Map entityMap) throws Exception {
        //判断当前用户是否是是否是小组管理员或者是白名单成员
        User user = userService.getUserFromRedis();
        String group_id = user.getGroup_id();
        List<String> role_id = user.getRole_id();
        String user_name_en = user.getUser_name_en();
        if (!role_id.contains(groupRoleAdminId) && !whiteList.contains(user_name_en) && !Dict.ADMIN.equals(user_name_en)){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"当前用户没有权限"});
        }
        if (CommonUtils.isNullOrEmpty(entityMap)) {
            logger.error("params error entityMap: " + JSONObject.toJSONString(entityMap));
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"参数异常"});
        }
        else {
            logger.info("params entityMap: " + JSONObject.toJSONString(entityMap));
            //判断集群的运行环境是否和runner一至
            String platform =(String) entityMap.get(Dict.PLATFORM);
            List<Map<String, Object>> runnerList=(List<Map<String, Object>>) entityMap.get(Dict.RUNNERLIST);
            if (!CommonUtils.isNullOrEmpty(runnerList)){
                for (Map<String, Object> map : runnerList) {
                    String platForm1 =(String) map.get(Dict.PLATFORM);
                    if (!platForm1.equals(platform)){
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"runner平台和集群平台不同"});
                    }
                }
            }
            return this.runnerClusterDao.insertRunnerCluster(entityMap);
        }
    }

    @Override
    public List<RunnerCluster> getRunnerClusterByParam(Map param) throws Exception {
        User user = this.userService.getUserFromRedis();
        String nameEn = user.getUser_name_en();
        param.put(Dict.USER_USERNAME, nameEn);
        return runnerClusterDao.queryRunnerClusterByParam(param);
    }


    @Override
    public RunnerCluster getRunnerClusterById(String runnerClusterId) {
        return this.runnerClusterDao.queryRunnerClusterById(runnerClusterId);
    }

    @Override
    public RunnerCluster getRunnerClusterByRunner(String runnerId) throws Exception {
        Map param = new HashMap();
        param.put(Dict.RUNNERID, runnerId);
        List<RunnerCluster> runnerClusters = this.runnerClusterDao.queryRunnerClusterByParam(param);
        if (CommonUtils.isNullOrEmpty(runnerClusters)) {
            return null;
        }else if (runnerClusters.size() == 1) {
            return runnerClusters.get(0);
        }
        return null;
    }

    @Override
    public RunnerCluster updateRunnerCluster(Map<String, Object> param) throws Exception {
        String runnerClusterId = (String) param.get(Dict.RUNNERCLUSTERID);
        if (CommonUtils.isNullOrEmpty(runnerClusterId)) {
            logger.error(" update runnerClusterId is illegal");
            throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{runnerClusterId});
        }
        RunnerCluster runnerCluster = runnerClusterDao.queryRunnerClusterById(runnerClusterId);
        //判断当前用户是否有修改权限
        User currentUser = userService.getUserFromRedis();
        List<String> userRoleIds = currentUser.getRole_id();
        String userNameEn = currentUser.getUser_name_en();
        String nameEn = runnerCluster.getAuthor().getNameEn();
        Map author =new HashMap();
        if (whiteList.contains(userNameEn) || Dict.ADMIN.equals(userNameEn)){
            author.put(Dict.ID, Dict.SYSTEM);
            author.put(Dict.NAMEEN, Dict.SYSTEM);
            author.put(Dict.NAMECN, Dict.SYSTEM);
            param.put(Dict.AUTHOR, author);
        }else {
            String group_id = currentUser.getGroup_id();
//            List<String> groupIds = userService.getChildGroupIdsByGroupId(group_id);
//            if (groupIds.contains(runnerCluster.getGroupId()) && userRoleIds.contains(groupRoleAdminId)){
            if (userRoleIds.contains(groupRoleAdminId)){
                author.put(Dict.ID, currentUser.getId());
                author.put(Dict.NAMEEN, userNameEn);
                author.put(Dict.NAMECN, currentUser.getUser_name_cn());
                param.put(Dict.AUTHOR, author);
            }else if (nameEn.equals(userNameEn)){
                author.put(Dict.ID, currentUser.getId());
                author.put(Dict.NAMEEN, userNameEn);
                author.put(Dict.NAMECN, currentUser.getUser_name_cn());
                param.put(Dict.AUTHOR, author);
            }else {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"无修改权限"});
            }
        }
        param.put(Dict.UPDATETIME, CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        return this.runnerClusterDao.updateParamsById(param);
    }

}
