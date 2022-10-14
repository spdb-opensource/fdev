package com.spdb.fdev.fdevenvconfig.base;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ICommonDao;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author xxx
 * @date 2019/7/5 14:16
 */
@Component
public class CommonValidate {

    private CommonValidate() {
    }

    public static void validateRepeatParamForUpdate(Object parm, String operator, String[] validateParms,
                                                    Class<?> clazz, ICommonDao commonDao) {
        Map<Object, Object> validateMap = new HashMap<>();
        if (parm instanceof Map) {
            validateMap = (Map) parm;
        } else {
            for (String validateParm : validateParms) {
                Object value;
                try {
                    value = CommonUtils.getGetMethod(parm, validateParm);
                    validateMap.put(validateParm, value);
                } catch (Exception ignored) {
                }
            }
        }
        List<?> commonQuery = commonDao.commonQuery(validateMap, operator, clazz);
        StringBuilder errorMsg = new StringBuilder();
        if (commonQuery != null && commonQuery.size() > 1) {
            Object object = commonQuery.get(0);
            for (String validateParm : validateParms) {
                try {
                    String existValue = (String) CommonUtils.getGetMethod(object, validateParm);
                    String nowValue = (String) CommonUtils.getGetMethod(parm, validateParm);
                    if (StringUtils.isNotBlank(existValue) && existValue.equals(nowValue)) {
                        errorMsg.append(CommonUtils.getFiledAnnotationVal(parm, validateParm, ApiModelProperty.class,
                                Dict.VALUE));
                        errorMsg.append("太火爆了,再换一个试试");
                    }
                } catch (Exception ignored) {
                }

            }
        } else if (commonQuery != null && commonQuery.size() == 1) {
            Object object = commonQuery.get(0);
            Object queryId = CommonUtils.getGetMethod(object, Constants.ID);
            Object id = CommonUtils.getGetMethod(parm, Constants.ID);
            if (!CommonUtils.isNullOrEmpty(queryId) && !queryId.equals(id)) {
                for (String validateParm : validateParms) {
                    try {
                        String existValue = (String) CommonUtils.getGetMethod(object, validateParm);
                        String nowValue = (String) CommonUtils.getGetMethod(parm, validateParm);
                        if (StringUtils.isNotBlank(existValue) && existValue.equals(nowValue)) {
                            errorMsg.append(CommonUtils.getFiledAnnotationVal(parm, validateParm, ApiModelProperty.class,
                                    Constants.VALUE));
                            errorMsg.append("太火爆了,再换一个试试");
                        }
                    } catch (Exception ignored) {
                    }

                }
            }
        }

        if (!"".equals(errorMsg.toString())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg.toString()});
        }
    }

    public static void validateRepeatParam(Object parm, String operator, String[] validateParms, Class<?> clazz,
                                           ICommonDao commonDao) throws Exception {
        Map<Object, Object> validateMap = new HashMap<>();
        if (parm instanceof Map) {
            Map parmMap = (Map) parm;
            for (String validateParm : validateParms) {
                validateMap.put(validateParm, parmMap.get(validateParm));
            }
        } else {
            for (String validateParm : validateParms) {
                Object value;
                try {
                    value = CommonUtils.getGetMethod(parm, validateParm);
                    validateMap.put(validateParm, value);
                } catch (Exception ignored) {
                }
            }
        }
        List<?> commonQuery = commonDao.commonQuery(validateMap, operator, clazz);
        StringBuilder errorMsg = new StringBuilder();
        if (!commonQuery.isEmpty()) {
            Object object = commonQuery.get(0);
            for (String validateParm : validateParms) {
                try {
                    String existValue = (String) CommonUtils.getGetMethod(object, validateParm);
                    String nowValue = (String) CommonUtils.getGetMethod(parm, validateParm);
                    if (StringUtils.isNotBlank(existValue) && existValue.equals(nowValue)) {
                        errorMsg.append(CommonUtils.getFiledAnnotationVal(parm, validateParm, ApiModelProperty.class,
                                Constants.VALUE));
                        errorMsg.append("太火爆了,再换一个试试");
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (!"".equals(errorMsg.toString())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg.toString()});
        }

    }

    /**
     * 对实体的env_key中的name_en进行检验
     *
     * @param parmList 实体env_key
     * @param key      name_en
     */
    public static void validateRepeatParam(List<Object> parmList, String key) {
        Set<String> set = new HashSet<>();
        for (Object o : parmList) {
            Map<String, String> map = (Map<String, String>) o;
            if (set.contains(map.get(key))) {
                throw new FdevException(ErrorConstants.PARAM_ERROR,
                        new String[]{"实体KEY中" + key + "值：" + map.get(key) + "重复"});
            } else {
                String value = map.get(key);
                if (Constants.NAME_EN.equals(key)) {
                    Boolean flag = Pattern.matches(Constants.PATTERN_MODEL_NAME_EN, value);
                    if (!flag) {
                        String errorMsg = "实体env_key中" + key + "只能是字母、数字、下划线";
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
                    }
                }
                set.add(map.get(key));
            }
        }
    }

    /**
     * 对实体模板envKey中的propKey进行检验(校验)
     * 对实体模板envKey中的所有属性进行去重。
     *
     * @param parmList 实体模板的envKey
     * @param key      propKey
     */
    public static void validateTemplateRepeatParam(List<Object> parmList, String key) {
        Set<String> set = new HashSet<>();
        for (Object o : parmList) {
            Map<String, String> map = (Map<String, String>) o;
            if (set.contains(map.get(key))) {
                throw new FdevException(ErrorConstants.PARAM_ERROR,
                        new String[]{"实体模板ENVKEY中" + key + "值：" + map.get(key) + "重复"});
            } else {
                String value = map.get(key);
                if (Dict.PROPKEY.equals(key)) {
                    Boolean flag = Pattern.matches(Constants.PATTERN_MODEL_TEMPLATE_NAME_EN, value);
                    if (!flag) {
                        String errorMsg = "实体模板envKey中" + key + "只能是字母、数字、下划线";
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
                    }
                }
                set.add(map.get(key));
            }
        }
    }


    public static void validateModelType(String type) {
        if (!"deploy".equals(type) && !"runtime".equals(type)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"实体类型不正确"});
        }

    }

    /**
     * 对实体组成英文名的字段值进行判断,并将字段值拼接一起返回
     *
     * @param parm          实体类
     * @param validateParms 需要校验的字段名称
     * @return 返回实体英文名
     */
    public static String validateRepeatParamPattern(Object parm, String[] validateParms) {
        StringBuilder nameEn = new StringBuilder();
        if (!(parm instanceof Map)) {
            for (String validateParm : validateParms) {
                Object value;
                try {

                    value = CommonUtils.getGetMethod(parm, validateParm);
                    String v = (String) value;
                    if (Constants.TYPE.equals(validateParm)) {
                        if (Constants.COMM.equals(value)) {
                            continue;
                        }
                    }
                    boolean flag;
                    String errorMsg;
                    if (Constants.SUFFIX_NAME.equals(validateParm)) {
                        flag = Pattern.matches(Constants.PATTERN_SUFFIX_NAME, v);
                        errorMsg = "只能是英文和数字和-";
                    } else {
                        flag = Pattern.matches(Constants.PATTERN_MODEL_FILTER, v);
                        errorMsg = "只能是英文和数字";
                    }

                    if (!flag) {
                        errorMsg = validateParm + errorMsg;
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
                    }
                    if (nameEn.length() == 0) {
                        nameEn.append(v);
                    } else {
                        nameEn.append("_").append(v);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return nameEn.toString();
    }

}