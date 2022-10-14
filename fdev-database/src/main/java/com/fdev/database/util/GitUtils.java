
package com.fdev.database.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@RefreshScope
public class GitUtils implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(GitUtils.class);

    @Value("${gitlab.manager.username}")
    private String gitlabUsername;

    @Value("${gitlab.manager.password}")
    private String gitlabPassword;

    private static UsernamePasswordCredentialsProvider provider = null;

    public static boolean gitPullFromGitlab(String diskDir) {
        boolean pullFlag = true;
        Git git = null;
        Repository repo = null;
        File fileDir = new File(new File(diskDir).getAbsolutePath() + "/.git");
        try {
            repo = new FileRepository(fileDir.getAbsolutePath());
            git = new Git(repo);
            git.pull().setCredentialsProvider(provider).call();
            logger.info(diskDir + "在进行git pull 成功");
        } catch (Exception e) {
            pullFlag = false;
            logger.error("在进行git pull的时候失败" + e);
            throw new FdevException(ErrorConstants.FILE_UPLOAD_GITLAB_FAILED);
        } finally {
            if (git != null) {
                git.close();
            }
            if (repo != null) {
                repo.close();
            }
        }

        return pullFlag;
    }


    /**
     * 克隆master项目
     *
     * @param gitURI git地址
     * @param dir    本地存放项目的目录
     */
    public static boolean gitCloneFromGitlab(String gitURI, String dir) {
        boolean cloneFlag = false;
        File file = new File(dir);
        logger.info("Create mkdir " + dir + " success!");
        file.mkdir();
        Git git = null;
        if (!StringUtils.isBlank(gitURI)) {
            try {
                CloneCommand clone = Git.cloneRepository()
                        .setCredentialsProvider(provider)
                        .setURI(gitURI)
                        .setDirectory(file);
                git = clone.call();
                cloneFlag = true;
                logger.info("clone project success from " + gitURI + " to " + clone.getRepository());
            } catch (Exception e) {
                logger.error("在进行git clone的时候失败", e);
                throw new FdevException(ErrorConstants.FILE_UPLOAD_GITLAB_FAILED);
            } finally {
                if (git != null)
                    git.close();
            }

        }

        return cloneFlag;
    }

    /**
     * 克隆指定分支项目
     * @param gitURI git地址
     * @param dir 本地存放项目的目录
     */
    public static boolean gitCloneFromGitlab(String gitURI, String dir,String name,String password,String branch) {
        boolean cloneFlag = true;
        if(StringUtils.isBlank(branch))
            throw new FdevException(ErrorConstants.I_GITLAB_ERROR,new String[] {"请传入分支名称"});
        Git git = null;
        if (!StringUtils.isBlank(gitURI)) {
            try {
                git = Git.cloneRepository().setURI(gitURI)
                        .setDirectory(new File(dir))
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password))
                        .setBranch(branch).call();
            } catch (GitAPIException e) {
                logger.error("在进行git clone的时候失败" + e);
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git clone 失败"});
            }finally {
                if(git!=null)
                    git.close();
            }
        }
        return cloneFlag;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        provider = new UsernamePasswordCredentialsProvider(gitlabUsername, gitlabPassword);

    }


    /**
     * 判断本地是否存在指定分支
     *
     * @param git
     * @param branchName
     * @return
     * @throws Exception
     */
    public static boolean checkLocalExisitBranch(Git git, String branchName) throws Exception {
        String newBranchIndex = "refs/heads/" + branchName;
        List<Ref> refList = git.branchList().call();
        for (Ref ref : refList) {
            if (newBranchIndex.equals(ref.getName()))
                return true;
        }
        return false;
    }

    /**
     * http请求工具，通过gitlab接口获取相关信息
     *
     * @param url
     * @return
     */
    public static String httpMethodGetExchange(String url, RestTemplate restTemplate, String gitlabToken) {
        System.out.println("url:" + url);
        //http header配置
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Private-Token", gitlabToken);
        //http body配置
        /*MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("search", "CommonModules");
        HttpEntity httpEntity = new HttpEntity(httpHeaders，multiValueMap);*/
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        ResponseEntity<String> result;
        String body;
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            body = result.getBody();
        } catch (Exception e) {
            return null;
        }
        return body;
    }

    /**
     * json字符串转list集合
     *
     * @param body
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> stringToList(String body) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(body);
        if (jsonArray.length() > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = objectMapper.readValue(jsonObject.toString(), Map.class);
                list.add(map);
            }
        }
        return list;
    }

    

}
