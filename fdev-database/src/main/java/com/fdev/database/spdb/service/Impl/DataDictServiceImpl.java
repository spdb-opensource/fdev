package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.dao.DataDictDao;
import com.fdev.database.spdb.entity.DataDict;
import com.fdev.database.spdb.service.DataDictService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;


@Service
@RefreshScope
public class DataDictServiceImpl implements DataDictService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Autowired
    private RestTransport restTransport;

    @Resource
    private DataDictDao dataDictDao;

    @Value("${datadict.informix.fieldType}")
    private String informixFieldType;

    @Value("${datadict.oracle.fieldType}")
    private String oracleFieldType;

    @Value("${datadict.mysql.fieldType}")
    private String mysqlFieldType;

    @Override
    public List<DataDict> queryByKey(DataDict dataDict) throws Exception {
        return dataDictDao.queryByKey(dataDict);
    }

    @Override
    public DataDict add(DataDict dataDict) {
        return dataDictDao.add(dataDict);
    }

    @Override
    public void update(DataDict dataDict) {
        dataDictDao.update(dataDict);
    }

    @Override
    public List<String> queryFieldType(String fieldType) {
        List<String> result = new ArrayList<>();
        String[] fieldTypes = {};
        if(fieldType.equals("informix")){
            fieldTypes = informixFieldType.split(",");
        } else if (fieldType.equals("mysql")){
            fieldTypes = mysqlFieldType.split(",");
        } else if (fieldType.equals("oracle")){
            fieldTypes = oracleFieldType.split(",");
        }
        for(String type : fieldTypes){
            result.add(type);
        }
        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse resp) {
        ClassPathResource resource = new ClassPathResource("file/template.xlsx");
        try (OutputStream output = resp.getOutputStream();
             InputStream in = resource.getInputStream();) {
            // 读取文件
            resp.reset();
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Headers", "content-type");
            resp.setHeader("Access-Control-Allow-Methods", "*");
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/vnd.ms-excel;charset=utf-8");
            resp.setHeader("Content-Disposition",
                    "attachment;filename=" +"template.xlsx");
            // 写文件
            byte buffer[] = new byte[4096];
            int x = -1;
            while ((x = in.read(buffer, 0, 4096)) != -1) {
                output.write(buffer, 0, x);
            }
            resp.flushBuffer();
        } catch (Exception e) {
            logger.error("e"+e);
            throw new FdevException(ErrorConstants.DOWNLOAD_ERROR, new String[]{"模版下载失败！"});
        }
    }

    @Override
    public List<DataDict> queryIdByFields(List<String> all_field) {
        return dataDictDao.queryIdByFields(all_field);
    }
}
