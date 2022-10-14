package com.auto.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.auto.common.CommonGeneratorFile;
import com.auto.common.CommonModuleGenerateFile;
import com.auto.dict.ErrorConstants;
import com.auto.service.ITaskService;
import com.test.testmanagecommon.exception.FtmsException;

@Service
public class TaskServiceImpl implements ITaskService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());



	@Override
	public void genFile(Map<String, JSONArray> map, String folder) throws Exception {
        try {
        	List<String> moduleList = new ArrayList<>();
        	JSONArray jsArray = map.get("caseList");
        	for(int i = 0; i < jsArray.size(); i++){
        		String menuNo = jsArray.getJSONObject(i).get("menuNo").toString();
            	String testCaseNo = jsArray.getJSONObject(i).get("testCaseNo").toString();
            	moduleList = CommonGeneratorFile.generatorFile(menuNo, testCaseNo, folder);
            	CommonModuleGenerateFile.generatorModule(moduleList);
        	}

        } catch (Exception e) {
            logger.error("生成自动化代码失败"+e.getMessage());
            throw new FtmsException(ErrorConstants.GENERATE_FILE_ERROR, new String[]{"生成自动化代码失败"});
        }
	}


	@Override
	public void excuteCase(Map<String, String> map) throws Exception {
        try {
            //restTransport.submitSourceBack(restTransport.getUrl("fnotify.host"), param);
        } catch (Exception e) {
            logger.error("执行自动化代码失败"+e.getMessage());
            throw new FtmsException(ErrorConstants.EXCUTE_FILE_ERROR, new String[]{"执行自动化代码失败"});
        }
		
	}



}
