package com.spdb.fdev.freport.spdb.vo;

import com.spdb.fdev.freport.base.dict.DemandEnum;
import lombok.Data;

@Data
public class DemandStatisticsType {

    private Integer techCount;

    private Integer busCount;

    private Integer dailyCount;

    public DemandStatisticsType() {
        this.techCount = 0;
        this.busCount = 0;
        this.dailyCount = 0;
    }

    public void count2Plus(String demandType) {
        DemandEnum.DemandTypeEnum byValue = DemandEnum.DemandTypeEnum.getByValue(demandType);
        if (byValue != null) {
            switch (byValue) {
                case TECH:
                    techCount++;
                    break;
                case BUSINESS:
                    busCount++;
                    break;
                case DAILY:
                    dailyCount++;
            }
        }
    }
}
