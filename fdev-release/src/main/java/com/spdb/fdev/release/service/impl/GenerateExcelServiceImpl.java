package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.release.dao.IAutomationParamDao;
import com.spdb.fdev.release.entity.AutomationParam;
import com.spdb.fdev.release.entity.GroupAbbr;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class GenerateExcelServiceImpl implements IGenerateExcelService {

	@Value("${scripts.path}")
	private String scripts_path;
	@Value("${excel.local.dir}")
	private String excel_local_dir;
	@Autowired
	private IProdRecordService prodRecordService;
	@Autowired
	private IGroupAbbrService groupAbbrService;
	@Autowired
	private IAutomationParamDao automationParamDao;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IReleaseRqrmntInfoService releaseRqrmntInfoService;
    @Autowired
    private IFileService fileService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ITaskService taskService;

	private static final Logger logger = LoggerFactory.getLogger(GenerateExcelServiceImpl.class);

	@Override
	public String generateProdExcel(List<String> prod_ids) throws Exception {
	    StringBuilder return_tips = new StringBuilder();
		ProdRecord prodRecord = prodRecordService.queryDetail(prod_ids.get(0));
		if (CommonUtils.isNullOrEmpty(prodRecord)) {
			logger.error("pord not exist,prod id is" + prod_ids.get(0));
			throw new FdevException(ErrorConstants.GENERATE_EXCEL_FAIL);
		}
		//根据组id查系统标识
		GroupAbbr groupAbbr = groupAbbrService.queryGroupAbbr(prodRecord.getOwner_groupId());
		//获取该文件的下载流
        String excel_template_url = prodRecord.getExcel_template_url();
        byte[] content = getFileContent("fdev-release", excel_template_url);
		try(ByteArrayInputStream byteInputStream = new ByteArrayInputStream(content);
		        Workbook workbook = WorkbookFactory.create(byteInputStream)){
		    // 判断为非空白模板，则写入内容
		    if(!prodRecord.getExcel_template_url().contains(Dict.BLANK_EXCEL_MODEL)) {
                setCellValue(workbook, 0, 0, 1, prodRecord.getProd_spdb_no(), null);
                setCellValue(workbook, 0, 1, 1, groupAbbr.getSystem_abbr(), null);
                int num = 6;
                for (String prod_id : prod_ids) {
                    ProdRecord pr = prodRecordService.queryDetail(prod_id);
                    if (!CommonUtils.isNullOrEmpty(pr)) {
                        if(num == 6) {
                            String prod_assets_version = pr.getVersion() + "_" + pr.getProd_spdb_no();
                            if(!prod_assets_version.equals(pr.getProd_assets_version())) {
                                logger.info("excel文件中的变更说明与fdev系统变更说明不一致,复制介质到新目录");
                                // 没有勾选下载所有模板 且 没有勾选第一个 介质未在该变更说明的目录中 转移介质服务器中的介质
                                try {
                                    CommonUtils.runPythonArray(scripts_path + "copy_media.py",
                                            new String[]{String.join(",", prod_ids), prod_assets_version});
                                } catch (Exception e) {
                                    logger.error("介质复制失败异常,不影响模板正常下载");
                                }
                            }
                        }
                        String tar_name = new StringBuilder(pr.getVersion()).append(".tar").toString();
                        setCellValue(workbook, 0, num,0, tar_name, null);
                        num++;
                    }
                }
                //使excel中所有公式生效
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                if (formulaEvaluator != null) {
                    for(int i = 0; i < workbook.getNumberOfSheets(); ++i) {
                        Sheet sheet = workbook.getSheetAt(i);
                        Iterator sheetIter = sheet.iterator();
                        while(sheetIter.hasNext()) {
                            Row r = (Row)sheetIter.next();
                            Iterator rowIter = r.iterator();
                            while(rowIter.hasNext()) {
                                Cell c = (Cell)rowIter.next();
                                if(c.getCellTypeEnum() == CellType.FORMULA) {
                                    try {
                                        formulaEvaluator.evaluateFormulaCellEnum(c);
                                    } catch (Exception ex) {
                                        logger.error(ex.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            String local_path = new StringBuilder(excel_local_dir).append(CommonUtils.getSessionUser().getUser_name_en())
                    .append("_").append(System.currentTimeMillis()).append("/").toString();
            CommonUtils.createDirectory(local_path);
            String file_path = local_path + prodRecord.getExcel_template_name();
            try (OutputStream os = new FileOutputStream(file_path);){
                workbook.write(os);
            } catch (Exception exception){
                logger.error(exception.getMessage());
            }

            AutomationParam autorelease_server_host = automationParamDao.queryByKey(Dict.AUTORELEASE_SERVER_HOST);
            AutomationParam autorelease_server_root_dir = automationParamDao.queryByKey(Dict.AUTORELEASE_SERVER_ROOT_DIR);
            String media_path = new StringBuilder(autorelease_server_root_dir.getValue()).append("/")
                    .append(groupAbbr.getSystem_abbr()).append("/").append(prodRecord.getVersion()).append("_")
                    .append(prodRecord.getProd_spdb_no()).append("/").toString();
            CommonUtils.runPythonArray(scripts_path + "send_excel_to_media.py", new String[]{local_path,
                    file_path, media_path});
            return_tips.append("excel模板已在").append(autorelease_server_host.getValue()).append("介质服务器生成，文件地址为：")
                    .append(media_path).append(prodRecord.getExcel_template_name());
		} catch (Exception e) {
			logger.error("excel generate failed: ", e);
			throw new FdevException(ErrorConstants.GENERATE_EXCEL_FAIL);
		}
		return return_tips.toString();
	}

    @Override
    public void exportRqrmntInfoList(String release_date, String type , List<String> groupIds, boolean isParent, HttpServletResponse resp) throws Exception {
        List<Map> list = releaseRqrmntInfoService.queryRqrmntInfoList(release_date, type, groupIds, isParent);
        // excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle greenStyle = workbook.createCellStyle();
        greenStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
        greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CreationHelper creationHelper = workbook.getCreationHelper();
        setCellValue(workbook, 0, 0, 0, "组名", cellStyle);
        setCellValue(workbook, 0, 0, 1, "标签", cellStyle);
        setCellValue(workbook, 0, 0, 2, "实施单元\n" + "【ipmp实施单元编号/FDEV编号】", cellStyle);
        setCellValue(workbook, 0, 0, 3, "需求名称", cellStyle);
        setCellValue(workbook, 0, 0, 4, "需求编号", cellStyle);
        setCellValue(workbook, 0, 0, 5, "需求类型", cellStyle);
        setCellValue(workbook, 0, 0, 6, "科技类型", cellStyle);
        setCellValue(workbook, 0, 0, 7, "行内负责人", cellStyle);
        setCellValue(workbook, 0, 0, 8, "任务负责人", cellStyle);
        setCellValue(workbook, 0, 0, 9, "业务联系人", cellStyle);
        setCellValue(workbook, 0, 0, 10, "确认书到达时间", cellStyle);
        setCellValue(workbook, 0, 0, 11, "涉及应用", cellStyle);
        setCellValue(workbook, 0, 0, 12, "涉及系统", cellStyle);
        setCellValue(workbook, 0, 0, 13, "同步投产系统", cellStyle);
        setCellValue(workbook, 0, 0, 14, "涉及新增应用", cellStyle);
        setCellValue(workbook, 0, 0, 15, "涉及数据库", cellStyle);
        setCellValue(workbook, 0, 0, 16, "涉及公共配置文件", cellStyle);
        setCellValue(workbook, 0, 0, 17, "涉及特殊项内容", cellStyle);
        int i = 1;
        for (Map map : list) {
            sheet.addMergedRegion(new CellRangeAddress(i,i,0,17));
            List<ReleaseRqrmntInfo> rqrmnt_lists = (List<ReleaseRqrmntInfo>)map.get(Dict.RQRMNT_LIST);
            setCellValue(workbook, 0, i, 0, (String)map.get(Dict.GROUP_NAME), greenStyle);
            i++;
            int j = 1;
            for (ReleaseRqrmntInfo releaseRqrmntInfo: rqrmnt_lists) {
                setCellValue(workbook, 0, i, 0, String.valueOf(j), null);
                changeTag(releaseRqrmntInfo);//将tag标签标识修改为中文意思
                setCellValue(workbook, 0, i, 1, releaseRqrmntInfo.getTag(), null);
                setCellValue(workbook, 0, i, 2, releaseRqrmntInfo.getIpmpUnit(), null);
                setCellValue(workbook, 0, i, 3, releaseRqrmntInfo.getRqrmnt_name(), null);
                setCellValue(workbook, 0, i, 4, releaseRqrmntInfo.getRqrmnt_no(), null);
                if(Constants.RQRMNT_TYPE_BUSINESS.equals(releaseRqrmntInfo.getType())){ // 业务需求
                    setCellValue(workbook, 0, i, 5, Constants.RQRMNT_TYPE_BUSINESS, null);
                }else if(Constants.RQRMNT_TYPE_INNER.equals(releaseRqrmntInfo.getType())){ // 科技内部需求
                    setCellValue(workbook, 0, i, 5, Constants.RQRMNT_TYPE_INNER, null);
                }else{ // 日常需求
                    setCellValue(workbook, 0, i, 5, Constants.RQRMNT_TYPE_DAILY, null);
                }
                setCellValue(workbook, 0, i, 6, releaseRqrmntInfo.getTechnology_type(), null);
                List<Map> spdb_managers = releaseRqrmntInfo.getRqrmnt_spdb_manager();
                if(CommonUtils.isNullOrEmpty(spdb_managers)){
                    setCellValue(workbook, 0, i, 7, "", null);
                }else{
                    List<String> collect = spdb_managers.stream().map(e -> (String)e.get(Dict.USER_NAME_CN)).collect(Collectors.toList());
                    setCellValue(workbook, 0, i, 7, String.join(",", collect), null);
                }
                setCellValue(workbook, 0, i, 8, queryTaskManagerStr(releaseRqrmntInfo), null);
                setCellValue(workbook, 0, i, 9, releaseRqrmntInfo.getRqrmntContact(), null);
                List<String> confirmFileDateList = releaseRqrmntInfo.getConfirmFileDateList();
                StringBuffer sbConfirmFileDate = new StringBuffer();
                if(!Util.isNullOrEmpty(confirmFileDateList)) {
                	for (Iterator iterator = confirmFileDateList.iterator(); iterator.hasNext();) {
						String confirmFileDate = (String) iterator.next();
						if(!Util.isNullOrEmpty(confirmFileDate)) {
							if(!Util.isNullOrEmpty(sbConfirmFileDate.toString())) {
								sbConfirmFileDate.append(",");
							}
							sbConfirmFileDate.append(confirmFileDate);
						}
					}
                	setCellValue(workbook, 0, i, 10, sbConfirmFileDate.toString(), null);
                }else {
                	setCellValue(workbook, 0, i, 10, taskService.queryRqrmntInfoConfirmRecord(releaseRqrmntInfo), null);
                }
                Map<String, String> appMap = taskService.queryRqrmntInfoApp(releaseRqrmntInfo);
                setCellValue(workbook, 0, i, 11, appMap.get(Dict.APP), null);
                setCellValue(workbook, 0, i, 12, appMap.get(Dict.SYSNAME_CN), null);
                setCellValue(workbook, 0, i, 13, releaseRqrmntInfo.getOtherSystem(), null);
                setCellValue(workbook, 0, i, 14, appMap.get(Dict.NEW_ADD_SIGN), null);
                if(!CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getDataBaseAlter()) && releaseRqrmntInfo.getDataBaseAlter().equals("0")){
                    setCellValue(workbook, 0, i, 15, "是", null);
                }else{
                    setCellValue(workbook, 0, i, 15, "否", null);
                }
                if(!CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getCommonProfile()) && releaseRqrmntInfo.getCommonProfile().equals("0")){
                    setCellValue(workbook, 0, i, 16, "是", null);
                }else{
                    setCellValue(workbook, 0, i, 16, "否", null);
                }

                if(!CommonUtils.isNullOrEmpty(appMap.get(Dict.SPECIALCASE))){
                    setCellValue(workbook, 0, i, 17, appMap.get(Dict.SPECIALCASE), null);
                }else{
                    setCellValue(workbook, 0, i, 17, "否", null);
                }
                i++;
                j++;
            }
        }
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "RelaseRqrmntList.xls");
        workbook.write(resp.getOutputStream());
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

    private String queryTaskManagerStr(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception{
        Set<String> task_ids = releaseRqrmntInfo.getTask_ids();
        Map<String, Object> tasks = taskService.queryTasksByIds(task_ids);
        Set<String> names = new HashSet<>();
        for (String task_id : task_ids) {
            Map task = (Map)tasks.get(task_id);
            List<Map> masters = (List<Map>)task.get(Dict.MASTER);
            List<String> collect = masters.stream().map(e -> (String) e.get(Dict.USER_NAME_CN)).collect(Collectors.toList());
            names.addAll(collect);
        }
        return String.join(";", names);
    }

    @Override
    public void exportRqrmntInfoListByType(String release_date, String type ,List<String> groupIds, boolean isParent, HttpServletResponse resp) throws Exception {
        List<ReleaseRqrmntInfo> list = releaseRqrmntInfoService.queryRqrmntInfoListByType(release_date, type, groupIds, isParent);
        // excel格式为xlsx则使用XSSF 格式为xls则使用HSSF
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setCellValue(workbook, 0, 0, 0, "小组名", cellStyle);
        setCellValue(workbook, 0, 0, 1, "需求名称", cellStyle);
        setCellValue(workbook, 0, 0, 2, "需求单号", cellStyle);
        setCellValue(workbook, 0, 0, 3, "行内负责人", cellStyle);
        setCellValue(workbook, 0, 0, 4, "业务联系人", cellStyle);
        if(Constants.RQRMNT_POINT_QUERY.equals(type)){
            setCellValue(workbook, 0, 0, 5, "回归测试功能点", cellStyle);
        }else{
            setCellValue(workbook, 0, 0, 5, "涉及系统", cellStyle);
        }
        int i = 1;
        for (ReleaseRqrmntInfo releaseRqrmntInfo : list) {
            //过滤已经导出过的需求
            if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getExportFlag())){
                setCellValue(workbook, 0, i, 0, releaseRqrmntInfo.getGroup_name(), null);
                setCellValue(workbook, 0, i, 1, releaseRqrmntInfo.getRqrmnt_name(), null);
                setCellValue(workbook, 0, i, 2, releaseRqrmntInfo.getRqrmnt_no(), null);
                List<Map> spdb_managers = (List<Map>)releaseRqrmntInfo.getRqrmnt_spdb_manager();
                List<String> collect = spdb_managers.stream().map(e -> (String)e.get(Dict.USER_NAME_CN)).collect(Collectors.toList());
                setCellValue(workbook, 0, i, 3, String.join(",", collect), null);
                setCellValue(workbook, 0, i, 4, releaseRqrmntInfo.getRqrmntContact(), null);
                if(Constants.RQRMNT_POINT_QUERY.equals(type)){
                    setCellValue(workbook, 0, i, 5, releaseRqrmntInfo.getTestKeyNote(), null);
                }else{
                    setCellValue(workbook, 0, i, 5, releaseRqrmntInfo.getOtherSystem(), null);
                }
                i++;
                //对导出的需求新增导出标识。
                releaseRqrmntInfoService.updateRqrmntInfoFlag(releaseRqrmntInfo.getId(),"1");
            }

        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "RelaseRqrmntList.xlsx");
        workbook.write(resp.getOutputStream());
    }

    @Override
    public void exportSpecialRqrmntInfoList(String release_date, String type, List<String> groupIds, boolean isParent, HttpServletResponse resp) throws Exception {
        List<Map> list = releaseRqrmntInfoService.queryRqrmntInfoList(release_date, type, groupIds, isParent);
        List<ReleaseRqrmntInfo> rarmntList = new ArrayList();
        for (Map map : list) {
            List<ReleaseRqrmntInfo> rqrmntInfos = (List<ReleaseRqrmntInfo>)map.get(Dict.RQRMNT_LIST);
            rarmntList.addAll(rqrmntInfos);
        }
        Map result = new HashMap();
        for (ReleaseRqrmntInfo releaseRqrmntInfo : rarmntList) {
            if(CommonUtils.isNullOrEmpty(releaseRqrmntInfo.getTag())){
                continue;
            }
            if (releaseRqrmntInfo.getTag().equals(Constants.RQRMNT_TAG_LENGTH_CN) || releaseRqrmntInfo.getTag().equals(Constants.RQRMNT_TAG_CHIEF_CN)){
                if (Util.isNullOrEmpty(result.get(releaseRqrmntInfo.getTag()))) {
                    Map groupMap = new HashMap();
                    List<ReleaseRqrmntInfo> rqrGroup = new ArrayList<>();
                    rqrGroup.add(releaseRqrmntInfo);
                    groupMap.put(releaseRqrmntInfo.getGroup_id(), rqrGroup);
                    result.put(releaseRqrmntInfo.getTag(), groupMap);
                } else {
                    Map groupMap = (Map) result.get(releaseRqrmntInfo.getTag());
                    if (Util.isNullOrEmpty(groupMap.get(releaseRqrmntInfo.getGroup_id()))) {
                        List<ReleaseRqrmntInfo> rarGroup = new ArrayList<>();
                        rarGroup.add(releaseRqrmntInfo);
                        groupMap.put(releaseRqrmntInfo.getGroup_id(), rarGroup);
                    } else {
                        List<ReleaseRqrmntInfo> rqrGroup = (List<ReleaseRqrmntInfo>) groupMap.get(releaseRqrmntInfo.getGroup_id());
                        rqrGroup.add(releaseRqrmntInfo);
                    }
                }
            }
        }
        Set<String> set = result.keySet();
        if(Util.isNullOrEmpty(result)){
            throw new FdevException(ErrorConstants.RELEASE_RQRMNT_INFO_LIST_NOT_INCLUDE_SPECIAL);
        }
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle blueStyle = workbook.createCellStyle();
        blueStyle.setAlignment(HorizontalAlignment.LEFT);
        blueStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle yellowStyle = workbook.createCellStyle();
        yellowStyle.setAlignment(HorizontalAlignment.LEFT);
        yellowStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle greenStyle = workbook.createCellStyle();
        greenStyle.setAlignment(HorizontalAlignment.LEFT);
        greenStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
        greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle redStyle = workbook.createCellStyle();
        redStyle.setAlignment(HorizontalAlignment.LEFT);
        redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        int i = 0;
        for (String tag : set) {
            sheet.addMergedRegion(new CellRangeAddress(i,i,0,3));
            if(tag.equals(Constants.RQRMNT_TAG_LENGTH_CN)){
                setCellValue(workbook, 0, i, 0, tag, redStyle);
            }else{
                setCellValue(workbook, 0, i, 0, tag, yellowStyle);
            }
            i++;
            setCellValue(workbook, 0, i, 0, "组名", blueStyle);
            setCellValue(workbook, 0, i, 1, "需求编号", blueStyle);
            setCellValue(workbook, 0, i, 2, "需求名称", blueStyle);
            setCellValue(workbook, 0, i, 3, "确认书到达时间", blueStyle);
            Map groupMap = (Map)result.get(tag);
            Set<String> group = groupMap.keySet();
            for(String group_id : group){
                i++;
                Map<String, Object> group_map = userService.queryGroupDetail(group_id);
                sheet.addMergedRegion(new CellRangeAddress(i,i,0,3));
                if(!CommonUtils.isNullOrEmpty(group_map)){
                    setCellValue(workbook, 0, i, 0, (String)group_map.get(Dict.NAME), greenStyle);
                }else{
                    setCellValue(workbook, 0, i, 0, "/", blueStyle);
                }
                List<ReleaseRqrmntInfo> rqrmntInfos = (List<ReleaseRqrmntInfo>)groupMap.get(group_id);
                int j = 1;
                for (ReleaseRqrmntInfo releaseRqrmntInfo : rqrmntInfos) {
                    i++;
                    setCellValue(workbook, 0, i, 0, String.valueOf(j), null);
                    setCellValue(workbook, 0, i, 1, releaseRqrmntInfo.getRqrmnt_no(), null);
                    setCellValue(workbook, 0, i, 2, releaseRqrmntInfo.getRqrmnt_name(), null);
                    setCellValue(workbook, 0, i, 3, taskService.queryRqrmntInfoConfirmRecord(releaseRqrmntInfo), null);
                    j++;
                }
            }
            i++;
        }
        //单元格自动设置列宽
        for (int j = 0; j <= 7; j++) {
            sheet.autoSizeColumn(i);
        }
        resp.reset();
        resp.setContentType("application/octet-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Content-Disposition", "attachment;filename=" + "RelaseRqrmntList.xls");
        workbook.write(resp.getOutputStream());

    }

    private byte[] getFileContent(String moduleName, String minioPath) {
        Map<String,String> param=new HashMap<>();
        param.put(Dict.MODULE_NAME,moduleName);
        param.put(Dict.PATH,minioPath);
        try {
            byte[] result = (byte[]) restTransport.submitGet("filesDownload", param);
            return result;
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
    }

    /**
	 * excel填值
	 * @param
	 * @param sheetIndex
	 * @param rowIndex
	 * @param cellIndex
	 * @param cellValue
	 * @throws Exception
	 */
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

    /**
     * excel填值超链接
     * @param
     * @param sheetIndex
     * @param rowIndex
     * @param cellIndex
     * @param cellValue
     * @throws Exception
     */
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
}
