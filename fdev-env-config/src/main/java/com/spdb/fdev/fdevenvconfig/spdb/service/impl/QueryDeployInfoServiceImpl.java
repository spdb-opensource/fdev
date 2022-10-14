package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.GitlabTransport;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IAppConfigMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IDefinedVariablesDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IEnvironmentDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ModelSetDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.*;
import com.spdb.fdev.fdevenvconfig.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class QueryDeployInfoServiceImpl implements QueryDeployInfoService {

    @Autowired
    private IRequestService iRequestService;

    @Autowired
    private IAppDeployMappingService iAppDeployMappingService;

    @Autowired
    private IModelService iModelService;

    @Autowired
    private IAPPEnvMapingService iappEnvMapingService;

    @Autowired
    private IEnvironmentService iEnvironmentService;

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private IModelEnvService iModelEnvService;

    @Autowired
    private ModelSetService modelSetService;

    @Autowired
    private AppOwnFieldService appOwnFieldService;

    @Autowired
    private ModelSetDao modelSetDao;

    @Autowired
    private IEnvironmentDao environmentDao;

    @Autowired
    private IDefinedVariablesDao definedVariablesDao;

    @Autowired
    private GitlabTransport gitlabTransport;

    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/

    @Value("${env.sort}")
    private List<String> envSort;

    @Value("${gitlab.token}")
    private String gitlab_token;

    private static ResourceBundle labels;
    private static List<String> labelsRelList;

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
    public Map<String, Object> query(Map<String, Object> requestParam) {
        // 获取状态不为0的应用id和GitLab id
        List<Map<String, Object>> simpleAppInfoList = iRequestService.queryApps();
        List<String> appIdList = new ArrayList<>();
        List<Integer> gitLabIdList = new ArrayList<>();
        for (Map<String, Object> appInfoMap : simpleAppInfoList) {
            appIdList.add(String.valueOf(appInfoMap.get(Dict.ID)));
            gitLabIdList.add((Integer) appInfoMap.get(Dict.GITLAB_PROJECT_ID));
        }
        // 若env_id为空，则以app-deploy-map为主表查询；反之，则以app-env-map为主表查询
        String envId = (String) requestParam.get(Dict.ENV_ID);
        if (StringUtils.isEmpty(envId)) {
            return getDeployInfoByAppDeploy(requestParam, gitLabIdList);
        }
        return getDeployByAppEnv(requestParam, appIdList);
    }

    /**
     * 以app-env-map为主表查询
     *
     * @param requestParam
     * @param appIdList
     * @return
     */
    private Map<String, Object> getDeployByAppEnv(Map<String, Object> requestParam, List<String> appIdList) {
        Map<String, Object> responseMap = iappEnvMapingService.queryAppEnvMapByPage(requestParam, appIdList);
        List<Map<String, Object>> appEnvMappingList = (List<Map<String, Object>>) responseMap.get(Dict.LIST);
        // 根据分页查出来的应用id，获取应用模块详细信息
        Set<String> pageAppIdList = new HashSet<>();
        appEnvMappingList.forEach(appEnvMapping -> pageAppIdList.add(String.valueOf(appEnvMapping.get(Dict.APPID))));
        List<Map<String, Object>> appInfoList = iRequestService.getAppByIdsOrGitlabIds(Dict.ID, pageAppIdList,
                new HashSet<>());
        // 遍历应用列表，组装应用信息
        Set<Integer> pageGitLabIdList = new HashSet<>();
        for (Map<String, Object> appEnvMapping : appEnvMappingList) {
            for (Map<String, Object> appInfo : appInfoList) {
                if (appInfo.get(Dict.ID).equals(appEnvMapping.get(Dict.APPID))) {
                    Integer gitLabId = (Integer) appInfo.get(Dict.GITLAB_PROJECT_ID);
                    appEnvMapping.put(Dict.GITLAB_PROJECT_ID, appInfo.get(Dict.GITLAB_PROJECT_ID));
                    pageGitLabIdList.add(gitLabId);
                    Map<String, Object> appInfoMap = new HashMap<>();
                    appInfoMap.put(Dict.ID, appInfo.get(Dict.ID));
                    appInfoMap.put(Dict.NAME_EN, appInfo.get(Dict.NAME_EN));
                    appEnvMapping.put(Dict.APPINFO, appInfoMap);
                    // 行内负责人
                    List<Map> spdbManagers = (List<Map>) appInfo.get(Dict.SPDB_MANAGERS);
                    // 应用负责人
                    List<Map> devManagers = (List<Map>) appInfo.get(Dict.DEV_MANAGERS);
                    if (CollectionUtils.isNotEmpty(devManagers)) {
                        spdbManagers.addAll(devManagers);
                    }
                    appEnvMapping.put(Dict.MANAGERS, spdbManagers);
                    // 所属组
                    Map<String, Object> group = (Map<String, Object>) appInfo.get(Dict.GROUP);
                    appEnvMapping.put(Dict.MANAGERS, spdbManagers);
                    appEnvMapping.put(Dict.GROUP, group.get(Dict.NAME));
                    break;
                }
            }
        }
        // 遍历app-deploy-map列表，组装实体组信息
        List<AppDeployMapping> appDeployMappingList = iAppDeployMappingService.queryByGitlabIds(pageGitLabIdList);
        for (Map<String, Object> appEnvMapping : appEnvMappingList) {
            for (AppDeployMapping appDeployMapping : appDeployMappingList) {
                if (appEnvMapping.get(Dict.GITLAB_PROJECT_ID).equals(appDeployMapping.getGitlabId())) {
                    Map<String, Object> modelSetMap = new HashMap<>();
                    modelSetMap.put(Dict.NAMECN, appDeployMapping.getModelSet());
                    appEnvMapping.put(Dict.MODELSET, modelSetMap);
                }
            }
            // 去掉多余数据
            appEnvMapping.remove(Dict.GITLAB_PROJECT_ID);
            appEnvMapping.remove(Dict.APPID);
        }
        return responseMap;
    }

    /**
     * 以app-deploy-map为主表查询
     *
     * @param requestParam
     * @param gitLabIdList
     * @return
     */
    private Map<String, Object> getDeployInfoByAppDeploy(Map<String, Object> requestParam, List<Integer> gitLabIdList) {
        Map<String, Object> responseMap = iAppDeployMappingService.queryByPage(requestParam, gitLabIdList);
        List<Map<String, Object>> appDeployMappingList = (List<Map<String, Object>>) responseMap.get(Dict.LIST);
        Set<Integer> pageGitLabIdList = new HashSet<>();
        appDeployMappingList.forEach(
                appDeployMapping -> pageGitLabIdList.add((Integer) appDeployMapping.get(Dict.GITLAB_PROJECT_ID)));
        List<String> pageAppIdList = new ArrayList<>();
        // 遍历应用列表，组装应用相关信息
        List<Map<String, Object>> appInfoList = iRequestService.getAppByIdsOrGitlabIds(Dict.GITLAB_PROJECT_ID,
                new HashSet<>(), pageGitLabIdList);
        for (Map<String, Object> appDeployMapping : appDeployMappingList) {
            for (Map<String, Object> appInfo : appInfoList) {
                if (appInfo.get(Dict.GITLAB_PROJECT_ID).equals(appDeployMapping.get(Dict.GITLAB_PROJECT_ID))) {
                    String applicationId = (String) appInfo.get(Dict.ID);
                    appDeployMapping.put(Dict.APPID, appInfo.get(Dict.ID));
                    pageAppIdList.add(applicationId);
                    Map<String, Object> appInfoMap = new HashMap<>();
                    appInfoMap.put(Dict.ID, appInfo.get(Dict.ID));
                    appInfoMap.put(Dict.NAME_EN, appInfo.get(Dict.NAME_EN));
                    appDeployMapping.put(Dict.APPINFO, appInfoMap);
                    // 行内负责人
                    List<Map> spdbManagers = (List<Map>) appInfo.get(Dict.SPDB_MANAGERS);
                    // 应用负责人
                    List<Map> devManagers = (List<Map>) appInfo.get(Dict.DEV_MANAGERS);
                    if (CollectionUtils.isNotEmpty(devManagers)) {
                        spdbManagers.addAll(devManagers);
                    }
                    // 所属组
                    Map<String, Object> group = (Map<String, Object>) appInfo.get(Dict.GROUP);
                    appDeployMapping.put(Dict.MANAGERS, spdbManagers);
                    appDeployMapping.put(Dict.GROUP, group.get(Dict.NAME));
                    break;
                }
            }
        }
        // 遍历app-env-map列表，组装测试环境和生产环境相关信息
        List<AppEnvMapping> appEnvMappingList = iappEnvMapingService.queryByAppIds(pageAppIdList);
        for (Map<String, Object> appDeployMapping : appDeployMappingList) {
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put(Dict.MODELSET, appDeployMapping.get(Dict.MODELSET));
            returnMap.put(Dict.MANAGERS, appDeployMapping.get(Dict.MANAGERS));
            List<Environment> testEnvList = new ArrayList<>();
            List<Environment> proEnvList = new ArrayList<>();
            for (AppEnvMapping appEnvMapping : appEnvMappingList) {
                if (appDeployMapping.get(Dict.APPID).equals(appEnvMapping.getApp_id())) {
                    String environmentId = appEnvMapping.getEnv_id();
                    if (StringUtils.isEmpty(environmentId)) {
                        // 组装测试环境
                        String network = appEnvMapping.getNetwork();
                        if (!StringUtils.isEmpty(network)) {
                            String[] networks = network.split(",");
                            List<String> tags = appEnvMapping.getTags();
                            Collections.addAll(tags, networks);
                            testEnvList = iEnvironmentService.queryByLabelsFuzzy(tags);
                        }
                    } else {
                        // 组装生产环境
                        Environment proEnv = iEnvironmentService.queryById(environmentId);
                        proEnvList.add(proEnv);
                    }
                }
            }
            appDeployMapping.put(Dict.TESTENV, testEnvList);
            appDeployMapping.put(Dict.PRODUCTENV, proEnvList);
            // 去掉多余数据
            appDeployMapping.remove(Dict.GITLAB_PROJECT_ID);
            appDeployMapping.remove(Dict.APPID);
        }
        return responseMap;
    }

    @Override
    public Map queryDeployByAppId(Map requestParam) throws Exception {
        String app_id = (String) requestParam.get(Dict.APPID);
        HashSet<String> set = new HashSet<>();    //绑定的caas实体英文名的集合
        HashSet<String> set1 = new HashSet<>();   //绑定的scc实体英文名的集合
        List<Model> caasModels = new ArrayList<>();    //绑定的caas实体
        List<Model> sccModels = new ArrayList<>();   //绑定的scc实体
        Set<String> envIds = new HashSet<>();
        Map modelSetMsg = new HashMap();
        Map app = iRequestService.findByAppId(app_id);
        Integer gitlabId = (Integer) app.get(Dict.GITLAB_PROJECT_ID);
        AppDeployMapping appDeployMapping = iAppDeployMappingService.queryByGitlabId(gitlabId);
        if (appDeployMapping != null) {
            String modelSetName = appDeployMapping.getModelSet();
            if (!CommonUtils.isNullOrEmpty(modelSetName)) {
                ModelSet modelSet = modelSetService.queryByName(modelSetName);
                if (!CommonUtils.isNullOrEmpty(modelSet)) {
                    modelSetMsg.put(Dict.ID, modelSet.getId());
                    modelSetMsg.put("nameCn", modelSet.getNameCn());
                }
            } else {
                modelSetMsg = null;
            }
            List<Map> variables = appDeployMapping.getVariables();
            if (variables != null && variables.size() >= 1) {
                for (Map variable : variables) {
                    String model_key = (String) variable.get(Dict.MODEL_KEY);
                    String[] split = model_key.split("\\.");
                    set.add(split[0]);
                }
            }

            List<Map> sccVariables = appDeployMapping.getScc_variables();
            if (sccVariables != null && sccVariables.size() >= 1) {
                for (Map variable : sccVariables) {
                    String model_key = (String) variable.get(Dict.MODEL_KEY);
                    String[] split = model_key.split("\\.");
                    set1.add(split[0]);
                }
            }

        }

        //CaaS部署时绑定的实体
        for (String s : set) {
            Model model = new Model();
            model.setName_en(s);
            List<Model> models1 = iModelService.query(model);
            if (models1 != null && models1.size() > 0) {
                caasModels.add(models1.get(0));
            }
        }

        //SCC部署时绑定的实体
        for (String s : set1) {
            Model model = new Model();
            model.setName_en(s);
            List<Model> models1 = iModelService.query(model);
            if (models1 != null && models1.size() > 0) {
                sccModels.add(models1.get(0));
            }
        }

        AppEnvMapping appEnvMapping = new AppEnvMapping();
        appEnvMapping.setApp_id(app_id);
        List<Environment> envs = iEnvironmentService.getEnvByLabels(labelsRelList);
        Set<Environment> test_env = new HashSet<>(envs);
        Set<String> dmz = new HashSet<>();
        Set<String> biz = new HashSet<>();

        if ("1".equals(app.get(Dict.CAASSTATUS))) {
            for (Environment environment : test_env) {
                if (environment.getLabels().contains("dmz") && environment.getLabels().contains("caas")) {
                    dmz.add(environment.getId());
                }
                if (environment.getLabels().contains("biz") && environment.getLabels().contains("caas")) {
                    biz.add(environment.getId());
                }
            }
        }

        List<AppEnvMapping> appEnvMappings = iappEnvMapingService.query(appEnvMapping);
        if (appEnvMappings != null && appEnvMappings.size() >= 1) {
            for (AppEnvMapping envMapping : appEnvMappings) {
                String network = envMapping.getNetwork();
                if (!CommonUtils.isNullOrEmpty(envMapping.getEnv_id())) {
                    envIds.add(envMapping.getEnv_id());
                } else if (!CommonUtils.isNullOrEmpty(network)) {
                    if (network.contains("dmz")) {
                        envIds.addAll(dmz);
                    }
                    if (network.contains("biz")) {
                        envIds.addAll(biz);
                    }
                } else if (!CommonUtils.isNullOrEmpty(envMapping.getScc_sit_id())) {
                    envIds.add(envMapping.getScc_sit_id());
                } else if (!CommonUtils.isNullOrEmpty(envMapping.getScc_uat_id())) {
                    envIds.add(envMapping.getScc_uat_id());
                } else if (!CommonUtils.isNullOrEmpty(envMapping.getScc_rel_id())) {
                    envIds.add(envMapping.getScc_rel_id());
                } else if (!CommonUtils.isNullOrEmpty(envMapping.getScc_pro_id())) {
                    envIds.add(envMapping.getScc_pro_id());
                }
            }
        }
        return assembleDeployMsg(app, caasModels, sccModels, envIds, modelSetMsg);
    }

    @Override
    public Map queryBindMsgByApp(Map requestParam) throws Exception {
        Map map = new HashMap();
        String app_id = (String) requestParam.get(Dict.APPID);
        HashSet<String> set = new HashSet<>();
        HashSet<String> set1 = new HashSet<>();
        List<Model> models = new ArrayList<>();
        List<Model> sccModels = new ArrayList<>();
        Set<String> envIds = new HashSet<>();
        Map modelSetMsg = new HashMap();
        Map sccModelSetMsg = new HashMap();
        JSONObject app1 = iRequestService.findByAppId(app_id);
        com.alibaba.fastjson.JSONObject app = (com.alibaba.fastjson.JSONObject) com.alibaba.fastjson.JSONObject
                .parse(app1.toString());
        Integer gitlabId = (Integer) app.get(Dict.GITLAB_PROJECT_ID);
        String network = (String) app.get(Dict.NETWORK);
        String caasStatus = (String) app.get(Dict.CAASSTATUS);  // 是否部署在CaaS平台
        String sccStatus = (String) app.get(Dict.SCCSTATUS);    // 是否部署在SCC平台
        AppDeployMapping appDeployMapping = iAppDeployMappingService.queryByGitlabId(gitlabId);
        List<Map> variables = new ArrayList<>();
        List<Map> sccVariables = new ArrayList<>();
        if (appDeployMapping != null) {
            String modelSetName = appDeployMapping.getModelSet();
            if (!CommonUtils.isNullOrEmpty(modelSetName)) {
                ModelSet modelSet = modelSetDao.queryByNameContAllStatus(modelSetName);
                if (modelSet != null) {
                    modelSetMsg.put(Dict.ID, modelSet.getId());
                    modelSetMsg.put("nameCn", modelSet.getNameCn());
                }
            } else {
                modelSetMsg = null;
            }
            String sccModelSetName = appDeployMapping.getScc_modeSet();
            if (!CommonUtils.isNullOrEmpty(sccModelSetName)) {
                ModelSet modelSet = modelSetDao.queryByNameContAllStatus(sccModelSetName);
                if (modelSet != null) {
                    sccModelSetMsg.put(Dict.ID, modelSet.getId());
                    sccModelSetMsg.put("nameCn", modelSet.getNameCn());
                }
            } else {
                sccModelSetMsg = null;
            }
            variables = appDeployMapping.getVariables();
            sccVariables = appDeployMapping.getScc_variables();
        }

        if (variables != null && variables.size() >= 1) {
            for (Map variable : variables) {
                String model_key = (String) variable.get(Dict.MODEL_KEY);
                String[] split = model_key.split("\\.");
                for (int i = 0; i < split.length; i++) {
                    set.add(split[0]);
                }
            }
        }
        if (sccVariables != null && sccVariables.size() >= 1) {
            for (Map variable : sccVariables) {
                String model_key = (String) variable.get(Dict.MODEL_KEY);
                String[] split = model_key.split("\\.");
                set1.add(split[0]);
            }
        }

        //CaaS部署时绑定的实体
        for (String s : set) {
            Model model = new Model();
            model.setName_en(s);
            List<Model> models1 = iModelService.query(model);
            if (models1 != null && models1.size() > 0) {
                Model model1 = models1.get(0);
                models.add(model1);
            }
        }

        //SCC部署时绑定的实体
        for (String s : set1) {
            Model model = new Model();
            model.setName_en(s);
            List<Model> models1 = iModelService.query(model);
            if (models1 != null && models1.size() > 0) {
                Model model1 = models1.get(0);
                sccModels.add(model1);
            }
        }

        List<String> categoryList = new ArrayList();
        Map<String, Object> category = iModelService.queryModelCategory();
        if (category != null) {
            List categorys = (List) category.get(Dict.CATEGORY);
            for (Object o : categorys) {
                Map<String, Object> map2 = (Map<String, Object>) o;
                if (!CommonUtils.isNullOrEmpty(map2.get("ci"))) {
                    List<String> o1 = (List<String>) map2.get("ci");
                    for (String o2 : o1) {
                        categoryList.add(o2);
                    }
                }
            }
        }

        AppEnvMapping appEnvMapping = new AppEnvMapping();
        appEnvMapping.setApp_id(app_id);
        List<AppEnvMapping> appEnvMappings = iappEnvMapingService.query(appEnvMapping);
        Set<Environment> scc_sit_env = new HashSet<>();
        Set<Environment> scc_uat_env = new HashSet<>();
        Set<Environment> scc_rel_env = new HashSet<>();
        Set<Environment> scc_pro_env = new HashSet<>();
        for (AppEnvMapping envMapping : appEnvMappings) {
            if (!CommonUtils.isNullOrEmpty(envMapping.getEnv_id())) {
                envIds.add(envMapping.getEnv_id());
            }
            if (!CommonUtils.isNullOrEmpty(envMapping.getScc_sit_id())) {
                scc_sit_env.add(environmentDao.queryById(envMapping.getScc_sit_id()));
            }
            if (!CommonUtils.isNullOrEmpty(envMapping.getScc_uat_id())) {
                scc_uat_env.add(environmentDao.queryById(envMapping.getScc_uat_id()));
            }
            if (!CommonUtils.isNullOrEmpty(envMapping.getScc_rel_id())) {
                scc_rel_env.add(environmentDao.queryById(envMapping.getScc_rel_id()));
            }
            if (!CommonUtils.isNullOrEmpty(envMapping.getScc_pro_id())) {
                scc_pro_env.add(environmentDao.queryById(envMapping.getScc_pro_id()));
            }
        }

        Set<Environment> pro_env = new HashSet<>();
        for (String envId : envIds) {
            Environment environment = new Environment();
            environment.setId(envId);
            Environment environment1 = iEnvironmentService.queryById(environment);
            List<String> labels = environment1.getLabels();
            if (labels.contains(Dict.PRO)) {
                pro_env.add(environment1);
            }
        }
        // 通过标签去查测试环境

        List<Environment> envs = iEnvironmentService.getEnvByLabels(labelsRelList);
        Set<Environment> test_env = new HashSet<>(envs);
        if ("biz".equals(network)) {
            map.put(Dict.NETWORK, "biz");
        } else if ("dmz".equals(network)) {
            map.put(Dict.NETWORK, "dmz");
        } else if (!CommonUtils.isNullOrEmpty(network) && network.contains("biz") && network.contains("dmz")) {
            map.put(Dict.NETWORK, network);
        } else {
            map.put(Dict.NETWORK, "");
        }
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put(Dict.APPID, app.get(Dict.ID));
        appInfo.put(Dict.APP_NAME_EN, app.get(Dict.NAME_EN));
        map.put(Dict.APP_NAME_EN, app.get(Dict.NAME_EN));
        map.put(Dict.APPINFO, appInfo);
        map.put(Dict.MODELSETMSG, modelSetMsg);
        map.put(Dict.SCCMODELSETMSG, sccModelSetMsg);
        map.put(Dict.CAASSTATUS, caasStatus);
        map.put(Dict.SCCSTATUS, sccStatus);
        map.put(Dict.MODELS_INFO, models);
        map.put(Dict.SCC_MODELS_INFO, sccModels);
        map.put(Dict.ENVTESTLIST, test_env);
        map.put(Dict.ENVPROLIST, pro_env);
        map.put(Dict.SCCSITLIST, scc_sit_env);
        map.put(Dict.SCCUATLIST, scc_uat_env);
        map.put(Dict.SCCRELLIST, scc_rel_env);
        map.put(Dict.SCCPROLIST, scc_pro_env);

        return map;
    }

    @Override
    public Map queryRealTimeBindMsg(Map requestParam) throws Exception {
        String app_id = (String) requestParam.get(Dict.APPID);
        List<Map> env_tests = (List<Map>) requestParam.get(Dict.ENVTESTLIST);
        List<Map> env_pros = (List<Map>) requestParam.get(Dict.ENVPROLIST);
        List<Map> env_sccs = (List<Map>) requestParam.get(Dict.ENVSCCLIST);
        List<Map> models = (List<Map>) requestParam.get(Dict.MODELS_INFO);          //获取CaaS部署时绑定的实体
        List<Map> models1 = (List<Map>) requestParam.get(Dict.SCC_MODELS_INFO);     //获取SCC部署时绑定的实体
        List<Model> caasModels = new ArrayList<>();
        List<Model> sccModels = new ArrayList<>();

        //CaaS部署时绑定的实体
        if(models!=null){
            for (Map model : models) {
                Model model1 = new Model();
                model1.setId((String) model.get(Dict.ID));
                Model model2 = iModelService.queryById(model1);
                if (model2 != null) {
                    caasModels.add(model2);
                }
            }
        }

        //SCC部署时绑定的实体
        if(models1!=null){
            for (Map model : models1) {
                Model model1 = new Model();
                model1.setId((String) model.get(Dict.ID));
                Model model2 = iModelService.queryById(model1);
                if (model2 != null) {
                    sccModels.add(model2);
                }
            }
        }

        JSONObject app = iRequestService.findByAppId(app_id);
        Set<String> env_ids = new HashSet<>();
        if (env_tests != null) {
            for (Map env_test : env_tests) {
                env_ids.add((String) env_test.get(Dict.ID));
            }
        }
        if (env_pros != null) {
            for (Map env_pro : env_pros) {
                env_ids.add((String) env_pro.get(Dict.ID));
            }
        }
        if (env_sccs != null) {
            for (Map env_scc : env_sccs) {
                env_ids.add((String) env_scc.get(Dict.ID));
            }
        }
        Map modelSetMsg = new HashMap();
        return assembleRealTimeDeployMsg(app, caasModels, sccModels, env_ids, modelSetMsg);
    }

    private Map assembleDeployMsg(Map app, List<Model> caasModels, List<Model> sccModels, Set<String> envIds, Map modelSetMsg) throws Exception {
        Map data = new HashMap();
        Set<Environment> test_env = new HashSet<>();
        Set<Environment> pro_env = new HashSet<>();
        Set<Environment> scc_env = new HashSet<>();
        Set<Environment> caas_env = new HashSet<>();
        List<Map> maps = new ArrayList<>();
        List<String> types = new ArrayList<>();
        Set<String> sccEnv = new HashSet<>();
        Iterator<String> iterator = envIds.iterator();
        while (iterator.hasNext()) {
            String envId = iterator.next();
            Environment environment = new Environment();
            environment.setId(envId);
            Environment environment1 = iEnvironmentService.queryById(environment);
            if (environment1.getLabels().contains(Dict.SCC)) {
                scc_env.add(environment1);
            } else if (environment1.getLabels().contains(Dict.PRO)) {
                pro_env.add(environment1);
                caas_env.add(environment1);
            } else {
                test_env.add(environment1);
                caas_env.add(environment1);
            }
        }
        Map caas_model_env_mapping = queryDeployMsg(app, caasModels, caas_env); // CaaS平台的部署信息
        Map scc_model_env_mapping = queryDeployMsg(app, sccModels, scc_env); // SCC平台的部署信息
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put(Dict.APPID, app.get(Dict.ID));
        appInfo.put(Dict.APP_NAME_EN, app.get(Dict.NAME_EN));
        data.put(Dict.APP_NAME_EN, app.get(Dict.NAME_EN));
        data.put(Dict.APPINFO, appInfo);
        data.put(Dict.MODELSETMSG, modelSetMsg);
        data.put(Dict.ENVTESTLIST, test_env);
        data.put(Dict.ENVPROLIST, pro_env);
        data.put(Dict.ENVSCCLIST, scc_env);
        data.put(Dict.CAAS_MODEL_ENV_MAPPING, caas_model_env_mapping);
        data.put(Dict.SCC_MODEL_ENV_MAPPING, scc_model_env_mapping);
        return data;
    }

    /**
     * @param app
     * @param models
     * @param envs
     * @return 根据应用、实体、环境查询应用的部署信息
     * @throws Exception
     */
    private Map queryDeployMsg(Map app, List<Model> models, Set<Environment> envs) throws Exception {
        List<Map> maps = new ArrayList<Map>();
        for (Environment environment : envs) {
            for (Model model : models) {
                List all_pvc_value = new ArrayList();
                ModelEnv modelEnv = new ModelEnv();
                modelEnv.setEnv_id(environment.getId());
                modelEnv.setModel_id(model.getId());
                ModelEnv modelEnv1 = iModelEnvService.queryByEnvIdAndModelId(modelEnv);
                AppOwnField appOwnField = new AppOwnField();
                appOwnField.setApp_Id((String) app.get(Dict.ID));
                appOwnField.setEnv_id(environment.getId());
                AppOwnField appOwnField1 = appOwnFieldService.queryByAppIdAndEnvId(appOwnField);
                List<Map> modelFleld_value = new ArrayList<>();
                if (appOwnField1 != null) {
                    modelFleld_value = appOwnField1.getModelFleld_value();
                }
                Map jsonObject = JSONObject.fromObject(model);
                jsonObject.remove("_id");
                List<Object> oldEnv_key = (List<Object>) jsonObject.remove(Dict.ENV_KEY);
                List<Object> newEnv_key = new ArrayList<>();
                for (Object envKey : oldEnv_key) {
                    Map<String, Object> envKeyMap = (Map<String, Object>) envKey;
                    String json_schema_id = (String) envKeyMap.get(Dict.JSON_SCHEMA_ID);
                    JsonSchema jsonSchema = new JsonSchema();
                    if (!CommonUtils.isNullOrEmpty(json_schema_id)) {
                        jsonSchema = jsonSchemaService.getJsonSchema(json_schema_id);
                    }
                    String id = (String) envKeyMap.get(Dict.ID);
                    if (modelEnv1 != null) {
                        Map variables = modelEnv1.getVariables();
                        if (!CommonUtils.isNullOrEmpty(jsonSchema.getTitle())) {
                            if (jsonSchema.getTitle().equals("pvc") && !CommonUtils.isNullOrEmpty(variables.get(id))) {
                                List o = (List) variables.get(id);
                                for (Object o1 : o) {
                                    Map<String, Object> map = (Map<String, Object>) o1;
                                    map.put(Dict.MOUNT_PATH, "");
                                    map.put(Dict.SUB_PATH, "");
                                    all_pvc_value.add(map);
                                }
                            }
                        }
                    }
                }
                for (Object envKey : oldEnv_key) {
                    Map<String, Object> envKeyMap = (Map<String, Object>) envKey;
                    String name_en = (String) envKeyMap.get(Dict.NAME_EN);
                    String id = (String) envKeyMap.get(Dict.ID);
                    String type = (String) envKeyMap.get(Dict.TYPE);
                    JsonSchema jsonSchema = new JsonSchema();
                    String json_schema_id = (String) envKeyMap.get(Dict.JSON_SCHEMA_ID);
                    if (!CommonUtils.isNullOrEmpty(json_schema_id)) {
                        jsonSchema = jsonSchemaService.getJsonSchema(json_schema_id);
                        envKeyMap.put(Dict.JSON_SCHEMA, jsonSchema);
                    }
                    if(envKeyMap.get(Dict.PROP_KEY)!=null&&("null".equals(envKeyMap.get(Dict.PROP_KEY).toString()))){
                        envKeyMap.put(Dict.PROP_KEY, "");
                    }
                    if (modelEnv1 != null) {
                        Map variables = modelEnv1.getVariables();
                        if (!CommonUtils.isNullOrEmpty(variables.get(id))) {
                            envKeyMap.put(Dict.VALUE, variables.get(id));
                        } else {
                            envKeyMap.put(Dict.VALUE, "");
                        }
                        newEnv_key.add(envKeyMap);
                    } else {
                        newEnv_key.add(envKeyMap);
                    }
                    if (!CommonUtils.isNullOrEmpty(type) && "1".equals(type)) {
                        String value2 = "";
                        List<String> name = new ArrayList<>();
                        if (modelFleld_value.size() >= 1) {
                            for (Map map : modelFleld_value) {
                                String modelField = (String) map.get(Dict.MODEL_FIELD);
                                String[] split = modelField.split("\\.");
                                name.add(split[1]);
                                // 非pvc的非高级应用独立值
                                if (model.getName_en().equals(split[0]) && name_en.equals(split[1])
                                        && CommonUtils.isNullOrEmpty(json_schema_id)) {
                                    value2 = (String) map.get(Dict.VALUE);
                                    envKeyMap.put(Dict.HAVE_PATH, "1");
                                    envKeyMap.put(Dict.OWN_VALUE, value2);
                                }
                                // 非pvc的高级应用独立值
                                if (model.getName_en().equals(split[0]) && name_en.equals(split[1])
                                        && !CommonUtils.isNullOrEmpty(json_schema_id)) {
                                    Object value = map.get(Dict.VALUE);
                                    envKeyMap.put(Dict.HAVE_PATH, "1");
                                    envKeyMap.put(Dict.OWN_VALUE, value);
                                }
                                // pvc的应用独立值
                                if (model.getName_en().equals(split[0]) && name_en.equals(split[1])
                                        && !CommonUtils.isNullOrEmpty(jsonSchema.getTitle())
                                        && jsonSchema.getTitle().equals("pvc")) {
                                    envKeyMap.put(Dict.HAVE_PATH, "1");
                                    List values = (List) map.get(Dict.VALUE);
                                    List own_values = new ArrayList();
                                    for (Object value : values) {
                                        Map<String, Object> value1 = (Map<String, Object>) value;
                                        String name_en1 = (String) value1.get(Dict.NAME_EN);
                                        if (modelEnv1 != null) {
                                            for (Object o : all_pvc_value) {
                                                Map<String, Object> o1 = (Map<String, Object>) o;
                                                if (o1.get(Dict.NAME_EN).equals(name_en1)) {
                                                    if (!CommonUtils.isNullOrEmpty(value1.get(Dict.MOUNT_PATH))) {
                                                        o1.put(Dict.MOUNT_PATH, value1.get(Dict.MOUNT_PATH));
                                                    }
                                                    if (!CommonUtils.isNullOrEmpty(value1.get(Dict.SUB_PATH))) {
                                                        o1.put(Dict.SUB_PATH, value1.get(Dict.SUB_PATH));
                                                    }
                                                    own_values.add(o1);
                                                }
                                            }
                                        }
                                    }
                                    envKeyMap.put(Dict.OWN_VALUE, own_values);
                                }
                            }
                        } else {
                            envKeyMap.put(Dict.OWN_VALUE, value2);
                        }
                        if (CommonUtils.isNullOrEmpty(appOwnField1)) {
                            envKeyMap.put(Dict.OWN_VALUE, "");
                        }
                        if (!name.contains(envKeyMap.get(Dict.NAME_EN))) {
                            envKeyMap.put(Dict.OWN_VALUE, "");
                        }
                    }

                }
                jsonObject.put(Dict.ENV_KEY, newEnv_key);
                jsonObject.put(Dict.ENV, environment.getName_cn());
                jsonObject.put(Dict.LABELS, environment.getLabels());
                jsonObject.put(Dict.ALL_PVC_VALUE, all_pvc_value);
                maps.add(jsonObject);
            }
        }
        // 根据环境标签排序
        List<Map> sortedMaps = CommonUtils.compareByEnvSourt(maps, envSort);
        List<String> set = new ArrayList<>();
        Map model_env_mapping = new LinkedHashMap();
        for (Map map : sortedMaps) {
            set.add((String) map.get(Dict.ENV));
        }
        List<String> setDistinct = set.stream().distinct().collect(Collectors.toList());
        for (String s : setDistinct) {
            List<Map> maps1 = new ArrayList<>();
            for (Map map : sortedMaps) {
                if (!CommonUtils.isNullOrEmpty(map.get(Dict.ENV))) {
                    if (s.equals(map.get(Dict.ENV))) {
                        maps1.add(map);
                    }
                }
            }
            model_env_mapping.put(s, maps1);
        }
        return model_env_mapping;
    }

    private Map assembleRealTimeDeployMsg(Map app, List<Model> caasModels, List<Model> sccModels, Set<String> envIds, Map modelSetMsg)
            throws Exception {
        Map data = new HashMap();
        Set<Environment> test_env = new HashSet<>();
        Set<Environment> pro_env = new HashSet<>();
        Set<Environment> scc_env = new HashSet<>();
        Set<Environment> caas_env = new HashSet<>();
        List<Map> maps = new ArrayList<>();
        Set<String> sccEnv = new HashSet<>();
        Iterator<String> iterator = envIds.iterator();
        while (iterator.hasNext()) {
            String envId = iterator.next();
            Environment environment = new Environment();
            environment.setId(envId);
            Environment environment1 = iEnvironmentService.queryById(environment);
            if (environment1.getLabels().contains(Dict.SCC)) {
                scc_env.add(environment1);
            } else if (environment1.getLabels().contains(Dict.PRO)) {
                pro_env.add(environment1);
                caas_env.add(environment1);
            } else {
                test_env.add(environment1);
                caas_env.add(environment1);
            }
        }
        Map caas_model_env_mapping = queryRealTimeDeployMsg(app, caasModels, caas_env); // CaaS平台的部署信息
        Map scc_model_env_mapping = queryRealTimeDeployMsg(app, sccModels, scc_env); // SCC平台的部署信息

        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put(Dict.APPID, app.get(Dict.ID));
        appInfo.put(Dict.APP_NAME_EN, app.get(Dict.NAME_EN));
        data.put(Dict.APP_NAME_EN, app.get(Dict.NAME_EN));
        data.put(Dict.APPINFO, appInfo);
        data.put(Dict.MODELSETMSG, modelSetMsg);
        data.put(Dict.ENVTESTLIST, test_env);
        data.put(Dict.ENVPROLIST, pro_env);
        data.put(Dict.ENVSCCLIST, scc_env);
        data.put(Dict.CAAS_MODEL_ENV_MAPPING, caas_model_env_mapping);
        data.put(Dict.SCC_MODEL_ENV_MAPPING, scc_model_env_mapping);
        return data;
    }

    /**
     * @param app
     * @param models
     * @param envs
     * @return 根据应用、实体、环境查询应用实时的部署信息
     * @throws Exception
     */

    private Map queryRealTimeDeployMsg(Map app, List<Model> models, Set<Environment> envs) throws Exception {
        List<Map> maps = new ArrayList<Map>();
        if (envs.size() >= 1) {
            for (Environment environment : envs) {

                if (models.size() >= 1) {
                    for (Model model : models) {
                        List all_pvc_value = new ArrayList();
                        ModelEnv modelEnv = new ModelEnv();
                        modelEnv.setEnv_id(environment.getId());
                        modelEnv.setModel_id(model.getId());
                        ModelEnv modelEnv1 = iModelEnvService.queryByEnvIdAndModelId(modelEnv);
                        AppOwnField appOwnField = new AppOwnField();
                        appOwnField.setApp_Id((String) app.get(Dict.ID));
                        appOwnField.setEnv_id(environment.getId());
                        AppOwnField appOwnField1 = appOwnFieldService.queryByAppIdAndEnvId(appOwnField);
                        List<Map> modelFleld_value = new ArrayList<>();
                        if (appOwnField1 != null) {
                            modelFleld_value = appOwnField1.getModelFleld_value();
                        }
                        Map jsonObject = JSONObject.fromObject(model);
                        jsonObject.remove("_id");
                        List<Object> oldEnv_key = (List<Object>) jsonObject.remove(Dict.ENV_KEY);
                        List<Object> newEnv_key = new ArrayList<>();
                        for (Object envKey : oldEnv_key) {
                            Map<String, Object> envKeyMap = (Map<String, Object>) envKey;
                            String json_schema_id = (String) envKeyMap.get(Dict.JSON_SCHEMA_ID);
                            JsonSchema jsonSchema = new JsonSchema();
                            if (!CommonUtils.isNullOrEmpty(json_schema_id)) {
                                jsonSchema = jsonSchemaService.getJsonSchema(json_schema_id);
                            }
                            String id = (String) envKeyMap.get(Dict.ID);
                            if (modelEnv1 != null) {
                                Map variables = modelEnv1.getVariables();
                                if (!CommonUtils.isNullOrEmpty(jsonSchema.getTitle())) {
                                    if (jsonSchema.getTitle().equals("pvc")
                                            && !CommonUtils.isNullOrEmpty(variables.get(id))) {
                                        List o = (List) variables.get(id);
                                        for (Object o1 : o) {
                                            Map<String, Object> map = (Map<String, Object>) o1;
                                            map.put(Dict.MOUNT_PATH, "");
                                            map.put(Dict.SUB_PATH, "");
                                            all_pvc_value.add(map);
                                        }
                                    }
                                }
                            }
                        }
                        for (Object envKey : oldEnv_key) {
                            Map<String, Object> envKeyMap = (Map<String, Object>) envKey;
                            String name_en = (String) envKeyMap.get(Dict.NAME_EN);
                            String id = (String) envKeyMap.get(Dict.ID);
                            String type = (String) envKeyMap.get(Dict.TYPE);
                            String data_type = (String) envKeyMap.get("data_type");
                            JsonSchema jsonSchema = new JsonSchema();
                            String json_schema_id = (String) envKeyMap.get(Dict.JSON_SCHEMA_ID);
                            if (!CommonUtils.isNullOrEmpty(json_schema_id)) {
                                jsonSchema = jsonSchemaService.getJsonSchema(json_schema_id);
                                if (!CommonUtils.isNullOrEmpty(jsonSchema.getId())) {
                                    envKeyMap.put(Dict.JSON_SCHEMA, jsonSchema);
                                }
                            }
                            if(envKeyMap.get(Dict.PROP_KEY)!=null&&("null".equals(envKeyMap.get(Dict.PROP_KEY).toString()))){
                                envKeyMap.put(Dict.PROP_KEY, "");
                            }
                            if (modelEnv1 != null) {
                                Map variables = modelEnv1.getVariables();
                                if (!CommonUtils.isNullOrEmpty(variables.get(id))) {
                                    envKeyMap.put(Dict.VALUE, variables.get(id));
                                    envKeyMap.put(Dict.ALL_VALUE, variables.get(id));
                                } else {
                                    envKeyMap.put(Dict.VALUE, "");
                                    envKeyMap.put(Dict.ALL_VALUE, "");
                                }
                                newEnv_key.add(envKeyMap);
                            } else {
                                newEnv_key.add(envKeyMap);
                            }
                            if (!CommonUtils.isNullOrEmpty(type) && "1".equals(type)) {
                                String value2 = "";
                                List<String> name = new ArrayList<>();
                                if (!CommonUtils.isNullOrEmpty(jsonSchema.getTitle())
                                        && jsonSchema.getTitle().equals("pvc")) {
                                    envKeyMap.put(Dict.HAVE_PATH, "1");
                                }
                                if (modelFleld_value.size() >= 1) {
                                    for (Map map : modelFleld_value) {
                                        String modelField = (String) map.get(Dict.MODEL_FIELD);
                                        String[] split = modelField.split("\\.");
                                        name.add(split[1]);
                                        // 非高级应用独立值
                                        if (model.getName_en().equals(split[0]) && name_en.equals(split[1])
                                                && CommonUtils.isNullOrEmpty(json_schema_id)) {
                                            value2 = (String) map.get(Dict.VALUE);
                                            envKeyMap.put(Dict.OWN_VALUE, value2);
                                        }
                                        // 非类型为数组的高级应用独立值
                                        if (model.getName_en().equals(split[0]) && name_en.equals(split[1])
                                                && !CommonUtils.isNullOrEmpty(json_schema_id)
                                                && !"array".equals(data_type)) {
                                            Object value = map.get(Dict.VALUE);
                                            envKeyMap.put(Dict.OWN_VALUE, value);
                                        }
                                        // 类型为数组的应用独立值
                                        if (model.getName_en().equals(split[0]) && name_en.equals(split[1])
                                                && !CommonUtils.isNullOrEmpty(json_schema_id)
                                                && "array".equals(data_type)) {
                                            if ("pvc".equals(jsonSchema.getTitle())) {
                                                envKeyMap.put(Dict.HAVE_PATH, "1");
                                            }
                                            List values = (List) map.get(Dict.VALUE);
                                            List own_values = new ArrayList();
                                            for (Object value : values) {
                                                Map<String, Object> value1 = (Map<String, Object>) value;
                                                String name_en1 = (String) value1.get(Dict.NAME_EN);
                                                if (modelEnv1 != null) {
                                                    for (Object o : all_pvc_value) {
                                                        Map<String, Object> o1 = (Map<String, Object>) o;
                                                        if (o1.get(Dict.NAME_EN).equals(name_en1)) {
                                                            if (!CommonUtils
                                                                    .isNullOrEmpty(value1.get(Dict.MOUNT_PATH))) {
                                                                o1.put(Dict.MOUNT_PATH, value1.get(Dict.MOUNT_PATH));
                                                            }
                                                            if (!CommonUtils.isNullOrEmpty(value1.get(Dict.SUB_PATH))) {
                                                                o1.put(Dict.SUB_PATH, value1.get(Dict.SUB_PATH));
                                                            }
                                                            own_values.add(o1);
                                                        }
                                                    }
                                                }
                                            }
                                            envKeyMap.put(Dict.OWN_VALUE, own_values);
                                            JSONArray value1 = (JSONArray) envKeyMap.get(Dict.VALUE);
                                            List<Map> value = com.alibaba.fastjson.JSONObject
                                                    .parseArray(value1.toString(), Map.class);
                                            Iterator<Map> iterator = value.iterator();
                                            while (iterator.hasNext()) {
                                                Map map1 = iterator.next();
                                                for (Object own_value : own_values) {
                                                    Map<String, Object> map2 = (Map<String, Object>) own_value;
                                                    if (map1.get(Dict.NAME_EN).equals(map2.get(Dict.NAME_EN))) {
                                                        iterator.remove();
                                                    }
                                                }
                                            }
                                            List<Map> all_value = new ArrayList<>();
                                            all_value.addAll(value);
                                            all_value.addAll(own_values);
                                            envKeyMap.put(Dict.ALL_VALUE, all_value);
                                        }

                                    }
                                } else {
                                    envKeyMap.put(Dict.OWN_VALUE, value2);
                                }
                                if (appOwnField1 == null) {
                                    envKeyMap.put(Dict.OWN_VALUE, "");
                                }
                                if (!name.contains(envKeyMap.get(Dict.NAME_EN))) {
                                    envKeyMap.put(Dict.OWN_VALUE, "");
                                }
                            }

                        }
                        jsonObject.put(Dict.ENV_KEY, newEnv_key);
                        jsonObject.put(Dict.ENV, environment.getName_cn());
                        jsonObject.put(Dict.LABELS, environment.getLabels());
                        maps.add(jsonObject);
                    }
                }
            }
        }
        // 根据环境标签排序
        List<Map> sortedMaps = CommonUtils.compareByEnvSourt(maps, envSort);
        List<String> set = new ArrayList<>();
        Map model_env_mapping = new LinkedHashMap();
        for (Map map : sortedMaps) {
            set.add((String) map.get(Dict.ENV));
        }
        List<String> setDistinct = set.stream().distinct().collect(Collectors.toList());
        for (String s : setDistinct) {
            List<Map> maps1 = new ArrayList<>();
            for (Map map : maps) {
                if (!CommonUtils.isNullOrEmpty(map.get(Dict.ENV))) {
                    if (s.equals(map.get(Dict.ENV))) {
                        maps1.add(map);
                    }
                }
            }
            model_env_mapping.put(s, maps1);
        }
        return model_env_mapping;
    }

    @Override
    public Object definedDeploy(Map requestParam) throws Exception {
        String appId = (String) requestParam.get(Dict.APP_ID);
        //通过应用id获取对应的gitlabId
        Map<String, String> reqParam = new HashMap<>();
        reqParam.put(Dict.ID, appId);
        Object result = iRequestService.getProjectById(reqParam);
        String gitlabId = String.valueOf(JSONObject.fromObject(result).get(Dict.ID));
//        Map responseMap = (Map) this.restTemplate.postForObject(getProjectById, reqParam, Map.class, new Object[0]);
//        String gitlabId = (String) JSONObject.fromObject(responseMap.get(Dict.DATA)).get(Dict.ID);
        //自定义参数入库
        String ref = (String) requestParam.get(Dict.REF);
        String editConfigFlag = (String) requestParam.get(Dict.CONFIG_UPDATE_FLAG);
        String deployFlag = (String) requestParam.get(Dict.RE_DEPLOY_FLAG);
        Map variables = (Map) requestParam.get(Dict.VARIABLES);
        DefinedVariables definedVariables = new DefinedVariables();
        definedVariables.setAppId(appId);
        definedVariables.setConfigUpdateFlag(editConfigFlag);
        definedVariables.setReDeployFlag(deployFlag);
        definedVariables.setRef(ref);
        definedVariables.setVariables(variables);
        DefinedVariables var = definedVariablesDao.save(definedVariables);
        //将definedDeployId作为参数传给helper
        String _id = var.get_id().toString();
        List variable = new ArrayList();
        Map map = new HashMap();
        map.put(Dict.KEY, "definedDeployId");
        map.put(Dict.VALUE, _id);
        variable.add(map);

        // 拆分 variables参数 将其重新组装为map再加入到list中
        Map<String, Object> param = new HashMap<String, Object>();
        if (!CommonUtils.isNullOrEmpty(variable)) {
            param.put(Dict.VARIABLES, variable);
        }
        //发送gitlab接口触发流水线
        String pipeline_url = url + "projects" + "/" + gitlabId + "/pipeline";
        param.put(Dict.ID, gitlabId);
        param.put(Dict.PRIVATE_TOKEN_L, gitlab_token);
        param.put(Dict.REF, ref);
        return this.gitlabTransport.submitPost(pipeline_url, param);

    }

    @Override
    public Map queryVariablesById(Map requestMap) {
        String definedDeployId = (String) requestMap.get(Dict.DEFINED_DEPLOY_ID);
        DefinedVariables definedVariables = this.definedVariablesDao.queryDefinedVariables(definedDeployId);
        String varstr = com.alibaba.fastjson.JSONObject.toJSONString(definedVariables);
        Map variables = com.alibaba.fastjson.JSONObject.parseObject(varstr,Map.class);
        return variables;
    }


}
