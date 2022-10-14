package com.spdb.fdev.fdemand.spdb.service.impl;


import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.DemandDocEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DemandBaseInfoUtil;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IDemandAssessDao;
import com.spdb.fdev.fdemand.spdb.dao.IDemandDocDao;
import com.spdb.fdev.fdemand.spdb.dao.IImplementUnitDao;
import com.spdb.fdev.fdemand.spdb.dao.impl.IpmpUnitDaoImpl;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class DocServiceImpl implements IDocService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private IDemandDocDao demandDocDao;

    @Autowired
    private DemandDoc demandDoc;

    @Autowired
    private DemandBaseInfo demandBaseInfo;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IDemandAssessDao demandAssessDao;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IpmpUnitDaoImpl ipmpUnitDao;

    @Autowired
    private IGitlabService gitlabService;

    @Value("${fdev.docmanage.domain}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private FdocmanageService fdocmanageService;

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Value("${fdemand.doc.folder}")
    private String docFolder;

    @Override
    public Map queryDemandDocPagination(Map<String, Object> param) {
        Integer pageSize = (Integer) param.get(Dict.SIZE);//页面大小
        Integer currentPage = (Integer) param.get(Dict.INDEX);//当前页
        if (CommonUtils.isNullOrEmpty(pageSize)) {
            pageSize = 0;
        }
        if (CommonUtils.isNullOrEmpty(currentPage)) {
            currentPage = 1;
        }
        if (CommonUtils.isNullOrEmpty(param.get(Dict.DEMAND_ID))) {
            String[] args = {Dict.DEMAND_ID};
            throw new FdevException("COMM002", args);
        }

        if (CommonUtils.isNullOrEmpty(param.get(Dict.DEMAND_DOC_TYPE))) {
            param.put(Dict.DEMAND_DOC_TYPE, "");
        }

        if (CommonUtils.isNullOrEmpty(param.get(Dict.DEMAND_KIND))) {
            param.put(Dict.DEMAND_KIND, "demand");
        }

        Integer start = pageSize * (currentPage - 1);   //起始
        List data = demandDocDao.queryDemandDocPagination(start, pageSize, param.get(Dict.DEMAND_ID).toString(), param.get(Dict.DEMAND_DOC_TYPE).toString(), param.get(Dict.DEMAND_KIND).toString());
        Long count = demandDocDao.queryCountDemandDoc(param.get(Dict.DEMAND_ID).toString(), param.get(Dict.DEMAND_DOC_TYPE).toString(), param.get(Dict.DEMAND_KIND).toString());
        Map result = new HashMap();
        result.put(Dict.DATA, data);
        result.put(Dict.COUNT, count);
        return result;
    }


    /**
     * 上传需求的doc
     *
     * @param demand_id
     * @param
     * @return
     */
    @Override
    public void uploadFile(String demand_id, String doc_type, String doc_link, String user_group_id, String user_group_cn, MultipartFile[] file, String demand_kind) throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
//        boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
//        boolean isDemandGroupLeader = roleService.isDemandGroupLeader(demand_id, user.getId());
//        if (!roleService.isDemandManager() && !isDemandLeader && !isDemandGroupLeader) {
//            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员或需求牵头人或板块涉及人员"});
//        }
        DemandDoc demandDoc = new DemandDoc();
        List<String> listPathAll = new ArrayList<String>();
        if (CommonUtils.isNullOrEmpty(doc_link)) {
            //path:环境/需求id/文档类型/文件名
            String pathCommon = docFolder + "/" + demand_id + "/" + DemandBaseInfoUtil.getDocType(doc_type) + "/";
            listPathAll = fdocmanageService.uploadFilestoMinio("fdev-demand", pathCommon, file, user);
            if (CommonUtils.isNullOrEmpty(listPathAll)) {
                throw new FdevException(ErrorConstants.DOC_ERROR_UPLOAD, new String[]{"上传文件失败,请重试!"});
            }
            //说明有文档，需要调接口上传文档。
            updateDemandDoc(user, demand_id, doc_type, listPathAll, user_group_id, user_group_cn, file, demand_kind);
        } else {
            //说明没有文档，上传的是链接
            updateDemandDocLink(user, demand_id, doc_type, doc_link, user_group_id, user_group_cn, demand_kind);
        }


    }

    /**
     * 更新需求的doc
     *
     * @param demand_id
     * @return
     */

    @Override
    public void updateDemandDoc(User user, String demand_id, String doc_type, List<String> listPathAll, String user_group_id, String user_group_cn, MultipartFile[] file, String demand_kind) throws Exception {
        DemandDoc demandDoc = new DemandDoc();
        for (String path : listPathAll) {
            demandDoc.setDemand_id(demand_id);
            demandDoc.setDoc_path(path);
            demandDoc.setDoc_type(doc_type);
            demandDoc.setUser_group_id(user_group_id);
            demandDoc.setUser_group_cn(user_group_cn);
            if (StringUtils.isEmpty(demand_kind)) {
                demandDoc.setDemand_kind("demand");
            } else {
                demandDoc.setDemand_kind(demand_kind);
            }
            String doc_name = path.substring(path.lastIndexOf("/"));
            demandDoc.setDoc_name(doc_name.substring(1));
            List<DemandDoc> demandDocList = demandDocDao.query(demandDoc);
            if (CommonUtils.isNullOrEmpty(demandDocList)) {
                demandDoc.setUpload_user(user.getId());//获取当前id
                UserInfo userInfo = new UserInfo();
                userInfo.setId(user.getId());
                userInfo.setUser_name_cn(user.getUser_name_cn());
                userInfo.setUser_name_en(user.getUser_name_en());
                demandDoc.setUpload_user_all(userInfo);
                demandDoc.setCreate_time(TimeUtil.getTimeStamp(new Date()));//设置创建时间
                demandDocDao.save(demandDoc);
            } else {
                DemandDoc demandDocBefore = demandDocList.get(0);
                demandDoc.setUpdate_time(TimeUtil.getTimeStamp(new Date()));
                demandDoc.setId(demandDocBefore.getId());
                demandDocDao.updateById(demandDoc);
            }
        }
    }

    @Override
    public void updateDemandDocLink(User user, String demand_id, String doc_type, String doc_link, String user_group_id, String user_group_cn, String demand_kind) throws Exception {
        DemandDoc demandDoc = new DemandDoc();
        demandDoc.setDemand_id(demand_id);
        demandDoc.setDoc_link(doc_link);
        demandDoc.setDoc_type(doc_type);
        demandDoc.setUser_group_id(user_group_id);
        demandDoc.setUser_group_cn(user_group_cn);
        if (StringUtils.isEmpty(demand_kind)) {
            demandDoc.setDemand_kind("demand");
        } else {
            demandDoc.setDemand_kind(demand_kind);
        }
        List<DemandDoc> demandDocList = demandDocDao.query(demandDoc);
        if (CommonUtils.isNullOrEmpty(demandDocList)) {
            demandDoc.setUpload_user(user.getId());//获取当前id
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUser_name_cn(user.getUser_name_cn());
            userInfo.setUser_name_en(user.getUser_name_en());
            demandDoc.setUpload_user_all(userInfo);
            demandDoc.setCreate_time(TimeUtil.getTimeStamp(new Date()));//设置创建时间
            demandDocDao.save(demandDoc);
        } else {
            DemandDoc demandDocBefore = demandDocList.get(0);
            demandDoc.setUpdate_time(TimeUtil.getTimeStamp(new Date()));
            demandDoc.setId(demandDocBefore.getId());
            demandDocDao.updateById(demandDoc);
        }
    }


    /**
     * 文档相关数据录入
     */
    @Override
    public DemandDoc save(DemandDoc demandDoc) throws Exception {
        return demandDocDao.save(demandDoc);
    }

    @Override
    public boolean uploadFiletoMinio(String moduleName, String path, MultipartFile multipartFile, User user) {
        String authorization = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader("Authorization");
        try {
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("moduleName", moduleName);
            param.add("path", path);
            param.add("files", multipartFile.getResource());
            param.add("user", user);
            HttpEntity httpHeaders = setHttpHeader(param, authorization);
            String ula = url + "/fdocmanage/api/file/filesUpload";
            restTemplate.exchange(ula, HttpMethod.POST, httpHeaders, String.class);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            logger.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    path,
                    sw.toString());
            return false;
        }
        return true;
    }

    //需要在gitlab上创建文件夹的文档类型
    private final static List<String> docTypeList = new ArrayList<String>(){{
        add(DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getValue());
        add(DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_INSTRUCTION.getValue());
        add(DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getValue());
        add(DemandDocEnum.DemandDocTypeEnum.DEMAND_ASSESS_REPORT.getValue());
        add(DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_CONFIRM.getValue());
        add(DemandDocEnum.DemandDocTypeEnum.BUSINESS_TEST_REPORT.getValue());
        add(DemandDocEnum.DemandDocTypeEnum.LAUNCH_CONFIRM.getValue());
    }};
    @Async
    @Override
    public void fileToGitlab(DemandBaseInfo demand) {
        //没有开发任务的需求不用管
        List<Map> taskList = taskService.queryTaskByDemandId(demand.getId());
        if (!CommonUtils.isNullOrEmpty(taskList)
                && taskList.stream().anyMatch(task -> null == task.get(Dict.TASKTYPE))) {
            //查询需求下所有实施单元
            List<IpmpUnit> ipmpUnitList = null;
            try {
                if (Constants.TECH.equals(demand.getDemand_type())) {
                    //获取所有研发单元
                    List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryAllFdevUnitByDemandId(demand.getId());
                    if(!CommonUtils.isNullOrEmpty(fdevUnitList)){
                        Set implUnitNumList = fdevUnitList.stream().map(fdevUnit -> fdevUnit.getIpmp_implement_unit_no()).collect(Collectors.toSet());
                        if(!CommonUtils.isNullOrEmpty(implUnitNumList)){
                            Map<String,Object> ipmpMap = ipmpUnitDao.queryIpmpUnitByNums(new HashMap<String, Object>(){{
                                put(Dict.IMPLUNITNUMLIST,implUnitNumList);
                            }});
                            if (!CommonUtils.isNullOrEmpty(ipmpMap)) {
                                ipmpUnitList = (List<IpmpUnit>) ipmpMap.get(Dict.DATA);
                            }
                        }
                    }
                } else {
                    ipmpUnitList = ipmpUnitDao.queryIpmpUnitByDemandId(demand.getOa_contact_no());
                }
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] {"查询需求下实施单元失败"});
            }
            if (!CommonUtils.isNullOrEmpty(ipmpUnitList)) {
                //涉及到的任务集编号
                List<String> prjNumList = ipmpUnitList.stream().map(IpmpUnit::getPrjNum).distinct().collect(Collectors.toList());
                prjNumList.remove(null);
                //归档需求相关文档
                fileDemandDoc(demand, prjNumList);
                //归档任务相关文档
                fileTaskDoc(demand, taskList, prjNumList);
            }
        }
    }

    /**
     * 归档需求相关文档
     * @param demand
     * @param prjNumList
     */
    private void fileDemandDoc(DemandBaseInfo demand, List<String> prjNumList) {
        //查询需求下文档
        DemandDoc demandDoc = new DemandDoc();
        demandDoc.setDemand_id(demand.getId());
        List<DemandDoc> demandDocList = new ArrayList<>();
        try {
            demandDocList = demandDocDao.queryAll(demandDoc);
        } catch (Exception e) {
            logger.info(">>>queryDemandFile fail,{}", demand.getId());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        for (DemandDoc doc : demandDocList) {
            byte[] file = fdocmanageService.downloadFileByte("fdev-demand", doc.getDoc_path());
            //指定文件类型中的才用类型建文件夹，其它的类型放在其它目录
            StringBuffer path = new StringBuffer();
            path.append("/");
            if (docTypeList.contains(doc.getDoc_type())) {
                //如果是内测报告
                if (DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getValue().equals(doc.getDoc_type())) {
                    //只取用需求名称创建的那个，另一个是其它功能用的
                    if (doc.getDoc_name().contains(demand.getOa_contact_name())) {
                        path.append(DemandBaseInfoUtil.getDocType(doc.getDoc_type()));
                    } else {
                        continue;
                    }
                } else {
                    path.append(DemandBaseInfoUtil.getDocType(doc.getDoc_type()));
                }
            } else {
                path.append("需求其它文档");
            }
            path.append("/");
            path.append(demand.getOa_contact_no());
            path.append("/");
            path.append(doc.getDoc_name());
            //上传文件至gitlab
            uploadToGitLab(prjNumList, file, path);
        }
    }

    /**
     * 归档任务相关文档
     * @param demand
     * @param taskList
     * @param prjNumList
     */
    private void fileTaskDoc(DemandBaseInfo demand, List<Map> taskList, List<String> prjNumList) {
        //查询每个开发任务的关联文档
        for (Map<String, Object> task : taskList) {
            List<Map> taskDocList = taskService.queryDocDetail((String) task.get(Dict.ID));
            if (!CommonUtils.isNullOrEmpty(taskDocList)) {
                for (Map<String, Object> taskDoc : taskDocList) {
                    if (Constants.BUSINESS.equals(demand.getDemand_type()) && "投产类-自测报告".equals(taskDoc.get(Dict.TYPE))) {
                        continue;
                    }
                    byte[] file = fdocmanageService.downloadFileByte("fdev-task", (String) taskDoc.get(Dict.PATH));
                    StringBuffer path = new StringBuffer();
                    path.append("/");
                    if ("投产类-自测报告".equals(taskDoc.get(Dict.TYPE))) {
                        path.append("自测报告");
                    } else {
                        path.append("任务其它文档");
                    }
                    path.append("/");
                    path.append(demand.getOa_contact_no());
                    path.append("/");
                    path.append(task.get(Dict.NAME));
                    path.append("/");
                    path.append(taskDoc.get(Dict.NAME));
                    //上传文件至gitlab
                    uploadToGitLab(prjNumList, file, path);
                }
            }
        }
    }

    /**
     * 上传文件至gitlab
     * @param prjNumList
     * @param file
     * @param path
     */
    private void uploadToGitLab(List<String> prjNumList, byte[] file, StringBuffer path) {
        //每个任务集对应的目录下都要上传一份
        for (String prjNum : prjNumList) {
            //上传到gitlab
            List<byte[]> files = new ArrayList<>();
            files.add(file);
            gitlabService.uploadFile(files, prjNum + path.toString());
        }
    }

    private HttpEntity setHttpHeader(MultiValueMap<String, Object> param, String auth) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", auth);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
        HttpEntity request = new HttpEntity<Object>(param, httpHeaders);
        return request;
    }

    /**
     * 删除需求doc
     * 批量删除
     *
     * @param
     */
    @Override
    public void deleteDemandDoc(Map params) throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
        List<String> listIds = (List<String>) params.get(Dict.IDS);
        for (String id : listIds) {
            String doc_id = id;
            DemandDoc demandDoc = demandDocDao.queryById(doc_id);
            String demand_id = demandDoc.getDemand_id();
            // 如果是需求评估删除
            if ("demandAccess".equals(String.valueOf(params.get("demand_kind")))) {
                // 通过id 查询是否存在需求评估业务
                DemandAssess getDemandAssess = demandAssessDao.queryById(demand_id);
                // 当查询不出任何需求评估业务内容时，则直接抛出异常
                if (CommonUtils.isNullOrEmpty(getDemandAssess)) {
                    throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
                }
                // 判断是否拥有权限
                if (!(roleService.isDemandManager() ||
                        (!CommonUtils.isNullOrEmpty(getDemandAssess.getDemand_leader()) &&
                                getDemandAssess.getDemand_leader().contains(CommonUtils.getSessionUser().getId()))
                )) {
                    // 如果没有拥有权限，则抛出权限异常
                    throw new FdevException(ErrorConstants.FUSER_ROLE_ERROR);
                }
            } else {
                // 否则需求文档删除
                boolean isDemandLeader = roleService.isDemandLeader(demand_id, user.getId());
                boolean isDemandGroupLeader = roleService.isDemandGroupLeader(demand_id, user.getId());
                if (!roleService.isDemandManager() && !isDemandLeader && !isDemandGroupLeader) {
                    throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为需求管理员或需求牵头人或板块涉及人员"});
                }
            }

        }
        List<String> listIdStrings = new ArrayList<>();
        List<String> listLinkStrings = new ArrayList<>();
        listIdStrings = (List<String>) params.get(Dict.IDS);
        listLinkStrings = (List<String>) params.get(Dict.DOC_LINK);
        if (CommonUtils.isNullOrEmpty(listLinkStrings)) {
            for (String id : listIdStrings) {
                DemandDoc deamnDocs = demandDocDao.queryById(id);
                if (CommonUtils.isNullOrEmpty(deamnDocs)) {
                    continue;
                }
                String path = deamnDocs.getDoc_path();
                boolean result = fdocmanageService.deleteFiletoMinio("fdev-demand", path, user);
                if (!result) {
                    throw new FdevException(ErrorConstants.DOC_ERROR_DELETE, new String[]{"文件删除失败,请重试!"});
                }
                demandDocDao.deleteById(id);
            }
        } else {
            for (String id : listIdStrings) {
                DemandDoc deamnDocs = demandDocDao.queryById(id);
                if (CommonUtils.isNullOrEmpty(deamnDocs)) {
                    continue;
                }
                demandDocDao.deleteById(id);
            }
        }

    }
}
