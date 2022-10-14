package com.spdb.fdev.fdevenvconfig.spdb.service;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface ModelEnvUpdateApplyService {

    void save(Map<String, Object> requestMap) throws Exception;

    void finish(Map<String, Object> requestMap) throws Exception;

    void update(Map<String, Object> requestMap);

    Map<String, Object> listModelEnvUpdateApplysByPage(Map<String, Object> requestMap);

    Map<String, Object> compare(Map<String, Object> requestMap);

    /**
     * 超过约定时间未核对的申请，改为超期overtime
     */
    void updateStatus(String dateTime);

    void cancel(Map<String, Object> requestMap);

    String downloadAppInfo(Map<String, Object> requestMap, HttpServletResponse response);

    void exportFile(String fileName, HttpServletResponse response) throws Exception;

    String checkConnectionDocker(Map requestMap);
}
