package com.manager.ftms;

import com.manager.ftms.service.TestCaseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FtmsApplicationTests {

	@Autowired
	private TestCaseService TestCaseService;

	@Test
	public void testCaseNo(){
		System.out.println("开始测试");
		System.out.println("结束测试");
	}


}
