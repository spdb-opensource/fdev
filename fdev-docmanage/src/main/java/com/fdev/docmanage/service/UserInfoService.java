package com.fdev.docmanage.service;

import java.util.List;
import java.util.Map;

import com.fdev.docmanage.entity.FileInfo;
import com.fdev.docmanage.entity.UserInfo;

public interface UserInfoService {
	//添加用户操作信息
	void addUserInfo(Map<String,String> userOperationInfo) throws Exception;
	
	List<UserInfo> queryUserInfo(Map map)throws Exception;

	List<UserInfo> queryUserInfoByUploadStage(String uploadStage, String parentId) throws Exception;

	void updateUserOperation(String fileId, String uploadStage);
}
