package com.spdb.fdev.pipeline.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.MinioUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.ICategoryDao;
import com.spdb.fdev.pipeline.dao.IJobEntityDao;
import com.spdb.fdev.pipeline.dao.IPipelineTemplateDao;
import com.spdb.fdev.pipeline.dao.IPluginDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;


@Service
public class JobEntityServiceImpl implements IJobEntityService {

    private static Logger logger = LoggerFactory.getLogger(JobEntityServiceImpl.class);

    @Autowired
    private IJobEntityDao jobEntityDao;

    @Autowired
    private IPluginDao pluginDao;

    @Autowired
    private ICategoryDao categoryDao;

    @Autowired
    IPipelineService pipelineService;

    @Autowired
    IPipelineTemplateDao pipelineTemplateDao;

    @Autowired
    private MinioUtils minioUtils;

    @Value("${mioBuket}")
    private String mioBuket;

    @Override
    public List<Map> getAllJobs(String pageNum, String pageSize, User user, String searchContent) throws Exception {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        List<Map> list = jobEntityDao.queryAllJobs(skip, limit, user, searchContent);
//        Iterator<Map> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            if (CommonUtils.isNullOrEmpty(iterator.next().get(Dict.JOBLIST))) {
//                iterator.remove();
//            }
//        }
        return list;
    }

    @Override
    public JobEntity getJobTemplateInfo(String id) throws Exception {
        JobEntity entity = jobEntityDao.queryFullJobsById(id);
        if (CommonUtils.isNullOrEmpty(entity)) {
            return null;
        }
        for (Step step : entity.getSteps()) {
            PluginInfo pluginInfo = step.getPluginInfo();
            //组装scriptCmd
            if (!CommonUtils.isNullOrEmpty(pluginInfo.getScript())) {
                String path = pluginInfo.getScript().get(Dict.MINIO_OBJECT_NAME);
                String scriptCmd = minioUtils.minioDowloadFiles(mioBuket, path);
                pluginInfo.setScriptCmd(scriptCmd);
            }
            Plugin plugin = pluginDao.queryPluginDetail(pluginInfo.getPluginCode());
            if (Constants.ONE.equals(plugin.getStatus())) {
                pluginInfo.setValidFlag(true);
            } else {
                pluginInfo.setValidFlag(false);
            }
            step.setPluginInfo(pluginInfo);
        }
        if(entity.getImage() != null && !CommonUtils.isNullOrEmpty(entity.getImage().getId()) ){
            Images image = pipelineService.findImageById(entity.getImage().getId());
            entity.setImage(image);
        }

        return entity;
    }

    @Override
    public String add(JobEntity jobEntity) throws Exception {
        //检查steps
        for (Step step : jobEntity.getSteps()) {
            if (CommonUtils.isNullOrEmpty(step.getName())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{step.getName()});
            }
            if (CommonUtils.isNullOrEmpty(step.getPluginInfo())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{step.getName() + ".pluginInfo"});
            }
        }
        jobEntity.setJobId(null);
        jobEntity.setVersion(null);
        this.prepareJobEntity(jobEntity);
        return jobEntityDao.add(jobEntity);
    }

    @Override
    public String update(JobEntity jobEntity) throws Exception {
        //小组管理员可以编辑自己组的任务组模板、管理员可以编辑所有模板
        Boolean checkResult = this.checkUserRightByJobEntityid(jobEntity.getJobId());
        if (!checkResult) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //检查steps
        for (Step step : jobEntity.getSteps()) {
            if (CommonUtils.isNullOrEmpty(step.getName())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{step.getName()});
            }
            if (CommonUtils.isNullOrEmpty(step.getPluginInfo())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{step.getName() + ".pluginInfo"});
            }
        }
        String nameId = jobEntity.getNameId();
        //1. 旧版本status均设为0
        JobEntity oldJobEntity = jobEntityDao.findOldVersion(nameId);
        if (CommonUtils.isNullOrEmpty(oldJobEntity)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"nameId = " + nameId});
        }
        jobEntityDao.setStatusClose(oldJobEntity.getJobId());
        Integer version = Integer.valueOf(oldJobEntity.getVersion().split("\\.")[0]);
        Integer newVersion = version + 1;
        jobEntity.setVersion(String.valueOf(newVersion));
        this.prepareJobEntity(jobEntity);
        return jobEntityDao.add(jobEntity);
    }

    @Override
    public void delTemplate(String id) throws Exception {
        //小组管理员可以删除自己的任务组模板、管理员可以删除所有模板
        Boolean checkResult = this.checkUserRightByJobEntityid(id);
        if (!checkResult) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //删除仅把状态改为失效
        jobEntityDao.setStatusClose(id);
    }

    @Override
    public JobEntity updateVisibleRange(String id, String visibleRange) throws Exception {
        //小组管理员或管理员可以修改可见范围
        Boolean checkResult = this.checkUserRightByJobEntityid(id);
        if (!checkResult) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        JobEntity jobEntity = jobEntityDao.queryFullJobsById(id);
        if (CommonUtils.isNullOrEmpty(jobEntity)) {
            logger.error("********the jobEntity collection  can not find jobEntity******");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"jobId：" + id});
        }
        jobEntity.setVisibleRange(visibleRange);
        JobEntity resultJObEntity = jobEntityDao.updateVisibleRange(id, jobEntity);
        return resultJObEntity;
    }

    @Override
    public void updateStepsInJobEntity(String nameId,String type) throws Exception {
        List<Plugin> pluginList = pluginDao.queryPluginListByNameId(nameId);
        List<String> pluginCodeList = new ArrayList<>();
        pluginList.stream().forEach( e -> {
            pluginCodeList.add(e.getPluginCode());
        });
        List<JobEntity> jobEntityList = jobEntityDao.findAllByPluginCode(pluginCodeList);
        if (Constants.ZERO.equals(type)) {
            //删除
            for (JobEntity jobEntity : jobEntityList) {
                if (jobEntity.getSteps().size() > Integer.valueOf(Constants.ONE)) {
                    //删除其中该pluginCode的step
                    List<Step> stepList = jobEntity.getSteps();
                    for (Step step : stepList) {
                        if (pluginCodeList.contains(step.getPluginInfo().getPluginCode())) {
                            stepList.remove(step);
                        }
                    }
                    jobEntity.setSteps(stepList);
                    jobEntityDao.updateStepsInJobEntity(jobEntity);
                } else {
                    jobEntityDao.setStatusClose(jobEntity.getJobId());
                }
            }
        } else {
            //更新
            for (JobEntity jobEntity : jobEntityList) {
                for (Step step : jobEntity.getSteps()) {
                    step.getPluginInfo().setParams(null);
                    pipelineService.prepareJobTemplateStep(step);
                }
                jobEntityDao.updateStepsInJobEntity(jobEntity);
            }
        }
        // 同步更新实体模板
        List<PipelineTemplate> pipelineTemplateList = pipelineTemplateDao.queryByPluginCodeList(pluginCodeList);
        for (PipelineTemplate pipelineTemplate : pipelineTemplateList) {
            List<Stage> stages = pipelineTemplate.getStages();
            for (Stage stage : stages) {
                List<Job> jobs = stage.getJobs();
                for (Job job : jobs) {
                    List<Step> steps = job.getSteps();
                    for (Step step : steps) {
                        step.getPluginInfo().setParams(null);
                        pipelineService.prepareJobTemplateStep(step);
                    }
                }
            }
            pipelineTemplateDao.updateStages(pipelineTemplate);
        }
    }


    private Boolean checkUserRightByJobEntityid(String id) throws Exception {
        if (CommonUtils.isNullOrEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        JobEntity jobEntity = jobEntityDao.queryFullJobsById(id);
        if (CommonUtils.isNullOrEmpty(jobEntity)) {
            logger.error("**************jobEntity not exist***************jobId" + id);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"jobId" + id});
        }else if (CommonUtils.isNullOrEmpty(jobEntity.getSteps()) || jobEntity.getSteps().size() == 0){
            logger.error("**************this is empty jobEntity***************jobId"+id);
            throw new FdevException(ErrorConstants.EMPTY_JOBENTITY_NOT_UPDATE_OR_DELETE, new String[]{"jobId"+id});
        }else {
            String dataGroupId = jobEntity.getGroupId();
            return pipelineService.checkGroupidInUserGroup(dataGroupId);

        }
    }

    private Category getCustomedCategory() throws Exception {
        Category categoryParam = new Category();
        categoryParam.setCategoryLevel(Dict.JOBENTITY);
        categoryParam.setCategoryName(Constants.CATEGORY_DEFINE);
        List<Category> categoryList = categoryDao.getCategory(categoryParam);
        if (CommonUtils.isNullOrEmpty(categoryList)) {
            logger.error("********the category collection  can not find customed jobEntity******");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        Category one = categoryList.get(0);
        Category category = new Category();
        category.setCategoryId(one.getCategoryId());
        category.setCategoryName(one.getCategoryName());
        category.setCategoryNameEn(one.getCategoryNameEn());
        return category;
    }

    private JobEntity prepareJobEntity(JobEntity jobEntity) throws Exception {
        if (CommonUtils.isNullOrEmpty(jobEntity.getCategory())) {
            Category customedCategory = this.getCustomedCategory();
            jobEntity.setCategory(customedCategory);
        }
        List<Step> steps = jobEntity.getSteps();
        for (Step step : steps) {
            pipelineService.prepareJobTemplateStep(step);
            //模板里面的步骤都是不允许删除的
            step.setDeleteFlag(false);
        }
        return jobEntity;
    }

}
