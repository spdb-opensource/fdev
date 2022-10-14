package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.TemplateDocument;

import java.util.List;

public interface ITemplateDocumentService {

    List<String> getDocumentList(String sysname_cn,String template_type);

    TemplateDocument getDocument(String sysname_cn,String template_type);

    TemplateDocument save(TemplateDocument templateDocument);

    TemplateDocument editDocumentList(TemplateDocument templateDocument);
}
