package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.*;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.IFileService;
import com.spdb.fdev.release.service.IGitlabService;
import com.spdb.fdev.release.service.IProdAssetService;
import com.spdb.fdev.release.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
@RefreshScope
public class ProdConfigDaoImpl implements IProdConfigDao {

	private static final Logger logger = LoggerFactory.getLogger(ProdConfigDaoImpl.class);

    @Value("${scripts.path}")
    private String scripts_path;
    @Value("${upload.local.tempdir}")
	private String tempDir;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private IUserService userService;
	@Autowired
	private IProdApplicationDao prodApplicationDao;
	@Autowired
	private IProdTemplateDao prodTemplateDao;
	@Autowired
	private IGitlabService gitlabService;
    @Autowired
    @Lazy
    private IProdAssetService prodAssetService;

    @Autowired
    private IGroupAbbrDao groupAbbrDao;
    @Autowired
	private IFileService fileService;

	/**
	 * 根据投产点名称查询配置文件变更应用
	 * @param release_node_name
	 * @return List<ProdConfigApp>
	 * @throws Exception
	 */
	@Override
	public List<ProdConfigApp> queryConfigApplication(String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
		return mongoTemplate.find(query, ProdConfigApp.class);
	}

	/**
	 * 根据应用id和投产点名称查询配置文件变更应用
	 * @param application_id
	 * @param release_node_name
	 * @return
	 */
	@Override
	public ProdConfigApp queryConfigs(String application_id, String release_node_name) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.APPLICATION_ID).is(application_id));
		return mongoTemplate.findOne(query, ProdConfigApp.class);
	}

	/**
	 * 更新配置变更应用表实体属性
	 * @param prodConfigApp
	 * @return
	 */
	@Override
	public ProdConfigApp updateConfig(ProdConfigApp prodConfigApp) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(prodConfigApp.getRelease_node_name()).
				and(Dict.APPLICATION_ID).is(prodConfigApp.getApplication_id()));
		Update update = Update.update(Dict.CONFIG_TYPE, prodConfigApp.getConfig_type()).set(Dict.DATE, prodConfigApp.getDate());
		mongoTemplate.findAndModify(query, update, ProdConfigApp.class);
		return mongoTemplate.findOne(query, ProdConfigApp.class);
	}

	/**
	 * 通过投产点点名称、应用ID集合查询
	 * @param release_node_name
	 * @param applications
	 * @return
	 */
	@Override
	public List<ProdConfigApp> queryConfigList(String release_node_name, List<String> applications) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.APPLICATION_ID).in(applications));
		List<ProdConfigApp> prodConfigApp = this.mongoTemplate.find(query, ProdConfigApp.class);
		return prodConfigApp;
	}

	/**
	 * 更新配置变更应用表配置文件生成状态以及配置文件TAG
	 * @param prodConfigApp
	 * @return
	 */
	@Override
	public ProdConfigApp updateStatus(ProdConfigApp prodConfigApp) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(prodConfigApp.getRelease_node_name()).
				and(Dict.APPLICATION_ID).is(prodConfigApp.getApplication_id()));
		Update update = Update.update(Dict.STATUS, prodConfigApp.getStatus()).set(Dict.TAG, prodConfigApp.getTag());
		mongoTemplate.findAndModify(query, update, ProdConfigApp.class);
		return mongoTemplate.findOne(query, ProdConfigApp.class);
	}

	/**
	 * 删除配置文件变更应用
	 * @param release_node_name
	 * @param application_id
	 */
	@Override
	public void deleteConfig(String release_node_name, String application_id) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.APPLICATION_ID).is(application_id));
		mongoTemplate.findAndRemove(query, ProdConfigApp.class);
	}

	@Override
	public ProdConfigApp addConfigApplication(ProdConfigApp prodConfigApp) {
		return mongoTemplate.save(prodConfigApp);
	}

	/**
	 * 修改审核状态。
	 * @param release_node_name
	 * @param application_id
	 * @param ischeck
	 * @return
	 */
	@Override
	public ProdConfigApp checkConfigApplication(String release_node_name, String application_id,String ischeck) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.APPLICATION_ID).is(application_id));
		Update update = Update.update(Dict.ISCHECK, ischeck);
		mongoTemplate.findAndModify(query, update, ProdConfigApp.class);
		return mongoTemplate.findOne(query, ProdConfigApp.class);
	}


}
