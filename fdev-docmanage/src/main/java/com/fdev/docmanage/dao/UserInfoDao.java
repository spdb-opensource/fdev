package com.fdev.docmanage.dao;

import com.fdev.docmanage.entity.UserInfo;

import java.util.List;

public interface UserInfoDao {
	
	UserInfo addUserInfo(UserInfo userInfo) throws Exception;
	
	UserInfo queryUserInfo(String fileId) throws Exception;

	List<UserInfo> queryUserInfoByUploadStage(String uploadStage, String parentId) throws Exception;

	void updateUserOperation(String fileId, String uploadStage);
}
