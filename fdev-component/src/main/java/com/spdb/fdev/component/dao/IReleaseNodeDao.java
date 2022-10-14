package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.MpassReleaseIssue;
import com.spdb.fdev.component.entity.ReleaseNode;

public interface IReleaseNodeDao {

    ReleaseNode   save(ReleaseNode releaseNode) throws Exception;

    ReleaseNode   queryByAppIdAndReleaseNodeName(String appId,String releaseNodeName) throws Exception;

    ReleaseNode   update(ReleaseNode releaseNode) throws Exception;

}
