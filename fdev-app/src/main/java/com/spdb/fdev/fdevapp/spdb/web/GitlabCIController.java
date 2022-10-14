package com.spdb.fdev.fdevapp.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevapp.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.validate.ValidateApp;
import com.spdb.fdev.fdevapp.spdb.entity.AuthorityManagers;
import com.spdb.fdev.fdevapp.spdb.entity.GitlabCI;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabCIService;
import com.spdb.fdev.fdevapp.spdb.service.impl.AppEntityServiceImpl;
import com.spdb.fdev.transport.RestTransport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "持续集成模板实体")
@RequestMapping("/api/gitlabci")
@RestController
@RefreshScope
public class GitlabCIController {
    @Autowired
    private IGitlabCIService gitlabciservice;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private AuthorityManagers authorityManagers;
    @Autowired
    private AppEntityServiceImpl appEntityService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Value("${user.api}")
    private String userApi;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @OperateRecord(operateDiscribe = "应用模块-新增持续集成模板实体")
    @NoNull(require = {Dict.NAME, Dict.YAML_NAME}, parameter = GitlabCI.class)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public JsonResult save(@RequestBody @ApiParam GitlabCI gitlabci) throws Exception {
        //获取当前用户权限
        ArrayList roleList = appEntityService.getManagerAuthority();
        boolean authorityManager = true;
        //校验当前用户是否是卡点管理员
        boolean operateManager = true;
        if (!ValidateApp.validateUserStuckPoint(roleList, authorityManagers.getStuckPointRoleId())) {
            operateManager = false;
        }
        //校验用户权限 厂商负责人 行内负责人 拥有权限
        if (!ValidateApp.validateUserAuthority(roleList, authorityManagers.getEnvManagers())) {
            authorityManager = false;
        }
        if (authorityManager == false&&operateManager==false) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足"});
        }

        GitlabCI result = null;
        try {
            result = gitlabciservice.save(gitlabci);
        } catch (Exception e) {
            logger.error("save: " + e);
            throw e;
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @NoNull(require = {Dict.ID}, parameter = GitlabCI.class)
    @RequestMapping(value = "/findbyid", method = RequestMethod.POST)
    public JsonResult findById(@RequestBody @ApiParam GitlabCI gitlabci) throws Exception {
        GitlabCI result = null;
        try {
            result = gitlabciservice.findById(gitlabci.getId());
        } catch (Exception e) {
            logger.error("findbyid: " + e);
            throw e;
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "应用模块-更新持续集成模板实体")
    @NoNull(require = {Dict.ID}, parameter = GitlabCI.class)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public JsonResult update(@RequestBody @ApiParam GitlabCI gitlabci) throws Exception {
        //获取当前用户权限
        ArrayList roleList = appEntityService.getManagerAuthority();
        boolean authorityManager = false;
        boolean operateManager = false;
        if (ValidateApp.validateUserStuckPoint(roleList, authorityManagers.getStuckPointRoleId())) {
            operateManager = true;
        }
        //校验用户权限 厂商负责人 行内负责人 拥有权限
        if (ValidateApp.validateUserAuthority(roleList, authorityManagers.getEnvManagers())) {
            authorityManager = true;
        }
        if (authorityManager==false&&operateManager==false) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足"});
        }

        GitlabCI result = null;
        try {
            result = gitlabciservice.update(gitlabci);
        } catch (Exception e) {
            logger.error("update: " + e);
            throw e;
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public JsonResult query(@RequestBody @ApiParam GitlabCI gitlabci) throws Exception {
        List<GitlabCI> result = null;
        try {
            result = gitlabciservice.query(gitlabci);
        } catch (Exception e) {
            logger.error("query: " + e);
            throw e;
        }
        return JsonResultUtil.buildSuccess(result);
    }
}
