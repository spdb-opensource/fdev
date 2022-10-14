package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class UserApiImpl implements IUserApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private RestTransport restTransport;

    @Override
//	@LazyInitProperty(redisKeyExpression = "ftask.username.{param.id}")
    public Map queryUser(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"id 或 用户英文名"});
        }
        param.put(Dict.REST_CODE, "queryUser");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->USER 查询用户返回为空"});
        }
        ArrayList jsonArray = (ArrayList) result;
        return (Map) jsonArray.get(0);
    }
//	map.put(Dict.REST_CODE, "queryTaskDetailByIds");
//	Map<String, Object> result = (Map<String, Object>) restTransport.submit(map);

    @Override
//	@LazyInitProperty(redisKeyExpression = "ftask.username.{param.id}")
    public List queryUserAll(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"id 或 用户英文名"});
        }
        param.put(Dict.REST_CODE, "queryUser");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->USER 查询用户返回为空"});
        }
        return (ArrayList) result;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public List queryUserList(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"id 或 用户英文名"});
        }
        param.put(Dict.REST_CODE, "queryByUserCoreData");
        LinkedHashMap result = (LinkedHashMap) restTransport.submit(param);
        ArrayList<Map> jsonArray = new ArrayList<Map>();
        List<String> ids = (List) param.get(Dict.IDS);
        for (String id : ids) {
            if (CommonUtils.isNullOrEmpty(result.get(id))) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"用户不存在"});
            }
            jsonArray.add((Map) result.get(id));
        }
        return jsonArray;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "ftask.username.{param.id}")
    public Map queryTaskDetailUser(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"id 或 用户英文名"});
        }
        param.put(Dict.REST_CODE, "queryUser");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            return new HashMap();
        }
        ArrayList jsonArray = (ArrayList) result;
        return (Map) jsonArray.get(0);
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "ftask.group.{param.id}")
    public Map queryGroup(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id "});
        }
        param.put(Dict.REST_CODE, "queryGroup");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->USER 查询小组返回为空"});
        }
        ArrayList jsonArray = (ArrayList) result;
        return (Map) jsonArray.get(0);
    }


    @Override
    public List queryAllGroup() throws Exception {
        Map param = new HashMap();
        param.put(Dict.REST_CODE, "queryGroup");
        Object result = restTransport.submit(param);
        return (List) result;
    }

    @Override
    public List queryAllGroupV2() {
        Map<String,String> param = new HashMap<>();
        param.put(Dict.REST_CODE, "queryGroup");
        param.put("status", "1");
        List<Map<String,String>> result;
        try {
            result = (List)restTransport.submit(param);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error("获取小组信息失败:{}",sw.toString());
            return Collections.EMPTY_LIST;
        }
        return  Optional.of(result).orElse(Collections.EMPTY_LIST);
    }

    /**
     * @param // user_ids:		代办负责人id集合(有权操作的人员)
     *           //    ​	module:			所属模块(如: release, task, env, interface) 枚举值 task
     *           //    ​	description:	代办描述(如: 任务归档待确认)
     *           //    ​	link:			代办相关链接(如: xxx/fdev/#/release/list/20190730_003/joblist)
     *           //    ​	type:			代办类型(如: task_archived)
     *           //    ​	target_id:		目标id(各模块事项唯一标识) 不一样的才会当成新数据
     *           //    ​	create_user_id:	创建人id(代办发起人)
     * @return
     * @throws Exception
     */
    @Override
    public Map addTodoList(Map param) {
        Object result = null;
        try {
            param.put(Dict.REST_CODE, "addCommissionEvent");
            result = restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id "});
        }
        return (Map) result;
    }

    /**
     * ​	target_id:		目标id(各模块事项唯一标识)
     * ​	type:			代办类型(如: task_archived)
     * ​	executor_id:    执行人id
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public Map updateTodoList(Map param) throws Exception {
        param.put(Dict.REST_CODE, "updateByTargetIdAndType");
        Object result = restTransport.submit(param);
        Objects.nonNull(result);
        return (Map) result;
    }

	/**
	 *
	 * @param groups 小组id集合
	 * @return
	 */
	@Override
	public List queryDevResource(List groups){
		List result;
    	Map param = new HashMap();
    	param.put("groupIds",groups);
    	param.put(Dict.REST_CODE,"queryDevResource");
		try {
			result = (List) restTransport.submit( param);
		} catch (ClassCastException e) {
			throw new FdevException(ErrorConstants.APP_THIRD_SERVER_ERROR,new String[]{"查询开发人员数数目返回值格式异常"});
		} catch (Exception e) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"组id "});
		}
		return result;
	}

	@Override
	public List queryRole(){
		List result;
		Map param = new HashMap();
		param.put(Dict.REST_CODE,"queryRole");
		try {
			result = (List) restTransport.submit( param);
		}  catch (Exception e) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"组id "});
		}
		return result;
	}

	@Override
	public List<Map> queryChildGroup(String id) throws Exception {
		Map param = new HashMap();
		param.put(Dict.REST_CODE, "queryChildGroupById");
		param.put(Dict.ID, id);
		List<Map> result = (List<Map>) restTransport.submit(param);
		return result;
	}

    @Override
    public List<Map> hasChild(String id) throws Exception {
        List<Map> groupInfo = queryChildGroup(id);
        List<Map> result = new ArrayList();
        if (!CommonUtils.isNullOrEmpty(groupInfo))
            result = groupInfo.stream().filter(n -> "1".equals(n.get(Dict.STATUS)) && !id.equals(n.get(Dict.ID))).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<Map> getJobUser() throws Exception {
        Map param = new HashMap();
        param.put(Dict.REST_CODE, "getJobUser");
        List<Map> users = (List<Map>) restTransport.submit(param);
        return users;
    }

    @Override
    public List<Map> queryParentGroupById(String id) throws Exception {
        Map param = new HashMap();
        param.put(Dict.REST_CODE, "queryParentGroupById");
        param.put(Dict.ID, id);
        List<Map> result = (List<Map>) restTransport.submit(param);
        return result;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "AllChildGroup.{groupId}")
    public List<String> getAllChildGroup(String groupId) throws Exception {
        List<String> result = new ArrayList<>();
        List<Map> groups = this.queryAllGroup();
        //递归获取所有子组
        getChild(groupId, groups, result);
        result.add(groupId);
        return result;
    }

    @Override
    public Object deleteTodoList(Map param) throws Exception {
        Object result = null;
        try {
            param.put(Dict.REST_CODE, "deleteCommissionEvent");
            result = restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id "});
        }
        return  result;
    }

    @Override
    public List<Map> queryGroupByIds(List<String> ids) throws Exception {
        Map param = new HashMap();
        param.put(Dict.REST_CODE, "queryGroupByIds");
        param.put(Dict.GROUP_IDS, ids);
        return (List<Map>) restTransport.submit(param);
    }

    /**
     * 批量获取小组中文名，并以map的形式存储
     * @param groupIds
     * @return
     */
    @Override
    public Map getGroupsNameByIds(Set<String> groupIds) throws Exception {
        Map resultMap = new HashMap();
        Map param = new HashMap();
        param.put(Dict.ALLFLAG,true);
        param.put(Dict.GROUPIDS, groupIds);
        param.put(Dict.REST_CODE,"queryGroupByIds");
        List<Map> groups = (List<Map>)restTransport.submit(param);
        for(Map group : groups){
            resultMap.put(group.get(Dict.ID),group.get(Dict.NAME));
        }
        return resultMap;
    }

    @Override
    public String getGroupNameById(String groupId) {
        Map<String,String> param = new HashMap();
        param.put(Dict.GROUPID, groupId);
        param.put(Dict.REST_CODE, "queryGroupNameById");
        try {
            return (String) restTransport.submit(param);
        } catch (Exception e) {
            return "";
        }
    }

    public void getChild(String groupId, List<Map> groups, List<String> result) {
        List<String> childResult = new ArrayList<>();
        for (Map info : groups) {
            if (groupId.equals(info.get("parent_id"))) {
                childResult.add((String) info.get(Dict.ID));
            }
        }
        if (childResult.size() > 0) {
            result.addAll(childResult);
            for (String subChild : childResult) {
                getChild(subChild, groups, result);
            }
        }
    }

    @Override
	@LazyInitProperty(redisKeyExpression = "ftask.username.{userName}")
    public Map queryUser(String userName) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put(Dict.USER_NAME_CN, userName);
        param.put(Dict.REST_CODE, "queryUser");
        List<Map> result = (List<Map>) restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            logger.info("未查询到用户信息{}",userName);
            return new HashMap();
        }
        return result.get(0);
    }

    @Override
    public Map<String, List> queryChildGroupByIds(List<String> group) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put(Dict.IDS, group);
        param.put(Dict.REST_CODE, "queryChildGroupByIds");
        return (Map<String, List>) restTransport.submit(param);
    }
}
