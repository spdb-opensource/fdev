package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IOutsideTemplateDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.OutSideTemplate;
import com.spdb.fdev.fdevenvconfig.spdb.service.IConfigFileService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IOutsideTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Service
@RefreshScope
public class IOutsideTemplateServiceImpl implements IOutsideTemplateService {

    @Value("${record.switch}")
    private Boolean recordSwitch;

    @Autowired
    private IConfigFileService configFileService;
    @Autowired
    private IOutsideTemplateDao outsideTemplateDao;
    @Autowired
    private ServiceUtil serviceUtil;
    @Autowired
	public UserVerifyUtil userVerifyUtil;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public OutSideTemplate outsideTemplateSave(String project_id, String branch, List<Map<String, String>> variables) throws Exception {
        OutSideTemplate outSideTemplate = new OutSideTemplate();
        outSideTemplate.setProject_id(project_id);
        outSideTemplate.setVariables(variables);
        if (CommonUtils.isNullOrEmpty(outSideTemplate.getVariables())) {
            outSideTemplate.setVariables(new ArrayList<>());
        }
        outSideTemplate.setOpno(serviceUtil.getOpno());
        outSideTemplate.setCtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        outSideTemplate.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        OutSideTemplate addOutSideTemplate = this.outsideTemplateDao.outsideTemplateSave(outSideTemplate);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("@@@@@@ 新增外部参数配置模版 成功 {}", objectMapper.writeValueAsString(addOutSideTemplate));
        return addOutSideTemplate;
    }

    @Override
    public List<OutSideTemplate> queryOutsideTemplate(OutSideTemplate outSideTemplate) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<OutSideTemplate> outSideTemplates = this.outsideTemplateDao.query(outSideTemplate);
        logger.info("@@@@@@ 外部参数配置模版 成功 {}", objectMapper.writeValueAsString(outSideTemplates));
        return outSideTemplates;
    }

    @Override
    public OutSideTemplate updateOutsideTemplate(OutSideTemplate outSideTemplate) throws Exception {
        if (this.recordSwitch) {
            User user = userVerifyUtil.getRedisUser();
            outSideTemplate.setOpno(user.getId());
        }
        if (CommonUtils.isNullOrEmpty(outSideTemplate.getVariables())) {
            outSideTemplate.setVariables(new ArrayList<Map<String, String>>());
        }
        outSideTemplate.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        OutSideTemplate updateOutSideTemplate = this.outsideTemplateDao.update(outSideTemplate);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("@@@@@@ 更新外部参数配置模版 成功 {}", objectMapper.writeValueAsString(updateOutSideTemplate));
        return updateOutSideTemplate;
    }

}
