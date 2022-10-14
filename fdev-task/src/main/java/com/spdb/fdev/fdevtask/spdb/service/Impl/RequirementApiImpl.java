package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.RequirementApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Service
@RefreshScope
public class RequirementApiImpl implements RequirementApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    @Override
    public boolean updateRqrmntState(String rqrmntId, String stage, Map time) {
        Map param = new HashMap<>();
        if (!time.isEmpty()) {
            Iterator iterator = time.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry next = (Map.Entry) iterator.next();
                param.put(next.getKey(), next.getValue());
            }
        }
        param.put(Dict.RQRMNTID, rqrmntId);
        param.put(Dict.STAGE, stage);
        param.put(Dict.REST_CODE, "updateRqrmntstate");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("更新需求状态失败：ex--" + e.getMessage());
            throw new FdevException(ErrorConstants.RQRMNT_ERROR, new String[]{e.getMessage()});
        }
        return true;
    }

    //{"type":"","state":"","keyword":"","relevant":false,"userid":"","groupid":"","datetype":"","stateNum":"","featureType":"","featureNum":"","size":-1,"index":-1,"sortBy":"","descending":false}
    @Override
    public Object query(boolean priority){
        Map param = new HashMap();
        param.put("priority",priority?"高":"");
        param.put(Dict.REST_CODE,"queryRqrmnts");
        param.put(Dict.TYPE,"");
        param.put(Dict.STATE,new String[]{});
        param.put("keyword","");
        param.put("relevant",false);
        param.put("userid","");
        param.put("groupid",new String[]{});
        param.put("datetype","");
        param.put("stateNum","");
        param.put("featureType","");
        param.put("featureNum","");
        param.put("size",0);
        param.put("index",-1);
        param.put("sortBy","");
        param.put("descending",false);
        Object result;
        try {
            result = restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR,new String[]{e.getMessage()});
        }
        return result;
    }

    @Override
    public Map queryRqrmntInfo(String rqrmntId) {
        Map param = new HashMap();
        param.put(Dict.RQRMNTID,rqrmntId);
        param.put(Dict.REST_CODE,"queryRqrmntsInfo");
        Map result = new HashMap<>();
        try {
            result = (Map)restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR,new String[]{e.getMessage()});
        }
        return result;
    }

    @Override
    public List<Map> queryRqrmntsByRqrmntNo(String rqrmntNo) {
        List<Map> result = new ArrayList<>();
        if (CommonUtils.isNotNullOrEmpty(rqrmntNo)){
            Map param=new HashMap();
            param.put("rqrmntNum",rqrmntNo);
            param.put(Dict.REST_CODE,"queryRegexRqrmntNum");
            try {
                result = (List<Map>) restTransport.submit(param);
            } catch (Exception e) {
                logger.error("模糊查询需求失败");
                throw new FdevException(ErrorConstants.RQRMNT_ERROR,new String[]{e.getMessage()});
            }
        }
        return result;
    }

    @Override
    public Map queryRqrmntById(String rqrmntId) {
        Map param=new HashMap();
        param.put("id",rqrmntId);
        param.put(Dict.REST_CODE,"queryDemandInfoDetail");
        Map result = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(rqrmntId)){
            try {
                result = (Map) restTransport.submit(param);
            } catch (Exception e) {
                logger.error("根据需求id查询需求,需求不存在");
                return result;
            }
        }
        return result;
    }

    /**
     * 更新实施单元，发送实施单元接口
     *
     * @param unitNo
     * @param stage
     * @param time
     * @return
     */
    @Override
    public Boolean updateImplUnitState(String unitNo, String stage, Map time) {
        Map param = new HashMap<>();
        param.put("fdev_implement_unit_no", unitNo);
        param.put(Dict.STAGE, stage);
        if (!time.isEmpty()) {
            Iterator iterator = time.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry next = (Map.Entry) iterator.next();
                param.put(next.getKey(), next.getValue());
            }
        }
        String userName = "";
        if(!CommonUtils.isNullOrEmpty(RequestContextHolder.getRequestAttributes())) {
            User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getSession().getAttribute(Dict._USER);
            if (!CommonUtils.isNullOrEmpty(user)) {
                userName = user.getUser_name_en();
            }
        }

        param.put(Dict.USER_NAME_EN, userName);
        param.put(Dict.REST_CODE, "updateStatus");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("更新实施单元状态失败：ex--" + e.getMessage());
            throw new FdevException(ErrorConstants.RQRMNT_ERROR, new String[]{e.getMessage()});
        }
        return true;
    }


    /**
     * 更新需求，发送研发单元接口
     *
     * @param rqrNo
     * @param stage
     * @param time
     * @return
     */
    @Override
    public Boolean updateDemandState(String rqrNo, String stage, Map time) {
        Map param = new HashMap<>();
        param.put("demand_id", rqrNo);
        param.put(Dict.STAGE, stage);
        if (!time.isEmpty()) {
            Iterator iterator = time.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry next = (Map.Entry) iterator.next();
                param.put(next.getKey(), next.getValue());
            }
        }
        param.put(Dict.REST_CODE, "updateDemandForTask");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("更新研发单元状态失败：ex--" + e.getMessage());
            throw new FdevException(ErrorConstants.RQRMNT_ERROR, new String[]{e.getMessage()});
        }
        return true;
    }
}
