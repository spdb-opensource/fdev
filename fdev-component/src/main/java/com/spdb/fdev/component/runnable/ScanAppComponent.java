package com.spdb.fdev.component.runnable;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.Dependency;
import com.spdb.fdev.component.service.ICommonScanService;
import com.spdb.fdev.component.service.IComponentApplicationService;
import com.spdb.fdev.component.service.IComponentInfoService;
import com.spdb.fdev.component.service.IComponentScanService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 扫描应用使用组件信息
 */
@Component(Constants.SCAN_APP_COMPONENT)
@RefreshScope
public class ScanAppComponent extends BaseRunnable {

    private static final Logger logger = LoggerFactory.getLogger(ScanAppComponent.class);

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IComponentApplicationService componentApplicationService;

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Override
    public void run() {
        Map param = super.getParam();
        //获取扫描参数
        String id = (String) param.get(Dict.ID);//应用id
        String name_en = (String) param.get(Dict.NAME_EN);
        try {
            //根据应用信息， 删除所有应用和组件关系
            ComponentApplication component = new ComponentApplication();
            component.setApplication_id(id);
            componentApplicationService.deleteAllByApplicationId(component);
            //遍历所有组件进行扫描
            List<ComponentInfo> componentInfoList = componentInfoService.query(new ComponentInfo());
            //获取当前应用下的组件依赖列表
            List<Dependency> dependencyList = commonScanService.getDependencyList(nas_apps_path + name_en, Constants.COMPONENT_COMPONENT);
            //进行组件匹配，获取应用使用组件情况
            if (!CommonUtils.isNullOrEmpty(componentInfoList)) {
                for (ComponentInfo info : componentInfoList) {
                    ComponentApplication componentApplication = componentScanService.getComponentApplication(id, nas_apps_path + name_en, info, dependencyList);
                    if (componentApplication != null) {
                        // 获取最新的组件版本，判断应用中用到的是否最新版本，0：是，1：不是
                        String latestVersion = commonScanService.getLatestVersion(info.getGroupId(), info.getArtifactId());
                        if (StringUtils.isNotBlank(latestVersion)
                                && componentApplication.getComponent_version().equals(latestVersion)) {
                            componentApplication.setIsLatest(Constants.LATEST);
                        } else {
                            componentApplication.setIsLatest(Constants.NOTLATEST);
                        }
                        // 根据组件id和应用id，判断当前应用和组件关系是否已存在，存在则更新原有数据
                        ComponentApplication application = componentApplicationService
                                .queryByComponentIdAndAppId(componentApplication);
                        if (application == null) {
                            componentApplicationService.save(componentApplication);
                        } else {
                            application.setUpdate_time(Util.simdateFormat(new Date()));
                            application.setComponent_version(componentApplication.getComponent_version());
                            application.setIsLatest(componentApplication.getIsLatest());
                            componentApplicationService.update(application);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("{},ScanAppComponent更新组件和应用信息失败,{}", name_en, e.getMessage());
        }
    }

}
