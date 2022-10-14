package com.gotest.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotest.dao.TaskListMapper;
import com.gotest.dao.WorkOrderMapper;
import com.gotest.dict.Dict;
import com.gotest.domain.TaskList;
import com.gotest.domain.WorkOrder;
import com.gotest.service.IUserService;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Component
@RefreshScope
public class FdevTransport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private TaskListMapper taskListMapper;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Value("${fdev.transport.log.data.enabled:true}")
    private boolean logDataEnabled = true;

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private IUserService userService;


    /**
     * 将json数据发送到外部特定的api接口
     * 并接收返回值。
     */
    public String doPost(String url, JSONObject date) {
        HttpClient client = null;
        HttpPost post = null;
        JSONObject jsonObject = null;
        String result = "";
        try {
            client = HttpClients.createDefault();
            post = new HttpPost(url);        //要调用的接口
            StringEntity s = new StringEntity(date.toString()); //发送数据
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            post.addHeader("source", "back");   //免验证 请求头
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(res.getEntity(), "utf-8"); //返回结果
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ((CloseableHttpClient) client).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 更新fdev测试人员
     * @param workOrderNo
     * @param groupLeader
     * @return
     */
    public void sendMessageToFdev(String workOrderNo, List<String> groupLeader) throws Exception {
        Set<String> taskNos = new HashSet<>();
        //获取工单主任务
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workOrderNo);
        if(!Util.isNullOrEmpty(workOrder.getMainTaskNo())){
            taskNos.add(workOrder.getMainTaskNo());
        }
        //获取任务集中任务
        List<String> taskListNos = taskListMapper.queryTaskNoByOrder(workOrderNo);
        taskNos.addAll(taskListNos);
        //获取人员id
        List<String> testers = groupLeader.stream().distinct().collect(Collectors.toList());
        List<String> testerIds = new ArrayList<>();
        for(String testerEn : testers){
            Map<String, Object> user = userService.queryUserCoreDataByNameEn(testerEn);
            if (!Util.isNullOrEmpty(user)){
                testerIds.add((String) user.get(Dict.ID));
            }
        }
        for(String taskNo : taskNos){
            Map send = new HashMap();
            send.put(Dict.REST_CODE, "updatetaskinner");
            send.put(Dict.TESTER, testerIds);
            send.put(Dict.ID, taskNo);
            try {
                restTransport.submitSourceBack(send);
            } catch (Exception e) {
                logger.error("fail to update fdev task testers for task : " + taskNo);
            }
        }
    }


    public Set<String> getUsersEmail(HashMap model, String mainTaskNo) throws Exception {
        List<String> taskNoList = new ArrayList<>();
        if(!Util.isNullOrEmpty(mainTaskNo)){
            taskNoList.add(mainTaskNo);
        }
        String workNo = String.valueOf(model.get(Dict.WORKORDERNO));
        //查询是否有子任务，如果有子任务，汇总任务编号
        try {
            List<TaskList> subTaskList = taskListMapper.queryTaskByNo(workNo);
            if(!Util.isNullOrEmpty(subTaskList)){
                for(TaskList t : subTaskList){
                    taskNoList.add(t.getTaskno());
                }
            }
        } catch (Exception e) {
            logger.error("fail to query fdev subtask info");
        }
        //遍历主子任务
        Set<String> fdevUsers = new HashSet<>();
        for(String id : taskNoList){
            Map mm = new HashMap<String, String>();
            mm.put(Dict.ID, id);
            mm.put(Dict.REST_CODE, "queryTaskDetail");
            try {
                mm = (Map) restTransport.submitSourceBack(mm);
                //拼接 任务负责人  暂不生效显示
                model.put(Dict.MASTER, makeFdevUserName(mm, fdevUsers, Dict.MASTER));
                //拼接 行方管理员  暂不生效显示
                model.put(Dict.SPDBMASTER, makeFdevUserName(mm, fdevUsers, Dict.SPDBMASTER));
                //拼接 开发人员   暂不生效显示
                model.put(Dict.DEVELOPER, makeFdevUserName(mm, fdevUsers, Dict.DEVELOPER));
                //拼接 任务创建人  暂不生效显示
                fdevUsers.add((String) ((Map) mm.get(Dict.CREATOR)).get(Dict.ID));
                model.put(Dict.CREATOR, ((Map) mm.get(Dict.CREATOR)).getOrDefault(Dict.USERNAMECN, "无")); //任务创建人
            } catch (Exception e) {
                logger.error("查询fdev任务失败");
            }
        }
        // 根据当前任务 所有用户 id 获取到 对应用户的email
        fdevUsers.remove(null);
        Set<String> userEmails = new HashSet<>();
        for (String users : fdevUsers) {
            Map<String, Object> user = userService.queryUserCoreDataById(users);
            if(!Util.isNullOrEmpty(user)){
                userEmails.add((String) user.getOrDefault(Dict.EMAIL, null));
            }
        }
        userEmails.remove(null);
        return userEmails;
    }

    /**
     * 将 fdev任务明细中
     * 人员信息数据提取出来
     */
    public String makeFdevUserName(Map fdevDate, Set<String> fdevUsers, String key) {
        //拼接 任务负责人
        Set<String> persons = new HashSet<>();
        for (Map k : (ArrayList<Map>) fdevDate.get(key)) {
            persons.add((String) k.get(Dict.USERNAMECN));
            fdevUsers.add((String) k.get(Dict.ID));
        }
        StringBuffer sb = new StringBuffer();
        Iterator iterator = persons.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString());
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public String sendPatch(String url,String token,Map sendMap) {
        logger.info("===patch url"+url);
        String patchForObject = restTemplate.patchForObject(url, sendDataPrepare(token,sendMap),  String.class);
        return patchForObject;
    }

    public HttpEntity<Object> sendDataPrepare(String token, Map sendMap) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.AUTHORIZATION, token);
        httpHeaders.addAll(header);
        HttpEntity<Object> request = null;
        if(Util.isNullOrEmpty(sendMap)) {
            request = new HttpEntity<Object>(httpHeaders);
        }else {
            JSONObject parse = JSONObject.parseObject(JSON.toJSONString(sendMap));
            request = new HttpEntity<Object>(parse, httpHeaders);
        }
        return request;
    }


}
