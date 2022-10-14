package com.spdb.fdev.fuser.spdb.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fuser.spdb.entity.user.Group;

public interface GroupDao {

    /** 新增一个组 参数 组名称 */
	Group addGroup(Group group) throws Exception;

	/** 查询所有的分组 */
	List<Group> queryGroup(Group group) throws Exception;
	
	/** 更新分组 */
	Group updateGroup(Group group);

	/** 删除分组 */
	Group deleteGroup(Group group);

	Group queryDetailById(String groupId);

	/** 通过组名查询组id */
    List<String> queryGroupByNames(String testcentre, String businessdept, String plandept);

    List<Group> queryFirstGroup();

	/**
	 * 根据多个小组id查询小组信息
	 * @param groupIds
	 * @return
	 */
	List<Group> queryGroupByIds(List<String> groupIds, boolean allFlag);

	List queryGroupFullNameByIds(List<String> groupIds);

	Group queryGroupById(String groupId);

	/**
	 * 查询根节点
	 * delFlag：是否包含废弃的，true包含，false不包含
	 * @return
	 */
	List<Group> queryFirstGroupMap(boolean delFlag);

	/**
	 * 查询子节点
	 * @param parentId
	 * @param delFlag：是否包含废弃的，true包含，false不包含
	 * @return
	 */
	List<Group> queryChild(String parentId, boolean delFlag);

	List<Group> queryAllGroup();

    void addGroupUsers(List<String> userIds, String groupId);

	/**
	 * 根据sortNum查询组信息
	 * @param sortNum
	 * @return
	 */
    List<Group> queryGroupBySortNum(String sortNum);
}
