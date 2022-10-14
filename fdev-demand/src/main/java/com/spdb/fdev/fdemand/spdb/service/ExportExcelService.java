package com.spdb.fdev.fdemand.spdb.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;

public interface ExportExcelService {
	public void ToExportAssessExcelByDemandId (String id,HttpServletResponse resp) throws Exception;

	public void ExportAssessExcelByDemandId(Map<String, String> data, List<FdevImplementUnit> unitdatas,
			String sheetname ,HttpServletResponse resp ) throws Exception;
	public void exportDemandsExcel(Map map,HttpServletResponse resp) throws Exception;
	public Map importDemandExcel(MultipartFile excel,String demandId) throws Exception;
	public void exportModelExcel(HttpServletResponse resp) throws Exception;
}
