package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.dao.JiraIssuesDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.TaskJira;

@Repository
public class JiraIssuesDaoImpl implements JiraIssuesDao {
	private static Logger logger = LoggerFactory.getLogger(JiraIssuesDaoImpl.class);
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
    private RestTemplate restTemplate;

	@Override
	public String queryJiraIssues(String requestJson , String queryUrl ,String jiraUserAndPwd) {
		String auth=  new String(Base64.encodeBase64(jiraUserAndPwd.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic "+auth);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        Object obj = null;
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange( queryUrl + requestJson , HttpMethod.GET , request , String.class );
        } catch (RestClientException e) {
            logger.error("jira服务异常=== " + e.getMessage());
            throw new FdevException(ErrorConstants.JIRA_EXCEPTION);
        }
        int status = responseEntity.getStatusCodeValue();
        if(status == 403){
            logger.error("用户验证失败");
            throw new FdevException(ErrorConstants.JIRA_ERROR);
        }
        obj = responseEntity.getBody();
        if (CommonUtils.isNullOrEmpty(obj)) {
            return null;
        }
        return obj.toString() ;
	}

	@Override
	public List queryTaskAndJiraIssues(String taskId) {
		Criteria criteria = new Criteria();
		criteria.and(Dict.TASK_ID).in(taskId);
		Query query = new Query(criteria);
        List<TaskJira> list = mongoTemplate.find(query, TaskJira.class);
		return list;
	}
	
	@Override
	public String updateJiraIssues(String jsonString, String updateUrl, String jiraUserAndPwd ) {
		String auth=  new String(Base64.encodeBase64(jiraUserAndPwd.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic "+auth);
        headers.add("Content-Type","application/json");
        HttpEntity<String> request = new HttpEntity<String>(jsonString,headers);
        Object obj = null;
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange( updateUrl , HttpMethod.POST , request , String.class);
        } catch (RestClientException e) {
            logger.error("jira服务异常=== " + e.getMessage());
            throw new FdevException("看来您已尝试执行工作流程操作，最可能的原因是最近有人改变了问题，请刷新并再试一次！");
        }
        int status = responseEntity.getStatusCodeValue();
        if(status == 403){
            logger.error("用户验证失败");
            throw new FdevException(ErrorConstants.JIRA_ERROR);
        }
        obj = responseEntity.getBody();
        if (CommonUtils.isNullOrEmpty(obj)) {
            return null;
        }
        return (String) obj;
	}

	//查询测试中任务
	@Override
	public List<FdevTask> queryTestTask(String userId) {
		  List<FdevTask> result = mongoTemplate.find(Query.query(
	                Criteria.where(Dict.STAGE).in(CommonUtils.getTestTask()).
	                        orOperator(Criteria.where(Dict.DEVELOPER).all(userId),
	                                Criteria.where(Dict.TESTER).all(userId),
	                                Criteria.where(Dict.CONCERN).all(userId),
	                                Criteria.where(Dict.SPDB_MASTER).all(userId),
	                                Criteria.where(Dict.MASTER).all(userId),
	                                Criteria.where(Dict.CREATOR).all(userId))),
	         FdevTask.class);
		  return result;
	}

	@Override
	public void saveTaskAndJiraIssues(TaskJira taskJira) {
		Query query = Query.query(Criteria.where(Dict.JIRA_KEY).in(taskJira.getJira_key()));
		TaskJira result = mongoTemplate.findOne(query, TaskJira.class);
		if(CommonUtils.isNullOrEmpty(result)) {
			 ObjectId objectId = new ObjectId();
			 taskJira.set_id(objectId);
			 taskJira.setId(objectId.toString());
		     mongoTemplate.save(taskJira);
		}else {
			Update update = Update.update(Dict.JIRA_KEY, taskJira.getJira_key());
			update.set(Dict.TASK_ID, taskJira.getTask_id());
			update.set(Dict.TASK_NAME, taskJira.getTask_name());
			update.set(Dict.DEMAND_ID, taskJira.getDemand_id());
			mongoTemplate.findAndModify(query, update, TaskJira.class);
		}
	}

	@Override
	public TaskJira queryTaskName(String key) {
		TaskJira result = mongoTemplate.findOne(Query.query(
                Criteria.where(Dict.JIRA_KEY).in(key)),
				TaskJira.class);
		return result;
	}

	@Override
	public void putJiraIssues(String jsonString, String updateUrl, String jiraUserAndPwd) {
		String auth=  new String(Base64.encodeBase64(jiraUserAndPwd.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic "+auth);
        headers.add("Content-Type","application/json");
        HttpEntity<String> request = new HttpEntity<String>(jsonString,headers);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange( updateUrl , HttpMethod.PUT , request , String.class);
        } catch (RestClientException e) {
            logger.error("jira服务异常=== " + e.getMessage());
            
            throw new FdevException("该用户无该项目空间权限，请联系jira项目管理员进行添加！");
        }
        int status = responseEntity.getStatusCodeValue();
        if(status == 403){
            logger.error("用户验证失败");
            throw new FdevException(ErrorConstants.JIRA_ERROR);
        }
	}

    @Override
    public String createJiraSubTask(String jsonString, String updateUrl, String jiraUserAndPwd) {
        String auth=  new String(Base64.encodeBase64(jiraUserAndPwd.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic "+auth);
        headers.add("Content-Type","application/json");
        HttpEntity<String> request = new HttpEntity<String>(jsonString,headers);
        ResponseEntity<String> responseEntity = null;
        Object obj = null;
        try {
            responseEntity = restTemplate.exchange( updateUrl , HttpMethod.POST , request , String.class);
        } catch (RestClientException e) {
            logger.error("jira服务异常=== " + e.getMessage());
            throw new FdevException("jira服务异常或无该项目空间权限");
        }
        int status = responseEntity.getStatusCodeValue();
        if(status == 403){
            logger.error("用户验证失败");
            throw new FdevException(ErrorConstants.JIRA_ERROR);
        }
        obj = responseEntity.getBody();
        if (CommonUtils.isNullOrEmpty(obj)) {
            return null;
        }
        return (String) obj;
    }

    @Override
    public void deleteJiraSubTask(String key, String updateUrl, String jiraUserAndPwd) {
        String auth=  new String(Base64.encodeBase64(jiraUserAndPwd.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic "+auth);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange( updateUrl + key, HttpMethod.DELETE , request , String.class);
        } catch (RestClientException e) {
            logger.error("jira服务异常=== " + e.getMessage());
            throw new FdevException("jira服务异常！");
        }
        int status = responseEntity.getStatusCodeValue();
        if(status == 403){
            logger.error("用户验证失败");
            throw new FdevException(ErrorConstants.JIRA_ERROR);
        }
    }
}
