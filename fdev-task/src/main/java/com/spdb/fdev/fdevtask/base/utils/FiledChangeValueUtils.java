package com.spdb.fdev.fdevtask.base.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spdb.fdev.fdevtask.base.annotation.BaseEnum;
import com.spdb.fdev.fdevtask.base.annotation.FieldEnumConverter;
import com.spdb.fdev.fdevtask.base.annotation.FiledTransConvert;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.enums.OperateTaskTypeEnum;
import com.spdb.fdev.fdevtask.spdb.entity.ChangeValueLog;
import com.spdb.fdev.fdevtask.spdb.service.DemandManageApi;
import com.spdb.fdev.fdevtask.spdb.service.Impl.DemandManageApiImpl;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;


/**
 * @author patrick
 * @date 2021/3/15 上午9:52
 * @Des bean对象操作工具
 * 最簡單的事是堅持，最難的事還是堅持
 */
public class FiledChangeValueUtils {



    // 缓存转换器，防止强引用，内存泄露
    @SuppressWarnings("all")
    private static Map<Class<? extends FieldEnumConverter>, FieldEnumConverter> converterCache =
            Collections.synchronizedMap(new WeakHashMap<>(8));

    /**
     * 计算两个对象(可不同类，比较规则:字段名字和类型一致)属性变化值，如有多个则返回列表
     * 默认归属更新属性操作
     *
     * @param newObj 新对象
     * @param old    旧对象
     * @return 对象值改变列表
     */
    public static List<ChangeValueLog> getFieldValueChangeRecords(Object newObj, Object old)  throws Exception{
        return getFieldValueChangeRecords(newObj, old, OperateTaskTypeEnum.UPDATE, null);
    }

    /**
     * 计算两个对象(可不同类，比较规则:字段名字和类型一致)属性变化值，如有多个则返回列表
     * 自定义操作类型
     *
     * @param newObj 新对象
     * @param old    旧对象
     * @param type   操作类型
     * @return 对象值改变列表
     */
    public static List<ChangeValueLog> getFieldValueChangeRecords(Object newObj, Object old, OperateTaskTypeEnum type)  throws Exception{
        return getFieldValueChangeRecords(newObj, old, type, null);
    }

    /**
     * 获取两个对象之间属性值改变记录列表（对象类型可不同，只要字段名和类型相同即可）
     *
     * @param newObj    新对象
     * @param old       旧对象
     * @param type      操作类型
     * @param predicate 字段过滤器
     * @return 对象值改变列表
     */
    public static List<ChangeValueLog> getFieldValueChangeRecords(Object newObj, Object old, OperateTaskTypeEnum type, Predicate<Field> predicate)  throws Exception{
        Class<?> beforeObjClass = newObj.getClass();
        List<ChangeValueLog> changeRecords = new ArrayList<>();
        Field[] fields = getFields(old.getClass());
        for (Field nf : fields) {
            if (predicate != null && predicate.test(nf)) {
                continue;
            }
            String fieldName = nf.getName();
            Field oldObjFiled = getField(beforeObjClass, fieldName, nf.getType());
            if (oldObjFiled == null) {
                continue;
            }
            Object afterValue = getFieldValue(nf, newObj);
            Object beforeValue = getFieldValue(oldObjFiled, old);
            if (!(CommonUtils.isNullOrEmpty(afterValue) && CommonUtils.isNullOrEmpty(beforeValue)) && !equals(beforeValue, afterValue)) {
                if (afterValue == null && nf.getName().equals(Dict.STATUS)){
                    continue;
                }
                if (afterValue == null){
                    afterValue = "";
                }
                afterValue = getDifferenceObject(afterValue, beforeValue);
                //枚举值转译
                FiledTransConvert convert = AnnotationUtils.getAnnotation(nf, FiledTransConvert.class);
                convert = convert == null ? AnnotationUtils.getAnnotation(oldObjFiled, FiledTransConvert.class) : convert;
                if (convert != null) {
                    afterValue = convertEnumValue(convert, afterValue);
                    beforeValue = convertEnumValue(convert, beforeValue);
                    // 判断属性名字是否需要转译
                    fieldName = StringUtils.hasLength(convert.rename()) ? convert.rename() : fieldName;
                }
                changeRecords.add(ChangeValueLog.builder().fieldName(fieldName).afterValue(afterValue).beforeValue(beforeValue).field(nf.getName()).type(type.getValue()+"").build());
            }
        }
        return changeRecords;
    }

    /**
     * 求两个对象之间的差集 例如:元素属于A而且不属于B
     *
     * @param afterValue  新值
     * @param beforeValue 老值
     * @return 差集
     */
    private static Object getDifferenceObject(Object afterValue, Object beforeValue) {
        if (afterValue instanceof List) {
            Set afterSet = Sets.newHashSet((List) afterValue);
            Set beforeSet = Sets.newHashSet(beforeValue == null ? Lists.newArrayList() : (List) beforeValue);
            Set set = Sets.difference(afterSet, beforeSet);
            return set;
        }
        return afterValue;
    }

    /**
     * 枚举值类型转换
     *
     * @param filedTransConvert 转换器
     * @param value             转换前
     * @return 转换后
     */
    private static Object convertEnumValue(FiledTransConvert filedTransConvert, Object value) {
        Class<? extends FieldEnumConverter> converterClazz = filedTransConvert.converter();
        if (converterClazz != FieldEnumConverter.class) {
            FieldEnumConverter fc = converterCache.get(converterClazz);
            if (fc == null) {
                fc = instantiate(converterClazz);
                converterCache.put(converterClazz, fc);
            }
            return fc.convert(value);
        }

        Class<? extends Enum> enumClass = filedTransConvert.enumConverter();
        if (enumClass != FiledTransConvert.DefaultEnum.class) {
            return getNameByValue((BaseEnum[]) enumClass.getEnumConstants(), value);
        }

        return value;
    }

    /**
     * 获取所有field字段，包含父类继承的
     *
     * @param clazz 字段所属类型
     * @return
     */
    public static Field[] getFields(Class<?> clazz) {
        return getFields(clazz, null);
    }

    /**
     * 获取所有field字段，包含父类继承的
     *
     * @param clazz       字段所属类型
     * @param fieldFilter 过滤器
     * @return
     */
    public static Field[] getFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        List<Field> fields = new ArrayList<>(64);
        while (Object.class != clazz && clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (fieldFilter != null && !fieldFilter.test(field)) {
                    continue;
                }
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    /**
     * 获取类的字段，包括父亲的
     *
     * @param clazz 字段所属类
     * @param name  类名
     * @param type  field类型
     * @return
     */
    public static Field getField(Class<?> clazz, String name, Class<?> type) {
        Assert.notNull(clazz, "clazz不能为空!");
        while (clazz != Object.class && clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if ((name == null || name.equals(field.getName())) &&
                        (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 获取字段值
     *
     * @param field  字段
     * @param target 字段所属实例对象
     * @return 字段值
     */
    public static Object getFieldValue(Field field, Object target) {
        ReflectionUtils.makeAccessible(field);
        try {
            return field.get(target);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("获取%s对象的%s字段值错误!", target.getClass().getName(), field.getName()), e);
        }
    }

    /**
     * 根据枚举值获取其对应的名字
     *
     * @param enums 枚举列表
     * @param value 枚举值
     * @return 枚举名称
     */
    public static <T> String getNameByValue(BaseEnum<T>[] enums, T value) {
        if (value == null) {
            return null;
        }
        for (BaseEnum<T> e : enums) {
            if (value.equals(e.getValue())) {
                return e.getName();
            }
        }
        return null;
    }


    /**
     * 实例化clazz对象
     *
     * @param clazz    目标clazz
     * @param initArgs 初始化参数
     * @param <T>      对象泛型类
     * @return 对象
     */
    public static <T> T instantiate(Class<T> clazz, Object... initArgs) {
        try {
            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
            return declaredConstructor.newInstance(initArgs);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("实例化%s对象失败", clazz.getName()), e);
        }
    }

    public static boolean equals(Object a,Object b){
        if (a instanceof String[] && b instanceof String[]){
            if (Arrays.equals((String[])a,(String[])b)){
                return true;
            }
        }
        if((a == b) || (a != null && a.equals(b))){
            return true;
        }
        return false;
    }
}
