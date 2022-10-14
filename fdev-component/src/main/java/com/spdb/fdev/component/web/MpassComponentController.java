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
import com.spdb.fdev.component.entity.ComponentRecord;
import com.spdb.fdev.component.entity.MpassComponent;
import com.spdb.fdev.component.entity.MpassDevIssue;
import com.spdb.fdev.component.entity.MpassReleaseIssue;
import com.spdb.fdev.component.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mpasscomponent")
public class MpassComponentController {

    @Autowired
    private IMpassComponentService mpassComponentService;

    @Autowired
    private IComponentRecordService componentRecordService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private IMpassRelaseIssueService mpassRelaseIssueService;

    @Autowired
    private ICommonInfoService commonInfoService;

    /**
     * 查询所有mpass组件信息
     *
     * @param mpassComponent
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassComponents")
    public JsonResult queryMpassComponents(@RequestBody MpassComponent mpassComponent) throws Exception {
        List<Map> result = new ArrayList<>();
        List<MpassComponent> list = mpassComponentService.query(mpassComponent);
        for (MpassComponent info : list) {
            Map map = CommonUtils.object2Map(info);
            //查询历史版本表中的推荐版本
            ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                map.put(Dict.RECOMMOND_VERSION, record.getVersion());
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
     * 已有mpass组件录入
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addMpassComponent")
    @NoNull(require = {Dict.NAME_EN, Dict.NAME_CN, Dict.MANAGER, Dict.NPM_NAME, Dict.SOURCE,
            Dict.GROUP, Dict.TYPE, Dict.DESC}, parameter = MpassComponent.class)
    public JsonResult addMpassComponent(@RequestBody MpassComponent componentInfo) throws Exception {
        if (Constants.INNER_SOURCE.equals(componentInfo.getSource())
                && CommonUtils.isNullOrEmpty(componentInfo.getGitlab_url())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.GITLAB_URL});
        }
        return JsonResultUtil.buildSuccess(mpassComponentService.save(componentInfo));
    }

    /**
     * mpass组件删除
     *
     * @param componentInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/deleteMpassComponent")
    @NoNull(require = {Dict.ID}, parameter = MpassComponent.class)
    public JsonResult deleteMpassComponent(@RequestBody MpassComponent componentInfo) throws Exception {
        mpassComponentService.delete(componentInfo);
        return JsonResultUtil.buildSuccess(null);
    }

    /**
     * 查询组件详情
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassComponentDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryMpassComponentDetail(@RequestBody Map<String, String> param) throws Exception {
        String id = param.get(Dict.ID);
        MpassComponent info = mpassComponentService.queryById(id);
        if (info != null) {
            Map map = CommonUtils.object2Map(info);
            //查询历史版本表中的推荐版本
            ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                map.put(Dict.RECOMMOND_VERSION, record.getVersion());
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
     * 修改mpass组件信息
     *
     * @param mpassComponent
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateMpassComponent")
    @NoNull(require = {Dict.ID}, parameter = MpassComponent.class)
    public JsonResult updateMpassComponent(@RequestBody MpassComponent mpassComponent) throws Exception {
        return JsonResultUtil.buildSuccess(mpassComponentService.update(mpassComponent));
    }

    /**
     * 查询组件历史版本
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassComponentHistary")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID})
    public JsonResult queryMpassComponentHistary(@RequestBody Map param) {
        List<Map> result = new ArrayList<>();
        String componentId = (String) param.get(Dict.COMPONENT_ID);
        String type = (String) param.get(Dict.VERSION);//当前版本类型匹配，如alpha、rc、release
        String numVersion = (String) param.get(Dict.PREDICT_VERSION);//是否进行版本号匹配，如1.0.1
        Boolean packageType = (Boolean) param.get(Dict.PACKAGETYPE);//是否包含prepackage类型版本
        if (null == packageType) {
            packageType = false;
        }
        List<ComponentRecord> list = componentRecordService.getAlphaOrRcVersion(componentId, type, numVersion, packageType);
        for (ComponentRecord record : list) {
            Map map = CommonUtils.object2Map(record);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, record.getUpdate_user());
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 扫描mpass组件历史版本
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/scanMpassComponentHistory")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult scanMpassComponentHistory(@RequestBody Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        MpassComponent mpassComponent = mpassComponentService.queryById(id);
        componentScanService.initMpassComponentHistory(mpassComponent);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改组件历史版本
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateMpassComponentHistary")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.VERSION, Dict.COMPONENT_ID})
    public JsonResult updateMpassComponentHistary(@RequestBody Map param) throws Exception {
        return JsonResultUtil.buildSuccess(componentRecordService.updateForMpass(param));
    }

    /**
     * 查询所有mpass组件优化需求
     *
     * @param mpassReleaseIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassReleaseIssue")
    @NoNull(require = {Dict.COMPONENT_ID}, parameter = MpassReleaseIssue.class)
    public JsonResult queryMpassReleaseIssue(@RequestBody MpassReleaseIssue mpassReleaseIssue) throws Exception {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.query(mpassReleaseIssue));
    }

    /**
     * 查询所有组件优化需求详情
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassReleaseIssueDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryMpassReleaseIssueDetail(@RequestBody Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.queryMpassReleaseIssueDetail(id));
    }

    /**
     * 新增mpass组件优化需求
     *
     * @param mpassReleaseIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addMpassReleaseIssue")
    @NoNull(require = {Dict.COMPONENT_ID, Dict.TITLE, Dict.MANAGER, Dict.ISSUE_TYPE, Dict.FEATURE_BRANCH, Dict.PREDICT_VERSION, Dict.DUE_DATE}, parameter = MpassReleaseIssue.class)
    public JsonResult addMpassReleaseIssue(@RequestBody MpassReleaseIssue mpassReleaseIssue) throws Exception {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.save(mpassReleaseIssue));
    }




    /**
     * 更新组件优化需求窗口
     *
     * @param mpassReleaseIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateMpassReleaseIssue")
    @NoNull(require = {Dict.ID}, parameter = MpassReleaseIssue.class)
    public JsonResult updateMpassReleaseIssue(@RequestBody MpassReleaseIssue mpassReleaseIssue) throws Exception {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.update(mpassReleaseIssue));
    }

    /**
     * 查询可迁移窗口
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/queryTransgerReleaseIssue")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID,Dict.FEATURE_BRANCH})
    public JsonResult queryTransgerReleaseIssue(@RequestBody Map param) {
        String feature_branch= (String) param.get(Dict.FEATURE_BRANCH);
        String component_id = (String) param.get(Dict.COMPONENT_ID);
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.queryTransgerReleaseIssue(component_id,feature_branch));
    }

    /**
     * 开发分支迁移窗口
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/devIssueTransger")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.ISSUE_ID})
    public JsonResult devIssueTransger(@RequestBody Map<String, String> param) throws Exception {
        mpassRelaseIssueService.devIssueTransger(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据优化需求类型和组件名称回填分支和预设版本
     */
    @PostMapping(value = "/defaultBranchAndVersion")
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.ISSUE_TYPE})
    public JsonResult defaultBranchAndVersion(@RequestBody Map<String, String> param) throws Exception {
        String component_id = param.get(Dict.COMPONENT_ID);
        String issue_type = param.get(Dict.ISSUE_TYPE);
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.defaultBranchAndVersion(component_id, issue_type));
    }

    /**
     * 查询所有优化开发需求
     *
     * @param mpassDevIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassDevIssue")
    @NoNull(require = {Dict.ISSUE_ID}, parameter = MpassDevIssue.class)
    public JsonResult queryMpassDevIssue(@RequestBody MpassDevIssue mpassDevIssue) throws Exception {
        List<Map> result = new ArrayList<>();
        List<MpassDevIssue> query = mpassRelaseIssueService.query(mpassDevIssue);
        for (MpassDevIssue devIssue : query) {
            Map map = CommonUtils.object2Map(devIssue);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, devIssue.getAssignee());
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询所有组件优化开发需求详情
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassDevIssueDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryMpassDevIssueDetail(@RequestBody Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        MpassDevIssue mpassDevIssue = mpassRelaseIssueService.queryMpassDevIssueDetail(id);
        if (null != mpassDevIssue) {
            Map map = CommonUtils.beanToMap(mpassDevIssue);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, mpassDevIssue.getAssignee());
            return JsonResultUtil.buildSuccess(map);
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 新增mpass组件开发优化
     *
     * @param mpassDevIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addMpassDevIssue")
    @NoNull(require = {Dict.ISSUE_ID, Dict.ASSIGNEE, Dict.FEATURE_BRANCH, Dict.DUE_DATE}, parameter = MpassDevIssue.class)
    public JsonResult addMpassDevIssue(@RequestBody MpassDevIssue mpassDevIssue) throws Exception {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.save(mpassDevIssue));
    }

    /**
     * 修改Mpass开发需求
     *
     * @param mpassDevIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateMpassDevIssue")
    @NoNull(require = {Dict.ID}, parameter = MpassDevIssue.class)
    public JsonResult updateMpassDevIssue(@RequestBody MpassDevIssue mpassDevIssue) throws Exception {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.update(mpassDevIssue));
    }


    /**
     * 根据release已出包，获取下一出包版本
     * type为alpha或beta或rc
     *
     * @param param
     * @return
     */
    @PostMapping("/getNextAlphaorRcVersion")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_URL, Dict.TARGET_BRANCH, Dict.TYPE})
    public JsonResult getNextAlphaVersion(@RequestBody Map param) throws Exception {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.getNextAlphaVersion(param));
    }

    @PostMapping("/getNextAlphaorBetaVersion")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_URL, Dict.VERSION, Dict.TYPE,"component_type"})
    public JsonResult getNextAlphaorBetaVersion(@RequestBody Map param) throws Exception {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.getNextAlphaorBetaVersion(param));
    }

    /**
     * 持续集成获取发包路径
     *
     * @param param
     * @return
     */
    @PostMapping("/getRootDir")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_URL})
    public JsonResult getRootDir(@RequestBody Map param) {
        return JsonResultUtil.buildSuccess(mpassRelaseIssueService.getRootDir(param));
    }

    /**
     * 新增mpass组件版本记录，持续集成中直接调用
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/createComponentRecord")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_URL, Dict.SOURCE_BRANCH, Dict.TARGET_BRANCH, Dict.VERSION, Dict.RELEASE_LOG, Dict.GIT_USER_ID})
    public JsonResult createComponentRecord(@RequestBody Map param) throws Exception {
        mpassRelaseIssueService.createComponentRecord(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     *
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/addComponentRecord")
    @RequestValidate(NotEmptyFields = {Dict.GITLAB_URL, Dict.VERSION})
    public JsonResult addComponentRecord(@RequestBody Map param) throws Exception {
        mpassRelaseIssueService.addComponentRecord(param);
        return JsonResultUtil.buildSuccess();
    }



    /**
     * mpass组件开发打包
     * id为当前开发需求的id，stage为选择阶段打包
     *
     * @param param
     * @return
     */
    @PostMapping("/devPackage")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.STAGE})
    public JsonResult devPackage(@RequestBody Map param) throws Exception {
        if (mpassRelaseIssueService.canPackage(param)) {
            mpassRelaseIssueService.devPackage(param);
        }
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 修改当前开发需求的阶段
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/changeStage")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.STAGE})
    public JsonResult changeStage(@RequestBody Map<String, String> map) throws Exception {
        mpassRelaseIssueService.changeStage(map);
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
        mpassRelaseIssueService.releasePackage(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询优化需求打包版本
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryMpassIssueRecord")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.TYPE})
    public JsonResult queryMpassIssueRecord(@RequestBody Map param) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ComponentRecord> list = mpassRelaseIssueService.queryMpassIssueRecord(param);
        for (ComponentRecord record : list) {
            Map map = CommonUtils.object2Map(record);
            //查询用户模块，返回用户的中英文名
            roleService.addUserName(map, record.getUpdate_user());
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
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
        mpassRelaseIssueService.destroyIssue(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 批量更新组件模块持续集成文件
     */
    @PostMapping(value = "/updateGitlabCiYaml")
    public JsonResult updateGitlabCiYaml(@RequestBody Map<String, String> map) throws Exception {
        MpassComponent mpassComponent = new MpassComponent();
        List<MpassComponent> list = mpassComponentService.query(mpassComponent);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                MpassComponent info = list.get(i);
                if (StringUtils.isNotBlank(info.getGitlab_url())) {
                    commonInfoService.updateIntegration(info.getGitlab_url(), info.getName_en(), Constants.COMPONENT_MPASS_COMPONENT);//持续集成
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
    @PostMapping(value = "/queryMyMpassComponents")
    @RequestValidate(NotEmptyFields = {Dict.USER_ID})
    public JsonResult queryMyMpassComponents(@RequestBody Map<String, String> hashMap) throws Exception {
        String user_id = hashMap.get(Dict.USER_ID);
        List<Map> result = new ArrayList<>();
        List<MpassComponent> list = mpassComponentService.queryByUserId(user_id);
        for (MpassComponent info : list) {
            Map map = CommonUtils.object2Map(info);
            //查询历史版本表中的推荐版本
            ComponentRecord record = componentRecordService.queryByComponentIdAndType(info.getId(), Constants.RECORD_RECOMMEND_TYPE);
            if (record != null) {
                map.put(Dict.RECOMMOND_VERSION, record.getVersion());
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

}
