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
public class DictGenerator {

    /**
     * Generates a dictionary class from a properties-file
     */
    public static void main(String[] args) {
        Properties fp = new Properties();
        String[] dictFilenames = {
                "/config/i18n/dict.properties"};
        String targetName = "../dict/Dict.java";
        PrintStream prt = null;
        InputStream in = null;
        try {
            System.out.println("begin generated the ../dict/Dict.java file  .....");
            Properties tmpProp = new Properties();
            for (String fileName : dictFilenames) {
                in = tmpProp.getClass().getResourceAsStream(fileName);
                fp.load(in);
                in.close();
                fp.putAll(tmpProp);
            }

            // find the target java file.
            String dictName = "/" + DictGenerator.class.getName().replace('.', '/');
            java.net.URL outUrl1 = DictGenerator.class.getResource(dictName + ".class");
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
            prt = new PrintStream(new java.io.FileOutputStream(new File(dirFile, targedFile)));
            java.util.Set set = new TreeSet(fp.keySet());
            prt.println("package com.spdb.common.dict;");
            prt.println("/**");
            prt.println(" * 公共字典文件");
            prt.println(" * Creation date: (" + new Date() + ")");
            prt.println(" * @author: Auto Generated");
            prt.println(" */");
            prt.println("public class Dict {");
            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String name = (String) i.next();
                String value = new String(fp.getProperty(name).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String[] message = value.split("\\|");
                // skip index type name. Lawrence Dai 2002/11/4
                if (name.indexOf('[') == -1 && name.indexOf(']') == -1) {
                    prt.println("/**  " + message[0] + "  */");
                }
                try {
                    prt.println("  public static final String " + name.toUpperCase() + "=\"" + message[1] + "\";");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("name:" + name + "   message:" + message.toString());
                    System.out.println("Exception:" + e);
                }
            }
            prt.println("}");
            System.out.println("the Dict.java file generated .....");
        } catch (Exception e) {
            System.err.println("Exception in main() of DictGenerator: " + e.toString());
        } finally {
            if(prt != null ){
                prt.close();
            }
        }
    }


}
