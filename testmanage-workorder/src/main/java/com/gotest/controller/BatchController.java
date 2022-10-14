package com.gotest.controller;

import com.gotest.service.BatchService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/batch")
public class BatchController {

    @Autowired
    private BatchService batchService;

    /**
     * 跑批工单组和负责人组长
     * @return
     * @throws Exception
     */
    @RequestMapping("/batchGroupManagerLeader")
    public JsonResult batchGroupManagerLeader() throws Exception {
        batchService.batchGroupManagerLeader();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 跑批提测表错误需求编号
     * @return
     * @throws Exception
     */
    @RequestMapping("/batchRqrNoInSubmit")
    public JsonResult batchRqrNoInSubmit() throws Exception {
        batchService.batchRqrNoInSubmit();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 跑批工单组和负责人组长
     * @return
     * @throws Exception
     */
    @RequestMapping("/batchInsertDemandNo")
        public JsonResult batchInsertDemandNo() throws Exception {
        batchService.batchInsertDemandNo();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 跑批工单需求名称
     * @return
     * @throws Exception
     */
    @RequestMapping("/batchInsertDemandName")
    public JsonResult batchInsertDemandName() throws Exception {
        batchService.batchInsertDemandName();
        return JsonResultUtil.buildSuccess();
    }

}
