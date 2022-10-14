package com.spdb.fdev.codeReview.spdb.controller;

import com.spdb.fdev.codeReview.base.annotation.nonull.NoNull;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.spdb.fdev.codeReview.spdb.service.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@CrossOrigin
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    IOrderService orderService;

    @PostMapping("/add")
    @RequestValidate(NotEmptyFields = {Dict.LEADER,Dict.PLANPRODUCTDATE, Dict.AUDITCONTENT, Dict.DEMANDID,
            Dict.TASKIDS,Dict.EMAILTO,Dict.REQUIREMENTSPECIFICATION,Dict.CODEREVIEWTABLE, Dict.PROTOTYPEFIGURE,Dict.DEMANDINSTRUCTIONBOOK})
    public JsonResult add(@RequestParam String leader,@RequestParam String planProductDate,
                          @RequestParam String auditContent, @RequestParam String demandId,
                          @RequestParam List<String> taskIds,@RequestParam List<String> emailTo,
                          @RequestParam String expectAuditDate,
                          @RequestParam MultipartFile[] requirementSpecification,@RequestParam MultipartFile[] codeReviewTable,
                           @RequestParam MultipartFile[] prototypeFigure,@RequestParam MultipartFile[] demandInstructionBook) throws Exception {
        CodeOrder codeOrder = new CodeOrder();
        codeOrder.setLeader(leader);
        codeOrder.setPlanProductDate(planProductDate);
        codeOrder.setAuditContent(auditContent);
        codeOrder.setDemandId(demandId);
        codeOrder.setTaskIds(new HashSet<>(taskIds));
        codeOrder.setEmailTo(new HashSet<>(emailTo));
        codeOrder.setExpectAuditDate(expectAuditDate);
        return JsonResultUtil.buildSuccess(orderService.add(codeOrder,requirementSpecification,codeReviewTable,prototypeFigure,demandInstructionBook));
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.ID,Dict.LEADER,Dict.PLANPRODUCTDATE,Dict.ORDERSTATUS,Dict.AUDITCONTENT,Dict.TASKIDS},parameter = CodeOrder.class)
    public JsonResult update(@RequestBody CodeOrder codeOrder) throws Exception {
        orderService.update(codeOrder);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/deleteOrderById")
    @NoNull(require = {Dict.ID},parameter = CodeOrder.class)
    public JsonResult deleteOrderById(@RequestBody CodeOrder codeOrder) throws Exception {
        String id = codeOrder.getId();
        orderService.deleteOrderById(id);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryOrders")
    public JsonResult queryOrders(@RequestBody Map param) throws Exception {
        return JsonResultUtil.buildSuccess(orderService.queryOrders(param));
    }

    @PostMapping("/exportOrderExcel")
    public JsonResult exportOrderExcel(@RequestBody Map param, HttpServletResponse resp) throws Exception {
        orderService.exportOrderExcel(param, resp);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryOrderById")
    @NoNull(require = {Dict.ID},parameter = CodeOrder.class)
    public JsonResult queryOrderById(@RequestBody CodeOrder codeOrder) throws Exception {
        String orderId = codeOrder.getId();
        return JsonResultUtil.buildSuccess(orderService.queryOrderById(orderId));
    }

    @PostMapping("/updateByTask")
    @NoNull(require = {Dict.ORDERNO},parameter = CodeOrder.class)
    public JsonResult updateByTask(@RequestBody CodeOrder codeOrder) throws Exception {
        orderService.updateByTask(codeOrder);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryListSimple")
    public JsonResult queryListSimple() throws Exception {
        return JsonResultUtil.buildSuccess(orderService.queryListSimple());
    }

    /**
     * 复审邮件提醒
     * @return
     */
    @PostMapping("/recheckRemind")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult recheckRemind(@RequestBody Map param) throws Exception {
        orderService.recheckRemind((String) param.get(Dict.ID));
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 申请复审邮件
     * @return
     */
    @PostMapping("/applyRecheck")
    @RequestValidate(NotEmptyFields = {Dict.ID, "recheckContent"})
    public JsonResult applyRecheck(@RequestBody Map param) throws Exception {
        orderService.applyRecheck((String) param.get(Dict.ID), (String) param.get("recheckContent"));
        return JsonResultUtil.buildSuccess();
    }
}
