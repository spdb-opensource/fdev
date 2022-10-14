package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.spdb.service.INotifyApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RefreshScope
public class INotifyApiImpl implements INotifyApi {


    @Autowired
    private RestTransport restTransport;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void sendMail(String content, String subject, String[] to) {
        Map param = new HashMap<>();
        param.put(Dict.CONTENT, content);
        param.put("subject", subject);
        param.put("to", to);
        try {
            param.put(Dict.REST_CODE, "sendEmail");
            restTransport.submit(param);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送邮件失败" + e.getMessage());
        }
    }

    @Override
    public void sendWebsocketMsg(Map map) throws Exception {
        Map param = new HashMap();
        String pipe = (String) map.remove(Dict.NAME);
        param.put("target", pipe);
        param.put(Dict.CONTENT, JSONObject.toJSONString(map));
        param.put("action", "add bulk");
        param.put(Dict.MODULE, "ftask");
        param.put(Dict.REST_CODE, "sendStageData");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.WEBSOCKET_SEND_ERROR, new String[]{});
        }
    }

    @Override
    public void sendUserNotify(String content, ArrayList<String> to, String type, String hyperlink, String desc) throws Exception{
        Map param = new HashMap();
        param.put(Dict.CONTENT, content);
        param.put("target", to);
        param.put(Dict.TYPE, type);
        param.put("hyperlink", hyperlink);
        param.put(Dict.REST_CODE, "sendUserNotify");
        param.put(Dict.DESC, desc);
        restTransport.submit(param);
    }
    /**
     * content：内容
     * type: 现存类型：versionUpdate,announce-halt,announce-update,process,version-refresh
     * target:用户英文名数组
     * hyperLink：链接
     */
	@Override
	public void sendFdevNotify(String content, String type, String[] target, String hyperLink, String desc) {
		Map param = new HashMap();
		param.put(Dict.CONTENT, content);
		param.put("target", target);
		param.put(Dict.TYPE, type);
		param.put("hyperlink", hyperLink);
		param.put(Dict.REST_CODE, "sendUserNotify");
		param.put(Dict.DESC, desc);
        try {
            restTransport.submit(param);
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error("用户通知发送失败！" + e.getMessage());
        }
	}

    /**
     * 删除代办
     * @param type
     * @param taskId
     */
    @Override
    public void deleteTodoList(String type,String taskId) {
        Map param = new HashMap();
        param.put("type",type);
        param.put("target_id",taskId);
        param.put("module","task");
        param.put(Dict.REST_CODE, "deleteCommissionEvent");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除失败！" + e.getMessage());
        }
    }

}
