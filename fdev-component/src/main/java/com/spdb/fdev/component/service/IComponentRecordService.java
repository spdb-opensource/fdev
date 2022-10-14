package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ComponentRecord;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface IComponentRecordService {
    List query(ComponentRecord componentRecord) throws Exception;

    List queryByComponentIdAndRegexVersion(ComponentRecord componentRecord);

    List queryByComponentIdAndVersion(ComponentRecord componentRecord);

    ComponentRecord queryByComponentIdAndVersion(String component_id, String version);

    ComponentRecord queryByComponentIdAndType(String componentId, String type);

    ComponentRecord save(ComponentRecord componentRecord) throws Exception;

    ComponentRecord update(ComponentRecord componentRecord) throws Exception;

    ComponentRecord queryById(ComponentRecord componentRecord) throws Exception;

    void mergedCallBack(String state, Integer iid, Integer project_id) throws Exception;

    int queryCompoentRecoreds(String componentId);

    ComponentRecord updateForMpass(Map param) throws Exception;

    ComponentRecord updateForMpass(ComponentRecord componentRecord) throws Exception;

    List<ComponentRecord> getAlphaOrRcVersion(String id, String alphaOrRc, String numVersion, Boolean containPrepackage);

    void upsert(ComponentRecord componentRecord) throws Exception;

    List<ComponentRecord> queryByComponentIdAndBranchs(String component_id, String numVersion,List<String> branchList);

    List<ComponentRecord> queryByComponentIdAndBranch(String component_id, String numVersion,String feature_branch, String stage);

    void delete(ComponentRecord componentRecord) throws Exception;

    Map<String, Object> queryIssueData(Map requestparam) throws ParseException;

    List<ComponentRecord> queryByIssueId(String id) throws Exception;

    void mergedSitAndReleaseCallBack(String state, Integer iid, Integer project_id,String target_branch) throws Exception;

    void mergedMasterCallBack(String state, Integer iid, Integer project_id, String target_branch) throws Exception;
}
