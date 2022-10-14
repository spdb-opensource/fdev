package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
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
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WeekResourceEmailAction extends BaseExecutableAction{
	private static Logger logger = LoggerFactory.getLogger(WeekResourceEmailAction.class);

    @Autowired
    private RestTransport restTransport;


    @Autowired
    private FuserService fuserService;
    @Autowired
    private FtaskService ftaskService;

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
        logger.info("execute WeekResourceEmail begin");
        Map<String, List<Map>> excelDate = new HashMap();       //开发  -》 【人员信息】

        excelDate.put("开发人员", fuserService.getUserResource("开发人员", "developer", "plan_start_time", "plan_inner_test_time"));
        excelDate.put("测试人员", fuserService.getUserResource("测试人员", "tester", "plan_inner_test_time", "plan_uat_test_start_time"));

        //组装邮件发送
        HashMap model = new HashMap();  //模版注入数据
        //邮件接收人
        List<String> to = receiver;

        //附件地址
        List<String> filePaths = new ArrayList<>();
        filePaths.add(reportExportService.weekUserResourceToPath(filePath, excelDate));

        try {
            emailService.sendEmail("email.user.weekResource", model, to, filePaths);
        } catch (Exception e) {
            logger.error("send email error:"+e);
            e.printStackTrace();
        }
        logger.info("execute WeekResourceEmail end");
    }
	
}
