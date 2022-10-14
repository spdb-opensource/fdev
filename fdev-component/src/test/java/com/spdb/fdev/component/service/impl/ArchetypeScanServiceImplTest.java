package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.component.service.IArchetypeScanService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ArchetypeScanServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(ArchetypeScanServiceImplTest.class);


    @Autowired
    private IArchetypeScanService archetypeScanService;

    @Test
    public void test() throws Exception {
        logger.error("{},ScanAppComponent更新组件和应用信息失败,{}", "mpsmk", "error");
//        String archetype_id = "5ee0a4489b104a001403d6a4";
//        String version = "1.0.5-RELEASE";
//        archetypeScanService.scanImageArchetype(archetype_id, version);
    }
}