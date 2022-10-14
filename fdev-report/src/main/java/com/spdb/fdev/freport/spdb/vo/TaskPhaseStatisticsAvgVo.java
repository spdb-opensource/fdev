package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskPhaseStatisticsAvgVo extends TaskPhaseStatisticsVo {

    private BigDecimal createAvg;

    private BigDecimal devAvg;

    private BigDecimal sitAvg;

    private BigDecimal uatAvg;

    private BigDecimal relAvg;

    private BigDecimal proAvg;

}
