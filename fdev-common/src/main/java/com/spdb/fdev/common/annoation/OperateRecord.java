package com.spdb.fdev.common.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description：
 * author Hubery
 * date 2020-07-22
 * version v0.0.1
 * since v0.0.1
 **/
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateRecord {

    String operateDiscribe() default "";
}
