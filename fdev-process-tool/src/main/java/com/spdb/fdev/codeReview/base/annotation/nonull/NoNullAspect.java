package com.spdb.fdev.codeReview.base.annotation.nonull;

import com.spdb.fdev.codeReview.base.dict.ErrorConstants;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xxx
 * @date 2019/5/10 13:44
 */
@Aspect
@Component
public class NoNullAspect {

    @Pointcut("@annotation(com.spdb.fdev.codeReview.base.annotation.nonull.NoNull)")
    public void controllerInterceptor() {
    }

    @Around("controllerInterceptor()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        MethodInvocationProceedingJoinPoint mjp = (MethodInvocationProceedingJoinPoint) pjp;
        MethodSignature signature = (MethodSignature) mjp.getSignature();
        Method method = signature.getMethod();
        NoNull noNull = method.getAnnotation(NoNull.class);
        String[] filedNames = noNull.require();
        Object parameter = null;
        for (Object pa : args) {
            if (pa.getClass() == noNull.parameter()) {
                parameter = pa;
            }
        }

        if (parameter == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"the annotation missing parameter"});
        }
        checkData(filedNames, parameter);
        return pjp.proceed();
    }

    private static void checkData(String[] filedNames, Object parameter) throws Exception {
        for (String filedName : filedNames) {
            checkData(filedName, parameter);
        }
    }

    private static void checkData(String filedName, Object parameter) throws Exception {
        int index = filedName.indexOf('.');
        if (index > 0) {
            String key = filedName.substring(0, index);
            String value = filedName.substring(index + 1);
            Object checkData = CommonUtils.returnObjByKey(parameter, key);
            checkData(value, checkData);
        } else {
            checkData(parameter, filedName);
        }
    }

    public static void checkData(Object parameter, String filedName) throws Exception {
        if (parameter == null)
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{filedName + "为空"});
        if (parameter instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) parameter;
            if (CommonUtils.isNullOrEmpty(map.get(filedName)))
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{filedName + "为空"});
        } else if (parameter instanceof List) {
            List<Object> list = (List<Object>) parameter;
            for (Object o : list) {
                checkData(o, filedName);
            }
        } else if (parameter instanceof Set) {
            Set<Object> set = (Set<Object>) parameter;
            for (Object o : set) {
                checkData(o, filedName);
            }
        } else if (parameter instanceof String && !StringUtils.isNotBlank((String) parameter)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{filedName + "为空"});
        } else if (parameter instanceof Integer && CommonUtils.isNullOrEmpty(parameter)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{filedName + "为空"});
        } else {
            Class<?> clazz = parameter.getClass();
            Field field = clazz.getDeclaredField(filedName);
            field.setAccessible(true);
            Object object = field.get(parameter);
            if (CommonUtils.isNullOrEmpty(object))
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{filedName + "为空"});
        }
    }
}
