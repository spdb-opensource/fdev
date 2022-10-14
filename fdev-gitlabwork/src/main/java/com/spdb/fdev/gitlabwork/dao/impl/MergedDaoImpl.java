package com.spdb.fdev.gitlabwork.dao.impl;

import com.spdb.fdev.base.cache.GuavaCache;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.gitlabwork.dao.MergedDao;
import com.spdb.fdev.gitlabwork.entiy.MergedInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MergedDaoImpl implements MergedDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GuavaCache cache;

    @Override
    public MergedInfo saveMergedInfo(MergedInfo mergedInfo) {
        mergedInfo.initId();
        return this.mongoTemplate.save(mergedInfo, Dict.MERGEDINFO);
    }

    /**
     * 按条件查询，分页查询
     *
     * @param params
     * @return
     */
    @Override
    public Map queryMergedInfo(Map params) {
        Criteria c = new Criteria();
        List<Criteria> criterias = new ArrayList<>();
        //处理人
        if (!CommonUtil.isNullOrEmpty(params.get(Dict.GIT_USER_ID)))
            c.and(Dict.HANDLER + "." + Dict.GITLAB_ID).is(params.get(Dict.GIT_USER_ID));
        //从选择的组中获取其所有的子组放入list作为条件筛选
        if (!CommonUtil.isNullOrEmpty(params.get(Dict.CHILDRENGROUPIDS)))
            c.and(Dict.GROUP_ID_UP).in((List) params.get(Dict.CHILDRENGROUPIDS));
        //筛选时间
        if (!CommonUtil.isNullOrEmpty(params.get(Dict.START_TIME)))
            criterias.add(Criteria.where(Dict.HANDLETIME).gte(params.get(Dict.START_TIME)));

        String endTime = (String) params.get(Dict.END_TIME);
        if (!CommonUtil.isNullOrEmpty(endTime)) {
            //表示查开始时间那天的数据，修改endTime，给endTime加一天
            //if (endTime.equals((String) params.get(Dict.START_TIME)))
            endTime = CommonUtil.timeAddOneDay(endTime);
            criterias.add(Criteria.where(Dict.HANDLETIME).lte(endTime));
        }
        if (!CommonUtil.isNullOrEmpty(criterias))
            c.andOperator(criterias.toArray(new Criteria[criterias.size()]));
        Query query = new Query(c);
        Map map = new HashMap<>();
        long total = mongoTemplate.count(query, MergedInfo.class);
        if (!StringUtils.isEmpty(params.get(Dict.PAGE)) && !StringUtils.isEmpty(params.get(Dict.PAGENUM))) {
            int page = (int) params.get(Dict.PAGE);
            int pageNum = (int) params.get(Dict.PAGENUM);
            if ((int) params.get(Dict.PAGE) != 0)
                query.skip((page > 0 ? page - 1 : 0) * pageNum).limit(pageNum);
        }
        List<Map> mergeds = mongoTemplate.find(query, Map.class, Dict.MERGEDINFO);

        //遍历加入fullname字段
        List<Map> fullNameCache = (List<Map>) this.cache.getCache(Dict.FULLNAMES);
        for (Map merged : mergeds) {
            for (Map group : fullNameCache) {
                if (!CommonUtil.isNullOrEmpty(merged.get(Dict.GROUP_ID_UP)))
                    if (merged.get(Dict.GROUP_ID_UP).equals(group.get(Dict.ID))) {
                        String fullName = (String) group.get(Dict.FULLNAME);
                        merged.put(Dict.FULLNAME, fullName);
                        break;
                    }
            }
        }
        map.put(Dict.TOTAL, total);
        map.put(Dict.MERGEDINFO, mergeds);
        return map;
    }
}
