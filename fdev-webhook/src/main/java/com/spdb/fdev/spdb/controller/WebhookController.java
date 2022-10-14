package com.spdb.fdev.spdb.controller;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.strategy.context.WebhookContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/webHook")
public class WebhookController {

    private static Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private WebhookContext webhookContext;

    @PostMapping
    public JsonResult webHook(@RequestBody String req, HttpServletRequest request) {
        logger.info("webhook接收到的消息是： " + req);
        Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
        if (CollectionUtils.isEmpty(parse)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        String event = request.getHeader(Dict.X_GITLAB_EVENT);
        String webHookHoken = request.getHeader(Dict.X_GITLAB_TOKEN);
        logger.info("webhook 的token 是 " + webHookHoken);
        parse.put(Dict.X_GITLAB_TOKEN, webHookHoken);
        logger.info("Trigger event:{}", event);
        webhookContext.getResource(event, parse);
        return JsonResultUtil.buildSuccess();
    }

}
