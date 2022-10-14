package com.spdb.fdev.spdb.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.spdb.dao.ICaasModifyKeyDao;
import com.spdb.fdev.spdb.entity.CaasModifyKey;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/9/30-13:57
 * @Description:caas_modifykey dao 层
 */
@Repository
public class CaasModifyKeyDaoImpl implements ICaasModifyKeyDao {

    @Resource
    private MongoTemplate mongoTemplate;

    /*
     * @author:guanz2
     * @Description: 更新caas_modifyKey信息
     */
    @Override
    public void updateModifyKeys(List<CaasModifyKey> caasModifyKeyList) {
        for(CaasModifyKey caasModifyKey : caasModifyKeyList){
            Query query = new Query();
            String deployment = (String) caasModifyKey.getModifykey().get(Dict.DEPLOYMENT);
            String cluster = (String) caasModifyKey.getModifykey().get(Dict.CLUSTER);
            String namespace = (String) caasModifyKey.getModifykey().get(Dict.NAMESPACE);
            query.addCriteria(Criteria.where(Dict.MODIFYDEPLOYMENT).is(deployment));
            query.addCriteria(Criteria.where(Dict.MODIFYCLUSTER).is(cluster));
            query.addCriteria(Criteria.where(Dict.MODIFYNAMESPACE).is(namespace));

            //如果数据库中没有该应用则新增，有则更新
            if(CommonUtils.isNullOrEmpty(queryModifyKeys(deployment, cluster, namespace))){
                this.mongoTemplate.insert(caasModifyKey, Dict.CAASMODIFYKEY);
            }else{
                Update update = new Update();
                Iterator<Map.Entry<String, Object>> it = caasModifyKey.getModifykey().entrySet().iterator();
                while (it.hasNext()){
                    Map.Entry<String, Object> entry = it.next();
                    update.set(Dict.MODIFYKEY+"."+entry.getKey(), entry.getValue());
                }
                this.mongoTemplate.updateFirst(query, update, CaasModifyKey.class);
            }
        }
    }

    @Override
    public CaasModifyKey queryModifyKeys(String deployment, String cluster, String namespace) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.MODIFYDEPLOYMENT).is(deployment));
        query.addCriteria(Criteria.where(Dict.MODIFYCLUSTER).is(cluster));
        query.addCriteria(Criteria.where(Dict.MODIFYNAMESPACE).is(namespace));
        List<CaasModifyKey> common = mongoTemplate.find(query, CaasModifyKey.class);
        if(common.size() == 0){
            return  null;
        }
        CaasModifyKey caasModifyKey = common.get(0);
        return caasModifyKey;
    }

    @Override
    public CaasModifyKey queryCommonModifyKeys() {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.TYPE).is(Dict.COMMON));
        List<CaasModifyKey> common = mongoTemplate.find(query, CaasModifyKey.class);
        if(common.size() > 0){
            CaasModifyKey caasModifyKey = common.get(0);
            return caasModifyKey;
        }else {
            return  null;
        }
    }
}
