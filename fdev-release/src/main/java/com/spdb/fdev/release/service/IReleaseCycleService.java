package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseCycle;

import java.text.ParseException;
import java.util.Map;

public interface IReleaseCycleService {
    ReleaseCycle create(String releaseDate) throws Exception;

    ReleaseCycle update(Map<String, Object> requestParam) throws Exception;

    Map<String, String> queryByReleaseDate(Map<String, String> requestParam);

    ReleaseCycle edit(String oldReleaseDate,String releaseDate) throws ParseException;
}
