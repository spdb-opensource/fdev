package com.spdb.fdev.component.web;


import com.spdb.fdev.base.annotation.NoNull;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.component.entity.BaseImageInfo;
import com.spdb.fdev.component.entity.BaseImageIssue;
import com.spdb.fdev.component.entity.BaseImageRecord;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.component.service.impl.CommonInfoService;
import com.spdb.fdev.component.vo.BaseImageVoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/baseImage")
public class BaseImageController {

    @Autowired
    private IBaseImageInfoService baseImageInfoService;

    @Autowired
    private IBaseImageRecordService baseImageRecordService;

    @Autowired
    private IBaseImageIssueService baseImageIssueService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private CommonInfoService commonInfoService;

    @Autowired
    private IAppService appService;

    @Autowired
    private BaseImageVoService baseImageVoService;

    /**
     * 根据应用和镜像查询最新镜像版本（环境配置模块部署使用）
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/queryBaseImageVersion")
    @RequestValidate(NotEmptyFields = {Dict.GITLABID, Dict.IMAGE})
    public JsonResult queryBaseImageVersion(@RequestBody Map hashMap) {
        String gitlabId = (String) hashMap.get(Dict.GITLABID);
        String image = (String) hashMap.get(Dict.IMAGE);
        BaseImageRecord baseImageRecord = baseImageRecordService.queryByNameAndTrialApps(image, gitlabId);
        Map result = new HashMap();
        result.put(Constants.FDEV_CAAS_BASE_IMAGE_VERSION, baseImageRecord.getImage_tag());
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询基础镜像列表
     *
     * @param baseImageInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/queryBaseImage")
    public JsonResult queryBaseImage(@RequestBody BaseImageInfo baseImageInfo) throws Exception {
        return JsonResultUtil.buildSuccess(baseImageInfoService.query(baseImageInfo));
    }

    /**
     * 已有镜像录入
     *
     * @param baseImageInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/addBaseImage")
    @NoNull(require = {Dict.NAME, Dict.NAME_CN, Dict.GITLAB_URL, Dict.TYPE, Dict.GROUP, Dict.TARGET_ENV, Dict.MANAGER}, parameter = BaseImageInfo.class)
    public JsonResult addBaseImage(@RequestBody BaseImageInfo baseImageInfo) throws Exception {
        return JsonResultUtil.buildSuccess(baseImageInfoService.save(baseImageInfo));
    }


    /**
     * 更新基础镜像信息
     *
     * @param baseImageInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/updateBaseImage")
    @NoNull(require = {Dict.ID}, parameter = BaseImageInfo.class)
    public JsonResult updateBaseImage(@RequestBody BaseImageInfo baseImageInfo) throws Exception {
        return JsonResultUtil.buildSuccess(baseImageInfoService.update(baseImageInfo));
    }


    @PostMapping(value = "/relDevops")
    @RequestValidate(NotEmptyFields = {Dict.IMAGE_ID, Dict.BRANCH,"release_node_name"})
    public JsonResult relDevops(@RequestBody Map<String, String> map) throws Exception {
        baseImageInfoService.relDevops(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询基础镜像详情
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/queryBaseImageDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryBaseImageDetail(@RequestBody Map hashMap) {
        String id = (String) hashMap.get(Dict.ID);
        return JsonResultUtil.buildSuccess(baseImageInfoService.queryById(id));
    }


    /**
     * 根据基础镜像名称查询基础镜像历史版本列表
     *
     * @param baseImageRecord
     * @return
     * @throws Exception
     */
    @PostMapping("/queryBaseImageRecord")
    @NoNull(require = {Dict.NAME}, parameter = BaseImageRecord.class)
    public JsonResult queryBaseImageRecord(@RequestBody BaseImageRecord baseImageRecord) throws Exception {
        List<BaseImageRecord> recordList = baseImageRecordService.query(baseImageRecord);
        return JsonResultUtil.buildSuccess(baseImageVoService.queryBaseImageRecord(recordList));
    }


    /**
     * 查询基础镜像历史版本详情
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/queryBaseImageRecordDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryBaseImageRecordDetail(@RequestBody Map hashMap) {
        String id = (String) hashMap.get(Dict.ID);
        BaseImageRecord record = baseImageRecordService.queryById(id);
        return JsonResultUtil.buildSuccess(baseImageVoService.queryBaseImageRecordDetail(record));
    }

    /**
     * 新增镜像历史记录
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @PostMapping("/createBaseImageRecord")
    @RequestValidate(NotEmptyFields = {Dict.NAME, Dict.BRANCH, Dict.IMAGE_TAG, Dict.STAGE})
    public JsonResult createBaseImageRecord(@RequestBody Map hashMap) throws Exception {
        return JsonResultUtil.buildSuccess(baseImageRecordService.save(hashMap));
    }


    /**
     * 更新镜像镜像历史记录
     *
     * @param baseImageRecord
     * @return
     * @throws Exception
     */
    @PostMapping("/updateBaseImageRecord")
    @NoNull(require = {Dict.ID}, parameter = BaseImageRecord.class)
    public JsonResult updateBaseImageRecord(@RequestBody BaseImageRecord baseImageRecord) throws Exception {
        return JsonResultUtil.buildSuccess(baseImageRecordService.update(baseImageRecord));
    }


    /**
     * 将invalid版本恢复为正式版本
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/recoverInvalidRecord")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult recoverInvalidRecord(@RequestBody Map hashMap) throws Exception {
        String id = (String) hashMap.get(Dict.ID);
        baseImageRecordService.recoverInvalidRecord(id);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 新增基础镜像优化需求
     *
     * @param baseImageIssue
     * @return
     * @throws Exception
     */
    @PostMapping("/optimizeBaseImageIssue")
    @NoNull(require = {Dict.NAME, Dict.TITLE, Dict.BRANCH, Dict.ASSIGNEE}, parameter = BaseImageIssue.class)
    public JsonResult optimizeBaseImageIssue(@RequestBody BaseImageIssue baseImageIssue) throws Exception {
        return JsonResultUtil.buildSuccess(baseImageIssueService.save(baseImageIssue));
    }

    /**
     * 删除优化非已完成状态的优化需求
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/destroyBaseImageIssue")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult destroyBaseImageIssue(@RequestBody Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);
        baseImageIssueService.destroyBaseImageIssue(id);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 根据基础镜像名称查询优化需求列表
     *
     * @param baseImageIssue
     * @return
     * @throws Exception
     */
    @PostMapping("/queryBaseImageIssue")
    @NoNull(require = {Dict.NAME}, parameter = BaseImageIssue.class)
    public JsonResult queryBaseImageIssue(@RequestBody BaseImageIssue baseImageIssue) throws Exception {
        List<Map> result = new ArrayList<>();
        List<BaseImageIssue> issues = baseImageIssueService.query(baseImageIssue);
        if (!CommonUtils.isNullOrEmpty(issues)) {
            for (BaseImageIssue imageIssue : issues) {
                Map map = CommonUtils.object2Map(imageIssue);
                //查询用户模块，返回用户的中英文名
                roleService.addUserName(map, imageIssue.getAssignee());
                result.add(map);
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 查询优化需求详情
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/queryBaseImageIssueDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryBaseImageIssueDetail(@RequestBody Map hashMap) {
        String id = (String) hashMap.get(Dict.ID);
        BaseImageIssue imageIssue = baseImageIssueService.queryById(id);
        Map map = null;
        if (imageIssue != null) {
            map = CommonUtils.object2Map(imageIssue);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, imageIssue.getAssignee());
        }
        return JsonResultUtil.buildSuccess(map);
    }


    /**
     * 镜像发布
     *
     * @param record
     * @return
     */
    @PostMapping("/packageTag")
    @NoNull(require = {Dict.NAME, Dict.BRANCH, Dict.RELEASE_LOG, Dict.STAGE, Dict.UPDATE_USER}, parameter = BaseImageRecord.class)
    public JsonResult packageTag(@RequestBody BaseImageRecord record) throws Exception {
        baseImageIssueService.packageTag(record);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 根据镜像名称获取镜像元数据
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/queryMetaData")
    @RequestValidate(NotEmptyFields = {Dict.NAME})
    public JsonResult queryMetaData(@RequestBody Map hashMap) {
        String name = (String) hashMap.get(Dict.NAME);
        return JsonResultUtil.buildSuccess(baseImageInfoService.queryMetaData(name));
    }


    /**
     * 修改优化需求状态
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/changeStage")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.STAGE})
    public JsonResult changeStage(@RequestBody Map<String, String> map) throws Exception {
        baseImageIssueService.changeStage(map);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 根据镜像名称，分支，状态获取镜像历史版本列表
     *
     * @param map
     * @return
     */
    @PostMapping("queryIssueRecord")
    @RequestValidate(NotEmptyFields = {Dict.NAME, Dict.BRANCH, Dict.STAGE})
    public JsonResult queryIssueRecord(@RequestBody Map<String, String> map) {
        String name = map.get(Dict.NAME);
        String branch = map.get(Dict.BRANCH);
        String stage = map.get(Dict.STAGE);
        List<BaseImageRecord> recordList = baseImageRecordService.queryIssueRecord(name, branch, stage);
        List<Map> result = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(recordList)) {
            for (BaseImageRecord imageRecord : recordList) {
                Map beanMap = CommonUtils.object2Map(imageRecord);
                //查询用户模块，返回用户的中英文名
                roleService.addUserName(beanMap, imageRecord.getUpdate_user());
                result.add(beanMap);
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 当前优化需求试用版本升级为正式版本
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/relasePackage")
    @RequestValidate(NotEmptyFields = {Dict.NAME, Dict.BRANCH})
    public JsonResult relasePackage(@RequestBody Map<String, String> map) throws Exception {
        String name = map.get(Dict.NAME);
        String branch = map.get(Dict.BRANCH);
        baseImageRecordService.relasePackage(name, branch);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 批量更新组件模块持续集成文件
     */
    @PostMapping(value = "/updateGitlabCiYaml")
    public JsonResult updateGitlabCiYaml(@RequestBody Map<String, String> map) throws Exception {
        BaseImageInfo baseImageInfo = new BaseImageInfo();
        List<BaseImageInfo> list = baseImageInfoService.query(baseImageInfo);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                BaseImageInfo info = list.get(i);
                if (StringUtils.isNotBlank(info.getGitlab_url())) {
                    commonInfoService.updateIntegration(info.getGitlab_url(), info.getName(), Constants.COMPONENT_IMAGE);//持续集成
                }
            }
        }
        return JsonResultUtil.buildSuccess();
    }

}
