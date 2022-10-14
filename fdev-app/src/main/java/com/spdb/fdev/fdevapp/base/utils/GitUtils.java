package com.spdb.fdev.fdevapp.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class GitUtils {

    private static Logger logger = LoggerFactory.getLogger(GitUtils.class);
    private static UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider("fdev", "2020fdev");

    /**
     * 从 master 上 pull 项目
     * @param diskDir 本地存放项目的 路径
     * @return
     */
    public static boolean gitPullFromGitlab(String diskDir) {
        boolean pullFlag = true;
        Git git = null;
        try {
            git = Git.open(new File(diskDir + "/.git"));
            git.pull().setRemoteBranchName(Dict.MASTER).setCredentialsProvider(provider).call();
            git.close();
        } catch (Exception e) {
            pullFlag = false;
            logger.error("在进行git pull的时候失败" + e);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git pull 失败"});
        } finally {
            if (git != null) {
                git.close();
            }
        }

        return pullFlag;
    }

    /**
     * 克隆项目
     * @param gitURI git地址
     * @param dir 本地存放项目的目录
     */
    public static boolean gitCloneFromGitlab(String gitURI, String dir) {
        boolean cloneFlag = true;
        if (!StringUtils.isBlank(gitURI)) {
            try {
                Git.cloneRepository().setURI(gitURI).setDirectory(new File(dir)).setCredentialsProvider(provider).call();
            } catch (GitAPIException e) {
                cloneFlag = false;
                logger.error("在进行git clone的时候失败" + e);
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git clone 失败"});
            }
        }

        return cloneFlag;
    }

    /**
     * 提交文件到远程的分支
     * @param diskDir
     * @param filename
     * @throws Exception
     */
    public static void gitPushFromGitlab(String diskDir, String filename) {


        Git git = null;
        try {
            git = new Git(new FileRepository(diskDir + "/.git"));
            git.add().addFilepattern(".").call();
            git.commit().setMessage(filename).call();
            // 将本地仓库提交到 远程仓库
            git.push().setRemote("origin").setCredentialsProvider(provider).call();
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

    /**
     * 克隆项目
     * @param gitURI git地址
     * @param dir 本地存放项目的目录
     */
    public static boolean gitCloneFromGitlab(String gitURI, String dir,String name,String password,String branch) {
        boolean cloneFlag = true;
        if(StringUtils.isBlank(branch))
        	throw new FdevException(Constant.I_GITLAB_ERROR,new String[] {"请传入分支名称"});
        Git git = null;
        if (!StringUtils.isBlank(gitURI)) {
            try {
                logger.info("gitURI:" +gitURI + " dir :" + dir);
                logger.info("name:" +name + " password :" + password);
                git = Git.cloneRepository().setURI(gitURI).setDirectory(new File(dir)).setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).setBare(false).setBranch(branch).call();
            } catch (GitAPIException e) {
                cloneFlag = false;
                logger.error("在进行git clone的时候失败" + e);
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"git clone 失败"});
            }finally {
            	if(git!=null)
            		git.close();
            }
        }
        return cloneFlag;
    }
    
    /**
     * 提交文件到远程的分支
     * @param filename
     * @throws Exception
     */
    public static void gitPushFromGitlab(Git git,String filename,String name,String password) {
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
    
    public static boolean gitPullFromGitlab(Git git,String name,String password,String branch) {
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
     * 创建分支，如已存在该分支，检查overwired,如为true，删除原有分支，重新创建，如为false，则不创建分支
     * @param branchName
     * @param git
     * @param overWired
     * @throws Exception
     */
    public static void createBranch(Git git,String branchName,boolean overWired) throws Exception {
    	if(checkLocalExisitBranch(git,branchName)) {
    		if(!overWired)
    			return;
    		deleteBranch(git,branchName);
    	}
    	createBranch(git,branchName);
    }
    
    /**
     * 创建分支
     * @param git
     * @param branchName
     * @throws Exception
     */
    public static void createBranch(Git git,String branchName) throws Exception{
    	git.branchCreate().setName(branchName).call();
    }
    
    /**
     * 删除指定分支 (等同于 git branch -d branchname)
     * @param git
     * @param branchName
     * @throws Exception
     */
    public static void deleteBranch(Git git,String branchName) throws Exception{
    	git.branchDelete().setBranchNames(branchName).call();
    }
    
    /**
     * 删除多个分支
     * @param git
     * @param branchName
     * @throws Exception
     */
    public static void deleteBranches(Git git,String [] branchName) throws Exception{
		git.branchDelete().setBranchNames(branchName).call();
    }
    /**
     * 切换分支(等同于 git checkout branchname)
     * @param branchName
     * @param git
     * @throws Exception
     */
    public static void checkoutBranch(String branchName,Git git) throws Exception{
    	git.checkout().setName(branchName).call();
    }
    
    /**
     * 获取git实例
     * @param filePath
     * @return
     * @throws Exception
     */
    public static Git getGit(String filePath) throws Exception{
    	return Git.open(new File(filePath+File.separator+".git"));
    }
    
    /**
     * 判断本地是否存在指定分支
     * @param git
     * @param branchName
     * @return
     * @throws Exception
     */
    public static boolean checkLocalExisitBranch(Git git,String branchName) throws Exception{
    	String newBranchIndex = "refs/heads/"+branchName;
    	List<Ref> refList = git.branchList().call();
    	for(Ref ref : refList) {
    		if(newBranchIndex.equals(ref.getName())) 
    			return true;
    	}
    	return false;
    }
    
    /**
     * 创建本地仓库分支并关联远程仓库分支(等同于 git checkout -b branchname origin/branchname)
     * @param git
     * @param localBranchName
     * @param remoteBranchName
     * @throws Exception
     */
    public static void createLocalBranchAndRealectRemotBranch(Git git,String localBranchName,String remoteBranchName) throws Exception{
    	git.checkout().setCreateBranch(true).setName(localBranchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
    	.setStartPoint("origin/"+remoteBranchName).call();
    }
}
