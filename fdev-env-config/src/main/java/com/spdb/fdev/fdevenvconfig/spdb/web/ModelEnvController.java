package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import com.spdb.fdev.fdevenvconfig.spdb.service.IModelEnvService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xxx
 * @date 2019/7/5 13:13
 */
@Api(tags = "实体环境接口")
@RequestMapping("/api/v2/var")
@RestController
public class ModelEnvController {

    @Autowired
    private IModelEnvService modelEnvService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 聚合查询接口，根据条件查询app-model-env记录
     *
     * @param modelEnv 实例对象，如果属性有值，作作为查询条件，如属性全部无值，则作全表查询
     * @return 查询结果集
     * @throws Exception
     * @author xxx
     */
    @PostMapping("/query")
    public JsonResult query(@RequestBody @ApiParam ModelEnv modelEnv) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.query(modelEnv));
    }

    /**
     * 新增接口，在app-model-env新增一条数据
     *
     * @param map 实例中的每个key值为新增内容
     * @return 新增成功的那条记录
     * @throws Exception
     * @author xxx
     */
    @PostMapping("/add")
    @NoNull(require = {Constants.ENV_ID, Constants.MODEL_ID, Constants.VARIABLES, Constants.VARIABLES_ID}, parameter = LinkedHashMap.class)
    public JsonResult add(@RequestBody @ApiParam Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.add(map));
    }

    /**
     * 根据ID修改 app-model-env
     *
     * @param map 实例中的ID作为查询条件，其余key值作为修改
     * @return 修改后的那条数据
     * @throws Exception
     * @author xxx
     */
    @PostMapping("/update")
    @NoNull(require = {Constants.ENV_ID, Constants.MODEL_ID, Constants.VARIABLES, Constants.VARIABLES_ID}, parameter = LinkedHashMap.class)
    public JsonResult update(@RequestBody @ApiParam Map map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.update(map));
    }

    /**
     * 根据ID，删除app-model-env表中的一条记录（逻辑删除）
     *
     * @param map 实例中的ID,作为查询条件
     * @return
     * @throws Exception
     * @author xxx
     */
    @PostMapping("/delete")
    @RequestValidate(NotEmptyFields = {Constants.ID, Dict.VERFITYCODE})
    public JsonResult delete(@RequestBody Map map) throws Exception {
        this.modelEnvService.delete(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据环境英文名称(如：sit,uat,real,...),实体类型(如：deploy,runtime,...),查询出对应的大key和其对应值
     * 产出：｛namespace.大key：大KeyValue,...｝
     *
     * @param map 查询条件  env:环境英文名称 ，type:实体类型
     * @return
     * @throws Exception
     * @author xxx
     */
    @PostMapping("/queryEnvBySlug")
    @NoNull(require = {Constants.ENV, Constants.TYPE}, parameter = LinkedHashMap.class)
    public JsonResult queryEnvBySlug(@RequestBody @ApiParam Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.queryEnvBySlug(map));
    }

    /**
     * 根据环境英文名称(如：sit,uat,real,...),实体类型(如：deploy,runtime,...),查询出对应的大key和属性的中文名和其对应值
     * 产出：[｛name_en:namespace.大key,name_cn:key的中文名,value:大KeyValue｝...]
     *
     * @param map 查询条件  env:环境英文名称 ，type:实体类型
     * @return
     * @throws Exception
     * @author yuxudong1512025
     */
    @PostMapping("/queryVarByEnvAndType")
    @NoNull(require = {Constants.ENV, Constants.TYPE, Constants.GITLAB_ID}, parameter = LinkedHashMap.class)
    public JsonResult queryEnvwithNameCnBySlug(@RequestBody @ApiParam Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.queryVarByEnvAndType(map));
    }

    /**
     * 根据环境标签(如：sit,uat,rel,...),作用域(如：deploy,comm,...),应用GitlabId,查询出实体属性对应的的大key、中文名及其对应值
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("/queryVarByLabelAndType")
    @NoNull(require = {Dict.LABEL, Constants.TYPE, Constants.GITLAB_ID}, parameter = LinkedHashMap.class)
    public JsonResult queryVarByLabelAndType(@RequestBody @ApiParam Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.queryVarByLabelAndType(map));
    }

    /**
     * 根据环境英文名称查询环境实体
     *
     * @param map 环境英文名
     * @return 环境实体映射数据
     * @throws Exception
     */
    @PostMapping("queryModelEnvByEnvNameEn")
    @NoNull(require = {Constants.NAME_EN, Constants.GITLAB_ID}, parameter = LinkedHashMap.class)
    public JsonResult queryModelEnvByEnvNameEn(@RequestBody @ApiParam Map<String, Object> map) throws Exception {
        map.put(Constants.TYPE, "deploy");
        map.put(Constants.ENV, map.get(Constants.NAME_EN));
        return JsonResultUtil.buildSuccess(this.modelEnvService.queryEnvMappingwithCn(map));
    }

    /**
     * 根据实体英文名称和环境英文名查询环境实体
     *
     * @param map 环境英文名
     * @return 环境实体映射数据
     * @throws Exception
     */
    @PostMapping("queryModelEnvByModelNameEn")
    @NoNull(require = {Constants.NAME_EN}, parameter = LinkedHashMap.class)
    public JsonResult queryModelEnvByModelNameEn(@RequestBody Map<String, Object> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.queryModelEnvByModelNameEn(map));
    }

    /**
     * 通过环境英文名或者环境id查询环境实体信息
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping("queryModelEnvByEnvIdOrName")
    public JsonResult queryModelEnvByEnvIdOrName(@RequestBody @ApiParam Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.queryModelEnvByEnvIdOrName(map));
    }

    @PostMapping("/ciDecrypt")
    @NoNull(require = {Dict.FDEV_CAAS_PD}, parameter = LinkedHashMap.class)
    public JsonResult ciDecrypt(@RequestBody Map<String, String> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.ciDecrypt(map));
    }

    @PostMapping("pageQuery")
    @NoNull(require = {Dict.PAGE, Dict.PER_PAGE}, parameter = LinkedHashMap.class)
    public JsonResult pageQuery(@RequestBody Map<String, Object> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.modelEnvService.pageQuery(map));
    }

    /**
     * 根据value值查询哪些实体的实体属性在哪些环境用了这个值
     *
     * @param requestMap value:需要查询的值   type:CI实体还是非CI实体    labels:环境标签
     * @return
     */
    @PostMapping("queryModelEnvByValue")
    @NoNull(require = {Dict.VALUE}, parameter = LinkedHashMap.class)
    public JsonResult queryModelEnvByValue(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(this.modelEnvService.queryModelEnvByValue(requestMap));
    }

    /**
     * @auth : gejunpeng
     * @Description: 根据环境英文名和绑定的实体字段查询对应的值
     * @Return: map 绑定字段：值
     * @Param: map envName variables_key
     */
    @PostMapping("/getVariablesValue")
    @NoNull(require = {Dict.ENV_NAME}, parameter = LinkedHashMap.class)
    public JsonResult getVariablesValue(@RequestBody Map<String, Object> requestParam) throws Exception {
        // variables_key
        return JsonResultUtil.buildSuccess(this.modelEnvService.getVariablesValue(requestParam));
    }

}
