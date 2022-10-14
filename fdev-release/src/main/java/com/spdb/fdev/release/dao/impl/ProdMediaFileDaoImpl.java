package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.IProdMediaFileDao;
import com.spdb.fdev.release.entity.ProdMediaFile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class ProdMediaFileDaoImpl implements IProdMediaFileDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void save(ProdMediaFile prodMediaFile) {
        mongoTemplate.save(prodMediaFile);
    }

    @Override
    public List<ProdMediaFile> findByProdId(String prod_id) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, ProdMediaFile.class);
    }

    @Override
    public void deleteByProdId(String prod_id) {
        Query query = new Query(Criteria.where(Dict.PROD_ID).is(prod_id));
        mongoTemplate.remove(query, ProdMediaFile.class);
    }
}
