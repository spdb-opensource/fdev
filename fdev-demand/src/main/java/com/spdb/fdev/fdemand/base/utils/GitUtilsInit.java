
package com.spdb.fdev.fdemand.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.File;

@RefreshScope
@Component
public class GitUtilsInit implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(GitUtilsInit.class);

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
            logger.error("在进行git pull的时候失败" + e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
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
     * 克隆项目
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
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR);
            } finally {
                if (git != null)
                    git.close();
            }

        }

        return cloneFlag;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        provider = new UsernamePasswordCredentialsProvider(gitlabUsername, gitlabPassword);

    }
}
