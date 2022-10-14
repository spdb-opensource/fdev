package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ReleaseNode;

import java.util.List;
import java.util.Map;

public interface IReleaseNodeService {
       ReleaseNode saveTag(Map<String, String> map ) throws Exception;
       List<String> queryTags(Map<String, String> map ) throws Exception;
}
