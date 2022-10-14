package com.spdb.fdev.fdemand.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.DemandDocEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.IpmpUtils;
import com.spdb.fdev.fdemand.spdb.entity.TestOrderFile;
import com.spdb.fdev.fdemand.spdb.service.FdocmanageService;
import com.spdb.fdev.fdemand.spdb.service.IXTestUtilsService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class XTestUtilsServiceImpl implements IXTestUtilsService {


    @Value("${xtest.api.url}")
    private String xTestUrl;
    @Value("${xtest.submitTestOrder}")
    private String submitTestOrder;
    @Value("${xtest.importTestOrderFile}")
    private String importTestOrderFile;
    @Value("${xtest.delTestOrderFile}")
    private String delTestOrderFile;
    @Value("${xtest.updateTestOrder}")
    private String updateTestOrder;
    @Value("${xtest.getTestManagerInfo}")
    private String getTestManagerInfo;
    @Value("${xtest.modifyTestOrderStatus}")
    private String modifyTestOrderStatus;
    @Value("${xtest.getTechReqTestInfo}")
    private String getTechReqTestInfo;

    @Autowired
    private IpmpUtils ipmpUtils;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FdocmanageService fdocmanageService;

    @Override
    public void submitTestOrder(Map<String, Object> params) throws Exception {

        Object response = ipmpUtils.send(params,xTestUrl + submitTestOrder );
        Map map = JSONObject.parseObject((String) response, Map.class);
        boolean result = (boolean) map.get(Dict.RESULT);
        //true 请求成功 false 请求失败
        if( !result ){
            //失败抛出错误原因
            throw new FdevException((String) map.get(Dict.MSG));
        }
    }

    @Override
    public void importTestOrderFile(List<TestOrderFile> files) throws Exception {
        List<Map<String, Object>> trans = files.stream().map(file -> {
            return new HashMap<String, Object>() {{
                put("id", file.getId());
                put("file_name", file.getFile_name());
                put("file_type", file.getFile_type());
                // put("file_path", file.getFile_path());
                put("test_order_id", file.getTest_order_id());
                put("upload_time", file.getUpload_time());
                put("upload_user_info", file.getUpload_user_info());
            }};
        }).collect(Collectors.toList());
        String filesJson = JSONObject.toJSONString(trans);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(
                new LinkedMultiValueMap<String, Object>() {{
                    add("testOrderFileStr", filesJson);
                    for (TestOrderFile file : files) {
                        //为避免测试报告重复上传，取相同路径
                        String moduleName = "fdev-demand-testOrder";
                        InputStreamResource fileSystemResource = fdocmanageService.downloadFileInputStream(moduleName, file.getFile_path());
                        add("attchment", fileSystemResource);
                    }
                }}, new HttpHeaders() {{
            setContentType(MediaType.MULTIPART_FORM_DATA);
        }});
        Map result = restTemplate.postForEntity(xTestUrl + importTestOrderFile, request, Map.class).getBody();
        if (!(boolean) result.get(Dict.RESULT))
            throw new FdevException(result.get(Dict.MSG) + "-" + Dict.TEST_ORDER_FILE + ":" + filesJson);//失败抛出错误原因
    }

    @Override
    public void delTestOrderFile(List<String> testOrderFileIds) throws Exception {
        Map<String, Object> result = JSONObject.parseObject((String) ipmpUtils.send(new HashMap<String, Object>() {{
            put("files", testOrderFileIds);
        }}, xTestUrl + delTestOrderFile), Map.class);
        if (!(boolean) result.get(Dict.RESULT)) throw new FdevException((String) result.get(Dict.MSG));
    }

    @Override
    public void updateTestOrder(Map<String, Object> params) throws Exception {

        Object response = ipmpUtils.send(params,xTestUrl + updateTestOrder );
        Map map = JSONObject.parseObject((String) response, Map.class);
        boolean result = (boolean) map.get(Dict.RESULT);
        //true 请求成功 false 请求失败
        if( !result ){
            //失败抛出错误原因
            throw new FdevException((String) map.get(Dict.MSG));
        }
    }

    @Override
    public void modifyTestOrderStatus(Map<String, Object> params) throws Exception {

        Object response = ipmpUtils.send(params,xTestUrl + modifyTestOrderStatus );
        Map map = JSONObject.parseObject((String) response, Map.class);
        boolean result = (boolean) map.get(Dict.RESULT);
        //true 请求成功 false 请求失败
        if( !result ){
            //失败抛出错误原因
            throw new FdevException((String) map.get(Dict.MSG));
        }
    }
    @Override
    public List<Map> getTestManagerInfo(Map<String, Object> params) throws Exception {

        Object response = ipmpUtils.send(params,xTestUrl + getTestManagerInfo );
        Map map = JSONObject.parseObject((String) response, Map.class);
        boolean result = (boolean) map.get(Dict.RESULT);
        //true 请求成功 false 请求失败
        if( !result ){
            //失败抛出错误原因
            throw new FdevException((String) map.get(Dict.MSG));
        }
        List<Map> list = (List<Map>) map.get(Dict.CONTENT);
        return list;
    }

    @Override
    public List<Map> getTechReqTestInfo(List<String> demandIds) throws Exception {

        Object response = ipmpUtils.send(demandIds,xTestUrl + getTechReqTestInfo );
        Map map = JSONObject.parseObject((String) response, Map.class);
        boolean result = (boolean) map.get(Dict.RESULT);
        //true 请求成功 false 请求失败
        if( !result ){
            //失败抛出错误原因
            throw new FdevException((String) map.get(Dict.MSG));
        }
        List<Map> list = (List<Map>) map.get(Dict.CONTENT);
        return list;
    }

}
