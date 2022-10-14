package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdemand.spdb.service.SendEmailDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/email")
public class SendEmailDemandController {

    @Autowired
    private SendEmailDemandService sendEmailDemandService;

    /**
     * zhanghp4
     * 需求评估超期通知邮件
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/sendEmailAssessDemand", method = RequestMethod.POST)
    public JsonResult sendDemandEmail() throws Exception{
        sendEmailDemandService.sendEmailAssessDemand();
        return JsonResultUtil.buildSuccess();
    }

}
