package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdemand.base.dict.*;
import com.spdb.fdev.fdemand.base.utils.*;
import com.spdb.fdev.fdemand.spdb.dao.*;
import com.spdb.fdev.fdemand.spdb.dao.impl.IpmpUnitDaoImpl;
import com.spdb.fdev.fdemand.spdb.dto.conf.ContentDto;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.notify.NotifyContext;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.fdemand.spdb.unit.DealFdevTaskUnit;
import com.spdb.fdev.fdemand.spdb.unit.DealTaskUnit;
import com.spdb.fdev.fdemand.spdb.unit.DesignUnit;
import com.spdb.fdev.transport.RestTransport;
import org.apache.http.entity.ContentType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class DemandBaseInfoServiceImpl implements IDemandBaseInfoService {

    private static final Logger logger = LoggerFactory.getLogger(DemandBaseInfoServiceImpl.class);

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private ICommonBusinessService commonBusinessService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private GroupUtil groupUtil;

    @Autowired
    private NotifyContext notifyContext;

    @Autowired
    private IImplementUnitService implementUnitService;

    @Autowired
    private IIpmpUnitService iipmpUnitService;

    @Autowired
    private DealTaskUnit dealTaskUnit;

    @Autowired
    private DealFdevTaskUnit dealFdevTaskUnit;

    @Autowired
    private IpmpUnitDaoImpl ipmpUnitDao;

    @Autowired
    private DesignUnit designUnit;

    @Autowired
    private IRoleService iRoleService;

    @Autowired
    private IIpmpUnitDao iIpmpUnitDao;

    @Autowired
    private IOtherDemandTaskService otherDemandTaskService;

    @Autowired
    private DictDao dictDao;

    @Autowired
    private IDemandDocDao demandDocDao;

    @Autowired
    private ConfUtils confUtils;

    @Autowired
    private IConfService confService;

    @Autowired
    private IGitlabService gitlabService;

    @Autowired
    private FdocmanageService fdocmanageService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private IDocService docService;

    @Value("${rqrmnt.sendUiDesign.isEmail}")
    private boolean isEmail;
    @Value("${fdev.demand.ip}")
    private String demandIp;
    @Value("${confluence.pageId}")
    private String groupPageId;
    @Value("${fdemand.doc.folder}")
    private String docFolder;

    @Override
    public List query(DemandBaseInfo demandBaseInfo) throws Exception {
        List<DemandBaseInfo> list = demandBaseInfoDao.query(demandBaseInfo);
        if (!CommonUtils.isNullOrEmpty(list)) {
            for (DemandBaseInfo demand : list) {
                String group = demand.getDemand_leader_group();
                List<RelatePartDetail> listPart = demand.getRelate_part_detail();
                if (!CommonUtils.isNullOrEmpty(listPart)) {
                    for (RelatePartDetail relatePartDetail : listPart) {
                        if (group.equalsIgnoreCase(relatePartDetail.getPart_id())) {
                            demand.setLeader_group_status(relatePartDetail.getAssess_status());
                            break;
                        }
                    }
                }

            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> queryForSelect() throws Exception {
        List<DemandBaseInfo> demandList = demandBaseInfoDao.query(null);
        return demandList.stream().map(x -> {
            return new HashMap<String, Object>() {{
                put(Dict.ID, x.getId());
                put(Dict.OA_CONTACT_NAME, x.getOa_contact_name());
                put(Dict.OA_CONTACT_NO, x.getOa_contact_no());
                put(Dict.DEMAND_LEADER,x.getDemand_leader());
                put(Dict.DEMAND_STATUS_NORMAL,x.getDemand_status_normal());
                put(Dict.DEMAND_STATUS_SPECIAL,x.getDemand_status_special());
            }};
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> queryTechBusinessForSelect() throws Exception {
        List<DemandBaseInfo> demandList = demandBaseInfoDao.query(null);
        return demandList.stream().filter(demandBaseInfo ->
            Constants.BUSINESS.equals(demandBaseInfo.getDemand_type()) || Constants.TECH.equals(demandBaseInfo.getDemand_type())
        ).map(x -> {
            return new HashMap<String, Object>() {{
                put(Dict.ID, x.getId());
                put(Dict.OA_CONTACT_NAME, x.getOa_contact_name());
                put(Dict.OA_CONTACT_NO, x.getOa_contact_no());
                put(Dict.DEMAND_LEADER,x.getDemand_leader());
                put(Dict.DEMAND_STATUS_NORMAL,x.getDemand_status_normal());
                put(Dict.DEMAND_STATUS_SPECIAL,x.getDemand_status_special());
            }};
        }).collect(Collectors.toList());
    }

    @Override
    /*
     *	需求录入
     *
     */
    public DemandBaseInfo save(DemandBaseInfo demandBaseInfo) throws Exception {
        //替换时间格式
        demandBaseInfo.setAccept_date(TimeUtil.timeConvertNew(demandBaseInfo.getAccept_date()));
        //校验用户权限 必须为需求负责人，直接返回用户权限不足
        if (!roleService.isDemandManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求负责人"});
        }
        //设置牵头人员
        List<UserInfo> demand_leader_all = demandBaseInfo.getDemand_leader_all();
        HashSet<String> demand_leader = new HashSet<>();
        if (!CommonUtils.isNullOrEmpty(demand_leader_all)) {
            for (UserInfo userInfo : demand_leader_all) {
                demand_leader.add(userInfo.getId());
            }
            demandBaseInfo.setDemand_leader(demand_leader);
        }
        //查询需求类型为科技需求，并根据创建需求编号，格式是 科技：KEJI-年份-最大值
        //校验科技需求相关入参不能为空
        String demandType = demandBaseInfo.getDemand_type();
        if ((Dict.TECH).equalsIgnoreCase(demandType)) {
            String demand_instruction = demandBaseInfo.getDemand_instruction();
            if (CommonUtils.isNullOrEmpty(demand_instruction)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"需求说明书名称"});
            }
            demandBaseInfo.setDesign_status(UIEnum.UIDesignEnum.NORELATE.getValue());
            demandBaseInfo.setUi_verify(false);
            DemandBaseInfo demandBaseInfos = demandBaseInfoDao.queryBydemandType(demandType);
            Calendar calendar = Calendar.getInstance();
            String fdev_oa_contact_no = "KEJI-" + calendar.get(Calendar.YEAR) + "-";
            if (CommonUtils.isNullOrEmpty(demandBaseInfos)) {
                fdev_oa_contact_no = fdev_oa_contact_no + "0001";
            } else {
                String db_oa_contact_no = demandBaseInfos.getOa_contact_no();
                String[] db_oa_contact_noArray = db_oa_contact_no.split("-");
                int no_latest = Integer.parseInt(db_oa_contact_noArray[db_oa_contact_noArray.length - 1]) + 1;
                fdev_oa_contact_no = fdev_oa_contact_no + String.format("%04d", no_latest);
            }
            //生成需求编号(OA联系单编号)
            demandBaseInfo.setOa_contact_no(fdev_oa_contact_no);
            //获取OA联系单名称
            demandBaseInfo.setOa_contact_name(demandBaseInfo.getDemand_instruction());
        } else if ((Dict.BUSINESS).equalsIgnoreCase(demandBaseInfo.getDemand_type())) {
            //校验业务需求相关入参不能为空
            String oa_contact_no = demandBaseInfo.getOa_contact_no();
            if (CommonUtils.isNullOrEmpty(oa_contact_no)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"OA联系单编号"});
            }
            String oa_contact_name = demandBaseInfo.getOa_contact_name();
            if (CommonUtils.isNullOrEmpty(oa_contact_name)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"OA联系单名称"});
            }
            //业务需求增加需求紧急标识
            demandBaseInfo.setDemand_flag(DemandEnum.DemandDemand_flag.URGENT.getValue());
            if (!CommonUtils.isNullOrEmpty(demandBaseInfo.getUi_verify())) {
                if (demandBaseInfo.getUi_verify()) {
                    demandBaseInfo.setDesign_status(UIEnum.UIDesignEnum.NOLOADNOT.getValue());
                } else {
                    demandBaseInfo.setDesign_status(UIEnum.UIDesignEnum.NORELATE.getValue());
                }
            }
        } else if (Dict.DAILY.equalsIgnoreCase(demandType)) {//日常需求
            String oa_contact_name = demandBaseInfo.getOa_contact_name();
            if (CommonUtils.isNullOrEmpty(oa_contact_name)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"需求名称"});
            }
            demandBaseInfo.setDesign_status(UIEnum.UIDesignEnum.NORELATE.getValue());
            demandBaseInfo.setUi_verify(false);
            DemandBaseInfo demandBaseInfos = demandBaseInfoDao.queryBydemandType(demandType);
            Calendar calendar = Calendar.getInstance();
            String fdev_oa_contact_no = "DAILY-" + calendar.get(Calendar.YEAR) + "-";
            if (CommonUtils.isNullOrEmpty(demandBaseInfos)) {
                fdev_oa_contact_no = fdev_oa_contact_no + "0001";
            } else {
                String db_oa_contact_no = demandBaseInfos.getOa_contact_no();
                String[] db_oa_contact_noArray = db_oa_contact_no.split("-");
                int no_latest = Integer.parseInt(db_oa_contact_noArray[db_oa_contact_noArray.length - 1]) + 1;
                fdev_oa_contact_no = fdev_oa_contact_no + String.format("%04d", no_latest);
            }
            //生成需求编号(OA联系单编号)
            demandBaseInfo.setOa_contact_no(fdev_oa_contact_no);
        } else {
            throw new FdevException(ErrorConstants.DEMAND_TYPE_ERROR);
        }
        //设置版本详情中的评估人员,需求录入成功后，每个板块状态:预评估
        ArrayList<RelatePartDetail> relate_part_detail = demandBaseInfo.getRelate_part_detail();
        if (!CommonUtils.isNullOrEmpty(relate_part_detail)) {
            for (RelatePartDetail relatePartDetail : relate_part_detail) {
                if (CommonUtils.isNullOrEmpty(relatePartDetail.getAssess_user_all())) {
                    throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"评估人员"});
                }
                relatePartDetail.setAssess_status(DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue());
                List<UserInfo> assess_user_all = relatePartDetail.getAssess_user_all();
                HashSet<String> assess_user = new HashSet<String>();
                for (UserInfo UserInfo : assess_user_all) {
                    assess_user.add(UserInfo.getId());
                }
                relatePartDetail.setAssess_user(assess_user);
            }
        }

        //设置创建时间
        demandBaseInfo.setDemand_create_time(TimeUtil.getTimeStamp(new Date()));
        //设置需求正常状态
        if (CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_status_normal())) {
            demandBaseInfo.setDemand_status_normal(DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue());
        }
        //获取创建人
        User user = CommonUtils.getSessionUser();
        demandBaseInfo.setDemand_create_user(user.getId());

        //校验一个需求编号只能对应一个需求
        //判断数据库是否存在该需求数据，没有则存入数据库，有则判断该需求是否处于已撤销状态，处于已撤销状态则存入数据库
        String oa_contact_no = demandBaseInfo.getOa_contact_no();
        Long demandCount = demandCount(oa_contact_no);
        DemandBaseInfo resultBaseInfo = null;
        if (demandCount == null || demandCount == 0) {
            resultBaseInfo = demandBaseInfoDao.save(demandBaseInfo);
        } else {
            throw new FdevException(ErrorConstants.DEMAND_INFO_ERROR, new String[]{oa_contact_no});
        }
        //发送邮件给涉及板块中指定的人员通知需求录入成功，发送相关通知及代办项
        Map<String, Object> param = CommonUtils.object2Map(demandBaseInfo);
        notifyContext.notifyContext(Dict.DemandInfoNotifyImpl, param);
        return resultBaseInfo;

    }


    @Override
    /*
     *	根据oa联系单编号查询需求条数
     *
     */
    public Long demandCount(String oa_contact_no) throws Exception {
        Long demandCount = demandBaseInfoDao.countOa_contact_no(oa_contact_no.trim());
        return demandCount;
    }

    @Override
    public DemandBaseInfo update(DemandBaseInfo demandBaseInfo) throws Exception {
        DemandBaseInfo demand = demandBaseInfoDao.queryById(demandBaseInfo.getId());
        HashSet<String> newDemandLeader = demandBaseInfo.getDemand_leader();
        HashSet<String> oldDemandLeader = demand.getDemand_leader();
        //判断需求牵头人是否有编辑 编辑过 则将标识置为true
        if(!newDemandLeader.equals(oldDemandLeader)){
            demandBaseInfo.setLeader_update_flag(true);
        }
        if ((Dict.BUSINESS).equalsIgnoreCase(demandBaseInfo.getDemand_type())) {
            if (!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())) {
                if (DemandEnum.DemandDemand_flag.NORMAL_SYNCHRONIZATION.getValue() == (demandBaseInfo.getDemand_flag())) {
                    if (!(demandBaseInfo.getOa_contact_name().equals(demand.getOa_contact_name()))) {
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"oa联系单名称"});
                    }
                }
            }
        } else if ((Dict.TECH).equalsIgnoreCase(demandBaseInfo.getDemand_type())) {
            if (CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_instruction())) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"需求说明书名称"});
            }
            //更新OA联系单名称
            demandBaseInfo.setOa_contact_name(demandBaseInfo.getDemand_instruction());
        } else if (Dict.DAILY.equalsIgnoreCase(demandBaseInfo.getDemand_type())) {
            if (CommonUtils.isNullOrEmpty(demandBaseInfo.getOa_contact_name())) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"需求名称"});
            }
        } else {
            throw new FdevException(ErrorConstants.DEMAND_TYPE_ERROR);
        }
        //校验用户权限 必须为需求负责人或牵头人员
        User user = CommonUtils.getSessionUser();
        boolean isDemandLeader = roleService.isDemandLeader(demandBaseInfo.getId(), user.getId());
        if (!roleService.isDemandManager() && !isDemandLeader) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员或需求牵头人"});
        }
        //已归档、已撤销的需求不允许修改，暂缓中的需求不允许修改
        DemandBaseInfo demandBaseInfoBefore = demandBaseInfoDao.queryById(demandBaseInfo.getId());
        int demand_status_normal = demandBaseInfoBefore.getDemand_status_normal();
        if (demand_status_normal == DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue() || demand_status_normal == DemandEnum.DemandStatusEnum.IS_CANCELED.getValue()) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_UPDATE_ERROR);
        }
        Integer demand_status_special = demandBaseInfoBefore.getDemand_status_special();
        if (demand_status_special != null
                && DemandEnum.DemandDeferStatus.DEFER.getValue().equals(demand_status_special)
                && DemandEnum.DemandDeferStatus.RECOVER.getValue().equals(demand_status_special)) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_UPDATE_ERROR);
        }
        //根据牵头小组详细信息更新牵头小组
        List<UserInfo> demand_leader_all = demandBaseInfo.getDemand_leader_all();
        if (!CommonUtils.isNullOrEmpty(demand_leader_all)) {
            HashSet<String> demand_leader = new HashSet<>();
            for (UserInfo userInfo : demand_leader_all) {
                if (!CommonUtils.isNullOrEmpty(userInfo)) {
                    demand_leader.add(userInfo.getId());
                }
            }
            demandBaseInfo.setDemand_leader(demand_leader);
        }
        //根据牵头详细信息更新评估人员
        List<RelatePartDetail> relate_part_detail = demandBaseInfo.getRelate_part_detail();
        List<RelatePartDetail> relate_part_detailOl = demandBaseInfoBefore.getRelate_part_detail();
        if (!CommonUtils.isNullOrEmpty(relate_part_detailOl)) {
            if (!CommonUtils.isNullOrEmpty(relate_part_detail)) {
                boolean flag = false;
                for (RelatePartDetail relatePartDetailOl : relate_part_detailOl) {
                    flag = false;
                    for (RelatePartDetail relatePartDetail : relate_part_detail) {
                        if (relatePartDetail.getPart_id().equals(relatePartDetailOl.getPart_id())) {
                            flag = true;
                            if (!relatePartDetailOl.getAssess_status().equals(relatePartDetail.getAssess_status())) {
                                relatePartDetail.setAssess_status(relatePartDetailOl.getAssess_status());
                            }
                        }
                    }
                    if (!flag) {
                        if (!DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue().equals(relatePartDetailOl.getAssess_status())) {
                            throw new FdevException(ErrorConstants.UPLOADER_CANNOT_BE_EMPTY, new String[]{relatePartDetailOl.getPart_name()});
                        }
                    }
                }
            } else {
                for (RelatePartDetail relatePartDetailOl : relate_part_detailOl) {
                    if (!DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue().equals(relatePartDetailOl.getAssess_status())) {
                        throw new FdevException(ErrorConstants.UPLOADER_CANNOT_BE_EMPTY, new String[]{relatePartDetailOl.getPart_name()});
                    }
                }
            }
        }

        if (!CommonUtils.isNullOrEmpty(relate_part_detail)) {
            for (RelatePartDetail relatePartDetail : relate_part_detail) {
                List<UserInfo> assess_user_all = relatePartDetail.getAssess_user_all();
                HashSet<String> assess_user = new HashSet<String>();
                for (UserInfo UserInfo : assess_user_all) {
                    assess_user.add(UserInfo.getId());
                }
                relatePartDetail.setAssess_user(assess_user);
            }
        }
        //判断ui审核
        //获取需求初始ui审核状态
        Boolean uiFlagOld = demandBaseInfoBefore.isUi_verify();
        //获取此次需求ui审核状态
        Boolean uiFlagNew = demandBaseInfo.isUi_verify();
        if (uiFlagOld != uiFlagNew) {
            if (!uiFlagNew) {
                demandBaseInfo.setDesign_status(Constants.NORELATE);
            } else {
                demandBaseInfo.setDesign_status(Constants.UPLOADNOT);
            }
            demandBaseInfo.setUi_verify(uiFlagNew);
        }
        //校验一个需求编号只能对应一个需求且需求不处于已撤销状态
        DemandBaseInfo resultBaseUpdate = null;
        String oa_contact_no = demandBaseInfoDao.queryById(demandBaseInfo.getId()).getOa_contact_no();
        if (!CommonUtils.isNullOrEmpty(oa_contact_no)) {
            Long demandCount = demandCount(oa_contact_no);
            if (demandCount == 1) {
                resultBaseUpdate = demandBaseInfoDao.update(demandBaseInfo);
            } else {
                throw new FdevException(ErrorConstants.DEMAND_INFO_ERROR, new String[]{oa_contact_no});
            }
        }
        iipmpUnitService.updateDemandStatus(resultBaseUpdate);
        //发送邮件给涉及板块中指定的人员通知需求更新成功，发送相关通知
        Map<String, Object> param = CommonUtils.object2Map(demandBaseInfo);
        notifyContext.notifyContext(Dict.DemandUpdateNotifyImpl, param);
        return resultBaseUpdate;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public DemandBaseInfo updateImpl(DemandBaseInfo demandBaseInfo) throws Exception {
        //判断需求是否可以进行预评估
        String demand_id = demandBaseInfo.getId();
        String demand_plan_no = demandBaseInfo.getDemand_plan_no();
        DemandBaseInfo data = demandBaseInfoDao.queryById(demand_id);
        Integer demand_status_normal = data.getDemand_status_normal();
        String demand_type = data.getDemand_type();
        if (!((Dict.BUSINESS).equalsIgnoreCase(demand_type))) {
            throw new FdevException(ErrorConstants.PRE_APPRAISAL);
        }
        //校验用户权限 必须为需求负责人或牵头人员
        User user = CommonUtils.getSessionUser();
        boolean isDemandLeader = roleService.isDemandLeader(demandBaseInfo.getId(), user.getId());
        if (!roleService.isDemandManager() && !isDemandLeader) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员或需求牵头人"});
        }
        Pattern pattern = Pattern.compile("^[0-9]{4}[\\u4E00-\\u9FA5]{2,6}[0-9]{3,4}$");
        Matcher m = pattern.matcher(demand_plan_no);
        if (!m.matches()) {
            throw new FdevException(ErrorConstants.INCORRECT_DATA_FORMAT);
        }
        if (data.getDemand_status_normal() == 0) {
            //设置版本详情中的评估人员,需求预评估成功后，每个板块状态:预评估
            List<RelatePartDetail> relate_part_detail = demandBaseInfo.getRelate_part_detail();
            if (!CommonUtils.isNullOrEmpty(relate_part_detail)) {
                for (RelatePartDetail relatePartDetail : relate_part_detail) {
                    relatePartDetail.setAssess_status(DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue());
                    List<UserInfo> assess_user_all = relatePartDetail.getAssess_user_all();
                    HashSet<String> assess_user = new HashSet<String>();
                    for (UserInfo UserInfo : assess_user_all) {
                        assess_user.add(UserInfo.getId());
                    }
                    relatePartDetail.setAssess_user(assess_user);
                }
            }
            //判断是否涉及Ui设计，并赋予相应状态
            Boolean ui_verify = demandBaseInfo.isUi_verify();
            if (CommonUtils.isNullOrEmpty(ui_verify) || !ui_verify) {
                demandBaseInfo.setDesign_status(UIEnum.UIDesignEnum.NORELATE.getValue());
                demandBaseInfo.setUi_verify(false);
            } else
                demandBaseInfo.setDesign_status(UIEnum.UIDesignEnum.NOLOADNOT.getValue());
        }
        //判断前端传递的oa联系单编号不为空 且 数据库中的oa联系单编号不为空 且两者不相同时，不允许修改oa联系单编号
        String oa_real_no = demandBaseInfo.getOa_real_no();
        String db_oa_real_no = demandBaseInfoDao.queryById(demandBaseInfo.getId()).getOa_real_no();
        if ((!CommonUtils.isNullOrEmpty(db_oa_real_no)) && (!CommonUtils.isNullOrEmpty(oa_real_no)) && (!(oa_real_no.equalsIgnoreCase(db_oa_real_no)))) {
            throw new FdevException(ErrorConstants.NOT_ALLOW_UPDATE, new String[]{db_oa_real_no});
        }
        //校验一个需求编号只能对应一个需求且需求不处于已撤销状态
        //当前端传递的oa联系单编号为空时，已存在需求编号，查询数据库该需求数量为1。
        //当前端传递的oa联系单编号不为空时，前端传递的oa联系单编号与数据库中的需求编号相同，查询数据库该需求数量为1
        //当前端传递的oa联系单编号不为空时，前端传递的oa联系单编号与数据库中的需求编号不相同，查询数据库该需求数量为null或0
        DemandBaseInfo resultBaseUpdate = null;
        String db_oa_contact_no = demandBaseInfoDao.queryById(demandBaseInfo.getId()).getOa_contact_no();
        if (CommonUtils.isNullOrEmpty(oa_real_no)) {
            Long demandCount = demandCount(db_oa_contact_no);
            if (demandCount == 1) {
                resultBaseUpdate = demandBaseInfoDao.updateImpl(demandBaseInfo);
            } else {
                throw new FdevException(ErrorConstants.DEMAND_INFO_ERROR, new String[]{db_oa_contact_no});
            }
        } else {
            if (demandBaseInfo.getOa_real_no().equalsIgnoreCase(db_oa_contact_no)) {
                Long demandCount = demandCount(db_oa_contact_no);
                if (demandCount == 1) {
                    resultBaseUpdate = demandBaseInfoDao.updateImpl(demandBaseInfo);
                } else {
                    throw new FdevException(ErrorConstants.DEMAND_INFO_ERROR, new String[]{oa_real_no});
                }
            } else {
                demandBaseInfo.setOa_contact_no(demandBaseInfo.getOa_real_no());
                Long demandCount = demandCount(oa_real_no);
                if (demandCount == null || demandCount == 0) {
                    resultBaseUpdate = demandBaseInfoDao.updateImpl(demandBaseInfo);
                } else {
                    throw new FdevException(ErrorConstants.DEMAND_INFO_ERROR, new String[]{oa_real_no});
                }
            }
        }
        if (data.getDemand_status_normal() == 0) {
            //发送邮件给涉及板块中指定的人员通知需求更新成功，发送相关通知及代办项
            Map<String, Object> param = CommonUtils.object2Map(demandBaseInfo);
            notifyContext.notifyContext(Dict.DemandUpdateNotifyImpl, param);
        }
        return resultBaseUpdate;
    }


    @Override
    public DemandBaseInfo queryById(String id) throws Exception {
        DemandBaseInfo demand = demandBaseInfoDao.queryById(id);
        String group = demand.getDemand_leader_group();
        List<RelatePartDetail> listPart = demand.getRelate_part_detail();
        Boolean addImplFlag = false;
        if (!CommonUtils.isNullOrEmpty(listPart)) {
            for (RelatePartDetail relatePartDetail : listPart) {
                if (DemandEnum.DemandAssess_status.EVALUATE_OVER.getValue().equals(relatePartDetail.getAssess_status())) {
                    addImplFlag = true;
                    break;
                }
            }
            if (!addImplFlag) {
                List<FdevImplementUnit> unitList = implementUnitService.queryByDemandId(id);
                if (!CommonUtils.isNullOrEmpty(unitList)) {
                    for (FdevImplementUnit fdevImplementUnit : unitList) {
                        if (!CommonUtils.isNullOrEmpty(fdevImplementUnit.getAdd_flag()) && fdevImplementUnit.getAdd_flag() > 0) {
                            addImplFlag = true;
                            break;
                        }
                    }
                }
            }
            for (RelatePartDetail relatePartDetail : listPart) {
                if (group.equalsIgnoreCase(relatePartDetail.getPart_id())) {
                    demand.setLeader_group_status(relatePartDetail.getAssess_status());
                    break;
                }
            }
            //判断需求是否属于特殊状态
            Integer demandStatusSpecial = demand.getDemand_status_special();
            if (!CommonUtils.isNullOrEmpty(demandStatusSpecial) && demandStatusSpecial == 3) {
                addImplFlag = true;
            }
        }
        demand.setAddImplFlag(addImplFlag);
        if (CommonUtils.isNullOrEmpty(demand.getDemand_flag())) {
            demand.setDemand_flag(DemandEnum.DemandDemand_flag.URGENT.getValue());
        }
        queryIsIpmpUnitLeader(demand);
        //查询全量需求标签
        Set<String> types = new HashSet();
        types.add(Dict.DEMANDLABEL);
        List<DictEntity> demand_label_list = dictDao.queryByTypes(types);
        //处理需求标签
        demandLabelInfo( demand , demand_label_list);

        /**
         * 判断需求状态
         */
        //正常需求状态
        Integer norStatus = demand.getDemand_status_normal();
        //特殊需求状态
        Integer speStatus = demand.getDemand_status_special();
        //实施单元
        Long implLong = implementUnitDao.queryCountByDemandId(id);
        //ui编辑状态初始化
        demand.setUi_status(true);
        if (norStatus >= 7 || (!CommonUtils.isNullOrEmpty(speStatus) && speStatus != 3) || !CommonUtils.isNullOrEmpty(demand.getDesignDoc())) {
            //ui编辑状态为不可修改
            demand.setUi_status(false);
        }
        if (implLong > 0) {
            demand.setHavaImpl(true);
        } else {
            demand.setHavaImpl(false);
        }
        try {
            demand.setDemand_leader_group_cn((String) iRoleService.queryGroup(group).get("name"));
        } catch (Exception e) {
            logger.error("查询牵头小组中文名失败！");
        }

        UserInfo demand_create_user_all = new UserInfo();
        demand_create_user_all.setId(demand.getDemand_create_user());
        if (!CommonUtils.isNullOrEmpty(demand.getDemand_create_user())) {
            Map<String, Object> userMap = roleService.queryUserbyid(demand.getDemand_create_user());
            if (!CommonUtils.isNullOrEmpty(userMap)) {
                demand_create_user_all.setUser_name_cn((String) userMap.get("user_name_cn"));
                demand_create_user_all.setUser_name_en((String) userMap.get("user_name_en"));
            }
        }
        demand.setDemand_create_user_all(demand_create_user_all);
        //查询涉及小组中文名
        HashSet<String> relatePartIds = demand.getRelate_part();
        List<Map<String, String>> groupInfoList = this.queryGroupByIds(relatePartIds);
        if(!CommonUtils.isNullOrEmpty(demand.getRelate_part_detail())){
            for(RelatePartDetail relatePartDetail:demand.getRelate_part_detail()){
                for(Map<String, String> groupInfo:groupInfoList){
                    if(relatePartDetail.getPart_id().equals(groupInfo.get(Dict.ID))){
                        relatePartDetail.setPart_name(groupInfo.get(Dict.NAME));
                    }
                }
            }
        }
        if (CommonUtils.isNullOrEmpty(demand.getRelate_part())) demand.setRelate_part(new HashSet<>());
        if (CommonUtils.isNullOrEmpty(demand.getRelate_part_detail())) demand.setRelate_part_detail(new ArrayList<>());
        if (CommonUtils.isNullOrEmpty(demand.getDemand_leader())) demand.setDemand_leader(new HashSet<>());
        return demand;
    }

    private void queryIsIpmpUnitLeader(DemandBaseInfo demand) throws Exception {
        List<IpmpUnit> ipmpUnits = new ArrayList<>();
        Set<String> ipmpUnitLeaderSet = new HashSet<>();
        if (Constants.TECH.equals(demand.getDemand_type())) {//若是科技需求
            //获取所有研发单元
            List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryCanceledFdevUnitByDemandId(demand.getId());
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> ipmpUnitDetailMap = new HashMap<>();
            if (!CommonUtils.isNullOrEmpty(fdevUnitList)) {
                Set<String> implUnitNumList = new HashSet<>();
                for (FdevImplementUnit fdevUnit : fdevUnitList) {
                    implUnitNumList.add(fdevUnit.getIpmp_implement_unit_no());
                }
                if (!CommonUtils.isNullOrEmpty(implUnitNumList)) {
                    params.put(Dict.IMPLUNITNUMLIST, implUnitNumList);
                    ipmpUnitDetailMap = ipmpUnitDao.queryIpmpUnitByNums(params);
                    ipmpUnits = (List<IpmpUnit>) ipmpUnitDetailMap.get(Dict.DATA);
                }
            }
        } else if (Dict.TECH.equals(demand.getDemand_type())) {//若是业务需求
            ipmpUnits = iIpmpUnitDao.queryIpmpUnitByDemandId(demand.getOa_contact_no());
        } else if (Dict.DAILY.equals(demand.getDemand_type())) {

        }

        if (!CommonUtils.isNullOrEmpty(ipmpUnits)) {
            for (IpmpUnit ipmpUnitDetail : ipmpUnits) {
                if (!CommonUtils.isNullOrEmpty(ipmpUnitDetail.getImplLeader())) {
                    String[] leaderNameArray = ipmpUnitDetail.getImplLeader().split(",");
                    ipmpUnitLeaderSet.addAll(Arrays.asList(leaderNameArray));
                }
            }
        }
        try {
            User user = CommonUtils.getSessionUser();
            demand.setIs_ipmp_unit_leader(ipmpUnitLeaderSet.contains(user.getUser_name_en()));
        } catch (Exception e) {
            demand.setIs_ipmp_unit_leader(false);//获取登录信息失败 默认 false
        }
    }

    @Override
    public Map queryPagination(Map<String, Object> param) {
        Integer pageSize = (Integer) param.get(Dict.SIZE);//页面大小
        Integer currentPage = (Integer) param.get(Dict.INDEX);//当前页
        String keyword = (String) param.get(Dict.KEYWORD);       //检索框
        Integer start = pageSize * (currentPage - 1);   //起始
        List data = demandBaseInfoDao.queryPagination(start, pageSize, keyword);
        Long count = demandBaseInfoDao.queryCount(keyword);
        Map result = new HashMap();
        result.put(Dict.DATA, data);
        result.put(Dict.COUNT, count);
        return result;
    }

    @Override
    public Long queryCount(String keyword) {
        return demandBaseInfoDao.queryCount(keyword);
    }

    /**
     * 如果需求下有任务已挂载投产窗口，不可以暂缓。特殊情况为投产窗口审核不通过
     *
     * @param param
     * @throws Exception
     */
    @Override
    public void defer(Map<String, Object> param) throws Exception {
        String id = (String) param.get(Dict.ID);
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(id);
        //校验用户权限 必须为需求负责人或牵头人员
        User user = CommonUtils.getSessionUser();
        boolean isDemandLeader = roleService.isDemandLeader(id, user.getId());
        String isTransferRqrmnt = demandBaseInfo.getIsTransferRqrmnt();
        if ("1".equals(isTransferRqrmnt)) {
            //老需求模块的存量数据不允许暂缓
            throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{demandBaseInfo.getOa_contact_name()});
        }
        if (!roleService.isDemandManager() && !isDemandLeader) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员或需求牵头人"});
        }
        //待评估和评估中的需求不允许暂缓
        int demand_status_normal = demandBaseInfo.getDemand_status_normal();
        if (demand_status_normal < DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue()) {
            throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{"待评估和评估中不允许暂缓"});
        }
        //投产、归档和撤销需求不可暂缓
        if (DemandEnum.DemandStatusEnum.IS_CANCELED.getValue().equals(demandBaseInfo.getDemand_status_normal())
                || DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue().equals(demandBaseInfo.getDemand_status_normal())
                || DemandEnum.DemandStatusEnum.PRODUCT.getValue().equals(demandBaseInfo.getDemand_status_normal())) {
            throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{"已经投产、归档或撤销的需求不能暂缓"});
        }

        //已经在暂缓的需求不可暂缓
        if (DemandEnum.DemandDeferStatus.DEFER.getValue().equals(demandBaseInfo.getDemand_status_special())
                || DemandEnum.DemandDeferStatus.RECOVER.getValue().equals(demandBaseInfo.getDemand_status_special())) {
            throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{"当前需求" + demandBaseInfo.getOa_contact_name() + "已经为暂缓状态"});
        }
        //调用任务模块接口查询任务是否可以暂缓（查询是否任务挂载了投产窗口）
        Map result = taskService.judgeDeferOrDelete(id);
        boolean isDefer = (boolean) result.get(Dict.IS_DEFER);
        if (!isDefer) {
            throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{"当前需求" + demandBaseInfo.getOa_contact_name() + "下已有任务挂载投产窗口"});
        }
        //设置需求状态为暂缓中
        demandBaseInfo.setDemand_status_special(DemandEnum.DemandDeferStatus.DEFER.getValue());
        //更新暂缓时间
        demandBaseInfo.setDemand_defer_time(TimeUtil.getTimeStamp(new Date()));
        //更新需求表
        demandBaseInfoDao.update(demandBaseInfo);
        //更新所有实施单元状态为暂缓中
        commonBusinessService.updateUnitStatus(id,
                ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue(), Dict.IMPLEMENT_UNIT_STATUS_SPECIAL);
        //更新所有实施单元的暂缓时间
        commonBusinessService.updateUnitDate(id, TimeUtil.getTimeStamp(new Date()), Dict.DEFER_TIME);

        //发送任务模块接口修改任务状态为暂缓中
        try {
            taskService.taskDeferOrRecover(id, DemandEnum.DemandDeferStatus.DEFER.getValue());
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DEMAND_DEFER_FAIL, new String[]{"需求暂缓调用任务失败"});

        }
        //发送邮件给需求管理员、各板块负责人通知需求暂缓
        notifyContext.notifyContext(Dict.DemandDeferNotifyImpl, param);

    }


    /**
     * 需求恢复
     *
     * @param param
     * @throws Exception
     */
    @Override
    public void recover(Map<String, Object> param) throws Exception {
        String id = (String) param.get(Dict.ID);
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(id);
        //校验用户权限 必须为需求负责人或牵头人员
        User user = CommonUtils.getSessionUser();
        boolean isDemandLeader = roleService.isDemandLeader(id, user.getId());
        if (!roleService.isDemandManager() && !isDemandLeader) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员或需求牵头人"});
        }
        if (!DemandEnum.DemandDeferStatus.DEFER.getValue().equals(demandBaseInfo.getDemand_status_special())) {
            throw new FdevException(ErrorConstants.DEMAND_RECOVER_FAIL, new String[]{"请确认当前需求是否为暂缓中需求"});
        }
        //修改需求状态为恢复中
        //如果实施单元为空，需求直接恢复完成
        List<FdevImplementUnit> unitList = implementUnitService.queryByDemandId(id);
        if (CommonUtils.isNullOrEmpty(unitList)) {
            demandBaseInfo.setDemand_status_special(DemandEnum.DemandDeferStatus.FINISTH.getValue());
            demandBaseInfo.setDemand_recover_time(TimeUtil.getTimeStamp(new Date()));
        } else {
            demandBaseInfo.setDemand_status_special(DemandEnum.DemandDeferStatus.RECOVER.getValue());
        }
        //更新需求表
        demandBaseInfoDao.update(demandBaseInfo);
        //更新所有实施单元状态为恢复中
        commonBusinessService.updateUnitStatus(id,
                ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue(), Dict.IMPLEMENT_UNIT_STATUS_SPECIAL);
        //发送邮件给需求管理员、各板块负责人通知需求恢复
        notifyContext.notifyContext(Dict.DemandRecoverNotifyImpl, param);

    }


    @Override
    public void placeOnFile(Map<String, Object> param) throws Exception {
        //只有需求管理员和需求牵头人才可以点击归档
        //需求成为已投产状态时，说明已经无法点击暂缓，则只需要考虑正常状态就行
        String id = param.get(Dict.ID).toString();//需求id
        DemandBaseInfo demandBaseInfo = queryById(id);
        if (CommonUtils.isNullOrEmpty(demandBaseInfo)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        String oa_contact_no = demandBaseInfo.getOa_contact_no();//需求编号
        Integer demand_status_special = demandBaseInfo.getDemand_status_special();
        if ((DemandEnum.DemandDeferStatus.DEFER.getValue() == demand_status_special) || (DemandEnum.DemandDeferStatus.RECOVER.getValue() == demand_status_special)) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_DEFER_ERROR, new String[]{oa_contact_no});
        }
        Integer demand_status_normal = demandBaseInfo.getDemand_status_normal();
        if (DemandEnum.DemandStatusEnum.PRODUCT.getValue() != demand_status_normal) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_PRODUCT_ERROR, new String[]{oa_contact_no});
        }
        boolean isDemandManager = roleService.isDemandManager();
        User user = CommonUtils.getSessionUser();
        boolean isDemandLeader = roleService.isDemandLeader(id, user.getId());
        if (isDemandManager || isDemandLeader) {
            demandBaseInfo.setDemand_status_normal(DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue());
            demandBaseInfoDao.update(demandBaseInfo);
            commonBusinessService.updateUnitStatus(id,
                    ImplementUnitEnum.ImplementUnitStatusEnum.PLACE_ON_FILE.getValue(), Dict.IMPLEMENT_UNIT_STATUS_NORMAL);

        } else {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //将需求下相关的文档上传到gitlab上进行归档
        docService.fileToGitlab(demandBaseInfo);
    }

    /**
     * 查询需求列表
     * 需求列表查询条件描述:
     * 1:根据人员查询:下拉框中可选fdev上全量用户，后台根据需求中科技负责人、各板块评估人、需求创建人三个属性进行匹配，能匹配的需求列表均返回。前端表单回显提醒内容“负责人/评估人、创建者”。
     * 2：根据需求名称、需求编号查询：
     */
    @Override
    public Map queryDemandList(Map map) throws Exception {
        String groupQueryType = (String) map.get("groupQueryType");//小组查询类型

        Integer pageSize = (Integer) map.get(Dict.SIZE);//页面大小

        Integer currentPage = (Integer) map.get(Dict.INDEX);//当前页

        String userid = (String) map.get(Dict.USERID);//负责人

        String keyword = (String) map.get(Dict.KEYWORD);//检索框:根据需求名称、需求编号查询

        String type = (String) map.get(Dict.DEMANDTYPE);//类型

        List<String> state = (List<String>) map.get(Dict.STATES);//正常需求状态

        List<String> demand_label = (List<String>) map.get(Dict.DEMAND_LABEL);//需求标签


        //状态天数
        String sNum = (String) map.get(Dict.STATENUM);
        Integer stateNum = CommonUtils.isNullOrEmpty(sNum) ? null : (Integer.valueOf(sNum));

        String featureType = (String) map.get(Dict.FEATURETYPE);//预进行状态 类型
        // 预进行天数
        String fNum = (String) map.get(Dict.FEATURENUM);
        Integer featureNum = CommonUtils.isNullOrEmpty(fNum) ? null : (Integer.valueOf(fNum));

        Boolean relevant = (Boolean) map.get(Dict.RELEVENT);//与我有关

        List<String> groupid = (List<String>) map.get(Dict.GROUP_ID);
        //小組
        List<String> groupids = new ArrayList<String>();
        if (CommonUtils.isNullOrEmpty(groupQueryType) || "1".equals(groupQueryType)) {
            if (!CommonUtils.isNullOrEmpty(groupid)) {
                groupids.addAll(groupUtil.getGroupByParent(groupid));
                groupid = groupids;
            }

        }

        String groupState = (String) map.get(Dict.GROUPSTATE);        //牵头小组还是涉及小组
        List<DemandBaseInfo> rqrmntList = new ArrayList<DemandBaseInfo>();

        //返回集：
        Map resData = new HashMap();

        String datetype = (String) map.get(Dict.DATETYPE);     //延期类型

        //延期选项天数
        String datetypeNum = (String) map.get(Dict.DELAYNUM);
        Integer delayNum = CommonUtils.isNullOrEmpty(datetypeNum) ? 0 : (Integer.valueOf(datetypeNum));

        String sortBy = (String) map.get(Dict.SORTBY);             //排序
        Boolean descending = (Boolean) map.get(Dict.DESCENDING);     //是否降序

        String priority = (String) map.get(Dict.PRIORITY); //优先级

        String isCodeCheck = (String) map.get(Dict.ISSUBMITCODEREVIEW); //是否提交代码审核

        /*
         * 实际日期类型
         * */
        List<String> relDateType = (List<String>) map.get(Dict.RELDATETYPE); //实际日期类型
        String relStartDate = (String) map.get(Dict.RELSTARTDATE); //实际起始时间
        String relEndDate = (String) map.get(Dict.RELENDDATE); //实际结束时间

        String designState = (String) map.get(Dict.DESIGNSTATE); //设计稿审核状态

        if (relevant) {  //如果与我相关，，，那么需要 把当前登录的用户作为 负责人查询
            User user = MyUtils.getLoginUser();
            if (!CommonUtils.isNullOrEmpty(user))
                userid = user.getId();
        }

        List<DemandBaseInfo> data = new ArrayList<>();

        //组装 状态及天数的 过滤时间条件
        List<HashMap> stateListMap = DemandBaseInfoUtil.getStateDate(state, stateNum);
        //计算 预进行状态的 过滤时间条件
        Map featureDate = DemandBaseInfoUtil.getFeatureDate(featureType, featureNum);
        //计算 延期状态的 过滤时间条件
        Map delayDate = DemandBaseInfoUtil.getDelayDate(datetype, delayNum);
        //计算 实际日期的 过滤时间条件
        Map relDateMap = DemandBaseInfoUtil.getRelDate(relDateType, relStartDate, relEndDate);

        //延期 和 预进行不能 同时过滤
        if (!CommonUtils.isNullOrEmpty(featureDate) && !CommonUtils.isNullOrEmpty(delayDate)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"", "延期和预进行不能同时进行过滤"});
        }

        String group = null;

        //查询全量需求标签
        Set<String> types = new HashSet();
        types.add(Dict.DEMANDLABEL);
        List<DictEntity> demand_label_list = dictDao.queryByTypes(types);

        if (pageSize.toString().equals("0")) {

            data = demandBaseInfoDao.queryDemandList(isCodeCheck, userid, keyword, delayDate, groupid, state, stateListMap, featureDate, type, sortBy, descending, priority, relDateMap, groupState, designState,demand_label);  //查询所有
            for (int i = 0; i < data.size(); i++) {
                // 判断需求状态
                //正常需求状态
                Integer norStatus = data.get(i).getDemand_status_normal();
                //特殊需求状态
                Integer speStatus = data.get(i).getDemand_status_special();
                //ui编辑状态初始化
                data.get(i).setUi_status(true);
                if (norStatus > 1 || (!CommonUtils.isNullOrEmpty(speStatus) && speStatus != 3)) {
                    //ui编辑状态为不可修改
                    data.get(i).setUi_status(false);
                }
                //处理需求标签
                demandLabelInfo( data.get(i) , demand_label_list);
            }
        } else {
            Integer start = pageSize * (currentPage - 1);   //起始
            data = demandBaseInfoDao.queryDemandBaseInfos(isCodeCheck, start, pageSize, userid, keyword, delayDate, groupid, state, stateListMap, featureDate, type, sortBy, descending, priority, relDateMap, groupState, designState,demand_label);    //分页查询
            Set<String> groupIdList = new HashSet<>();
            for (int i = 0; i < data.size(); i++) {
                groupIdList.add(data.get(i).getDemand_leader_group());
                // 判断需求状态
                //正常需求状态
                Integer norStatus = data.get(i).getDemand_status_normal();
                //特殊需求状态
                Integer speStatus = data.get(i).getDemand_status_special();
                //ui编辑状态初始化
                data.get(i).setUi_status(true);
                if (norStatus > 1 || (!CommonUtils.isNullOrEmpty(speStatus) && speStatus != 3)) {
                    //ui编辑状态为不可修改
                    data.get(i).setUi_status(false);
                }

            }

            List<Map<String, String>> groupList = queryGroupByIds(groupIdList);
            Map<String, String> groupIdNameMap = new HashMap<>();
            for (Map<String, String> item : groupList) {
                groupIdNameMap.put(item.get("id"), item.get("name"));
            }
            for (DemandBaseInfo item : data) {
                //处理需求标签
                demandLabelInfo( item , demand_label_list);
                item.setDemand_leader_group_cn(groupIdNameMap.get(item.getDemand_leader_group()));
                queryIsIpmpUnitLeader(item);
                if (CommonUtils.isNullOrEmpty(item.getRelate_part())) item.setRelate_part(new HashSet<>());
                if (CommonUtils.isNullOrEmpty(item.getRelate_part_detail()))
                    item.setRelate_part_detail(new ArrayList<>());
                if (CommonUtils.isNullOrEmpty(item.getDemand_leader())) item.setDemand_leader(new HashSet<>());
                //添加延期需求标志
                item.setDelayFlag(isDelay(item));
            }
        }

        //总数
        resData.put("count", demandBaseInfoDao.queryDemandsCount(isCodeCheck, userid, keyword, delayDate, groupid, state, stateListMap, featureDate, type, sortBy, descending, priority, relDateMap, groupState, designState,demand_label));
        resData.put("demands", data);
        resData.put("page", currentPage);
        resData.put("size", pageSize);
        return resData;

    }

    //判断需求是否延期
    private Boolean isDelay(DemandBaseInfo demandBaseInfo) throws ParseException {
        if(CommonUtils.isDelay(demandBaseInfo.getPlan_start_date(), demandBaseInfo.getReal_start_date())) return true;
        if(CommonUtils.isDelay(demandBaseInfo.getPlan_inner_test_date(), demandBaseInfo.getReal_inner_test_date())) return true;
        if(CommonUtils.isDelay(demandBaseInfo.getPlan_test_date(), demandBaseInfo.getReal_test_date())) return true;
        if(CommonUtils.isDelay(demandBaseInfo.getPlan_test_finish_date(), demandBaseInfo.getReal_test_finish_date())) return true;
        if(CommonUtils.isDelay(demandBaseInfo.getPlan_product_date(), demandBaseInfo.getReal_product_date())) return true;
        return false;
    }

    private void demandLabelInfo(DemandBaseInfo demandBaseInfo ,List<DictEntity> demand_label_list) {
        Set<String> demand_label = demandBaseInfo.getDemand_label();
        List<Map<String, Object>> demand_label_info = new ArrayList<>();
        if( !CommonUtils.isNullOrEmpty(demand_label) ){
            for ( DictEntity dictEntity : demand_label_list ) {
                Map<String, Object> labelInfo = new HashMap<>();
                //当前需求是否包含该标签
                if( demand_label.contains( dictEntity.getCode() ) ){
                    for ( String label : demand_label ) {
                        if( label.equals(dictEntity.getCode()) ){
                            labelInfo.put(Dict.LABEL,dictEntity.getValue());
                            labelInfo.put(Dict.FLAG,true);
                            labelInfo.put(Dict.COLOR,dictEntity.getColor());
                            labelInfo.put(Dict.CODE,dictEntity.getCode());
                        }
                    }
                } else {
                    labelInfo.put(Dict.LABEL,dictEntity.getValue());
                    labelInfo.put(Dict.FLAG,false);
                    labelInfo.put(Dict.COLOR,dictEntity.getColor());
                    labelInfo.put(Dict.CODE,dictEntity.getCode());
                }
                demand_label_info.add(labelInfo);
            }
        } else {
            for ( DictEntity dictEntity : demand_label_list ) {
                Map<String, Object> labelInfo = new HashMap<>();
                labelInfo.put(Dict.LABEL,dictEntity.getValue());
                labelInfo.put(Dict.FLAG,false);
                labelInfo.put(Dict.COLOR,dictEntity.getColor());
                labelInfo.put(Dict.CODE,dictEntity.getCode());
                demand_label_info.add(labelInfo);
            }
        }
        demandBaseInfo.setDemand_label_info( demand_label_info );
    }

    /**
     * 批量查询小组的中文名
     *
     * @param groupIdList 小组ID的集合list
     */
    private List<Map<String, String>> queryGroupByIds(Set<String> groupIdList) throws Exception {
        try {
            return (List<Map<String, String>>) restTransport.submit(new HashMap() {{
                put("groupIds", groupIdList);
                put("allFlag",true);
                put(Dict.REST_CODE, "queryGroupByIds");
            }});
        } catch (Exception e) {
            logger.error("查询小组中文名失败！" + e.getMessage());
            return null;
        }
    }

    /**
     * 需求撤销
     *
     * @param param
     */
    @Override
    public void repeal(Map<String, Object> param) throws Exception {
        String id = (String) param.get(Dict.ID);
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(id);
        //校验用户权限 必须为需求负责人或牵头人员
        User user = CommonUtils.getSessionUser();
        boolean isDemandLeader = roleService.isDemandLeader(id, user.getId());
        if (!roleService.isDemandManager() && !isDemandLeader) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员或需求牵头人"});
        }
        if (DemandEnum.DemandStatusEnum.IS_CANCELED.getValue().equals(demandBaseInfo.getDemand_status_normal())) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_REPEAL, new String[]{"当前需求已为撤销状态"});
        }
        //已归档不可撤销
        if (DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue().equals(demandBaseInfo.getDemand_status_normal())) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_REPEAL, new String[]{"已归档需求不可撤销"});
        }
        //如果需求在暂缓和恢复中不允许撤销
        if (DemandEnum.DemandDeferStatus.DEFER.getValue().equals(demandBaseInfo.getDemand_status_special())) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_REPEAL, new String[]{"需求处于暂缓状态"});
        } else if (DemandEnum.DemandDeferStatus.RECOVER.getValue().equals(demandBaseInfo.getDemand_status_special())) {
            throw new FdevException(ErrorConstants.DEMAND_CANNOT_REPEAL, new String[]{"需求处于恢复中状态"});
        }
        //调用任务模块接口查询任务，判断是否可以撤销
        Map result = taskService.judgeDeferOrDelete(id);
        boolean isDelete = (boolean) result.get(Dict.IS_DELETE);
        if (!isDelete) {
            throw new FdevException(ErrorConstants.DEMAND_DELETE_FAIL, new String[]{"当前需求" + demandBaseInfo.getOa_contact_name() + "下已有任务，请先将任务废弃"});
        }
        //修改需求状态为已撤销
        demandBaseInfo.setDemand_status_normal(DemandEnum.DemandStatusEnum.IS_CANCELED.getValue());
        //更新撤销时间
        demandBaseInfo.setDemand_delete_time(TimeUtil.getTimeStamp(new Date()));
        //更新所有研发单元单元状态为撤销
        commonBusinessService.updateUnitStatus(id,
                ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue(), Dict.IMPLEMENT_UNIT_STATUS_NORMAL);
        //更新所有研发单元的撤销时间
        commonBusinessService.updateUnitDate(id, TimeUtil.getTimeStamp(new Date()), Dict.DELETE_TIME);
        //日常需求删除其他需求任务
        if (Dict.DAILY.equals(demandBaseInfo.getDemand_type())) {
            otherDemandTaskService.deleteAllOtherDemandTask(demandBaseInfo);
        }
        //更新需求表
        demandBaseInfoDao.update(demandBaseInfo);
        //调用玉衡，删除对应工单
        commonBusinessService.deleteOrder(id);
    }

    @Override
    public List<Map<String, Object>> queryTaskByDemandId(Map<String, Object> param) {
        String demand_id = param.get(Dict.DEMAND_ID).toString();
        List<FdevImplementUnit> listImpl = implementUnitService.queryByDemandId(demand_id);
        List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
        if (!CommonUtils.isNullOrEmpty(listImpl)) {
            for (FdevImplementUnit fdevImplementUnit : listImpl) {
                //根据实施单元编号查任务列表
            }
        }
        return null;
    }

    @Override
    public List<DemandBaseInfo> queryDemandByIds(Set<String> ids) throws Exception {
        if (CommonUtils.isNullOrEmpty(ids))
            return null;
        List<DemandBaseInfo> resList = demandBaseInfoDao.queryDemandByIds(ids);
        if (CommonUtils.isNullOrEmpty(resList)) {
            return null;
        }
        return resList;
    }

    @Override
    public List<DemandBaseInfo> queryDemandByGroupId(List<String> groupIds) {
        return demandBaseInfoDao.queryDemandByGroupId(groupIds);
    }

    @Override
    public List<DemandBaseInfo> queryDemandByGroupIdAndPriority(Set<String> groupids, String priority, Boolean isParent) throws Exception {
        return demandBaseInfoDao.queryDemandByGroupIdAndPriority(groupids, priority, isParent);
    }

    @Override
    public List<DemandBaseInfo> queryDemandByGroupId(Set<String> groupId) {
        return demandBaseInfoDao.queryDemandByGroupId(groupId);
    }

    @Override
    public DemandBaseInfo updateRqrmntUiReporter(String rqrId, String uiDesignReporter) {
        Map uiDesignRole = roleService.queryFdevRoleByName("UI团队设计师");
        String designId = (String) uiDesignRole.get(Dict.ID);
        List ids = new ArrayList<>();
        ids.add(uiDesignReporter);
        Map users = roleService.queryByUserCoreData(ids);
        LinkedHashMap userInfo = (LinkedHashMap) users.get(uiDesignReporter);
        if (CommonUtils.isNullOrEmpty(userInfo)) {
            throw new FdevException(ErrorConstants.FUSER_QUERY_NOTFOUND);
        }
        List<String> roles = (List<String>) userInfo.get(Dict.ROLEID);
        //设置ui设计稿审核人时，需要校验 修改的用户是否拥有 UI团队设计师 权限
        if (!roles.contains(designId)) { //如果当前用户没有权限
            throw new FdevException(ErrorConstants.FUSER_ROLE_ERROR, new String[]{"UI团队设计师"});
        }

        DemandBaseInfo demand = demandBaseInfoDao.updateRqrmntUiReporter(rqrId, uiDesignReporter);
        sendUsersToDo(demand, ids, "infoTo");
        return demand;
    }


    /**
     * 给用户发送ui设计稿   用户通知、 代办事项、 邮件通知
     */
    @Override
    public void sendUsersToDo(DemandBaseInfo demand, List<String> userIds, String type) {
        if (CommonUtils.isNullOrEmpty(userIds)) {
            logger.error("----->" + "userIds is null!");
            return;
        }
        List<String> to = new ArrayList<>();
        List<String> userNames = new ArrayList<>();
        Map userInfos = roleService.queryByUserCoreData(userIds);
        for (String userId : userIds) {
            LinkedHashMap userInfo = (LinkedHashMap) userInfos.get(userId);
            if (!CommonUtils.isNullOrEmpty(userInfo)) {
                userNames.add((String) userInfo.get(Dict.USERNAMEEN));
                to.add(userInfo.get(Dict.EMAIL).toString());
            }
        }
        roleService.addCommissionEvent(demand, userIds, type);
        //给用户添加通知
        String content = "在需求编号 " + demand.getOa_contact_no() + " 下，存在ui设计稿的审核。";
        roleService.sendNotify(userNames, content, demand.getId());
        //给用户发邮件通知
        if (isEmail) {
            HashMap model = new HashMap();
            model.put("demand", demand);
            /**
             * @// TODO: 2020/11/6
             */
            String link = demandIp + "/fdev/#/rqrmn/designReviewRqr/" + demand.getId();
            model.put("link", link);
            try {
                mailService.sendEmailPath("需求ui设计稿代办通知", "fdemand_uiDocToDo", model, to, null);
            } catch (Exception e) {
                logger.error("#########发送ui设计稿代办通知邮件失败########" + e.getMessage());
                throw new FdevException("#########发送ui设计稿代办通知邮件失败########");
            }
        }
    }

    @Override
    public void updateDemandForTask(Map<String, Object> param) throws Exception {
        String demandId = (String) param.get(Dict.DEMAND_ID);
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        if (null == demandBaseInfo)
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务所属需求不存在"});
        //已归档的需求不做状态和时间修改
        if (DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue().equals(demandBaseInfo.getDemand_status_normal()))
            return;
        //需求处于预评估
        String stage = (String) param.get(Dict.STAGE);
        String demandRealStartDate = (String) param.get(Dict.REAL_START_DATE);//实际启动时间，(任务)最早
        String demandRealInnerTestDate = (String) param.get(Dict.REAL_INNER_TEST_DATE);//实际内测时间（SIT）最晚
        String demandRealTestDate = (String) param.get(Dict.REAL_TEST_DATE);//实际业测时间（UAT）最晚
        String demandRealTestFinishDate = (String) param.get(Dict.REAL_TEST_FINISH_DATE);//实际测试完成时间（REL）最晚
        String demandRealProductDate = (String) param.get(Dict.REAL_PRODUCT_DATE);//时间投产时间 最晚
        Integer demandStage = CommonUtils.pareStage(stage);
        //获取所有实施单元的状态和时间，修改需求相关信息
        List<FdevImplementUnit> unitList = implementUnitDao.queryByDemandId(demandId);
        String isTransferRqrmnt = demandBaseInfo.getIsTransferRqrmnt();
        if (CommonUtils.isNullOrEmpty(unitList)) {
            //需求没有实施单元，直接给任务的状态，只有老需求才可以改需求的状态
            if ("1".equals(isTransferRqrmnt) || "2".equals(isTransferRqrmnt)) {
                demandBaseInfo.setDemand_status_normal(demandStage);
                if (null != demandRealStartDate)
                    demandBaseInfo.setReal_start_date(demandRealStartDate);
                if (null != demandRealInnerTestDate)
                    demandBaseInfo.setReal_inner_test_date(demandRealInnerTestDate);
                if (null != demandRealTestDate)
                    demandBaseInfo.setReal_test_date(demandRealTestDate);
                if (null != demandRealTestFinishDate)
                    demandBaseInfo.setReal_test_finish_date(demandRealTestFinishDate);
                if (null != demandRealProductDate)
                    demandBaseInfo.setReal_product_date(demandRealProductDate);
                demandBaseInfoDao.update(demandBaseInfo);
            }
        } else {
            List<FdevImplementUnit> collect = unitList.stream()
                    .sorted(Comparator.comparingInt(FdevImplementUnit::getImplement_unit_status_normal))
                    .collect(Collectors.toList());
            //获取所有实施单元最小阶段
            Integer minStage = collect.get(0).getImplement_unit_status_normal();
            if (minStage >= ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()) {
                //需要考虑未新增实施单元板块
                List<RelatePartDetail> relatePartDetailList1 = demandBaseInfo.getRelate_part_detail();
                if (!CommonUtils.isNullOrEmpty(relatePartDetailList1)) {
                    for (RelatePartDetail relatePartDetail : relatePartDetailList1) {
                        if (relatePartDetail.getAssess_status().compareToIgnoreCase(DemandEnum.DemandAssess_status.EVALUATE_OVER.getValue()) < 0) {
                            //说明还有个板块一定是在预评估的，该板块未新增实施单元
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue();
                            break;
                        }
                    }
                }
            }
            //待评估和评估中的需求仅修改状态
            if (DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue().equals(minStage) || DemandEnum.DemandStatusEnum.EVALUATE.getValue().equals(minStage)) {
                demandBaseInfo.setDemand_status_normal(minStage);
                demandBaseInfoDao.update(demandBaseInfo);
                return;
            }
            //如果实施单元在待实施状态，如果有一个状态进入开发中或之后的状态，状态改为开发中
            if (ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue().equals(minStage)) {
                for (FdevImplementUnit fdevImplementUnit1 : unitList) {
                    if (fdevImplementUnit1.getImplement_unit_status_normal() > ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()) {
                        minStage = ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue();
                        demandBaseInfo.setDemand_status_normal(DemandEnum.DemandStatusEnum.DEVELOP.getValue());
                        unitList = unitList.stream().filter(x -> !x.getImplement_unit_status_normal().equals(ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue())).collect(Collectors.toList());
                        break;
                    }
                }
            }
            //获取所有任务的最小阶段
            List<Map> taskList = taskService.queryTaskByDemandId(demandBaseInfo.getId());
            List<Map> trueTaskInfo = dealFdevTaskUnit.getTrueTaskInfo(taskList);
            Integer taskMinStage;
            if (!CommonUtils.isNullOrEmpty(trueTaskInfo)) {
                List<Integer> stageList = new ArrayList<>();
                trueTaskInfo.stream().forEach(task -> {
                    Integer taskStage = (Integer) task.get(Dict.STAGE);
                    stageList.add(taskStage);
                });
                Collections.sort(stageList);
                taskMinStage = stageList.get(0);
                if (ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue().equals(taskMinStage)) {
                    for (int i = 0; i < stageList.size(); i++) {
                        if (stageList.get(i) > ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()) {
                            taskMinStage = DemandEnum.DemandStatusEnum.DEVELOP.getValue();
                            trueTaskInfo = trueTaskInfo.stream().filter(x -> (Integer) x.get(Dict.STAGE) > 2).collect(Collectors.toList());
                        }
                    }
                }
                //对实施单元和任务的阶段进行比较，去较小的阶段和时间
                minStage = minStage > taskMinStage ? taskMinStage : minStage;
                demandBaseInfo.setDemand_status_normal(minStage);
                switch (minStage) {
                    //待实施
                    case 2:
                        demandBaseInfo.setReal_start_date("");
                        demandBaseInfo.setReal_inner_test_date("");
                        demandBaseInfo.setReal_test_date("");
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        demandBaseInfoDao.update(demandBaseInfo);
                        break;
                    //开发中
                    case 3:
                        demandBaseInfo.setReal_start_date(
                                CommonUtils.getLittleTime(dealTaskUnit.getRealStartDate(unitList), dealFdevTaskUnit.getRealStartDate(trueTaskInfo)));
                        demandBaseInfo.setReal_inner_test_date("");
                        demandBaseInfo.setReal_test_date("");
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        demandBaseInfoDao.update(demandBaseInfo);
                        break;
                    //SIT
                    case 4:
                        demandBaseInfo.setReal_start_date(
                                CommonUtils.getLittleTime(dealTaskUnit.getRealStartDate(unitList), dealFdevTaskUnit.getRealStartDate(trueTaskInfo)));
                        demandBaseInfo.setReal_inner_test_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealInnerTestDate(unitList), dealFdevTaskUnit.getRealInnerTestDate(trueTaskInfo)));
                        demandBaseInfo.setReal_test_date("");
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        demandBaseInfoDao.update(demandBaseInfo);
                        break;
                    //UAT
                    case 5:
                        demandBaseInfo.setReal_start_date(
                                CommonUtils.getLittleTime(dealTaskUnit.getRealStartDate(unitList), dealFdevTaskUnit.getRealStartDate(trueTaskInfo)));
                        demandBaseInfo.setReal_inner_test_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealInnerTestDate(unitList), dealFdevTaskUnit.getRealInnerTestDate(trueTaskInfo)));
                        demandBaseInfo.setReal_test_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealTestDate(unitList), dealFdevTaskUnit.getRealTestDate(trueTaskInfo)));
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        demandBaseInfoDao.update(demandBaseInfo);
                        break;
                    //REL
                    case 6:
                        demandBaseInfo.setReal_start_date(
                                CommonUtils.getLittleTime(dealTaskUnit.getRealStartDate(unitList), dealFdevTaskUnit.getRealStartDate(trueTaskInfo)));
                        demandBaseInfo.setReal_inner_test_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealInnerTestDate(unitList), dealFdevTaskUnit.getRealInnerTestDate(trueTaskInfo)));
                        demandBaseInfo.setReal_test_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealTestDate(unitList), dealFdevTaskUnit.getRealTestDate(trueTaskInfo)));
                        demandBaseInfo.setReal_test_finish_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealTestFinishDate(unitList), dealFdevTaskUnit.getRealTestFinishDate(trueTaskInfo)));
                        demandBaseInfo.setReal_product_date("");
                        demandBaseInfoDao.update(demandBaseInfo);
                        break;
                    //已投产
                    case 7:
                        demandBaseInfo.setReal_start_date(
                                CommonUtils.getLittleTime(dealTaskUnit.getRealStartDate(unitList), dealFdevTaskUnit.getRealStartDate(trueTaskInfo)));
                        demandBaseInfo.setReal_inner_test_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealInnerTestDate(unitList), dealFdevTaskUnit.getRealInnerTestDate(trueTaskInfo)));
                        demandBaseInfo.setReal_test_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealTestDate(unitList), dealFdevTaskUnit.getRealTestDate(trueTaskInfo)));
                        demandBaseInfo.setReal_test_finish_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealTestFinishDate(unitList), dealFdevTaskUnit.getRealTestFinishDate(trueTaskInfo)));
                        demandBaseInfo.setReal_product_date(
                                CommonUtils.getElderTime(dealTaskUnit.getRealProductDate(unitList), dealFdevTaskUnit.getRealProductDate(trueTaskInfo)));
                        demandBaseInfoDao.update(demandBaseInfo);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public DemandBaseInfo queryDemandByImplUnitsIdAndPriority(String implUnitsId, String priority) {
        return demandBaseInfoDao.queryDemandByImplUnitsIdAndPriority(implUnitsId, priority);
    }

    @Override
    public DemandBaseInfo queryDemandByImplUnitsId(String implUnitsId) {
        return demandBaseInfoDao.queryDemandByImplUnitsId(implUnitsId);
    }

    @Override
    public DemandBaseInfo calcWorkload(DemandBaseInfo demandBaseInfo) {
        if (CommonUtils.isNullOrEmpty(demandBaseInfo.getId())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"需求id"});
        }
        List<FdevImplementUnit> listImpl = implementUnitDao.queryImplPreImplmentByDemandId(demandBaseInfo.getId());
        double deptWorkload = 0.0;
        double companyWorkload = 0.0;
        if (!CommonUtils.isNullOrEmpty(listImpl)) {
            for (FdevImplementUnit fdevImplementUnit : listImpl) {
                deptWorkload += fdevImplementUnit.getDept_workload();
                companyWorkload += fdevImplementUnit.getCompany_workload();
            }
        }
        demandBaseInfo.setDept_workload(deptWorkload);
        demandBaseInfo.setCompany_workload(companyWorkload);
        return demandBaseInfo;
    }

    @Override
    public Map<String, Object> queryPartInfo(Map<String, Object> param) throws Exception {
        Map<String, Object> queryPartInfo = new HashMap<>();
        List<String> listPartInfoAdd = new ArrayList<>();
        List<String> listPartInfoSupply = new ArrayList<>();
        //如果这个人为需求管理员、需求牵头人、实施单元牵头人，则可以新增/补充所有板块，如果为板块评估人员，则可点击涉及的板块
        String demandId = param.get(Dict.DEMAND_ID).toString();
        boolean isDemandManager = roleService.isDemandManager();
        User user = CommonUtils.getSessionUser();
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        boolean isDemandLeader = roleService.isDemandLeader(demandId, user.getId());
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(demandId, user.getUser_name_en());
        if (Dict.DAILY.equals(demandBaseInfo.getDemand_type())) {
            isIpmpUnitLeader = roleService.isOtherDemandTaskLeader(demandId, user.getId());
        }
        if (CommonUtils.isNullOrEmpty(demandBaseInfo)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        ArrayList<RelatePartDetail> relate_part_detail = demandBaseInfo.getRelate_part_detail();
        if (!CommonUtils.isNullOrEmpty(relate_part_detail)) {
            if (isDemandManager || isDemandLeader || isIpmpUnitLeader) {
                for (RelatePartDetail relatePartDetail : relate_part_detail) {
                    queryPartInfo = getMapPartInfo(demandId, relatePartDetail, listPartInfoAdd, listPartInfoSupply);
                }
            } else {
                for (RelatePartDetail relatePartDetail : relate_part_detail) {
                    if (relatePartDetail.getAssess_user().contains(user.getId())) {
                        //如果涉及板块包含这个用户
                        queryPartInfo = getMapPartInfo(demandId, relatePartDetail, listPartInfoAdd, listPartInfoSupply);
                    }
                }
            }
        }
        String partId = param.get(Dict.PART_ID).toString();

        boolean addLight = false;//新增实施单元不亮
        boolean supplyLight = false;//补充实施单元不亮
        if (DemandEnum.DemandStatusEnum.IS_CANCELED.getValue().equals(demandBaseInfo.getDemand_status_normal())
                || DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue().equals(demandBaseInfo.getDemand_status_normal())
                ||(!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_status_special())
                && (DemandEnum.DemandDeferStatus.DEFER.getValue().equals(demandBaseInfo.getDemand_status_special())
                || DemandEnum.DemandDeferStatus.RECOVER.getValue().equals(demandBaseInfo.getDemand_status_special())))) {
            queryPartInfo.put("addLight", addLight);
            queryPartInfo.put("supplyLight", supplyLight);
            queryPartInfo.put("addWords", "SpecialStatus");
            queryPartInfo.put("supplyWords", "SpecialStatus");
            return queryPartInfo;
        }
        if (!CommonUtils.isNullOrEmpty(listPartInfoAdd) && listPartInfoAdd.contains(partId)) {
            //新增实施单元亮
            addLight = true;
        }
        if (!CommonUtils.isNullOrEmpty(listPartInfoSupply) && listPartInfoSupply.contains(partId)) {
            //补充实施单元亮
            supplyLight = true;
        }
        queryPartInfo.put("addLight", addLight);
        queryPartInfo.put("supplyLight", supplyLight);
        queryPartInfo = getButtonWords(demandBaseInfo, isDemandManager, isIpmpUnitLeader, isDemandLeader, partId, queryPartInfo, user);

        //判断补充的时候，小组不能选的原因
        Map canNotSupplyReason = new HashMap();
        //判断新增的时候，小组不能选的原因
        Map canNotAddReason = new HashMap();
        //补充研发单元时，这些小组不可选的原因
        //1、该用户不是需求牵头人、需求管理员、实施单元牵头人，仅为某小组评估人，则无权限在其他小组新增或补充
        //2、小组已完成初次评估，不可新增。小组未完成初次评估，不可补充
        for (RelatePartDetail relatePartDetail : relate_part_detail) {
            if (!(isDemandManager || isDemandLeader || isIpmpUnitLeader)) {
                if (!listPartInfoSupply.contains(relatePartDetail.getPart_id())) {
                    if (!relatePartDetail.getAssess_user().contains(user.getId())) {
                        //用户无权限在此小组补充
                        canNotSupplyReason.put(relatePartDetail.getPart_id(), "当前用户无权限在此小组补充研发单元");
                    } else {
                        //此小组还未完成初次评估，不可补充研发单元
                        canNotSupplyReason.put(relatePartDetail.getPart_id(), "此小组还未完成初次评估，不可补充研发单元");

                    }
                }

                if (!listPartInfoAdd.contains(relatePartDetail.getPart_id())) {
                    if (relatePartDetail.getAssess_user().contains(user.getId())) {
                        //此小组已完成初次评估，不可新增研发单元
                        canNotAddReason.put(relatePartDetail.getPart_id(), "此小组已完成初次评估，不可新增研发单元");
                    } else {
                        //用户无权限在此小组新增
                        canNotAddReason.put(relatePartDetail.getPart_id(), "当前用户无权限在此小组新增研发单元");
                    }
                }
            }
        }

        queryPartInfo.put("canNotAddReason", canNotAddReason);
        queryPartInfo.put("canNotSupplyReason", canNotSupplyReason);
        return queryPartInfo;
    }

    public Map<String, Object> getMapPartInfo(String demandId, RelatePartDetail relatePartDetail, List<String> listPartInfoAdd, List<String> listPartInfoSupply) {
        Map<String, Object> getMapPartInfo = new HashMap<>();
        if ("0".equals(relatePartDetail.getAssess_status())) {
            //板块状态为0：预评估，说明这个板块一定是新增
            listPartInfoAdd.add(relatePartDetail.getPart_id());
        } else if ("2".equals(relatePartDetail.getAssess_status())) {
            //板块状态为2：评估完成，说明这个板块一定是补充
            listPartInfoSupply.add(relatePartDetail.getPart_id());
        } else if ("1".equals(relatePartDetail.getAssess_status())) {
            //板块状态为1：评估中，说明这个板块有研发单元，需要去判断研发单元的最大值
            FdevImplementUnit fdevImplementUnits = implementUnitDao.queryByDemandIdAndGroupMax(demandId, relatePartDetail.getPart_id());
            if (!CommonUtils.isNullOrEmpty(fdevImplementUnits)) {
                if ((CommonUtils.isNullOrEmpty(fdevImplementUnits.getAdd_flag())
                        || 0 == fdevImplementUnits.getAdd_flag()) && (CommonUtils.isNullOrEmpty(fdevImplementUnits.getImplement_unit_status_normal()) || 1 == fdevImplementUnits.getImplement_unit_status_normal())) {
                    //如果实施单元的补充标识为null或者为0，且还在评估中，说明该板块是新增
                    listPartInfoAdd.add(relatePartDetail.getPart_id());
                } else {
                    listPartInfoSupply.add(relatePartDetail.getPart_id());
                }
            } else {
                listPartInfoAdd.add(relatePartDetail.getPart_id());
            }

        }
        getMapPartInfo.put("addPart", listPartInfoAdd);
        getMapPartInfo.put("supplyPart", listPartInfoSupply);
        return getMapPartInfo;
    }

    /**
     * 批量查询小组信息
     *
     * @param groupIds
     * @return
     */
    public List<Map> getGroupInfoByGroupIds(List<String> groupIds) {
        List<Map> result = new ArrayList<>();
        for (String groupId : groupIds) {
            Map<String, Object> groupInfo = roleService.queryByGroupId(groupId);
            result.add(groupInfo);
        }
        return result;
    }


    public Map<String, Object> getButtonWords(DemandBaseInfo demandBaseInfo, boolean isDemandManager, boolean isIpmpUnitLeader, boolean isDemandLeader, String partId, Map<String, Object> queryPartInfo, User user) {
        ArrayList<RelatePartDetail> relate_part_detail = demandBaseInfo.getRelate_part_detail();
        if (!CommonUtils.isNullOrEmpty(relate_part_detail)) {
            for (RelatePartDetail relatePartDetail : relate_part_detail) {
                if (relatePartDetail.getPart_id().equals(partId)) {
                    if (isDemandManager || isDemandLeader || isIpmpUnitLeader) {
                        if ("2".equals(relatePartDetail.getAssess_status())) {
                            //该板块已评估完成
                            queryPartInfo.put("addWords", "CompleteAssess");
                            break;
                        } else if ("0".equals(relatePartDetail.getAssess_status())) {
                            queryPartInfo.put("supplyWords", "NoCompleteAssess");
                            break;
                        } else if ("1".equals(relatePartDetail.getAssess_status())) {
                            FdevImplementUnit fdevImplementUnits = implementUnitDao.queryByDemandIdAndGroupMax(demandBaseInfo.getId(), relatePartDetail.getPart_id());
                            if (!CommonUtils.isNullOrEmpty(fdevImplementUnits)) {
                                if ((CommonUtils.isNullOrEmpty(fdevImplementUnits.getAdd_flag())
                                        || 0 == fdevImplementUnits.getAdd_flag()) && (CommonUtils.isNullOrEmpty(fdevImplementUnits.getImplement_unit_status_normal()) || 1 == fdevImplementUnits.getImplement_unit_status_normal())) {
                                    //如果实施单元的补充标识为null或者为0，且还在评估中，说明该板块是新增
                                    queryPartInfo.put("supplyWords", "NoCompleteAssess");
                                } else {
                                    queryPartInfo.put("addWords", "CompleteAssess");
                                }
                            }
                        }
                    } else if (relatePartDetail.getAssess_user().contains(user.getId())) {
                        if ("2".equals(relatePartDetail.getAssess_status())) {
                            //该板块已评估完成
                            queryPartInfo.put("addWords", "CompleteAssess");
                            break;
                        } else if ("0".equals(relatePartDetail.getAssess_status())) {
                            queryPartInfo.put("supplyWords", "NoCompleteAssess");
                            break;
                        } else if ("1".equals(relatePartDetail.getAssess_status())) {
                            FdevImplementUnit fdevImplementUnits = implementUnitDao.queryByDemandIdAndGroupMax(demandBaseInfo.getId(), relatePartDetail.getPart_id());
                            if (!CommonUtils.isNullOrEmpty(fdevImplementUnits)) {
                                if ((CommonUtils.isNullOrEmpty(fdevImplementUnits.getAdd_flag())
                                        || 0 == fdevImplementUnits.getAdd_flag()) && (CommonUtils.isNullOrEmpty(fdevImplementUnits.getImplement_unit_status_normal()) || 1 == fdevImplementUnits.getImplement_unit_status_normal())) {
                                    //如果实施单元的补充标识为null或者为0，且还在评估中，说明该板块是新增
                                    queryPartInfo.put("supplyWords", "NoCompleteAssess");
                                } else {
                                    queryPartInfo.put("addWords", "CompleteAssess");
                                }
                            }
                        }
                    } else {
                        queryPartInfo.put("addWords", "NoPermissionAdd");
                        queryPartInfo.put("supplyWords", "NoPermissionSupply");
                        break;
                    }
                }

            }
        }
        return queryPartInfo;
    }

    @Override
    public List<DemandBaseInfo> queryDemandByGroupIdAssessOver(Set<String> groupIds, int todayLast) {
        return demandBaseInfoDao.queryDemandByGroupIdAssessOver(groupIds, todayLast);
    }

    @Override
    public List<TechType> queryTechType(Map<String, Object> param) throws Exception {
        return demandBaseInfoDao.queryTechType(param);
    }

    @Override
    public void updateDemandCodeOrderNo(Map<String, Object> param) throws Exception {
        String no = (String) param.get(Dict.CODE_ORDER_NO);//代码审核工单编号
        List<String> demandIds = (List<String>) param.get(Dict.DEMAND_ID);//需求列表
        //查询原需求列表
        List<DemandBaseInfo> demandBaseInfos = demandBaseInfoDao.queryDemandByCodeOrderNo(no);
        if (!CommonUtils.isNullOrEmpty(demandBaseInfos)){
            for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
                Set<String> code_order_no = demandBaseInfo.getCode_order_no();
                //清空代码审核工单编号
                code_order_no.remove(no);
                demandBaseInfo.setCode_order_no(code_order_no);
                demandBaseInfoDao.update(demandBaseInfo);
            }
        }
        //查询需求
        if(!CommonUtils.isNullOrEmpty(demandIds)){
            for (String demandId : demandIds) {
                DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
                if(!CommonUtils.isNullOrEmpty(demandBaseInfo)){
                    Set<String> code_order_no = demandBaseInfo.getCode_order_no();
                    //更新代码审核工单编号
                    if( CommonUtils.isNullOrEmpty(code_order_no) ) code_order_no = new HashSet<>();
                    code_order_no.add(no);
                    demandBaseInfo.setCode_order_no(code_order_no);
                    demandBaseInfoDao.update(demandBaseInfo);
                }
            }
        }

    }

    @Override
    public List<DemandBaseInfo> getDemandsInfoByIds(Map<String, Object> param) {
        List<String> demandIds = (List<String>)param.get(Dict.DEMANDIDS);
        List<DemandBaseInfo> demandBaseInfos = demandBaseInfoDao.queryDemandByIds(new HashSet<>(demandIds));
        return demandBaseInfos;
    }

    @Override
    public List<String> queryDemandFile(String demandId) throws Exception {
        List<String> result = new ArrayList<>();
        DemandBaseInfo demand = demandBaseInfoDao.queryById(demandId);
        //如果需求下没有开发任务，不做处理
        List<Map> taskList = taskService.queryTaskByDemandId(demandId);
        if (!CommonUtils.isNullOrEmpty(taskList)
                && taskList.stream().anyMatch(task -> null == task.get(Dict.TASKTYPE))) {
            //查询需求下文档
            DemandDoc demandDoc = new DemandDoc();
            demandDoc.setDemand_id(demandId);
            List<DemandDoc> demandDocList = new ArrayList<>();
            try {
                demandDocList = demandDocDao.queryAll(demandDoc);
            } catch (Exception e) {
                logger.info(">>>queryDemandFile fail,{}", demandId);
                throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
            }
            if (!CommonUtils.isNullOrEmpty(demandDocList)) {
                //查询需求说明书
                if (!demandDocList.stream().anyMatch(doc -> DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getValue().equals(doc.getDoc_type()))) {
                    //如果需求关联文档中没有需求说明书，就从confluence上读取
                    getInstructionFromConf(result, demand);
                }
                //查询需求规格说明书
                if (!demandDocList.stream().anyMatch(doc -> DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_INSTRUCTION.getValue().equals(doc.getDoc_type()))) {
                    result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_INSTRUCTION.getName());
                }
                //如果需求相关文档中没有一个文档的类型是内测报告且报告名称包含需求名称，生成一个
                if (!demandDocList.stream().anyMatch(doc -> DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getValue().equals(doc.getDoc_type())
                        && doc.getDoc_name().contains(demand.getOa_contact_name()))) {
                    //查询此需求在玉衡上的测试数据，生成内测报告，保存到minio
                    createInnerTestReport(demand);
                }
                //如果是业务需求，还要查看需求评估表、需规确认材料、上线确认书和业测报告是否已有
                if (Dict.BUSINESS.equals(demand.getDemand_type())) {
                    //需求评估表
                    if (!demandDocList.stream().anyMatch(doc -> DemandDocEnum.DemandDocTypeEnum.DEMAND_ASSESS_REPORT.getValue().equals(doc.getDoc_type()))) {
                        result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_ASSESS_REPORT.getName());
                    }
                    //需求确认材料
                    if (!demandDocList.stream().anyMatch(doc -> DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_CONFIRM.getValue().equals(doc.getDoc_type()))) {
                        result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_CONFIRM.getName());
                    }
                    //上线确认书
                    if (!demandDocList.stream().anyMatch(doc -> DemandDocEnum.DemandDocTypeEnum.LAUNCH_CONFIRM.getValue().equals(doc.getDoc_type()))) {
                        result.add(DemandDocEnum.DemandDocTypeEnum.LAUNCH_CONFIRM.getName());
                    }
                    //业测报告
                    if (!demandDocList.stream().anyMatch(doc -> DemandDocEnum.DemandDocTypeEnum.BUSINESS_TEST_REPORT.getValue().equals(doc.getDoc_type()))) {
                        result.add(DemandDocEnum.DemandDocTypeEnum.BUSINESS_TEST_REPORT.getName());
                    }
                }
            } else {
                getInstructionFromConf(result, demand);
                //查询此需求在玉衡上的测试数据，生成内测报告，保存到minio
                createInnerTestReport(demand);
                result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_INSTRUCTION.getName());
                if (Dict.BUSINESS.equals(demand.getDemand_type())) {
                    result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_ASSESS_REPORT.getName());
                    result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_CONFIRM.getName());
                    result.add(DemandDocEnum.DemandDocTypeEnum.LAUNCH_CONFIRM.getName());
                    result.add(DemandDocEnum.DemandDocTypeEnum.BUSINESS_TEST_REPORT.getName());
                }
            }
        }
        return result;
    }

    /**
     * 从confulence导出需求说明书
     * @param result
     * @param demand
     * @throws Exception
     */
    private void getInstructionFromConf(List<String> result, DemandBaseInfo demand) throws Exception {
        List<ContentDto> totalPageData = confService.getConfluencePageInfo(groupPageId);
        if (!CommonUtils.isNullOrEmpty(totalPageData)) {
            boolean existFlag = false;
            for (ContentDto content : totalPageData) {
                if (content.getTitle().contains(demand.getOa_contact_no())) {
                    //需求在confluence中有需求说明书，读取这个说明书上传到minIO
                    byte[] file = null;
                    try {
                        file = confService.exportWord(content.getId());
                    } catch (Exception e) {
                        logger.info(">>>>exportWord fail,{}", content.getId());
                        result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getName());
                    }
                    InputStream inputStream = new ByteArrayInputStream(file);
                    MultipartFile multipartFile = new MockMultipartFile(content.getTitle() + ".docx", content.getTitle() + ".docx", ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
                    MultipartFile[] multipartFiles = {multipartFile};
                    User user = userVerifyUtil.getRedisUser();
                    docService.uploadFile(demand.getId(), DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getValue(), null, user.getGroup_id(), null, multipartFiles, null);
                    existFlag = true;
                }
            }
            if (!existFlag) {
                result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getName());
            }
        } else {
            result.add(DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getName());
        }
    }

    /**
     * 创建内测报告
     * @param demand
     */
    private void createInnerTestReport(DemandBaseInfo demand) {
        try {
            List<Map> workOrderList = commonBusinessService.queryInnerTestData(demand.getOa_contact_no());
            if (!CommonUtils.isNullOrEmpty(workOrderList)) {
                XSSFWorkbook workbook = new XSSFWorkbook();
                String fileName = "【内测报告】"+ demand.getOa_contact_name() + ".xlsx";//文件名
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                for (int index = 0; index < workOrderList.size(); index++) {
                    Map<String, Object> workOrder = workOrderList.get(index);
                    MyUtils.createInnerTestReport(workOrder, workbook, index);
                }
                workbook.write(bs);
                byte[] file = bs.toByteArray();
                InputStream inputStream = new ByteArrayInputStream(file);
                MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
                MultipartFile[] multipartFiles = {multipartFile};
                User user = userVerifyUtil.getRedisUser();
                docService.uploadFile(demand.getId(), DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getValue(), null, user.getGroup_id(), null, multipartFiles, null);
            }
        } catch (Exception e) {
            logger.info(">>>queryDemandFile createInnerTestReport fail,{}", demand.getOa_contact_no());
        }
    }
}
