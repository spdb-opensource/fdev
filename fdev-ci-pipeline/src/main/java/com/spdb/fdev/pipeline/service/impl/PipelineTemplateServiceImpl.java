package com.spdb.fdev.pipeline.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.ICategoryDao;
import com.spdb.fdev.pipeline.dao.IPipelineDao;
import com.spdb.fdev.pipeline.dao.IPipelineTemplateDao;
import com.spdb.fdev.pipeline.dao.IPluginDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.*;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PipelineTemplateServiceImpl implements IPipelineTemplateService {

    @Autowired
    private IPipelineTemplateDao pipelineTemplateDao;

    @Autowired
    private IPluginDao pluginDao;

    @Autowired
    private ICategoryDao categoryDao;

    @Autowired
    private IPipelineDao pipelineDao;

    @Autowired
    private IFileService fileService;

    @Value("${mioBuket}")
    private String mioBuket;

    @Autowired
    private IPluginService iPluginService;

    @Autowired
    private IAppService iAppService;

    @Autowired
    private IPipelineService iPipelineService;

    @Autowired
    private IUserService userService;

    @Value("${group.role.admin.id}")
    private String groupRoleAdminId;

    private static final Logger logger = LoggerFactory.getLogger(PipelineServiceImpl.class);

    @Override
    public void delTemplate(String id) throws Exception {
        User user = userService.getUserFromRedis();
        //小组管理员可以删除自己组流水线模板、管理员可以删除所有模板
        Boolean checkResult = checkUserRightByTemplateid(id);
        if (!checkResult) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //删除仅把状态改为失效
        PipelineTemplate template = pipelineTemplateDao.queryById(id);
        if (template != null) {
            pipelineTemplateDao.updateStatusClose(id, user);
            iPluginService.closeYamlConfigInStages(template.getStages());
        }
    }

    @Override
    public Map<String, Object> queryMinePipelineTemplateList(String pageNum, String pageSize, User user, String searchContent) throws Exception {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        Map<String, Object> map = pipelineTemplateDao.queryMinePipelineTemplateList(skip, limit, user, searchContent);
        return map;
    }


    @Override
    public String add(PipelineTemplate template) throws Exception {
        CommonUtils.stagesCheck(template.getStages());
        template.setNameId(null);
        template.setVersion(null);
        preparePipelineTemplate(template);
        //改成从请求参数中传递过来Category
        /*Category customedCategory = getCustomedCategory();
        template.setCategory(customedCategory);*/
        return pipelineTemplateDao.add(template);
    }


    public PipelineTemplate preparePipelineTemplate(PipelineTemplate pipelineTemplate) throws Exception {
        if (CommonUtils.isNullOrEmpty(pipelineTemplate.getCategory())) {
            Category customedCategory = getCustomedCategory();
            pipelineTemplate.setCategory(customedCategory);//没有模版分类  组内模板
        }
        List<Stage> stages = pipelineTemplate.getStages();
        for (Stage stage : stages) {
            List<Job> jobs = stage.getJobs();
            for (Job job : jobs) {
                for (Step step : job.getSteps()) {
                    iPipelineService.prepareStep(step);
                }
            }
        }
        return pipelineTemplate;
    }

    @Override
    public PipelineTemplate queryById(String id) throws Exception {
        //1. 查询库中的pipeline
        PipelineTemplate template = pipelineTemplateDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(template))
            return null;
        //2. 取出所有的plugin
        for (Stage stage : template.getStages()) {
            for (Job job : stage.getJobs()) {
                //替换image
                Images image = job.getImage();
                if (!CommonUtils.isNullOrEmpty(image)) {
                    image = pipelineDao.findImageById(image.getId());
                }
                job.setImage(image);
                for (Step step : job.getSteps()) {
                    PluginInfo pluginInfo = step.getPluginInfo();
                    String scriptCmd = null;
                    Map<String, String> script = pluginInfo.getScript();
                    if (!CommonUtils.isNullOrEmpty(script)) {
                        //String mioPath = script.get("minio_object_name");
                        String mioPath = script.get(Dict.MINIO_OBJECT_NAME);
                        if (!CommonUtils.isNullOrEmpty(mioPath)) {
                            scriptCmd = fileService.downloadDocumentFile(mioBuket, mioPath);
                        }
                    }
                    pluginInfo.setScriptCmd(scriptCmd);
                    iPipelineService.prePareResultShowPluginInfo(pluginInfo);
                }
            }
        }
        return template;
    }

    public Boolean checkUserRightByTemplateid(String id) throws Exception {
        if (CommonUtils.isNullOrEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        PipelineTemplate oldPipelineTemplate = pipelineTemplateDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(oldPipelineTemplate)) {
            logger.error("**************pipelineTemplate not exist***************pipelineTemplateId" + id);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"pipelineTemplateId" + id});
        } else if (CommonUtils.isNullOrEmpty(oldPipelineTemplate.getStages()) || oldPipelineTemplate.getStages().size() == 0) {
            logger.error("**************this is empty pipelineTemplate***************pipelineTemplateId" + id);
            throw new FdevException(ErrorConstants.EMPTY_PIPELINETEMPLATE_NOT_UPDATE_OR_DELETE, new String[]{"pipelineTemplateId" + id});
        } else {
            String dataGroupId = oldPipelineTemplate.getGroupId();
            return iPipelineService.checkGroupidInUserGroup(dataGroupId);
        }
    }

    /*else {
        String userId = oldPipelineTemplate.getAuthor().getId();
        if (!Dict.SYSTEM.equals(userId)){
            Map map = iAppService.queryUserInfoByUserId(userId);
            if (CommonUtils.isNullOrEmpty(map)){
                logger.error("***************************It has a error that getting userGroupId by userId");
                throw new FdevException(ErrorConstants.OTHER_APP_ERROR,new String[]{"用户获取组信息异常："+userId});
            }
            String groupId= String.valueOf(map.get(Dict.GROUPID));
            if (user.getRole_id().contains(groupRoleAdminId) && groupIds.contains(groupId)){
                return true;
            }
        }
    }*/
    @Override
    public String update(PipelineTemplate template) throws Exception {
        CommonUtils.stagesCheck(template.getStages());
        //小组管理员可以编辑自己组流水线模板、管理员可以编辑所有模板
        Boolean checkResult = checkUserRightByTemplateid(template.getId());
        if (!checkResult) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String nameId = template.getNameId();
        //1. 旧版本status均设为0
        PipelineTemplate orgPipelineTemplate = pipelineTemplateDao.findActiveVersion(template.getNameId());
        if (CommonUtils.isNullOrEmpty(orgPipelineTemplate)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"nameId = " + nameId});
        }
        pipelineTemplateDao.setStatusClose(orgPipelineTemplate.getId());
        //2. 新版本的版本号自动新增
        Integer version = Integer.valueOf(orgPipelineTemplate.getVersion().split("\\.")[0]);
        Integer newVersion = version + 1;
        template.setVersion(String.valueOf(newVersion));
        preparePipelineTemplate(template);
        return pipelineTemplateDao.add(template);
    }

    @Override
    public Map<String, Object> queryPipelineTemplateHistory(String nameId, String pageNum, String pageSize) throws Exception {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        Map<String, Object> maps = pipelineTemplateDao.findHistoryPipelineTemplateList(skip, limit, nameId);
        List<PipelineTemplate> historyTemplateList = (List<PipelineTemplate>) maps.get(Dict.TEMPLATELIST);
        if (!CommonUtils.isNullOrEmpty(historyTemplateList)) {
        }
        maps.put(Dict.TEMPLATELIST, historyTemplateList);
        return maps;
    }


    /**
     * 复制流水线模板
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public String copy(String id) throws Exception {
        PipelineTemplate pipelineTemplate = pipelineTemplateDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(pipelineTemplate)) {
            logger.error("**************pipelineTemplate not exist;pipelineTemplateId" + id);
            //throw new Exception("找不到该流水线模板，请重新复制~");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        //入库的pipelineTemplate
        PipelineTemplate newPipelineTemplate = new PipelineTemplate();
        BeanUtils.copyProperties(pipelineTemplate, newPipelineTemplate);
        newPipelineTemplate.setName(pipelineTemplate.getName() + Dict._COPY);
        newPipelineTemplate.setObjectId(null);
        newPipelineTemplate.setId(null);
        newPipelineTemplate.setNameId(null);
        newPipelineTemplate.setVersion(Constants.UP_CHANGE_VERSION);
        newPipelineTemplate.setAuthor(null);
        //管理员复制模版原本类型 小组管理员复制为自定义
//        Author author = userService.getAuthor();
//        if (!pipelineTemplateDao.checkAdminRole(author.getNameEn())) {
//            //admin或者白名单复制的，是原来的分类,其余人复制的，都在自定义里
//            Category category = getCustomedCategory();
//            newPipelineTemplate.setCategory(category);
//        }
        iPluginService.copyYamlConfigInStages(newPipelineTemplate.getStages());
        return pipelineTemplateDao.add(newPipelineTemplate);
    }

    /**
     * 流水线另存为模板
     *
     * @param pipelineId
     * @return
     * @throws Exception
     */
    @Override
    public String saveFromPipeline(String pipelineId) throws Exception {
        Pipeline pipeline = pipelineDao.queryById(pipelineId);
        if (CommonUtils.isNullOrEmpty(pipeline)) {
            logger.error("*****pipeline not exist;pipelineId :" + pipelineId);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"pipelineId" + pipelineId});
        }
        PipelineTemplate pipelineTemplate = new PipelineTemplate();
        BeanUtils.copyProperties(pipeline, pipelineTemplate);

        ObjectId objectId = new ObjectId();
        pipelineTemplate.setId(objectId.toString());
        pipelineTemplate.setNameId(objectId.toString());
        pipelineTemplate.setVersion("1");
        pipelineTemplate.setStatus(Constants.STATUS_OPEN);
        pipelineTemplate.setUpdateTime(TimeUtils.getDate(TimeUtils.FORMAT_DATE_TIME));

        Category category = getCustomedCategory();
        pipelineTemplate.setCategory(category);//模板分类
        //设置可见范围和用户信息
        if (pipelineTemplateDao.checkAdminRole(userService.getAuthor().getNameEn())) {
            Author adminAuthor = new Author();
            adminAuthor.setId(Dict.SYSTEM);
            adminAuthor.setNameCn(Dict.SYSTEM);
            adminAuthor.setNameEn(Dict.SYSTEM);
            pipelineTemplate.setAuthor(adminAuthor);
            pipelineTemplate.setVisibleRange(Dict.PUBLIC);
        } else if (userService.getUserFromRedis().getRole_id().contains(groupRoleAdminId)) {
            pipelineTemplate.setAuthor(userService.getAuthor());
            pipelineTemplate.setGroupId(userService.getUserFromRedis().getGroup_id());
            pipelineTemplate.setVisibleRange(Dict.PUBLIC);
        } else {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        return pipelineTemplateDao.save(pipelineTemplate);
    }

    /**
     * 返回组内模板的Category
     *
     * @return
     */
    public Category getCustomedCategory() throws Exception {
        Category categoryParam = new Category();
        categoryParam.setCategoryLevel(Dict.PIPELINETEMPLATE);
        categoryParam.setCategoryName(Dict.PIPELINETEMPLATE_GROUP);
        List<Category> categoryList = categoryDao.getCategory(categoryParam);
        if (CommonUtils.isNullOrEmpty(categoryList)) {
            logger.error("********the category collection  can not find customed pipelineTemplate******");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        Category one = categoryList.get(0);
        Category category = new Category();
        category.setCategoryId(one.getCategoryId());
        category.setCategoryName(one.getCategoryName());
        return category;
    }

    @Override
    public PipelineTemplate updateVisibleRange(String id, String visibleRange) throws Exception {
        PipelineTemplate pipelineTemplate = pipelineTemplateDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(pipelineTemplate)) {
            logger.error("********the pipeline_template collection  can not find pipelineTemplate******");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"流水线模版id：" + id});
        }
        //小组管理员或管理员可以修改可见范围
        Boolean checkResult = checkUserRightByTemplateid(id);
        if (!checkResult) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        pipelineTemplate.setVisibleRange(visibleRange);
        PipelineTemplate resultPipelineTemplate = pipelineTemplateDao.updateVisibleRange(id, pipelineTemplate);
        return resultPipelineTemplate;
    }
}
