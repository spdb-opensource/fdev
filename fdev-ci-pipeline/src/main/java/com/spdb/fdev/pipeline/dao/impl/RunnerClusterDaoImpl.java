package com.spdb.fdev.pipeline.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.IRunnerClusterDao;
import com.spdb.fdev.pipeline.entity.Author;
import com.spdb.fdev.pipeline.entity.RunnerCluster;
import com.spdb.fdev.pipeline.service.IUserService;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class RunnerClusterDaoImpl implements IRunnerClusterDao {

    @Autowired
    private IUserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${pipelineTemplate.white.list:}")
    private List<String> whiteList;

    @Value("${group.role.admin.id}")
    private String groupRoleAdminId;
    /**
     * 查询所有正在使用的runner集群
     *
     * @return
     */
    @Override
    public List<RunnerCluster> queryAllRunnerCluster() {
        Criteria c = new Criteria();
        c.and(Dict.STATUS).is(Constants.ONE);
        Query query = new Query(c);
        return this.mongoTemplate.find(query, RunnerCluster.class, "runnerCluster");
    }

    /**
     * 插入runner集群表
     *
     * @param entityMap
     * @return id      返回runner集群的id
     * @throws Exception
     */
    @Override
    public String insertRunnerCluster(Map entityMap) throws Exception {
        RunnerCluster runnerCluster = CommonUtils.map2Object(entityMap, RunnerCluster.class);
        String runnerClusterId = new ObjectId().toString();
        runnerCluster.setRunnerClusterId(runnerClusterId);
        runnerCluster.setNameId(runnerClusterId);
        runnerCluster.setCreateTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        runnerCluster.setStatus(Constants.ONE);
        List runnerList = runnerCluster.getRunnerList();
        int size = runnerList.size();
        runnerCluster.setRunnerNum(size);
        runnerCluster.setActive(true);

        User user = userService.getUserFromRedis();
        if (checkAdminRole(user.getUser_name_en())){
            Author author = new Author();
            author.setId(Dict.SYSTEM);
            author.setNameCn(Dict.SYSTEM);
            author.setNameEn(Dict.SYSTEM);
            runnerCluster.setAuthor(author);
        } else if (user.getRole_id().contains(groupRoleAdminId)){
            Author author = new Author();
            author.setId(user.getId());
            author.setNameEn(user.getUser_name_en());
            author.setNameCn(user.getUser_name_cn());
            runnerCluster.setAuthor(author);
            runnerCluster.setGroupId(user.getGroup_id());
        }else {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }

        this.mongoTemplate.insert(runnerCluster, Dict.RUNNERCLUSTER);
        return runnerClusterId;
    }

    /**
     * 根据用户英文名判断用户是否是admin或者白名单用户
     * @param userNameEn
     * @return
     */
    public boolean checkAdminRole(String userNameEn) {
        return (Dict.ADMIN.equals(userNameEn) || whiteList.contains(userNameEn));
    }

    /**
     * 查询runner集群
     * 按用户查询，即可以查询用户的（包括可见不可见，但是不能查询出来）
     *
     * @param param
     * @return
     */
    @Override
    public List<RunnerCluster> queryRunnerClusterByParam(Map param) throws Exception {
        List<RunnerCluster> resRunnerClusters=new ArrayList<>();
        Criteria c = new Criteria();
        c.and(Dict.STATUS).is(Constants.ONE);
        if (!CommonUtils.isNullOrEmpty(param.get(Dict.ACTIVE))) {
            c.and(Dict.ACTIVE).is(Boolean.TRUE);
        }
        Query orgQuery = new Query(c);
        orgQuery.with(new Sort(Sort.Direction.DESC, Dict.CREATETIME));
        List<RunnerCluster> runnerClusters = this.mongoTemplate.find(orgQuery, RunnerCluster.class, Dict.RUNNERCLUSTER);

        if (!CommonUtils.isNullOrEmpty(param.get(Dict.USER_USERNAME))) {
            User user = this.userService.getUserFromRedis();
            //判断当前用户是否是管理员
            String nameEn=(String) param.get(Dict.USER_USERNAME);
            if (checkAdminRole(nameEn) || user.getRole_id().contains(groupRoleAdminId)){
                for (RunnerCluster runnerCluster : runnerClusters) {
                    runnerCluster.setCanEdit(Constants.ONE);
                }
                return runnerClusters;
            }/*else {
                //只能查看当前组以及子组
                //判断当前用户是否是小组管理员
                for (RunnerCluster runnerCluster : runnerClusters) {
                    //如果集群不是系统的，需要校验用户是否可以看到，子组用户可以看到上级组的集群
                    *//*if (!Dict.SYSTEM.equals(runnerCluster.getAuthor().getNameEn())){
                        List<String> runnerGroupIds = userService.getChildGroupIdsByGroupId(runnerCluster.getGroupId());
                        if(CommonUtils.isNullOrEmpty(runnerGroupIds) || !runnerGroupIds.contains(user.getGroup_id())){
                            continue;
                        }
                    }*//*
                    //小组管理员可以编辑本组及子组的
                    List<String> role_id = user.getRole_id();
                    List<String> groupIds = userService.getChildGroupIdsByGroupId(user.getGroup_id());
                    if (role_id.contains(groupRoleAdminId) && groupIds.contains(runnerCluster.getGroupId())){
                        runnerCluster.setCanEdit(Constants.ONE);
                    }else{
                        runnerCluster.setCanEdit(Constants.ZERO);
                    }
                    resRunnerClusters.add(runnerCluster);
                }
            }*/
            return resRunnerClusters;
        }
        if (!CommonUtils.isNullOrEmpty(param.get(Dict.RUNNERID))) {
            Criteria elemMatch = new Criteria();
            elemMatch.and(Dict.RUNNERID).is(param.get(Dict.RUNNERID));
            c.and(Dict.RUNNERLIST).elemMatch(elemMatch);
        }
//        c.and(Dict.ISVISIBLE).is()
//        if (!CommonUtils.isNullOrEmpty(param.get()))
//            c.and()
        Query query = new Query(c);
        return this.mongoTemplate.find(query, RunnerCluster.class, Dict.RUNNERCLUSTER);
    }

    @Override
    public RunnerCluster queryRunnerClusterById(String id) {
        Criteria c = new Criteria();
        c.and(Dict.STATUS).is(Constants.ONE);
        c.and(Dict.RUNNERCLUSTERID).is(id);
        Query query = new Query(c);
        return this.mongoTemplate.findOne(query, RunnerCluster.class, Dict.RUNNERCLUSTER);
    }

    @Override
    public RunnerCluster updateParamsById(Map<String, Object> param) throws JsonProcessingException {
        RunnerCluster params = CommonUtils.map2Object(param, RunnerCluster.class);
        String runnerClusterId = params.getRunnerClusterId();
        params.setRunnerClusterId(null);
        Criteria criteria = new Criteria();
        criteria.and(Dict.RUNNERCLUSTERID).is(runnerClusterId);
        /************************************/
        //该方式只有java 实体和数据库的key一致才可以使用
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(params);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update up = new Update();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.RUNNERCLUSTERID.equals(key) || Dict._ID.equals(key) || Dict.NAMEID.equals(key)) {
                continue;
            }
            up.set(key, value);
        }
        /****************************************/
        Query query = new Query(criteria);
        UpdateResult updateResult = this.mongoTemplate.updateFirst(query, up, Dict.RUNNERCLUSTER);
        RunnerCluster runnerCluster = queryRunnerClusterById(runnerClusterId);
        return runnerCluster;
    }

    @Override
    public  List<RunnerCluster> queryRunnerClusterByRunnerId(String runnerId) {
        //在使用中的集群
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is(Constants.ONE);
        criteria.and(Dict.RUNNERLISTRUNNERID).is(runnerId);
        Query query = new Query(criteria);
        List<RunnerCluster> runnerClusters = mongoTemplate.find(query, RunnerCluster.class, Dict.RUNNERCLUSTER);
        return  runnerClusters;
    }
}
