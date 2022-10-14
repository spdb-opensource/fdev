package com.spdb.fdev.common.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyInitProperty {
    //redis key ognl表达式
    String redisKeyExpression() default "";

    //redis过期时间
    long redisDuration() default 0;
}
