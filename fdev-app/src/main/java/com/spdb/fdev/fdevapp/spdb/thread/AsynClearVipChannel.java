package com.spdb.fdev.fdevapp.spdb.thread;


import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.entity.AppVipChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * 异步清理当前应用超过50条的记录，按时间清理最早的，留最近的
 */
public class AsynClearVipChannel implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(AsynClearVipChannel.class);// 控制台日志打印


    private String appName;

    private MongoTemplate mongoTemplate;

    public AsynClearVipChannel() {
    }

    public AsynClearVipChannel(String appName, MongoTemplate mongoTemplate) {
        this.appName = appName;
        this.mongoTemplate = mongoTemplate;
    }

    public AsynClearVipChannel(String appName) {
        this.appName = appName;
    }

    @Override
    public void run() {
        //查询应用的记录数是否大于50条
        Criteria c = Criteria.where(Dict.APP + "." + Dict.APP_NAME).is(this.appName);
        Query query = new Query(c);
        long count = this.mongoTemplate.count(query, AppVipChannel.class, "app-vip-channel");
        logger.info(this.appName + "当前记录条数为" + count);
        if (count <= 50)
            return;
        //若超过
        //从触发时间大到小排序
        query.with(Sort.by(Sort.Order.desc(Dict.TRIGGER_TIME))).skip(50);
        this.mongoTemplate.remove(query, AppVipChannel.class);
        logger.info("删除超过50条时间以外的记录成功！");
    }
}
