package com.spdb.executor.service;


import java.util.HashMap;
import java.util.List;

public interface MailService {

    public void sendEmail(String confKey, HashMap model, List<String> to, List<String> filePaths) throws Exception;
}
