package com.testmanage.admin.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.testmanage.admin.entity.FunctionMenu;
import org.omg.PortableInterceptor.INACTIVE;


public interface FunctionMenuMapper {
    //添加功能菜单
	int insert(FunctionMenu functionMenu) throws Exception;
	
	//修改功能菜单
    int update(@Param("funcId")Integer funcId, @Param("funcName")String funcName, @Param("field1")String field1, @Param("field2")String field2) throws Exception;
    //查询菜单下的全部子菜单
    List<FunctionMenu> selectByParentId(@Param("sysFuncId")Integer sys_func_id, @Param("parentId")Integer parent_id) throws Exception;
    
	List<FunctionMenu> getMenu()throws Exception;
	
	//根据系统id查询功能菜单列表
	List<Map<String, Object>> queryMenuBysysId(Integer sys_id)throws Exception;
	
	//根据功能菜单id查询集合
	List<FunctionMenu> queryByFuncId(Integer func_id)throws Exception;
	
	//查询所有功能菜单
	List<FunctionMenu> queryAll()throws Exception;
	
	//查询功能菜单明细
	FunctionMenu queryFuncDetail(FunctionMenu functionMenu);
	
	//根据系统id和菜单级别查询功能菜单列表
	List<FunctionMenu> queryMenuBySysIdAndLever(@Param("map")Map<String, Object> requestParam)throws Exception;
	
	//根据系统id,功能菜单名称,菜单级别查询菜单
	FunctionMenu queryMenuBySysIdAndLeverAndName(@Param("map")Map<String, Object> map)throws Exception;
	
	//根据功能菜单id查询菜单详情
	FunctionMenu queryMenuDetailByFuncId(Integer funcId)throws Exception;

	/**
	 * 使用func_id作为parent_id进行查询记录
	 * @param func_id
	 * @return
	 */
	List<FunctionMenu> queryByParentId(Integer func_id);

	//根据funcId来进行假删除（更新）
	Integer delete(Integer funcId);

}
