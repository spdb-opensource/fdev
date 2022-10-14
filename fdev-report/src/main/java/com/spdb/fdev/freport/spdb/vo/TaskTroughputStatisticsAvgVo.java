package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author liux81
 * @DATE 2022/1/24
 */
@Data
public class TaskTroughputStatisticsAvgVo {
    private BigDecimal devTaskAvg;//开发任务
    private BigDecimal noCodeTaskAvg;//无代码任务
    private BigDecimal dailyTaskAvg;//日常任务
    private BigDecimal sum;//合计

    public TaskTroughputStatisticsAvgVo() {
    }

    public TaskTroughputStatisticsAvgVo(BigDecimal devTaskAvg, BigDecimal noCodeTaskAvg, BigDecimal dailyTaskAvg) {
        this.devTaskAvg = devTaskAvg;
        this.noCodeTaskAvg = noCodeTaskAvg;
        this.dailyTaskAvg = dailyTaskAvg;
    }
}
