package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.DictEnum;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.ConfUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.*;
import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.dto.conf.ContentDto;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.spdb.fdev.fdemand.base.dict.Dict.UNDETERMINED;

/**
 * @author zhanghp4
 */
@RefreshScope
@Service
public class DemandAssessServiceImpl implements IDemandAssessService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Value("${email.demand.assess}")
    private String emailDemandAssess;

    @Value("${assess.send.email.flag}")
    private Boolean assessSendEmailFlag;

    @Value("${conf.state.final.code}")
    private String confStateFinalCode;

    @Value("${conf.assess.send.email}")
    private String confAssessSendEmail;

    @Value("${conf.assess.send.email.flag}")
    private Boolean confAssessSendEmailFlag;

    @Value("${fdev.admin.account.id}")
    private String adminAccountId;

    @Value("${demand.access.confirm.user.names}")
    private String accessConfirmEnNames;

    @Value("${demand.access.defer.user.emails}")
    private String deferUserEmails;

    @Value("${fdemand.doc.folder}")
    private String docFolder;

    @Value("${section.info}")
    private String sectionInfo;//各条线经理

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IDemandAssessDao demandAssessDao;

    @Autowired
    private IIpmpUnitDao ipmpUnitDao;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private SendEmailDemandService sendEmailDemandService;

    @Autowired
    private IFdevUserService userService;

    @Autowired
    private DictService dictService;

    @Autowired
    private ConfUtils confUtils;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    private FdocmanageService fdocmanageService;

    @Autowired
    private IDemandDocDao demandDocDao;

    @Autowired
    private IFdevFinalDateApproveDao fdevFinalDateApproveDao;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private IDocService docService;


    @Override
    public void save(DemandAssess demandAssess) throws Exception {
        /**
         * 先判断用户权限是否满足；再判断未撤销的需求编号是不是存在，存在，则不可录入；
         * 再判断需求列表的实施单元是否已同步到ipmp，若已同步，则需求状态为分析完成，
         * 否则，为分析中，并发送邮件给需求牵头人等，“需求已进入评估阶段，请尽快进行需求评估”
         */
        //校验用户权限 必须为需求管理人，直接返回用户权限不足
        if (!roleService.isDemandManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求负责人"});
        }
        String oaContactNo = demandAssess.getOa_contact_no().trim();
        DemandAssess demandAssessSelect = demandAssessDao.queryByIdOrNo(null, oaContactNo, 9);
        if (!CommonUtils.isNullOrEmpty(demandAssessSelect)) {
            throw new FdevException(ErrorConstants.DEMAND_INFO_ERROR, new String[]{oaContactNo});
        }
        //判断需求列表的实施单元是否已同步到ipmp，若已同步，则需求状态为分析完成
        List<IpmpUnit> listIpmpUnit = ipmpUnitDao.queryIpmpUnitByDemandId(oaContactNo);
        demandAssess.setDemand_status(1);
        if (!CommonUtils.isNullOrEmpty(listIpmpUnit)) {
            int count = 0;
            for (IpmpUnit ipmpUnit : listIpmpUnit) {
                if ("1".equals(ipmpUnit.getSyncFlag())) {
                    //表示已同步
                    demandAssess.setDemand_status(2);
                    demandAssess.setConfirm_from("ipmp");
                    demandAssess.setEnd_assess_date(TimeUtil.formatToday());
                    demandAssess.setAssess_days(0);
                    break;
                }
                if ("stockUnit".equalsIgnoreCase(ipmpUnit.getUsedSysCode())) {
                    count++;
                }
            }
            if (count == listIpmpUnit.size()) {
                demandAssess.setDemand_status(2);
                demandAssess.setConfirm_from("ipmp");
                demandAssess.setEnd_assess_date(TimeUtil.formatToday());
                demandAssess.setAssess_days(0);
            }
        }
        demandAssess.setCreate_time(TimeUtil.getTimeStamp(new Date()));
        User user = CommonUtils.getSessionUser();
        demandAssess.setCreate_user(user.getId());
        demandAssess.setDemand_type("business");
        DemandAssess demandAssessNew = demandAssessDao.save(demandAssess);
        if (assessSendEmailFlag) {
            if (1 == demandAssessNew.getDemand_status()) {
                //异步执行邮件发送步骤
                try {
                    sendEmailDemandService.sendEmailAssessSave(demandAssessNew.getId(), oaContactNo,
                            demandAssess.getOa_contact_name(), getDemandLeaderEmail(demandAssess.getDemand_leader()));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }

    }

    @Override
    public DemandAssess queryById(String id) throws Exception {
        // 依据id获取需求评估
        DemandAssess getDemandAssess = demandAssessDao.queryById(id);
        // 如果没有查询出任何数据，则提示出错
        if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        //处理人员和小组
        Set<String> user = new HashSet<>();
        user.add(getDemandAssess.getCreate_user());
        // 判断是否有需求牵头人
        if (!CommonUtils.isNullOrEmpty(getDemandAssess.getDemand_leader())) {
            // 如果有需求牵头人，则将需求牵头人也一起放入
            user.addAll(getDemandAssess.getDemand_leader());
            // 获取牵头需求牵头小组的中文名称，返回给前端
            getDemandAssess.setDemand_leader_group_cn((String) roleService.queryGroup(getDemandAssess.getDemand_leader_group()).get("name"));
        }
        // 依据需求编号查询需求内容
        DemandBaseInfo getDemandBaseInfo = demandBaseInfoDao.queryByOaContactNo(getDemandAssess.getOa_contact_no());
        // 依据用户id，获取用户数据
        Map<String, Map> map = roleService.queryByUserCoreData(new ArrayList<>(user));
        HashSet<UserInfo> hashUserInfo = new HashSet<>();
        // 查询需求牵头人信息
        if (!CommonUtils.isNullOrEmpty(getDemandAssess.getDemand_leader())) {
            for (String userId : getDemandAssess.getDemand_leader()) {
                UserInfo userInfo = new UserInfo();
                userInfo.setId(userId);
                if (CommonUtils.isNullOrEmpty(map.get(userId))) {
                    continue;
                }
                userInfo.setUser_name_cn((String) (map.get(userId).get("user_name_cn")));
                userInfo.setUser_name_en((String) (map.get(userId).get("user_name_en")));
                hashUserInfo.add(userInfo);
            }
        }
        getDemandAssess.setDemand_leader_info(hashUserInfo);
        UserInfo userInfoCreater = new UserInfo();
        userInfoCreater.setId(getDemandAssess.getCreate_user());
        // 获取创建人信息
        userInfoCreater.setUser_name_cn((String) (map.get(getDemandAssess.getCreate_user()).get("user_name_cn")));
        userInfoCreater.setUser_name_en((String) (map.get(getDemandAssess.getCreate_user()).get("user_name_en")));
        getDemandAssess.setCreate_user_info(userInfoCreater);
        if (CommonUtils.isNullOrEmpty(getDemandBaseInfo)) {
            getDemandAssess.setDemand_id("");
        } else {
            getDemandAssess.setDemand_id(getDemandBaseInfo.getId());
        }
        getDemandAssess.setOperate_flag("show");
        if (2 == getDemandAssess.getDemand_status()) {
            getDemandAssess.setOperate_flag("tip");
        }
        User userLogin = CommonUtils.getSessionUser();
        if ((!roleService.isDemandManager()
                && !roleService.isManagerAndDemandAssessLeader(id, userLogin.getId()))
                || 9 == getDemandAssess.getDemand_status()) {
            getDemandAssess.setOperate_flag("noshow");
        }
        // 判断当前登录用户是否拥有手动确认权限
        List<String> userEnNames = Arrays.asList(accessConfirmEnNames.split(","));
        if (userEnNames.contains(userLogin.getUser_name_en())) {
            getDemandAssess.setConfirmStatus(true);
        } else {
            getDemandAssess.setConfirmStatus(false);
        }
        if (!CommonUtils.isNullOrEmpty(getDemandAssess.getConf_url())) {
            getDemandAssess.setConf_url(confUtils.getConfUrl() + getDemandAssess.getConf_url());
        }
        // 判断之前是否有过审批记录，没有记录为0，有的话记录为1
        if (CommonUtils.isNullOrEmpty(getDemandAssess.getApprove_time_list())) {
            getDemandAssess.setFinal_date_status(0);
        } else {
            getDemandAssess.setFinal_date_status(1);
        }
        // 如果之前同步过confluence ，则不能再修改，记录为2
        if (!CommonUtils.isNullOrEmpty(getDemandAssess.getConf_url())) {
            getDemandAssess.setFinal_date_status(2);
        }
        setDictField(getDemandAssess, getDictMap());
        return getDemandAssess;
    }

    @Override
    public DemandAssess update(DemandAssess demandAssess) throws Exception {
        DemandAssess getDemandAssess = demandAssessDao.queryById(demandAssess.getId());
        if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        if (2 == getDemandAssess.getDemand_status()) {
            throw new FdevException(ErrorConstants.DEMAND_ASSESS_NOT_OPERATE, new String[]{getDemandAssess.getOa_contact_no()});
        }
        User user = CommonUtils.getSessionUser();
        if (!roleService.isDemandManager()
                && !roleService.isManagerAndDemandAssessLeader(demandAssess.getId(), user.getId())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求负责人"});
        }
        demandAssess.setUpdate_user(user.getId());
        demandAssess.setUpdate_time(TimeUtil.getTimeStamp(new Date()));
        return demandAssessDao.update(demandAssess);
    }

    @Override
    public Map<String, Object> query(DemandAssessDto dto) throws Exception {
        Map<String, Object> query = demandAssessDao.query(dto);
        List<DemandAssess> data = (List<DemandAssess>) query.get(Dict.DATA);
        if (!CommonUtils.isNullOrEmpty(data)) {
            Set<String> demandLeader = new HashSet<>();
            Set<String> demandLeaderGroup = new HashSet<>();
            data.forEach(x -> {
                if (!CommonUtils.isNullOrEmpty(x.getDemand_leader())) demandLeader.addAll(x.getDemand_leader());

                if (!CommonUtils.isNullOrEmpty(x.getDemand_leader_group()))
                    demandLeaderGroup.add(x.getDemand_leader_group());
            });
            //批量查询用户信息字典map
            Map<String, Map> userMap = !CommonUtils.isNullOrEmpty(demandLeader) ? userService.queryByUserCoreData(demandLeader, null) : new HashMap<>();
            //批量查询组信息字典map
            Map<String, String> groupNameMap = !CommonUtils.isNullOrEmpty(demandLeaderGroup) ? roleService.queryGroupByIds(demandLeaderGroup).stream().collect(Collectors.toMap(x -> x.get(Dict.ID), x -> x.get(Dict.NAME))) : new HashMap<>();
            //获取字典集
            Map<String, String> codeNameMap = dictService.getCodeNameMapByTypes(new HashSet<String>() {{
                add(DictEnum.OVERDUE_TYPE.getType());
                add(DictEnum.CONF_STATE.getType());
            }});
            User user = CommonUtils.getSessionUser();
            String userEnName = user.getUser_name_en();
            for (DemandAssess x : data) {
                // 判断当前登录用户是否拥有手动确认权限
                List<String> userEnNames = Arrays.asList(accessConfirmEnNames.split(","));
                if (userEnNames.contains(userEnName)) {
                    x.setConfirmStatus(true);
                } else {
                    x.setConfirmStatus(false);
                }
                x.setDemand_leader_group_cn(groupNameMap.get(x.getDemand_leader_group()));
                //判断是否有操作权限
                if (roleService.isDemandManager() ||
                        (!CommonUtils.isNullOrEmpty(x.getDemand_leader()) &&
                                x.getDemand_leader().contains(CommonUtils.getSessionUser().getId()))
                ) {
                    if (2 == x.getDemand_status() || 9 == x.getDemand_status()) {//有权限但是状态不满足
                        x.setOperate_flag("tip");
                    } else {
                        x.setOperate_flag("show");
                    }
                } else {//无权限不展示
                    x.setOperate_flag("noshow");
                }
                if (!CommonUtils.isNullOrEmpty(x.getDemand_leader())) {
                    x.setDemand_leader_info(new LinkedHashSet<UserInfo>(x.getDemand_leader().size()) {{
                        x.getDemand_leader().forEach(x -> {
                            if (!CommonUtils.isNullOrEmpty(userMap.get(x))) {
                                add(new UserInfo() {{
                                    setId(x);
                                    setUser_name_cn((String) (userMap.get(x).get(Dict.USER_NAME_CN)));
                                    setUser_name_en((String) (userMap.get(x).get(Dict.USER_NAME_EN)));
                                }});
                            }
                        });
                    }});
                }
                setDictField(x, codeNameMap);
                if (!CommonUtils.isNullOrEmpty(x.getDemand_type())) {
                    switch (x.getDemand_type()) {
                        case "tech":
                            x.setDemand_type("科技");
                            break;
                        case "business":
                            x.setDemand_type("业务");
                            break;
                        case "daily":
                            x.setDemand_type("日常");
                            break;
                    }
                }
                if (!CommonUtils.isNullOrEmpty(x.getDemand_status())) {
                    switch (x.getDemand_status()) {
                        case 1:
                            x.setDemand_status_name("分析中");
                            break;
                        case 2:
                            x.setDemand_status_name("分析完成");
                            break;
                        case 3:
                            x.setDemand_status_name("暂缓中");
                            break;
                        case 9:
                            x.setDemand_status_name("撤销");
                            break;
                    }
                }
                if (!CommonUtils.isNullOrEmpty(x.getPriority())) {
                    switch (x.getPriority()) {
                        case "0":
                            x.setPriority("高");
                            break;
                        case "1":
                            x.setPriority("中");
                            break;
                        case "2":
                            x.setPriority("一般");
                            break;
                        case "3":
                            x.setPriority("低");
                            break;
                    }
                }
                // 判断之前是否有过审批记录，没有记录为0，有的话记录为1
                if (CommonUtils.isNullOrEmpty(x.getApprove_time_list())) {
                    x.setFinal_date_status(0);
                } else {
                    x.setFinal_date_status(1);
                }
                // 如果之前同步过confluence ，则不能再修改，记录为2
                if (!CommonUtils.isNullOrEmpty(x.getConf_url())) {
                    x.setFinal_date_status(2);
                }
            }
        }
        return query;
    }

    @Override
    public DemandAssess delete(String id) throws Exception {
        DemandAssess getDemandAssess = demandAssessDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        if (2 == getDemandAssess.getDemand_status()) {
            throw new FdevException(ErrorConstants.DEMAND_ASSESS_NOT_OPERATE, new String[]{getDemandAssess.getOa_contact_no()});
        }
        User user = CommonUtils.getSessionUser();
        if (!roleService.isDemandManager()
                && !roleService.isManagerAndDemandAssessLeader(id, user.getId())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求负责人"});
        }
        getDemandAssess.setDelete_user(user.getId());
        getDemandAssess.setDelete_time(TimeUtil.getTimeStamp(new Date()));
        getDemandAssess.setDemand_status(9);
        return demandAssessDao.update(getDemandAssess);
    }

    /**
     * 批量更新需求的评估天数，对已撤销和已分析完成的需求不做任何计算
     * 对分析中的需求的计算公式：昨天-起始评估日期
     */
    @Override
    public void calcDemandAssessDays() {
        //查询分析中的需求
        List<DemandAssess> getDemandAssessList = demandAssessDao.queryByStatus(1);
        if (CommonUtils.isNullOrEmpty(getDemandAssessList)) {
            return;
        }
        String yesterday = TimeUtil.todayLastTwoWeek(1);
        //当评估时长为7天时，fdev邮件发送需求牵头人、吴磊、杨静：“需求编号-需求名称已评估7天，请及时推进。”
        List<DemandAssess> listDemandAssess7 = new ArrayList<>();
        //当评估时长为11天时，fdev邮件发送需求牵头人、吴磊、杨静：“需求编号-需求名称即将在3天后评估超期，请及时推进。
        List<DemandAssess> listDemandAssess11 = new ArrayList<>();
        //当评估时长超过14天时，fdev每天邮件发送需求牵头人、吴磊、杨静：“需求编号-需求名称已超期评估，累计评估XX天，请及时推进。”
        List<DemandAssess> listDemandAssess14 = new ArrayList<>();
        for (DemandAssess demandAssess : getDemandAssessList) {
            if (CommonUtils.isNullOrEmpty(demandAssess.getStart_assess_date())) continue;
            try {
                int diff = TimeUtil.subtractTime(yesterday, demandAssess.getStart_assess_date());
                if (diff <= 0) {
                    continue;
                }
                demandAssess.setAssess_days(diff);
                demandAssessDao.update(demandAssess);
                if (diff == 7) {
                    listDemandAssess7.add(demandAssess);
                } else if (diff == 11) {
                    listDemandAssess11.add(demandAssess);
                } else if (diff > 14) {
                    listDemandAssess14.add(demandAssess);
                }
            } catch (Exception e) {
                logger.error("----[" + demandAssess.getOa_contact_no() + "]需求保存异常");
            }
        }
        if (assessSendEmailFlag) {
            List<String> emailDemandAssessList = Arrays.asList(emailDemandAssess.split(","));
            if (!CommonUtils.isNullOrEmpty(listDemandAssess7)) {
                for (DemandAssess demandAssess7 : listDemandAssess7) {
                    List<String> demandLeaderEmail7 = getDemandLeaderEmail(demandAssess7.getDemand_leader());
                    demandLeaderEmail7.addAll(emailDemandAssessList);
                    try {
                        sendEmailDemandService.sendEmailAssess7(demandAssess7.getId(), demandAssess7.getOa_contact_no(),
                                demandAssess7.getOa_contact_name(), demandLeaderEmail7);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }

            }
            if (!CommonUtils.isNullOrEmpty(listDemandAssess11)) {
                for (DemandAssess demandAssess11 : listDemandAssess11) {
                    List<String> demandLeaderEmail11 = getDemandLeaderEmail(demandAssess11.getDemand_leader());
                    demandLeaderEmail11.addAll(emailDemandAssessList);
                    try {
                        sendEmailDemandService.sendEmailAssess11(demandAssess11.getId(), demandAssess11.getOa_contact_no(),
                                demandAssess11.getOa_contact_name(), demandLeaderEmail11);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
            if (!CommonUtils.isNullOrEmpty(listDemandAssess14)) {
                for (DemandAssess demandAssess14 : listDemandAssess14) {
                    List<String> demandLeaderEmail14 = getDemandLeaderEmail(demandAssess14.getDemand_leader());
                    demandLeaderEmail14.addAll(emailDemandAssessList);
                    try {
                        sendEmailDemandService.sendEmailAssess14(demandAssess14.getId(), demandAssess14.getOa_contact_no(),
                                demandAssess14.getOa_contact_name(), demandAssess14.getAssess_days(), demandLeaderEmail14);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 获取需求牵头人邮件
     *
     * @param demandLeader
     * @return
     */
    @Override
    public List<String> getDemandLeaderEmail(HashSet<String> demandLeader) {
        List<String> demandLeaderEmail = new ArrayList<>();
        Map<String, Map> map = roleService.queryByUserCoreData(new ArrayList(demandLeader));
        for (String userId : demandLeader) {
            demandLeaderEmail.add((String) map.get(userId).get(com.spdb.fdev.fdemand.base.dict.Dict.EMAIL));
        }
        return demandLeaderEmail;
    }

    @Override
    public XSSFWorkbook export(DemandAssessDto dto) throws Exception {
        // 初始化workbook
        InputStream inputStream;
        XSSFWorkbook workbook;
        XSSFSheet sheet;
        //引入模板
        try {
            ClassPathResource classPathResource = new ClassPathResource("demandAssess.xlsx");
            inputStream = classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
        } catch (Exception e) {
            throw new FdevException("初始化模板失败");
        }
        List<DemandAssess> data = (List<DemandAssess>) query(dto).get(Dict.DATA);
        Set<String> creatorIds = new HashSet<>();
        for (DemandAssess i : data) {//收集全部创建人ID
            creatorIds.add(i.getCreate_user());
        }
        //批量查询用户信息字典map
        Map<String, Map> userMap = !CommonUtils.isNullOrEmpty(creatorIds) ? userService.queryByUserCoreData(creatorIds, null) : new HashMap<>();
        int line = 1;
        for (DemandAssess i : data) {
            XSSFRow row = sheet.createRow(line);
            StringBuilder demandLeaderInfoSb = new StringBuilder();
            if (!CommonUtils.isNullOrEmpty(i.getDemand_leader_info())) {
                i.getDemand_leader_info().forEach(x -> demandLeaderInfoSb.append(x.getUser_name_cn()).append(','));
            }
            if (demandLeaderInfoSb.length() > 0) {
                demandLeaderInfoSb.deleteCharAt(demandLeaderInfoSb.length() - 1);
            }
            row.createCell(0).setCellValue(i.getOa_contact_no());//需求编号
            row.createCell(1).setCellValue(i.getOa_contact_name());//需求名称
            row.createCell(2).setCellValue(i.getDemand_type());//需求类型
            row.createCell(3).setCellValue(i.getDemand_leader_group_cn());//牵头小组
            row.createCell(4).setCellValue(demandLeaderInfoSb.toString());//牵头人员
            if (!CommonUtils.isNullOrEmpty(i.getPriority())) {
                row.createCell(5).setCellValue(i.getPriority());//优先级
            }
            if (!CommonUtils.isNullOrEmpty(i.getDemand_status_name())) {
                row.createCell(6).setCellValue(i.getDemand_status_name());//分析状态
            }
            if (!CommonUtils.isNullOrEmpty(i.getOverdue_type())) {
                row.createCell(7).setCellValue(i.getOverdue_type_cn());//超期分类
            }
            if (!CommonUtils.isNullOrEmpty(i.getAssess_days())) {
                row.createCell(8).setCellValue(i.getAssess_days());//评估天数
            }
            row.createCell(9).setCellValue(i.getStart_assess_date());//评估起始日期
            if (!CommonUtils.isNullOrEmpty(i.getEnd_assess_date())) {
                row.createCell(10).setCellValue(i.getEnd_assess_date());//评估完成日期
            }
            if (!CommonUtils.isNullOrEmpty(i.getAssess_present())) {
                row.createCell(11).setCellValue(i.getAssess_present());//评估现状
            }
            if (!CommonUtils.isNullOrEmpty(i.getConf_state())) {
                row.createCell(12).setCellValue(i.getConf_state_cn());//conf状态
            }
            if (!CommonUtils.isNullOrEmpty(i.getFinal_date())) {
                row.createCell(13).setCellValue(i.getFinal_date());//定稿时间
            }
            if (!CommonUtils.isNullOrEmpty(i.getCreate_user())) {
                row.createCell(14).setCellValue(CommonUtils.isNullOrEmpty(userMap.get(i.getCreate_user())) || CommonUtils.isNullOrEmpty(userMap.get(i.getCreate_user()).get(Dict.USER_NAME_CN)) ? "" : (String) userMap.get(i.getCreate_user()).get(Dict.USER_NAME_CN));//是否为conf同步
            }
            if (!CommonUtils.isNullOrEmpty(i.getConfirm_from())) {
                String confirmFrom;
                switch (i.getConfirm_from()) {
                    case "ipmp":
                    default:
                        confirmFrom = "ipmp同步";
                        break;
                    case "manual":
                        confirmFrom = "手动分析完成";
                        break;
                }
                row.createCell(15).setCellValue(confirmFrom);//是否为conf同步
            }
            line++;
        }
        return workbook;
    }

    @Override
    public void updateAssessOver(String oaContactNo) {
        DemandAssess getDemandAssess = demandAssessDao.queryByIdOrNo(null, oaContactNo, 9);
        if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
            return;
        }
        if (2 == getDemandAssess.getDemand_status()) {
            return;
        }
        // 当原数据为暂缓时，则直接修改评估完成时间为当前，状态为完成
        getDemandAssess.setEnd_assess_date(TimeUtil.formatToday());
        getDemandAssess.setDemand_status(2);
        getDemandAssess.setConfirm_from("ipmp");
        // 当原数据为分析中时，还需计算评估时间
        if (1 == getDemandAssess.getDemand_status()) {
            String yesterday = TimeUtil.todayLastTwoWeek(1);
            int diff = TimeUtil.subtractTime(yesterday, getDemandAssess.getStart_assess_date());
            if (diff < 0) {
                diff = 0;
            }
            getDemandAssess.setAssess_days(diff);
        }
        try {
            demandAssessDao.update(getDemandAssess);
        } catch (Exception e) {
            logger.error("---" + getDemandAssess.getOa_contact_no() + "数据更新异常------");
        }
    }

    @Override
    public void syncConfState() throws Exception {
        Date temp = new Date();
        List<String> confStateFinalCodes = Arrays.asList(confStateFinalCode.split(","));
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(temp);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(temp);
        List<ContentDto> confResult = new ArrayList<>();//这里不用set 不会重复的
        List<DictEntity> confState = dictService.query(new DictEntity() {{
            setType("confState");//初版：已暂缓、进行中、已批准
        }});
        Pattern pattern = Pattern.compile("^\\d{4}$");
        Map<String, String> codeNameMap = dictService.getCodeNameMap(confState);
        for (DictEntity confStateDict : confState) {
            String url = "/content/search?cql=label!=\"已归档\" AND state=\"" + confStateDict.getValue() + "\"" + " AND " + confUtils.getDefaultSpaceUrl();
            List<ContentDto> results = confUtils.getTotalPageData(url);
            results.forEach(x -> x.setState(confStateDict.getCode()));
            confResult.addAll(results);
        }
        for (ContentDto contentDto : confResult) {
            String title = contentDto.getTitle();
            int leftIndex = title.indexOf("【");//左括号下标
            int rightIndex = title.indexOf("】");//右括号下标
            String newState = contentDto.getState();
            String confPage = contentDto.get_links().getWebui();
            String demandNo;
            if (-1 == leftIndex || -1 == rightIndex) {
                if (confStateFinalCodes.contains(newState) && confAssessSendEmailFlag)
                    sendEmailDemandService.sendEmailConfRemind(title, confUtils.getConfUrl() + confPage, confAssessSendEmail.split(","));
                continue;//如果不满足标题格式就跳过
            } else {//就算title满足【】还需要判断
                demandNo = title.substring(leftIndex + 1, rightIndex).trim();
                //如果编号前后有空格 || 编号不囊括"-YYYY-" 都是错误格式
                // "如果编号前后有空格"判定为错误格式 - 这个逻辑取消 - 2021-11-26 和杨静老师确定需求
                String[] split = demandNo.split("-");
                boolean flag = false;
                if (split.length != 1 && split.length != 2) {
                    for (int i = 1; i < split.length - 1; i++) {
                        String noYear = split[i];
                        if (pattern.matcher(noYear).matches() && Integer.valueOf(noYear).compareTo(Integer.valueOf(date.split("-")[0])) <= 0) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (/*demandNo.length() != demandNo.trim().length() || !title.contains("-" + currentTime.split("-")[0] + "-")*/!flag) {//如果编号格式有误
                    if (confStateFinalCodes.contains(newState) && confAssessSendEmailFlag)
                        sendEmailDemandService.sendEmailConfRemind(title, confUtils.getConfUrl() + confPage, confAssessSendEmail.split(","));
                    continue;//只同步当前年数据顺便校验格式
                }
            }
            DemandAssess entityDa = demandAssessDao.queryByNo(demandNo);
            if (null == entityDa) {//如果不存在则插入
                DemandAssess da = new DemandAssess();
                da.setDemand_type(Dict.BUSINESS);
                da.setOa_contact_name(title.substring(rightIndex + 1));
                da.setDemand_status(1);
                da.setOa_contact_no(demandNo);
                da.setCreate_user(adminAccountId);
                da.setCreate_time(currentTime);
                da.setStart_assess_date(date);
                da.setConf_state(newState);
                da.setConf_url(confPage);
                if (confStateFinalCodes.contains(newState)) da.setFinal_date(date);
                demandAssessDao.save(da);
            } else {//不为空则更新
                String oldState = entityDa.getConf_state();
                DemandAssess da = new DemandAssess();
                da.setId(entityDa.getId());
                if (adminAccountId.equals(entityDa.getCreate_user()))//名字必须自始至终都是Conf新建的才同步
                    da.setOa_contact_name(title.substring(rightIndex + 1));
                if (CommonUtils.isNullOrEmpty(entityDa.getConf_url())) {//判断是否是第一次同步
                    da.setConf_state(newState);
                    da.setUpdate_user(adminAccountId);
                    da.setUpdate_time(currentTime);
                    if (!confStateFinalCodes.contains(newState)) {//只要不是要改成“批准”状态就置空
                        da.setFinal_date("");
                    } else {
                        da.setFinal_date(currentTime);
                    }
                } else {//已经conf同步过
                    if (!newState.equals(oldState)) {
                        da.setConf_state(newState);
                        //这里就以这个不太严谨的方案作为判断依据 若创建人admin账号者即是confluence同步数据
                        da.setUpdate_user(adminAccountId);
                        da.setUpdate_time(currentTime);
                        //只有当接下来要更新的状态是”已批准“ 并且 旧值不是“已批准” 才能变更定稿日期
                        if (confStateFinalCodes.contains(newState)) {
                            da.setFinal_date(date);
                        }
                    }
                }
                da.setConf_url(confPage);//这种需求 不排除 改编号的链接置换问题
                demandAssessDao.update(da);
                // 需求文档状态和表中是否一致，如状态有改变 && 满足邮件开关 - 则 - 发邮件
                if (!newState.equals(oldState) && confAssessSendEmailFlag) {
                    sendEmailDemandService.sendEmailConfUpdateAssess(entityDa.getId(), entityDa.getOa_contact_no(), entityDa.getOa_contact_name(), null == oldState ? "无状态" : codeNameMap.get(oldState), codeNameMap.get(newState), confAssessSendEmail.split(","));
                }
            }
        }

    }

    @Override
    public DemandAssess confirmFinish(String id, String endAccessDate) throws Exception {
        User user = CommonUtils.getSessionUser();
        String userEnName = user.getUser_name_en();
        // 获取只能确认评估完成的用户英文名称列表
        List<String> userEnNames = Arrays.asList(accessConfirmEnNames.split(","));
        if (!userEnNames.contains(userEnName)) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        // 通过id 查询是否存在需求评估业务
        DemandAssess getDemandAssess = demandAssessDao.queryById(id);
        // 当查询不出任何内容时，则直接抛出异常
        if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        // 获取开始评估日期
        String startAccessDate = getDemandAssess.getStart_assess_date();
        // 如果不是分析中，则不允许手动确认
        if (1 != getDemandAssess.getDemand_status()) {
            throw new FdevException(ErrorConstants.DEMAND_ASSESS_NOT_CONFIRM);
        }
        // 需求评估手动确认完成后，更新评估天数
        int betweenDays = TimeUtil.subtractTime(endAccessDate, startAccessDate);
        if (betweenDays < 0) {
            betweenDays = 0;
        }
        // 组装数据进行更新
        DemandAssess demandAssess = new DemandAssess();
        demandAssess.setId(id);
        demandAssess.setEnd_assess_date(endAccessDate);
        demandAssess.setDemand_status(2);
        demandAssess.setConfirm_from("manual");
        demandAssess.setAssess_days(betweenDays);
        return demandAssessDao.update(demandAssess);
    }

    @Override
    public void deferOperate(MultipartFile[] files, String fileType, String id, Integer demandStatus, String userGroupCn) throws Exception {
        // 通过id 查询是否存在需求评估业务
        DemandAssess getDemandAssess = demandAssessDao.queryById(id);
        // 当查询不出任何需求评估业务内容时，则直接抛出异常
        if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        // 获取当前日期并与起始评估日期相比较
        String nowStr = TimeUtil.formatToday();
        if (TimeUtil.subtractTime(nowStr, getDemandAssess.getStart_assess_date()) < 0) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"未到起始评估日期，不能被暂缓或者取消暂缓"});
        }
        // 获取原数据中的分析状态，如果不是分析中或者暂缓中，则说明不能进行暂缓或者取消暂缓，直接抛出异常
        Integer oldDemandStatus = getDemandAssess.getDemand_status();
        if (oldDemandStatus != 1 && oldDemandStatus != 3) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"原数据不能被暂缓或者取消暂缓"});
        }
        if (demandStatus != 1 && demandStatus != 3) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        User user = CommonUtils.getSessionUser();

        // 判断是否拥有权限
        if (!(roleService.isDemandManager() ||
                (!CommonUtils.isNullOrEmpty(getDemandAssess.getDemand_leader()) &&
                        getDemandAssess.getDemand_leader().contains(CommonUtils.getSessionUser().getId()))
        )) {
            // 如果没有拥有权限，则抛出权限异常
            throw new FdevException(ErrorConstants.FUSER_ROLE_ERROR);
        }
        // 3代表将要进行暂缓
        String title = "";
        String prefixContent = "";
        String suffixContent = "";
        List<String> accessTimes = getDemandAssess.getAccess_times();
        if (null == accessTimes) {
            accessTimes = new ArrayList<>();
        }
        DemandAssess entity = new DemandAssess();
        entity.setId(id);
        entity.setDemand_status(demandStatus);
        if (3 == demandStatus) {
            if (files == null || files.length == 0) {
                throw new FdevException(ErrorConstants.UPLOADER_CANNOT_BE_EMPTY, new String[]{"暂缓操作必须上传邮件"});
            }
            for (MultipartFile file : files)
                if (file.getSize() >= (20 * 1024 * 1024))
                    throw new FdevException(ErrorConstants.FILE_INFO_SIZE_ERROR, new String[]{file.getOriginalFilename()});
            // 之前的状态为分析中才可以进行暂缓
            if (1 != getDemandAssess.getDemand_status()) {
                throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{"只有分析中的任务才能暂缓"});
            }
            // 进行文件上传
            docService.uploadFile(id, fileType, null, user.getGroup_id(), userGroupCn, files, "demandAccess");
            title = "需求暂缓通知";
            prefixContent = "您的需求";
            suffixContent = "已暂缓，请注意。";
            String content = "起始评估日期" + (accessTimes.size() + 1) + ": " + getDemandAssess.getStart_assess_date() + " —— 暂缓日期" + (accessTimes.size() + 1) + ": " + nowStr;
            accessTimes.add(content);
            entity.setAccess_times(accessTimes);
        } else {
            // 否则代表取消暂缓
            // 之前的状态为暂缓中才可以进行取消
            if (3 != getDemandAssess.getDemand_status()) {
                throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{"只有分析中的任务才能暂缓"});
            }
            title = "取消暂缓通知";
            prefixContent = "您的暂缓需求";
            suffixContent = "已恢复，请注意。";
            entity.setAssess_days(0);
            entity.setStart_assess_date(nowStr);
        }
        // 进行更新状态
        demandAssessDao.update(entity);
        // 进行邮件发送
        Set<String> sendSet = new HashSet<>();
        Set<String> demandLeaderHash = getDemandAssess.getDemand_leader();
        if (!CommonUtils.isNullOrEmpty(demandLeaderHash)) {
            sendSet.addAll(getDemandLeaderEmail(getDemandAssess.getDemand_leader()));
        }
        sendSet.addAll(Arrays.asList(deferUserEmails.split(",")));
        // 异步发送（取消）暂缓邮件
        asyncService.sendDeferEmail(title, prefixContent, suffixContent, getDemandAssess.getId(), getDemandAssess.getOa_contact_no(),
                getDemandAssess.getOa_contact_name(), new ArrayList<>(sendSet));
    }

    @Override
    public void updateFinalDate(DemandAssess demandAssess) throws Exception {
        // 获取需求评估id
        String id = demandAssess.getId();
        // 通过id 查询是否存在需求评估业务
        DemandAssess getDemandAssess = demandAssessDao.queryById(id);
        // 当查询不出任何内容时，则直接抛出异常
        if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        // 当存在conf_url 代表已经从confluence 同步过来过，因此不能再进行修改
        if (!CommonUtils.isNullOrEmpty(getDemandAssess.getConf_url())) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"已经从confluence 同步过来的数据无法再被修改"});
        }
        User user = CommonUtils.getSessionUser();
        // 判断是否拥有权限
        if (!(roleService.isDemandManager() ||
                (!CommonUtils.isNullOrEmpty(getDemandAssess.getDemand_leader()) &&
                        getDemandAssess.getDemand_leader().contains(user.getId()))
        )) {
            // 如果没有拥有权限，则抛出权限异常
            throw new FdevException(ErrorConstants.FUSER_ROLE_ERROR, new String[]{"该用户无法修改定稿日期"});
        }
        List<String> approveTimeList = getDemandAssess.getApprove_time_list();
        // 获取需要修改的定稿日期
        String finalDate = demandAssess.getFinal_date();
        // 进行时间格式化转换，如果转换出错即可判断时间格式出错
        TimeUtil.parseDateStr(finalDate);
        DemandAssess updateEntity = new DemandAssess();
        updateEntity.setId(id);
        updateEntity.setFinal_date(finalDate);
        // 判断是否经过审批，审批时间列表为空，说明没有
        if (CommonUtils.isNullOrEmpty(approveTimeList)) {
            approveTimeList = new ArrayList<>();
            approveTimeList.add(TimeUtil.formatTodayHs());
            updateEntity.setApprove_time_list(approveTimeList);
            updateEntity.setConf_state("confState.final");
            demandAssessDao.update(updateEntity);
        } else {
            // 代表已经修改
            // 获取申请原因
            String applyReason = demandAssess.getApply_reason();
            // 如果第二次及以上修改，必须写明申请原因
            if (CommonUtils.isNullOrEmpty(applyReason)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"申请原因"});
            }
            FdevFinalDateApprove entity = new FdevFinalDateApprove();
            // 获取能够接受邮件的人员信息
            String sectionId = getSectionIdByGroupId(getDemandAssess.getDemand_leader_group());
            if (CommonUtils.isNullOrEmpty(sectionId) || !sectionInfo.contains(sectionId)) {
                sectionId = "defaultSection";
            }
            entity.setAccess_id(id);
            entity.setOa_contact_no(getDemandAssess.getOa_contact_no());
            entity.setApply_reason(applyReason);
            entity.setApply_user_id(user.getId());
            entity.setApply_update_time(finalDate);
            entity.setCreate_time(TimeUtil.formatTodayHs());
            entity.setSection_id(sectionId);
            entity.setOperate_status(UNDETERMINED);
            // 在审批表里新增一条数据
            fdevFinalDateApproveDao.save(entity);
            // 异步给对应的人发送邮件
            asyncService.sendUpdateFinalDate(getDemandAssess.getOa_contact_no(), getDemandAssess.getOa_contact_name(), sectionId);
        }
    }

    private Map<String, String> getDictMap() throws Exception {
        return dictService.getCodeNameMapByTypes(new HashSet<String>() {{
            add(DictEnum.OVERDUE_TYPE.getType());
            add(DictEnum.CONF_STATE.getType());
        }});
    }

    private void setDictField(DemandAssess da, Map<String, String> codeNameMap) throws Exception {
        da.setOverdue_type_cn(codeNameMap.get(da.getOverdue_type()));
        da.setConf_state_cn(codeNameMap.get(da.getConf_state()));
    }

    public String getSectionIdByGroupId(String groupId) {
        String sectionId = null;
        if (!CommonUtils.isNullOrEmpty(groupId)) {
            Map<String, Object> groupMap = fdevUserService.getThreeLevelGroup(groupId);
            if (!CommonUtils.isNullOrEmpty(groupMap)) {

                if (!CommonUtils.isNullOrEmpty(groupMap.get(Dict.LEVEL))) {
                    //判断是否小组 等级
                    if (1 == (Integer) groupMap.get(Dict.LEVEL)) {
                        //1级小组无人审批默认至  固定审批人
                        sectionId = "defaultSection";
                    } else if (2 == (Integer) groupMap.get(Dict.LEVEL)) {
                        // 2级小组当前小组ID就是条线ID
                        sectionId = groupId;
                    } else {
                        //不是 1 2级  3级获取父id 为条线id
                        sectionId = (String) groupMap.get(Dict.PARENT_ID);
                    }

                }
            }
        }
        return sectionId;
    }
}