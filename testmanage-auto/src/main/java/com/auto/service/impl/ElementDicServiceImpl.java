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

import com.auto.dao.ElementDicMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.ElementDic;
import com.auto.entity.User;
import com.auto.service.IElementDicService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

@Service
public class ElementDicServiceImpl implements IElementDicService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private ElementDicMapper elementDicMapper;

    @Override
    public void addElementDic(Map<String, String> map) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        String elementDicMethod = map.getOrDefault(Dict.ELEMENTDICMETHOD, "");
        String elementDicParam = map.getOrDefault(Dict.ELEMENTDICPARAM, "");
        String methodDesc = map.getOrDefault(Dict.METHODDESC, "");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        ElementDic elementDic = new ElementDic(elementDicMethod, elementDicParam, methodDesc , "0", time, time,  userNameEn);
        List<ElementDic> eList = elementDicMapper.queryElementDicByMethod(elementDicMethod, null);
    	if(eList.size() == 0){
	        try {
	        	elementDicMapper.addElementDic(elementDic);
	        } catch (Exception e) {
	            logger.error("fail to create elementdic");
	            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
	        }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"元素方法已存在"} );
    	}
    }

    @Override
    public List<ElementDic> queryElementDic(Map<String, String> map) throws Exception {
        List<ElementDic> result = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            result = elementDicMapper.queryElementDic(search, valid);
        } catch (Exception e) {
            logger.error("fail to query elementdic");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return result;
    }

    @Override
    public void deleteElementDic(Map map) throws Exception {
        List<String> elementDicIds = (List<String>)map.get(Dict.ELEMENTDICID);
        String elementDicId = "'" + String.join("','", elementDicIds)+"'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
        	elementDicMapper.deleteElementDic(elementDicId, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete elementdic");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateElementDic(Map map) throws Exception {
        String elementDicId = String.valueOf(map.get(Dict.ELEMENTDICID));
        String elementDicMethod = String.valueOf(map.getOrDefault(Dict.ELEMENTDICMETHOD, ""));
        String elementDicParam = String.valueOf(map.getOrDefault(Dict.ELEMENTDICPARAM, ""));
        String methodDesc = String.valueOf(map.getOrDefault(Dict.METHODDESC, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<ElementDic> eList = elementDicMapper.queryElementDicByMethod(elementDicMethod, elementDicId);
    	if(eList.size() == 0){
	        try {
	        	elementDicMapper.updateElementDic(elementDicId, elementDicMethod, elementDicParam, 
	        			methodDesc, userNameEn, time);
	        } catch (Exception e) {
	            logger.error("fail to update elementdic");
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"元素方法已存在"} );
    	}
    }
}
