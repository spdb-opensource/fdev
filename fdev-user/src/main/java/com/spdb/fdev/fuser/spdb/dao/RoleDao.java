package com.spdb.fdev.fuser.spdb.dao;

import java.util.List;

import com.spdb.fdev.fuser.spdb.entity.user.Role;

public interface RoleDao {
	
	
	/**新增一个角色*/
	Role addRole(Role role);
	
	/**根据角色名称删除一个角色*/
	Role delRoleByID(String id);
	
	/**查询所有的角色*/
	List<Role> queryRole(Role role)  throws Exception;

	/**查询所有的角色，不包含模糊查询*/
	List<Role> queryRole2(Role role)  throws Exception;


	Role updateRole(Role role);

    /**
     * 根据多个角色中文名查询多个角色id
     * @param roleNames
     * @return
     */
    List<String> queryRoleid(List<String> roleNames);

	/**
	 * 根据角色名称查询id
	 * @param roleName
	 * @return
	 */
    Role queryIdByName(String roleName);

	/**
	 * 根据角色id批量查询角色信息
	 * @param roleIds
	 * @return
	 */
	List<Role> queryRoleByIds(List<String> roleIds);

	/**
	 * 查询包含menuids中任意一个menuid的角色
	 * @param menuIds
	 * @return
	 */
	List<Role> queryRoleByMenuId(List<String> menuIds);
}
