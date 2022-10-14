package com.spdb.fdev.fdevtask.spdb.entity;

import com.spdb.fdev.fdevtask.base.utils.GroupTreeUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author patrick
 * @date 2021/3/4 2:13 下午
 * @Des 123
 * 最簡單的事是堅持，最難的事還是堅持
 */
public class TreeGroupNode implements GroupTreeUtils.GroupTreeNode {

    private String groupId;//组id
    private String parent;//父组id
    private List<TreeGroupNode> children;//子组列表
    private String name;//组名字
    private Long taskNum;//任务数量
    private Map<String, Long> listStage;//任务各个阶段数量
    private Map<String, Long> delayCycle;//各个延期周期数量
    private List<TopFiveClass> topFive;//各组延期任务top5


    @Override
    public String id() {
        return this.groupId;
    }

    @Override
    public String parentId() {
        return this.parent;
    }

    @Override
    public boolean root() {
        return Objects.equals(this.parent, "");
    }

    @Override
    public void setChildren(List children) {
        this.children = children;
    }


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public List<TreeGroupNode> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Long getTaskNum() {
        return taskNum;
    }

    @Override
    public void setTaskNum(Long taskNum) {
        this.taskNum = taskNum;
    }


    public Map<String, Long> getListStage() {
        return listStage;
    }

    public void setListStage(Map<String, Long> listStage) {
        this.listStage = listStage;
    }

    public Map<String, Long> getDelayCycle() {
        return delayCycle;
    }

    public void setDelayCycle(Map<String, Long> delayCycle) {
        this.delayCycle = delayCycle;
    }

    public List<TopFiveClass> getTopFive() {
        return topFive;
    }

    public void setTopFive(List<TopFiveClass> topFive) {
        this.topFive = topFive;
    }
}

