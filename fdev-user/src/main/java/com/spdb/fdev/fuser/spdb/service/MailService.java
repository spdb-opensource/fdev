package com.spdb.fdev.fuser.spdb.service;


import java.util.HashMap;

public interface MailService {

    public void sendEmail(String subject, String templateName, HashMap<String, String> model, String... to) throws Exception;
}
