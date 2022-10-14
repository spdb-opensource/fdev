import request from '@/utils/request';
import service from './serviceMap';

//查需求可新建的任务类型
export async function queryAddTaskType(params) {
  return request(service.ftask.queryAddTaskType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查应用的所有分支
export async function queryProjectBranchList(params) {
  return request(service.ftask.queryProjectBranchList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增合并审批
export async function addApprove(params) {
  return request(service.ftask.addApprove, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//新增部署审批
export async function appDeploy(params) {
  return request(service.ftask.appDeploy, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//判断应用所属组
export async function queryAppAscriptionGroup(params) {
  return request(service.fapp.queryAppAscriptionGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//判断任务应用的部署状态
export async function queryAppDeployByTaskId(params) {
  return request(service.ftask.queryAppDeployByTaskId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查合并审批列表
export async function releaseApproveList(params) {
  return request(service.ftask.releaseApproveList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//合并审批列表导出
export async function exportApproveList(params) {
  return request(service.ftask.exportApproveList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
//审批通过
export async function passApprove(params) {
  return request(service.ftask.passApprove, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//审批拒绝
export async function refuseApprove(params) {
  return request(service.ftask.refuseApprove, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//判断任务是否挂载了实施单元
export async function checkMountUnit(params) {
  return request(service.ftask.checkMountUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateJiraIssues(params) {
  return request(service.ftask.updateJiraIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function putJiraIssues(params) {
  return request(service.ftask.putJiraIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function saveTaskAndJiraIssues(params) {
  return request(service.ftask.saveTaskAndJiraIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTasks(params) {
  return request(service.torder.queryTasks, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTestTask(params) {
  return request(service.ftask.queryTestTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryRqrmntFileUri(params) {
  return request(service.frelease.queryRqrmntFileUri, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryTaskRqrmntAlert(params) {
  return request(service.frelease.queryTaskRqrmntAlert, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function nextStage(params) {
  return request(service.ftask.nextStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function uploadFilesRid(params) {
  return request(service.ftask.uploadFilesRid, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function addNoCodeRelator(params) {
  return request(service.ftask.addNoCodeRelator, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function filesUpload(params) {
  return request(service.fdocmanage.filesUpload, {
    method: 'POST',
    data: params
  });
}
export async function delNoCodeRelator(params) {
  return request(service.ftask.delNoCodeRelator, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deleteFileRid(params) {
  return request(service.ftask.deleteFileRid, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function noCodeRelator(params) {
  return request(service.ftask.noCodeRelator, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function nocodeTask(params) {
  return request(service.ftask.addNocodeTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateNocodeInfo(params) {
  return request(service.ftask.updateNocodeInformation, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function query(params) {
  return request(service.ftask.taskQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function add(params) {
  return request(service.ftask.taskAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addApp(params) {
  return request(service.ftask.taskNewApp, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addBranch(params) {
  return request(service.ftask.newFeature, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateTaskStatus(params) {
  return request(service.ftask.updateTaskStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryMainTask(params) {
  return request(service.ftask.queryMainTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteJob(params) {
  return request(service.ftask.deleteTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDeleteJob(params) {
  return request(service.ftask.queryDeleteTaskDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryMainJobByTaskId(params) {
  return request(service.ftask.queryBySubTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryMailTaskByUnitNo(params) {
  return request(service.ftask.queryTaskCByUnitNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

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
      confirmFileDate: params.confirmFileDate,
      id: params.id,
      desc: params.desc,
      group: params.group,
      stage: params.stage.value,
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
      proWantWindow: params.proWantWindow,
      plan_start_time: params.plan_start_time,
      plan_inner_test_time: params.plan_inner_test_time,
      plan_uat_test_start_time: params.plan_uat_test_start_time,
      plan_rel_test_time: params.plan_rel_test_time,
      plan_fire_time: params.plan_fire_time
    }
  });
}
// 修改任务时间
export async function updateJobDate(params) {
  return request(service.ftask.taskUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 改变任务阶段
export async function updateJobStage(params) {
  return request(service.ftask.taskUpdate, {
    method: 'POST',
    data: {
      id: params.id,
      stage: params.stage
    }
  });
}
// 更新uat承接方
export async function updateUatReceivingParty(params) {
  return request('/ftask/api/task/update', {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询小组，阶段
export async function queryWithOption(params) {
  return request(service.ftask.queryByTerms, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询实施单元编号，任务名称，所属应用，模糊匹配
export async function fuzzyQuery(params) {
  return request(service.ftask.queryByVague, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function profile(params) {
  return request(service.ftask.queryTaskDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDocDetail(params) {
  return request(service.ftask.queryDocDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryTestDetail(params) {
  return request(service.ftask.queryTestDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryUATTestDetail(params) {
  return request(service.ftask.queryUATTestDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function putSitTest(params) {
  return request(service.ftask.putSitTest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function noticeTest(params) {
  return request(service.ftask.interactTest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function putUatTest(params) {
  return request(service.ftask.putUatTest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function checkSccOrCaas(params) {
  return request(service.ftask.checkSccOrCaas, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryEnv(params) {
  return request(service.ftask.queryEnvDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取当前人员参与的任务列表
export async function queryUserTask(params) {
  return request(service.ftask.queryUserTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询外部配置参数（优先生效）
export async function queryExtraConfigParam(params) {
  return request(service.fenvconfig.outSideTemplateQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//新增外部配置参数（优先生效）
export async function addExtraConfigParam(params) {
  return request(service.fenvconfig.outsideTemplateSave, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function saveConfigTemplate(params) {
  return request(service.fenvconfig.saveConfigTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//更新外部配置参数（优先生效）
export async function updateExtraConfigParam(params) {
  return request(service.fenvconfig.outSideTemplateUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryRqrmnts(params) {
  return request(service.fdemand.queryForSelect, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 通过任务列表查询缺陷
export async function queryFtaskMantis(params) {
  return request(service.ftask.queryFtaskMantis, {
    method: 'POST',
    data: {
      taskList: [params.id]
    }
  });
}
export async function queryJiraIssues(params) {
  return request(service.ftask.queryJiraIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function abandonTask(params) {
  return request(service.ftask.abandonTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 保存环境配置数据
export async function saveDevConfigProperties(params) {
  return request(service.fenvconfig.saveDevConfigProperties, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deleteFile(params) {
  return request(service.ftask.deleteFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function createFirstReview(params) {
  return request(service.ftask.createFirstReview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function saveReviewRecord(params) {
  return request(service.ftask.saveReviewRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function addReviewIdea(params) {
  return request(service.ftask.addReviewIdea, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryReviewRecordStatus(params) {
  return request(service.ftask.queryReviewRecordStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 新建文件夹
export async function createFolder(params) {
  return request(service.fdocmanage.createFolder, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 设计还原审核状态更新
export async function updateState(params) {
  return request(service.ftask.updateState, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// wps 获取文件列表
export async function queryFileList(params) {
  return request(service.fdocmanage.queryFileList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

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

export async function getJobUser(params) {
  return request(service.fuser.getJobUser, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// wps 文件删除
export async function deleteFileById(params) {
  return request(service.fdocmanage.deleteFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryReviewRecordHistory(params) {
  return request(service.ftask.queryReviewRecordHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 获取下载地址
export async function queryPreviewLink(params) {
  return request(service.fdocmanage.queryPreviewLink, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function taskNameJudge(params) {
  return request(service.ftask.taskNameJudge, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryByTaskIdNode(params) {
  return request(service.ftask.queryByTaskIdNode, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function testReportCreate(params) {
  return request(service.ftask.testReportCreate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTestMergeInfo(params) {
  return request(service.ftask.queryTestMergeInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function createTestRunMerge(params) {
  return request(service.ftask.createTestRunMerge, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryCommitTips(params) {
  return request(service.ftask.queryCommitTips, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getCodeQuality(params) {
  return request(service.ftask.getCodeQuality, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getScanProcess(params) {
  return request(service.ftask.getScanProcess, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// android和iOS出测试包
export async function iOSOrAndroidAppPackage(params) {
  return request(service.ftask.iOSOrAndroidAppPackage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function downloadSonarLog(params) {
  return request(service.ftask.downloadSonarLog, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 导出文件
export async function exportExcelData(params) {
  return request(service.fdocmanage.exportExcelData, {
    method: 'GET',
    responseType: 'arraybuffer',
    params: {
      ...params
    }
  });
}
export async function bafflePoint(params) {
  return request(service.ftask.bafflePoint, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function downloadForWps(params) {
  return request(service.fdocmanage.downloadForWps, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 删除文件
export async function deleteFileNew(params) {
  return request(service.fdocmanage.deleteFileNew, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function configFilePreview(params) {
  return request(service.fenvconfig.configFilePreview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateTaskDoc(params) {
  return request(service.ftask.updateTaskDoc, {
    method: 'POST',
    data: params
  });
}
export async function deleteTaskDoc(params) {
  return request(service.ftask.deleteTaskDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function confirmBtn(params) {
  return request(service.ftask.confirmBtn, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function testKeyNote(params) {
  return request(service.ftask.testKeyNote, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询需求下的文档是否存在
export async function queryRqrDocInfo(params) {
  return request(service.ftask.queryRqrDocInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询需求详情下任务列表
export async function queryTaskByDemandId(params) {
  return request(service.ftask.queryTaskByDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryJiraStoryByKey(params) {
  return request(service.ftask.queryJiraStoryByKey, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询原生参数文件
export async function queryParamFile(params) {
  return request(service.ftask.queryParamFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
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
export async function taskCardDisplay(params) {
  return request(service.ftask.taskCardDisplay, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 前后端组件列表
export async function queryAllComponents(params) {
  return request(service.fcomponent.queryAllComponents, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 下载安全测试清单模板文件
export async function downloadTemplateFile(params) {
  return request(service.ftask.downloadTemplateFile, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 前后端骨架列表
export async function queryAllArchetypeTypes(params) {
  return request(service.fcomponent.queryAllArchetypeTypes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 提交安全测试
export async function putSecurityTest(params) {
  return request(service.ftask.putSecurityTest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 校验组件版本号
export async function checkComponentV(params) {
  return request(service.fcomponent.judgeTargetVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 校验骨架版本号
export async function checkArchetypeV(params) {
  return request(service.fcomponent.archetyVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 上传内部安全测试相关文档
export async function uploadSecurityTestDoc(params) {
  return request(service.ftask.uploadSecurityTestDoc, {
    method: 'POST',
    data: params
  });
}

//查询所有用户信息（包括角色）
export async function getAllUserAndRole(params) {
  return request(service.fuser.getAllUserAndRole, {
    method: 'POST',
    data: params
  });
}
//查询审批部署信息
export async function queryAppDeploy(params) {
  return request(service.ftask.queryAppDeploy, {
    method: 'POST',
    data: params
  });
}
//部署应用
export async function deployApps(params) {
  return request(service.ftask.deployApps, {
    method: 'POST',
    data: params
  });
}
//查询应用下其他未审批任务
export async function queryDeployTask(params) {
  return request(service.ftask.queryDeployTask, {
    method: 'POST',
    data: params
  });
}
//sit审批白名单
export async function queryWhiteList(params) {
  return request(service.ftask.queryWhiteList, {
    method: 'POST',
    data: params
  });
}
//导出
export async function exportDeployTask(params) {
  return request(service.ftask.exportDeployTask, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: params
  });
}
//跳过功能测试
export async function skipInnerTest(params) {
  return request(service.ftask.skipInnerTest, {
    method: 'POST',
    data: params
  });
}
