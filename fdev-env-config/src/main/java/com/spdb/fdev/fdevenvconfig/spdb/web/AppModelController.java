package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppModel;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAppModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "应用实体关联接口")
@RequestMapping("/api/v2/appModel")
@RestController
public class AppModelController {
    @Autowired
    private IAppModelService appModelService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 查询全量的应用实体关联信息
     *
     * @param appModel
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    public JsonResult query(@RequestBody @ApiParam AppModel appModel) throws Exception {
        return JsonResultUtil.buildSuccess(appModelService.query(appModel));
    }


    @PostMapping("/add")
    @NoNull(require = {"app_id", "model_id"}, parameter = AppModel.class)
    public JsonResult add(@RequestBody @ApiParam AppModel appModel) throws Exception {

        return JsonResultUtil.buildSuccess(appModelService.add(appModel));
    }
}
