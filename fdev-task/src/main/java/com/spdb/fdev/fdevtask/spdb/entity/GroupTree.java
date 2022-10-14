package com.spdb.fdev.fdevtask.spdb.entity;

import java.util.ArrayList;
import java.util.List;

public class GroupTree {

    private String id;
    private String parent;
    private int depth;
    private List<GroupTree> child;
    private String name;

    public GroupTree() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<GroupTree> getChild() {
        return child;
    }

    public void setChild(List<GroupTree> child) {
        this.child = child;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean ischild() {
        return child == null || child.size() == 0;
    }

    public List getChildIds() {
        List childids = new ArrayList();
        if (ischild()) {
            return childids;
        }
        child.forEach(n -> {
            childids.add(n.getId());
        });
        return childids;
    }
}
