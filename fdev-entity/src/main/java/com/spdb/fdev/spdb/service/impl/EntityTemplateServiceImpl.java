package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.spdb.dao.IEntityTemplateDao;
import com.spdb.fdev.spdb.entity.EntityTemplate;
import com.spdb.fdev.spdb.service.IEntityTemplateService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EntityTemplateImpl
 * @DESCRIPTION 实体模板实现类
 * @Author xxx
 * @Date 2021/5/8 15:43
 * @Version 1.0
 */
@Service
public class EntityTemplateServiceImpl implements IEntityTemplateService {

    @Autowired
    IEntityTemplateDao entityTemplateDao;

    @Override
    public void add(Map<String, Object> req) throws Exception {
        List<Map<String, Object>> list = (List<Map<String, Object>>) req.get(Dict.DATA);
        List<EntityTemplate> entityTemplateList = new ArrayList<>();
        if (!CommonUtil.isNullOrEmpty(list)) {
            for (Map<String, Object> entityTemplateMap : list) {
                EntityTemplate entityTemplate = CommonUtil.map2Object(entityTemplateMap, EntityTemplate.class);
                ObjectId id = new ObjectId();
                entityTemplate.set_id(id);
                entityTemplate.setId(id.toString());
                entityTemplateList.add(entityTemplate);
            }
            entityTemplateDao.save(entityTemplateList);
        }

    }

    @Override
    public List<EntityTemplate> queryList(Map<String, Object> req) throws Exception {
        String likeKey = (String) req.get(Dict.LIKEKEY);
        return entityTemplateDao.queryList(likeKey);
    }

    @Override
    public EntityTemplate queryById(Map<String, Object> req) throws Exception {
        String id = (String) req.get(Dict.ID);
        return entityTemplateDao.queryById(id);
    }
}
