package com.spdb.fdev.component.service;

import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.transport.GitlabTransport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class GitlabUserServiceImplTest {

    @Value("${gitlab.api.url1:http://xxx/api/v4/}")
    private String gitlabApiUrl;

    @Value("${gitlab.manager.token:xxx}")
    private String token;

    @Value("${gitlab.manager.username}")
    private String gitlabUsername;

    @Value("${gitlab.manager.password}")
    private String gitlabPassword;

    @Value("${gitlab.manager.userid}")
    private String gitlabUserid;

    @Autowired
    private GitlabTransport gitlabTransport;

    @Autowired
    private IGitlabUserService gitlabUserService;

    @Autowired
    private ICommonInfoService commonInfoService;

    @Test
    public void getProjectsUser() throws Exception {
        String id = "5633";
        List<Map> result = new ArrayList<>();
        boolean flag = true;
        int i = 1;
        while (flag) {
            String userUrl = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/members?page=" + i + "&per_page=100";
            MultiValueMap<String, String> header = new LinkedMultiValueMap();
            header.add(Dict.PRIVATE_TOKEN, token);
            Object object = gitlabTransport.submitGet(userUrl, header);
            if (object != null) {
                List<Map> userList = JSONArray.parseArray((String) object, Map.class);
                if (userList != null && userList.size() > 0) {
                    i++;
                    result.addAll(userList);
                } else {
                    flag = false;
                }
            } else {
                flag = false;
            }
        }
        System.out.println(result.size());
    }

    @Test
    public void addMember() throws Exception {
        gitlabUserService.addMembers(commonInfoService.addMembersForApp("5db688e8f8d61800136ffe11", "11", Dict.DEVELOPER));
    }

    @Test
    public void addRole() throws Exception {
        String projectId = "404";
        gitlabUserService.changeFdevRole(projectId, Dict.MAINTAINER);
        gitlabUserService.changeFdevRole(projectId, Dict.REPORTER);

    }
}