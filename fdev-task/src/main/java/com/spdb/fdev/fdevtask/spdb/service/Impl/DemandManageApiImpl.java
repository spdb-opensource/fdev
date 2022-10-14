package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.DemandManageApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-03-08 11:15
 **/
@Service
public class DemandManageApiImpl implements DemandManageApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    @Override
    public List<Map> queryIpmpListByid(List<String> implUnitIds) {
        Map<String,Object> param = new HashMap();
        param.put(Dict.REQUIREMENT_ID,implUnitIds);
        param.put(Dict.REST_CODE,"queryIpmpListByid");
        try {
            Object impl = restTransport.submit(param);
            if (!CommonUtils.isNullOrEmpty(impl)){
                Object implObject = ((Map)impl).get(Dict.DATA);
                if (!CommonUtils.isNullOrEmpty(implObject)){
                   return ((List<Map>)implObject);
                }
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR,new String[]{"查询研发单元异常"});
        }
        return null;
    }

    @Override
    public List<Map> queryDemandInfoByIds(List<String> demandIds) throws Exception {
        Map<String,Object> map = new HashMap();
        map.put(Dict.DEMAND_ID,demandIds);
        map.put(Dict.REST_CODE,"queryDemandInfoByIds");
        List<Map> result;
        try {
            result = CommonUtils.castList(restTransport.submit(map),Map.class);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取需求信息列表失败"});
        }
        return result;
    }

    @Override
    public void queryDemandStatus(String demandId, String demandStatus) throws Exception {
        Map<String,Object> map = new HashMap();
        map.put(Dict.DEMAND_ID,demandId);
        map.put(Dict.DEMAND_STATUS,demandStatus);
        map.put(Dict.REST_CODE,"queryDemandStatus");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"修改需求状态失败"});
        }
    }

    @Override
    public Map queryDemandManageInfoDetail(String demandId) throws Exception {
        Map<String,Object> map = new HashMap();
        map.put(Dict.DEMAND_ID,demandId);
        map.put(Dict.REST_CODE,"queryDemandManageInfoDetail");
        Map result;
        try {
            result = (Map) restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取需求信息列表失败"});
        }
        return result;
    }
}
