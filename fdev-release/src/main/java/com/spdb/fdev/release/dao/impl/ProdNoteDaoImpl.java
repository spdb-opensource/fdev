package com.spdb.fdev.release.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IProdNoteDao;
import com.spdb.fdev.release.entity.BatchTaskInfo;
import com.spdb.fdev.release.entity.NoteConfiguration;
import com.spdb.fdev.release.entity.NoteManual;
import com.spdb.fdev.release.entity.NoteService;
import com.spdb.fdev.release.entity.NoteSql;
import com.spdb.fdev.release.entity.ProdNote;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseRqrmnt;

@Service
@RefreshScope
public class ProdNoteDaoImpl implements IProdNoteDao {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public ProdNote create(ProdNote releaseNotes) throws Exception {
		try {
			return mongoTemplate.save(releaseNotes);
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
	public ProdNote queryProdRecordByVersion(String release_node_name, String version) throws Exception {
		Query query =new Query(Criteria.where(Dict.VERSION)
				.is(version)
				.and(Dict.RELEASE_NODE_NAME)
				.is(release_node_name));			
		return mongoTemplate.findOne(query, ProdNote.class);
	}

	@Override
	public ProdNote queryProdByVersion(String version) throws Exception {
		Query query = new Query (Criteria.where(Dict.VERSION).is(version));
		return mongoTemplate.findOne(query, ProdNote.class);
	}

	@Override
	public List query(String release_node_name) throws Exception {
		Query query=new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		//按version 倒叙排列
		query.with(Sort.by(Sort.Order.desc(Dict.VERSION)));
		List<ProdNote> prodNotes = mongoTemplate.find(query, ProdNote.class);
		return prodNotes;
	}

	@Override
	public ProdNote queryDetail(String id) throws Exception {
		Query query=new Query(Criteria.where(Dict.NOTE_ID).is(id));
		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
		ProdNote prodNote = mongoTemplate.findOne(query, ProdNote.class);
		return prodNote;
//		return setProd_assets_version(prodNote);
	}
	
//	public ProdNote setProd_assets_version(ProdNote prodRecord) {
//		if(CommonUtils.isNullOrEmpty(prodRecord)) {
//			return null;
//		}
//		Query query = new Query(Criteria.where(Dict.NOTE_ID)
//				.is(prodRecord.getNote_id()));
//		query.fields().exclude(Dict.AUTO_RELEASE_LOG);
//		if(CommonUtils.isNullOrEmpty(prodRecord.getProd_assets_version())) {
//			String date = prodRecord.getDate().replace("/", "");
//			String prod_assets_version = new StringBuilder(date)
//					.append("_").append(prodRecord.getProd_spdb_no()).toString(); 
//			prodRecord.setProd_assets_version(prod_assets_version);
//			Update update = Update.update(Dict.PROD_ASSETS_VERSION,prod_assets_version)
//					.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
//			mongoTemplate.findAndModify(query, update, ProdRecord.class);
//		}
//		return prodRecord;
//	}

	@Override
	public ProdNote queryEarliestByProdSpdbNo(String prod_spdb_no) {
		Query query = new Query(Criteria.where(Dict.PROD_SPDB_NO).is(prod_spdb_no));
		query.with(Sort.by(Sort.Order.asc(Dict.DATE)));
		query.with(Sort.by(Sort.Order.asc(Dict.PLAN_TIME)));
		ProdNote prodNote = null;
		List<ProdNote> list = mongoTemplate.find(query, ProdNote.class);
		if(!CommonUtils.isNullOrEmpty(list)) {
			prodNote = list.get(0);
		}
		return prodNote;
	}

	@Override
	public void updatePordAssetsVersionByProdSpdbNo(String prod_spdb_no, String prod_assets_version) {
		Query query = new Query(Criteria.where(Dict.PROD_SPDB_NO).is(prod_spdb_no));
		Update update = Update.update(Dict.PROD_ASSETS_VERSION, prod_assets_version)
				.set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		mongoTemplate.updateMulti(query, update, ProdRecord.class);
		
	}

	@Override
	public void delete(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		mongoTemplate.findAndRemove(query, ProdNote.class);
	}

	@Override
	public void createService(NoteService noteService) throws Exception {
		mongoTemplate.save(noteService);
	}

	@Override
	public List<NoteService> queryNoteService(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		List<NoteService> list = mongoTemplate.find(query, NoteService.class);
		return list;
	}

	@Override
	public void deleteNoteSrevice(String id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		mongoTemplate.findAndRemove(query, NoteService.class);
		
	}

	@Override
	public void updateNoteService(String id,String application_name_en,String application_name_cn,List<Map<String, Object>> dev_managers_info,String tag_Name,String application_id,String application_type,Map<String, Object> expand_info) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		Update update=Update.update(Dict.APPLICATION_NAME_EN, application_name_en).set(Dict.APPLICATION_NAME_CN, application_name_cn).set("dev_managers_info", dev_managers_info)
				.set(Dict.TAG_NAME, tag_Name).set(Dict.APPLICATION_ID, application_id).set("application_type", application_type).set("expand_info", expand_info);
		this.mongoTemplate.findAndModify(query, update, NoteService.class);
		
	}

	@Override
	public void createConfiguration(NoteConfiguration noteConfiguration) throws Exception {
		mongoTemplate.save(noteConfiguration);
		
	}

	@Override
	public List<NoteConfiguration> queryNoteConfiguration(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		List<NoteConfiguration> list = mongoTemplate.find(query, NoteConfiguration.class);
		return list;
	}

	@Override
	public void deleteNoteConfiguration(String id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		mongoTemplate.findAndRemove(query, NoteConfiguration.class);
		
	}

	@Override
	public void updateNoteConfiguration(String id,String fileName,String file_url,String file_principal,String principal_phone,List<Map<String, Object>> diff_content,String file_type,String city,String safeguard_explain,String diff_flag,String module_ip) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		Update update=Update.update(Dict.FILENAME_U, fileName).set("file_url", file_url).set("file_principal", file_principal).set("principal_phone", principal_phone)
				.set("diff_content", diff_content).set("file_type", file_type).set("city", city).set("safeguard_explain", safeguard_explain).set("diff_flag", diff_flag).set("module_ip", module_ip);
		this.mongoTemplate.findAndModify(query, update, NoteConfiguration.class);
		
	}

	@Override
	public void createSql(NoteSql noteSql) throws Exception {
		mongoTemplate.save(noteSql);
		
	}

	@Override
	public void deleteNoteSql(String id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		mongoTemplate.findAndRemove(query, NoteSql.class);
		
	}

	@Override
	public void updateNoteSql(String id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
//		Update update=Update.update(Dict.PROD_COMMITID, NoteService.getProd_commitid()).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
//		this.mongoTemplate.findAndModify(query, update, NoteService.class);
		
	}

	@Override
	public void updateNoteLockFlag(String note_id, String lock_flag, String lock_people, String lock_name_cn) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		Update update=Update.update("lock_flag", lock_flag).set("lock_people", lock_people).set("lock_name_cn", lock_name_cn);
		this.mongoTemplate.findAndModify(query, update, ProdNote.class);
		
	}

	@Override
	public NoteSql updateNoteSeqNo(String id, String seq_no) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(id));
		Update update = Update.update(Dict.SEQ_NO, seq_no);
		mongoTemplate.findAndModify(query, update, NoteSql.class);
		return mongoTemplate.findOne(query, NoteSql.class);
	}

	@Override
	public List<NoteSql> queryNoteSqlList(String note_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(note_id))
				.with(new Sort(Sort.Direction.ASC, Dict.SEQ_NO));
		List<NoteSql> list = mongoTemplate.find(query, NoteSql.class);
		return list;
	}

	@Override
	public List<NoteSql> queryNoteSql(String note_id, String asset_catalog_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(note_id).and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name));
		return  mongoTemplate.find(query, NoteSql.class);
	}
	
	@Override
	public NoteSql queryNoteSql(String id) throws Exception {
		Query query = new Query (Criteria.where(Dict.ID).is(id));
		return mongoTemplate.findOne(query, NoteSql.class);
	}

	@Override
	public NoteSql querySqlByName(String note_id, String asset_catalog_name, String fileName, String script_type)
			throws Exception {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(note_id)
				.and(Dict.FILENAME_U).is(fileName)
				.and(Dict.ASSET_CATALOG_NAME).is(asset_catalog_name)
				.and("script_type").is(script_type));
		return mongoTemplate.findOne(query, NoteSql.class);
	}
	
	public void updateProdNoteInfo(ProdNote prodNote) throws Exception {
		mongoTemplate.save(prodNote);
	}

	@Override
	public NoteConfiguration queryNoteConfigurationInfo(String id, String fileName, String module_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.NOTE_ID).is(id)
				.and(Dict.FILENAME_U).is(fileName)
				.and("module_name").is(module_name));
		return mongoTemplate.findOne(query, NoteConfiguration.class);
	}

	@Override
	public ProdNote queryProdRecordByNoteBatch(String release_node_name, String note_batch, String release_date, String owner_system, String type)
			throws Exception {
		Query query =new Query(Criteria.where("note_batch")
				.is(note_batch)
				.and(Dict.RELEASE_NODE_NAME)
				.is(release_node_name)
				.and(Dict.DATE)
				.is(release_date)
				.and(Dict.OWNER_SYSTEM).is(owner_system)
				.and(Dict.TYPE).is(type));			
		return mongoTemplate.findOne(query, ProdNote.class);
	}

	@Override
	public NoteManual createManualNote(NoteManual noteManual) throws Exception {
		return mongoTemplate.save(noteManual);
	}

	@Override
	public void updateManualNote(String note_id, String note_info) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		Update update=Update.update("note_info", note_info);
		this.mongoTemplate.findAndModify(query, update, NoteManual.class);
		
	}

	@Override
	public NoteManual queryManualNote(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		return mongoTemplate.findOne(query, NoteManual.class);
	}

	@Override
	public void deleteNoteConfigurationByNoteId(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		mongoTemplate.findAndRemove(query, NoteConfiguration.class);
		
	}

	@Override
	public void deleteNoteSqlByNoteId(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		mongoTemplate.findAndRemove(query, NoteSql.class);
		
	}

	@Override
	public void deleteNoteSreviceByNoteId(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		mongoTemplate.findAndRemove(query, NoteService.class);
		
	}

	@Override
	public void deleteNoteManualByNoteId(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		mongoTemplate.findAndRemove(query, NoteManual.class);
		
	}

	@Override
	public void updateProdNoteReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
        Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.updateMulti(query, update, ProdNote.class);
	}

	@Override
	public void updateNoteServiceReleaseNodeName(String old_release_node_name, String release_node_name)
			throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
        Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name);
        mongoTemplate.updateMulti(query, update, NoteService.class);
	}

	@Override
	public void updateNoteConfigurationReleaseNodeName(String old_release_node_name, String release_node_name)
			throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
        Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name);
        mongoTemplate.updateMulti(query, update, NoteConfiguration.class);
	}

	@Override
	public void updateNoteSqlReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
        Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name);
        mongoTemplate.updateMulti(query, update, NoteSql.class);
	}

	@Override
	public void updateNoteBatchReleaseNodeName(String old_release_node_name, String release_node_name)
			throws Exception {
		Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(old_release_node_name));
        Update update = Update.update(Dict.RELEASE_NODE_NAME, release_node_name);
        mongoTemplate.updateMulti(query, update, BatchTaskInfo.class);
	}

	@Override
	public void deleteBatchInfoByNoteId(String note_id) throws Exception {
		Query query = new Query (Criteria.where(Dict.NOTE_ID).is(note_id));
		mongoTemplate.findAndRemove(query, BatchTaskInfo.class);
	}

}
