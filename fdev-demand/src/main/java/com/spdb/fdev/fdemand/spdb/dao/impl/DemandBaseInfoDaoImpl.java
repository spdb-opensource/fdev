package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.DemandEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DemandBaseInfoUtil;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.DesignDoc;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.TechType;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

@Repository
public class DemandBaseInfoDaoImpl implements IDemandBaseInfoDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestTransport restTransport;

    @Override
    public List query(DemandBaseInfo demandBaseInfo) throws Exception {
        List<DemandBaseInfo> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = demandBaseInfo == null ? "{}" : objectMapper.writeValueAsString(demandBaseInfo);
        BasicDBObject queryJson = BasicDBObject.parse(json);
        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        if (null != demandBaseInfo && null == demandBaseInfo.getDemand_status_normal()) {
            c.andOperator(Criteria.where(Dict.DEMAND_STATUS_NORMAL).ne(DemandEnum.DemandStatusEnum.IS_CANCELED.getValue()));
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<DemandBaseInfo> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.DEMAND_BASEINFO, DemandBaseInfo.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public List query2() {
        return mongoTemplate.findAll(DemandBaseInfo.class);
    }


    /**
     * 需求录入
     */
    @Override
    public DemandBaseInfo save(DemandBaseInfo demandBaseInfo) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        demandBaseInfo.set_id(objectId);
        demandBaseInfo.setId(id);
        return mongoTemplate.save(demandBaseInfo);
    }

    /**
     * 根据需求类型，查询最新科技需求编号
     */
    @Override
    public DemandBaseInfo queryBydemandType(String demandType) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + demandType + ".*$");
        Calendar calendar = Calendar.getInstance();
        Criteria criteria = Criteria.where(Dict.DEMAND_TYPE).regex(pattern);
        Criteria criteriaTwo = new Criteria();
        if (Dict.BUSINESS.equalsIgnoreCase(demandType)) {
        	criteriaTwo = Criteria.where(Dict.OA_CONTACT_NO).regex(calendar.get(Calendar.YEAR) + "-QTXM" + "-").andOperator(Criteria.where("demand_status_normal").ne(9));
		}else if (Dict.TECH.equalsIgnoreCase(demandType)) {
			criteriaTwo = Criteria.where(Dict.OA_CONTACT_NO).regex("KEJI-" + calendar.get(Calendar.YEAR) + "-").andOperator(Criteria.where("demand_status_normal").ne(9));
		}else if(Dict.DAILY.equalsIgnoreCase(demandType)){
            criteriaTwo = Criteria.where(Dict.OA_CONTACT_NO).regex("DAILY-" + calendar.get(Calendar.YEAR) + "-").andOperator(Criteria.where("demand_status_normal").ne(9));
        } else {
            throw new FdevException(ErrorConstants.DEMAND_TYPE_ERROR);
        }
        query.addCriteria(criteria);
        query.addCriteria(criteriaTwo);
        query.with(new Sort(Sort.Direction.DESC, Dict.OA_CONTACT_NO));
        return mongoTemplate.findOne(query, DemandBaseInfo.class);
    }


    /**
     * 根据需求编号，查询需求的数量
     */
    @Override
    public Long countOa_contact_no(String oa_contact_no) throws Exception {
        Query q = Query.query(Criteria.where(Dict.OA_CONTACT_NO).is(oa_contact_no).andOperator(Criteria.where("demand_status_normal").ne(9)));
        Long count;
        try {
            count = mongoTemplate.count(q, DemandBaseInfo.class);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return count;
    }

    @Override
    public DemandBaseInfo updateUploader(String firstUploader,String demandId,String uploader) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(demandId));
        if (CommonUtils.isNullOrEmpty(uploader)) {
            throw new FdevException(ErrorConstants.UPLOADER_CANNOT_BE_EMPTY, new String[]{"UI设计稿上传人"});
        }
        Update update = Update.update(Dict.ID, demandId);
        update.set("firstUploader", uploader);
        mongoTemplate.upsert(query, update, DemandBaseInfo.class);
        return this.mongoTemplate.findAndModify(query, update, DemandBaseInfo.class);
    }

    @Override
    public DemandBaseInfo updateDesign_status(String demandId,String design_status) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(demandId));
        Update update = Update.update(Dict.ID, demandId);
        update.set(Dict.DESIGN_STATUS, design_status);
        return this.mongoTemplate.findAndModify(query, update, DemandBaseInfo.class);
    }

    @Override
    public DemandBaseInfo update(DemandBaseInfo demandBaseInfo) throws Exception {
        //替换时间格式
        Query query = Query.query(Criteria.where(Dict.ID).is(demandBaseInfo.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(demandBaseInfo);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, demandBaseInfo.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            if (Dict.DEMAND_FLAG.equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, DemandBaseInfo.class);
        return this.mongoTemplate.findOne(query, DemandBaseInfo.class);
    }

    @Override
    public DemandBaseInfo updateImpl(DemandBaseInfo demandBaseInfo) throws Exception {
        //替换时间格式
        demandBaseInfo.setAccept_date(TimeUtil.timeConvertNew(demandBaseInfo.getAccept_date()));
        demandBaseInfo.setOa_receive_date(TimeUtil.timeConvertNew(demandBaseInfo.getOa_receive_date()));
        demandBaseInfo.setRespect_product_date(TimeUtil.timeConvertNew(demandBaseInfo.getRespect_product_date()));
        Query query = Query.query(Criteria.where(Dict.ID).is(demandBaseInfo.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(demandBaseInfo);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, demandBaseInfo.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            if (Dict.DEMAND_FLAG.equals(key)) {
                continue;
            }
            if(Dict.DEMAND_LEADER_ALL.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            if(Dict.DEMAND_LEADER.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            if(Dict.ACCEPT_DATE.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            if(Dict.PROPOSE_DEMAND_USER.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            if(Dict.PROPOSE_DEMAND_DEPT.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            if(Dict.DEMAND_CREATE_USER_ALL.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            if(Dict.OA_CONTACT_NO.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, DemandBaseInfo.class);
        return this.mongoTemplate.findOne(query, DemandBaseInfo.class);
    }

    @Override
    public DemandBaseInfo queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, DemandBaseInfo.class);
    }

    @Override
    public List queryPagination(Integer start, Integer pageSize, String keyword) {
        Query query = new Query();
        //搜索框过滤
        if (!CommonUtils.isNullOrEmpty(keyword)) {
            Pattern pattern = Pattern.compile("^.*" + keyword + ".*$");
            Criteria criteria = Criteria.where("_id").ne(null);
            criteria.orOperator(Criteria.where(Dict.DEMAND_INSTRUCTION).regex(pattern),
                    Criteria.where(Dict.DEMAND_PLAN_NO).regex(pattern));
            query.addCriteria(criteria);
        }
        if (!pageSize.toString().equals("0")) {
            query.limit(pageSize).skip(start);  //分页
        }
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, DemandBaseInfo.class);
    }

    @Override
    public List<DemandBaseInfo> queryDemandByIds(Set<String> ids) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(Dict.ID).in(ids);
        query.addCriteria(criteria);
        return this.mongoTemplate.find(query, DemandBaseInfo.class);
    }

    /**
     * 根据组ids来查询需求
     *
     * @param groupId
     * @return
     */
    @Override
    public List<DemandBaseInfo> queryDemandByGroupId(Set<String> groupId) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("demand_leader_group").in(groupId);
//        criteria.orOperator(Criteria.where("demand_leader_group").in(groupId), Criteria.where("relate_part").in(groupId));
        criteria.and("is_canceled").ne(true);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, "demand_create_time"));
        List<DemandBaseInfo> resList;
        try {
            resList = mongoTemplate.find(query, DemandBaseInfo.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        return resList;
    }

    /**
     * 根据组ids和优先级查询需求
     *
     * @param groupids 组ids
     * @param priority 优先级
     * @param isParent 是否包含子组
     * @return
     * @throws Exception
     */
    @Override
    public List<DemandBaseInfo> queryDemandByGroupIdAndPriority(Set<String> groupids, String priority, Boolean isParent) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("demand_leader_group").in(groupids);
//        criteria.orOperator(Criteria.where("demand_leader_group").in(groupids), Criteria.where("relate_part").in(groupids));
        criteria.and(Dict.PRIORITY).is(priority);
        criteria.and("is_canceled").ne(true);
        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.DESC, "demand_create_time"));
        List<DemandBaseInfo> resList;
        try {
            resList = mongoTemplate.find(query, DemandBaseInfo.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        return resList;
    }

    @Override
    public Long queryCount(String keyword) {
        Query q = new Query();
        //搜索框过滤
        if (!CommonUtils.isNullOrEmpty(keyword)) {
            Pattern pattern = Pattern.compile("^.*" + keyword + ".*$");
            Criteria criteria = Criteria.where("_id").ne(null);
            criteria.orOperator(Criteria.where(Dict.DEMAND_INSTRUCTION).regex(pattern),
                    Criteria.where(Dict.DEMAND_PLAN_NO).regex(pattern));
            q.addCriteria(criteria);
        }
        return mongoTemplate.count(q, DemandBaseInfo.class);
    }

    /**
     * 查询需求列表
     */
    @Override
    public List<DemandBaseInfo> queryDemandList(String isCodeCheck,String userid, String keyword,
                                                Map delayDate,
                                                List<String> groupid,
                                                List<String> state, List<HashMap> stateListMap,
                                                Map featureDate,
                                                String type, String sortBy, Boolean descending,
                                                String priority,
                                                Map relDateMap,
                                                String groupState,
                                                String designState,List<String> demand_label) throws Exception {
        Query q = makeQueryRqrmnts(isCodeCheck,
                userid, keyword,
                delayDate,
                groupid,
                state, stateListMap,
                featureDate,
                type,
                sortBy, descending,
                priority,
                relDateMap,
                groupState,
                designState,demand_label);

        List<DemandBaseInfo> list;
        try {
            list = mongoTemplate.find(q, DemandBaseInfo.class);

        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return list;

    }

    @Override
    public List<DemandBaseInfo> queryDemandBaseInfos(String isCodeCheck,Integer start, Integer pageSize,
                                                     String userid, String keyword,
                                                     Map delayDate,
                                                     List<String> groupid,
                                                     List<String> state, List<HashMap> stateListMap,
                                                     Map featureDate,
                                                     String type, String sortBy, Boolean descending,
                                                     String priority,
                                                     Map relDateMap,
                                                     String groupState,
                                                     String designState,List<String> demand_label) throws Exception {
        Query q = makeQueryRqrmnts(isCodeCheck,userid, keyword,
                delayDate,
                groupid,
                state, stateListMap,
                featureDate,
                type, sortBy, descending,
                priority,
                relDateMap,
                groupState,
                designState,demand_label);

        //q.limit(pageSize).skip(start);  //分页
        List<DemandBaseInfo> datas = new ArrayList<>();
        try {
            List<DemandBaseInfo> list = mongoTemplate.find(q, DemandBaseInfo.class);
            if (!CommonUtils.isNullOrEmpty(state)) {
                if (state.contains(9)) {
                    //存量数据迁移
                    for (DemandBaseInfo rqrmntForUI : list) {
                        if (9 == rqrmntForUI.getDemand_status_normal()) {//只迁移撤销的需求

                        }
                    }
                }

                List<DemandBaseInfo> repealList = mongoTemplate.find(q, DemandBaseInfo.class, "rqrmnts_repeal");
                for (DemandBaseInfo rqrmntForUI : repealList) {
                    if (!list.contains(rqrmntForUI)) {
                        list.add(rqrmntForUI);
                    }
                }
            }
            if (list.size() > 0) {
                if ((list.size() > pageSize && start == 0)) {
                    for (int i = 0; i < pageSize; i++) {
                        if (!datas.contains(list.get(i))) {
                            datas.add(list.get(i));
                        }
                    }
                } else if ((list.size() < pageSize || list.size() == pageSize) && start == 0) {
                    for (int i = 0; i < list.size(); i++) {
                        if (!datas.contains(list.get(i))) {
                            datas.add(list.get(i));
                        }
                    }
                } else if ((list.size() > (pageSize + start) && start != 0)) {
                    for (int i = start; i < pageSize + start; i++) {
                        if (!datas.contains(list.get(i))) {
                            datas.add(list.get(i));
                        }
                    }
                } else if ((list.size() < (pageSize + start) || list.size() == (pageSize + start)) && start != 0) {
                    for (int i = start; i < list.size(); i++) {
                        if (!datas.contains(list.get(i))) {
                            datas.add(list.get(i));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return datas;
    }

    /**
     * 组装过滤条件
     *
     * @param userid       所选用户
     * @param keyword      输入框
     * @param delayDate    延期日期过滤
     * @param groupids     过滤组
     * @param stateList    过滤状态
     * @param stateListMap 状态时间过滤
     * @param featureDate  欲进行时间
     * @param type         过滤需求类型
     * @param sortBy       排序字段名
     * @param descending   是否降序
     * @param priority     优先级
     */
    private Query makeQueryRqrmnts(String isCodeCheck,String userid, String keyword,
                                   Map delayDate,
                                   List<String> groupids,
                                   List<String> stateList, List<HashMap> stateListMap,
                                   Map featureDate,
                                   String type, String sortBy, Boolean descending,
                                   String priority,
                                   Map relDateMap,
                                   String groupState,
                                   String designState,List<String> demand_label) throws Exception {
        Query q = new Query();
        Criteria[] criteriaArr = new Criteria[]{};
        List<Criteria> listCriteria = new ArrayList<>();
        List<List<Criteria>> orList = new ArrayList<>();
        List<Criteria> andList = new ArrayList<>();
        List<Criteria> stateS = new ArrayList<Criteria>();

        //过滤需求状态
        Criteria rqrmntCriteria = null;
        if (!CommonUtils.isNullOrEmpty(stateList)) {
            criteriaArr = new Criteria[]{};
            listCriteria = new ArrayList<>();
            for (HashMap itemState : stateListMap) {
                Criteria stateDateCri = null;
                if ("defer".equals(itemState.get("state"))) {
                    int[] stateSpe = {1, 2};
                    stateDateCri = Criteria.where("demand_status_special").in(stateSpe);
                } else {
                    Integer state = Integer.valueOf((Integer) itemState.get("state"));

                    String stateDateType = (String) itemState.get("stateDateType");
                    if (!CommonUtils.isNullOrEmpty(stateDateType)) {
                        String calDa = (String) itemState.get("stateDate");
                        stateDateCri = Criteria.where("demand_status_normal").is(state).andOperator(Criteria.where(stateDateType).lte(calDa));
                    } else {
                        if (rqrmntCriteria == null)
                            rqrmntCriteria = Criteria.where("demand_status_normal").is(state);
                        stateDateCri = Criteria.where("demand_status_normal").is(state);
                    }
                }

                if (stateDateCri != null)
                    listCriteria.add(stateDateCri);
            }

            criteriaArr = listCriteria.toArray(criteriaArr);
            if (rqrmntCriteria == null && !CommonUtils.isNullOrEmpty(criteriaArr)) {
                orList.add(listCriteria);
            } else if (rqrmntCriteria != null && !CommonUtils.isNullOrEmpty(criteriaArr)) {
                orList.add(listCriteria);
            }
            List<Integer> normal = new ArrayList<Integer>();

            for (String State : stateList) {
                if ("defer".equals(State)) {
                    rqrmntCriteria = Criteria.where("demand_status_special").in(1, 2);
                    stateS.add(rqrmntCriteria);
                } else {

                    normal.add(Integer.valueOf(State));
                }
            }
            if (!CommonUtils.isNullOrEmpty(normal)) {
                rqrmntCriteria = Criteria.where("demand_status_normal").in(normal).andOperator(Criteria.where("demand_status_special").nin(1, 2));
                stateS.add(rqrmntCriteria);
            }

        } else {
            if (!("abnormalShutdown".equalsIgnoreCase(designState))){
                rqrmntCriteria = Criteria.where("demand_status_normal").nin(8, 9);
                q.addCriteria(rqrmntCriteria);
            }
        }
        if (stateS.size() != 0) {
            orList.add(stateS);
        }


        //过滤人员  ------ 存在与 我相关，，，的冲突bug 。。。待修复
        if (!CommonUtils.isNullOrEmpty(userid)) {
            listCriteria = new ArrayList<>();
            Criteria criteria = new Criteria();
            Criteria criteria1 = new Criteria();
            if (!CommonUtils.isNullOrEmpty(userid)) {
                criteria.in(userid);
                criteria1.and("assess_user").in(userid);
            }
            listCriteria.add(Criteria.where("demand_create_user").is(userid));
            listCriteria.add(Criteria.where("demand_leader").elemMatch(criteria));
            listCriteria.add(Criteria.where("relate_part_detail").elemMatch(criteria1));

            orList.add(listCriteria);
        }
        //搜索框过滤
        if (!CommonUtils.isNullOrEmpty(keyword)) {
            listCriteria = new ArrayList<>();
            Pattern pattern = Pattern.compile("^.*" + keyword + ".*$");
            listCriteria.add(Criteria.where("oa_contact_no").regex(pattern));
            listCriteria.add(Criteria.where("oa_contact_name").regex(pattern));
            orList.add(listCriteria);
        }

        /**
         * 延期需求，过滤
         *  计算规则，，，   预计时间 《= 今天（最大）
         *      且：对应的实际日期 为空，或者为null
         */
        if (!CommonUtils.isNullOrEmpty(delayDate)) {
            listCriteria = new ArrayList<>();
            String dateType = (String) delayDate.get("datetype");

            String relDateType = DemandBaseInfoUtil.getRelDateKey(dateType);
            listCriteria.add(Criteria.where(relDateType).is(""));
            listCriteria.add(Criteria.where(relDateType).is(null));
            orList.add(listCriteria);


            q.addCriteria(Criteria.where(dateType).lte(delayDate.get("gteDate")));
        }
        /**
         * 预进行计划 时间过滤
         *  计算规则，，，   今天（最小）<= 预计时间 <= 今天（最大） + 数字天数
         */
        if (!CommonUtils.isNullOrEmpty(featureDate)) {
            listCriteria = new ArrayList<>();
            String featureType = (String) featureDate.get("datetype");
            String gteDate = (String) featureDate.get("gteDate");
            String lteDate = (String) featureDate.get("lteDate");
            String relDateType = DemandBaseInfoUtil.getRelDateKey(featureType);
            listCriteria.add(Criteria.where(relDateType).is(""));
            listCriteria.add(Criteria.where(relDateType).is(null));
            orList.add(listCriteria);
            andList.add(Criteria.where(featureType).gte(lteDate));
            andList.add(Criteria.where(featureType).lte(gteDate));
        }

        //过滤需求实际时间
        /**
         * 计算规则：        输入查询的实际起始日期<=需求对应的实际时间<=输入查询的实际结束日期
         */
        if (!CommonUtils.isNullOrEmpty(relDateMap)) {
            listCriteria = new ArrayList<>();
            List<String> relDateType = (List<String>) relDateMap.get("relDateType");
            String startDate = (String) relDateMap.get("relStartDate");
            String endDate = (String) relDateMap.get("relEndDate");

            for (String typeItem : relDateType) {
                if ("demand_create_time".equals(typeItem)) {
                    endDate = endDate + "999";
                }
                listCriteria.add(Criteria.where(typeItem).gte(startDate).lte(endDate));
            }
            orList.add(listCriteria);
        }

        //统合 or 条件
        if (!CommonUtils.isNullOrEmpty(orList)) {
            Criteria[] arr = new Criteria[]{};
            List<Criteria> listCr = new ArrayList<>();
            for (List<Criteria> item : orList) {
                criteriaArr = new Criteria[]{};
                criteriaArr = item.toArray(criteriaArr);
                listCr.add(new Criteria().orOperator(criteriaArr));
            }

            if (!CommonUtils.isNullOrEmpty(andList))
                listCr.addAll(andList);
            //如果涉及计划时间的过滤，那么需要过滤各个 启动时间都不能为空 即 使用了 延期或者 预进行过滤
            if (!CommonUtils.isNullOrEmpty(featureDate) || !CommonUtils.isNullOrEmpty(delayDate)) {
                List filterList = new ArrayList();
                //filterList.add(null);
                filterList.add("");
                if (!CommonUtils.isNullOrEmpty(featureDate)) {
                    listCr.add(Criteria.where((String) featureDate.get(Dict.DATETYPE)).nin(filterList));
                }
                if (!CommonUtils.isNullOrEmpty(delayDate)) {
                    listCr.add(Criteria.where((String) delayDate.get(Dict.DATETYPE)).nin(filterList));
                }

            }
            arr = listCr.toArray(arr);
            q.addCriteria(new Criteria().andOperator(arr));
        }

        //需求标签
        if ( !CommonUtils.isNullOrEmpty(demand_label) ) {
            q.addCriteria(Criteria.where(Dict.DEMAND_LABEL).in(demand_label));
        }

        //过滤组
        if (!CommonUtils.isNullOrEmpty(groupids) && groupids.size() > 0) {
            if (!CommonUtils.isNullOrEmpty(groupState)) {
                if ("0".equals(groupState)) {
                    Criteria criteria = Criteria.where("demand_leader_group").in(groupids);
                    q.addCriteria(criteria);
                }
                if ("1".equals(groupState)) {

                    Criteria c1 = new Criteria();
                    c1.in(groupids);
                    Criteria criteria = Criteria.where("relate_part").elemMatch(c1);
                    q.addCriteria(criteria);

                }
            } else {
                Criteria criteria = Criteria.where("relate_part").in(groupids);
                q.addCriteria(criteria);
            }

        }
        //过滤需求类型
        if (!CommonUtils.isNullOrEmpty(type)) {
            q.addCriteria(Criteria.where("demand_type").is(type));
        }
        //过滤ui设计稿类型
        if (!CommonUtils.isNullOrEmpty(designState)) {
            q.addCriteria(Criteria.where("design_status").is(designState));
        }
        //根据字段排序
        if (!CommonUtils.isNullOrEmpty(sortBy)) {
            if (descending) {
                q.with(new Sort(Sort.Direction.DESC, sortBy));
            } else {
                q.with(new Sort(Sort.Direction.ASC, sortBy));
            }
        } else {
            q.with(new Sort(Sort.Direction.DESC, "demand_create_time"));
        }
        //过滤优先级
        if (!CommonUtils.isNullOrEmpty(priority)) {
            q.addCriteria(Criteria.where("priority").is(priority));
        }


        if ( "0".equals(isCodeCheck) ) {
            q.addCriteria(Criteria.where(Dict.CODE_ORDER_NO).nin(null,new HashSet<String>()));
        } else if ( "1".equals(isCodeCheck) ){
            q.addCriteria(Criteria.where(Dict.CODE_ORDER_NO).is(null));
        }

        q.fields().exclude("_id");
        return q;

    }

    @Override
    public List<FdevImplementUnit> queryAvailableIpmpUnit() {
        Query query = new Query(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL)
                .nin(new Integer[]{DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getValue(), DemandEnum.DemandStatusEnum.IS_CANCELED.getValue(),0}));
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }

    /**
     * 根据需求查询实施单元，过滤撤销和评估中
     *
     * @param set
     * @return
     */
    @Override
    public List<FdevImplementUnit> queryAvailableIpmpUnit(HashSet<String> set) {
        Query query = new Query(Criteria.where(Dict.IMPLEMENT_UNIT_STATUS_NORMAL)
                .nin(new Integer[]{DemandEnum.DemandStatusEnum.EVALUATE.getValue(), DemandEnum.DemandStatusEnum.IS_CANCELED.getValue(),0})
                .and(Dict.DEMAND_ID).in(set));
        return mongoTemplate.find(query, FdevImplementUnit.class);
    }


    @Override
    public DemandBaseInfo queryDemandByImplUnitsId(String demandId) {
        Query query = new Query();
        DemandBaseInfo resList = new DemandBaseInfo();
        try {
            query.addCriteria(Criteria.where(Dict.ID).in(demandId));
            resList = mongoTemplate.findOne(query, DemandBaseInfo.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException("出错");
        }
        return resList;
    }

    @Override
    public DemandBaseInfo queryDemandByImplUnitsIdAndPriority(String demandId, String priority) {
        Query query = new Query();
        DemandBaseInfo resList = new DemandBaseInfo();
        try {
            query.addCriteria(Criteria.where(Dict.ID).in(demandId));
            query.addCriteria(Criteria.where(Dict.PRIORITY).is(priority));
            resList = mongoTemplate.findOne(query, DemandBaseInfo.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return resList;
    }

    /**
     * 修改ui设计稿审核人
     */
    @Override
    public DemandBaseInfo updateRqrmntUiReporter(String demandId, String uiDesignReporter) {
        Query query = new Query(Criteria.where(Dict.ID).is(demandId));
        Update update = Update.update("ui_verify_user", uiDesignReporter);
        try {
            mongoTemplate.findAndModify(query, update, DemandBaseInfo.class);
            return mongoTemplate.findOne(query, DemandBaseInfo.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
    }

    /**
     * @// TODO: 2020/11/6  entity add
     * <p>
     * 修改ui设计稿审核人
     */
    @Override
    public Long updateDesignAndMap(String demandId, String newStatus, Map designMap, String uploader) {
        Query query = new Query(Criteria.where(Dict.ID).is(demandId));
        Update update = Update.update("design_status", newStatus)
                .set("designMap", designMap)
                .set("uploader", uploader);
        if (!CommonUtils.isNullOrEmpty(designMap.get("finished")))
            update.set("is_verify", true);
        try {
            return mongoTemplate.updateFirst(query, update, DemandBaseInfo.class).getMatchedCount();
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
    }

    /**
     * 更新审核意见
     */
    @Override
    public Long updateDesignRermark(String demandId, String desingRemark) {
        Query query = new Query(Criteria.where(Dict.ID).is(demandId));
        Update update = Update.update("designRemark", desingRemark);
        try {
            return mongoTemplate.updateFirst(query, update, DemandBaseInfo.class).getMatchedCount();
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
    }

    @Override
    public List<DemandBaseInfo> queryDemandByGroupId(List<String> groupIds) {
        Query query = new Query();
        ArrayList<Object> normalState = new ArrayList<>();
        normalState.add(DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue());
        normalState.add(DemandEnum.DemandStatusEnum.DEVELOP.getValue());
        normalState.add(DemandEnum.DemandStatusEnum.SIT.getValue());
        ArrayList<Object> specialState = new ArrayList<>();
        specialState.add(DemandEnum.DemandDeferStatus.DEFER.getValue());
        specialState.add(DemandEnum.DemandDeferStatus.RECOVER.getValue());
        query.addCriteria(Criteria.where(Dict.DEMAND_LEADER_GROUP).in(groupIds).orOperator(Criteria.where(Dict.RELATE_PART).in(groupIds)));
        query.addCriteria(Criteria.where(Dict.DEMAND_STATUS_NORMAL).in(normalState));
        query.addCriteria(Criteria.where(Dict.DEMAND_STATUS_SPECIAL).nin(specialState));
        query.addCriteria(Criteria.where(Dict.IS_CANCELED).ne(true));
        return mongoTemplate.find(query, DemandBaseInfo.class);
    }

    @Override
    public Long queryDemandsCount(String isCodeCheck,String userid, String keyword,
                                  Map delayDate,
                                  List<String> groupid,
                                  List<String> state, List<HashMap> stateListMap,
                                  Map featureDate,
                                  String type, String sortBy, Boolean descending,
                                  String priority,
                                  Map relDateMap,
                                  String groupState,
                                  String designState,List<String> demand_label) throws Exception {
        Query q = makeQueryRqrmnts(isCodeCheck,userid, keyword,
                delayDate,
                groupid,
                state, stateListMap,
                featureDate,
                type, sortBy, descending,
                priority,
                relDateMap,
                groupState,
                designState,demand_label);
        Long count = null;
        try {
            count = mongoTemplate.count(q, DemandBaseInfo.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return count;
    }

    @Override
    public List<DemandBaseInfo> queryTaskForDesign() {
        Query query = Query.query(Criteria.where("ui_verify").is(true));
        List<DemandBaseInfo> demandBaseInfos = mongoTemplate.find(query, DemandBaseInfo.class);
        return demandBaseInfos;
    }

    @Override
    public long updateDesignDoc(String id, DesignDoc designDoc) {
        DemandBaseInfo demandBaseInfo = queryById(id);
        List<DesignDoc> designDocs = Optional.ofNullable(demandBaseInfo.getDesignDoc()).orElse(new ArrayList<>());

        designDocs.add(designDoc);
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = new Update()
                .set("designDoc", designDocs);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, DemandBaseInfo.class);
        return updateResult.getMatchedCount();
    }

    /**
     * 根据审核人和牵头小组查需求
     *
     * @return
     */
    @Override
    public List<DemandBaseInfo> queryReviewDetailLists(String ui_verify_user, List<String> groupIds, String startDate, String endDate) {
        List<DemandBaseInfo> result = new ArrayList<>();
        Query q= new Query();
        Criteria c = Criteria.where("designMap").exists(true).ne(null)
                .and("demand_status_normal").ne(9)
                .and("designMap.wait_allot.time").lte(endDate);
        if (!CommonUtils.isNullOrEmpty(groupIds)) {
            c.and("demand_leader_group").in(groupIds);
        }
        if(CommonUtils.dateFormat(new Date(),CommonUtils.STANDARDDATEPATTERN).compareTo(startDate) >= 0) {
            c.orOperator(
                    Criteria.where("designMap.finished.time").ne(null).gte(startDate),
                    Criteria.where("designMap.finished.time").is(null)
            );
        }else {
            c.andOperator(Criteria.where("designMap.finished.time").ne(null).gte(startDate));
        }

        q.addCriteria(c);
        if(!CommonUtils.isNullOrEmpty(ui_verify_user))
        {
            q.addCriteria(Criteria.where("ui_verify_user").is(ui_verify_user));
        }
        q.fields().include("designMap");
        q.fields().include("ui_verify_user");
        q.fields().include("demand_leader_group");
        q.fields().include("demand_status_normal");
        q.fields().include("demand_status_special");
        q.fields().include("oa_contact_no");
        q.fields().include("oa_contact_name");
        q.fields().include("id");
        q.fields().include("firstUploader");
        q.fields().include("plan_inner_test_date");
        q.fields().include("plan_test_date");
        q.fields().include("real_inner_test_date");
        q.fields().include("real_test_date");
        q.fields().exclude("_id");
        List<DemandBaseInfo> demandBaseInfos = mongoTemplate.find(q, DemandBaseInfo.class);
        return demandBaseInfos;
    }

    /**
     * 根据审核人和牵头小组查需求
     *
     * @return
     */
    @Override
    public List<DemandBaseInfo> queryReviewDetailList(String ui_verify_user, List<String> demand_leader_group) {
        List<DemandBaseInfo> result = new ArrayList<>();
        Query q= new Query();
        Criteria c = Criteria.where("designMap").exists(true).ne(null).andOperator(Criteria.where("demand_status_normal").ne(9));
        q.addCriteria(c);
        if(!CommonUtils.isNullOrEmpty(ui_verify_user))
        {
            Criteria c1 = Criteria.where("ui_verify_user").is(ui_verify_user);
            q.addCriteria(c1);
        }
        if(!CommonUtils.isNullOrEmpty(demand_leader_group)){
            Criteria c1 = Criteria.where("demand_leader_group").in(demand_leader_group);
            q.addCriteria(c1);
        }
        q.fields().include("designMap");
        q.fields().include("ui_verify_user");
        q.fields().include("demand_leader_group");
        q.fields().include("demand_status_normal");
        q.fields().include("demand_status_special");
        q.fields().include("oa_contact_no");
        q.fields().include("oa_contact_name");
        q.fields().include("id");
        q.fields().exclude("_id");
        List<DemandBaseInfo> demandBaseInfos = mongoTemplate.find(q, DemandBaseInfo.class);
        return demandBaseInfos;
    }


    @Override
    @LazyInitProperty(redisKeyExpression = "fdemand.group.{param.id}")
    public Map queryGroup(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id "});
        }
        param.put(Dict.REST_CODE, "queryGroup");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"DEMAND->USER 查询小组返回为空"});
        }
        ArrayList jsonArray = (ArrayList) result;
        return (Map) jsonArray.get(0);
    }

    @Override
    public long countDemandByGroup(Set<String> groupIdsOne, String startDate, String endDate) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(Dict.DEMAND_STATUS_NORMAL).in(7,8);
        criteria.and(Dict.DEMAND_STATUS_SPECIAL).nin(1,2);
        criteria.and(Dict.DEMAND_LEADER_GROUP).in(groupIdsOne);
        criteria.andOperator(Criteria.where(Dict.REAL_PRODUCT_DATE).gte(startDate),
                Criteria.where(Dict.REAL_PRODUCT_DATE).lte(endDate));
        query.addCriteria(criteria);
        return mongoTemplate.count(query, DemandBaseInfo.class);
    }
    /**
     * 查询全量实施中需求
     *
     * @param
     * @return
     */
    @Override
    public List<DemandBaseInfo> queryImpingDemand(List<String> groupIdList) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        //过滤 7：已投产、8：已归档 9：已撤销
        criteria.and(Dict.DEMAND_STATUS_NORMAL).nin(7,8,9);
        //过滤 1：暂缓中、2：恢复中
        criteria.and(Dict.DEMAND_STATUS_SPECIAL).nin(1,2);
        criteria.and(Dict.DEMAND_LEADER_GROUP).in(groupIdList);
        query.addCriteria(criteria);
        List<DemandBaseInfo> resList;
        try {
            resList = mongoTemplate.find(query, DemandBaseInfo.class);
        } catch (Exception e) {
            logger.error("----->" + e.getStackTrace());
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return resList;
    }

    @Override
    public List<DemandBaseInfo> queryDemandByGroupIdAssessOver(Set<String> groupIds,int todayLast) {
        Query query = new Query();
        ArrayList<Object> specialState = new ArrayList<>();
        specialState.add(DemandEnum.DemandDeferStatus.DEFER.getValue());
        specialState.add(DemandEnum.DemandDeferStatus.RECOVER.getValue());
        String assessOver = TimeUtil.todayLastTwoWeek(todayLast);
        query.addCriteria(Criteria.where(Dict.DEMAND_LEADER_GROUP).in(groupIds));
        query.addCriteria(Criteria.where(Dict.DEMAND_STATUS_NORMAL).is(DemandEnum.DemandStatusEnum.EVALUATE.getValue()));
        query.addCriteria(Criteria.where(Dict.DEMAND_STATUS_SPECIAL).nin(specialState));
        query.addCriteria(Criteria.where(Dict.DEMAND_ASSESS_DATE).lt(assessOver));
        query.addCriteria(Criteria.where(Dict.ASSESS_FLAG).is(Constants.YES));
        return mongoTemplate.find(query, DemandBaseInfo.class);
    }

    @Override
    public DemandBaseInfo queryByOaContactNo(String oaContactNo) {
        Query query = new Query(Criteria.where(Dict.OA_CONTACT_NO).is(oaContactNo));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, DemandBaseInfo.class);
    }

    @Override
    public DemandBaseInfo updateDemandBaseInfo(DemandBaseInfo demandBaseInfo) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(demandBaseInfo.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(demandBaseInfo);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, demandBaseInfo.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            if(Dict.OA_CONTACT_NO.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, DemandBaseInfo.class);
        return this.mongoTemplate.findOne(query, DemandBaseInfo.class);
    }

    @Override
    public List<TechType> queryTechType(Map<String, Object> param) throws Exception {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, TechType.class);
    }

    @Override
    public DemandBaseInfo queryByOaContactNoAndStatus(String oaContactNo, int status) {
        Query query = new Query(Criteria.where(Dict.OA_CONTACT_NO).is(oaContactNo));
        query.addCriteria(Criteria.where(Dict.DEMAND_STATUS_NORMAL).ne(status));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, DemandBaseInfo.class);
    }

    @Override
    public List<DemandBaseInfo> queryDemandByCodeOrderNo(String no) {
        Query query = new Query(Criteria.where(Dict.CODE_ORDER_NO).in(no));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, DemandBaseInfo.class);
    }

    @Override
    public List<DemandBaseInfo> queryDemandBaseList(String demandKey) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if(!CommonUtils.isNullOrEmpty(demandKey)){
            Pattern pattern = Pattern.compile("^.*" + demandKey + ".*$");
            criteria.orOperator(Criteria.where(Dict.OA_CONTACT_NO).regex(pattern),
                    Criteria.where(Dict.OA_CONTACT_NAME).regex(pattern));
        }
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, DemandBaseInfo.class);
    }

    @Override
    public List<DemandBaseInfo> queryByIds(List<String> ids) {
        Query query = new Query(Criteria.where(Dict.ID).in(ids));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, DemandBaseInfo.class);
    }

    @Override
    public List<DemandBaseInfo> queryByKeyWord(String keyWord) {
        Pattern pattern = Pattern.compile("^.*" + keyWord + ".*$");
        Query query = new Query(new Criteria().orOperator(Criteria.where(Dict.OA_CONTACT_NO).regex(pattern), Criteria.where(Dict.OA_CONTACT_NAME).regex(pattern)));
        return mongoTemplate.find(query, DemandBaseInfo.class);
    }
}
