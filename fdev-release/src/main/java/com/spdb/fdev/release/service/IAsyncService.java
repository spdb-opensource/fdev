package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseApplication;

import java.util.List;

/**
 * Created by xxx on 上午11:16.
 */
public interface IAsyncService {
	/**
	 * 异步修改投产应用mergestate
	 * @param releaseApplications
	 * @throws Exception
	 */
    void updateApplicationMergeState(List<ReleaseApplication> releaseApplications)throws Exception;
    
}