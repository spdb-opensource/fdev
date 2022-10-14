package com.spdb.fdev.common.util;

import com.spdb.fdev.common.exception.FdevException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class ErrorMessageUtil {

    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

    ErrorMessageUtil(){
        messageSource.setCacheSeconds(-1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setBasenames("/config/errmsg");
        LocaleContextHolder.setDefaultLocale(Locale.CHINA);
    }

    public String get(String msgKey) {

        try {
            Locale locale = LocaleContextHolder.getLocale();
            if(Util.isNullOrEmpty(locale)){
                locale = Locale.CHINA;
            }
            return messageSource.getMessage(msgKey, null, locale);
        } catch (Exception e) {
            return msgKey;
        }
    }

    public String get(FdevException fdx) {

        try {
//            LocaleContextHolder.setDefaultLocale(Locale.CHINA);
//            String errorMessage =  messageSource.getMessage(fdx.getCode(),
//                    fdx.getArgs(), LocaleContextHolder.getLocale());
            //only use locale zh_CN
            String errorMessage =  messageSource.getMessage(fdx.getCode(),
                    fdx.getArgs(), Locale.CHINA);
            if(errorMessage != null){
                errorMessage = errorMessage.replaceAll("\\{\\d+\\}", "");
            }
            return errorMessage;
        } catch (Exception e) {
            return fdx.getCode();
        }
    }
}
