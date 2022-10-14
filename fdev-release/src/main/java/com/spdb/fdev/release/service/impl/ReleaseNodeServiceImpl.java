package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IReleaseNodeDao;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 投产窗口业务层 2019年3月26日
 */
@Service
public class ReleaseNodeServiceImpl implements IReleaseNodeService {

	@Resource
	private IReleaseNodeDao releaseNodeDao;
	@Resource
	private IUserService userService;
	@Autowired
	ISendEmailService SendEmailService;
	@Autowired
	private IReleaseApplicationService releaseApplicationService;
	@Autowired
	private IReleaseTaskService releaseTaskService;
	@Autowired
	private IProdRecordService prodRecordService;
	@Autowired
	private IRelDevopsRecordService relDevopsRecordService;
	@Autowired
	private IGitlabService gitlabService;
	@Autowired
	private IProdAssetService prodAssetService;
	@Autowired
	private IAuditRecordService auditRecordService;
	@Autowired
	private IReleaseRqrmntService releaseRqrmntService;
	@Autowired
	private IProdNoteService prodNoteService;

	private static Logger logger = LoggerFactory.getLogger(ReleaseNodeServiceImpl.class);

	@Override
	public List<ReleaseNode> getReleaseNodeName(String release_date) throws Exception {
		return releaseNodeDao.getReleaseNodeName(release_date);
	}

	@Override
	public ReleaseNode delete(ReleaseNode releaseNode) throws Exception {
		return releaseNodeDao.delete(releaseNode);
	}

	@Override
	public List<ReleaseNode> threeDaysAgoNode() throws Exception {
		return releaseNodeDao.threeDaysAgoNode();
	}

	@Override
	public ReleaseNode update(Map<String, Object> map) throws Exception {
		ReleaseNode old_node = null;
		String old_release_node_name = (String) map.get(Dict.OLD_RELEASE_NODE_NAME);
		if (!CommonUtils.isNullOrEmpty(old_release_node_name)) {
			old_node = releaseNodeDao.queryDetail(old_release_node_name);
		} else {
			old_node = releaseNodeDao.queryDetail(Dict.RELEASE_NODE_NAME);
		}
		ReleaseNode releaseNode = releaseNodeDao.update(map);
		try {// 邮件发送通知该组及子组的任务负责人
			HashMap model = (HashMap) updateNodeChange(old_node, releaseNode);
			List<Map<String, Object>> groups = userService.queryChildGroupByGroupId(releaseNode.getOwner_groupId());
			List user_list = new ArrayList();
			for (Map<String, Object> group : groups) {// 查询该组所有的任务负责人
				List<Map<String, Object>> dev_managers = userService.queryAllGroupManagers((String) group.get(Dict.ID),
						Constants.ROLE_CSII);
				List<Map<String, Object>> spdb_managers = userService.queryAllGroupManagers((String) group.get(Dict.ID),
						Constants.ROLE_SPDB);
				user_list.addAll(dev_managers);
				user_list.addAll(spdb_managers);
			}
			model.put(Dict.USER, user_list);
			SendEmailService.sendEmailUpdateNode(model);
		} catch (Exception e) {
			logger.error("update node send email error");
		}
		// 若投产窗口名修改 则要一同修改投产应 投产任务 变更记录中的投产窗口名
		if (!CommonUtils.isNullOrEmpty(old_release_node_name)) {
			releaseApplicationService.updateReleaseNodeName(old_release_node_name,releaseNode.getRelease_node_name());
			releaseTaskService.updateReleaseNodeName(old_release_node_name,releaseNode.getRelease_node_name());
			prodRecordService.updateReleaseNodeName(old_release_node_name,releaseNode.getRelease_node_name());
			prodAssetService.updateNode(old_release_node_name, releaseNode.getRelease_node_name());
			relDevopsRecordService.updateNode(old_release_node_name, releaseNode.getRelease_node_name());
			auditRecordService.updateNode(old_release_node_name, releaseNode.getRelease_node_name());
			releaseRqrmntService.updateNode(old_release_node_name, releaseNode.getRelease_node_name());
			prodNoteService.updateProdNoteReleaseNodeName(old_release_node_name, releaseNode.getRelease_node_name());
			prodNoteService.updateNoteServiceReleaseNodeName(old_release_node_name, releaseNode.getRelease_node_name());
			prodNoteService.updateNoteConfigurationReleaseNodeName(old_release_node_name, releaseNode.getRelease_node_name());
			prodNoteService.updateNoteSqlReleaseNodeName(old_release_node_name, releaseNode.getRelease_node_name());
			prodNoteService.updateNoteBatchReleaseNodeName(old_release_node_name, releaseNode.getRelease_node_name());
		}
		return releaseNode;
	}

	@Override
	public List<ReleaseNode> queryReleaseNodes(Map<String, String> map) throws Exception {
		return releaseNodeDao.queryReleaseNodes(map);
	}

	@Override
	public ReleaseNode queryDetail(String release_node_name) throws Exception {
		ReleaseNode releaseNode = releaseNodeDao.queryDetail(release_node_name);
		return completeNodeUserNameCn(releaseNode);
	}

	@Override
	public ReleaseNode create(ReleaseNode releaseNode) throws Exception {
		releaseNode.setNode_status(Constants.NODE_CREATED);// 将node_status状态设为1
		releaseNode = releaseNodeDao.create(releaseNode);
		return releaseNode;
	}

	@Override
	public List<ReleaseNode> queryReleaseNodesByGroupId(String group_id) throws Exception {
		List<Map<String, Object>> groups = userService.queryParentGroupByGroupId(group_id);
		return releaseNodeDao.queryReleaseNodesByGroupId(groups);
	}

	@Override
	public Map updateNodeChange(ReleaseNode the_old, ReleaseNode the_new) throws Exception {
		HashMap<String, Object> model = new HashMap<>();
		model.put(Dict.RELEASE_NODE_NAME, the_new.getRelease_node_name());
		model.put(Dict.RELEASE_DATE, the_new.getRelease_date());

		Map<String, Object> release_manager = userService.queryUserInfo(the_old.getRelease_manager());
		if (!the_old.getRelease_manager().equals(the_new.getRelease_manager())) {
			Map<String, Object> release_manager_new = userService.queryUserInfo(the_new.getRelease_manager());

			if (CommonUtils.isNullOrEmpty(release_manager) || CommonUtils.isNullOrEmpty(release_manager_new)) {
				model.put(Dict.RELEASE_MANAGER, the_old.getRelease_manager() + " --> " + the_new.getRelease_manager());
			} else {
				model.put(Dict.RELEASE_MANAGER,
						release_manager.get(Dict.USER_NAME_CN) + " --> " + release_manager_new.get(Dict.USER_NAME_CN));
			}
		} else {
			if (CommonUtils.isNullOrEmpty(release_manager)) {
				model.put(Dict.RELEASE_MANAGER, the_old.getRelease_manager());
			} else {
				model.put(Dict.RELEASE_MANAGER, release_manager.get(Dict.USER_NAME_CN));
			}
		}
		Map<String, Object> release_spdb_manager = userService.queryUserInfo(the_old.getRelease_spdb_manager());
		if (!the_old.getRelease_spdb_manager().equals(the_new.getRelease_spdb_manager())) {
			Map<String, Object> release_spdb_manager_new = userService.queryUserInfo(the_new.getRelease_spdb_manager());
			if (CommonUtils.isNullOrEmpty(release_spdb_manager)
					|| CommonUtils.isNullOrEmpty(release_spdb_manager_new)) {
				model.put(Dict.RELEASE_SPDB_MANAGER,
						the_old.getRelease_spdb_manager() + " --> " + the_new.getRelease_spdb_manager());
			} else {
				model.put(Dict.RELEASE_SPDB_MANAGER, release_spdb_manager.get(Dict.USER_NAME_CN) + " --> "
						+ release_spdb_manager_new.get(Dict.USER_NAME_CN));
			}
		} else {
			if (CommonUtils.isNullOrEmpty(release_spdb_manager)) {
				model.put(Dict.RELEASE_SPDB_MANAGER, the_old.getRelease_spdb_manager());
			} else {
				model.put(Dict.RELEASE_SPDB_MANAGER, release_spdb_manager.get(Dict.USER_NAME_CN));
			}
		}

		if (!the_old.getRelease_node_name().equals(the_new.getRelease_node_name())) {
			model.put(Dict.RELEASE_NODE_NAME,
					the_old.getRelease_node_name() + " --> " + the_new.getRelease_node_name());
		} else {
			model.put(Dict.RELEASE_NODE_NAME, the_old.getRelease_node_name());
		}
		String owner_group_name = userService.queryGroupNameById(the_new.getOwner_groupId());
		model.put(Dict.GROUP, owner_group_name);
		return model;
	}

	@Override
	public ReleaseNode completeNodeUserNameCn(ReleaseNode releaseNode) throws Exception {
		if (!CommonUtils.isNullOrEmpty(releaseNode)) {
			Map<String, Object> create_user = userService.queryUserById(releaseNode.getCreate_user());
			if (!CommonUtils.isNullOrEmpty(create_user)) {
				releaseNode.setCreate_user_name_cn((String) create_user.get(Dict.USER_NAME_CN));
				releaseNode.setCreate_user_name_en((String) create_user.get(Dict.USER_NAME_EN));
			} else {
				releaseNode.setCreate_user_name_cn("");
				releaseNode.setCreate_user_name_en("");
			}
			Map<String, Object> release_manager = userService.queryUserInfo(releaseNode.getRelease_manager());
			if (!CommonUtils.isNullOrEmpty(release_manager)) {
				releaseNode.setRelease_manager_id((String) release_manager.get(Dict.ID));
				releaseNode.setRelease_manager_name_cn((String) release_manager.get(Dict.USER_NAME_CN));
			} else {
				releaseNode.setRelease_manager_id("");
				releaseNode.setRelease_manager_name_cn(releaseNode.getRelease_manager());
			}

			Map<String, Object> release_spdb_manager = userService.queryUserInfo(releaseNode.getRelease_spdb_manager());
			if (!CommonUtils.isNullOrEmpty(release_spdb_manager)) {
				releaseNode.setRelease_spdb_manager_id((String) release_spdb_manager.get(Dict.ID));
				releaseNode.setRelease_spdb_manager_name_cn((String) release_spdb_manager.get(Dict.USER_NAME_CN));
			} else {
				releaseNode.setRelease_spdb_manager_id("");
				releaseNode.setRelease_spdb_manager_name_cn(releaseNode.getRelease_spdb_manager());
			}
		}
		return releaseNode;
	}

	@Override
	public List<String> archivedNodeByDate(String release_date) throws Exception {
		return releaseNodeDao.archivedNodeByDate(release_date);
	}

	@Override
	public List<ReleaseNode> updateReleaseNodeBatch(String start_date, String end_date) throws Exception {
		return releaseNodeDao.updateReleaseNodeBatch(start_date, end_date);
	}

	@Override
	public ReleaseNode updateBigReleaseNode(ReleaseNode releaseNode, String releaseDate) {
		return releaseNodeDao.updateBigReleaseNode(releaseNode,releaseDate.replace("-",""));
	}

	@Override
	public List<Map<String, Object>> queryContactInfo(Map<String, String> requestParam) throws Exception {
		String releaseDate = requestParam.get(Dict.RELEASE_DATE);
		//查询符合要求的投产窗口
		List<ReleaseNode> releaseNodeList=releaseNodeDao.queryReleaseNodesByCondition(releaseDate);
		List<Map<String,Object>> result=new ArrayList<>();
		Map<String,Map<String,Object>> contactMap=new HashMap<>();
		for (ReleaseNode releaseNode : releaseNodeList) {
			Map<String,Object> map=new HashMap<>();
			String ownerGroupName = userService.queryGroupNameById(releaseNode.getOwner_groupId());
            map.put(Dict.OWNER_GROUP_NAME,ownerGroupName);
			String releaseSpdbManager = releaseNode.getRelease_spdb_manager();
			Map<String, Object> releaseSpdbManagerInfo = userService.queryUserInfo(releaseSpdbManager);
			Map<String,String> releaseSpdbManagerMap=new HashMap<>();
			releaseSpdbManagerMap.put(Dict.ID, (String) releaseSpdbManagerInfo.get(Dict.ID));
			releaseSpdbManagerMap.put(Dict.USER_NAME_EN, (String) releaseSpdbManagerInfo.get(Dict.USER_NAME_EN));
			releaseSpdbManagerMap.put(Dict.USER_NAME_CN, (String) releaseSpdbManagerInfo.get(Dict.USER_NAME_CN));
			releaseSpdbManagerMap.put(Dict.TELEPHONE, (String) releaseSpdbManagerInfo.get(Dict.TELEPHONE));
			map.put(Dict.RELEASE_SPDB_MANAGER,releaseSpdbManagerMap);
			String releaseManager = releaseNode.getRelease_manager();
			Map<String, Object> releaseManagerInfo = userService.queryUserInfo(releaseManager);
			Map<String,String> releaseManagerMap=new HashMap<>();
			releaseManagerMap.put(Dict.ID, (String) releaseManagerInfo.get(Dict.ID));
			releaseManagerMap.put(Dict.USER_NAME_EN, (String) releaseManagerInfo.get(Dict.USER_NAME_EN));
			releaseManagerMap.put(Dict.USER_NAME_CN, (String) releaseManagerInfo.get(Dict.USER_NAME_CN));
			releaseManagerMap.put(Dict.TELEPHONE, (String) releaseManagerInfo.get(Dict.TELEPHONE));
			map.put(Dict.RELEASE_MANAGER,releaseManagerMap);
			contactMap.put(ownerGroupName,map);
		}
		result.addAll(contactMap.values());
		return result;
	}

    @Override
    public List<String> getReleaseSmallNodeNames(String releaseDate) {
        //查询符合要求的投产窗口
        List<ReleaseNode> releaseNodeList=releaseNodeDao.queryReleaseNodesByCondition(releaseDate);
        return releaseNodeList.stream().map(ReleaseNode::getRelease_node_name).collect(Collectors.toList());
    }

    @Override
    public ReleaseNode editReleaseDate(String oldReleaseDate, String releaseDate) throws Exception {
        String releaseNodeName=oldReleaseDate.replace("-","");
        String newReleaseNodeName=releaseDate.replace("-","");
        User user = CommonUtils.getSessionUser();
        String usernameEn = user.getUser_name_en();
        ReleaseNode releaseNode = releaseNodeDao.queryDetail(releaseNodeName);
        if(CommonUtils.isNullOrEmpty(releaseNode)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"待修改的投产大窗口不存在"});
        }
        List<String> releaseContact = releaseNode.getRelease_contact();
        //判断当前用户是否为投产牵头人
        if(!releaseContact.contains(usernameEn)){
            throw new FdevException(ErrorConstants.ROLE_ERROR,new String[]{"用户不是牵头联系人，没有修改权限"});
        }
        //判断用户是否修改了投产日期
        if(releaseDate.equals(oldReleaseDate)){
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR,new String[]{"投产周期未修改"});
        }
        ReleaseNode node = releaseNodeDao.queryDetail(newReleaseNodeName);
        if(!CommonUtils.isNullOrEmpty(node)){
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
        releaseNode.setRelease_date(releaseDate);
        releaseNode.setRelease_node_name(newReleaseNodeName);
        releaseNode.setUpdate_time(CommonUtils.formatDate("yyyy-MM-dd"));
        releaseNode = releaseNodeDao.updateBigReleaseNode(releaseNode, releaseNodeName);
        return releaseNode;
    }

	@Override
	public List<ReleaseNode> queryReleaseNodesByGroupIdAndDate(String groupId, String release_date) throws Exception {
		return releaseNodeDao.queryReleaseNodesByGroupIdAndDate(groupId, release_date);
	}

	@Override
	public List<ReleaseNode> queryFromDate(String date) {
		return releaseNodeDao.queryFromDate(date);
	}

    @Override
    public String queryDateByName(String release_node_name) {
        return releaseNodeDao.queryDateByName(release_node_name);
    }

	@Override
	public String queryNodes(String application_id, String startDate) {
		return releaseNodeDao.queryNodes(application_id, startDate);
	}

	@Override
	public Map<String, Object> queryThreeLevelGroups(Map<String, String> req) throws Exception {
		// 通过用户英文名获取用户详情
		Map<String,Object> returnMap = new HashMap<>();
		User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getSession().getAttribute(Dict._USER);
		// 获取当前用户的第三层级组信息
		Map<String,Object> threeLevelGroupMap = userService.getThreeLevelGroup(user.getGroup_id());
		String threeLevelId = (String) threeLevelGroupMap.get(Dict.ID);
		// 获取第三层级组及子组集合
        List<Map<String, Object>> threeChildGroups = userService.queryChildGroupByGroupId(threeLevelId);
        List<Map<String,Object>> threeLevelGroupList = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        for(Map<String,Object> child: threeChildGroups){
			groupIds.add((String) child.get(Dict.ID));
            Map<String,Object> map = new HashMap<>();
            map.put(Dict.OWNER_GROUPID, child.get(Dict.ID));
            map.put(Dict.OWNER_GROUP_NAME, child.get(Dict.NAME));
            map.put("level",child.get("level"));
			threeLevelGroupList.add(map);
        }
		returnMap.put(Dict.USER_ID,user.getId());
        returnMap.put(Dict.GROUPS,threeLevelGroupList);
        return returnMap;
	}


}
