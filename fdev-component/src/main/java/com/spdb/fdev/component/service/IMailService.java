package com.spdb.fdev.component.service;

import java.util.HashMap;
import java.util.Map;

public interface IMailService {

    void sendEmail(String subject, String templateName, HashMap<String, String> model, String... to) throws Exception;

    String getMailContent(HashMap hashMap) throws Exception;

    String mpassMailDestroyContent(HashMap hashMap) throws Exception;

    String archetypeNotifyContent(HashMap hashMap) throws Exception;

    String mpassMailDevNotifyContent(HashMap hashMap) throws Exception;
}
