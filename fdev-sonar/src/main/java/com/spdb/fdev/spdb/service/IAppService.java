package com.spdb.fdev.spdb.service;

import java.util.Map;

public interface IAppService {

    Map<String, Object> findById(String id) throws Exception;

    Map<String, Object> queryByGitId(Integer gitlab_project_id) throws Exception;

    Map<String, Object> queryByNameEn(String name_en) throws Exception;

}
