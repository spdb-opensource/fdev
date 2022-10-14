package com.spdb.fdev.release.dao.impl;

import java.util.*;

import javax.annotation.Resource;

import com.spdb.fdev.base.dict.DeployTypeEnum;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.entity.ProdApplication;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseApplication;

@Repository
public class ProdApplicationDaoImpl implements IProdApplicationDao {

	@Resource
	private MongoTemplate mongoTemplate;

	public ProdApplication queryApplication(String prod_id, String application_id) throws Exception {
		//查询变更应用
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		query.fields().exclude(Dict.OBJECTID);
		return mongoTemplate.findOne(query, ProdApplication.class);
	}

	@Override
	public List<ProdApplication> queryApplications(ProdApplication prodApplication) {
        Criteria criteria = Criteria.where(Dict.PROD_ID).is(prodApplication.getProd_id());
        if(!CommonUtils.isNullOrEmpty(prodApplication.getRelease_type())) {
            criteria.and(Dict.RELEASE_TYPE).is(prodApplication.getRelease_type());
        }
		Query query = new Query(criteria);
		return mongoTemplate.find(query, ProdApplication.class);
	}

	@Override
	public ProdApplication addApplication(ProdApplication prodApplication) {
		return mongoTemplate.save(prodApplication);
	}

	@Override
	public void deleteApplication(String prod_id, String application_id) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		mongoTemplate.findAllAndRemove(query, ProdApplication.class);
	}

	@Override
	public ProdApplication setApplicationTemplate(String prod_id, String application_id, String template_id) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = Update.update(Dict.TEMPLATE_ID, template_id);
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
		return mongoTemplate.findOne(query, ProdApplication.class);
	}

	@Override
	public ProdApplication setImageUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri, String fake_image_uri) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = new Update();
		if(!CommonUtils.isNullOrEmpty(pro_image_uri)){
			update = update.set(Dict.PRO_IMAGE_URI, pro_image_uri);
		}
		if(!CommonUtils.isNullOrEmpty(pro_scc_image_uri)){
			update = update.set(Dict.PRO_SCC_IMAGE_URI, pro_scc_image_uri);
		}
		update.set(Dict.FAKE_IMAGE_URI, fake_image_uri);
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
		return mongoTemplate.findOne(query, ProdApplication.class);
	}

	@Override
	public void deleteApplicationByNode(String release_node_name, String application_id) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		List<ProdRecord> prodRecords = mongoTemplate.find(query, ProdRecord.class);
		for (ProdRecord prodRecord : prodRecords) {
			Query queryApp = new Query(Criteria.where(Dict.PROD_ID)
					.is(prodRecord.getProd_id())
					.and(Dict.APPLICATION_ID)
					.is(application_id));
			mongoTemplate.findAndRemove(queryApp, ProdApplication.class);
		}
	}

	@Override
	public List<ReleaseApplication> queryAppWithOutSum(String prod_assets_version, String release_node_name) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		List<ReleaseApplication> releaseApplications = mongoTemplate.find(query, ReleaseApplication.class);
		Query querysum = new Query(Criteria.where(Dict.PROD_ASSETS_VERSION).is(prod_assets_version));
		List<ProdRecord> prodRecords = mongoTemplate.find(querysum, ProdRecord.class);
		Set<ProdApplication> appset= new HashSet<>();
		for (ProdRecord prodRecord : prodRecords) {
			Query queryprod = new Query(Criteria.where(Dict.PROD_ID).is(prodRecord.getProd_id()));
			List<ProdApplication> apps = mongoTemplate.find(queryprod, ProdApplication.class);
			appset.addAll(apps);
		}
		Iterator<ReleaseApplication> iterator = releaseApplications.iterator();
		while(iterator.hasNext()) {
			ReleaseApplication releaseApplication = iterator.next();
			for (ProdApplication prodApplication : appset) {
				if((releaseApplication.getApplication_id().equals(prodApplication.getApplication_id()))) {
					iterator.remove();
					break;
				}
			}
		}
		return releaseApplications;
	}

	@Override
	public Set<ProdApplication> queryAppBySum(String prod_assets_version) {
		Query query = new Query (Criteria.where( Dict.PROD_ASSETS_VERSION).and(prod_assets_version));
		List<ProdRecord> prodRecords = mongoTemplate.find(query, ProdRecord.class);
		Set<ProdApplication> set = new HashSet<>();
		for (ProdRecord prodRecord : prodRecords) {
			Query queryApp = new Query(Criteria.where(Dict.PROD_ID).is(prodRecord.getProd_id()));
			List<ProdApplication> prodApplications = mongoTemplate.find(queryApp, ProdApplication.class);
			set.addAll(prodApplications);
		}
		return set;
	}

	@Override
	public void deleteAppByProd(String prod_id) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		mongoTemplate.findAndRemove(query, ProdApplication.class);
	}

    @Override
    public ProdApplication setConfigUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri, boolean fdev_config_changed, String compare_url, String fdev_config_confirm, String new_tag) {

        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
        Update update = Update.update(Dict.FDEV_CONFIG_CONFIRM, fdev_config_confirm)
                .set(Dict.FDEV_CONFIG_CHANGED, fdev_config_changed)
                .set(Dict.COMPARE_URL, compare_url).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"))
        		.set(Dict.TAG, new_tag);
        if(!CommonUtils.isNullOrEmpty(pro_image_uri)) {
			update.set(Dict.PRO_IMAGE_URI, pro_image_uri);
		}
        if(!CommonUtils.isNullOrEmpty(pro_scc_image_uri)) {
			update.set(Dict.PRO_SCC_IMAGE_URI, pro_scc_image_uri);
		}
        mongoTemplate.findAndModify(query, update, ProdApplication.class);
        return mongoTemplate.findOne(query, ProdApplication.class);
    }

    @Override
	public ProdApplication updateApplication(ProdApplication prodApplication) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prodApplication.getProd_id()).and(Dict.APPLICATION_ID).is(prodApplication.getApplication_id()));
		Update update = Update.update(Dict.STATUS, prodApplication.getStatus()).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		return mongoTemplate.findAndModify(query, update, ProdApplication.class);
	}

	/**
	 * 根据应用id查询上一次发布的镜像uri
	 * @param application_id 应用id
	 * @param prodRecord 本次变更
	 * @param deployType 部署平台
	 * @return
	 */
	@Override
	public String findLastReleaseUri(String application_id, ProdRecord prodRecord, String deployType) {
		Map<String,Object> map = findLastReleaseInfo(application_id,prodRecord, DeployTypeEnum.getUri(deployType),false);
		String uri = null;
		if (!CommonUtils.isNullOrEmpty(map)) {
			uri = (String) map.get(DeployTypeEnum.getUri(deployType));
		}
		return uri;
	}

	@Override
	public String findLatestUri(String application_id, ProdRecord prodRecord,String deployType) {
		Map<String, Object> map = findLastTag(application_id, prodRecord, DeployTypeEnum.getUri(deployType), true);
		String tag = null;
		if (!CommonUtils.isNullOrEmpty(map)) {
			tag = (String) map.get("tag");
			if (CommonUtils.isNullOrEmpty(tag)) {
				if ("caas".equals(deployType)) {
					String proImageUrl = (String) map.get("pro_image_uri");
					tag = proImageUrl.split(":")[1];
				} else if ("scc".equals(deployType)) {
					String proImageUriSCC = (String) map.get("pro_scc_image_uri");
					tag = proImageUriSCC.split(":")[1];
				} 
			}
		}
		return tag;
	}

	@Override
	public String findLastReleaseTag(String application_id, ProdRecord prodRecord,String deployType) {
		Map<String,Object> map = findLastReleaseInfo(application_id,prodRecord,DeployTypeEnum.getUri(deployType),true);
		String res = null;
		if (!CommonUtils.isNullOrEmpty(map)) {
			res = (String) map.get(DeployTypeEnum.getUri(deployType));
		}
		return res;
	}

	private Map<String,Object> findLastReleaseInfo(String application_id, ProdRecord prodRecord, String uriField,boolean fdevConfigChanged){
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		Criteria dateCriteria = new Criteria().orOperator(Criteria.where("prod_record.date").is(prodRecord.getDate())
						.and("prod_record.plan_time").lt(prodRecord.getPlan_time()),
				new Criteria("prod_record.date").lt(prodRecord.getDate()));
		Criteria criteria = new Criteria(uriField).ne(null)
				.and(Dict.APPLICATION_ID).is(application_id)
				.and("prod_record.auto_release_stage").is("7")
				.and("prod_record.type").is(prodRecord.getType());
		if(fdevConfigChanged){
			criteria.and(Dict.FDEV_CONFIG_CHANGED).is(true);
		}
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria, dateCriteria));
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				// 查询条件 取第一个
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		if (!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			return docs.getMappedResults().get(0);
		}
		return null;
	}

	@Override
	public List<ProdApplication> findUriByApplicationId(String application_id) {
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		// 在变更应用中查找有镜像，与本次应用相同，自动化发布阶段为介质准备完毕
		Criteria criteria = new Criteria().and("pro_image_uri").exists(true).and(Dict.APPLICATION_ID).is(application_id)
				.and(Dict.FDEV_CONFIG_CHANGED).is(true).and("prod_record.auto_release_stage").is("7");
		// 变更记录表与变更应用表联合查询
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria));
		// 按照变更记录的日期倒序排列
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				// 查询条件 取第一个
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		List<ProdApplication> list = new ArrayList<>();
		// 不为空则取出tag分支名称
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			for(Map map : docs.getMappedResults()) {
				ProdApplication prodApplication = new ProdApplication();
				prodApplication.setPro_image_uri((String) map.get("pro_image_uri"));
				prodApplication.setProd_id((String) map.get(Dict.PROD_ID));
				list.add(prodApplication);
			}
		}
		return list;
	}

	//获取当前日期之前上一次投产中的配置文件tag，如果没有则获取上一次投产镜像标签
	@Override
	public String findLatestTag(String application_id,String deployType) {
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		Criteria dateCriteria = new Criteria("prod_record.date").lte(CommonUtils.formatDate(CommonUtils.INPUT_DATE));
		Criteria criteria = new Criteria().and(DeployTypeEnum.getUri(deployType)).exists(true).and(Dict.APPLICATION_ID).is(application_id)
				.and(Dict.FDEV_CONFIG_CHANGED).is(true).and("prod_record.auto_release_stage").is("7");
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria, dateCriteria));
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				// 查询条件 取第一个
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		String old_uri = "";
		if (!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			if(CommonUtils.isNullOrEmpty(old_uri)){
				old_uri = (String) docs.getMappedResults().get(0).get(DeployTypeEnum.getUri(deployType));
			}
		}
		return old_uri;
	}

	@Override
	public String findLastTagByApplicationId(String application_id) {
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		// 在变更应用中查找有镜像，与本次应用相同，自动化发布阶段为介质准备完毕
		Criteria criteria = new Criteria().orOperator(new Criteria("pro_image_uri").exists(true),new Criteria("pro_scc_image_uri").exists(true))
				.and(Dict.APPLICATION_ID).is(application_id)
				.and(Dict.FDEV_CONFIG_CHANGED).is(true).and("prod_record.auto_release_stage").is("7");
		// 变更记录表与变更应用表联合查询
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria));
		// 按照变更记录的日期倒序排列
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				// 查询条件 取第一个
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		String tag = "";
		// 不为空则取出tag分支名称
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			String proImageUri = (String) docs.getMappedResults().get(0).get("pro_image_uri");
			String proSccImageUri = (String) docs.getMappedResults().get(0).get("pro_scc_image_uri");
			if(CommonUtils.isNullOrEmpty(proImageUri)){
				tag = proSccImageUri.split(":")[1];
			}else{
				tag = proImageUri.split(":")[1];
			}
		}
		return tag;
	}

	@Override
	public String findImageUriByReleaseNodeName(String application_id, String release_node_name) {
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		// 在变更应用中查找有镜像，与本次应用相同，自动化发布阶段为介质准备完毕
		Criteria criteria = new Criteria().orOperator(new Criteria("pro_image_uri").exists(true),new Criteria("pro_scc_image_uri").exists(true))
				.and(Dict.APPLICATION_ID).is(application_id)
				.and("prod_record.release_node_name").ne(release_node_name).and("prod_record.auto_release_stage").is("7");
		// 变更记录表与变更应用表联合查询
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria));
		// 按照变更记录的日期倒序排列
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				// 查询条件 取第一个
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		String tag = "";
		// 不为空则取出tag分支名称
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			String proImageUri = (String) docs.getMappedResults().get(0).get("pro_image_uri");
			String proSccImageUri = (String) docs.getMappedResults().get(0).get("pro_scc_image_uri");
			if(CommonUtils.isNullOrEmpty(proImageUri)){
				tag = proSccImageUri.split(":")[1];
			}else{
				tag = proImageUri.split(":")[1];
			}
		}
		return tag;
	}

	@Override
	public ProdApplication setImageConfigUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri,
								  boolean fdev_config_changed, String compare_url,
											 String fdev_config_confirm, String fake_image_uri,String tag) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = Update.update(Dict.FDEV_CONFIG_CONFIRM, fdev_config_confirm)
				.set(Dict.FDEV_CONFIG_CHANGED, fdev_config_changed)
				.set(Dict.COMPARE_URL, compare_url).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		if("1".equals(fdev_config_confirm)) {
			update.set(Dict.PRO_IMAGE_URI, pro_image_uri).set(Dict.PRO_SCC_IMAGE_URI,pro_scc_image_uri).set(Dict.FAKE_IMAGE_URI, fake_image_uri).set(Dict.TAG, tag);
		} else {
			update.set(Dict.PRO_IMAGE_URI, null).set(Dict.PRO_SCC_IMAGE_URI,null).set(Dict.FAKE_IMAGE_URI, "");
		}
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
		return mongoTemplate.findOne(query, ProdApplication.class);
	}

	@Override
	public ProdApplication setImageConfigConfirmUri(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri,
													String fdev_config_confirm, String fake_image_uri) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = Update.update(Dict.FDEV_CONFIG_CONFIRM, fdev_config_confirm)
                .set(Dict.FAKE_IMAGE_URI, fake_image_uri)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		    if(!CommonUtils.isNullOrEmpty(pro_image_uri)){
				update.set(Dict.PRO_IMAGE_URI, pro_image_uri);
            }
		    if(!CommonUtils.isNullOrEmpty(pro_scc_image_uri)){
				update.set(Dict.PRO_SCC_IMAGE_URI, pro_scc_image_uri);
			}
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
		return mongoTemplate.findOne(query, ProdApplication.class);
	}

	@Override
	public String queryLastTagByGitlabId(String application_id, String prod_id, String type, String release_node_name) {
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		// 在变更应用中查找有镜像，自动化发布阶段为介质准备完毕
		Criteria criteria = new Criteria().orOperator(new Criteria("pro_image_uri").exists(true),new Criteria("pro_scc_image_uri").exists(true))
				.and(Dict.APPLICATION_ID).is(application_id)
				.and("prod_record.release_node_name").ne(release_node_name)
				.and("prod_record.auto_release_stage").is("7");
		if(!CommonUtils.isNullOrEmpty(type)) {
			criteria.and("prod_record.type").is(type);
		}
		if(!CommonUtils.isNullOrEmpty(prod_id)) {
			criteria.and("prod_record.prod_id").ne(prod_id);
		}
		// 变更记录表与变更应用表联合查询
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria));
		// 按照变更记录的日期倒序排列
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				// 查询条件 取第一个
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		String tag = "";
		// 不为空则取出tag分支名称
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			String proImageUri = (String) docs.getMappedResults().get(0).get("pro_image_uri");
			String proSccImageUri = (String) docs.getMappedResults().get(0).get("pro_scc_image_uri");
			if(CommonUtils.isNullOrEmpty(proImageUri)){
				tag = proSccImageUri.split(":")[1];
			}else{
				tag = proImageUri.split(":")[1];
			}
		}
		return tag;
	}

	@Override
	public List<ProdApplication> queryImages(String application_id, List<String> prodIds) {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(application_id).and(Dict.PROD_ID).in(prodIds));
		return mongoTemplate.find(query, ProdApplication.class);
	}

    @Override
    public List<ProdApplication> queryByProdId(String prod_id) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
        return mongoTemplate.find(query, ProdApplication.class);
    }

    @Override
    public List<ProdApplication> queryByApplicationId(String application_id) {
        Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(application_id));
        return mongoTemplate.find(query, ProdApplication.class);
    }

	@Override
	public ProdApplication setPackageTag(String prod_id, String application_id, String pro_package_uri) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = Update.update(Dict.PRO_PACKAGE_URI, pro_package_uri);
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
		return mongoTemplate.findOne(query, ProdApplication.class);
	}

	@Override
	public void updateProdDir(String prd_id, String application_id, List<String> prod_dir, List<String> deploy_type) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prd_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = Update.update(Dict.PROD_DIR, prod_dir)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"))
				.set(Dict.DEPLOY_TYPE, deploy_type);
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
	}

	@Override
	public ProdApplication queryByTag(String prod_id, String application_id, String tag) {
		String uri = ".*?" + tag + ".*";
		Query query = Query.query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id)
				.and(Dict.PRO_IMAGE_URI).regex(uri));
		return mongoTemplate.findOne(query, ProdApplication.class);
	}

	@Override
	public void updateProdChange(String prd_id, String application_id, Map<String, Object> changeList) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prd_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = Update.update(Dict.CHANGE, changeList).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
	}

	// 获取上一次部署的平台
	@Override
	public List<String> queryOldDeployTypeByAppId(String application_id, String prod_id, String type) {
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		Criteria criteria = new Criteria().orOperator(new Criteria("pro_image_uri").exists(true),new Criteria("pro_scc_image_uri").exists(true))
				.and(Dict.APPLICATION_ID).is(application_id)
				.and("prod_record.auto_release_stage").is("7");
		if(!CommonUtils.isNullOrEmpty(type)) {
			criteria.and("prod_record.type").is(type);
		}
		if(!CommonUtils.isNullOrEmpty(prod_id)) {
			criteria.and("prod_record.prod_id").ne(prod_id);
		}
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria));
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		List<String> deployType = new ArrayList<>();
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			deployType = (List<String>) docs.getMappedResults().get(0).get(Dict.DEPLOY_TYPE);
		}
		return deployType;
	}

	
	// 查询之前所有部署过的平台
	@Override
	public HashSet<String> findAllDeployedType(String application_id, ProdRecord prodRecord) {
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		Criteria criteria = new Criteria().orOperator(new Criteria("pro_image_uri").exists(true),new Criteria("pro_scc_image_uri").ne(null))
				.and(Dict.APPLICATION_ID).is(application_id)
				.and("prod_record.auto_release_stage").is("7")
				.and("prod_record.type").is(prodRecord.getType());
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria));
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		HashSet<String> set = new HashSet<String>();
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			List<Map> resultList = docs.getMappedResults();
			resultList.forEach(prod -> {
				set.addAll((List<String>)prod.get(Dict.DEPLOY_TYPE));
			});
		}
		return set;
	}

	@Override
	public void updateProdDirs(ProdApplication prodApplication) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prodApplication.getProd_id()).and(Dict.APPLICATION_ID).is(prodApplication.getApplication_id()));
		Update update = new Update();
		update.set(Dict.PROD_DIR,prodApplication.getProd_dir());
		update.set(Dict.TAG,prodApplication.getTag());
		update.set("esf_flag",prodApplication.getEsf_flag());
		update.set("esf_platform",prodApplication.getEsf_platform());
		update.set(Dict.DEPLOY_TYPE,prodApplication.getDeploy_type());
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
	}

	@Override
	public void updateEsfFlag(ProdApplication prodApplication) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prodApplication.getProd_id()).and(Dict.APPLICATION_ID).is(prodApplication.getApplication_id()));
		Update update = new Update();
		update.set("esf_flag",prodApplication.getEsf_flag());
		update.set("esf_platform",prodApplication.getEsf_platform());
		update.set(Dict.DEPLOY_TYPE,prodApplication.getDeploy_type());
		update.set(Dict.PROD_DIR,prodApplication.getProd_dir());
		update.set(Dict.CHANGE,prodApplication.getChange());
		update.set(Dict.TAG,prodApplication.getTag());
		mongoTemplate.findAndModify(query, update, ProdApplication.class);
	}

	@Override
	public void updateProdStopEnv(String prd_id, String application_id, List<String> caas_stop_env, List<String> scc_stop_env) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prd_id).and(Dict.APPLICATION_ID).is(application_id));
		Update update = new Update();
		update.set("caas_stop_env",caas_stop_env);
		update.set("scc_stop_env",scc_stop_env);
		mongoTemplate.findAndModify(query,update,ProdApplication.class);
	}

	/**
	 * 查询日期小于投产窗口日期的投产镜像
	 * */
	private Map<String,Object> findLastTag(String application_id, ProdRecord prodRecord, String uriField,boolean fdevConfigChanged){
		AggregationOperation recordlookup = Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		Criteria dateCriteria = new Criteria().orOperator(Criteria.where("prod_record.date").lt(prodRecord.getDate())
						.and("prod_record.plan_time").lt(prodRecord.getPlan_time()),
				new Criteria("prod_record.date").lt(prodRecord.getDate()));
		Criteria criteria = new Criteria(uriField).ne(null)
				.and(Dict.APPLICATION_ID).is(application_id) 
				.and("prod_record.auto_release_stage").is("7")
				.and("prod_record.type").is(prodRecord.getType());
		if(fdevConfigChanged){
			criteria.and(Dict.FDEV_CONFIG_CHANGED).is(true);
		}
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria, dateCriteria));
		Sort sort = new Sort(Sort.Direction.DESC, "prod_record.date", "prod_record.plan_time");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				// 查询条件 取第一个
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		if (!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			return docs.getMappedResults().get(0);
		}
		return null;
	}
}
