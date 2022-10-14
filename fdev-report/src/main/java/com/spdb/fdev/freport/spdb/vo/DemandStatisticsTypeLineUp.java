package com.spdb.fdev.freport.spdb.vo;


import com.spdb.fdev.freport.base.dict.DemandEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DemandStatisticsTypeLineUp extends DemandStatisticsType {

    private Integer techLineUp;

    private Integer busLineUp;

    private Integer dailyLineUp;

    public DemandStatisticsTypeLineUp() {
        this.techLineUp = 0;
        this.busLineUp = 0;
        this.dailyLineUp = 0;
    }

    public void lineCount2Plus(String demandType) {
        DemandEnum.DemandTypeEnum byValue = DemandEnum.DemandTypeEnum.getByValue(demandType);
        if (byValue != null) {
            switch (byValue) {
                case TECH:
                    techLineUp++;
                    break;
                case BUSINESS:
                    busLineUp++;
                    break;
                case DAILY:
                    dailyLineUp++;
                    break;
            }
        }
    }
}
