package com.mantis.util;

import com.mantis.dict.Dict;
import com.mantis.entity.GroupTree;
import com.mantis.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupTreeUtil {
    @Autowired
    private UserService iUserApi;

    public GroupTree getGroupTree() {
        GroupTree root = new GroupTree();
        try {
            List groups = iUserApi.queryAllGroup();
            root.setName("root");
            root.setId("1");
            root.setDepth(0);
            root.setParent("");
            List<GroupTree> rootChild = new ArrayList<>();
            List<GroupTree> groupTree = (List) groups.stream().filter(n -> "1".equals(((Map) n).get(Dict.STATUS))).map(n -> {
                Map tmp = (Map) n;
                String parent = (String) tmp.get("parent_id");
                String id = (String) tmp.get(Dict.ID);
                String name = (String) tmp.get(Dict.NAME);
                GroupTree tree = new GroupTree();
                tree.setId(id);
                tree.setName(name);
                tree.setParent(parent);
                if (StringUtils.isBlank(parent)) {
                    tree.setDepth(1);
                    rootChild.add(tree);
                }
                return tree;
            }).collect(Collectors.toList());
            root.setChild(rootChild);
            rootChild.forEach(n -> {
                buildChild(n, groupTree, 0);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * 根据上级节点迭代子节点
     *
     * @param parent
     * @param noTopTree
     * @param depth
     */
    private void buildChild(GroupTree parent, List<GroupTree> noTopTree, int depth) {
        if (parent.getDepth() < depth || depth == 0) {
            List<GroupTree> childList = new ArrayList<>();
            noTopTree.forEach(c -> {
                if (parent.getId().equals(c.getParent())) {
                    c.setDepth(parent.getDepth() + 1);
                    childList.add(c);
                    buildChild(c, noTopTree, depth);
                }
            });
            parent.setChild(childList);
        }
    }


    /**
     * 获取对应组所有子节点id信息 没有去重
     *
     * @param groupTree
     * @param fullChild
     * @return
     */
    public List<String> getFullChilds(GroupTree groupTree, List fullChild) {
        List<GroupTree> children = groupTree.getChild();
        if (groupTree.ischild()) {
            return fullChild;
        }
        for (GroupTree n : children) {
            fullChild.add(n.getId());
            getFullChilds(n, fullChild);
        }
        return fullChild;
    }

    public List<String> getFullChildsById(String id) {
        return getFullChilds(getNodeById(getGroupTree(), id), new ArrayList());
    }


    /**
     * 根据id查找组信息
     *
     * @param root
     * @param id
     * @return
     */
    private GroupTree getNodeById(GroupTree root, String id) {
        GroupTree resultTree = new GroupTree();
        if (root.getId().equals(id)) {
            return root;
        }
        List<GroupTree> child = root.getChild();
        if (CollectionUtils.isNotEmpty(child)) {
            for (GroupTree tree : child) {
                resultTree = getNodeById(tree, id);
                if (StringUtils.isNotBlank(resultTree.getId())) {
                    return resultTree;
                }
            }
        }
        return resultTree;
    }

    /**
     * 是不是包含子组
     *
     * @param id
     * @return
     */
    public boolean isChild(String id) {
        return getNodeById(getGroupTree(), id).ischild();
    }

    /**
     * @param id
     * @return
     */
    public List getWithChild(GroupTree root,String id) {
        List result = new ArrayList();
        result.add(id);
        GroupTree target = getNodeById(root, id);
        if (!target.ischild()) {
            result.addAll(getFullChilds(target, new ArrayList<>()));
        }
        return result;
    }

    public List getWithChild(GroupTree root, List groups) {
        List result = new ArrayList();
        for(Object o:groups){
            result.add(o);
            GroupTree target = getNodeById(root, (String) o);
            if (!target.ischild()) {
                result.addAll(getFullChilds(target, new ArrayList<>()));
            }
        }
        return result;
    }


    public List getAllChilds(String parentid){
        GroupTree groupTree = getGroupTree();
        return getWithChild(groupTree, parentid);
    }

}
