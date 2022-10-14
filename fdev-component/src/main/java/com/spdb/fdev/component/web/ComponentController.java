package com.spdb.fdev.component.web;

import com.spdb.fdev.base.annotation.NoNull;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.component.dao.IComponentInfoDao;
import com.spdb.fdev.component.dao.IMpassComponentDao;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.component.service.impl.ComponentIssueServiceImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/component")
public class ComponentController {
    @Autowired
    private IMpassComponentService mpassComponentService;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IRoleService roleService;
    @Autowired
    private ComponentIssueServiceImpl componentIssueService;

    @Autowired
    private IComponentRecordService componentRecordService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private ICommonInfoService commonInfoService;

    @Autowired
    private IPythonService pythonService;

    @Autowired
    IReleaseNodeService iReleaseNodeService;

    @Autowired
    private IComponentReleaseIssueService componentReleaseIssueService;

    @Autowired
    private IMpassRelaseIssueService mpassRelaseIssueService;


    @Autowired
    private IComponentInfoDao componentInfoDao;

    @Autowired
    private IMpassComponentDao mpassComponentDao;

    @Autowired
    private IComponentRecordServiceHistoryLog componentRecordServiceHistoryLog;

    /**
     * 查询所有组件信息
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryComponents")
    public JsonResult queryComponentInfos(@RequestBody ComponentInfo componentInfo) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ComponentInfo> list = componentInfoService.query(componentInfo);
        for (ComponentInfo info : list) {
            Map map = CommonUtils.object2Map(info);
            //查询历史版本表中的推荐版本
            ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                map.put(Dict.RECOMMOND_VERSION, record.getVersion());
            } else {
                map.put(Dict.RECOMMOND_VERSION, "");
            }
            //查询是否有父组件
            ComponentInfo parentInfo = componentInfoService.queryById(info.getParentId());
            if (parentInfo != null) {
                map.put(Dict.PARENT_NAMEEN, parentInfo.getName_en());
            }
            //返回组名
            if (StringUtils.isNotBlank(info.getGroup())) {
                Map groupMap = roleService.queryByGroupId(info.getGroup());
                if (groupMap != null) {
                    map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                }
            }
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 新增应用
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/createComponent")
    @NoNull(require = {Dict.NAME_EN, Dict.NAME_CN, Dict.MANAGER_ID, Dict.DESC, Dict.GROUPID, Dict.ARTIFACTID}, parameter = ComponentInfo.class)
    public JsonResult createComponent(@RequestBody ComponentInfo componentInfo) throws Exception {
        return JsonResultUtil.buildSuccess(componentInfoService.create(componentInfo));
    }



    /**
     * 已有组件录入
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/addComponent")
    @RequestValidate(NotEmptyFields = {Dict.NAME_EN, Dict.NAME_CN, Dict.DESC, Dict.SOURCE,
            Dict.GROUPID, Dict.ARTIFACTID, Dict.MANAGER_ID, Dict.TYPE})
    public JsonResult addComponent(@RequestBody Map<String,Object> param) throws Exception {
        if (Constants.INNER_SOURCE.equals(param.get("source"))
                && CommonUtils.isNullOrEmpty(param.get("gitlab_url"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.GITLAB_URL});
        }
        return JsonResultUtil.buildSuccess(componentInfoService.save(param));
    }


    /**
     * 组件删除
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/deleteComponent")
    @NoNull(require = {Dict.ID}, parameter = ComponentInfo.class)
    public JsonResult deleteComponentInfo(@RequestBody ComponentInfo componentInfo) throws Exception {
        componentInfoService.delete(componentInfo);
        return JsonResultUtil.buildSuccess(null);
    }

    /**
     * 查询组件详情
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryComponentDetail")
    @NoNull(require = {Dict.ID}, parameter = ComponentInfo.class)
    public JsonResult queryComponentInfoDetail(@RequestBody ComponentInfo componentInfo) throws Exception {
        ComponentInfo info = componentInfoService.queryById(componentInfo);
        if (info != null) {
            Map map = CommonUtils.object2Map(info);
            //查询历史版本表中的推荐版本
            ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                map.put(Dict.RECOMMOND_VERSION, record.getVersion());
            } else {
                map.put(Dict.RECOMMOND_VERSION, "");
            }
            //查询是否有父组件
            ComponentInfo parentInfo = componentInfoService.queryById(info.getParentId());
            if (parentInfo != null) {
                map.put(Dict.PARENT_NAMEEN, parentInfo.getName_en());
            }
            //返回组名
            if (StringUtils.isNotBlank(info.getGroup())) {
                Map groupMap = roleService.queryByGroupId(info.getGroup());
                if (groupMap != null) {
                    map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                }
            }
            return JsonResultUtil.buildSuccess(map);
        }
        return JsonResultUtil.buildSuccess();
    }



    /**
     * 修改组件信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/updateComponent")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult updateComponent(@RequestBody Map<String,Object> param) throws Exception {
          return JsonResultUtil.buildSuccess(componentInfoService.update(param));
    }





    @PostMapping(value = "/relDevops")
    @RequestValidate(NotEmptyFields = {Dict.TYPE, Dict.COMPONENT_ID, Dict.BRANCH, "release_node_name"})
    public JsonResult relDevops(@RequestBody Map<String, String> map) throws Exception {
        componentInfoService.relDevops(map);
        return JsonResultUtil.buildSuccess();
    }


    @PostMapping(value = "/queryLatestVersion")
    @RequestValidate(NotEmptyFields = {Dict.TYPE, Dict.COMPONENT_ID, "release_node_name"})
    public JsonResult queryLatestVersion(@RequestBody Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(componentInfoService.queryLatestVersion(map));
    }


    @PostMapping(value = "/queryReleaseVersion")
    @RequestValidate(NotEmptyFields = {Dict.TYPE, Dict.COMPONENT_ID})
    public JsonResult queryReleaseVersion(@RequestBody Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(componentInfoService.queryReleaseVersion(map));
    }


    @PostMapping(value = "/queryTags")
    @RequestValidate(NotEmptyFields = {"release_node_name", "app_id"})
    public JsonResult queryTags(@RequestBody Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(iReleaseNodeService.queryTags(map));
    }


    @PostMapping(value = "/getDetailByIds")
    @RequestValidate(NotEmptyFields = {Dict.TYPE, Dict.IDS})
    public JsonResult getDetailByIds(@RequestBody @ApiParam Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(componentInfoService.getDetailByIds(requestParam));
    }


    /**
     * 查询组件历史版本
     *
     * @param componentRecord
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryComponentHistory")
    @NoNull(require = {Dict.COMPONENT_ID}, parameter = ComponentRecord.class)
    public JsonResult query(@RequestBody ComponentRecord componentRecord) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ComponentRecord> list = componentRecordService.queryByComponentIdAndRegexVersion(componentRecord);
        for (ComponentRecord record : list) {
            Map map = CommonUtils.object2Map(record);

            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, record.getUpdate_user());
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 扫描组件历史版本
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/scanComponentHistory")
    @NoNull(require = {Dict.ID, Dict.GROUPID, Dict.ARTIFACTID}, parameter = ComponentInfo.class)
    public JsonResult scanComponentHistory(@RequestBody ComponentInfo componentInfo) throws Exception {
        componentInfo = componentInfoService.queryById(componentInfo.getId());
        if (componentInfo.getType().equals(Constants.MULTI_COMPONENT)) {
            componentScanService.initMultiComponentHistory(componentInfo);
        } else {
            componentScanService.initComponentHistory(componentInfo);
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改组件历史版本
     *
     * @param componentRecord
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateComponentHistary")
    @NoNull(require = {Dict.ID}, parameter = ComponentRecord.class)
    public JsonResult update(@RequestBody ComponentRecord componentRecord) throws Exception {
        return JsonResultUtil.buildSuccess(componentRecordService.update(componentRecord));
    }

    /**
     * 查询所有组件优化需求
     *
     * @param componentIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryComponentIssues")
    @NoNull(require = {Dict.COMPONENT_ID}, parameter = ComponentIssue.class)
    public JsonResult queryComponentIssues(@RequestBody ComponentIssue componentIssue) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ComponentIssue> issueList = componentIssueService.query(componentIssue);
        for (ComponentIssue issue : issueList) {
            Map map = CommonUtils.object2Map(issue);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, issue.getAssignee());
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 保存合并请求记录
     *
     * @param tagRecord
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/saveMergeRequest")
    @NoNull(require = {Dict.GITLAB_ID, Dict.MERGE_REQUEST_ID}, parameter = TagRecord.class)
    public JsonResult queryComponentIssues(@RequestBody TagRecord tagRecord) throws Exception {
        componentInfoService.saveMergeRequest(tagRecord);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 查询所有的组件
     *
     * @param resMap
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryAllComponents")
    public JsonResult queryAllComponents(@RequestBody Map<String, String> resMap) throws Exception {

        List<Map> result = new ArrayList<>();
        //查询后端组件
        List<ComponentInfo> list1 = componentInfoService.query(new ComponentInfo());
        for (ComponentInfo info : list1) {
            if ("0".equals(info.getSource()) && !"3".equals(info.getType())) {

                if (!CommonUtils.isNullOrEmpty(info.getGitlab_id())) {

                    Map map = CommonUtils.object2Map(info);
                    //查询是否有父组件
                    ComponentInfo parentInfo = componentInfoService.queryById(info.getParentId());
                    if (parentInfo != null) {
                        map.put(Dict.PARENT_NAMEEN, parentInfo.getName_en());
                    }
                    //返回组名
                    if (StringUtils.isNotBlank(info.getGroup())) {
                        Map groupMap = roleService.queryByGroupId(info.getGroup());
                        if (groupMap != null) {
                            map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                        }
                    }
                    result.add(map);
                }
            }

        }

        //查询所有的前端组件
        List<MpassComponent> list2 = mpassComponentService.query(new MpassComponent());
        for (MpassComponent info : list2) {
            if ("0".equals(info.getSource())) {

                if (!CommonUtils.isNullOrEmpty(info.getGitlab_id())) {

                    Map map = CommonUtils.object2Map(info);
                    //返回组名
                    if (StringUtils.isNotBlank(info.getGroup())) {
                        Map groupMap = roleService.queryByGroupId(info.getGroup());
                        if (groupMap != null) {
                            map.put(Dict.GROUP_NAME, groupMap.get(Dict.NAME));
                        }
                    }
                    result.add(map);
                }
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 删除优化非已完成状态的优化需求
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/destroyComponentIssue")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult destroyComponentIssue(@RequestBody Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);
        componentIssueService.destroyComponentIssue(id);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 新增组件优化
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/optimizeComponent")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.TITLE, Dict.TARGET_VERSION, Dict.FEATURE_BRANCH, Dict.ASSIGNEE, Dict.DUE_DATE})
    public JsonResult addComponentIssue(@RequestBody Map<String, String> map) throws Exception {
        componentIssueService.save(map);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 保存版本号
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/saveTargetVersion")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.TARGET_VERSION, Dict.TYPE})
    public JsonResult saveTargetVersion(@RequestBody Map<String, String> map) throws Exception {
        componentIssueService.saveTargetVersion(map);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 如果有多个优化需求，发布时判断是否最小版本
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryFirstVersion")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID})
    public JsonResult queryFirstVersion(@RequestBody Map<String, String> map) {
        String component_id = map.get(Dict.COMPONENT_ID);
        List<ComponentIssue> list = componentIssueService.queryFirstVersion(component_id);
        if (null != list && list.size() > 0) {
            return JsonResultUtil.buildSuccess(list.get(list.size() - 1).getTarget_version());
        } else {
            return JsonResultUtil.buildSuccess();
        }
    }

    /**
     * 校验版本号
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/judgeTargetVersion")
    @RequestValidate(NotEmptyFields = {Dict.TYPE, Dict.COMPONENT_ID, Dict.TARGET_VERSION})
    public JsonResult judgeTargetVersion(@RequestBody Map<String, String> map) throws Exception {
        componentIssueService.judgeTargetVersion(map);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 前后端组件出测试包
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/testPackage")
    @RequestValidate(NotEmptyFields = {Dict.TYPE, Dict.PREDICT_VERSION, Dict.COMPONENT_ID, Dict.BRANCH, "packageType"})
    public JsonResult testPackage(@RequestBody Map<String, String> map) throws Exception {
        componentIssueService.testPackageTag(map);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 查询组件优化需求详情
     *
     * @param componentIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryComponentIssueDetail")
    @NoNull(require = {Dict.ID}, parameter = ComponentIssue.class)
    public JsonResult queryComponentIssueDetail(@RequestBody ComponentIssue componentIssue) throws Exception {
        Map map = new HashMap();
        ComponentIssue issue = componentIssueService.queryById(componentIssue);
        if (issue != null) {
            map = CommonUtils.object2Map(issue);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, issue.getAssignee());
            //查询组件的manageid
            ComponentInfo componentInfo = componentInfoService.queryById(issue.getComponent_id());
            if (componentInfo != null) {
                map.put(Dict.MANAGER_ID, componentInfo.getManager_id());
            }
        }
        return JsonResultUtil.buildSuccess(map);
    }

    @PostMapping(value = "/package")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.JDK_VERSION, Dict.STAGE,
            Dict.TARGET_VERSION, Dict.FEATURE_BRANCH})
    public JsonResult packageTag(@RequestBody Map<String, String> map) throws Exception {
        componentIssueService.packageTag(map);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping(value = "/queryReleaseLog")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID,
            Dict.TARGET_VERSION})
    public JsonResult queryReleaseLog(@RequestBody Map<String, String> map) {
        String componentId = map.get(Dict.COMPONENT_ID);
        String targetVersion = map.get(Dict.TARGET_VERSION);
        ComponentRecord record = componentIssueService.queryReleaseLog(componentId, targetVersion);
        return JsonResultUtil.buildSuccess(record);
    }

    @PostMapping(value = "/queryBranches")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_URL})
    public JsonResult queryBranches(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(componentIssueService.queryBranches(map));
    }


    @PostMapping(value = "/changeStage")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.STAGE})
    public JsonResult changeStage(@RequestBody Map<String, String> map) throws Exception {
        componentIssueService.changeStage(map);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping(value = "/queryIssueRecord")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.TARGET_VERSION, Dict.STAGE})
    public JsonResult queryIssueRecord(@RequestBody Map<String, String> map) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ComponentRecord> list = (List) componentIssueService.queryIssueRecord(map);
        for (ComponentRecord record : list) {
            Map beanMap = CommonUtils.object2Map(record);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(beanMap, record.getUpdate_user());
            result.add(beanMap);
        }
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 批量更新组件模块持续集成文件
     */
    @PostMapping(value = "/updateGitlabCiYaml")
    public JsonResult updateGitlabCiYaml(@RequestBody Map<String, String> map) throws Exception {
        ComponentInfo componentInfo = new ComponentInfo();
        List<ComponentInfo> list = componentInfoService.query(componentInfo);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                ComponentInfo info = list.get(i);
                if (StringUtils.isNotBlank(info.getGitlab_url())) {
                    commonInfoService.updateIntegration(info.getGitlab_url(), info.getName_en(), Constants.COMPONENT_COMPONENT);//持续集成
                }
            }
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据user_id查询我负责的组件
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMyComponents")
    @RequestValidate(NotEmptyFields = {Dict.USER_ID})
    public JsonResult queryMyComponents(@RequestBody Map<String, String> hashMap) throws Exception {
        String user_id = hashMap.get(Dict.USER_ID);
        List<Map> result = new ArrayList<>();
        List<ComponentInfo> list = componentInfoService.queryByUserId(user_id);
        for (ComponentInfo info : list) {
            Map map = CommonUtils.object2Map(info);
            //查询历史版本表中的推荐版本
            ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                map.put(Dict.RECOMMOND_VERSION, record.getVersion());
            }
            //查询是否有父组件
            ComponentInfo parentInfo = componentInfoService.queryById(info.getParentId());
            if (parentInfo != null) {
                map.put(Dict.PARENT_NAMEEN, parentInfo.getName_en());
            }
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 获取组件依赖树
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryDependencyTree")
    public JsonResult scanComponentDependency(@RequestBody Map<String, String> hashMap) throws Exception {
        String groupId = hashMap.get(Dict.GROUPID);
        String artifactId = hashMap.get(Dict.ARTIFACTID);
        String version = hashMap.get(Dict.VERSION);
        String dependency = pythonService.queryDependencyTree(groupId, artifactId, version);
        return JsonResultUtil.buildSuccess(dependency);
    }

    /**
     * 查询多模块组件预设版本号及分支名
     *
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/defaultBranchAndVersion")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.ISSUE_TYPE})
    public JsonResult defaultBranchAndVersion(@RequestBody Map<String, String> param) throws Exception {
        String component_id = param.get(Dict.COMPONENT_ID);
        String issue_type = param.get(Dict.ISSUE_TYPE);
        return JsonResultUtil.buildSuccess(componentInfoService.defaultBranchAndVersion(component_id, issue_type));
    }

    /**
     * 新增组件优化需求
     *
     * @param mpassReleaseIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addReleaseIssue")
    @NoNull(require = {Dict.COMPONENT_ID, Dict.TITLE, Dict.MANAGER, Dict.ISSUE_TYPE, Dict.FEATURE_BRANCH, Dict.PREDICT_VERSION, Dict.DUE_DATE}, parameter = MpassReleaseIssue.class)
    public JsonResult addReleaseIssue(@RequestBody MpassReleaseIssue mpassReleaseIssue) throws Exception {
        return JsonResultUtil.buildSuccess(componentReleaseIssueService.save(mpassReleaseIssue));
    }

    /**
     * 更新组件优化需求窗口
     *
     * @param mpassReleaseIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateReleaseIssue")
    @NoNull(require = {Dict.ID}, parameter = MpassReleaseIssue.class)
    public JsonResult updateReleaseIssue(@RequestBody MpassReleaseIssue mpassReleaseIssue) throws Exception {
        return JsonResultUtil.buildSuccess(componentReleaseIssueService.update(mpassReleaseIssue));
    }

    /**
     * 新增多模块组件开发优化
     *
     * @param mpassDevIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addDevIssue")
    @NoNull(require = {Dict.ISSUE_ID, Dict.ASSIGNEE, Dict.FEATURE_BRANCH, Dict.DUE_DATE}, parameter = MpassDevIssue.class)
    public JsonResult addDevIssue(@RequestBody MpassDevIssue mpassDevIssue) throws Exception {
        return JsonResultUtil.buildSuccess(componentReleaseIssueService.save(mpassDevIssue));
    }

    /**
     * 组件开发打包
     * id为当前开发需求的id，stage为选择阶段打包
     *
     * @param param
     * @return
     */
    @PostMapping("/devPackage")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.STAGE})
    public JsonResult devPackage(@RequestBody Map param) throws Exception {
        if (componentReleaseIssueService.canPackage(param)) {
            componentReleaseIssueService.devPackage(param);
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 优化需求废弃
     *
     * @param param
     * @return
     */
    @PostMapping("/destroyIssue")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.TYPE})
    public JsonResult destroyIssue(@RequestBody Map param) throws Exception {
        componentReleaseIssueService.destroyIssue(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * mpass组件release打包
     * id为当前release需求id
     *
     * @param param
     * @return
     */
    @PostMapping("/releasePackage")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.RELEASE_LOG})
    public JsonResult releasePackage(@RequestBody Map param) throws Exception {
        componentReleaseIssueService.releasePackage(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 获取多模块组件的alpha的打包版本
     *
     * @param hashMap
     * @return
     */
    @PostMapping("/getComponentAlphaVersion")
    public JsonResult getComponentAlphaVersion(@RequestBody Map<String, String> hashMap) throws Exception {
        String nextVersion = mpassRelaseIssueService.getComponentAlphaVersion(hashMap);
        return JsonResultUtil.buildSuccess(nextVersion);
    }

    /**
     * 获取当前需要打包的多模块组件的jdk版本
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @PostMapping("/queryJdkByGitlabUrl")
    public JsonResult queryJdkByGitlabUrl(@RequestBody Map<String, String> hashMap) throws Exception {
        String jdkVersion = mpassRelaseIssueService.queryJdkByGitlabUrl(hashMap);
        return JsonResultUtil.buildSuccess(jdkVersion);
    }

    /**
     * 新增多模块组件版本记录，持续集成中直接调用
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/createMutiComponentRecord")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_URL, Dict.TARGET_BRANCH, Dict.VERSION, Dict.RELEASE_LOG, Dict.GIT_USER_ID})
    public JsonResult createMutiComponentRecord(@RequestBody Map param) throws Exception {
        mpassRelaseIssueService.createMutiComponentRecord(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询优化需求打包版本
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryMultiIssueRecord")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.TYPE})
    public JsonResult queryMpassIssueRecord(@RequestBody Map param) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ComponentRecord> list = componentReleaseIssueService.queryMpassIssueRecord(param);
        for (ComponentRecord record : list) {
            Map map = CommonUtils.object2Map(record);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, record.getUpdate_user());
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @PostMapping(value = "/queryComponentRecordHis")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID})
    public JsonResult queryComponentRecordHis(@RequestBody Map<String, String> map) throws Exception {
        ComponentRecordHistoryLog componentRecordHistoryLog = new ComponentRecordHistoryLog();
        componentRecordHistoryLog.setComponent_id(map.get(Dict.COMPONENT_ID));
        return JsonResultUtil.buildSuccess(componentRecordServiceHistoryLog.queryByComponentId(componentRecordHistoryLog));
    }
}
