package com.spdb.fdev.fdevapp;

import com.spdb.fdev.fdevapp.spdb.service.IArchetypeService;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created with IntelliJ IDEA.
 * User: luotao
 * Date: 2019/2/27
 * Time: 下午6:40
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ArchetypeServiceTests {

    @Autowired
    IArchetypeService service;

    @Test
    public void testArchetype() throws Exception {
        service.queryArchetype("5df896521dcf5400150c73cb");
    }

}
