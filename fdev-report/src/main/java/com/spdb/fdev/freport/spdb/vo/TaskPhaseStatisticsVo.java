package com.spdb.fdev.freport.spdb.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class TaskPhaseStatisticsVo {

    @JsonIgnore
    private String id;//对应维度ID

    private String name;//对应维度名称

    private Integer createCount;

    private Integer devCount;

    private Integer sitCount;

    private Integer uatCount;

    private Integer relCount;

    private Integer proCount;

    private List<String> createIds;

    private List<String> devIds;

    private List<String> sitIds;

    private List<String> uatIds;

    private List<String> relIds;

    private List<String> proIds;

    public void setCount(){
        if (null != createIds) createCount = createIds.size();
        if (null != devIds) devCount = devIds.size();
        if (null != sitIds) sitCount = sitIds.size();
        if (null != uatIds) uatCount = uatIds.size();
        if (null != relIds) relCount = relIds.size();
        if (null != proIds) proCount = proIds.size();
    }

}
