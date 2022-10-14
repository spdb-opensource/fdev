package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IBluekingDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Blueking;
import org.apache.commons.lang.StringUtils;
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
 * @Author: lisy26
 * @date: 2021/1/4 15:47
 * @ClassName: BluekingDaoImpl
 * @Description:
 */
@Repository
public class BluekingDaoImpl implements IBluekingDao {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * @Author： lisy26
     * @Description： 批量添加蓝鲸数据
     * @Date： 2021/1/18 20:15
     * @Param: [data]
     * @return: void
     **/
    @Override
    public void addAll(List<Blueking> data) {
        this.mongoTemplate.insertAll(data);
    }


    /**
     * @Author： lisy26
     * @Description： 删除蓝鲸blueking表数据
     * @Date： 2021/1/18 20:15
     * @Param: [collectionName]
     * @return: void
     **/
    @Override
    public void dropCollection(String collectionName) {
        mongoTemplate.dropCollection(collectionName);
    }

    /**
     * @Author： lisy26
     * @Description： 根据应用名或系统名模糊查询
     * @Date： 2021/1/18 20:15
     * @Param: [name]
     * @return: java.util.List<com.spdb.fdev.fdevenvconfig.spdb.entity.Blueking>
     **/
    @Override
    public List<Blueking> queryDeploymentList(String name) {
        Criteria c = new Criteria();
        c.and(Dict.ENTITY_TYPE).is(Dict.DEPLOYMENT);
        Pattern pattern = null;
        if (StringUtils.isNotBlank(name)) {
            pattern = Pattern.compile("^.*" + name
                    + ".*$", Pattern.CASE_INSENSITIVE);
            Criteria c1 = new Criteria();
            Criteria c2 = new Criteria();
            c1.and(Dict.DATASYSNAME).regex(pattern);
            c2.and(Dict.DATADEPLOYMENT).regex(pattern);
            c.orOperator(c1, c2);
        }
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.ASC, Dict.DATASYSNAME, Dict.DATADEPLOYMENT));
        List<Blueking> responseList = this.mongoTemplate.find(query, Blueking.class);
        return responseList;
    }

    /**
     * @Author： lisy26
     * @Description： 根据应用名查询container的相关信息
     * @Date： 2021/1/18 20:16
     * @Param: [deploymentSet]
     * @return: java.util.List<com.spdb.fdev.fdevenvconfig.spdb.entity.Blueking>
     **/
    @Override
    public List<Blueking> queryLimits(List<String> deploymentSet) {
        Criteria c = new Criteria();
        c.and(Dict.ENTITY_TYPE).is(Dict.CONTAINER);
        c.and(Dict.DATADEPLOYMENT).in(deploymentSet);
        Query query = new Query(c);
        List<Blueking> responseList = this.mongoTemplate.find(query, Blueking.class);
        return responseList;
    }

    /**
     * @Author： lisy26
     * @Description： 查询应用详细信息
     * @Date： 2021/1/18 20:16
     * @Param: [deployment, area, cluster, type]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Override
    public List<Map<String, Object>> queryBluekingDetail(String deployment, String area, String cluster, String type) {
        Criteria c = new Criteria();
        c.and(Dict.DATADEPLOYMENT).is(deployment);
        c.and(Dict.DATAAREA).is(area);
        c.and(Dict.DATACLUSTER).is(cluster);
        c.and(Dict.ENTITY_TYPE).is(type);
        Query query = new Query(c);
        List<Map<String, Object>> responseList = new ArrayList<>();
        List<Blueking> resultList = mongoTemplate.find(query, Blueking.class);
        for (int i = 0; i < resultList.size(); i++) {
            Blueking bluking = resultList.get(i);
            Map<String, Object> responseMap = bluking.getData();
            responseList.add(responseMap);
        }
        return responseList;
    }


}
