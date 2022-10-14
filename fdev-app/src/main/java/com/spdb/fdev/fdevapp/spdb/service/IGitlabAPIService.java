package com.spdb.fdev.fdevapp.spdb.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * gitlabapi相关交易
 *
 * @author xxx
 */
public interface IGitlabAPIService {
    /**
     * 通过项目id查询项目信息
     *
     * @param projectid 项目id
     * @param token     AccessTokens验证用户
     * @return 项目信息json字符串
     * @throws Exception
     */
    Object queryProjectById(String projectid, String token) throws Exception;

    /**
     * 创建项目
     *
     * @param path  项目路径
     * @param name  项目名称
     * @param token AccessTokens验证用户
     * @return 创建的项目信息json字符串
     * @throws Exception
     */
    Object createProject(String path, String name, String token) throws Exception;

    /**
     * 查询用户所有项目
     *
     * @param userid 用户id
     * @param token  AccessTokens验证用户
     * @return 用户所有项目信息的json字符串
     * @throws Exception
     */
    Object quertProjectByUser(String userid, String token) throws Exception;

    /**
     * 查询项目所属成员的个人信息
     *
     * @param id    项目id
     * @param token AccessTokens验证用户
     * @return 所有成员的个人信息json字符串
     * @throws Exception
     */
    Object queryProjectMembers(String id, String token) throws Exception;

    /**
     * 添加项目webhook
     *
     * @param id           gitlab id
     * @param token        AccessTokens验证用户
     * @param mergeRequest 设置mergerRequest提醒 值为boolean
     * @param pipeline     设置pipeline提醒 值为boolean
     * @return 添加的webhook信息json字符串
     * @throws Exception
     */
    Object addProjectHook(String id, String token, String mergeRequest, String pipeline) throws Exception;

    /**
     * 给应用新增和录入已有应用时添加fwebhook模块 webhook
     *
     * @param id
     * @param token
     * @return
     * @throws Exception
     */
    Object addProjectHook(String id, String token) throws Exception;

    /**
     * 判断项目是否有webhook
     *
     * @param id
     * @param token
     * @return
     * @throws Exception
     */
    Boolean validateProjectHook(String id, String token) throws Exception;

    /**
     * 创建项目pipline
     *
     * @param id        项目id
     * @param ref       所属分支 exl：master
     * @param variables 所需参数[{ "key" : "CI_DEPLOY", "value" : "deploy" }]
     * @param token     AccessTokens验证用户
     * @return pipline相关信息
     * @throws Exception
     */
    Object createPipeline(String id, String ref, List<Map<String, String>> variables, String token) throws Exception;

    /**
     * 接收webhook返回信息
     *
     * @param req 接收webhook请求 获取其中的requestbody内容
     * @return 接收webhook请求 获取其中的requestbody内容
     * @throws Exception
     */
    String webHook(Map<String, Object> req) throws Exception;


    /**
     * 创建分支
     *
     * @param id    项目id
     * @param name  分支名
     * @param ref   父分支
     * @param token AccessToken验证用户
     * @return 分支信息
     * @throws Exception
     */
    Object createBranch(String id, String name, String ref, String token) throws Exception;

    /**
     * @param id      项目id
     * @param sbranch 将要合的分支名
     * @param tbranch 目标分支
     * @param title   填写信息
     * @param token   AccessToken验证用户
     * @return 合并信息
     * @throws Exception
     */
    Object createMergeRequest(String id, String sbranch, String tbranch, String title, String desc, String token) throws Exception;

    /**
     * 查询项目下的所有分支
     *
     * @param id    项目id
     * @param token 验证码
     * @return 所以分支名 list
     * @throws Exception
     */
    Object getProjectBranchList(String id, String token) throws Exception;

    /**
     * 查询单次mergerequest信息
     *
     * @param id    项目id
     * @param iid   mergerequest id
     * @param token 验证码
     * @return 单次mergerequest信息
     * @throws Exception
     */
    Object getMergeRequestInfo(String id, String iid, String token) throws Exception;

    /**
     * 通过应用id查询projectid
     *
     * @param appId 项目id
     * @return projectid
     * @throws Exception
     */
    String getProjectIDByApp(String appId) throws Exception;

    /**
     * 通过Pipelineid查询Pipeline信息
     *
     * @param id    项目id
     * @param pid   Pipelineid
     * @param token 验证码
     * @return Pipeline信息
     * @throws Exception
     */
    Object queryPipelineByPid(String id, String pid, String token) throws Exception;

    /**
     * 通过mergaRequestId查询Pipeline信息
     *
     * @param id    项目id
     * @param mid   mergaRequestId
     * @param token 验证码
     * @return Pipeline信息
     * @throws Exception
     */
    Map<String, Object> queryPipelineByMid(String id, String mid, String token) throws Exception;

    /**
     * 删除分支
     *
     * @param id     项目id
     * @param branch 分支名
     * @return
     * @throws Exception
     */
    Object deleteBranch(String id, String branch, String token) throws Exception;


    /**
     * 创建分支
     *
     * @param id    项目id
     * @param name  标签名
     * @param ref   分支名，或其他分支名
     * @param token AccessToken验证用户
     * @return 分支信息
     * @throws Exception
     */
    Object createTages(String id, String name, String ref, String token) throws Exception;

    /**
     * 查询项目下的所有tags
     *
     * @param id    项目id
     * @param token 验证码
     * @return 所以分支名 list
     * @throws Exception
     */
    List<Map<String, Object>> getProjectTagsList(String id, String token, String order_by) throws Exception;


    /**
     * release merge to master 向 投产发送请求
     *
     * @param state
     * @param iid
     * @param project_id
     * @throws Exception
     */
    void sendMergedCallBack(String state, Integer iid, Integer project_id) throws Exception;

    /**
     * 根据gitlab id 去获取gitlab项目的信息
     *
     * @param id
     * @return
     */
    Map getProjectInfo(String id, String token) throws Exception;

    /**
     * @param id    gitlab id
     * @param name  受保护分支的名字
     * @param token token
     * @return
     * @throws Exception
     */
    Object setProtectedBranches(String id, String name, String token) throws Exception;

    /**
     * 查询一个组 信息
     *
     * @param group        组名字 /ebank/devops /ebank/devops/
     * @param gitlab_token
     * @return
     */
    boolean getGroupInfo(String group, String gitlab_token) throws Exception;

    /**
     * 创建一个 组
     *
     * @param group        组名字 /ebank/devops /ebank/devops/
     * @param gitlab_token
     * @return
     */
    boolean createGroup(String group, String gitlab_token) throws Exception;

    /**
     * 查询gitlab 具体某个pipeline下jobs:
     * /projects/:id/pipelines/:pipeline_id/jobs
     */
    Object queryJobs(Map param) throws Exception;

    /**
     * 查询gitlab 具体某个pipeline
     * /projects/:id/pipelines
     */
    Object queryPipelines(Map param) throws Exception;

    /**
     * 查询gitlab 具体某个pipeline下jobs的日志:
     * /projects/:id/jobs/:job_id/trace
     */
    Object queryTraces(Map param) throws Exception;

    /**
     * 查询gitlab 所有pipeline下jobs的日志:
     */
    Object queryPipelinesWithJobs(Map param) throws Exception;

    /**
     * 查询gitlab 所有pipeline下jobs的日志:
     */
    Object queryPipelinesWithJobsPage(Map param) throws Exception;

    List<String> getAllBranch(String id, String token,  HashSet<String> onlySet) throws Exception;

    /**
     * 根据gitlabId删除在gitlab上的项目
     *
     * @param id gitlabId
     * @return
     * @throws Exception
     */
    Object deleteProjectById(String id) throws Exception;

    String getFileContent(String type_name, String id, String isInternetSystem) throws Exception;

    String getContent(String id, String file_path, String branchName) throws Exception;

    /**
     * 通过gitlab查询，查询最新一次commit记录
     *
     * @param gitlabId gitlab上的projectId
     * @param refName  分支名
     * @return commit相关的信息
     */
    Map getChannelCommitInfo(String gitlabId, String refName);

    /**
     * 通过gitlabId来查询project，获得当前应用的namespace
     *
     * @param gitlabId
     * @return
     */
    Map getChannelNamespace(String gitlabId);

    /**
     * 获取全量的组（gitlab）信息(public)
     *
     * @return
     */
    List<Map> getFullGroupGit();

}
