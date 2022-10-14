package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.TreeGroupNode;

import java.util.List;
import java.util.Map;

public interface TaskDashBoardService {

    List<TreeGroupNode> statisticsTaskInfo(String groupId) throws Exception;

    Long getAllTaskNum() throws Exception;

    Map getDelayTaskByGroup(Map request) throws Exception;
}
