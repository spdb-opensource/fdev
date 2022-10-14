package com.spdb.fdev.fdevenvconfig.spdb.dao;

import java.util.List;

import com.spdb.fdev.fdevenvconfig.spdb.entity.OutSideTemplate;

public interface IOutsideTemplateDao {

    OutSideTemplate outsideTemplateSave(OutSideTemplate outSideTemplate);

    List<OutSideTemplate> query(OutSideTemplate outSideTemplate) throws Exception;

    OutSideTemplate update(OutSideTemplate outSideTemplate);

    OutSideTemplate queryByProjectId(String projectId);
}
