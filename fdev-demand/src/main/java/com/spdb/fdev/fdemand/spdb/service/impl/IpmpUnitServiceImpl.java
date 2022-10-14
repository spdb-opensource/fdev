package com.spdb.fdev.fdemand.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.*;
import com.spdb.fdev.fdemand.base.utils.*;
import com.spdb.fdev.fdemand.spdb.dao.*;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.fdemand.spdb.unit.DealTaskUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class IpmpUnitServiceImpl implements IIpmpUnitService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private IIpmpUnitDao ipmpUnitDao;

    @Autowired
    private IIpmpUtilsService ipmpUtilsService;

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private DealTaskUnit dealTaskUnit;

    @Autowired
    private SendEmailDemandService sendEmailDemandService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    IIpmpUnitUpdateDao ipmpUnitUpdateDao;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    IpmpUtils ipmpUtils;

    @Autowired
    ICommonBusinessService commonBusinessService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    ILogService logService;

    @Autowired
    private IOtherDemandTaskService otherDemandTaskService;

    @Autowired
    private IOtherDemandTaskDao otherDemandTaskDao;

    @Autowired
    ILogDao logDao;

    @Autowired
    private IDemandAssessService DemandAssessService;

    @Autowired
    private GroupUtil groupUtil;

    @Autowired
    private IIpmpProjectDao ipmpProjectDao;

    @Autowired
    private ITestOrderService testOrderService;

    @Autowired
    private IImplementUnitService implementUnitService;

    @Value("${fdev.ipmp.url}")
    private String ipmpUrl;
    @Value("${ipmp.increment.appno}")
    private String appno;
    @Value("${ipmp.increment.method}")
    private String method;
    @Value("${ipmp.increment.version}")
    private String version;
    @Value("${ipmp.increment.isAuth}")
    private boolean isAuth;
    @Value("${ipmp.increment.appKey}")
    private String appKey;

    @Value("${no.sync.unit.projectNo}")
    private List<String> noSyncPrjNum;
    @Value("${ipmp.unit.url}")
    private String ipmpUnitUrl;

    @Value("${ban.status.type:已投产,已撤销,暂缓,暂存}")
    private List<String> banStatusType;

    @Value("${xtest.api.getTestFinishDate}")
    private String getTestFinishDate;

    @Value("${xtest.api.getTestFinishDateByUpdateTime}")
    private String getTestFinishDateByUpdateTime;

    @Value("${fdemand.doc.folder}")
    private String docFolder;

    @Autowired
    private DictDao dictDao;

    @Resource
    FdocmanageService fdocmanageService;

    @Override
    public Map<String, Object> queryIpmpUnitByDemandId(Map<String, Object> params) throws Exception {
        User user = CommonUtils.getSessionUser();

        DemandBaseInfo demandBaseInfo = new DemandBaseInfo();
        if(CommonUtils.isNullOrEmpty(params.get(Dict.INFORMATIONNUM))){//需求编号
            String demandId = (String)params.get(Dict.DEMANDID);//需求ID
            //查询需求
            demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(demandId);
            params.put(Dict.INFORMATIONNUM,demandBaseInfo.getOa_contact_no());//需求编号
        }else{
            //查询需求
            demandBaseInfo = demandBaseInfoDao.queryByOaContactNo((String) params.get(Dict.INFORMATIONNUM));
        }
        String demandType = demandBaseInfo.getDemand_type();//需求类型
        Map<String, Object> ipmpMap = new HashMap<>();
        //科技需求查询 年度实施单元列表
        if(Constants.TECH.equals(demandType)){
            //科技需求详情页 查询研发单元关联的实施单元列表
            if( Constants.TECH.equals(params.get(Dict.ISTECH)) ){
                //获取所有研发单元
                List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryAllFdevUnitByDemandId(demandBaseInfo.getId());

                if(!CommonUtils.isNullOrEmpty(fdevUnitList)){
                    Set implUnitNumList = new HashSet();
                    for(FdevImplementUnit fdevUnit : fdevUnitList){
                        implUnitNumList.add(fdevUnit.getIpmp_implement_unit_no());
                    }
                    if(!CommonUtils.isNullOrEmpty(implUnitNumList)){
                        params.put(Dict.IMPLUNITNUMLIST,implUnitNumList);
                        ipmpMap = ipmpUnitDao.queryIpmpUnitByNums(params);
                    }
                }
            }else{
                ipmpMap = ipmpUnitDao.queryAllTechIpmpUnit(params);
            }

        }else {
            ipmpMap = ipmpUnitDao.queryIpmpUnitAllByDemandNum(params);
        }
        List<IpmpUnit> unitList = (List<IpmpUnit>)ipmpMap.get(Dict.DATA);
        //是否必填 默认不必填
        Boolean disabledTip = false ;
        if(!CommonUtils.isNullOrEmpty(unitList)){
            for(IpmpUnit ipmpUnit : unitList){
                String leaderGroup = ipmpUnit.getLeaderGroup();
                //牵头小组
                if(!CommonUtils.isNullOrEmpty(leaderGroup)){
                    Map<String, Object> groupMap = roleService.queryGroup(leaderGroup);
                    if(!CommonUtils.isNullOrEmpty(groupMap)){
                        ipmpUnit.setLeaderGroupName((String) groupMap.get(Dict.NAME));
                    }
                }
                //科技需求列表页 或业务需求查询用户信息
                if( Constants.TECH.equals(params.get(Dict.ISTECH)) || Constants.BUSINESS.equals(demandType)){
                    String implLeader = ipmpUnit.getImplLeader();//牵头人
                    String implLeaderName = ipmpUnit.getImplLeaderName();//牵头人中文名
                    List<UserInfo> implLeaderInfoList = new ArrayList<>();
                    if(!CommonUtils.isNullOrEmpty(implLeader)){
                        List<String>  implLeaders = Arrays.asList(implLeader.split(","));
                        List<String>  implLeaderNames = Arrays.asList(implLeaderName.split(","));
                        Set<String> implLeaderSet = new HashSet<>();
                        implLeaderSet.addAll(implLeaders);
                        //查询牵头人信息
                        Map<String,Map> userMapAll = fdevUserService.queryByUserCoreData(null,implLeaderSet);
                        int i = 0 ;
                        for (String implLeaderEn : implLeaders) {
                            UserInfo userInfo = new UserInfo();
                            if(!CommonUtils.isNullOrEmpty(userMapAll.get(implLeaderEn))){
                                Map userMap =  userMapAll.get(implLeaderEn);
                                userInfo.setId((String) userMap.get(Dict.ID));
                                userInfo.setUser_name_en((String) userMap.get(Dict.USER_NAME_EN));
                                userInfo.setUser_name_cn((String) userMap.get(Dict.USER_NAME_CN));
                            }else {
                                userInfo.setId("");
                                userInfo.setUser_name_en(implLeaderEn);
                                userInfo.setUser_name_cn(implLeaderNames.get(i));
                            }
                            implLeaderInfoList.add(userInfo);
                            i++;
                        }
                        ipmpUnit.setImplLeaderInfo(implLeaderInfoList);
                    }
                }
                //校验是否展示 向IPMP同步按钮
                isShowSync(ipmpUnit,user);

                if(!Constants.TECH.equals(demandType)){
                    //判断实施单元是否可选
                    if(!CommonUtils.isNullOrEmpty(ipmpUnit.getUsedSysCode()) && "ZH-0748".equals(ipmpUnit.getUsedSysCode())
                            && "'stockUnit'".equals(ipmpUnit.getUsedSysCode())) {
                        ipmpUnit.setUnitText("该实施单元不属于fdev平台，不允许选择");
                    } else if ("3".equals(ipmpUnit.getLeaderFlag())){
                        ipmpUnit.setUnitText("该实施单元牵头人不为fdev用户, 不允许选择");
                    } else if (banStatusType.contains(ipmpUnit.getImplStatusName())){
                        ipmpUnit.setUnitText("已投产、撤销，暂缓 ，暂存的实施单元不得被选择");
                    } else if (CommonUtils.isNullOrEmpty(ipmpUnit.getLeaderGroup())
                            ||CommonUtils.isNullOrEmpty(ipmpUnit.getPlanInnerTestDate())) {
                        ipmpUnit.setUnitText("请前往实施单元详情页补全实施单元牵头小组、预计提交内测时间！");
                        //默认不必填 只要有一个 满足此条件 则必填
                        disabledTip = true ;
                    }else {
                        //正常实施单元 必填
                        disabledTip = true ;
                    }
                }

                String prjNum = ipmpUnit.getPrjNum();//项目编号
                String projectType = ipmpUnit.getProjectType();//项目类型
                //项目编号不为空 项目类型为空 查询项目类型
                if(!CommonUtils.isNullOrEmpty(prjNum) && CommonUtils.isNullOrEmpty(projectType)){
                    List<IpmpProject> ipmpProjects = ipmpProjectDao.queryIpmpProject(prjNum);
                    if(!CommonUtils.isNullOrEmpty(ipmpProjects)){
                        IpmpProject ipmpProject = ipmpProjects.get(0);
                        ipmpUnit.setProjectType(ipmpProject.getProject_type());
                    }
                }
            }
        }

        ipmpMap.put(Dict.DATA,CommonUtils.isNullOrEmpty(unitList) ? new ArrayList<>() : unitList );
        ipmpMap.put(Dict.DEMAND_TYPE,demandType);
        //是否必填 科技 必填  业务则判断
        if(Constants.TECH.equals(demandType)){
            ipmpMap.put(Dict.DISABLEDTIP, true );
        }else {
            ipmpMap.put(Dict.DISABLEDTIP, disabledTip );
        }
        return ipmpMap;
    }

    //校验是否展示 向IPMP同步按钮
    public void isShowSync( IpmpUnit ipmpUnit , User user ) throws Exception {
        ipmpUnit.setIsShowSync(false);//默认false
        //实施单元编号是否包含科技 并且 业测结束  日期不为空
        if(!ipmpUnit.getImplUnitNum().contains("科技") &&
               !CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestFinishDate())){
            List<IpmpUnitOperateLog> ipmpUnitOperateLogs = logDao.queryIpmpUnitOperateLog(ipmpUnit.getImplUnitNum());
            //日志不为空最后一次日志的错误码为30  且为该实施单元牵头人 展示同步按钮
            if( !CommonUtils.isNullOrEmpty(ipmpUnitOperateLogs) &&
                    "30".equals(ipmpUnitOperateLogs.get(0).getErrorCode()) &&
                    roleService.isIpmpUnitLeader(ipmpUnit, user.getUser_name_en()) )
                ipmpUnit.setIsShowSync(true);
        }
    }

    @Override
    public IpmpUnit queryIpmpUnitById(Map<String, Object> params) throws Exception {
        String ipmpUnitNo = (String)params.get(Dict.IMPLUNITNUM);//实施单元编号
        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnitNo);
        String implLeader = ipmpUnit.getImplLeader();//牵头人
        String implLeaderName = ipmpUnit.getImplLeaderName();//牵头人中文名
        List<UserInfo> implLeaderInfoList = new ArrayList<>();
        String leaderGroup = ipmpUnit.getLeaderGroup();
        //牵头小组
        if(!CommonUtils.isNullOrEmpty(leaderGroup)){
            Map<String, Object> groupMap = roleService.queryGroup(leaderGroup);
            if(!CommonUtils.isNullOrEmpty(groupMap)){
                ipmpUnit.setLeaderGroupName((String) groupMap.get(Dict.NAME));
            }
        }
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            List<String>  implLeaders = Arrays.asList(implLeader.split(","));
            List<String>  implLeaderNames = Arrays.asList(implLeaderName.split(","));
            Set<String> implLeaderSet = new HashSet<>();
            implLeaderSet.addAll(implLeaders);
            //查询牵头人信息
            Map<String,Map> userMapAll = fdevUserService.queryByUserCoreData(null,implLeaderSet);
            int i = 0 ;
            for (String implLeaderEn : implLeaders) {
                UserInfo userInfo = new UserInfo();
                if(!CommonUtils.isNullOrEmpty(userMapAll.get(implLeaderEn))){
                    Map userMap =  userMapAll.get(implLeaderEn);
                    userInfo.setId((String) userMap.get(Dict.ID));
                    userInfo.setUser_name_en((String) userMap.get(Dict.USER_NAME_EN));
                    userInfo.setUser_name_cn((String) userMap.get(Dict.USER_NAME_CN));
                }else {
                    userInfo.setId("");
                    userInfo.setUser_name_en(implLeaderEn);
                    userInfo.setUser_name_cn(implLeaderNames.get(i));
                }
                implLeaderInfoList.add(userInfo);
                i++;
            }
            ipmpUnit.setImplLeaderInfo(implLeaderInfoList);
        }
        String prjNum = ipmpUnit.getPrjNum();//项目编号
        String projectType = ipmpUnit.getProjectType();//项目类型
        //项目编号不为空 项目类型为空 查询项目类型
        if(!CommonUtils.isNullOrEmpty(prjNum) && CommonUtils.isNullOrEmpty(projectType)){
            List<IpmpProject> ipmpProjects = ipmpProjectDao.queryIpmpProject(prjNum);
            if(!CommonUtils.isNullOrEmpty(ipmpProjects)){
                IpmpProject ipmpProject = ipmpProjects.get(0);
                ipmpUnit.setProjectType(ipmpProject.getProject_type());
            }
        }
        //封装页面确认延期展示选项（延期的）
        ipmpUnit.setConfirmDelayItem(getConfirmDelayItem(ipmpUnit));
        //实施计划变更原因分类翻译为中文
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getImplChangeType())){
            ipmpUnit.setImplChangeTypeName(getDictValueByCodes(ipmpUnit.getImplChangeType()));

        }
        return ipmpUnit;
    }

    /**
     * 返回页面需要展示的确认延期选项（计划调整日期不为空 且 不等于对应的计划日期）
     * @param ipmpUnit
     * @return
     */
    public List<String> getConfirmDelayItem(IpmpUnit ipmpUnit) throws ParseException {
        List<String> result = new ArrayList<>();
        String planDevelopDate = ipmpUnit.getPlanDevelopDate();//计划启动开发日期
        String planTestStartDate = ipmpUnit.getPlanTestStartDate();//计划提交用户测试日期
        String planTestFinishDate = ipmpUnit.getPlanTestFinishDate();//计划用户测试完成日期
        String planProductDate = ipmpUnit.getPlanProductDate();//计划投产日期
        String acturalDevelopDate = ipmpUnit.getActuralDevelopDate();//实际启动开发日期
        String acturalTestStartDate = ipmpUnit.getActuralTestStartDate();//实际提交用户测试日期
        String acturalTestFinishDate = ipmpUnit.getActuralTestFinishDate();//实际用户测试完成日期
        String acturalProductDate = ipmpUnit.getActuralProductDate();//实际投产日期
        if(CommonUtils.isDelay(planDevelopDate,acturalDevelopDate)) result.add(Dict.DEVELOPDELAY);//开发延期
        if(CommonUtils.isDelay(planTestStartDate,acturalTestStartDate)) result.add(Dict.TESTSTARTDELAY);//提测延期
        if(CommonUtils.isDelay(planTestFinishDate,acturalTestFinishDate)) result.add(Dict.TESTFINISHDELAY);//完成测试延期
        if(CommonUtils.isDelay(planProductDate,acturalProductDate)) result.add(Dict.PRODUCTDELAY);//投产延期

        return result;
    }

    @Override
    public Map<String,String> updateIpmpUnit(Map<String,Object> params) throws Exception {

        String ipmpUnitNo = (String) params.get(Dict.IMPLUNITNUM);//实施单元编号
        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnitNo);
        IpmpUnit oldIpmpUnit = new IpmpUnit();
        BeanUtils.copyProperties(ipmpUnit,oldIpmpUnit);
        //获取人员信息 校验权限
        User user = CommonUtils.getSessionUser();
        boolean isDemandManager = roleService.isDemandManager();//是否需求管理员
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(ipmpUnit, user.getUser_name_en());//是否为该实施单元牵头人
        if ( !isDemandManager && !isIpmpUnitLeader) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }
        //非FDEV实施单元不可编辑
        if("3".equals(ipmpUnit.getLeaderFlag()) || (!CommonUtils.isNullOrEmpty(ipmpUnit.getUsedSysCode())
                && !ipmpUnit.getUsedSysCode().equals(appno) &&
                !ipmpUnit.getUsedSysCode().equals("stockUnit"))){
            throw new FdevException(ErrorConstants.IPMP_ERROR_NOT_UPDATE);
        }

        String ipmpStatusName = ipmpUnit.getImplStatusName();
        if( "暂缓".equals(ipmpStatusName) || "已撤销".equals(ipmpStatusName) || "暂存".equals(ipmpStatusName) ){
            throw new FdevException(ErrorConstants.IPMP_ERROR_CANNOT_UPDATE);
        }
        /*String testLeaderName = (String) params.get(Dict.TESTLEADERNAME);//测试牵头人中文姓名
        String testLeader = (String) params.get(Dict.TESTLEADER);//测试牵头人域账号
        String testLeaderEmail = (String) params.get(Dict.TESTLEADEREMAIL);//测试牵头人邮箱
        //判断测试牵头人长度 不为空判断不能大于 10
        if( !CommonUtils.isNullOrEmpty(testLeader) && testLeader.split(",").length > 10 ){
            throw new FdevException(ErrorConstants.IPMP_ERROR_TEST_LEADER);
        }*/


        String syncFlag = (String) params.get(Dict.SYNCFLAG);//是否同步IPMP标识 1 为同步
        String implDelayTypeName = (String) params.get(Dict.IMPLDELAYTYPENAME);//实施延期原因分类
        String implDelayType = (String) params.get(Dict.IMPLDELAYTYPE);//实施延期原因分类Type
        String implDelayReason = (String) params.get(Dict.IMPLDELAYREASON);//实施延期原因
        String implLeader = (String) params.get(Dict.IMPLLEADER);//实施牵头人域账号
        String implLeaderName = (String) params.get(Dict.IMPLLEADERNAME);//实施牵头人中文姓名
        String headerUnitName = (String) params.get(Dict.HEADERUNITNAME);//牵头单位
        String headerTeamName = (String) params.get(Dict.HEADERTEAMNAME);//实施牵头团队
        String prjNum = (String) params.get(Dict.PRJNUM);//项目编号
        String planPrjName = (String) params.get(Dict.PLANPRJNAME);//拟纳入项目名称
        String projectType = (String) params.get(Dict.PROJECTTYPE);//项目类型
        String cloudFlag = (String) params.get(Dict.CLOUDFLAG);//是否上云
        String cloudFlagName = (String) params.get(Dict.CLOUDFLAGNAME);//是否上云转译
        String techSchemeKey = (String) params.get(Dict.TECHSCHEMEKEY);//技术方案编号id
        String techSchemeNo = (String) params.get(Dict.TECHSCHEMENO);//技术方案编号
        String checkerUserIds = (String) params.get(Dict.CHECKERUSERIDS);//审核人
        String checkerUserNames = (String) params.get(Dict.CHECKERUSERNAMES);//审核人姓名

        Double expectOwnWorkload = null ;
        if(!CommonUtils.isNullOrEmpty(params.get(Dict.EXPECTOWNWORKLOAD))){
            expectOwnWorkload = Double.valueOf(params.get(Dict.EXPECTOWNWORKLOAD).toString()) ;//预期行内人员工作量
        }
        Double expectOutWorkload = null ;
        if(!CommonUtils.isNullOrEmpty(params.get(Dict.EXPECTOUTWORKLOAD))){
            expectOutWorkload = Double.valueOf(params.get(Dict.EXPECTOUTWORKLOAD).toString()) ;//预期公司人员工作量
        }
        String leaderGroup = (String) params.get(Dict.LEADERGROUP);//牵头小组
        String planInnerTestDate = (String) params.get(Dict.PLANINNERTESTDATE);//计划提交内测时间
        /*if(CommonUtils.isNullOrEmpty(testLeader)) {
            testLeaderName = implLeaderName ;//测试牵头人为空  默认实施单元牵头人
            testLeader = implLeader ;//测试牵头人为空  默认实施单元牵头人
        }*/
        //判断牵头人长度 不为空判断不能大于 10
        if( !CommonUtils.isNullOrEmpty(implLeader) && implLeader.split(",").length > 10 ){
            throw new FdevException(ErrorConstants.IPMP_ERROR_LEADER);
        }
        //牵头小组 计划内测不为空 且原来字段至少其一为空 创建工单
        if(!CommonUtils.isNullOrEmpty(leaderGroup) && !CommonUtils.isNullOrEmpty(planInnerTestDate)
                &&(CommonUtils.isNullOrEmpty(ipmpUnit.getLeaderGroup()) || CommonUtils.isNullOrEmpty(ipmpUnit.getPlanInnerTestDate()))){
            commonBusinessService.createYuhengOrder(ipmpUnit.getId(), planInnerTestDate , ipmpUnit.getPlanTestStartDate(),
                    ipmpUnit.getPlanProductDate(), ipmpUnit.getImplContent() + "创建工单", leaderGroup ,"");

        }else if(!CommonUtils.isNullOrEmpty(leaderGroup) && !CommonUtils.isNullOrEmpty(planInnerTestDate) &&
                (!leaderGroup.equals(ipmpUnit.getLeaderGroup()) || !planInnerTestDate.equals(ipmpUnit.getPlanInnerTestDate()))){
            //改动 更新工单
            commonBusinessService.updateYuhengPlanDate(ipmpUnit.getId(), planInnerTestDate,
                    ipmpUnit.getPlanTestStartDate(), ipmpUnit.getPlanProductDate());
        }
        ipmpUnit.setLeaderGroup(leaderGroup);
        ipmpUnit.setPlanInnerTestDate(planInnerTestDate);

        //同步IPMP仅能同步一次的字段 且未同步过
        if( Constants.ONE.equals(syncFlag) && !syncFlag.equals(ipmpUnit.getSyncFlag())){
            //判断同步IPMP字段均不能为空
            if( !CommonUtils.isNullOrEmpty(implLeader) && !CommonUtils.isNullOrEmpty(headerTeamName) &&
                    !CommonUtils.isNullOrEmpty(prjNum) && !CommonUtils.isNullOrEmpty(planPrjName) &&
                    !CommonUtils.isNullOrEmpty(expectOwnWorkload) && !CommonUtils.isNullOrEmpty(expectOutWorkload) ){
                //仅能同步IPMP一次的字段
                params.put(Dict.USERTYPE,CommonUtils.isInt(user.getEmail()) ? "0" : "1");
                params.put(Dict.USER_NAME_EN,user.getUser_name_en());
                ipmpUtilsService.updateImplUnitOther(params);
            }else{
                throw new FdevException(ErrorConstants.IPMP_ERROR_SYNC_NULL);
            }
            ipmpUnit.setSyncFlag(syncFlag);
            ipmpUnitDao.updateIpmpUnit(ipmpUnit);
            //通过需求编号，修改需求的评估完成日期，若需求状态是1，则将当前日期填充至评估完成日期，并计算评估时长。若需求状态是2或9，则跳过。
            DemandAssessService.updateAssessOver(ipmpUnit.getInformationNum());

        }
        Map<String, String> updateImplUnitMap = new HashMap<>();
        //判断实施单元是否延期
        if( implIsDelay(ipmpUnit) ){
            if( !CommonUtils.isNullOrEmpty(implDelayType) && !CommonUtils.isNullOrEmpty(implDelayReason) ){
                //实施延期原因分类、实施延期原因有修改 同步至 IPMP
                updateImplUnitMap.put(Dict.IMPLDELAYTYPE,implDelayType);
                updateImplUnitMap.put(Dict.IMPLDELAYREASON,implDelayReason);
            }else{
                if(!ipmpUnit.getUsedSysCode().equals("stockUnit")){
                    throw new FdevException(ErrorConstants.IPMP_ERROR_DELAY);
                }
            }
        }
        //同步IPMP 实际日期 测试牵头人
       /* updateImplUnitMap.put(Dict.TESTLEADERNAME,testLeaderName);//测试牵头人中文姓名
        updateImplUnitMap.put(Dict.TESTLEADER,testLeader);//测试牵头人域账号
        updateImplUnitMap.put(Dict.TESTLEADEREMAIL,testLeaderEmail);//测试牵头人邮箱*/
        updateImplUnitMap.put(Dict.ACTURALDEVELOPDATE,ipmpUnit.getActuralDevelopDate());
        updateImplUnitMap.put(Dict.ACTURALTESTSTARTDATE,ipmpUnit.getActuralTestStartDate());
        updateImplUnitMap.put(Dict.ACTURALTESTFINISHDATE,ipmpUnit.getActuralTestFinishDate());
        updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,ipmpUnit.getActuralProductDate());
        updateImplUnitMap.put(Dict.IMPLUNITNUM,ipmpUnit.getImplUnitNum());
        updateImplUnitMap.put(Dict.USERTYPE,CommonUtils.isInt(user.getEmail()) ? "0" : "1");
        updateImplUnitMap.put(Dict.USER_NAME_EN,user.getUser_name_en());
        Map<String,String> returnMap = new HashMap();
        returnMap.put(Dict.IMPLUNITNUM,ipmpUnitNo);
        //不是存量的实施单元才发IPMP接口
        if(!Constants.STOCKUNIT.equals(ipmpUnit.getUsedSysCode())){
            //如果投产日期延期且未确认延期，则不同步实施单元实际投产日期
            if(this.isDelayNoConfirm(ipmpUnit)){
                updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,"");
            }
            Map map = ipmpUtilsService.updateImplUnit(updateImplUnitMap);
            Map map2 = new HashMap();//第二次请求IPMP结果
            if("30".equals(map.get(Dict.ERRORCODE)) /*&& "0".equals(map.get(Dict.STATUS))*/){
                updateImplUnitMap.put(Dict.ACTURALTESTFINISHDATE,null);
                updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,null);
                map2 = ipmpUtilsService.updateImplUnit(updateImplUnitMap);
            }
            //第一次为 未核算 第二次为成功  前端弹窗 数据正常入库
            if( "30".equals(map.get(Dict.ERRORCODE)) && "0".equals(map2.get(Dict.STATUS)) ){
                returnMap.put(Dict.URL,ipmpUnitUrl);
                returnMap.put(Dict.ERRORCODE,"30");
            }else if("30".equals(map.get(Dict.ERRORCODE)) && "30".equals(map2.get(Dict.ERRORCODE)) /* && "1".equals(map.get(Dict.STATUS))*/){
                //判断是否 均为未核算报错 直接返回不入库 前端弹窗
                returnMap.put(Dict.URL,ipmpUnitUrl);
                returnMap.put(Dict.ERRORCODE,"30");
                return returnMap;
            }
        }
        updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,ipmpUnit.getActuralProductDate());
        //判断是否上云是否改动 为FDEV维护的 切不为待定 有改动则请求IPMP接口
        if( !CommonUtils.isNullOrEmpty(cloudFlag) && !cloudFlag.equals(ipmpUnit.getCloudFlag())
            && ( Constants.ONE.equals(syncFlag) || Constants.ONE.equals(ipmpUnit.getSyncFlag())) ){
            //请求IPMP接口 更新指定实施单元云标识和技术方案
            ipmpUtilsService.updateCloudData(ipmpUnitNo,cloudFlag,techSchemeKey,prjNum,checkerUserIds
                    ,user.getUser_name_en(),CommonUtils.isInt(user.getEmail()) ? "0" : "1");
            //是 则转换为审核中
            if("implunit.cloud.flag.01".equals(cloudFlag)){
                cloudFlag = "implunit.cloud.flag.04";
                cloudFlagName = "审核中" ;
            }
        }
        ipmpUnit.setImplLeader(implLeader);
        ipmpUnit.setImplLeaderName(implLeaderName);
        ipmpUnit.setHeaderUnitName(headerUnitName);
        ipmpUnit.setHeaderTeamName(headerTeamName);
        /*ipmpUnit.setTestLeaderName(testLeaderName);
        ipmpUnit.setTestLeaderEmail(testLeaderEmail);
        ipmpUnit.setTestLeader(testLeader);*/
        ipmpUnit.setPrjNum(prjNum);
        ipmpUnit.setProjectType(projectType);
        ipmpUnit.setPlanPrjName(planPrjName);
        ipmpUnit.setExpectOwnWorkload(expectOwnWorkload);
        ipmpUnit.setExpectOutWorkload(expectOutWorkload);
        ipmpUnit.setImplDelayTypeName(implDelayTypeName);
        ipmpUnit.setImplDelayType(implDelayType);
        ipmpUnit.setImplDelayReason(implDelayReason);
        ipmpUnit.setLeaderGroup(leaderGroup);
        ipmpUnit.setPlanInnerTestDate(planInnerTestDate);
        ipmpUnit.setUnitDevMode("implunit.dev.mode.02");//研发模式默认稳态
        ipmpUnit.setCloudFlag(cloudFlag);
        ipmpUnit.setCloudFlagName(cloudFlagName);
        ipmpUnit.setTechSchemeKey(techSchemeKey);
        ipmpUnit.setTechSchemeNo(techSchemeNo);
        ipmpUnit.setCheckerUserIds(checkerUserIds);
        ipmpUnit.setCheckerUserNames(checkerUserNames);
        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
        //记录实体日志 updateType  syncNew = 同步新增 syncUpdate = 同步修改 ,fdevUpdate = 页面修改
        logService.saveIpmpUnitEntityLog(ipmpUnit,oldIpmpUnit,"fdevUpdate");
        //更新 需求计划内测时间
        if(!CommonUtils.isNullOrEmpty(planInnerTestDate)){
            //查询需求
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum());
            demandBaseInfo.setPlan_inner_test_date(CommonUtils.getJudgementDate(planInnerTestDate,demandBaseInfo.getPlan_inner_test_date())
                    && !CommonUtils.isNullOrEmpty(demandBaseInfo.getPlan_inner_test_date()) ? demandBaseInfo.getPlan_inner_test_date() : planInnerTestDate) ;
            demandBaseInfoDao.updateDemandBaseInfo(demandBaseInfo);
        }
        return returnMap;
    }

    @Override
    public Map<String,String> syncIpmpUnit(Map<String,Object> params) throws Exception {

        String ipmpUnitNo = (String) params.get(Dict.IMPLUNITNUM);//实施单元编号
        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnitNo);
        //获取人员信息 校验权限
        User user = CommonUtils.getSessionUser();
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(ipmpUnit, user.getUser_name_en());//是否为该实施单元牵头人
        if ( !isIpmpUnitLeader ) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }

        String implDelayType = ipmpUnit.getImplDelayType();//实施延期原因分类Type
        String implDelayReason = ipmpUnit.getImplDelayReason();//实施延期原因
        Map<String, String> updateImplUnitMap = new HashMap<>();
        //判断实施单元是否延期
        if( implIsDelay(ipmpUnit) ){
            if( !CommonUtils.isNullOrEmpty(implDelayType) && !CommonUtils.isNullOrEmpty(implDelayReason) ){
                //实施延期原因分类、实施延期原因有修改 同步至 IPMP
                updateImplUnitMap.put(Dict.IMPLDELAYTYPE,implDelayType);
                updateImplUnitMap.put(Dict.IMPLDELAYREASON,implDelayReason);
            }else{
                if(!ipmpUnit.getUsedSysCode().equals("stockUnit")){
                    throw new FdevException(ErrorConstants.IPMP_ERROR_DELAY);
                }
            }
        }
        //同步IPMP 实际日期 测试牵头人
        updateImplUnitMap.put(Dict.TESTLEADERNAME,ipmpUnit.getTestLeaderName());//测试牵头人中文姓名
        updateImplUnitMap.put(Dict.TESTLEADER,ipmpUnit.getTestLeader());//测试牵头人域账号
        updateImplUnitMap.put(Dict.TESTLEADEREMAIL,ipmpUnit.getTestLeaderEmail());//测试牵头人邮箱
        updateImplUnitMap.put(Dict.ACTURALDEVELOPDATE,ipmpUnit.getActuralDevelopDate());
        updateImplUnitMap.put(Dict.ACTURALTESTSTARTDATE,ipmpUnit.getActuralTestStartDate());
        updateImplUnitMap.put(Dict.ACTURALTESTFINISHDATE,ipmpUnit.getActuralTestFinishDate());
        updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,ipmpUnit.getActuralProductDate());
        updateImplUnitMap.put(Dict.IMPLUNITNUM,ipmpUnit.getImplUnitNum());
        updateImplUnitMap.put(Dict.USERTYPE,CommonUtils.isInt(user.getEmail()) ? "0" : "1");
        updateImplUnitMap.put(Dict.USER_NAME_EN,user.getUser_name_en());

        Map<String,String> returnMap = new HashMap();
        //不是存量的实施单元才发IPMP接口
        if(!Constants.STOCKUNIT.equals(ipmpUnit.getUsedSysCode())){
            Map map = ipmpUtilsService.updateImplUnit(updateImplUnitMap);
            returnMap.put(Dict.IMPLUNITNUM,ipmpUnitNo);
            //判断是否 未核算报错 前端弹窗
            if("30".equals(map.get(Dict.ERRORCODE))){
                returnMap.put(Dict.URL,ipmpUnitUrl);
                returnMap.put(Dict.ERRORCODE,"30");
                return returnMap;
            }
        }
        return returnMap;
    }


    @Override
    public void sendUserMountDevUnit(Map<String, Object> params) throws Exception {
        //查询没有挂载实施单元的研发单元
        List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryFdevImplementUnit();

        if(!CommonUtils.isNullOrEmpty(fdevUnitList)){
            for(FdevImplementUnit fdevUnit : fdevUnitList){
                String demandId = fdevUnit.getDemand_id();
                DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
                if(!CommonUtils.isNullOrEmpty(demandBaseInfo)){
                    String informationNum = demandBaseInfo.getOa_contact_no();//需求编号
                    List<IpmpUnit> ipmpUnitList = ipmpUnitDao.queryIpmpUnitByDemandId(informationNum);
                    //实施单元是否为空
                    if(!CommonUtils.isNullOrEmpty(ipmpUnitList)){
                        //不为空发送邮件添加待办
                        sendEmailDemandService.sendUserMountDevUnit(fdevUnit,demandBaseInfo);
                    }
                }
            }
        }
    }

    @Override
    public List<Map> queryTaskByIpmpUnitNo(Map<String, Object> params) throws Exception {
        String implUnitNum = (String) params.get(Dict.IMPLUNITNUM);//实施单元编号
        String taskNum  = (String) params.get(Dict.TASKNUM );//其他需求任务编号
        //根据实施单元编号查询研发单元
        List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryByImplUnitNum(implUnitNum,taskNum);
        //所有研发单元编号
        List<String> fdevUnitNoList = new ArrayList<>();
        for(FdevImplementUnit fdevUnit : fdevUnitList)
            fdevUnitNoList.add(fdevUnit.getFdev_implement_unit_no());

        return  taskService.queryDetailByUnitNo(fdevUnitNoList);
    }

    @Override
    public void syncAllIpmpInfo(Map<String, Object> params) throws Exception {
        //1、从ipmpUnit_update表获取时间参数
        String updateDate = "";
        String updateTextDate = "";
        IpmpUnitUpdate ipmpunitUpdate = ipmpUnitUpdateDao.queryNewInfo();
        if(!CommonUtils.isNullOrEmpty(ipmpunitUpdate)){
            updateDate = ipmpunitUpdate.getUpdateDate();
            updateTextDate = ipmpunitUpdate.getUpdateTextDate();
        }else{
            updateDate = TimeUtil.yesterday(1);
            updateTextDate = TimeUtil.yesterday(1);
        }
        //2、发接口获取批量更新数据
        List result = null;
        Map param = null;
        //需求编号
        Set<String> informationNumSet = new HashSet<>();
        //新增的实施单元
        List<IpmpUnit> newIpmpUnitList = new ArrayList<>();
        //全部实施单元列表（修改后）
        List<IpmpUnit> allIpmpUnitList = new ArrayList<>();
        //全部实施单元列表（修改前）
        List<IpmpUnit> allIpmpUnitListNoFix = new ArrayList<>();

        try {
            Map data = this.queryIpmpUtilFromIpmp(updateDate, updateTextDate);
            result = (List) data.get(Dict.RESULT);
            param = (Map)data.get(Dict.PARAM);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"IPMP服务故障！"+e.getMessage()});
        }
            if(!CommonUtils.isNullOrEmpty(result)){
                List<IpmpUnit> ipmpUnitList = new ArrayList<>();//同步过来的实施单元列表
                Set<String> implLeaderSet = new HashSet<>();//英文名集合
                for(int i = 0;i < result.size();i++) {
                    if (!CommonUtils.isNullOrEmpty(result.get(i))) {
                        IpmpUnit ipmpUnit = JSONObject.toJavaObject((JSONObject) result.get(i), IpmpUnit.class);
                        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getImplUnitNum())){
                            ipmpUnit.setImplUnitNum(ipmpUnit.getImplUnitNum().trim());
                            //不为空
                            if(!CommonUtils.isNullOrEmpty(ipmpUnit.getInformationNum())){
                                //实施单元编号是否包含科技
                                if(!ipmpUnit.getImplUnitNum().contains("科技")){
                                    //不包含新增需求
                                    informationNumSet.add(ipmpUnit.getInformationNum().trim());//需求编号
                                }
                                //去空格
                                ipmpUnit.setInformationNum(ipmpUnit.getInformationNum().trim());
                            }
                            if( CommonUtils.isNullOrEmpty(noSyncPrjNum) || !noSyncPrjNum.contains(ipmpUnit.getPrjNum()) ){
                                ipmpUnitList.add(ipmpUnit);
                                String implLeader = ipmpUnit.getImplLeader();//牵头人
                                if(!CommonUtils.isNullOrEmpty(implLeader)) {
                                    List<String> implLeaders = Arrays.asList(implLeader.split(","));
                                    implLeaderSet.addAll(implLeaders);
                                }
                            }
                        }
                    }
                }
                //查询牵头人信息
                Map<String,Map> userMap = fdevUserService.queryByUserCoreData(null,implLeaderSet);
                for(IpmpUnit ipmpUnit : ipmpUnitList) {
                    //判断该条是否存在
                    IpmpUnit unit = ipmpUnitDao.queryIpmpUnitById(ipmpUnit.getImplUnitNum());
                    IpmpUnit oldUnit = new IpmpUnit();
                    BeanUtils.copyProperties(ipmpUnit,oldUnit);
                    //校验是否是IPMP存量实施单元 存量的不可编辑
                    checkImplUnit(ipmpUnit);
                    //存在则更新
                    if(!CommonUtils.isNullOrEmpty(unit)){
                        getIpmpUnit(ipmpUnit, unit);
                        //牵头人标识
                        queryLeaderFlag( unit , userMap ,ipmpUnit);
                        //去掉日期后面多余 00:00:00 保留格式 yyyy-MM-dd
                        dealDate(unit);
                        //若计划提交内测日期不符合规则，则默认将计划提交内测日期调整为调整后的计划提交业测日期
                        String planDevelopDate = unit.getPlanDevelopDate();
                        String planInnerTestDate = unit.getPlanInnerTestDate();
                        String planTestStartDate = unit.getPlanTestStartDate();
                        if(!CommonUtils.getJudgementDate(planDevelopDate,planInnerTestDate)
                                || !CommonUtils.getJudgementDate(planInnerTestDate,planTestStartDate)){
                            unit.setPlanInnerTestDate(unit.getPlanTestStartDate());
                        }
                        // 内测时间不为空 判断提交用户测试日期 计划投产日期是否有修改
                        if(!CommonUtils.isNullOrEmpty(unit.getLeaderGroup()) && !CommonUtils.isNullOrEmpty(planInnerTestDate) &&
                                ((!CommonUtils.isNullOrEmpty(planTestStartDate) && !planTestStartDate.equals(oldUnit.getPlanTestStartDate()))
                                        || (!CommonUtils.isNullOrEmpty(unit.getPlanProductDate()) && !unit.getPlanProductDate().equals(oldUnit.getPlanProductDate())))){
                            //改动 更新工单
                            commonBusinessService.updateYuhengPlanDate(unit.getId(), unit.getPlanInnerTestDate(),
                                    unit.getPlanTestStartDate(), unit.getPlanProductDate());
                        }

                        ipmpUnitDao.updateIpmpUnit(unit);
                        //记录实体日志 updateType   syncNew = 同步新增 syncUpdate = 同步修改 ,fdevUpdate = 页面修改
                        logService.saveIpmpUnitEntityLog(unit,oldUnit,"syncUpdate");
                        allIpmpUnitList.add(unit);
                    }else{
                        //牵头人标识
                        queryLeaderFlag( ipmpUnit , userMap ,ipmpUnit);
                        ObjectId objectId = new ObjectId();
                        String id = objectId.toString();
                        ipmpUnit.set_id(objectId);
                        ipmpUnit.setId("IPMP_" + id);
                        //新增
                        //去掉日期后面多余 00:00:00 保留格式 yyyy-MM-dd
                        dealDate(ipmpUnit);

                        allIpmpUnitList.add(ipmpUnit);
                        newIpmpUnitList.add(ipmpUnit);
                        //记录实体日志 updateType  syncNew = 同步新增 syncUpdate = 同步修改 ,fdevUpdate = 页面修改
                        logService.saveIpmpUnitEntityLog(ipmpUnit,null,"syncNew");
                    }
                }

                ipmpUnitDao.saveIpmpUnit(newIpmpUnitList);
                //所有本次同步涉及的需求
                for (String informationNum : informationNumSet) {
                    //查询需求
                    DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryByOaContactNo(informationNum);
                    //查询实施单元
                    Map<String, Object> informationNumMap = new HashMap<>();
                    informationNumMap.put(Dict.INFORMATIONNUM,informationNum);
                    List<IpmpUnit> unitList = (List<IpmpUnit>)ipmpUnitDao.queryIpmpUnitAllByDemandNum(informationNumMap).get(Dict.DATA);
                    if(!CommonUtils.isNullOrEmpty(unitList)){
                        //存量的需求不同步实施单元
                        if(!CommonUtils.isNullOrEmpty(demandBaseInfo) &&
                                (!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())
                                || DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue().equals(demandBaseInfo.getDemand_status_normal())) ){
                            updateDemand(demandBaseInfo,unitList, userMap);
                            //更新需求的涉及小组评估状态
                            implementUnitService.updateDemandAssessStatus( demandBaseInfo );
                            demandBaseInfoDao.updateDemandBaseInfo(demandBaseInfo);
                            //更新需求实际日期 状态
                            updateDemandStatus(demandBaseInfo);
                        }else if (CommonUtils.isNullOrEmpty(demandBaseInfo)){
                            demandBaseInfo = new DemandBaseInfo();
                            //新增需求
                            saveDemand(demandBaseInfo,unitList,userMap);
                            if (CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_leader())) {
                                //需求牵头人为空 需求不入库并删除实施单元
                                ipmpUnitDao.removeIpmpUnitByDemandNum(demandBaseInfo.getOa_contact_no());
                            }else{
                                demandBaseInfoDao.save(demandBaseInfo);
                                //更新需求实际日期 状态
                                updateDemandStatus(demandBaseInfo);
                                //发送待办邮件
                                sendEmailDemandService.sendEmailUpdateDemand(demandBaseInfo);
                            }

                        } else {
                            //存量需求 删除实施单元
                            ipmpUnitDao.removeIpmpUnitByDemandNum(demandBaseInfo.getOa_contact_no());
                        }
                    }
                }
                //判断本次实施单元是否存在延期
                for(IpmpUnit ipmpUnit :allIpmpUnitList){
                    //实施单元编号是否包含科技,暂缓和已撤销的不发送邮件
                    if(!ipmpUnit.getImplUnitNum().contains("科技") && !IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName().equals(ipmpUnit.getImplStatusName()) && !IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName().equals(ipmpUnit.getImplStatusName())){
                        //判断是否延期(暂缓和已撤销的实施单元不发送延期邮件)
                        if( implIsDelayEmail(ipmpUnit) ){
                            //发送延期邮件，投产延期不发送延期邮件
                            sendEmailDemandService.sendEmailImplDelay(ipmpUnit,demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum()),userMap);
                        }

                    }
                }
                for(IpmpUnit ipmpUnit : ipmpUnitList){
                    //实施单元编号是否包含科技,暂缓和已撤销的不发送邮件
                    if(!ipmpUnit.getImplUnitNum().contains("科技") && !IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName().equals(ipmpUnit.getImplStatusName()) && !IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName().equals(ipmpUnit.getImplStatusName())){
                        //实施单元若已经从延期状态调整为不延期，则将实际投产日期同步到ipmp
                        if(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralProductDate()) && !CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestFinishDate())){
                            IpmpUnit fdevUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnit.getImplUnitNum());
                            if(!CommonUtils.isNullOrEmpty(fdevUnit)){
                                if(CommonUtils.getJudgementDate(fdevUnit.getActuralProductDate(),fdevUnit.getPlanProductDate())){
                                    updateImplUnit(fdevUnit,null);
                                }
                            }
                        }
                    }

                }

            }

        //更新时间表
        IpmpUnitUpdate ipmpUnitUpdateNew = new IpmpUnitUpdate();
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.UPDATEDATE)) &&  !CommonUtils.isNullOrEmpty(param.get(Dict.UPDATETEXTDATE))){
            ipmpUnitUpdateNew.setUpdateDate((String) param.get(Dict.UPDATEDATE));
            ipmpUnitUpdateNew.setUpdateTextDate((String) param.get(Dict.UPDATETEXTDATE));
        }else {
            ipmpUnitUpdateNew.setUpdateDate(updateDate);
            ipmpUnitUpdateNew.setUpdateTextDate(updateTextDate);
        }
        ipmpUnitUpdateDao.save(ipmpUnitUpdateNew);
    }

    /**
     * 判断是否调整过计划日期
     * @return
     */
    public Boolean haveAdjustDate(IpmpUnit ipmpUnit){
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanDevelopDateAdjust()) || !CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDateAdjust())
                || !CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDateAdjust()) || !CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDateAdjust())){

            return true;
        }
        return false;
    }

    //校验是否是IPMP存量实施单元 存量的不可编辑
    public void checkImplUnit(IpmpUnit ipmpUnit){
        String prjNum = ipmpUnit.getPrjNum();//项目编号
        String usedSysCode = ipmpUnit.getUsedSysCode();//流水线标识 为空或等于 ZH-0748 可编辑
        //项目编号不为空 流水线标识为空 存量实施单元 赋予特殊标识 FDEV不可编辑
        if(!CommonUtils.isNullOrEmpty(prjNum) && CommonUtils.isNullOrEmpty(usedSysCode)){
            ipmpUnit.setUsedSysCode("stockUnit");
        }
    }

    //去掉日期后面多余 00:00:00 保留格式 yyyy-MM-dd
    public void dealDate(IpmpUnit ipmpUnit){
        //不为空截取时间
        ipmpUnit.setPlanDevelopDate(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanDevelopDate()) || ipmpUnit.getPlanDevelopDate().length() < 10 ? "" : ipmpUnit.getPlanDevelopDate().substring(0,10));
        ipmpUnit.setPlanTestStartDate(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDate()) || ipmpUnit.getPlanTestStartDate().length() < 10 ? "" : ipmpUnit.getPlanTestStartDate().substring(0,10));
        ipmpUnit.setPlanTestFinishDate(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDate()) || ipmpUnit.getPlanTestFinishDate().length() < 10 ? "" : ipmpUnit.getPlanTestFinishDate().substring(0,10));
        ipmpUnit.setPlanProductDate(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDate()) || ipmpUnit.getPlanProductDate().length() < 10 ? "" : ipmpUnit.getPlanProductDate().substring(0,10));
        ipmpUnit.setActuralDevelopDate(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralDevelopDate()) || ipmpUnit.getActuralDevelopDate().length() < 10 ? "" : ipmpUnit.getActuralDevelopDate().substring(0,10));
        ipmpUnit.setActuralTestStartDate(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestStartDate()) || ipmpUnit.getActuralTestStartDate().length() < 10 ? "" : ipmpUnit.getActuralTestStartDate().substring(0,10));
        ipmpUnit.setActuralTestFinishDate(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestFinishDate()) || ipmpUnit.getActuralTestFinishDate().length() < 10 ? "" : ipmpUnit.getActuralTestFinishDate().substring(0,10));
        ipmpUnit.setActuralProductDate(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralProductDate()) || ipmpUnit.getActuralProductDate().length() < 10 ? "" : ipmpUnit.getActuralProductDate().substring(0,10));
        ipmpUnit.setReceiveDate(CommonUtils.isNullOrEmpty(ipmpUnit.getReceiveDate()) || ipmpUnit.getReceiveDate().length() < 10 ? "" : ipmpUnit.getReceiveDate().substring(0,10));
    }

    //新增需求
    public void saveDemand(DemandBaseInfo demandBaseInfo, List<IpmpUnit> unitList,Map<String,Map> userMap) throws Exception {

        String oaContactNo = demandBaseInfo.getOa_contact_no();//需求信息单编号
        String oaContactName = demandBaseInfo.getOa_contact_name();//需求信息单标题
        String demandNo = demandBaseInfo.getDemand_no();//需求书编号
        String demandInstruction = demandBaseInfo.getDemand_instruction();//需求书名称
        String proposeDemandDept = demandBaseInfo.getPropose_demand_dept();//需求提出部门

        Set<String> implTrackUserList = demandBaseInfo.getImpl_track_user();//实施单元跟踪人
        if(CommonUtils.isNullOrEmpty(implTrackUserList)){
            implTrackUserList = new HashSet<>();
        }
        String oaReceiveDate = demandBaseInfo.getOa_receive_date();//需求信息单我部收件日期
        String demandPlanName = demandBaseInfo.getDemand_plan_no();//需求计划名称
        String demandPlanNo = demandBaseInfo.getDemand_plan_no();//对应需求计划编号
//        String planStartDate = demandBaseInfo.getPlan_start_date();//计划启动开发日期
//        String planInnerTestDate = demandBaseInfo.getPlan_inner_test_date();//计划内测日期
//        String planTestDate = demandBaseInfo.getPlan_test_date();//计划提交用户测试日期
//        String planTestFinishDate = demandBaseInfo.getPlan_test_finish_date();//计划用户测试完成日期
//        String planProductDate = demandBaseInfo.getPlan_product_date();//计划投产日期

        Set<String> implLeaderSet = new HashSet<>();//英文名集合
        for(IpmpUnit ipmpUnit : unitList){
            String implLeader = ipmpUnit.getImplLeader();//牵头人
            if(!CommonUtils.isNullOrEmpty(implLeader)) {
                List<String> implLeaders = Arrays.asList(implLeader.split(","));
                implLeaderSet.addAll(implLeaders);
            }
            oaContactNo = ipmpUnit.getInformationNum();
            oaContactName = ipmpUnit.getInformationTitle();
            demandNo = ipmpUnit.getBookNum();
            demandInstruction = ipmpUnit.getBookName();
            proposeDemandDept = ipmpUnit.getInfoSubmitDept();
            oaReceiveDate = ipmpUnit.getReceiveDate();
            demandPlanName = ipmpUnit.getDpName();
            demandPlanNo = ipmpUnit.getDpNum();
            implTrackUserList.add(ipmpUnit.getUnitTrackerName());
//            planStartDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanDevelopDate(),planStartDate) && !CommonUtils.isNullOrEmpty(planStartDate) ? planStartDate : ipmpUnit.getPlanDevelopDate() ;
//            planInnerTestDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanInnerTestDate(),planInnerTestDate) && !CommonUtils.isNullOrEmpty(planInnerTestDate) ? planInnerTestDate : ipmpUnit.getPlanInnerTestDate() ;
//            planTestDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanTestStartDate(),planTestDate) && !CommonUtils.isNullOrEmpty(planTestDate) ? planTestDate : ipmpUnit.getPlanTestStartDate() ;
//            planTestFinishDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanTestFinishDate(),planTestFinishDate) && !CommonUtils.isNullOrEmpty(planTestFinishDate) ? planTestFinishDate :  ipmpUnit.getPlanTestFinishDate() ;
//            planProductDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanProductDate(),planProductDate) && !CommonUtils.isNullOrEmpty(planProductDate) ? planProductDate : ipmpUnit.getPlanProductDate() ;
        }
        //查询牵头人信息
        //Map<String,Map> userMap = fdevUserService.queryByUserCoreData(null,implLeaderSet);
        HashSet<String> userIds = new HashSet<>();
        for (String userEn: implLeaderSet) {
            if(!CommonUtils.isNullOrEmpty(userMap.get(userEn))){
                Map userInfo = userMap.get(userEn);
                userIds.add((String) userInfo.get(Dict.ID));
            }

        }
        demandBaseInfo.setDemand_leader(userIds);//需求总牵头负责人
        demandBaseInfo.setDemand_leader_all(userInfoList(implLeaderSet,userMap));//需求总牵头负责人信息
        demandBaseInfo.setOa_contact_no(oaContactNo);//需求信息单编号
        demandBaseInfo.setOa_contact_name(oaContactName);//需求信息单标题
        demandBaseInfo.setDemand_no(demandNo);//需求书编号
        demandBaseInfo.setDemand_instruction(demandInstruction);//需求书名称
        demandBaseInfo.setPropose_demand_dept(proposeDemandDept);//需求提出部门
        demandBaseInfo.setImpl_track_user(implTrackUserList);//实施单元跟踪人
        demandBaseInfo.setOa_receive_date(oaReceiveDate);//需求信息单我部收件日期
        demandBaseInfo.setDemand_plan_no(demandPlanName);//需求计划名称
        demandBaseInfo.setDemand_plan_no(demandPlanNo);//对应需求计划编号
//        demandBaseInfo.setPlan_start_date(planStartDate);//计划启动开发日期
//        demandBaseInfo.setPlan_inner_test_date(planInnerTestDate);//计划内测日期
//        demandBaseInfo.setPlan_test_date(planTestDate);//计划提交用户测试日期
//        demandBaseInfo.setPlan_test_finish_date(planTestFinishDate);//计划用户测试完成日期
//        demandBaseInfo.setPlan_product_date(planProductDate);//计划投产日期
        demandBaseInfo.setDemand_status_normal(DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue());//需求状态默认预评估
        demandBaseInfo.setDemand_type(Dict.BUSINESS);//需求类型
        demandBaseInfo.setDemand_flag(1);//需求标识
        //设置创建时间
        demandBaseInfo.setDemand_create_time(TimeUtil.getTimeStamp(new Date()));
    }

    //用户信息
    public List<UserInfo> userInfoList(Set<String> userIds, Map<String,Map> userMap) throws Exception {
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();
        if(!CommonUtils.isNullOrEmpty(userIds)){
            for(String userId : userIds){
                UserInfo userInfo = new UserInfo();
                if(!CommonUtils.isNullOrEmpty(userMap.get(userId))){
                    Map userMapInfo = userMap.get(userId);
                    userInfo.setId((String)userMapInfo.get(Dict.ID));
                    userInfo.setUser_name_en((String) userMapInfo.get(Dict.USER_NAME_EN));
                    userInfo.setUser_name_cn((String) userMapInfo.get(Dict.USER_NAME_CN));
                    userInfoList.add(userInfo);
                }
            }
        }
        return userInfoList;
    }

    //更新需求
    public void updateDemand(DemandBaseInfo demandBaseInfo, List<IpmpUnit> unitList,Map<String,Map> userMap) throws Exception {

        String oaContactNo = demandBaseInfo.getOa_contact_no();//需求信息单编号
        String oaContactName = demandBaseInfo.getOa_contact_name();//需求信息单标题
        String demandNo = demandBaseInfo.getDemand_no();//需求书编号
        String demandInstruction = demandBaseInfo.getDemand_instruction();//需求书名称
        String proposeDemandDept = demandBaseInfo.getPropose_demand_dept();//需求提出部门
        Set<String> implTrackUserList = demandBaseInfo.getImpl_track_user();//实施单元跟踪人
        if(CommonUtils.isNullOrEmpty(implTrackUserList)){
            implTrackUserList = new HashSet<>();
        }
        String oaReceiveDate = demandBaseInfo.getOa_receive_date();//需求信息单我部收件日期
        String demandPlanName = demandBaseInfo.getDemand_plan_no();//需求计划名称
        String demandPlanNo = demandBaseInfo.getDemand_plan_no();//对应需求计划编号
        //String planStartDate = demandBaseInfo.getPlan_start_date();//计划启动开发日期
        //String planInnerTestDate = demandBaseInfo.getPlan_inner_test_date();//计划内测日期
        //String planTestDate = demandBaseInfo.getPlan_test_date();//计划提交用户测试日期
        //String planTestFinishDate = demandBaseInfo.getPlan_test_finish_date();//计划用户测试完成日期
        //String planProductDate = demandBaseInfo.getPlan_product_date();//计划投产日期
        Set<String> implLeaderSet = new HashSet<>();//英文名集合
        for(IpmpUnit ipmpUnit : unitList){
            String implLeader = ipmpUnit.getImplLeader();//牵头人
            if(!CommonUtils.isNullOrEmpty(implLeader)) {
                List<String> implLeaders = Arrays.asList(implLeader.split(","));
                implLeaderSet.addAll(implLeaders);
            }

            oaContactNo = ipmpUnit.getInformationNum();
            oaContactName = ipmpUnit.getInformationTitle();
            demandNo = ipmpUnit.getBookNum();
            demandInstruction = ipmpUnit.getBookName();
            proposeDemandDept = ipmpUnit.getInfoSubmitDept();
            oaReceiveDate = ipmpUnit.getReceiveDate();
            demandPlanName = ipmpUnit.getDpName();
            demandPlanNo = ipmpUnit.getDpNum();
            implTrackUserList.add(ipmpUnit.getUnitTrackerName());
//            planStartDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanDevelopDate(),planStartDate) && !CommonUtils.isNullOrEmpty(planStartDate) ? planStartDate : ipmpUnit.getPlanDevelopDate() ;
//            planInnerTestDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanInnerTestDate(),planInnerTestDate) && !CommonUtils.isNullOrEmpty(planInnerTestDate) ? planInnerTestDate : ipmpUnit.getPlanInnerTestDate() ;
//            planTestDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanTestStartDate(),planTestDate) && !CommonUtils.isNullOrEmpty(planTestDate) ? planTestDate : ipmpUnit.getPlanTestStartDate() ;
//            planTestFinishDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanTestFinishDate(),planTestFinishDate) && !CommonUtils.isNullOrEmpty(planTestFinishDate) ? planTestFinishDate :  ipmpUnit.getPlanTestFinishDate() ;
//            planProductDate = CommonUtils.getJudgementDate(ipmpUnit.getPlanProductDate(),planProductDate) && !CommonUtils.isNullOrEmpty(planProductDate) ? planProductDate : ipmpUnit.getPlanProductDate() ;
        }
        Boolean leaderUpdateFlag = demandBaseInfo.getLeader_update_flag();//需求牵头人是否编辑
        //未编辑过才需更新 需求牵头人
        if( CommonUtils.isNullOrEmpty(leaderUpdateFlag) || !leaderUpdateFlag ){

            HashSet<String> demandLeader = demandBaseInfo.getDemand_leader();//需求总牵头负责人
            if (null == demandLeader)  demandLeader = new HashSet<>();
            //查询牵头人信息
            //Map<String,Map> userMap = fdevUserService.queryByUserCoreData(null,implLeaderSet);
            Set<String> userIds = new HashSet<>();
            Set<String> newUserEns = new HashSet<>();
            for (String userEn: implLeaderSet) {
                if(!CommonUtils.isNullOrEmpty(userMap.get(userEn))){
                    Map userInfo = userMap.get(userEn);
                    String userId = (String) userInfo.get(Dict.ID);
                    if(!demandLeader.contains(userId)){
                        //新的牵头人 id Set
                        userIds.add(userId);
                        //新的牵头人 英文名 Set
                        newUserEns.add(userEn);
                    }
                }
            }

            demandLeader.addAll(userIds);
            List<UserInfo> demandLeaderAll = demandBaseInfo.getDemand_leader_all();//需求总牵头负责人信息
            if (null == demandLeaderAll)  demandLeaderAll =new ArrayList<>();
            demandLeaderAll.addAll(userInfoList(newUserEns,userMap));

            demandBaseInfo.setDemand_leader(demandLeader);//需求总牵头负责人
            demandBaseInfo.setDemand_leader_all(demandLeaderAll);//需求总牵头负责人信息
        }

        demandBaseInfo.setOa_contact_no(oaContactNo);//需求信息单编号
        demandBaseInfo.setOa_contact_name(oaContactName);//需求信息单标题
        demandBaseInfo.setDemand_no(demandNo);//需求书编号
        demandBaseInfo.setDemand_instruction(demandInstruction);//需求书名称
        demandBaseInfo.setPropose_demand_dept(proposeDemandDept);//需求提出部门
        demandBaseInfo.setImpl_track_user(implTrackUserList);//实施单元跟踪人
        demandBaseInfo.setOa_receive_date(oaReceiveDate);//需求信息单我部收件日期
        demandBaseInfo.setDemand_plan_no(demandPlanName);//需求计划名称
        demandBaseInfo.setDemand_plan_no(demandPlanNo);//对应需求计划编号
//        demandBaseInfo.setPlan_start_date(planStartDate);//计划启动开发日期
//        demandBaseInfo.setPlan_inner_test_date(planInnerTestDate);//计划内测日期
//        demandBaseInfo.setPlan_test_date(planTestDate);//计划提交用户测试日期
//        demandBaseInfo.setPlan_test_finish_date(planTestFinishDate);//计划用户测试完成日期
//        demandBaseInfo.setPlan_product_date(planProductDate);//计划投产日期
        demandBaseInfo.setDemand_flag(1);//需求标识
    }


    //更新字段
    public void getIpmpUnit(IpmpUnit newIpmpUnit, IpmpUnit unit) throws Exception {
        unit.setPlanDevelopDate(newIpmpUnit.getPlanDevelopDate());//计划启动开发日期
        unit.setPlanTestStartDate(newIpmpUnit.getPlanTestStartDate());//计划提交用户测试日期
        unit.setPlanTestFinishDate(newIpmpUnit.getPlanTestFinishDate());//计划用户测试完成日期
        unit.setPlanProductDate(newIpmpUnit.getPlanProductDate());//计划投产日期
        if(!(("ZH-0748".equals(unit.getUsedSysCode()) || "ZH-0748".equals(newIpmpUnit.getUsedSysCode())) && Constants.ONE.equals(newIpmpUnit.getTestImplDeptDemdKey()))){
            if(!CommonUtils.isNullOrEmpty(newIpmpUnit.getActuralDevelopDate())) unit.setActuralDevelopDate( newIpmpUnit.getActuralDevelopDate() ) ;
            if(!CommonUtils.isNullOrEmpty(newIpmpUnit.getActuralTestStartDate())) unit.setActuralTestStartDate( newIpmpUnit.getActuralTestStartDate() );
            if(!CommonUtils.isNullOrEmpty(newIpmpUnit.getActuralTestFinishDate())) unit.setActuralTestFinishDate( newIpmpUnit.getActuralTestFinishDate() );
            if(!CommonUtils.isNullOrEmpty(newIpmpUnit.getActuralProductDate())) unit.setActuralProductDate( newIpmpUnit.getActuralProductDate() );
        }

        //判断实施单元状态是否为暂缓 撤销 暂存 并修改研发单元 任务状态
        checkImplStatus(newIpmpUnit,unit);
        //保存已IPMP为准的字段
        unit.setCloudFlag(newIpmpUnit.getCloudFlag());
        unit.setCloudFlagName(newIpmpUnit.getCloudFlagName());
        unit.setTechSchemeKey(newIpmpUnit.getTechSchemeKey());
        unit.setTechSchemeNo(newIpmpUnit.getTechSchemeNo());
        unit.setCheckerUserIds(newIpmpUnit.getCheckerUserIds());
        unit.setCheckerUserNames(newIpmpUnit.getCheckerUserNames());
        unit.setImplStatusName(newIpmpUnit.getImplStatusName());//实施状态
        unit.setImplContent(newIpmpUnit.getImplContent());//实施单元内容
        unit.setRelateSysName(newIpmpUnit.getRelateSysName());//涉及系统名称
        unit.setRelateSysCode(newIpmpUnit.getRelateSysCode());//涉及系统编号
        unit.setActualOwnUfworkload(newIpmpUnit.getActualOwnUfworkload());//行内人员实际工作量（非功能性）（人月）
        unit.setActualOutUfworkload(newIpmpUnit.getActualOutUfworkload());//公司人员实际工作量（非功能性）（人月）
        unit.setActualOwnFworkload(newIpmpUnit.getActualOwnFworkload());//行内人员实际工作量（功能性）（人月）
        unit.setActualOutFworkload(newIpmpUnit.getActualOutFworkload());//公司人员实际工作量（功能性）（人月）
        unit.setInformationTitle(newIpmpUnit.getInformationTitle());//需求信息单标题
        unit.setBookNum(newIpmpUnit.getBookNum());//需求书编号
        unit.setBookName(newIpmpUnit.getBookName());//需求书名称
        unit.setInfoSubmitDept(newIpmpUnit.getInfoSubmitDept());//需求提出部门
        unit.setUnitTrackerName(newIpmpUnit.getUnitTrackerName());//实施单元跟踪人
        unit.setReceiveDate(newIpmpUnit.getReceiveDate());//需求信息单我部收件日期
        unit.setDpName(newIpmpUnit.getDpName());//需求计划名称
        unit.setDpNum(newIpmpUnit.getDpNum());//对应需求计划编号
        unit.setTestImplDeptDemdKey(newIpmpUnit.getTestImplDeptDemdKey());//测试实施部门key
        unit.setTestImplDeptName(newIpmpUnit.getTestImplDeptName());//测试实施部门
        unit.setUpdateTextDate(newIpmpUnit.getUpdateTextDate());//实施单元同步时间
        unit.setCreateTime(newIpmpUnit.getCreateTime());//实施单元首次同步时间
        unit.setUnitSubmitDate(newIpmpUnit.getUnitSubmitDate());//实施单元提交时间（需求平台同步字段）
        String informationNum = unit.getInformationNum();
        //判断需求编号是否有修改有修改则更新
        if( !informationNum.equals(newIpmpUnit.getInformationNum()) ){
            //查询需求
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryByOaContactNo(informationNum);
            if(!CommonUtils.isNullOrEmpty(demandBaseInfo)){
                demandBaseInfo.setOa_contact_no(newIpmpUnit.getInformationNum());
                //更新需求
                demandBaseInfoDao.updateDemandBaseInfo(demandBaseInfo);
            }
            unit.setInformationNum(newIpmpUnit.getInformationNum());//需求信息单编号
        }
        unit.setUnitDevMode(newIpmpUnit.getUnitDevMode());//研发模式
        if(CommonUtils.isNullOrEmpty(unit.getUsedSysCode())){
            unit.setUsedSysCode(newIpmpUnit.getUsedSysCode());//流水线标识
        }
        //存量实施单元已IPMP为准
        if(Constants.STOCKUNIT.equals(newIpmpUnit.getUsedSysCode())){
            unit.setHeaderTeam(newIpmpUnit.getHeaderTeam());//实施牵头团队id
            unit.setHeaderUnit(newIpmpUnit.getHeaderUnit());//牵头单位id
            unit.setHeaderTeamName(newIpmpUnit.getHeaderTeamName());//实施牵头团队
            unit.setHeaderUnitName(newIpmpUnit.getHeaderUnitName());//牵头单位
            unit.setPrjNum(newIpmpUnit.getPrjNum());//项目编号
            unit.setProjectType(newIpmpUnit.getProjectType());//项目类型
            unit.setPlanPrjName(newIpmpUnit.getPlanPrjName());//拟纳入项目名称
            unit.setExpectOwnWorkload(newIpmpUnit.getExpectOwnWorkload());//预期行内人员工作量
            unit.setExpectOutWorkload(newIpmpUnit.getExpectOutWorkload());//预期公司人员工作量
        }
        unit.setImplDelayTypeName(newIpmpUnit.getImplDelayTypeName());//实施延期原因分类
        unit.setImplDelayReason(newIpmpUnit.getImplDelayReason());//实施延期原因
        unit.setTestLeader(CommonUtils.isNullOrEmpty(newIpmpUnit.getTestLeader()) ? "" : newIpmpUnit.getTestLeader());//测试牵头人域账号
        unit.setTestLeaderName(CommonUtils.isNullOrEmpty(newIpmpUnit.getTestLeaderName()) ? "" : newIpmpUnit.getTestLeaderName());//测试牵头人中文姓名
        unit.setTestLeaderEmail(CommonUtils.isNullOrEmpty(newIpmpUnit.getTestLeaderEmail())? "" : newIpmpUnit.getTestLeaderEmail());//测试牵头人邮箱
        //判断仅能同步一次的字段是否同步过
        if("1".equals(unit.getSyncFlag())){
            //同步过 已IPMP为准
            unit.setImplLeader(newIpmpUnit.getImplLeader());//实施牵头人域账号
            unit.setImplLeaderName(newIpmpUnit.getImplLeaderName());//实施牵头人中文姓名
            unit.setHeaderTeam(newIpmpUnit.getHeaderTeam());//实施牵头团队id
            unit.setHeaderUnit(newIpmpUnit.getHeaderUnit());//牵头单位id
            unit.setHeaderTeamName(newIpmpUnit.getHeaderTeamName());//实施牵头团队
            unit.setHeaderUnitName(newIpmpUnit.getHeaderUnitName());//牵头单位
            unit.setPrjNum(newIpmpUnit.getPrjNum());//项目编号
            unit.setProjectType(newIpmpUnit.getProjectType());//项目类型
            unit.setPlanPrjName(newIpmpUnit.getPlanPrjName());//拟纳入项目名称
            unit.setExpectOwnWorkload(newIpmpUnit.getExpectOwnWorkload());//预期行内人员工作量
            unit.setExpectOutWorkload(newIpmpUnit.getExpectOutWorkload());//预期公司人员工作量
        }else{
            //字段为空或存量实施单元已IPMP为准
            if(Constants.STOCKUNIT.equals(newIpmpUnit.getUsedSysCode()) || CommonUtils.isNullOrEmpty(unit.getImplLeader())){
                unit.setImplLeader(newIpmpUnit.getImplLeader());
            }else {
                //实施牵头人域账号
                String implLeader = unit.getImplLeader();//原牵头人
                String newIpmpLeader = newIpmpUnit.getImplLeader();//新牵头人
                if(!CommonUtils.isNullOrEmpty(newIpmpLeader)){
                    if(!implLeader.equals(newIpmpLeader)){
                        String[] newIpmpLeaders = newIpmpLeader.split(",");
                        for(String leader : newIpmpLeaders){
                            if(!implLeader.contains(leader))implLeader = implLeader + "," + leader;
                        }
                    }
                }
                unit.setImplLeader(implLeader);
            }
            //字段为空或存量实施单元已IPMP为准
            if(Constants.STOCKUNIT.equals(newIpmpUnit.getUsedSysCode()) || CommonUtils.isNullOrEmpty(unit.getImplLeaderName())){
                unit.setImplLeaderName(newIpmpUnit.getImplLeaderName());
            }else {
                //实施牵头人中文姓名
                String implLeaderName = unit.getImplLeaderName();//原牵头人
                String newIpmpLeaderName = newIpmpUnit.getImplLeaderName();//新牵头人
                if(!CommonUtils.isNullOrEmpty(newIpmpLeaderName)){
                    if(!implLeaderName.equals(newIpmpLeaderName)){
                        String[] newIpmpLeaderNames = newIpmpLeaderName.split(",");
                        for(String leaderName : newIpmpLeaderNames){
                            if(!implLeaderName.contains(leaderName))implLeaderName = implLeaderName + "," + leaderName;
                        }
                    }
                }
                unit.setImplLeaderName(implLeaderName);
            }

            if(CommonUtils.isNullOrEmpty(unit.getHeaderTeam())) unit.setHeaderTeam(newIpmpUnit.getHeaderTeam());//实施牵头团队id
            if(CommonUtils.isNullOrEmpty(unit.getHeaderUnit())) unit.setHeaderUnit(newIpmpUnit.getHeaderUnit());//牵头单位id
            if(CommonUtils.isNullOrEmpty(unit.getHeaderTeamName())) unit.setHeaderTeamName(newIpmpUnit.getHeaderTeamName());//实施牵头团队
            if(CommonUtils.isNullOrEmpty(unit.getHeaderUnitName())) unit.setHeaderUnitName(newIpmpUnit.getHeaderUnitName());//牵头单位
            if(CommonUtils.isNullOrEmpty(unit.getPrjNum())) unit.setPrjNum(newIpmpUnit.getPrjNum());//项目编号
            if(CommonUtils.isNullOrEmpty(unit.getProjectType())) unit.setProjectType(newIpmpUnit.getProjectType());//项目类型
            if(CommonUtils.isNullOrEmpty(unit.getPlanPrjName())) unit.setPlanPrjName(newIpmpUnit.getPlanPrjName());//拟纳入项目名称
            if(CommonUtils.isNullOrEmpty(unit.getExpectOwnWorkload())) unit.setExpectOwnWorkload(newIpmpUnit.getExpectOwnWorkload());//预期行内人员工作量
            if(CommonUtils.isNullOrEmpty(unit.getExpectOutWorkload())) unit.setExpectOutWorkload(newIpmpUnit.getExpectOutWorkload());//预期公司人员工作量
        }

    }

    //牵头人标识
    public void queryLeaderFlag(IpmpUnit unit,Map<String,Map> userMap,IpmpUnit newIpmpUnit) throws Exception {
        String implLeader = unit.getImplLeader();//牵头人
        String leaderFlag = "1"; //实施牵头人均是fdev：1；实施牵头人部分是fdev：2；实施牵头人均不是fdev：3
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            List<String>  implLeaders = Arrays.asList(implLeader.split(","));
            Set<String> implLeaderSet = new HashSet<>();
            implLeaderSet.addAll(implLeaders);
            //记录初始长度
            int initSize = implLeaderSet.size();
            Set<String> removeImplLeaderSet = new HashSet<>();
            //Map<String,Map> userMap = fdevUserService.queryByUserCoreData(null,implLeaderSet);
            if (!CommonUtils.isNullOrEmpty(userMap)) {
                for (String implLeaderEn : implLeaderSet) {
                    if(!CommonUtils.isNullOrEmpty(userMap.get(implLeaderEn))){
                        removeImplLeaderSet.add(implLeaderEn);
                    }
                }

            }
            //去掉是FDEV用户的牵头人 结束后的长度
            int endSize = initSize - removeImplLeaderSet.size();
            //长度相等 均不是FDEV用户
            if( initSize == endSize ) {
                //无在FDEV新增的牵头人标识为3 有则为2
                leaderFlag = implLeader.equals(newIpmpUnit.getImplLeader()) || "3".equals(unit.getLeaderFlag()) ? "3" : "2";
            }else if ( endSize < initSize && endSize != 0 ) //小于 且不为 0 部分是fdev用户
                leaderFlag = "2";
        }

        unit.setLeaderFlag(leaderFlag);
    }

    public Integer IpmpUnitConFdevUnitStatus(IpmpUnit newIpmpUnit){
        String implStatusName =  newIpmpUnit.getImplStatusName();
        //默认评估中
        Integer fdevUnitStatus = IpmpUnitEnum.IpmpUnitConFdevUnitStatus.EVALUATE.getValue();
        if(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.PRE_IMPLEMENT.getName().equals(implStatusName))
            fdevUnitStatus = IpmpUnitEnum.IpmpUnitConFdevUnitStatus.PRE_IMPLEMENT.getValue();
        if(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.DEVELOP.getName().equals(implStatusName))
            fdevUnitStatus = IpmpUnitEnum.IpmpUnitConFdevUnitStatus.DEVELOP.getValue();
        if(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.UAT.getName().equals(implStatusName))
            fdevUnitStatus = IpmpUnitEnum.IpmpUnitConFdevUnitStatus.UAT.getValue();
        if(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.REL.getName().equals(implStatusName))
            fdevUnitStatus = IpmpUnitEnum.IpmpUnitConFdevUnitStatus.REL.getValue();
        if(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.PRODUCT.getName().equals(implStatusName))
            fdevUnitStatus = IpmpUnitEnum.IpmpUnitConFdevUnitStatus.PRODUCT.getValue();
        return fdevUnitStatus;
    }

    public void checkImplStatus(IpmpUnit newIpmpUnit, IpmpUnit unit) throws Exception{
        //实施单元编号不包含科技 为业务实施单元
        if(!unit.getImplUnitNum().contains("科技")){
            String newImplStatus = newIpmpUnit.getImplStatusName();//新实施状态
            String oldImplStatus = unit.getImplStatusName();//旧实施状态
            List<String> status = new ArrayList<>();
            status.add(IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName());//暂存
            status.add(IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName());//暂缓
            status.add(IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName());//已撤销
            if( status.contains(newImplStatus) || status.contains(oldImplStatus)){

                if( !IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName().equals(newImplStatus) &&
                        IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName().equals(oldImplStatus)){
                    //新不为撤销 旧为撤销
                    //研发单元置为正常状态 任务置为恢复
                    updateSpectialStatus(unit,IpmpUnitConFdevUnitStatus(newIpmpUnit),"cancelAbort","1");
                }else if( IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName().equals(newImplStatus) &&
                        !IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName().equals(oldImplStatus)){
                    //新为撤销 旧不为撤销
                    //研发单元置为已撤销 任务置为已撤销
                    updateSpectialStatus(unit,ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue(),"abort","0");
                }else if((IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName().equals(newImplStatus)||
                        IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName().equals(newImplStatus))&&
                        !(IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName().equals(oldImplStatus)||
                                IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName().equals(oldImplStatus))){
                    //新为暂缓,暂存 旧不为暂缓 暂存
                    //研发单元置为暂缓 任务置为暂缓
                    updateSpectialStatus(unit,ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue(),"","1");
                }else if(!(IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName().equals(newImplStatus)||
                        IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName().equals(newImplStatus))&&
                        (IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName().equals(oldImplStatus)||
                                IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName().equals(oldImplStatus))){
                    //新不为暂缓,暂存 旧为暂缓 暂存
                    //研发单元置为恢复完成 任务置为正常
                    updateSpectialStatus(unit,IpmpUnitConFdevUnitStatus(newIpmpUnit),"","1");
                }
            }
        }
    }

    private void updateSpectialStatus(IpmpUnit unit, Integer status ,String stage, String taskSpectialStatus) throws Exception {
        //实施单元编号
        String implUnitNum = unit.getImplUnitNum();
        //根据实施单元编号查询研发单元
        List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryByImplUnitNum(implUnitNum,"");
        if(!CommonUtils.isNullOrEmpty(fdevUnitList)){
            for(FdevImplementUnit fdevUnit : fdevUnitList){
                if( ImplementUnitEnum.ImplementUnitDeferStatus.DEFER.getValue().equals(status) ){
                    //研发单元置为暂缓
                    fdevUnit.setImplement_unit_status_special(status);
                }else{
                    //研发单元置为已撤销或正常状态
                    fdevUnit.setImplement_unit_status_normal(status);
                    //研发单元置为恢复完成
                    fdevUnit.setImplement_unit_status_special(ImplementUnitEnum.ImplementUnitDeferStatus.FINISTH.getValue());
                    //不为撤销置为恢复中
                    if(!ImplementUnitEnum.ImplementUnitStatusEnum.IS_CANCELED.getValue().equals(status)){
                        //研发单元置为恢复中
                        fdevUnit.setImplement_unit_status_special(ImplementUnitEnum.ImplementUnitDeferStatus.RECOVER.getValue());
                        if(!CommonUtils.isNullOrEmpty(unit.getLeaderGroup()) && !CommonUtils.isNullOrEmpty(unit.getPlanInnerTestDate())){
                            //更新工单
                            commonBusinessService.updateYuhengPlanDate(unit.getId(), unit.getPlanInnerTestDate(),
                                    unit.getPlanTestStartDate(), unit.getPlanProductDate());
                        }
                    }
                }
                implementUnitDao.update(fdevUnit);
                //任务置为正常
                taskService.updateTaskSpectialStatus(fdevUnit.getFdev_implement_unit_no(),stage,taskSpectialStatus);
            }
        }
    }

    public Map queryIpmpUtilFromIpmp(String updateDate, String updateTextDate) throws Exception {
        Map common = new HashMap();
        Map request = new HashMap();
        request.put(Dict.UPDATEDATE,updateDate);
        request.put(Dict.UPDATETEXTDATE,updateTextDate);
        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(method,appKey));
        sendMap.put(Dict.REQUEST,request);
        Object body = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) body, Map.class);
        Map dataMap = (Map)map.get(Dict.DATA);
        return dataMap;
    }

    public Map getCommon(String ipmpMethod,String fdevAppkey) {
        Map common = new HashMap();
        common.put(Dict.APPNO,appno);
        common.put(Dict.METHOD,ipmpMethod);
        common.put(Dict.VERSION,version);
        common.put(Dict.ISAUTH,isAuth);
        common.put(Dict.APPKEY,fdevAppkey);
        return common;
    }

    @Override
    public void updateStatus(Map<String, Object> params)  throws Exception {
        String realStartDate = (String) params.get(Dict.REAL_START_DATE);//实际启动日期
        String realInnerTestDate = (String) params.get(Dict.REAL_INNER_TEST_DATE);//实际提交内测日期
        String realTestDate = (String) params.get(Dict.REAL_TEST_DATE);//实际提交业测日期
        String realTestFinishDate = (String) params.get(Dict.REAL_TEST_FINISH_DATE);//实际用户测试完成日期
        String realProductDate = (String) params.get(Dict.REAL_PRODUCT_DATE);//实际投产日期
        String taskStatus = (String) params.get(Dict.STAGE);//任务状态
        String fdevImplementUnitNo = (String) params.get(Dict.FDEV_IMPLEMENT_UNIT_NO);//研发单元编号
        String userNameEn = (String) params.get(Dict.USER_NAME_EN);//用户英文名
        if(!CommonUtils.getJudgementDate(realStartDate, realInnerTestDate)) {
            throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }else if(!CommonUtils.getJudgementDate(realInnerTestDate, realTestDate)) {
            throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }else if(!CommonUtils.getJudgementDate(realTestDate, realTestFinishDate)) {
            throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }else if(!CommonUtils.getJudgementDate(realTestFinishDate, realProductDate)) {
            throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }
        FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByUnitNo(fdevImplementUnitNo);
        if ( CommonUtils.isNullOrEmpty(fdevImplementUnit) ) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前任务所属研发单元不存在"});
        }
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(fdevImplementUnit.getDemand_id());
        if ( CommonUtils.isNullOrEmpty(demandBaseInfo) ) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前任务所属需求不存在"});
        }

        //将任务的Status转为研发单元的状态
        Integer unitStatus = CommonUtils.pareStage(taskStatus);
        fdevImplementUnit.setImplement_unit_status_normal(unitStatus);
        //修改研发单元启动，STI，UAT，REL和投产时间
        fdevImplementUnit.setReal_start_date(realStartDate);
        fdevImplementUnit.setReal_inner_test_date(CommonUtils.isNullOrEmpty(realInnerTestDate) ? "" : realInnerTestDate);
        fdevImplementUnit.setReal_test_date(CommonUtils.isNullOrEmpty(realTestDate) ? "" : realTestDate);
        fdevImplementUnit.setReal_test_finish_date(CommonUtils.isNullOrEmpty(realTestFinishDate) ? "" : realTestFinishDate);
        fdevImplementUnit.setReal_product_date(CommonUtils.isNullOrEmpty(realProductDate) ? "" : realProductDate);
        implementUnitDao.update(fdevImplementUnit);

        //业务需求更新实施单元
        if( Dict.BUSINESS.equals(demandBaseInfo.getDemand_type()) ){
            //更新实施单元实际日期
            String implUnitNum = fdevImplementUnit.getIpmp_implement_unit_no();
            if(!CommonUtils.isNullOrEmpty(implUnitNum)){
                IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(implUnitNum);
                updateIpmpUnitStatus(ipmpUnit,userNameEn);
            }
        }else if( Dict.DAILY.equals(demandBaseInfo.getDemand_type()) ){//日常需求
            //更新其他需求任务实际时间 状态
            String other_demand_task_num = fdevImplementUnit.getOther_demand_task_num();
            if(!CommonUtils.isNullOrEmpty(other_demand_task_num)){
                OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum(other_demand_task_num);
                otherDemandTaskService.updateStatus(otherDemandTask);
            }
        }
        //更新需求实际日期 状态
        updateDemandStatus(demandBaseInfo);
    }


    @Override
    public void updateIpmpUnitStatus(IpmpUnit ipmpUnit,String userNameEn)  throws Exception {
        //修改实施单元状态与实际日期
        if (!CommonUtils.isNullOrEmpty(ipmpUnit)) {
            IpmpUnit oldIpmpUnit = new IpmpUnit();
            BeanUtils.copyProperties(ipmpUnit,oldIpmpUnit);
            //获取所有研发单元的状态和时间
            List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryByImplUnitNum(ipmpUnit.getImplUnitNum(),"");
            if (!CommonUtils.isNullOrEmpty(fdevUnitList)) {
                //获取所有研发单元最小阶段
                Integer minStage = fdevUnitList.get(0).getImplement_unit_status_normal();
                //获取所有研发单元最大阶段
                Integer maxStage = fdevUnitList.get( fdevUnitList.size() - 1 ).getImplement_unit_status_normal();
                //评估中的实施单元仅修改状态
                if (IpmpUnitEnum.IpmpUnitConFdevUnitStatus.EVALUATE.getValue().equals(maxStage)) {
                   // ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.EVALUATE.getName());
                    //ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                    return;
                }
                //状态小于UAT则进入 判断是否有一个状态进入开发中，SIT,UAT状态改为对应状态
                if (minStage < ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue()) {
                    //如果有一个状态进入开发中，SIT,UAT状态改为对应状态
                    for (FdevImplementUnit fdevImplementUnit : fdevUnitList) {
                        if (ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                ||ImplementUnitEnum.ImplementUnitStatusEnum.REL.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                ||ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                        ) {
                            //UAT
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue();
                            break;
                        }else if(ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())){
                            //sit
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue();

                        }else if(ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                && minStage < ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue()){
                            //开发中
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue();
                        }
                    }
                }

                /*if(ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(minStage) ){
                    //开发中仅保留开发中状态及以上的研发单元
                    fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >
                            ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()).collect(Collectors.toList());
                }else if(ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue().equals(minStage)){
                    //SIT仅保留SIT状态及以上的研发单元
                    fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >
                            ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue()).collect(Collectors.toList());
                }else if(ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue().equals(minStage)){
                    //UAT仅保留UAT状态及以上的研发单元
                    fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >
                            ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue()).collect(Collectors.toList());
                }*/
                switch (minStage) {
                    //待实施
                    case 2:
                        ipmpUnit.setActuralDevelopDate("");
                        ipmpUnit.setActualInnerTestDate("");
                        ipmpUnit.setActuralTestStartDate("");
                        ipmpUnit.setActuralTestFinishDate("");
                        ipmpUnit.setActuralProductDate("");
                        //ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.PRE_IMPLEMENT.getName());
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                        updateImplUnit(ipmpUnit,userNameEn);
                        break;
                    //开发中
                    case 3:
                        ipmpUnit.setActuralDevelopDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        ipmpUnit.setActualInnerTestDate("");
                        ipmpUnit.setActuralTestStartDate("");
                        ipmpUnit.setActuralTestFinishDate("");
                        ipmpUnit.setActuralProductDate("");
                        //ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.DEVELOP.getName());
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                        updateImplUnit(ipmpUnit,userNameEn);
                        break;
                    //SIT=实施单元开发中
                    case 4:
                        ipmpUnit.setActuralDevelopDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        ipmpUnit.setActualInnerTestDate(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        ipmpUnit.setActuralTestStartDate("");
                        ipmpUnit.setActuralTestFinishDate("");
                        ipmpUnit.setActuralProductDate("");
                        //ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.SIT.getName());
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                        updateImplUnit(ipmpUnit,userNameEn);
                        break;
                    //业务测试中
                    case 5:
                        ipmpUnit.setActuralDevelopDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        ipmpUnit.setActualInnerTestDate(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        ipmpUnit.setActuralTestStartDate(dealTaskUnit.getRealTestDate(fdevUnitList));
                        if( !(Constants.ONE.equals(ipmpUnit.getTestImplDeptDemdKey())
                                && "ZH-0748".equals(ipmpUnit.getUsedSysCode())) ){
                            ipmpUnit.setActuralTestFinishDate("");
                        }
                        ipmpUnit.setActuralProductDate("");
                        //ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.UAT.getName());
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                        updateImplUnit(ipmpUnit,userNameEn);
                        break;
                    //业务测试完成
                    case 6:
                        ipmpUnit.setActuralDevelopDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        ipmpUnit.setActualInnerTestDate(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        ipmpUnit.setActuralTestStartDate(dealTaskUnit.getRealTestDate(fdevUnitList));
                        //测试实施部门不为测试服务中心的才需要 同步用户测试完成日期
                        if( !(Constants.ONE.equals(ipmpUnit.getTestImplDeptDemdKey())
                                && "ZH-0748".equals(ipmpUnit.getUsedSysCode())) ){
                            ipmpUnit.setActuralTestFinishDate(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
                        }
                        ipmpUnit.setActuralProductDate("");
                        //ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.REL.getName());
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                        updateImplUnit(ipmpUnit,userNameEn);
                        break;
                    //已投产
                    case 7:
                        ipmpUnit.setActuralDevelopDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        ipmpUnit.setActualInnerTestDate(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        ipmpUnit.setActuralTestStartDate(dealTaskUnit.getRealTestDate(fdevUnitList));
                        //测试实施部门不为测试服务中心的才需要 同步用户测试完成日期
                        if( !(Constants.ONE.equals(ipmpUnit.getTestImplDeptDemdKey())
                                && "ZH-0748".equals(ipmpUnit.getUsedSysCode())) ){
                            ipmpUnit.setActuralTestFinishDate(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
                        }
                        //判断实施单元投产是否延期，若延期，则不向ipmp更新
                        String planProductDate = ipmpUnit.getPlanProductDate();
                        String realProductDate = dealTaskUnit.getRealProductDate(fdevUnitList);
                        ipmpUnit.setActuralProductDate(realProductDate);
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                        if(!CommonUtils.getJudgementDate(realProductDate,planProductDate)
                                && (CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) || !ipmpUnit.getConfirmDelayStage().contains(Dict.PRODUCTDELAY))){
                            ipmpUnit.setActuralProductDate("");
                            //投产延期提醒邮件，邮件提醒实施单元牵头人申请调整排期
                            try {
                                sendEmailDemandService.sendEmailImplProDelay(ipmpUnit,demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum()),null);
                            }catch (Exception e){
                                logger.info(e.getMessage()+"。投产延期调整排期提醒邮件发送失败！");
                            }
                        }
                        updateImplUnit(ipmpUnit,userNameEn);
                        break;
                    default:
                        break;
                }
            }else {
                //研发单元为空 将实际日期置空 状态置为评估中
                ipmpUnit.setActuralDevelopDate("");
                ipmpUnit.setActualInnerTestDate("");
                ipmpUnit.setActuralTestStartDate("");
                ipmpUnit.setActuralTestFinishDate("");
                ipmpUnit.setActuralProductDate("");
                //ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.EVALUATE.getName());
                ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                updateImplUnit(ipmpUnit,userNameEn);
            }
            //判断是否延期
            if( implIsDelayEmail(ipmpUnit) ){
                //发送延期邮件(不包括投产延期)
                sendEmailDemandService.sendEmailImplDelay(ipmpUnit,demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum()),null);
            }

            //记录实体日志 updateType  syncNew = 同步新增 syncUpdate = 同步修改 ,fdevUpdate = 页面修改
            logService.saveIpmpUnitEntityLog(ipmpUnit,oldIpmpUnit,"fdevUpdate");
        }
    }

    public void updateImplUnit(IpmpUnit ipmpUnit,String userNameEn)  throws Exception {

        String testLeaderName = ipmpUnit.getTestLeaderName();//测试牵头人中文姓名
        String testLeader = ipmpUnit.getTestLeader();//测试牵头人域账号
        String testLeaderEmail = ipmpUnit.getTestLeaderEmail();//测试牵头人邮箱
        if(CommonUtils.isNullOrEmpty(testLeader)) {
            testLeaderName = ipmpUnit.getImplLeaderName() ;//测试牵头人为空  默认实施单元牵头人
            testLeader = ipmpUnit.getImplLeader() ;//测试牵头人为空  默认实施单元牵头人
        }
        Map<String, String> updateImplUnitMap = new HashMap<>();
        updateImplUnitMap.put(Dict.ACTURALDEVELOPDATE,ipmpUnit.getActuralDevelopDate());
        updateImplUnitMap.put(Dict.ACTURALTESTSTARTDATE,ipmpUnit.getActuralTestStartDate());
        updateImplUnitMap.put(Dict.ACTURALTESTFINISHDATE,ipmpUnit.getActuralTestFinishDate());
        updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,ipmpUnit.getActuralProductDate());
        updateImplUnitMap.put(Dict.IMPLUNITNUM,ipmpUnit.getImplUnitNum());
        //延期字段
        updateImplUnitMap.put(Dict.IMPLDELAYTYPE,ipmpUnit.getImplDelayType());
        updateImplUnitMap.put(Dict.IMPLDELAYREASON,ipmpUnit.getImplDelayReason());
        //实际日期 测试牵头人
        updateImplUnitMap.put(Dict.TESTLEADERNAME,testLeaderName);//测试牵头人中文姓名
        updateImplUnitMap.put(Dict.TESTLEADER,testLeader);//测试牵头人域账号
        updateImplUnitMap.put(Dict.TESTLEADEREMAIL,testLeaderEmail);//测试牵头人邮箱
        try {
            //判断行内 厂商 1 代表厂商   0 代表 行内
            User user = CommonUtils.getSessionUser();
            updateImplUnitMap.put(Dict.USERTYPE,CommonUtils.isInt(user.getEmail()) ? "0" : "1");
        }catch (Exception e){
            //获取登录信息失败默认 用户为行内 默认类型也是行内
            updateImplUnitMap.put(Dict.USERTYPE, "0");
        }
        updateImplUnitMap.put(Dict.USER_NAME_EN,userNameEn);
        updateImplUnitMap.put(Dict.ISCHECK,"1");//自动同步 校验是否核算
        //不是存量的实施单元才发IPMP接口
        if(!Constants.STOCKUNIT.equals(ipmpUnit.getUsedSysCode())){
            ipmpUtilsService.updateImplUnit(updateImplUnitMap);
        }
    }

    @Override
    public void updateDemandStatus(DemandBaseInfo demandBaseInfo)  throws Exception {
        //修改需求状态与实际日期
        if( !CommonUtils.isNullOrEmpty(demandBaseInfo) ){
            if( !CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_status_special()) ){
                //特殊状态不为空置为 恢复完成
                demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.FINISTH.getValue());
            }
            //获取除去特殊状态的研发单元的状态和时间，修改需求相关信息
            List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryFdevImplementUnitByDemandId(demandBaseInfo.getId());
            //为空
            if( CommonUtils.isNullOrEmpty(fdevUnitList) ){
                    //获取恢复中研发单元
                    List<FdevImplementUnit> fdevUnitListRecover = implementUnitDao.queryRecoverFdevUnitByDemandId(demandBaseInfo.getId());
                    if (!CommonUtils.isNullOrEmpty(fdevUnitListRecover)){
                        //存在恢复中研发单元 需求恢复中
                        demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.RECOVER.getValue());
                    } else {
                        if(!CommonUtils.isNullOrEmpty(implementUnitDao.queryDeferFdevUnitByDemandId(demandBaseInfo.getId()))){
                            //存在研发单元暂缓 需求暂缓
                            demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.DEFER.getValue());
                        }else if(!CommonUtils.isNullOrEmpty(implementUnitDao.queryCanceledFdevUnitByDemandId(demandBaseInfo.getId()))){
                            //所有研发单元均为撤销
                            demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.IS_CANCELED.getValue());
                        }
                        //业务需求
                        if(Dict.BUSINESS.equals(demandBaseInfo.getDemand_type())){
                            //研发单元为空或均为暂缓或撤销获取实施单元
                            Map<String, Object> informationNumMap = new HashMap<>();
                            informationNumMap.put(Dict.INFORMATIONNUM,demandBaseInfo.getOa_contact_no());
                            List<IpmpUnit> unitList = (List<IpmpUnit>)ipmpUnitDao.queryIpmpUnitByDemandId(informationNumMap).get(Dict.DATA);
                            if(!CommonUtils.isNullOrEmpty(unitList)){
                                Set<String> implStatusNameSet = new HashSet<>();
                                for(IpmpUnit ipmpUnit : unitList){
                                    implStatusNameSet.add(ipmpUnit.getImplStatusName());
                                }
                                //是否包含业务测试中 需求业务测试中 业务测试>开发>待实施>评估中
                                if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.UAT.getName())){
                                    demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.UAT.getValue());
                                }else if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.DEVELOP.getName())){
                                    //包含开发中 需求开发中
                                    demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.DEVELOP.getValue());
                                }else if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.PRE_IMPLEMENT.getName())){
                                    //包含待实施 需求待实施
                                    demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue());
                                }else if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.EVALUATE.getName())){
                                    //包含评估中 需求评估中
                                    demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.EVALUATE.getValue());
                                }else if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.REL.getName())){
                                    //最小状态为REL 需求REL
                                    demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.REL.getValue());
                                }else if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.PRODUCT.getName())){
                                    //最小状态为已投产 判断是否存在暂缓
                                    if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName()) ||
                                            implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName())){
                                        //存在暂缓 需求暂缓
                                        demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.DEFER.getValue());
                                    }else {
                                        //需求 已投产
                                        demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.PRODUCT.getValue());
                                    }

                                }else if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName()) ||
                                        implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName())){
                                    //暂缓 暂存 需求暂缓
                                    demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.DEFER.getValue());
                                }else if(implStatusNameSet.contains(IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName())){
                                    //已撤销
                                    demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.IS_CANCELED.getValue());
                                }
                            }else{
                                demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue());
                            }
                        }/*else if (Dict.DAILY.equals(demandBaseInfo.getDemand_type())){//日常需求
                            Map<String, Object> params = new HashMap<>();
                            params.put(Dict.DEMANDID,demandBaseInfo.getId());
                            Map<String, Object> returnMap = otherDemandTaskDao.queryOtherDemandTaskList(params);
                            List<OtherDemandTask> otherDemandTasks = (List<OtherDemandTask>) returnMap.get(Dict.DATA);
                            if(!CommonUtils.isNullOrEmpty(otherDemandTasks)){
                                Set<String> statusSet = new HashSet<>();
                                for(OtherDemandTask otherDemandTask : otherDemandTasks){
                                    statusSet.add(otherDemandTask.getStatus());
                                }
                            }
                        }*/
                    }
                //未新增研发单元的小组
                List<RelatePartDetail> relatePartDetailList1 = demandBaseInfo.getRelate_part_detail();
                if(!CommonUtils.isNullOrEmpty(relatePartDetailList1)){
                    for (int i=0;i < relatePartDetailList1.size();i++) {
                        if("0".equals(relatePartDetailList1.get(i).getAssess_status())) {
                            //说明还有小组是在预评估的，该板块未新增研发单元
                            demandBaseInfo.setDemand_status_normal(DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue());
                            break;
                        }
                    }
                }
                demandBaseInfoDao.update(demandBaseInfo);
                return;
            }
            //需求状态置为恢复完成
            demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.FINISTH.getValue());
            //获取所有研发单元最小阶段   最多为评估中
            Integer minStage = fdevUnitList.get(0).getImplement_unit_status_normal();
            //状态为已投产 判断是否有暂缓
            if( ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue().equals(minStage) ){
                //获取暂缓研发单元
                List<FdevImplementUnit> fdevUnitListRecover = implementUnitDao.queryRecoverFdevUnitByDemandId(demandBaseInfo.getId());
                if (!CommonUtils.isNullOrEmpty(fdevUnitListRecover)){
                    //需求暂缓
                    demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.DEFER.getValue());
                }
            }
            if(minStage >= ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()){
                //需要考虑未新增研发单元的小组
                List<RelatePartDetail> relatePartDetailList1 = demandBaseInfo.getRelate_part_detail();
                if(!CommonUtils.isNullOrEmpty(relatePartDetailList1)){
                    for(RelatePartDetail relatePartDetail : relatePartDetailList1){
                        if(relatePartDetail.getAssess_status().compareToIgnoreCase(DemandEnum.DemandAssess_status.EVALUATE_OVER.getValue()) < 0){
                            //说明还有小组是在预评估的，该板块未新增研发单元
                            minStage = DemandEnum.DemandStatusEnum.EVALUATE.getValue();
                            break;
                        }
                    }
                }
            }
            //评估中的需求仅修改状态
            if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())) {//日常需求
                if (DemandEnum.DemandStatusEnum.EVALUATE.getValue().equals(minStage)) {
                    demandBaseInfo.setReal_start_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfo.setDemand_status_normal(minStage);
                    demandBaseInfoDao.update(demandBaseInfo);
                    return;
                }
            }else{
                if (DemandEnum.DemandStatusEnum.EVALUATE.getValue().equals(minStage)) {
                    demandBaseInfo.setReal_start_date("");
                    demandBaseInfo.setReal_inner_test_date("");
                    demandBaseInfo.setReal_test_date("");
                    demandBaseInfo.setReal_test_finish_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfo.setDemand_status_normal(minStage);
                    demandBaseInfoDao.update(demandBaseInfo);
                    return;
                }
            }

            //状态小于UAT则进入 判断是否有一个状态进入开发中，SIT,UAT状态改为对应状态
            if (minStage < ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue()) {
                if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())) {
                    //如果有一个状态进入开发中，最新状态又小于sit，最小状态设置为开发中
                    for (FdevImplementUnit fdevImplementUnit : fdevUnitList) {
                        if((ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                || ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal()))
                                && minStage < ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue()){
                            //开发中
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue();
                        }
                    }
                }else {
                    //如果有一个状态进入开发中，SIT,UAT状态改为对应状态 存在大于UAT状态的实施单元 需求置为UAT
                    //只考虑有测试时间的已投产研发单元，没时间的是下面只有一个日常任务的
                    for (FdevImplementUnit fdevImplementUnit : fdevUnitList) {
                        if (ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                ||ImplementUnitEnum.ImplementUnitStatusEnum.REL.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                || (ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                && !CommonUtils.isNullOrEmpty(fdevImplementUnit.getReal_test_date()))
                        ) {
                            //UAT
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue();
                            break;
                        }else if(ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())){
                            //sit
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue();

                        }else if(ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(fdevImplementUnit.getImplement_unit_status_normal())
                                && minStage < ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue()){
                            //开发中
                            minStage = ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue();
                        }
                    }
                }
            }

            /*if(ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(minStage) ){
                //开发中仅保留开发中状态的研发单元
                fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >
                        ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()).collect(Collectors.toList());
            }else if(ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue().equals(minStage)){
                //SIT仅保留SIT状态的研发单元
                fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >
                        ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue()).collect(Collectors.toList());
            }else if(ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue().equals(minStage)){
                //UAT仅保留UAT及以上状态的研发单元
                fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >
                        ImplementUnitEnum.ImplementUnitStatusEnum.SIT.getValue()).collect(Collectors.toList());
            }*/

            demandBaseInfo.setDemand_status_normal(minStage);
            if(Dict.DAILY.equals(demandBaseInfo.getDemand_type())){//日常需求
                switch (minStage) {
                    //待实施
                    case 2:
                        demandBaseInfo.setReal_start_date("");
                        demandBaseInfo.setReal_product_date("");
                        break;
                    //进行中
                    case 3:
                        demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                        demandBaseInfo.setReal_product_date("");
                        break;
                    //已投产
                    case 7:
                        demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                        demandBaseInfo.setReal_product_date(dealTaskUnit.getRealProductDate(fdevUnitList));
                        break;
                    default:
                        break;
                }
            }else{
                switch (minStage) {
                    //待实施
                    case 2:
                        demandBaseInfo.setReal_start_date("");
                        demandBaseInfo.setReal_inner_test_date("");
                        demandBaseInfo.setReal_test_date("");
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        break;
                    //开发中
                    case 3:
                        demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                        demandBaseInfo.setReal_inner_test_date("");
                        demandBaseInfo.setReal_test_date("");
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        break;
                    //SIT
                    case 4:
                        demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                        demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        demandBaseInfo.setReal_test_date("");
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        break;
                    //UAT
                    case 5:
                        demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                        demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(fdevUnitList));
                        demandBaseInfo.setReal_test_finish_date("");
                        demandBaseInfo.setReal_product_date("");
                        break;
                    //REL
                    case 6:
                        demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                        demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(fdevUnitList));
                        demandBaseInfo.setReal_test_finish_date(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
                        demandBaseInfo.setReal_product_date("");
                        break;
                    //已投产
                    case 7:
                        demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                        demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(fdevUnitList));
                        demandBaseInfo.setReal_test_finish_date(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
                        demandBaseInfo.setReal_product_date(dealTaskUnit.getRealProductDate(fdevUnitList));
                        break;
                    default:
                        break;
                }
            }
            demandBaseInfoDao.update(demandBaseInfo);
        }

    }

    //判断实施单元是否延期
    public boolean implIsDelay(IpmpUnit ipmpUnit) throws ParseException {
        String planDevelopDate = ipmpUnit.getPlanDevelopDate();//计划启动开发日期
        String planTestStartDate = ipmpUnit.getPlanTestStartDate();//计划提交用户测试日期
        String planTestFinishDate = ipmpUnit.getPlanTestFinishDate();//计划用户测试完成日期
        String planProductDate = ipmpUnit.getPlanProductDate();//计划投产日期
        String acturalDevelopDate = ipmpUnit.getActuralDevelopDate();//实际启动开发日期
        String acturalTestStartDate = ipmpUnit.getActuralTestStartDate();//实际提交用户测试日期
        String acturalTestFinishDate = ipmpUnit.getActuralTestFinishDate();//实际用户测试完成日期
        String acturalProductDate = ipmpUnit.getActuralProductDate();//实际投产日期
        if(CommonUtils.isDelay(planDevelopDate,acturalDevelopDate)) return true;//开发延期
        if(CommonUtils.isDelay(planTestStartDate,acturalTestStartDate)) return true;//提测延期
        if(CommonUtils.isDelay(planTestFinishDate,acturalTestFinishDate)) return true;//完成测试延期
        if(CommonUtils.isDelay(planProductDate,acturalProductDate)) return true;//投产延期
        return false;
    }

    //判断实施单元是否延期发送邮件
    public boolean implIsDelayEmail(IpmpUnit ipmpUnit) throws ParseException {
        String planDevelopDate = ipmpUnit.getPlanDevelopDate();//计划启动开发日期
        String planTestStartDate = ipmpUnit.getPlanTestStartDate();//计划提交用户测试日期
        String planTestFinishDate = ipmpUnit.getPlanTestFinishDate();//计划用户测试完成日期
        String planProductDate = ipmpUnit.getPlanProductDate();//计划投产日期
        String acturalDevelopDate = ipmpUnit.getActuralDevelopDate();//实际启动开发日期
        String acturalTestStartDate = ipmpUnit.getActuralTestStartDate();//实际提交用户测试日期
        String acturalTestFinishDate = ipmpUnit.getActuralTestFinishDate();//实际用户测试完成日期
        String acturalProductDate = ipmpUnit.getActuralProductDate();//实际投产日期
        String developDelayEmail = ipmpUnit.getDevelopDelayEmail();//实际启动开发延期邮件 是否有发送过邮件 1 = 已发送
        String testStartDelayEmail = ipmpUnit.getTestStartDelayEmail();//实际提交用户测试延期邮件
        String testFinishDelayEmail = ipmpUnit.getTestFinishDelayEmail();//实际用户测试完成延期邮件
        String productDelayEmail = ipmpUnit.getProductDelayEmail();//实际投产延期邮件
        //不为 1 未发送过该状态的延期邮件
        if(!Constants.ONE.equals(developDelayEmail)){
            if(CommonUtils.isDelay(planDevelopDate,acturalDevelopDate)){
                ipmpUnit.setDevelopDelayEmail(Constants.ONE);
                return true;//开发延期
            }
        }
        if(!Constants.ONE.equals(testStartDelayEmail)){
            if(CommonUtils.isDelay(planTestStartDate,acturalTestStartDate)) {
                ipmpUnit.setTestStartDelayEmail(Constants.ONE);
                return true;//提测延期
            }
        }
        if(!Constants.ONE.equals(testFinishDelayEmail)){
            if(CommonUtils.isDelay(planTestFinishDate,acturalTestFinishDate)) {
                ipmpUnit.setTestFinishDelayEmail(Constants.ONE);
                return true;//完成测试延期
            }
        }
//        if(!Constants.ONE.equals(productDelayEmail)){
//            if(CommonUtils.isDelay(planProductDate,acturalProductDate)) {
//                ipmpUnit.setProductDelayEmail(Constants.ONE);
//                return true;//投产延期
//            }
//        }
        return false;
    }

    @Override
    public Map<String, Object> queryByUnitNoAndDemandId(String demandId, String unitNo) throws Exception {
        //根据需求id查询需求信息，如果实施单元不为空，增加返回实施单元信息
        Map<String, Object> result = new HashMap<>();
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        if (StringUtils.isNotBlank(unitNo)) {
            IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitByNo(unitNo);
            Map<String, Object> unitInfo = new HashMap<>();
            unitInfo.put(Dict.GROUP, ipmpUnit.getLeaderGroup());
            unitInfo.put(Dict.PLAN_INNER_TEST_DATE, ipmpUnit.getPlanInnerTestDate());
            unitInfo.put(Dict.PLAN_TEST_DATE, ipmpUnit.getPlanTestStartDate());
            unitInfo.put(Dict.PLAN_PRODUCT_DATE, ipmpUnit.getPlanProductDate());
            unitInfo.put(Dict.IMPLEMENT_UNIT_STATUS_NORMAL, unitStatusToInteger(ipmpUnit.getImplStatusName()));
            DemandBaseInfo unitDemandBaseInfo = demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum());
            String demand_id = "";
            if(!CommonUtils.isNullOrEmpty(unitDemandBaseInfo)) {
                demand_id = unitDemandBaseInfo.getId();
            }
            unitInfo.put(Dict.DEMAND_ID, demand_id);
            List<Map<String,String>> leaderList = new ArrayList<>();
            String leaderNameStr = ipmpUnit.getImplLeader();
            if(StringUtils.isNotBlank(leaderNameStr)) {
                String[] leaderENNameArr = leaderNameStr.split(",");
                String[] leaderCNNameArr = ipmpUnit.getImplLeaderName().split(",");
                Map<String, String> leaderMap = null;
                for (int i = 0; i < leaderENNameArr.length; i++) {
                    leaderMap = new HashMap<>();
                    leaderMap.put(Dict.USER_NAME_EN, leaderENNameArr[i]);
                    leaderMap.put(Dict.USER_NAME_CN, leaderCNNameArr[i]);
                    leaderList.add(leaderMap);
                }
            }
            unitInfo.put(Dict.IMPLEMENT_LEADER_ALL, leaderList);
            result.put(Dict.IMPLEMENT_UNIT_INFO, null != ipmpUnit ? unitInfo : new HashMap<>());
            if (null == demandBaseInfo && null != ipmpUnit)
                demandBaseInfo = unitDemandBaseInfo;
        }
        result.put(Dict.DEMAND_BASEINFO, null != demandBaseInfo ? demandBaseInfo : new HashMap<>());
        return result;
    }

    @Override
    public Map<String,Object> queryIpmpUnitIsCheck(Map<String, Object> param) throws Exception {
        Map<String,Object> returnMap = new HashMap();
        returnMap.put(Dict.URL,ipmpUnitUrl);
        //获取人员信息 校验权限
        User user = CommonUtils.getSessionUser();
        param.put(Dict.IMPLLEADER,user.getUser_name_en());//登录人英文名
        Map<String, Object> map = ipmpUnitDao.queryIpmpUnitByDemandId(param);
        if(!CommonUtils.isNullOrEmpty(map.get(Dict.DATA))){
            StringBuffer stringBuffer = new StringBuffer();
            List<IpmpUnit> ipmpUnitList = (List<IpmpUnit>) map.get(Dict.DATA) ;

            for (IpmpUnit ipmpUnit : ipmpUnitList) {
                //判断不是存量的实施单元 并且业务测试完成日期不为空
                if(!Constants.STOCKUNIT.equals(ipmpUnit.getUsedSysCode()) && !CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestFinishDate())){
                    //请求IPMP接口 判断是否核算
                    if(ipmpUtilsService.getUfpCountFlag( ipmpUnit.getImplUnitNum() )){
                        stringBuffer.append("【").append(ipmpUnit.getImplUnitNum()).append("】");
                    }
                }
            }
            returnMap.put(Dict.IMPLUNITNUM,stringBuffer.toString());
            return  returnMap;
        }
        return returnMap;
    }

    @Override
    public Map<String, Object> queryIpmpUnitList(Map<String, Object> params) throws Exception {
        String demandKey = (String)params.get(Dict.DEMANDKEY);//需求名称/编号 模糊搜索
        String implUnitType = (String)params.get(Dict.IMPLUNITTYPE);//实施单元类型 tech--科技；business--业务
        String keyword = (String)params.get(Dict.KEYWORD);//实施单元内容/实施单元编号  模糊搜索
        List<String> groupIds = (List<String>)params.get(Dict.GROUPIDS);//牵头小组
        String prjNum = (String)params.get(Dict.PRJNUM);//项目编号 模糊搜索
        String implLeader = (String)params.get(Dict.IMPLLEADER);//牵头人域账号 英文名
        Integer size = (Integer)params.get(Dict.SIZE);//每页条数，不传默认查全部
        Integer index = (Integer)params.get(Dict.INDEX);//页码
        String groupQueryType = (String)params.get(Dict.GROUPQUERYTYPE);//是否查询子组 0 本组 1 本组子组
        String applyStage = (String) params.get("applyStage");//调整排期阶段，all全部、noApply未申请、applying申请中、applied已完成
        List<String> implStatusNameList = (List<String>) params.get(Dict.IMPLSTATUSNAMELIST);//实施单元状态列表
        //小组
        List<String> groupAll = new ArrayList<String>();
        if ( "1".equals(groupQueryType) ) {
            if ( !CommonUtils.isNullOrEmpty(groupIds) ) {
                groupAll.addAll(groupUtil.getGroupByParent(groupIds));
                groupIds = groupAll;
            }
        }
        Map<String, Object> map = ipmpUnitDao.queryIpmpUnitList(demandKey,implUnitType,keyword,groupIds
                ,prjNum,implLeader,size,index,applyStage,implStatusNameList);

        List<IpmpUnit> ipmpUnitList = (List<IpmpUnit>) map.get(Dict.DATA);
        if(!CommonUtils.isNullOrEmpty(ipmpUnitList)){
            for (IpmpUnit ipmpUnit : ipmpUnitList) {
                String leaderGroup = ipmpUnit.getLeaderGroup();
                //牵头小组
                if(!CommonUtils.isNullOrEmpty(leaderGroup)){
                    Map<String, Object> groupMap = roleService.queryGroup(leaderGroup);
                    if(!CommonUtils.isNullOrEmpty(groupMap)){
                        ipmpUnit.setLeaderGroupName((String) groupMap.get(Dict.NAME));
                    }
                }
                if( Dict.BUSINESS.equals(implUnitType) || CommonUtils.isNullOrEmpty(implUnitType) ) {
                    //业务需求查询需求ID
                    if( !ipmpUnit.getImplUnitNum().contains("科技") ){
                        List<DemandBaseInfo> demandBaseInfos = demandBaseInfoDao.queryDemandBaseList(ipmpUnit.getInformationNum());
                        if(!CommonUtils.isNullOrEmpty(demandBaseInfos)){
                            ipmpUnit.setDemandId(demandBaseInfos.get(0).getId());
                        }
                        Boolean isIpmpLeader = isIpmpLeader(ipmpUnit);
                        //业务需求实施单元可调整排期、确认延期
                        //调整排期按钮:实施单元牵头人可操作自己牵头的，其他人置灰提示“仅本实施单元牵头人可操作”
                        ipmpUnit.setAdjustDateButton(getAdjustDateButton(isIpmpLeader));
                        //确认延期按钮：实施单元牵头人可操作自己牵头的，其他人置灰提示“仅本实施单元牵头人可操作”。
                        // 申请中的实施单元可操作，否则置灰提示“本实施单元暂未申请调整排期”。
                        ipmpUnit.setConfirmDelayButton(getConfirmDelayButton(ipmpUnit, isIpmpLeader));
                        //实施计划变更原因分类翻译为中文
                        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getImplChangeType())){
                            ipmpUnit.setImplChangeTypeName(getDictValueByCodes(ipmpUnit.getImplChangeType()));

                        }
                    }
                }
            }
        }
        return map;
    }

    private List<String> getDictValueByCodes(List<String> codes){
        List<DictEntity> dictEntities = dictDao.queryByCodes(codes);
        List<String> result = new ArrayList<>();
        for(DictEntity dictEntity : dictEntities){
            result.add(dictEntity.getValue());
        }
        return result;
    }

    /**
     * 获取调整排期按钮：0亮，1置灰（仅本实施单元牵头人可操作）
     * @param isIpmpLeader
     * @return
     */
    private String getAdjustDateButton(boolean isIpmpLeader){
        if(isIpmpLeader){
            return "0";
        }
        return "1";
    }

    /**
     * 判断当前登录用户是否为实施单元牵头人
     * @param ipmpUnit
     * @return
     */
    private Boolean isIpmpLeader(IpmpUnit ipmpUnit) throws Exception {
        User user = CommonUtils.getSessionUser();
        String implLeader = ipmpUnit.getImplLeader();
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            String[] leaders = implLeader.split(",");
            for(String leader : leaders){
                if(user.getUser_name_en().equals(leader)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取确认延期按钮：0亮，1置灰（仅本实施单元牵头人可操作），2置灰（本实施单元未延期）
     * @param ipmpUnit
     * @param isIpmpLeader
     * @return
     */
    private String getConfirmDelayButton(IpmpUnit ipmpUnit, boolean isIpmpLeader) throws ParseException {
        if(!isIpmpLeader){
            return "1";
        }else {
//            if(isApplying(ipmpUnit)){
            if(implIsDelay(ipmpUnit)){
                return "0";
            }else {
                return "2";
            }
        }
    }
    /**
     * 判断实施单元是否未申请调整排期：所有计划调整日期为空
     * @param ipmpUnit
     * @return
     */
    public Boolean isNoApply(IpmpUnit ipmpUnit){
        if(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDateAdjust()) && CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDateAdjust())
                && CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDateAdjust()) && CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDateAdjust())){
            return true;
        }
        return false;
    }

    /**
     * 判断实施单元是否正在申请调整排期：申请中：存在不为空的计划调整日期 且 不等于对应的计划日期 且 对应的阶段没有确认延期
     * @param ipmpUnit
     * @return
     */
    public Boolean isApplying(IpmpUnit ipmpUnit){
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanDevelopDateAdjust())){
            if(!ipmpUnit.getPlanDevelopDateAdjust().equals(ipmpUnit.getPlanDevelopDate())
                    && (CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) || !ipmpUnit.getConfirmDelayStage().contains(Dict.DEVELOPDELAY))){
                return true;
            }
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDateAdjust())){
            if(!ipmpUnit.getPlanTestStartDateAdjust().equals(ipmpUnit.getPlanTestStartDate())
                    && (CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) || !ipmpUnit.getConfirmDelayStage().contains(Dict.TESTSTARTDELAY))){
                return true;
            }
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDateAdjust())){
            if(!ipmpUnit.getPlanTestFinishDateAdjust().equals(ipmpUnit.getPlanTestFinishDate())
                    && (CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) || !ipmpUnit.getConfirmDelayStage().contains(Dict.TESTFINISHDELAY))){
                return true;
            }
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDateAdjust())){
            if(!ipmpUnit.getPlanProductDateAdjust().equals(ipmpUnit.getPlanProductDate())
                    && (CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) || !ipmpUnit.getConfirmDelayStage().contains(Dict.PRODUCTDELAY))){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断实施单元是否已完成排期调整，已完成：存在不为空的计划调整日期 且 （均等于对应的计划日期 或 对应的阶段确认延期）
     * @param ipmpUnit
     * @return
     */
    public Boolean isApplied(IpmpUnit ipmpUnit){
        int flag = 0;
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanDevelopDateAdjust())){
            if(ipmpUnit.getPlanDevelopDateAdjust().equals(ipmpUnit.getPlanDevelopDate())
                    || (!CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) && ipmpUnit.getConfirmDelayStage().contains(Dict.DEVELOPDELAY))){
                flag++;
            }else {
                return false;
            }
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDateAdjust())){
            if(ipmpUnit.getPlanTestStartDateAdjust().equals(ipmpUnit.getPlanTestStartDate())
                    || (!CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) && ipmpUnit.getConfirmDelayStage().contains(Dict.TESTSTARTDELAY))){
                flag++;
            }else {
                return false;
            }
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDateAdjust())){
            if(ipmpUnit.getPlanTestFinishDateAdjust().equals(ipmpUnit.getPlanTestFinishDate())
                    || (!CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) && ipmpUnit.getConfirmDelayStage().contains(Dict.TESTFINISHDELAY))){
                flag++;
            }else {
                return false;
            }
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDateAdjust())){
            if(ipmpUnit.getPlanProductDateAdjust().equals(ipmpUnit.getPlanProductDate())
                    || (!CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) && ipmpUnit.getConfirmDelayStage().contains(Dict.PRODUCTDELAY))){
                flag++;
            }else {
                return false;
            }
        }
        if(flag > 0){
            return true;
        }
        return false;
    }

    @Override
    public void exportIpmpUnitList(Map<String, Object> params, HttpServletResponse resp) throws Exception {
        List<IpmpUnit> data = (List<IpmpUnit>) queryIpmpUnitList(params).get(Dict.DATA);
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource=new ClassPathResource("IpmpUnitExport.xlsx");
            inputStream=classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);

        } catch (Exception e1) {
            throw new FdevException("实施单元列表导出失败，请联系fdev管理员");
        }
        if(!CommonUtils.isNullOrEmpty(data)){
            int i=1;//行数
            for (IpmpUnit ipmpUnit : data) {
                int j=0;//列数
                sheet.createRow(i);
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getInformationNum()) ? "":ipmpUnit.getInformationNum());//需求信息单编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getInformationTitle()) ? "":ipmpUnit.getInformationTitle());//需求信息单标题
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplUnitNum()) ? "":ipmpUnit.getImplUnitNum());//实施单元编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplContent()) ? "":ipmpUnit.getImplContent());//实施单元内容
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplStatusName()) ? "":ipmpUnit.getImplStatusName());//实施状态
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplLeaderName()) ? "":ipmpUnit.getImplLeaderName());//实施牵头人中文姓名
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplLeader()) ? "":ipmpUnit.getImplLeader());//实施牵头人域账号
                if(CommonUtils.isNullOrEmpty(ipmpUnit.getDemandId())){
                    sheet.getRow(i).createCell(j++).setCellValue("");//实施单元跟踪人
                }else {
                    DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(ipmpUnit.getDemandId());
                    Set<String> impl_track_user = demandBaseInfo.getImpl_track_user();
                    String implTrackUserNameString = "";
                    if(!CommonUtils.isNullOrEmpty(impl_track_user)){
                        for(String name : impl_track_user){
                            implTrackUserNameString += name + " ";
                        }
                        sheet.getRow(i).createCell(j++).setCellValue(implTrackUserNameString.trim());//实施单元跟踪人
                    }else {
                        sheet.getRow(i).createCell(j++).setCellValue("");//实施单元跟踪人
                    }
                }
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getLeaderGroupName()) ? "":ipmpUnit.getLeaderGroupName());//牵头小组
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getHeaderUnitName()) ? "":ipmpUnit.getHeaderUnitName());//牵头单位
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getHeaderTeamName()) ? "":ipmpUnit.getHeaderTeamName());//牵头团队
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanDevelopDate()) ? "":ipmpUnit.getPlanDevelopDate());//计划启动开发日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanDevelopDateAdjust()) ? "":ipmpUnit.getPlanDevelopDateAdjust());//计划启动开发（调）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanInnerTestDate()) ? "":ipmpUnit.getPlanInnerTestDate());//计划提交内测日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDate()) ? "":ipmpUnit.getPlanTestStartDate());//计划提交用户测试日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestStartDateAdjust()) ? "":ipmpUnit.getPlanTestStartDateAdjust());//计划提交用户测试日期（调）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDate()) ? "":ipmpUnit.getPlanTestFinishDate());//计划用户测试完成日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanTestFinishDateAdjust()) ? "":ipmpUnit.getPlanTestFinishDateAdjust());//计划用户测试完成日期（调）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDate()) ? "":ipmpUnit.getPlanProductDate());//计划投产日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanProductDateAdjust()) ? "":ipmpUnit.getPlanProductDateAdjust());//计划投产日期（调）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralDevelopDate()) ? "":ipmpUnit.getActuralDevelopDate());//实际启动开发日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActualInnerTestDate()) ? "":ipmpUnit.getActualInnerTestDate());//实际提交内测日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestStartDate()) ? "":ipmpUnit.getActuralTestStartDate());//实际提交用户测试日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestFinishDate()) ? "":ipmpUnit.getActuralTestFinishDate());//实际用户测试完成日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActuralProductDate()) ? "":ipmpUnit.getActuralProductDate());//实际投产日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getRelateSysName()) ? "":ipmpUnit.getRelateSysName());//涉及系统名称
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getRelateSysCode()) ? "":ipmpUnit.getRelateSysCode());//涉及系统编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActualOwnUfworkload()) ? "":ipmpUnit.getActualOwnUfworkload().toString());//行内人员实际工作量（非功能性）（人月）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActualOutUfworkload()) ? "":ipmpUnit.getActualOutUfworkload().toString());//公司人员实际工作量（非功能性）（人月）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActualOwnFworkload()) ? "":ipmpUnit.getActualOwnFworkload().toString());//行内人员实际工作量（功能性）（人月）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getActualOutFworkload()) ? "":ipmpUnit.getActualOutFworkload().toString());//公司人员实际工作量（功能性）（人月）
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getExpectOwnWorkload()) ? "":ipmpUnit.getExpectOwnWorkload().toString());//预期行内人员工作量
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getExpectOutWorkload()) ? "":ipmpUnit.getExpectOutWorkload().toString());//预期公司人员工作量
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPlanPrjName()) ? "":ipmpUnit.getPlanPrjName());//拟纳入项目名称
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getPrjNum()) ? "":ipmpUnit.getPrjNum());//项目编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getTestLeaderName()) ? "":ipmpUnit.getTestLeaderName());//测试牵头人中文姓名
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getTestLeader()) ? "":ipmpUnit.getTestLeader());//测试牵头人域账号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getTestLeaderEmail()) ? "":ipmpUnit.getTestLeaderEmail());//测试牵头人邮箱
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getTestImplDeptName()) ? "":ipmpUnit.getTestImplDeptName());//测试实施部门
                //实施计划变更原因分类
                List<String> implChangeType = ipmpUnit.getImplChangeType();
                if(!CommonUtils.isNullOrEmpty(implChangeType)){
                    List<String> values = this.getDictValueByCodes(implChangeType);
                    String implChangeTypeNames = "";
                    for(String value : values){
                        implChangeTypeNames += value + "、";
                    }
                    sheet.getRow(i).createCell(j++).setCellValue(implChangeTypeNames.trim().substring(0,implChangeTypeNames.length()-1));//实施计划变更原因分类
                }else {
                    sheet.getRow(i).createCell(j++).setCellValue("");//实施计划变更原因分类
                }
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplChangeReason()) ? "":ipmpUnit.getImplChangeReason());//实施计划变更原因
                //邮件附件名称
                if(CommonUtils.isNullOrEmpty(ipmpUnit.getBusinessEmail())){
                    sheet.getRow(i).createCell(j++).setCellValue("");
                }else {
                    //获取邮件名称
                    String emailName = "";
                    for(Map<String, String> map : ipmpUnit.getBusinessEmail()){
                        emailName += map.get("businessEmailName") + "、";
                    }
                    sheet.getRow(i).createCell(j++).setCellValue(emailName.trim().substring(0,emailName.length()-1));
                }
                //确认延期阶段
                if(CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage())){
                    sheet.getRow(i).createCell(j++).setCellValue("");
                }else {
                    List<String> confirmDelayStage = ipmpUnit.getConfirmDelayStage();
                    String delayStage = "";
                    for(String stage : confirmDelayStage){
                        if(Dict.DEVELOPDELAY.equals(stage)){
                            delayStage += "启动延期" + "、";
                        }
                        if(Dict.TESTSTARTDELAY.equals(stage)){
                            delayStage += "提交用户测试延期" + "、";
                        }
                        if(Dict.TESTFINISHDELAY.equals(stage)){
                            delayStage += "用户测试完成延期" + "、";
                        }
                        if(Dict.PRODUCTDELAY.equals(stage)){
                            delayStage += "投产延期" + "、";
                        }
                    }
                    sheet.getRow(i).createCell(j++).setCellValue(delayStage.trim().substring(0,delayStage.length() - 1));
                }
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplDelayTypeName()) ? "":ipmpUnit.getImplDelayTypeName());//实施延期原因分类
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getImplDelayReason()) ? "":ipmpUnit.getImplDelayReason());//实施延期原因
                //sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getCloudFlagName()) ? "":ipmpUnit.getCloudFlagName());//是否上云转译
                //sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getTechSchemeNo()) ? "":ipmpUnit.getTechSchemeNo());//技术方案编号
                //sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(ipmpUnit.getCheckerUserNames()) ? "":ipmpUnit.getCheckerUserNames());//审核人姓名
                i++;
            }
        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "IpmpUnitList"+ ".xlsx");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
        }
    }

    @Override
    public Object getTestFinishDateByUpdateTime(Map<String, Object> params) throws Exception {

        String beginDate = TimeUtil.todayLastTwoWeek(1);
        String endDate = TimeUtil.todayLastTwoWeek(0);
        String url = getTestFinishDateByUpdateTime + "?" + "beginDate=" + beginDate + "&endDate=" + endDate ;
        //定时获取云测试平台的实施单元
        Map<String, Object> responseMap = JSONObject.parseObject((String) ipmpUtils.sendGet(url), Map.class);
        //返回为true 请求成功
        if((boolean)responseMap.get(Dict.RESULT)){
            List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get(Dict.CONTENT);
            if( !CommonUtils.isNullOrEmpty(content) ){
                contentManage(content);
            }
        }else{
            throw new FdevException("云测试平台getTestFinishDateByUpdateTime接口请求失败----- " + responseMap.get(Dict.MSG) + " ----时间==" + TimeUtil.formatTodayHs());
        }

        return responseMap;
    }

    @Override
    public void getTestFinishDate(Map<String, Object> params) throws Exception {
        List<XTestIpmpUnit> xTestIpmpUnitList = ipmpUnitDao.queryXTestIpmpUnit();
        Map<String, Object> response = null ;
        if (!CommonUtils.isNullOrEmpty(xTestIpmpUnitList)){
            List<String> ipmpUnitNos = new ArrayList<>();
            List<String> removeIpmpUnitNos = new ArrayList<>();
            for (XTestIpmpUnit xTestIpmpUnit : xTestIpmpUnitList) {
                //最新的记录为失败则  取出重新获取云测试平台获取数据
                if("error".equals(xTestIpmpUnit.getStatus())){
                    ipmpUnitNos.add(xTestIpmpUnit.getImplUnitNum());
                }else if("succeed".equals(xTestIpmpUnit.getStatus())){
                    removeIpmpUnitNos.add(xTestIpmpUnit.getImplUnitNum());
                }
            }
            for ( String removeIpmpUnitNo : removeIpmpUnitNos ) {
                ipmpUnitNos.remove(removeIpmpUnitNo);
            }
            if(!CommonUtils.isNullOrEmpty(ipmpUnitNos)) {
                List<Map<String, Object>> content = new ArrayList<>();
                int sizeMax = ipmpUnitNos.size();

                if ( sizeMax > 2000 ) {
                    int page = 1;
                    int end = 2000;
                    while (  end < sizeMax ) {
                        end = 2000;
                        int index = (page - 1) * end;
                        end = page * end < sizeMax ? page * end : sizeMax;
                        response = JSONObject.parseObject((String)ipmpUtils.send(ipmpUnitNos.subList( index , end ),getTestFinishDate) , Map.class);
                        //返回为true 请求成功
                        if((boolean)response.get(Dict.RESULT)) {
                            content.addAll((List<Map<String, Object>>) response.get(Dict.CONTENT));
                        }else{
                            throw new FdevException("云测试平台getTestFinishDate接口请求失败----- " + response.get(Dict.MSG) + " ----时间==" + TimeUtil.formatTodayHs());
                        }
                        page++;
                    }

                }else{

                    response = JSONObject.parseObject((String)ipmpUtils.send(ipmpUnitNos,getTestFinishDate) , Map.class);
                    //返回为true 请求成功
                    if((boolean)response.get(Dict.RESULT)) {
                        content = (List<Map<String, Object>>) response.get(Dict.CONTENT);
                    }else{
                        throw new FdevException("云测试平台getTestFinishDate接口请求失败----- " + response.get(Dict.MSG) + " ----时间==" + TimeUtil.formatTodayHs());
                    }

                }
                if (!CommonUtils.isNullOrEmpty(content)) {
                    contentManage(content);
                }
            }

        }


    }

    public void contentManage(List<Map<String, Object>> content) throws Exception {
        List<XTestIpmpUnit> xTestIpmpUnitList = new ArrayList<>();
        //遍历实施单元
        for ( Map<String, Object> map : content ) {
            String implementUnitNo = (String)map.get(Dict.IMPLEMENTUNITNO);//实施单元编号
            String actualSubmitTestDate = (String)map.get(Dict.ACTUALSUBMITTESTDATE);//实际提交用户测试日期
            String actualTestFinishDate = (String)map.get(Dict.ACTUALTESTFINISHDATE);//实际用户测试完成日期
            String updateTime = (String)map.get(Dict.UPDATETIME);//实施单元更新时间
            if( !CommonUtils.isNullOrEmpty(implementUnitNo) ){
                //获取实施单元
                IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(implementUnitNo);

                if( !CommonUtils.isNullOrEmpty(ipmpUnit) ){
                    //判断实施单元是否由FDEV维护
                    if( "ZH-0748".equals(ipmpUnit.getUsedSysCode()) ){
                        String acturalProductDate = ipmpUnit.getActuralProductDate();//实际投产日期
                        String acturalTestFinishDate = ipmpUnit.getActuralTestFinishDate();//实际用户测试完成日期
                        String acturalTestStartDate = ipmpUnit.getActuralTestStartDate();//实际提交用户测试日期
                        String planProductDate = ipmpUnit.getPlanProductDate();//计划投产日期
                        //测试完成日期为空
                        if( CommonUtils.isNullOrEmpty(actualTestFinishDate) ){
                            // 原来测试完成日期不为空 投产日期为空
                            if(!CommonUtils.isNullOrEmpty(acturalTestFinishDate)
                                    && CommonUtils.isNullOrEmpty(acturalProductDate)){
                                ipmpUnit.setActuralTestFinishDate(actualTestFinishDate);
                                ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                                //更新IPMP
                                updateImplUnit(ipmpUnit,"");

                                //记录日志 测试完成日期置空原值为 acturalTestFinishDate
                                xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                                        ,actualTestFinishDate,updateTime,"succeed","测试完成日期置空原值为=" + acturalTestFinishDate ));

                                //归档提测单
                                testOrderService.businessFileTestOrder(ipmpUnit);
                            }
                            continue;
                        }

                        //投产日期不为空，则判断测试完成日期不能大于投产日期
                        if( !CommonUtils.isNullOrEmpty(acturalProductDate)
                                && !CommonUtils.getJudgementDate(actualTestFinishDate,acturalProductDate)){
                            //记录日志 用户测试完成日期大于投产日期
                            xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                                    ,actualTestFinishDate,updateTime,"error","用户测试完成日期大于投产日期"));
                        }else{
                            //提测日期不为空
                            if( !CommonUtils.isNullOrEmpty(acturalTestStartDate) ){
                                //提测日期不大于 测试完成日期
                                if(CommonUtils.getJudgementDate(acturalTestStartDate,actualTestFinishDate)){
                                    //校验实施单元是否核算
                                    if( ipmpUtilsService.getUfpCountFlag( ipmpUnit.getImplUnitNum() ) ){
                                        //发送未核算邮件
                                        sendEmailDemandService.sendEmailIpmpUnitCheck(ipmpUnit.getImplUnitNum());
                                        //记录日志 实施单元未核算
                                        xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                                                ,actualTestFinishDate,updateTime,"error","实施单元未核算" ));
                                        continue;
                                    }
                                    ipmpUnit.setActuralTestFinishDate(actualTestFinishDate);
                                    ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                                    //更新IPMP
                                    //若投产延期，则不推送实际投产日期
                                    if(!CommonUtils.isNullOrEmpty(acturalProductDate) &&
                                            !CommonUtils.getJudgementDate(acturalProductDate,planProductDate)
                                            &&( CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage())
                                                || !ipmpUnit.getConfirmDelayStage().contains(Dict.PRODUCTDELAY)
                                                )){
                                        ipmpUnit.setActuralProductDate("");
                                    }
                                    updateImplUnit(ipmpUnit,"");
                                    //记录日志 更新成功
                                    xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                                            ,actualTestFinishDate,updateTime,"succeed","更新成功" ));
                                    //归档提测单
                                    testOrderService.businessFileTestOrder(ipmpUnit);
                                }else{
                                    //记录日志 提交用户测试日期大于用户测试完成日期
                                    xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                                            ,actualTestFinishDate,updateTime,"error","提交用户测试日期大于用户测试完成日期"));
                                }
                            }else {
                                //记录日志 该实施单元提交用户测试日期为空
                                xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                                        ,actualTestFinishDate,updateTime,"error","该实施单元提交用户测试日期为空"));
                            }
                        }

                    } else {
                        //记录日志 该实施单元不由FDEV维护
                        xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                                ,actualTestFinishDate,updateTime,"error","该实施单元不由FDEV维护"));
                    }

                } else {
                    //记录日志 不存在该实施单元
                    xTestIpmpUnitList.add(getXTestIpmpUnit(implementUnitNo,actualSubmitTestDate
                            ,actualTestFinishDate,updateTime,"error","不存在该实施单元"));
                }
            }
        }
        //保存日志
        ipmpUnitDao.saveXTestIpmpUnit(xTestIpmpUnitList);
    }

    public XTestIpmpUnit getXTestIpmpUnit(String implementUnitNo,String actualSubmitTestDate
            ,String actualTestFinishDate,String updateTime,String status ,String msg){
        XTestIpmpUnit xTestIpmpUnit = new XTestIpmpUnit();
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        xTestIpmpUnit.setId(id);
        xTestIpmpUnit.setImplUnitNum(implementUnitNo);
        xTestIpmpUnit.setActualTestStartDate(actualSubmitTestDate);
        xTestIpmpUnit.setActualTestFinishDate(actualTestFinishDate);
        xTestIpmpUnit.setTestUpdateTime(updateTime);
        xTestIpmpUnit.setSyncTime(TimeUtil.formatTodayHs());
        xTestIpmpUnit.setStatus(status);
        xTestIpmpUnit.setMsg(msg);
        return xTestIpmpUnit;
    }

    @Override
    public List<Map<String, Object>> getCloudCheckers(Map<String, Object> params) throws Exception {
        return ipmpUtilsService.getCloudCheckers();
    }

    @Override
    public List<Map<String, Object>> getSchemeReview(Map<String, Object> params) throws Exception {
        return ipmpUtilsService.getSchemeReview();
    }

    /**
     * 调整排期
     * @param ipmpUnit
     * @param businessEmail
     */
    @Override
    public String adjustDate(IpmpUnit ipmpUnit, MultipartFile[] businessEmail) throws Exception {
        String planDevelopDateAdjust = ipmpUnit.getPlanDevelopDateAdjust();
        String planTestStartDateAdjust = ipmpUnit.getPlanTestStartDateAdjust();
        String planTestFinishDateAdjust = ipmpUnit.getPlanTestFinishDateAdjust();
        String planProductDateAdjust = ipmpUnit.getPlanProductDateAdjust();
        //若实施单元延期但未确认，则点击申请调整按钮后，计划投产日期（调）字段为必填
        String id = ipmpUnit.getId();
        IpmpUnit fdevUnit = ipmpUnitDao.queryIpmpUnitByNo(id);
        if(isDelayNoConfirm(fdevUnit)){
            if(CommonUtils.isNullOrEmpty(planProductDateAdjust)){
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[]{"planProductDateAdjust"});
            }
        }
        String planDevelopDate = fdevUnit.getPlanDevelopDate();
        String planTestStartDate = fdevUnit.getPlanTestStartDate();
        String planTestFinishDate = fdevUnit.getPlanTestFinishDate();
        String planProductDate = fdevUnit.getPlanProductDate();
        String acturalDevelopDate = fdevUnit.getActuralDevelopDate();
        String acturalTestStartDate = fdevUnit.getActuralTestStartDate();
        String acturalTestFinishDate = fdevUnit.getActuralTestFinishDate();
        String acturalProductDate = fdevUnit.getActuralProductDate();
        //控制计划调整日期的日期选择框应符合规则，须同时满足两个条件：
        // a、计划启动日期（调）"   <= ”计划提交用户测试日期（调）“ <= ”计划用户测试完成日期（调）“ <= ”计划投产日期（调）（若计划调整日期为空，则与原计划日期作比较）。
        // b、计划调整日期 >= 对应的原实际日期。
        checkAdJustDate(planDevelopDateAdjust,planTestStartDateAdjust,planTestFinishDateAdjust,planProductDateAdjust,
                planDevelopDate, planTestStartDate, planTestFinishDate, planProductDate);
        if(!CommonUtils.isNullOrEmpty(planDevelopDateAdjust) && !CommonUtils.isNullOrEmpty(acturalDevelopDate)){
            if(!CommonUtils.getJudgementDate(acturalDevelopDate, planDevelopDateAdjust)){
                throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"计划启动日期（调）","计划启动日期（调）不可早于实际启动日期"});
            }
        }
        if(!CommonUtils.isNullOrEmpty(planTestStartDateAdjust) && !CommonUtils.isNullOrEmpty(acturalTestStartDate)){
            if(!CommonUtils.getJudgementDate(acturalTestStartDate, planTestStartDateAdjust)){
                throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"计划提交用户测试日期（调）","计划提交用户测试日期（调）不可早于实际提交用户测试日期"});
            }
        }
        if(!CommonUtils.isNullOrEmpty(planTestFinishDateAdjust) && !CommonUtils.isNullOrEmpty(acturalTestFinishDate)){
            if(!CommonUtils.getJudgementDate(acturalTestFinishDate, planTestFinishDateAdjust)){
                throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"计划用户测试完成日期（调）","计划用户测试完成日期（调）不可早于实际用户测试完成日期"});
            }
        }
        if(!CommonUtils.isNullOrEmpty(planProductDateAdjust) && !CommonUtils.isNullOrEmpty(acturalProductDate)){
            if(!CommonUtils.getJudgementDate(acturalProductDate, planProductDateAdjust)){
                throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"计划投产日期（调）","计划投产日期（调）不可早于实际投产日期"});
            }
        }
        //上传文件，记录文件名及文件存储路径
        if(!CommonUtils.isNullOrEmpty(businessEmail)){
            for (MultipartFile multipartFile : businessEmail) {
                if (multipartFile.getSize() >= (10*1024*1024)) {
                    throw new FdevException(ErrorConstants.FILE_INFO_SIZE_FILE);
                }
            }
            //path:环境/ipmpUnit/实施单元编号/文件名
            String pathCommon = docFolder +"/ipmpUnit" + "/" + fdevUnit.getImplUnitNum() +"/";
            User user = CommonUtils.getSessionUser();
            List<String> listPathAll = fdocmanageService.uploadFilestoMinio("fdev-demand", pathCommon, businessEmail, user);
            if(CommonUtils.isNullOrEmpty(listPathAll)) {
                throw new FdevException(ErrorConstants.DOC_ERROR_UPLOAD, new String[]{"上传文件失败,请重试!"});
            }
            List<Map<String, String>> fileInfo = new ArrayList<>();
            for(String path : listPathAll){
                HashMap<String, String> file = new HashMap<>();
                String fileName = path.substring(path.lastIndexOf("/"));
                file.put("businessEmailName",fileName.substring(1));
                file.put("businessEmailPath",path);
                fileInfo.add(file);
            }
            //此处文件只做新增，删除另有接口
            List<Map<String, String>> businessEmail1 = fdevUnit.getBusinessEmail();
            if(CommonUtils.isNullOrEmpty(businessEmail1)){
                businessEmail1 = new ArrayList<>();
            }
            businessEmail1.addAll(fileInfo);
            fdevUnit.setBusinessEmail(businessEmail1);
        }
        //字段入库
        fdevUnit.setPlanDevelopDateAdjust(planDevelopDateAdjust);
        fdevUnit.setPlanTestStartDateAdjust(planTestStartDateAdjust);
        fdevUnit.setPlanTestFinishDateAdjust(planTestFinishDateAdjust);
        fdevUnit.setPlanProductDateAdjust(planProductDateAdjust);
        fdevUnit.setImplChangeType(ipmpUnit.getImplChangeType());
        fdevUnit.setImplChangeReason(ipmpUnit.getImplChangeReason());
        return ipmpUnitDao.updateIpmpUnit(fdevUnit);
    }

    @Override
    public void confirmDelay(Map<String, Object> params) throws Exception {
        //将选中的字段加入到“确认延期阶段”字段中
        String implDelayType = (String) params.get("implDelayType");
        String implDelayTypeName = (String)params.get(Dict.IMPLDELAYTYPENAME);
        String implDelayReason = (String) params.get("implDelayReason");
        String id = (String) params.get(Dict.ID);
        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitByNo(id);
        List<String> confirmDelayStage = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(params.get("confirmDelayStage"))){
            confirmDelayStage = (List<String>) params.get("confirmDelayStage");
            ipmpUnit.setConfirmDelayStage(confirmDelayStage);
        }
        ipmpUnit.setImplDelayType(implDelayType);
        ipmpUnit.setImplDelayTypeName(implDelayTypeName);
        ipmpUnit.setImplDelayReason(implDelayReason);
        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
        //若计划投产日期确认延期，则还需要将实际投产日期同步至ipmp
        if(CommonUtils.isNullOrEmpty(params.get("confirmDelayStage")) || !confirmDelayStage.contains(Dict.PRODUCTDELAY)){
            ipmpUnit.setActuralProductDate("");
        }
        User user = CommonUtils.getSessionUser();
        updateImplUnit(ipmpUnit,user.getUser_name_en());
    }

    /**
     *删除业务确认邮件
     * @param params
     */
    @Override
    public void deleteEmailFile(Map<String, Object> params) throws Exception {
        String id = (String) params.get(Dict.ID);
        String businessEmailName = (String) params.get("businessEmailName");
        String businessEmailPath = (String) params.get("businessEmailPath");
        //从表中删除
        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitByNo(id);
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getBusinessEmail())){
            List<Map<String, String>> businessEmail = ipmpUnit.getBusinessEmail();
            for(int i = 0; i < businessEmail.size(); i++){
                String path = businessEmail.get(i).get("businessEmailPath");
                if(businessEmailName.equals(businessEmail.get(i).get("businessEmailName")) && businessEmailPath.equals(path)){
                    businessEmail.remove(i);
                    ipmpUnit.setBusinessEmail(businessEmail);
                    ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                    //从minio中将文件删除
                    fdocmanageService.deleteFiletoMinio("fdev-demand",path,CommonUtils.getSessionUser());
                    break;
                }
            }

        }
    }

    /**
     *根据实施单元编号和任务集编号校验是否可以挂接
     */
    @Override
    public Boolean getImplUnitRelatSpFlag(String implUnitNum, String prjNum) throws Exception {
        return ipmpUtilsService.getImplUnitRelatSpFlag( implUnitNum,  prjNum);
    }

    /**
     * 检查调整后的日期是否符合规则
     * @param planDevelopDateAdjust
     * @param planTestStartDateAdjust
     * @param planTestFinishDateAdjust
     * @param planProductDateAdjust
     * @param planDevelopDate
     * @param planTestStartDate
     * @param planTestFinishDate
     * @param planProductDate
     * @throws ParseException
     */
    public void checkAdJustDate(String planDevelopDateAdjust, String planTestStartDateAdjust, String planTestFinishDateAdjust,
                                   String planProductDateAdjust, String planDevelopDate, String planTestStartDate,
                                   String planTestFinishDate, String planProductDate) throws ParseException {
        String date1 = planDevelopDateAdjust;
        String date2 = planTestStartDateAdjust;
        String date3 = planTestFinishDateAdjust;
        String date4 = planProductDateAdjust;
        if(CommonUtils.isNullOrEmpty(planDevelopDateAdjust)){
            date1 = planDevelopDate;
        }
        if(CommonUtils.isNullOrEmpty(planTestStartDateAdjust)){
            date2 = planTestStartDate;
        }
        if(CommonUtils.isNullOrEmpty(planTestFinishDateAdjust)){
            date3 = planTestFinishDate;
        }
        if(CommonUtils.isNullOrEmpty(planProductDateAdjust)){
            date4 = planProductDate;
        }
        //保证date1 <= date2 <= date3 <= date4
        if(!CommonUtils.getJudgementDate(date1, date2)){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"","计划启动开发日期应不晚于计划提交用户测试日期"});
        }
        if(!CommonUtils.getJudgementDate(date2, date3)){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"","计划提交用户测试日期应不晚于计划用户测试完成日期"});
        }
        if(!CommonUtils.getJudgementDate(date3, date4)){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"","计划用户测试完成日期应不晚于计划投产日期"});
        }

    }

    /**
     * 判断实施单元是否为：投产延期且未确认延期
     * @return
     */
    public Boolean isDelayNoConfirm(IpmpUnit ipmpUnit) throws ParseException {
        if(!CommonUtils.isNullOrEmpty(ipmpUnit.getActuralTestFinishDate()) && !CommonUtils.isNullOrEmpty(ipmpUnit.getActuralProductDate())){
            if(!CommonUtils.getJudgementDate(ipmpUnit.getActuralProductDate(),ipmpUnit.getPlanProductDate())
                    && (CommonUtils.isNullOrEmpty(ipmpUnit.getConfirmDelayStage()) || !ipmpUnit.getConfirmDelayStage().contains(Dict.PRODUCTDELAY))){
                return true;
            }
        }
        return false;
    }

    public Integer unitStatusToInteger(String statusName) {
        switch (statusName) {
            case "评估中":
                return 1;
            case "待实施":
                return 2;
            case "开发中":
                return 3;
            case "业务测试中":
                return 5;
            case "业务测试完成":
                return 6;
            case "已投产":
                return 7;
            case "已撤销":
                return 9;
            default:
                return 0;
        }
    }
}
