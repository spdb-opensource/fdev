package com.spdb.fdev.codeReview.spdb.service;

import com.spdb.fdev.codeReview.spdb.entity.CodeProblem;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
public interface IProblemService {

    Map<String, Object> queryProblems(String orderId);

    void deleteProblemById(String id);

    void add(CodeProblem problem) throws Exception;

    void update(CodeProblem problem) throws Exception;

    void exportProblemExcel(Map param, HttpServletResponse resp) throws Exception;

    Map queryAll(Map param) throws Exception;

    void exportAllProblemExcel(Map param, HttpServletResponse resp) throws Exception;
}
