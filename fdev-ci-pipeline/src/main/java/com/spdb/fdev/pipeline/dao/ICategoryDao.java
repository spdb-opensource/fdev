package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.Category;

import java.util.List;
import java.util.Map;

public interface ICategoryDao {


    List<Category> getCategory(Category category);

    String addCategory(Category category);

    List<Category> getCategoryByParam(Map map);
}
