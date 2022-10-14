package com.auto.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auto.dao.CaseDetailMapper;
import com.auto.dao.CaseMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.CaseDetail;
import com.auto.entity.Testcase;
import com.auto.service.ICaseService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

@Service
public class CaseServiceImpl implements ICaseService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private CaseMapper caseMapper;
    @Autowired
    private CaseDetailMapper caseDetailMapper;

    @Override
    public void addCase(Map<String, String> map) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        String dataId = map.getOrDefault(Dict.DATAID, "");
        String userId = map.getOrDefault(Dict.USERID, "");
        String funcPoint = map.getOrDefault(Dict.FUNCPOINT, "");
        String testcaseName = map.getOrDefault(Dict.TESTCASENAME, "");
        String precondition = map.getOrDefault(Dict.PRECONDITION, "");
        String testcaseDesc = map.getOrDefault(Dict.TESTCASEDESC, "");
        String expectedResult = map.getOrDefault(Dict.EXPECTEDRESULT, "");
        String validFlag = map.getOrDefault(Dict.VALIDFLAG, "");
        String menuSheetId = map.getOrDefault(Dict.MENUSHEETID, "");
        String assertId = map.getOrDefault(Dict.ASSERTID, "");
        String testcaseNo_multi = map.getOrDefault(Dict.TESTCASENO, "");
        String isMulti = map.getOrDefault(Dict.ISMULTI, "0");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        Testcase testcase = new Testcase(dataId, userId, funcPoint, testcaseName,
                precondition, testcaseDesc, expectedResult, validFlag, menuSheetId, assertId,
                "0", time, time, userNameEn);
        List<Map<String, String>> cList= caseMapper.queryCaseByCaseName(testcaseName, null);
    	if(cList.size() == 0){
            try {
                caseMapper.addCase(testcase);
                //复用案例明细
                if(isMulti.equals("1") && !testcaseNo_multi.isEmpty()){
                	List<Map<String, String>> cdList = caseDetailMapper.queryCaseDetailByTestCaseNo(testcaseNo_multi);
                	for (Map<String, String> cd : cdList) {
                		CaseDetail caseDetail = new CaseDetail();
                		caseDetail.setTestcaseNo(testcase.getTestcaseNo().toString());
                		caseDetail.setStepNo(cd.get(Dict.STEPNO));
                		caseDetail.setModuleId(cd.get(Dict.MODULEID));
                		caseDetail.setElementType(cd.get(Dict.ELEMENTTYPE));
                		caseDetail.setAssertId(cd.get(Dict.ASSERTID));
                		caseDetail.setElementId(cd.get(Dict.ELEMENTID));
                		caseDetail.setElementData(cd.get(Dict.ELEMENTDATA));
                		caseDetail.setExeTimes(cd.get(Dict.EXETIMES));
                		caseDetail.setDeleted("0");
                		caseDetail.setCreateTime(time);
                		caseDetail.setModifyTime(time);
                        caseDetail.setLastOpr(userNameEn);
                		caseDetailMapper.addCaseDetail(caseDetail);
					}
                }
            } catch (Exception e) {
                logger.error("fail to create case");
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
            }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"案例名称已存在"} );
    	}
    }

    @Override
    public List<Map<String, String>> queryCase(Map<String, String> map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        String flag = map.getOrDefault(Dict.FLAG, "");
        try {
            result = caseMapper.queryCase(search, valid, flag);
        } catch (Exception e) {
            logger.error("fail to query case");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return result;
    }

    @Override
    public void deleteCase(Map map) throws Exception {
        List<String> testcaseNos = (List<String>) map.get(Dict.TESTCASENO);
        String testcaseNo = "'" + String.join("','", testcaseNos) + "'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
            caseMapper.deleteCase(testcaseNo, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete case");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateCase(Map map) throws Exception {
        String testcaseNo = String.valueOf(map.get(Dict.TESTCASENO));
        String dataId = String.valueOf(map.getOrDefault(Dict.DATAID, ""));
        String userId = String.valueOf(map.getOrDefault(Dict.USERID, ""));
        String funcPoint = String.valueOf(map.getOrDefault(Dict.FUNCPOINT, ""));
        String testcaseName = String.valueOf(map.getOrDefault(Dict.TESTCASENAME, ""));
        String precondition = String.valueOf(map.getOrDefault(Dict.PRECONDITION, ""));
        String testcaseDesc = String.valueOf(map.getOrDefault(Dict.TESTCASEDESC, ""));
        String expectedResult = String.valueOf(map.getOrDefault(Dict.EXPECTEDRESULT, ""));
        String validFlag = String.valueOf(map.getOrDefault(Dict.VALIDFLAG, ""));
        String menuSheetId = String.valueOf(map.getOrDefault(Dict.MENUSHEETID, ""));
        String assertId = String.valueOf(map.getOrDefault(Dict.ASSERTID, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<Map<String, String>> cList= caseMapper.queryCaseByCaseName(testcaseName, testcaseNo);
        if(cList.size() == 0){
            try {
                caseMapper.updateCase(testcaseNo, dataId, userId, funcPoint, testcaseName, precondition,
                        testcaseDesc, expectedResult, validFlag, menuSheetId, assertId, userNameEn, time);
            } catch (Exception e) {
                logger.error("fail to update case");
                throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
            }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"案例名称已存在"} );
        }
    }
}
