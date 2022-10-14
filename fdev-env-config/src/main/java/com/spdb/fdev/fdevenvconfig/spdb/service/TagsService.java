package com.spdb.fdev.fdevenvconfig.spdb.service;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface TagsService {

    @Async
    void saveTags(Map tags);

}
