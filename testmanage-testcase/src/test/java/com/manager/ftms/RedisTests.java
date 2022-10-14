package com.manager.ftms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedisTests {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	public void contextLoads() {
		//redisTemplate.opsForValue().set("www", "1111", 1000, TimeUnit.SECONDS);
		Object result = redisTemplate.opsForValue().get("T-panj1user.login.token"); 
		System.out.println(result);
	}

	@Test
	public void jobs() {
//		Testcase testcase = new Testcase();
//		testcase.setPlanId(1);
//		testcase.setExpectedResult("1");
//		testcase.setRemark("aaa");
//		testcase.setTestcaseExecuteResult("sadadasd");
//		testcase.setFuncationPoint("1");
//		testcase.setTestcaseName("qqqqqq");
//		testcase.setTestcasePriority("1");
//		Map tMap = (Map) map.get("testcase");
//		String testcaseNo = this.generateTestcaseNo(map);
//		tMap.put("testcaseNo", testcaseNo);
//		tMap.put("testcaseStatus", "0");// 新增时状态
//		tMap.put("testcasePeople", redisUtils.getCurrentUserInfoMap().get(Constants.USER_EN_NAME));// 案例编写人redisUtils.getCurrentUserInfoMap().get(Constants.USER_EN_NAME)
//		tMap.put("testcaseDate", Utils.dateUtil(new Date()));// 时间
//		tMap.put("testcaseVersion", "1");// 案例版本号
//		System.out.println(tMap + "***********");
//		Testcase testcase = Utils.parseMap2Object(tMap, Testcase.class);
//		try {
//			int num = testcaseMapper.addTestcase(testcase);
//		} catch (Exception e) {
//			throw new FtmsException(DicConstants.TRY_AGAIN_LATER);
//		}
//		return testcaseMapper.queryDetailByTestcaseNo(testcaseNo);
		
	}
}
