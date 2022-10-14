package com.spdb.fdev.fdevapp;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdb.fdev.fdevapp.spdb.entity.GitlabCI;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabCIService;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitlabCIServiceTests {

	@Autowired
	IGitlabCIService service;

	@Test
	public void GitlabCITest() {
		try {
			GitlabCI gitlabCI = service.save(new GitlabCI());
			Assert.assertNotNull(gitlabCI);

			GitlabCI resultFind = service.findById(gitlabCI.getId());
			Assert.assertNotNull(resultFind);

			gitlabCI.setName("test");
			GitlabCI resultUpdate = service.update(gitlabCI);
			Assert.assertNotNull(resultUpdate);
			Assert.assertEquals("test", resultUpdate.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
