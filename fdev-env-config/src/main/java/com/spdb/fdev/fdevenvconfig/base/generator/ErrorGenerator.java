package com.spdb.fdev.fdevenvconfig.base.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

@SuppressWarnings({"rawtypes", "unchecked", "resource"})
public class ErrorGenerator {

    private static Logger logger = LoggerFactory.getLogger(ErrorGenerator.class);

    /**
     * Generates a dictionary class from a properties-file
     * Creation date: (2002-8-29 18:16:57)
     *
     * @param args java.lang.String[]
     */
    public static void main(String[] args) {
        Properties fp = new Properties();
        String[] dictFilenames = {
                "/config/error_mapping.properties"};
        String targetName1 = "/config/errmsg_zh_CN.properties";
        String targetName = "../dict/ErrorConstants.java";
        logger.info("begin generated the ../dict/ErrorConstants.java file  .....");
        Properties tmpProp = new Properties();
        for (String fileName : dictFilenames) {
            try (InputStream in = tmpProp.getClass().getResourceAsStream(fileName)) {
                fp.load(in);
                fp.putAll(tmpProp);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }

        //find the target java file.
        String dictName = "/" + com.spdb.fdev.fdevenvconfig.base.generator.ErrorGenerator.class.getName().replace('.', '/');
        java.net.URL outUrl1 = com.spdb.fdev.fdevenvconfig.base.generator.ErrorGenerator.class.getResource(dictName + ".class");
        String outUrl = "";
        try {
            outUrl = URLDecoder.decode(outUrl1.getFile(), "UTF-8");
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        dictName = dictName.substring(0, (dictName.lastIndexOf('/') + 1));
        String targedFile = dictName + targetName;
        logger.info("the targedFile is: {}", targedFile);

        String tmpPath = outUrl;
        String tmp = "/target/";
        String rep = "src/main/java";
        String dir = null;
        if (tmpPath.contains(tmp)) {
            dir = tmpPath.substring(0, tmpPath.indexOf(tmp)) + "/";
        }
        File dirFile = null;
        if (new File(dir + rep).exists()) {
            dirFile = new File(dir + rep);
        }
        try (PrintStream prt = new PrintStream(new java.io.FileOutputStream(new File(dirFile, targedFile)));
             PrintStream prt2 = new PrintStream(new java.io.FileOutputStream(new File("src/main/resources", targetName1)))) {
            java.util.Set set = new TreeSet(fp.keySet());
            prt.println("/**");
            prt.println(" * ErrorConstants.java Created on " + new Date());
            prt.println(" * ");
            prt.println(" * Copyright 2004 Client Server International, Inc. All rights reserved.");
            prt.println(" * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.");
            prt.println(" */ ");
            prt.println("package com.spdb.fdev.fdevenvconfig.base.dict;");

            prt.println("/**");
            prt.println(" * FDEV系统错误字典文件");
            prt.println(" * Creation date: (" + new Date() + ")");
            prt.println(" * @author: Auto Generated");
            prt.println(" */");
            prt.println("public class ErrorConstants {\n");
            prt.println("private ErrorConstants() {}");

            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String name = (String) i.next();
                String value = new String(fp.getProperty(name).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                if (name.indexOf('[') == -1 && name.indexOf(']') == -1) //skip index type name. Lawrence Dai 2002/11/4
                {
                    String value1 = value.substring(0, value.indexOf('|'));
                    String value2 = value.substring(value.indexOf('|') + 1);
                    prt.println("/**  " + value2 + "  */");
                    prt.println("    public static final String "
                            + name.toUpperCase() + "=\"" + value1 + "\";");
                    prt2.println(value1 + "=" + value2);
                }
            }
            prt.println("}");
            logger.info("the ErrorConstants.java file generated .....");
        } catch (Exception e) {
            logger.info("Exception in main() of ErrorGenerator: {}", e.getMessage());
        }
    }

}
