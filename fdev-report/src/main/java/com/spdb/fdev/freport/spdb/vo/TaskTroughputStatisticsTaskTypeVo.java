package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;

/**
 * @Author liux81
 * @DATE 2022/1/24
 */
@Data
public class TaskTroughputStatisticsTaskTypeVo {
    private Integer devTaskSum;//开发任务数量
    private Integer noCodeTaskSum;//无代码任务数量
    private Integer dailyTaskSum;//日常任务数量
    private Integer sum;//合计

    public TaskTroughputStatisticsTaskTypeVo() {
    }

    public TaskTroughputStatisticsTaskTypeVo(Integer devTaskSum, Integer noCodeTaskSum, Integer dailyTaskSum, Integer sum) {
        this.devTaskSum = devTaskSum;
        this.noCodeTaskSum = noCodeTaskSum;
        this.dailyTaskSum = dailyTaskSum;
        this.sum = sum;
    }

    public void init(){
        this.setSum(this.devTaskSum + this.noCodeTaskSum + this.dailyTaskSum);
    }
}
