package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.service.IPythonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

@Service
@RefreshScope
public class PythonServiceImpl implements IPythonService {

    private static Logger logger = LoggerFactory.getLogger(PythonServiceImpl.class);

    @Value("${python.path}")
    private String python_path;

    @Override
    /**
     * 调用python脚本获取组件扫描结果
     */
    public String queryDependencyTree(String groupId, String artifactId, String version) throws IOException, InterruptedException {
        logger.info("脚本目录：{},参数1：{},参数2：{},参数3：{}", python_path + "scanComponent.py", groupId, artifactId, version);
        String[] cmdA = {"python", python_path + "scanComponent.py", groupId, artifactId, version};
        Process process = Runtime.getRuntime().exec(cmdA);
        try (BufferedReader success = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));) {
            StringBuffer sc = new StringBuffer();
            StringBuffer er = new StringBuffer();
            String line1;
            while ((line1 = success.readLine()) != null) {
                sc.append(line1 + "\n");
            }
            String line2;
            while ((line2 = error.readLine()) != null) {
                er.append(line2);
            }
            if (!CommonUtils.isNullOrEmpty(er.toString())) {
                FdevException fdevException = new FdevException(ErrorConstants.QUERY_DEPENDENCY_TREE);
                fdevException.setMessage(er.toString());
                throw fdevException;
            }
            return sc.toString().substring(0, sc.lastIndexOf("\n"));
        } catch (IOException e) {
            logger.error("执行{}的时候出现错误，错误信息如下{}", python_path, e);
        }
        return null;
    }
}
