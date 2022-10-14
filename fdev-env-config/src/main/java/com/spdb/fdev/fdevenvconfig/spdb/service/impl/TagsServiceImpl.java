package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.TagsDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AutoConfigTags;
import com.spdb.fdev.fdevenvconfig.spdb.service.TagsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TagsServiceImpl implements TagsService {

    @Autowired
    private TagsDao tagsDao;

    @Override
    public void saveTags(Map tags) {
        String tagName = (String) tags.get(Constants.BRANCH);
        Integer gitlabId = (Integer) tags.get(Constants.GITLAB_PROJECT_ID);
        Integer pipelineId = (Integer) tags.get(Dict.PIPELINE_ID);
        Map<String, Object> tagMap = new HashMap<>();
        tagMap.put(Dict.TAG_NAME, tagName);
        tagMap.put(Dict.PIPELINE_ID, pipelineId);
        String cTime = DateUtil.getDate(new Date(), DateUtil.DATETIME_ISO_FORMAT);
        tagMap.put(Constants.CTIME, cTime);
        AutoConfigTags queryAutoConfigTags = tagsDao.queryByGitlabId(gitlabId);
        if (queryAutoConfigTags != null) {
            List<Map<String, Object>> tagsInfoList = queryAutoConfigTags.getTagInfo();
            // 判断原来的tags列表里是否存在当前tag，若存在，只需要更新pipeline_id和ctime，若不存在，则需将tagMap加入tags列表
            boolean flag = false;
            for (Map<String, Object> oldTagsMap : tagsInfoList) {
                String oldTagName = (String) oldTagsMap.get(Dict.TAG_NAME);
                if (StringUtils.isNotEmpty(oldTagName) && oldTagName.equals(tagName)) {
                    flag = true;
                    oldTagsMap.put(Dict.PIPELINE_ID, pipelineId);
                    oldTagsMap.put(Constants.CTIME, cTime);
                    break;
                }
            }
            if (!flag) {
                tagsInfoList.add(tagMap);
            }
            tagsDao.update(gitlabId, tagsInfoList);
        } else {
            AutoConfigTags tagInfo = new AutoConfigTags();
            List<Map<String, Object>> tagList = new ArrayList<>();
            tagList.add(tagMap);
            tagInfo.setGitlab_id(gitlabId);
            tagInfo.setTagInfo(tagList);
            tagsDao.saveTag(tagInfo);
        }
    }
}
