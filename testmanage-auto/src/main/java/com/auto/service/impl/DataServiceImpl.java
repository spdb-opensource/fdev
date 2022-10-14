package com.auto.service.impl;

import com.auto.dao.DataMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.Data;
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
public class DataServiceImpl implements IDataService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private DataMapper dataMapper;

    @Override
    public void addData(Data data) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        data.setLastOpr(userNameEn);
        data.setCreateTime(time);
        data.setModifyTime(time);
        data.setDeleted("0");
        List<Map<String, String>> dList = dataMapper.queryDataByLabel(data.getLabel(), null);
        if(dList.size() == 0){
            try {
                dataMapper.addData(data);
            } catch (Exception e) {
                logger.error("fail to add data");
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
            }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"测试数据标签已存在"} );
        }

    }

    @Override
    public List<Map<String, String>> queryData(Map<String, String> map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        List<Map<String, String>> datas = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            datas = dataMapper.queryData(search, valid);
        } catch (Exception e) {
            logger.error("fail to query data");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        Map<String, String> data;
        if(!Util.isNullOrEmpty(datas)){
            for(Map<String, String> d : datas){
                data = new HashMap<>();
                data.put(Dict.DATAID, String.valueOf(d.get(Dict.DATAID)));
                data.put(Dict.LABEL, d.get(Dict.LABEL));
                data.put(Dict.DELETED, d.get(Dict.DELETED));
                data.put(Dict.CREATETIME, d.get(Dict.CREATETIME));
                data.put(Dict.MODIFYTIME, d.get(Dict.MODIFYTIME));
                data.put(Dict.LASTOPR, d.get(Dict.LASTOPR));
                for(int i = 0; i<50; i++){
                    String caseId = "CASE"+i;
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
    public List<Map<String, String>> queryDataByTestCaseNo(Map<String, String> map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        List<Map<String, String>> datas = new ArrayList<>();
        String testcaseNo = map.getOrDefault(Dict.TESTCASENO, "");
        try {
            datas = dataMapper.queryDataByTestCaseNo(testcaseNo);
        } catch (Exception e) {
            logger.error("fail to query data");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        if(!Util.isNullOrEmpty(datas)){
            for(Map<String, String> d : datas){
            	Map<String, String> data = new HashMap<>();
                for(int i = 0; i<50; i++){
                    String caseId = "CASE"+i;
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
    public List<Map<String, String>> queryDataByModuleId(Map<String, String> map) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        List<Map<String, String>> datas = new ArrayList<>();
        String moduleId = map.getOrDefault(Dict.MODULEID, "");
        try {
            datas = dataMapper.queryDataByModuleId(moduleId);
        } catch (Exception e) {
            logger.error("fail to query data");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        if(!Util.isNullOrEmpty(datas)){
            for(Map<String, String> d : datas){
            	Map<String, String> data = new HashMap<>();
                for(int i = 0; i<50; i++){
                    String caseId = "CASE"+i;
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
    public void deleteData(Map map) throws Exception {
        List<String> dataIds = (List<String>) map.get(Dict.DATAID);
        String dataId = "'" + String.join("','", dataIds) + "'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
            dataMapper.deleteData(dataId, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete data");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateData(Map map) throws Exception {
        String dataId = String.valueOf(map.get(Dict.DATAID));
        String label = String.valueOf(map.getOrDefault(Dict.LABEL, ""));
        map.remove(Dict.LABEL);
        map.remove(Dict.DATAID);
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        Map blankMap = new HashMap();
        for(int i = 1; i<=50; i++){
            String caseData = "CASE" + i;
            blankMap.put(caseData, "");
        }
        List<Map<String, String>> dList = dataMapper.queryDataByLabel(label, dataId);
        if(dList.size() == 0){
            try {
                dataMapper.clearData(dataId, blankMap);
                dataMapper.updateData(dataId, label, userNameEn, time, map);
            } catch (Exception e) {
                logger.error("fail to update data" +e);
                throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
            }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"测试数据标签已存在"} );
        }
    }
}
