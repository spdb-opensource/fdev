package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.dao.ITemplateDocumentDao;
import com.spdb.fdev.release.entity.TemplateDocument;
import com.spdb.fdev.release.service.ITemplateDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TemplateDocumentServiceImpl implements ITemplateDocumentService {

    @Autowired
    private ITemplateDocumentDao templateDocumentDao;

    @Override
    public List<String> getDocumentList(String sysname_cn, String template_type) {
        TemplateDocument templateDocument = templateDocumentDao.getDocument(sysname_cn, template_type);
        List<String> documentList = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(templateDocument)) {
            documentList = templateDocument.getDocument_list();
        }
        return documentList;
    }

    @Override
    public TemplateDocument getDocument(String sysname_cn, String template_type) {
        TemplateDocument templateDocument = templateDocumentDao.getDocument(sysname_cn, template_type);
        return templateDocument;
    }

    @Override
    public TemplateDocument save(TemplateDocument templateDocument) {
        return templateDocumentDao.save(templateDocument);
    }

    @Override
    public TemplateDocument editDocumentList(TemplateDocument templateDocument) {
        return templateDocumentDao.editDocumentList(templateDocument);
    }
}
