package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.OutSideTemplate;

import java.util.*;

public interface IOutsideTemplateService {

    OutSideTemplate outsideTemplateSave(String project_id, String branch, List<Map<String, String>> variables) throws Exception;

    List<OutSideTemplate> queryOutsideTemplate(OutSideTemplate outSideTemplate) throws Exception;

    OutSideTemplate updateOutsideTemplate(OutSideTemplate outSideTemplate) throws Exception;

}
