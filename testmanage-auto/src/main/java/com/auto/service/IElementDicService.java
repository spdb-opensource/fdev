package com.auto.service;

import java.util.List;
import java.util.Map;

import com.auto.entity.ElementDic;

public interface IElementDicService {

    void addElementDic(Map<String, String> map) throws Exception;

    List<ElementDic> queryElementDic(Map<String, String> map) throws Exception;

    void deleteElementDic(Map map) throws Exception;

    void updateElementDic(Map map) throws Exception;
}
