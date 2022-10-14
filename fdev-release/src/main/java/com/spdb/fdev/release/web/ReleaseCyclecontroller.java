package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ReleaseCycle;
import com.spdb.fdev.release.service.IReleaseCycleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "投产周期接口")
@RequestMapping("/api/releaseCycle")
@RestController
public class ReleaseCyclecontroller {
    @Autowired
    private IReleaseCycleService releaseCycleService;

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_DATE,Dict.RELEASE_DATE_LIST})
    @PostMapping(value = "/update")
    public JsonResult update(@RequestBody @Validated @ApiParam Map<String, Object> requestParam) throws Exception {
        ReleaseCycle releaseCycle=releaseCycleService.update(requestParam);
        return JsonResultUtil.buildSuccess(releaseCycle);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_DATE})
    @PostMapping(value = "/queryByReleaseDate")
    public JsonResult queryByReleaseDate(@RequestBody @Validated @ApiParam Map<String, String> requestParam){
        Map<String,String> releaseCycle=releaseCycleService.queryByReleaseDate(requestParam);
        return JsonResultUtil.buildSuccess(releaseCycle);
    }

}
