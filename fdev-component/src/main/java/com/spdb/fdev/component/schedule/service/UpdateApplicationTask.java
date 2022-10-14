package com.spdb.fdev.component.schedule.service;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.component.strategy.ScanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * UpdateGitlabProjectTask
 *
 * @blame Android Team
 */
@Component
@RefreshScope
public class UpdateApplicationTask {
    private static final Logger logger = LoggerFactory.getLogger(UpdateApplicationTask.class);

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Value("${scan.skip.app.type}")
    private String scan_skip_app_type;

    @Value("${scan.app.count}")
    private int scan_app_count;

    @Value("${cache.app.isdelete}")
    private boolean cache_app_isdelete;

    @Autowired
    private IAppService appService;

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
    private ScanContext scanContext;

    /**
     * 获取所有应用信息，判断项目路径下是否存在.git文件，进行git clone或git pull操作
     * 通过mvn dependency:list获取项目下所有依赖列表
     * 获取所有组件列表，通过groupId和artifactId匹配
     */
    @Async
    public void updateApp() throws Exception {
        List<Map<String, Object>> appList = appService.getAppList();
        if (appList != null && appList.size() > 0) {
            int length = appList.size();
            if (scan_app_count != 0) {
                length = scan_app_count;
            }
            for (int i = 0; i < length; i++) {
                logger.info("开始扫描第{}个应用，共{}个应用", (i + 1), length);
                Map map = appList.get(i);
                String type_name = (String) map.get(Dict.TYPE_NAME);
                scanContext.scanExecutor(type_name, map);
            }
        }
        logger.info("定时任务更新组件和应用信息完成");
    }
}
