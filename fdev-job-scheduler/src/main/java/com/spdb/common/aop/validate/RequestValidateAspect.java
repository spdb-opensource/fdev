package com.spdb.common.aop.validate;

import com.spdb.common.dict.Constants;
import com.spdb.common.exception.CustomException;
import com.spdb.common.util.Util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xxx
 * @date 2018/4/8
 */
@Aspect
@Component
public class RequestValidateAspect {
    private static Logger logger = LoggerFactory.getLogger(RequestValidateAspect.class);

    @Pointcut("execution(* *(..)) && @annotation(requestValidate)")
    public void validateFields(RequestValidate requestValidate) {
    }

    @Around("validateFields(requestValidate)")
    public Object validateFieldsProcess(ProceedingJoinPoint joinPoint, RequestValidate requestValidate) throws Throwable {
        //获取切面传入参数列表
        Object[] args = joinPoint.getArgs();
        String[] notEmptyFields = requestValidate.NotEmptyFields();
        String[] notBothEmptyFields = requestValidate.NotBothEmptyFields();
        if (args != null && args.length != 0) {
            Object request = args[0];
            if (request instanceof Map) {
                Map requestMap = (Map) request;
                for (String notEmptyField : notEmptyFields) {
                    if (Util.isNullOrEmpty(requestMap.get(notEmptyField))) {
                        throw new CustomException(Constants.PARAM_CANNOT_BE_EMPTY, new String[]{notEmptyField});
                    }
                }
                if (notBothEmptyFields != null && notBothEmptyFields.length != 0) {
                    boolean bothEmpty = true;
                    StringBuilder notBothEmptyFieldsName = new StringBuilder();
                    for (String notBothEmptyField : notBothEmptyFields) {
                        bothEmpty = bothEmpty && Util.isNullOrEmpty(requestMap.get(notBothEmptyField));
                        notBothEmptyFieldsName.append("|").append(notBothEmptyField);
                    }
                    if (bothEmpty) {
                        throw new CustomException(Constants.PARAM_CANNOT_BE_BOTH_EMPTY,
                                new String[]{notBothEmptyFieldsName.append("|").toString()});
                    }
                }
            }
        }
        Object object = joinPoint.proceed();
        return object;
    }
}