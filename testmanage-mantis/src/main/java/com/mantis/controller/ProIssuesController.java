package com.mantis.controller;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.mantis.dict.ErrorConstants;
import com.test.testmanagecommon.exception.FtmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mantis.dict.Dict;
import com.mantis.entity.ProIssue;
import com.mantis.service.ProIssueService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

@RestController
@RequestMapping("/proIssue")
public class ProIssuesController {

    @Autowired
    private ProIssueService proIssueService;

    @RequestValidate(NotEmptyFields = {Dict.USER_NAME_EN})
    @PostMapping(value = "/queryUserProIssues")
    public JsonResult queryUserProIssues(@RequestBody Map<String, String> map) throws Exception {
        String user_name_en = map.get(Dict.USER_NAME_EN);
        List list = proIssueService.queryUserProIssues(user_name_en);
        return JsonResultUtil.buildSuccess(list);
    }

    @RequestValidate(NotEmptyFields = {Dict.TASK_ID})
    @PostMapping(value = "/queryTaskProIssues")
    public JsonResult queryTaskProIssues(@RequestBody Map<String, String> map) throws Exception {
        String task_id = map.get(Dict.TASK_ID);
        List list = proIssueService.queryTaskProIssues(task_id);
        return JsonResultUtil.buildSuccess(list);
    }

    /**
     * 请求生产缺陷
     * @param map
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.CURRENT_PAGE, Dict.PAGE_SIZE})
    @PostMapping(value = "/queryProIssues")
    public JsonResult queryProIssues(@RequestBody Map<String, Object> map) throws Exception {
        String current_page = String.valueOf(map.get(Dict.CURRENT_PAGE));
        String page_size = String.valueOf(map.get(Dict.PAGE_SIZE));
        String start_time = (String) map.get(Dict.START_TIME);
        String end_time = (String) map.get(Dict.END_TIME);
        List<String> module = (List<String>) map.get(Dict.MODULE);//fdev组id的列表
        List<String> problem_type = (List<String>) map.get(Dict.PROBLEMTYPE);//问题类型
        String responsible_type = (String) map.get(Dict.RESPONSIBLE_TYPE);//问责类型
        String responsible_name_en = (String) map.get(Dict.RESPONSIBLE_NAME_EN);//问责人英文
        String deal_status = (String) map.get(Dict.DEAL_STATUS);//处理状态
        String issue_level = (String) map.get(Dict.ISSUE_LEVEL);//生产问题级别
        Boolean isIncludeChildren = (Boolean) map.get(Dict.ISINCLUDECHILDREN);//是否包含子组
        String reviewerStatus = (String)map.get(Dict.REVIEWER_STATUS);
        String sortParam = (String)map.get(Dict.SORTPARAM);      //需求编号
        String sortord = (String)map.get(Dict.SORTORD);   //应用英文名
        List list = proIssueService.queryProIssues(current_page, page_size, start_time,
                end_time, module, responsible_type, responsible_name_en, deal_status,
                issue_level, problem_type, isIncludeChildren, reviewerStatus, sortParam, sortord);
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping(value = "/countProIssues")
    public JsonResult countProIssues(@RequestBody Map<String, Object> map) throws Exception {
        String start_time = (String) map.get(Dict.START_TIME);
        String end_time = (String) map.get(Dict.END_TIME);
        List<String> module = (List<String>) map.get(Dict.MODULE);
        String responsible_type = (String) map.get(Dict.RESPONSIBLE_TYPE);
        String responsible_name_en = (String) map.get(Dict.RESPONSIBLE_NAME_EN);
        String deal_status = (String) map.get(Dict.DEAL_STATUS);
        String issue_level = (String) map.get(Dict.ISSUE_LEVEL);
        List<String> problem_type = (List<String>) map.get(Dict.PROBLEMTYPE);
        Boolean isIncludeChildren = (Boolean) map.get(Dict.ISINCLUDECHILDREN);
        String reviewerStatus = (String)map.get(Dict.REVIEWER_STATUS);
        String count = proIssueService.countProIssues(start_time,
                end_time, module, responsible_type, responsible_name_en, deal_status, issue_level, problem_type, isIncludeChildren, reviewerStatus);
        return JsonResultUtil.buildSuccess(count);
    }


    @PostMapping(value = "/exportProIssues")
    public void exportProIssues(@RequestBody Map<String, Object> map,HttpServletResponse resp) throws Exception {
        String start_time = (String) map.get(Dict.START_TIME);
        String end_time = (String) map.get(Dict.END_TIME);
        List<String> module = (List<String>) map.get(Dict.MODULE);
        String responsible_type = (String) map.get(Dict.RESPONSIBLE_TYPE);
        String responsible_name_en = (String) map.get(Dict.RESPONSIBLE_NAME_EN);
        String deal_status = (String) map.get(Dict.DEAL_STATUS);
        String issue_level = (String) map.get(Dict.ISSUE_LEVEL);
        List<String> problem_type = (List<String>) map.get(Dict.PROBLEMTYPE);
        Boolean isIncludeChildren = (Boolean) map.get(Dict.ISINCLUDECHILDREN);
        String reviewerStatus = (String)map.get(Dict.REVIEWER_STATUS);
        List<String> ids = (List<String>) map.get(Dict.IDS);
        String sortParam = (String)map.get(Dict.SORTPARAM);      //需求编号
        String sortord = (String)map.get(Dict.SORTORD);   //应用英文名
        proIssueService.exportProIssues(start_time, end_time, module,
                responsible_type, responsible_name_en, deal_status, issue_level,
                problem_type, isIncludeChildren ,reviewerStatus, ids, sortParam, sortord,resp);
    }

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/queryProIssueDetail")
    public JsonResult queryProIssueDetail(@RequestBody Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);
        ProIssue proIssue = proIssueService.queryProIssueDetail(id);
        return JsonResultUtil.buildSuccess(proIssue);
    }


    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/queryProIssueById")
    public JsonResult queryProIssueById(@RequestBody Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);
        Map proIssue = proIssueService.queryProIssueById(id);
        return JsonResultUtil.buildSuccess(proIssue);
    }

    @RequestValidate(NotEmptyFields = {Dict.REQUIREMENT_NAME, Dict.MODULE, Dict.OCCURRED_TIME
            , Dict.IS_TRIGGER_ISSUE, Dict.PROBLEM_PHENOMENON, Dict.INFLUENCE_AREA, Dict.ISSUE_REASON
            , Dict.ISSUE_TYPE, Dict.DISCOVER_STAGE, Dict.IS_UAT_REPLICATION, Dict.IS_REL_REPLICATION
            , Dict.IS_GRAY_REPLICATION, Dict.IS_INVOLVE_URGENCY, Dict.IMPROVEMENT_MEASURES, Dict.ISSUE_LEVEL
            , Dict.REVIEWER_STATUS, Dict.DEAL_STATUS, Dict.ORFANIZER})
    @PostMapping(value = "/updateProIssue")
    public JsonResult updateProIssue(@RequestBody Map<String, Object> map) throws Exception {
        proIssueService.updateProIssue(map);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/delete")
    public JsonResult delete(@RequestBody Map<String, String> map) throws Exception {
        proIssueService.delete(map);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/addFile")
    public JsonResult addFile(@RequestBody Map<String, Object> map) throws Exception {
        proIssueService.addFile(map);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/deleteFile")
    public JsonResult deleteFile(@RequestBody Map<String, String> map) throws Exception {
        proIssueService.deleteFile(map);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/queryIssueFiles")
    public JsonResult queryIssueFiles(@RequestBody Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);
        List<Map<String, Object>> list = proIssueService.queryIssueFiles(id);
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping(value = "/fileDownload")
    public void fileDownload(@RequestBody Map<String, String> map, HttpServletResponse resp) throws Exception {
        String file_id = map.get(Dict.ID);
        try {
            proIssueService.fileDownload(file_id, resp);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.FILE_DOWN_LOAD_ERROR);
        }
    }

    @PostMapping(value = "/queryProByTeam")
    public JsonResult queryProByTeam(@RequestBody Map<String, Object> map) throws Exception {
        return JsonResultUtil.buildSuccess(proIssueService.queryProByTeam(map));
    }


    @PostMapping(value = "/addProIssueReleaseNode")
    public JsonResult addProIssueReleaseNode(@RequestBody Map<String, Object> map) throws Exception {
        //补充存量数据投产窗口
        proIssueService.addProIssueReleaseNode();
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping(value = "/releaseNodeProIssueRate")
    public JsonResult releaseNodeProIssueRate(@RequestBody Map<String, String> map) throws Exception {
        String startDate = map.get(Dict.STARTDATE);
        String endDate = map.get(Dict.ENDDATE);
        return JsonResultUtil.buildSuccess(proIssueService.releaseNodeProIssueRate(startDate, endDate));
    }

    @PostMapping(value = "/releaseNodeProIssueType")
    public JsonResult releaseNodeProIssueType(@RequestBody Map<String, String> map) throws Exception {
        String startDate = map.get(Dict.STARTDATE);
        String endDate = map.get(Dict.ENDDATE);
        List list = proIssueService.releaseNodeProIssueType(startDate, endDate);
        return JsonResultUtil.buildSuccess(list);
    }


}
