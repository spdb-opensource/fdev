package com.spdb.fdev.spdb.service.impl;


import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.base.utils.SFTPClient;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.FdevUserCacheUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.spdb.dao.*;
import com.spdb.fdev.spdb.entity.*;
import com.spdb.fdev.spdb.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author huyz
 * @description  IEntityService
 * @date 2021/5/7
 */
@Service
@RefreshScope
@SuppressWarnings("all")
public class EntityServiceImpl implements IEntityService {

	@Value("${gitlab.token}")
	private String token;
	@Value("${gitlab.application.file}")
	private String applicationFile;
	@Value("${gitlab.application.file.ci}")
	private String applicationFileCi;
	@Autowired
	private IEntityDao entityDao;
	@Autowired
    public UserVerifyUtil userVerifyUtil;
	@Autowired
    private IRestService restService;
	@Autowired
    IEntityTemplateService entityTemplateService;
	@Autowired
	IEnvDao envDao;
	@Autowired
    IEntityTemplateDao entityTemplateDao;
	@Autowired
	private OutsideTemplateDao outsideTemplateDao;
    @Value("${fdev.config.host.ip}")
    private String fdevConfigHostIp;
    @Value("${fdev.config.dir}")
    private String fdevConfigDir;
    @Value("${fdev.config.user}")
    private String fdevConfigUser;
    @Value("${fdev.config.password}")
    private String fdevConfigPassword;
    @Value("${fdev.application.properties.dir}")
    private String propertiesDir;
	@Autowired
	private IGitlabApiService gitlabApiService;
	@Autowired
	private IServiceConfigDao serviceConfigDao;
	@Autowired
	private IConfigFileService configFileService;
	@Autowired
	private FdevUserCacheUtil fdevUserCacheUtil;

	@Override
	public Map queryEntityModel(Map<String, Object> requestParam) throws Exception {
		//根据条件查询实体列表
		Map map = entityDao.queryEntityModel(requestParam);
		List<Entity> entityModelList = (List<Entity>) map.get(Dict.ENTITYMODELLIST);
		//实体列表不为空
		if(!CommonUtil.isNullOrEmpty(entityModelList)) {

			for (Entity entity : entityModelList) {
				//组装用户信息
				entity.setCreateUserName(entity.getCreateUser().getNameCn());
				entity.setUpdateUserName(entity.getUpdateUser().getNameCn());
				entity.setCreateUserId(entity.getCreateUser().getUserId());
				entity.setUpdateUserId(entity.getUpdateUser().getUserId());
				String templateId = entity.getTemplateId();
				//查询模板名称 字段列表
				if(!CommonUtil.isNullOrEmpty(templateId)) {
					EntityTemplate entityTemplate = entityTemplateDao.queryById(templateId);
					entity.setTemplateName(entityTemplate.getNameCn());
					entity.setProperties(entityTemplate.getProperties());
				} else {
					//为空赋值自定义模板
					entity.setTemplateName(Constants.EMPTYTEMPLATE);
				}
			}
		}
		map.put(Dict.ENTITYMODELLIST, entityModelList);
		return map;
	}

	@Override
	public Entity queryEntityModelDetail(String id) throws Exception {
		//根据ID查询实体详情
		Entity entity = entityDao.queryEntityModelById(id);
		if(!CommonUtil.isNullOrEmpty(entity)) {
			String templateId = entity.getTemplateId();
			//实体模块ID不为空 获取实体模块字段
			if(!CommonUtil.isNullOrEmpty(templateId)) {
				Map map = new HashMap();
				map.put(Dict.ID,templateId);
				List<Map<String, Object>> properties = entityTemplateService.queryById(map).getProperties();
				entity.setProperties(properties);
				String templateName = entityTemplateService.queryById(map).getNameCn();
				entity.setTemplateName(templateName);
			}
			//映射为空赋值空对象
			entity.setPropertiesValue(CommonUtil.isNullOrEmpty(entity.getPropertiesValue()) ? new HashMap() : entity.getPropertiesValue());;
		}

		return entity;
	}

	@Override
	public Map<String, Boolean> checkEntityModel(Map<String, Object> requestParam) throws Exception {
		Map map = new HashMap();
		Map<String, Object> nameEnMap = new HashMap();
		nameEnMap.put(Dict.NAMEEN, requestParam.get(Dict.NAMEEN));//实体英文名
		//校验实体英文名是否重复 重复返回true 不重复返回false nameEn为空返回false
		map.put(Dict.NAMEENFLAG, CommonUtil.isNullOrEmpty(requestParam.get(Dict.NAMEEN)) ? false :
								!CommonUtil.isNullOrEmpty(entityDao.queryOneEntityModel(nameEnMap)));
		Map<String, Object> nameCnMap = new HashMap();
		nameCnMap.put(Dict.NAMECN, requestParam.get(Dict.NAMECN));//实体中文名
		//校验实体中文名是否重复 重复返回true 不重复返回false nameCn为空返回false
		map.put(Dict.NAMECNFLAG, CommonUtil.isNullOrEmpty(requestParam.get(Dict.NAMECN)) ? false :
								!CommonUtil.isNullOrEmpty(entityDao.queryOneEntityModel(nameCnMap)));
		return map;
	}


	@Override
	public Map<String,String> addEntityModel(Map<String, Object> requestParam) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		if(!checkEntityModel(requestParam).get(Dict.NAMEENFLAG) && !checkEntityModel(requestParam).get(Dict.NAMECNFLAG)) {
			//组装对象
			Entity entity = getEntity(requestParam);
			entityDao.addEntityModel(entity);
			//记录日志
			addEntityLog(entity,new ArrayList<>(),"新增");

			//保存操作日志
			Map<String, Object> editHistoryDetailsMap = new  HashMap<String, Object>();
			String templateId = entity.getTemplateId();
			//查询字段列表
			if(!CommonUtil.isNullOrEmpty(templateId)) {
				EntityTemplate entityTemplate = entityTemplateDao.queryById(templateId);
				entity.setProperties(entityTemplate.getProperties());
			}
            editHistoryDetailsMap.put(Dict.ENTITYID, entity.getId());//实体ID
            editHistoryDetailsMap.put(Dict.ENTITYNAMEEN, entity.getNameEn());//实体英文名
            editHistoryDetailsMap.put(Dict.ENTITYNAMECN, entity.getNameCn());//实体中文名
            editHistoryDetailsMap.put(Dict.OPERATETYPE, "1");//0-编辑，1-新增，2-删除
            editHistoryDetailsMap.put(Dict.FIELDS, entity.getProperties());//字段列表
            editHistoryDetailsMap.put(Dict.BEFORE, new HashMap());//新增 赋空
            editHistoryDetailsMap.put(Dict.AFTER, (Map) requestParam.get(Dict.PROPERTIESVALUE));//修改后字段
            editHistoryDetailsMap.put(Dict.ENVNAME, (String) requestParam.get(Dict.ENVNAME));//环境名
            editHistoryDetailsMap.put(Dict.DESC, (String) requestParam.get(Dict.DESC));//描述
			editHistoryDetails(editHistoryDetailsMap);
			map.put( Dict.ID , entity.getId());
		}
		return map;
	}

	@Override
	public Map<String, String> updateEntityModel(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);//实体ID
		String nameEn = (String) requestParam.get(Dict.NAMEEN);//实体英文名
		String nameCn = (String) requestParam.get(Dict.NAMECN);//实体中文名
		List<Map<String,Object>> propertiesList = (List<Map<String,Object>>) requestParam.get(Dict.PROPERTIESLIST);//字段列表
		//获取原实体
		Entity oldEntity = entityDao.queryEntityModelById(id);
		String oldNameEnDependency = oldEntity.getNameEn();
		boolean dependency = queryDependency(oldNameEnDependency,null);//true实体未被使用 false实体有被使用
		List<String> contentList = new ArrayList<String>();

		//判断实体英文名是否修改
		if(!CommonUtil.isNullOrEmpty(nameEn)) {
			if(!nameEn.equals(oldEntity.getNameEn())){
				if(dependency){
					contentList.add("将实体英文名由\"" + oldEntity.getNameEn() + "\"改为\"" + nameEn + "\".");
					oldEntity.setNameEn(nameEn);
				}else{
					throw new FdevException(ErrorConstants.ENT_UPDATE_ERROR,new String[]{"修改实体英文名"} );
				}
			}
		}
		//判断实体中文名是否修改
		if(!CommonUtil.isNullOrEmpty(nameCn)) {
			if(!nameCn.equals(oldEntity.getNameCn())){
				contentList.add("将实体中文名由\"" + oldEntity.getNameCn() + "\"改为\"" + nameCn + "\".");
				oldEntity.setNameCn(nameCn);
			}
		}

		//字段列表
		if(!CommonUtil.isNullOrEmpty(propertiesList) && CommonUtil.isNullOrEmpty(oldEntity.getTemplateId()) ) {
			List<Map<String, Object>> newPropertiesList = new ArrayList<>();
			Map<String, Object> oldPropertiesValue = oldEntity.getPropertiesValue();
			List<String> fields = new ArrayList<String>();
			for( Map<String,Object> properties : propertiesList ){
				String oldNameEn = (String) properties.get(Dict.OLDNAMEEN);//原字段英文名
				String oldNameCn = (String) properties.get(Dict.OLDNAMECN);//原字段中文名
				Boolean oldRequired = null ;
				if(!CommonUtil.isNullOrEmpty(properties.get(Dict.OLDREQUIRED))){
					oldRequired = (Boolean) properties.get(Dict.OLDREQUIRED);//原是否必输 true=是
				}
				String newNameEn = (String) properties.get(Dict.NEWNAMEEN);//新字段英文名
				String newNameCn = (String) properties.get(Dict.NEWNAMECN);//新字段中文名
				Boolean newRequired = null ;
				if(!CommonUtil.isNullOrEmpty(properties.get(Dict.NEWREQUIRED))){
					newRequired = (Boolean) properties.get(Dict.NEWREQUIRED);//新是否必输 true=是
				}
				if( !CommonUtil.isNullOrEmpty(newNameEn) && !CommonUtil.isNullOrEmpty(oldNameEn) && !newNameEn.equals(oldNameEn) ){
					//字段英文名被修改
					fields.add(oldNameEn);
					contentList.add("将属性\"" +oldNameEn + "\"英文名由\""+ oldNameEn + "\"改为\"" + newNameEn + "\".");//记录日志
					//修改propertiesValue
					updatePropertiesValue(oldPropertiesValue,oldNameEn,newNameEn);
				} else if(!CommonUtil.isNullOrEmpty(newNameEn) && CommonUtil.isNullOrEmpty(oldNameEn) ){
					contentList.add("添加属性\"" +newNameEn + "\".");//新增字段 记录日志
				} else if( CommonUtil.isNullOrEmpty(newNameEn) && !CommonUtil.isNullOrEmpty(oldNameEn) ) {
					contentList.add("删除属性\"" + oldNameEn + "\".");//删除字段 记录日志
					//清除propertiesValue 中该字段
					updatePropertiesValue(oldPropertiesValue,oldNameEn,newNameEn);
				}
				//字段中文名是否被修改 //记录日志
				if( !CommonUtil.isNullOrEmpty(newNameCn) && !CommonUtil.isNullOrEmpty(oldNameCn) && !newNameCn.equals(oldNameCn) )
					contentList.add("将属性\"" +oldNameEn + "\"中文名由\""+ oldNameCn + "\"改为\"" + newNameCn + "\".");

				//字段是否必输是否被修改 //记录日志
				if( !CommonUtil.isNullOrEmpty(newRequired) && !CommonUtil.isNullOrEmpty(oldRequired) && !newRequired.equals(oldRequired) )
					contentList.add("将属性\"" +oldNameEn + "\"由\"" + ( oldRequired ? "必输": "非必输") + "\"改为\"" + ( newRequired ? "必输": "非必输") + "\".");

				if( !CommonUtil.isNullOrEmpty(newNameCn) && !CommonUtil.isNullOrEmpty(newRequired) && !CommonUtil.isNullOrEmpty(newNameEn) ){
					Map<String, Object> newPropertiesMap = new HashMap<>();
					newPropertiesMap.put(Dict.TYPE,"string");//字段类型默认string
					newPropertiesMap.put(Dict.NAMEEN,newNameEn);
					newPropertiesMap.put(Dict.NAMECN,newNameCn);
					newPropertiesMap.put(Dict.REQUIRED,newRequired);
					newPropertiesList.add(newPropertiesMap);
				}
			}
			//判断被修改字段是否 存在依赖
			if( !CommonUtil.isNullOrEmpty(fields) && !queryDependency(oldNameEnDependency,fields)) {
				throw new FdevException(ErrorConstants.ENT_UPDATE_ERROR,new String[]{"修改实体字段"} );
			}
			oldEntity.setProperties(newPropertiesList);
			oldEntity.setPropertiesValue(oldPropertiesValue);
		}
		//更新当前操作人员信息
		oldEntity.setUpdateUser(getEntityUser());
		oldEntity.setUpdateTime(TimeUtils.formatToday());
		//编辑实体入库
		entityDao.updateEntityModel(oldEntity);
		if(!CommonUtil.isNullOrEmpty(contentList)) {
			//记录日志
			addEntityLog(oldEntity,contentList,"编辑");
		}

		Map<String,String> map = new HashMap<String,String>();
		map.put( Dict.ID , oldEntity.getId() );
		return map;
	}

	//添加实体操作日志
	public void addEntityLog(Entity entity,List<String> contentList,String updateType ) throws Exception {
		User user = userVerifyUtil.getRedisUser();//查询用户信息
		EntityLog entityLog = new EntityLog();
		entityLog.setEntityId(entity.getId());
		entityLog.setUpdateUserId(user.getId());
		entityLog.setUpdateUserName(user.getUser_name_cn());
		entityLog.setUpdateTime(TimeUtils.formatToday());
		entityLog.setUpdateType(updateType);
		entityLog.setContent(contentList);
		entityDao.addEntityLog(entityLog);
	}

	//修改字段 删除字段
	private void updatePropertiesValue(Map<String, Object> oldPropertiesValue, String oldNameEn, String newNameEn) {
		if(!CommonUtil.isNullOrEmpty(oldPropertiesValue)){
			//获取所有环境key
			Set<String> envSet = oldPropertiesValue.keySet();
			for(String env : envSet){
				Map envMap = (Map)oldPropertiesValue.get(env);
				if(!CommonUtil.isNullOrEmpty(envMap)){
					//获取所有环境名称key
					Set<String> envNameSet = envMap.keySet();
					for(String envName : envNameSet){
						//获取环境字段对应的映射值
						Map envNameMap = (Map)envMap.get(envName);
						if(!CommonUtil.isNullOrEmpty(newNameEn)){
							if(envNameMap.keySet().contains(oldNameEn)) {
								if(!CommonUtil.isNullOrEmpty(envNameMap.get(oldNameEn))) {
									envNameMap.put(newNameEn, envNameMap.get(oldNameEn));
								}
							}
						}
						envNameMap.remove(oldNameEn);
					}
				}
			}
		}
	}


	public boolean queryDependency(String entityNameEn,List<String> fields) throws Exception {
		Map<String, Object> requestParam = new HashMap<>();
		requestParam.put(Dict.ENTITYNAMEEN,entityNameEn);
		requestParam.put(Dict.FIELDS,fields);
		//判断是否有配置依赖
		Long count = serviceConfigDao.queryServiceConfigCount(requestParam);
		//判断是否有部署依赖
		Map<String, Object> deployDependency = configFileService.queryDeployDependency(requestParam);
		if(count > 0 || (Integer) deployDependency.get(Dict.COUNT) > 0){
			return false;
		}
		return true;
	}

	@Override
	public Map<String, Object> getVariablesValue(List<String> envNames, List<String> variablesKeys) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 当variablesKeys为空时返回空结果
        if (CommonUtil.isNullOrEmpty(variablesKeys)){
        	return returnMap;
        }
        Map<String, Object> normal = new HashMap<String, Object>();
        Map<String, Object> error = new HashMap<String, Object>();
        for (String envName : envNames) {
        	List<String> errorList = new ArrayList<String>();
        	Map<String, Object> map = new HashMap<String, Object>();
        	//获取环境信息
    		Map<String, Object>  envMap = new HashMap();
            envMap.put(Dict.NAMEEN,envName);
      	    Env	env = envDao.queryEnv(envMap);
        	if(!CommonUtil.isNullOrEmpty(env)) {
            	//获取环境type
                String envType = env.getType();
                Map<String, Object> queryEntityMap = new HashMap<String, Object>();
                for (String variablesKey : variablesKeys) {
                	// 实体名  字段名
                	String[] variablesKeyList= variablesKey.split("\\.");
                	queryEntityMap.put(Dict.NAMEEN, variablesKeyList[0]);
                	//字段值
                	Object value = "";
                    //获取实体
            		Entity entity = entityDao.queryOneEntityModel(queryEntityMap);
            		//字段必输是否必输 默认必输
    				Boolean required = true ;
            		if(!CommonUtil.isNullOrEmpty(entity) && variablesKeyList.length >= 2 ) {
            			//字段名
                    	String key = variablesKeyList[1];
            			List<Map<String, Object>> properties = entity.getProperties();
                		String templateId = entity.getTemplateId();
        				//查询模板名称 字段列表
        				if(!CommonUtil.isNullOrEmpty(templateId)) {
        					EntityTemplate entityTemplate = entityTemplateDao.queryById(templateId);
        					properties = entityTemplate.getProperties();
        				}
        				for (Map<String, Object> propertiesMap : properties) {
    						if(key.equals(propertiesMap.get(Dict.NAMEEN))) {
    							required = CommonUtil.isNullOrEmpty(propertiesMap.get(Dict.REQUIRED)) ? false : (Boolean)propertiesMap.get(Dict.REQUIRED);
    						}
    					}
            			//获取当前环境type字段值
            			Map propertiesValue = (Map) entity.getPropertiesValue();
            			if(!CommonUtil.isNullOrEmpty(propertiesValue)) {
            				Map envTypeMap = (Map)propertiesValue.get(envType);
                    		if(!CommonUtil.isNullOrEmpty(envTypeMap)) {
                    			//获取当前环境字段值
                    			Map envValue = (Map) envTypeMap.get(envName);
                    			if(!CommonUtil.isNullOrEmpty(envValue)) {
                    				value = envValue.get(key);
                    			}
                    		}
            			}
            		}
            		if(required && CommonUtil.isNullOrEmpty(value)) errorList.add(variablesKey);
            		else map.put(variablesKey,value);
                }

        	}else errorList = variablesKeys ;
        	normal.put(envName, map);
        	if(!CommonUtil.isNullOrEmpty(errorList)) {
           	 error.put(envName, errorList);
           }
        }
        returnMap.put(Dict.NORMAL, normal);
        returnMap.put(Dict.ERROR, error);
		return returnMap;
	}

	//封装Entity实体
	public Entity getEntity(Map<String, Object> requestParam) throws Exception {
        String nameEn = (String) requestParam.get(Dict.NAMEEN);//实体英文名
        String nameCn = (String) requestParam.get(Dict.NAMECN);//实体中文名
        String templateId = (String) requestParam.get(Dict.TEMPLATEID);//实体模板
        String envType = (String) requestParam.get(Dict.ENVTYPE);//环境
        String envName = (String) requestParam.get(Dict.ENVNAME);//环境名称
        Map propertiesValue = (Map) requestParam.get(Dict.PROPERTIESVALUE);//实体字段值
		ObjectId objectId = new ObjectId();
        String id = objectId.toString();
		Entity entity = new Entity();
		entity.set_id(objectId);
        entity.setId(id);
		entity.setNameEn(nameEn);//实体英文名
		entity.setNameCn(nameCn);//实体中文名
		entity.setCreateUser(getEntityUser());//创建人
		entity.setCreateTime(TimeUtils.formatToday());//创建时间
		entity.setUpdateUser(getEntityUser());//最新更新人
		entity.setUpdateTime(TimeUtils.formatToday());//更新时间
		if(!CommonUtil.isNullOrEmpty(templateId)) {
			entity.setTemplateId(templateId);//实体模板
		}else {
			List<Map<String, Object>> properties = (List<Map<String, Object>>) requestParam.get(Dict.PROPERTIES);
			if(!CommonUtil.isNullOrEmpty(properties)) {
				//校验实体属性中文名是否重复
				checkProperties(properties);
				entity.setProperties(properties);
			}
		}
		if(!CommonUtil.isNullOrEmpty(envName)&&
			!CommonUtil.isNullOrEmpty(envType)&&
			!CommonUtil.isNullOrEmpty(propertiesValue)) {
			//组装实体映射 实体字段值
			Map envNameMap = new HashMap();
			envNameMap.put(envName, propertiesValue);
			Map envTypeMap = new HashMap();
			envTypeMap.put(envType, envNameMap);
			entity.setPropertiesValue(envTypeMap);
			//非空字段校验
			checkPropertiesValue(entity,propertiesValue);
		}
		return entity;
	}

	//校验实体属性中文名是否重复
	private void checkProperties(List<Map<String, Object>> properties) throws Exception {
		List<String> nameEnList = new ArrayList<String>();
		List<String> nameCnList = new ArrayList<String>();
		for (Map<String, Object> map : properties) {
			String nameEn = (String) map.get(Dict.NAMEEN);
			String nameCn = (String) map.get(Dict.NAMECN);
			if(nameEnList.contains(nameEn)) {
				throw new FdevException(ErrorConstants.PROPERTIES_NAMEEN_ERROR, new String[]{nameEn});
			}else {
				nameEnList.add(nameEn);
			}
			if(nameCnList.contains(nameCn)) {
				throw new FdevException(ErrorConstants.PROPERTIES_NAMECN_ERROR, new String[]{nameCn});
			}else {
				nameCnList.add(nameCn);
			}
		}
	}

	public void checkPropertiesValue(Entity entity , Map propertiesValue) throws Exception {
		List<Map<String, Object>> propertiesList = new ArrayList<Map<String, Object>>();
		if(!CommonUtil.isNullOrEmpty(entity.getTemplateId())){
			EntityTemplate entityTemplate = entityTemplateDao.queryById(entity.getTemplateId());
			propertiesList = entityTemplate.getProperties();
		}else if(!CommonUtil.isNullOrEmpty(entity.getProperties())) {
			propertiesList = entity.getProperties();
		}
		for( Map<String, Object> properties : propertiesList ) {
			if(Constants.STRING.equals(properties.get(Dict.TYPE))) {//普通数据类型
				if(((Boolean) properties.get(Dict.REQUIRED)).booleanValue()) {
					if(CommonUtil.isNullOrEmpty(propertiesValue.get(properties.get(Dict.NAMEEN)))) {
						throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{(String) properties.get(Dict.NAMECN)});
					}
				}
			}
		}
	}



	@Override
	public String addEntityClass(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);//ID
        String envType = (String) requestParam.get(Dict.ENVTYPE);//环境
        String envName = (String) requestParam.get(Dict.ENVNAME);//环境名称
        Map propertiesValue = (Map) requestParam.get(Dict.PROPERTIESVALUE);//实体字段值
		List<Map<String, String>> envs = (List)requestParam.get("envs");//复制实体所需，多种环境
		//获取原实体
		Entity entity = entityDao.queryEntityModelById(id);
		//非空字段校验
      	checkPropertiesValue(entity,propertiesValue);
		Map oldPropertiesValue = entity.getPropertiesValue();
		if(CommonUtil.isNullOrEmpty(envs) && !CommonUtil.isNullOrEmpty(envType) && !CommonUtil.isNullOrEmpty(envName)){
			envs = new ArrayList<>();
			Map envMap = new HashMap();
			envMap.put(Dict.ENVTYPE,envType);
			envMap.put(Dict.ENVNAME,envName);
			envs.add(envMap);
		}else if((CommonUtil.isNullOrEmpty(envType) || CommonUtil.isNullOrEmpty(envName)) && CommonUtil.isNullOrEmpty(envs)){
			throw new FdevException(ErrorConstants.PARAM_ERROR);
		}

		for(Map envMap:envs){
			envType = (String) envMap.get(Dict.ENVTYPE);
			envName = (String) envMap.get(Dict.ENVNAME);
			if(!CommonUtil.isNullOrEmpty(oldPropertiesValue)) {
				Map propertiesEnv = (Map) oldPropertiesValue.get(envType);
				if(!CommonUtil.isNullOrEmpty(propertiesEnv)) {
					propertiesEnv.put(envName, propertiesValue);
				} else {
					propertiesEnv = new HashMap();
					propertiesEnv.put(envName, propertiesValue);
				}
				oldPropertiesValue.put(envType, propertiesEnv);
				entity.setPropertiesValue(oldPropertiesValue);
			} else {
				//组装实体映射 实体字段值
				Map envNameMap = new HashMap();
				envNameMap.put(envName, propertiesValue);
				Map envTypeMap = new HashMap();
				envTypeMap.put(envType, envNameMap);
				entity.setPropertiesValue(envTypeMap);
			}
			entity.setUpdateUser(getEntityUser());//最新更新人
			entity.setUpdateTime(TimeUtils.formatToday());//更新时间
			entityDao.updateEntityModel(entity);
			//保存操作日志
			Map<String, Object> editHistoryDetailsMap = new  HashMap<String, Object>();
			String templateId = entity.getTemplateId();
			//查询字段列表
			if(!CommonUtil.isNullOrEmpty(templateId)) {
				EntityTemplate entityTemplate = entityTemplateDao.queryById(templateId);
				entity.setProperties(entityTemplate.getProperties());
			}
			editHistoryDetailsMap.put(Dict.ENTITYID, id);//实体ID
			editHistoryDetailsMap.put(Dict.ENTITYNAMEEN, entity.getNameEn());//实体英文名
			editHistoryDetailsMap.put(Dict.ENTITYNAMECN, entity.getNameCn());//实体中文名
			editHistoryDetailsMap.put(Dict.OPERATETYPE, "1");//0-编辑，1-新增，2-删除
			editHistoryDetailsMap.put(Dict.FIELDS, entity.getProperties());//字段列表
			editHistoryDetailsMap.put(Dict.BEFORE, new HashMap());//新增 赋空
			editHistoryDetailsMap.put(Dict.AFTER, propertiesValue);//修改后字段
			editHistoryDetailsMap.put(Dict.ENVNAME, envName);//环境名
			editHistoryDetailsMap.put(Dict.DESC, (String) requestParam.get(Dict.DESC));//描述
			editHistoryDetails(editHistoryDetailsMap);
		}
		return id;
	}

	@Override
	public String updateEntityClass(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);//ID
        String envType = (String) requestParam.get(Dict.ENVTYPE);//环境
        String envName = (String) requestParam.get(Dict.ENVNAME);//环境名称
        Map propertiesValue = (Map) requestParam.get(Dict.PROPERTIESVALUE);//实体字段值
		//获取原实体
		Entity entity = entityDao.queryEntityModelById(id);
		//非空字段校验
      	checkPropertiesValue(entity,propertiesValue);
		Map oldPropertiesValue = entity.getPropertiesValue();
		Map propertiesEnv = (Map) oldPropertiesValue.get(envType);
		//获取修改前字段
		Map oldPropertiesEnvName = (Map)propertiesEnv.get(envName);
		propertiesEnv.put(envName, propertiesValue);
		oldPropertiesValue.put(envType, propertiesEnv);
		entity.setPropertiesValue(oldPropertiesValue);
		entity.setUpdateUser(getEntityUser());//最新更新人
		entity.setUpdateTime(TimeUtils.formatToday());//更新时间
		entityDao.updateEntityModel(entity);
		String templateId = entity.getTemplateId();
		//查询字段列表
		if(!CommonUtil.isNullOrEmpty(templateId)) {
			EntityTemplate entityTemplate = entityTemplateDao.queryById(templateId);
			entity.setProperties(entityTemplate.getProperties());
		}
		//保存操作日志
		Map<String, Object> editHistoryDetailsMap = new  HashMap<String, Object>();
		editHistoryDetailsMap.put(Dict.ENTITYID, id);//实体ID
		editHistoryDetailsMap.put(Dict.ENTITYNAMEEN, entity.getNameEn());//实体英文名
		editHistoryDetailsMap.put(Dict.ENTITYNAMECN, entity.getNameCn());//实体中文名
		editHistoryDetailsMap.put(Dict.OPERATETYPE, "0");//0-编辑，1-新增，2-删除
		editHistoryDetailsMap.put(Dict.FIELDS, entity.getProperties());//字段列表
		editHistoryDetailsMap.put(Dict.BEFORE, oldPropertiesEnvName);//修改前字段
		editHistoryDetailsMap.put(Dict.AFTER, propertiesValue);//修改后字段
		editHistoryDetailsMap.put(Dict.ENVNAME, envName);//环境名
		editHistoryDetailsMap.put(Dict.DESC, (String) requestParam.get(Dict.DESC));//描述
		editHistoryDetails(editHistoryDetailsMap);
		return id;
	}

	@Override
	public String deleteEntityClass(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);//ID
        String envType = (String) requestParam.get(Dict.ENVTYPE);//环境
        String envName = (String) requestParam.get(Dict.ENVNAME);//环境名称
        //获取原实体
        Entity entity = entityDao.queryEntityModelById(id);
        Map oldPropertiesValue = entity.getPropertiesValue();
        //获取该环境所有映射
        Map propertiesEnv = (Map) oldPropertiesValue.get(envType);
        //获取删除前字段
        Map oldPropertiesEnvName = (Map)propertiesEnv.get(envName);
        //删除该映射
        propertiesEnv.remove(envName);
        if(!CommonUtil.isNullOrEmpty(propertiesEnv)) {
        	oldPropertiesValue.put(envType, propertiesEnv);
        } else {
        	oldPropertiesValue.remove(envType);
        }
        //重新插入数据库
        entity.setPropertiesValue(oldPropertiesValue);
        //更新
        entity.setUpdateUser(getEntityUser());//最新更新人
		entity.setUpdateTime(TimeUtils.formatToday());//更新时间
		entityDao.updateEntityModel(entity);
		String templateId = entity.getTemplateId();
		//查询字段列表
		if(!CommonUtil.isNullOrEmpty(templateId)) {
			EntityTemplate entityTemplate = entityTemplateDao.queryById(templateId);
			entity.setProperties(entityTemplate.getProperties());
		}
		//保存操作日志
		Map<String, Object> editHistoryDetailsMap = new  HashMap<String, Object>();
		editHistoryDetailsMap.put(Dict.ENTITYID, id);//实体ID
		editHistoryDetailsMap.put(Dict.ENTITYNAMEEN, entity.getNameEn());//实体英文名
		editHistoryDetailsMap.put(Dict.ENTITYNAMECN, entity.getNameCn());//实体中文名
		editHistoryDetailsMap.put(Dict.OPERATETYPE, "2");//0-编辑，1-新增，2-删除
		editHistoryDetailsMap.put(Dict.FIELDS, entity.getProperties());//字段列表
		editHistoryDetailsMap.put(Dict.BEFORE, oldPropertiesEnvName);//修改前字段
		editHistoryDetailsMap.put(Dict.AFTER, new HashMap());//修改后字段
		editHistoryDetailsMap.put(Dict.ENVNAME, envName);//环境名
		editHistoryDetailsMap.put(Dict.DESC, (String) requestParam.get(Dict.DESC));//描述
		editHistoryDetails(editHistoryDetailsMap);
		return id;
	}

	private EntityUser getEntityUser() throws Exception {
		EntityUser entityUser = new EntityUser();
		User user = userVerifyUtil.getRedisUser();
		entityUser.setUserId(user.getId());
		entityUser.setNameEn(user.getUser_name_en());
		entityUser.setNameCn(user.getUser_name_cn());
		return entityUser;
	}

	@Override
	public List<Entity> queryEntityMapping(List<String> entityIdList, String envNameEn) throws Exception {
		List<Entity> entityList = entityDao.queryEntityModelByIds(entityIdList);
		//获取环境信息
		Env env = new Env();
		if(!CommonUtil.isNullOrEmpty(envNameEn)) {
			Map<String, Object>  envMap = new HashMap();
	        envMap.put(Dict.NAMEEN,envNameEn);
			env = envDao.queryEnv(envMap);
		}
		if (CommonUtil.isNullOrEmpty(env)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"环境不存在！", envNameEn});
		}
        String envType = env.getType();
		if(!CommonUtil.isNullOrEmpty(entityList)) {
			for(Entity entity : entityList) {
				Map<String, Object> propertiesValue = entity.getPropertiesValue();
				Map<String, Object> newPropertiesValue = new HashMap();
				if(!CommonUtil.isNullOrEmpty(envType)) {
					Map propertiesValueEnvName = (Map) propertiesValue.get(envType);
					if(!CommonUtil.isNullOrEmpty(propertiesValueEnvName))
					newPropertiesValue.put(envNameEn,propertiesValueEnvName.get(envNameEn));
				}else {
					Set<String> propertiesKeys= propertiesValue.keySet();
					for(String propertiesKey : propertiesKeys) {
						Map propertiesValueEnvName = (Map) propertiesValue.get(propertiesKey);
						newPropertiesValue.putAll(propertiesValueEnvName);
					}
				}
				entity.setPropertiesValue(newPropertiesValue);
			}
		}
		return entityList;
	}

	@Override
	public String queryConfigTemplate(Map<String, String> requestParam) throws Exception {
		// 应用id
		String projectId = requestParam.get(Dict.PROJECTID).trim();
		// feature 分支
		String featureBranch = requestParam.get(Dict.FEATUREBRANCH).trim();
		// 通过id 查询应用信息
		Map<String, Object> app = restService.queryApp(projectId);
		String gitlabId = ((Integer) app.get(Dict.GITLAB_PROJECT_ID)).toString();
		this.gitlabApiService.checkBranch(this.token, gitlabId, featureBranch);
		String content = this.gitlabApiService.getFileContent(this.token, gitlabId, featureBranch, this.applicationFileCi);
		if(CommonUtil.isNullOrEmpty(content)) {
			 content = this.gitlabApiService.getFileContent(this.token, gitlabId, featureBranch, this.applicationFile);
		}
		return content ;
	}

	@Override
	public Map queryServiceEntity(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
		Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
		String entityTemplateId = (String) requestParam.get(Dict.TEMPLATEID);//实体模板
		//根据条件查询实体列表
		Map map = entityDao.queryServiceEntity(entityTemplateId,page,perPage);
		List<Entity> entityModelList = (List<Entity>) map.get(Dict.ENTITYMODELLIST);
		//实体列表不为空
		if(!CommonUtil.isNullOrEmpty(entityModelList)) {
			for (Entity entity : entityModelList) {
				//组装用户信息
				entity.setCreateUserName(entity.getCreateUser().getNameCn());
				entity.setUpdateUserName(entity.getUpdateUser().getNameCn());
				entity.setCreateUserId(entity.getCreateUser().getUserId());
				entity.setUpdateUserId(entity.getUpdateUser().getUserId());
				//获取所属范围名称
				String templateId = entity.getTemplateId();
				//实体模块ID不为空 获取实体模块字段
				if(!CommonUtil.isNullOrEmpty(templateId)) {
					Map param = new HashMap();
					param.put(Dict.ID,templateId);
					EntityTemplate entityTemplate = entityTemplateService.queryById(param);
					entity.setProperties(entityTemplate.getProperties());
					//查询模板名称
					entity.setTemplateName(entityTemplate.getNameCn());
				} else {
					//为空赋值自定义实体
					entity.setTemplateName(Constants.EMPTYTEMPLATE);
				}
			}
		}
		map.put(Dict.ENTITYMODELLIST, entityModelList);
		return map;
	}

	@Override
	public Map<String, Object> previewConfigFile(Map<String, Object> requestParam) throws Exception {
		String envName = (String) requestParam.get(Dict.ENVNAME);
        String content = (String) requestParam.get(Dict.CONTENT);
        String projectId = (String) requestParam.get(Dict.PROJECTID);
        //获取环境信息
		Map envMap = new HashMap();
	    envMap.put(Dict.NAMEEN,envName);
	    Env env = envDao.queryEnv(envMap);
	    if (CommonUtil.isNullOrEmpty(env)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"环境不存在！", envName});
        }
		// 解析配置模板，校验模板，获得模板中所引用的实体信息
		return replaceConfigFile(content,env,projectId);
	}

	@Override
	public String saveDevConfigProperties(Map<String, Object> requestParam) throws Exception {
		String projectId = (String) requestParam.get(Dict.PROJECTID);//应用id
        String content = (String) requestParam.get(Dict.CONTENT);//前端:配置模板（中间的）内容
        String envName = (String) requestParam.get(Dict.ENVNAME);//环境名
    	Map sitEnvMap = new HashMap();
    	sitEnvMap.put(Dict.NAMEEN, envName );
    	Env env = envDao.queryEnv(sitEnvMap);
        if (CommonUtil.isNullOrEmpty(env)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"环境不存在！", envName});
        }
        Map<String, Object> appInfo = restService.queryApp(projectId);
        String fileCont = (String) replaceConfigFile(content,env,projectId).get(Dict.CONTENT);
        Map<String, Object> devMap = new HashMap<>();
        devMap.put(Dict.PROJECTNAME, appInfo.get(Dict.NAMEENN));
        devMap.put(Dict.FDEVCONFIGHOSTIP, fdevConfigHostIp);
        devMap.put(Dict.FDEVCONFIGUSER, fdevConfigUser);
        devMap.put(Dict.FDEVCONFIGPASSWORD, fdevConfigPassword);
        devMap.put(Dict.FDEVCONFIGDIR, fdevConfigDir);
        String remotePath = pushConfigFile(devMap, fileCont, env.getNameEn());
        return remotePath + "，配置中心Ip:" + devMap.get(Dict.FDEVCONFIGHOSTIP);
	}

	/**
     * 上传配置文件至配置中心
     *
     * @param requestParam
     * @param fileCont
     * @param envName
     * @return
     */
    private String pushConfigFile(Map<String, Object> requestParam, String fileCont, String envName) {
        // 生成本地配置文件
        String firstContentShowEnvName = Constants.CONFIG_FILE_FIRST_LINE + envName + "\n";
        fileCont = firstContentShowEnvName + fileCont;
        String ciProjectName = (String) requestParam.get(Dict.PROJECTNAME);
        String fileName = ciProjectName + "-fdev.properties";
        File file = new File(propertiesDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fdevRuntimePath = propertiesDir + fileName;
        try (PrintWriter printWriter = new PrintWriter(fdevRuntimePath, StandardCharsets.UTF_8.name())) {
            printWriter.write(fileCont);
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{e.getMessage()});
        }
        // 上传配置中心
        String fdevConfigHostIp = (String) requestParam.get(Dict.FDEVCONFIGHOSTIP);
        String fdevConfigUser = (String) requestParam.get(Dict.FDEVCONFIGUSER);
        String fdevConfigPassword = (String) requestParam.get(Dict.FDEVCONFIGPASSWORD);
        String fdevConfigDir = (String) requestParam.get(Dict.FDEVCONFIGDIR);
        String[] nameEnSplit = ciProjectName.split("-");
        String projectName = nameEnSplit[0];
        if (!fdevConfigDir.endsWith("/")) {
            fdevConfigDir += "/";
        }
        String remoteDirectory = fdevConfigDir + projectName + "/";
        SFTPClient sftpClient = new SFTPClient(fdevConfigHostIp, fdevConfigUser, fdevConfigPassword);
        sftpClient.pushConfig(remoteDirectory, fdevRuntimePath);
        return remoteDirectory;
    }
	/**
     * 解析配置模板，校验模板，获得模板中所引用的实体信息
     *
     * @param configFileContent
     * @return
	 * @throws Exception
     */
    public Map<String, Object> replaceConfigFile(String configFileContent, Env env ,String projectId) throws Exception {
    	String envType = env.getType();
    	String envName = env.getNameEn();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, String> modelParam = new HashMap<>();
        // 记录等号左边的appkey
        List<String> leftAppKey = new ArrayList<>();
        //组装预览配置
        StringBuilder contentBuilder = new StringBuilder();

        Set<String> entityNameSet = new HashSet<>();
        Map<String,Entity> entityMap = new HashMap<String,Entity>();
        // 解析配置模板
        configFileContent = configFileContent.replace("\r", "");
        String[] configFileContentSplit = configFileContent.split("\n");
        // 获取优先生效参数
        Map<String, String> appKeyMap = getAppKeyMap(projectId, env.getType());
        for (int i = 0; i < configFileContentSplit.length; i++) {
        	Integer lineNum = i + 1;
            String lineContent = configFileContentSplit[i];
            // 跳过注释
            if (lineContent.startsWith(Constants.NOTE_PLACEHOLDER)) {
            	contentBuilder.append(lineContent).append("\n");
                continue;
            }
            // 跳过不包括等号的行
            String[] lineSplit = lineContent.split("=", 2);
            if (lineSplit.length <= 1) {
            	contentBuilder.append(lineContent).append("\n");
                continue;
            }
            // 记录等号左边的appkey
            leftAppKey.add(lineSplit[0]);
            // 获取优先生效参数，若存在优先生效参数，则直接替换为优先生效参数，无需进行后面的逻辑处理
            String appKey = lineSplit[0];
            String outSideValue = appKeyMap.get(appKey);
            if (!CommonUtil.isNullOrEmpty(outSideValue)) {
                lineContent = appKey + "=" + outSideValue;
                contentBuilder.append(lineContent).append("\n");
                continue;
            }
            String rightContent = lineSplit[1];
            if (rightContent.contains("$<")) {
                String[] rightContentSplit = rightContent.split("\\$<");
                for (int k = 1; k < rightContentSplit.length; k++) {
                	String envValue = "";
                    String singleRightContentSplit = rightContentSplit[k];
                    if (singleRightContentSplit.contains(">")) {
                        String modelField = singleRightContentSplit.split(">", 2)[0];

                        if (!(StringUtils.isEmpty(modelField) || modelField.contains("$") || modelField.contains("<"))) {
                            String[] centerContentSplit = modelField.split("\\.");
                            // "$<"与">"之间的内容，包含且只包含一个"."，且"."前后的内容都不能为空
                            if (!modelField.endsWith(".") && centerContentSplit.length == 2 && StringUtils.isNotEmpty(centerContentSplit[0]) && StringUtils.isNotEmpty(centerContentSplit[1])) {
                            	//实体名
                            	String entityName = centerContentSplit[0];
                            	Entity entity = new Entity();
                            	if(!entityNameSet.contains(entityName)) {
                            		Map<String, Object> entityQuery = new HashMap<String, Object>();
                            		entityQuery.put(Dict.NAMEEN, entityName);
                                	entity = entityDao.queryOneEntityModel(entityQuery);
                                	entityNameSet.add(entityName);
                                	entityMap.put(entityName, entity);
                            	}else {
                            		entity = entityMap.get(entityName);
                            	}
                            	if(!CommonUtil.isNullOrEmpty(entity)) {
                            		//获取当前环境type字段值
                            		if(!CommonUtil.isNullOrEmpty(entity.getPropertiesValue())) {
                            			Map envTypeMap = (Map) entity.getPropertiesValue().get(envType);
                            			if(!CommonUtil.isNullOrEmpty(envTypeMap)) {
                                			//获取当前环境字段值
                                			Map envValueMap = (Map) envTypeMap.get(envName);
                                			if(!CommonUtil.isNullOrEmpty(envValueMap)) {

                                				// 获取该属性的映射值
                                				envValue = (String) envValueMap.get(centerContentSplit[1]);
                                			}
                                		}
                            		}
                            	}
                            }
                        }
                        if (CommonUtil.isNullOrEmpty(envValue)) {
                            envValue = Constants.ERROR;
                        }
                        modelParam.put(modelField, envValue);
                        rightContent = rightContent.replace("$<" + modelField + ">", envValue);
                    }
                }
                contentBuilder.append(appKey).append("=").append(rightContent).append("\n");
            } else {
                contentBuilder.append(lineContent).append("\n");
            }
        }
     // 等号左边的appKey不能重复
        Set<String> repeatKey = CommonUtil.checkRepeat(leftAppKey);
        if (CollectionUtils.isNotEmpty(repeatKey)) {
        	throw new FdevException(ErrorConstants.CONFIGFILE_KEY_ERROR, new String[]{repeatKey.toString()});
        }
        resultMap.put(Dict.CONTENT, contentBuilder.toString());
        resultMap.put(Dict.MODELPARAM, modelParam);
        resultMap.put(Dict.OUTSIDEPARAM, appKeyMap);
        return resultMap;
    }

    /**
     * 获取指定应用在指定环境下的优先生效配置参数
     *
     * @param appId
     * @param envNameEn
     * @return
     */
    private Map<String, String> getAppKeyMap(String appId, String envNameEn) throws Exception {
        Map<String, String> appKeyMap = new HashMap<>();
        OutSideTemplate outSideTemplate = new OutSideTemplate();
        outSideTemplate.setProjectId(appId);
        List<OutSideTemplate> outSideTemplateList = this.outsideTemplateDao.query(outSideTemplate);
        if(!CommonUtil.isNullOrEmpty(outSideTemplateList)) {
        	outSideTemplate = outSideTemplateList.get(0);
        	if (outSideTemplate != null) {
                List<Map<String, String>> variableList = outSideTemplate.getVariables();
                for (Map<String, String> params : variableList) {
                    String appkey = params.get(Dict.APPKEY);
                    String value = params.get(Dict.VALUE);
                    if (envNameEn != null && envNameEn.equals(params.get(Dict.ENVNAME))) {
                        appKeyMap.put(appkey, value);
                    }
                }
            }
        }

        return appKeyMap;
    }

	@Override
	public Map querySectionEntity(Map<String, Object> requestParam) throws Exception {
		Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
		Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
		String entityTemplateId = (String) requestParam.get(Dict.TEMPLATEID);//实体模板
		//根据条件查询实体列表
		Map map = entityDao.queryServiceEntity(entityTemplateId,page,perPage);
		List<Entity> entityModelList = (List<Entity>) map.get(Dict.ENTITYMODELLIST);
		//实体列表不为空
		if(!CommonUtil.isNullOrEmpty(entityModelList)) {
			for (Entity entity : entityModelList) {
				//组装用户信息
				entity.setCreateUserName(entity.getCreateUser().getNameCn());
				entity.setUpdateUserName(entity.getUpdateUser().getNameCn());
				entity.setCreateUserId(entity.getCreateUser().getUserId());
				entity.setUpdateUserId(entity.getUpdateUser().getUserId());
				//获取所属范围名称
				String templateId = entity.getTemplateId();
				//实体模块ID不为空 获取实体模块字段
				if(!CommonUtil.isNullOrEmpty(templateId)) {
					Map param = new HashMap();
					param.put(Dict.ID,templateId);
					EntityTemplate entityTemplate = entityTemplateService.queryById(param);
					entity.setProperties(entityTemplate.getProperties());
					//查询模板名称
					entity.setTemplateName(entityTemplate.getNameCn());
				} else {
					//为空赋值自定义实体
					entity.setTemplateName(Constants.EMPTYTEMPLATE);
				}
			}
		}
		map.put(Dict.ENTITYMODELLIST, entityModelList);
		return map;
	}

	@Override
	public Map<String, Object> getMappingHistoryList(Map<String, Object> requestParam) throws Exception {
		Map<String, Object> historyDetails = entityDao.queryHistoryDetails(requestParam);
		return historyDetails;
	}

	//日志信息实体组装 入库
	public void editHistoryDetails(Map<String, Object> requestParam) throws Exception {
		User user = userVerifyUtil.getRedisUser();
		ObjectId objectId = new ObjectId();
        String id = objectId.toString();
		HistoryDetails historyDetails = new HistoryDetails();
        historyDetails.set_id(objectId);
        historyDetails.setId(id);
        historyDetails.setUpdateUserId(user.getId());
        historyDetails.setUpdateUserName(user.getUser_name_cn());
        historyDetails.setEntityId((String)requestParam.get(Dict.ENTITYID));
        historyDetails.setEntityNameEn((String)requestParam.get(Dict.ENTITYNAMEEN));
        historyDetails.setEntityNameCn((String)requestParam.get(Dict.ENTITYNAMECN));
        historyDetails.setOperateType((String)requestParam.get(Dict.OPERATETYPE));
        historyDetails.setFields((List<Map<String, Object>>)requestParam.get(Dict.FIELDS));
        historyDetails.setBefore((Map<String, Object>)requestParam.get(Dict.BEFORE));
        historyDetails.setAfter((Map<String, Object>)requestParam.get(Dict.AFTER));
        historyDetails.setUpdateTime(TimeUtils.formatToday());
        historyDetails.setEnvName((String)requestParam.get(Dict.ENVNAME));
        historyDetails.setDesc((String)requestParam.get(Dict.DESC));
        entityDao.addHistoryDetails(historyDetails);
	}

	@Override
	public void deleteEntity(Map<String, Object> requestParam) throws Exception {
    	String entityId = requestParam.get(Dict.ID).toString();
		Entity entity = entityDao.queryEntityModelById(entityId);
		String entityNameEn = entity.getNameEn();
		requestParam.put(Dict.ENTITYNAMEEN,entityNameEn);
		//判断是否有配置依赖
		Long count = (Long) configFileService.queryConfigDependency(requestParam).get(Dict.COUNT);
		//判断是否有部署依赖
		Map<String, Object> deployDependency = configFileService.queryDeployDependency(requestParam);
		if(count == 0 && (Integer) deployDependency.get(Dict.COUNT) == 0){
			entityDao.deleteEntity(entity);
			//记录日志 删除实体
			addEntityLog(entity,new ArrayList<>(),"删除");
		}else {
			throw new FdevException(ErrorConstants.CON_NOT_DELETE);
		}
	}

	@Override
	public  Map<String, Object> queryEntityLog(Map<String, Object> requestParam) throws Exception {
		return entityDao.queryEntityLog(requestParam);
	}

	/**
	 * 复制实体
	 * 模板实体属性不可更改；自定义实体可更改
	 * @param requestParam
	 * @return
	 */
	@Override
	public Map<String, String> copyEntity(Map<String, Object> requestParam) throws Exception {
		Entity newEntity = new Entity();
    	String nameEn = (String) requestParam.get(Dict.NAMEEN);
    	String nameCn = (String) requestParam.get(Dict.NAMECN);
    	String copyId = (String) requestParam.get(Dict.COPYID); //被复制实体的id
		if(checkEntityModel(requestParam).get(Dict.NAMEENFLAG) || checkEntityModel(requestParam).get(Dict.NAMECNFLAG)){
			throw new FdevException(ErrorConstants.ENTITY_NAME_ERROR);
		}

		Entity oldEntity = entityDao.queryEntityModelById(copyId);
		//实体基础信息
		newEntity.setNameEn(nameEn);
		newEntity.setNameCn(nameCn);
		User user = userVerifyUtil.getRedisUser();
		EntityUser entityUser = new EntityUser(user.getId(),user.getUser_name_en(),user.getUser_name_cn());
		newEntity.setCreateUser(entityUser);
		newEntity.setUpdateUser(entityUser);
		newEntity.setCreateTime(CommonUtil.formatDate("yyyy-MM-dd hh:mm:ss"));
		newEntity.setUpdateTime(CommonUtil.formatDate("yyyy-MM-dd hh:mm:ss"));

		List<Map<String, Object>> properties = (List<Map<String, Object>>)requestParam.get(Dict.PROPERTIES);
		List<Map<String, Object>> newProperties = new ArrayList<>();//修改后的实体属性
		Map<String, Object> newPropertiesValue = new HashMap<>();//修改后的映射值
		//提取原实体的映射值
		Map<String, Object> oldPropertiesValue = oldEntity.getPropertiesValue();
		newEntity.setPropertiesValue(oldPropertiesValue);
		newPropertiesValue = oldPropertiesValue;
		if(CommonUtil.isNullOrEmpty(oldEntity.getTemplateId())){
			//自定义实体，修改属性和映射值
			for(Map property:properties){
				Map newProperty = new HashMap();
				newProperty.put(Dict.NAMEEN,property.get(Dict.NEWNAMEEN));
				newProperty.put(Dict.NAMECN,property.get(Dict.NEWNAMECN));
				newProperty.put(Dict.REQUIRED,property.get(Dict.NEWREQUIRED));
				newProperty.put(Dict.TYPE,"string");
				if(CommonUtil.isNullOrEmpty(property.get(Dict.OLDNAMEEN)) && !CommonUtil.isNullOrEmpty(property.get(Dict.NEWNAMEEN))){
					newProperties.add(newProperty);
					//新增属性--映射值不变
				}else if(!CommonUtil.isNullOrEmpty(property.get(Dict.OLDNAMEEN)) && !CommonUtil.isNullOrEmpty(property.get(Dict.NEWNAMEEN)) && !property.get("newNameEn").equals(property.get("oldNameEn"))){
					newProperties.add(newProperty);
					//英文名修改--直接修改映射值的key
					this.updatePropertiesValue(newPropertiesValue,(String) property.get(Dict.OLDNAMEEN),(String) property.get(Dict.NEWNAMEEN));
				}else if(!CommonUtil.isNullOrEmpty(property.get(Dict.NEWNAMEEN)) && property.get(Dict.NEWNAMEEN).equals(property.get(Dict.OLDNAMEEN))){
					newProperties.add(newProperty);
					//英文不变--不涉及修改映射值
				}else {
					//字段删除（中英文名全为null）--删除映射值对应的key和value
					this.updatePropertiesValue(newPropertiesValue,(String) property.get(Dict.OLDNAMEEN),(String) property.get(Dict.NEWNAMEEN));
				}
			}
			newEntity.setPropertiesValue(newPropertiesValue);
			newEntity.setProperties(newProperties);
		}else {
			//非自定义实体，不修改属性和映射值
			newEntity.setTemplateId(oldEntity.getTemplateId());
		}
		ObjectId objectId = new ObjectId();
		String id = objectId.toString();
		newEntity.set_id(objectId);
		newEntity.setId(id);
		entityDao.addEntityModel(newEntity);
		addEntityLog(newEntity,new ArrayList<>(),"新增");
		Map resultMap = new HashMap();
		resultMap.put(Dict.ID,id);
		return resultMap;
	}

	@Override
	public void deleteConfigDependency(Map<String, Object> requestParam) throws Exception {
		//删除配置依赖
		serviceConfigDao.deleteServiceConfig(requestParam);

		//删除应用下所有实体
		String serviceId = String.valueOf(requestParam.get(Dict.ID));
		Map<String, Object> map = new HashMap<String,Object>();
		map.put(Dict.SCOPEID, serviceId);
		Map<String , Object> entityMap = entityDao.queryEntityModel(map);
		if(!CommonUtil.isNullOrEmpty(entityMap.get(Dict.ENTITYMODELLIST))) {
			List<Entity> list = (List<Entity>) entityMap.get(Dict.ENTITYMODELLIST);
			for(Entity entity : list) {
				entityDao.deleteEntity(entity);
			}
		}

	}

}
