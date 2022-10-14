package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.release.dao.IReleaseRqrmntInfoDao;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Repository
public class ReleaseRqrmntInfoDaoImpl implements IReleaseRqrmntInfoDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public ReleaseRqrmntInfo queryReleaseRqrmntInfo(String release_date, String rqrmnt_no, String group_id) throws Exception {
        Query query = new Query(Criteria.where(Dict.RELEASE_DATE)
                .is(release_date)
                .and(Dict.RQRMNT_NO)
                .is(rqrmnt_no)
                .and(Dict.GROUP_ID)
                .is(group_id));
        return mongoTemplate.findOne(query, ReleaseRqrmntInfo.class);
    }

    @Override
    public void saveReleaseRqrmntInfo(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception {
        releaseRqrmntInfo.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.save(releaseRqrmntInfo);
    }

    @Override
    public ReleaseRqrmntInfo queryReleaseRqrmntInfoByTaskNo(String task_id) throws Exception {
        Query query = new Query(Criteria.where(Dict.TASK_IDS).in(task_id));
        return mongoTemplate.findOne(query, ReleaseRqrmntInfo.class);
    }

    public List<ReleaseRqrmntInfo> queryReleaseRqrmntInfosByTaskNo(String task_id) throws Exception{
        Query query = new Query(Criteria.where(Dict.TASK_IDS).in(task_id));
        return mongoTemplate.find(query, ReleaseRqrmntInfo.class);
    }

    @Override
    public void updateRqrmntInfo(String relase_date, String package_submit_test, String rel_test) throws Exception {
        Criteria criteria = Criteria.where(Dict.RELEASE_DATE).is(relase_date);
        Update update = Update.update(Dict.ID, relase_date)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        if(!Util.isNullOrEmpty(package_submit_test)){

            update.set(Dict.PACKAGE_SUBMIT_TEST, package_submit_test);
        }
        if(!Util.isNullOrEmpty(rel_test)){
            update.set(Dict.REL_TEST, rel_test);
        }
        Query query = new Query(Criteria.where(Dict.RELEASE_DATE).is(relase_date));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmntInfo.class);
    }

    @Override
    public void updateRqrmntInfoTaskList(String id, List<String> task_ids) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.TASK_IDS, task_ids)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmntInfo.class);
    }

    @Override
    public void deleteRqrmntInfo(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        mongoTemplate.findAndRemove(query, ReleaseRqrmntInfo.class);
    }

    @Override
    public void addTaskReleaseRqrmntInfo(String id, String task_id, String new_add_sign,String otherSystem,String commonProfile,String dateBaseAlter,String specialCase) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        ReleaseRqrmntInfo one = mongoTemplate.findOne(query, ReleaseRqrmntInfo.class);
        Set<String> task_ids = one.getTask_ids();
        task_ids.add(task_id);
        Update update = Update.update(Dict.TASK_IDS, task_ids)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        if(!CommonUtils.isNullOrEmpty(new_add_sign)){
            update.set(Dict.NEW_ADD_SIGN,"0");
        }
        if(!CommonUtils.isNullOrEmpty(otherSystem)){
            if(!CommonUtils.isNullOrEmpty(one.getOtherSystem())){
                update.set(Dict.OTHERSYSTEM, one.getOtherSystem()+";"+otherSystem);
            }else{
                update.set(Dict.OTHERSYSTEM, otherSystem);
            }
        }
        if(!CommonUtils.isNullOrEmpty(commonProfile)){
            update.set(Dict.COMMONPROFILE,"0");
        }
        if(!CommonUtils.isNullOrEmpty(dateBaseAlter)){
            update.set(Dict.DATABASEALTER,"0");
        }
        if(!CommonUtils.isNullOrEmpty(specialCase)){
            update.set(Dict.SPECIALCASE,"0");
        }
        mongoTemplate.updateFirst(query, update, ReleaseRqrmntInfo.class);
    }

    @Override
    public void deleteTaskReleaseRqrmntInfo(String id, String task_id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID)
                .is(id)
                .and(Dict.TASK_IDS)
                .in(task_id));
        ReleaseRqrmntInfo one = mongoTemplate.findOne(query, ReleaseRqrmntInfo.class);
        if(CommonUtils.isNullOrEmpty(one)){
            return;
        }
        Set<String> task_ids = one.getTask_ids();
        task_ids.remove(task_id);
        //若此列表已无任务， 则删除该需求信息
        if(task_ids.size() == 0){
            mongoTemplate.findAndRemove(query, ReleaseRqrmntInfo.class);
        }else{
            Update update = Update.update(Dict.TASK_IDS, task_ids)
                    .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
            mongoTemplate.findAndModify(query, update, ReleaseRqrmntInfo.class);
        }
    }

    @Override
    public List<ReleaseRqrmntInfo> queryRqrmntInfoList(String release_date, String type, Set groupId) throws Exception {
        Criteria criteria = Criteria.where(Dict.RELEASE_DATE)
                .is(release_date);
        if(!CommonUtils.isNullOrEmpty(groupId)){
            criteria.and(Dict.GROUP_ID)
                    .in(groupId);
        }
        if(!CommonUtils.isNullOrEmpty(type)){
            if(type.equals(Dict.NEW_ADD_SIGN)){
                criteria.and(Dict.NEW_ADD_SIGN).is("0");
            }
            if(type.equals(Dict.DATABASEALTER)){
                criteria.and(Dict.DATABASEALTER).is("0");
            }
            if(type.equals(Dict.COMMONPROFILE)){
                criteria.and(Dict.COMMONPROFILE).is("0");
            }
            if(type.equals(Dict.SPECIALCASE)){
                criteria.and(Dict.SPECIALCASE).is("0");
            }
        }
        Query query = new Query(criteria);
        return mongoTemplate.find(query, ReleaseRqrmntInfo.class);
    }

    @Override
    public List<ReleaseRqrmntInfo> queryEarilyThanTodayRqrmntInfo(String today) {
        Query query = new Query(Criteria.where(Dict.RELEASE_DATE).gte(today));
        return mongoTemplate.find(query, ReleaseRqrmntInfo.class);
    }

    @Override
    public void updateRqrmntInfoReview(String id, String otherSystem, String commonProfile, String dateBaseAlter, String specialCase) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.DATABASEALTER, dateBaseAlter)
                .set(Dict.COMMONPROFILE, commonProfile)
                .set(Dict.OTHERSYSTEM, otherSystem)
                .set(Dict.SPECIALCASE, specialCase)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update,ReleaseRqrmntInfo.class);
    }


    @Override
    public ReleaseRqrmntInfo queryRqrmntById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, ReleaseRqrmntInfo.class);
    }

    @Override
    public void updateRqrmntInfoPackageSubmitTest(String id, String stage) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.PACKAGE_SUBMIT_TEST, stage)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmntInfo.class);
    }

    @Override
    public void updateRqrmntInfoRelTest(String id, String stage) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.REL_TEST, stage)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmntInfo.class);
    }

    @Override
    public void updateRqrmntInfoTag(String id, String tag) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.TAG, tag)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmntInfo.class);
    }

    @Override
    public void deleteReleaseRqrmntInfoTask(String task_id) throws Exception {
        List<ReleaseRqrmntInfo> releaseRqrmntInfos = queryReleaseRqrmntInfosByTaskNo(task_id);
        for (ReleaseRqrmntInfo releaseRqrmntInfo : releaseRqrmntInfos) {
            deleteTaskReleaseRqrmntInfo(releaseRqrmntInfo.getId(), task_id);
        }
    }

    @Override
    public void updateRqrmntInfoFlag(String id, String flag) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.EXPORT_FLAG, flag)
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        mongoTemplate.findAndModify(query, update, ReleaseRqrmntInfo.class);
    }
}
