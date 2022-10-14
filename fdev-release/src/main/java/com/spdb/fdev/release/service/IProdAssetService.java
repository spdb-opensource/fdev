package com.spdb.fdev.release.service;


import com.spdb.fdev.common.User;
import com.spdb.fdev.release.entity.AwsConfigure;
import com.spdb.fdev.release.entity.ProdAsset;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseApplication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IProdAssetService {

    /**
     * 新增投产文件
     * @param prodAsset 投产文件对象
     * @return 投产文件
     */
    Map create(ProdAsset prodAsset, MultipartFile[] files, String child_catalog, User user, String aws_type)throws Exception;
    
    
    ProdAsset addGitlabAsset(ProdAsset prodAsset) throws Exception;
    /**
     * 查询投产文件列表
     * @param prod_id 变更ID
     * @return 投产文件列表
     */
    List<ProdAsset> queryAssetsList(String prod_id)throws Exception;

    Map<String, Object> querySortedAssets(String prod_id) throws Exception;
    
    List<ProdAsset> queryAssetsWithSeqno(String prod_id, String asset_catalog_name) throws Exception;
    
    ProdAsset queryAssetByName(String prod_id, String asset_catalog_name, String file_name, String runtime_env) throws Exception;

    ProdAsset queryAssetByNameAndSid(String prod_id, String asset_catalog_name, String file_name, String runtime_env, String bucket_name, String bucket_path) throws Exception;

    /**
     * 根据id查询变更文件对象
     * @param id 变更文件objectid
     * @return 变更文件对象
     */
    ProdAsset queryAssetsOne(String id)throws Exception;
    
    /**
     * 根据id删除变更文件对象
     * @param prodAsset 变更文件对象
     * @return 变更文件对象
     */
    void deleteAsset(ProdAsset prodAsset)throws Exception;
    /**
     * 设置文件的commitid
     * @param prodAsset
     * @throws Exception
     */
	void setCommitId(ProdAsset prodAsset) throws Exception;

	List queryAssets(String prod_id, String source_application) throws Exception;
    
    ProdAsset updateAssetSeqNo(ProdAsset prodAsset) throws Exception;

	void updateNode(String release_node_name, String string) throws Exception;

	boolean isAssetCatalogUsed(String template_id, String asset_catalog_name)throws Exception;

	/**
	 * 通过变更id 删除变更文件
	 * @param prod_id
	 */
    List<ProdAsset> deleteByProd(String prod_id) throws Exception;

    void delCommonConfigByAppAndProd(String application_id, String prod_id) throws Exception;

    List queryConfigAssetsByParam(String prod_id, List<String> catalogTypes) throws Exception;

    /**
     * 新增投产文件
     * @param prodAsset 投产文件对象
     * @return 投产文件
     */
    Map deAutoCreate(ProdAsset prodAsset, MultipartFile[] files, String child_catalog)throws Exception;

    List queryAssetsByProdId(String prod_id) throws Exception;

	Map uploadAssets(User user, String prod_id, String file_encoding, String asset_catalog_name, String source_application, String runtime_env, String seq_no, String child_catalog, String bucket_name, String bucket_path, String type, MultipartFile[] files) throws Exception;

    void updateEsfcommonconfigAssets(User user, String prod_id, String file_encoding, String asset_catalog_name, String source_application, String runtime_env, String child_catalog, String bucket_name, MultipartFile[] files) throws Exception;

    void uploadSourceMap(String userId, ProdRecord prodRecord, String asset_catalog_name, String application_id, String application_name,ReleaseApplication releaseApplication, String tag) throws Exception;

    void uploadRouter(String user_id, ProdRecord prodRecord, String config, String application_id, String s, String s1, ReleaseApplication releaseApplication, String new_tag) throws Exception;

    List<AwsConfigure> queryAwsConfigByGroupId(String groupId);

    Map<String, Object> queryAllProdAssets(Map<String,String> req) throws Exception;
    
    void uploadSccRouter(String user_id, ProdRecord prodRecord, String config, String application_id, String s, String s1, ReleaseApplication releaseApplication, String new_tag) throws Exception;
    
    void delCommonConfigByAssetCatalogName(String application_id, String prod_id, String asset_catalog_name) throws Exception;

}
