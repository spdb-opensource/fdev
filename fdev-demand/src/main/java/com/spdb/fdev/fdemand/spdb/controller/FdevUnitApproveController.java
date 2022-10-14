package com.spdb.fdev.fdemand.spdb.controller;


import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.service.IFdevUnitApproveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@RestController
@RequestMapping("/api/approve")
public class FdevUnitApproveController {

    @Autowired
    private IFdevUnitApproveService fdevUnitApproveService;

    /**
     * 查询评估完成时间和XY值
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryDemandAssessDate")
    public JsonResult queryDemandAssessDate(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevUnitApproveService.queryDemandAssessDate(params));
    }

    /**
     * 申请超期审批（发送研发单元超期申请审批邮件）
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/applyApprove")
    @RequestValidate(NotEmptyFields = {Dict.FDEVUNITNO})
    public JsonResult applyApprove(@RequestBody Map<String,Object> params) throws Exception {
        fdevUnitApproveService.applyApprove(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询我的审批
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryMyApproveList")
    @RequestValidate(NotEmptyFields = {Dict.CODE})
    public JsonResult queryMyApproveList(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevUnitApproveService.queryMyApproveList(params));
    }

    /**
     * 审批通过（支持批量审批）
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/approvePass")
    @RequestValidate(NotEmptyFields = {Dict.IDS})
    public JsonResult approvePass(@RequestBody Map<String,Object> params) throws Exception {
        fdevUnitApproveService.approvePass(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 审批拒绝（发送邮件，可填写拒绝原因）
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/approveReject")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult approveReject(@RequestBody Map<String,Object> params) throws Exception {
        fdevUnitApproveService.approveReject(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询审批列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryApproveList")
    public JsonResult queryApproveList(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevUnitApproveService.queryApproveList(params));
    }

    /**
     * 导出审批列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/exportApproveList")
    public JsonResult exportApproveList(@RequestBody Map<String,Object> params, HttpServletResponse resp) throws Exception {
        fdevUnitApproveService.exportApproveList(params,resp);
        return JsonResultUtil.buildSuccess();
    }




}
