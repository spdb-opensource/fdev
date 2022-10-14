//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spdb.common.aop.log;

import com.spdb.common.dict.Dict;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.common.util.IPUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * @author lizz
 */
@Component
@Aspect
@RefreshScope
public class LogAspect {
    private static Logger logger = LoggerFactory.getLogger(LogAspect.class);
    @Value("${req.res.data.log.switch:true}")
    private boolean logDataEnabled;

    private ThreadLocal transTimeThreadLocal = new ThreadLocal();

    @Pointcut("execution(* *(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping))")
    public void webController() {
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before("webController()")
    public void before(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String schedulerSeqNo = request.getHeader(Dict.MDCSCHEDULERSEQNO);
        MDC.put(Dict.MDCSCHEDULERSEQNO, schedulerSeqNo);
        Object[] args = joinPoint.getArgs();
        transTimeThreadLocal.set(System.currentTimeMillis());
        logger.info("**** trans begin: {}", ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getRequestURL());
        if (args != null && args.length != 0 && args[0] instanceof Map) {
            String host = IPUtils.getIpAddr(request);
            ((Map) args[0]).put(Dict.HOST, host);
            ((Map) args[0]).put(Dict.SCHEDULERSEQNO, schedulerSeqNo);
        }

        if (this.logDataEnabled) {
            try {
                if (args != null) {
                    logger.info("**** request data: {}", objectMapper.writeValueAsString(args));
                } else {
                    logger.info("**** request data: null");
                }
            } catch (JsonProcessingException e) {
                logger.warn("**** log request data error!");
            }
        }

    }

    @AfterReturning(pointcut = "webController()", returning = "returnData")
    public void after(JoinPoint joinPoint, Object returnData) {
        if (this.logDataEnabled) {
            try {
                if (returnData != null) {

                    logger.info("**** response data: {}", objectMapper.writeValueAsString(returnData));
                } else {
                    logger.info("**** response data: null");
                }
            } catch (JsonProcessingException e) {
                logger.warn("**** log response data error!");
            }
        }
        try {
            logger.info("**** trans end: {}  on time: {}ms", ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getRequest().getRequestURL(), (System.currentTimeMillis() - (long) transTimeThreadLocal.get()));
            transTimeThreadLocal.remove();
        } catch (Exception e) {
            logger.warn("**** log trans end error!");
        }
    }
}
