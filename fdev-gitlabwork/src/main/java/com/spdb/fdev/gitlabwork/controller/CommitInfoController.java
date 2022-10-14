package com.spdb.fdev.gitlabwork.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.gitlabwork.entiy.Commit;
import com.spdb.fdev.gitlabwork.service.CommitService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/commitInfo")
public class CommitInfoController {

    @Autowired
    private CommitService commitService;

    @ApiOperation(value = "代码提交文件详情")
    @RequestMapping(value = "/queryCommitDiff", method = RequestMethod.POST)
    public JsonResult queryCommitDiff(@RequestBody Commit commit) throws Exception {
        return JsonResultUtil.buildSuccess(this.commitService.queryCommitDiff(commit.getProjectId(), commit.getShortId()));
    }
}
