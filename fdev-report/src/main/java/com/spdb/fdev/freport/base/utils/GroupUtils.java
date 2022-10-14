package com.spdb.fdev.freport.base.utils;

import com.spdb.fdev.freport.spdb.entity.user.Group;

import java.util.*;

public class GroupUtils {

    /**
     * 组装父子结构
     *
     * @param resource
     * @return
     */
    public static void setChildren(Set<Group> resource) {
        resource.forEach(x -> { //组装父子结构
            x.setChildren(new ArrayList<>());
            resource.forEach(y -> {
                if (x.getId().equals(y.getParentId())) {
                    x.getChildren().add(y);
                }
            });
        });
    }

    /**
     * 过滤
     *
     * @param resource
     * @return
     */
    public static Set<Group> filterByGroupIds(Set<Group> resource, Set<String> groupIds) {
        return new LinkedHashSet<Group>() {{
            resource.forEach(group -> {
                if (groupIds.contains(group.getId())) {
                    add(group);
                }
            });
        }};
    }


    /**
     * n级结构转两级结构
     *
     * @param resource
     * @return
     */
    public static List<Group> getChildren(List<Group> resource) {
        return new ArrayList<Group>() {{
            for (Group item : resource) {
                addAll(item.getChildren());
                if (!CommonUtils.isNullOrEmpty(item.getChildren())) {
                    addAll(getChildren(item.getChildren()));
                }
            }
        }};
    }

    public static Set<Group> getGroupChildrenTotalSingleLevel(Set<Group> filterGroup) {
        return new LinkedHashSet<Group>() {{
            for (Group item : filterGroup) {
                if (!CommonUtils.isNullOrEmpty(item.getChildren())) {
                    item.getChildren().addAll(getChildren(item.getChildren()));
                }
                add(item);
            }
        }};
    }
}
