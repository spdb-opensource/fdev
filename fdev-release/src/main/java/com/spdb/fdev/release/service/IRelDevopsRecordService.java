package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.RelDevopsRecord;

import java.util.List;
import java.util.Map;

public interface IRelDevopsRecordService {
	 /**
	  * 保存应用持续集成记录
	  * @param relDevopsRecord 
	  * @return
	  * @throws Exception
	  */
	 RelDevopsRecord save(RelDevopsRecord relDevopsRecord) throws Exception;
	 /**
	  * 查询持续集成记录 
	  * @param relDevopsRecord  应用id  投产窗口名
	  * @return
	  * @throws Exception
	  */
	 List<RelDevopsRecord> query(RelDevopsRecord relDevopsRecord) throws Exception;
	 /**
	  * 查询对应镜像列表
	  * @param relDevopsRecord
	  * @return
	  * @throws Exception
	  */
	 Map<String,Object> queryImageTags(RelDevopsRecord relDevopsRecord) throws Exception;
	 /**
	  * 设置对应tag名
	  * @param relDevopsRecord
	  * @throws Exception
	  */
	 RelDevopsRecord setTag(RelDevopsRecord relDevopsRecord) throws Exception;
	 /**
	  * 通过mergerid和应用id 查询持续集成记录
	  * @param application_id
	  * @param merge_request_id
	  * @return
	  * @throws Exception
	  */
	 RelDevopsRecord findAppByMidAndAppid(String application_id, String merge_request_id)throws Exception;
	 /**
	  * 设置对应 镜像标签
	  * @param relDevopsRecord
	  * @return
	  * @throws Exception
	  */
	 RelDevopsRecord setUri(RelDevopsRecord relDevopsRecord)throws Exception;
	 /**
	  * 设置持续集成状态
	  * @param rel
	  * @throws Exception
	  */
	 void setDevStatus(RelDevopsRecord rel) throws Exception;
	 /**
	  * 通过tag名和应用id查询持续集成记录
	  * @param application_id
	  * @param product_tag
	  * @return
	  * @throws Exception
	  */
	 RelDevopsRecord findAppByTagAndAppid(String application_id, String product_tag)throws Exception;
     /**
      * 查询对应tag列表
      * @param relDevopsRecord
      * @return
      * @throws Exception
      */
	 List<String> queryTagList(RelDevopsRecord relDevopsRecord) throws Exception;
	 /**
	  * 查询对应镜像列表
	  * @param relDevopsRecord
	  * @return
	  * @throws Exception
	  */
	 Map<String,Object> queryImageList(RelDevopsRecord relDevopsRecord) throws Exception;


	 void updateNode(String old_release_node_name, String new_release_node_name) throws Exception;

	/**
	 * 修改投产窗口
	 * @param release_node_name
	 * @param release_node_name_new
	 * @param application_id
	 */
	void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id) throws Exception;

	List<String> queryNormalTags(String release_node_name, String application_id);

	boolean queryDockerDir(RelDevopsRecord relDevopsRecord, ProdRecord prodRecord, String pro_image_uri);

    RelDevopsRecord setPackageUri(RelDevopsRecord relDevopsRecord);

	List<String> queryPackageTags(RelDevopsRecord relDevopsRecord) throws Exception;

    RelDevopsRecord saveProductWar(Map<String, Object> requestParam) throws Exception;

    String queryPackageByTagAndApp(String pro_tag, String application_id);

    RelDevopsRecord queryProTagByPackage(String pro_package_uri);
}