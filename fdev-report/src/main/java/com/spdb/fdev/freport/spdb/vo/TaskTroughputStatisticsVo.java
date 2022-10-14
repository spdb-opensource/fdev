package com.spdb.fdev.freport.spdb.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class TaskTroughputStatisticsVo {

    @JsonIgnore
    private String groupId;//组ID
    private String groupName;//组名
    private TaskTroughputStatisticsTaskTypeVo addBusSum;//新增业务任务数
    private TaskTroughputStatisticsTaskTypeVo addTechSum;//新增科技任务数
    private TaskTroughputStatisticsTaskTypeVo addDailySum;//新增日常任务数
    private TaskTroughputStatisticsTaskTypeVo proBusSum;//投产业务任务数
    private TaskTroughputStatisticsTaskTypeVo proTechSum;//投产科技任务数
    private TaskTroughputStatisticsTaskTypeVo proDailySum;//完成日常任务数
    private TaskTroughputStatisticsTaskTypeVo proSum;//投产任务总数
    private Integer person;//人数
    private TaskTroughputStatisticsAvgVo perProBusAvg;//人均投产业务任务数
    private TaskTroughputStatisticsAvgVo perProTechAvg;//人均投产科技任务数
    private TaskTroughputStatisticsAvgVo perProDailyAvg;//人均完成日常任务数
    private TaskTroughputStatisticsAvgVo perProAvg;//人均投产任务数

}
