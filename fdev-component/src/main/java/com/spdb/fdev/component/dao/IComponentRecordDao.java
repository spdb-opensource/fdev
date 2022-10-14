package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.ComponentRecord;

import java.util.List;
import java.util.Map;

public interface IComponentRecordDao {

    List query(ComponentRecord componentRecord) throws Exception;

    List queryByComponentIdAndVersion(ComponentRecord componentRecord);

    List queryByComponentIdAndRegexVersion(ComponentRecord componentRecord);

    ComponentRecord queryByComponentIdAndVersion(String component_id, String version);

    ComponentRecord queryByComponentIdAndType(String component_id, String type);

    ComponentRecord save(ComponentRecord componentRecord) throws Exception;

    ComponentRecord update(ComponentRecord componentRecord) throws Exception;

    ComponentRecord queryById(ComponentRecord componentRecord) throws Exception;

    List<ComponentRecord> queryListByType(String componentid, String type);

    ComponentRecord queryByType(String componentId, String type);

    long deleteByComponentId(String component_id);

    ComponentRecord upsert(ComponentRecord componentRecord) throws Exception;

    List queryRecordByComponentIdAndVersion(String componentId, String tagName);

    List queryRecordForDestroy(String componentId, String tagName);

    ComponentRecord queryByPipId(String pipid);

    int queryCompoentRecoreds(String componentId);

    void delete(ComponentRecord componentRecord) throws Exception;

    List<ComponentRecord> getAlphaOrRcVersion(String id, String alphaOrRc, String numVersion, Boolean containPrepackage);

    List<ComponentRecord> queryByComponentIdAndBranchs(String component_id, String numVersion, List<String> branchList);

    List<ComponentRecord> queryByComponentIdAndBranch(String component_id, String numVersion, String feature_branch, String stage);

    Map<String,Object> queryIssueData(String startTime, String endTime);

    List queryRecordByComponentIdAndVersionandFeature(String componentId, String tagName,String featureBranch);

    List<ComponentRecord> queryByIssueId(String id) throws Exception;

    List<ComponentRecord> queryReleaseRecordByComponentId(String componentId,String regex);
}
