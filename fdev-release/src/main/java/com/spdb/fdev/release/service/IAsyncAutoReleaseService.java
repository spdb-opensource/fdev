package com.spdb.fdev.release.service;

import java.util.Map;

public interface IAsyncAutoReleaseService {
    /**
     * 自动化发布
     *
     * @param prod_id
     * @throws Exception
     */
    void autoRelease(String prod_id) throws Exception;

    /**
     * 缓存镜像
     *
     * @param app_name_en
     * @param pro_image_uri
     */
    void cacheImage(String app_name_en, String pro_image_uri) throws Exception;

    void pushDockerImage(Integer gitlab_project_id, String name_en, String pro_image_uri, String fake_image_uri,
                         String release_node_name, String version, String prod_id, String application_id, String logPath,
                         String logFileName, String type,String caasEnv,String sccEnv);

    /**
     * 自动化部署镜像
     * @param requestParam
     * @throws Exception
     */
    void deploy(Map<String,Object> requestParam) throws Exception;
}
