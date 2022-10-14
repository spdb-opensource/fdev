package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.DemandDocEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DemandBaseInfoUtil;
import com.spdb.fdev.fdemand.base.utils.MyUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.*;
import com.spdb.fdev.fdemand.spdb.dto.testorder.TestOrderDto;
import com.spdb.fdev.fdemand.spdb.dto.testorder.TestOrderQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.fdemand.spdb.vo.PageVo;
import org.apache.http.entity.ContentType;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class TestOrderServiceImpl implements ITestOrderService {

    private static final Logger logger = LoggerFactory.getLogger(TestOrderServiceImpl.class);

    @Autowired
    private ITestOrderDao testOrderDao;

    @Autowired
    private ITestOrderDao testOrder;

    @Autowired
    private ITestOrderFileDao testOrderFileDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private IIpmpUnitDao ipmpUnitDao;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IAppService appService;

    @Autowired
    ICommonBusinessService commonBusinessService;

    @Autowired
    private SendEmailDemandService sendEmailDemandService;

    @Value("${test.order.env}")
    private String testOrderEnv;

    @Value("${fdemand.doc.folder}")
    private String docFolder;

    @Value("${file.path}")
    private String filePath;

    @Value("${demand.email.filePath}")
    private String emailFilePath;

    @Value("${test.order.cc}")
    private List<String> testOrderCc;//提测单邮件固定收件人

    @Autowired
    private FdocmanageService fdocmanageService;

    @Autowired
    private IXTestUtilsService xTestUtilsService;

    @Override
    public void addTestOrder(TestOrder testOrder,MultipartFile[] requirementSpecification,
                             MultipartFile[] demandInstructionBook, MultipartFile[] otherFiles) throws Exception {
        User user = CommonUtils.getSessionUser();
        String todayDate = TimeUtil.getDateStamp(new Date());
        //查询创建时间为今天的提测单编号，并根据创建时间排序，取最晚的那条数据
        TestOrder dateTestOrder = testOrderDao.queryByCreateTime(todayDate);
        String test_order = "FDEV-CLOUD-" + todayDate + "-";
        //由FDEV-CLOUD-年-月-日-最大值-PRO
        if (CommonUtils.isNullOrEmpty(dateTestOrder)) {
            test_order += "00001" + testOrderEnv;
        } else {
            String fiunLatest = dateTestOrder.getTest_order();
            String[] fiunoLatestArray = fiunLatest.split("-");
            int no_latest ;
            //生产不带后缀 取 length-1 为数字
            if(CommonUtils.isNullOrEmpty(testOrderEnv)){
                no_latest = Integer.parseInt(fiunoLatestArray[fiunoLatestArray.length - 1]) + 1;
            }else {
                no_latest = Integer.parseInt(fiunoLatestArray[fiunoLatestArray.length - 2]) + 1;
            }
            test_order += String.format("%05d", no_latest)  + testOrderEnv;
        }
        //补充实体非页面填写的信息
        addTestOrder(testOrder);

        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        testOrder.set_id(objectId);//id
        testOrder.setId(id);//id
        testOrder.setTest_order(test_order);//提测单编号
        testOrder.setCreate_time(TimeUtil.formatTodayHs());//创建时间
        testOrder.setStatus(Constants.CREATE);//创建状态
        testOrder.setCreate_user_id(user.getId());//创建人ID

        //处理是否内测通过拉取内测报告
        innerTestResult( testOrder ) ;

        //上传附件
        upload2FileMinio( requirementSpecification, id , DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_INSTRUCTION.getValue());
        upload2FileMinio( demandInstructionBook, id ,  DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getValue());
        //其他文档不为空上传
        if( !CommonUtils.isNullOrEmpty(otherFiles) ){
            upload2FileMinio( otherFiles, id ,  DemandDocEnum.DemandDocTypeEnum.OTHER_RELATED_FILE.getValue());
        }
        //入库
        testOrderDao.addTestOrder(testOrder);
    }

    //处理是否内测通过拉取内测报告
    public void innerTestResult( TestOrder testOrder ) throws Exception {
        String workNo = testOrder.getWorkNo();
        //内测通过自动拉取内测报告
        if(!CommonUtils.isNullOrEmpty(workNo)){

            //判断内测通过文件是否已经上传
            List<TestOrderFile> testOrderFiles = testOrderFileDao.queryByTestOrderId(testOrder.getId());
            List<TestOrderFile> testOrderFileList = testOrderFiles.stream().filter(TestOrderFile ->
                    TestOrderFile.getFile_type().equals(DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getValue())).collect(Collectors.toList());
            //已有内测报告为空才需获取
            if(CommonUtils.isNullOrEmpty(testOrderFileList)){
                //获取内测通过报告文件的数据
                Map<String, Object> sitData = commonBusinessService.exportSitReportData(workNo);
                Map<String, Object> workData = commonBusinessService.queryOrderByOrderNo(workNo);

                XSSFWorkbook workbook = null;
                String fileName = "【内测报告】"+ testOrder.getTest_order() + ".xlsx";//文件名
                ByteArrayOutputStream bs = null;
                try {
                    workbook = new XSSFWorkbook();
                    if (!CommonUtils.isNullOrEmpty(sitData)) {
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setWrapText(true);
                        style.setFillBackgroundColor(new XSSFColor(Color.decode("#c0c0c0")));
                        XSSFFont font = workbook.createFont();
                        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                        style.setFont(font);
                        Sheet sheet = workbook.createSheet();
                        sheet.setDefaultColumnWidth(20);
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 3));
                        sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 3));
                        sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 3));
                        MyUtils.setCellValue(workbook, 0, 0, 0, style, "工单名称");
                        MyUtils.setCellValue(workbook, 0, 0, 1, style, (String) workData.get("mainTaskName"));

                        MyUtils.setCellValue(workbook, 0, 1, 0, "需求编号/实施单元编号");
                        MyUtils.setCellValue(workbook, 0, 1, 1, (String) workData.get("mainTaskName"));
                        MyUtils.setCellValue(workbook, 0, 1, 2, "工单编号");
                        MyUtils.setCellValue(workbook, 0, 1, 3, (String) sitData.get("workNo"));

                        MyUtils.setCellValue(workbook, 0, 2, 0, "测试小组长");
                        MyUtils.setCellValue(workbook, 0, 2, 1, (String) workData.get("groupLeader"));
                        MyUtils.setCellValue(workbook, 0, 2, 2, "测试人员");
                        MyUtils.setCellValue(workbook, 0, 2, 3, (String) workData.get("testers"));

                        MyUtils.setCellValue(workbook, 0, 3, 0, "测试结果");
                        MyUtils.setCellValue(workbook, 0, 3, 1, (String) sitData.get("testResult"));

                        MyUtils.setCellValue(workbook, 0, 4, 0, "测试范围");
                        MyUtils.setCellValue(workbook, 0, 4, 1, (String) sitData.get("testRange"));

                        int i = 5;
                        if (!CommonUtils.isNullOrEmpty(sitData.get("planData"))) {
                            List<Map<String, Object>> planDataList = (List<Map<String, Object>>) sitData.get("planData");
                            for (Map<String, Object> planData : planDataList) {
                                sheet.addMergedRegion(new CellRangeAddress(i, i, 1, 3));
                                MyUtils.setCellValue(workbook, 0, i, 0, style, "测试计划");
                                MyUtils.setCellValue(workbook, 0, i, 1, style, (String) planData.get("planName"));
                                i++;
                                MyUtils.setCellValue(workbook, 0, i, 0, "测试案例总数");
                                MyUtils.setCellValue(workbook, 0, i, 1, String.valueOf(planData.get("allCase")));
                                MyUtils.setCellValue(workbook, 0, i, 2, "执行通过数");
                                MyUtils.setCellValue(workbook, 0, i, 3, String.valueOf( planData.get("sumSucc")));
                                i++;
                                MyUtils.setCellValue(workbook, 0, i, 0, "案例执行失败数");
                                MyUtils.setCellValue(workbook, 0, i, 1, String.valueOf(planData.get("sumFail")));
                                MyUtils.setCellValue(workbook, 0, i, 2, "案例执行阻塞数");
                                MyUtils.setCellValue(workbook, 0, i, 3, String.valueOf(planData.get("sumBlock")));
                                i++;
                                MyUtils.setCellValue(workbook, 0, i, 0, "有效缺陷数");
                                MyUtils.setCellValue(workbook, 0, i, 1, (String) planData.get("validMantis"));
                                MyUtils.setCellValue(workbook, 0, i, 2, "无效缺陷数");
                                MyUtils.setCellValue(workbook, 0, i, 3, (String) planData.get("braceMantis"));
                                i++;
                                MyUtils.setCellValue(workbook, 0, i, 0, "设备信息");
                                MyUtils.setCellValue(workbook, 0, i, 1, (String) planData.get("deviceInfo"));
                                MyUtils.setCellValue(workbook, 0, i, 2, "无效用例数");
                                MyUtils.setCellValue(workbook, 0, i, 3, String.valueOf(planData.get("invalidCase")));
                                i++;
                                MyUtils.setCellValue(workbook, 0, i, 0, "计划开始时间");
                                MyUtils.setCellValue(workbook, 0, i, 1, (String) planData.get("planStartDate"));
                                MyUtils.setCellValue(workbook, 0, i, 2, "计划结束时间");
                                MyUtils.setCellValue(workbook, 0, i, 3, (String) planData.get("planEndDate"));
                                i++;
                            }
                        }
                    }

                    bs = new ByteArrayOutputStream();
                    workbook.write(bs);
                    byte[] bytes = bs.toByteArray();
                    InputStream inputStream = new ByteArrayInputStream(bytes);
                    MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
                    //上传内测报告
                    uploadTestOrderFile( new MultipartFile[]{multipartFile}, testOrder.getId() ,  DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getValue());

                } catch(Exception e){
                    logger.error("1生成内测报告失败：" + e.getMessage());
                } finally{
                    try {
                        bs.close();
                    } catch (IOException e) {
                        logger.error("2生成内测报告失败：" + e.getMessage());
                    }
                }
            }
        }
    }


    @Override
    public void submitTestOrder(Map<String, Object> params) throws Exception {
        String id = (String) params.get(Dict.ID);
        TestOrder testOrder = testOrderDao.queryTestOrderById(id);
        //修改提测单的状态
        testOrder.setSubmit_time(TimeUtil.formatTodayHs());//提交时间
        testOrder.setStatus(Constants.FDEV_TEST);//提测状态
        User user = CommonUtils.getSessionUser();
        testOrder.setTest_user_id(user.getId());//提测人ID
        completeTestOrderInfo(Collections.singletonList(testOrder),user);
        HashMap<String, Object> testOrderMap = CommonUtils.beanToHashMap(testOrder);
        //提交至云测试平台
        xTestUtilsService.submitTestOrder(testOrderMap);

        //文件同步提交
        List<TestOrderFile> testOrderFiles = testOrderFileDao.queryByTestOrderId(id);
        uploadFile2X(testOrderFiles);

        Set<String> toEmailSet = new HashSet<>();//收件人 业务 测试经理
        Set<String> ccEmailSet = new HashSet<>();//抄送人 其他
        ccEmailSet.addAll(testOrderCc);//固定抄送人
        toEmailSet.addAll(Arrays.asList(testOrder.getBusiness_email().split(";")));//业务邮箱
        ccEmailSet.add( user.getEmail() );//提测用户
        ccEmailSet.add( testOrder.getCreate_user_info().getUser_email() );//创建人员
        //提测邮件通知抄送人员
        if(!CommonUtils.isNullOrEmpty(testOrder.getTest_cc_user_info())){
            for (UserInfo userInfo : testOrder.getTest_cc_user_info()) {
                ccEmailSet.add( userInfo.getUser_email() );
            }
        }
        //测试经理用户信息
        if(!CommonUtils.isNullOrEmpty(testOrder.getTest_manager_info())){
            for (UserInfo userInfo : testOrder.getTest_manager_info()) {
                toEmailSet.add( userInfo.getUser_email() );
            }
        }
        List<String> testOrderFileList = new ArrayList<>();
        testOrderFileList.add(emailFilePath);
        for (TestOrderFile testOrderFile : testOrderFiles) {
            byte[] bytes = fdocmanageService.downloadFileByte("fdev-demand-testOrder", testOrderFile.getFile_path());
            String savePath = filePath + testOrderFile.getFile_name();
            FileOutputStream output = null;
            try {
                File file = new File(savePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                file.setWritable(true, false);
                file.setReadable(true, false);
                output = new FileOutputStream(file);
                output.write(bytes);
            } catch (Exception e) {
                logger.error("1提测单提交失败：" + e.getMessage());
            } finally {
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    logger.error("2提测单提交失败：" + e.getMessage());
                }
            }
            testOrderFileList.add( emailFilePath  + testOrderFile.getFile_name() );
        }


        sendEmailDemandService.sendSubmitTestOrder(testOrderMap,testOrderFileList,new ArrayList<>(toEmailSet),new ArrayList<>(ccEmailSet));
        //同步修改 任务时间 任务反推 研发单元 实施单元 需求
        String[] fdevUnitNos = testOrder.getFdev_implement_unit_no().split(";");
        List<Map> taskList = taskService.queryTaskByUnitNos(Arrays.asList(fdevUnitNos));
        //修改任务
        if(!CommonUtils.isNullOrEmpty(taskList)){
            for (Map task : taskList) {
                String taskId = (String) task.get(Dict.ID);
                String start_uat_test_time = TimeUtil.formatToday2();
                taskService.updateStartUatTime(taskId,start_uat_test_time);
            }
        }
        //提测成功入库
        testOrderDao.updateTestOrder(testOrder);

    }

    @Override
    public void deleteTestOrder(Map<String, Object> params) throws Exception {
        String id = (String) params.get(Dict.ID);//提测单ID
        User user = CommonUtils.getSessionUser();//用户信息
        TestOrder testOrder = testOrderDao.queryTestOrderById(id);
        String[] split = testOrder.getFdev_implement_unit_no().split(";");
        List<FdevImplementUnit> fdevImplementUnitList = implementUnitDao.queryFdevUnitList(null, null, Arrays.asList(split), null);
        boolean isTestFinish = true ;//默认没有研发单元测试完成
        for (FdevImplementUnit fdevImplementUnit : fdevImplementUnitList) {
            //判断研发单元是否有测试完成时间 若有则不可删除
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getReal_test_finish_date())){
                isTestFinish = false ;
                break;
            }
        }
        //没有研发单元测试完成
        if(isTestFinish){
            String date = TimeUtil.formatTodayHs();
            //已提测同步撤销云测试平台
            if(Constants.FDEV_TEST.equals(testOrder.getStatus())){
                Map<String, Object> map = new HashMap<>();
                map.put(Dict.ID,testOrder.getId());
                map.put(Dict.STATUS,Constants.DELETED);//撤销
                map.put(Dict.DELETED_TIME,date);
                xTestUtilsService.modifyTestOrderStatus(map);

                completeTestOrderInfo(Collections.singletonList(testOrder),user);

                //已提测的 提测单 同步发送撤销邮件
                Set<String> toEmailSet = new HashSet<>();//收件人
                Set<String> ccEmailSet = new HashSet<>();//抄送人
                ccEmailSet.addAll(testOrderCc);//固定抄送人
                toEmailSet.addAll(Arrays.asList(testOrder.getBusiness_email().split(";")));//业务邮箱
                ccEmailSet.add( user.getEmail() );//撤销用户
                ccEmailSet.add( testOrder.getCreate_user_info().getUser_email() );//创建人员
                //提测邮件通知抄送人员
                if(!CommonUtils.isNullOrEmpty(testOrder.getTest_cc_user_info())){
                    for (UserInfo userInfo : testOrder.getTest_cc_user_info()) {
                        ccEmailSet.add( userInfo.getUser_email() );
                    }
                }
                //测试经理用户信息
                if(!CommonUtils.isNullOrEmpty(testOrder.getTest_manager_info())){
                    for (UserInfo userInfo : testOrder.getTest_manager_info()) {
                        toEmailSet.add( userInfo.getUser_email() );
                    }
                }
                sendEmailDemandService.sendDeletedTestOrder(testOrder,new ArrayList<>(toEmailSet),new ArrayList<>(ccEmailSet),user);
            }
            //撤销提测单
            testOrderDao.deleteTestOrder(id,user.getId(),date);
        }else{
            throw new FdevException("该提测单下包含已测试完成的研发单元,不可删除!");
        }
    }

    @Override
    public void  updateTestOrder(TestOrder testOrder) throws Exception {
        User user = CommonUtils.getSessionUser();//用户信息
        testOrder.setUpdate_time(TimeUtil.formatTodayHs());//修改时间
        testOrder.setUpdate_user_id(user.getId());//更新人id
        //补充实体非页面填写的信息
        addTestOrder(testOrder);
        //处理是否内测通过拉取内测报告
        innerTestResult( testOrder ) ;
        //已提测同步修改云测试平台
        if(Constants.FDEV_TEST.equals(testOrder.getStatus())){
            completeTestOrderInfo(Collections.singletonList(testOrder),user);
            HashMap<String, Object> testOrderMap = CommonUtils.beanToHashMap(testOrder);
            xTestUtilsService.updateTestOrder(testOrderMap);
            //发送修改邮件
            Set<String> toEmailSet = new HashSet<>();//收件人 业务 测试经理
            Set<String> ccEmailSet = new HashSet<>();//抄送人 其他
            ccEmailSet.addAll(testOrderCc);//固定抄送人
            toEmailSet.addAll(Arrays.asList(testOrder.getBusiness_email().split(";")));//业务邮箱
            ccEmailSet.add( user.getEmail() );//修改用户
            ccEmailSet.add( testOrder.getCreate_user_info().getUser_email() );//创建人员
            //提测邮件通知抄送人员
            if(!CommonUtils.isNullOrEmpty(testOrder.getTest_cc_user_info())){
                for (UserInfo userInfo : testOrder.getTest_cc_user_info()) {
                    ccEmailSet.add( userInfo.getUser_email() );
                }
            }
            //测试经理用户信息
            if(!CommonUtils.isNullOrEmpty(testOrder.getTest_manager_info())){
                for (UserInfo userInfo : testOrder.getTest_manager_info()) {
                    toEmailSet.add( userInfo.getUser_email() );
                }
            }
            List<TestOrderFile> testOrderFiles = testOrderFileDao.queryByTestOrderId(testOrder.getId());
            List<String> testOrderFileList = new ArrayList<>();
            testOrderFileList.add(emailFilePath);
            for (TestOrderFile testOrderFile : testOrderFiles) {
                byte[] bytes = fdocmanageService.downloadFileByte("fdev-demand-testOrder", testOrderFile.getFile_path());
                String savePath = filePath + testOrderFile.getFile_name();
                FileOutputStream output = null;
                try {
                    File file = new File(savePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                    file.setWritable(true, false);
                    file.setReadable(true, false);
                    output = new FileOutputStream(file);
                    output.write(bytes);
                } catch (Exception e) {
                    logger.error("1提测单提交失败：" + e.getMessage());
                } finally {
                    try {
                        output.flush();
                        output.close();
                    } catch (IOException e) {
                        logger.error("2提测单提交失败：" + e.getMessage());
                    }
                }
                testOrderFileList.add( emailFilePath  + testOrderFile.getFile_name() );
            }
            sendEmailDemandService.sendUpdateTestOrder(testOrderMap,testOrderFileList,new ArrayList<>(toEmailSet),new ArrayList<>(ccEmailSet),user);

        }

        testOrderDao.updateTestOrder(testOrder);
    }

    public void addTestOrder(TestOrder testOrder) throws ParseException {

        String[] split = testOrder.getFdev_implement_unit_no().split(";");
        //计划业测时间 取最早
        List<FdevImplementUnit> fdevImplementUnitList = implementUnitDao.queryFdevUnitList(null, null, Arrays.asList(split), null);
        String minDate = fdevImplementUnitList.stream().map(FdevImplementUnit::getPlan_test_date).min(Comparator.comparing(date -> date)).get();
        testOrder.setPlan_test_start_date(minDate);

        //查询任务 获取涉及应用 列表
        String[] fdevUnitNos = testOrder.getFdev_implement_unit_no().split(";");
        List<Map> taskList = taskService.queryTaskByUnitNos(Arrays.asList(fdevUnitNos));
        String appName = "";
        Set<String> appIds = new HashSet<>();
        for (Map taskMap : taskList) {
            if(!CommonUtils.isNullOrEmpty(taskMap.get(Dict.PROJECT_NAME))){
                String projectName = (String) taskMap.get(Dict.PROJECT_NAME);
                if(CommonUtils.isNullOrEmpty(appName))
                    appName += projectName;
                else if(!appName.contains(projectName))
                    appName += ";" + projectName;
            }
            if(!CommonUtils.isNullOrEmpty(taskMap.get(Dict.PROJECT_ID))){
                String appId = (String) taskMap.get(Dict.PROJECT_ID);
                appIds.add( appId );
            }
        }
        String systemName = "" ;
        if(!CommonUtils.isNullOrEmpty(appIds)){
            List<Map<String, Object>> appList = appService.getAppByIdsOrGitlabIds(appIds);
            Set<String> systemIds = new HashSet<>();//系统ids
            for (Map<String, Object> app : appList) {
                systemIds.add((String) app.get(Dict.SYSTEM));
            }
            Set<String> systemNames = new HashSet<>();//系统名称
            for (String system : systemIds) {
                Map<String, String> systemMap = appService.querySystem(system);
                systemNames.add(systemMap.get(Dict.NAME));
            }
            if( !CommonUtils.isNullOrEmpty(systemNames) ){
                if( systemNames.size() == 1 ){
                    systemName = new ArrayList<>(systemNames).get(0);
                } else {
                    for (String name : systemNames) {
                        if( CommonUtils.isNullOrEmpty(systemName) ){
                            systemName = name ;
                        } else {
                            systemName += "," + name ;
                        }
                    }
                }
            }
        }
        testOrder.setSystemName(systemName);
        testOrder.setApp_name(appName);
        //查询需求类型
        testOrder.setDemand_type(demandBaseInfoDao.queryById(testOrder.getDemand_id()).getDemand_type());

    }
    @Override
    public Map<String,Object> queryCopyFlag(Map<String, Object> params) throws Exception {
        String demandId = (String) params.get(Dict.DEMAND_ID);
        //复制标识 默认不展示 0 可编辑 1 不展示 2 灰显
        Map<String,Object> copy_flag = new HashMap<>();
        User user = CommonUtils.getSessionUser();
        String userId = user.getId();
        String userEn = user.getUser_name_en();
        List<DemandBaseInfo> demandBaseInfoList = new ArrayList<>();
        //需求ID为空查询全部
        if(CommonUtils.isNullOrEmpty(demandId)){
            //查询全量需求
            demandBaseInfoList = demandBaseInfoDao.query2();
        } else {
            demandBaseInfoList.add(demandBaseInfoDao.queryById(demandId));
        }

        //判断是否需求牵头人
        if(!CommonUtils.isNullOrEmpty(demandBaseInfoList)){
            for (DemandBaseInfo demandBaseInfo : demandBaseInfoList) {
                if(!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_leader())){
                    if(demandBaseInfo.getDemand_leader().contains(userId)){
                        copy_flag.put(Dict.CODE,"0");
                        copy_flag.put(Dict.MSG,"");
                        return copy_flag;
                    }
                }

            }
        }
        List<IpmpUnit> ipmpUnitList = new ArrayList<>();
        //需求ID为空查询全部
        if(CommonUtils.isNullOrEmpty(demandId)){
            ipmpUnitList = ipmpUnitDao.queryAllIpmpUnit();
        } else {
            if(!CommonUtils.isNullOrEmpty(demandBaseInfoList)){
                DemandBaseInfo demandBaseInfo = demandBaseInfoList.get(0);
                //业务需求获取实施单元
                if(Constants.BUSINESS.equals(demandBaseInfo.getDemand_type())){
                    String oa_contact_no = demandBaseInfo.getOa_contact_no();
                    List<IpmpUnit> ipmpUnitList1 = ipmpUnitDao.queryIpmpUnitByDemandId(oa_contact_no);
                    if(!CommonUtils.isNullOrEmpty(ipmpUnitList1)){
                        ipmpUnitList.addAll(ipmpUnitList1);
                    }

                }
            }
        }
        //判断是否实施单元牵头人
        for (IpmpUnit ipmpUnit : ipmpUnitList) {
            if(!CommonUtils.isNullOrEmpty(ipmpUnit.getImplLeader())){
                if (ipmpUnit.getImplLeader().contains(userEn)) {
                    copy_flag.put(Dict.CODE, "0");
                    copy_flag.put(Dict.MSG, "");
                    return copy_flag;
                }
            }
        }
        List<FdevImplementUnit> fdevImplementUnits = new ArrayList<>();
        //需求ID为空查询全部
        if(CommonUtils.isNullOrEmpty(demandId)){
            //全量研发单元判断用户是否是研发单元牵头人
            fdevImplementUnits = demandBaseInfoDao.queryAvailableIpmpUnit();
        } else {
            List<FdevImplementUnit> fdevImplementUnitList = implementUnitDao.queryByDemandId(demandId);
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnitList)){
                fdevImplementUnits.addAll(fdevImplementUnitList);
            }
        }

        for (FdevImplementUnit fdevImplementUnit : fdevImplementUnits) {
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getImplement_leader())){
                if(fdevImplementUnit.getImplement_leader().contains(userId)){
                    copy_flag.put(Dict.CODE,"0");
                    copy_flag.put(Dict.MSG,"");
                    return copy_flag;
                }
            }
        }

        copy_flag.put(Dict.CODE,"2");
        copy_flag.put(Dict.MSG,"需求牵头人、实施单元牵头人、研发单元牵头人才可创建提测单!");
        return copy_flag;
    }

    //封装提测单信息
    private void completeTestOrderInfo(List<TestOrder> testOrderList, User user) throws Exception {
        if(!CommonUtils.isNullOrEmpty(testOrderList)){
            //全量涉及用户信息
            Set<String> leaderSet = new HashSet<>();
            for (TestOrder testOrder : testOrderList) {
                if(!CommonUtils.isNullOrEmpty(testOrder.getTest_cc_user_ids()))
                    leaderSet.addAll(testOrder.getTest_cc_user_ids());//提测邮件通知抄送人员id
                if(!CommonUtils.isNullOrEmpty(testOrder.getDaily_cc_user_ids()))
                    leaderSet.addAll(testOrder.getDaily_cc_user_ids());//测试日报抄送人员id
                if(!CommonUtils.isNullOrEmpty(testOrder.getTest_user_id()))
                    leaderSet.add(testOrder.getTest_user_id());//提测用户id
                if(!CommonUtils.isNullOrEmpty(testOrder.getUpdate_user_id()))
                    leaderSet.add(testOrder.getUpdate_user_id());//修改用户id

                leaderSet.add(testOrder.getCreate_user_id());//创建人员id
            }
            //查询人员信息
            Map<String, Map> userMapAll = fdevUserService.queryByUserCoreData(leaderSet, null);

            for (TestOrder testOrder : testOrderList) {
                //用户信息
                getUserInfo(testOrder, userMapAll);
                //查询小组名称
                Map<String, Object> groupMap = roleService.queryGroup(testOrder.getGroup());
                testOrder.setGroup_cn((String)groupMap.get(Dict.GROUP_NAME));//小组名称
                String[] fdevUnitNos = testOrder.getFdev_implement_unit_no().split(";");
                List<FdevImplementUnit> fdevImplementUnitList = new ArrayList<>();
                String implement_unit_content = "";
                for (String fdevUnitNo : fdevUnitNos) {
                    //查询研发单元
                    FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByFdevNo(fdevUnitNo);
                    fdevImplementUnitList.add(fdevImplementUnit);
                    if(CommonUtils.isNullOrEmpty(implement_unit_content))
                        implement_unit_content += fdevImplementUnit .getImplement_unit_content();
                    else implement_unit_content += ";" + fdevImplementUnit .getImplement_unit_content();
                }

                testOrder.setImplement_unit_content(implement_unit_content);//研发单元内容
                //查询需求信息
                DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(testOrder.getDemand_id());
                testOrder.setOa_contact_no(demandBaseInfo.getOa_contact_no());//需求编号
                testOrder.setOa_contact_name(demandBaseInfo.getOa_contact_name());//需求名称

                if(!CommonUtils.isNullOrEmpty(user)){
                    //权限控制返回
                    boolean isDemandLeader = roleService.isDemandLeader(demandBaseInfo, user.getId());//是否需求牵头人
                    boolean isCreateUser = user.getId().equals(testOrder.getCreate_user_id());//是否创建人
                    boolean isFdevUnitListLeader = roleService.isFdevUnitListLeader(fdevImplementUnitList,user.getId());//是否研发单元牵头人
                    //编辑标识 0 可编辑 1 不展示 2 灰显
                    Map<String,Object> update_flag = new HashMap<>();
                    //提交标识  0 可编辑 1 不展示 2 灰显
                    Map<String,Object> submit_flag = new HashMap<>();
                    //删除标识 0 可编辑 1 不展示 2 灰显
                    Map<String,Object> delete_flag = new HashMap<>();
                    //文件标识 0 可编辑 1 不展示 2 灰显
                    Map<String,Object> file_flag = new HashMap<>();
                    submit_flag.put(Dict.CODE,"1");//默认不展示
                    submit_flag.put(Dict.MSG,"");
                    String status = testOrder.getStatus();//提测单状态
                    //状态为 已归档 已撤销
                    if(Constants.FILE.equals(status) || Constants.DELETED.equals(status)){
                        update_flag.put(Dict.CODE,"2");//编辑标识灰显
                        update_flag.put(Dict.MSG,"提测单状态为已归档/已撤销,不可编辑!");
                        file_flag.put(Dict.CODE,"2");//文件标识标识灰显
                        file_flag.put(Dict.MSG,"提测单状态为已归档/已撤销,不可上传/删除附件!");
                        delete_flag.put(Dict.CODE,"2");//删除标识灰显
                        delete_flag.put(Dict.MSG,"提测单状态为已归档/已撤销,不可删除!");
                    } else {
                        //判断权限 需求牵头人 | 创建人 | 研发单元牵头人
                        if( isDemandLeader || isFdevUnitListLeader || isCreateUser ){
                            update_flag.put(Dict.CODE,"0");//编辑标识亮
                            update_flag.put(Dict.MSG,"");
                            file_flag.put(Dict.CODE,"0");//文件标识亮
                            file_flag.put(Dict.MSG,"");
                            boolean isTestFinish = false ;//默认没有研发单元测试完成
                            for (FdevImplementUnit fdevImplementUnit : fdevImplementUnitList) {
                                //判断研发单元是否有测试完成时间 若有则不可删除
                                if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getReal_test_finish_date())){
                                    isTestFinish = true ;
                                    break;
                                }
                            }
                            if(isTestFinish){
                                delete_flag.put(Dict.CODE,"2");//删除标识灰显
                                delete_flag.put(Dict.MSG,"该提测单下包含已测试完成的研发单元,不可删除!");
                            }else{
                                delete_flag.put(Dict.CODE,"0");//删除标识亮
                                delete_flag.put(Dict.MSG,"");
                            }

                            //状态为 已创建 create
                            if(Constants.CREATE.equals(status)){
                                submit_flag.put(Dict.CODE,"0");//提测按钮亮
                                submit_flag.put(Dict.MSG,"");
                            }else if(Constants.FDEV_TEST.equals(status)){ //已提测、test
                                submit_flag.put(Dict.CODE,"2");//提测按钮灰显
                                submit_flag.put(Dict.MSG,"该提测单已提测!");
                            }
                        } else {
                            update_flag.put(Dict.CODE,"2");//编辑标识灰显
                            update_flag.put(Dict.MSG,"请联系需求牵头人、提测单创建人、研发单元牵头人编辑!");
                            file_flag.put(Dict.CODE,"2");//文件标识标识灰显
                            file_flag.put(Dict.MSG,"请联系需求牵头人、提测单创建人、研发单元牵头人上传/删除附件!");
                            delete_flag.put(Dict.CODE,"2");//删除标识灰显
                            delete_flag.put(Dict.MSG,"请联系需求牵头人、提测单创建人、研发单元牵头人删除!");
                            if(Constants.CREATE.equals(status)){
                                submit_flag.put(Dict.CODE,"2");//提测按钮灰显
                                submit_flag.put(Dict.MSG,"请联系需求牵头人、提测单创建人、研发单元牵头人提测!");
                            }else if(Constants.FDEV_TEST.equals(status)){ //已提测、test
                                submit_flag.put(Dict.CODE,"2");//提测按钮灰显
                                submit_flag.put(Dict.MSG,"该提测单已提测!");
                            }
                        }
                    }
                    testOrder.setUpdate_flag(update_flag);
                    testOrder.setFile_flag(file_flag);
                    testOrder.setDelete_flag(delete_flag);
                    testOrder.setSubmit_flag(submit_flag);

                }

            }
        }
    }


    public void getUserInfo(TestOrder testOrder,Map<String, Map> userMapAll) throws Exception {
        //提测用户id
        String test_user_id = testOrder.getTest_user_id();
        if(!CommonUtils.isNullOrEmpty(test_user_id)){
            if (!CommonUtils.isNullOrEmpty(userMapAll.get(test_user_id))) {
                UserInfo userInfo = new UserInfo();
                Map userMap = userMapAll.get(test_user_id);
                userInfo.setId((String) userMap.get(Dict.ID));
                userInfo.setUser_name_cn((String) userMap.get(Dict.USER_NAME_CN));
                userInfo.setUser_name_en((String) userMap.get(Dict.USER_NAME_EN));
                userInfo.setUser_email((String) userMap.get(Dict.EMAIL));
                testOrder.setTest_user_info(userInfo);
            }
        }
        String create_user_id = testOrder.getCreate_user_id();
        //创建人员id
        if(!CommonUtils.isNullOrEmpty(create_user_id)){
            if (!CommonUtils.isNullOrEmpty(userMapAll.get(create_user_id))) {
                UserInfo userInfo = new UserInfo();
                Map userMap = userMapAll.get(create_user_id);
                userInfo.setId((String) userMap.get(Dict.ID));
                userInfo.setUser_name_cn((String) userMap.get(Dict.USER_NAME_CN));
                userInfo.setUser_name_en((String) userMap.get(Dict.USER_NAME_EN));
                userInfo.setUser_email((String) userMap.get(Dict.EMAIL));
                testOrder.setCreate_user_info(userInfo);
            }
        }
        String update_user_id = testOrder.getUpdate_user_id();
        //修改用户id
        if(!CommonUtils.isNullOrEmpty(update_user_id)){
            if (!CommonUtils.isNullOrEmpty(userMapAll.get(update_user_id))) {
                UserInfo userInfo = new UserInfo();
                Map userMap = userMapAll.get(update_user_id);
                userInfo.setId((String) userMap.get(Dict.ID));
                userInfo.setUser_name_cn((String) userMap.get(Dict.USER_NAME_CN));
                userInfo.setUser_name_en((String) userMap.get(Dict.USER_NAME_EN));
                userInfo.setUser_email((String) userMap.get(Dict.EMAIL));
                testOrder.setUpdate_user_info(userInfo);
            }
        }

        //提测邮件通知抄送人员id
        List<String> test_cc_user_ids = testOrder.getTest_cc_user_ids();
        if(!CommonUtils.isNullOrEmpty(test_cc_user_ids)){
            //用户信息
            List<UserInfo> userInfoList = new ArrayList<>();
            for (String userId : test_cc_user_ids) {
                UserInfo userInfo = new UserInfo();
                if (!CommonUtils.isNullOrEmpty(userMapAll.get(userId))) {
                    Map userMap = userMapAll.get(userId);
                    userInfo.setId((String) userMap.get(Dict.ID));
                    userInfo.setUser_name_cn((String) userMap.get(Dict.USER_NAME_CN));
                    userInfo.setUser_name_en((String) userMap.get(Dict.USER_NAME_EN));
                    userInfo.setUser_email((String) userMap.get(Dict.EMAIL));
                    userInfoList.add(userInfo);
                }
            }
            testOrder.setTest_cc_user_info(userInfoList);
        }

        //测试日报抄送人员id
        List<String> daily_cc_user_ids = testOrder.getDaily_cc_user_ids();
        if(!CommonUtils.isNullOrEmpty(daily_cc_user_ids)){
            //用户信息
            List<UserInfo> userInfoList = new ArrayList<>();
            for (String userId : daily_cc_user_ids) {
                UserInfo userInfo = new UserInfo();
                if (!CommonUtils.isNullOrEmpty(userMapAll.get(userId))) {
                    Map userMap = userMapAll.get(userId);
                    userInfo.setId((String) userMap.get(Dict.ID));
                    userInfo.setUser_name_cn((String) userMap.get(Dict.USER_NAME_CN));
                    userInfo.setUser_name_en((String) userMap.get(Dict.USER_NAME_EN));
                    userInfo.setUser_email((String) userMap.get(Dict.EMAIL));
                    userInfoList.add(userInfo);
                }
            }
            testOrder.setDaily_cc_user_info(userInfoList);
        }
    }

    @Override
    public List<Map> getTestManagerInfo(Map<String, Object> params) throws Exception {
        return xTestUtilsService.getTestManagerInfo(new HashMap<>());
    }

    @Override
    public PageVo<TestOrder> queryTestOrderList(TestOrderDto dto) throws Exception {
        TestOrderQueryDto queryDto = new TestOrderQueryDto();
        BeanUtils.copyProperties(dto, queryDto);
        if (!CommonUtils.isNullOrEmpty(dto.getDemandKey())) queryDto.setDemandIds(demandBaseInfoDao.queryByKeyWord(dto.getDemandKey()).stream().map(DemandBaseInfo::getId).collect(Collectors.toSet()));
        if (!CommonUtils.isNullOrEmpty(dto.getImplKey())) queryDto.setImplNos(implementUnitDao.queryFdevUnitList(dto.getImplKey(), null, null, null).stream().map(FdevImplementUnit::getFdev_implement_unit_no).filter(x -> !CommonUtils.isNullOrEmpty(x)).collect(Collectors.toSet()));//
        if (!CommonUtils.isNullOrEmpty(dto.getIpmpKey())) queryDto.setIpmpNos(ipmpUnitDao.queryByImplUnitNum(dto.getIpmpKey()).stream().map(IpmpUnit::getImplUnitNum).collect(Collectors.toSet()));
        PageVo<TestOrder> testOrderPageVo = testOrderDao.queryTestOrder(queryDto);
        completeTestOrderInfo(testOrderPageVo.getData(), CommonUtils.getSessionUser());
        return testOrderPageVo;
    }

    @Override
    public TestOrder queryTestOrder(String id) throws Exception {
        TestOrder testOrder = testOrderDao.queryTestOrderById(id);
        completeTestOrderInfo(Collections.singletonList(testOrder), CommonUtils.getSessionUser());
        return testOrder;
    }

    @Override
    public void uploadTestOrderFile(MultipartFile[] files, String testOrderId, String fileType) throws Exception {
        TestOrder testOrder = testOrderDao.queryTestOrderById(testOrderId);
        if (Constants.DELETED.equals(testOrder.getStatus())) return;
        List<TestOrderFile> testOrderFiles = upload2FileMinio(files, testOrderId, fileType);
        if (Constants.FDEV_TEST.equals(testOrder.getStatus())) uploadFile2X(testOrderFiles);
    }

    @Override
    public List<TestOrderFile> queryTestOrderFile(String testOrderId) throws Exception {
        List<TestOrderFile> testOrderFiles = testOrderFileDao.queryByTestOrderId(testOrderId);
        setTestOrderFileUserInfo(testOrderFiles);
        setTestOrderFileType(testOrderFiles);
        return testOrderFiles;
    }

    @Override
    public void deleteTestOrderFile(List<String> ids) throws Exception {
        //过滤掉内测报告
        List<String> filePaths = testOrderFileDao.queryByIds(ids).stream().filter(file -> !DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getValue().equals(file.getFile_type())).map(TestOrderFile::getFile_path).collect(Collectors.toList());
        deleteFile2X(ids);//先发接口 失败率高的优先
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute("_USER");
        fdocmanageService.deleteFiletoMinio("fdev-demand-testOrder", filePaths, user);
        testOrderFileDao.delete(ids);
    }

    @Override
    public List<FdevImplementUnit> queryFdevUnitListByDemandId(String demandId) throws Exception {
        List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryAllFdevUnitByDemandId(demandId);
        if(CommonUtils.isNullOrEmpty(fdevImplementUnits)) return new ArrayList<>() ;//为空直接返回
        User user = CommonUtils.getSessionUser();
        //查询需求信息
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(demandId);
        String userId = user.getId();
        String userEn = user.getUser_name_en();
        //权限控制
        boolean isDemandLeader = roleService.isDemandLeader(demandBaseInfo, user.getId());//是否需求牵头人
        //是否实施单元牵头人默认不是
        boolean isIpmpUnitLeader = false;

        Set<String> leaderGroupSet = new HashSet<>() ;
        //不是需求牵头人判断是否是其中一个小组的研发单元牵头人 实施单元牵头人
        if(!isDemandLeader){
            //判断是否实施单元牵头人
            List<IpmpUnit> ipmpUnitList = ipmpUnitDao.queryIpmpUnitByDemandId(demandBaseInfo.getOa_contact_no());
            if(!CommonUtils.isNullOrEmpty(ipmpUnitList)){
                for (IpmpUnit ipmpUnit : ipmpUnitList) {
                    if(ipmpUnit.getImplLeader().contains(userEn)){
                        isIpmpUnitLeader = true;//包含 为实施单元牵头人
                        break;
                    }
                }
            }
            //不是实施单元牵头人校验是否研发单元牵头人
            if(!isIpmpUnitLeader){
                for (FdevImplementUnit fdevImplementUnit : fdevImplementUnits) {
                    if (fdevImplementUnit.getImplement_leader().contains(userId))
                        leaderGroupSet.add(fdevImplementUnit.getGroup());
                }
            }

        }
        for (FdevImplementUnit fdevImplementUnit : fdevImplementUnits) {
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getReal_inner_test_date())){
                //判断研发单元是否已有用户测试完成时间
                if(CommonUtils.isNullOrEmpty(fdevImplementUnit.getReal_test_finish_date())){
                    //判断当前用户是否有权限选择当前研发单元 是需求牵头人 实施单元牵头人 是牵头研发单元所在组的研发单元 研发单元可选
                    if( isDemandLeader || isIpmpUnitLeader || (!CommonUtils.isNullOrEmpty(leaderGroupSet)
                            && leaderGroupSet.contains(fdevImplementUnit.getGroup()))){
                        //判断研发单元是否被其他提测单选择
                        if(CommonUtils.isNullOrEmpty(testOrderDao.queryTestOrderByFdevUnitNo(fdevImplementUnit.getFdev_implement_unit_no()))){
                            //研发单元可选
                            fdevImplementUnit.setTestOrderFlag("");
                        }else{
                            //提测单不为空 已被选中
                            fdevImplementUnit.setTestOrderFlag("该研发单元已存在提测单,不可重复选择!");
                        }
                    }else {
                        //没有研发单元可选择
                        fdevImplementUnit.setTestOrderFlag("非当前研发单元需求、实施单元、研发单元牵头人不可选择!");
                    }
                }else{
                    //已经用户测试完成
                    fdevImplementUnit.setTestOrderFlag("该研发单元用户测试已完成!");
                }
            }else{
                //该研发单元尚未提交内测
                fdevImplementUnit.setTestOrderFlag("该研发单元尚未提交内测!");
            }

        }
        return fdevImplementUnits;
    }

    @Override
    public List<Map<String, Object>> queryInnerTestTab(List<String> fdevUnitNos) throws Exception {
        List<Map<String, Object>> returnList = new ArrayList<>();
        returnList.add(new HashMap<String, Object>(){{
            put(Dict.INNERTESTTAB, "内业测并行");
            put(Dict.ISOPTION, "");
            }});

        //查询任务 获取涉及应用 列表
        List<Map> taskList = taskService.queryTaskByUnitNos(fdevUnitNos);
        boolean innerTestStatus = false;//默认均未通过内测
        for (Map taskMap : taskList) {
            Map<String,Object> taskOrder = commonBusinessService.testPlanQuery( (String)taskMap.get(Dict.ID) );
            //0未分配  1开发中 2sit 3uat 4已投产 5sit uat并行 6 uat(有风险紧急上) 7 uat提测   8 无效  9 分包测试  10分包测试（含风险） 11已废弃
            //12-安全测试（内测完成）、13-安全测试（含风险）
            // 3 9 为内测完成
            if(!CommonUtils.isNullOrEmpty(taskOrder)){
                if( "3".equals(taskOrder.get(Dict.STAGESTATUS)) || "9".equals(taskOrder.get(Dict.STAGESTATUS)) ){
                    innerTestStatus = true;
                    returnList.add(new HashMap<String, Object>(){{
                        put(Dict.INNERTESTTAB, "内测通过");
                        put(Dict.ISOPTION, "");
                        put(Dict.WORKNO, taskOrder.get("work_no"));
                    }});
                    returnList.add(new HashMap<String, Object>(){{
                        put(Dict.INNERTESTTAB, "部分内测通过");
                        put(Dict.ISOPTION, "");
                        put(Dict.WORKNO, taskOrder.get("work_no"));
                    }});
                    returnList.add(new HashMap<String, Object>(){{
                        put(Dict.INNERTESTTAB, "未内测");
                        put(Dict.ISOPTION, "已有任务内测通过不可选择未内测!");
                    }});
                    break;
                }
            }
        }

        
        //内测未完成
        if( !innerTestStatus ){
            returnList.add(new HashMap<String, Object>(){{
                put(Dict.INNERTESTTAB, "内测通过");
                put(Dict.ISOPTION, "无任务通过内测不可选择!");
            }});
            returnList.add(new HashMap<String, Object>(){{
                put(Dict.INNERTESTTAB, "部分内测通过");
                put(Dict.ISOPTION, "无任务通过内测不可选择!");
            }});
            returnList.add(new HashMap<String, Object>(){{
                put(Dict.INNERTESTTAB, "未内测");
                put(Dict.ISOPTION, "");
            }});
        }
        return returnList;
    }

    @Override
    public void businessFileTestOrder(IpmpUnit ipmpUnit) throws Exception {
        String implUnitNum = ipmpUnit.getImplUnitNum();
        List<TestOrder> testOrderList = testOrderDao.queryTestOrderByImplUnitNo(implUnitNum);
        for (TestOrder order : testOrderList) {
            //查询提测单所有涉及的实施单元
            Map<String, Object> params = new HashMap<>();
            params.put(Dict.IMPLUNITNUMLIST, new HashSet<>(Arrays.asList(order.getImpl_unit_num().split(";"))));
            List<IpmpUnit> ipmpUnitList = (List<IpmpUnit>) ipmpUnitDao.queryIpmpUnitByNums(params).get(Dict.DATA);
            //判断实施单元的判断实施单元是否都有测试完成日期
            //默认测试完成
            boolean file = true ;
            for (IpmpUnit unit : ipmpUnitList) {
                //包含为空的实施单元 测试未完成
                if(CommonUtils.isNullOrEmpty(unit.getActuralTestFinishDate())){
                    file = false;
                    break;
                }
            }
            //所有实施单元 测试完成 归档提测单
            if( file ){
                String date = TimeUtil.formatTodayHs();
                Map<String, Object> map = new HashMap<>();
                map.put(Dict.ID,order.getId());
                map.put(Dict.STATUS,Constants.FILE);//归档
                map.put(Dict.FILE_TIME,date);
                xTestUtilsService.modifyTestOrderStatus(map);
                //提测单归档
                testOrderDao.fileTestOrder(order.getId(),date);

                //提测单归档 发送邮件
                //封装信息
                completeTestOrderInfo(Collections.singletonList(order),null);
                Set<String> toEmailSet = new HashSet<>();//收件人 业务 测试经理
                Set<String> ccEmailSet = new HashSet<>();//抄送人 其他
                //ccEmailSet.addAll(testOrderCc);//固定抄送人
                toEmailSet.add( order.getCreate_user_info().getUser_email() );//创建人员
                //提测邮件通知抄送人员
                if(!CommonUtils.isNullOrEmpty(order.getTest_cc_user_info())){
                    for (UserInfo userInfo : order.getTest_cc_user_info()) {
                        ccEmailSet.add( userInfo.getUser_email() );
                    }
                }
                //提测人员
                if(!CommonUtils.isNullOrEmpty(order.getTest_user_info())){
                    toEmailSet.add( order.getTest_user_info().getUser_email() );
                }
                //归档邮件不发测试经理
                /*//测试经理用户信息
                if(!CommonUtils.isNullOrEmpty(order.getTest_manager_info())){
                    for (UserInfo userInfo : order.getTest_manager_info()) {
                        toEmailSet.add( userInfo.getUser_email() );
                    }
                }*/
                sendEmailDemandService.sendFileTestOrder(order,new ArrayList<>(toEmailSet),new ArrayList<>(ccEmailSet));

            }
        }

    }


    /**
     * 上传文件到上云
     */
    private void uploadFile2X(List<TestOrderFile> testOrderFiles) throws Exception {
        setTestOrderFileUserInfo(testOrderFiles);
        xTestUtilsService.importTestOrderFile(testOrderFiles);
    }

    /**
     * 删除上云文件
     * @param testOrderFileIds
     * @throws Exception
     */
    private void deleteFile2X(Collection<String> testOrderFileIds) throws Exception {
        List<TestOrderFile> testOrderFiles = testOrderFileDao.queryByIds(testOrderFileIds);
        xTestUtilsService.delTestOrderFile(testOrderFiles.stream().map(TestOrderFile::getId).collect(Collectors.toList()));
    }

    private void setTestOrderFileUserInfo(Collection<TestOrderFile> testOrderFiles) throws Exception {
        if (!CommonUtils.isNullOrEmpty(testOrderFiles)) {
            Map<String, Map> userMap = fdevUserService.queryByUserCoreData(testOrderFiles.stream().map(TestOrderFile::getUpload_user_id).collect(Collectors.toSet()), null);
            testOrderFiles.forEach(x -> x.setUpload_user_info(new UserInfo(){{
                Map map = userMap.get(x.getUpload_user_id());
                setId(x.getUpload_user_id());
                setUser_name_cn((String) map.get(Dict.USER_NAME_CN));
                setUser_name_en((String) map.get(Dict.USER_NAME_EN));
            }}));
        }
    }
    /**
     * 研发单元提测提醒
     * @param
     * @throws Exception
     */
    @Override
    public void fdevUnitWarnDelay() throws Exception {
        //获取即将延期的研发单元
        List<FdevImplementUnit> fdevImplementUnitList = implementUnitDao.queryFdevUnitWarnOverdueList();
        if(!CommonUtils.isNullOrEmpty(fdevImplementUnitList)){
            for (FdevImplementUnit fdevImplementUnit : fdevImplementUnitList) {
                //研发单元业务测试提测当天邮件提醒牵头人
                DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(fdevImplementUnit.getDemand_id());
                Set<String> idSet = new HashSet<>();
                if(!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_leader())){
                    idSet.addAll(demandBaseInfo.getDemand_leader());
                };
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getImplement_leader())){
                    idSet.addAll(fdevImplementUnit.getImplement_leader());
                };
                if(!CommonUtils.isNullOrEmpty(idSet)){
                    //查询人员信息
                    Map<String, Map> userMapAll = fdevUserService.queryByUserCoreData(idSet, null);
                    List<String> emailList = new ArrayList<>();
                    for (String id : idSet) {
                        if (!CommonUtils.isNullOrEmpty(userMapAll.get(id))) {
                            emailList.add((String) userMapAll.get(id).get(Dict.EMAIL));
                        }
                    }
                    //发送邮件提醒
                    sendEmailDemandService.sendFdevUnitWarnDelay(fdevImplementUnit,demandBaseInfo,emailList);
                }
            }
        }
    }

    /**
     * 科技需求归档提测单
     * @param
     * @throws Exception
     */
    @Override
    public void getTechReqTestInfo() throws Exception {
        //获取所有提测状态为 已提测的科技需求 提测单
        List<TestOrder> testOrderList = testOrderDao.queryTechTestOrder();
        if(!CommonUtils.isNullOrEmpty(testOrderList)){
            Set<String> demandIds = new HashSet<>();
            for ( TestOrder testOrder : testOrderList ) {
                demandIds.add(testOrder.getDemand_id());
            }
            List<DemandBaseInfo> demandBaseInfoList = demandBaseInfoDao.queryDemandByIds(demandIds);
            List<String> demandNos = demandBaseInfoList.stream().map(DemandBaseInfo::getOa_contact_no).collect(Collectors.toList());
            List<Map> techReqTestInfoList = xTestUtilsService.getTechReqTestInfo(demandNos);
            if(!CommonUtils.isNullOrEmpty(techReqTestInfoList)){
                for (String demandNo : demandNos) {
                    for (Map map : techReqTestInfoList) {
                        if(demandNo.equals(map.get(Dict.RIM_REQ_NO))){
                            if(!CommonUtils.isNullOrEmpty(map.get(Dict.TECH_REQ_TEST_INFO))){
                                List<Map> tech_req_test_info = (List<Map>) map.get(Dict.TECH_REQ_TEST_INFO);
                                boolean testFinish = true; //默认测试完成 其中一个未通过则未通过
                                for (Map techReqTestInfo : tech_req_test_info) {
                                    if(CommonUtils.isNullOrEmpty(techReqTestInfo.get(Dict.RIM_FILL_ACTUAL_TEST_FINISH_DATE))){
                                        testFinish = false;
                                        break;
                                    }
                                }
                                //测试完成 归档提测单发送邮件
                                if( testFinish ){
                                    List<DemandBaseInfo> demandBaseInfos = demandBaseInfoList.stream().filter(demandBaseInfo -> demandNo.equals(demandBaseInfo.getOa_contact_no())).collect(Collectors.toList());
                                    DemandBaseInfo demandBaseInfo = demandBaseInfos.get(0);
                                    List<TestOrder> testOrders = testOrderDao.queryTestOrderByDemandId(demandBaseInfo.getId());
                                    String date = TimeUtil.formatTodayHs();
                                    //提测单归档 发送邮件
                                    for ( TestOrder testOrder : testOrders ) {

                                        Map<String, Object> sendMap = new HashMap<>();
                                        sendMap.put(Dict.ID,testOrder.getId());
                                        sendMap.put(Dict.STATUS,Constants.FILE);//归档
                                        sendMap.put(Dict.FILE_TIME,date);
                                        xTestUtilsService.modifyTestOrderStatus(sendMap);
                                        testOrderDao.fileTestOrder(testOrder.getId(),date);
                                        //封装信息
                                        completeTestOrderInfo(Collections.singletonList(testOrder),null);
                                        Set<String> toEmailSet = new HashSet<>();//收件人 业务 测试经理
                                        Set<String> ccEmailSet = new HashSet<>();//抄送人 其他
                                        //ccEmailSet.addAll(testOrderCc);//固定抄送人
                                        toEmailSet.add( testOrder.getCreate_user_info().getUser_email() );//创建人员
                                        //提测邮件通知抄送人员
                                        if(!CommonUtils.isNullOrEmpty(testOrder.getTest_cc_user_info())){
                                            for (UserInfo userInfo : testOrder.getTest_cc_user_info()) {
                                                ccEmailSet.add( userInfo.getUser_email() );
                                            }
                                        }
                                        //提测人员
                                        if(!CommonUtils.isNullOrEmpty(testOrder.getTest_user_info())){
                                            toEmailSet.add( testOrder.getTest_user_info().getUser_email() );
                                        }
                                        //归档邮件不发测试经理
                                       /* //测试经理用户信息
                                        if(!CommonUtils.isNullOrEmpty(testOrder.getTest_manager_info())){
                                            for (UserInfo userInfo : testOrder.getTest_manager_info()) {
                                                toEmailSet.add( userInfo.getUser_email() );
                                            }
                                        }*/
                                        sendEmailDemandService.sendFileTestOrder(testOrder,new ArrayList<>(toEmailSet),new ArrayList<>(ccEmailSet));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setTestOrderFileType(List<TestOrderFile> testOrderFiles) {
        testOrderFiles.forEach(file -> file.setFile_type(DemandBaseInfoUtil.getDocType(file.getFile_type())));
    }

    private List<TestOrderFile> upload2FileMinio(MultipartFile[] files, String testOrderId, String fileType) throws Exception {
        for (MultipartFile file : files) if (file.getSize() >= (10 * 1024 * 1024)) throw new FdevException( ErrorConstants.FILE_INFO_SIZE_ERROR_10 );
        //所有文件大小不可超过10M
        checkFilesSize( files ,  testOrderId );
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute("_USER");
        String docType = DemandBaseInfoUtil.getDocType(fileType);
        //path:环境/提测单id/文档类型/文件名
        String pathCommon = docFolder + "/" + testOrderId + "/" + docType + "/";
        List<String> paths = fdocmanageService.uploadFilestoMinio("fdev-demand-testOrder", pathCommon, files, user);
        if (CommonUtils.isNullOrEmpty(paths))
            throw new FdevException(ErrorConstants.DOC_ERROR_UPLOAD, new String[]{"上传文件失败,请重试!"});
        List<TestOrderFile> testOrderFiles = new ArrayList<TestOrderFile>() {{
            for (int i = 0; i < files.length; i++) {
                TestOrderFile testOrderFile = new TestOrderFile();
                testOrderFile.setTest_order_id(testOrderId);
                testOrderFile.setFile_type(fileType);
                testOrderFile.setUpload_time(TimeUtil.formatTodayHs());
                testOrderFile.setUpload_user_id(user.getId());
                testOrderFile.setFile_name(files[i].getOriginalFilename());
                testOrderFile.setFile_size(files[i].getSize());//文件大小
                testOrderFile.setFile_path(paths.get(i));//是顺序可以直接下标取出对应
                add(testOrderFile);
            }
        }};
        testOrderFileDao.insert(testOrderFiles);
        return testOrderFiles;
    }

    //所有文件大小不可超过10M
    private void checkFilesSize(MultipartFile[] files, String testOrderId) throws Exception {
        List<TestOrderFile> testOrderFiles = testOrderFileDao.queryByTestOrderId(testOrderId);
        //已有文件大小默认大小
        long size = 0 ;
        if( !CommonUtils.isNullOrEmpty(testOrderFiles) ){
            for (TestOrderFile testOrderFile : testOrderFiles) {
                size += testOrderFile.getFile_size();
            }
        }

        for (MultipartFile file : files) {
            size += file.getSize();
        }
        //同一提测单下上传文件大小总和不能超过10M!
        if (size > (10 * 1024 * 1024)) {
            throw new FdevException(ErrorConstants.FILE_INFO_SIZE_ERROR_10);
        }
    }
}
