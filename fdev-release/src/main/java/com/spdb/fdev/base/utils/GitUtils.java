
package com.spdb.fdev.base.utils;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
@RefreshScope
public class GitUtils implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(GitUtils.class);

    private static final String SUFFIX = "/.git";

    @Value("${gitlab.manager.username}")
    private String gitlabUsername;

    @Value("${gitlab.manager.password}")
    private String gitlabPassword;

    private static UsernamePasswordCredentialsProvider provider = null;

    public static boolean gitPullFromGitlab(String diskDir) {
        File fileDir = new File(new File(diskDir).getAbsolutePath() + SUFFIX);
        try (Repository repo = new FileRepository(fileDir.getAbsolutePath());
             Git git = new Git(repo)
        ) {
            git.pull().setCredentialsProvider(provider).call();
            logger.info("在进行git pull 成功");
        } catch (Exception e) {
            logger.error("在进行git pull的时候失败", e);
            throw new FdevException(ErrorConstants.FILE_UPLOAD_GITLAB_FAILED);
        }
        return true;
    }


    /**
     * 克隆项目
     *
     * @param gitURI git地址
     * @param dir    本地存放项目的目录
     */
    public static boolean gitCloneFromGitlab(String gitURI, String dir) {
        boolean cloneFlag = false;
        File file = new File(dir);
        logger.info("Create mkdir{}success!",dir);
        file.mkdir();
        if (!StringUtils.isBlank(gitURI)) {
            try {
                CloneCommand clone = Git.cloneRepository().setRemote("origin")
                        .setCredentialsProvider(provider)
                        .setURI(gitURI)
                        .setDirectory(file);
                clone.call();
                cloneFlag = true;
                logger.info("clone project success from {} to {}", gitURI, clone.getRepository());
            } catch (Exception e) {
                logger.error("在进行git clone的时候失败", e);
                throw new FdevException(ErrorConstants.FILE_UPLOAD_GITLAB_FAILED);
            }
        }

        return cloneFlag;
    }

    /**
     * 提交文件到远程的分支
     *
     * @param diskDir
     * @param commitMessage
     * @throws Exception
     */
    public static void gitPushFromGitlab(String diskDir, String commitMessage) {
        try (Git git = new Git(new FileRepository(new File(diskDir + SUFFIX)))) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(commitMessage).call();
            git.status().call();
            // 将本地仓库提交到 远程仓库
            String error = push(git);
            if(!CommonUtils.isNullOrEmpty(error)) {
                logger.error("推送10次仍然失败，错误信息：{}", error);
                logger.error("推送失败，删除本地目录{}，不影响下一次克隆或拉取", diskDir);
                CommonUtils.deleteDirectory(new File(diskDir), diskDir);
                throw new FdevException(ErrorConstants.PROJECT_INIT_FAILED);
            }
            logger.info("在进行git push 成功");
        } catch (Exception e) {
            logger.info("gitlab push本地仓库路径是:{}",diskDir);
            logger.error("prod asset {} upload failed!", commitMessage, e);
            throw new FdevException(ErrorConstants.FILE_UPLOAD_GITLAB_FAILED);
        }
    }

    private static String push(Git git) throws GitAPIException, InterruptedException {
        String error = null;
        for(int i = 0;i < 10;i++) {
            Thread.sleep(1000);
            Iterable<PushResult> result = git.push().setRemote("origin").setCredentialsProvider(provider).call();
            error = result.iterator().next().getMessages();
            if(CommonUtils.isNullOrEmpty(error)) {
                logger.info("git推送{}次成功", i + 1);
                return null;
            }
        }
        return error;
    }

    /**
     * 删除文件到更新到远程的分支
     *
     * @param diskDir
     * @param commitMessage
     * @throws Exception
     */
    public static void deleteFilePushToGitlab(String diskDir, String commitMessage) {
        try (Git git = new Git(new FileRepository(new File(diskDir + SUFFIX)))) {
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage(commitMessage).call();
            git.status().call();
            // 将本地仓库提交到 远程仓库
            git.push().setRemote("origin").setCredentialsProvider(provider).call();
            logger.info("在进行git push 成功");
        } catch (Exception e) {
            logger.error("prod asset {} upload failed!", commitMessage, e);
            throw new FdevException(ErrorConstants.FILE_UPLOAD_GITLAB_FAILED);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        provider = new UsernamePasswordCredentialsProvider(gitlabUsername, gitlabPassword);
    }
}
