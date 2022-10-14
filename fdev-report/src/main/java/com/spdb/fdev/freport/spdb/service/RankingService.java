package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.vo.RankingVo;

public interface RankingService {

    RankingVo queryUserCommit(String rankingType, String startDate, String endDate) throws Exception;

    RankingVo querySonarProjectRank(String code) throws Exception;

}
