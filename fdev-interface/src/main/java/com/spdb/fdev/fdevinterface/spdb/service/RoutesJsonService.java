package com.spdb.fdev.fdevinterface.spdb.service;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/27 16:38
 */
public interface RoutesJsonService {

    String buildTestDatTar(Map<String, Object> requestMap);

    Map<String, String> buildProDatTar(Map<String, Object> requestMap);

    void exportFile(String fileName, HttpServletResponse response);
    
    String buildSccDatTar(Map<String, Object> requestMap);
    
}
