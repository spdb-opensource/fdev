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

import com.auto.dao.ModuleMapper;
import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.auto.entity.Module;
import com.auto.service.IModuleService;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.exception.FtmsException;

@Service
public class ModuleServiceImpl implements IModuleService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private ModuleMapper moduleMapper;

    @Override
    public void addModule(Module module) throws Exception {
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        module.setLastOpr(userNameEn);
        module.setCreateTime(time);
        module.setModifyTime(time);
        module.setDeleted("0");
        List<Map<String, String>> mList= moduleMapper.queryModuleByNo(module.getModuleNo(), module.getModuleGroup(), module.getModuleName(), null);
        if(mList.size() == 0){
            try {
                moduleMapper.addModule(module);
            } catch (Exception e) {
                logger.error("fail to add module");
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
            }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"组件已存在"} );
        }
    }

    @Override
    public List<Map<String, String>> queryModule(Map<String, String> map) throws Exception {
        List<Map<String, String>> modules = new ArrayList<>();
        String search = map.getOrDefault(Dict.SEARCH, "");
        String valid = map.getOrDefault(Dict.VALID, "0");
        try {
            modules = moduleMapper.queryModule(search, valid);
        } catch (Exception e) {
            logger.error("fail to query module");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return modules;
    }

    @Override
    public void deleteModule(Map map) throws Exception {
        List<String> modules = (List<String>) map.get(Dict.MODULEID);
        String module = "'" + String.join("','", modules) + "'";
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        try {
            moduleMapper.deleteModule(module, userNameEn, time);
        } catch (Exception e) {
            logger.error("fail to delete module");
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
    }


    @Override
    public void updateModule(Map map) throws Exception {
        String moduleId = String.valueOf(map.get(Dict.MODULEID));
        String moduleNo = String.valueOf(map.getOrDefault(Dict.MODULENO, ""));
        String moduleGroup = String.valueOf(map.getOrDefault(Dict.MODULEGROUP, ""));
        String moduleName = String.valueOf(map.getOrDefault(Dict.MODULENAME, ""));
        String moduleNameCn = String.valueOf(map.getOrDefault(Dict.MODULENAMECN, ""));
        String userNameEn = myUtil.getCurrentUserEnName();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        List<Map<String, String>> mList= moduleMapper.queryModuleByNo(moduleNo, moduleGroup, moduleName, moduleId);
        if(mList.size() == 0){
	        try {
	            moduleMapper.updateModule(moduleId, moduleNo, moduleGroup, moduleName, moduleNameCn, userNameEn, time);
	        } catch (Exception e) {
	            logger.error("fail to update module");
	            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR);
	        }
        }else{
        	throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR,  new String[]{"组件已存在"} );
        }
    }
}
