package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ReleaseCycle;

import java.util.List;

public interface IReleaseCycleDao {
    ReleaseCycle save(ReleaseCycle releaseCycle);

    ReleaseCycle deleteByReleaseNodeName(String releaseNodeName);

    ReleaseCycle queryByReleaseNodeName(String releaseNodeName);

    ReleaseCycle update(ReleaseCycle releaseCycle);

    List<ReleaseCycle> queryAll();
}
