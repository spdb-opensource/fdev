package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;

import java.util.List;

@Data
public class RankingVo {

    private String tips;//提示

    private List<Ranking> ranking;

    public RankingVo(String tips, List<Ranking> ranking) {
        this.tips = tips;
        this.ranking = ranking;
    }
}
