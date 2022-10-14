package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.entity.*;
import com.spdb.fdev.fdevenvconfig.spdb.service.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BindServiceImpl implements BindService {

    @Autowired
    private ModelSetService modelSetService;

    @Autowired
    private IEnvironmentService iEnvironmentService;

    @Autowired
    private IAPPEnvMapingService iappEnvMapingService;

    @Autowired
    private IRequestService iRequestService;

    @Autowired
    private IAppDeployMappingService iAppDeployMappingService;

    @Autowired
    private IModelEnvService iModelEnvService;

    @Autowired
    private AppOwnFieldService appOwnFieldService;

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private IModelService iModelService;
    private static ResourceBundle labels;
    private static List<String> labelsRelList;
    private static List<Map> appDeployRelList;

    public static final Logger LOGGER = LoggerFactory.getLogger(BindServiceImpl.class);

    static {
        labels = ResourceBundle.getBundle("labels");
        Enumeration<String> labelsKeys = labels.getKeys();
        labelsRelList = new ArrayList<>();
        while (labelsKeys.hasMoreElements()) {
            String key = labelsKeys.nextElement();
            labelsRelList.add(labels.getString(key));
        }
    }

    @Override
    public void bind(Map requestParam) throws Exception {
        /** 读取环境列表，1.生产环境 env_ids 2.所有环境 env_idsAll */
        Set<String> env_ids = new HashSet<>();
        Set<String> env_idsAll = new HashSet<>();
        Set<String> scc_idsSit = new HashSet<>();
        Set<String> scc_idsUat = new HashSet<>();
        Set<String> scc_idsRel = new HashSet<>();
        Set<String> scc_idsPro = new HashSet<>();
        String caasStatus = (String) requestParam.get(Dict.CAASSTATUS);
        String sccStatus = (String) requestParam.get(Dict.SCCSTATUS);
        List<Map> envPros = (List) requestParam.get(Dict.ENVPROLIST);
        List<Map> envTest = (List<Map>) requestParam.get(Dict.ENVTESTLIST);
        // 获取SCC部署环境
        List<Map> sccSitList = (List<Map>) requestParam.get("sccSitList");
        List<Map> sccUatList = (List<Map>) requestParam.get("sccUatList");
        List<Map> sccRelList = (List<Map>) requestParam.get("sccRelList");
        List<Map> sccProList = (List<Map>) requestParam.get("sccProList");
        String network = (String) requestParam.get(Dict.NETWORK);
        for (Map envPro : envPros) {
            env_ids.add((String) envPro.get(Dict.ID));
            env_idsAll.add((String) envPro.get(Dict.ID));
        }
        for (Map map : envTest) {
            env_idsAll.add((String) map.get(Dict.ID));
        }
        for (Map map : sccSitList) {
            scc_idsSit.add((String) map.get(Dict.ID));
        }
        for (Map map : sccUatList) {
            scc_idsUat.add((String) map.get(Dict.ID));
        }
        for (Map map : sccRelList) {
            scc_idsRel.add((String) map.get(Dict.ID));
        }
        for (Map map : sccProList) {
            scc_idsPro.add((String) map.get(Dict.ID));
        }
        /** 实体组是干嘛的？ */
        String modelSetId = (String) requestParam.get(Dict.MODELSET);
        String modelSetNameCn = "";
        if (StringUtils.isNotEmpty(modelSetId)) {
            ModelSet modelSet = modelSetService.queryById(modelSetId);
            if (modelSet != null && modelSet.getNameCn() != null) {
                modelSetNameCn = modelSet.getNameCn();
            }
        }

        String sccModelSetId = (String) requestParam.get(Dict.SCCMODELSET);
        String sccModelSetNameCn = "";
        if (StringUtils.isNotEmpty(sccModelSetId)) {
            ModelSet modelSet = modelSetService.queryById(sccModelSetId);
            if (modelSet != null && modelSet.getNameCn() != null) {
                sccModelSetNameCn = modelSet.getNameCn();
            }
        }

        /** 实体列表信息 */
        List<Map> modelsInfo = (List<Map>) requestParam.get(Dict.MODELS_INFO);
        Set<String> model_ids = new HashSet<>();     //CaaS部署时绑定实体的id
        Set<String> scc_model_ids = new HashSet<>();  //SCC部署时绑定实体的id
        if (modelsInfo != null) {
            for (Map map : modelsInfo) {
                List labels = (List) map.get(Dict.LABELS);
                if(labels.contains("caas")){
                    model_ids.add((String) map.get(Dict.ID));
                }else if(labels.contains("scc")){
                    scc_model_ids.add((String) map.get(Dict.ID));
                }
            }
        }

        //查询CaaS部署时绑定的实体
        Set<Model> caasModels = new HashSet<>();
        for (String model_id : model_ids) {
            Model model1 = new Model();
            model1.setId(model_id);
            Model model = iModelService.queryById(model1);
            if (model != null) {
                caasModels.add(model);
            }
        }

        //查询SCC部署时绑定的实体
        Set<Model> sccModels = new HashSet<>();
        for (String model_id : scc_model_ids) {
            Model model1 = new Model();
            model1.setId(model_id);
            Model model = iModelService.queryById(model1);
            if (model != null) {
                sccModels.add(model);
            }
        }

        /** 应用信息 */
        String appId = (String) requestParam.get(Dict.APPID);
        JSONObject app = iRequestService.findByAppId(appId); // 发rest接口获取

        /** 修改应用环境映射 */
        AppEnvMapping appEnvMapping = new AppEnvMapping();
        appEnvMapping.setApp_id((String) app.get(Dict.ID));
        List<AppEnvMapping> appEnvMappings = iappEnvMapingService.query(appEnvMapping);
        if (appEnvMappings != null) {
            for (AppEnvMapping envMapping : appEnvMappings) {
                iappEnvMapingService.delete(envMapping); // 先删除原有的应用环境映射
            }
        }
        //如果需要使用CaaS部署
        if("1".equals(caasStatus)){
            appEnvMapping.setTags(labelsRelList); // sit uat rel
            appEnvMapping.setNetwork(network);
            appEnvMapping.setOpno((String) requestParam.get(Dict.USER_ID));
            iappEnvMapingService.save(appEnvMapping); // 重新设置应用环境映射
        }

        for (String env_id : env_ids) { // 生产环境
            // 新增应用与环境映射
            Environment environment = new Environment();
            environment.setId(env_id);
            AppEnvMapping appEnvMapping1 = new AppEnvMapping();
            appEnvMapping1.setEnv_id(env_id);
            appEnvMapping1.setApp_id((String) app.get(Dict.ID));
            appEnvMapping1.setOpno((String) requestParam.get(Dict.USER_ID));
            iappEnvMapingService.save(appEnvMapping1);
        }
        for (String env_id : scc_idsSit) { // scc sit
            // 新增应用与环境映射
            AppEnvMapping appEnvMappingScc = new AppEnvMapping();
            appEnvMappingScc.setScc_sit_id(env_id);
            appEnvMappingScc.setApp_id((String) app.get(Dict.ID));
            appEnvMappingScc.setOpno((String) requestParam.get(Dict.USER_ID));
            iappEnvMapingService.save(appEnvMappingScc);
        }
        for (String env_id : scc_idsUat) { // scc uat
            // 新增应用与环境映射
            AppEnvMapping appEnvMappingScc = new AppEnvMapping();
            appEnvMappingScc.setScc_uat_id(env_id);
            appEnvMappingScc.setApp_id((String) app.get(Dict.ID));
            appEnvMappingScc.setOpno((String) requestParam.get(Dict.USER_ID));
            iappEnvMapingService.save(appEnvMappingScc);
        }
        for (String env_id : scc_idsRel) { // scc rel
            // 新增应用与环境映射
            AppEnvMapping appEnvMappingScc = new AppEnvMapping();
            appEnvMappingScc.setScc_rel_id(env_id);
            appEnvMappingScc.setApp_id((String) app.get(Dict.ID));
            appEnvMappingScc.setOpno((String) requestParam.get(Dict.USER_ID));
            iappEnvMapingService.save(appEnvMappingScc);
        }
        for (String env_id : scc_idsPro) { // scc pro
            // 新增应用与环境映射
            AppEnvMapping appEnvMappingScc = new AppEnvMapping();
            appEnvMappingScc.setScc_pro_id(env_id);
            appEnvMappingScc.setApp_id((String) app.get(Dict.ID));
            appEnvMappingScc.setOpno((String) requestParam.get(Dict.USER_ID));
            iappEnvMapingService.save(appEnvMappingScc);
        }

        // 修改应用独立字段值
        for (String env_id : env_idsAll) {
            Environment environment = new Environment();
            environment.setId(env_id);
            Environment environment1 = iEnvironmentService.queryById(environment);
            AppOwnField appOwnField = new AppOwnField();
            appOwnField.setApp_Id(appId);
            appOwnField.setEnv_id(env_id);
            AppOwnField appOwnField11 = appOwnFieldService.queryByAppIdAndEnvId(appOwnField);
            if (appOwnField11 != null) {
                appOwnFieldService.deleteByAppId(appOwnField11.getApp_Id(), appOwnField11.getEnv_id());
            }
            appOwnField.setApp_name((String) app.get(Dict.NAME_EN));
            if (environment1 != null) {
                appOwnField.setEnv_name(environment1.getName_en());
            }
            List<Map> modelField_value = new ArrayList<>();
            List<Map> model_fields = new ArrayList<>();
            List typeList = new ArrayList();
            for (Map model : modelsInfo) {
                String env_name = (String) model.get(Dict.ENV);
                Environment environment2 = iEnvironmentService.queryByNameCN(env_name);
                if (environment2 != null && env_id.equals(environment2.getId())) {
                    List<Map> env_key = (List<Map>) model.get(Dict.ENV_KEY);
                    for (Map map : env_key) {
                        Map model_field = new HashMap();
                        Object value = map.get(Dict.OWN_VALUE);
                        String json_schema_id = (String) map.get(Dict.JSON_SCHEMA_ID);
                        JsonSchema jsonSchema = new JsonSchema();
                        if (!CommonUtils.isNullOrEmpty(json_schema_id)) {
                            jsonSchema = jsonSchemaService.getJsonSchema(json_schema_id);
                        }
                        if ("1".equals(map.get(Dict.TYPE)) && !"pvc".equals(jsonSchema.getTitle())) {
                            if (!CommonUtils.isNullOrEmpty(value)) {
                                typeList.add(map.get(Dict.TYPE));
                                model_field.put(Dict.MODEL_FIELD,
                                        model.get(Dict.NAME_EN) + "." + map.get(Dict.NAME_EN));
                                model_field.put(Dict.VALUE, value);
                            }
                            model_fields.add(model_field);
                        }
                        if ("1".equals(map.get(Dict.TYPE)) && "pvc".equals(jsonSchema.getTitle())) {
                            if (!CommonUtils.isNullOrEmpty(value)) {
                                typeList.add(map.get(Dict.TYPE));
                                List values = new ArrayList();
                                for (Object o : (List) value) {
                                    Map map1 = new HashMap();
                                    Map<String, Object> o1 = (Map<String, Object>) o;
                                    map1.put(Dict.NAME_EN, o1.get(Dict.NAME_EN));
                                    map1.put(Dict.MOUNT_PATH, o1.get(Dict.MOUNT_PATH));
                                    map1.put(Dict.SUB_PATH, o1.get(Dict.SUB_PATH));
                                    values.add(map1);
                                }
                                Map map1 = new HashMap();
                                map1.put(Dict.MODEL_FIELD, model.get(Dict.NAME_EN) + "." + map.get(Dict.NAME_EN));
                                map1.put(Dict.VALUE, values);
                                model_fields.add(map1);
                            }
                        }
                    }
                }
            }
            if (typeList.size() >= 1 && typeList.contains("1")) {
                for (Map model_field : model_fields) {
                    if (!CommonUtils.isNullOrEmpty(model_field.get(Dict.VALUE))) {
                        modelField_value.add(model_field);
                    }
                }
                appOwnField.setModelFleld_value(modelField_value);
                appOwnFieldService.save(appOwnField);
            }
        }

        /** 新增应用与ci实体映射 */
        bindCiKey(app, caasModels, sccModels, modelSetNameCn, sccModelSetNameCn, (String) requestParam.get(Dict.USER_ID));

        // 更新实体环境映射
        List<Environment> environments1 = iEnvironmentService.getEnvByLabels(labelsRelList);
        Set<Environment> environments2 = new HashSet<>(environments1);
        List<Environment> autos1 = new ArrayList<>();
        List<Environment> autos2 = new ArrayList<>();
        List<Environment> schedule1 = new ArrayList<>();
        List<Environment> schedule2 = new ArrayList<>();

        List sit = new ArrayList();
        Map map1 = new HashMap();
        Map map2 = new HashMap();
        map2.put(Dict.NAME, "SIT");

        //当需要部署在CaaS平台时
        if ("1".equals(caasStatus)) {
            for (Environment environment : environments2) {

                if (environment.getLabels().contains("caas") && environment.getName_en().contains("dmz") && environment.getName_en().contains("sit1")) {
                    autos1.add(environment);
                }
                if (environment.getLabels().contains("caas") && environment.getName_en().contains("dmz") && environment.getName_en().contains("sit2")) {
                    schedule1.add(environment);
                }

                if (environment.getLabels().contains("caas") && environment.getName_en().contains("biz") && environment.getName_en().contains("sit1")) {
                    autos2.add(environment);
                }
                if (environment.getLabels().contains("caas") && environment.getName_en().contains("biz") && environment.getName_en().contains("sit2")) {
                    schedule2.add(environment);
                }
            }
            if (network.contains("dmz")) {
                map2.put(Dict.AUTO, autos1.get(0).getId());
                map2.put(Dict.AUTO_ENV_NAME, autos1.get(0).getName_en());
                map2.put(Dict.SCHEDULE, schedule1.get(0).getId());
                map2.put(Dict.SCHEDULE_ENV_NAME, schedule1.get(0).getName_en());
            } else if (network.contains("biz")) {
                map2.put(Dict.AUTO, autos2.get(0).getId());
                map2.put(Dict.AUTO_ENV_NAME, autos2.get(0).getName_en());
                map2.put(Dict.SCHEDULE, schedule2.get(0).getId());
                map2.put(Dict.SCHEDULE_ENV_NAME, schedule2.get(0).getName_en());
            }
        }
        sit.add(map2);
        map1.put(Dict.ID, appId);
        map1.put(Dict.SIT, sit);
        map1.put(Dict.NETWORK, network);
        map1.put(Dict.CAASSTATUS, caasStatus);
        map1.put(Dict.SCCSTATUS, sccStatus);
        iRequestService.updateApp(map1);
    }

    /**
     * 应用绑定实体属性的ci_key
     */
    private void bindCiKey(JSONObject app, Set<Model> caasModels, Set<Model> sccModels, String modelSetNameCn, String sccModelSetNameCn,
                           String userId) throws Exception {

        List<Map> variables = new ArrayList<Map>();     // 1. 生成appDeployMap的variables(CaaS实体信息)
        List<Map> sccVariables = new ArrayList<Map>(); // 2. 生成appDeployMap的sccVariables(SCC实体信息)

        for (Model model : caasModels) {   // 遍历实体
            List<Object> envKeys = model.getEnv_key();
            for (Object propObj : envKeys) { // 遍历env_key 单条属性propObj
                Map variableMap = new HashMap();
                Map<String, Object> prop = (Map<String, Object>) propObj; // 属性Map: prop
                String propNameEn = (String) prop.get(Dict.NAME_EN);
                if (!CommonUtils.isNullOrEmpty(prop.get(Dict.PROP_KEY))) {
                    variableMap.put(Dict.CI_KEY, prop.get(Dict.PROP_KEY));
                } else {
                    variableMap.put(Dict.CI_KEY, "");
                }
                variableMap.put(Dict.MODEL_KEY, model.getName_en() + "." + propNameEn);
                variables.add(variableMap);
            }
        }


        for (Model model : sccModels) {   // 遍历实体
            List<Object> envKeys = model.getEnv_key();
            for (Object propObj : envKeys) { // 遍历env_key 单条属性propObj
                Map variableMap = new HashMap();
                Map<String, Object> prop = (Map<String, Object>) propObj; // 属性Map: prop
                String propNameEn = (String) prop.get(Dict.NAME_EN);
                if (!CommonUtils.isNullOrEmpty(prop.get(Dict.PROP_KEY))) {
                    variableMap.put(Dict.CI_KEY, prop.get(Dict.PROP_KEY));
                } else {
                    variableMap.put(Dict.CI_KEY, "");
                }
                variableMap.put(Dict.MODEL_KEY, model.getName_en() + "." + propNameEn);
                sccVariables.add(variableMap);
            }
        }



        Integer gitlabId = (Integer) app.get(Dict.GITLAB_PROJECT_ID);
        AppDeployMapping appDeployMapping = iAppDeployMappingService.queryByGitlabId(gitlabId); // 获取已有部署信息
        int exist = 1; // 是否已存在部署信息
        if (appDeployMapping == null) {
            appDeployMapping = new AppDeployMapping();
            appDeployMapping.setGitlabId(gitlabId);
            exist = 0;
        }
        if (!CommonUtils.isNullOrEmpty(userId)) {
            appDeployMapping.setOpno(userId);
        }
        appDeployMapping.setModelSet(modelSetNameCn);
        appDeployMapping.setVariables(variables);
        appDeployMapping.setScc_modeSet(sccModelSetNameCn);
        appDeployMapping.setScc_variables(sccVariables);
        if (exist == 1)
            iAppDeployMappingService.update(appDeployMapping);
        else
            iAppDeployMappingService.save(appDeployMapping);
    }
}
