package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.BaseImageIssue;
import com.spdb.fdev.component.entity.BaseImageRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IBaseImageIssueService {

    List query(BaseImageIssue baseImageIssue) throws Exception;

    BaseImageIssue save(BaseImageIssue baseImageIssue) throws Exception;

    BaseImageIssue update(BaseImageIssue baseImageIssue) throws Exception;

    BaseImageIssue queryById(String id);

    void packageTag(BaseImageRecord record) throws Exception;

    void changeStage(Map<String, String> map) throws Exception;

    void destroyBaseImageIssue(String id) throws Exception;

    List<HashMap<String, Object>>  queryQrmntsData(Map requestParam) throws Exception;

    List<Map>  queryIssueDelay(Map requestParam) throws Exception;
}
