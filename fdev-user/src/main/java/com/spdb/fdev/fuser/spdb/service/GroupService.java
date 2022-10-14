package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.Group;

import java.util.List;
import java.util.Map;

public interface GroupService {

	
	/**-----------------Group----------------------------------*/
	/**新增一个组 参数 组名称*/
	Group addGroup(Group group)throws Exception;
	
	/**查询分组*/
	List<Group> queryGroup(Group group)throws Exception;

	/**查询分组下所有成员*/
	List<Map> queryByGroupId(Group group)throws Exception;

	/**查询组及以上组*/
	List<Group> queryParentGroupById(Group group)throws Exception;

	/**查询组及所有子组*/
	List<Group> queryChildGroupById(Group group)throws Exception;

	/**修改分组*/
	Group updateGroup(Group group);

	/**删除分组*/
	Group deleteGroup(Group group)throws Exception;

	void removeCache(String key);
	
	/**查询组的开发人数*/
	List<Group> queryDevResource(List<String> groupIds) throws Exception;

	Group queryDetailById(String groupId);

	List<String> queryGroupByNames(String testcentre, String businessdept, String plandept);

	List<Group> queryByGroup(Group group) throws Exception;

    List<Group> groupBySortNum() throws Exception;

	List<String> queryGroupid(List<String> grouplist, String groupId) throws Exception;

    /**
     * 所有小组信息进行排序查询，并进行redis缓存处理
     * @return
     * @throws Exception
     */
    List<Group> queryAllGroup() throws Exception;

	/**
	 * 根据多个小组id查询小组信息
	 * @param groupIds
	 * @return
	 */
	List<Group> queryGroupByIds(List<String> groupIds, boolean allFlag);

	List queryGroupFullNameByIds(List<String> groupIds);

	String queryGroupNameById(String groupId) throws Exception;

	Map queryAllGroupName();

	/**
	 * 查询所有小组包含本组及子组的人数，按层级关系返回
	 * @return
	 */
	List<Group> queryAllGroupAndChild();

	/**
	 * 根据组id分页查询组下的人（分包含和不包含子组）
	 * @param groupId
	 * @param showChild
	 * @param pageSize
	 * @param index
	 * @return
	 */
	Map queryUserByGroupId(String groupId, boolean showChild, int pageSize, int index) throws Exception;

	/**
	 * 批量修改用户所属组
	 * @param userIds
	 */
	void addGroupUsers(List<String> userIds, String groupId);

	/**
	 *获取组的三级父组，若当前组为3级之内，则直接返回自身
	 * @param groupId
	 * @return
	 */
	Group getThreeLevelGroup(String groupId) throws Exception;

	/**
	 * 批量查询当前组及子组信息
	 * @param ids
	 * @return
	 */
    Map<String, List<Group>> queryChildGroupByIds(List<String> ids);
}
