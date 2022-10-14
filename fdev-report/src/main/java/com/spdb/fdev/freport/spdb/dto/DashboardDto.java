package com.spdb.fdev.freport.spdb.dto;

import lombok.Data;

@Data
public class DashboardDto {

    private Integer cycle;//周期

    private String demandType;//需求类型

    private String taskStage;//区分task原本的状态（规则太丑了），这里直接不走规范定制化为create和pro

}
