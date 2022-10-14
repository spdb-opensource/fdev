package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.pipeline.entity.Category;

import java.util.List;
import java.util.Map;

public interface ICategoryService {

    List<Category> getCategory(Category category);

    String addCategory(Map category);
}
