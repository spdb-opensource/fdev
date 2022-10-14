package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.DbReview;
import com.spdb.fdev.release.entity.ProdAsset;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IDbReviewService {

	DbReview query(String task_id);

	void update(DbReview dbReview1);

	void dbReviewUpload(String task_id);

	List<String> queryDbPath(String release_node_name, List<String> project_ids);

	Object queryPath(String release_date, String group_id, String reviewStatus);

    Map uploadAssets(String prod_id, String asset_catalog_name, String child_catalog, String file) throws Exception;

    ProdAsset queryAssetByName(String prod_id, String asset_catalog_name, String file_name, String runtime_env) throws Exception;

    List<ProdAsset> queryAssetsWithSeqno(String prod_id, String asset_catalog_name) throws Exception;


}
