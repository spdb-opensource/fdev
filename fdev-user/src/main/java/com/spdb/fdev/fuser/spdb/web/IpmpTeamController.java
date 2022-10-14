package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.service.IIpmpTeamService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ipmpTeam")
public class IpmpTeamController {
    @Autowired
    private IIpmpTeamService ipmpTeamService;

    @RequestMapping(value = "/syncAllIpmpTeam", method = RequestMethod.POST)
    @ApiOperation(value = "同步全量牵头单位和团队信息")
    public JsonResult syncAllIpmpTeam() throws Exception {
        ipmpTeamService.syncAllIpmpTeam();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryIpmpLeadTeam", method = RequestMethod.POST)
    @ApiOperation(value = "查询牵头单位和团队信息")
    public JsonResult queryIpmpLeadTeam(@RequestBody Map<String,String> param) throws Exception {
        return JsonResultUtil.buildSuccess(ipmpTeamService.queryIpmpLeadTeam(
                param.get(Dict.DEPT_ID),param.get(Dict.TEAM_ID),param.get(Dict.DEPT_NAME)));
    }
}
