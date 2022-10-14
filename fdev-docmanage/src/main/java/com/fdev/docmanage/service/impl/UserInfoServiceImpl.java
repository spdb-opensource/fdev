package com.fdev.docmanage.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fdev.docmanage.dao.UserInfoDao;
import com.fdev.docmanage.dict.ErrorConstants;
import com.fdev.docmanage.entity.UserInfo;
import com.fdev.docmanage.service.UserInfoService;
import com.fdev.docmanage.util.CommonUtils;
import com.fdev.docmanage.util.MyUtil;
import com.spdb.fdev.common.exception.FdevException;

@Service
public class UserInfoServiceImpl implements UserInfoService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印
	
	 @Autowired
	 private UserInfoDao userInfoDao;
	
	@Override
	public void addUserInfo(Map<String, String> userOperationInfo) throws Exception {
		Date date = new Date();
		LocalDateTime operation_date = MyUtil.dateToLocalDateTime(MyUtil.getIOSDate(date));
		UserInfo userInfo = new UserInfo();
		userInfo.setUser_name_cn(userOperationInfo.get("user_name_cn"));
		userInfo.setType(userOperationInfo.get("type"));
		userInfo.setUser_name_en(userOperationInfo.get("user_name_en"));
		userInfo.setOperation(userOperationInfo.get("operation"));
		userInfo.setFileName(userOperationInfo.get("fileName"));
		userInfo.setFileId(userOperationInfo.get("fileId"));
		userInfo.setParentId(userOperationInfo.get("parentId"));
		userInfo.setOperation_date(operation_date);
		userInfo.setUploadStage(userOperationInfo.get("uploadStage"));
		//初始化 主键 id
		userInfo.initId();
		UserInfo user_info = userInfoDao.addUserInfo(userInfo);
		if(CommonUtils.isNullOrEmpty(user_info)) {
	         throw new FdevException(ErrorConstants.DATA_ADD_ERROR);
		}
	}
	
	@Override
	public List<UserInfo> queryUserInfo(Map map) throws Exception {
		List<UserInfo> list = new ArrayList<UserInfo>();
		List<String> fileIds = (List<String>) map.get("fileIds");
		if(!CommonUtils.isNullOrEmpty(fileIds)) {
			for(String fileId : fileIds) {
				UserInfo userInfo = userInfoDao.queryUserInfo(fileId);
				list.add(userInfo);
			}
		}
		return list;
	}

	@Override
	public List<UserInfo> queryUserInfoByUploadStage(String uploadStage, String parentId) throws Exception {

		List<UserInfo> userInfo = userInfoDao.queryUserInfoByUploadStage(uploadStage, parentId);
		return userInfo;
	}

	@Override
	public void updateUserOperation(String fileId, String uploadStage) {
		userInfoDao.updateUserOperation(fileId,uploadStage);
	}


}
