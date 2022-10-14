package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.service.IDemandBaseInfoService;
import com.spdb.fdev.fdemand.spdb.service.IMailService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RefreshScope
@RestController
@RequestMapping(value = "/api/mail")
public class MailController {

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IDemandBaseInfoService demandBaseInfoService;

    @Value("${history.demand.url}")
    private String history_demand_url;

    /**
     * 获取邮件发送内容
     * wangfq
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/getDeferContent")
    @RequestValidate(NotEmptyFields = {"demand_id",Dict.DEFER_REASON})
    public JsonResult getDeferContent(@RequestBody Map param) throws Exception {
        HashMap hashMap = new HashMap();
        String demand_id = (String) param.get("demand_id");
        DemandBaseInfo demandBaseInfo = demandBaseInfoService.queryById(demand_id);
        String user_name_cn = CommonUtils.getSessionUser().getUser_name_cn();
        hashMap.put(Dict.NAME_CN, demandBaseInfo.getDemand_instruction());
        hashMap.put(Dict.DEFER_REASON, param.get(Dict.DEFER_REASON));//获取暂缓原因
        hashMap.put(Dict.USER_NAME_CN, user_name_cn);
        hashMap.put(Dict.HISTORY_DEMAND_URL,history_demand_url);
        String mailContent = mailService.getDeferMailContent(hashMap);
        Map result = new HashMap();
        result.put(Dict.EMAIL_CONTENT, mailContent);
        return JsonResultUtil.buildSuccess(result);
    }

}
