package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;

@Data
public class DemandStatisticsVo {//这里其实应该用工厂模式

    private String groupName;//组名称

    private DemandStatisticsType preEvaluate;//预评估

    private DemandStatisticsType evaluate;//评估

    private DemandStatisticsTypeLineUp preImplement;//待实施(排队)

    private DemandStatisticsType leaderDevelop;//牵头开发中

    private DemandStatisticsType joinDevelop;//参与开发中

    private DemandStatisticsType sit;

    private DemandStatisticsType uat;

    private DemandStatisticsType rel;

    private DemandStatisticsType pro;

    private DemandStatisticsType wait;

    private String busAvg;

    private String developAvg;//开发总人均负荷=（开发阶段（牵头）+ 开发阶段（参与））/项目组规模

    private DemandStatisticsTypeTotal total;

    public DemandStatisticsVo(String groupName) {
        this.groupName = groupName;
        this.preEvaluate = new DemandStatisticsType();
        this.evaluate = new DemandStatisticsType();
        this.preImplement = new DemandStatisticsTypeLineUp();
        this.leaderDevelop = new DemandStatisticsType();
        this.joinDevelop = new DemandStatisticsType();
        this.sit = new DemandStatisticsType();
        this.uat = new DemandStatisticsType();
        this.rel = new DemandStatisticsType();
        this.pro = new DemandStatisticsType();
        this.wait = new DemandStatisticsType();
        this.total = new DemandStatisticsTypeTotal();
    }
}
