package com.spdb.fdev.release.service.impl;

import com.csii.pe.redis.util.Util;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.dao.IProdRecordDao;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.swing.text.html.parser.Entity;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProdApplicationServiceImpl implements IProdApplicationService {

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IProdApplicationDao prodApplicationDao;
    @Autowired
    private IAppService appService;
    @Autowired
    private IProdRecordService prodRecordService;
    @Autowired
    private IProdTemplateService prodTemplateService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IRelDevopsRecordService relDevopsRecordService;
    @Autowired
    IGitlabService gitlabService;
    @Autowired
    private IProdRecordDao prodRecordDao;
    @Autowired
    private IAutomationEnvService automationEnvService;
    @Value("${fdev.biz.mapping}")
    private String fdevBiz;
    @Value("${fdev.dmz.mapping}")
    private String fdevDmz;
    @Value("${gitlab.manager.token}")
    private String Token;

    private Logger logger = LoggerFactory.getLogger(ProdApplicationServiceImpl.class);

    @Override
    public ProdApplication queryApplication(String prod_id, String application_id) throws Exception {
        return prodApplicationDao.queryApplication(prod_id, application_id);
    }

    @Override
    public List<Map> queryApplications(ProdApplication prodApp) throws Exception {
        List<ProdApplication> application_list = prodApplicationDao.queryApplications(prodApp);
        ProdRecord prodRecord = prodRecordService.queryDetail(prodApp.getProd_id());
        String image_deliver_type = prodRecord.getImage_deliver_type();
        List<Map> list = new ArrayList<>();
        for (ProdApplication prodApplication : application_list) {
            Map<String, Object> application = appService.queryAPPbyid(prodApplication.getApplication_id());
            if (CommonUtils.isNullOrEmpty(application)) {
                continue;
            }
            Map<String, Object> result = new HashMap<>();
            result.put(Dict.CAAS_STATUS, application.get(Dict.CAAS_STATUS));
            result.put(Dict.SCC_STATUS, application.get(Dict.SCC_STATUS));
            RelDevopsRecord relDevopsRecord1 = relDevopsRecordService.queryProTagByPackage(prodApplication.getPro_package_uri());
            if (!CommonUtils.isNullOrEmpty(relDevopsRecord1)) {
                result.put(Dict.PRODUCT_TAG, relDevopsRecord1.getProduct_tag());
            } else {
                result.put(Dict.PRODUCT_TAG, "");
            }
            result.put(Dict.PRO_PACKAGE_URI, prodApplication.getPro_package_uri());
            result.put(Dict.PROD_ID, prodApp.getProd_id());
            result.put(Dict.APP_NAME_EN, CommonUtils.isNullOrEmpty(application) ? "" : application.get(Dict.NAME_EN));
            result.put(Dict.APP_NAME_ZH, CommonUtils.isNullOrEmpty(application) ? "" : application.get(Dict.NAME_ZH));
            // 应用类型
            result.put(Dict.TYPE_NAME, CommonUtils.isNullOrEmpty(application) ? "" : application.get(Dict.TYPE_NAME));
            if (!CommonUtils.isNullOrEmpty(application.get(Dict.GITLAB_PROJECT_ID))) {
                result.put(Dict.GITLAB_PROJECT_ID, application.get(Dict.GITLAB_PROJECT_ID));
            } else {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用" + application.get(Dict.NAME_ZH) + "未配置gitlabId"});
            }
            String network = (String) application.get(Dict.NETWORK);
            if (!CommonUtils.isNullOrEmpty(network) && !network.contains("dmz")) {
                result.put(Dict.NETWORK, "biz");
            } else {
                result.put(Dict.NETWORK, "dmz");
            }
            //查询应用行内负责人信息
            List<Map<String, String>> appSpdbManager = userService.queryAppSpdbManager(prodApplication.getApplication_id());
            result.put(Dict.APP_SPDB_MANAGERS, appSpdbManager);
            //查询应用厂商负责人信息
            List<Map<String, String>> appDevManager = userService.queryAppDevManager(prodApplication.getApplication_id());
            result.put(Dict.APP_DEV_MANAGERS, appDevManager);
            String template_id = prodApplication.getTemplate_id();
            if (CommonUtils.isNullOrEmpty(template_id) && Constants.IMAGE_DELIVER_TYPE_NEW.equals(image_deliver_type)) {
                if (CommonUtils.isNullOrEmpty(prodRecord) || CommonUtils.isNullOrEmpty(prodRecord.getTemplate_id())) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"未设置变更模版"});
                }
                template_id = prodRecord.getTemplate_id();
            }

            result.put(Dict.TEMPLATE_ID, template_id);
            ProdTemplate prodTemplate = new ProdTemplate();
            prodTemplate.setId(template_id);
            prodTemplate = prodTemplateService.queryDetail(prodTemplate);
            if (CommonUtils.isNullOrEmpty(prodTemplate)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"未设置变更模版"});
            }
            // 应用变更时，目录根据应用变更类型判断
            /*if (Constants.APPLICATION_DOCKER_ALL.equals(prodApplication.getRelease_type())) {
                // K1、K2镜像同步更新
                result.put(Dict.ASSET_CATALOG, Dict.DOCKER_ALL);
            } else if (Constants.APPLICATION_DOCKER_STOP_START_ALL.equals(prodApplication.getRelease_type())) {
                RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
                relDevopsRecord.setApplication_id(prodApplication.getApplication_id());
                relDevopsRecord.setRelease_node_name(prodRecord.getRelease_node_name());
                boolean docker = relDevopsRecordService.queryDockerDir(relDevopsRecord, prodRecord, prodApplication.getPro_image_uri());
                if (docker) {
                    // 停止后更新再启动
                    result.put(Dict.ASSET_CATALOG, Dict.DOCKER_STOP_DOCKER_START_ALL);
                } else {
                    // 停止后重启
                    result.put(Dict.ASSET_CATALOG, Dict.DOCKER_STOP_START_ALL);
                }
            } else if (Constants.APPLICATION_DOCKER_RESTART.equals(prodApplication.getRelease_type())) {
                // 服务重启
                result.put(Dict.ASSET_CATALOG, Dict.DOCKER_RESTART);
            } else {
                // 微服务镜像异步更新
                result.put(Dict.ASSET_CATALOG, Dict.DOCKER_LOWERCASE);
            }*/
            result.put("caas_add_sign", prodApplication.getCaas_add_sign());
            result.put("scc_add_sign", prodApplication.getScc_add_sign());
            /*if ("1".equals(prodApplication.getAdd_sign())) {
                // 若为新增应用，一律替换为docker_yaml目录
                result.put(Dict.ASSET_CATALOG, Dict.DOCKER_YAML);
            }*/
            result.put(Dict.PROD_DIR, prodApplication.getProd_dir());
            result.put(Dict.CHANGE, prodApplication.getChange());
            result.put(Dict.TEMP_NAME, prodTemplate.getTemp_name());
            result.put(Dict.APPLICATION_ID, prodApplication.getApplication_id());
            result.put(Dict.PRO_IMAGE_URI, prodApplication.getPro_image_uri());
            result.put(Dict.PRO_SCC_IMAGE_URI, prodApplication.getPro_scc_image_uri());
            result.put(Dict.RELEASE_TYPE, prodApplication.getRelease_type());
            result.put(Dict.CONTAINER_NUM, prodApplication.getContainer_num());
            result.put(Dict.DEPLOY_TYPE, prodApplication.getDeploy_type());
            result.put(Dict.FAKE_IMAGE_URI, !CommonUtils.isNullOrEmpty(prodApplication.getFake_image_uri())
                    ? prodApplication.getFake_image_uri() : "");
            // 配置文件变化与审核状态
            if (!CommonUtils.isNullOrEmpty(prodApplication.isFdev_config_changed())) {
                result.put(Dict.FDEV_CONFIG_CONFIRM, prodApplication.getFdev_config_confirm());
                result.put(Dict.FDEV_CONFIG_CHANGED, prodApplication.isFdev_config_changed());
                result.put(Dict.COMPARE_URL, prodApplication.getCompare_url());
            }
            //查询当前应用是否存在当前时间之后的最近一次变更
            List<Map<String, Object>> apps = prodRecordDao.queryRiskProd(prodApplication.getApplication_id(), prodRecord.getDate());
            if (!CommonUtils.isNullOrEmpty(apps)) {
                Map map = new HashMap();
                ProdRecord prod = (ProdRecord) apps.get(0);
                map.put(Dict.VERSION, prod.getVersion());
                map.put(Dict.DATE, prod.getDate());
                //查询到数据,设置风险标识,并将涉及变更数据返回
                result.put(Dict.ISAPPRISK, "1");
                result.put(Dict.RISKPROD, map);
            }
            result.put(Dict.TAG, prodApplication.getTag());
            result.put("esf_flag", prodApplication.getEsf_flag());
            result.put("esf_platform", prodApplication.getEsf_platform());
            result.put("caas_stop_env", CommonUtils.isNullOrEmpty(prodApplication.getCaas_stop_env())?new ArrayList<>():prodApplication.getCaas_stop_env());
            result.put("scc_stop_env", CommonUtils.isNullOrEmpty(prodApplication.getScc_stop_env())?new ArrayList<>():prodApplication.getScc_stop_env());
            list.add(result);
        }
        return list;
    }

    @Override
    public ProdApplication addApplication(ProdApplication prodApplication) throws Exception {
        Map<String, Object> application = appService.queryAPPbyid(prodApplication.getApplication_id());
        if (CommonUtils.isNullOrEmpty(application)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"app not exits"});
        }
        return prodApplicationDao.addApplication(prodApplication);
    }

    @Override
    public void deleteApplication(String prod_id, String application_id) throws Exception {
        prodApplicationDao.deleteApplication(prod_id, application_id);
    }

    @Override
    public ProdApplication setApplicationTemplate(String prod_id, String application_id, String template_id)
            throws Exception {
        return prodApplicationDao.setApplicationTemplate(prod_id, application_id, template_id);
    }

    @Override
    public ProdApplication setImageUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri, String fake_image_uri) {
        return prodApplicationDao.setImageUri(prod_id, application_id, pro_image_uri, pro_scc_image_uri, fake_image_uri);
    }

    @Override
    public void deleteApplicationByNode(String release_node_name, String application_id) throws Exception {
        prodApplicationDao.deleteApplicationByNode(release_node_name, application_id);
    }

    @Override
    public List<ReleaseApplication> queryAppWithOutSum(String prod_assets_version, String release_node_name) throws Exception {
        return prodApplicationDao.queryAppWithOutSum(prod_assets_version, release_node_name);
    }

    @Override
    public Set<ProdApplication> queryAppBySum(String prod_assets_version) throws Exception {
        return prodApplicationDao.queryAppBySum(prod_assets_version);
    }

    @Override
    public void deleteAppByProd(String prod_id) throws Exception {
        prodApplicationDao.deleteAppByProd(prod_id);
    }

    @Override
    public ProdApplication updateApplication(ProdApplication prodApplication) {
        return prodApplicationDao.updateApplication(prodApplication);
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "frelease.envinfo.{gitlabId}")
    public Map<String, Object> findConfigByGitlab(String gitlabId) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.GITLABID, gitlabId);// 发fdev-env-config模块获取应用绑定部署实体配置
        send_map.put(Dict.REST_CODE, "queryByGitlabId");
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    @Override
    public Map<String, Set<String>> queryVarByLabelAndType(Integer gitlabId) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.GITLABID, gitlabId);
        send_map.put(Dict.LABEL, "pro");
        send_map.put(Dict.TYPE, "deploy");
        send_map.put(Dict.REST_CODE, "queryVarByLabelAndType");
        Map<String, Object> result = (Map<String, Object>) restTransport.submit(send_map);
        Set<String> namaspaces = new HashSet<>();
        Set<String> sccNamaspaces = new HashSet<>();
        for (String key : result.keySet()) {
            for (Map<String, Object> map : (List<Map<String, Object>>) result.get(key)) {
                if ("FDEV_CAAS_REGISTRY_NAMESPACE".equals(map.get(Dict.KEY))) {
                    if (!CommonUtils.isNullOrEmpty(map.get(Dict.VALUE))) {
                        namaspaces.add((String) map.get(Dict.VALUE));
                    }
                }
                if ("dockerservice_namespace".equals(map.get(Dict.KEY))) {
                    if (!CommonUtils.isNullOrEmpty(map.get(Dict.VALUE))) {
                        sccNamaspaces.add((String) map.get(Dict.VALUE));
                    }
                }
            }
        }
        Map<String, Set<String>> resultMap = new HashMap<>();
        resultMap.put(Dict.CAAS, namaspaces);
        resultMap.put(Dict.SCC, sccNamaspaces);
        return resultMap;
    }

    @Override
    public String findLatestUri(String application_id, ProdRecord prodRecord, String deployType) {
        return prodApplicationDao.findLatestUri(application_id, prodRecord, deployType);
    }

    @Override
    public String findLatestTag(String application_id, String deployType) {
        return prodApplicationDao.findLatestTag(application_id, deployType);
    }

    @Override
    public String findLastReleaseTag(String application_id, ProdRecord prodRecord, String deployType) {
        return prodApplicationDao.findLastReleaseTag(application_id, prodRecord, deployType);
    }

    @Override
    public ProdApplication setConfigUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri, boolean fdev_config_changed, String compare_url, String fdev_config_confirm, String new_tag) {
        return prodApplicationDao.setConfigUri(prod_id, application_id, pro_image_uri, pro_scc_image_uri, fdev_config_changed,
                compare_url, fdev_config_confirm, new_tag);
    }

    @Override
    public String findLastReleaseUri(String application_id, ProdRecord prodRecord, String deployType) {
        return prodApplicationDao.findLastReleaseUri(application_id, prodRecord, deployType);
    }

    @Override
    public List<ProdApplication> findUriByApplicationId(String application_id) {
        return prodApplicationDao.findUriByApplicationId(application_id);
    }

    @Override
    public String findLastTagByApplicationId(String application_id) {
        return prodApplicationDao.findLastTagByApplicationId(application_id);
    }

    @Override
    public ProdApplication setImageConfigUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri,
                                             boolean fdev_config_changed, String compare_url,
                                             String fdev_config_confirm, String fake_image_uri, String tag) {
        return prodApplicationDao.setImageConfigUri(prod_id, application_id, pro_image_uri, pro_scc_image_uri, fdev_config_changed,
                compare_url, fdev_config_confirm, fake_image_uri, tag);
    }

    @Override
    public ProdApplication setImageConfigConfirmUri(String prod_id, String application_id, String pro_image_uri, String pro_scc_image_uri,
                                                    String fdev_config_confirm, String fake_image_uri) {
        return prodApplicationDao.setImageConfigConfirmUri(prod_id, application_id, pro_image_uri, pro_scc_image_uri,
                fdev_config_confirm, fake_image_uri);
    }

    @Override
    public List<Map<String, Object>> queryApplicationsNoException(String prod_id) throws Exception {
        List<ProdApplication> application_list = prodApplicationDao.queryApplications(new ProdApplication(prod_id));
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        List<Map<String, Object>> list = new ArrayList<>();
        for (ProdApplication prodApplication : application_list) {
            Map<String, Object> application = appService.queryAPPbyid(prodApplication.getApplication_id());
            if (CommonUtils.isNullOrEmpty(application)) {
                continue;
            }
            Map<String, Object> result = new HashMap<>();
            result.put(Dict.PROD_ID, prod_id);
            result.put(Dict.APP_NAME_EN, CommonUtils.isNullOrEmpty(application) ? "" : application.get(Dict.NAME_EN));
            result.put(Dict.APP_NAME_ZH, CommonUtils.isNullOrEmpty(application) ? "" : application.get(Dict.NAME_ZH));
            if (!CommonUtils.isNullOrEmpty(application.get(Dict.GITLAB_PROJECT_ID))) {
                result.put(Dict.GITLAB_PROJECT_ID, application.get(Dict.GITLAB_PROJECT_ID));
            } else {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用" + application.get(Dict.NAME_ZH) + "未配置gitlabId"});
            }
            //查询应用行内负责人信息
            List<Map<String, String>> appSpdbManager = userService.queryAppSpdbManager(prodApplication.getApplication_id());
            result.put(Dict.APP_SPDB_MANAGERS, appSpdbManager);
            //查询应用厂商负责人信息
            List<Map<String, String>> appDevManager = userService.queryAppDevManager(prodApplication.getApplication_id());
            result.put(Dict.APP_DEV_MANAGERS, appDevManager);

            result.put(Dict.TEMPLATE_ID, prodRecord.getTemplate_id());
            ProdTemplate prodTemplate = prodTemplateService.queryDetailById(prodRecord.getTemplate_id());
            result.put(Dict.TEMP_NAME, !CommonUtils.isNullOrEmpty(prodTemplate) ? prodTemplate.getTemp_name() : "");
            // 应用变更时，目录根据应用变更类型判断
            if (Constants.APPLICATION_DOCKER_ALL.equals(prodApplication.getRelease_type())) {
                // K1、K2镜像同步更新
                result.put(Dict.ASSET_CATALOG, Dict.DOCKER_ALL);
            } else if (Constants.APPLICATION_DOCKER_STOP_START_ALL.equals(prodApplication.getRelease_type())) {
                // 停止应用后重启
                result.put(Dict.ASSET_CATALOG, Dict.DOCKER_STOP_START_ALL);
            } else {
                // 微服务镜像异步更新
                result.put(Dict.ASSET_CATALOG, Dict.DOCKER_LOWERCASE);
            }
            result.put(Dict.APPLICATION_ID, prodApplication.getApplication_id());
            result.put(Dict.PRO_IMAGE_URI, prodApplication.getPro_image_uri());
            result.put(Dict.RELEASE_TYPE, prodApplication.getRelease_type());
            result.put(Dict.CONTAINER_NUM, prodApplication.getContainer_num());
            result.put(Dict.DEPLOY_TYPE, prodApplication.getDeploy_type());

            list.add(result);
        }
        return list;
    }

    @Override
    public String checkEnvConfig(Map<String, Object> configGitlab, String newTag) {
        if (CommonUtils.isNullOrEmpty(newTag)) {
            throw new FdevException(ErrorConstants.NO_TAG);
        }
        boolean isEvnConfig = !CommonUtils.isNullOrEmpty(configGitlab) && !CommonUtils.isNullOrEmpty(configGitlab.get(Dict.CONFIGGITLABID));
        if (isEvnConfig) {
            // 实体配置的gitlab的id
            String configGitlabId = String.valueOf(configGitlab.get(Dict.CONFIGGITLABID));
            if (newTag.contains("-esf")) {
                return configGitlabId;
            }
            if (!CommonUtils.isNullOrEmpty(configGitlab.get("tagInfo"))) {
                List<Map<String, String>> tagInfo = (List<Map<String, String>>) configGitlab.get("tagInfo");
                for (Map<String, String> tag : tagInfo) {
                    if (newTag.equals(tag.get("tag_name"))) {
                        return configGitlabId;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String queryLastTagByGitlabId(String application_id, String prod_id, String type,String release_node_name) {
        return prodApplicationDao.queryLastTagByGitlabId(application_id, prod_id, type, release_node_name);
    }

    @Override
    public List<ProdApplication> queryByProdId(String prod_id) {
        return prodApplicationDao.queryByProdId(prod_id);
    }

    @Override
    public List<ProdApplication> queryByApplicationId(String application_id) {
        return prodApplicationDao.queryByApplicationId(application_id);
    }

    @Override
    public ProdApplication setPackageTag(String prod_id, String application_id, String pro_package_uri) {
        return prodApplicationDao.setPackageTag(prod_id, application_id, pro_package_uri);
    }

    @Override
    public void updateProdDir(Map<String, Object> requestParam) throws Exception {
        // 获取前端上送参数
        String prd_id = (String) requestParam.get(Dict.PROD_ID);
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        String release_type = (String) requestParam.get(Dict.RELEASE_TYPE);
        List<String> prodDirList = (List<String>) requestParam.get(Dict.PROD_DIR);
        Map<String,Object> change = (Map<String, Object>) requestParam.get(Dict.CHANGE);
        ProdApplication prodApplication = queryApplication(prd_id, application_id);
        ProdRecord prodRecord = prodRecordDao.queryDetail(prd_id);
        // 获取修改完介质目录后最终的部署平台
        Set<String> deploy_type = new HashSet<>();
        for (String prod_dir : prodDirList) {
            if(prod_dir.startsWith(Dict.DOCKER_LOWERCASE)){
                deploy_type.add("CAAS");
            }
            if(prod_dir.startsWith(Dict.SCC)){
                deploy_type.add("SCC");
            }
        }
        // 变更类型为停止后重启或重启时，需要该应用曾经部署过该平台才可以添加变更应用卡片成功
        HashSet<String> hasDeployedType = prodApplicationDao.findAllDeployedType(application_id, prodRecord);
        deploy_type.forEach(deploy -> {
            if (Constants.SERVICE_SYN_UPDATE.equals(release_type) && !hasDeployedType.contains(deploy)) {
                throw new FdevException(ErrorConstants.DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行单集群停止操作，修改失败！"});
            }
            if (Constants.SERVICE_STOP_AND_START.equals(release_type) && !hasDeployedType.contains(deploy)) {
                throw new FdevException(ErrorConstants.DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行停止后启动操作，修改失败！"});
            }
            if (Constants.SERVICE_RESTART.equals(release_type) && !hasDeployedType.contains(deploy)) {
                throw new FdevException(ErrorConstants.DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行重启操作，修改失败！"});
            }
        });

        // 若修改后介质目录包含docker_stop/scc_stop，则修改caas_stop_env或scc_stop_env字段
        List<String> caas_stop_env = prodApplication.getCaas_stop_env();
        List<String> scc_stop_env = prodApplication.getScc_stop_env();
        if(prodDirList.contains(Dict.DOCKER_STOP)){
            caas_stop_env = (List<String>) requestParam.get("caas_stop_env");
        }
        if(prodDirList.contains(Dict.SCC_STOP)){
            scc_stop_env = (List<String>) requestParam.get("scc_stop_env");
        }
        prodApplicationDao.updateProdStopEnv(prd_id, application_id,caas_stop_env, scc_stop_env);

        // 修改介质目录若包含docker_yaml,scc_yaml,docker_startall,scc_startall需要发蓝鲸获取上次投产的生产信息,更新gitlab上master分支并拉取新tag，同时保存check_XXX到change字段

        Map<String, Object> changeMap = prodApplication.getChange();
        if(CommonUtils.isNullOrEmpty(changeMap)){
            changeMap = new HashMap<>();
        }
        // 合并两个map
        Map<String,Object> allMap = new HashMap<>(changeMap);
        // key相同的，value求并集
        for(Map.Entry<String,Object> entry: changeMap.entrySet()){
            for(Map.Entry<String,Object> entry1: change.entrySet()){
                if(entry.getKey().equals(entry1.getKey())){
                    Map<String,Object> envMap = (Map<String, Object>) entry.getValue();
                    envMap.putAll((Map<String,Object>)entry1.getValue());
                    allMap.put(entry.getKey(),envMap);
                }
            }
        }
        // 求用户上送的change - 库里的change 差集
        List<String> strRemove = new ArrayList<>();
        Map<String,Object> res = new HashMap<>(change);
        for(String s : res.keySet()){
            if(changeMap.containsKey(s)){
                strRemove.add(s);
            }
        }
        for(String s: strRemove){
            res.remove(s);
        }
        logger.info("用户上送的change与库里的差集：" + res.toString());
        allMap.putAll(res);

        logger.info("合并后的map:" + allMap.toString());

        // 在判断此次修改后的介质目录是否包含docker_yaml或scc_yaml
        if(!prodDirList.contains(Dict.DOCKER_YAML) && !prodDirList.contains(Dict.SCC_YAML)){
            updateProdChange(prodRecord.getProd_id(), application_id, allMap);
        }else{
            if (prodDirList.contains(Dict.DOCKER_YAML)) {
                if (!"1".equals(prodApplication.getCaas_add_sign())) { // 已投产发蓝鲸
                    String pro_image_uri = prodApplication.getPro_image_uri();

                    if(CommonUtils.isNullOrEmpty(pro_image_uri)){
                        pro_image_uri = prodApplicationDao.findLastReleaseUri(application_id, prodRecord, "CAAS");
                    }
                    getCaasNewConfig(prodDirList, application_id, prodRecord, prodApplication, pro_image_uri, allMap);
                }
            }
            if (prodDirList.contains(Dict.SCC_YAML)) {
                if (!"1".equals(prodApplication.getScc_add_sign())) { // 已投产发蓝鲸
                    String pro_scc_image_uri = prodApplication.getPro_scc_image_uri();
                    if(CommonUtils.isNullOrEmpty(pro_scc_image_uri)){
                        pro_scc_image_uri = prodApplicationDao.findLastReleaseUri(application_id, prodRecord, "SCC");
                    }
                    getSccNewConfig(prodDirList, application_id, prodRecord, prodApplication, pro_scc_image_uri, allMap);
                }
            }
        }

        // 修改介质目录入库
        prodApplicationDao.updateProdDir(prd_id, application_id, prodDirList, new ArrayList<>(deploy_type));
    }

    public void getSccNewConfig(List<String> prodDirList, String application_id, ProdRecord prodRecord, ProdApplication prodApplication, String proSccImageUri, Map<String, Object> changeMap) throws Exception {
        String tag = proSccImageUri.split(":")[1];
        String deployName = proSccImageUri.split(":")[0].split("/")[2];
        //查询应用信息
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        //查询配置文件实体
        Map<String, Object> configGitlab = findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
        // 发蓝鲸获取scc的生产部属信息并更新gitlab上的yaml
        List<Map<String, Object>> scc_yamls;
        try {
            scc_yamls = appService.getSccNewYaml(deployName, tag);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{e.getMessage()});
        }
        if (!CommonUtils.isNullOrEmpty(scc_yamls)) {
            for (Map<String, Object> scc_map : scc_yamls) {
                String namespace = (String) scc_map.get("namespace_code"); //租户
                // 根据租户推出fdev环境并修改auto-config项目下yaml文件
                List<AutomationEnv> automationEnvList = automationEnvService.query();
                StringBuffer sb = new StringBuffer();
                boolean flag = false;
                for (AutomationEnv automationEnv : automationEnvList) {
                    Map<String, Object> scc_fdev_map = (Map<String, Object>) automationEnv.getScc_fdev_env_name().get(prodRecord.getType()); // scc的fdev环境
                    Map<String, Object> scc_namespace_map = (Map<String, Object>) automationEnv.getScc_namespace().get(prodRecord.getType()); // scc的租户
                    String scc_dmz_namespace = (String) scc_namespace_map.get("dmz"); // 网银网段对应的租户
                    String scc_biz_namespace = (String) scc_namespace_map.get("biz"); // 业务网段对应的租户
                    if (namespace.equals(scc_dmz_namespace)) {
                        flag = true;
                        sb.append(scc_fdev_map.get("dmz")); // 网银网段对应的fdev环境
                        break;
                    } else if (namespace.equals(scc_biz_namespace)) {
                        flag = true;
                        sb.append(scc_fdev_map.get("biz")); // 业务网段对应的fdev环境
                        break;
                    } else {
                        continue;
                    }
                }
                if (flag) {
                    sb.append("/").append(deployName.split("-")[0]).append("/").append(deployName).append("-scc.yaml");
                    logger.info("开始替换scc yaml文件，名称：{}", deployName + "/" + tag);
                    Yaml yaml = new Yaml();
                    String yamlStr = yaml.dumpAsMap(scc_map.get("yaml")).replaceAll("'######'", "");
                    appService.updateGitFile(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)),
                            URLEncoder.encode(sb.toString(), "UTF-8"), Dict.MASTER, yamlStr,
                            "替换tag包配置文件", Token);
                    logger.info("替换scc yaml文件成功");
                }
            }
            updateProdChange(prodRecord.getProd_id(), application_id, changeMap);
        }
        logger.info("开始删除tag，名称：{}", tag);
        gitlabService.deleteTag((Integer) configGitlab.get(Dict.CONFIGGITLABID), tag);
        logger.info("删除tag成功");

        logger.info("开始生成新的tag，名称：{}", tag);
        gitlabService.createTag(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)), tag, Dict.MASTER, Token);
        logger.info("生成新的tag成功");
    }

    public void getCaasNewConfig(List<String> prodDirList, String application_id, ProdRecord prodRecord, ProdApplication prodApplication, String proImageUri, Map<String, Object> changeMap) throws Exception {

        String tag = proImageUri.split(":")[1];
        String deployName = proImageUri.split(":")[0].split("/")[2];

        //查询应用信息
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        //查询配置文件实体
        Map<String, Object> configGitlab = findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
        List<Map<String, Object>> yamls;
        try {
            yamls = appService.getNewYaml(deployName, tag);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{e.getMessage()});
        }
        if (!CommonUtils.isNullOrEmpty(yamls)) {
            Map changeTemp = new HashMap();
            for (Map<String, Object> map : yamls) {
                String tenant = (String) map.get("cluster");
                String namespace = (String) map.get("namespace");
                String type = namespace.contains(Dict.GRAY) ? Dict.GRAY : "proc";
                if (type.equals(prodRecord.getType())) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(tenant, 0, 2).append("-").append(tenant, 2, 4);
                    List<String> bizs = Arrays.asList(fdevBiz.split(","));
                    List<String> dmzs = Arrays.asList(fdevDmz.split(","));
                    if (bizs.contains(namespace)) {
                        sb.append("-").append("biz");
                    } else if (dmzs.contains(namespace)) {
                        sb.append("-").append("dmz");
                    } else {
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{namespace + "数据异常"});
                    }
                    if (namespace.contains(Dict.GRAY)) {
                        sb.append("-").append(Dict.GRAY);
                    }
                    sb.append("/").append(deployName.split("-")[0]).append("/").append(deployName).append(".yaml");
                    logger.info("开始替换yaml文件，名称：{}", deployName + "/" + tag);
                    Yaml yaml = new Yaml();
                    String yamlStr = yaml.dumpAsMap(map.get("yaml")).replaceAll("'######'", "");
                    appService.updateGitFile(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)),
                            URLEncoder.encode(sb.toString(), "UTF-8"), Dict.MASTER, yamlStr,
                            "替换tag包配置文件", Token);
                    logger.info("替换yaml文件成功");
                    String key = "/PROC/" + tenant.toUpperCase();
                    Map<String, Object> change = (Map<String, Object>) map.get(Dict.CHANGE);
                    if (CommonUtils.isNullOrEmpty(change)) {
                        change = new HashMap<>();
                    }
                    if (!CommonUtils.isNullOrEmpty(changeMap)) {
                        Map<String, Object> envMap = (Map<String, Object>) changeMap.get(key);
                        if (!CommonUtils.isNullOrEmpty(envMap)) {
                            change.putAll(envMap);
                        }
                    }
                    changeMap.put(key, change);
                    changeTemp.putAll(change);
                }
            }
            // 介质目录包含docker_startall或scc_startall时，DEV,TEST,TCYZ获取到的是查询副本数上送的数据（逻辑是这三个环境默认副本数为1）
            if(!prodDirList.contains(Dict.DOCKER_STARTALL) && !prodDirList.contains(Dict.SCC_STARTALL)){
                changeMap.put("/DEV/DEV", changeTemp);
                changeMap.put("/TEST/TEST", changeTemp);
                changeMap.put("/TCYZ/TCYZ", changeTemp);
            }
            updateProdChange(prodRecord.getProd_id(), application_id, changeMap);
        }
        logger.info("开始删除tag，名称：{}", tag);
        gitlabService.deleteTag((Integer) configGitlab.get(Dict.CONFIGGITLABID), tag);
        logger.info("删除tag成功");

        logger.info("开始生成新的tag，名称：{}", tag);
        gitlabService.createTag(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)), tag, Dict.MASTER, Token);
        logger.info("生成新的tag成功");
    }

    @Override
    public Map<String, Object> queryReplicasnu(Map<String, Object> req) throws Exception {
        // 1.获取到deploy_name、tag
        String prod_id = (String) req.get(Dict.PROD_ID); //变更id
        String application_id = (String) req.get(Dict.APPLICATION_ID);
        List<String> deploy_type = (List<String>) req.get(Dict.DEPLOY_TYPE);
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        String tag = null;
        String deployName = null;
        Map<String, String> lastTagMap = new HashMap<>();
        for (String type : deploy_type) {
            String lastTag = findLastReleaseUri(application_id, prodRecord, type);
            if (!CommonUtils.isNullOrEmpty(lastTag)) {
                lastTagMap.put(type, lastTag);
                tag = lastTag.split(":")[1];
                deployName = lastTag.split(":")[0].split("/")[2];
            }
        }
        // 未查找到上次投产的镜像表示未曾投产，即无法对该应用进行弹性扩展
        if (CommonUtils.isNullOrEmpty(lastTagMap)) {
            throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行停止后启动操作！"});
        }
        // 查询该应用的部署网段
        Map<String,Object> result = new HashMap<>();
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        String network = (String) application.get(Dict.NETWORK);
        if (!CommonUtils.isNullOrEmpty(network) && !network.contains("dmz")) {
            result.put(Dict.NETWORK, "biz");
        } else {
            result.put(Dict.NETWORK, "dmz");
        }

        List<String> caas_content = new ArrayList<>();
        List<String> scc_content = new ArrayList<>();
        Map<String, Object> changeMap = new HashMap();
        if (deploy_type.contains("CAAS") && lastTagMap.containsKey("CAAS")) { // 部署平台包含caas，且能获取到上次投产镜像标签
            // 2.发蓝鲸getCaasNewYaml获取caas生产部署信息,只返回了PROC环境目录下的副本数
            setCaasReplicasByfblue(caas_content, deployName, tag, prodRecord, changeMap, result);
        }
        if (deploy_type.contains("SCC") && lastTagMap.containsKey("SCC")) {
            // 3.发蓝鲸getSccNewYaml 获取scc的生产部署信息，返回了该应用所有租户信息
            setSccReplicasByfblue(scc_content, deployName, tag, prodRecord, changeMap, result);
        }

        // 环境表拿到所有环境目录
        List<AutomationEnv> automationEnvList = automationEnvService.query();
        List<String> envList = new ArrayList<>();
        automationEnvList.forEach(automationEnv -> {
            if (automationEnv.getEnv_name().contains("/PROC")) {
                envList.add(automationEnv.getEnv_name());
            }
        });
        // 4.处理PROC下环境目录副本数，若未取到设置默认值0
         envList.stream().forEach(env -> {

             Map<String, Object> replicasMap = new HashMap<>();
             replicasMap.put("replicas", "0");
             Map<String, Object> scc_replicasMap = new HashMap<>();
             scc_replicasMap.put("scc_replicas", "0");

            // proc环境目录下未从蓝鲸取到的给默认值0
            if (deploy_type.contains("CAAS") && !caas_content.contains(env)) {
                Map<String, Object> envMap = (Map<String, Object>) changeMap.get(env);
                if(!CommonUtils.isNullOrEmpty(envMap)){
                    replicasMap.putAll(envMap);
                }
                changeMap.put(env, replicasMap);
            }
            if (deploy_type.contains("SCC") &&  !scc_content.contains(env)) {
                Map<String, Object> envMap = (Map<String, Object>) changeMap.get(env);
                if (!CommonUtils.isNullOrEmpty(envMap)) {
                    scc_replicasMap.putAll(envMap);
                }
                changeMap.put(env, scc_replicasMap);
            }
        });
        // 5.设置DEV、TEST、TCYZ环境目录副本数设置默认值1

        envList.clear();
        automationEnvList.forEach(automationEnv -> {
            if (!automationEnv.getEnv_name().contains("/PROC")) {
                envList.add(automationEnv.getEnv_name());
            }
        });
        envList.forEach(env -> {

            Map<String, Object> allReplicasMap = new HashMap<>();
            allReplicasMap.put("replicas", "1");
            Map<String, Object> allSccReplicasMap = new HashMap<>();
            allSccReplicasMap.put("scc_replicas", "1");
            Map<String,Object> allMap = new HashMap<>();
            allMap.putAll(allReplicasMap);
            allMap.putAll(allSccReplicasMap);

            Map<String, Object> envMap = (Map<String, Object>) changeMap.get(env);
            if (!CommonUtils.isNullOrEmpty(envMap)) {
                allMap.putAll(envMap);
            }
            changeMap.put(env,allMap);
        });
        Map<String, Object> change = new HashMap<>();
        change.put(Dict.CHANGE, changeMap);
        return change;
    }

    private void setSccReplicasByfblue(List<String> scc_content, String deployName, String tag, ProdRecord prodRecord, Map<String, Object> changeMap, Map<String, Object> networkMap) {
        List<Map<String, Object>> scc_yamls;
        try {
            scc_yamls = appService.getSccNewYaml(deployName, tag);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{e.getMessage()});
        }
        if (!CommonUtils.isNullOrEmpty(scc_yamls)) {
            for (Map<String, Object> scc_map : scc_yamls) {
                String namespace = (String) scc_map.get("namespace_code"); //租户
                Map<String, Object> sccYaml = (Map<String, Object>) scc_map.get("yaml");
                Map<String, Object> scc_specMap = (Map<String, Object>) sccYaml.get("spec");
                int replicas = !CommonUtils.isNullOrEmpty(scc_specMap.get("replicas"))?(int) scc_specMap.get("replicas") : 0;
                // 循环环境表，取到变更类型下该网段的租户名称
                List<AutomationEnv> automationEnvList = automationEnvService.query();
                for (AutomationEnv automationEnv : automationEnvList) {
                    String key = automationEnv.getEnv_name();   //环境目录
                    Map<String,Object> namespaceMap = automationEnv.getScc_namespace(); // scc租户与环境映射关系map
                    Map<String,Object> typeMap = (Map<String, Object>) namespaceMap.get(prodRecord.getType()); // 变更类型
                    String table_namespace = (String) typeMap.get(networkMap.get(Dict.NETWORK)); // 租户名称
                    Map<String,Object> replicasMap = new HashMap<>();
                    replicasMap.put("scc_replicas", replicas);
                    if(table_namespace.equals(namespace)){
                        scc_content.add(key);
                        Map<String, Object> envMap = (Map<String, Object>) changeMap.get(key);
                        if (!CommonUtils.isNullOrEmpty(envMap)) {
                            replicasMap.putAll(envMap);
                        }
                        changeMap.put(key, replicasMap);
                    }
                }
            }
        }
    }

    private void setCaasReplicasByfblue(List<String> caas_content, String deployName, String tag, ProdRecord prodRecord, Map<String, Object> changeMap, Map<String, Object> networkMap) {
        List<Map<String, Object>> yamls;
        try {
            yamls = appService.getNewYaml(deployName, tag);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{e.getMessage()});
        }
        if (!CommonUtils.isNullOrEmpty(yamls)) {
            Map<String,Object> resultMap = new HashMap<>();
            for (Map<String, Object> map : yamls) {
                String tenant = (String) map.get("cluster"); // shk1、shk2、hfk1、hfk2
                String namespace = (String) map.get("namespace");
                Map<String, Object> yamlMap = (Map<String, Object>) map.get("yaml");
                Map<String, Object> specMap = (Map<String, Object>) yamlMap.get("spec");
                int replicas = !CommonUtils.isNullOrEmpty(specMap.get("replicas"))?(int) specMap.get("replicas") : 0;
                String type = namespace.contains(Dict.GRAY) ? Dict.GRAY : "proc";
                if (type.equals(prodRecord.getType())) {
                    List<String> bizs = Arrays.asList(fdevBiz.split(","));
                    List<String> dmzs = Arrays.asList(fdevDmz.split(","));
                    resultMap.put(Dict.NAMESPACE,namespace);
                    resultMap.put("replicas",replicas);
                    if (bizs.contains(namespace)) {
                        resultMap.put(Dict.NETWORK, "biz");
                    } else if (dmzs.contains(namespace)) {
                        resultMap.put(Dict.NETWORK, "dmz");
                    } else {
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{namespace + "数据异常"});
                    }
                    if(networkMap.get(Dict.NETWORK).equals(resultMap.get(Dict.NETWORK))){
                        String key = "/PROC/" + tenant.toUpperCase(); //环境目录
                        caas_content.add(key);
                        Map<String, Object> params = new HashMap<>();
                        params.put("replicas", replicas);
                        changeMap.put(key, params);
                    }
                }
            }
        }
    }

    @Override
    public void updateReplicasnu(Map<String, Object> req) throws Exception {
        String prod_id = (String) req.get(Dict.PROD_ID);
        String application_id = (String) req.get(Dict.APPLICATION_ID);
        Map<String, Object> change = (Map<String, Object>) req.get(Dict.CHANGE); // 用户上送
        ProdApplication prodApplication = prodApplicationDao.queryApplication(prod_id, application_id);
        Map<String, Object> changeMap = prodApplication.getChange();
        if(CommonUtils.isNullOrEmpty(changeMap)){
            changeMap = new HashMap<>();
        }
        // 合并两个map
        Map<String,Object> allMap = new HashMap<>(changeMap);
        // key相同的，value求并集
        for(Map.Entry<String,Object> entry: changeMap.entrySet()){
            for(Map.Entry<String,Object> entry1: change.entrySet()){
                if(entry.getKey().equals(entry1.getKey())){
                    Map<String,Object> envMap = (Map<String, Object>) entry.getValue();
                    envMap.putAll((Map<String,Object>)entry1.getValue());
                    allMap.put(entry.getKey(),envMap);
                }
            }
        }
        // 求用户上送的change - 库里的change 差集
        List<String> strRemove = new ArrayList<>();
        Map<String,Object> res = new HashMap<>(change);
        for(String s : res.keySet()){
            if(changeMap.containsKey(s)){
                strRemove.add(s);
            }
        }
        for(String s: strRemove){
            res.remove(s);
        }
        logger.info("用户上送的change与库里的差集：" + res.toString());
        allMap.putAll(res);
        updateProdChange(prod_id, application_id, allMap);

    }


    @Override
    public void updateProdChange(String prd_id, String application_id, Map<String, Object> changeList) {
        prodApplicationDao.updateProdChange(prd_id, application_id, changeList);
    }

    @Override
    public Map<String, Object> queryDeployTypeByAppId(String applicationId, String prodId) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        ProdRecord prodRecord = prodRecordService.queryDetail(prodId);
//        List<String> deployType = queryOldDeployTypeByAppId(applicationId, prodRecord);
        HashSet<String> deployType = findAllDeployedType(applicationId, prodRecord);
        // 如果是老模板则不返回scc部署平台
        if (prodRecord.getScc_prod().equals("0") && deployType.contains("SCC")) {
            deployType.removeIf(s -> s.contains("SCC"));
        }
        resultMap.put(Dict.DEPLOY_TYPE, deployType);
        return resultMap;
    }

    @Override
    public void updateProdDeploy(String prd_id, String application_id, String release_type, List<String> deploy_type) throws Exception {
        List<String> prod_dir = new ArrayList<String>(); // 介质目录
        ProdRecord prodRecord = prodRecordService.queryDetail(prd_id);
        ProdApplication prodApplication = prodApplicationDao.queryApplication(prd_id, application_id);
        List<String> platformList = prodApplication.getDeploy_type(); // 库里的部署平台

        // 已经部署过的平台
        HashSet<String> hasDeployType = prodApplicationDao.findAllDeployedType(application_id, prodRecord);
        if (!"1".equals(prodApplication.getEsf_flag())) {
            if (Constants.SERVICE_STOP_AND_START.equals(release_type)) {
                deploy_type.forEach(deploy -> {
                    if (!hasDeployType.contains(deploy)) {
                        throw new FdevException(ErrorConstants.DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行停止后启动操作！"});
                    }
                });
            }
            if (Constants.SERVICE_RESTART.equals(release_type)) {
                deploy_type.forEach(deploy -> {
                    if (!hasDeployType.contains(deploy)) {
                        throw new FdevException(ErrorConstants.DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行重启操作！"});
                    }
                });
            }
        }
        List<String> prodDirList = prodApplication.getProd_dir();
        if (deploy_type.size() < platformList.size()) { // 取消部署平台
            if (deploy_type.contains("CAAS")) {
                // 去掉scc相关平台的目录
                prod_dir = prodDirList.stream().filter(dir -> !dir.split("_")[0].equals("scc")).collect(Collectors.toList());
            }
            if (deploy_type.contains("SCC")) {
                // 去掉caas相关平台的目录
                prod_dir = prodDirList.stream().filter(dir -> !dir.split("_")[0].equals("docker")).collect(Collectors.toList());
            }
        } else { // 新增平台 需要判断新增的是哪个平台，则只设置新增哪个平台的介质目录
            // 取前端上送的部署平台 与 数据库中的部署平台 求差集，则认为是新增的部署平台
            List<String> chaList = deploy_type.stream().filter(item -> !platformList.contains(item)).collect(Collectors.toList());
            logger.info("新增的部署平台：" + chaList);

            if (chaList.contains("CAAS")) {
                if (!hasDeployType.contains("CAAS")) { //未部署过cass
                    prod_dir.add(Dict.DOCKER_YAML);
                } else { // 部署过caas
                    if (Constants.SERVICE_ASYN_UPDATE.equals(release_type)) { // 微服务镜像异步更新
                        prod_dir.add(Dict.DOCKER_LOWERCASE);
                    } else if (Constants.SERVICE_SYN_UPDATE.equals(release_type)) { // K1、K2镜像同步更新 =》更改为：单集群停止
                        prod_dir.add(Dict.DOCKER_STOP);
                    } else if (Constants.SERVICE_STOP_AND_START.equals(release_type)) { // 停止后重启
                        prod_dir.add(Dict.DOCKER_STOPALL);
                        prod_dir.add(Dict.DOCKER_STARTALL);
                    } else if (Constants.SERVICE_RESTART.equals(release_type)) { // 服务重启
                        prod_dir.add(Dict.DOCKER_RESTART);
                    }
                }
                // 需加上库中scc的介质目录
                prod_dir.addAll(prodDirList.stream().filter(dir -> dir.split("_")[0].equals("scc")).collect(Collectors.toList()));
            }
            if (chaList.contains("SCC")) {
                if (!hasDeployType.contains("SCC")) {
                    prod_dir.add(Dict.SCC_YAML);
                } else {
                    if (Constants.SERVICE_ASYN_UPDATE.equals(release_type)) { // 微服务镜像异步更新
                        prod_dir.add(Dict.SCC);
                    } else if (Constants.SERVICE_SYN_UPDATE.equals(release_type)) { // K1、K2镜像同步更新=>单集群停止
                        prod_dir.add(Dict.SCC_STOP);
                    } else if (Constants.SERVICE_STOP_AND_START.equals(release_type)) { // 停止后重启
                        prod_dir.add(Dict.SCC_STOPALL);
                        prod_dir.add(Dict.SCC_STARTALL);
                    } else if (Constants.SERVICE_RESTART.equals(release_type)) { // 服务重启
                        prod_dir.add(Dict.SCC_RESTART);
                    }
                }
                // 需加上库中caas介质目录
                prod_dir.addAll(prodDirList.stream().filter(dir -> dir.split("_")[0].equals("docker")).collect(Collectors.toList()));
            }
        }
        // 新增逻辑:如果应用是从esf添加而来，在修改部署平台时，首先根据用户在esf介质目录添加的部署平台,然后判断用户上送的部署平台，最终来决定docker_yaml或scc_yaml
        List<String> esf_platform = prodApplication.getEsf_platform();
        if ("1".equals(prodApplication.getEsf_flag())) {
            if (esf_platform.contains("CAAS") && deploy_type.contains("CAAS") && !prod_dir.contains(Dict.DOCKER_YAML)) {
                prod_dir.add(Dict.DOCKER_YAML);
            }
            if (esf_platform.contains("SCC") && deploy_type.contains("SCC") && !prod_dir.contains(Dict.SCC_YAML)) {
                prod_dir.add(Dict.SCC_YAML);
            }
        }
        // mspmk-cli前端应用，去掉scc相关介质目录
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        String name_en = (String) application.get(Dict.NAME_EN);
        List<String> new_prod_dir = prod_dir;
        if (name_en.startsWith("mspmk-cli")) {
            new_prod_dir = prod_dir.stream().filter(dir -> !dir.split("_")[0].equals("scc")).collect(Collectors.toList());
        }
        prodApplicationDao.updateProdDir(prd_id, application_id, new_prod_dir, deploy_type);
    }

    @Override
    public List<String> queryOldDeployTypeByAppId(String application_id, ProdRecord prodRecord) {
        return prodApplicationDao.queryOldDeployTypeByAppId(application_id, prodRecord.getProd_id(), prodRecord.getType());
    }

    @Override
    public HashSet<String> findAllDeployedType(String application_id, ProdRecord prodRecord) {
        return prodApplicationDao.findAllDeployedType(application_id, prodRecord);
    }

}
