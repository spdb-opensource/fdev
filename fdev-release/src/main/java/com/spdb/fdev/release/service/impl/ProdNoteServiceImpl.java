package com.spdb.fdev.release.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.csii.pe.redis.util.Util;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Validator;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IAssetCatalogDao;
import com.spdb.fdev.release.dao.IBatchTaskDao;
import com.spdb.fdev.release.dao.INoteConfigurationDao;
import com.spdb.fdev.release.dao.INoteServiceDao;
import com.spdb.fdev.release.dao.INoteSqlDao;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.dao.IProdNoteDao;
import com.spdb.fdev.release.entity.AssetCatalog;
import com.spdb.fdev.release.entity.BatchTaskInfo;
import com.spdb.fdev.release.entity.NoteConfiguration;
import com.spdb.fdev.release.entity.NoteManual;
import com.spdb.fdev.release.entity.NoteService;
import com.spdb.fdev.release.entity.NoteSql;
import com.spdb.fdev.release.entity.ProdNote;
import com.spdb.fdev.release.entity.ProdTemplate;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IFileService;
import com.spdb.fdev.release.service.IProdNoteService;
import com.spdb.fdev.release.service.IProdTemplateService;
import com.spdb.fdev.release.service.IReleaseNodeService;
import com.spdb.fdev.release.service.IRoleService;
import com.spdb.fdev.release.service.ITaskService;
import com.spdb.fdev.release.service.IUserService;
import com.spdb.fdev.transport.RestTransport;

@Service
@RefreshScope
public class ProdNoteServiceImpl implements IProdNoteService  {

	@Value("${scripts.path}")
	private String scripts_path;
	@Autowired
	private IProdNoteDao prodNoteDao;
	@Autowired
	private IProdTemplateService prodTemplateService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IReleaseNodeService releaseNodeService;
	@Autowired
	private ITaskService taskService;
	@Autowired
	private IRoleService roleService;
    @Autowired
    private IAssetCatalogDao assetCatalogDao;
    
    @Autowired
    private INoteServiceDao noteServiceDao;
    @Autowired
    private INoteSqlDao noteSqlDao;
    
    @Autowired
    private INoteConfigurationDao noteConfigurationDao;
    
    @Autowired
    private IFileService fileService;
    
    @Autowired
    private IAppService appService;
    
    @Autowired
    private IBatchTaskDao batchTaskDao;
    
    @Value("${docmanage.file.url}")
    private String docFileUrl;
    @Autowired
	private IProdApplicationDao prodApplicationDao;
    @Autowired
    private RestTransport restTransport;
	
	private static Logger logger = LoggerFactory.getLogger(ProdNoteServiceImpl.class);
	
	
	@Override
	public void createNote(Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String release_date = (String) requestParam.get(Dict.DATE);
		String owner_groupId = (String) requestParam.get(Dict.OWNER_GROUPID);
		String plan_time = (String) requestParam.get(Dict.PLAN_TIME);
		String image_deliver_type = (String) requestParam.get(Dict.IMAGE_DELIVER_TYPE);//是否自动化发布
		String release_note_name = (String) requestParam.get("release_note_name");//发布说明名称
		String version = (String) requestParam.get(Dict.VERSION);
		String type = (String) requestParam.get(Dict.TYPE);
		String owner_system = (String) requestParam.get(Dict.OWNER_SYSTEM);
		String namespace = (String) requestParam.get(Dict.NAMESPACE);
		String leaseholder = (String) requestParam.get("leaseholder");
		String systemType = (String) requestParam.get("systemType");
		String system_name = "";
		if (CommonUtils.isNullOrEmpty(systemType)) {
			if(!CommonUtils.isNullOrEmpty(owner_system)){
				system_name = taskService.querySystemName(owner_system);
			}
		} else {
			system_name = "网银系统群_批量";
		}
		// 校验变更单号格式
		if(!Validator.versionValidate(version)){
			logger.error("version is not permitted");
			throw new FdevException(ErrorConstants.VERSION_NOT_PERMIT);
		}
		//校验当前用户创建变更权限
		if(!roleService.isGroupReleaseManager(owner_groupId)) {
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		//变更版本防重
		if(!CommonUtils.isNullOrEmpty(prodNoteDao.queryProdByVersion(version))){
			throw new FdevException(ErrorConstants.PRODNOTE_REPEAT);
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
		ProdNote checkRepect = prodNoteDao.queryProdRecordByVersion(release_node_name, version);
		if (!CommonUtils.isNullOrEmpty(checkRepect)) {// version不能重复
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "version is repeat" });
		}
		//查询窗口所属小组
		Map<String, Object> groupDetail = userService.queryGroupDetail(owner_groupId);
		if (CommonUtils.isNullOrEmpty(groupDetail)) {
			logger.error("can't find the group info !@@@@@group_id={}", owner_groupId);
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "组信息不存在" });
		}
		ProdNote prodNote = new ProdNote();
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
					throw new FdevException(ErrorConstants.TEMPLATE_NOT_EXISTS, new String[]{"当前组下" + system_name + template_type_name});
				} else {
					throw new FdevException(ErrorConstants.TEMPLATE_MUST_BE_DELETE, new String[]{"当前组下" + system_name + template_type_name});
				}
			}
			prodTemplate = prodTemplates.get(0);
			prodNote.setTemplate_id(prodTemplate.getId());
		}
		//获取当前用户
		User user = CommonUtils.getSessionUser();
		ObjectId objectId = new ObjectId();
		prodNote.set_id(objectId);
		prodNote.setNote_id(objectId.toString());
		prodNote.setRelease_node_name(release_node_name);
		prodNote.setDate(release_date);
		prodNote.setVersion(version);
		prodNote.setType(type);
		prodNote.setLauncher(user.getId());
		prodNote.setLauncher_name_cn(user.getUser_name_cn());
		prodNote.setCreate_time(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
		if (CommonUtils.isNullOrEmpty(systemType)) {
			prodNote.setOwner_system(owner_system);
		} else {
			prodNote.setOwner_system("");
		}
		prodNote.setOwner_system_name(system_name);
		prodNote.setOwner_groupId(owner_groupId);
		prodNote.setPlan_time(plan_time);
		prodNote.setImage_deliver_type(image_deliver_type);
		prodNote.setRelease_note_name(release_note_name);
		prodNote.setLock_flag("1");
		prodNote.setNamespace(namespace);
		prodNote.setLeaseholder(leaseholder);
		prodNoteDao.create(prodNote);
	}

	@Override
	public List<Map<String, Object>> queryReleaseNote(String release_node_name) throws Exception {
		List<ProdNote> prodNotes = prodNoteDao.query(release_node_name);
		List<Map<String, Object>> prodNoteList = new ArrayList<>();
		for (ProdNote map : prodNotes) {
			Map<String, Object> prodNoteMap = new HashMap<>();
			String owner_group_name = userService.queryGroupNameById(map.getOwner_groupId());
			prodNoteMap.put(Dict.NOTE_ID, map.getNote_id());
			prodNoteMap.put(Dict.RELEASE_NODE_NAME, map.getRelease_node_name());
			prodNoteMap.put(Dict.DATE, map.getDate());
			prodNoteMap.put(Dict.VERSION, map.getVersion());
			prodNoteMap.put(Dict.TYPE, map.getType());
			prodNoteMap.put("launcher", map.getLauncher());
			prodNoteMap.put("launcher_name_cn", map.getLauncher_name_cn());
			prodNoteMap.put(Dict.CREATE_TIME, map.getCreate_time());
			prodNoteMap.put(Dict.OWNER_SYSTEM, map.getOwner_system());
			prodNoteMap.put(Dict.OWNER_SYSTEM_NAME, map.getOwner_system_name());
			prodNoteMap.put(Dict.OWNER_GROUPID, map.getOwner_groupId());
			prodNoteMap.put(Dict.OWNER_GROUP_NAME, owner_group_name);
			prodNoteMap.put(Dict.PLAN_TIME, map.getPlan_time());
			prodNoteMap.put(Dict.IMAGE_DELIVER_TYPE, map.getImage_deliver_type());
			prodNoteMap.put("lock_flag", map.getLock_flag());
			prodNoteMap.put("lock_people", map.getLock_people());
			prodNoteMap.put("lock_name_cn", map.getLock_name_cn());
			prodNoteMap.put("release_note_name", map.getRelease_note_name());
			prodNoteMap.put(Dict.NAMESPACE, map.getNamespace());
			prodNoteMap.put(Dict.TEMPLATE_ID, map.getTemplate_id());
			prodNoteMap.put("note_download_url", map.getNote_download_url());
			prodNoteList.add(prodNoteMap);
		}
		return prodNoteList;
	}

	@Override
	public Map queryNoteDetail(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		ProdNote prodNote = prodNoteDao.queryDetail(note_id);
		if (!CommonUtils.isNullOrEmpty(prodNote)) {
			if (!CommonUtils.isNullOrEmpty(prodNote.getLauncher())) {
				Map<String, Object> user = userService.queryUserById(prodNote.getLauncher());
				prodNote.setLauncher_name_cn(
						CommonUtils.isNullOrEmpty(user) ? "" : (String) user.get(Dict.USER_NAME_CN));
			}
		}
		return CommonUtils.beanToMap(prodNote);
	}

	@Override
	public void deleteNote(Map<String, Object> requestParam) throws Exception {
		String image_deliver_type = (String) requestParam.get(Dict.IMAGE_DELIVER_TYPE);
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		if ("1".equals(image_deliver_type)) {
			prodNoteDao.delete(note_id);
			prodNoteDao.deleteNoteConfigurationByNoteId(note_id);
			prodNoteDao.deleteNoteSqlByNoteId(note_id);
			prodNoteDao.deleteNoteSreviceByNoteId(note_id);
			prodNoteDao.deleteBatchInfoByNoteId(note_id);
		} else if ("2".equals(image_deliver_type)) {
			prodNoteDao.delete(note_id);
			prodNoteDao.deleteNoteManualByNoteId(note_id);
		}
	}

	@Override
	public void addNoteSrevice(Map<String, Object> requestParam) throws Exception {
		String application_name_en = (String) requestParam.get(Dict.APPLICATION_NAME_EN);
		String application_name_cn = (String) requestParam.get(Dict.APPLICATION_NAME_CN);
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		List<Map<String, Object>> dev_managers_info = (List<Map<String, Object>>) requestParam.get("dev_managers_info");
		String tag_name = (String) requestParam.get(Dict.TAG_NAME);
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String application_type = (String) requestParam.get("application_type");
		Map<String, Object> expand_info = (Map<String, Object>) requestParam.get("expand_info");
		String leaseholder = (String) requestParam.get("leaseholder");
		String leaseholderInfo = leaseholder.split("/")[0];
		ProdNote prodNote = prodNoteDao.queryDetail(note_id);
		String type = prodNote.getType();// gray-灰度  proc-生产
		String namespace = prodNote.getNamespace();// 1-业务 2-网银
		String lastTag = prodApplicationDao.queryLastTagByGitlabId(application_id,"",type,null);
		List<String> keys = new ArrayList<>();
		keys.add("fdev_caas_tenant");
		String envEnName = null;
		if ("2".equals(namespace) && "gray".equals(type)) {
			envEnName = "sh-k1-dmz-gray";
		} else if ("1".equals(namespace) && "proc".equals(type)) {
			envEnName = "sh-k1-biz";
		} else if ("1".equals(namespace) && "gray".equals(type)) {
			envEnName = "sh-k1-biz-gray";
		} else if ("2".equals(namespace) && "proc".equals(type)) {
			envEnName = "sh-k1-dmz";
		}
		if (!"ebankbat-tenant".equals(leaseholder)) {
			Map<String, Object> send_map = new HashMap<>();
	        send_map.put("app_id", application_id);
	        send_map.put(Dict.REST_CODE, "queryDeploy");
	        Map<String, Object> applictionEntity = (Map<String, Object>) restTransport.submit(send_map);
	        Map<String, Object> caas_model_env_mapping = (Map<String, Object>) applictionEntity.get("caas_model_env_mapping");
	        List<String> leaseholderOld = new ArrayList<>();
	        if ("gray".equals(type)) {
	        	List<Map<String, Object>> caasModelEnvBIZK1 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境业务网段上海k1灰度");
	        	List<Map<String, Object>> caasModelEnvDMZK1 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境网银网段上海k1灰度");
	        	List<Map<String, Object>> caasModelEnvBIZK2 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境业务网段上海k2灰度");
	        	List<Map<String, Object>> caasModelEnvDMZK2 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境网银网段上海k2灰度");
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvBIZK1)) {
					for (Map<String, Object> map : caasModelEnvBIZK1) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvDMZK1)) {
					for (Map<String, Object> map : caasModelEnvDMZK1) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvBIZK2)) {
					for (Map<String, Object> map : caasModelEnvBIZK2) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvDMZK2)) {
					for (Map<String, Object> map : caasModelEnvDMZK2) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
			} else {
				List<Map<String, Object>> caasModelEnvBIZK1 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境业务网段上海k1");
	        	List<Map<String, Object>> caasModelEnvDMZK1 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境网银网段上海k1");
	        	List<Map<String, Object>> caasModelEnvBIZK2 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境业务网段上海k2");
	        	List<Map<String, Object>> caasModelEnvDMZK2 = (List<Map<String, Object>>) caas_model_env_mapping.get("生产环境网银网段上海k2");
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvBIZK1)) {
					for (Map<String, Object> map : caasModelEnvBIZK1) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvDMZK1)) {
					for (Map<String, Object> map : caasModelEnvDMZK1) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvBIZK2)) {
					for (Map<String, Object> map : caasModelEnvBIZK2) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
		        if (!CommonUtils.isNullOrEmpty(caasModelEnvDMZK2)) {
					for (Map<String, Object> map : caasModelEnvDMZK2) {
						if ("零售微服务dce".equals(map.get("name_cn"))) {
							List<Map<String, Object>> env_key = (List<Map<String, Object>>) map.get("env_key");
							for (Map<String, Object> map2 : env_key) {
								if ("fdev_caas_tenant".equals(map2.get("name_en"))) {
									leaseholderOld.add((String) map2.get("value"));
									break;
								}
							}
						}
					}
				}
			}
	        if (CommonUtils.isNullOrEmpty(leaseholderOld) || !leaseholderOld.contains(leaseholderInfo)) {
	        	throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_LEASEHOLDER, new String[]{leaseholder});
			}
		}
		NoteService noteService = new NoteService();
		ObjectId objectId = new ObjectId();
		if ("docker_restart".equals(application_type)) {
			//获取当前日期之前包括当前日期,投产tag包作为本次配置文件镜像标签。
			if(CommonUtils.isNullOrEmpty(lastTag)) {
				throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行重启操作！"+application_name_en});
			}
		}
		if ("stop_all".equals(application_type)) {
			//获取当前日期之前包括当前日期,投产tag包作为本次配置文件镜像标签。
			if(CommonUtils.isNullOrEmpty(lastTag)) {
				throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行停止操作！"+application_name_en});
			}
		}
		if ("docker_scale".equals(application_type)) {
			if(CommonUtils.isNullOrEmpty(lastTag)) {
				throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行弹性拓展操作！"+application_name_en});
			}
			noteService.setExpand_info(expand_info);
		}
		if ("docker".equals(application_type) || "docker_yaml".equals(application_type)) {
			noteService.setTag_name(tag_name);
		} else {
			noteService.setTag_name(lastTag);
		}
		noteService.set_id(objectId);
		noteService.setCatalog_type("1");
		noteService.setId(objectId.toString());
		noteService.setApplication_name_en(application_name_en);
		noteService.setApplication_name_cn(application_name_cn);
		noteService.setRelease_node_name(release_node_name);
		noteService.setNote_id(note_id);
		noteService.setApplication_id(application_id);
		noteService.setDev_managers_info(dev_managers_info);
		noteService.setApplication_type(application_type);
		prodNoteDao.createService(noteService);
	}

	@Override
	public List<NoteService> queryNoteSrevice(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		return prodNoteDao.queryNoteService(note_id);
	}

	@Override
	public void deleteNoteSrevice(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		prodNoteDao.deleteNoteSrevice(id);
	}

	@Override
	public void updateNoteSrevice(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		String application_name_en = (String) requestParam.get(Dict.APPLICATION_NAME_EN);
		String application_name_cn = (String) requestParam.get(Dict.APPLICATION_NAME_CN);
		List<Map<String, Object>> dev_managers_info = (List<Map<String, Object>>) requestParam.get("dev_managers_info");
		String tag_Name = (String) requestParam.get(Dict.TAG_NAME);
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String application_type = (String) requestParam.get("application_type");
		Map<String, Object> expand_info = (Map<String, Object>) requestParam.get("expand_info");
		prodNoteDao.updateNoteService(id,application_name_en,application_name_cn,dev_managers_info,tag_Name,application_id,application_type,expand_info);
	}

	@Override
	public void addNoteConfiguration(Map<String, Object> requestParam) throws Exception {
		User user = CommonUtils.getSessionUser();
		String module_name = (String) requestParam.get("module_name");
		String module_ip = (String) requestParam.get("module_ip");
		String fileName = (String) requestParam.get(Dict.FILENAME_U);
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String file_url = (String) requestParam.get("file_url");
		List<Map<String, Object>> diff_content = (List<Map<String, Object>>) requestParam.get("diff_content");
		String file_type = (String) requestParam.get("file_type");
		String city = (String) requestParam.get("city");
		String safeguard_explain = (String) requestParam.get("safeguard_explain");
		String diff_flag = (String) requestParam.get("diff_flag");
		NoteConfiguration nConfiguration = prodNoteDao.queryNoteConfigurationInfo(note_id, fileName, module_name);
		NoteConfiguration noteConfiguration = new NoteConfiguration();
		if (!CommonUtils.isNullOrEmpty(nConfiguration)) {
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "文件名已存在,请更换" });
		}
		if ("app_nas".equals(module_name)) {
			noteConfiguration.setSafeguard_explain(safeguard_explain);
		}
		ObjectId objectId = new ObjectId();
		noteConfiguration.set_id(objectId);
		noteConfiguration.setCatalog_type("2");
		noteConfiguration.setId(objectId.toString());
		noteConfiguration.setNote_id(note_id);
		noteConfiguration.setDiff_content(diff_content); 
		noteConfiguration.setFile_principal(user.getUser_name_cn());
		noteConfiguration.setFile_type(file_type);
		noteConfiguration.setFile_url(file_url);
		noteConfiguration.setFileName(fileName);
		noteConfiguration.setModule_ip(module_ip);
		noteConfiguration.setModule_name(module_name);
		noteConfiguration.setPrincipal_phone(user.getTelephone());
		noteConfiguration.setRelease_node_name(release_node_name);
		noteConfiguration.setCity(city);
		noteConfiguration.setDiff_flag(diff_flag);
		prodNoteDao.createConfiguration(noteConfiguration);
	}

	@Override
	public List<Map<String, Object>> queryNoteConfiguration(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		List<NoteConfiguration> list = prodNoteDao.queryNoteConfiguration(note_id);
		List<Map<String, Object>> noteConfigurations = new ArrayList<>();
		Map<String, Object> commonconfigMap = new HashMap<>();
		Map<String, Object> cfgNasMap = new HashMap<>();
		Map<String, Object> cfgCoreMap = new HashMap<>();
		Map<String, Object> appNasMap = new HashMap<>();
		Map<String, Object> cfgBefMbankMap = new HashMap<>();
		List<NoteConfiguration> commonconfigList = new ArrayList<>();
		List<NoteConfiguration> cfgNasList = new ArrayList<>();
		List<NoteConfiguration> cfgCoreList = new ArrayList<>();
		List<NoteConfiguration> appNasList = new ArrayList<>();
		List<NoteConfiguration> cfgBefMbankList = new ArrayList<>();
		for (NoteConfiguration noteConfiguration : list) {
			if ("commonconfig".equals(noteConfiguration.getModule_name())) {
				commonconfigMap.put("module_name", noteConfiguration.getModule_name());
				commonconfigList.add(noteConfiguration);
				commonconfigMap.put("note_configurations", commonconfigList);
			} else if ("cfg_nas".equals(noteConfiguration.getModule_name())) {
				cfgNasMap.put("module_name", noteConfiguration.getModule_name());
				cfgNasList.add(noteConfiguration);
				cfgNasMap.put("note_configurations", cfgNasList);
			} else if ("cfg_core".equals(noteConfiguration.getModule_name())) {
				cfgCoreMap.put("module_name", noteConfiguration.getModule_name());
				cfgCoreList.add(noteConfiguration);
				cfgCoreMap.put("note_configurations", cfgCoreList);
			} else if ("app_nas".equals(noteConfiguration.getModule_name())){
				appNasMap.put("module_name", noteConfiguration.getModule_name());
				appNasList.add(noteConfiguration);
				appNasMap.put("note_configurations", appNasList);
			} else {
				cfgBefMbankMap.put("module_name", noteConfiguration.getModule_name());
				cfgBefMbankList.add(noteConfiguration);
				cfgBefMbankMap.put("note_configurations", cfgBefMbankList);
			}
				
		}
		if (!CommonUtils.isNullOrEmpty(commonconfigMap)) {
			commonconfigMap.put("module_name", "commonconfig");
			noteConfigurations.add(commonconfigMap);
		}
		if (!CommonUtils.isNullOrEmpty(cfgNasMap)) {
			cfgNasMap.put("module_name", "cfg_nas");
			noteConfigurations.add(cfgNasMap);
		}
		if (!CommonUtils.isNullOrEmpty(cfgCoreMap)) {
			cfgCoreMap.put("module_name", "cfg_core");
			noteConfigurations.add(cfgCoreMap);
		}
		if (!CommonUtils.isNullOrEmpty(appNasMap)) {
			appNasMap.put("module_name", "app_nas");
			noteConfigurations.add(appNasMap);
		}
		if (!CommonUtils.isNullOrEmpty(cfgBefMbankMap)) {
			cfgBefMbankMap.put("module_name", "cfgbef_mbank");
			noteConfigurations.add(cfgBefMbankMap);
		}
		return noteConfigurations;
	}

	@Override
	public void deleteNoteConfiguration(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		prodNoteDao.deleteNoteConfiguration(id);
	}

	@Override
	public void updateNoteConfiguration(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		String fileName = (String) requestParam.get(Dict.FILENAME_U);
		String file_url = (String) requestParam.get("file_url");
		String file_principal = (String) requestParam.get("file_principal");
		String principal_phone = (String) requestParam.get("principal_phone");
		List<Map<String, Object>> diff_content = (List<Map<String, Object>>) requestParam.get("diff_content");
		String file_type = (String) requestParam.get("file_type");
		String city = (String) requestParam.get("city");
		String safeguard_explain = (String) requestParam.get("safeguard_explain");
		String diff_flag = (String) requestParam.get("diff_flag");
		String module_ip = (String) requestParam.get("module_ip");
		prodNoteDao.updateNoteConfiguration(id,fileName,file_url,file_principal,principal_phone,diff_content,file_type,city,safeguard_explain,diff_flag,module_ip);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addNoteSql(Map<String, Object> requestParam) throws Exception {
		List<Map<String, Object>> sqlList = (List<Map<String, Object>>) requestParam.get(Dict.LIST);
		for (Map<String, Object> map : sqlList) {
			String release_node_name = (String) map.get("release_node_name");
			String file_principal = (String) map.get("file_principal");
			String principal_phone = (String) map.get("principal_phone");
			String note_id = (String) map.get(Dict.NOTE_ID);
			String file_url = (String) map.get("file_url");
			String fileName = file_url.substring(file_url.lastIndexOf("/")+1);
			String file_type = (String) map.get("file_type");
			String asset_catalog_name = (String) map.get("asset_catalog_name");
			String seq_no = (String) map.get("seq_no");
			String script_type = (String) map.get("script_type");// 脚本类型
			NoteSql noteSqlInfo = prodNoteDao.querySqlByName(note_id, asset_catalog_name, fileName, script_type);
			if (!CommonUtils.isNullOrEmpty(noteSqlInfo)) {
				throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"文件名已存在,请更换"});
			}
			NoteSql noteSql = new NoteSql();
			ObjectId objectId = new ObjectId();
			noteSql.set_id(objectId);
			noteSql.setCatalog_type("3");
			noteSql.setId(objectId.toString());
			noteSql.setNote_id(note_id);
			noteSql.setAsset_catalog_name(asset_catalog_name);
			noteSql.setFile_principal(file_principal);
			noteSql.setFile_type(file_type);
			noteSql.setFile_url(file_url);
			noteSql.setFileName(fileName);
			noteSql.setPrincipal_phone(principal_phone);
			noteSql.setRelease_node_name(release_node_name);
			if ("2".equals(script_type)) {
				noteSql.setSeq_no(seq_no);
			} else {
				noteSql.setSeq_no(seq_no);
			}
			noteSql.setScript_type(script_type);
			prodNoteDao.createSql(noteSql);
		}
	}

	@Override
	public List queryNoteSql(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		ProdNote prodNote = prodNoteDao.queryDetail(note_id);
        String template_id = prodNote.getTemplate_id();
        List<AssetCatalog> list = assetCatalogDao.queryByTemplateIdAndType(template_id, Collections.singletonList("3"));
        if (CommonUtils.isNullOrEmpty(list)) {
            list = new ArrayList();
        }
        List result = new ArrayList<>();
        for (AssetCatalog assetCatalog : list) {
            Map catalogResult = new HashMap<>();
            catalogResult.putAll(CommonUtils.beanToMap(assetCatalog));
            List<NoteSql> noteSqls = prodNoteDao.queryNoteSql(note_id, assetCatalog.getCatalog_name());
            List<NoteSql> implementSql = new ArrayList<>();
            List<NoteSql> goBackSql = new ArrayList<>();
            for (NoteSql noteSql : noteSqls) {
				if ("2".equals(noteSql.getScript_type())) {
					implementSql.add(noteSql);
				} else {
					goBackSql.add(noteSql);
				}
			}
            if (!CommonUtils.isNullOrEmpty(implementSql)) {
            	catalogResult.put("implementSql", implementSql);
			} else {
				catalogResult.put("implementSql", implementSql);
			}
            if (!CommonUtils.isNullOrEmpty(goBackSql)) {
            	catalogResult.put("goBackSql", goBackSql);
			} else {
				catalogResult.put("goBackSql", goBackSql);
			}
            result.add(catalogResult);
        }
        return result;
	}

	@Override
	public void deleteNoteSql(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		NoteSql noteSql = prodNoteDao.queryNoteSql(id);
        if (CommonUtils.isNullOrEmpty(noteSql)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"变更文件不存在"});
        }
        if (!CommonUtils.isNullOrEmpty(noteSql.getSeq_no()) && "2".equals(noteSql.getScript_type())) {
            String DelSeqNo = noteSql.getSeq_no();
            String asset_catalog_name = noteSql.getAsset_catalog_name();
            prodNoteDao.deleteNoteSql(id);
            List<NoteSql> seqNo = prodNoteDao.queryNoteSqlList(noteSql.getNote_id());
            for (NoteSql note : seqNo) {
                if (!CommonUtils.isNullOrEmpty(note.getSeq_no()) && asset_catalog_name.equals(note.getAsset_catalog_name())) {
                    if (Integer.parseInt(DelSeqNo) < Integer.parseInt(note.getSeq_no())) {
                    	note.setSeq_no((Integer.parseInt(note.getSeq_no()) - 1) + "");
                        prodNoteDao.updateNoteSeqNo(note.getId(),note.getSeq_no());
                    }
                }
            }
        } else {
        	prodNoteDao.deleteNoteSql(id);
        }
		
	}

	@Override
	public void updateNoteSql(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		prodNoteDao.updateNoteSql(id);
	}

	@Override
	public void lockNote(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		String lock_flag = (String) requestParam.get("lock_flag");
		//获取当前用户
		User user = CommonUtils.getSessionUser();
		//校验当前用户创建变更权限
		if(!roleService.isGroupReleaseManager(user.getGroup_id())) {
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		String userId = user.getId();
		String lock_name_cn = user.getUser_name_cn();
		ProdNote prodNote = prodNoteDao.queryDetail(note_id);
		if (userId.equals(prodNote.getLock_people()) || CommonUtils.isNullOrEmpty(prodNote.getLock_people())) {
			if ("0".equals(lock_flag)) {
				prodNoteDao.updateNoteLockFlag(note_id,lock_flag,userId,lock_name_cn);
			} else {
				prodNoteDao.updateNoteLockFlag(note_id,lock_flag,"","");
			}
		} else {
			throw new FdevException(ErrorConstants.LOCK_PEOPLE_NOT_SAME_PERSON);
		}
	}

	@Override
	public Map<String, Object> updateNoteSeqNo(Map<String, Object> requestParam) throws Exception {
		if (CommonUtils.isNullOrEmpty(requestParam)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"更新文件序号参数错误"});
        }
        List<String> ids = (List<String>) requestParam.get(Dict.IDS);
        if (ids.size() != 2) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"更新文件序号参数错误"});
        }

        List<String> tempList = new ArrayList<>();
        List<NoteSql> prodSqlList = new ArrayList<>();
        for (String id : ids) {
        	NoteSql noteSql = prodNoteDao.queryNoteSql(id);
        	prodSqlList.add(noteSql);
            String seq_no = noteSql.getSeq_no();
            tempList.add(seq_no);
        }
        Map hashMap = new HashMap();
        prodSqlList.get(0).setSeq_no(tempList.get(1));
        hashMap.put(prodSqlList.get(0).getId(), prodNoteDao.updateNoteSeqNo(prodSqlList.get(0).getId(),prodSqlList.get(0).getSeq_no()));
        prodSqlList.get(1).setSeq_no(tempList.get(0));
        hashMap.put(prodSqlList.get(1).getId(), prodNoteDao.updateNoteSeqNo(prodSqlList.get(1).getId(),prodSqlList.get(1).getSeq_no()));
		return hashMap;
	}
	
	/**
	 * 生成发布说明
	 * */
	@Override
	public void generateReleaseNotes(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);//发布说明id
		ProdNote prodNote = prodNoteDao.queryDetail(id);//查询发布说明详情
		if(Util.isNullOrEmpty(prodNote)) {
			throw new FdevException("查询发布说明详情异常");
		}
		List<NoteService> noteServiceList = new ArrayList<NoteService>();
		List<NoteSql> noteSqlList =  new ArrayList<NoteSql>();
		List<NoteConfiguration> noteConfigList = new ArrayList<NoteConfiguration>();
		NoteManual noteManual = new NoteManual();
		List<BatchTaskInfo> batchTaskInfos = new ArrayList<BatchTaskInfo>();
		
		if(!Util.isNullOrEmpty(prodNote) && !Util.isNullOrEmpty(prodNote.getNote_id())) {
			noteServiceList = noteServiceDao.queryNoteService(prodNote.getNote_id());//查询发布说明应用信息
			noteSqlList = noteSqlDao.queryNoteSql(prodNote.getNote_id());//查询数据库脚本信息
			noteConfigList =  noteConfigurationDao.queryNoteConfiguration(prodNote.getNote_id()); //查询发布说明配置文件信息
			noteManual = prodNoteDao.queryManualNote(prodNote.getNote_id());
			batchTaskInfos = batchTaskDao.queryBatchTaskInfoByNoteId(prodNote.getNote_id());
		}
		if ("1".equals(prodNote.getImage_deliver_type())) {
			//自动生成发布说明
			generateNote(prodNote,noteServiceList,noteSqlList,noteConfigList,id,batchTaskInfos);
		} else if ("2".equals(prodNote.getImage_deliver_type())) {
			//手动生成发布说明
			generateManualNote(prodNote,noteManual);
		}
	}
	
	/**
	 * 自动生成发布说明
	 * */
	public void generateNote(ProdNote prodNote,List<NoteService> noteServiceList,List<NoteSql> noteSqlList,List<NoteConfiguration> noteConfigList,String id,List<BatchTaskInfo> batchTaskInfos)  throws Exception {
		String path = "/fdev/frelease/";
		File file = new File(path);
		if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
            logger.info("发布说明目录创建成功,目录为:" + path);
        }
		path = path+prodNote.getRelease_note_name();
		file = new File(path);
		StringBuffer sb = new StringBuffer();
		sb.append("######所属系统：{{");
		sb.append(prodNote.getOwner_system_name());
		if("gray".equals(prodNote.getType())) {
			sb.append("_灰度");
		}else if ("proc".equals(prodNote.getType())) {
			sb.append("_生产");
		}
		sb.append("}}######");
		
		StringBuffer configInfo = new StringBuffer();//配置列表
		StringBuffer noteSqlInfo = new StringBuffer();//数据库列表
		StringBuffer serviceInfo = new StringBuffer();//镜像列表
		StringBuffer batchInfo = new StringBuffer();//批量列表
		if (!CommonUtils.isNullOrEmpty(noteConfigList)) {
			configInfo = generateConfigInfo(prodNote.getType(),noteConfigList,prodNote.getOwner_system_name());
		}
		if (!CommonUtils.isNullOrEmpty(noteSqlList)) {
			noteSqlInfo = generateSqlInfo(noteSqlList,id);
		}
		if (!CommonUtils.isNullOrEmpty(noteServiceList)) {
			serviceInfo = generateServiceInfo(noteServiceList,prodNote.getType(),prodNote.getLeaseholder());
		}
		if (!CommonUtils.isNullOrEmpty(batchTaskInfos)) {
			batchInfo = generateBatchInfo(batchTaskInfos,prodNote.getNote_id());
		}
		sb.append(configInfo).append(noteSqlInfo).append(serviceInfo).append(batchInfo);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(sb.toString().getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			fos.close();
		}
		//上传minio保存文件下载地址
		fileService.uploadWord(path, file, "fdev-release");
		prodNote.setNote_download_url(path);
		prodNoteDao.updateProdNoteInfo(prodNote);
	}
	
	
	/**
	 * 发布说明配置文件
	 * */
	public StringBuffer generateConfigInfo(String type, List<NoteConfiguration> noteConfigList, String systemName) {
		StringBuffer sb = new StringBuffer();
		sb.append("\r\n");
		sb.append("一、配置文件更新").append("\r\n");
		
		StringBuffer publicConfig = new StringBuffer();
//		StringBuffer privateConfig = new StringBuffer();
		StringBuffer cfgNas = new StringBuffer();
		StringBuffer cfgCore = new StringBuffer();
		StringBuffer appNas = new StringBuffer();
		
		StringBuffer publicAddConfig = new StringBuffer();//公共配置文件新增
		StringBuffer publicEditConfig = new StringBuffer();//公共配置文件修改
		
		StringBuffer cfgNasAddConfig = new StringBuffer();//cfgNas配置文件新增
		StringBuffer cfgNasEditConfig = new StringBuffer();//cfgNas配置文件修改
		
		StringBuffer cfgCoreAddConfig = new StringBuffer();//cfgCore配置文件新增
		StringBuffer cfgCoreEditConfig = new StringBuffer();//cfgCore配置文件修改
		
		StringBuffer appNasAddConfig = new StringBuffer();//appNas配置文件新增
		StringBuffer appNasEditConfig = new StringBuffer();//appNas配置文件修改
		
		if(!Util.isNullOrEmpty(noteConfigList)) {
			List<Map<String,Object>> diffList = new ArrayList<Map<String,Object>>();
			Boolean commonconfigFlag = false;
			Boolean cfgNasFlag = false;
			Boolean cfgCoreFlag = false;
			Boolean appNasFlag = false;
			NoteConfiguration commonconfig = new NoteConfiguration();
			NoteConfiguration cfg_nas = new NoteConfiguration();
			NoteConfiguration cfg_core = new NoteConfiguration();
			NoteConfiguration app_nas = new NoteConfiguration();
			for (Iterator iterator = noteConfigList.iterator(); iterator.hasNext();) {
				NoteConfiguration noteConfiguration = (NoteConfiguration) iterator.next();
				if("commonconfig".equals(noteConfiguration.getModule_name())) {
					commonconfigFlag = true;
					commonconfig = noteConfiguration;
					continue;
				}
				if("cfg_nas".equals(noteConfiguration.getModule_name())) {
					cfgNasFlag = true;
					cfg_nas = noteConfiguration;
					continue;
				}
				if("cfg_core".equals(noteConfiguration.getModule_name())) {
					cfgCoreFlag = true;
					cfg_core = noteConfiguration;
					continue;
				}
				if("app_nas".equals(noteConfiguration.getModule_name())) {
					appNasFlag = true;
					app_nas = noteConfiguration;
					continue;
				}
			}
			if (systemName.contains("个人手机") && !systemName.contains("村镇")) {
				if ("gray".equals(type)) {
					if (commonconfigFlag) {
						queryPublicConfigGray(commonconfig,publicConfig);
					}
					if (cfgNasFlag) {
						queryCfgNas(cfg_nas,cfgNas);
					}
					if (cfgCoreFlag) {
						cfgCore.append("\r\n").append("1.3【cfg_core】公共配置文件更新").append("\r\n");
						String city = cfg_core.getCity();
						String[] moduleIps = cfg_core.getModule_ip().split(",");
						List<String> moduleIpList = new ArrayList<>();
						for (String string : moduleIps) {
							moduleIpList.add(string);
						}
						if("1".equals(city)) {
							StringBuffer moduleIpSH = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("1").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
						}else if("2".equals(city)) {
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListHF("1").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						} else {
							StringBuffer moduleIpSH = new StringBuffer();
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("1").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							for (String string : moduleIpList) {
								if (queryIpListHF("1").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						}
					}
					if (appNasFlag) {
						queryAppNas(app_nas,appNas);
					}
				} else if ("proc".equals(type)) {
					if (commonconfigFlag) {
						queryPublicConfigProc(commonconfig,publicConfig);
					}
					if (cfgNasFlag) {
						queryCfgNas(cfg_nas,cfgNas);
					}
					if (cfgCoreFlag) {
						queryCfgCore(cfg_core,cfgCore);
					}
					if (appNasFlag) {
						queryAppNas(app_nas,appNas);
					}
				}
			} else if (systemName.contains("个人网银") && !systemName.contains("村镇")) {
				if ("gray".equals(type)) {
					if (commonconfigFlag) {
						queryPublicConfigGray(commonconfig,publicConfig);
					}
					if (cfgNasFlag) {
						String city = cfg_nas.getCity();
						cfgNas.append("\r\n").append("1.2【cfg_nas】公共配置文件更新").append("\r\n");
						if("1".equals(city)) {
							cfgNas.append("SH IP：xxx").append("\r\n");
						}else if("2".equals(city)) {
							cfgNas.append("HF IP：xxx").append("\r\n");
						} else {
							cfgNas.append("SH IP：xxx").append("\r\n");
							cfgNas.append("HF IP：xxx").append("\r\n");
						}
					}
					if (cfgCoreFlag) {
						cfgCore.append("\r\n").append("1.3【cfg_core】公共配置文件更新").append("\r\n");
						String city = cfg_core.getCity();
						String[] moduleIps = cfg_core.getModule_ip().split(",");
						List<String> moduleIpList = new ArrayList<>();
						for (String string : moduleIps) {
							moduleIpList.add(string);
						}
						if("1".equals(city)) {
							StringBuffer moduleIpSH = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("3").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
						}else if("2".equals(city)) {
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListHF("3").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						} else {
							StringBuffer moduleIpSH = new StringBuffer();
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("3").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							for (String string : moduleIpList) {
								if (queryIpListHF("3").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						}
					}
					if (appNasFlag) {
						appNas.append("\r\n").append("1.4【app_nas】公共配置文件更新").append("\r\n");
						String city = commonconfig.getCity();
						if("1".equals(city)) {
							appNas.append("SH IP：xxx").append("\r\n");
						}else if("2".equals(city)) {
							appNas.append("HF IP：xxx").append("\r\n");
						} else {
							appNas.append("SH IP：xxx").append("\r\n");
							appNas.append("HF IP：xxx").append("\r\n");
						}
					}
				} else if ("proc".equals(type)) {
					if (commonconfigFlag) {
						queryPublicConfigProc(commonconfig,publicConfig);
					}
					if (cfgNasFlag) {
						queryCfgNas(cfg_nas,cfgNas);
					}
					if (cfgCoreFlag) {
						queryCfgCore(cfg_core,cfgCore);
					}
					if (appNasFlag) {
						queryAppNas(app_nas,appNas);
					}
				}
			} else if (systemName.contains("柜面及消息")) {
				if ("proc".equals(type)) {
					if (commonconfigFlag) {
						publicConfig.append("\r\n").append("1.1【commonconfig】公共配置文件更新").append("\r\n");
						String city = commonconfig.getCity();
						if("1".equals(city)) {
							publicConfig.append("SH IP：K1:(xxx-250)K2:(xxx-16)").append("\r\n");
						}else if("2".equals(city)) {
							publicConfig.append("HF IP：K1:(xxx-250)K2:(xxx-16)").append("\r\n");
						} else {
							publicConfig.append("SH IP：K1:(xxx-250)K2:(xxx-16)").append("\r\n");
							publicConfig.append("HF IP：K1:(xxx-250)K2:(xxx-16)").append("\r\n");
						}
					}
					if (cfgNasFlag) {
						queryCfgNas(cfg_nas,cfgNas);
					}
					if (cfgCoreFlag) {
						cfgCore.append("\r\n").append("1.3【cfg_core】公共配置文件更新").append("\r\n");
						String city = cfg_core.getCity();
						String[] moduleIps = cfg_core.getModule_ip().split(",");
						List<String> moduleIpList = new ArrayList<>();
						for (String string : moduleIps) {
							moduleIpList.add(string);
						}
						if("1".equals(city)) {
							StringBuffer moduleIpSH = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("5").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
						}else if("2".equals(city)) {
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListHF("5").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						} else {
							StringBuffer moduleIpSH = new StringBuffer();
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("5").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							for (String string : moduleIpList) {
								if (queryIpListHF("5").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						}
					}
					if (appNasFlag) {
						queryAppNas(app_nas,appNas);
					}
				}
			} else if (systemName.contains("批量")) {
				if ("proc".equals(type)) {
					if (commonconfigFlag) {
						publicConfig.append("\r\n").append("1.1【commonconfig】公共配置文件更新").append("\r\n");
						String city = commonconfig.getCity();
						if("1".equals(city)) {
							publicConfig.append("SH IP：K1:(xxx-56)").append("\r\n");
						}else if("2".equals(city)) {
							publicConfig.append("HF IP：K1:(xxx-175)").append("\r\n");
						} else {
							publicConfig.append("SH IP：K1:(xxx-56)").append("\r\n");
							publicConfig.append("HF IP：K1:(xxx-175)").append("\r\n");
						}
					}
					if (cfgNasFlag) {
						queryCfgNas(cfg_nas,cfgNas);
					}
					if (cfgCoreFlag) {
						cfgCore.append("\r\n").append("1.3【cfg_core】公共配置文件更新").append("\r\n");
						String city = cfg_core.getCity();
						String[] moduleIps = cfg_core.getModule_ip().split(",");
						List<String> moduleIpList = new ArrayList<>();
						for (String string : moduleIps) {
							moduleIpList.add(string);
						}
						if("1".equals(city)) {
							StringBuffer moduleIpSH = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("4").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
						}else if("2".equals(city)) {
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListHF("4").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						} else {
							StringBuffer moduleIpSH = new StringBuffer();
							StringBuffer moduleIpHF = new StringBuffer();
							for (String string : moduleIpList) {
								if (queryIpListSH("4").contains(string)) {
									moduleIpSH.append(string).append(",");
								}
							}
							for (String string : moduleIpList) {
								if (queryIpListHF("4").contains(string)) {
									moduleIpHF.append(string).append(",");
								}
							}
							String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
							String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
							cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
							cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
						}
					}
					if (appNasFlag) {
						queryAppNas(app_nas,appNas);
					}
				}
			}
			
			for (Iterator iterator = noteConfigList.iterator(); iterator.hasNext();) {
				NoteConfiguration noteConfiguration = (NoteConfiguration) iterator.next();
				diffList = noteConfiguration.getDiff_content();//获取对比列表
					if("commonconfig".equals(noteConfiguration.getModule_name())) {
							if("1".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								publicAddConfig.append("配置文件{{新增}} ：").append("\r\n");
								publicAddConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									publicAddConfig.append("diff结果：").append("\r\n");
								}
								
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH新增前{{").append("\\").append("}}").append("\r\n");
											shConfig.append("SH新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF新增前{{").append("\\").append("}}").append("\r\n");
											hfConfig.append("HF新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									publicAddConfig.append(shConfig).append(hfConfig);
								}
							}else if("2".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								publicEditConfig.append("配置文件{修改}} ：").append("\r\n");
								publicEditConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									publicEditConfig.append("diff结果：").append("\r\n");
								}
							
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											shConfig.append("SH修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											hfConfig.append("HF修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									publicEditConfig.append(shConfig).append(hfConfig);
								}
							}
					}
					//cfgNas
					if("cfg_nas".equals(noteConfiguration.getModule_name())) {
							if("1".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								cfgNasAddConfig.append("配置文件{{新增}} ：").append("\r\n");
								cfgNasAddConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									cfgNasAddConfig.append("diff结果：").append("\r\n");
								}
								
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH新增前{{").append("\\").append("}}").append("\r\n");
											shConfig.append("SH新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF新增前{{").append("\\").append("}}").append("\r\n");
											hfConfig.append("HF新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									cfgNasAddConfig.append(shConfig).append(hfConfig);
								}
							}else if("2".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								cfgNasEditConfig.append("配置文件{修改}} ：").append("\r\n");
								cfgNasEditConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									cfgNasEditConfig.append("diff结果：").append("\r\n");
								}
							
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											shConfig.append("SH修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											hfConfig.append("HF修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									cfgNasEditConfig.append(shConfig).append(hfConfig);
								}
							}
					}
					//cfgCore
					if("cfg_core".equals(noteConfiguration.getModule_name())) {
							if("1".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								cfgCoreAddConfig.append("配置文件{{新增}} ：").append("\r\n");
								cfgCoreAddConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									cfgCoreAddConfig.append("diff结果：").append("\r\n");
								}
								
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH新增前{{").append("\\").append("}}").append("\r\n");
											shConfig.append("SH新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF新增前{{").append("\\").append("}}").append("\r\n");
											hfConfig.append("HF新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									cfgCoreAddConfig.append(shConfig).append(hfConfig);
								}
							}else if("2".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								cfgCoreEditConfig.append("配置文件{修改}} ：").append("\r\n");
								cfgCoreEditConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									cfgCoreEditConfig.append("diff结果：").append("\r\n");
								}
							
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											shConfig.append("SH修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											hfConfig.append("HF修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									cfgCoreEditConfig.append(shConfig).append(hfConfig);
								}
							}
					}
					//appNas
					if("app_nas".equals(noteConfiguration.getModule_name())) {
							if("1".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								appNasAddConfig.append("配置文件{{新增}} ：").append("\r\n");
								appNasAddConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								appNasAddConfig.append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getSafeguard_explain()).append("】").append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									appNasAddConfig.append("diff结果：").append("\r\n");
								}
								
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH新增前{{").append("\\").append("}}").append("\r\n");
											shConfig.append("SH新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF新增前{{").append("\\").append("}}").append("\r\n");
											hfConfig.append("HF新增后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									appNasAddConfig.append(shConfig).append(hfConfig);
								}
							}else if("2".equals(noteConfiguration.getFile_type())) {//file_type文件类型 1-新增 2-修改
								appNasEditConfig.append("配置文件{修改}} ：").append("\r\n");
								appNasEditConfig.append(noteConfiguration.getFile_url()).append("/").append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								appNasAddConfig.append(noteConfiguration.getFileName()).append("【").append(noteConfiguration.getSafeguard_explain()).append("】").append("【").append(noteConfiguration.getFile_principal()).append("/").append(noteConfiguration.getPrincipal_phone()).append("】").append("\r\n");
								if ("0".equals(noteConfiguration.getDiff_flag())) {
									appNasEditConfig.append("diff结果：").append("\r\n");
								}
							
								if(!Util.isNullOrEmpty(diffList)) {
									StringBuffer shConfig = new StringBuffer();//上海
									StringBuffer hfConfig = new StringBuffer();//合肥
									for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
										Map<String, Object> map = (Map<String, Object>) iterator2.next();
										String city = (String) map.get("diffCity");
										if("SH".equals(city)) {
											shConfig.append("SH修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											shConfig.append("SH修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}else if("HF".equals(city)) {
											hfConfig.append("HF修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
											hfConfig.append("HF修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
										}
									}
									appNasEditConfig.append(shConfig).append(hfConfig);
								}
							}
					}
			}
			publicConfig.append(publicAddConfig).append(publicEditConfig);
			cfgNas.append(cfgNasAddConfig).append(cfgNasEditConfig);
			cfgCore.append(cfgCoreAddConfig).append(cfgCoreEditConfig);
			appNas.append(appNasAddConfig).append(appNasEditConfig);
		}
		sb.append(publicConfig).append(cfgNas).append(cfgCore).append(appNas);
		return sb;
	}
	
	/**
	 * 发布说明数据库脚本
	 * */
	public StringBuffer generateSqlInfo(List<NoteSql> noteSqlList,String id) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("\r\n");
		sb.append("二、数据库脚本执行").append("\r\n");
		sb.append("\r\n");
		
		List<NoteSql> dbbefMbank = new ArrayList<NoteSql>();
		List<NoteSql> dbmidMbank = new ArrayList<NoteSql>();
		List<NoteSql> dbaftMbank = new ArrayList<NoteSql>();
		List<NoteSql> dbbefNbper = new ArrayList<NoteSql>();
		List<NoteSql> dbmidNbper = new ArrayList<NoteSql>();
		List<NoteSql> dbaftNbper = new ArrayList<NoteSql>();

		List<NoteSql> dbbefOracle = new ArrayList<NoteSql>();
		List<NoteSql> dbmidOracle = new ArrayList<NoteSql>();
		List<NoteSql> dbaftOracle = new ArrayList<NoteSql>();
		
		StringBuffer dbbefMbankSb = new StringBuffer();
		StringBuffer dbmidMbankSb = new StringBuffer();
		StringBuffer dbaftMbankSb = new StringBuffer();
		StringBuffer dbbefNbperSb = new StringBuffer();
		StringBuffer dbmidNbperSb = new StringBuffer();
		StringBuffer dbaftNbperSb = new StringBuffer();
		StringBuffer dbbefOracleSb = new StringBuffer();
		StringBuffer dbmidOracleSb = new StringBuffer();
		StringBuffer dbaftOracleSb = new StringBuffer();
		
		if(!Util.isNullOrEmpty(noteSqlList)) {
			for (Iterator iterator = noteSqlList.iterator(); iterator.hasNext();) {
				NoteSql noteSql = (NoteSql) iterator.next();
				if("dbbef_mbank".equals(noteSql.getAsset_catalog_name())) {
					dbbefMbank.add(noteSql);
				}
				if("dbmid_mbank".equals(noteSql.getAsset_catalog_name())) {
					dbmidMbank.add(noteSql);
				}
				if("dbaft_mbank".equals(noteSql.getAsset_catalog_name())) {
					dbaftMbank.add(noteSql);
				}
				if("dbbef_nbper".equals(noteSql.getAsset_catalog_name())) {
					dbbefNbper.add(noteSql);
				}
				if("dbmid_nbper".equals(noteSql.getAsset_catalog_name())) {
					dbmidNbper.add(noteSql);
				}
				if("dbaft_nbper".equals(noteSql.getAsset_catalog_name())) {
					dbaftNbper.add(noteSql);
				}
				if("dbbef_oracle".equals(noteSql.getAsset_catalog_name())) {
					dbbefOracle.add(noteSql);
				}
				if("dbmid_oracle".equals(noteSql.getAsset_catalog_name())) {
					dbmidOracle.add(noteSql);
				}
				if("dbaft_oracle".equals(noteSql.getAsset_catalog_name())) {
					dbaftOracle.add(noteSql);
				}
			}
			
			//dbbef_mbank
			if(!Util.isNullOrEmpty(dbbefMbank)) {
				dbbefMbank = sortList(dbbefMbank);
				dbbefMbankSb.append("【dbbef_mbank】个人手机银行数据库脚本_停止前").append("\r\n");
				dbbefMbankSb.append("用户:").append("ebank").append("\r\n");
				dbbefMbankSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbbefMbank.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbbefMbankSb.append(execSql).append("\r\n");
				dbbefMbankSb.append("应急回退：").append("\r\n");
				dbbefMbankSb.append(backSql).append("\r\n");
			}
			//dbmid_mbank
			if(!Util.isNullOrEmpty(dbmidMbank)) {
				dbmidMbank = sortList(dbmidMbank);
				dbmidMbankSb.append("【dbmid_mbank】个人手机银行数据库脚本_停止中").append("\r\n");
				dbmidMbankSb.append("用户:").append("ebank").append("\r\n");
				dbmidMbankSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbmidMbank.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbmidMbankSb.append(execSql).append("\r\n");
				dbmidMbankSb.append("应急回退：").append("\r\n");
				dbmidMbankSb.append(backSql).append("\r\n");
			}
			//dbaft_mbank
			if(!Util.isNullOrEmpty(dbaftMbank)) {
				dbaftMbank = sortList(dbaftMbank);
				dbaftMbankSb.append("【dbaft_mbank】个人网银数据库脚本_停止后").append("\r\n");
				dbaftMbankSb.append("用户:").append("ebank").append("\r\n");
				dbaftMbankSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbaftMbank.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbaftMbankSb.append(execSql).append("\r\n");
				dbaftMbankSb.append("应急回退：").append("\r\n");
				dbaftMbankSb.append(backSql).append("\r\n");
			}
			//dbbef_nbper
			if(!Util.isNullOrEmpty(dbbefNbper)) {
				dbbefNbper = sortList(dbbefNbper);
				dbbefNbperSb.append("【dbbef_nbper】个人网银数据库脚本_停止前").append("\r\n");
				dbbefNbperSb.append("用户:").append("ebank").append("\r\n");
				dbbefNbperSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbbefNbper.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbbefNbperSb.append(execSql).append("\r\n");
				dbbefNbperSb.append("应急回退：").append("\r\n");
				dbbefNbperSb.append(backSql).append("\r\n");
			}
			//dbmid_nbper
			if(!Util.isNullOrEmpty(dbmidNbper)) {
				dbmidNbper = sortList(dbmidNbper);
				dbmidNbperSb.append("【dbmid_nbper】个人网银数据库脚本_停止中").append("\r\n");
				dbmidNbperSb.append("用户:").append("ebank").append("\r\n");
				dbmidNbperSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbmidNbper.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbmidNbperSb.append(execSql).append("\r\n");
				dbmidNbperSb.append("应急回退：").append("\r\n");
				dbmidNbperSb.append(backSql).append("\r\n");
			}
			//dbaft_nbper
			if(!Util.isNullOrEmpty(dbaftNbper)) {
				dbaftNbper = sortList(dbaftNbper);
				dbaftNbperSb.append("【dbaft_nbper】个人网银数据库脚本_停止后").append("\r\n");
				dbaftNbperSb.append("用户:").append("ebank").append("\r\n");
				dbaftNbperSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbaftNbper.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbaftNbperSb.append(execSql).append("\r\n");
				dbaftNbperSb.append("应急回退：").append("\r\n");
				dbaftNbperSb.append(backSql).append("\r\n");
			}
			//dbbef_oracle
			if(!Util.isNullOrEmpty(dbbefOracle)) {
				dbbefOracle = sortList(dbbefOracle);
				dbbefOracleSb.append("【dbbef_oracle】Oracle数据库脚本_停止前").append("\r\n");
				dbbefOracleSb.append("用户:").append("ebank").append("\r\n");
				dbbefOracleSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbbefOracle.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbbefOracleSb.append(execSql).append("\r\n");
				dbbefOracleSb.append("应急回退：").append("\r\n");
				dbbefOracleSb.append(backSql).append("\r\n");
			}
			//dbmid_oracle 
			if(!Util.isNullOrEmpty(dbmidOracle)) {
				dbmidOracle = sortList(dbmidOracle);
				dbmidOracleSb.append("【dbmid_oracle】Oracle数据库脚本_停止中").append("\r\n");
				dbmidOracleSb.append("用户:").append("ebank").append("\r\n");
				dbmidOracleSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbmidOracle.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbmidOracleSb.append(execSql).append("\r\n");
				dbmidOracleSb.append("应急回退：").append("\r\n");
				dbmidOracleSb.append(backSql).append("\r\n");
			}
			//dbaft_oracle 
			if(!Util.isNullOrEmpty(dbaftOracle)) {
				dbaftOracle = sortList(dbaftOracle);
				dbaftOracleSb.append("【dbbef_mbank】Oracle数据库脚本_停止后").append("\r\n");
				dbaftOracleSb.append("用户:").append("ebank").append("\r\n");
				dbaftOracleSb.append("IP:").append("xxx").append("\r\n");
				StringBuffer execSql = new StringBuffer();
				StringBuffer backSql = new StringBuffer();
				for (Iterator iterator = dbaftOracle.iterator(); iterator.hasNext();) {
					NoteSql noteSql = (NoteSql) iterator.next();
					if("2".equals(noteSql.getScript_type())) {
						execSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}else if("3".equals(noteSql.getScript_type())) {
						backSql.append("        {{sql/").append(noteSql.getFileName()).append("}}").append("        【").append(noteSql.getFile_principal()).append("/").append(noteSql.getPrincipal_phone()).append("】").append("\r\n");
					}
				}
				dbaftOracleSb.append(execSql).append("\r\n");
				dbaftOracleSb.append("应急回退：").append("\r\n");
				dbaftOracleSb.append(backSql).append("\r\n");
			}
		}
		sb.append(dbbefMbankSb).append(dbmidMbankSb).append(dbaftMbankSb);
		sb.append(dbbefNbperSb).append(dbmidNbperSb).append(dbaftNbperSb);
		sb.append(dbbefOracleSb).append(dbmidOracleSb).append(dbaftOracleSb);
		
		//判断是否有cfgbef_mbank
		List<NoteConfiguration> confList = noteConfigurationDao.queryByModule(id, "cfgbef_mbank");
		if(!Util.isNullOrEmpty(confList)) {
			sb.append("【cfgbef_mbank】上传文件至个人手机银行数据库服务器").append("\r\n");
			sb.append("用户:").append("ebank").append("\r\n");
			sb.append("IP:").append("xxx").append("\r\n");
			
			StringBuffer cfgbefMbankAddConfig = new StringBuffer();//cfgbefMbank配置文件新增
			StringBuffer cfgbefMbankEditConfig = new StringBuffer();//cfgbefMbank配置文件修改
			
			List<Map<String,Object>> diffList = new ArrayList<>();
			for (Iterator iterator = confList.iterator(); iterator.hasNext();) {
				NoteConfiguration noteConfiguration2 = (NoteConfiguration) iterator.next();
				diffList = noteConfiguration2.getDiff_content();//获取对比列表
				if("1".equals(noteConfiguration2.getFile_type())) {//file_type文件类型 1-新增 2-修改
					cfgbefMbankAddConfig.append("配置文件{{新增}} ：").append("\r\n");
					cfgbefMbankAddConfig.append(noteConfiguration2.getFile_url()).append("/").append(noteConfiguration2.getFileName()).append("【").append(noteConfiguration2.getFile_principal()).append("/").append(noteConfiguration2.getPrincipal_phone()).append("】").append("\r\n");
					if ("0".equals(noteConfiguration2.getDiff_flag())) {
						cfgbefMbankAddConfig.append("diff结果：").append("\r\n");
					}
					if(!Util.isNullOrEmpty(diffList)){
						StringBuffer diffConfig = new StringBuffer();//配置
						for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
							Map<String, Object> map = (Map<String, Object>) iterator2.next();
							diffConfig.append("修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
							diffConfig.append("修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
						}
						cfgbefMbankAddConfig.append(diffConfig);
					}
				}else if ("2".equals(noteConfiguration2.getFile_type())) {
					cfgbefMbankEditConfig.append("配置文件{{修改}} ：").append("\r\n");
					cfgbefMbankEditConfig.append(noteConfiguration2.getFile_url()).append("/").append(noteConfiguration2.getFileName()).append("【").append(noteConfiguration2.getFile_principal()).append("/").append(noteConfiguration2.getPrincipal_phone()).append("】").append("\r\n");
					if ("0".equals(noteConfiguration2.getDiff_flag())) {
						cfgbefMbankEditConfig.append("diff结果：").append("\r\n");
					}
					if(!Util.isNullOrEmpty(diffList)){
						StringBuffer diffConfig = new StringBuffer();//配置
						for (Iterator iterator2 = diffList.iterator(); iterator2.hasNext();) {
							Map<String, Object> map = (Map<String, Object>) iterator2.next();
							diffConfig.append("修改前{{").append(map.get("before_content")).append("}}").append("\r\n");
							diffConfig.append("修改后{{").append(map.get("after_content")).append("}}").append("\r\n");
						}
						cfgbefMbankEditConfig.append(diffConfig);
					}
				}
			}
			sb.append(cfgbefMbankAddConfig).append(cfgbefMbankEditConfig);
		}
		return sb;
	}
	
	
	
	/**
	 * 发布说明镜像列表
	 * */
	public StringBuffer generateServiceInfo(List<NoteService> noteServiceList,String prodType,String leaseholder) {
		StringBuffer sb = new StringBuffer();
		String userNameSH = null;
		String userNameHF = null;
		switch (leaseholder) {
		case "appgray-tenant":
			userNameSH = "appgray-user";
			userNameHF = "appgray-user";
			break;
		case "appper-tenant":
			userNameSH = "appper-user";
			userNameHF = "appper-user";
			break;
		case "appcom-tenant":
			userNameSH = "appcom-user";
			userNameHF = "appcom-user";
			break;
		case "ebankgraysh-tenant/ebankgrayhf-tenant":
			userNameSH = "ebankgraysh";
			userNameHF = "ebankgrayhf";
			break;
		case "ebanksh-tenant/ebankhf-tenant":
			userNameSH = "ebanksh";
			userNameHF = "ebankhf";
			break;
		case "ebankbat-tenant":
			userNameSH = "ebankbat-user";
			userNameHF = "ebankbat-user";
			break;
		case "sh-ebank-tenant/hf-ebank-tenant":
			userNameSH = "sh-ebank-user";
			userNameHF = "hf-ebank-user";
			break;
		case "sh-pubebank-tenant/hf-pubebank-tenant":
			userNameSH = "sh-pubebank-user";
			userNameHF = "hf-pubebank-user";
			break;
		}
		sb.append("\r\n");
		sb.append("三、镜像更新").append("\r\n");
		sb.append("    ").append("K1集群：").append("\r\n");
		sb.append("    ").append("CaaS地址:https://xxx/index.html#/app-list/all(上海)").append("\r\n");
		sb.append("    ").append("CaaS地址:https://xxx/index.html#/app-list/all(合肥)").append("\r\n");
		if("gray".equals(prodType)) {
			sb.append("    ").append("上海CaaS应用用户(灰度)：").append(userNameSH).append("\r\n");
			sb.append("    ").append("合肥CaaS应用用户(灰度)：").append(userNameHF).append("\r\n");
		}else if("proc".equals(prodType)) {
			sb.append("    ").append("上海CaaS应用用户(生产)：").append(userNameSH).append("\r\n");
			sb.append("    ").append("合肥CaaS应用用户(生产)：").append(userNameHF).append("\r\n");
		}
		
		sb.append("\r\n");
		sb.append("    ").append("K2集群：").append("\r\n");
		sb.append("    ").append("CaaS地址:https://xxx/index.html#/app-list/all(上海)").append("\r\n");
		sb.append("    ").append("CaaS地址:https://xxx/index.html#/app-list/all(合肥)").append("\r\n");
		if("gray".equals(prodType)) {
			sb.append("    ").append("上海CaaS应用用户(灰度)：").append(userNameSH).append("\r\n");
			sb.append("    ").append("合肥CaaS应用用户(灰度)：").append(userNameHF).append("\r\n");
		}else if("proc".equals(prodType)) {
			sb.append("    ").append("上海CaaS应用用户(生产)：").append(userNameSH).append("\r\n");
			sb.append("    ").append("合肥CaaS应用用户(生产)：").append(userNameHF).append("\r\n");
		}
		sb.append("\r\n");
		
		if(!Util.isNullOrEmpty(noteServiceList)) {
			sb.append("\r\n").append("\r\n");
			Boolean dockerFlag = false;
			Boolean dockerRestartFlag = false;
			Boolean dockerScaleFlag = false;
			Boolean dockerYamlFlag = false;
			Boolean stopAllFlag = false;
			for (Iterator iterator = noteServiceList.iterator(); iterator.hasNext();) {
				NoteService noteService = (NoteService) iterator.next();
				if("docker".equals(noteService.getApplication_type())) {
					dockerFlag = true;
					continue;
				}
				if("docker_restart".equals(noteService.getApplication_type())) {
					dockerRestartFlag = true;
					continue;
				}
				if("docker_scale".equals(noteService.getApplication_type())) {
					dockerScaleFlag = true;
					continue;
				}
				if("docker_yaml".equals(noteService.getApplication_type())) {
					dockerYamlFlag = true;
					continue;
				}
				if("stop_all".equals(noteService.getApplication_type())) {
					stopAllFlag = true;
					continue;
				}
			}
			StringBuffer docker = new StringBuffer();
			StringBuffer dockerRestart = new StringBuffer();
			StringBuffer dockerScale = new StringBuffer();
			StringBuffer dockerYaml = new StringBuffer();
			StringBuffer stopAll = new StringBuffer();
			if (dockerFlag) {
				docker.append("\r\n").append("3.1【docker】滚动发布微服务列表").append("\r\n");
			}
			if (dockerRestartFlag) {
				dockerRestart.append("\r\n").append("3.2【docker_restart】应用组重启").append("\r\n");
			}
			if (dockerScaleFlag) {
				dockerScale.append("\r\n").append("3.3【docker_scale】弹性扩展").append("\r\n");
			}
			if (dockerYamlFlag) {
				dockerYaml.append("\r\n").append("3.4【docker_yaml】新增微服务").append("\r\n");
			}
			if (stopAllFlag) {
				stopAll.append("\r\n").append("3.5【stop_all】应用停止").append("\r\n");
			}
			
			for (Iterator iterator = noteServiceList.iterator(); iterator.hasNext();) {
				NoteService noteService = (NoteService) iterator.next();
				if("docker".equals(noteService.getApplication_type())) {
					docker.append("    ").append(noteService.getApplication_name_en()).append("    ").append(noteService.getTag_name()).append("    ").append("【").append(noteService.getDev_managers_info().get(0).get("user_name_cn")).append("/").append(noteService.getDev_managers_info().get(0).get("telephone")).append("】").append("\r\n");
				}else if("docker_restart".equals(noteService.getApplication_type())) {
					dockerRestart.append("    ").append(noteService.getApplication_name_en()).append("    ").append(noteService.getTag_name()).append("    ").append("【").append(noteService.getDev_managers_info().get(0).get("user_name_cn")).append("/").append(noteService.getDev_managers_info().get(0).get("telephone")).append("】").append("\r\n");
				}else if("docker_scale".equals(noteService.getApplication_type())) {
					Map<String, Object> expandInfoMap = noteService.getExpand_info();
					StringBuffer expandInfo = new StringBuffer();
					expandInfo.append("SHK1 扩展至").append(expandInfoMap.get("SHK1")).append("  ").append("SHK2 扩展至").append(expandInfoMap.get("SHK2")).append("  ").append("HFK1 扩展至").append(expandInfoMap.get("HFK1")).append("  ").append("HFK2 扩展至").append(expandInfoMap.get("HFK2"));
					dockerScale.append("    ").append(noteService.getApplication_name_en()).append("    ").append(expandInfo).append("    ").append("【").append(noteService.getDev_managers_info().get(0).get("user_name_cn")).append("/").append(noteService.getDev_managers_info().get(0).get("telephone")).append("】").append("\r\n");
				}else if("docker_yaml".equals(noteService.getApplication_type())) {
					dockerYaml.append("    ").append(noteService.getApplication_name_en()).append("    ").append(noteService.getTag_name()).append("    ").append("【").append(noteService.getDev_managers_info().get(0).get("user_name_cn")).append("/").append(noteService.getDev_managers_info().get(0).get("telephone")).append("】").append("\r\n");
				}else if ("stop_all".equals(noteService.getApplication_type())) {
					stopAll.append("    ").append(noteService.getApplication_name_en()).append("    ").append(noteService.getTag_name()).append("    ").append("【").append(noteService.getDev_managers_info().get(0).get("user_name_cn")).append("/").append(noteService.getDev_managers_info().get(0).get("telephone")).append("】").append("\r\n");
				}
			}
			sb.append(docker).append(dockerRestart).append(dockerScale).append(dockerYaml).append(stopAll);
		}
		return sb;
	}
	
	public List<NoteSql> sortList(List<NoteSql> list){
		Collections.sort(list, new Comparator<NoteSql>() {
			@Override
			public int compare(NoteSql o1, NoteSql o2) {
				Integer id1 = Integer.parseInt((String) o1.getSeq_no());
				Integer id2 = Integer.parseInt((String) o2.getSeq_no());
				if (id1<id2) {
				     return -1;
				    } else {
				     return 1;
				    }
			}
		});
		return list;
	}
	
	public StringBuffer generateBatchInfo(List<BatchTaskInfo> batchTaskInfos,String note_id) throws Exception {
		StringBuffer sb = new StringBuffer();
		List<String> applicationIds = new ArrayList<>();
		for (BatchTaskInfo batchTaskInfo : batchTaskInfos) {
			applicationIds.add(batchTaskInfo.getApplication_id());
		}
		applicationIds = ((List<String>) applicationIds.stream().distinct().collect(Collectors.toList()));
		sb.append("\r\n");
		sb.append("四、任务调度中心批量任务").append("\r\n");
		sb.append("\r\n");
		sb.append("4.1 进入任务调度中心 xxx/spdb-job-mgmt/index").append("\r\n");
		sb.append("    ").append("账号：统一认证账号").append("\r\n");
		sb.append("    ").append("密码：统一认证账号密码").append("\r\n");
		for (int i = 0; i < applicationIds.size(); i++) {
			sb.append("\r\n");
			Map<String, Object> requestParam = new HashMap<>();
			requestParam.put(Dict.APPLICATION_ID, applicationIds.get(i));
			requestParam.put(Dict.NOTE_ID, note_id);
			List<BatchTaskInfo> batchTaskInfoList = batchTaskDao.queryBatchTaskInfoListByAppId(requestParam);
			sb.append("4.").append(i+2).append(" ").append(batchTaskInfoList.get(0).getApplication_name_en()).append("批量任务").append("\r\n");
			for (BatchTaskInfo batchTaskInfo : batchTaskInfoList) {
				String type = batchTaskInfo.getType();
				String typeName = null;
				switch (type) {
				case "httpAddJob":
					typeName = "新增任务";
					break;
				case "httpTriggerJob":
					typeName = "新增一次性任务";
					break;
				case "httpDeleteJob":
					typeName = "删除任务";
					break;
				case "httpUpdateJob":
					typeName = "更新任务信息";
					break;
				}
				sb.append(typeName).append(" ").append(batchTaskInfo.getBatchInfo()).append("【").append(batchTaskInfo.getUser_name_cn()).append("/").append(batchTaskInfo.getUser_name_phone()).append("】").append("\r\n");
			}
		}
		
		return sb;
	}

	@Override
	public void createManualNote(Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String release_date = (String) requestParam.get(Dict.DATE);
		String owner_groupId = (String) requestParam.get(Dict.OWNER_GROUPID);
		String image_deliver_type = (String) requestParam.get(Dict.IMAGE_DELIVER_TYPE);//是否自动化发布
		String release_note_name = (String) requestParam.get("release_note_name");//发布说明名称
		String type = (String) requestParam.get(Dict.TYPE);
		String namespace = (String) requestParam.get(Dict.NAMESPACE);
		String note_batch = (String) requestParam.get("note_batch");
		String owner_system = (String) requestParam.get(Dict.OWNER_SYSTEM);
		String systemType = (String) requestParam.get("systemType");
		String system_name = "";
		if (CommonUtils.isNullOrEmpty(systemType)) {
			if(!CommonUtils.isNullOrEmpty(owner_system)){
				system_name = taskService.querySystemName(owner_system);
			}
		} else {
			system_name = "网银系统群_批量";
		}
		//校验当前用户创建变更权限
		if(!roleService.isGroupReleaseManager(owner_groupId)) {
			throw new FdevException(ErrorConstants.ROLE_ERROR);
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
		ProdNote checkRepect = prodNoteDao.queryProdRecordByNoteBatch(release_node_name, note_batch, release_date, owner_system, type);
		if (!CommonUtils.isNullOrEmpty(checkRepect)) {// version不能重复
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "note_batch is repeat" });
		}
		//查询窗口所属小组
		Map<String, Object> groupDetail = userService.queryGroupDetail(owner_groupId);
		if (CommonUtils.isNullOrEmpty(groupDetail)) {
			logger.error("can't find the group info !@@@@@group_id={}", owner_groupId);
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "组信息不存在" });
		}
		ProdNote prodNote = new ProdNote();
		//获取当前用户
		User user = CommonUtils.getSessionUser();
		ObjectId objectId = new ObjectId();
		prodNote.set_id(objectId);
		prodNote.setNote_id(objectId.toString());
		prodNote.setRelease_node_name(release_node_name);
		prodNote.setDate(release_date);
		prodNote.setType(type);
		prodNote.setLauncher(user.getId());
		prodNote.setLauncher_name_cn(user.getUser_name_cn());
		prodNote.setCreate_time(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
		prodNote.setOwner_groupId(owner_groupId);
		prodNote.setImage_deliver_type(image_deliver_type);
		prodNote.setRelease_note_name(release_note_name);
		prodNote.setLock_flag("1");
		prodNote.setNamespace(namespace);
		prodNote.setNote_batch(note_batch);
		if (CommonUtils.isNullOrEmpty(systemType)) {
			prodNote.setOwner_system(owner_system);
		} else {
			prodNote.setOwner_system("");
		}
		prodNote.setOwner_system_name(system_name);
		prodNoteDao.create(prodNote);
	}

	@Override
	public void addManualNoteInfo(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		String note_info = (String) requestParam.get("note_info");
		NoteManual noteManual = new NoteManual();
		ObjectId objectId = new ObjectId();
		noteManual.set_id(objectId);
		noteManual.setId(objectId.toString());
		noteManual.setNote_id(note_id);
		noteManual.setNote_info(note_info);
		prodNoteDao.createManualNote(noteManual);
		
	}

	@Override
	public NoteManual queryManualNoteInfo(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		return prodNoteDao.queryManualNote(note_id);
	}

	@Override
	public void updateManualNoteInfo(Map<String, Object> requestParam) throws Exception {
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		String note_info = (String) requestParam.get("note_info");
		prodNoteDao.updateManualNote(note_id,note_info);
		
	}
	
	public void generateManualNote(ProdNote prodNote, NoteManual noteManual)  throws Exception {
		String path = "/fdev/frelease/";
		File file = new File(path);
		if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
            logger.info("发布说明目录创建成功,目录为:" + path);
        }
		path = path+prodNote.getRelease_note_name();
		file = new File(path);
		StringBuffer sb = new StringBuffer();
		String[] noteInfo = noteManual.getNote_info().split("\\n");
		for (int i = 0; i < noteInfo.length; i++) {
			if (i != (noteInfo.length - 1)) {
				sb.append(noteInfo[i]).append("\r\n");
			} else {
				sb.append(noteInfo[i]);
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(sb.toString().getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			fos.close();
		}
		//上传minio保存文件下载地址
		fileService.uploadWord(path, file, "fdev-release");
		prodNote.setNote_download_url(path);
		prodNoteDao.updateProdNoteInfo(prodNote);
	}
	@Override
	public void updateProdNoteReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
		prodNoteDao.updateProdNoteReleaseNodeName(old_release_node_name, release_node_name);
	}

	@Override
	public void updateNoteServiceReleaseNodeName(String old_release_node_name, String release_node_name)
			throws Exception {
		prodNoteDao.updateNoteServiceReleaseNodeName(old_release_node_name, release_node_name);
	}

	@Override
	public void updateNoteConfigurationReleaseNodeName(String old_release_node_name, String release_node_name)
			throws Exception {
		prodNoteDao.updateNoteConfigurationReleaseNodeName(old_release_node_name, release_node_name);
	}

	@Override
	public void updateNoteSqlReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
		prodNoteDao.updateNoteSqlReleaseNodeName(old_release_node_name, release_node_name);
	}

	@Override
	public void updateNoteBatchReleaseNodeName(String old_release_node_name, String release_node_name)
			throws Exception {
		prodNoteDao.updateNoteBatchReleaseNodeName(old_release_node_name, release_node_name);
	}
	
	public void queryCfgNas(NoteConfiguration cfg_nas, StringBuffer cfgNas) {
		cfgNas.append("\r\n").append("1.2【cfg_nas】公共配置文件更新").append("\r\n");
		String city = cfg_nas.getCity();
		if("1".equals(city)) {
			cfgNas.append("SH IP：xxx").append("\r\n");
		}else if("2".equals(city)) {
			cfgNas.append("HF IP：xxx").append("\r\n");
		} else {
			cfgNas.append("SH IP：xxx").append("\r\n");
			cfgNas.append("HF IP：xxx").append("\r\n");
		}
	}
	
	public void queryCfgCore(NoteConfiguration cfg_core, StringBuffer cfgCore) {
		cfgCore.append("\r\n").append("1.3【cfg_core】公共配置文件更新").append("\r\n");
		String city = cfg_core.getCity();
		String[] moduleIps = cfg_core.getModule_ip().split(",");
		List<String> moduleIpList = new ArrayList<>();
		for (String string : moduleIps) {
			moduleIpList.add(string);
		}
		if("1".equals(city)) {
			StringBuffer moduleIpSH = new StringBuffer();
			for (String string : moduleIpList) {
				if (queryIpListSH("2").contains(string)) {
					moduleIpSH.append(string).append(",");
				}
			}
			String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
			cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
		}else if("2".equals(city)) {
			StringBuffer moduleIpHF = new StringBuffer();
			for (String string : moduleIpList) {
				if (queryIpListHF("2").contains(string)) {
					moduleIpHF.append(string).append(",");
				}
			}
			String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
			cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
		} else {
			StringBuffer moduleIpSH = new StringBuffer();
			StringBuffer moduleIpHF = new StringBuffer();
			for (String string : moduleIpList) {
				if (queryIpListSH("2").contains(string)) {
					moduleIpSH.append(string).append(",");
				}
			}
			for (String string : moduleIpList) {
				if (queryIpListHF("2").contains(string)) {
					moduleIpHF.append(string).append(",");
				}
			}
			String ipSH = moduleIpSH.substring(0, moduleIpSH.length()-1);
			String ipHF = moduleIpHF.substring(0, moduleIpHF.length()-1);
			cfgCore.append("SH IP：").append("(").append(ipSH).append(")").append("\r\n");
			cfgCore.append("HF IP：").append("(").append(ipHF).append(")").append("\r\n");
		}
	}
	
	public void queryAppNas(NoteConfiguration app_nas, StringBuffer appNas) {
		appNas.append("\r\n").append("1.4【app_nas】公共配置文件更新").append("\r\n");
		String city = app_nas.getCity();
		if("1".equals(city)) {
			appNas.append("SH IP：xxx").append("\r\n");
		}else if("2".equals(city)) {
			appNas.append("HF IP：xxx").append("\r\n");
		} else {
			appNas.append("SH IP：xxx").append("\r\n");
			appNas.append("HF IP：xxx").append("\r\n");
		}
	}
	
	public void queryPublicConfigGray(NoteConfiguration commonconfig, StringBuffer publicConfig) {
		publicConfig.append("\r\n").append("1.1【commonconfig】公共配置文件更新").append("\r\n");
		String city = commonconfig.getCity();
		if("1".equals(city)) {
			publicConfig.append("SH IP：K1:(xxx)").append("\r\n");
		}else if("2".equals(city)) {
			publicConfig.append("HF IP：K1:(xxx)").append("\r\n");
		} else {
			publicConfig.append("SH IP：K1:(xxx)").append("\r\n");
			publicConfig.append("HF IP：K1:(xxx)").append("\r\n");
		}
	}
	
	public void queryPublicConfigProc(NoteConfiguration commonconfig, StringBuffer publicConfig) {
		publicConfig.append("\r\n").append("1.1【commonconfig】公共配置文件更新").append("\r\n");
		String city = commonconfig.getCity();
		if("1".equals(city)) {
			publicConfig.append("SH IP：K1:(xxx-32)K2:(xxx-134)").append("\r\n");
		}else if("2".equals(city)) {
			publicConfig.append("HF IP：K1:(xxx-32)(xxx)K2:(xxx-134)").append("\r\n");
		} else {
			publicConfig.append("SH IP：K1:(xxx-32)K2:(xxx-134)").append("\r\n");
			publicConfig.append("HF IP：K1:(xxx-32)(xxx)K2:(xxx-134)").append("\r\n");
		}
	}
	
	public List<String> queryIpListSH(String type) {
		List<String> ipList = new ArrayList<>();
		switch (type) {
		case "1":
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "2":
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "3":
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "4":
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "5":
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		}
		return ipList;
		
	}
	
	public List<String> queryIpListHF(String type) {
		List<String> ipList = new ArrayList<>();
		switch (type) {
		case "1":
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "2":
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "3":
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "4":
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		case "5":
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			ipList.add("xxx");
			break;
		}
		return ipList;
	}
}
