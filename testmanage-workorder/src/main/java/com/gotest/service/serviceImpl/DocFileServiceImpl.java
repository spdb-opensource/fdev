package com.gotest.service.serviceImpl;

import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.IDocService;
import com.gotest.utils.CommonUtils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
public class DocFileServiceImpl implements IDocService {
    private Logger logger = LoggerFactory.getLogger(DocFileServiceImpl.class);
    @Autowired
    private RestTransport restTransport;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Map<String, String> uploadDocFile(MultipartFile file, String fileType, String workNo) throws Exception {
        String path = workNo + "/" + fileType + "/" + file.getOriginalFilename();
        if (!uploadFiletoMinio(Constants.APPLICATION_NAME_ORDER, path, file, Constants.RESTCODE_FILESUPLOAD)) {
            throw new FtmsException(ErrorConstants.UPLOAD_LOAD_ERROR);
        }
        return new HashMap<String, String>(){{
            put(Dict.PATH, restTransport.getUrl(Constants.RESTCODE_FILESDOWNLOAD)+"?moduleName=testmanage-order&path=" + URLEncoder.encode(path, "UTF-8"));
        }};
    }

    /**
     * 调用文档管理模块接口把文件上传到minIO
     * @param moduleName
     * @param path
     * @param multipartFile
     * @param restCode
     * @return
     * @throws Exception
     */
    public boolean uploadFiletoMinio(String moduleName, String path, MultipartFile multipartFile, String restCode) throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        try {
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add(Dict.FILES, multipartFile.getResource());
            param.add(Dict.MODULENAME, moduleName);
            param.add(Dict.PATH, path);
            param.add(Dict.USER, user);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, Constants.CONTENTTYPE_FORMDATA);
            httpHeaders.add(Dict.SOURCE, Constants.BACK);
            HttpEntity request = new HttpEntity<Object>(param, httpHeaders);
            String url = restTransport.getUrl(restCode);
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    path,
                    sw.toString());
            return false;
        }
        return true;
    }
}
