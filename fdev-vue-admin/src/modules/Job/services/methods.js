import request from '@/utils/request.js';
import service from './api.js';
export const commonRequest = url => async (params = {}) =>
  await request(url, { methods: 'POST', data: { ...params } });

//判断任务是否挂载了实施单元
export const checkMountUnit = commonRequest(service.ftask.checkMountUnit);

export const updateJiraIssues = commonRequest(service.ftask.updateJiraIssues);
export const putJiraIssues = commonRequest(service.ftask.putJiraIssues);

export const saveTaskAndJiraIssues = commonRequest(
  service.ftask.saveTaskAndJiraIssues
);
export const queryTasks = commonRequest(service.torder.queryTasks);
export const queryTestTask = commonRequest(service.ftask.queryTestTask);

export const queryTaskRqrmntAlert = commonRequest(
  service.frelease.queryTaskRqrmntAlert
);

export const nextStage = commonRequest(service.ftask.nextStage);
export const uploadFilesRid = commonRequest(service.ftask.uploadFilesRid);
export const addNoCodeRelator = commonRequest(service.ftask.addNoCodeRelator);
export async function filesUpload(params) {
  return request(service.fdocmanage.filesUpload, {
    method: 'POST',
    data: params
  });
}
export const delNoCodeRelator = commonRequest(service.ftask.delNoCodeRelator);
export const deleteFileRid = commonRequest(service.ftask.deleteFileRid);
export const noCodeRelator = commonRequest(service.ftask.noCodeRelator);
export const nocodeTask = commonRequest(service.ftask.addNocodeTask);
export const updateNocodeInfo = commonRequest(
  service.ftask.updateNocodeInformation
);
export const query = commonRequest(service.ftask.taskQuery);

export const add = commonRequest(service.ftask.taskAdd);

export const addApp = commonRequest(service.ftask.taskNewApp);

export const addBranch = commonRequest(service.ftask.newFeature);

export const updateTaskStatus = commonRequest(service.ftask.updateTaskStatus);

export const queryMainTask = commonRequest(service.ftask.queryMainTask);

export const deleteJob = commonRequest(service.ftask.deleteTask);

export const queryDeleteJob = commonRequest(
  service.ftask.queryDeleteTaskDetail
);

export const queryMainJobByTaskId = commonRequest(service.ftask.queryBySubTask);

export const queryMailTaskByUnitNo = commonRequest(
  service.ftask.queryTaskCByUnitNo
);

export async function remove(params) {
  return request(service.ftask.taskDelete, {
    method: 'POST',
    data: [...params]
  });
}

// 修改任务
export async function update(params) {
  return request(service.ftask.taskUpdate, {
    method: 'POST',
    data: {
      id: params.id,
      desc: params.desc,
      master: params.partyB.map(ele => ele.id),
      spdb_master: params.partyA.map(ele => ele.id),
      tester: params.tester.map(ele => ele.id),
      developer: params.developer.map(ele => ele.id),
      fdev_implement_unit_no: params.fdev_implement_unit_no,
      start_time: params.start_time,
      start_inner_test_time: params.start_inner_test_time,
      fire_time: params.fire_time,
      start_uat_test_time: params.start_uat_test_time,
      start_rel_test_time: params.start_rel_test_time,
      tag: params.tag,
      doc: params.doc.map(ele => ele.id),
      review: {
        data_base_alter: params.review.db,
        commonProfile: params.review.commonProfile,
        other_system: params.review.other_system,
        specialCase: params.review.specialCase,
        securityTest: params.review.securityTest
      },
      system_remould: params.system_remould,
      impl_data: params.impl_data,
      difficulty_desc: params.difficulty_desc,
      modify_reason: params.modify_reason,
      proWantWindow: params.proWantWindow
    }
  });
}
// 修改任务时间
export const updateJobDate = commonRequest(service.ftask.taskUpdate);
// 改变任务阶段
export async function updateJobStage(params) {
  return request(service.ftask.taskUpdate, {
    method: 'POST',
    data: { id: params.id, stage: params.stage }
  });
}
// 更新uat承接方
export const updateUatReceivingParty = commonRequest('/ftask/api/task/update');
// 查询小组，阶段
export const queryWithOption = commonRequest(service.ftask.queryByTerms);
// 查询实施单元编号，任务名称，所属应用，模糊匹配
export const fuzzyQuery = commonRequest(service.ftask.queryByVague);

export const profile = commonRequest(service.ftask.queryTaskDetail);

export const queryDocDetail = commonRequest(service.ftask.queryDocDetail);

export const queryTestDetail = commonRequest(service.ftask.queryTestDetail);

export const queryUATTestDetail = commonRequest(
  service.ftask.queryUATTestDetail
);

export const putSitTest = commonRequest(service.ftask.putSitTest);

export const noticeTest = commonRequest(service.ftask.interactTest);

export const putUatTest = commonRequest(service.ftask.putUatTest);

export const queryEnv = commonRequest(service.ftask.queryEnvDetail);

// 获取当前人员参与的任务列表
export const queryUserTask = commonRequest(service.ftask.queryUserTask);
//查询外部配置参数（优先生效）
export const queryExtraConfigParam = commonRequest(
  service.fenvconfig.outSideTemplateQuery
);
//新增外部配置参数（优先生效）
export const addExtraConfigParam = commonRequest(
  service.fenvconfig.outsideTemplateSave
);

export const saveConfigTemplate = commonRequest(
  service.fenvconfig.saveConfigTemplate
);
//更新外部配置参数（优先生效）
export const updateExtraConfigParam = commonRequest(
  service.fenvconfig.outSideTemplateUpdate
);
export const queryRqrmnts = commonRequest(service.fdemand.queryForSelect);
// 通过任务列表查询缺陷
// 通过任务列表查询缺陷
export async function queryFtaskMantis(params) {
  return request(service.ftask.queryFtaskMantis, {
    method: 'POST',
    data: { taskList: [params.id] }
  });
}
export const queryJiraIssues = commonRequest(service.ftask.queryJiraIssues);
export const abandonTask = commonRequest(service.ftask.abandonTask);
// 保存环境配置数据
export const saveDevConfigProperties = commonRequest(
  service.fenvconfig.saveDevConfigProperties
);
export const deleteFile = commonRequest(service.ftask.deleteFile);
export const createFirstReview = commonRequest(service.ftask.createFirstReview);
export const saveReviewRecord = commonRequest(service.ftask.saveReviewRecord);
export const addReviewIdea = commonRequest(service.ftask.addReviewIdea);
export const queryReviewRecordStatus = commonRequest(
  service.ftask.queryReviewRecordStatus
);
// 新建文件夹
export const createFolder = commonRequest(service.fdocmanage.createFolder);
// 设计还原审核状态更新
export const updateState = commonRequest(service.ftask.updateState);
// wps 获取文件列表
export const queryFileList = commonRequest(service.fdocmanage.queryFileList);

// 新建生产问题总结
export async function addProIssue(params) {
  return request(service.tplan.addProIssue, {
    method: 'POST',
    data: {
      ...params,
      dev_responsible: params.dev_responsible
        ? params.dev_responsible.map(item => item.user_name_en)
        : [],
      audit_responsible: params.audit_responsible
        ? params.audit_responsible.map(item => item.user_name_en)
        : [],
      test_responsible: params.test_responsible
        ? params.test_responsible.map(item => item.user_name_en)
        : [],
      task_responsible: params.task_responsible
        ? params.task_responsible.map(item => item.user_name_en)
        : []
    }
  });
}

export const getJobUser = commonRequest(service.fuser.getJobUser);
// wps 文件删除
export const deleteFileById = commonRequest(service.fdocmanage.deleteFile);

export const queryReviewRecordHistory = commonRequest(
  service.ftask.queryReviewRecordHistory
);
// 获取下载地址
export const queryPreviewLink = commonRequest(
  service.fdocmanage.queryPreviewLink
);
export const taskNameJudge = commonRequest(service.ftask.taskNameJudge);
export const queryByTaskIdNode = commonRequest(service.ftask.queryByTaskIdNode);
export const testReportCreate = commonRequest(service.ftask.testReportCreate);
export const queryTestMergeInfo = commonRequest(
  service.ftask.queryTestMergeInfo
);
export const createTestRunMerge = commonRequest(
  service.ftask.createTestRunMerge
);
export const queryCommitTips = commonRequest(service.ftask.queryCommitTips);
export const getCodeQuality = commonRequest(service.ftask.getCodeQuality);
export const getScanProcess = commonRequest(service.ftask.getScanProcess);
// android和iOS出测试包
export const iOSOrAndroidAppPackage = commonRequest(
  service.ftask.iOSOrAndroidAppPackage
);
export async function downloadSonarLog(params) {
  return request(service.ftask.downloadSonarLog, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: { ...params }
  });
}
// 导出文件
export async function exportExcelData(params) {
  return request(service.fdocmanage.exportExcelData, {
    method: 'GET',
    responseType: 'arraybuffer',
    params: { ...params }
  });
}
export const bafflePoint = commonRequest(service.ftask.bafflePoint);
export const downloadForWps = commonRequest(service.fdocmanage.downloadForWps);
// 删除文件
export const deleteFileNew = commonRequest(service.fdocmanage.deleteFileNew);

export const configFilePreview = commonRequest(
  service.fenvconfig.configFilePreview
);
export async function updateTaskDoc(params) {
  return request(service.ftask.updateTaskDoc, { method: 'POST', data: params });
}
export const deleteTaskDoc = commonRequest(service.ftask.deleteTaskDoc);
export const confirmBtn = commonRequest(service.ftask.confirmBtn);
export const testKeyNote = commonRequest(service.ftask.testKeyNote);
// 查询需求下的文档是否存在
export const queryRqrDocInfo = commonRequest(service.ftask.queryRqrDocInfo);

// 查询需求详情下任务列表
export const queryTaskByDemandId = commonRequest(
  service.ftask.queryTaskByDemandId
);

export const queryJiraStoryByKey = commonRequest(
  service.ftask.queryJiraStoryByKey
);

export const queryRqrmntFileUri = commonRequest(
  service.frelease.queryRqrmntFileUri
);

// 按条价查询用户
export const queryAll = commonRequest(service.fuser.userQuery);
// 通过id查询所有前端项目
export const queryApps = commonRequest(service.fapp.queryApps);

export const queryGroup = commonRequest(service.fuser.groupQuery);
//任务新增查询实施单元
export const queryAvailableIpmpUnit = commonRequest(
  service.fdemand.queryAvailableIpmpUnit
);
export const queryPaginationByDemandId = commonRequest(
  service.fdemand.queryPaginationByDemandId
);
// 根据任务id集合查询任务列表
export const queryUserCoreData = commonRequest(service.fuser.queryUserCoreData);
// 查询小组信息，包括小组人数
export const queryGroupAll = commonRequest(service.fuser.queryGroup);
export const queryReleaseNodeByJob = commonRequest(
  service.frelease.queryDetailByTaskId
);
export const queryRole = commonRequest(service.fuser.roleQuery);

export async function upload(params) {
  return request(service.fdatabase.upload, {
    method: 'POST',
    data: params
  });
}
// 接口换成直接查需求模块
export async function queryRedmineById(params) {
  return request(service.frqrmnt.queryRedmineInfoByRedmineId, {
    method: 'POST',
    data: {
      ...params,
      redmine_id: params.id
    }
  });
}
// 扫描--判断用户是否为任务负责人、行内项目负责人、开发人员
export const isTaskManager = commonRequest(service.finterface.isTaskManager);
// 查询是否有未申请的接口
export const queryIsNoApplyInterface = commonRequest(
  service.finterface.queryIsNoApplyInterface
);
// 查询应用信息
export const appQuery = commonRequest(service.fapp.appQuery);
// 应用模块--pipelines
export const queryPipelines = commonRequest(
  service.fapp.queryPipelinesWithJobs
);
export const scanningFeatureBranch = commonRequest(
  service.fsonar.scanningFeatureBranch
);
export const queryReleaseNode = commonRequest(
  service.frelease.queryReleaseNodes
);
export const addJobReleaseNode = commonRequest(service.frelease.taskAdd);
export const taskChangeNotise = commonRequest(
  service.frelease.taskChangeNotise
);
/* 修改批次、设置批次 */
export const addBatch = commonRequest(service.frelease.addBatch);
export const queryBatchInfoByAppId = commonRequest(
  service.frelease.queryBatchInfoByAppId
);
/* 确认已投产 */
export const updateTaskArchived = commonRequest(
  service.frelease.updateTaskArchived
);

// 根据任务id查询任务关联项审核最终结果
export const queryTaskReview = commonRequest(service.ftask.queryTaskReview);

export const queryTaskSitMsg = commonRequest(service.torder.queryTaskSitMsg);
// 人员维度缺陷查询
export const updateFdevMantis = commonRequest(service.tmantis.updateFdevMantis);
export const updateAssignUser = commonRequest(service.tmantis.updateAssignUser);
// 人员维度缺陷查询
export const queryFuserMantis = commonRequest(service.tmantis.queryFuserMantis);
export const queryByFdevNoAndDemandId = commonRequest(
  service.fdemand.queryByFdevNoAndDemandId
);
// 查询延期任务
export async function queryPostponeTask(params) {
  return request(service.ftask.queryPostponeTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 电子看板
export const taskCardDisplay = commonRequest(service.ftask.taskCardDisplay);
