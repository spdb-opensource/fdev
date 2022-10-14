package com.spdb.fdev.fdemand.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.IpmpUtils;
import com.spdb.fdev.fdemand.spdb.service.IIpmpUtilsService;
import com.spdb.fdev.fdemand.spdb.service.ILogService;
import com.spdb.fdev.fdemand.spdb.service.SendEmailDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RefreshScope
@Service
public class IpmpUtilsServiceImpl implements IIpmpUtilsService {


    @Value("${fdev.ipmp.url}")
    private String ipmpUrl;
    @Value("${fdev.ipmp.userId}")
    private String ipmpUser;
    @Value("${ipmp.increment.appno}")
    private String appno;
    @Value("${ipmp.increment.version}")
    private String version1;
    @Value("${ipmp.increment.version2}")
    private String version2;
    @Value("${ipmp.increment.isAuth}")
    private boolean isAuth;
    //编辑实施单元
    @Value("${ipmp.increment.updateImplUnit}")
    private String updateImplUnit;
    @Value("${ipmp.increment.appKey.updateImplUnit}")
    private String updateImplUnitKey;
    //评估实施单元
    @Value("${ipmp.increment.updateImplUnitOther}")
    private String updateImplUnitOther;
    @Value("${ipmp.increment.appKey.updateImplUnitOther}")
    private String updateImplUnitOtherKey;
    //查询实施单元是否需功能点核算
    @Value("${ipmp.increment.getUfpCountFlag}")
    private String getUfpCountFlag;
    @Value("${ipmp.increment.appKey.getUfpCountFlag}")
    private String getUfpCountFlagKey;
    //查询实施单元上云审核人（条线负责人或处长）
    @Value("${ipmp.increment.getCloudCheckers}")
    private String getCloudCheckers;
    @Value("${ipmp.increment.appKey.getCloudCheckers}")
    private String getCloudCheckersKey;
    //查询技术方案编号
    @Value("${ipmp.increment.getSchemeReview}")
    private String getSchemeReview;
    @Value("${ipmp.increment.appKey.getSchemeReview}")
    private String getSchemeReviewKey;
    //是否上云编辑
    @Value("${ipmp.increment.updateCloudData}")
    private String updateCloudData;
    @Value("${ipmp.increment.appKey.updateCloudData}")
    private String updateCloudDataKey;
    //根据实施单元编号和任务集编号校验是否可以挂接
    @Value("${ipmp.increment.getImplUnitRelatSpFlag}")
    private String getImplUnitRelatSpFlag;
    @Value("${ipmp.increment.appKey.getImplUnitRelatSpFlagKey}")
    private String getImplUnitRelatSpFlagKey;

    @Autowired
    IpmpUtils ipmpUtils;

    @Autowired
    private SendEmailDemandService sendEmailDemandService;

    @Autowired
    ILogService logService;

    @Override
    public Map updateImplUnit(Map<String, String> params) throws Exception {
        Map request = new HashMap();
        String implUnitNum = params.get(Dict.IMPLUNITNUM);//实施单元编号
        String acturalDevelopDate = params.get(Dict.ACTURALDEVELOPDATE);//实际启动开发日期
        String acturalTestStartDate = params.get(Dict.ACTURALTESTSTARTDATE);//实际提交用户测试日期
        String acturalTestFinishDate = params.get(Dict.ACTURALTESTFINISHDATE);//实际用户测试完成日期
        String acturalProductDate = params.get(Dict.ACTURALPRODUCTDATE);//实际投产日期
        String implDelayType = params.get(Dict.IMPLDELAYTYPE);//实施延期原因分类
        String implDelayReason = params.get(Dict.IMPLDELAYREASON);//实施延期原因
        /*//测试牵头人
        if( !CommonUtils.isNullOrEmpty(params.get(Dict.TESTLEADER)) ){
            request.put(Dict.TESTLEADEREMAIL,params.get(Dict.TESTLEADEREMAIL));//测试牵头人邮箱
            request.put(Dict.TESTLEADERNAME,params.get(Dict.TESTLEADERNAME));//测试牵头人中文姓名
            request.put(Dict.TESTLEADER,params.get(Dict.TESTLEADER));//测试牵头人域账号
        }*/
        request.put(Dict.IMPLUNITNUM,implUnitNum);
        //实际日期
        request.put(Dict.ACTURALDEVELOPDATE,acturalDevelopDate);
        request.put(Dict.ACTURALTESTSTARTDATE,acturalTestStartDate);
        request.put(Dict.ACTURALTESTFINISHDATE,acturalTestFinishDate);
        request.put(Dict.ACTURALPRODUCTDATE,acturalProductDate);
        //延期时必填
        request.put(Dict.IMPLDELAYTYPE,!CommonUtils.isNullOrEmpty(implDelayType) ? implDelayType : "");
        request.put(Dict.IMPLDELAYREASON,!CommonUtils.isNullOrEmpty(implDelayReason) ? implDelayReason : "");

        request.put(Dict.UNITDEVMODE,"implunit.dev.mode.02");// 默认送稳态 implunit.dev.mode.01  敏态；implunit.dev.mode.02  稳态
        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(version2,updateImplUnit,updateImplUnitKey,params.get(Dict.USER_NAME_EN),params.get(Dict.USERTYPE)));
        sendMap.put(Dict.REQUEST,request);
        Object response = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) response, Map.class);
        //记录日志
        logService.saveIpmpUnitOperateLog(implUnitNum,updateImplUnit,sendMap,map);
        String isCheck = params.get(Dict.ISCHECK);//校验是否核算
        //判断IPMP是否抛错  抛错原因是否为未核算 自动同步时触发 发送邮件
        // 未核算的错误码30
        if ( "1".equals(isCheck) && "30".equals( map.get(Dict.ERRORCODE)) ) {
            sendEmailDemandService.sendEmailIpmpUnitCheck(implUnitNum);
        }
        return map ;
    }

    @Override
    public void updateImplUnitOther(Map<String, Object> params) throws Exception {
        Map request = new HashMap();
        request.put(Dict.IMPLUNITNUM,params.get(Dict.IMPLUNITNUM));
        request.put(Dict.IMPLLEADER,params.get(Dict.IMPLLEADER));
        request.put(Dict.HEADERTEAM,params.get(Dict.HEADERTEAMNAME));
        /*String testLeader = (String) params.get(Dict.TESTLEADER);//测试牵头人域账号
        if(CommonUtils.isNullOrEmpty(testLeader)){
            request.put(Dict.TESTLEADER,params.get(Dict.IMPLLEADER));//测试牵头人域账号为空 默认送实施单元牵头人
        }else{
            request.put(Dict.TESTLEADER,testLeader);//测试牵头人域账号
        }*/
        request.put(Dict.EXPECTOWNWORKLOAD,params.get(Dict.EXPECTOWNWORKLOAD));
        request.put(Dict.EXPECTOUTWORKLOAD,params.get(Dict.EXPECTOUTWORKLOAD));
        request.put(Dict.PRJNUM,params.get(Dict.PRJNUM));
        //添加字段 项目名称
        request.put(Dict.PLANPRJNAME,params.get(Dict.PLANPRJNAME));
        request.put(Dict.UNITDEVMODE,"implunit.dev.mode.02");// 默认送稳态 implunit.dev.mode.01  敏态；implunit.dev.mode.02  稳态

        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(version1,updateImplUnitOther,updateImplUnitOtherKey, (String) params.get(Dict.USER_NAME_EN),(String)params.get(Dict.USERTYPE)));
        sendMap.put(Dict.REQUEST,request);
        Object response = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) response, Map.class);
        //记录日志
        logService.saveIpmpUnitOperateLog((String)params.get(Dict.IMPLUNITNUM),updateImplUnitOther,sendMap,map);
        //不为0 请求失败抛出错误信息
        if ( !"0".equals( map.get(Dict.STATUS)) ) {
            throw new FdevException((String) map.get(Dict.MESSAGE));
        }
    }

    @Override
    public boolean getUfpCountFlag(String implUnitNum) throws Exception {
        Map request = new HashMap();
        request.put(Dict.IMPLUNITNUM,implUnitNum);
        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(version1,getUfpCountFlag,getUfpCountFlagKey));
        sendMap.put(Dict.REQUEST,request);
        Object response = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) response, Map.class);
        String status = (String) map.get(Dict.STATUS);//0：成功，非0：失败
        //记录日志
        logService.saveIpmpUnitOperateLog(implUnitNum,getUfpCountFlag,sendMap,map);
        if("0".equals(status)) return (boolean)((Map)map.get(Dict.DATA)).get(Dict.NEEDUFPFLAG);
        else return false;
    }

    @Override
    public List<Map<String, Object>> getCloudCheckers() throws Exception {
        Map request = new HashMap();
        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(version1,getCloudCheckers,getCloudCheckersKey));
        sendMap.put(Dict.REQUEST,request);
        Object response = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) response, Map.class);
        //记录日志
        logService.saveIpmpUnitOperateLog("",getCloudCheckers,sendMap,map);
        return (List<Map<String, Object>>) map.get(Dict.DATA);
    }

    @Override
    public List<Map<String, Object>> getSchemeReview() throws Exception {
        Map request = new HashMap();
        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(version1,getSchemeReview,getSchemeReviewKey));
        sendMap.put(Dict.REQUEST,request);
        Object response = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) response, Map.class);
        //记录日志
        logService.saveIpmpUnitOperateLog("",getSchemeReview,sendMap,map);
        return (List<Map<String, Object>>) map.get(Dict.DATA);
    }

    @Override
    public Object updateCloudData(String implUnitNum,String planCloudFlag ,String planTechSchemeKey
                                        ,String prjNum ,String lineChargers,String userId ,String userType) throws Exception {
        Map request = new HashMap();
        request.put(Dict.IMPLUNITNUM,implUnitNum);
        request.put(Dict.PLANCLOUDFLAG,planCloudFlag);
        request.put(Dict.PLANTECHSCHEMEKEY,planTechSchemeKey);
        request.put(Dict.PRJNUM,prjNum);
        request.put(Dict.LINECHARGERS,lineChargers);
        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(version1,updateCloudData,updateCloudDataKey,userId,userType));
        sendMap.put(Dict.REQUEST,request);
        Object response = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) response, Map.class);
        Map data = (Map)map.get(Dict.DATA);
        //记录日志
        logService.saveIpmpUnitOperateLog(implUnitNum,updateCloudData,sendMap,data);
        //不为0 请求失败抛出错误信息
        if ( !"0".equals( data.get(Dict.STATUS)) ) {
            throw new FdevException((String) data.get(Dict.MESSAGE));
        }

        return data;
    }

    @Override
    public Boolean getImplUnitRelatSpFlag(String implUnitNum,String prjNum ) throws Exception {
        Map request = new HashMap();
        request.put(Dict.IMPLUNITNUM,implUnitNum);
        request.put(Dict.PRJNUM,prjNum);
        Map sendMap = new HashMap();
        sendMap.put(Dict.COMMON,getCommon(version1,getImplUnitRelatSpFlag,getImplUnitRelatSpFlagKey));
        sendMap.put(Dict.REQUEST,request);
        Object response = ipmpUtils.send(sendMap,ipmpUrl);
        Map map = JSONObject.parseObject((String) response, Map.class);
        Map data = (Map)map.get(Dict.DATA);
        //记录日志
        logService.saveIpmpUnitOperateLog(implUnitNum,getImplUnitRelatSpFlag,sendMap,map);
        //默认false
        Boolean implUnitRelatSpFlag = false ;
        //请求成功 获取返回结果
        if ( "0".equals( map.get(Dict.STATUS)) ) {
            implUnitRelatSpFlag = (Boolean) data.get(Dict.IMPLUNITRELATSPFLAG);
        }
        return implUnitRelatSpFlag;
    }

    public Map getCommon(String version,String ipmpMethod,String fdevAppkey) {
        Map common = new HashMap();
        common.put(Dict.APPNO,appno);
        common.put(Dict.VERSION,version);
        common.put(Dict.ISAUTH,isAuth);
        common.put(Dict.METHOD,ipmpMethod);
        common.put(Dict.APPKEY,fdevAppkey);
        common.put(Dict.SEQUENCE,System.currentTimeMillis());
        return common;
    }

    public Map getCommon(String version,String ipmpMethod,String fdevAppkey,String userId,String userType) {
        Map common = new HashMap();
        common.put(Dict.APPNO,appno);
        common.put(Dict.VERSION,version);
        common.put(Dict.ISAUTH,isAuth);
        common.put(Dict.METHOD,ipmpMethod);
        common.put(Dict.APPKEY,fdevAppkey);
        common.put(Dict.USERTYPE,userType);
        if(!CommonUtils.isNullOrEmpty(userId)){
            common.put(Dict.IPMPUSERID,userId);
        }else{
            //默认用户
            common.put(Dict.IPMPUSERID,ipmpUser);
        }

        common.put(Dict.SEQUENCE,System.currentTimeMillis());
        return common;
    }
}
