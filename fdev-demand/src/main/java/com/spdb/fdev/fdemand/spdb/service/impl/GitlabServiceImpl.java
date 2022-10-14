package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.GitlabTransport;
import com.spdb.fdev.fdemand.spdb.service.IGitlabService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RefreshScope
@Service
public class GitlabServiceImpl implements IGitlabService {
    private static final Logger logger = LoggerFactory.getLogger(GitlabServiceImpl.class);
    @Autowired
    private GitlabTransport gitlabTransport;
    @Value("${doc.project.id}")
    private String docProjectId;
    @Value("${gitlab.api.url}")
    private String url;//gitlab地址http://xxx/api/v4/
    @Value("${gitlab.manager.token}")
    private String token;

    @Override
    public void uploadFile(List<byte[]> files, String path) {
        List<Map> actions = new ArrayList<>();
        Map<String, Object> action = null;
        for (byte[] file : files) {
            action = new HashMap<>();
            action.put(Dict.ACTION, "create");
            action.put(Dict.FILE_PATH, path);
            action.put(Dict.CONTENT, Base64.encodeBase64String(file));
            action.put(Dict.ENCODING, "base64");
            actions.add(action);
        }
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.BRANCH, "master");
        param.put(Dict.COMMIT_MESSAGE, "文件归档");
        param.put(Dict.ACTIONS, actions);
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + docProjectId + "/repository/commits";
        try {
            gitlabTransport.submitPost(gitLabUrl, param);
        } catch (Exception e) {
            logger.info(">>>uploadFile fail,{}", gitLabUrl);
        }
    }
}
