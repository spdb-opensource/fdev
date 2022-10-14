package com.spdb.fdev.freport.spdb.service.impl;

import com.spdb.fdev.freport.base.dict.StatisticsDict;
import com.spdb.fdev.freport.base.dict.TaskEnum;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.base.utils.GroupUtils;
import com.spdb.fdev.freport.base.utils.UserUtils;
import com.spdb.fdev.freport.spdb.dao.TaskDao;
import com.spdb.fdev.freport.spdb.dao.UserDao;
import com.spdb.fdev.freport.spdb.entity.task.Task;
import com.spdb.fdev.freport.spdb.entity.user.Area;
import com.spdb.fdev.freport.spdb.entity.user.Company;
import com.spdb.fdev.freport.spdb.entity.user.Group;
import com.spdb.fdev.freport.spdb.entity.user.User;
import com.spdb.fdev.freport.spdb.service.ResourceManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceManageServiceImpl implements ResourceManageService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private UserUtils userUtils;

    @Value("${fdev.user.company.spdb.id}")
    private String spdbId;

    /**
     * 项目组规模
     * 请注意，这是一个重量不重质的接口，完全参照旧接口的垃圾数据结构实现，部分地方也无所谓规范不规范了
     *
     * @param groupIds
     * @param includeChild
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryPersonStatistics(List<String> groupIds, Boolean includeChild) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Company> company = userDao.findCompany();//默认查启用的公司
        Map<String, String> companyMap = company.stream().collect(Collectors.toMap(Company::getId, Company::getName));
        List<Area> area = userDao.findArea();
        Set<Group> totalGroups = new HashSet<>(userDao.findGroups(new HashSet<>(groupIds), "1"));
        Map<String, String> groupNameMap = totalGroups.stream().collect(Collectors.toMap(Group::getId, Group::getName));
        Set<Group> resultGroups;
        if (!CommonUtils.isNullOrEmpty(includeChild) && includeChild) {//默认不查组下
            totalGroups.addAll(userDao.findGroupBySortNum(totalGroups.stream().map(Group::getSortNum).collect(Collectors.toSet())));
            GroupUtils.setChildren(totalGroups); //获取组下组
            //获取所需展示组  并 组装成单级数据结构
            resultGroups = GroupUtils.getGroupChildrenTotalSingleLevel(GroupUtils.filterByGroupIds(totalGroups, new HashSet<>(groupIds)));
        } else {
            resultGroups = totalGroups;
        }
        //准备 组 - 人 map
        Map<String, List<User>> groupUserMap = new HashMap<>();
        if (!CommonUtils.isNullOrEmpty(totalGroups)) {
            //根据组获取全部在职人员
            List<User> users = userDao.findUserByGroupIds(totalGroups.stream().map(Group::getId).collect(Collectors.toSet()));
            if (!CommonUtils.isNullOrEmpty(users)) {
                groupUserMap = users.stream().collect(Collectors.groupingBy(User::getGroupId));
            }
        }
        result.put("bank", area);
        result.put("dev", company.stream().filter(x -> !spdbId.equals(x.getId())).collect(Collectors.toList()));
        List<Map<String, Object>> groups = new ArrayList<>();
        for (Group group : resultGroups) {
            String groupId = group.getId();
            Map<String, Object> groupMap = new HashMap<>();
            List<User> users = new ArrayList<>();
            if (!CommonUtils.isNullOrEmpty(groupUserMap.get(groupId))) users.addAll(groupUserMap.get(groupId));
            if (!CommonUtils.isNullOrEmpty(group.getChildren())) {
                for (Group child : group.getChildren()) {
                    if (!CommonUtils.isNullOrEmpty(groupUserMap.get(child.getId())))
                        users.addAll(groupUserMap.get(child.getId()));
                }
            }
            StringBuffer remark = new StringBuffer();
            users.removeIf(user -> {
                if (userUtils.isUnResource(user)) {
                    remark.append(user.getUserNameCn() + "(" + companyMap.get(user.getCompanyId()) + ")");
                    remark.append(',');
                    return true;
                }
                return false;
            });
            if (remark.length() > 0) remark.deleteCharAt(remark.length() - 1);
            groupMap.put("remark", remark.toString());
            groupMap.put("userSum", users.size());
            groupMap.put("groupId", groupId);
            groupMap.put("groupName", groupNameMap.get(groupId));
            groupMap.put("bank", new HashMap<String, Integer>() {{
                Map<String, List<User>> areaUser = users.stream().filter(x -> spdbId.equals(x.getCompanyId()) && !CommonUtils.isNullOrEmpty(x.getAreaId())).collect(Collectors.groupingBy(User::getAreaId));
                for (String areaId : area.stream().map(Area::getId).collect(Collectors.toList())) {
                    if (areaUser.get(areaId) != null) {
                        put(areaId, areaUser.get(areaId).size());
                    } else {
                        put(areaId, 0);
                    }
                }
            }});
            groupMap.put("dev", new HashMap<String, Integer>() {{
                Map<String, List<User>> companyUser = users.stream().filter(x -> !spdbId.equals(x.getCompanyId())).collect(Collectors.groupingBy(User::getCompanyId));
                for (String companyId : company.stream().filter(x -> !spdbId.equals(x.getId())).map(Company::getId).collect(Collectors.toList())) {
                    if (companyUser.get(companyId) != null) {
                        put(companyId, companyUser.get(companyId).size());
                    } else {
                        put(companyId, 0);
                    }
                }
            }});
            groups.add(groupMap);
        }
        result.put("groups", groups);
        return result;
    }

    @Override
    public List<List<Object>> queryPersonFreeStatistics(List<String> groupIds, Boolean includeChild) throws Exception {
        return new ArrayList<List<Object>>() {{
            List<Company> company = userDao.findCompany();//默认查启用的公司
            Set<Group> totalGroups = new HashSet<>(userDao.findGroups(new HashSet<>(groupIds), "1"));
            Set<Group> resultGroups;
            if (!CommonUtils.isNullOrEmpty(includeChild) && includeChild) {//默认不查组下
                totalGroups.addAll(userDao.findGroupBySortNum(totalGroups.stream().map(Group::getSortNum).collect(Collectors.toSet())));
                GroupUtils.setChildren(totalGroups); //获取组下组
                //获取所需展示组  并 组装成单级数据结构
                resultGroups = GroupUtils.getGroupChildrenTotalSingleLevel(GroupUtils.filterByGroupIds(totalGroups, new HashSet<>(groupIds)));
            } else {
                resultGroups = totalGroups;
            }
            //准备 组 - 人 map
            Map<String, List<User>> groupUserMap = new HashMap<>();
            //准备 人 - 开发任务 map
            Map<String, Set<String>> userDevelopTaskIdMap = new HashMap<>();
            //准备 人 - 涉及任务 map
            Map<String, Set<String>> userRelateTaskIdMap = new HashMap<>();
            if (!CommonUtils.isNullOrEmpty(totalGroups)) {
                //根据组获取全部在职人员
                List<User> users = userDao.findUserByGroupIds(totalGroups.stream().map(Group::getId).collect(Collectors.toSet()));
                if (!CommonUtils.isNullOrEmpty(users)) {
                    groupUserMap.putAll(users.stream().collect(Collectors.groupingBy(User::getGroupId)));
                    Set<String> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
                    //获取全部在职人员全部任务
                    List<Task> totalTask = taskDao.findByUserIds(userIds);
                    //过滤归档、废弃、删除的任务
                    totalTask = totalTask.stream().filter(x -> !TaskEnum.TaskStage.FILE.getName().equals(x.getStage()) || !TaskEnum.TaskStage.ABORT.getName().equals(x.getStage()) || !TaskEnum.TaskStage.DISCARD.getName().equals(x.getStage())).collect(Collectors.toList());
                    for (Task task : totalTask) {
                        Set<String> develop = new HashSet<String>() {{
                            if (!CommonUtils.isNullOrEmpty(task.getDeveloper())) addAll(task.getDeveloper());
                        }};
                        develop.forEach(x -> {
                            Set<String> temp = userDevelopTaskIdMap.getOrDefault(x, new HashSet<>());
                            temp.add(task.getId());
                            userDevelopTaskIdMap.put(x, temp);
                        });
                        Set<String> relate = new HashSet<String>() {{
                            if (!CommonUtils.isNullOrEmpty(task.getCreator())) add(task.getCreator());
                            if (!CommonUtils.isNullOrEmpty(task.getMaster())) addAll(task.getMaster());
                            if (!CommonUtils.isNullOrEmpty(task.getSpdbMaster())) addAll(task.getSpdbMaster());
                            if (!CommonUtils.isNullOrEmpty(task.getTester())) addAll(task.getTester());
                            addAll(develop);
                        }};
                        relate.forEach(x -> {
                            Set<String> temp = userRelateTaskIdMap.getOrDefault(x, new HashSet<>());
                            temp.add(task.getId());
                            userRelateTaskIdMap.put(x, temp);
                        });
                    }
                }
            }
            //最终行数据
            //第一行
            add(new ArrayList<Object>() {{
                add(StatisticsDict.GROUP);
                addAll(company.stream().map(Company::getName).collect(Collectors.toList()));
            }});
            //统计数据行
            for (Group group : resultGroups) {
                add(new ArrayList<Object>() {{
                    add(group.getName());
                    Set<User> groupUser = new HashSet<User>() {{
                        List<User> users = groupUserMap.get(group.getId());
                        if (!CommonUtils.isNullOrEmpty(users)) addAll(users);
                        if (!CommonUtils.isNullOrEmpty(group.getChildren())) {
                            for (Group child : group.getChildren()) {
                                List<User> childUsers = groupUserMap.get(child.getId());
                                if (!CommonUtils.isNullOrEmpty(childUsers)) addAll(childUsers);
                            }
                        }
                    }};
                    for (Company company : company) {
                        int companyUserNum = 0;
                        int noDevelopTaskNum = 0;
                        int noRelateTaskNum = 0;
                        Set<User> groupCompanyUser = groupUser.stream().filter(x -> company.getId().equals(x.getCompanyId())).collect(Collectors.toSet());
                        if (!CommonUtils.isNullOrEmpty(groupCompanyUser)) {
                            companyUserNum += groupCompanyUser.size();
                            for (User user : groupCompanyUser) {
                                if (CommonUtils.isNullOrEmpty(userDevelopTaskIdMap.get(user.getId())))
                                    noDevelopTaskNum++;
                                if (CommonUtils.isNullOrEmpty(userRelateTaskIdMap.get(user.getId()))) noRelateTaskNum++;
                            }
                        }
                        add(companyUserNum + "/" + noDevelopTaskNum + "/" + noRelateTaskNum);
                    }
                }});
            }
        }};
    }
}