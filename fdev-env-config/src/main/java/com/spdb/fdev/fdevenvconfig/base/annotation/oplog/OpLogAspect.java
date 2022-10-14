package com.spdb.fdev.fdevenvconfig.base.annotation.oplog;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author xxx
 */
@Aspect
@Component
public class OpLogAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
	public UserVerifyUtil userVerifyUtil;


    @Pointcut("@annotation(com.spdb.fdev.fdevenvconfig.base.annotation.oplog.OpLog)")
    public void controllerInterceptor() {
    }

    @Around("controllerInterceptor()")
    public void around(ProceedingJoinPoint pjp) throws Exception {
        MethodInvocationProceedingJoinPoint mjp = (MethodInvocationProceedingJoinPoint) pjp;
        MethodSignature signature = (MethodSignature) mjp.getSignature();
        Method method = signature.getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);
        String filedName = opLog.controllerName();
        User user = userVerifyUtil.getRedisUser();
        logger.info("@@@@@@ 操作员 {} 执行 {}", user.getUser_name_cn(), filedName);
    }
}