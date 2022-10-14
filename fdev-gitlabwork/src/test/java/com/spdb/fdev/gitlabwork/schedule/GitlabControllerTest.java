package com.spdb.fdev.gitlabwork.schedule;

import com.spdb.fdev.gitlabwork.util.Util;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GitlabControllerTest {

    @Resource
    private RestTemplate restTemplate;

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.token}")
    private  String gitlabToken;

    @Test
    public void findBrancheListById() throws IOException {
        boolean flag = true;
        int i = 1;
        int projectId = 1454;
        List<Map<String, Object>> result = new ArrayList<>();
        while (flag) {
            String url = gitlabApiUrl+"projects/" + projectId + "/repository/branches?page=" + i + "&per_page=100";
            String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
            if (StringUtils.isNotBlank(body)) {
                List<Map<String, Object>> branchList = Util.stringToList(body);
                if (branchList != null && branchList.size() > 0) {
                    result.addAll(branchList);
                    i++;
                } else
                    flag = false;
            } else if (body == null)
                flag = false;
        }
        System.out.println(result.size());
    }

    @Test
    public void findOneBranchCommitList() throws IOException {
        int projectId = 1616;
        String ref_name = "UAT";
        String since = "2019-07-01";
        String until = "2019-09-01";
        boolean flag = true;
        int i = 1;
        List<Map<String, Object>> result = new ArrayList<>();
        while (flag) {
            String url = gitlabApiUrl+"projects/" + projectId + "/repository/commits?ref_name=" + ref_name + "&page=" + i + "&per_page=100&since=" + since + "&until=" + until;
            String body = Util.httpMethodGetExchange(url, restTemplate, gitlabToken);
            if (StringUtils.isNotBlank(body)) {
                List<Map<String, Object>> oneBranchCommitList = Util.stringToList(body);
                if (oneBranchCommitList != null && oneBranchCommitList.size() > 0) {
                    result.addAll(oneBranchCommitList);
                    i++;
                } else
                    flag = false;
            } else if (body == null)
                flag = false;
        }
        //logger.info("getOneBranchCommitList:" + oneBranchCommitList.toString());
        System.out.println(result.size());
    }
}
