package com.spdb.fdev.fuser.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.fuser.spdb.entity.user.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserDao {
	
	/**
	 * 新增用户
	 * @param user
	 * @return user
	 * */
	User addUser(User user);
	
	/**
	 * 根据英文缩写（英文名），删除用户
	 * @param nameEn
	 * @return long 删除数据条数
	 * */
	long delUserByNameEn(String nameEn);
	
	/**
	 * 修改用户，传入完整信息实体
	 * @param user
	 * @return user修改后的用户
	 * */
	User updateUser(User user)  throws Exception;
	
	/**
	 * 获取用户信息
	 * @param user
	 * @return List<User> 满足条件的用户
	 * */
	List<Map> getUser(User user) throws Exception;

	/**
	 * 获取用户核心数据
	 * @param user
	 * @return List<User> 满足条件的用户
	 * @throws Exception
	 */
	List<Map> getUserCoreData(User user) throws Exception;

	/**
	 * 获取所有用户核心信息
	 * @return
	 */
	List<Map> getAllUserCoreData();

	/**
	 * 模糊查询获取用户信息
	 * @param nameCn
	 * @return List<User> 满足条件的用户
	 * */
	List<Map>  getUserByName(String nameCn) throws Exception;
	
	/**
	 * 修改用户gitlabel帐号
	 * @throws Exception 
	 * */
	void updateGitUser(String user_name_en, String gitlabUsername) throws Exception;

	/**
	 * 更新用户密码
	 * @param user_name_en
	 * @param password
	 * @throws Exception
	 */
	void updatePassword(String user_name_en, String password) throws Exception;

	/**
	 * 获取用户信息
	 * @param name_en,status
	 * @return user 在职用户 
	 * */
	User getIsJobUser(String name_en, String status) throws Exception;
	
	/**
	 * 获取浦发行内所有的所属地区
	 * */
	List queryArea(String area_id) throws Exception;

	/**
	 * 获取用户所有的工作岗位
	 * */
	List queryPost(String post_id) throws Exception;
	/**
	 * 项目组资源统计表查询
	 * */
    List<User> queryUserStatis(List<String> queryRoleids, String s);
	/**
	 * 查询在职角色不为开发人员或测试人员或标签为非项目组资源
	 * */
	List<User> queryRemark(String s, String id, List<String> queryRoleids);
	/**
	 * 获取用户人员职能
	 * */
	List queryfunction(String function_id);
	/**
	 * 获取用户人员职级
	 * */
	List queryrank(String rank_id);

	/**
	 * 获取组中的所有在职人员
	 * */
	List<User> queryUsersByGroups(List<String> groupids);

	List<User> queryUserByCompanyGroup(String status, String company_id, Set<String> child_group_ids);


	Map<String, Object> queryUserBySearch(List<String> search, String companyId, String groupId, String status, int page, int per_page, String labelId,String is_party_member, String  area_id, String function_id, String section) throws Exception;

	/**
	 * 获取查询的组下用户角色中包含“开发人员“或“测试人员“，并且标签中不包含“非项目组资源“的用户信息
	 * @param groups
	 * @return
	 */
    List<User> queryProGroup(List<String> groups, List<String> roleIds, String labId);

	/**
	 * 获取查询的组下用户角色中不包含“开发人员“和“测试人员“或者标签中包含“非项目组资源“的用户信息
	 * @param
	 * @return
	 */
	List<User> queryNoProGroup(List<String> groupAll, List<String> queryRoleids, String id);

	/**
	 * 更新用户的玉衡角色、用户测试等级、mantis密钥
	 * */
	User updateUserFtms(User user);

	/**
	 * 根据小组id获取小组
	 * @param 
	 * @return
	 */
    Map queryUserNumBygroup(List<String> groupIds, String devId, String testId, String pufaId);

	/**
	 * 查询所有用户信息，加redis缓存
	 * @return
	 */
	List<Map> queryAllUser();

	/**
	 * 根据用户的id来查询获取用户的email
	 *
	 * @param ids
	 * @return
	 */
    List<Map> queryEmailByUserIds(List<String> ids);

    /**
	 * 查询所有用户信息(不连表)
	 * @return
	 */
	List<Map> queryAllUserName();

	/**
	 * 更新gitToken
	 * @param userNameEn
	 * @param gitToken
	 * @return
	 */
	Map updateGitToken(String userNameEn, String gitToken) throws Exception;

    List<User> getUserByRole(String roleId);

	/**
	 * 获取组的人数
	 * @return
	 */
	long queryInJobUserNum(String groupId);

	/**
	 * 查询这些组下的人
	 * @param iAndChildGroupIds
	 * @return
	 */
	Map getUsersInGroup(List<String> iAndChildGroupIds, int pageSize, int index);

	/**
	 * 查询所有在职的用户
	 * @return
	 */
	List<User> getAllUserInUse();

	/**
	 * 批量将用户信息修改为离职
	 * @param needUpdateUsers
	 */
	void updateUsersToLeave(Set<String> needUpdateUsers);

	/**
	 * 根据条件查询人数
	 * @param u
	 * @return
	 * @throws JsonProcessingException
	 */
	long getUserCount(User u) throws JsonProcessingException;

	List<User> getUsersInfoByIds(List<String> ids);

    List<Map> getAllUserAndRole(Map requestMap);
}
