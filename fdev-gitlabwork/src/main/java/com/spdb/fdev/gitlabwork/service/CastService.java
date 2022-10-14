package com.spdb.fdev.gitlabwork.service;


import com.spdb.fdev.gitlabwork.entiy.SitMergedInfo;

import java.util.List;
import java.util.Map;

public interface CastService {

    void save(SitMergedInfo obj);

    List<SitMergedInfo> query(Map query);


}
