package com.test.controller;

import com.test.dict.Dict;
import com.test.dict.ErrorConstants;
import com.test.entity.Message;
import com.test.service.MessageService;
import com.test.service.UserService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;
import com.test.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/notify")
public class NotityController {
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private UserService userService;
	
    @RequestValidate(NotEmptyFields = {Dict.CONTENT, Dict.TYPE})
    @PostMapping(value = "/sendAnnounce")
    public JsonResult sendAnnounce(@RequestBody Map<String, String> params) throws Exception {
        if(!userService.vaildateAdmin()){
            throw new FtmsException(ErrorConstants.ROLE_ERROR);
        };
        Message message = new Message(params.get(Dict.CONTENT), params.get(Dict.EXPIRY_TIME));
        message.setType(params.get(Dict.TYPE));
        try {
        	messageService.addMessage(message);
        } catch (Exception e) {
        }
        WebSocketServer.sendAnnounceRealTime(message);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping(value = "/queryAnnounce")
    public JsonResult queryAnnounce(@RequestBody Map<String, String> params) {
        List<Message> messages = null;
        try {
            messages = messageService.queryMessage(null);
        } catch (Exception e) {
        }
        return JsonResultUtil.buildSuccess(messages);
    }
    
    @RequestValidate(NotEmptyFields = {Dict.TARGET, Dict.CONTENT, Dict.TYPE})
    @PostMapping(value = "/sendUserNotify")
    public JsonResult sendUserNotify(@RequestBody Map<String, Object> params) {
        Message message = null;
        for (String item : (ArrayList<String>) params.get(Dict.TARGET)) {
            message = new Message((String) params.get(Dict.CONTENT), (String) params.get(Dict.TYPE), item);
            message.setTarget(Dict.TUSER + "-" + message.getTarget());
            message.setLinkUri((String)params.get(Dict.LINKURI));
            try {
				messageService.addUserMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
            boolean result = WebSocketServer.sendMessage(message);
        }
        return JsonResultUtil.buildSuccess();
    }
    
    @PostMapping(value = "/queryMessageUser")
    public JsonResult queryMessageUser(@RequestBody Map<String, String> params) {
    	Message message = new Message();
        message.setTarget(Dict.TUSER + "-" + params.get(Dict.TARGET));
        message.setType(params.get(Dict.TYPE));
        List<Message> messages = null;
        try {
        	messages = messageService.queryMessageUser(message);
        } catch (Exception e) {
        }
        return JsonResultUtil.buildSuccess(messages);
    }

    /**
     * 查询消息类型列表
     * @param params
     * @return
     */
    @PostMapping(value = "/queryMessageTypeList")
    public JsonResult queryMessageTypeList(@RequestBody Map<String, String> params) {
        /**
         * 新增消息类型需在配置文件 messageType.properties添加
         */
        Set set = messageService.queryMessageTypeList();
        return JsonResultUtil.buildSuccess(set);
    }


    
    @PostMapping(value = "/updateNotifyStatus")
    public JsonResult updateNotifyStatus(@RequestBody Map<String, String> params) {
    	Integer  i= 0;
        try {
            i = messageService.updateNotifyStatus(params.get("id"));
        } catch (Exception e) {
        }
        if(i<1) {
    		throw new FtmsException("更新失败！");
    	}
		return JsonResultUtil.buildSuccess("true"); 
    }

    @PostMapping(value = "/batchUpdateNotiftStatus")
    public JsonResult batchUpdateNotiftStatus(@RequestBody Map<String, List<String>> params) throws Exception {
        List<String> ids = params.get(Dict.IDS);
        messageService.batchUpdateNotiftStatus(ids);
        return JsonResultUtil.buildSuccess("true");
    }

}
