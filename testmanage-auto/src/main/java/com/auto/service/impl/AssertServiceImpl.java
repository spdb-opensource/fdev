package com.auto.service.impl;

import com.auto.dao.AssertMapper;
import com.auto.dao.DataMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.Assert;
import com.auto.entity.Data;
import com.auto.service.IAssertService;
import com.auto.service.IDataService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AssertServiceImpl implements IAssertService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private AssertMapper assertMapper;

    @Override
    public void addAssert(Assert asrt) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        asrt.setLastOpr(userNameEn);
        asrt.setCreateTime(time);
        asrt.setModifyTime(time);
        asrt.setDeleted("0");
        List<Map<String, String>> aList =  assertMapper.queryAssertByLabel(asrt.getLabel(), null);
        if(aList.size() == 0){
            try {
                assertMapper.addAssert(asrt);
            } catch (Exception e) {
                logger.error("fail to add assert");
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
            }
        }else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"预想数据标签已存在"} );
        }
    }

    @Override
    public List<Map<String, String>> queryAssert(Map<String, String> map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        List<Map<String, String>> asserts = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            asserts = assertMapper.queryAssert(search, valid);
        } catch (Exception e) {
            logger.error("fail to query assert");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        Map<String, String> data;
        if(!Util.isNullOrEmpty(asserts)){
            for(Map<String, String> d : asserts){
                data = new HashMap<>();
                data.put(Dict.ASSERTID, String.valueOf(d.get(Dict.ASSERTID)));
                data.put(Dict.LABEL, d.get(Dict.LABEL));
                data.put(Dict.DELETED, d.get(Dict.DELETED));
                data.put(Dict.CREATETIME, d.get(Dict.CREATETIME));
                data.put(Dict.MODIFYTIME, d.get(Dict.MODIFYTIME));
                data.put(Dict.LASTOPR, d.get(Dict.LASTOPR));
                for(int i = 0; i<10; i++){
                    String caseId = "ASSERTDATA"+i;
                    if(!Util.isNullOrEmpty(d.get(caseId))&&!(d.get(caseId)).contains("NULL")){
                        data.put(caseId, d.get(caseId));
                    }
                }
                result.add(data);
            }
        }
        return result;
    }
    
    @Override
    public List<Map<String, String>> queryAssertByTestCaseNo(Map<String, String> map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        List<Map<String, String>> asserts = new ArrayList<>();
        String testcaseNo = map.getOrDefault(Dict.TESTCASENO, "");
        try {
            asserts = assertMapper.queryAssertByTestCaseNo(testcaseNo);
        } catch (Exception e) {
            logger.error("fail to query assert");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        if(!Util.isNullOrEmpty(asserts)){
            for(Map<String, String> d : asserts){
            	Map<String, String> data = new HashMap<>();
                for(int i = 0; i<10; i++){
                    String assertId = "ASSERTDATA"+i;
                    if(!Util.isNullOrEmpty(d.get(assertId))&&!(d.get(assertId)).contains("NULL")){
                        data.put(assertId, d.get(assertId));
                    }
                }
                result.add(data);
            }
        }
        return result;
    }

    @Override
    public void deleteAssert(Map map) throws Exception {
        List<String> asserts = (List<String>) map.get(Dict.ASSERTID);
        String asrt = "'" + String.join("','", asserts) + "'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
            assertMapper.deleteAssert(asrt, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete assert");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateAssert(Map map) throws Exception {
        String assertId = String.valueOf(map.get(Dict.ASSERTID));
        String label = String.valueOf(map.getOrDefault(Dict.LABEL, ""));
        map.remove(Dict.LABEL);
        map.remove(Dict.ASSERTID);
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<Map<String, String>> aList =  assertMapper.queryAssertByLabel(label, assertId);
        if(aList.size() == 0){
	        try {
	            assertMapper.updateAssert(assertId, label, userNameEn, time, map);
	        } catch (Exception e) {
	            logger.error("fail to update assert" +e);
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
        }else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"预想数据标签已存在"} );
        }
    }
}
