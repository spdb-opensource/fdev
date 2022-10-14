package com.spdb.fdev.fdevapp;

import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.entity.EnvBranch;
import com.spdb.fdev.fdevapp.spdb.service.IAppEntityService;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppEntityServiceTests {

    @Autowired
    IAppEntityService appEntityService;

    @Value("${continuous.intergration.file}")
    private String continuous_file;

    @Value("${gitlab.fileDir}")
    private String local_temp_repo;

    @Test
    public void addDevops() throws Exception {
        List<String> filePahtList = new ArrayList<String>();
        String projectName = "aaa";
        String[] fileArray = continuous_file.split(",");
        for (String path : fileArray) {
            String filePath = local_temp_repo + projectName + File.separator + path;
            CommonUtils.getContinuousPathFiles(new File(filePath), filePahtList);
        }
        //CommonUtils.getReplacePathFiles(new File(fdevCiTemplatePath + File.separator + templatePath), fdevCiTemplateName + File.separator + templatePath, projectName, filePahtList);
        CommonUtils.replaceStrByFilePathList(filePahtList, "^project_name^", projectName);
    }
}
