package com.spdb.fdev.common.validate;

import java.lang.annotation.*;

/**
 * Created by xxx on 下午2:28.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestValidate {

    String[] NotEmptyFields() default {};

    String[] NotBothEmptyFields() default {};
}
