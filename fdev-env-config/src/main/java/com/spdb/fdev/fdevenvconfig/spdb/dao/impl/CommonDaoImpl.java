package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ICommonDao;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class CommonDaoImpl implements ICommonDao {
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public <T> List<T> commonQuery(Map<Object, Object> parmMap, String operator, Class<T> clazz) {
        Query query = new Query();
        Criteria[] criterias = this.valdates(parmMap);
        if (Constants.AND.equals(operator)) {
            query.addCriteria(new Criteria().andOperator(criterias));
        } else if (Constants.OR.equals(operator)) {
            query.addCriteria(new Criteria().orOperator(criterias));
        }
        return this.mongoTemplate.find(query, clazz);
    }

    /**
     * 对map判断是否是空的
     *
     * @param param
     * @return 返回一个查询条件
     */
    private Criteria[] valdates(Map<Object, Object> param) {

        if (param != null && param.size() != 0) {
            Criteria[] criteria = new Criteria[param.size()];
            int index = 0;
            for (Map.Entry<Object, Object> entry : param.entrySet()) {
                criteria[index] = Criteria.where(entry.getKey().toString()).is(entry.getValue());
                index++;
            }
            return criteria;
        }

        return null;
    }
}
