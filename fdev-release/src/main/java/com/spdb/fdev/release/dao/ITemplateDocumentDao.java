package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.TemplateDocument;

import java.util.List;

public interface ITemplateDocumentDao {

    TemplateDocument getDocument(String sysname_cn, String template_type);

    TemplateDocument save(TemplateDocument templateDocument);

    TemplateDocument editDocumentList(TemplateDocument templateDocument);

}
