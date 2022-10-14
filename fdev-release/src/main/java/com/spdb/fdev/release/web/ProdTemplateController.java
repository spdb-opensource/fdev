package com.spdb.fdev.release.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "投产管理接口")
@RequestMapping("/api/template")
@RestController
public class ProdTemplateController {

	@Autowired
	private IUserService userService;
	@Autowired
	private ITaskService taskService;
	@Autowired
	private IAppService appService;
	@Autowired
	private IRoleService iRoleService;
	@Autowired
	private IOptionalCatalogService optionalCatalogService;
	@Autowired
	private IProdTemplateService prodTemplateService;
	@Autowired
	private IReleaseCatalogService catalogService;
	@Autowired
	private IProdRecordService prodRecordService;
	@Autowired
	private ISystemReleaseInfoService systemReleaseInfoService;
	@Autowired
	private IProdAssetService prodAssetService;
	@Autowired
	private IReleaseNodeService releaseNodeService;
	@Autowired
    private ITemplateDocumentService templateDocumentService;
	@Autowired
	UserVerifyUtil userVerifyUtil;
	@Autowired
	IRoleService roleService;
	private static Logger logger = LoggerFactory.getLogger(ProdTemplateController.class);

	@OperateRecord(operateDiscribe="投产模块-新增变更模板")
	@RequestValidate(NotEmptyFields = { Dict.OWNER_SYSTEM, Dict.TEMPLATE_TYPE})
	@PostMapping(value = "/create")
	public JsonResult createTemplate(@RequestBody Map<String, Object> requestParam) throws Exception {
		if (!iRoleService.isReleaseManager()) {
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		String owner_system = (String) requestParam.get(Dict.OWNER_SYSTEM);
		String owner_app = (String) requestParam.get(Dict.OWNER_APP);
		String template_type = (String) requestParam.get(Dict.TEMPLATE_TYPE);
		String catalogs = (String) requestParam.get(Dict.CATALOGS);
		ProdTemplate prodTemplate = new ProdTemplate();
		prodTemplate.setOwner_system(owner_system);
		prodTemplate.setTemplate_type(template_type);
		if (!CommonUtils.isNullOrEmpty(owner_app)) {
			prodTemplate.setOwner_app(owner_app);
		}
		String date = CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN);
		Map<String,Object> owner_group = (Map<String, Object>) requestParam.get(Dict.OWNER_GROUP);
		prodTemplate.setOwner_group((String) owner_group.get(Dict.OWNER_GROUPID));
		prodTemplate.setUpdate_time(date);
		prodTemplate.setUpdate_user(CommonUtils.getSessionUser().getId());
		ProdTemplate rt;
		List<ProdTemplate> list = prodTemplateService.findExists(prodTemplate);
		if(list.size() > 0) {
			String system_name = taskService.querySystemName(owner_system);
			String template_type_name = Dict.GRAY.equals(template_type) ? "灰度" : "生产";
			throw new FdevException(ErrorConstants.TEMPLATE_EXISTS, new String[] { system_name + "下" + template_type_name + "类型" });
		}
		rt = prodTemplateService.create(prodTemplate);
		Map map = CommonUtils.beanToMap(rt);
		if (CommonUtils.isNullOrEmpty(rt.getOwner_app())) {
			map.put(Dict.OWNER_APP_NAME, "");
		} else {
			Map appMap = appService.queryAPPbyid(prodTemplate.getOwner_app());
			map.put(Dict.OWNER_APP_NAME, appMap == null ? "" : appMap.get(Dict.APP_NAME_ZH));
		}
		if (!CommonUtils.isNullOrEmpty(catalogs)) {
			ArrayList<AssetCatalog> catalogList = new ObjectMapper().readValue(catalogs,
					new TypeReference<ArrayList<AssetCatalog>>() {
					});
			Set set = new HashSet();
			for (AssetCatalog c : catalogList) {
				set.add(c.getCatalog_name());
			}
			if (set.size() < catalogList.size()) {
			  TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			  throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { Dict.CATALOG_NAME, "变更目录名重复!" });
			}
			for (AssetCatalog assetCatalog : catalogList) {
				assetCatalog.setTemplate_id(rt.getId());
				catalogService.create(assetCatalog);
			}
			map.put(Dict.CATALOGS, catalogList);
		} else {
			map.put(Dict.CATALOGS, "");
		}
		SystemReleaseInfo systemReleaseInfo = systemReleaseInfoService.querySysRlsDetail(owner_system);
		map.put(Dict.UPDATE_USERNAME_CN, CommonUtils.getSessionUser().getUser_name_cn());
		map.put("systemReleaseInfo", systemReleaseInfo);
		return JsonResultUtil.buildSuccess(map);
	}

	@OperateRecord(operateDiscribe="投产模块-修改变更模板")
	@RequestValidate(NotEmptyFields = {Dict.ID})
	@PostMapping(value = "/update")
	public JsonResult updateTemplate(@RequestBody Map<String,Object> requestParam) throws Exception {
		Map result = updateProdTemplate((String) requestParam.get(Dict.ID),
				(Map<String,Object>) requestParam.get(Dict.OWNER_GROUP),
				(String) requestParam.get(Dict.CATALOGS));
		return JsonResultUtil.buildSuccess(result);
	}

	private Map updateProdTemplate(String id, Map<String,Object> owner_group, String catalogs)
			throws Exception {
		/*if (!iRoleService.isReleaseManager()) {
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}*/
		ProdTemplate old = new ProdTemplate();
		old.setId(id);
		List<ProdTemplate> list = prodTemplateService.query(old);
		if (null == list || list.size() <= 0) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "根据ID查询的模板不存在!" });
		}
		old = list.get(0);
		/*if(!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
			if(!CommonUtils.isNullOrEmpty(old.getOwner_group())
					&& !user.getGroup_id().equals(owner_group)){
				throw new FdevException(ErrorConstants.ROLE_ERROR);
			}
		}*/
		if(!CommonUtils.isNullOrEmpty(old.getOwner_group()) && !roleService.isGroupReleaseManager((String) owner_group.get(Dict.OWNER_GROUPID))){
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		if (CommonUtils.isNullOrEmpty(catalogs)) {
			catalogService.deleteByTemplateId(id);
		} else {
//			ObjectMapper objectMapper = new ObjectMapper();
//			objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
			ArrayList<AssetCatalog> qcatalogList = new ObjectMapper().readValue(catalogs,
					new TypeReference<ArrayList<AssetCatalog>>() {
					});
			Set set = new HashSet();
			for (AssetCatalog c : qcatalogList) {
				set.add(c.getCatalog_name());
			}
			if (set.size() < qcatalogList.size()) {
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { Dict.CATALOG_NAME, "变更目录名已存在!" });
			}
			List<AssetCatalog> hcatalogList = catalogService.query(id);
			if (CommonUtils.isNullOrEmpty(hcatalogList)) {
				for (AssetCatalog catalog : qcatalogList) {
					catalog.setTemplate_id(id);
					catalogService.create(catalog);
				}
			} else {
				for (Iterator<AssetCatalog> ac = hcatalogList.iterator(); ac.hasNext();) {
					AssetCatalog hcatalog = ac.next();
					for (Iterator<AssetCatalog> catalog = qcatalogList.iterator(); catalog.hasNext();) {
						AssetCatalog qcatalog = catalog.next();
						if (CommonUtils.isNullOrEmpty(qcatalog.getId())){
							qcatalog.setTemplate_id(id);
							catalogService.create(qcatalog);
							catalog.remove();
							continue;
						} else if (hcatalog.getId().equals(qcatalog.getId())) {
							catalogService.update(qcatalog);
							catalog.remove();
							ac.remove();
							continue;
						} else {
							continue;
						}
					}
				}
				if (!CommonUtils.isNullOrEmpty(hcatalogList)) {
					for (AssetCatalog ac : hcatalogList) {
						if(prodAssetService.isAssetCatalogUsed(id, ac.getCatalog_name())) {
							throw new FdevException(ErrorConstants.ASSET_CALALOG_IS_USED);
						}
						catalogService.delete(ac.getId());
					}
				}
			}
		}
		ProdTemplate prodTemplate = new ProdTemplate();
		prodTemplate.setId(id);
		String date = CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN);
		prodTemplate.setUpdate_time(date);
		prodTemplate.setUpdate_user(CommonUtils.getSessionUser().getId());
		prodTemplate.setOwner_group((String) owner_group.get(Dict.OWNER_GROUPID));
		prodTemplate = prodTemplateService.update(prodTemplate);
		Map result = CommonUtils.beanToMap(prodTemplate);
		if (CommonUtils.isNullOrEmpty(prodTemplate.getOwner_app())) {
			result.put(Dict.OWNER_APP_NAME, "");
		} else {
			Map appMap = appService.queryAPPbyid(prodTemplate.getOwner_app());
			result.put(Dict.OWNER_APP_NAME, appMap == null ? "" : appMap.get(Dict.NAME_ZH));
		}
		result.put(Dict.UPDATE_USERNAME_CN, CommonUtils.getSessionUser().getUser_name_cn());
		return result;
	}

	@OperateRecord(operateDiscribe="投产模块-删除变更模板")
	@RequestValidate(NotEmptyFields = { Dict.ID })
	@PostMapping(value = "/delete")
	public JsonResult deleteTemplate(@RequestBody @ApiParam Map requestParam) throws Exception {
		String template_id = (String) requestParam.get(Dict.ID);
		ProdTemplate prodTemplate = new ProdTemplate();
		prodTemplate.setId(template_id);
		List<ProdTemplate> list = prodTemplateService.query(prodTemplate);
		if (CommonUtils.isNullOrEmpty(list)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
		}
		ProdTemplate newProdTemplate = list.get(0);
		User user = CommonUtils.getSessionUser();
		/*if(!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
			if (!iRoleService.isReleaseManager() ||
					(!CommonUtils.isNullOrEmpty(newProdTemplate.getOwner_group()) && //模板所属组不为空
							!CommonUtils.getSessionUser().getGroup_id().equals(newProdTemplate.getOwner_group()))) {//模板所属组与用户组不同
				throw new FdevException(ErrorConstants.ROLE_ERROR);
			}
		}*/
		if(!CommonUtils.isNullOrEmpty(newProdTemplate.getOwner_group()) && !roleService.isGroupReleaseManager(newProdTemplate.getOwner_group())){
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		//返回 0-被使用 但已投产  1-正在被使用  2-未被使用
		Integer status = isTemplateUsed(template_id);
		if(status == 1) {
			throw new FdevException(ErrorConstants.TEMPLATE_IS_USED_CAN_NOT_DELETE);
		}

		//对于已使用但所在投产窗口已归档的变更模版可删除
		if (status == 0) {
			//删除模版事只是修改模版的状态为 已废弃
			prodTemplateService.cancel(template_id);
		}else {//若未使用则直接删除
			catalogService.delete(template_id);
			prodTemplateService.delete(template_id);
		}
		return JsonResultUtil.buildSuccess();
	}

	public Integer isTemplateUsed(String template_id) throws Exception{
		List<ProdRecord> proRecord_list = prodRecordService.queryByTemplateId(template_id);
		if (!CommonUtils.isNullOrEmpty(proRecord_list)) {
			for (ProdRecord prodRecord : proRecord_list) {
				ReleaseNode releaseNode = releaseNodeService.queryDetail(prodRecord.getRelease_node_name());
				if(!CommonUtils.isNullOrEmpty(releaseNode) && !TimeUtils.compareDate(releaseNode.getRelease_date())) {
					logger.info("正在使用该变更模板的投产窗口名：{}，投产日期为：{}",
							releaseNode.getRelease_node_name(), releaseNode.getRelease_date());
					return 1;
				}
			}
		} else {
			return 2;
		}
		return 0;
	}

	@PostMapping(value = "/query")
	public JsonResult queryTemplate(@RequestBody @ApiParam Map requestParam) throws Exception {
		ProdTemplate rt = CommonUtils.mapToBean(requestParam, ProdTemplate.class);
		if(!CommonUtils.isNullOrEmpty(rt.getOwner_group())) {
			List<Map<String, Object>> parentGroups = userService.queryParentGroupByGroupId(rt.getOwner_group());
			List<Map<String, Object>> childGroups = userService.queryChildGroupByGroupId(rt.getOwner_group());
			Set<String> groups = new HashSet<>();
			groups.add(rt.getOwner_group());
			for(Map<String, Object> map : parentGroups) {
				groups.add((String) map.get(Dict.ID));
			}
			for(Map<String, Object> map : childGroups) {
				groups.add((String) map.get(Dict.ID));
			}
			String groupIds = String.join(",", groups);
			logger.info("查询所有组id：{}", groupIds);
			rt.setOwner_group(groupIds);
		}
		List<ProdTemplate> result = prodTemplateService.query(rt);
		if (CommonUtils.isNullOrEmpty(result)) {
			return JsonResultUtil.buildSuccess(new ArrayList<>());
		}
		ArrayList array = new ArrayList();
		for (int i = 0; i < result.size(); i++) {
			ArrayList<AssetCatalog> list = (ArrayList) catalogService.query(result.get(i).getId());
			if (CommonUtils.isNullOrEmpty(list)) {
				list = new ArrayList();
			}
			Map userMap = userService.queryUserById(result.get(i).getUpdate_user());
			Map map = CommonUtils.beanToMap(result.get(i));
			map.put(Dict.UPDATE_USERNAME_CN, userMap == null ? "" : userMap.get(Dict.USER_NAME_CN));
			if (CommonUtils.isNullOrEmpty(result.get(i).getOwner_app())) {
				map.put(Dict.OWNER_APP_NAME, "");
			} else {
				Map appMap = appService.queryAPPbyid(result.get(i).getOwner_app());
				map.put(Dict.OWNER_APP_NAME, appMap == null ? "" : appMap.get(Dict.NAME_ZH));
			}
			if (CommonUtils.isNullOrEmpty(result.get(i).getOwner_system())) {
				map.put(Dict.OWNER_SYSTEM_NAME, "");
			} else {
				String systemName = taskService.querySystemName((result.get(i).getOwner_system()));
				map.put(Dict.OWNER_SYSTEM_NAME, systemName);
			}
			if (CommonUtils.isNullOrEmpty(result.get(i).getOwner_group())) {
				map.put(Dict.OWNER_GROUP_NAME, "");
			} else {
				String groupName = (String)userService.queryGroupDetail(result.get(i).getOwner_group()).get(Dict.NAME);
				map.put(Dict.OWNER_GROUP_NAME, groupName);
			}
			map.put(Dict.CATALOGS, list);
			array.add(map);
		}
		return JsonResultUtil.buildSuccess(array);
	}

	@PostMapping(value = "/queryDetail")
	public JsonResult queryTemplateDetail(@RequestBody @ApiParam ProdTemplate requestParam) throws Exception {
		String id = requestParam.getId();
		if (CommonUtils.isNullOrEmpty(id)) {
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "模版", "模版ID不允许为空!" });
		}
		ProdTemplate template = prodTemplateService.queryDetail(requestParam);
		Map result = CommonUtils.beanToMap(template);
		if (CommonUtils.isNullOrEmpty(template.getOwner_app())) {
			result.put(Dict.OWNER_APP_NAME, "");
		} else {
            Map appMap = appService.queryAPPbyid(template.getOwner_app());
            result.put(Dict.OWNER_APP_NAME, appMap == null ? "" : appMap.get(Dict.NAME_ZH));
        }
		if (CommonUtils.isNullOrEmpty(template.getOwner_system())) {
			result.put(Dict.OWNER_SYSTEM_NAME, "");
		} else {
			String systemName = taskService.querySystemName((template.getOwner_system()));
			result.put(Dict.OWNER_SYSTEM_NAME, systemName);
		}
		if (CommonUtils.isNullOrEmpty(template.getOwner_group())) {
			result.put(Dict.OWNER_GROUP_NAME, "");
		} else {
			String groupName = (String)userService.queryGroupDetail(template.getOwner_group()).get(Dict.NAME);
			result.put(Dict.OWNER_GROUP_NAME, groupName);
		}
		Map map = userService.queryUserById(template.getUpdate_user());
		result.put(Dict.UPDATE_USERNAME_CN, map == null ? "" : map.get(Dict.USER_NAME_CN));
		List<AssetCatalog> list = catalogService.query(id);
		if (CommonUtils.isNullOrEmpty(list)) {
			list = new ArrayList();
		}
		result.put(Dict.CATALOGS, list);
		logger.debug("查询模版明细  返回 ：{} ", result);
		return JsonResultUtil.buildSuccess(result);
	}

	@PostMapping(value = "/queryOptionalCatalog")
	public JsonResult queryOptionalCatalog() {
		// 查询不带id且全部查出
		List<OptionalCatalog> list = optionalCatalogService.queryAll();
		return JsonResultUtil.buildSuccess(list);
	}

	/**
	 * 查询所属系统
	 * @param requestParam 变更类型与投产窗口所属小组id
	 * @return
	 * @throws Exception
	 */
	@RequestValidate(NotEmptyFields = {Dict.TEMPLATE_TYPE, Dict.OWNER_GROUP})
	@PostMapping(value = "/querySystem")
	public JsonResult querySystem(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String template_type = requestParam.get(Dict.TEMPLATE_TYPE);
		String owner_group = requestParam.get(Dict.OWNER_GROUP);
		ProdTemplate prodTemplate = new ProdTemplate();
		prodTemplate.setOwner_group(owner_group);
		prodTemplate.setTemplate_type(template_type);
		List<ProdTemplate> list = prodTemplateService.queryByGroupType(prodTemplate);
		// 若查询当前组当前变更类型没有模板，则去找当前组的第三层级组在查一次库
		if(CommonUtils.isNullOrEmpty(list)){
			Map<String,Object> three_level_group_map = userService.getThreeLevelGroup(owner_group);
			String three_group_id = (String) three_level_group_map.get(Dict.ID);
			prodTemplate.setOwner_group(three_group_id);
			list = prodTemplateService.queryByGroupType(prodTemplate);
		}
		Set<Map<String, Object>> set = new HashSet<>();
		for(ProdTemplate template : list) {
			set.add(taskService.querySystemById(template.getOwner_system()));
		}
		return JsonResultUtil.buildSuccess(set);
	}

	@RequestValidate(NotEmptyFields = {Dict.SYSNAME_CN, Dict.TEMPLATE_TYPE})
	@PostMapping(value = "/queryExcelTemplate")
	public JsonResult queryExcelTemplate(@RequestBody @ApiParam Map<String, String> requestParam) {
		String sysname_cn = requestParam.get(Dict.SYSNAME_CN);
		String template_type = requestParam.get(Dict.TEMPLATE_TYPE);
        List<String> documentFiles = templateDocumentService.getDocumentList(sysname_cn, template_type);
        List<String> commonFiles = templateDocumentService.getDocumentList("全网通用", null);
		documentFiles.addAll(commonFiles);
		List<Map<String, String>> excel_list = new ArrayList<>();
		for (String documentFile : documentFiles) {
			Map<String, String> map = new HashMap<>();
			String[] split = documentFile.split("/");
			String fileName = split[split.length - 1];
			map.put(Dict.URL, documentFile);
			map.put(Dict.FILENAME, fileName);
			excel_list.add(map);
		}
		return JsonResultUtil.buildSuccess(excel_list);
	}

}
