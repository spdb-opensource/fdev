package com.spdb.fdev.fdevtask.base.unit;


import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GroupTreeUtils;
import com.spdb.fdev.fdevtask.base.utils.Tuple;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.TopFiveClass;
import com.spdb.fdev.fdevtask.spdb.entity.TreeGroupNode;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DealDashBoardUnit {

    @Autowired
    private IUserApi iUserApi;
    @Autowired
    private IFdevTaskDao iFdevTaskDao;

    public List<TreeGroupNode> buildGroupNode(String groupId) {
        List<Map<String, String>> list = iUserApi.queryAllGroupV2();
        //转换
        List<TreeGroupNode> collect1 = list.stream().map(map -> {
            TreeGroupNode groupListNode = new TreeGroupNode();
            groupListNode.setChildren(new ArrayList<>());
            groupListNode.setName(map.get("name"));
            groupListNode.setGroupId(map.get("id"));
            groupListNode.setParent(map.get("parent_id"));
            return groupListNode;
        }).collect(Collectors.toList());
        List<TreeGroupNode> treeGroupNodes = GroupTreeUtils.buildTreeNodes(collect1);
        //获取部分节点
        if (CommonUtils.isNotNullOrEmpty(groupId) && !"null".equals(groupId)) {
            TreeGroupNode treeGroupNode = new TreeGroupNode();
            treeGroupNode.setGroupId(groupId);
            GroupTreeUtils.getPartChild(treeGroupNodes, treeGroupNode);
            treeGroupNodes = Arrays.asList(treeGroupNode);
        }
        //计算每个节点的任务总数
        count(treeGroupNodes);
        return treeGroupNodes;
    }

    public void count(List<TreeGroupNode> treeGroupNodes) {
        treeGroupNodes.forEach(groupNode -> {
            List<TreeGroupNode> children = groupNode.getChildren();
            if (children.size() > 0) {
                List<TreeGroupNode> nodes = GroupTreeUtils.getNodes(groupNode);
                List<String> collect = nodes.stream().map(TreeGroupNode::getGroupId).collect(Collectors.toList());
//                //大组都能建任务
//                collect.add(groupNode.getGroupId());
                Tuple.Tuple4 tuple4 = queryTaskNumByGroupId(collect);
                groupNode.setTaskNum(Long.valueOf(tuple4.first().toString()));
                groupNode.setListStage((Map) tuple4.second());
                groupNode.setDelayCycle((Map) tuple4.third());
                groupNode.setTopFive((List) tuple4.fourth());
                count(children);
            } else {
                Tuple.Tuple4 tuple4 = queryTaskNumByGroupId(Arrays.asList(groupNode.getGroupId()));
                groupNode.setTaskNum(Long.valueOf(tuple4.first().toString()));
                groupNode.setListStage((Map) tuple4.second());
                groupNode.setDelayCycle((Map) tuple4.third());
                groupNode.setTopFive((List) tuple4.fourth());
            }

        });
    }

    /**
     * 根据小组id查询任务数量
     *
     * @param groupId 组ids
     * @return
     */
    private Tuple.Tuple4 queryTaskNumByGroupId(List groupId) {
        List<FdevTask> fdevTasks = iFdevTaskDao.queryByGroupId(groupId);
        Integer taskNum = fdevTasks.size();
        Map<String, Long> stageCount = fdevTasks.stream().filter(task -> !CommonUtils.isNullOrEmpty(task.getStage())).collect(Collectors.groupingBy(FdevTask::getStage, Collectors.counting()));
        Tuple.Tuple2 tuple2 = buildDelayCycle(fdevTasks);
        return Tuple.tuple(taskNum, stageCount, tuple2.first(), tuple2.second());
    }

    /**
     * 计算传入任务的延期天数
     *
     * @param fdevTasks
     * @return
     */
    public Tuple.Tuple2 buildDelayCycle(List<FdevTask> fdevTasks) {
        Map<String, Long> delayCycleMap = new HashMap<>();
        long oneMonth;
        long threeMonth;
        long sixMonth;
        long oneYear;
        long overYear;
        //获取延期投产的
        List<TopFiveClass> collect = fdevTasks.stream().filter(task -> "production".equals(task.getStage()) || "file".equals(task.getStage()))
                .map(task -> {
                    TopFiveClass topFiveClass = new TopFiveClass();
                    //转换top5对象
                    String delayDay = CommonUtils.dateCompare(task.getPlan_fire_time(), task.getFire_time());
                    topFiveClass.setTaskName(task.getName());
                    topFiveClass.setTaskId(task.getId());
                    topFiveClass.setDelayDay("".equals(delayDay) ? 0 : Integer.valueOf(delayDay));
                    return topFiveClass;
                }).filter(topFiveClass -> topFiveClass.getDelayDay() != 0)
                .collect(Collectors.toList());

        //计算top5
        List<TopFiveClass> top5 = collect.stream()
                .sorted(Comparator.comparingInt(TopFiveClass::getDelayDay).reversed())
                .limit(5L).collect(Collectors.toList());
        //计算延期时间，展示一天、一周、一月、三月、三月以上
        oneMonth = collect.stream()
                .filter(delay -> Integer.valueOf(delay.getDelayDay()) > 7 && Integer.valueOf(delay.getDelayDay()) <= 30).count();
        threeMonth = collect.stream()
                .filter(delay -> Integer.valueOf(delay.getDelayDay()) > 30 && Integer.valueOf(delay.getDelayDay()) <= 90).count();
        sixMonth = collect.stream()
                .filter(delay -> Integer.valueOf(delay.getDelayDay()) > 90 && Integer.valueOf(delay.getDelayDay()) <= 180).count();
        oneYear = collect.stream()
                .filter(delay -> Integer.valueOf(delay.getDelayDay()) > 180 && Integer.valueOf(delay.getDelayDay()) <= 360).count();
        overYear = collect.stream()
                .filter(delay -> Integer.valueOf(delay.getDelayDay()) > 360).count();
        delayCycleMap.put("oneMonth", oneMonth);
        delayCycleMap.put("threeMonth", threeMonth);
        delayCycleMap.put("sixMonth", sixMonth);
        delayCycleMap.put("oneYear", oneYear);
        delayCycleMap.put("overYear", overYear);
        return Tuple.tuple(delayCycleMap, top5);
    }

    public void buildResult(List<TreeGroupNode> treeGroupNodes, Map result, String queryMonth) {
        treeGroupNodes.forEach(groupNode -> {
            List<TreeGroupNode> children = groupNode.getChildren();
            if (children.size() > 0) {
                List<TreeGroupNode> nodes = GroupTreeUtils.getNodes(groupNode);
                List<String> collect = nodes.stream().map(TreeGroupNode::getGroupId).collect(Collectors.toList());
                String delayRate = getDelayRate(collect,queryMonth);
                result.put(groupNode.getGroupId(), delayRate);
                buildResult(children, result, queryMonth);
            } else {
                String delayRate = getDelayRate(Arrays.asList(groupNode.getGroupId()), queryMonth);
                result.put(groupNode.getGroupId(), delayRate);
            }
        });
    }

    public String getDelayRate(List groupIds, String queryMonth) {
        LocalDate parse = LocalDate.parse(queryMonth);
        parse = parse.plusMonths(1);
        queryMonth = CommonUtils.dateParse(queryMonth);
        Criteria c = Criteria.where(Dict.GROUP).in(groupIds).and(Dict.STAGE).in(CommonUtils.getNoAbortList());
        c.and("plan_fire_time").gte(queryMonth).lt(CommonUtils.dateParse(parse.toString()));
        Query query = new Query(c);
        query.fields().include("id").include("stage").include("plan_fire_time").include("plan_start_time").include("fire_time").include("name");
        List<FdevTask> fdevTasks = iFdevTaskDao.queryByQuery(query, FdevTask.class);
        if (fdevTasks.size() == 0)
            return "0.0";
        //计算任务周期总和 任务周期（计划开始时间-计划投产时间）
        int cycleNum = fdevTasks.stream().filter(task -> CommonUtils.isNotNullOrEmpty(task.getPlan_start_time(),task.getPlan_fire_time()))
                .map(task ->{
                    if(task.getPlan_start_time().contains("-")){
                        task.setPlan_start_time(CommonUtils.dateParse(task.getPlan_start_time()));
                    }
                    if(task.getPlan_fire_time().contains("-")){
                        task.setPlan_fire_time(CommonUtils.dateParse(task.getPlan_fire_time()));
                    }
                    return task;
        }).mapToInt(task -> {
            String dayNum = CommonUtils.dateCompare(task.getPlan_start_time(), task.getPlan_fire_time());
            return CommonUtils.isNotNullOrEmpty(dayNum) ? Integer.valueOf(dayNum) : 0;
        }).sum();
        if (cycleNum == 0)
            return "0.0";
        //计算延期天数总和 延期天数（今日/实际结束时间-计划投产日期）
        int delaySum = fdevTasks.stream().mapToInt(task -> {
            String dayNum = CommonUtils.dateCompare(CommonUtils.isNotNullOrEmpty(task.getFire_time()) ? task.getFire_time() : LocalDate.now().format(DateTimeFormatter.ofPattern(CommonUtils.DATE_PATTERN_F1)), task.getPlan_fire_time());
            return CommonUtils.isNotNullOrEmpty(dayNum) ? Integer.valueOf(dayNum) : 0;
        }).sum();
        float result = (float) (delaySum * 100 / cycleNum) / 100;
        return "" + result*100;
    }
}
