package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.AutomationParam;

import java.util.List;

public interface IAutomationParamService {

    List<AutomationParam> query();

    void save(AutomationParam automationParam);

    void update(AutomationParam automationParam);

    void deleteById(String id);

    AutomationParam queryByKey(String key);
}
