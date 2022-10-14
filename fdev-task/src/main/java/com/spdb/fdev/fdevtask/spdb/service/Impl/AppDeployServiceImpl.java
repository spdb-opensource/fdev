package com.spdb.fdev.fdevtask.spdb.service.Impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.dao.IAppDeployReviewDao;
import com.spdb.fdev.fdevtask.spdb.entity.AppDeployReview;
import com.spdb.fdev.fdevtask.spdb.service.IAppDeployService;
import com.spdb.fdev.fdevtask.spdb.service.IReleaseTaskApi;

@Service
@RefreshScope
public class AppDeployServiceImpl implements IAppDeployService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IAppDeployReviewDao appDeployReviewDao;
	
	@Autowired
    private IReleaseTaskApi ReleaseTaskApi;
	
	@Override
	public AppDeployReview applayDeploy(AppDeployReview applayDeploy) throws Exception {
		return appDeployReviewDao.applayDeploy(applayDeploy);
	}

	@Override
	public Map<String,Object> queryApproved(Map<String, Object> param) throws Exception {
		return appDeployReviewDao.queryApproved(param);
	}

	@Override
	public List<AppDeployReview> queryNotApproved() throws Exception {
		return appDeployReviewDao.queryNotApproved();
	}

	@Override
	public List<AppDeployReview> queryAppDeploy(List<String> ids) throws Exception {
		return appDeployReviewDao.queryAppDeploy(ids);
	}

	@Override
	public AppDeployReview queryAppDeployByTaskId(String taskId) throws Exception {
		return appDeployReviewDao.queryAppDeployByTaskId(taskId);
	}

	@Override
	public AppDeployReview queryAppImage(String appName, String queryType) throws Exception {
		//查询caas最新记录
		List<AppDeployReview> caasList = appDeployReviewDao.queryAppImage(appName, "caas");
		//查询scc最新记录
		List<AppDeployReview> sccList = appDeployReviewDao.queryAppImage(appName, "scc");
		
		String caasImage = "";
		String sccImage = "";
		
		if(!Util.isNullOrEmpty(caasList)) {
			for (Iterator iterator = caasList.iterator(); iterator.hasNext();) {
				AppDeployReview appDeployReview = (AppDeployReview) iterator.next();
				caasImage = appDeployReview.getImageCaasUrl();
				if(!Util.isNullOrEmpty(caasImage)) {
					break;
				}
			}
		}
		
		if(!Util.isNullOrEmpty(sccList)) {
			for (Iterator iterator = sccList.iterator(); iterator.hasNext();) {
				AppDeployReview appDeployReview = (AppDeployReview) iterator.next();
				sccImage = appDeployReview.getImageSccUrl();
				if(!Util.isNullOrEmpty(sccImage)) {
					break;
				}
			}
		}
		
		AppDeployReview applayDeploy = new AppDeployReview();
		applayDeploy.setImageCaasUrl(caasImage);
		applayDeploy.setImageSccUrl(sccImage);
		return applayDeploy;
	}

	@Override
	public List<AppDeployReview> queryOtherTasks(String appNames, String ids) throws Exception {
		List<AppDeployReview> resultList = new ArrayList<AppDeployReview>();
		List<AppDeployReview> deployAllList = new ArrayList<AppDeployReview>();
		if(!Util.isNullOrEmpty(appNames)) {
			String [] appList  = appNames.split(",");
			String apps = null;
			for(String appName:appList) {
				if(!Util.isNullOrEmpty(appName)) {
					if(!apps.contains(appName)) {
						List<AppDeployReview> appDeployList = appDeployReviewDao.queryAppImage(appName, "0");
						deployAllList.addAll(appDeployList);
						apps = apps + "," + appName;
					}
				}
			}
		}
		if (!Util.isNullOrEmpty(deployAllList) && !Util.isNullOrEmpty(ids)) {
			for (Iterator iterator = deployAllList.iterator(); iterator.hasNext();) {
				AppDeployReview appDeployReview = (AppDeployReview) iterator.next();
				String taskName = appDeployReview.getTaskName();
				if(!Util.isNullOrEmpty(taskName)) {
					if(!ids.contains(appDeployReview.getTaskId())) {
						resultList.add(appDeployReview);
					}	
				}
			}
		}
		return resultList;
	}

	@Override
	public List<AppDeployReview> queryDeployTask(String appNames, String queryType) throws Exception {
		List<AppDeployReview> deployAllList = new ArrayList<AppDeployReview>();
		if(!Util.isNullOrEmpty(appNames)) {
			String [] appList  = appNames.split(",");
			String apps = "";
			for(String appName:appList) {
				if(!Util.isNullOrEmpty(appName)) {
					if(!apps.contains(appName)) {//TODO 此处存在名称相似bug
						List<AppDeployReview> appDeployList = appDeployReviewDao.queryDeployTask(appName, queryType);
						deployAllList.addAll(appDeployList);
						apps = apps + "," + appName;
					}
				}
			}
		}
		return deployAllList;
	}

	@Override
	public AppDeployReview savePiplineAppInfo(AppDeployReview applayDeploy) throws Exception {
		return appDeployReviewDao.savePiplineAppInfo(applayDeploy);
	}

	@Override
	public void deploy(Map<String, Object> param) throws Exception {
		ReleaseTaskApi.deploy(param);
	}

	@Override
	public void update(AppDeployReview applayDeploy) throws Exception {
		appDeployReviewDao.update(applayDeploy);
	}

	@Override
	public void export(Map<String, Object> resultMap,HttpServletResponse resp) throws Exception {
		List<AppDeployReview> list = (List) resultMap.get(Dict.LIST);
		if(!Util.isNullOrEmpty(list)) {
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
	        setCellValue(workbook, 0, 0, 0, "任务名称", cellStyle);
	        setCellValue(workbook, 0, 0, 1, "需求名称", cellStyle);
	        setCellValue(workbook, 0, 0, 2, "所属应用", cellStyle);
	        setCellValue(workbook, 0, 0, 3, "部署环境", cellStyle);
	        setCellValue(workbook, 0, 0, 4, "申请人", cellStyle);
	        setCellValue(workbook, 0, 0, 5, "申请原因", cellStyle);
	        setCellValue(workbook, 0, 0, 6, "所属小组", cellStyle);
	        setCellValue(workbook, 0, 0, 7, "操作管理员", cellStyle);
	        setCellValue(workbook, 0, 0, 8, "操作时间", cellStyle);
	        int i = 1;
	        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				AppDeployReview appDeployReview = (AppDeployReview) iterator.next();
				setCellValue(workbook, 0, i, 0, appDeployReview.getTaskName(), greenStyle);
				setCellValue(workbook, 0, i, 1, appDeployReview.getOaContactName(), greenStyle);
				setCellValue(workbook, 0, i, 2, appDeployReview.getApplicationNameEn(), greenStyle);
				setCellValue(workbook, 0, i, 3, appDeployReview.getDeployEnv(), greenStyle);
				setCellValue(workbook, 0, i, 4, appDeployReview.getApplayUserNameZh(), greenStyle);
				setCellValue(workbook, 0, i, 5, appDeployReview.getOverdueReason(), greenStyle);
				setCellValue(workbook, 0, i, 6, appDeployReview.getApplayUserOwnerGroup(), greenStyle);
				setCellValue(workbook, 0, i, 7, appDeployReview.getReviewUserNameZh(), greenStyle);
				setCellValue(workbook, 0, i, 8, appDeployReview.getReviewTime(), greenStyle);
				i++;
			}
	        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
	        resp.reset();
	        resp.setContentType("application/octet-stream");
	        resp.setCharacterEncoding("UTF-8");
	        resp.setHeader("Access-Control-Allow-Origin", "*");
	        resp.setHeader("Content-Disposition", "attachment;filename=" + "deployInfo.xls");
	        workbook.write(resp.getOutputStream());
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
            throw new FdevException("生成excel失败");
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

	@Override
	public void modifyDeloyStatus(Map<String, Object> param) {
		appDeployReviewDao.modifyDeployStatus(param);
	}

}
