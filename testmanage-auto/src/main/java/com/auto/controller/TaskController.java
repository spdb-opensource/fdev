package com.auto.controller;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.testng.TestNG;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auto.common.HttpUtils;
import com.auto.service.ITaskService;
import com.auto.util.MailUtil;
import com.auto.util.MyUtil;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;

@RestController
@RequestMapping("/autotest")
public class TaskController {
	@Autowired 
	ITaskService iTaskService;
	@Autowired 
	RedissonClient redissonClient;
	@Autowired 
	MailUtil mailUtil;
    @Autowired
    private MyUtil myUtil;
	@Value("${fjob.email.filePath}")
	private String filePath;
    @Value("${email.get.attachment.filePath}")
    private String emailGetAttachmentFilePath;
    @Value("${fjob.zip.filePath}")
    private String sourceRptPath;
    

	private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    //生成自动化代码
    @RequestMapping(value = "/genFile",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult genFile(@RequestBody Map<String, JSONArray> map) throws Exception{
		RLock rLock = redissonClient.getLock("autotest");
    	JSONObject data = new JSONObject();
    	try{
        	if(rLock.tryLock(0, -1L, TimeUnit.SECONDS)) {
    			//时间戳文件夹
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    			String folder = "t" + sdf.format(new Date());	
    			iTaskService.genFile(map, folder);
    			data.put("folder", folder);
        	}
    	}catch(Exception e){
    		throw new Exception(e);
    	}finally{
        	rLock.unlock();
    	}
    	return JsonResultUtil.buildSuccess(data);
    }
    
    //执行案例
    @RequestMapping(value = "/excuteCase",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult excuteCase(@RequestBody Map<String, String> map) throws Exception{

    	RLock rLock = redissonClient.getLock("autotest");
    	String path  = System.getProperty("user.dir");
//    	String path = Thread.currentThread().getContextClassLoader().toString();
    	if(rLock.tryLock(60000, -1L, TimeUnit.SECONDS)) {
	    	String folder = map.get("folder");
	    	String[] menuNumber = map.get("menuNo").split(",");
	    	List<String> suitList = new ArrayList<String>();
	    	for(int i = 0; i <menuNumber.length; i++ ){
	        	//String path = ResourceUtils.getURL("\\src\\main\\resources\\testng-suit-task.xml").getPath();
	        	suitList.add(path + "\\src\\main\\resources\\" + folder + "\\testng-suite-" + menuNumber[i] + ".xml");
	    	}
	    	try{
	    		logger.info("new========");
				TestNG testNg = new TestNG();
	    		logger.info("setTestSuites======");
		    	testNg.setTestSuites(suitList);
	    		logger.info("run==========");
		    	testNg.run();
	    		logger.info("runafter==========");
//		    	File file = new File(path+filePath);
//		    	if(!file.exists()) {
//		    		file.mkdirs();
//		    	}


		        System.out.println("zippath===========" + emailGetAttachmentFilePath + "report.zip");

		        System.out.println("zippath11111===========" + filePath + "report.zip");
		    	HttpUtils.toZip(path + sourceRptPath, filePath + "report.zip", true);
//		    	HttpUtils.toZip(path + sourceRptPath, "d:\\report.zip", false);
		    	String subject =  map.get("menuNo") + "_" + folder + "自动化测试报告";
		    	String content = "自动化测试报告见附件";
		    	List<String> to = new ArrayList<String>();
		    	to.add(myUtil.getCurrentUserEmial());
		    	List<String> filePaths = new ArrayList<String>();
		    	filePaths.add(emailGetAttachmentFilePath + "report.zip");
		    	mailUtil.sendEmail(subject, content, to, filePaths);
	    	}catch(Exception e){
	    		throw new Exception(e);
	    	}finally{
	        	rLock.unlock();
	    	}
    	}
    	return JsonResultUtil.buildSuccess();
    }
}
