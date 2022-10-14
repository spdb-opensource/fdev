package com.spdb.fdev.fdevapp.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface IAppEntityDao {
    List<AppEntity> query(AppEntity appEntity) throws Exception;

    AppEntity update(AppEntity appEntity) throws Exception;

    AppEntity findById(String id) throws Exception;

    AppEntity save(AppEntity appEntity) throws Exception;

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

    AppEntity getAppByGitlabId(Integer id);

    /**
     * 根据起始日期查询应用数
     *
     * @param groupid
     * @param startDate
     * @param endDate
     * @return
     */
    int queryAppNum(String groupid, String startDate, String endDate);

    int queryAppNum(List groupList, String startDate, String endDate);

    List<AppEntity> queryAbandonApp(AppEntity appEntity) throws Exception;

    List<AppEntity> queryByAppSchedule() throws Exception;

    List<AppEntity> queryAppByLabel(HashSet<String> label) throws Exception;

    public Map queryduty(Integer start, Integer pageSize, String userId, String name, String status);
    /**
     * 根据应用id或gitlabId的list集合查询应用
     *
     * @param params
     * @param type
     * @return
     * @throws Exception
     */
    List<AppEntity> getAppByIdsOrGitlabIds(List params, String type) throws Exception;

    List<AppEntity> queryAppsByUserId(String user_id);

    List<AppEntity> queryPagination(Integer start, Integer pageSize, String keyword, String status, List<String> group, String typeId, List<String> label, String userId, String system) throws Exception;

    Long findAppListCount(String keyword, String status, List<String> group, String typeId, List<String> label, String userId, String system) throws Exception;

    AppEntity updateForEnv(AppEntity appEntity) throws Exception;

    /**
     * 查询只涉及环境部署应用的接口
     *
     * @return
     */
    List<AppEntity> queryNoInvolveEnvApp();

    /**
     * 更新应用所属系统字段
     *
     * @param systemId
     * @param appId
     * @return
     */
    void updateSystem(String systemId, String appId);

    List<AppEntity> getAppByNameEns(List params);
    
	List<AppEntity> queryByGroupId(String groupId)throws Exception;
}
