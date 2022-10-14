package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.Token.TokenManger;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.entity.user.UserCommissionEvent;
import com.spdb.fdev.fuser.spdb.service.CommissionEventService;
import com.spdb.fdev.fuser.spdb.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(tags = "代办事项接口")
@RequestMapping("/api/userCommissionEvent")
@RestController
public class UserCommissionEventController {
	private static final Logger logger = LoggerFactory.getLogger(UserCommissionEventController.class);
	
	@Autowired
	private CommissionEventService commissionEventService;
	@Autowired
	private UserService userService;
	@Resource
    private TokenManger tokenManger;
	
	
    @ApiOperation(value = "新增代办事项")
    @RequestMapping(value = "/addCommissionEvent", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addCommissionEvent(@RequestBody UserCommissionEvent userCommissionEvent) throws Exception {
    	List<String> user_ids = userCommissionEvent.getUser_ids();
    	String module = userCommissionEvent.getModule();
    	String description = userCommissionEvent.getDescription();
    	String link = userCommissionEvent.getLink();
    	String target_id = userCommissionEvent.getTarget_id();
    	String type = userCommissionEvent.getType();
    	if(CommonUtils.isNullOrEmpty(user_ids)) {
    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"代办负责人id为空"});
    	}
    	for (String user_id : user_ids) {
    		User user = new User();
    		user.setId(user_id);
			List<Map> queryUser = userService.queryUser(user);
			if(CommonUtils.isNullOrEmpty(queryUser)) {
				throw new FdevException(ErrorConstants.USR_NOT_EXIST);
			}
		}
    	if(CommonUtils.isNullOrEmpty(module)) {
    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"所属模块为空"});
    	}
    	if(CommonUtils.isNullOrEmpty(description)) {
    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"代办描述为空"});
    	}
    	if(CommonUtils.isNullOrEmpty(link)) {
    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"代办相关链接为空"});
    	}
    	if(CommonUtils.isNullOrEmpty(target_id)) {
    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"目标id为空"});
    	}
    	if(CommonUtils.isNullOrEmpty(type)) {
    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"代办类型为空"});
    	}
    	//module字段传入英文转中文
    	String transformString = this.TransformString(module);
    	userCommissionEvent.setModule(transformString);
    	UserCommissionEvent commissionEvent = commissionEventService.queryDetailBytargetIdAndType(target_id,type,transformString);
    	UserCommissionEvent saveCommissionEvent = null;
    	if(CommonUtils.isNullOrEmpty(commissionEvent)) {    		
    		saveCommissionEvent = commissionEventService.addCommissionEvent(userCommissionEvent);
    	}else {
    		saveCommissionEvent = commissionEventService.upsertCommissionEvent(commissionEvent,userCommissionEvent);
		}
    	return JsonResultUtil.buildSuccess(saveCommissionEvent);
    }
	
	
    
    private String TransformString(String module) {
    	String result = "";
		if ("release".equals(module)) {
			result = "投产模块";
		}
		if ("env".equals(module)) {
			result = "环境模块";
		}
		if ("interface".equals(module)) {
			result = "接口模块";
		}
		if ("task".equals(module)) {
			result = "任务模块";
		}
		if ("app".equals(module)) {
			result = "应用模块";
		}
		if ("rqrmnt".equals(module)) {
			result = "需求模块";
		}
		return result;
	}



	@ApiOperation(value = "更新代办事项")
    @RequestMapping(value = "/updateByTargetIdAndType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateByTargetIdAndType(@RequestBody Map<String,String> requestParam,
    		HttpServletRequest request) throws Exception {
    	String target_id = requestParam.get("target_id");
    	String type = requestParam.get("type");
    	String module = requestParam.get("module");
    	String transformString = this.TransformString(module);
    	String executor_id = requestParam.get("executor_id");
    	UserCommissionEvent commissionEvent = commissionEventService.queryDetailBytargetIdAndType(target_id,type,transformString);
    	if(CommonUtils.isNullOrEmpty(commissionEvent) || "1".equals(commissionEvent.getStatus())) {
    		throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"数据不存在或数据异常"});
    	}
        User user = new User();
        user.setId(executor_id);
        List<Map> result = userService.queryUser(user);
		commissionEvent.setExecutor_id(executor_id);
        commissionEvent.setExecutor_name_cn((String)result.get(0).get(Dict.USER_NAME_CN));
        commissionEvent.setExecutor_name_en((String)result.get(0).get(Dict.USER_NAME_EN));
        commissionEvent.setExecuteTime(CommonUtils.formatDate(CommonUtils.DATE_PARSE));
        //已操作待办标签直接为done
        commissionEvent.setLabel("done");
    	UserCommissionEvent update = commissionEventService.updateByTargetIdAndType(commissionEvent);
    	return JsonResultUtil.buildSuccess(update);
    }
    
    @ApiOperation(value = "查询代办事项")
    @RequestMapping(value = "/queryCommissionEvent", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryCommissionEvent(@RequestBody UserCommissionEvent userCommissionEvent
    		, HttpServletRequest request) throws Exception {
    	List<UserCommissionEvent> list = new ArrayList<UserCommissionEvent>();
    	String userNameEn = tokenManger.getUserByToken(request.getHeader(Dict.AUTHORIZATION));
        User loginUser = new User();
        loginUser.setUser_name_en(userNameEn);
        List<Map> result = userService.queryUser(loginUser);
        List<String> list2 = new ArrayList<>();
        list2.add((String)result.get(0).get(Dict.ID));
        userCommissionEvent.setUser_ids(list2);
        List newList = new ArrayList<>();
    	if(!CommonUtils.isNullOrEmpty(userCommissionEvent.getStatus())) {
    		list = commissionEventService.queryCommissionEventByStatus(userCommissionEvent);
    		for (UserCommissionEvent event : list) {
    			List<String> user_ids = event.getUser_ids();
    			List newList2 = new ArrayList<>();
    			for (String userId : user_ids) {
    				Map hashMap = new HashMap<>();
    				User user = new User();
					user.setId(userId);
					List<Map> queryUserCoreData = userService.queryUserCoreData(user);
					if (CommonUtils.isNullOrEmpty(queryUserCoreData)) {
						logger.error("查询用户不存在, 用户id为: " + userId);
						continue;
					}
					hashMap.put(Dict.ID, queryUserCoreData.get(0).get(Dict.ID));
					hashMap.put(Dict.USER_NAME_CN, queryUserCoreData.get(0).get(Dict.USER_NAME_CN));
					hashMap.put(Dict.USER_NAME_EN, queryUserCoreData.get(0).get(Dict.USER_NAME_EN));
					newList2.add(hashMap);
				}
    			event.setUser_list(newList2);
    			newList.add(event);
			}
    	}else {			
    		list = commissionEventService.queryCommissionEvent(userCommissionEvent);
    		for (UserCommissionEvent event : list) {
    			List<String> user_ids = event.getUser_ids();
    			List newList3 = new ArrayList<>();
    			for (String userId : user_ids) {
    				Map hashMap2 = new HashMap<>();
    				User user = new User();
					user.setId(userId);
					List<Map> queryUserCoreData = userService.queryUserCoreData(user);
					if (CommonUtils.isNullOrEmpty(queryUserCoreData)) {
						logger.error("查询用户不存在, 用户id为: " + userId);
						continue;
					}
					hashMap2.put(Dict.ID, queryUserCoreData.get(0).get(Dict.ID));
					hashMap2.put(Dict.USER_NAME_CN, queryUserCoreData.get(0).get(Dict.USER_NAME_CN));
					hashMap2.put(Dict.USER_NAME_EN, queryUserCoreData.get(0).get(Dict.USER_NAME_EN));
					newList3.add(hashMap2);
				}
    			event.setUser_list(newList3);
    			newList.add(event);
			}
		}
    	Long total = commissionEventService.totalLabelNum(list2);
    	Map map = new HashMap<>();
    	map.put("eventList", newList);
    	map.put("total", total);
    	return JsonResultUtil.buildSuccess(map);
    }
    
    
    @ApiOperation(value = "更新代办事项标签,返回todo标签个数和done标签个数")
    @RequestMapping(value = "/updateLabelById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateLabelById(@RequestBody Map<String,String> requestParam,
    		HttpServletRequest request) throws Exception {
    	String id = requestParam.get("id");
    	UserCommissionEvent userCommissionEvent = commissionEventService.queryEventById(id);
    	//判断当前用户是否为代办负责人
    	String userNameEn = tokenManger.getUserByToken(request.getHeader(Dict.AUTHORIZATION));
        User loginUser = new User();
        loginUser.setUser_name_en(userNameEn);
        List<Map> result = userService.queryUser(loginUser);
        if(!userCommissionEvent.getUser_ids().contains(result.get(0).get(Dict.ID))) {
        	throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前用户不是待办事项负责人"});
        }
    	//点击todo按钮修改label
    	if("todo".equals(userCommissionEvent.getLabel())) {
    		UserCommissionEvent newEvent = new UserCommissionEvent();
    		newEvent.setId(id);
    		newEvent.setLabel("done");
    		UserCommissionEvent updateEvent = commissionEventService.updateLabelById(newEvent);
    	}
    	if("done".equals(userCommissionEvent.getLabel())) {
    		UserCommissionEvent newEvent = new UserCommissionEvent();
    		newEvent.setId(id);
    		newEvent.setLabel("todo");
    		UserCommissionEvent updateEvent = commissionEventService.updateLabelById(newEvent);
    	}
    	HashMap map = new HashMap<>();
    	Long countTodoNum = commissionEventService.countLabelNum(userCommissionEvent.getUser_ids(), "todo");
    	Long countDoneNum = commissionEventService.countLabelNum(userCommissionEvent.getUser_ids(), "done");
    	map.put("todoNum", countTodoNum);
    	map.put("doneNum", countDoneNum);
    	return JsonResultUtil.buildSuccess(map);
    }
    
    @ApiOperation(value = "根据target_id,type,module参数删除单个代办事项")
    @RequestMapping(value = "/deleteCommissionEvent", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteCommissionEvent(@RequestBody Map<String,String> requestParam) throws Exception {
    	String target_id = requestParam.get("target_id");
    	String type = requestParam.get("type");
    	String module = requestParam.get("module");
    	String transformString = this.TransformString(module);
    	UserCommissionEvent commissionEvent = commissionEventService.queryDetailBytargetIdAndType(target_id,type,transformString);
    	if(CommonUtils.isNullOrEmpty(commissionEvent)) {
    		return JsonResultUtil.buildSuccess();
    	}
    	Long num = commissionEventService.deleteCommissionEventById(commissionEvent.getId());
    	if (num < 0) {
    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"代办删除失败"});
		}
    	return JsonResultUtil.buildSuccess(num);
    }
    
    @ApiOperation(value = "批量删除1个月前状态为：已解决或DONE的代办项")
    @RequestMapping(value = "/batchDeleteCommissionEvent", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult batchDeleteCommissionEvent(@RequestBody Map<String,String> requestParam) throws Exception {
    	UserCommissionEvent userCommissionEvent = new UserCommissionEvent();
    	userCommissionEvent.setLabel("done");
    	userCommissionEvent.setStatus("1");
    	List<UserCommissionEvent> list = commissionEventService.queryListByStatusOrLabel(userCommissionEvent);
    	long currentTimeMillis = System.currentTimeMillis();
    	for (UserCommissionEvent userCommissionEvent2 : list) {
    		//操作完成, 根据执行时间进行删除
    		if ("1".equals(userCommissionEvent2.getStatus())) {
				String executeTime = userCommissionEvent2.getExecuteTime();
				SimpleDateFormat sFormat = new SimpleDateFormat(CommonUtils.DATE_PARSE);
				Date date = sFormat.parse(executeTime);
				if (currentTimeMillis - date.getTime() > 30 * 24 * 3600L * 1000) {
					long num = commissionEventService.deleteCommissionEventById(userCommissionEvent2.getId());
					if (num <= 0) {
			    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"代办删除失败"});
					}
				}
			}else if ("done".equals(userCommissionEvent2.getLabel()) 
					&& "0".equals(userCommissionEvent2.getStatus())) {
    			String createTime = userCommissionEvent2.getCreateTime();
				SimpleDateFormat sFormat = new SimpleDateFormat(CommonUtils.DATE_PARSE);
				Date date = sFormat.parse(createTime);
				if (currentTimeMillis - date.getTime() > 30 * 24 * 3600L * 1000) {
					long num = commissionEventService.deleteCommissionEventById(userCommissionEvent2.getId());
					if (num <= 0) {
			    		throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"代办删除失败"});
					}
				}
			}
		}
    	return JsonResultUtil.buildSuccess(new ArrayList());
    }
}
