package com.spdb.job.service;

import java.util.List;

public interface IElasticSearchService {

    public List queryJobLog(String schedulerSeqNo);
}
