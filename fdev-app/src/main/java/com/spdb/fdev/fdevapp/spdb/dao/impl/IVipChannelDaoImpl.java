package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.dao.IVipChannelDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppVipChannel;
import com.spdb.fdev.fdevapp.spdb.thread.AsynClearVipChannel;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class IVipChannelDaoImpl implements IVipChannelDao {

    private static Logger logger = LoggerFactory.getLogger(AsynClearVipChannel.class);// 控制台日志打印

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void saveVipChannel(AppVipChannel appVipChannel) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        appVipChannel.setId(id);
        appVipChannel.set_id(objectId);
        //执行异步线程
        new Thread(new AsynClearVipChannel((String) appVipChannel.getApp().get(Dict.APP_NAME), this.mongoTemplate)).start();
        this.mongoTemplate.save(appVipChannel, "app-vip-channel");
        logger.info("保存" + appVipChannel.getTrigger_time() + "channel信息成功！");
    }

    /**
     * 根据条件查询channel记录，可以根据timestamp来对记录进行唯一标识的查询
     *
     * @param appVipChannel
     * @return
     */
    @Override
    public List<AppVipChannel> queryVipChannelByParams(AppVipChannel appVipChannel) {
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(appVipChannel.getId()))
            c.and(Dict.ID).is(appVipChannel.getId());
        if (!CommonUtils.isNullOrEmpty(appVipChannel.getTrigger_time()))
            c.and(Dict.TRIGGER_TIME).is(appVipChannel.getTrigger_time());
        if (!CommonUtils.isNullOrEmpty(appVipChannel.getApp())) {
            Map app = (Map) appVipChannel.getApp();
            if (!CommonUtils.isNullOrEmpty(app.get(Dict.APP_NAME))) {
                c.and(Dict.APP + "." + Dict.APP_NAME).is((String) app.get(Dict.APP_NAME));
            }
        }
        Query query = new Query(c);
        return this.mongoTemplate.find(query, AppVipChannel.class);
    }

    /**
     * 根据id获取单条pipeline的信息
     *
     * @param id
     * @return
     */
    @Override
    public AppVipChannel queryPipelineById(String id) {
        return this.mongoTemplate.findOne(new Query(Criteria.where(Dict.ID).is(id)), AppVipChannel.class);
    }

    /**
     * 更新job的状态
     *
     * @param status    更新的job的状态
     * @param timestamp 用标识更新的整条记录
     * @param stage     用来标识需要更新的是哪个job
     */
    @Override
    public void updateJobStatus(String status, Long timestamp, String stage) {
        Query query = new Query(Criteria.where(Dict.TRIGGER_TIME).is(timestamp));
        AppVipChannel vipChannel = this.mongoTemplate.findOne(query, AppVipChannel.class);
        List<Map<String, Object>> jobs = vipChannel.getJobs();
        Update update = new Update();
        // 若status为failed，当前job修改为failed，当前job之后的job修改为skipped，整条记录的status为failed
        if (Dict.FAILED.equals(status)) {
            Integer priority = 0;
            for (Map job : jobs) {
                String stages = (String) job.get(Dict.STAGES);
                if (stages.equals(stage)) {
                    //修改当前状态为failed
                    job.put(Dict.STATUS, status);
                    job.put(Dict.FINISHED_AT, Calendar.getInstance().getTimeInMillis());
                    // 记录当前job的优先级
                    priority = (Integer) job.get(Dict.PRIORITY);
                }
            }
            // 修改后面的job，即优先级大于当前阶段优先级的
            for (Map job : jobs) {
                if ((Integer) job.get(Dict.PRIORITY) > priority) {
                    job.put(Dict.STATUS, Dict.SKIPPED);
                }
            }
            update.set(Dict.JOBS, jobs);
            this.mongoTemplate.updateFirst(query, update, "app-vip-channel");
            //修改当前记录的status为failed
            this.updateStatus(Dict.FAILED, timestamp);
        } else {
            for (Map job : jobs) {
                String stages = (String) job.get(Dict.STAGES);
                if (stages.equals(stage)) {
                    job.put(Dict.STATUS, status);
                    if (Dict.PASSED.equals(status)) {
                        job.put(Dict.FINISHED_AT, Calendar.getInstance().getTimeInMillis());
                    }
                    break;
                }
            }
            update.set(Dict.JOBS, jobs);
            this.mongoTemplate.updateFirst(query, update, "app-vip-channel");
        }

    }

    /**
     * 更新job的minioUrl
     *
     * @param minioUrl
     * @param timestamp 作为更新的唯一标识
     */
    @Override
    public void updateMinioUrl(String minioUrl, Long timestamp, String stages) {
        Query query = new Query(Criteria.where(Dict.TRIGGER_TIME).is(timestamp));
        List<AppVipChannel> appVipChannels = this.mongoTemplate.find(query, AppVipChannel.class);
        //查询得到记录，将vipChannel的minioUrl进行修改
        AppVipChannel vipChannel = appVipChannels.get(0);
        List jobs = (List) vipChannel.getJobs();
        for (Map job : (List<Map>) jobs) {
            //用stages定位到需要修改的job
            if (job.get(Dict.STAGES).equals(stages)) {
                job.put(Dict.MINIO_URL, minioUrl);
            }
        }
        Update update = new Update();
        update.set(Dict.JOBS, jobs);
        //更新整个jobs
        this.mongoTemplate.updateFirst(query, update, "app-vip-channel");
    }

    /**
     * 更新正条vipChannel记录的status
     *
     * @param status
     * @param timestamp
     */
    @Override
    public void updateStatus(String status, Long timestamp) {
        Query query = new Query(Criteria.where(Dict.TRIGGER_TIME).is(timestamp));
        Update update = new Update();
        update.set(Dict.STATUS, status);
        if (status.equals(Dict.PASSED) || status.equals(Dict.FAILED)) {
            //当为passed或者failed的时候，表示当前pipeline已经跑完或失败，需要对记录更新加入duration和finished_at
            Date date = new Date();
            Long finished_at = date.getTime();
            update.set(Dict.FINISHED_AT, finished_at);
            Long duration = CommonUtils.getTimestampDiffValue(timestamp, finished_at);
            //最大时间为30分钟
            //duration = duration.replace("分钟", ":").replace("秒", "").replace("小时", "");
            update.set(Dict.DURATION, duration);
        }
        this.mongoTemplate.updateFirst(query, update, "app-vip-channel");
    }

    /**
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param stage     标识那一个job的stage
     * @param timeStamp 标识那一条channel记录
     */
    @Override
    public void updateJobTime(Long startTime, Long endTime, String stage, Long timeStamp) {
        Query query = new Query(Criteria.where(Dict.TRIGGER_TIME).is(timeStamp));
        List<AppVipChannel> vipChannels = this.mongoTemplate.find(query, AppVipChannel.class);
        AppVipChannel vipChannel = vipChannels.get(0);
        Update update = new Update();
        List<Map<String, Object>> jobs = vipChannel.getJobs();
        for (Map job : jobs) {
            String stages = (String) job.get(Dict.STAGES);
            if (stages.equals(stage)) {
                if (!CommonUtils.isNullOrEmpty(startTime))
                    job.put(Dict.TRIGGER_TIME, startTime);
                if (!CommonUtils.isNullOrEmpty(endTime))
                    job.put(Dict.FINISHED_AT, endTime);
                break;
            }
        }
        update.set(Dict.JOBS, jobs);
        this.mongoTemplate.updateFirst(query, update, "app-vip-channel");

    }

    /**
     * 更新channel状态
     *
     * @param id
     * @return
     */
    @Override
    public AppVipChannel updateStatusForCancel(String id) {
        //用timestamp查出当前记录
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.ID).is(id));
        List<AppVipChannel> appVipChannels = this.mongoTemplate.find(query, AppVipChannel.class);
        AppVipChannel vipChannel = appVipChannels.get(0);
        Update update = new Update();
        //判断jobs有没有存在有job的status为created的，存在有修改为canceled，然后修改整条记录的status为canceled
        List<Map<String, Object>> jobs = vipChannel.getJobs();
        for (Map job : jobs) {
            String status = (String) job.get(Dict.STATUS);
            if (Dict.CREATED.equals(status) || Dict.PENDING.equals(status) || Dict.RUNNING.equals(status)) {
                job.put(Dict.STATUS, Dict.CANCELED);
            }
        }
        update.set(Dict.JOBS, jobs);
        update.set(Dict.STATUS, Dict.CANCELED);
        Long finishedTime = Calendar.getInstance().getTimeInMillis();
        Long diffValue = CommonUtils.getTimestampDiffValue(vipChannel.getTrigger_time(), finishedTime);
        update.set(Dict.DURATION, diffValue);
        //更新完成时间
        update.set(Dict.FINISHED_AT, finishedTime);
        this.mongoTemplate.updateFirst(query, update, AppVipChannel.class);
        vipChannel.setStatus(Dict.CANCELED);
        return vipChannel;
    }

    /**
     * 根据查询条件进行分页查询
     *
     * @param params
     * @return
     */
    @Override
    public Map queryChannelListByPagination(Map params) {
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.NAME_EN)))
            c.and(Dict.APP + "." + Dict.APP_NAME).is(params.get(Dict.NAME_EN));
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.REF)))
            c.and(Dict.REF).is(params.get(Dict.REF));
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.STATUS)))
            c.and(Dict.STATUS).is(params.get(Dict.STATUS));
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.TRIGGERER)))
            c.and(Dict.TRIGGERER + "." + Dict.TRIGGERERID).is(params.get(Dict.TRIGGERER));

        int page = 1;       //页码
        int per_page = 10;  //页量
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.PAGE)))
            page = (int) params.get(Dict.PAGE);
        if (!CommonUtils.isNullOrEmpty(params.get(Dict.PER_PAGE)))
            per_page = (int) params.get(Dict.PER_PAGE);

        Map vipChannelMap = new HashMap();
        Query query = new Query(c);
        long total = mongoTemplate.count(query, AppVipChannel.class);
        if (page != 0)
            query.skip((page > 0 ? page - 1 : 0) * per_page).limit(per_page);
        //根据触发时间倒序返回
        query.with(Sort.by(Sort.Order.desc(Dict.TRIGGER_TIME)));
        List<AppVipChannel> vipChannelList = mongoTemplate.find(query, AppVipChannel.class, "app-vip-channel");
        for (AppVipChannel appVipChannel : vipChannelList) {
            if (!CommonUtils.isNullOrEmpty(appVipChannel.getDuration()) && appVipChannel.getDuration() != 0L) {
                Long duration = appVipChannel.getDuration();
                duration = duration % 1000 >= 500 ? duration / 1000 + 1 : duration / 1000;
                appVipChannel.setDuration(duration);
            }
        }
        vipChannelMap.put(Dict.TOTAL, total);
        vipChannelMap.put(Dict.VIPCHANNELLIST, vipChannelList);
        return vipChannelMap;
    }

    /**
     * 根据条件查询获取得到minio_url并获返回
     *
     * @param id
     * @param stage
     * @return
     */
    @Override
    public String queryMinioUrl(String id, String stage) {
        Criteria c = new Criteria();
        c.and(Dict.ID).is(id);
        Query query = new Query(c);
        List<AppVipChannel> appVipChannels = this.mongoTemplate.find(query, AppVipChannel.class);
        String minioUrl = "";
        List<Map<String, Object>> jobs = appVipChannels.get(0).getJobs();
        for (Map job : jobs) {
            if (!CommonUtils.isNullOrEmpty(job.get(Dict.STAGES)))
                if (stage.equals(job.get(Dict.STAGES))) {
                    minioUrl = (String) job.get(Dict.MINIO_URL);
                    break;
                }
        }
        return minioUrl;
    }

    /**
     * 返回 status1 or status2 的记录
     *
     * @param status1
     * @param status2
     * @return
     */
    @Override
    public List<AppVipChannel> queryPipelineByTowStatus(String status1, String status2) {
        Criteria leftCriteria = new Criteria();
        leftCriteria.and(Dict.STATUS).is(status1);
        Criteria rightCriteria = new Criteria();
        rightCriteria.and(Dict.STATUS).is(status2);

        Query query = new Query(new Criteria().orOperator(leftCriteria, rightCriteria));
        return this.mongoTemplate.find(query, AppVipChannel.class);
    }

    /**
     * 判断当前job的状态是否为status
     *
     * @param status    需要校验的status
     * @param timestamp
     * @param stage
     * @return
     */
    @Override
    public Boolean checkedStatus(String status, Long timestamp, String stage) {
        Query query = new Query(Criteria.where(Dict.TRIGGER_TIME).is(timestamp));
        AppVipChannel channel = this.mongoTemplate.findOne(query, AppVipChannel.class);
        List<Map<String, Object>> jobs = channel.getJobs();
        for (Map<String, Object> job : jobs) {
            if (job.get(Dict.STAGES).equals(stage)) {
                String jobStatus = (String) job.get(Dict.STATUS);
                if (jobStatus.equals(status))
                    return true;
            }
        }
        return false;
    }
}
