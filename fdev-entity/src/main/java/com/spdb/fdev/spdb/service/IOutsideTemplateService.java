package com.spdb.fdev.spdb.service;

import com.spdb.fdev.spdb.entity.OutSideTemplate;

import java.util.List;

public interface IOutsideTemplateService {

    /**
     * 查询外部参数配置模版
     * @param outSideTemplate
     * @return
     * @throws Exception
     */
    List<OutSideTemplate> queryOutsideTemplate(OutSideTemplate outSideTemplate) throws Exception;
}
