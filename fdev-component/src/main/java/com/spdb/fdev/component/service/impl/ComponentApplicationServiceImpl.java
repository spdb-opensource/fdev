package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.*;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.runnable.BaseRunnable;
import com.spdb.fdev.component.runnable.RunnableFactory;
import com.spdb.fdev.component.service.IAppService;
import com.spdb.fdev.component.service.ICommonScanService;
import com.spdb.fdev.component.service.IComponentApplicationService;
import com.spdb.fdev.component.service.IComponentScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ComponentApplicationServiceImpl implements IComponentApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ComponentApplicationServiceImpl.class);

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Value("${cache.app.isdelete}")
    private boolean cache_app_isdelete;

    @Autowired
    private IComponentApplicationDao componentApplicationDao;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private IComponentInfoDao componentInfoDao;

    @Autowired
    private IAppService appService;

    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Autowired
    private IComponentArchetypeDao componentArchetypeDao;

    @Autowired
    private IArchetypeInfoDao archetypeInfoDao;

    @Autowired
    private IArchetypeRecordDao archetypeRecordDao;

    @Autowired
    private IMpassComponentDao mpassComponentDao;

    @Autowired
    private IImageApplicationDao imageApplicationDao;

    @Autowired
    private IBaseImageRecordDao baseImageRecordDao;

    @Autowired
    private IImageArchetypeDao imageArchetypeDao;

    @Autowired
    private RunnableFactory runnableFactory;

    @Override
    public List<ComponentApplication> query(ComponentApplication componentApplication) throws JsonProcessingException {
        return componentApplicationDao.query(componentApplication);
    }

    @Override
    public ComponentApplication save(ComponentApplication componentApplication) {
        return componentApplicationDao.save(componentApplication);
    }

    @Override
    public ComponentApplication update(ComponentApplication componentApplication) throws JsonProcessingException {
        return componentApplicationDao.update(componentApplication);
    }

    @Override
    public ComponentApplication queryByComponentIdAndAppId(ComponentApplication componentApplication) {
        return componentApplicationDao.queryByComponentIdAndAppId(componentApplication);
    }

    @Override
    public void scanApplication(String application_id) throws Exception {
        Map map = appService.queryAPPbyid(application_id);
        if (map == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"applicaton can not find"});
        }
        String name_en = (String) map.get(Dict.NAME_EN);
        String git = (String) map.get(Dict.GIT);
        String type_name = (String) map.get(Dict.TYPE_NAME);
        if (ComponentEnum.AppTypeEnum.Java.getValue().equals(type_name) || ComponentEnum.AppTypeEnum.Container.getValue().equals(type_name)) {
            logger.info("开始扫描{}", type_name);
            try {
                componentScanService.gitOperation(name_en, git);
                BaseRunnable baseRunnable = runnableFactory.getRunnable(Constants.SCAN_APP_COMPONENT);
                baseRunnable.setParam(map);
                baseRunnable.run();
            } catch (Exception e) {
                logger.error("扫描应用{}失败,{}", name_en, e.getMessage());
            } finally {
                //根据配置选择是否删除缓存应用
                if (cache_app_isdelete) {
                    String localRepository = nas_apps_path + name_en;
                    CommonUtils.deleteFile(new File(localRepository));
                }
            }
        }
    }

    @Override
    public void delete(ComponentApplication componentApplication) throws Exception {
        componentApplicationDao.delete(componentApplication);
    }

    @Override
    public void deleteAllByApplicationId(ComponentApplication componentApplication) throws Exception {
        componentApplicationDao.deleteAllByApplicationId(componentApplication);
    }

    @Override
    public void deleteAllByComponentId(ComponentApplication componentApplication) throws Exception {
        componentApplicationDao.deleteAllByComponentId(componentApplication);
    }

    @Override
    public List<Map> queryApplicatonsByComponent(ComponentApplication componentApplication) throws Exception {
        List<ComponentApplication> list = this.query(componentApplication);
        List<Map> result = new ArrayList<>();
        for (ComponentApplication application : list) {
            Map map = new HashMap();
            Map app = null;
            try {
                app = appService.queryAPPbyid(application.getApplication_id());
            } catch (Exception e) {
                logger.error("获取项目{}信息失败,{}", application.getApplication_id(), e.getMessage());
                this.delete(application);
            }
            if (app != null) {
                map.put(Dict.NAME_EN, app.get(Dict.NAME_EN));
                map.put(Dict.NAME_ZH, app.get(Dict.NAME_ZH));
                map.put(Dict.SPDB_MANGERS, app.get(Dict.SPDB_MANGERS));
                map.put(Dict.GROUP, app.get(Dict.GROUP));
                map.put(Dict.DEV_MANAGERS, app.get(Dict.DEV_MANAGERS));
            }
            map.put(Dict.APPLICATION_ID, application.getApplication_id());
            map.put(Dict.COMPONENT_VERSION, application.getComponent_version());
            map.put(Dict.UPDATE_TIME, application.getUpdate_time());
            ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(application.getComponent_id(), application.getComponent_version());
            if (record != null) {
                map.put(Dict.TYPE, record.getType());
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map> queryFrameByComponent(Map<String, String> requetMap) throws Exception {
        List<Map> result = new ArrayList<>();
        //增加骨架相关信息
        ComponentArchetype componentArchetype = new ComponentArchetype();
        componentArchetype.setComponent_id(requetMap.get(Dict.COMPONENT_ID));
        List<ComponentArchetype> archetypeList = componentArchetypeDao.query(componentArchetype);
        if (archetypeList != null && archetypeList.size() > 0) {
            for (ComponentArchetype info : archetypeList) {
                Map typeMap = new HashMap();
                ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(info.getArchetype_id());
                if (archetypeInfo != null) {
                    typeMap.put(Dict.NAME_EN, archetypeInfo.getName_en());
                    typeMap.put(Dict.NAME_ZH, archetypeInfo.getName_cn());
                } else {
                    componentArchetypeDao.delete(info);
                }
                typeMap.put(Dict.ARCHETYPE_ID, info.getArchetype_id());
                typeMap.put(Dict.ARCHETYPE_VERSION, info.getArchetype_version());
                typeMap.put(Dict.COMPONENT_VERSION, info.getComponent_version());
                typeMap.put(Dict.ISLATEST, info.getIsLatest());
                typeMap.put(Dict.UPDATE_TIME, info.getUpdate_time());
                typeMap.put(Dict.ARCHETYPE, true);

                //查询骨架记录信息，当过滤type不为空 且 与骨架type 不相等时，不记录
                ArchetypeRecord archetypeRecord = archetypeRecordDao.queryByArchetypeIdAndVersion(info.getArchetype_id(), info.getArchetype_version());
                if (!CommonUtils.isNullOrEmpty(requetMap.get(Dict.TYPE)) && archetypeRecord != null && !archetypeRecord.getType().equals(requetMap.get(Dict.TYPE)))
                    continue;
                if (!CommonUtils.isNullOrEmpty(requetMap.get(Dict.TYPE)) && archetypeRecord == null)
                    continue;

                typeMap.put(Dict.TYPE, CommonUtils.isNullOrEmpty(archetypeRecord) ? "" : archetypeRecord.getType());
                ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(info.getComponent_id(), info.getComponent_version());
                if (CommonUtils.isNullOrEmpty(record)) {
                    typeMap.put("component_type", "");
                } else {
                    if (!CommonUtils.isNullOrEmpty(requetMap.get("component_type")) && !record.getType().equals(requetMap.get("component_type")))
                        continue;
                    typeMap.put("component_type", record.getType());
                }
                result.add(typeMap);
            }
        }
        return result;
    }

    /**
     * 异步扫描应用使用情况
     *
     * @param application_id
     * @throws Exception
     */
    @Override
    public void scanMpassComponentByApplication(String application_id) throws Exception {
        Map map = appService.queryAPPbyid(application_id);
        if (map == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"applicaton can not find"});
        }
        String name_en = (String) map.get(Dict.NAME_EN);
        String git = (String) map.get(Dict.GIT);
        String type_name = (String) map.get(Dict.TYPE_NAME);
        if (ComponentEnum.AppTypeEnum.Vue.getValue().equals(type_name)) {
            logger.info("开始扫描{}", type_name);
            try {
                componentScanService.gitOperation(name_en, git);
                BaseRunnable baseRunnable = runnableFactory.getRunnable(Constants.Scan_App_MpassComponent);
                baseRunnable.setParam(map);
                baseRunnable.run();
            } catch (Exception e) {
                logger.error("扫描应用{}失败,{}", name_en, e.getMessage());
            } finally {
                //根据配置选择是否删除缓存应用
                if (cache_app_isdelete) {
                    String localRepository = nas_apps_path + name_en;
                    CommonUtils.deleteFile(new File(localRepository));
                }
            }
        }
    }

    @Override
    public List<Map> queryApplicationByImage(String imageName) {
        List<ImageApplication> list = imageApplicationDao.queryByImage(imageName);
        List<Map> result = new ArrayList<>();
        for (ImageApplication application : list) {
            Map map = new HashMap();
            Map app = null;
            try {
                app = appService.queryAPPbyid(application.getApplication_id());
            } catch (Exception e) {
                logger.error("获取项目{}信息失败,{}", application.getApplication_id(), e.getMessage());
                imageApplicationDao.delete(application);
            }
            if (app != null) {
                map.put(Dict.NAME_EN, app.get(Dict.NAME_EN));
                map.put(Dict.NAME_ZH, app.get(Dict.NAME_ZH));
                map.put(Dict.SPDB_MANGERS, app.get(Dict.SPDB_MANGERS));
                map.put(Dict.GROUP, app.get(Dict.GROUP));
                map.put(Dict.DEV_MANAGERS, app.get(Dict.DEV_MANAGERS));
            }
            map.put(Dict.APPLICATION_ID, application.getApplication_id());
            map.put(Dict.IMAGE_TAG, application.getImage_tag());
            map.put(Dict.UPDATE_TIME, application.getUpdate_time());
            BaseImageRecord record = baseImageRecordDao.queryByNameAndTag(imageName, application.getImage_tag());
            if (record != null) {
                map.put(Dict.STAGE, record.getStage());
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map> queryFrameByImage(Map requestMap) throws Exception {
        List<Map> result = new ArrayList<>();
        String imageName = (String) requestMap.get(Dict.IMAGE_NAME);
        //增加骨架相关信息
        List<ImageArchetype> archetypeList = imageArchetypeDao.queryByImage(imageName);
        if (archetypeList != null && archetypeList.size() > 0) {
            for (ImageArchetype info : archetypeList) {
                Map typeMap = new HashMap();
                ArchetypeInfo archetypeInfo = archetypeInfoDao.queryById(info.getArchetype_id());
                if (archetypeInfo != null) {
                    typeMap.put(Dict.NAME_EN, archetypeInfo.getName_en());
                    typeMap.put(Dict.NAME_ZH, archetypeInfo.getName_cn());
                } else {
                    imageArchetypeDao.delete(info);
                }
                typeMap.put(Dict.ARCHETYPE_ID, info.getArchetype_id());
                typeMap.put(Dict.ARCHETYPE_VERSION, info.getArchetype_version());
                typeMap.put(Dict.IMAGE_TAG, info.getImage_tag());
                typeMap.put(Dict.UPDATE_TIME, info.getUpdate_time());
                typeMap.put(Dict.ARCHETYPE, true);

                //查询骨架记录信息，当过滤type不为空 且 与骨架type 不相等时，不记录
                ArchetypeRecord archetypeRecord = archetypeRecordDao.queryByArchetypeIdAndVersion(info.getArchetype_id(), info.getArchetype_version());
                if (!CommonUtils.isNullOrEmpty(requestMap.get(Dict.TYPE)) && archetypeRecord != null && !archetypeRecord.getType().equals(requestMap.get(Dict.TYPE)))
                    continue;
                if (!CommonUtils.isNullOrEmpty(requestMap.get(Dict.TYPE)) && archetypeRecord == null)
                    continue;
                BaseImageRecord record = baseImageRecordDao.queryByNameAndTag(imageName, info.getImage_tag());
                if (record != null) {
                    typeMap.put(Dict.STAGE, record.getStage());
                }
                result.add(typeMap);
            }
        }
        return result;
    }

}
