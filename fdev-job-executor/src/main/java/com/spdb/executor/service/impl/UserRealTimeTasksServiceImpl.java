package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Constants;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.action.AllUserTasksAction;
import com.spdb.executor.service.FuserService;
import com.spdb.executor.service.MailService;
import com.spdb.executor.service.RealTimeTaskExportService;
import com.spdb.executor.service.UserRealTimeTasksService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserRealTimeTasksServiceImpl implements UserRealTimeTasksService {

    private static Logger logger = LoggerFactory.getLogger(UserRealTimeTasksServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private RealTimeTaskExportService realTimeTaskExportService;

    @Autowired
    private MailService mailService;
    @Autowired
    private FuserService fuserService;

    @Value("${fjob.email.receiver}")
    private List<String> realTimeTaskFixedReceiver;//固定收件人

    @Value("${fjob.email.filePath}")
    private String filePath;//excel存放路径

    @Value("${isSendEmail}")
    private boolean isSendEmail;



    @Override
    public void realTimeTaskNotiy(List<Map> users) throws Exception {
        List<String> userIds = new ArrayList<>();
        for (Map user : users) {
            userIds.add((String) user.get(Dict.ID));
        }
        Map creatorTasks = new HashMap();
        Map testTasks = new HashMap();
        Map developTasks = new HashMap();
        Map masterTasks = new HashMap();
        Map spdb_managerTasks = new HashMap();
//        姓名  公司  小组  待实施任务数量  开发中任务数量
        String[] roles = {Dict.CREATOR, Dict.DEVELOPER, Dict.TESTER, Dict.MASTER, Dict.SPDB_MASTER};
        for (int i = 0; i < 5; i++) {
            Map map = new HashMap();
            map.put(Dict.IDS, userIds);
            map.put(Dict.ROLES, new String[]{roles[i]});
            map.put(Dict.REST_CODE, "queryTaskNumByMember");
            if (i == 0) {
                creatorTasks = (Map) restTransport.submit(map);
            }
            if (i == 1) {
                developTasks = (Map) restTransport.submit(map);
            }
            if (i == 2) {
                testTasks = (Map) restTransport.submit(map);
            }
            if (i == 3) {
                masterTasks = (Map) restTransport.submit(map);
            }
            if (i == 4) {
                spdb_managerTasks = (Map) restTransport.submit(map);
            }
        }



//        姓名  公司  小组  待实施任务数量  开发中任务数量  SIT任务数量  UAT任务数量 REL任务数量
        List<String> needRoles = new ArrayList();
        String kelanManange = fuserService.queryRoleByName("厂商项目负责人").get("id").toString();
        String spdbManange = fuserService.queryRoleByName("行内项目负责人").get("id").toString();
        String testerRole = fuserService.queryRoleByName("测试人员").get("id").toString();
        String devlopRole = fuserService.queryRoleByName("开发人员").get("id").toString();

        needRoles.add(kelanManange);
        needRoles.add(spdbManange);
        //创建人sheet
        List<Map> creatorRealTimeTasks = getRealTimeTasks(users, creatorTasks, needRoles);
        //任务负责人sheet
        List<Map> masterRealTimeTasks = getRealTimeTasks(users, masterTasks, needRoles);

        needRoles = new ArrayList();
        needRoles.add(testerRole);
        //测试人员sheet
        List<Map> testerRealTimeTasks = getRealTimeTasks(users, testTasks, needRoles);

        needRoles = new ArrayList();
        needRoles.add(devlopRole);
        //开发人员sheet
        List<Map> developRealTimeTasks = getRealTimeTasks(users, developTasks, needRoles);

        needRoles = new ArrayList();
        needRoles.add(spdbManange);
        //行内负责人sheet
        List<Map> spdb_managerRealTimeTasks = getRealTimeTasks(users, spdb_managerTasks, needRoles);
//生成excel文件,导出到服务器指定目录下
        String excelPath_dev = realTimeTaskExportService.realTimeTaskExport(creatorRealTimeTasks, testerRealTimeTasks, developRealTimeTasks, masterRealTimeTasks, spdb_managerRealTimeTasks, filePath);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(excelPath_dev);
        HashMap model = new HashMap();
        if (!CommonUtils.isNullOrEmpty(filePaths)) {
            //发送邮件
            if (isSendEmail) {
                try {
                    mailService.sendEmail(Constants.EMAI_REALTIMETASK, model, realTimeTaskFixedReceiver, filePaths);
                } catch (Exception e) {
                    logger.error("#########发送全量人员的实时任务数量邮件失败########" + e.getMessage());
                    throw new FdevException("#########发送全量人员的实时任务数量邮件失败########");
                }
            }
        }
    }

    private List<Map> getRealTimeTasks(List<Map> users, Map roleMap, List<String> roleIds) throws Exception {
        List<Map> realTimeTasks = new ArrayList<>();
        for (Map user : users) {
            List roles = (List) user.get("role_id");
            //当前用户没有权限这个key，直接跳过
            if (CommonUtils.isNullOrEmpty(roles))
                continue;

            Map o = (Map) roleMap.get(user.get(Dict.ID));
            // 如果当前用户在任何阶段都没有任务，则不进行显示
            if(Constants.ZERO.equals(String.valueOf(o.get(Dict.TODO))) &&
               Constants.ZERO.equals(String.valueOf(o.get(Dict.DEVELOP))) &&
               Constants.ZERO.equals(String.valueOf(o.get(Dict.SIT))) &&
               Constants.ZERO.equals(String.valueOf(o.get(Dict.UAT))) &&
               Constants.ZERO.equals(String.valueOf(o.get(Dict.REL))) &&
               Constants.ZERO.equals(String.valueOf(o.get(Dict.PRODUCTION)))) {
                continue;
            }

            Map map = new HashMap();
            map.put(Dict.NAME_CN, user.get("user_name_cn"));
            Map map1 = new HashMap();
            map1.put(Dict.ID, user.get("company_id"));
            map1.put(Dict.REST_CODE, "queryCompany");
            List<Map> company = (List<Map>) restTransport.submit(map1);
            if (company != null && company.size() > 0) {
                for (Map map2 : company) {
                    if ("1".equals(map2.get(Dict.STATUS))) {
                        map.put(Dict.COMPANY, map2.get(Dict.NAME));
                    }
                }
            }
            Map map2 = new HashMap();
            map2.put(Dict.ID, user.get("group_id"));
            map2.put(Dict.REST_CODE, "queryGroup");
            List<Map> group = (List<Map>) restTransport.submit(map2);
            if (group != null && group.size() > 0) {
                for (Map map3 : group) {
                    if ("1".equals(map3.get(Dict.STATUS))) {
                        map.put(Dict.GROUP, map3.get(Dict.NAME));
                    }
                }
            }



            map.put(Dict.TODO, o.get(Dict.TODO));
            map.put(Dict.DEVELOP, o.get(Dict.DEVELOP));
            map.put(Dict.SIT, o.get(Dict.SIT));
            map.put(Dict.UAT, o.get(Dict.UAT));
            map.put(Dict.REL, o.get(Dict.REL));
            map.put(Dict.PRODUCTION, o.get(Dict.PRODUCTION));
            realTimeTasks.add(map);
        }
        return realTimeTasks;
    }
}
