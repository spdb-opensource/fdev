package com.spdb.fdev.spdb.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.dao.ISccPodDao;
import com.spdb.fdev.spdb.entity.SccDeploy;
import com.spdb.fdev.spdb.entity.SccPod;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author:guanz2
 * @Date:2021/10/3-18:02
 * @Description: scc_pod dao层 实现类
 */
@Repository
public class SccPodDaoImpl implements ISccPodDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void addAll(List<SccPod> list) {
        this.mongoTemplate.insertAll(list);
    }

    @Override
    public void dropCollection(String collectionName) {
        this.mongoTemplate.dropCollection(collectionName);
    }

    @Override
    public long queryCountByCondition(String cluster) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where(Dict.CLUSTERCODE).is(cluster));
        long count = this.mongoTemplate.count(query, SccPod.class);
        return count;
    }

    @Override
    public long queryCountByCondition(List clusters) {
        long count = 0;
        for(Object cluster : clusters){
            Criteria criteria = new Criteria();
            Query query = new Query(criteria.where(Dict.CLUSTERCODE).is(cluster.toString()));
            count = count + this.mongoTemplate.count(query, SccPod.class);
        }
        return count;
    }

    //scc_deploy没有存储集群信息，只能通过scc_pod的集群信息，来查询deploy数据
    @Override
    public long queryClusterCount(String cluster) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where(Dict.CLUSTERCODE).is(cluster));
        List<SccPod> pods = this.mongoTemplate.find(query, SccPod.class);
        Set deploys = new HashSet();
        for(SccPod item : pods){
            String KEY = item.getOwner_code()+"###"+item.getNamespace_code();
            if(!deploys.contains(KEY)){
                deploys.add(KEY);
            }
        }
        long count = deploys.size();
        return count;
    }

    @Override
    public long queryClusterCount(List clusters) {
        Set deploys = new HashSet();
        for(Object cluster : clusters){
            Criteria criteria = new Criteria();
            Query query = new Query(criteria.where(Dict.CLUSTERCODE).is(cluster.toString()));
            List<SccPod> pods = this.mongoTemplate.find(query, SccPod.class);
            for(SccPod item : pods){
                String KEY = item.getOwner_code();
                if(!deploys.contains(KEY)){
                    deploys.add(KEY);
                }
            }
        }
        return deploys.size();
    }

    @Override
    public List queryPodsByCondition(String namespace_code, String owner_code) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where(Dict.NAMESPACECODE).is(namespace_code).and(Dict.OWNERCODE).is(owner_code));
        List<SccPod> pods = this.mongoTemplate.find(query, SccPod.class);
        return pods;
    }

    @Override
    public List<String> getDistinctField(String field) {
        List<String> res = this.mongoTemplate.findDistinct(field, SccPod.class , String.class);
        return res;
    }
}
