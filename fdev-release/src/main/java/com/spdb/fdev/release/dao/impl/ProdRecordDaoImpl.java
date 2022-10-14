package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.*;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RefreshScope
public class ProdRecordDaoImpl implements IProdRecordDao{

	private static final Logger logger = LoggerFactory.getLogger(ProdRecordDaoImpl.class);

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
    @Autowired
	private ITemplateDocumentDao templateDocumentDao;

    @Override
	public ProdRecord create(ProdRecord releaseRecords) throws Exception {
		try {
			return mongoTemplate.save(releaseRecords);
		}catch(DuplicateKeyException e)
		{
			if(e.getMessage().indexOf(Dict.PROD_SPDB_NO) >= 0)
			{
				throw new FdevException(ErrorConstants.REPET_INSERT_REEOR,new String[] {"变更编号不能重复!"});
			}
			throw  new FdevException(ErrorConstants.REPET_INSERT_REEOR);
		}
	}

	@Override
	public List query(String release_node_name) throws Exception {
		Query query=new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		//按version 倒叙排列
		query.with(Sort.by(Sort.Order.desc(Dict.VERSION)));
		List<ProdRecord> prodRecords = mongoTemplate.find(query, ProdRecord.class);
		for (ProdRecord prodRecord : prodRecords) {
			if (!CommonUtils.isNullOrEmpty(prodRecord.getLauncher())) {
				Map<String, Object> user = userService.queryUserById(prodRecord.getLauncher());
				prodRecord.setLauncher_name_cn(
						CommonUtils.isNullOrEmpty(user) ? "" : (String) user.get(Dict.USER_NAME_CN));
			}
			Map prodMap = new HashMap();
			Map records_map = CommonUtils.beanToMap(prodRecord);
			prodMap.putAll(records_map);
			List<ProdApplication> applications = prodApplicationDao.queryApplications(new ProdApplication(prodRecord.getProd_id()));
			prodMap.put(Dict.APPLICATIONS, applications);
			setProd_assets_version(prodRecord);
		}
		return prodRecords;
	}

	@Override
	public ProdRecord queryDetail(String prod_id) throws Exception {
		Query query=new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		ProdRecord prodRecord = mongoTemplate.findOne(query, ProdRecord.class);
		return setProd_assets_version(prodRecord);
	}
	
	public ProdRecord setProd_assets_version(ProdRecord prodRecord) {
		if(CommonUtils.isNullOrEmpty(prodRecord)) {
			return null;
		}
		Query query = new Query(Criteria.where(Dict.PROD_ID)
				.is(prodRecord.getProd_id()));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		if(CommonUtils.isNullOrEmpty(prodRecord.getProd_assets_version())) {
			String date = prodRecord.getDate().replace("/", "");
			String prod_assets_version = new StringBuilder(date)
					.append("_").append(prodRecord.getProd_spdb_no()).toString(); 
			prodRecord.setProd_assets_version(prod_assets_version);
			Update update = Update.update(Dict.PROD_ASSETS_VERSION,prod_assets_version)
					.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
			mongoTemplate.findAndModify(query, update, ProdRecord.class);
		}
		return prodRecord;
	}

	@Override
	public ProdRecord queryTrace(String prod_id) throws Exception {
		Query query=new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		query.fields().include(Dict.AUTO_RELEASE_STAGE);
		query.fields().include(Dict.AUTO_RELEASE_LOG);
		query.fields().include(Dict.STATUS);
		query.fields().include(Dict.PROD_ID);
		query.fields().include(Dict.PROD_ENV);
		query.fields().exclude(Dict.OBJECTID);
		return	mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public ProdRecord audit(String prod_id, String audit_type, String reject_reason) throws Exception {
		Query query=new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		Update update=Update.update(Dict.STATUS, audit_type).set(Dict.REJECT_REASON, reject_reason)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
		return mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public ProdRecord queryProdRecordByVersion(String release_node_name,String version) throws Exception {
		Query query =new Query(Criteria.where(Dict.VERSION)
				.is(version)
				.and(Dict.RELEASE_NODE_NAME)
				.is(release_node_name));			
		return mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public List queryByTemplateId(String template_id) throws Exception {
		Query query =new Query(Criteria.where(Dict.TEMPLATE_ID).is(template_id));		
		return mongoTemplate.find(query, ProdRecord.class);
	}
	
	@Override
	public ProdRecord setTemplate(String prod_id, String template_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		Update update = Update.update(Dict.TEMPLATE_ID, template_id)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
		return mongoTemplate.findOne(query, ProdRecord.class);
		
	}

	@Override
	public ProdRecord updateAutoReleaseLog(String  prod_id, String auto_release_log) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		Update update = Update.update(Dict.AUTO_RELEASE_LOG, auto_release_log)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
		return mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public ProdRecord updateAutoReleaseStage(String prod_id, String auto_release_stage) throws Exception {
		Query query =new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		Update update=Update.update(Dict.AUTO_RELEASE_STAGE, auto_release_stage)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
		return mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public ProdRecord updateStatus(String prod_id, String status) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		Update update = Update.update(Dict.STATUS, status)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
		return mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public void auditSetTemplate(String prod_id, String audit_type, String reject_reason, String template_properties) {
		Query query=new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		Update update=Update.update(Dict.STATUS, audit_type)
			.set(Dict.REJECT_REASON, reject_reason)
			.set(Dict.TEMPLATE_PROPERTIES, template_properties)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
	}

	@Override
	public Map<String, String> queryBeforePordImages(String release_node_name, String version) throws Exception {
		String[] array = version.split("_");
		String dateString = array[2];
		String timeString = array[3];
		String date = new StringBuilder(dateString.substring(0,4)).append("/")
				.append(dateString.substring(4,6)).append("/")
				.append(dateString.substring(6,8)).toString();
		String time = new StringBuilder(timeString.substring(0,2)).append(":")
				.append(timeString.substring(2,4)).toString();
		Query query = new Query();
		query.addCriteria(new Criteria().and(Dict.RELEASE_NODE_NAME)
				.is(release_node_name)
				.and(Dict.STATUS)
				.is(Constants.PROD_RECORD_STSTUS_READY));
		Criteria[] a = new Criteria[2];
		a[0]= Criteria.where(Dict.DATE).lt(date);
		a[1] = Criteria.where(Dict.DATE).is(date).and(Dict.PLAN_TIME).lt(time);
		query.addCriteria(new Criteria().orOperator(a));
		List<ProdRecord> prodRecords = mongoTemplate.find(query, ProdRecord.class);
		Map<String, String> map=new HashMap<>();
		for (ProdRecord prodRecord : prodRecords) {
			String prod_id = prodRecord.getProd_id();
			Query queryApp =new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
			List<ProdApplication> ProdApplications = mongoTemplate.find(queryApp, ProdApplication.class);
			List<ProdApplicationLegacy> ProdApplicationLegacys = mongoTemplate.find(queryApp, ProdApplicationLegacy.class);
			for (ProdApplication prodApplication : ProdApplications) {
				String pro_image_uri = prodApplication.getPro_image_uri();
				if(!CommonUtils.isNullOrEmpty(pro_image_uri)) {
					map.put(pro_image_uri, prodRecord.getVersion());
				}
				if(!CommonUtils.isNullOrEmpty(prodApplication.getPro_scc_image_uri())){
					map.put(prodApplication.getPro_scc_image_uri(), prodRecord.getVersion());
				}
			}
			for (ProdApplicationLegacy prodApplicationLegacy : ProdApplicationLegacys) {
				String pro_image_uri = prodApplicationLegacy.getPro_image_uri();
				if(!CommonUtils.isNullOrEmpty(pro_image_uri)) {
					map.put(pro_image_uri, prodRecord.getVersion());
				}
			}
		}
		return map;
	}

	@Override
	public void saveHostName(String prod_id, String host_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		Update update = Update.update(Dict.PROD_ENV, host_name)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
	}

	@Override
	public void update(Map<String, String> requestParam) throws Exception {
    	String excel_template_name = requestParam.get(Dict.EXCEL_TEMPLATE_NAME);
    	String excel_template_url = requestParam.get(Dict.EXCEL_TEMPLATE_URL);
		String prod_spdb_no = requestParam.get(Dict.PROD_SPDB_NO);
		String prod_id = requestParam.get(Dict.PROD_ID);
		String plan_time = requestParam.get(Dict.PLAN_TIME);
		//获取变更详情
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
		ProdRecord prodRecord = mongoTemplate.findOne(query, ProdRecord.class);
		String type = prodRecord.getType();
		String system_name = prodRecord.getOwner_system_name();
		// 查询template_document表获取scc所有模板集合
		TemplateDocument templateDocument = templateDocumentDao.getDocument(system_name, type);
		List<String> scc_document_list = templateDocument.getScc_document_list();
		String scc_prod = "0";
		if(scc_document_list.contains(excel_template_name)){  // 是否是SCC变更（1是0否）
			scc_prod = "1";
		}
		//查出要变更的记录当前的变更目录
		ProdRecord oldEarliestProdRecord = this.queryEarliestByProdSpdbNo(prod_spdb_no);
		Update update = Update.update(Dict.PROD_SPDB_NO, prod_spdb_no)
				.set(Dict.EXCEL_TEMPLATE_NAME, excel_template_name).set(Dict.EXCEL_TEMPLATE_URL, excel_template_url)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"))
				.set("scc_prod", scc_prod);
		if(!CommonUtils.isNullOrEmpty(prodRecord)) {
			String date = prodRecord.getDate().replace("/", "");
			String version = prodRecord.getVersion();
			if(!prodRecord.getPlan_time().equals(plan_time)) {
				String time = plan_time.replace(":", "");
				String[] split = prodRecord.getVersion().split("_");
				version = new StringBuilder(split[0])
						.append("_").append(split[1])
						.append("_").append(date)
						.append("_").append(time).toString();
				if(split.length > 4) {
					version = new StringBuilder(version).append(split[4]).toString();
				}
				update.set(Dict.PLAN_TIME, plan_time).set(Dict.VERSION, version);
				//对变更记录修改完成后，统一修改变更目录，此处不做修改。
                List<ProdAsset> prodAssets = prodAssetService.queryAssetsList(prod_id);
                for (ProdAsset prodAsset : prodAssets) {
                    if("1".equals(prodAsset.getSource())){
                        String file_giturl = prodAsset.getFile_giturl();
                        Query prodAssetquery =new Query(Criteria.where(Dict.ID).is(prodAsset.getId()));
                    	String fileDir=tempDir+CommonUtils.getSessionUser().getUser_name_en()+"_"+System.currentTimeMillis();
                    	//下载文件
						CommonUtils.createDirectory(fileDir);
						String filePath=fileDir+"/"+prodAsset.getFileName();
                    	fileService.downloadDocumentFile(filePath,file_giturl,"fdev-release");
                    	//删除文件
						fileService.deleteFiles(file_giturl,"fdev-release");
						file_giturl=file_giturl.replaceAll(prodRecord.getVersion(),version);
						//上传新文件
						fileService.uploadWord(file_giturl,new File(filePath),"fdev-release");
						Update prodAssetupdate =Update.update(Dict.FILE_GITURL, file_giturl);
						mongoTemplate.findAndModify(prodAssetquery, prodAssetupdate, ProdAsset.class);
						CommonUtils.deleteDirectory(new File(fileDir),fileDir);
					}
                }
			}
			mongoTemplate.findAndModify(query, update, ProdRecord.class);
			String abbr = "";
			if(Constants.IMAGE_DELIVER_TYPE_NEW.equals(prodRecord.getImage_deliver_type())) {
				GroupAbbr groupAbbr = groupAbbrDao.queryGroupAbbr(prodRecord.getOwner_groupId());
				abbr = groupAbbr.getSystem_abbr();
			}
			String oldDir = "";
			// 修改变更目录
			// 如果变更单号没有改变，则不转移介质
			if(!prod_spdb_no.equals(prodRecord.getProd_spdb_no())) {
				//修改旧变更目录
				//参数1：修改前的变更单号		参数2：系统简称		参数3：当前变更记录的原目录
				oldDir = updateOtherProdRecords(prodRecord.getProd_spdb_no(), abbr, prodRecord.getProd_assets_version(), prodRecord.getImage_deliver_type());
			}
			logger.info("新变更单号是否已存在：{}", (!CommonUtils.isNullOrEmpty(oldEarliestProdRecord)));
			if(!CommonUtils.isNullOrEmpty(oldEarliestProdRecord)) {
				logger.info("新变更单号修改前的目录：{}", oldEarliestProdRecord.getProd_assets_version());
			}
			//修改新变更目录
			//参数1：修改后的变更单号		参数2：系统简称		参数3：要修改的变更单号对应的变更记录的原目录
			String newDir = updateOtherProdRecords(prod_spdb_no,
					abbr,
					!CommonUtils.isNullOrEmpty(oldEarliestProdRecord) ? oldEarliestProdRecord.getProd_assets_version() : null, prodRecord.getImage_deliver_type());
			logger.info("是否需要转移文件：{}", (!CommonUtils.isNullOrEmpty(oldDir) && prodRecord.getStatus().equals(Constants.PROD_RECORD_STSTUS_READY)));
			logger.info("转移介质旧目录：{}，新目录：{}，文件名：{}", oldDir, newDir, version);
			// 变更单号改变并且要修改的变更记录已完成准备介质		转移目标介质
			if(!CommonUtils.isNullOrEmpty(oldDir) && prodRecord.getStatus().equals(Constants.PROD_RECORD_STSTUS_READY)) {
				CommonUtils.runPythonArray(scripts_path + "change_remote_dir.py",
						new String[]{abbr,
								oldDir,
								newDir,
								version}
				);
			}
		}
	}

	@Override
	public void delete(String prod_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.PROD_ID).is(prod_id));
		mongoTemplate.findAndRemove(query, ProdRecord.class);
	}

	@Override
	public ProdRecord queryProdByVersion(String version) throws Exception {
		Query query = new Query (Criteria.where(Dict.VERSION).is(version));
		return mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public List<ProdRecord> queryPlan(String start_date, String end_date) throws Exception {
		if(CommonUtils.isNullOrEmpty(start_date)) {
			start_date = CommonUtils.formatDate(CommonUtils.INPUT_DATE);
		}
		Criteria criteria;
		if(!CommonUtils.isNullOrEmpty(end_date)) {
			criteria = Criteria.where(Dict.DATE).gte(start_date).lte(end_date);
		}else{
			criteria = Criteria.where(Dict.DATE).gte(start_date);
		}
		Query query = new Query (criteria);
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		return mongoTemplate.find(query, ProdRecord.class);
	}

	@Override
	public Map<String, String> queryProdInfo(String prod_assets_version) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ASSETS_VERSION).is(prod_assets_version));
		List<ProdRecord> prodRecords = mongoTemplate.find(query, ProdRecord.class);
		if(CommonUtils.isNullOrEmpty(prodRecords)) {
			return null;
		}
		Map<String,String> map = new HashMap<>();
		map.put(Dict.TYPE, prodRecords.get(0).getType());
		map.put(Dict.PROD_SPDB_NO, prodRecords.get(0).getProd_spdb_no());
		map.put(Dict.TEMPLATE_ID, prodRecords.get(0).getTemplate_id());
		map.put(Dict.OWNER_SYSTEM, prodRecords.get(0).getOwner_system());
		map.put(Dict.OWNER_SYSTEM_NAME, prodRecords.get(0).getOwner_system_name());
		map.put(Dict.EXCEL_TEMPLATE_NAME, prodRecords.get(0).getExcel_template_name());
		map.put(Dict.EXCEL_TEMPLATE_URL, prodRecords.get(0).getExcel_template_url());
		return map;
	}

	@Override
	public void updateReleaseNodeName(String old_release_node_name, String release_node_name) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
		Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.updateMulti(query, update, ProdRecord.class);
	}

	/**
	 * 根据变更单号查找最小的变更版本号
	 * @param prod_spdb_no
	 */
	@Override
	public ProdRecord queryEarliestByProdSpdbNo(String prod_spdb_no) {
		Query query = new Query(Criteria.where(Dict.PROD_SPDB_NO).is(prod_spdb_no));
		query.with(Sort.by(Sort.Order.asc(Dict.DATE)));
		query.with(Sort.by(Sort.Order.asc(Dict.PLAN_TIME)));
		ProdRecord prodRecord = null;
		List<ProdRecord> list = mongoTemplate.find(query, ProdRecord.class);
		if(!CommonUtils.isNullOrEmpty(list)) {
			prodRecord = list.get(0);
		}
		return prodRecord;
	}

	/**
	 * 根据变更版本号修改变更目录
	 * @param prod_spdb_no 变更单号
	 * @param prod_assets_version 变更目录
	 */
	@Override
	public void updatePordAssetsVersionByProdSpdbNo(String prod_spdb_no, String prod_assets_version) {
		Query query = new Query(Criteria.where(Dict.PROD_SPDB_NO).is(prod_spdb_no));
		Update update = Update.update(Dict.PROD_ASSETS_VERSION, prod_assets_version)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.updateMulti(query, update, ProdRecord.class);
	}

	@Override
	public List<ProdRecord> queryBeforeProdByReleaseNode(ProdRecord prodRecord) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(prodRecord.getRelease_node_name())
				.and(Dict.TYPE).is(prodRecord.getType()));
		Criteria dateCriteria = new Criteria().orOperator(Criteria.where(Dict.DATE).is(prodRecord.getDate())
						.and(Dict.PLAN_TIME).lt(prodRecord.getPlan_time()),
				new Criteria(Dict.DATE).lt(prodRecord.getDate()));
		query.addCriteria(dateCriteria);
		return mongoTemplate.find(query, ProdRecord.class);
	}

	@Override
	public ProdRecord queryByProdId(String prod_id) {
		Query query = new Query (Criteria.where(Dict.PROD_ID).is(prod_id));
		return mongoTemplate.findOne(query, ProdRecord.class);
	}

	@Override
	public List<ProdRecord> queryOldProdList(ProdRecord prodRecord, List<String> application_ids) {
		List<ProdRecord> list = new ArrayList<>();
		Query q = new Query(Criteria.where(Dict.APPLICATION_ID).in(application_ids).and(Dict.PROD_ID).ne(prodRecord.getProd_id()));
		List<ProdApplication> prodApplications = mongoTemplate.find(q, ProdApplication.class);
		if(!CommonUtils.isNullOrEmpty(prodApplications)) {
			List<String> prodIds = prodApplications.stream().map(pa -> pa.getProd_id()).collect(Collectors.toList());
			Query query = new Query (Criteria.where(Dict.RELEASE_NODE_NAME).is(prodRecord.getRelease_node_name()).and(Dict.PROD_ID).in(prodIds));
			Criteria[] criteria = new Criteria[2];
			criteria[0]= Criteria.where(Dict.DATE).gt(prodRecord.getDate());
			criteria[1] = Criteria.where(Dict.DATE).is(prodRecord.getDate()).and(Dict.PLAN_TIME).gt(prodRecord.getPlan_time());
			query.addCriteria(new Criteria().orOperator(criteria));
			list = mongoTemplate.find(query, ProdRecord.class);
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> queryRiskProd(String application_id, String startDate) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		AggregationOperation recordlookup =
				Aggregation.lookup("prod_record", "prod_id", "prod_id", "prod_record");
		Criteria dateCriteria =	new Criteria().and("prod_record.date").gt(startDate);
		Criteria criteria = new Criteria().and(Dict.APPLICATION_ID).is(application_id);
		AggregationOperation match = Aggregation.match(new Criteria().andOperator(criteria, dateCriteria));
		Sort sort = new Sort(Sort.Direction.ASC, "prod_record.date");
		AggregationOperation sortOperation = Aggregation.sort(sort);
		AggregationResults<Map> docs = mongoTemplate.aggregate(
				Aggregation.newAggregation(recordlookup, match, sortOperation), "prod_application", Map.class);
		Map<String, Object> map = new HashMap<>();
		if(!CommonUtils.isNullOrEmpty(docs.getMappedResults())) {
			result = (List<Map<String, Object>>) docs.getMappedResults().get(0).get("prod_record");
		}
		return result;
	}

	@Override
	public List<ProdRecord> queryProdListByProdAssetsVersion(String prod_assets_version) {
		Query query = new Query(Criteria.where(Dict.PROD_ASSETS_VERSION).is(prod_assets_version));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		return mongoTemplate.find(query, ProdRecord.class);
	}

	@Override
	public List<ProdRecord> queryByReleaseNodeName(ProdRecord prodRecord) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(prodRecord.getRelease_node_name())
				.and(Dict.PROD_ID).ne(prodRecord.getProd_id()));
		Criteria dateCriteria = new Criteria().orOperator(Criteria.where(Dict.DATE).is(prodRecord.getDate())
						.and(Dict.PLAN_TIME).lt(prodRecord.getPlan_time()),
				new Criteria(Dict.DATE).lt(prodRecord.getDate()));
		query.addCriteria(dateCriteria);
		return mongoTemplate.find(query, ProdRecord.class);
	}

	@Override
	public void updateAwsAssetGroupId(String prodId, String groupId) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prodId));
		Update update = Update.update("aws_group", groupId);
		mongoTemplate.findAndModify(query, update, ProdRecord.class);
	}

	/**
	 * 先根据变更单号查询最早的变更记录，根据传入的变更单号修改变更目录，返回新目录用于转移介质
	 * @param prod_spdb_no 变更单号
	 */
	public String updateOtherProdRecords(String prod_spdb_no, String groupSystemAbbr, String prod_assets_version_dir, String image_deliver_type) {
		String dir;
		// 根据变更单号查询最早的变更记录
		ProdRecord prodRecord = this.queryEarliestByProdSpdbNo(prod_spdb_no);
		if(!CommonUtils.isNullOrEmpty(prodRecord)) {
			String old_prod_assets_version = prod_assets_version_dir;
			String oldVersion = prodRecord.getVersion();
			// 新变更目录
			String prod_assets_version = new StringBuilder(oldVersion).append("_").append(prod_spdb_no).toString();
			// 批量修改变更目录
			this.updatePordAssetsVersionByProdSpdbNo(prod_spdb_no, prod_assets_version);
			logger.info("是否需要更改目录名称：{}",
					(!CommonUtils.isNullOrEmpty(old_prod_assets_version) && !old_prod_assets_version.equals(prod_assets_version)));
            logger.info("旧目录：{}，新目录：{}", old_prod_assets_version, prod_assets_version);
            // 返回新目录，用于转移介质
            dir = prod_assets_version;
            //如果旧目录与新目录名称不一样，则修改远程目录名称
            if(!CommonUtils.isNullOrEmpty(old_prod_assets_version) && !old_prod_assets_version.equals(prod_assets_version)
					&& Constants.IMAGE_DELIVER_TYPE_NEW.equals(prodRecord.getImage_deliver_type())) {
                CommonUtils.runPythonArray(scripts_path + "change_remote_dir.py",
						new String[]{groupSystemAbbr,
								old_prod_assets_version,
								prod_assets_version,
								""}
                        );
            }
		} else {
			// 如果查询为空，则为只有一个变更记录的变更单号被修改了，返回传入当前的原变更目录，用于转移介质
			dir = prod_assets_version_dir;
		}
		return dir;
	}

}
