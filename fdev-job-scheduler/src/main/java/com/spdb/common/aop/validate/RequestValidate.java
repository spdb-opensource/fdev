package com.spdb.common.aop.validate;

import java.lang.annotation.*;

/**
 * @author xxx
 * @date 下午2:28
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestValidate {

    String[] NotEmptyFields() default {};

    String[] NotBothEmptyFields() default {};
}
