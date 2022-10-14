package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.GroupDao;
import com.spdb.fdev.fuser.spdb.dao.RoleDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.dto.UserListInGroupPage;
import com.spdb.fdev.fuser.spdb.entity.user.Group;
import com.spdb.fdev.fuser.spdb.entity.user.Role;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class GroupServiceImpl implements GroupService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());//日志打印

    @Resource
    private GroupDao groupDao;

    @Resource
    private UserDao userDao;
    @Resource
    private RoleDao roleDao;

    @Override
    public Group addGroup(Group group) throws Exception {
        group.setStatus("1");
        if(CommonUtils.isNullOrEmpty(group.getParent_id())){
            List<Group> gfirstlist = groupDao.queryFirstGroup();
            group.setSortNum(String.valueOf(gfirstlist.size()+1));
        } else {
            Group group1 = groupDao.queryDetailById(group.getParent_id());
            Group group2 = new Group();
            group2.setParent_id(group.getParent_id());
            List<Group> grouplist = groupDao.queryGroup(group2);
            group.setSortNum(group1.getSortNum()+"-"+String.valueOf(grouplist.size()+1));
        }
        List<Group> oldMap = groupDao.queryGroup(group);
        if (!CommonUtils.isNullOrEmpty(oldMap)) {//该条记录已经存在
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{Dict.GROUP});
        }
        return groupDao.addGroup(group);
    }

    @Override
    public List<Group> queryGroup(Group group) throws Exception {
        List<Group> list = new ArrayList<>();
        List<Group> glist = groupDao.queryGroup(group);
        for (Group group2 : glist) {
            int count = 0;
            User user = new User();
            user.setGroup_id(group2.getId());
            user.setStatus("0");  //在职
            List<Map> ulist = userDao.getUserCoreData(user);
            if (ulist != null) {
                count = ulist.size();
            }
            //当前组和子组id
            List<String> childListIds = new ArrayList<>();
            childListIds = queryGroupid(childListIds, group2.getId());
            int allcount = userDao.queryUsersByGroups(childListIds).size();
            group2.setCount(allcount);
            group2.setCurrent_count(count);
            list.add(group2);
        }

        //全量查询组时，进行小组排序（同一父组排在一起）
            List<Group> sortlist = new ArrayList<>();
        //查询所有正在使用且不存在父组的小组
            List<Group> gfirstlist = groupDao.queryFirstGroup();
            for(Group firstgroup : gfirstlist){
                //查询当前组和子组id
                List<String> firstchildListIds = new ArrayList<>();
                firstchildListIds = queryGroupid(firstchildListIds, firstgroup.getId() );
                for(String childId : firstchildListIds){
                    for (Group group3 : list) {
                      if(group3.getId().equals(childId))
                          sortlist.add(group3);
                    }
                }
            }
        return  sortlist;
    }

    @Override
    public List<Map> queryByGroupId(Group group) throws Exception {
        List<Map> ulist = new ArrayList<>();
        //子组用户
        List<String> list = new ArrayList<>();
        list = queryGroupid(list, group.getId());//拿到组及子组的id
        User user = new User();
        for (String lis : list) {
            user.setGroup_id(lis);
            List<Map> user1 = userDao.getUser(user);
            if (!CommonUtils.isNullOrEmpty(user1)) {
                ulist.addAll(user1);
            }
        }
        return ulist;
    }

    /**
     *  通过组id查询父组信息
     * @param group
     * @return
     * @throws Exception
     */
    @Override
    public List<Group> queryParentGroupById(Group group) throws Exception {
        List<Group> groupList = new ArrayList<>();
        //当前组
        List<Group> childGroup = groupDao.queryGroup(group);
        if (CommonUtils.isNullOrEmpty(childGroup)) {
            return new ArrayList<>();
        }
        //父组
        List<String> parentGroupIdList = new ArrayList<>();
        parentGroupIdList = queryParent(parentGroupIdList, childGroup.get(0));
        Group queryGroup = new Group();
        for (String parentGroupId : parentGroupIdList) {
            queryGroup.setId(parentGroupId);
            List<Group> groups = groupDao.queryGroup(queryGroup);
            if (!CommonUtils.isNullOrEmpty(groups)) {
                groupList.addAll(groups);
            }
        }
        return groupList;
    }

    @Override
    public List<Group> queryChildGroupById(Group group) throws Exception {
        List<Group> ulist = new ArrayList<>();
        //子组
        List<String> list = new ArrayList<>();
        list = queryGroupid(list, group.getId());
        Group group1 = new Group();
        for (String lis : list) {
            group1.setId(lis);
            List<Group> groups = groupDao.queryGroup(group1);
            if (!CommonUtils.isNullOrEmpty(groups)) {
                ulist.addAll(groups);
            }
        }
        return ulist;
    }

    @Override
    public Group updateGroup(Group group) {
        Group newGroup = groupDao.updateGroup(group);
        return newGroup;
    }

    @Override
    public Group deleteGroup(Group group) throws Exception {
        User user = new User();
        user.setStatus("0");
        user.setGroup_id(group.getId());
        List<Map> user2 = userDao.getUser(user);
        //该小组下没有人员就进行删除操作
        if (CommonUtils.isNullOrEmpty(user2)) {
        	return groupDao.deleteGroup(group);
        }else {
        	throw new FdevException(ErrorConstants.USR_INUSE_ERROR);
		}
    }

    /*
       根据组id查询父组id
     */
    public List<String> queryParent(List<String> list, Group group) throws Exception {
        list.add(group.getId());
        if (CommonUtils.isNullOrEmpty(group.getParent_id())) {
            return list;  //无父节点
        } else {
            Group group1 = new Group();
            group1.setId(group.getParent_id());
            group1.setStatus("1");
            List<Group> glist = groupDao.queryGroup(group1);
            if (CommonUtils.isNullOrEmpty(glist)) {
                return list;
            }
            for (Group lis : glist) {
                queryParent(list, lis);
            }
        }
        return list;
    }

    /*
       根据组id查询子组id
     */
    public List<String> queryGroupid(List<String> list, String groupId) throws Exception {
        Group group1 = new Group();
        group1.setParent_id(groupId);
        List<Group> glist = groupDao.queryGroup(group1);
        if (CommonUtils.isNullOrEmpty(glist)) {
            list.add(group1.getParent_id());  //无子节点
        } else {
            list.add(groupId);
            for (Group lis : glist) {
                queryGroupid(list, lis.getId());
            }
        }
        return list;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "fuser.allgroup")
    public List<Group> queryAllGroup() throws Exception {
        List<Group> grouplist = groupBySortNum();
        grouplist = rBuildGroupName(grouplist);
        return grouplist;
    }

    //拼接小组全称
    public List<Group> rBuildGroupName(List<Group> grouplist) throws Exception {
        String s = "-";
        for (Group group1 : grouplist) {
            Group group2 = new Group();
            group2.setId(group1.getId());
            List<Group> groups = queryParentGroupById(group2);
            String groupName = "";
            for (int i = groups.size() - 1; i >= 0; i--) {
                String name = groups.get(i).getName();
                if (!groups.get(0).equals(groups.get(i))) {
                    name += s;
                }
                groupName += name;
            }
            group1.setFullName(groupName);
        }
        return grouplist;
    }

    /**
     * 根据 环境+"*"+key+"*" 来清除缓存
     *
     * @param key
     */
    @RemoveCachedProperty(redisKeyExpression = "{key}")
    public void removeCache(String key) {
    }

	@Override
	public List<Group> queryDevResource(List<String> groupIds) throws Exception {
		//在职人员
		User user = new User();
		user.setStatus("0");
		List<Map> users = userDao.getUser(user);
		Role role = new Role();
		role.setName("开发人员");
		List<Role> roles = roleDao.queryRole(role);
		List<Map> userMap = new ArrayList<>();
		//过滤角色为开发的人员
		for (Map u : users) {
			List<String> rList = (List<String>)u.get(Dict.ROLE_ID);
			if (rList.contains(roles.get(0).getId())) {
				userMap.add(u);
			}
		}
		Group group = new Group();
		List<Group> groups = groupDao.queryGroup(group);
		List<Group> groupMap = new ArrayList<>();
		for (String groupId : groupIds) {
			for (Group g : groups) {
				if (groupId.equals(g.getId())) {
					groupMap.add(g);
				}
			}
		}
		for (Group g : groupMap) {
			int i = 0;
			for (Map uMap : userMap) {
				if (g.getId().equals(uMap.get(Dict.GROUP_ID))) {
					i++;
				}
			}
			g.setCount(i);
		}
		return groupMap;
	}

    @Override
    public Group queryDetailById(String groupId) {
        return groupDao.queryDetailById(groupId);
    }

    @Override
    public List<String> queryGroupByNames(String testcentre, String businessdept, String plandept) {
        return groupDao.queryGroupByNames(testcentre, businessdept, plandept);
    }

    @Override
    public List<Group> queryByGroup(Group group) throws Exception {
        List<Group> glist = groupDao.queryGroup(group);
        glist = rBuildGroupName(glist);
        return  glist;
    }

    @Override
    public List<Group> groupBySortNum() throws Exception{
        List<Group> sortGroup = new ArrayList<>();
        //一级组信息
        List<Group> gfirstlist = groupDao.queryFirstGroup();
        sortGroup = sortBySortNum(gfirstlist, sortGroup);
        return sortGroup;
    }

    //根据 SortNum字段进行组排序
    public List<Group> sortBySortNum(List<Group> glist, List<Group> sortGroup) throws Exception{
        //根据同层的序号排序（glist为同层组信息）
        Collections.sort(glist, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                String[] Num1 = o1.getSortNum().split("-");
                String[] Num2 = o2.getSortNum().split("-");
                if(Num1.length == Num2.length) {
                    return  Integer.valueOf(Num1[Num1.length-1]).compareTo(Integer.valueOf(Num2[Num2.length-1]));
                }
                return 0;
            }
        });

        for(Group group : glist){
            Group group1 = new Group();
            group1.setParent_id(group.getId());
            List<Group> childGroup = groupDao.queryGroup(group1);
            if (CommonUtils.isNullOrEmpty(childGroup)) {
                sortGroup.add(group);  //无子节点
            } else {
                sortGroup.add(group);
                sortBySortNum(childGroup, sortGroup);
            }
        }
        return sortGroup;
    }

    @Override
    public List<Group> queryGroupByIds(List<String> groupIds, boolean allFlag) {
        return groupDao.queryGroupByIds(groupIds,allFlag);
    }

    @LazyInitProperty(redisKeyExpression = "fuser.allgroupname")
    public Map queryAllGroupName(){
        List<Group> groups = groupDao.queryAllGroup();
        Map<String, String> result = new HashMap();
        for(Group group:groups){
            result.put(group.getId(),group.getName());
        }
        return result;
    }

    /**
     * 通过组ids查询group，并拼装fullName（每一个级的组都加上）
     *
     * @param groupIds
     * @return
     */
    @Override
    public List queryGroupFullNameByIds(List<String> groupIds) {
        return this.groupDao.queryGroupFullNameByIds(groupIds);
    }

    /**
     * 根据小组id查询小组中文名
     * @param groupId
     * @return
     */
    @Override
    public String queryGroupNameById(String groupId) throws Exception {
        Group group = groupDao.queryGroupById(groupId);
        if(!CommonUtils.isNullOrEmpty(group)){
            return group.getName();
        }
        return null;
    }

    /**
     * 查询所有小组包含本组及子组的人数，按层级关系返回
     * @return
     */
    @Override
    public List<Group> queryAllGroupAndChild() {
        //先查所有的一级组
        List<Group> groups = groupDao.queryFirstGroupMap(false);
//        List<Group> groups = groupDao.queryFirstGroupMap(true);
        //递归查询一级组及其子组
        int i = 0;
        for(Group group:groups){
//            group.setLevel(1);
//            group.setSortNum(++i + "");
//            groupDao.updateGroup(group);
            this.queryChild(group);
        }
        return groups;
    }

    /**
     * 根据组id分页查询组下的人（分包含和不包含子组）
     * @param groupId
     * @param showChild
     * @param pageSize
     * @param index
     * @return
     */
    @Override
    public Map queryUserByGroupId(String groupId, boolean showChild, int pageSize, int index) throws Exception {
        List<String> iAndChildGroupIds = new ArrayList<>();
        if(showChild){
            //查询包含子组
            iAndChildGroupIds = queryGroupid(iAndChildGroupIds, groupId);//拿到组及子组的id
        }else {
            iAndChildGroupIds.add(groupId);
        }
        Map data = userDao.getUsersInGroup(iAndChildGroupIds,pageSize,index);
        List<UserListInGroupPage> usersInGroup = (List<UserListInGroupPage>)data.get(Dict.DATA);
        long count = (long)data.get("count");
        Map result = new HashMap();
        result.put("total",count);
        result.put("data",usersInGroup);
        return result;
    }

    /**
     * 批量修改人员组信息
     * @param userIds
     */
    @Override
    public void addGroupUsers(List<String> userIds, String groupId) {
        groupDao.addGroupUsers(userIds, groupId);
    }

    /**
     * 获取组的三级父组，若当前组为3级之内，则直接返回自身
     * @param groupId
     * @return
     */
    @Override
    public Group getThreeLevelGroup(String groupId) throws Exception {
        Group group = new Group();
        group.setId(groupId);
        List<Group> groups = groupDao.queryGroup(group);
        if(CommonUtils.isNullOrEmpty(groups)){
            throw new FdevException("组id不存在");
        }
        group = groups.get(0);
        if(group.getLevel() <= 3){
            return group;
        }
        String sortNum = group.getSortNum();
        String[] split = sortNum.split("-");
        String resultSortNum = "";
        for(int i = 0; i < 3; i++){
            resultSortNum += split[i];
            if(i < 2){
                resultSortNum +=  "-";
            }
        }
        group = new Group();
        group.setSortNum(resultSortNum);
        return groupDao.queryGroup(group).get(0);
    }

    public void queryChild(Group group){
        List<Group> childs = groupDao.queryChild(group.getId(), false);
//        List<Group> childs = groupDao.queryChild(group.getId(), true);
        group.setChildGroup(childs);
        //获取当前组的在职人数
        group.setCurrent_count((int)userDao.queryInJobUserNum(group.getId()));
        if(CommonUtils.isNullOrEmpty(childs)){
            group.setCount(group.getCurrent_count());
            return;
        }
        int i = 0;
        for(Group child : childs){
            //跑数据前要把查询改成查全部，包含已废弃的
//            child.setLevel(group.getLevel() + 1);
//            child.setSortNum(group.getSortNum() + "-" + ++i);
//            groupDao.updateGroup(child);
            queryChild(child);
        }
        group.setCount(group.getCurrent_count() + getGroupUserSum(childs));

    }

    /**
     * 计算子组人数之和
     * @param childs
     * @return
     */
    private Integer getGroupUserSum(List<Group> childs) {
        int result = 0;
        for(Group g:childs){
            result += g.getCount();
        }
        return result;
    }

    @Override
    public Map<String, List<Group>> queryChildGroupByIds(List<String> ids) {
        Map<String, List<Group>> result = new HashMap<>();
        List<Group> parentGroupList = groupDao.queryGroupByIds(ids, false);
        for (Group group : parentGroupList) {
            List<Group> childGroupList = groupDao.queryGroupBySortNum(group.getSortNum());
            result.put(group.getId(), childGroupList);
        }
        return result;
    }
}
