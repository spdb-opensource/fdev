package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdemand.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dto.UpdateFinalDateQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.FdevFinalDateApprove;
import com.spdb.fdev.fdemand.spdb.service.IFdevFinalDateApproveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.spdb.fdev.fdemand.base.dict.Dict.*;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/fdevFinalDateApprove")
public class FdevFinalDateApproveController {

    @Autowired
    private IFdevFinalDateApproveService iFdevFinalDateApproveService;


    /**
     * 查询定稿日期修改审批列表
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryApproveList")
    public JsonResult queryApproveList(@RequestBody UpdateFinalDateQueryDto queryDto) throws Exception {
        return JsonResultUtil.buildSuccess(iFdevFinalDateApproveService.queryApproveList(queryDto));
    }

    /**
     * 查询定稿日期修改审批列表
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryMyList")
    @NoNull(require = {STATUS}, parameter = UpdateFinalDateQueryDto.class)
    public JsonResult queryMyList(@RequestBody UpdateFinalDateQueryDto queryDto) throws Exception {
        return JsonResultUtil.buildSuccess(iFdevFinalDateApproveService.queryMyList(queryDto));
    }

    /**
     * 同意审批，支持批量审批
     *
     * @param updateDto
     * @return
     * @throws Exception
     */
    @PostMapping("/agree")
    @NoNull(require = {"ids"}, parameter = UpdateFinalDateQueryDto.class)
    public JsonResult agree(@RequestBody UpdateFinalDateQueryDto updateDto) throws Exception {
        String agree = iFdevFinalDateApproveService.agree(updateDto);
        if (CommonUtils.isNullOrEmpty(agree)) {
            return JsonResultUtil.buildSuccess();
        } else {
            return JsonResultUtil.buildError("201", agree);
        }

    }

    /**
     * 同意审批，支持批量审批
     *
     * @param updateDto
     * @return
     * @throws Exception
     */
    @PostMapping("/refuse")
    @NoNull(require = {ID, STATE}, parameter = FdevFinalDateApprove.class)
    public JsonResult refuse(@RequestBody FdevFinalDateApprove updateDto) throws Exception {
        iFdevFinalDateApproveService.refuse(updateDto);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryCount")
    public JsonResult queryCount() throws Exception {
        return JsonResultUtil.buildSuccess(iFdevFinalDateApproveService.queryCount());
    }
}
