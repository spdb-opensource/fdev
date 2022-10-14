package com.spdb.fdev.fdevapp.spdb.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.service.IComponentService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabUserService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.GitUtils;
import com.spdb.fdev.fdevapp.base.utils.GitlabTransport;
import com.spdb.fdev.fdevapp.spdb.dao.IGitlabCIDao;
import com.spdb.fdev.fdevapp.spdb.entity.GitlabCI;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabCIService;

@Service
@RefreshScope
public class GitlabCIServiceImpl implements IGitlabCIService {
    private static Logger logger = LoggerFactory.getLogger(GitlabCIServiceImpl.class);// 控制台日志打印
    @Autowired
    private IGitlabCIDao gitlabcisdao;

    @Autowired
    private GitlabAPIServiceImpl apiService;

    @Autowired
    private IComponentService componentService;

    @Autowired
    private IGitlabUserService gitlabUserService;

    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/

    @Value("${gitlib.fdev-ci-template.id}")
    private String templateId;

    @Value("${gitlib.fdev-ci-template.branch}")
    private String branch;

    @Value("${gitlab.fileDir}")
    private String local_temp_repo;

    @Value("${gitlab.name}")
    private String name;

    @Value("${gitlab.password}")
    private String password;

    @Value("${gitlib.fdev-ci-template.name}")
    private String fdevCiTemplateName;

    @Value("${continuous.intergration.file}")
    private String continuous_file;

    @Value("${continuous.ignore}")
    private String continuous_ignore;

    @Value("${continuous.except}")
    private String continuous_except;


    @Autowired
    private GitlabTransport gitlabTransport;

    private Object lock = new Object();

    @Override
    public GitlabCI save(GitlabCI gitlabci) throws Exception {
        return gitlabcisdao.save(gitlabci);
    }

    @Override
    public List<GitlabCI> query(GitlabCI gitlabci) throws Exception {
        return gitlabcisdao.query(gitlabci);
    }

    @Override
    public GitlabCI update(GitlabCI gitlabci) throws Exception {
        return gitlabcisdao.update(gitlabci);
    }

    @Override
    public GitlabCI findById(String id) throws Exception {
        return gitlabcisdao.findById(id);
    }

    @Override
    public GitlabCI findByName(String yaml_name) throws Exception {
        return gitlabcisdao.findByName(yaml_name);
    }

    /**
     * 通过模板id查询-ci.yml文件的文件名
     *
     * @param gitlabciId 模板id
     * @return
     * @throws Exception
     */
    public String findYaml_nameById(String gitlabciId) throws Exception {
        GitlabCI gitlabCI = this.findById(gitlabciId);
        return gitlabCI.getYaml_name();
    }

    /**
     * 通过项目id查询项目名称
     *
     * @param projectId 项目id
     * @param token     AccessToken验证用户
     * @return
     * @throws Exception
     */
    public String getProjectNameById(String projectId, String token) throws Exception {
        Object result = apiService.queryProjectById(projectId, token);
        JSONArray json = JSONArray.parseArray((String) result);
        Map map = (Map) json.get(0);
        String projectName = (String) map.get(Dict.NAME);
        return projectName;

    }

    /**
     * 查询目录下所有文件的文件名称
     *
     * @param
     * @param token token AccessToken验证用户
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> queryFileNameByPath(String dirPath, String token) throws Exception {
        String pro_url = CommonUtils.projectUrl(url);
        String dirUrl = pro_url + "/" + templateId + "/repository/tree?path=" + dirPath + "&ref=" + this.branch; // Dockerfile等文件所在的目录
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String content = (String) gitlabTransport.submitGet(dirUrl, header);
        JSONArray json = JSONArray.parseArray(content);
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Object object : json) {
            Map map = (Map) object;
            list.add(map);
        }
        return list;
    }

    /**
     * 通过模板id获取-ic.yml文件的内容
     *
     * @param
     * @param token AccessToken验证用户
     * @return 返回文件内容
     * @throws Exception
     */
    public Map getContentAll(String token, String dirPath, String projectName) throws Exception {
        String pro_url = CommonUtils.projectUrl(url);
        List filesList = this.queryFileNameByPath(dirPath, token);
        String content = null;
        String updateContent = null;
        Map contentMap = new HashMap<>();
        for (int i = 0; i < filesList.size(); i++) {
            Map fileMap = (Map) filesList.get(i);
            if (fileMap.get(Dict.TYPE).equals(Dict.TREE)) {
                contentMap = this.getContent(token, (String) fileMap.get(Dict.PATH), projectName);
            } else {
                MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
                String filePath = (String) fileMap.get(Dict.PATH);
                String replacePath = filePath.replace("/", "%2F");
                replacePath = replacePath.replace(".", "%2E");
                String fileContentUrl = pro_url + "/" + templateId + "/repository/files/" + replacePath + "/raw"; // 查询的文件的路径
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fileContentUrl).queryParam(Dict.REF,
                        branch);
                URI uri = builder.build(true).toUri();
                header.add(Dict.PRIVATE_TOKEN, token);
                content = (String) gitlabTransport.submitGet(uri, header);
                updateContent = content.replace("^project_name^", projectName);
                contentMap.put(fileMap.get(Dict.NAME), updateContent);
            }
        }

        return contentMap;
    }

    public Map getContent(String token, String dirPath, String projectName) throws Exception {
        String pro_url = CommonUtils.projectUrl(url);
        List filesList = this.queryFileNameByPath(dirPath, token);
        String content = null;
        String updateContent = null;
        Map contentMap = new HashMap<>();
        for (int i = 0; i < filesList.size(); i++) {
            Map fileMap = (Map) filesList.get(i);
            MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
            String filePath = (String) fileMap.get(Dict.PATH);
            String replacePath = filePath.replace("/", "%2F");
            replacePath = replacePath.replace(".", "%2E");
            String fileContentUrl = pro_url + "/" + templateId + "/repository/files/" + replacePath + "/raw"; // 查询的文件的路径
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fileContentUrl).queryParam(Dict.REF, branch);
            URI uri = builder.build(true).toUri();
            header.add(Dict.PRIVATE_TOKEN, token);
            content = (String) gitlabTransport.submitGet(uri, header);
            updateContent = content.replace("^project_name^", projectName);
            contentMap.put(fileMap.get(Dict.NAME), updateContent);

        }

        return contentMap;
    }

    public String getFileContent(String token, Map mapFile, String projectName) throws Exception {
        String pro_url = CommonUtils.projectUrl(url);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        String dirPath = (String) mapFile.get(Dict.PATH);
        String replacePath = dirPath.replace("/", "%2F");
        replacePath = replacePath.replace(".", "%2E");
        String fileContentUrl = pro_url + "/" + templateId + "/repository/files/" + replacePath + "/raw"; // 查询的文件的路径
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fileContentUrl).queryParam(Dict.REF, branch);
        URI uri = builder.build(true).toUri();
        header.add(Dict.PRIVATE_TOKEN, token);
        String content = (String) gitlabTransport.submitGet(uri, header);
        content = content.replace("^project_name^", projectName);
        return content;
    }

    public String quickCreatFile(String projectId, String token, String fileName, String replacePath, Map contentMap) throws Exception {
        Map<String, String> param = new HashMap<String, String>();
        String con = (String) contentMap.get(fileName);
        String fileCreatUrl = CommonUtils.projectUrl(url) + "/" + projectId + "/repository/files/" + replacePath;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fileCreatUrl);
        URI uri = builder.build(true).toUri();
        logger.info("@@@@@@@@@@ fileCreatUrl:" + uri.toString());
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.CONTENT, con);
        param.put(Dict.BRANCH, Dict.MASTER);
        param.put(Dict.COMMIT_MESSAGE, Dict.ADD_MODEL_FILE);
        return (String) gitlabTransport.submitPost(uri, param);

    }

    public void saveTempletateFile(String token, String templatePath, String projectName) throws Exception {
        try {
            List listFiles = this.queryFileNameByPath(templatePath, token);
            String localProjectPath = local_temp_repo + projectName;
            String file_path;
            String type;
            String file_name;
            String sub_file_path;
            for (Object o : listFiles) {
                Map mapFiles = (Map) o;
                file_path = (String) mapFiles.get(Dict.PATH);
                type = (String) mapFiles.get(Dict.TYPE);
                file_name = (String) mapFiles.get(Dict.NAME);
                sub_file_path = file_path.substring(file_path.indexOf("/") + 1);
                if (Dict.TREE.equals(type)) {//代表文件夾
                    File file = new File(localProjectPath + File.separator + sub_file_path);
                    if (!file.exists())
                        file.mkdirs();
                    saveTempletateFile(token, file_path, projectName);
                } else {//代表文件
                    String content = this.getFileContent(token, mapFiles, projectName);
                    File file = new File(localProjectPath + File.separator + sub_file_path);
                    CommonUtils.writeFile(file, content);
                }
            }
        } catch (Exception e) {
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"保存持续集成模板文件至" + projectName + "出错！"});
        }

    }

    /**
     * 应用录入使用
     *
     * @param projectId        gitlab project Id
     * @param token            gitlab 操作 token
     * @param templatePath     持续集成模板
     * @param projectName      项目名
     * @param http_url_to_repo
     * @param fileEncoding     应用编码类型
     * @return
     * @throws Exception
     */
    @Override
    public boolean addDevops(String projectId, String token, String templatePath, String projectName, String http_url_to_repo, String fileEncoding) throws Exception {
        String fdevCiTemplatePath = local_temp_repo + fdevCiTemplateName;//持续集成模板文件本仓库路径
        String localTempProjectPath = local_temp_repo + projectName;    //存放待持续集成项目的本地临时路径
        Git git = null;//持续集成模板文件本地仓库的句柄
        Git git1 = null;//待被持续集成项目本地仓库的句柄
        synchronized (lock) {
            try {
                gitlabUserService.changeFdevRole(projectId, Dict.MAINTAINER);//设置权限，增加为Maintainer
                GitUtils.gitCloneFromGitlab(http_url_to_repo, localTempProjectPath, name, password, Dict.MASTER);//将待持续集成的项目clone到本地
                File file = new File(fdevCiTemplatePath);
                if (!file.exists()) {//当本地仓库不存在模板仓库时执行
                    String repoUrl = (String) getProjectDeatilByProjectId(token, templateId).get(Dict.HTTP_URL_TO_REPO);
                    GitUtils.gitCloneFromGitlab(repoUrl, fdevCiTemplatePath, name, password, branch);//将待持续集成的项目clone到本地
                }
                git = GitUtils.getGit(fdevCiTemplatePath);
                if (!GitUtils.checkLocalExisitBranch(git, branch)) {//判断本地持续集成模板文件本地仓库是否存在对应分支
                    GitUtils.createLocalBranchAndRealectRemotBranch(git, branch, branch);
                } else {
                    if (!branch.equals(git.getRepository().getBranch()))//判断持续集成模板文件本地仓库是否是对应的环境分支，若不是切切换当前环境分支
                    {
                        GitUtils.checkoutBranch(branch, git);
                    }
                }
                GitUtils.gitPullFromGitlab(git, name, password, branch);//通过指定分支pull远程分支至本地
                FileUtils.copyDirectory(new File(fdevCiTemplatePath + File.separator + templatePath), new File(localTempProjectPath));//将模板文件的内容拷贝至本地临时项目
                List<String> filePahtList = new ArrayList<String>();
                CommonUtils.getReplacePathFiles(new File(fdevCiTemplatePath + File.separator + templatePath), fdevCiTemplateName + File.separator + templatePath, projectName, filePahtList);
                CommonUtils.replaceStrByFilePathList(filePahtList, "^project_name^", projectName);

                //若编码不为null的情况，不需要修改
                if (null != fileEncoding) {
                    //获取setEnv.sh文件
                    String setEnvFilePath = localTempProjectPath + File.separator + "gitlab-ci" + File.separator + "setEnv.sh";
                    File setEnvFile = new File(setEnvFilePath);
                    logger.info("setEnv文件路径:" + setEnvFilePath);
                    if (null != setEnvFile) {
                        try (BufferedReader br = new BufferedReader(new FileReader(setEnvFile));){
                            String line = null;
                            String replaceLine = null;
                            //export JAVA_OPTIONS="-Dfile.encoding=GBK"
                            while ((line = br.readLine()) != null) {
                                logger.info("fileEncoding:" + line);
                                if (line.contains("-Dfile.encoding")) {
                                    logger.info("fileEncoding JAVA_OPTIONS:" + line);
                                    replaceLine = handleEncodingStr(line, fileEncoding);
                                    break;
                                }
                            }
                            if (null != replaceLine){
                                logger.info("-Dfile.encoding=GBK替换后为" + replaceLine);
                                logger.info("-Dfile.encoding=GBK替换前为" + line);
                                CommonUtils.replaceFirstStrInFile(setEnvFile, line, replaceLine);
                            } else {
                                logger.error("修改setEnv.sh文件失败");
                            }
                        } catch (IOException e) {
                            logger.error("setEnv.sh文件读取失败", e);
                        }
                    }
                }

                git1 = GitUtils.getGit(localTempProjectPath);
                GitUtils.gitPushFromGitlab(git1, "添加持续集成文件", name, password);//将本地临时项目推送到远程
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"添加持续集成模板文件失败！"});
            } finally {
                if (git != null)
                    git.close();
                if (git1 != null)
                    git1.close();
                CommonUtils.deleteFile(new File(localTempProjectPath));//清除本地项目临时仓库
                gitlabUserService.changeFdevRole(projectId, Dict.REPORTER);//回收权限，重新设置为Reporter
            }
            return true;
        }
    }

    /**
     * 异步新增应用使用
     *
     * @param projectId        gitlab project Id
     * @param token            gitlab 操作 token
     * @param templatePath     持续集成模板
     * @param projectName      项目名
     * @param http_url_to_repo
     * @param archetype
     * @param fileEncoding     应用编码类型
     * @return
     * @throws Exception
     */
    @Override
    public boolean addDevops(String projectId, String token, String templatePath, String projectName, String http_url_to_repo, Map archetype, String fileEncoding) throws Exception {
        String fdevCiTemplatePath = local_temp_repo + fdevCiTemplateName;//持续集成模板文件本仓库路径
        String localTempProjectPath = local_temp_repo + projectName;    //存放待持续集成项目的本地临时路径
        Git git = null;//持续集成模板文件本地仓库的句柄
        Git git1 = null;//待被持续集成项目本地仓库的句柄
        synchronized (lock) {
            try {
                gitlabUserService.changeFdevRole(projectId, Dict.MAINTAINER);//设置权限，增加为Maintainer
                GitUtils.gitCloneFromGitlab(http_url_to_repo, localTempProjectPath, name, password, Dict.MASTER);//将待持续集成的项目clone到本地
                File file = new File(fdevCiTemplatePath);
                if (!file.exists()) {//当本地仓库不存在模板仓库时执行
                    String repoUrl = (String) getProjectDeatilByProjectId(token, templateId).get(Dict.HTTP_URL_TO_REPO);
                    GitUtils.gitCloneFromGitlab(repoUrl, fdevCiTemplatePath, name, password, branch);//将待持续集成的项目clone到本地
                }
                git = GitUtils.getGit(fdevCiTemplatePath);
                if (!GitUtils.checkLocalExisitBranch(git, branch)) {//判断本地持续集成模板文件本地仓库是否存在对应分支
                    GitUtils.createLocalBranchAndRealectRemotBranch(git, branch, branch);
                } else {
                    if (!branch.equals(git.getRepository().getBranch()))//判断持续集成模板文件本地仓库是否是对应的环境分支，若不是切切换当前环境分支
                        GitUtils.checkoutBranch(branch, git);
                }
                GitUtils.gitPullFromGitlab(git, name, password, branch);//通过指定分支pull远程分支至本地
                //判断新增应用是否为骨架项目，进行拷贝持续集成文件判断
                List<String> exceptList = new ArrayList<>();
                String[] exceptArray = continuous_except.split(",");
                if (!CommonUtils.isNullOrEmpty(exceptArray)) {
                    exceptList = Arrays.asList(exceptArray);
                }
                if (exceptList.size() > 0 && exceptList.contains(templatePath)) {
                    //如果是mobcli-vue，先拷贝.gitnore和.gitlab-ci.yml,再拷贝gitlab-ci到ci
                    FileUtils.copyFileToDirectory(new File(fdevCiTemplatePath + File.separator + templatePath + File.separator + ".gitignore"), new File(localTempProjectPath));
                    FileUtils.copyFileToDirectory(new File(fdevCiTemplatePath + File.separator + templatePath + File.separator + ".gitlab-ci.yml"), new File(localTempProjectPath));
                    FileUtils.copyDirectory(new File(fdevCiTemplatePath + File.separator + templatePath + File.separator + "gitlab-ci"), new File(localTempProjectPath+"/ci"));
                } else {
                    FileUtils.copyDirectory(new File(fdevCiTemplatePath + File.separator + continuous_ignore), new File(localTempProjectPath));//将模板文件的内容拷贝至本地临时项目
                    String templateDirectory = CommonUtils.getTemplateByArcheType((String) archetype.get("name_en"));
                    if("" != templateDirectory) {
                        //将持续集成模板里的.gitlab-ci.yaml文件放到应用根目录
                        FileUtils.copyFileToDirectory(new File(fdevCiTemplatePath + File.separator + templateDirectory + "/.gitlab-ci.yml"), new File(localTempProjectPath));
                        //再将gitlab-ci文件夹下的deployment.yaml、selector.yaml和Dockerfile放到ci文件夹
                        FileUtils.copyFileToDirectory(new File(fdevCiTemplatePath + File.separator + templateDirectory + "/gitlab-ci/deployment.yaml"), new File(localTempProjectPath+"/ci"));
                        FileUtils.copyFileToDirectory(new File(fdevCiTemplatePath + File.separator + templateDirectory + "/gitlab-ci/selector.yaml"), new File(localTempProjectPath+"/ci"));
                        FileUtils.copyFileToDirectory(new File(fdevCiTemplatePath + File.separator + templateDirectory + "/gitlab-ci/Dockerfile"), new File(localTempProjectPath+"/ci"));
                    }
                }
                //对持续集成的部分文件做关键字替换
                List<String> filePahtList = new ArrayList<>();
                String[] fileArray = continuous_file.split(",");
                if (!CommonUtils.isNullOrEmpty(fileArray)) {
                    for (String path : fileArray) {
                        String filePath = localTempProjectPath + File.separator + path;
                        File pathFile = new File(filePath);
                        if (pathFile == null) {
                            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{path + "模板文件为空！"});
                        }
                        CommonUtils.getContinuousPathFiles(pathFile, filePahtList);
                    }
                }
                CommonUtils.replaceStrByFilePathList(filePahtList, "^project_name^", projectName);
                //持续集成文件增加环境信息
                if (Constant.ENV_SIT.equals(branch) || Constant.ENV_REL.equals(branch)) {
                    String ciPath = localTempProjectPath + File.separator + ".gitlab-ci.yml";
                    File ciFile = new File(ciPath);
                    CommonUtils.addCIFile(ciFile, branch);
                }
                //非前端骨架，且骨架编码为GBK，替换环境配置模版文件
                if (archetype != null
                        && Constant.GBK.equals(archetype.get(Constant.ENCODING))
                        && !Constant.VUE.equals(archetype.get(Dict.TYPE))) {
                    String content = componentService.queryConfigTemplate(Dict.MASTER, (String) archetype.get(Dict.ID));
                    if (StringUtils.isNotBlank(content)) {
                        String configPath = localTempProjectPath + File.separator + "ci/fdev-application.properties";
                        File configFile = new File(configPath);
                        if (configFile != null) {
                            CommonUtils.replaceContent(configFile, content);
                        } else {
                            logger.error("应用环境配置模块文件不存在");
                        }
                    }
                }
                // 获取最新的镜像版本
                Map<String, String> paramMap = new HashMap<>();
                String dockerFilePath = localTempProjectPath + File.separator + "ci" + File.separator + "Dockerfile";
                String imageName = null;
                String imageValue = null;
                File dockerFile = new File(dockerFilePath);
                logger.info(dockerFilePath);
                if(dockerFile != null) {
                    try (BufferedReader br = new BufferedReader(new FileReader(dockerFile));){
                        String line = null;
                        while((line = br.readLine()) != null && line.startsWith("FROM")) {
                            if (line.contains("\r\n")) {
                                line = line.replace("\r\n", "\n");
                            }
                            String[] strs = line.split("/");
                            String imageNameAndValue = strs[strs.length - 1].trim();
                            String[] imageNameAndValues = imageNameAndValue.split(":");
                            imageName = imageNameAndValues[0];
                            imageValue = imageNameAndValues[1];
                            logger.info(imageName+"----"+imageValue);
                            break;
                        }
                    } catch (IOException e) {
                        logger.error("Dockerfile文件读取失败", e);
                    }

                } else {
                    logger.error("Dockerfile文件不存在");
                }
                if(imageName != null) {
                    paramMap.put(Constant.GITLAB_ID, projectId);
                    paramMap.put(Constant.IMAGE, imageName);
                    String imageVersion = componentService.queryBaseImageVersion(paramMap);
                    if (StringUtils.isNotEmpty(imageVersion) && !imageVersion.equals(imageValue)) {
                        // 如果获取到的版本号和文件中的版本号不一致时，才对文件中的版本信息进行替换
                        CommonUtils.replaceFirstStrInFile(dockerFile,imageValue, imageVersion);
                    }
                }
                //修改应用的编码，setEnv.sh的-Dfile.encoding=GBK修改
                //若编码不为null的情况，不需要修改
                if (null != fileEncoding) {
                    //获取setEnv.sh文件
                    String setEnvFilePath = localTempProjectPath + File.separator + "ci" + File.separator + "setEnv.sh";
                    File setEnvFile = new File(setEnvFilePath);
                    logger.info("setEnv文件路径:" + setEnvFilePath);
                    if (null != setEnvFile) {
                        try (BufferedReader br = new BufferedReader(new FileReader(setEnvFile));) {
                            String line = null;
                            String replaceLine = null;
                            //export JAVA_OPTIONS="-Dfile.encoding=GBK"
                            while ((line = br.readLine()) != null) {
                                logger.info("fileEncoding:" + line);
                                if (line.contains("-Dfile.encoding")) {
                                    logger.info("fileEncoding JAVA_OPTIONS:" + line);
                                    replaceLine = handleEncodingStr(line, fileEncoding);
                                    break;
                                }
                            }
                            if (null != replaceLine){
                                logger.info("-Dfile.encoding=GBK替换后为" + replaceLine);
                                logger.info("-Dfile.encoding=GBK替换前为" + line);
                                CommonUtils.replaceFirstStrInFile(setEnvFile, line, replaceLine);
                            } else {
                                logger.error("修改setEnv.sh文件失败");
                            }
                        } catch (IOException e) {
                            logger.error("setEnv.sh文件读取失败", e);
                        }
                    }
                }

                git1 = GitUtils.getGit(localTempProjectPath);
                GitUtils.gitPushFromGitlab(git1, "添加持续集成文件", name, password);//将本地临时项目推送到远程
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"添加持续集成模板文件失败！"});
            } finally {
                if (git != null) {
                    git.close();
                }
                if (git1 != null) {
                    git1.close();
                }
                CommonUtils.deleteFile(new File(localTempProjectPath));//清除本地项目临时仓库
                gitlabUserService.changeFdevRole(projectId, Dict.REPORTER);//回收权限，重新设置为Reporter
            }
            return true;
        }
    }

    /**
     * 处理setEnv.sh的export JAVA_OPTIONS="-Dfile.encoding="的line
     *
     * @param line
     * @param paramFileEncoding 前端传来的编码
     * @return
     */
    private String handleEncodingStr(String line, String paramFileEncoding) {
        if (line.contains("\r\n")) {
            line = line.replace("\r\n", "\n");
        }
        String[] splitLine = line.split("\"");
        //-Dfile.encoding=GBK
        String encoding = splitLine[splitLine.length - 1].trim();
        String[] encodingSplit = encoding.split("=");
        String fileEncoding = encodingSplit[encodingSplit.length - 1].trim();
        line = line.replace(fileEncoding, paramFileEncoding);
        logger.info("替换后的encoding line: " + line);
        return line;
    }

    public List createFile(String projectId, String token, String dirPath, String templatePath, Map contentMap) throws Exception {

        List listResult = new ArrayList<>();
        List listFiles = this.queryFileNameByPath(dirPath, token);
        String file_path = null;
        String fileName = null;
        String replacePath = null;
        String createPath = null;
        for (int i = 0; i < listFiles.size(); i++) {
            MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
            Map mapFiles = (Map) listFiles.get(i);
            if (!Dict.TREE.equals(mapFiles.get(Dict.TYPE))) {
                try {
                    file_path = (String) mapFiles.get(Dict.PATH);
                    replacePath = file_path.replace("/", "%2F");
                    replacePath = replacePath.replace(".", "%2E");
                    String deleteUrl = CommonUtils.projectUrl(url) + "/" + projectId + "/repository/files/" + replacePath;
                    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(deleteUrl)
                            .queryParam(Dict.BRANCH, Dict.MASTER).queryParam(Dict.COMMIT_MESSAGE, Dict.DELETE_MODEL_FILE);
                    URI uri = builder.build(true).toUri();
                    header.add(Dict.PRIVATE_TOKEN, token);
                    fileName = (String) mapFiles.get(Dict.NAME);
                    createPath = replacePath.split(templatePath + "%2F")[1];
                    gitlabTransport.submitDelete(uri, header);
                    this.quickCreatFile(projectId, token, fileName, createPath, contentMap);
                } catch (Exception e) {
                    this.quickCreatFile(projectId, token, fileName, createPath, contentMap); // 当新建项目中的文件不存在时直接创建该文件
                }
            }
        }

        return listResult;
    }

    public Map getProjectDeatilByProjectId(String token, String projectId) throws Exception {
        String pro_url = CommonUtils.projectUrl(url);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        String requestPath = pro_url + "/" + projectId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestPath);
        URI uri = builder.build(true).toUri();
        header.add(Dict.PRIVATE_TOKEN, token);
        String content = (String) gitlabTransport.submitGet(uri, header);
        return (Map) JSONObject.parse(content);
    }
}
