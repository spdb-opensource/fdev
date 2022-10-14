package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.google.common.collect.Maps;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.unit.TaskDealDashBoardUnit;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GroupTreeUtils;
import com.spdb.fdev.fdevtask.spdb.dao.TaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.TreeGroupNode;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import com.spdb.fdev.fdevtask.spdb.service.TaskDashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-04-06 10:17
 **/
@Service
@RefreshScope
public class TaskDashBoardServiceImpl implements TaskDashBoardService {

    @Autowired
    private TaskDealDashBoardUnit taskDealDashBoardUnit;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private IUserApi iUserApi;

    @Override
    public List<TreeGroupNode> statisticsTaskInfo(String groupId) throws Exception {
        List<TreeGroupNode> treeGroupNodes = taskDealDashBoardUnit.buildGroupNode(groupId);
        return treeGroupNodes;
    }

    @Override
    public Long getAllTaskNum() throws Exception {
        return taskDao.queryTaskNum();
    }

    @Override
    public Map getDelayTaskByGroup(Map request) {
        Object queryMonthObject = request.get("queryMonth");
        if(CommonUtils.isNullOrEmpty(queryMonthObject)){
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"缺少查询日期参数"});
        }
        List<Map> list = CommonUtils.castList(iUserApi.queryAllGroupV2(),Map.class);
        List<TreeGroupNode> treeGroupNodeList = list.stream().map(map -> {
            TreeGroupNode groupListNode = new TreeGroupNode();
            groupListNode.setChildren(new ArrayList<>());
            groupListNode.setName((String)map.get("name"));
            groupListNode.setGroupId((String)map.get("id"));
            groupListNode.setParent((String)map.get("parent_id"));
            return groupListNode;
        }).collect(Collectors.toList());
        Map<String, String> result = Maps.newHashMap();
        List<TreeGroupNode> treeGroupNodes = GroupTreeUtils.buildTreeNodes(treeGroupNodeList);
        taskDealDashBoardUnit.buildResult(treeGroupNodes, result, (String)queryMonthObject);
        return result;
    }
}