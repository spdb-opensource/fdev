package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.RelDevopsRecord;

import java.util.List;
import java.util.Map;

public interface IRelDevopsRecordDao {

   RelDevopsRecord save(RelDevopsRecord relDevopsRecord) throws Exception;
   
   List<RelDevopsRecord> query(RelDevopsRecord relDevopsRecord) throws Exception;

   Map<String,Object> queryImageTags(RelDevopsRecord relDevopsRecord);

   RelDevopsRecord findAppByMidAndAppid(String application_id, String merge_request_id) throws Exception;

   RelDevopsRecord setTag(RelDevopsRecord relDevopsRecord) throws Exception;

   RelDevopsRecord setUri(RelDevopsRecord relDevopsRecord)throws Exception;

   void setDevStatus(RelDevopsRecord rel) throws Exception;

   RelDevopsRecord findAppByTagAndAppid(String application_id, String product_tag)throws Exception;

   List<String> queryTagList(RelDevopsRecord relDevopsRecord) throws Exception;

   Map<String,Object> queryImageList(RelDevopsRecord relDevopsRecord)throws Exception;

   void updateNode(String old_release_node_name, String new_release_node_name) throws Exception;

   void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id)throws Exception;

   List<RelDevopsRecord> queryNormalTags(String release_node_name, String application_id);

   RelDevopsRecord setPackageUri(RelDevopsRecord relDevopsRecord);

   List<String> queryPackageTags(RelDevopsRecord relDevopsRecord);

    String queryPackageByTagAndApp(String pro_tag, String application_id);

    RelDevopsRecord queryProTagByPackage(String pro_package_uri);
}