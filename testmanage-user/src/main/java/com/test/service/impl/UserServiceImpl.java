package com.test.service.impl;

import com.test.dao.WorkConfigDao;
import com.test.dict.Constants;
import com.test.dict.Dict;
import com.test.dict.ErrorConstants;
import com.test.entity.WorkConfig;
import com.test.service.FdevUserService;
import com.test.service.MantisUserService;
import com.test.service.UserService;
import com.test.testmanagecommon.cache.LazyInitProperty;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import com.test.utils.MyUtils;
import com.test.utils.SpringUtil;
import com.test.utils.TokenManger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class UserServiceImpl implements UserService {
    @Autowired
    private TokenManger tokenManger;
    @Autowired
    private MantisUserService mantisUserService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FdevUserService fdevUserService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private WorkConfigDao workConfigDao;

	@Value("${user.assessor.role.id}")
	private String assessorRoleId;

	@Value("${user.testManager.role.id}")
	private String testManagerRoleId;

	@Value("${user.testLeader.role.id}")
	private String testLeaderRoleId;

	@Value("${user.securityTestLeader.role.id}")
	private String securityTestLeaderRoleId;

	@Value("${user.tester.role.id}")
	private String testerRoleId;

    @Value("${user.testAdmin.role.id}")
    private String testAdminRoleId;

    @Value("${user.testAuto.role.id}")
    private String testAutoRoleId;

    @Resource
    private SpringUtil springUtil;

    @Autowired
    private RestTransport restTransport;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public Map update(String user_id,String  level, List<String> roleIds) throws Exception {
		Map sendMap = new HashMap();
		//获取被修改人信息
		Map user = queryUserCoreDataById(user_id);
		if(!checkUser(roleIds, (String)user.get(Dict.USER_NAME_EN))){
			throw new FtmsException(ErrorConstants.ROLE_ERROR);
		}
		sendMap.put(Dict.REST_CODE, "fuser.updateUserFtms");
		sendMap.put(Dict.ID, user_id);
		sendMap.put(Dict.FTMS_LEVEL, level);
		sendMap.put(Dict.ROLE, roleIds);
		sendMap.put(Dict.MANTIS_TOKEN, user.get(Dict.MANTIS_TOKEN));
		try {
			user = (Map)restTransport.submitSourceBack(sendMap);
		}catch (Exception e){
			logger.error("fdev user update error, id = "+user_id);
			logger.error("e:"+e);
		}
		return user;
    }

    private boolean checkUser(List<String> roleIds, String userNameEn) throws Exception{
		//获取当前用户
		Map<String, Object> currentUser = redisUtils.getCurrentUserInfoMap();
		if(MyUtils.isEmpty(currentUser)) {
			throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		String currentUserNameEn = (String)currentUser.get(Dict.USER_NAME_EN);
		//查询当前登录用户的信息
		Map user = queryUserDetailByNameEn(currentUserNameEn);
		//实时获取当前登录角色
		List<String> role_id = (List<String>)user.get(Dict.ROLE_ID);
		if(role_id.contains(testAdminRoleId)){
			//当前执行修改的用户为admin 可以修改所有
			return  true;
		}
		if(role_id.contains(testManagerRoleId)){
			//若当前用户为管理员，则要修改的用户为管理员或者超级管理且非本人  不可修改
			if(roleIds.contains(testAdminRoleId)){
				return false;
			}
			if(roleIds.contains(testAutoRoleId)){
			    return false;
            }
			if(roleIds.contains(testManagerRoleId) && !userNameEn.equals(currentUserNameEn)){
				return false;
			}
			return true;
		}
		if(role_id.contains(testLeaderRoleId)){
			//若当前用户为管理员，则要修改的用户为管理员,超级管理员或测试组长且非本人  不可修改
			if(roleIds.contains(testAdminRoleId) || roleIds.contains(testManagerRoleId)|| roleIds.contains(testAutoRoleId)){
				return false;
			}
			if(roleIds.contains(testLeaderRoleId) && !userNameEn.equals(currentUserNameEn)){
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void saveMantisToken(String mantis_token) throws Exception {
		Map<String, Object> currentUser = redisUtils.getCurrentUserInfoMap();
		if(MyUtils.isEmpty(currentUser)) {
			throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		String id = (String)currentUser.get(Dict.ID);
		Map sendMap = new HashMap();
		sendMap.put(Dict.REST_CODE, "fdev.user.core.query");
		sendMap.put(Dict.ID, id);
		List<Map> users = (List<Map>)restTransport.submitSourceBack(sendMap);
		if(MyUtils.isEmpty(users)){
			throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
		}
		Map user = users.get(0);
		sendMap.put(Dict.REST_CODE, "fuser.updateUserFtms");
		sendMap.put(Dict.ID, id);
		sendMap.put(Dict.FTMS_LEVEL, user.get(Dict.FTMS_LEVEL));
		sendMap.put(Dict.MANTIS_TOKEN, mantis_token);
		try {
			restTransport.submitSourceBack(sendMap);
		}catch (Exception e){
			logger.error("fdev user update error, id = "+id);
			logger.error("e:"+e);
		}
	}

	@Override
	public boolean vaildateAdmin() throws Exception{
		Map<String, Object> currentUser = redisUtils.getCurrentUserInfoMap();
		if(MyUtils.isEmpty(currentUser)) {
			throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		List<String> role_id = (List<String>)currentUser.get(Dict.ROLE_ID);
		if(role_id.contains(testAdminRoleId)){
			//当前执行修改的用户为admin 可以修改
			return  true;
		}
    	return false;
	}

	@Override
	public Map login(String userNameEn, String password) throws Exception {
		Map sendMap = new HashMap();
		sendMap.put(Dict.USER_NAME_EN, userNameEn);
		sendMap.put(Dict.PASSWORD, password);
		sendMap.put(Dict.TYPE, "account");
		sendMap.put("userName", userNameEn);
		sendMap.put("remark",1);
		sendMap.put(Dict.REST_CODE, "fdev.login");
		Map user = (Map)restTransport.submitSourceBack(sendMap);
		return loginSaveUser(user);
	}

	public Map getCurrentUser(String token) throws Exception {
		String redis_token = new StringBuilder(Constants.REDIS_USER_TOKEN).append(token).toString();
		Object object = redisTemplate.opsForValue().get(redis_token);
		return (MyUtils.beanToMap(object));
	}

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.user.{userNameEn}")
    public Map queryUserDetailByNameEn(String userNameEn) throws Exception{
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "fdev.user.core.query");
        sendMap.put(Dict.USER_NAME_EN, userNameEn);
        List<Map> list = (List<Map>)restTransport.submitSourceBack(sendMap);
        if(Util.isNullOrEmpty(list)){
            return null;
        }
        return  list.get(0);
    }

    @Override
	public Map loginSaveUser(Map user) throws Exception{
		String user_name_en = (String)user.get(Dict.USER_NAME_EN);
        String token = "";
		if(springUtil.getApplicationContext().getEnvironment().getActiveProfiles()[0].contains("new")) {
            token = (String)user.get(Dict.TOKEN);
        } else {
            token = tokenManger.createToken(user_name_en);
        }
    	String redis_token = new StringBuilder(Constants.REDIS_USER_TOKEN).append(token).toString();
        Map map = MyUtils.beanToMap(user);
    	redisTemplate.opsForValue().set(redis_token, map,7,TimeUnit.DAYS);
        map.put(Dict.USERTOKEN, token);
        return map;
	}

	@Override
	public void exportUser(List<String> search, String groupId, HttpServletResponse resp) throws Exception {
		//查询全部非离职人员
		Map sendMap = new HashMap();
		sendMap.put(Dict.REST_CODE, "fuser.queryUser");
		sendMap.put(Dict.GROUP_ID, groupId);
		if(Util.isNullOrEmpty(search)){
			search.add("测试人员");
			search.add("玉衡-测试组长");
			search.add("玉衡-测试管理员");
			search.add("玉衡超级管理员");
		}
		sendMap.put(Dict.SEARCH, search);
		sendMap.put(Dict.PAGE, 1);
		sendMap.put(Dict.PER_PAGE, 0);
		sendMap.put(Dict.STATUS,"0");
		Map userMap = (Map)restTransport.submitSourceBack(sendMap);
		if(MyUtils.isEmpty(userMap)){
			return;
		}
		List<Map> userList = (List<Map>)userMap.get(Dict.LIST);
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			setCellValue(workbook, 0, 0, 0, "姓名");
			setCellValue(workbook, 0, 0, 1, "用户名");
			setCellValue(workbook, 0, 0, 2, "小组");
			setCellValue(workbook, 0, 0, 3, "角色");
			setCellValue(workbook, 0, 0, 4, "级别");
			setCellValue(workbook, 0, 0, 5, "公司");
			setCellValue(workbook, 0, 0, 6, "邮箱");
			setCellValue(workbook, 0, 0, 7, "电话");
			int i = 1;
			for (Map user : userList) {
				if(MyUtils.isEmpty(user)) {
					continue;
				}
				setCellValue(workbook, 0, i, 0, String.valueOf(user.get(Dict.USER_NAME_CN)));
				setCellValue(workbook, 0, i, 1, String.valueOf(user.get(Dict.USER_NAME_EN)));
				Map group = (Map)user.get(Dict.GROUP);
				setCellValue(workbook, 0, i, 2, String.valueOf(group.get(Dict.NAME)));
				List<Map> role = (List<Map>)user.get(Dict.ROLE);
				List<String> collect = role.stream().map(e -> (String)e.get(Dict.NAME)).collect(Collectors.toList());
				setCellValue(workbook, 0, i, 3, String.join(",", collect));
				setCellValue(workbook, 0, i, 4, String.valueOf(user.get(Dict.FTMS_LEVEL)));
				Map company = (Map)user.get(Dict.COMPANY);
				setCellValue(workbook, 0, i, 5, String.valueOf(company.get(Dict.NAME)));
				setCellValue(workbook, 0, i, 6, String.valueOf(user.get(Dict.EMAIL)));
				setCellValue(workbook, 0, i, 7, String.valueOf(user.get(Dict.TELEPHONE)));
				i++;
			}
		} catch (Exception e) {
			logger.error("e"+e);
			throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
		}
		resp.reset();
		resp.setContentType("application/octet-stream");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Content-Disposition", "attachment;filename=" + "FtmsUserList.xlsx");
		workbook.write(resp.getOutputStream());
	}


	/**
	 * excel填值
	 *
	 * @param workbook excel对象
	 * @param sheetIndex
	 * @param rowIndex
	 * @param cellIndex
	 * @param cellValue
	 * @throws Exception
	 */
	private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue)
			throws Exception {
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		if (sheet == null) {
			sheet = workbook.createSheet(String.valueOf(sheetIndex));
		}
		if (sheet.getRow(rowIndex) == null) {
			sheet.createRow(rowIndex);
		}
		if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
			sheet.getRow(rowIndex).createCell(cellIndex);
		}
		sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
	}


	@Override
	public void fdevSyncUser() throws Exception {
		/*List<User> queryAllUser = queryAllUser();
		for (User user : queryAllUser) {
			Map<String, Object> fdevuser = fdevUserService.queryUserByUserNameEn(user.getUser_en_name());
			if (Util.isNullOrEmpty(fdevuser)) {
				deleteUser(user.getUser_id());
				continue;
			}
			String fdevgroup_id = (String) fdevuser.get("group_id");
			String group_id = groupService.queryGroupMapByFdev(fdevgroup_id);
			if (Util.isNullOrEmpty(group_id)) {
				System.out.println(fdevgroup_id);
				throw new FtmsException("");
			}
			user.setGroup_id(group_id);
			update(user);
		}*/
	}

	@Override
	public Integer updateWorkConfig(String group_id, String manager, List<String> groupLeaders,
									String uatContact, List<String> securityLeaders) throws Exception {
		//获取当前用户
		Map currentUser = redisUtils.getCurrentUserInfoMap();
		if(MyUtils.isEmpty(currentUser)) {
			throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		List<String> role_id = (List<String>)currentUser.get(Dict.ROLE_ID);
		if(!role_id.contains(testAdminRoleId)){
			throw new FtmsException(ErrorConstants.ROLE_ERROR);
		}
		WorkConfig workConfig = new WorkConfig();
		//查询该组下是否已经分配人员，如果没有分配，则做插入操作，否则做修改操作
		WorkConfig currentManagerId = workConfigDao.queryWorkLeader(group_id);
		if(!Util.isNullOrEmpty(manager) &&
				(!Util.isNullOrEmpty(groupLeaders) || !Util.isNullOrEmpty(securityLeaders))) {
			String groupleader = "";
			if(!Util.isNullOrEmpty(groupLeaders)) {
				groupleader = String.join(",", groupLeaders);
			}
			String securityLeader = "";
			if(!Util.isNullOrEmpty(securityLeaders)) {
				securityLeader = String.join(",", securityLeaders);
			}
			//该组存在工单负责人
			if(!Util.isNullOrEmpty(currentManagerId)) {
				//判断所选工单负责人的权限
				Map user = queryUserDetailByNameEn(manager);
				List<String> managertRole = (List<String>)user.get(Dict.ROLE_ID);
				if( !managertRole.contains(testManagerRoleId) && !managertRole.contains(testAdminRoleId)){
					throw new FtmsException(ErrorConstants.MANAGER_ROLE_ERROR);
				}
				//判断所选测试小组长的权限
				for(String userName : groupLeaders) {
					Map leaderUser = queryUserDetailByNameEn(userName);
					List<String> leaderRole = (List<String>)leaderUser.get(Dict.ROLE_ID);
					if(!leaderRole.contains(testManagerRoleId) && !leaderRole.contains(testAdminRoleId)
							&& !leaderRole.contains(testLeaderRoleId) ){
						throw new FtmsException(ErrorConstants.LEADER_ROLE_ERROR);
					}
				}
				//判断所选安全测试组长的权限
				for(String userName : securityLeaders) {
					Map leaderUser = queryUserDetailByNameEn(userName);
					List<String> leaderRole = (List<String>)leaderUser.get(Dict.ROLE_ID);
					if(!leaderRole.contains(testManagerRoleId) && !leaderRole.contains(testAdminRoleId)
							&& !leaderRole.contains(securityTestLeaderRoleId) ){
						throw new FtmsException(ErrorConstants.LEADER_ROLE_ERROR);
					}
				}
				workConfig.setFtms_group_id(group_id);
				workConfig.setWork_leader(manager);
				workConfig.setGroup_leader(groupleader);
				workConfig.setUatContact(uatContact);
				workConfig.setSecurityLeader(securityLeader);
				return workConfigDao.updateWorkConfig(workConfig);
			}else {
				return workConfigDao.insertWorkConfig(group_id, manager, groupleader, uatContact, securityLeader);
			}
		}else {
			return 0;
		}
	}

	@Override
	public Map query(List<String> search, String groupId, Integer currentPage, Integer pageSize) throws Exception {
		Map sendMap = new HashMap();
		sendMap.put(Dict.REST_CODE, "fuser.queryUser");
		sendMap.put(Dict.GROUP_ID, groupId);
		sendMap.put(Dict.SEARCH, search);
		sendMap.put(Dict.PAGE, currentPage);
		sendMap.put(Dict.PER_PAGE, pageSize);
		return (Map)restTransport.submitSourceBack(sendMap);
	}


	@LazyInitProperty(redisKeyExpression = "tuser.fdev.child.group.{groupId}")
	@Override
	public List<Map<String, String>> queryChildGroupById(String groupId) throws Exception {
		Map sendMap = new HashMap();
		sendMap.put(Dict.ID, String.valueOf(groupId));
		sendMap.put(Dict.REST_CODE, "queryChildGroupById");
		List<Map<String, String>> resultList = (List<Map<String, String>>) restTransport.submitSourceBack(sendMap);
		return resultList;
	}

	@LazyInitProperty(redisKeyExpression = "tuser.fdev.group.testers.{groupId}")
	@Override
	public Integer queryGroupCountTester(String groupId) throws Exception {
		List<Map<String, String>> mapList = queryChildGroupById(groupId);
		Set<String> collect = mapList.stream().map(e -> e.get(Dict.ID)).collect(Collectors.toSet());
		Integer countTester = 0;
		for (String group_id : collect) {
			Map sendMap = new HashMap();
			sendMap.put(Dict.REST_CODE, "queryUser");
			sendMap.put(Dict.GROUP_ID, group_id);
			List<String> search = new ArrayList<>();
			search.add("测试人员");
			sendMap.put(Dict.SEARCH, search);
			sendMap.put(Dict.STATUS, "0");
			sendMap.put(Dict.PAGE, 0);
			sendMap.put(Dict.PER_PAGE, 0);
			Map users = (Map) restTransport.submitSourceBack(sendMap);
			if (!Util.isNullOrEmpty(users)) {
				countTester += (Integer) users.get(Dict.TOTAL);
			}
		}
		return countTester;
	}

	@Override
	public List<Map> queryGroupTester(String groupId, boolean flag) throws Exception {
		List<Map> userList = new ArrayList<>();
		if(Util.isNullOrEmpty(groupId)){
			Map sendMap = new HashMap();
			sendMap.put(Dict.REST_CODE, "fuser.queryUser");
			List<String> search = new ArrayList<>();
			search.add("测试人员");
			sendMap.put(Dict.SEARCH, search);
			sendMap.put(Dict.STATUS, "0");
			sendMap.put(Dict.PAGE, 0);
			sendMap.put(Dict.PER_PAGE, 0);
			Map users = (Map) restTransport.submitSourceBack(sendMap);
			return  (List<Map>)users.get(Dict.LIST);
		}
		if(flag){
			List<Map<String, String>> mapList = queryChildGroupById(groupId);
			Set<String> collect = mapList.stream().map(e -> e.get(Dict.ID)).collect(Collectors.toSet());
			for (String group_id : collect) {
				Map sendMap = new HashMap();
				sendMap.put(Dict.REST_CODE, "fuser.queryUser");
				sendMap.put(Dict.GROUP_ID, group_id);
				List<String> search = new ArrayList<>();
				search.add("测试人员");
				sendMap.put(Dict.SEARCH, search);
				sendMap.put(Dict.STATUS, "0");
				sendMap.put(Dict.PAGE, 0);
				sendMap.put(Dict.PER_PAGE, 0);
				Map users = (Map) restTransport.submitSourceBack(sendMap);
				if(Util.isNullOrEmpty(users)){
					return null;
				}
				List<Map> user = (List<Map>)users.get(Dict.LIST);
				userList.addAll(user);
			}
		}else{
			Map sendMap = new HashMap();
			sendMap.put(Dict.REST_CODE, "fuser.queryUser");
			sendMap.put(Dict.GROUP_ID, groupId);
			List<String> search = new ArrayList<>();
			search.add("测试人员");
			sendMap.put(Dict.SEARCH, search);
			sendMap.put(Dict.STATUS, "0");
			sendMap.put(Dict.PAGE, 0);
			sendMap.put(Dict.PER_PAGE, 0);
			Map users = (Map) restTransport.submitSourceBack(sendMap);
			if(Util.isNullOrEmpty(users)){
				return null;
			}
			List<Map> user = (List<Map>)users.get(Dict.LIST);
			userList.addAll(user);
		}
		return userList;
	}

	@Override
	public List<Map> queryTester() throws Exception {
		List<Map> userList = new ArrayList<>();
		Map sendMap = new HashMap();
		sendMap.put(Dict.REST_CODE, "fuser.queryUser");
		List<String> search = new ArrayList<>();
		search.add("测试人员");
		sendMap.put(Dict.SEARCH, search);
		sendMap.put(Dict.STATUS, "0");
		sendMap.put(Dict.PAGE, 0);
		sendMap.put(Dict.PER_PAGE, 0);
		Map users = (Map) restTransport.submitSourceBack(sendMap);
		if(Util.isNullOrEmpty(users)){
			return null;
		}
		return (List<Map>)users.get(Dict.LIST);
	}

	@LazyInitProperty(redisKeyExpression = "tuser.fdev.group.{groupId}")
	@Override
	public Map<String, Object> queryGroupDetailById(String groupId) throws Exception {
		Map sendMap = new HashMap();
		sendMap.put(Dict.REST_CODE, "fdev.user.queryGroupDetail");
		sendMap.put(Dict.ID, groupId);
		sendMap.put("scope", "all");
		List<Map> groups = (List<Map>)restTransport.submitSourceBack(sendMap);
		if(!Util.isNullOrEmpty(groups)){
			return groups.get(0);
		}else{
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> queryAlreadyAllocated() throws Exception {
        List<Map<String, String>> allocated = workConfigDao.queryAlreadyAllocated();
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> resultUnit;
        for(Map<String, String> unit : allocated){
            resultUnit = new HashMap<>();
            resultUnit.put(Dict.GROUPID, unit.get(Dict.GROUPID));
			Map workleader = new HashMap();
            String workleaderCn = "";
            String workleaderEn = "";
			if(!Util.isNullOrEmpty(unit.get(Dict.WORKLEADER))){
				workleaderEn = unit.get(Dict.WORKLEADER);
				Map userData = queryUserDetailByNameEn(unit.get(Dict.WORKLEADER));
				if(!Util.isNullOrEmpty(userData)){
					workleaderCn = String.valueOf(userData.get(Dict.USER_NAME_CN));
				}else{
					workleaderCn = unit.get(Dict.WORKLEADER);
				}
				workleader.put(Dict.USER_NAME_EN, workleaderEn);
				workleader.put(Dict.USER_NAME_CN, workleaderCn);
			}
            resultUnit.put(Dict.WORKLEADER, workleader);
            resultUnit.put(Dict.NAME, String.valueOf(queryGroupDetailById(unit.get(Dict.GROUPID)).get(Dict.NAME)));
            resultUnit.put(Dict.FULLNAME,String.valueOf(queryGroupDetailById(unit.get(Dict.GROUPID)).get(Dict.FULLNAME)));
            String groupleader = unit.get(Dict.GROUPLEADER);
            List<Map<String, String>> groupleaders = new ArrayList<>();
            String ensString = "";
            if(!Util.isNullOrEmpty(groupleader)){
				String[] leaders = groupleader.split(",");
				List<String> ens = new ArrayList<>();
				for(String en : leaders){
					Map groupleaderUnit = new HashMap();
					Map leaderData = queryUserDetailByNameEn(en);
					String leaderCn = "";
					if(!Util.isNullOrEmpty(leaderData)){
						leaderCn = String.valueOf(leaderData.get(Dict.USER_NAME_CN));
					}else{
						leaderCn = en;
					}
					groupleaderUnit.put(Dict.USER_NAME_EN, en);
					groupleaderUnit.put(Dict.USER_NAME_CN, leaderCn);
					groupleaders.add(groupleaderUnit);
				}
			}
            resultUnit.put(Dict.GROUPLEADER, groupleaders);
			String uatCn = "";
			Map uat = new HashMap();
            if(!Util.isNullOrEmpty(unit.get(Dict.UATCONTACT))){
				Map uatData = queryUserDetailByNameEn(unit.get(Dict.UATCONTACT));
				if(!Util.isNullOrEmpty(uatData)){
					uatCn = String.valueOf(uatData.get(Dict.USER_NAME_CN));
				}else{
					uatCn = unit.get(Dict.UATCONTACT);
				}
				uat.put(Dict.USER_NAME_EN, unit.get(Dict.UATCONTACT));
				uat.put(Dict.USER_NAME_CN, uatCn);
			}
			resultUnit.put(Dict.UATCONTACT, uat);
            //安全测试组长
			String securityLeader = unit.get(Dict.SECURITYLEADER);
			List<Map<String, String>> securityLeaders = new ArrayList<>();
			if(!Util.isNullOrEmpty(securityLeader)){
				String[] leaders = securityLeader.split(",");
				for(String en : leaders){
					Map securityLeaderUnit = new HashMap();
					Map leaderData = queryUserDetailByNameEn(en);
					String leaderCn = "";
					if(!Util.isNullOrEmpty(leaderData)){
						leaderCn = String.valueOf(leaderData.get(Dict.USER_NAME_CN));
					}else{
						leaderCn = en;
					}
					securityLeaderUnit.put(Dict.USER_NAME_EN, en);
					securityLeaderUnit.put(Dict.USER_NAME_CN, leaderCn);
					securityLeaders.add(securityLeaderUnit);
				}
			}
			resultUnit.put(Dict.SECURITYLEADER, securityLeaders);
            result.add(resultUnit);
        }
		return result;
	}

	@Override
	public Map queryUserCoreDataById(String id) throws Exception {
		Map sendMap = new HashMap();
		sendMap.put(Dict.REST_CODE, "fdev.user.core.query");
		sendMap.put(Dict.ID, id);
		List<Map> users = (List<Map>)restTransport.submitSourceBack(sendMap);
		if(MyUtils.isEmpty(users)){
            logger.error("fail to query fdev user :" + id);
		    throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
		}
		return users.get(0);
	}

	@Override
	public Map syncLoginFtms(String token) throws Exception {
		//根据token获取fdev用户信息
	    Map send = new HashMap();
		send.put(Dict.AUTHORIZATION, token);
		send.put(Dict.REST_CODE,"get.user.url");
        List<Map> result = (List<Map> )restTransport.submitSourceBack(send);
        Map user = null;
        if(!Util.isNullOrEmpty(result)) {
            //缓存用户信息
            user = result.get(0);
            String redis_token = new StringBuilder(Constants.REDIS_USER_TOKEN).append(token).toString();
            Map map = MyUtils.beanToMap(user);
            user.put(Dict.USERTOKEN, token);
            redisTemplate.opsForValue().set(redis_token, map,7,TimeUnit.DAYS);
        }
        //返回用户信息
        return user;
	}

	@Override
	public Map<String, Map> queryUserByNameEns(List<String> nameEns) {
		Map<String, Object> param = new HashMap<String, Object>(){{
			put(Dict.USERNAMEENS, nameEns);
			put(Dict.REST_CODE, "queryByUserCoreData");
		}};
		Map<String, Map> userInfoMap = new HashMap<>();
		try {
			userInfoMap = (Map<String, Map>) restTransport.submitSourceBack(param);
		} catch (Exception e) {
			logger.info(">>>>>>>>>queryUserByNameEns fail,{}", e.getMessage());
		}
		return userInfoMap;
	}

	@Override
	public Map<String, String> queryGroupNameByIds(List<String> groupIds) throws Exception {
		List<Map> groupInfos = queryGroupByIds(groupIds);
		Map<String,String> groupNameMap = new HashMap<>();
		if(!Util.isNullOrEmpty(groupInfos)) {
			for (Map<String,String> groupInfo : groupInfos) {
				groupNameMap.put(groupInfo.get(Dict.ID), groupInfo.get(Dict.NAME));
			}
		}
		return groupNameMap;
	}

	@Override
	public List<Map> queryGroupByIds(List<String> groupIds) throws Exception {
		Map<String, Object> param = new HashMap<>();
		param.put(Dict.GROUPIDS, groupIds);
		param.put(Dict.REST_CODE, "queryGroupByIds");
		return (List<Map>) restTransport.submitSourceBack(param);
	}
}
