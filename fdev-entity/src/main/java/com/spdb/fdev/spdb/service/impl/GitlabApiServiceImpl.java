package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.GitlabTransport;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.service.IGitlabApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装 gitlab api 请求
 *
 * @author xxx
 * @date 2019/7/31 9:27
 */
@Component
@RefreshScope
public class GitlabApiServiceImpl implements IGitlabApiService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${gitlib.path}")
    private String gitlabUrl;
    private static String FILECONTEXT = "/repository/files/";
    private static String BRANCHCONTEXT = "/repository/branches/";
    @Autowired
    private GitlabTransport gitlabTransport;

    private String getProjectsUrl() {
        return this.gitlabUrl + "projects/";
    }


    /**
     * 检验分支是否存在
     *
     * @param token
     * @param gitlabId
     * @param branch
     */
    @Override
    public void checkBranch(String token, String gitlabId, String branch) throws Exception {
        String url = this.getProjectsUrl() + gitlabId + BRANCHCONTEXT + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        // 如果没有查到当前分支,此处会抛错
        this.gitlabTransport.submitGet(url, header);
    }

    /**
     * 获取 gitlab 文件内容
     *
     * @param token
     * @param gitlabId
     * @param featureBranch
     * @param fileDir
     * @return
     */
    @Override
    public String getFileContent(String token, String gitlabId, String featureBranch, String fileDir) throws Exception {
        String url = this.getProjectsUrl() + gitlabId + FILECONTEXT + fileDir + "/raw";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).queryParam(Dict.REF, featureBranch).build(true).toUri();
        Object content = null;
        try {
            content = this.gitlabTransport.submitGet(uri, headers);
        } catch (FdevException e) {
            if (e.getCode().equals(ErrorConstants.INVAILD_OPERATION_DATA)) {
                logger.error("应用:[{}], 分支:[{}], 文件路径:[{}], 未配置外部参数,出错信息:[{}]", gitlabId, featureBranch, fileDir, e.getCode());
            }
        }
        if (content == null) {
            content = "";
        }
        return content.toString();
    }

    /**
     * 向 对应的分支 创建文件,有则执行更新,无则创建
     *
     * @param token
     * @param gitlabId
     * @param branch
     * @param fileDir
     * @param content
     * @param commitMessage
     */
    @Override
    public void createFile(String token, String gitlabId, String branch, String fileDir, String content,
                           String commitMessage) {
        String url = this.getProjectsUrl() + gitlabId + FILECONTEXT + fileDir;
        Map<String, String> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.BRANCH, branch);
        param.put(Dict.CONTENT, content);
        param.put(Dict.COMMIT_MESSAGE, commitMessage);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
        try {
            this.gitlabTransport.submitPost(uri, param);
        } catch (Exception e) {
            try {
                param.remove(Dict.PRIVATE_TOKEN);
                uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
                this.gitlabTransport.submitPut(uri, token, param);
            } catch (Exception e1) {
                this.logger.error("####Update file error! rul=[{}], data=[{}]", url, param);
                throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"文件存在，请删除"});
            }
        }
    }
}
