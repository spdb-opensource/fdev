package com.spdb.fdev.freport.spdb.dto;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsDto {

    private String startDate;

    private String endDate;

    private List<String> groupIds;

    private List<String> appIds;

    private Boolean showDaily;

    private Boolean includeChild;

    private List<String> taskPersonTypeForAvg;//spdbMaster、master、developer、tester

    private String priority;

    private List<String> demandType;//需求类型：科技、业务、日常

    private List<Integer> taskType;//任务类型：开发任务、无代码任务、日常任务

}