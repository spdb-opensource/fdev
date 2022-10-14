package com.spdb.fdev.fuser;

import java.util.Vector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by wujin on 2019/1/2.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UtilTest {
	
    @Autowired
    private MongoTemplate mongoTemplate;
	@Test
	public void test(){

	}
	
	public static void main(String[] args) {
		Vector<String> v;
	}

}
