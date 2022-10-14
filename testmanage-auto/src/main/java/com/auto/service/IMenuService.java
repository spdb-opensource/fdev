package com.auto.service;

import com.auto.entity.MenuSheet;

import java.util.List;
import java.util.Map;

public interface IMenuService {

    void addMenu(Map<String, String> map) throws Exception;

    List<MenuSheet> queryMenu(Map<String, String> map) throws Exception;

    void deleteMenu(Map map) throws Exception;

    void updateMenu(Map map) throws Exception;
}
