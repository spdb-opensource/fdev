package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Constants;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.csii.pe.spdb.common.util.DateUtils;
import com.spdb.executor.service.FtaskService;
import com.spdb.executor.service.FuserService;
import com.spdb.executor.service.MailService;
import com.spdb.executor.service.ReportExportService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

public class WeekGroupResourceEmailAction extends BaseExecutableAction{
	private static Logger logger = LoggerFactory.getLogger(WeekGroupResourceEmailAction.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private FuserService fuserService;


    @Autowired
    private MailService emailService;
    @Autowired
    private ReportExportService reportExportService;

    @Value("${fjob.email.filePath}")
    private String filePath;
    @Value("${fjob.email.receiver}")
    private List<String>  receiver;



    @Override
    public void execute(Context context) {
        logger.info("execute WeekGroupResourceEmail begin");
        List<Map> devlopList = fuserService.getUserResource("开发人员", "developer", "plan_start_time", "plan_inner_test_time");
        List<Map> testerList = fuserService.getUserResource("测试人员", "tester", "plan_inner_test_time", "plan_uat_test_start_time");

        List<LinkedHashMap> allGroup = fuserService.queryAllGroup();   //获取到所有组信息
        List<LinkedHashMap> allGroup2 = fuserService.queryAllGroup();   //list中的map需要深拷贝才能实现不复用，所以干脆发两次接口就

        Map<String, List<Map>> excelDate = new HashMap();       //开发  -》 【人员信息】

        excelDate.put("开发人员", makeUserGroup(allGroup, devlopList));
        excelDate.put("测试人员", makeUserGroup(allGroup2, testerList));

        //组装邮件发送
        HashMap model = new HashMap();  //模版注入数据
        //邮件接收人
        List<String> to = receiver;

        //附件地址
        List<String> filePaths = new ArrayList<>();
        filePaths.add(reportExportService.weekGroupResourceToPath(filePath, excelDate));

        try {
            emailService.sendEmail("email.user.weekGroupResource", model, to, filePaths);
        } catch (Exception e) {
            logger.error("send email error:"+e);
            e.printStackTrace();
        }
        logger.info("execute WeekGroupResourceEmail end");
    }


    private List makeUserGroup(List<LinkedHashMap> allGroup, List<Map> userList){
        List<String> days = DateUtils.getAfterDateMap(Constants.EMAI_DATE_NUM);
        for (LinkedHashMap group : allGroup) {
            int groupUserNum = 0;
            for (String day : days) {       //日期
                group.put(day, 0);
            }
            List<String> groupIds = fuserService.queryChildGroup(group.get("id").toString());
            for (Map userInfo : userList) {
                //当用户的组id 与 遍历的组 id相同，则进行运算
                if (groupIds.contains(userInfo.get("groupId").toString())){
                    groupUserNum++;
                    for (String day : days) {       //日期
                        if (!userInfo.get(day).toString().equals("0"))
                            group.put(day, (int)group.get(day) + 1);
                    }
                }
            }
            group.put("totalUser", groupUserNum);
        }
        return allGroup;
    }

	
}
