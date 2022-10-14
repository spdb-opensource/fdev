package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.OutSideTemplate;

import java.util.List;

public interface OutsideTemplateDao {

    /**
     * 查询外部参数配置模版
     * @param outSideTemplate
     * @return
     * @throws Exception
     */
    List<OutSideTemplate> query(OutSideTemplate outSideTemplate) throws Exception;
}
