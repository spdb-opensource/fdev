package com.spdb.fdev.fdevapp;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitlabAPIServiceTests {
	@Autowired
	private IGitlabAPIService iGitlabAPIService;
	private String id = "1633";
	private String token = "XGF1-3oiBsdSkhJxkYi2";
	private String mergeRequest = "true";
	private String pipeline = "true";
	private String gitlabciId = "5c7e462d2b90000528ccdb1e";
	private String ref = "master";
	private List<Map<String, String>> variables = null;
	private String userid="467";
	private String path="http://xxx/devops";
	private String name="test-project";
	private String luotao_token="qNMxg-bnRsBievnGFM1h";
	private String sbranch="test-project";
	private String tbranch="master";
	private String title="test";
	private String iid="6";
	@Test
	public void GitlabAPIServiceTest() {
		/*try {
			Map<String,Object> result = iGitlabAPIService.addProjectHook(id, token, mergeRequest, pipeline);
			Assert.assertNotNull(result);
			Map<String,Object> createPipeline = iGitlabAPIService.createPipeline(id, ref, variables, token);
			Assert.assertNotNull(createPipeline);
			Map<String,Object> quertProjectByUser = iGitlabAPIService.quertProjectByUser(userid, token);
			Assert.assertNotNull(quertProjectByUser);
			Map<String,Object> queryProjectById = iGitlabAPIService.queryProjectById(id, token);
			Assert.assertNotNull(queryProjectById);
			Map<String,Object> queryProjectMembers = iGitlabAPIService.queryProjectMembers(id, token);
			Assert.assertNotNull(queryProjectMembers);
			Map<String,Object> createProject = iGitlabAPIService.createProject(path, name, luotao_token);
			Assert.assertNotNull(createProject);
			Map<String,Object> createBranch = iGitlabAPIService.createBranch(id,name,ref,token);
			Assert.assertNotNull(createBranch);
			Map<String,Object> createMergeRequest = iGitlabAPIService.createMergeRequest(id,sbranch,tbranch,title,token);
			Assert.assertNotNull(createMergeRequest);
			Map<String,Object> mergeRequestInfo = iGitlabAPIService.getMergeRequestInfo(id, iid, token);
			Assert.assertNotNull(mergeRequestInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

}
