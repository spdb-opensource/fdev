package com.spdb.fdev.component.schedule.service;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.entity.ArchetypeInfo;
import com.spdb.fdev.component.entity.ArchetypeRecord;
import com.spdb.fdev.component.entity.ComponentArchetype;
import com.spdb.fdev.component.service.IArchetypeInfoService;
import com.spdb.fdev.component.service.IArchetypeRecordService;
import com.spdb.fdev.component.service.IComponentArchetypeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ArchetypeVersionNotifyTest {

    @Autowired
    private ArchetypeVersionNotify archetypeVersionNotify;

    @Autowired
    private IArchetypeInfoService archetypeInfoService;

    @Autowired
    private IArchetypeRecordService archetypeRecordService;

    @Autowired
    private IComponentArchetypeService componentArchetypeService;

    @Test
    public void test() throws Exception {
        archetypeVersionNotify.checkComponent();
    }

    @Test
    public void archetypeNofity() throws Exception {
        String component_id = "5ea28674a9218600148bdc6e";
        Set<String> users = new HashSet<>();
        //如果骨架使用此组件，也进行邮件发送
        List<ArchetypeInfo> archetypeInfoList = archetypeInfoService.query(new ArchetypeInfo());
        if (!CommonUtils.isNullOrEmpty(archetypeInfoList)) {
            for (ArchetypeInfo archetypeInfo : archetypeInfoList) {
                String name_en = archetypeInfo.getName_en();
                //查询骨架推荐版本
                ArchetypeRecord archetypeRecord = archetypeRecordService.queryByArchetypeIdAndType(archetypeInfo.getId(), Constants.RECORD_RECOMMEND_TYPE);
                if (archetypeRecord != null) {
                    //查询属于这个推荐版本的所有组件关联信息
                    List<ComponentArchetype> componentArchetypeList =
                            componentArchetypeService.queryByArcIdAndVersionCopId(archetypeRecord.getArchetype_id(), archetypeRecord.getVersion(), component_id);
                    if (!CommonUtils.isNullOrEmpty(componentArchetypeList)) {
                        users.addAll(CommonUtils.getManageIds(archetypeInfo.getManager_id()));
                    }
                }
            }
        }
        System.out.println(users);
    }

    @Test
    public void archetypeAutoScan() {
        archetypeVersionNotify.archetypeAutoScan();
    }

}