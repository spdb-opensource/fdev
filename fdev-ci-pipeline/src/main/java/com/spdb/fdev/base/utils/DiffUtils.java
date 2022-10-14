package com.spdb.fdev.base.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.entity.Pipeline;
import com.spdb.fdev.pipeline.entity.PipelineUpdateDiff;
import com.spdb.fdev.pipeline.entity.Stage;
import com.spdb.fdev.pipeline.service.IPipelineService;
import com.spdb.fdev.pipeline.service.impl.PipelineServiceImpl;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class DiffUtils {


    private static final Logger logger = LoggerFactory.getLogger(DiffUtils.class);

    private static List filterPipelineFields = null;

    static {
        //pipeline的过滤字段列表
        String[] PipelineFields = new String[]{"id", "nameId", "version", "createTime", "updateTime", "status", "author", "collected", "pipelineTemplateId", "pipelineTemplateNameId", "buildTime", "nameCn", "gitlabProjectId", "desc", "visibleRange", "fixedModeFlag"};
        filterPipelineFields = new ArrayList(Arrays.asList(PipelineFields));
    }

    /**
     * 获取更新流水线的变更（不同）信息，参考字段有：
     * <p>
     * 修改触发规则
     * <p>
     * 修改绑定应用
     * <p>
     * 修改流水线名称
     * <p>
     * 新增步骤：www-eeee-eee
     * <p>
     * 删除步骤：eee-rrr-rrrr
     * <p>
     * 修改步骤：222-eee-rrr
     * <p>
     * 修改stage名称
     * <p>
     * 修改job名称
     *
     * 生成diffEntity对象
     *
     *
     *
     * @param oldPipeline
     * @param newPipeline
     * @return
     */
    public static PipelineUpdateDiff getHandlerPipelineDiff(Pipeline oldPipeline, Pipeline newPipeline) throws NoSuchFieldException, IllegalAccessException, JsonProcessingException, InstantiationException {
        PipelineUpdateDiff diffEntity = new PipelineUpdateDiff();
        diffEntity.setSourcePipelineId(oldPipeline.getId());
        diffEntity.setTargetPipelineId(newPipeline.getId());
        diffEntity.setPipelineNameId(oldPipeline.getNameId());
        diffEntity.setId(new ObjectId().toString());
        List<Map> diffListMap = new ArrayList<>();
        //比较不同，并将变更记录到diffListMap中
        try {
            diff2EntityField(diffListMap, oldPipeline, newPipeline, Pipeline.class, 0);
        } catch (Exception e) {
            logger.error(" diff2EntityField error " + e.getMessage());
        }
        logger.info("比较出的变更记录为 " + JSONObject.toJSONString(diffListMap));
        //将比较出来的不同，按照fieldsMapping文件翻译成中文
        List<Map> diffList = handlerDiffENToCNByFieldsMappingYml(diffListMap);
//        diffListMap.get()
        diffEntity.setDiff(diffList);
        //名称
        return diffEntity;
    }


    /**
     * 比较两个对象的不同，将不同的信息记录到result中，结构如下
     *  {
     *             "diffData" : {
     *                 "value2" : "cast",
     *                 "value1" : "C++",
     *                 "key" : "Pipeline.name"
     *             },
     *             "diffInfo" : "C++ -> cast",
     *             "diffLabel" : "修改了 [Pipeline.name]"
     *         }
     * }
     * 需要将label中的字段翻译成中文返回给前端，故加上[*****]来好匹配
     *
     *
     *
     *
     *
     * @param result   比较不同的结果
     * @param obj1     比较对象
     * @param obj2     被比较对象
     * @param clazz    对象的类
     * @param subIndex 递归调用对象属性的深度，初始需要传0
     */
    public static void diff2EntityField(List result, Object obj1, Object obj2, Class<?> clazz, int subIndex) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        //获取entity包
        String fullClazzName = clazz.getName();
        String simpleName = clazz.getSimpleName();
        fullClazzName = fullClazzName.replace("." + simpleName, "");
        //对比class的type
        if (obj1 == null && obj2 == null) {
            logger.info("当前obj1 和 obj2 是 null, class:" + clazz.getSimpleName());
            return;
        }
        if (obj1 == null) {
            Map valueMap1 = new HashMap();
            valueMap1.put("value1", null);
            valueMap1.put("value2", obj2);
            String label = new String();
            String info = new String();
            try {
                Field nameField = clazz.getDeclaredField(Dict.NAME);
                nameField.setAccessible(true);
                Object nameValue = nameField.get(obj2);
                label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                info = nameValue.toString();
                Map diffMap = new HashMap();
                diffMap.put(Dict.DIFFLABEL, label);
                diffMap.put(Dict.DIFFINFO, info);
                diffMap.put(Dict.DIFFDATA, valueMap1);
                result.add(diffMap);
                return;
            } catch (NoSuchFieldException e) {
                label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                info = "";
            }
            if (obj2 instanceof String) {
                info = JSONObject.toJSONString(obj1) + " -> " + obj2.toString();
            } else {
                info = JSONObject.toJSONString(obj1) + " -> " + obj2.getClass().getSimpleName();
            }
            if (subIndex != 0 && !obj2.getClass().getGenericSuperclass().getClass().getSimpleName().equals(Class.class.getSimpleName())) {
                valueMap1.put(Dict.KEY, obj2.getClass().getGenericSuperclass().getClass().getSimpleName() + "." + obj2.getClass().getSimpleName());
            } else {
                valueMap1.put(Dict.KEY, obj2.getClass().getSimpleName());
            }
            Map diffMap = new HashMap();
            diffMap.put(Dict.DIFFLABEL, label);
            diffMap.put(Dict.DIFFINFO, info);
            diffMap.put(Dict.DIFFDATA, valueMap1);
            result.add(diffMap);
        } else if (obj2 == null) {
            Map valueMap1 = new HashMap();
            valueMap1.put("value1", obj1);
            valueMap1.put("value2", null);
            String label = new String();
            String info = new String();
            try {
                Field nameField = clazz.getDeclaredField(Dict.NAME);
                nameField.setAccessible(true);
                Object nameValue = nameField.get(obj1);
                label = "删除了 " + Dict.BRACKET_LEFT + obj1.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                info = nameValue.toString();
                Map diffMap = new HashMap();
                diffMap.put(Dict.DIFFLABEL, label);
                diffMap.put(Dict.DIFFINFO, info);
                diffMap.put(Dict.DIFFDATA, valueMap1);
                result.add(diffMap);
                return;
            } catch (NoSuchFieldException e) {
                label = "删除了 " + Dict.BRACKET_LEFT + obj1.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                info = "";
            }
            if (obj1 instanceof String) {
                info = obj1.toString() + " -> " + JSONObject.toJSONString(obj2);
            } else {
                info = obj1.getClass().getSimpleName() + " -> " + JSONObject.toJSONString(obj2);
            }
            if (subIndex != 0 && !obj1.getClass().getGenericSuperclass().getClass().getSimpleName().equals(Class.class.getSimpleName())) {
                valueMap1.put(Dict.KEY, obj1.getClass().getGenericSuperclass().getClass().getSimpleName() + "." + obj1.getClass().getSimpleName());
            } else {
                valueMap1.put(Dict.KEY, obj1.getClass().getSimpleName());
            }
            Map diffMap = new HashMap();
            diffMap.put(Dict.DIFFLABEL, label);
            diffMap.put(Dict.DIFFINFO, info);
            diffMap.put(Dict.DIFFDATA, valueMap1);
            result.add(diffMap);
        } else {
            if (Map.class.isAssignableFrom(obj1.getClass())) {
                //如果obj为Map，该情况只有List<Map>进来
                if (obj1.equals(obj2))
                    return;
                String mapValue1 = (String) ((Map) obj1).get(Dict.VALUE);
                String mapValue2 = (String) ((Map) obj2).get(Dict.VALUE);
                if (CommonUtils.isNullOrEmpty(mapValue1) && CommonUtils.isNullOrEmpty(mapValue2)) {
                    return;
                }
                if (!CommonUtils.isNullOrEmpty(mapValue1)) {
                    if (!mapValue1.equals(mapValue2)) {
                        //减少了一个值
                        Map valueMap1 = new HashMap();
                        valueMap1.put("value1", mapValue1);
                        valueMap1.put("value2", mapValue2);
                        valueMap1.put(Dict.KEY, ((Map) obj1).get(Dict.KEY) + " : " + ((Map) obj2).get(Dict.KEY));
                        String label = new String();
                        String info = new String();
                        label = "修改了 " + Dict.BRACKET_LEFT + "params.key" + Dict.BRACKET_RIGHT;
                        info = ((Map) obj1).get(Dict.KEY) + " : " + mapValue1 + " -> " + mapValue2;
                        Map diffMap = new HashMap();
                        diffMap.put(Dict.DIFFLABEL, label);
                        diffMap.put(Dict.DIFFINFO, info);
                        diffMap.put(Dict.DIFFDATA, valueMap1);
                        result.add(diffMap);
                        return;
                    }else if (mapValue1.equals(mapValue2)) {
                        return;
                    }
                }else if (!CommonUtils.isNullOrEmpty(mapValue2)) {
                    if (!mapValue2.equals(mapValue1)) {
                        //减少了一个值
                        Map valueMap1 = new HashMap();
                        valueMap1.put("value1", mapValue1);
                        valueMap1.put("value2", mapValue2);
                        valueMap1.put(Dict.KEY, ((Map) obj1).get(Dict.KEY) + " : " + ((Map) obj2).get(Dict.KEY));
                        String label = new String();
                        String info = new String();
                        label = "修改了 " + Dict.BRACKET_LEFT + "params.key" + Dict.BRACKET_RIGHT;
                        info = ((Map) obj1).get(Dict.KEY) + " : " + mapValue1 + " -> " + mapValue2;
                        Map diffMap = new HashMap();
                        diffMap.put(Dict.DIFFLABEL, label);
                        diffMap.put(Dict.DIFFINFO, info);
                        diffMap.put(Dict.DIFFDATA, valueMap1);
                        result.add(diffMap);
                        return;
                    }else if (mapValue2.equals(mapValue1)) {
                        return;
                    }
                }
            }
            for (Field declaredField2 : obj2.getClass().getDeclaredFields()) {
                declaredField2.setAccessible(true);
                String fieldName2 = declaredField2.getName();
                if (filterPipelineFields.contains(fieldName2)) {
                    //过滤字段
                    continue;
                }
                Field declaredField1 = obj1.getClass().getDeclaredField(fieldName2);
                declaredField1.setAccessible(true);
                //对比字段的type
                if (declaredField2.getGenericType().getTypeName().equals(declaredField1.getGenericType().getTypeName())) {
                    //获取entity各自的value对比
                    Object value2 = declaredField2.get(obj2);
                    Object value1 = declaredField1.get(obj1);
                    Map valueMap1 = new HashMap();
                    valueMap1.put(Dict.KEY, obj1.getClass().getSimpleName() + "." + declaredField1.getName());
                    valueMap1.put("value1", value1);
                    valueMap1.put("value2", value2);

                    String label = new String();
                    String info = new String();
                    if ((value2 != null && value1 != null) && value2.equals(value1)) {
                        //一致
                    } else if (value1 != null && !value1.equals(value2)) {
                        if (List.class.isAssignableFrom(value1.getClass())) {
                            //如果它是list 要处理List<自定义类型>
                            List value1List = null;
                            List value2List = null;
                            int list1Size = 0;
                            int list2Size = 0;
                            try {
                                value1List = (List) value1;
                                value2List = (List) value2;
                                list1Size = value1List.size();
                                list2Size = value2List.size();
                            } catch (Exception e) {
                                logger.info(JSONObject.toJSONString(value1) + " -> " + JSONObject.toJSONString(value2));
                                for (Object item : value1List) {
                                    Map diffMap = new HashMap();
                                    try {
                                        Field declaredField = item.getClass().getDeclaredField(Dict.NAME);
                                        declaredField.setAccessible(true);
                                        Object itemValue = declaredField.get(item);
                                        label = "删除了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                        info = itemValue.toString();
                                    } catch (NoSuchFieldException e1) {
                                        label = "删除了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                        info = "";
                                    }

                                    diffMap.put(Dict.DIFFLABEL, label);
                                    diffMap.put(Dict.DIFFINFO, info);
                                    diffMap.put(Dict.DIFFDATA, valueMap1);
                                    result.add(diffMap);
                                    continue;
                                }

                            }
                            if (list2Size > list1Size) {
                                //增加
                                for (int i = 0; i < list2Size; i++) {
                                    Object item2 = value2List.get(i);
                                    Object item1 = null;
                                    try {
                                        item1 = value1List.get(i);
                                    } catch (Exception e) {
                                        logger.info(obj1 + " 下的 " + value1.getClass().getSimpleName() + " 的下标 " + i + " 不存在,为增加");
                                        Map diffMap = new HashMap();
                                        try {
                                            Field declaredField = item2.getClass().getDeclaredField(Dict.NAME);
                                            declaredField.setAccessible(true);
                                            Object itemValue = declaredField.get(item2);
                                            label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item2.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = itemValue.toString();
                                        } catch (NoSuchFieldException e1) {
                                            label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item2.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = "";
                                        }
                                        diffMap.put(Dict.DIFFLABEL, label);
                                        diffMap.put(Dict.DIFFINFO, info);
                                        diffMap.put(Dict.DIFFDATA, valueMap1);
                                        result.add(diffMap);
                                        continue;
                                    }
                                    //递归调用
                                    subIndex++;
                                    diff2EntityField(result, value1List.get(i), value2List.get(i), value1List.get(i).getClass(), subIndex);
                                    subIndex--;
                                }
                            } else if (list2Size < list1Size) {
                                //删除
                                //增加
                                for (int i = 0; i < list1Size; i++) {
                                    Object item1 = value1List.get(i);
                                    Object item2 = null;
                                    try {
                                        item2 = value2List.get(i);
                                    } catch (Exception e) {
                                        logger.info(obj2 + " 下的 " + value2.getClass().getSimpleName() + " 的下标 " + i + " 不存在,为删除");
                                        Map diffMap = new HashMap();
                                        try {
                                            Field declaredField = item1.getClass().getDeclaredField(Dict.NAME);
                                            declaredField.setAccessible(true);
                                            Object itemValue = declaredField.get(item1);
                                            label = "删除了 " + Dict.BRACKET_LEFT + obj1.getClass().getSimpleName() + "." + item1.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = itemValue.toString();
                                        } catch (NoSuchFieldException e1) {
                                            label = "删除了 " + Dict.BRACKET_LEFT + obj1.getClass().getSimpleName() + "." + item1.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = "";
                                        }
                                        diffMap.put(Dict.DIFFLABEL, label);
                                        diffMap.put(Dict.DIFFINFO, info);
                                        diffMap.put(Dict.DIFFDATA, valueMap1);
                                        result.add(diffMap);
                                        continue;
                                    }
                                    //递归调用
                                    subIndex++;
                                    diff2EntityField(result, value1List.get(i), value2List.get(i), value1List.get(i).getClass(), subIndex);
                                    subIndex--;
                                }

                            } else if (list1Size == list2Size) {
                                //修改或是list里面的字段进行了增删改
                                for (int i = 0; i < list1Size; i++) {
                                    //递归调用
                                    subIndex++;
                                    diff2EntityField(result, value1List.get(i), value2List.get(i), value1List.get(i).getClass(), subIndex);
                                    subIndex--;
                                }
                            }

                        } else if (value1.getClass().getName().contains(fullClazzName)) {
                            subIndex++;
                            diff2EntityField(result, value1, value2, value1.getClass(), subIndex);
                            subIndex--;
                        } else {
                            label = "修改了 " + Dict.BRACKET_LEFT + obj1.getClass().getSimpleName() + "." + declaredField1.getName() + Dict.BRACKET_RIGHT;
                            info = value1 + " -> " + value2;
                            Map diffMap = new HashMap();
                            diffMap.put(Dict.DIFFLABEL, label);
                            diffMap.put(Dict.DIFFINFO, info);
                            diffMap.put(Dict.DIFFDATA, valueMap1);
                            result.add(diffMap);
                        }
                    } else if (value2 != null && !value2.equals(value1)) {
                        if (List.class.isAssignableFrom(value2.getClass())) {
                            //如果它是list 要处理List<自定义类型>
                            List value1List = null;
                            List value2List = null;
                            int list1Size = 0;
                            int list2Size = 0;
                            try {
                                value1List = (List) value1;
                                value2List = (List) value2;

                                list1Size = value1List.size();
                                list2Size = value2List.size();
                            } catch (Exception e) {
                                logger.info(JSONObject.toJSONString(value1) + " -> " + JSONObject.toJSONString(value2));
                                for (Object item : value2List) {
                                    Map diffMap = new HashMap();
                                    try {
                                        Field declaredField = item.getClass().getDeclaredField(Dict.NAME);
                                        declaredField.setAccessible(true);
                                        Object itemValue = declaredField.get(item);
                                        label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                        info = itemValue.toString();
                                    } catch (NoSuchFieldException e1) {
                                        label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                        info = "";
                                    }
                                    diffMap.put(Dict.DIFFLABEL, label);
                                    diffMap.put(Dict.DIFFINFO, info);
                                    diffMap.put(Dict.DIFFDATA, valueMap1);
                                    result.add(diffMap);
                                }
                            }
                            if (list2Size > list1Size) {
                                //增加
                                for (int i = 0; i < list2Size; i++) {
                                    Object item2 = value2List.get(i);
                                    Object item1 = null;
                                    try {
                                        item1 = value1List.get(i);
                                    } catch (Exception e) {
                                        logger.info(obj1 + " 下的 " + value1.getClass().getSimpleName() + " 的下标 " + i + " 不存在,为增加");
                                        Map diffMap = new HashMap();
                                        try {
                                            //获取增加的name
                                            Field declaredField = item2.getClass().getDeclaredField(Dict.NAME);
                                            declaredField.setAccessible(true);
                                            Object itemValue = declaredField.get(item2);
                                            label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item2.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = itemValue.toString();
                                        } catch (NoSuchFieldException e1) {
                                            label = "增加了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item2.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = "";
                                        }
                                        diffMap.put(Dict.DIFFLABEL, label);
                                        diffMap.put(Dict.DIFFINFO, info);
                                        diffMap.put(Dict.DIFFDATA, valueMap1);
                                        result.add(diffMap);
                                        continue;
                                    }
                                    //递归调用
                                    subIndex++;
                                    diff2EntityField(result, value1List.get(i), value2List.get(i), value1List.get(i).getClass(), subIndex);
                                    subIndex--;
                                }
                            } else if (list2Size < list1Size) {
                                //删除
                                //增加
                                for (int i = 0; i < list1Size; i++) {
                                    Object item1 = value1List.get(i);
                                    Object item2 = null;
                                    try {
                                        item2 = value2List.get(i);
                                    } catch (Exception e) {
                                        logger.info(obj2 + " 下的 " + value2.getClass().getSimpleName() + " 的下标 " + i + " 不存在,为增加");
                                        Map diffMap = new HashMap();
                                        try {
                                            Field declaredField = item1.getClass().getDeclaredField(Dict.NAME);
                                            declaredField.setAccessible(true);
                                            Object itemValue = declaredField.get(item1);
                                            label = "删除了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item1.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = itemValue.toString();
                                        } catch (NoSuchFieldException e1) {
                                            label = "删除了 " + Dict.BRACKET_LEFT + obj2.getClass().getSimpleName() + "." + item1.getClass().getSimpleName() + Dict.BRACKET_RIGHT;
                                            info = "";
                                        }

                                        diffMap.put(Dict.DIFFLABEL, label);
                                        diffMap.put(Dict.DIFFINFO, info);
                                        diffMap.put(Dict.DIFFDATA, valueMap1);
                                        result.add(diffMap);
                                        continue;
                                    }
                                    //递归调用
                                    subIndex++;
                                    diff2EntityField(result, value1List.get(i), value2List.get(i), value1List.get(i).getClass(), subIndex);
                                    subIndex--;
                                }
                            } else if (list1Size == list2Size) {
                                //修改或是list里面的字段进行了增删改
                                for (int i = 0; i < list1Size; i++) {
                                    //递归调用
                                    subIndex++;
                                    diff2EntityField(result, value1List.get(i), value2List.get(i), value1List.get(i).getClass(), subIndex);
                                    subIndex--;
                                }
                            }

                        } else if (value2.getClass().getName().contains(fullClazzName)) {
                            subIndex++;
                            diff2EntityField(result, value1, value2, value2.getClass(), subIndex);
                            subIndex--;
                        } else {
                            label = "修改了 " + Dict.BRACKET_LEFT + obj1.getClass().getSimpleName() + "." + declaredField1.getName() + Dict.BRACKET_RIGHT;
                            info = value1 + " -> " + value2;
                            Map diffMap = new HashMap();
                            diffMap.put(Dict.DIFFLABEL, label);
                            diffMap.put(Dict.DIFFINFO, info);
                            diffMap.put(Dict.DIFFDATA, valueMap1);
                            result.add(diffMap);
                            continue;
                        }
                    }
                } else {
                    throw new FdevException(ErrorConstants.PARAMS_IS_ILLEGAL, new String[]{"entity field type is not same!"});
                }
            }
        }
    }

    /**
     *
     * 将result中的[****]中的字段 按照fieldsMappingCN.yaml 翻译成中文，若不存在即不将其返回
     *
     * @param result
     * @return newResultList 翻译后的结果
     * @throws JsonProcessingException
     */
    public static List<Map> handlerDiffENToCNByFieldsMappingYml(List result) throws JsonProcessingException {
        Yaml fieldsMapping = new Yaml();
        List newResultList = new ArrayList();
        try (InputStream in = DiffUtils.class.getClassLoader().getResourceAsStream(Dict.FIELDSMAPPINGCN_YML)) {
            HashMap yamlMap = fieldsMapping.loadAs(in, HashMap.class);
            for (int i = 0; i < result.size(); i++) {
                Map resultMap = (Map) result.get(i);
                String diffInfo = (String) resultMap.get(Dict.DIFFINFO);
                String diffLabel = (String) resultMap.get(Dict.DIFFLABEL);
                Object diffData = resultMap.get(Dict.DIFFDATA);
                int leftIndex = diffLabel.indexOf(Dict.BRACKET_LEFT);
                int rightIndex = diffLabel.indexOf(Dict.BRACKET_RIGHT);
                String fieldName = diffLabel.substring(leftIndex + 1, rightIndex);
                if (yamlMap.containsKey(fieldName)) {
                    String fieldCN = (String) yamlMap.get(fieldName);
                    Map newResultMap = new HashMap();
                    diffLabel = diffLabel.replace(Dict.BRACKET_LEFT + fieldName + Dict.BRACKET_RIGHT, fieldCN);
                    newResultMap.put(Dict.DIFFLABEL, diffLabel);
                    newResultMap.put(Dict.DIFFINFO, diffInfo);
                    newResultMap.put(Dict.DIFFDATA, diffData);
                    newResultList.add(newResultMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newResultList;

    }

    /**
     * 将source替换成target
     *
     * @param source
     * @param target
     * @return
     */
    public static String replaceStrToTarget(String source, String target) {
        if (source.contains(target)) {
            return target;
        }else
            return null;
    }


    public static <T> void getListField(List result, Class<T> clazz) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        //获取entity的包名
        String fullClazzName = clazz.getName();
        String simpleName = clazz.getSimpleName();
        fullClazzName = fullClazzName.replace("." + simpleName, "");
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            Type type = declaredField.getGenericType();
            Class<?> fieldType = declaredField.getType();
            if (List.class.isAssignableFrom(fieldType) && !type.getTypeName().contains(fullClazzName)) {
                result.add(declaredField.getName());
            } else {
                //表示为自定义实体
                if (type.getTypeName().contains(fullClazzName)) {
                    //该字段是clazz的子类
                    getListField(result, fieldType);
                }
            }
        }
    }

}
