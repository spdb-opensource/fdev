package com.fdev.docmanage.util;

import com.fdev.docmanage.dict.Dict;
import com.fdev.docmanage.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;

public class GitUtil {
    private static Logger logger = LoggerFactory.getLogger(GitUtil.class);

    @Resource
    private RestTemplate restTemplate;

    private static RestTemplate rest;

    @PostConstruct
    public void init() {
        this.rest = restTemplate;
    }

    /**
     * 克隆项目
     *
     * @param gitURI   git地址
     * @param dir      本地存放项目的目录
     * @param name
     * @param password
     * @param branch
     */
    public static void gitCloneFromGitlab(String gitURI, String dir, String name, String password, String branch) {
        Git git = null;
        try {
            git = Git.cloneRepository().setURI(gitURI).setDirectory(new File(dir)).setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).setBranch(branch).call();
        } catch (Exception e) {
            logger.error("在进行git clone的时候失败" + e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git clone 失败"});
        } finally {
            if (git != null)
                git.close();
        }
    }

    /**
     * 从 master 上 pull 项目
     *
     * @param tmpDir
     * @param name
     * @param password
     */
    public static void gitPullFromGitlab(String tmpDir, String name, String password) {
        boolean pullFlag = true;
        Git git = null;
        try {
            git = Git.open(new File(tmpDir));
            git.pull().setRemoteBranchName(Dict.MASTER).setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).call();
            git.close();
        } catch (Exception e) {
            logger.error("在进行git pull的时候失败" + e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git pull 失败"});
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    /**
     * 提交文件到远程的分支
     *
     * @param dir
     * @param commit
     * @param name
     * @param password
     * @throws Exception
     */
    public static void gitPushFromGitlab(String dir, String commit, String name, String password) {
        Git git = null;
        try {
            git = Git.open(new File(dir));
            git.add().addFilepattern(".").call();
            git.commit().setMessage(commit).call();
            // 将本地仓库提交到 远程仓库
            git.push().setRemote("origin").setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).call();
            git.close();
            logger.info("在进行git push 成功");
        } catch (Exception e) {
            logger.error("在进行git push 失败 {}", e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git push 失败"});
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

}
