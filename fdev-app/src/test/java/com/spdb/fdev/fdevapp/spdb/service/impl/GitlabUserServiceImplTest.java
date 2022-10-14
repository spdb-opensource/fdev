package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class GitlabUserServiceImplTest {

    @Autowired
    IGitlabUserService gitlabUserService;

    @Test
    public void changeFdevRole() throws Exception {
        gitlabUserService.changeFdevRole("295", Dict.MAINTAINER);
        gitlabUserService.changeFdevRole("295", Dict.REPORTER);
    }

}