package com.spdb.fdev.fdevapp.spdb.web;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.entity.AppSystem;
import com.spdb.fdev.fdevapp.spdb.entity.AppType;
import com.spdb.fdev.fdevapp.spdb.entity.DomainEntity;
import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;
import com.spdb.fdev.transport.RestTransport;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Api(tags = "应用数据迁移接口")
@RequestMapping("/api/app")
@RestController
@RefreshScope
public class AppDataMigrationController {


    @Resource
    private MongoTemplate mongoTemplate;
	@Autowired
    private RestTransport restTransport;
    
    @PostMapping("/getApp")
    public JsonResult getApp(@RequestBody Map<String, Object> requestMap) throws Exception {
        Integer size = (Integer) requestMap.get("size");//页面大小
        Integer index = (Integer) requestMap.get("index");//当前页
        String createTime = (String) requestMap.get("createTime");//创建时间
        return JsonResultUtil.buildSuccess(queryAppService(size,index,createTime));
    }
    
    @PostMapping("/getAppCount")
    public JsonResult getAppCount(@RequestBody Map<String, Object> requestMap) throws Exception {
        String createTime = (String) requestMap.get("createTime");//创建时间
        return JsonResultUtil.buildSuccess(getAppCount(createTime));
    }
    
    @PostMapping("/getDomain")
    public JsonResult getDomain(@RequestBody Map<String, Object> requestMap) throws Exception {
        return JsonResultUtil.buildSuccess(getDomain());
    }
    
    @PostMapping("/getServiceType")
    public JsonResult getServiceType(@RequestBody Map<String, Object> requestMap) throws Exception {
        return JsonResultUtil.buildSuccess(getServiceType());
    }
    
    public Long getAppCount(String createTime) throws Exception {
    	Criteria criteria = new Criteria();
    	if(!CommonUtils.isNullOrEmpty(createTime)) {
    		criteria.and("createtime").gte(createTime);
    	}
    	criteria.and("status").ne("0");
    	Query query = new Query();
    	query.addCriteria(criteria);
        return this.mongoTemplate.count(query, AppEntity.class);
   }
    
    public List<AppType> getServiceType() throws Exception {
        return this.mongoTemplate.find(new Query(), AppType.class);
   }
    
    public List<Map> getDomain() throws Exception {
    	List<Map> appMap = new ArrayList<>();
        List<DomainEntity> domain = getDomainDao();
        for (DomainEntity entity : domain) {
            Map<String, Object> map = CommonUtils.object2Map(entity);
            map.put("type", "domain");
            appMap.add(map);
        }
        List<ServiceSystem> serviceSystem = getServiceSystemDao();
        for (ServiceSystem entity : serviceSystem) {
            Map<String, Object> map = CommonUtils.object2Map(entity);
            map.put("type", "business");
            appMap.add(map);
        }
        return appMap;
    }
    
    public List<Map> queryAppService(Integer size,Integer index,String createTime) throws Exception {
        List<AppEntity> query = queryAppDao(size,index,createTime);
        List<Map> appMap = new ArrayList<>();
        for (AppEntity entity : query) {
            Map<String, Object> map = CommonUtils.object2Map(entity);
            //组装类型名称
            AppType appType = findById((String) map.get(Dict.TYPE_ID));
            if (appType != null) {
                map.put(Dict.TYPE_NAME, queryAppTypeValue(appType.getName()));
            }
            //负责人
            HashSet<Map<String, String>> spdb = (HashSet<Map<String, String>>) map.get("spdb_managers");
            Set<String> managers = new HashSet();
            if(!CommonUtils.isNullOrEmpty(spdb)) {
            	for(Map<String, String> spdbManager: spdb) {
            		managers.add(spdbManager.get("id"));
                }
            }
            HashSet<Map<String, String>> dev = (HashSet<Map<String, String>>) map.get("dev_managers");
            if(!CommonUtils.isNullOrEmpty(dev)) {
            	for(Map<String, String> devManager: dev) {
            		managers.add(devManager.get("id"));
                }
            }
            List spdb_managers = new ArrayList();
            List dev_managers = new ArrayList();
            List managerInfo = new ArrayList();
            for(String manager : managers) {
            	Map<String, Object> managerMap= getUserName(manager);
            	if(!CommonUtils.isNullOrEmpty(managerMap)) {
            		if(isInt((String)managerMap.get("email"))) {
                		spdb_managers.add(manager);
                	}else {
                		dev_managers.add(manager);
                	}
                	managerInfo.add(managerMap);
            	}
            	
            }
            map.put("spdb_managers", spdb_managers);
            map.put("dev_managers", dev_managers);
            map.put("managerInfo", managerInfo);
            //组装系统名
            if (!CommonUtils.isNullOrEmpty(entity.getSystem())) {
                AppSystem appSystem = new AppSystem();
                appSystem.setId(entity.getSystem());
                List<AppSystem> appSystemlist = findSystem(appSystem);
                if (!CommonUtils.isNullOrEmpty(appSystemlist)) {
                    map.put("systemName", appSystemlist.get(0).getName());
                }
            }
            appMap.add(map);
        }
        return appMap;
    }
    
    @LazyInitProperty(redisKeyExpression = "fhuyz.username.{userId}")
	public Map<String, Object> getUserName(String userId) throws Exception {
		Map<String, Object> userMap = new HashMap<>();
		Map<String, String> param = new HashMap<>();
        param.put(Dict.ID, userId);
        param.put(Dict.REST_CODE, Dict.QUERYUSER);
        Object submit = this.restTransport.submit(param);
        if (!CommonUtils.isNullOrEmpty(submit)) {
            List userList = net.sf.json.JSONArray.fromObject(submit);
            if (userList.size() > 0) {
                Map parse = (Map) JSONObject.parse(userList.get(0).toString());
                userMap.put("userId", userId);
                userMap.put("nameEN", (String) parse.get(Dict.USER_NAME_EN));
                userMap.put("nameCN", (String) parse.get(Dict.USER_NAME_CN));
                userMap.put("email", (String) parse.get("email"));
                userMap.put("gitId", (String) parse.get(Dict.GIT_USER_ID));
            }
        }
        return userMap;
	}
    
    public static boolean isInt(String email) {
    	boolean isInt= false;//默认行外
    	if(!CommonUtils.isNullOrEmpty(email) && email.endsWith("com.cn")) {
    		isInt = true;//行内
    	}
    	 return isInt;
    }
    
    public String queryAppTypeValue(String name) throws Exception {
    	String nameValue = "";
    	switch (name) {
			case "IOS应用":
				nameValue = "IOS";
				break;
			case "Android应用":
				nameValue = "Android";
				break;
			case "Java微服务":
				nameValue = "Java";
				break;
			case "Vue应用":
				nameValue = "Vue";
				break;
			case "容器化项目":
				nameValue = "containerItem";
				break;
			default:
				nameValue = "oldService";
				break;
		}
         return nameValue ;
    }
    
    public List<AppEntity> queryAppDao(Integer size,Integer index,String createTime) throws Exception {
    	Integer start = size * (index - 1);   //起始
    	Criteria criteria = new Criteria();
    	if(!CommonUtils.isNullOrEmpty(createTime)) {
    		criteria.and("createtime").gte(createTime);
    	}
    	criteria.and("status").ne("0");
    	Query query = new Query();
    	query.addCriteria(criteria);
    	query.limit(size).skip(start);  //分页
        return this.mongoTemplate.find(query, AppEntity.class);
   }
    
    public AppType findById(String id) throws Exception {
        List <AppType> list = mongoTemplate.find(Query.query(Criteria.where(Dict.ID).is(id)), AppType.class);
        if (list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }
    
    public List<AppSystem> findSystem(AppSystem param) {
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(param.getId()))
            c.and(Dict.ID).is(param.getId());
        if (!CommonUtils.isNullOrEmpty(param.getName()))
            c.and(Dict.NAME).is(param.getName());
        Query query = new Query(c);
        query.fields().exclude(Dict.OBJECTID);
        return this.mongoTemplate.find(query, AppSystem.class, "system");
    }
    
    public List<DomainEntity> getDomainDao() throws Exception {
        return this.mongoTemplate.find(new Query(), DomainEntity.class);
    }
    
    public List<ServiceSystem> getServiceSystemDao() throws Exception {
        return this.mongoTemplate.find(new Query(), ServiceSystem.class);
    }
    
}
