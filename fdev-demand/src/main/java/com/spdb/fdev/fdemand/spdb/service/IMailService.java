package com.spdb.fdev.fdemand.spdb.service;

import java.util.HashMap;
import java.util.List;

public interface IMailService {

    void sendEmail(String subject, String templateName, HashMap<String, String> model,String... to) throws Exception;

    String getDeferMailContent(HashMap hashMap) throws Exception;

    String getRecoverMailContent(HashMap hashMap) throws Exception;
    
    String getInfoMailContent(HashMap hashMap) throws Exception;
    
    String getUpdateMailContent(HashMap hashMap) throws Exception;
    
    String getUiUploadedMailContent(HashMap hashMap) throws Exception;
    
    String getUiAuditWaitMailContent(HashMap hashMap) throws Exception;
    
    String getUiAuditPassMailContent(HashMap hashMap) throws Exception;
    
    String getUiAuditPassNotMailContent(HashMap hashMap) throws Exception;

    void sendEmail(String subject, String templateName, HashMap<String, Object> model, List<String> to, List<String> filePaths) throws Exception;

    void sendEmail(String subject, String templateName, HashMap<String, Object> model, List<String> to,List<String> cc,  List<String> filePaths) throws Exception ;

    String getOverDueMsg(String template,HashMap hashMap) throws Exception;

    void sendEmailPath(String subject, String templateName, HashMap<String, Object> model, List<String> to, List<String> filePaths) throws Exception;
}
