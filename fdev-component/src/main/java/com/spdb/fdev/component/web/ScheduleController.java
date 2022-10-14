package com.spdb.fdev.component.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.component.schedule.service.ArchetypeVersionNotify;
import com.spdb.fdev.component.schedule.service.UpdateApplicationTask;
import com.spdb.fdev.component.schedule.service.UpdateComponentDependencyTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/schedule")
public class ScheduleController {

    @Autowired
    private UpdateApplicationTask updateApplicationTask;

    @Autowired
    private UpdateComponentDependencyTree updateComponentDependencyTree;

    @Autowired
    private ArchetypeVersionNotify archetypeVersionNotify;

    /**
     * 刷新组件和应用关联信息
     * 每天凌晨1点执行一次代码
     */
    @PostMapping(value = "/updateApplicationTaskFlag")
    public JsonResult updateApplicationTaskFlag(@RequestBody Map param) throws Exception {
        updateApplicationTask.updateApp();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 刷新最新组件自身依赖树
     * 每日凌晨1点执行一次代码
     */
    @PostMapping(value = "/updateConponentDependencyTree")
    public JsonResult updateConponentDependencyTree(@RequestBody Map param) throws Exception {
        updateComponentDependencyTree.updateComponent();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 检查骨架中涉及组件是否为最新推荐版本，若不是，邮件通知此骨架负责人
     * 每日上午9：30执行一次代码
     */
    @PostMapping(value = "/archetypeVersionNotify")
    public JsonResult archetypeVersionNotify(@RequestBody Map param) throws Exception {
        archetypeVersionNotify.checkComponent();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 更新最新的骨架使用的组件版本信息
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/archetypeAutoScan")
    public JsonResult archetypeAutoScan(@RequestBody Map param) {
        archetypeVersionNotify.archetypeAutoScan();
        return JsonResultUtil.buildSuccess();
    }
}
