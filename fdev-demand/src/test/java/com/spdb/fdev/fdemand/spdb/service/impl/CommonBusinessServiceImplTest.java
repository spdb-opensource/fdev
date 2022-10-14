package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.fdemand.spdb.service.ICommonBusinessService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CommonBusinessServiceImplTest {

    @Autowired
    private ICommonBusinessService commonBusinessService;

    @Test
    private void test(){
        commonBusinessService.deleteOrder("5fa933694dd71200125c0dc3");
    }

}