package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xxx on 上午11:16.
 */
@Service
@RefreshScope
public class AsyncServiceImpl implements IAsyncService {

	private static Logger logger = LoggerFactory.getLogger(AsyncServiceImpl.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private IAppService appService;

	@Autowired
	private IRelDevopsRecordService relDevopsRecordService;

	@Async("taskExecutor")
	@Override
	public void updateApplicationMergeState(List<ReleaseApplication> releaseApplications) throws Exception {
		logger.info("begin update application merge state...");
		for (ReleaseApplication app : releaseApplications) {
			RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
			relDevopsRecord.setApplication_id(app.getApplication_id());
			relDevopsRecord.setRelease_node_name(app.getRelease_node_name());
			List<RelDevopsRecord> List = relDevopsRecordService.query(relDevopsRecord);
			for (RelDevopsRecord rel : List) {
				if (!CommonUtils.isNullOrEmpty(rel.getMerge_request_id())
						&& Constants.DEVOPS_MERGEREQ_CREATED.equals(rel.getDevops_status())) {
					ReleaseApplication releaseApplication = new ReleaseApplication();
					try {
						String token = userService.queryAppManagerGitlabToken(app.getApplication_id());
						String merge_request_status = appService.getMergeRequestInfo(token, app.getApplication_id(),
								rel.getMerge_request_id());

						releaseApplication.setApplication_id(app.getApplication_id());
						releaseApplication.setRelease_node_name(app.getRelease_node_name());
						if (Dict.MERGED.equals(merge_request_status)) {// 若分支合并状态为merged
							rel.setDevops_status(Constants.DEVOPS_MERGEREQ_MERGED);
							relDevopsRecordService.setDevStatus(rel);
						} else if (Dict.CLOSED.equals(merge_request_status)) {// 若分支合并状态为closed
							rel.setDevops_status(Constants.DEVOPS_MERGEREQ_CLOSED);
							relDevopsRecordService.setDevStatus(rel);
						}
					} catch (Exception e) {
						logger.error("begin update application merge state error with:" , e);
					}
				}
			}
		}
		logger.info("end update application merge state...");
	}

}