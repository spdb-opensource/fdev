package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.base.utils.DES3;
import com.spdb.fdev.fuser.base.utils.MD5;
import com.spdb.fdev.fuser.base.utils.MantisRestTemplate;
import com.spdb.fdev.fuser.spdb.Token.TokenManger;
import com.spdb.fdev.fuser.spdb.dao.LdapUserDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.entity.user.*;
import com.spdb.fdev.fuser.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RefreshScope
public class UserServiceImpl implements UserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private TokenManger tokenManger;

    @Resource
    private UserDao userDao;

    @Value("${git.resouce.id}")
    private String gitResouceId;

    @Value("${git.auth.token}")
    private String gitAuthToken;


    @Resource
    private RestTransport restTransport;

    @Resource
    private LdapUserDao ldapUserDao;

    @Resource
    private GroupService groupService;

    @Resource
    private CompanyService companyService;

    @Resource
    private LabelService labelService;

    @Resource
    private RoleService roleService;

    @Resource
    private DES3 des3;

    @Autowired
    private MantisRestTemplate mantisRestTemplate;

    @Value("${manits.add.user.url}")
    private String manits_user_url;
    @Value("${manits.admin.token}")
    private String manits_token;


    /**
     * @param user
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 新增用户
     */
    @Override
    public User addUser(User user) throws Exception {
        //创建ldap用户
        try {
            ldapUserDao.create(adaptUserToLdap(user));
        } catch (NameAlreadyBoundException e) {
            //如果ldap报用户已重名，说明老fdev用户已创建该用户，忽略该报错
            logger.error(user.getUser_name_en() + " has already exists in fdev ldap");
        } catch (Exception e) {
            //ldap其他报错阻拦新增用户
            logger.error("ldap error");
            throw e;
        }
        user.setCreateTime(new SimpleDateFormat(CommonUtils.STANDARDDATEPATTERN).format(new Date()));
        User returnUser = userDao.addUser(user);
        try {
            //注册mantis用户
            regeisterMantisUser(user);
        } catch (Exception e) {
            logger.info("用户"+user.getUser_name_en()+"注册失败！");
        }
        return returnUser;
    }

    @Override
    public User addUserWithLDAP(User user) throws Exception {
        User returnUser = userDao.addUser(user);
        return returnUser;
    }

    /***
     *
     * @Desc 根据用户英文名称，删除用户
     * @param nameEn
     * @return
     * 2019年3月27日
     */
    @Override
    public long delUserByNameEn(String nameEn) {
        LdapUser ldapUser = new LdapUser();
        ldapUser.setUid(nameEn);
        ldapUserDao.delete(ldapUser);
        long result = userDao.delUserByNameEn(nameEn);
        return result;
    }

    /**
     * @param user
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 更新用户
     */
    @Override
    public User updateUser(User user) throws Exception {
        if (ldapUserDao.findOne(user.getUser_name_en()) != null) {
            ldapUserDao.update(adaptUserToLdap(user));
        } else {
            ldapUserDao.create(adaptUserToLdap(user));
        }
        user.setUpdateTime(new SimpleDateFormat(CommonUtils.STANDARDDATEPATTERN).format(new Date()));
        User user1 = userDao.updateUser(user);
        return user1;
    }

    /**
     * @param user
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 根据用户输入信息，查询匹配条件的用户
     */
    @Override
    public List<Map> queryUser(User user) throws Exception {
        List<Map> queryUser = new ArrayList<>();
        if (CommonUtils.checkObjFieldIsAllNull(user)) {
            //查询所有用户信息，加redis缓存
            queryUser = userDao.queryAllUser();
        }else {
            queryUser = userDao.getUser(user);
        }
        if (queryUser.size() == 1) {
            queryUser = reBuildFullName(queryUser);
        }
        return queryUser;
    }

    /**
     * 获取小组全称	在用户详情和当前用户场景需要group放回fullgeroupname，所以只给第一个用户添加数据
     *
     * @param users
     * @return
     * @throws Exception
     */
    private List<Map> reBuildFullName(List<Map> users) throws Exception {
        if (!Util.isNullOrEmpty(users) && users.size() > 0) {
            // 用来存组id相同的 完整组名
            HashMap<String, String> groupHold = new HashMap<>();
            //获取小组全称
            Group group = new Group();
            for (int x = 0; x < users.size(); x++) {
                String groupId = (String) users.get(x).get(Dict.GROUP_ID);
                if (Util.isNullOrEmpty(groupHold.get(groupId))) {
                    group.setId(groupId);
                    List<Group> groups = groupService.queryParentGroupById(group);
                    String sub = "-";
                    String groupName = "";
                    for (int i = groups.size() - 1; i >= 0; i--) {
                        String name = groups.get(i).getName();
                        if (!groups.get(0).equals(groups.get(i))) {
                            name += sub;
                        }
                        groupName += name;
                    }
                    groupHold.put(groupId, groupName);
                }
                Group objectgroup = (Group) users.get(x).get(Dict.GROUP);
                objectgroup.setFullName(groupHold.get(groupId));
            }
        }
        return users;
    }

    @Override
    public List<Map> queryUserCoreData(User user) throws Exception {
        if (CommonUtils.checkObjFieldIsAllNull(user)) {
           return  userDao.getAllUserCoreData();
        }
        return userDao.getUserCoreData(user);
    }

    /**
     * @param userNameEn
     * @param passWord
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 登录
     */
    @Override
    public Map login(String userNameEn, String passWord) throws Exception {
        Map result = null;
        User user = new User();
        user.setUser_name_en(userNameEn);
        List<Map> list = userDao.getUser(user);
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        this.reBuildFullName(list);
        // 验证用户提交的密码是否正确
        String password = (String) list.get(0).get(Dict.PASSWORD);
        if (password.equals(des3.encrypt(passWord)) || password.equals(MD5.encoder(userNameEn, passWord))) {
            // 登录凭证（用户名及登录凭证ID），存储在redis中
            result = list.get(0);
            String tokenPre = result.get(Dict.USER_NAME_EN) + "-" + System.currentTimeMillis();
            String token = tokenManger.createToken(tokenPre);
            redisTemplate.opsForValue().set(((String) result.get(Dict.USER_NAME_EN)) + "user.login.token", token, 7, TimeUnit.DAYS);
            result.put(Dict.TOKEN, token);
        }
        return result;
    }

    /**
     * @param user
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 退出登录
     */
    @Override
    public Boolean exit(User user) throws Exception {
        List<Map> getUser = userDao.getUser(user);
        if (getUser.size() != 1 || CommonUtils.isNullOrEmpty(getUser.get(0))) {
            return false;
        }
        return redisTemplate.delete(((String) getUser.get(0).get(Dict.USER_NAME_EN)) + "user.login.token");
    }

    /***
     * @Desc 根据中文名称模糊匹配查询用户
     * @param nameCn 用户中文名称
     * @return
     * @throws Exception
     * 2019年3月27日
     */
    @Override
    public List<Map> getUserByName(String nameCn) throws Exception {

        return userDao.getUserByName(nameCn);
    }

    /**
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 给新添加的人添加Resource项目的reporter权限
     */
    @Override
    public Object addMember(Map param) throws Exception {
        param.put("id", gitResouceId);
        param.put("token", gitAuthToken);
        param.put("role", "20");
        param.put(Dict.REST_CODE, "addResourceMember");
        Object object = null;
        try {
            object = restTransport.submit(param);
        } catch (Exception e) {
            String mString = e.getMessage();
            logger.error(mString);
            if (CommonUtils.isNullOrEmpty(mString) || (!CommonUtils.isNullOrEmpty(mString) && !mString.contains("should be higher than"))) {
                throw e;
            }
        }
        return object;
    }

    @Override
    @RemoveCachedProperty(redisKeyExpression = "{key}")
    public void removeCache(String key) {

    }


    @Override
    public void updatePassword(String user_name_en, String password) throws Exception {
        User user = new User();
        user.setUser_name_en(user_name_en);
        user.setPassword(password);
        if(ldapUserDao.findOne(user.getUser_name_en()) != null) {
            ldapUserDao.update(adaptUserToLdap(user));
        } else {
            ldapUserDao.create(adaptUserToLdap(user));
        }
        userDao.updatePassword(user_name_en, password);
    }

	@Override
	public String checkGitUserById(String gitUserId) throws Exception {
		Map map = new HashMap<>();
		map.put("Private-Token", gitAuthToken);
		map.put(Dict.GIT_USER_ID, gitUserId);
		map.put(Dict.REST_CODE, "checkGitlabUser");
		String submitGet=null;
		try {
			submitGet = (String)restTransport.submit(map);
			if (CommonUtils.isNullOrEmpty(submitGet)) {
	        	throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitlab用户Id错误"});
	        }
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitlab用户Id错误"});
		}
		return submitGet;
	}

	private LdapUser adaptUserToLdap(User user) {
		LdapUser ldapUser = new LdapUser();
		ldapUser.setUid(user.getUser_name_en());
		ldapUser.setUserNameEn(user.getUser_name_en());
		if (!CommonUtils.isNullOrEmpty(user.getUser_name_cn())) {
			ldapUser.setUserNameCn(user.getUser_name_cn());
		}
		String encryptPassword = user.getPassword();
		if (!CommonUtils.isNullOrEmpty(encryptPassword)) {
			String plainPassword = des3.decrypt(encryptPassword);
			ldapUser.setPassword(MD5.ldapEncoderByMd5(plainPassword));
		}
		if (!CommonUtils.isNullOrEmpty(user.getEmail())) {
			ldapUser.setMail(user.getEmail());
		}
		if (!CommonUtils.isNullOrEmpty(user.getTelephone())) {
			ldapUser.setMobile(user.getTelephone());
		}
		return ldapUser;
	}

	@Override
	public void updateGitUser(String user_name_en, String gitlabUsername) throws Exception {
		userDao.updateGitUser(user_name_en, gitlabUsername);
	}

	public User getIsJobUser(String name_en, String status) throws Exception {
		return userDao.getIsJobUser(name_en, status);
	}

	@Override
	public List<Map> queryArea(String area_id) throws Exception {
		return userDao.queryArea(area_id);
	}

	@Override
	public Map queryUserStatis(List<String> groups, boolean includeChild) throws Exception {
        Map result = new HashMap();
        //获取表头地域
        List<Map> area = new ArrayList<>();
        List<Map> areas = userDao.queryArea("");
        for (Map map : areas) {
            Map areamap = new HashMap();
            areamap.put("id", map.get(Dict.ID));
            areamap.put("area", map.get(Dict.NAME));
            area.add(areamap);
        }
        result.put("bank", area);

        //获取表头厂商公司
        List<Map> devcompany = new ArrayList<>();
        Company company = new Company();
        company.setStatus("1");
        List<Company> companyNames = companyService.getCompany(company);
        //排除浦发
        String spdbId = "";
        for (Company com : companyNames) {
            Map companymap = new HashMap();
            if (!com.getName().equals("浦发")) {
                companymap.put("id", com.getId());
                companymap.put("company", com.getName());
                devcompany.add(companymap);
            } else {
                spdbId = com.getId();
            }
        }
        result.put("dev", devcompany);

        //小组统计相关人数
        List<Map> groupdata = new ArrayList<>();
        //获取开发人员和测试人员角色id
        List<String> roleNames = new ArrayList<>();
        roleNames.add(Constants.DEVELOPER);
        roleNames.add(Constants.TESTER);
        List<String> queryRoleids = roleService.queryRoleid(roleNames);
        //获取非项目组资源id
        Label label = new Label();
        label.setName(Constants.NOPRORESOURCE);
        List<Label> labs = labelService.queryLabel(label);

        //组装所有公司id为key，公司名为value 的数据
        Map<String, String> companyName = companyNames.stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));
        for (String groupId : groups) {
            List<Group> grouplist1 = new ArrayList<>();
            List<String> groupAll = new ArrayList<>();
            Group group1 = new Group();
            group1.setId(groupId);
            if(includeChild){          //包含子组（查询组下所有子组）
                grouplist1 = groupService.queryChildGroupById(group1);
                for (Group group : grouplist1) {
                   groupAll.add(group.getId());
                }
            } else {
                grouplist1.addAll(groupService.queryByGroup(group1));
                groupAll.add(groupId);
            }
            //组装所有组id为key，组名为value 的数据
            Map<String, String> groupName = grouplist1.stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));
            //获取查询的组下用户角色中包含“开发人员“或“测试人员“，并且标签中不包含“非项目组资源“的用户信息
            List<User> proGroupUser = userDao.queryProGroup(groupAll, queryRoleids, labs.get(0).getId());
            // 获取查询的组下用户角色中不包含“开发人员“或“测试人员“或标签中包含“非项目组资源“的用户信息
            List<User> noProGroupUser = userDao.queryNoProGroup(groupAll, queryRoleids, labs.get(0).getId());
            Map groupmap = new HashMap();
            //组别
            groupmap.put("group", groupName.get(groupId));
            groupmap.put("groupid", groupId);
            //行内
            Map bankmap = new HashMap();
            for (Map areaMap : area) {
                int groupUserNum = 0;
                if(!CommonUtils.isNullOrEmpty(proGroupUser)){
                    for (User user : proGroupUser) {
                        if (user.getCompany_id().equals(spdbId) && !CommonUtils.isNullOrEmpty(user.getArea_id()) && user.getArea_id().equals(areaMap.get("id").toString())) {
                            groupUserNum++;
                        }
                    }
                }
                bankmap.put(areaMap.get("id").toString(), groupUserNum);
            }
            groupmap.put("bank", bankmap);

            //厂商
            Map devmap = new HashMap();
            for (Map companyMap : devcompany) {
                int groupUserNum = 0;
                if(!CommonUtils.isNullOrEmpty(proGroupUser)) {
                    for (User user : proGroupUser) {
                        if (user.getCompany_id().equals(companyMap.get("id").toString())) {
                            groupUserNum++;
                        }
                    }
                }
                devmap.put(companyMap.get("id").toString(), groupUserNum);
            }
            groupmap.put("dev", devmap);
            //项目组规模
            groupmap.put("userSum", proGroupUser.size());
            //备注
            StringBuilder str = new StringBuilder();
            if (!CommonUtils.isNullOrEmpty(noProGroupUser)) {
                for (int i = 0; i<noProGroupUser.size(); i++) {
                    String username = noProGroupUser.get(i).getUser_name_cn();
                    String companyname = (String) companyName.get(noProGroupUser.get(i).getCompany_id());
                    if (i == 0) {
                        str.append(username + "(" + companyname + ")");
                    } else {
                        str.append("、"+username + "(" + companyname + ")");
                    }
                }
            }
            groupmap.put("remark", str);
            groupdata.add(groupmap);
        }
        result.put("groups", groupdata);
        return result;
    }

    @Override
    public List<User> queryUserByCompanyGroup(String status, String company_id, Set<String> child_group_ids) {
        return userDao.queryUserByCompanyGroup(status, company_id, child_group_ids);
    }

    @Override
    public Map<String, Object> queryTaskNum(List<String> user_ids, List<String> roles) {
        Map<String, Object> map = new HashMap<>();
        map.put("ids", user_ids);
        map.put("roles", roles);
        map.put(Dict.REST_CODE, "queryTaskNumByMember");
        Object object = null;
        try {
            object = restTransport.submit(map);
        } catch (Exception e) {
            String mString = e.getMessage();
            logger.error(mString);
        }
        return (Map<String, Object>) object;
    }

    @Override
    public Map<String, Object> queryUserBySearch(List<String> search, String companyId, String groupId, String status, int page, int per_page, String labelId,String is_party_member, String  area_id, String function_id, String section) throws Exception {
        return userDao.queryUserBySearch(search, companyId, groupId, status, page, per_page, labelId, is_party_member, area_id, function_id, section);
    }

    @Override
    public boolean authenticate(String username, String password) {
        return ldapUserDao.authenticate(username,password);
    }

    @Override
    public boolean authenticateManu(String username, String password) {
        return ldapUserDao.authenticateManu(username,password);
    }

    @Override
	public Map updateUserFtms(Map<String, Object> requestParam) throws Exception {
		String userId = (String) requestParam.get(Dict.ID);
		if(CommonUtils.isNullOrEmpty(userId)){
			throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"用户的ID"});
		}
		User user = new User();
		user.setId(userId);
		List<Map> returnData = userDao.getUser(user);
		//待修改的用户是否存在
		if(CommonUtils.isNullOrEmpty(returnData)){
			throw new FdevException(ErrorConstants.USR_NOT_EXIST);
		}
		List<String> role = (List<String>) requestParam.get(Dict.ROLE);
		String ftmsLevel = (String) requestParam.get(Dict.FTMS_LEVEL);
		String mantisToken = (String) requestParam.get(Dict.MANTIS_TOKEN);
		//如果没有待修改数据不执行修改
		if(CommonUtils.isNullOrEmpty(role) && CommonUtils.isNullOrEmpty(ftmsLevel) && CommonUtils.isNullOrEmpty(mantisToken)){
			return new HashMap();
		}
		user.setRole_id(role);
        user.setFtms_level(ftmsLevel);
		user.setMantis_token(mantisToken);
		userDao.updateUserFtms(user);
		User u = new User();
		u.setId(userId);
		return queryUser(u).get(0);
	}

    @Override
    public List<Map> queryUserNumByGroup(List<String> groupIds) {
        List<Map> result = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(groupIds)){
            //获取开发人员和测试人员角色id
            String devId =  roleService.queryByName(Constants.DEVELOPER).getId();
            String testId =  roleService.queryByName(Constants.TESTER).getId();
            //获取浦发公司id
            String pufaId = companyService.queryByName("浦发").getId();
            for(String groupId : groupIds){
                List<String> groups = new ArrayList<>();
                try {
                    groups = groupService.queryGroupid(groups, groupId);
                } catch (Exception e) {
                   logger.info("获取子组信息异常");
                }
                String groupName = groupService.queryDetailById(groupId).getName();
                Map userNum = userDao.queryUserNumBygroup(groups, devId, testId, pufaId);
                Map groupMap = new HashMap();
                groupMap.put("groupId", groupId);
                groupMap.put("groupName", groupName);
                groupMap.put("userCount", userNum.get("userCount"));
                groupMap.put("inDevNum", userNum.get("inDevNum"));
                groupMap.put("inTestNum", userNum.get("inTestNum"));
                groupMap.put("outDevNum", userNum.get("outDevNum"));
                groupMap.put("outTestNum", userNum.get("outTestNum"));
                result.add(groupMap);
            }
        }
        return result;
    }

    @Override
    public List<Map> queryUserIgnoreCase(User user) throws Exception {
        List<Map> users = userDao.queryAllUserName();
        for(Map map : users){
            String name = ((String)map.get(Dict.USER_NAME_EN)).toLowerCase();
            if(name.equals(user.getUser_name_en().toLowerCase())){
                User u = new User();
                u.setUser_name_en((String)map.get(Dict.USER_NAME_EN));
                List<Map> result = userDao.getUser(u);
                return reBuildFullName(result);
            }
        }
        return null;
    }

    @Override
    public Map updateGitToken(String userNameEn, String gitToken) throws Exception {
        if(!userNameEn.equals(CommonUtils.getSessionUser().getUser_name_en())){
            logger.error("user could only update own git token");
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户只能修改自己的git token"});
        }
        return  userDao.updateGitToken(userNameEn, gitToken);
    }

    /**
     * 批量更新所有人的离职信息
     */
    @Override
    public void updateAllLeaveUser(){
        List<User> allUserInUse = userDao.getAllUserInUse();
        boolean isSpdb = false;
        Set<String> needUpdateUsers = new HashSet<>();//用于记录需要更新的用户id
        for(User user:allUserInUse){
            isSpdb = false;
            //判断是否是浦发
            if(user.getEmail().toLowerCase().endsWith("xxx")){
                isSpdb = true;
            }
            if(ldapUserDao.checkIsLeaveOrNot(user.getEmail(),isSpdb)){
                needUpdateUsers.add(user.getId());
            }
        }
        //批量更新用户离职信息
        userDao.updateUsersToLeave(needUpdateUsers);
    }

    /**
     * 供用户批量调整用户所在组使用。
     *  根据当前用户身份，返回可调整的人员列表
     *   admin、超级管理员可调整所有人；小组管理员、团队负责人只能调整本组及子组的人
     * @return
     */
    @Override
    public List<Map> canAddUserList() throws Exception {
        User user = new User();
        user.setStatus("0");
        if(roleService.checkRole(null,Constants.SUPER_MANAGER) || roleService.checkRole(null,Constants.USER_MANAGER)){
            return userDao.getUserCoreData(user);
        }
        if(roleService.checkRole(null,Constants.GROUP_MANAGER) || roleService.checkRole(null,Constants.TEAM_LEADER)){
            com.spdb.fdev.common.User sessionUser = CommonUtils.getSessionUser();
            Map map = groupService.queryUserByGroupId(sessionUser.getGroup_id(), true, 0, 0);
            return (List<Map>)map.get(Dict.DATA);
        }
        return null;
    }

    @Override
    public List<Map> getUsersInfoByIds(Map requestMap) {
        if(!CommonUtils.isNullOrEmpty(requestMap.get(Dict.IDS))){
            List<String> ids = (List<String>)requestMap.get(Dict.IDS);
            List<User> users = userDao.getUsersInfoByIds(ids);
            List<Map> result = new ArrayList<>();
            for(User user : users){
                Map<String, Object> map = new HashMap();
                map.put(Dict.ID,user.getId());
                map.put(Dict.USER_NAME_EN,user.getUser_name_en());
                map.put(Dict.USER_NAME_CN,user.getUser_name_cn());
                map.put(Dict.EMAIL,user.getEmail());
                result.add(map);
            }
            return result;
        }
        return null;
    }

    @Override
    public List<Map> getAllUserAndRole(Map requestMap) {
        List<Map> allUserAndRole = userDao.getAllUserAndRole(requestMap);
//        List<Map> result = new ArrayList<>();
//        for(Map user : allUserAndRole){
//            Map map = new HashMap();
//            map.put(Dict.ID,user.get(Dict.ID));
//            map.put(Dict.USER_NAME_EN,);
//            map.put(Dict.USER_NAME_CN,);
//            map.put(Dict.ROLE,);
//            map.put(Dict.ro)
//        }
        return allUserAndRole;
    }

    @Override
    public List queryfunction(String function_id) {
        return userDao.queryfunction(function_id);
    }

    @Override
    public List queryrank(String rank_id) {
        return userDao.queryrank(rank_id);
    }

    public void regeisterMantisUser(User user) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put(Dict.USER_NAME, user.getUser_name_en());
        userMap.put(Dict.USER_NAME_CN, user.getUser_name_cn());
        userMap.put(Dict.PASSWORD, des3.decrypt(user.getPassword()));
        userMap.put(Dict.EMAIL, user.getEmail());
        userMap.put(Dict.REST_CODE, "regeisterMantisUser");
        try {
            restTransport.submit(userMap);
        } catch (Exception e) {
            logger.error("add mantis user error,user =" + user.getUser_name_cn() + "|" + user.getUser_name_en());
            throw new FdevException(ErrorConstants.ADD_MANTISUSER_ERROR, new String[]{"user =" + user.getUser_name_cn()});
        }
    }

    @Override
    public List<Map> queryEmailByUserIds(List<String> ids) {
        return this.userDao.queryEmailByUserIds(ids);
    }
}