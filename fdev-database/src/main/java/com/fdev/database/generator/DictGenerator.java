package com.fdev.database.generator;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;


@SuppressWarnings({ "rawtypes", "unchecked", "resource" })
public class DictGenerator {

    /**
     * Generates a dictionary class from a properties-file
     */
    public static void main(String[] args) {
        Properties fp = new Properties();
        String[] dict_filenames = {
                "/dict-database.properties",};
        String targetName = "../dict/Dict.java";
        PrintStream prt = null;
        try {
            System.out.println("begin generated the ../dict/Dict.java file  .....");
            Properties tmpProp = new Properties();
            for (String fileName : dict_filenames) {
                try {
                    InputStream in = tmpProp.getClass().getResourceAsStream(fileName);
                    fp.load(in);
                    in.close();
                    fp.putAll(tmpProp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // find the target java file.
            String dictName = "/" + com.fdev.database.generator.DictGenerator.class.getName().replace('.', '/');
            java.net.URL outUrl1 = com.fdev.database.generator.DictGenerator.class.getResource(dictName + ".class");
            String outUrl = URLDecoder.decode(outUrl1.getFile(), "UTF-8");
            dictName = dictName.substring(0, (dictName.lastIndexOf("/") + 1));
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
            // PrintStream prt2 = new PrintStream(new java.io.FileOutputStream(new File("src/main/resources",fileNamesort)));
            java.util.Set set = new TreeSet(fp.keySet());
            prt.println("package com.fdev.database.dict;");
            prt.println("/**");
            prt.println(" * fdev系统——公共字典文件");
            prt.println(" * Creation date: (" + new Date() + ")");
            prt.println(" * @author: Auto Generated");
            prt.println(" */");
            prt.println("public class Dict {");
            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String name = (String) i.next();
                String value = new String(fp.getProperty(name).getBytes("ISO-8859-1"), "UTF-8");
                String[] message = value.split("\\|");
                if (name.indexOf('[') == -1 && name.indexOf(']') == -1) // skip index type name. Lawrence Dai 2002/11/4
                    prt.println("/**  " + message[0] + "  */");
                try {
                    prt.println("    public static final String " + name.toUpperCase() + "=\"" + message[1] + "\";");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("name  :" + name + "   message : " + message.toString());
                    System.out.println("Exception  :" + e);
                }
                // prt2.println(name+"="+value);
            }
            prt.println("}");
            System.out.println("the Dict.java file generated .....");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception in main() of DictGenerator: " + e.toString());
        } finally {
            prt.close();
        }
    }


}
