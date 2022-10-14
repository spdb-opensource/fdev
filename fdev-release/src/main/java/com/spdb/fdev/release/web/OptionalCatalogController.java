package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.OptionalCatalog;
import com.spdb.fdev.release.service.IOptionalCatalogService;
import com.spdb.fdev.release.service.IRoleService;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RequestMapping("/api/optionalcatalog")
@RestController
public class OptionalCatalogController {

    private static final Logger logger = LoggerFactory.getLogger(OptionalCatalogController.class);

    @Autowired
    private IOptionalCatalogService optionalCatalogService;

    @Autowired
    private IRoleService roleService;

    @PostMapping(value = "/query")
    public JsonResult query() {
        // 列表展示只查询数据库文件与配置文件3-数据库更新4-配置文件更新5-公共配置文件更新6-老nas更新
        // 1-nbnewperAPP  2-docker  排除其他全部查出
        return JsonResultUtil.buildSuccess(optionalCatalogService.query(Arrays.asList("1", "2")));
    }

    @RequestValidate(NotEmptyFields = {Dict.CATALOG_NAME, Dict.CATALOG_TYPE, Dict.DESCRIPTION})
    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String catalogName = requestParam.get(Dict.CATALOG_NAME);
        String catalogType = requestParam.get(Dict.CATALOG_TYPE);
        String description = requestParam.get(Dict.DESCRIPTION);
        ObjectId id = new ObjectId();
        try {
            optionalCatalogService.save(new OptionalCatalog(id, id.toString(), catalogName, catalogType, description));
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{catalogName + "已存在！"});
        }
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.CATALOG_NAME, Dict.CATALOG_TYPE, Dict.DESCRIPTION})
    @PostMapping(value = "/update")
    public JsonResult update(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String id = requestParam.get(Dict.ID);
        String catalogName = requestParam.get(Dict.CATALOG_NAME);
        String catalogType = requestParam.get(Dict.CATALOG_TYPE);
        String description = requestParam.get(Dict.DESCRIPTION);
        optionalCatalogService.update(new OptionalCatalog(id, catalogName, catalogType, description));
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/delete")
    public JsonResult delete(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String id = requestParam.get(Dict.ID);
        optionalCatalogService.deleteById(id);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据目录名称修改目录类型
     * @param requestParam
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.CATALOG_NAME, Dict.CATALOG_TYPE})
    @PostMapping(value = "/editTypeByName")
    public JsonResult editTypeByName(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String name = requestParam.get(Dict.CATALOG_NAME);
        String type = requestParam.get(Dict.CATALOG_TYPE);
        // 修改optional_catalog介质目录表 asset_catalog模板与介质目录关联表
        Map<String, Object> map = optionalCatalogService.editTypeByName(name, type);
        return JsonResultUtil.buildSuccess(map);
    }

}
