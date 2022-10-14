package com.spdb.fdev.pipeline.service;

import java.util.List;
import java.util.Map;

public interface IGitlabService {

    List<String> queryBranches(Integer gitlabId, String git_token) throws Exception;

    List<String> queryTags(Integer gitlabId, String git_token) throws Exception;

    String queryShortId(Integer gitlabId, String git_token,String branch) throws Exception;

    Map queryTypeAndContent(Integer gitlabId, String git_token,String path,String branch) throws Exception;
}
