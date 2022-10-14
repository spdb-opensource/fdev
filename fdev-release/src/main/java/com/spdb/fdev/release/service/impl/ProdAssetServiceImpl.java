package com.spdb.fdev.release.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.HttpRequestUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.release.dao.IAssetCatalogDao;
import com.spdb.fdev.release.dao.IBatchTaskDao;
import com.spdb.fdev.release.dao.IEsfRegistrationDao;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.dao.IProdAssetDao;
import com.spdb.fdev.release.dao.IProdRecordDao;
import com.spdb.fdev.release.entity.AssetCatalog;
import com.spdb.fdev.release.entity.AwsConfigure;
import com.spdb.fdev.release.entity.BatchTaskInfo;
import com.spdb.fdev.release.entity.EsfRegistration;
import com.spdb.fdev.release.entity.OptionalCatalog;
import com.spdb.fdev.release.entity.ProdApplication;
import com.spdb.fdev.release.entity.ProdAsset;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseApplication;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.service.IAppDatService;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IFileService;
import com.spdb.fdev.release.service.IOptionalCatalogService;
import com.spdb.fdev.release.service.IProdAppScaleService;
import com.spdb.fdev.release.service.IProdApplicationService;
import com.spdb.fdev.release.service.IProdAssetService;
import com.spdb.fdev.release.service.IProdRecordService;
import com.spdb.fdev.release.service.IReleaseCatalogService;
import com.spdb.fdev.release.service.IReleaseNodeService;
import com.spdb.fdev.release.service.IRoleService;
import com.spdb.fdev.release.service.IUserService;

@Service
@RefreshScope
public class ProdAssetServiceImpl implements IProdAssetService {

    private static Logger logger = LoggerFactory.getLogger(ProdAssetServiceImpl.class);

    @Resource
    private IProdAssetDao prodAssetDao;
    @Autowired
    private IFileService fileService;
    @Autowired
    private IProdRecordService prodRecordService;
    @Autowired
    private IReleaseCatalogService releaseCatalogService;
    @Autowired
    private IProdRecordDao prodRecordDao;
    @Autowired
    private IProdApplicationDao prodApplicationDao;
    @Autowired
    private IAppService appService;
    @Autowired
    private IReleaseCatalogService catalogService;
    @Autowired
    private IAssetCatalogDao assetCatalogDao;
    @Autowired
    private IUserService userService;
    @Value("${prod.record.minio.url}")
    private String recordAssetsUrl;// /testassets-sit/
    @Autowired
    IReleaseNodeService releaseNodeService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IAppDatService appDatService;
    @Autowired
    private IEsfRegistrationDao esfRegistrationDao;
    @Autowired
    private EsfRegistrationServiceImpl esfRegistrationService;
    @Autowired
    private IProdApplicationService prodApplicationService;
    @Autowired
    private IOptionalCatalogService optionalCatalogService;
    @Autowired
    private IProdAppScaleService prodAppScaleService;
    @Autowired
    private IBatchTaskDao batchTaskDao;
    
    @Value("${nacos.rel.url}")
    private String nacosRelUrl;
    @Value("${nacos.nacosUserName}")
    private String nacosUserName;
    @Value("${nacos.nacosPassword}")
    private String nacosPassword;
    @Value("${nacos.nacosGroup}")
    private String nacosGroup;
    @Value("${repo.down.path}")
    private String downRepoPath;
   

    private static final String ORDER_TXT_NAME = "order.txt (介质准备后自动生成)";


    @Override
    public Map create(ProdAsset prodAsset, MultipartFile[] files, String child_catalog, User user, String aws_type) throws Exception {
        String prod_id = prodAsset.getProd_id();
        //同一变更文件目录下的文件序号不能相同
        ProdAsset prodAssetSeq_no = prodAssetDao.queryAssetBySeq_no(prod_id, prodAsset.getAsset_catalog_name(), prodAsset.getSeq_no());
        if (!CommonUtils.isNullOrEmpty(prodAssetSeq_no)) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"文件序号已存在,请更换"});
        }
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        if (CommonUtils.isNullOrEmpty(prodRecord)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"变更记录不存在"});
        }
        // 根据介质目录名查询目录
        AssetCatalog assetCatalog = releaseCatalogService.queryAssetCatalogByName(prodRecord.getTemplate_id(), prodAsset.getAsset_catalog_name());
        //目录类型为1或2时,不能上传文件
        if ("1".equals(assetCatalog.getCatalog_type()) || "2".equals(assetCatalog.getCatalog_type())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前目录类型不能上传文件,请更换目录类型"});
        }
        prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
        // 配置文件路径+投产窗口名称 为全局唯一file路径
        String groupFullPath = recordAssetsUrl + prodRecord.getRelease_node_name();
        // 获取当前变更的变更版本version
        String version = prodRecord.getVersion();
        String projectFullPath = new StringBuilder(groupFullPath).append("/").append(version).toString();
        // 方式为上传文件
        prodAsset.setSource("1");
        String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
        prodAsset.setUpload_user(user.getId());
        prodAsset.setUpload_time(upload_time);

        String bucketName = "";
        String sid = "";
        String filename = "";
        String runtime_env = "";
        String path = "";
        Map prodAssetMap = new HashMap<>();
        for (MultipartFile file : files) {
            ProdAsset saveProdAsset;
            String assetCatalogName = prodAsset.getAsset_catalog_name();
            if (Dict.COMMONCONFIG.equals(assetCatalogName) || "dynamicconfig".equals(assetCatalogName) || "bastioncommonconfig".equals(assetCatalogName)) {
                String[] arr = file.getOriginalFilename().split("/");
                filename = arr[0];
                runtime_env = arr[1];
                prodAsset.setRuntime_env(runtime_env);
                if (CommonUtils.isNullOrEmpty(child_catalog)) {
                    path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName + Constants.SPLIT_STRING + runtime_env + Constants.SPLIT_STRING + filename;
                } else {
                    path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName + Constants.SPLIT_STRING + runtime_env + Constants.SPLIT_STRING + child_catalog + Constants.SPLIT_STRING + filename;
                }
            }else if("5".equals(assetCatalog.getCatalog_type())){ // 5-esfcommonconfig
                String[] arr = file.getOriginalFilename().split("/");
                filename = arr[0];
                runtime_env = arr[1];
                bucketName = arr[2];
                sid = arr[3];
                prodAsset.setRuntime_env(runtime_env);
                prodAsset.setBucket_name(bucketName);
                prodAsset.setSid(sid);
                // 拼一层目录结构　sid_20210925
                String date = prodRecord.getDate();
                date = date.replace("/","");
                String fileContent = sid + "_" + date;
                if (CommonUtils.isNullOrEmpty(child_catalog)) {
                    path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName + Constants.SPLIT_STRING + runtime_env + Constants.SPLIT_STRING + fileContent + Constants.SPLIT_STRING + filename;
                } else {
                    path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName + Constants.SPLIT_STRING + runtime_env + Constants.SPLIT_STRING + fileContent +Constants.SPLIT_STRING + child_catalog + Constants.SPLIT_STRING + filename;
                }
            }else if("7".equals(assetCatalog.getCatalog_type())){
                String[] arr = file.getOriginalFilename().split("/");
                filename = arr[0];
                runtime_env = arr[1];
                if("2".equals(aws_type)){ // 上传的压缩包，给压缩包重命名给个标识主要为了在脚本中获取后好区分
                    filename = filename.substring(0,filename.indexOf(".")) + "_folder" +  filename.substring(filename.indexOf("."));
                }
                prodAsset.setRuntime_env(runtime_env);
                bucketName = prodAsset.getBucket_name();
                String bucketPath = prodAsset.getBucket_path();
                path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName + Constants.SPLIT_STRING + runtime_env
                        + Constants.SPLIT_STRING + bucketName + Constants.SPLIT_STRING ;
                if(!CommonUtils.isNullOrEmpty(bucketPath)){
                    path += bucketPath + Constants.SPLIT_STRING;
                }
                if (CommonUtils.isNullOrEmpty(child_catalog)) {
                    path += filename;
                } else {
                    path += child_catalog + Constants.SPLIT_STRING + filename;
                }
            }else {
                filename = file.getOriginalFilename();
                if (CommonUtils.isNullOrEmpty(child_catalog)) {
                    path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName + Constants.SPLIT_STRING + filename;
                } else {
                    path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName + Constants.SPLIT_STRING + child_catalog + Constants.SPLIT_STRING + filename;
                }
            }
            //文件上传
            fileService.uploadFiles(path, file, filename, "fdev-release");
            prodAsset.setFileName(filename);
            prodAsset.setFile_giturl(path);
            saveProdAsset = prodAssetDao.save(prodAsset);
            ProdAsset queryAssetsOne = prodAssetDao.queryAssetsOne(saveProdAsset.getId());
            prodAssetMap.put(saveProdAsset.getId(), queryAssetsOne);
        }
        return prodAssetMap;
    }

    @Override
    public List<ProdAsset> queryAssetsList(String prod_id) throws Exception {
        return prodAssetDao.queryAssetsList(prod_id);
    }

    /**
     * 按目录及运行环境分类变更文件
     *
     * @param prod_id
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> querySortedAssets(String prod_id) throws Exception {
        List<ProdAsset> prodAssets = compareListBySeqNo(prodAssetDao.queryAssetsList(prod_id));
        Map<String, Object> envProdFiles = new HashMap<>();
        for (ProdAsset prodAsset : prodAssets) {
            String envName = prodAsset.getRuntime_env();
            String catalogName = prodAsset.getAsset_catalog_name();
            String fileName = prodAsset.getFileName();
            String child_catalog = prodAsset.getChild_catalog();
            if (!Util.isNullOrEmpty(envName)) {
                Map<String, List> envFileMap = (Map<String, List>) envProdFiles.get(catalogName);
                if (Util.isNullOrEmpty(envFileMap)) {
                    envFileMap = new HashMap<>();
                    envProdFiles.put(catalogName, envFileMap);
                }
                if (Util.isNullOrEmpty(envFileMap.get(envName))) {
                    envFileMap.put(envName, new ArrayList());
                }
                List envFileList = envFileMap.get(envName);
                envFileList.add(prodAsset);
                envProdFiles.put(catalogName, envFileMap);
                continue;
            } else if (catalogName.equals("config") && fileName.indexOf("repo.json") !=-1 && !Util.isNullOrEmpty(child_catalog)){
                List<Object> jsonFileList = (List<Object>) envProdFiles.get(catalogName);
                if (Util.isNullOrEmpty(jsonFileList)) {
                    jsonFileList = new ArrayList();
                }
                Map temp = new HashMap();
                List tempList = new ArrayList();
                tempList.add(prodAsset);
                temp.put(child_catalog, tempList);
                jsonFileList.add(temp);
                envProdFiles.put(catalogName, jsonFileList);
            }else {
                List normalFiles = (List) envProdFiles.get(catalogName);
                if (Util.isNullOrEmpty(normalFiles)) {
                    normalFiles = new ArrayList();
                    envProdFiles.put(catalogName, normalFiles);
                }
                normalFiles.add(prodAsset);
            }
        }
        return envProdFiles;
    }

    private static List<ProdAsset> compareListBySeqNo(List<ProdAsset> list) {
        Collections.sort(list, (o1, o2) -> {
            Integer seq_no1 = !CommonUtils.isNullOrEmpty(o1.getSeq_no()) ? Integer.valueOf(o1.getSeq_no()) : 0;
            Integer seq_no2 = !CommonUtils.isNullOrEmpty(o2.getSeq_no()) ? Integer.valueOf(o2.getSeq_no()) : 0;
            return seq_no1.compareTo(seq_no2);
        });
        return list;
    }

    @Override
    public List<ProdAsset> queryAssetsWithSeqno(String prod_id, String asset_catalog_name) throws Exception {
        return prodAssetDao.queryAssetsWithSeqno(prod_id, asset_catalog_name);
    }

    @Override
    public List queryAssets(String prod_id, String source_application) throws Exception {
        String template_id = "";
        if (!CommonUtils.isNullOrEmpty(source_application)) {
            try {
                ProdApplication prodApplication = prodApplicationDao.queryApplication(prod_id, source_application);
                template_id = prodApplication.getTemplate_id();
            } catch (Exception e) {
                return new ArrayList();
            }
        }

        if (CommonUtils.isNullOrEmpty(template_id)) {
            ProdRecord proRecord = prodRecordDao.queryDetail(prod_id);
            template_id = proRecord.getTemplate_id();
        }
        List<AssetCatalog> list = catalogService.query(template_id);
        if (CommonUtils.isNullOrEmpty(list)) {
            list = new ArrayList();
        }
        List result = new ArrayList<>();
        for (AssetCatalog assetCatalog : list) {
            Map catalogResult = new HashMap<>();
            catalogResult.putAll(CommonUtils.beanToMap(assetCatalog));
            List<ProdAsset> prodAssetList = prodAssetDao.queryAssets(prod_id, assetCatalog.getCatalog_name());
            for (ProdAsset prodAsset : prodAssetList) {
                Map userInfo = userService.queryUserById(prodAsset.getUpload_user());
                prodAsset.setUpload_username_cn(userInfo == null ? "" : (String) userInfo.get(Dict.USER_NAME_CN));
                if (!CommonUtils.isNullOrEmpty(prodAsset.getSource_application())) {
                    try {
                        Map<String, Object> queryAPP = appService.queryAPPbyid(prodAsset.getSource_application());
                        prodAsset.setSource_application_name(queryAPP == null ? "未知应用" : (String) queryAPP.get("name_zh"));
                    } catch (Exception e) {
                    }
                }
            }
            // esf介质目录是没有上传文件的目录，脚本里需要拿到该目录
            if (CommonUtils.isNullOrEmpty(prodAssetList) && (!"esf".equals(assetCatalog.getCatalog_name()) && !"8".equals(assetCatalog.getCatalog_type()))) {
                continue;
            }
            catalogResult.put(Dict.ASSETS, prodAssetList);
            result.add(catalogResult);
        }
        return result;
    }

    @Override
    public ProdAsset queryAssetsOne(String id) throws Exception {
        return prodAssetDao.queryAssetsOne(id);
    }

    @Override
    public void deleteAsset(ProdAsset prodAsset) throws Exception {
        //上传来源为1并且不是数据库文件时,删除minio上的文件
        // 准备介质不需要完全拷贝gitlab项目，所以minio文件并不需要同步删除，数据库不存在的文件，介质不会准备
//        if ("1".equals(prodAsset.getSource()) && CommonUtils.isNullOrEmpty(prodAsset.getSeq_no())) {
//            // 根据id查询文件file_giturl 删除minio上的文件
//            String file_giturl = prodAsset.getFile_giturl();
//            try {
//                fileService.deleteFiles(file_giturl, "fdev-release");
//            } catch (Exception e) {
//                logger.info("{}文件删除异常", file_giturl);
//                throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
//            }
//            logger.info("删除minio地址 :{}成功", file_giturl);
//        }
        prodAssetDao.deleteAsset(prodAsset.getId());
    }

    @Override
    public void setCommitId(ProdAsset prodAsset) throws Exception {
        prodAssetDao.setCommitId(prodAsset);

    }

    @Override
    public ProdAsset updateAssetSeqNo(ProdAsset prodAsset) throws Exception {
        return prodAssetDao.updateAssetSeqNo(prodAsset.getId(), prodAsset.getSeq_no());
    }

    @Override
    public ProdAsset addGitlabAsset(ProdAsset prodAsset) throws Exception {
        return prodAssetDao.save(prodAsset);
    }

    @Override
    public ProdAsset queryAssetByName(String prod_id, String asset_catalog_name, String file_name, String runtime_env) throws Exception {
        return prodAssetDao.queryAssetByName(prod_id, asset_catalog_name, file_name, runtime_env);
    }


    public ProdAsset queryAssetByNameAndPath(String prod_id, String asset_catalog_name, String file_name, String runtime_env, String bucket_name, String bucket_path, String aws_type) throws Exception {
        return prodAssetDao.queryAssetByNameAndPath(prod_id, asset_catalog_name, file_name, runtime_env, bucket_name, bucket_path, aws_type);
    }

    public ProdAsset queryAssetByNameAndSid(String prod_id, String asset_catalog_name, String file_name, String runtime_env, String bucket_name, String bucket_path) throws Exception {
        return prodAssetDao.queryAssetByNameAndSid(prod_id, asset_catalog_name, file_name, runtime_env, bucket_name, bucket_path);
    }


    @Override
    public void updateNode(String old_release_node_name, String new_release_node_name) throws Exception {
        prodAssetDao.updateNode(old_release_node_name, new_release_node_name);

    }

    @Override
    public boolean isAssetCatalogUsed(String template_id, String asset_catalog_name) throws Exception {
        return prodAssetDao.isAssetCatalogUsed(template_id, asset_catalog_name);
    }

    @Override
    public List<ProdAsset> deleteByProd(String prod_id) throws Exception {
        return prodAssetDao.deleteByProd(prod_id);
    }

    @Override
    public void delCommonConfigByAppAndProd(String application_id, String prod_id) throws Exception {
        prodAssetDao.delCommonConfigByAppAndProd(application_id, prod_id);
    }

    @Override
    public List queryConfigAssetsByParam(String prod_id, List<String> catalogTypes) throws Exception {
        ProdRecord proRecord = prodRecordDao.queryDetail(prod_id);
        String sccProd = proRecord.getScc_prod();
        String template_id = proRecord.getTemplate_id();
        List<AssetCatalog> list = assetCatalogDao.queryByTemplateIdAndType(template_id, catalogTypes);
        // esf新增逻辑：如果是老模板过滤掉AWS,esf,esfcommonconfig介质目录
        if("0".equals(sccProd)){
            list = list.stream().filter(assetCatalog -> !"AWS_STATIC".equals(assetCatalog.getCatalog_name()) && !"AWS_COMMON".equals(assetCatalog.getCatalog_name()) && !"esf".equals(assetCatalog.getCatalog_name()) && !"esfcommonconfig".equals(assetCatalog.getCatalog_name())).collect(Collectors.toList());
        }
        if(CommonUtils.isNullOrEmpty(list)){
            new ArrayList<>();
        }
        List result = new ArrayList<>();
        for (AssetCatalog assetCatalog : list) {
            Map catalogResult = new HashMap<>();
            catalogResult.putAll(CommonUtils.beanToMap(assetCatalog));
            List<ProdAsset> prodAssetList = prodAssetDao.queryAssets(prod_id, assetCatalog.getCatalog_name());
            for (ProdAsset prodAsset : prodAssetList) {
                Map userInfo = userService.queryUserById(prodAsset.getUpload_user());
                prodAsset.setUpload_username_cn(userInfo == null ? "" : (String) userInfo.get(Dict.USER_NAME_CN));
                if (!CommonUtils.isNullOrEmpty(prodAsset.getSource_application())) {
                    try {
                        Map<String, Object> queryAPP = appService.queryAPPbyid(prodAsset.getSource_application());
                        prodAsset.setSource_application_name(queryAPP == null ? "未知应用" : (String) queryAPP.get("name_zh"));
                    } catch (Exception e) {
                    }
                }
                // AWS新增逻辑：当文件夹上传去掉加的_folder标识
                if("7".equals(assetCatalog.getCatalog_type())){
                    if("2".equals(prodAsset.getAws_type())){
                        String filename = prodAsset.getFileName();
                        prodAsset.setFileName(filename.split("_folder")[0] + filename.split("_folder")[1]);
                    }
                }

            }
            if("7".equals(assetCatalog.getCatalog_type())){
                AwsConfigure awsConfigure = null;
                if(!CommonUtils.isNullOrEmpty(proRecord.getAws_group())){
                    List<AwsConfigure> configList = queryAwsConfigByGroupId(proRecord.getAws_group());
                    if(!CommonUtils.isNullOrEmpty(configList)){
                        awsConfigure = configList.get(0);
                    }
                }
                if(!CommonUtils.isNullOrEmpty(prodAssetList)){ // 当AWS目录下有文件时，存储对象所属组不在那四个组里面，此时不展示所属组，页面提示用户去选择组
                    if(null != awsConfigure){
                        catalogResult.put(Dict.GROUP_NAME, awsConfigure.getGroup_name());
                    } else {
                        catalogResult.put(Dict.GROUP_NAME, "");
                    }
                } else { // 当AWS目录下没有文件时，不校验对象存储用户所属组是否包含在那四个组里面
                    catalogResult.put(Dict.GROUP_NAME, "");
                }
            }
            if("esf".equals(assetCatalog.getCatalog_name()) && "8".equals(assetCatalog.getCatalog_type())){
                List<EsfRegistration> esfRegistrationList = esfRegistrationDao.queryEsfRegists(prod_id);
                for(EsfRegistration esfRegistration : esfRegistrationList){
                    Map<String, Object> queryAPP = appService.queryAPPbyid(esfRegistration.getApplication_id());
                    esfRegistration.setApplication_en(queryAPP == null? "": (String) queryAPP.get("name_en"));
                    esfRegistration.setApplication_cn(queryAPP==null? "未知应用": (String) queryAPP.get("name_zh"));
                }
                catalogResult.put("esfUserList", esfRegistrationList);
            }
            catalogResult.put(Dict.ASSETS, prodAssetList);
            result.add(catalogResult);
        }
        return result;
    }

    @Override
    public Map deAutoCreate(ProdAsset prodAsset, MultipartFile[] files, String child_catalog) throws Exception {
        User user = CommonUtils.getSessionUser();
        String prod_id = prodAsset.getProd_id();
        //同一变更文件目录下的文件序号不能相同
        ProdAsset prodAssetSeq_no = prodAssetDao.queryAssetBySeq_no(prod_id, prodAsset.getAsset_catalog_name(), prodAsset.getSeq_no());
        if (!CommonUtils.isNullOrEmpty(prodAssetSeq_no)) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"文件序号已存在,请更换"});
        }
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        if (CommonUtils.isNullOrEmpty(prodRecord)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"变更记录不存在"});
        }
       /* // 根据介质目录名查询目录
        AssetCatalog assetCatalog = releaseCatalogService.queryAssetCatalogByName(prodRecord.getTemplate_id(), prodAsset.getAsset_catalog_name());
        //目录类型为1或2时,不能上传文件
        if ("1".equals(assetCatalog.getCatalog_type()) || "2".equals(assetCatalog.getCatalog_type())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前目录类型不能上传文件,请更换目录类型"});
        }*/
        prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
        // 配置文件路径+投产窗口名称 为全局唯一file路径
        String groupFullPath = recordAssetsUrl + prodRecord.getRelease_node_name();
        // 获取当前变更的变更版本version
        String version = prodRecord.getVersion();
        String projectFullPath = new StringBuilder(groupFullPath).append("/").append(version).toString();
        // 方式为上传文件
        prodAsset.setSource("1");
        String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
        prodAsset.setUpload_user(user.getId());
        prodAsset.setUpload_time(upload_time);

        String filename = "";
        String runtime_env = "";
        String path = "";
        Map prodAssetMap = new HashMap<>();
        for (MultipartFile file : files) {
            ProdAsset saveProdAsset;
            String assetCatalogName = prodAsset.getAsset_catalog_name();
            filename = file.getOriginalFilename();
            if (CommonUtils.isNullOrEmpty(assetCatalogName)) {
                path = projectFullPath + Constants.SPLIT_STRING + filename;
            } else {
                path = projectFullPath + Constants.SPLIT_STRING + assetCatalogName +  Constants.SPLIT_STRING + filename;
            }
            //文件上传
            fileService.uploadFiles(path, file, filename, "fdev-release");
            prodAsset.setFileName(filename);
            prodAsset.setFile_giturl(path);
            saveProdAsset = prodAssetDao.save(prodAsset);
            ProdAsset queryAssetsOne = prodAssetDao.queryAssetsOne(saveProdAsset.getId());
            prodAssetMap.put(saveProdAsset.getId(), queryAssetsOne);
        }
        return prodAssetMap;
    }

    @Override
    public List queryAssetsByProdId(String prod_id) throws Exception {
        List result = new ArrayList<>();
        List<ProdAsset> prodAssetList = prodAssetDao.queryAssetsList(prod_id);
        Map catalogResult = new HashMap<>();
        for (ProdAsset prodAsset : prodAssetList) {
            Map userInfo = userService.queryUserById(prodAsset.getUpload_user());
            prodAsset.setUpload_username_cn(userInfo == null ? "" : (String) userInfo.get(Dict.USER_NAME_CN));
            if (!CommonUtils.isNullOrEmpty(prodAsset.getSource_application())) {
                try {
                    Map<String, Object> queryAPP = appService.queryAPPbyid(prodAsset.getSource_application());
                    prodAsset.setSource_application_name(queryAPP == null ? "未知应用" : (String) queryAPP.get("name_zh"));
                } catch (Exception e) {
                }
            }
        }
        catalogResult.put(Dict.ASSETS, prodAssetList);
        result.add(catalogResult);
        return result;
    }

	@Override
	public Map uploadAssets(User user, String prod_id, String file_encoding, String asset_catalog_name, String source_application, String runtime_env, String seq_no, String child_catalog, String bucket_name, String bucket_path, String aws_type, MultipartFile[] files) throws Exception {
        // esfcommonconfig介质目录根据应用添加配置文件，同一应用只能添加一次
        if("esfcommonconfig".equals(asset_catalog_name)){
            List<ProdAsset> assetList = prodAssetDao.queryhasAssets(prod_id,asset_catalog_name, source_application);
            if(!CommonUtils.isNullOrEmpty(assetList)){
                throw new FdevException("该应用已添加！");
            }
        }
        // 参数校验
        if (CommonUtils.isNullOrEmpty(prod_id)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.PROD_ID});
        }
        // 变更记录日期小于当前日期,不能操作
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        AssetCatalog assetCatalog = catalogService.queryAssetCatalogByName(prodRecord.getTemplate_id(), asset_catalog_name);
        ReleaseNode releaseNode = releaseNodeService.queryDetail(prodRecord.getRelease_node_name());
        if(CommonUtils.isNullOrEmpty(releaseNode)) {
            throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EXIST, new String[]{prodRecord.getRelease_node_name()});
        }
        if (CommonUtils.isNullOrEmpty(asset_catalog_name)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.ASSET_CATALOG_NAME});
        }
        if (CommonUtils.isNullOrEmpty(files)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.FILE});
        }
        for (MultipartFile file : files) {
            // 新增逻辑：AWS新增上传zip的入口
            if("2".equals(aws_type)){ // type 1-文件上传入口，2-zip上传入口
                String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (fileType.equals("rar") || fileType.equals("7z") || fileType.equals("tar") || fileType.equals("war") || fileType.equals("jar")) {
                    throw new FdevException("上传文件类型错误,压缩包仅支持上传.zip格式，请重新上传!");
                }
            }
            if (file.getOriginalFilename().contains(" ")) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"文件名不能有空格"});
            }
            // 文件名。介质目录是公共配置commonconfig或者对象存储AWS时，环境必传
            String filename;
            String sid = "";
            String[] arr = file.getOriginalFilename().split("/");
            if (Dict.COMMONCONFIG.equals(asset_catalog_name) || "7".equals(assetCatalog.getCatalog_type()) || "dynamicconfig".equals(asset_catalog_name) || "bastioncommonconfig".equals(asset_catalog_name)) {
                filename = arr[0];
                runtime_env = arr[1];
            } else if("5".equals(assetCatalog.getCatalog_type())){ // 5-esfcommonconfig
                filename = arr[0];
                runtime_env = arr[1];
                bucket_name = arr[2];
                sid = arr[3];
            }else {
                filename = file.getOriginalFilename();
            }
            ProdAsset savedProdAsset;
            // 检查当前窗口下配置文件名唯一性
            if("7".equals(assetCatalog.getCatalog_type())){
                savedProdAsset = queryAssetByNameAndPath(prod_id, asset_catalog_name, filename, runtime_env, bucket_name, bucket_path, aws_type);
            }else if("5".equals(assetCatalog.getCatalog_type())){
                savedProdAsset = queryAssetByNameAndSid(prod_id, asset_catalog_name, filename, runtime_env, bucket_name, sid);
            }else {
                savedProdAsset = queryAssetByName(prod_id, asset_catalog_name, filename, runtime_env);
            }
            if (!CommonUtils.isNullOrEmpty(savedProdAsset)) {
                throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"文件名已存在,请更换"});
            }
            // asset_catalog字段为“commonconfig”或者“AWS”或者“esfcommonconfig”时runtime_env字段必传
            if (Dict.COMMONCONFIG.equals(asset_catalog_name) || "7".equals(assetCatalog.getCatalog_type()) || "5".equals(assetCatalog.getCatalog_type()) || "dynamicconfig".equals(asset_catalog_name)) {
                // 运行环境
                if (CommonUtils.isNullOrEmpty(runtime_env)) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.RUNTIME_ENV});
                }
            }
            // 若为数据库文件，不以.sql或.sh结尾的文件不排序
            if("3".equals(assetCatalog.getCatalog_type()) && !(filename.endsWith(".sql") || filename.endsWith(".sh"))) {
                seq_no = null;
            }
        }
        ProdAsset prodAsset = new ProdAsset();
        // 查询变更文件的投产窗口明细
        if (!roleService.isGroupReleaseManager(CommonUtils.getSessionUser().getGroup_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"需投产管理员角色"});
        }
        if(!CommonUtils.isNullOrEmpty(source_application)){
            prodAsset.setSource_application(source_application);
            Map<String, Object> queryAPP = appService.queryAPPbyid(source_application);
            String source_application_name = queryAPP == null ? "未知应用" : (String) queryAPP.get("name_zh");
            prodAsset.setSource_application_name(source_application_name);
        }
        prodAsset.setProd_id(prod_id);
        prodAsset.setFile_encoding(file_encoding);
        prodAsset.setAsset_catalog_name(asset_catalog_name);
        prodAsset.setChild_catalog(child_catalog);
        if(!CommonUtils.isNullOrEmpty(bucket_name)){
            prodAsset.setBucket_name(bucket_name);
        }
        if(!CommonUtils.isNullOrEmpty(bucket_path)){
            prodAsset.setBucket_path(bucket_path);
        }
        prodAsset.setAws_type(aws_type);
        // 有序文件
        if (!CommonUtils.isNullOrEmpty(seq_no)) {
            List<ProdAsset> assetsWithSeqno = queryAssetsWithSeqno(prod_id, asset_catalog_name);
            if (!CommonUtils.isNullOrEmpty(assetsWithSeqno)) {
                prodAsset.setSeq_no(String.valueOf(assetsWithSeqno.size() + 1));
            } else {
                prodAsset.setSeq_no("1");
            }
        }
        Map map;
        try {
            map = create(prodAsset, files, child_catalog, user, aws_type);
        } catch (Exception e) {
            logger.error("file upload failed:{}", e);
            throw e;
        }
        return map;
	}

    @Override
    public void updateEsfcommonconfigAssets(User user, String prod_id, String file_encoding, String asset_catalog_name, String source_application, String runtime_env, String child_catalog, String bucket_name, MultipartFile[] files) throws Exception {
        ProdAsset prodAsset = new ProdAsset();
        prodAsset.setSource_application(source_application);
        prodAsset.setProd_id(prod_id);
        prodAsset.setFile_encoding(file_encoding);
        prodAsset.setAsset_catalog_name(asset_catalog_name);
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        if (CommonUtils.isNullOrEmpty(prodRecord)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"变更记录不存在"});
        }
        prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
        // 配置文件路径+投产窗口名称 为全局唯一file路径
        String groupFullPath = recordAssetsUrl + prodRecord.getRelease_node_name();
        // 获取当前变更的变更版本version
        String version = prodRecord.getVersion();
        String projectFullPath = new StringBuilder(groupFullPath).append("/").append(version).toString();
        // 方式为上传文件
        prodAsset.setSource("1");
        String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
        prodAsset.setUpload_user(user.getId());
        prodAsset.setUpload_time(upload_time);
        for (MultipartFile file : files) {
            String filename = "";
            String sid = "";
            String path = "";
            if (file.getOriginalFilename().contains(" ")) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"文件名不能有空格"});
            }
            // 介质目录是esfcommonconfig环境必传
            if ("esfcommonconfig".equals(asset_catalog_name)) {
                String[] arr = file.getOriginalFilename().split("/");
                filename = arr[0];
                runtime_env = arr[1];
                bucket_name = arr[2];
                sid = arr[3];
            } else {
                filename = file.getOriginalFilename();
            }
            ProdAsset savedProdAsset;
            // 检查当前窗口下配置文件名唯一性
            if("esfcommonconfig".equals(asset_catalog_name)){
                savedProdAsset = queryAssetByNameAndSid(prod_id, asset_catalog_name, filename, runtime_env, bucket_name, sid);
            }else {
                savedProdAsset = queryAssetByName(prod_id, asset_catalog_name, filename, runtime_env);
            }
            if (!CommonUtils.isNullOrEmpty(savedProdAsset)) {
                throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"文件名已存在,请更换"});
            }
            // asset_catalog字段为“esfcommonconfig”时runtime_env字段必传
            if ("esfcommonconfig".equals(asset_catalog_name)) {
                // 运行环境
                if (CommonUtils.isNullOrEmpty(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("/") + 1))) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.RUNTIME_ENV});
                }
                prodAsset.setRuntime_env(runtime_env);
                prodAsset.setBucket_name(bucket_name);
                prodAsset.setSid(sid);
                // 拼一层目录结构　sid_20210925
                String date = prodRecord.getDate();
                date = date.replace("/","");
                String fileContent = sid + "_" + date;
                if (CommonUtils.isNullOrEmpty(child_catalog)) {
                    path = projectFullPath + Constants.SPLIT_STRING + asset_catalog_name + Constants.SPLIT_STRING + runtime_env + Constants.SPLIT_STRING + fileContent + Constants.SPLIT_STRING + filename;
                } else {
                    path = projectFullPath + Constants.SPLIT_STRING + asset_catalog_name + Constants.SPLIT_STRING + runtime_env + Constants.SPLIT_STRING + fileContent + Constants.SPLIT_STRING + child_catalog + Constants.SPLIT_STRING + filename;
                }
            }
            //文件上传
            fileService.uploadFiles(path, file, filename, "fdev-release");
            prodAsset.setFileName(filename);
            prodAsset.setFile_giturl(path);
            prodAssetDao.editEsfCommonconfig(prodAsset);
        }


    }

    @Override
    public void uploadSourceMap(String userId, ProdRecord prodRecord, String asset_catalog_name, String application_id, String application_name,ReleaseApplication releaseApplication, String tag) throws Exception {
        if(CommonUtils.isNullOrEmpty(releaseApplication) || CommonUtils.isNullOrEmpty(releaseApplication.getPath())
                || CommonUtils.isNullOrEmpty(releaseApplication.getPath().get(tag))) {
            return;
        }
        List<ProdRecord> list = prodRecordDao.queryByReleaseNodeName(prodRecord);
        for(ProdRecord pr : list) {
            ProdApplication pa = prodApplicationDao.queryByTag(pr.getProd_id(), application_id, tag);
            if(!CommonUtils.isNullOrEmpty(pa)) {
                logger.info("sourceMap.tar文件已在变更{}中生成，镜像名称{}", pr.getVersion(), pa.getPro_image_uri());
                return;
            }
        }
        String path = releaseApplication.getPath().get(tag);
        ProdAsset prodAsset = new ProdAsset();
        String scc_prod = prodRecord.getScc_prod();
        if ("0".equals(scc_prod)) {
        	prodAsset.setSource_application(application_id);
        	prodAsset.setSource_application_name(application_name);
        	prodAsset.setProd_id(prodRecord.getProd_id());
        	prodAsset.setAsset_catalog_name(asset_catalog_name);
        	prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
        	// 方式为上传文件
        	prodAsset.setSource("1");
        	String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
        	prodAsset.setUpload_user(userId);
        	prodAsset.setUpload_time(upload_time);
        	prodAsset.setFileName(path.split("/")[path.split("/").length - 1]);
        	prodAsset.setFile_giturl(path);
        	prodAssetDao.save(prodAsset);
		} else if ("1".equals(scc_prod)) {
			String fileName = path.split("/")[path.split("/").length - 1];
			String paths =path.split(fileName)[0];
			CommonUtils.createDirectory(paths);
        	fileService.downloadDocumentFile(path,path,"fdev-release");
			for (int i = 0; i < 3; i++) {
				prodAsset.setSource_application(application_id);
	        	prodAsset.setSource_application_name(application_name);
	        	prodAsset.setProd_id(prodRecord.getProd_id());
	        	prodAsset.setAsset_catalog_name(asset_catalog_name);
	        	prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
	        	// 方式为上传文件
	        	prodAsset.setSource("1");
	        	String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
	        	prodAsset.setUpload_user(userId);
	        	prodAsset.setUpload_time(upload_time);
	        	prodAsset.setFileName(fileName);
	        	prodAsset.setChild_catalog("/mupload/spdb-footprint-processor");
	        	String runtime_env = null;
	        	switch (i) {
				case 0:
					prodAsset.setRuntime_env("DEV");
					runtime_env = "DEV";
					break;
				case 1:
					prodAsset.setRuntime_env("TEST");
					runtime_env = "TEST";
					break;
				case 2:
					prodAsset.setRuntime_env("PROCSH");
					runtime_env = "PROCSH";
					break;
				}
	        	String minioPath = recordAssetsUrl + prodRecord.getRelease_node_name() + "/" + prodRecord.getVersion() + "/" + "bastioncommonconfig" + "/" + runtime_env + "/mupload/spdb-footprint-processor/" + fileName;
	        	fileService.uploadWord(minioPath,new File(path),"fdev-release");
	        	prodAsset.setFile_giturl(minioPath);
	        	prodAssetDao.save(prodAsset);
			}
		}
    }

    @Override
    public void uploadRouter(String userId, ProdRecord prodRecord, String asset_catalog_name, String application_id, String application_name, String application_name_en ,ReleaseApplication releaseApplication, String tag) throws Exception {
        Map<String, Object> resultMap;
        Map<String, Object> queryAPPJson;
        try {
            resultMap = appDatService.queryLastAppJson(application_name_en , tag);
            queryAPPJson  = (Map<String, Object>) resultMap.get("appJson");
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用：" + application_name_en +"_repo.json查询异常"});
        }
        if(CommonUtils.isNullOrEmpty(queryAPPJson)) {
            return;

        }
        String release_node_name = prodRecord.getRelease_node_name();
        String release_node_version = prodRecord.getVersion();
        String fileName = application_name_en+Constants.SPLIT_STRING+"repo.json";
        ProdAsset prodAsset = new ProdAsset();
        prodAsset.setSource_application(application_id);
        prodAsset.setSource_application_name(application_name);
        prodAsset.setProd_id(prodRecord.getProd_id());
        prodAsset.setAsset_catalog_name(asset_catalog_name);
        prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
        // 方式为上传文件
        prodAsset.setSource("1");
        String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
        prodAsset.setUpload_user(userId);
        prodAsset.setUpload_time(upload_time);
        prodAsset.setFileName(fileName);
        
        prodAsset.setFirst((Boolean) resultMap.get(Dict.ISFIRST));
        prodAsset.setCfg_type((String) resultMap.get(Dict.CFGTYPE));
        String repoJsonFileName = (String) queryAPPJson.get("repoTarName");
        String repoJsonFilePath = (String) queryAPPJson.get("repoJsonPath");
        MultipartFile f;
        try {
            f = new MockMultipartFile(repoJsonFileName, repoJsonFileName, ContentType.APPLICATION_OCTET_STREAM.toString(),
                    new FileInputStream(new File(repoJsonFilePath)));
        } catch (IOException e) {
             throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{repoJsonFileName + "文件生成失败"});
        }
        String sccProd = prodRecord.getScc_prod();
        String prodType = prodRecord.getType();
        
        if("1".equals(sccProd)) {
        	String minioPath = null;
            if("bastioncommonconfig".equals(asset_catalog_name) && "gray".equals(prodType)) {
            	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/DEV/grayparam/mobgray/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("DEV");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/DEV/grayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/DEV/hfgrayparam/mobgray/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("DEV");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/DEV/hfgrayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/TEST/grayparam/mobgray/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("TEST");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/TEST/grayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/TEST/hfgrayparam/mobgray/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("TEST");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/TEST/hfgrayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/PROCSH/grayparam/mobgray/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("PROCSH");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/PROCSH/grayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/PROCHF/hfgrayparam/mobgray/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("PROCHF");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/PROCHF/hfgrayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
            }else if("bastioncommonconfig".equals(asset_catalog_name) && "proc".equals(prodType)) {
            	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/DEV/params/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("DEV");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/DEV/params/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/TEST/params/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("TEST");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/TEST/params/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/PROCSH/params/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("PROCSH");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/PROCSH/params/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
                
                minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/bastioncommonconfig" + "/PROCHF/params/dynamicconfig/syncconfig/xrouter/"  + application_name_en+"/repo.json";
            	prodAsset.setRuntime_env("PROCHF");
            	fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            	prodAsset.setFile_giturl(minioPath);
            	prodAsset.setChild_catalog("/PROCHF/params/dynamicconfig/syncconfig/xrouter/"+application_name_en);
                prodAssetDao.save(prodAsset);
            }
        }else {
        	prodAsset.setChild_catalog(application_name_en);
        	String minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/config" + "/" + application_name_en+"/repo.json";
            try {
                fileService.uploadFiles(minioPath, f, "repo.json", "fdev-release");
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{repoJsonFileName + "文件上传minio失败"});
            }
            prodAsset.setFile_giturl(minioPath);
            prodAssetDao.save(prodAsset);
        }
        
    }

    @Override
    public List<AwsConfigure> queryAwsConfigByGroupId(String groupId) {
        return prodAssetDao.queryAwsConfigByGroupId(groupId);
    }

    @Override
    public Map<String, Object> queryAllProdAssets(Map<String, String> requestParam) throws Exception {
        // 定义返回map
        Map<String, Object> retuen_map = new HashMap<>();
        String prod_id = requestParam.get(Dict.PROD_ID);
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        //变更类型
        String type = prodRecord.getType();
        if (!CommonUtils.isNullOrEmpty(prodRecord.getTemplate_properties())) {
            retuen_map.put(Dict.TEMPLATE_PROPERTIES, prodRecord.getTemplate_properties());
        }
        Map<String, String> prodImages = prodRecordService.queryBeforePordImages(prodRecord.getRelease_node_name(), prodRecord.getVersion());
        Map<String, Map> prodAssets = new HashMap();
        // 查询所有变更应用
        List<Map> prodApplications = prodApplicationService.queryApplications(new ProdApplication(prod_id));
        Map<String, OptionalCatalog> catalogs = optionalCatalogService.queryOptionalCatalogs();
        Set<String> orderTxtAdded = new HashSet<>();
        List<String> sbList = new ArrayList<String>();
        prodApplications.forEach(prodApplication -> {
            Map applicationMap = new HashMap();
            applicationMap.put(Dict.ASSET_NAME, prodApplication.get(Dict.APP_NAME_EN));
            applicationMap.put(Dict.PRO_IMAGE_URI, prodApplication.get(Dict.PRO_IMAGE_URI));
            applicationMap.put(Dict.DEPLOY_TYPE, prodApplication.get(Dict.DEPLOY_TYPE));
            List<String> catalogName = (List<String>) prodApplication.get(Dict.PROD_DIR);
            catalogName.stream().forEach(catalog->{
                String platform = catalog.split("_")[0];
                showOrderTxt(prodAssets, catalog, catalogs.get(catalogName), orderTxtAdded);
                if("docker".equals(platform)){
                    Map caas_applicationMap = new HashMap();
                    caas_applicationMap.put(Dict.PRO_IMAGE_URI, prodApplication.get(Dict.PRO_IMAGE_URI));
                    caas_applicationMap.putAll(applicationMap);
                    assembleAssets(prodAssets, catalog, catalogs.get(catalogName), caas_applicationMap);
                }else{
                    Map scc_applicationMap = new HashMap();
                    scc_applicationMap.put(Dict.PRO_SCC_IMAGE_URI,prodApplication.get(Dict.PRO_SCC_IMAGE_URI));
                    scc_applicationMap.putAll(applicationMap);
                    assembleAssets(prodAssets, catalog, catalogs.get(catalogName), scc_applicationMap);
                }

            });
            boolean isPackaged = !CommonUtils.isNullOrEmpty(prodImages)
                    && !CommonUtils.isNullOrEmpty(prodImages.get(prodApplication.get(Dict.PRO_IMAGE_URI)));
            if (isPackaged) {
                sbList.add("[" + prodApplication.get(Dict.APP_NAME_EN) + "]");
            }
        });
        List<String> sb = new ArrayList<>(new LinkedHashSet<>(sbList));
        String application_tips = String.join(",", sb).replace(",", "");
        if (!CommonUtils.isNullOrEmpty(application_tips)) {
            retuen_map.put(Dict.APPLICATION_TIPS,
                    new StringBuilder().append("应用").append(application_tips).append("已在本次投产窗口下的其他变更中生成镜像，本次变更不再生成。").toString());
        }

        // 查询所有变更文件列表
        Map<String, Object> sortedAssets = querySortedAssets(prod_id);
        for (Iterator iterator = sortedAssets.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry sortedAssetsEntry = (Map.Entry) iterator.next();
            String catalogName = (String) sortedAssetsEntry.getKey();
            if (sortedAssetsEntry.getValue() instanceof List) { // 不区分环境变更文件
                List<Object> objects = (List<Object>) sortedAssetsEntry.getValue();
                for (Object object : objects) {
                    if (object instanceof ProdAsset) {
                        ProdAsset asset = (ProdAsset) object;
                        String child_catalog;
                        if (asset.getChild_catalog() == null) {
                            child_catalog = "";
                        } else {
                            child_catalog = asset.getChild_catalog() + "/";
                        }
                        String sourceMap = assetShowName(asset).split("-")[0];
                        Map fileMap = new HashMap();
                        fileMap.put(Dict.ASSET_NAME, assetShowName(asset));
                        fileMap.put(Dict.ASSET_URL, asset.getFile_giturl());
                        if("sourceMap".equals(sourceMap)) {
                            String sourceMapStr="#add:cfg_nas&xxx#"+assetShowName(asset)+"#/ebank/spdb/mupload/spdb-footprint-processor/"+child_catalog+assetShowName(asset);
                            String sourceMapStr1="#add:cfg_nas&xxx#"+assetShowName(asset)+"#/ebank/spdb/mupload/spdb-footprint-processor/"+child_catalog+assetShowName(asset);
                            String asset_templateContent = sourceMapStr +"\n" + sourceMapStr1;
                            fileMap.put("asset_templateContent", asset_templateContent);
                        }else {
                            String str="#cover:cfg_core&all#"+assetShowName(asset)+"#/ebank/spdb/configs/"+child_catalog+assetShowName(asset);
                            fileMap.put("asset_templateContent", str);
                        }
                        fileMap.put(Dict.WRITE_FLAG, asset.getWrite_flag());
                        if (!Util.isNullOrEmpty(asset.getSeq_no())) {
                            showOrderTxt(prodAssets, catalogName, catalogs.get(catalogName), orderTxtAdded);
                        }
                        assembleAssets(prodAssets, catalogName, catalogs.get(catalogName), fileMap);
                    } else {
                        Map<String, List> repoFiles = (Map<String, List>) object;
                        setMapTypeDate(repoFiles, prodAssets, catalogName, catalogs , type);
                    }
                }
            } else if (sortedAssetsEntry.getValue() instanceof Map) {  //区分环境文件
                Map<String, List> envProdFiles = (Map<String, List>) sortedAssetsEntry.getValue();
                // 遍历各环境文件列表
                setMapTypeDate(envProdFiles, prodAssets, catalogName, catalogs , type);
            }
        }

        // 查询应用扩展列表
        Map paramMap = new HashMap<>();
        paramMap.put(Dict.PROD_ID, prod_id);
        List<Map<String, Object>> appScaleList = prodAppScaleService.queryAppScale(paramMap);
        if (!Util.isNullOrEmpty(appScaleList)) {
            Map<String, Map> orderMap = new HashMap();
            List<String> deploys = (List<String>) appScaleList.get(0).get(Dict.DEPLOY_TYPE);
            List<Map<String, String>> env_scales = (List<Map<String, String>>) appScaleList.get(0).get(Dict.ENV_SCALES);
            for (Map<String, String> map : env_scales) {
                Map childMap = new HashMap();
                childMap.put(Dict.ASSET_NAME, ORDER_TXT_NAME);
                assembleAssets(orderMap, map.get(Dict.ENV_NAME), null, childMap);
            }
            for (Map map : orderMap.values()) {
                OptionalCatalog optionalCatalog = new OptionalCatalog();
                optionalCatalog.setDescription("弹性伸缩");
                if(deploys.contains("CAAS")){
                    assembleAssets(prodAssets, Dict.DOCKER_SCALE, optionalCatalog, map);
                }
                if(deploys.contains("SCC")){
                    assembleAssets(prodAssets, "scc_scale", optionalCatalog, map);
                }

            }
        }
        // 查询AWS对象存储列表
        Iterator iter = prodAssets.values().iterator();
        String aws_group = prodRecord.getAws_group();
        while (iter.hasNext()){
            Map prodAssetMap = (Map) iter.next();
            if("AWS_STATIC".equals(prodAssetMap.get(Dict.CATALOG_NAME)) || "AWS_COMMON".equals(prodAssetMap.get(Dict.CATALOG_NAME))){
                if(!CommonUtils.isNullOrEmpty(prodAssetMap.get(Dict.CHILDREN))){ // 有介质判断组是否正确
                    List<HashMap> children = (List<HashMap>) prodAssetMap.get(Dict.CHILDREN);
                    children.forEach(map -> {
                        List<HashMap> assertList = (List<HashMap>)map.get(Dict.CHILDREN);
                        if(!CommonUtils.isNullOrEmpty(assertList)){
                            HashMap assertMap = new HashMap();
                            assertMap.put(Dict.ASSET_NAME, ORDER_TXT_NAME);
                            assertList.add(0, assertMap);
                        }
                    });
                    if(!CommonUtils.isNullOrEmpty(aws_group)){
                        List<AwsConfigure> awsConfigureList = queryAwsConfigByGroupId(aws_group);
                        if(!CommonUtils.isNullOrEmpty(awsConfigureList)){
                            prodAssetMap.put("right_group", true);
                        } else {
                            prodAssetMap.put("right_group", false);
                        }
                    } else {
                        prodAssetMap.put("right_group", false);
                    }
                } else { // 没有介质不展示
                    iter.remove();
                }
            }
        }
        // 查询esf列表
        List<EsfRegistration> esfRegistrationList = esfRegistrationDao.queryEsfRegists(prod_id);
        if(!CommonUtils.isNullOrEmpty(esfRegistrationList)){
            OptionalCatalog optionalCatalog = new OptionalCatalog();
            optionalCatalog.setDescription("esf注册");
            optionalCatalog.setCatalog_type("8");
            showOrderTxt(prodAssets, "esf", optionalCatalog, orderTxtAdded);
            for(EsfRegistration esfRegistration: esfRegistrationList){
                String application_id = esfRegistration.getApplication_id();
                Map<String, Object> queryAPP = appService.queryAPPbyid(application_id);
                ProdApplication prodApplication = prodApplicationService.queryApplication(prod_id, application_id);
                List<String> deployList = prodApplication.getDeploy_type();
                List<String> esf_paltfrom = prodApplication.getEsf_platform();
                List<String> chaList = new ArrayList<>();
                if(deployList.size()>esf_paltfrom.size()){
                    chaList = deployList.stream().filter(item -> !esf_paltfrom.contains(item)).collect(Collectors.toList());
                }

                Map<String,Object> appMap = new HashMap<>();
                appMap.put(Dict.ASSET_NAME, queryAPP.get("name_en"));
                List<String> esf_platform = esfRegistration.getPlatform();
                if(esf_platform.contains("CAAS")){
                    appMap.put(Dict.PRO_IMAGE_URI, prodApplication.getPro_image_uri());
                }
                if(esf_paltfrom.contains("SCC")){
                    appMap.put(Dict.PRO_SCC_IMAGE_URI,prodApplication.getPro_scc_image_uri());
                }
                if(chaList.contains("SCC")){
                    appMap.put(Dict.PRO_SCC_IMAGE_URI,prodApplication.getPro_scc_image_uri());
                }
                appMap.put(Dict.DEPLOY_TYPE,esf_platform);
                assembleAssets(prodAssets, "esf", optionalCatalog, appMap);
            }
        }
        
    	//新增批量任务目录
        List<BatchTaskInfo> batchTaskInfos = batchTaskDao.queryBatchTaskInfoByProdId(prod_id);
        if (!CommonUtils.isNullOrEmpty(batchTaskInfos)) {
        	OptionalCatalog optionalCatalog = new OptionalCatalog();
            optionalCatalog.setDescription("批量任务");
            optionalCatalog.setCatalog_type("9");
            showOrderTxt(prodAssets, "batch", optionalCatalog, orderTxtAdded);
            for (BatchTaskInfo batchTaskInfo : batchTaskInfos) {
				Map<String, Object> batchMap = new HashMap<String, Object>();
				batchMap.put(Dict.ASSET_NAME, batchTaskInfo.getType());
				batchMap.put("batchInfo", batchTaskInfo.getBatchInfo());
				assembleAssets(prodAssets, "batch", optionalCatalog, batchMap);
			}
		}

        retuen_map.put(Dict.PROD_ASSETS, prodAssets.values());
        return retuen_map;
    }

    /**
     * 前端展示文件名
     *
     * @param prodAsset
     * @return
     */
    private String assetShowName(ProdAsset prodAsset) {
        StringBuilder nameSb = new StringBuilder();
        if (!Util.isNullOrEmpty(prodAsset.getSeq_no())) {
            nameSb.append(prodAsset.getSeq_no()).append(". ");
        }
        if (!Util.isNullOrEmpty(prodAsset.getChild_catalog())) {
            nameSb.append(prodAsset.getChild_catalog()).append("/");
        }
        String namestr = "";
        // 新增逻辑：AWS配置文件更新通过文件夹上传，介质准备展示文件夹名称
        if("AWS_STATIC".equals(prodAsset.getAsset_catalog_name()) || "AWS_COMMON".equals(prodAsset.getAsset_catalog_name())){
            String filename = prodAsset.getFileName();
            String giturl = prodAsset.getFile_giturl();
            String env = prodAsset.getRuntime_env();
            String split_env = "/" + env + "/";
            String bucket_file = giturl.split(split_env)[1];
            if("0".equals(prodAsset.getAws_type())){
                namestr = nameSb.append(bucket_file.substring(0,bucket_file.indexOf("."))).toString();
            }else if("2".equals(prodAsset.getAws_type()) && filename.contains("_folder")){
                namestr = nameSb.append(bucket_file.substring(0,bucket_file.indexOf("_folder"))).toString();
            }else{
                namestr = nameSb.append(bucket_file).toString();
            }
        }else{
            namestr = nameSb.append(prodAsset.getFileName()).toString();
        }
        if(namestr.indexOf("repo.json") != -1){
            namestr = "repo.json";
        }

        return namestr;
    }

    /**
     * 添加order.txt
     *
     * @param prodAssets
     * @param catalogName
     * @param optionalCatalog
     * @param orderTxtAdded
     */
    private void showOrderTxt(Map<String, Map> prodAssets, String catalogName,
                              OptionalCatalog optionalCatalog, Set<String> orderTxtAdded) {
        if (!orderTxtAdded.contains(catalogName)) {
            Map orderMap = new HashMap();
            orderMap.put(Dict.ASSET_NAME, ORDER_TXT_NAME);
            assembleAssets(prodAssets, catalogName, optionalCatalog, orderMap);
            orderTxtAdded.add(catalogName);
        }
    }

    /**
     * 聚合所有变更文件
     *
     * @param prodAssets
     * @param catalogName
     * @param optionalCatalog
     * @param assetMap
     */
    private void assembleAssets(Map<String, Map> prodAssets, String catalogName, OptionalCatalog optionalCatalog,
                                Map assetMap) {
        Map catalogMap = prodAssets.get(catalogName);
        if (catalogMap == null) {
            catalogMap = new HashMap();
            catalogMap.put(Dict.CATALOG_NAME, catalogName);
            catalogMap.put(Dict.CATALOG_DESCRIPTION, optionalCatalog == null ? "" : optionalCatalog.getDescription());
            catalogMap.put(Dict.CATALOG_TYPE, optionalCatalog == null ? "" : optionalCatalog.getCatalog_type());
            prodAssets.put(catalogName, catalogMap);
        }
        if (assetMap != null) {
            List assets = (List) catalogMap.get(Dict.CHILDREN);
            if (assets == null) {
                assets = new ArrayList();
            }
            assets.add(assetMap);
            catalogMap.put(Dict.CHILDREN, assets);
        }
    }

    /**
     * 存放map类型的数据
     *
     * @param FilesMap    每个目下的文件Map集合
     * @param prodAssets  变更文件列表
     * @param catalogName 目录名
     * @param catalogs    介质可选目录表的Map
     */
    private void setMapTypeDate(Map<String, List> FilesMap, Map<String, Map> prodAssets, String catalogName, Map<String, OptionalCatalog> catalogs ,String type) {
        for (Iterator iter = FilesMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String parentName = (String) entry.getKey();
            List<ProdAsset> assets = (List<ProdAsset>) entry.getValue();
            Map<String, Map> childAssetsMap = new HashMap<>();
            for (ProdAsset asset : assets) {
                Map fileMap = new HashMap();
                if("bastioncommonconfig".equals(asset.getAsset_catalog_name()) && !Util.isNullOrEmpty(asset.getChild_catalog())) {
                	String [] childCatalog = asset.getChild_catalog().split(asset.getRuntime_env());
                	if(childCatalog.length > 1) {
                		fileMap.put(Dict.ASSET_NAME, asset.getChild_catalog().split(asset.getRuntime_env())[1]+"/"+assetShowName(asset));		
                	}else if(asset.getFileName().contains("tar")){
                		fileMap.put(Dict.ASSET_NAME, assetShowName(asset));	
                	}else {
                		fileMap.put(Dict.ASSET_NAME, asset.getChild_catalog()+"/"+assetShowName(asset));
                	}
                }else if ("dynamicconfig".equals(asset.getAsset_catalog_name()) && !Util.isNullOrEmpty(asset.getChild_catalog())){
                	String [] childCatalog = asset.getChild_catalog().split(asset.getRuntime_env());
                	if(childCatalog.length > 1) {
                		fileMap.put(Dict.ASSET_NAME, asset.getChild_catalog().split(asset.getRuntime_env())[1]);		
                	}else {
                		fileMap.put(Dict.ASSET_NAME, asset.getChild_catalog()+"/"+assetShowName(asset));
                	}
                }else {
                	fileMap.put(Dict.ASSET_NAME, assetShowName(asset));
                }
                fileMap.put(Dict.ASSET_URL, asset.getFile_giturl());
                //拼接模板内容
                String str;
                String child_catalog;
                String cfg_type;
                String updateType = "#cover:";
                if (asset.getChild_catalog() == null) {
                    child_catalog = "";
                } else {
                    child_catalog = asset.getChild_catalog() + "/";
                }
                if (asset.getCfg_type() == null) {
                    cfg_type = "cfg_core&all#";
                } else {
                    cfg_type = asset.getCfg_type() + "&all#";
                }
                if (asset.isFirst()) {
                    updateType = "#add:";
                }
                if (catalogName.equals("config")) {
                    if(assetShowName(asset).equals("repo.json")){
                        if (type.equals("gray")){
                            str = updateType + cfg_type + child_catalog +assetShowName(asset) +"#/ebank/spdb/grayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+ child_catalog + assetShowName(asset)+"\n"+
                                    updateType + cfg_type + child_catalog +assetShowName(asset) +"#/ebank/spdb/hfgrayparam/mobgray/dynamicconfig/syncconfig/xrouter/"+ child_catalog + assetShowName(asset);
                        }else {
                            str = updateType + cfg_type + child_catalog +assetShowName(asset) +"#/ebank/spdb/params/dynamicconfig/syncconfig/xrouter/"+ child_catalog + assetShowName(asset);
                        }
                        fileMap.put("asset_templateContent", str);
                    }else {
                        str = updateType + cfg_type + child_catalog +assetShowName(asset) +"#/ebank/spdb/configs/"+ child_catalog + assetShowName(asset);
                        fileMap.put("asset_templateContent", str);
                    }
                }
                assembleAssets(childAssetsMap, parentName, null, fileMap);
            }
            for (Map map : childAssetsMap.values()) {
                assembleAssets(prodAssets, catalogName, catalogs.get(catalogName), map);
            }
        }
    }
    
	@Override
	public void uploadSccRouter(String userId, ProdRecord prodRecord, String asset_catalog_name, String application_id,
			String application_name, String application_name_en, ReleaseApplication releaseApplication, String tag)
			throws Exception {
		String repoName = new StringBuffer(application_name_en).append("_repo.json").toString();
		StringBuffer queryRepoUrl = new StringBuffer();
		queryRepoUrl.append(nacosRelUrl).append("?").append("username").append("=").append(nacosUserName);
		queryRepoUrl.append("&").append("password").append("=").append(nacosPassword);
		queryRepoUrl.append("&").append("tenant").append("=").append("scc-rel-uz11");
		queryRepoUrl.append("&").append("group").append("=").append(nacosGroup);
		queryRepoUrl.append("&").append("dataId").append("=").append(repoName);

		ResponseEntity responseEntity = queryNacosRepo(queryRepoUrl.toString());

		String downTime = CommonUtils.formatDate("yyyyMMddHHmmss");
		
		if (Util.isNullOrEmpty(responseEntity)) {
			logger.info("路由介质上传minio失败，请检查nacos平台是否存在路由介质");
			return ;
		} else {
			downNacosRepo(responseEntity, downRepoPath,application_name_en+"_"+downTime+"_repo.json");
		}
        
        String release_node_name = prodRecord.getRelease_node_name();
        String release_node_version = prodRecord.getVersion();
        
        ProdAsset prodAsset = new ProdAsset();
        prodAsset.setSource_application(application_id);
        prodAsset.setSource_application_name(application_name);
        prodAsset.setProd_id(prodRecord.getProd_id());
        prodAsset.setAsset_catalog_name(asset_catalog_name);
        prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
        // 方式为上传文件
        prodAsset.setSource("1");
        String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
        prodAsset.setUpload_user(userId);
        prodAsset.setUpload_time(upload_time);
        prodAsset.setFileName(repoName);
//        prodAsset.setFirst((Boolean) resultMap.get(Dict.ISFIRST));//是否首次投产
        prodAsset.setCfg_type("cfg_nas");
     
        MultipartFile f;
		try {
			f = new MockMultipartFile(repoName, repoName, ContentType.APPLICATION_OCTET_STREAM.toString(),
					new FileInputStream(new File(downRepoPath+application_name_en+"_"+downTime+"_repo.json")));
		} catch (IOException e) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { repoName + "文件生成失败" });
		}
    	 
		String minioPath = null;
   	 	
        prodAsset.setFile_giturl(minioPath);
        if("gray".equals(prodRecord.getType())) {
        	prodAsset.setRuntime_env("DEV");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/DEV/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/DEV/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("TEST");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/TEST/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/TEST/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("shanghaiHd");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/shanghaiHd/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/shanghaiHd/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("hefeiHd");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/hefeiHd/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/hefeiHd/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	//Caas
        	prodAsset.setRuntime_env("CAAS_SHHd");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/CAAS_SHHd/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/CAAS_SHHd/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("CAAS_HFHd");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/CAAS_HFHd/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/CAAS_HFHd/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        }else if("proc".equals(prodRecord.getType())) {
        	prodAsset.setRuntime_env("DEV");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/DEV/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/DEV/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("TEST");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/TEST/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/TEST/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("shanghaiK1");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/shanghaiK1/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/shanghaiK1/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("shanghaiK2");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/shanghaiK2/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/shanghaiK2/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("hefeiK1");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/hefeiK1/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/hefeiK1/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("hefeiK2");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/hefeiK2/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/hefeiK2/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	//Caas
        	prodAsset.setRuntime_env("CAAS_SHK1");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/CAAS_SHK1/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/CAAS_SHK1/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("CAAS_SHK2");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/CAAS_SHK2/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/CAAS_SHK2/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("CAAS_HFK1");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/CAAS_HFK1/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/CAAS_HFK1/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        	
        	prodAsset.setRuntime_env("CAAS_HFK2");
        	minioPath = recordAssetsUrl + release_node_name + "/" + release_node_version + "/dynamicconfig" +"/CAAS_HFK2/per_xrouter/" + repoName;
        	uploadMinio(minioPath, f, repoName);
        	prodAsset.setFile_giturl(minioPath);
        	prodAsset.setChild_catalog("/CAAS_HFK2/per_xrouter/"+repoName);
        	prodAssetDao.save(prodAsset);
        }
    }
    
    /**
     * 调用nacos api 查询应用{app_name}_repo.json文件
     * */
    public ResponseEntity queryNacosRepo(String url) {
    	MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
    	ResponseEntity responseEntity = null;
    	try {
			responseEntity = HttpRequestUtils.sendGET(url, header);
		} catch (Exception e) {
			return responseEntity;
		}
    	return responseEntity;
    }
    
    /**
     * 调用nacos api 查询应用{app_name}_repo.json文件
     * */
    public void downNacosRepo(ResponseEntity responseEntity,String filePath,String fileName) {
    	FileOutputStream fos = null;
    	if(Util.isNullOrEmpty(responseEntity)) {
        	logger.info("路由介质上传minio失败，请检查nacos平台是否存在路由介质");
        }else {
	    	String body = (String) responseEntity.getBody();
	    	File file = new File(filePath);
	    	if(!file.exists()) {
	    		file.mkdir();
	    	}
	    	File downRepoFile = new File(filePath+fileName);
    		try {
				fos = new FileOutputStream(downRepoFile);
				fos.write(body.getBytes());
			} catch (FileNotFoundException e) {
				logger.info("文件不存在"+filePath);
			} catch (IOException e) {
				logger.info("io异常"+filePath);
			}finally {
				try {
					fos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }
    }

    
    public void uploadMinio(String minioPath,MultipartFile f,String repoName) {
    	try {
            fileService.uploadFiles(minioPath, f, repoName, "fdev-release");
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{repoName + "文件上传minio失败"});
        }
    }

	@Override
	public void delCommonConfigByAssetCatalogName(String application_id, String prod_id, String asset_catalog_name)
			throws Exception {
		prodAssetDao.delCommonConfigByAssetCatalogName(application_id, prod_id, asset_catalog_name);
		
	}
}
