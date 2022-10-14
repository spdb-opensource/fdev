package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.release.service.impl.ProdRecordServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "投产管理接口")
@RequestMapping("/api/releasenode")
@RestController
public class ReleaseNodeController {

	@Autowired
	IReleaseNodeService releaseNodeService;
	@Autowired
	IUserService userService;
	@Autowired
	IRoleService roleService;
	@Autowired
	IReleaseTaskService releaseTaskService;
	@Autowired
	ITaskService taskService;
	@Autowired
	IReleaseCycleService releaseCycleService;
	@Autowired
	IReleaseRqrmntInfoService releaseRqrmntInfoService;
	@Autowired
	ProdRecordServiceImpl prodRecordService;

	private static Logger logger = LoggerFactory.getLogger(ReleaseTaskController.class);

	@PostMapping(value = "/getReleaseNodeName")
	public JsonResult getReleaseNodeName(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String release_date = requestParam.get(Dict.RELEASE_DATE);
		if (CommonUtils.isNullOrEmpty(release_date)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { Dict.RELEASE_DATE });
		}
		Date parsedDate;
		try {
			parsedDate = CommonUtils.parseDate(release_date, CommonUtils.DATE_PARSE);
		} catch (FdevException e) {
			throw new FdevException(ErrorConstants.DATE_FROMAT_ERROR);
		}
		String now = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
		Date date = CommonUtils.parseDate(now, CommonUtils.DATE_PARSE);
		if (parsedDate.getTime() < date.getTime()) {
			logger.error("release date must not be before than current date!");
			throw new FdevException(ErrorConstants.RELEASE_DATE_ERROR);
		}

		// 将传入时间与当前时间做比较 若早于当前时间则报错
		List<ReleaseNode> list = releaseNodeService.getReleaseNodeName(release_date);
		int total = 0;
		if (!CommonUtils.isNullOrEmpty(list)) {
			ReleaseNode tmpNode = list.get(0);
			String node_name = tmpNode.getRelease_node_name();
			total = Integer.parseInt(node_name.substring(node_name.lastIndexOf("_") + 1));
		}
		// 数据库查询投产日期为当前日期的投产点个数n，返回投产点名称为：{release_date}_{n+1}
		// ({release_date}格式： YYYYMMDD ;{n+1} 为三位数字，不足前补0，如：‘007’)
		String formattedDate = release_date.replace("-", "");
		String serialNo = String.format("%03d", total + 1);
		String result = formattedDate + "_" + serialNo;
		return JsonResultUtil.buildSuccess(result);
	}

	/**
	 * 删除投产窗口
	 * @param releaseNode
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/delete")
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME })
	public JsonResult delete(@RequestBody @ApiParam ReleaseNode releaseNode) throws Exception {
        String releaseNodeName = releaseNode.getRelease_node_name().replace("-", "");
        ReleaseNode node = releaseNodeService.queryDetail(releaseNodeName);
        if (CommonUtils.isNullOrEmpty(node)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "投产窗口不存在" });
        }
	    //如果有-，是大窗口
	    if(releaseNode.getRelease_node_name().contains("-")){
			if(!roleService.isReleaseContact(releaseNodeName)){
				logger.error("the user must be release contact !");
				throw new FdevException(ErrorConstants.ROLE_ERROR, new String[] { "操作人员必须为牵头联系人" });
			}
        }else{
			//仅卡点管理员和窗口对应小组投产管理员可删除
			if (!roleService.isGroupReleaseManager(node.getOwner_groupId())) {
				logger.error("the user must be group release manager!");
				throw new FdevException(ErrorConstants.ROLE_ERROR, new String[] { "操作人员必须为小组投产管理员" });
			}
			//查询投产任务，变更实体是否存在
			List<ReleaseTask> releaseTasks = releaseTaskService.queryNecessaryTask(releaseNodeName);
			if(!CommonUtils.isNullOrEmpty(releaseTasks)){
				//如果是废弃任务，不报错
				boolean isDiscard = true;
				for(ReleaseTask releaseTask : releaseTasks){
					Map taskInfo = taskService.queryTaskInfo(releaseTask.getTask_id());
					if(!Dict.DISCARD.equals(taskInfo.get(Dict.STAGE))){
						isDiscard = false;
					}
				}
				if(!isDiscard){
					logger.error("releaseNode has task");
					throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_DELETABLE, new String[]{"投产任务"});
				}
			}
			List proRecords = prodRecordService.query(releaseNode.getRelease_node_name());
			if(!CommonUtils.isNullOrEmpty(proRecords)){
				logger.error("releaseNode has proRecord");
				throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_DELETABLE, new String[]{"投产变更"});
			}
        }
		releaseNode = releaseNodeService.delete(node);
		//同时删除已拒绝的投产任务
		List<ReleaseTask> releaseTasksRefused = releaseTaskService.queryRefusedTask(releaseNodeName);
		for(ReleaseTask task : releaseTasksRefused){
			releaseTaskService.deleteTask(task.getTask_id(), null);
		}
		return JsonResultUtil.buildSuccess(releaseNode);
	}

	@OperateRecord(operateDiscribe="投产模块-编辑投产窗口")
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME })
	@PostMapping(value = "/update")
	public JsonResult update(@RequestBody @Validated @ApiParam Map<String, Object> map) throws Exception {
		String release_node_name = (String) map.get(Dict.RELEASE_NODE_NAME);
		String old_release_node_name = (String) map.get("old_release_node_name");
		ReleaseNode node;
		if(CommonUtils.isNullOrEmpty(old_release_node_name)) {
			node = releaseNodeService.queryDetail(release_node_name);
		} else {
			node = releaseNodeService.queryDetail(old_release_node_name);
		}
        List<ReleaseTask> list = releaseTaskService.queryByReleaseNodeName(node.getRelease_node_name());
        if(!CommonUtils.isNullOrEmpty(list) && !node.getType().equals(map.get(Dict.TYPE))) {
            throw new FdevException(ErrorConstants.CAN_NOT_UPDATE_RELEASE_NODE_TYPE, new String[]{node.getRelease_node_name()});
        }
		if (CommonUtils.isNullOrEmpty(node)) {// 先根据投产窗口名查询投产窗口对象 若不存在 则报错
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "所要修改的投产窗口不存在" });
		}
		// 当前操作人员operate_user（session中获取“_USER”）必须为该投产点所属小组的投产管理员
		if (!roleService.isGroupReleaseManager(node.getOwner_groupId())) {
			logger.error("the user must be group release manager");
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		ReleaseNode releaseNode = releaseNodeService.update(map);
		releaseNode = releaseNodeService.completeNodeUserNameCn(releaseNode);
		if(!CommonUtils.isNullOrEmpty(old_release_node_name)) {
			//更换投产窗口日期  则需求信息需要修改
			for (ReleaseTask releaseTask : list) {
				if(Constants.TASK_RLS_CONFIRMED.equals(releaseTask.getTask_status())){
					releaseRqrmntInfoService.updateRqrmntInfoReview(releaseTask.getTask_id());
				}
			}
		}
		return JsonResultUtil.buildSuccess(releaseNode);
	}

	@PostMapping(value = "/queryReleaseNodes")
	public JsonResult queryReleaseNodes(@RequestBody @ApiParam Map<String, String> map) throws Exception {
		String start_date = map.get(Dict.START_DATE);
		String end_date = map.get(Dict.END_DATE);
		String owner_goupid = map.get(Dict.OWNER_GROUPID);
		if(!CommonUtils.isNullOrEmpty(map.get(Dict.OWNER_GROUPID))) {
			// 查询当前组与子组
			List<Map<String, Object>> childGroups = userService.queryChildGroupByGroupId(map.get(Dict.OWNER_GROUPID));
			Set<String> groups = new HashSet<>();
			groups.add(map.get(Dict.OWNER_GROUPID));
			for(Map<String, Object> child : childGroups) {
				groups.add((String) child.get(Dict.ID));
			}
			String groupIds = String.join(",", groups);
			logger.info("查询所有组id：{}", groupIds);
			map.put(Dict.OWNER_GROUPID, groupIds);
		}
		// 根据投产窗口 参数 模糊查询投产窗口列表
		List<ReleaseNode> list = releaseNodeService.queryReleaseNodes(map);
		// 若查无数据，则查询该组的第三层级组及子组在查一次库
		if(CommonUtils.isNullOrEmpty(list) && !CommonUtils.isNullOrEmpty(owner_goupid)){
			Map<String,String> threeLvel_map = new HashMap<>();
			threeLvel_map.put(Dict.START_DATE,start_date);
			threeLvel_map.put(Dict.END_DATE,end_date);
			threeLvel_map.put(Dict.OWNER_GROUPID,owner_goupid);
			Map<String,Object> threeLvelGropInfo = userService.getThreeLevelGroup(threeLvel_map.get(Dict.OWNER_GROUPID));
			String three_level_id = (String) threeLvelGropInfo.get(Dict.ID);
			String status = (String) threeLvelGropInfo.get(Dict.STATUS);
			if(!"0".equals(status)){
				List<Map<String, Object>> threeChildGroups = userService.queryChildGroupByGroupId(three_level_id);
				Set<String> groups = new HashSet<>();
				groups.add(map.get(Dict.OWNER_GROUPID));
				for(Map<String, Object> child : threeChildGroups) {
					groups.add((String) child.get(Dict.ID));
				}
				String groupIds = String.join(",", groups);
				logger.info("查询所有组id：{}", groupIds);
				threeLvel_map.put(Dict.OWNER_GROUPID, groupIds);
			}
			// 根据投产窗口 参数 模糊查询投产窗口列表
			list = releaseNodeService.queryReleaseNodes(threeLvel_map);
		}
		List<Map<String, Object>> releaseNodeList = new ArrayList<Map<String,Object>>();
		for (ReleaseNode releaseNode : list) {
			String owner_group_name = userService.queryGroupNameById(releaseNode.getOwner_groupId());
			releaseNode = releaseNodeService.completeNodeUserNameCn(releaseNode);
			Map<String, Object> releaseNodeMap = new HashMap<String, Object>();
			releaseNodeMap.put(Dict.RELEASE_DATE, releaseNode.getRelease_date());
			releaseNodeMap.put(Dict.RELEASE_NODE_NAME, releaseNode.getRelease_node_name());
			releaseNodeMap.put(Dict.CREATE_USER, releaseNode.getCreate_user());
			releaseNodeMap.put(Dict.CREATE_USER_NAME_EN, releaseNode.getCreate_user_name_en());
			releaseNodeMap.put(Dict.CREATE_USER_NAME_CN, releaseNode.getCreate_user_name_cn());
			releaseNodeMap.put(Dict.CREATE_TIME, releaseNode.getCreate_time());
			releaseNodeMap.put(Dict.OWNER_GROUPID, releaseNode.getOwner_groupId());
			releaseNodeMap.put(Dict.OWNER_GROUP_NAME, owner_group_name);
			releaseNodeMap.put(Dict.RELEASE_MANAGER, releaseNode.getRelease_manager());
			releaseNodeMap.put(Dict.RELEASE_MANAGER_ID, releaseNode.getRelease_manager_id());
			releaseNodeMap.put(Dict.RELEASE_MANAGER_NAME_CN, releaseNode.getRelease_manager_name_cn());
			releaseNodeMap.put(Dict.RELEASE_SPDB_MANAGER, releaseNode.getRelease_spdb_manager());
			releaseNodeMap.put(Dict.RELEASE_SPDB_MANAGER_ID, releaseNode.getRelease_spdb_manager_id());
			releaseNodeMap.put(Dict.RELEASE_SPDB_MANAGER_NAME_CN, releaseNode.getRelease_spdb_manager_name_cn());
			releaseNodeMap.put(Dict.UAT_ENV_NAME, "1");
			releaseNodeMap.put(Dict.NODE_STATUS, releaseNode.getNode_status());
			releaseNodeMap.put(Dict.TYPE, releaseNode.getType());
			releaseNodeList.add(releaseNodeMap);
		}
		return JsonResultUtil.buildSuccess(releaseNodeList);
	}

	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME })
	@PostMapping(value = "/queryDetail")
	public JsonResult queryDetail(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		// 通过投产窗口名查询该投产窗口详情
		String relase_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
		ReleaseNode releaseNode = releaseNodeService.queryDetail(relase_node_name);
		String owner_group_name = userService.queryGroupNameById(releaseNode.getOwner_groupId());
		if(CommonUtils.isNullOrEmpty(releaseNode)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
		}
		Map map = CommonUtils.beanToMap(releaseNode);
		map.put(Dict.OWNER_GROUP_NAME, owner_group_name);
		boolean flag = roleService.isGroupReleaseManager(releaseNode.getOwner_groupId());
		map.put(Dict.CAN_OPERATION, flag);
		return JsonResultUtil.buildSuccess(map);
	}

	@OperateRecord(operateDiscribe="投产模块-新增投产窗口")
	@RequestValidate(NotEmptyFields = {Dict.OWNER_GROUP, Dict.RELEASE_DATE, Dict.RELEASE_NODE_NAME,
			Dict.RELEASE_MANAGER, Dict.RELEASE_SPDB_MANAGER, Dict.TYPE})
	@PostMapping(value = "/create")
	public JsonResult create(@RequestBody @Validated @ApiParam Map<String, Object> requestParam) throws Exception {
		Map<String,Object> owner_group = (Map<String, Object>) requestParam.get(Dict.OWNER_GROUP);// 所属小组Id
		String owner_groupId = (String) owner_group.get(Dict.OWNER_GROUPID);
		String release_manager = (String) requestParam.get(Dict.RELEASE_MANAGER);// 投产管理员唯一标识
		String type = (String) requestParam.get(Dict.TYPE);
		User user = CommonUtils.getSessionUser();
		// 权限控制添加窗口人员（create_user）和投产管理员（release_manager）必须为该投产小组（owner_groupId）的投产管理员
		if (!roleService.isGroupReleaseManager(owner_groupId)) {
			logger.error("the user must be group release manager");
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		String release_date = (String) requestParam.get(Dict.RELEASE_DATE);// 投产日期
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);// 投产窗口名称
		String release_spdb_manager = (String) requestParam.get(Dict.RELEASE_SPDB_MANAGER);// 投产行内管理员唯一标识
		Map<String, Object> groupDetail = userService.queryGroupDetail(owner_groupId);
		if (CommonUtils.isNullOrEmpty(groupDetail)) {
			logger.error("can't find the group info !@@@@@group_id={}", owner_groupId);
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "组信息不存在" });
		}
		ReleaseNode node = releaseNodeService.queryDetail(release_node_name);
		if (!CommonUtils.isNullOrEmpty(node)) {
			logger.error("release node already exist!");
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "投产窗口已存在" });
		}
		Date parsedDate = CommonUtils.parseDate(release_date, CommonUtils.DATE_PARSE);
		String now = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
		Date date = CommonUtils.parseDate(now, CommonUtils.DATE_PARSE);
		if (parsedDate.getTime() < date.getTime()) {
			logger.error("release_date must not be before than current date!");
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "投产时间必须大于当前时间" });
		} else if (!release_node_name.substring(0, 8).equals(release_date.replaceAll("-", ""))) {
			logger.error("release_date {} is not compatible with the release_node_name {}",release_date,release_node_name);
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "投产窗口名和投产时间必须一致" });
		}
		ReleaseNode releaseNode = new ReleaseNode();
		releaseNode.setCreate_user(user.getId());// 从session中获取当前用户id
		releaseNode.setRelease_date(release_date);
		releaseNode.setRelease_node_name(release_node_name);
		releaseNode.setRelease_manager(release_manager);
		releaseNode.setOwner_groupId(owner_groupId);
		releaseNode.setRelease_spdb_manager(release_spdb_manager);
		releaseNode.setType(type);
		releaseNode.setCreate_time(CommonUtils.formatDate("yyyy-MM-dd"));
		releaseNode = releaseNodeService.create(releaseNode);
		return JsonResultUtil.buildSuccess(releaseNode);
	}

	@RequestValidate(NotEmptyFields = { Dict.OWNER_GROUPID })
	@PostMapping(value = "/queryParentReleaseNodeByGroupId")
	public JsonResult queryParentReleaseNodeByGroupId(@RequestBody @ApiParam Map<String, String> requestParam)
			throws Exception {
		// 通过投产窗口名查询该投产窗口详情
		String group_id = requestParam.get(Dict.OWNER_GROUPID);
		List<ReleaseNode> releaseNodes=new ArrayList<>();
		List<ReleaseNode> groups = releaseNodeService.queryReleaseNodesByGroupId(group_id);
		for (ReleaseNode releaseNode : groups) {
			releaseNode=releaseNodeService.completeNodeUserNameCn(releaseNode);
			releaseNodes.add(releaseNode);
		}
		return JsonResultUtil.buildSuccess(releaseNodes);
	}
	
	
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_DATE,Dict.IGNORE_ARCHIVED})
	@PostMapping(value = "/archiveReleaseNodes")
	public JsonResult archiveReleaseNodes(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String release_date = requestParam.get(Dict.RELEASE_DATE);
		if(!CommonUtils.isArchivePermited(release_date)) {
			throw new FdevException(ErrorConstants.RELEASE_DATE_ERROR,new String [] {"归档日期过早"});
		}
		List<String> list = releaseNodeService.archivedNodeByDate(release_date);
		List<String> task_ids = releaseTaskService.releaseTaskByNodeName(list);
		for (String task_id : task_ids) {
			try {
				releaseTaskService.updateTaskProduction(task_id);
				taskService.updateTaskProduction(task_id);
			} catch (Exception e) {
				logger.error("task archived error,task_id = "+ task_id);
				continue;
			}
		}
		return JsonResultUtil.buildSuccess(list);
	}

	@RequestMapping(value = "/queryThreeDaysAgoNodes", method = RequestMethod.POST)
	public JsonResult queryThreeDaysAgoNodes() throws Exception {
		return JsonResultUtil.buildSuccess(releaseNodeService.threeDaysAgoNode());
	}

	@RequestValidate(NotEmptyFields = {Dict.OWNER_GROUPID, Dict.RELEASE_DATE, Dict.RELEASE_CONTACT})
	@PostMapping(value = "/createBigReleaseNode")
	public JsonResult createBigReleaseNode(@RequestBody @Validated @ApiParam Map<String, Object> requestParam) throws Exception {
		String ownerGroupId = (String) requestParam.get(Dict.OWNER_GROUPID);
		String releaseDate = (String) requestParam.get(Dict.RELEASE_DATE);
		List<String> releaseContact = (List<String>) requestParam.get(Dict.RELEASE_CONTACT);
		User user = CommonUtils.getSessionUser();
		// 权限控制添加窗口人员（create_user）和投产管理员（release_manager）必须为该投产小组（owner_groupId）的投产管理员
		if (!roleService.isGroupReleaseManager(ownerGroupId)) {
			logger.error("the user must be group release manager");
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		String releaseNodeName=releaseDate.replace("-","");
		//判断牵头小组是否存在
		Map<String, Object> groupDetail = userService.queryGroupDetail(ownerGroupId);
		if (CommonUtils.isNullOrEmpty(groupDetail)) {
			logger.error("can't find the group info !@@@@@group_id={}", ownerGroupId);
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "牵头组信息不存在" });
		}
		//判断大窗口是否已经存在
		ReleaseNode node = releaseNodeService.queryDetail(releaseNodeName);
		if (!CommonUtils.isNullOrEmpty(node)) {
			logger.error("release big node already exist!");
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "投产大窗口已存在" });
		}
		//判断投产日期是否小于当前日期
		Date parsedDate = CommonUtils.parseDate(releaseDate, CommonUtils.DATE_PARSE);
		String now = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
		Date date = CommonUtils.parseDate(now, CommonUtils.DATE_PARSE);
		if (parsedDate.getTime() < date.getTime()) {
			logger.error("release_date must not be before than current date!");
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "投产时间必须大于当前时间" });
		} else if (!releaseNodeName.equals(releaseDate.replaceAll("-", ""))) {
			logger.error("release_date {} is not compatible with the release_node_name {}",releaseDate,releaseNodeName);
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "投产窗口名和投产时间必须一致" });
		}
		ReleaseNode releaseNode = new ReleaseNode();
		releaseNode.setCreate_time(CommonUtils.formatDate(CommonUtils.DATE_PARSE));
		releaseNode.setCreate_user(user.getId());
		releaseNode.setRelease_date(releaseDate);
		releaseNode.setRelease_node_name(releaseNodeName);
		releaseNode.setOwner_groupId(ownerGroupId);
		releaseNode.setType(Constants.RELEASE_BIG_NODE_TYPE);
		releaseNode.setRelease_contact(releaseContact);
		releaseNode.setRelease_manager(releaseContact.get(0));
		releaseNode.setRelease_spdb_manager(releaseContact.get(0));
		releaseNode=releaseNodeService.create(releaseNode);
		//创建投产周期
		releaseCycleService.create(releaseDate);
		return JsonResultUtil.buildSuccess(releaseNode);
	}

	@RequestValidate(NotEmptyFields = { Dict.RELEASE_DATE,Dict.OLD_RELEASE_DATE,Dict.RELEASE_CONTACT,Dict.OWNER_GROUPID})
	@PostMapping(value = "/updateBigReleaseNode")
	public JsonResult updateBigReleaseNode(@RequestBody @Validated @ApiParam Map<String, Object> requestParam) throws Exception {
		User user = CommonUtils.getSessionUser();
		String oldReleaseDate = (String) requestParam.get(Dict.OLD_RELEASE_DATE);
		String releaseDate = (String) requestParam.get(Dict.RELEASE_DATE);
		List<String> releaseContact = (List<String>) requestParam.get(Dict.RELEASE_CONTACT);
		String ownerGroupId = (String) requestParam.get(Dict.OWNER_GROUPID);
		ReleaseNode node=releaseNodeService.queryDetail(oldReleaseDate.replace("-",""));
		if(!oldReleaseDate.equals(releaseDate)){
            ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseDate.replace("-", ""));
            if(!CommonUtils.isNullOrEmpty(releaseNode)){
                logger.error("release node already exist!");
                throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "投产大窗口已存在" });
            }
            //修改的投产日期必须大于当前时间
			Date parsedDate = CommonUtils.parseDate(releaseDate, CommonUtils.DATE_PARSE);
			String now = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
			Date date = CommonUtils.parseDate(now, CommonUtils.DATE_PARSE);
			if (parsedDate.getTime() < date.getTime()) {
				logger.error("release_date must not be before than current date!");
				throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "投产时间必须大于当前时间" });
			}
		}
		//判断当前用户是否属于牵头联系人
		List<String> release_contact = node.getRelease_contact();
		if(!release_contact.contains(user.getUser_name_en())){
			throw new FdevException(ErrorConstants.ROLE_ERROR,new String[]{"用户不是牵头联系人，没有修改权限"});
		}
		//修改牵头组信息
		if(!CommonUtils.isNullOrEmpty(ownerGroupId)){
			Map<String, Object> groupDetail = userService.queryGroupDetail(ownerGroupId);
			if (CommonUtils.isNullOrEmpty(groupDetail)) {
				logger.error("can't find the group info !@@@@@group_id={}", ownerGroupId);
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "牵头组信息不存在" });
			}
			node.setOwner_groupId(ownerGroupId);
//			node.setOwner_group_name((String) (groupDetail.get(Dict.NAME)));
		}
		// 权限控制添加窗口人员（create_user）和投产管理员（release_manager）必须为该投产小组（owner_groupId）的投产管理员
		if (!roleService.isGroupReleaseManager(ownerGroupId)) {
			logger.error("the user must be group release manager");
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		//修改牵头联系人
		if(!CommonUtils.isNullOrEmpty(releaseContact)){
			node.setRelease_contact(releaseContact);
		}
		node.setRelease_node_name(releaseDate.replace("-",""));
		node.setRelease_date(releaseDate);
		node.setUpdate_time(CommonUtils.formatDate("yyyy-MM-dd"));
		node=releaseNodeService.updateBigReleaseNode(node,oldReleaseDate);
		//修改投产周期
        if(!oldReleaseDate.equals(releaseDate)){
            releaseCycleService.edit(oldReleaseDate,releaseDate);
        }
		return JsonResultUtil.buildSuccess(node);
	}

	@RequestValidate(NotEmptyFields = { Dict.RELEASE_DATE })
	@PostMapping(value = "/queryContactInfo")
	public JsonResult queryContactInfo(@RequestBody @Validated @ApiParam Map<String, String> requestParam) throws Exception {
		List<Map<String,Object>> contactList=releaseNodeService.queryContactInfo(requestParam);
		return JsonResultUtil.buildSuccess(contactList);
	}

	@RequestValidate(NotEmptyFields = { Dict.RELEASE_DATE })
	@PostMapping(value = "/queryBigReleaseNodeDetail")
	public JsonResult queryBigReleaseNodeDetail(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String releaseDate = requestParam.get(Dict.RELEASE_DATE);
		String releaseNodeName = releaseDate.replace("-", "");
		ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseNodeName);
		if(CommonUtils.isNullOrEmpty(releaseNode)){
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
		}
		Map<String,Object> map=new HashMap<>();
		String owner_group_name = userService.queryGroupNameById(releaseNode.getOwner_groupId());
		map.put(Dict.RELEASE_DATE,releaseNode.getRelease_date());
		map.put(Dict.OWNER_GROUP_NAME,owner_group_name);
		map.put(Dict.OWNER_GROUPID,releaseNode.getOwner_groupId());
		List<String> releaseContact = releaseNode.getRelease_contact();
		List<Map<String,String>> releaseContactList=new ArrayList<>();
		for (String contact : releaseContact) {
			Map<String,String> contactMap=new HashMap<>();
			Map<String, Object> userDetail = userService.queryUserInfo(contact);
			contactMap.put(Dict.ID, (String) userDetail.get(Dict.ID));
			contactMap.put(Dict.USER_NAME_EN, (String) userDetail.get(Dict.USER_NAME_EN));
			contactMap.put(Dict.USER_NAME_CN, (String) userDetail.get(Dict.USER_NAME_CN));
			releaseContactList.add(contactMap);
		}
		map.put(Dict.RELEASE_CONTACT,releaseContactList);
		//查询投产大窗口下的投产小窗口
		List<String> releaseSmallNodeNames=releaseNodeService.getReleaseSmallNodeNames(releaseDate);
		map.put(Dict.RELEASE_SMALL_NODE_NAMES,releaseSmallNodeNames);
		return JsonResultUtil.buildSuccess(map);
	}

	@PostMapping(value = "/queryBigReleaseNodes")
	public JsonResult queryBigReleaseNodes(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String ownerGroupId = requestParam.get(Dict.OWNER_GROUPID);
		if(!CommonUtils.isNullOrEmpty(ownerGroupId)){
			List<Map<String, Object>> childGroups = userService.queryChildGroupByGroupId(ownerGroupId);
			Set<String> groups=new HashSet<>();
			groups.add(ownerGroupId);
			for(Map<String, Object> child : childGroups) {
				groups.add((String) child.get(Dict.ID));
			}
			String groupIds = String.join(",", groups);
			logger.info("查询所有组id：{}", groupIds);
			requestParam.put(Dict.OWNER_GROUPID, groupIds);
		}
		requestParam.put(Dict.TYPE,Constants.RELEASE_BIG_NODE_TYPE);
		List<ReleaseNode> list = releaseNodeService.queryReleaseNodes(requestParam);
		List<Map<String,Object>> result=new ArrayList<>();
		for (ReleaseNode releaseNode : list) {
			Map<String,Object> map=new HashMap<>();
			releaseNode = releaseNodeService.completeNodeUserNameCn(releaseNode);
			map.put(Dict.RELEASE_DATE,releaseNode.getRelease_date());
			List<String> releaseContact = releaseNode.getRelease_contact();
			List<Map<String,String>> releaseContactList=new ArrayList<>();
			for (String contact : releaseContact) {
				Map<String,String> contactMap=new HashMap<>();
				Map<String, Object> userDetail = userService.queryUserInfo(contact);
				contactMap.put(Dict.ID, (String) userDetail.get(Dict.ID));
				contactMap.put(Dict.USER_NAME_EN, (String) userDetail.get(Dict.USER_NAME_EN));
				contactMap.put(Dict.USER_NAME_CN, (String) userDetail.get(Dict.USER_NAME_CN));
				releaseContactList.add(contactMap);
			}
			map.put(Dict.RELEASE_CONTACT,releaseContactList);
			String owner_group_name = userService.queryGroupNameById(releaseNode.getOwner_groupId());
			map.put(Dict.OWNER_GROUP_NAME,owner_group_name);
			map.put(Dict.OWNER_GROUPID,releaseNode.getOwner_groupId());
			Map<String,String> createMap=new HashMap<>();
			createMap.put(Dict.ID,releaseNode.getCreate_user());
			createMap.put(Dict.USER_NAME_EN,releaseNode.getCreate_user_name_en());
			createMap.put(Dict.USER_NAME_CN,releaseNode.getCreate_user_name_cn());
            map.put(Dict.CREATER,createMap);
			result.add(map);
		}
		return JsonResultUtil.buildSuccess(result);
	}

	@PostMapping(value = "/queryThreeLevelGroups")
	public JsonResult queryThreeLevelGroups(@RequestBody @Validated @ApiParam Map<String, String> requestParam) throws Exception {
		Map<String,Object> result= releaseNodeService.queryThreeLevelGroups(requestParam);
		return JsonResultUtil.buildSuccess(result);
	}
}
