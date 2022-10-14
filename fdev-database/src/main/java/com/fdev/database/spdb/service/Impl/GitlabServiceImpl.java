package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.dao.GitlabDao;
import com.fdev.database.spdb.service.GitlabService;
import com.fdev.database.util.GitUtils;
import com.spdb.fdev.common.exception.FdevException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;


@Service
@RefreshScope
public class GitlabServiceImpl implements GitlabService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Resource
    private GitlabDao gitlabDao;

    @Value("${gitlab.manager.username}")
    private String name;

    @Value("${gitlab.manager.password}")
    private String password;

    /**
     * 调用app模块接口获取所有应用，判断进行git clone或者git pull操作
     *
     * @param name_en 项目名
     * @param git     git路径
     */
    @Override
    public void gitOperation(String name_en, String git, String apps_path,String branch) {
        // 本地仓库的 地址
        String localRepository = apps_path +branch+"/"+ name_en;
        File partDirGit = new File(localRepository + "/.git");
        try {
            // 首次本地没有项目的时候进行克隆
            if (!partDirGit.exists()) {
                // git clone
                GitUtils.gitCloneFromGitlab(git, localRepository,  name, password, branch);
                logger.info(git + "克隆项目文件时成功");
            }
            GitUtils.gitPullFromGitlab(localRepository);
        } catch (Exception e) {
            logger.error("未查询到当前分支信息");
//            FileUtils.deleteFile(new File(localRepository));
            throw new FdevException(ErrorConstants.BRANCH_DONOT_EXIST);//当前分支不存在
        }
    }

    @Override
    public List<Map<String, Object>> findCommitDiff(Integer projectId, String commitId) throws Exception {
        return gitlabDao.findCommitDiff(projectId, commitId);
    }

    @Override
    public Map<String, Object> compareBranch(Integer projectId, String sourceBranch, String mergeBranch) throws Exception {
        return gitlabDao.compareBranch(projectId, sourceBranch, mergeBranch);
    }

}
