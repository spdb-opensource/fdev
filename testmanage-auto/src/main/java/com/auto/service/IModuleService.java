package com.auto.service;

import com.auto.entity.Module;

import java.util.List;
import java.util.Map;

public interface IModuleService {

    void addModule(Module module) throws Exception;

    List<Map<String, String>> queryModule(Map<String, String> map) throws Exception;

    void deleteModule(Map map) throws Exception;

    void updateModule(Map map) throws Exception;
}
