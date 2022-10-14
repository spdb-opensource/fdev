package com.spdb.fdev.fdevtask;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GitHttpServer;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import io.swagger.models.auth.In;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class Test1 {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IAppApi iAppApi;
    @Autowired
    private MongoTemplate mongoTemplate;


    @Test
    public void test(){
        String a = "数据库审核";
        char[] b = a.toCharArray();
        StringBuffer re = new StringBuffer();
        for(int i=0;i<b.length;i++){
            re.append("\\u"+Integer.toHexString((int)b[i]));
        }
        System.out.println(re);
    }

    @Test
    public void test2(){

        Query q = new Query();
        Criteria c = new Criteria();
        c.orOperator(
                Criteria.where("start_time").exists(false),
                Criteria.where("start_inner_test_time").exists(false),
                Criteria.where("start_uat_test_time").exists(false),
                Criteria.where("stop_uat_test_time").exists(false),
                Criteria.where("start_rel_test_time").exists(false),
                Criteria.where("fire_time").exists(false)

        );
        q.addCriteria(c);
        List errTask = new ArrayList();
        List<FdevTask> list = mongoTemplate.find(q,FdevTask.class,"task");
        errTask = list.stream().map(n->{
            boolean f = false;
            String errtime = "";
            Map tmp = new HashMap();
            String id = n.getId();
            String name = n.getName();
            String stage = n.getStage();
            String realStart = n.getStart_time();
            String realDevEnd = n.getStart_inner_test_time();
            String realSitEnd = n.getStart_uat_test_time();
            String realUatEnd = n.getStop_uat_test_time();
            String fire = n.getFire_time();
            tmp.put("id",id);
            tmp.put("name",name);
            tmp.put("realStart",realStart);
            tmp.put("realDevEnd",realDevEnd);
            tmp.put("realSitEnd",realSitEnd);
            tmp.put("realUatEnd",realUatEnd);
            tmp.put("fire",fire);
            if(!CommonUtils.isNotNullOrEmpty(stage,realStart)){
                f = true;
                errtime = "start";
                tmp.put("errtime",errtime);
                return tmp;
            }
            switch (stage){
                case Dict.TASK_STAGE_CREATE_INFO:
                case Dict.TASK_STAGE_CREATE_APP:
                case Dict.TASK_STAGE_CREATE_FEATURE:
                case Dict.TASK_STAGE_DEVELOP:
                case Dict.TASK_STAGE_SIT:
                    if(!CommonUtils.isNullOrEmpty(realDevEnd) &&
                            CommonUtils.isNullOrEmpty(realSitEnd) &&
                            CommonUtils.isNullOrEmpty(realUatEnd) &&
                            CommonUtils.isNullOrEmpty(fire) ){
                    }else {
                        f = true;errtime = "before-uat";
                    }
                    break;
                case Dict.TASK_STAGE_UAT:
                    if(!CommonUtils.isNullOrEmpty(realDevEnd) &&
                            !CommonUtils.isNullOrEmpty(realSitEnd) &&
                            CommonUtils.isNullOrEmpty(realUatEnd) &&
                            CommonUtils.isNullOrEmpty(fire) ){

                    }else {
                        f = true;errtime = "uat";
                    }
                    break;
                case Dict.TASK_STAGE_REL:
                    if(!CommonUtils.isNullOrEmpty(realDevEnd) &&
                            !CommonUtils.isNullOrEmpty(realSitEnd) &&
                            !CommonUtils.isNullOrEmpty(realUatEnd) &&
                            CommonUtils.isNullOrEmpty(fire) ){

                    }else {
                        f = true;errtime = "rel";
                    }
                    break;
                case Dict.TASK_STAGE_PRODUCTION:
                case Dict.TASK_STAGE_FILE:
                    if(!CommonUtils.isNullOrEmpty(realDevEnd) &&
                            !CommonUtils.isNullOrEmpty(realSitEnd) &&
                            !CommonUtils.isNullOrEmpty(realUatEnd) &&
                            !CommonUtils.isNullOrEmpty(fire) ){

                    }else {
                        f = true;errtime = "after-rel";
                    }
                    break;
                default:
                    break;

            }
            tmp.put("errtime",errtime);

            return tmp;
        }).collect(Collectors.toList());
        JSONArray jsonObject = JSONArray.fromObject(errTask);
        System.out.println(jsonObject.toString());


    }

    @Test
    public void test3(){
        GitHttpServer gitHttpServer = new GitHttpServer();
        gitHttpServer.getMergeInfo(new HashMap(){{
            put("gitlab_project_id",499);
            put("state","merged");
            put("source_branch","SIT");
            put("target_branch","master");
        }});
    }

    @Test
    public void test4(){
        List<Integer> a = new ArrayList();
        a.add(4);
        a.add(5);
        a.add(6);
        a.add(7);
        a.add(7);
        for(int i=0;i<5;i++){
            if(a.get(i) == 7){
                a.remove(i);
            }
        }
        a.size();
    }

    /*@org.junit.Test
    public void test1() throws Exception {
       String url = "xxx/spdb-tms-server/require/addRequireImplementNo";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json");
            JSONObject param=new JSONObject();
            param.put("implementNo", "5c935517a3178a67d421fe6b");
            param.put("requireNo","研发单元2018运营137-005");
            param.put("requireName","测试任务1");
            param.put("requireRemark","公共");
            param.put("rsrvfld1", "所属系统");
            param.put("internalTestStatus", "未测试");
            param.put("programmer","开发人员");
            param.put("internalTester","测试人员");
            param.put("spdbManager","行方人员");
            param.put("businessTester", "业内老师");
            HttpEntity<Object> request = new HttpEntity<Object>(param, httpHeaders);
            ResponseEntity<String> postForEntity=restTemplate.postForEntity(url, request, String.class);
            System.out.print(postForEntity.toString());
    }*/
}
