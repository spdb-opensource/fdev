package com.spdb.fdev.gitlabwork.service;

import com.spdb.fdev.gitlabwork.dto.CommitUpdate;

import java.util.List;

public interface CommitService {

    void updateCommit(CommitUpdate commitUpdate) throws Exception;

    void updateCommitStats() throws Exception;

    List<String> queryCommitDiff(Integer project, String shortId) throws Exception;

}
