package com.spdb.fdev.fdemand.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.*;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.IpmpUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.dao.IImplementUnitDao;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpUnitDao;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpUnitUpdateDao;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.fdemand.spdb.unit.DealTaskUnit;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class TestIpmpUnitServiceImpl implements ITestIpmpUnitService {

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
    ILogService logService;

    @Autowired
    IpmpUtils ipmpUtils;

    @Autowired
    ICommonBusinessService commonBusinessService;

    @Autowired
    private IRoleService roleService;

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


    @Override
    public Map<String, Object> queryIpmpUnitByDemandId(Map<String, Object> params) throws Exception {
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
                List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryCanceledFdevUnitByDemandId(demandBaseInfo.getId());

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
            }
        }
        ipmpMap.put(Dict.DATA,CommonUtils.isNullOrEmpty(unitList) ? new ArrayList<>() : unitList );
        ipmpMap.put(Dict.DEMAND_TYPE,demandType);
        return ipmpMap;
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
        return ipmpUnit;
    }

    @Override
    public String updateIpmpUnit(Map<String,Object> params) throws Exception {

        String ipmpUnitNo = (String) params.get(Dict.IMPLUNITNUM);//实施单元编号
        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnitNo);
        //获取人员信息 校验权限
        User user = CommonUtils.getSessionUser();
        boolean isDemandManager = roleService.isDemandManager();//是否需求管理员
        boolean isIpmpUnitLeader = roleService.isIpmpUnitLeader(ipmpUnit, user.getUser_name_en());//是否为该实施单元牵头人
        if ( !isDemandManager && !isIpmpUnitLeader) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }
        //非FDEV实施单元不可编辑
        if("3".equals(ipmpUnit.getLeaderFlag()) || (!CommonUtils.isNullOrEmpty(ipmpUnit.getUsedSysCode()) && !ipmpUnit.getUsedSysCode().equals(appno))){
            throw new FdevException(ErrorConstants.IPMP_ERROR_NOT_UPDATE);
        }

        String ipmpStatusName = ipmpUnit.getImplStatusName();
        if("已投产".equals(ipmpStatusName) || "暂缓".equals(ipmpStatusName) || "已撤销".equals(ipmpStatusName) || "暂存".equals(ipmpStatusName) ){
            throw new FdevException(ErrorConstants.IPMP_ERROR_CANNOT_UPDATE);
        }

        String syncFlag = (String) params.get(Dict.SYNCFLAG);//是否同步IPMP标识 1 为同步
        String implDelayTypeName = (String) params.get(Dict.IMPLDELAYTYPENAME);//实施延期原因分类
        String implDelayType = (String) params.get(Dict.IMPLDELAYTYPE);//实施延期原因分类Type
        String implDelayReason = (String) params.get(Dict.IMPLDELAYREASON);//实施延期原因
        String implLeader = (String) params.get(Dict.IMPLLEADER);//实施牵头人域账号
        String implLeaderName = (String) params.get(Dict.IMPLLEADERNAME);//实施牵头人中文姓名
        String headerUnitName = (String) params.get(Dict.HEADERUNITNAME);//牵头单位
        String headerTeamName = (String) params.get(Dict.HEADERTEAMNAME);//实施牵头团队
        String testLeaderName = (String) params.get(Dict.TESTLEADERNAME);//测试牵头人中文姓名
        String testLeader = (String) params.get(Dict.TESTLEADER);//测试牵头人域账号
        String testLeaderEmail = (String) params.get(Dict.TESTLEADEREMAIL);//测试牵头人邮箱
        String prjNum = (String) params.get(Dict.PRJNUM);//项目编号
        String planPrjName = (String) params.get(Dict.PLANPRJNAME);//拟纳入项目名称
        Double expectOwnWorkload = Double.valueOf(params.get(Dict.EXPECTOWNWORKLOAD).toString()) ;//预期行内人员工作量
        Double expectOutWorkload = Double.valueOf(params.get(Dict.EXPECTOUTWORKLOAD).toString()) ;//预期公司人员工作量
        String leaderGroup = (String) params.get(Dict.LEADERGROUP);//牵头小组
        String planInnerTestDate = (String) params.get(Dict.PLANINNERTESTDATE);//计划提交内测时间

        //判断实施单元是否延期
        if( implIsDelay(ipmpUnit) ){
            if( !CommonUtils.isNullOrEmpty(implDelayType) && !CommonUtils.isNullOrEmpty(implDelayReason) ){
                //实施延期原因分类、实施延期原因有修改 同步至 IPMP
                if(!implDelayType.equals(ipmpUnit.getImplDelayType()) ||
                        !implDelayReason.equals(ipmpUnit.getImplDelayReason()) ){
                    Map<String, String> updateImplUnitMap = new HashMap<>();
                    updateImplUnitMap.put(Dict.ACTURALDEVELOPDATE,ipmpUnit.getActuralDevelopDate());
                    updateImplUnitMap.put(Dict.ACTURALTESTSTARTDATE,ipmpUnit.getActuralTestStartDate());
                    updateImplUnitMap.put(Dict.ACTURALTESTFINISHDATE,ipmpUnit.getActuralTestFinishDate());
                    updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,ipmpUnit.getActuralProductDate());
                    updateImplUnitMap.put(Dict.IMPLDELAYTYPE,implDelayType);
                    updateImplUnitMap.put(Dict.IMPLDELAYREASON,implDelayReason);
                    updateImplUnitMap.put(Dict.IMPLUNITNUM,ipmpUnit.getImplUnitNum());
                    updateImplUnitMap.put(Dict.USERTYPE,CommonUtils.isInt(user.getEmail()) ? "0" : "1");
                    updateImplUnitMap.put(Dict.USER_NAME_EN,user.getUser_name_en());
                    ipmpUtilsService.updateImplUnit(updateImplUnitMap);
                }
            }else{
                throw new FdevException(ErrorConstants.IPMP_ERROR_DELAY);
            }
        }
        ipmpUnit.setLeaderGroup(leaderGroup);
        ipmpUnit.setPlanInnerTestDate(planInnerTestDate);
        //牵头小组 计划内测不为空 且原来字段至少其一为空 创建工单
        if(!CommonUtils.isNullOrEmpty(leaderGroup) && !CommonUtils.isNullOrEmpty(planInnerTestDate)){
            commonBusinessService.createYuhengOrder(ipmpUnit.getId(), planInnerTestDate , ipmpUnit.getPlanTestStartDate(),
                    ipmpUnit.getPlanProductDate(), ipmpUnit.getImplContent() + "创建工单", leaderGroup ,"");
        }
        //同步IPMP仅能同步一次的字段 且未同步过
        if( Constants.ONE.equals(syncFlag) && !syncFlag.equals(ipmpUnit.getSyncFlag())){
            //判断同步IPMP字段均不能为空
            if( !CommonUtils.isNullOrEmpty(implLeader) && !CommonUtils.isNullOrEmpty(headerTeamName) &&
                    !CommonUtils.isNullOrEmpty(testLeaderName) &&  !CommonUtils.isNullOrEmpty(testLeader) &&
                    !CommonUtils.isNullOrEmpty(testLeaderEmail) &&  !CommonUtils.isNullOrEmpty(prjNum) &&
                    !CommonUtils.isNullOrEmpty(expectOwnWorkload) &&  !CommonUtils.isNullOrEmpty(expectOutWorkload) ){
                //同步IPMP
                params.put(Dict.USERTYPE,CommonUtils.isInt(user.getEmail()) ? "0" : "1");
                params.put(Dict.USER_NAME_EN,user.getUser_name_en());
                ipmpUtilsService.updateImplUnitOther(params);
            }else{
                throw new FdevException(ErrorConstants.IPMP_ERROR_SYNC_NULL);
            }
            ipmpUnit.setSyncFlag(syncFlag);
        }
        ipmpUnit.setImplLeader(implLeader);
        ipmpUnit.setImplLeaderName(implLeaderName);
        ipmpUnit.setHeaderUnitName(headerUnitName);
        ipmpUnit.setHeaderTeamName(headerTeamName);
        ipmpUnit.setTestLeaderName(testLeaderName);
        ipmpUnit.setTestLeader(testLeader);
        ipmpUnit.setTestLeaderEmail(testLeaderEmail);
        ipmpUnit.setPrjNum(prjNum);
        ipmpUnit.setPlanPrjName(planPrjName);
        ipmpUnit.setExpectOwnWorkload(expectOwnWorkload);
        ipmpUnit.setExpectOutWorkload(expectOutWorkload);
        ipmpUnit.setImplDelayTypeName(implDelayTypeName);
        ipmpUnit.setImplDelayType(implDelayType);
        ipmpUnit.setImplDelayReason(implDelayReason);
        ipmpUnit.setLeaderGroup(leaderGroup);
        ipmpUnit.setPlanInnerTestDate(planInnerTestDate);
        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
        //更新 需求计划内测时间
        if(!CommonUtils.isNullOrEmpty(planInnerTestDate)){
            //查询需求
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum());
            demandBaseInfo.setPlan_inner_test_date(CommonUtils.getJudgementDate(planInnerTestDate,demandBaseInfo.getPlan_inner_test_date())
                    && !CommonUtils.isNullOrEmpty(demandBaseInfo.getPlan_inner_test_date()) ? demandBaseInfo.getPlan_inner_test_date() : planInnerTestDate) ;
            demandBaseInfoDao.updateDemandBaseInfo(demandBaseInfo);
        }
        return ipmpUnitNo;
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
        //根据实施单元编号查询研发单元
        List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryByImplUnitNum(implUnitNum,"");
        //所有研发单元编号
        List<String> fdevUnitNoList = new ArrayList<>();
        for(FdevImplementUnit fdevUnit : fdevUnitList)
            fdevUnitNoList.add(fdevUnit.getFdev_implement_unit_no());

        return  taskService.queryDetailByUnitNo(fdevUnitNoList);
    }

    @Override
    public void syncAllIpmpInfo(Map<String, Object> params) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1 );
        String dateStr =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
        //1、从ipmpUnit_update表获取时间参数
        String updateDate = "2021-01-01 00:00:00";
        String updateTextDate = "2021-01-01 00:00:00";
        //2、发接口获取批量更新数据
        List result = null;
        Map param = null;
        //需求编号
        Set<String> informationNumSet = new HashSet<>();
        //新增的实施单元
        List<IpmpUnit> newIpmpUnitList = new ArrayList<>();
        //全部实施单元列表
        List<IpmpUnit> allIpmpUnitList = new ArrayList<>();

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
                    JSONObject jsonObject = (JSONObject) result.get(i);
                    String implUnitNum = (String) jsonObject.get("implUnitNum");
                    //保留科技实施单元
                    if(!CommonUtils.isNullOrEmpty(implUnitNum) && implUnitNum.contains("科技")){
                        IpmpUnit ipmpUnit = JSONObject.toJavaObject(jsonObject, IpmpUnit.class);
                        if( CommonUtils.isNullOrEmpty(noSyncPrjNum) || !noSyncPrjNum.contains(ipmpUnit.getPrjNum())){
                            ipmpUnit.setImplUnitNum(implUnitNum.trim());
                            //去空格
                            ipmpUnit.setInformationNum(ipmpUnit.getInformationNum().trim());
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
                //存在则更新
                if(!CommonUtils.isNullOrEmpty(unit)){
                    getIpmpUnit(ipmpUnit, unit);
                    //牵头人标识
                    queryLeaderFlag( unit , userMap );
                    //去掉日期后面多余 00:00:00 保留格式 yyyy-MM-dd
                    dealDate(unit);
                    ipmpUnitDao.updateIpmpUnit(unit);
                    allIpmpUnitList.add(unit);
                }else{
                    //牵头人标识
                    queryLeaderFlag( ipmpUnit , userMap );
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

            /*//所有本次同步涉及的需求
            for (String informationNum : informationNumSet) {
                //查询需求
                DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryByOaContactNo(informationNum);
                //查询实施单元
                Map<String, Object> informationNumMap = new HashMap<>();
                informationNumMap.put(Dict.INFORMATIONNUM,informationNum);
                List<IpmpUnit> unitList = (List<IpmpUnit>)ipmpUnitDao.queryIpmpUnitAllByDemandNum(informationNumMap).get(Dict.DATA);
                if(!CommonUtils.isNullOrEmpty(unitList)){
                    if(!CommonUtils.isNullOrEmpty(demandBaseInfo) &&
                            (!CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())
                                    || DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue().equals(demandBaseInfo.getDemand_status_normal())) ){
                        updateDemand(demandBaseInfo,unitList, userMap);
                        demandBaseInfoDao.updateDemandBaseInfo(demandBaseInfo);
                        //更新需求实际日期 状态
                        updateDemandStatus(demandBaseInfo);
                    }else if (CommonUtils.isNullOrEmpty(demandBaseInfo)){
                        demandBaseInfo = new DemandBaseInfo();
                        //新增需求
                        saveDemand(demandBaseInfo,unitList,userMap);
                        demandBaseInfoDao.save(demandBaseInfo);
                        //更新需求实际日期 状态
                        updateDemandStatus(demandBaseInfo);
                        //发送待办邮件
                        sendEmailDemandService.sendEmailUpdateDemand(demandBaseInfo);
                    } else {
                        //存量需求 删除实施单元
                        ipmpUnitDao.removeIpmpUnitByDemandNum(demandBaseInfo.getOa_contact_no());
                    }
                }
            }
            //判断本次实施单元是否存在延期
            for(IpmpUnit ipmpUnit :allIpmpUnitList){
                //实施单元编号是否包含科技
                if(!ipmpUnit.getImplUnitNum().contains("科技")){
                    //判断是否延期
                    if( implIsDelayEmail(ipmpUnit) ){
                        //发送延期邮件
                        sendEmailDemandService.sendEmailImplDelay(ipmpUnit,demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum()),userMap);
                    }
                }
            }*/

        }

        /*//更新时间表
        IpmpUnitUpdate ipmpUnitUpdateNew = new IpmpUnitUpdate();
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.UPDATEDATE)) &&  !CommonUtils.isNullOrEmpty(param.get(Dict.UPDATETEXTDATE))){
            ipmpUnitUpdateNew.setUpdateDate((String) param.get(Dict.UPDATEDATE));
            ipmpUnitUpdateNew.setUpdateTextDate((String) param.get(Dict.UPDATETEXTDATE));
        }else {
            ipmpUnitUpdateNew.setUpdateDate(updateDate);
            ipmpUnitUpdateNew.setUpdateTextDate(updateTextDate);
        }
        ipmpUnitUpdateDao.save(ipmpUnitUpdateNew);*/
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
        //判断实施单元状态是否为暂缓 撤销 暂存 并修改研发单元 任务状态
        checkImplStatus(newIpmpUnit,unit);
        //保存已IPMP为准的字段
        unit.setImplStatusName(newIpmpUnit.getImplStatusName());//实施状态
        unit.setImplContent(newIpmpUnit.getImplContent());//实施单元内容
        unit.setPlanDevelopDate(newIpmpUnit.getPlanDevelopDate());//计划启动开发日期
        unit.setPlanTestStartDate(newIpmpUnit.getPlanTestStartDate());//计划提交用户测试日期
        unit.setPlanTestFinishDate(newIpmpUnit.getPlanTestFinishDate());//计划用户测试完成日期
        unit.setPlanProductDate(newIpmpUnit.getPlanProductDate());//计划投产日期
        unit.setRelateSysName(newIpmpUnit.getRelateSysName());//涉及系统名称
        unit.setRelateSysCode(newIpmpUnit.getRelateSysCode());//涉及系统编号
        unit.setActualOwnUfworkload(newIpmpUnit.getActualOwnUfworkload());//行内人员实际工作量（非功能性）（人月）
        unit.setActualOutUfworkload(newIpmpUnit.getActualOutUfworkload());//公司人员实际工作量（非功能性）（人月）
        unit.setActualOwnFworkload(newIpmpUnit.getActualOwnFworkload());//行内人员实际工作量（功能性）（人月）
        unit.setActualOutFworkload(newIpmpUnit.getActualOutFworkload());//公司人员实际工作量（功能性）（人月）
        unit.setImplDelayTypeName(newIpmpUnit.getImplDelayTypeName());//实施延期原因分类
        unit.setImplDelayReason(newIpmpUnit.getImplDelayReason());//实施延期原因

        //判断仅能同步一次的字段是否同步过
        if("1".equals(unit.getSyncFlag())){
            //同步过 已IPMP为准
            unit.setImplLeader(newIpmpUnit.getImplLeader());//实施牵头人域账号
            unit.setImplLeaderName(newIpmpUnit.getImplLeaderName());//实施牵头人中文姓名
            unit.setTestLeaderName(newIpmpUnit.getTestLeaderName());//测试牵头人中文姓名
            unit.setHeaderTeamName(newIpmpUnit.getHeaderTeamName());//实施牵头团队
            unit.setHeaderUnitName(newIpmpUnit.getHeaderUnitName());//牵头单位
            unit.setTestLeader(newIpmpUnit.getTestLeader());//测试牵头人域账号
            unit.setTestLeaderEmail(newIpmpUnit.getTestLeaderEmail());//测试牵头人邮箱
            unit.setPrjNum(newIpmpUnit.getPrjNum());//项目编号
            unit.setPlanPrjName(newIpmpUnit.getPlanPrjName());//拟纳入项目名称
            unit.setExpectOwnWorkload(newIpmpUnit.getExpectOwnWorkload());//预期行内人员工作量
            unit.setExpectOutWorkload(newIpmpUnit.getExpectOutWorkload());//预期公司人员工作量
        }else{
            //字段为空已IPMP为准
            if(CommonUtils.isNullOrEmpty(unit.getImplLeader())){
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
            if(CommonUtils.isNullOrEmpty(unit.getImplLeaderName())){
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

            if(CommonUtils.isNullOrEmpty(unit.getHeaderTeamName())) unit.setHeaderTeamName(newIpmpUnit.getHeaderTeamName());//实施牵头团队
            if(CommonUtils.isNullOrEmpty(unit.getHeaderUnitName())) unit.setHeaderUnitName(newIpmpUnit.getHeaderUnitName());//牵头单位
            if(CommonUtils.isNullOrEmpty(unit.getTestLeaderName())) unit.setTestLeaderName(newIpmpUnit.getTestLeaderName());//测试牵头人中文姓名
            if(CommonUtils.isNullOrEmpty(unit.getTestLeader())) unit.setTestLeader(newIpmpUnit.getTestLeader());//测试牵头人域账号
            if(CommonUtils.isNullOrEmpty(unit.getTestLeaderEmail())) unit.setTestLeaderEmail(newIpmpUnit.getTestLeaderEmail());//测试牵头人邮箱
            if(CommonUtils.isNullOrEmpty(unit.getPrjNum())) unit.setPrjNum(newIpmpUnit.getPrjNum());//项目编号
            if(CommonUtils.isNullOrEmpty(unit.getPlanPrjName())) unit.setPlanPrjName(newIpmpUnit.getPlanPrjName());//拟纳入项目名称
            if(CommonUtils.isNullOrEmpty(unit.getExpectOwnWorkload())) unit.setExpectOwnWorkload(newIpmpUnit.getExpectOwnWorkload());//预期行内人员工作量
            if(CommonUtils.isNullOrEmpty(unit.getExpectOutWorkload())) unit.setExpectOutWorkload(newIpmpUnit.getExpectOutWorkload());//预期公司人员工作量
        }

    }

    //牵头人标识
    public void queryLeaderFlag(IpmpUnit unit,Map<String,Map> userMap) throws Exception {
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
            if( initSize == endSize ) leaderFlag = "3";
                //小于 且不为 0 部分是fdev用户
            else if ( endSize < initSize && endSize != 0 ) leaderFlag = "2";
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
                    updateSpectialStatus(unit,IpmpUnitConFdevUnitStatus(newIpmpUnit),"cancelAbort","0");
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
                    updateSpectialStatus(unit,IpmpUnitConFdevUnitStatus(newIpmpUnit),"","0");
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
            //更新实施单元实际日期 状态
            String implUnitNum = fdevImplementUnit.getIpmp_implement_unit_no();
            if(!CommonUtils.isNullOrEmpty(implUnitNum)){
                IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(implUnitNum);
                updateIpmpUnitStatus(ipmpUnit,userNameEn);
            }
        }
        //更新需求实际日期 状态
        updateDemandStatus(demandBaseInfo);
    }


    @Override
    public void updateIpmpUnitStatus(IpmpUnit ipmpUnit,String userNameEn)  throws Exception {
        //修改实施单元状态与实际日期
        if (!CommonUtils.isNullOrEmpty(ipmpUnit)) {
            //获取所有研发单元的状态和时间
            List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryByImplUnitNum(ipmpUnit.getImplUnitNum(),"");
            if (!CommonUtils.isNullOrEmpty(fdevUnitList)) {
                //获取所有研发单元最小阶段
                Integer minStage = fdevUnitList.get(0).getImplement_unit_status_normal();
                //获取所有研发单元最大阶段
                Integer maxStage = fdevUnitList.get( fdevUnitList.size() - 1 ).getImplement_unit_status_normal();
                //评估中的实施单元仅修改状态
                if (IpmpUnitEnum.IpmpUnitConFdevUnitStatus.EVALUATE.getValue().equals(maxStage)) {
                    ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.EVALUATE.getName());
                    ipmpUnitDao.updateIpmpUnit(ipmpUnit);
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

                if(ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(minStage) ){
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
                }
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
                        break;
                    //业务测试中
                    case 5:
                        ipmpUnit.setActuralDevelopDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        ipmpUnit.setActualInnerTestDate(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                        ipmpUnit.setActuralTestStartDate(dealTaskUnit.getRealTestDate(fdevUnitList));
                        ipmpUnit.setActuralTestFinishDate("");
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
                        ipmpUnit.setActuralTestFinishDate(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
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
                        ipmpUnit.setActuralTestFinishDate(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
                        ipmpUnit.setActuralProductDate(dealTaskUnit.getRealProductDate(fdevUnitList));
                        //ipmpUnit.setImplStatusName(IpmpUnitEnum.IpmpUnitConFdevUnitStatus.PRODUCT.getName());
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
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
                //发送延期邮件
                sendEmailDemandService.sendEmailImplDelay(ipmpUnit,demandBaseInfoDao.queryByOaContactNo(ipmpUnit.getInformationNum()),null);

            }
        }
    }

    public void updateImplUnit(IpmpUnit ipmpUnit,String userNameEn)  throws Exception {
        Map<String, String> updateImplUnitMap = new HashMap<>();
        updateImplUnitMap.put(Dict.ACTURALDEVELOPDATE,ipmpUnit.getActuralDevelopDate());
        updateImplUnitMap.put(Dict.ACTURALTESTSTARTDATE,ipmpUnit.getActuralTestStartDate());
        updateImplUnitMap.put(Dict.ACTURALTESTFINISHDATE,ipmpUnit.getActuralTestFinishDate());
        updateImplUnitMap.put(Dict.ACTURALPRODUCTDATE,ipmpUnit.getActuralProductDate());
        updateImplUnitMap.put(Dict.IMPLUNITNUM,ipmpUnit.getImplUnitNum());
        try {
            //判断行内 厂商 1 代表厂商   0 代表 行内
            User user = CommonUtils.getSessionUser();
            updateImplUnitMap.put(Dict.USERTYPE,CommonUtils.isInt(user.getEmail()) ? "0" : "1");
        }catch (Exception e){
            //获取登录信息失败默认 用户为行内 默认类型也是行内
            updateImplUnitMap.put(Dict.USERTYPE, "0");
        }
        updateImplUnitMap.put(Dict.USER_NAME_EN,userNameEn);
        ipmpUtilsService.updateImplUnit(updateImplUnitMap);
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
                //获取暂缓研发单元
                List<FdevImplementUnit> fdevUnitListRecover = implementUnitDao.queryRecoverFdevUnitByDemandId(demandBaseInfo.getId());
                if (!CommonUtils.isNullOrEmpty(fdevUnitListRecover)){
                    //所有研发单元均为暂缓
                    demandBaseInfo.setDemand_status_special( DemandEnum.DemandDeferStatus.DEFER.getValue());
                } else if(!CommonUtils.isNullOrEmpty(implementUnitDao.queryCanceledFdevUnitByDemandId(demandBaseInfo.getId()))){
                    //所有研发单元均为撤销
                    demandBaseInfo.setDemand_status_normal( DemandEnum.DemandStatusEnum.IS_CANCELED.getValue());
                }else{
                    //研发单元为空获取实施单元
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
            if (DemandEnum.DemandStatusEnum.EVALUATE.getValue().equals(minStage)) {
                demandBaseInfo.setDemand_status_normal(minStage);
                demandBaseInfoDao.update(demandBaseInfo);
                return;
            }
            //状态小于UAT则进入 判断是否有一个状态进入开发中，SIT,UAT状态改为对应状态
            if (minStage < ImplementUnitEnum.ImplementUnitStatusEnum.UAT.getValue()) {
                //如果有一个状态进入开发中，SIT,UAT状态改为对应状态 存在大于UAT状态的实施单元 需求置为UAT
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

            if(ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(minStage) ){
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
            }
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
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                    demandBaseInfo.setReal_inner_test_date("");
                    demandBaseInfo.setReal_test_date("");
                    demandBaseInfo.setReal_test_finish_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //SIT
                case 4:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                    demandBaseInfo.setReal_test_date("");
                    demandBaseInfo.setReal_test_finish_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //UAT
                case 5:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                    demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(fdevUnitList));
                    demandBaseInfo.setReal_test_finish_date("");
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //REL
                case 6:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                    demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(fdevUnitList));
                    demandBaseInfo.setReal_test_finish_date(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
                    demandBaseInfo.setReal_product_date("");
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                //已投产
                case 7:
                    demandBaseInfo.setReal_start_date(dealTaskUnit.getRealStartDate(fdevUnitList));
                    demandBaseInfo.setReal_inner_test_date(dealTaskUnit.getRealInnerTestDate(fdevUnitList));
                    demandBaseInfo.setReal_test_date(dealTaskUnit.getRealTestDate(fdevUnitList));
                    demandBaseInfo.setReal_test_finish_date(dealTaskUnit.getRealTestFinishDate(fdevUnitList));
                    demandBaseInfo.setReal_product_date(dealTaskUnit.getRealProductDate(fdevUnitList));
                    demandBaseInfoDao.update(demandBaseInfo);
                    break;
                default:
                    break;
            }
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
        if(!Constants.ONE.equals(productDelayEmail)){
            if(CommonUtils.isDelay(planProductDate,acturalProductDate)) {
                ipmpUnit.setProductDelayEmail(Constants.ONE);
                return true;//投产延期
            }
        }
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
