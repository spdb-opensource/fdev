package com.spdb.fdev.release.service;

import java.util.List;
import java.util.Map;

/**
 * 发送app模块的接口类
 */
public interface IAppService {
    /**
     * 创建分支
     *
     * @param id    appid
     * @param name  新建分支名
     * @param ref   源分支名
     * @param token gitlab token
     * @throws Exception
     */
    void createReleaseBranch(String id, String name, String ref, String token) throws Exception;

    /**
     * 获取应用下所以分支列表
     *
     * @param id    应用id
     * @param token gitlab token
     * @return 分支列表
     * @throws Exception
     */
    List<Map<String, Object>> getBranchList(String id, String token) throws Exception;

    /**
     * 通过应用id查询应用英文名和中文名
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryAPPbyid(String id) throws Exception;

    /**
     * 获MergeRequest信息
     *
     * @param token 验证码
     * @param id    项目id
     * @param iid   MergeRequestid
     * @return mergerequest信息
     * @throws Exception
     */
    String getMergeRequestInfo(String token, String id, String iid) throws Exception;

    /**
     * 发起目标为master的合并请求
     *
     * @param application_id 应用id
     * @param token          gitlabtoken
     * @param source_branch  所合分支
     * @throws Exception
     */
    String commitMergeRequest(String application_id, String token, String source_branch, String desc) throws Exception;

    /**
     * 拉取应用tag
     *
     * @param id    应用id
     * @param name  tag名
     * @param ref   源分支
     * @param token gitlabtoken
     * @return
     * @throws Exception
     */
    Map createTags(String id, String name, String ref, String token) throws Exception;

    /**
     * 获取项目tag列表
     *
     * @param id    应用id
     * @param token gitlabtoken
     * @return
     * @throws Exception
     */
    List<Map> getProjectTagsList(String id, String token) throws Exception;

    /**
     * 运行项目product_tag打包
     *
     * @param id        应用id
     * @param ref       tag名
     * @throws Exception
     */
    void createProjectPipeline(String id, String ref) throws Exception;


    /**
     * 创建应用
     *
     * @param path 路径
     * @param name 应用名
     * @return
     * @throws Exception
     */
    String createProject(String path, String name) throws Exception;

    /**
     * 查询文件last_commit+id
     *
     * @param url 文件url
     * @return
     * @throws Exception
     */
    String queryFileCommitId(String url) throws Exception;

    /**
     * 查询类型对应的环境名称
     *
     * @param type 环境类型
     * @return
     * @throws Exception
     */
    String queryByLabelsFuzzy(String type, String network) throws Exception;

    Map<String, Object> queryVariablesMapping(Integer gitlabId, String env)throws Exception;

    /**
     * 通过应用英文名查询应用id
     *
     * @param app_name_en 应用英文名
     * @return
     * @throws Exception
     */
    Map<String, Object> queryAPPbyNameEn(String app_name_en) throws Exception;

    /**
     * 设置应用的默认环境名称
     *
     * @param map
     * @return
     * @throws Exception
     */
    void updateApplication(Map map) throws Exception;

    String getGitFileContentById(String git_id, String filename, String ref) throws Exception;

    void updateGitFile(String git_id, String filename, String ref, String content, String commit_message, String token) throws Exception;

    /**
     * 根据应用ids查应用信息集合
     * @param appIds
     * @return
     * @throws Exception
     */
    List<Map<String, String>> getAppByIdsOrGitlabIds(List<String> appIds, String releaseNodeName) throws Exception;
    /**
     * 发蓝鲸获取上次投产的caas部属信息
     * @param deployName 应用名
     * @param tag
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> getNewYaml(String deployName, String tag) throws Exception;
    /**
     * 发蓝鲸获取上次投产的scc部属信息
     * @param deployName 应用名
     * @param tag
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> getSccNewYaml(String deployName, String tag) throws Exception;
    /**
     * 根据gitlabId或应用id,环境英文名，属性列表查询环境实体中属性对应的值
     * */
    Map<String,Object> getEnvValues(String gitlabId,String appId, String envName,List<String> keys) throws Exception;

    List<Map<String, Object>> getApps() throws Exception;
}