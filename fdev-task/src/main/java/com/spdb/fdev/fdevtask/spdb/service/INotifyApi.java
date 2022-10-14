package com.spdb.fdev.fdevtask.spdb.service;

import java.util.ArrayList;
import java.util.Map;

public interface INotifyApi {

    void sendMail(String content, String subject, String[] to) ;

    void sendWebsocketMsg(Map map) throws Exception;

    void sendUserNotify(String content, ArrayList<String>to, String type, String hyperlink, String desc)throws Exception;

    /**
     *
     * @param content 发送内容
     * @param type 通知类型
     * @param target 发送人
     * @param hyperLink 跳转链接
     * @param desc title
     */
    void sendFdevNotify(String content, String type, String[] target, String hyperLink, String desc);

    void deleteTodoList(String type,String taskId);
}
