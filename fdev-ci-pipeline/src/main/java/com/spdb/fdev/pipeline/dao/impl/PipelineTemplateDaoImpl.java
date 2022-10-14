package com.spdb.fdev.pipeline.dao.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.ICategoryDao;
import com.spdb.fdev.pipeline.dao.IPipelineTemplateDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.IAppService;
import com.spdb.fdev.pipeline.service.IUserService;
import com.spdb.fdev.pipeline.service.impl.PipelineServiceImpl;
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

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PipelineTemplateDaoImpl implements IPipelineTemplateDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ICategoryDao categoryDao;

    @Autowired
    private IUserService userService;

    @Autowired
    private IAppService iAppService;

    @Value("${group.role.admin.id}")
    private String groupRoleAdminId;


    @Value("${pipelineTemplate.white.list:}")
    private List<String> whiteList;

    private static final Logger logger = LoggerFactory.getLogger(PipelineServiceImpl.class);

    /**
     * 根据用户英文名判断用户是否是admin或者白名单用户
     *
     * @param userNameEn
     * @return
     */
    public boolean checkAdminRole(String userNameEn) {
        return (Dict.ADMIN.equals(userNameEn) || whiteList.contains(userNameEn));
    }

    @Override
    public List<PipelineTemplate> queryByPluginCodeList(List<String> pluginCodeList) {
        Query query = Query.query(Criteria.where("stages.jobs.steps.pluginInfo.pluginCode").in(pluginCodeList));
        return mongoTemplate.find(query, PipelineTemplate.class);
    }

    @Override
    public void updateStages(PipelineTemplate pipelineTemplate) {
        Query query = Query.query(Criteria.where(Dict.ID).is(pipelineTemplate.getId()));
        Update update = Update.update(Dict.STAGES, pipelineTemplate.getStages());
        mongoTemplate.findAndModify(query, update, PipelineTemplate.class);
    }


    @Override
    public void delTemplate(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        mongoTemplate.findAndRemove(query, PipelineTemplate.class);
    }

    @Override
    public Map<String, Object> queryMinePipelineTemplateList(long skip, int limit, User user, String searchContent) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int total = 0;
        //查询模板分类
        List categoryList = new ArrayList();  //存放结果分类
        //1 查出所有的分类
        Category queryParam = new Category();
        queryParam.setCategoryLevel(Dict.PIPELINETEMPLATE);
        List<Category> allCategory = this.categoryDao.getCategory(queryParam);

        /*List<String> groupIds = new ArrayList<>();
        if (!Dict.ADMIN.equals(user.getUser_name_en())) {
            //根据组id查询当前组和其所有子组
            groupIds = userService.getChildGroupIdsByGroupId(user.getGroup_id());
        }*/

        //查询该分类下满足条件的记录
        Query query = Query.query(Criteria.where(Dict.STATUS).is(Constants.ONE));
        if (!CommonUtils.isNullOrEmpty(searchContent)) {
            searchContent = ".*?" + searchContent + ".*";
            Criteria nameCriteria = Criteria.where(Dict.NAME).regex(searchContent);
            query.addCriteria(nameCriteria);
        }

//        Criteria categoryCriteria = Criteria.where(Dict.CATEGORY_CATEGORYID).is(category.getCategoryId());
//        query.addCriteria(categoryCriteria);
        //非管理员用户，对于private 只能看到自己的
        if (!checkAdminRole(user.getUser_name_en())) {
            Criteria publicCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.PUBLIC);
            Criteria userCriteria = Criteria.where(Dict.AUTHOR__ID).is(user.getId());
            Criteria groupsCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.GROUP);
            query.addCriteria(new Criteria().orOperator(publicCriteria, userCriteria, groupsCriteria));
        }
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
        List<PipelineTemplate> templatelist = mongoTemplate.find(query, PipelineTemplate.class);

        Map<String, Map<String, Object>> id_categoryMap = allCategory.stream().collect(Collectors.toMap(Category::getCategoryId, c -> {
            Map<String, Object> categoryMap = new HashMap<>();      //每个分类模板单元
            categoryMap.put(Dict.CATEGORYID, c.getCategoryId());      //存放分类id
            categoryMap.put(Dict.CATEGORYNAME, c.getCategoryName());          //存放分类名称
            categoryMap.put(Dict.PIPELINETEMPLATELIST, new ArrayList<Map>());
            return categoryMap;
        }, (a, b) -> b, LinkedHashMap::new));
        for (PipelineTemplate pipelineTemplate : templatelist) {
            /*if (pipelineTemplate.getVisibleRange().equals(Dict.GROUP) && !checkAdminRole(user.getUser_name_en())) {
                List<String> templateGroupIds = userService.getChildGroupIdsByGroupId(pipelineTemplate.getGroupId());
                if (CommonUtils.isNullOrEmpty(templateGroupIds) || !templateGroupIds.contains(user.getGroup_id())) {
                    continue;
                }
            }*/
            List<Map> l = ((List<Map>) (id_categoryMap.get(pipelineTemplate.getCategory().getCategoryId())).get(Dict.PIPELINETEMPLATELIST));
//            Map tempMap = preparePipelineTemplateMap(user, pipelineTemplate, groupIds);
            Map tempMap = preparePipelineTemplateMap(user, pipelineTemplate, null);
            l.add(tempMap);
            total++;
        }
        categoryList.addAll(id_categoryMap.values());
//        for (Category category : allCategory) {
//            if(!CommonUtils.isNullOrEmpty(templatelist)){
//                ArrayList<Map<String, Object>> arrayList = new ArrayList<>(); //模板单元中的数据
//                for (PipelineTemplate pipelineTemplate : templatelist) {
//                    //如果不是管理员，对于group的，只能看到用户在group子组的。 根据模板所在组id查询其所有子组,看用户在不在模板的子组内
//                    if (pipelineTemplate.getVisibleRange().equals(Dict.GROUP) && !checkAdminRole(user.getUser_name_en())){
//                        List<String> templateGroupIds = userService.getChildGroupIdsByGroupId(pipelineTemplate.getGroupId());
//                        if(CommonUtils.isNullOrEmpty(templateGroupIds) || !templateGroupIds.contains(user.getGroup_id())){
//                            continue;
//                        }
//                    }
//                    Map tempMap = preparePipelineTemplateMap(user, pipelineTemplate, groupIds);
//                    arrayList.add(tempMap);
//                }
//                Map<String, Object> categoryMap = new HashMap<>();      //每个分类模板单元
//                categoryMap.put(Dict.CATEGORYID, category.getCategoryId());      //存放分类id
//                categoryMap.put(Dict.CATEGORYNAME, category.getCategoryName());          //存放分类名称
//                categoryMap.put(Dict.PIPELINETEMPLATELIST, arrayList);
//                categoryList.add(categoryMap);  //将单元放入分类list中
//                total += templatelist.size();
//            }
//        }
        //返回记录总数
        map.put(Dict.TOTAL, total);
        map.put(Dict.CATEGORYLIST, categoryList);  //将分类map放入返回的结果集
        return map;
    }

    /**
     * 查询模板列表时，准备返回的一条PipelineMap
     *
     * @param user
     * @param pipelineTemplate
     * @param groupIds         用户所属组以及子组列表
     * @return
     * @throws Exception
     */
    private Map preparePipelineTemplateMap(User user, PipelineTemplate pipelineTemplate, List<String> groupIds) throws Exception {
        Map piplineTemplateMap = new HashMap<>();
        piplineTemplateMap.put(Dict.ID, pipelineTemplate.getId());
        piplineTemplateMap.put(Dict.NAMEID, pipelineTemplate.getNameId());
        piplineTemplateMap.put(Dict.STATUS, pipelineTemplate.getStatus());
        piplineTemplateMap.put(Dict.NAME, pipelineTemplate.getName());
        piplineTemplateMap.put(Dict.DESC, pipelineTemplate.getDesc());
        piplineTemplateMap.put(Dict.AUTHOR, pipelineTemplate.getAuthor());
        piplineTemplateMap.put(Dict.VERSION, pipelineTemplate.getVersion());
        piplineTemplateMap.put(Dict.UPDATETIME, pipelineTemplate.getUpdateTime());
        piplineTemplateMap.put(Dict.FIXEDMODEFLAG, pipelineTemplate.getFixedModeFlag());
        //小组管理员可以编辑本组模版、管理员可以编辑所有模版
        piplineTemplateMap.put(Dict.CANEDIT, Constants.ZERO);   //不可编辑
        if (checkAdminRole(user.getUser_name_en()) || user.getRole_id().contains(groupRoleAdminId)) {
            piplineTemplateMap.put(Dict.CANEDIT, Constants.ONE);   //可以编辑
        } else if (pipelineTemplate.getFixedModeFlag()) {
            piplineTemplateMap.put(Dict.CANEDIT, Constants.ZERO);   //如果是固定模板，只有admin白名单可以编辑
        } else {
           /* if (!CommonUtils.isNullOrEmpty(pipelineTemplate.getGroupId())) {
                //用户是小组管理员，并且用户组或者子组包含该模板所属组，用户可以编辑
                if (user.getRole_id().contains(groupRoleAdminId) && groupIds.contains(pipelineTemplate.getGroupId())) {
                }
            }*/
        }
        //空模版不允许编辑
        if (CommonUtils.isNullOrEmpty(pipelineTemplate.getStages()) || pipelineTemplate.getStages().size() == 0) {
            piplineTemplateMap.put(Dict.CANEDIT, Constants.ZERO);   //不可编辑
        }
        ArrayList<Map> stagesList = new ArrayList<>();
        for (Stage stage : pipelineTemplate.getStages()) {
            List<String> jobNames = new ArrayList<>();
            for (Job job : stage.getJobs()) {
                jobNames.add(job.getName());
            }
            Map<String, Object> stageItem = new HashMap<>();
            stageItem.put(Dict.STAGE_NAME, stage.getName());
            stageItem.put(Dict.JOBNAMES, jobNames);
            stagesList.add(stageItem);
        }
        piplineTemplateMap.put(Dict.STAGES, stagesList);
        piplineTemplateMap.put(Dict.VISIBLERANGE, pipelineTemplate.getVisibleRange());
        return piplineTemplateMap;
    }

    @Override
    public String add(PipelineTemplate template) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        template.setObjectId(objectId);
        template.setId(id);
        if (CommonUtils.isNullOrEmpty(template.getNameId())) {
            template.setNameId(id);
        }
        template.setStatus(Constants.STATUS_OPEN);
        template.setUpdateTime(TimeUtils.getDate(TimeUtils.FORMAT_DATE_TIME));
        Author author = userService.getAuthor();
        //增加白名单
        if (checkAdminRole(author.getNameEn())) {
            Author adminAuthor = new Author();
            adminAuthor.setId(Dict.SYSTEM);
            adminAuthor.setNameCn(Dict.SYSTEM);
            adminAuthor.setNameEn(Dict.SYSTEM);
            template.setAuthor(adminAuthor);
            if (CommonUtils.isNullOrEmpty(template.getVisibleRange())) {
                template.setVisibleRange(Dict.PUBLIC);
            }
        } else if (userService.getUserFromRedis().getRole_id().contains(groupRoleAdminId)) {
            template.setAuthor(userService.getAuthor());
            template.setGroupId(userService.getUserFromRedis().getGroup_id());
            if (CommonUtils.isNullOrEmpty(template.getVisibleRange())) {
                template.setVisibleRange(Dict.PUBLIC);
            }
        } else {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        if (CommonUtils.isNullOrEmpty(template.getVersion())) {
            template.setVersion(Constants.UP_CHANGE_VERSION);
        }
        return this.mongoTemplate.save(template).getId();
    }

    @Override
    public PipelineTemplate queryById(String id) {
        Query query = Query.query(Criteria.where(Dict.STATUS).ne(Constants.ZERO));
        Criteria queryId = Criteria.where(Dict.ID).is(id);
        query.addCriteria(queryId);
        PipelineTemplate pipelineTemplate = mongoTemplate.findOne(query, PipelineTemplate.class, "pipeline_template");
        return pipelineTemplate;
    }

    @Override
    public PipelineTemplate findActiveVersion(String nameId) {
        Query query = Query.query(Criteria.where(Dict.NAMEID).is(nameId).and(Dict.STATUS).is(Constants.STATUS_OPEN));
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
        return mongoTemplate.findOne(query, PipelineTemplate.class);
    }

    @Override
    public void setStatusClose(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.ID, id);
        update.set(Dict.STATUS, Constants.STATUS_CLOSE);
        mongoTemplate.findAndModify(query, update, PipelineTemplate.class);
    }

    @Override
    public void updateStatusClose(String id, User user) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.STATUS, Constants.STATUS_CLOSE);
        mongoTemplate.findAndModify(query, update, PipelineTemplate.class);
    }

    @Override
    public Map<String, Object> findHistoryPipelineTemplateList(long skip, int limit, String nameId) {
        Map<String, Object> map = new HashMap<>();
        Query query = Query.query(Criteria.where(Dict.NAMEID).is(nameId).and(Dict.STATUS).is(Constants.STATUS_CLOSE));
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
        long total = mongoTemplate.count(query, PipelineTemplate.class);
        map.put(Dict.TOTAL, total);
        if (limit != 0) {
            query.skip(skip).limit(limit);
        }
        List<PipelineTemplate> list = mongoTemplate.find(query, PipelineTemplate.class);
        map.put(Dict.TEMPLATELIST, list);
        return map;
    }

    @Override
    public String save(PipelineTemplate pipelineTemplate) throws Exception {
        return mongoTemplate.save(pipelineTemplate).getId();
    }

    @Override
    public PipelineTemplate updateVisibleRange(String id, PipelineTemplate pipelineTemplate) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.ID, id);
        update.set(Dict.VISIBLERANGE, pipelineTemplate.getVisibleRange());
        mongoTemplate.findAndModify(query, update, PipelineTemplate.class);
        PipelineTemplate resultPipelineTemplate = queryById(id);
        return resultPipelineTemplate;
    }

    @Override
    public PipelineTemplate queryByNameId(String nameId) throws Exception {
        Query query = Query.query(Criteria.where(Dict.STATUS).ne(Constants.ZERO));
        Criteria queryId = Criteria.where(Dict.NAMEID).is(nameId);
        query.addCriteria(queryId);
        PipelineTemplate pipelineTemplate = mongoTemplate.findOne(query, PipelineTemplate.class);
        return pipelineTemplate;
    }
}
