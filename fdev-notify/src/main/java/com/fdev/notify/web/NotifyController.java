package com.fdev.notify.web;

import com.fdev.notify.dict.Dict;
import com.fdev.notify.entity.Notify;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 *
 */
@RestController
public class NotifyController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestValidate(NotEmptyFields = {Dict.TARGET, Dict.CONTENT, Dict.MODULE})
    @RequestMapping(value = "/sendStageData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult sendStageData(@RequestBody Map<String, String> params) {
        Notify notify = new Notify(params.get(Dict.CONTENT), params.get(Dict.MODULE), params.get(Dict.TARGET));
        notify.setTarget(params.get(Dict.MODULE) + "-" + params.get(Dict.TARGET));
        //redis发布消息
        stringRedisTemplate.convertAndSend("sendNotify",notify.toString());
        return JsonResultUtil.buildSuccess();
    }
}
