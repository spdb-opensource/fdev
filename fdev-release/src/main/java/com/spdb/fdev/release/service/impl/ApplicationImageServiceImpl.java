package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.dao.IApplicationImageDao;
import com.spdb.fdev.release.dao.IProdRecordDao;
import com.spdb.fdev.release.entity.ApplicationImage;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.service.IApplicationImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApplicationImageServiceImpl implements IApplicationImageService {

    @Autowired
    IApplicationImageDao applicationImageDao;

    @Autowired
    IProdRecordDao prodRecordDao;

    @Override
    public void save(ApplicationImage applicationImage) {
        applicationImageDao.save(applicationImage);
    }

    @Override
    public void delete(String prod_id, String application_id) {
        applicationImageDao.delete(prod_id, application_id);
    }

    @Override
    public void deleteByDeployType(String prod_id, String application_id, String type) {
        applicationImageDao.deleteByDeployType(prod_id, application_id,type);
    }

    @Override
    public List<ApplicationImage> queryPushImageUri(String release_node_name, String prod_id, String application_id) {
        return applicationImageDao.queryPushImageUri(release_node_name, prod_id, application_id);
    }

    @Override
    public void updateStatusLogById(String id, String status, String log,String deploy_type) {
        applicationImageDao.updateStatusLogById(id, status, log, deploy_type);
    }

    @Override
    public List<ApplicationImage> queryByImages(List<String> images) {
        return applicationImageDao.queryByImages(images);
    }

    @Override
    public Map<String, List<ApplicationImage>> queryByProdId(String prod_id) {
        List<ApplicationImage> list = applicationImageDao.queryByProdId(prod_id);
        List<ApplicationImage> justStart = new ArrayList<>();
        List<ApplicationImage> pushing = new ArrayList<>();
        List<ApplicationImage> complete = new ArrayList<>();
        List<ApplicationImage> pushError = new ArrayList<>();
        for(ApplicationImage ai : list) {
            switch (ai.getStatus()) {
                case "0":
                    justStart.add(ai);
                    break;
                case "1":
                    pushing.add(ai);
                    break;
                case "2":
                    complete.add(ai);
                    break;
                case "3":
                    pushError.add(ai);
                    break;
            }
        }
        Map<String, List<ApplicationImage>> map = new HashMap<>();
        map.put(Dict.JUST_START, justStart);
        map.put(Dict.PUSHING, pushing);
        map.put(Dict.COMPLETE_PUSH, complete);
        map.put(Dict.PUSH_ERROR, pushError);
        map.put(Dict.TOTAL, list);
        return map;
    }

    @Override
    public void updateByProdApplication(String prod_id, String application_id, String status, String push_image_log,String imageUri) {
        applicationImageDao.updateByProdApplication(prod_id, application_id, status, push_image_log,imageUri);
    }

    @Override
    public List<String> queryImagelistByProdAssetVersion(String prod_assets_version) {
        List<ProdRecord> prodRecords = prodRecordDao.queryProdListByProdAssetsVersion(prod_assets_version);
        if(!CommonUtils.isNullOrEmpty(prodRecords)) {
            List<String> ids = prodRecords.stream().map(pr -> pr.getProd_id()).collect(Collectors.toList());
            List<ApplicationImage> ais = applicationImageDao.queryByProdIds(ids);
            if(!CommonUtils.isNullOrEmpty(ais)) {
                return ais.stream().map(ai -> ai.getImage_uri()).collect(Collectors.toList());
            }
        }
        return null;
    }
}
