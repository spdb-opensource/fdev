package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AutoConfigTags;

import java.util.List;
import java.util.Map;

public interface TagsDao {

    void saveTag(AutoConfigTags autoConfigTags);

    AutoConfigTags queryByGitlabId(Integer gitlabId);

    void update(Integer gitlabId, List<Map<String, Object>> tagsInfoList);

    void updateConfigGitlabId(AutoConfigTags autoConfigTags);
}
