package com.testmanage.admin.service;


import java.util.List;
import java.util.Map;

import com.testmanage.admin.entity.FunctionMenu;

public interface IFunctionMenuService {
    /**
     * 添加功能菜单
     * @param requestParam
     * @return
     */
    int save(Map<String, Object> requestParam);

    /**
     * 修改功能菜单
     * @param funcId    功能菜单id
     * @param funcName	功能菜单名称
     * @return
     * @throws Exception
     */
    void update(Integer funcId, String funcName) throws Exception;

    /**
     * 查询功能菜单列表
     * @param sys_func_id
     * @param parent_id
     * @return
     * @throws Exception
     * 
     */
    List<FunctionMenu> query(Integer sys_func_id, Integer parent_id) throws Exception;
    
    /**
     * 查询全量功能菜单列表
     * @return
     * @throws Exception
     */
	List<FunctionMenu> queryMenu()throws Exception;

	/**
	 * 根据系统id查询功能菜单列表
	 * @param sys_id
	 * @return
	 */
	List<Map<String, Object>> queryMenuBysysId(Integer sys_id)throws Exception;
	
	//根据功能菜单id查询功能菜单列表
	List<FunctionMenu> queryByFuncId(Integer func_id)throws Exception;

	//查询功能菜单明细
	FunctionMenu queryFuncDetail(FunctionMenu functionMenu) throws Exception;
	
	//根据系统id和菜单级别查询功能菜单列表
	List<FunctionMenu> queryMenuBySysIdAndLever(Map<String, Object> requestParam)throws Exception;
	
	//根据功能菜单id查询菜单详情
	FunctionMenu queryMenuDetailByFuncId(Integer funcId)throws Exception;
	
	//查询所有一级菜单
	List<FunctionMenu> queryAll()throws Exception;
	
	//修改存量数据菜单
	void updateData(Integer funcId, String funcName, String field1, String field2, Integer sysId)throws Exception;

	/**
	 * 按id删除，其实为假删除
	 * @param funcId
	 */
	void delete(Integer funcId) throws Exception;
}
