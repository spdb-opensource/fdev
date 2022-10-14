package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.*;

import java.util.List;
import java.util.Map;

public interface IComponentScanService {

    void gitOperation(String name_en, String git);

    void gitOperation(String name_en, String git, String version);

    void initComponentHistory(ComponentInfo componentInfo) throws Exception;

    void initComponentApplication(ComponentInfo componentInfo) throws Exception;

    ComponentApplication getComponentApplication(String application_id, String dir, ComponentInfo component) throws Exception;

    ComponentApplication getComponentApplication(String application_id, String dir, ComponentInfo component, List<Dependency> dependencyList)
            throws Exception;

    ComponentApplication getComponentApplication(String application_id, String dir, MpassComponent component)
            throws Exception;

    void initMpassComponentHistory(MpassComponent mpassComponent);

    void initMpassComponentApplication(MpassComponent mpassComponent) throws Exception;

    ComponentApplication getComponentApplication(String application_id, String dir, MpassComponent component, List<Map> dependencyList);

    void initMavenArcheType(String path, String groupId, String artifactId, String applicationName, String http_url_to_repo) throws Exception;

    void initImageApplication(BaseImageInfo baseImageInfo) throws Exception;

    void initMultiComponentHistory(ComponentInfo componentInfo) throws Exception;
}
