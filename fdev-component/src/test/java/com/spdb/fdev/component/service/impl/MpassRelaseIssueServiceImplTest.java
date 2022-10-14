package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.component.dao.IMpassComponentDao;
import com.spdb.fdev.component.entity.MpassComponent;
import com.spdb.fdev.component.service.IGitlabSerice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MpassRelaseIssueServiceImplTest {

    @Autowired
    private IGitlabSerice gitlabSerice;

    @Test
    public void updateRelease() throws Exception {
        String feature_branch = "release1.2.0";
        String tag = "1.2.2";
        MpassComponent mpassComponent = new MpassComponent();
        mpassComponent.setGitlab_id("470");
        mpassComponent.setName_cn("router");
        gitlabSerice.updateReleaseChangeLog(mpassComponent, feature_branch, tag);
    }

}