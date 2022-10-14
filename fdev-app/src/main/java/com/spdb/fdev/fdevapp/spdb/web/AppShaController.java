package com.spdb.fdev.fdevapp.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.entity.AppSha;
import com.spdb.fdev.fdevapp.spdb.service.IAppShaService;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author xxx
 * @date 2019/7/1 18:10
 */
@RestController
@RequestMapping("/api/sha")
public class AppShaController {

    @Autowired
    private IAppShaService appShaService;

    /**
     * 配合持续集成 判断当前 分支 是否要继续允许
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/getSha")
    @RequestValidate(NotEmptyFields = {Dict.CI_PROJECT_ID, Dict.CI_COMMIT_SHORT_SHA,Dict.CI_COMMIT_REF_NAME})
    public JsonResult findById(@RequestBody @ApiParam Map param) throws Exception {
        Integer gitlab_id = (Integer) param.get(Dict.CI_PROJECT_ID);
        String sha = (String) param.get(Dict.CI_COMMIT_SHORT_SHA);
        String branch = (String) param.get(Dict.CI_COMMIT_REF_NAME);
        return JsonResultUtil.buildSuccess(this.appShaService.getSha(gitlab_id, sha, branch));
    }
}
