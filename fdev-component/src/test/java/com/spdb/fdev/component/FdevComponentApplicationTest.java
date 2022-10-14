package com.spdb.fdev.component;

import com.spdb.fdev.base.lock.RedissionConfiguration;
import com.spdb.fdev.base.utils.GitUtilsNew;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAsync
@EnableScheduling
@Import({RedissionConfiguration.class, GitUtilsNew.class})
public class FdevComponentApplicationTest {
    @Test
    public void contextLoads() {
    }

}
