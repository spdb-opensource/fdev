package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.dao.IReleaseTaskDao;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseRqrmnt;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xwpf.usermodel.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Api(tags = "投产需求接口")
@RequestMapping("/api/rqrmnt")
@RestController
@RefreshScope
public class ReleaseRqrmntController {

    private static Logger logger = LoggerFactory.getLogger(ReleaseRqrmntController.class);

    @Autowired
    IReleaseRqrmntService releaseRqrmntService;

    @Autowired
    IReleaseTaskService releaseTaskService;

    @Autowired
    IUserService userService;

    @Autowired
    IReleaseApplicationService releaseApplicationService;

    @Autowired
    IReleaseNodeService releaseNodeService;

    @Autowired
    ITaskService taskService;

    @Autowired
    IReleaseRqrmntInfoService releaseRqrmntInfoService;

    @Autowired
    IAppService appService;

    @Autowired
    IFileService fileService;

    @Autowired
    IGenerateExcelService generateExcelService;

    @Value("${upload.local.rqrmntdir}")
    String localdir;

    @Value("${scripts.path}")
    private String scripts_path;

    @Value("${release.rqrmnt.minio.url}")
    private String minioUrl;// /release-rqrmnt-sit/

    @Autowired
    private IReleaseTaskDao releaseTaskDao;

    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String task_id = requestParam.get(Dict.TASK_ID);
        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntService.addOrEditRqrmntTask(task_id, release_node_name);
        return JsonResultUtil.buildSuccess(releaseRqrmnt);
    }

    @PostMapping(value = "/delete")
    public JsonResult delete(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String task_id = requestParam.get(Dict.TASK_ID);
        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntService.deleteRqrmntTask(task_id, release_node_name);
        return JsonResultUtil.buildSuccess(releaseRqrmnt);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.TASK_ID, Dict.TYPE})
    @PostMapping(value = "/taskChangeNotise")
    public JsonResult taskChangeNotise(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String task_id = requestParam.get(Dict.TASK_ID);
        String type = requestParam.get(Dict.TYPE);
        if ("1".equals(type)) {// 添加或修改任务
            releaseRqrmntService.addOrEditRqrmntTask(task_id, release_node_name);
        } else if ("2".equals(type)) {//任务废弃
            releaseRqrmntService.deleteRqrmntTask(task_id, release_node_name);
            // 任务废弃,若此任务对应的应用在此投产窗口下只有此任务,则删除此投产窗口下的投产应用与变更应用
            releaseTaskService.deleteReleaseProdApplication(task_id, release_node_name);
        } else {
            logger.error("taskChangeNotise未知参数:type={},不做处理", type);
        }
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "投产需求文件上传")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public JsonResult upload(@RequestParam(Dict.RELEASE_NODE_NAME) String release_node_name,
                             @RequestParam(Dict.FILES) MultipartFile[] files) throws Exception {
        // 投产窗口名称不能为空
        if (CommonUtils.isNullOrEmpty(release_node_name)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.RELEASE_NODE_NAME});
        }
        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntService.queryByRqrmntId(release_node_name, null,
                Constants.RELEASE_RQRMNT_FILE);
        String dirUrl = new StringBuilder(minioUrl).append(release_node_name).toString();
        if (!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
            List<Map<String, String>> list = releaseRqrmnt.getRqrmnt_file();
            boolean flag = processFileList(files, list, dirUrl);
            if (flag) {
                releaseRqrmnt.setRqrmnt_file(list);
                releaseRqrmntService.editRqrmntFile(releaseRqrmnt);
            }
        } else {
            List<Map<String, String>> list = new ArrayList<>();
            processFileList(files, list, dirUrl);
            ObjectId objectId = new ObjectId();
            releaseRqrmnt = new ReleaseRqrmnt(objectId, objectId.toString(), release_node_name,
                    Constants.RELEASE_RQRMNT_FILE, list,CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
            releaseRqrmntService.saveReleaseRqrmnt(releaseRqrmnt);
        }
        return JsonResultUtil.buildSuccess(releaseRqrmnt);
    }

    private boolean processFileList(MultipartFile[] files, List<Map<String, String>> list,
                                    String dirUrl) throws Exception {
        boolean flag = false;
        List<String> fileNameList = new ArrayList<>();
        for (Map<String, String> map : list) {
            fileNameList.add(map.get(Dict.NAME));
        }
        for (MultipartFile file : files) {
            String path = new StringBuilder(dirUrl).append("/").append(file.getOriginalFilename()).toString();
            fileService.uploadFiles(path, file, file.getOriginalFilename(), "fdev-release");
            if (!fileNameList.contains(file.getOriginalFilename())) {
                Map<String, String> map = new HashMap<>();
                map.put(Dict.ID, new ObjectId().toString());
                map.put(Dict.NAME, file.getOriginalFilename());
                map.put(Dict.PATH, path);
                list.add(map);
                flag = true;
            }
        }
        return flag;
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/queryByReleaseNodeName")
    public JsonResult queryByReleaseNodeName(@RequestBody @ApiParam Map<String, String> requestParam) {
        List<ReleaseRqrmnt> list = releaseRqrmntService.queryByReleaseNodeName(requestParam.get(Dict.RELEASE_NODE_NAME));
        List<Map<String, String>> rqrmnt_file_list = new ArrayList<>();
        List<ReleaseRqrmnt> businessRqrmntList = new ArrayList<>();
        List<ReleaseRqrmnt> techRqrmntList = new ArrayList<>();
        for (ReleaseRqrmnt releaseRqrmnt : list) {
            //把业务需求和科技需求分开显示
            if (!Constants.RELEASE_RQRMNT_FILE.equals(releaseRqrmnt.getType())) {
                releaseRqrmnt = releaseRqrmntService.buildRqrmnt(releaseRqrmnt);
                if(!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
                    if (Constants.RELEASE_RQRMNT_TASK_FILE.equals(releaseRqrmnt.getType())) {
                        techRqrmntList.add(releaseRqrmnt);
                    } else if (Constants.BUSINESS_RQRMNT.equals(releaseRqrmnt.getType())) {
                        businessRqrmntList.add(releaseRqrmnt);
                    }
                }
            } else {
                for (Map<String, String> file : releaseRqrmnt.getRqrmnt_file()) {
                    file.put(Dict.TYPE, Constants.RELEASE_RQRMNT_FILE);
                    rqrmnt_file_list.add(file);
                }
            }
        }
        Map<String, List> map = new HashMap<>();
        map.put(Dict.BUSINESS_RQRMNT_LIST, businessRqrmntList);
        map.put(Dict.TECH_RQRMNT_LIST, techRqrmntList);
        map.put(Dict.RQRMNT_FILE_LIST, rqrmnt_file_list);
        return JsonResultUtil.buildSuccess(map);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.ID})
    @PostMapping(value = "/deleteFile")
    public JsonResult deleteFile(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String id = requestParam.get(Dict.ID);
        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntService.queryByRqrmntId(release_node_name, null,
                Constants.RELEASE_RQRMNT_FILE);
        if (!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
            Iterator iterator = releaseRqrmnt.getRqrmnt_file().iterator();
            while (iterator.hasNext()) {
                Map<String, String> file = (Map<String, String>) iterator.next();
                if (id.equals(file.get(Dict.ID))) {
                    try {
                        fileService.deleteFiles(file.get(Dict.PATH), "fdev-release");
                    } catch (Exception e) {
                        logger.info("{}文件删除异常", file.get(Dict.PATH));
                        throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
                    }
                    iterator.remove();

                }
            }
            releaseRqrmntService.editRqrmntFile(releaseRqrmnt);
        }
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.FILES})
    @PostMapping(value = "/download")
    public void download(@RequestBody @ApiParam Map<String, Object> requestParam, HttpServletResponse response) throws Exception {
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
        List<Map<String, String>> file_list = (List<Map<String, String>>) requestParam.get(Dict.FILES);
        String local_root_path = new StringBuilder(localdir).append("download/")
                .append(CommonUtils.getSessionUser().getUser_name_en()).append("_")
                .append(System.currentTimeMillis()).append("/").toString();
        String release_node_path = local_root_path + release_node_name + "/";
        CommonUtils.createDirectory(release_node_path);
        for (Map<String, String> file : file_list) {
            String type = file.get(Dict.TYPE);
            String name = file.get(Dict.NAME);
            String path = file.get(Dict.PATH);
            String rqrmnt_name = file.get(Dict.RQRMNT_NAME_L);
            String task_name = file.get(Dict.TASK_NAME);
            if (Constants.BUSINESS_RQRMNT.equals(type) || Constants.RELEASE_RQRMNT_TASK_FILE.equals(type)) {//任务下载
                //判断需求的类型
                String rqrmntType = "";
                if (Constants.RELEASE_RQRMNT_TASK_FILE.equals(type)) {
                    rqrmntType = Dict.TECH_RQRMNT;
                } else if (Constants.BUSINESS_RQRMNT.equals(type)) {
                    rqrmntType = Dict.BUSINESS_RQRMNT;
                }
                String rqrmnt_directory = release_node_path + rqrmntType + "/" + rqrmnt_name + "/" + task_name + "/";
                // 创建文件夹
                CommonUtils.createDirectory(rqrmnt_directory);
                // 下载文件到本地
                fileService.downloadDocumentFile(rqrmnt_directory + name, path, Constants.TASK_MODULE);
            } else if (Constants.RELEASE_RQRMNT_FILE.equals(type)) {//投产下载
                //下载到本地
                fileService.downloadDocumentFile(release_node_path + name, path, "fdev-release");
            }
        }
        String zip_name = local_root_path + release_node_name + ".zip";
        // 将投产窗口的文件夹压缩成zip
        CommonUtils.pressToZip(release_node_path, zip_name);
        // 下载压缩包
        downloadZip(response, zip_name);

        // 删除文件夹
        if (CommonUtils.deleteDirectory(new File(local_root_path), local_root_path)) {
            logger.info("本地目录{}已删除", local_root_path);
        }
    }

    private void downloadZip(HttpServletResponse response, String path) throws IOException {
        File zip = new File(path);
        InputStream in=null;
        FileInputStream fileInputStream = null;
        byte[] buffer;
        try {
            fileInputStream = new FileInputStream(zip);
            in = new BufferedInputStream(fileInputStream);
            buffer = new byte[in.available()];
            in.read(buffer);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
            }catch (Exception ex) {
                logger.error("关闭流异常");
            }
        }
        response.reset();
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(zip.getName().getBytes()));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Length", String.valueOf(zip.length()));
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        try(OutputStream os = new BufferedOutputStream(response.getOutputStream());){
            os.write(buffer);
            os.flush();
//            os.close();
        }

    }

    @OperateRecord(operateDiscribe = "投产模块-查询投产材料详情")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.IDS, Dict.QUESTION_NO})
    @PostMapping(value = "/querySourceReviewDetail")
    public JsonResult querySourceReviewDetail(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        //1. 获取前端上送的参数
        List<String> systemIds = (List<String>) requestParam.get(Dict.IDS); //系统id列表
        List<String> systemNames = new ArrayList<>();
        for(String systemId : systemIds) {
            systemNames.add(taskService.querySystemName(systemId));
        }
        String sysNameCn = String.join("、", systemNames);
        String question_no = (String) requestParam.get(Dict.QUESTION_NO);//问题单号

        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);//投产点名称
        ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);//根据投产点名称查询产投点详情
        // 根据上送的需求id--》所有任务--》关联的应用
        List<String> rqrmnt_ids = (List<String>) requestParam.get("rqrmnt_ids");//需求id列表
        List<String> applicationIds = new ArrayList<>();
        List<ReleaseRqrmnt> list = releaseRqrmntService.queryByReleaseNodeName(release_node_name);
        Set<String> allTaskIds = new HashSet<>();
        for (ReleaseRqrmnt releaseRqrmnt : list) {
            if(rqrmnt_ids.contains(releaseRqrmnt.getRqrmnt_id())){
                Set<String> taskIds = releaseRqrmnt.getTask_map().keySet();
                allTaskIds.addAll(taskIds);
            }
        }
        Map<String,String> req = new HashMap<>();
        req.put(Dict.RELEASE_NODE_NAME, release_node_name);
        List<ReleaseTask> releaseTasks = releaseTaskDao.queryTasks(req);
        for (ReleaseTask task: releaseTasks){
            if(allTaskIds.contains(task.getTask_id())){
                applicationIds.add(task.getApplication_id());
            }
        }
        //根据投产点名称和问题单号以及系统英文名查询所属应用
        List<Map<String, Object>> filepath = releaseApplicationService.queryApplicationsByReleaseNodeName(release_node_name, question_no, systemIds, applicationIds);
//        List<String> ids = releaseTaskService.releaseTaskByNodeName(Arrays.asList(release_node_name));//任务特定日期所有投产窗口下任务列表的id集合
//        Map<String, Object> allTaskMap = taskService.queryTasksByIds(new HashSet<>(allTaskIds));//通过id列表查询task信息
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.SYSNAME_CN, sysNameCn);//系统中文名
        map.put(Dict.RELEASE_DATE, releaseNode.getRelease_date());//投产日期
        String spdbMastaer = (String) userService.queryUserInfo(releaseNode.getRelease_manager()).get(Dict.USER_NAME_CN);
        Map<String,Object> spdbMastaer_map = new HashMap<>();
        spdbMastaer_map.put(Dict.USER_NAME_EN, releaseNode.getRelease_manager());
        spdbMastaer_map.put(Dict.USER_NAME_CN, spdbMastaer);
        // 投产窗口的行内负责人 修改成 投产窗口投产负责人 release_manager（dao）  --代码审查负责人
        map.put(Dict.SPDB_MASTER, spdbMastaer_map);
        // 投产任务的行内负责人 修改成 投产窗口科技负责人release_spdb_manager(dao)  --代码审查人
//        Map<String,Object> spdbManager_map = findManageByTask(allTaskMap, new ArrayList<>(allTaskIds), spdbMastaer);
        String spdbManager = (String) userService.queryUserInfo(releaseNode.getRelease_spdb_manager()).get(Dict.USER_NAME_CN);
        Map<String,Object> spdbManager_map = new HashMap<>();
        spdbManager_map.put(Dict.USER_NAME_EN, releaseNode.getRelease_spdb_manager());
        spdbManager_map.put(Dict.USER_NAME_CN, spdbManager);
        map.put(Dict.SPDB_MANAGERS, spdbManager_map);
        map.put(Dict.DATE, CommonUtils.formatDate("yyyy-MM-dd"));//审查日期
        map.put(Dict.LIST, filepath);
        return JsonResultUtil.buildSuccess(map);
    }

    @OperateRecord(operateDiscribe = "投产模块-生成投产材料")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME })
    @PostMapping(value = "/addSourceReview")
    public JsonResult addSourceReview(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String risk_flag = "0";
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);//投产点名称
        String sysNameCn = (String) requestParam.get(Dict.SYSNAME_CN);
        Map<String, String> map = new HashMap<>();
        map.put(Dict.SYSNAME_CN, sysNameCn);//系统中文名
        map.put(Dict.RELEASE_DATE, (String) requestParam.get(Dict.RELEASE_DATE));//投产日期
        // 投产窗口的行内负责人  --代码审查负责人
        Map<String,Object> spdb_master_map = (Map<String, Object>) requestParam.get(Dict.SPDB_MASTER);
        map.put(Dict.SPDB_MASTER, (String) spdb_master_map.get(Dict.USER_NAME_CN));
        // 投产任务的行内负责人  --代码审查人
        Map<String,Object> spdb_managers_map = (Map<String, Object>) requestParam.get(Dict.SPDB_MANAGERS);
        map.put(Dict.SPDB_MANAGERS, (String)spdb_managers_map.get(Dict.USER_NAME_CN));
        // 校验审查人、审查负责人、申请人是否已填且不相同，如不满足，正常生成文件但是提示风险。
        String spdb_master = (String) spdb_master_map.get(Dict.USER_NAME_EN);
        String spdb_managers = (String)spdb_managers_map.get(Dict.USER_NAME_EN);
        if(spdb_master.equals(spdb_managers)){
            risk_flag = "1";
        }
        map.put(Dict.DATE, (String) requestParam.get(Dict.DATE));//日期
        List<Map<String, Object>> filepath = (List<Map<String, Object>>) requestParam.get(Dict.LIST);
        Set<String> userEnList = new HashSet<>();
        String question_no = (String) requestParam.get(Dict.QUESTION_NO);
        if(!CommonUtils.isNullOrEmpty(filepath)){
            for(Map<String,Object> detail : filepath){
                Map<String,Object> user = (Map<String, Object>) detail.get(Dict.NAME);
                userEnList.add((String) user.get(Dict.NAME_EN));
            }
            if(userEnList.contains(spdb_master) || userEnList.contains(spdb_managers)){
                risk_flag = "1";
            }
        }

        //上传文件
        String local_path = localdir + release_node_name;
        String fileName = "【" + release_node_name + "】" + sysNameCn + "_源代码审查登记表(" + question_no + ").docx";
        CommonUtils.createDirectory(local_path);
        writeToWord(local_path + "/" + fileName, filepath, map);
        String path = new StringBuilder(minioUrl).append(release_node_name).append("/").append(fileName).toString();
        fileService.uploadWord(path, new File(local_path + "/" + fileName), "fdev-release");
        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntService.queryByRqrmntId(release_node_name, null,
                Constants.RELEASE_RQRMNT_FILE);
        if (!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
            boolean flag = true;
            for (Map<String, String> file : releaseRqrmnt.getRqrmnt_file()) {
                if (fileName.equals(file.get(Dict.NAME))) {
                    flag = false;
                    file.put("risk_flag", risk_flag); // 有同名文件，需修改该同名文件的风险提示
                }
            }
            if (flag) { //没有与库中同名文件直接添加该文件
                List<Map<String, String>> list = releaseRqrmnt.getRqrmnt_file();
                saveRqrmntFile(list, path, fileName, risk_flag);
                releaseRqrmnt.setRqrmnt_file(list);
            }
            releaseRqrmntService.editRqrmntFile(releaseRqrmnt);
        } else {
            List<Map<String, String>> list = new ArrayList<>();
            saveRqrmntFile(list, path, fileName,risk_flag);
            ObjectId objectId = new ObjectId();
            releaseRqrmnt = new ReleaseRqrmnt(objectId, objectId.toString(), release_node_name,
                    Constants.RELEASE_RQRMNT_FILE, list);
            releaseRqrmntService.saveReleaseRqrmnt(releaseRqrmnt);
        }
        return JsonResultUtil.buildSuccess();
    }

    /*@OperateRecord(operateDiscribe = "投产模块-生成投产材料")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.IDS, Dict.QUESTION_NO})
    @PostMapping(value = "/addSourceReview_bak")
    public JsonResult addSourceReview_bak(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        List<String> systemIds = (List<String>) requestParam.get(Dict.IDS);
        List<String> systemNames = new ArrayList<>();
        for(String systemId : systemIds) {
            systemNames.add(taskService.querySystemName(systemId));
        }
        String sysNameCn = String.join("、", systemNames);
        String question_no = (String) requestParam.get(Dict.QUESTION_NO);//问题单号
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);//投产点名称
        ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);//根据投产点名称查询投产点详情
        // 根据上送的需求id--》所有任务--》关联的应用
        List<String> rqrmnt_ids = (List<String>) requestParam.get("rqrmnt_ids");//需求id列表
        List<String> appIds = new ArrayList<>();
        List<ReleaseRqrmnt> RqrmntList = releaseRqrmntService.queryByReleaseNodeName(release_node_name);
        Set<String> allTaskIds = new HashSet<>();
        for (ReleaseRqrmnt releaseRqrmnt : RqrmntList) {
            if(rqrmnt_ids.contains(releaseRqrmnt.getRqrmnt_id())){
                Set<String> taskIds = releaseRqrmnt.getTask_map().keySet();
                allTaskIds.addAll(taskIds);
            }
        }
        Map<String,String> req = new HashMap<>();
        req.put(Dict.RELEASE_NODE_NAME, release_node_name);
        List<ReleaseTask> releaseTasks = releaseTaskDao.queryTasks(req);
        for (ReleaseTask task: releaseTasks){
            if(allTaskIds.contains(task.getTask_id())){
                appIds.add(task.getApplication_id());
            }
        }
        //根据投产点名称和问题单号以及系统英文名查询所属应用
        List<Map<String, String>> filepath = releaseApplicationService.queryApplicationsByReleaseNodeName(release_node_name, question_no, systemIds, appIds);
        List<String> ids = releaseTaskService.releaseTaskByNodeName(Arrays.asList(release_node_name));//任务特定日期所有投产窗口下任务列表的id集合
        Map<String, Object> allTaskMap = taskService.queryTasksByIds(new HashSet<>(ids));//通过id列表查询task信息
        Map<String, String> map = new HashMap<>();
        map.put(Dict.SYSNAME_CN, sysNameCn);//系统中文名
        map.put(Dict.RELEASE_DATE, releaseNode.getRelease_date());//投产日期
        String spdbMastaer = (String) userService.queryUserInfo(releaseNode.getRelease_spdb_manager()).get(Dict.USER_NAME_CN);
        // 投产窗口的行内负责人  --代码审查负责人
        map.put(Dict.SPDB_MASTER, spdbMastaer);
        // 投产任务的行内负责人  --代码审查人
        String spdbManager = findManageByTask(allTaskMap, ids, spdbMastaer);
        map.put(Dict.SPDB_MANAGERS, spdbManager);
        map.put(Dict.DATE, CommonUtils.formatDate("yyyy-MM-dd"));//日期
        //上传文件
        String local_path = localdir + release_node_name;
        String fileName = sysNameCn + "_源代码审查登记表.docx";
        CommonUtils.createDirectory(local_path);
        writeToWord(local_path + "/" + fileName, filepath, map);
        String path = new StringBuilder(minioUrl).append(release_node_name).append("/").append(fileName).toString();
        fileService.uploadWord(path, new File(local_path + "/" + fileName), "fdev-release");

        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntService.queryByRqrmntId(release_node_name, null,
                Constants.RELEASE_RQRMNT_FILE);
        if (!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
            boolean flag = true;
            for (Map<String, String> file : releaseRqrmnt.getRqrmnt_file()) {
                if (fileName.equals(file.get(Dict.NAME))) {
                    flag = false;
                }
            }
            if (flag) {
                List<Map<String, String>> list = releaseRqrmnt.getRqrmnt_file();
                saveRqrmntFile(list, path, fileName);
                releaseRqrmnt.setRqrmnt_file(list);
                releaseRqrmntService.editRqrmntFile(releaseRqrmnt);
            }
        } else {
            List<Map<String, String>> list = new ArrayList<>();
            saveRqrmntFile(list, path, fileName);
            ObjectId objectId = new ObjectId();
            releaseRqrmnt = new ReleaseRqrmnt(objectId, objectId.toString(), release_node_name,
                    Constants.RELEASE_RQRMNT_FILE, list);
            releaseRqrmntService.saveReleaseRqrmnt(releaseRqrmnt);
        }
        return JsonResultUtil.buildSuccess();
    }*/

    private Map<String,Object> findManageByTask(Map<String, Object> allTaskMap, List<String> ids, String spdbMaster) {
        String manager = "";
        Map<String,Object> manager_map = new HashMap<>();
        taskManager:for(String id : ids) {
            Map<String, Object> task = (Map<String, Object>) allTaskMap.get(id);
            List<Map<String, Object>> spdbManagers = (List<Map<String, Object>>) task.get(Dict.SPDB_MASTER);
            for(Map<String, Object> map : spdbManagers) {
                manager = (String) map.get(Dict.USER_NAME_CN);
                if(!spdbMaster.equals(manager)) {
                    manager_map.putAll(map);
                    break taskManager;
                }
            }
        }
        return manager_map;
    }


    private void saveRqrmntFile(List<Map<String, String>> list, String path, String fileName,String riskFlag) {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.ID, new ObjectId().toString());
        map.put(Dict.NAME, fileName);
        map.put(Dict.PATH, path);
        map.put("risk_flag", riskFlag);
        list.add(map);
    }

    private void writeToWord(String filePath, List<Map<String, Object>> contants, Map<String, String> requestParam) throws IOException {
        try (
            FileInputStream fis = new FileInputStream(new File(scripts_path + "word_templates/源代码审查登记表.docx"));
            OutputStream os = new FileOutputStream(filePath);
            XWPFDocument document = new XWPFDocument(fis);
        ) {
            replaceInPara(document, requestParam, true, 18, "");
            XWPFTable table = document.getTables().get(0);
            for (int i = 1; i < contants.size(); i++) {
                table.addRow(table.getRow(table.getNumberOfRows() - 1));
            }
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            document.write(os);
//            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        FileInputStream fiss = null;
        ZipSecureFile.setMinInflateRatio(-1.0d);
        OutputStream oss = null;
        XWPFDocument document1=null;
        try {
            fiss = new FileInputStream(new File(filePath));
            document1 = new XWPFDocument(fiss);
            XWPFTable tabless = document1.getTables().get(0);
            int j = 1;
            for (Map<String, Object> map : contants) {
                XWPFTableRow row = tabless.getRow(j);
                row.getCell(0).setText((String) map.get(Dict.QUESTION_NO));
                Map<String,Object> name_map = (Map<String, Object>) map.get(Dict.NAME);
                row.getCell(1).setText(CommonUtils.isNullOrEmpty(name_map)? "": (String) name_map.get(Dict.NAME_CN));
                row.getCell(2).setText((String) map.get(Dict.PATH));
                j++;
            }

            oss = new FileOutputStream(filePath);
            document1.write(oss);
        } finally {
            if(document1!=null){
                document1.close();
            }
            if(fiss!=null){
                fiss.close();
            }
            if (oss != null) {
                oss.close();
            }
        }
    }

    private void replaceInPara(XWPFDocument document, Map<String, String> requestParam, boolean bold, int fontSize, String fontFamily) {
        Iterator<XWPFParagraph> iterator = document.getParagraphsIterator();
        while (iterator.hasNext()) {
            replaceInPara(iterator.next(), requestParam, bold, fontSize, fontFamily);
        }
    }

    private void replaceInPara(XWPFParagraph paragraph, Map<String, String> requestParam, boolean bold, int fontSize, String fontFamily) {
        if (matcher(paragraph.getParagraphText()).find()) {
            List<XWPFRun> runs = paragraph.getRuns();
            if (runs.size() > 0) {
                for (int i = 0; i < runs.size(); i++) {
                    for (String key : requestParam.keySet()) {
                        if (runs.get(i).toString().contains(key)) {
                            if (runs.get(i).toString().equals(key)) {
                                if ("{".equals(runs.get(i - 1).toString())) {
                                    i = i - 2;
                                } else {
                                    i = i - 1;
                                }
                                // 删除${}
                                paragraph.removeRun(i);
                                paragraph.removeRun(i);
                                paragraph.removeRun(i);
                                paragraph.removeRun(i);
                                // 重新写入内容
                                XWPFRun run = paragraph.insertNewRun(i);
                                run.setText(requestParam.get(key));
                                run.setBold(bold);
                                run.setFontSize(fontSize);
                                if (!CommonUtils.isNullOrEmpty(fontFamily)) {
                                    run.setFontFamily(fontFamily);
                                }
                            } else if (runs.get(i).toString().equals("${" + key + "}")) {
                                paragraph.removeRun(i);
                                // 重新写入内容
                                XWPFRun run = paragraph.insertNewRun(i);
                                run.setText(requestParam.get(key));
                                run.setBold(bold);
                                run.setFontSize(fontSize);
                                if (!CommonUtils.isNullOrEmpty(fontFamily)) {
                                    run.setFontFamily(fontFamily);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    @OperateRecord(operateDiscribe = "投产模块-生成投产材料")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.IDS})
    @PostMapping(value = "/addSystemTestFile")
    public JsonResult addSystemTestFile(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
        List<String> systemIds = (List<String>) requestParam.get(Dict.IDS);
        ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);
        String manager = (String) userService.queryUserInfo(releaseNode.getRelease_manager()).get(Dict.USER_NAME_CN);
        String spdbManager = (String) userService.queryUserInfo(releaseNode.getRelease_spdb_manager()).get(Dict.USER_NAME_CN);
        List<String> ids = releaseTaskService.findReleaseTaskByReleaseNodeName(release_node_name);
        //Map<String, Object> allTaskMap = taskService.queryTasksByIds(new HashSet<>(ids));
        List<Map<String, Object>> testcase_list = new ArrayList<>();
        List<Map<String, Object>> task_list = new ArrayList<>();
        for (String task_id : ids) {
            Map<String, Object> task_map = taskService.queryTaskDetail(task_id);
            //Map<String, Object> task_map = (Map<String, Object>) allTaskMap.get(task_id);
            if (!CommonUtils.isNullOrEmpty(task_map)) {
                String application_id = (String) task_map.get(Dict.PROJECT_ID);
                Map<String, Object> application = appService.queryAPPbyid(application_id);
                if(systemIds.contains(((Map<String, String>)application.get(Dict.SYSTEM)).get(Dict.ID))) {
                    task_map.put(Dict.SYSNAME_CN, ((Map<String, String>)application.get(Dict.SYSTEM)).get(Dict.NAME));
                    String rqrmnt_no = (String) task_map.get(Dict.RQRMNT_NO);
                    Map<String, Object> rqrmnt_detail = releaseRqrmntService.queryRqrmntsInfo(rqrmnt_no);
                    // 需求编号
                    String rqrmnt_num = (String) rqrmnt_detail.get(Dict.OA_CONTACT_NO);
                    task_map.put(Dict.RQRMNT_NUM_U, rqrmnt_num);
                    task_list.add(task_map);
                    if("1".equals(task_map.get("isTest"))){
                        Map<String, Object> testCases = taskService.queryTestcaseByTaskId(task_id);
                        if (!CommonUtils.isNullOrEmpty(testCases)) {
                            List<Map<String, Object>> testCaseList = (List<Map<String, Object>>) testCases.get("testcases");
                            if(testCaseList.size() > 50) {
                                testcase_list.addAll(testCaseList.stream().skip(0L).limit(50L).collect(Collectors.toList()));
                            } else {
                                testcase_list.addAll(testCaseList);
                            }
                        }
                    }
                }
            }
        }
        List<Map<String, Integer>> tablelist = new ArrayList<>();
        Map<String, Integer> table_task = new HashMap<>();
        table_task.put("table_no", 5);
        table_task.put("table_num", task_list.size());
        tablelist.add(table_task);

        Map<String, Integer> table_testcase = new HashMap<>();
        table_testcase.put("table_no", 6);
        table_testcase.put("table_num", testcase_list.size());
        tablelist.add(table_testcase);
        String local_path = localdir + release_node_name;
        String fileName = "系统测试报告.docx";
        String target_file = local_path + "/" + fileName;
        CommonUtils.createDirectory(local_path);
        Map<String, String> task_num = new HashMap<>();
        String owner_group_name = userService.queryGroupNameById(releaseNode.getOwner_groupId());
        task_num.put("group", owner_group_name.indexOf("-") != -1
                ? owner_group_name.split("-")[1] : owner_group_name);
        task_num.put("release_node", release_node_name.split("_")[0]);
        task_num.put("yyyyMM", CommonUtils.formatDate("yyyy年MM月"));
        task_num.put("task", String.valueOf(task_list.size()));
        task_num.put("testcase", String.valueOf(testcase_list.size()));

        //上传文件
        // 扩展table
        table(target_file, tablelist, task_num);
        String today = CommonUtils.formatDate("yyyy-MM-dd");
        // 填写每个表格
        editTable1(target_file, today, manager, spdbManager);
        editTable2(target_file, today);
        editTable3(target_file, manager);
        editTable5(target_file, task_list);
        editTable6(target_file, testcase_list);
        editTable7(target_file, today);
        editTable8(target_file, testcase_list.size());
        String path = new StringBuilder(minioUrl).append(release_node_name).append("/").append(fileName).toString();
        fileService.uploadWord(path, new File(target_file), "fdev-release");

        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntService.queryByRqrmntId(release_node_name, null,
                Constants.RELEASE_RQRMNT_FILE);

        if (!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
            boolean flag = true;
            for (Map<String, String> file : releaseRqrmnt.getRqrmnt_file()) {
                if (fileName.equals(file.get(Dict.NAME))) {
                    flag = false;
                }
            }
            if (flag) {
                List<Map<String, String>> list = releaseRqrmnt.getRqrmnt_file();
                saveRqrmntFile(list, path, fileName, "");
                releaseRqrmnt.setRqrmnt_file(list);
                releaseRqrmntService.editRqrmntFile(releaseRqrmnt);
            }

        } else {
            List<Map<String, String>> list = new ArrayList<>();
            saveRqrmntFile(list, path, fileName, "");
            ObjectId objectId = new ObjectId();
            releaseRqrmnt = new ReleaseRqrmnt(objectId, objectId.toString(), release_node_name,
                    Constants.RELEASE_RQRMNT_FILE, list);
            releaseRqrmntService.saveReleaseRqrmnt(releaseRqrmnt);
        }

        return JsonResultUtil.buildSuccess();
    }

    private void editTable1(String filePath, String time, String manager, String spsbManager) throws IOException {
        FileInputStream fis = null;
        ZipSecureFile.setMinInflateRatio(-1.0d);
        OutputStream os = null;
        XWPFDocument document=null;
        try {
            fis = new FileInputStream(new File(filePath));
            document = new XWPFDocument(fis);
            XWPFTable tabless = document.getTables().get(1);
            XWPFTableRow row = tabless.getRow(1);
            row.getCell(3).setText(time);
            row.getCell(4).setText(manager);
            row.getCell(5).setText(spsbManager);
            os = new FileOutputStream(filePath);
            document.write(os);
//            os.close();
        } finally {
            if(document!=null){
                document.close();
            }
            if(fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private void editTable2(String filePath, String time) throws IOException {
        FileInputStream fis = null;
        XWPFDocument document=null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new File(filePath));
            document = new XWPFDocument(fis);
            XWPFTable tabless = document.getTables().get(2);
            for (int i = 1; i < 6; i++) {
                XWPFTableRow row = tabless.getRow(i);
                row.getCell(1).setText(time);
                row.getCell(2).setText(time);
            }
            os = new FileOutputStream(filePath);
            document.write(os);
        } finally {
            if(document!=null){
                document.close();
            }
            if(fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private void editTable3(String filePath, String manager) throws IOException {
        FileInputStream fis = null;
        XWPFDocument document =null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new File(filePath));
            document = new XWPFDocument(fis);
            XWPFTable tabless = document.getTables().get(3);
            for (int i = 1; i < 6; i++) {
                XWPFTableRow row = tabless.getRow(i);
                row.getCell(2).setText(manager);
            }
            os = new FileOutputStream(filePath);
            document.write(os);
        } finally {
            if(document!=null){
                document.close();
            }
            if(fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private void editTable5(String filePath, List<Map<String, Object>> contants) throws IOException {
        FileInputStream fis = null;
        XWPFDocument document=null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new File(filePath));
            document = new XWPFDocument(fis);
            XWPFTable tabless = document.getTables().get(5);
            int j = 1;
            for (Map<String, Object> map : contants) {
                XWPFTableRow row = tabless.getRow(j);
                row.getCell(0).setText(String.valueOf(j));
                row.getCell(1).setText((String) map.get(Dict.SYSNAME_CN));
                row.getCell(2).setText(map.get(Dict.RQRMNT_NUM_U) + "_" + map.get(Dict.NAME));
                j++;
            }
            os = new FileOutputStream(filePath);
            document.write(os);
        } finally {
            if(document!=null){
                document.close();
            }
            if(fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private void editTable6(String filePath, List<Map<String, Object>> contants) throws IOException {
        FileInputStream fis = null;
        XWPFDocument document=null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new File(filePath));
            document = new XWPFDocument(fis);
            XWPFTable tabless = document.getTables().get(6);
            int j = 2;
            for (Map<String, Object> map : contants) {
                XWPFTableRow row = tabless.getRow(j);
                row.getCell(0).setText(String.valueOf(j - 1));
                row.getCell(1).setText((String) map.get("testcaseName"));
                row.getCell(2).setText((String) map.get("expectedResult"));
                j++;
            }

            os = new FileOutputStream(filePath);
            document.write(os);
        } finally {
            if(document!=null){
                document.close();
            }
            if(fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private void editTable7(String filePath, String time) throws IOException {
        FileInputStream fis = null;
        XWPFDocument document=null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new File(filePath));
            document = new XWPFDocument(fis);
            XWPFTable tabless = document.getTables().get(7);
            XWPFTableRow row = tabless.getRow(2);
            row.getCell(0).setText(time);
            row.getCell(1).setText(time);
            row.getCell(2).setText(time);
            row.getCell(3).setText(time);
            os = new FileOutputStream(filePath);
            document.write(os);
        } finally {
            if(document!=null){
                document.close();
            }
            if(fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private void editTable8(String filePath, int size) throws IOException {
        FileInputStream fis = null;
        XWPFDocument document=null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new File(filePath));
            document = new XWPFDocument(fis);
            XWPFTable tabless = document.getTables().get(8);
            XWPFTableRow row = tabless.getRow(0);
            row.getCell(3).setText(String.valueOf(size));
            XWPFTableRow row1 = tabless.getRow(1);
            row1.getCell(3).setText(String.valueOf(size));
            os = new FileOutputStream(filePath);
            document.write(os);
        } finally {
            if(document!=null){
                document.close();
            }
            if (fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    // 扩展表格
    private void table(String filePath, List<Map<String, Integer>> tablelist, Map<String, String> task_num) throws IOException {
        FileInputStream fis = null;
        XWPFDocument document=null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new File(scripts_path+"word_templates/系统测试报告.docx"));
            document = new XWPFDocument(fis);
            replaceInPara(document.getParagraphArray(14), task_num, true, 22, "");
            replaceInPara(document.getParagraphArray(26), task_num, false, 12, "");
            replaceInPara(document.getParagraphArray(47), task_num, false, 12, "");
            replaceInPara(document.getParagraphArray(59), task_num, true, 12, "");
            replaceInPara(document.getParagraphArray(66), task_num, false, 15, "");
            replaceInPara(document.getParagraphArray(67), task_num, false, 15, "");
            for (Map<String, Integer> map : tablelist) {
                XWPFTable table = document.getTables().get(map.get("table_no"));
                for (int i = 1; i < map.get("table_num"); i++) {
                    table.addRow(table.getRow(table.getNumberOfRows() - 1));
                }
            }

            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            os = new FileOutputStream(filePath);
            document.write(os);
        } finally {
            if(document!=null){
                document.close();
            }
            if(fis!=null){
                fis.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    @PostMapping(value = "/updateRqrmntInfoReview")
    public JsonResult updateRqrmntInfoReview(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String task_id = (String)requestParam.get(Dict.TASK_ID);
        releaseRqrmntInfoService.updateRqrmntInfoReview(task_id);
        releaseRqrmntService.changeRqrmntMsg(task_id);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/deleteRqrmntInfoTask")
    public JsonResult deleteRqrmntInfoTask(@RequestBody Map<String, Object> requestParam) throws Exception {
        String task_id = (String)requestParam.get(Dict.TASK_ID);
        //任务删除 同时删除需求列表信息
        releaseRqrmntInfoService.deleteRqrmntInfoTask(task_id);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/updateRqrmntInfo")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_DATE, Dict.GROUPIDS})
    public void updateRqrmntInfo(@RequestBody Map<String, Object> requestParam, HttpServletResponse resp) throws Exception {
        String relase_date = (String)requestParam.get(Dict.RELEASE_DATE);
        String type =  (String)requestParam.get(Dict.TYPE);
        List<String> groupIds = (List<String>)requestParam.get(Dict.GROUPIDS);
        releaseRqrmntInfoService.updateRqrmntInfo(relase_date, type, groupIds, resp);
    }

    @PostMapping(value = "/queryRqrmntInfoList")
    public JsonResult queryRqrmntInfoList(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        /**
         * 小组名  投产窗口（可能多个） 需求单号  需求状态（大窗口下首个任务的状态） 行内负责人
         * 确认书情况 整包提测 准生产提测  需规地址（需求中的需求规格说明书wps地址）
         */
        String release_date = (String)requestParam.get(Dict.RELEASE_DATE);
        String type = (String)requestParam.get(Dict.TYPE);
        List<String> groupIds = (List<String>)requestParam.get(Dict.GROUPIDS);
        boolean isParent = (boolean)requestParam.get(Dict.ISPARENT);
        List list = releaseRqrmntInfoService.queryRqrmntInfoList(release_date, type, groupIds, isParent);
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping(value = "/exportRqrmntInfoList")
    public void exportRqrmntInfoList(@RequestBody @ApiParam Map<String, Object> requestParam, HttpServletResponse resp) throws Exception {
        String release_date = (String)requestParam.get(Dict.RELEASE_DATE);
        String type = (String)requestParam.get(Dict.TYPE);
        List<String> groupIds = (List<String>)requestParam.get(Dict.GROUPIDS);
        boolean isParent = (boolean)requestParam.get(Dict.ISPARENT);
        generateExcelService.exportRqrmntInfoList(release_date, type, groupIds, isParent, resp);
    }

    @PostMapping(value = "/queryRqrmntInfoListByType")
    public JsonResult queryRqrmntInfoListByType(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String release_date = (String)requestParam.get(Dict.RELEASE_DATE);
        String type = (String)requestParam.get(Dict.TYPE);
        List<String> groupIds = (List<String>)requestParam.get(Dict.GROUPIDS);
        boolean isParent = (boolean)requestParam.get(Dict.ISPARENT);
        List list = releaseRqrmntInfoService.queryRqrmntInfoListByType(release_date, type, groupIds, isParent);
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping(value = "/exportRqrmntInfoListByType")
    public void exportRqrmntInfoListByType(@RequestBody @ApiParam Map<String, Object> requestParam, HttpServletResponse resp) throws Exception {
        String release_date = (String)requestParam.get(Dict.RELEASE_DATE);
        String type = (String)requestParam.get(Dict.TYPE);
        List<String> groupIds = (List<String>)requestParam.get(Dict.GROUPIDS);
        boolean isParent = (boolean)requestParam.get(Dict.ISPARENT);
        generateExcelService.exportRqrmntInfoListByType(release_date, type, groupIds, isParent,resp);
    }

    @PostMapping(value = "/asyncRqrmntInfoTag")
    public JsonResult asyncRqrmntInfoTag(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        releaseRqrmntInfoService.asyncRqrmntInfoTag();
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/confirmRqrmntInfoTag")
    public JsonResult confirmRqrmntInfoTag(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String confirmFileDate = (String)requestParam.get(Dict.CONFIRMFILEDATE);
        String task_id = (String)requestParam.get(Dict.TASK_ID);
        releaseRqrmntInfoService.confirmRqrmntInfoTag(confirmFileDate,task_id);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/asyncRqrmntInfoTagNotAllow")
    public JsonResult asyncRqrmntInfoTagNotAllow(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        releaseRqrmntInfoService.asyncRqrmntInfoTagNotAllow();
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/batchAddRqrmntInfo")
    public JsonResult batchAddRqrmntInfo(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        releaseRqrmntInfoService.batchAddRqrmntInfo();
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/queryRqrmntInfoTasks")
    public JsonResult queryRqrmntInfoTasks(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String id = requestParam.get(Dict.ID);
        List<Map> result = releaseRqrmntInfoService.queryRqrmntInfoTasks(id);
        return JsonResultUtil.buildSuccess(result);
    }

    @PostMapping(value = "/exportSpecialRqrmntInfoList")
    public void exportSpecialRqrmntInfoList(@RequestBody @ApiParam Map<String, Object> requestParam, HttpServletResponse resp) throws Exception {
        String release_date = (String)requestParam.get(Dict.RELEASE_DATE);
        String type = (String)requestParam.get(Dict.TYPE);
        List<String> groupIds = (List<String>)requestParam.get(Dict.GROUPIDS);
        boolean isParent = (boolean)requestParam.get(Dict.ISPARENT);
        generateExcelService.exportSpecialRqrmntInfoList(release_date, type, groupIds, isParent, resp);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/queryReleaseSystem")
    public JsonResult queryReleaseSystem(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        List<Map<String, Object>> applications = releaseApplicationService.queryApplications(release_node_name);
        Set<Map<String, String>> set = new HashSet<>();
        for(Map<String, Object> map : applications) {
            if(CommonUtils.isNullOrEmpty(map.get(Dict.SYSTEM))) {
                throw new FdevException(ErrorConstants.APPLICATION_NOT_BIND_SYSTEM, new String[]{"【" + map.get(Dict.APP_NAME_ZH) + "】"});
            }
            set.add((Map<String, String>) map.get(Dict.SYSTEM));
        }
        return JsonResultUtil.buildSuccess(set);
    }

    @PostMapping(value = "/batchDeleteRqrmntInfo")
    public JsonResult batchDeleteRqrmntInfo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        releaseRqrmntInfoService.batchDeleteRqrmntInfo();
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/batchUpdateRqrmntInfo")
    public JsonResult batchUpdateRqrmntInfo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        releaseRqrmntInfoService.batchUpdateRqrmntInfo();
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/queryTaskRqrmntAlert")
    public JsonResult queryTaskRqrmntAlert(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String task_id = requestParam.get(Dict.TASK_ID);
        String release_date = releaseRqrmntInfoService.queryTaskRqrmntAlert(task_id);
        return JsonResultUtil.buildSuccess(release_date);
    }

    @PostMapping(value = "/queryRqrmntFileUri")
    public JsonResult queryRqrmntFileUri(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String release_date = requestParam.get(Dict.RELEASE_DATE);
        return JsonResultUtil.buildSuccess(releaseRqrmntInfoService.queryRqrmntFileUri(release_date));
    }

    @PostMapping(value = "/addReleaseCycleT8")
    public JsonResult addReleaseCycleT8(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        releaseRqrmntInfoService.addReleaseCycleT8();
        return JsonResultUtil.buildSuccess(null);
    }
    @PostMapping(value = "/queryRqrmntInfoByTaskNo")
    public JsonResult queryRqrmntInfoByTaskNo(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String task_id = requestParam.get(Dict.TASK_ID);
        return JsonResultUtil.buildSuccess(releaseRqrmntInfoService.queryReleaseRqrmntInfoByTaskNo(task_id));
    }
    
    @OperateRecord(operateDiscribe = "投产模块-生成红线扫描报告")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/addRedLineScanReport")
    public JsonResult addRedLineScanReport(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        releaseRqrmntInfoService.addRedLineScanReport(requestParam);
        return JsonResultUtil.buildSuccess(null);
    }
}
