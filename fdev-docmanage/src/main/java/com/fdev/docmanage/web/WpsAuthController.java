package com.fdev.docmanage.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fdev.docmanage.dict.Dict;
import com.fdev.docmanage.dict.ErrorConstants;
import com.fdev.docmanage.entity.Constants;
import com.fdev.docmanage.entity.HeaderParameter;
import com.fdev.docmanage.entity.PathParameter;
import com.fdev.docmanage.entity.PreviewParameter;
import com.fdev.docmanage.entity.RequestParameterBody;
import com.fdev.docmanage.entity.Token;
import com.fdev.docmanage.entity.TokenParameter;
import com.fdev.docmanage.service.AuthService;
import com.fdev.docmanage.service.UserInfoService;
import com.fdev.docmanage.util.CommonUtils;
import com.fdev.docmanage.util.CookieUtils;
import com.fdev.docmanage.util.EncryptionTool;
import com.fdev.docmanage.util.HttpClientUtil;
import com.fdev.docmanage.util.MyUtil;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;




/**
 * Fdev平台对接Wps平台接口
 * */
@Controller
@RequestMapping(value = "/wps")
public class WpsAuthController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Token token = null;
	
	@Resource
    private AuthService authService;
	
	@Resource 
	private Constants constants;
	
    @Resource
    private HttpClientUtil httpClientUtil;
    
    @Resource
    private UserInfoService userInfoService;
    
    /**
	  初始化完成执行方法
	 * @throws Exception 
	 */
	@PostConstruct
	public void init() {
		cleanToken();
	}
	
	/**
	 * 获得token
	 * @throws Exception
	 * */
	@Scheduled(fixedRate = 3000000)
	public void cleanToken() {
		token = null;
	}
    
   
	/*
	 * 1. 接受前端请求
	 * 2. 模拟请求https://graph.wps.cn/oauth2/auth    302
	 * 3. 返回视图（登录——》输入用户名密码——》调用回调地址） /wps/index
	 * 4. 从回调地址中解析出code state
	 * 5. 根据code获取token
	 * 6. 后续步骤保持一致
	 */
	
	  @RequestMapping(value = "/login")
	  public void login(HttpServletResponse response, HttpServletRequest request) throws Exception{
	  	String url = "http://graph.spdb.com/oauth2/auth?response_type=code&client_id="+constants.getAppId()+"&scope="+constants.getScope()+"&state=L1_JOf2e8QSj3vExl5p4j2sSWJurRFEkIpOQANmpS&redirect_uri="+constants.getRedirectUri()+"/fdocmanage/wps/index";
        response.setHeader("Authorization", constants.getAuthorization());
        response.sendRedirect(url);
	  }
	  
	
	  @RequestMapping(value = "/index")
	  public void index(Model model,
	                        HttpServletResponse response,
	                        HttpServletRequest request,
	                        @RequestParam(value = "code" , required = false)String code,
	                        @RequestParam(value = "state" , required = false)String state){
	        HttpSession session = request.getSession();
	        if(!StringUtils.isEmpty(code)){
	        	CookieUtils.writeCookie(response,"code",code);
	            model.addAttribute("showCode",code);
	            session.setAttribute("code",code);
	        }
	        if(!StringUtils.isEmpty(state)){
	        	CookieUtils.writeCookie(response,"state",state);
	            model.addAttribute("showState",state);
	            session.setAttribute("state",state);
	        }
	        try {
				response.sendRedirect(constants.getRedirectUri()+"/fdocmanage/wps/getToken");
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	  
	  @RequestMapping(value = "/getToken")
	  @ResponseBody
	  public JsonResult getToken(HttpServletResponse response,HttpServletRequest request) {
		  Map<String,String> map = new HashMap<String,String>();
			try {
				TokenParameter tokenParameter = new TokenParameter();
		        tokenParameter.setGrantType("authorization_code");
		        tokenParameter.setScope(constants.getScope());
		        tokenParameter.setCode(CookieUtils.getCookie(request, "code"));
		        tokenParameter.setRedirectUri(constants.getRedirectUri()+"/fdocmanage/wps/index");
		        
		        HeaderParameter headerParameter = new HeaderParameter();
		        headerParameter.setContentType(Constants.CONTENT_TYPE_FORM);
		        headerParameter.setAuthorization(constants.getAuthorization());
		        
		        String json = authService.getToken(tokenParameter,headerParameter);
		        logger.info("method: getToken, data: {}, tokenParameter:{}, headerParameter:{}",json,tokenParameter.toString(),headerParameter.toString());
		        // 如果成功获取了token
		        if(!StringUtils.isEmpty(json)) {
		            token = JSON.parseObject(json, Token.class);
		            logger.info("method: getToken, token: {}",token);
		            CookieUtils.writeCookie(response,"access_token",token.getAccessToken());
		            map.put("access_token", token.getAccessToken()); 
		        }
			}catch(Exception e) {
				logger.error("getToken: mssage: {}",e.getMessage());
			}	
			return JsonResultUtil.buildSuccess(map);
		}
	  
	  
	  /**
		 * 上传文件 会话
		 * @throws Exception 
		 * */
		@RequestMapping(value = "/volumes/files/upload", method = RequestMethod.POST)
	    @ResponseBody
	    public JsonResult uploadFile(@RequestParam(value = "access_token") String access_token,
	    							@RequestParam(value = "volume") String volume,
	                               @RequestParam(value = "file") String file,
	                               @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) throws FdevException{	
			if(CommonUtils.isNullOrEmpty(file)) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"file（文件夹路径）"});
			}
			if(CommonUtils.isNullOrEmpty(volume)) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"volume（卷id）"});
			}
			Long fileSize = uploadFile.getSize();
			String fileName = uploadFile.getOriginalFilename();
			//判断token值
	    	if(CommonUtils.isNullOrEmpty(access_token) || token == null) {
	    		throw new FdevException("token失效，请重新登录！");
	    	}
	    	if(!token.getAccessToken().equals(access_token)) {	
    			throw new FdevException("token失效，请重新登录！");
    		 }
	    	//第一步 创建上传事务  createUpload
			String uploadStep1;
			try {
				uploadStep1 = createUpload(token,volume,file,fileName,fileSize);
			} catch(Exception e) {
	        	logger.error("method: uploadFile, message:{}, stack",e.getMessage(),e);
	        	throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
	        }
			logger.info("method: uploadFile, uploadStep1: {}, token: {}",uploadStep1,token);
			//第二步 上传文件至文件服务器
			/**--------------------------- 步骤1返回参数解析 用于第二步上传文件传入参数 ------------------------------- **/
	        JSONObject jsonObject = JSON.parseObject(uploadStep1);
			//参数
			String feedback = jsonObject.getString("feedback");
			String uploadRequests = jsonObject.getString("upload_requests");	
		
			List<String> ss = JSON.parseArray(uploadRequests, String.class);
			String temp = ss.get(0);
			
			String requestParam = JSON.parseObject(temp).getString("request");
			JSONObject jsonObject2 = JSON.parseObject(requestParam);
			//参数url
			String url = jsonObject2.getString("url");
			
			String form = jsonObject2.getString("form");
			List<String> s2 = JSON.parseArray(form, String.class);
			//参数key x-amz-credential x-amz-date x-amz-algorithm x-amz-signature policy 
			String key = "",xAmzCredential = "",xAmzDate = "",xAmzAlgorithm = "",xAmzSignature = "",policy = "";
			key = JSON.parseObject(s2.get(0)).getString("value");
			xAmzCredential = JSON.parseObject(s2.get(1)).getString("value");
			xAmzDate = JSON.parseObject(s2.get(2)).getString("value");
			xAmzAlgorithm = JSON.parseObject(s2.get(3)).getString("value");
			xAmzSignature = JSON.parseObject(s2.get(4)).getString("value");
			policy = JSON.parseObject(s2.get(5)).getString("value");
			/***************** 向云平台发送上传文档*******************/
			Map<String,String> mapHeader = new HashMap<String,String>();
			mapHeader.put("Content-Type", Constants.CONTENT_TYPE_FORM1);
			mapHeader.put("Authorization", token.getTokenType()+" "+token.getAccessToken());
			Map<String,Object> mapParam = new HashMap<String,Object>();
			mapParam.put("file", uploadFile);
			mapParam.put("key", key);
			mapParam.put("x-amz-credential",xAmzCredential);
			mapParam.put("x-amz-date", xAmzDate);
			mapParam.put("x-amz-algorithm", xAmzAlgorithm);
			mapParam.put("x-amz-signature", xAmzSignature);
			mapParam.put("policy", policy);
			String uploadStep2;
			try {
				uploadStep2 = httpClientUtil.doPostWithFile(url, mapParam, mapHeader);
			}catch(Exception e) {
	        	logger.error("method: uploadFile, message:{}, stack",e.getMessage(),e);
	        	throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
	        }
	    	logger.info("method: uploadFile, uploadStep2: {}",uploadStep2);
	
			//第三步 提交上传事务    commitUpload
			/*************** ① 得到上传第二步返回的参数值     进而解析出  供上传第三步传递参数 *****************/
			JSONObject jsonObject3 = JSON.parseObject(uploadStep2);
			String sha1 = jsonObject3.getString("sha1");
			String storekey = jsonObject3.getString("storekey");
			
			/********** ② 发送上传  commit事务*******************/
			String uploadStep3;
			try {
				uploadStep3 = commitUpload(token,volume,file,feedback,storekey,sha1,uploadFile);
			} catch(Exception e) {
	        	logger.error("method: uploadFile, message:{}, stack",e.getMessage(),e);
	        	throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
	        }
	    	logger.info("method: uploadFile, uploadStep3: {}",uploadStep3);
	    	JSONObject result = MyUtil.dataProcessing(uploadStep3);
			return JsonResultUtil.buildSuccess(result);  
	}
		
		/**
		 * 上传-1 获取上传事务
		 * @throws Exception 
		  */
	    public String createUpload(Token token, String volume, String file,String fileName,Long fileSize) throws Exception{
		    HeaderParameter head = new HeaderParameter();
		    head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
		    head.setContentType(Constants.CONTENT_TYPE_JSON);
		
		    PathParameter pathParameter = new PathParameter();
		    pathParameter.setFile(file);
		    pathParameter.setVolume(volume);
		
		    RequestParameterBody requestBody = new RequestParameterBody();
		    requestBody.setFileSize(fileSize);
		    requestBody.setFileName(fileName);
		    requestBody.setFileNameConflictBehavior("overwrite");
		    requestBody.setUploadMethod("POST");
		    String createUpload = authService.createUpload(head,pathParameter,requestBody);
		    logger.info(createUpload);
		    return createUpload;
	    }
		
		/**
		 * 上传-3 提交上传事务
		 * @throws Exception 
		  */
	    public String commitUpload(Token token,String volume,String file,String feedback,String storekey,String sha1,
	                               MultipartFile uploadFile) throws Exception{
	    	HeaderParameter head = new HeaderParameter();
	        head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
	        head.setContentType(Constants.CONTENT_TYPE_JSON);

	        PathParameter pathParameter = new PathParameter();
	        pathParameter.setFile(file);
	        pathParameter.setVolume(volume);
	        pathParameter.setTransaction(storekey);

	        RequestParameterBody requestBody = new RequestParameterBody();
	        requestBody.setFeedback(feedback);
	        requestBody.setStatus("commited");

	        Map<String,String> map = new HashMap<>();
	        if (StringUtils.isEmpty(sha1)){
	            if(uploadFile!=null){
	                File dest = File.createTempFile("temp",null);
	                uploadFile.transferTo(dest);
	                map.put("sha1", EncryptionTool.getFileSha1(dest));
	            }
	        }else{
	            map.put("sha1",sha1);
	        }
	        requestBody.setFileHashes(map);
	        String commitUpload = authService.commitUpload(head,pathParameter,requestBody);
	        logger.info(commitUpload);
	        return commitUpload;
	    }

		
		/**
		 * 获取文件列表
		 * @throws Exception 
		  */
	    @RequestMapping(value = "/volumes/files/list",method = RequestMethod.POST)
	    @ResponseBody
	    public JsonResult retrieveDir(@RequestBody Map<String, String> requestParam) throws Exception{
	    	if(CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"file（文件夹id）"});
			}
			if(CommonUtils.isNullOrEmpty(requestParam.get("volume"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"volume（卷id）"});
			}
	    	//判断token值
	    	String access_token = requestParam.get(Dict.ACCESSTOKEN);
	    	if(CommonUtils.isNullOrEmpty(access_token) || token == null) {
	    		throw new FdevException("token失效，请重新登录！");
	    	}
	    	if(!token.getAccessToken().equals(access_token)) {	
    			throw new FdevException("token失效，请重新登录！");
    		 }
	    	HeaderParameter head = new HeaderParameter();
	        head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
	        head.setContentType(Constants.CONTENT_TYPE_JSON);
	
	        PathParameter pathParameter = new PathParameter();
	        pathParameter.setFile(requestParam.get("file"));
	        pathParameter.setVolume(requestParam.get("volume"));
	        //下一页游标
	        String next = null;
	        String retrieveDir;
	        Map<String,Object> retrieveDirMap = new HashMap<String,Object>();
	        List<Map<String,Object>> lists = new ArrayList<Map<String,Object>>();
	        try {
	        	do {
	        		retrieveDir = authService.retrieveDir(head,pathParameter,next);
	        		JSONObject jsonObject = JSON.parseObject(retrieveDir);
	        		retrieveDirMap=(Map<String, Object>) jsonObject;
	        		lists.addAll((List<Map<String, Object>>) retrieveDirMap.get("value"));
	        		next = jsonObject.getString("next");
	        	}while(!CommonUtils.isNullOrEmpty(next));
			} catch(Exception e) {
	        	logger.error("method: uploadFile, message:{}, stack",e.getMessage(),e);
	        	throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
	        }
	        Map<String,Object> fileList = new HashMap<String,Object>();
	        if(!"root".equals(requestParam.get("file"))) {
	        	//参数解析
	            JSONObject jsonObject = JSON.parseObject(retrieveDir);
	            String value = jsonObject.getString("value");
	            List<String> list = JSON.parseArray(value, String.class);
	            
	            List<Map<String,Object>> list1 = null;
	            Map<String,Object> map = new HashMap<String,Object>();
	            //判断该文件夹下是否有文件
	            if(list!=null && list.size()>0) {
	            	String fileVersions = null;
	            	list1=new ArrayList<Map<String,Object>>();
	            	for(String file : list) {
	            		if(!"folder".equals(JSON.parseObject(file).getString("type"))) {   			
	            			JSONObject jsonObject1 = JSON.parseObject(file);
	                		map=(Map<String, Object>) jsonObject1;
	                		String fileId = jsonObject1.getString("id");
	                		fileVersions=queryVersions(token,fileId,requestParam.get("volume"));
	                		JSONObject jsonObject2 = JSON.parseObject(fileVersions);
	                		//文件版本列表
	                		String value1 = jsonObject2.getString("value");
	                		List<String> ss = JSON.parseArray(value1, String.class);
	                		String temp = ss.get(0);
	                		String number = JSON.parseObject(temp).getString("number");
	                		map.put("number", number);
	                		list1.add(map);
	            		}
	            		
	            	}
	            	 fileList.put("value", list1);
	                 JSONObject json = new JSONObject(fileList);
	                 retrieveDir = JSON.toJSONString(json)+"\n";
	                 JSONObject result = MyUtil.dataProcessing(retrieveDir);
	                 return JsonResultUtil.buildSuccess(result);
	            }
	        }
	        fileList.put("value", lists);
            JSONObject json = new JSONObject(fileList);
            retrieveDir = JSON.toJSONString(json)+"\n";
	        JSONObject result = MyUtil.dataProcessing(retrieveDir);
	        logger.info("method: retrieveDir, data: {}",retrieveDir);
	        return JsonResultUtil.buildSuccess(result);
    }
	    
		
		/**
		 * 下载文件
		 * @throws Exception 
		  */
	    @RequestMapping(value = "/volumes/files/content",method = RequestMethod.POST)
	    @ResponseBody
	    public JsonResult downloadFile(@RequestBody Map<String, String> requestParam) throws Exception{
	    	if(CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"file（文件id）"});
			}
			if(CommonUtils.isNullOrEmpty(requestParam.get("volume"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"volume（卷id）"});
			}
	    	//判断token值
	    	String access_token = requestParam.get(Dict.ACCESSTOKEN);
	    	if(CommonUtils.isNullOrEmpty(access_token) || token == null) {
	    		throw new FdevException("token失效，请重新登录！");
	    	}
	    	if(!token.getAccessToken().equals(access_token)) {	
    			throw new FdevException("token失效，请重新登录！");
    		 }
	    	HeaderParameter head = new HeaderParameter();
	        head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
	
	        PathParameter pathParameter = new PathParameter();
	        pathParameter.setFile(requestParam.get("file"));
	        pathParameter.setVolume(requestParam.get("volume"));
	        String volumes;
	        try {
	        	volumes = (authService.download(head,pathParameter)).replace("yundoc-dev.spdb.com", "xxx:8080");
			} catch(Exception e) {
	        	logger.error("method: uploadFile, message:{}, stack",e.getMessage(),e);
	        	throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
	        }
	        JSONObject result = MyUtil.dataProcessing(volumes);
	        logger.info("method: volumes, data: {}",volumes);
	        return JsonResultUtil.buildSuccess(result);
    }
	    
	    /**
		 * 文件预览(可编辑)
	     * @throws Exception 
		  */
	    @RequestMapping(value = "/volumes/files/preview",method = RequestMethod.POST)
	    @ResponseBody
	    public JsonResult preview(@RequestBody Map<String, String> requestParam) throws Exception{
	    	if(CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"file（文件id）"});
			}
			if(CommonUtils.isNullOrEmpty(requestParam.get("volume"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"volume（卷id）"});
			}
	    	//判断token值
	    	String access_token = requestParam.get(Dict.ACCESSTOKEN);
	    	if(CommonUtils.isNullOrEmpty(access_token) || token == null) {
	    		throw new FdevException("token失效，请重新登录！");
	    	}
	    	if(!token.getAccessToken().equals(access_token)) {	
    			throw new FdevException("token失效，请重新登录！");
    		 }
	    	HeaderParameter head = new HeaderParameter();
	        head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
	
	        PathParameter pathParameter = new PathParameter();
	        pathParameter.setFile(requestParam.get("file"));
	        pathParameter.setVolume(requestParam.get("volume"));
	
	        PreviewParameter previewParameter = new PreviewParameter();
	        previewParameter.setCopyable(false);
	        previewParameter.setPrintable(false);
	        previewParameter.setExtuserid(requestParam.get("ext_userid"));
	        String preview;
	        try {
	        	preview = (authService.getPreview(head,pathParameter,previewParameter)).replace("weboffice.spdb.com", "xxx:8207");
	        }catch(Exception e) {
	        	logger.error("method: preview, message: {}, stack:{}",e.getMessage(),e);
	        	throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
	        }
	        JSONObject result = MyUtil.dataProcessing(preview);
	        return JsonResultUtil.buildSuccess(result);
	    }
	    
	    /**
	            * 创建文件夹
	     * @throws Exception 
	      */
	    @RequestMapping(value = "/volumes/files/children",method = RequestMethod.POST)
	    @ResponseBody
	    public JsonResult createDir(@RequestBody Map<String, String> requestParam) throws Exception{	
	    	if(CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"file（文件夹路径）"});
			}
			if(CommonUtils.isNullOrEmpty(requestParam.get("volume"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"volume（卷id）"});
			}
			if(CommonUtils.isNullOrEmpty(requestParam.get("file_name"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"file_name（文件夹名）"});
			}
	    	//----------查询卷id-----------------//
	    	JsonResult volumes = volumes(requestParam);
	    	JSONObject jsonObject = JSON.parseObject(volumes.getData().toString());
	    	String feedback = jsonObject.getString("value");
	    	List<String> volumesList = JSON.parseArray(feedback, String.class);
	    	String volumeId = null;
	    	for(String volume : volumesList) {
	    		volumeId = JSON.parseObject(volume).getString("id");
	    		if(requestParam.get("volume").equals(volumeId)) {
	    			break;
	    		}
	    	}
	    	if(CommonUtils.isNullOrEmpty(volumeId)) {
	    		throw new FdevException("该卷id不存在！");
	    	}
			//----------查询卷id-----------------//
			
	    	//查询文件列表
			Map<String,String> requestParam1 = new HashMap<>();
	    	requestParam1.put("file", "root");
	    	requestParam1.put("volume", volumeId);
	    	JsonResult retrieveDir1 = retrieveDir(requestParam);
	    	JSONObject jsonFile = JSON.parseObject(retrieveDir1.getData().toString());
	    	String valueFile = jsonFile.getString("value");
	    	List<String> fileList = JSON.parseArray(valueFile, String.class);
	    	Map<String,String> fileMap = new HashMap<>();
	    	for(int i = 0;i < fileList.size();i++) {
	    		String fileTemp = fileList.get(i);
	    		if("folder".equals(JSON.parseObject(fileTemp).getString("type"))) {   			
	    			fileMap.put(JSON.parseObject(fileTemp).getString("name"), JSON.parseObject(fileTemp).getString("id"));
	    		}
	    	}
			//查询文件夹是否存在, 如果存在，则返回文件夹id
	    	String fileName = requestParam.get("file_name");
	    	if(fileMap.containsKey(fileName)) {
	    		String fileId = fileMap.get(fileName);
	    		String createExit = "{\"id\":\""+fileId+"\"}";
	    		JSONObject result = MyUtil.dataProcessing(createExit);
	    		return JsonResultUtil.buildSuccess(result);
	    	}
	    	//判断token值
	    	String access_token = requestParam.get(Dict.ACCESSTOKEN);
	    	if(CommonUtils.isNullOrEmpty(access_token) || token == null) {
	    		throw new FdevException("token失效，请重新登录！");
	    	}
	    	if(!token.getAccessToken().equals(access_token)) {	
    			throw new FdevException("token失效，请重新登录！");
    		 }
	       
        	HeaderParameter head = new HeaderParameter();
            head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
            head.setContentType(Constants.CONTENT_TYPE_JSON);

            PathParameter pathParameter = new PathParameter();
            pathParameter.setFile(requestParam.get("file"));
            pathParameter.setVolume(volumeId);

            RequestParameterBody requestBody = new RequestParameterBody();
            requestBody.setFileName(requestParam.get("file_name")); 
            requestBody.setFileType("folder");
            requestBody.setFileNameConflictBehavior("fail");
            requestBody.setParents(true);
            String createFolder = authService.createDir(head,pathParameter,requestBody);
            JSONObject result = MyUtil.dataProcessing(createFolder);
            logger.info("method: createDir, data: {}",createFolder);
            return JsonResultUtil.buildSuccess(result);
	        
	    }
	    
	    /**
	             * 获取卷列表
	     * @throws Exception 
	      */
	    @RequestMapping(value = "/volumes",method = RequestMethod.POST)
	    @ResponseBody
	    public JsonResult volumes(@RequestBody Map<String, String> requestParam) throws Exception{
	    	//判断token值
	    	String access_token = requestParam.get(Dict.ACCESSTOKEN);
	    	if(CommonUtils.isNullOrEmpty(access_token) || token == null) {
	    		throw new FdevException("token失效，请重新登录！");
	    	}
	    	if(!token.getAccessToken().equals(access_token)) {	
    			throw new FdevException("token失效，请重新登录！");
    		 }
        	HeaderParameter head = new HeaderParameter();
            head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
            String volumes;
            try {
            	volumes = authService.appVolumes(head);
            }catch(Exception e) {
           	 logger.info("method: volumes, message: {}, stack:{}",e.getMessage(),e);
           	 throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
            }
            logger.info("method: volumes, data: {}",volumes);
            JSONObject result = MyUtil.dataProcessing(volumes);
            return JsonResultUtil.buildSuccess(result);
	       
	    }
	   
	    
	    /**
	     * 获取文件版本列表
	     */
	    public String queryVersions(Token token,String fileId,String volumeId) throws Exception{
	    	HeaderParameter head = new HeaderParameter();
		    head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());
		    head.setContentType(Constants.CONTENT_TYPE_JSON);
		
		    PathParameter pathParameter = new PathParameter();
		    pathParameter.setFile(fileId);
		    pathParameter.setVolume(volumeId);
		
		    String getVersions = authService.queryVersions(head,pathParameter);
		    logger.info(getVersions);
		    return getVersions;
	    }
    
	    /**
	     * 删除文件
	     */
	    @RequestMapping("/deleteFile")
	    @ResponseBody
	    public JsonResult deleteFile(@RequestBody Map<String, String> requestParam) throws Exception{
	    	if(CommonUtils.isNullOrEmpty(requestParam.get("volume"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"volume（卷id）"});
			}
			if(CommonUtils.isNullOrEmpty(requestParam.get("file"))) {
				throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,new String[] {"file（文件id）"});
			}
	    	//判断token值
	    	String access_token = requestParam.get(Dict.ACCESSTOKEN);
	    	if(CommonUtils.isNullOrEmpty(access_token) || token == null) {
	    		throw new FdevException("token失效，请重新登录！");
	    	}
	    	if(!token.getAccessToken().equals(access_token)) {	
    			throw new FdevException("token失效，请重新登录！");
    		 }
        	HeaderParameter head = new HeaderParameter();
            head.setAuthorization(token.getTokenType()+" "+token.getAccessToken());

            PathParameter pathParameter = new PathParameter();
            pathParameter.setFile(requestParam.get("file"));
            pathParameter.setVolume(requestParam.get("volume"));
            String value;
            try {
            	value = authService.deleteFile(head,pathParameter);
            }catch(Exception e) {
           	 logger.info("method: volumes, message: {}, stack:{}",e.getMessage(),e);
           	 throw new FdevException(ErrorConstants.WPS_ERROR, new String[]{e.getMessage()});
            }
            if(CommonUtils.isNullOrEmpty(value)) {
            	return JsonResultUtil.buildSuccess("true");
            }else {
            	JSONObject result = MyUtil.dataProcessing(value);
            	return JsonResultUtil.buildSuccess(result);
            }
         
	    }
    

}
