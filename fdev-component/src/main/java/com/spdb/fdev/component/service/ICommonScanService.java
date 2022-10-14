package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ArchetypeInfo;
import com.spdb.fdev.component.entity.Dependency;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ICommonScanService {

    List<Dependency> getDependencyList(String dir, String type) throws Exception;

    List<Map> getDependencyMapList(String dir, String type) throws Exception;

    String getPythonResult(String dir, String type) throws IOException, InterruptedException;

    JSONArray getNexus(String groupId, String artifactId);

    JSONArray getMultiNexus(String groupId, String artifactId) throws Exception;

    String getLatestVersion(String groupId, String artifactId);

    String getMaxVersion(String groupId, String artifactId);

    Date getLastChangeDate(JSONObject jsonObject);
}
