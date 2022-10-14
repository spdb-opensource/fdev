package com.spdb.fdev.release.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.release.entity.NoteConfiguration;
import com.spdb.fdev.release.entity.NoteManual;
import com.spdb.fdev.release.entity.NoteService;
import com.spdb.fdev.release.entity.NoteSql;
import com.spdb.fdev.release.entity.ProdNote;

public interface IProdNoteDao {

	ProdNote create(ProdNote releaseNotes) throws Exception;
	
	ProdNote queryProdRecordByVersion(String release_node_name, String version) throws Exception;
	
	ProdNote queryProdByVersion(String version) throws Exception;
	
	List<ProdNote> query(String release_node_name) throws Exception;
	
	ProdNote queryDetail(String id) throws Exception;

	ProdNote queryEarliestByProdSpdbNo(String prod_spdb_no);
	
	void updatePordAssetsVersionByProdSpdbNo(String prod_spdb_no, String prod_assets_version);
	
	void delete(String prod_id) throws Exception;
	
	void createService(NoteService noteService) throws Exception;
	
	List<NoteService> queryNoteService(String note_id) throws Exception;
	
	void deleteNoteSrevice(String id) throws Exception;

	void updateNoteService(String id,String application_name_en,String application_name_cn,List<Map<String, Object>> dev_managers_info,String tag_Name,String application_id,String application_type,Map<String, Object> expand_info) throws Exception;
	
	void createConfiguration(NoteConfiguration noteConfiguration) throws Exception;
	
	List<NoteConfiguration> queryNoteConfiguration(String note_id) throws Exception;
	
	void deleteNoteConfiguration(String id) throws Exception;

	void updateNoteConfiguration(String id,String fileName,String file_url,String file_principal,String principal_phone,List<Map<String, Object>> diff_content,String file_type,String city,String safeguard_explain,String diff_flag,String module_ip) throws Exception;
	
	void createSql(NoteSql noteSql) throws Exception;
	
	void deleteNoteSql(String id) throws Exception;

	void updateNoteSql(String id) throws Exception;

	void updateNoteLockFlag(String note_id, String lock_flag, String lock_people, String lock_name_cn) throws Exception;
	
	NoteSql updateNoteSeqNo(String id, String seq_no) throws Exception;
	
    List<NoteSql> queryNoteSqlList(String note_id)throws Exception;
    
    List<NoteSql> queryNoteSql(String note_id, String asset_catalog_name) throws Exception;
    
    NoteSql queryNoteSql(String id) throws Exception;
    
    NoteSql querySqlByName(String note_id, String asset_catalog_name, String fileName, String script_type) throws Exception;
    
    void updateProdNoteInfo(ProdNote prodNote) throws Exception;
    
    NoteConfiguration queryNoteConfigurationInfo(String id, String fileName, String module_name) throws Exception;

    ProdNote queryProdRecordByNoteBatch(String release_node_name, String note_batch, String release_date, String owner_system, String type) throws Exception;
    
    NoteManual createManualNote(NoteManual noteManual) throws Exception;
    
    void updateManualNote(String note_id, String note_info) throws Exception;
    
    NoteManual queryManualNote(String note_id) throws Exception;
    
    void deleteNoteConfigurationByNoteId(String note_id) throws Exception;
    
    void deleteNoteSqlByNoteId(String note_id) throws Exception;
    
    void deleteNoteSreviceByNoteId(String note_id) throws Exception;
    
    void deleteNoteManualByNoteId(String note_id) throws Exception;
    
    void updateProdNoteReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
    
    void updateNoteServiceReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
    
    void updateNoteConfigurationReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
    
    void updateNoteSqlReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;
    
    void updateNoteBatchReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;

    void deleteBatchInfoByNoteId(String note_id) throws Exception;
}
