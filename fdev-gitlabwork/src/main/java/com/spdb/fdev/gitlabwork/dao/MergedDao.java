package com.spdb.fdev.gitlabwork.dao;


import com.spdb.fdev.gitlabwork.entiy.MergedInfo;

import java.util.Map;

public interface MergedDao {

    MergedInfo saveMergedInfo(MergedInfo mergedInfo);

    Map queryMergedInfo(Map params);

}
