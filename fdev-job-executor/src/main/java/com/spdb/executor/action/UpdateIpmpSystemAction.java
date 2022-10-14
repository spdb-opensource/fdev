package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.service.IpmpService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateIpmpSystemAction extends BaseExecutableAction {
    private static Logger logger = LoggerFactory.getLogger(UpdateIpmpSystemAction.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IpmpService ipmpService;

    @Override
    public void execute(Context context) {
        logger.info("execute UpdateIpmpSystemInfo begin");
        List<Map<String, String>> ipmpList = ipmpService.queryIpmpSystemInfo();
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.IPMPLIST, ipmpList);
        map.put(Dict.REST_CODE, "updateIpmpSystemBatch");
        try {
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("批量更新ipmp系统信息数据错误", e);
            throw new FdevException("批量更新ipmp系统信息数据错误");
        }
        logger.info("execute UpdateIpmpSystemInfo end");
    }
}
