package com.spdb.fdev.gitlabwork.kafka;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.gitlabwork.entiy.MergedInfo;
import com.spdb.fdev.gitlabwork.entiy.SitMergedInfo;
import com.spdb.fdev.gitlabwork.service.CastService;
import com.spdb.fdev.gitlabwork.service.GitLabService;
import com.spdb.fdev.gitlabwork.service.MergedService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class KafkaMsgListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${gitlab.target.branch}")
    private List<String> gitlabTargetBranch;

    @Autowired
    private CastService castService;

    @Autowired
    private MergedService mergedService;

    @Autowired
    private GitLabService gitLabService;


    @KafkaListener(topics = {"${kafka.merge.topic}"}, groupId = "${kafka.group.id}")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        Map<String, Object> projectInfo = (Map<String, Object>) parse.get(Dict.PROJECT);
        String gitHttpUrl = (String) projectInfo.get(Dict.GIT_HTTP_URL);
        Integer projectId = (Integer) projectInfo.get(Dict.ID);
        String projectName = (String) projectInfo.get(Dict.NAME);
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        //获取merge iid
        Integer iid = (Integer) attributes.get(Dict.IID);
        // 合并的目标分支
        String targetBranch = (String) attributes.get(Dict.TARGET_BRANCH);
        // 合并的源分支
        String sourceBranch = (String) attributes.get(Dict.SOURCE_BRANCH);
        String state = (String) attributes.get(Dict.STATE);
        Map<String, Object> user = (Map<String, Object>) parse.get(Dict.USER);

        // 合到SIT & 合并成功的记录入库 sitmerged
        saveSitMergedInfo(gitHttpUrl, targetBranch, projectName, sourceBranch, iid + "", state);

        //获取创建人
        Integer creator = (Integer) attributes.get(Dict.AUTHOR_ID);
        //获取处理人
        Integer handler = getHandlerId((String) user.get("username"));
        //标记目标分支是否为SIT、release、master、dev、testrun
        Boolean branchTag = false;

        for (String branch : gitlabTargetBranch) {
            if (targetBranch.toLowerCase().startsWith(branch.toLowerCase())) {
                branchTag = true;
                break;
            }
        }
        //检验fdev是否存在有该projectId对应的应用
        Map appMap = this.mergedService.checkFapp(projectId);
        if (!CommonUtil.isNullOrEmpty(creator) && !CommonUtil.isNullOrEmpty(handler) && !CommonUtil.isNullOrEmpty(appMap)) {
            //如果状态为merged且创建人和处理人为同一个人，合并的目标分支为SIT、release、master、dev、testrun
            //才将该merge相关的信息保存
            if (Dict.MERGED.equals(state) && creator.equals(handler) && branchTag) {
                //获取应用名称
                String appName = (String) projectInfo.get(Dict.NAME);
                //获取创建时间
                String createTime = CommonUtil.handleTime((String) attributes.get(Dict.CREATED_AT));
                //获取处理时间
                String handleTime = CommonUtil.handleTime((String) attributes.get(Dict.UPDATED_AT));
                //获取merge_commit_sha
                String mergeCommitSha = (String) attributes.get(Dict.MERGE_COMMIT_SHA);
                saveMergedInfo(appName, creator, (String) appMap.get(Dict.ID), createTime, handleTime, sourceBranch, targetBranch, iid, projectId, mergeCommitSha, (String) appMap.get(Dict.GROUP));
            }
        }
    }

    /**
     * 保存相关的merge信息
     *
     * @param appName        应用名
     * @param creator        创建人
     * @param appId          fdev应用id
     * @param createTime     创建时间
     * @param handleTime     处理时间
     * @param sourceBranch   源分支
     * @param targetBranch   目标分支
     * @param iid            merge请求的iid
     * @param projectId      gitlab应用的ID
     * @param mergeCommitSha commit的sha值
     * @param groupId        当前fdev应用对应的组id
     */
    private void saveMergedInfo(String appName, Integer creator, String appId, String createTime, String handleTime, String sourceBranch, String targetBranch, Integer iid, Integer projectId, String mergeCommitSha, String groupId) {
        MergedInfo mergedInfo = new MergedInfo();
        List<Map> commits = this.gitLabService.getCommitsByIid(iid, projectId);
        //commit提交的id集合
        List<String> commitsId = new ArrayList<>();
        for (Map commit : commits) {
            String id = (String) commit.get(Dict.ID);
            commitsId.add(id);
        }
        Map commitMap = this.gitLabService.getCommitBySha(projectId, mergeCommitSha);
        String title = (String) commitMap.get(Dict.TITLE);
        //如果存在冲突，便存入冲突的commit_sha值    存在冲突的时候 title的值中包含有"conflict"
        if (title.contains(Dict.CONFLICT)) {
            mergedInfo.setConflict_sha(mergeCommitSha);
        }
        mergedInfo.setAppName(appName);
        //通过creator的id去查用户模块获取用户信息以及查gitlab的相应信息
        Map memberInfo = this.mergedService.getCreatorInfo(creator);
        //不是白名单组的才会记录信息
        if (!this.mergedService.checkGroupIsWList(memberInfo)) {
            mergedInfo.setCreator(memberInfo);
            mergedInfo.setHandler(memberInfo);
            mergedInfo.setAppId(appId);
            mergedInfo.setGroupId(groupId);
            mergedInfo.setCreateTime(createTime);
            mergedInfo.setHandleTime(handleTime);
            mergedInfo.setSourceBranch(sourceBranch);
            mergedInfo.setTargetBranch(targetBranch);
            mergedInfo.setCommitsId(commitsId);
            MergedInfo result = this.mergedService.addMergedInfo(mergedInfo);
            if (CommonUtil.isNullOrEmpty(result)) {
                logger.error("记录merged信息失败!");
            } else {
                logger.info("记录merged信息成功！");
            }
        }
    }


    /**
     * 获取处理人id
     *
     * @param username
     * @return
     */
    private Integer getHandlerId(String username) {
        List<Map> users = this.gitLabService.getUserByUsername(username);
        return (Integer) users.get(0).get(Dict.ID);
    }

    private void saveSitMergedInfo(String gitHttpUrl, String targetBranch, String appName, String sourceBranch, String iid, String state) {
        if (!"SIT".equals(targetBranch) || !Dict.MERGED.equals(state)) {
            return;
        }
        ObjectId objectId = new ObjectId();
        SitMergedInfo sitMergedInfo = new SitMergedInfo();
        sitMergedInfo.setSitBranch(targetBranch);
        sitMergedInfo.setGitUrl(gitHttpUrl);
        sitMergedInfo.setId(objectId.toString());
        sitMergedInfo.setAppName(appName);
        sitMergedInfo.setSourceBranch(sourceBranch);
        sitMergedInfo.setMergeDate(CommonUtil.dateFormate(new Date(), CommonUtil.DATE_FORMATE));
        sitMergedInfo.setMergeId(iid);
        castService.save(sitMergedInfo);
    }

}
