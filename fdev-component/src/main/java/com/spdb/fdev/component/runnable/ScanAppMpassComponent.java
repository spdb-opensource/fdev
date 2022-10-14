package com.spdb.fdev.component.runnable;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.entity.MpassComponent;
import com.spdb.fdev.component.service.ICommonScanService;
import com.spdb.fdev.component.service.IComponentApplicationService;
import com.spdb.fdev.component.service.IComponentScanService;
import com.spdb.fdev.component.service.IMpassComponentService;
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
 * 扫描应用使用镜像信息
 */
@Component(Constants.Scan_App_MpassComponent)
@RefreshScope
public class ScanAppMpassComponent extends BaseRunnable {

    private static final Logger logger = LoggerFactory.getLogger(ScanAppComponent.class);


    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IComponentApplicationService componentApplicationService;

    @Autowired
    private IMpassComponentService mpassComponentService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Override
    public void run() {
        //获取扫描参数
        Map param = super.getParam();
        String id = (String) param.get(Dict.ID);//应用id
        String name_en = (String) param.get(Dict.NAME_EN);
        try {
            //根据应用信息， 删除所有应用和组件关系
            ComponentApplication component = new ComponentApplication();
            component.setApplication_id(id);
            componentApplicationService.deleteAllByApplicationId(component);
            //获取所有Mpass组件列表
            List<MpassComponent> query = mpassComponentService.query(new MpassComponent());
            //获取应用依赖列表
            List<Map> dependencyList = commonScanService.getDependencyMapList(nas_apps_path + name_en, Constants.COMPONENT_MPASS_COMPONENT);
            if (!CommonUtils.isNullOrEmpty(query)) {
                for (MpassComponent mpassComponent : query) {
                    ComponentApplication componentApplication = componentScanService.getComponentApplication(id, nas_apps_path + name_en, mpassComponent, dependencyList);
                    if (componentApplication != null) {
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
            logger.error("{},ScanAppMpassComponent更新Mpass组件和应用信息失败,{}", name_en, e.getMessage());
        }

    }
}
