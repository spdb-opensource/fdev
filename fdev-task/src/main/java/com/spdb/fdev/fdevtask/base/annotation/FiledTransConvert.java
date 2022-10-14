package com.spdb.fdev.fdevtask.base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author patrick
 * @date 2021/3/15 下午3:32
 * @Des 字段描述转换
 * 最簡單的事是堅持，最難的事還是堅持
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface FiledTransConvert {

    String rename() default "";

    /**
     * 枚举值必须集成BaseEnum接口，获取属性枚举值转换
     */
    Class<? extends Enum> enumConverter() default DefaultEnum.class;

    /**
     * 值转换器
     */
    Class<? extends FieldEnumConverter> converter() default FieldEnumConverter.class;

    enum DefaultEnum implements BaseEnum<Integer> {
        ;

        @Override
        public Integer getValue() {
            return 0;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
