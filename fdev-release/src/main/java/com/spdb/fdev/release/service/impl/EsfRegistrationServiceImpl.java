package com.spdb.fdev.release.service.impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.DeployTypeEnum;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.service.LdapUserAuthenticationService;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IAutomationEnvDao;
import com.spdb.fdev.release.dao.impl.EsfRegistrationDaoImpl;
import com.spdb.fdev.release.dao.impl.ProdApplicationDaoImpl;
import com.spdb.fdev.release.dao.impl.ProdRecordDaoImpl;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.IAutomationEnvService;
import com.spdb.fdev.release.service.IEsfRegistration;
import com.spdb.fdev.release.service.IGitlabService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RefreshScope
public class EsfRegistrationServiceImpl implements IEsfRegistration {

    @Value("${fdev.biz.mapping}")
    private String fdevBiz;
    @Value("${fdev.dmz.mapping}")
    private String fdevDmz;
    @Value("${gitlab.manager.token}")
    private String Token;
    @Value("${sdk.gk.url}")
    private String sdk_gk_url;
    @Autowired
    IGitlabService gitlabService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private EsfRegistrationDaoImpl registrationDao;
    @Autowired
    private ProdRecordDaoImpl prodRecordDao;
    @Autowired
    private ProdApplicationDaoImpl prodApplicationDao;
    @Autowired
    private IAutomationEnvDao automationParamDao;
    @Autowired
    private AppServiceImpl appService;
    @Autowired
    private ProdApplicationServiceImpl prodApplicationService;
    @Autowired
    private IAutomationEnvService automationEnvService;
    @Autowired
    LdapUserAuthenticationService ldapUserAuthenticationService;

    private final static Logger logger = LoggerFactory.getLogger(EsfRegistrationServiceImpl.class);

    @Override
    public void batchAdd(Map<String, Object> req) throws Exception {
        List<Map<String, Object>> esfConfiguraList = (List<Map<String, Object>>) req.get("esfConfigList");
        List<EsfConfiguration> esfConfigurationList = new ArrayList<>();
        for (Map esfConfig : esfConfiguraList) {
            EsfConfiguration esfConfiguration = CommonUtils.map2Object(esfConfig, EsfConfiguration.class);
            esfConfigurationList.add(esfConfiguration);
        }
        registrationDao.batchAdd(esfConfigurationList);
    }

    @Override
    public List<EsfConfiguration> queryEsfConfig(Map<String, Object> req) throws Exception {
        String application_id = (String) req.get(Dict.APPLICATION_ID);
        String env_name = (String) req.get(Dict.ENV_NAME); // 环境目录 DEV TEST PROCSH PROCHF
        //根据应用id查询网段
        Map<String, Object> appMap = appService.queryAPPbyid(application_id);
        String network = "";
        if (!CommonUtils.isNullOrEmpty(appMap)) {
            network = (String) appMap.get(Dict.NETWORK);
        }
        String[] network_arr = network.split(",");
        return registrationDao.queryEsfConfig(env_name, network_arr);
    }

    @Override
    public void addEsfRegistration(Map<String, Object> req) throws Exception {
        // 获取前端上送的参数
        String prod_id = (String) req.get(Dict.PROD_ID);
        String application_id = (String) req.get(Dict.APPLICATION_ID);
        String sid = (String) req.get("sid"); // 应用sid
        List<String> platform = (List<String>) req.get("platform");
        String scc_network_area = null;
        char caas_run_time = '1'; // caas平台只有Underlay，RUN_TIME的值'1'
        char scc_run_time = '1'; // scc平台网络区域:Underlay，Overlay，当Overlay时,RUN_TIME值'1';当Overlay时，RUN_TIME值'2'
        String caas_network_area = (String) req.get("caas_network_area"); // 网络区域:Underlay
        if (platform.contains("SCC")) {
            scc_network_area = (String) req.get("scc_network_area"); // 网络区域:Underlay，Overlay
            if ("Overlay".equals(scc_network_area)) {
                scc_run_time = '2';
            }
        }
        // 根据应用id获取应用详情，从而获取到网段，应用英文名
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        EsfRegistration esfRegistration = registrationDao.queryEsfRegistByPlatform(prod_id, application_id, platform);
        // 控制同个应用一个平台只能添加一次
        if (!CommonUtils.isNullOrEmpty(esfRegistration)) {
            throw new FdevException(ErrorConstants.HAS_TYPE_PALATFORM, new String[]{"【" + application.get(Dict.NAME_EN) + "】", platform.toString()});
        }
        Map<String, Object> esfInfo = (Map<String, Object>) req.get("esf_info");
        ProdRecord prodRecord = prodRecordDao.queryByProdId(prod_id);
        ProdApplication prodApplication = prodApplicationDao.queryApplication(prod_id, application_id);
        User user = CommonUtils.getSessionUser();
        String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
        // 1.根据用户注册的平台获取上次投产镜像标签，从而判断应用是否有过投产
        String last_tag = null;
        String deployName = null;
        Map<String, String> lastTagMap = new HashMap<>();
        for (String type : platform) {
            String lastTag = prodApplicationDao.findLastReleaseUri(application_id, prodRecord, type);
            if (!CommonUtils.isNullOrEmpty(lastTag)) {
                lastTagMap.put(type, lastTag);
                last_tag = lastTag.split(":")[1];
                deployName = lastTag.split(":")[0].split("/")[2];
            }
        }
        // 查询配置文件实体
        Map<String, Object> configGitlab = findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
        String network = (String) application.get(Dict.NETWORK); // 网段
        if(!CommonUtils.isNullOrEmpty(network) && !network.contains("dmz")){
            network = "biz";
        }else {
            network = "dmz";
        }
        String name_en = (String) application.get(Dict.NAME_EN);
        String type = prodRecord.getType(); // 变更类型
        Map<String, Object> tempChangeMap = new HashMap<>();
        // 2.未投过，则从auto-config的master分支拉取文件，进行修改
        if (CommonUtils.isNullOrEmpty(lastTagMap)) { // 两个平台都未投过产
            // 根据环境目录 + 变更类型 + 网段，取出环境表所有对应的fdev环境信息
            List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, platform, esfInfo);
            // 拿到fdev所有环境，根据fdev环境去找到auto-config 对应的目录yaml文件进行修改
            setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);
        } else { // 有一个平台投过
            // 3.投过产，从蓝鲸平台拉取最新的yaml，更新yaml后，推送到master分支,拉取新的ESF TAG。命名为当前投产窗口加上后缀-esf,举例：pro-20210914_001-001-esf
            // PROCSH,PROCHF SDK-GK的值保持一致
            String sdk_gk = getSDK_GK();
            Map<String, Object> procsh_map = (Map<String, Object>) esfInfo.get("PROCSH");
            procsh_map.put("sdk_gk", sdk_gk);// 存esf_registration表，为脚本中取做准备
            Map<String, Object> prochf_map = (Map<String, Object>) esfInfo.get("PROCHF");
            prochf_map.put("sdk_gk", sdk_gk);// 存esf_registration表，为脚本中取做准备
            // 拆分成两个map
            Map<String, Object> testMap = new HashMap<>();
            Map<String, Object> procMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : esfInfo.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> value = (Map<String, Object>) entry.getValue();
                if (key.startsWith("PROC")) {
                    procMap.put(key, value);
                } else {
                    testMap.put(key, value);
                }
            }
            logger.info("@@DEV,TEST: " + testMap.toString());
            logger.info("@@PROC: " + procMap.toString());

            // 更新PROC环境目录对应的fdev环境目录下的yaml内容
            for (String deploy_type : platform) {
                if ("CAAS".equals(deploy_type)) {
                    if (!lastTagMap.containsKey(deploy_type)) { // CAAS未投过
                        List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, Arrays.asList("CAAS"), procMap);
                        setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);

                    } else { // CAAS投过
                        if (!CommonUtils.isNullOrEmpty(prodApplication) && !CommonUtils.isNullOrEmpty(prodApplication.getPro_image_uri())) {
                            String caas_image_uri = prodApplication.getPro_image_uri();
                            last_tag = caas_image_uri.split(":")[1];
                            deployName = caas_image_uri.split(":")[0].split("/")[2];
                        }
                        setCaasYamlByfblue(application_id, procMap, deployName, last_tag, prodRecord, prodApplication, tempChangeMap, caas_run_time);
                    }
                }
                if ("SCC".equals(deploy_type)) {
                    if (!lastTagMap.containsKey(deploy_type)) { // SCC未投过
                        List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, Arrays.asList("SCC"), procMap);
                        setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);
                    } else { // SCC投过
                        if (!CommonUtils.isNullOrEmpty(prodApplication) && !CommonUtils.isNullOrEmpty(prodApplication.getPro_scc_image_uri())) {
                            String scc_image_uri = prodApplication.getPro_scc_image_uri();
                            last_tag = scc_image_uri.split(":")[1];
                            deployName = scc_image_uri.split(":")[0].split("/")[2];
                        }
                        setSccYamlByfblue(application_id, procMap, deployName, last_tag, prodRecord, scc_run_time, scc_network_area);
                    }
                }
            }

            // 更新DEV,TEST环境目录对应的fdev环境目录下的yaml内容
            List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, platform, testMap);
            setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);
        }
        // 4.删出带esf的tag
        String new_tag = "pro-" + prodRecord.getRelease_node_name() + "-esf";
        logger.info("开始删除tag，名称：{}", new_tag);
        try {
            gitlabService.deleteTag((Integer) configGitlab.get(Dict.CONFIGGITLABID), new_tag);
        } catch (Exception e) {
            logger.error("无此tag删除失败，略过");
        }
        logger.info("删除tag成功");
        // 5.从master分支拉取新tag，并命名带esf的后缀
        try {
            pullNewBranch(new_tag, configGitlab);
            // 检查变更的应用更新列表，若该应用已有应用卡片，则新增docker_yaml或scc_yaml目录
            if (!CommonUtils.isNullOrEmpty(prodApplication)) {
                // 设置tag为esf的tag
                prodApplication.setTag(new_tag);
                prodApplication.setEsf_flag("1"); // 0或空-无esf应用记录 1-有esf应用记录
                // 部署平台根据esf介质目录用户添加该应用的部署平台来
                List<EsfRegistration> esfRegistrationList = registrationDao.queryEsfRegistsById(prod_id, application_id);
                Set<String> platforms = new HashSet<>();
                platforms.addAll(platform);
                if (!CommonUtils.isNullOrEmpty(esfRegistrationList)) {
                    Set<String> plat_form = (Set<String>) esfRegistrationList.stream().map(EsfRegistration::getPlatform);
                    if (!CommonUtils.isNullOrEmpty(plat_form)) {
                        platforms.addAll(plat_form);
                    }
                }
                prodApplication.setEsf_platform(new ArrayList<>(platforms));
                // 判断是否有docker_yaml或scc_yaml目录，若没有则判断esf新增的应用所属平台，然后添加对应的目录
                List<String> prod_dir = prodApplication.getProd_dir();
                if (!prod_dir.contains(Dict.DOCKER_YAML) && platforms.contains("CAAS")) {
                    prod_dir.add(Dict.DOCKER_YAML);
                }
                if (!prod_dir.contains(Dict.SCC_YAML) && platforms.contains("SCC")) {
                    prod_dir.add(Dict.SCC_YAML);
                }
                List<String> new_prod_dir = prod_dir;
                if (name_en.startsWith("mspmk-cli")) {
                    new_prod_dir = prod_dir.stream().filter(dir -> !dir.split("_")[0].equals("scc")).collect(Collectors.toList());
                }
                prodApplication.setProd_dir(new_prod_dir);
                prodApplicationDao.updateProdDirs(prodApplication);
            } else {
                // 7.变更应用中不存在该应用，则添加该应用，变更类型为应用重启，镜像标签自动选择上次的镜像标签，配置文件tag字段使用esf的tag
                prodApplication = new ProdApplication();
                prodApplication.setProd_id(prod_id);
                prodApplication.setApplication_id(application_id);
                String pro_image_uri = lastTagMap.get(DeployTypeEnum.CAAS.getType());
                String pro_scc_image_uri = lastTagMap.get(DeployTypeEnum.SCC.getType());
                if (platform.contains("CAAS")) {
                    prodApplication.setPro_image_uri(pro_image_uri);
                }
                if (platform.contains("SCC")) {
                    prodApplication.setPro_scc_image_uri(pro_scc_image_uri);
                }
                // 获取不到上次投产镜像标签的默认为新增应用
                String caas_add_sign = "0";  // 新增应用标识 1-新增应用
                String scc_add_sign = "0";
                if (CommonUtils.isNullOrEmpty(pro_image_uri)) {
                    caas_add_sign = "1";
                }
                if (CommonUtils.isNullOrEmpty(pro_scc_image_uri)) {
                    scc_add_sign = "1";
                }
                prodApplication.setCaas_add_sign(caas_add_sign);
                prodApplication.setScc_add_sign(scc_add_sign);
                prodApplication.setStatus("0");
                prodApplication.setRelease_type("4");
                prodApplication.setTag(new_tag);
                prodApplication.setEsf_flag("1");
                // 部署平台根据esf介质目录用户添加该应用的部署平台来
                List<EsfRegistration> esfRegistrationList = registrationDao.queryEsfRegistsById(prod_id, application_id);
                Set<String> platforms = new HashSet<>();
                platforms.addAll(platform);
                if (!CommonUtils.isNullOrEmpty(esfRegistrationList)) {
                    Set<String> plat_form = (Set<String>) esfRegistrationList.stream().map(EsfRegistration::getPlatform);
                    if (!CommonUtils.isNullOrEmpty(plat_form)) {
                        platforms.addAll(plat_form);
                    }
                }
                prodApplication.setEsf_platform(new ArrayList<>(platforms));
                prodApplication.setDeploy_type(new ArrayList<>(platforms));
                // 老模板不能选中scc部署平台
                if ("0".equals(prodRecord.getScc_prod())) {
                    prodApplication.setDeploy_type(Arrays.asList("CAAS"));
                } else {
                    prodApplication.setDeploy_type(new ArrayList<>(platforms));
                }
                List<String> prod_dir = new ArrayList<>();
                if (platforms.contains("CAAS")) {
                    prod_dir.add(Dict.DOCKER_RESTART);
                    prod_dir.add(Dict.DOCKER_YAML);
                }
                if (platforms.contains("SCC") && !name_en.startsWith("mspmk-cli")) {
                    prod_dir.add(Dict.SCC_RESTART);
                    prod_dir.add(Dict.SCC_YAML);
                }
                prodApplication.setProd_dir(prod_dir);
                prodApplication.setChange(tempChangeMap);
                prodApplicationService.addApplication(prodApplication);

            }
            // 6.插入数据esf_registration
            registrationDao.addEsfRegistration(new EsfRegistration(prod_id, application_id, caas_network_area, scc_network_area, sid, platform, user.getUser_name_en(), user.getUser_name_cn(), upload_time, esfInfo));
        } catch (Exception e) {
            logger.error("新拉取esf分支异常！" + e.getMessage());
            e.printStackTrace();
        }

    }


    @Override
    public void updateEsf(Map<String, Object> req) throws Exception {
        String id = (String) req.get(Dict.ID);
        String prod_id = (String) req.get(Dict.PROD_ID);
        String application_id = (String) req.get(Dict.APPLICATION_ID);
        String sid = (String) req.get("sid"); // 应用sid
        List<String> platform = (List<String>) req.get("platform");
        String caas_network_area = (String) req.get("caas_network_area"); // 网络区域: Underlay
        String scc_network_area = null;
        char caas_run_time = '1';
        char scc_run_time = '1';
        if (platform.contains("SCC")) {
            scc_network_area = (String) req.get("scc_network_area"); // 网络区域:Underlay，Overlay
            if ("Overlay".equals(scc_network_area)) {
                scc_run_time = '2';
            }
        }
        Map<String, Object> esfInfo = (Map<String, Object>) req.get("esf_info");
        ProdRecord prodRecord = prodRecordDao.queryByProdId(prod_id);
        // 2.获取到应用详情，从而获取到网段
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        ProdApplication prodApplication = prodApplicationService.queryApplication(prod_id, application_id);
        String last_tag = null;
        String deployName = null;
        Map<String, String> lastTagMap = new HashMap<>();
        Map<String, Object> tempChangeMap = new HashMap<>();
        for (String type : platform) {
            String lastTag = prodApplicationDao.findLastReleaseUri(application_id, prodRecord, type);
            if (!CommonUtils.isNullOrEmpty(lastTag)) {
                lastTagMap.put(type, lastTag);
                last_tag = lastTag.split(":")[1];
                deployName = lastTag.split(":")[0].split("/")[2];
            }
        }
        //查询配置文件实体
        Map<String, Object> configGitlab = findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
        // 3.未投过，则从auto-config的master分支拉取文件，进行修改
        String network = (String) application.get(Dict.NETWORK); // 网段
        if(!CommonUtils.isNullOrEmpty(network) && !network.contains("dmz")){
            network = "biz";
        }else {
            network = "dmz";
        }
        String name_en = (String) application.get(Dict.NAME_EN);
        String type = prodRecord.getType(); // 变更类型
        Map<String, Object> temp_change = new HashMap<>();
        // 2.未投过，则从auto-config的master分支拉取文件，进行修改
        if (CommonUtils.isNullOrEmpty(lastTagMap)) { // 两个平台都未投过产
            // 根据环境目录 + 变更类型 + 网段，取出环境表所有对应的fdev环境信息
            List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, platform, esfInfo);
            // 拿到fdev所有环境，根据fdev环境去找到auto-config 对应的目录yaml文件进行修改
            setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);
        } else { // 有一个平台投过;3.投过产，从蓝鲸平台拉取最新的yaml，更新yaml后，推送到master分支,拉取新的ESF TAG。命名为当前投产窗口加上后缀-esf,举例：pro-20210914_001-001-esf

            // PROCSH sdk-gk的值保持一致  PROCHF sdf-gk的值保持一致
            String sdk_gk = getSDK_GK();
            Map<String, Object> procsh_map = (Map<String, Object>) esfInfo.get("PROCSH");
            procsh_map.put("sdk_gk", sdk_gk);// 存esf_registration表，为脚本中取做准备
            Map<String, Object> prochf_map = (Map<String, Object>) esfInfo.get("PROCHF");
            prochf_map.put("sdk_gk", sdk_gk);// 存esf_registration表，为脚本中取做准备

            // 拆分成两个map
            Map<String, Object> testMap = new HashMap<>();
            Map<String, Object> procMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : esfInfo.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> value = (Map<String, Object>) entry.getValue();
                if (key.startsWith("PROC")) {
                    procMap.put(key, value);
                } else {
                    testMap.put(key, value);
                }
            }
            logger.info("@@DEV,TEST: " + testMap.toString());
            logger.info("@@PROC: " + procMap.toString());

            // 更新PROC环境目录对应的fdev环境目录下的yaml内容
            for (String deploy_type : platform) {
                if ("CAAS".equals(deploy_type)) {
                    if (!lastTagMap.containsKey(deploy_type)) { // CAAS未投过
                        List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, Arrays.asList("CAAS"), procMap);
                        setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);
                    } else { // CAAS投过
                        if (!CommonUtils.isNullOrEmpty(prodApplication) && !CommonUtils.isNullOrEmpty(prodApplication.getPro_image_uri())) {
                            String caas_image_uri = prodApplication.getPro_image_uri();
                            last_tag = caas_image_uri.split(":")[1];
                            deployName = caas_image_uri.split(":")[0].split("/")[2];
                        }
                        setCaasYamlByfblue(application_id, procMap, deployName, last_tag, prodRecord, prodApplication, tempChangeMap, caas_run_time);
                    }
                }
                if ("SCC".equals(deploy_type)) {
                    if (!lastTagMap.containsKey(deploy_type)) { // SCC未投过
                        List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, Arrays.asList("SCC"), procMap);
                        setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);
                    } else { // SCC投过
                        if (!CommonUtils.isNullOrEmpty(prodApplication) && !CommonUtils.isNullOrEmpty(prodApplication.getPro_scc_image_uri())) {
                            String scc_image_uri = prodApplication.getPro_scc_image_uri();
                            last_tag = scc_image_uri.split(":")[1];
                            deployName = scc_image_uri.split(":")[0].split("/")[2];
                        }
                        setSccYamlByfblue(application_id, procMap, deployName, last_tag, prodRecord, scc_run_time, scc_network_area);
                    }
                }
            }
            // 更新DEV,TEST环境目录对应的fdev环境目录下的yaml内容
            List<Map<String, Object>> fdev_env_list = getFdevEnvByEnvName(type, network, platform, testMap);
            setYaml(fdev_env_list, name_en, configGitlab, caas_run_time, scc_run_time, scc_network_area);
        }
        String new_tag = "pro-" + prodRecord.getRelease_node_name() + "-esf";
        // 5.删除带-esf的tag
        logger.info("开始删除带-esf的tag，名称：{}", new_tag);
        try {
            gitlabService.deleteTag((Integer) configGitlab.get(Dict.CONFIGGITLABID), new_tag);
        } catch (Exception e) {
            logger.error("无此tag删除失败，略过");
        }
        logger.info("删除tag成功");
        // 6.从master分支拉取新tag，并命名带esf的后缀
        pullNewBranch(new_tag, configGitlab);
        // 修改esf_registration数据
        registrationDao.updateEsfRegistration(id, prod_id, application_id, caas_network_area, scc_network_area, sid, platform, esfInfo);
        // 7. 修改变更应用的esf_paltform字段// 部署平台根据esf介质目录用户添加该应用的部署平台来
        List<String> deploy_type = prodApplication.getDeploy_type();
        deploy_type.addAll(platform);
        Set<String> deploy_type_set = new HashSet<>(deploy_type);
        prodApplication.setEsf_platform(platform);
        prodApplication.setDeploy_type(new ArrayList<>(deploy_type_set));
        List<String> prod_dir = prodApplication.getProd_dir();
        if (!prod_dir.contains(Dict.DOCKER_YAML) && platform.contains("CAAS")) {
            prod_dir.add(Dict.DOCKER_YAML);
        }
        if (!prod_dir.contains(Dict.SCC_YAML) && platform.contains("SCC")) {
            prod_dir.add(Dict.SCC_YAML);
        }
        List<String> new_prod_dir = prod_dir;
        if (name_en.startsWith("mspmk-cli")) {
            new_prod_dir = prod_dir.stream().filter(dir -> !dir.split("_")[0].equals("scc")).collect(Collectors.toList());
        }
        prodApplication.setProd_dir(new_prod_dir);
        prodApplicationDao.updateEsfFlag(prodApplication);
    }

    @Override
    public Map<String, Object> queryAppStatus(Map<String, Object> req) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String application_id = (String) req.get(Dict.APPLICATION_ID);
        String prod_id = (String) req.get(Dict.PROD_ID);
        ProdRecord prodRecord = prodRecordDao.queryByProdId(prod_id);
        String type = prodRecord.getType(); // 变更类型
        //1.判断应用是否添加到变更中prod_applicaton
        ProdApplication prodApplication = prodApplicationDao.queryApplication(prod_id, application_id);
        int hasProducted = 2; //false: 0-未投产，true：1-已添加变更应用；2-已投产
        List<String> deploy_type = new ArrayList<>();
        String pro_image_uri = null;
        String pro_scc_image_uri = null;
        if (CommonUtils.isNullOrEmpty(prodApplication)) {
            //2.没有添加，则判断应用在当前变更类型下，是否有过投产，若无投产，则提示需要先添加变更应用。
            String last_tag = prodApplicationDao.findLastReleaseUri(application_id, prodRecord, Dict.CAAS);
            if (CommonUtils.isNullOrEmpty(last_tag)) {
                last_tag = prodApplicationDao.findLastReleaseUri(application_id, prodRecord, Dict.SCC);
            }
            // 未查找到上次投产的镜像表示未曾投产
            if (CommonUtils.isNullOrEmpty(last_tag)) {
                hasProducted = 0;
                logger.info("该应用在" + type + "变更未曾投过产！");
            } else {
                hasProducted = 2;
                deploy_type = Arrays.asList("CAAS", "SCC");
            }
        } else {
            // 已有卡片，判断是否有镜像标签，若无则提示去选择镜像标签
            deploy_type = prodApplication.getDeploy_type();
            resultMap.put(Dict.DEPLOY_TYPE, deploy_type);
            for (String deployType : deploy_type) {
                if ("CAAS".equals(deployType)) {
                    if (!CommonUtils.isNullOrEmpty(prodApplication.getPro_image_uri())) {
                        pro_image_uri = prodApplication.getPro_image_uri();
                    }
                }
                if ("SCC".equals(deployType)) {
                    if (!CommonUtils.isNullOrEmpty(prodApplication.getPro_scc_image_uri())) {
                        pro_scc_image_uri = prodApplication.getPro_scc_image_uri();
                    }
                }
            }
            // 已添加应用卡片，把卡片选择的平台一并返回给前端
            hasProducted = 1;
        }
        resultMap.put(Dict.PRO_IMAGE_URI, pro_image_uri);
        resultMap.put(Dict.PRO_SCC_IMAGE_URI, pro_scc_image_uri);
        resultMap.put(Dict.DEPLOY_TYPE, deploy_type);
        resultMap.put("flag", hasProducted);
        return resultMap;
    }

    @Override
    public void delEsf(Map<String, Object> req) throws Exception {
        String id = (String) req.get(Dict.ID);
        String prod_id = (String) req.get(Dict.PROD_ID);
        String application_id = (String) req.get(Dict.APPLICATION_ID);
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        //查询配置文件实体
        Map<String, Object> configGitlab = findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
        // 1.删除auto-config下带-esf的tag
        logger.info("开始删除esf-tag");
        ProdRecord prodRecord = prodRecordDao.queryByProdId(prod_id);
        String new_tag = "pro-" + prodRecord.getRelease_node_name() + "-esf";
        try {
            gitlabService.deleteTag((Integer) configGitlab.get(Dict.CONFIGGITLABID), new_tag);
        } catch (Exception e) {
            logger.error("无此tag删除失败，略过");
        }
        logger.info("删除tag成功");
        // 2.删除应用用户注册信息
        try {
            registrationDao.delEsf(id);
            // 3.当esf介质目录该变更该应用至少有一条且变更不为空，设置esf标识为1，否则为0
            List<EsfRegistration> esfRegistrationList = registrationDao.queryEsfRegistsById(prod_id, application_id);
            ProdApplication prodApplication = prodApplicationService.queryApplication(prod_id, application_id);
            if (!CommonUtils.isNullOrEmpty(prodApplication)) {
                if (!CommonUtils.isNullOrEmpty(esfRegistrationList) && esfRegistrationList.size() >= 1) {
                    prodApplication.setEsf_flag("1");
                    // 部署平台根据esf介质目录用户添加该应用的部署平台来
                    Set<String> platforms = new HashSet<>();
                    if (!CommonUtils.isNullOrEmpty(esfRegistrationList)) {
                        platforms = (Set<String>) esfRegistrationList.stream().map(EsfRegistration::getPlatform);
                    }
                    prodApplication.setEsf_platform(new ArrayList<>(platforms));
                } else {
                    prodApplication.setEsf_flag("0");
                    prodApplication.setEsf_platform(new ArrayList<>());
                }
                // 删除esf用户注册信息，同时删除变更应用表中change字段的check_env:0
                Map<String, Object> changeMap = prodApplication.getChange();
                if (!CommonUtils.isNullOrEmpty(changeMap)) {
                    List<AutomationEnv> automationEnvList = automationEnvService.query();
                    List<String> envList = new ArrayList<>();
                    automationEnvList.forEach(automationEnv -> {
                        envList.add(automationEnv.getEnv_name());
                    });
                    for (String env : envList) {
                        Map<String, Object> envMap = (Map<String, Object>) changeMap.get(env);
                        envMap.remove("check_env");
                    }
                }
                // 删esf同时删tag
                if (!CommonUtils.isNullOrEmpty(prodApplication.getTag())) {
                    prodApplication.setTag(null);
                }
            }
            prodApplicationDao.updateEsfFlag(prodApplication);
        } catch (Exception e) {
            logger.info("删除esf用户注册信息失败！");
            e.printStackTrace();
        }

    }

    @Override
    public List<EsfRegistration> queryEsfRegistration(Map<String, Object> req) throws Exception {
        List<EsfRegistration> esfRegistrationList = registrationDao.queryEsfRegists((String) req.get(Dict.PROD_ID));
        if (!CommonUtils.isNullOrEmpty(esfRegistrationList)) {
            for (EsfRegistration esfRegistration : esfRegistrationList) {
                Map<String, Object> queryAPP = appService.queryAPPbyid(esfRegistration.getApplication_id());
                esfRegistration.setApplication_en(queryAPP == null ? "" : (String) queryAPP.get("name_en"));
            }
        }
        return esfRegistrationList;
    }


    /**
     * 从蓝鲸平台拉取最新的yaml，更新yaml后，推送到master分支,拉取新的TAG，命名规则：当前投产窗口加上后缀-esf,举例：pro-20210914_001-001-esf
     *
     * @param application_id 应用id
     * @param esfInfo        esf用户信息
     * @param last_tag       镜像标签
     * @param prodRecord     变更记录
     */
    private void setCaasYamlByfblue(String application_id, Map<String, Object> esfInfo, String deployName, String last_tag, ProdRecord prodRecord, ProdApplication prodApplication, Map<String, Object> tempChangeMap, char caas_run_time) throws Exception {

        List<Map<String, Object>> yamls;
        try {
            yamls = appService.getNewYaml(deployName, last_tag);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{e.getMessage()});
        }
        if (!CommonUtils.isNullOrEmpty(yamls)) {
            //查询应用信息
            Map<String, Object> application = appService.queryAPPbyid(application_id);
            //查询配置文件实体
            Map<String, Object> configGitlab = prodApplicationService.findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
            Map<String, Object> changeMap = new HashMap();
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
                    logger.info("开始更新yaml文件，名称：{}", deployName + "/" + last_tag);
                    Map<String, Object> yamlMap = (Map<String, Object>) map.get("yaml");
                    Map<String, Object> specMap = (Map<String, Object>) yamlMap.get("spec");
                    if (CommonUtils.isNullOrEmpty(specMap)) {
                        continue;
                    }
                    Map<String, Object> templateMap = (Map<String, Object>) specMap.get("template");
                    if (CommonUtils.isNullOrEmpty(templateMap)) {
                        continue;
                    }
                    Map<String, Object> sunspecMap = (Map<String, Object>) templateMap.get("spec");
                    if (CommonUtils.isNullOrEmpty(sunspecMap)) {
                        continue;
                    }
                    List<Map<String, Object>> containers = (List<Map<String, Object>>) sunspecMap.get("containers");
                    if (CommonUtils.isNullOrEmpty(containers)) {
                        continue;
                    }
                    // 获取对应环境的配置中心域名
                    String config_area = "";
                    Set<String> keyset = esfInfo.keySet();
                    for (String key : keyset) {
                        if (key.length() > 4) {
                            String area = key.substring(4).toLowerCase(); //sh hf
                            Map<String, Object> esfMap = (Map<String, Object>) esfInfo.get(key);
                            Map<String, Object> configAreaMap = (Map<String, Object>) esfMap.get("config_area");
                            if (tenant.substring(0, 2).equals(area)) {
                                if ("k1".equals(tenant.substring(2, 4))) {
                                    Map<String, Object> config_area_k1 = (Map<String, Object>) configAreaMap.get("k1");
                                    config_area = (String) config_area_k1.get("config_area");
                                }
                                if ("k2".equals(tenant.substring(2, 4))) {
                                    Map<String, Object> config_area_k2 = (Map<String, Object>) configAreaMap.get("k2");
                                    config_area = (String) config_area_k2.get("config_area");
                                }
                            }
                        }
                    }

                    List<Map<String, Object>> envList = new ArrayList<>();
                    Map<String, Object> container_map = containers.get(0);
                    if (CommonUtils.isNullOrEmpty(container_map)) { // env字段缺失的情况
                        container_map = new HashMap<>();
                    }
                    envList = (List<Map<String, Object>>) container_map.get("env");
                    if (CommonUtils.isNullOrEmpty(envList)) {
                        envList = new ArrayList<>();
                        container_map.put("env", envList);
                    }
                    appendfblueYaml(envList, config_area, tenant, esfInfo, caas_run_time);
                    // 该段逻辑始终无法进入，蓝鲸始终会返回dnsPolicy、dnsConfig字段
                    if (CommonUtils.isNullOrEmpty(sunspecMap.get("dnsPolicy"))) {
                        sunspecMap.put("dnsPolicy", "None");
                        Map<String, Object> temp = new HashMap<>();
                        List<String> nameserverList = new ArrayList<>(Arrays.asList("xxx", "xxx"));
                        temp.put("nameservers", nameserverList);
                        sunspecMap.put("dnsConfig", temp);
                    } else {
                        if (!sunspecMap.get("dnsPolicy").equals("None")) {
                            sunspecMap.put("dnsPolicy", "None");
                        }
                        Map<String, Object> dnsConfig_map = (Map<String, Object>) sunspecMap.get("dnsConfig");
                        if (!CommonUtils.isNullOrEmpty(dnsConfig_map)) {
                            if (CommonUtils.isNullOrEmpty(dnsConfig_map.get("nameservers"))) {
                                Map<String, Object> temp = new HashMap<>();
                                List<String> nameserverList = new ArrayList<>(Arrays.asList("xxx", "xxx"));
                                temp.put("nameservers", nameserverList);
                                sunspecMap.put("dnsConfig", temp);
                            }
                        }
                    }
                    logger.info("更新yaml成功");
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
                    change.put("check_env", 0);
                    if (!CommonUtils.isNullOrEmpty(prodApplication) && !CommonUtils.isNullOrEmpty(prodApplication.getChange())) {
                        changeMap = prodApplication.getChange();
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
            // 给蓝鲸没有返回的proc的环境增加check_env:0
            List<AutomationEnv> automationEnvList = automationEnvService.query();
            List<String> envList = new ArrayList<>();
            automationEnvList.forEach(automationEnv -> {
                if (automationEnv.getEnv_name().contains("/PROC")) {
                    envList.add(automationEnv.getEnv_name());
                }
            });
            Map<String, Object> change = new HashMap<>();
            change.put("check_env", 0);
            for (String env : envList) {
                Map<String, Object> envMap = (Map<String, Object>) changeMap.get(env);
                if (!CommonUtils.isNullOrEmpty(envMap)) {
                    envMap.put("check_env", 0);
                } else {
                    changeMap.put(env, change);
                }
            }
            changeMap.put("/DEV/DEV", changeTemp);
            changeMap.put("/TEST/TEST", changeTemp);
            changeMap.put("/TCYZ/TCYZ", changeTemp);
            tempChangeMap.putAll(changeMap);
            prodApplicationDao.updateProdChange(prodRecord.getProd_id(), application_id, changeMap);
        }
    }

    private void appendfblueYaml(List<Map<String, Object>> envList, String config_area, String tenant, Map<String, Object> esfInfo, char caas_run_time) {

        Set<String> keys = new HashSet<>();
        for (Map<String, Object> env : envList) {
            if (env.containsKey("name")) {
                String key = (String) env.get("name");
                keys.add(key);
            }
        }
        // 若蓝鲸返回CFG_SVR_URL，SDK-GK 则先删后加
        envList.removeIf(env -> "CFG_SVR_URL".equals(env.get("name")) || "SDK-GK".equals(env.get("name")) || "RUN_TIME".equals(env.get("name")));

        // 域名
        Map<String, Object> cfg_svr_url_map = new HashMap<>();
        cfg_svr_url_map.put("name", "CFG_SVR_URL");
        cfg_svr_url_map.put("value", config_area);
        envList.add(cfg_svr_url_map);

        // SDK-GK
        Map<String, Object> sdk_gk_map = new HashMap<>();
        sdk_gk_map.put("name", "SDK-GK");
        if ("SH".equals(tenant.substring(0, 2).toUpperCase())) {
            Map procsh_map = (Map) esfInfo.get("PROCSH");
            sdk_gk_map.put("value", procsh_map.get("sdk_gk"));
        } else {
            Map prochf_map = (Map) esfInfo.get("PROCHF");
            sdk_gk_map.put("value", prochf_map.get("sdk_gk"));
        }
        envList.add(sdk_gk_map);
        if (!keys.contains("RUN_TIME")) {
            // 运行环境
            Map<String, Object> run_time_map = new HashMap<>();
            run_time_map.put("name", "RUN_TIME");
            run_time_map.put("value", caas_run_time);
            envList.add(run_time_map);
        }
        if (!keys.contains("INT_POD_IP")) {
            // 容器 Pod 的 IP
            Map<String, Object> fieldPath_map = new HashMap<>();
            fieldPath_map.put("fieldPath", "status.podIP");

            Map<String, Object> fieldRef_map = new HashMap<>();
            fieldRef_map.put("fieldRef", fieldPath_map);

            Map<String, Object> int_pod_ip_map = new HashMap<>();
            int_pod_ip_map.put("name", "INT_POD_IP");
            int_pod_ip_map.put("valueFrom", fieldRef_map);
            envList.add(int_pod_ip_map);
        }

    }

    /**
     * 从蓝鲸平台拉取最新的scc yaml，更新yaml后，推送到master分支,拉取新的TAG，命名规则：当前投产窗口加上后缀-esf,举例：pro-20210914_001-001-esf
     *
     * @param application_id 应用id
     * @param esfInfo        esf用户信息
     * @param last_tag       镜像标签
     * @param prodRecord     变更记录
     */
    private void setSccYamlByfblue(String application_id, Map<String, Object> esfInfo, String deployName, String last_tag, ProdRecord prodRecord, char scc_run_time, String scc_network_area) throws Exception {
        List<Map<String, Object>> yamls;
        try {
            yamls = appService.getSccNewYaml(deployName, last_tag);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{e.getMessage()});
        }
        if (!CommonUtils.isNullOrEmpty(yamls)) {
            //查询应用信息
            Map<String, Object> application = appService.queryAPPbyid(application_id);
            //查询配置文件实体
            Map<String, Object> configGitlab = prodApplicationService.findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
            for (Map<String, Object> map : yamls) {
                String namespace = (String) map.get("namespace_code");//租户
                // 根据租户推出要修改的fdev环境 begin
                List<AutomationEnv> automationEnvList = automationEnvService.query();
                StringBuffer sb = new StringBuffer();
                boolean flag = false;
                String env_name = null;
                for (AutomationEnv automationEnv : automationEnvList) {
                    Map<String, Object> scc_fdev_map = (Map<String, Object>) automationEnv.getScc_fdev_env_name().get(prodRecord.getType()); // scc的fdev环境
                    Map<String, Object> scc_namespace_map = (Map<String, Object>) automationEnv.getScc_namespace().get(prodRecord.getType()); // scc的租户
                    String scc_dmz_namespace = (String) scc_namespace_map.get("dmz"); // 网银网段对应的租户
                    String scc_biz_namespace = (String) scc_namespace_map.get("biz"); // 业务网段对应的租户
                    if (namespace.equals(scc_dmz_namespace)) {
                        flag = true;
                        sb.append(scc_fdev_map.get("dmz")); // 网银网段对应的fdev环境
                        env_name = automationEnv.getEnv_name();
                        break;
                    } else if (namespace.equals(scc_biz_namespace)) {
                        flag = true;
                        sb.append(scc_fdev_map.get("biz")); // 业务网段对应的fdev环境
                        env_name = automationEnv.getEnv_name();
                        break;
                    }
                }
                if (flag) {
                    // 获取对应环境的配置中心域名
                    String envName = env_name.substring(env_name.lastIndexOf("/") + 1); //环境目录DEV TEST SHK1 SHK2  HFK1 HFK2
                    String config_area = "";
                    sb.append("/").append(deployName.split("-")[0]).append("/").append(deployName).append("-scc.yaml");
                    logger.info("@@gitlab path:" + sb.toString());
                    // 根据租户推出要修改的fdev环境 end
                    // 拿到最新的yaml然后组装 begin
                    logger.info("开始组装yaml文件，名称：{}", deployName + "/" + last_tag);
                    Map<String, Object> yamlMap = (Map<String, Object>) map.get("yaml");
                    Map<String, Object> specMap = (Map<String, Object>) yamlMap.get("spec");
                    if (CommonUtils.isNullOrEmpty(specMap)) {
                        continue;
                    }
                    Map<String, Object> templateMap = (Map<String, Object>) specMap.get("template");
                    if (CommonUtils.isNullOrEmpty(templateMap)) {
                        continue;
                    }
                    Map<String, Object> sunspecMap = (Map<String, Object>) templateMap.get("spec");
                    if (CommonUtils.isNullOrEmpty(sunspecMap)) {
                        continue;
                    }
                    List<Map<String, Object>> containers = (List<Map<String, Object>>) sunspecMap.get("containers");
                    if (CommonUtils.isNullOrEmpty(containers)) {
                        continue;
                    }
                    List<Map<String, Object>> envList = new ArrayList<>();
                    Map<String, Object> container_map = containers.get(0);
                    if (CommonUtils.isNullOrEmpty(container_map)) {
                        container_map = new HashMap<>();
                    }
                    envList = (List<Map<String, Object>>) container_map.get("env");
                    if (CommonUtils.isNullOrEmpty(envList)) {
                        envList = new ArrayList<>();
                        container_map.put("env", envList);
                    }
                    appendsccfblueYaml(envList, envName, esfInfo, scc_run_time, scc_network_area);
                    logger.info("更新yaml成功");
                    Yaml yaml = new Yaml();
                    String yamlStr = yaml.dumpAsMap(map.get("yaml")).replaceAll("'######'", "");
                    appService.updateGitFile(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)),
                            URLEncoder.encode(sb.toString(), "UTF-8"), Dict.MASTER, yamlStr,
                            "替换tag包配置文件", Token);
                    logger.info("替换yaml文件成功");
                }

            }
        }
    }

    private void appendsccfblueYaml(List<Map<String, Object>> envList, String envName, Map<String, Object> esfInfo, char scc_run_time, String scc_network_area) {
        Set<String> keys = new HashSet<>();
        for (Map<String, Object> env : envList) {
            if (env.containsKey("name")) {
                String key = (String) env.get("name");
                keys.add(key);
            }
        }
        // 若蓝鲸返回了SDK-GK，则先删除，在添加
        envList.removeIf(env -> "CFG_SVR_URL".equals(env.get("name")) || "SDK-GK".equals(env.get("name")) || "INT_HOST_IP".equals(env.get("name")) || "RUN_TIME".equals(env.get("name")));

        Map<String, Object> SDK_GK_map = new HashMap<>();
        SDK_GK_map.put("name", "SDK-GK");
        if ("SH".equals(envName.substring(0, 2))) {
            Map procsh_map = (Map) esfInfo.get("PROCSH");
            SDK_GK_map.put("value", procsh_map.get("sdk_gk"));
        } else {
            Map prochf_map = (Map) esfInfo.get("PROCHF");
            SDK_GK_map.put("value", prochf_map.get("sdk_gk"));
        }
        envList.add(SDK_GK_map);

        if (!keys.contains("RUN_TIME")) {
            // 运行环境
            Map<String, Object> run_time_map = new HashMap<>();
            run_time_map.put("name", "RUN_TIME");
            run_time_map.put("value", scc_run_time);
            envList.add(run_time_map);
        }
        if (!keys.contains("INT_POD_IP")) {
            // 容器 Pod 的 IP
            Map<String, Object> fieldPath_map = new HashMap<>();
            fieldPath_map.put("fieldPath", "status.podIP");

            Map<String, Object> fieldRef_map = new HashMap<>();
            fieldRef_map.put("fieldRef", fieldPath_map);

            Map<String, Object> int_pod_ip_map = new HashMap<>();
            int_pod_ip_map.put("name", "INT_POD_IP");
            int_pod_ip_map.put("valueFrom", fieldRef_map);
            envList.add(int_pod_ip_map);
        }
        if ("Overlay".equals(scc_network_area) && !keys.contains("INT_HOST_IP")) {
            // 容器 Pod 的 IP
            Map<String, Object> fieldPath_map = new HashMap<>();
            fieldPath_map.put("fieldPath", "status.hostIP");

            Map<String, Object> fieldRef_map = new HashMap<>();
            fieldRef_map.put("fieldRef", fieldPath_map);

            Map<String, Object> int_pod_ip_map = new HashMap<>();
            int_pod_ip_map.put("name", "INT_HOST_IP");
            int_pod_ip_map.put("valueFrom", fieldRef_map);
            envList.add(int_pod_ip_map);
        }
    }

    /**
     * 发fdev-env-config模块获取应用绑定部署实体配置
     *
     * @param gitlabId
     */
    @LazyInitProperty(redisKeyExpression = "frelease.envinfo.{gitlabId}")
    public Map<String, Object> findConfigByGitlab(String gitlabId) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.GITLABID, gitlabId);
        send_map.put(Dict.REST_CODE, "queryByGitlabId");
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    /**
     * 设置fdev环境对象
     *
     * @param fdevEnv    fdev环境
     * @param configArea 配置中心域名
     */
    public Map<String, Object> setfdevEnvMap(String fdevEnv, String sccFdevEnv, String configArea, List<String> clusterList, List<String> platform, String sdfGk, String envMame) {
        Map<String, Object> param = new HashMap<>();
        param.put("env_name", envMame);
        param.put("fdev_env", fdevEnv);
        param.put("scc_fdev_env", sccFdevEnv);
        param.put("config_area", configArea);
        param.put("cluster_id", clusterList);
        param.put("platform", platform);
        param.put("sdf_gk", sdfGk);
        return param;
    }


    /**
     * 发post请求获取gk值
     */
    public String getSDK_GK() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        Map requestMap = new HashMap();
        com.alibaba.fastjson.JSONObject parse = com.alibaba.fastjson.JSONObject.parseObject(JSON.toJSONString(requestMap));
        HttpEntity<Object> request = new HttpEntity<Object>(parse, headers);
        ResponseEntity<String> result = null;
        try {
            result = restTemplate.postForEntity(sdk_gk_url, request, String.class);
        } catch (RestClientException e) {
            throw new FdevException(ErrorConstants.SDK_GK_ERROR, new String[]{e.getMessage()});
        }
        return result.getBody();
    }

    /**
     * 设置各个环境目录下yaml内容
     *
     * @param fdev_env_list fdev环境
     * @param name_en       应用英文名
     * @param configGitlab
     */
    private void setYaml(List<Map<String, Object>> fdev_env_list, String name_en, Map<String, Object> configGitlab, char caas_run_time, char scc_run_time, String scc_network_area) throws Exception {
        for (Map<String, Object> fdev_env_map : fdev_env_list) {
            String sdk = (String) fdev_env_map.get("sdf_gk");
            List<String> platform = (List<String>) fdev_env_map.get("platform");
            if (platform.contains("CAAS") && !CommonUtils.isNullOrEmpty(fdev_env_map.get("fdev_env"))) {
                StringBuffer caas_gitlab_url = new StringBuffer();
                caas_gitlab_url.append(fdev_env_map.get("fdev_env")).append("/").append(name_en.split("-")[0]).append("/").append(name_en).append(".yaml");
                String dockerYamlStr = appService.getGitFileContent(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)),
                        URLEncoder.encode(caas_gitlab_url.toString(), "UTF-8"), Dict.MASTER);
                writeDockerYaml(dockerYamlStr, caas_gitlab_url.toString(), sdk, fdev_env_map, configGitlab, caas_run_time);

            }
            if (platform.contains("SCC") && !CommonUtils.isNullOrEmpty(fdev_env_map.get("scc_fdev_env"))) {
                StringBuffer scc_gitlab_url = new StringBuffer();
                scc_gitlab_url.append(fdev_env_map.get("scc_fdev_env")).append("/").append(name_en.split("-")[0]).append("/").append(name_en).append("-scc.yaml");
                String sccYamlStr = appService.getGitFileContent(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)),
                        URLEncoder.encode(scc_gitlab_url.toString(), "UTF-8"), Dict.MASTER);
                writeSccYaml(sccYamlStr, scc_gitlab_url.toString(), sdk, fdev_env_map, configGitlab, scc_run_time, scc_network_area);
            }

        }
    }


    private void writeSccYaml(String yamlStr, String gitlab_url, String sdk, Map<String, Object> fdev_env_map, Map<String, Object> configGitlab, char scc_run_time, String scc_network_area) {

        // 开始修改yaml内容
        StringBuilder new_yaml = new StringBuilder();
        if (yamlStr.contains("\r")) {
            yamlStr.replace("\r", "");
        }
        List<String> yaml_list = new ArrayList<>(Arrays.asList(yamlStr.split("\n")));
        // 若yaml中包含env:[] 删除
        removeNullEnv(yaml_list);
        // 若包含 env(针对存量数据) 则去除
        removeStockEnv(yaml_list);
        String new_str = yaml_list.toString().replaceAll("(?:\\[|null|\\]| +)", ""); //去空格
        logger.info("==============================");
        logger.info("环境目录：" + fdev_env_map.get("env_name") + ",对应修改的fdev环境：" + fdev_env_map.get("scc_fdev_env"));
        logger.info("yaml_list:" + yaml_list);
        logger.info("new_str:" + new_str);
        logger.info("==============================");
        // 若scc平台，Underlay则去除INT_HOST_IP属性
        if ("Underlay".equals(scc_network_area) && new_str.contains("INT_HOST_IP")) {
            removeIntHostIp(yaml_list);
        }
        // 若yaml中包含-envFrom:则去除
        boolean hasEnvFormFlag = false;
        if (new_str.contains("-envFrom")) {
            hasEnvFormFlag = true;
            removeEnvFrom(yaml_list);
        }
        System.out.println("@@yaml_list: " + yaml_list.toString());
        int line = 1;
        int findFlag = 0; //默认值,有SDK-GK,标识改为1，判断sdk-gk的值不为空，替换该值
        int runTimeFlag = 0;
        for (int i = 0; i < yaml_list.size(); i++) {
            String line_content = yaml_list.get(i);
            if (line == 1) {
                new_yaml.append(line_content);
            } else {
                if (new_str.contains("SDK-GK")) {
                    if ("- name: SDK-GK".equals(line_content.trim())) {
                        findFlag = 1;
                    } else if ((findFlag == 1) && !CommonUtils.isNullOrEmpty(line_content.trim())) {
                        StringBuilder replace_str = new StringBuilder();
                        String key = line_content.split(":")[0]; //             value: IUZ7V1VFPTNHKjRQO0BLQQ==
                        replace_str = replace_str.append(StringUtils.leftPad(key + ": ", key.length() + 1) + sdk);
                        line_content = replace_str.toString();
                        yaml_list.set(i, line_content);
                        logger.info("替换后的SDK-GK值:" + line_content);
                        findFlag = 0;
                    }
                }
                // DEV,TEST环境，overlay时修改run_time值为2
                if (new_str.contains("RUN_TIME")) {
                    if ("- name: RUN_TIME".equals(line_content.trim())) {
                        runTimeFlag = 1;
                    } else if ((runTimeFlag == 1) && !CommonUtils.isNullOrEmpty(line_content.trim())) {
                        StringBuilder replace_str = new StringBuilder();
                        String key = line_content.split(":")[0]; //             value: '2'
                        replace_str = replace_str.append(StringUtils.leftPad(key + ": ", key.length() + 1) + "\'" + scc_run_time + "\'");
                        line_content = replace_str.toString();
                        yaml_list.set(i, line_content);
                        logger.info("RUN_TIME替换后的值:" + line_content);
                        runTimeFlag = 0;
                    }
                }

                new_yaml.append("\n").append(line_content);

                if (!new_str.contains("env:") && "containers:".equals(line_content.trim())) {
                    appendsccEnv(new_yaml, line_content, sdk, fdev_env_map, scc_run_time, scc_network_area, hasEnvFormFlag);
                }
                if (!new_str.contains("SDK-GK") && "- env:".equals(line_content.trim())) {
                    appendSccSdkGk(line_content, new_yaml, sdk, fdev_env_map);
                }
                if (!new_str.contains("RUN_TIME") && "- env:".equals(line_content.trim())) {
                    appendSccRunTime(line_content, new_yaml, scc_run_time);
                }
                if (!new_str.contains("INT_POD_IP") && "- env:".equals(line_content.trim())) {
                    appendSccIntPodIp(line_content, new_yaml, scc_run_time);
                }
                if ("Overlay".equals(scc_network_area) && !new_str.contains("INT_HOST_IP") && "- env:".equals(line_content.trim())) {
                    appendsccIntHostIp(line_content, new_yaml);
                }
                if (hasEnvFormFlag && "- env:".equals(line_content.trim())) {
                    appendsccEnvForm(line_content, new_yaml);
                }

            }
            line++;
        }
        logger.info("fdev环境为：" + fdev_env_map.get("fdev_env") + ",的修改后的scc yaml文件内容：" + new_yaml.toString());
        logger.info("开始修改-scc.yaml文件");
        try {
            appService.updateGitFile(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)),
                    URLEncoder.encode(gitlab_url, "UTF-8"), Dict.MASTER, new_yaml.toString(),
                    "替换tag包配置文件", Token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("修改-scc.yaml文件成功");
    }

    private void removeStockEnv(List<String> yaml_list) {
        Iterator<String> iterator = yaml_list.iterator();
        int count = 0;
        int envflag = 0;
        boolean flag = false;
        while (iterator.hasNext()) {
            count++;
            if ("env:".equals(iterator.next().trim())) {
                iterator.remove();
                flag = true;
                envflag = count;
            }
            // 删除 env下面的8行
            if (flag) {
                for(int i = 1; i<= 8; i++){
                    if(count == envflag + i){
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void appendsccEnvForm(String line_content, StringBuilder new_yaml) {
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("envFrom:");
    }

    private void removeEnvFrom(List<String> yaml_list) {
        Iterator<String> iterator = yaml_list.iterator();
        while (iterator.hasNext()) {
            if ("- envFrom:".equals(iterator.next().trim())) {
                iterator.remove();
            }
        }
    }

    private void removeNullEnv(List<String> yaml_list) {
        Iterator<String> iterator = yaml_list.iterator();
        while (iterator.hasNext()) {
            if ("env: []".equals(iterator.next().trim())) {
                iterator.remove();
            }
        }
    }

    private void removeIntHostIp(List<String> yaml_list) {
        Iterator<String> iterator = yaml_list.iterator();
        int count = 0;
        int hostIPflag = 0;
        boolean flag = false;
        while (iterator.hasNext()) {
            count++;
            if ("- name: INT_HOST_IP".equals(iterator.next().trim())) {
                flag = true;
                hostIPflag = count;
                iterator.remove();
            }
            // 删除 hostIPIndex这行及后面的4行
            if (flag) {
                if (count == hostIPflag + 1 || count == hostIPflag + 2 || count == hostIPflag + 3) {
                    iterator.remove();
                }
            }
        }
    }


    private void appendSccIntPodIp(String line_content, StringBuilder new_yaml, char scc_run_time) {
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: INT_POD_IP")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("valueFrom:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 0)).append("fieldRef:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - -2)).append("fieldPath: ").append("status.podIP");
    }

    private void appendSccRunTime(String line_content, StringBuilder new_yaml, char scc_run_time) {
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: RUN_TIME")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("value: ").append("\'" + scc_run_time + "\'");
    }

    private void appendsccIntHostIp(String line_content, StringBuilder new_yaml) {
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: INT_HOST_IP")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("valueFrom:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 0)).append("fieldRef:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - -2)).append("fieldPath: ").append("status.hostIP");
    }

    private void appendSccSdkGk(String line_content, StringBuilder new_yaml, String sdk, Map<String, Object> fdev_env_map) {
        logger.info("fdev环境为：" + fdev_env_map.get("fdev_env") + ",的yaml文件中没有找到CFG_SVR_URL、SDK-GK属性,下面开始修改-scc.yaml文件");
        logger.info("属性env左边长度：" + line_content.length());
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: SDK-GK")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("value: ").append(sdk);

    }

    private void appendsccEnv(StringBuilder new_yaml, String line_content, String sdk, Map<String, Object> fdev_env_map, char scc_run_time, String scc_network_area, boolean hasEnvFormFlag) {
        logger.info("fdev环境为：" + fdev_env_map.get("fdev_env") + ",的yaml文件中没有找到envFrom属性,下面开始修改-scc.yaml文件");
        logger.info("属性containers左边长度：" + line_content.length());
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 11)).append("- env:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: SDK-GK")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("value: ").append(sdk)
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: RUN_TIME")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("value: ").append("\'" + scc_run_time + "\'")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: INT_POD_IP")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("valueFrom:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 5)).append("fieldRef:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 3)).append("fieldPath: ").append("status.podIP");
        if ("Overlay".equals(scc_network_area)) {
            new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: INT_HOST_IP")
                    .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("valueFrom:")
                    .append("\n").append(StringUtils.leftPad("", line_content.length() - 5)).append("fieldRef:")
                    .append("\n").append(StringUtils.leftPad("", line_content.length() - 3)).append("fieldPath: ").append("status.hostIP");
        }
        if(hasEnvFormFlag){
            new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("envFrom:");
        }


    }

    private void writeDockerYaml(String yamlStr, String sb, String sdk, Map<String, Object> fdev_env_map, Map<String, Object> configGitlab, char caas_run_time) {
        // 开始修改yaml内容
        StringBuilder new_yaml = new StringBuilder();
        if (yamlStr.contains("\r")) {
            yamlStr.replace("\r", "");
        }
        List<String> yaml_list = Arrays.asList(yamlStr.split("\n"));
        String new_str = yaml_list.toString().replaceAll("(?:\\[|null|\\]| +)", ""); //去空格
        int line = 1;
        int findFlag = 0; //默认值,有SDK-GK,标识改为1，判断sdk-gk的值不为空，替换该值
        int nullFlag = 0; //默认值,有nameservers,标识改为1，因为nameservers下面有两个-null均需替换成ip，故需三个标识符来区分
        boolean appenFlag = false;
        for (int i = 0; i < yaml_list.size(); i++) {
            String line_content = yaml_list.get(i);
            if (line == 1) {
                new_yaml.append(line_content);
            } else {
                if (new_str.contains("dnsPolicy") && new_str.contains("dnsConfig")) {
                    if ("dnsPolicy".equals(line_content.trim().split(":")[0])) {
                        if (!" None".equals(line_content.trim().split(":")[1])) {
                            new_yaml.append(StringUtils.leftPad("", line_content.length())).append(" None");
                        }
                    }
                    if ("nameservers:".equals(line_content.trim())) {
                        nullFlag = 1; //
                    } else if ((nullFlag == 1 || nullFlag == 2) && "- null".equals(line_content.trim())) {
                        String replace_str = "";
                        if (nullFlag == 1) {
                            replace_str = StringUtils.leftPad("", line_content.length() - 6) + "- xxx";
                        } else if (nullFlag == 2) {
                            replace_str = StringUtils.leftPad("", line_content.length() - 6) + "- xxx";
                        }
                        line_content = replace_str;
                        logger.info("替换后的-null:" + line_content);
                        yaml_list.set(i, line_content);
                        nullFlag = 2;
                    } else {
                        nullFlag = 0;
                    }
                }
                if (new_str.contains("CFG_SVR_URL") && new_str.contains("SDK-GK")) {
                    logger.info("fdev环境为：" + fdev_env_map.get("fdev_env") + ",的yaml文件中有CFG_SVR_URL、SDK-GK属性,下面开始修改yaml文件");
                    if ("- name: CFG_SVR_URL".equals(line_content.trim())) {
                        findFlag = 1;
                    } else if ("- name: SDK-GK".equals(line_content.trim())) {
                        findFlag = 2;
                    } else if ((findFlag == 1) && !CommonUtils.isNullOrEmpty(line_content.trim())) {
                        StringBuilder replace_str = new StringBuilder();
                        String key = line_content.split(":")[0]; //value: http://esfconfdmzhfnbent1.spdb.com:2222,http://esfconfdmzhfnbent2.spdb.com:2222
                        replace_str = replace_str.append(StringUtils.leftPad(key + ": ", key.length() + 1) + fdev_env_map.get("config_area"));
                        line_content = replace_str.toString();
                        yaml_list.set(i, line_content);
                        logger.info("替换后的CFG_SVR_URL值:" + line_content);
                        findFlag = 0;
                    } else if ((findFlag == 2) && !CommonUtils.isNullOrEmpty(line_content.trim())) {
                        StringBuilder replace_str = new StringBuilder();
                        String key = line_content.split(":")[0]; //             value: IUZ7V1VFPTNHKjRQO0BLQQ==
                        replace_str = replace_str.append(StringUtils.leftPad(key + ": ", key.length() + 1) + sdk);
                        line_content = replace_str.toString();
                        yaml_list.set(i, line_content);
                        logger.info("替换后的SDK-GK值:" + line_content);
                        findFlag = 0;
                    }

                }
                new_yaml.append("\n").append(line_content);
                if (!new_str.contains("dnsPolicy") && "spec:".equals(line_content.trim())) {
                    appendDnsPolicy(line_content, new_yaml, fdev_env_map);
                }
                if (!new_str.contains("env:") && "containers:".equals(line_content.trim())) {
                    appendEnv(line_content, new_yaml, sdk, fdev_env_map, caas_run_time);
                }
                if (!new_str.contains("CFG_SVR_URL") && !new_str.contains("SDK-GK") && "env:".equals(line_content.trim())) {
                    appendCfgSvrUrlAndSdkGk(line_content, new_yaml, sdk, fdev_env_map);
                }
                if (!new_str.contains("RUN_TIME") && !new_str.contains("INT_POD_IP") && "env:".equals(line_content.trim())) {
                    appendRunTimeAndIntpodip(line_content, new_yaml, caas_run_time);
                }
            }
            line++;
        }
        logger.info("fdev环境为：" + fdev_env_map.get("fdev_env") + ",的修改后的yaml文件内容：" + new_yaml);
        logger.info("开始修改yaml文件");
        try {
            appService.updateGitFile(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)),
                    URLEncoder.encode(sb, "UTF-8"), Dict.MASTER, new_yaml.toString(),
                    "替换tag包配置文件", Token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("修改yaml文件成功");
    }

    private void appendRunTimeAndIntpodip(String line_content, StringBuilder new_yaml, char caas_run_time) {
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: RUN_TIME")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("value: ").append("\'" + caas_run_time + "\'")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: INT_POD_IP")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("valueFrom: ")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 0)).append("fieldRef: ")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - -2)).append("fieldPath: ").append("status.podIP");
    }


    private void appendDnsPolicy(String line_content, StringBuilder new_yaml, Map<String, Object> fdev_env_map) {
        logger.info("fdev环境为：" + fdev_env_map.get("fdev_env") + ",的yaml文件中没有找到dnsPolicy、dnsConfig属性,下面开始修改yaml文件");
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 3)).append("dnsPolicy: None")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 3)).append("dnsConfig:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 1)).append("nameservers:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 1)).append("- xxx")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 1)).append("- xxx");
    }

    private void appendCfgSvrUrlAndSdkGk(String line_content, StringBuilder new_yaml, String sdk, Map<String, Object> fdev_env_map) {
        logger.info("属性env左边长度：" + line_content.length());
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: CFG_SVR_URL")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("value: ").append(fdev_env_map.get("config_area"))
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 4)).append("- name: SDK-GK")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 2)).append("value: ").append(sdk);
    }

    /**
     * yaml中没有env:属性，需追加
     */
    private void appendEnv(String line_content, StringBuilder new_yaml, String sdk, Map<String, Object> fdev_env_map, char caas_run_time) {
        logger.info("fdev环境为：" + fdev_env_map.get("fdev_env") + ",的yaml文件中没有找到env属性,下面开始修改yaml文件");
        logger.info("属性containers左边长度：" + line_content.length());
        new_yaml.append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("env:")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: CFG_SVR_URL")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("value: ").append(fdev_env_map.get("config_area"))
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: SDK-GK")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("value: ").append(sdk)
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: RUN_TIME")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("value: ").append("\'" + caas_run_time + "\'")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 9)).append("- name: INT_POD_IP")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 7)).append("valueFrom: ")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 5)).append("fieldRef: ")
                .append("\n").append(StringUtils.leftPad("", line_content.length() - 3)).append("fieldPath: status.podIP");
    }


    /**
     * 通过环境目录去环境表获取fdev环境列表
     *
     * @param type     变更类型
     * @param network  网段
     * @param platform 平台
     * @param esfInfo  用户注册信息
     */
    private List<Map<String, Object>> getFdevEnvByEnvName(String type, String network, List<String> platform, Map<String, Object> esfInfo) {
        List<Map<String, Object>> fdev_env_list = new ArrayList<>();
        Set<String> keyset = esfInfo.keySet();
        String proc_sdk_gk = null;
        Map<String, Object> proc_map = (Map<String, Object>) esfInfo.get("PROCSH");
        if (!CommonUtils.isNullOrEmpty(proc_map)) {
            proc_sdk_gk = (String) proc_map.get("sdk_gk");
            if (CommonUtils.isNullOrEmpty(proc_sdk_gk)) {
                proc_sdk_gk = getSDK_GK();
            }
        }
        for (String key : keyset) { // 遍历环境目录
            Map<String, Object> esfMap = (Map<String, Object>) esfInfo.get(key);
            Map<String, Object> configAreaMap = new HashMap<>();
            Map<String, String> configArea_k1_map = new HashMap<>();
            Map<String, String> configArea_k2_map = new HashMap<>();
            String configArea_k1 = null;
            String configArea_k2 = null;
            List<String> clusterList = new ArrayList<>();
            // 添加esf caas平台用户可以选择域名及集群
            if (platform.contains("CAAS")) {
                configAreaMap = (Map<String, Object>) esfMap.get("config_area");
                configArea_k1_map = (Map<String, String>) configAreaMap.get("k1");
                configArea_k2_map = (Map<String, String>) configAreaMap.get("k2");
                configArea_k1 = configArea_k1_map.get("config_area");
                if (!"DEV".equals(key) && !"TEST".equals(key)) {
                    configArea_k2 = configArea_k2_map.get("config_area");
                }

            }
            String caas_fdev_env = "";
            String scc_fdev_env = "";
            String content = key;
            if (key.length() > 4) { // PROCSH PROCHF
                content = key.substring(0, 4);
            }
            List<AutomationEnv> automationEnvList = automationParamDao.queryByEnvName(content, platform);
            if ("DEV".equals(key)) {
                String sdkGk = getSDK_GK();
                Map<String, Object> map = (Map<String, Object>) esfInfo.get(key);
                map.put("sdk_gk", sdkGk);
                if (platform.contains("CAAS")) {
                    Map<String, Object> fdev_env_name = automationEnvList.get(0).getFdev_env_name();
                    Map<String, Object> typeMap = (Map<String, Object>) fdev_env_name.get(type);
                    caas_fdev_env = (String) typeMap.get(network);
                }
                if (platform.contains("SCC")) {
                    Map<String, Object> scc_fdev_env_name = automationEnvList.get(0).getScc_fdev_env_name();
                    Map<String, Object> typeMap = (Map<String, Object>) scc_fdev_env_name.get(type);
                    scc_fdev_env = (String) typeMap.get(network);
                }
                fdev_env_list.add(setfdevEnvMap(caas_fdev_env, scc_fdev_env, configArea_k1, clusterList, platform, sdkGk, key));
            }
            if ("TEST".equals(key)) {
                String sdkGk = getSDK_GK();
                Map<String, Object> map = (Map<String, Object>) esfInfo.get(key);
                map.put("sdk_gk", sdkGk);
                if (platform.contains("CAAS")) {
                    Map<String, Object> fdev_env_name = automationEnvList.get(0).getFdev_env_name();
                    Map<String, Object> typeMap = (Map<String, Object>) fdev_env_name.get(type);
                    caas_fdev_env = (String) typeMap.get(network);
                }
                if (platform.contains("SCC")) {
                    Map<String, Object> scc_fdev_env_name = automationEnvList.get(0).getScc_fdev_env_name();
                    Map<String, Object> typeMap = (Map<String, Object>) scc_fdev_env_name.get(type);
                    scc_fdev_env = (String) typeMap.get(network);
                }
                fdev_env_list.add(setfdevEnvMap(caas_fdev_env, scc_fdev_env, configArea_k1, clusterList, platform, sdkGk, key));
            }
            if ("PROCSH".equals(key)) {
                Map<String, Object> map = (Map<String, Object>) esfInfo.get(key);
                map.put("sdk_gk", proc_sdk_gk);
                for (AutomationEnv automationEnv : automationEnvList) {
                    String env_name = automationEnv.getEnv_name();
                    if ("/PROC/SHK1".equals(env_name)) {
                        if (platform.contains("CAAS")) {
                            Map<String, Object> fdev_env_name = automationEnv.getFdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) fdev_env_name.get(type);
                            caas_fdev_env = (String) typeMap.get(network);
                        }
                        if (platform.contains("SCC")) {
                            Map<String, Object> scc_fdev_env_name = automationEnv.getScc_fdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) scc_fdev_env_name.get(type);
                            scc_fdev_env = (String) typeMap.get(network);
                        }
                        fdev_env_list.add(setfdevEnvMap(caas_fdev_env, scc_fdev_env, configArea_k1, clusterList, platform, proc_sdk_gk, env_name));
                    }
                    if ("/PROC/SHK2".equals(env_name)) {
                        if (platform.contains("CAAS")) {
                            Map<String, Object> fdev_env_name = automationEnv.getFdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) fdev_env_name.get(type);
                            caas_fdev_env = (String) typeMap.get(network);
                        }
                        if (platform.contains("SCC")) {
                            Map<String, Object> scc_fdev_env_name = automationEnv.getScc_fdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) scc_fdev_env_name.get(type);
                            scc_fdev_env = (String) typeMap.get(network);
                        }
                        fdev_env_list.add(setfdevEnvMap(caas_fdev_env, scc_fdev_env, configArea_k2, clusterList, platform, proc_sdk_gk, env_name));
                    }
                }
            }
            if ("PROCHF".equals(key)) {
                Map<String, Object> map = (Map<String, Object>) esfInfo.get(key);
                map.put("sdk_gk", proc_sdk_gk);
                for (AutomationEnv automationEnv : automationEnvList) {
                    String env_name = automationEnv.getEnv_name();
                    if ("/PROC/HFK1".equals(env_name)) {
                        if (platform.contains("CAAS")) {
                            Map<String, Object> fdev_env_name = automationEnv.getFdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) fdev_env_name.get(type);
                            caas_fdev_env = (String) typeMap.get(network);
                        }
                        if (platform.contains("SCC")) {
                            Map<String, Object> scc_fdev_env_name = automationEnv.getScc_fdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) scc_fdev_env_name.get(type);
                            scc_fdev_env = (String) typeMap.get(network);
                        }
                        fdev_env_list.add(setfdevEnvMap(caas_fdev_env, scc_fdev_env, configArea_k1, clusterList, platform, proc_sdk_gk, env_name));
                    }
                    if ("/PROC/HFK2".equals(env_name)) {
                        if (platform.contains("CAAS")) {
                            Map<String, Object> fdev_env_name = automationEnv.getFdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) fdev_env_name.get(type);
                            caas_fdev_env = (String) typeMap.get(network);
                        }
                        if (platform.contains("SCC")) {
                            Map<String, Object> scc_fdev_env_name = automationEnv.getScc_fdev_env_name();
                            Map<String, Object> typeMap = (Map<String, Object>) scc_fdev_env_name.get(type);
                            scc_fdev_env = (String) typeMap.get(network);
                        }
                        fdev_env_list.add(setfdevEnvMap(caas_fdev_env, scc_fdev_env, configArea_k2, clusterList, platform, proc_sdk_gk, env_name));
                    }
                }
            }
        }
        return fdev_env_list;
    }

    /**
     * 从远程git拉取新分支
     *
     * @param new_tag      新tag名称
     * @param configGitlab
     */
    private void pullNewBranch(String new_tag, Map<String, Object> configGitlab) throws Exception {
        logger.info("开始生成新的tag，名称：当前窗口+esf");
        gitlabService.createTag(String.valueOf(configGitlab.get(Dict.CONFIGGITLABID)), new_tag, Dict.MASTER, Token);
        logger.info("生成新的tag成功");
    }

    @Override
    public List<Map<String, Object>> queryApps(Map<String, Object> req) throws Exception {
        // 调应用模块查全量应用
        List<Map<String, Object>> allApps = appService.getApps();
        // 当前变更下所有添加的esf用户信息
        List<EsfRegistration> esfList = queryEsfRegistration(req);
        if (CommonUtils.isNullOrEmpty(esfList)) {
            return allApps;
        }
        List<String> esf_app = esfList.stream().map(EsfRegistration::getApplication_id).collect(Collectors.toList());
        return allApps.stream().filter(app -> !esf_app.contains(app.get(Dict.ID))).collect(Collectors.toList());
    }


}
