package com.gotest.service.serviceImpl;

import com.gotest.dict.Dict;
import com.gotest.service.NewAppService;
import com.gotest.utils.CommonUtils;
import com.test.testmanagecommon.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.smartcardio.CommandAPDU;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@Service
@RefreshScope
public class NewAppServiceImpl implements NewAppService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    @Override
    public Map queryAppInfoById(String relatedApplication) throws Exception {
        try {
            Map send = new HashMap();
            send.put(Dict.IDS, Arrays.asList(relatedApplication));
            send.put(Dict.REST_CODE, "new.fdev.queryApps");
            Map result = (Map) restTransport.submitSourceBack(send);
            List<Map> apps = (List<Map>) result.get("serviceAppList");
            if(!CommonUtils.isNullOrEmpty(apps)){
                return apps.get(0);
            }else{
                return null;
            }
        } catch (Exception e) {
            logger.error("fail to get app info");
        }
        return null;
    }
}
