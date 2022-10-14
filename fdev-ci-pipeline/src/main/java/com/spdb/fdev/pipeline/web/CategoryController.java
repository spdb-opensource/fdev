package com.spdb.fdev.pipeline.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.pipeline.entity.Category;
import com.spdb.fdev.pipeline.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    
    @Autowired
    private ICategoryService categoryService;

    @RequestMapping(value = "/addCategory", method = RequestMethod.POST)
    public JsonResult addCategory(@RequestBody Map request) throws Exception {
        return JsonResultUtil.buildSuccess(this.categoryService.addCategory(request));
    }

    @RequestMapping(value = "/queryCategory", method = RequestMethod.POST)
    public JsonResult queryCategory(@RequestBody Map request) throws Exception {
        Category queryParam;
        if (CommonUtils.isNullOrEmpty(request)) {
            //没有request默认查全部
            queryParam = new Category();
        }else
            queryParam = CommonUtils.map2Object(request, Category.class);
        return JsonResultUtil.buildSuccess(this.categoryService.getCategory(queryParam));
    }
    
}
