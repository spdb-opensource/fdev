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

import com.auto.dao.ElementMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.Element;
import com.auto.entity.ElementDic;
import com.auto.service.IElementService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

@Service
public class ElementServiceImpl implements IElementService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private ElementMapper elementMapper;

    @Override
    public void addElement(Map<String, String> map) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        String elementType = map.getOrDefault(Dict.ELEMENTTYPE, "");
        String elementContent = map.getOrDefault(Dict.ELEMENTCONTENT, "");
        String elementName = map.getOrDefault(Dict.ELEMENTNAME, "");
        String elementDir = map.getOrDefault(Dict.ELEMENTDIR, "");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        Element element = new Element(elementType, elementContent, elementName, elementDir, "0", time, time,  userNameEn);
        List<Element> eList = elementMapper.queryElementByName(elementName, null);
    	if(eList.size() == 0){
	        try {
	        	elementMapper.addElement(element);
	        } catch (Exception e) {
	            logger.error("fail to create element");
	            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
	        }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"元素名称已存在"} );
    	}
    }

    @Override
    public List<Element> queryElement(Map<String, String> map) throws Exception {
        List<Element> result = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            result = elementMapper.queryElement(search, valid);
        } catch (Exception e) {
            logger.error("fail to query element");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return result;
    }

    @Override
    public void deleteElement(Map map) throws Exception {
        List<String> elementIds = (List<String>)map.get(Dict.ELEMENTID);
        String elementId = "'" + String.join("','", elementIds)+"'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
        	elementMapper.deleteElement(elementId, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete element");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateElement(Map map) throws Exception {
        String elementId = String.valueOf(map.get(Dict.ELEMENTID));
        String elementType = String.valueOf(map.getOrDefault(Dict.ELEMENTTYPE, ""));
        String elementContent = String.valueOf(map.getOrDefault(Dict.ELEMENTCONTENT, ""));
        String elementName = String.valueOf(map.getOrDefault(Dict.ELEMENTNAME, ""));
        String elementDir = String.valueOf(map.getOrDefault(Dict.ELEMENTDIR, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<Element> eList = elementMapper.queryElementByName(elementName, elementId);
    	if(eList.size() == 0){
	        try {
	        	elementMapper.updateElement(elementId, elementType, elementContent, 
	        			elementName, elementDir, userNameEn, time);
	        } catch (Exception e) {
	            logger.error("fail to update element");
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"元素名称已存在"} );
    	}
    }
}
