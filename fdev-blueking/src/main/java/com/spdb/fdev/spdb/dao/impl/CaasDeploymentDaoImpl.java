package com.spdb.fdev.spdb.dao.impl;

import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.spdb.dao.ICaasDeploymentDao;
import com.spdb.fdev.spdb.entity.CaasDeployment;
import com.spdb.fdev.spdb.entity.SccPod;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author:guanz2
 * @Date:2021/9/30-13:56
 * @Description:caas_deployment dao 层
 */

@Repository
public class CaasDeploymentDaoImpl implements ICaasDeploymentDao {

    @Resource
    private MongoTemplate mongoTemplate;

    /*
    * @author:guanz2
    * @Description:caas_deployment插入数据
    */
    @Override
    public void addAll(List<CaasDeployment> listDeployment) {
        BasicDBObject index = new BasicDBObject();
        index.put(Dict.DEPLOYMENT, 1);
        index.put(Dict.NAMESPACE, 2);
        index.put(Dict.CLUSTER, 3);
        this.mongoTemplate.createCollection(CaasDeployment.class).createIndex(index);
        this.mongoTemplate.insertAll(listDeployment);
    }


    /*
    * @author:guanz2
    * @Description: 删除caas_deployment表
    */
    @Override
    public void dropCollection(String collectionName) {
        this.mongoTemplate.dropCollection(collectionName);
    }

    /*
    * @author:guanz2
    * @Description:根据应用名查询所有的deployment
    */
    @Override
    public List<CaasDeployment> queryCaasDeploymentCondition(String deploymentName) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where(Dict.DEPLOYMENT).is(deploymentName));
        List<CaasDeployment> list = new ArrayList<>();
        list = this.mongoTemplate.find(query, CaasDeployment.class);
        return list;
    }

    @Override
    public CaasDeployment queryCaasDeploymentCondition(String deploymentName, String cluster, String vlan) {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.where(Dict.DEPLOYMENT).is(deploymentName));
        List<CaasDeployment> deployments = this.mongoTemplate.find(query, CaasDeployment.class);
        CaasDeployment result = null;
        if(!CommonUtils.isNullOrEmpty(deployments)){
            for(CaasDeployment item : deployments){
                //areas是eww-nodes gray-nodes，都算做网银网
                if(cluster.equals(Dict.SHK1) && item.getCluster().equals(Dict.SHK1) && item.getNamespace().indexOf(Dict.GRAY) < 0 &&
                        (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
                if(cluster.equals(Dict.SHK2) && item.getCluster().equals(Dict.SHK2) && item.getNamespace().indexOf(Dict.GRAY) < 0 &&
                        (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
                if(cluster.equals(Dict.HFK1) && item.getCluster().equals(Dict.HFK1) && item.getNamespace().indexOf(Dict.GRAY) < 0 &&
                        (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
                if(cluster.equals(Dict.HFK2) && item.getCluster().equals(Dict.HFK2) && item.getNamespace().indexOf(Dict.GRAY) < 0 &&
                        (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
                if(cluster.equals(Dict.SHK1GRAY) && item.getCluster().equals(Dict.SHK1) && item.getNamespace().indexOf(Dict.GRAY) >= 0
                        && (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
                if(cluster.equals(Dict.SHK2GRAY) && item.getCluster().equals(Dict.SHK2) && item.getNamespace().indexOf(Dict.GRAY) >= 0
                        && (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
                if(cluster.equals(Dict.HFK1GRAY) && item.getCluster().equals(Dict.HFK1) && item.getNamespace().indexOf(Dict.GRAY) >= 0
                        && (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
                if(cluster.equals(Dict.HFK2GRAY) && item.getCluster().equals(Dict.HFK2) && item.getNamespace().indexOf(Dict.GRAY) >= 0
                        && (item.getArea().indexOf(vlan) >= 0 || item.getArea().indexOf("eww-nodes") >=0 || item.getArea().indexOf("gray-nodes") >=0)){
                    result = item;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public long queryCountByConditon(String cluster){
        Query query = new Query(Criteria.where(Dict.CLUSTER).is(cluster));
        Long count = this.mongoTemplate.count(query,CaasDeployment.class);
        return count;
    }

    @Override
    public List<CaasDeployment> fuzzyQueryCaasDeployment(String key, String vlaue) {
        List<CaasDeployment> list = new ArrayList<>();
        Sort sort = new Sort(Sort.Direction.ASC, Dict.DEPLOYMENT);
        if(!CommonUtils.isNullOrEmpty(key) && !CommonUtils.isNullOrEmpty(vlaue)){
            Pattern pattern = Pattern.compile("^.*" + vlaue
                    + ".*$", Pattern.CASE_INSENSITIVE);
            if (key.equals(Dict.DEPLOYNAME)){
                key = Dict.DEPLOYMENT; //key是deploy_name 对应caas的是deployment
            }
            Query query = new Query(Criteria.where(key).regex(pattern)).with(sort);
            list = this.mongoTemplate.find(query, CaasDeployment.class);
        }else{
            //key和value有一个为空，就给全量的数据
            Query query = new Query().with(sort);
            list = this.mongoTemplate.find(query, CaasDeployment.class);
        }
        return list;
    }

    @Override
    public List<String> getDistinctField(String field) {
        List<String> res = this.mongoTemplate.findDistinct(field, CaasDeployment.class , String.class);
        return res;
    }

}
