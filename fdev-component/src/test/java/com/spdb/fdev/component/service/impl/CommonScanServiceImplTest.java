package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.entity.MpassComponent;
import com.spdb.fdev.component.service.ICommonScanService;
import com.spdb.fdev.component.service.IComponentScanService;
import com.spdb.fdev.component.service.IMpassComponentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CommonScanServiceImplTest {

    @Autowired
    private ICommonScanService commonScanService;

    @Autowired
    private IMpassComponentService mpassComponentService;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Test
    public void testGetDependency() throws Exception {
        String component_id="5f04679a673c364588bc29ad";
        MpassComponent mpassComponent = mpassComponentService.queryById(component_id);
        if (mpassComponent == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"component can not find"});
        }
//        //根据组件数据  删除所有 应用和组件关系
//        ComponentApplication componentApplication = new ComponentApplication();
//        componentApplication.setComponent_id(component_id);
//        componentApplicationService.deleteAllByComponentId(componentApplication);
        componentScanService.initMpassComponentApplication(mpassComponent);
    }

}