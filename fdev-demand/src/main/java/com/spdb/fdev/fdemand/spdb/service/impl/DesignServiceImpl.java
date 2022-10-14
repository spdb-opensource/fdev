package com.spdb.fdev.fdemand.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdemand.base.dict.*;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.DesignDoc;
import com.spdb.fdev.fdemand.spdb.notify.NotifyContext;
import com.spdb.fdev.fdemand.spdb.service.*;
import com.spdb.fdev.fdemand.spdb.unit.DesignUnit;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@RefreshScope
@Service
public class DesignServiceImpl implements DesignService {
    private Logger log = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Resource
    private IRoleService roleService;

    @Resource
    private IFdevUserService fdevUserService;

    @Resource
    private DesignUnit designUnit;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Resource
    private IDemandBaseInfoService demandService;

    @Resource
    private IDocService docService;
    
    @Autowired
    private RestTransport restTransport;
    
    @Autowired
    private NotifyContext notifyContext;
    
    @Value("${demand.sendUiDesign.isEmail}")
    private boolean isEmail;

    @Override
    public Map getDesignStateAndData(String demandId) throws Exception {
        //查询当前需求的状态
        DemandBaseInfo demand = demandBaseInfoDao.queryById(demandId);
        if (CommonUtils.isNullOrEmpty(demand)){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"需求id不正确，查询错误"});
        }
        //判断需求状态是否为已撤销、已投产、已归档
        if ((DemandEnum.DemandStatusEnum.PRODUCT.getValue() == demand.getDemand_status_normal() || DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue() == demand.getDemand_status_normal()
                || DemandEnum.DemandStatusEnum.IS_CANCELED.getValue() == demand.getDemand_status_normal()) && !(Dict.COMPLETEDAUDIT.equalsIgnoreCase(demand.getDesign_status()))){
            String design_status = UIEnum.UIDesignEnum.ABNORMALSHUTDOWN.getValue();
            demandBaseInfoDao.updateDesign_status(demandId,design_status);
            demand.setDesign_status(UIEnum.UIDesignEnum.ABNORMALSHUTDOWN.getValue());
        }
        //判断是否需要构造数据
        if (!Dict.NORRLATE.equals(demand.getDesign_status()) && CommonUtils.isNullOrEmpty(demand.getDesignMap()) && !Dict.ABNORMALSHUTDOWN.equals(demand.getDesign_status())) {
            Map map = buildForOldData(demand);
            log.info("老数据，需要构造map:{}", JSON.toJSONString(map, true));
            demand.setDesignMap(map);
        }
        //存在审核人，返回审核人相关信息
        Map<Object, Object> result = new HashMap<>();
        String reportId = demand.getUi_verify_user();
        if (!CommonUtils.isNullOrEmpty(reportId)) {
            List userIds = new ArrayList();
            userIds.add(reportId);
            Map usersMap = roleService.queryByUserCoreData(userIds);
            result.put("reviewer", usersMap.get(reportId));
        }
        Map groupInfo = designUnit.getGroupInfo(new HashMap() {{
            put(Dict.ID, demand.getDemand_leader_group());
        }});
        result.put("demand", demand);
        result.put("group", groupInfo);
        return result;
    }

    //String newStage, String stage, String name, String date, String remark
    @Override
    //未上传 uploadNot、已上传：uploaded、待审核：auditWait、审核中：auditIn、审核通过：auditPass、审核不通过：auditPassNot、审核完成：completedAudit
    //对应 wait_upload  => uploaded => wait_allot => fixing => nopass / finished
    public Long updateDesignState(String demandId, Map<String, String> param) throws Exception {
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
        String designState = param.get("newStatus");
        designUnit.verifyRoleAndStage(param, designState);
        DemandBaseInfo demand = demandBaseInfoDao.queryById(demandId);
        //校验是否更新的阶段是下一个阶段
        designUnit.verifyIsNextStage(demand, designState,  param);
        designState = param.get("newStatus");
        Long result = 0L;
        //ui设计稿审核人 不为空的话
        String uiDesignReporter = param.get(Dict.UIDESIGNREPORTER);
        if (!CommonUtils.isNullOrEmpty(uiDesignReporter)) {
            //修改设计稿审核人，并且发送邮件通知  <----此时添加的代办类型是infoTo，id是需求id---->
            demandService.updateRqrmntUiReporter(demandId, uiDesignReporter);
            //更新infoMsg
            //发送代办
            designUnit.deleteNotify(Arrays.asList(user.getId()), "infoTo",demand.getId());
            designUnit.deleteNotify(Arrays.asList(user.getId()), "infoMsg",demand.getId());
            designUnit.sendToList(demand, Arrays.asList(uiDesignReporter), "设计还原审核需求审核！", "check");
        }
        //更新阶段，更新建议
        if ("uploaded".equals(designState)) {
            Map<String, List<Map<String, String>>> designMap = designUnit.getStringListMap(param, demand, "uploaded");
            log.info("uploaded阶段更新designMap:{}", JSON.toJSONString(designMap, true));
            result = demandBaseInfoDao.updateDesignAndMap(demandId, param.get("newStatus"), designMap, user.getId());
        }
        if ("auditWait".equals(designState)) {
            Map<String, List<Map<String, String>>> designMap = designUnit.getStringListMap(param, demand, "wait_allot");
            log.info("auditWait阶段更新designMap:{}", JSON.toJSONString(designMap, true));
            result = demandBaseInfoDao.updateDesignAndMap(demandId, param.get("newStatus"), designMap, demand.getUploader());
            if (result >= 1) {
                //如果修改的状态是auditWait 待审核，需通知所有 具有 UI团队负责人 权限的人员
                //此时添加的代办是infoMsg  id 是需求编号
                designUnit.sendNotify(demand);
                if (isEmail) {
                	Map<String, Object> param1 = CommonUtils.object2Map(demand);
                	notifyContext.notifyContext(Dict.UiUploadedImpl, param1);
				}
            }
            String firstUploader = demand.getUploader();
            demandBaseInfoDao.updateUploader(firstUploader,demandId,demand.getUploader());
        }
        if ("auditIn".equals(designState)  && !"load_upload".equals(param.get("stage"))) {
            Map<String, List<Map<String, String>>> designMap = designUnit.getStringListMap(param, demand, "fixing");
            log.info("auditIn阶段更新designMap:{}", JSON.toJSONString(designMap, true));
            result = demandBaseInfoDao.updateDesignAndMap(demandId, param.get("newStatus"), designMap, demand.getUploader());
            if (isEmail) {
            	Map<String, Object> param1 = CommonUtils.object2Map(demand);
            	notifyContext.notifyContext(Dict.UiAuditWaitImpl, param1);
			}
        }
        if ("completedAudit".equals(designState)) {
            Map<String, List<Map<String, String>>> designMap = designUnit.getStringListMap(param, demand, "finished");
            log.info("completedAudit阶段更新designMap:{}", JSON.toJSONString(designMap, true));
            result = demandBaseInfoDao.updateDesignAndMap(demandId, param.get("newStatus"), designMap, demand.getUploader());
            //删除check代办
            designUnit.deleteNotify(Arrays.asList(user.getId()), "check",demand.getId());
            designUnit.deleteNotify(Arrays.asList(user.getId()), "infoTo",demand.getId());
            if (isEmail) {
            	if (CommonUtils.isNullOrEmpty(demand.getDesignMap().get("nopass"))) {
            		Map<String, Object> param1 = CommonUtils.object2Map(demand);
            		notifyContext.notifyContext(Dict.UiAuditPassImpl, param1);
				}
			}
        }
        if ("auditPassNot".equals(designState)) {
            Map<String, List<Map<String, String>>> designMap = designUnit.getNopassMap(param, demand, "nopass");
            log.info("auditPassNot阶段更新designMap:{}", JSON.toJSONString(designMap, true));
            result = demandBaseInfoDao.updateDesignAndMap(demandId, param.get("newStatus"), designMap, demand.getUploader());
            //发送新的代办 param.get("stage") 发送设计师上传新的设计稿的代办
            if ("load_nopass".equals(param.get("stage"))) {
                //接着通知当前设计师 并且清楚自己的代办
            	designUnit.deleteNotify(Arrays.asList(user.getId()), "check",demand.getId());
                designUnit.sendToList(demand, Arrays.asList(demand.getUi_verify_user()), "请上传新的设计稿！", "infoTo");
            }
            if (isEmail) {
            	Map<String, Object> param1 = CommonUtils.object2Map(demand);
            	notifyContext.notifyContext(Dict.UiAuditPassNotImpl, param1);
			}
        }
        if ("auditIn".equals(designState)  && "load_upload".equals(param.get("stage"))){
        	if (!user.getId().equals(demand.getUi_verify_user())) {
        		throw new FdevException(ErrorConstants.UI_VERIFY_USER_ERROR, new String[]{"必须为审核该需求Ui设计稿的审核人"});
			}
            Map<String, List<Map<String, String>>> designMap = designUnit.getNopassMap(param, demand, "nopass");
            log.info("auditPassNot阶段更新designMap:{}", JSON.toJSONString(designMap, true));
            result = demandBaseInfoDao.updateDesignAndMap(demandId, param.get("newStatus"), designMap, demand.getUploader());
            //发送新的代办 param.get("stage") 发送设计师上传新的设计稿的代办
            //接着通知当前设计师 并且清楚自己的代办
            designUnit.deleteNotify(Arrays.asList(user.getId()), "check",demand.getId());
            designUnit.sendToList(demand, Arrays.asList(demand.getUi_verify_user()), "请上传新的设计稿！", "infoTo");
        }
        return result;
    }


    @Override
    public Long updateDesignRemark(String demandId, String remark) {
    	User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                 .getSession().getAttribute("_USER");
        DemandBaseInfo demand = demandBaseInfoDao.queryById(demandId);
        //删除infoTo代办
        designUnit.deleteNotify(Arrays.asList(user.getId()), "infoTo",demand.getId());
        designUnit.sendToList(demand, Arrays.asList(demand.getUploader()), "设计还原审核未通过！", "nopass");
        return demandBaseInfoDao.updateDesignRermark(demandId, remark);
    }

    public Map buildForOldData(DemandBaseInfo demand) {
        String designState = demand.getDesign_status();
        //对应wait_upload
        if ("uploadNot".equals(designState)) {
            return buildForStage("wait_upload");
        }
        //对应uploaded
        if ("uploaded".equals(designState)) {
            Map map = buildForStage("wait_upload");
            map.putAll(buildForStage("uploaded"));
            return map;
        }
        //wait_allot 对应
        if ("auditWait".equals(designState)) {
            Map map = buildForStage("wait_upload");
            map.putAll(buildForStage("uploaded"));
            map.putAll(buildForStage("wait_allot"));
            return map;
        }
        //对应fixing
        if ("auditIn".equals(designState)) {
            Map map = buildForStage("wait_upload");
            map.putAll(buildForStage("uploaded"));
            map.putAll(buildForStage("wait_allot"));
            map.putAll(buildForFixing(demand.getUi_verify_user()));
            return map;
        }
        //对应auditPassNot
        if ("nopass".equals(designState)) {
            Map map = buildForStage("wait_upload");
            map.putAll(buildForStage("uploaded"));
            map.putAll(buildForStage("wait_allot"));
            map.putAll(buildForFixing(demand.getUi_verify_user()));
            map.putAll(buildForStage("nopass"));
            return map;
        }
        //auditPass和completedAudit 一样 finished
        Map map = buildForStage("wait_upload");
        map.putAll(buildForStage("uploaded"));
        map.putAll(buildForStage("wait_allot"));
        map.putAll(buildForFixing(demand.getUi_verify_user()));
        map.putAll(buildForStage("nopass"));
        map.putAll(buildForStage("finished"));
        return map;
    }

    //未上传 uploadNot、已上传：uploaded、待审核：auditWait、审核中：auditIn、审核通过：auditPass、审核不通过：auditPassNot、审核完成：completedAudit
    //对应 wait_upload  => uploaded => wait_allot => fixing => nopass / finished
    public Map buildForStage(String stage) {
        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        designMap.put(stage, Arrays.asList(buildMap(Arrays.asList("name", "time", "remark"),
                Arrays.asList("", "", ""))));
        return designMap;
    }

    //对应 wait_upload  => uploaded => wait_allot => fixing => nopass / finished
    public Map buildForFixing(String ui_verify_user) {
        String name = "";
        if (StringUtils.isNotBlank(ui_verify_user)) {
            Map map = fdevUserService.queryUserInfo(ui_verify_user);
            name = (String) map.get("user_name_cn");
        }

        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        designMap.put("fixing", Arrays.asList(buildMap(Arrays.asList("name", "time", "remark"),
                Arrays.asList(name, "", ""))));
        return designMap;
    }

    public <K, V> Map buildMap(List<K> keys, List<V> values) {
        return keys.stream()
                .collect(Collectors.toMap(
                        key -> key, key -> values.get(keys.indexOf(key))));
    }


    @Override
    public DemandBaseInfo uploadDesignFile(MultipartFile file, String fileName, String demandId, String fileType, String uploadStage, String remark) throws Exception {
        //上传到minio
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
        //准备上传
        String pathMinio = demandId + "/" + fileType + "/" + uploadStage + "/" + fileName;
        boolean result = this.docService.uploadFiletoMinio("fdev-design", pathMinio, file, user);
        //上传完毕后更新任务数据库
        if (result) {
            DemandBaseInfo demand = this.demandBaseInfoDao.queryById(demandId);
            DesignDoc designDoc = new DesignDoc();
            designDoc.setDocType(fileType);
            designDoc.setFileName(fileName);
            designDoc.setUploadStage(uploadStage);
            designDoc.setMinioPath(pathMinio);
            List<DesignDoc> designDocs = Optional.ofNullable(demand.getDesignDoc()).orElse(new ArrayList<>());
            //首次上传以及首次重复上传处理
            designDocs = designDocs.stream().filter((designDoc1) -> !(uploadStage.equals(designDoc1.getUploadStage()) && fileType.equals(designDoc1.getDocType()))).collect(Collectors.toList());
            designDocs.add(designDoc);
            if ("0".equals(uploadStage)) {
                //更新阶段和desinMap
                DemandBaseInfo updateEntity = new DemandBaseInfo();
                updateEntity.setId(demandId);
                Map<String, List<Map<String, String>>> designMap = Optional
                        .ofNullable(demand.getDesignMap())
                        .orElse(new HashMap<>());
                //构造upload阶段数据
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("name", user.getUser_name_cn());
                dataMap.put("time", CommonUtils.dateFormat(new Date(), CommonUtils.STANDARDDATEPATTERN));
                dataMap.put("remark", remark);
                designMap.put("uploaded", Arrays.asList(dataMap));
                updateEntity.setDesignMap(designMap);
                if("uploaded".equals(demand.getDesign_status()) || "uploadNot".equals(demand.getDesign_status())) {
                	updateEntity.setDesign_status("uploaded");
                }
                updateEntity.setDesignDoc(designDocs);
                updateEntity.setUploader(user.getId());
                DemandBaseInfo update = this.demandBaseInfoDao.update(updateEntity);
                log.info("upload阶段上传=>update={}", JSON.toJSONString(updateEntity, true));
                return update;
            }
            DemandBaseInfo updateEntity = new DemandBaseInfo();
            updateEntity.setId(demandId);
            updateEntity.setDesignDoc(designDocs);
            DemandBaseInfo update = this.demandBaseInfoDao.update(updateEntity);
            log.info("更新设计还原文档完毕结果=>update={}{}", JSON.toJSONString(designDocs, true));
            return update;
        }
        throw new FdevException(ErrorConstants.DOC_ERROR, new String[]{"上传文件失败,请重试!"});
    }

    @Override
    public String batchUpdateTaskDoc(String moduleName) throws Exception {
        //拉取所有文档
        List<DemandBaseInfo> demandBaseInfos = demandBaseInfoDao.queryTaskForDesign();
        log.info("需要同步的文档数量:{}",demandBaseInfos.size());
        //任务分区
        AtomicInteger atomicInteger = new AtomicInteger(0);
        demandBaseInfos.forEach(task->{
            designUnit.dealWpsFile(task);
            log.info("第{}处理完毕",atomicInteger.addAndGet(1));
        });
        return null;
    }
    
    @Override
    public List<Map<String,Object>> queryReviewDetailList(
            String reviewer, List<String> group, String startDate, String endDate, String internetChildGroupId) throws Exception{
        //未传时间就取当前月份起始和终止日期
        Map<String, String> initDate = getInitDate(startDate, endDate);
        String startDate2 = initDate.get(Dict.FIRSTDATE) + " 00:00:00";
        String endDate2 = initDate.get(Dict.LASTDATE) + " 23:59:59";
        //选择了互联网应用下的某个组的子组后，自动填充所选择的互联网应用子组
        List<String> groupIds = new ArrayList<>();
        groupIds.add(internetChildGroupId);
        if(!CommonUtils.isNullOrEmpty(group)) {
            for(String groupId : group) {
                Map<String, Object> param = new HashMap<>();
                param.put("id", groupId);
                param.put(Dict.REST_CODE,"queryChildGroupById");
                List<Map> result = (List<Map>) restTransport.submit(param);
                result = result.stream().filter(map -> "1".equals(map.get(Dict.STATUS))).collect(Collectors.toList());
                groupIds.addAll(result.stream().map(map -> (String)map.get(Dict.ID)).collect(Collectors.toList()));
            }
        }
        List<DemandBaseInfo> demandBaseInfos = demandBaseInfoDao.queryReviewDetailLists(reviewer, groupIds, startDate2, endDate2);
        if(CommonUtils.isNullOrEmpty(demandBaseInfos)){
            log.info("设计稿审核查询为空");
            return null;
        }
        //获取审核次数、当前阶段及时间和完成情况数据，并设置到需求信息中
        demandBaseInfos.forEach(demandInfo -> {
            getCheckCount(demandInfo);
            try {
                getCurrenStage(demandInfo, endDate2);
            } catch (Exception e) {
                log.info(">>>getCurrenStage fail :"+demandInfo.getId()+",err:"+e.getMessage());
            }
            getFinished(demandInfo, endDate2);
        });
        //组装ui审核发起人和研发单元牵头人姓名
        Set<String> ids = new HashSet<>();
        demandBaseInfos.forEach(demandBaseInfo -> {
            if(!CommonUtils.isNullOrEmpty(demandBaseInfo.getFirstUploader())) {
                ids.add(demandBaseInfo.getFirstUploader());
            }
        });
        Map userInfoMap = roleService.queryByUserCoreData(Arrays.asList(ids.toArray()));
        if(!CommonUtils.isNullOrEmpty(userInfoMap)) {
            demandBaseInfos.forEach(demandBaseInfo -> {
                if(!CommonUtils.isNullOrEmpty(userInfoMap.get(demandBaseInfo.getFirstUploader()))) {
                    Map<String,Object> userInfo = (Map<String, Object>) userInfoMap.get(demandBaseInfo.getFirstUploader());
                    demandBaseInfo.setFirstUploader((String) userInfo.get(Dict.USER_NAME_CN));
                }
            });
        }
        //按group分组
        Map<String,List<DemandBaseInfo>> uiResult = demandBaseInfos.stream().collect(Collectors.groupingBy(DemandBaseInfo::getDemand_leader_group));
        if(CommonUtils.isNullOrEmpty(uiResult)){ 
            log.info("设计稿审核查询为空");
            return null;
        }
        //处理返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        dealResult(result,uiResult);
        //排序
        result.sort(Comparator.comparing(p->(String)p.get("sortNum")));
        return result;
    }
    
    /**
     * 获取筛选时间
     * @return
     */
    private Map<String,String> getInitDate(String mixDate,String maxDate){
        Map<String, String> result = new HashMap<>();
        if(!CommonUtils.isNullOrEmpty(mixDate) && !CommonUtils.isNullOrEmpty(maxDate)){
            result.put("firstDate",mixDate);
            result.put("lastDate",maxDate);
            return result;
        }else if (CommonUtils.isNullOrEmpty(mixDate) && CommonUtils.isNullOrEmpty(maxDate)) {
        	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Dict.DATE_PATTERN);
        	Calendar cale = Calendar.getInstance();
        	cale.add(Calendar.MONTH,0);
        	cale.set(Calendar.DAY_OF_MONTH,1);
        	result.put("firstDate",simpleDateFormat.format(cale.getTime()));
        	cale = Calendar.getInstance();
        	cale.add(Calendar.MONTH,1);
        	cale.set(Calendar.DAY_OF_MONTH,0);
        	result.put("lastDate",simpleDateFormat.format(cale.getTime()));
        	return result;
		}else {
			throw new FdevException(ErrorConstants.TIME_CANNOT_BE_EMPTY, new String[]{"开始时间和结束时间"});
		}
    }
    
    /**
     * 获取待分配时间
     * @return
     */
    private String getWailAllotTime(DemandBaseInfo demandBaseInfo){        
    	Map<String, List<Map<String, String>>> designMap = demandBaseInfo.getDesignMap();
        if(CommonUtils.isNullOrEmpty(designMap))return "";
        List<Map<String, String>> wait_allot = designMap.get("wait_allot");
        if(CommonUtils.isNullOrEmpty(wait_allot))return "";
        Map<String, String> waitAllotMap = wait_allot.get(0);
        if(CommonUtils.isNullOrEmpty(waitAllotMap))return "";
        return waitAllotMap.get("time");
    }
    
    /**
     * 获取设计稿审核结束时间
     * @return
     */
    private String getFinishedTime(DemandBaseInfo demandBaseInfo){
        if(CommonUtils.isNullOrEmpty(demandBaseInfo.getDesignMap())){
            return "";
        }
        List<Map<String, String>> finished = demandBaseInfo.getDesignMap().get("finished");
        //还在审核中，取当前时间。否则取审核结束时间
        if(CommonUtils.isNullOrEmpty(finished) || CommonUtils.isNullOrEmpty(finished.get(0)) || CommonUtils.isNullOrEmpty(finished.get(0).get("time"))){
            return CommonUtils.dateFormat(new Date(),Dict.DATE_PATTERN);
        }
        return finished.get(0).get("time");
    }

    /**
     * 获取审核轮数
     * @param demandBaseInfo
     * @return
     */
    public int getCheckCount(DemandBaseInfo demandBaseInfo) {
        Map<String,List<Map<String,String>>> designMap = demandBaseInfo.getDesignMap();
        int count = 0;
        //有finished(审核通过)代表一轮
        if(!CommonUtils.isNullOrEmpty(designMap.get(Dict.FINISHED))) {
            ++count;
        }
        //如果有nopass，nopass下的数据集合中，有几条数据的stage为load_nopass(审核未通过)，那就再加几轮
        List<Map<String,String>> nopassList = designMap.get(Dict.NOPASS);
        if(!CommonUtils.isNullOrEmpty(nopassList)) {
            for(Map<String,String> nopass : nopassList) {
                if(Constants.STAGE_LOADNOPASS.equals(nopass.get(Dict.STAGE))) {
                    ++count;
                }
            }
        }
        demandBaseInfo.setCheckCount(String.valueOf(count));
        return count;
    }

    /**
     * 获取当前审核阶段
     * @param demandBaseInfo
     * @return
     */
    public String getCurrenStage(DemandBaseInfo demandBaseInfo, String endDate2) {
        Map<String,List<Map<String,String>>> designMap = new HashMap<>(demandBaseInfo.getDesignMap());
        //将审核记录中，每一个步骤的时间晚于查询结束时间的剔除
        List<Map<String, String>> finishedList = designMap.get(Dict.FINISHED);
        if(!CommonUtils.isNullOrEmpty(finishedList)) {
            Map<String,String> finished = finishedList.get(0);
            if(finished.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.FINISHED);
            }
        }
        List<Map<String,String>> nopassList = designMap.get(Dict.NOPASS);
        String lastStage = "";//审核记录中nopass不晚于查询结束时间的记录中最后一条数据的stage
        String lastStageTime = "";//审核记录中nopass不晚于查询结束时间的记录中最后一条数据的time
        if(!CommonUtils.isNullOrEmpty(nopassList)) {
            //将noPass记录中，时间晚于查询结束时间的剔除
            nopassList = nopassList.stream().filter(map -> endDate2.compareTo((String)map.get(Dict.TIME)) >= 0).collect(Collectors.toList());
            if(CommonUtils.isNullOrEmpty(nopassList)) {
                designMap.remove(Dict.NOPASS);
            }else {
                lastStage = nopassList.get(nopassList.size()-1).get(Dict.STAGE);
                lastStageTime = nopassList.get(nopassList.size()-1).get(Dict.TIME);
            }
        }
        List<Map<String, String>> fixingList = designMap.get(Dict.FIXING);
        if(!CommonUtils.isNullOrEmpty(fixingList)) {
            Map<String,String> fixing = fixingList.get(0);
            if(fixing.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.FIXING);
            }
        }
        List<Map<String, String>> waitAllotList = designMap.get(Dict.WAIT_ALLOT);
        if(!CommonUtils.isNullOrEmpty(waitAllotList)) {
            Map<String,String> waitAllot = waitAllotList.get(0);
            if(waitAllot.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.WAIT_ALLOT);
            }
        }
        List<Map<String, String>> uploadedList = designMap.get(Dict.UPLOADED);
        if(!CommonUtils.isNullOrEmpty(uploadedList)) {
            Map<String,String> uploaded = uploadedList.get(0);
            if(uploaded.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.UPLOADED);
            }
        }
        //当前审核状态，就是满足时间条件后的最后一个状态
        List<String> keys = new ArrayList<>(designMap.keySet());
        //因为前面按时间过滤过了，这里的数据，至少有一条是时间满足的
        String reviewStatus = "";
        if(keys.contains(Constants.REVIEWSTATUS_FINISHED)) {
            reviewStatus = Constants.REVIEWSTATUS_FINISHED;
        }else if(keys.contains(Constants.REVIEWSTATUS_NOPASS)) {
            reviewStatus = Constants.REVIEWSTATUS_NOPASS;
        }else if(keys.contains(Constants.REVIEWSTATUS_FIXING)) {
            reviewStatus = Constants.REVIEWSTATUS_FIXING;
        }else if(keys.contains(Constants.REVIEWSTATUS_WAITALLOT)) {
            reviewStatus = Constants.REVIEWSTATUS_WAITALLOT;
        }else {
            log.info(">>>getCurrenStage数据异常"+demandBaseInfo.getId());
        }

        //分配中
        ////开发发起ui还原性申请，未分配审核人员，审核状态标志为wait_allot；
        if(Constants.REVIEWSTATUS_WAITALLOT.equals(reviewStatus)) {
            demandBaseInfo.setCurrentStageTime(designMap.get(reviewStatus).get(0).get(Dict.TIME));
            demandBaseInfo.setCurrentStage(Constants.REVIEWSTAGE_ALLOTING);
            return Constants.REVIEWSTAGE_ALLOTING;
        }
        //审核中
        ////分配审核人员后，但未审核完成，审核状态标志为fixing，且分配时间早于查询结束时间；
        if(Constants.REVIEWSTATUS_FIXING.equals(reviewStatus)) {
            demandBaseInfo.setCurrentStageTime(designMap.get(reviewStatus).get(0).get(Dict.TIME));
            demandBaseInfo.setCurrentStage(Constants.REVIEWSTAGE_CHECKING);
            return Constants.REVIEWSTAGE_CHECKING;
        }
        ////开发人员二次及多次（非第一次）上传文件后，未审核完成，审核状态标志为nopass，且审核记录中nopass最后一条数据的stage为load_upload
        if(Constants.REVIEWSTATUS_NOPASS.equals(reviewStatus)
                && Constants.STAGE_LOADUPLOAD.equals(lastStage)) {
            demandBaseInfo.setCurrentStageTime(lastStageTime);
            demandBaseInfo.setCurrentStage(Constants.REVIEWSTAGE_CHECKING);
            return Constants.REVIEWSTAGE_CHECKING;
        }
        //修改中
        ////ui审核拒绝后，开发人员未上传材料，审核状态标志为nopass，且审核记录中nopass最后一条数据的stage为load_nopass。
        if(Constants.REVIEWSTATUS_NOPASS.equals(reviewStatus)
                && Constants.STAGE_LOADNOPASS.equals(lastStage)) {
            demandBaseInfo.setCurrentStageTime(lastStageTime);
            demandBaseInfo.setCurrentStage(Constants.REVIEWSTAGE_UPDATEING);
            return Constants.REVIEWSTAGE_UPDATEING;
        }
        //完成
        ////审核通过，审核状态标志为finished
        if(Constants.REVIEWSTATUS_FINISHED.equals(reviewStatus)) {
            demandBaseInfo.setCurrentStageTime(designMap.get(reviewStatus).get(0).get(Dict.TIME));
            demandBaseInfo.setCurrentStage(Constants.REVIEWSTAGE_FINISH);
            return Constants.REVIEWSTAGE_FINISH;
        }
        return "";
    }

    /**
     * 获取完成情况
     * 先看任务是否有finished，再看finished的时间是否晚于查询结束时间
     * @param demandBaseInfo
     * @param endDate2
     * @return
     */
    public boolean getFinished(DemandBaseInfo demandBaseInfo, String endDate2) {
        Map<String,List<Map<String,String>>> designMap = demandBaseInfo.getDesignMap();
        List<Map<String,String>> finishedList = designMap.get(Dict.FINISHED);
        if(CommonUtils.isNullOrEmpty(finishedList)) {
            demandBaseInfo.setFinshFlag(Constants.FINSHFLAG_NOPASS);
            return false;
        }else {
            String finishedTime = finishedList.get(0).get(Dict.TIME);
            if(finishedTime.compareTo(endDate2) > 0) {
                demandBaseInfo.setFinshFlag(Constants.FINSHFLAG_NOPASS);
                return false;
            }
        }
        demandBaseInfo.setFinshFlag(Constants.FINSHFLAG_PASS);
        return true;
    }

    /**
     * 处理返回结果
     */
    private void dealResult(List<Map<String, Object>> result,Map<String, List<DemandBaseInfo>> uiResult){
        Map<String, Object> groupParam = new HashMap<>();
        Iterator<Map.Entry<String, List<DemandBaseInfo>>> iterator = uiResult.entrySet().iterator();
        while (iterator.hasNext()){
            Map<String, Object> resultByGruop = new HashMap<>();
            List<DemandBaseInfo> finishedDemand = new ArrayList<>();
            List<DemandBaseInfo> unFinishedDemand = new ArrayList<>();
            Map.Entry<String, List<DemandBaseInfo>> next = iterator.next();
            groupParam.put("id",next.getKey());
            Map queryGroup = null;
            try {
                queryGroup = demandBaseInfoDao.queryGroup(groupParam);
            } catch (Exception e) {
                log.error("查询小组信息异常,组id:" + next.getKey());
                throw new FdevException(ErrorConstants.USR_AUTH_FAIL,new String[]{"查询小组信息异常"});
            }
            if(CommonUtils.isNullOrEmpty(queryGroup)){
                log.error("查询组信息为空,组id:" + next.getKey());
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"查询小组信息为空"});
            }
            //板块名称
            String groupName = (String)queryGroup.get("name");
            //排序字段
            String sortNum = (String)queryGroup.get("sortNum");
            resultByGruop.put("group",next.getKey());
            resultByGruop.put("groupName",groupName);
            resultByGruop.put("sortNum",sortNum);
            //按已完成、未完成分为两个list
            divideByFinished(next.getValue(),finishedDemand,unFinishedDemand);
            resultByGruop.put("finishedList",finishedDemand);
            resultByGruop.put("unfinishedList",unFinishedDemand);
            result.add(resultByGruop);
        }
    }
    
    /**
     * 设计稿审核是否完成分类
     * @param demandBaseInfos
     */
    private void divideByFinished(List<DemandBaseInfo> demandBaseInfos,List<DemandBaseInfo> finishedDemand,List<DemandBaseInfo> unFinishedDemand){
    	demandBaseInfos.forEach(p ->{
            List<Map<String, String>> finished = p.getDesignMap().get("finished");
            if(CommonUtils.isNullOrEmpty(finished) || CommonUtils.isNullOrEmpty(finished.get(0)) || CommonUtils.isNullOrEmpty(finished.get(0).get("time"))){
                unFinishedDemand.add(p);
            }else {
                finishedDemand.add(p);
            }
        });
    }

    @Override
    public void downLoadReviewList(HttpServletResponse response, String reviewer, List<String> group, String startDate, String endDate,
                                   Map<String, String> columnMap, String internetChildGroupId) throws Exception {
        excel(response, queryReviewDetailList(reviewer,group,startDate,endDate,internetChildGroupId), columnMap);
    }

    /**
     * 创建Excel并插入数据
     * @param response
     * @param dataList
     * @param columnMap
     * @throws Exception
     */
    public void excel(HttpServletResponse response, List<Map<String, Object>> dataList, Map<String,String> columnMap) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet1");
        XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //合并单元格，第一列项目小组其实占了小组和完成情况及数量两列
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,1));
        //主体内容，按小组区分，一个小组一大块
        int rowNum = 1;
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        for (int i = 0; i < dataList.size(); i++) {
            //审核通过任务数量
            int finishedRowNum = ((List)dataList.get(i).get(Dict.FINISHEDLIST)).size();
            //未审核通过任务数量
            int unFinishedRowNum = ((List)dataList.get(i).get(Dict.UNFINISHEDLIST)).size();
            //小组占用的行数，是小组下所有任务的条数
            if(finishedRowNum+unFinishedRowNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum,finishedRowNum+unFinishedRowNum+rowNum-1,0,0));
            }
            if(finishedRowNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum,finishedRowNum+rowNum-1,1,1));
            }
            if(unFinishedRowNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(finishedRowNum+rowNum,finishedRowNum+unFinishedRowNum+rowNum-1,1,1));
            }
            rowNum += finishedRowNum + unFinishedRowNum;
        }

        int excelRow = 0;
        Row titleRow = sheet.createRow(excelRow++);
        //表头
        List<String> titleList = new ArrayList<>(columnMap.values());
        for (int i = 0; i < titleList.size(); i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(titleList.get(i));
        }
        List<String> keyList = new ArrayList<>(columnMap.keySet());
        //数据
        List<DemandBaseInfo> demandList = null;
        for (int i = 0; i < dataList.size(); i++) {
            demandList = new ArrayList<>();
            demandList.addAll((List<DemandBaseInfo>) dataList.get(i).get(Dict.FINISHEDLIST));
            demandList.addAll((List<DemandBaseInfo>) dataList.get(i).get(Dict.UNFINISHEDLIST));
            //审核通过任务数量
            int finishedRowNum = ((List)dataList.get(i).get(Dict.FINISHEDLIST)).size();
            //未审核通过任务数量
            int unFinishedRowNum = ((List)dataList.get(i).get(Dict.UNFINISHEDLIST)).size();
            for (int j = 0; j < demandList.size(); j++) {
                Map demand = CommonUtils.beanToMap(demandList.get(j));
                //行
                Row dataRow = sheet.createRow(excelRow++);
                Cell cell = null;
                for (int k = 0; k < keyList.size(); k++) {
                    //单元格
                    cell = dataRow.createCell(k);
                    String key = keyList.get(k);
                    String text = "";
                    if(Dict.GROUPNAME.equals(key)) {
                        cell.setCellStyle(cellStyle);
                        text = (String)dataList.get(i).get(key);
                        cell.setCellValue(text+"\r\n总数\r\n"+(finishedRowNum+unFinishedRowNum));
                    }else if(Dict.NUM.equals(key)) {
                        cell.setCellStyle(cellStyle);
                        //项目小组下面还有一列，展示各完成情况的任务数量
                        if(j < finishedRowNum) {
                            cell.setCellValue("通过\r\n"+finishedRowNum);
                        }else {
                            cell.setCellValue("未通过\r\n"+unFinishedRowNum);
                        }
                    }else {
                        text = (String) demand.get(key);
                        if(Constants.SEARCHKEY_POSITIONSTATUS.equals(key)) {
                            text = Constants.POSITIONSTATUS_OK.equals(text) ? "正常" : "失败";
                        }else if(Constants.SEARCHKEY_FINSHFLAG.equals(key)) {
                            text = Constants.FINSHFLAG_PASS.equals(text) ? "通过" : "未通过";
                        }else if(Constants.SEARCHKEY_CURRENTSTAGE.equals(key)) {
                            if(Constants.REVIEWSTAGE_ALLOTING.equals(text)) {
                                text = "分配中";
                            }
                            if(Constants.REVIEWSTAGE_CHECKING.equals(text)) {
                                text = "审核中";
                            }
                            if(Constants.REVIEWSTAGE_UPDATEING.equals(text)) {
                                text = "修改中";
                            }
                            if(Constants.REVIEWSTAGE_FINISH.equals(text)) {
                                text = "完成";
                            }
                        }else if(Constants.SEARCHKEY_CHECKCOUNT.equals(key)) {
                            text = text+"轮";
                        }
                        cell.setCellValue(text);
                    }
                }
            }
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode("需求进度详情","UTF-8")+".xlsx");
        wb.write(response.getOutputStream());
    }
}
