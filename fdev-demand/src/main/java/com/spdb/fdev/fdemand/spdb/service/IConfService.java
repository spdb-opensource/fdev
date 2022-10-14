package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.dto.conf.ContentDto;

import java.util.List;

public interface IConfService {
    void addFileLabel(String pageId);

    List<ContentDto> getConfluencePageInfo(String pageId);

    byte[] exportWord(String pageId) throws Exception;
}
