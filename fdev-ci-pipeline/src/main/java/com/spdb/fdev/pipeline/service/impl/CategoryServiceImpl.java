package com.spdb.fdev.pipeline.service.impl;


import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.ICategoryDao;
import com.spdb.fdev.pipeline.entity.Category;
import com.spdb.fdev.pipeline.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private ICategoryDao categoryDao;

    @Override
    public List<Category> getCategory(Category category) {
        return this.categoryDao.getCategory(category);
    }

    @Override
    public String addCategory(Map category) {
        Category addEntity = new Category();
        addEntity.setCategoryLevel((String) category.get(Dict.CATEGORYLEVEL));
        addEntity.setCategoryName((String) category.get(Dict.CATEGORYNAME));
        if (!CommonUtils.isNullOrEmpty(category.get(Dict.PARENTID)))
            addEntity.setParentId((String) category.get(Dict.PARENTID));
        return this.categoryDao.addCategory(addEntity);
    }
}
