package com.spdb.fdev.release.service.impl;


import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IReleaseBatchDao;
import com.spdb.fdev.release.dao.IReleaseTaskDao;
import com.spdb.fdev.release.entity.ReleaseBatchRecord;
import com.spdb.fdev.release.entity.ReleaseBindRelation;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IComponenService;
import com.spdb.fdev.release.service.IReleaseBatchService;
import com.spdb.fdev.release.service.IReleaseNodeService;
import com.spdb.fdev.release.service.IReleaseTaskService;
import com.spdb.fdev.release.service.ITaskService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 投产批次业务实现类
 */
@Service
public class ReleaseBatchServiceImpl implements IReleaseBatchService {

    private static Logger logger = LoggerFactory.getLogger(ReleaseApplicationServiceImpl.class);
    @Autowired
    private IReleaseTaskService iReleaseTaskService;
    @Autowired
    private IAppService iAppService;
    @Autowired
    private IReleaseBatchDao iReleaseBatchDao;
    @Autowired
    private ITaskService iTaskService;
    @Autowired
    private IReleaseTaskDao iReleaseTaskDao;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IComponenService componenService;
    @Autowired
	private IReleaseNodeService releaseNodeService;
    @Autowired
    private ITaskService taskService;
    @Autowired
	private IReleaseTaskDao releaseTaskDao;

    /**
     * 查询任务批次信息
     *
     * @param taskId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryBatchInfoByTaskId(String taskId, String appId, String releaseNodeName) throws Exception {
        Map<String, Object> result = new HashMap<>();
        //判断appId是否为空
        if (CommonUtils.isNullOrEmpty(appId)) {
            if (CommonUtils.isNullOrEmpty(taskId)) {
                logger.error("taskId and appId could not both be empty");
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务id和应用id不能同时为空"});
            }
            //为空则说明是挂载窗口调用，根据任务id查询投产任务，查处应用id和投产窗口
            ReleaseTask releaseTask = iReleaseTaskService.findOneTask(taskId, null);
            releaseNodeName = releaseTask.getRelease_node_name();//投产窗口名
            appId = String.valueOf(releaseTask.getApplication_id());//应用id
        } else {
            //有应用id，投产窗口就不能为空
            if (CommonUtils.isNullOrEmpty(releaseNodeName)) {
                logger.error("releaseNodeName could not be empty when appId exist");
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用id和投产窗口必须同时作为请求参数"});
            }
        }
        ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseNodeName);
        String applicationType = (String) taskService.queryTaskDetail(taskId).get("applicationType");
        if ("4".equals(releaseNode.getType())) {
        	Map<String, Object> component = new HashMap<String, Object>();
        	//查询组件
        	if ("componentWeb".equals(applicationType)) {
        		component = componenService.queryMpassComponentDetail(appId);
			} else {
				component = componenService.queryComponenbyid(appId);
			}
	        //查询已有批次信息，若没有，说明应用下任务尚未挂窗口或者应用还没被关联过
	        ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
	        String batchId = "";
	        String modifyReason = "";
	        if (!CommonUtils.isNullOrEmpty(releaseBatchRecord)) {
	            batchId = releaseBatchRecord.getBatch_id();
	            modifyReason = releaseBatchRecord.getModify_reason();
	        }
	        //查询关联应用
	        List<Map<String, String>> bindApps = new ArrayList<>();
	        String bindPeople = "";
	        ReleaseBindRelation releaseBindRelation = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, appId);
	        List<String> applications = new ArrayList<>();
	        //如果有绑定关系
	        if (!CommonUtils.isNullOrEmpty(releaseBindRelation)) {
	            applications = releaseBindRelation.getApplications();
	            applications.remove(appId);
	            if (!CommonUtils.isNullOrEmpty(applications)) {
	                bindApps = componenService.getComponenByIdsOrGitlabIds(applications, releaseNodeName,applicationType);
	                bindPeople = String.join(",", iReleaseBatchDao.queryBindPeople(applications, releaseNodeName));
	            }
	        }
	        //查询同需求下其他投产的应用作为可绑定的选项
	        List<Map<String, String>> couldBindApps = new ArrayList<>();
	        String rqrmntNo = "";
	        if(!CommonUtils.isNullOrEmpty(taskId)){
	            Map taskInfo = iTaskService.queryTaskDetail(taskId);
	            if(!CommonUtils.isNullOrEmpty(taskInfo)){
	                rqrmntNo = String.valueOf(taskInfo.get(Dict.RQRMNT_NO));
	            }
	        }else{
	            rqrmntNo = iReleaseTaskDao.queryRqrmntNoByAppIdReleaseNode(appId, releaseNodeName);
	        }
	        List<String> couldBindAppIds = iReleaseTaskDao.queryCounldBindAppIds(rqrmntNo, releaseNodeName);
	        couldBindAppIds.addAll(applications);
	        couldBindAppIds.remove(appId);
	        if (!CommonUtils.isNullOrEmpty(couldBindAppIds)) {
	            couldBindApps = iAppService.getAppByIdsOrGitlabIds(couldBindAppIds, releaseNodeName);
	        }
	        result.put(Dict.RELEASE_NODE_NAME, releaseNodeName);//投产窗口名
	        result.put(Dict.APPLICATION_ID, appId);//应用id
	        result.put(Dict.APP_NAME_EN, component.get(Dict.NAME_EN));//应用名
	        result.put(Dict.BATCH_ID, batchId);//批次号
	        result.put(Dict.BIND_APP, bindApps);//绑定应用
	        result.put(Dict.COULD_BIND_APP, couldBindApps);//可绑定应用
	        result.put(Dict.BIND_PEOPLE, bindPeople);//已绑定应用联系人
	        result.put(Dict.MODIFY_REASON, modifyReason);//已绑定应用联系人
		} else if ("5".equals(releaseNode.getType())) {
			Map<String, Object> archetype = new HashMap<String, Object>();
			//查询骨架
        	if ("archetypeWeb".equals(applicationType)) {
        		archetype = componenService.queryMpassArchetypeDetail(appId);
			} else {
				archetype = componenService.queryArchetypeDetail(appId);
			}
	        //查询已有批次信息，若没有，说明应用下任务尚未挂窗口或者应用还没被关联过
	        ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
	        String batchId = "";
	        String modifyReason = "";
	        if (!CommonUtils.isNullOrEmpty(releaseBatchRecord)) {
	            batchId = releaseBatchRecord.getBatch_id();
	            modifyReason = releaseBatchRecord.getModify_reason();
	        }
	        //查询关联应用
	        List<Map<String, String>> bindApps = new ArrayList<>();
	        String bindPeople = "";
	        ReleaseBindRelation releaseBindRelation = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, appId);
	        List<String> applications = new ArrayList<>();
	        //如果有绑定关系
	        if (!CommonUtils.isNullOrEmpty(releaseBindRelation)) {
	            applications = releaseBindRelation.getApplications();
	            applications.remove(appId);
	            if (!CommonUtils.isNullOrEmpty(applications)) {
	            	bindApps = componenService.getComponenByIdsOrGitlabIds(applications, releaseNodeName,applicationType);
	                bindPeople = String.join(",", iReleaseBatchDao.queryBindPeople(applications, releaseNodeName));
	            }
	        }
	        //查询同需求下其他投产的应用作为可绑定的选项
	        List<Map<String, String>> couldBindApps = new ArrayList<>();
	        String rqrmntNo = "";
	        if(!CommonUtils.isNullOrEmpty(taskId)){
	            Map taskInfo = iTaskService.queryTaskDetail(taskId);
	            if(!CommonUtils.isNullOrEmpty(taskInfo)){
	                rqrmntNo = String.valueOf(taskInfo.get(Dict.RQRMNT_NO));
	            }
	        }else{
	            rqrmntNo = iReleaseTaskDao.queryRqrmntNoByAppIdReleaseNode(appId, releaseNodeName);
	        }
	        List<String> couldBindAppIds = iReleaseTaskDao.queryCounldBindAppIds(rqrmntNo, releaseNodeName);
	        couldBindAppIds.addAll(applications);
	        couldBindAppIds.remove(appId);
	        if (!CommonUtils.isNullOrEmpty(couldBindAppIds)) {
	            couldBindApps = iAppService.getAppByIdsOrGitlabIds(couldBindAppIds, releaseNodeName);
	        }
	        result.put(Dict.RELEASE_NODE_NAME, releaseNodeName);//投产窗口名
	        result.put(Dict.APPLICATION_ID, appId);//应用id
	        result.put(Dict.APP_NAME_EN, archetype.get(Dict.NAME_EN));//应用名
	        result.put(Dict.BATCH_ID, batchId);//批次号
	        result.put(Dict.BIND_APP, bindApps);//绑定应用
	        result.put(Dict.COULD_BIND_APP, couldBindApps);//可绑定应用
	        result.put(Dict.BIND_PEOPLE, bindPeople);//已绑定应用联系人
	        result.put(Dict.MODIFY_REASON, modifyReason);//已绑定应用联系人
		} else if ("6".equals(releaseNode.getType())) {
			//查询应用
	        Map<String, Object> image = componenService.queryBaseImageDetail(appId);
	        //查询已有批次信息，若没有，说明应用下任务尚未挂窗口或者应用还没被关联过
	        ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
	        String batchId = "";
	        String modifyReason = "";
	        if (!CommonUtils.isNullOrEmpty(releaseBatchRecord)) {
	            batchId = releaseBatchRecord.getBatch_id();
	            modifyReason = releaseBatchRecord.getModify_reason();
	        }
	        //查询关联应用
	        List<Map<String, String>> bindApps = new ArrayList<>();
	        String bindPeople = "";
	        ReleaseBindRelation releaseBindRelation = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, appId);
	        List<String> applications = new ArrayList<>();
	        //如果有绑定关系
	        if (!CommonUtils.isNullOrEmpty(releaseBindRelation)) {
	            applications = releaseBindRelation.getApplications();
	            applications.remove(appId);
	            if (!CommonUtils.isNullOrEmpty(applications)) {
	            	bindApps = componenService.getComponenByIdsOrGitlabIds(applications, releaseNodeName,applicationType);
	                bindPeople = String.join(",", iReleaseBatchDao.queryBindPeople(applications, releaseNodeName));
	            }
	        }
	        //查询同需求下其他投产的应用作为可绑定的选项
	        List<Map<String, String>> couldBindApps = new ArrayList<>();
	        String rqrmntNo = "";
	        if(!CommonUtils.isNullOrEmpty(taskId)){
	            Map taskInfo = iTaskService.queryTaskDetail(taskId);
	            if(!CommonUtils.isNullOrEmpty(taskInfo)){
	                rqrmntNo = String.valueOf(taskInfo.get(Dict.RQRMNT_NO));
	            }
	        }else{
	            rqrmntNo = iReleaseTaskDao.queryRqrmntNoByAppIdReleaseNode(appId, releaseNodeName);
	        }
	        List<String> couldBindAppIds = iReleaseTaskDao.queryCounldBindAppIds(rqrmntNo, releaseNodeName);
	        couldBindAppIds.addAll(applications);
	        couldBindAppIds.remove(appId);
	        if (!CommonUtils.isNullOrEmpty(couldBindAppIds)) {
	            couldBindApps = iAppService.getAppByIdsOrGitlabIds(couldBindAppIds, releaseNodeName);
	        }
	        result.put(Dict.RELEASE_NODE_NAME, releaseNodeName);//投产窗口名
	        result.put(Dict.APPLICATION_ID, appId);//应用id
	        result.put(Dict.APP_NAME_EN, image.get(Dict.NAME));//应用名
	        result.put(Dict.BATCH_ID, batchId);//批次号
	        result.put(Dict.BIND_APP, bindApps);//绑定应用
	        result.put(Dict.COULD_BIND_APP, couldBindApps);//可绑定应用
	        result.put(Dict.BIND_PEOPLE, bindPeople);//已绑定应用联系人
	        result.put(Dict.MODIFY_REASON, modifyReason);//已绑定应用联系人
		} else {
        	//查询应用
            Map<String, Object> app = iAppService.queryAPPbyid(appId);
            //查询已有批次信息，若没有，说明应用下任务尚未挂窗口或者应用还没被关联过
            ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
            String batchId = "";
            String modifyReason = "";
            if (!CommonUtils.isNullOrEmpty(releaseBatchRecord)) {
                batchId = releaseBatchRecord.getBatch_id();
                modifyReason = releaseBatchRecord.getModify_reason();
            }
            //查询关联应用
            List<Map<String, String>> bindApps = new ArrayList<>();
            String bindPeople = "";
            ReleaseBindRelation releaseBindRelation = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, appId);
            List<String> applications = new ArrayList<>();
            //如果有绑定关系
            if (!CommonUtils.isNullOrEmpty(releaseBindRelation)) {
                applications = releaseBindRelation.getApplications();
                applications.remove(appId);
                if (!CommonUtils.isNullOrEmpty(applications)) {
                    bindApps = iAppService.getAppByIdsOrGitlabIds(applications, releaseNodeName);
                    bindPeople = String.join(",", iReleaseBatchDao.queryBindPeople(applications, releaseNodeName));
                }
            }
            //查询同需求下其他投产的应用作为可绑定的选项
            List<Map<String, String>> couldBindApps = new ArrayList<>();
            String rqrmntNo = "";
            if(!CommonUtils.isNullOrEmpty(taskId)){
                Map taskInfo = iTaskService.queryTaskDetail(taskId);
                if(!CommonUtils.isNullOrEmpty(taskInfo)){
                    rqrmntNo = String.valueOf(taskInfo.get(Dict.RQRMNT_NO));
                }
            }else{
                rqrmntNo = iReleaseTaskDao.queryRqrmntNoByAppIdReleaseNode(appId, releaseNodeName);
            }
            List<String> couldBindAppIds = iReleaseTaskDao.queryCounldBindAppIds(rqrmntNo, releaseNodeName);
            couldBindAppIds.addAll(applications);
            couldBindAppIds.remove(appId);
            if (!CommonUtils.isNullOrEmpty(couldBindAppIds)) {
                couldBindApps = iAppService.getAppByIdsOrGitlabIds(couldBindAppIds, releaseNodeName);
            }
            result.put(Dict.RELEASE_NODE_NAME, releaseNodeName);//投产窗口名
            result.put(Dict.APPLICATION_ID, appId);//应用id
            result.put(Dict.APP_NAME_EN, app.get(Dict.NAME_EN));//应用名
            result.put(Dict.BATCH_ID, batchId);//批次号
            result.put(Dict.BIND_APP, bindApps);//绑定应用
            result.put(Dict.COULD_BIND_APP, couldBindApps);//可绑定应用
            result.put(Dict.BIND_PEOPLE, bindPeople);//已绑定应用联系人
            result.put(Dict.MODIFY_REASON, modifyReason);//已绑定应用联系人
		}
        return result;
    }

    /**
     * 新增批次信息
     *
     * @param releaseNodeName
     * @param batchId
     * @param appId
     * @param modifyReason
     * @param bindAppIds
     * @throws Exception
     */
    @Override
    public void addBatch(String releaseNodeName, String batchId, String appId, String modifyReason, List<String> bindAppIds) throws Exception {
        User user = CommonUtils.getSessionUser();
        String userNameEn = user.getUser_name_en();
        String userNameCn = user.getUser_name_cn();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        //判断应用在当前窗口是否锁定批次
        ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
        if (CommonUtils.isNullOrEmpty(releaseBatchRecord)) {
            //未锁定过,则新增锁定记录
            iReleaseBatchDao.addBatchRecord(appId, releaseNodeName, batchId, modifyReason, userNameEn, userNameCn, dateStr);
        } else {
            //锁定过，比较批次是否变更
            if (!batchId.equals(releaseBatchRecord.getBatch_id())) {
                //批次变更了，新增一条批次记录
                iReleaseBatchDao.addBatchRecord(appId, releaseNodeName, batchId, modifyReason, userNameEn, userNameCn, dateStr);
            }
        }
        //查询当前app绑定关系
        ReleaseBindRelation current = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, appId);
        //遍历已绑定app
        List<String> alrbindAppids = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(current)) {
            alrbindAppids = current.getApplications();
            Iterator<String> iterator = alrbindAppids.iterator();
            while (iterator.hasNext()) {
                String alrbindAppid = iterator.next();
                if (!bindAppIds.contains(alrbindAppid) && !alrbindAppid.equals(appId)) {
                    iterator.remove();
                    iReleaseBatchDao.updateBindRelation(current.getRelease_node_name(), current.getBatch_id(), alrbindAppids);
                }
            }
        }
        current = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, appId);
        //如果新增了绑定应用
        if (!CommonUtils.isNullOrEmpty(bindAppIds)) {
            for (String app : bindAppIds) {
                //查询被绑定app的锁定记录
                ReleaseBatchRecord bindReleaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, app);
                if (CommonUtils.isNullOrEmpty(bindReleaseBatchRecord)) {
                    //未锁定过,说明没有绑定关系，则新增锁定记录，并增加绑定关系
                    iReleaseBatchDao.addBatchRecord(app, releaseNodeName, batchId, modifyReason, userNameEn, userNameCn, dateStr);
                    if (!CommonUtils.isNullOrEmpty(current)) {
                        //如果当前应用已有绑定关系，则添加绑定应用id
                        List<String> currentAppIds = current.getApplications();
                        currentAppIds.add(app);
                        iReleaseBatchDao.updateBindRelation(releaseNodeName, batchId, currentAppIds);
                    } else {
                        //如果当前应用没有绑定关系，那么新增一条绑定关系
                        List<String> newAppIds = new ArrayList<>();
                        newAppIds.add(app);
                        newAppIds.add(appId);
                        ReleaseBindRelation newReleaseBindRelation = new ReleaseBindRelation();
                        newReleaseBindRelation.setRelease_node_name(releaseNodeName);
                        newReleaseBindRelation.setApplications(newAppIds);
                        newReleaseBindRelation.setBatch_id(batchId);
                        iReleaseBatchDao.addBindRelation(newReleaseBindRelation);
                    }
                } else {
                    //目标app被锁定过，比较批次是否变更
                    if (!batchId.equals(bindReleaseBatchRecord.getBatch_id())) {
                        //批次变更了，新增一条批次记录
                        iReleaseBatchDao.addBatchRecord(app, releaseNodeName, batchId, modifyReason, userNameEn, userNameCn, dateStr);
                    }
                    //查看绑定关系
                    ReleaseBindRelation releaseBindRelation = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, app);
                    if (!CommonUtils.isNullOrEmpty(releaseBindRelation)) {
                        //如果目标app存在绑定关系
                        if (!releaseBindRelation.getApplications().contains(appId)) {
                            //如果目标app绑定关系里没有当前应用id，那么目标app要脱离原绑定关系；如果已有绑定关系包含当前应用id，那么无需改变
                            ReleaseBindRelation old = iReleaseBatchDao.queryBindAppByAppId(releaseNodeName, app);
                            List<String> appIdsafterdelete = old.getApplications();
                            appIdsafterdelete.remove(app);
                            iReleaseBatchDao.updateBindRelation(releaseNodeName, old.getBatch_id(), appIdsafterdelete);
                            if (!CommonUtils.isNullOrEmpty(current)) {
                                //如果当前应用已有绑定关系，则添加绑定应用id
                                List<String> currentAppIds = current.getApplications();
                                currentAppIds.add(app);
                                iReleaseBatchDao.updateBindRelation(releaseNodeName, batchId, currentAppIds);
                            } else {
                                //如果当前应用没有绑定关系，那么新增一条绑定关系
                                List<String> newAppIds = new ArrayList<>();
                                newAppIds.add(app);
                                newAppIds.add(appId);
                                ReleaseBindRelation newReleaseBindRelation = new ReleaseBindRelation();
                                newReleaseBindRelation.setRelease_node_name(releaseNodeName);
                                newReleaseBindRelation.setApplications(newAppIds);
                                newReleaseBindRelation.setBatch_id(batchId);
                                iReleaseBatchDao.addBindRelation(newReleaseBindRelation);
                            }
                        }
                    } else {
                        if (!CommonUtils.isNullOrEmpty(current)) {
                            //如果当前应用已有绑定关系，则添加绑定应用id
                            List<String> currentAppIds = current.getApplications();
                            currentAppIds.add(app);
                            iReleaseBatchDao.updateBindRelation(releaseNodeName, batchId, currentAppIds);
                        } else {
                            //如果当前应用没有绑定关系，那么新增一条绑定关系
                            List<String> newAppIds = new ArrayList<>();
                            newAppIds.add(app);
                            newAppIds.add(appId);
                            ReleaseBindRelation newReleaseBindRelation = new ReleaseBindRelation();
                            newReleaseBindRelation.setRelease_node_name(releaseNodeName);
                            newReleaseBindRelation.setApplications(newAppIds);
                            newReleaseBindRelation.setBatch_id(batchId);
                            iReleaseBatchDao.addBindRelation(newReleaseBindRelation);
                        }
                    }
                }
            }
        } else {
            //如果没有新增绑定
            if (CommonUtils.isNullOrEmpty(current)) {
                //如果当前应用没有绑定关系,查询当前批次有没有绑定关系存在
                ReleaseBindRelation releaseBindRelation = iReleaseBatchDao.queryBindAppByBatchId(releaseNodeName, batchId);
                if (CommonUtils.isNullOrEmpty(releaseBindRelation)) {
                    //如果当前批次没有绑定关系，新增绑定关系
                    List<String> newAppIds = new ArrayList<>();
                    newAppIds.add(appId);
                    ReleaseBindRelation newReleaseBindRelation = new ReleaseBindRelation();
                    newReleaseBindRelation.setRelease_node_name(releaseNodeName);
                    newReleaseBindRelation.setApplications(newAppIds);
                    newReleaseBindRelation.setBatch_id(batchId);
                    iReleaseBatchDao.addBindRelation(newReleaseBindRelation);
                } else {
                    //如果当前批次存在绑定关系，将当前应用添加进该绑定关系
                    List<String> currentAppIds = releaseBindRelation.getApplications();
                    currentAppIds.add(appId);
                    iReleaseBatchDao.updateBindRelation(releaseNodeName, batchId, currentAppIds);
                }
            }
        }
    }

    /**
     * 根据应用id查询任务批次信息
     *
     * @param appIds
     * @param releaseNodeName
     * @return
     * @throws Exception
     */

    @Override
    public List<Map<String, String>> queryBatchInfoByAppId(List<String> appIds, String releaseNodeName) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        for (String appId : appIds) {
            Map<String, String> element = new HashMap<>();
            ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
            ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseNodeName);
            Map<String, Object> app = new HashMap<String, Object>();
            List<ReleaseTask> tasks = releaseTaskDao.querySameAppTask(appId,releaseNodeName);
        	String taskId = tasks.get(0).getTask_id();
        	String applicationType = (String) taskService.queryTaskDetail(taskId).get("applicationType");
            if ("4".equals(releaseNode.getType())) {
            	if ("componentWeb".equals(applicationType)) {
            		app = componenService.queryMpassComponentDetail(appId);
    			} else {
    				app = componenService.queryComponenbyid(appId);
    			}
			} else if ("5".equals(releaseNode.getType())) {
				if ("archetypeWeb".equals(applicationType)) {
            		app = componenService.queryMpassArchetypeDetail(appId);
    			} else {
    				app = componenService.queryArchetypeDetail(appId);
    			}
			} else if ("6".equals(releaseNode.getType())) {
				app = componenService.queryBaseImageDetail(appId);
			} else {
				app = iAppService.queryAPPbyid(appId);
			}
            //查询应用
            element.put(Dict.APPLICATION_ID, appId);
            element.put(Dict.APP_NAME_EN, String.valueOf(app.get(Dict.NAME_EN)));//应用名
            element.put(Dict.BIND_PEOPLE, releaseBatchRecord.getUser_name_cn());
            result.add(element);
        }
        return result;
    }

    /**
     * 根据投产窗口和批次查应用集合（变更用）
     *
     * @param releaseNodeName
     * @param batchId
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> queryAppIdsByNodeAndBatch(String releaseNodeName, String batchId) throws Exception {
        ReleaseBindRelation releaseBindRelation = iReleaseBatchDao.queryBindAppByBatchId(releaseNodeName, batchId);
        releaseBindRelation.set_id(null);
        List<Map<String, String>> result = iAppService.getAppByIdsOrGitlabIds(releaseBindRelation.getApplications(), releaseNodeName);
        return result;
    }

    @Override
    public List<ReleaseTask> batchReleaseTask(String date) throws Exception {
        List<ReleaseTask> releaseTasks = iReleaseBatchDao.batchReleaseTask(date);
        List<ReleaseTask> result = new ArrayList<>();
        for (ReleaseTask releaseTask : releaseTasks) {
            Map<String, Object> task = new HashMap<>();
            if (CommonUtils.isNullOrEmpty(releaseTask.getRqrmnt_no())) {
                String taskId = releaseTask.getTask_id();
                try {
                    task = iTaskService.queryTaskDetail(taskId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String rqrmntNo = "";
                if (!CommonUtils.isNullOrEmpty(task)) {
                    rqrmntNo = String.valueOf(task.get(Dict.RQRMNT_NO));
                }
                releaseTask.setRqrmnt_no(rqrmntNo);
                iReleaseBatchDao.updateReleaseTask(releaseTask);
                result.add(releaseTask);
            }
        }
        return result;
    }
}
