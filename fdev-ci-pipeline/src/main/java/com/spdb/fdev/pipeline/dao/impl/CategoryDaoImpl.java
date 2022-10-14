package com.spdb.fdev.pipeline.dao.impl;


import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.ICategoryDao;
import com.spdb.fdev.pipeline.entity.Category;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.DataInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CategoryDaoImpl implements ICategoryDao {

    private static Logger logger = LoggerFactory.getLogger(CategoryDaoImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Category> getCategory(Category category) {
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(category.getCategoryId())) {
            criteria.and(Dict.CATEGORYID).is(category.getCategoryId());
        }
        if (!CommonUtils.isNullOrEmpty(category.getCategoryLevel())) {
            criteria.and(Dict.CATEGORYLEVEL).is(category.getCategoryLevel());
        }
        if (!CommonUtils.isNullOrEmpty(category.getCategoryName())) {
            criteria.and(Dict.CATEGORYNAME).is(category.getCategoryName());
        }
        if (!CommonUtils.isNullOrEmpty(category.getParentId())) {
            criteria.and(Dict.PARENTID).is(category.getParentId());
        }
        Query query = new Query(criteria);
        query.fields().exclude("_id");
        List<Category> categories = this.mongoTemplate.find(query, Category.class, Dict.CATEGORY);
        return categories;
    }

    /**
     *
     * 新增/更新 category
     *
     * @param category
     * @return
     */
    @Override
    public String addCategory(Category category) {
        //不存在有分类记录，直接插入
        ObjectId id = new ObjectId();
        category.set_id(id);
        category.setCategoryId(id.toString());
        Category inserted = this.mongoTemplate.insert(category, Dict.CATEGORY);
        return inserted.getCategoryId();
    }

    @Override
    public List<Category> getCategoryByParam(Map map) {
        Criteria criteria = new Criteria();
        if (!CommonUtils.isNullOrEmpty(map.get(Dict.CATEGORYID))) {
            criteria.and(Dict.CATEGORYID).is(map.get(Dict.CATEGORYID));
        }
        if (!CommonUtils.isNullOrEmpty(map.get(Dict.CATEGORYLEVEL))) {
            criteria.and(Dict.CATEGORYLEVEL).is(map.get(Dict.CATEGORYLEVEL));
        }
        if (!CommonUtils.isNullOrEmpty(map.get(Dict.CATEGORYNAME))) {
            criteria.and(Dict.CATEGORYNAME).is(map.get(Dict.CATEGORYNAME));
        }
        if (!CommonUtils.isNullOrEmpty(map.get(Dict.PARENTID))) {
            criteria.and(Dict.PARENTID).is(map.get(Dict.PARENTID));
        }
        Query query = new Query(criteria);
        query.fields().exclude("_id");
        query.with(new Sort(Sort.Direction.DESC, Dict.CATEGORYID));
        List<Category> categories = this.mongoTemplate.find(query, Category.class, Dict.CATEGORY);
        return categories;
    }
}
