package com.spdb.fdev.component.web;


import com.spdb.fdev.base.annotation.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.component.entity.ArchetypeIssue;
import com.spdb.fdev.component.entity.ArchetypeIssueTag;
import com.spdb.fdev.component.entity.MpassArchetype;
import com.spdb.fdev.component.service.IMpassArchetypeIssueService;
import com.spdb.fdev.component.service.IMpassArchetypeService;
import com.spdb.fdev.component.service.IRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mpassarchetype")
public class MpassArchetypeController {

    @Autowired
    private IMpassArchetypeService mpassArchetypeService;

    @Autowired
    private IMpassArchetypeIssueService mpassArchetypeIssueService;

    @Autowired
    private IRoleService roleService;

    /**
     * 查询所有mpass骨架信息
     *
     * @param mpassArchetype
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassArchetypes")
    public JsonResult queryMpassArchetypes(@RequestBody MpassArchetype mpassArchetype) throws Exception {
        List<Map> result = new ArrayList<>();
        List<MpassArchetype> list = mpassArchetypeService.query(mpassArchetype);
        for (MpassArchetype info : list) {
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
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 已有mpass骨架录入
     *
     * @param mpassArchetype
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addMpassArchetype")
    @NoNull(require = {Dict.NAME_EN, Dict.NAME_CN, Dict.DESC, Dict.GITLAB_URL,
            Dict.MANAGER, Dict.GROUP}, parameter = MpassArchetype.class)
    public JsonResult addMpassArchetype(@RequestBody MpassArchetype mpassArchetype) throws Exception {
        return JsonResultUtil.buildSuccess(mpassArchetypeService.save(mpassArchetype));
    }

    /**
     * 骨架更新
     *
     * @param mpassArchetype
     * @return
     * @throws Exception
     */
    @PostMapping(value = "updateMpassArchetype")
    @NoNull(require = {Dict.ID}, parameter = MpassArchetype.class)
    public JsonResult updateMpassArchetype(@RequestBody MpassArchetype mpassArchetype) throws Exception {
        return JsonResultUtil.buildSuccess(mpassArchetypeService.update(mpassArchetype));
    }

    /**
     * 查询骨架详情
     *
     * @param mpassArchetype
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassArchetypeDetail")
    @NoNull(require = {Dict.ID}, parameter = MpassArchetype.class)
    public JsonResult queryMpassArchetypeDetail(@RequestBody MpassArchetype mpassArchetype) throws Exception {
        MpassArchetype info = mpassArchetypeService.queryById(mpassArchetype);
        if (info != null) {
            Map map = CommonUtils.object2Map(info);
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
     * 查询骨架历史版本
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/queryMpassArchetypeHistory")
    @RequestValidate(NotEmptyFields = {Dict.ARCHETYPE_ID})
    public JsonResult queryMpassArchetypeHistory(@RequestBody Map param) {
        List<Map> result = new ArrayList<>();
        List<ArchetypeIssueTag> list = mpassArchetypeIssueService.queryMpassArchetypeHistory(param);
        for (ArchetypeIssueTag tag : list) {
            Map map = CommonUtils.object2Map(tag);
            if (StringUtils.isNotBlank(tag.getUpdate_user())) {
                roleService.addUserName(map, tag.getUpdate_user());
            }
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 新增mpass骨架开发优化
     *
     * @param archetypeIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addMpassArchetyepIssue")
    @NoNull(require = {Dict.ARCHETYPE_ID, Dict.TITLE, Dict.ASSIGNEE, Dict.DESC, Dict.FEATURE_BRANCH, Dict.DUE_DATE}, parameter = ArchetypeIssue.class)
    public JsonResult addMpassArchetyepIssue(@RequestBody ArchetypeIssue archetypeIssue) throws Exception {
        return JsonResultUtil.buildSuccess(mpassArchetypeIssueService.save(archetypeIssue));
    }

    /**
     * 查询mpass骨架优化需求
     *
     * @param archetypeIssue
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassArchetyepIssue")
    public JsonResult queryMpassArchetyepIssue(@RequestBody ArchetypeIssue archetypeIssue) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ArchetypeIssue> issueList = mpassArchetypeIssueService.query(archetypeIssue);
        for (ArchetypeIssue issue : issueList) {
            Map map = CommonUtils.object2Map(issue);
            if (StringUtils.isNotBlank(issue.getAssignee())) {
                roleService.addUserName(map, issue.getAssignee());
            }
            result.add(map);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询mpass骨架优化需求详情
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMpassArchetyepIssueDetail")
    public JsonResult queryMpassArchetyepIssueDetail(@RequestBody Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        ArchetypeIssue archetypeIssue = mpassArchetypeIssueService.queryById(id);
        if (null != archetypeIssue) {
            Map result = CommonUtils.beanToMap(archetypeIssue);
            roleService.addUserName(result, archetypeIssue.getAssignee());
            return JsonResultUtil.buildSuccess(result);
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * mpass骨架优化需求打包
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/package")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.STAGE})
    public JsonResult packageTag(@RequestBody Map param) throws Exception {
        mpassArchetypeIssueService.packageTag(param);
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
        mpassArchetypeIssueService.changeStage(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询骨架打的tag版本
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryIssueTag")
    @RequestValidate(NotEmptyFields = {Dict.ISSUE_ID})
    public JsonResult queryIssueTag(@RequestBody Map param) throws Exception {
        List<Map> result = new ArrayList<>();
        List<ArchetypeIssueTag> list = mpassArchetypeIssueService.queryIssueTag(param);
        for (ArchetypeIssueTag tag : list) {
            Map map = CommonUtils.object2Map(tag);
            if (StringUtils.isNotBlank(tag.getUpdate_user())) {
                roleService.addUserName(map, tag.getUpdate_user());
            }
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
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult destroyIssue(@RequestBody Map param) throws Exception {
        mpassArchetypeIssueService.destroyIssue(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据user_id查询我负责的骨架
     *
     * @param hashMap
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryMyMpassArchetypes")
    @RequestValidate(NotEmptyFields = {Dict.USER_ID})
    public JsonResult queryMyMpassArchetypes(@RequestBody Map<String, String> hashMap) throws Exception {
        String user_id = hashMap.get(Dict.USER_ID);
        List<MpassArchetype> list = mpassArchetypeService.queryByUserId(user_id);
        List<Map> result = new ArrayList<>();
        for (MpassArchetype info : list) {
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
        return JsonResultUtil.buildSuccess(result);
    }

}
