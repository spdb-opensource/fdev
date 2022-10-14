package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.google.common.collect.Maps;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.unit.DealDashBoardUnit;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GroupTreeUtils;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.TreeGroupNode;
import com.spdb.fdev.fdevtask.spdb.service.IDashBaordService;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IDashBaordServiceImpl implements IDashBaordService {

    @Autowired
    private DealDashBoardUnit dealDashBoardUnit;

    @Autowired
    private IFdevTaskDao iFdevTaskDao;

    @Autowired
    private IUserApi iUserApi;

    @Override
    public List<TreeGroupNode> statisticsTaskInfo(String groupId) throws Exception {
        List<TreeGroupNode> treeGroupNodes = dealDashBoardUnit.buildGroupNode(groupId);
        return treeGroupNodes;
    }

    @Override
    public Long getAllTaskNum() throws Exception {
        Criteria c = Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortList());
        Query query = new Query(c);
        return iFdevTaskDao.queryTaskNum(query);
    }

    @Override
    public Map getDelayTaskByGroup(String queryMonth) {
        if(!CommonUtils.isNotNullOrEmpty(queryMonth)){
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"缺少查询日期参数"});
        }
        List<Map<String, String>> list = iUserApi.queryAllGroupV2();
        List<TreeGroupNode> treeGroupNodeList = list.stream().map(map -> {
            TreeGroupNode groupListNode = new TreeGroupNode();
            groupListNode.setChildren(new ArrayList<>());
            groupListNode.setName(map.get("name"));
            groupListNode.setGroupId(map.get("id"));
            groupListNode.setParent(map.get("parent_id"));
            return groupListNode;
        }).collect(Collectors.toList());
        Map<String, String> result = Maps.newHashMap();
        List<TreeGroupNode> treeGroupNodes = GroupTreeUtils.buildTreeNodes(treeGroupNodeList);
        dealDashBoardUnit.buildResult(treeGroupNodes, result, queryMonth);
        return result;
    }
}
