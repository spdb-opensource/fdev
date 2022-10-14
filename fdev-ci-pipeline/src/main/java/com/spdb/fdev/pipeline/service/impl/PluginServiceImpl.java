package com.spdb.fdev.pipeline.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.IPipelineDao;
import com.spdb.fdev.pipeline.dao.IPluginDao;
import com.spdb.fdev.pipeline.dao.IYamlConfigDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.*;
import com.spdb.fdev.pipeline.transport.GitlabTransport;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONArray;
import okhttp3.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

@Service
@RefreshScope
public class PluginServiceImpl implements IPluginService {
    @Autowired
    @Lazy
    private IPluginDao pluginDao;

    @Autowired
    @Lazy
    private IPipelineDao pipelineDao;

    @Value("${fdev.app.domain}")
    private String url;

    @Value("${gitlab.url}")
    private String gitlab_url;

    @Value("${gitlab.token}")
    private String token;

    @Value("${mioBuket}")
    private String mioBuket;

    @Value("${git_back_Url}")
    private String gitBackUrl;

    @Value("${uploadfile.suffix.list:}")
    private List<String> fileSuffixList;

    @Value("${uploadfile.name.list:}")
    private List<String> fileNameList;

    @Autowired
    private GitlabTransport gitlabTransport;

    @Autowired
    IFileService fileService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private RestTransport restTransport;//

    @Autowired
    private IUserService userService;

    @Autowired
    private IPipelineExeService iPipelineExeService;

    @Autowired
    private IJobExeService jobExeService;

    @Autowired
    private IYamlConfigDao yamlConfigDao;

    @Autowired
    private IPipelineService pipelineService;

    @Value("${jfrog.username}")
    private String username;

    @Value("${jfrog.password}")
    private String password;

    private Logger logger = LoggerFactory.getLogger(Plugin.class);

    private Properties pluginNameMapping = new Properties();

    @Override
    public List queryPlugin(List<String> key, Map request) throws Exception {
        User user = userService.getUserFromRedis();
        Map result = pluginDao.queryPlugin(key, request, user);
        List resultList = new ArrayList();
        //查询插件的分类，获取所有的分类，并创建不同的map来装
        Category queryParam = new Category();
        //查询所有的plugin分类
        queryParam.setCategoryLevel(Dict.PLUGIN);
        List<Category> categories = this.categoryService.getCategory(queryParam);
        List categoryNames = new ArrayList();
        categories.forEach(e -> {
            categoryNames.add(e.getCategoryName());
        });
        Map indexMap = new HashMap();
        for (int i = 0; i < categoryNames.size(); i++) {
            String categoryName = (String) categoryNames.get(i);
            Map resultMap = new HashMap();
            Map category = new HashMap();
            category.put(Dict.CATEGORYNAME, categoryName);
            category.put(Dict.CATEGORYID, categories.get(i).getCategoryId());
            resultMap.put(Dict.CATEGORY, category);
            resultList.add(resultMap);
            indexMap.put(categoryName, i);
        }


        if (!CommonUtils.isNullOrEmpty(result)) {
            List<Plugin> pluginList = (List<Plugin>) result.get(Dict.LIST);
            if (!CommonUtils.isNullOrEmpty(pluginList)) {
                for (Plugin plugin : pluginList) {
                    Category category = plugin.getCategory();

                    Integer index;
                    if (CommonUtils.isNullOrEmpty(category)) {
                        //为空，归到其他分类
                        index = (Integer) indexMap.get(Constants.CATEGORY_OTHER);

                    } else {
                        String categoryName = (String) plugin.getCategory().getCategoryName();
                        if (CommonUtils.isNullOrEmpty(categoryName)) {
                            //为空，归到其他分类
                            index = (Integer) indexMap.get(Constants.CATEGORY_OTHER);
                        } else {
                            index = (Integer) indexMap.get(categoryName);
                            if (CommonUtils.isNullOrEmpty(index))
                                throw new FdevException("存在有plugin的分类不在分类表中!");
                        }
                    }
                    Map pluginMap = (Map) resultList.get(index);
                    List resultPluginList = (List) pluginMap.get(Dict.PLUGINLIST);
                    if (CommonUtils.isNullOrEmpty(resultPluginList))
                        resultPluginList = new ArrayList();
                    Map pluginMapInfo = CommonUtils.beanToMap(plugin);
                    //查询插件的综合评分,是查所有用户对该插件评分的平均分
                    Double average = pluginDao.queryPluginEvaluateByNameId((String) pluginMapInfo.get(Dict.NAMEID));
                    pluginMapInfo.put(Dict.AVERAGE, average);
                    pluginMapInfo.remove(Dict.EXECUTION);
                    pluginMapInfo.remove(Dict.PARAMS);
                    pluginMapInfo.remove(Dict.ENTITYTEMPLATELIST);
                    pluginMapInfo.remove(Dict.SCRIPT);
                    pluginMapInfo.remove(Dict.STATUS);
                    pluginMapInfo.remove(Dict.CATEGORY);
                    pluginMapInfo.remove(Dict.ARTIFACTS);
                    pluginMapInfo.remove(Dict.OUTPUT);
                    resultPluginList.add(pluginMapInfo);
                    pluginMap.put(Dict.PLUGINLIST, resultPluginList);
                }
            }
        }

        ListIterator listIterator = resultList.listIterator();
        while (listIterator.hasNext()) {
            Object item = listIterator.next();
            Map itemMap = (Map) item;
            List pluginList = (List) itemMap.get(Dict.PLUGINLIST);
            if (CommonUtils.isNullOrEmpty(pluginList) || pluginList.size() == 0) {
                listIterator.remove();
            }
        }
        return resultList;
    }

    @Override
    public void savePlugin(MultipartFile script, String pluginName, String pluginDesc, Map category, Boolean isPublic, List entityTemplateList, List params, Map artifacts, Map categoryMap) throws Exception {

        if (CommonUtils.isNullOrEmpty(categoryMap)) {
            Category queryParam = new Category();
            queryParam.setCategoryName("自定义");
            queryParam.setCategoryLevel(Dict.PLUGIN);
            List<Category> categoryData = this.categoryService.getCategory(queryParam);
            categoryMap = CommonUtils.beanToMap(categoryData.get(0));
            categoryMap.remove(Dict._ID);
            categoryMap.remove(Dict.CATEGORYLEVEL);
            categoryMap.remove(Dict.PARENTID);
        }
        Plugin plugin = this.getPlugin(script, pluginName, pluginDesc, isPublic, entityTemplateList, params, true, categoryMap);
        plugin.setNameId(plugin.getPluginCode());
        plugin.setVersion(Constants.DEFAULT_VERSION);
        plugin.setArtifacts(artifacts);
        pluginDao.savePlugin(plugin);
    }

    @Override
    public void editPlugin(String nameId, String pluginName, String pluginDesc, MultipartFile script, String releaseLog, String entityTemplateList, String params, Boolean isPublic, String version, String artifacts, String category, Boolean scriptUpdateFlag) throws Exception {
        User user = userService.getUserFromRedis();
        // 1、修改当前插件状态为失效，带生效的参数查询
        List<Plugin> pluginList = pluginDao.queryPluginDetailByNameId(nameId);
        Plugin oldPlugin = pluginList.get(0);
//        String oldVersion = oldPlugin.getVersion();
        //将老版本更新为关闭status->0
        pluginDao.setStatusClose(oldPlugin.getPluginCode(), user);
//        String oldChangeVersion = Constants.UP_CHANGE_VERSION;
//        for(Plugin plugin:pluginList){
//            String status = plugin.getStatus();
//            if(Constants.STATUS_OPEN.equals(status)){//1-有效
//                oldChangeVersion = plugin.getChangeVersion();
//                pluginDao.setStatusClose(plugin.getPluginCode(), user);
//            }
//        }
        List<Map> paramsMap = com.alibaba.fastjson.JSONArray.parseArray(params, Map.class);
        Map categoryMap = JSONObject.parseObject(category, Map.class);
        Map artifactsMap = JSONObject.parseObject(artifacts, Map.class);
        List<Map> entityTemplateListMap = com.alibaba.fastjson.JSONArray.parseArray(entityTemplateList, Map.class);
        // 2、新增一条插件数据
        Plugin plugin = this.getPlugin(script, pluginName, pluginDesc, isPublic, entityTemplateListMap, paramsMap, scriptUpdateFlag, categoryMap);
        plugin.setArtifacts(artifactsMap);
        plugin.setNameId(nameId);
        plugin.setReleaseLog(releaseLog);
        plugin.setVersion(version);//版本号用户输入
        String updateTimes = plugin.getUpdateTimes();
        plugin.setUpdateTimes(String.valueOf((Integer.valueOf(updateTimes) + 1)));
        //变更号自增1
//        float changeVersion = Float.parseFloat(oldChangeVersion);
//        DecimalFormat versionFormat = new DecimalFormat(Constants.VERSION_FORMAT);
//        String newChangeVersion = versionFormat.format(changeVersion + 1f);
//        plugin.setChangeVersion(newChangeVersion);
//        plugin.setType(Constants.TYPE_1);
        pluginDao.savePlugin(plugin);
    }

    @Override
    public void delPlugin(String pluginCode) throws Exception {
        //并非真正的删除，把插件状态置为失效
        User user = userService.getUserFromRedis();
        pluginDao.setStatusClose(pluginCode, user);
    }

    @Override
    public List<Map<String, String>> queryPluginHistory(String nameId) throws Exception {
        List<Plugin> pluginList = pluginDao.queryPluginHistory(nameId);
        List<Map<String, String>> releaseLogList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(pluginList)) {
            for (Plugin plugin : pluginList) {
                Map<String, String> releaseMap = new HashMap<>();
                String releselog = plugin.getReleaseLog();
                if (CommonUtils.isNullOrEmpty(releselog)) {//过滤掉新增插件时变更记录为空的那条数据
                    continue;
                }
                releaseMap.put(Dict.CREATETIME, plugin.getCreateTime());
                releaseMap.put(Dict.RELEASELOG, releselog);
                releaseMap.put(Dict.VERSION, plugin.getVersion());
                releaseLogList.add(releaseMap);
            }
        }
        return releaseLogList;
    }

    @Override
    public Plugin queryPluginDetail(String pluginCode) throws Exception {
        return pluginDao.queryPluginDetail(pluginCode);
    }

    //分别上传miO和gitlab
    @Override
    public Map<String, String> uploadFile(MultipartFile file, String filename) throws Exception {
        Map<String, String> resMap = new HashMap<>();
        //第一步：上传mio
        User user = userService.getUserFromRedis();
        String pathMinio = Constants.SCRIPT_PREFIX + user.getUser_name_en() + "/" + filename + ".sh";
        resMap.put(Dict.PACKAGE_PATH, pathMinio);
        fileService.minioUploadFiles(mioBuket, pathMinio, file);
        //第二步：上传到gitLab
        //获取文件内容作为上传git方法的入参
//        InputStream is = null;
//        filename.replace("/", "%2F");
//        String gitUrl = gitlab_url + "api/v4/projects/" + projectId + "/repository/files/plugin%2F" + user.getUser_name_en() +"%2F" + filename + "%2Esh";
//        String file_content = "";
//        try {
//            if(file != null ){
//                is = file.getInputStream();
//                //如果文件内容为空则抛错
//                if(file.getSize()==0){
//                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST,  new String[] { "上传的脚本内容为空！" });
//                }
//            }
//            byte[] buff = new byte[1024];
//            int len;
//            StringBuilder sb = new StringBuilder();
//            while (-1 != (len = is.read(buff))) {
//                file_content = new String(buff, 0, len);
//                // 若换行符为\r\n，则剔除\r
//                file_content.replaceAll("\r", "");
//                sb.append(file_content);
//            }
//            logger.info("读取文件内容：{}", sb.toString());
//
//            Map<String,String> res = this.uploadFiletoGit(gitUrl, "master", sb.toString(), "脚本信息", token);
//            if(!CommonUtils.isNullOrEmpty(res)){
//                String file_path = (String) res.get(Dict.FILE_PATH);
//                String branch = (String) res.get(Dict.BRANCH);
//                resMap.put(Dict.BACK_PATH, gitBackUrl + branch + "/" + file_path);
//            }
//        } catch (Exception e) {
//            logger.error("上传gitlab时，读取脚本内容发生错误！" ,e.getMessage());
//            throw e;
//        }finally {
//            is.close();
//        }
        return resMap;
    }

    private Map uploadFiletoGit(String url, String ref, String content, String commit_message, String token) throws Exception {

        Map<String, String> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN, token);
        param.put(Dict.BRANCH, ref);
        param.put(Dict.CONTENT, content);
        param.put(Dict.COMMIT_MESSAGE, commit_message);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
//        this.gitlabTransport.submitPut(uri,token, param);//上传同名文件第二次上传用的方法
        Map res = JSONObject.parseObject((String) this.gitlabTransport.submitPost(uri, param));
        return res;
    }

    private Plugin getPlugin(MultipartFile file, String pluginName, String pluginDesc, Boolean isPublic, List entityTemplateList, List params, Boolean scriptUpdateFlag, Map categoryMap) throws Exception {
        User user = userService.getUserFromRedis();
        String categoryId = (String) categoryMap.get(Dict.CATEGORYID);
        Category queryParams = new Category();
        queryParams.setCategoryId(categoryId);
        List<Category> categories = this.categoryService.getCategory(queryParams);
        if (CommonUtils.isNullOrEmpty(categories)) {
            logger.error(ErrorConstants.PLUGIN_PARAMS_ERROR, new String[]{"categoryId为无效数据"});
            throw new FdevException(ErrorConstants.PLUGIN_PARAMS_ERROR, new String[]{"categoryId为无效数据"});
        }
//        Map saveCategoryMap = new HashMap();
//        saveCategoryMap.put(Dict.CATEGORY_ID, categories.get(0).getCategoryId());
//        saveCategoryMap.put(Dict.CATEGORY_NAME, );
        Category saveCategory = new Category();
        saveCategory.setCategoryId(categories.get(0).getCategoryId());
        saveCategory.setCategoryName(categories.get(0).getCategoryName());
        //插件
        Plugin plugin = new Plugin();
        String id = new ObjectId().toString();
        String scriptName = id;
        plugin.setPluginCode(id);
        plugin.setPluginName(pluginName);
        plugin.setPluginDesc(pluginDesc);
        plugin.setCategory(saveCategory);
        //接收到前端传过来的文件，将脚本分别上传至mio和gitlab
        if (scriptUpdateFlag) {
            //当变更的时候才会去上传minio和gitlab，以及更新execution和script
            Map<String, String> map = this.uploadFile(file, scriptName);
            plugin.setScript(map);
            Map execution = new HashMap();
            execution.put(Dict.LANGUAGE, Constants.LANGUAGE);
            //存放mio地址
            execution.put(Dict.PACKAGE_PATH, "system/goShell/1.0.0/goShell");
            //备份地址gitlab地址
//            String backPath = (String) map.get(Dict.BACK_PATH);
//            String[] pathSplit = backPath.split(user.getUser_name_en());
//            String exeBackPath = pathSplit[0] + "system/goShell";
            execution.put(Dict.BACK_PATH, "");
            List<String> demands = new ArrayList<>();
            String defineDemands = Dict.GOSHELL;
            demands.add("chmod +x " + defineDemands);
            execution.put(Dict.DEMANDS, demands);
            execution.put(Dict.TARGET, "./" + defineDemands);
            plugin.setExecution(execution);
        }
        List<Map<String, Object>> mapListJson = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(params)) {
            mapListJson = JSONArray.fromObject(params);
        } else {
            mapListJson = null;
        }
        //添加minio_object_name和back_path
//        Map<String, String> minioMap = new HashMap<>();
//        minioMap.put(Dict.NAME, "minio_object_name");
//        minioMap.put(Dict.TYPE, Dict.INPUT);
//        minioMap.put("placeholder", "例如：xxx.sh");
//        minioMap.put("label", "自定义脚本文件");
//        minioMap.put(Dict.VALUE, map.get(Dict.PACKAGE_PATH));
//
//        Map<String, String> backMap = new HashMap<>();
//        backMap.put(Dict.NAME, "back_path");
//        backMap.put(Dict.TYPE, Dict.INPUT);
//        backMap.put("placeholder", "例如：xxx.sh");
//        backMap.put("label", "自定义脚本文件备份目录");
//        backMap.put(Dict.VALUE, map.get(Dict.BACK_PATH));
//        mapListJson.add(minioMap);
//        mapListJson.add(backMap);
        plugin.setParams(mapListJson);
//        if(!CommonUtils.isNullOrEmpty(entityTemplateList)) {
//            //将字符串转换成list集合
//            entityTemplateList = JSONObject.parseArray(entityTemplateList);
//        }else{
//            entityTemplateList = null;
//        }
        //模版信息
        plugin.setEntityTemplateList(entityTemplateList);
        String date = CommonUtils.dateFormat(new Date(), CommonUtils.STANDARDDATEPATTERN);
        plugin.setCreateTime(date);
        //作者
        Author author = userService.getAuthor();
        plugin.setAuthor(author);
        plugin.setOpen(isPublic);
        plugin.setStatus(Constants.STATUS_OPEN);//插件状态：1-有效
        plugin.setUpdateTimes(Constants.ZERO);  //插件更新次数 0次
        //获取插件的内容
//        String fileContent = new String(file.getBytes());
//        //放进script，该字段的值不落库
//        Map script = plugin.getScript();
//        script.put("content", fileContent);
        return plugin;
    }

    @Override
    public Map getPluginFullInfo(String pluginCode) throws Exception {
        Plugin plugin = this.pluginDao.queryPluginDetail(pluginCode);
        if (CommonUtils.isNullOrEmpty(plugin)) {
            logger.error("************************getPluginFullInfo时没有找到这个插件,该插件的pluginCode为" + pluginCode);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        Map originPluginMap = CommonUtils.beanToMap(plugin);
        String nameEn = plugin.getAuthor().getNameEn();
        //根据作者的用户判断是否是自定义插件还是系统插件
        if (!CommonUtils.isNullOrEmpty(plugin.getScript())) {
            //获取minio路径，得到minio上的脚本内容
            String scriptCmd = "";
            Map script = plugin.getScript();
            Map<String, Object> execution = plugin.getExecution();
            String language = (String) execution.get(Dict.LANGUAGE);
            String mioPath = (String) script.get(Dict.MINIO_OBJECT_NAME);
            if (!CommonUtils.isNullOrEmpty(mioPath)) {
                scriptCmd = fileService.downloadDocumentFile(mioBuket, mioPath);
            }
            if (CommonUtils.isNullOrEmpty(scriptCmd))
                //logger.error("从minio获取得到内容为空 cmd:" + scriptCmd);
                logger.error("************Get info from minio is empty; cmd:" + scriptCmd);
            //去除script，不对外暴露
//            originPluginMap.remove(Dict.SCRIPT);
            originPluginMap.put(Dict.SCRIPTCMD, scriptCmd);
        }
        originPluginMap.remove(Dict._ID);
        return originPluginMap;
    }


    @Override
    public Map queryMarkDowm(String pluginCode) throws Exception {
        Plugin plugin = null;
        Map map = new HashMap();
        plugin = this.pluginDao.queryPluginDetail(pluginCode);
        if (CommonUtils.isNullOrEmpty(plugin)) {
            logger.error("************can't not find the plugin");
//            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        // markdown文档的地址
        String markDownUrl = plugin.getMarkDownUrl();
        //如果不存在这个markdown的话就直接不查数据了
        String markdown = "";
        if (!CommonUtils.isNullOrEmpty(markDownUrl)) {
//            logger.error("************the plugin don't have markdown information");
//            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            markdown = fileService.downloadDocumentFile(mioBuket, markDownUrl);
        }

        map.put(Dict.MARKDOWN, markdown);
        return map;
    }

    @Override
    public Map queryEntityTemplateByContent(String str) throws Exception {
        List<String> entityTemplatelList = CommonUtils.getEntityTempalteNames(str);
        List list = new ArrayList();
        for (String queryName : entityTemplatelList) {
            Map<String, Object> send_map = new HashMap();
            send_map.put(Dict.NAMEEN, queryName);
            send_map.put(Dict.REST_CODE, "queryModelTemplateByNameEn");
            Map result = (Map) restTransport.submit(send_map);
            if (!CommonUtils.isNullOrEmpty(result)) {
                Map copyMap = new LinkedHashMap();
                copyMap.put(Dict.ID, result.get(Dict.ID));
                copyMap.put(Dict.NAMEEN, result.get(Dict.NAMEEN));
                copyMap.put(Dict.NAMECN, result.get(Dict.NAMECN));
                list.add(copyMap);
            }
        }
        Map return_map = new HashMap();
        return_map.put(Dict.ENTITYTEMPLATELIST, list);
        return return_map;
    }

    @Override
    public void statisticsPluginUseCount() throws Exception {
        List<PluginUseCount> pluginUseCountList = pluginDao.statisticsPluginUseCount(null);
        if (!CommonUtils.isNullOrEmpty(pluginUseCountList)) {
            Map<String, BindProject> bindProjectMap = new HashMap<>();  //用于存放查询的bindProject，减少访问数据库次数
            for (PluginUseCount pluginUseCount : pluginUseCountList) {
                String projectId = pluginUseCount.getBindProject().getProjectId();
                BindProject bindProject = bindProjectMap.get(projectId);
                if (CommonUtils.isNullOrEmpty(bindProject)) {
                    bindProject = pipelineDao.queryOneByProjectId(projectId).getBindProject();
                    bindProjectMap.put(projectId, bindProject);
                }
                pluginUseCount.setBindProject(bindProject);
                pluginDao.savePluginUseCount(pluginUseCount);
            }
        }
    }

    @Override
    public void upsertPluginEvaluate(User user, PluginEvaluate pluginEvaluate) {
        ObjectId id = new ObjectId();
        pluginEvaluate.setPluginEvaluateId(id.toString());
        //把用户的部分信息给封装进去
        Author author = new Author();
        author.setId(user.getId());
        author.setNameEn(user.getUser_name_en());
        author.setNameCn(user.getUser_name_cn());
        pluginEvaluate.setAuthor(author);
        //评分保留一位小数
        double average = Double.parseDouble(String.format("%.1f", (pluginEvaluate.getOperationDifficulty() + pluginEvaluate.getRobustness() + pluginEvaluate.getLogClarity()) / 3.0));
        pluginEvaluate.setAverage(average);
        pluginDao.upsertPluginEvaluate(pluginEvaluate);
    }

    @Override
    public PluginEvaluate queryPluginEvaluate(User user, String nameId) {
        String userId = user.getId();
        return pluginDao.queryPluginEvaluate(userId, nameId);
    }


    @Override
    public YamlConfig getYamlConfigById(String configId) {
        return yamlConfigDao.queryById(configId);
    }

    @Override
    public String addYamlConfig(YamlConfig addEntity){
        if(Dict.PRIVATE.equals(addEntity.getType())){
            //模板id不能为空
            if(CommonUtils.isNullOrEmpty(addEntity.getConfigTemplateId())){
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.CONFIGTEMPLATEID});
            }
            //实例ID不能与模板id相同
            if (addEntity.getConfigTemplateId().equals(addEntity.getConfigId())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.CONFIGID,"不能与配置模板Id相同"});
            }

            String orgConfigId = addEntity.getConfigId();
            //如果上送了configId，是修改，否则是新增
            if (!CommonUtils.isNullOrEmpty(orgConfigId)) {
                YamlConfig orgEntity = getYamlConfigById(orgConfigId);
                    logger.info("******YamlConfig compare,"
                            +" update configId:" + orgConfigId
                            +" old::"+ orgEntity.paramsInfo()
                            +" new::"+ addEntity.paramsInfo() );

                if (orgEntity != null && orgEntity.paramsInfo().equals(addEntity.paramsInfo())
                                        && orgEntity.getEnv().equals(addEntity.getEnv())) {
                    //参数相同就不修改了
                    logger.info("******config not changed, configId:" + orgConfigId);
                    return orgEntity.getConfigId();
                }
                String newConfigId = yamlConfigDao.add(addEntity);
                //如果修改，要把老的配置状态改为0-不使用
                yamlConfigDao.updateStatusClose(orgConfigId);
                return newConfigId;
            } else {
                return yamlConfigDao.add(addEntity);
            }
        }
        logger.info("******YamlConfig type is public.");
        return null;
    }

    /**
     * 在stage列表中的job的step的插件参数params中，如果有fileEdit类型的，如果value不是模板id,将yamlconfig复制一条记录。
     * @param stages
     */
    @Override
    public void copyYamlConfigInStages(List<Stage> stages) {
        //复制时，如果步骤参数中有fileEdit，将复制一个新的yamlConfig，重新复制value
        for(Stage stage: stages){
            for(Job job : stage.getJobs()) {
                for(Step step : job.getSteps()){
                    PluginInfo pluginInfo = step.getPluginInfo();
                    List<Map<String, Object>> params  = pluginInfo.getParams();
                    if(!CommonUtils.isNullOrEmpty(params)){
                        for(Map param: params){
                            String type = (String) param.get(Dict.TYPE);
                            if(Dict.FILEEDIT.equals(type)){
                                String value = (String) param.get(Dict.VALUE);
                                if(!CommonUtils.isNullOrEmpty(value)){
                                    //不是模板就新建一条，是模板就不处理
                                    YamlConfig yamlConfig = getYamlConfigById(value);
                                    if (yamlConfig != null && Dict.PRIVATE.equals(yamlConfig.getType())) {
                                        yamlConfig.setConfigId(null);
                                        String newConfigId = addYamlConfig(yamlConfig);
                                        param.put(Dict.VALUE, newConfigId);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * 在stage列表中的job的step的插件参数params中，如果有fileEdit类型的，如果value不是模板id,将yamlconfig状态-未使用。
     * @param stages
     */
    @Override
    public void closeYamlConfigInStages(List<Stage> stages) {
        //复制时，如果步骤参数中有fileEdit，将复制一个新的yamlConfig，重新复制value
        for(Stage stage: stages){
            for(Job job : stage.getJobs()) {
                for(Step step : job.getSteps()){
                    PluginInfo pluginInfo = step.getPluginInfo();
                    List<Map<String, Object>> params  = pluginInfo.getParams();
                    if(!CommonUtils.isNullOrEmpty(params)){
                        for(Map param: params){
                            String type = (String) param.get(Dict.TYPE);
                            if(Dict.FILEEDIT.equals(type)){
                                String value = (String) param.get(Dict.VALUE);
                                if(!CommonUtils.isNullOrEmpty(value)){
                                    //不是模板就新建一条，是模板就不处理
                                    YamlConfig yamlConfig = getYamlConfigById(value);
                                    if(yamlConfig != null && Dict.PRIVATE.equals(yamlConfig.getType())){
                                        yamlConfigDao.updateStatusClose(value);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    /**
     * 校验上传文件的后缀，是否合法
     *
     * @param fileName
     * @return
     */
    @Override
    public boolean checkUploadFileSuffix(String fileName) throws Exception {
        if (CommonUtils.isNullOrEmpty(fileName)) {
            logger.error("**************The uploadFileName is empty");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"", "文件名不能为空！"});
        }
        logger.info("**************The uploadFile is :" + fileName);
        //校验文件格式：xml，yaml
        if (fileName.indexOf("/") >= 0) {
            logger.error("**************The uploadFileName error!");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"", "文件名不能包含路径！"});
        }

        boolean suffixCheck = false;
        boolean nameCheck = false;
        int index = fileName.indexOf(".");
        if (index >= 0) {
            String suffix = fileName.substring(index);
            suffixCheck = fileSuffixList.contains(suffix);
        } else {
            nameCheck = fileNameList.contains(fileName);
        }

        if (!suffixCheck && !nameCheck) {
            logger.error("**************The uploadFile is illegal ");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"", "文件名格式不合法！"});
        }

        return true;
    }


    /**
     * 下载制品
     *
     * @param requestParam
     * @return
     */
    @Override
    public Object downloadArt(Map requestParam) {
//        requestParam.get()
//        return this.pipelineService.downLoadArtifacts();
        return null;
    }

    /**
     * 上传制品
     *
     * @param file
     * @param path
     */
    @Override
    public void uploadArt(MultipartFile file, String path) throws Exception {
        String ip = "xxx:80";
        String passwordStr = password.replace("@", "%40");
        String userUrl = "http://" + username + ":" + passwordStr;
        if (path.indexOf("/") != 0) {
            path = "/" + path;
        }
        String url = userUrl + "@" + ip + "/artifactory" + path;
        URI uri = new URI(url);
        gitlabTransport.submitPut(uri, file.getBytes(), false);
    }

    @Override
    public void uploadMinio(MultipartFile file, String bucket, String object) throws Exception {
        fileService.minioUploadFiles(bucket, object, file);
    }

    @Override
    public Object downloadMinio(String bucket, String object, HttpServletResponse response) throws Exception {
        OutputStream out = null;
        InputStream in = null;
        try {
            in = fileService.downloadFileStream(bucket, object);
            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            String filename = object.substring(object.lastIndexOf("/") + 1);
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            out = response.getOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024 * 9];
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            out.flush();
        } catch (
                Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"下载失败"});
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
        return null;
    }
}
