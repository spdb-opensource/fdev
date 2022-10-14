package com.spdb.fdev.fdevenvconfig.base.utils;

import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

public class WordUtils {
    private static Logger logger = LoggerFactory.getLogger(WordUtils.class);

    public static void exportMillCertificateWord(Map map, String ftlFile, String filePath, FreeMarkerConfigurer freeMarkerConfig) throws IOException {
        Template template = freeMarkerConfig.getConfiguration().getTemplate(ftlFile);
        Writer w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(filePath), "utf-8");
            template.process(map, w);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("生成本地邮件附加失败" + e.getMessage());
        } finally {
            if (w != null) w.close();
        }
    }
}
