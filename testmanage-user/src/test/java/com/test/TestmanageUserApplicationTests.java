package com.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestmanageUserApplicationTests {

    @Test
    public void contextLoads() {
        Flux.just("tom","jack","alien").filter(s -> s.length()>3).map(s -> s.concat("@qq.com")).subscribe(System.out::println);
    }

}
