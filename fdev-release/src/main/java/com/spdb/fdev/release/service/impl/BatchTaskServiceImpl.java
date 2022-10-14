package com.spdb.fdev.release.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IBatchTaskDao;
import com.spdb.fdev.release.entity.BatchTaskInfo;
import com.spdb.fdev.release.entity.NoteSql;
import com.spdb.fdev.release.service.IBatchTaskService;

@Service
@RefreshScope
public class BatchTaskServiceImpl implements IBatchTaskService   {

	@Autowired
	private IBatchTaskDao batchTaskDao;
	
	@Override
	public void createBatchTask(Map<String, Object> requestParam) throws Exception {
		User user = CommonUtils.getSessionUser();
		BatchTaskInfo batchTaskInfo = new BatchTaskInfo();
		StringBuffer batchInfo = assembleBatchInfo(requestParam);
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String application_name_en = (String) requestParam.get(Dict.APPLICATION_NAME_EN);
		String application_name_cn = (String) requestParam.get(Dict.APPLICATION_NAME_CN);
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String type = (String) requestParam.get(Dict.TYPE);
		String executorId = (String) requestParam.get("executorId");
		String transName = (String) requestParam.get("transName");
		String jobGroup = (String) requestParam.get("jobGroup");
		String description = (String) requestParam.get("description");
		String cronExpression = (String) requestParam.get("cronExpression");
		String misfireInstr = (String) requestParam.get("misfireInstr");
		String fireTime = (String) requestParam.get("fireTime");
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		String batch_no = (String) requestParam.get("batch_no");
		String user_name_cn = user.getUser_name_cn();
		String user_name_phone = user.getTelephone();
		String create_time = CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN);
		ObjectId objectId = new ObjectId();
		batchTaskInfo.set_id(objectId);
		batchTaskInfo.setId(objectId.toString());
		batchTaskInfo.setApplication_id(application_id);
		batchTaskInfo.setApplication_name_cn(application_name_cn);
		batchTaskInfo.setApplication_name_en(application_name_en);
		batchTaskInfo.setCreate_time(create_time);
		batchTaskInfo.setCronExpression(cronExpression);
		batchTaskInfo.setDescription(description);
		batchTaskInfo.setExecutorId(executorId);
		batchTaskInfo.setFireTime(fireTime);
		batchTaskInfo.setJobGroup(jobGroup);
		batchTaskInfo.setMisfireInstr(misfireInstr);
		batchTaskInfo.setNote_id(note_id);
		batchTaskInfo.setProd_id("");
		batchTaskInfo.setTransName(transName);
		batchTaskInfo.setType(type);
		batchTaskInfo.setRelease_node_name(release_node_name);
		batchTaskInfo.setUser_name_cn(user_name_cn);
		batchTaskInfo.setBatchInfo(batchInfo.toString());
		batchTaskInfo.setBatch_no(batch_no);
		batchTaskInfo.setUser_name_phone(user_name_phone);
		batchTaskDao.create(batchTaskInfo);
	}

	@Override
	public void updateBatchTask(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		String type = (String) requestParam.get(Dict.TYPE);//任务类型
		String executorId = (String) requestParam.get("executorId");
		String transName = (String) requestParam.get("transName");
		String jobGroup = (String) requestParam.get("jobGroup");
		String description = (String) requestParam.get("description");
		String cronExpression = (String) requestParam.get("cronExpression");
		String misfireInstr = (String) requestParam.get("misfireInstr");
		String fireTime = (String) requestParam.get("fireTime");
		String batchInfo = assembleBatchInfo(requestParam).toString();
		batchTaskDao.updateBatchTaskInfo(id,type,executorId,transName,jobGroup,description,cronExpression,misfireInstr,fireTime,batchInfo);
	}

	@Override
	public void deteleBatchTask(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String flag = (String) requestParam.get("flag");//1-变更 2-发布说明
		if ("1".equals(flag)) {
			String btachNo = batchTaskDao.queryBatchTaskInfoById(id).getBatch_no();
			batchTaskDao.updateBatchTaskProdId(id, "","0");
			List<BatchTaskInfo> batchTaskInfoList = batchTaskDao.queryBatchTaskInfoByProdIdAndAppId(prod_id,application_id);
			for (BatchTaskInfo batchTaskInfo : batchTaskInfoList) {
                if (!CommonUtils.isNullOrEmpty(batchTaskInfo.getBatch_no())) {
                    if (Integer.parseInt(btachNo) < Integer.parseInt(batchTaskInfo.getBatch_no())) {
                    	batchTaskInfo.setBatch_no((Integer.parseInt(batchTaskInfo.getBatch_no()) - 1) + "");
                    	batchTaskDao.updateNoteBatchNo(batchTaskInfo.getId(),batchTaskInfo.getBatch_no());
                    }
                }
            }
		}
		if ("2".equals(flag)) {
			BatchTaskInfo batchTaskInfo = batchTaskDao.queryBatchTaskInfoById(id);
			if (!CommonUtils.isNullOrEmpty(batchTaskInfo.getProd_id())) {
				throw new FdevException(ErrorConstants.BATCHTASK_IS_USED_CAN_NOT_DELETE);
			} else {
				batchTaskDao.deleteBatchTaskInfo(id);
			}
		}
	}

	@Override
	public List<Map<String, Object>> queryBatchTask(Map<String, Object> requestParam) throws Exception {
		List<BatchTaskInfo>  batchTaskInfos= batchTaskDao.queryBatchTypeList(requestParam);
		List<Map<String, Object>> batchTaskTypes = new ArrayList<Map<String,Object>>();
		for (BatchTaskInfo batchTaskInfo : batchTaskInfos) {
			Map<String, Object> type = new HashMap<String, Object>();
			type.put(Dict.ID, batchTaskInfo.getId());
			type.put(Dict.TYPE, batchTaskInfo.getType());
			type.put(Dict.USER_NAME_CN, batchTaskInfo.getUser_name_cn());
			type.put(Dict.CREATE_TIME, batchTaskInfo.getCreate_time());
			type.put("jobGroup", batchTaskInfo.getJobGroup());
			type.put("transName", batchTaskInfo.getTransName());
			batchTaskTypes.add(type);
		}
		return batchTaskTypes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addBatchTask(Map<String, Object> requestParam) throws Exception {
		List<Map<String, Object>> ids = (List<Map<String, Object>>) requestParam.get(Dict.IDS);
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		for (Map<String, Object> id : ids) {
			String batchId = (String) id.get("id");
			String batchNo = (String) id.get("batch_no");
			batchTaskDao.updateBatchTaskProdId(batchId,prod_id,batchNo);
		}
	}

	@Override
	public List<Map<String, Object>> queryBatchTaskList(Map<String, Object> requestParam) throws Exception {
		List<BatchTaskInfo> batchTaskInfos = batchTaskDao.queryBatchTaskInfoList(requestParam);
		List<Map<String, Object>> batchTaskInfoList = new ArrayList<>();
		List<String> batchTaskApplications = new ArrayList<>();
		for (BatchTaskInfo batchTaskInfo : batchTaskInfos) {
			batchTaskApplications.add(batchTaskInfo.getApplication_id());
		}
		batchTaskApplications = ((List<String>) batchTaskApplications.stream().distinct().collect(Collectors.toList()));
		for (int i = 0; i < batchTaskApplications.size(); i++) {
			Map<String, Object> batchTaskMap = new HashMap<>();
			List<Map<String, Object>> batchInfoList = new ArrayList<>();
			for (int j = 0; j < batchTaskInfos.size(); j++) {
				if (batchTaskInfos.get(j).getApplication_id().equals(batchTaskApplications.get(i))) {
					batchTaskMap.put(Dict.APPLICATION_NAME_EN, batchTaskInfos.get(j).getApplication_name_en());
					Map<String, Object> batchInfo = new HashMap<>();
					batchInfo.put(Dict.ID, batchTaskInfos.get(j).getId());
					batchInfo.put(Dict.RELEASE_NODE_NAME, batchTaskInfos.get(j).getRelease_node_name());
					batchInfo.put(Dict.APPLICATION_ID, batchTaskInfos.get(j).getApplication_id());
					batchInfo.put(Dict.APPLICATION_NAME_EN, batchTaskInfos.get(j).getApplication_name_en());
					batchInfo.put(Dict.APPLICATION_NAME_CN, batchTaskInfos.get(j).getApplication_name_cn());
					batchInfo.put(Dict.TYPE, batchTaskInfos.get(j).getType());
					batchInfo.put("executorId", batchTaskInfos.get(j).getExecutorId());
					batchInfo.put("transName", batchTaskInfos.get(j).getTransName());
					batchInfo.put("jobGroup", batchTaskInfos.get(j).getJobGroup());
					batchInfo.put("description", batchTaskInfos.get(j).getDescription());
					batchInfo.put("cronExpression", batchTaskInfos.get(j).getCronExpression());
					batchInfo.put("misfireInstr", batchTaskInfos.get(j).getMisfireInstr());
					batchInfo.put("fireTime", batchTaskInfos.get(j).getFireTime());
					batchInfo.put(Dict.NOTE_ID, batchTaskInfos.get(j).getNote_id());
					batchInfo.put(Dict.PROD_ID, batchTaskInfos.get(j).getProd_id());
					batchInfo.put(Dict.USER_NAME_CN, batchTaskInfos.get(j).getUser_name_cn());
					batchInfo.put(Dict.CREATE_TIME, batchTaskInfos.get(j).getCreate_time());
					batchInfo.put("batchInfo", batchTaskInfos.get(j).getBatchInfo());
					batchInfo.put("batch_no", batchTaskInfos.get(j).getBatch_no());
					batchInfoList.add(batchInfo);
				}
			}
			batchTaskMap.put(Dict.LIST, batchInfoList);
			batchTaskInfoList.add(batchTaskMap);
		}
		return batchTaskInfoList;
	}
	
	@Override
	public List<Map<String, Object>> queryBatchTaskByProdId(Map<String, Object> requestParam) throws Exception {
		List<BatchTaskInfo> batchTaskInfos = batchTaskDao.queryBatchTaskInfoList(requestParam);
		String prod_id = (String) requestParam.get(Dict.PROD_ID);
		List<String> applicationIds = new ArrayList<>();
		for (BatchTaskInfo batchTaskInfo : batchTaskInfos) {
			applicationIds.add(batchTaskInfo.getApplication_id());
		}
		applicationIds = ((List<String>) applicationIds.stream().distinct().collect(Collectors.toList()));
		List<Map<String, Object>> batchInfos = new ArrayList<>();
		for (int i = 0; i < applicationIds.size(); i++) {
			Map<String, Object> requestParam1 = new HashMap<>();
			requestParam1.put(Dict.APPLICATION_ID, applicationIds.get(i));
			requestParam1.put(Dict.PROD_ID, prod_id);
			List<BatchTaskInfo> batchTaskInfoList = batchTaskDao.queryBatchTaskInfoListByAppId(requestParam1);
			for (BatchTaskInfo batchTaskInfo : batchTaskInfoList) {
				Map<String, Object> batchInfoMap = new HashMap<>();
				batchInfoMap.put("batchInfo",batchTaskInfo.getBatchInfo());
				batchInfos.add(batchInfoMap);
			}
		}
		return batchInfos;
	}

	public StringBuffer assembleBatchInfo(Map<String, Object> requestParam) throws Exception {
		StringBuffer batchInfo = new StringBuffer();
		String type = (String) requestParam.get(Dict.TYPE);//任务类型
		String executorId = (String) requestParam.get("executorId");
		String transName = (String) requestParam.get("transName");
		String jobGroup = (String) requestParam.get("jobGroup");
		String description = (String) requestParam.get("description");
		String cronExpression = (String) requestParam.get("cronExpression");
		String misfireInstr = (String) requestParam.get("misfireInstr");
		String fireTime = (String) requestParam.get("fireTime");
		switch (type) {
		case "httpAddJob":
			batchInfo = batchInfo.append("{").append(Constants.TYPE).append(":").append(Constants.APOSTROPHE).append(type).append(Constants.APOSTROPHE).append(",")
				.append(Constants.JOBGROUP).append(":").append(Constants.APOSTROPHE).append(jobGroup).append(Constants.APOSTROPHE).append(",")
				.append(Constants.EXECUTORID).append(":").append(Constants.APOSTROPHE).append(executorId).append(Constants.APOSTROPHE).append(",")
				.append(Constants.TRANSNAME).append(":").append(Constants.APOSTROPHE).append(transName).append(Constants.APOSTROPHE).append(",")
				.append(Constants.DESCRIPTION).append(":").append(Constants.APOSTROPHE).append(description).append(Constants.APOSTROPHE).append(",")
				.append(Constants.CRONEXPRESSION).append(":").append(Constants.APOSTROPHE).append(cronExpression).append(Constants.APOSTROPHE).append(",")
				.append(Constants.MISFIREINSTR).append(":").append(misfireInstr).append("}");
			break;
		case "httpTriggerJob":
			if (!CommonUtils.isNullOrEmpty(fireTime)) {
				batchInfo = batchInfo.append("{").append(Constants.TYPE).append(":").append(Constants.APOSTROPHE).append(type).append(Constants.APOSTROPHE).append(",")
						.append(Constants.JOBGROUP).append(":").append(Constants.APOSTROPHE).append(jobGroup).append(Constants.APOSTROPHE).append(",")
						.append(Constants.EXECUTORID).append(":").append(Constants.APOSTROPHE).append(executorId).append(Constants.APOSTROPHE).append(",")
						.append(Constants.TRANSNAME).append(":").append(Constants.APOSTROPHE).append(transName).append(Constants.APOSTROPHE).append(",")
						.append(Constants.FIRETIME).append(":").append(Constants.APOSTROPHE).append(fireTime).append(Constants.APOSTROPHE).append("}");
			} else {
				batchInfo = batchInfo.append("{").append(Constants.TYPE).append(":").append(Constants.APOSTROPHE).append(type).append(Constants.APOSTROPHE).append(",")
						.append(Constants.JOBGROUP).append(":").append(Constants.APOSTROPHE).append(jobGroup).append(Constants.APOSTROPHE).append(",")
						.append(Constants.EXECUTORID).append(":").append(Constants.APOSTROPHE).append(executorId).append(Constants.APOSTROPHE).append(",")
						.append(Constants.TRANSNAME).append(":").append(Constants.APOSTROPHE).append(transName).append(Constants.APOSTROPHE).append("}");
			}
			break;
		case "httpUpdateJob":
			batchInfo = batchInfo.append("{").append(Constants.TYPE).append(":").append(Constants.APOSTROPHE).append(type).append(Constants.APOSTROPHE).append(",")
				.append(Constants.JOBGROUP).append(":").append(Constants.APOSTROPHE).append(jobGroup).append(Constants.APOSTROPHE).append(",")
				.append(Constants.EXECUTORID).append(":").append(Constants.APOSTROPHE).append(executorId).append(Constants.APOSTROPHE).append(",")
				.append(Constants.TRANSNAME).append(":").append(Constants.APOSTROPHE).append(transName).append(Constants.APOSTROPHE).append(",")
				.append(Constants.DESCRIPTION).append(":").append(Constants.APOSTROPHE).append(description).append(Constants.APOSTROPHE).append(",")
				.append(Constants.CRONEXPRESSION).append(":").append(Constants.APOSTROPHE).append(cronExpression).append(Constants.APOSTROPHE).append(",")
				.append(Constants.MISFIREINSTR).append(":").append(misfireInstr).append("}");
			break;
		case "httpDeleteJob":
			batchInfo = batchInfo.append("{").append(Constants.TYPE).append(":").append(Constants.APOSTROPHE).append(type).append(Constants.APOSTROPHE).append(",")
				.append(Constants.JOBGROUP).append(":").append(Constants.APOSTROPHE).append(jobGroup).append(Constants.APOSTROPHE).append(",")
				.append(Constants.EXECUTORID).append(":").append(Constants.APOSTROPHE).append(executorId).append(Constants.APOSTROPHE).append(",")
				.append(Constants.TRANSNAME).append(":").append(Constants.APOSTROPHE).append(transName).append(Constants.APOSTROPHE).append("}");
			break;
		}
		return batchInfo;
		
	}
	
	@Override
	public Map<String, Object> updateNoteBatchNo(Map<String, Object> requestParam) throws Exception {
		if (CommonUtils.isNullOrEmpty(requestParam)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"更新文件序号参数错误"});
        }
        List<String> ids = (List<String>) requestParam.get(Dict.IDS);
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        if (ids.size() != 2) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"更新文件序号参数错误"});
        }

        List<String> tempList = new ArrayList<>();
        List<BatchTaskInfo> BatchList = new ArrayList<>();
        for (String id : ids) {
        	BatchTaskInfo batchTaskInfo = batchTaskDao.queryBatchTaskInfoByAppId(id,application_id);
        	BatchList.add(batchTaskInfo);
            String batch_no = batchTaskInfo.getBatch_no();
            tempList.add(batch_no);
        }
        Map hashMap = new HashMap();
        BatchList.get(0).setBatch_no(tempList.get(1));
        hashMap.put(BatchList.get(0).getId(), batchTaskDao.updateNoteBatchNo(BatchList.get(0).getId(),BatchList.get(0).getBatch_no()));
        BatchList.get(1).setBatch_no(tempList.get(0));
        hashMap.put(BatchList.get(1).getId(), batchTaskDao.updateNoteBatchNo(BatchList.get(1).getId(),BatchList.get(1).getBatch_no()));
		return hashMap;
	}

	@Override
	public List<String> queryBatchAppIdByNoteId(Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		String note_id = (String) requestParam.get(Dict.NOTE_ID);
		List<Map<String, Object>> batchApplicationIds = new ArrayList<>();
		List<String> applicationIds = new ArrayList<>();
		List<BatchTaskInfo> batchTaskInfos = batchTaskDao.queryBatchTaskInfoByReleaseNodeName(release_node_name);
		for (BatchTaskInfo batchTaskInfo : batchTaskInfos) {
			Map<String, Object> batchApplicationId = new HashMap<>();
			batchApplicationId.put(Dict.APPLICATION_ID, batchTaskInfo.getApplication_id());
			batchApplicationId.put(Dict.NOTE_ID, batchTaskInfo.getNote_id());
			batchApplicationIds.add(batchApplicationId);
		}
		for (Map<String, Object> batchApplicationId : batchApplicationIds) {
			if (!note_id.equals(batchApplicationId.get(Dict.NOTE_ID))) {
				String applicationId = (String) batchApplicationId.get(Dict.APPLICATION_ID);
				applicationIds.add(applicationId);
			}
		}
		applicationIds = ((List<String>) applicationIds.stream().distinct().collect(Collectors.toList()));
		return applicationIds;
	}
}
