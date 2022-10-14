package com.spdb.fdev.fdevtask.base.annotation;

/**
 * @author patrick
 * @date 2021/3/15 下午3:42
 * @Des 基础enum
 * 最簡單的事是堅持，最難的事還是堅持
 */
public interface BaseEnum<T> {
    /**
     * 获取value
     *
     * @return 目标类型
     */
    T getValue();

    /**
     * 获取值
     *
     * @return 值
     */
    String getName();
}
