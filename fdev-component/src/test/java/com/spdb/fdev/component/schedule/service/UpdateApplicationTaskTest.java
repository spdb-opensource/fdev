package com.spdb.fdev.component.schedule.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class UpdateApplicationTaskTest {

    @Autowired
    private UpdateApplicationTask updateApplicationTask;

    @Test
    public void updateApp() throws Exception {
        updateApplicationTask.updateApp();
    }

}