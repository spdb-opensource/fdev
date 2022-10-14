package com.spdb.fdev.base.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ShellUtils {

    public static Logger logger = LoggerFactory.getLogger(ShellUtils.class);

    /**
     * 查询前端组件版本，如果flag为true，代表查询所有版本
     *
     * @param mpassComponentName
     * @param flag
     * @return
     */
    public static String cellShell(String mpassComponentName, String mpassComponentGroup, boolean flag) {
        String shell;
        StringBuffer stringBuffer;
        if (StringUtils.isNotBlank(mpassComponentGroup)) {
            mpassComponentName = "@" + mpassComponentGroup + "/" + mpassComponentName;
        }
        if (flag) {
            shell = "npm view " + mpassComponentName + " versions";

        } else {
            shell = "npm view " + mpassComponentName + " version";
        }
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
            shell = "cmd /c " + shell;
        }
        logger.info("shell:" + shell);
        Process child = null;
        try {
            child = Runtime.getRuntime().exec(shell);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (InputStream in = child.getInputStream();
             InputStreamReader isr = new InputStreamReader(in);
             BufferedReader br = new BufferedReader(isr);) {
            stringBuffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                logger.error("InterruptedException", e);
                //重新设置线程中断标志为true
                Thread.currentThread().interrupt();
            }
        } catch (
                IOException e) {
            logger.error(e.getMessage());
            logger.error("cell error");
            return null;
        }
        return stringBuffer.toString();
    }

    public static void runCmd(String cmd) {
        String[] cmdA = {"/bin/sh", "-c", cmd};
        try {
            Process process = Runtime.getRuntime().exec(cmdA);
        } catch (IOException e) {
            logger.error("执行 {} 的时候出现错误，错误信息如下{}", cmd, e);
        }
    }

    /**
     * standard-version --release-as 修改changelog
     *
     * @param dir
     * @param dir
     * @return
     */
    public static void releaseShell(String dir, String tag) {
        String command = "cd " + dir + " && standard-version --release-as " + tag;
        String[] cmdA = {"/bin/sh", "-c", command};
        logger.info("shell:" + StringUtils.join(cmdA));
        try {
            Process child = Runtime.getRuntime().exec(cmdA);
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                logger.error("InterruptedException", e);
                //重新设置线程中断标志为true
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.error("cell error");
        }
    }
}
