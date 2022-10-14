package com.spdb.executor.dao;

import com.csii.pe.pojo.*;

public interface CollectGitlabBranchNewDao {

    void saveReleaseBranch(NewReleaseBranch releaseBranch) throws Exception;

    void saveTagBranch(NewProductTag productTag) throws Exception;

    void saveFeatureBranch(NewFeatureBranch featureBranch) throws Exception;


}
