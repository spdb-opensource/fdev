package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GroupTreeUtil;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTaskCollection;
import com.spdb.fdev.fdevtask.spdb.entity.GroupTree;
import com.spdb.fdev.fdevtask.spdb.service.DemandService;
import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class FdevDaoImpl implements IFdevTaskDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private GroupTreeUtil groupTreeUtil;

    @Autowired
    private IUserApi userApi;

    @Autowired
    @Lazy
    private IFdevTaskService fdevTaskService;

    @Autowired
    private DemandService demandService;


    @Override
    public FdevTask save(FdevTask task) throws Exception {
        if (null != task.getReview()) {
            task.getReview().viewId();
        }
        return mongoTemplate.save(task);
    }

    @Override
    public List<FdevTask> query(FdevTask task) throws Exception {
        List<FdevTask> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = task == null ? "{}" : objectMapper.writeValueAsString(task);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Criteria c = new Criteria();
        c.andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()));
//        if (null == task || CommonUtils.isNullOrEmpty(task.getStage())) {
//            c.norOperator(Criteria.where("stage").is("file"));
//        }
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude("_id");
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<FdevTask> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "task", FdevTask.class);
        docs.forEach(doc -> {
            result.add(doc);
        });
        return result;
    }

    @Override
    public List<FdevTask> queryAll(FdevTask task) throws Exception {
        List<FdevTask> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = task == null ? "{}" : objectMapper.writeValueAsString(task);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Criteria c = new Criteria();
//        if (null == task || CommonUtils.isNullOrEmpty(task.getStage())) {
//            c.norOperator(Criteria.where("stage").is("file"));
//        }
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude("_id");
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<FdevTask> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "task", FdevTask.class);
        docs.forEach(doc -> {
            result.add(doc);
        });
        return result;
    }

    @Override
    public FdevTask update(FdevTask task) throws Exception {
        if (null != task.getReview()) {
            task.getReview().viewId();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        String json = objectMapper.writeValueAsString(task);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Query query = Query.query(Criteria.where("id").is(task.getId()));
        Update update = Update.update("id", task.getId());

        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key)) {
                continue;
            }
            if ("review".equals(key)) {
                Iterator<String> reviewIt = ((BasicDBObject) value).keySet().iterator();
                while (reviewIt.hasNext()) {
                    String next = reviewIt.next();
                    if (next.equals(Dict.SECURITY_TEST)) {
                        String c = (String) ((BasicDBObject) value).get(Dict.SECURITY_TEST);
                        Map temp = new HashMap<>();
                        temp.put(Dict.SECURITY_TEST, c);
                    } else if (next.equals(Dict.COMMON_PROFILE)) {
                        Boolean c = (Boolean) ((BasicDBObject) value).get(Dict.COMMON_PROFILE);
                        Map temp = new HashMap<>();
                        temp.put(Dict.COMMON_PROFILE, c);
                    } else if (next.equals(Dict.SPECIAL_CASE)) {
                        List c = (List) ((BasicDBObject) value).get(Dict.SPECIAL_CASE);
                        Map temp = new HashMap<>();
                        temp.put(Dict.COMMON_PROFILE, c);
                    } else {
                        BasicDBList reviewValueList = (BasicDBList) ((BasicDBObject) value).get(next);
                        for (Object reviewValueObj : reviewValueList) {
                            if (null != reviewValueObj) {
                                Map map = (Map) reviewValueObj;
                                map.put("cid", map.get(Dict.ID));
                                map.remove(Dict.ID);
                            }
                        }
                    }
                }
            }
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, FdevTask.class);
        return mongoTemplate.findOne(query, FdevTask.class);
    }


    /* 根据任务id集合查询任务列表 */
    @Override
    public List<FdevTask> queryTasksByIds(List<String> ids) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).in(ids).andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile())));
        return mongoTemplate.find(query, FdevTask.class);
    }


    /* 根据任务id集合查询没删除任务列表 */
    @Override
    public List<FdevTask> queryTasksByIdsNoAbort(List<String> ids) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).in(ids).andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortList())));
        return mongoTemplate.find(query, FdevTask.class);
    }


    /* 根据任务id集合查询任务列表 */
    @Override
    public List<FdevTask> queryAllTasksByIds(ArrayList ids) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).in(ids));
        List<FdevTask> result = mongoTemplate.find(query, FdevTask.class);
        return result;
    }

    /* 获取当前人员参与的任务列表 */
    @Override
    public Map<String, Object> queryUserTask(String userId, String nameKey, Integer pageNum, Integer pageSize, Boolean incloudFile) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.STAGE).in(incloudFile ? CommonUtils.getNoAbortList() : CommonUtils.getNoAbortAndNoFile()).
                orOperator(Criteria.where(Dict.DEVELOPER).all(userId),
                        Criteria.where(Dict.TESTER).all(userId),
                        Criteria.where(Dict.CONCERN).all(userId),
                        Criteria.where(Dict.SPDB_MASTER).all(userId),
                        Criteria.where(Dict.MASTER).all(userId),
                        Criteria.where(Dict.CREATOR).all(userId));
        if( !CommonUtils.isNullOrEmpty(nameKey) ){
            Pattern pattern = Pattern.compile("^.*"+nameKey+".*$");
            criteria.and(Dict.NAME).regex(pattern);
        }
        query.addCriteria(criteria);
        Long count = mongoTemplate.count(query,FdevTask.class);
        query.with(new Sort(Sort.Direction.DESC, Dict.START_TIME));
        if ( !CommonUtils.isNullOrEmpty(pageNum) && !CommonUtils.isNullOrEmpty(pageSize) && 0 != pageSize ) {
            Integer start = pageSize * (pageNum - 1);   //起始
            query.limit(pageSize).skip(start);  //分页
        }
        List<FdevTask> result = mongoTemplate.find( query , FdevTask.class );
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,result);
        return map;
    }

    /* 模糊匹配 */
    @Override
    public List<Map> queryTaskByTerm(String taskName, String stage) throws Exception {
        taskName = escapeExprSpeciaWord(taskName);
        List queryList = CommonUtils.getNoAbortAndNoFile();
        if (CommonUtils.isNotNullOrEmpty(stage)) {
            queryList.clear();
            Stream.of(stage.split(",")).forEach(str -> {
                if (Dict.TASK_STAGE_FILE.equals(str)) {
                    queryList.add(Dict.TASK_STAGE_FILE);
                }
                if (Dict.TASK_STAGE_ABORT.equals(str)) {
                    queryList.add(Dict.TASK_STAGE_ABORT);
                }
                if (Dict.TASK_STAGE_DISCARD.equals(str)) {
                    queryList.add(Dict.TASK_STAGE_DISCARD);
                }
            });
        }
        Criteria c = Criteria.where(Dict.STAGE).in(queryList);
        List<FdevTask> nameTerm = mongoTemplate.find(Query.query(Criteria.where(Dict.NAME).regex(taskName).andOperator(c)),
                FdevTask.class);
        List<FdevTask> redTerm = mongoTemplate.find(Query.query(Criteria.where(Dict.REDMINE_ID).regex(taskName).andOperator(c)),
                FdevTask.class);
        List<FdevTask> proTerm = mongoTemplate.find(Query.query(Criteria.where(Dict.PROJECT_NAME).regex(taskName).andOperator(c)),
                FdevTask.class);
        List<FdevTask> tagTerm = mongoTemplate.find(Query.query(Criteria.where(Dict.TAG).regex(taskName).andOperator(c)),
                FdevTask.class);
        List<FdevTask> implTerm = mongoTemplate.find(Query.query(Criteria.where(Dict.IMPLEMENT_ID).regex(taskName).andOperator(c)),
                FdevTask.class);
        List<Map> result = new ArrayList<>();

       /* List<FdevTask> rqrmntNoTerm = this.queryTaskByRqrmntNo(taskName,stage);
        List<Map> rqrmntNoTerm1 = addRqrmntInfo(rqrmntNoTerm);
        result = haveResult(rqrmntNoTerm1, result);*/
        List<Map> nameTerm1 = addRqrmntInfo(nameTerm);
        List<Map> redTerm1 = addRqrmntInfo(redTerm);
        List<Map> proTerm1 = addRqrmntInfo(proTerm);
        List<Map> tagTerm1 = addRqrmntInfo(tagTerm);
        List<Map> implTerm1 = addRqrmntInfo(implTerm);
        result = haveResult(nameTerm1, result);
//
        result = haveResult(redTerm1, result);
        result = haveResult(proTerm1, result);
        result = haveResult(tagTerm1, result);
        result = haveResult(implTerm1, result);
//        //过滤操作
//        result = result.stream().filter(map -> {
//            String newStage = (String)map.get("stage");
//            return stage.contains(newStage);
//        }).collect(Collectors.toList());
        return result;
    }

    public String escapeExprSpeciaWord(String keyWord) {
        String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "{", "}", "|"};
        for (String key : fbsArr) {
            if (keyWord.contains(key)) {
                keyWord = keyWord.replace(key, "\\" + key);
            }
        }
        return keyWord;
    }

  /*  private List<FdevTask> queryTaskByRqrmntNo(String taskName, String stage) {
        List<Map> rqrmnts = requirementApi.queryRqrmntsByRqrmntNo(taskName);
        Set<String> rqrmntIds = new HashSet<>();
        if (rqrmnts != null && rqrmnts.size() >= 1) {
            for (Map rqrmnt : rqrmnts) {
                rqrmntIds.add((String) rqrmnt.get("_id"));
            }
        }
        List<String> rqrmntIds1 = new ArrayList<>(rqrmntIds);
        List<FdevTask> tasks = queryByrqrmntIds(rqrmntIds1,stage);
        return tasks;

    }*/

    private List<Map> addRqrmntInfo(List<FdevTask> fdevTasks) {
        List<Map> tasks = new ArrayList<>();
        if (fdevTasks != null && fdevTasks.size() > 0) {
            for (FdevTask fdevTask : fdevTasks) {
                Map map = CommonUtils.obj2Map(fdevTask);
                //通过研发单元编号添加需求信息
                Map demand = demandService.queryByFdevNo(fdevTask.getRqrmnt_no(), fdevTask.getFdev_implement_unit_no());
                map.put(Dict.DEMAND, demand.get(Dict.DEMAND_BASEINFO));
                map.put(Dict.IMPLEMENT_UNIT_INFO, demand.get(Dict.IMPLEMENT_UNIT_INFO));
                tasks.add(map);
            }
        }
        return tasks;
    }

    @Override
    public FdevTask updateTaskView(String scope, String viewId, String name, Boolean audit) {
        Query query = Query.query(Criteria.where("review." + scope + ".cid").is(viewId));
        Update update = new Update();
        update.set("review." + scope + ".$.audit", audit);
        if (null != name && !"".equals(name)) {
            update.set("review." + scope + ".$.name", name);
        }
        mongoTemplate.findAndModify(query, update, FdevTask.class);
        return mongoTemplate.findOne(query, FdevTask.class);
    }

    public List<Map> haveResult(List<Map> devs, List<Map> result) {
        for (Map dev : devs) {
            if (result.size() == 0) {
                result.add(dev);
            } else {
                boolean haveFlag = false;
                for (Map re : result) {
                    if (re.get(Dict.ID).equals(dev.get(Dict.ID))) {
                        haveFlag = true;
                    }
                }
                if (!haveFlag) {
                    result.add(dev);
                }
            }
        }
        return result;
    }

    @Override
    public List<FdevTask> queryByTerms(Set<String> groupIds, ArrayList stage, boolean isIncludeChildren, Integer isDefered) throws Exception {
        Criteria c = new Criteria();
        if (null == stage || stage.size() == 0) {
            c.andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()));
        } else if (null != stage && stage.size() > 0) {
            c.and(Dict.STAGE).in(stage);
        }
        if (!CommonUtils.isNullOrEmpty(groupIds)) {
            c.and(Dict.GROUP).in(groupIds);
        }
        if (null != isDefered && isDefered == 1) {
            c.and("taskSpectialStatus").is(isDefered);
        }
        Query query = Query.query(c);
        List<FdevTask> fdevTaskList = mongoTemplate.find(query.with(new Sort(Sort.Direction.DESC, "start_time")), FdevTask.class);
        return fdevTaskList;
    }

    @Override
    public FdevTaskCollection saveFdevTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception {
        return mongoTemplate.save(fdevTaskCollection);
    }

    @Override
    public List<Map> queryFdevTaskCollection(ArrayList ids) {
        List<Map> listMap = new ArrayList<Map>();
        Criteria criteria;
        if (CommonUtils.isNullOrEmpty(ids)) {
            criteria = new Criteria();
        } else {
            criteria = Criteria.where(Dict.ID).in(ids);
        }
        List<FdevTask> tasks = mongoTemplate.find(Query.query(criteria.andOperator(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_DEVELOP))), FdevTask.class);
        List<String> taskIds = new ArrayList<>();
        for (FdevTask task : tasks) {
            taskIds.add(task.getId());
        }
        List<FdevTaskCollection> fdevTaskCollectionList = mongoTemplate.find(Query.query(Criteria.where(Dict.TASK_ID).in(taskIds)), FdevTaskCollection.class);
        for (FdevTaskCollection fdevTaskCollection : fdevTaskCollectionList) {
            for (FdevTask task : tasks) {
                if (fdevTaskCollection.getTask_id().equals(task.getId())) {
                    Map task1 = new HashMap();
                    task1.put(Dict.PROJECT_ID, task.getProject_id());
                    task1.put(Dict.PROJECT_NAME, task.getProject_name());
                    task1.put(Dict.ID, task.getId());
                    task1.put(Dict.NAME, task.getName());
                    Map dataMap = CommonUtils.obj2Map(fdevTaskCollection);
                    dataMap.remove("_id");
                    dataMap.remove("serialVersionUID");
                    dataMap.put(Dict.TASK, task1);
                    listMap.add(dataMap);
                }
            }
        }
        return listMap;
    }

    @Override
    public FdevTaskCollection queryByJobId(FdevTaskCollection fdevTaskCollection) throws Exception {
        FdevTaskCollection fdevTaskCollection1 = mongoTemplate.findOne(
                Query.query(Criteria.where(Dict.ID).is(fdevTaskCollection.getId())), FdevTaskCollection.class);
        return fdevTaskCollection1;
    }

    @Override
    public List<FdevTaskCollection> queryByTaskId(String task_id) throws Exception {
        List<FdevTaskCollection> ftcList = mongoTemplate.find(
                Query.query(Criteria.where(Dict.TASK_ID).is(task_id)), FdevTaskCollection.class);
        return ftcList;
    }


    @Override
    public FdevTaskCollection updateTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        String json = objectMapper.writeValueAsString(fdevTaskCollection);
        BasicDBObject taskcollectionkJson = BasicDBObject.parse(json);
        Iterator<String> it = taskcollectionkJson.keySet().iterator();
        Query query = Query.query(Criteria.where(Dict.ID).is(fdevTaskCollection.getId()));
        Update update = Update.update(Dict.ID, fdevTaskCollection.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskcollectionkJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key)) continue;
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, FdevTaskCollection.class);
        return mongoTemplate.findOne(query, FdevTaskCollection.class);
    }

    public List<Map> queryBySubTask(FdevTaskCollection task) throws Exception {
        List<Map> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(task);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.TASKCOLLECTION.equals(key)) {
                c.and(key).all(task.getTaskcollection());
            }
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<Map> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match), "task_collection", Map.class);
        Iterator<Map> iterator = docs.iterator();
        while (iterator.hasNext()) {
            Map next = iterator.next();
            next.remove("_id");
            result.add(next);
        }
        if (result.size() == 0) {
            Criteria c2 = new Criteria();
            c2.and(Dict.TASK_ID).is(task.getTaskcollection().get(0));
            AggregationOperation match2 = Aggregation.match(c2);
            AggregationResults<Map> docs2 = mongoTemplate.aggregate(Aggregation.newAggregation(match2), "task_collection", Map.class);
            Iterator<Map> iterator2 = docs2.iterator();
            while (iterator2.hasNext()) {
                Map next = iterator2.next();
                next.remove("_id");
                result.add(next);
            }
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    public List<FdevTask> queryTaskCByUnitNo(String unitno) throws Exception {
        List<FdevTask> listt = new ArrayList<FdevTask>();
        List<FdevTaskCollection> listtc = new ArrayList<FdevTaskCollection>();
        List repeatIds = new ArrayList<>();
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_DEVELOP);
        listt = mongoTemplate.find(Query.query(Criteria.where("redmine_id").is(unitno).and(Dict.STAGE).in(stagelist)), FdevTask.class);
        listtc = mongoTemplate.findAll(FdevTaskCollection.class);
        for (FdevTask task : listt) {
            for (FdevTaskCollection taskc : listtc) {
                if (task.getId().equals(taskc.getTask_id())) {
                    repeatIds.add(task.getId());
                }
            }
        }
        return queryTasksByIds((ArrayList) repeatIds);
    }

    @Override
    public void removeTask(String id) throws Exception {
        mongoTemplate.findAllAndRemove(Query.query(Criteria.where(Dict.ID).is(id)), FdevTask.class);
    }

    @Override
    public int queryTaskNum(String groupId, String startDate, String endDate, GroupTree groupTree) throws Exception {
        Criteria c;
        if (!CommonUtils.isNullOrEmpty(groupTree)) {
            c = Criteria.where(Dict.GROUP).in(groupTreeUtil.getWithChild(groupTree, groupId));
        } else {
            c = Criteria.where(Dict.GROUP).is(groupId);
        }
        Query query;
        if (CommonUtils.isNullOrEmpty(startDate))
            query = new Query(
                    new Criteria().andOperator(c,
                            Criteria.where("start_time").lte(endDate)).and(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()));
        query = new Query(
                new Criteria().andOperator(c,
                        Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()),
                        Criteria.where("start_time").gt(startDate),
                        Criteria.where("start_time").lte(endDate)));
        List<FdevTask> fdevTasks = mongoTemplate.find(query, FdevTask.class);
        if (fdevTasks != null)
            return fdevTasks.size();
        return 0;
    }

    @Override
    public Map<String, Object> queryTaskNum(String groupId, GroupTree groupTree) {
        Criteria c;
        if (!CommonUtils.isNullOrEmpty(groupTree)) {
            c = Criteria.where(Dict.GROUP).in(groupTreeUtil.getWithChild(groupTree, groupId));
        } else {
            c = Criteria.where(Dict.GROUP).is(groupId);
        }
        Query query = new Query(c
                .andOperator(
                        Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile())));
        return queryCurrentTaskNum(query);
    }

    private Map<String, Object> queryCurrentTaskNum(Query query) {
        HashMap<String, Object> result = new HashMap<>();
        int todo = 0;
        int sit = 0;
        int develop = 0;
        int uat = 0;
        int rel = 0;
        int production = 0;
        ArrayList<String> todo_ids = new ArrayList<>();
        ArrayList<String> sit_ids = new ArrayList<>();
        ArrayList<String> dev_ids = new ArrayList<>();
        ArrayList<String> rel_ids = new ArrayList<>();
        ArrayList<String> uat_ids = new ArrayList<>();
        ArrayList<String> pro_ids = new ArrayList<>();

        List<FdevTask> fdevTasks = mongoTemplate.find(query, FdevTask.class);
        if (!CommonUtils.isNullOrEmpty(fdevTasks)) {
            for (FdevTask fdevTask : fdevTasks) {
                switch (fdevTask.getStage()) {
                    case Dict.TASK_STAGE_CREATE_INFO:
                        todo++;
                        todo_ids.add(fdevTask.getId());
                        break;
                    case Dict.TASK_STAGE_CREATE_APP:
                        todo++;
                        todo_ids.add(fdevTask.getId());
                        break;
                    case Dict.TASK_STAGE_CREATE_FEATURE:
                        todo++;
                        todo_ids.add(fdevTask.getId());
                        break;
                    case Dict.TASK_STAGE_DEVELOP:
                        develop++;
                        dev_ids.add(fdevTask.getId());
                        break;
                    case Dict.TASK_STAGE_SIT:
                        sit++;
                        sit_ids.add(fdevTask.getId());
                        break;
                    case Dict.TASK_STAGE_UAT:
                        uat++;
                        uat_ids.add(fdevTask.getId());
                        break;
                    case Dict.TASK_STAGE_REL:
                        rel++;
                        rel_ids.add(fdevTask.getId());
                        break;
                    case Dict.TASK_STAGE_PRODUCTION:
                        production++;
                        pro_ids.add(fdevTask.getId());
                        break;
                    default:
                        break;

                }
            }
        }
        result.put(Dict.TASK_STAGE_TODO, todo);
        result.put(Dict.TASK_STAGE_DEVELOP, develop);
        result.put(Dict.TASK_STAGE_SIT, sit);
        result.put(Dict.TASK_STAGE_UAT, uat);
        result.put(Dict.TASK_STAGE_REL, rel);
        result.put(Dict.TASK_STAGE_PRODUCTION, production);
        result.put(Dict.TASK_STAGE_TODO_TASKIDS, todo_ids);
        result.put(Dict.TASK_STAGE_DEVELOP_TASKIDS, dev_ids);
        result.put(Dict.TASK_STAGE_SIT_TASKIDS, sit_ids);
        result.put(Dict.TASK_STAGE_UAT_TASKIDS, uat_ids);
        result.put(Dict.TASK_STAGE_REL_TASKIDS, rel_ids);
        result.put(Dict.TASK_STAGE_PRODUCTION_TASKIDS, pro_ids);
        int total = todo + develop + sit + rel + uat + production;
        result.put(Dict.TOTAL, total);
        return result;
    }

    private Map<String, Object> queryCurrentTaskCard(Query query) {
        HashMap<String, Object> result = new HashMap<>();
        //创建
        List<Map<String, Object>> todoList = new ArrayList<>();
        //开发中
        List<Map<String, Object>> devList = new ArrayList<>();
        //sit测试
        List<Map<String, Object>> sitList = new ArrayList<>();
        //uat测试
        List<Map<String, Object>> uatList = new ArrayList<>();
        //rel测试
        List<Map<String, Object>> relList = new ArrayList<>();
        //投产
        List<Map<String, Object>> proList = new ArrayList<>();
        //总需求id
        List<String> rqrmntList = new ArrayList<>();

        List<FdevTask> fdevTasks = mongoTemplate.find(query, FdevTask.class);
        if (!CommonUtils.isNullOrEmpty(fdevTasks)) {
            for (FdevTask fdevTask : fdevTasks) {
                rqrmntList.add(fdevTask.getRqrmnt_no());
                switch (fdevTask.getStage()) {
                    case Dict.TASK_STAGE_CREATE_INFO:
                        todoList.add(getTaskCardMap(fdevTask));
                        break;
                    case Dict.TASK_STAGE_CREATE_APP:
                        todoList.add(getTaskCardMap(fdevTask));
                        break;
                    case Dict.TASK_STAGE_CREATE_FEATURE:
                        todoList.add(getTaskCardMap(fdevTask));
                        break;
                    case Dict.TASK_STAGE_DEVELOP:
                        devList.add(getTaskCardMap(fdevTask));
                        break;
                    case Dict.TASK_STAGE_SIT:
                        sitList.add(getTaskCardMap(fdevTask));
                        break;
                    case Dict.TASK_STAGE_UAT:
                        uatList.add(getTaskCardMap(fdevTask));
                        break;
                    case Dict.TASK_STAGE_REL:
                        relList.add(getTaskCardMap(fdevTask));
                        break;
                    case Dict.TASK_STAGE_PRODUCTION:
                        proList.add(getTaskCardMap(fdevTask));
                        break;
                    default:
                        break;
                }
            }
        }
        result.put("todoList", todoList);
        result.put("devList", devList);
        result.put("sitList", sitList);
        result.put("uatList", uatList);
        result.put("relList", relList);
        result.put("proList", proList);
        result.put("rqrmnNumList", rqrmntList);
        return result;
    }


    /**
     * 获取得到任务卡变看板的信息放入map，为了将map放入对应阶段的list中
     *
     * @param param
     * @return
     */
    private Map<String, Object> getTaskCardMap(FdevTask param) {
        //定义一个map来将task信息和rqrmnt信息放在一起，以及延期情况
        Map<String, Object> taskCardInfo = new HashMap<>();
        taskCardInfo.put("taskInfo", param);
        //获取需求id
        String rqrmnt_no = param.getRqrmnt_no();
        /*通过研发单元编号获取需求信息*/
        String fdev_implement_unit_no = param.getFdev_implement_unit_no();
        Map map = demandService.queryByFdevNo(param.getRqrmnt_no(), fdev_implement_unit_no);
        Map demand_baseinfo = (Map)map.get(Dict.DEMAND_BASEINFO);
        if (demand_baseinfo == null) {
            taskCardInfo.put("rqrmntInfo", new HashMap<>());
        } else
            taskCardInfo.put("rqrmntInfo", demand_baseinfo);
        //{"group":"","member":"","rqrmnt_no": rqrmnt_no,"redmine_id":oaNum,"history":false,"postCondition":[]}
        Map<String, Object> params = new HashMap();
        params.put(Dict.GROUP, "");
        params.put("member", "");
        params.put(Dict.RQRMNT_NO, rqrmnt_no);
        params.put(Dict.FDEV_IMPLEMENT_UNIT_NO, param.getFdev_implement_unit_no());
        params.put("history", false);
        params.put("postCondition", new ArrayList<>());
        //发postponeTask接口获取延期任务所处的状态
        List<Map> postponeInfo = this.fdevTaskService.getPostponeInfo(param);
        if (CommonUtils.isNullOrEmpty(postponeInfo))
            return taskCardInfo;
        String name = param.getName();
        for (Map postpone : postponeInfo) {
            String postponeName = (String) postpone.get(Dict.NAME);
            if (name.equals(postponeName)) {
                //获取延期的阶段
                String postponeStage = (String) postpone.get(Dict.STAGE);
                //延期任务查询接口存在bug，这边自己处理
                if (Dict.TASK_STAGE_CREATE_INFO.equals(postponeStage) ||
                        "create-app".equals(postponeStage) || "create-feature".equals(postponeStage))
                    postponeStage = Dict.TASK_STAGE_DEVELOP;
                //为了保证为dev,sit,uat,rel,pro的顺序，用arrayList来
                List<String> postponeList = new ArrayList<>();
                //当前延期的阶段
                // postponeList.add(postponeStage);
                //获取所有延期
                Map postponeMap = (Map) postpone.get("postpone");
                if (!CommonUtils.isNullOrEmpty(postponeMap)) {
                    //后续延期的阶段
                    Set keySet = postponeMap.keySet();
                    Iterator iterator = keySet.iterator();
                    while (iterator.hasNext()) {
                        postponeList.add((String) iterator.next());
                    }
                    taskCardInfo.put("postpone", postponeList);
                }
                break;
            }
        }
        return taskCardInfo;
    }

    @Override
    public Map<String, Object> queryTaskCardByMember(String id, List roles) {
        return queryCurrentTaskCard(new Query(getRoleCriteria(id, roles, false)));
    }

    @Override
    public List<FdevTask> queryTaskForDesign() {
        Query query = Query.query(Criteria.where("folder_id").ne(null).and("designDoc").exists(false));
        List<FdevTask> fdevTasks = mongoTemplate.find(query, FdevTask.class);
        return fdevTasks;
    }

    @Override
    public Map<String, Object> queryTaskNumByMember(String id, List roles) {
        return queryCurrentTaskNum(new Query(getRoleCriteria(id, roles, false)));
    }


    @Override
    public Map<String, Object> queryTaskNumByApp(String appId) {
        Query query = new Query(
                Criteria.where(Dict.PROJECT_ID).is(appId)
                        .andOperator(
                                Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile())));
        return queryCurrentTaskNum(query);
    }

    @Override
    public List<FdevTask> queryTaskDetailByfdevImplementUnitNo(List<String> fdev_implement_unit_no_list) {
        Query query = Query.query(Criteria.where("fdev_implement_unit_no").in(fdev_implement_unit_no_list));
        return mongoTemplate.find(query, FdevTask.class);
    }

    private Map<String, Object> queryOutputTaskNum(Criteria criteria, String startDate, String endDate) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        int create = 0;
        int dev = 0;
        int sit = 0;
        int uat = 0;
        int rel = 0;
        int pro = 0;
        ArrayList<String> create_ids = new ArrayList<>();
        ArrayList<String> dev_ids = new ArrayList<>();
        ArrayList<String> sit_ids = new ArrayList<>();
        ArrayList<String> rel_ids = new ArrayList<>();
        ArrayList<String> uat_ids = new ArrayList<>();
        ArrayList<String> pro_ids = new ArrayList<>();
        Criteria outputsCriteria = getOutputsCriteria(startDate, endDate);
        criteria.andOperator(outputsCriteria);
        List<FdevTask> fdevTasks = mongoTemplate.find(new Query(criteria), FdevTask.class);
        //批量查询需求信息
        if (!CommonUtils.isNullOrEmpty(fdevTasks)) {
            for (FdevTask fdevTask : fdevTasks) {
                if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_time(), startDate, endDate)) {
                    create++;
                    create_ids.add(fdevTask.getId());
                }
                if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_time(), startDate, endDate)) {
                    dev++;
                    dev_ids.add(fdevTask.getId());
                }
                if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_inner_test_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_inner_test_time(), startDate, endDate)) {
                    sit++;
                    sit_ids.add(fdevTask.getId());
                }
                if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_uat_test_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_uat_test_time(), startDate, endDate)) {
                    uat++;
                    uat_ids.add(fdevTask.getId());
                }
                if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_rel_test_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_rel_test_time(), startDate, endDate)) {
                    rel++;
                    rel_ids.add(fdevTask.getId());
                }
                if (!CommonUtils.isNullOrEmpty(fdevTask.getFire_time()) && CommonUtils.isBetweenDates(fdevTask.getFire_time(), startDate, endDate)) {
                    pro++;
                    pro_ids.add(fdevTask.getId());
                }
            }
        }
        result.put(Dict.TASK_STAGE_CREATE, create);
        result.put(Dict.TASK_STAGE_DEVELOP, dev);
        result.put(Dict.TASK_STAGE_SIT, sit);
        result.put(Dict.TASK_STAGE_UAT, uat);
        result.put(Dict.TASK_STAGE_REL, rel);
        result.put(Dict.TASK_STAGE_PRODUCTION, pro);
        result.put(Dict.TASK_STAGE_CREATE_TASKIDS, create_ids);
        result.put(Dict.TASK_STAGE_DEVELOP_TASKIDS, dev_ids);
        result.put(Dict.TASK_STAGE_SIT_TASKIDS, sit_ids);
        result.put(Dict.TASK_STAGE_UAT_TASKIDS, uat_ids);
        result.put(Dict.TASK_STAGE_REL_TASKIDS, rel_ids);
        result.put(Dict.TASK_STAGE_PRODUCTION_TASKIDS, pro_ids);
        int total = create + dev + sit + rel + uat + pro;
        result.put(Dict.TOTAL, total);
        return result;
    }

    private Map<String, Object> queryOutputTaskNum(Criteria criteria, String startDate, String endDate, List<String> demandTypeList, List<Integer> taskTypeList) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        int create = 0;
        int dev = 0;
        int sit = 0;
        int uat = 0;
        int rel = 0;
        int pro = 0;
        ArrayList<String> create_ids = new ArrayList<>();
        ArrayList<String> dev_ids = new ArrayList<>();
        ArrayList<String> sit_ids = new ArrayList<>();
        ArrayList<String> rel_ids = new ArrayList<>();
        ArrayList<String> uat_ids = new ArrayList<>();
        ArrayList<String> pro_ids = new ArrayList<>();
        Criteria outputsCriteria = getOutputsCriteria(startDate, endDate);
        criteria.andOperator(outputsCriteria);
        List<FdevTask> fdevTasks = mongoTemplate.find(new Query(criteria), FdevTask.class);
        if (!CommonUtils.isNullOrEmpty(fdevTasks)) {
            Set<String> demandIds = fdevTasks.stream().map(FdevTask::getRqrmnt_no).collect(Collectors.toSet());
            if (!CommonUtils.isNullOrEmpty(demandIds)) {
                //批量查询需求信息
                Map<String, String> demandTypeMap = demandService.queryDemandByIds(demandIds).stream().collect(Collectors.toMap(map -> map.get("id"), map -> map.get(Dict.DEMAND_TYPE)));
                for (FdevTask fdevTask : fdevTasks) {
                    String demandType = demandTypeMap.get(fdevTask.getRqrmnt_no());
                    if(demandTypeList.contains(demandType) && ((taskTypeList.contains(0) && (CommonUtils.isNullOrEmpty(fdevTask.getTaskType()))) || taskTypeList.contains(fdevTask.getTaskType()))){
                        if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_time(), startDate, endDate)) {
                            create++;
                            create_ids.add(fdevTask.getId());
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_time(), startDate, endDate)) {
                            dev++;
                            dev_ids.add(fdevTask.getId());
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_inner_test_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_inner_test_time(), startDate, endDate)) {
                            sit++;
                            sit_ids.add(fdevTask.getId());
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_uat_test_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_uat_test_time(), startDate, endDate)) {
                            uat++;
                            uat_ids.add(fdevTask.getId());
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevTask.getStart_rel_test_time()) && CommonUtils.isBetweenDates(fdevTask.getStart_rel_test_time(), startDate, endDate)) {
                            rel++;
                            rel_ids.add(fdevTask.getId());
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevTask.getFire_time()) && CommonUtils.isBetweenDates(fdevTask.getFire_time(), startDate, endDate)) {
                            pro++;
                            pro_ids.add(fdevTask.getId());
                        }
                    }
                }
            }
        }
        result.put(Dict.TASK_STAGE_CREATE, create);
        result.put(Dict.TASK_STAGE_DEVELOP, dev);
        result.put(Dict.TASK_STAGE_SIT, sit);
        result.put(Dict.TASK_STAGE_UAT, uat);
        result.put(Dict.TASK_STAGE_REL, rel);
        result.put(Dict.TASK_STAGE_PRODUCTION, pro);
        result.put(Dict.TASK_STAGE_CREATE_TASKIDS, create_ids);
        result.put(Dict.TASK_STAGE_DEVELOP_TASKIDS, dev_ids);
        result.put(Dict.TASK_STAGE_SIT_TASKIDS, sit_ids);
        result.put(Dict.TASK_STAGE_UAT_TASKIDS, uat_ids);
        result.put(Dict.TASK_STAGE_REL_TASKIDS, rel_ids);
        result.put(Dict.TASK_STAGE_PRODUCTION_TASKIDS, pro_ids);
        int total = create + dev + sit + rel + uat + pro;
        result.put(Dict.TOTAL, total);
        return result;
    }


    @Override
    public FdevTaskCollection deleteMainTask(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findAndRemove(query, FdevTaskCollection.class);
    }


    @Override
    public Map queryTaskAndRedmineNo() throws Exception {
        Map result = new HashMap();
        List<FdevTask> tmp = new ArrayList<>();
        Query query;
        query = new Query(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()));
        tmp = mongoTemplate.find(query, FdevTask.class);
        if (CommonUtils.isNullOrEmpty(tmp)) return result;
        tmp.forEach(task -> result.put(task.getId(), task.getRedmine_id()));
        return result;
    }

    @Override
    public List<FdevTask> queryTaskGroupByRqrmnt(String rqrmnt) throws Exception {
        Query query = new Query(Criteria.where("rqrmnt_no").is(rqrmnt)
                .andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortList()))
        );
        query.fields().exclude("_id");
        return mongoTemplate.find(query, FdevTask.class, Dict.TASK);
    }

    /**
     * 根据需求编号获取所有除了abort状态的任务集
     *
     * @param rqrmntNo 需求编号
     * @return
     * @throws Exception
     */
    @Override
    public List<FdevTask> queryAllTaskExcludeAbort(String rqrmntNo) {
        Query query = new Query(Criteria.where("rqrmnt_no").is(rqrmntNo)
                .andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortList()))
        );
        query.fields().exclude("_id");
        return mongoTemplate.find(query, FdevTask.class, Dict.TASK);
    }

    @Override
    public Map<String, Object> queryPostponeTask(Map<String, Object> params, GroupTree groupTree) {
        //分页参数
        Integer pageNum = (Integer) params.get(Dict.PAGE_NUM);
        Integer pageSize = (Integer) params.get(Dict.PAGE_SIZE);
        //延期阶段参数
        List<String> delayPhase = (List<String>) params.getOrDefault("postCondition", new ArrayList<>());
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()));
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.NAME))) criteriaList.add(Criteria.where(Dict.NAME).regex((String) params.get(Dict.NAME)));
        criteriaList.add(Criteria.where(Dict.TASK_SPECIAL_STATUS).ne(1));//过滤暂缓任务
        List<Criteria> isPostponeCriteria = new ArrayList<>();
        //脚本执行原isPostpone方法的过滤
        String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1);
        boolean nullDelayPhase = CommonUtils.isNullOrEmpty(delayPhase);
        if (nullDelayPhase && 5 == delayPhase.size()) nullDelayPhase = false;//细节优化，如果是5个阶段条件那么还是当作null，这样后面的查询条件会简化很多，不会影响查询结果
        boolean isHistory = (boolean) params.get("history");
        if (nullDelayPhase) {
            isPostponeCriteria.add(Criteria.where(Dict.STAGE).in(Dict.TASK_STAGE_CREATE_INFO,
                    Dict.TASK_STAGE_CREATE_APP,
                    Dict.TASK_STAGE_CREATE_FEATURE,
                    Dict.TASK_STAGE_DEVELOP).and(Dict.PLAN_INNER_TEST_TIME).nin(null, "").lt(now));
            //Sit
            isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_SIT).and(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").lt(now).and(Dict.UAT_TEST_TIME).in(null, ""));
            //Uat
            isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_SIT).and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now).and(Dict.UAT_TEST_TIME).nin(null, ""));
            isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_UAT).and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now));
            //Rel
            isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_REL).and(Dict.PLAN_FIRE_TIME).lt(now).nin(null, ""));
        }else{
            if (1 == delayPhase.size() && Dict.TASK_STAGE_DEVELOP.equals(delayPhase.get(0)) && !isHistory) {
                //这里完全保持原本逻辑，导致代码不美观甚至逻辑有点恶心
                //当且仅当 延期阶段条件唯一并且只有develop、非历史条件的时候，直接返回0数据量分页对象
                return new HashMap<String, Object>() {{
                    put(Dict.TOTAL,0);
                    put(Dict.TASKLIST, new ArrayList<>());
                }};
            }
            if (delayPhase.contains(Dict.TASK_STAGE_SIT)) {
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).in(Dict.TASK_STAGE_CREATE_INFO,
                        Dict.TASK_STAGE_CREATE_APP,
                        Dict.TASK_STAGE_CREATE_FEATURE,
                        Dict.TASK_STAGE_DEVELOP).and(Dict.PLAN_INNER_TEST_TIME).nin(null, "").lt(now));
            }
            if (delayPhase.contains(Dict.TASK_STAGE_UAT)) {
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).in(Dict.TASK_STAGE_CREATE_INFO,
                        Dict.TASK_STAGE_CREATE_APP,
                        Dict.TASK_STAGE_CREATE_FEATURE,
                        Dict.TASK_STAGE_DEVELOP).and(Dict.PLAN_INNER_TEST_TIME).nin(null, "").lt(now).and(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").lt(now));
                //Sit
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_SIT).and(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").lt(now).and(Dict.UAT_TEST_TIME).in(null, ""));
            }
            if (delayPhase.contains(Dict.TASK_STAGE_REL)) {
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).in(Dict.TASK_STAGE_CREATE_INFO,
                        Dict.TASK_STAGE_CREATE_APP,
                        Dict.TASK_STAGE_CREATE_FEATURE,
                        Dict.TASK_STAGE_DEVELOP).and(Dict.PLAN_INNER_TEST_TIME).nin(null, "").lt(now).and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now));
                //Sit
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_SIT).and(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").lt(now).and(Dict.UAT_TEST_TIME).in(null, "").and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now));
                //Uat
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_SIT).and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now).and(Dict.UAT_TEST_TIME).nin(null, ""));
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_UAT).and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now));
            }
            if (delayPhase.contains(Dict.TASK_STAGE_PRODUCTION)) {
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).in(Dict.TASK_STAGE_CREATE_INFO,
                        Dict.TASK_STAGE_CREATE_APP,
                        Dict.TASK_STAGE_CREATE_FEATURE,
                        Dict.TASK_STAGE_DEVELOP).and(Dict.PLAN_INNER_TEST_TIME).nin(null, "").lt(now).and(Dict.PLAN_FIRE_TIME).nin(null, "").lt(now));
                //Sit
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_SIT).and(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").lt(now).and(Dict.UAT_TEST_TIME).in(null, "").and(Dict.PLAN_FIRE_TIME).nin(null, "").lt(now));
                //Uat
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_SIT).and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now).and(Dict.UAT_TEST_TIME).nin(null, "").and(Dict.PLAN_FIRE_TIME).nin(null, "").lt(now));
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_UAT).and(Dict.PLAN_REL_TEST_TIME).nin(null, "").lt(now).and(Dict.PLAN_FIRE_TIME).nin(null, "").lt(now));
                //Rel
                isPostponeCriteria.add(Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_REL).and(Dict.PLAN_FIRE_TIME).lt(now).nin(null, ""));
            }
        }
        if (isHistory) {
            if (nullDelayPhase) {
                isPostponeCriteria.add(Criteria.where(Dict.PLAN_START_TIME).nin(null, "").and(Dict.START_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_START_TIME + " < " + "this." + Dict.START_TIME));
                isPostponeCriteria.add(Criteria.where(Dict.PLAN_INNER_TEST_TIME).nin(null, "").and(Dict.START_INNER_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_INNER_TEST_TIME + " < " + "this." + Dict.START_INNER_TEST_TIME));
                isPostponeCriteria.add(Criteria.where(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").and(Dict.START_UAT_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_UAT_TEST_START_TIME + " < " + "this." + Dict.START_UAT_TEST_TIME));
                isPostponeCriteria.add(Criteria.where(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").and(Dict.UAT_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_UAT_TEST_START_TIME + " < " + "this." + Dict.UAT_TEST_TIME).and(Dict.STAGE).is(Dict.TASK_STAGE_SIT));
                isPostponeCriteria.add(Criteria.where(Dict.PLAN_REL_TEST_TIME).nin(null, "").and(Dict.STOP_UAT_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_REL_TEST_TIME + " < " + "this." + Dict.STOP_UAT_TEST_TIME));
                isPostponeCriteria.add(Criteria.where(Dict.PLAN_FIRE_TIME).nin(null, "").and(Dict.FIRE_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_FIRE_TIME + " < " + "this." + Dict.FIRE_TIME));
            } else {
                if (delayPhase.contains(Dict.TASK_STAGE_DEVELOP)) {
                    isPostponeCriteria.add(Criteria.where(Dict.PLAN_START_TIME).nin(null, "").and(Dict.START_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_START_TIME + " < " + "this." + Dict.START_TIME));
                }
                if (delayPhase.contains(Dict.TASK_STAGE_SIT)) {
                    isPostponeCriteria.add(Criteria.where(Dict.PLAN_INNER_TEST_TIME).nin(null, "").and(Dict.START_INNER_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_INNER_TEST_TIME + " < " + "this." + Dict.START_INNER_TEST_TIME));
                }
                if (delayPhase.contains(Dict.TASK_STAGE_UAT)) {
                    isPostponeCriteria.add(Criteria.where(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").and(Dict.START_UAT_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_UAT_TEST_START_TIME + " < " + "this." + Dict.START_UAT_TEST_TIME));
                    isPostponeCriteria.add(Criteria.where(Dict.PLAN_UAT_TEST_START_TIME).nin(null, "").and(Dict.UAT_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_UAT_TEST_START_TIME + " < " + "this." + Dict.UAT_TEST_TIME).and(Dict.STAGE).is(Dict.TASK_STAGE_SIT));
                }
                if (delayPhase.contains(Dict.TASK_STAGE_REL)) {
                    isPostponeCriteria.add(Criteria.where(Dict.PLAN_REL_TEST_TIME).nin(null, "").and(Dict.STOP_UAT_TEST_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_REL_TEST_TIME + " < " + "this." + Dict.STOP_UAT_TEST_TIME));
                }
                if (delayPhase.contains(Dict.TASK_STAGE_PRODUCTION)) {
                    isPostponeCriteria.add(Criteria.where(Dict.PLAN_FIRE_TIME).nin(null, "").and(Dict.FIRE_TIME).nin(null, "").and("$where").is("this." + Dict.PLAN_FIRE_TIME + " < " + "this." + Dict.FIRE_TIME));
                }
            }
        }
        criteriaList.add(new Criteria().orOperator(isPostponeCriteria.toArray(new Criteria[]{})));
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.GROUP))) {//组转换为对应的子组和本身的集合
            criteriaList.add(Criteria.where(Dict.GROUP).in(groupTreeUtil.getWithChild(groupTree, (String) params.get(Dict.GROUP))));
        }
        if (!CommonUtils.isNullOrEmpty(params.get("member"))) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where(Dict.DEVELOPER).all(params.get("member")),
                    Criteria.where(Dict.TESTER).all(params.get("member")),
                    Criteria.where(Dict.SPDB_MASTER).all(params.get("member")),
                    Criteria.where(Dict.MASTER).all(params.get("member")),
                    Criteria.where(Dict.CREATOR).all(params.get("member")))
            );
        }
        //其他参数
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.RQRMNT_NO))) {
            criteriaList.add(new Criteria(Dict.RQRMNT_NO).all(params.get(Dict.RQRMNT_NO)));
        }
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.REDMINE_ID))) {
            criteriaList.add(new Criteria(Dict.REDMINE_ID).all(params.get(Dict.REDMINE_ID)));
        }
        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[]{})));
        query.fields().exclude("_id").exclude(Dict.DESC).exclude(Dict.DOC).exclude(Dict.REVIEW).exclude(Dict.SIT_MERGE_ID)
                .exclude(Dict.UAT_MERGE_ID).exclude(Dict.FEATURE_BRANCH).exclude(Dict.PROJECT_NAME).exclude(Dict.PROJECT_ID);
        return new HashMap<String, Object>() {{
            put(Dict.TOTAL, mongoTemplate.count(query, FdevTask.class, Dict.TASK));
            if(pageNum != null && pageSize != null){
                query.skip((pageNum - 1) * pageSize).limit(pageSize);
            }
            put(Dict.TASKLIST, mongoTemplate.find(query, FdevTask.class, Dict.TASK));
        }};
    }

    @Override
    public List<FdevTask> queryTaskNumRQR(String id, List roles) {
        List<FdevTask> list = mongoTemplate.find(new Query(getRoleCriteria(id, roles, false)), FdevTask.class);
        return list;
    }

    @Override
    public List<FdevTask> queryTaskNumByGroupinAll(String group, GroupTree groupTree, List stages) {
        Query query;
        if (groupTree != null) {
            query = new Query(new Criteria().andOperator(
                    Criteria.where(Dict.GROUP).in(groupTreeUtil.getWithChild(groupTree, group)),
                    Criteria.where(Dict.STAGE).in(stages))
            );
        } else {
            query = new Query(new Criteria().andOperator(
                    Criteria.where(Dict.GROUP).is(group),
                    Criteria.where(Dict.STAGE).in(stages))
            );
        }

        query.fields().exclude("_id");
        return mongoTemplate.find(query, FdevTask.class, Dict.TASK);
    }

    @Override
    public List<FdevTask> queryTasksByIdsNotinlineTasks(ArrayList taskIds) {
        Query query = Query.query(Criteria.where(Dict.ID).in(taskIds).andOperator(Criteria.where(Dict.STAGE).in(getNotinline())));
        List<FdevTask> result = mongoTemplate.find(query, FdevTask.class);
        return result;
    }

    @Override
    public Map<String, Object> queryOutputTaskbyGroup(GroupTree groupTree, String groupId, String startDate, String endDate) throws Exception {
        Criteria criteria = getCriteria(groupTree, groupId);
        return queryOutputTaskNum(criteria, startDate, endDate);
    }

    @Override
    public Map<String, Object> queryOutputTaskbyUser(List roles, String userId, String startDate, String endDate, List<String> demandTypeList, List<Integer> taskTypeList) throws Exception {
        Criteria roleCriteria = getRoleCriteria(userId, roles, true);
        return queryOutputTaskNum(roleCriteria, startDate, endDate, demandTypeList, taskTypeList);
    }

    @Override
    public List<FdevTask> queryByrqrmntIds(List<String> rqrmntIds1) {
        Query query = Query.query(Criteria.where(Dict.RQRMNT_NO).in(rqrmntIds1).andOperator(Criteria.where(Dict.STAGE).in(getNotinline())));
        List<FdevTask> result = mongoTemplate.find(query, FdevTask.class);
        return result;
    }

    @Override
    public List<FdevTask> queryByrqrmntIds(List<String> rqrmntIds1, String stage) {
        List notinline = getNotinline();
        if (CommonUtils.isNotNullOrEmpty(stage)) {
            notinline.clear();
            Stream.of(stage.split(",")).forEach(str -> {
                if (Dict.TASK_STAGE_FILE.equals(str)) {
                    notinline.add(Dict.TASK_STAGE_FILE);
                }
                if (Dict.TASK_STAGE_ABORT.equals(str)) {
                    notinline.add(Dict.TASK_STAGE_ABORT);
                }
                if (Dict.TASK_STAGE_DISCARD.equals(str)) {
                    notinline.add(Dict.TASK_STAGE_DISCARD);
                }
            });
        }
        Query query = Query.query(Criteria.where(Dict.RQRMNT_NO).in(rqrmntIds1).andOperator(Criteria.where(Dict.STAGE).in(notinline)));
        List<FdevTask> result = mongoTemplate.find(query, FdevTask.class);
        return result;
    }

    @Override
    public Map<String, Object> queryByTermsAndPage(Set<String> groupIds, ArrayList stage, boolean isIncludeChildren, Integer isDefered, int page, int per_page) throws Exception {
        Criteria group1 = getCriteriaByGroup(groupIds, isIncludeChildren, isDefered);
        Criteria c = getCriteriaByStage(group1, stage);
        Query query = Query.query(c);
        query.with(new Sort(Sort.Direction.DESC, "start_time"));
        Long total = mongoTemplate.count(query, FdevTask.class);
        if(per_page > 0) {
            query.skip((page - 1L) * per_page).limit(per_page);
        }
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.TASKLIST, mongoTemplate.find(query, FdevTask.class));
        return responseMap;

    }

    @Override
    public List<FdevTask> queryBySitMergeId(String merge_id, String project_id) {
        Query query;
        query = new Query(new Criteria().andOperator(
                Criteria.where(Dict.SIT_MERGE_ID).is(merge_id),
                Criteria.where(Dict.PROJECT_ID).is(project_id),
                Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile())));

        List<FdevTask> tasks = mongoTemplate.find(query, FdevTask.class);
        return tasks;
    }

    /*非归档阶段*/
    private List getNoFileList() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_CREATE_INFO);
        stagelist.add(Dict.TASK_STAGE_CREATE_APP);
        stagelist.add(Dict.TASK_STAGE_CREATE_FEATURE);
        stagelist.add(Dict.TASK_STAGE_DEVELOP);
        stagelist.add(Dict.TASK_STAGE_SIT);
        stagelist.add(Dict.TASK_STAGE_UAT);
        stagelist.add(Dict.TASK_STAGE_REL);
        stagelist.add(Dict.TASK_STAGE_PRODUCTION);
        stagelist.add(Dict.TASK_STAGE_ABORT);
        return stagelist;
    }

    /*异常阶段*/
    private List getAbortAndFileAndDiscard() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_ABORT);
        stagelist.add(Dict.TASK_STAGE_FILE);
        stagelist.add(Dict.TASK_STAGE_DISCARD);
        return stagelist;

    }

    /*未上线状态*/
    private List getNotinline() {
        List stagelist = new ArrayList();
        stagelist.add(Dict.TASK_STAGE_CREATE_INFO);
        stagelist.add(Dict.TASK_STAGE_CREATE_APP);
        stagelist.add(Dict.TASK_STAGE_CREATE_FEATURE);
        stagelist.add(Dict.TASK_STAGE_DEVELOP);
        stagelist.add(Dict.TASK_STAGE_SIT);
        stagelist.add(Dict.TASK_STAGE_UAT);
        return stagelist;
    }

    private Criteria getCriteria(GroupTree groupTree, String groupId) {
        Criteria c = null;
        if (!CommonUtils.isNullOrEmpty(groupTree)) {
            c = Criteria.where(Dict.GROUP).in(groupTreeUtil.getWithChild(groupTree, groupId));
        } else {
            c = Criteria.where(Dict.GROUP).is(groupId);
        }
        return c;
    }

    private Criteria getRoleCriteria(String id, List roles, boolean isOutputs) {
        //roles为空默认查全部
        if (roles != null && roles.size() == 0) {
            roles.add(Dict.TESTER);
            roles.add(Dict.DEVELOPER);
            roles.add(Dict.MASTER);
            roles.add(Dict.SPDB_MASTER);
            roles.add(Dict.CREATOR);
        }
        Criteria criteria = new Criteria();
        if (!isOutputs) {
            criteria = Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile());
        }
        Criteria[] criterias = new Criteria[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
            criterias[i] = Criteria.where((String) roles.get(i)).all(id);
        }
        criteria.orOperator(criterias);
        return criteria;
    }

    private Criteria getOutputsCriteria(String startDate, String endDate) {
        Criteria start_date = new Criteria();
        Criteria start_inner = new Criteria();
        Criteria start_uat = new Criteria();
        Criteria start_rel = new Criteria();
        Criteria into_pro = new Criteria();
        if (!CommonUtils.isNullOrEmpty(startDate) && !CommonUtils.isNullOrEmpty(endDate)) {
            start_date = Criteria.where(Dict.START_TIME).gte(startDate).lte(endDate);
            start_inner = Criteria.where(Dict.START_INNER_TEST_TIME).gte(startDate).lte(endDate);
            start_uat = Criteria.where(Dict.START_UAT_TEST_TIME).gte(startDate).lte(endDate);
            start_rel = Criteria.where(Dict.START_REL_TEST_TIME).gte(startDate).lte(endDate);
            into_pro = Criteria.where(Dict.FIRE_TIME).gte(startDate).lte(endDate);
        } else if (CommonUtils.isNullOrEmpty(startDate) && !CommonUtils.isNullOrEmpty(endDate)) {
            start_date = Criteria.where(Dict.START_TIME).lte(endDate);
            start_inner = Criteria.where(Dict.START_INNER_TEST_TIME).lte(endDate);
            start_uat = Criteria.where(Dict.START_UAT_TEST_TIME).lte(endDate);
            start_rel = Criteria.where(Dict.START_REL_TEST_TIME).lte(endDate);
            into_pro = Criteria.where(Dict.FIRE_TIME).lte(endDate);
        } else if (!CommonUtils.isNullOrEmpty(startDate) && CommonUtils.isNullOrEmpty(endDate)) {
            start_date = Criteria.where(Dict.START_TIME).gte(startDate);
            start_inner = Criteria.where(Dict.START_INNER_TEST_TIME).gte(startDate);
            start_uat = Criteria.where(Dict.START_UAT_TEST_TIME).gte(startDate);
            start_rel = Criteria.where(Dict.START_REL_TEST_TIME).gte(startDate);
            into_pro = Criteria.where(Dict.FIRE_TIME).gte(startDate);
        }
        Criteria c = Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortList());
        c.orOperator(start_date, start_inner, start_rel, start_uat, into_pro);
        return c;
    }

    private Criteria getCriteriaByGroup(Set<String> groupIds, boolean isIncludeChildren, Integer isDefered) throws Exception {
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(groupIds)) {
            c.and(Dict.GROUP).in(groupIds);
        }
        if (null != isDefered && isDefered == 1) {
            c.and("taskSpectialStatus").is(isDefered);
        }
        return c;
    }

    private Criteria getCriteriaByStage(Criteria group, List stage) {
        Criteria sit = null;
        Criteria uat = null;
        Criteria other = null;
        if (!CommonUtils.isNullOrEmpty(stage)) {
            group.andOperator(Criteria.where(Dict.STAGE).in(stage));
        } else {
            group.andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()));
        }
        return group;
    }

    /**
     * 查询任务
     *
     * @param task_id
     * @return
     */
    @Override
    public FdevTask selectTaskById(String task_id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(task_id));
        FdevTask result = mongoTemplate.findOne(query, FdevTask.class);
        return result;
    }

    /**
     * 更新
     *
     * @param featureId
     * @param webNameEn
     * @param sonarId
     * @return
     */
    @Override
    public long updateTaskByBranchAndWebName(String featureId, String webNameEn, String sonarId, String scanTime) {
        Query query = Query.query(Criteria.where(Dict.FEATURE_BRANCH).is(featureId))
                .addCriteria(Criteria.where(Dict.PROJECT_NAME).is(webNameEn))
                .addCriteria(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortAndNoFile()));
        Update sonarId1 = new Update()
                .set(Dict.FEATURE_BRANCH, featureId)
                .set(Dict.PROJECT_NAME, webNameEn)
                .set("sonarId", sonarId);
        if (CommonUtils.isNotNullOrEmpty(scanTime)) {
            sonarId1.set("sonarId", "");
            sonarId1.set("scanTime", scanTime);
        }
        UpdateResult updateResult = mongoTemplate.updateFirst(query, sonarId1, FdevTask.class);
        return Optional.ofNullable(updateResult.getMatchedCount()).orElse(0l);
    }

    public long updateDesign(Map<String, List<Map<String, String>>> designMap, String taskId) {
        Query query = Query.query(Criteria.where(Dict.ID).is(taskId));
        Update update = new Update()
                .set("designMap", designMap);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, FdevTask.class);
        return Optional.ofNullable(updateResult.getMatchedCount()).orElse(0l);
    }

    @Override
    public List<FdevTask> queryReviewDetailList(String reviewer, List<String> group, String startDate, String endDate) {
        Criteria c = Criteria.where("designMap").exists(true).ne(null)
                .and(Dict.STAGE).in(CommonUtils.getNoAbortList())
                .and("designMap.wait_allot.time").lte(endDate);
        if(CommonUtils.dateFormat(new Date(),CommonUtils.DATE_TIME_PATTERN).compareTo(startDate) >= 0) {
            c.orOperator(
                    Criteria.where("designMap.finished.time").ne(null).gte(startDate),
                    Criteria.where("designMap.finished.time").is(null)
            );
        }else {
            c.andOperator(Criteria.where("designMap.finished.time").ne(null).gte(startDate));
        }
        if (StringUtils.isNotBlank(reviewer)) c.and("reviewer").is(reviewer);
        if (!CommonUtils.isNullOrEmpty(group)) {
            c.and("group").in(group);
        }
        Query query = new Query(c);
        return mongoTemplate.find(query, FdevTask.class);
    }

    /**
     * 根据需求id（rqrmnt_id）查询该需求id的所有任务
     *
     * @param demandNo
     * @param isTodoStatus 是否查询为待实施的状态
     * @return
     */
    @Override
    public List<FdevTask> queryAllTaskByDemandNo(String demandNo, Boolean isTodoStatus) {
        List param = new ArrayList();
        if (isTodoStatus)
            param = CommonUtils.getWaitTodoList();
        else
            param = CommonUtils.getNoAbortTodoList();

        Query query = new Query(Criteria.where("rqrmnt_no").is(demandNo)
                .andOperator(Criteria.where(Dict.STAGE).in(param))
        );
        query.fields().exclude("_id");
        return this.mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryTaskByreadmine(String group) {
        List param = CommonUtils.getNoAbortTodoList();
        Query query = new Query(Criteria.where("redmine_id").is(group)
                .andOperator(Criteria.where(Dict.STAGE).in(param))
        );
        query.fields().exclude("_id");
        return this.mongoTemplate.find(query, FdevTask.class);

    }

    /**
     * 根据研发单元编号（unitNo）查询研发单元下的所有任务
     *
     * @param unitNo
     * @param isTodoStatus 是否查询为待实施的状态
     * @return
     */
    @Override
    public List<FdevTask> queryAllTaskByUnitNo(String unitNo, Boolean isTodoStatus) {
        List param = new ArrayList();
        if (isTodoStatus)
            param = CommonUtils.getWaitTodoList();
        else
            param = CommonUtils.getNoAbortTodoList();

        Query query = new Query(Criteria.where("fdev_implement_unit_no").is(unitNo)
                .andOperator(Criteria.where(Dict.STAGE).in(param))
        );
        query.fields().exclude("_id");
        return this.mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public Long deferOrRecoverTask(List<String> ids, Integer taskSpecialStatus) {
        Query q = Query.query(Criteria.where("fdev_implement_unit_no").in(ids));
        Update update = new Update();
        update.set("taskSpectialStatus", taskSpecialStatus);
        if (taskSpecialStatus == 1) {
            update.set("deferTime", CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
        } else if (taskSpecialStatus == 3) {
            update.set("recoverTime", CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
        }
        UpdateResult updateResult = mongoTemplate.updateMulti(q, update, FdevTask.class);
        return updateResult.getMatchedCount();
    }


    public void updateByFdevImplementUnitNo(String fdev_implement_unit_no, Map implement_unit_info) {
        Query q = Query.query(Criteria.where("fdev_implement_unit_no").is(fdev_implement_unit_no));
        Update update = new Update();
        update.set("plan_start_time", CommonUtils.dateParse((String) implement_unit_info.get("plan_start_date")));
        update.set("plan_inner_test_time", CommonUtils.dateParse((String) implement_unit_info.get("plan_inner_test_date")));
        update.set("plan_uat_test_start_time", CommonUtils.dateParse((String) implement_unit_info.get("plan_test_date")));
        update.set("plan_uat_test_stop_time", CommonUtils.dateParse((String) implement_unit_info.get("plan_test_finish_date")));
        update.set("plan_rel_test_time", CommonUtils.dateParse((String) implement_unit_info.get("plan_test_finish_date")));
        update.set("plan_fire_time", CommonUtils.dateParse((String) implement_unit_info.get("plan_product_date")));
        update.set("rqrmnt_no", implement_unit_info.get("demand_id"));
        mongoTemplate.updateMulti(q, update, FdevTask.class);
    }

    /**
     * 根据应用id查询阶段在
     *
     * @param project_id
     * @return
     */
    @Override
    public List<FdevTask> queryTaskIdsByProjectId(String project_id) {
        Criteria c = Criteria.where(Dict.PROJECT_ID).is(project_id).and(Dict.STAGE).in(new ArrayList<String>() {
            {
                add(Dict.TASK_STAGE_DEVELOP);
                add(Dict.TASK_STAGE_SIT);
                add(Dict.TASK_STAGE_UAT);
                add(Dict.TASK_STAGE_REL);
                add(Dict.TASK_STAGE_PRODUCTION);
            }
        });
        Query query = new Query(c);
        query.fields().include("id");
        return mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public FdevTask queryById(String taskId) {
        Query query = Query.query(Criteria.where("id").is(taskId));
        return mongoTemplate.findOne(query, FdevTask.class);
    }

    /**
     * 根据研发单元查询创建的任务状态为非删除废弃的数量
     *
     * @param fdev_implement_unit_no
     */
    @Override
    public Integer queryNotDiscarddnum(String fdev_implement_unit_no) {
        Criteria c = Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).is(fdev_implement_unit_no).and(Dict.STAGE).in(new ArrayList<String>() {
            {
                add(Dict.TASK_STAGE_FILE);
                add(Dict.TASK_STAGE_CREATE_INFO);
                add(Dict.TASK_STAGE_CREATE_APP);
                add(Dict.TASK_STAGE_CREATE_FEATURE);
                add(Dict.TASK_STAGE_DEVELOP);
                add(Dict.TASK_STAGE_SIT);
                add(Dict.TASK_STAGE_UAT);
                add(Dict.TASK_STAGE_REL);
                add(Dict.TASK_STAGE_PRODUCTION);
            }
        });
        Query query = new Query(c);
        return (int) mongoTemplate.count(query, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryTaskByStoryId(String storyId) {
        Query query = Query.query(Criteria.where(Dict.STORY_ID).is(storyId));
        return mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryByGroupId(List groupIds) {
        Criteria c = Criteria.where(Dict.GROUP).in(groupIds).and(Dict.STAGE).in(CommonUtils.getNoAbortList());
        Query query = new Query(c);
        query.fields().include("id").include("stage").include("plan_fire_time").include("fire_time").include("name");
        return mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public long queryTaskNum(Query query) {
        return mongoTemplate.count(query, "task");
    }

    @Override
    public <T> List<T> queryByQuery(Query query, Class<T> clazz) {
        return mongoTemplate.find(query, clazz);
    }

    @Override
    public List<FdevTask> queryUserProductionTask(String userId) {
        List<FdevTask> result = mongoTemplate.find(Query.query(
                Criteria.where(Dict.STAGE).is(Dict.TASK_STAGE_PRODUCTION).
                        orOperator(Criteria.where(Dict.SPDB_MASTER).all(userId),
                                Criteria.where(Dict.MASTER).all(userId),
                                Criteria.where(Dict.CREATOR).all(userId))),
                FdevTask.class);
        return result;
    }

    @Override
    public List<FdevTask> updateTaskStateToFileBatch(List<String> ids) {
        Query query = new Query(Criteria.where(Dict.ID).in(ids));
        Update update = new Update();
        update.set(Dict.STAGE, Dict.TASK_STAGE_FILE);
        mongoTemplate.updateMulti(query, update, FdevTask.class);
        return mongoTemplate.find(query, FdevTask.class);
    }

    /**
     * 根据研发单元编号（unitNo）查询研发单元下的所有任务
     *
     * @param unitNo
     * @return
     */
    @Override
    public List<FdevTask> queryAllTaskByUnitNo(String unitNo) {
        Query query = new Query(Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).is(unitNo));
        query.fields().exclude("_id");
        return this.mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryNotDiscardTask(List<String> fdev_implement_unit_no_list) {
        Criteria c = Criteria.where(Dict.FDEV_IMPLEMENT_UNIT_NO).in(fdev_implement_unit_no_list)
                .and(Dict.STAGE).in(CommonUtils.getNoAbortList());
        Query query = new Query(c);
        return mongoTemplate.find(query, FdevTask.class);
    }

    /**
     * 查询需求下的任务，不包含已删除已废弃的
     * @param demandNo
     * @return
     */
    @Override
    public List<FdevTask> queryAllTaskByDemandNo(String demandNo) {
        Query query = new Query(Criteria.where(Dict.RQRMNT_NO).is(demandNo)
                .andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortList()))
        );
        query.fields().exclude("_id");
        return this.mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryTaskByCodeOrderNo(String code_order_no) {
        Query query = new Query(Criteria.where(Dict.CODE_ORDER_NO).in(code_order_no)
                .andOperator(Criteria.where(Dict.STAGE).in(CommonUtils.getNoAbortList()))
        );
        query.fields().exclude("_id");
        query.with(new Sort(Sort.Direction.DESC, Dict.FIRE_TIME));
        return this.mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryUITask() {
        Query query = new Query(Criteria.where(Dict.DESIGNMAP).ne(null).and(Dict.UIVERIFYREPORTER).is(null));
        query.fields().exclude("_id");
        return this.mongoTemplate.find(query, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryAllTask() {
        return this.mongoTemplate.findAll(FdevTask.class);
    }

    @Override
    public void updateApplicationType(String taskId, String typeName) {
        Query query = new Query(Criteria.where(Dict.ID).is(taskId));
        Update update = new Update();
        update.set(Dict.APPLICATIONTYPE, typeName);
        mongoTemplate.findAndModify(query, update, FdevTask.class);
    }

    @Override
    public void updateInnerTestTime(FdevTask task) {
        Query query = new Query(Criteria.where(Dict.ID).is(task.getId()));
        Update update = new Update();
        update.set(Dict.STAGE,task.getStage());
        update.set(Dict.START_INNER_TEST_TIME, task.getStart_inner_test_time());
        update.set("skip_flag",task.getSkipFlag());
        mongoTemplate.findAndModify(query, update, FdevTask.class);
    }

    @Override
    public List<FdevTask> queryTaskInStage(List<String> stageList) {
        Query query = Query.query(Criteria.where(Dict.STAGE).in(stageList).and(Dict.PROJECT_ID).nin(null, ""));
        return mongoTemplate.find(query, FdevTask.class);
    }

}
