package com.auto.service.impl;

import com.auto.dao.MenuMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.MenuSheet;
import com.auto.service.IMenuService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MenuServiceImpl implements IMenuService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private MenuMapper menuMapper;

    @Override
    public void addMenu(Map<String, String> map) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        String menuName = map.getOrDefault(Dict.MENUNAME, "");
        String menuNo = map.getOrDefault(Dict.MENUNO, "");
        String secondaryMenu = map.getOrDefault(Dict.SECONDARYMENU, "");
        String secondaryMenuNo = map.getOrDefault(Dict.SECONDARYMENUNO, "");
        String thirdMenu = map.getOrDefault(Dict.THIRDMENU, "");
        String thirdMenuNo = map.getOrDefault(Dict.THIRDMENUNO, "");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        MenuSheet menuSheet = new MenuSheet(menuName, menuNo, secondaryMenu, secondaryMenuNo,
                thirdMenu, thirdMenuNo, "0", time, time,  userNameEn);
    	List<MenuSheet> mList= menuMapper.queryMenuByMenuSheet(menuNo, secondaryMenuNo, thirdMenuNo, null);
    	if(mList.size() == 0){
    		try {
    			menuMapper.addMenu(menuSheet);
            } catch (Exception e) {
                logger.error("fail to create menu");
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
            }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"菜单编号已存在"} );
    	}
    }

    @Override
    public List<MenuSheet> queryMenu(Map<String, String> map) throws Exception {
        List<MenuSheet> result = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            result = menuMapper.queryMenu(search, valid);
        } catch (Exception e) {
            logger.error("fail to query menu");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return result;
    }

    @Override
    public void deleteMenu(Map map) throws Exception {
        List<String> menuSheetIds = (List<String>)map.get(Dict.MENUSHEETID);
        String menuSheetId = "'" + String.join("','", menuSheetIds)+"'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
            menuMapper.deleteMenu(menuSheetId, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete menu");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }

    @Override
    public void updateMenu(Map map) throws Exception {
        String menuSheetId = String.valueOf(map.get(Dict.MENUSHEETID));
        String menuName = String.valueOf(map.getOrDefault(Dict.MENUNAME, ""));
        String menuNo = String.valueOf(map.getOrDefault(Dict.MENUNO, ""));
        String secondaryMenu = String.valueOf(map.getOrDefault(Dict.SECONDARYMENU, ""));
        String secondaryMenuNo = String.valueOf(map.getOrDefault(Dict.SECONDARYMENUNO, ""));
        String thirdMenu = String.valueOf(map.getOrDefault(Dict.THIRDMENU, ""));
        String thirdMenuNo = String.valueOf(map.getOrDefault(Dict.THIRDMENUNO, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
    	List<MenuSheet> mList= menuMapper.queryMenuByMenuSheet(menuNo, secondaryMenuNo, thirdMenuNo, menuSheetId);
    	if(mList.size() == 0){
	        try {
	            menuMapper.updateMenu(menuSheetId, menuName, menuNo, secondaryMenu, secondaryMenuNo, thirdMenu, thirdMenuNo, userNameEn, time);
	        } catch (Exception e) {
	            logger.error("fail to update menu");
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
    	}else{
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"菜单编号已存在"} );
    	}
    }
}
