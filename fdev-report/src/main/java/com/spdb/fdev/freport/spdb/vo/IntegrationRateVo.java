package com.spdb.fdev.freport.spdb.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.freport.spdb.entity.report.GitlabMergeRecord;
import lombok.Data;

import java.util.List;

@Data
public class IntegrationRateVo {

    private String id;//组id

    private String name;//组名称

    private String parentId;//父组id

    @JsonIgnore
    private List<IntegrationRateVo> children;//子组信息

    private List<GitlabMergeRecord> gitLabMerge;//集成率（git合并）信息
}
