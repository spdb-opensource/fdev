package com.spdb.fdev.gitlabwork.dao;



import com.spdb.fdev.gitlabwork.entiy.SitMergedInfo;

import java.util.List;
import java.util.Map;

public interface CastDao {

    void save(SitMergedInfo obj);

    List<SitMergedInfo> query(Map query);

}
