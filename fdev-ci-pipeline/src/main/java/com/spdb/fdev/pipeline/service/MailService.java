package com.spdb.fdev.pipeline.service;


import java.util.HashMap;

public interface MailService {

    public void sendEmail(String subject, String templateName, HashMap<String, Object> model, String... to) throws Exception;
}
