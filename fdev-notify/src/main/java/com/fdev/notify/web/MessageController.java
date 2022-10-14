package com.fdev.notify.web;

import com.fdev.notify.dict.Dict;
import com.fdev.notify.dict.ErrorConstants;
import com.fdev.notify.entity.Message;
import com.fdev.notify.service.MessageService;
import com.fdev.notify.util.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.common.validate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户通知
 * 公告通知
 */
@RestController
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Resource
    private MessageService messageService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserVerifyUtil userVerifyUtil;


    @RequestValidate(NotEmptyFields = {Dict.TARGET, Dict.CONTENT, Dict.TYPE, Dict.DESC})
    @RequestMapping(value = "/sendUserNotify", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult sendUserNotify(@RequestBody Map<String, Object> params) {
        Message message = null;
        String type = (String) params.get(Dict.TYPE);
        String hyperlink = (String) params.get(Dict.HYPERLINK);
        if("0".equals(type) || "1".equals(type)) {
            if(CommonUtils.isNullOrEmpty(hyperlink)) {
                throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[] {Dict.HYPERLINK + "不可为空"});
            }
        } else if("2".equals(type) || "3".equals(type)) {
            if(!CommonUtils.isNullOrEmpty(hyperlink)) {
                throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[] {Dict.HYPERLINK + "无需传参"});
            }
        } else {
            throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[] {Dict.TYPE + "传参错误"});
        }
        for (String target : (ArrayList<String>) params.get(Dict.TARGET)) {
            if (Util.isNullOrEmpty(params.get(Dict.HYPERLINK))) {
                message = new Message((String) params.get(Dict.CONTENT), (String) params.get(Dict.TYPE), target,
                        (String) params.get(Dict.DESC));
            } else {
                message = new Message((String) params.get(Dict.CONTENT), (String) params.get(Dict.TYPE), target,
                        (String) params.get(Dict.HYPERLINK), (String) params.get(Dict.DESC));
            }
            message.setTarget(Dict.FUSER + "-" + message.getTarget());
            messageService.addMessage(message);
            //redis发布消息
            stringRedisTemplate.convertAndSend("sendMessage",message.toString());
        }
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.CONTENT, Dict.TYPE})
    @RequestMapping(value = "/sendAnnounce", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult sendAnnounce(@RequestBody Map<String, String> params) throws Exception {
        //仅admin账号可发布
        User user = userVerifyUtil.getRedisUser();
        if(!"admin".equals(user.getUser_name_en())){
            throw new FdevException(ErrorConstants.USER_ROLE_LIMIT);
        }
        Message message = new Message(params.get(Dict.CONTENT), params.get(Dict.EXPIRY_TIME));
        message.setType(params.get(Dict.TYPE));
        try {
            messageService.addMessage(message);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.ANNOUNCE_SAVE_ERROR);
        }
        //redis发布消息
        stringRedisTemplate.convertAndSend("sendAnnounceRealTime",message.toString());
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryAnnounce", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryAnnounce() throws ParseException {
        List<Message> announces = messageService.queryAnnounce();
        return JsonResultUtil.buildSuccess(announces);
    }

    @RequestValidate(NotEmptyFields = {Dict.TARGET})
    @RequestMapping(value = "/queryMessage", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryMessage(@RequestBody Map<String, String> params) throws Exception {
        User user = userVerifyUtil.getRedisUser();
        if(!params.get(Dict.TARGET).equals(user.getUser_name_en())){
            throw new FdevException(ErrorConstants.USER_ROLE_LIMIT);
        }
        Message message = new Message();
        message.setTarget(Dict.FUSER + "-" + params.get(Dict.TARGET));
        List<Message> messages = null;
        try {
            messages = messageService.queryMessage(message);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.NOTIFY_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(messages);
    }

    @RequestValidate(NotEmptyFields = {Dict.TARGET})
    @RequestMapping(value = "/queryNoReadMessage", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryNoReadMessage(@RequestBody Map<String, String> params) throws Exception {
        User user = userVerifyUtil.getRedisUser();
        if(!params.get(Dict.TARGET).equals(user.getUser_name_en())){
            throw new FdevException(ErrorConstants.USER_ROLE_LIMIT);
        }
        Message message = new Message();
        message.setTarget(Dict.FUSER + "-" + params.get(Dict.TARGET));
        List<Message> messages = null;
        try {
            messages = messageService.queryNoReadMessage(message);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.NOTIFY_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(messages);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/updateNotifyStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateNotifyStatus(@RequestBody Map params) {
        List<String> ids = (List<String>) params.get(Dict.ID);
         messageService.updateMessageStatus(ids);
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.TARGET})
    @RequestMapping(value = "/queryMessageByType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryMessageByType(@RequestBody Map<String, String> params) throws Exception {
        User user = userVerifyUtil.getRedisUser();
        if(!params.get(Dict.TARGET).equals(user.getUser_name_en())){
            throw new FdevException(ErrorConstants.USER_ROLE_LIMIT);
        }
        Message message = new Message();
        message.setTarget(Dict.FUSER + "-" + params.get(Dict.TARGET));
        message.setDesc(params.get(Dict.DESC));
        message.setState(params.get(Dict.STATE));
        List<Message> messages = null;
        try {
            messages = messageService.queryMessageByType(message);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.NOTIFY_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(messages);
    }


}
