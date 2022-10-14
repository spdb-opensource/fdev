package com.auto.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auto.dao.CaseDetailMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.CaseDetail;
import com.auto.service.ICaseDetailService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

@Service
public class CaseDetailServiceImpl implements ICaseDetailService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private CaseDetailMapper caseDetailMapper;

    @Override
    public void addCaseDetail(Map<String, String> map) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        String testcaseNo = map.getOrDefault(Dict.TESTCASENO, "");
        String stepNo = map.getOrDefault(Dict.STEPNO, "");
        String moduleId = map.getOrDefault(Dict.MODULEID, "");
        String elementType = map.getOrDefault(Dict.ELEMENTTYPE, "");
        String assertId = map.getOrDefault(Dict.ASSERTID, "");
        String elementId = map.getOrDefault(Dict.ELEMENTID, "");
        String elementData = map.getOrDefault(Dict.ELEMENTDATA, "");
        String exeTimes = map.getOrDefault(Dict.EXETIMES, "");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        CaseDetail caseDetail = new CaseDetail(testcaseNo, stepNo, moduleId, elementType,
                assertId, elementId, elementData, exeTimes, "0", time,
                time, userNameEn);
        List<Map<String, String>> cdList = caseDetailMapper.queryCaseDetailByCaseNoAndStep(testcaseNo, stepNo, null);
        if(cdList.size() == 0){
            try {
                caseDetailMapper.addCaseDetail(caseDetail);
            } catch (Exception e) {
                logger.error("fail to create casseDetail");
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
            }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"当前案例STEP已存在"} );
        }
    }

    @Override
    public List<Map<String, String>> queryCaseDetail(Map<String, String> map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            result = caseDetailMapper.queryCaseDetail(search, valid);
        } catch (Exception e) {
            logger.error("fail to query casseDetail" + e);
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return result;
    }
    
    @Override
    public List<Map<String, String>> queryCaseDetailByTestCaseNo(String testcaseNo) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            result = caseDetailMapper.queryCaseDetailByTestCaseNo(testcaseNo);
        } catch (Exception e) {
            logger.error("fail to query casseDetail" + e);
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return result;
    }

    @Override
    public void deleteCaseDetail(Map<String, String[]> map) throws Exception {
    	List<String> detailIds = Arrays.asList(map.get(Dict.DETAILID));
        String detailId = "'" + String.join("','", detailIds) + "'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
            caseDetailMapper.deleteCaseDetail(detailId, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete casseDetail");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateCaseDetail(Map<String, String> map) throws Exception {
        String detailId = String.valueOf(map.get(Dict.DETAILID));
        String testcaseNo = String.valueOf(map.getOrDefault(Dict.TESTCASENO, ""));
        String stepNo = String.valueOf(map.getOrDefault(Dict.STEPNO, ""));
        String moduleId = String.valueOf(map.getOrDefault(Dict.MODULEID, ""));
        String elementType = String.valueOf(map.getOrDefault(Dict.ELEMENTTYPE, ""));
        String assertId = String.valueOf(map.getOrDefault(Dict.ASSERTID, ""));
        String elementId = String.valueOf(map.getOrDefault(Dict.ELEMENTID, ""));
        String elementData = String.valueOf(map.getOrDefault(Dict.ELEMENTDATA, ""));
        String exeTimes = String.valueOf(map.getOrDefault(Dict.EXETIMES, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<Map<String, String>> cdList = caseDetailMapper.queryCaseDetailByCaseNoAndStep(testcaseNo, stepNo, detailId);
        if(cdList.size() == 0){
	        try {
	            caseDetailMapper.updateCaseDetail(detailId, testcaseNo, stepNo, moduleId, elementType, assertId,
	                    elementId, elementData, exeTimes, userNameEn, time);
	        } catch (Exception e) {
	            logger.error("fail to update casseDetail");
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"当前案例STEP已存在"} );
        }
    }
}
