package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelTemplateDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelTemplate;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author: lisy26
 * @date: 2020/11/16 10:21
 * @ClassName ModelTemplateDaoImpl
 * @Description
 **/
@Repository
public class ModelTemplateDaoImpl implements IModelTemplateDao {

    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private ServiceUtil serviceUtil;

    @Override
    public ModelTemplate add(ModelTemplate modelTemplate) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        modelTemplate.set_id(objectId);
        modelTemplate.setId(id);
        //初始化数据,系统自动添加
        //以前为String类型，如今改为Integer类型
        modelTemplate.setStatus(Integer.valueOf(Constants.STATUS_OPEN));
        modelTemplate.setCtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        modelTemplate.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        modelTemplate.setOpno(serviceUtil.getOpno());
        return this.mongoTemplate.save(modelTemplate);
    }

    @Override
    public Map<String, Object> pageQuery(Map map) throws Exception{
        Map<String, Object> responseMap = new HashMap<>();
        Criteria c = new Criteria();
        c.and(Dict.STATUS).ne((Integer.valueOf(Constants.ZERO)));
        if (StringUtils.isNotBlank((String) map.get(Dict.NAMEEN))) {
            Pattern pattern = Pattern.compile("^.*" + map.get(Dict.NAMEEN)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            c.and(Dict.NAMEEN).regex(pattern);
        }
        if (StringUtils.isNotBlank((String) map.get(Dict.NAMECN))) {
            Pattern pattern = Pattern.compile("^.*" + map.get(Dict.NAMECN)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            c.and(Dict.NAMECN).regex(pattern);
        }
        if (StringUtils.isNotBlank((String) map.get(Dict.ENVKEY))) {
            Pattern pattern = Pattern.compile("^.*" + map.get(Dict.ENVKEY)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            c.and(Dict.ENV_KEY_PROP_KEY).regex(pattern);
        }
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.ASC, Dict.NAMEEN));
        Long total = mongoTemplate.count(query, ModelTemplate.class);
        responseMap.put(Dict.TOTAL, total);
        long startPage = ((Integer) map.get(Dict.PAGE) - 1) * ((Integer) map.get(Dict.PERPAGE));
        query.skip(startPage).limit((Integer) map.get(Dict.PERPAGE));
        responseMap.put(Dict.LIST, this.mongoTemplate.find(query, ModelTemplate.class));
        return responseMap;
    }

    @Override
    public ModelTemplate queryById(String id) {
        return this.mongoTemplate.findOne(Query.query(Criteria.where(Dict.ID).is(id)), ModelTemplate.class);
    }

	@Override
	public ModelTemplate queryByNameEn(String nameEn) {
		return this.mongoTemplate.findOne(Query.query(Criteria.where(Dict.NAMEEN).is(nameEn)), ModelTemplate.class);
	}

}
