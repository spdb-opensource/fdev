package com.spdb.fdev.fdevapp.spdb.service.impl;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.ShellUtils;
import com.spdb.fdev.fdevapp.spdb.service.IArchetypeService;
import com.spdb.fdev.transport.RestTransport;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: luotao
 * Date: 2019/2/27
 * Time: 下午4:46
 * To change this template use File | Settings | File Templates.
 */
@Service
public class ArchetypeServiceImpl implements IArchetypeService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private RestTransport restTransport;

    @Override
    public Map<String, Object> queryArchetype(String id) throws Exception {
        Map param = new HashMap<>();
        param.put(Dict.ID,id);
        param.put(Dict.REST_CODE,"queryLastArchetypes");
        logger.info("queryArchetype execute...");
        Object submit = restTransport.submit(param);
        logger.info("queryArchetype execute...end");
        logger.info(submit == null?"submit is null":"submit is not null");
        List<Map> parseArray = (List<Map>) submit;
        if (CommonUtils.isNullOrEmpty(parseArray)){
            logger.info("queryArchetype execute error...");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"", " 该版本骨架不存在于仓库中!"});
        }else
            return parseArray.get(0);
    }

    @Override
    public boolean createArchetype(Map archetype, String http_url_to_repo, String gitlabPath, String gitlabName, String gitlabPassword, String projectName, String token) throws Exception {
        /* 将项目 clone 到本地 */
        String localPath = "/tmp/";//服务器上的目录
        String currentDir = localPath + projectName;
        String result = cloneRepository(gitlabName, gitlabPassword, http_url_to_repo, currentDir);
        if (null != result && Dict.SUCCESS == result) {
            /* 通过mvn命令生成应用骨架 */
            boolean b = ShellUtils.cellShell(archetype, projectName);
            if (true == b) {
                /* 将骨架项目推到 git */
                String message = commit(gitlabName, gitlabPassword, localPath + projectName, Dict.INIT_PROJECT);
                logger.info("message:" + message);
                Object runCmd = CommonUtils.runCmd("rm -rf " + currentDir + "/");
                if (null == runCmd) {
                    logger.info("@@@@@@@ delete dir is failed in after clone");
                }
                logger.info("@@@@@@@ delete dir is success in after clone");
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /*
    将项目 clone 到本地
    */
    public String cloneRepository(String gitlabName, String gitlabPassword, String url, String localPath) {
        File file = new File(localPath);
        try {
            Git.cloneRepository().setURI(url).setDirectory(file).setCloneAllBranches(true)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitlabName, gitlabPassword)).call();
            logger.info("cloneRepository");
        } catch (GitAPIException e) {
            logger.error("cloneRepository error");
            return Dict.ERROR;
        }
        return Dict.SUCCESS;
    }

    /*
     将骨架项目推到 git
     */
    public String commit(String gitlabName, String gitlabPassword, String localPath, String pushMessage) {
        Git git = null;

        try {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                logger.error("error message:"+e);
                //重新设置线程中断标志为true
                Thread.currentThread().interrupt();
            }
            git = Git.open(new File(localPath));
            Status status = null;
            try {
                status = git.status().call();
            } catch (GitAPIException e) {
                logger.error("error message:"+e);
            }
            if (status!=null&&status.hasUncommittedChanges() == true) {
                //log 没有修改的文件
                logger.info("没有修改的文件");
                return Dict.ERROR;
            }
            try {
                git.add().addFilepattern(".").call();
                git.commit().setMessage(pushMessage).call();
                git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitlabName, gitlabPassword)).call();
                //view log message
                for (RevCommit revCommit : git.log().call()) {
                    //log.debug  revCommit
                    logger.info(revCommit.getFullMessage());
                    logger.info(revCommit.getCommitterIdent().getName());
                    logger.info(revCommit.getCommitterIdent().getEmailAddress());
                }
            } catch (GitAPIException e) {
                logger.error("error message:"+e);
            }
        } catch (IOException e) {
            logger.error("error message:"+e);
        }finally {
            if (git != null) {
                git.close();
            }
        }
        return Dict.SUCCESS;
    }

}
