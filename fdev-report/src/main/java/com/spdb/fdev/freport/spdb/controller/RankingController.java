package com.spdb.fdev.freport.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.freport.spdb.dto.RankingDto;
import com.spdb.fdev.freport.spdb.service.RankingService;
import com.spdb.fdev.freport.spdb.service.SonarQubeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "排行榜")
@RestController
@RequestMapping(value = "/api/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private SonarQubeService SonarQubeService;

    @ApiOperation(value = "仪表盘 - 查询用户提交排行")
    @RequestMapping(value = "/queryUserCommit", method = RequestMethod.POST)
    public JsonResult queryUserCommit(@RequestBody RankingDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.rankingService.queryUserCommit(dto.getRankingType(), dto.getStartDate(), dto.getEndDate()));
    }

    @ApiOperation(value = "缓存sonar扫描应用")
    @RequestMapping(value = "/cacheSonarProject", method = RequestMethod.POST)
    public JsonResult cacheSonarProject() throws Exception {
        this.SonarQubeService.cacheSonarProject();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 落地缓存排行榜数据
     *
     * @param
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "仪表盘 - 查询sonar扫描project排行")
    @RequestMapping(value = "/querySonarProjectRank", method = RequestMethod.POST)
    public JsonResult querySonarProjectRank(@RequestBody RankingDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(this.rankingService.querySonarProjectRank(dto.getCode()));
    }
}
