package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.BaseImageIssue;

import java.util.List;
import java.util.Map;

public interface IBaseImageIssueDao {
    List query(BaseImageIssue baseImageIssue) throws Exception;

    BaseImageIssue save(BaseImageIssue baseImageIssue) throws Exception;

    BaseImageIssue update(BaseImageIssue baseImageIssue) throws Exception;

    BaseImageIssue queryById(String id);

    BaseImageIssue queryByNameAndBranch(String name, String branch);

    void delete(BaseImageIssue baseImageIssue) throws Exception;

    List<Map>  queryQrmntsData(String userId);

    List<Map> queryIssueDelay(Map params);
}
