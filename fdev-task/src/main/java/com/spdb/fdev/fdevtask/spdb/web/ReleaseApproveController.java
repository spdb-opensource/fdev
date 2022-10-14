package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.spdb.service.IReleaseApproveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/12/29
 */
@RequestMapping("/api/releaseApprove")
@RestController
@RefreshScope
public class ReleaseApproveController {

    @Autowired
    IReleaseApproveService releaseApproveService;

    /**
     * 新增审批
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/add")
    @RequestValidate(NotEmptyFields = {"taskId","merge_reason"})
    public JsonResult add(@RequestBody Map param) throws Exception {
        releaseApproveService.add(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询审核列表，可根据审核状态(多选)、任务所属小组（多选）、申请人（多选）、审核人（多选）、任务名称（模糊搜索）、任务id（单选精准匹配）分页查询
     * @return
     */
    @PostMapping("/releaseApproveList")
    public JsonResult releaseApproveList(@RequestBody Map param) throws Exception {
        return JsonResultUtil.buildSuccess(releaseApproveService.releaseApproveList(param));
    }

    /**
     * 导出列表
     * @return
     */
    @PostMapping("/exportApproveList")
    public JsonResult exportApproveList(@RequestBody Map<String,Object> param, HttpServletResponse resp) throws Exception {
        releaseApproveService.exportApproveList(param,resp);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 审批通过
     * @param param
     * @return
     */
    @PostMapping("/pass")
    public  JsonResult pass(@RequestBody Map param) throws Exception {
        releaseApproveService.pass(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 审批拒绝
     * @param param
     * @return
     */
    @PostMapping("/refuse")
    public JsonResult refuse(@RequestBody Map param) throws Exception {
        releaseApproveService.refuse(param);
        return JsonResultUtil.buildSuccess();
    }

}
