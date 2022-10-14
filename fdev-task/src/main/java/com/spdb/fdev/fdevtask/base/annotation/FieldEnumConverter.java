package com.spdb.fdev.fdevtask.base.annotation;

/**
 * @author patrick
 * @date 2021/3/16 上午9:57
 * @Des 属性枚举类转换
 * 最簡單的事是堅持，最難的事還是堅持
 */
@FunctionalInterface
public interface FieldEnumConverter<T,R> {

    R convert(T filedValue);

}
