package com.spdb.fdev.codeReview.base.utils;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.dict.ErrorConstants;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author liux81
 * @DATE 2021/11/10
 */
@Component
public class DemandUtil {

    @Autowired
    private RestTransport restTransport;

    private static Logger logger = LoggerFactory.getLogger(DemandUtil.class);

    /**
     * 批量查询需求信息，并存放到map中
     * @param demandIds
     * @return
     */
    public Map getDemandsInfoByIds(Set<String> demandIds) throws Exception {
        Map<String,Map> resultMap = new HashMap();
        if(!CommonUtils.isNullOrEmpty(demandIds)){
            Map param = new HashMap();
            param.put(Dict.REST_CODE,Dict.GETDEMANDSINFOBYIDS);
            param.put(Dict.DEMANDIDS, demandIds);
            List<Map> demands = (List<Map>)restTransport.submit(param);
            for(Map demand : demands){
                resultMap.put((String)demand.get(Dict.ID),demand);
            }
        }
        return resultMap;
    }

    /**
     * 修改需求的代码审核工单编号
     * @param demandIds
     * @param orderNo
     * @throws Exception
     */
    public void updateDemandCodeOrderNo(List<String> demandIds,String orderNo){
        Map param = new HashMap();
        param.put(Dict.REST_CODE,Dict.UPDATEDEMANDCODEORDERNO);
        param.put(Dict.DEMAND_ID,demandIds);
        param.put(Dict.code_order_no,orderNo);
        try {
            restTransport.submit(param);
        }catch (Exception e){
            logger.error(ErrorConstants.INTERFACE_DEMAND_FAIL,new String[]{Dict.UPDATEDEMANDCODEORDERNO});
        }
    }
}
