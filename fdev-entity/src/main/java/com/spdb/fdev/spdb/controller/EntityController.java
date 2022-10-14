package com.spdb.fdev.spdb.controller;


import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.FdevUserCacheUtil;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.spdb.entity.Entity;
import com.spdb.fdev.spdb.service.IConfigFileService;
import com.spdb.fdev.spdb.service.IEntityService;
import com.spdb.fdev.spdb.service.IRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


/**
 * @author huyz
 * @description  
 * @date 2021/5/7
 */
@RestController
@RequestMapping("/api/entity")
public class EntityController {
	
    @Autowired
    private IEntityService entityService;
    @Autowired
    private IRestService restService;
    @Autowired
    private IConfigFileService configFileService;
    @Autowired
	private UserVerifyUtil userVerifyUtil;
    @Autowired
    private FdevUserCacheUtil fdevUserCacheUtil;
    /**
     * 查询实体模型列表
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryEntityModel")
    public JsonResult queryEntityModel(@RequestBody Map<String, Object> requestParam) throws Exception {
    	return JsonResultUtil.buildSuccess(entityService.queryEntityModel(requestParam));
    }
    
    /**
     * 查询实体详情
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/queryEntityModelDetail")
    public JsonResult queryEntityModelDetail(@RequestBody Map<String, Object> requestParam) throws Exception {
    	String id = (String) requestParam.get(Dict.ID);//实体ID
    	return JsonResultUtil.buildSuccess(entityService.queryEntityModelDetail(id));
    }
    
     /**
      * 新建实体
      *
      * @param requestParam
      * @return
      * @throws Exception
      */
    @RequestValidate(NotEmptyFields = {Dict.NAMEEN,Dict.NAMECN})
    @PostMapping(value = "/addEntity")
    public JsonResult addEntityModel(@RequestBody Map<String, Object> requestParam) throws Exception {
    	return JsonResultUtil.buildSuccess(entityService.addEntityModel(requestParam));
    }
    
  /**
   * 编辑实体
   *
   * @param
   * @return
   * @throws Exception
   */
    @PostMapping(value = "/updateEntity")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult updateEntity(@RequestBody Map<String, Object> requestParam) throws Exception {
    	return JsonResultUtil.buildSuccess(entityService.updateEntityModel(requestParam));
    }
    
    /**
     * 新增实体映射
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
	 @PostMapping(value = "/addEntityClass")
	 @RequestValidate(NotEmptyFields = {Dict.ID,Dict.PROPERTIESVALUE})
	 public JsonResult addEntityClass(@RequestBody Map<String, Object> requestParam) throws Exception {
	   return JsonResultUtil.buildSuccess(entityService.addEntityClass(requestParam));
	 }
	 
	 /**
     * 删除实体映射
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
	 @PostMapping(value = "/deleteEntityClass")
	 @RequestValidate(NotEmptyFields = {Dict.ID,Dict.ENVTYPE,Dict.ENVNAME})
	 public JsonResult deleteEntityClass(@RequestBody Map<String, Object> requestParam) throws Exception {
	   return JsonResultUtil.buildSuccess(entityService.deleteEntityClass(requestParam));
	 }
	 
	 /**
     * 编辑实体映射
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
	 @PostMapping(value = "/updateEntityClass")
	 @RequestValidate(NotEmptyFields = {Dict.ID,Dict.ENVTYPE,Dict.ENVNAME,Dict.PROPERTIESVALUE})
	 public JsonResult updateEntityClass(@RequestBody Map<String, Object> requestParam) throws Exception {
	   return JsonResultUtil.buildSuccess(entityService.updateEntityClass(requestParam));
	 }
    
   /**
    * 校验实体是否重复
    *
    * @param requestParam
    * @return
    * @throws Exception
    */

    @PostMapping(value = "/checkEntity")
    public JsonResult checkEntityModel(@RequestBody Map<String, Object> requestParam) throws Exception {
    	return JsonResultUtil.buildSuccess(entityService.checkEntityModel(requestParam));
    }
    

     
     /**
      * 根据环境英文名和实体字段集合查询实体环境映射值
      *
      * @param requestParam
      * @return
      * @throws Exception
      */
      @PostMapping(value = "/getVariablesValue")
      @RequestValidate(NotEmptyFields = {Dict.ENVNAMES})
      public JsonResult getVariablesValue(@RequestBody Map<String, Object> requestParam) throws Exception {
    	List<String> envNames = (List<String>) requestParam.get(Dict.ENVNAMES);//环境英文名集合
      	List<String> variablesKey = (List<String>) requestParam.get(Dict.VARIABLESKEY);//实体字段数组
      	return JsonResultUtil.buildSuccess(entityService.getVariablesValue(envNames,variablesKey));
      }
    
      /**
       * 根据环境英文名和实体字段集合查询实体环境映射值
       *
       * @param requestParam
       * @return
       * @throws Exception
       */
       @PostMapping(value = "/queryEntityMapping")
       @RequestValidate(NotEmptyFields = {Dict.ENTITYIDLIST})
       public JsonResult queryEntityMapping(@RequestBody Map<String, Object> requestParam) throws Exception {
    	List<String> entityIdList = (List<String>) requestParam.get(Dict.ENTITYIDLIST);//实体id集合
       	String envNameEn = (String) requestParam.get(Dict.ENVNAMEEN);//环境英文名
       	return JsonResultUtil.buildSuccess(entityService.queryEntityMapping(entityIdList,envNameEn));
       }

    /**
     * 用于保存配置模板
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/saveConfigTemplate")
    @RequestValidate(NotEmptyFields = {Dict.CONTENT, Dict.FEATUREBRANCH})
    public JsonResult saveConfigTemplate(@RequestBody Map<String, Object> requestParam) throws Exception {
        if (CommonUtil.isNullOrEmpty(requestParam.get(Dict.PROJECTID)) && CommonUtil.isNullOrEmpty(requestParam.get(Dict.GITLABID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"参数id上送异常"});
        }
        return JsonResultUtil.buildSuccess(this.configFileService.saveConfigTemplate(requestParam));
    }

    /**
     * 查询排除非以本应用名结尾的私有实体
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/queryServiceEntity")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryServiceEntity(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.entityService.queryServiceEntity(requestParam));
    }

    /**
     * 配置模板回显
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/queryConfigTemplate")
    @RequestValidate(NotEmptyFields = {Dict.FEATUREBRANCH, Dict.PROJECTID})
    public JsonResult queryConfigTemplate(@RequestBody Map<String, String> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.entityService.queryConfigTemplate(requestParam));
    }
    
    /**
     * 配置模板预览
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/previewConfigFile")
    @RequestValidate(NotEmptyFields = {Dict.ENVNAME, Dict.CONTENT,Dict.PROJECTID})
    public JsonResult previewConfigFile(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.entityService.previewConfigFile(requestParam));
    }
    
    /**
     * 上传配置文件到开发配置中心
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/saveDevConfigProperties")
    @RequestValidate(NotEmptyFields = {Dict.PROJECTID, Dict.CONTENT})
    public JsonResult saveDevConfigProperties(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.entityService.saveDevConfigProperties(requestParam));
    }
    
    /**
     * 查询条线下所有实体包含系统，应用。
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping("/querySectionEntity")
    public JsonResult querySectionEntity(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.entityService.querySectionEntity(requestParam));
    }
    
    /**
     * 配置文件依赖分析
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/queryConfigDependency")
    @RequestValidate(NotEmptyFields = {Dict.ENTITYNAMEEN})
    public JsonResult queryConfigDependency(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileService.queryConfigDependency(requestParam));
    }

    /**
     * 部署依赖分析
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/queryDeployDependency")
    @RequestValidate(NotEmptyFields = {Dict.ENTITYNAMEEN})
    public JsonResult queryDeployDependency(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.configFileService.queryDeployDependency(requestParam));
    }

    /**
     * 查看实体操作日志信息
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/getMappingHistoryList") 
    public JsonResult getMappingHistoryList(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.entityService.getMappingHistoryList(requestParam));
    }

    @PostMapping("/deleteEntity")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult deleteEntity(@RequestBody Map<String, Object> requestParam) throws Exception {
        entityService.deleteEntity(requestParam);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryEntityLog")
    @RequestValidate(NotEmptyFields = {Dict.ENTITYID})
    public JsonResult queryEntityLog(@RequestBody Map<String, Object> requestParam) throws Exception {
        Map<String, Object> entityLog = entityService.queryEntityLog(requestParam);
        return JsonResultUtil.buildSuccess(entityLog);
	}

    @PostMapping("/copyEntity")
    public JsonResult copyEntity(@RequestBody Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(entityService.copyEntity(requestParam));
    }
    
    @PostMapping("/deleteConfigDependency")
    @RequestValidate(NotEmptyFields = {Dict.GITLABID,Dict.ID})
    public JsonResult deleteConfigDependency(@RequestBody Map<String, Object> requestParam) throws Exception {
    	entityService.deleteConfigDependency(requestParam);
        return JsonResultUtil.buildSuccess();
    }
}
