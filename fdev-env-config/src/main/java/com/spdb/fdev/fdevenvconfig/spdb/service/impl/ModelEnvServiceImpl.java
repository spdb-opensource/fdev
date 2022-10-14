package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.Lists;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.CommonValidate;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.DES3Util;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.*;
import com.spdb.fdev.fdevenvconfig.spdb.entity.*;
import com.spdb.fdev.fdevenvconfig.spdb.notify.INotifyEventStrategy;
import com.spdb.fdev.fdevenvconfig.spdb.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date 2019/7/5 13:17
 */
@Service
@RefreshScope
public class ModelEnvServiceImpl implements IModelEnvService {

    @Value("${fenvconfig.sendMail}")
    private boolean sendMail;
    @Value("${update.model.env.sendMail}")
    private boolean subSendMail;
    @Value("${delete.model.env.sendMail}")
    private boolean deleteModeEnvSendMail;
    @Autowired
    private IModelEnvDao modelEnvDao;
    @Autowired
    private ICommonDao commonDao;
    @Autowired
    private IModelService modelService;
    @Autowired
    private IEnvironmentService environmentService;
    @Autowired
    private IAppDeployMappingService appDeployMappingService;
    private Logger logger = LoggerFactory.getLogger(ModelEnvServiceImpl.class);
    @Resource(name = "UpdateModelFieldMapping")
    private INotifyEventStrategy notifyEventStrategy;
    @Autowired
    private IModelDao modelDao;
    @Autowired
    private ServiceUtil serviceUtil;
    @Autowired
    private IVerifyCodeService verifyCodeService;
    @Autowired
    private JsonSchemaService jsonSchemaService;
    @Autowired
    private DES3Util des3Util;
    @Autowired
    private AppOwnFieldService appOwnFieldService;

    @Autowired
    private IRequestService requestService;
    @Autowired
    private AppEnvMappingDao appEnvMappingDao;
    @Autowired
    private IEnvironmentDao iEnvironmentDao;
    @Value("${env.sort}")
    private List<String> envSort;

    @Override
    public List<Map> query(ModelEnv modelEnv) throws Exception {
        List<ModelEnv> queryModelEnv = this.modelEnvDao.query(modelEnv);
        List<Map> list = new ArrayList<>();
        for (ModelEnv me : queryModelEnv) {
            Map<String, Object> map = CommonUtils.object2Map(me);
            Environment env = new Environment();
            env.setId(me.getEnv_id());
            env = this.environmentService.queryById(env);
            if (env != null) {
                map.put(Constants.ENV, env.getName_cn());
                map.put(Constants.ENV_NAME_EN, env.getName_en());
                map.put(Constants.ENV_DESC, env.getDesc());
            }
            Model model = new Model();
            model.setId(me.getModel_id());
            model = this.modelService.queryById(model);
            if (model != null) {
                map.put(Constants.MODEL, model.getName_cn());
                map.put(Constants.MODEL_NAME_EN, model.getName_en());
                map.put(Constants.MODEL_VERSION, model.getVersion());
                map.put(Constants.MODEL_DESC, model.getDesc());
                map.put(Constants.SCOPE, model.getScope());
                map.put(Constants.FIRST_CATEGORY, model.getFirst_category());
                List<Object> envKey = model.getEnv_key();
                Map<String, String> ma = (Map<String, String>) map.get(Constants.VARIABLES);
                for (Object obj : envKey) {
                    Map<String, Object> oMap = (Map<String, Object>) obj;
                    String keyId = (String) oMap.get(Constants.ID);
                    Object value = ma.get(keyId);
                    oMap.put(Constants.VALUE, value);
                }
                map.put(Constants.VARIABLES, envKey);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 新增实体环境映射，增加高级属性录入
     *
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public ModelEnv add(Map map) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        CommonValidate.validateRepeatParam(map, Constants.AND, new String[]{Constants.ENV_ID, Constants.MODEL_ID}, ModelEnv.class,
                commonDao);
        map.put(Constants.OPNO, serviceUtil.getOpno());
        map.put(Constants.CTIME, DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        map.put(Constants.VERSION, Constants.UP_VERSION);
        List<Map<String, Object>> variables = (List<Map<String, Object>>) map.get(Dict.VARIABLES);
        Map<String, Object> variableMap = new HashMap<>();
        // 验证必填属性
        Model paramModel = new Model();
        paramModel.setId(String.valueOf(map.get(Constants.MODEL_ID)));
        Model model = modelService.queryById(paramModel);
        checkRequire(model.getName_en(), variables);
        // 加密
        if (Constants.DCE.equals(model.getSecond_category())) {
            encryptionPwd(variables);
        }
        for (Map<String, Object> oMap : variables) {
            variableMap.put((String) oMap.get(Constants.ID), oMap.get(Constants.VALUE));
        }
        map.put(Constants.VARIABLES, variableMap);
        ModelEnv modelEnv = CommonUtils.map2Object(map, ModelEnv.class);
        modelEnv.setStatus(Constants.STATUS_OPEN);
        ModelEnv addModelEnv = modelEnvDao.add(modelEnv);
        logger.info("@@@@@@ 新增实体与环境映射 成功{}", objectMapper.writeValueAsString(addModelEnv));
        return addModelEnv;
    }

    @Override
    public ModelEnv update(Map<String, Object> map) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        map.put(Constants.OPNO, serviceUtil.getOpno());
        Map<String, Object> variablesMap = new HashMap<>();
        List<Map<String, Object>> variables = (List<Map<String, Object>>) map.get(Dict.VARIABLES);
        // 验证必填属性
        checkRequire(String.valueOf(map.get(Dict.MODEL_NAME_EN)), variables);
        for (Map<String, Object> oMap : variables) {
            variablesMap.put((String) oMap.get(Constants.ID), oMap.get(Constants.VALUE));
        }
        map.put(Constants.VARIABLES, variablesMap);
        ModelEnv modelEnv = CommonUtils.map2Object(map, ModelEnv.class);
        modelEnv.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        modelEnv.setStatus(Constants.ONE);
        CommonValidate.validateRepeatParamForUpdate(modelEnv, Constants.AND, new String[]{Constants.ENV_ID, Constants.MODEL_ID, Constants.STATUS}, ModelEnv.class, commonDao);
        // 获取修改前的实体映射值
        ModelEnv beforeModelEnv = this.modelEnvDao.queryId(modelEnv);
        Map<String, Object> beforeVariables = beforeModelEnv.getVariables();
        //判断实体映射是否更改
        modelEnv.setVersion(CommonUtils.CompareGetVersion(modelEnv.getVariables(), beforeVariables, beforeModelEnv.getVersion()));
        // 对 密码进行加密
        encryptionPwd(beforeVariables, variables, variablesMap);
        modelEnv.setVariables(variablesMap);
        // 获取修改后的实体映射值
        ModelEnv updateModelEnv = this.modelEnvDao.update(modelEnv);
        // 异步发送邮件
        if (!CommonUtils.CompareMapOrList(modelEnv.getVariables(), beforeVariables, Constants.ZERO) && sendMail && subSendMail) {
            HashMap<String, Object> parse = new HashMap<>();
            parse.put(Dict.BEFORE, beforeModelEnv);
            parse.put(Dict.AFTER, updateModelEnv);
            parse.put(Dict.ENV_NAME_EN, map.get(Dict.ENV_NAME_EN));
            parse.put(Dict.MODEL_NAME_EN, map.get(Dict.MODEL_NAME_EN));
            parse.put(Dict.MODEL, map.get(Dict.MODEL));
            parse.put(Dict.ENV, map.get(Dict.ENV));
            parse.put(Dict.USER_ID, map.get(Constants.OPNO));
            parse.put(Dict.APPLY_ID, map.get(Dict.APPLY_ID));
            parse.put(Dict.APPLY_USERNAME, map.get(Dict.APPLY_USERNAME));
            parse.put(Dict.MODIFY_REASON, map.get(Dict.MODIFY_REASON));
            this.notifyEventStrategy.doNotify(parse);
        }
        logger.info("@@@@@@ 修改环境实体 成功{}", objectMapper.writeValueAsString(updateModelEnv));
        return updateModelEnv;
    }

    @Override
    public void delete(Map map) throws Exception {
        String verfityCode = (String) map.get(Dict.VERFITYCODE);
        // 校验验证码
        verifyCodeService.checkVerifyCode(verfityCode);
        ModelEnv modelEnv = new ModelEnv();
        modelEnv.setId((String) map.get(Dict.ID));
        modelEnv.setOpno(serviceUtil.getOpno());
        // 获取删除前的实体映射值
        ModelEnv beforeModelEnv = this.modelEnvDao.queryId(modelEnv);
        // 检查该实体是否正在使用中
        Model paramModel = new Model();
        paramModel.setId(beforeModelEnv.getModel_id());
        Model model = modelService.queryById(paramModel);
        modelService.checkUseModel(model.getName_en(), "");
        // 删除实体与环境映射
        this.modelEnvDao.delete(modelEnv);
        // 异步发送邮件
        if (sendMail && deleteModeEnvSendMail) {
            HashMap<String, Object> parse = new HashMap<>();
            parse.put(Dict.BEFORE, beforeModelEnv);
            parse.put(Dict.AFTER, null);
            parse.put(Dict.USER_ID, serviceUtil.getOpno());
            this.notifyEventStrategy.doNotify(parse);
        }
        logger.info("@@@@@@ 删除环境实体 成功");
    }

    @Override
    public Map<String, Object> queryEnvBySlug(Map<String, String> map) throws Exception {
        Environment envEntity = new Environment();
        envEntity.setName_en(map.get(Constants.ENV));
        List<Environment> envEntityList = this.environmentService.query(envEntity);
        envEntity = envEntityList.get(0);
        ModelEnv modelEnv = new ModelEnv();
        String envId = envEntity.getId();
        modelEnv.setEnv_id(envId);
        List<ModelEnv> modelEnvList = modelEnvDao.query(modelEnv);
        HashSet<String> modelIdList = new HashSet<>();
        for (ModelEnv me : modelEnvList) {
            modelIdList.add(me.getModel_id());
        }
        List<Model> modelList = modelService.queryByIdList(modelIdList, Constants.SCOPE, map.get(Constants.TYPE));
        return returnModelKeyMap(envId, modelList);
    }

    @Override
    public List<Map<String, Object>> queryEnvwithNameCnBySlug(String appId, List<Model> modelList, String envName) throws Exception {
        Environment envEntity = new Environment();
        envEntity.setName_en(envName);
        List<Environment> envEntityList = this.environmentService.query(envEntity);
        if (CollectionUtils.isEmpty(envEntityList)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前环境不存在: " + envName});
        }
        envEntity = envEntityList.get(0);
        String envId = envEntity.getId();
        HashSet<String> modelIds = new HashSet<>();
        modelList.forEach(model -> modelIds.add(model.getId()));
        // 查询实体环境映射值
        List<ModelEnv> modelEnvList = modelEnvDao.queryModelEnvByModelsAndEnvId(modelIds, envId);
        // 查询应用独有的值
        AppOwnField appOwnFieldParam = new AppOwnField();
        appOwnFieldParam.setApp_Id(appId);
        appOwnFieldParam.setEnv_id(envId);
        AppOwnField appOwnField = appOwnFieldService.queryByAppIdAndEnvId(appOwnFieldParam);
        Map<String, Object> appModelFieldValueMap = new HashMap<>();
        if (appOwnField != null) {
            List<Map> appModelFieldValueList = appOwnField.getModelFleld_value();
            for (Map map : appModelFieldValueList) {
                appModelFieldValueMap.put((String) map.get(Dict.MODEL_FIELD), map.get(Dict.VALUE));
            }
        }
        List<Map<String, Object>> resultMap = new ArrayList<>();
        for (Model model : modelList) {
            String modelName = model.getName_en();
            String modelId = model.getId();
            List<Object> envKey = model.getEnv_key();
            Map<String, Object> variables = new HashMap();
            for (ModelEnv modelEnv : modelEnvList) {
                if (modelId.equals(modelEnv.getModel_id())) {
                    variables = modelEnv.getVariables();
                }
            }
            for (Object o : envKey) {
                Map<String, String> envKeyMap = (Map<String, String>) o;
                Map<String, Object> var = new HashMap<>();
                String keyId = envKeyMap.get(Constants.ID);
                String modelFieldName = modelName + "." + envKeyMap.get(Constants.NAME_EN);
                var.put(Constants.NAME_EN, modelFieldName);
                var.put(Constants.NAME_CN, envKeyMap.get(Constants.NAME_CN));
                var.put(Constants.VALUE, variables.get(keyId));
                if (MapUtils.isNotEmpty(appModelFieldValueMap) && appModelFieldValueMap.containsKey(modelFieldName)) {
                    var.put(Constants.VALUE, appModelFieldValueMap.get(modelFieldName));
                }
                var.put(Constants.REQUIRE, envKeyMap.get(Constants.REQUIRE));
                var.put(Dict.DATA_TYPE, envKeyMap.get(Dict.DATA_TYPE));
                var.put(Dict.JSON_SCHEMA, envKeyMap.get(Dict.JSON_SCHEMA));
                resultMap.add(var);
            }
        }
        return resultMap;
    }

    @Override
    public Map queryModelEnvByEnvNameEn(Map<String, String> map) throws Exception {
        map.put(Constants.STATUS, Constants.STATUS_OPEN);
        Environment environment = CommonUtils.map2Object(map, Environment.class);
        List<Environment> environments = this.environmentService.query(environment);
        Map param = new HashMap();
        List varList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(environments)) {
            String id = environments.get(0).getId();
            ModelEnv modelEnv = new ModelEnv();
            modelEnv.setEnv_id(id);
            List<Map> mapList = this.query(modelEnv);
            if (!CommonUtils.isNullOrEmpty(mapList)) {
                for (Map m : mapList) {
                    if (Constants.CI.equals(m.get(Constants.FIRST_CATEGORY))) {
                        param.put(Constants.NAME, m.get(Constants.ENV_NAME_EN));
                        List<Map> list = (List<Map>) m.get(Constants.VARIABLES);
                        for (Map a : list) {
                            Map variables = new HashMap();
                            variables.put(Constants.NAME_ZH, a.get(Constants.NAME_CN));
                            variables.put(Constants.VALUE, a.get(Constants.VALUE));
                            variables.put(Constants.KEY, a.get(Constants.NAME_EN));
                            varList.add(variables);
                        }
                    }
                }
                param.put(Constants.VAR_LIST, varList);
            }
            return param;
        }
        return null;
    }

    @Override
    public List<Map> queryModelEnvByEnvIdOrName(Map<String, String> map) throws Exception {
        ModelEnv modelEnv = new ModelEnv();
        List<Map> modelEnvLists = new ArrayList<>();
        List<Map> mapList;
        if (map != null && map.size() != 0) {
            String id = "";
            if (map.containsKey(Constants.NAME_EN)) {
                map.put(Constants.STATUS, Constants.STATUS_OPEN);
                Environment environment = CommonUtils.map2Object(map, Environment.class);
                List<Environment> environments = this.environmentService.query(environment);
                if (!CommonUtils.isNullOrEmpty(environments)) {
                    id = environments.get(0).getId();
                }
            } else {
                id = map.get(Constants.ID);
            }
            modelEnv.setEnv_id(id);
            mapList = this.query(modelEnv);
            Map param = this.valDataModelEnv(mapList, null);
            modelEnvLists.add(param);
        } else {
            Environment environment = new Environment();
            environment.setStatus(Constants.STATUS_OPEN);
            List<Environment> environments = this.environmentService.query(environment);
            for (Environment env : environments) {
                modelEnv.setEnv_id(env.getId());
                mapList = this.query(modelEnv);
                Map param = this.valDataModelEnv(mapList, env);
                modelEnvLists.add(param);
            }
        }
        return modelEnvLists;
    }

    public Map<String, Object> returnModelKeyMap(String envId, List<Model> modelList) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        for (Model model : modelList) {
            String modelName = model.getName_en();
            String modelId = model.getId();
            List<Object> envKey = model.getEnv_key();
            ModelEnv me = new ModelEnv();
            me.setEnv_id(envId);
            me.setModel_id(modelId);
            List<ModelEnv> query = modelEnvDao.query(me);
            if (query == null || query.isEmpty()) {
                continue;
            }
            me = query.get(0);
            Map<String, Object> variables = me.getVariables();
            for (Object o : envKey) {
                Map<String, String> envKeyMap = (Map<String, String>) o;
                String keyId = envKeyMap.get(Constants.ID);
                String nameEn = envKeyMap.get(Constants.NAME_EN);
                Object keyValue = variables.get(keyId);
                resultMap.put(modelName + "." + nameEn, keyValue);
            }
        }
        return resultMap;
    }

    private Map valDataModelEnv(List<Map> mapList, Environment environment) {
        Map param = new HashMap();
        Map variables = new HashMap();
        if (!CommonUtils.isNullOrEmpty(mapList)) {
            for (Map m : mapList) {
                param.put(Constants.ID, m.get(Constants.ENV_ID));
                param.put(Constants.ENV_NAME, m.get(Constants.ENV_NAME_EN));
                if (Constants.CI.equals(m.get(Constants.FIRST_CATEGORY))) {
                    List<Map> list = (List<Map>) m.get(Constants.VARIABLES);
                    list.forEach(a -> variables.put(a.get(Constants.NAME_EN), a.get(Constants.VALUE)));
                }
            }
            param.put(Constants.ENV_VARIABLES, variables);
        } else {
            if (environment != null) {
                param.put(Constants.ID, environment.getId());
                param.put(Constants.ENV_NAME, environment.getName_en());
                param.put(Constants.ENV_VARIABLES, variables);
            }
        }
        return param;
    }


    /**
     * 查询应用deploy时需要的变量映射关系,带小key的中文名
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryEnvMappingwithCn(Map requestMap) throws Exception {
        Map<String, Map> variablesNameCnMap = new HashMap<>();
        Map<String, String> variablesValueMap = this.appDeployMappingService.doQueryVariablesMapping(requestMap, variablesNameCnMap);
        Map<String, Object> result = new HashMap<>();
        List<Map> varList = new ArrayList<>();
        result.put(Constants.NAME, requestMap.get(Constants.ENV));

        for (Map.Entry<String, Map> entry : variablesNameCnMap.entrySet()) {
            Map<String, String> variable = new HashMap<>();
            variable.put(Constants.KEY, entry.getKey());
            variable.put(Constants.NAME_ZH, entry.getValue().get(Constants.NAME_CN).toString());
            variable.put(Constants.VALUE, variablesValueMap.get(entry.getKey()));
            varList.add(variable);
        }
        result.put(Constants.VAR_LIST, varList);
        return result;
    }

    @Override
    public List<Map> queryVarByEnvAndType(Map requestMap) throws Exception {
        Map<String, Map> variablesNameCnMap = new HashMap<>();
        Map<String, String> variablesValueMap = this.appDeployMappingService.doQueryVariablesMapping(requestMap, variablesNameCnMap);

        List<Map> result = new ArrayList<>();

        for (Map.Entry<String, Map> entry : variablesNameCnMap.entrySet()) {
            Map<String, String> variable = new HashMap<>();
            variable.put(Constants.KEY, entry.getKey());
            variable.put(Constants.NAME_ZH, entry.getValue().get(Constants.NAME_CN).toString());
            variable.put(Constants.REQUIRE, entry.getValue().get(Constants.REQUIRE).toString());
            Object dataTypeObj = entry.getValue().get(Dict.DATA_TYPE);
            variable.put(Dict.DATA_TYPE, (String) dataTypeObj);
            if (Dict.OBJECT.equals(dataTypeObj) || Dict.ARRAY.equals(dataTypeObj)) {
                variable.put(Dict.JSON_SCHEMA, "");
                Object jsonSchemaObj = entry.getValue().get(Dict.JSON_SCHEMA);
                if (jsonSchemaObj != null) {
                    variable.put(Dict.JSON_SCHEMA, jsonSchemaObj.toString());
                }
            }
            variable.put(Constants.VALUE, variablesValueMap.get(entry.getKey()));
            result.add(variable);
        }
        return result;
    }


    @Override
    public ModelEnv queryByEnvIdAndModelId(ModelEnv modelEnv) throws Exception {
        List<ModelEnv> modelEnvs = modelEnvDao.query(modelEnv);
        if (CommonUtils.isNullOrEmpty(modelEnv) || modelEnvs.isEmpty()) {
            return null;
        }
        return modelEnvs.get(0);
    }

    @Override
    public Map queryVarByLabelAndType(Map<String, String> requestMap) throws Exception {
        String label = requestMap.get(Dict.LABEL);
        Integer gitlabId = Integer.valueOf(requestMap.get(Constants.GITLAB_ID));
        // 获取应用信息
        Map appMap = this.requestService.getAppByGitId(gitlabId);
        if (appMap == null || appMap.size() == 0) {
            return new HashMap();
        }
        String appId = (String) appMap.get(Dict.ID);
        List<String> envNameList = new ArrayList<>();
        // 该应用绑定过的生产环境
        List<Map> proEnvList = appEnvMappingDao.queryProEnvByAppId(appId);
        for (Map map : proEnvList) {
            List<String> labels = (List<String>) map.get(Dict.LABELS);
            if (labels.contains(label)) {
                envNameList.add(String.valueOf(map.get(Dict.NAME_EN)));
            }
        }

        // 该应用绑定过的SCC生产环境
        List<Map> sccProEnvList = appEnvMappingDao.querySccProEnvByAppId(appId);
        for (Map map : sccProEnvList) {
            List<String> labels = (List<String>) map.get(Dict.LABELS);
            if (labels.contains(label)) {
                envNameList.add(String.valueOf(map.get(Dict.NAME_EN)));
            }
        }

        Map returnMap = new HashMap<>();
        for (String envName : envNameList) {
            Map<String, Object> map = new HashMap<>();
            map.put(Constants.ENV, envName);
            map.put(Constants.TYPE, requestMap.get(Constants.TYPE));
            map.put(Constants.GITLAB_ID, gitlabId);
            returnMap.put(envName, queryVarByEnvAndType(map));
        }
        return returnMap;
    }

    @Override
    public List<Map> queryModelEnvByModelNameEn(Map<String, Object> requestMap) throws Exception {
        String modelNameEn = (String) requestMap.get(Constants.NAME_EN);
        Model model = modelService.getByNameEn(modelNameEn);
        if (model == null) {
            return Lists.newArrayList();
        }
        ModelEnv modelEnv = new ModelEnv();
        modelEnv.setModel_id(model.getId());
        String envNameEn = (String) requestMap.get(Constants.ENV_NAME_EN);
        if (StringUtils.isEmpty(envNameEn)) {
            return this.query(modelEnv);
        } else {
            Environment environment = environmentService.queryByNameEn(envNameEn);
            if (environment == null) {
                return Lists.newArrayList();
            }
            modelEnv.setEnv_id(environment.getId());
            return this.query(modelEnv);
        }
    }

    /**
     * 验证必填属性
     *
     * @param modelNameEn
     * @param variables
     */
    private void checkRequire(String modelNameEn, List<Map<String, Object>> variables) {
        Model model = modelService.getByNameEn(modelNameEn);
        List<Object> modelEnvKeys = model.getEnv_key();
        for (Object envKeyObj : modelEnvKeys) {
            Map<String, Object> modelEnvKey = (Map<String, Object>) envKeyObj;
            String modelEnvKeyNameEn = (String) modelEnvKey.get(Dict.NAME_EN);
            String require = String.valueOf(modelEnvKey.get(Dict.REQUIRE));
            if (Constants.ONE.equals(require)) {
                for (Map<String, Object> variable : variables) {
                    String keyNameEn = (String) variable.get(Dict.NAME_EN);
                    Object keyValue = variable.get(Dict.VALUE);
                    if (modelEnvKeyNameEn.equals(keyNameEn)) {
                        if (CommonUtils.isNullOrEmpty(keyValue)) {
                            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{modelEnvKeyNameEn + "为必填属性！"});
                        }
                        // 验证高级属性
                        if (modelEnvKey.containsKey(Dict.JSON_SCHEMA_ID)) {
                            validateJsonValue(variable, require);
                        }
                        break;
                    }
                }
            } else {
                for (Map<String, Object> variable : variables) {
                    String keyNameEn = (String) variable.get(Dict.NAME_EN);
                    Object keyValue = variable.get(Dict.VALUE);
                    if (modelEnvKeyNameEn.equals(keyNameEn)) {
                        if (!CommonUtils.isNullOrEmpty(keyValue)) {
                            // 验证高级属性
                            if (modelEnvKey.containsKey(Dict.JSON_SCHEMA_ID)) {
                                validateJsonValue(variable, require);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 新增时，加密
     *
     * @param variables
     */
    private void encryptionPwd(List<Map<String, Object>> variables) {
        if (null == variables || variables.isEmpty()) {
            return;
        }
        variables.forEach(var -> {
            // 高级类型不加密
            String dataType = (String) var.get(Dict.DATA_TYPE);
            String nameEn = String.valueOf(var.get(Dict.NAME_EN));
            if (StringUtils.isEmpty(dataType) && (Dict.FDEV_CAAS_PD.equals(nameEn) || Dict.FDEV_CAAS_REGISTRY_PD.equals(nameEn))) {
                String value = (String) var.get(Dict.VALUE);
                if (!CommonUtils.isNullOrEmpty(value)) {
                    var.put(Dict.VALUE, des3Util.encrypt(value));
                }
            }
        });
    }

    /**
     * 更新时，若与库里的密文不一致，加密
     *
     * @param variables
     */
    private void encryptionPwd(Map<String, Object> beforeVariables, List<Map<String, Object>> variables, Map<String, Object> variablesMap) {
        if (null == variables || variables.isEmpty()) {
            return;
        }
        variables.forEach(var -> {
            // 高级类型不加密
            String dataType = (String) var.get(Dict.DATA_TYPE);
            String nameEn = String.valueOf(var.get(Dict.NAME_EN));
            if (StringUtils.isEmpty(dataType) && (Dict.FDEV_CAAS_PD.equals(nameEn) || Dict.FDEV_CAAS_REGISTRY_PD.equals(nameEn))) {
                String id = (String) var.get(Dict.ID);
                Object valueObj = var.get(Dict.VALUE);
                if (valueObj != null && beforeVariables.containsKey(id)) {
                    String beforeValue = String.valueOf(beforeVariables.get(id));
                    String value = String.valueOf(valueObj);
                    // 库里跟传入的不一致
                    if (!beforeValue.equals(value)) {
                        variablesMap.put(id, des3Util.encrypt(value));
                    }
                }
            }
        });
    }

    /**
     * 校验json schema数据
     *
     * @param variable
     */
    private void validateJsonValue(Map<String, Object> variable, String require) {
        String nameEn = (String) variable.get(Dict.NAME_EN);
        String jsonSchemaId = (String) variable.get(Constants.JSON_SCHEMA_ID);
        JsonSchema jsonSchema = jsonSchemaService.getJsonSchema(jsonSchemaId);
        if (jsonSchema == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"json schema{" + jsonSchemaId + "}不存在！"});
        }
        Object jsonValue = variable.get(Constants.VALUE);
        if (Constants.ZERO.equals(require) && CommonUtils.isNullOrEmpty(jsonValue)) {
            return;
        }
        if (jsonValue instanceof List) {
            jsonValue = JSONArray.fromObject(jsonValue);
            List<Map<String, Object>> valueList = (List<Map<String, Object>>) jsonValue;
            for (int i = 0; i < valueList.size(); i++) {
                Map<String, Object> map1 = valueList.get(i);
                for (int j = i + 1; j < valueList.size(); j++) {
                    Map<String, Object> map2 = valueList.get(j);
                    if (map1.equals(map2)) {
                        throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{nameEn + "的数据不能重复"});
                    }
                }
            }
        }
        if (jsonValue instanceof Map) {
            jsonValue = JSONObject.fromObject(jsonValue);
        }
        ProcessingReport processingReport = CommonUtils.validateJsonSchema(String.valueOf(jsonValue), jsonSchema.getJsonSchema());
        if (!processingReport.isSuccess()) {
            if (logger.isDebugEnabled()) {
                logger.debug("校验高级属性出错，出错的属性是{}", nameEn);
            }
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{nameEn + "高级属性数据格式异常！"});
        }
    }

    @Override
    public String ciDecrypt(Map<String, String> map) {
        String fdevCaasPwd = map.get(Dict.FDEV_CAAS_PD);
        return des3Util.decrypt(fdevCaasPwd);
    }

    public Map pageQuery(Map qmap) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Map> list = new ArrayList<>();
        Map<String, Object> qmap1 = new HashMap<>();
        qmap1.putAll(qmap);
        if (CollectionUtils.isNotEmpty((List<String>) qmap.get(Dict.LABELS))) {
            List<Environment> environments = iEnvironmentDao.queryByLabelsFuzzy((List<String>) qmap.get(Dict.LABELS));
            List<String> collect = environments.stream().map(e -> e.getId()).collect(Collectors.toList());
            qmap.put(Dict.LABELS, collect);
        }
        if (CommonUtils.isNullOrEmpty(qmap.get(Dict.LABELS)) && CommonUtils.isNullOrEmpty(qmap.get(Dict.ENV_ID)) && !CommonUtils.isNullOrEmpty(qmap1.get(Dict.LABELS))) {
            result.put(Dict.TOTAL, 0);
            result.put(Dict.LIST, list);
            return result;
        }
        Map<String, Object> resultMap = this.modelEnvDao.pageQuery(qmap);
        List<ModelEnv> modelEnvLists = (List<ModelEnv>) resultMap.get(Dict.LIST);
        Long total = (Long) resultMap.get(Dict.TOTAL);
        for (ModelEnv me : modelEnvLists) {
            Map<String, Object> map = CommonUtils.object2Map(me);
            Environment env = new Environment();
            env.setId(me.getEnv_id());
            env = this.environmentService.queryById(env);
            Model model = new Model();
            model.setId(me.getModel_id());
            model = this.modelService.queryById(model);
            // 如果环境或实体为空，则不返回此条数据，总数量需要减掉
            if (env == null || model == null) {
                total--;
                continue;
            }
            map.put(Constants.ENV, env.getName_cn());
            map.put(Constants.ENV_NAME_EN, env.getName_en());
            map.put(Constants.LABELS, env.getLabels());
            map.put(Constants.ENV_DESC, env.getDesc());
            map.put(Constants.MODEL, model.getName_cn());
            map.put(Constants.MODEL_NAME_EN, model.getName_en());
            map.put(Constants.MODEL_VERSION, model.getVersion());
            map.put(Constants.MODEL_DESC, model.getDesc());
            map.put(Constants.SCOPE, model.getScope());
            map.put(Constants.FIRST_CATEGORY, model.getFirst_category());
            List<Object> envKey = model.getEnv_key();
            Map<String, String> ma = (Map<String, String>) map.get(Constants.VARIABLES);
            for (Object obj : envKey) {
                Map<String, Object> oMap = (Map<String, Object>) obj;
                String keyId = (String) oMap.get(Constants.ID);
                Object value = ma.get(keyId);
                oMap.put(Constants.VALUE, value);
            }
            map.put(Constants.VARIABLES, envKey);
            list.add(map);
        }
        List<Map> maps = CommonUtils.compareByEnvSourt(list, envSort);
        result.put(Dict.TOTAL, total);
        result.put(Dict.LIST, maps);
        return result;
    }

    @Override
    public List<Map<String, Object>> queryModelEnvByValue(Map<String, Object> requestMap) {
        List<Map<String, Object>> responseList = new ArrayList<>();
        String value = (String) requestMap.get(Dict.VALUE);
        // 获取满足类型的实体,若没有，则直接返回
        String type = (String) requestMap.get(Dict.TYPE);
        List<Model> modelList = modelService.getModels(type);
        if (CollectionUtils.isEmpty(modelList)) {
            return responseList;
        }
        List<String> modelIds = modelList.stream().map(Model::getId).collect(Collectors.toList());
        // 获取满足标签的环境，若没有，则直接返回
        List<String> labels = (List<String>) requestMap.get(Dict.LABELS);
        List<Environment> environmentList = environmentService.getEnvs(labels);
        if (CollectionUtils.isEmpty(environmentList)) {
            return responseList;
        }
        List<String> envIds = environmentList.stream().map(Environment::getId).collect(Collectors.toList());
        // 根据实体id列表或环境id列表获取实体环境映射值,若列表都为空，则查询所有的
        List<ModelEnv> modelEnvList = modelEnvDao.getModelEnvList(modelIds, envIds);
        for (ModelEnv modelEnv : modelEnvList) {
            Map<String, Object> variables = modelEnv.getVariables();
            for (Map.Entry<String, Object> idValueEntry : variables.entrySet()) {
                if (idValueEntry.getValue().toString().contains(value)) {
                    // 返回的map信息
                    Map<String, Object> modelEnvMap = new HashMap<>();
                    // 组装实体信息
                    String modelId = modelEnv.getModel_id();
                    // 标记实体字段id是否在当前实体中存在，若不存在，则表示此字段已被删除
                    boolean isExistModelField = false;
                    for (Model model : modelList) {
                        if (modelId.equals(model.getId())) {
                            modelEnvMap.put(Constants.MODEL_ID, modelId);
                            modelEnvMap.put(Dict.MODEL_NAME_EN, model.getName_en());
                            modelEnvMap.put(Dict.MODEL_NAME_CN, model.getName_cn());
                            List<Object> modelField = model.getEnv_key();
                            for (Object fieldObj : modelField) {
                                Map<String, Object> fieldMap = (Map<String, Object>) fieldObj;
                                if (idValueEntry.getKey().equals(fieldMap.get(Dict.ID))) {
                                    isExistModelField = true;
                                    modelEnvMap.put(Constants.FIELD_NAME_EN, fieldMap.get(Dict.NAME_EN));
                                    modelEnvMap.put(Dict.DATA_TYPE, fieldMap.get(Dict.DATA_TYPE));
                                    modelEnvMap.put(Dict.VALUE, idValueEntry.getValue());
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    // 若该字段已被删除，则不组装环境信息，进行下一次循环
                    if (!isExistModelField) {
                        continue;
                    }
                    // 组装环境信息
                    String envId = modelEnv.getEnv_id();
                    for (Environment environment : environmentList) {
                        if (envId.equals(environment.getId())) {
                            modelEnvMap.put(Constants.ENV_ID, envId);
                            modelEnvMap.put(Dict.ENV_NAME_EN, environment.getName_en());
                            modelEnvMap.put(Dict.ENV_NAME_CN, environment.getName_cn());
                            modelEnvMap.put(Dict.LABELS, environment.getLabels());
                            break;
                        }
                    }
                    responseList.add(modelEnvMap);
                }
            }
        }
        return responseList;
    }

    @Override
    public Map<String, Object> getVariablesValue(Map<String, Object> requestParam) throws Exception {
        String envName = String.valueOf(requestParam.get(Dict.ENV_NAME));
        List<String> variablesKeys = (List<String>) requestParam.get(Dict.VARIABLES_KEY);
        // 当variablesKeys为空时返回空结果
        if (CommonUtils.isNullOrEmpty(variablesKeys)){
            return new HashMap<String,Object>();
        }
        // 实体集合
        Set<String> modelSet = new HashSet<>();
        // 实体.属性字段集合
        Set<String> modelFieldSet = new HashSet<>();
        for (String variablesKey : variablesKeys) {
            modelSet.add(variablesKey.split("\\.")[0]);
            modelFieldSet.add(variablesKey);
        }
        Environment environment = this.environmentService.queryByNameEn(envName);
        if (environment == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"环境不存在！", envName});
        }
        List<Model> modelList = this.modelDao.queryByNameEn(modelSet);
        if (modelList == null || modelList.isEmpty())
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用绑定实体为空"});
        Map<String, Object> preliminaryResult = this.returnModelKeyMap(environment.getId(), modelList);
        Map<String, Object> result = new HashMap<>();
        modelFieldSet.forEach(modelField -> {
            if (preliminaryResult.containsKey(modelField))
                result.put(modelField, preliminaryResult.get(modelField));
        });
        return result;
    }
}
