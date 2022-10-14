package com.spdb.fdev.fdevenvconfig.spdb.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IMailService {

    void sendEmail(String subject, String templateName, HashMap<String, Object> model, List<String> to) throws Exception;

    Map<String, Object> valdateKeys(List<Map> dev_managers, List<Map> spdb_managers);

}
