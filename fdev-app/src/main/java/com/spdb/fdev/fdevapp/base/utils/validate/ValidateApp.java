package com.spdb.fdev.fdevapp.base.utils.validate;


import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.dao.ICommonDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.transport.RestTransport;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xxx
 * @date 2019/5/16 17:00
 */
@Component
public class ValidateApp {

	/**
	 * 检验 应用 英文，中文名不能重复
	 * @param query
	 * @param appEntity
	 */
	public static void checkAppNameEnAndNameZh(List<AppEntity> query, AppEntity appEntity) {
		for (AppEntity entity : query) {
			if (CommonUtils.isNullOrEmpty(entity.getName_zh()) || CommonUtils.isNullOrEmpty(entity.getName_en())) {
				continue;
			}
			if (appEntity.getName_en().equals(entity.getName_en())) {
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"应用英文名太火爆了,再换一个试试"});
			}
			if (appEntity.getName_zh().equals(entity.getName_zh())) {
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"应用中文名太火爆了,再换一个试试"});
			}
		}
	}
   
	/**
	 * 校验传入的值是否已在数据库中存在(修改数据时使用)
	 * @param parm 查询参数，可以是map也可以是实体类
	 * @param operator 逻辑符（目前只支持and、or）
	 * @param validateParms 需要校验的key或field
	 * @param clazz 查询表所关联的class对象
	 * @param commonDao 通用查询的dao实例
	 * @throws Exception
	 */
	public static void validateRepeatParmForUpdate(Object parm,String operator,String [] validateParms,Class<?> clazz,ICommonDao commonDao) throws Exception{
		Map<Object,Object> validateMap = new HashMap<Object,Object>();
		for(String validateParm:validateParms) {
			if(parm instanceof Map) {
				Map m = (Map)parm;
				validateMap.put(validateParm, m.get(validateParm));
			}else {
				Object value;
				try {
					value = CommonUtils.getGetMethod(parm, validateParm);
					validateMap.put(validateParm,value);
				} catch (Exception e) {
				}
			}
		}
		
		
		List<?> commonQuery = commonDao.commonQuery(validateMap, operator, clazz);
		String errorMsg = "";
		if(commonQuery!=null&&commonQuery.size()>1) {
			Object object = commonQuery.get(0);
			for(String validateParm : validateParms) {
				try {
					String existValue = CommonUtils.getGetMethod(object, validateParm).toString();
					String nowValue = CommonUtils.getGetMethod(parm, validateParm).toString();
					if(existValue.equals(nowValue)) {
						errorMsg+= CommonUtils.getFiledAnnotationVal(parm, validateParm,ApiModelProperty.class,"value");
						errorMsg+="太火爆了,再换一个试试";
					}
				} catch (Exception e) {
				}

			}
		}else if(commonQuery!=null&&commonQuery.size()==1) {
			Object object = commonQuery.get(0);
			Object queryId = CommonUtils.getGetMethod(object, "id");
			Object Id = CommonUtils.getGetMethod(parm, "id");
			if(!queryId.equals(Id)) {    
				for(String validateParm : validateParms) {
					try {
						String existValue = CommonUtils.getGetMethod(object, validateParm).toString();
						String nowValue = CommonUtils.getGetMethod(parm, validateParm).toString();
						if(existValue.equals(nowValue)) {
							errorMsg+= CommonUtils.getFiledAnnotationVal(parm, validateParm,ApiModelProperty.class,"value");
							errorMsg+="太火爆了,再换一个试试";
						}
					} catch (Exception e) {
					}

				}
			}
		}

		if(!"".equals(errorMsg)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
		}
	}

	/**
	 * 校验传入的值是否已在数据库中存在(新增数据时使用)
	 * @param parm 查询参数，可以是map也可以是实体类
	 * @param operator 逻辑符（目前只支持and、or）
	 * @param validateParms 需要校验的key或field
	 * @param clazz 查询表所关联的class对象
	 * @param commonDao 通用查询的dao实例
	 * @throws Exception
	 */
	public static void validateRepeatParm(Object parm,String operator,String [] validateParms,Class<?> clazz,ICommonDao commonDao) throws Exception {
		Map<Object,Object> validateMap = new HashMap<Object,Object>();
		if(parm instanceof Map) {
			validateMap = (Map)parm;
		}else {
			for(String validateParm:validateParms) {
				Object value;
				try {
					value = CommonUtils.getGetMethod(parm, validateParm);
					validateMap.put(validateParm,value);
				} catch (Exception e) {
				}
			}
		}
		List<?> commonQuery = commonDao.commonQuery(validateMap, operator, clazz);
		String errorMsg = "";
		if(commonQuery!=null&&commonQuery.size()>0) {
			Object object = commonQuery.get(0);
			for(String validateParm : validateParms) {
				try {
					String existValue = CommonUtils.getGetMethod(object, validateParm).toString();
					String nowValue = CommonUtils.getGetMethod(parm, validateParm).toString();
					if(existValue.equals(nowValue)) {
						errorMsg+= CommonUtils.getFiledAnnotationVal(parm, validateParm,ApiModelProperty.class,"value");
						errorMsg+="太火爆了,再换一个试试";
					}
				} catch (Exception e) {
				}

			}
		}

		if(!"".equals(errorMsg)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
		}

	}
	
	/**
	 * 校验传入的key值是否已存在
	 * @param parmList
	 * @param key
	 */
	public static void validateRepeatParm(List<Object> parmList,String key) {
		Set<String> set = new HashSet<String>();
		for(Object o : parmList) {
			Map<String,String> map = (Map<String,String>)o;
			if(set.contains(map.get(key))) {
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"实体KEY中"+key+"值："+map.get(key)+"重复"}); 
			}else {
				set.add(map.get(key));
			}
		}
	}
	
	/**
	 * 校验实体类型（目前只支持deploy和runtie两种类型）
	 * @param type 客户端传入的类型
	 */
	public static void validateModelType(String type) {
		if(!"deploy".equals(type)&&!"runtime".equals(type)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"实体类型不正确"}); 
		}
	}

	/**
	 * 校验应用英文名
	 *
	 * @param appName
	 * @return
	 */
	public static boolean validateAppEnName(String appName) {
		if (CommonUtils.isNullOrEmpty(appName))
			return false;
		Pattern humpPattern = Pattern.compile("^[a-z][a-z0-9-]*[a-z]$");
		Matcher matcher = humpPattern.matcher(appName);
		if (!matcher.find()) {
			return false;
		}
		int count = 0;
		Pattern pattern = Pattern.compile("-");
		matcher = pattern.matcher(appName);
		while (matcher.find()) {
			count++;
		}
		if (count != 2 || appName.contains("--")) {
			return false;
		}
		return true;
	}

	/**
	 * 校验应用英文名，不校验三段式
	 *
	 * @param appName
	 * @return
	 */
	public static boolean validateAppEnNameNoThreeStage(String appName) {
		if (CommonUtils.isNullOrEmpty(appName))
			return false;
		Pattern humpPattern = Pattern.compile("^[a-z][a-z0-9-]*[a-z]$");
		Matcher matcher = humpPattern.matcher(appName);
		if (!matcher.find()) {
			return false;
		}
		return true;
	}

	/**
	 * 校验用户权限
	 * @param roleList
	 * @param authority
	 * @return
	 */
	public static Boolean validateUserAuthority(ArrayList roleList, List<String> authority){
        Boolean authorityFlag = false;
        for (String authManager : authority) {
            if (!CommonUtils.isNullOrEmpty(roleList) && (roleList.contains(authManager))) {
            	authorityFlag = true;
                return authorityFlag;
            }
        }
       return authorityFlag;
    }
	public static Boolean validateUserStuckPoint(ArrayList roleList, List<String> StuckPoint){
		Boolean stuckPointFlag = false;
		for (String stuckPoint : StuckPoint) {
			if (!CommonUtils.isNullOrEmpty(roleList) && (roleList.contains(stuckPoint))) {
				stuckPointFlag = true;
				return stuckPointFlag;
			}
		}
		return stuckPointFlag;
	}


	/**
	 * 检验gitlab_project_id 防止重复插入
	 * @param query
	 * @param appEntity
	 */
	public static void checkAppGitlaProjectId(List<AppEntity> query, AppEntity appEntity) {
		for (AppEntity entity : query) {
			if (entity != null && entity.getGitlab_project_id().equals(appEntity.getGitlab_project_id())) {
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"该应用已经存在数据库"});
			}
		}
	}
}
