package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.AwsConfigure;
import com.spdb.fdev.release.entity.ProdAsset;

import java.util.List;
import java.util.Map;


/**
 * 投产文件数据库接口层
 *
 */
public interface IProdAssetDao {

	ProdAsset save(ProdAsset prodAsset) throws Exception;

	ProdAsset queryAssetByName(String prod_id, String asset_catalog_name, String filename, String runtime_env) throws Exception;

	List<ProdAsset> queryAssetsList(String prod_id) throws Exception;

	List<ProdAsset> queryAssetsWithSeqno(String prod_id, String asset_catalog_name) throws Exception;

	List<ProdAsset> queryAssetsWithSeqnoDesc(String prod_id, String asset_catalog_name) throws Exception;

	ProdAsset queryAssetsOne(String id) throws Exception;

	ProdAsset deleteAsset(String id) throws Exception;

	void setCommitId(ProdAsset prodAsset) throws Exception;

	List<ProdAsset> queryAssets(String prod_id, String asset_catalog_name) throws Exception;

	ProdAsset updateAssetSeqNo(String id, String seq_no) throws Exception;

	ProdAsset queryAssetBySeq_no(String prod_id, String asset_catalog_name, String seq_no) throws Exception;

	void updateNode(String old_release_node_name, String new_release_node_name) throws Exception;

	boolean isAssetCatalogUsed(String template_id, String asset_catalog_name)throws Exception;

	List<ProdAsset> deleteByProd(String prod_id) throws Exception;

	List<ProdAsset> findAssetByAppAndProd(String application_id, String prod_id) throws Exception;

    void delCommonConfigByAppAndProd(String application_id, String prod_id);

	List<AwsConfigure> queryAwsConfigByGroupId(String groupId);

	ProdAsset queryAssetByNameAndPath(String prod_id, String asset_catalog_name, String file_name, String runtime_env, String bucket_name, String bucket_path, String aws_type);

	ProdAsset queryAssetByNameAndSid(String prod_id, String asset_catalog_name, String file_name, String runtime_env, String bucket_name, String sid);

	void updateAssetWriteFlag(String prodId, String writeFlag, String fileName, String asset_catalog_name) throws Exception;

	void addWriteFlagField() throws Exception;

	List<ProdAsset> queryhasAssets(String prod_id,String asset_catalog_name,String application_id) throws Exception;

	void editEsfCommonconfig(ProdAsset prodAsset) throws Exception;

	ProdAsset queryAwsAssetByType(String prod_id, String asset_catalog_name, String runtime_env, String bucket_name) throws Exception;

	ProdAsset queryAwsAsset(String prod_id, String asset_catalog_name, String applicationId,String runtime_env, String aws_type, String bucket_name, String bucket_path) throws Exception;

	void updateAsset(ProdAsset prodAsset) throws Exception;
	
	void delCommonConfigByAssetCatalogName(String application_id, String prod_id, String asset_catalog_name);
}
