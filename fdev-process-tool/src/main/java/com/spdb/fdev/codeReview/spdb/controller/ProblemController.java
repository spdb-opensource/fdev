package com.spdb.fdev.codeReview.spdb.controller;

import com.spdb.fdev.codeReview.base.annotation.nonull.NoNull;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.codeReview.spdb.entity.CodeProblem;
import com.spdb.fdev.codeReview.spdb.service.IProblemService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@CrossOrigin
@RestController
@RequestMapping("/api/problem")
public class ProblemController {

    @Autowired
    IProblemService problemService;

    //查询工单下的问题
    @PostMapping("/queryProblems")
    @NoNull(require = {Dict.ID},parameter = CodeOrder.class)
    public JsonResult queryProblems(@RequestBody CodeOrder codeOrder){
        return JsonResultUtil.buildSuccess(problemService.queryProblems(codeOrder.getId()));
    }

    @PostMapping("/deleteProblemById")
    @NoNull(require = {Dict.ID},parameter = CodeProblem.class)
    public JsonResult deleteProblemById(@RequestBody CodeProblem problem){
        String id = problem.getId();
        problemService.deleteProblemById(id);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/add")
    @NoNull(require = {Dict.MEETINGID,Dict.PROBLEM,Dict.PROBLEMTYPE,Dict.PROBLEMTNUM}, parameter = CodeProblem.class)
    public JsonResult add(@RequestBody CodeProblem problem) throws Exception {
        problemService.add(problem);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.ID,Dict.PROBLEM,Dict.PROBLEMTYPE,Dict.PROBLEMTNUM}, parameter = CodeProblem.class)
    public JsonResult update(@RequestBody CodeProblem problem) throws Exception {
        problemService.update(problem);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/exportProblemExcel")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult exportProblemExcel(@RequestBody Map param, HttpServletResponse resp) throws Exception {
        problemService.exportProblemExcel(param,resp);
        return JsonResultUtil.buildSuccess();
    }

    //查询所有问题
    @PostMapping("/queryAll")
    public JsonResult queryAll(@RequestBody Map param) throws Exception {
        return JsonResultUtil.buildSuccess(problemService.queryAll(param));
    }

    //问题列表上的导出，针对所有工单的问题导出
    @PostMapping("/exportAll")
    public JsonResult exportAllProblemExcel(@RequestBody Map param, HttpServletResponse resp) throws Exception {
        problemService.exportAllProblemExcel(param,resp);
        return JsonResultUtil.buildSuccess();
    }
}
