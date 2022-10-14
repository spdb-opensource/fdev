package com.spdb.fdev.fdemand.spdb.controller;


import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DemandBaseInfoUtil;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpUnitDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/test")
public class TestIpmpUnitController {

    @Autowired
    private ITestIpmpUnitService testIpmpUnitService;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IIpmpUnitService ipmpUnitService;

    @Autowired
    private IIpmpUtilsService ipmpUtilsService;

    @Autowired
    private IIpmpUnitDao ipmpUnitDao;
    /**
     * 编辑存量已投产的实施单元上云标识
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/updateCloudFlag")
    public JsonResult updateCloudFlag(@RequestBody Map<String,List<String>> params) throws Exception {
        List<String> ipmpUnitNoList = params.get("ipmpUnitNoList");
        //打印 已修改
        List<String> successList = new ArrayList<>();
        //打印 修改失败
        List<String> errorList = new ArrayList<>();
        for (String ipmpUnitNo : ipmpUnitNoList) {
            IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(ipmpUnitNo);
            //实施单元不为空 状态为已投产 为FDEV维护 上云标识为空
            if(!CommonUtils.isNullOrEmpty(ipmpUnit)){
                if(  "已投产".equals(ipmpUnit.getImplStatusName()) ){
                    if(  "1".equals(ipmpUnit.getSyncFlag()) && "ZH-0748".equals(ipmpUnit.getUsedSysCode()) ){
                        if(  CommonUtils.isNullOrEmpty(ipmpUnit.getCloudFlag()) ||
                                "implunit.cloud.flag.03".equals(ipmpUnit.getCloudFlag()) ){
                            try {
                                String implLeader = ipmpUnit.getImplLeader().split(",")[0];
                                //请求IPMP接口 更新指定实施单元云标识和技术方案
                                ipmpUtilsService.updateCloudData(ipmpUnitNo,"implunit.cloud.flag.02", "",ipmpUnit.getPrjNum(),"",implLeader, "0" );
                                ipmpUnit.setCloudFlag("implunit.cloud.flag.02");
                                ipmpUnit.setCloudFlagName("否");
                                ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                                //未报错  成功
                                successList.add(ipmpUnitNo);
                            }catch (FdevException fdevException){
                                //接口报错 不抛出  记录
                                errorList.add( ipmpUnitNo  + " ,接口报错,报错原因: "+ fdevException.getMessage() );
                            }
                        }else{
                            errorList.add( ipmpUnitNo + " ,上云标识不为空或待定.");
                        }
                    }else{
                        errorList.add( ipmpUnitNo + " ,不由FDEV维护.");
                    }
                } else {
                    errorList.add( ipmpUnitNo + " ,状态不为已投产.");
                }
            }else{
                errorList.add( ipmpUnitNo + " ,为空.");
            }

        }
        for (String ipmpUnitNo : successList) {
            System.out.println(ipmpUnitNo);
        }

        for (String ipmpUnitNo : errorList) {
            System.out.println(ipmpUnitNo);
        }

        return JsonResultUtil.buildSuccess();
    }






    /**
     * 查询存量状态异常需求
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/findDemandBaseInfo")
    public JsonResult findDemandBaseInfo(@RequestBody Map<String,Object> params) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("demand_create_time").lt("2021-09-17 23:59:59");
        criteria.and("demand_status_normal").nin(7,8,9);
        criteria.and("demand_status_special").nin(1,2);
        query.addCriteria(criteria);
        List<DemandBaseInfo> demandBaseInfos = mongoTemplate.find(query, DemandBaseInfo.class);
        for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
            System.out.println(demandBaseInfo.getOa_contact_no());
        }
        for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
            System.out.println(demandBaseInfo.getOa_contact_name());
        }
        for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
            System.out.println(
                    demandBaseInfo.getDemand_type());
        }
        for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
            System.out.println(
                    DemandBaseInfoUtil.changeStateCn(demandBaseInfo.getDemand_status_normal()) );
        }
        for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
            System.out.println(
                    demandBaseInfo.getDemand_leader_group_cn()
                     );
        }
        for (DemandBaseInfo demandBaseInfo : demandBaseInfos) {
            System.out.println(
                    demandBaseInfo.getDemand_leader_all().get(0).getUser_name_cn() );
        }

        return JsonResultUtil.buildSuccess();
    }





    /**
     * 从IPMP定时同步实施单元
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/testUpdateIpmpUnit")
    public JsonResult testUpdateIpmpUnit(@RequestBody Map<String,Object> params) throws Exception {
        testIpmpUnitService.syncAllIpmpInfo(params);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 批量处理全量需求的牵头小组名
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/testUpdateDemand")
    public JsonResult testUpdateDemand(@RequestBody Map<String,Object> params) throws Exception {
        Map<String,String> groups = fdevUserService.queryGroup();//查询组名
        List<DemandBaseInfo> demandBaseInfoList = queryDemand() ; //获取全部需求
        for (DemandBaseInfo demandBaseInfo : demandBaseInfoList) {
            String demand_leader_group = demandBaseInfo.getDemand_leader_group();
            if(!CommonUtils.isNullOrEmpty(demand_leader_group)){
                String demand_leader_group_cn = groups.get(demand_leader_group);
                updateDemandBaseInfo(demandBaseInfo.getId(),demand_leader_group_cn);
            }
        }
        return JsonResultUtil.buildSuccess();
    }

    public List<DemandBaseInfo> queryDemand() {
        return mongoTemplate.find(new Query(), DemandBaseInfo.class);
    }

    public DemandBaseInfo updateDemandBaseInfo(String demandId, String demand_leader_group_cn) {
        Query query = new Query(Criteria.where(Dict.ID).is(demandId));
        Update update = Update.update("demand_leader_group_cn", demand_leader_group_cn);
        return  mongoTemplate.findAndModify(query, update, DemandBaseInfo.class);
    }
}
