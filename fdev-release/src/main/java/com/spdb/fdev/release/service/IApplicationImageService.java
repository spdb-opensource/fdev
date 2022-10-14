package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ApplicationImage;

import java.util.List;
import java.util.Map;

public interface IApplicationImageService {
    
    void save(ApplicationImage applicationImage);

    void delete(String prod_id, String application_id);

    void deleteByDeployType(String prod_id, String application_id, String type);

    List<ApplicationImage> queryPushImageUri(String release_node_name, String prod_id, String application_id);

    void updateStatusLogById(String id, String status, String log,String deploy_type);

    List<ApplicationImage> queryByImages(List<String> images);

    Map<String, List<ApplicationImage>> queryByProdId(String prod_id);

    void updateByProdApplication(String prod_id, String application_id, String status, String push_image_log,String imageUri);

    List<String> queryImagelistByProdAssetVersion(String prod_assets_version);
}
