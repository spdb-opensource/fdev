package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.DemandService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DemandServiceImpl implements DemandService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;


    @Override
    public Map queryByFdevNo(String rqrmnt_no,String fdev_implement_unit_no) {
        Map param = new HashMap();
        param.put(Dict.FDEV_IMPLEMENT_UNIT_NO,fdev_implement_unit_no);
        if (!CommonUtils.isNullOrEmpty(rqrmnt_no)) {
            param.put("demand_id", rqrmnt_no);
        }
        param.put(Dict.REST_CODE,"queryByFdevNo");
        Map result = new HashMap<>();
        try {
            result = (Map)restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR);
        }
        return result;
    }

    @Override
    public Map queryDemandInfoDetail(String demand_id) {
        Map param = new HashMap();
        param.put(Dict.ID,demand_id);
        param.put(Dict.REST_CODE,"queryDemandById");
        Map result = new HashMap<>();
        try {
            result = (Map)restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR,new String[]{"查询需求模块异常"});
        }
        return result;
    }

    @Override
    public List showDemandDoc(String demand_id) {
        Map param = new HashMap();
        param.put("demand_id",demand_id);
        param.put("doc_type","");
        param.put(Dict.REST_CODE,"showDemandDoc");
        try {
            return (List) ((Map)restTransport.submit(param)).get(Dict.DATA);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR,new String[]{"查询需求模块异常"});
        }
    }

    @Override
    public List<Map> queryDemandList() {
        Map param = new HashMap();
        param.put(Dict.REST_CODE,"queryAllDemand");
        try {
            return  (List<Map>)restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR,new String[]{"查询需求模块异常"});
        }
    }

    @Override
    public Map<String, Object> queryFdevImplUnitDetail(String unitNo) throws Exception {
        Map<String,String> sendMap = new HashMap<>();
        sendMap.put(Dict.FDEV_IMPLEMENT_UNIT_NO, unitNo);
        sendMap.put(Dict.REST_CODE,"queryFdevImplUnitDetail");
        //根据研发单元编号查询详情
        return (Map<String, Object>) restTransport.submit(sendMap);
    }

    @Override
    public List<Map> queryFdevImplUnitByUnitNos(List<String> unitNos) throws Exception {
        Map<String,Object> sendMap = new HashMap<>();
        sendMap.put(Dict.UNITNOS, unitNos);
        sendMap.put(Dict.REST_CODE,"queryFdevUnitByUnitNos");
        //根据研发单元编号查询详情
        return (List<Map>) restTransport.submit(sendMap);
    }

    @Override
    public List<Map<String, String>> queryDemandByIds(Set<String> ids) throws Exception {
        Map<String,Object> sendMap = new HashMap<>();
        sendMap.put(Dict.IDS, ids);
        sendMap.put(Dict.REST_CODE,"queryDemandByIds");
        //根据研发单元编号查询详情
        return (List<Map<String, String>>) restTransport.submit(sendMap);
    }

}
