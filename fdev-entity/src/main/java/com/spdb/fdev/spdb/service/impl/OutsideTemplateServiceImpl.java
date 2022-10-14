package com.spdb.fdev.spdb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.spdb.dao.OutsideTemplateDao;
import com.spdb.fdev.spdb.entity.OutSideTemplate;
import com.spdb.fdev.spdb.service.IOutsideTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RefreshScope
public class OutsideTemplateServiceImpl implements IOutsideTemplateService {

    @Autowired
    private OutsideTemplateDao outsideTemplateDao;

    /**
     * 查询外部参数配置模版
     * @param outSideTemplate
     * @return
     * @throws Exception
     */
    @Override
    public List<OutSideTemplate> queryOutsideTemplate(OutSideTemplate outSideTemplate) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<OutSideTemplate> outSideTemplates = this.outsideTemplateDao.query(outSideTemplate);
        return outSideTemplates;
    }
}
