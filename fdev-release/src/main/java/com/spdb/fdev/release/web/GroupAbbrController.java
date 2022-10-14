package com.spdb.fdev.release.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.GroupAbbr;
import com.spdb.fdev.release.service.IGroupAbbrService;

import io.swagger.annotations.ApiParam;

@RequestMapping("/api/release")
@RestController
public class GroupAbbrController {
    private static final Logger logger = LoggerFactory.getLogger(SystemReleaseInfoController.class);

    @Autowired
    private IGroupAbbrService groupAbbrService;

    @RequestValidate(NotEmptyFields = {Dict.GROUP_ID})
    @PostMapping(value = "/queryGroupAbbr")
    public JsonResult queryGroupAbbr(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String group_id = requestParam.get(Dict.GROUP_ID);
        GroupAbbr groupAbbr = groupAbbrService.queryGroupAbbr(group_id);
        return JsonResultUtil.buildSuccess(groupAbbr);
    }

    @RequestValidate(NotEmptyFields = {Dict.GROUP_ID, Dict.GROUP_ABBR})
    @PostMapping(value = "/updateGroupAbbr")
    public JsonResult updateGroupAbbr(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String group_id = requestParam.get(Dict.GROUP_ID);
        String group_abbr = requestParam.get(Dict.GROUP_ABBR);
        groupAbbrService.updateGroupAbbr(group_id, group_abbr);
        return JsonResultUtil.buildSuccess(group_abbr);
    }

    @RequestValidate(NotEmptyFields = {Dict.GROUP_ID})
    @PostMapping(value = "/queryGroupSysAbbr")
    public JsonResult queryGroupSysAbbr(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String group_id = requestParam.get(Dict.GROUP_ID);
        GroupAbbr groupAbbr = groupAbbrService.queryGroupAbbr(group_id);
        String system_abbr = "";
        if (!CommonUtils.isNullOrEmpty(groupAbbr) && !CommonUtils.isNullOrEmpty(groupAbbr.getSystem_abbr())) {
            system_abbr = groupAbbr.getSystem_abbr();
        }
        return JsonResultUtil.buildSuccess(system_abbr);
    }

    @RequestValidate(NotEmptyFields = {Dict.GROUP_ID, Dict.SYSTEM_ABBR})
    @PostMapping(value = "/updateGroupSysAbbr")
    public JsonResult updateGroupSysAbbr(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String group_id = requestParam.get(Dict.GROUP_ID);
        String system_abbr = requestParam.get(Dict.SYSTEM_ABBR);
        GroupAbbr groupAbbr = groupAbbrService.queryBySysAbbrNotGroupId(group_id, system_abbr);
        if(CommonUtils.isNullOrEmpty(groupAbbr)) {
            groupAbbrService.updateSystemAbbr(group_id, system_abbr);
        } else {
            throw new FdevException(ErrorConstants.SYSTEM_ABBR_REPET_INSERT_REEOR);
        }
        return JsonResultUtil.buildSuccess(system_abbr);
    }
    
    @PostMapping(value = "/queryAllGroupAbbr")
    public JsonResult queryAllGroupAbbr(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        List<GroupAbbr> groupAbbr = groupAbbrService.queryAllGroupAbbr();
        return JsonResultUtil.buildSuccess(groupAbbr);
    }

}
