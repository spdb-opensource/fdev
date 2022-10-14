package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "配置文件变更管理接口")
@RequestMapping("api/config")
@RestController
@RefreshScope
public class ProdConfigController {

	@Value("${scripts.path}")
	private String scripts_path;

	@Autowired
	private IProdConfigService prodConfigService;

	@Autowired
	private IReleaseNodeService releaseNodeService;

	@Autowired
	private IGenerateExcelService generateExcelService;

	@Autowired
	IUserService userService;

	@Autowired
	private IProdRecordService prodRecordService;

	@Autowired
	private IProdApplicationService prodApplicationService;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IAppService appService;

	private static Logger logger = LoggerFactory.getLogger(ProdConfigController.class);

	/**
	 * add 配置文件变更应用保存
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/addConfigApplication")
	public JsonResult addConfigApplication(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {

		List<Map> Prams = (List<Map>) requestParam.get(Dict.APPLICATIONS);	//应用id集合

		List<Map<String,Object>> result = new ArrayList<>();
		if(!CommonUtils.isNullOrEmpty(Prams)) {
			for (Map application : Prams) {// 新增配置文件变更应用
				if (CommonUtils.isNullOrEmpty(application)) {
					continue;
				}
				ProdConfigApp prodConfigApp = new ProdConfigApp();
				prodConfigApp.setRelease_node_name((String) application.get(Dict.RELEASE_NODE_NAME));
				prodConfigApp.setApplication_id((String) application.get(Dict.APPLICATION_ID));
				prodConfigApp.setConfig_type(new ArrayList<>());
				prodConfigApp.getConfig_type().add((String) application.get(Dict.CONFIG_TYPE));
				prodConfigApp.setDate(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
				ProdConfigApp ConfigResult = prodConfigService.queryConfigs(prodConfigApp.getApplication_id(),prodConfigApp.getRelease_node_name());
				//没有查询到数据，说明是首次添加，进行保存
				if(CommonUtils.isNullOrEmpty(ConfigResult)){
					//无数据需要将ProdConfigApp创建
					ConfigResult  = new ProdConfigApp();
					List managers = new ArrayList();
					//查询应用并设置应用负责人
					List<Map<String, String>> appDevManager = userService.queryAppDevManager(prodConfigApp.getApplication_id());
					for (Map<String, String> map : appDevManager){
						managers.add(map.get(Dict.USER_NAME_CN));
					}
					prodConfigApp.setDev_managers(managers);
					//是否挂载投产窗口,条件为应用id、当前时间，查询当前时间之后的最近一次投产
					String release_node_name = releaseNodeService.queryNodes(prodConfigApp.getApplication_id(),CommonUtils.formatDate(CommonUtils.DATE_PARSE));
					if(!CommonUtils.isNullOrEmpty(release_node_name)){
						//存在挂载投产窗口时，查询是否已经新建变更。
						List Records = prodRecordService.query(release_node_name);
						for (Object item : Records) {
							Map tmp = (Map) item;
							List<Map> prod_records = (List) tmp.get("prod_records");
							for (Map prod_record : prod_records) {
								List<Map> applications = (List) prod_record.get("applications");
								for (Map tmp2 : applications) {
									//如果新建变更中的应用与当前应用ID相同则将这个新建总介质目录设置到配置变更表
									if(prodConfigApp.getApplication_id().equals(tmp2.get("application_id"))){
										prodConfigApp.setAsset_name((String) prod_record.get(Dict.PROD_ASSETS_VERSION));
									}
								}
							}
						}
						prodConfigApp.setRelease_node(release_node_name);
						prodConfigApp.setIsrisk("1");//是含风险1, 0：否,1:是
						prodConfigApp.setIscheck("1");//已经挂载投产窗口后需要投产牵头人进行审核。
					}else{
						prodConfigApp.setIsrisk("0");//不含风险0, 0：否,1:是
						prodConfigApp.setIscheck("0");//没有挂载投产窗口不需要进行审核。
					}
					Map<String, Object> apps = appService.queryAPPbyid(prodConfigApp.getApplication_id());
					prodConfigApp.setApplication_name((String) apps.get(Dict.NAME_EN));
					prodConfigApp.setStatus("0");//配置文件生成状态默认为0, 0：未生成,1:已生成
					prodConfigApp = prodConfigService.addConfigApplication(prodConfigApp);
				}else{
					if(!ConfigResult.getConfig_type().contains(application.get(Dict.CONFIG_TYPE))){
						//可以查询到数据，说明是第二次添加到配置文件表，进行修改,将实体属性全部整合到一起进行修改。
						prodConfigApp.getConfig_type().addAll(ConfigResult.getConfig_type());
						prodConfigApp.setStatus("0");//二次添加，也将配置文件状态设置为未生成。
						prodConfigApp = prodConfigService.updateConfig(prodConfigApp);
					}

				}
			}
		}
		return JsonResultUtil.buildSuccess();
	}
	/**
	 * query 查询配置文件变更应用
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	@RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/queryConfigApplication")
	public JsonResult queryConfigApplication(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		//参数为投产窗口名， 查询投产应用列表
		String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
		List<ProdConfigApp> result = prodConfigService.queryConfigApplication(release_node_name);
		return JsonResultUtil.buildSuccess(result);
	}

	/**
	 * query 配置文件生成
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/createConfig")
	public JsonResult createConfig(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		List<String> applications = (List<String>) requestParam.get(Dict.APPLICATION_ID);
		//参数为投产窗口名， 查询配置文家变更表
		List<ProdConfigApp> list = prodConfigService.queryConfigList(release_node_name,applications);
		List<Map<String,String>> Prams = new ArrayList<>();
		String prodDateStr=release_node_name.split("_")[0].substring(4,8);
		for (ProdConfigApp app : list) {
			//判断只生成，无风险："0"、是否审核："0"的配置文件
			if("0".equals(app.getIsrisk()) || "0".equals(app.getIscheck())){
				//根据应用id获取应用信息
				Map<String, Object> application = appService.queryAPPbyid(app.getApplication_id());
				Map map = new HashMap();
				//获取当前日期之前包括当前日期,投产tag包作为本次配置文件镜像标签。
				String old_tag = prodApplicationService.findLatestTag(app.getApplication_id(),Dict.CAAS);

				if(CommonUtils.isNullOrEmpty(old_tag)) {
					throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行重启操作！"+application.get(Dict.NAME_EN)});
				}

				ProdConfigApp configApp = new ProdConfigApp();
				//保存生成配置文件信息
				//上次投产镜像+_config+投产日期:pro-20201201_007-003_config1201
				String tag = old_tag.split(":")[1]+Dict._CONFIG+prodDateStr;
				//将成功生成配置文件的数据保存到配置变更表中
				configApp.setApplication_id(app.getApplication_id());
				configApp.setRelease_node_name(release_node_name);
				configApp.setTag(tag);//将上次投产的镜像标签+"_config"+投产日期存放到配置变更表中
				configApp.setStatus("1");//生成配置文件后将状态修改为已生成。
				configApp = prodConfigService.updateStatus(configApp);
				//发送生成配置文件
				String  gitlabId= String.valueOf(application.get(Dict.GITLAB_PROJECT_ID));
				map.put("gitlabId",gitlabId);//应用gitlab_id
				map.put("appName",application.get(Dict.NAME_EN));//应用英文名
				map.put("appId",app.getApplication_id());//应用id
				//上次投产镜像标签,作为配置文件镜像tag,命名规则:上次投产的镜像标签+"config"+投产日期
				map.put("tagName",old_tag.split(":")[1]+Dict._CONFIG+prodDateStr);
				Prams.add(map);

			}
		}
		//将应用集合批量发送给环境配置模块进行配置文件生成。
		List<Map<String,Object>>ConfigFile = prodConfigService.batchSaveConfigFile(Prams);
		List<Map> result = new ArrayList<>();
		for (Map map :	ConfigFile) {
			String application_id = (String) map.get("appId");
			//配置文件生成失败会将失败数据返回,将失败数据更新为未生成.
			if("0".equals(map.get("flag"))){//flag:1: 成功 0：错误
				ProdConfigApp configApp = new ProdConfigApp();
				configApp.setApplication_id(application_id);
				configApp.setRelease_node_name(release_node_name);
				configApp.setStatus("0");//生成配置文件后将状态修改为未生成。
				configApp = prodConfigService.updateStatus(configApp);
				Map configAppMap = CommonUtils.beanToMap(configApp);
                configAppMap.put("errorInfo",map.get("errorInfo"));//将错误信息返回前端
				result.add(configAppMap);
			}
		}



		return JsonResultUtil.buildSuccess(result);
	}
	/**
	 * update 修改审核状态
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME, Dict.APPLICATION_ID })
	@PostMapping(value = "/checkConfigApplication")
	public JsonResult checkConfigApplication(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String application_id = requestParam.get(Dict.APPLICATION_ID);
		String ischeck = "0";//是否审核，设置为否，不显示审核
		List<Map<String, Object>> result = new ArrayList<>();

		ProdConfigApp prodConfigApp = prodConfigService.checkConfigApplication(release_node_name,application_id,ischeck);

		return JsonResultUtil.buildSuccess();
	}
	/**
	 * delete 删除配置文件变更应用
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME, Dict.APPLICATION_ID })
	@PostMapping(value = "/deleteConfig")
	public JsonResult deleteConfig(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {

		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String application_id = requestParam.get(Dict.APPLICATION_ID);
		List<Map<String, Object>> result = new ArrayList<>();

		prodConfigService.deleteConfig(release_node_name,application_id);

		return JsonResultUtil.buildSuccess();
	}
	/**
	 * query 查询变更记录
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	@RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/queryConfigSum")
	public JsonResult queryConfigSum(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		//参数为投产窗口名， 查询投产应用列表
		String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
		List<ProdConfigApp> result = prodConfigService.queryConfigSum(release_node_name);
		Map<String,String> map = new HashMap<String,String>();
		if(result.size()==0){
			map.put(Dict.ISCONFIGSUM,"2");
			return JsonResultUtil.buildSuccess(map);
        }
		for (ProdConfigApp app : result) {
			//有数据未生成配置文件，返回状态1：部分生成
			if("0".equals(app.getStatus())){
				map.put(Dict.ISCONFIGSUM,"1");
				return JsonResultUtil.buildSuccess(map);
			}
		}
		//数据全部生成配置文件，返回状态0：全部生成
		map.put(Dict.ISCONFIGSUM,"0");
		return JsonResultUtil.buildSuccess(map);
	}



}
