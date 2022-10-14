package com.spdb.fdev.fdevtask.base.annotation;


import com.spdb.fdev.fdevtask.base.enums.OperateTaskTypeEnum;

import java.lang.annotation.*;

/**
 * @author patrick
 * @date 2021/3/15 下午1:55
 * @Des 操作记录
 * 最簡單的事是堅持，最難的事還是堅持
 */
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateChangeLog {
    /**
     * 此方法操作记录描述，如果有值则覆盖默认OperateTaskTypeEnum中的descName
     */
    String value() default "";
    /**
     * 日志描述description,如果类和方法该值都为空，则不会有该项日志记录
     */
    OperateTaskTypeEnum type() default OperateTaskTypeEnum.UPDATE;

    /**
     * 如果是true，则为新建，需要在创建完成之后
     */
    boolean result() default false;

    /**
     * 记录时间，方便排查慢接口
     */
    boolean time() default true;
}
