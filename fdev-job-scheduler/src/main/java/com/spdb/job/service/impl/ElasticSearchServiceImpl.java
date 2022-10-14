package com.spdb.job.service.impl;

import com.spdb.common.config.database.MultiJdbcAccess;
import com.spdb.common.dict.Dict;
import com.spdb.common.schedule.JobDynamicScheduler;
import com.spdb.common.util.DateUtil;
import com.spdb.common.util.Util;
import com.spdb.job.service.IElasticSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ElasticSearchServiceImpl implements IElasticSearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);
    @Value("${search.elasticsearch.aliases.name}")
    private String indexName;
    @Value("${spring.profiles.active}")
    private String env;
    @Autowired
    private MultiJdbcAccess multiJdbcAccess;

    @Autowired
    private RestHighLevelClient client;

    public List queryJobLog(String schedulerSeqNo) {
        List result = new ArrayList();
        if(Util.isNullOrEmpty(schedulerSeqNo)){
            return result;
        }
        Map jobInfoMap = new HashMap();
        jobInfoMap.put(Dict.SCHEDULERSEQNO, schedulerSeqNo);
        jobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        Map planInfo = multiJdbcAccess.getSqlMap().selectOne("ExecutionPlan.selectJobOperationRecordOne", jobInfoMap);
        long date = Long.valueOf(planInfo.get(Dict.FIREDTIME).toString());
        //查询调度器交易日志
        queryAppLog(schedulerSeqNo,result,"fdev-job-scheduler", date);
        //查询执行器交易日志
        queryAppLog(schedulerSeqNo,result,"fdev-job-executor", date);
        return result;
    }

    private void queryAppLog(String schedulerSeqNo, List result, String appName, long date){
        SearchResponse search = null;
        try {
        //通过调度开始时间  后加5个小时做时间区间
        Map esDateScope = DateUtil.getEsDateScope(date);
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        QueryBuilder agentNameQueryBuilder = QueryBuilders.matchPhraseQuery("agent.name",appName);
        if("pro".equals(env)){
            env = "master";
        }
        QueryBuilder envQueryBuilder = QueryBuilders.matchPhraseQuery("fields.env",env);
        QueryBuilder messageQueryBuilder = QueryBuilders.matchPhraseQuery("message","SchedulerSeqNo:"+schedulerSeqNo);
        QueryBuilder timeQueryBuilder = QueryBuilders.rangeQuery("@timestamp").from(esDateScope.get(Dict.STARTTIME)).to(esDateScope.get(Dict.ENDTIME));
        boolQueryBuilder.must(agentNameQueryBuilder);
        boolQueryBuilder.must(envQueryBuilder);
        boolQueryBuilder.must(messageQueryBuilder);
        boolQueryBuilder.must(timeQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        search = client.search(searchRequest, RequestOptions.DEFAULT);
        }catch (Exception e) {
            logger.error("Searching all documents fails,SchedulerSeqNo:"+schedulerSeqNo);
            logger.error(e.getMessage(),e);
            result.add("Elastic Search Error:"+e);
        }
        for (SearchHit hit : search.getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            result.add(source.get("message"));
        }
    }
}
