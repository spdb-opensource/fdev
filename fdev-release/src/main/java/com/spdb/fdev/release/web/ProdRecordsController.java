package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Validator;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.dao.ITemplateDocumentDao;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Api(tags = "变更管理接口")
@RequestMapping("api/release")
@RestController
@RefreshScope
public class ProdRecordsController implements InitializingBean {

	@Value("${scripts.path}")
	private String scripts_path;

	@Value("${fdev.autorelease.dmz.env.product.mapping}")
	private String dmzEnvProductMappingString;

	@Value("${fdev.autorelease.dmz.env.gray.mapping}")
	private String dmzEnvGrayMappingString;

	@Value("${fdev.autorelease.biz.env.product.mapping}")
	private String bizEnvProductMappingString;

	@Value("${fdev.autorelease.biz.env.gray.mapping}")
	private String bizEnvGrayMappingString;

	@Autowired
	private IProdRecordService prodRecordService;
	@Autowired
	private IReleaseNodeService releaseNodeService;
	@Autowired
	private IProdApplicationService prodApplicationService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IReleaseCatalogService releaseCatalogService;
	@Autowired
	private RedissonClient redissonClient;
	@Autowired
	private IProdTemplateService prodTemplateService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IGroupAbbrService groupAbbrService;
	@Autowired
	private IGenerateExcelService generateExcelService;
	@Autowired
	private ITaskService taskService;
	@Autowired
	private IApplicationImageService applicationImageService;
    @Autowired
	private IProdConfigService prodConfigService;
    @Autowired
	private IProdAssetService prodAssetService;
    @Autowired
	private IAppService appService;
    @Autowired
	private IGitlabService gitlabService;

	@Autowired
	private ITemplateDocumentDao templateDocumentDao;

    //生产状态
    private List<String> dmzProductStatus;
    //灰度状态
    private List<String> dmzGrayStatus;
    //生产状态
    private List<String> bizProductStatus;
    //灰度状态
    private List<String> bizGrayStatus;

    private Map<String, String> dmzEnvProductMapping;

    private Map<String, String> dmzEnvGrayMapping;

    private Map<String, String> bizEnvProductMapping;

    private Map<String, String> bizEnvGrayMapping;

	private static Logger logger = LoggerFactory.getLogger(ProdRecordsController.class);

	@OperateRecord(operateDiscribe="投产模块-新建变更")
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME, Dict.DATE, Dict.PROD_SPDB_NO, Dict.VERSION, Dict.TYPE,
			Dict.OWNER_GROUPID, Dict.PLAN_TIME, Dict.IMAGE_DELIVER_TYPE })
	@PostMapping(value = "/create")
	public JsonResult create(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String release_date = (String) requestParam.get(Dict.DATE);
		String prod_spdb_no = (String) requestParam.get(Dict.PROD_SPDB_NO);
		String owner_groupId = (String) requestParam.get(Dict.OWNER_GROUPID);
		String plan_time = (String) requestParam.get(Dict.PLAN_TIME);
		String image_deliver_type = (String) requestParam.get(Dict.IMAGE_DELIVER_TYPE);//是否自动化发布
		String excel_template_url = (String) requestParam.get(Dict.EXCEL_TEMPLATE_URL);
		String excel_template_name = null;
		if(!CommonUtils.isNullOrEmpty(excel_template_url)){
			String[] templateList = excel_template_url.split("/");
			excel_template_name = templateList[templateList.length-1];
		}
		String rex = "^[0-9a-zA-Z]+$";
		String version = (String) requestParam.get(Dict.VERSION);
		String type = (String) requestParam.get(Dict.TYPE);
		String owner_system = (String) requestParam.get(Dict.OWNER_SYSTEM);
		List<String> applications = (List<String>) requestParam.get(Dict.APPLICATIONS);
		String system_name = "";
		if(!CommonUtils.isNullOrEmpty(owner_system)){
			system_name = taskService.querySystemName(owner_system);
		}
		// 查询template_document表获取scc所有模板集合
		TemplateDocument templateDocument = templateDocumentDao.getDocument(system_name, type);
		List<String> scc_document_list = templateDocument.getScc_document_list();
		// 校验变更单号格式
		if(!Validator.versionValidate(version)){
			logger.error("version is not permitted");
			throw new FdevException(ErrorConstants.VERSION_NOT_PERMIT);
		}
		if (!Pattern.matches(rex, prod_spdb_no)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { Dict.PROD_SPDB_NO, "变更编号只能为数字或英文字母" });
		}
		//校验当前用户创建变更权限
		if(!roleService.isGroupReleaseManager(owner_groupId)) {
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		//变更版本防重
		if(!CommonUtils.isNullOrEmpty(prodRecordService.queryProdByVersion(version))){
			throw new FdevException(ErrorConstants.PRODRECORD_REPEAT);
		}
		//投产窗口日期，变更日期，当前日期：变更日期必须大于等于今天，而且不能超过投产窗口日期三天
		Integer node_date = Integer.parseInt(release_node_name.split("_")[0]);
		Integer date = Integer.parseInt(release_date.replace("/", ""));
		Integer formatDate = Integer.parseInt(CommonUtils.formatDate(CommonUtils.DATE_COMPACT_FORMAT));
		if (date > node_date+3 || date < formatDate) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "日期不能小于今天且不能大于投产日期时间后三天"});
		}
		//查询投产窗口实体
		ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);
		if (CommonUtils.isNullOrEmpty(releaseNode)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST,
					new String[] { "release_node_name:" + release_node_name });
		}
		//同窗口下变更版本防重
		ProdRecord checkRepect = prodRecordService.queryProdRecordByVersion(release_node_name, version);
		if (!CommonUtils.isNullOrEmpty(checkRepect)) {// version不能重复
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "version is repeat" });
		}
		//查询窗口所属小组
		Map<String, Object> groupDetail = userService.queryGroupDetail(owner_groupId);
		if (CommonUtils.isNullOrEmpty(groupDetail)) {
			logger.error("can't find the group info !@@@@@group_id={}", owner_groupId);
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "组信息不存在" });
		}
		ProdRecord prodRecord = new ProdRecord();
		//如果是自动化发布，则有模板
		if(Constants.IMAGE_DELIVER_TYPE_NEW.equals(image_deliver_type)){
			//按组，系统，变更类型查询变更模板
			ProdTemplate prodTemplate = new ProdTemplate();
			prodTemplate.setOwner_group(owner_groupId);
			prodTemplate.setOwner_system(owner_system);
			prodTemplate.setTemplate_type(type);
			List<ProdTemplate> prodTemplates = prodTemplateService.query(prodTemplate);
			if(prodTemplates.size() != 1) {
				String template_type_name = Dict.GRAY.equals(type) ? "灰度" : "生产";
				if(prodTemplates.size() == 0) {
					// 新增逻辑：如果查不到数据，则去找该组的第三层级在查询一次
					Map<String, Object> three_level_map = userService.getThreeLevelGroup(owner_groupId);
					String three_level_id = (String) three_level_map.get(Dict.ID);
					prodTemplate.setOwner_group(three_level_id);
					prodTemplates = prodTemplateService.query(prodTemplate);
					if(prodTemplates.size() != 1){
						if(prodTemplates.size() == 0) {
							throw new FdevException(ErrorConstants.TEMPLATE_NOT_EXISTS, new String[]{"当前组下" + system_name + template_type_name});
						}else{
							throw new FdevException(ErrorConstants.TEMPLATE_MUST_BE_DELETE, new String[]{"当前组下" + system_name + template_type_name});
						}
					}

				} else {
					throw new FdevException(ErrorConstants.TEMPLATE_MUST_BE_DELETE, new String[]{"当前组下" + system_name + template_type_name});
				}
			}
			prodTemplate = prodTemplates.get(0);
			// 检测所选变更模板目录是否包含（1-常规应用包更新，2-微服务应用更新），如果一个都不包含则抛错
			boolean flag = false;
			if (!CommonUtils.isNullOrEmpty(applications)) {
				List<AssetCatalog> catalog_list = releaseCatalogService.query(prodTemplate.getId());
				for (AssetCatalog assetCatalog : catalog_list) {
					if (assetCatalog.getCatalog_type().equals(Constants.CATALOG_TYPE_NORMAL)
							|| assetCatalog.getCatalog_type().equals(Constants.CATALOG_TYPE_MICROSERIVICE)) {
						flag = false;
						break;
					} else {
						flag = true;
					}
				}
			}
			if(flag) {
				// 当前变更模板无docker目录，无法添加变更应用，请更换变更模板后添加变更应用！
				throw new FdevException(ErrorConstants.PROD_TEMP_NOT_EXIST_PROD_APPLICATION);
			}
			prodRecord.setTemplate_id(prodTemplate.getId());
			prodRecord.setExcel_template_name(excel_template_name);
			prodRecord.setExcel_template_url(excel_template_url);
			if(scc_document_list.contains(excel_template_name)){  // 是否是SCC变更（1是0否）
				prodRecord.setScc_prod("1");
			} else {
				prodRecord.setScc_prod("0");
			}
		}
		//获取当前用户
		User user = CommonUtils.getSessionUser();
		//根据变更单号获取最早变更实体
		ProdRecord earliestProdRecord = prodRecordService.queryEarliestByProdSpdbNo(prod_spdb_no);
		String prod_assets_version = version + "_" + prod_spdb_no;
		if(!CommonUtils.isNullOrEmpty(earliestProdRecord)) {
			String oldDate = earliestProdRecord.getDate();
			String oldPlanTime = earliestProdRecord.getPlan_time();
			String oldVersion = earliestProdRecord.getVersion();
            String old_prod_assets_version = earliestProdRecord.getProd_assets_version();
			if((release_date.compareToIgnoreCase(oldDate) > 0) //当前日期大
					|| (oldDate.equals(release_date) && plan_time.compareToIgnoreCase(oldPlanTime) > 0)) {//日期相等，当前计划时间大
				prod_assets_version = oldVersion + "_" + prod_spdb_no;
			} else {
				if(Constants.IMAGE_DELIVER_TYPE_NEW.equals(image_deliver_type)){
					//获取组缩写
					GroupAbbr groupAbbr = groupAbbrService.queryGroupAbbr(owner_groupId);
					if(!CommonUtils.isNullOrEmpty(groupAbbr) && !CommonUtils.isNullOrEmpty(groupAbbr.getSystem_abbr())) {
						CommonUtils.runPythonArray(scripts_path + "change_remote_dir.py",
								new String[]{groupAbbr.getSystem_abbr(),
										old_prod_assets_version,
										prod_assets_version,""}
						);
					} else {
						throw new FdevException(ErrorConstants.LACK_SYSTEM_ABBR);
					}
				}
            }
			prodRecordService.updatePordAssetsVersionByProdSpdbNo(prod_spdb_no, prod_assets_version);
		}

		HashSet<String> groupSet = new HashSet(); // 零售金融组、支付组、公共组、公司金融组
		List<AwsConfigure> awsConfigureList = prodAssetService.queryAwsConfigByGroupId(null);
		awsConfigureList.forEach(awsConfigure -> groupSet.add(awsConfigure.getGroup_name()));
		// 查询当前变更组及父组
		List<Map<String, Object>> groupList = userService.queryParentGroupByGroupId(owner_groupId);
		for(Iterator iter = groupList.iterator(); iter.hasNext();){
			Map<String, Object> map = (Map<String, Object>) iter.next();
			if(groupSet.contains(map.get(Dict.NAME))){
				prodRecord.setAws_group((String)map.get(Dict.ID));  // 设置对象存储所属小组id
				break;
			}
		}
		prodRecord.setOwner_groupId(owner_groupId);
		prodRecord.setRelease_node_name(release_node_name);
		prodRecord.setLauncher(user.getId());
		prodRecord.setCreate_time(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
		prodRecord.setDate(release_date);
		prodRecord.setProd_spdb_no(prod_spdb_no);
		prodRecord.setVersion(version);
		prodRecord.setType(type);
		ObjectId objectId = new ObjectId();
		prodRecord.set_id(objectId);
		prodRecord.setStatus(Constants.RELEASESTATUS_0);
		prodRecord.setProd_id(objectId.toString());
		prodRecord.setPlan_time(plan_time);
		prodRecord.setProd_assets_version(prod_assets_version);
		prodRecord.setImage_deliver_type(image_deliver_type);
		prodRecord.setOwner_system(owner_system);
		prodRecord.setOwner_system_name(system_name);
		prodRecord = prodRecordService.create(prodRecord);

		if(!CommonUtils.isNullOrEmpty(applications)) {
			for (String application_id : applications) {// 新增变更记录的同时 新增变更应用
				if (CommonUtils.isNullOrEmpty(application_id)) {
					continue;
				}
				ProdApplication prodApplication = new ProdApplication();
				prodApplication.setApplication_id(application_id);
				prodApplication.setProd_id(prodRecord.getProd_id());
				prodApplication.setRelease_type(Constants.APPLICATION_DOCKER);
				prodApplication.setContainer_num("X");
				//判断变更应用是否为新增
//				Boolean signflag = prodRecordService.checkAddSign(application_id, prodRecord);
				HashSet<String> hasDeployType = prodApplicationService.findAllDeployedType(application_id, prodRecord);

				if(!hasDeployType.contains("CAAS") && !hasDeployType.contains("SCC")){
					prodApplication.setCaas_add_sign("1"); // caas平台为新增应用
					prodApplication.setScc_add_sign("1"); // scc平台为新增应用
					if(scc_document_list.contains(excel_template_name)){
						prodApplication.setDeploy_type(new ArrayList<String>());
						prodApplication.setProd_dir(new ArrayList<String>());
					} else {
						prodApplication.setDeploy_type(Arrays.asList("CAAS"));
						prodApplication.setProd_dir(Arrays.asList(Dict.DOCKER_YAML));
					}
				} else {
					List<String> prod_dir = new ArrayList<String>(); // 介质目录
					if(!scc_document_list.contains(excel_template_name) && hasDeployType.contains("SCC")){
						hasDeployType.remove("SCC");
					}
					if(hasDeployType.contains("CAAS")){
						prodApplication.setCaas_add_sign("0");
						prod_dir.add(Dict.DOCKER_LOWERCASE);
					}
					if(hasDeployType.contains("SCC")){
						prodApplication.setScc_add_sign("0");
					}
					// 前端应用只有云下
					Map<String, Object> application = appService.queryAPPbyid(application_id);
					String name_en = (String) application.get(Dict.NAME_EN);
					if(!name_en.startsWith("mspmk-cli")){
						if(hasDeployType.contains("SCC")){
							prod_dir.add(Dict.SCC);
						}
					}else{
						hasDeployType.remove("SCC");
					}
					prodApplication.setDeploy_type(new ArrayList<String>(hasDeployType));
					prodApplication.setProd_dir(prod_dir);
				}
				prodApplicationService.addApplication(prodApplication);
//				// 添加崩溃日志文件至config目录
//				asyncService.setCollapseConfig(prodRecord, application_id, release_node_name, CommonUtils.getSessionUser());
			}
		}
        /**
         * 配置文件应用变更部分
         */
        String isConfigSum = (String) requestParam.get(Dict.ISCONFIGSUM);//配置文件变更标识
        //判断标识是否所有数据都已生成配置文件，
        if(!CommonUtils.isNullOrEmpty(isConfigSum) && "0".equals(isConfigSum)) {
            List<ProdConfigApp> configApplications = prodConfigService.queryConfigApplication(release_node_name);
            for (ProdConfigApp app : configApplications) {// 新增变更记录的同时 新增变更应用
                if ("0".equals(app.getStatus())) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"配置文件变更应用有配置文件未生成["+app.getApplication_name()+"]"});
                }
                //自动变更，获取上一次投产镜像标签。
				String lastCaasTag = prodApplicationService.findLastReleaseTag(app.getApplication_id(), prodRecord,Dict.CAAS);
				String lastSccTag = prodApplicationService.findLastReleaseTag(app.getApplication_id(), prodRecord,Dict.SCC);
                //获取应用
                if(CommonUtils.isNullOrEmpty(lastCaasTag) && CommonUtils.isNullOrEmpty(lastSccTag)) {
                    throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行重启操作！["+app.getApplication_name()+"]"});
                }
                //自动变更，获取上一次投产tag包作为本次投产镜像标签。
				String tag = app.getTag();

				ProdApplication prodApplication = new ProdApplication();
				prodApplication.setApplication_id(app.getApplication_id());
				prodApplication.setProd_id(prodRecord.getProd_id());
				prodApplication.setRelease_type(Constants.APPLICATION_DOCKER_RESTART);
				prodApplication.setProd_dir(Arrays.asList(Dict.DOCKER_YAML));
				prodApplication.setContainer_num("X");
				prodApplication.setPro_image_uri(lastCaasTag);
				prodApplication.setPro_scc_image_uri(lastSccTag);
				prodApplication.setTag(tag);
				prodApplicationService.addApplication(prodApplication);
				//将新生成的配置文件与老的进行对比
                setConfigImageTag(prodRecord,prodApplication,tag);
            }
        }
		return JsonResultUtil.buildSuccess(prodRecord);
	}

	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME })
	@PostMapping(value = "/query")
	public JsonResult query(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		return JsonResultUtil.buildSuccess(prodRecordService.query(release_node_name));
	}

	@RequestValidate(NotEmptyFields = { Dict.PROD_ID })
	@PostMapping(value = "/queryDetail")
	public JsonResult queryDetail(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
		boolean flag = roleService.isUserOperatorProd(prod_id);
		Map map = CommonUtils.beanToMap(prodRecord);
		map.put(Dict.CAN_OPERATION, flag);
		return JsonResultUtil.buildSuccess(map);
	}

	@OperateRecord(operateDiscribe="投产模块-变更介质准备-确定")
	@RequestValidate(NotEmptyFields = { Dict.PROD_ID, Dict.AUDIT_TYPE })
	@PostMapping(value = "/audit")
	public JsonResult audit(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		String templte_properties=(String) requestParam.get(Dict.TEMPLATE_PROPERTIES);
		roleService.isGroupReleaseManagerByProd(prod_id);		
		RLock rLock = redissonClient.getLock(CommonUtils.generateRlockKey(prod_id + ":audit-prod-record"));
		if (!rLock.tryLock(0, -1L, TimeUnit.SECONDS)) {
			throw new FdevException(ErrorConstants.OTHER_USER_AUDITING);
		}
		try {
			String audit_type = (String) requestParam.get(Dict.AUDIT_TYPE);
			if (Constants.OPERATION_TYPR_REFUSE.equals(audit_type)) {// 审核类型为拒绝时，则拒绝理由不能为空
				if (CommonUtils.isNullOrEmpty(requestParam.get(Dict.REJECT_REASON))) {
					logger.error("audit:reject_reason is empty");
					throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.REJECT_REASON});
				}
			} else if (!Constants.OPERATION_TYPR_ACCESS.equals(audit_type)) {
				logger.error("audit:audit_type is invaild");
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.AUDIT_TYPE});
			}
			String reject_reason = (String) requestParam.get(Dict.REJECT_REASON);
			Map map = prodRecordService.audit(prod_id, audit_type, reject_reason,templte_properties);
			return JsonResultUtil.buildSuccess(map);
		}finally {
			rLock.unlock();
		}
	}

	@RequestValidate(NotEmptyFields = { Dict.GROUP_ID, Dict.DATE,Dict.PLAN_TIME })
	@PostMapping(value = "/getReleaseVersion")
	public JsonResult getReleaseVersion(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String group_id = requestParam.get(Dict.GROUP_ID);
		String release_date = requestParam.get(Dict.DATE);
		String plan_time = requestParam.get(Dict.PLAN_TIME);
		GroupAbbr groupAbbr = groupAbbrService.queryGroupAbbr(group_id);
		if(CommonUtils.isNullOrEmpty(groupAbbr)) {
			throw new FdevException(ErrorConstants.GROUP_ABBR_NOT_EXIST);
		}
        String date = release_date.replace("/", "");
        String time = plan_time.replace(":", "");
        StringBuilder version = new StringBuilder(Dict.DOCKER)
        		.append("_").append(groupAbbr.getGroup_abbr())
        		.append("_").append(date)
        		.append("_").append(time);
		return JsonResultUtil.buildSuccess(version.toString());
	}
	
	@RequestValidate(NotEmptyFields = { Dict.PROD_ID,Dict.AUTO_RELEASE_STAGE})
	@PostMapping(value = "/updateAutoReleaseStage")
	public JsonResult updateAutoReleaseStage(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		String auto_release_stage = (String) requestParam.get(Dict.AUTO_RELEASE_STAGE);
		ProdRecord prodRecord=prodRecordService.updateAutoReleaseStage(prod_id,auto_release_stage);
		return JsonResultUtil.buildSuccess(prodRecord);
	}

	/**
	 * 更新自动化发布日志
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	@RequestValidate(NotEmptyFields = { Dict.PROD_ID, Dict.AUTO_RELEASE_LOG})
	@PostMapping(value = "/updateAutoReleaseLog")
	public JsonResult updateAutoReleaseLog(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		String auto_release_log = (String) requestParam.get(Dict.AUTO_RELEASE_LOG);
		ProdRecord prodRecord=prodRecordService.updateAutoReleaseLog(prod_id, auto_release_log);
		return JsonResultUtil.buildSuccess(prodRecord);
	}

	@RequestValidate(NotEmptyFields = { Dict.PROD_ID, Dict.STATUS})
	@PostMapping(value = "/updateProdStatus")
	public JsonResult updateProdStatus(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String prod_id = requestParam.get(Dict.PROD_ID);
		String status = requestParam.get(Dict.STATUS);
		prodRecordService.updateProdStatus(prod_id, status);
		return JsonResultUtil.buildSuccess();
	}

	@RequestValidate(NotEmptyFields = { Dict.PROD_ID })
	@PostMapping(value = "/trace")
	public JsonResult trace(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		ProdRecord prodRecord = prodRecordService.queryTrace(prod_id);
		if (!CommonUtils.isNullOrEmpty(prodRecord) &&
				!CommonUtils.isNullOrEmpty(prodRecord.getAuto_release_log())) {
			if("6".equals(prodRecord.getAuto_release_stage()) || "7".equals(prodRecord.getAuto_release_stage())) {
				StringBuilder sb = new StringBuilder();
				sb.append("<strong style='color: yellow'>自动化介质已上传至介质服务器，以下检查镜像推送，请耐心等待</strong>").append("\n");
				sb.append("=============[+]步骤六：检查镜像推送=============").append("\n");
				Map<String, List<ApplicationImage>> applicationMap = applicationImageService.queryByProdId(prod_id);
				if(!CommonUtils.isNullOrEmpty(applicationMap.get(Dict.TOTAL))) {
					if(!CommonUtils.isNullOrEmpty(applicationMap.get(Dict.COMPLETE_PUSH))) {
						for(ApplicationImage ai : applicationMap.get(Dict.COMPLETE_PUSH)) {
							sb.append(ai.getPush_image_log()).append("\n");
						}
					}
					if(!CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSHING))) {
						for(ApplicationImage ai : applicationMap.get(Dict.PUSHING)) {
							sb.append(ai.getPush_image_log()).append("\n");
						}
					}
					if(!CommonUtils.isNullOrEmpty(applicationMap.get(Dict.JUST_START))) {
						for(ApplicationImage ai : applicationMap.get(Dict.JUST_START)) {
							sb.append(ai.getPush_image_log()).append("\n");
						}
					}
					if(applicationMap.get(Dict.JUST_START).size() > 0) {
						sb.append("<strong style='color: yellow'>若推送镜像长时间处于排队推送等待中，")
								.append("请在变更应用列表重新对该应用选择镜像标签，以重新发起镜像推送</strong>");
					}
					if(applicationMap.get(Dict.COMPLETE_PUSH).size() == applicationMap.get("total").size()) {
						sb.append("===============镜像推送检查完毕================");
					}
					if(!CommonUtils.isNullOrEmpty(applicationMap.get(Dict.PUSH_ERROR))) {
						sb.append(applicationMap.get(Dict.PUSH_ERROR).get(0).getPush_image_log()).append("\n");
						Set<String> appNames = new HashSet<>();
						for(ApplicationImage ai : applicationMap.get(Dict.PUSH_ERROR)) {
							appNames.add(ai.getApp_name_en());
						}
						sb.append("<strong style='color: yellow'>应用").append(String.join("、", appNames))
								.append("推送镜像失败，请根据错误提示修复，")
								.append("修复后无需重新发起介质准备，在变更应用列表内重新选择镜像版本，")
								.append("以重新发起镜像推送，然后回到此处查看推送状态</strong>").append("\n");
						sb.append("<strong style='color: yellow'>若生成imagelist文件内容不正确，请重新发起介质准备</strong>").append("\n");
					}
				} else {
					sb.append("本次变更不含镜像推送，无需检查").append("\n");
				}
				prodRecord.setAuto_release_log(
						new StringBuilder(prodRecord.getAuto_release_log()).append("\n").append(sb.toString()).toString());
			}
			prodRecord.setAuto_release_log(prodRecord.getAuto_release_log().replaceAll("\n", "<br/>"));
		}
		return JsonResultUtil.buildSuccess(prodRecord);
	}
	
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME,Dict.VERSION })
	@PostMapping(value = "/queryBeforePordImages")
	public JsonResult queryBeforePordImages(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String version = (String) requestParam.get(Dict.VERSION);
		Map<String, String> map=prodRecordService.queryBeforePordImages(release_node_name,version);
		return JsonResultUtil.buildSuccess(map);
	}

	@OperateRecord(operateDiscribe="投产模块-编辑")
	@RequestValidate(NotEmptyFields = {Dict.PROD_SPDB_NO, Dict.PROD_ID, Dict.PLAN_TIME})
	@PostMapping(value = "/update")
	public JsonResult update (@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String prod_id = requestParam.get(Dict.PROD_ID);
		roleService.isGroupReleaseManagerByProd(prod_id);
		prodRecordService.update(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}

	@OperateRecord(operateDiscribe="投产模块-删除子变更")
	@RequestValidate(NotEmptyFields = {Dict.PROD_ID})
	@PostMapping(value = "/delete")
	public JsonResult delete (@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String prod_id = requestParam.get(Dict.PROD_ID);
		roleService.isGroupReleaseManagerByProd(prod_id);
		ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
		if(CommonUtils.isNullOrEmpty(prodRecord)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
		}
		//校验删除变更，若存在新增应用，提示警告
		String content = prodRecordService.checkApplication(prodRecord);
		prodRecordService.delete(prod_id);
		if(!CommonUtils.isNullOrEmpty(content)){
			return JsonResultUtil.buildSuccess(content);
		}
		return JsonResultUtil.buildSuccess(null);
	}
	

	@PostMapping(value = "/queryPlan")
	public JsonResult queryPlan(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String start_date = (String) requestParam.get(Dict.START_DATE);
		String end_date = (String) requestParam.get(Dict.END_DATE);
		List<ProdRecord> list = prodRecordService.queryPlan(start_date,end_date);
		return JsonResultUtil.buildSuccess(list);
	}
	
	@PostMapping(value = "/queryProdInfo")
	public JsonResult queryProdInfo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String prod_assets_version = (String) requestParam.get(Dict.PROD_ASSETS_VERSION);
		Map<String, String> map = prodRecordService.queryProdInfo(prod_assets_version);
		return JsonResultUtil.buildSuccess(map);
	}

	@OperateRecord(operateDiscribe="投产模块-生成模版实例")
	@RequestValidate(NotEmptyFields = {Dict.PROD_IDS})
	@PostMapping(value = "/generateProdExcel")
	public JsonResult generateProdExcel(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		List<String> prod_ids = (List<String>) requestParam.get(Dict.PROD_IDS);
		String tips = generateExcelService.generateProdExcel(prod_ids);
		return JsonResultUtil.buildSuccess(tips);
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

    private void setConfigImageTag(ProdRecord prodRecord, ProdApplication prod, String last_tag) throws Exception {
        String application_id = prod.getApplication_id();
        String prod_id = prodRecord.getProd_id();
        //上次投产镜像标签
		String pro_image_uri = prodApplicationService.findLastReleaseTag(application_id, prodRecord, Dict.CAAS);
		String pro_scc_image_uri = prodApplicationService.findLastReleaseTag(application_id, prodRecord, Dict.SCC);
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
                        String old_tag = prodApplicationService.findLatestUri(application_id, prodRecord,Dict.CAAS);
                        if(CommonUtils.isNullOrEmpty(old_tag)){
							old_tag = prodApplicationService.findLatestUri(application_id, prodRecord,Dict.SCC);
						}
                        Map<String, Object> differs = new HashMap<>();

                        //判断是否有上一次投产的uri
                        if(!CommonUtils.isNullOrEmpty(old_tag) && old_tag.contains("pro")) {
                            if(old_tag.contains(":")){
                                // 截取最后一次投产的分支名
                                old_tag = old_tag.split(":")[1];
                            }

                            // 比对两个分支差异
                            differs = gitlabService.compareBranches(configGitlabId, old_tag, new_tag);
                        }

                        // 有差异或无上次投产则保存，其他不做处理
                        if(CommonUtils.isNullOrEmpty(old_tag) || !CommonUtils.isNullOrEmpty(differs.get(Dict.DIFFS))) {
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
            String fake_uri = "";
            if(CommonUtils.isNullOrEmpty(fdev_config_confirm)) {
                prodApplicationService.setImageUri(prod_id, application_id, pro_image_uri,pro_scc_image_uri, fake_uri);
            } else {
                prodApplicationService.setConfigUri(prod_id, application_id, pro_image_uri,pro_scc_image_uri,
                        fdev_config_changed, compare_url, fdev_config_confirm,new_tag);
            }
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "此操作为应用级别,没有操作权限" });
        }
    }

	@OperateRecord(operateDiscribe="投产模块-非自动化变更修改变更介质准备状态和阶段")
	@RequestValidate(NotEmptyFields = {Dict.PROD_ID})
	@PostMapping(value = "/checkDeAutoRelease")
	public JsonResult checkDeAutoRelease(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String prod_id = (String)requestParam.get(Dict.PROD_ID);
		prodRecordService.updateAutoReleaseStage(prod_id, "7");
		prodRecordService.updateProdStatus(prod_id, "3");
		return JsonResultUtil.buildSuccess();
	}

	@RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.GROUP_ID})
	@PostMapping(value = "/updateAwsAssetGroupId")
	public JsonResult updateAwsAssetGroupId(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String prodId = requestParam.get(Dict.PROD_ID);
		String groupId = requestParam.get(Dict.GROUP_ID);
		prodRecordService.updateAwsAssetGroupId(prodId, groupId);
		return JsonResultUtil.buildSuccess(null);
	}

}
