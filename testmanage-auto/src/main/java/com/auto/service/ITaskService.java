package com.auto.service;

import java.util.Map;

import com.alibaba.fastjson.JSONArray;

public interface ITaskService {

    void genFile(Map<String, JSONArray> map, String folder) throws Exception;

    void excuteCase(Map<String, String> map) throws Exception;
}
