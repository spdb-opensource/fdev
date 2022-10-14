package com.spdb.fdev.spdb.service;

import com.spdb.fdev.spdb.entity.Entity;

import java.util.List;
import java.util.Map;

public interface IEntityService {

	/**
	 * 查询实体列表
	 * @param 
	 * @return
	 * @throws Exception
	 */
	Map queryEntityModel(Map<String, Object> requestParam) throws Exception;

	/**
	 * 校验实体是否重复
	 * @param 
	 * @return
	 * @throws Exception
	 */
	Map<String, Boolean> checkEntityModel(Map<String, Object> requestParam) throws Exception ;

	/**
	 * 新建实体
	 * @param 
	 * @return
	 * @throws Exception
	 */
	Map<String,String> addEntityModel(Map<String, Object> requestParam) throws Exception;

	/**
	 * 编辑实体
	 * @param 
	 * @return
	 * @throws Exception
	 */
	Map<String,String> updateEntityModel(Map<String, Object> requestParam)throws Exception ;

	/**
	 * 查询实体详情
	 * @param 
	 * @return
	 * @throws Exception
	 */
	Entity queryEntityModelDetail(String id) throws Exception;
	
	/**
     * 根据环境英文名和实体字段集合查询实体环境映射值
     *
     * @param envName
     * @param variablesKey
     * @return
     * @throws Exception
     */
	Map<String, Object> getVariablesValue(List<String> envNames, List<String> variablesKey) throws Exception ;

	/**
     * 新增实体映射
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
	String addEntityClass(Map<String, Object> requestParam) throws Exception ;
	
	/**
     * 编辑实体映射
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
	String updateEntityClass(Map<String, Object> requestParam) throws Exception ;

	/**
     * 删除实体映射
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
	String deleteEntityClass(Map<String, Object> requestParam) throws Exception ;

	List<Entity> queryEntityMapping(List<String> entityIdList, String envNameEn)throws Exception ;

	String queryConfigTemplate(Map<String, String> requestParam) throws Exception;

	Map queryServiceEntity(Map<String, Object> requestParam) throws Exception;
	
	/**
     * 配置模板预览
     *
     * @param requestParam
     * @return
     */
    Map<String, Object> previewConfigFile(Map<String, Object> requestParam) throws Exception;
    
    /**
     * 配置文件上传开发配置中心
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    String saveDevConfigProperties(Map<String, Object> requestParam) throws Exception;

    /**
	 * 查询条线下所有实体列表
	 * @param 
	 * @return
	 * @throws Exception
	 */
	Map querySectionEntity(Map<String, Object> requestParam) throws Exception;
	
    /**
     * 查看实体映射值操作日志信息
     *
     * @param requestParam
     */
    Map<String, Object> getMappingHistoryList(Map<String, Object> requestParam) throws Exception;

	void deleteEntity(Map<String, Object> requestParam) throws Exception;

	/**
	 * 查看实体操作日志记录
	 *
	 * @param requestParam
	 */
	Map<String, Object> queryEntityLog(Map<String, Object> requestParam) throws Exception;
	
	Map<String,String> copyEntity(Map<String, Object> requestParam) throws Exception;

	void deleteConfigDependency(Map<String, Object> requestParam) throws Exception;
}
