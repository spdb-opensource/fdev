package com.spdb.fdev.fdemand.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class GitUtils {


    private static Logger logger = LoggerFactory.getLogger(GitUtils.class);

    /**
     * 克隆项目
     *
     * @param gitURI git地址
     * @param dir    本地存放项目的目录
     */
    public static boolean gitCloneFromGitlab(String gitURI, String dir, String name, String password, String branch) {
        boolean cloneFlag = true;
        if (StringUtils.isBlank(branch))
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"请传入分支名称"});
        Git git = null;
        if (!StringUtils.isBlank(gitURI)) {
            try {
                git = Git.cloneRepository().setURI(gitURI).setDirectory(new File(dir)).setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).setBranch(branch).call();
            } catch (GitAPIException e) {
                logger.error("在进行git clone的时候失败" + e);
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git clone 失败"});
            } finally {
                if (git != null)
                    git.close();
            }
        }
        return cloneFlag;
    }

    /**
     * 提交文件到远程的分支
     *
     * @param filename
     * @throws Exception
     */
    public static void gitPushFromGitlab(Git git, String filename, String name, String password) {
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(filename).call();
            // 将本地仓库提交到 远程仓库
            git.push().setRemote("origin").setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).call();
            logger.info("在进行git push 成功");
        } catch (Exception e) {
            logger.error("在进行git push 失败 {}", e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git push 失败"});
        }
    }

    public static boolean gitPullFromGitlab(Git git, String name, String password, String branch) {
        boolean pullFlag = true;
        try {
            git.pull().setRemoteBranchName(branch).setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).call();
        } catch (Exception e) {
            pullFlag = false;
            logger.error("在进行git pull的时候失败" + e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git pull 失败"});
        }
        return pullFlag;
    }

    /**
     * 切换分支(等同于 git checkout branchname)
     *
     * @param branchName
     * @param git
     * @throws Exception
     */
    public static void checkoutBranch(String branchName, Git git) throws Exception {
        git.checkout().setName(branchName).call();
    }

    /**
     * 获取git实例
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static Git getGit(String filePath) throws Exception {
        return Git.open(new File(filePath + File.separator + ".git"));
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
     * 创建本地仓库分支并关联远程仓库分支(等同于 git checkout -b branchname origin/branchname)
     *
     * @param git
     * @param localBranchName
     * @param remoteBranchName
     * @throws Exception
     */
    public static void createLocalBranchAndRealectRemotBranch(Git git, String localBranchName, String remoteBranchName) throws Exception {
        git.checkout().setCreateBranch(true).setName(localBranchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setStartPoint("origin/" + remoteBranchName).call();
    }
}
