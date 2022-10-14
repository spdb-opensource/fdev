package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DemandStatisticsTypeTotal extends DemandStatisticsType{

    private Integer totalCount;

    public DemandStatisticsTypeTotal() {
        this.totalCount = 0;
    }

}
