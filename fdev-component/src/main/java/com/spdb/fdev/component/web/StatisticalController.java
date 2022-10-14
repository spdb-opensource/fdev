package com.spdb.fdev.component.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.component.service.impl.ComponentIssueServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class StatisticalController {

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IMpassComponentService iMpassComponentService;

    @Autowired
    private IArchetypeInfoService iArchetypeInfoService;

    @Autowired
    private IMpassArchetypeService iMpassArchetypeService;

    @Autowired
    private IBaseImageInfoService iBaseImageInfoService;

    @Autowired
    private IComponentRecordService iComponentRecordService;

    @Autowired
    private IMpassRelaseIssueService mpassRelaseIssueService;

    @Autowired
    private IArchetypeIssueService iArchetypeIssueService;

    @Autowired
    private IBaseImageIssueService iBaseImageIssueService;

    @Autowired
    private IMpassArchetypeIssueService iMpassArchetypeIssueService;

    @Autowired
    private ComponentIssueServiceImpl componentIssueService;

    /**
     * 统计组件、骨架、镜像总数
     *
     * */
    @PostMapping(value = "/queryNumByType")
    public JsonResult queryNumByType(@RequestBody ComponentInfo componentInfo) throws Exception {
        List<Object> numInfo = new ArrayList<>();
        Map<String, Object> dates = new HashMap<>();
        MpassComponent mpassComponent = new MpassComponent();
        ArchetypeInfo archetypeInfo = new ArchetypeInfo();
        MpassArchetype mpassArchetype = new MpassArchetype();
        BaseImageInfo baseImageInfo = new BaseImageInfo();
        List componentNum= componentInfoService.query(componentInfo);
        List mpassComponentNum = iMpassComponentService.query(mpassComponent);
        List archetypeNum = iArchetypeInfoService.query(archetypeInfo);
        List mpassArchetypeNum = iMpassArchetypeService.query(mpassArchetype);
        List baseImageNum = iBaseImageInfoService.query(baseImageInfo);
        dates.put(Dict.COMPONENTS,componentNum.size());
        dates.put(Dict.ARCHETYPES,archetypeNum.size());
        dates.put(Dict.MPASSCOMPONENTS,mpassComponentNum.size());
        dates.put(Dict.MPASSARCHETYPES,mpassArchetypeNum.size());
        dates.put(Dict.BASEIMAGE,baseImageNum.size());
        numInfo.add(dates);
        return JsonResultUtil.buildSuccess(numInfo);
    }


    /**
     * 统计近6周组件、骨架、镜像数量增长趋势
     *
     * */
    @PostMapping(value = "/queryDataByType")
    public JsonResult queryDataByType(@RequestBody ComponentInfo componentInfo) throws ParseException {
        Map<String, Object> componentInfos = componentInfoService.queryDataByType(componentInfo);
        return  JsonResultUtil.buildSuccess(componentInfos);
    }

    /**
     *
     *获取组件、骨架、镜像测试、正式版本发布数
     *
     * */
    @PostMapping(value = "/queryIssueData")
    public JsonResult queryIssueData(@RequestBody Map requestParam) throws ParseException {
        Map<String, Object> IssueData = iComponentRecordService.queryIssueData(requestParam);
        return  JsonResultUtil.buildSuccess(IssueData);
    }

    /**
     *
     *获取用户目前参与各个阶段的优化需求
     *
     * */
    @PostMapping(value = "/queryQrmntsData")
    public JsonResult  queryQrmntsData(@RequestBody Map requestParam) throws Exception {
        if(CommonUtils.isNullOrEmpty(requestParam)){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"请求参数不能为空"});
        }
        List<HashMap<String, Object>> list = componentIssueService.queryQrmntsData(requestParam);
        return JsonResultUtil.buildSuccess(list);
    }

    /**
     *
     *获取优化需求延期情况
     *
     * */
    @PostMapping(value = "/queryIssueDelay")
    public JsonResult queryIssueDelay(@RequestBody Map requestParam) throws Exception {
        List<Map> allNum = componentIssueService.queryIssueDelay(requestParam);
        return JsonResultUtil.buildSuccess(allNum);
    }

    /**
     *
     *获取所有基础架构维度下信息
     *
     * */
    @PostMapping(value = "/allIsuue")
    public JsonResult queryAllIssue(@RequestBody Map requestParam) throws Exception {
        List list = componentInfoService.queryAllIssue(requestParam);
        return JsonResultUtil.buildSuccess(list);
    }
}
