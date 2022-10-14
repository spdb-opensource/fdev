package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.*;
import com.spdb.fdev.fdemand.base.dict.DemandEnum.DemandAssess_status;
import com.spdb.fdev.fdemand.base.dict.DemandEnum.DemandDeferStatus;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DemandBaseInfoUtil;
import com.spdb.fdev.fdemand.base.utils.GroupUtil;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.dao.IImplementUnitDao;
import com.spdb.fdev.fdemand.spdb.dao.IOtherDemandTaskDao;
import com.spdb.fdev.fdemand.spdb.dao.impl.IpmpUnitDaoImpl;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.fdemand.spdb.unit.DealFdevTaskUnit;
import com.spdb.fdev.fdemand.spdb.unit.DealTaskUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImplementUnitServiceImpl implements IImplementUnitService {

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private ICommonBusinessService commonBusinessService;

    @Autowired
    private DealTaskUnit dealTaskUnit;

    @Autowired
    private DealFdevTaskUnit dealFdevTaskUnit;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IpmpTaskService ipmpTaskService;

    @Autowired
    private IDemandBaseInfoService demandBaseInfoService;

    private static Logger logger = LoggerFactory.getLogger(ImplementUnitServiceImpl.class);

    @Autowired
    private IRoleService iRoleService;

    @Autowired
    private IpmpUnitDaoImpl ipmpUnitDao;

    @Autowired
    private IIpmpUnitService ipmpUnitService;

    @Autowired
    private IOtherDemandTaskDao otherDemandTaskDao;

    @Autowired
    private IOtherDemandTaskService otherDemandTaskService;

    @Autowired
    private GroupUtil groupUtil;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    private SendEmailDemandService sendEmailDemandService;

    @Autowired
    private IFdevUnitApproveService fdevUnitApproveService;

    @Override
    public Map queryPaginationByDemandId(Map<String, Object> param) throws Exception {
        Integer pageSize = (Integer) param.get(Dict.SIZE);//页面大小
        Integer currentPage = (Integer) param.get(Dict.INDEX);//当前页
        if (CommonUtils.isNullOrEmpty(pageSize)) {
            pageSize = 0;
        }
        if (CommonUtils.isNullOrEmpty(currentPage)) {
            currentPage = 1;
        }
        Integer start = pageSize * (currentPage - 1);   //起始
        List<FdevImplementUnit> data = implementUnitDao.queryPaginationByDemandId(start, pageSize, param.get(Dict.DEMAND_ID).toString());
        Long count = implementUnitDao.queryCountByDemandId(param.get(Dict.DEMAND_ID).toString());
        completeFdevImplUnit(data);//补全团队中文名、创建人中文名等信息
        User user = CommonUtils.getSessionUser();
        String demand_id = param.get(Dict.DEMAND_ID).toString();
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
        try {
			data = addDelFlag(data,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            data = addUpdateFlag(data,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            data = addMountFlag(data,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            data = addApproveFlag(data,user,demand_id,isDemandManager,isDemandLeader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Map result = new HashMap();
        result.put(Dict.DATA, data);
        result.put(Dict.COUNT, count);
        return result;
    }

    @Override
    public FdevImplementUnit add(FdevImplementUnit fdevImplementUnit) throws Exception {
        String demand_id = fdevImplementUnit.getDemand_id();
        String ipmp_unit_no = fdevImplementUnit.getIpmp_implement_unit_no();
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demand_id);
        //如果isTransferRqrmnt为1，说明该需求为老需求，则可以在任何状态修改，
        // 如果为2，则说明该需求已经被处理过了，如果为null，说明是新建的需求
        if(!"1".equals(demandBaseInfo.getIsTransferRqrmnt())){
            Integer demand_status_special = demandBaseInfo.getDemand_status_special();
            Integer demand_status_normal = demandBaseInfo.getDemand_status_normal();
            if (!CommonUtils.isNullOrEmpty(demand_status_special) && !DemandDeferStatus.FINISTH.getValue().equals(demand_status_special)) {
                //需求处在特殊状态时，不允许新增
                throw new FdevException(ErrorConstants.DEMAND_SPECIAL_STATUS_ERROR);
            }
        }
        if(Dict.TECH.equals(demandBaseInfo.getDemand_type()) || Dict.BUSINESS.equals(demandBaseInfo.getDemand_type())){
            //科技业务需求 计划内测 计划业测必填
            if(CommonUtils.isNullOrEmpty(fdevImplementUnit.getPlan_inner_test_date())
                    || CommonUtils.isNullOrEmpty(fdevImplementUnit.getPlan_test_date())){
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
            }
            //已投产、已撤销、暂缓、暂存状态的实施单元不允许被挂载
            if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)){
                //查询关联的实施单元信息
                IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
                String ipmpStatusName = ipmpUnit.getImplStatusName();

                //业务需求下实施单元牵头人在fdev找不到的，不允许挂载
                if("business".equals(demandBaseInfo.getDemand_type())){
                    if("已投产".equals(ipmpStatusName) || "暂缓".equals(ipmpStatusName) || "已撤销".equals(ipmpStatusName) || "暂存".equals(ipmpStatusName)){
                        throw new FdevException(ErrorConstants.IMPLEMENTUNIT_SPECIAL_STATUS_ERROR);
                    }
                    if("3".equals(ipmpUnit.getLeaderFlag())){
                        throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR3);
                    }
                    //如果实施单元的预计工作量已经同步到ipmp，则需要校验实施单元预计工作量>=各研发单元工作量之和
                    /*if("1".equals(ipmpUnit.getSyncFlag())){
                        this.checkIpmpExceptWork(ipmpUnit, fdevImplementUnit);
                    }*/
                }
            }
            //判断预期工作量是否为两位小数
            this.checkWork(fdevImplementUnit.getDept_workload(),fdevImplementUnit.getCompany_workload());
           /* FdevImplementUnit fdevImplementUnitBefore = implementUnitDao.queryByDemandIdAndGroupOne(demand_id, fdevImplementUnit.getGroup());
            if (!CommonUtils.isNullOrEmpty(fdevImplementUnitBefore)) {
                int iusnBefore = fdevImplementUnitBefore.getImplement_unit_status_normal();
                if (iusnBefore >= ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()) {
                    //说明已经评估完成 需求管理员、需求牵头人、板块牵头人
                    throw new FdevException(ErrorConstants.IMPLEMENTUNIT_CANNOT_CREATE_ERROR);
                }
            }*/
        }
        this.planDateCheck(fdevImplementUnit);//校验计划时间是否符合规则
        FdevImplementUnit fdevImplementUnit1 = addAndSupplyCommon(fdevImplementUnit);//生成编号、初始化状态等信息
        //根据板块和需求id查询本板块
        fdevImplementUnit1.setAdd_flag(0);
        User user = CommonUtils.getSessionUser();
        String todayDate = TimeUtil.getDateStamp(new Date());
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
        boolean isPartAssesser = roleService.isPartAsesser(demand_id, fdevImplementUnit1.getGroup(), user);
        boolean isIpmpUnitLeader = false ;
        //是否是日常需求
        if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
            OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit1.getOther_demand_task_num());
            isIpmpUnitLeader = roleService.isOtherDemandTaskLeader( otherDemandTask, user.getId());
        }else{
            isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
        }


        /**
         * 1、需求管理员/需求牵头人/设计小组评估人/ 实施单元牵头人|其他需求任务负责人
         */
        if (isDemandManager || isDemandLeader || isPartAssesser || isIpmpUnitLeader) {
            //新增研发单元
            FdevImplementUnit fdevImplementUnitSave = implementUnitDao.save(fdevImplementUnit1);
            DemandBaseInfo demandBaseInfoNew = demandBaseInfoDao.queryById(demand_id);
            String group = fdevImplementUnit.getGroup();
            List<RelatePartDetail> relatePartDetailList = demandBaseInfoNew.getRelate_part_detail();
            if(!CommonUtils.isNullOrEmpty(relatePartDetailList)){
                for (RelatePartDetail relatePartDetail: relatePartDetailList) {
                    //修改涉及小组状态
                    if (group.equalsIgnoreCase(relatePartDetail.getPart_id())){
                        relatePartDetail.setAssess_status(DemandAssess_status.EVALUATE.getValue());
                    }
                    //如果是需求第一条研发单元，修改需求状态、需求评估时间和可发送邮件标识
                    if(DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue().equals(demandBaseInfoNew.getDemand_status_normal())){
//                        demandBaseInfoNew.setDemand_status_normal(DemandEnum.DemandStatusEnum.EVALUATE.getValue());
                        //更新实施单元和需求的状态、实际时间
                        //科技需求研发单元不用推实施单元状态
                        if("business".equals(demandBaseInfo.getDemand_type())){
                            if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)){
                                IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
                                ipmpUnitService.updateIpmpUnitStatus(ipmpUnit,user.getUser_name_en());
                            }
                        }else if( Dict.DAILY.equals(demandBaseInfo.getDemand_type()) ){
                            //日常需求推 其他需求任务
                            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit1.getOther_demand_task_num())){
                                OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit1.getOther_demand_task_num());
                                otherDemandTaskService.updateStatus(otherDemandTask);
                            }
                        }

                        demandBaseInfoNew.setDemand_assess_date(todayDate);
                        if (CommonUtils.isNullOrEmpty(demandBaseInfoNew.getAssess_flag())){
                            demandBaseInfoNew.setAssess_flag("yes");
                        }
                    }
                }
            }
            demandBaseInfoDao.update(demandBaseInfoNew);
            ipmpUnitService.updateDemandStatus(demandBaseInfoNew);
            return fdevImplementUnitSave;
        } else {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }
    }

    @Override
    public void update(FdevImplementUnit fdevImplementUnit) throws Exception {
        this.planDateCheck(fdevImplementUnit);//校验计划时间是否符合规则
   		//判断研发单元有没有关联实施单元，若关联的实施单元状态为已投产、暂缓、暂存、撤销，则不允许修改，报“所属实施单元状态为已投产、暂缓、暂存、撤销，不可修改”
        String ipmp_unit_no = fdevImplementUnit.getIpmp_implement_unit_no();
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(fdevImplementUnit.getDemand_id());

   		if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)){
            //查询关联的实施单元信息
            IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
            if( CommonUtils.isNullOrEmpty(ipmpUnit) ){
                throw new FdevException(ErrorConstants.IPMP_ERROR_NOT_NULL);
            }
            String ipmpStatusName = ipmpUnit.getImplStatusName();

            //业务需求下实施单元牵头人在fdev找不到的，不允许挂载
            if("business".equals(demandBaseInfo.getDemand_type())){
                if("已投产".equals(ipmpStatusName) || "暂缓".equals(ipmpStatusName) || "已撤销".equals(ipmpStatusName) || "暂存".equals(ipmpStatusName)){
                    throw new FdevException(ErrorConstants.IMPLEMENTUNIT_SPECIAL_STATUS_CANNOT_UPDATE);
                }
                if("3".equals(ipmpUnit.getLeaderFlag())){
                    throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR3);
                }
                //如果实施单元的预计工作量已经同步到ipmp，则需要校验实施单元预计工作量>=各研发单元工作量之和
                /*if("1".equals(ipmpUnit.getSyncFlag())){
                    this.checkIpmpExceptWork(ipmpUnit, fdevImplementUnit);
                }*/
            }
        }
        //判断预期工作量是否为两位小数
        this.checkWork(fdevImplementUnit.getDept_workload(),fdevImplementUnit.getCompany_workload());

        String demand_id = fdevImplementUnit.getDemand_id();
        FdevImplementUnit fdevImplementUnitBefore = implementUnitDao.queryById(fdevImplementUnit.getId());
        if (CommonUtils.isNullOrEmpty(fdevImplementUnitBefore)) {
            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_NULL_ERROR);
        }
        Integer iussBefore = fdevImplementUnitBefore.getImplement_unit_status_special();//修改前的特殊状态
        Integer iusnBefore = fdevImplementUnitBefore.getImplement_unit_status_normal();//修改前的正常状态
        if (!CommonUtils.isNullOrEmpty(iussBefore)) {
            if (ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue() == iussBefore) {
                //研发单元处于暂缓状态时，不允许更新
                throw new FdevException(ErrorConstants.IMPLEMENTUNIT_CANNOT_UPDATE_ERROR);
            } else if (ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue() == iussBefore) {
                fdevImplementUnit.setImplement_unit_status_special(ImplementUnitEnum.ImplementUnitDeferStatus.FINISTH.getValue());
            }
        }/* else {
            if (!CommonUtils.isNullOrEmpty(iusnBefore)
                    && iusnBefore >= ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()) {
                //研发单元已经评估完成，不允许修改
                throw new FdevException(ErrorConstants.IMPLEMENTUNIT_CANNOT_UPDATE_ERROR2);
            }
        }*/

        User user = CommonUtils.getSessionUser();
        List<UserInfo> implement_leader_all = fdevImplementUnit.getImplement_leader_all();
        HashSet<String> implement_leader = new HashSet<String>();
        for (UserInfo userInfo : implement_leader_all) {
            implement_leader.add(userInfo.getId());
        }
        fdevImplementUnit.setImplement_leader(implement_leader);
        if (fdevImplementUnit.isUi_verify()) {
            fdevImplementUnit.setUi_verify(true);
        } else {
            fdevImplementUnit.setUi_verify(false);
        }

        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
        boolean isAssesser = roleService.isPartAsesser(demand_id, fdevImplementUnit.getGroup(), user);
        boolean isIpmpUnitLeader = false ;
        //是否是日常需求
        if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
            OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit.getOther_demand_task_num());
            isIpmpUnitLeader = roleService.isOtherDemandTaskLeader( otherDemandTask, user.getId());
        }else{
            isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
        }
        boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnit.getId(),user.getId());

        /**
         * 1、需求管理员/需求牵头人/涉及小组评估人/所属实施单元牵头人/研发单元牵头人
         */
        if (isDemandManager || isDemandLeader || isAssesser || isIpmpUnitLeader || isFdevUnitLeader) {
            if(CommonUtils.isNullOrEmpty(fdevImplementUnit.getIpmp_implement_unit_no())) fdevImplementUnit.setIpmp_implement_unit_no("");
            //老业务需求 状态不为评估中 计划时间有修改 更新工单
            if( !CommonUtils.isNullOrEmpty(fdevImplementUnitBefore.getImplement_unit_status_normal())
                    && fdevImplementUnitBefore.getImplement_unit_status_normal() >= ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()
                        && CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())
                        && Constants.BUSINESS.equals(demandBaseInfo.getDemand_type()) &&
                            (!fdevImplementUnit.getPlan_inner_test_date().equals(fdevImplementUnitBefore.getPlan_inner_test_date())
                                || !fdevImplementUnit.getPlan_test_date().equals(fdevImplementUnitBefore.getPlan_test_date())
                                    || !fdevImplementUnit.getPlan_product_date().equals(fdevImplementUnitBefore.getPlan_product_date()))){
                //改动
                commonBusinessService.updateYuhengPlanDate(fdevImplementUnitBefore.getFdev_implement_unit_no(),
                        fdevImplementUnit.getPlan_inner_test_date(), fdevImplementUnit.getPlan_test_date(),
                        fdevImplementUnit.getPlan_product_date());
            }
            //更新研发单元内容
            implementUnitDao.update(fdevImplementUnit);
            DemandBaseInfo demandBaseInfoNew = demandBaseInfoDao.queryById(demand_id);
            //更新需求计划时间，计划启动日期：取最小。其他计划日期：取最晚。
            HashMap<String, String> dateByDemandIdMap = commonBusinessService.dateByDemandIdMapAssessOver(demand_id);
            demandBaseInfoNew.setPlan_start_date(dateByDemandIdMap.get(Dict.PLAN_START_DATE));
            demandBaseInfoNew.setPlan_inner_test_date(dateByDemandIdMap.get(Dict.PLAN_INNER_TEST_DATE));
            demandBaseInfoNew.setPlan_test_date(dateByDemandIdMap.get(Dict.PLAN_TEST_DATE));
            demandBaseInfoNew.setPlan_test_finish_date(dateByDemandIdMap.get(Dict.PLAN_TEST_FINISH_DATE));
            demandBaseInfoNew.setPlan_product_date(dateByDemandIdMap.get(Dict.PLAN_PRODUCT_DATE));

            demandBaseInfoDao.update(demandBaseInfoNew);
        } else {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }
    }


    @Override
    public FdevImplementUnit supply(FdevImplementUnit fdevImplementUnit) throws Exception {
        String demand_id = fdevImplementUnit.getDemand_id();
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demand_id);
        //如果isTransferRqrmnt为1，说明该需求为老需求，则可以在任何状态修改，
        // 如果为2，则说明该需求已经被处理过了，如果为null，说明是新建的需求
        if(!"1".equals(demandBaseInfo.getIsTransferRqrmnt())){
            Integer demand_status_special = demandBaseInfo.getDemand_status_special();
            if (!CommonUtils.isNullOrEmpty(demand_status_special) && !DemandDeferStatus.FINISTH.getValue().equals(demand_status_special)) {
                //需求处在特殊状态时(1：暂缓中、2：恢复中)，不允许补充
                //3：恢复完成
                throw new FdevException(ErrorConstants.DEMAND_SPECIAL_STATUS_ERROR);
            }
        }

        /*
         * 可以补充研发单元的板块：
         * 1、评估完成的板块
         * 2、板块在评估中，addflag最大的研发单元addfalg>=1且也在评估中
         * */
        String grp = fdevImplementUnit.getGroup();
        List<RelatePartDetail> relate_part = demandBaseInfo.getRelate_part_detail();
        List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryByDemandId(demand_id);
        //获取小组下所有研发单元
        List<FdevImplementUnit> implUnits = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(fdevImplementUnits)){
            for (FdevImplementUnit fdevImplUnit: fdevImplementUnits) {
                if (grp.equalsIgnoreCase(fdevImplUnit.getGroup())){
                    implUnits.add(fdevImplUnit);
                }
            }
        }
        if (!CommonUtils.isNullOrEmpty(implUnits)){
            //存量研发单元ddflag为空， 都改成0
            for (FdevImplementUnit uni:implUnits) {
                if(CommonUtils.isNullOrEmpty(uni.getAdd_flag())){
                    uni.setAdd_flag(0);
                }
            }
            
            //研发单元中addflag最大的研发单元addflag>0且在评估中
            boolean b = implUnits.stream()
                    .sorted(Comparator.comparingInt(FdevImplementUnit::getAdd_flag).reversed()).limit(1).allMatch((fdevImplementUnitmatch) -> {
                        return fdevImplementUnitmatch.getImplement_unit_status_normal().equals(1) && fdevImplementUnitmatch.getAdd_flag() > 0;
                    });
            if (null == relate_part) relate_part = new ArrayList<>();
            for (RelatePartDetail relatePart: relate_part) {
                if (grp.equalsIgnoreCase(relatePart.getPart_id())){
                    if (!(relatePart.getAssess_status().equalsIgnoreCase(DemandAssess_status.EVALUATE_OVER.getValue()) ||
                            (relatePart.getAssess_status().equalsIgnoreCase(DemandAssess_status.EVALUATE.getValue()) && b))){
                        List<FdevImplementUnit> fdevImplementUnitGroupList = implementUnitDao.queryByDemandIdAndGroup(demand_id, grp);
                        boolean flag = true;//默认报错
                        List<Integer> statusList = new ArrayList<>();
                        for (FdevImplementUnit fdevImplementUnitGroup : fdevImplementUnitGroupList) {
                            //状态为评估中 且不为暂缓或恢复中 不需要审批的
                            if( CommonUtils.isNullOrEmpty(fdevImplementUnitGroup.getImplement_unit_status_special()) ||
                                    ImplementUnitEnum.ImplementUnitDeferStatus.FINISTH.getValue()
                                            == fdevImplementUnitGroup.getImplement_unit_status_special()) {
                                statusList.add(fdevImplementUnitGroup.getImplement_unit_status_normal());
                            }
                        }
                        //包含评估中 与评估中以上的 不报错
                        if(statusList.contains(1)){
                            for (Integer status : statusList) {
                                if( status > 1 ){
                                    flag = false;
                                    break;
                                }
                            }
                        }
                        if(flag)throw new FdevException(ErrorConstants.IMPLEMENTUNIT_CANNOT_SUPPLY_ERROR);
                    }
                }
            }
            if(Dict.TECH.equals(demandBaseInfo.getDemand_type()) || Dict.BUSINESS.equals(demandBaseInfo.getDemand_type())) {
                //科技业务需求 计划内测 计划业测必填
                if (CommonUtils.isNullOrEmpty(fdevImplementUnit.getPlan_inner_test_date())
                        || CommonUtils.isNullOrEmpty(fdevImplementUnit.getPlan_test_date())) {
                    throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
                }
                //判断研发单元有没有关联实施单元，若关联的实施单元状态为已投产、暂缓、暂存、撤销，则不允许修改，报“所属实施单元状态为已投产、暂缓、暂存、撤销，不可修改”
                String ipmp_unit_no = fdevImplementUnit.getIpmp_implement_unit_no();
                if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)){
                    //查询关联的实施单元信息
                    IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
                    String ipmpStatusName = ipmpUnit.getImplStatusName();

                    //业务需求下实施单元牵头人在fdev找不到的，不允许挂载
                    if("business".equals(demandBaseInfo.getDemand_type())){
                        if("已投产".equals(ipmpStatusName) || "暂缓".equals(ipmpStatusName) || "已撤销".equals(ipmpStatusName) || "暂存".equals(ipmpStatusName)){
                            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_SPECIAL_STATUS_CANNOT_UPDATE);
                        }
                        if("3".equals(ipmpUnit.getLeaderFlag())){
                            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR3);
                        }
                        //如果实施单元的预计工作量已经同步到ipmp，则需要校验实施单元预计工作量>=各研发单元工作量之和
                        /*if("1".equals(ipmpUnit.getSyncFlag())){
                            this.checkIpmpExceptWork(ipmpUnit, fdevImplementUnit);
                        }*/
                    }
                }
            }
            this.planDateCheck(fdevImplementUnit);//校验计划时间是否符合规则

            //判断预期工作量是否为两位小数
            this.checkWork(fdevImplementUnit.getDept_workload(),fdevImplementUnit.getCompany_workload());
            //补充研发单元标识
            List<FdevImplementUnit> collect = implUnits.stream()
                    .sorted(Comparator.comparingInt(FdevImplementUnit::getAdd_flag))
                    .collect(Collectors.toList());
            //获取板块下所有研发单元最大补充次数
            Integer maxStage = collect.get(collect.size() - 1).getImplement_unit_status_normal();
            Integer max = collect.get(collect.size() - 1 ).getAdd_flag();
            //最大补充次数的研发单元状态为评估中，取当前评估补充次数，否则加1
            if ( maxStage == 1){
                fdevImplementUnit.setAdd_flag(max);
            }else{
                fdevImplementUnit.setAdd_flag(max + 1);
            }
        }else{
            for (RelatePartDetail relatePart : relate_part){
                if (grp.equalsIgnoreCase(relatePart.getPart_id())) {
                    if (relatePart.getAssess_status().equalsIgnoreCase(DemandAssess_status.EVALUATE_OVER.getValue())) {
                        fdevImplementUnit.setAdd_flag(1);
                    }else{
                        throw new FdevException(ErrorConstants.IMPLEMENTUNIT_CANNOT_SUPPLY_ERROR);
                }
                }
            }
        }


        FdevImplementUnit fdevImplementUnit1 = addAndSupplyCommon(fdevImplementUnit);//生成研发单元编号、初始化状态等

        User user = CommonUtils.getSessionUser();
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
        boolean isPartAssesser = roleService.isPartAsesser(demand_id, fdevImplementUnit1.getGroup(), user);
        boolean isIpmpUnitLeader = false ;
        //是否是日常需求
        if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
            OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit1.getOther_demand_task_num());
            isIpmpUnitLeader = roleService.isOtherDemandTaskLeader( otherDemandTask, user.getId());
        }else{
            isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
        }

        if (isDemandManager || isDemandLeader || isPartAssesser || isIpmpUnitLeader){
            //新增研发单元
            FdevImplementUnit fdevImplementUnitSave = implementUnitDao.save(fdevImplementUnit1);
            //修改涉及小组评估
            DemandBaseInfo demandBaseInfoNew = demandBaseInfoDao.queryById(demand_id);
            String group = fdevImplementUnit.getGroup();
            List<RelatePartDetail> relatePartDetailList = demandBaseInfoNew.getRelate_part_detail();
            if (null == relate_part) relate_part = new ArrayList<>();
            for (RelatePartDetail relatePartDetail: relatePartDetailList) {
                if (group.equalsIgnoreCase(relatePartDetail.getPart_id())){
                    relatePartDetail.setAssess_status(DemandAssess_status.EVALUATE.getValue());
                    break;
                }
            }
            //修改需求状态，改为评估中
//            demandBaseInfoNew.setDemand_status_normal(DemandEnum.DemandStatusEnum.EVALUATE.getValue());
            demandBaseInfoDao.update(demandBaseInfoNew);
            //更新实施单元和需求的状态、实际时间
            if("business".equals(demandBaseInfo.getDemand_type())){
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getIpmp_implement_unit_no())){
                    IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(fdevImplementUnit1.getIpmp_implement_unit_no());
                    ipmpUnitService.updateIpmpUnitStatus(ipmpUnit,user.getUser_name_en());
                }
            }else if( Dict.DAILY.equals(demandBaseInfo.getDemand_type()) ){
                //日常需求推 其他需求任务
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnit1.getOther_demand_task_num())){
                    OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit1.getOther_demand_task_num());
                    otherDemandTaskService.updateStatus(otherDemandTask);
                }
            }
            ipmpUnitService.updateDemandStatus(demandBaseInfoNew);
            return fdevImplementUnitSave;
        } else {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }
    }


    @Override
    public void deleteById(Map<String, Object> param) throws Exception {
    	List<String> ids = (List<String>) param.get(Dict.IDS);
        String demand_id = "";
        String group = "";
        Integer iusnBefore = null;//研发单元状态
        for (String id : ids) {
            FdevImplementUnit fdevImplementUnitBefore = implementUnitDao.queryById(id);
            if (CommonUtils.isNullOrEmpty(fdevImplementUnitBefore)) {
               continue;
            }
            if (!CommonUtils.isNullOrEmpty(fdevImplementUnitBefore.getGroup())) {
                group = fdevImplementUnitBefore.getGroup();
            }
            demand_id = fdevImplementUnitBefore.getDemand_id();
            String ipmp_unit_no = "";
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnitBefore.getIpmp_implement_unit_no())){
                ipmp_unit_no = fdevImplementUnitBefore.getIpmp_implement_unit_no();
            }
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demand_id);
            /**
             * 1、需求管理员/需求牵头人/所属小组评估人/实施单元牵头人|其他需求任务负责人/研发单元牵头人
             */
            User user = CommonUtils.getSessionUser();
            boolean isDemandManager = roleService.isDemandManager();
            boolean isDemandLeader = roleService.isDemandLeader(fdevImplementUnitBefore.getDemand_id(), user.getId());
            boolean isAssesser = roleService.isPartAsesser(fdevImplementUnitBefore.getDemand_id(), fdevImplementUnitBefore.getGroup(), user);
            boolean isIpmpUnitLeader = false ;
            //是否是日常需求
            if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
                OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnitBefore.getOther_demand_task_num());
                isIpmpUnitLeader = roleService.isOtherDemandTaskLeader( otherDemandTask, user.getId());
            }else{
                isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
            }
            boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnitBefore.getId(),user.getId());

            IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
            //研发单元处于评估中，则可以直接删除
            //正常状态
            iusnBefore = fdevImplementUnitBefore.getImplement_unit_status_normal();
            if(iusnBefore >= DemandEnum.DemandStatusEnum.DEVELOP.getValue()){
                //判断研发单元有没有关联实施单元，若关联的实施单元状态为已投产、暂缓、暂存、撤销，则不允许删除，报“所属实施单元状态为已投产、暂缓、暂存、撤销，不可删除”
                if(!CommonUtils.isNullOrEmpty(ipmpUnit)){
                    //查询关联的实施单元信息
                    String ipmpStatusName = ipmpUnit.getImplStatusName();
                    if("已投产".equals(ipmpStatusName) || "暂缓".equals(ipmpStatusName) || "已撤销".equals(ipmpStatusName) || "暂存".equals(ipmpStatusName)){
                        throw new FdevException(ErrorConstants.IMPLEMENTUNIT_SPECIAL_STATUS_CANNOT_UPDATE);
                    }
                }
                //评估完成的研发单元，有任务不可删除
                Integer taskNum = taskService.queryNotDiscarddnum(fdevImplementUnitBefore.getFdev_implement_unit_no());
                if(taskNum > 0){
                    throw new FdevException(ErrorConstants.IMPLEMENTUNIT_HAVE_TASK_CANNOT_DELETE);
                }
                //评估中的研发单元，只允许需求牵头人、需求管理员、实施单元牵头人可删除
                if(!isDemandManager && !isDemandLeader && !isIpmpUnitLeader){
                    throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
                }
            }

            if (isDemandManager || isDemandLeader || isAssesser || isIpmpUnitLeader || isFdevUnitLeader) {
                //删除
                implementUnitDao.deleteById(id);
                //此处逻辑保留，仅适用于老数据或科技需求删除玉衡工单
                if(CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag()) || !"business".equals(demandBaseInfo.getDemand_type()) ){
                    if(iusnBefore == ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()) {
                        //研发单元为待实施时同步删除玉衡工单
                        commonBusinessService.deleteImpl(fdevImplementUnitBefore.getFdev_implement_unit_no());
                    }
                }
                //更新实施单元的状态、实际时间
                if("business".equals(fdevImplementUnitBefore.getDemand_type())){
                    if(!CommonUtils.isNullOrEmpty(ipmpUnit)){
                        ipmpUnitService.updateIpmpUnitStatus(ipmpUnit,user.getUser_name_en());
                        //删除研发单元为待实施时同步更新实施单元工作量
                        if( !"1".equals(ipmpUnit.getSyncFlag()) && !CommonUtils.isNullOrEmpty(iusnBefore) && iusnBefore>= DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue()) {
                            Set ipmpUnits = new HashSet();
                            ipmpUnits.add(ipmpUnit);
                            this.updateIpmpExceptWork(ipmpUnits);
                        }
                    }
                }else if( Dict.DAILY.equals(demandBaseInfo.getDemand_type()) ){
                    Set<String> taskNums = new HashSet<>();
                    taskNums.add(fdevImplementUnitBefore.getOther_demand_task_num());
                    //日常需求累加工作量
                    otherDemandTaskService.updateOtherDemandTaskWorkload(taskNums);
                    //日常需求推 其他需求任务
                    if(!CommonUtils.isNullOrEmpty(fdevImplementUnitBefore.getOther_demand_task_num())){
                        OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnitBefore.getOther_demand_task_num());
                        otherDemandTaskService.updateStatus(otherDemandTask);
                    }
                }
            } else {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
            }
        }
        if (!CommonUtils.isNullOrEmpty(demand_id)) {
            DemandBaseInfo demandBaseInfoNew = demandBaseInfoDao.queryById(demand_id);
            //删除的研发单元所属小组已经评估完成，需要重新推需求的计划时间
            HashMap<String, String> dateByDemandIdMap = commonBusinessService.dateByDemandIdMapAssessOver(demand_id);
            demandBaseInfoNew.setPlan_start_date(dateByDemandIdMap.get(Dict.PLAN_START_DATE));
            demandBaseInfoNew.setPlan_inner_test_date(dateByDemandIdMap.get(Dict.PLAN_INNER_TEST_DATE));
            demandBaseInfoNew.setPlan_test_date(dateByDemandIdMap.get(Dict.PLAN_TEST_DATE));
            demandBaseInfoNew.setPlan_test_finish_date(dateByDemandIdMap.get(Dict.PLAN_TEST_FINISH_DATE));
            demandBaseInfoNew.setPlan_product_date(dateByDemandIdMap.get(Dict.PLAN_PRODUCT_DATE));
            //删除研发单元为待实施时同步更新需求工作量
            if(!CommonUtils.isNullOrEmpty(iusnBefore) && iusnBefore>= DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue()) {
            	demandBaseInfoNew = demandBaseInfoService.calcWorkload(demandBaseInfoNew);
            }
            //更新需求涉及小组的评估状态
            this.updateDemandAssessStatus(demandBaseInfoNew);
            demandBaseInfoNew = demandBaseInfoDao.update(demandBaseInfoNew);
            //更新需求状态
            ipmpUnitService.updateDemandStatus(demandBaseInfoNew);
        }
      
    }

    @Override
    public List<FdevImplementUnit> queryByDemandId(String demandId) {
        return implementUnitDao.queryByDemandId(demandId);
    }

    public Boolean isDevApprove(FdevImplementUnit fdevUnit) throws Exception {

        //实施单元编号为 空  并为业务需求
        if( CommonUtils.isNullOrEmpty(fdevUnit.getIpmp_implement_unit_no())
                && Constants.BUSINESS.equals(fdevUnit.getDemand_type())){
            return true ;
        }

        return false ;
    }

    public Boolean isOverdue(FdevImplementUnit fdevUnit) throws Exception {
        Map<String, Object> map = new HashMap();
        map.put(Dict.DEMANDID,fdevUnit.getDemand_id());
        Map<String, String> demandAssessDate = fdevUnitApproveService.queryDemandAssessDate(map);
        String assessDate = demandAssessDate.get(Dict.ASSESSDATE);//评估完成时间
        String startOverdueDate = demandAssessDate.get(Dict.STARTOVERDUEDATE);//判断计划开始日期超期时间
        String testOverdueDate = demandAssessDate.get(Dict.TESTOVERDUEDATE);//判断计划业测日期超期时间
        //评估完成时间为空 不校验
        if( CommonUtils.isNullOrEmpty(assessDate) ){
            return false ;
        }
        //判断两个时间计划开始 计划业测时间相差几天
        int start = CommonUtils.getJudgementDate(assessDate,fdevUnit.getPlan_start_date())
                ? 0 : TimeUtil.subtractTime(fdevUnit.getPlan_start_date(),assessDate);
        int test = CommonUtils.getJudgementDate(assessDate,fdevUnit.getPlan_test_date())
                ? 0 : TimeUtil.subtractTime(fdevUnit.getPlan_test_date(),assessDate);
        //判断是否超期 并且未填上超期原因
        if( (start >= Integer.parseInt(startOverdueDate) || test >= Integer.parseInt(testOverdueDate))
            && CommonUtils.isNullOrEmpty(fdevUnit.getApproveState()) ){
            return true ;
        }
        return false ;
    }
    /**
     * 研发单元状态取最晚，所有板块都完成评估了，才能进入待实施及之后的状态
     *
     * @param param
     * @throws Exception
     */
    @Override
    public void assess(Map<String, Object> param) throws Exception {

        List<String> listId = (List<String>) param.get(Dict.ID);
        List<FdevImplementUnit> listFdevImplementUnitUpdate = new ArrayList<FdevImplementUnit>();
        List<FdevImplementUnit> errorFdevUnitList = new ArrayList<>();

        String demandId = null;
        String implpart = null;
        Set<String> groupIdList = new HashSet<String>();
        for (String id : listId) {
            FdevImplementUnit fdevImplementUnitBefore = implementUnitDao.queryById(id);
            if (CommonUtils.isNullOrEmpty(fdevImplementUnitBefore)) {
                continue;
            }

            if(CommonUtils.isNullOrEmpty(fdevImplementUnitBefore.getAdd_flag())){
                fdevImplementUnitBefore.setAdd_flag(0);
            }
            implpart = fdevImplementUnitBefore.getGroup();
            groupIdList.add(implpart);
            demandId = fdevImplementUnitBefore.getDemand_id();
            Integer impSpecial = fdevImplementUnitBefore.getImplement_unit_status_special();
            //特殊状态不允许评估完成
            if (!CommonUtils.isNullOrEmpty(impSpecial)) {
                throw new FdevException(ErrorConstants.IMPL_DEFER_ERROR);
            }
            fdevImplementUnitBefore.setImplement_unit_status_normal(ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue());
            //判断是否需要审批 需要则记录后续抛错 超期 或实施单元为空
            if( !Constants.YES.equals(param.get(Dict.ROLE)) &&
                    (isOverdue( fdevImplementUnitBefore ) || isDevApprove(fdevImplementUnitBefore)) ){
                errorFdevUnitList.add( fdevImplementUnitBefore );
                continue;
            }
            listFdevImplementUnitUpdate.add(fdevImplementUnitBefore);
        }
        if(!CommonUtils.isNullOrEmpty(listFdevImplementUnitUpdate)){
            User user = CommonUtils.getSessionUser();
            //条线经理审批 不需要校验权限
            if(!Constants.YES.equals(param.get(Dict.ROLE))){
                boolean isDemandManager = roleService.isDemandManager();
                boolean isDemandLeader = roleService.isDemandLeader(demandId, user.getId());
                boolean isAssesser = roleService.isPartAsesser(demandId, implpart, user);

                int addFlag = listFdevImplementUnitUpdate.get(0).getAdd_flag();
                if(addFlag == 0){
                    if (!isDemandManager && !isDemandLeader && !isAssesser) {
                        throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
                    }
                }else if(addFlag > 0){
                    if (!isDemandLeader && !isDemandManager) {
                        throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
                    }
                }
            }

            Set<String> ipmpUnitNums = new HashSet<>();//评估完成的研发单元所属的实施单元的编号
            Set<IpmpUnit> ipmpUnits = new HashSet<>();//评估完成的研发单元所属的实施单元
            //业务需求判断评估的研发单元是否超出实施单元预期工作量
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
            if("business".equals(demandBaseInfo.getDemand_type())){
                for(FdevImplementUnit fdevImplementUnit : listFdevImplementUnitUpdate){
                    String ipmp_implement_unit_no = fdevImplementUnit.getIpmp_implement_unit_no();
                    if(!CommonUtils.isNullOrEmpty(ipmp_implement_unit_no)){
                        if(!ipmpUnitNums.contains(ipmp_implement_unit_no)){
                            ipmpUnitNums.add(ipmp_implement_unit_no);
                            IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_implement_unit_no);
                            if(!CommonUtils.isNullOrEmpty(ipmpUnit)){
                                ipmpUnits.add(ipmpUnit);
                            }
                        }
                    }
                }
            /*//业务需求判断评估的研发单元是否超出实施单元预期工作量
            checkIpmpExceptWorks(ipmpUnits);*/
            }
            Set<String> taskNums = new HashSet<>();
            //更新研发单元状态
            for(FdevImplementUnit fdevImplementUnit : listFdevImplementUnitUpdate){
                implementUnitDao.update(fdevImplementUnit);
                //老需求和科技需求则生成玉衡工单
                if((CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag()) && !Dict.DAILY.equals(demandBaseInfo.getDemand_type()))
                        || Dict.TECH.equals(demandBaseInfo.getDemand_type())){
                    String groupId = fdevImplementUnit.getGroup();
                    String groupName = fdevImplementUnit.getGroup_cn();
                    commonBusinessService.createYuhengOrder(fdevImplementUnit.getFdev_implement_unit_no(),
                            fdevImplementUnit.getPlan_inner_test_date(), fdevImplementUnit.getPlan_test_date(),
                            fdevImplementUnit.getPlan_product_date(), "", groupId, groupName);
                }
                if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
                    taskNums.add(fdevImplementUnit.getOther_demand_task_num());
                }
            }
            //仅业务需求更新实施单元的工作量
            if("business".equals(demandBaseInfo.getDemand_type())){
                this.updateIpmpExceptWork(ipmpUnits);
            }else if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
                //日常需求累加工作量
                otherDemandTaskService.updateOtherDemandTaskWorkload(taskNums);
            }

            if (!CommonUtils.isNullOrEmpty(demandId)) {
                //获取所有研发单元的状态和时间，修改需求相关信息
                if (!CommonUtils.isNullOrEmpty(demandBaseInfo)) {
                    List<FdevImplementUnit> unitList = queryByDemandId(demandId);
                    List<FdevImplementUnit> collect = unitList.stream()
                            .sorted(Comparator.comparingInt(FdevImplementUnit::getImplement_unit_status_normal))
                            .collect(Collectors.toList());
                    //获取所有研发单元最小阶段，（不考虑待实施）
                    Integer minStage = collect.get(0).getImplement_unit_status_normal();
                    if(minStage >= ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()){
                        //需要考虑未新增研发单元板块
                        List<RelatePartDetail> relatePartDetailList1 = demandBaseInfo.getRelate_part_detail();
                        if(!CommonUtils.isNullOrEmpty(relatePartDetailList1)){
                            for(RelatePartDetail relatePartDetail : relatePartDetailList1){
                                if(relatePartDetail.getAssess_status().compareToIgnoreCase(DemandAssess_status.EVALUATE_OVER.getValue()) < 0){
                                    //说明还有个板块一定是在预评估的，该板块未新增研发单元
                                    minStage = ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue();
                                    break;
                                }
                            }
                        }
                    }
                    if(minStage >= DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue()){
                        //如果进入待实施后，则可发送邮件标识置为no
                        demandBaseInfo.setAssess_flag("no");
                    }
                    if("1".equals(demandBaseInfo.getIsTransferRqrmnt())){
                        List<RelatePartDetail> relatePartDetailListNew = demandBaseInfo.getRelate_part_detail();
                        if(!CommonUtils.isNullOrEmpty(relatePartDetailListNew)){
                            //只要有一个板块评估完成，就变成已处理数据
                            for(RelatePartDetail relatePartDetail : relatePartDetailListNew){
                                if(DemandAssess_status.PRE_EVALUATE.getValue().equals(relatePartDetail.getAssess_status())
                                        || DemandAssess_status.EVALUATE.getValue().equals(relatePartDetail.getAssess_status())){
                                    continue;
                                }
                                demandBaseInfo.setIsTransferRqrmnt("2");
                                break;
                            }
                        }
                    }
                    //更新需求计划日期
                    HashMap<String, String> dateByDemandIdMap = commonBusinessService.dateByDemandIdMapAssessOver(demandBaseInfo.getId());
                    demandBaseInfo.setPlan_start_date(dateByDemandIdMap.get(Dict.PLAN_START_DATE));
                    demandBaseInfo.setPlan_inner_test_date(dateByDemandIdMap.get(Dict.PLAN_INNER_TEST_DATE));
                    demandBaseInfo.setPlan_test_date(dateByDemandIdMap.get(Dict.PLAN_TEST_DATE));
                    demandBaseInfo.setPlan_test_finish_date(dateByDemandIdMap.get(Dict.PLAN_TEST_FINISH_DATE));
                    demandBaseInfo.setPlan_product_date(dateByDemandIdMap.get(Dict.PLAN_PRODUCT_DATE));

                    //计算评估完成的研发单元的工作量
                    demandBaseInfo = demandBaseInfoService.calcWorkload(demandBaseInfo);

                    //更新需求的涉及小组状态
                    this.updateDemandAssessStatus(demandBaseInfo);
                    demandBaseInfo = demandBaseInfoDao.update(demandBaseInfo);
                    //更新实施单元状态
                    if("business".equals(demandBaseInfo.getDemand_type())){
                        for(FdevImplementUnit fdevImplementUnit : listFdevImplementUnitUpdate){
                            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getIpmp_implement_unit_no())){
                                IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(fdevImplementUnit.getIpmp_implement_unit_no());
                                if(!CommonUtils.isNullOrEmpty(ipmpUnit)){
                                    ipmpUnitService.updateIpmpUnitStatus(ipmpUnit,user.getUser_name_en());
                                }
                            }
                        }
                    }else if( Dict.DAILY.equals(demandBaseInfo.getDemand_type()) ){
                        //日常需求推 其他需求任务
                        for(FdevImplementUnit fdevImplementUnit : listFdevImplementUnitUpdate){
                            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getOther_demand_task_num())){
                                OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit.getOther_demand_task_num());
                                otherDemandTaskService.updateStatus(otherDemandTask);
                            }
                        }

                    }
                    //更新需求状态
                    ipmpUnitService.updateDemandStatus(demandBaseInfo);
                }
            }
        }

        //存在需要审批的 研发单元 抛错
        if( !CommonUtils.isNullOrEmpty( errorFdevUnitList ) ){
            String fdevUnitNos = "";
            for (FdevImplementUnit fdevImplementUnit : errorFdevUnitList) {
                if(CommonUtils.isNullOrEmpty(fdevUnitNos)){
                    fdevUnitNos = fdevImplementUnit.getFdev_implement_unit_no();
                }else {
                    fdevUnitNos += "," + fdevImplementUnit.getFdev_implement_unit_no();
                }
            }
            throw new FdevException(ErrorConstants.FDEV_UNIT_ERROR, new String[]{ fdevUnitNos } );
        }
    }

    /**
     * 更新需求的涉及小组评估状态
     */
    @Override
    public void updateDemandAssessStatus(DemandBaseInfo demandBaseInfo){
        ArrayList<RelatePartDetail> relate_part_detail = demandBaseInfo.getRelate_part_detail();
        if (null == relate_part_detail) relate_part_detail = new ArrayList<>();
        for(RelatePartDetail relatePartDetail : relate_part_detail){
            //查询本需求下，该涉及小组的研发单元
            List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryByDemandIdAndGroup(demandBaseInfo.getId(), relatePartDetail.getPart_id());
            if(CommonUtils.isNullOrEmpty(fdevImplementUnits) || fdevImplementUnits.size() == 0){
                //涉及小组下没有研发单元，则涉及小组状态置为预评估
                relatePartDetail.setAssess_status(DemandAssess_status.PRE_EVALUATE.getValue());
            }else {
                //涉及小组下存在研发单元
                int numOfAssess = 0;//评估中的研发单元数量
                for(FdevImplementUnit fdevImplementUnit : fdevImplementUnits){
                    if(ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())){
                        numOfAssess++;
                    }
                }
                if(numOfAssess > 0){
                    //存在评估中的研发单元，则涉及小组状态为评估中
                    relatePartDetail.setAssess_status(DemandAssess_status.EVALUATE.getValue());
                }else {
                    relatePartDetail.setAssess_status(DemandAssess_status.EVALUATE_OVER.getValue());
                }
            }
        }
    }

    @Override
    public Map queryByFdevNo(Map<String, Object> param) {

        String fdevNo = param.get(Dict.FDEV_IMPLEMENT_UNIT_NO).toString();
        Map result = new HashMap();
        /** 1. 查询实施单元信息 */
        FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByFdevNo(fdevNo);
        if (CommonUtils.isNullOrEmpty(fdevImplementUnit)) {
            //实施单元不存在
            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_NULL_ERROR, new String[]{fdevNo});
        }
        result.put(Dict.IMPLEMENT_UNIT_INFO, fdevImplementUnit);

        /** 2. 查询对应的需求信息 */
        String demandId = fdevImplementUnit.getDemand_id();
        DemandBaseInfo demand = demandBaseInfoDao.queryById(demandId);
        result.put(Dict.DEMAND_BASEINFO, demand);
        return result;
    }

    /**
     * 修改实时单元状态后，需同步更新需求状态
     *
     * @param param
     */
    @Override
    public void updateIpmpUnitForTask(Map<String, Object> param) throws Exception {
        String unitNo = (String) param.get(Dict.FDEV_IMPLEMENT_UNIT_NO);
        String stage = (String) param.get(Dict.STAGE);
        String unitRealStartDate = (String) param.get(Dict.REAL_START_DATE);//实际启动时间，取最早
        String unitRealInnerTestDate = (String) param.get(Dict.REAL_INNER_TEST_DATE);//实际内测时间（SIT）取最晚
        String unitRealTestDate = (String) param.get(Dict.REAL_TEST_DATE);//实际业测时间（UAT）取最晚
        String unitRealTestFinishDate = (String) param.get(Dict.REAL_TEST_FINISH_DATE);//实际测试完成时间（REL）取最晚
        String unitRealProductDate = (String) param.get(Dict.REAL_PRODUCT_DATE);//时间投产时间 取最晚
        if(!CommonUtils.getJudgementDate(unitRealStartDate, unitRealInnerTestDate)) {
            throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }else if(!CommonUtils.getJudgementDate(unitRealInnerTestDate, unitRealTestDate)) {
        	throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }else if(!CommonUtils.getJudgementDate(unitRealTestDate, unitRealTestFinishDate)) {
        	throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }else if(!CommonUtils.getJudgementDate(unitRealTestFinishDate, unitRealProductDate)) {
        	throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }
        
        FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByUnitNo(unitNo);
        if (null == fdevImplementUnit) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前任务所属实施单元不存在"});
        }
        //如果实施单元已经处于归档状态，不再接收任务推送操作
        if (ImplementUnitEnum.ImplementUnitStatusEnum.PLACE_ON_FILE.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal()))
            return;
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(fdevImplementUnit.getDemand_id());
        if (null == demandBaseInfo) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前任务所属需求不存在"});
        }
        //评估中不允许新建任务，
        if (ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue()
                .equals(fdevImplementUnit.getImplement_unit_status_normal())) {
            throw new FdevException(ErrorConstants.TASK_UPDATE_IMPLEMENTUNIT_FAIL, new String[]{"当前实施单元为评估中"});
        }
        //将任务的stage转为实施单元的状态
        Integer unitStage = CommonUtils.pareStage(stage);
        fdevImplementUnit.setImplement_unit_status_normal(unitStage);
        //修改实施单元启动，STI，UAT，REL和投产时间
        fdevImplementUnit.setReal_start_date(unitRealStartDate);
        fdevImplementUnit.setReal_inner_test_date(null == unitRealInnerTestDate ? "" : unitRealInnerTestDate);
        fdevImplementUnit.setReal_test_date(null == unitRealTestDate ? "" : unitRealTestDate);
        fdevImplementUnit.setReal_test_finish_date(null == unitRealTestFinishDate ? "" : unitRealTestFinishDate);
        fdevImplementUnit.setReal_product_date(null == unitRealProductDate ? "" : unitRealProductDate);
        implementUnitDao.update(fdevImplementUnit);
        //获取所有实施单元的状态和时间，修改需求相关信息
        List<FdevImplementUnit> unitList = implementUnitDao.queryByDemandId(fdevImplementUnit.getDemand_id());
        List<FdevImplementUnit> collect = unitList.stream()
                .sorted(Comparator.comparingInt(FdevImplementUnit::getImplement_unit_status_normal))
                .collect(Collectors.toList());
        //获取所有实施单元最小阶段
        Integer minStage = collect.get(0).getImplement_unit_status_normal();
        if(minStage >= ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()){
            //需要考虑未新增实施单元板块
            List<RelatePartDetail> relatePartDetailList1 = demandBaseInfo.getRelate_part_detail();
            if(!CommonUtils.isNullOrEmpty(relatePartDetailList1)){
                for(RelatePartDetail relatePartDetail : relatePartDetailList1){
                    if(relatePartDetail.getAssess_status().compareToIgnoreCase(DemandAssess_status.EVALUATE_OVER.getValue()) < 0){
                        //说明还有个板块一定是在预评估的，该板块未新增实施单元
                        minStage = ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue();
                        break;
                    }
                }
            }
        }
        demandBaseInfo.setDemand_status_normal(minStage);
        //待评估和评估中的需求仅修改状态
        if (DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue().equals(minStage) || DemandEnum.DemandStatusEnum.EVALUATE.getValue().equals(minStage)) {
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
        } else {
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
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(unitList));
                    demandBaseInfo.setReal_inner_test_date("");
                    demandBaseInfo.setReal_test_date("");
                    demandBaseInfo.setReal_test_finish_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //SIT
                case 4:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(unitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(unitList));
                    demandBaseInfo.setReal_test_date("");
                    demandBaseInfo.setReal_test_finish_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //UAT
                case 5:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(unitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(unitList));
                    demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(unitList));
                    demandBaseInfo.setReal_test_finish_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //REL
                case 6:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(unitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(unitList));
                    demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(unitList));
                    demandBaseInfo.setReal_test_finish_date(dealTaskUnit.getRealTestFinishDate(unitList));
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //已投产
                case 7:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(unitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(unitList));
                    demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(unitList));
                    demandBaseInfo.setReal_test_finish_date(dealTaskUnit.getRealTestFinishDate(unitList));
                    demandBaseInfo.setReal_product_date(dealTaskUnit.getRealProductDate(unitList));
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public List<FdevImplementUnit> queryAvailableIpmpUnit(Map<String, Object> param) throws Exception {
//        List<DemandBaseInfo> query = demandBaseInfoDao.query(new DemandBaseInfo());
        List<DemandBaseInfo> query = demandBaseInfoDao.query2();
        if (!CommonUtils.isNullOrEmpty(query)) {
            Set<String> set = new HashSet<>();
            Map<String, String> nameMap = new HashMap();
            Map<String, String> noMap = new HashMap();
            query.stream().forEach(x -> {
                nameMap.put(x.getId(), x.getOa_contact_name());
                noMap.put(x.getId(), x.getOa_contact_no());
                set.add(x.getId());
            });
            List<FdevImplementUnit> unitList = demandBaseInfoDao.queryAvailableIpmpUnit();
            if (!CommonUtils.isNullOrEmpty(unitList)) {
                unitList.stream().forEach(x -> {
                    x.setOa_contact_name(nameMap.get(x.getDemand_id()));
                    x.setOa_contact_no(noMap.get(x.getDemand_id()));
                });
            }
            return unitList;
        } else
            return null;
    }

    @Override
    public List<FdevImplementUnit> queryAvailableIpmpUnitNew(Map<String, Object> param) throws Exception {
            List<FdevImplementUnit> unitList = demandBaseInfoDao.queryAvailableIpmpUnit();
            return unitList;
    }

    @Override
    public void updateByRecover(Map<String, Object> param) throws Exception {

        FdevImplementUnit fdevImplementUnitBefore = implementUnitDao.queryById(param.get(Dict.ID).toString());
        Integer iussBefore = fdevImplementUnitBefore.getImplement_unit_status_special();
        if (!CommonUtils.isNullOrEmpty(iussBefore) && ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue() == iussBefore) {
            //研发单元的计划日期从上到下要满足：上面的日期小于等于下面的日期
            String plan_start_date = (String) param.get(Dict.PLAN_START_DATE);
            String plan_inner_test_date = (String) param.get(Dict.PLAN_INNER_TEST_DATE);
            String plan_test_date = (String) param.get(Dict.PLAN_TEST_DATE);
            String plan_test_finish_date = (String) param.get(Dict.PLAN_TEST_FINISH_DATE);
            String plan_product_date = (String) param.get(Dict.PLAN_PRODUCT_DATE);
            String overdueType = (String) param.get(Dict.OVERDUETYPE);
            String overdueReason = (String) param.get(Dict.OVERDUEREASON);
            String approveState = (String) param.get(Dict.APPROVESTATE);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (!(sdf.parse(plan_start_date).getTime() <= sdf.parse(plan_inner_test_date).getTime()
                    && sdf.parse(plan_inner_test_date).getTime() <= sdf.parse(plan_test_date).getTime())){
                throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
            }

            if(!CommonUtils.isNullOrEmpty(plan_test_finish_date) && !(sdf.parse(plan_test_date).getTime() <= sdf.parse(plan_test_finish_date).getTime())){
                throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
            }
            if(!CommonUtils.isNullOrEmpty(plan_product_date) && !CommonUtils.isNullOrEmpty(plan_test_finish_date) && !(sdf.parse(plan_test_date).getTime() <= sdf.parse(plan_test_finish_date).getTime())){
                throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
            }

            //需求恢复中可以更新实施单元
            fdevImplementUnitBefore.setImplement_unit_status_special(ImplementUnitEnum.ImplementUnitDeferStatus.FINISTH.getValue());
            fdevImplementUnitBefore.setPlan_start_date((String) param.get(Dict.PLAN_START_DATE));
            fdevImplementUnitBefore.setPlan_inner_test_date((String) param.get(Dict.PLAN_INNER_TEST_DATE));
            fdevImplementUnitBefore.setPlan_test_date((String) param.get(Dict.PLAN_TEST_DATE));
            fdevImplementUnitBefore.setPlan_test_finish_date((String) param.get(Dict.PLAN_TEST_FINISH_DATE));
            fdevImplementUnitBefore.setPlan_product_date((String) param.get(Dict.PLAN_PRODUCT_DATE));
            fdevImplementUnitBefore.setOverdueType(overdueType);
            fdevImplementUnitBefore.setOverdueReason(overdueReason);
            fdevImplementUnitBefore.setApproveState(approveState);
            User user = CommonUtils.getSessionUser();
            String demand_id = fdevImplementUnitBefore.getDemand_id();
            boolean isDemandManager = roleService.isDemandManager();
            boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
            boolean isPartAsesser = roleService.isPartAsesser(demand_id, fdevImplementUnitBefore.getGroup(), user);
            boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
            boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnitBefore.getId(),user.getId());
            if (isDemandManager || isDemandLeader || isPartAsesser || isIpmpUnitLeader || isFdevUnitLeader) {
                implementUnitDao.update(fdevImplementUnitBefore);
            } else {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
            }
            //特殊需求状态，如果为最后一个，则将需求更新为恢复完成，如果不是最后一个，则不更新特殊状态的值
            Integer specialDemand = DemandDeferStatus.FINISTH.getValue();
            List<FdevImplementUnit> listFdev = queryByDemandId(demand_id);
            for (FdevImplementUnit fdevImp : listFdev) {
                if (!CommonUtils.isNullOrEmpty(fdevImp.getImplement_unit_status_special())
                        && DemandDeferStatus.FINISTH.getValue() != fdevImp.getImplement_unit_status_special()) {
                    specialDemand = null;
                    continue;
                }
            }

            //更新需求的计划时间、更新任务计划时间和状态、更新玉衡工单时间
            DemandBaseInfo demandBaseInfoNew = demandBaseInfoDao.queryById(demand_id);
            HashMap<String, String> dateByDemandIdMap = commonBusinessService.dateByDemandIdMap(demand_id);
            demandBaseInfoNew.setPlan_start_date(dateByDemandIdMap.get(Dict.PLAN_START_DATE));
            demandBaseInfoNew.setPlan_inner_test_date(dateByDemandIdMap.get(Dict.PLAN_INNER_TEST_DATE));
            demandBaseInfoNew.setPlan_test_date(dateByDemandIdMap.get(Dict.PLAN_TEST_DATE));
            demandBaseInfoNew.setPlan_test_finish_date(dateByDemandIdMap.get(Dict.PLAN_TEST_FINISH_DATE));
            demandBaseInfoNew.setPlan_product_date(dateByDemandIdMap.get(Dict.PLAN_PRODUCT_DATE));
            demandBaseInfoNew.setDemand_status_special(specialDemand);
            demandBaseInfoDao.update(demandBaseInfoNew);
            taskService.taskRecoverByImpl(fdevImplementUnitBefore.getFdev_implement_unit_no(), ImplementUnitEnum.ImplementUnitDeferStatus.FINISTH.getValue());
            commonBusinessService.updateYuhengPlanDate(fdevImplementUnitBefore.getFdev_implement_unit_no(),
                    fdevImplementUnitBefore.getPlan_inner_test_date(), fdevImplementUnitBefore.getPlan_test_date(),
                    fdevImplementUnitBefore.getPlan_product_date());
        }

    }

    @Override
    public Map<String, Object> queryByFdevNoAndDemandId(Map<String, Object> param) {
        //根据需求id查询需求信息，如果实施单元不为空，增加返回实施单元信息
        Map<String, Object> result = new HashMap<>();
        String demandId = (String) param.get(Dict.DEMAND_ID);
        String fdevNo = (String) param.get(Dict.FDEV_IMPLEMENT_UNIT_NO);
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        if (StringUtils.isNotBlank(fdevNo)) {
            FdevImplementUnit fdevImpl = implementUnitDao.queryByFdevNo(fdevNo);
            result.put(Dict.IMPLEMENT_UNIT_INFO, null != fdevImpl ? fdevImpl : new HashMap<>());
            if (null == demandBaseInfo && null != fdevImpl)
                demandBaseInfo = demandBaseInfoDao.queryById(fdevImpl.getDemand_id());
        }
        result.put(Dict.DEMAND_BASEINFO, null != demandBaseInfo ? demandBaseInfo : new HashMap<>());
        return result;
    }

    @Override
    public List<FdevImplementUnit> queryImplUnitByGroupId(String groupId, Boolean isParent) throws Exception {
        Set<String> groupids = new HashSet<>();
        if (isParent) {
            List groupArr = new ArrayList<>();
            groupArr.add(groupId);
            groupids = iRoleService.queryChildGroupByIds(groupArr);
        } else {
            groupids.add(groupId);
        }
        return implementUnitDao.queryImplUnitByGroupId(groupids);
    }

    @Override
    public List<FdevImplementUnit> queryImplUnitByipmpImplementUnitNo(String ipmpImplementUnitNo) {
        return implementUnitDao.queryImplUnitByipmpImplementUnitNo(ipmpImplementUnitNo);
    }

    @Override
    public List<FdevImplementUnit> queryImplByGroupAndDemandId(Map<String, Object> param) throws Exception {
        List<FdevImplementUnit> fdevImplementUnitList = implementUnitDao.queryByDemandIdAndGroup(
        		param.get(Dict.DEMAND_ID).toString(), param.get(Dict.GROUP).toString());
        this.completeFdevImplUnit(fdevImplementUnitList);
        User user = CommonUtils.getSessionUser();
        String demand_id = param.get(Dict.DEMAND_ID).toString();
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
        try {
            fdevImplementUnitList = addDelFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            fdevImplementUnitList = addUpdateFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            fdevImplementUnitList = addMountFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            fdevImplementUnitList = addApproveFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Map map = assessButton(param);

        return fdevImplementUnitList;
    }

    @Override
    public List<FdevImplementUnit> queryDailyFdevUnitList(Map<String, Object> param) throws Exception {
        String demand_id = param.get(Dict.DEMAND_ID).toString();
        String other_demand_task_num = "";
        if (!CommonUtils.isNullOrEmpty(param.get(Dict.OTHER_DEMAND_TASK_NUM)))
            other_demand_task_num = param.get(Dict.OTHER_DEMAND_TASK_NUM).toString();

        String group = "";
        if (!CommonUtils.isNullOrEmpty(param.get(Dict.GROUP)))
            group = param.get(Dict.GROUP).toString();
        List<FdevImplementUnit> fdevImplementUnitList = implementUnitDao.queryDailyFdevUnitList(
                demand_id, group, other_demand_task_num );
        this.completeFdevImplUnit(fdevImplementUnitList);
        User user = CommonUtils.getSessionUser();
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(demand_id, user.getUser_name_en());
        try {
            fdevImplementUnitList = addDelFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            fdevImplementUnitList = addUpdateFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            fdevImplementUnitList = addMountFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            fdevImplementUnitList = addApproveFlag(fdevImplementUnitList,user,demand_id,isDemandManager,isDemandLeader);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return fdevImplementUnitList;
    }

    /**
     * 编辑权限：需求管理员、需求牵头人、实施单元牵头人、本研发单元牵头人、本研发单元所属小组评估人
     * 1、评估完成的研发单元不可编辑
     * 2、挂载的实施单元状态为已投产、已撤销、暂缓、暂存，则不可编辑
     * @param fdevImplementUnitList
     * @param demandId
     * @return
     */
    public List<FdevImplementUnit> addUpdateFlag(List<FdevImplementUnit> fdevImplementUnitList,User user,String demandId,boolean isDemandManager,boolean isDemandLeader,boolean isIpmpUnitLeader) throws Exception {
        for(FdevImplementUnit fdevImplementUnit:fdevImplementUnitList){
            Map update_flag = new HashMap();
            update_flag.put("flag",true);//默认按钮亮
            update_flag.put("msg","");
            fdevImplementUnit.setUpdate_flag(update_flag);
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
            if("business".equals(demandBaseInfo.getDemand_type())){
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getIpmp_implement_unit_no())){
                    IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(fdevImplementUnit.getIpmp_implement_unit_no());
                    if(CommonUtils.isNullOrEmpty(ipmpUnit)){
                        logger.info("实施单元"+fdevImplementUnit.getIpmp_implement_unit_no()+"不存在");
                        continue;
                    }
                    if("已投产".equals(ipmpUnit.getImplStatusName()) || "已撤销".equals(ipmpUnit.getImplStatusName()) || "暂缓".equals(ipmpUnit.getImplStatusName()) || "暂存".equals(ipmpUnit.getImplStatusName())
                            && !ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue().equals(fdevImplementUnit.getImplement_unit_status_special())){
                        update_flag.put("flag",false);
                        update_flag.put("msg","所属实施单元为已投产、已撤销、暂缓、暂存时，研发单元不可修改！");
                        fdevImplementUnit.setUpdate_flag(update_flag);
                        continue;
                    }
                }
            }
            //已投产、已归档、已撤销、暂缓不可修改
            if(ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                    || ImplementUnitEnum.ImplementUnitStatusEnum.PLACE_ON_FILE.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                    || ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                    || ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue().equals(fdevImplementUnit.getImplement_unit_status_special())){
                update_flag.put("flag",false);
                update_flag.put("msg","研发单元状态已投产、已归档、已撤销、暂缓不可修改！");
                fdevImplementUnit.setUpdate_flag(update_flag);
                continue;
            }
            //业务需求评估完成后也能编辑
            if(!Constants.BUSINESS.equals(fdevImplementUnit.getDemand_type())){
                if(!ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                        && !ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue().equals(fdevImplementUnit.getImplement_unit_status_special())){
                    update_flag.put("flag",false);
                    update_flag.put("msg","评估完成的研发单元不可再修改！");
                    fdevImplementUnit.setUpdate_flag(update_flag);
                    continue;
                }
            }
            boolean isAssesser = roleService.isPartAsesser(demandId,fdevImplementUnit.getGroup(),user);
            boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnit.getId(),user.getId());

            if(!(isDemandManager || isDemandLeader || isAssesser || isIpmpUnitLeader || isFdevUnitLeader)){
                update_flag.put("flag",false);
                update_flag.put("msg","请联系需求牵头人、需求管理员、实施单元牵头人、本小组评估人、研发单元牵头人编辑！");
                fdevImplementUnit.setUpdate_flag(update_flag);
                continue;
            }
            //待审批的研发单元不可编辑
            if( Constants.WAIT.equals(fdevImplementUnit.getApproveState()) ){
                update_flag.put("flag",false);
                update_flag.put("msg","研发单元待审批不可编辑！");
                fdevImplementUnit.setUpdate_flag(update_flag);
                continue;
            }
        }
        return fdevImplementUnitList;
    }

    /**
     * 编辑权限：需求管理员、需求牵头人、实施单元牵头人、本研发单元牵头人、本研发单元所属小组评估人
     * 1、评估完成的研发单元不可编辑
     * 2、挂载的实施单元状态为已投产、已撤销、暂缓、暂存，则不可编辑
     * @param fdevImplementUnitList
     * @param demandId
     * @return
     */
    public List<FdevImplementUnit> addApproveFlag(List<FdevImplementUnit> fdevImplementUnitList,User user,String demandId,boolean isDemandManager,boolean isDemandLeader) throws Exception {
        for(FdevImplementUnit fdevImplementUnit : fdevImplementUnitList){
            Map<String, String> approveFlag = new HashMap();
            approveFlag.put("flag","0");//默认不展示
            approveFlag.put("msg","");
            fdevImplementUnit.setApproveFlag(approveFlag);
            //审批状态为未提交 拒绝 待审批 才展示
            if(Constants.NOSUBMIT.equals(fdevImplementUnit.getApproveState()) ||
                    Constants.WAIT.equals(fdevImplementUnit.getApproveState())
                    || Constants.REJECT.equals(fdevImplementUnit.getApproveState()) ){
                boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnit.getId(),user.getId());
                if(Constants.WAIT.equals(fdevImplementUnit.getApproveState())){
                    approveFlag.put("flag","1");// 1=展示，灰显
                    approveFlag.put("msg","该研发单元已提交申请,不可重复申请!");
                    fdevImplementUnit.setApproveFlag(approveFlag);
                }else if(ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal()) ||
                         ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue().equals(fdevImplementUnit.getImplement_unit_status_special())
                         || ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue().equals(fdevImplementUnit.getImplement_unit_status_special())){
                    //暂缓，撤销 恢复中展示 灰显
                    approveFlag.put("flag","1");// 1=展示，灰显
                    approveFlag.put("msg","研发单元状态为撤销、暂缓、恢复中,不可申请!");
                    fdevImplementUnit.setApproveFlag(approveFlag);
                }else if(!(isDemandManager || isDemandLeader || isFdevUnitLeader)){
                    approveFlag.put("flag","1");
                    approveFlag.put("msg","请联系需求牵头人、需求管理员、研发单元牵头人申请！");
                    fdevImplementUnit.setUpdate_flag(approveFlag);
                }else {
                    approveFlag.put("flag","2");//可点击
                    approveFlag.put("msg","");
                    fdevImplementUnit.setUpdate_flag(approveFlag);
                }
            }
        }
        return fdevImplementUnitList;
    }

    /**
     * 挂载按钮标记
     * 需求牵头人、需求管理员、实施单元牵头人、本研发单元牵头人、本研发单元所属小组评估人可挂载
     * 已挂载且研发单元状态不为评估中的研发单元不允许挂载
     * 施单元牵头人在fdev找不到的，不允许挂载
     * 仅评估完成的研发单元显示挂载按钮
     * @param fdevImplementUnitList
     * @param demandId
     * @return
     * @throws Exception
     */
    public List<FdevImplementUnit> addMountFlag(List<FdevImplementUnit> fdevImplementUnitList,User user,String demandId,boolean isDemandManager,boolean isDemandLeader,boolean isIpmpUnitLeader)  throws Exception {
        for(FdevImplementUnit fdevImplementUnit : fdevImplementUnitList){
            fdevImplementUnit.setMount_flag(true);
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
            if(CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())){
                //存量业务需求无挂载
                fdevImplementUnit.setMount_flag(false);
                continue;
            }
            if(ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue() == fdevImplementUnit.getImplement_unit_status_normal()){
                fdevImplementUnit.setMount_flag(false);
                continue;
            }

            //暂缓和撤销的研发单元不允许挂载
            Integer implement_unit_status_special = fdevImplementUnit.getImplement_unit_status_special();
            Integer implement_unit_status_normal = fdevImplementUnit.getImplement_unit_status_normal();
            if(!CommonUtils.isNullOrEmpty(implement_unit_status_special)){
                if(ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue() == implement_unit_status_special){
                    fdevImplementUnit.setMount_flag(false);
                    continue;
                }
            }else if(!CommonUtils.isNullOrEmpty(implement_unit_status_normal)){
                if(ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue() == implement_unit_status_normal){
                    fdevImplementUnit.setMount_flag(false);
                    continue;
                }
            }

            //已挂载且研发单元状态不为评估中的研发单元不允许挂载
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getIpmp_implement_unit_no()) && !fdevImplementUnit.getImplement_unit_status_normal().equals(ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue())){
                fdevImplementUnit.setMount_flag(false);
                continue;
            }

            boolean isAssesser = roleService.isPartAsesser(demandId, fdevImplementUnit.getGroup(), user);
            boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnit.getId(),user.getId());
            if(!(isDemandManager || isDemandLeader || isAssesser || isIpmpUnitLeader || isFdevUnitLeader)){
                fdevImplementUnit.setMount_flag(false);
            }
        }
        return fdevImplementUnitList;
    }

    public List<FdevImplementUnit> addDelFlag(List<FdevImplementUnit> fdevImplementUnitList,User user,String demandId,boolean isDemandManager,boolean isDemandLeader,boolean isIpmpUnitLeader)  throws Exception {
    	if(!CommonUtils.isNullOrEmpty(fdevImplementUnitList)) {
        	Integer del_flag = 4;//研发单元是否可删除标识 ,0 可以删除 默认不可删除
        	DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);

        	//获取需求特殊状态
        	Integer demand_status_special = demandBaseInfo.getDemand_status_special();
        	if(!CommonUtils.isNullOrEmpty(demand_status_special) && demand_status_special < 3) {
    			del_flag = 1;
        		for(FdevImplementUnit fdevImplementUnit : fdevImplementUnitList) {
        			fdevImplementUnit.setDel_flag(del_flag);//需求处于特殊状态无法删除
        		}
        	}else {
        		for(FdevImplementUnit fdevImplementUnit : fdevImplementUnitList) {
            		//判断研发单元状态
        			//获取研发单元状态
        			Integer implStatus = fdevImplementUnit.getImplement_unit_status_normal();
        			boolean isAssesser = roleService.isPartAsesser(demandId,fdevImplementUnit.getGroup(), user);
        			boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnit.getId(),user.getId());
        			if(isDemandManager || isDemandLeader || isAssesser || isIpmpUnitLeader || isFdevUnitLeader) {
        				if(!CommonUtils.isNullOrEmpty(implStatus)){
            				if(implStatus == 1) {
            					del_flag = 0;
            				}else if (implStatus == 2) {
            					//判断是否存在任务
                    			Integer taskList = taskService.queryNotDiscarddnum(fdevImplementUnit.getFdev_implement_unit_no());
                    			if(!CommonUtils.isNullOrEmpty(taskList) && taskList == 0 ) {
                    				if(isDemandLeader || isIpmpUnitLeader || isDemandManager) {
                    					del_flag = 0;//可以删除
                    				}else {
                    					del_flag = 3;//只有需求牵头人可以删除
                    				}
                    			}else {
                    				del_flag = 2;//该研发单元下存在任务不可删除
                    			}
            				}else{
            					del_flag = 2;
            				}
            			}else {
            				del_flag = 0;
            			}	
        			}else{
        				del_flag = 4;//该用户无权限删除
        			}
                    //待审批的研发单元不可编辑
                    if( Constants.WAIT.equals(fdevImplementUnit.getApproveState()) ){
                        del_flag = 5;//待审批的研发单元不可删除
                    }
        			
            		fdevImplementUnit.setDel_flag(del_flag);
//            		del_flag = 4;//默认用户无权限删除
        		}
        	}
       }
    	return fdevImplementUnitList;
    }
    
    
    /**
     * 更改板块状态
     */
    public void updateRelate(String demandId)  throws Exception{
    	DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
    	ArrayList<RelatePartDetail> relatePartDetail = demandBaseInfo.getRelate_part_detail();
    	List<FdevImplementUnit> implList = implementUnitDao.queryByDemandId(demandId);
    	Boolean statusFlag = false;
    	Integer relateFlag = 0;
    	List<Integer> fdevImplList = new ArrayList();
    	if(!CommonUtils.isNullOrEmpty(relatePartDetail)) {
    		for(RelatePartDetail relatePart : relatePartDetail) {
    			relateFlag = 0;
    			if(!CommonUtils.isNullOrEmpty(implList)) {
    				for(FdevImplementUnit fdevImplementUnit : implList) {
    					if(fdevImplementUnit.getGroup().equals(relatePart.getPart_id())) {
    						fdevImplList.add(fdevImplementUnit.getImplement_unit_status_normal());
    						relateFlag = relateFlag + 1;
    					}
    				}
    				if(relateFlag > 0) {
    					for(Integer reatel : fdevImplList) {
    						if( reatel == 1 ) {
    							statusFlag = true;
    						}
    					}
    					if(statusFlag) {
    						relatePart.setAssess_status(DemandAssess_status.EVALUATE.getValue());
    					}else {
    						relatePart.setAssess_status(DemandAssess_status.EVALUATE_OVER.getValue());
    					}
    				}else {
    					if(!DemandAssess_status.EVALUATE_OVER.getValue().equals(relatePart.getAssess_status())) {
        					relatePart.setAssess_status(DemandAssess_status.PRE_EVALUATE.getValue());
        				}
    				}
    			}else {
    				if(!DemandAssess_status.EVALUATE_OVER.getValue().equals(relatePart.getAssess_status())) {
    					relatePart.setAssess_status(DemandAssess_status.PRE_EVALUATE.getValue());
    				}
    			}
    		}
        	demandBaseInfo.setRelate_part_detail(relatePartDetail);
        	demandBaseInfoDao.update(demandBaseInfo);
    	}

    }

    /**
     * 校验研发单元日期
     * 1、各阶段计划日期不可早于上一阶段
     * 2、研发单元的计划启动日期、计划提交内测日期、计划提交用户测试日期要比实施单元对应日期早
     * @param fdevImplementUnit
     * @throws Exception
     */
    public void planDateCheck(FdevImplementUnit fdevImplementUnit) throws Exception {
        if(Dict.TECH.equals(fdevImplementUnit.getDemand_type()) || Dict.BUSINESS.equals(fdevImplementUnit.getDemand_type())){
            String demand_id = fdevImplementUnit.getDemand_id();
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demand_id);
            if (CommonUtils.isNullOrEmpty(demandBaseInfo)){
                throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR, new String[]{demand_id});
            }

            //研发单元的计划日期从上到下要满足：上面的日期小于等于下面的日期
            String plan_start_date = fdevImplementUnit.getPlan_start_date();
            String plan_inner_test_date = fdevImplementUnit.getPlan_inner_test_date();
            String plan_test_date = fdevImplementUnit.getPlan_test_date();
            String plan_test_finish_date = fdevImplementUnit.getPlan_test_finish_date();
            String plan_product_date = fdevImplementUnit.getPlan_product_date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (!(sdf.parse(plan_start_date).getTime() <= sdf.parse(plan_inner_test_date).getTime()
                    && sdf.parse(plan_inner_test_date).getTime() <= sdf.parse(plan_test_date).getTime())){
                throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
            }

            if(!CommonUtils.isNullOrEmpty(plan_test_finish_date) && !(sdf.parse(plan_test_date).getTime() <= sdf.parse(plan_test_finish_date).getTime())){
                throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
            }
            if(!CommonUtils.isNullOrEmpty(plan_product_date) && !CommonUtils.isNullOrEmpty(plan_test_finish_date) && !(sdf.parse(plan_test_date).getTime() <= sdf.parse(plan_test_finish_date).getTime())){
                throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
            }

            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getIpmp_implement_unit_no())){
                IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(fdevImplementUnit.getIpmp_implement_unit_no());
                //科技需求需要校验研发单元的计划启动日期、计划提交内测日期、计划提交用户测试日期要比实施单元对应日期早
                if("business".equals(demandBaseInfo.getDemand_type())){
                    if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanDevelopDate())){
                        String planDevelopDate = ipmpUnit.getPlanDevelopDate();
                        boolean a = CommonUtils.getJudgementDate(fdevImplementUnit.getPlan_start_date(), planDevelopDate);
                        if(!a){
                            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_PLAN_DATE_EROOR);
                        }
                    }
                    if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanInnerTestDate())){
                        String planInnerTestDate = ipmpUnit.getPlanInnerTestDate();
                        boolean b = CommonUtils.getJudgementDate(fdevImplementUnit.getPlan_inner_test_date(), planInnerTestDate);
                        if(!b){
                            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_PLAN_DATE_EROOR);
                        }
                    }
                    if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDate())){
                        String planTestStartDate = ipmpUnit.getPlanTestStartDate();
                        boolean c = CommonUtils.getJudgementDate(fdevImplementUnit.getPlan_test_date(), planTestStartDate);
                        if(!c){
                            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_PLAN_DATE_EROOR);
                        }
                    }
                    if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDate())
                            && !CommonUtils.isNullOrEmpty(fdevImplementUnit.getPlan_test_finish_date()) ){
                        String planTestFinishDate = ipmpUnit.getPlanTestFinishDate();
                        boolean d = CommonUtils.getJudgementDate(fdevImplementUnit.getPlan_test_finish_date(), planTestFinishDate);
                        if(!d){
                            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_PLAN_DATE_EROOR);
                        }
                    }
                    if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDate())
                            && !CommonUtils.isNullOrEmpty(fdevImplementUnit.getPlan_product_date()) ){
                        String planProductDate = ipmpUnit.getPlanProductDate();
                        boolean e = CommonUtils.getJudgementDate(fdevImplementUnit.getPlan_product_date(), planProductDate);
                        if(!e){
                            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_PLAN_DATE_EROOR);
                        }
                    }
                }
            }
        }else if(Dict.DAILY.equals(fdevImplementUnit.getDemand_type())){//日常需求
            String demand_id = fdevImplementUnit.getDemand_id();
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demand_id);
            if (CommonUtils.isNullOrEmpty(demandBaseInfo)){
                throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR, new String[]{demand_id});
            }

            //日常需求的研发单元只有开发 和结束日期
            String plan_start_date = fdevImplementUnit.getPlan_start_date();
            String plan_product_date = fdevImplementUnit.getPlan_product_date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (!CommonUtils.isNullOrEmpty(plan_start_date) && !CommonUtils.isNullOrEmpty(plan_product_date)
                    && !(sdf.parse(plan_start_date).getTime() <= sdf.parse(plan_product_date).getTime())){
                throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
            }

            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getOther_demand_task_num())){
                OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit.getOther_demand_task_num());
                //日常需求需要校验研发单元的计划日期其他需求任务对应日期早
                if(!CommonUtils.isNullOrEmpty(otherDemandTask.getPlanStartDate())){
                    String planStartDate = otherDemandTask.getPlanStartDate();
                    boolean a = CommonUtils.getJudgementDate(plan_start_date, planStartDate);
                    if(!a){
                        throw new FdevException(ErrorConstants.DAILY_ERROR_PLAN_DATE);
                    }
                }
                if(!CommonUtils.isNullOrEmpty(otherDemandTask.getPlanDoneDate())
                        && !CommonUtils.isNullOrEmpty(plan_product_date)){
                    String planDoneDate = otherDemandTask.getPlanDoneDate();
                    boolean b = CommonUtils.getJudgementDate(plan_product_date, planDoneDate);
                    if(!b){
                        throw new FdevException(ErrorConstants.DAILY_ERROR_PLAN_DATE);
                    }
                }
            }
        }
    }

    public FdevImplementUnit addAndSupplyCommon(FdevImplementUnit fdevImplementUnit) throws Exception {
        User user = CommonUtils.getSessionUser();
        String todayDate = TimeUtil.getDateStamp(new Date());
        //查询创建时间为今天的研发单元编号，并根据创建时间排序，取最晚的那条数据
        FdevImplementUnit fdevImplementUnitLatest = implementUnitDao.queryByCreatetime(todayDate);
        String fdev_implement_unit_no = "FDEV-" + todayDate + "-";
        if (CommonUtils.isNullOrEmpty(fdevImplementUnitLatest)) {
            fdev_implement_unit_no = fdev_implement_unit_no + "00001";
        } else {
            String fiunLatest = fdevImplementUnitLatest.getFdev_implement_unit_no();
            String[] fiunoLatestArray = fiunLatest.split("-");
            int no_latest = Integer.parseInt(fiunoLatestArray[fiunoLatestArray.length - 1]) + 1;
            fdev_implement_unit_no = fdev_implement_unit_no + String.format("%05d", no_latest);
        }
        //研发单元编号
        fdevImplementUnit.setFdev_implement_unit_no(fdev_implement_unit_no);
        //添加研发单元标识，老数据不添加
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(fdevImplementUnit.getDemand_id());
        if(!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())){
            fdevImplementUnit.setIs_new(true);
        }
        //研发单元状态设为“评估中”
        fdevImplementUnit.setImplement_unit_status_normal(ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue());

        List<UserInfo> implement_leader_all = fdevImplementUnit.getImplement_leader_all();
        HashSet<String> implement_leader = new HashSet<String>();
        for (UserInfo userInfo : implement_leader_all) {
            implement_leader.add(userInfo.getId());
        }
        fdevImplementUnit.setImplement_leader(implement_leader);
        fdevImplementUnit.setCreate_user(user.getId());
//        UserInfo create_user_all = new UserInfo();
//        create_user_all.setId(user.getId());
//        create_user_all.setUser_name_cn(user.getUser_name_cn());
//        create_user_all.setUser_name_en(user.getUser_name_en());
//        fdevImplementUnit.setCreate_user_all(create_user_all);
        fdevImplementUnit.setCreate_time(TimeUtil.getTimeStamp(new Date()));
        if (fdevImplementUnit.isUi_verify()) {
            fdevImplementUnit.setUi_verify(true);
        } else {
            fdevImplementUnit.setUi_verify(false);
        }

        return  fdevImplementUnit;
    }
    public List<FdevImplementUnit> queryImplPreImplmentByDemandId(String demandId) {
        return implementUnitDao.queryImplPreImplmentByDemandId(demandId);
    }

    @Override
    public FdevImplementUnit queryFdevImplUnitDetail(Map<String, Object> param) {
        //查询研发单元详情
        String fdev_unit_no = (String) param.get(Dict.FDEV_IMPLEMENT_UNIT_NO);
        FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByFdevNo(fdev_unit_no);
        List<FdevImplementUnit> fdevImplementUnits = new ArrayList<>();
        fdevImplementUnits.add(fdevImplementUnit);
        fdevImplementUnits = this.completeFdevImplUnit(fdevImplementUnits);
        fdevImplementUnit = fdevImplementUnits.get(0);
        return fdevImplementUnit;
    }

    /**
     * 需求牵头人、需求管理员、实施单元牵头人、本研发单元牵头人、本研发单元所属小组评估人可挂载
     * 已挂载且研发单元状态不为评估中的研发单元不允许挂载
     * 已投产、暂缓、暂存、撤销状态的实施单元不允许被挂载
     * 实施单元牵头人在fdev找不到的，不允许挂载
     * 挂载完成后，需向ipmp更新实施单元的实际日期
     * @param param
     */
    @Override
    public void mount(Map<String, Object> param) throws Exception {
        String fdevImplUnitId = (String) param.get(Dict.ID);
        String ipmpUnitNum = (String) param.get(Dict.IPMP_IMPLEMENT_UNIT_NO);
        String demandId = (String) param.get(Dict.DEMAND_ID);
        String plan_start_date = ((String) param.get(Dict.PLAN_START_DATE)).replaceAll("/","-");
        String plan_inner_test_date = ((String) param.get(Dict.PLAN_INNER_TEST_DATE)).replaceAll("/","-");
        String plan_test_date = ((String) param.get(Dict.PLAN_TEST_DATE)).replaceAll("/","-");
        String plan_test_finish_date = ((String) param.get(Dict.PLAN_TEST_FINISH_DATE)).replaceAll("/","-");
        String plan_product_date = ((String) param.get(Dict.PLAN_PRODUCT_DATE)).replaceAll("/","-");

        FdevImplementUnit fdevImplementUnit = implementUnitDao.queryById(fdevImplUnitId);
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        //暂缓和撤销的研发单元不允许挂载
        Integer implement_unit_status_special = fdevImplementUnit.getImplement_unit_status_special();
        Integer implement_unit_status_normal = fdevImplementUnit.getImplement_unit_status_normal();
        if(!CommonUtils.isNullOrEmpty(implement_unit_status_special)){
            if(ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue() == implement_unit_status_special){
                throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR4);
            }
        }else if(!CommonUtils.isNullOrEmpty(implement_unit_status_normal)){
            if(ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue() == implement_unit_status_normal){
                throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR5);
            }
        }

        //已挂载且研发单元状态不为评估中的研发单元不允许挂载
        if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getIpmp_implement_unit_no()) && !fdevImplementUnit.getImplement_unit_status_normal().equals(ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue())){
            throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR1);
        }

        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnitNum);
        //业务需求下实施单元牵头人在fdev找不到的，不允许挂载
        if("business".equals(demandBaseInfo.getDemand_type())){
            //已投产、暂缓、暂存、撤销状态的实施单元不允许被挂载
            String ipmpUnitStatus = ipmpUnit.getImplStatusName();
            if("已投产".equals(ipmpUnitStatus) || "暂缓".equals(ipmpUnitStatus) || "暂存".equals(ipmpUnitStatus) ||"已撤销".equals(ipmpUnitStatus)){
                throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR2);
            }
            if("3".equals(ipmpUnit.getLeaderFlag())){
                throw new FdevException(ErrorConstants.IMPLEMENTUNIT_MOUNT_EROOR3);
            }
            //评估完成的研发单元，判断挂载到同步后的实施单元后，是否存在工作量超出
            /*if("1".equals(ipmpUnit.getSyncFlag()) && ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue() <= fdevImplementUnit.getImplement_unit_status_normal()){
                this.checkIpmpExceptWork(ipmpUnit,fdevImplementUnit);
            }*/
        }

        //
        /**
         * 1、需求管理员/需求牵头人/所属小组评估人/实施单元牵头人/当前研发单元牵头人
         */
        User user = CommonUtils.getSessionUser();
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demandId, user.getId());
        boolean isAssesser = roleService.isPartAsesser(demandId, fdevImplementUnit.getGroup(), user);
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(demandId, user.getUser_name_en());
        boolean isFdevUnitLeader = roleService.isFdevUnitLeader(fdevImplementUnit.getId(),user.getId());

        if(isDemandManager || isDemandLeader || isAssesser || isIpmpUnitLeader || isFdevUnitLeader){
            //更新研发单元所属的实施单元、计划时间
            fdevImplementUnit.setIpmp_implement_unit_no(ipmpUnitNum);
            //修改计划时间
            fdevImplementUnit.setPlan_start_date(plan_start_date);
            fdevImplementUnit.setPlan_inner_test_date(plan_inner_test_date);
            fdevImplementUnit.setPlan_test_date(plan_test_date);
            fdevImplementUnit.setPlan_test_finish_date(plan_test_finish_date);
            fdevImplementUnit.setPlan_product_date(plan_product_date);
            //校验计划时间的规则
            this.planDateCheck(fdevImplementUnit);//校验计划时间是否符合规则
            implementUnitDao.update(fdevImplementUnit);
            //更新实施单元和需求的状态、实际时间
            if("business".equals(demandBaseInfo.getDemand_type())){
                ipmpUnitService.updateIpmpUnitStatus(ipmpUnit,user.getUser_name_en());
                //没同步的实施单元，更新实施单元的预期工作量
                if(!"1".equals(ipmpUnit.getSyncFlag()) && (ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue() <= fdevImplementUnit.getImplement_unit_status_normal())){
                    Set ipmpUnits = new HashSet();
                    ipmpUnits.add(ipmpUnit);
                    this.updateIpmpExceptWork(ipmpUnits);
                }
            }else if( Dict.DAILY.equals(demandBaseInfo.getDemand_type()) ){
                //日常需求推 其他需求任务
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getOther_demand_task_num())){
                    OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(fdevImplementUnit.getOther_demand_task_num());
                    otherDemandTaskService.updateStatus(otherDemandTask);
                }
            }
            //更新需求计划时间，计划启动日期：取最小。其他计划日期：取最晚。
            HashMap<String, String> dateByDemandIdMap = commonBusinessService.dateByDemandIdMapAssessOver(demandId);
            demandBaseInfo.setPlan_start_date(dateByDemandIdMap.get(Dict.PLAN_START_DATE));
            demandBaseInfo.setPlan_inner_test_date(dateByDemandIdMap.get(Dict.PLAN_INNER_TEST_DATE));
            demandBaseInfo.setPlan_test_date(dateByDemandIdMap.get(Dict.PLAN_TEST_DATE));
            demandBaseInfo.setPlan_test_finish_date(dateByDemandIdMap.get(Dict.PLAN_TEST_FINISH_DATE));
            demandBaseInfo.setPlan_product_date(dateByDemandIdMap.get(Dict.PLAN_PRODUCT_DATE));
            demandBaseInfo = demandBaseInfoDao.updateDemandBaseInfo(demandBaseInfo);
            //更新需求状态
            ipmpUnitService.updateDemandStatus(demandBaseInfo);
        }
    }

    @Override
    public Map queryPaginationByIpmpUnitNo(Map<String, Object> param) throws Exception {

        String demandId = param.get(Dict.DEMAND_ID).toString();
        String other_demand_task_num = "";
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);

        Map result = new HashMap();
        //日常需求查询列表
        List<FdevImplementUnit> data = new ArrayList<>();
        if ( Dict.DAILY.equals(demandBaseInfo.getDemand_type()) || !CommonUtils.isNullOrEmpty(param.get(Dict.OTHER_DEMAND_TASK_NUM))){
            other_demand_task_num = (String)param.get(Dict.OTHER_DEMAND_TASK_NUM);
            String group = "";
            if (!CommonUtils.isNullOrEmpty(param.get(Dict.GROUP)))
                group = param.get(Dict.GROUP).toString();
            data = implementUnitDao.queryDailyFdevUnitList(
                    demandId, group, other_demand_task_num );
        }else{
            Integer pageSize = (Integer) param.get(Dict.SIZE);//页面大小
            Integer currentPage = (Integer) param.get(Dict.INDEX);//当前页
            if (CommonUtils.isNullOrEmpty(pageSize)) {
                pageSize = 0;
            }
            if (CommonUtils.isNullOrEmpty(currentPage)) {
                currentPage = 1;
            }
            Integer start = pageSize * (currentPage - 1);   //起始
            String ipmpUnitNo = "";
            if (!CommonUtils.isNullOrEmpty(param.get(Dict.IPMP_UNIT_NO)))
                ipmpUnitNo = param.get(Dict.IPMP_UNIT_NO).toString();

            String group = null;
            if(!CommonUtils.isNullOrEmpty(param.get(Dict.GROUP))){
                group = param.get(Dict.GROUP).toString();
            }
            data = implementUnitDao.queryPaginationByIpmpUnitNo(demandId, start, pageSize, ipmpUnitNo,group);
            Long count = implementUnitDao.queryPaginationByIpmpUnitNo(demandId, ipmpUnitNo,group);
            result.put(Dict.COUNT, count);
        }
        data = completeFdevImplUnit(data);//补全团队中文名、创建人中文名等信息
        /**
         * 1、需求管理员/需求牵头人/实施单元牵头人
         */
        User user = CommonUtils.getSessionUser();
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demandId, user.getId());
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(demandId, user.getUser_name_en());
        //如果是日常需求，用其它需求任务负责人权限替换实施单元牵头人
        if (Dict.DAILY.equals(demandBaseInfo.getDemand_type()) || !CommonUtils.isNullOrEmpty(param.get(Dict.OTHER_DEMAND_TASK_NUM))){
            isIpmpUnitLeader = roleService.isOtherDemandTaskLeader(demandId, user.getId());
        }
        try {
            data = addDelFlag(data,user,demandId,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            data = addUpdateFlag(data,user,demandId,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            data = addMountFlag(data,user,demandId,isDemandManager,isDemandLeader,isIpmpUnitLeader);
            data = addApproveFlag(data,user,demandId,isDemandManager,isDemandLeader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.put(Dict.DATA, data);
        return result;
    }

    /**
     * 新增的研发单元，需求管理员、需求牵头人、本涉及小组评估人可评估完成
     * 补充的研发单元，仅需求牵头人可评估完成
     * @param param
     * @return
     */
    @Override
    public Map assessButton(Map<String, Object> param) throws Exception {
        Map result = new HashMap<>();
        result.put("flag",true);//默认按钮亮
        result.put("msg","");
        String demandId = param.get(Dict.DEMAND_ID).toString();
        String implpart = param.get(Dict.GROUP).toString();

        User user = CommonUtils.getSessionUser();
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader(demandId, user.getId());
        boolean isAssesser = roleService.isPartAsesser(demandId, implpart, user);

        //涉及小组评估状态为评估中才可以点评估完成
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        ArrayList<RelatePartDetail> relate_part_details = demandBaseInfo.getRelate_part_detail();
        if (null == relate_part_details) relate_part_details = new ArrayList<>();
        for(RelatePartDetail relate_part_detail : relate_part_details){
            if(implpart.equals(relate_part_detail.getPart_id())){
                if(DemandAssess_status.PRE_EVALUATE.getValue().equals(relate_part_detail.getAssess_status())){
                    result.put("flag",false);
                    result.put("msg","研发单元列表为空，不可评估完成！");
                    break;
                } else if(DemandAssess_status.EVALUATE_OVER.getValue().equals(relate_part_detail.getAssess_status())){
                    result.put("flag",false);
                    result.put("msg","已完成初次评估，无需再次评估确认！");
                    break;
                }else {
                    //评估中的小组，根据是新增还是补充的研发单元来判用户有无权限
                    if(!(isDemandManager || isDemandLeader || isAssesser)){
                        result.put("flag",false);
                        result.put("msg","当前用户无权限！");
                        break;
                    }
                    //查询该需求下，该小组的研发单元
                    List<FdevImplementUnit> fdevImplementUnitGroup = implementUnitDao.queryByDemandIdAndGroup(demandId, implpart);
                    if( !CommonUtils.isNullOrEmpty(fdevImplementUnitGroup) ){

                        for (FdevImplementUnit fdevImplementUnit : fdevImplementUnitGroup) {
                            result.put("flag",false);
                            result.put("msg","研发单元申请审批通过后方评估完成,不可直接评估完成!");
                            //状态为评估中 且不为暂缓或恢复中 不需要审批的
                            if(ImplementUnitEnum.ImplementUnitStatusEnum.EVALUATE.getValue() == fdevImplementUnit.getImplement_unit_status_normal()
                                    && (CommonUtils.isNullOrEmpty(fdevImplementUnit.getImplement_unit_status_special()) ||
                                    ImplementUnitEnum.ImplementUnitDeferStatus.FINISTH.getValue() == fdevImplementUnit.getImplement_unit_status_special())
                                    && CommonUtils.isNullOrEmpty(fdevImplementUnit.getApproveState())){
                                result.put("flag",true);
                                result.put("msg","");
                                break;
                            }

                        }
                    }
                    //查询该需求下，该小组的研发单元
                    //板块状态为1：评估中，说明这个板块有研发单元，需要去判断研发单元的最大值
                    FdevImplementUnit fdevImplementUnits = implementUnitDao.queryByDemandIdAndGroupMax(demandId, implpart);
                    if (!CommonUtils.isNullOrEmpty(fdevImplementUnits)) {
                        if ((CommonUtils.isNullOrEmpty(fdevImplementUnits.getAdd_flag())
                                || 0 == fdevImplementUnits.getAdd_flag()) && (CommonUtils.isNullOrEmpty(fdevImplementUnits.getImplement_unit_status_normal()) || 1 == fdevImplementUnits.getImplement_unit_status_normal())) {
                            //如果实施单元的补充标识为null或者为0，且还在评估中，说明该板块是新增
                        } else {
                            if(!isDemandLeader && !isDemandManager){
                                result.put("flag",false);
                                result.put("msg","当前用户无权限！");
                            }
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map checkWork(Map<String, Object> param) throws Exception {
        String demandId = param.get(Dict.DEMAND_ID).toString();
        String ipmpUnitNum = param.get(Dict.IPMP_IMPLEMENT_UNIT_NO).toString();
        String id = "";//研发单元id,编辑时用
        IpmpUnit ipmpUnit = new IpmpUnit();
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.ID))){
            id = param.get(Dict.ID).toString();
        }
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        double deptWorkload = 0.00;
        double companyWorkload = 0.00;
        if("business".equals(demandBaseInfo.getDemand_type())){
            if(!CommonUtils.isNullOrEmpty(ipmpUnitNum)){
                ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnitNum);
            }
            List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryImplUnitByipmpImplementUnitNo(ipmpUnitNum);

            if(!CommonUtils.isNullOrEmpty(fdevImplementUnits)){
                for(FdevImplementUnit temp : fdevImplementUnits){
                    //累计除自身的其他研发单元
                    if(CommonUtils.isNullOrEmpty(id) || !temp.getId().equals(id)){
                        deptWorkload += temp.getDept_workload();
                        companyWorkload += temp.getCompany_workload();
                    }
                }
            }
        }
        DecimalFormat df1 = new DecimalFormat("0.00");//保留两位小数
        Map result = new HashMap();
        result.put("dept_work",df1.format((ipmpUnit.getExpectOwnWorkload() - deptWorkload)) );
        result.put("company_work",df1.format(ipmpUnit.getExpectOutWorkload() - companyWorkload));
        return result;
    }

    @Override
    //判断当前用户是否展示新增按钮 需求管理员或者需求牵头人或者设计小组评估人或者实施单元牵头人 其他需求任务负责人
    public String isShowAdd(Map<String, Object> param) throws Exception {
        String demandId = (String) param.get(Dict.DEMANDID);
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        // 暂缓 恢复中的需求 或 涉及小组为空 不展示按钮
        if(DemandEnum.DemandStatusEnum.IS_CANCELED.getValue().equals(demandBaseInfo.getDemand_status_normal())
                || DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue().equals(demandBaseInfo.getDemand_status_normal())
                ||(!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_status_special())
                && (DemandDeferStatus.DEFER.getValue().equals(demandBaseInfo.getDemand_status_special())
                || DemandDeferStatus.RECOVER.getValue().equals(demandBaseInfo.getDemand_status_special())))){
            return "归档、撤销、暂缓或恢复中的需求，不可新增研发单元！";
        }
        if(CommonUtils.isNullOrEmpty(demandBaseInfo.getRelate_part())){
            return "涉及小组为空，请先前往需求编辑添加涉及小组！";
        }

        //日常需求 查询其他需求任务
        if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
            param.put(Dict.DEMANDID,demandId);
            Map<String, Object> returnMap = otherDemandTaskDao.queryOtherDemandTaskList(param);
            List<OtherDemandTask> otherDemandTasks = (List<OtherDemandTask>) returnMap.get(Dict.DATA);
            if(CommonUtils.isNullOrEmpty(otherDemandTasks)){
                return "其他需求任务为空，请先前往新增其他需求任务！";
            }
        }

        boolean isShowAdd = roleService.isDemandManager();//是否需求管理员
        if(!isShowAdd){//不是需求管理员
            User user = CommonUtils.getSessionUser();
            isShowAdd = roleService.isDemandLeader(demandId, user.getId());//判断是否需求牵头人
            if(!isShowAdd){//不是需求牵头人
                isShowAdd = roleService.isDemandGroupLeader(demandId, user.getId());//判断是否涉及小组评估人
                if(!isShowAdd){//不是需求牵头人涉及小组评估人
                    if( !CommonUtils.isNullOrEmpty(demandBaseInfo) ){
                        //日常需求 查询其他需求任务
                        if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){
                            param.put(Dict.DEMANDID,demandId);
                            Map<String, Object> returnMap = otherDemandTaskDao.queryOtherDemandTaskList(param);
                            List<OtherDemandTask> otherDemandTasks = (List<OtherDemandTask>) returnMap.get(Dict.DATA);
                            if(!CommonUtils.isNullOrEmpty(otherDemandTasks)){
                                for (OtherDemandTask otherDemandTask : otherDemandTasks) {
                                    //判断是否其他需求任务负责人
                                    if( roleService.isOtherDemandTaskLeader(otherDemandTask, user.getId()) ){
                                        isShowAdd = true ;
                                        break;
                                    }
                                }
                            }
                        }else{
                            //科技或业务需求
                            isShowAdd = roleService.isIpmpUnitLeader(demandId,user.getId());
                        }
                    }
                }

            }
        }
        //false 当前用户无权限新增研发单元
        if(!isShowAdd){
            return "用户权限不足，无法新增研发单元！";
        }
        return "";
    }

    //封装研发单元信息
    public List<FdevImplementUnit> completeFdevImplUnit(List<FdevImplementUnit> fdevImplementUnitList){
        if (!CommonUtils.isNullOrEmpty(fdevImplementUnitList)) {
            try {
                //塞需求信息
                DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(fdevImplementUnitList.get(0).getDemand_id());
                for (FdevImplementUnit fdevImplementUnit : fdevImplementUnitList) {
                    Map<String, Object> map = roleService.queryGroup(fdevImplementUnit.getGroup());
                    fdevImplementUnit.setGroup_cn((String)map.get(Dict.GROUP_NAME));
                    String create_user = fdevImplementUnit.getCreate_user();
                    if(CommonUtils.isNullOrEmpty(fdevImplementUnit.getCreate_user_all())){
                        Map<String, Object> createUserInfo = roleService.queryUserbyid(create_user);
                        UserInfo create_user_all = new UserInfo();
                        create_user_all.setId(create_user);
                        if(!CommonUtils.isNullOrEmpty(createUserInfo)){
                            create_user_all.setUser_name_en((String) createUserInfo.get(Dict.USER_NAME_EN));
                            create_user_all.setUser_name_cn((String) createUserInfo.get(Dict.USER_NAME_CN));
                        }
                        fdevImplementUnit.setCreate_user_all(create_user_all);
                    }
                    fdevImplementUnit.setOa_contact_no(demandBaseInfo.getOa_contact_no());
                    fdevImplementUnit.setOa_contact_name(demandBaseInfo.getOa_contact_name());

                    String ipmp_unit_no = fdevImplementUnit.getIpmp_implement_unit_no();
                    fdevImplementUnit.setHave_link(0);
                    if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)){
                        //查询研发单元所属实施单元的牵头人域账号集合
                        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
                        List<String> implLeaders = new ArrayList<>();
                        if(!CommonUtils.isNullOrEmpty(ipmpUnit)){
                            String implLeader = ipmpUnit.getImplLeader();
                            String[] implLeaderStrings = implLeader.split(",");
                            implLeaders = Arrays.asList(implLeaderStrings);
                            fdevImplementUnit.setHave_link(1);//实施单元是否已同步至fdev，1已同步，0未同步
                            //关联实施单元状态是否为(已投产、已撤销、暂缓、暂存)
                            String implStatusName = ipmpUnit.getImplStatusName();
                            if("已投产".equals(implStatusName) || "已撤销".equals(implStatusName) || "暂缓".equals(implStatusName) || "暂存".equals(implStatusName)){
                                fdevImplementUnit.setIs_fourStatus(true);
                            }
                        }else {
                            fdevImplementUnit.setHave_link(0);
                        }
                        fdevImplementUnit.setIpmp_unit_leader(implLeaders);
                    }

                    //查询研发单元所属小组的评估人id集合
                    ArrayList<RelatePartDetail>  relate_part_details= demandBaseInfo.getRelate_part_detail();
                    if (null == relate_part_details) relate_part_details = new ArrayList<>();
                    for(RelatePartDetail relate_part_detail : relate_part_details){
                        if(fdevImplementUnit.getGroup().equals(relate_part_detail.getPart_id())){
                            HashSet<String> assess_user = relate_part_detail.getAssess_user();
                            fdevImplementUnit.setAccess_user(new ArrayList<>(assess_user));
                            break;
                        }
                    }
                    //添加延期标志
                    fdevImplementUnit.setDelayFlag(isDelay(fdevImplementUnit));
                }
            } catch (Exception e) {
                logger.error("---select group error22---");
            }
        }
        return fdevImplementUnitList;
    }

    /**
     * 实施单元预期工作量=各研发单元预期工作量之和
     * 如果实施单元的预期工作量已经同步给了ipmp，则需要判断研发单元预期工作量之和不得超过实施单元预期工作量。
     * 否则直接修改实施单元的预期工作量
     * @param ipmpUnit
     */
    public void checkIpmpExceptWork(IpmpUnit ipmpUnit, FdevImplementUnit fdevImplementUnit){
        List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryImplUnitByipmpImplementUnitNo(ipmpUnit.getImplUnitNum());
        double deptWorkload = 0.00;
        double companyWorkload = 0.00;
        if(!CommonUtils.isNullOrEmpty(fdevImplementUnits)){
            for(FdevImplementUnit temp : fdevImplementUnits){
                //累计除自身的其他研发单元
                if(!temp.getId().equals(fdevImplementUnit.getId())){
                    deptWorkload += temp.getDept_workload();
                    companyWorkload += temp.getCompany_workload();
                }
            }
        }
        deptWorkload += fdevImplementUnit.getDept_workload();
        companyWorkload += fdevImplementUnit.getCompany_workload();
        DecimalFormat df1 = new DecimalFormat("0.00");//保留两位小数
        if(deptWorkload > ipmpUnit.getExpectOwnWorkload()){
            if(ipmpUnit.getExpectOwnWorkload()+fdevImplementUnit.getDept_workload()-deptWorkload == 0){
                throw new FdevException(ErrorConstants.EXPECT_OWN_WORK_ERROR3);
            }else {
                throw new FdevException(ErrorConstants.EXPECT_OWN_WORK_ERROR,new String[]{df1.format(ipmpUnit.getExpectOwnWorkload()+fdevImplementUnit.getDept_workload()-deptWorkload)});
            }
        }
        if(companyWorkload > ipmpUnit.getExpectOutWorkload()){
            throw new FdevException(ErrorConstants.EXPECT_OUT_WORK_ERROE,new String[]{df1.format(ipmpUnit.getExpectOutWorkload()+fdevImplementUnit.getCompany_workload()-companyWorkload)});
        }
    }

    /**
     *
     * 判断评估的研发单元是否都满足预计工作量之和<=实施单元
     */
    public void checkIpmpExceptWorks(Set<IpmpUnit> ipmpUnits) throws Exception {
        for(IpmpUnit ipmpUnit : ipmpUnits){
            if("1".equals(ipmpUnit.getSyncFlag())){
                //仅同步过的实施单元需满足该校验
                List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryImplUnitByipmpImplementUnitNo(ipmpUnit.getImplUnitNum());
                double deptWorkload = 0.00;
                double companyWorkload = 0.00;
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnits)){
                    for(FdevImplementUnit temp : fdevImplementUnits){
                        deptWorkload += temp.getDept_workload();
                        companyWorkload += temp.getCompany_workload();
                    }
                    if(deptWorkload > ipmpUnit.getExpectOwnWorkload() || companyWorkload > ipmpUnit.getExpectOutWorkload()){
                        throw new FdevException(ErrorConstants.EXPECT_WORK_ERROE,new String []{ipmpUnit.getImplUnitNum()});
                    }
                }
            }
        }
    }

    /**
     * 批量更新ipmp实施单元的预计工作量，仅业务需求
     * 实施单元还没同步到ipmp时，可更新
     */
    public void updateIpmpExceptWork(Set<IpmpUnit> ipmpUnits) throws Exception {
        for(IpmpUnit ipmpUnit : ipmpUnits){
            if(!"1".equals(ipmpUnit.getSyncFlag())) {
                List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryImplUnitByipmpImplementUnitNo(ipmpUnit.getImplUnitNum());
                double deptWorkload = 0.00;
                double companyWorkload = 0.00;
                if (!CommonUtils.isNullOrEmpty(fdevImplementUnits)) {
                    for (FdevImplementUnit temp : fdevImplementUnits) {
                        if (ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue() <= temp.getImplement_unit_status_normal()) {
                            deptWorkload += temp.getDept_workload();
                            companyWorkload += temp.getCompany_workload();
                        }
                    }
                }
                ipmpUnit.setExpectOwnWorkload(deptWorkload);
                ipmpUnit.setExpectOutWorkload(companyWorkload);
                ipmpUnitDao.updateIpmpUnit(ipmpUnit);
            }
        }
    }

    public void checkWork(double deptWorkload,double companyWorkload){
        //判断预期工作量是否为两位小数
        String dept_workload = Double.toString(deptWorkload);
        if(!CommonUtils.isNumber(dept_workload)){
            throw new FdevException(ErrorConstants.EXPECT_OWN_WORK_ERROR2);
        }
        if(!CommonUtils.isNullOrEmpty(companyWorkload)){
            String company_workload = Double.toString(companyWorkload);
            if(!CommonUtils.isNumber(company_workload)){
                throw new FdevException(ErrorConstants.EXPECT_OUT_WORK_ERROE2);
            }
        }
    }

    @Override
    public List<Map> queryFdevUnitByUnitNos(List<String> unitNos) {
        return implementUnitDao.queryFdevUnitByUnitNos(unitNos);
    }

    @Override
    public Map<String, Object> queryFdevUnitList(Map<String, Object> params) throws Exception {

        Map dateType = (Map)params.get(Dict.DATETYPE1);//延期类型
        String dateTypePlan = "" ;
        String dateTypeReal = "" ;
        if(!CommonUtils.isNullOrEmpty(dateType)){
            dateTypePlan = (String)dateType.get(Dict.DATETYPEPLAN);//延期类型 计划
            dateTypeReal = (String)dateType.get(Dict.DATETYPEREAL);//延期类型：实际
        }
        List<String> groupIds = (List<String>)params.get(Dict.GROUPIDS);//所属小组
        String keyword = (String)params.get(Dict.KEYWORD);//研发单元内容/编号模糊搜索
        String demandType = (String)params.get(Dict.DEMANDTYPE);//需求类型：tech-科技需求；business-业务需求，daily-日常需求
        String demandKey = (String)params.get(Dict.DEMANDKEY);//需求名称/编号  模糊搜索
        String ipmpUnitNo = (String)params.get(Dict.IPMPUNITNO);//实施单元编号  模糊搜索
        String otherDemandTaskNum = (String)params.get(Dict.OTHERDEMANDTASKNUM);//其他需求任务编号 模糊搜索
        List<String> userIds = (List<String>)params.get(Dict.USERIDS);//牵头人
        List<String> states = (List<String>)params.get(Dict.STATES);//状态
        Integer size = (Integer)params.get(Dict.SIZE);//每页条数，不传默认查全部
        Integer index = (Integer)params.get(Dict.INDEX);//页码
        String groupQueryType = (String)params.get(Dict.GROUPQUERYTYPE);//是否查询子组 0 本组 1 本组子组
        //小组
        List<String> groupAll = new ArrayList<String>();
        if ( "1".equals(groupQueryType) ) {
            if ( !CommonUtils.isNullOrEmpty(groupIds) ) {
                groupAll.addAll(groupUtil.getGroupByParent(groupIds));
                groupIds = groupAll;
            }
        }
        List<String> demandIds = new ArrayList();
        List<DemandBaseInfo> demandBaseInfos = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(demandKey)){
            demandBaseInfos = demandBaseInfoDao.queryDemandBaseList(demandKey);
            if(!CommonUtils.isNullOrEmpty(demandBaseInfos)){
                for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
                    demandIds.add(demandBaseInfo.getId());
                }
            }
        }
        Map<String,Object> map = implementUnitDao.queryFdevUnitList(dateTypePlan,dateTypeReal,groupIds,keyword,demandType,demandKey,demandIds,ipmpUnitNo
                ,otherDemandTaskNum,userIds,states,size,index);
        if(!CommonUtils.isNullOrEmpty(demandBaseInfos)){
            List<FdevImplementUnit> data = (List<FdevImplementUnit>) map.get(Dict.DATA);
            if(!CommonUtils.isNullOrEmpty(data)){
                for (FdevImplementUnit fdevImplementUnit : data) {
                    for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
                        //获取需求名称 编号
                        if(fdevImplementUnit.getDemand_id().equals(demandBaseInfo.getId())){
                            fdevImplementUnit.setDemand_no(demandBaseInfo.getOa_contact_no());
                            fdevImplementUnit.setDemand_name(demandBaseInfo.getOa_contact_name());
                            break;
                        }
                    }
                    //获取小组名
                    Map<String, Object> group = roleService.queryGroup(fdevImplementUnit.getGroup());
                    if(!CommonUtils.isNullOrEmpty(group)) {
                        fdevImplementUnit.setGroup_cn((String) group.get(Dict.GROUP_NAME));
                    }
                    String ipmp_unit_no = fdevImplementUnit.getIpmp_implement_unit_no();
                    if(!Dict.DAILY.equals(fdevImplementUnit.getDemand_type())){
                        fdevImplementUnit.setHave_link(0);
                    }
                    if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)) {
                        //查询研发单元所属实施单元是否可跳转
                        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
                        if (!CommonUtils.isNullOrEmpty(ipmpUnit)) {
                            fdevImplementUnit.setHave_link(1);//实施单元是否已同步至fdev，1已同步，0未同步
                        }
                    }
                    String create_user = fdevImplementUnit.getCreate_user();
                    if(CommonUtils.isNullOrEmpty(fdevImplementUnit.getCreate_user_all())){
                        Map<String, Object> createUserInfo = roleService.queryUserbyid(create_user);
                        UserInfo create_user_all = new UserInfo();
                        create_user_all.setId(create_user);
                        if(!CommonUtils.isNullOrEmpty(createUserInfo)){
                            create_user_all.setUser_name_en((String) createUserInfo.get(Dict.USER_NAME_EN));
                            create_user_all.setUser_name_cn((String) createUserInfo.get(Dict.USER_NAME_CN));
                        }
                        fdevImplementUnit.setCreate_user_all(create_user_all);
                    }
                    //添加延期标志
                    fdevImplementUnit.setDelayFlag(isDelay(fdevImplementUnit));
                }
            }
        }else{
            List<FdevImplementUnit> data = (List<FdevImplementUnit>) map.get(Dict.DATA);
            if(!CommonUtils.isNullOrEmpty(data)){
                for (FdevImplementUnit fdevImplementUnit : data) {
                    DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(fdevImplementUnit.getDemand_id());
                    //获取需求名称 编号
                    if(!CommonUtils.isNullOrEmpty(demandBaseInfo)){
                        fdevImplementUnit.setDemand_no(demandBaseInfo.getOa_contact_no());
                        fdevImplementUnit.setDemand_name(demandBaseInfo.getOa_contact_name());
                    }
                    //获取小组名
                    Map<String, Object> group = roleService.queryGroup(fdevImplementUnit.getGroup());
                    if(!CommonUtils.isNullOrEmpty(group)){
                        fdevImplementUnit.setGroup_cn((String)group.get(Dict.GROUP_NAME));
                    }
                    String ipmp_unit_no = fdevImplementUnit.getIpmp_implement_unit_no();
                    if(!Dict.DAILY.equals(fdevImplementUnit.getDemand_type())){
                        fdevImplementUnit.setHave_link(0);
                    }
                    if(!CommonUtils.isNullOrEmpty(ipmp_unit_no)) {
                        //查询研发单元所属实施单元是否可跳转
                        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmp_unit_no);
                        if (!CommonUtils.isNullOrEmpty(ipmpUnit)) {
                            fdevImplementUnit.setHave_link(1);//实施单元是否已同步至fdev，1已同步，0未同步
                        }
                    }
                    String create_user = fdevImplementUnit.getCreate_user();
                    if(CommonUtils.isNullOrEmpty(fdevImplementUnit.getCreate_user_all())){
                        Map<String, Object> createUserInfo = roleService.queryUserbyid(create_user);
                        UserInfo create_user_all = new UserInfo();
                        create_user_all.setId(create_user);
                        if(!CommonUtils.isNullOrEmpty(createUserInfo)){
                            create_user_all.setUser_name_en((String) createUserInfo.get(Dict.USER_NAME_EN));
                            create_user_all.setUser_name_cn((String) createUserInfo.get(Dict.USER_NAME_CN));
                        }
                        fdevImplementUnit.setCreate_user_all(create_user_all);
                    }
                    //添加延期标志
                    fdevImplementUnit.setDelayFlag(isDelay(fdevImplementUnit));
                }
            }
        }
        return map ;
    }

    //判断需求是否延期
    private Boolean isDelay(FdevImplementUnit fdevImplementUnit) throws ParseException {
        if(CommonUtils.isDelay(fdevImplementUnit.getPlan_start_date(), fdevImplementUnit.getReal_start_date())) return true;
        if(CommonUtils.isDelay(fdevImplementUnit.getPlan_inner_test_date(), fdevImplementUnit.getReal_inner_test_date())) return true;
        if(CommonUtils.isDelay(fdevImplementUnit.getPlan_test_date(), fdevImplementUnit.getReal_test_date())) return true;
        if(CommonUtils.isDelay(fdevImplementUnit.getPlan_test_finish_date(), fdevImplementUnit.getReal_test_finish_date())) return true;
        if(CommonUtils.isDelay(fdevImplementUnit.getPlan_product_date(), fdevImplementUnit.getReal_product_date())) return true;
        return false;
    }

    @Override
    public void exportFdevUnitList(Map<String, Object> params, HttpServletResponse resp) throws Exception {
        List<FdevImplementUnit> data = (List<FdevImplementUnit>)queryFdevUnitList(params).get(Dict.DATA);
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource=new ClassPathResource("FdevUnitExport.xlsx");
            inputStream=classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);

        } catch (Exception e1) {
            throw new FdevException("研发单元列表导出失败，请联系fdev管理员");
        }
        if(!CommonUtils.isNullOrEmpty(data)){
            int i=1;//行数
            for (FdevImplementUnit fdevUnit : data) {
                int j=0;//列数
                sheet.createRow(i);
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getDemand_name()) ? "":fdevUnit.getDemand_name());//需求名称
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getDemand_no()) ? "":fdevUnit.getDemand_no());//需求编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getFdev_implement_unit_no()) ? "":fdevUnit.getFdev_implement_unit_no());//研发单元编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getImplement_unit_content()) ? "":fdevUnit.getImplement_unit_content());//研发单元内容
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getIpmp_implement_unit_no()) ? "":fdevUnit.getIpmp_implement_unit_no());//IPMP实施单元编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getOther_demand_task_num()) ? "":fdevUnit.getOther_demand_task_num());//其他需求任务编号
                if(!CommonUtils.isNullOrEmpty(fdevUnit.getImplement_unit_status_special()) && 1 == fdevUnit.getImplement_unit_status_special()){
                    sheet.getRow(i).createCell(j++).setCellValue("暂缓");
                }else if(!CommonUtils.isNullOrEmpty(fdevUnit.getImplement_unit_status_special()) &&2 == fdevUnit.getImplement_unit_status_special()){
                    sheet.getRow(i).createCell(j++).setCellValue("恢复中");
                }else{
                    sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getImplement_unit_status_normal()) ? "": DemandBaseInfoUtil.changeStateCn(fdevUnit.getImplement_unit_status_normal()));//研发单元状态
                }
                if(!CommonUtils.isNullOrEmpty(fdevUnit.getImplement_leader_all())){
                    String user = "";
                    for(UserInfo userInfo : fdevUnit.getImplement_leader_all()){
                        if(CommonUtils.isNullOrEmpty(user)){
                            user = userInfo.getUser_name_cn();
                        }else{
                            user = user + "," + userInfo.getUser_name_cn();
                        }
                    }
                    sheet.getRow(i).createCell(j++).setCellValue(user);//研发单元牵头人
                }else{
                    sheet.getRow(i).createCell(j++).setCellValue("");
                }

                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getGroup_cn()) ? "":fdevUnit.getGroup_cn());//所属小组
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getPlan_start_date()) ? "":fdevUnit.getPlan_start_date());//计划启动开发日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getPlan_inner_test_date()) ? "":fdevUnit.getPlan_inner_test_date());//计划提交内测日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getPlan_test_date()) ? "":fdevUnit.getPlan_test_date());//计划提交用户测试日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getPlan_test_finish_date()) ? "":fdevUnit.getPlan_test_finish_date());//计划用户测试完成日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getPlan_product_date()) ? "":fdevUnit.getPlan_product_date());//计划投产日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getReal_start_date()) ? "":fdevUnit.getReal_start_date());//实际启动开发日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getReal_inner_test_date()) ? "":fdevUnit.getReal_inner_test_date());//实际提交内测日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getReal_test_date()) ? "":fdevUnit.getReal_test_date());//实际提交用户测试日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getReal_test_finish_date()) ? "":fdevUnit.getReal_test_finish_date());//实际用户测试完成日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getReal_product_date()) ? "":fdevUnit.getReal_product_date());//实际投产日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getDept_workload()) ? "":fdevUnit.getDept_workload().toString());//预期我部人员工作量
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getCompany_workload()) ? "":fdevUnit.getCompany_workload().toString());//预期公司人员工作量
                sheet.getRow(i).createCell(j++).setCellValue(fdevUnit.isUi_verify() ? "涉及" : "不涉及");//是否涉及UI审核
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getCreate_user_all()) ? "" : fdevUnit.getCreate_user_all().getUser_name_cn());//研发单元创建人
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdevUnit.getCreate_time()) ? "":fdevUnit.getCreate_time());//研发单元创建时间
                i++;
            }
        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "FdevUnitExport"+ ".xlsx");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
        }
    }

    @Override
    public void fdevUnitOverdueEmail(Map<String, Object> params) throws Exception {

        //获取计划提交业测前一天尚未提测的研发单元
        List<FdevImplementUnit> fdevUnitTestOverdueList = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(-1),Constants.FDEV_TEST,null);
        //获取提交业测已经延期 1天 的研发单元
        List<FdevImplementUnit> fdevUnitTestOverdueList1 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(1),Constants.FDEV_TEST,null);
        //获取提交业测已经延期 3天 的研发单元
        List<FdevImplementUnit> fdevUnitTestOverdueList3 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(3),Constants.FDEV_TEST,null);
        //获取提交业测已经延期 7天 的研发单元
        List<FdevImplementUnit> fdevUnitTestOverdueList7 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(7),Constants.FDEV_TEST,null);
        //获取提交业测已经延期 7天以上 的研发单元
        List<FdevImplementUnit> fdevUnitTestOverdueListGt7 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(7),Constants.FDEV_TEST,Constants.GT);
        //获取计划启动开发前一天尚未启动的研发单元
        List<FdevImplementUnit> fdevUnitStartOverdueList = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(-1),Constants.START,null);
        //获取启动开发已经延期 1天 的研发单元
        List<FdevImplementUnit> fdevUnitStartOverdueList1 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(1),Constants.START,null);
        //获取启动开发已经延期 3天 的研发单元
        List<FdevImplementUnit> fdevUnitStartOverdueList3 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(3),Constants.START,null);
        //获取启动开发已经延期 7天 的研发单元
        List<FdevImplementUnit> fdevUnitStartOverdueList7 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(7),Constants.START,null);
        //获取启动开发已经延期 7天以上 的研发单元
        List<FdevImplementUnit> fdevUnitStartOverdueListGt7 = implementUnitDao.queryFdevUnitOverdueList(TimeUtil.todayLastTwoWeek(7),Constants.START,Constants.GT);

        //全部研发单元集合
        List<FdevImplementUnit> fdevUnitList = new ArrayList<>();
        fdevUnitList.addAll(fdevUnitTestOverdueList);
        fdevUnitList.addAll(fdevUnitTestOverdueList1);
        fdevUnitList.addAll(fdevUnitTestOverdueList3);
        fdevUnitList.addAll(fdevUnitTestOverdueList7);
        fdevUnitList.addAll(fdevUnitTestOverdueListGt7);

        fdevUnitList.addAll(fdevUnitStartOverdueList);
        fdevUnitList.addAll(fdevUnitStartOverdueList1);
        fdevUnitList.addAll(fdevUnitStartOverdueList3);
        fdevUnitList.addAll(fdevUnitStartOverdueList7);
        fdevUnitList.addAll(fdevUnitStartOverdueListGt7);
        //全部需求id
        List<String> demandIds = new ArrayList<>();
        //全部用户ID
        Set<String> userIdSet = new HashSet<>();

        for (FdevImplementUnit fdevImplementUnit : fdevUnitList) {
            demandIds.add(fdevImplementUnit.getDemand_id());
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit.getImplement_leader())){
                userIdSet.addAll(fdevImplementUnit.getImplement_leader());
            }
        }
        //全部需求
        List<DemandBaseInfo> demandBaseInfos = demandBaseInfoDao.queryByIds(demandIds);

        for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
            if(!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_leader())){
                userIdSet.addAll(demandBaseInfo.getDemand_leader());
            }
        }
        //全量用户信息
        Map<String, Map> userMapAll = fdevUserService.queryByUserCoreData(userIdSet, null);

        //获取计划提交业测前一天尚未提测的研发单元
        sendFdevUnitOverdueEmail(fdevUnitTestOverdueList,"研发单元提测提醒","将在明日到达计划提交用户测试日期里程碑，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.FDEV_TEST) ;
        //获取提交业测已经延期 1天 的研发单元
        sendFdevUnitOverdueEmail(fdevUnitTestOverdueList1,"研发单元提测延期提醒","已超过计划提交用户测试日期1天，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.FDEV_TEST) ;
        //获取提交业测已经延期 3天 的研发单元
        sendFdevUnitOverdueEmail(fdevUnitTestOverdueList3,"研发单元提测延期提醒","已超过计划提交用户测试日期3天，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.FDEV_TEST) ;
        //获取提交业测已经延期 7天 的研发单元
        sendFdevUnitOverdueEmail(fdevUnitTestOverdueList7,"研发单元提测延期提醒","已超过计划提交用户测试日期7天，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.FDEV_TEST) ;
        //获取提交业测已经延期 7天以上 的研发单元 延期天数未定 方法里 获取emailContent
        sendFdevUnitOverdueEmail(fdevUnitTestOverdueListGt7,"研发单元提测延期提醒",""
                ,userMapAll,demandBaseInfos,Constants.FDEV_TEST) ;
        //获取计划启动开发前一天尚未启动的研发单元
        sendFdevUnitOverdueEmail(fdevUnitStartOverdueList,"研发单元启动提醒","将在明日到达计划启动开发日期里程碑，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.START) ;
        //获取启动开发已经延期 1天 的研发单元
        sendFdevUnitOverdueEmail(fdevUnitStartOverdueList1,"研发单元启动延期提醒","已超过计划启动开发日期1天，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.START) ;
        //获取启动开发已经延期 3天 的研发单元
        sendFdevUnitOverdueEmail(fdevUnitStartOverdueList3,"研发单元启动延期提醒","已超过计划启动开发日期3天，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.START) ;
        //获取启动开发已经延期 7天 的研发单元
        sendFdevUnitOverdueEmail(fdevUnitStartOverdueList7,"研发单元启动延期提醒","已超过计划启动开发日期7天，请及时推进!"
                ,userMapAll,demandBaseInfos,Constants.START) ;
        //获取启动开发已经延期 7天以上 的研发单元 延期天数未定 方法里 获取emailContent
        sendFdevUnitOverdueEmail(fdevUnitStartOverdueListGt7,"研发单元提测延期提醒",""
                ,userMapAll,demandBaseInfos,Constants.START) ;


    }

    //封装研发单元审批表信息
    public void sendFdevUnitOverdueEmail(List<FdevImplementUnit> fdevUnitOverdueList, String titleHead,String emailContent
                                          ,Map<String, Map> userMapAll,List<DemandBaseInfo> demandBaseInfos ,String type) throws Exception {
        if(!CommonUtils.isNullOrEmpty(fdevUnitOverdueList)){
            for (FdevImplementUnit fdevImplementUnit : fdevUnitOverdueList ) {
                String emailContentDay = emailContent;
                List<String> emailList = new ArrayList<>();

                String fdevUnitNo = fdevImplementUnit.getFdev_implement_unit_no();
                String fdevUnitName = fdevImplementUnit.getImplement_unit_content();
                String demandNo = fdevImplementUnit.getImplement_unit_content();
                String demandName = fdevImplementUnit.getImplement_unit_content();
                String demandId = fdevImplementUnit.getDemand_id();

                //获取邮箱
                HashSet<String> implLeaderSet = fdevImplementUnit.getImplement_leader();
                if( !CommonUtils.isNullOrEmpty(implLeaderSet) ){
                    for (String implLeaderId : implLeaderSet) {
                        if (!CommonUtils.isNullOrEmpty(userMapAll.get(implLeaderId))) {
                            Map userMap = userMapAll.get(implLeaderId);
                            emailList.add((String) userMap.get(Dict.EMAIL));
                        }
                    }
                }
                //需求信息
                for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
                    //获取当前需求
                    if(demandId.equals(demandBaseInfo.getId())){
                        demandNo = demandBaseInfo.getOa_contact_no();
                        demandName = demandBaseInfo.getOa_contact_name();
                        //需求牵头人邮箱
                        HashSet<String> demandLeaderSet = demandBaseInfo.getDemand_leader();
                        if( !CommonUtils.isNullOrEmpty(demandLeaderSet) ){
                            for (String demandLeaderId : demandLeaderSet) {
                                if (!CommonUtils.isNullOrEmpty(userMapAll.get(demandLeaderId))) {
                                    Map userMap = userMapAll.get(demandLeaderId);
                                    emailList.add((String) userMap.get(Dict.EMAIL));
                                }
                            }
                        }
                        break;
                    }
                }
                //邮件内容为空 超期时间需要判断
                if(CommonUtils.isNullOrEmpty(emailContent)){
                    if( Constants.FDEV_TEST.equals(type) ){
                        //提交用户测试延期天数
                        long day = CommonUtils.countDay(TimeUtil.formatToday(), fdevImplementUnit.getPlan_test_date());
                        emailContentDay = "已超过计划提交用户测试日期"+ day +"天，请及时推进!";
                    }else{//启动开发延期
                        //启动开发延期延期天数
                        long day = CommonUtils.countDay(TimeUtil.formatToday(), fdevImplementUnit.getPlan_start_date());
                        emailContentDay = "已超过计划启动开发日期"+ day +"天，请及时推进!";
                    }
                }
                sendEmailDemandService.sendEmailFdevUnitOverdue(emailList,titleHead,fdevUnitNo,fdevUnitName,
                        demandNo,demandName,demandId,emailContentDay);
            }
        }
    }

}
