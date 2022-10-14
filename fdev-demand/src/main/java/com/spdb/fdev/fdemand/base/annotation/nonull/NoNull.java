package com.spdb.fdev.fdemand.base.annotation.nonull;

import java.lang.annotation.*;

/**
 * 判断实体类一些属性是否为空(对应每个字段都要有值)
 * <p>
 * 目前支持的实体类类型 有如下
 * String, Integer, List, Map, List<Map>, HashSet
 * List<Map<String, List<Map<String, List<Map>>>>>
 *
 * @author xxx
 * @date 2019/5/10 13:44
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface NoNull {
    String[] require() default {};

    Class<?> parameter() default Object.class;
}
