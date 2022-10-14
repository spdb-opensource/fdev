package com.spdb.fdev.fdemand.spdb.unit;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.DesignDoc;
import com.spdb.fdev.fdemand.spdb.service.FdocmanageService;
import com.spdb.fdev.fdemand.spdb.service.IDemandBaseInfoService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * 设计还原相关
 */
@RefreshScope
@Component
public class DesignUnit {

    private Logger log = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Resource
    private IRoleService fdevUserService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private IDemandBaseInfoService demandService;

    @Autowired
    private FdocmanageService fdocmanageService;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IDemandBaseInfoDao iDemandBaseInfoDao;


    @Value("${fdev.demand.ip}")
    private String demandId;

    @Value("${fdev.demand.design.stage}")
    private List<String> designStages;


    public void verifyRoleAndStage(Map<String, String> param, String designState) throws Exception {
        Map uiMainRole = fdevUserService.queryFdevRoleByName(Constants.UI_MANAGER);
        String mainId = (String) uiMainRole.get(Dict.ID);
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession().getAttribute(Dict._USER);
        if (!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            //当状态 变为auditIn审核中时，需要校验当前用户是否拥有 ui团队负责人权限
            if (designState.equals(Dict.AUDITIN) && !user.getRole_id().contains(mainId)) { //如果当前用户没有权限
                throw new FdevException(ErrorConstants.FUSER_ROLE_ERROR, new String[]{"UI团队负责人"});
            }
        }
        if (CommonUtils.isNullOrEmpty(param.get("newStatus"))) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{" 设计稿状态 "});
        }
    }


    /**
     * //校验更新的stage是否是属于该阶段的下一阶段
     *  仅校验 uploadNot -> uploaded -> auditWait -> auditIn
     * @param demand
     * @param designState
     */
    public void verifyIsNextStage(DemandBaseInfo demand, String designState , Map<String, String> param) {
        if (CommonUtils.isNullOrEmpty(demand))
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"该需求不存在"});
        String oldStage = demand.getDesign_status();
        String stage = param.get("stage");
        //校验输入的阶段是否合法
        Integer mark = null;
        for (int i = 0; i < designStages.size(); i++) {
            if (designState.equals(designStages.get(i))) {
                mark = i;
                break;
            }
        }
        //uploaded,auditWait,auditIn
        if (mark == null && !designState.equals("completedAudit") &&
                !designState.equals("auditPassNot") && !designState.equals("auditPass"))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"所更新的阶段不存在"});
        if (mark == null) {
            if (oldStage.equals("completedAudit"))
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"所更新的阶段已经完成"});
            //判断是否为auditIn的类型
            if ((designState.equals("completedAudit")) &&
                    !oldStage.equals(designStages.get(designStages.size() - 1)) && !oldStage.equals("auditPassNot"))
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"更新阶段不合法"});
            if (designState.equals("auditPassNot")){
                if (!oldStage.equals("auditPassNot") && !oldStage.equals(designStages.get(designStages.size() - 1)))
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"更新阶段不合法"});
                //如果状态为auditPassNot，小的为load_upload的时候将状态更新为auditIn
                if (stage.equals("load_upload")) {
                    designState = designStages.get(designStages.size() - 1);
                    param.put("newStatus",designState);
                    return;
                }
            }
        } else {
            if (mark - 1 < 0) {
                //即为更新成uploaded
                if (!oldStage.equals(designStages.get(0)))
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前更新的阶段不属于当前阶段的下一个阶段"});
            }
            String updateStage = designStages.get(mark - 1);
            //查看当前mark - 1是否与原阶段相同
            if (!updateStage.equals(oldStage)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前更新的阶段不属于当前阶段的下一个阶段"});
            }
        }
    }

    //组装stage参数
    public Map<String, List<Map<String, String>>> getStringListMap(Map<String, String> param, DemandBaseInfo rqrmnt, String stage) {
        Map<String, List<Map<String, String>>> designMap = Optional
                .ofNullable(rqrmnt.getDesignMap())
                .orElse(new HashMap<>());
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("name", param.get("name"));
        dataMap.put("time", param.get("time"));
        dataMap.put("remark", param.get("remark"));
        designMap.put(stage, Arrays.asList(dataMap));
        return designMap;
    }

    //组装nopass参数
    public Map<String, List<Map<String, String>>> getNopassMap(Map<String, String> param, DemandBaseInfo rqrmnt, String stage) {
        Map<String, List<Map<String, String>>> designMap = Optional
                .ofNullable(rqrmnt.getDesignMap())
                .orElse(new HashMap<>());
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("name", param.get("name"));
        dataMap.put("time", param.get("time"));
        dataMap.put("remark", param.get("remark"));
        dataMap.put("stage", param.get("stage"));
        List<Map<String, String>> nopass = Optional
                .ofNullable(designMap.get("nopass")).orElse(new ArrayList<>());
        nopass.add(dataMap);
        designMap.put(stage, nopass);
        return designMap;
    }

    public void sendNotify(DemandBaseInfo DemandBaseInfo) throws Exception {
        Map uiMainRole = fdevUserService.queryFdevRoleByName(Constants.UI_MANAGER);
        String mainId = (String) uiMainRole.get(Dict.ID);
        List<LinkedHashMap> userInfos = fdevUserService.queryUserByRole(mainId);
        List<String> userIds = new ArrayList<>();
        for (LinkedHashMap userInfo : userInfos) {
            if (!CommonUtils.isNullOrEmpty(userInfo.get(Dict.ID)))
                userIds.add((String) userInfo.get(Dict.ID));
        }
        demandService.sendUsersToDo(DemandBaseInfo, userIds, "infoMsg");
    }

    public Map getGroupInfo(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id "});
        }
        param.put(Dict.REST_CODE, "getGroups");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
//            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->USER 查询小组返回为空"});
            //避免报错
            log.error(ErrorConstants.DATA_NOT_EXIST, "TASK->USER 查询小组返回为空");
            return new HashMap();
        }
        ArrayList jsonArray = (ArrayList) result;
        return (Map) jsonArray.get(0);
    }

    @Async
    public void deleteNotify(List<String> rqrId, String type,String demanId) {
        rqrId.stream().forEach(user -> deleteTodoList(user, type,demanId));
    }

    public void deleteTodoList(String userId, String type,String demanId) {
        try {
            restTransport.submit(new HashMap() {{
                put("type", type);
                put("target_id", demanId);
                put("module", "rqrmnt");
                put("executor_id", userId);
                put(Dict.REST_CODE, "updateByTargetIdAndType");
            }});
        } catch (Exception e) {
            log.error("删除失败！" + e.getMessage());
        }
    }

    @Async
    public void sendToList(DemandBaseInfo rqrmnt, List<String> userId, String desc, String type) {
       sendToListMethod(rqrmnt, userId, desc, type);
    }

    /**
     *
     * @// TODO: 2020/11/6
     * @param demand
     * @param userId
     * @param desc
     * @param type
     */
    public void sendToListMethod(DemandBaseInfo demand, List<String> userId, String desc, String type) {
        String link = demandId + "/fdev/#/rqrmn/designReviewRqr/" + demand.getId();
        HashMap todoMap = new HashMap<>();
        todoMap.put(Dict.REST_CODE, "addCommissionEvent");
        todoMap.put("user_ids", userId);
        todoMap.put("module", "rqrmnt");
        todoMap.put("description", desc);
        todoMap.put("link", link);
        todoMap.put("type", type);
        todoMap.put("target_id", demand.getId());
        try {
            restTransport.submit(todoMap);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            log.info("发送todoList失败,请求参数:{}Error Trace:{}", todoMap, sw.toString());
        }
    }

    public void deleteAll() throws Exception {
        Map uiMainRole = fdevUserService.queryFdevRoleByName(Constants.UI_MANAGER);
        String mainId = (String) uiMainRole.get(Dict.ID);
        List<LinkedHashMap> userInfos = fdevUserService.queryUserByRole(mainId);
        List<String> userIds = new ArrayList<>();
        for (LinkedHashMap userInfo : userInfos) {
            if (!CommonUtils.isNullOrEmpty(userInfo.get(Dict.ID)))
                userIds.add((String) userInfo.get(Dict.ID));
        }
    }

    public void dealWpsFile(DemandBaseInfo demandBaseInfo) {
        //        createFolder
        Map folder = fdocmanageService.createFolder(demandBaseInfo.getId());
        if(CommonUtils.isNullOrEmpty(folder)){
            log.error("查询所有文件夹id失败：查询id={}",demandBaseInfo.getId());
            return;
        }
        String androidId = (String)folder.get("androidUploadId");
        String iosId = (String)folder.get("iosUploadId");
        String folderId = (String)folder.get("folderId");
        //wpsList
        Map<String,List> listFile = fdocmanageService.wpsList(folderId);
        List lists = Optional.ofNullable(listFile.get("value")).orElse(new ArrayList());
        if(lists.size() < 4 ){
            return;
        }
        lists.forEach(list->{
            Map<String,Object> listMap = (Map)list;
            if(null != androidId && androidId.equals(listMap.get("id"))){
                List childFile = (List)listMap.get("childFile");
                //安卓设计稿
                if (!CommonUtils.isNullOrEmpty(listMap.get("childFile"))){
                    //存在文件，获取下载地址
                    childFile.forEach(child->{
                        Map<String,Object> chilldMap = (Map)child;
                        Map childInfo = fdocmanageService.content((String) chilldMap.get("id"));
                        String url = (String)childInfo.get("url");
                        log.info("获取的下载地址为:{}",url);
                        String pathMinio  = "demand/"+demandBaseInfo.getId() + "/" + "android" + "/" + chilldMap.get("uploadStage") + "/"+chilldMap.get("name");
                        //上传
                        boolean result = fdocmanageService.downloadAndUpload(url,pathMinio,(String)chilldMap.get("name"));
                        //更新
                        if(result){
                            DesignDoc designDoc = new DesignDoc();
                            designDoc.setDocType("android");
                            designDoc.setMinioPath(pathMinio);
                            designDoc.setUploadStage((String)chilldMap.get("uploadStage"));
                            designDoc.setFileName((String)chilldMap.get("name"));
                            long l =  iDemandBaseInfoDao.updateDesignDoc(demandBaseInfo.getId(), designDoc);
                            log.info("更新结果影响行数:{}",l);
                        }
                    });
                }
            }
            if(null != iosId && iosId.equals(listMap.get("id"))){
                //ios设计稿
                List childFile = (List)listMap.get("childFile");
                if (!CommonUtils.isNullOrEmpty(listMap.get("childFile"))){
                    //存在文件，获取下载地址
                    childFile.forEach(child->{
                        Map<String,Object> chilldMap = (Map)child;
                        Map childInfo = fdocmanageService.content((String) chilldMap.get("id"));
                        String url = (String)childInfo.get("url");
                        log.info("获取的下载地址为:{}",url);
                        String pathMinio  = "demand/"+demandBaseInfo.getId() + "/"+"ios" + "/"+chilldMap.get("uploadStage") + "/" +chilldMap.get("name");
                        //上传
                        boolean result = fdocmanageService.downloadAndUpload(url,pathMinio,(String)chilldMap.get("name"));
                        //更新
                        if(result){
                            DesignDoc designDoc = new DesignDoc();
                            designDoc.setDocType("ios");
                            designDoc.setMinioPath(pathMinio);
                            designDoc.setUploadStage((String)chilldMap.get("uploadStage"));
                            designDoc.setFileName((String)chilldMap.get("name"));
                            long l =  iDemandBaseInfoDao.updateDesignDoc(demandBaseInfo.getId(), designDoc);
                            log.info("更新结果影响行数:{}",l);
                        }
                    });
                }
            }
        });

    }

}
