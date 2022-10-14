package com.auto.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auto.dao.UserMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.User;
import com.auto.service.IUserService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

@Service
public class UserServiceImpl implements IUserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void addUser(Map<String, String> map) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        String userName = map.getOrDefault(Dict.USERNAME, "");
        String queryPwd = map.getOrDefault(Dict.QUERYPWD, "");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        User user = new User(userName, queryPwd, "0", time, time,  userNameEn);
        List<User> uList = userMapper.queryUserByName(userName, null);
    	if(uList.size() == 0){
	        try {
	        	userMapper.addUser(user);
	        } catch (Exception e) {
	            logger.error("fail to create user");
	            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
	        }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"用户名已存在"} );
    	}
    }

    @Override
    public List<User> queryUser(Map<String, String> map) throws Exception {
        List<User> result = new ArrayList<>();
        String userName = map.getOrDefault(Dict.USERNAME, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            result = userMapper.queryUser(userName, valid);
        } catch (Exception e) {
            logger.error("fail to query user");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return result;
    }

    @Override
    public void deleteUser(Map map) throws Exception {
        List<String> userIds = (List<String>)map.get(Dict.USERID);
        String userId = "'" + String.join("','", userIds)+"'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
        	userMapper.deleteUser(userId, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete user");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateUser(Map map) throws Exception {
        String userId = String.valueOf(map.get(Dict.USERID));
        String userName = String.valueOf(map.getOrDefault(Dict.USERNAME, ""));
        String queryPwd = String.valueOf(map.getOrDefault(Dict.QUERYPWD, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<User> uList = userMapper.queryUserByName(userName, userId);
    	if(uList.size() == 0){
	        try {
	            userMapper.updateUser(userId, userName, queryPwd, userNameEn, time);
	        } catch (Exception e) {
	            logger.error("fail to update user");
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"用户名已存在"} );
    	}
    }
}
