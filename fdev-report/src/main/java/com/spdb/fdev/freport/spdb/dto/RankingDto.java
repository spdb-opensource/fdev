package com.spdb.fdev.freport.spdb.dto;

import lombok.Data;

@Data
public class RankingDto {

    private String rankingType;//topTen(前十)、lastTen(后十)

    private String startDate;

    private String endDate;

    private String code;//sonar code - bugs(bug)、vulnerabilities(漏洞)、code_smells(异味)、duplicated_lines_density(重复率)
}
