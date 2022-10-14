package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.csii.pe.redis.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.TimeUtils;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.entity.AppSystem;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

@Repository
public class AppEntityDaoImpl implements IAppEntityDao {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印
    @Resource
    private MongoTemplate mongoTemplate;
    
    @Override
    public List<AppEntity> query(AppEntity appEntity) throws Exception {
        List<AppEntity> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = appEntity == null ? "{}" : objectMapper.writeValueAsString(appEntity);

        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        if (null == appEntity || CommonUtils.isNullOrEmpty(appEntity.getStatus())) {
            c.andOperator(Criteria.where(Dict.STATUS).nin("0"));
        }
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<AppEntity> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "app-entity", AppEntity.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public AppEntity update(AppEntity appEntity) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(appEntity.getId()));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(appEntity);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();


        Update update = Update.update(Dict.ID, appEntity.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (!Dict.OBJECTID.equals(key)) {
                if (Dict.LABEL.equals(key)) {
                    // 去除Label属性中空字符串
                    Set<String> set = new HashSet<>();
                    appEntity.getLabel().forEach(label -> {
                        if (!CommonUtils.isNullOrEmpty(label)) {
                            set.add(label);
                        }
                    });
                    update.set(key, set);
                } else
                    update.set(key, value);
            }
        }
        if (CommonUtils.isNullOrEmpty(appEntity.getDesc())) {
            update.set(Dict.DESC, appEntity.getDesc());
        }
        if (CommonUtils.isNullOrEmpty(appEntity.getPipeline_schedule_switch())) {
            update.set(Dict.PIPELINE_SCHEDULE_SWITCH, appEntity.getPipeline_schedule_switch());
        }
        if (null == appEntity.getLabel() || appEntity.getLabel().isEmpty()) {
            update.set(Dict.LABEL, new HashSet<>());
        }
        // 返回 查询到的数据
        this.mongoTemplate.findAndModify(query, update, AppEntity.class);
        return this.mongoTemplate.findOne(query, AppEntity.class);
    }

    @Override
    public AppEntity findById(String id) throws Exception {
        List<AppEntity> list = mongoTemplate.find(Query.query(Criteria.where(Dict.ID).is(id)), AppEntity.class);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public AppEntity save(AppEntity appEntity) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        appEntity.set_id(objectId);
        appEntity.setId(id);
        appEntity.setCreatetime(TimeUtils.getFormat(Dict.FORMAT_DATE));
        logger.info("saveAppEntity :" + appEntity);
        return mongoTemplate.save(appEntity);
    }

    /**
     * 根据中文名称模糊查询应用信息
     *
     * @param appnamezh
     * @return
     * @throws Exception
     */
    @Override
    public List<AppEntity> queryByAppNameZh(String appnamezh) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + appnamezh + ".*$", Pattern.CASE_INSENSITIVE);
        query.addCriteria(Criteria.where(Dict.NAME_ZH).regex(pattern));
        return this.mongoTemplate.find(query, AppEntity.class);
    }

    /**
     * 根据英文名称模糊查询应用信息
     *
     * @param appnameen
     * @return
     * @throws Exception
     */
    @Override
    public List<AppEntity> queryByAppNameEn(String appnameen) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + appnameen + ".*$", Pattern.CASE_INSENSITIVE);
        query.addCriteria(Criteria.where(Dict.NAME_EN).regex(pattern));
        return this.mongoTemplate.find(query, AppEntity.class);
    }

    /**
     * 根据英文信息精准查询应用信息
     *
     * @param appNameEn
     * @return
     */
    @Override
    public List<AppEntity> queryByAppName(String appNameEn) {
        return this.mongoTemplate.find(Query.query(Criteria.where(Dict.NAME_EN).is(appNameEn)), AppEntity.class);
    }

    @Override
    public AppEntity getAppByGitlabId(Integer id) {
        Criteria c = new Criteria();
        c.and(Dict.GITLAB_PROJECT_ID).is(id).andOperator(Criteria.where(Dict.STATUS).nin("0"));
        List<AppEntity> list = mongoTemplate.find(Query.query(c), AppEntity.class);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public int queryAppNum(String groupid, String startDate, String endDate) {
        Query query = new Query(
                Criteria.where("group").is(groupid)
                        .andOperator(
                                Criteria.where(Dict.CREATETIME).gt(startDate),
                                Criteria.where(Dict.CREATETIME).lte(endDate),
                                Criteria.where(Dict.STATUS).nin("0")));
        long apps = mongoTemplate.count(query, AppEntity.class);
        return Integer.parseInt(String.valueOf(apps));
    }

    @Override
    public int queryAppNum(List groupList, String startDate, String endDate) {
        Query query = new Query(
                Criteria.where("group").in(groupList)
                        .andOperator(
                                Criteria.where(Dict.CREATETIME).gt(startDate),
                                Criteria.where(Dict.CREATETIME).lte(endDate),
                                Criteria.where(Dict.STATUS).nin("0")));
        long apps = mongoTemplate.count(query, AppEntity.class);
        return Integer.parseInt(String.valueOf(apps));
    }

    @Override
    public List<AppEntity> queryAbandonApp(AppEntity appEntity) throws Exception {
        List<AppEntity> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = appEntity == null ? "{}" : objectMapper.writeValueAsString(appEntity);

        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        if (null == appEntity || CommonUtils.isNullOrEmpty(appEntity.getStatus())) {
            c.and(Dict.STATUS).is("0");
        }
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<AppEntity> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "app-entity", AppEntity.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public List<AppEntity> queryByAppSchedule() throws Exception {
        //查找所有打开 定时持续集成开关的应用
        List<AppEntity> appEntityList = mongoTemplate.find(Query.query(
                Criteria.where(Dict.PIPELINE_SCHEDULE_SWITCH).is("1")
                        .andOperator(Criteria.where(Dict.STATUS).nin("0"))), AppEntity.class);
        if (CommonUtils.isNullOrEmpty(appEntityList)) {
            return null;
        }
        return appEntityList;
    }

    @Override
    public List<AppEntity> queryAppByLabel(HashSet<String> label) throws Exception {
        if (label.size() == 1) {
            Query query = new Query(Criteria.where(Dict.STATUS).nin("0").and(Dict.LABEL).in(label));
            return this.mongoTemplate.find(query, AppEntity.class);
        }
        Query query = new Query(Criteria.where(Dict.STATUS).nin("0").and(Dict.LABEL).is(label));
        return this.mongoTemplate.find(query, AppEntity.class);
    }

    @Override
    public Map queryduty(Integer start, Integer pageSize, String userId, String name, String status) {
        Query query = new Query();
        Criteria elemCriteria = new Criteria();
        elemCriteria.and("id").is(userId);
        Criteria ctr = new Criteria();

        if (!CommonUtils.isNullOrEmpty(name)) {
            Pattern pattern = Pattern.compile("^.*" + name + ".*$");
            ctr.orOperator(Criteria.where(Dict.SPDB_MANAGERS).elemMatch(elemCriteria).orOperator(Criteria.where(Dict.NAMECN).regex(pattern), Criteria.where(Dict.NAMEEN).regex(pattern)),
                    Criteria.where(Dict.DEV_MANAGERS).elemMatch(elemCriteria).orOperator(Criteria.where(Dict.NAMECN).regex(pattern), Criteria.where(Dict.NAMEEN).regex(pattern)));
        }else{
            ctr.orOperator(Criteria.where(Dict.SPDB_MANAGERS).elemMatch(elemCriteria), Criteria.where(Dict.DEV_MANAGERS).elemMatch(elemCriteria));
        }

        if (!pageSize.toString().equals("0")) {
            query.limit(pageSize).skip(start);  //分页
        }
        Long count = 0L;
        query.with(Sort.by(Sort.Order.desc(Dict.CREATETIME)));
        List<AppEntity> appList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(status) && "0".equals(status)) {
            query.addCriteria(ctr);
            count = mongoTemplate.count(query, AppEntity.class,"app-entity_copy");
            appList = mongoTemplate.find(query, AppEntity.class, "app-entity_copy");
        } else {
            ctr.and(Dict.STATUS).nin("0");
            query.addCriteria(ctr);
            count = mongoTemplate.count(query, AppEntity.class);
            appList = mongoTemplate.find(query, AppEntity.class);
        }
        Map map = new HashMap();
        map.put(Dict.DATA, appList);
        map.put(Dict.COUNT, count);
        return map;
    }

    @Override
    public List<AppEntity> getAppByIdsOrGitlabIds(List params, String type) throws Exception {
        Criteria c = new Criteria();
        if (type.equals(Dict.ID)) {
            c.and(Dict.ID).in(params).andOperator(Criteria.where(Dict.STATUS).nin("0"));
        } else if (type.equals(Dict.GITLAB_PROJECT_ID)) {
            c.and(Dict.GITLAB_PROJECT_ID).in(params).and(Dict.STATUS).nin("0");
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.TYPE});
        }
        List<AppEntity> appEntityList = this.mongoTemplate.find(Query.query(c), AppEntity.class);
        return appEntityList;
    }

    @Override
    public List<AppEntity> queryAppsByUserId(String user_id) {
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(user_id)) {
            criteria.and(Dict.ID).in(user_id);
        }
        Criteria ctr = new Criteria();
        ctr.orOperator(Criteria.where(Dict.SPDB_MANAGERS).elemMatch(criteria), Criteria.where(Dict.DEV_MANAGERS).elemMatch(criteria));
        ctr.andOperator(Criteria.where(Dict.STATUS).nin("0"));
        Query query = new Query(ctr);
        return mongoTemplate.find(query, AppEntity.class);
    }

    /**
     * 分页查询
     */
    @Override
    public List<AppEntity> queryPagination(Integer start, Integer pageSize, String keyword, String status, List<String> groupIds, String typeId, List<String> label, String userId, String system) throws Exception {
        List<AppEntity> result = new ArrayList<>();
        Query q = makeQueryApp(keyword, status, groupIds, typeId, label, userId, system);
        if (!pageSize.toString().equals("0")) {
            q.limit(pageSize).skip(start);  //分页
        }
        try {
            result = mongoTemplate.find(q, AppEntity.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        return result;
    }

    /**
     * 根据条件查询应用列表总数
     */
    @Override
    public Long findAppListCount(String keyword, String status, List<String> groupIds, String typeId, List<String> label, String userId, String system) throws Exception {
        List<AppEntity> result = new ArrayList<>();

        Query q = makeQueryApp(keyword, status, groupIds, typeId, label, userId, system);
        Long count;
        try {
            count = mongoTemplate.count(q, AppEntity.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
        }
        return count;
    }

    @Override
    public AppEntity updateForEnv(AppEntity appEntity) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(appEntity.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(appEntity);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, appEntity.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, AppEntity.class);
        return this.mongoTemplate.findOne(query, AppEntity.class);
    }

    /**
     * 组装过滤条件
     *
     * @return
     */
    private Query makeQueryApp(String keyword, String status, List<String> groupIds, String typeId, List<String> label, String userId, String system) {
        Query q = new Query();
        Criteria[] arr;
        List<Criteria> listCr;
        List<List<Criteria>> orList = new ArrayList<>();
        //搜索框过滤
        if (!CommonUtils.isNullOrEmpty(keyword)) {
            Pattern pattern = Pattern.compile("^.*" + keyword + ".*$");
            Criteria criteria = Criteria.where("_id").ne(null);
            criteria.orOperator(Criteria.where(Dict.NAME_ZH).regex(pattern),
                    Criteria.where(Dict.NAME_EN).regex(pattern));
            q.addCriteria(criteria);
        }
        //状态过滤
        if (CommonUtils.isNullOrEmpty(status)) {
            Criteria criteria = Criteria.where(Dict.STATUS).nin("0");
            q.addCriteria(criteria);
        } else if (status.equals("0")) {
            Criteria criteria = Criteria.where(Dict.STATUS).is("0");
            q.addCriteria(criteria);
        }
        //组过滤
        if (!CommonUtils.isNullOrEmpty(groupIds)) {
            Criteria criteria = Criteria.where(Dict.GROUP).in(groupIds);
            q.addCriteria(criteria);
        }
        //应用类型名称
        if (!CommonUtils.isNullOrEmpty(typeId)) {
            Criteria criteria = Criteria.where(Dict.TYPE_ID).is(typeId);
            q.addCriteria(criteria);
        }
        //应用标签
        if (!CommonUtils.isNullOrEmpty(label)) {
            listCr = new ArrayList<>();
            for (String lab : label) {
                Pattern pattern = Pattern.compile("^.*" + lab + ".*$");
                listCr.add(Criteria.where(Dict.LABEL).regex(pattern));
            }
            orList.add(listCr);
        }
        //行内应用负责人id/应用负责人id
        if (!CommonUtils.isNullOrEmpty(userId)) {
            listCr = new ArrayList<>();
            listCr.add(Criteria.where(Dict.SPDB_MANAGERS).elemMatch(Criteria.where(Dict.ID).is(userId)));
            listCr.add(Criteria.where(Dict.DEV_MANAGERS).elemMatch(Criteria.where(Dict.ID).is(userId)));
            orList.add(listCr);
        }
        //系统id
        if (!CommonUtils.isNullOrEmpty(system)) {
            Criteria criteria = Criteria.where("system").is(system);
            q.addCriteria(criteria);
        }

        if (!CommonUtils.isNullOrEmpty(orList)) {
            Criteria[] criteriaArr = new Criteria[]{};
            List<Criteria> list = new ArrayList<>();
            for (List<Criteria> item : orList) {
                arr = new Criteria[]{};
                arr = item.toArray(arr);
                list.add(new Criteria().orOperator(arr));
            }
            criteriaArr = list.toArray(criteriaArr);
            q.addCriteria(new Criteria().andOperator(criteriaArr));
        }
        return q;
    }


    /**
     * 查询只涉及环境部署应用
     *
     * @return 应用id，英文名，中文名
     */
    @Override
    public List<AppEntity> queryNoInvolveEnvApp() {
        String label_name = "不涉及环境部署";
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).ne("0");
        criteria.and(Dict.LABEL).nin(label_name);
        Query query = new Query(criteria);
        List<AppEntity> appEntities = this.mongoTemplate.find(query, AppEntity.class);
        return appEntities;
    }

    @Override
    public void updateSystem(String systemId, String appId) {
        Query q = new Query(Criteria.where(Dict.ID).is(systemId));
        AppSystem system = this.mongoTemplate.findOne(q, AppSystem.class, "system");
        if (system == null)
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"所绑定的系统不存在"});

        Query query = new Query(Criteria.where(Dict.ID).is(appId));
        Update update = new Update();
        update.set("system", systemId);
        this.mongoTemplate.updateFirst(query, update, "app-entity");
    }

    @Override
    public List<AppEntity> getAppByNameEns(List params) {
        Criteria c = new Criteria();
        c.and(Dict.NAME_EN).in(params).and(Dict.STATUS).nin("0");
        List<AppEntity> appEntityList = this.mongoTemplate.find(Query.query(c), AppEntity.class);
        return appEntityList;
    }

	@Override
	public List<AppEntity> queryByGroupId(String groupId) throws Exception {
		Criteria c = new Criteria();
        c.and(Dict.GROUP).in(groupId).and(Dict.STATUS).nin("0");
		return mongoTemplate.find(Query.query(c), AppEntity.class);
	}

}
