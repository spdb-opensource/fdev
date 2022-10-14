package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IRelDevopsRecordDao;
import com.spdb.fdev.release.entity.RelDevopsRecord;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

@Repository
public class RelDevopsRecordDaoImpl implements IRelDevopsRecordDao{

	private final List<String> IMAGE_URI_USED = Arrays.asList("1", "2");

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public RelDevopsRecord save(RelDevopsRecord relDevopsRecord)
			throws Exception {
		ObjectId objectId = new ObjectId();
		relDevopsRecord.set_id(objectId);
		relDevopsRecord.setId(objectId.toString());
		relDevopsRecord.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		try {
			return mongoTemplate.save(relDevopsRecord);	
		} catch (Exception e) {
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "数据已存在,请更换" });
		}
	}

	@Override
	public List<RelDevopsRecord> query(RelDevopsRecord relDevopsRecord)
			throws Exception {
		Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(relDevopsRecord.getRelease_node_name())
				.and(Dict.APPLICATION_ID)
				.is(relDevopsRecord.getApplication_id()));
		
		return mongoTemplate.find(query, RelDevopsRecord.class);
	}

	@Override
	public Map<String,Object> queryImageTags(RelDevopsRecord relDevopsRecord) {
		Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(relDevopsRecord.getRelease_node_name())
				.and(Dict.APPLICATION_ID)
				.is(relDevopsRecord.getApplication_id())
				.and(Dict.DEVOPS_TYPE).in(IMAGE_URI_USED));
		query.fields().include(Dict.PRO_IMAGE_URI).include(Dict.PRO_SCC_IMAGE_URI);
		List<RelDevopsRecord> list = mongoTemplate.find(query, RelDevopsRecord.class);
		List<String> proImageUriList = new ArrayList<>();
		List<String> proSccImageUriList = new ArrayList<>();
		for (RelDevopsRecord relDevopsRecord2 : list) {
			if(!CommonUtils.isNullOrEmpty(relDevopsRecord2.getPro_image_uri())) {
				proImageUriList.add(relDevopsRecord2.getPro_image_uri());
			}
			if(!CommonUtils.isNullOrEmpty(relDevopsRecord2.getPro_scc_image_uri())){
				proSccImageUriList.add(relDevopsRecord2.getPro_scc_image_uri());
			}
		}
		Map<String,Object> resultMap = new HashMap<>();
		resultMap.put(Dict.CAAS,proImageUriList);
		resultMap.put(Dict.SCC,proSccImageUriList);
		return resultMap;
	}

	@Override
	public RelDevopsRecord findAppByMidAndAppid(String application_id, String merge_request_id) throws Exception {
		Query query =new Query(Criteria.where(Dict.APPLICATION_ID)
				.is(application_id)
				.and(Dict.MERGE_REQUEST_ID)
				.is(merge_request_id));
		return mongoTemplate.findOne(query, RelDevopsRecord.class);
	}

	@Override
	public RelDevopsRecord setTag(RelDevopsRecord relDevopsRecord) throws Exception {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID)
				.is(relDevopsRecord.getApplication_id())
				.and(Dict.MERGE_REQUEST_ID)
				.is(relDevopsRecord.getMerge_request_id()));
		Update update=Update.update(Dict.PRODUCT_TAG, relDevopsRecord.getProduct_tag())
				.set(Dict.DEVOPS_STATUS, relDevopsRecord.getDevops_status())
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, RelDevopsRecord.class);
		return mongoTemplate.findOne(query, RelDevopsRecord.class);
	}

	@Override
	public RelDevopsRecord setUri(RelDevopsRecord relDevopsRecord) throws Exception {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID)
				.is(relDevopsRecord.getApplication_id())
				.and(Dict.PRODUCT_TAG)
				.is(relDevopsRecord.getProduct_tag()));
		Update update=new Update();
		update.set(Dict.DEVOPS_STATUS, relDevopsRecord.getDevops_status())
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		if(!CommonUtils.isNullOrEmpty(relDevopsRecord.getPro_image_uri())){
			update.set(Dict.PRO_IMAGE_URI, relDevopsRecord.getPro_image_uri()).set(Dict.CAAS_ENV,relDevopsRecord.getCaas_env());
		}
		if(!CommonUtils.isNullOrEmpty(relDevopsRecord.getPro_scc_image_uri())){
			update.set(Dict.PRO_SCC_IMAGE_URI, relDevopsRecord.getPro_scc_image_uri()).set(Dict.SCC_ENV,relDevopsRecord.getScc_env());
		}
	   	mongoTemplate.findAndModify(query, update, RelDevopsRecord.class);
	   	return mongoTemplate.findOne(query, RelDevopsRecord.class);
	}

	@Override
	public void setDevStatus(RelDevopsRecord rel) throws Exception {
		Query query =new Query(Criteria.where(Dict.APPLICATION_ID)
				.is(rel.getApplication_id())
				.and(Dict.PRODUCT_TAG)
				.is(rel.getProduct_tag()));
		Update update=Update.update(Dict.DEVOPS_STATUS, rel.getDevops_status())
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
	   	mongoTemplate.findAndModify(query, update, RelDevopsRecord.class);
	}

	@Override
	public RelDevopsRecord findAppByTagAndAppid(String application_id, String product_tag) throws Exception {
		Query query=new Query(Criteria
				.where(Dict.APPLICATION_ID)
				.is(application_id)
				.and(Dict.PRODUCT_TAG)
				.is(product_tag));
		return mongoTemplate.findOne(query, RelDevopsRecord.class);
	}

	@Override
	public List<String> queryTagList(RelDevopsRecord relDevopsRecord) throws Exception {
		Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(relDevopsRecord.getRelease_node_name())
				.and(Dict.APPLICATION_ID)
				.is(relDevopsRecord.getApplication_id()));
		query.with(Sort.by(Sort.Order.desc(Dict.PRODUCT_TAG)));
		List<RelDevopsRecord> relDevopsRecords = mongoTemplate.find(query, RelDevopsRecord.class);
    	List<String> tagList = new ArrayList<>();
		for (RelDevopsRecord rel : relDevopsRecords) {
			if(!CommonUtils.isNullOrEmpty(rel.getProduct_tag())) {
				tagList.add(rel.getProduct_tag());
			}
		}
		 return tagList;
	}

	@Override
	public Map<String,Object> queryImageList(RelDevopsRecord relDevopsRecord) throws Exception {
		Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(relDevopsRecord.getRelease_node_name())
				.and(Dict.APPLICATION_ID).is(relDevopsRecord.getApplication_id())
				.and(Dict.DEVOPS_TYPE).in(IMAGE_URI_USED));
		query.with(Sort.by(Sort.Order.desc(Dict.PRO_IMAGE_URI)));
		List<RelDevopsRecord> relDevopsRecords = mongoTemplate.find(query, RelDevopsRecord.class);
		List<String> imageList = new ArrayList<>();
		List<String> sccImageList = new ArrayList<>();
		for (RelDevopsRecord rel : relDevopsRecords) {
			if(!CommonUtils.isNullOrEmpty(rel.getPro_image_uri())) {			
				imageList.add(rel.getPro_image_uri());
			}
			if(!CommonUtils.isNullOrEmpty(rel.getPro_scc_image_uri())) {
				sccImageList.add(rel.getPro_scc_image_uri());
			}
		}
		Map<String,Object> res = new HashMap<>();
		res.put(Dict.CAAS,imageList);
		res.put(Dict.SCC,sccImageList);
		return res;
	}

	@Override
	public void updateNode(String old_release_node_name, String new_release_node_name) throws Exception {
		Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
		Update update=Update.update(Dict.RELEASE_NODE_NAME, new_release_node_name)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.updateMulti(query, update, RelDevopsRecord.class);
	}

	@Override
	public void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id)
			throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(release_node_name)
				.and(Dict.APPLICATION_ID)
				.is(application_id));
		Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name_new)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.updateMulti(query, update, RelDevopsRecord.class);
	}

	@Override
	public List<RelDevopsRecord> queryNormalTags(String release_node_name, String application_id) {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name).and(Dict.APPLICATION_ID)
				.is(application_id).and(Dict.DEVOPS_TYPE).in(IMAGE_URI_USED));
		query.with(Sort.by(Sort.Order.desc(Dict.PRODUCT_TAG)));
		return mongoTemplate.find(query, RelDevopsRecord.class);
	}

	@Override
	public RelDevopsRecord setPackageUri(RelDevopsRecord relDevopsRecord) {
		Query query = new Query(Criteria.where(Dict.APPLICATION_ID)
				.is(relDevopsRecord.getApplication_id())
				.and(Dict.PRODUCT_TAG)
				.is(relDevopsRecord.getProduct_tag()));
		Update update=Update.update(Dict.PRO_PACKAGE_URI, relDevopsRecord.getPro_package_uri()
		).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, RelDevopsRecord.class);
		return mongoTemplate.findOne(query, RelDevopsRecord.class);
	}

	@Override
	public List<String> queryPackageTags(RelDevopsRecord relDevopsRecord) {
		Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME)
				.is(relDevopsRecord.getRelease_node_name())
				.and(Dict.APPLICATION_ID)
				.is(relDevopsRecord.getApplication_id())
				.and(Dict.PRO_PACKAGE_URI).exists(true)
				.and(Dict.DEVOPS_TYPE).in(IMAGE_URI_USED));//fake不查
		List<RelDevopsRecord> list = mongoTemplate.find(query, RelDevopsRecord.class, "rel_devops_record");
		List<String> pro_image_uriList = new ArrayList<>();
		for (RelDevopsRecord relDevopsRecord2 : list) {
			pro_image_uriList.add(relDevopsRecord2.getProduct_tag());
		}
		return pro_image_uriList;
	}

	@Override
	public String queryPackageByTagAndApp(String pro_tag, String application_id) {
		Criteria criteria = Criteria.where(Dict.PRODUCT_TAG).is(pro_tag).and(Dict.APPLICATION_ID).is(application_id);
		Query query = new Query(criteria);
		RelDevopsRecord relDevopsRecord = mongoTemplate.findOne(query, RelDevopsRecord.class);
		return relDevopsRecord.getPro_package_uri();
	}

	@Override
	public RelDevopsRecord queryProTagByPackage(String pro_package_uri) {
		Criteria criteria = Criteria.where(Dict.PRO_PACKAGE_URI).is(pro_package_uri);
		Query query = new Query(criteria);
		RelDevopsRecord relDevopsRecord = mongoTemplate.findOne(query, RelDevopsRecord.class);
		return relDevopsRecord;
	}

}