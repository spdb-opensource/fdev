package com.spdb.fdev.pipeline.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.pipeline.entity.YamlConfig;

import java.util.Map;

public interface IYamlConfigDao {

    YamlConfig queryById(String configId);

    String add(YamlConfig yamlConfig);

    String updateStatusClose(String configId);
}
