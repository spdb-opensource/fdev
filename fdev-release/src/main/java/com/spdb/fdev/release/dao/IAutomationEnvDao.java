package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.AutomationEnv;

import java.util.List;
import java.util.Set;

public interface IAutomationEnvDao {

    List<AutomationEnv> query();

    void save(AutomationEnv automationEnv);

    void update(AutomationEnv automationEnv);

    void deleteById(String id);

    List<AutomationEnv> queryByEnvName(String content, List<String> platform);
}
