package com.spdb.fdev.fuser;

import com.spdb.fdev.fuser.spdb.entity.user.User;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * Created by wujin on 2019/1/2.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoTest {
    private MongoTemplate mongoTemplate;
    @Test
    public void test1(){
    }
}
