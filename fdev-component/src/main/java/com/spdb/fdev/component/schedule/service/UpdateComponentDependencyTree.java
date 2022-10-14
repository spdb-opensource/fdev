package com.spdb.fdev.component.schedule.service;

import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.ComponentRecord;
import com.spdb.fdev.component.service.IComponentInfoService;
import com.spdb.fdev.component.service.IComponentRecordService;
import com.spdb.fdev.component.service.IPythonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateComponentDependencyTree {

    private static final Logger logger = LoggerFactory.getLogger(UpdateComponentDependencyTree.class);

    @Autowired
    private IPythonService pythonService;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IComponentRecordService componentRecordService;


    /**
     * 对于组件的所有版本扫描
     * 将组件的依赖树存入到文件中
     */
    @Async
    public void updateComponent() throws Exception {
        List<ComponentInfo> infoList = componentInfoService.query(new ComponentInfo());
        if (infoList != null && infoList.size() > 0) {
            for (int i = 0; i < infoList.size(); i++) {
                ComponentInfo info = infoList.get(i);
                logger.info("组件扫描依赖树开始,共{}条,当前第{}条", infoList.size(), (i + 1));
                ComponentRecord record = new ComponentRecord();
                record.setComponent_id(info.getId());
                List<ComponentRecord> recordList = componentRecordService.query(record);
                if (infoList != null && recordList.size() > 0) {
                    for (ComponentRecord componentRecord : recordList) {
                        pythonService.queryDependencyTree(info.getGroupId(), info.getArtifactId(), componentRecord.getVersion());
                    }
                }
            }
        }

    }

}
