package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.service.IProdRecordService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProdApplicationServiceImplTest {

    @Autowired
    private IProdApplicationDao prodApplicationDao;
    @Autowired
    private IProdRecordService prodRecordService;

    @Test
    public void findLatestUri() throws Exception {

        ProdRecord prodRecord = prodRecordService.queryDetail("5e15d03aefeae4000e1596bb");
        String oldUrl =  prodApplicationDao.findLatestUri("5cd3b9b8a94f870006cf0818", prodRecord, Dict.CAAS);
        System.out.println(oldUrl);
    }
}