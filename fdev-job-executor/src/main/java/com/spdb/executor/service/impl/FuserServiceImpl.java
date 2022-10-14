package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Constants;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.dict.ErrorConstants;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.csii.pe.spdb.common.util.DateUtils;
import com.spdb.executor.service.FtaskService;
import com.spdb.executor.service.FuserService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FuserServiceImpl implements FuserService {

    private Logger logger = LoggerFactory.getLogger(FuserServiceImpl.class);

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private FtaskService ftaskService;

    /**
     * 查询用户全量 权限信息
     */
    @Override
    public List queryRole() {
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, "queryRole");
        List resDate = new ArrayList();
        try {
            resDate = (List) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error("查询用户数据失败", e);
            throw new FdevException(ErrorConstants.FUSER_ERROR);
        }
        return resDate;
    }

    /**
     * 获取用户 对应权限的信息
     * @PARAM roleName  用户权限中文名
     */
    @Override
    public Map queryRoleByName(String roleName) {
        List<LinkedHashMap> roleDate = queryRole();
        Map resDate = new HashMap();
        for (LinkedHashMap item : roleDate) {
            if (item.get(Dict.NAME).toString().equals(roleName)){
                resDate = item;
                break;
            }
        }
        return resDate;
    }


    /**
     * 查询全量用户详细信息]   去除已离职 的用户
     * @PARAM roleId  用户权限id
     */
    @Override
    public List queryUser(String roleId) {
        Map userSend = new HashMap();
        userSend.put(Dict.REST_CODE, "queryUser");
        userSend.put(Dict.STATUS, "0");
        if (!CommonUtils.isNullOrEmpty(roleId)){
            List<String> roleIds = new ArrayList<>();
            roleIds.add(roleId);
            userSend.put("role_id", roleIds);
        }
        List resDate = new ArrayList();
        try {
            resDate = (List) restTransport.submit(userSend);
        } catch (Exception e) {
            logger.error("查询用户数据失败", e);
            throw new FdevException(ErrorConstants.FUSER_ERROR);
        }
        return resDate;
    }

    /**
     * 查询全量的组数据  除废弃 小组
     */
    @Override
    public List queryAllGroup() {
        Map userSend = new HashMap();
        userSend.put(Dict.REST_CODE, "queryGroup");
        userSend.put(Dict.STATUS, "1");
        List resDate = new ArrayList();
        try {
            resDate = (List) restTransport.submit(userSend);
        } catch (Exception e) {
            logger.error("查询用户组数据失败", e);
            throw new FdevException(ErrorConstants.FUSER_ERROR);
        }
        return resDate;
    }

    @Override
    public List<Map> queryChildGroupById(String id) {
        Map userSend = new HashMap();
        userSend.put(Dict.REST_CODE, "queryChildGroupById");
        userSend.put(Dict.ID, id);
        List resDate = new ArrayList();
        try {
            resDate = (List) restTransport.submit(userSend);
        } catch (Exception e) {
            logger.error("查询用户组数据失败", e);
            throw new FdevException(ErrorConstants.FUSER_ERROR);
        }
        return resDate;
    }

    @Override
    public List<String> queryChildGroup(String id) {
        List<Map> resDate = queryChildGroupById(id);

        List resList = new ArrayList();
        for (Map item : resDate) {
            resList.add(item.get("id").toString());
        }

        return resList;
    }


    /**
     * 组装 周 资源统计 数据
     * @param roleName   权限中文名名称
     * @param roleEnName    权限英文名
     * @param startTimeKey  时间计算 开始时间
     * @param endTimeKey    时间计算 结束时间
     * @return
     */
    public List getUserResource(String roleName, String roleEnName, String startTimeKey, String endTimeKey){
        List developList = new ArrayList();
        Map roleMap = queryRoleByName(roleName); //获取 对应权限 权限的 id
        List<LinkedHashMap> developUsers = queryUser(roleMap.get(Dict.ID).toString());    //获取到所有的开发人员的 数据
        for (LinkedHashMap userItem : developUsers) {
            Map userInfo = new HashMap();
            String groupName = "";
            String groupId = "";
            String companyName = "";
            String userEnName = userItem.get(Dict.USER_NAME_CN).toString();
            if (!CommonUtils.isNullOrEmpty(userItem.get(Dict.GROUP))){
                groupName = (String) ((LinkedHashMap) userItem.get(Dict.GROUP)).get(Dict.NAME);
                groupId = (String) userItem.get("group_id");
            }
            if (!CommonUtils.isNullOrEmpty(userItem.get(Dict.COMPANY))){
                companyName = (String) ((LinkedHashMap) userItem.get(Dict.COMPANY)).get(Dict.NAME);
            }

            userInfo.put(Dict.USER_NAME_CN, userEnName);  //姓名
            userInfo.put(Dict.GROUP, groupName);       //小组
            userInfo.put("groupId", groupId);       //小组
            userInfo.put(Dict.COMPANY, companyName);   //公司
            List<String> days = DateUtils.getAfterDateMap(Constants.EMAI_DATE_NUM);
            for (String day : days) {       //日期
                userInfo.put(day, 0);
            }
            String userId = (String) userItem.get(Dict.ID);
            //获取到当前用户的所有的任务情况
            List<LinkedHashMap> userTasks = ftaskService.queryUserTask(userId);
            loopTask:for (LinkedHashMap itemTask : userTasks) {
                if (!CommonUtils.isNullOrEmpty(itemTask.get(roleEnName))) {
                    List<LinkedHashMap> developer = (List<LinkedHashMap>) itemTask.get(roleEnName);    //获取开发人员列表
                    for (LinkedHashMap devUser : developer) {
                        //判断该任务的开发人员是否是 包含当前用户
                        if (!CommonUtils.isNullOrEmpty(devUser.get(Dict.USER_NAME_CN)) && devUser.get(Dict.USER_NAME_CN).toString().equals(userEnName)) {
                            //对比时间，资源数+1
                            String startTime = (String) itemTask.get(startTimeKey);   //计划启动
                            String endTime = (String) itemTask.get(endTimeKey); //计划内侧
                            if (CommonUtils.isNullOrEmpty(startTime) || CommonUtils.isNullOrEmpty(endTime))
                                continue loopTask;
                            for (String tempTime : days) {      //遍历5个日期
                                Integer tempTimeNum = (Integer) userInfo.get(tempTime);
                                if (tempTime.compareTo(startTime) >=0 && tempTime.compareTo(endTime) <= 0 ) //当前遍历的日期 大于开始， 小于等于结束，则计数加1
                                    userInfo.put(tempTime, ++tempTimeNum);
                            }
                        }
                    }
                }
            }
            developList.add(userInfo);
        }

        return developList;
    }


}
