package com.spdb.fdev.release.service;

import java.util.Map;

/**
 * 推送镜像
 */
public interface IPushImageService {

    Object pushImage(Map<String, Object> requestParam) throws Exception;
    
    Map<String, Object> findConfigByGitlab(String gitlabId) throws Exception;
    
    void queryProfile(Map<String, Object> requestParam) throws Exception;
}
