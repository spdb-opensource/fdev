package com.spdb.fdev.fuser.spdb.dao;


import com.spdb.fdev.fuser.spdb.entity.user.Section;

import java.util.List;

public interface ISectionDao {
	void addSection(Section section);

	List<Section> querySectionByNameEn(String sectionNameEn);

	List<Section> querySectionByNameCn(String sectionNameCn);

	List<Section> queryAllSection();
}
