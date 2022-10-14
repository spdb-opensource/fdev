package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.service.IModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author xxx
 * @date 2019/7/5 13:13
 */
@Api(tags = "实体接口")
@RequestMapping("/api/v2/model")
@RestController
public class ModelController {

    @Autowired
    private IModelService modelService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 查询全量的实体信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    public JsonResult query(@RequestBody @ApiParam Model model) throws Exception {
        List<Model> modelList = this.modelService.query(model);
        return JsonResultUtil.buildSuccess(this.modelService.joinTemplateName(modelList));
    }

    /**
     * 用户自定义的实体配置不要返回前端deploy类型的实体
     *
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping("/queryWithoutDeploy")
    public JsonResult queryWithoutDeploy(@RequestBody @ApiParam Model model) throws Exception {
        List<Model> models = this.modelService.query(model);
        List<Model> result = new ArrayList<>();
        for (Model m : models) {
            if (Constants.DEPLOY.equals(m.getScope())) {
                continue;
            }
            result.add(m);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 用户自定义部署时的实体属性映射，只返回前端deploy类型的实体
     *
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping("/queryWithDeploy")
    public JsonResult queryWithDeploy(@RequestBody @ApiParam Model model) throws Exception {
        List<Model> models = this.modelService.query(model);
        List<Model> result = new ArrayList<>();
        for (Model m : models) {
            if (Constants.DEPLOY.equals(m.getScope())) {
                result.add(m);
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 新增实体信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping("/add")
    @NoNull(require = {Constants.NAME_EN, Constants.NAME_CN, Constants.ENV_KEY_TYPE,
            Constants.FIRST_CATEGORY, Constants.SECOND_CATEGORY, Constants.SUFFIX_NAME,
            Constants.ENV_KEY, Constants.SCOPE, Constants.ENV_KEY_NAME_EN,
            Constants.ENV_KEY_NAME_CN, Constants.ENV_KEY_REQUIRE }, parameter = Model.class)
    public JsonResult add(@RequestBody @ApiParam Model model) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelService.add(model));
    }

    /**
     * 修改实体信息
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/update")
    @NoNull(require = {Constants.NAME_EN, Constants.NAME_CN,
            Constants.FIRST_CATEGORY, Constants.SECOND_CATEGORY, Constants.SUFFIX_NAME,
            Constants.ENV_KEY, Constants.SCOPE, Constants.VERSION, Constants.ENV_KEY_NAME_EN,
            Constants.ENV_KEY_NAME_CN, Constants.ENV_KEY_REQUIRE}, parameter = Model.class)
    public JsonResult update(@RequestBody @ApiParam Model model) throws Exception {

        return JsonResultUtil.buildSuccess(this.modelService.update(model));
    }

    /**
     * 删除实体信息
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/delete")
    @RequestValidate(NotEmptyFields = {Constants.ID, Dict.VERFITYCODE})
    public JsonResult delete(@RequestBody Map map) throws Exception {

        this.modelService.delete(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 模糊查询实体
     *
     * @return 返回实体
     * @throws Exception
     */
    @PostMapping("/queryFuzz")
    public JsonResult queryFuzz(@RequestBody @ApiParam Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelService.queryFuzz(map));
    }

    /***
     * 查询实体类型常量(包括作用域)
     * @return 返回实体类型常量
     * @throws Exception
     */
    @PostMapping("/queryModelCategory")
    public JsonResult queryModelCategory() throws Exception {
        return JsonResultUtil.buildSuccess(this.modelService.queryModelCategory());
    }

    /**
     * 查询排除非以本应用名结尾的私有实体
     *
     * @param model
     * @return
     * @throws Exception
     */
    @PostMapping("/queryExcludePirvateModel")
    public JsonResult queryExcludePirvateModel(@RequestBody @ApiParam Model model) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelService.queryExcludePirvateModel(model));
    }

    /**
     * 实体分页
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/pageQuery")
    @NoNull(require = {Dict.PAGE, Dict.PER_PAGE}, parameter = LinkedHashMap.class)
    public JsonResult query(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelService.pageQuery(map));
    }

    /**
     * 查询全量的实体属性列表
     * @return
     * @throws Exception
     */
    @PostMapping("/queryNoCiEnvKeyList")
    public JsonResult queryNoCiEnvKeyList() throws Exception {
        return JsonResultUtil.buildSuccess(this.modelService.queryNoCiEnvKeyList());
    }

    /**
     * 查询非CI实体列表
     * @return
     * @throws Exception
     */
    @PostMapping("/configModel")
    public JsonResult configModel() throws Exception {
        return JsonResultUtil.buildSuccess(this.modelService.configModel());
    }

}
