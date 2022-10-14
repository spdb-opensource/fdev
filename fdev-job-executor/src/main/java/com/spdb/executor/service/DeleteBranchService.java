package com.spdb.executor.service;

import com.csii.pe.pojo.FeatureBranch;
import com.csii.pe.pojo.ProductTag;
import com.csii.pe.pojo.ReleaseBranch;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class DeleteBranchService {

    private static Logger logger = LoggerFactory.getLogger(DeleteBranchService.class);
    private SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.INPUT_DATE);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GitlabService gitlabService;

    public void deleteBranchAndTag(String projectId) {
        List<FeatureBranch> featureBranches = queryNotCleanFeatureBranch(projectId);
        List<ReleaseBranch> releaseBranches = queryNotCleanReleaseBranch(projectId);
        List<ProductTag> productTags = queryNotCleanProductTag(projectId);
        for(FeatureBranch featureBranch:featureBranches){
            try {
                gitlabService.deleteBranch(String.valueOf(featureBranch.getGitlab_project_id()), featureBranch.getFeature_branch());
                updateFeatureBranch(featureBranch.getGitlab_project_id(), featureBranch.getFeature_branch(), Integer.valueOf(Dict.FINISH_CLEAN));
            } catch (Exception e) {
                logger.error("删除projectId:" + featureBranch.getGitlab_project_id() + ",feature:" + featureBranch.getFeature_branch() + "失败!" + e.getMessage());
                //throw new RuntimeException(e.getMessage());
            }
        }
        for (ReleaseBranch releaseBranch:releaseBranches){
            try {
                gitlabService.deleteBranch(String.valueOf(releaseBranch.getGitlab_project_id()), releaseBranch.getRelease_branch());
                updateReleaseBranch(releaseBranch.getGitlab_project_id(), releaseBranch.getRelease_branch(), Integer.valueOf(Dict.FINISH_CLEAN));
            } catch (Exception e) {
                logger.error("删除projectId:" + releaseBranch.getGitlab_project_id() + ",release:" + releaseBranch.getRelease_branch() + "失败!" + e.getMessage());
                //throw new RuntimeException(e.getMessage());
            }
        }
        for(ProductTag tag:productTags){
            try {
                gitlabService.deleteTag(String.valueOf(tag.getGitlab_project_id()), tag.getProduct_tag());
                updateProductTag(tag.getGitlab_project_id(), tag.getProduct_tag(), Integer.valueOf(Dict.FINISH_CLEAN));
            } catch (Exception e) {
                logger.error("删除projectId:" + tag.getGitlab_project_id() + ",tag:" + tag.getProduct_tag() + "失败!" + e.getMessage());
                //throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * 更新具体项目对应分支的清理状态
     *
     * @param projectId
     * @param branch
     * @param status
     */
    public void updateFeatureBranch(Integer projectId, String branch, Integer status) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.FEATURE_BRANCH).is(branch);
        criteria.and(Dict.GITLAB_PROJECT_ID).is(projectId);
        Query query = new Query(criteria);
        Update update = Update.update(Dict.ALREADY_CLEAN, status);
        mongoTemplate.updateFirst(query, update, FeatureBranch.class);
        }

    /**
     * 更新具体项目对应tag的清理状态
     *
     * @param projectId
     * @param tagName
     * @param status
     */
    public void updateProductTag(Integer projectId, String tagName, Integer status) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.PRODUCT_TAG).is(tagName);
        criteria.and(Dict.GITLAB_PROJECT_ID).is(projectId);
        Query query = new Query(criteria);
        Update update = Update.update(Dict.ALREADY_CLEAN, status);
        mongoTemplate.updateFirst(query, update, ProductTag.class);
    }

    /**
     * 更新具体项目对应release分支的清理状态
     *
     * @param projectId
     * @param releaseBranch
     * @param status
     */
    public void updateReleaseBranch(Integer projectId, String releaseBranch, Integer status) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.RELEASE_BRANCH).is(releaseBranch);
        criteria.and(Dict.GITLAB_PROJECT_ID).is(projectId);
        Query query = new Query(criteria);
        Update update = Update.update(Dict.ALREADY_CLEAN, status);
        mongoTemplate.updateFirst(query, update, ReleaseBranch.class);
    }

    private List<FeatureBranch> queryNotCleanFeatureBranch(String projectId) {
        Query query = new Query(getCriteria(projectId));
        return mongoTemplate.find(query, FeatureBranch.class);
    }

    private List<ProductTag> queryNotCleanProductTag(String projectId) {
        Query query = new Query(getCriteria(projectId));
        return mongoTemplate.find(query, ProductTag.class);
    }

    private List<ReleaseBranch> queryNotCleanReleaseBranch(String projectId) {
        Query query = new Query(getCriteria(projectId));
        return mongoTemplate.find(query, ReleaseBranch.class);
    }

    private Criteria getCriteria(String projectId) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ALREADY_CLEAN).is(Integer.valueOf(Dict.NOT_FINISH_CLEAN));
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -30);
        criteria.and(Dict.CREATE_TIME).lt(sdf.format(calendar.getTime()));
        if (!CommonUtils.isNullOrEmpty(projectId))
            criteria.and(Dict.GITLAB_PROJECT_ID).is(Integer.valueOf(projectId));
        return criteria;
    }
}
