package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.esotericsoftware.minlog.Log;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.SpringContextUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.GitlabTransportUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceApplicationDao;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceDao;
import com.spdb.fdev.fdevinterface.spdb.entity.ApproveStatus;
import com.spdb.fdev.fdevinterface.spdb.entity.RestApi;
import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import com.spdb.fdev.fdevinterface.spdb.service.GitLabService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceApplicationService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceRelationService;
import com.spdb.fdev.transport.RestTransport;
import freemarker.template.Template;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import java.util.*;

@Service
@RefreshScope
public class InterfaceApplicationServiceImpl implements InterfaceApplicationService {

    @Autowired
    private RestTransportServiceImpl restTransportServiceImpl;
    @Resource
    private InterfaceApplicationDao interfaceApplicationDao;
    @Autowired
    private InterfaceRelationService interfaceRelationService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;
    @Resource
    private InterfaceDao interfaceDao;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private InterfaceLazyInitService interfaceLazyInitService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    WebApplicationContext applicationContext;
    @Autowired
    private GitLabService gitLabService;
    @Value("${fdev.user.domain}")
    private String url;
    @Value(value = "${path.rest.client.transport}")
    private String transport;
    @Value(value = "${path.rest.client.web.transport}")
    private String restWebTransport;
    @Value(value = "${interface.approval.url}")
    private String interfaceApprovalUrl;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private GitlabTransportUtil gitlabTransportUtil;
    @Value("${path.web.rest.call}")
    private String webRestPath;
    @Value("${path.not.web.rest.call}")
    private String notWebRestPath;
    @Value("${interface.apply.send.email}")
    private boolean isSendApplyEmail;
    private Logger logger = LoggerFactory.getLogger(GitLabServiceImpl.class);

    @Override
    public void addRecord() {
        List<RestRelation> restRelationList = interfaceRelationService.getRestRetation();
        List<RestRelation> sitList = new ArrayList<>();
        List<RestRelation> masterList = new ArrayList<>();
        List<ApproveStatus> approveStatusList = new ArrayList<>();
        for (RestRelation restRelation : restRelationList) {
            if ("SIT".equals(restRelation.getBranch())) {
                sitList.add(restRelation);
            }
            if ("master".equals(restRelation.getBranch())) {
                masterList.add(restRelation);
            }
        }
        for (RestRelation restRelation : sitList) {
            for (RestRelation rest : masterList) {
                if (restRelation.getTransId().equals(rest.getTransId()) && restRelation.getServiceCalling().equals(rest.getServiceCalling())) {
                    masterList.remove(rest);
                    break;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(masterList)) {
            if (CollectionUtils.isEmpty(sitList)) {
                sitList = masterList;
            } else {
                sitList.addAll(masterList);
            }
        }
        for (RestRelation restRelation : sitList) {
            ApproveStatus approveStatus = new ApproveStatus();
            approveStatus.setTransId(restRelation.getTransId());
            approveStatus.setServiceCalling(restRelation.getServiceCalling());
            approveStatus.setServiceId(restRelation.getServiceId());
            approveStatus.setStatus("0");
            // 设置关联的应用Id
            approveStatus.setAppId(interfaceLazyInitService.getAppIdByName(restRelation.getServiceId()));
            approveStatus.setCallingId(interfaceLazyInitService.getAppIdByName(restRelation.getServiceCalling()));
            approveStatusList.add(approveStatus);
        }
        interfaceApplicationDao.insertRecord(approveStatusList);
    }

    @Override
    public Map<String, Object> queryApproveList(Map map) {
        Map resultMap = new HashMap();
        Map<String, Object> approveStatus = interfaceApplicationDao.getApproveList(map);
        List<ApproveStatus> approveStatusList = (List<ApproveStatus>) approveStatus.get("list");
        List<Map> maps = new ArrayList<>();
        if (!FileUtil.isNullOrEmpty(approveStatusList)) {
            //关联应用模块获取应用负责人及行内应用负责人
            for (ApproveStatus approve : approveStatusList) {
                Map<String, Object> params = approverCovertMap(approve);
                List<RestApi> restApiList = interfaceDao.getRestApi((String) params.get("transId"), (String) params.get("serviceId"));
                RestApi restApi = new RestApi();
                if (restApiList.size() >= 1) {
                    restApi = restApiList.get(0);
                }
                params.put("interfaceId", restApi.getId());
                Map<String, Object> appInfoMap = restTransportServiceImpl.getAppInfo((String) params.get("serviceId"));
                if (!FileUtil.isNullOrEmpty(appInfoMap)) {
                    List<Map<String, Object>> devManagersList = (List<Map<String, Object>>) appInfoMap.get(Dict.DEV_MANAGERS);
                    if (!FileUtil.isNullOrEmpty(devManagersList)) {
                        List list = new ArrayList();
                        for (Map param : devManagersList) {
                            Map map1 = new HashMap();
                            map1.put("id", param.get("id"));
                            map1.put("userName", param.get("user_name_cn"));
                            list.add(map1);
                        }
                        params.put("provideDevManagers", list);
                    }
                    List<Map<String, Object>> spdbManagersList = (List<Map<String, Object>>) appInfoMap.get(Dict.SPDB_MANAGERS);
                    if (!FileUtil.isNullOrEmpty(spdbManagersList)) {
                        List list = new ArrayList();
                        for (Map param : spdbManagersList) {
                            Map map1 = new HashMap();
                            map1.put("id", param.get("id"));
                            map1.put("userName", param.get("user_name_cn"));
                            list.add(map1);
                        }
                        params.put("provideSpdbManagers", list);
                    }
                }
                maps.add(params);
            }
        }
        resultMap.put("total", approveStatus.get("total"));
        resultMap.put("list", maps);
        return resultMap;
    }

    @Override
    public Map queryStatus(String transId, String serviceCalling, String serviceId) {
        String status = "2";
        Map map = new HashMap();
        List<ApproveStatus> approveStatusList = interfaceApplicationDao.ApproveRecord(transId, serviceCalling, serviceId);
        if (!FileUtil.isNullOrEmpty(approveStatusList)) {
            List<String> list = new ArrayList<>();
            for (ApproveStatus approveStatus : approveStatusList) {
                list.add(approveStatus.getStatus());
            }
            if (list.contains("0")) {
                status = "0";
                map.put("status", status);
                return map;
            } else if (list.contains("1")) {
                status = "1";
            }
        }
        map.put("status", status);
        return map;
    }

    @Override
    public List<String> insertApproveRecord(Map map) throws Exception {
        User user = getUser();
        if (user == null) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        //获取提供方应用负责人、行内应用负责人和userId
        Map resultMap = queryAppManagers((String) map.get("serviceId"));
        Map manager = getAppManagers(resultMap, "appDevManagers");
        Map spdbManager = getAppManagers(resultMap, "appSpdbManagers");
        //获取调用方应用负责人、行内应用负责人和userId
        Map result = queryAppManagers((String) map.get("serviceCalling"));
        Map manager1 = getAppManagers(result, "appDevManagers");
        Map spdbManager1 = getAppManagers(result, "appSpdbManagers");
        Map re = queryStatus((String) map.get("transId"), (String) map.get("serviceCalling"), (String) map.get("serviceId"));
        String status = (String) re.get("status");
        HashMap model = new HashMap();
        if ("0".equals(status)) {//接口已申请
            throw new FdevException(ErrorConstants.TRANS_ID_IS_APPROVE);
        } else if ("1".equals(status)) {//接口正在审批中
            throw new FdevException(ErrorConstants.TRANS_ID_IS_PROCESSED);
        }
        if ("2".equals(status)) {//如果没有记录或记录为拒绝则直接插入数据
            List<ApproveStatus> approveStatusList = new ArrayList<>();
            ApproveStatus approveStatus = new ApproveStatus();
            approveStatus.setTransId((String) map.get("transId"));
            approveStatus.setServiceId((String) map.get("serviceId"));
            approveStatus.setProvideManagers((List<String>) manager.get("userName"));
            approveStatus.setProvideSpdbManagers((List<String>) spdbManager.get("userName"));
            approveStatus.setServiceCalling((String) map.get("serviceCalling"));
            approveStatus.setCallingManagers((List<String>) manager1.get("userName"));
            approveStatus.setCallingSpdbManagers((List<String>) spdbManager1.get("userName"));
            approveStatus.setApplicant(user.getUser_name_cn());
            approveStatus.setReason((String) map.get("reason"));
            approveStatus.setStatus("1");
            // 设置关联的应用Id
            approveStatus.setAppId(interfaceLazyInitService.getAppIdByName((String) map.get("serviceId")));
            approveStatus.setCallingId(interfaceLazyInitService.getAppIdByName((String) map.get("serviceCalling")));
            approveStatus.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            approveStatusList.add(approveStatus);
            interfaceApplicationDao.insertRecord(approveStatusList);
        }
        //给提供方、调用方应用负责人及行内负责人发邮件通知
        if (isSendApplyEmail) {
            model.put("serviceCalling", map.get("serviceCalling"));
            model.put("serviceId", map.get("serviceId"));
            model.put("transId", map.get("transId"));
            model.put("applicant", user.getUser_name_cn());
            model.put("reason", map.get("reason"));
            model.put("date", TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            //提供方收件人
            String[] arr1 = getAddress((List<String>) manager.get("id"), (List<String>) spdbManager.get("id"));
            //调用方收件人
            String[] arr2 = getAddress((List<String>) manager1.get("id"), (List<String>) spdbManager1.get("id"));
            String[] to = (String[]) ArrayUtils.addAll(arr1, arr2);
            send(to, Constants.INTERFACE_APPLY, model, (String) map.get("serviceId"));
        }
        //给提供方方应用负责人及行内应用负责人添加用户待办项
        String[] ids = getUserIds(manager, spdbManager);
        List<ApproveStatus> approveStatusList = interfaceApplicationDao.queryApproveList((String) map.get("transId"), (String) map.get("serviceCalling"), (String) map.get("serviceId"));
        ApproveStatus approveStatus = new ApproveStatus();
        for (ApproveStatus approve : approveStatusList) {
            if ("1".equals(approve.getStatus())) {
                approveStatus = approve;
            }
        }
        addCommissionEvent(ids, approveStatus);
        //给提供方应用负责人及行内负责人添加fdev通知
        String content = approveStatus.getServiceCalling() + "调用" + approveStatus.getServiceId() + "的REST接口" + approveStatus.getTransId() + "审批";
        String[] userNameEnList = getUserNameEnList(manager, spdbManager);
        sendFdevNotify(content, "0", userNameEnList, interfaceApprovalUrl, "新增接口调用申请");
        Set<String> provide = new HashSet<>();
        provide.addAll((List<String>) manager.get("userName"));
        provide.addAll((List<String>) spdbManager.get("userName"));
        List<String> provideManagers = new ArrayList<>();
        provideManagers.addAll(provide);
        return provideManagers;
    }

    @Override
    public void updateApproveStatus(Map map) throws Exception {
    	
		Map<String, Object> serviceInfoMap = getAppInfoById((String) map.get("appId"));
		if ("0".equals(serviceInfoMap.get(Dict.STATUS))) {			
			 interfaceApplicationDao.deleteApproveStatus(map);
			// 调用通知模块接口将审批人删除用户待办项
			 deleteCommissionEvent((String) map.get("id"));
			 throw new FdevException(ErrorConstants.SERVICE_APP_NOT_EXIST,new String[]{"请刷新页面"});
		}
		Map<String, Object> callInfoMap = getAppInfoById((String) map.get("callingId"));
		if ("0".equals(callInfoMap.get(Dict.STATUS))) {
			interfaceApplicationDao.deleteApproveStatus(map);
			deleteCommissionEvent((String) map.get("id"));
			throw new FdevException(ErrorConstants.CALLING_APP_NOT_EXIST,new String[]{"请刷新页面"});
		}
		List<RestApi> restApiList = interfaceDao.getRestApi((String) map.get("transId"), (String) map.get("serviceId"));
		if(CommonUtil.isNullOrEmpty(restApiList)) {
		  interfaceApplicationDao.deleteApproveStatus(map);
		  deleteCommissionEvent((String) map.get("id"));
		  throw new FdevException(ErrorConstants.SERVICE_TRANS_NOT_EXIST,new String[]{"请刷新页面"});
		}
		 
        User user = getUser();
        if (user == null) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        map.put("approver", user.getUser_name_cn());
        interfaceApplicationDao.updateApproveStatus(map);
        ApproveStatus approveStatus = interfaceApplicationDao.queryRecordById((String) map.get("id"));
        //获取提供方应用负责人、行内应用负责人的userId
        Map resultMap = queryAppManagers(approveStatus.getServiceId());
        Map manager = getAppManagers(resultMap, "appDevManagers");
        Map spdbManager = getAppManagers(resultMap, "appSpdbManagers");
        //获取调用方应用负责人、行内应用负责人的userId
        Map result = queryAppManagers(approveStatus.getServiceCalling());
        Map manager1 = getAppManagers(result, "appDevManagers");
        Map spdbManager1 = getAppManagers(result, "appSpdbManagers");
        //调用通知模块接口通知提供方、调用方的应用负责人和行内应用负责人接口已审批
        if (isSendApplyEmail) {
            HashMap model = new HashMap();
            model.put("serviceCalling", approveStatus.getServiceCalling());
            model.put("serviceId", approveStatus.getServiceId());
            model.put("transId", approveStatus.getTransId());
            model.put("approver", user.getUser_name_cn());
            if ("0".equals(approveStatus.getStatus())) {
                model.put("result", "审批通过");
            }
            if ("2".equals(approveStatus.getStatus())) {
                model.put("result", "审批拒绝");
            }
            model.put("reason", approveStatus.getRefuseReason());
            model.put("date", TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            //提供方收件人
            String[] arr1 = getAddress((List<String>) manager.get("id"), (List<String>) spdbManager.get("id"));
            //调用方收件人
            String[] arr2 = getAddress((List<String>) manager1.get("id"), (List<String>) spdbManager1.get("id"));
            String[] to = (String[]) ArrayUtils.addAll(arr1, arr2);
            send(to, Constants.INTERFACE_APPROVE, model, approveStatus.getServiceId());
        }
        // 调用通知模块接口将审批人的用户待办项状态改为已完成
        updateByTargetIdAndType((String) map.get("id"));
        // 发送fdev通知提供方和调用方应用负责人和行内负责人接口已审批
        String content = approveStatus.getServiceCalling() + "调用" + approveStatus.getServiceId() + "的REST接口" + approveStatus.getTransId() + "审批";
        if ("0".equals(approveStatus.getStatus())) {
            content += "已通过";
        }
        if ("2".equals(approveStatus.getStatus())) {
            content += "已拒绝";
        }
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(Arrays.asList(getUserNameEnList(manager, spdbManager)));
        hashSet.addAll(Arrays.asList(getUserNameEnList(manager1, spdbManager1)));
        String[] userNameEnList = hashSet.toArray(new String[hashSet.size()]);
        sendFdevNotify(content, "0", userNameEnList, interfaceApprovalUrl, "接口调用申请状态更新");
    }

    
/**
 * 通过应用id获取应用信息
 * @param appId
 * @return
 * @throws Exception
 */
	private Map<String, Object> getAppInfoById(String appId) throws Exception{
    	
         Map<String, String> map = new HashMap<>();
         map.put(Dict.REST_CODE, "queryAppById");
         map.put(Dict.ID, appId);
         Map<String, Object> appMap = new HashMap<>();
         try {
        	 appMap = (Map<String, Object>) restTransport.submit(map);
         } catch (Exception e) {
             Log.info("调用fdev的app模块服务出错-------", e);
             throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"通讯异常!"});
         }
         return appMap;
	}
    
    
    

	@Override
    public Map isNoApplyInterface(String serviceCalling, String branch) throws Exception {
        Map map = new HashMap();
        boolean flag = true;
        // 去掉应用英文名中末尾的-parent
        if (serviceCalling.endsWith(Dict.PARENT)) {
            serviceCalling = serviceCalling.replace(Dict.PARENT, "");
        }
        // 根据应用英文名，调应用模块的接口拿到GitLab Project ID
        Map<String, Object> appInfoMap = restTransportServiceImpl.getAppInfo(serviceCalling);
        Integer projectId;
        if (appInfoMap != null) {
            projectId = (Integer) appInfoMap.get("gitlab_project_id");
        } else {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据应用英文名" + serviceCalling + "未获取到应用模块相关数据"});
        }
        String transportFilePath;
        if (serviceCalling.contains(Dict.WEB)) {
            // web项目路径：src\main\resources\config\channel\pe-transport.xml
            transportFilePath = serviceCalling + webRestPath;
        } else {
            // 非web项目路径：src\main\resources\config\spdb\common\channel\rest_transport.xml
            transportFilePath = notWebRestPath;
        }

        if (StringUtils.isEmpty(transportFilePath)) {
            flag = false;
            map.put("flag", flag);
            return map;
        }
        String content = gitLabService.getFileContent(projectId, branch, transportFilePath);
        List<Map> list = analysisXmlFile(content);
        List<String> resultList = new ArrayList<>();
        if (FileUtil.isNullOrEmpty(list)) {
            flag = false;
            map.put("flag", flag);
            return map;
        } else {
            for (Map map1 : list) {
                Map resultMap = queryStatus((String) map1.get("transId"), serviceCalling, (String) map1.get("serviceId"));
                if ("1".equals(resultMap.get("status")) || "2".equals(resultMap.get("status"))) {
                    resultList.add((String) map1.get("transId"));
                }
            }

            if (resultList.size() == 0 || resultList == null) {
                flag = false;
            }
            map.put("flag", flag);
            map.put("transId", resultList);
        }
        return map;
    }

    @Override
    public boolean isManagers() {
        User user = getUser();
        if (user == null) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String userId = user.getId();
        //发送应用模块接口查询当前用户为应用负责人的应用列表
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryPagination");
        map.put("user_id", userId);
        map.put("index", 1);
        map.put("size", 1);
        try {
            Map resultMap = (Map) restTransport.submit(map);
            if ((int) resultMap.get("count") > 0) {
                return true;
            }
        } catch (Exception e) {
            logger.error("调用应用模块接口出错");
        }
        return userVerifyUtil.userRoleIsSPM(user.getRole_id());
    }

    /**
     * 发应用模块接口获取应用负责人、行内负责人
     *
     * @param serviceName
     * @return
     */
    private Map queryAppManagers(String serviceName) {
        List<Map<String, Object>> appDevManagers = new ArrayList<>();
        List<Map<String, Object>> appSpdbManagers = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryAppManagers");
        map.put(Dict.NAME_EN, serviceName);
        List<Map<String, Object>> result;
        try {
            result = (List<Map<String, Object>>) restTransport.submit(map);
            if (CollectionUtils.isNotEmpty(result)) {
                for (int i = 0; i < result.size(); i++) {
                    if (serviceName.equalsIgnoreCase(((String) result.get(i).get(Dict.NAME_EN)).trim())) {
                        List<Map> devList = (List<Map>) result.get(i).get("dev_managers");
                        List<Map> spdbList = (List<Map>) result.get(i).get("spdb_managers");
                        for (Map map1 : devList) {
                            Map<String, Object> resultMap = new HashMap();
                            resultMap.put("userName", map1.get("user_name_cn"));
                            resultMap.put("id", map1.get("id"));
                            resultMap.put("userNameEn", map1.get("user_name_en"));
                            appDevManagers.add(resultMap);
                        }
                        for (Map map1 : spdbList) {
                            Map<String, Object> resultMap = new HashMap();
                            resultMap.put("userName", map1.get("user_name_cn"));
                            resultMap.put("id", map1.get("id"));
                            resultMap.put("userNameEn", map1.get("user_name_en"));
                            appSpdbManagers.add(resultMap);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.info("查询应用负责人出错-------" + serviceName);
        }
        Map resultMap = new HashMap();
        resultMap.put("appDevManagers", appDevManagers);
        resultMap.put("appSpdbManagers", appSpdbManagers);
        return resultMap;
    }

    /**
     * 获取应用模块中所有应用负责人及行内应用负责人
     *
     * @return
     */
    private Map getAllAppManagers() {
        List devList = new ArrayList();
        List spdbList = new ArrayList();
        Map map = new HashMap<>();
        Map reqMap = new HashMap();
        reqMap.put(Dict.REST_CODE, "queryApp");	
        try {
            List<Map<String, Object>> appInfoList = (List<Map<String, Object>>) restTransport.submit(reqMap);
            if (!FileUtil.isNullOrEmpty(appInfoList)) {
                for (Map<String, Object> appMap : appInfoList) {
                    List<Map> devManagers = (List<Map>) appMap.get("dev_managers");
                    List<Map> spdbManagers = (List<Map>) appMap.get("spdb_managers");
                    devList.addAll(devManagers);
                    spdbList.addAll(spdbManagers);
                }
            }

        } catch (Exception e) {
            Log.info("调用fdev的app模块服务出错-------", e);
        }
        map.put("devManagers", devList);
        map.put("spdbManagers", spdbList);
        return map;
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    private User getUser() {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
        return user;
    }

    /**
     * 调通知模块接口发通知邮件
     *
     * @param to
     * @param content
     * @param subject
     */
    private void sendEmail(String[] to, String content, String subject) {
        Map param = new HashMap<>();
        param.put(Dict.CONTENT, content);
        param.put("subject", subject);
        param.put("to", to);
        try {
            param.put(Dict.REST_CODE, "sendEmail");
            restTransport.submit(param);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("发送邮件失败" + e.getMessage());
        }
    }

    /**
     * 发用户模块接口获取用户邮箱地址和用户id
     *
     * @param userId
     * @return
     */
    private String userEmail(String userId) {
        String address = "";
        Map param = new HashMap<>();
        param.put("id", userId);
        param.put(Dict.REST_CODE, "queryUser");
        try {
            List<Map<String, Object>> result = (List<Map<String, Object>>) restTransport.submit(param);
            if (!FileUtil.isNullOrEmpty(result)) {
                for (Map map : result) {
                    address = (String) map.get("email");
                }
            }
        } catch (Exception e) {
            Log.info("获取用户邮箱地址失败" + e.getMessage());
        }
        return address;
    }

    /**
     * 发送邮件通知
     *
     * @param to
     * @param key
     * @param model
     * @param serviceId
     * @throws Exception
     */
    private void send(String[] to, String key, HashMap model, String serviceId) throws Exception {
        Properties ret = PropertiesLoaderUtils
                .loadProperties(new ClassPathResource("application-email.properties"));
        String emailConf = ret.getProperty(key);
        String str = null;
        if (Constants.INTERFACE_APPLY.equals(key)) {
            str = "REST接口申请";
        }
        if (Constants.INTERFACE_APPROVE.equals(key)) {
            str = "REST接口审批";
        }
        String subject = str + "(申请" + serviceId + "应用接口调用)";
        String activeProfile = SpringContextUtil.getActiveProfile();
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(activeProfile)) {
            subject = "【" + activeProfile + "】" + subject;
        }
        String templateName = emailConf;
        if (FileUtil.isNullOrEmpty(to)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"发件人为空"});
        }
        model.put("subject", subject);
        model.put("template_name", templateName);
        String content = getContent("index.ftl", model);
        sendEmail(to, content, subject);

    }

    /**
     * @param templatePath 模版文件名
     * @param replace      替换的字段和value的map集合
     * @return 邮件正文
     * @throws Exception
     */
    private String getContent(String templatePath, HashMap replace) throws Exception {
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templatePath);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, replace);
    }

    /**
     * 获取收件人列表
     *
     * @param appManagersId
     * @param appSpdbManagersId
     * @return
     */
    private String[] getAddress(List<String> appManagersId, List<String> appSpdbManagersId) {
        Set<String> set = new HashSet();
        if (!FileUtil.isNullOrEmpty(appManagersId)) {
            for (String id : appManagersId) {
                set.add(userEmail(id));
            }
        }
        if (!FileUtil.isNullOrEmpty(appSpdbManagersId)) {
            for (String id : appSpdbManagersId) {
                set.add(userEmail(id));
            }
        }
        String[] to = set.toArray(new String[set.size()]);
        return to;
    }

    /**
     * 添加用户待办项
     *
     * @param userId
     */
    private void addCommissionEvent(String[] userId, ApproveStatus approveStatus) {
        Map map = new HashMap();
        map.put("user_ids", userId);
        map.put("module", "interface");
        map.put("description", approveStatus.getServiceCalling() + "调用" + approveStatus.getServiceId() + "的REST接口" + approveStatus.getTransId() + "审批");
        map.put("link", url + "/fdev/#/interface/interfaceCall/interfaceCall");
        map.put("type", "interface_approve");
        map.put("target_id", approveStatus.getId());
        User user = getUser();
        if (user == null) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        map.put("create_user_id", user.getId());
        addTodoList(map);
    }

    /**
     * 修改用户待办项
     *
     * @param id
     */
    private void updateByTargetIdAndType(String id) throws Exception {
        Map map = new HashMap();
        map.put("target_id", id);
        map.put("type", "interface_approve");
        map.put("module", "interface");
        User user = getUser();
        if (user == null) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        map.put("executor_id", user.getId());
        updateTodoList(map);
    }
    
    
    
   /* 删除用户待办项
    *
    * @param id
    */
	private void deleteCommissionEvent(String id) {
		Map map = new HashMap();
		map.put("target_id", id);
		map.put("type", "interface_approve");
		map.put("module", "interface");
		map.put(Dict.REST_CODE, "deleteCommissionEvent");
		Object result = null;
		try {
			result = restTransport.submit(map);
		} catch (Exception e) {
			Log.info("调用fdev的user模块服务出错-------", e);
			throw new FdevException(ErrorConstants.SERVER_ERROR, new String[] { "通讯异常!" });
		}
		Objects.nonNull(result);
	}

    /**
     * @param // user_ids:		代办负责人id集合(有权操作的人员)
     *           //    ?	module:			所属模块(如: release, task, env, interface) 枚举值 task
     *           //    ?	description:	代办描述(如: 任务归档待确认)
     *           //    ?	link:			代办相关链接(如: xxx/fdev/#/release/list/20190730_003/joblist)
     *           //    ?	type:			代办类型(如: task_archived)
     *           //    ?	target_id:		目标id(各模块事项唯一标识) 不一样的才会当成新数据
     *           //    ?	create_user_id:	创建人id(代办发起人)
     * @return
     * @throws Exception
     */
    private Map addTodoList(Map param) {
        Object result = null;
        try {
            param.put(Dict.REST_CODE, "addCommissionEvent");
            result = restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id "});
        }
        return (Map) result;
    }

    /**
     * ?	target_id:		目标id(各模块事项唯一标识)
     * ?	type:			代办类型(如: task_archived)
     * ?	executor_id:    执行人id
     * ?	module:         所属模块(如: release, task, env, interface) 枚举值 task
     *
     * @param param
     * @return
     * @throws Exception
     */
    private Map updateTodoList(Map param) throws Exception {
        param.put(Dict.REST_CODE, "updateByTargetIdAndType");
        Object result = restTransport.submit(param);
        Objects.nonNull(result);
        return (Map) result;
    }

    /**
     * 获取应用负责人名字和id
     *
     * @param map
     * @return
     */
    private Map getAppManagers(Map map, String key) {
        Map resultMap = new HashMap();
        List<Map> appManagers = new ArrayList<>();
        if ("appDevManagers".equals(key)) {
            appManagers = (List<Map>) map.get("appDevManagers");
        }
        if ("appSpdbManagers".equals(key)) {
            appManagers = (List<Map>) map.get("appSpdbManagers");
        }
        List<String> managerList = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        List<String> managerNameEnList = new ArrayList<>();
        if (!FileUtil.isNullOrEmpty(appManagers)) {
            for (Map map1 : appManagers) {
                managerList.add((String) map1.get("userName"));
                idList.add((String) map1.get("id"));
                managerNameEnList.add((String) map1.get("userNameEn"));
            }
        }
        resultMap.put("userName", managerList);
        resultMap.put("id", idList);
        resultMap.put("userNameEn", managerNameEnList);
        return resultMap;
    }

    /**
     * 获取待办负责人的集合id
     *
     * @param devMap
     * @param spdbMap
     * @return
     */
    private String[] getUserIds(Map devMap, Map spdbMap) {
        Set<String> set = new HashSet<>();
        List<String> devManagersId = (List<String>) devMap.get("id");
        List<String> spdbManagersId = (List<String>) spdbMap.get("id");
        if (!FileUtil.isNullOrEmpty(devManagersId)) {
            for (String dev : devManagersId) {
                set.add(dev);
            }
        }
        if (!FileUtil.isNullOrEmpty(spdbManagersId)) {
            for (String spdb : spdbManagersId) {
                set.add(spdb);
            }
        }
        String[] ids = set.toArray(new String[set.size()]);
        return ids;
    }

    private Map<String, Object> approverCovertMap(ApproveStatus approveStatus) {
        Map map = new HashMap();
        map.put("id", approveStatus.getId());
        map.put("transId", approveStatus.getTransId());
        map.put("appId", approveStatus.getAppId());
        map.put("serviceId", approveStatus.getServiceId());
        map.put("callingId", approveStatus.getCallingId());
        map.put("serviceCalling", approveStatus.getServiceCalling());
        map.put("applicant", approveStatus.getApplicant());
        map.put("approver", approveStatus.getApprover());
        map.put("reason", approveStatus.getReason());
        map.put("refuseReason", approveStatus.getRefuseReason());
        map.put("status", approveStatus.getStatus());
        return map;
    }

    /**
     * 解析配置Rest接口调用关系的xml文件
     *
     * @param content
     * @return
     */
    public List<Map> analysisXmlFile(String content) throws Exception {
        List<Map> list = new ArrayList<>();
        if (!StringUtils.isEmpty(content)) {
            Document doc = DocumentHelper.parseText(content);
            // 获取根元素
            Element root = doc.getRootElement();
            List<Element> beanList = root.elements(Dict.BEAN);
            if (CollectionUtils.isEmpty(beanList)) {
                return list;
            }
            // 获取元素信息
            for (Element bean : beanList) {
                // 获取beanid 判断是不是restUriMapping
                String beanId = bean.attributeValue(Dict.ID);
                if (!Dict.RESTURIMAPPING.equals(beanId)) {
                    continue;
                }
                // 解析bean id="restUriMapping"的结点
                list = getParamList(bean);
            }
        }

        return list;
    }

    /**
     * 解析bean id="restUriMapping"的结点
     *
     * @param bean
     * @return
     */
    public List<Map> getParamList(Element bean) {
        List<Map> list = new ArrayList<>();
        Element propsList = bean.element(Dict.PROPS);
        // 获取props的孩子节点
        List<Element> paramList = propsList.elements(Dict.PARAM);
        if (CollectionUtils.isNotEmpty(paramList)) {
            // 获取param的属性值
            for (Element param : paramList) {
                String url = param.attributeValue(Dict.NAME);
                if (!StringUtils.isEmpty(url)) {
                    String[] arr = url.split("//");
                    String[] array = arr[1].split("/");
                    String serviceProvider = array[0].toLowerCase();
                    String transId = array[2];
                    // 获取param的内容
                    Map map = new HashMap();
                    map.put("serviceId", serviceProvider);
                    map.put("transId", transId);
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * 获取待办负责人英文名列表
     *
     * @param devMap
     * @param spdbMap
     * @return
     */
    private String[] getUserNameEnList(Map devMap, Map spdbMap) {
        Set<String> set = new HashSet<>();
        List<String> devManagersEn = (List<String>) devMap.get("userNameEn");
        List<String> spdbManagersEn = (List<String>) spdbMap.get("userNameEn");
        if (!FileUtil.isNullOrEmpty(devManagersEn)) {
            for (String dev : devManagersEn) {
                set.add(dev);
            }
        }
        if (!FileUtil.isNullOrEmpty(spdbManagersEn)) {
            for (String spdb : spdbManagersEn) {
                set.add(spdb);
            }
        }
        String[] NameEns = set.toArray(new String[set.size()]);
        return NameEns;
    }


    /**
     * content：内容
     * type: 现存类型：versionUpdate,announce-halt,announce-update,process,version-refresh
     * target:用户英文名数组
     * hyperLink：链接
     */
    @Override
    public void sendFdevNotify(String content, String type, String[] target, String hyperLink, String desc) {
        Map param = new HashMap();
        param.put(Dict.CONTENT, content);
        param.put("target", target);
        param.put(Dict.TYPE, type);
        param.put("hyperlink", hyperLink);
        param.put("desc", desc);
        param.put(Dict.REST_CODE, "sendUserNotify");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户通知发送失败！" + e.getMessage());
        }
    }
}
