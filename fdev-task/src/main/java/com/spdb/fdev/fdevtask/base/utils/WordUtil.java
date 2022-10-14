package com.spdb.fdev.fdevtask.base.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.util.Map;

public class WordUtil {
    private static Logger logger = LoggerFactory.getLogger(WordUtil.class);

    public static void createWord(Map map, String ftlFile, String filePath, FreeMarkerConfigurer freeMarkerConfig) throws IOException {
        Template template = freeMarkerConfig.getConfiguration().getTemplate(ftlFile);
        Writer w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(filePath), "utf-8");
            template.process(map, w);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("生成文档失败");
        } finally {
            if (w != null) w.close();
        }
    }
}

