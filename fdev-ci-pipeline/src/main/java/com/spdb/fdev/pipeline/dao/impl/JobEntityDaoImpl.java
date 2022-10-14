package com.spdb.fdev.pipeline.dao.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.ICategoryDao;
import com.spdb.fdev.pipeline.dao.IJobEntityDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.IPipelineService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JobEntityDaoImpl implements IJobEntityDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IUserService userService;

    @Autowired
    private ICategoryDao categoryDao;

    @Value("${group.role.admin.id}")
    private String groupRoleAdminId;

    @Value("${pipelineTemplate.white.list:}")
    private List<String> whiteList;

    @Autowired
    IPipelineService pipelineService;

    private static final Logger logger = LoggerFactory.getLogger(JobEntityDaoImpl.class);

    @Override
    public List<Map> queryAllJobs(long skip, int limit, User user, String searchContent) throws Exception {
        List<Map> list = new ArrayList<>();
        //1 查出所有的分类
        Map param = new HashMap();
        param.put(Dict.CATEGORYLEVEL, Dict.JOBENTITY);
        List<Category> allCategory = categoryDao.getCategoryByParam(param);
        for (Category category : allCategory) {
            Map map = new HashMap();
            map.put(Dict.CATEGORY, category);
            //查询该分类下满足条件的记录
            Query query = new Query();
            Criteria statusCriteria = Criteria.where(Dict.STATUS).is(Constants.STATUS_OPEN);
            Criteria categoryCriteria = Criteria.where(Dict.CATEGORY_CATEGORYID).is(category.getCategoryId());
            query.addCriteria(statusCriteria);
            query.addCriteria(categoryCriteria);
            Criteria descOrName = new Criteria();
            if (!CommonUtils.isNullOrEmpty(searchContent)) {
                searchContent = ".*?" + searchContent + ".*";
                Criteria nameCriteria = Criteria.where(Dict.JOBNAME).regex(searchContent);
                Criteria descCriteria = Criteria.where(Dict.JOBDESC).regex(searchContent);
                descOrName.orOperator(nameCriteria, descCriteria);
            }
            Criteria andCriteria = new Criteria();
            Criteria threeOrCriteria = new Criteria();
            //非管理员用户，对于private 只能看到自己的
            if (!checkAdminRole(user.getUser_name_en())) {
                Criteria publicCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.PUBLIC);
                Criteria userCriteria = Criteria.where(Dict.AUTHOR__ID).is(user.getId());
                Criteria groupsCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.GROUP);
                threeOrCriteria.orOperator(publicCriteria, userCriteria, groupsCriteria);
            }
            if (!CommonUtils.isNullOrEmpty(searchContent)) {
                andCriteria.andOperator(descOrName, threeOrCriteria);
            } else {
                andCriteria.andOperator(threeOrCriteria);
            }
            query.addCriteria(andCriteria);
            query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
            List<JobEntity> entityList = mongoTemplate.find(query, JobEntity.class);
            if (!CommonUtils.isNullOrEmpty(entityList)) {
                List<Map> arrayList = new ArrayList<>();//模板单元中的数据
                for (JobEntity entity : entityList) {
                    //如果不是管理员，对于group的，只能看到用户在group子组的。 根据模板所在组id查询其所有子组,看用户在不在模板的子组内
//                    if (entity.getVisibleRange().equals(Dict.GROUP) && !checkAdminRole(user.getUser_name_en())){
//                        List<String> templateGroupIds = userService.getChildGroupIdsByGroupId(entity.getGroupId());
//                        if(CommonUtils.isNullOrEmpty(templateGroupIds) || !templateGroupIds.contains(user.getGroup_id())){
//                            continue;
//                        }
//                    }
                    Map tempMap = this.prepareJobEntityMap(entity);
                    arrayList.add(tempMap);
                }
                map.put(Dict.JOBLIST, arrayList);
                map.put(Dict.TOTAL, arrayList.size());
            }else {
                map.put(Dict.JOBLIST, new ArrayList<>());
                map.put(Dict.TOTAL, 0);
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 根据用户英文名判断用户是否是admin或者白名单用户
     * @param userNameEn
     * @return
     */
    public boolean checkAdminRole(String userNameEn) {
        return (Dict.ADMIN.equals(userNameEn) || whiteList.contains(userNameEn));
    }

    @Override
    public JobEntity queryFullJobsById(String id) throws Exception {
        Query query = Query.query(
                Criteria.where(Dict.STATUS).is(Constants.STATUS_OPEN)
                        .and(Dict.JOBID).is(id)
        );
        return mongoTemplate.findOne(query, JobEntity.class);
    }

    @Override
    public String add(JobEntity jobEntity) throws Exception {
        String id = new ObjectId().toString();
        jobEntity.setJobId(id);
        if (CommonUtils.isNullOrEmpty(jobEntity.getNameId())) {
            jobEntity.setNameId(id);
        }
        jobEntity.setStatus(Constants.STATUS_OPEN);
        jobEntity.setCreateTime(TimeUtils.getDate(TimeUtils.FORMAT_DATE_TIME));
        jobEntity.setUpdateTime(TimeUtils.getDate(TimeUtils.FORMAT_DATE_TIME));
        if (Dict.ADMIN.equals(userService.getAuthor().getNameEn()) || whiteList.contains(userService.getAuthor().getNameEn())) {
            Author adminAuthor = new Author();
            adminAuthor.setId(Dict.SYSTEM);
            adminAuthor.setNameCn(Dict.SYSTEM);
            adminAuthor.setNameEn(Dict.SYSTEM);
            jobEntity.setAuthor(adminAuthor);
            if(CommonUtils.isNullOrEmpty(jobEntity.getVisibleRange())){
                jobEntity.setVisibleRange(Dict.PUBLIC);
            }
        } else if (userService.getUserFromRedis().getRole_id().contains(groupRoleAdminId)){
            jobEntity.setAuthor(userService.getAuthor());
            jobEntity.setGroupId(userService.getUserFromRedis().getGroup_id());
            if(CommonUtils.isNullOrEmpty(jobEntity.getVisibleRange())){
                jobEntity.setVisibleRange(Dict.PUBLIC);
            }
        } else {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        if (CommonUtils.isNullOrEmpty(jobEntity.getVersion())) {
            jobEntity.setVersion(Constants.UP_CHANGE_VERSION);
        }
        return mongoTemplate.save(jobEntity).getJobId();
    }

    @Override
    public JobEntity findOldVersion(String nameId) throws Exception {
        Query query = Query.query(
                Criteria.where(Dict.STATUS).is(Constants.STATUS_OPEN)
                        .and(Dict.NAMEID).is(nameId)
        );
        return mongoTemplate.findOne(query, JobEntity.class);
    }

    @Override
    public void setStatusClose(String id) throws Exception {
        Query query = Query.query(Criteria.where(Dict.JOBID).is(id));
        Update update = Update.update(Dict.STATUS, Constants.STATUS_CLOSE);
        mongoTemplate.findAndModify(query, update, JobEntity.class);
    }

    @Override
    public JobEntity updateVisibleRange(String id, JobEntity jobEntity) throws Exception {
        Query query = Query.query(Criteria.where(Dict.JOBID).is(id));
        Update update = Update.update(Dict.VISIBLERANGE, jobEntity.getVisibleRange());
        mongoTemplate.findAndModify(query, update, JobEntity.class);
        JobEntity resultJobEntity = this.queryFullJobsById(id);
        return resultJobEntity;
    }

    @Override
    public List<JobEntity> findAllByPluginCode(List<String> pluginCodeList) throws Exception {
        Query query = Query.query(Criteria.where(Dict.STEPSPLUGININFOPLUGINCODE).in(pluginCodeList));
        return mongoTemplate.find(query,JobEntity.class);
    }

    @Override
    public void updateStepsInJobEntity(JobEntity jobEntity) throws Exception {
        Query query = Query.query(Criteria.where(Dict.JOBID).is(jobEntity.getJobId()));
        Update update = Update.update(Dict.STEPS,jobEntity.getSteps());
        mongoTemplate.findAndModify(query,update,JobEntity.class);
    }

    private Map prepareJobEntityMap(JobEntity jobEntity) throws Exception {
        Map jobEntityMap = new HashMap<>();
        jobEntityMap.put(Dict.JOBID, jobEntity.getJobId());
        jobEntityMap.put(Dict.NAMEID, jobEntity.getNameId());
        jobEntityMap.put(Dict.JOBNAME, jobEntity.getName());
        jobEntityMap.put(Dict.JOBDESC, jobEntity.getDesc());
        jobEntityMap.put(Dict.STATUS, jobEntity.getStatus());
        jobEntityMap.put(Dict.AUTHOR, jobEntity.getAuthor());
        jobEntityMap.put(Dict.CATEGORY, jobEntity.getCategory());
        jobEntityMap.put(Dict.STEPS,jobEntity.getSteps());
        jobEntityMap.put(Dict.VERSION, jobEntity.getVersion());
        jobEntityMap.put(Dict.CREATETIME, jobEntity.getCreateTime());
        jobEntityMap.put(Dict.UPDATETIME, jobEntity.getUpdateTime());
        jobEntityMap.put(Dict.VISIBLERANGE, jobEntity.getVisibleRange());
        jobEntityMap.put(Dict.GROUPID, jobEntity.getGroupId());
        //小组管理员可以编辑本组模版、管理员可以编辑所有模版
        jobEntityMap.put(Dict.CANEDIT, Constants.ZERO);   //不可编辑
        if(pipelineService.checkGroupidInUserGroup(jobEntity.getGroupId())){
            jobEntityMap.put(Dict.CANEDIT, Constants.ONE);   //可以编辑
        }
        //空模版不允许编辑
        if (CommonUtils.isNullOrEmpty(jobEntity.getSteps()) || jobEntity.getSteps().size() == 0){
            jobEntityMap.put(Dict.CANEDIT, Constants.ZERO);   //不可编辑
        }
        return jobEntityMap;
    }

}
