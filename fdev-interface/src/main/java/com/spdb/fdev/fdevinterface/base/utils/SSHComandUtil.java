package com.spdb.fdev.fdevinterface.base.utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @auther :gejunpeng
 * @date :2020/12/25 13:20
 * @location :shanghai
 * @description: 登录远程服务器执行终端命令
 */
public class SSHComandUtil {

    private static Logger logger = LoggerFactory.getLogger(SSHComandUtil.class);

    public static void excute(String user, String password, String ip, String cmd) {
        try {
            Connection conn = new Connection(ip);
            conn.connect();
            boolean isAuthed = conn.authenticateWithPassword(user, password);
            if (isAuthed) {
                Session session = conn.openSession();
                session.execCommand(cmd);
                String stdout = processStdout(session.getStdout());
                logger.info("执行命令: {}", cmd);
                logger.info("执行命令的stdout: {}", stdout);
                session.close();
                conn.close();
            } else {
                throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR, new String[]{"账号认证失败"});
            }
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR, new String[]{"执行远端命令出错" + cmd});
        }
    }

    private static String processStdout(InputStream in) {
        StringBuilder buffer = new StringBuilder();
        try (InputStream stdout = new StreamGobbler(in);
             BufferedReader br = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));){
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.SCP_ROUTE_DATA_ERROR, new String[]{"解析远端命令输出出错" + e.getMessage()});
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        excute("weblogic", "weblogic*2020", "xxx", "cd /tmp/gjp");
    }
}
