package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.ScanRecordDao;
import com.spdb.fdev.fdevinterface.spdb.entity.ScanRecord;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import com.spdb.fdev.fdevinterface.spdb.service.ScanRecordService;
import com.spdb.fdev.transport.RestTransport;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ScanRecordServiceImpl implements ScanRecordService {

	private Logger logger = LoggerFactory.getLogger(ScanRecordServiceImpl.class);
    @Resource
    private ScanRecordDao scanRecordDao;
    @Resource
    private InterfaceLazyInitService interfaceLazyInitService;
    
    @Autowired
    private RestTransport restTransport;
    @Value("${finterface.nas}")
    private String scanRecordPath;
    
    @Override
    public void save(ScanRecord scanRecord) {
        Map map = new HashMap();
        map.put("code", "0");
        map.put("msg", "不存在该类型的接口！");
        if (scanRecord != null) {
            if (scanRecord.getRest() == null) {
                scanRecord.setRest(map);
            }
            if (scanRecord.getRestRel() == null) {
                scanRecord.setRestRel(map);
            }
            if (scanRecord.getSoap() == null) {
                scanRecord.setSoap(map);
            }
            if (scanRecord.getSoapRel() == null) {
                scanRecord.setSoapRel(map);
            }
            if (scanRecord.getSopRel() == null) {
                scanRecord.setSopRel(map);
            }
            if (scanRecord.getTrans() == null) {
                scanRecord.setTrans(map);
            }
            if (scanRecord.getTransRel() == null) {
                scanRecord.setTransRel(map);
            }
            if (scanRecord.getRouter() == null) {
                scanRecord.setRouter(map);
            }
            scanRecordDao.save(scanRecord);
        }
    }

    @Override
    public Map getScanRecord(String serviceId, String branch, String type, Integer page, Integer pageNum, String startTime, String endTime, String groupId, String isScanSuccessFlag, String recentlyScanFlag) {
    	List<String> serviceIdList = new ArrayList<String>() ;
    	//查询当前应用小组的所有应用
    	if( !StringUtils.isEmpty(groupId) ) {
    		List<Map<String, Object>> serviceList = interfaceLazyInitService.queryPagination(groupId);
    		for (Map<String, Object> serviceMap : serviceList) {
    			serviceIdList.add((String) serviceMap.get(Dict.NAME_EN));
    			
    		}
    	}
    	List<ScanRecord> newScanRecordList  = new ArrayList<ScanRecord>();
    	List<ScanRecord> returnScanRecordList  = new ArrayList<ScanRecord>();
    	if(StringUtils.isEmpty(groupId) || !Util.isNullOrEmpty(serviceIdList) ) {
    		Map scanRecordMap = scanRecordDao.getScanRecord(serviceId, branch, type,startTime,endTime,serviceIdList,recentlyScanFlag);
        	List<ScanRecord> scanRecordList  = (List<ScanRecord>) scanRecordMap.get(Dict.LIST);
        	for (ScanRecord scanRecord : scanRecordList) {
        		//判断是否筛选最新扫描记录
        		if("1".equals(recentlyScanFlag)) {
        			//最新查询记录的分组信息
            		List<ScanRecord> scanRecordGroupList = (List<ScanRecord>) scanRecordMap.get(Dict.GROUPLIST);
            		for (ScanRecord scanRecordGroup : scanRecordGroupList) {
            			if(scanRecordGroup.getServiceId().equals(scanRecord.getServiceId())&&
            					scanRecordGroup.getBranch().equals(scanRecord.getBranch())&&
            					scanRecordGroup.getScanTime().equals(scanRecord.getScanTime())) {
            				//是否筛选扫描成功记录
            				if("1".equals(isScanSuccessFlag)) {
            					if(!Util.isNullOrEmpty(scanRecord.getRest()) && !Util.isNullOrEmpty(scanRecord.getRestRel())&&
                    					!Util.isNullOrEmpty(scanRecord.getSoap()) && !Util.isNullOrEmpty(scanRecord.getRestRel())&&
                    					!Util.isNullOrEmpty(scanRecord.getSopRel()) && !Util.isNullOrEmpty(scanRecord.getTrans())&&
                    					!Util.isNullOrEmpty(scanRecord.getTransRel()) && !Util.isNullOrEmpty(scanRecord.getRouter())) {
            						if(!("2".equals(scanRecord.getRest().get(Dict.CODE)) || "2".equals(scanRecord.getRestRel().get(Dict.CODE)) || 
                							"2".equals(scanRecord.getSoap().get(Dict.CODE)) || "2".equals(scanRecord.getSoapRel().get(Dict.CODE)) || 
                							"2".equals(scanRecord.getSopRel().get(Dict.CODE)) || "2".equals(scanRecord.getTrans().get(Dict.CODE)) || 
                							"2".equals(scanRecord.getTransRel().get(Dict.CODE)) || "2".equals(scanRecord.getRouter().get(Dict.CODE)))) {
                						newScanRecordList.add(scanRecord);
                					}
            					}
            				}else {
            					newScanRecordList.add(scanRecord);
            				}
            			}
            		}
            	}else if("1".equals(isScanSuccessFlag)) {
            		//是否筛选扫描成功记录
        			if(!Util.isNullOrEmpty(scanRecord.getRest()) && !Util.isNullOrEmpty(scanRecord.getRestRel())&&
        					!Util.isNullOrEmpty(scanRecord.getSoap()) && !Util.isNullOrEmpty(scanRecord.getRestRel())&&
        					!Util.isNullOrEmpty(scanRecord.getSopRel()) && !Util.isNullOrEmpty(scanRecord.getTrans())&&
        					!Util.isNullOrEmpty(scanRecord.getTransRel()) && !Util.isNullOrEmpty(scanRecord.getRouter())) {
        				if(!("2".equals(scanRecord.getRest().get(Dict.CODE)) || "2".equals(scanRecord.getRestRel().get(Dict.CODE)) ||
    							"2".equals(scanRecord.getSoap().get(Dict.CODE)) || "2".equals(scanRecord.getSoapRel().get(Dict.CODE)) ||
    							"2".equals(scanRecord.getSopRel().get(Dict.CODE)) || "2".equals(scanRecord.getTrans().get(Dict.CODE)) ||
    							"2".equals(scanRecord.getTransRel().get(Dict.CODE)) || "2".equals(scanRecord.getRouter().get(Dict.CODE)))) {
    						newScanRecordList.add(scanRecord);
    					}
            	} 
            	}else {
            		newScanRecordList.addAll(scanRecordList);
            		break;
            	}
            }
        	
    	}
    	Map map = new HashMap();
    	if (page == null) {
			page = 1;
		}
		if (pageNum == null) {	
			pageNum = 10;
		}
		if(pageNum == 0){
			pageNum = Integer.MAX_VALUE;
		}
		//分页处理
		int start = (page - 1) * pageNum;
		int end = start + pageNum;
		//页面参数不合法返回空list
		if(start >= newScanRecordList.size() || end <= 0 || start < 0)
			map.put(Dict.LIST, new ArrayList<>());
		else{//end值超出列表范围则赋值为长度-1
			end = end > newScanRecordList.size() ? newScanRecordList.size() : end;
			returnScanRecordList.addAll(newScanRecordList.subList(start, end));
			for (ScanRecord scanRecord : returnScanRecordList) {
	    		String scanRecordServiceId = scanRecord.getServiceId();
				scanRecord.setAppId(interfaceLazyInitService.getAppIdByName(scanRecordServiceId));
	            scanRecord.setGroup(interfaceLazyInitService.getAppGroupName(scanRecordServiceId));
	    	}
			map.put(Dict.LIST, returnScanRecordList);
        }
    	map.put(Dict.TOTAL, newScanRecordList.size());
        return map;
    }

    @Override
    public String createExcel(List<ScanRecord> scanRecordList) {
        XSSFWorkbook workBook = new XSSFWorkbook();
        XSSFFont font = workBook.createFont();
        font.setBold(true);
        XSSFSheet sheet = workBook.createSheet("INDEX");
        XSSFRow hssfRow = sheet.createRow(0);
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        XSSFCellStyle cellStyle0 = getXSSFCellStyle0(workBook, font);
        String[] array = {"应用英文名", "应用所属小组", "分支", "触发扫描方式", "REST提供", "REST调用", "SOAP提供", "SOAP调用", "SOP调用", "交易提供", "交易调用", "路由","扫描时间"};
        for (int i = 0; i < array.length; i++) {
            XSSFCell headCell = hssfRow.createCell(i);
            headCell.setCellValue(array[i]);
            addBorder(headCell, sheet, cellStyle0);
        }

        for (int i = 0; i < scanRecordList.size(); i++) {
        	hssfRow = sheet.createRow(i + 1);
            ScanRecord scanRecord = scanRecordList.get(i);
            XSSFCell cell = hssfRow.createCell(0);
            cell.setCellValue(scanRecord.getServiceId());//应用英文名
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(1);
            cell.setCellValue(scanRecord.getGroup());//应用所属小组
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(2);
            cell.setCellValue(scanRecord.getBranch());//分支
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(3);
            cell.setCellValue(scanRecord.getType());//触发扫描方式
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(4);
            cell.setCellValue(getScanResult(scanRecord.getRest()));//REST提供
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(5);
            cell.setCellValue(getScanResult(scanRecord.getRestRel()));//REST调用
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(6);
            cell.setCellValue(getScanResult(scanRecord.getSoap()));//SOAP提供
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(7);
            cell.setCellValue(getScanResult(scanRecord.getSoapRel()));//SOAP调用
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(8);
            cell.setCellValue(getScanResult(scanRecord.getSopRel()));//SOP调用
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(9);
            cell.setCellValue(getScanResult(scanRecord.getTrans()));//交易提供
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(10);
            cell.setCellValue(getScanResult(scanRecord.getTransRel()));//交易调用
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(11);
            cell.setCellValue(getScanResult(scanRecord.getRouter()));//路由
            addBorder(cell, sheet, cellStyle);
            cell = hssfRow.createCell(12);
            cell.setCellValue(scanRecord.getScanTime());//扫描时间
            addBorder(cell, sheet, cellStyle);
        }
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = sdf.format(date);
        String excelPath = scanRecordPath + Constants.SCAN_RECORD_FILE_PATH + Constants.SCAN_RECORD_FILE + timeStamp + ".xlsx";
        String fileName = Constants.SCAN_RECORD_FILE + timeStamp + ".xlsx";
        FileUtil.newExport(excelPath, scanRecordPath + Constants.SCAN_RECORD_FILE_PATH, workBook);
        return fileName;
    }
    private String getScanResult(Map map) {
    	String scanResult = "不涉及";
    	if(!Util.isNullOrEmpty(map) && !"0".equals(map.get(Dict.CODE))) {
    		if("1".equals(map.get(Dict.CODE))) {
    			scanResult = "扫描成功";
    		}else if("2".equals(map.get(Dict.CODE))) {
    			scanResult = (String) map.get(Dict.MSG);
    		}
    	}
        return scanResult;
    }
    /**
     * 添加表格边框及样式
     *
     * @param cell
     * @param sheet
     * @param cellStyle
     */
    private void addBorder(XSSFCell cell, XSSFSheet sheet, XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cell.setCellStyle(cellStyle);
        sheet.setColumnWidth(cell.getColumnIndex(), cell.getStringCellValue().getBytes().length * 400 > 20000 ? 9000 : cell.getStringCellValue().getBytes().length * 400);
    }
    
    private XSSFCellStyle getXSSFCellStyle0(XSSFWorkbook workBook, XSSFFont font) {
        XSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
        cellStyle.setFont(font);
        return cellStyle;
    }
    
    /**
     * 导出文件 接口
     *
     * @param fileName
     * @return
     */
    @Override
    public void exportFile(String fileName, HttpServletResponse response) throws Exception {
        String excelPath = scanRecordPath + Constants.SCAN_RECORD_FILE_PATH + fileName;
        FileUtil.commonDown(fileName, excelPath, response);
    }

	@Override
	public void timingClearScanRecord() {
		logger.info("===============================扫描记录清理开始=================");
		long currentTimeMillis = System.currentTimeMillis();
		int clearScanRecord = scanRecordDao.timingClearScanRecord();
		logger.info("===============================扫描记录清理结束，清理了数据条数：{} ,耗时：{} 。",clearScanRecord,System.currentTimeMillis()-currentTimeMillis);
	}
}
