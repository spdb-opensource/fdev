package com.spdb.fdev.base.annotation.nonull;

import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.entity.RunnerInfo;
import com.spdb.fdev.pipeline.service.IRunnerInfoService;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @Author: c-jiangjk
 * @Date: 2021/4/2 15:22
 */
@Aspect
@Component
public class RunnerTokenAspect {

    private static Logger logger = LoggerFactory.getLogger(RunnerTokenAspect.class);
    @Autowired
    IRunnerInfoService runnerInfoService;

    @Pointcut("@annotation(com.spdb.fdev.base.annotation.nonull.RunnerToken)")
    public void controllerInterceptor() {
    }

    @Around("controllerInterceptor()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (sra == null){
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"请求为空"});
        }
        HttpServletResponse response = sra.getResponse();
        HttpServletRequest request = sra.getRequest();
        String token = request.getHeader("Runner-Token");
        if (token == null || StringUtils.isEmpty(token.trim())) {
            response.setStatus(403);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"token is empty"});
        }
        RunnerInfo runnerInfo = new RunnerInfo();
        runnerInfo.setToken(token);
        logger.info("token is {}", token);
        if(!runnerInfoService.checkRunner(runnerInfo)){
            response.setStatus(403);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"Runner-Token"});
        }
        return pjp.proceed();
    }

}
