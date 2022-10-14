package com.spdb.fdev.fdevtask.spdb.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/12/30
 */
public interface IReleaseApproveService {

    void add(Map param) throws Exception;

    Map<String, Object> releaseApproveList(Map param) throws Exception;

    void pass(Map param) throws Exception;

    void refuse(Map param) throws Exception;

    void exportApproveList(Map<String,Object> param, HttpServletResponse resp) throws Exception;
}
