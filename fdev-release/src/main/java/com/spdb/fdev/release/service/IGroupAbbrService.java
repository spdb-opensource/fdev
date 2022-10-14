package com.spdb.fdev.release.service;

import java.util.List;

import com.spdb.fdev.release.entity.GroupAbbr;

public interface IGroupAbbrService {

	GroupAbbr queryGroupAbbr(String group_id) throws Exception;

	GroupAbbr updateGroupAbbr(String group_id, String group_abbr) throws Exception;

	GroupAbbr updateSystemAbbr(String group_id, String system_abbr);

    GroupAbbr queryBySysAbbrNotGroupId(String group_id, String system_abbr);

	List<GroupAbbr> queryAllGroupAbbr();
}
