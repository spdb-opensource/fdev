package com.spdb.fdev.spdb.service;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface MergeEventService {

    @Async
    void doMerge(Map<String, Object> parse);

}
