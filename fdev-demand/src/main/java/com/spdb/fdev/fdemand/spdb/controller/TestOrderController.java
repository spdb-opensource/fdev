package com.spdb.fdev.fdemand.spdb.controller;


import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.TestOrder;
import com.spdb.fdev.fdemand.spdb.dto.testorder.TestOrderDto;
import com.spdb.fdev.fdemand.spdb.service.ITestOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/test")
public class TestOrderController {

    @Autowired
    private ITestOrderService testOrderService;

    /**
     * 创建提测单
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/addTestOrder")
    public JsonResult addTestOrder(@RequestParam String testOrder,
                                   @RequestParam MultipartFile[] req_files,
                                   @RequestParam MultipartFile[] deInstruction_files,
                                   @RequestParam MultipartFile[] other_files) throws Exception {
        TestOrder testOrderDto = JSONObject.parseObject(testOrder, TestOrder.class);
        //必输字段校验
        if (CommonUtils.isNullOrEmpty(testOrderDto.getGroup())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"小组"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getDemand_id())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"需求"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getFdev_implement_unit_no())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"研发单元"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getInner_test_result())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"内测通过情况"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getTrans_interface_change())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"是否涉及交易接口改动"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getDatabase_change())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"是否涉及数据库改动"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getRegress_test())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"是否涉及回归测试"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getClient_change())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"是否涉及客户端更新"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getClient_download())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"客户端下载地址(明确具体的测试包)"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getTest_environment())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"测试环境"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getBusiness_email())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"业务人员邮箱"});
        }
        if (CommonUtils.isNullOrEmpty(testOrderDto.getTest_manager_info())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"测试经理"});
        }
        if (CommonUtils.isNullOrEmpty(req_files)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"需求规格说明书"});
        }
        if (CommonUtils.isNullOrEmpty(deInstruction_files)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"需求说明书"});
        }
        testOrderService.addTestOrder(testOrderDto,req_files,deInstruction_files,other_files);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 内测选项列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryInnerTestTab", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.FDEVUNITNOS})
    public JsonResult queryInnerTestTab(@RequestBody Map<String, String> params) throws Exception {
        return JsonResultUtil.buildSuccess(testOrderService.queryInnerTestTab(Arrays.asList(params.get(Dict.FDEVUNITNOS).split(","))));
    }

    /**
     * 提交提测单
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/submitTestOrder")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult submitTestOrder(@RequestBody Map<String,Object> params) throws Exception {
        testOrderService.submitTestOrder(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 撤销提测单
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/deleteTestOrder")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult deleteTestOrder(@RequestBody Map<String,Object> params) throws Exception {
        testOrderService.deleteTestOrder(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改提测单
     *
     * @param testOrder
     * @return
     * @throws Exception
     */
    @PostMapping("/updateTestOrder")
    @NoNull(require = {Dict.GROUP,Dict.DEMAND_ID, Dict.DEMAND_TYPE, Dict.FDEV_IMPLEMENT_UNIT_NO,Dict.INNER_TEST_RESULT
            ,Dict.TRANS_INTERFACE_CHANGE,Dict.DATABASE_CHANGE, Dict.REGRESS_TEST, Dict.CLIENT_CHANGE,Dict.CLIENT_DOWNLOAD,
            Dict.TEST_ENVIRONMENT}, parameter = TestOrder.class)
    public JsonResult updateTestOrder(@RequestBody TestOrder testOrder) throws Exception {
        testOrderService.updateTestOrder(testOrder);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 复制按钮控制
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryCopyFlag")
    public JsonResult queryCopyFlag(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(testOrderService.queryCopyFlag(params));
    }

    /**
     * 归档提测单
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/businessFileTestOrder")
    public JsonResult businessFileTestOrder(@RequestBody Map<String,Object> params) throws Exception {
        testOrderService.businessFileTestOrder(new IpmpUnit());
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询云测试平台测试人员
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/getTestManagerInfo")
    public JsonResult getTestManagerInfo(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(testOrderService.getTestManagerInfo(params));
    }

    /**
     * 查询提测单列表
     *
     * @param dto
     * @return
     * @throws Exception
     */
    @PostMapping("/queryTestOrderList")
    public JsonResult queryTestOrderList(@RequestBody TestOrderDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(testOrderService.queryTestOrderList(dto));
    }

    /**
     * 查询提测单详情
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryTestOrder")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryTestOrder(@RequestBody Map<String, String> params) throws Exception {
        return JsonResultUtil.buildSuccess(testOrderService.queryTestOrder(params.get(Dict.ID)));
    }

    /**
     * 上传文件
     *
     * @param files
     * @param testOrderId
     * @param fileType
     * 需求说明书 demandInstruction
     * 需求规格说明书 demandPlanInstruction
     * 内测报告 innerTestReport
     * 其他相关材料 otherRelatedFile
     * @return
     * @throws Exception
     */

    @RequestMapping(value = "/uploadTestOrderFile", method = RequestMethod.POST)
    public JsonResult uploadTestOrderFile(@RequestParam(value = "files") MultipartFile[] files,
                                          @RequestParam(value = "testOrderId") String testOrderId,
                                          @RequestParam(value = "fileType") String fileType

    ) throws Exception {
        testOrderService.uploadTestOrderFile(files, testOrderId, fileType);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询文件列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryTestOrderFile", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryTestOrderFile(@RequestBody Map<String, String> params) throws Exception {
        return JsonResultUtil.buildSuccess( testOrderService.queryTestOrderFile(params.get(Dict.ID)) );
    }

    /**
     * 删除文件
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/deleteTestOrderFile", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS})
    public JsonResult deleteTestOrderFile(@RequestBody Map<String, List<String>> params) throws Exception {
        testOrderService.deleteTestOrderFile(params.get(Dict.IDS));
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据需求ID查询研发单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryFdevUnitListByDemandId", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.DEMANDID})
    public JsonResult queryFdevUnitListByDemandId(@RequestBody Map<String, String> params) throws Exception {
        return JsonResultUtil.buildSuccess(testOrderService.queryFdevUnitListByDemandId(params.get(Dict.DEMANDID)));
    }

    /**
     * 研发单元提测提醒
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fdevUnitWarnDelay", method = RequestMethod.POST)
    public JsonResult fdevUnitWarnDelay(@RequestBody Map<String, String> params) throws Exception {
        testOrderService.fdevUnitWarnDelay();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 科技需求归档提测单
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/techDemandFileTestOrder", method = RequestMethod.POST)
    public JsonResult techDemandFileTestOrder(@RequestBody Map<String, String> params) throws Exception {
        testOrderService.getTechReqTestInfo();
        return JsonResultUtil.buildSuccess();
    }

}
