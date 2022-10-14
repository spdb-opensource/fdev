package com.spdb.fdev.spdb.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.dao.ISccModifyKeyDao;
import com.spdb.fdev.spdb.entity.SccModifyKey;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/3-18:02
 * @Description: scc_modifyKey dao层 实现类
 */
@Repository
public class SccModifyKeyDaoImpl implements ISccModifyKeyDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void updateModifyKeyDeploy(List<SccModifyKey> list) {
        if(list.size() > 0){
            for(int i = 0; i < list.size(); i++){
                String namespace_code = list.get(i).getNamespace_code();
                String resource_code = list.get(i).getResource_code();
                String yaml = list.get(i).getYaml();
                if(namespace_code != null && resource_code != null){
                    SccModifyKey sccModifyKey = this.getModifyDeploy(namespace_code, resource_code);
                    if(sccModifyKey != null){
                        //update
                        Criteria criteria = new Criteria();
                        Query query = new Query(criteria.where("namespace_code").is(namespace_code)
                        .and("resource_code").is(resource_code));
                        Update update = new Update();
                        update.set("yaml",yaml);
                        this.mongoTemplate.updateFirst(query, update, SccModifyKey.class);
                    }else {
                        this.mongoTemplate.insert(list.get(i), Dict.SCCMODIFYKEY);
                    }
                }

            }
        }
    }

    public SccModifyKey getModifyDeploy(String namespace_code, String resource_code) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where("namespace_code").is(namespace_code).and("resource_code").is(resource_code));
        List<SccModifyKey> list = this.mongoTemplate.find(query, SccModifyKey.class);
        if(list.size() > 0){
            return list.get(0);
        }else{
            return null;
        }
    }

    @Override
    public List<SccModifyKey> getModifyDeploy(String resource_code) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where("resource_code").is(resource_code));
        List<SccModifyKey> list = this.mongoTemplate.find(query, SccModifyKey.class);
        if(list.size() > 0){
            return list;
        }else{
            return null;
        }
    }
}
