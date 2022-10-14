package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.Group;
import com.spdb.fdev.fuser.spdb.entity.user.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

	/**-----------------user----------------------------------*/
	Map login(String userNameEn,String passWord) throws Exception;

	Boolean exit(User user)throws Exception;



	/**
	 * 新增用户
	 * @param user
	 * @return user
	 * */
	User addUser(User user)  throws Exception;

	/**
	 * 通过LDAP新增的用户
	 * @param user
	 * @return
	 * @throws Exception
	 */
	User addUserWithLDAP(User user) throws Exception;
	/**
	 * 根据英文缩写（英文名），删除用户
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
	 * @param user,用户信息
	 * @return List<User> 满足条件的用户
	 * */
	List<Map> queryUser(User user) throws Exception;

	/**
	 * 获取用户核心数据
	 * @param user,用户信息
	 * @return List<User> 满足条件的用户
	 * @throws Exception
	 */
	List<Map> queryUserCoreData(User user) throws Exception;
	/**
	 * 模糊查询
	 * 获取用户信息
	 * @param nameCn,用户中文名
	 * @return List<User> 满足条件的用户
	 * */
    List<Map> getUserByName(String nameCn) throws Exception;

    //添加 Reporter 权限
	Object addMember(Map param) throws Exception;

	void removeCache(String key);

	/**
	 * 查询gitlab用户的id
	 * @param gitUserId
	 * @return
	 * @throws Exception
	 */
	String checkGitUserById(String gitUserId) throws Exception;

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
	 * @param name_en，status，用户英文名称,在职状态
	 * @return user 在职用户
	 * */
	User getIsJobUser(String name_en, String status) throws Exception;

	/**
	 * 获取浦发行内的所有的所属地区
	 * */
	List<Map> queryArea(String area_id) throws Exception;

	/**
	 * 获取人员职能
	 * */
	List queryfunction(String function_id);
	/**
	 * 获取用户职级
	 * */
	List queryrank(String rank_id);
	/**
	 * 项目组资源统计表查询
	 * */
    Map queryUserStatis(List<String> ids, boolean includeChild) throws Exception;

	List<User> queryUserByCompanyGroup(String status, String company_id, Set<String> child_group_ids);

	Map<String, Object> queryTaskNum(List<String> user_ids, List<String> roles);

	Map<String, Object> queryUserBySearch(List<String> search, String companyId, String groupId, String status, int page, int per_page, String labelId
	,String is_party_member, String  area_id, String function_id, String section) throws Exception;

	/**
	 * 接入行内统一认证平台进行身份认证
	 * @param username
	 * @param password
	 * @return
	 */
	boolean authenticate(String username, String password);

	/**
	 * 接入行内统一认证平台进行厂商用户身份认证
	 * @param username
	 * @param password
	 * @return
	 */
	boolean authenticateManu(String username, String password);

	/**
	 * 更新用户的玉衡角色、用户测试等级、mantis密钥
	 * */
	Map updateUserFtms(Map<String, Object> requestParam) throws Exception;

    /**
     * 根据指定组查询总人数和行内行外开发测试人员数量
     * @param groupIds
     * @return
     */
    List<Map> queryUserNumByGroup(List<String> groupIds);

    List<Map> queryEmailByUserIds(List<String> ids);
	/**
	 * 获取用户信息（不区分大小写）
	 * @param
	 * @return List<User> 满足条件的用户
	 * */
	List<Map> queryUserIgnoreCase(User user) throws Exception;

	Map updateGitToken(String userNameEn, String gitToken) throws Exception;

	/**
	 * 批量更新所有人的离职信息
	 */
    void updateAllLeaveUser();

	/**
	 * 供用户批量调整用户所在组使用。
	 *  根据当前用户身份，返回可调整的人员列表
	 *   admin、超级管理员、用户管理员可调整所有人；小组管理员、团队负责人只能调整本组及子组的人
	 * @return
	 */
	List<Map> canAddUserList() throws Exception;

	List<Map> getUsersInfoByIds(Map requestMap);

    List<Map> getAllUserAndRole(Map requestMap);
}
