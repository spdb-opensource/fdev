package com.fdev.database.spdb.service;

import java.util.*;

public interface SendEmailService {

    /**
     * 发送邮件提醒
     * @param model
     * @throws Exception
     */
    void sendEmail(HashMap model) throws Exception;

    /***
     * 文件上传完成发送通知提醒
     * @throws Exception
     */
    void sendUserNotify(HashMap model) throws Exception;
}
