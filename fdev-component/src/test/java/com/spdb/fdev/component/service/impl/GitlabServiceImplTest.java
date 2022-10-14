package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.service.IGitlabSerice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class GitlabServiceImplTest {

    @Value("${gitlab.manager.token}")
    private String token;


    @Autowired
    IGitlabSerice gitlabSerice;

    @Test
    public void checkBranch() throws Exception {
        String gitlabId = "145";
        String branch = "dev-0617";
        String tag = "test-2006171624";
        boolean result = gitlabSerice.checkBranchOrTag(token, gitlabId, Dict.BRANCH, branch);
        System.out.println(result);
    }

}