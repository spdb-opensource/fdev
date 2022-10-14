package com.spdb.fdev.fdemand.base.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
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
    public static void main(String[] args) throws UnsupportedEncodingException {
        Properties fp = new Properties();
        String[] dict_filenames = {
                "/config/error_mapping.properties"};
        String targetName1 = "/config/errmsg_zh_CN.properties";
        String targetName = "../dict/ErrorConstants.java";
        System.out.println("begin generated the ../dict/ErrorConstants.java file  .....");
        Properties tmpProp = new Properties();
        for (String fileName : dict_filenames) {
            try {
                //第一种读文件的方式可能会读不到，时情况修改
                    /*InputStream in = tmpProp.getClass().getResourceAsStream(fileName);
                    fp.load(in);
                    in.close();
                    fp.putAll(tmpProp);*/
                fp.load(new FileInputStream("src/main/resources" + fileName));
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }

        //find the target java file.
        String dictName = "/"
                + ErrorGenerator.class.getName().replace('.', '/');

        java.net.URL outUrl1 = ErrorGenerator.class.getResource(dictName
                + ".class");
        String outUrl = URLDecoder.decode(outUrl1.getFile(), "UTF-8");

        dictName = dictName.substring(0, (dictName.lastIndexOf('/') + 1));
        String targedFile = dictName + targetName;
        System.out.println("the targedFile is: " + targedFile);

        String tmpPath = outUrl;
        String tmp = "/target/";

        String rep = "src/main/java";

        String dir = null;

        if (tmpPath.indexOf(tmp) != -1) {
            dir = tmpPath.substring(0, tmpPath.indexOf(tmp));
        }

        File dirFile = null;
        if (new File(dir + "/" + rep).exists()) {
            dirFile = new File(dir + "/" + rep);
        }

        try(PrintStream prt = new PrintStream(new java.io.FileOutputStream(new File(dirFile, targedFile)));
            PrintStream prt2 = new PrintStream(new java.io.FileOutputStream(new File("src/main/resources", targetName1)));) {
            java.util.Set set = new TreeSet(fp.keySet());
            prt.println("/**");
            prt.println(" * ErrorConstants.java Created on " + new Date());
            prt.println(" * ");
            prt.println(" * Copyright 2004 Client Server International, Inc. All rights reserved.");
            prt.println(" * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.");
            prt.println(" */ ");
            prt.println("package com.spdb.fdev.fdemand.base.dict;");

            prt.println("/**");
            prt.println(" * FDEV系统错误字典文件");
            prt.println(" * @author: Auto Generated");
            prt.println(" */");
            prt.println("public class ErrorConstants {");

            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String name = (String) i.next();
                String value = new String(fp.getProperty(name).getBytes("ISO-8859-1"), "UTF-8");
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

            System.out.println("the ErrorConstants.java file generated .....");
        } catch (Exception e) {
            logger.info("Exception in main() of ErrorGenerator: " + e.toString());
        }
    }

}
