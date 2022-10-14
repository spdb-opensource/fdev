package com.spdb.fdev.component.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.base.annotation.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.component.dao.IComponentRecordDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RefreshScope
@RequestMapping(value = "/api/application")
public class ApplicationController {

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Value("${lock.scanApplication.timeout}")
    private int lock_scanapp_timeout;

    @Value("${lock.scanComponent.timeout}")
    private int lock_scancmp_timeout;

    @Autowired
    private IComponentApplicationService componentApplicationService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IMpassComponentService mpassComponentService;

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Autowired
    private IAppService appService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IBaseImageInfoService baseImageInfoService;

    @Autowired
    private IImageApplicationService imageApplicationService;

    /**
     * 根据应用id查询集成后端组件情况
     *
     * @param componentApplication
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/queryComponentsByApplicaton")
    @NoNull(require = {Dict.APPLICATION_ID}, parameter = ComponentApplication.class)
    public JsonResult queryComponentsByApplicaton(@RequestBody ComponentApplication componentApplication) throws Exception {
        List<ComponentApplication> componentApplicationList = componentApplicationService.query(componentApplication);
        List<Map> result = new ArrayList<>();
        for (ComponentApplication application : componentApplicationList) {
            ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(application.getComponent_id(), application.getComponent_version());
            Map map = CommonUtils.object2Map(application);
            if (record != null) {
                map.put(Dict.TYPE, record.getType());
                ComponentInfo info = componentInfoService.queryById(record.getComponent_id());
                if (info != null) {
                    map.put(Dict.NAME_EN, info.getName_en());
                    map.put(Dict.NAME_CN, info.getName_cn());
                    result.add(map);
                }
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 扫描应用使用后端组件的使用情况
     *
     * @return
     */
    @PostMapping("/scanApplication")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID})
    public JsonResult scanApplication(@RequestBody Map requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        //防止频繁扫描
        if (!redisTemplate.opsForValue().setIfAbsent("lock.scanApplication." + application_id, "1",
                this.lock_scanapp_timeout, TimeUnit.MINUTES)) {
            throw new FdevException(ErrorConstants.SCAN_OPERATION_TOO_FREQUENT,
                    new String[]{String.valueOf(this.lock_scanapp_timeout)});
        }
        componentApplicationService.scanApplication(application_id);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据组件id和组件版本查询所有集成此组件的应用
     * 组件版本可以不传
     *
     * @param componentApplication
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/queryApplicatonsByComponent")
    @NoNull(require = {Dict.COMPONENT_ID}, parameter = ComponentApplication.class)
    public JsonResult queryApplicatonsByComponent(@RequestBody ComponentApplication componentApplication) throws Exception {
        List<Map> result = componentApplicationService.queryApplicatonsByComponent(componentApplication);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 扫描所有使用该组件的应用信息同步入表
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/scanComponent")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID})
    public JsonResult scanComponent(@RequestBody Map<String, String> requestParam) throws Exception {
        String component_id = requestParam.get(Dict.COMPONENT_ID);
        //防止频繁扫描
        if (!redisTemplate.opsForValue().setIfAbsent("lock.scanComponent." + component_id, "1",
                this.lock_scancmp_timeout, TimeUnit.MINUTES))
            throw new FdevException(ErrorConstants.SCAN_OPERATION_TOO_FREQUENT,
                    new String[]{String.valueOf(this.lock_scancmp_timeout)});

        ComponentInfo componentInfo = componentInfoService.queryById(component_id);
        if (componentInfo == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"component can not find"});
        }
        //根据组件数据  删除所有 应用和组件关系
        ComponentApplication componentApplication = new ComponentApplication();
        componentApplication.setComponent_id(component_id);
        componentApplicationService.deleteAllByComponentId(componentApplication);
        componentScanService.initComponentApplication(componentInfo);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据组件id和组件版本查询所有集成此组件的骨架
     * 组件版本可以不传
     */
    @PostMapping("/queryFrameByComponent")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID})
    public JsonResult queryFrameByComponent(@RequestBody Map requestMap) throws Exception {
        List<Map> result = componentApplicationService.queryFrameByComponent(requestMap);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 根据应用id查询集成Mpass组件信息
     *
     * @param componentApplication
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/queryMpassComponentsByApplicaton")
    @NoNull(require = {Dict.APPLICATION_ID}, parameter = ComponentApplication.class)
    public JsonResult queryMpassComponentsByApplicaton(@RequestBody ComponentApplication componentApplication) throws Exception {
        List<ComponentApplication> componentApplicationList = componentApplicationService.query(componentApplication);
        List<Map> result = new ArrayList<>();
        for (ComponentApplication application : componentApplicationList) {
            ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion(application.getComponent_id(), application.getComponent_version());
            Map map = CommonUtils.object2Map(application);
            if (record != null) {
                map.put(Dict.TYPE, record.getType());
                MpassComponent info = mpassComponentService.queryById(record.getComponent_id());
                if (info != null) {
                    map.put(Dict.NAME_EN, info.getName_en());
                    map.put(Dict.NAME_CN, info.getName_cn());
                    result.add(map);
                }
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 扫描应用使用Mpass组件的使用情况
     *
     * @return
     */
    @PostMapping("/scanMpassComponentByApplication")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID})
    public JsonResult scanMpassComponentByApplication(@RequestBody Map requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        //防止频繁扫描
        if (!redisTemplate.opsForValue().setIfAbsent("lock.scanMpassComponentByApplication." + application_id, "1",
                this.lock_scanapp_timeout, TimeUnit.MINUTES)) {
            throw new FdevException(ErrorConstants.SCAN_OPERATION_TOO_FREQUENT,
                    new String[]{String.valueOf(this.lock_scanapp_timeout)});
        }
        componentApplicationService.scanMpassComponentByApplication(application_id);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据组件id查询所有集成Mpass组件的应用
     *
     * @param componentApplication
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/queryApplicationByMpassComponent")
    @NoNull(require = {Dict.COMPONENT_ID}, parameter = ComponentApplication.class)
    public JsonResult queryApplicationByMpassComponent(@RequestBody ComponentApplication componentApplication) throws Exception {
        List<Map> result = componentApplicationService.queryApplicatonsByComponent(componentApplication);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 扫描所有使用该Mpass组件的应用信息同步入表
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/scanApplicationByMpassComponent")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID})
    public JsonResult scanApplicationByMpassComponent(@RequestBody Map<String, String> requestParam) throws Exception {
        String component_id = requestParam.get(Dict.COMPONENT_ID);
        //防止频繁扫描
        if (!redisTemplate.opsForValue().setIfAbsent("lock.scanApplicationByMpassComponent." + component_id, "1",
                this.lock_scancmp_timeout, TimeUnit.MINUTES))
            throw new FdevException(ErrorConstants.SCAN_OPERATION_TOO_FREQUENT,
                    new String[]{String.valueOf(this.lock_scancmp_timeout)});

        MpassComponent mpassComponent = mpassComponentService.queryById(component_id);
        if (mpassComponent == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"component can not find"});
        }
        //根据组件数据  删除所有 应用和组件关系
        ComponentApplication componentApplication = new ComponentApplication();
        componentApplication.setComponent_id(component_id);
        componentApplicationService.deleteAllByComponentId(componentApplication);
        componentScanService.initMpassComponentApplication(mpassComponent);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据镜像查询应用使用情况
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/queryApplicationByImage")
    @RequestValidate(NotEmptyFields = {Dict.IMAGE_NAME})
    public JsonResult queryApplicationByImage(@RequestBody Map requestParam) {
        String imageName = (String) requestParam.get(Dict.IMAGE_NAME);
        List<Map> result = componentApplicationService.queryApplicationByImage(imageName);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 扫描所有使用该镜像的应用信息同步入表
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/scanImage")
    @RequestValidate(NotEmptyFields = {Dict.IMAGE_NAME})
    public JsonResult scanImage(@RequestBody Map<String, String> requestParam) throws Exception {
        String imageName = requestParam.get(Dict.IMAGE_NAME);
        //防止频繁扫描
        if (!redisTemplate.opsForValue().setIfAbsent("lock.scanImage." + imageName, "1",
                this.lock_scancmp_timeout, TimeUnit.MINUTES))
            throw new FdevException(ErrorConstants.SCAN_OPERATION_TOO_FREQUENT,
                    new String[]{String.valueOf(this.lock_scancmp_timeout)});

        BaseImageInfo baseImageInfo = baseImageInfoService.queryByName(imageName);
        if (baseImageInfo == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"Image can not find"});
        }
        //根据组件删除所有应用和组件关系
        imageApplicationService.deleteAllByImageName(imageName);
        componentScanService.initImageApplication(baseImageInfo);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据镜像查询骨架使用镜像情况
     * 组件版本可以不传
     */
    @PostMapping("/queryFrameByImage")
    @RequestValidate(NotEmptyFields = {Dict.IMAGE_NAME})
    public JsonResult queryFrameByImage(@RequestBody Map requestMap) throws Exception {
        List<Map> result = componentApplicationService.queryFrameByImage(requestMap);
        return JsonResultUtil.buildSuccess(result);
    }

}
