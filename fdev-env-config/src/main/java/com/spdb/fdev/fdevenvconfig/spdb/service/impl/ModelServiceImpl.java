package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.CommonValidate;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.cache.IConfigFileCache;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ICommonDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelEnvDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelTemplateDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelCategory;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelTemplate;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Scope;
import com.spdb.fdev.fdevenvconfig.spdb.notify.INotifyEventStrategy;
import com.spdb.fdev.fdevenvconfig.spdb.service.IModelService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IVerifyCodeService;
import com.spdb.fdev.fdevenvconfig.spdb.service.JsonSchemaService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.swing.text.html.parser.Entity;
import java.util.*;

/**
 * @author xxx
 * @date 2019/7/5 13:20
 */
@Service
@RefreshScope
public class ModelServiceImpl implements IModelService {

    @Value("${fenvconfig.sendMail}")
    private boolean sendMail;
    @Value("${update.model.sendMail}")
    private boolean subSendMail;
    @Value("${delete.model.sendMail}")
    private boolean deleteModelSendMail;
    @Autowired
    private IModelDao modelDao;
    @Autowired
    private IModelEnvDao modelEnvDao;
    @Autowired
    private ICommonDao commonDao;
    @Resource(name = "UpdateModel")
    private INotifyEventStrategy notifyEventStrategy;
    @Autowired
    private ServiceUtil serviceUtil;
    @Autowired
    private JsonSchemaService jsonSchemaService;
    @Autowired
    private IVerifyCodeService verifyCodeService;
    @Autowired
    private IConfigFileCache configFileCache;
    @Autowired
    private IModelTemplateDao modelTemplateDao;

    private Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);

    @Override
    public List<Model> query(Model model) throws Exception {
        model.setStatus(Constants.STATUS_OPEN);
        List<Model> modelList = this.modelDao.query(model);
        //List<Map> modelMapList = joinTemplateName(modelList);
        return modelList;
    }

    @Override
    public Model add(Model model) throws Exception {
        //一级分类为“ci”,需要校验platform
        if ("ci".equals(model.getFirst_category())) {
            if (CommonUtils.isNullOrEmpty(model.getPlatform())) {
                String errorMsg = "新增ci实体时,适用平台不能为空";
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"platform", errorMsg});
            }
        }
        //判断type、first_category、second_category、suffix_name
        String nameEn = CommonValidate.validateRepeatParamPattern(model, new String[]{Constants.FIRST_CATEGORY, Constants.SECOND_CATEGORY, Constants.SUFFIX_NAME, Constants.TYPE});
        if (!nameEn.equals(model.getName_en())) {
            String errorMsg = "传入的实体英文名与拼接出来的不对等";
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
        }
        CommonValidate.validateRepeatParam(model, Constants.OR, new String[]{Constants.NAME_EN, Constants.NAME_CN}, Model.class, this.commonDao);
        CommonValidate.validateRepeatParam(model.getEnv_key(), Constants.NAME_EN);
        CommonValidate.validateRepeatParam(model.getEnv_key(), Constants.NAME_CN);
        //如果使用了实体模板，则校验属性是否对应
        if (!CommonUtils.isNullOrEmpty(model.getModel_template_id())) {
            if (!validateModelAndTemplate(model))
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"实体属性和模板不对等"});
        }
        model.setOpno(serviceUtil.getOpno());
        List<Object> envKey = model.getEnv_key();
        // 处理实体属性getModelAttribute
        model.setEnv_key(getModelAttribute(envKey, nameEn));
        return this.modelDao.add(model);
    }

    /**
     * 校验实体和实体模板使用正确
     * 属性一一对应，
     * 属性类型为Stirng
     * 是否必填为必填
     *
     * @param model
     * @return
     */
    public boolean validateModelAndTemplate(Model model) {
        boolean flag = true;
        ModelTemplate template = modelTemplateDao.queryById(model.getModel_template_id());
        List<Object> modelEnvKeyList = (List<Object>) model.getEnv_key();
        List<Object> templateEnvKeyList = (List<Object>) template.getEnvKey();
        for (Object modelPropObj : modelEnvKeyList) {
            Map modelProp = (Map) modelPropObj;
            String propKey = (String) modelProp.get(Dict.PROP_KEY);
            if (propKey == null)
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"prop_key字段缺失"});
            int match = 0;             //是否能找到匹配项
            for (Object templatePropObj : templateEnvKeyList) {
                Map templateProp = (Map) templatePropObj;
                if (propKey.equals((String) templateProp.get(Dict.PROP_KEY))) {
                    match = 1;  //找到匹配项
                    templateEnvKeyList.remove(templatePropObj);   //删除该项
                    //判断类型是否为String
                    if (!(CommonUtils.isNullOrEmpty(modelProp.get(Dict.DATA_TYPE)) || "String".equals(modelProp.get(Dict.DATA_TYPE))))
                        return false;
                    //判断是否为必填
                    if (!"1".equals(modelProp.get(Dict.REQUIRE)))
                        return false;
                    break;
                }
            }
            if (match == 0)
                return false;
        }
        return flag;
    }

    @Override
    public Model update(Model model) throws Exception {
        //一级分类为“ci”,需要校验platform
        if ("ci".equals(model.getFirst_category())) {
            if (CommonUtils.isNullOrEmpty(model.getPlatform())) {
                String errorMsg = "更新ci实体时,适用平台不能为空";
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"platform", errorMsg});
            }
        }
        String nameEn = model.getName_en();
        model.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        // 获取修改前的实体
       // Model beforeModel = this.modelDao.queryById(model);
        // 检查是否删除了在用实体属性
        //checkUseModelField(beforeModel, model);
      //  model.setVersion(CommonUtils.CompareGetVersion(model.getEnv_key(), beforeModel.getEnv_key(), beforeModel.getVersion()));
        List<Object> envKey = model.getEnv_key();
        // 处理实体属性
        model.setEnv_key(getModelAttribute(envKey, nameEn));
        // 获取修改后的实体
        Model updateModel = this.modelDao.update(model);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("@@@@@@ 修改实体 成功{}", objectMapper.writeValueAsString(updateModel));
        return updateModel;
    }

    @Override
    public void delete(Map map) throws Exception {
        String verfityCode = (String) map.get(Dict.VERFITYCODE);
        // 校验验证码
        verifyCodeService.checkVerifyCode(verfityCode);
        // 获取删除前的实体
        Model model = new Model();
        model.setId((String) map.get(Dict.ID));
        Model beforeModel = this.modelDao.queryById(model);
        // 检查该实体是否正在使用中
        checkUseModel(beforeModel.getName_en(), "");
        // 删除实体
        String opno = serviceUtil.getOpno();
        model.setOpno(opno);
        model.setStatus(Constants.STATUS_LOSE);
        model.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        this.modelDao.delete(model);
        // 异步发送邮件
        if (sendMail && deleteModelSendMail) {
            HashMap<String, Object> parse = new HashMap<>();
            parse.put(Dict.BEFORE, beforeModel);
            parse.put(Dict.AFTER, null);
            parse.put(Dict.USER_ID, opno);
            this.notifyEventStrategy.doNotify(parse);
        }
        logger.info("@@@@@@ 删除实体 成功");
        this.modelEnvDao.deleteModelEnv(Constants.MODEL_ID, model.getId(), opno);
        logger.info("@@@@@@ 实体映射环境删除 成功");
    }

    @Override
    public Model queryById(Model model) throws Exception {
        model.setStatus(Constants.STATUS_OPEN);
        return this.modelDao.queryById(model);
    }

    @Override
    public Model queryById(String id) {
        return modelDao.queryById(id);
    }

    @Override
    public Model getByNameEn(String nameEn) {
        return modelDao.getByNameEn(nameEn);
    }

    @Override
    public List<Model> queryByIdList(Set<String> modelIdList, String var, String value) {
        if (CommonUtils.isNullOrEmpty(value)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{var, value});
        }
        return this.modelDao.queryByIdList(modelIdList, var, value);
    }

    @Override
    public List<Model> queryFuzz(Map map) throws Exception {
        Map paramMap = new HashMap();
        // 搜索条目
        String term = (String) map.get(Constants.TERM);
        // 实体类型
        String type = (String) map.get(Constants.TYPE);
        // 实体一级分类
        String firstCategory = (String) map.get(Constants.FIRST_CATEGORY);
        // 实体二级分类
        String secondCategory = (String) map.get(Constants.SECOND_CATEGORY);
        term = StringUtils.isBlank(term) ? "" : term;
        paramMap.put(Constants.TYPE, type);
        paramMap.put(Constants.FIRST_CATEGORY, firstCategory);
        paramMap.put(Constants.SECOND_CATEGORY, secondCategory);
        paramMap.put(Constants.NAME_EN, term);
        paramMap.put(Constants.NAME_CN, term);
        return modelDao.queryFuzz(paramMap, Constants.STATUS_OPEN, Model.class);
    }

    @Override
    public Map<String, Object> queryModelCategory() {
        // 查询实体类型常量
        ModelCategory modelCategory = modelDao.queryModelCateCategory();
        Scope scope = modelDao.queryScope();
        Map<String, Object> map = new HashMap();
        map.put(Constants.ID, modelCategory.getId());
        map.put(Constants.CATEGORY, modelCategory.getCategory());
        map.put(Constants.SCOPE, scope.getScope());
        map.put(Constants.CTIME, modelCategory.getCtime());
        map.put(Constants.UTIME, modelCategory.getUtime());
        map.put(Constants.OPNO, modelCategory.getOpno());
        return map;
    }

    @Override
    public Map queryModelVariables(Map requestMap) {
        String modelNameVariable = requestMap.get(Constants.NAME_EN).toString();
        String[] var = modelNameVariable.split("\\.");
        if (var.length <= 1) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        Model model = this.modelDao.queryVaribleForOne(var[0], var[1]);
        if (CommonUtils.isNullOrEmpty(model.getEnv_key())) {
            logger.warn(Constants.NAME_EN + "不存在");
            return null;
        }
        return (Map) model.getEnv_key().get(0);
    }

    @Override
    public List<Model> queryExcludePirvateModel(Model model) throws Exception {
        model.setStatus(Constants.STATUS_OPEN);
        String name_en = model.getName_en();
        model.setName_en("");
        List<Model> queryModel = this.modelDao.query(model);
        List<Model> newModel = new ArrayList<>();
        for (Model model1 : queryModel) {
            String name_en1 = model1.getName_en();
            if (!(name_en1.startsWith(Dict.PRIVATE) && !name_en1.endsWith(name_en))) {
                newModel.add(model1);
            }
        }
        return newModel;
    }

    /**
     * / 处理实体属性，判断是否有高级属性，有的话需要将高级属性存储在json_schema表中
     *
     * @param envKey
     * @param nameEn
     */
    private List<Object> getModelAttribute(List<Object> envKey, String nameEn) throws Exception {
        List<Object> modelAttributeList = new ArrayList<>();
        for (Object envKeyObj : envKey) {
            Map<String, Object> map = (Map<String, Object>) envKeyObj;
            // 设置属性id
            String id = (String) map.get(Constants.ID);
            if (CommonUtils.isNullOrEmpty(id)) {
                map.put(Constants.ID, new ObjectId().toString());
            }
            // 验证data_type
            String dataType = (String) map.get(Dict.DATA_TYPE);
            if (Dict.OBJECT.equals(dataType) || Dict.ARRAY.equals(dataType)) {
                String jsonSchema = (String) map.get(Dict.JSON_SCHEMA);
                String jsonSchemaId = (String) map.get(Dict.JSON_SCHEMA_ID);
                if (StringUtils.isEmpty(jsonSchema) && StringUtils.isEmpty(jsonSchemaId)) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.JSON_SCHEMA + "或" + Dict.JSON_SCHEMA_ID, Constants.PARAM_CAN_NOT_NULL});
                }
                // 若json_schema_id为空，则需要保存json_schema
                if (StringUtils.isEmpty(jsonSchemaId)) {
                    // 保存json_schema，并在实体表存json_schema对应的id
                    map.put(Dict.JSON_SCHEMA_ID, jsonSchemaService.saveJsonSchema(nameEn, map));
                } /*else {
                    jsonSchemaService.updateJsonSchema(map, serviceUtil.getOpno());
                }*/
            }
            // 过滤前端传来的无用数据
            Map<String, Object> modelAttribute = new HashMap<>();
            modelAttribute.put(Dict.ID, map.get(Dict.ID));
            modelAttribute.put(Dict.NAME_EN, map.get(Dict.NAME_EN));
            modelAttribute.put(Dict.NAME_CN, map.get(Dict.NAME_CN));
            modelAttribute.put(Dict.DESC, map.get(Dict.DESC));
            modelAttribute.put(Dict.REQUIRE, map.get(Dict.REQUIRE));
            modelAttribute.put(Dict.TYPE, map.get(Dict.TYPE));
            modelAttribute.put(Dict.DATA_TYPE, map.get(Dict.DATA_TYPE));
            if(map.get(Dict.PROP_KEY)!=null){
                modelAttribute.put(Dict.PROP_KEY, map.get(Dict.PROP_KEY));
            }
            if (Dict.OBJECT.equals(dataType) || Dict.ARRAY.equals(dataType)) {
                modelAttribute.put(Dict.JSON_SCHEMA_ID, map.get(Dict.JSON_SCHEMA_ID));
            }
            modelAttributeList.add(modelAttribute);
        }
        return modelAttributeList;
    }

    /**
     * 校验实体或实体属性是否正在使用中
     *
     * @param modelNameEn
     * @param fieldNameEn
     */
    @Override
    public void checkUseModel(String modelNameEn, String fieldNameEn) {
        Map<String, Object> requestParam = new HashMap<>();
        requestParam.put(Constants.MODEL_NAME_EN, modelNameEn);
        requestParam.put(Constants.FIELD_NAME_EN, fieldNameEn);
        List<String> range = new ArrayList<>();
        range.add(Dict.MASTER);
        range.add(Dict.SIT);
        range.add(Dict.RELEASE);
        requestParam.put(Constants.RANGE, range);
        List<Map> configDependencyList;
        try {
            configDependencyList = configFileCache.preQueryConfigDependency(requestParam);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"获取实体依赖信息失败"});
        }
        if (CollectionUtils.isNotEmpty(configDependencyList)) {
            if (StringUtils.isEmpty(fieldNameEn)) {
                throw new FdevException(ErrorConstants.DELETE_MODEL_ERROR, new String[]{modelNameEn, "该实体正在使用中，不能直接删除"});
            }
            throw new FdevException(ErrorConstants.UPDATE_MODEL_ERROR, new String[]{modelNameEn, "该实体的" + fieldNameEn + "属性正在使用中，不能直接删除"});
        }
    }

    @Override
    public Map pageQuery(Map map) {
        return this.modelDao.pageQuery(map);
    }

    @Override
    public List<Model> getModels(String type) {
        return modelDao.getModels(type);
    }

    @Override
    public List<Model> queryByNameEnSet(Set<String> modelNameEnSet, String status) {
        return modelDao.queryByNameEnSet(modelNameEnSet, status);
    }

    /**
     * 查询全量的实体属性
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> queryNoCiEnvKeyList() throws Exception {
        List<Model> modelList = this.configModel();
        List<Map> result = new ArrayList<>();
        for (Model model : modelList) {
            List<Map<String, Object>> envKeyList = objectListToMapList(model.getEnv_key());
            for (Map envKey : envKeyList) {
                Map map = new HashMap();
                map.put(Dict.NAMEEN, envKey.get(Dict.NAME_EN));
                map.put(Dict.NAMECN, envKey.get(Dict.NAME_CN));
                map.put(Dict.MODELID, model.getId());
                map.put(Dict.MODELNAMEEN, model.getName_en());
                map.put(Dict.MODELNAMECN, model.getName_cn());
                map.put(Dict.NAMEALL, model.getName_en() + "." + envKey.get(Dict.NAME_EN));
                result.add(map);
            }
        }
        return result;
    }

    /**
     * 校验实体属性是否正在使用中
     *
     * @param before
     * @param after
     */
    private void checkUseModelField(Model before, Model after) {
        List<Object> beforeEnvKeys = before.getEnv_key();
        List<Object> afterEnvKeys = after.getEnv_key();
        List<Map<String, Object>> beforeEnvKeyList = objectListToMapList(beforeEnvKeys);
        List<Map<String, Object>> afterEnvKeyList = objectListToMapList(afterEnvKeys);
        // 获取删除实体的属性和新增的实体属性
        Map<String, List<Map<String, Object>>> updateModel = getUpdateModel(beforeEnvKeyList, afterEnvKeyList);
        List<Map<String, Object>> removedKeyList = updateModel.get(Dict.DELETE);
        for (Map<String, Object> map : removedKeyList) {
            checkUseModel(before.getName_en(), (String) map.get(Dict.NAME_EN));
        }
    }

    private List<Map<String, Object>> objectListToMapList(List<Object> objectList) {
        List<Map<String, Object>> removedKeyMapList = new ArrayList<>();
        for (Object removedKey : objectList) {
            Map<String, Object> removedKeyMap = (Map<String, Object>) removedKey;
            removedKeyMapList.add(removedKeyMap);
        }
        return removedKeyMapList;
    }

    /**
     * 更新实体时，获取删除实体的属性和新增的实体属性
     *
     * @param beforeEnvKeyList
     * @param afterEnvKeyList
     * @return
     */
    public Map<String, List<Map<String, Object>>> getUpdateModel(List<Map<String, Object>> beforeEnvKeyList, List<Map<String, Object>> afterEnvKeyList) {
        Map<String, List<Map<String, Object>>> updateModelMap = new HashMap<>();
        // 删除两个list中实体属性id相同的，则beforeEnvKeyList剩下来的便是删除的，afterEnvKeyList剩下来的便是新增的
        Iterator<Map<String, Object>> beforeIterator = beforeEnvKeyList.iterator();
        while (beforeIterator.hasNext()) {
            Map<String, Object> beforeEnvKeyMap = beforeIterator.next();
            Iterator<Map<String, Object>> afterIterator = afterEnvKeyList.iterator();
            while (afterIterator.hasNext()) {
                Map<String, Object> afterEnvKeyMap = afterIterator.next();
                if (beforeEnvKeyMap.get(Dict.ID).equals(afterEnvKeyMap.get(Dict.ID))) {
                    beforeIterator.remove();
                    afterIterator.remove();
                }
            }
        }
        updateModelMap.put(Dict.DELETE, beforeEnvKeyList);
        updateModelMap.put(Dict.ADD, afterEnvKeyList);
        return updateModelMap;
    }

    /**
     * 组装实体模板中英文名
     *
     * @param modelList
     * @return
     */
    public List<Map> joinTemplateName(List<Model> modelList) {
        return modelDao.joinTemplateName(modelList);
    }

    /**
     * 查询非CI实体列表
     *
     * @return
     * @throws Exception
     */
    public List<Model> configModel() throws Exception {
        Model queryModel = new Model();
        queryModel.setStatus(Constants.STATUS_OPEN); //查询所有状态为1的实体
        List<Model> modelList = modelDao.query(queryModel);
        for (int i = 0; i < modelList.size(); ) {
            Model model = modelList.get(i);
            if (model.getFirst_category().equals(Dict.CI)) {
                modelList.remove(model);
                continue;
            }
            i++;
        }
        return modelList;
    }
}
