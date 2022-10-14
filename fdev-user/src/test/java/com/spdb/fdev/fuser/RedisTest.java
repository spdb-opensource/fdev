package com.spdb.fdev.fuser;

import com.spdb.fdev.fuser.spdb.entity.user.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * Created by wujin on 2019/1/2.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Test
	@Cacheable
	public void test() throws Exception {
		// 保存字符串
        User user = new User();
        redisTemplate.opsForValue().set("ll", user);
		Set keys = redisTemplate.keys("*ll*");
		for (Object o : keys) {
			String key = (String) o;
			key = key.substring("sit.".length());
			if (!key.contains("user.login.token"))
				System.out.println(redisTemplate.delete(key));
		}
	}
}
