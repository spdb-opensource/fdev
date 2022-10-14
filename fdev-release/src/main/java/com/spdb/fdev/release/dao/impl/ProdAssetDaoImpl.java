package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.FreleaseDao;
import com.spdb.fdev.release.dao.IProdAssetDao;
import com.spdb.fdev.release.entity.AwsConfigure;
import com.spdb.fdev.release.entity.ProdAsset;
import com.spdb.fdev.release.entity.ProdRecord;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 投产窗口数据处理层
 */
@Repository
public class ProdAssetDaoImpl extends FreleaseDao implements IProdAssetDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public ProdAsset save(ProdAsset prodAsset) throws Exception {
		ObjectId id = new ObjectId();
		prodAsset.set_id(id);
		prodAsset.setId(id.toString());
		try {
			return mongoTemplate.save(prodAsset);
		} catch (DuplicateKeyException e) {
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "文件名已存在,请更换" });
		}
	}

	@Override
	public ProdAsset queryAssetByName(String prod_id, String asset_catalog_name, String filename,String runtime_env) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.FILENAME).is(filename)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and(Dict.RUNTIME_ENV).is(runtime_env));
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public List<ProdAsset> queryAssetsList(String prod_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id))
				.with(new Sort(Sort.Direction.ASC, Dict.SEQ_NO));
		List<ProdAsset> list = mongoTemplate.find(query, ProdAsset.class);
		return list;
	}

	@Override
	public List<ProdAsset> queryAssetsWithSeqno(String prod_id,String asset_catalog_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and(Dict.SEQ_NO).exists(true));
		return  mongoTemplate.find(query, ProdAsset.class);
	}

	@Override
	public List<ProdAsset> queryAssetsWithSeqnoDesc(String prod_id,String asset_catalog_name) throws Exception {
		Collation collation = Collation.of(Locale.CHINESE).numericOrdering(true);
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and(Dict.SEQ_NO).exists(true))
				.with(new Sort(Sort.Direction.DESC, Dict.SEQ_NO));
		query.collation(collation);
		return  mongoTemplate.find(query, ProdAsset.class);
	}

	@Override
	public List<ProdAsset> queryAssets(String prod_id,String asset_catalog_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name));
		return  mongoTemplate.find(query, ProdAsset.class);
	}

	@Override
	public ProdAsset queryAssetsOne(String id) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(id));
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public ProdAsset deleteAsset(String id) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(id));
		return this.mongoTemplate.findAndRemove(query, ProdAsset.class);
	}

	@Override
	public void setCommitId(ProdAsset prodAsset) throws Exception {
		Query query =new Query(Criteria.where(Dict.FILENAME)
				.is(prodAsset.getFileName())
				.and(Dict.FILE_GITURL)
				.is(prodAsset.getFile_giturl()));
		Update update=Update.update(Dict.PROD_COMMITID, prodAsset.getProd_commitid()).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		this.mongoTemplate.findAndModify(query, update, ProdAsset.class);
	}

	@Override
	public ProdAsset updateAssetSeqNo(String id, String seq_no) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(id));
		Update update = Update.update(Dict.SEQ_NO, seq_no).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.findAndModify(query, update, ProdAsset.class);
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public ProdAsset queryAssetBySeq_no(String prod_id, String asset_catalog_name, String seq_no) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.FILENAME).is(seq_no)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name));
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public void updateNode(String old_release_node_name, String new_release_node_name) throws Exception {
		Query query =new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
		List<ProdAsset> prod_assets = mongoTemplate.find(query, ProdAsset.class);
		for (ProdAsset prodAsset : prod_assets) {
			Query query2 =new Query(Criteria.where(Dict.ID).is(prodAsset.getId()));
			Update update =Update.update(Dict.RELEASE_NODE_NAME, new_release_node_name).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
			mongoTemplate.findAndModify(query2, update, ProdAsset.class);
		}

	}

	@Override
	public boolean isAssetCatalogUsed(String template_id, String asset_catalog_name) throws Exception {
		Query query =new Query(Criteria.where(Dict.TEMPLATE_ID).is(template_id));
		List<ProdRecord> prodRecords = mongoTemplate.find(query, ProdRecord.class);
		List<String> ids= new ArrayList<String>();
		for (ProdRecord prodRecord : prodRecords) {
			ids.add(prodRecord.getProd_id());
		}
		Query query2 =new Query(Criteria.where(Dict.PROD_ID)
				.in(ids)
				.and(Dict.ASSET_CATALOG_NAME)
				.is(asset_catalog_name));
		List<ProdAsset> find = mongoTemplate.find(query2, ProdAsset.class);
		if(!CommonUtils.isNullOrEmpty(find)) {
			return true;
		}
		return false;
	}

	@Override
	public List<ProdAsset> deleteByProd(String prod_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.PROD_ID).is(prod_id));
		return mongoTemplate.findAllAndRemove(query, ProdAsset.class);
	}

    @Override
    public List<ProdAsset> findAssetByAppAndProd(String application_id, String prod_id) throws Exception {
        Query query = new Query (Criteria.where(Dict.SOURCE_APPLICATION).is(application_id)
                .and(Dict.PROD_ID).is(prod_id)
                .and(Dict.SEQ_NO).exists(true));
        return mongoTemplate.find(query, ProdAsset.class);
    }

	@Override
	public void delCommonConfigByAppAndProd(String application_id, String prod_id) {
		Query query = new Query (Criteria.where(Dict.SOURCE_APPLICATION).is(application_id).and(Dict.PROD_ID).is(prod_id).and(Dict.ASSET_CATALOG_NAME).ne("esfcommonconfig"));
		mongoTemplate.findAllAndRemove(query, ProdAsset.class);
	}

	@Override
	public List<AwsConfigure> queryAwsConfigByGroupId(String groupId) {
		Query query = new Query();
		if(!CommonUtils.isNullOrEmpty(groupId)) {
			query.addCriteria(Criteria.where(Dict.GROUP_ID).is(groupId));
		}
		return mongoTemplate.find(query, AwsConfigure.class);
	}

	@Override
	public ProdAsset queryAssetByNameAndPath(String prod_id, String asset_catalog_name, String file_name, String runtime_env, String bucket_name, String bucket_path, String aws_type) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.FILENAME).is(file_name)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and(Dict.RUNTIME_ENV).is(runtime_env)
				.and("bucket_name").is(bucket_name));
		if(!CommonUtils.isNullOrEmpty(bucket_path)){
			Criteria bucketPathCri = Criteria.where("bucket_path").is(bucket_path);
			query.addCriteria(bucketPathCri);
		}
		if(!CommonUtils.isNullOrEmpty(aws_type)){
			Criteria typeCri = Criteria.where("aws_type").is(aws_type);
			query.addCriteria(typeCri);
		}
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public ProdAsset queryAwsAssetByType(String prod_id, String asset_catalog_name, String runtime_env, String bucket_name) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and(Dict.RUNTIME_ENV).is(runtime_env)
				.and("aws_type").is("2")
				.and("bucket_name").is(bucket_name));
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public ProdAsset queryAssetByNameAndSid(String prod_id, String asset_catalog_name, String file_name, String runtime_env, String bucket_name, String sid) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.FILENAME).is(file_name)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and(Dict.RUNTIME_ENV).is(runtime_env)
				.and("bucket_name").is(bucket_name)
				.and("sid").is(sid));
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public void updateAssetWriteFlag(String prod_id, String writeFlag, String fileName, String asset_catalog_name) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.FILENAME).is(fileName)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name));
		Update update =Update.update(Dict.WRITE_FLAG, writeFlag);
		mongoTemplate.findAndModify(query, update, ProdAsset.class);
	}
	
	@Override
	public void addWriteFlagField() {
		List<ProdAsset> list = mongoTemplate.findAll(ProdAsset.class);
		list.forEach(item -> {
			item.setWrite_flag("0");
			mongoTemplate.save(item);
		});
		
	}

	@Override
	public List<ProdAsset> queryhasAssets(String prod_id,String asset_catalog_name,String application_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name).and(Dict.SOURCE_APPLICATION).is(application_id));
		return  mongoTemplate.find(query, ProdAsset.class);
	}

	@Override
	public void editEsfCommonconfig(ProdAsset prodAsset) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prodAsset.getProd_id()).and(Dict.ASSET_CATALOG_NAME).is(prodAsset.getAsset_catalog_name()).and(Dict.SOURCE_APPLICATION).is(prodAsset.getSource_application()).and("runtime_env").is(prodAsset.getRuntime_env()));
		Update update = new Update();
		update.set("sid",prodAsset.getSid());
		update.set(Dict.BUCKET_NAME,prodAsset.getBucket_name());
		update.set(Dict.FILENAME,prodAsset.getFileName());
		update.set(Dict.FILE_GITURL,prodAsset.getFile_giturl());
		mongoTemplate.findAndModify(query,update,ProdAsset.class);
	}

	@Override
	public ProdAsset queryAwsAsset(String prod_id, String asset_catalog_name, String applicationId, String runtime_env, String aws_type, String bucket_name, String type) {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and(Dict.SOURCE_APPLICATION).is(applicationId)
				.and(Dict.RUNTIME_ENV).is(runtime_env)
				.and("aws_type").is(aws_type)
				.and("bucket_name").is(bucket_name)
				.and("bucket_path").is(type));
		return mongoTemplate.findOne(query, ProdAsset.class);
	}

	@Override
	public void updateAsset(ProdAsset prodAsset) throws Exception {
		Query query = new Query(Criteria.where(Dict.PROD_ID).is(prodAsset.getProd_id()).and(Dict.ASSET_CATALOG_NAME).is(prodAsset.getAsset_catalog_name()).and("runtime_env").is(prodAsset.getRuntime_env()));
		Update update = new Update();
		update.set(Dict.BUCKET_NAME,prodAsset.getBucket_name());
		update.set(Dict.FILENAME,prodAsset.getFileName());
		update.set(Dict.FILE_GITURL,prodAsset.getFile_giturl());
		update.set("aws_type",prodAsset.getAws_type());
		update.set(Dict.SOURCE_APPLICATION,prodAsset.getSource_application());

		mongoTemplate.findAndModify(query,update,ProdAsset.class);
	}

	@Override
	public void delCommonConfigByAssetCatalogName(String application_id, String prod_id, String asset_catalog_name) {
		Query query = new Query (Criteria.where(Dict.SOURCE_APPLICATION).is(application_id).and(Dict.PROD_ID).is(prod_id).and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name));
		mongoTemplate.findAllAndRemove(query, ProdAsset.class);
	}

}
