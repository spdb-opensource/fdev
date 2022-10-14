import request from '@/utils/request';
import service from './serviceMap';

// 新建批量任务
export async function createBatchTask(params) {
  return request(service.frelease.createBatchTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新批量任务
export async function updateBatchTask(params) {
  return request(service.frelease.updateBatchTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除批量任务
export async function deteleBatchTask(params) {
  return request(service.frelease.deteleBatchTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询批量任务
export async function queryBatchTask(params) {
  return request(service.frelease.queryBatchTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 添加批量任务
export async function addBatchTask(params) {
  return request(service.frelease.addBatchTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取批量任务列表
export async function queryBatchTaskList(params) {
  return request(service.frelease.queryBatchTaskList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新批量任务序号
export async function updateNoteBatchNo(params) {
  return request(service.frelease.updateNoteBatchNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新发布说明配置文件
export async function updateNoteConfiguration(params) {
  return request(service.frelease.updateNoteConfiguration, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除发布说明配置文件
export async function deleteNoteConfiguration(params) {
  return request(service.frelease.deleteNoteConfiguration, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除发布说明
export async function deleteNote(params) {
  return request(service.frelease.deleteNote, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 添加发布说明配置文件
export async function addNoteConfiguration(params) {
  return request(service.frelease.addNoteConfiguration, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取发布说明文件列表
export async function queryNoteConfiguration(params) {
  return request(service.frelease.queryNoteConfiguration, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function exportSpecialRqrmntInfoList(params) {
  return request(service.frelease.exportSpecialRqrmntInfoList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function queryReleaseNodes(params) {
  return request(service.frelease.queryReleaseNodes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询发布说明详情
export async function queryNoteDetail(params) {
  return request(service.frelease.queryNoteDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 添加发布说明应用
export async function addNoteService(params) {
  return request(service.frelease.addNoteService, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询发布说明应用
export async function queryNoteService(params) {
  return request(service.frelease.queryNoteService, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除发布说明应用
export async function deleteNoteService(params) {
  return request(service.frelease.deleteNoteService, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新发布说明应用
export async function updateNoteService(params) {
  return request(service.frelease.updateNoteService, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 锁定发布说明
export async function lockNote(params) {
  return request(service.frelease.lockNote, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询小组标识
export async function queryAllGroupAbbr(params) {
  return request(service.frelease.queryAllGroupAbbr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取发布说明列表
export async function queryReleaseNote(params) {
  return request(service.frelease.queryReleaseNote, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 创建发布说明
export async function createNote(params) {
  return request(service.frelease.createNote, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新发布说明数据库序号
export async function updateNoteSeqNo(params) {
  return request(service.frelease.updateNoteSeqNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询发布说明数据库
export async function queryNoteSql(params) {
  return request(service.frelease.queryNoteSql, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 根据发布说明查应用ID
export async function queryBatchAppIdByNoteId(params) {
  return request(service.frelease.queryBatchAppIdByNoteId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 生成发布说明
export async function generateReleaseNotes(params) {
  return request(service.frelease.generateReleaseNotes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除发布说明数据库
export async function deleteNoteSql(params) {
  return request(service.frelease.deleteNoteSql, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 添加发布说明数据库
export async function addNoteSql(params) {
  return request(service.frelease.addNoteSql, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function getNodeName(params) {
  return request(service.frelease.getReleaseNodeName, {
    method: 'POST',
    data: {
      release_date: params
    }
  });
}

export async function queryEnv(params) {
  return request(service.fenvconfig.queryByLabels, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 通过标签模糊查询环境
export async function queryByLabelsFuzzy(params) {
  return request(service.fenvconfig.queryByLabelsFuzzy, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function update(params) {
  return request(service.frelease.releaseNodeUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 手动添加发布说明
export async function createManualNote(params) {
  return request(service.frelease.createManualNote, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 手动添加发布说明文件内容
export async function addManualNoteInfo(params) {
  return request(service.frelease.addManualNoteInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询手动发布说明文件内容
export async function queryManualNoteInfo(params) {
  return request(service.frelease.queryManualNoteInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新手动发布说明文件内容
export async function updateManualNoteInfo(params) {
  return request(service.frelease.updateManualNoteInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function create(params) {
  return request(service.frelease.releaseNodeCreate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryTasks(params) {
  return request(service.frelease.queryTasks, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function auditAdd(params) {
  return request(service.frelease.taskAuditAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryApply(params) {
  return request(service.frelease.queryApplications, {
    method: 'POST',
    data: {
      release_node_name: params
    }
  });
}

export async function queryComponent(params) {
  return request(service.frelease.queryComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryTargetVersion(params) {
  return request(service.frelease.queryTargetVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryReleaseVersionComponent(params) {
  return request(service.fcomponent.queryReleaseVersionComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryReleaseVersionArchetype(params) {
  return request(service.fcomponent.queryReleaseVersionArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryLatestVersionComponent(params) {
  return request(service.fcomponent.queryLatestVersionComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryLatestVersionArchetype(params) {
  return request(service.fcomponent.queryLatestVersionArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function relDevopsComponent(params) {
  return request(service.fcomponent.relDevopsComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function relDevopsArchetype(params) {
  return request(service.fcomponent.relDevopsArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function relDevopsBaseImage(params) {
  return request(service.fcomponent.relDevopsBaseImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function judgeTargetVersionComponent(params) {
  return request(service.fcomponent.judgeTargetVersionComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function judgeTargetVersionarchetype(params) {
  return request(service.fcomponent.judgeTargetVersionarchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function relDevops(params) {
  return request(service.frelease.relDevops, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function packageFromTag(params) {
  return request(service.frelease.packageFromTag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryReleaseNodeDetail(params) {
  return request(service.frelease.releaseNodeQueryDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 根据任务id查询任务关联项审核最终结果
export async function queryTaskReview(params) {
  return request(service.ftask.queryTaskReview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 根据任务关联项id修改关联项审核结果
export async function updateTaskReview(params) {
  return request(service.ftask.updateTaskReview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询任务关联项
export async function queryTasksReviews(params) {
  return request(service.frelease.queryTasksReviews, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function sendEmailForTaskManagers(params) {
  return request(service.frelease.sendEmailForTaskManagers, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询变更模板列表
export async function templateQuery(params) {
  return request(service.frelease.templateQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询变更模板详情
export async function queryTemplateDetail(params) {
  return request(service.frelease.templateQueryDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更记录查询
export async function queryRecord(params) {
  return request(service.frelease.releaseQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 新建变更--变更模板查询
export async function queryVersion(params) {
  return request(service.frelease.getReleaseVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 新建变更
export async function createChanges(params) {
  return request(service.frelease.releaseCreate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更详情查询
export async function queryChangesDetail(params) {
  return request(service.frelease.queryDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// tag打包--选择分支
export async function queryApplicationTags(params) {
  return request(service.frelease.queryApplicationTags, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function trace(params) {
  return request(service.frelease.trace, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询变更应用列表
export async function queryApplications(params) {
  return request(service.frelease.releaseQueryApplications, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询变更文件列表
export async function queryAssetsList(params) {
  return request(service.frelease.queryAssets, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 新建变更应用
export async function addChangeApplication(params) {
  return request(service.frelease.addApplication, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 新建变更应用
export async function setTemplate(params) {
  return request(service.frelease.setTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更审核
export async function audit(params) {
  return request(service.frelease.releaseAudit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 编辑模板
export async function updateTemplate(params) {
  return request(service.frelease.templateUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更文件删除
export async function deleteAsset(params) {
  return request(service.frelease.deleteAsset, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更文件数组删除
export async function deleteAssetArr(params) {
  return request(service.frelease.deleteAssets, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更模板删除
export async function deleteTemplate(params) {
  return request(service.frelease.templateDelete, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更应用列表删除
export async function deleteApplication(params) {
  return request(service.frelease.deleteApplication, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询变更目录
export async function catalogoptions(params) {
  return request(service.frelease.queryOptionalCatalog, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 变更模板弹窗回显
export async function querySysRlsInfo(params) {
  return request(service.frelease.querySysRlsInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// gitlab分支查询
export async function queryResourceBranches(params) {
  return request(service.frelease.queryResourceBranches, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// gitlab文件查询
export async function queryResourceFiles(params) {
  return request(service.frelease.queryResourceFiles, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 添加gitlab文件
export async function addGitlabAsset(params) {
  return request(service.frelease.addGitlabAsset, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 获取配置文件
export async function queryProfile(params) {
  return request(service.frelease.queryProfile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 移动有序文件
export async function moveOrder(params) {
  return request(service.frelease.updateAssetSeqNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询当前窗口此应用所有可用镜像标签
export async function queryImageTags(params) {
  return request(service.frelease.queryImageTags, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 选择镜像标签
export async function setImageTag(params) {
  return request(service.frelease.setImageTag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 保存环境
export async function setTestEnvs(params) {
  return request(service.frelease.setTestEnvs, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryAppTagPiplines(params) {
  return request(service.frelease.queryAppTagPiplines, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTestRunPiplines(params) {
  return request(service.frelease.queryTestRunPiplines, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 任务解挂 */
export async function deleteTask(params) {
  return request(service.frelease.deleteTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function changeReleaseNode(params) {
  return request(service.frelease.changeReleaseNode, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 确认已投产 */
export async function updateTaskArchived(params) {
  return request(service.frelease.updateTaskArchived, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryProdInfo(params) {
  return request(service.frelease.queryProdInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function changesUpdate(params) {
  return request(service.frelease.releaseUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryAppWithOutSum(params) {
  return request(service.frelease.queryApplications, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 删除变更 */
export async function deleteRelease(params) {
  return request(service.frelease.releaseDelete, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 查询小组标识 */
export async function queryGroupAbbr(params) {
  return request(service.frelease.queryGroupAbbr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 修改小组标识 */
export async function updateGroupAbbr(params) {
  return request(service.frelease.updateGroupAbbr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 查询变更计划 */
export async function queryPlan(params) {
  return request(service.frelease.queryPlan, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 生成模板实例 */
export async function generateProdExcel(params) {
  return request(service.frelease.generateProdExcel, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 修改系统缩写标识 */
export async function updateSysRlsInfo(params) {
  return request(service.frelease.updateSysRlsInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryResourceProjects(params) {
  return request(service.frelease.queryResourceProjects, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 查询弹性接口 */
export async function queryAppScale(params) {
  return request(service.frelease.queryAppScale, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 新增弹性接口 */
export async function addAppScale(params) {
  return request(service.frelease.addAppScale, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 修改弹性接口 */
export async function updateAppScale(params) {
  return request(service.frelease.updateAppScale, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 删除弹性接口 */
export async function deleteAppScale(params) {
  return request(service.frelease.deleteAppScale, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 根据变更id查询当前变更所有介质文件 */
export async function queryAllProdAssets(params) {
  return request(service.frelease.queryAllProdAssets, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 根据变更id查询当前变更所有非自动化介质文件 */
export async function queryDeAutoAllProdAssets(params) {
  return request(service.frelease.queryDeAutoAllProdAssets, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 导出发布说明*/
export async function exportProdDirection(params) {
  return request(service.frelease.exportProdDirection, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 投产应用-提交发布-查询是否有未进入uat阶段的任务*/
export async function tasksInSitStage(params) {
  return request(service.frelease.tasksInSitStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 查询小组标识*/
export async function queryGroupSysAbbr(params) {
  return request(service.frelease.queryGroupSysAbbr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 修改小组标识*/
export async function updateGroupSysAbbr(params) {
  return request(service.frelease.updateGroupSysAbbr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 根据taskid查投产*/
export async function queryDetailByTaskId(params) {
  return request(service.frelease.queryDetailByTaskId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 以下是参数维护 */
export async function queryModuleType(params) {
  return request(service.frelease.queryModuleType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 试运行发布查询*/
export async function createTestrunBranch(params) {
  return request(service.frelease.createTestrunBranch, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deleteModuleType(params) {
  return request(service.frelease.deleteModuleType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 试运行发布-确定*/
export async function mergeTaskBranch(params) {
  return request(service.frelease.mergeTaskBranch, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function addModuleType(params) {
  return request(service.frelease.addModuleType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 根据应用id查任务 */
export async function queryNotinlineTasksByAppId(params) {
  return request(service.ftask.queryNotinlineTasksByAppId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateModuleType(params) {
  return request(service.frelease.updateModuleType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 以下是脚本参数 */
export async function queryScript(params) {
  return request(service.frelease.queryScript, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deleteScript(params) {
  return request(service.frelease.deleteScript, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function addScript(params) {
  return request(service.frelease.addScript, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateScript(params) {
  return request(service.frelease.updateScript, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 以下是变更环境维护 */
export async function queryAutomationEnv(params) {
  return request(service.frelease.queryAutomationEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deleteAutomationEnv(params) {
  return request(service.frelease.deleteAutomationEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function addAutomationEnv(params) {
  return request(service.frelease.addAutomationEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateAutomationEnv(params) {
  return request(service.frelease.updateAutomationEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryDBAssets(params) {
  return request(service.frelease.queryDBAssets, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 查询已试运行任务 */
export async function queryTaskByTestRunId(params) {
  return request(service.frelease.queryTaskByTestRunId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function findByProdId(params) {
  return request(service.frelease.findByProdId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function changeConfirmConf(params) {
  return request(service.frelease.changeConfirmConf, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 关联项查询 */
export async function queryReviewRecord(params) {
  return request(service.ftask.queryReviewRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function changeReleaseConf(params) {
  return request(service.frelease.changeReleaseConf, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 关联项详情更新 */
export async function updateReviewRecord(params) {
  return request(service.ftask.updateReviewRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 根据任务id查询审核基本信息
export async function queryReviewBasicMsg(params) {
  return request(service.ftask.queryReviewBasicMsg, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 新增/修改审核记录
export async function addReview(params) {
  return request(service.ftask.addReview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryByReleaseNodeName(params) {
  return request(service.frelease.queryByReleaseNodeName, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteFile(params) {
  return request(service.frelease.deleteFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function taskChangeNotise(params) {
  return request(service.frelease.taskChangeNotise, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function downloadFiles(params) {
  return request(service.frelease.downloadFiles, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function addSystemTestFile(params) {
  return request(service.frelease.addSystemTestFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addSourceReview(params) {
  return request(service.frelease.addSourceReview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function querySourceReviewDetail(params) {
  return request(service.frelease.querySourceReviewDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function pullGrayBranch(params) {
  return request(service.frelease.pullGrayBranch, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 创建变更模板
export async function createATemplate(params) {
  return request(service.frelease.templateCreate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function querySystem(params) {
  return request(service.frelease.querySystem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryExcelTemplate(params) {
  return request(service.frelease.queryExcelTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function editFakeInfo(params) {
  return request(service.frelease.editFakeInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 用于iOS和Android应用的打包
export async function iOSOrAndroidAppPublish(params) {
  return request(service.frelease.iOSOrAndroidAppPublish, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 创建投产大窗口
export async function createBigReleaseNode(params) {
  return request(service.frelease.createBigReleaseNode, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 编辑投产大窗口
export async function updateBigReleasenode(params) {
  return request(service.frelease.updateBigReleasenode, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询投产大窗口
export async function queryBigReleaseNodes(params) {
  return request(service.frelease.queryBigReleaseNodes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询投产大窗口详情
export async function queryBigReleaseNodeDetail(params) {
  return request(service.frelease.queryBigReleaseNodeDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询会同联系人
export async function queryContactInfo(params) {
  return request(service.frelease.queryContactInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询投产大窗口-需求列表
export async function queryRqrmntInfoList(params) {
  return request(service.frelease.queryRqrmntInfoList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询投产大窗口-查询投产周期时间
export async function queryByReleaseDate(params) {
  return request(service.frelease.queryByReleaseDate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询投产大窗口-修改投产周期时间
export async function updateBigReleaseDate(params) {
  return request(service.frelease.updateBigReleaseDate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询投产意向任务
export async function queryProWantTasks(params) {
  return request(service.frelease.queryProWantTasks, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryBatchInfoByTaskId(params) {
  return request(service.frelease.queryBatchInfoByTaskId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 修改批次、设置批次 */
export async function addBatch(params) {
  return request(service.frelease.addBatch, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 查询提测重点 */
export async function queryRqrmntInfoListByType(params) {
  return request(service.frelease.queryRqrmntInfoListByType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 投产大窗口--导出需求列表 */
export async function exportRqrmntInfoList(params) {
  return request(service.frelease.exportRqrmntInfoList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
/* 投产大窗口--导出提测重点、安全测试 */
export async function exportRqrmntInfoListByType(params) {
  return request(service.frelease.exportRqrmntInfoListByType, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
/* 投产大窗口--整包提测 */
export async function updateRqrmntInfo(params) {
  return request(service.frelease.updateRqrmntInfo, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function preview(params) {
  return request(service.fdocmanage.preview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryBatchInfoByAppId(params) {
  return request(service.frelease.queryBatchInfoByAppId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryRqrmntInfoTasks(params) {
  return request(service.frelease.queryRqrmntInfoTasks, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//根据投产窗口名称查询系统
export async function queryReleaseSystem(params) {
  return request(service.frelease.queryReleaseSystem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryDeAutoAssets(params) {
  return request(service.frelease.queryDeAutoAssets, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryConfigApplication(params) {
  return request(service.frelease.queryConfigApplication, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deAutoUpload(params) {
  return request(service.frelease.deAutoUpload, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addConfigApplication(params) {
  return request(service.frelease.addConfigApplication, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryPackageTags(params) {
  return request(service.frelease.queryPackageTags, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function checkConfigApplication(params) {
  return request(service.frelease.checkConfigApplication, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function setPackageTag(params) {
  return request(service.frelease.setPackageTag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function createConfig(params) {
  return request(service.frelease.createConfig, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteConfig(params) {
  return request(service.frelease.deleteConfig, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryConfigSum(params) {
  return request(service.frelease.queryConfigSum, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function confirmChanges(params) {
  return request(service.frelease.confirmChanges, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteReleaseNode(params) {
  return request(service.frelease.deleteReleaseNode, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//系统投产信息查询（根据变更id）
export async function querySystemDetailByProdId(params) {
  return request(service.frelease.querySystemDetailByProdId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 获取审核通过的数据库文件路径
export async function queryDbPath(params) {
  return request(service.frelease.queryDbPath, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 数据库变更文件上传
export async function uploadAssets(params) {
  return request(service.frelease.uploadAssets, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询变更应用介质目录可选项
export async function queryProdDir(params) {
  return request(service.frelease.queryProdDir, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改灰度变更应用介质目录
export async function updateProdDir(params) {
  return request(service.frelease.updateProdDir, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改应用卡片的部署平台
export async function updateProdDeploy(params) {
  return request(service.frelease.updateProdDeploy, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 根据应用id查询部署平台
export async function queryDeployTypeByAppId(params) {
  return request(service.frelease.queryDeployTypeByAppId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询全量环境
export async function queryEnvList(params) {
  return request(service.fenvconfig.envQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 对象存储修改组
export async function updateAwsAssetGroupId(params) {
  return request(service.frelease.updateAwsAssetGroupId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询对象存储用户所属组列表
export async function queryAwsGroups(params) {
  return request(service.frelease.queryAwsConfigByGroupId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 生产红线扫描报告
export async function addRedLineScanReport(params) {
  return request(service.frelease.addRedLineScanReport, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 是否写入Order
export async function whetherWriteOrder(params) {
  return request(service.frelease.whetherWriteOrder, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询副本数
export async function queryReplicasnu(params) {
  return request(service.frelease.queryReplicasnu, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// esf新增
export async function addEsfRegistration(params) {
  return request(service.frelease.addEsfRegistration, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改副本数
export async function updateReplicasnu(params) {
  return request(service.frelease.updateReplicasnu, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// esf删除
export async function delEsf(params) {
  return request(service.frelease.delEsf, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// esf修改
export async function updateEsf(params) {
  return request(service.frelease.updateEsf, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询配置中心
export async function queryEsfConfiguration(params) {
  return request(service.frelease.queryEsfConfiguration, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// esfCommonConfig目录新增
export async function addEsfCommonConfig(params) {
  return request(service.frelease.addEsfCommonConfig, {
    method: 'POST',
    data: params
  });
}

// 编辑esfcommonconfig
export async function updateEsfcommonconfigAssets(params) {
  return request(service.frelease.updateEsfcommonconfigAssets, {
    method: 'POST',
    data: params
  });
}

// 查询应用是否投过产
export async function queryAppStatus(params) {
  return request(service.frelease.queryAppStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询应用是否填写对应部署平台的部署信息
export async function queryAppDeployPlatformInfo(params) {
  return request(service.fapp.findById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 添加esf时查询应用
export async function queryAppsByAddEsf(params) {
  return request(service.frelease.queryAppsByAddEsf, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询用户所属第三层级组及子组
export async function queryThreeLevelGroups(params) {
  return request(service.frelease.queryThreeLevelGroups, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
