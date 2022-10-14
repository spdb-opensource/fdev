package com.spdb.fdev.spdb.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.dao.ICaasPodDao;
import com.spdb.fdev.spdb.entity.CaasDeployment;
import com.spdb.fdev.spdb.entity.CaasPod;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/9/30-13:56
 * @Description: caas_pod dao 层
 */
@Repository
public class CaasPodDaoImpl implements ICaasPodDao {

    @Resource
    private MongoTemplate mongoTemplate;

    /*
    * @author:guanz2
    * @Description:caas_pod插入数据
    */
    @Override
    public void addAll(List<CaasPod> listPod) {
        this.mongoTemplate.insertAll(listPod);
    }

    /*
    * @author:guanz2
    * @Description:删除caas_pod信息
    */
    @Override
    public void dropCollection(String collectionName) {
        this.mongoTemplate.dropCollection(collectionName);
    }

    /*
    * @author:guanz2
    * @Description:根据应用名查询所有pod
    */
    @Override
    public List<CaasPod> queryCaasPodCondition(String deployment) {
        Criteria criteria = new Criteria();
        Query query = new Query(Criteria.where(Dict.DEPLOYMENT));
        List<CaasPod> pods = this.mongoTemplate.find(query, CaasPod.class);
        return pods;
    }

    @Override
    public List<CaasPod> queryCaasPodCondition(String deployment, String cluster, String namespace) {
        Criteria criteria = new Criteria();
        Query query = new Query(Criteria.where(Dict.DEPLOYMENT).is(deployment).and(Dict.CLUSTER).is(cluster).and(Dict.NAMESPACE).is(namespace));
        List<CaasPod> pods = this.mongoTemplate.find(query, CaasPod.class);
        return pods;
    }

    @Override
    public long queryCountByCondition(String cluster) {
        Query query = new Query(Criteria.where(Dict.CLUSTER).is(cluster));
        Long count = this.mongoTemplate.count(query, CaasPod.class);
        return count;
    }
}
