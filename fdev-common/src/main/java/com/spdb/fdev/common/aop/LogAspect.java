//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spdb.fdev.common.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.dict.Dict;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Random;

@Component
@Aspect
public class LogAspect {
    private static Logger logger = LoggerFactory.getLogger(LogAspect.class);
    @Value("${req.data.log.switch:true}")
    private boolean logReqDataEnabled;

    @Value("${res.data.log.switch:false}")
    private boolean logRspDataEnabled;
    
    @Value("${req.long.message.log.switch:true}")
    private boolean logLongMessageEnabled;

    private static final String LOG_REQUEST_BEGIN_FORMAT = "**** trans begin: {} , random: {}";
    private static final String LOG_REQUEST_DATA_FORMAT = "**** request data: {} , random: {}";
    private static final String LOG_RESPONSE_DATA_FORMAT = "**** response data: {} , random: {}";
    private static final String LOG_REQUEST_END_FORMAT = "**** trans end: {} on time: {}ms , random: {}";

    private ThreadLocal transTimeThreadLocal = new ThreadLocal();

    private ThreadLocal transRandomThreadLocal = new ThreadLocal();

    @Pointcut("execution(* *(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping))")
    public void webController() {
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before("webController()")
    public void before(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        transTimeThreadLocal.set(System.currentTimeMillis());
        String random = new Random().ints().toString();
        transRandomThreadLocal.set(System.currentTimeMillis());

        logger.info(LOG_REQUEST_BEGIN_FORMAT, ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest().getRequestURL(), transRandomThreadLocal.get());

        if (this.logReqDataEnabled) {
        	String long_message= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest().getHeader(Dict.LONG_MESSAGE);
        	//长报文打印开关打开  且请求头longmessage字段为ture则打印日志
        	if (!this.logLongMessageEnabled&&
        			!StringUtils.isBlank(long_message)
        					&&Dict.TRUE.equalsIgnoreCase(long_message)) {
        		return;
        	}
        	
            try {
                if (args != null) {
                    logger.info(LOG_REQUEST_DATA_FORMAT,  objectMapper.writeValueAsString(args),
                            transRandomThreadLocal.get());
                } else {
                    logger.info(LOG_REQUEST_DATA_FORMAT, null, transRandomThreadLocal.get());
                }
            } catch (JsonProcessingException e) {
                logger.warn("**** log request data error!");
            }       	
        }

    }

    @AfterReturning(
            pointcut = "webController()",
            returning = "returnData"
    )
    public void after(JoinPoint joinPoint, Object returnData) {
        if (this.logRspDataEnabled) {
            try {
                if (returnData != null){
                    logger.info(LOG_RESPONSE_DATA_FORMAT, objectMapper.writeValueAsString(returnData),
                            transRandomThreadLocal.get());
                } else {
                    logger.info(LOG_RESPONSE_DATA_FORMAT, null, transRandomThreadLocal.get());
                }
            } catch (JsonProcessingException e) {
                logger.warn("**** log response data error!");
            }
        }
        try {
            logger.info(LOG_REQUEST_END_FORMAT,
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURL(),
                    (System.currentTimeMillis() - (long) transTimeThreadLocal.get()),
                    transRandomThreadLocal.get());
            transTimeThreadLocal.remove();
            transRandomThreadLocal.remove();
        }catch (Exception e){
            logger.warn("**** log trans end error!");
        }
    }
}
