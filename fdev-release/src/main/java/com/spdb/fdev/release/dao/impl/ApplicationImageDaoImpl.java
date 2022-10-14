package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.release.dao.IApplicationImageDao;
import com.spdb.fdev.release.entity.ApplicationImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApplicationImageDaoImpl implements IApplicationImageDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void save(ApplicationImage applicationImage) {
        mongoTemplate.insert(applicationImage);
    }

    @Override
    public void delete(String prod_id, String application_id) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
        mongoTemplate.findAllAndRemove(query, ApplicationImage.class);
    }

    @Override
    public void deleteByDeployType(String prod_id, String application_id, String type) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id).and(Dict.DEPLOY_TYPE).is(type));
        mongoTemplate.findAllAndRemove(query, ApplicationImage.class);
    }

    @Override
    public List<ApplicationImage> queryPushImageUri(String release_node_name, String prod_id, String application_id) {
        Query query = new Query(Criteria.where(Dict.RELEASE_NODE_NAME).is(release_node_name)
                .and(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id));
        return mongoTemplate.find(query, ApplicationImage.class);
    }

    @Override
    public void updateStatusLogById(String id, String status, String log, String deploy_type) {
        Query query = new Query(Criteria.where(Dict.ID).is(id).and(Dict.DEPLOY_TYPE).is(deploy_type));
        Update update = Update.update(Dict.STATUS, status).set(Dict.PUSH_IMAGE_LOG, log).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ApplicationImage.class);
    }

    @Override
    public List<ApplicationImage> queryByImages(List<String> images) {
        Query query = new Query(Criteria.where(Dict.IMAGE_URI).in(images));
        return mongoTemplate.find(query, ApplicationImage.class);
    }

    @Override
    public List<ApplicationImage> queryByProdId(String prod_id) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
        return mongoTemplate.find(query, ApplicationImage.class);
    }


    @Override
    public void updateByProdApplication(String prod_id, String application_id, String status, String push_image_log, String imageUri) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id).and(Dict.APPLICATION_ID).is(application_id).and(Dict.IMAGE_URI).is(imageUri));
        Update update = Update.update(Dict.STATUS, status).set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        if(!CommonUtils.isNullOrEmpty(push_image_log)) {
            update.set(Dict.PUSH_IMAGE_LOG, push_image_log);
        }
        mongoTemplate.updateMulti(query, update, ApplicationImage.class);
    }

    @Override
    public List<ApplicationImage> queryByProdIds(List<String> ids) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).in(ids));
        return mongoTemplate.find(query, ApplicationImage.class);
    }
}
