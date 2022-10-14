package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.dao.IReleaseCycleDao;
import com.spdb.fdev.release.dao.IReleaseNodeDao;
import com.spdb.fdev.release.dao.IReleaseRqrmntInfoDao;
import com.spdb.fdev.release.dao.IReleaseTaskDao;
import com.spdb.fdev.release.entity.GroupAbbr;
import com.spdb.fdev.release.entity.ReleaseCycle;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseRqrmnt;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.transport.RestTransport;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class ReleaseRqrmntInfoServiceImpl implements IReleaseRqrmntInfoService {

    private static Logger logger = LoggerFactory.getLogger(ReleaseRqrmntInfoServiceImpl.class);

    @Autowired
    private IReleaseRqrmntInfoDao releaseRqrmntInfoDao;
    @Autowired
    private ITaskService taskService;
    @Autowired
    private IReleaseRqrmntService releaseRqrmntService;
    @Autowired
    private IUserService userService;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IReleaseNodeService releaseNodeService;
    @Autowired
    private IReleaseTaskService releaseTaskService;
    @Autowired
    private ISendEmailService sendEmailService;
    @Autowired
    private IReleaseNodeDao releaseNodeDao;
    @Autowired
    private IReleaseTaskDao releaseTaskDao;
    @Autowired
    private IReleaseCycleDao releaseCycleDao;
    @Autowired
    private IAppService appService;
    @Autowired
    private IFileService fileService;
    @Autowired
    IReleaseApplicationService releaseApplicationService;
    @Autowired
    private IGroupAbbrService groupAbbrService;
    @Autowired
	private IProdApplicationDao prodApplicationDao;
    @Value("${scripts.path}")
    private String scripts_path;
    @Value("${upload.local.rqrmntdir}")
    String localdir;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${demand.file.minio.uri}")
    private String minioUri;
    @Value("${release.rqrmnt.minio.url}")
    private String minioUrl;
    @Override
    public ReleaseRqrmntInfo queryReleaseRqrmntInfo(String release_date, String rqrmnt_no, String group_id) throws Exception {
        return releaseRqrmntInfoDao.queryReleaseRqrmntInfo(release_date, rqrmnt_no, group_id);
    }

    @Override
    public void saveReleaseRqrmntInfo(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        releaseRqrmntInfoDao.saveReleaseRqrmntInfo(releaseRqrmntInfo);
    }

    @Override
    public ReleaseRqrmntInfo queryReleaseRqrmntInfoByTaskNo(String task_id) throws Exception {
        return releaseRqrmntInfoDao.queryReleaseRqrmntInfoByTaskNo(task_id);
    }

    @Override
    public void updateRqrmntInfo(String relase_date, String type, List<String> groupIds, HttpServletResponse resp) throws Exception {
        Set<String> groupIdSet = new HashSet<>();
        for (String groupId : groupIds) {
            List<Map<String, Object>> maps = userService.queryChildGroupByGroupId(groupId);
            Set<String> collect = maps.stream().map(e -> (String)e.get(Dict.ID)).collect(Collectors.toSet());
            groupIdSet.addAll(collect);
        }
        User user = CommonUtils.getSessionUser();
        List<ReleaseRqrmntInfo> releaseRqrmntInfoss = releaseRqrmntInfoDao.queryRqrmntInfoList(
                relase_date, "", groupIdSet);
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = new ArrayList<ReleaseRqrmntInfo>();
        Map<String, Map> maps = new HashMap<String, Map>();
        queryReleaseRqrmntInfoByTaskIds(releaseRqrmntInfoss,releaseRqrmntInfos,maps);
        List<ReleaseRqrmntInfo> excelDatas = new ArrayList<>();
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            //整包提测
            if("1".equals(type)){
                if("0".equals(queryRqrmntInfoPackageSubmitTest(releaseRqrmntInfo, maps))){
                    //需求未打包  不予修改
                    continue;
                }
                //业务需求需判断投产确认书情况
                if("2".equals(queryRqrmntInfoPackageSubmitTest(releaseRqrmntInfo, maps))){
                    //状态为已提测  则不再修改
                    continue;
                }
                if (!Constants.RQRMNT_TYPE_INNER.equals(releaseRqrmntInfo.getType())){
                //如果不是科技内部需求，需求确认书 已到达
                    if(!taskService.queryTaskReleaseConfirmDoc(releaseRqrmntInfo, maps)){
                        continue;
                    }
                }
                queryRqrmntSpdbManager(releaseRqrmntInfo);
                excelDatas.add(releaseRqrmntInfo);
            }else{
                //准生产提测
                if("1".equals(queryRqrmntInfoRelTest(releaseRqrmntInfo.getId(), maps))){
                    queryRqrmntSpdbManager(releaseRqrmntInfo);
                    excelDatas.add(releaseRqrmntInfo);
                }
            }
        }
        //存在需求已提测 导出需求列表
        if(!CommonUtils.isNullOrEmpty(excelDatas)){
            List list = prepareData(excelDatas);
            exportRqrmntTestList(list, resp);
        }else{
            throw new FdevException(ErrorConstants.RELEASE_RQRMNT_INFO_LIST_EMPTY);
        }
        fileService.putRarmntInfosMinioFile(excelDatas, type, user);
    }


    public void exportRqrmntTestList(List<Map> emailDatas, HttpServletResponse resp) throws Exception {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle blueStyle = workbook.createCellStyle();
        blueStyle.setAlignment(HorizontalAlignment.LEFT);
        blueStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setCellValue(workbook, 0, 0, 0, "小组名", blueStyle); 
        setCellValue(workbook, 0, 0, 1, "项目名称", blueStyle);
        setCellValue(workbook, 0, 0, 2, "联系单号", blueStyle);
        setCellValue(workbook, 0, 0, 3, "实施单元【ipmp实施单元编号/FDEV编号】", blueStyle);
        setCellValue(workbook, 0, 0, 4, "涉及系统", blueStyle);
        setCellValue(workbook, 0, 0, 5, "行内负责人", blueStyle);
        setCellValue(workbook, 0, 0, 6, "同步投产系统及新增或修改交易", blueStyle);
        setCellValue(workbook, 0, 0, 7, "业务联系人", blueStyle);
        CellStyle greenStyle = workbook.createCellStyle();
        greenStyle.setAlignment(HorizontalAlignment.LEFT);
        greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        int i = 1;
        for (Map map : emailDatas) {
            sheet.addMergedRegion(new CellRangeAddress(i,i,0,7));
            List<ReleaseRqrmntInfo> rqrmnt_lists = (List<ReleaseRqrmntInfo>)map.get(Dict.RQRMNT_LIST);
            int n = 1;
            setCellValue(workbook, 0, i, 0, (String)map.get(Dict.GROUP_NAME), greenStyle);
            for (ReleaseRqrmntInfo releaseRqrmntInfo: rqrmnt_lists) {
                i++;
                setCellValue(workbook, 0, i, 0, String.valueOf(n), null);
                setCellValue(workbook, 0, i, 1, releaseRqrmntInfo.getRqrmnt_name(), null);
                setCellValue(workbook, 0, i, 2, releaseRqrmntInfo.getRqrmnt_no(), null);
                setCellValue(workbook, 0, i, 3, releaseRqrmntInfo.getIpmpUnit(), null);
                Map<String, String> otherSystemMap = taskService.queryRqrmntInfoApp(releaseRqrmntInfo);
                setCellValue(workbook, 0, i, 4, otherSystemMap.get(Dict.SYSNAME_CN), null);
                List<Map> spdb_managers = releaseRqrmntInfo.getRqrmnt_spdb_manager();
                if(CommonUtils.isNullOrEmpty(spdb_managers)){
                    setCellValue(workbook, 0, i, 5, "", null);
                }else{
                    spdb_managers = spdb_managers.stream().filter(e -> e.get(Dict.GROUP_ID).equals(map.get(Dict.GROUP_ID))).collect(Collectors.toList());
                    List<String> collect = spdb_managers.stream().map(e -> (String)e.get(Dict.USER_NAME_CN)).collect(Collectors.toList());
                    setCellValue(workbook, 0, i, 5, String.join(",", collect), null);
                }
                setCellValue(workbook, 0, i, 6, releaseRqrmntInfo.getOtherSystem(), null);
                setCellValue(workbook, 0, i, 7, releaseRqrmntInfo.getRqrmntContact(), null);
                n++;
            }
            i++;
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "RelaseRqrmntList.xls");
        workbook.write(resp.getOutputStream());
    }

    private void setCellValue(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue, CellStyle cellStyle) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            throw new FdevException(ErrorConstants.GENERATE_EXCEL_FAIL);
        }
        if (sheet.getRow(rowIndex) == null) {
            sheet.createRow(rowIndex);
        }
        if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
            sheet.getRow(rowIndex).createCell(cellIndex);
        }
        sheet.getRow(rowIndex).getCell(cellIndex).setCellFormula(null);
        sheet.getRow(rowIndex).getCell(cellIndex).setCellStyle(cellStyle);
        sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
    }

    private void setCellValueHyperlink(Workbook workbook, int sheetIndex, int rowIndex, int cellIndex, String cellValue, Hyperlink link) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            throw new FdevException(ErrorConstants.GENERATE_EXCEL_FAIL);
        }
        if (sheet.getRow(rowIndex) == null) {
            sheet.createRow(rowIndex);
        }
        if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
            sheet.getRow(rowIndex).createCell(cellIndex);
        }
        sheet.getRow(rowIndex).getCell(cellIndex).setHyperlink(link);
        sheet.getRow(rowIndex).getCell(cellIndex).setCellValue(cellValue);
    }

    @Override
    public void updateRqrmntInfoTaskList(String id, List<String> task_ids) throws Exception {
        releaseRqrmntInfoDao.updateRqrmntInfoTaskList(id, task_ids);
    }

    @Override
    public void deleteRqrmntInfo(String id) throws Exception {
        releaseRqrmntInfoDao.deleteRqrmntInfo(id);
    }

    @Override
    public void addTaskReleaseRqrmntInfo(String id, String task_id, String new_add_sign,String otherSystem,String commonProfile,String dateBaseAlter,String specialCase) throws Exception {
        List list = new ArrayList();
        list.add(task_id);
        releaseRqrmntInfoDao.addTaskReleaseRqrmntInfo(id, task_id, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
    }

    @Override
    public void deleteTaskReleaseRqrmntInfo(String id, String task_id) throws Exception {
        releaseRqrmntInfoDao.deleteTaskReleaseRqrmntInfo(id, task_id);
    }

    @Override
    public void deleteReleaseRqrmntInfoTask(String task_id) throws Exception {
        releaseRqrmntInfoDao.deleteReleaseRqrmntInfoTask(task_id);
    }

    @Override
    public void updateRqrmntInfoFlag(String id, String flag) {
        releaseRqrmntInfoDao.updateRqrmntInfoFlag(id,flag);
    }


    @Override
    public void addReleaseRqrmntInfoOrTask(String rqrmntNo, String rqrmntId, String rqrmntName, String groupId,
                                           String releaseDate, String taskId, String type, String new_add_sign,
                                           String otherSystem, String commonProfile, String dateBaseAlter,
                                           String specialCase) throws Exception {
        //查询是否已存在该需求
        ReleaseRqrmntInfo releaseRqrmntInfo = queryReleaseRqrmntInfo(releaseDate, rqrmntNo, groupId);
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo)){
            if(CommonUtils.isNullOrEmpty(rqrmntNo)){
                //若任务未关联需求 则不添加
                return;
            }
            //若无，则新插入需求信息
            ReleaseRqrmntInfo rri= new ReleaseRqrmntInfo();
            rri.setGroup_id(groupId);
            rri.setRelease_date(releaseDate);
            Set task_ids = new HashSet();
            ObjectId id = new ObjectId();
            task_ids.add(taskId);
            rri.set_id(id);
            rri.setTask_ids(task_ids);
            rri.setRqrmnt_no(rqrmntNo);
            rri.setRqrmnt_name(rqrmntName);
            rri.setId(id.toString());
            rri.setType(type);
            rri.setRqrmnt_id(rqrmntId);
            rri.setNew_add_sign(new_add_sign);
            rri.setOtherSystem(otherSystem);
            rri.setCommonProfile(commonProfile);
            rri.setDataBaseAlter(dateBaseAlter);
            rri.setSpecialCase(specialCase);
            saveReleaseRqrmntInfo(rri);
            List<ReleaseRqrmntInfo> releaseRqrmntInfos = new ArrayList<>();
            releaseRqrmntInfos.add(rri);
            Map<String, Map> maps = taskService.queryRqrmntInfoTasks(releaseRqrmntInfos);
            //同步标签;
            setRqrmntInfoTag(rri, maps);
            setRqrmntInfoNotAllow(rri, maps);
        }else{
            //若存在则插入该任务id在此需求信息
            addTaskReleaseRqrmntInfo(releaseRqrmntInfo.getId() ,taskId, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
            //同步标签
            List<ReleaseRqrmntInfo> releaseRqrmntInfos = new ArrayList<>();
            releaseRqrmntInfos.add(releaseRqrmntInfo);
            Map<String, Map> maps = taskService.queryRqrmntInfoTasks(releaseRqrmntInfos);
            setRqrmntInfoTag(releaseRqrmntInfo, maps);
            setRqrmntInfoNotAllow(releaseRqrmntInfo, maps);
        }
    }

    @Override
    public List queryRqrmntInfoList(String release_date, String type, List<String> groupIds, boolean isParent) throws Exception {
        Set<String> groupIdSet = new HashSet<>();
        if(isParent){
            for (String groupId : groupIds) {
                List<Map<String, Object>> maps = userService.queryChildGroupByGroupId(groupId);
                Set<String> collect = maps.stream().map(e -> (String)e.get(Dict.ID)).collect(Collectors.toSet());
                groupIdSet.addAll(collect);
            }
        }else{
            groupIdSet.addAll(groupIds);
        }
        List<ReleaseRqrmntInfo> releaseRqrmntInfoss = releaseRqrmntInfoDao.queryRqrmntInfoList(release_date, type, groupIdSet);
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = new ArrayList<ReleaseRqrmntInfo>();
        Map<String, Map> maps = new HashMap<String, Map>();
        queryReleaseRqrmntInfoByTaskIds(releaseRqrmntInfoss,releaseRqrmntInfos,maps);
        List<Map> result = new ArrayList();
        Map map = new HashMap();
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            if(Constants.RQRMNT_TYPE_INNER.equals(releaseRqrmntInfo.getType())){
                //若需求类型为 科技  则投产确认书为不涉及
                releaseRqrmntInfo.setRelease_confirm_doc(Constants.RELEASE_COMFIRM_FILE_NOT_INVOLVE);
            }else{
                if(taskService.queryTaskReleaseConfirmDoc(releaseRqrmntInfo, maps)){
                    releaseRqrmntInfo.setRelease_confirm_doc(Constants.RELEASE_COMFIRM_FILE_OPEN);
                }else{
                    releaseRqrmntInfo.setRelease_confirm_doc(Constants.RELEASE_COMFIRM_FILE_CLOSE);
                }
            }
            queryRqrmntSpdbManager(releaseRqrmntInfo);
            changeTag(releaseRqrmntInfo);
            queryRqrmntInfoPackageSubmitTest(maps, releaseRqrmntInfo);
            setIpmpUnit(maps, releaseRqrmntInfo);
            queryRqrmntInfoRelTest(maps, releaseRqrmntInfo);
            releaseRqrmntInfo.setDoc(fileService.queryRqrmntDoc(releaseRqrmntInfo.getRqrmnt_id()));
            Map<String, Object> threeLevelMap = userService.getThreeLevelGroup(releaseRqrmntInfo.getGroup_id());
            if(CommonUtils.isNullOrEmpty(map.get(threeLevelMap.get("id")))){
                List list = new ArrayList();
                list.add(releaseRqrmntInfo);
                map.put(threeLevelMap.get("id"), list);
            }else{
                List rqrmnt_list = (List)map.get(threeLevelMap.get("id"));
                rqrmnt_list.add(releaseRqrmntInfo);
            }
        }
        Set<Map.Entry<String, List>> set = map.entrySet();
        for (Map.Entry<String, List> entry : set) {
            String groupId = entry.getKey();
            List value = entry.getValue();
            Map rri = new HashMap();
            rri.put(Dict.GROUP_ID, groupId);
            List<ReleaseNode> releaseNodes = releaseNodeService.queryReleaseNodesByGroupIdAndDate(groupId, release_date);
            List<String> collect = releaseNodes.stream().map(e -> e.getRelease_node_name()).collect(Collectors.toList());
            Map<String, Object> group = userService.queryGroupDetail(groupId);
            if(!CommonUtils.isNullOrEmpty(group)){
                rri.put(Dict.GROUP_NAME, group.get(Dict.NAME));
                rri.put(Dict.SORTNUM, group.get(Dict.SORTNUM));
            }
            rri.put(Dict.RQRMNT_LIST, value);
            rri.put(Dict.RELEASE_NODE_NAME, collect);
            result.add(rri);
        }
        
        //根据同层的序号排序（glist为同层组信息）
        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                String[] split1 = ((String) (o1.get(Dict.SORTNUM))).split("-");
                String[] split2 = ((String) (o2.get(Dict.SORTNUM))).split("-");
                StringBuilder num1 = new StringBuilder();
                StringBuilder num2 = new StringBuilder();
                if(split1.length == split2.length){
                    for(int i =0; i < split1.length; i++){
                        num1.append(String.format("%02d", Integer.parseInt(split1[i])));
                        num2.append(String.format("%02d", Integer.parseInt(split2[i])));
                    }
                }else{
                    for (int i =0; i < split1.length; i++){
                        num1.append(String.format("%02d", Integer.parseInt(split1[i])));
                    }
                    for (int i =0; i < split2.length; i++){
                        num2.append(String.format("%02d", Integer.parseInt(split2[i])));
                    }
                }
                return  num1.toString().compareTo(num2.toString());
            }
        });
        return result;
    }

    /**
     * 设置实施单元编号
     * */
    private void setIpmpUnit(Map<String, Map> maps, ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        Set<String> ipmpUnits= new HashSet<>();
        Set<String> fdevUnits = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (String task_id : releaseRqrmntInfo.getTask_ids()){
            Map task = maps.get(task_id);
            if(!CommonUtils.isNullOrEmpty(task.get("fdev_implement_unit_no"))){
                fdevUnits.add((String) task.get("fdev_implement_unit_no"));
            }
        }
        // 根据研发单元查询研发单元详情从而拿到ipmp实施单元
        if(!CommonUtils.isNullOrEmpty(fdevUnits)){
            for(String fdevUnitId: fdevUnits){
                String ipmpUnit = releaseRqrmntService.queryFdevImplUnitDetail(fdevUnitId);
                if(!CommonUtils.isNullOrEmpty(ipmpUnit)){
                    ipmpUnits.add(ipmpUnit);
                }
            }
        }
        if(!CommonUtils.isNullOrEmpty(ipmpUnits)){
            sb.append(String.join("、",ipmpUnits)).append("/");
        }else{
            sb.append("-").append("/");
        }
        if(!CommonUtils.isNullOrEmpty(fdevUnits)){
           sb.append(String.join("、",fdevUnits));
        }else{
            sb.append("-");
        }
        releaseRqrmntInfo.setIpmpUnit(sb.toString());
    }


    private void queryRqrmntInfoPackageSubmitTest(Map<String, Map> maps, ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        boolean stageFlag = true;
        for (String task_id : releaseRqrmntInfo.getTask_ids()){
            Map task = maps.get(task_id);
            if(!Dict.UAT_LOWER.equals ((String)task.get(Dict.STAGE)) && !Dict.REL_LOWERCASE.equals ((String)task.get(Dict.STAGE))){
                stageFlag = false;
                break;
            };
        }
        if(stageFlag){
            if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getPackage_submit_test()) || "0".equals(releaseRqrmntInfo.getPackage_submit_test())){
                //修改需求信息整包提测到 未提测
                releaseRqrmntInfoDao.updateRqrmntInfoPackageSubmitTest(releaseRqrmntInfo.getId(), "1");
                releaseRqrmntInfo.setPackage_submit_test(CommonUtils.switchRelTestOrPackage("1"));
            }
            releaseRqrmntInfo.setPackage_submit_test(CommonUtils.switchRelTestOrPackage(releaseRqrmntInfo.getPackage_submit_test()));
        }else{
            releaseRqrmntInfo.setPackage_submit_test(CommonUtils.switchRelTestOrPackage("0"));
        }
    }

    private void queryRqrmntInfoRelTest(Map<String, Map> maps, ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        boolean stageFlag = true;
        for (String task_id : releaseRqrmntInfo.getTask_ids()){
            Map task = maps.get(task_id);
            if(!Dict.REL_LOWERCASE.equals ((String)task.get(Dict.STAGE))){
                stageFlag = false;
                break;
            };
        }
        if(stageFlag){
            if(!"2".equals(releaseRqrmntInfo.getRel_test())){
                //修改需求信息整包提测到 未提测
                releaseRqrmntInfoDao.updateRqrmntInfoRelTest(releaseRqrmntInfo.getId(), "1");
                releaseRqrmntInfo.setRel_test(CommonUtils.switchRelTestOrPackage("1"));
            }else {
                releaseRqrmntInfo.setRel_test(CommonUtils.switchRelTestOrPackage("2"));
            }
        }else{
            releaseRqrmntInfo.setRel_test(CommonUtils.switchRelTestOrPackage("0"));
        }
    }

    @Override
    public List queryRqrmntInfoListByType(String release_date, String type, List<String> groupIds, boolean isParent) throws Exception {
        Set<String> groupIdSet = new HashSet<>()
                ;        if(isParent){
            for (String groupId : groupIds) {
                List<Map<String, Object>> maps = userService.queryChildGroupByGroupId(groupId);
                Set<String> collect = maps.stream().map(e -> (String)e.get(Dict.ID)).collect(Collectors.toSet());
                groupIdSet.addAll(collect);
            }
        }else{
            groupIdSet.addAll(groupIds);
        }
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = releaseRqrmntInfoDao.queryRqrmntInfoList(release_date, "", groupIdSet);
        Map<String, Map> maps = taskService.queryRqrmntInfoTasks(releaseRqrmntInfos);
        List result = new ArrayList();
        for (ReleaseRqrmntInfo  releaseRqrmntInfo: releaseRqrmntInfos) {
            Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
            List testKeyNotes = new ArrayList();
            boolean flag = false;
            for (String task_id : task_ids) {
                Map<String, Object> task = maps.get(task_id);
                // 查询类型为重点提测
                if(Constants.RQRMNT_POINT_QUERY.equals(type)){
                    //判断任务是否为重点提测任务
                    if(!CommonUtils.isNullOrEmpty(task.get(Dict.TESTKEYNOTE))){
                        testKeyNotes.add(task.get(Dict.TESTKEYNOTE));
                    }
                }else{
                    // 查询类型为安全测试
                    Map review = (Map)task.get(Dict.REVIEW);
                    if(!CommonUtils.isNullOrEmpty(review)){
                        String secyrityTest = (String)review.get(Dict.SECURITYTEST);
                        if(Constants.INVOLVE_SECURITY_TEST_WORD.equals(secyrityTest)){
                            flag = true;
                            break;
                        }
                    }
                }
            }
            Map<String, Object> group = userService.queryGroupDetail(releaseRqrmntInfo.getGroup_id());
            queryRqrmntSpdbManager(releaseRqrmntInfo);
            if(!CommonUtils.isNullOrEmpty(group)){
                String fullName = (String)group.get(Dict.NAME);
                releaseRqrmntInfo.setGroup_name(fullName);
            }
            if(testKeyNotes.size() != 0){
                String join = String.join(";", testKeyNotes);
                releaseRqrmntInfo.setTestKeyNote(join);
                result.add(releaseRqrmntInfo);
            }
            if(flag){
                result.add(releaseRqrmntInfo);
            }
        }
        return result;
    }

    @Override
    @Async
    public void asyncRqrmntInfoTag() throws Exception {
        //获取当日日期
        String today = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = releaseRqrmntInfoDao.queryEarilyThanTodayRqrmntInfo(today);
        Map<String, Map> maps = taskService.queryRqrmntInfoTasks(releaseRqrmntInfos);
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            setRqrmntInfoTag(releaseRqrmntInfo, maps);
        }
    }
    @Override
    public void confirmRqrmntInfoTag(String confirmFileDate, String task_id) throws Exception {
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = releaseRqrmntInfoDao.queryReleaseRqrmntInfosByTaskNo(task_id);
        Map<String, Map> maps = taskService.queryRqrmntInfoTasks(releaseRqrmntInfos);
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            confirmSetRqrmntInfoTag(releaseRqrmntInfo, confirmFileDate);
        }
    }

    private void setRqrmntInfoTag(ReleaseRqrmntInfo releaseRqrmntInfo, Map<String, Map> maps) throws Exception{
        //需求类型为业务需求
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getType())){
            logger.error("rqrmntInfo type is null, id ="+releaseRqrmntInfo.getId());
            return;
        }
        String today = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        String bigReleaseNodeName = releaseRqrmntInfo.getRelease_date().replace("-","");
        ReleaseCycle releaseCycle = releaseCycleDao.queryByReleaseNodeName(bigReleaseNodeName);
        if(CommonUtils.isNullOrEmpty(releaseCycle)){
            return;
        }
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getTag())){
            releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_DAILY);
        }
        if(releaseRqrmntInfo.getType().equals(Constants.RQRMNT_TYPE_BUSINESS)){
            //未上传投产确认书
            if(!taskService.queryTaskReleaseConfirmDoc(releaseRqrmntInfo, maps)){
                //t-7以前为日常流程
                if(releaseCycle.getT_7().compareTo(today) > 0 ){
                    releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_DAILY);
                }
                //t - 3 以前为处对处
                if(releaseCycle.getT_7().compareTo(today) <= 0 && releaseCycle.getT_3().compareTo(today) > 0){
                    releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_CHIEF);
                }
                //t - 2 以前为总对总
                if(releaseCycle.getT_3().compareTo(today) <= 0 && releaseCycle.getT_2().compareTo(today) > 0){
                    releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_LENGTH);
                }
            }else{
                String date = taskService.queryTaskConfirmRecord(releaseRqrmntInfo);
                if(!CommonUtils.isNullOrEmpty(date)){
                    String subDate = date.substring(0, 10);
                    //t - 3 以前为处对处
                    if(releaseCycle.getT_7().compareTo(subDate) <= 0 && releaseCycle.getT_3().compareTo(subDate) > 0){
                        releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_CHIEF);
                    }
                    //t - 2 以前为总对总
                    if(releaseCycle.getT_3().compareTo(subDate) <= 0 && releaseCycle.getT_2().compareTo(subDate) > 0){
                        releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_LENGTH);
                    }
                }
            }
        }else{
            //需求类型为科技内部需求+
            if(!innerTestTimeFlag(releaseRqrmntInfo)){
                //包含任务未内测完成
                //t-7以前为日常流程
                if(releaseCycle.getT_4().compareTo(today) > 0 ){
                    releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_DAILY);
                }
                //t - 1 以前为条线
                if(releaseCycle.getT_4().compareTo(today) <= 0  && releaseCycle.getT().compareTo(today) > 0 ){
                    releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_CHIEF);
                }
                //t  为老总
                if(releaseCycle.getT().compareTo(today) <= 0 ){
                    releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_LENGTH);
                }
            }else{
                List<String> dates = new ArrayList<>();
                Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
                for (String task_id : task_ids) {
                    Map testInfo = taskService.queryTaskTestInfo(task_id);
                    if(!CommonUtils.isNullOrEmpty(testInfo)){
                        if(!CommonUtils.isNullOrEmpty(testInfo.get(Dict.UATSUBMITDATE))){
                            String uatSubmitDate = (String)testInfo.get(Dict.UATSUBMITDATE);
                            dates.add(uatSubmitDate);
                        }
                    }
                }
                Collections.sort(dates);
                if(!CommonUtils.isNullOrEmpty(dates)){
                    String date = dates.get(0);
                    //t - 1 以前为条线
                    if(releaseCycle.getT_4().compareTo(date) <= 0  && releaseCycle.getT().compareTo(date) > 0 ){
                        releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_CHIEF);
                    }
                    //t  为老总
                    if(releaseCycle.getT().compareTo(date) <= 0 ){
                        releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_LENGTH);
                    }
                }
            }
        }
        releaseRqrmntInfoDao.updateRqrmntInfoTag(releaseRqrmntInfo.getId(), releaseRqrmntInfo.getTag());
    }

    @Override
    @Async
    public void asyncRqrmntInfoTagNotAllow() throws Exception {
        String today = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = releaseRqrmntInfoDao.queryEarilyThanTodayRqrmntInfo(today);
        Map<String, Map> maps = taskService.queryRqrmntInfoTasks(releaseRqrmntInfos);
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            setRqrmntInfoNotAllow(releaseRqrmntInfo, maps);
        }
    }

    private void setRqrmntInfoNotAllow(ReleaseRqrmntInfo releaseRqrmntInfo, Map<String, Map> tasks)throws Exception{
        //需求类型为业务需求
        String today = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        String bigReleaseNodeName = releaseRqrmntInfo.getRelease_date().replace("-","");
        ReleaseCycle releaseCycle = releaseCycleDao.queryByReleaseNodeName(bigReleaseNodeName);
        if(CommonUtils.isNullOrEmpty(releaseCycle)){
            return;
        }
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getTag())){
            releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_DAILY);
        }

        if(releaseRqrmntInfo.getType().equals(Constants.RQRMNT_TYPE_BUSINESS)) {
            List<Map> maps = taskService.queryNotConfirmDocTasks(releaseRqrmntInfo);
            //未上传投产确认书
            if (!taskService.queryTaskReleaseConfirmDoc(releaseRqrmntInfo, tasks)) {
                //t-3 未收到则发邮件提醒
                if(releaseCycle.getT_3().compareTo(today) <= 0  && releaseCycle.getT_2().compareTo(today) > 0) {
                    sendEmailService.sendEmailRqrmntConfirmNotify(sendConfirmFileNotifyPrepareData(maps, releaseRqrmntInfo));
                }
                //t - 2 为不予投产
                if(releaseCycle.getT_2().compareTo(today) <= 0){
                    releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_NOT_ALLOW);
                    sendEmailService.sendEmailRqrmntConfirmNotifyNotAllow(sendConfirmFileNotifyPrepareData(maps, releaseRqrmntInfo));
                }
            }else{
                String date = taskService.queryTaskConfirmRecord(releaseRqrmntInfo);
                if(!CommonUtils.isNullOrEmpty(date)){
                    String subDate = date.substring(0, 10);
                    //t-3 未收到则发邮件提醒
                    if(releaseCycle.getT_3().compareTo(subDate) <= 0  && releaseCycle.getT_2().compareTo(subDate) > 0) {
                        sendEmailService.sendEmailRqrmntConfirmNotify(sendConfirmFileNotifyPrepareData(maps, releaseRqrmntInfo));
                    }
                    //t - 2 为不予投产
                    if(releaseCycle.getT_2().compareTo(subDate) <= 0){
                        releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_NOT_ALLOW);
                        sendEmailService.sendEmailRqrmntConfirmNotifyNotAllow(sendConfirmFileNotifyPrepareData(maps, releaseRqrmntInfo));
                    }
                }
            }
        }
        releaseRqrmntInfoDao.updateRqrmntInfoTag(releaseRqrmntInfo.getId(), releaseRqrmntInfo.getTag());
    }

    @Override
    public void updateRqrmntInfoReview(String task_id) throws Exception {
        ReleaseRqrmntInfo releaseRqrmntInfo = releaseRqrmntInfoDao.queryReleaseRqrmntInfoByTaskNo(task_id);

        Map<String, Object> rqrmnt = taskService.queryTaskRqrmnt(task_id);
        //若无需求编号 则无需添加
        if(CommonUtils.isNullOrEmpty(rqrmnt) && CommonUtils.isNullOrEmpty(rqrmnt.get(Dict.RQRMNT_NO))){
            return;
        }
        ReleaseNode releaseNode = null;
        String rqrmntNo = (String)rqrmnt.get(Dict.RQRMNTNO);
        String rqrmntId = (String)rqrmnt.get(Dict.RQRMNT_NO);
        String rqrmntName = (String)rqrmnt.get(Dict.RQRMNT_NAME_U);
        String group_id = (String)rqrmnt.get(Dict.GROUP_ID);
        String release_date = (String)rqrmnt.get(Dict.RELEASE_DATE);
        String rqrmntType = (String)rqrmnt.get(Dict.RQRMNTTYPE);
        String otherSystem = (String)rqrmnt.get(Dict.OTHERSYSTEM);
        String commonProfile = (String)rqrmnt.get(Dict.COMMONPROFILE);
        String new_add_sign = (String)rqrmnt.get(Dict.NEW_ADD_SIGN);
        String dateBaseAlter = (String)rqrmnt.get(Dict.DATABASEALTER);
        String specialCase = (String)rqrmnt.get(Dict.SPECIALCASE);
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo)){
            if(CommonUtils.isNullOrEmpty(release_date)){
                //若意向投产窗口为空 则可能任务已挂载投产窗口
                Map<String, Object> task = releaseTaskService.queryDetailByTaskId(task_id, Constants.TASK_RLS_CONFIRMED);
                if(CommonUtils.isNullOrEmpty(task)){
                    return;
                }
                addReleaseRqrmntInfoOrTask(rqrmntNo,rqrmntId, rqrmntName, group_id, (String)task.get(Dict.RELEASE_DATE),
                        task_id, rqrmntType, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
                return;
            }
            //若不存在 则表示新增需求信息
            addReleaseRqrmntInfoOrTask(rqrmntNo,rqrmntId, rqrmntName, group_id, release_date,
                    task_id, rqrmntType, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
            return;
        }
        if(!CommonUtils.isNullOrEmpty(rqrmntId) && !rqrmntId.equals(releaseRqrmntInfo.getRqrmnt_id())){
            //修改投产任务需求编号
            releaseTaskService.updateReleaseTaskRqrmntId(task_id, rqrmntId);
            //若需求id修改 则需修改需求信息
            deleteReleaseRqrmntInfoTask(task_id);
            //1 删除原需求所在任务  2 新增新需求编号
            addReleaseRqrmntInfoOrTask(rqrmntNo, rqrmntId, rqrmntName, releaseRqrmntInfo.getGroup_id(), releaseRqrmntInfo.getRelease_date(),
                    task_id, rqrmntType, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
        }
        ReleaseTask releaseTask = releaseTaskService.findConfirmTask(task_id);
        if(!CommonUtils.isNullOrEmpty(releaseTask)){
            releaseNode = releaseNodeService.queryDetail(releaseTask.getRelease_node_name());
        if(!releaseNode.getRelease_date().equals(releaseRqrmntInfo.getRelease_date())){
                //任务已挂载投产窗口
                deleteReleaseRqrmntInfoTask(task_id);
                addReleaseRqrmntInfoOrTask(rqrmntNo, rqrmntId, rqrmntName, group_id, releaseNode.getRelease_date(),
                        task_id, rqrmntType, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
        }
        }else{
            //若意向投产窗口修改  则需要修改需求信息
            if(!CommonUtils.isNullOrEmpty(release_date) && !release_date.equals(releaseRqrmntInfo.getRelease_date())){
                deleteReleaseRqrmntInfoTask(task_id);
                addReleaseRqrmntInfoOrTask(rqrmntNo,rqrmntId, rqrmntName, group_id, release_date,
                        task_id, rqrmntType, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
            }
            //意向任务小组与需求列表中不同，则修改
            if(!releaseRqrmntInfo.getGroup_id().equals(group_id)){
                deleteReleaseRqrmntInfoTask(task_id);
                addReleaseRqrmntInfoOrTask(rqrmntNo, rqrmntId, rqrmntName, group_id, releaseRqrmntInfo.getRelease_date(),
                        task_id, rqrmntType, new_add_sign, otherSystem, commonProfile, dateBaseAlter, specialCase);
            }
        }
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        if (task_ids.size() == 1){
            //该需求下只包含一个任务，若任务修改则需求信息也修改
            releaseRqrmntInfoDao.updateRqrmntInfoReview(releaseRqrmntInfo.getId(), otherSystem, commonProfile, dateBaseAlter, specialCase);
        }else{
            //该任务不止包含一个任
            List<String> commonProfiles = new ArrayList<>();
            List<String> dateBaseAlters = new ArrayList<>();
            Set<String> otherSystems = new HashSet<>();
            for (String taskId : task_ids) {
                Map<String, Object> task = taskService.queryTaskRqrmnt(taskId);
                commonProfiles.add((String)task.get(Dict.COMMONPROFILE));
                dateBaseAlters.add((String)task.get(Dict.DATABASEALTER));
                if(!CommonUtils.isNullOrEmpty(task.get(Dict.OTHER_SYSTEM))){
                    otherSystems.addAll((List)task.get(Dict.OTHER_SYSTEM));
                }
            }
            if(commonProfiles.contains("0")){
                commonProfile = "0";
            }
            if(dateBaseAlters.contains("0")){
                dateBaseAlter = "0";
            }
            releaseRqrmntInfoDao.updateRqrmntInfoReview(releaseRqrmntInfo.getId(), String.join(";" ,otherSystems), commonProfile, dateBaseAlter, specialCase);
            List<ReleaseRqrmntInfo> releaseRqrmntInfos = releaseRqrmntInfoDao.queryReleaseRqrmntInfosByTaskNo(task_id);
            //若任务对应多个需求列表实体，清除多余错误实体
            if(!CommonUtils.isNullOrEmpty(releaseRqrmntInfos) && releaseRqrmntInfos.size() > 1){
                for (ReleaseRqrmntInfo releaseRqrmntInfo1 : releaseRqrmntInfos) {
                    if(CommonUtils.isNullOrEmpty(releaseNode)){
                        if(!releaseRqrmntInfo1.getRelease_date().equals(release_date)
                                || !releaseRqrmntInfo1.getRqrmnt_no().equals(rqrmntId)
                                || !releaseRqrmntInfo.getGroup_id().equals(group_id)
                        ){
                            deleteTaskReleaseRqrmntInfo(releaseRqrmntInfo.getId(), task_id);
                        }
                    }else{
                        if(!releaseNode.getRelease_date().equals(release_date)
                                || !releaseRqrmntInfo1.getRqrmnt_no().equals(rqrmntId)
                                || !releaseRqrmntInfo.getGroup_id().equals(group_id)){
                            deleteTaskReleaseRqrmntInfo(releaseRqrmntInfo.getId(), task_id);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String queryRqrmntInfoPackageSubmitTest(ReleaseRqrmntInfo releaseRqrmntInfo, Map<String, Map> tasks) throws Exception {
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        boolean flag = true;
        for (String task_id : task_ids) {
            Map<String, Object> task = (Map<String, Object>)tasks.get(task_id);
            if(!CommonUtils.isNullOrEmpty(task)){
                //任务若不为uat 和rel 状态 则需求整包提测 未打包
                if(!Dict.UAT_LOWER.equals ((String)task.get(Dict.STAGE)) && !Dict.REL_LOWERCASE.equals ((String)task.get(Dict.STAGE))){
                    flag = false;
                    break;
                }
            }
        }
        if(flag){
            if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getPackage_submit_test()) || "0".equals(releaseRqrmntInfo.getPackage_submit_test())){
                //修改需求信息整包提测到 未提测
                releaseRqrmntInfoDao.updateRqrmntInfoPackageSubmitTest(releaseRqrmntInfo.getId(), "1");
                return "1";
            }
            return  releaseRqrmntInfo.getPackage_submit_test();
        }else{
            return  "0";
        }
    }

    @Override
    public String queryRqrmntInfoRelTest(String id, Map<String, Map> tasks) throws Exception {
        ReleaseRqrmntInfo releaseRqrmntInfo = releaseRqrmntInfoDao.queryRqrmntById(id);
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        boolean flag = true;
        for (String task_id : task_ids) {
            Map<String, Object> task = tasks.get(task_id);
            if(!CommonUtils.isNullOrEmpty(task)){
                if(!Dict.REL_LOWERCASE.equals ((String)task.get(Dict.STAGE))){
                    flag = false;
                    break;
                }
            }
        }
        if(flag){
            if(!"2".equals(releaseRqrmntInfo.getRel_test())){
                //修改需求信息整包提测到 未提测
                releaseRqrmntInfoDao.updateRqrmntInfoRelTest(id, "1");
                return "1";
            }else {
                return "2";
            }
        }else{
            return  "0";
        }
    }

    private HashMap sendConfirmFileNotifyPrepareData(List<Map> maps, ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception{
        HashMap model = new HashMap();
        List tasks = new ArrayList();
        Set<String> emails = new HashSet<>();
        for (Map task : maps) {
            String groupId = String.valueOf(((Map)task.get(Dict.GROUP)).get(Dict.ID));
            Map  groupInfo = userService.queryGroupDetail(groupId);
            boolean belongToInternet = String.valueOf(groupInfo.get(Dict.FULLNAME)).contains(Constants.INTERNET_GROUP);
            Map taskModel = new HashMap();
            taskModel.put(Dict.RQRMNTNO, releaseRqrmntInfo.getRqrmnt_no());
            taskModel.put(Dict.RQRMNT_NAME_U, releaseRqrmntInfo.getRqrmnt_name());
            taskModel.put(Dict.TASK_NAME, task.get(Dict.NAME));
            List masters = new ArrayList();
            if(!CommonUtils.isNullOrEmpty(task.get(Dict.MASTER))){
                List<Map>  master =  (List)task.get(Dict.MASTER);
                for (Map map : master) {
                    masters.add(map.get(Dict.USER_NAME_CN));
                    Map<String, Object> user = userService.queryUserInfo((String) map.get(Dict.USER_NAME_EN));
                    if(!CommonUtils.isNullOrEmpty(user)&&belongToInternet){
                        emails.add((String)(user.get(Dict.EMAIL)));
                    }
                }
            }
            taskModel.put(Dict.DEV_MANAGERS, String.join(",", masters));
            List spdb_masters = new ArrayList();
            if(!CommonUtils.isNullOrEmpty(task.get(Dict.SPDB_MASTER))){
                List<Map>  spdb_master =  (List)task.get(Dict.SPDB_MASTER);
                for (Map map : spdb_master) {
                    spdb_masters.add(map.get(Dict.USER_NAME_CN));
                    Map<String, Object> user = userService.queryUserInfo((String) map.get(Dict.USER_NAME_EN));
                    if(!CommonUtils.isNullOrEmpty(user)&&belongToInternet){
                        emails.add((String)(user.get(Dict.EMAIL)));
                    }
                }
            }
            taskModel.put(Dict.SPDB_MANAGERS, String.join(",", spdb_masters));
            String bigNodeName = releaseRqrmntInfo.getRelease_date().replace("-","");
            ReleaseNode bigReleaseNode = releaseNodeService.queryDetail(bigNodeName);
            if(!CommonUtils.isNullOrEmpty(bigReleaseNode)){
                List<String> releaseContact = bigReleaseNode.getRelease_contact();
                for (String contact : releaseContact) {
                    Map<String, Object> userDetail = userService.queryUserInfo(contact);
                    if(!CommonUtils.isNullOrEmpty(userDetail)&&belongToInternet){
                        emails.add((String)userDetail.get(Dict.EMAIL));
                    }
                }
            }
            ReleaseTask oneTask = releaseTaskService.findConfirmTask((String) task.get(Dict.ID));
            //查询任务对的投产联系人
            Set<String> relaseManagers = new HashSet<>();
            if(!CommonUtils.isNullOrEmpty(oneTask)){
                ReleaseNode releaseNode = releaseNodeService.queryDetail(oneTask.getRelease_node_name());
                String release_manager_name_en = releaseNode.getRelease_manager();
                String release_spdb_manager_name_en = releaseNode.getRelease_spdb_manager();
                Map<String, Object> release_manager = userService.queryUserInfo(release_manager_name_en);
                if(!CommonUtils.isNullOrEmpty(release_manager)&&belongToInternet){
                    emails.add((String)release_manager.get(Dict.EMAIL));
                    relaseManagers.add((String)release_manager.get(Dict.USER_NAME_CN));
                }
                Map<String, Object> release_spdb_manager = userService.queryUserInfo(release_spdb_manager_name_en);
                if(!CommonUtils.isNullOrEmpty(release_spdb_manager)&&belongToInternet){
                    emails.add((String)release_spdb_manager.get(Dict.EMAIL));
                    relaseManagers.add((String)release_spdb_manager.get(Dict.USER_NAME_CN));
                }
                //投产联系人
                taskModel.put(Dict.RELEASE_CONTACT, String.join(",", relaseManagers));
            }
            tasks.add(taskModel);
        }
        model.put(Dict.FTASK, tasks);
        model.put(Dict.EMAIL,emails);
        String release_date = releaseRqrmntInfo.getRelease_date().replace("-","");
        model.put(Dict.RELEASE_DATE, release_date);
        return model;
    }

    private boolean innerTestTimeFlag(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception{
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        boolean flag = true;
        for (String task_id : task_ids) {
            Map<String, Object> task = taskService.queryTaskDetail(task_id);
            if(!CommonUtils.isNullOrEmpty(task)){
                Map test_info = (Map)task.get(Dict.TEST_INFO);
                if(CommonUtils.isNullOrEmpty(test_info)){
                    return  false;
                }
                String stage = (String)test_info.get(Dict.STAGESTATUS);
                String uat_test_time = (String)task.get(Dict.UAT_TEST_TIME);
                //判断该任务有无结束内测
                if(!(Constants.TASK_ORDER_STATUS_UAT.equals(stage) || !Constants.TASK_ORDER_STATUS_SUBPACKAGE.equals(stage))
                        && CommonUtils.isNullOrEmpty(uat_test_time) ){
                    flag = false;
                }
            }
        }
        return flag;
    }

    private void changeTag(ReleaseRqrmntInfo releaseRqrmntInfo) {
        String tag = releaseRqrmntInfo.getTag();
        String type = releaseRqrmntInfo.getType();
        if(CommonUtils.isNullOrEmpty(tag)){
            return;
        }
        //科技需求标签不同
        if(Constants.RQRMNT_TYPE_INNER.equals(type)){
            switch (tag){
                case "1" : releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_DAILY_CN);
                    break;
                case "2" : releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_CHIEF_TEC_CN);
                    break;
                case "3" : releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_LENGTH_TEC_CN);
                    break;
            }
        }else{
            switch (tag){
                case "1" : releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_DAILY_CN);
                    break;
                case "2" : releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_CHIEF_CN);
                    break;
                case "3" : releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_LENGTH_CN);
                    break;
                case "4" : releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_NOT_ALLOW_CN);
                    break;
            }
        }
    }

    private List prepareData(List<ReleaseRqrmntInfo> releaseRqrmntInfos) throws Exception{
        List<Map> result = new ArrayList();
        Map map = new HashMap();
        String release_date = releaseRqrmntInfos.get(0).getRelease_date();
        Map<String, Map> maps = taskService.queryRqrmntInfoTasks(releaseRqrmntInfos);
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            if(Constants.RQRMNT_TYPE_INNER.equals(releaseRqrmntInfo.getType())){
                //若需求类型为 科技  则投产确认书为不涉及
                releaseRqrmntInfo.setRelease_confirm_doc(Constants.RELEASE_COMFIRM_FILE_NOT_INVOLVE);
            }else{
                boolean flag = true;
                for (String task_id : releaseRqrmntInfo.getTask_ids()){
                    Map task = maps.get(task_id);
                    String confirmBtn = (String) task.get(Dict.CONFIRMBTN);
                    if(CommonUtils.isNullOrEmpty(confirmBtn) || "0".equals(confirmBtn)){
                        flag = false;
                    }
                }
                if(flag){
                    releaseRqrmntInfo.setRelease_confirm_doc(Constants.RELEASE_COMFIRM_FILE_OPEN);
                }else{
                    releaseRqrmntInfo.setRelease_confirm_doc(Constants.RELEASE_COMFIRM_FILE_CLOSE);
                }
            }
            queryRqrmntSpdbManager(releaseRqrmntInfo);
            changeTag(releaseRqrmntInfo);
            queryRqrmntInfoPackageSubmitTest(maps, releaseRqrmntInfo);
            setIpmpUnit(maps, releaseRqrmntInfo);
            queryRqrmntInfoRelTest(maps, releaseRqrmntInfo);
            releaseRqrmntInfo.setDoc(fileService.queryRqrmntDoc(releaseRqrmntInfo.getRqrmnt_id()));
            if(CommonUtils.isNullOrEmpty(map.get(releaseRqrmntInfo.getGroup_id()))){
                List list = new ArrayList();
                list.add(releaseRqrmntInfo);
                map.put(releaseRqrmntInfo.getGroup_id(), list);
            }else{
                List rqrmnt_list = (List)map.get(releaseRqrmntInfo.getGroup_id());
                rqrmnt_list.add(releaseRqrmntInfo);
            }
        }
        Set<Map.Entry<String, List>> set = map.entrySet();
        for (Map.Entry<String, List> entry : set) {
            String groupId = entry.getKey();
            List value = entry.getValue();
            Map rri = new HashMap();
            rri.put(Dict.GROUP_ID, groupId);
            List<ReleaseNode> releaseNodes = releaseNodeService.queryReleaseNodesByGroupIdAndDate(groupId, release_date);
            List<String> collect = releaseNodes.stream().map(e -> e.getRelease_node_name()).collect(Collectors.toList());
            Map<String, Object> group = userService.queryGroupDetail(groupId);
            if(!CommonUtils.isNullOrEmpty(group)){

                rri.put(Dict.GROUP_NAME, group.get(Dict.FULLNAME));
                rri.put(Dict.SORTNUM, group.get(Dict.SORTNUM));
            }
            rri.put(Dict.RQRMNT_LIST, value);
            rri.put(Dict.RELEASE_NODE_NAME, collect);
            result.add(rri);
        }
        //根据同层的序号排序（glist为同层组信息）
       Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                String[] num1 = ((String)o1.get(Dict.SORTNUM)).split("-");
                String[] num2 = ((String)o2.get(Dict.SORTNUM)).split("-");
                if(num1[0].compareTo(num2[0]) > 0){
                    return -1;
                }else if(num1[0].compareTo(num2[0]) < 0 ){
                    return 1;
                }else{
                   if(num1.length < num2.length){
                       return -1;
                   }
                   return 0;
                }
            }
        });
        return result;
    }

    private void queryRqrmntSpdbManager(ReleaseRqrmntInfo releaseRqrmntInfo) throws  Exception{
        String rqrmnt_id = releaseRqrmntInfo.getRqrmnt_id();
        if(CommonUtils.isNullOrEmpty(rqrmnt_id)){
            return;
        }
        Map<String, Object> rqrmnt = releaseRqrmntService.queryRqrmntsInfo(rqrmnt_id);
        if(CommonUtils.isNullOrEmpty(rqrmnt)){
            return;
        }
        //查询需求的行内负责人
        List<Map> relate_part_detail = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(rqrmnt)){
            relate_part_detail = (List<Map>)rqrmnt.get(Dict.RELATE_PART_DETAIL);//需求负责人
            String rqrmntConact = (String) rqrmnt.get(Dict.PROPOSE_DEMAND_USER);//需求联系人
            releaseRqrmntInfo.setRqrmntContact(rqrmntConact);
        }

        //业务联系人
        List<Map> spdbManegers = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(relate_part_detail)){
            for (Map relate_part_detail_unit : relate_part_detail) {
                String groupId = String.valueOf(relate_part_detail_unit.get(Dict.PART_ID));
                List<String> assess_user = (List<String>)relate_part_detail_unit.get(Dict.ASSESS_USER);
                for(String userId : assess_user){
                    Map spdbManager = new HashMap();
                    Map<String, Object> user = userService.queryUserById(userId);
                    if(!CommonUtils.isNullOrEmpty(user)){
                        spdbManager.put(Dict.ID ,user.get(Dict.ID));
                        spdbManager.put(Dict.GROUP_ID, groupId);
                        spdbManager.put(Dict.USER_NAME_EN ,user.get(Dict.USER_NAME_EN));
                        spdbManager.put(Dict.USER_NAME_CN ,user.get(Dict.USER_NAME_CN));
                        spdbManegers.add(spdbManager);
                    }
                }
            }
        }
        releaseRqrmntInfo.setRqrmnt_spdb_manager(spdbManegers);
        // 科技类型
        releaseRqrmntInfo.setTechnology_type((String) rqrmnt.get("tech_type"));

    }

    private int getMaxTaskStage(String stage)throws  Exception{
        String stageStr = "开发中situatrel";
        return stageStr.indexOf(stage);
    }

    private String getMaxTaskStage(int value)throws  Exception{
        String stageStr = "开发中situatrel";
        return stageStr.substring(value, value+3);
    }

    @Override
    public void batchAddRqrmntInfo() throws Exception {
        String date = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        Map map = new HashMap();
        map.put(Dict.START_DATE,date);
        List<ReleaseNode> list = releaseNodeService.queryReleaseNodes(map);
        for (ReleaseNode releaseNode : list) {
            map.put(Dict.REST_CODE,"proWantWindow");
            map.put(Dict.GROUP, releaseNode.getOwner_groupId());
            map.put(Dict.PROWANTWINDOW, releaseNode.getRelease_date().replace("-","/"));
            List<Map> tasks = (List<Map>)restTransport.submit(map);
            for (Map task : tasks) {
                updateRqrmntInfoReview((String)task.get(Dict.ID));
            }
            List<ReleaseTask> releaseTasks = releaseTaskService.queryByReleaseNodeName(releaseNode.getRelease_node_name());
            for (ReleaseTask releaseTask : releaseTasks) {
                if(Constants.TASK_RLS_CONFIRMED.equals(releaseTask.getTask_status())){
                    updateRqrmntInfoReview(releaseTask.getTask_id());
                }
            }
        }
    }

    @Override
    public void deleteRqrmntInfoTask(String task_id) throws Exception {
        ReleaseRqrmntInfo releaseRqrmntInfo = releaseRqrmntInfoDao.queryReleaseRqrmntInfoByTaskNo(task_id);
        deleteTaskReleaseRqrmntInfo(releaseRqrmntInfo.getId(), task_id);
    }

    @Override
    public List<Map> queryRqrmntInfoTasks(String id) throws Exception {
        ReleaseRqrmntInfo releaseRqrmntInfo = releaseRqrmntInfoDao.queryRqrmntById(id);
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        Map<String, Object> tasks = taskService.queryTasksByIds(task_ids);
        if(CommonUtils.isNullOrEmpty(tasks)){
            return null;
        }
        List<Map> list =  new ArrayList(tasks.values());
        List result = new ArrayList();
        for (Map task : list) {
            String stage = (String)task.get(Dict.STAGE);
            if(stage.equals("production")||stage.equals("abort")||stage.equals("discard")||stage.equals("file")){
                continue;
            }
            Map group = (Map)task.get(Dict.GROUP);
            Map<String, Object> taskInfo =  taskService.queryTaskDetail((String)task.get(Dict.ID));
            String confirmFileDate = null;
            if (!CommonUtils.isNullOrEmpty(taskInfo)) {
				confirmFileDate = (String) taskInfo.get("confirmFileDate");
			}
            Map confirmRecord = taskService.queryTaskConfirmRecord((String) task.get(Dict.ID));
            Map taskMap = new HashMap();
            if(releaseRqrmntInfo.getType().equals(Constants.RQRMNT_TYPE_INNER)){
                taskMap.put(Dict.CONFIRMBTN, "不涉及");
            }else{
                if(CommonUtils.isNullOrEmpty(confirmRecord)){
                    taskMap.put(Dict.CONFIRMBTN, "未到达");
                }else{
                	if (!CommonUtils.isNullOrEmpty(confirmFileDate)) {
                		taskMap.put(Dict.CONFIRMBTN, confirmFileDate);
					} 
//                	else {
//						taskMap.put(Dict.CONFIRMBTN, confirmRecord.get(Dict.OPERATETIME));
//					}
                }
            }
            taskMap.put(Dict.TASK_ID, task.get(Dict.ID));
            taskMap.put(Dict.GROUP_NAME, group.get(Dict.NAME));
            taskMap.put(Dict.TASK_NAME, task.get(Dict.NAME));
            taskMap.put(Dict.STAGE, task.get(Dict.STAGE));
            taskMap.put(Dict.MASTER, task.get(Dict.MASTER));
            taskMap.put(Dict.PROJECT_NAME, task.get(Dict.PROJECT_NAME));
            taskMap.put(Dict.PROJECT_ID, task.get(Dict.PROJECT_ID));
            Map review = (Map)task.get(Dict.REVIEW);
            review.get(Dict.DATA_BASE_ALTER);
            if(!CommonUtils.isNullOrEmpty(review.get(Dict.SPECIALCASE))){
                List<String> speciclcase = (List<String>)review.get(Dict.SPECIALCASE);
                taskMap.put(Dict.SPECIALCASE , String.join(";", speciclcase));
            }else{
                taskMap.put(Dict.SPECIALCASE, "/");
            }
            if(!CommonUtils.isNullOrEmpty(review.get(Dict.OTHER_SYSTEM))){
                List<Map> otherSystem = (List<Map>)review.get(Dict.OTHER_SYSTEM);
                Set<String> collect = otherSystem.stream().map(e -> (String)e.get(Dict.NAME)).collect(Collectors.toSet());
                taskMap.put(Dict.OTHER_SYSTEM , String.join(";", collect));
            }else{
                taskMap.put(Dict.OTHER_SYSTEM, "/");
            }
            if(!CommonUtils.isNullOrEmpty(review.get(Dict.COMMONPROFILE))){
                boolean commonProfile = (boolean)review.get(Dict.COMMONPROFILE);
                if(commonProfile){
                    taskMap.put(Dict.COMMONPROFILE, "是");
                }else{
                    taskMap.put(Dict.COMMONPROFILE, "否");
                }
            }else{
                taskMap.put(Dict.COMMONPROFILE, "否");
            }
            List<Map> date_base_alter = (List<Map>)review.get(Dict.DATA_BASE_ALTER);
            if(!CommonUtils.isNullOrEmpty(date_base_alter)){
                String dateFlag = (String)date_base_alter.get(0).get(Dict.NAME);
                if(dateFlag.equals(Constants.REVIEW_TRUE)){
                    taskMap.put(Dict.DATABASEALTER, "是");
                }else{
                    taskMap.put(Dict.DATABASEALTER, "否");
                }
            }else{
                taskMap.put(Dict.DATABASEALTER, "否");
            }
            if(!CommonUtils.isNullOrEmpty(task.get(Dict.PROJECT_ID))){
                Map<String, Object> app = appService.queryAPPbyid((String)task.get(Dict.PROJECT_ID));
                if(!CommonUtils.isNullOrEmpty(app)){
                    if("0".equals(app.get(Dict.NEW_ADD_SIGN))){
                        taskMap.put(Dict.NEW_ADD_SIGN ," 是");
                    }else{
                        taskMap.put(Dict.NEW_ADD_SIGN ,"否");
                    }
                }
            }
            result.add(taskMap);
        }
        return result;
    }

    @Async
    @Override
    public void auditAddRqrmntInfo(ReleaseTask releaseTask, String uat_testobject) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, releaseTask.getTask_id());
        map.put(Dict.UAT_TESTOBJECT, uat_testobject);
        //确认投产时同时修改 任务的投产意向窗口
        ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseTask.getRelease_node_name());
        map.put(Dict.PROWANTWINDOW, releaseNode.getRelease_date().replace("-","/"));
        taskService.updateTaskInner(map);
        // 将任务中的投产文档添加至投产需求表
        releaseRqrmntService.addOrEditRqrmntTaskAsync(releaseTask.getTask_id(), releaseTask.getRelease_node_name());
        //修改投产需求信息
        updateRqrmntInfoReview(releaseTask.getTask_id());
    }

    @Override
    public void batchDeleteRqrmntInfo() throws Exception {
        String date = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = releaseRqrmntInfoDao.queryEarilyThanTodayRqrmntInfo(date);
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
            Map<String, Object> tasks = taskService.queryTasksByIds(task_ids);
            for (String task_id : task_ids) {
                Map task = (Map)tasks.get(task_id);
                if(Util.isNullOrEmpty(task)){
                    deleteRqrmntInfoTask(task_id);
                    continue;
                }
                String stage = (String)task.get(Dict.STAGE);
                if(stage.equals("production")||stage.equals("abort")||stage.equals("discard")||stage.equals("file") ||stage.equals("create-feature") ){
                    deleteRqrmntInfoTask(task_id);
                }
            }
        }
    }

    @Override
    public void batchUpdateRqrmntInfo() throws Exception {
        String date = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = releaseRqrmntInfoDao.queryEarilyThanTodayRqrmntInfo(date);
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
            Map<String, Object> tasks = taskService.queryTasksByIds(task_ids);
            for (String task_id : task_ids) {
                Map task = (Map)tasks.get(task_id);
                Map group = (Map)task.get(Dict.GROUP);
                String groupId = (String)group.get(Dict.ID);
                if(!groupId.equals(releaseRqrmntInfo.getGroup_id())){
                    updateRqrmntInfoReview(task_id);
                }
            }
        }
    }

    @Override
    public String queryTaskRqrmntAlert(String task_id) throws Exception {
        try {
            String fullgroupName = String.valueOf(userService.queryGroupDetail(
                    String.valueOf(((Map)taskService.queryTaskInfo(task_id).get(Dict.GROUP)).get(Dict.ID))).get(Dict.FULLNAME));
            if(!fullgroupName.contains(Constants.INTERNET_GROUP)){
                return null;
            }
        } catch (Exception e) {
            logger.error("fail to query task info");
        }

        ReleaseRqrmntInfo releaseRqrmntInfo = releaseRqrmntInfoDao.queryReleaseRqrmntInfoByTaskNo(task_id);
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo)){
            return null;
        }
        //若为科技需求则不提示
        if(releaseRqrmntInfo.getType().equals(Constants.RQRMNT_TYPE_INNER)){
            return null;
        }
        String bigReleaseNodeName = releaseRqrmntInfo.getRelease_date().replace("-","");
        ReleaseCycle releaseCycle = releaseCycleDao.queryByReleaseNodeName(bigReleaseNodeName);
        if(CommonUtils.isNullOrEmpty(releaseCycle)){
            return null;
        }
        Map confirmRecord = taskService.queryTaskConfirmRecord(task_id);
        if(CommonUtils.isNullOrEmpty(confirmRecord)){
            String today = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
            if(today.equals(releaseCycle.getT_8())||today.equals(releaseCycle.getT_7()) ||today.equals(releaseCycle.getT_1()) ){
                return releaseRqrmntInfo.getRelease_date();
            }
        }
        return null;
    }

    @Override
    public String queryRqrmntFileUri(String release_date) throws Exception {
        StringBuilder path = new StringBuilder(minioUri);
        return path.append(release_date).append("/").toString();
    }

    @Async
    @Override
    public void changeReleaseNode(ReleaseTask releaseTask, ReleaseNode releaseNode) throws Exception {
        //删除投产需求信息
        deleteRqrmntInfoTask(releaseTask.getTask_id());
        Map<String, Object> task = taskService.queryTaskRqrmnt(releaseTask.getTask_id());
        //更换投产窗口同时 修改任务的投产意向窗口为先窗口日期
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, releaseTask.getTask_id());
        //确认投产时同时修改 任务的投产意向窗口
        map.put(Dict.PROWANTWINDOW, releaseNode.getRelease_date().replace("-","/"));
        taskService.updateTaskInner(map);
        //新增投产需求信息
        addReleaseRqrmntInfoOrTask((String)task.get(Dict.RQRMNTNO),(String)task.get(Dict.RQRMNT_NO),
                (String)task.get(Dict.RQRMNT_NAME_U), (String)task.get(Dict.GROUP_ID), (String)releaseNode.getRelease_date(), releaseTask.getTask_id(), (String)task.get(Dict.RQRMNTTYPE),
                (String)task.get(Dict.NEW_ADD_SIGN), (String)task.get(Dict.OTHERSYSTEM), (String)task.get(Dict.COMMONPROFILE),
                (String)task.get(Dict.DATABASEALTER), (String)task.get(Dict.SPECIALCASE));
    }

    @Override
    public void cancelRelease(ReleaseTask releaseTask) throws Exception {
        // 删除关联的投产需求的文档
        ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseTask.getRelease_node_name());
        Map<String, Object> task = taskService.queryTaskRqrmnt(releaseTask.getTask_id());
        ReleaseRqrmntInfo releaseRqrmntInfo = queryReleaseRqrmntInfoByTaskNo(releaseTask.getTask_id());
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo)){
            return;
        }
        /**
         * 若该任务取消投产，则任务的意向投产窗口若与本窗口相同，则不做修改
         * 若不相同则删除该窗口下需求，添加意向窗口需求
         * 取消投产 需将任务的意向投产窗口 改为任务的 计划投产日期
         */
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, releaseTask.getTask_id());
        //确认投产时同时修改 任务的投产意向窗口
        map.put(Dict.PROWANTWINDOW, task.get(Dict.PLAN_FIRE_TIME));
        taskService.updateTaskInner(map);
        String proWantWindow = (String)task.get(Dict.PLAN_FIRE_TIME);
        String replace = proWantWindow.replace("/", "-");
        if(!CommonUtils.isNullOrEmpty(proWantWindow)){
            //若无意向投产窗口 则无需新增意向需求信息
            if(replace.equals(releaseRqrmntInfo.getRelease_date()) && task.get(Dict.GROUP_ID).equals(releaseRqrmntInfo.getGroup_id())){
                //该窗口为任务新增时的意向窗口  不予修改
            }else{
                //不同
                deleteRqrmntInfoTask(releaseTask.getTask_id());
                //查询该任务的意向投产窗口下是否有该需求信息
                addReleaseRqrmntInfoOrTask((String)task.get(Dict.RQRMNTNO),(String)task.get(Dict.RQRMNT_NO),
                        (String)task.get(Dict.RQRMNT_NAME_U), (String)task.get(Dict.GROUP_ID), releaseNode.getRelease_date(), releaseTask.getTask_id(), (String)task.get(Dict.RQRMNTTYPE),
                        (String)task.get(Dict.NEW_ADD_SIGN), (String)task.get(Dict.OTHERSYSTEM), (String)task.get(Dict.COMMONPROFILE),
                        (String)task.get(Dict.DATABASEALTER), (String)task.get(Dict.SPECIALCASE));
            }
        }
    }

    @Override
    public void addReleaseCycleT8() throws Exception {
        List<ReleaseCycle> releaseCycles = releaseCycleDao.queryAll();
        for (ReleaseCycle releaseCycle : releaseCycles) {
            if(CommonUtils.isNullOrEmpty(releaseCycle.getT_8())){
                List<String> sevenDays = getSevenDays(releaseCycle.getT());
                releaseCycle.setT_8(sevenDays.get(7));
            }
        }
    }

    private List<String> getSevenDays(String releaseDate) throws ParseException {
        List<String> workDays = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String dateStr = getDaysBeforeStringDate(releaseDate, i);
            if (!isWeekend(dateStr)) {
                workDays.add(dateStr);
            }
        }
        return workDays;
    }

    private static boolean isWeekend(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        Date date = sdf.parse(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    private static String getDaysBeforeStringDate(String date, int n) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        calendar.setTime(sdf.parse(date));
        calendar.set(Calendar.DATE, calendar.get(calendar.DATE) - n);
        return sdf.format(calendar.getTime());
    }

    private void confirmSetRqrmntInfoTag(ReleaseRqrmntInfo releaseRqrmntInfo, String confirmFileDate) throws Exception{
        //需求类型为业务需求
        if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getType())){
            logger.error("rqrmntInfo type is null, id ="+releaseRqrmntInfo.getId());
            return;
        }
        String bigReleaseNodeName = releaseRqrmntInfo.getRelease_date().replace("-","");
        ReleaseCycle releaseCycle = releaseCycleDao.queryByReleaseNodeName(bigReleaseNodeName);
        if(CommonUtils.isNullOrEmpty(releaseCycle)){
            return;
        }
        if(releaseRqrmntInfo.getType().equals(Constants.RQRMNT_TYPE_BUSINESS)){
            String date = taskService.queryTaskConfirmRecord(releaseRqrmntInfo);
            //t-7以前为日常流程
            if(releaseCycle.getT_7().compareTo(confirmFileDate) > 0 ){
                releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_DAILY);
            }
            //t - 3 以前为处对处
            if(releaseCycle.getT_7().compareTo(confirmFileDate) <= 0 && releaseCycle.getT_3().compareTo(confirmFileDate) > 0){
                releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_CHIEF);
            }
            //t - 2 以前为总对总
            if(releaseCycle.getT_3().compareTo(confirmFileDate) <= 0 && releaseCycle.getT_2().compareTo(confirmFileDate) > 0){
                releaseRqrmntInfo.setTag(Constants.RQRMNT_TAG_LENGTH);
            }
        }
        releaseRqrmntInfoDao.updateRqrmntInfoTag(releaseRqrmntInfo.getId(), releaseRqrmntInfo.getTag());
    }
    
    @Override
    public List<Map<String, Object>> addRedLineScanReport(Map<String, Object> requestParam) throws Exception {
    	String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);//投产点名称
        List<Map<String, Object>> result = releaseApplicationService.queryApplications(release_node_name);//查询投产应用列表
        List<Map<String, Object>> ebankList = new ArrayList<Map<String,Object>>();//个人手机银行系统集合
        List<Map<String, Object>> netbankList = new ArrayList<Map<String,Object>>();//个人网银集合
        List<Map<String, Object>> otherList = new ArrayList<Map<String,Object>>();//其他系统集合
        if (!CommonUtils.isNullOrEmpty(result)) {
        	for (Map<String, Object> map : result) {
        		Map<String, Object> systemMap = (Map<String, Object>) map.get(Dict.SYSTEM);
        		String systemName = "";
        		if (!CommonUtils.isNullOrEmpty(systemMap)) {
        			systemName = (String) systemMap.get(Dict.NAME);
				}
        		if (systemName.contains("个人手机")) {
        			Map<String, Object> ebankMap = new HashMap<String, Object>();
        			ebankMap.put(Dict.APP_NAME_ZH, map.get(Dict.APP_NAME_ZH));
        			ebankMap.put(Dict.APP_NAME_EN, map.get(Dict.APP_NAME_EN));
        			ebankMap.put(Dict.ISPARENT, "是");
					ebankMap.put(Dict.GIT, map.get(Dict.GIT));
					ebankMap.put(Dict.RELEASE_BRANCH, map.get(Dict.RELEASE_BRANCH));
					String applictionId = (String)map.get(Dict.APPLICATION_ID);
					String lastTag = prodApplicationDao.queryLastTagByGitlabId(applictionId,"","proc",null);
					if (!CommonUtils.isNullOrEmpty(lastTag)) {
						ebankMap.put(Dict.LAST_RELEASE_TAG, lastTag);
						ebankMap.put(Dict.TYPE, "更新");
					} else {
						ebankMap.put(Dict.LAST_RELEASE_TAG, "");
						ebankMap.put(Dict.TYPE, "新增");
					}
					List<Map<String, Object>> appDevManagerList = (List<Map<String, Object>>) map.get(Dict.APP_DEV_MANAGERS);
					List<String> userNameCnLsit = new ArrayList<String>();
					for (Map<String, Object> map2 : appDevManagerList) {
						userNameCnLsit.add((String) map2.get(Dict.USER_NAME_CN));
					}
					ebankMap.put(Dict.APP_DEV_MANAGERS, String.join(",", userNameCnLsit));
					ebankList.add(ebankMap);
				} else if (systemName.contains("个人网银")) {
					Map<String, Object> netbankMap = new HashMap<String, Object>();
					netbankMap.put(Dict.APP_NAME_ZH, map.get(Dict.APP_NAME_ZH));
					netbankMap.put(Dict.APP_NAME_EN, map.get(Dict.APP_NAME_EN));
					netbankMap.put(Dict.ISPARENT, "是");
					netbankMap.put(Dict.GIT, map.get(Dict.GIT));
					netbankMap.put(Dict.RELEASE_BRANCH, map.get(Dict.RELEASE_BRANCH));
					String applictionId = (String)map.get(Dict.APPLICATION_ID);
					String lastTag = prodApplicationDao.queryLastTagByGitlabId(applictionId,"","proc",null);
					if (!CommonUtils.isNullOrEmpty(lastTag)) {
						netbankMap.put(Dict.LAST_RELEASE_TAG, lastTag);
						netbankMap.put(Dict.TYPE, "更新");
					} else {
						netbankMap.put(Dict.LAST_RELEASE_TAG, "");
						netbankMap.put(Dict.TYPE, "新增");
					}
					List<Map<String, Object>> appDevManagerList = (List<Map<String, Object>>) map.get(Dict.APP_DEV_MANAGERS);
					List<String> userNameCnLsit = new ArrayList<String>();
					for (Map<String, Object> map2 : appDevManagerList) {
						userNameCnLsit.add((String) map2.get(Dict.USER_NAME_CN));
					}
					netbankMap.put(Dict.APP_DEV_MANAGERS, String.join(",", userNameCnLsit));
					netbankList.add(netbankMap);
				} else {
					Map<String, Object> otherkMap = new HashMap<String, Object>();
					otherkMap.put(Dict.APP_NAME_ZH, map.get(Dict.APP_NAME_ZH));
					otherkMap.put(Dict.APP_NAME_EN, map.get(Dict.APP_NAME_EN));
					otherkMap.put(Dict.ISPARENT, "是");
					otherkMap.put(Dict.GIT, map.get(Dict.GIT));
					otherkMap.put(Dict.RELEASE_BRANCH, map.get(Dict.RELEASE_BRANCH));
					String applictionId = (String)map.get(Dict.APPLICATION_ID);
					String lastTag = prodApplicationDao.queryLastTagByGitlabId(applictionId,"","proc",null);
					if (!CommonUtils.isNullOrEmpty(lastTag)) {
						otherkMap.put(Dict.LAST_RELEASE_TAG, lastTag);
						otherkMap.put(Dict.TYPE, "更新");
					} else {
						otherkMap.put(Dict.LAST_RELEASE_TAG, "");
						otherkMap.put(Dict.TYPE, "新增");
					}
					List<Map<String, Object>> appDevManagerList = (List<Map<String, Object>>) map.get(Dict.APP_DEV_MANAGERS);
					List<String> userNameCnLsit = new ArrayList<String>();
					for (Map<String, Object> map2 : appDevManagerList) {
						userNameCnLsit.add((String) map2.get(Dict.USER_NAME_CN));
					}
					otherkMap.put(Dict.APP_DEV_MANAGERS, String.join(",", userNameCnLsit));
					otherList.add(otherkMap);
				}
        		
        	}
			
		}
        String group_id = (String) requestParam.get(Dict.GROUP_ID);
        GroupAbbr groupAbbr = groupAbbrService.queryGroupAbbr(group_id);
        String group_abbr = "";
        if (!CommonUtils.isNullOrEmpty(groupAbbr) && !CommonUtils.isNullOrEmpty(groupAbbr.getGroup_abbr())) {
        	group_abbr = groupAbbr.getGroup_abbr();
        }
        String releaseName = release_node_name.substring(0, release_node_name.lastIndexOf("_"));
        //上传文件
        String local_path = localdir + release_node_name;
        String fileName = "";
        if (!CommonUtils.isNullOrEmpty(group_abbr)) {
        	fileName = "红线扫描信息_" + releaseName + "_" + group_abbr + ".xlsx";
		}else {
			fileName = "红线扫描信息_" + releaseName + ".xlsx";
		}
        CommonUtils.createDirectory(local_path);
        writeToExecl(local_path + "/" + fileName, ebankList, netbankList, otherList);
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
        return result;
    }
    
    private void writeToExecl(String filePath, List<Map<String, Object>> ebankList, List<Map<String, Object>> netbankList, List<Map<String, Object>> otherList) throws IOException {
    	FileInputStream fis = new FileInputStream(new File(scripts_path + "word_templates/红线扫描信息.xlsx"));
//    	FileInputStream fis = new FileInputStream(new File("C:/Users/t-liujj1/Desktop/红线扫描信息.xlsx")); //本地测试使用
        XSSFWorkbook workBook = new XSSFWorkbook(fis);
        XSSFSheet sheet1 = workBook.getSheetAt(0);
        XSSFSheet sheet3 = workBook.createSheet();
        workBook.setSheetName(0, "sheet1");
        XSSFCellStyle cellStyle = sheet1.getRow(9).getCell(0).getCellStyle();
        XSSFCellStyle cellStyle2 = sheet1.getRow(2).getCell(0).getCellStyle();
        XSSFCellStyle cellStyle3 = sheet1.getRow(9).getCell(3).getCellStyle();
        XSSFCellStyle cellStyle1 = sheet1.getRow(0).getCell(1).getCellStyle();
        XSSFCellStyle cellStyle4 = sheet1.getRow(0).getCell(2).getCellStyle();
        XSSFCellStyle cellStyle5 = sheet1.getRow(0).getCell(7).getCellStyle();
        int cellStyleHead = sheet1.getColumnWidth(0);
        int cellStyleHead1 = sheet1.getColumnWidth(1);
        int cellStyleHead2 = sheet1.getColumnWidth(2);
        int cellStyleHead3 = sheet1.getColumnWidth(3);
        int cellStyleHead4 = sheet1.getColumnWidth(4);
        int cellStyleHead5 = sheet1.getColumnWidth(5);
        int cellStyleHead6 = sheet1.getColumnWidth(6);
        int cellStyleHead7 = sheet1.getColumnWidth(7);
        int cellStyleHeadHeight = sheet1.getRow(0).getHeight();
        //设置表头 begin
        XSSFRow creRowHead = (XSSFRow) sheet3.createRow(0);
        creRowHead.setHeightInPoints(cellStyleHeadHeight/20);
        //设置值
        creRowHead.createCell(0).setCellValue("微服务中文名");
        creRowHead.createCell(1).setCellValue("微服务ID");
        creRowHead.createCell(2).setCellValue("是否已提供只读权限");
        creRowHead.createCell(3).setCellValue("git路径（http地址）");
        creRowHead.createCell(4).setCellValue("最新release");
        creRowHead.createCell(5).setCellValue("前一次tag");
        creRowHead.createCell(6).setCellValue("负责人");
        creRowHead.createCell(7).setCellValue("投产类型（新增/更新）");
        //设置样式
        creRowHead.getCell(0).setCellStyle(cellStyle1);
        creRowHead.getCell(1).setCellStyle(cellStyle1);
        creRowHead.getCell(2).setCellStyle(cellStyle4);
        creRowHead.getCell(3).setCellStyle(cellStyle1);
        creRowHead.getCell(4).setCellStyle(cellStyle1);
        creRowHead.getCell(5).setCellStyle(cellStyle1);
        creRowHead.getCell(6).setCellStyle(cellStyle1);
        creRowHead.getCell(7).setCellStyle(cellStyle5);
        //设置宽度
        sheet3.setColumnWidth(0, cellStyleHead);
        sheet3.setColumnWidth(1, cellStyleHead1);
        sheet3.setColumnWidth(2, cellStyleHead2);
        sheet3.setColumnWidth(3, cellStyleHead3);
        sheet3.setColumnWidth(4, cellStyleHead4);
        sheet3.setColumnWidth(5, cellStyleHead5);
        sheet3.setColumnWidth(6, cellStyleHead6);
        sheet3.setColumnWidth(7, cellStyleHead7);
        //设置表头 end 
        int ebankLength = 0;
        int netbankLength = 0;
        int otherLength = 0;
        //个人手机系统数据处理  begin
        if (!CommonUtils.isNullOrEmpty(ebankList)) {//个人手机系统用数据
        	sheet3.shiftRows(1, 1, 1, true, false);
        	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(1);
        	creRow1.createCell(0).setCellValue("个人手机");
        	creRow1.getCell(0).setCellStyle(cellStyle2);
        	CellRangeAddress region = new CellRangeAddress(1,2,0,7);
        	sheet3.addMergedRegion(region);
        	RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet3);
        	ebankLength = ebankList.size();
			sheet3.shiftRows(3, ebankLength + 2, ebankLength, true, false);
			for (int i = 0; i < ebankList.size(); i++) {
				XSSFRow creRow = (XSSFRow) sheet3.createRow(3 + i);
				creRow.createCell(0).setCellValue(ebankList.get(i).get(Dict.APP_NAME_ZH).toString());
				creRow.createCell(1).setCellValue(ebankList.get(i).get(Dict.APP_NAME_EN).toString());
				creRow.createCell(2).setCellValue(ebankList.get(i).get(Dict.ISPARENT).toString());
				creRow.createCell(3).setCellValue(ebankList.get(i).get(Dict.GIT).toString());
				creRow.createCell(4).setCellValue(ebankList.get(i).get(Dict.RELEASE_BRANCH).toString());
				String lastTag = (String) ebankList.get(i).get(Dict.LAST_RELEASE_TAG);
				if (!CommonUtils.isNullOrEmpty(lastTag)) {
					creRow.createCell(5).setCellValue(lastTag);
				} else {
					creRow.createCell(5).setCellValue("新增微服务");
				}
				creRow.createCell(6).setCellValue(ebankList.get(i).get(Dict.APP_DEV_MANAGERS).toString());
				creRow.createCell(7).setCellValue(ebankList.get(i).get(Dict.TYPE).toString());
				creRow.getCell(0).setCellStyle(cellStyle);
				creRow.getCell(1).setCellStyle(cellStyle);
				creRow.getCell(2).setCellStyle(cellStyle);
				creRow.getCell(3).setCellStyle(cellStyle3);
				creRow.getCell(4).setCellStyle(cellStyle);
				creRow.getCell(5).setCellStyle(cellStyle);
				creRow.getCell(6).setCellStyle(cellStyle);
				creRow.getCell(7).setCellStyle(cellStyle);
			}
		} else {
			// 个人手机系统没有数据
			sheet3.shiftRows(1, 1, 1, true, false);
        	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(1);
        	creRow1.createCell(0).setCellValue("个人手机");
        	creRow1.getCell(0).setCellStyle(cellStyle2);
        	CellRangeAddress region = new CellRangeAddress(1,2,0,7);
        	sheet3.addMergedRegion(region);
        	RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet3);
			sheet3.shiftRows(3, 5, 3, true, false);
			for (int i = 0; i < 3; i++) {
				XSSFRow creRow = (XSSFRow) sheet3.createRow(3 + i);
				creRow.createCell(0).setCellValue("");
				creRow.createCell(1).setCellValue("");
				creRow.createCell(2).setCellValue("");
				creRow.createCell(3).setCellValue("");
				creRow.createCell(4).setCellValue("");
				creRow.createCell(5).setCellValue("");
				creRow.createCell(6).setCellValue("");
				creRow.createCell(7).setCellValue("");
				creRow.getCell(0).setCellStyle(cellStyle);
				creRow.getCell(1).setCellStyle(cellStyle);
				creRow.getCell(2).setCellStyle(cellStyle);
				creRow.getCell(3).setCellStyle(cellStyle3);
				creRow.getCell(4).setCellStyle(cellStyle);
				creRow.getCell(5).setCellStyle(cellStyle);
				creRow.getCell(6).setCellStyle(cellStyle);
				creRow.getCell(7).setCellStyle(cellStyle);
			}
		}
        //个人手机系统数据处理 end
        //个人网银数据处理 begin
        if (!CommonUtils.isNullOrEmpty(netbankList)) {//个人网银有数据 begin
        	if (ebankLength != 0) {//个人手机有数据
        		sheet3.shiftRows(ebankLength + 3, ebankLength + 3, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(ebankLength + 3);
            	creRow1.createCell(0).setCellValue("个人网银");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionNet = new CellRangeAddress(ebankLength + 3,ebankLength + 4,0,7);
            	sheet3.addMergedRegion(regionNet);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionNet, sheet3);
        		netbankLength = netbankList.size();
    			sheet3.shiftRows(ebankLength + 5, netbankLength + ebankLength + 4, netbankLength, true, false);
    			for (int i = 0; i < netbankList.size(); i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(ebankLength + 5 + i);
    				creRow.createCell(0).setCellValue(netbankList.get(i).get(Dict.APP_NAME_ZH).toString());
    				creRow.createCell(1).setCellValue(netbankList.get(i).get(Dict.APP_NAME_EN).toString());
    				creRow.createCell(2).setCellValue(netbankList.get(i).get(Dict.ISPARENT).toString());
    				creRow.createCell(3).setCellValue(netbankList.get(i).get(Dict.GIT).toString());
    				creRow.createCell(4).setCellValue(netbankList.get(i).get(Dict.RELEASE_BRANCH).toString());
    				String lastTag = (String) netbankList.get(i).get(Dict.LAST_RELEASE_TAG);
    				if (!CommonUtils.isNullOrEmpty(lastTag)) {
    					creRow.createCell(5).setCellValue(lastTag);
    				} else {
    					creRow.createCell(5).setCellValue("新增微服务");
    				}
    				creRow.createCell(6).setCellValue(netbankList.get(i).get(Dict.APP_DEV_MANAGERS).toString());
    				creRow.createCell(7).setCellValue(netbankList.get(i).get(Dict.TYPE).toString());
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			} else {
				//个人手机无数据
				sheet3.shiftRows(6, 6, 1, true, false);
	        	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(6);
	        	creRow1.createCell(0).setCellValue("个人网银");
	        	creRow1.getCell(0).setCellStyle(cellStyle2);
	        	CellRangeAddress regionnNet = new CellRangeAddress(6,7,0,7);
	        	sheet3.addMergedRegion(regionnNet);
	        	RegionUtil.setBorderRight(BorderStyle.THIN, regionnNet, sheet3);
	    		netbankLength = netbankList.size();
				sheet3.shiftRows(8, netbankLength + 7, netbankLength, true, false);
				for (int i = 0; i < netbankList.size(); i++) {
					XSSFRow creRow = (XSSFRow) sheet3.createRow(8 + i);
					creRow.createCell(0).setCellValue(netbankList.get(i).get(Dict.APP_NAME_ZH).toString());
					creRow.createCell(1).setCellValue(netbankList.get(i).get(Dict.APP_NAME_EN).toString());
					creRow.createCell(2).setCellValue(netbankList.get(i).get(Dict.ISPARENT).toString());
					creRow.createCell(3).setCellValue(netbankList.get(i).get(Dict.GIT).toString());
					creRow.createCell(4).setCellValue(netbankList.get(i).get(Dict.RELEASE_BRANCH).toString());
					String lastTag = (String) netbankList.get(i).get(Dict.LAST_RELEASE_TAG);
					if (!CommonUtils.isNullOrEmpty(lastTag)) {
						creRow.createCell(5).setCellValue(lastTag);
					} else {
						creRow.createCell(5).setCellValue("新增微服务");
					}
					creRow.createCell(6).setCellValue(netbankList.get(i).get(Dict.APP_DEV_MANAGERS).toString());
					creRow.createCell(7).setCellValue(netbankList.get(i).get(Dict.TYPE).toString());
					creRow.getCell(0).setCellStyle(cellStyle);
					creRow.getCell(1).setCellStyle(cellStyle);
					creRow.getCell(2).setCellStyle(cellStyle);
					creRow.getCell(3).setCellStyle(cellStyle3);
					creRow.getCell(4).setCellStyle(cellStyle);
					creRow.getCell(5).setCellStyle(cellStyle);
					creRow.getCell(6).setCellStyle(cellStyle);
					creRow.getCell(7).setCellStyle(cellStyle);
				}
			}
		} else { //个人网银无数据 begin
			if (ebankLength != 0) {// 个人手机有数据
				sheet3.shiftRows(ebankLength + 3, ebankLength + 3, 1, true, false);
	        	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(ebankLength + 3);
	        	creRow1.createCell(0).setCellValue("个人网银");
	        	creRow1.getCell(0).setCellStyle(cellStyle2);
	        	CellRangeAddress regionnNet = new CellRangeAddress(ebankLength + 3,ebankLength + 4,0,7);
	        	sheet3.addMergedRegion(regionnNet);
	        	RegionUtil.setBorderRight(BorderStyle.THIN, regionnNet, sheet3);
	    		netbankLength = netbankList.size();
				sheet3.shiftRows(ebankLength + 5, ebankLength + 7, 3, true, false);
				for (int i = 0; i < 3; i++) {
					XSSFRow creRow = (XSSFRow) sheet3.createRow(ebankLength + 5 + i);
					creRow.createCell(0).setCellValue("");
					creRow.createCell(1).setCellValue("");
					creRow.createCell(2).setCellValue("");
					creRow.createCell(3).setCellValue("");
					creRow.createCell(4).setCellValue("");
					creRow.createCell(5).setCellValue("");
					creRow.createCell(6).setCellValue("");
					creRow.createCell(7).setCellValue("");
					creRow.getCell(0).setCellStyle(cellStyle);
					creRow.getCell(1).setCellStyle(cellStyle);
					creRow.getCell(2).setCellStyle(cellStyle);
					creRow.getCell(3).setCellStyle(cellStyle3);
					creRow.getCell(4).setCellStyle(cellStyle);
					creRow.getCell(5).setCellStyle(cellStyle);
					creRow.getCell(6).setCellStyle(cellStyle);
					creRow.getCell(7).setCellStyle(cellStyle);
				}
			} else {// 个人手机无数据
				sheet3.shiftRows(6, 6, 1, true, false);
	        	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(6);
	        	creRow1.createCell(0).setCellValue("个人网银");
	        	creRow1.getCell(0).setCellStyle(cellStyle2);
	        	CellRangeAddress regionnNet = new CellRangeAddress(6,7,0,7);
	        	sheet3.addMergedRegion(regionnNet);
	        	RegionUtil.setBorderRight(BorderStyle.THIN, regionnNet, sheet3);
				sheet3.shiftRows(8, 10, 3, true, false);
				for (int i = 0; i < 3; i++) {
					XSSFRow creRow = (XSSFRow) sheet3.createRow(8 + i);
					creRow.createCell(0).setCellValue("");
					creRow.createCell(1).setCellValue("");
					creRow.createCell(2).setCellValue("");
					creRow.createCell(3).setCellValue("");
					creRow.createCell(4).setCellValue("");
					creRow.createCell(5).setCellValue("");
					creRow.createCell(6).setCellValue("");
					creRow.createCell(7).setCellValue("");
					creRow.getCell(0).setCellStyle(cellStyle);
					creRow.getCell(1).setCellStyle(cellStyle);
					creRow.getCell(2).setCellStyle(cellStyle);
					creRow.getCell(3).setCellStyle(cellStyle3);
					creRow.getCell(4).setCellStyle(cellStyle);
					creRow.getCell(5).setCellStyle(cellStyle);
					creRow.getCell(6).setCellStyle(cellStyle);
					creRow.getCell(7).setCellStyle(cellStyle);
				}
			}
		}
        // 个人网银数据处理 end
        // 其他数据处理 begin
        if (!CommonUtils.isNullOrEmpty(otherList)) { // 其他有数据
        	if (ebankLength != 0 && netbankLength != 0) { //个人手机和个人网银都有数据
        		sheet3.shiftRows(netbankLength + ebankLength + 5, netbankLength + ebankLength + 5, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(netbankLength + ebankLength + 5);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(netbankLength + ebankLength + 5,netbankLength + ebankLength + 6,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
            	otherLength = otherList.size();
    			sheet3.shiftRows(netbankLength + ebankLength + 7, netbankLength + ebankLength + otherLength + 6, otherLength, true, false);
    			for (int i = 0; i < otherList.size(); i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(netbankLength + ebankLength + 7 + i);
    				creRow.createCell(0).setCellValue(otherList.get(i).get(Dict.APP_NAME_ZH).toString());
    				creRow.createCell(1).setCellValue(otherList.get(i).get(Dict.APP_NAME_EN).toString());
    				creRow.createCell(2).setCellValue(otherList.get(i).get(Dict.ISPARENT).toString());
    				creRow.createCell(3).setCellValue(otherList.get(i).get(Dict.GIT).toString());
    				creRow.createCell(4).setCellValue(otherList.get(i).get(Dict.RELEASE_BRANCH).toString());
    				String lastTag = (String) otherList.get(i).get(Dict.LAST_RELEASE_TAG);
    				if (!CommonUtils.isNullOrEmpty(lastTag)) {
    					creRow.createCell(5).setCellValue(lastTag);
    				} else {
    					creRow.createCell(5).setCellValue("新增微服务");
    				}
    				creRow.createCell(6).setCellValue(otherList.get(i).get(Dict.APP_DEV_MANAGERS).toString());
    				creRow.createCell(7).setCellValue(otherList.get(i).get(Dict.TYPE).toString());
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        	if (ebankLength != 0 && netbankLength == 0) { //个人手机有数据，个人网银无数据
        		sheet3.shiftRows(ebankLength + 8, ebankLength + 8, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(ebankLength + 8);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(ebankLength + 8,ebankLength + 9,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
            	otherLength = otherList.size();
    			sheet3.shiftRows(ebankLength + 10, ebankLength + otherLength + 9, otherLength, true, false);
    			for (int i = 0; i < otherList.size(); i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(ebankLength + 10 + i);
    				creRow.createCell(0).setCellValue(otherList.get(i).get(Dict.APP_NAME_ZH).toString());
    				creRow.createCell(1).setCellValue(otherList.get(i).get(Dict.APP_NAME_EN).toString());
    				creRow.createCell(2).setCellValue(otherList.get(i).get(Dict.ISPARENT).toString());
    				creRow.createCell(3).setCellValue(otherList.get(i).get(Dict.GIT).toString());
    				creRow.createCell(4).setCellValue(otherList.get(i).get(Dict.RELEASE_BRANCH).toString());
    				String lastTag = (String) otherList.get(i).get(Dict.LAST_RELEASE_TAG);
    				if (!CommonUtils.isNullOrEmpty(lastTag)) {
    					creRow.createCell(5).setCellValue(lastTag);
    				} else {
    					creRow.createCell(5).setCellValue("新增微服务");
    				}
    				creRow.createCell(6).setCellValue(otherList.get(i).get(Dict.APP_DEV_MANAGERS).toString());
    				creRow.createCell(7).setCellValue(otherList.get(i).get(Dict.TYPE).toString());
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        	if (ebankLength == 0 && netbankLength != 0) { // 个人手机无数据，个人网银有数据
        		sheet3.shiftRows(netbankLength + 8, netbankLength + 8, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(netbankLength + 8);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(netbankLength + 8,netbankLength + 9,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
            	otherLength = otherList.size();
    			sheet3.shiftRows(netbankLength + 10, netbankLength + otherLength + 9, otherLength, true, false);
    			for (int i = 0; i < otherList.size(); i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(netbankLength + 10 + i);
    				creRow.createCell(0).setCellValue(otherList.get(i).get(Dict.APP_NAME_ZH).toString());
    				creRow.createCell(1).setCellValue(otherList.get(i).get(Dict.APP_NAME_EN).toString());
    				creRow.createCell(2).setCellValue(otherList.get(i).get(Dict.ISPARENT).toString());
    				creRow.createCell(3).setCellValue(otherList.get(i).get(Dict.GIT).toString());
    				creRow.createCell(4).setCellValue(otherList.get(i).get(Dict.RELEASE_BRANCH).toString());
    				String lastTag = (String) otherList.get(i).get(Dict.LAST_RELEASE_TAG);
    				if (!CommonUtils.isNullOrEmpty(lastTag)) {
    					creRow.createCell(5).setCellValue(lastTag);
    				} else {
    					creRow.createCell(5).setCellValue("新增微服务");
    				}
    				creRow.createCell(6).setCellValue(otherList.get(i).get(Dict.APP_DEV_MANAGERS).toString());
    				creRow.createCell(7).setCellValue(otherList.get(i).get(Dict.TYPE).toString());
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        	if (ebankLength == 0 && netbankLength == 0) { // 个人手机和个人网银都没有数据
        		sheet3.shiftRows(11, 11, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(11);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(11,12,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
            	otherLength = otherList.size();
    			sheet3.shiftRows(13, otherLength + 12, otherLength, true, false);
    			for (int i = 0; i < otherList.size(); i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(13 + i);
    				creRow.createCell(0).setCellValue(otherList.get(i).get(Dict.APP_NAME_ZH).toString());
    				creRow.createCell(1).setCellValue(otherList.get(i).get(Dict.APP_NAME_EN).toString());
    				creRow.createCell(2).setCellValue(otherList.get(i).get(Dict.ISPARENT).toString());
    				creRow.createCell(3).setCellValue(otherList.get(i).get(Dict.GIT).toString());
    				creRow.createCell(4).setCellValue(otherList.get(i).get(Dict.RELEASE_BRANCH).toString());
    				String lastTag = (String) otherList.get(i).get(Dict.LAST_RELEASE_TAG);
    				if (!CommonUtils.isNullOrEmpty(lastTag)) {
    					creRow.createCell(5).setCellValue(lastTag);
    				} else {
    					creRow.createCell(5).setCellValue("新增微服务");
    				}
    				creRow.createCell(6).setCellValue(otherList.get(i).get(Dict.APP_DEV_MANAGERS).toString());
    				creRow.createCell(7).setCellValue(otherList.get(i).get(Dict.TYPE).toString());
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        } else {  // 其他无数据
        	if (ebankLength != 0 && netbankLength != 0) { //个人手机和个人网银都有数据
        		sheet3.shiftRows(netbankLength + ebankLength + 5, netbankLength + ebankLength + 5, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(netbankLength + ebankLength + 5);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(netbankLength + ebankLength + 5,netbankLength + ebankLength + 6,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
    			sheet3.shiftRows(netbankLength + ebankLength + 7, netbankLength + ebankLength + 9, 3, true, false);
    			for (int i = 0; i < 3; i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(netbankLength + ebankLength + 7 + i);
    				creRow.createCell(0).setCellValue("");
    				creRow.createCell(1).setCellValue("");
    				creRow.createCell(2).setCellValue("");
    				creRow.createCell(3).setCellValue("");
    				creRow.createCell(4).setCellValue("");
    				creRow.createCell(5).setCellValue("");
    				creRow.createCell(6).setCellValue("");
    				creRow.createCell(7).setCellValue("");
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        	if (ebankLength != 0 && netbankLength == 0) { //个人手机有数据，个人网银无数据
        		sheet3.shiftRows(ebankLength + 8, ebankLength + 8, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(ebankLength + 8);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(ebankLength + 8,ebankLength + 9,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
    			sheet3.shiftRows(ebankLength + 10, ebankLength + 12, 3, true, false);
    			for (int i = 0; i < 3; i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(ebankLength + 10 + i);
    				creRow.createCell(0).setCellValue("");
    				creRow.createCell(1).setCellValue("");
    				creRow.createCell(2).setCellValue("");
    				creRow.createCell(3).setCellValue("");
    				creRow.createCell(4).setCellValue("");
    				creRow.createCell(5).setCellValue("");
    				creRow.createCell(6).setCellValue("");
    				creRow.createCell(7).setCellValue("");
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        	if (ebankLength == 0 && netbankLength != 0) { // 个人手机无数据，个人网银有数据
        		sheet3.shiftRows(netbankLength + 8, netbankLength + 8, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(netbankLength + 8);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(netbankLength + 8,netbankLength + 9,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
    			sheet3.shiftRows(netbankLength + 10, netbankLength + 12, 3, true, false);
    			for (int i = 0; i < 3; i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(netbankLength + 10 + i);
    				creRow.createCell(0).setCellValue("");
    				creRow.createCell(1).setCellValue("");
    				creRow.createCell(2).setCellValue("");
    				creRow.createCell(3).setCellValue("");
    				creRow.createCell(4).setCellValue("");
    				creRow.createCell(5).setCellValue("");
    				creRow.createCell(6).setCellValue("");
    				creRow.createCell(7).setCellValue("");
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        	if (ebankLength ==0 && netbankLength == 0) { // 个人手机和个人网银都没有数据
        		sheet3.shiftRows(11, 11, 1, true, false);
            	XSSFRow creRow1 = (XSSFRow) sheet3.createRow(11);
            	creRow1.createCell(0).setCellValue("其他");
            	creRow1.getCell(0).setCellStyle(cellStyle2);
            	CellRangeAddress regionOther = new CellRangeAddress(11,12,0,7);
            	sheet3.addMergedRegion(regionOther);
            	RegionUtil.setBorderRight(BorderStyle.THIN, regionOther, sheet3);
    			sheet3.shiftRows(13, 15, 3, true, false);
    			for (int i = 0; i < 3; i++) {
    				XSSFRow creRow = (XSSFRow) sheet3.createRow(13 + i);
    				creRow.createCell(0).setCellValue("");
    				creRow.createCell(1).setCellValue("");
    				creRow.createCell(2).setCellValue("");
    				creRow.createCell(3).setCellValue("");
    				creRow.createCell(4).setCellValue("");
    				creRow.createCell(5).setCellValue("");
    				creRow.createCell(6).setCellValue("");
    				creRow.createCell(7).setCellValue("");
    				creRow.getCell(0).setCellStyle(cellStyle);
    				creRow.getCell(1).setCellStyle(cellStyle);
    				creRow.getCell(2).setCellStyle(cellStyle);
    				creRow.getCell(3).setCellStyle(cellStyle3);
    				creRow.getCell(4).setCellStyle(cellStyle);
    				creRow.getCell(5).setCellStyle(cellStyle);
    				creRow.getCell(6).setCellStyle(cellStyle);
    				creRow.getCell(7).setCellStyle(cellStyle);
    			}
			}
        }
        OutputStream out = new FileOutputStream(filePath);
        try {
        	workBook.removeSheetAt(0);
        	workBook.write(out);
    	} finally {
			fis.close();
	        out.flush();
	        out.close();
		}
    }
    
    private void saveRqrmntFile(List<Map<String, String>> list, String path, String fileName) {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.ID, new ObjectId().toString());
        map.put(Dict.NAME, fileName);
        map.put(Dict.PATH, path);
        list.add(map);
    }
    
    private void queryReleaseRqrmntInfoByTaskIds(List<ReleaseRqrmntInfo> releaseRqrmntInfoss, List<ReleaseRqrmntInfo> releaseRqrmntInfos, Map<String, Map> maps) throws Exception {
    	for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfoss) {
			Set<String> taskIds = (Set<String>) releaseRqrmntInfo.getTask_ids();
			Map<String, Object> map = new HashMap<String, Object>();
			if (!CommonUtils.isNullOrEmpty(taskIds)) {
				map.put(Dict.IDS, taskIds);
				map.put(Dict.REST_CODE, "queryTaskBaseInfoByIds");// queryTaskDetailByIds queryTaskBaseInfoByIds
				Map<String, Object> taskInfos = new HashMap<String, Object>();
				ArrayList<Map<String, Object>> taskInfoList = (ArrayList<Map<String, Object>>) restTransport.submit(map);
				List<String> confirmFileDateList = new ArrayList<String>();
				for (Map<String, Object> map2 : taskInfoList) {
					String taskId = (String) map2.get(Dict.ID);
					taskInfos.put(taskId, map2);
					String confirmFileDate = (String)map2.get(Dict.CONFIRMFILEDATE);
					//任务状态非已投产、废弃、撤销、归档时，根据需求类型补充业务需求上线确认书到达时间
					String stage = (String)map2.get(Dict.STAGE);
		            if(stage.equals("production")||stage.equals("abort")||stage.equals("discard")||stage.equals("file")){
		                continue;
		            }
					if(releaseRqrmntInfo.getType().equals(Constants.RQRMNT_TYPE_INNER)){
		                confirmFileDateList.add("不涉及");
		            }else{
		            	if(Util.isNullOrEmpty(confirmFileDate)) {
							confirmFileDateList.add("未到达");
						}else{
		                	if (!Util.isNullOrEmpty(confirmFileDate)) {
		                		confirmFileDateList.add(confirmFileDate);
							} 
		                }
		            }
				}
				releaseRqrmntInfo.setConfirmFileDateList(confirmFileDateList);
//				Map<String, Object> taskInfos = (Map<String, Object>) restTransport.submit(map);
				Set<String> task_ids = new HashSet<>();
				for (String taskId : taskIds) {
					Map<String, Object> taskInfo = (Map<String, Object>) taskInfos.get(taskId);
					String applicationType = (String) taskInfo.get("applicationType");
					if (!CommonUtils.isNullOrEmpty(applicationType) && applicationType.contains("app")) {
						task_ids.add((String)taskInfo.get("id"));
						maps.put(taskId, taskInfo);
					}
				}
				if (!CommonUtils.isNullOrEmpty(task_ids)) {
					releaseRqrmntInfo.setTask_ids(task_ids);
					releaseRqrmntInfos.add(releaseRqrmntInfo);
				}
			}
		}
    }
}
