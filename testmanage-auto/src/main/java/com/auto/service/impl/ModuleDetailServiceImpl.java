package com.auto.service.impl;

import com.auto.dao.ModuleDetailMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.ModuleDetail;
import com.auto.service.IModuleDetailService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ModuleDetailServiceImpl implements IModuleDetailService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private ModuleDetailMapper moduleDetailMapper;

    @Override
    public void addModuleDetail(ModuleDetail moduleDetail) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        moduleDetail.setLastOpr(userNameEn);
        moduleDetail.setCreateTime(time);
        moduleDetail.setModifyTime(time);
        moduleDetail.setDeleted("0");
        List<Map<String, String>> mdList = moduleDetailMapper.queryModuleDetailByIdAndStep(moduleDetail.getModuleId(), moduleDetail.getElementStepNo(), null);
        if(mdList.size() == 0){
	        try {
	            moduleDetailMapper.addModuleDetail(moduleDetail);
	        } catch (Exception e) {
	            logger.error("fail to add moduleDetail");
	            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
	        }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"当前组件STEP已存在"} );
        }
    }

    @Override
    public List<Map<String, String>> queryModuleDetail(Map<String, String> map) throws Exception {
        List<Map<String, String>> modules = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            modules = moduleDetailMapper.queryModuleDetail(search, valid);
        } catch (Exception e) {
            logger.error("fail to query moduleDetail");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return modules;
    }
    

    @Override
    public List<Map<String, String>> queryModuleDetailByModuleId(String moduleId) throws Exception {
        List<Map<String, String>> modules = new ArrayList<>();
        try {
            modules = moduleDetailMapper.queryModuleDetailByModuleId(moduleId);
        } catch (Exception e) {
            logger.error("fail to query moduleDetail");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return modules;
    }

    @Override
    public void deleteModuleDetail(Map<String, String[]> map) throws Exception {
        List<String> modules = Arrays.asList(map.get(Dict.MODULEDETAILID));
        String module = "'" + String.join("','", modules) + "'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
            moduleDetailMapper.deleteModuleDetail(module, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete moduleDetail");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateModuleDetail(Map<String, String> map) throws Exception {
        String moduleDetailId = String.valueOf(map.get(Dict.MODULEDETAILID));
        String moduleId = String.valueOf(map.getOrDefault(Dict.MODULEID, ""));
        String elementStepNo = String.valueOf(map.getOrDefault(Dict.ELEMENTSTEPNO, ""));
        String elementId = String.valueOf(map.getOrDefault(Dict.ELEMENTID, ""));
        String elementType = String.valueOf(map.getOrDefault(Dict.ELEMENTTYPE, ""));
        String elementData = String.valueOf(map.getOrDefault(Dict.ELEMENTDATA, ""));
        String exeTimes = String.valueOf(map.getOrDefault(Dict.EXETIMES, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<Map<String, String>> mdList = moduleDetailMapper.queryModuleDetailByIdAndStep(moduleId, elementStepNo, moduleDetailId);
        if(mdList.size() == 0){
	        try {
	            moduleDetailMapper.updateModuleDetail(moduleDetailId, moduleId, elementStepNo, elementId,
	                    elementType, elementData, exeTimes, userNameEn, time);
	        } catch (Exception e) {
	            logger.error("fail to update moduleDetail");
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"当前组件STEP已存在"} );
        }
    }
}
