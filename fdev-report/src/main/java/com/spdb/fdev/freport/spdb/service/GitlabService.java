package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.dto.gitlab.MergeRequestDto;
import com.spdb.fdev.freport.spdb.dto.gitlab.WebHooksDto;

import java.util.List;

public interface GitlabService {

    void dealWebHooksMerge(WebHooksDto webHooksDto) throws Exception;

    List<MergeRequestDto> getProjectMergeRequest(String projectId) throws Exception;
}
