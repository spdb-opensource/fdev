package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface IGenerateExcelService {
	
	String generateProdExcel(List<String> prods) throws Exception;

    void exportRqrmntInfoList(String release_date, String type, List<String> groupIds, boolean isParent, HttpServletResponse resp) throws Exception;

    void exportRqrmntInfoListByType(String release_date, String type, List<String> groupIds, boolean isParent, HttpServletResponse resp) throws Exception;

    void exportSpecialRqrmntInfoList(String release_date, String type, List<String> groupIds, boolean isParent, HttpServletResponse resp) throws Exception;
}
