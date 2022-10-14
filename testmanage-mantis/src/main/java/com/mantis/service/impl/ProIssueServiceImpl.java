package com.mantis.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mantis.dao.MantisFileDao;
import com.mantis.dao.ProIssueDao;
import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.entity.ProIssue;
import com.mantis.service.FtmsService;
import com.mantis.service.MantisFdevService;
import com.mantis.service.ProIssueService;
import com.mantis.service.UserService;
import com.mantis.util.GroupTreeUtil;
import com.mantis.util.MantisRestTemplate;
import com.mantis.util.MyUtil;
import com.mantis.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class ProIssueServiceImpl implements ProIssueService {
    @Autowired
    private ProIssueDao proIssueDao;

    private static final Logger logger = LoggerFactory.getLogger(ProIssueServiceImpl.class);
    @Autowired
    private FtmsService ftmsService;
    @Autowired
    private MantisFdevService fdevService;
    @Autowired
    private MantisRestTemplate restTemplate;
    @Autowired
    private MantisFileDao mantisFileDao;
    @Value("${manits.issue.update.url}")
    private String mantis_url;
    @Value("${manits.admin.token}")
    private String adminToken;
    @Autowired
    private RestTransport restTransport;

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private GroupTreeUtil groupTreeUtil;

    @Autowired
    private UserService iUserApi;
    @Autowired
    private MyUtil myUtil;

    private static final String[] staticProblemType = new String[]{"需求分析", "开发", "代码审核", "数据库审核", "内测", "业测", "打包", "其他"};



    private String changeNameCn(String name_en, Set<String> companys) throws Exception {
        String name_cn = "";
        if (!Utils.isEmpty(name_en)) {
            String[] user_name_list = name_en.split(";");
            for (String user_name_en : user_name_list) {
                Map user = fdevService.queryUser(user_name_en);
                if (Util.isNullOrEmpty(user)) {
                    name_cn = name_en;
                    continue;
                }
                String user_name_cn = (String) user.get(Dict.USER_NAME_CN);
                Map company = (Map) user.get(Dict.COMPANY);
                if (Util.isNullOrEmpty(company)) {
                    name_cn = name_en;
                    continue;
                }
                String company_name = (String) company.get(Dict.NAME);
                companys.add(company_name);
                if (Util.isNullOrEmpty(name_cn)) {
                    name_cn = company_name + "-" + user_name_cn;
                } else {
                    name_cn = name_cn + "," + company_name + "-" + user_name_cn;
                }
            }
        }
        return name_cn;
    }

    private List<ProIssue> prepareData(List<ProIssue> proIssues) throws Exception {
        //查询相关人员中文名
        for (ProIssue proIssue : proIssues) {
            Set<String> companys = new HashSet<>();
            //开发责任人
            proIssue.setDev_responsible(changeNameCn(proIssue.getDev_responsible(),companys));
            //审核责任人
            proIssue.setAudit_responsible(changeNameCn(proIssue.getAudit_responsible(), companys));
            //内测责任人
            proIssue.setTest_responsible(changeNameCn(proIssue.getTest_responsible(), companys));
            //任务牵头责任人
            proIssue.setTask_responsible(changeNameCn(proIssue.getTask_responsible(), companys));
            //待办事项责任人1
            proIssue.setBacklog_schedule_reviewer(changeNameCn(proIssue.getBacklog_schedule_reviewer(), companys));
            //待办事项责任人2
            proIssue.setBacklog_schedule_reviewer_2(changeNameCn(proIssue.getBacklog_schedule_reviewer_2(), companys));
            //待办事项责任人3
            proIssue.setBacklog_schedule_reviewer_3(changeNameCn(proIssue.getBacklog_schedule_reviewer_3(), companys));
            //问责人1
            proIssue.setResponsible(changeNameCn(proIssue.getResponsible(), companys));
            //问责人2
            proIssue.setResponsible_2(changeNameCn(proIssue.getResponsible_2(), companys));
            //问责人3
            proIssue.setResponsible_3(changeNameCn(proIssue.getResponsible_3(), companys));
            //评审人
            proIssue.setReviewer(changeNameCn(proIssue.getReviewer(), companys));
            //应急负责人
            proIssue.setEmergency_responsible(changeNameCn(proIssue.getEmergency_responsible(), companys));
            proIssue.setCompanys(companys);
            concludeBacklogScheduleList(proIssue);
            concludeResponsibleList(proIssue);
        }
        return proIssues;
    }

    private void concludeBacklogScheduleList(ProIssue proIssue) {
        if (!Util.isNullOrEmpty(proIssue)) {
            List<Map<String, String>> backlog_schedule_list = new ArrayList<>();
            //归纳待办事项
            Map<String, String> modelMap = new HashMap<>();
            modelMap.put(Dict.BACKLOG_SCHEDULE, proIssue.getBacklog_schedule());
            modelMap.put(Dict.BACKLOG_SCHEDULE_REVIEWER, proIssue.getBacklog_schedule_reviewer());
            modelMap.put(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME, proIssue.getBacklog_schedule_complete_time());
            modelMap.put(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION, proIssue.getBacklog_schedule_current_completion());
            modelMap.put(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE, proIssue.getBacklog_schedule_complete_percentage());
            backlog_schedule_list.add(modelMap);
            if (!Util.isNullOrEmpty(proIssue.getBacklog_schedule_2())) {
                Map<String, String> map = new HashMap<>();
                map.put(Dict.BACKLOG_SCHEDULE, proIssue.getBacklog_schedule_2());
                map.put(Dict.BACKLOG_SCHEDULE_REVIEWER, proIssue.getBacklog_schedule_reviewer_2());
                map.put(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME, proIssue.getBacklog_schedule_complete_time_2());
                map.put(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION, proIssue.getBacklog_schedule_current_completion_2());
                map.put(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE, proIssue.getBacklog_schedule_complete_percentage_2());
                backlog_schedule_list.add(map);
            }
            if (!Util.isNullOrEmpty(proIssue.getBacklog_schedule_3())) {
                Map<String, String> map = new HashMap<>();
                map.put(Dict.BACKLOG_SCHEDULE, proIssue.getBacklog_schedule_3());
                map.put(Dict.BACKLOG_SCHEDULE_REVIEWER, proIssue.getBacklog_schedule_reviewer_3());
                map.put(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME, proIssue.getBacklog_schedule_complete_time_3());
                map.put(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION, proIssue.getBacklog_schedule_current_completion_3());
                map.put(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE, proIssue.getBacklog_schedule_complete_percentage_3());
                backlog_schedule_list.add(map);
            }
            proIssue.setBacklog_schedule_list(backlog_schedule_list);
        }
    }

    private void concludeResponsibleList(ProIssue proIssue) {
        if (!Util.isNullOrEmpty(proIssue)) {
            List<Map<String, String>> responsible_list = new ArrayList<>();
            Map<String, String> modelMap = new HashMap<>();
            modelMap.put(Dict.RESPONSIBLE, proIssue.getResponsible());
            modelMap.put(Dict.RESPONSIBILITY_TYPE, proIssue.getResponsibility_type());
            modelMap.put(Dict.RESPONSIBILITY_CONTENT, proIssue.getResponsibility_content());
            responsible_list.add(modelMap);
            if (!Util.isNullOrEmpty(proIssue.getResponsible_2())) {
                Map<String, String> map = new HashMap<>();
                map.put(Dict.RESPONSIBLE, proIssue.getResponsible_2());
                map.put(Dict.RESPONSIBILITY_TYPE, proIssue.getResponsibility_type_2());
                map.put(Dict.RESPONSIBILITY_CONTENT, proIssue.getResponsibility_content_2());
                responsible_list.add(map);
            }
            if (!Util.isNullOrEmpty(proIssue.getResponsible_3())) {
                Map<String, String> map = new HashMap<>();
                map.put(Dict.RESPONSIBLE, proIssue.getResponsible_3());
                map.put(Dict.RESPONSIBILITY_TYPE, proIssue.getResponsibility_type_3());
                map.put(Dict.RESPONSIBILITY_CONTENT, proIssue.getResponsibility_content_3());
                responsible_list.add(map);
            }
            proIssue.setResponsible_list(responsible_list);
        }
    }


    @Override
    public List queryUserProIssues(String user_name_en) throws Exception {
        return prepareData(proIssueDao.queryUserProIssues(user_name_en, env));
    }

    @Override
    public List queryTaskProIssues(String task_id) throws Exception {
        return prepareData(proIssueDao.queryTaskProIssues(task_id, env));
    }

    @Override
    public List queryProIssues(String current_page, String page_size, String start_time,
                               String end_time, List<String> module, String responsible_type,
                               String responsible_name_en, String deal_status, String issue_level,
                               List<String> problem_type, Boolean isIncludeChildren ,String reviewerStatus,
                               String sortParam, String sortord) throws Exception {
        Integer currentPage = Integer.parseInt(current_page);
        Integer pageSize = Integer.parseInt(page_size);
        Integer start_page = pageSize * (currentPage - 1);
        //获取选中组的中文
        List<Map> groups = iUserApi.queryAllGroup();
        List<String> moduleCh = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(module)) {
            moduleCh = getStrings(module, groups);
        }
        //如果查询包含子组，获取子组中文
        List<String> allChildsCh = new ArrayList<>();
        if (isIncludeChildren) {
            moduleCh = getAllModels(module, allChildsCh, groups);
        }
        String join = null;
        if(!Util.isNullOrEmpty(problem_type)){
            join = String.join("|", problem_type);
        }
        List<ProIssue> ProIssues = proIssueDao.queryProIssues(start_page, pageSize, start_time, end_time,
                moduleCh, responsible_type, responsible_name_en, deal_status, issue_level, env, reviewerStatus,join, sortParam, sortord);
        //对象去重
        List<ProIssue> collect = ProIssues.stream().distinct().collect(Collectors.toList());
        //拼装工单号
        for (ProIssue proIssue : collect) {
            if (!Utils.isEmpty(proIssue.getTask_no())) {
                proIssue.setWork_no(ftmsService.queryWorkNoByTaskId(proIssue.getTask_no()));
            }
        }
        return prepareData(collect);
    }

    @Override
    public String countProIssues(String start_time, String end_time, List<String> module,
                                 String responsible_type, String responsible_name_en, String deal_status, String issue_level,
                                 List<String> problem_type, Boolean isIncludeChildren ,String reviewerStatus)
            throws Exception {
        List<String> allChildsCh = new ArrayList<>();
        List<Map> groups = new ArrayList<>();
        groups = iUserApi.queryAllGroup();
        List<String> moduleCh = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(module)) {
            moduleCh = getStrings(module, groups);
        }
        if (isIncludeChildren) {
            moduleCh = getAllModels(module, allChildsCh, groups);
        }
        String join = null;
        if(!Util.isNullOrEmpty(problem_type)){
            join = String.join("|", problem_type);
        }
        List<Map> dataList = proIssueDao.countProIssues(start_time, end_time, moduleCh, responsible_type,
                responsible_name_en, deal_status, issue_level, env ,reviewerStatus ,join);
        return String.valueOf(dataList.size());
    }

    @Override
    public void exportProIssues(String start_time, String end_time, List<String> module,
                                String responsible_type, String responsible_name_en, String deal_status, String issue_level,
                                List<String> problem_type, Boolean isIncludeChildren ,String reviewerStatus,
                                List<String> ids,String sortParam, String sortord, HttpServletResponse resp) throws Exception {
        List<ProIssue> proIssues = new ArrayList<>();
        if(Util.isNullOrEmpty(ids) || ids.size() == 0){
        proIssues = queryProIssues("1", "0", start_time, end_time, module,
                    responsible_type, responsible_name_en, deal_status,
                    issue_level,problem_type, isIncludeChildren,reviewerStatus, sortParam, sortord);
        }else{
            for (String id : ids) {
                ProIssue proIssue = queryProIssueDetail(id);
                proIssues.add(proIssue);
            }
        }
        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            setCellValue(workbook, 0, 0, 0, "序号");
            setCellValue(workbook, 0, 0, 1, "问题现象");
            setCellValue(workbook, 0, 0, 2, "需求名称");
            setCellValue(workbook, 0, 0, 3, "fdev任务编号");
            setCellValue(workbook, 0, 0, 4, "投产窗口");
            setCellValue(workbook, 0, 0, 5, "发现日期");
            setCellValue(workbook, 0, 0, 6, "发现阶段");
            setCellValue(workbook, 0, 0, 7, "是否产生生产问题");
            setCellValue(workbook, 0, 0, 8, "生产问题级别");
            setCellValue(workbook, 0, 0, 9, "问题类型");
            setCellValue(workbook, 0, 0, 10, "所属小组");
            setCellValue(workbook, 0, 0, 11, "开发责任人");
            setCellValue(workbook, 0, 0, 12, "代码审核责任人");
            setCellValue(workbook, 0, 0, 13, "内测责任人");
            setCellValue(workbook, 0, 0, 14,"牵头任务责任人");
            setCellValue(workbook, 0, 0, 15, "能否UAT复现");
            setCellValue(workbook, 0, 0, 16, "能否REL复现");
            setCellValue(workbook, 0, 0, 17, "能否灰度复现");
            setCellValue(workbook, 0, 0, 18, "是否涉及紧急需求");
            setCellValue(workbook, 0, 0, 19, "评审状态");
            setCellValue(workbook, 0, 0, 20, "处理状态");
            setCellValue(workbook, 0, 0, 21, "影响范围");
            setCellValue(workbook, 0, 0, 22, "问题原因");
            setCellValue(workbook, 0, 0, 23, "改进措施");
            setCellValue(workbook, 0, 0, 24, "评审意见");
            int i = 1;
            for (ProIssue proIssue : proIssues) {
                if (Utils.isEmpty(proIssue)) {
                    continue;
                }
                setCellValue(workbook, 0, i, 0, String.valueOf(i));
                setCellValue(workbook, 0, i, 1, String.valueOf(proIssue.getProblem_phenomenon()));
                setCellValue(workbook, 0, i, 2, String.valueOf(proIssue.getRequirement_name()));
                setCellValue(workbook, 0, i, 3, String.valueOf(proIssue.getTask_no()));
                setCellValue(workbook, 0, i, 4, String.valueOf(proIssue.getRelease_node()));
                setCellValue(workbook, 0, i, 5, String.valueOf(proIssue.getOccurred_time()));
                setCellValue(workbook, 0, i, 6, String.valueOf(proIssue.getDiscover_stage()));
                setCellValue(workbook, 0, i, 7, String.valueOf(proIssue.getIs_trigger_issue()));
                setCellValue(workbook, 0, i, 8, String.valueOf(proIssue.getIssue_level()));
                setCellValue(workbook, 0, i, 9, String.valueOf(proIssue.getIssue_type()));
                setCellValue(workbook, 0, i, 10, String.valueOf(proIssue.getModule()));
                setCellValue(workbook, 0, i, 11, String.valueOf(proIssue.getDev_responsible()));
                setCellValue(workbook, 0, i, 12, String.valueOf(proIssue.getAudit_responsible()));
                setCellValue(workbook, 0, i, 13, String.valueOf(proIssue.getTest_responsible()));
                setCellValue(workbook, 0, i, 14, String.valueOf(proIssue.getTask_responsible()));
                setCellValue(workbook, 0, i, 15, MyUtil.transformString(proIssue.getIs_uat_replication()));
                setCellValue(workbook, 0, i, 16, MyUtil.transformString(proIssue.getIs_uat_replication()));
                setCellValue(workbook, 0, i, 17, MyUtil.transformString(proIssue.getIs_uat_replication()));
                setCellValue(workbook, 0, i, 18, MyUtil.transformString(proIssue.getIs_uat_replication()));
                setCellValue(workbook, 0, i, 19, String.valueOf(proIssue.getReviewer_status()));
                setCellValue(workbook, 0, i, 20, String.valueOf(proIssue.getDeal_status()));
                setCellValue(workbook, 0, i, 21, String.valueOf(proIssue.getInfluence_area()));
                setCellValue(workbook, 0, i, 22, String.valueOf(proIssue.getIssue_reason()));
                setCellValue(workbook, 0, i, 23, String.valueOf(proIssue.getImprovement_measures()));
                setCellValue(workbook, 0, i, 24, String.valueOf(proIssue.getReviewer_comment()));
                i++;
            }
        } catch (Exception e) {
            logger.error("e" + e);
            throw new FtmsException(ErrorConstants.EXPORT_EXCEL_ERROR);
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "issues.xlsx");
        workbook.write(resp.getOutputStream());
    }

    /**
     * excel填值
     *
     * @param workbook   excel对象
     * @param sheetIndex
     * @param rowIndex
     * @param cellIndex
     * @param cellValue
     * @throws Exception
     */
    private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue)
            throws Exception {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            sheet = workbook.createSheet(String.valueOf(sheetIndex));
        }
        if (sheet.getRow(rowIndex) == null) {
            sheet.createRow(rowIndex);
        }
        if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
            sheet.getRow(rowIndex).createCell(cellIndex);
        }
        sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
    }

    @Override
    public ProIssue queryProIssueDetail(String id) throws Exception {
        ProIssue proIssue = proIssueDao.queryProIssueById(id);
        if (!Util.isNullOrEmpty(proIssue) && Util.isNullOrEmpty(proIssue.getTask_no())) {
            proIssue.setWork_no(ftmsService.queryWorkNoByTaskId(proIssue.getTask_no()));
        }
        List<ProIssue> proIssues = new ArrayList<>();
        proIssues.add(proIssue);
        return prepareData(proIssues).get(0);
    }

    @Override
    public Map queryProIssueById(String id) throws Exception {
        ProIssue proIssue = proIssueDao.queryProIssueById(id);
        concludeBacklogScheduleList(proIssue);
        concludeResponsibleList(proIssue);
        Map map = Util.beanToMap(proIssue);
        if (!Util.isNullOrEmpty(map)) {
            changePeople(map, proIssue.getDev_responsible(), Dict.DEV_RESPONSIBLE);
            changePeople(map, proIssue.getTask_responsible(), Dict.TASK_RESPONSIBLE);
            changePeople(map, proIssue.getTest_responsible(), Dict.TEST_RESPONSIBLE);
            changePeople(map, proIssue.getAudit_responsible(), Dict.AUDIT_RESPONSIBLE);
            changeSinglePepple(map, (List<Map<String, Object>>) map.get(Dict.RESPONSIBLE_LIST), Dict.RESPONSIBLE);
            changeSinglePepple(map, (List<Map<String, Object>>) map.get(Dict.BACKLOG_SCHEDULE_LIST), Dict.BACKLOG_SCHEDULE_REVIEWER);
            changePeople(map, proIssue.getReviewer(), Dict.REVIEWER);
            changePeople(map, proIssue.getEmergency_responsible(), Dict.EMERGENCY_RESPONSIBLE);
            map.put(Dict.RESPONSIBILITY_LIST, map.get(Dict.RESPONSIBLE_LIST));
            map.remove(Dict.RESPONSIBLE_LIST);
            String issueType = (String) map.get(Dict.ISSUE_TYPE);
            map.put(Dict.ISSUE_TYPE, Arrays.asList(issueType.split("\\|")));
        }
        return map;
    }

    private void changeSinglePepple(Map map, List<Map<String, Object>> attr, String str) throws Exception {
        if (!Util.isNullOrEmpty(attr)) {
            for (Map<String, Object> item : attr) {
                if (!Util.isNullOrEmpty(item.get(str))) {
                    Map user = fdevService.queryUser((String) item.get(str));
                    if (Util.isNullOrEmpty(user)) {
                        continue;
                    }
                    String user_name_cn = (String) user.get(Dict.USER_NAME_CN);
                    Map<String, String> userName = new HashMap();
                    userName.put(Dict.USER_NAME_EN, (String) item.get(str));
                    userName.put(Dict.USER_NAME_CN, user_name_cn);
                    item.put(str, userName);
                }
                ;
            }
        }
    }

    private void changePeople(Map map, String attr, String str) throws Exception {
        if (!Util.isNullOrEmpty(attr)) {
            String dev_responsible = attr;
            String[] user_name_list = dev_responsible.split(";");
            List<Map<String, String>> devs = new ArrayList<>();
            for (String user_name_en : user_name_list) {
                Map user = fdevService.queryUser(user_name_en);
                if (Util.isNullOrEmpty(user)) {
                    continue;
                }
                String user_name_cn = (String) user.get(Dict.USER_NAME_CN);
                Map<String, String> userName = new HashMap();
                userName.put(Dict.USER_NAME_EN, user_name_en);
                userName.put(Dict.USER_NAME_CN, user_name_cn);
                devs.add(userName);
            }
            map.put(str, devs);
        }
    }

    @Override
    public void updateProIssue(Map<String, Object> map) throws Exception {
        String id = String.valueOf(map.get(Dict.ID));//id
        String requirement_name = (String) map.get(Dict.REQUIREMENT_NAME);//需求名称
        String task_no = (String) map.get(Dict.TASK_NO);//任务编号
        String module = (String) map.get(Dict.MODULE);//所属模块
        String occurred_time = (String) map.get(Dict.OCCURRED_TIME);//发生日期
        String is_trigger_issue = (String) map.get(Dict.IS_TRIGGER_ISSUE);//是否造成生产问题
        String problem_phenomenon = (String) map.get(Dict.PROBLEM_PHENOMENON);//问题现象
        String influence_area = (String) map.get(Dict.INFLUENCE_AREA);//影响范围
        String issue_reason = (String) map.get(Dict.ISSUE_REASON);//问题原因
        List<String> dev_responsible = new ArrayList<>();
        dev_responsible = (List<String>) map.get(Dict.DEV_RESPONSIBLE);//开发责任人
        List<String> audit_responsible = new ArrayList<>();
        audit_responsible = (List<String>) map.get(Dict.AUDIT_RESPONSIBLE); //代码审核责任人
        List<String> test_responsible = new ArrayList<>();
        test_responsible = (List<String>) map.get(Dict.TEST_RESPONSIBLE); //内测责任人
        List<String> task_responsible = new ArrayList<>();
        task_responsible = (List<String>) map.get(Dict.TASK_RESPONSIBLE); //牵头任务责任人
        List<String> issue_types = (List<String>) map.get(Dict.ISSUE_TYPE); //问题类型
        String issue_type = StringUtils.join(issue_types, "|");
        String discover_stage = (String) map.get(Dict.DISCOVER_STAGE);//发现阶段
        String is_uat_replication = (String) map.get(Dict.IS_UAT_REPLICATION);//能否uat复现
        String is_rel_replication = (String) map.get(Dict.IS_REL_REPLICATION);//能否rel复现
        String is_gray_replication = (String) map.get(Dict.IS_GRAY_REPLICATION);//能否灰度复现
        String is_involve_urgency = (String) map.get(Dict.IS_INVOLVE_URGENCY);//是否涉及紧急需求
        String improvement_measures = (String) map.get(Dict.IMPROVEMENT_MEASURES);//改进措施
        String reviewer_comment = (String) map.get(Dict.REVIEWER_COMMENT);//评审意见
        List<Map<String, String>> responsibility_list = (List<Map<String, String>>) map.get(Dict.RESPONSIBILITY_LIST);//问责列表
        List<Map<String, String>> backlog_schedule_list = (List<Map<String, String>>) map.get(Dict.BACKLOG_SCHEDULE_LIST);//待办事项列表
        String issue_level = (String) map.get(Dict.ISSUE_LEVEL);//生产问题级别
        String reviewer_status = (String) map.get(Dict.REVIEWER_STATUS);//评审状态
        String deal_status = (String) map.get(Dict.DEAL_STATUS);//处理状态
        String remark = (String) map.get(Dict.REMARK);//备注
        String release_node = (String) map.get(Dict.RELEASE_NODE);//投产窗口
        String first_occurred_time = (String) map.get(Dict.FIRST_OCCURRED_TIME);//首次发生时间
        String location_time = (String) map.get(Dict.LOCATION_TIME);//定位时间
        String repair_time = (String) map.get(Dict.REPAIR_TIME);//修复时间
        String reviewer_time = (String) map.get(Dict.REVIEWER_TIME);//评审时间
        List<String> reviewer = (List<String>) map.get(Dict.REVIEWER);//评审人
        String emergency_process = (String) map.get(Dict.EMERGENCY_PROCESS);//应急过程
        List<String> emergency_responsible = (List<String>) map.get(Dict.EMERGENCY_RESPONSIBLE);//应急负责人
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(assemblyCustomMap(20, requirement_name));//需求名称
        list.add(assemblyCustomMap(21, module));//所属模块
        list.add(assemblyCustomMap(22, Utils.dateStrToLongModel2(occurred_time)));//发生日期
        list.add(assemblyCustomMap(23, is_trigger_issue));//是否造成生产问题
        list.add(assemblyCustomMap(24, problem_phenomenon));//问题现象
        list.add(assemblyCustomMap(25, influence_area));//影响范围
        list.add(assemblyCustomMap(26, issue_reason));//问题原因
        list.add(assemblyCustomMap(27, StringUtils.join(dev_responsible, ";")));//开发责任人
        list.add(assemblyCustomMap(29, StringUtils.join(audit_responsible, ";")));//代码审核责任人
        list.add(assemblyCustomMap(30, StringUtils.join(test_responsible, ";")));//内测责任人
        list.add(assemblyCustomMap(31, StringUtils.join(task_responsible, ";")));//牵头任务责任人
        list.add(assemblyCustomMap(32, issue_type));//问题类型
        list.add(assemblyCustomMap(33, discover_stage));//发现阶段
        list.add(assemblyCustomMap(34, improvement_measures));//改进措施
        list.add(assemblyCustomMap(35, reviewer_comment));//评审意见
        list.add(assemblyCustomMap(66, is_uat_replication));//能否uat复现
        list.add(assemblyCustomMap(67, is_rel_replication));//能否rel复现
        list.add(assemblyCustomMap(68, is_gray_replication));//能否灰度复现
        list.add(assemblyCustomMap(69, is_involve_urgency));//是否涉及紧急需求
        list.add(assemblyCustomMap(36, issue_level));//生产问题级别
        list.add(assemblyCustomMap(37, reviewer_status));//评审状态
        list.add(assemblyCustomMap(19, task_no));//任务编号
        list.add(assemblyCustomMap(71, release_node));//投产窗口
        list.add(assemblyCustomMap(74, first_occurred_time));//首次发生时间
        list.add(assemblyCustomMap(75, location_time));//定位时间
        list.add(assemblyCustomMap(76, repair_time));//修复时间
        list.add(assemblyCustomMap(77, reviewer_time));//评审时间
        list.add(assemblyCustomMap(78, StringUtils.join(reviewer, ";")));//评审人
        list.add(assemblyCustomMap(79, emergency_process));//应急过程
        list.add(assemblyCustomMap(80, StringUtils.join(emergency_responsible, ";")));//应急负责人
        if (!Util.isNullOrEmpty(remark)) {
            list.add(assemblyCustomMap(44, remark));//备注
        } else {
            list.add(assemblyCustomMap(44, ""));
        }
        list.add(assemblyCustomMap(38, deal_status));//处理状态
        addResponsibility(list, responsibility_list);
        addBacklogSchedule(list, backlog_schedule_list);
        Map<String, Object> sendMap = new HashMap<String, Object>();
        sendMap.put(Dict.CUSTOM_FIELDS, list);
        try {
            JSONObject json = new JSONObject(sendMap);
            String url = new StringBuilder(mantis_url).append("/").append(id).toString();
            restTemplate.sendPatch(url, adminToken, sendMap);
        } catch (Exception e) {
            logger.error("add issues error, task_no =" + id);
            logger.error("=====" + e.getMessage());
            throw new FtmsException(ErrorConstants.UPDATE_PRO_ISSUES_ERROR);
        }
    }

    public Map<String, Object> assemblyParamMap(String type, Object value) {
        Map<String, Object> projectMap = new HashMap<String, Object>();
        projectMap.put(type, value);
        return projectMap;
    }

    public Map<String, Object> assemblyCustomMap(Integer id, Object value) {
        Map<String, Object> customMap = new HashMap<String, Object>();
        Map<String, Integer> custom_item = new HashMap<String, Integer>();
        custom_item.put(Dict.ID, id);
        customMap.put(Dict.FIELD, custom_item);
        customMap.put(Dict.VALUE, value);
        return customMap;
    }

    private void addResponsibility(List<Map<String, Object>> list, List<Map<String, String>> responsibility_list) {
        if (!Util.isNullOrEmpty(responsibility_list) && responsibility_list.size() == 0) {
            for (int i = 56; i <= 64; i++) {
                list.add(assemblyCustomMap(i, ""));
            }
        } else if (responsibility_list.size() == 1) {
            for (int i = 59; i <= 64; i++) {
                list.add(assemblyCustomMap(i, ""));
            }
        } else if (responsibility_list.size() == 2) {
            for (int i = 62; i <= 64; i++) {
                list.add(assemblyCustomMap(i, ""));
            }
        }
        if (!Util.isNullOrEmpty(responsibility_list)) {
            if (!Util.isNullOrEmpty(responsibility_list.get(0))) {
                Map<String, String> map = responsibility_list.get(0);
                String responsible = map.get(Dict.RESPONSIBLE);//问责人
                String responsibility_type = map.get(Dict.RESPONSIBILITY_TYPE);//问责类型
                String responsibility_content = map.get(Dict.RESPONSIBILITY_CONTENT);//问责内容
                if (!Util.isNullOrEmpty(responsible)) {
                    list.add(assemblyCustomMap(56, responsible));
                } else {
                    list.add(assemblyCustomMap(56, ""));
                }
                if (!Util.isNullOrEmpty(responsibility_type)) {
                    list.add(assemblyCustomMap(57, responsibility_type));
                } else {
                    list.add(assemblyCustomMap(57, ""));
                }
                if (!Util.isNullOrEmpty(responsibility_content)) {
                    list.add(assemblyCustomMap(58, responsibility_content));
                } else {
                    list.add(assemblyCustomMap(58, ""));
                }
            }
            if (responsibility_list.size() > 1 && !Util.isNullOrEmpty(responsibility_list.get(1))) {
                Map<String, String> map = responsibility_list.get(1);
                String responsible = map.get(Dict.RESPONSIBLE);//问责人
                String responsibility_type = map.get(Dict.RESPONSIBILITY_TYPE);//问责类型
                String responsibility_content = map.get(Dict.RESPONSIBILITY_CONTENT);//问责内容
                if (!Util.isNullOrEmpty(responsible)) {
                    list.add(assemblyCustomMap(59, responsible));
                } else {
                    list.add(assemblyCustomMap(59, ""));
                }
                if (!Util.isNullOrEmpty(responsibility_type)) {
                    list.add(assemblyCustomMap(60, responsibility_type));
                } else {
                    list.add(assemblyCustomMap(60, ""));
                }
                if (!Util.isNullOrEmpty(responsibility_content)) {
                    list.add(assemblyCustomMap(61, responsibility_content));
                } else {
                    list.add(assemblyCustomMap(61, ""));
                }
            }
            if (responsibility_list.size() > 2 && !Util.isNullOrEmpty(responsibility_list.get(2))) {
                Map<String, String> map = responsibility_list.get(2);
                String responsible = map.get(Dict.RESPONSIBLE);//问责人
                String responsibility_type = map.get(Dict.RESPONSIBILITY_TYPE);//问责类型
                String responsibility_content = map.get(Dict.RESPONSIBILITY_CONTENT);//问责内容
                if (!Util.isNullOrEmpty(responsible)) {
                    list.add(assemblyCustomMap(62, responsible));
                } else {
                    list.add(assemblyCustomMap(62, ""));
                }
                if (!Util.isNullOrEmpty(responsibility_type)) {
                    list.add(assemblyCustomMap(63, responsibility_type));
                } else {
                    list.add(assemblyCustomMap(63, ""));
                }
                if (!Util.isNullOrEmpty(responsibility_content)) {
                    list.add(assemblyCustomMap(64, responsibility_content));
                } else {
                    list.add(assemblyCustomMap(64, ""));
                }
            }
        }
    }

    private void addBacklogSchedule(List<Map<String, Object>> list, List<Map<String, String>> backlog_schedule_list) {
        if (!Util.isNullOrEmpty(backlog_schedule_list) && backlog_schedule_list.size() == 0) {
            for (int i = 39; i <= 42; i++) {
                list.add(assemblyCustomMap(i, ""));
            }
            for (int i = 46; i <= 55; i++) {
                list.add(assemblyCustomMap(i, ""));
            }
        } else if (backlog_schedule_list.size() == 1) {
            for (int i = 46; i <= 55; i++) {
                list.add(assemblyCustomMap(i, ""));
            }
        } else if (backlog_schedule_list.size() == 2) {
            for (int i = 51; i <= 55; i++) {
                list.add(assemblyCustomMap(i, ""));
            }
        }
        if (!Util.isNullOrEmpty(backlog_schedule_list)) {
            if (!Util.isNullOrEmpty(backlog_schedule_list.get(0))) {
                Map<String, String> map = backlog_schedule_list.get(0);
                String backlog_schedule = map.get(Dict.BACKLOG_SCHEDULE);//待办事项
                String backlog_schedule_reviewer = map.get(Dict.BACKLOG_SCHEDULE_REVIEWER);//待办事项负责人
                String backlog_schedule_complete_time = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME);//待办事项完成时间
                String backlog_schedule_current_completion = map.get(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION);//待办事项当前完成情况
                String backlog_schedule_complete_percentage = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE);//待办事项完成百分比
                if (!Util.isNullOrEmpty(backlog_schedule)) {
                    list.add(assemblyCustomMap(39, backlog_schedule));
                } else {
                    list.add(assemblyCustomMap(39, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_reviewer)) {
                    list.add(assemblyCustomMap(40, backlog_schedule_reviewer));
                } else {
                    list.add(assemblyCustomMap(40, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_time)) {
                    list.add(assemblyCustomMap(41, Utils.dateStrToLongModel2(backlog_schedule_complete_time)));
                } else {
                    list.add(assemblyCustomMap(41, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_current_completion)) {
                    list.add(assemblyCustomMap(42, backlog_schedule_current_completion));
                } else {
                    list.add(assemblyCustomMap(42, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_percentage)) {
                    list.add(assemblyCustomMap(43, backlog_schedule_complete_percentage));
                } else {
                    list.add(assemblyCustomMap(43, ""));
                }
            }
            if (backlog_schedule_list.size() > 1 && !Util.isNullOrEmpty(backlog_schedule_list.get(1))) {
                Map<String, String> map = backlog_schedule_list.get(1);
                String backlog_schedule = map.get(Dict.BACKLOG_SCHEDULE);//待办事项
                String backlog_schedule_reviewer = map.get(Dict.BACKLOG_SCHEDULE_REVIEWER);//待办事项负责人
                String backlog_schedule_complete_time = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME);//待办事项完成时间
                String backlog_schedule_current_completion = map.get(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION);//待办事项当前完成情况
                String backlog_schedule_complete_percentage = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE);//待办事项完成百分比
                if (!Util.isNullOrEmpty(backlog_schedule)) {
                    list.add(assemblyCustomMap(46, backlog_schedule));
                } else {
                    list.add(assemblyCustomMap(46, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_reviewer)) {
                    list.add(assemblyCustomMap(47, backlog_schedule_reviewer));
                } else {
                    list.add(assemblyCustomMap(47, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_time)) {
                    list.add(assemblyCustomMap(48, Utils.dateStrToLongModel2(backlog_schedule_complete_time)));
                } else {
                    list.add(assemblyCustomMap(48, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_current_completion)) {
                    list.add(assemblyCustomMap(49, backlog_schedule_current_completion));
                } else {
                    list.add(assemblyCustomMap(49, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_percentage)) {
                    list.add(assemblyCustomMap(50, backlog_schedule_complete_percentage));
                } else {
                    list.add(assemblyCustomMap(50, ""));
                }
            }
            if (backlog_schedule_list.size() > 2 && !Util.isNullOrEmpty(backlog_schedule_list.get(2))) {
                Map<String, String> map = backlog_schedule_list.get(2);
                String backlog_schedule = map.get(Dict.BACKLOG_SCHEDULE);//待办事项
                String backlog_schedule_reviewer = map.get(Dict.BACKLOG_SCHEDULE_REVIEWER);//待办事项负责人
                String backlog_schedule_complete_time = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_TIME);//待办事项完成时间
                String backlog_schedule_current_completion = map.get(Dict.BACKLOG_SCHEDULE_CURRENT_COMPLETION);//待办事项当前完成情况
                String backlog_schedule_complete_percentage = map.get(Dict.BACKLOG_SCHEDULE_COMPLETE_PERCENTAGE);//待办事项完成百分比
                if (!Util.isNullOrEmpty(backlog_schedule)) {
                    list.add(assemblyCustomMap(51, backlog_schedule));
                } else {
                    list.add(assemblyCustomMap(51, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_reviewer)) {
                    list.add(assemblyCustomMap(52, backlog_schedule_reviewer));
                } else {
                    list.add(assemblyCustomMap(52, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_complete_time)) {
                    list.add(assemblyCustomMap(53, Utils.dateStrToLongModel2(backlog_schedule_complete_time)));
                } else {
                    list.add(assemblyCustomMap(53, ""));
                }
                if (!Util.isNullOrEmpty(backlog_schedule_current_completion)) {
                    list.add(assemblyCustomMap(54, backlog_schedule_current_completion));
                } else {
                    list.add(assemblyCustomMap(54, ""));
                }

                if (!Util.isNullOrEmpty(backlog_schedule_complete_percentage)) {
                    list.add(assemblyCustomMap(55, backlog_schedule_complete_percentage));
                } else {
                    list.add(assemblyCustomMap(55, ""));
                }
            }
        }
    }

    @Override
    public void addFile(Map<String, Object> map) throws Exception {
        String id = String.valueOf(map.get(Dict.ID));
        List<Map<String, String>> files = (List<Map<String, String>>) map.get(Dict.FILES);
        String url = new StringBuilder(mantis_url).append("/")
                .append(id).append("/files").toString();
        for (Map<String, String> file : files) {
            String content = file.get(Dict.CONTENT);
            String[] split = content.split("base64,");
            file.put(Dict.CONTENT, split[1]);
        }
        try {
            restTemplate.sendPost(url, adminToken, map);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.ADD_ISSUES_FILE_ERROR);
        }
    }

    @Override
    public void deleteFile(Map<String, String> map) throws Exception {
        String file_id = map.get(Dict.ID);
        try {
            mantisFileDao.deleteFile(file_id);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DELETE_ISSUES_FILE_ERROR);
        }
    }

    @Override
    public List<Map<String, Object>> queryIssueFiles(String id) throws Exception {
        return mantisFileDao.queryIssueFilesExcludeContent(id);
    }

    @Override
    public void fileDownload(String file_id, HttpServletResponse resp) throws Exception {
        Map<String, Object> file = mantisFileDao.queryFileById(file_id);
        String fileName = (String) file.get(Dict.NAME);
        String file_type = (String) file.get(Dict.FILE_TYPE);
        byte[] content_blob = (byte[]) file.get(Dict.CONTENT);
        ServletOutputStream outputStream = resp.getOutputStream();
        fileName = URLEncoder.encode(fileName, "UTF-8");
        resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        resp.setContentType("application/octet-stream");
        outputStream.write(content_blob);
    }

    @Override
    public void delete(Map<String, String> map) throws Exception {
        String id = map.get(Dict.ID);
        String url = new StringBuilder(mantis_url).append("/").append(id).toString();
        try {
            restTemplate.sendDelete(url, adminToken);
        } catch (Exception e) {
            logger.error("======" + e.getMessage() + id);
            throw new FtmsException(ErrorConstants.DELETE_PRO_ISSUES_ERROR);
        }
    }

    @Override
    public Map<String, Object> queryProByTeam(Map map) throws Exception{
        map.put("env", env);
        //x轴数据
        List<String> xAxis = (List<String>) map.get(Dict.MODULE);
        List<String> xAxisCh = new ArrayList<>();
        List<String> allChildsCh = new ArrayList<>();
        List<Map> groups = iUserApi.queryAllGroup();
        if (CollectionUtils.isNotEmpty(xAxis)) {
            xAxisCh = getStrings(xAxis, groups);
            map.put(Dict.MODULE, xAxisCh);
        }
        if ((Boolean) map.get("isIncludeChildren")) {
            allChildsCh = getAllModels(xAxis, allChildsCh, groups);
            map.put(Dict.MODULE, allChildsCh);
        }
        String newFtms = myUtil.isNewFtms();
        map.put("newFtms",newFtms);
        List<Map> dataList = proIssueDao.queryProByTeam(map);
        //y轴数据
        List<String> yAxis = (List<String>) map.get(Dict.PROBLEMTYPE);
        if (CollectionUtils.isEmpty(yAxis)) {
            yAxis = Arrays.asList(staticProblemType);
        }
        if (CollectionUtils.isEmpty(xAxis)) {
            xAxisCh = dataList.stream().map(e -> {
                return e.get(Dict.MODULE).toString();
            }).distinct().collect(Collectors.toList());
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> xyData = new ArrayList<>();
        resultMap.put(Dict.XAXIS, xAxisCh);
        resultMap.put(Dict.YAXIS, yAxis);
        if (CollectionUtils.isNotEmpty(yAxis)) {
            for (String y : yAxis) {
                if (CollectionUtils.isNotEmpty(xAxis)) {
                    List<Long> xData = new ArrayList<>();
                    for (Object x : xAxis) {
                        String xStr = (String) x;
                        List<String> allChilds = new ArrayList<>();
                        if ((Boolean) map.get(Dict.ISINCLUDECHILDREN)) {
                            List<String> oneChildsId = groupTreeUtil.getAllChilds(xStr);
                            if (CollectionUtils.isNotEmpty(oneChildsId)) {
                                allChilds = getStrings(oneChildsId, groups);
                            }
                        } else {
                            allChilds = getStrings(Arrays.asList(xStr), groups);
                        }
                        Long sum = Long.valueOf(0);
                        List<String> finalAllChilds = allChilds;
                        sum = dataList.stream().filter(e -> {
                            return finalAllChilds.contains(e.get(Dict.MODULE)) && ((String) e.get(Dict.ISSUE_TYPE)).contains(y);
                        }).mapToLong(e -> (Long) e.get("num")).sum();
                        xData.add(sum);
                    }
                    xyData.add(xData);
                } else {
                    List<Long> xData = new ArrayList<>();
                    for (Object x : xAxisCh) {
                        String xStr = (String) x;
                        List<String> allChilds = Arrays.asList(xStr);
                        Long sum = Long.valueOf(0);
                        List<String> finalAllChilds = allChilds;
                        sum = dataList.stream().filter(e -> {
                            return finalAllChilds.contains(e.get(Dict.MODULE)) && ((String) e.get(Dict.ISSUE_TYPE)).contains(y);
                        }).mapToLong(e -> (Long) e.get("num")).sum();
                        xData.add(sum);
                    }
                    xyData.add(xData);
                }
            }
        }
        resultMap.put("xyData", xyData);
        return resultMap;
    }

    @Override
    public void addProIssueReleaseNode() throws Exception {
        List list = new ArrayList();
        //pagesize 为0 查询列表所有
        List<ProIssue> proIssues = queryProIssues("1","0", "", "", list,
                "", "","",
                "",list, false,"","","");
        for (ProIssue proIssue : proIssues) {
            Map sendMap = new HashMap();
            sendMap.put(Dict.TASK_ID, proIssue.getTask_no());
            sendMap.put(Dict.REST_CODE, "fdev.relase.by.task");
            Map release = (Map)restTransport.submitSourceBack(sendMap);
            if(!Util.isNullOrEmpty(release)){
                String release_node_name = (String)release.get(Dict.RELEASE_NODE_NAME);
                proIssueDao.addProIssueReleaseNode(proIssue.getId(),release_node_name);
            }
        }
    }

    @Override
    public List releaseNodeProIssueRate(String startDate, String endDate) throws Exception {
        List<Map> result = new ArrayList();
        //查询时间范围内的所有投产窗口
        Map sendMap = new HashMap();
        sendMap.put(Dict.START_DATE, startDate);
        sendMap.put(Dict.END_DATE, endDate);
        sendMap.put(Dict.REST_CODE, "fdev.release.by.date");
        List<Map> relaseNodes = (List<Map>)restTransport.submitSourceBack(sendMap);
        Map<String, Map> nodes = nodeListToMap(relaseNodes);
        Set nodeNames = nodes.keySet();
        List<ProIssue> proIssues = proIssueDao.queryProIssueByReleaseNodeList(nodeNames, env);
        Map<String, Integer> proIssueCountMap = proIssueListToCountMap(proIssues);
        for (Map relaseNode : relaseNodes) {
            try {
                Map map = new HashMap();
                if (Util.isNullOrEmpty(proIssueCountMap.get(relaseNode.get(Dict.RELEASE_NODE_NAME)))) {
                    continue;
                }
                int issueCount = proIssueCountMap.get(relaseNode.get(Dict.RELEASE_NODE_NAME));
                map.put(Dict.RELEASE_DATE, relaseNode.get(Dict.RELEASE_DATE));
                map.put(Dict.OWNER_GROUP_NAME, relaseNode.get(Dict.OWNER_GROUP_NAME));
                sendMap.put(Dict.RELEASE_NODE_NAME, relaseNode.get(Dict.RELEASE_NODE_NAME));
                sendMap.put(Dict.REST_CODE, "fdev.release.task.list");
                List<Map> releaseTasks = (List<Map>) restTransport.submitSourceBack(sendMap);
                Set<String> taskIds = releaseTasks.stream().map(e -> ((String) e.get(Dict.TASK_ID))).collect(Collectors.toSet());
                if (taskIds.size() == 0) {
                    continue;
                }
                sendMap.put(Dict.IDS, taskIds);
                sendMap.put(Dict.REST_CODE, "fdev.task.ids.query");
                List<Map> tasks = (List<Map>) restTransport.submitSourceBack(sendMap);
                Set<Object> rqrmntNos = tasks.stream().map(e -> (e.get(Dict.RQRMNT_NO))).collect(Collectors.toSet());
                map.put(Dict.RQRCOUNT, rqrmntNos.size());
                map.put(Dict.PROISSUECOUNT, issueCount);
                map.put(Dict.RQRPROISSUERATE, MyUtil.getPercentage(issueCount, rqrmntNos.size()));
                result.add(map);
            }catch (Exception e){
                logger.error("query releaseNodeProIssueRate error");
                logger.error("e"+e);
                throw  new FtmsException(ErrorConstants.QUERY_PROISSUES_ERPORT_ERROR);
            }
        }
        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                String date1 = (String)o1.get(Dict.RELEASE_DATE);
                String date2 = (String)o2.get(Dict.RELEASE_DATE);
                if(Utils.dateStrToLong(date1) > Utils.dateStrToLong(date2)){
                    return 1;
                }else if(Utils.dateStrToLong(date1) < Utils.dateStrToLong(date2)){
                    return -1;
                }
                return 0;
            }
        });
        return result;
    }

    private Map proIssueListToCountMap(List<ProIssue> proIssues){
        Map<String, Integer> issueCountMap = new HashMap();
        for (ProIssue proIssue : proIssues) {
            if(Util.isNullOrEmpty(issueCountMap.get(proIssue.getRelease_node()))){
                issueCountMap.put(proIssue.getRelease_node(), 1);
            }else{
               int i =  issueCountMap.get(proIssue.getRelease_node());
                issueCountMap.put(proIssue.getRelease_node(), i+1);
            }
        }
        return  issueCountMap;
    }

    @Override
    public List releaseNodeProIssueType(String startDate, String endDate) throws Exception {
        List result = new ArrayList();
        Map sendMap = new HashMap();
        sendMap.put(Dict.START_DATE, startDate);
        sendMap.put(Dict.END_DATE, endDate);
        sendMap.put(Dict.REST_CODE, "fdev.release.by.date");
        List<Map> relaseNodes = (List<Map>)restTransport.submitSourceBack(sendMap);
        Map<String, Map> nodes = nodeListToMap(relaseNodes);
        Set nodeNames = nodes.keySet();
        String newFtms = myUtil.isNewFtms();
        List<ProIssue> proIssues = proIssueDao.queryProIssueByReleaseNodeList(nodeNames, env);
        Map<String, List> proIssueTypeMap = proTypeListToMap(proIssues);
        for (Map relaseNode : relaseNodes) {
            List<ProIssue> proIssueList = proIssueTypeMap.get(relaseNode.get(Dict.RELEASE_NODE_NAME));
            if(Util.isNullOrEmpty(proIssueList)){
                continue;
            }
            try {
                Map issueMap = new HashMap();
                Map typeCount = new HashMap();
                for (ProIssue issue : proIssueList) {
                    String issueType = issue.getIssue_type();
                    countIssueType(issueType, typeCount);
                }
                getIssueRate(typeCount, proIssueList.size());
                issueMap.put(Dict.RELEASE_DATE, relaseNode.get(Dict.RELEASE_DATE));
                issueMap.put(Dict.ISSUES, typeCount);
                issueMap.put(Dict.PROISSUECOUNT, proIssueList.size());
                issueMap.put(Dict.GROUPNAME, relaseNode.get(Dict.OWNER_GROUP_NAME));
                result.add(issueMap);
            }catch (Exception e){
                logger.error("query releaseNodeProIssueType error");
                logger.error("e"+e);
                throw  new FtmsException(ErrorConstants.QUERY_PROISSUES_ERPORT_ERROR);
            }
        }
        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                String date1 = (String)o1.get(Dict.RELEASE_DATE);
                String date2 = (String)o2.get(Dict.RELEASE_DATE);
                if(Utils.dateStrToLong(date1) > Utils.dateStrToLong(date2)){
                    return 1;
                }else if(Utils.dateStrToLong(date1) < Utils.dateStrToLong(date2)){
                    return -1;
                }
                return 0;
            }
        });
        return result;
    }

    private Map nodeListToMap(List<Map> relaseNodes){
        Map<String, Map> collect = relaseNodes.stream().collect(Collectors.toMap(e -> (String)e.get((Dict.RELEASE_NODE_NAME)), e -> e));
        return  collect;
    }

    private Map proTypeListToMap(List<ProIssue> proIssues){
        Map<String, List> proIssueTypeMap = new HashMap();
        for (ProIssue proIssue : proIssues) {
            if(Util.isNullOrEmpty(proIssueTypeMap.get(proIssue.getRelease_node()))){
                List<ProIssue> proIssueList = new ArrayList<>();
                proIssueList.add(proIssue);
                proIssueTypeMap.put(proIssue.getRelease_node(), proIssueList);
            }else{
                List list  = proIssueTypeMap.get(proIssue.getRelease_node());
                list.add(proIssue);
            }
        }
        return  proIssueTypeMap;
    }

    private void countIssueType(String issueType, Map map){
        String[] types = issueType.split("\\|");
        for (String type : types) {
            if(!Util.isNullOrEmpty(map.get(type))){
                Map typeMap = (Map)map.get(type);
                Integer count = (Integer)typeMap.get(Dict.COUNT);
                count += 1;
                typeMap.put(Dict.COUNT, count);
            }else{
                Map typeMap = new HashMap();
                typeMap.put(Dict.COUNT, 1);
                map.put(type, typeMap);
            }
        }
    }

    private void getIssueRate(Map map, int issueSize){
        Collection<Map> values = map.values();
        for (Map issueCount : values) {
            Integer count = (Integer)issueCount.get(Dict.COUNT);
            issueCount.put(Dict.RATE ,MyUtil.getPercentage(count, issueSize));
        }
    }

    private List<String> getStrings(List<String> xAxis, List<Map> groups) {
        List<Map> finalGroups1 = groups;
        xAxis = xAxis.stream().map(e -> {
            String id = "";
            for (Map map1 : finalGroups1) {
                if (e.equals(map1.get("id"))) {
                    id = (String) map1.get("name");
                }
            }
            return id;
        }).collect(Collectors.toList());
        return xAxis;
    }

    private List<String> getAllModels(List<String> xAxis, List<String> allChildsCh, List<Map> groups) {
        //查询所有子组id
        List<String> allChildsId = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(xAxis)) {
            for (Object x : xAxis) {
                String id = (String) x;
                List Childs = groupTreeUtil.getAllChilds(id);
                allChildsId.addAll(Childs);
            }
        }
        if (CollectionUtils.isNotEmpty(allChildsId)) {
            allChildsCh = getStrings(allChildsId, groups);
        }
        return allChildsCh;
    }
}
