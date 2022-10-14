package com.fdev.docmanage.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fdev.docmanage.dao.UserInfoDao;
import com.fdev.docmanage.dict.ErrorConstants;
import com.fdev.docmanage.entity.UserInfo;
import com.spdb.fdev.common.exception.FdevException;

import java.util.List;


@Repository
public class UserInfoDaoImpl implements UserInfoDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private MongoTemplate mongoTemplate;

	@Override
	public UserInfo addUserInfo(UserInfo userOperationInfo) throws Exception {
		UserInfo userInfo;
		try {
			userInfo = mongoTemplate.insert(userOperationInfo);
        }catch (Exception e){
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_ADD_ERROR);
        }
		return userInfo;
	}

	@Override
	public UserInfo queryUserInfo(String fileId) throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where("fileId").is(fileId));
        query.addCriteria(Criteria.where("operation").is("文件上传"));
		return mongoTemplate.findOne(query, UserInfo.class);
	}

	@Override
	public List<UserInfo> queryUserInfoByUploadStage(String uploadStage, String parentId) throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where("uploadStage").is(uploadStage));
		query.addCriteria(Criteria.where("parentId").is(parentId));
		query.addCriteria(Criteria.where("operation").is("文件上传"));
		return mongoTemplate.find(query, UserInfo.class);
	}

	@Override
	public void updateUserOperation(String fileId, String uploadStage) {
		Query query = new Query();
		query.addCriteria(Criteria.where("fileId").is(fileId).and("uploadStage").is(uploadStage));
		Update update = new Update().set("operation","文件重新上传被删除");
		update.set("uploadStage","delete");
		mongoTemplate.updateMulti(query, update, UserInfo.class);
	}

}
