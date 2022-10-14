package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.ISectionDao;
import com.spdb.fdev.fuser.spdb.dao.Impl.SectionDaoImpl;
import com.spdb.fdev.fuser.spdb.entity.user.Section;
import com.spdb.fdev.fuser.spdb.service.ISectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.format.number.money.MonetaryAmountFormatter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionServiceImpl implements ISectionService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());//日志打印

    @Autowired
    ISectionDao sectionDao;

    @Override
    public void addSection(Section section) throws Exception {
        //控制条线中英文名称不可重复
        String sectionNameEn = section.getSectionNameEn();
        String sectionNameCn = section.getSectionNameCn();
        if(!CommonUtils.isNullOrEmpty(sectionDao.querySectionByNameEn(sectionNameEn))){
            throw new FdevException("英文名已存在！");
        }
        if(!CommonUtils.isNullOrEmpty(sectionDao.querySectionByNameCn(sectionNameCn))){
            throw new FdevException("中文名已存在！");
        }
        section.setCreateTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        section.initId();
        sectionDao.addSection(section);
    }

    @Override
    public List<Section> queryAllSection() {
        return sectionDao.queryAllSection();
    }
}
