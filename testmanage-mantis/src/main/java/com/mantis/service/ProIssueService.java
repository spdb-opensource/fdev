package com.mantis.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.mantis.entity.ProIssue;

public interface ProIssueService {

    List queryUserProIssues(String user_name_en) throws Exception;

    List queryTaskProIssues(String task_id) throws Exception;

    List queryProIssues(String current_page, String page_size, String start_time, String end_time, List<String> module,
                        String responsible_type, String responsible_name_en, String deal_status, String issue_level,
                        List<String> problem_type, Boolean isIncludeChildren ,String reviewerStatus ,String sortParam, String sortord) throws Exception;

    String countProIssues(String start_time, String end_time, List<String> module,
                          String responsible_type, String responsible_name_en, String deal_status, String issue_level, List<String> problem_type, Boolean isIncludeChildren ,String reviewerStatus) throws Exception;

    void exportProIssues(String start_time, String end_time, List<String> module,
                         String responsible_type, String responsible_name_en, String deal_status, String issue_level, List<String> problem_type, Boolean isIncludeChildren ,String reviewerStatus, List<String> ids,String sortParam, String sortord, HttpServletResponse resp) throws Exception;

    ProIssue queryProIssueDetail(String id) throws Exception;

    Map queryProIssueById(String id) throws Exception;

    void updateProIssue(Map<String, Object> map) throws Exception;

    void addFile(Map<String, Object> map) throws Exception;

    void deleteFile(Map<String, String> map) throws Exception;

    List<Map<String, Object>> queryIssueFiles(String id) throws Exception;

    void fileDownload(String file_id, HttpServletResponse resp) throws Exception;

    void delete(Map<String, String> map) throws Exception;

    Map<String, Object> queryProByTeam(Map map) throws Exception;

    void addProIssueReleaseNode() throws Exception;

    List releaseNodeProIssueRate(String startDate, String endDate) throws  Exception;

    List releaseNodeProIssueType(String startDate, String endDate) throws Exception;
}
