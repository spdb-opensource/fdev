package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.RestTransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

public class TestPipelineScheduleAction extends BaseExecutableAction {

    @Autowired
    private RestTransportService restTransportService;

    @Value("${whiteapps}")
    private String whiteapps;
    
    @Override
    // 后续要调整 实现方式
    public void execute(Context context) {
        logger.info("execute TestPipelineSchedule begin");
        if (!CommonUtils.isNullOrEmpty(whiteapps)) {
            String[] split = whiteapps.split(";");
            for (int i = 0; i < split.length; i++) {
            	String[] whiteapp = split[i].split(":"); 
                List result = restTransportService.querySitMergeInfo(whiteapp[0]);
                if (!CommonUtils.isNullOrEmpty(result)) {
                    int mergeId = 0;
                    try {
                        mergeId = result.stream().mapToInt(n -> Integer.parseInt((String) ((Map) n).get("mergeId"))).max().getAsInt();
                    } catch (Exception e) {
                        logger.info("获取mergeid失败");
                        return;
                    }
                    restTransportService.createTestPipeline(whiteapp[0],whiteapp[1], String.valueOf(mergeId));
                }
            }
        }
        logger.info("execute TestPipelineSchedule end");
    } 
    
}
