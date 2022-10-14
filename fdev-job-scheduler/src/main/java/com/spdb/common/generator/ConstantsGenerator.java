package com.spdb.common.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Auto Generated
 */
@SuppressWarnings({"rawtypes", "unchecked", "resource"})
public class ConstantsGenerator {

    /**
     * Generates a dictionary class from a properties-file
     * Creation date: (2002-8-29 18:16:57)
     *
     * @param args java.lang.String[]
     */
    public static void main(String[] args) {
        Properties fp = new Properties();
        String[] dictFilenames = {
                "/config/i18n/error_mapping.properties"};
        String targetName1 = "/config/i18n/errmsg_zh_CN.properties";
        String targetName = "../dict/Constants.java";
        PrintStream prt = null;
        PrintStream prt2 = null;
        InputStream in = null;
        try {
            System.out.println("begin generated the ../dict/Constants.java file  .....");
            Properties tmpProp = new Properties();
            for (String fileName : dictFilenames) {
                in = tmpProp.getClass().getResourceAsStream(fileName);
                fp.load(in);
                in.close();
                fp.putAll(tmpProp);
            }

            //find the target java file.
            String dictName = "/"
                    + ConstantsGenerator.class.getName().replace('.', '/');

            java.net.URL outUrl1 = ConstantsGenerator.class.getResource(dictName
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

            prt = new PrintStream(new java.io.FileOutputStream(
                    new File(dirFile, targedFile)));

            prt2 = new PrintStream(new java.io.FileOutputStream(new File("src/main/resources", targetName1)));

            java.util.Set set = new TreeSet(fp.keySet());
            prt.println("/**");
            prt.println(" * ErrorConstants.java Created on " + new Date());
            prt.println(" * ");
            prt.println(" * Copyright 2004 Client Server International, Inc. All rights reserved.");
            prt.println(" * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.");
            prt.println(" */ ");
            prt.println("package com.spdb.common.dict;");

            prt.println("/**");
            prt.println(" * 字典文件");
            prt.println(" * Creation date: (" + new Date() + ")");
            prt.println(" * @author: Auto Generated");
            prt.println(" */");
            prt.println("public class Constants {");

            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String name = (String) i.next();
                String value = new String(fp.getProperty(name).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                //skip index type name. Lawrence Dai 2002/11/4
                if (name.indexOf('[') == -1 && name.indexOf(']') == -1) {
                    String value1 = value.substring(0, value.indexOf('|'));
                    String value2 = value.substring(value.indexOf('|') + 1);
                    prt.println("/**  " + value2 + "  */");
                    prt.println("    public static final String "
                            + name.toUpperCase() + "=\"" + value1 + "\";");
                    prt2.println(value1 + "=" + value2);
                }
            }
            prt.println("}");

            System.out.println("the Constants.java file generated .....");

        } catch (Exception e) {
            System.err.println("Exception in main() of ConstantsGenerator: "
                    + e.toString());
        } finally {
            if(prt != null ){
                prt.close();
            }
            if(prt2 != null ){
                prt2.close();
            }
        }
    }
}
