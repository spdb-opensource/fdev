package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.IRedmineApi;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class RedmineApiImpl implements IRedmineApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Override
    public String getRedmineHost() {
        return redmineHost;
    }

    @Value("${redmine.host}")
    private String redmineHost;

    @Value("${redmine.auth}")
    private String auth;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Map queryRedmineInfoByRedmineId(Map param) {
        String id = (String) param.get(Dict.REDMINE_ID);

        StringBuffer url = new StringBuffer();
        url.append(redmineHost);
        url.append("?requriement_no=");
        url.append(id);
        Map json = redMineRest(url.toString());
        if(CommonUtils.isNullOrEmpty(json)){
            return null;
        }
        Map fs = (Map) ((Map) json.get(Dict.ISSUE)).get(Dict.TRACKER);
        if ("5".equals(fs.get(Dict.ID).toString())) {
            return redmineInfoToTask(json);
        }
        return redmineDayInfoToTask(json);
    }

    @Override
    public Map getRedmineInfoForExcel(String id) {
        Object ojb = null;
        try {
            String au=  new String(Base64.encodeBase64(auth.getBytes()));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization","Basic "+au);
            HttpEntity<String> request = new HttpEntity<String>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    redmineHost+"?requriement_no="+id,HttpMethod.GET,request,Map.class);
            int status = responseEntity.getStatusCodeValue();
            if(200==status){
                ojb = responseEntity.getBody();
            }else{
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        if (CommonUtils.isNullOrEmpty(ojb)) {
            return null;
        }
        Map json = (Map) ojb;
//        Map fs = (Map) ((Map) json.get(Dict.ISSUE)).get(Dict.TRACKER);
//        if ("5".equals(fs.get(Dict.ID).toString())) {
//            return redmineInfoToTask(json);
//        }
        return redmineDateForExcel(json);
    }

    //研发单元任务
    public Map redmineInfoToTask(Map json) {
        List fs = (ArrayList) ((Map) json.get(Dict.ISSUE)).get(Dict.CUSTOM_FIELDS);
        // 研发单元编号
        Map result = new HashMap();
        String redmineId = (String) find(fs, "66");
        result.put(Dict.REDMINE_ID, redmineId);
        // 计划启动开发日期,
        String planStartTime = (String) find(fs, "81");
        result.put(Dict.PLAN_START_TIME, CommonUtils.dateParse(planStartTime));
        // 计划提交用户测试日期,
        String planUatTestStartTime = (String) find(fs, "50");
        result.put(Dict.PLAN_UAT_TEST_START_TIME, CommonUtils.dateParse(planUatTestStartTime) );
        // 计划用户测试完成日期,
        String planUatTestStopTime = (String) find(fs, "82");
        result.put(Dict.PLAN_UAT_TEST_STOP_TIME, CommonUtils.dateParse(planUatTestStopTime));
        // 计划投产日期
        String planFireTime = (String) find(fs, "54");
        result.put(Dict.PLAN_FIRE_TIME, CommonUtils.dateParse(planFireTime));

        result.put(Dict.TYPE, "0");// 研发单元任务 0 研发单元任务
        return result;
    }


    //日常任务
    public Map redmineDayInfoToTask(Map json) {
        List fs = (ArrayList) ((Map) json.get(Dict.ISSUE)).get(Dict.CUSTOM_FIELDS);
        // 研发单元编号
        Map result = new HashMap();
        String redmineId = (String) find(fs, "150");
        result.put(Dict.REDMINE_ID, redmineId);
        // 计划启动开发日期,
        String planStartTime = (String) find(fs, "164");
        result.put(Dict.PLAN_START_TIME, CommonUtils.dateParse( planStartTime));

        // 计划结束日期,
        String planStopTime = (String) find(fs, "171");
        result.put(Dict.PLAN_FIRE_TIME, CommonUtils.dateParse( planStopTime));

        result.put(Dict.TYPE, "1");// 日常任务 1日常任务

        return result;
    }

    public Map redmineDateForExcel(Map json){
        List fs = (ArrayList) ((Map) json.get(Dict.ISSUE)).get(Dict.CUSTOM_FIELDS);
        // 研发单元编号
        Map result = new HashMap();
        String redmineId = (String) find(fs, "66");
        result.put(Dict.REDMINE_ID, redmineId);
        // 需求说明书名称
        result.put(Dict.REQUIRE_DOC, (String) find(fs, "121"));
        // 实施牵头人
        result.put(Dict.WORK_LEADER, (String) find(fs, "56"));

//        需求信息单编号:信息单2017科技224  41
//        需求信息单标题:网银证书系统优化 120
//        研发单元内容 43
//        涉及系统 60
//        研发单元跟踪人 126
//        行内工作量 235
//        厂商工作量 236
        result.put(Dict.REQUIRE_INFO_NO   ,(String) find(fs, "41"));
        result.put(Dict.REQUIRE_INFO_TITLE,(String) find(fs, "120"));
        result.put(Dict.REDMINE_CONTENT   ,(String) find(fs, "43"));
        result.put(Dict.REFER_SYSTEM      , StringUtils.join(((ArrayList) find(fs, "60")),"、"));
        result.put(Dict.REDMINE_FOLLOW    ,(String) find(fs, "126"));
        result.put(Dict.SPDB_WORKTIME     ,(String) find(fs, "235"));
        result.put(Dict.COMPANY_WORKTIME  ,(String) find(fs, "236"));
        return result;
    }

    private Object find(List fs, String id) {
        for (int i = 0; i < fs.size(); i++) {
            Map js = (Map) fs.get(i);
            if (id.equals(js.get(Dict.ID).toString())) {
                return js.get(Dict.VALUE);
            }
        }
        return null;
    }


    @Override
    public Boolean updateRedMineDate(String url){
        // 47 实际启动开发日期
        // 51 实际启动用户测试日期
        // 53 实际用户测试完成日期
        // 55 实际投产日期
//        requriement_no   研发单元编号
//        ssdy_ks_date  研发单元实际开始日期
//        ssdy_uat_date  研发单元提交用户测试日期
//        ssdy_uatjs_date 研发单元提交用户测试结束日期
//        ssdy_tc_date  研发单元实际投产日期
//        rc_ks_date  日常任务开始日期
//        rc_js_date   日常任务结束日期
        logger.info("更新redmine实际时间"+url);
        try {
            redMineRest(url);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public Map redMineRest(String url){
        String au=  new String(Base64.encodeBase64(auth.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic "+au);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        Object obj = null;
        ResponseEntity<Map> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.GET,request, Map.class);
        } catch (RestClientException e) {
            logger.error("redmine 服务异常" + e.getMessage());
            return null;
//            throw new FdevException(ErrorConstants.REDMINE_SERVER_ERROR);
        }
        int status = responseEntity.getStatusCodeValue();
        if(status == 403){
            logger.error("redmine 用户验证失败");
            throw new FdevException(ErrorConstants.REDMINE_AUTH_FORBBIDEN);
        }
        if(200==status){
            obj = responseEntity.getBody();
        }else{
            throw new FdevException(ErrorConstants.REDMINE_OTHER_ERROR);
        }
        if (CommonUtils.isNullOrEmpty(obj)) {
            return null;
        }
        return (Map) obj;
    }


   /* public Map redMineGet(String url, String param) throws Exception {
        CloseableHttpClient httpClients = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        JSONObject result = null;
        try {
            HttpGet httpGet = new HttpGet(url + "?requriement_no=" + param);
            httpGet.addHeader("source", "back");
            httpGet.addHeader(HTTP.CONTENT_TYPE, "application/json");
            response = httpClients.execute(httpGet);
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            if (statusCode == 200) {
                HttpEntity httpEntity = response.getEntity();
                String s = EntityUtils.toString(httpEntity, "UTF-8");
                result = JSONObject.fromObject(s);
                return result;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            httpClients.close();
            if (response != null) {
                response.close();
            }
        }
        return result;
    }*/

}
