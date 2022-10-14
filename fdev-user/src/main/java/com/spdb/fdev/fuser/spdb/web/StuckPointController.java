package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.spdb.service.StuckPointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;



@Api(tags = "卡点接口")
@RequestMapping("/api/stuckPoint")
@RestController
public class StuckPointController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印


    @Resource
    private StuckPointService stuckPointService;


    @ApiOperation(value = "新增卡点数据接口")
    @RequestMapping(value = "/addStuckPoint", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addStuckPoint(@RequestBody Map map){

        return JsonResultUtil.buildSuccess(this.stuckPointService.addStuckPoint(this.stuckPointService.checkStuckPointMapFieldsIsNull(map)));
    }

}
