package com.auto.service;

import java.util.List;
import java.util.Map;

import com.auto.entity.Element;

public interface IElementService {

    void addElement(Map<String, String> map) throws Exception;

    List<Element> queryElement(Map<String, String> map) throws Exception;

    void deleteElement(Map map) throws Exception;

    void updateElement(Map map) throws Exception;
}
