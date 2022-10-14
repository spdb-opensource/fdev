package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.ISystemReleaseInfoDao;
import com.spdb.fdev.release.entity.SystemReleaseInfo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 变更参数数据处理层
 */
@Repository
public class SystemReleaseInfoDaoImpl implements ISystemReleaseInfoDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public SystemReleaseInfo querySysRlsDetail(String system_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.SYSTEM_ID).is(system_id));
		return mongoTemplate.findOne(query, SystemReleaseInfo.class);
	}

	@Override
	public SystemReleaseInfo save(SystemReleaseInfo systemReleaseInfo) throws Exception {
		try {
			return this.mongoTemplate.save(systemReleaseInfo);
		} catch (DuplicateKeyException e) {
			if (e.getMessage().indexOf("system_abbr") >= 0) {
				throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "系统缩写不能重复!" });
			}
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR);
		}
	}

	@Override
	public SystemReleaseInfo update(SystemReleaseInfo systemReleaseInfo) throws Exception {

		Query query = new Query(Criteria.where(Dict.SYSTEM_ID).is(systemReleaseInfo.getSystem_id()));
		Update update = Update.update(Dict.RESOURCE_GITURL, systemReleaseInfo.getResource_giturl())
				.set(Dict.RESOURCE_PROJECTID, systemReleaseInfo.getResource_projectid());
		try {
			mongoTemplate.findAndModify(query, update, SystemReleaseInfo.class);
		} catch (DuplicateKeyException e) {
			if (e.getMessage().indexOf("system_abbr") >= 0) {
				throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "系统缩写不能重复!" });
			}
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR);
		}
		return mongoTemplate.findOne(query, SystemReleaseInfo.class);
	}

	@Override
	public SystemReleaseInfo updateSysRlsInfo(String system_id, String resouce_giturl) throws Exception {
		Query query = new Query(Criteria.where(Dict.SYSTEM_ID).is(system_id));
		Update update = Update.update(Dict.RESOURCE_GITURL, resouce_giturl);
		mongoTemplate.upsert(query, update, SystemReleaseInfo.class);
		return mongoTemplate.findOne(query, SystemReleaseInfo.class);
	}

	@Override
	public SystemReleaseInfo querySysInfoByConfigType(String configType) throws Exception {
		Query query = new Query(Criteria.where(Dict.CONFIG_TYPE).is(configType));
		return mongoTemplate.findOne(query, SystemReleaseInfo.class);
	}

}
