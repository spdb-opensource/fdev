package com.spdb.fdev.release.service;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.release.entity.NoteManual;
import com.spdb.fdev.release.entity.NoteService;

public interface IProdNoteService {

	/**
	 * 新建发布说明记录
	 * @return
	 * @throws Exception
	 */
	void createNote(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 查询发布说明记录
	 * @param release_node_name 投产窗口名
	 * @return
	 * @throws Exception
	 */
	List<Map<String, Object>> queryReleaseNote(String release_node_name) throws Exception;
	
	/**
	 * 查询发布说明模版详情
	 * @param Note_id 发布说明id
	 * @return
	 * @throws Exception
	 */
	Map queryNoteDetail(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 删除发布说明记录
	 * @return
	 * @throws Exception
	 */
	void deleteNote(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 添加发布说明应用
	 * @return
	 * @throws Exception
	 */
	void addNoteSrevice(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 查询发布说明应用
	 * @return
	 * @throws Exception
	 */
	List<NoteService> queryNoteSrevice(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 删除发布说明应用
	 * @return
	 * @throws Exception
	 */
	void deleteNoteSrevice(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 修改发布说明应用
	 * @return
	 * @throws Exception
	 */
	void updateNoteSrevice(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 添加发布说明配置文件
	 * @return
	 * @throws Exception
	 */
	void addNoteConfiguration(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 查询发布说明配置文件
	 * @return
	 * @throws Exception
	 */
	List<Map<String, Object>> queryNoteConfiguration(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 删除发布说明配置文件
	 * @return
	 * @throws Exception
	 */
	void deleteNoteConfiguration(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 修改发布说明配置文件
	 * @return
	 * @throws Exception
	 */
	void updateNoteConfiguration(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 添加发布说明数据库文件
	 * @return
	 * @throws Exception
	 */
	void addNoteSql(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 查询发布说明数据库文件
	 * @return
	 * @throws Exception
	 */
	List queryNoteSql(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 删除发布说明数据库文件
	 * @return
	 * @throws Exception
	 */
	void deleteNoteSql(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 修改发布说明数据库文件
	 * @return
	 * @throws Exception
	 */
	void updateNoteSql(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 锁定发布说明
	 * @return
	 * @throws Exception
	 */
	void lockNote(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 更新数据库文件序号
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> updateNoteSeqNo(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 生成发布说明
	 * @return
	 * @throws Exception
	 */
	void generateReleaseNotes(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 新建手动发布说明记录
	 * @return
	 * @throws Exception
	 */
	void createManualNote(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 添加手动发布说明信息
	 * @return
	 * @throws Exception
	 */
	void addManualNoteInfo(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 查询手动发布说明信息
	 * @return
	 * @throws Exception
	 */
	NoteManual queryManualNoteInfo(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 添加发布说明配置文件
	 * @return
	 * @throws Exception
	 */
	void updateManualNoteInfo(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 修改发布说明投产窗口
	 * @return
	 * @throws Exception
	 */
	void updateProdNoteReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
	
	/**
	 * 修改应用投产窗口
	 * @return
	 * @throws Exception
	 */
	void updateNoteServiceReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
	
	/**
	 * 修改配置文件投产窗口
	 * @return
	 * @throws Exception
	 */
	void updateNoteConfigurationReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
	
	/**
	 * 修改数据库文件投产窗口
	 * @return
	 * @throws Exception
	 */
	void updateNoteSqlReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
	
	/**
	 * 修改批量任务投产窗口
	 * @return
	 * @throws Exception
	 */
	void updateNoteBatchReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
}