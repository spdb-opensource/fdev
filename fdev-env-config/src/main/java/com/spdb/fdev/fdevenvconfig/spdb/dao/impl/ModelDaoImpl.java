package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.JsonSchemaDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author xxx
 * @date 2019/7/5 13:21
 */
@Repository
public class ModelDaoImpl implements IModelDao {

    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private JsonSchemaDao jsonSchemaDao;
    @Autowired
    private ModelTemplateDaoImpl modelTemplateDao;

    @Override
    public List<Model> query(Model model) throws Exception {
        List<Model> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = model == null ? "{}" : objectMapper.writeValueAsString(model);
        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Constants.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        SortOperation sort = Aggregation.sort(new Sort(Sort.Direction.ASC, Dict.SECOND_CATEGORY));
        AggregationResults<Model> docs = this.mongoTemplate.aggregate(Aggregation.newAggregation(project, match, sort), Constants.MODEL, Model.class);
        docs.forEach(result::add);
        joinJsonSchema(result);
        return result;
    }


    @Override
    public Model add(Model model) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        model.set_id(objectId);
        model.setId(id);
        // 初始化数据
        model.setStatus(Constants.STATUS_OPEN);
        model.setVersion(Constants.UP_VERSION);
        model.setCtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        model.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        return this.mongoTemplate.save(model);
    }

    @Override
    public Model update(Model model) throws Exception {
        Query query = Query.query(Criteria.where(Constants.ID).is(model.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(model);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Constants.ID, model.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, Model.class);
        return this.mongoTemplate.findOne(query, Model.class);
    }

    @Override
    public Model delete(Model model) {
        Query query = Query.query(Criteria.where(Constants.ID).is(model.getId()));
        Update update = Update.update(Constants.STATUS, model.getStatus())
                .set(Constants.OPNO, model.getOpno())
                .set(Constants.UTIME, model.getUtime());
        return this.mongoTemplate.findAndModify(query, update, Model.class);
    }

    @Override
    public Model queryById(Model model) {
        Query query = Query.query(Criteria.where(Constants.ID).is(model.getId()));
        Model requestModel = this.mongoTemplate.findOne(query, Model.class);
        joinJsonSchema(requestModel);
        return requestModel;
    }

    @Override
    public Model queryById(String id) {
        return this.mongoTemplate.findOne(Query.query(Criteria.where(Constants.ID).is(id)), Model.class);
    }

    @Override
    public List<Model> queryByIdList(Set<String> list, String status) {
        Query query = Query.query(Criteria.where(Constants.ID).in(list).and(Dict.STATUS).is(status));
        List<Model> models = this.mongoTemplate.find(query, Model.class);
        joinJsonSchema(models);
        return models;
    }

    @Override
    public List<Model> queryByIdList(Set<String> list, String var, String value) {
        Query query = Query.query(Criteria.where(Constants.ID).in(list).and(var).is(value).and(Constants.STATUS).is(Constants.STATUS_OPEN));
        List<Model> modelList = this.mongoTemplate.find(query, Model.class);
        joinJsonSchema(modelList);
        return modelList;
    }

    /**
     * 根据 实体英文集合 查询实体的信息
     *
     * @param modelNameEnSet
     * @param status
     * @return
     */
    @Override
    public List<Model> queryByNameEnSet(Set<String> modelNameEnSet, String status) {
        Query query = Query.query(Criteria.where(Constants.NAME_EN).in(modelNameEnSet).and(Constants.STATUS).is(status));
        List<Model> modelList = this.mongoTemplate.find(query, Model.class);
        joinJsonSchema(modelList);
        return modelList;
    }

    @Override
    public ModelCategory queryModelCateCategory() {
        List<ModelCategory> modelCategorys = this.mongoTemplate.findAll(ModelCategory.class);
        if (!modelCategorys.isEmpty()) {
            return modelCategorys.get(0);
        }
        return null;
    }

    @Override
    public Scope queryScope() {
        List<Scope> scopes = this.mongoTemplate.findAll(Scope.class);
        if (!scopes.isEmpty()) {
            return scopes.get(0);
        }
        return null;
    }

    @Override
    public Model queryVaribleForOne(String name_en, String Variable) {
        Query query = Query.query(Criteria.where(Constants.NAME_EN).is(name_en).and(Constants.STATUS).is(Dict.MESSAGEREAD));
        query.fields().elemMatch(Constants.ENV_KEY, Criteria.where(Constants.NAME_EN).is(Variable));
        return this.mongoTemplate.findOne(query, Model.class);
    }

    @Override
    public List<Model> queryFuzz(Map paramMap, String statusOpen, Class<Model> modelClass) {
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        Criteria criteria2 = new Criteria();

        criteria.and(Constants.STATUS).is(statusOpen);
        if (!CommonUtils.isNullOrEmpty(paramMap.get(Constants.TYPE))) {
            Pattern pattern = Pattern.compile("^.*" + paramMap.get(Constants.TYPE)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Constants.TYPE).regex(pattern);
        }
        if (!CommonUtils.isNullOrEmpty(paramMap.get(Constants.FIRST_CATEGORY))) {
            Pattern pattern = Pattern.compile("^.*" + paramMap.get(Constants.FIRST_CATEGORY)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Constants.FIRST_CATEGORY).regex(pattern);
        }
        if (!CommonUtils.isNullOrEmpty(paramMap.get(Constants.SECOND_CATEGORY))) {
            Pattern pattern = Pattern.compile("^.*" + paramMap.get(Constants.SECOND_CATEGORY)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Constants.SECOND_CATEGORY).regex(pattern);
        }

        if (!CommonUtils.isNullOrEmpty(paramMap.get(Constants.NAME_EN))) {
            Pattern pattern = Pattern.compile("^.*" + paramMap.get(Constants.NAME_EN)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            criteria1.and(Constants.NAME_EN).regex(pattern);
        }
        if (!CommonUtils.isNullOrEmpty(paramMap.get(Constants.NAME_CN))) {
            Pattern pattern = Pattern.compile("^.*" + paramMap.get(Constants.NAME_CN)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            criteria2.and(Constants.NAME_CN).regex(pattern);
        }
        Query query = new Query(criteria.orOperator(criteria1, criteria2));
        List<Model> modelList = mongoTemplate.find(query, modelClass);
        joinJsonSchema(modelList);
        return modelList;
    }

    @Override
    public List<Model> getByModelIds(List<String> idList) {
        Query query = new Query(Criteria.where(Dict.ID).in(idList));
        List<Model> modelList = mongoTemplate.find(query, Model.class);
        joinJsonSchema(modelList);
        return modelList;
    }

    @Override
    public List<Model> getModelBySecondCategory(String secondCategory) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is("1").and(Dict.SECOND_CATEGORY).is(secondCategory);
        List<Model> modelList = mongoTemplate.find(new Query(criteria), Model.class);
        joinJsonSchema(modelList);
        return modelList;
    }

    /**
     * 组装高级属性
     *
     * @param modelList
     */
    private void joinJsonSchema(List<Model> modelList) {
        for (Model model : modelList) {
            joinJsonSchema(model);
        }
    }

    /**
     * 组装高级属性
     *
     * @param model
     */
    private void joinJsonSchema(Model model) {
        if (model == null) {
            return;
        }
        List<Object> envKeyList = model.getEnv_key();
        // 遍历实体属性，判断是否有高级属性，有的话需要关联查询json_schema表获取高级属性
        for (Object envKey : envKeyList) {
            Map<String, Object> envKeyMap = (Map<String, Object>) envKey;
            String dataType = (String) envKeyMap.get(Dict.DATA_TYPE);
            if (Dict.OBJECT.equals(dataType) || Dict.ARRAY.equals(dataType)) {
                String jsonSchemaId = (String) envKeyMap.get(Dict.JSON_SCHEMA_ID);
                if (StringUtils.isNotEmpty(jsonSchemaId)) {
                    JsonSchema jsonSchema = jsonSchemaDao.getJsonSchema(jsonSchemaId);
                    envKeyMap.put(Dict.JSON_SCHEMA, jsonSchema.getJsonSchema());
                }
            }
        }
    }


    @Override
    public Model getByNameEn(String nameEn) {
        Query query = Query.query(Criteria.where(Constants.NAME_EN).is(nameEn).and(Dict.STATUS).ne(Constants.ZERO));
        return this.mongoTemplate.findOne(query, Model.class);
    }

    /**
     * 根据实体 英文集合查询实体信息
     *
     * @param modelSet
     * @return
     */
    @Override
    public List<Model> queryByNameEn(Set<String> modelSet) {
        Query query = new Query(Criteria.where(Dict.NAME_EN).in(modelSet));
        return mongoTemplate.find(query, Model.class);
    }

    @Override
    public Map<String, Object> pageQuery(Map map) {
        Map<String, Object> responseMap = new HashMap<>();
        Criteria c = new Criteria();
        c.and(Dict.STATUS).ne(Constants.ZERO);
        if (StringUtils.isNotBlank((String) map.get(Constants.NAME_EN))) {
            Pattern pattern = Pattern.compile("^.*" + map.get(Constants.NAME_EN)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            c.and(Constants.NAME_EN).regex(pattern);
        }
        if (StringUtils.isNotBlank((String) map.get(Constants.NAME_CN))) {
            Pattern pattern = Pattern.compile("^.*" + map.get(Constants.NAME_CN)
                    + ".*$", Pattern.CASE_INSENSITIVE);
            c.and(Constants.NAME_CN).regex(pattern);
        }
        if (StringUtils.isNotBlank((String) map.get(Dict.MODEL_TEMPLATE_ID))){
            c.and(Dict.MODEL_TEMPLATE_ID).is((String)map.get(Dict.MODEL_TEMPLATE_ID));
        }
        Query query = new Query(c);
        query.with(new Sort(Sort.Direction.ASC, Constants.FIRST_CATEGORY, Dict.SECOND_CATEGORY, Constants.SUFFIX_NAME));
        Long total = mongoTemplate.count(query, Model.class);
        responseMap.put(Dict.TOTAL, total);
        long startPage = ((Integer) map.get(Dict.PAGE) - 1) * ((Integer) map.get(Dict.PER_PAGE));
        query.skip(startPage).limit((Integer) map.get(Dict.PER_PAGE));
        List<Model> result = this.mongoTemplate.find(query, Model.class);
        List<Map> resultMap = joinTemplateName(result);
        responseMap.put(Dict.LIST, resultMap);
        return responseMap;
    }

    /**
     * 组装实体模板中英文名
     * @param modelList
     * @return
     */
    public List<Map> joinTemplateName(List<Model> modelList) {
        List<Map> modelMapList = new ArrayList<Map>();
        for (Model model : modelList) {
            Map modelMap = joinTemplateName(model);
            modelMapList.add(modelMap);
        }
        return modelMapList;
    }

    /**
     * 组装实体模板中英文名
     * @param model
     * @return
     */
    public Map joinTemplateName(Model model) {
        Map result = CommonUtils.object2Map(model);
        if (model == null || model.getModel_template_id() == null) {
            return result;
        }
        ModelTemplate template = modelTemplateDao.queryById(model.getModel_template_id());
        String templateNameCn, templateNameEn;
        if(CommonUtils.isNullOrEmpty(template)){
            templateNameCn = "";
            templateNameEn = "";
        }
        else{
            templateNameCn = template.getNameCn();
            templateNameEn = template.getNameEn();
        }
        result.put(Dict.MODEL_TEMPLATE_NAME_CN, templateNameCn);
        result.put(Dict.MODEL_TEMPLATE_NAME_EN, templateNameEn);
        return result;
    }


    @Override
    public List<Model> queryBySecondcategoryList(Set<String> secodSets) {
        Query query = Query.query(Criteria.where(Constants.SECOND_CATEGORY).in(secodSets).and(Dict.STATUS).ne(Constants.ZERO));
        List<Model> models = this.mongoTemplate.find(query, Model.class);
        return models;
    }

    @Override
    public List<Model> getModels(String type) {
        Criteria criteria = new Criteria();
        if (Dict.CI.equals(type)) {
            criteria.and(Constants.FIRST_CATEGORY).is(Dict.CI);
        } else if (Dict.NOT_CI.equals(type)) {
            criteria.and(Constants.FIRST_CATEGORY).ne(Dict.CI);
        }
        criteria.and(Dict.STATUS).ne(Constants.STATUS_LOSE);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Model.class);
    }
}
