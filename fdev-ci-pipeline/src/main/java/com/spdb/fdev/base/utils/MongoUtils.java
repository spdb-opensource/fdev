package com.spdb.fdev.base.utils;

import com.spdb.fdev.base.dict.Dict;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

import java.util.*;

public class MongoUtils {


    /**
     * 由于spring没有实现mongo的addFields需要自己写，于是加了该方法
     * 传入的map格式需要
     *  key 为聚合增加的字段名，value为值对应的$字段
     *  对应的mongo命令为
     *      {
     *         $addFields:{
     *             "aveErrorTime":"$digitalRate.aveErrorTime",
     *             "errorExeTotal":"$digitalRate.errorExeTotal",
     *             "exeTotal":"$digitalRate.exeTotal",
     *             "successExeTotal":"$digitalRate.successExeTotal",
     *             "successRate":"$digitalRate.successRate"
     *         }
     *     }
     * @param data
     * @param aggreKeys      该值为填入data中有值为AggregationOperation的key，若没有给null或者[]
     * @return
     */
    public static AggregationOperation getAddFieldsAggregationOper(Map<String, Object> data, List<String> aggreKeys) {
        AggregationOperation aggregationOperation = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                if (CommonUtils.isNullOrEmpty(aggreKeys)) {
                    return new Document("$addFields", new Document(data));
                }else {
                    for (String key : aggreKeys) {
                        Object obj = data.get(key);
                        if (obj instanceof AggregationOperation) {
                            //若是这种类型需要进行转换
                            AggregationOperation target = (AggregationOperation) obj;
                            Document document = target.toDocument(aggregationOperationContext);
                            Map targetMap = document2Map(document, aggregationOperationContext);
                            data.put(key, targetMap);
                        }
                    }
                    return new Document("$addFields", new Document(data));
                }

            }
        };
        return aggregationOperation;
    }

    /**
     * 实现project的过滤字段 即mongo命令为
     *     {
     *         $project:{
     *             "digitalRate":0
     *         }
     *     }
     * @param data
     * @param aggreKeys 该值为填入data中有值为AggregationOperation的key，若没有给null或者[]
     * @return
     */
    public static AggregationOperation getProjectFiterFields(Map<String, Object> data, List<String> aggreKeys) {
        AggregationOperation project = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                if (CommonUtils.isNullOrEmpty(aggreKeys)) {
                    return new Document("$project", new Document(data));
                }else {
                    for (String key : aggreKeys) {
                        Object obj = data.get(key);
                        if (obj instanceof AggregationOperation) {
                            //若是这种类型需要进行转换
                            AggregationOperation target = (AggregationOperation) obj;
                            Document document = target.toDocument(aggregationOperationContext);
                            Map targetMap = document2Map(document, aggregationOperationContext);
                            data.put(key, targetMap);
                        }
                    }
                    return new Document("$project", new Document(data));
                }

            }
        };
        return project;
    }


    /**
     * 生成cond的aggregation，可以用来在aggregation中进行cond逻辑比较而产生不同的结果
     * 例子
     * $addFields:{
     *             flag:{
     *                 $cond:  {
     *                     if:{
     *                         $eq: [
     *                             '$author.nameEn',
     *                             'c-lisl1'
     *                         ]
     *                     },
     *                     then: true,
     *                     else: false
     *                 }
     *             }
     *         }
     *
     * @param ifMap     if表达式内容
     * @param thenMap   if表达式成立返回值  返回值类型不定，是根据外层调用来确定的
     * @param elseMap   if表达式不成立返回值  返回值类型不定，是根据外层调用来确定的
     * @param aggreKeys 该值为填入data中有值为AggregationOperation的key，若没有给null或者[]
     * @return
     */
    public static AggregationOperation getCondAggregation(Map<String, Object> ifMap, Object thenMap, Object elseMap, List<String> aggreKeys) {
        AggregationOperation project = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                if (CommonUtils.isNullOrEmpty(aggreKeys)) {
                    Map condMap = new HashMap();
                    condMap.put("if", ifMap);
                    condMap.put("then", thenMap);
                    condMap.put("else", elseMap);
                    return new Document("$cond", new Document(condMap));
                }else {
                    for (String key : aggreKeys) {
                        Object obj = ifMap.get(key);
                        if (obj instanceof AggregationOperation) {
                            //若是这种类型需要进行转换
                            AggregationOperation target = (AggregationOperation) obj;
                            Document document = target.toDocument(aggregationOperationContext);
                            Map targetMap = document2Map(document, aggregationOperationContext);
                            ifMap.put(key, targetMap);
                        }
                    }
                    Map condMap = new HashMap();
                    condMap.put("if", ifMap);
                    condMap.put("then", thenMap);
                    condMap.put("else", elseMap);
                    return new Document("$cond", new Document(condMap));
                }
            }
        };
        return project;
    }

    /**
     * addToSet的AggregationOperation获取方法
     *
     *
     * @param addToSetExper addToSet的表达式
     * @return
     */
    public static AggregationOperation getAddToSetDocAggregation(String addToSetExper) {
        AggregationOperation addToSet = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                return new Document("$addToSet", addToSetExper);
            }
        };
        return addToSet;
    }


    /**
     * 根据传入的Map，生成新的group，由于是现版本过低，直接使用api的方式不太友好，所以使用此方法重新实现
     *
     * @param groupDocMap
     * @return
     */
    public static AggregationOperation getGroupAggregation(Map<String, Object> groupDocMap) {
        if (CommonUtils.isNullOrEmpty(groupDocMap))
            return null;
        AggregationOperation group = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {
                Set<String> keySet = groupDocMap.keySet();
                for (String key : keySet) {
                    Object obj = groupDocMap.get(key);
                    if (obj instanceof AggregationOperation) {
                        //若是这种类型需要进行转换
                        AggregationOperation target = (AggregationOperation) obj;
                        Document document = target.toDocument(aggregationOperationContext);
                        Map targetMap = document2Map(document, aggregationOperationContext);
                        groupDocMap.put(key, targetMap);
                    }
                }
                return new Document("$group", new Document(groupDocMap));
            }
        };
        return group;
    }


    /**
     * 以pipeline方式来进行lookup
     * {
     *         $lookup:{
     *             from:"jobEntity",
     *             let:{
     *                 "categoryId":"$categoryId"
     *             },
     *             pipeline:[
     *                 {
     *                     $match:{
     *                         $expr:{
     *                             $eq:["$category.categoryId", "$$categoryId"]
     *                         },
     *                         "status":"1"
     *                     }
     *                 }
     *             ],
     *             as:"jobList"
     *         }
     *     }
     *
     *
     *
     *
     * @param from          连接的表名
     * @param let           定义的变量
     * @param pipeline      需要对from表的结果进一步操作后需要lookup的结果
     * @param as            表连接后作为以什么字段存在于新的doc上
     * @return
     */
    public static AggregationOperation lookupPipeline(String from, Map<String, Object> let, List<Object> pipeline, String as) {
        if (CommonUtils.isNullOrEmpty(pipeline))
            return null;
        if (CommonUtils.isNullOrEmpty(from))
            return null;
        if (CommonUtils.isNullOrEmpty(as)) {

            return null;
        }
        AggregationOperation lookup = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext aggregationOperationContext) {

                Map lookupMap = new HashMap();
                lookupMap.put("from", from);
                lookupMap.put("let", let);
                lookupMap.put(Dict.PIPELINE, pipeline);
                lookupMap.put("as", as);

                ListIterator<Object> listIterator = pipeline.listIterator();
                while (listIterator.hasNext()) {
                    Object obj = listIterator.next();
                    if (obj instanceof AggregationOperation) {
                        //若是这种类型需要进行转换
                        AggregationOperation target = (AggregationOperation) obj;
                        Document document = target.toDocument(aggregationOperationContext);
                        Map targetMap = document2Map(document, aggregationOperationContext);
                        listIterator.remove();
                        listIterator.add(targetMap);
                    }
                }

                return new Document("$lookup", new Document(lookupMap));
            }
        };
        return lookup;
    }


    /**
     * 根据group的_id的map再构建group map
     *
     *
     *
     * @param _idMap
     * @param sourceMap
     * @return
     */
    public static Map build_IdForGroup(Map _idMap, Map sourceMap) {
        if (CommonUtils.isNullOrEmpty(_idMap)) {
            return sourceMap;
        }

        if (CommonUtils.isNullOrEmpty(sourceMap)) {
            Map resultMap = new HashMap();
            resultMap.put(Dict._ID, _idMap);
            return resultMap;
        }

        sourceMap.put(Dict._ID, _idMap);
        return sourceMap;
    }

    /**
     * document里面的AggregationOperation转换成map
     *
     * @param source
     * @param aggregationOperationContext
     * @return
     */
    public static Map document2Map(Document source, AggregationOperationContext aggregationOperationContext) {
        if (CommonUtils.isNullOrEmpty(source))
            return null;
        else {
            Map result = new HashMap();
            for (String key : source.keySet()) {
                if (source.get(key) instanceof AggregationOperation) {
                    AggregationOperation obj = (AggregationOperation) source.get(key);
                    Document objDoc = obj.toDocument(aggregationOperationContext);
                    Map map = document2Map(objDoc, aggregationOperationContext);
                    result.put(key, map);
                }else {
                    result.put(key, source.get(key));
                }
            }
            return result;
        }
    }

    /**
     * 将一个map转换成一个document
     *
     * @param sourceMap
     * @return
     */
    public static Document map2Document(Map<String, Object> sourceMap) {
        if (CommonUtils.isNullOrEmpty(sourceMap))
            return null;
        Document document = new Document(sourceMap);
        return document;
    }

}
