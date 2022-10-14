package com.spdb.fdev.fdevapp.spdb.service;

import java.util.List;
import java.util.Map;

public interface IGitlabRepositoriesService {
    /**
     * 查询项目文件树
     *
     * @param id    项目id
     * @param token 验证码
     * @return 文件数
     * @throws Exception
     */
    Object queryRepositoriesTree(String id, String token) throws Exception;

    /**
     * 校验项目是否存在或者用户有权限
     *
     * @param id    项目id
     * @param token 验证码
     * @return 当项目存在时返回true
     */
    boolean projectVaildation(String id, String token) throws Exception;

    /**
     * 删除原有的master分支，拉取新的分支并改名没master
     *
     * @param id
     * @param token
     * @throws Exception
     */
    void changeMasterBranch(String id, String token, String branch) throws Exception;

    /**
     * 修改项目默认
     *
     * @param id
     * @param token
     * @param branch
     * @throws Exception
     */
    void changeDefaultBranch(String id, String token, String branch) throws Exception;

    /**
     * 查询项目下所以MR的信息
     *
     * @param id    项目id
     * @param token 验证码
     * @param date  日期，表示在该时间之后创建
     * @return MRlist
     * @throws Exception
     */
    Object queryMergeRequestList(String id, String token, String date) throws Exception;

    /**
     * 查询对应任务下文件列表
     *
     * @param path 任务对应文件夹
     * @return 任务对应文件列表
     * @throws Exception
     */
    List<Map<String, String>> queryTaskFileList(String path) throws Exception;

    /**
     * 判断该应用在同一group下是否有相同项目
     *
     * @param namespaceId  项目路径
     * @param name  项目名称
     * @param token gitlab token
     * @return
     * @throws Exception
     */
    boolean isProjectExist(Integer namespaceId, String name, String token) throws Exception;

}
