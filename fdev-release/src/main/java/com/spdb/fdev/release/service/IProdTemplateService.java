package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ProdTemplate;

import java.util.List;

public interface IProdTemplateService {
	/**
	 * 创建变更模版
	 * @param prodTemplate
	 * @return
	 * @throws Exception
	 */
	ProdTemplate create(ProdTemplate prodTemplate) throws Exception;
	/**
	 * 查询变更模版列表
	 * @param prodTemplate
	 * @return
	 * @throws Exception
	 */
	List<ProdTemplate> query(ProdTemplate prodTemplate) throws Exception;
	/**
	 * 查询模版详情
	 * @param prodTemplate 模版id
	 * @return
	 * @throws Exception
	 */
	ProdTemplate queryDetail(ProdTemplate prodTemplate) throws Exception;
	/**
	 * 修改变更模版
	 * @param prodTemplate
	 * @return
	 * @throws Exception
	 */
	ProdTemplate update(ProdTemplate prodTemplate) throws Exception;
	/**
	 * 删除模版
	 * @param tempId 模版id
	 * @return
	 * @throws Exception
	 */
	boolean delete(String tempId) throws Exception;
	
	/**
	 * 将模版状态置为已废弃
	 * @param template_id
	 * @throws Exception
	 */
	void cancel(String template_id) throws Exception;

    ProdTemplate queryDetailById(String template_id);

	List<ProdTemplate> findExists(ProdTemplate prodTemplate);

	List<ProdTemplate> queryByGroupType(ProdTemplate prodTemplate);

	List<ProdTemplate> queryList(String owner_group, String owner_system, String type);
}
