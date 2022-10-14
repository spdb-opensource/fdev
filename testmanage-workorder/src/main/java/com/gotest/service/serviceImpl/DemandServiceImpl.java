package com.gotest.service.serviceImpl;

import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.IDemandService;
import com.gotest.utils.CommonUtils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisClusterConfig;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
@Service
public class DemandServiceImpl implements IDemandService {

    @Autowired
    private RestTransport restTransport;
    private static final Logger logger = LoggerFactory.getLogger(RedisClusterConfig.class);

    @Override
    public Map<String, Object> queryDemandById(String id) throws Exception {
        Map send = new HashMap();
        send.put(Dict.ID, id);
        send.put(Dict.REST_CODE, "queryDemandInfoDetail");
        Map result = new HashMap();
        try {
            result = (Map) restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to query demand");
        }
        return result;
    }

    @Override
    public Map queryByFdevNoAndDemandId(String unit) throws Exception {
        Map param = new HashMap();
        param.put(Dict.FDEV_IMPLEMENT_UNIT_NO,unit);
        if(!CommonUtils.isNullOrEmpty(unit) && unit.startsWith("IPMP")) {
            param.put(Dict.REST_CODE,"queryByUnitNoAndDemandId");
        }else {
            param.put(Dict.REST_CODE,"queryByFdevNoAndDemandId");
        }
        return  (Map)restTransport.submitSourceBack(param);
    }

    /**
     * 根据实施单元id查询实施单元详情
     *
     * @param unitId
     * @return
     * @throws Exception
     */
    @Override
    public Map queryNewUnitInfoById(String unitId) throws Exception {
        try {
            Map send = new HashMap();
            send.put(Dict.ID, unitId);
            send.put(Dict.REST_CODE, "queryIpmpInfo");
            Map result = (Map) restTransport.submitSourceBack(send);
            if (Util.isNullOrEmpty(result)) {
                logger.error("fail to get new demand info");
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询新实施单元信息失败！"});
            }
            return result;
        } catch (Exception e) {
            logger.error("fail to get new demand info");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询新实施单元信息失败！"});
        }
    }

    @Override
    public Map<String, Object> queryImplUnitById(String implUnitNum) throws Exception {
        Map<String,String> sendMap = new HashMap<>();
        sendMap.put(Dict.IMPL_UNIT_NUM, implUnitNum);
        sendMap.put(Dict.REST_CODE,"queryIpmpUnitById");
        //根据实施单元编号查询详情
        return (Map<String, Object>) restTransport.submitSourceBack(sendMap);
    }

    @Override
    public Map<String, Object> queryFdevImplUnitDetail(String unitNo) throws Exception {
        Map<String,String> sendMap = new HashMap<>();
        sendMap.put(Dict.FDEV_IMPLEMENT_UNIT_NO, unitNo);
        sendMap.put(Dict.REST_CODE,"queryFdevImplUnitDetail");
        //根据研发单元编号查询详情
        return (Map<String, Object>) restTransport.submitSourceBack(sendMap);
    }

    /**
     * 新版业务需求，根据研发单元编号获取实施单元id
     * @param unitNo
     * @return
     * @throws Exception
     */
    @Override
    public String getUnitNo(String unitNo) throws Exception {
        //新业务需求创建工单时使用的实施单元id，需要根据传入的研发单元编号查询
        //老需求和新的科技需求，使用研发单元创建的工单，不需要根据研发单元查询实施单元
        //查询研发单元信息
        Map<String,Object> demandAndUnitInfo = queryByFdevNoAndDemandId(unitNo);
        Map<String,Object> demand_baseinfo = (Map)demandAndUnitInfo.get("demand_baseinfo");
        Map<String,Object> fdevUnitInfo = (Map)demandAndUnitInfo.get("implement_unit_info");
        if(!CommonUtils.isNullOrEmpty(fdevUnitInfo)
                && !CommonUtils.isNullOrEmpty(demand_baseinfo.get(Dict.DEMAND_FLAG))
                && "business".equals((String)fdevUnitInfo.get(Dict.DEMAND_TYPE))) {
            //查询实施单元信息
            Map<String,Object> implUnitInfo = queryImplUnitById((String) fdevUnitInfo.get(Dict.IPMP_IMPLEMENT_UNIT_NO));
            if(!CommonUtils.isNullOrEmpty(implUnitInfo)) {
                unitNo = (String) implUnitInfo.get(Dict.ID);
            }
        }
        return unitNo;
    }

}
