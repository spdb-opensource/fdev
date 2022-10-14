package com.spdb.fdev.fdevapp.base.utils;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class ShellUtils {
    public static Logger logger = LoggerFactory.getLogger("ShellUtils");

    /**
     * 生成应用骨架
     *
     * @return
     */
    public static boolean cellShell(Map archetype, String projectName) {
        boolean result = true;
        String shell= null;
        if (((String)archetype.get(Dict.ARTIFACTID)).contains(Dict.VUE)){
            shell = "/ebank/vue.sh "+projectName;
        }else {
            shell = "/ebank/archetype.sh " + archetype.get(Dict.GROUPID) + " " + archetype.get(Dict.ARTIFACTID) + " " + projectName + " " + archetype.get(Dict.RECOMMOND_VERSION);
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
            String line = null;
            while ((line = br.readLine()) != null){
                logger.debug("line : " + line);
            }
            in.close();
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                logger.error("InterruptedException",e);
                //重新设置线程中断标志为true
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            logger.error("cell error");
            return false;
        }

        return result;
    }

//    public static boolean cmdShell(Archetype archetype, String projectName,String localPath) {
//        boolean result = true;
//        logger.info("localPath:" + localPath);
//        Runtime runtime = Runtime.getRuntime();
//        Process process;
//        try {
//            process = runtime.exec("ls ./");
//            int i = 0;
//            try {
//                i = process.waitFor();
//            } catch (InterruptedException e) {
//                logger.error("InterruptedException",e);
//                //重新设置线程中断标志为true
//                Thread.currentThread().interrupt();
//            }
//            logger.info("process:" + i);
//
//            InputStream is = process.getInputStream();
//
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(isr);
//            String line = null;
//            String s = br.readLine();
//            logger.info("message: "+ s);
//            while ((line = br.readLine()) != null){
//                logger.debug("line : " + line);
//            }
//        } catch (IOException e) {
//            logger.error("cmd error");
//            result = false;
//        }
//        return result;
//    }
}
