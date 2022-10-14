package com.spdb.fdev.fdevapp.spdb.service;

import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;

import java.util.List;
import java.util.Map;

public interface IAppEntityService {

    Map findById(String id) throws Exception;

    AppEntity update(Map map) throws Exception;

    List<Map> query(AppEntity appEntity) throws Exception;

    List<Map<String, String>> queryForSelect() throws Exception;

    /**
     * 根据中文名称模糊查询应用信息
     *
     * @param appnamezh
     * @return
     * @throws Exception
     */
    List<AppEntity> queryByAppNameZh(String appnamezh);

    /**
     * 根据英文名称模糊查询应用信息
     *
     * @param appnameen
     * @return
     * @throws Exception
     */
    List<AppEntity> queryByAppNameEn(String appnameen);


    /**
     * 根据英文信息精准查询应用信息
     *
     * @param appNameEn
     * @return
     */
    List<AppEntity> queryByAppName(String appNameEn);



    /**
     * 通过应用 id 分支名 自动false或者定时true状态 获取 环境的 env_name
     *
     * @param ci_project_id
     * @param ci_commit_ref_name
     * @param ci_schedule
     * @return
     */
    List<Map<String, String>> getSitSlug(Integer ci_project_id, String ci_commit_ref_name, Boolean ci_schedule) throws Exception;

    /**
     * 根据gitlab id 获取 应用信息
     *
     * @param id
     * @return
     */
    AppEntity getAppByGitlabId(Integer id);

    /**
     * 根据应用id 去获取分支名
     *
     * @param id
     * @return
     */
    List<String> getBranchNameByAppId(String id) throws Exception;


    /**
     * 录入已有的应用信息
     *
     * @param
     * @return
     */
    AppEntity add(Map map) throws Exception;


    /**
     * 查询小组对应的应用数量
     *
     * @param ids
     * @return
     * @throws Exception
     */
    Map<String, Map> queryAppNum(List<String> ids, boolean flag) throws Exception;

    /**
     * 异步新增应用
     *
     * @param
     * @return
     * @throws Exception
     * @author xxx
     */
    Map<String, Object> saveByAsync(Map map) throws Exception;

    /**
     * 自动部署开关，默认false
     *
     * @param requestParam
     * @return
     */
    String createPipelineSchedule(Map requestParam) throws Exception;

    /**
     * 查询新老持续集成
     *
     * @param id
     * @return
     * @throws Exception
     */
    String customDeplayment(String id) throws Exception;

    AppEntity deleteAppById(Map requestParam) throws Exception;

    List<Map> queryAbandonApp(AppEntity appEntity) throws Exception;

    void scheduleJob() throws Exception;

    /**
     * 根据应用id或gitlabId的list集合查询应用
     *
     * @param params
     * @return
     * @throws Exception
     */
    List<Map> getAppByIdsOrGitlabIds(Map params) throws Exception;

    List<AppEntity> queryAppByLabel(AppEntity appEntity) throws Exception;

    /**
     * 根据应用英文名获取应用所属系统
     *
     * @param name_en
     * @return
     * @throws Exception
     */
    List<String> getAppServiceSystem(String name_en) throws Exception;

    /**
     * 根据人员id获取当前人员负责的应用
     *
     * @param user_id
     * @return
     */
    List<Map> queryAppsByUserId(String user_id) throws Exception;

    List<AppEntity> queryApps(AppEntity appEntity) throws Exception;

    Map queryPagination(Map<String, Object> requestParam) throws Exception;

    AppEntity updateForEnv(AppEntity appEntity) throws Exception;

    /**
     * 返回标签不包括“不涉及环境部署” 的应用，返回应用id，英文名，中文名
     *
     * @return 应用id，英文名，中文名
     */
    List<AppEntity> getNoInvolveEnvApp();

    /**
     * 应用绑定所属系统
     *
     * @param systemId
     * @param appId
     */
    void bindAppSystem(String systemId, String appId) throws Exception;

    /**
     * 查询应用下的任务（除废弃、删除、归档、未挂载应用的任务）
     * @param appId
     * @return
     * @throws Exception
     */
    List<String> queryAppTasks(String appId) throws Exception;

    /**
     * 根据应用id来查询返回应用是否涉及内测
     *
     * @param appId
     * @return
     */
    Map<String, Object> getTestFlag(String appId) throws Exception;


    /**
     * 根据gitlab project id 来查询应用，并根据ciType来返回返回值
     *
     * @param projectId
     * @return
     */
    String getResultCodeForCiType(Integer projectId);


    /**
     * 查询我参与的应用基础信息
     * @return
     */
    Map<String,Object> queryDutyAppBaseInfo() throws Exception;

    Map  getAppByNameEns(Map requestParam) throws Exception;
    
    void autoDeploy() throws Exception;
}
