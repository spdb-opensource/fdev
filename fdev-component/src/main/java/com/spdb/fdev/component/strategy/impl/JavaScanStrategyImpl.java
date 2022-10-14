package com.spdb.fdev.component.strategy.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.runnable.BaseRunnable;
import com.spdb.fdev.component.runnable.RunnableFactory;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.component.strategy.IAppScanStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(Constants.JAVA_APP)
@RefreshScope
public class JavaScanStrategyImpl implements IAppScanStrategy {

    private static final Logger logger = LoggerFactory.getLogger(JavaScanStrategyImpl.class);

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IComponentApplicationService componentApplicationService;

    @Autowired
    private IImageApplicationService imageApplicationService;

    @Autowired
    private RunnableFactory runnableFactory;

    @Value("${nas.apps.path}")
    private String nas_apps_path;


    @Value("${cache.app.isdelete}")
    private boolean cache_app_isdelete;

    @Override
    public void eventExecuter(Map<String, Object> param) {
        //获取扫描参数
        String name_en = (String) param.get(Dict.NAME_EN);
        String git = (String) param.get(Dict.GIT);
        try {
            //进行git clone或git pull操作
            componentScanService.gitOperation(name_en, git);
            //配置执行列表
            List<BaseRunnable> runnableList = new ArrayList<>();
            //扫描应用使用组件情况
            BaseRunnable componentRunable = runnableFactory.getRunnable(Constants.SCAN_APP_COMPONENT);
            runnableList.add(componentRunable);
            //扫描应用使用镜像情况
            BaseRunnable imageRunable = runnableFactory.getRunnable(Constants.SCAN_APP_IMAGE);
            runnableList.add(imageRunable);
            if (runnableList.size() > 0) {
                for (BaseRunnable runnable : runnableList) {
                    runnable.setParam(param);
                    runnable.run();
                }
            }
        } catch (Exception e) {
            logger.error("{}扫描应用失败,{}", name_en, e.getStackTrace());
        } finally {
            if (cache_app_isdelete) {
                String localRepository = nas_apps_path + name_en;
                CommonUtils.deleteFile(new File(localRepository));
            }
        }
    }
}
