package com.spdb.fdev.component.strategy;

import com.spdb.fdev.base.dict.ComponentEnum;
import com.spdb.fdev.base.dict.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScanContext {

    private static final Logger logger = LoggerFactory.getLogger(ScanContext.class);

    @Autowired
    private Map<String, IAppScanStrategy> scanStrategyMap;

    public void scanExecutor(String type, Map<String, Object> param) {
        IAppScanStrategy appScanStrategy;
        if (ComponentEnum.AppTypeEnum.Java.getValue().equals(type) || ComponentEnum.AppTypeEnum.Container.getValue().equals(type)) {
            logger.info("开始扫描{}", type);
            appScanStrategy = scanStrategyMap.get(Constants.JAVA_APP);
        } else if (ComponentEnum.AppTypeEnum.Vue.getValue().equals(type)) {
            logger.info("开始扫描{}", type);
            appScanStrategy = scanStrategyMap.get(Constants.VUE_APP);
        } else {
            logger.info("当前类型{}无扫描策略", type);
            return;
        }
        appScanStrategy.eventExecuter(param);
    }
}
