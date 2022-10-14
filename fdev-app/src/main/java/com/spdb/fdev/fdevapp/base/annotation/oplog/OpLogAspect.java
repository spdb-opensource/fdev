package com.spdb.fdev.fdevapp.base.annotation.oplog;

import java.lang.reflect.Method;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.spdb.fdev.common.User;

/**
 * 
 * @author xxx
 *
 */
@Aspect
@Component
public class OpLogAspect {
	 private Logger logger = LoggerFactory.getLogger(this.getClass());
	 @Pointcut("@annotation(com.spdb.fdev.fdevapp.base.annotation.oplog.OpLog)")
	    public void controllerInterceptor() {
	    }
	 @Around("controllerInterceptor()")
	 public void around(ProceedingJoinPoint pjp) throws Throwable  {
	        Object[] args = pjp.getArgs();
	        MethodInvocationProceedingJoinPoint mjp = (MethodInvocationProceedingJoinPoint) pjp;
	        MethodSignature signature = (MethodSignature) mjp.getSignature();
	        Method method = signature.getMethod();
	        OpLog opLog = method.getAnnotation(OpLog.class);
	        String filedName = opLog.controllerName();
	        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getSession().getAttribute(Dict._USER);
	        logger.info("@@@@@@ 操作员"+user.getUser_name_cn()+"执行"+filedName);
}
}