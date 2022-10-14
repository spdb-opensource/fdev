package com.spdb.fdev.release.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdb.fdev.release.dao.IGroupAbbrDao;
import com.spdb.fdev.release.entity.GroupAbbr;
import com.spdb.fdev.release.service.IGroupAbbrService;

@Service
public class GroupAbbrServiceImpl implements IGroupAbbrService{
	
	@Autowired
	private IGroupAbbrDao groupAbbrDao;

	@Override
	public GroupAbbr queryGroupAbbr(String group_id) throws Exception {
		return groupAbbrDao.queryGroupAbbr(group_id);
	}

	@Override
	public GroupAbbr updateGroupAbbr(String group_id, String group_abbr) throws Exception {
		return groupAbbrDao.updateGroupAbbr(group_id, group_abbr);
	}

	@Override
	public GroupAbbr updateSystemAbbr(String group_id, String system_abbr) {
		return groupAbbrDao.updateSystemAbbr(group_id, system_abbr);
	}

	@Override
	public GroupAbbr queryBySysAbbrNotGroupId(String group_id, String system_abbr) {
		return groupAbbrDao.queryBySysAbbrNotGroupId(group_id, system_abbr);
	}

	@Override
	public List<GroupAbbr> queryAllGroupAbbr() {
		return groupAbbrDao.queryAllGroupAbbr();
	}

}
