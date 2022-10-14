package com.fdev.notify.redis;
import com.alibaba.fastjson.JSONObject;
import com.fdev.notify.dict.Dict;
import com.fdev.notify.dict.ErrorConstants;
import com.fdev.notify.entity.Notify;
import com.fdev.notify.websocket.WebSocketServer;
import com.spdb.fdev.common.exception.FdevException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class ChannelListener implements MessageListener {

    /***
     * 消息处理器
     * @param message 消息
     * @param bytes channel
     */
    @Override
    public void onMessage(Message message, byte[] bytes) {
        String channel = new String(bytes);
        switch (channel) {
            case "sendNotify":
                Notify notify = JSONObject.parseObject(message.toString(),Notify.class);
                Boolean result = WebSocketServer.sendNotify(notify);
                // if(!result){
                //     throw new FdevException(ErrorConstants.WEBSOCKET_SEND_ERROR);
                // }
                break;
            case "sendMessage":
                com.fdev.notify.entity.Message messageEntify =JSONObject.parseObject(message.toString(), com.fdev.notify.entity.Message.class);
                WebSocketServer.sendMessage(messageEntify);
                break;
            case "sendAnnounceRealTime":
                com.fdev.notify.entity.Message announce =JSONObject.parseObject(message.toString(), com.fdev.notify.entity.Message.class);
                WebSocketServer.sendAnnounceRealTime(announce);
                break;
        }

    }
}