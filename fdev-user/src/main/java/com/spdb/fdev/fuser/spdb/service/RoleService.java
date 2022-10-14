package com.spdb.fdev.fuser.spdb.service;

import java.util.List;

import com.spdb.fdev.fuser.spdb.entity.user.Role;

public interface RoleService {
	
	/**新增一个角色*/
	Role addRole(Role role) throws Exception;
	
	/**根据角色名称删除一个角色*/
	Role delRoleByID(String id) throws Exception;
	
	/**查询所有的角色*/
	List<Role> queryRole(Role role) throws Exception;
	
	/**更新Role*/
	Role updateRole(Role role)throws Exception;

	/**
	 * 根据多个角色中文名查询多个角色id
	 * @param roleNames
	 * @return
	 */
	List<String> queryRoleid(List<String> roleNames);

    /**
     * 根据角色中文名查询角色信息
     * @param roleName
     * @return
     */
	Role queryByName(String roleName);

	/**
	 * 判断用户是否为admin账号或某种角色
	 * 如userNameEn=null，则判断当前登录用户
	 * @return
	 */
	boolean checkRole(String userNameEn, String roleName) throws Exception;

//	/**
//	 * 判断用户是否为小组管理员
//	 * 如不传参，则判断当前登录用户
//	 * @return
//	 */
//	boolean isGroupManager(String userNameEn) throws Exception;

	/**
	 * 查询包含ids中任意一个menuId的角色
	 * @param menuIds
	 * @return
	 */
	List<Role> queryRoleByMenuId(List<String> menuIds);
}
