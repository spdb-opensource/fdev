package com.spdb.fdev.fdevapp;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdb.fdev.fdevapp.spdb.entity.DeployNet;
import com.spdb.fdev.fdevapp.spdb.service.IDeployNetService;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeployNetServiceTests {
	  @Autowired
	  IDeployNetService service;

	    @Test
	    public void DeployNetTest() {
	        try {
	            DeployNet deployNet = service.save(new DeployNet());
	            Assert.assertNotNull(deployNet);

	            DeployNet resultFind = service.findById(deployNet.getId());
	            Assert.assertNotNull(resultFind);

	            deployNet.setName("test");
	            DeployNet resultUpdate = service.update(deployNet);
	            Assert.assertNotNull(resultUpdate);
	            Assert.assertEquals("test",resultUpdate.getName());


	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	    }
}
