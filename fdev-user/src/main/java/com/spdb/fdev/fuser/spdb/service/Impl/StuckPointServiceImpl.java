package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.StuckPointDao;
import com.spdb.fdev.fuser.spdb.entity.user.StuckPoint;
import com.spdb.fdev.fuser.spdb.service.StuckPointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author 李尚林 on 2020/7/23
 * @c-lisl1
 **/
@Service
public class StuckPointServiceImpl implements StuckPointService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Resource
    private StuckPointDao stuckPointDao;

    @Override
    public StuckPoint addStuckPoint(StuckPoint stuckPoint) {
        stuckPoint.setId(CommonUtils.createId());
        return this.stuckPointDao.addStuckPoint(stuckPoint);
    }

    @Override
    public StuckPoint checkStuckPointMapFieldsIsNull(Map map) {
        StuckPoint stuckPoint = new StuckPoint();

        if (!CommonUtils.isNullOrEmpty(map)) {
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.INTERFACE_NAME))) {
                stuckPoint.setInterface_name((String) map.get(Dict.INTERFACE_NAME));
            }
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.INTERFACE_DESC))) {
                stuckPoint.setInterface_desc((String) map.get(Dict.INTERFACE_DESC));
            }
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.P_USER_NAME))) {
                stuckPoint.setP_user_name((String) map.get(Dict.P_USER_NAME));
            }
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.P_USER_ID))) {
                stuckPoint.setP_user_id((String) map.get(Dict.P_USER_ID));
            }
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.P_USER_GROUP))) {
                //System.out.println(map.get(Dict.P_USER_GROUP));
                stuckPoint.setP_user_group((String) map.get(Dict.P_USER_GROUP));
            }
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.OP_TIME))) {
                stuckPoint.setOp_time((String) map.get(Dict.OP_TIME));
            }
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.INTERFACE_DATA))) {
                //System.out.println(map.get(Dict.INTERFACE_DATA));
                /*ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                String json = objectMapper.writeValueAsString(map.get(Dict.INTERFACE_DATA));*/
                stuckPoint.setInterface_data((String) map.get(Dict.INTERFACE_DATA));
            }
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.P_BACK_FIELD))) {
                stuckPoint.setP_back_field((String) map.get(Dict.P_BACK_FIELD));
            }
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"请求参数为空"});
        }

        return stuckPoint;
    }
}
