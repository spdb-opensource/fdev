package com.spdb.fdev.spdb.dao.impl;

import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.spdb.dao.ISccDeploymentDao;
import com.spdb.fdev.spdb.entity.CaasDeployment;
import com.spdb.fdev.spdb.entity.SccDeploy;
import com.spdb.fdev.spdb.entity.SccPod;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author:guanz2
 * @Date:2021/10/3-18:01
 * @Description: scc_deploy dao层 实现类
 */
@Repository
public class SccDeploymentDaoImpl implements ISccDeploymentDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void addAll(List<SccDeploy> list) {
        if(list.size() > 0){
            for (SccDeploy item : list){
                String yaml = StringEscapeUtils.unescapeJava(item.getYaml());
                item.setYaml(yaml);
            }
        }
        BasicDBObject index = new BasicDBObject();
        index.put(Dict.RESOURCECODE, 1);
        index.put(Dict.NAMESPACECODE, 2);
        this.mongoTemplate.createCollection(SccDeploy.class).createIndex(index);
        this.mongoTemplate.insertAll(list);
    }

    @Override
    public void dropCollection(String collectionName) {
        this.mongoTemplate.dropCollection(collectionName);
    }

    @Override
    public List<SccDeploy> getSccDeployCondition(String resource_code) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where("resource_code").is(resource_code));
        List<SccDeploy> list = this.mongoTemplate.find(query,SccDeploy.class);
        if(list.size() > 0){
            return list;
        }else {
            return null;
        }
    }

    @Override
    public SccDeploy getSccDeployCondition(String namespace_code, String resource_code) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where(Dict.RESOURCECODE).is(resource_code).and(Dict.NAMESPACECODE).is(namespace_code));
        List<SccDeploy> list = this.mongoTemplate.find(query,SccDeploy.class);
        if(list.size() > 0){
            return list.get(0);
        }else {
            return null;
        }
    }

    @Override
    public List getAllSccDeploy() {
        Sort sort = new Sort(Sort.Direction.ASC, Dict.RESOURCECODE);
        Query query = new Query().with(sort);
        List<SccDeploy> list = this.mongoTemplate.find(query, SccDeploy.class);
        return  list;
    }

    @Override
    public long querySccCount() {
       Query query = new Query();
       long count = this.mongoTemplate.count(query, SccDeploy.class);
       return count;
    }

    @Override
    public List<String> getDistinctField(String field) {
        List<String> res = this.mongoTemplate.findDistinct(field, SccDeploy.class , String.class);
        return res;
    }

    @Override
    public List<SccDeploy> fuzzyQueryCaasDeployment(String key, String vlaue) {
        List<SccDeploy> list = new ArrayList<>();
        Sort sort = new Sort(Sort.Direction.ASC, Dict.RESOURCECODE);
        if(!CommonUtils.isNullOrEmpty(key) && !CommonUtils.isNullOrEmpty(vlaue)){
            Pattern pattern = Pattern.compile("^.*" + vlaue
                    + ".*$", Pattern.CASE_INSENSITIVE);
            Query query = new Query(Criteria.where(key).regex(pattern)).with(sort);
            list = this.mongoTemplate.find(query, SccDeploy.class);
        }else{
            //key和value有一个为空，就给全量的数据
            Query query = new Query().with(sort);
            list = this.mongoTemplate.find(query,SccDeploy.class);
        }
        return list;
    }
}
