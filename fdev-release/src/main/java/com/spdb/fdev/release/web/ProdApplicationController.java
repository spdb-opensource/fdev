package com.spdb.fdev.release.web;

import com.csii.pe.redis.util.Util;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.DeployTypeEnum;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.dao.IEsfRegistrationDao;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.dao.IProdAssetDao;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "变更应用接口")
@RequestMapping("/api/release")
@RestController
@RefreshScope
public class ProdApplicationController implements InitializingBean {

    @Value("${fdev.autorelease.dmz.env.product.mapping}")
    private String dmzEnvProductMappingString;

    @Value("${fdev.autorelease.dmz.env.gray.mapping}")
    private String dmzEnvGrayMappingString;

    @Value("${fdev.autorelease.biz.env.product.mapping}")
    private String bizEnvProductMappingString;

    @Value("${fdev.autorelease.biz.env.gray.mapping}")
    private String bizEnvGrayMappingString;

    @Value("${prod.dir}")
    private String prod_dir;

    private Map<String, String> dmzEnvProductMapping;

    private Map<String, String> dmzEnvGrayMapping;

    private Map<String, String> bizEnvProductMapping;

    private Map<String, String> bizEnvGrayMapping;

    @Autowired
    IProdApplicationService prodApplicationService;
    @Autowired
    IRoleService roleService;
    @Autowired
    IProdRecordService prodRecordService;
    @Autowired
    IProdAssetService prodAssetService;
    @Autowired
    IReleaseCatalogService catalogService;
    @Autowired
    IAppService appService;
    @Autowired
    IReleaseApplicationService releaseApplicationService;
    @Autowired
    IGitlabService gitlabService;
    @Autowired
    IAuditRecordService auditRecordService;
    @Autowired
    IRelDevopsRecordService relDevopsRecordService;
    @Autowired
    IApplicationImageService applicationImageService;
    @Autowired
    IProdConfigService prodConfigService;

    @Autowired
    private IPushImageService pushImageService;
    @Autowired
    private IProdApplicationDao prodApplicationDao;
    @Autowired
    private IEsfRegistrationDao esfRegistrationDao;
    @Autowired
    private IProdAssetDao prodAssetDao;
    //生产状态
    private List<String> dmzProductStatus;
    //灰度状态
    private List<String> dmzGrayStatus;

    //生产状态
    private List<String> bizProductStatus;
    //灰度状态
    private List<String> bizGrayStatus;

    private Logger logger = LoggerFactory.getLogger(ProdApplicationController.class);

    @RequestValidate(NotEmptyFields = { Dict.PROD_ID, Dict.TEMPLATE_ID })
    @PostMapping(value = "/setTemplate")
    public JsonResult setTemplate(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String prod_id = requestParam.get(Dict.PROD_ID);
        String template_id = requestParam.get(Dict.TEMPLATE_ID);
        prodRecordService.setTemplate(prod_id, template_id,application_id);
        return JsonResultUtil.buildSuccess(null);
    }

    @OperateRecord(operateDiscribe="投产模块-添加变更应用")
    @RequestValidate(NotEmptyFields = { Dict.APPLICATION_ID, Dict.PROD_ID})
    @PostMapping(value = "/addApplication")
    public JsonResult addApplication(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String prod_id = (String) requestParam.get(Dict.PROD_ID);
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        List<String> deploy_type = (List<String>) requestParam.get(Dict.DEPLOY_TYPE);
        String release_type = (String) requestParam.get(Dict.RELEASE_TYPE);
        try {
            ProdApplication queryProdApplication = prodApplicationService.queryApplication(prod_id, application_id);
            if (!CommonUtils.isNullOrEmpty(queryProdApplication)) {
                throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "请勿重复添加应用" });
            }
        } catch (FdevException e) {
            if (!ErrorConstants.DATA_NOT_EXIST.equals(e.getCode())) {
                throw e;
            }
        }
        
        roleService.isGroupReleaseManagerByProd(prod_id);
        Map<String, Object> application = appService.queryAPPbyid(application_id); // 根据应用id查询应用详情
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        String image_deliver_type = prodRecord.getImage_deliver_type();
        if(Constants.IMAGE_DELIVER_TYPE_NEW.equals(image_deliver_type)){
            String template_id = prodRecord.getTemplate_id();
            List<AssetCatalog> catalogs = catalogService.query(template_id);
            if(CommonUtils.isNullOrEmpty(catalogs)) {
                throw new FdevException(ErrorConstants.PROD_TEMP_NOT_EXIST_PROD_APPLICATION);
            }
            boolean flag = false;
            //添加变更应用时若对应变更模板没有类型为1或2，报错“当前变更模板无变更应用目录，无法添加变更应用，请更换变更模板后添加变更应用！”
            for (AssetCatalog assetCatalog : catalogs) {
                if (assetCatalog.getCatalog_type().equals(Constants.CATALOG_TYPE_NORMAL)
                        || assetCatalog.getCatalog_type().equals(Constants.CATALOG_TYPE_MICROSERIVICE)) {
                    flag = true;
                    break;
                }
            }
            if(!flag) {
                throw new FdevException(ErrorConstants.PROD_TEMP_NOT_EXIST_PROD_APPLICATION);
            }
        }
        Map configMap = new HashMap();
        String pro_image_uri = null;
        String pro_scc_image_uri = null;
        String webUrl = null;
        Boolean configFlag = false;

        ProdConfigApp ConfigResult = prodConfigService.queryConfigs(application_id,prodRecord.getRelease_node_name());
        //查询是否是配置文件变更进来的
        if((Constants.APPLICATION_DOCKER_STOP_START_ALL.equals(release_type)
                ||Constants.APPLICATION_DOCKER_RESTART.equals(release_type))
                        && !CommonUtils.isNullOrEmpty(ConfigResult)){
           configFlag = true;
           configMap =  setConfigImageTag(prodRecord,application_id,ConfigResult.getTag());
           webUrl = (String) configMap.get(Dict.COMPARE_URL);
           pro_image_uri = (String) configMap.get(Dict.PRO_IMAGE_URI);
        }else{
            if(Constants.APPLICATION_DOCKER_ALL.equals(release_type) || Constants.APPLICATION_DOCKER_STOP_START_ALL.equals(release_type)
                    || Constants.APPLICATION_DOCKER_RESTART.equals(release_type)) { //微服务单集群停止|停止后重启|服务重启
                Map<String,String> lastTagMap = new HashMap<>();
                String tag = null;
                for(String type : deploy_type){
                    String lastTag = prodApplicationService.findLastReleaseUri(application_id, prodRecord, type);
                    if(CommonUtils.isNullOrEmpty(lastTag)){
                        throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行重启操作！"});
                    }
                    tag = lastTag.split(":")[1];
                    lastTagMap.put(type,lastTag);
                }
                // 查询应用所有部署过的平台，未部署过无法停止后重启
                HashSet<String> allDeployedType = prodApplicationService.findAllDeployedType(application_id, prodRecord);
                deploy_type.forEach(deploy -> {
                	if(!allDeployedType.contains(deploy)){
                		throw new FdevException(ErrorConstants.DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行重启操作！"});
                	}
                });
                if(!CommonUtils.isNullOrEmpty(lastTagMap)) {
                    pro_image_uri = lastTagMap.get(DeployTypeEnum.CAAS.getType());
                    pro_scc_image_uri = lastTagMap.get(DeployTypeEnum.SCC.getType());
                }
//                webUrl = addCommonConfig(prodRecord, application, tag);
            } else {
                ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(application_id,
                        prodRecord.getRelease_node_name());
                if(!CommonUtils.isNullOrEmpty(releaseApplication.getFdev_config_confirm())
                        && !"1".equals(releaseApplication.getFdev_config_confirm())) {
                    throw new FdevException(ErrorConstants.CONFIG_FILE_WITHOUT_AUDIT);
                }
            }
        }
        ProdApplication prodApplication = new ProdApplication();
        prodApplication.setApplication_id(application_id);
        prodApplication.setProd_id(prod_id);
        prodApplication.setRelease_type(release_type);
        prodApplication.setContainer_num("X");
        prodApplication.setPro_image_uri(pro_image_uri);
        prodApplication.setPro_scc_image_uri(pro_scc_image_uri);
        // 如果是老模板则不返回scc部署平台
        if (prodRecord.getScc_prod().equals("0") &&  deploy_type.contains("SCC")){
            deploy_type.removeIf(s -> s.contains("SCC"));
        }
        // 新增逻辑：判断在配置文件更新esf目录添加过该应用，且有数据，设置esf_flag = 1, esf的部署平台
        List<EsfRegistration> esfRegistrationList = esfRegistrationDao.queryEsfRegistsById(prod_id,application_id);
        if (!CommonUtils.isNullOrEmpty(esfRegistrationList) && esfRegistrationList.size()>=1) {
            List<String> platforms = esfRegistrationList.get(0).getPlatform();
            prodApplication.setEsf_flag("1");
            prodApplication.setEsf_platform(platforms);
        } else {
            prodApplication.setEsf_flag("0");
            prodApplication.setEsf_platform(Arrays.asList(""));
        }

        prodApplication.setDeploy_type(deploy_type);
        if(!CommonUtils.isNullOrEmpty(webUrl)) {
            if(configFlag){
                prodApplication.setCompare_url(webUrl);
                prodApplication.setFdev_config_changed((Boolean) configMap.get(Dict.FDEV_CONFIG_CHANGED));
                prodApplication.setFdev_config_confirm((String) configMap.get(Dict.FDEV_CONFIG_CONFIRM));
                prodApplication.setTag((String) configMap.get(Dict.TAG));
            }else{
                prodApplication.setCompare_url(webUrl);
                prodApplication.setFdev_config_changed(true);
                prodApplication.setFdev_config_confirm("1");
                String tag;
                if(CommonUtils.isNullOrEmpty(pro_image_uri)){
                    tag = pro_scc_image_uri.split(":")[1];
                }else{
                    tag = pro_image_uri.split(":")[1];
                }
                prodApplication.setTag(tag);
            }
        }
        List<String> prod_dir = new ArrayList<>();
        //判断变更应用是否为新增
        HashSet<String> hasDeployType = prodApplicationService.findAllDeployedType(application_id, prodRecord); // 已经部署过的平台
        if(!hasDeployType.contains("CAAS")){
            prodApplication.setCaas_add_sign("1"); // 新增
        } else {
            prodApplication.setCaas_add_sign("0"); // 非新增
        }
        if(!hasDeployType.contains("SCC")){
            prodApplication.setScc_add_sign("1"); // 新增
        } else {
            prodApplication.setScc_add_sign("0"); // 非新增
        }
        if(deploy_type.contains("CAAS") ){
            if(!hasDeployType.contains("CAAS")){ //未部署过cass
                prod_dir.add(Dict.DOCKER_YAML);
            } else { // 部署过caas
                if(Constants.SERVICE_ASYN_UPDATE.equals(release_type)){ // 微服务镜像异步更新
                    prod_dir.add(Dict.DOCKER_LOWERCASE);
                } else if(Constants.SERVICE_SYN_UPDATE.equals(release_type)){ // K1、K2镜像同步更新 => 更改为：单集群停止
                    prod_dir.add(Dict.DOCKER_STOP); //单集群停止
                } else if(Constants.SERVICE_STOP_AND_START.equals(release_type)){ // 停止后重启
                    prod_dir.add(Dict.DOCKER_STOPALL);
                    prod_dir.add(Dict.DOCKER_STARTALL);
                } else if(Constants.SERVICE_RESTART.equals(release_type)){ // 服务重启
                    prod_dir.add(Dict.DOCKER_RESTART);
                }
            }
        }
        // mspmk-cli前端应用介质目录不包含scc相关目录
        String name_en = (String) application.get(Dict.NAME_EN);
        if(deploy_type.contains("SCC") && !name_en.startsWith("mspmk-cli")){
            if(!hasDeployType.contains("SCC")){
                prod_dir.add(Dict.SCC_YAML);
            }
            //部署过scc且不为老模板才设置scc目录
            if(hasDeployType.contains("SCC") && !prodRecord.getScc_prod().equals("0")) {
                if(Constants.SERVICE_ASYN_UPDATE.equals(release_type)){ // 微服务镜像异步更新
                    prod_dir.add(Dict.SCC);
                } else if(Constants.SERVICE_SYN_UPDATE.equals(release_type)){ // K1、K2镜像同步更新 =>更改为：单集群停止
                    prod_dir.add(Dict.SCC_STOP); //单集群停止
                } else if(Constants.SERVICE_STOP_AND_START.equals(release_type)){ // 停止后重启
                    prod_dir.add(Dict.SCC_STOPALL);
                    prod_dir.add(Dict.SCC_STARTALL);
                } else if(Constants.SERVICE_RESTART.equals(release_type)){ // 服务重启
                    prod_dir.add(Dict.SCC_RESTART);
                }
            }
        }
        prodApplication.setProd_dir(prod_dir);
        // 变更类型:微服务应用重启:docker_startall,发fbule获取生产上的副本数
        // 修改逻辑：获取由前端上送确认修改后的副本数入库
        if(prod_dir.contains(Dict.DOCKER_STARTALL) || prod_dir.contains(Dict.SCC_STARTALL)){
            Map<String,Object> change = (Map<String, Object>) requestParam.get(Dict.CHANGE);
            prodApplication.setChange(change);
        }
        // 变更类型：单集群停止 docker_stop,scc_stop 获取需要停止的集群 SHK1,SHK2,HFK1,HFK2 , 灰度没有K2
        if(prod_dir.contains(Dict.DOCKER_STOP)){
            prodApplication.setCaas_stop_env((List<String>) requestParam.get("caas_stop_env"));
        }
        if(prod_dir.contains(Dict.SCC_STOP)){
            prodApplication.setScc_stop_env((List<String>) requestParam.get("scc_stop_env"));
        }
        prodApplication = prodApplicationService.addApplication(prodApplication);
        return JsonResultUtil.buildSuccess(prodApplication);
    }

    private String addCommonConfig(ProdRecord prodRecord, Map<String, Object> application, String last_tag) throws Exception {
        String webUrl = null;
        if(!CommonUtils.isNullOrEmpty(application) && !CommonUtils.isNullOrEmpty(application.get(Dict.GITLAB_PROJECT_ID))
                && !Dict.SPDB_CLI_MOBCLI.equals(application.get(Dict.NAME_EN))
                && !((String) application.get(Dict.NAME_EN)).startsWith(Dict.MSPMK_CLI_)) {
            // 根据应用gitlab的id查询应用绑定部署实体配置
            Map<String, Object> configGitlab = prodApplicationService.findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
            // 实体配置的gitlab的id
            String configGitlabId = prodApplicationService.checkEnvConfig(configGitlab, last_tag);
            // 部署实体配置是否为空
            if (!CommonUtils.isNullOrEmpty(configGitlabId)) {
                webUrl = gitlabService.findWebUrlByGitlabId(configGitlabId);
                Set<String> set = gitlabService.queryResourceFiles(configGitlabId, last_tag);
                String network = (String) application.get(Dict.NETWORK);
                List<String> standardEnvList;
                Map<String, String> standardEnvMapping;
                if(!CommonUtils.isNullOrEmpty(network) && network.contains("biz") && !network.contains("dmz")) {
                    standardEnvList = Dict.GRAY.equals(prodRecord.getType()) ? this.bizGrayStatus : this.bizProductStatus;
                    standardEnvMapping = Dict.GRAY.equals(prodRecord.getType()) ?
                            this.bizEnvGrayMapping : this.bizEnvProductMapping;
                } else {
                    standardEnvList = Dict.GRAY.equals(prodRecord.getType()) ? this.dmzGrayStatus : this.dmzProductStatus;
                    standardEnvMapping = Dict.GRAY.equals(prodRecord.getType()) ?
                            this.dmzEnvGrayMapping : this.dmzEnvProductMapping;
                }
                List<String> envList = new ArrayList<>();
                for(String envSingle : set) {
                    envList.add(envSingle.split("/")[0]);
                }
                if(!envList.containsAll(standardEnvList)) {
                    StringBuilder sb = new StringBuilder();
                    for(String env : standardEnvList) {
                        if(!envList.contains(env)) {
                            sb.append(env).append("、");
                        }
                    }
                    String lackEnv = sb.toString();
                    if(lackEnv.endsWith("、")) {
                        lackEnv = lackEnv.substring(0, lackEnv.length() - 1);
                    }
                    if(!"PROCHF".equals(standardEnvMapping.get(lackEnv))) {
                        //环境不满足，抛出异常
                        throw new FdevException(ErrorConstants.RELEASE_LACK_FDEV_CONFIG,
                                new String[] { lackEnv, webUrl + "/tree/" + last_tag });
                    }
                }

                for(String branchFile : set) {
                    String env = branchFile.split("/")[0];
                    if(CommonUtils.isNullOrEmpty(standardEnvMapping.get(env)) || !branchFile.endsWith(".properties")) {
                        continue;
                    }
                    ProdAsset prodAsset = new ProdAsset(webUrl + "/blob/" + last_tag + "/" + branchFile,
                            branchFile.substring(branchFile.lastIndexOf("/") + 1),
                            prodRecord.getRelease_node_name(), Dict.COMMONCONFIG, Constants.SOURCE_FDEV,
                            CommonUtils.getSessionUser().getId(), prodRecord.getProd_id(),
                            (String)application.get(Dict.ID), (String) application.get(Dict.NAME_ZH), configGitlabId);

                    if(!CommonUtils.isNullOrEmpty(standardEnvMapping.get(env))) {
                        prodAsset.setRuntime_env(standardEnvMapping.get(env));
                    }
                    prodAssetService.addGitlabAsset(prodAsset);
                }
            }
        }
        return webUrl;
    }

    @RequestValidate(NotEmptyFields = { Dict.PROD_ID })
    @PostMapping(value = "/queryApplications")
    public JsonResult queryApplications(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        String release_type = requestParam.get(Dict.RELEASE_TYPE);
        List<Map> applications = prodApplicationService.queryApplications(new ProdApplication(prod_id, release_type));
        return JsonResultUtil.buildSuccess(applications);
    }
    
    /**
     * 根据应用id查询曾部署过的平台
     * */
    @RequestValidate(NotEmptyFields = { Dict.APPLICATION_ID, Dict.PROD_ID })
    @PostMapping(value = "/queryDeployTypeByAppId")
    public JsonResult queryDeployTypeByAppId(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String applicationId = requestParam.get(Dict.APPLICATION_ID);
        String prodId = requestParam.get(Dict.PROD_ID);
        Map<String, Object> resuleMap = prodApplicationService.queryDeployTypeByAppId(applicationId, prodId);
        return JsonResultUtil.buildSuccess(resuleMap);
    }

    @OperateRecord(operateDiscribe="投产模块-删除")
    @RequestValidate(NotEmptyFields = { Dict.APPLICATION_ID, Dict.PROD_ID })
    @PostMapping(value = "/deleteApplication")
    public JsonResult deleteApplication(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        roleService.isGroupReleaseManagerByProd(prod_id);
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        // 删除变更配置文件若涉及到有序文件则删除后对剩余的变更文件seq_no进行重新排序
        List<ProdAsset> prodAssetList = prodAssetDao.findAssetByAppAndProd(application_id, prod_id);
        if(!CommonUtils.isNullOrEmpty(prodAssetList)){
            for(ProdAsset prodAsset: prodAssetList){
                reSortProdAsset(prodAsset);
            }
        }
        prodApplicationService.deleteApplication(prod_id, application_id);
        // 同时将fdev配置文件从prod_asset表中删除
        prodAssetService.delCommonConfigByAppAndProd(application_id, prod_id);
        // 同时删除镜像推送列表
        applicationImageService.delete(prod_id, application_id);
        return JsonResultUtil.buildSuccess(null);
    }

    private void reSortProdAsset(ProdAsset prodAsset) throws Exception {
        String DelSeqNo = prodAsset.getSeq_no();
        String asset_catalog_name = prodAsset.getAsset_catalog_name();
        prodAssetService.deleteAsset(prodAsset);
        List<ProdAsset> seqNo = prodAssetService.queryAssetsList(prodAsset.getProd_id());
        for (ProdAsset asset : seqNo) {
            if (!CommonUtils.isNullOrEmpty(asset.getSeq_no()) && asset_catalog_name.equals(asset.getAsset_catalog_name())) {
                if (Integer.parseInt(DelSeqNo) < Integer.parseInt(asset.getSeq_no())) {
                    asset.setSeq_no((Integer.parseInt(asset.getSeq_no()) - 1) + "");
                    prodAssetService.updateAssetSeqNo(asset);
                }
            }
        }
    }

    @OperateRecord(operateDiscribe="投产模块-选择镜像版本")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.PROD_ID})
    @PostMapping(value = "/setImageTag")
    public JsonResult setApplicationImageTag(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.putAll(requestParam);
        param.put("bizGrayStatus",this.bizGrayStatus);
        param.put("bizProductStatus",this.bizProductStatus);
        param.put("bizEnvGrayMapping",this.bizEnvGrayMapping);
        param.put("bizEnvProductMapping",this.bizEnvProductMapping);
        param.put("dmzGrayStatus",this.dmzGrayStatus);
        param.put("dmzProductStatus",this.dmzProductStatus);
        param.put("dmzEnvGrayMapping",this.dmzEnvGrayMapping);
        param.put("dmzEnvProductMapping",this.dmzEnvProductMapping);
        return JsonResultUtil.buildSuccess(pushImageService.pushImage(param));
    }



    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.PROD_ID})
    @PostMapping(value = "/confirmConfigChanges")
    public JsonResult confirmConfigChanges(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String pro_image_uri = requestParam.get(Dict.PRO_IMAGE_URI);
        String pro_scc_image_uri = requestParam.get(Dict.PRO_SCC_IMAGE_URI);
        if (!roleService.isGroupReleaseManager(CommonUtils.getSessionUser().getGroup_id())
                && !roleService.isAppSpdbManager(application_id)
                && !roleService.isApplicationManager(application_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "此操作为应用级别,没有操作权限" });
        }
        // 截取当前分支名
        String pro_tag;
        
        if(CommonUtils.isNullOrEmpty(pro_image_uri)){
            pro_tag = pro_scc_image_uri.split(":")[1];
        }else{
            pro_tag = pro_image_uri.split(":")[1];
        }
        
        String fake_tag = pro_tag + "-" + Dict.FAKETIME;
        String fake_uri = "";
        RelDevopsRecord relDevopsRecord = relDevopsRecordService.findAppByTagAndAppid(application_id, fake_tag);
        if(!CommonUtils.isNullOrEmpty(relDevopsRecord)) {
            if(!CommonUtils.isNullOrEmpty(relDevopsRecord.getPro_image_uri())){
                fake_uri = relDevopsRecord.getPro_image_uri();
            }else if(!CommonUtils.isNullOrEmpty(relDevopsRecord.getPro_scc_image_uri())){
                fake_uri = relDevopsRecord.getPro_scc_image_uri();
            }
        }
        ProdApplication prodApplication = prodApplicationService.setImageConfigConfirmUri(prod_id, application_id,
                pro_image_uri, pro_scc_image_uri,"1", fake_uri);
        User user = CommonUtils.getSessionUser();
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(application_id,
                prodRecord.getRelease_node_name());
        
        String scc_prod = prodRecord.getScc_prod();
        
        if ("1".equals(scc_prod)) {
        	prodAssetService.uploadSccRouter(user.getId(), prodRecord, "dynamicconfig", application_id,
            		(String) application.get(Dict.NAME_ZH), (String) application.get(Dict.NAME_EN) ,releaseApplication, pro_tag);
        	prodAssetService.uploadRouter(user.getId(), prodRecord, "bastioncommonconfig", application_id,
        			(String) application.get(Dict.NAME_ZH), (String) application.get(Dict.NAME_EN) ,releaseApplication, pro_tag);
        }else {
        	prodAssetService.uploadRouter(user.getId(), prodRecord, "config", application_id,
            		(String) application.get(Dict.NAME_ZH), (String) application.get(Dict.NAME_EN) ,releaseApplication, pro_tag);
        }
        
        // 添加崩溃日志文件至config目录
        if(!CommonUtils.isNullOrEmpty(releaseApplication) && !CommonUtils.isNullOrEmpty(releaseApplication.getPath())
                && !CommonUtils.isNullOrEmpty(releaseApplication.getPath().get(pro_tag))) {
        	if ("0".equals(scc_prod)) {
        		prodAssetService.uploadSourceMap(user.getId(), prodRecord, "config", application_id,
        				(String) application.get(Dict.NAME_ZH),releaseApplication, pro_tag);
			} else if ("1".equals(scc_prod)) {
				prodAssetService.uploadSourceMap(user.getId(), prodRecord, "bastioncommonconfig", application_id,
						(String) application.get(Dict.NAME_ZH),releaseApplication, pro_tag);
			}
        }
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setOperation_type("1");
        // 变更应用配置文件审核
        auditRecord.setOperation(Dict.PROD_APPLICATION_CONFIG_CHANGED);
        auditRecord.setProd_id(prod_id);
        auditRecord.setApplication_id(application_id);
        auditRecord.setOperation_time(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        auditRecord.setOperator_name_en(user.getUser_name_en());
        auditRecord.setOperator_name_cn(user.getUser_name_cn());
        auditRecord.setOperator_email(user.getEmail());
        try {
            auditRecordService.save(auditRecord);
        } catch (Exception e) {
            logger.error("变更应用配置文件审核记录保存异常", e);
        }
        return JsonResultUtil.buildSuccess(prodApplication);
    }

    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.PROD_ID})
    @PostMapping(value = "/confirmChanges")
    public JsonResult confirmChanges(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String pro_image_uri = "";
        if (!roleService.isGroupReleaseManager(CommonUtils.getSessionUser().getGroup_id())
                && !roleService.isAppSpdbManager(application_id)
                && !roleService.isApplicationManager(application_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "此操作为应用级别,没有操作权限" });
        }
        // 截取当前分支名
        String fake_uri = "";
        //配置文件变更审核,pro_image_uri已经保存不需要更新.
        ProdApplication prodApplication = prodApplicationService.setImageConfigConfirmUri(prod_id, application_id,
                pro_image_uri, "","1", fake_uri);
        User user = CommonUtils.getSessionUser();
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setOperation_type("1");
        // 变更应用配置文件审核
        auditRecord.setOperation(Dict.PROD_APPLICATION_CONFIG_CHANGED);
        auditRecord.setProd_id(prod_id);
        auditRecord.setApplication_id(application_id);
        auditRecord.setOperation_time(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        auditRecord.setOperator_name_en(user.getUser_name_en());
        auditRecord.setOperator_name_cn(user.getUser_name_cn());
        auditRecord.setOperator_email(user.getEmail());
        try {
            auditRecordService.save(auditRecord);
        } catch (Exception e) {
            logger.error("变更应用配置文件审核记录保存异常", e);
        }
        return JsonResultUtil.buildSuccess(prodApplication);
    }
    /**
     * 查询非此总介质目录下的变更应用列表
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = { Dict.PROD_ASSETS_VERSION , Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/queryAppWithOutSum")
    public JsonResult queryAppWithOutSum(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_assets_version = requestParam.get(Dict.PROD_ASSETS_VERSION);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        List<ReleaseApplication> list = prodApplicationService.queryAppWithOutSum(prod_assets_version , release_node_name);
        List<Map<String, Object>> applist = new ArrayList<>();
        for (ReleaseApplication releaseApplication : list) {
            Map<String, Object> app = appService.queryAPPbyid(releaseApplication.getApplication_id());
            app.put(Dict.APPLICATION_ID, releaseApplication.getApplication_id());
            applist.add(app);
        }
        return JsonResultUtil.buildSuccess(applist);
    }

    @RequestValidate(NotEmptyFields = { Dict.APPLICATION_ID, Dict.PROD_ID, Dict.STATUS })
    @PostMapping(value = "/updateApplication")
    public JsonResult updateApplication(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String status = requestParam.get(Dict.STATUS);
        ProdApplication prodApplication = new ProdApplication();
        prodApplication.setApplication_id(application_id);
        prodApplication.setProd_id(prod_id);
        prodApplication.setStatus(status);
        prodApplication = prodApplicationService.updateApplication(prodApplication);
        return JsonResultUtil.buildSuccess(prodApplication);
    }

    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID})
    @PostMapping(value = "/queryLastTagByGitlabId")
    public JsonResult queryLastTagByGitlabId(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        Integer gitlab_project_id = (Integer) requestParam.get(Dict.GITLAB_PROJECT_ID);
        String prod_id = (String) requestParam.get(Dict.PROD_ID);
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
        String type = (String) requestParam.get(Dict.TYPE);
        String application_id = releaseApplicationService.queryApplicationId("", gitlab_project_id);
        if(CommonUtils.isNullOrEmpty(type)) {
            type = "proc";
        }
        String tag = prodApplicationService.queryLastTagByGitlabId(application_id, prod_id, type,release_node_name);
        return JsonResultUtil.buildSuccess(tag);
    }

    @RequestValidate(NotEmptyFields = {Dict.GITLAB_PROJECT_ID, Dict.TAG})
    @PostMapping(value = "/queryAutoConfigUrl")
    public JsonResult queryAutoConfigUrl(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        Integer gitlab_project_id = (Integer) requestParam.get(Dict.GITLAB_PROJECT_ID);
        String tag = (String) requestParam.get(Dict.TAG);
        Map<String, Object> configGitlab = prodApplicationService.findConfigByGitlab(String.valueOf(gitlab_project_id));
        String configGitlabId = prodApplicationService.checkEnvConfig(configGitlab, tag);
        String gitlabUrl = null;
        if(!CommonUtils.isNullOrEmpty(configGitlabId)) {
            gitlabUrl = gitlabService.findWebUrlByGitlabId(configGitlabId);
        }
        return JsonResultUtil.buildSuccess(gitlabUrl);
    }

    @PostMapping(value = "/queryProdDir")
    public JsonResult queryProdDir() {
        return JsonResultUtil.buildSuccess(prod_dir.split(","));
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID, Dict.RELEASE_TYPE})
    @PostMapping(value = "/updateProdDir")
    public JsonResult updateProdDir(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        long start = System.currentTimeMillis();
        prodApplicationService.updateProdDir(requestParam);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        logger.info("@@trans updateProdDir timeElapsed: " + timeElapsed);
        return JsonResultUtil.buildSuccess();
    }
    
    /**
     * 修改变更应用部署平台
     * */
    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID, Dict.RELEASE_TYPE})
    @PostMapping(value = "/updateProdDeploy")
    public JsonResult updateProdDeploy(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String prd_id = (String) requestParam.get(Dict.PROD_ID);
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        String release_type = (String) requestParam.get(Dict.RELEASE_TYPE);
        List<String> deploy_type = (List<String>) requestParam.get(Dict.DEPLOY_TYPE);
        prodApplicationService.updateProdDeploy(prd_id, application_id, release_type, deploy_type);
        return JsonResultUtil.buildSuccess();
    }
    

    @Override
    public void afterPropertiesSet() {
        this.dmzEnvProductMapping = JSONObject.fromObject(this.dmzEnvProductMappingString);
        this.dmzEnvGrayMapping = JSONObject.fromObject(this.dmzEnvGrayMappingString);
        this.dmzProductStatus = new ArrayList<>(this.dmzEnvProductMapping.keySet());
        this.dmzGrayStatus = new ArrayList<>(this.dmzEnvGrayMapping.keySet());

        this.bizEnvProductMapping = JSONObject.fromObject(this.bizEnvProductMappingString);
        this.bizEnvGrayMapping = JSONObject.fromObject(this.bizEnvGrayMappingString);
        this.bizProductStatus = new ArrayList<>(this.bizEnvProductMapping.keySet());
        this.bizGrayStatus = new ArrayList<>(this.bizEnvGrayMapping.keySet());
    }

    private Map setConfigImageTag(ProdRecord prodRecord, String application_id, String last_tag) throws Exception {
        String prod_id = prodRecord.getProd_id();
        Map result = new HashMap();
        if (roleService.isGroupReleaseManager(CommonUtils.getSessionUser().getGroup_id())||roleService.isAppSpdbManager(application_id)
                || roleService.isApplicationManager(application_id)) {
            String tips = null;
            String fdev_config_confirm = null;
            boolean fdev_config_changed = false;
            String compare_url = "";
            // 根据应用id查询应用详情
            Map<String, Object> application = appService.queryAPPbyid(application_id);
            // 截取当前分支名
            String new_tag = last_tag;
            prodAssetService.delCommonConfigByAppAndProd(application_id, prod_id);
            // 判断应用关联的gitlab的id是否为空
            if(!CommonUtils.isNullOrEmpty(application) && !CommonUtils.isNullOrEmpty(application.get(Dict.GITLAB_PROJECT_ID))) {
                // 根据应用gitlab的id查询应用绑定部署实体配置
                Map<String, Object> configGitlab = prodApplicationService.findConfigByGitlab(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)));
                String configGitlabId = prodApplicationService.checkEnvConfig(configGitlab, new_tag);
                // 部署实体配置是否为空
                if(!CommonUtils.isNullOrEmpty(configGitlabId)) {
                    String webUrl = gitlabService.findWebUrlByGitlabId(configGitlabId);
                    //分支有差异，列出当前分支的所有文件
                    Set<String> set = gitlabService.queryResourceFiles(configGitlabId, new_tag);
                    String user_id = CommonUtils.getSessionUser().getId();
                    String network = (String) application.get(Dict.NETWORK);
                    List<String> standardEnvList;
                    Map<String, String> standardEnvMapping;
                    if(!CommonUtils.isNullOrEmpty(network) && !network.contains("dmz")) {
                        standardEnvList = Dict.GRAY.equals(prodRecord.getType()) ? this.bizGrayStatus : this.bizProductStatus;
                        standardEnvMapping = Dict.GRAY.equals(prodRecord.getType()) ?
                                this.bizEnvGrayMapping : this.bizEnvProductMapping;
                    } else {
                        standardEnvList = Dict.GRAY.equals(prodRecord.getType()) ? this.dmzGrayStatus : this.dmzProductStatus;
                        standardEnvMapping = Dict.GRAY.equals(prodRecord.getType()) ?
                                this.dmzEnvGrayMapping : this.dmzEnvProductMapping;
                    }
                    String application_name_en = (String) application.get(Dict.NAME_EN);
                    if(Dict.SPDB_CLI_MOBCLI.equals(application_name_en) || application_name_en.startsWith(Dict.MSPMK_CLI_)) {
                        if (set.size() == 0) {
                            fdev_config_confirm = "1";
                        } else {
                            compare_url = webUrl + "/tree/" + new_tag;
                            // 遍历所有文件
                            for(String branches : set) {
                                // 取出所有md5文件
                                if(Dict.CLI_MD5.equals(branches.split("/")[0])) {
                                    fdev_config_confirm = "0";
                                    fdev_config_changed = true;
                                    ProdAsset prodAsset = new ProdAsset(webUrl + "/blob/" + new_tag + "/" + branches,
                                            branches.substring(branches.lastIndexOf("/") + 1),
                                            prodRecord.getRelease_node_name(), Dict.COMMONCONFIG, Constants.SOURCE_FDEV,
                                            user_id, prod_id, application_id, (String) application.get(Dict.NAME_ZH),
                                            configGitlabId);
                                    // 每个md5文件在每套环境放一份文件
                                    for(String env : standardEnvMapping.values()) {
                                        prodAsset.setRuntime_env(env);
                                        prodAssetService.addGitlabAsset(prodAsset);
                                    }
                                }
                            }
                        }
                    } else {
                        // 最后一次投产的tag
                        String oldTag = prodApplicationService.findLatestUri(application_id, prodRecord,Dict.CAAS);
                        if(CommonUtils.isNullOrEmpty(oldTag)){
                            oldTag = prodApplicationService.findLatestUri(application_id, prodRecord,Dict.SCC);
                        }
                        Map<String, Object> differs = new HashMap<>();

                        //判断是否有上一次投产的uri
                        if(!CommonUtils.isNullOrEmpty(oldTag) && oldTag.contains("pro")) {
                            if(oldTag.contains(":")){
                                // 截取最后一次投产的分支名
                                oldTag = oldTag.split(":")[1];
                            }
                            // 比对两个分支差异
                            differs = gitlabService.compareBranches(configGitlabId, oldTag, new_tag);
                        }

                        // 有差异或无上次投产则保存，其他不做处理
                        if(CommonUtils.isNullOrEmpty(oldTag) || !CommonUtils.isNullOrEmpty(differs.get(Dict.DIFFS))) {
                            List<String> envList = new ArrayList<>();
                            for(String envSingle : set) {
                                envList.add(envSingle.split("/")[0]);
                            }
                            if(!envList.containsAll(standardEnvList)) {
                                StringBuilder sb = new StringBuilder();
                                for(String env : standardEnvList) {
                                    if(!envList.contains(env)) {
                                        sb.append(env).append("、");
                                    }
                                }
                                String lackEnv = sb.toString();
                                if(lackEnv.endsWith("、")) {
                                    lackEnv = lackEnv.substring(0, lackEnv.length() - 1);
                                }
                                if("PROCHF".equals(standardEnvMapping.get(lackEnv))) {
                                    // 若仅缺少PROCHF目录，则做提示，不抛异常
                                    tips = "当前应用对应公共配置文件commonfig缺少PROCHF目录，该目录对应" + lackEnv + "环境，请仔细检查";
                                } else {
                                    // 环境不满足，抛出异常
                                    throw new FdevException(ErrorConstants.RELEASE_LACK_FDEV_CONFIG,new String[] { lackEnv, webUrl + "/tree/" + new_tag });
                                }
                            }
                            if(!CommonUtils.isNullOrEmpty(differs)) {
                                compare_url = webUrl + differs.get(Dict.COMPAREBRANCHES_URL);
                            } else {
                                compare_url = webUrl + "/tree/" + new_tag;
                            }
                            for(String branchFile : set) {
                                String env = branchFile.split("/")[0];
                                if(CommonUtils.isNullOrEmpty(standardEnvMapping.get(env)) || !branchFile.endsWith(".properties")) {
                                    continue;
                                }
                                fdev_config_confirm = "0";
                                fdev_config_changed = true;
                                ProdAsset prodAsset = new ProdAsset(webUrl + "/blob/" + new_tag + "/" + branchFile,
                                        branchFile.substring(branchFile.lastIndexOf("/") + 1),
                                        prodRecord.getRelease_node_name(), Dict.COMMONCONFIG, Constants.SOURCE_FDEV,
                                        user_id, prod_id, application_id, (String) application.get(Dict.NAME_ZH),
                                        configGitlabId);
                                prodAsset.setRuntime_env(standardEnvMapping.get(env));
                                prodAssetService.addGitlabAsset(prodAsset);
                            }
                        } else {
                            fdev_config_confirm = "1";
                        }
                    }
                }
            }
            //上次投产镜像标签
            String lastCaasTag = prodApplicationService.findLastReleaseTag(application_id, prodRecord, Dict.CAAS);
            String lastSCCTag = prodApplicationService.findLastReleaseTag(application_id, prodRecord, Dict.SCC);
            if(CommonUtils.isNullOrEmpty(fdev_config_confirm)) {
                // 应用模版
                result.put(Dict.PRO_IMAGE_URI, lastCaasTag);
                result.put(Dict.PRO_SCC_IMAGE_URI, lastSCCTag);
            } else {
                result.put(Dict.PRO_IMAGE_URI, lastCaasTag);
                result.put(Dict.PRO_SCC_IMAGE_URI, lastSCCTag);
                result.put(Dict.FDEV_CONFIG_CHANGED,fdev_config_changed);
                result.put(Dict.COMPARE_URL,compare_url);
                result.put(Dict.FDEV_CONFIG_CONFIRM,fdev_config_confirm);
                result.put(Dict.TAG,new_tag);
            }
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "此操作为应用级别,没有操作权限" });
        }
        return result;
    }

    @OperateRecord(operateDiscribe="投产模块-选择镜像版本")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.PROD_ID, Dict.PRO_PACKAGE_URI})
    @PostMapping(value = "/setPackageTag")
    public JsonResult setApplicationPackageTag(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String prod_id = requestParam.get(Dict.PROD_ID);
        String pro_tag = requestParam.get(Dict.PRO_PACKAGE_URI);
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        ProdApplication prodApplication = prodApplicationService.queryApplication(prod_id, application_id);
        ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(application_id,
                prodRecord.getRelease_node_name());
        if(!Constants.APPLICATION_DOCKER_STOP_START_ALL.equals(prodApplication.getRelease_type())
                && !CommonUtils.isNullOrEmpty(releaseApplication.getFdev_config_confirm())
                && !"1".equals(releaseApplication.getFdev_config_confirm())) {
            throw new FdevException(ErrorConstants.CONFIG_FILE_WITHOUT_AUDIT_TAG);
        }
        if (roleService.isGroupReleaseManager(CommonUtils.getSessionUser().getGroup_id())
                ||roleService.isAppSpdbManager(application_id)
                || roleService.isApplicationManager(application_id)) {
                // 应用模版
                String pro_package_uri = relDevopsRecordService.queryPackageByTagAndApp(pro_tag, application_id);
                prodApplicationService.setPackageTag(prod_id, application_id, pro_package_uri);
                ProdApplication result = new ProdApplication(prod_id, application_id, pro_package_uri,null);

            Map<String, Object> returnMap = CommonUtils.beanToMap(result);
            return JsonResultUtil.buildSuccess(returnMap);
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "此操作为应用级别,没有操作权限" });
        }
    }


    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID, Dict.DEPLOY_TYPE})
    @PostMapping(value = "/queryReplicasnu")
    public JsonResult queryReplicasnu(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        Map<String,Object> result = prodApplicationService.queryReplicasnu(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID, Dict.CHANGE})
    @PostMapping(value = "/updateReplicasnu")
    public JsonResult updateReplicasnu(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        prodApplicationService.updateReplicasnu(requestParam);
        return JsonResultUtil.buildSuccess();
    }
    
    @PostMapping(value = "/queryProfile")
    public JsonResult queryProfile(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
    	Map<String,Object> param = new HashMap<>();
        param.putAll(requestParam);
        param.put("bizGrayStatus",this.bizGrayStatus);
        param.put("bizProductStatus",this.bizProductStatus);
        param.put("bizEnvGrayMapping",this.bizEnvGrayMapping);
        param.put("bizEnvProductMapping",this.bizEnvProductMapping);
        param.put("dmzGrayStatus",this.dmzGrayStatus);
        param.put("dmzProductStatus",this.dmzProductStatus);
        param.put("dmzEnvGrayMapping",this.dmzEnvGrayMapping);
        param.put("dmzEnvProductMapping",this.dmzEnvProductMapping);
    	pushImageService.queryProfile(param);
    	return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID})
    @RequestMapping(value = "/queryAllApplications", method = RequestMethod.POST)
    public JsonResult queryAllApplications(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String prod_id = requestParam.get(Dict.PROD_ID);
        List<Map> prodApplications = prodApplicationService.queryApplications(new ProdApplication(prod_id));
        return JsonResultUtil.buildSuccess(prodApplications);
    }

}
