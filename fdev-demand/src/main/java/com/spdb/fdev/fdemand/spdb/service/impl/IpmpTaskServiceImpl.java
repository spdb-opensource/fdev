package com.spdb.fdev.fdemand.spdb.service.impl;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DashboradUtils;
import com.spdb.fdev.fdemand.spdb.dao.IpmpTaskDao;
import com.spdb.fdev.fdemand.spdb.dto.IpmpTaskDto;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpTask;
import com.spdb.fdev.fdemand.spdb.service.IImplementUnitService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import com.spdb.fdev.fdemand.spdb.service.IpmpTaskService;
import com.spdb.fdev.transport.RestTransport;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IpmpTaskServiceImpl implements IpmpTaskService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IpmpTaskDao ipmpTaskDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IImplementUnitService unitService;

    @Autowired
    private FdevUserServiceImpl fdevUserService;

    @Override
    public IpmpTask add(IpmpTask ipmpTask) throws Exception {
        if (!roleService.isDemandManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员"});
        }
        ObjectId objectId = new ObjectId();
        ipmpTask.setId(objectId.toString());
        return ipmpTaskDao.add(ipmpTask);
    }

    @Override
    public IpmpTask deleteIpmpUnitById(Map params) throws Exception {
        if (!roleService.isDemandManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员"});
        }
        String ipmpImplementUnitNo = (String) params.get(Dict.IPMP_IMPLELMENT_UNIT_NO);
        List<FdevImplementUnit> unitList = unitService.queryImplUnitByipmpImplementUnitNo(ipmpImplementUnitNo);
        if (!CommonUtils.isNullOrEmpty(unitList)) {
            throw new FdevException(ErrorConstants.IPMP_TASK_IMPLEMENTUNIT_CANNOT_DELETE);
        }
        IpmpTask ipmpTask= findIpmpTaskById((String) params.get(Dict.ID));
       if (ipmpTask==null){
           throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{(String) params.get(Dict.ID),"该id不存在！"});
       }
        List<IpmpImplementUnit> implementPlanUnitList = ipmpTask.getIpmpImplementUnitList();
        if (!CommonUtils.isNullOrEmpty(implementPlanUnitList)) {
            Iterator<IpmpImplementUnit> iterator = implementPlanUnitList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getIpmpImplementUnitNo().equals(params.get(Dict.IPMP_IMPLELMENT_UNIT_NO))) {
                    iterator.remove();
                    break;
                }
            }
        }
        return ipmpTaskDao.update(ipmpTask);
    }

    @Override
    public IpmpTask addUnit(Map params) throws Exception {
        if (!roleService.isDemandManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员"});
        }
        String unintNo = (String) params.get(Dict.IPMP_IMPLELMENT_UNIT_NO);
        List<IpmpTask> ipmpTaskList = ipmpTaskDao.queryUnitByUnitNo(unintNo);
        if (!CommonUtils.isNullOrEmpty(ipmpTaskList)) {
            throw new FdevException("该实施单元编号已存在");
        }
        IpmpTask ipmpTask  = ipmpTaskDao.findIpmpTaskByTaskNoAndGroupId((String) params.get(Dict.GROUP_ID),(String) params.get(Dict.ID));
        IpmpImplementUnit unit = new IpmpImplementUnit();
        unit.setIpmpImplementUnitNo((String) params.get(Dict.IPMP_IMPLELMENT_UNIT_NO));
        unit.setImplementUnitContent((String) params.get(Dict.IPMP_IMPLELMENT_UNIT_CONTENT));
        if (CommonUtils.isNullOrEmpty(ipmpTask)) {
            ipmpTask = new IpmpTask();
            ObjectId objectId = new ObjectId();
            ipmpTask.setId(objectId.toString());
            ipmpTask.setGroupId((String) params.get(Dict.GROUP_ID));
            ipmpTask.setTaskNo((String) params.get(Dict.ID));
            ipmpTask.setTaskName((String) params.get(Dict.IPMP_IMPLELMENT_NAME));
            List<IpmpImplementUnit> unitList = new ArrayList<>();
            unitList.add(unit);
            ipmpTask.setIpmpImplementUnitList(unitList);
            return ipmpTaskDao.add(ipmpTask);
        } else {
            List<IpmpImplementUnit> list = ipmpTask.getIpmpImplementUnitList();
            list.add(unit);
            ipmpTask.setIpmpImplementUnitList(list);
            return ipmpTaskDao.update(ipmpTask);
        }
    }

    @Override
    public IpmpTask findIpmpTaskById(String id) {
        return ipmpTaskDao.findIpmpTaskById(id);
    }

    @Override
    public List<IpmpTask> queryIpmpTaskByGroupId(Map params) {
        String groupId = (String) params.get("groupId");
        List<IpmpTask> ipmpTaskList = ipmpTaskDao.queryIpmpTaskByGroupId(groupId);
        Map<String,String> groupList = fdevUserService.queryGroup();
        ipmpTaskList = ipmpTaskList.stream().map((IpmpTask i)->{
            //实施单元排序
            i.setIpmpImplementUnitList(i.getIpmpImplementUnitList().stream()
                    .sorted(Comparator.comparing(IpmpImplementUnit::getIpmpImplementUnitNo))
                    .collect(Collectors.toList()));
            //赋值组名称
            i.setGroupName(groupList.get(i.getGroupId()));
            return i;})
                //通过任务集编号排序
                .sorted(Comparator.comparing(IpmpTask::getTaskNo))
                //通过组名称排序
                .sorted(Comparator.comparing(IpmpTask::getGroupName))
                .collect(Collectors.toList());
        return ipmpTaskList;
    }

    @Override
    public List<IpmpImplementUnit> queryUnitByTakskId(Map params) {
        String id = (String) params.get(Dict.ID);
        try {
            return ipmpTaskDao.queryUnitByTakskId(id).getIpmpImplementUnitList();
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{id,"该任务集不存在！"});
        }
    }


    @Override
    public Map searchTaskUnit(Map params) throws Exception {
        Integer page = (Integer) params.get("page");
        Integer pageSize = (Integer) params.get("pageSize");
        String keyWord = (String) params.get("keyWord");
        List<IpmpTask> ipmpTaskList = null;
        List<String> groupIds = (List<String>) params.get("groupIds");
        //组id不为空
        if (!CommonUtils.isNullOrEmpty(groupIds)){
            ipmpTaskList = ipmpTaskDao.queryIpmpTaskByGroupIds(groupIds);
        }else {//组id为空
            ipmpTaskList = ipmpTaskDao.queryAll();
        }
        List<IpmpTaskDto> coverse = coverse(ipmpTaskList);
        Map<String,String> groupList = fdevUserService.queryGroup();
        coverse = coverse.stream()
                .map((IpmpTaskDto i)->{
                    //赋值组名称
                    i.setGroupName(groupList.get(i.getGroupId()));
                    return i;})
                //通过实施单元编号排序
                .sorted(Comparator.comparing(IpmpTaskDto::getIpmpImplementUnitNo))
                //通过任务集编号排序
                .sorted(Comparator.comparing(IpmpTaskDto::getTaskNo))
                //通过组名称排序
                .sorted(Comparator.comparing(IpmpTaskDto::getGroupName))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        //通过关键字不为空进行筛选
        if (!CommonUtils.isNullOrEmpty(keyWord)) {
            coverse = coverse.stream().filter(s -> (s.getTaskNo().contains(keyWord)
                    || s.getTaskName().contains(keyWord) || s.getImplementUnitContent().contains(keyWord)
                    || s.getIpmpImplementUnitNo().contains(keyWord))).collect(Collectors.toList());
        }
        result.put("count", coverse.stream().count());
        //分页查询
        if (null != page && null != pageSize) {
            coverse = coverse.stream().skip((page - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
        }
        result.put("list", coverse);
        return result;
    }

    private List<IpmpTaskDto> coverse(List<IpmpTask> ipmpTaskList) {
        List<IpmpTaskDto> result = new ArrayList<>();
        for (IpmpTask ipmpTask : ipmpTaskList) {
            List<IpmpImplementUnit> implement_unit_list = ipmpTask.getIpmpImplementUnitList();
            for (IpmpImplementUnit unit : implement_unit_list) {
                IpmpTaskDto ipmpTaskDto = new IpmpTaskDto();
                BeanUtils.copyProperties(ipmpTask, ipmpTaskDto);
                BeanUtils.copyProperties(unit, ipmpTaskDto);
                result.add(ipmpTaskDto);
            }
        }
        return result;
    }

    @Override
    public List findDemandGroup() throws Exception {

//        if (!CommonUtils.isNullOrEmpty(redisTemplate.opsForList().range(Dict.REDISGETGROUPS, 0, -1))) {
//            return (List<LinkedHashMap<String, String>>)redisTemplate.opsForList().range(Dict.REDISGETGROUPS, 0, -1);
//        }
        List resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, Dict.GETGROUPS);
        try {
            resDate = (List<LinkedHashMap>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR);
        }
        List resDate2 = DashboradUtils.getGroupFullName(resDate);
//        if (CommonUtils.isNullOrEmpty(redisTemplate.opsForList().range(Dict.REDISGETGROUPS, 0, -1))) {
//            redisTemplate.opsForList().rightPushAll(Dict.REDISGETGROUPS, resDate2);
//            redisTemplate.expire(Dict.REDISGETGROUPS, 6, TimeUnit.HOURS);
//        }
        return resDate2;
    }

    @Override
    public List getAllGroup() {
        List resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, "getGroups");
        try {
            resDate = (List<LinkedHashMap>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR);
        }
        return DashboradUtils.getGroupFullName(resDate);
    }

    @Override
    public IpmpTask queryGroupIdByTaskNoAndUnitNo(String taskNo, String ipmpImplementUnitNo) {
        return ipmpTaskDao.queryGroupIdByTaskNoAndUnitNo(taskNo,ipmpImplementUnitNo);
    }


}
