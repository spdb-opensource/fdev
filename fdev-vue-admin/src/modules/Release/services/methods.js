// 查询变更模板列表
import request from '@/utils/request';
import service from './api';

export const commonRequest = url => async (params = {}) => {
  const response = await request(url, {
    method: 'POST',
    data: {
      ...params
    }
  });

  return response;
};

export const templateQuery = commonRequest(service.frelease.templateQuery);

// 新建变更应用
export const setTemplate = commonRequest(service.frelease.setTemplate);

// 变更审核
export const audit = commonRequest(service.frelease.releaseAudit);

// 创建发布说明
export const createNote = commonRequest(service.frelease.createNote);

// 发布说明列表
export const queryReleaseNote = commonRequest(
  service.frelease.queryReleaseNote
);

// 新增发布说明应用
export const addNoteService = commonRequest(service.frelease.addNoteService);

// 查询发布说明应用
export const queryNoteService = commonRequest(
  service.frelease.queryNoteService
);

// 删除发布说明应用
export const deleteNoteService = commonRequest(
  service.frelease.deleteNoteService
);

// 编辑模板
export const updateTemplate = commonRequest(service.frelease.templateUpdate);

// 新建变更
export const createChanges = commonRequest(service.frelease.releaseCreate);

// 查询变更应用列表
export const queryApplications = commonRequest(
  service.frelease.releaseQueryApplications
);

// 变更记录查询
export const queryRecord = commonRequest(service.frelease.releaseQuery);

// 查询变更模板详情
export const queryTemplateDetail = commonRequest(
  service.frelease.templateQueryDetail
);

// 查询变更文件列表
export const queryAssetsList = commonRequest(service.frelease.queryAssets);

// 变更文件删除
export const deleteAsset = commonRequest(service.frelease.deleteAsset);

// 变更模板删除
export const deleteTemplate = commonRequest(service.frelease.templateDelete);

// 变更应用列表删除
export const deleteApplication = commonRequest(
  service.frelease.deleteApplication
);

// 查询变更目录
export const catalogoptions = commonRequest(
  service.frelease.queryOptionalCatalog
);

// 变更模板弹窗回显
export const querySysRlsInfo = commonRequest(service.frelease.querySysRlsInfo);

// gitlab分支查询
export const queryResourceBranches = commonRequest(
  service.frelease.queryResourceBranches
);

// gitlab文件查询
export const queryResourceFiles = commonRequest(
  service.frelease.queryResourceFiles
);

// 添加gitlab文件
export const addGitlabAsset = commonRequest(service.frelease.addGitlabAsset);

// 移动有序文件
export const moveOrder = commonRequest(service.frelease.updateAssetSeqNo);

// 查询当前窗口此应用所有可用镜像标签
export const queryImageTags = commonRequest(service.frelease.queryImageTags);

// 选择镜像标签
export const setImageTag = commonRequest(service.frelease.setImageTag);

// 变更文件数组删除
export const deleteAssetArr = commonRequest(service.frelease.deleteAssets);

export const trace = commonRequest(service.frelease.trace);

export const queryReleaseNodes = commonRequest(
  service.frelease.queryReleaseNodes
);

export const queryReleaseNodeDetail = commonRequest(
  service.frelease.releaseNodeQueryDetail
);

export const queryEnv = commonRequest(service.fenvconfig.queryByLabels);

export const getNodeName = commonRequest(service.frelease.getReleaseNodeName);

export const create = commonRequest(service.frelease.releaseNodeCreate);

export const update = commonRequest(service.frelease.releaseNodeUpdate);

// 保存环境
export const setTestEnvs = commonRequest(service.frelease.setTestEnvs);

export const changeReleaseNode = commonRequest(
  service.frelease.changeReleaseNode
);

/* 确认已投产 */
export const updateTaskArchived = commonRequest(
  service.frelease.updateTaskArchived
);

/* 任务解挂 */
export const deleteTask = commonRequest(service.frelease.deleteTask);

export const queryTasks = commonRequest(service.frelease.queryTasks);

export const auditAdd = commonRequest(service.frelease.taskAuditAdd);

// 根据任务关联项id修改关联项审核结果
export const updateTaskReview = commonRequest(service.ftask.updateTaskReview);

// 根据任务id查询任务关联项审核最终结果
export const queryTaskReview = commonRequest(service.ftask.queryTaskReview);

export const queryApply = commonRequest(service.frelease.queryApplications);

export const queryComponent = commonRequest(service.frelease.queryComponent);

export const queryProdInfo = commonRequest(service.frelease.queryProdInfo);

// 新建变更--变更模板查询
export const queryVersion = commonRequest(service.frelease.getReleaseVersion);

export const changesUpdate = commonRequest(service.frelease.releaseUpdate);

export const queryAppWithOutSum = commonRequest(
  service.frelease.queryApplications
);

/* 删除变更 */
export const deleteRelease = commonRequest(service.frelease.releaseDelete);

/* 查询小组标识 */
export const queryGroupAbbr = commonRequest(service.frelease.queryGroupAbbr);

/* 修改小组标识 */
export const updateGroupAbbr = commonRequest(service.frelease.updateGroupAbbr);

/* 查询变更计划 */
export const queryPlan = commonRequest(service.frelease.queryPlan);

/* 生成模板实例 */
export const generateProdExcel = commonRequest(
  service.frelease.generateProdExcel
);

/* 修改系统缩写标识 */
export const updateSysRlsInfo = commonRequest(
  service.frelease.updateSysRlsInfo
);

export const queryResourceProjects = commonRequest(
  service.frelease.queryResourceProjects
);

export const packageFromTag = commonRequest(service.frelease.packageFromTag);

export const relDevops = commonRequest(service.frelease.relDevops);

// 查询任务关联项
export const queryTasksReviews = commonRequest(
  service.frelease.queryTasksReviews
);

export const sendEmailForTaskManagers = commonRequest(
  service.frelease.sendEmailForTaskManagers
);

// tag打包--选择分支
export const queryApplicationTags = commonRequest(
  service.frelease.queryApplicationTags
);

/* 查询弹性接口 */
export const queryAppScale = commonRequest(service.frelease.queryAppScale);

/* 新增弹性接口 */
export const addAppScale = commonRequest(service.frelease.addAppScale);

/* 删除弹性接口 */
export const deleteAppScale = commonRequest(service.frelease.deleteAppScale);

/* 修改弹性接口 */
export const updateAppScale = commonRequest(service.frelease.updateAppScale);

/* 根据变更id查询当前变更所有介质文件 */
export const queryAllProdAssets = commonRequest(
  service.frelease.queryAllProdAssets
);

/* 根据变更id查询当前变更所有非自动化介质文件 */
export const queryDeAutoAllProdAssets = commonRequest(
  service.frelease.queryDeAutoAllProdAssets
);

/* 导出发布说明*/
export const exportProdDirection = commonRequest(
  service.frelease.exportProdDirection
);

/* 投产应用-提交发布-查询是否有未进入uat阶段的任务*/
export const tasksInSitStage = commonRequest(service.frelease.tasksInSitStage);

/* 查询小组标识*/
export const queryGroupSysAbbr = commonRequest(
  service.frelease.queryGroupSysAbbr
);

/* 修改小组标识*/
export const updateGroupSysAbbr = commonRequest(
  service.frelease.updateGroupSysAbbr
);

/* 根据taskid查投产*/
export const queryDetailByTaskId = commonRequest(
  service.frelease.queryDetailByTaskId
);

/* 以下是参数维护 */
export const queryModuleType = commonRequest(service.frelease.queryModuleType);

export const deleteModuleType = commonRequest(
  service.frelease.deleteModuleType
);

export const addModuleType = commonRequest(service.frelease.addModuleType);

export const updateModuleType = commonRequest(
  service.frelease.updateModuleType
);

/* 以下是脚本参数 */
export const queryScript = commonRequest(service.frelease.queryScript);

export const deleteScript = commonRequest(service.frelease.deleteScript);

export const addScript = commonRequest(service.frelease.addScript);

export const updateScript = commonRequest(service.frelease.updateScript);

/* 以下是变更环境维护 */
export const queryAutomationEnv = commonRequest(
  service.frelease.queryAutomationEnv
);

export const deleteAutomationEnv = commonRequest(
  service.frelease.deleteAutomationEnv
);

export const addAutomationEnv = commonRequest(
  service.frelease.addAutomationEnv
);

export const updateAutomationEnv = commonRequest(
  service.frelease.updateAutomationEnv
);

export const queryDBAssets = commonRequest(service.frelease.queryDBAssets);

/* 试运行发布查询*/
export const createTestrunBranch = commonRequest(
  service.frelease.createTestrunBranch
);

/* 试运行发布-确定*/
export const mergeTaskBranch = commonRequest(service.frelease.mergeTaskBranch);

/* 根据应用id查任务 */
export const queryNotinlineTasksByAppId = commonRequest(
  service.ftask.queryNotinlineTasksByAppId
);

/* 查询已试运行任务 */
export const queryTaskByTestRunId = commonRequest(
  service.frelease.queryTaskByTestRunId
);

export const findByProdId = commonRequest(service.frelease.findByProdId);

// 变更详情查询
export const queryChangesDetail = commonRequest(service.frelease.queryDetail);

export const changeConfirmConf = commonRequest(
  service.frelease.changeConfirmConf
);

export const changeReleaseConf = commonRequest(
  service.frelease.changeReleaseConf
);

/* 关联项查询 */
export const queryReviewRecord = commonRequest(service.ftask.queryReviewRecord);

/* 关联项详情更新 */
export const updateReviewRecord = commonRequest(
  service.ftask.updateReviewRecord
);

// 通过标签模糊查询环境
export const queryByLabelsFuzzy = commonRequest(
  service.fenvconfig.queryByLabelsFuzzy
);

// 根据任务id查询审核基本信息
export const queryReviewBasicMsg = commonRequest(
  service.ftask.queryReviewBasicMsg
);

export const queryByReleaseNodeName = commonRequest(
  service.frelease.queryByReleaseNodeName
);

export const deleteFile = commonRequest(service.frelease.deleteFile);

export const taskChangeNotise = commonRequest(
  service.frelease.taskChangeNotise
);

export async function downloadFiles(params) {
  return request(service.frelease.downloadFiles, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export const addSystemTestFile = commonRequest(
  service.frelease.addSystemTestFile
);

export const addSourceReview = commonRequest(service.frelease.addSourceReview);

export const pullGrayBranch = commonRequest(service.frelease.pullGrayBranch);

// 创建变更模板
export const createATemplate = commonRequest(service.frelease.templateCreate);

export const querySystem = commonRequest(service.frelease.querySystem);

export const queryExcelTemplate = commonRequest(
  service.frelease.queryExcelTemplate
);

export const editFakeInfo = commonRequest(service.frelease.editFakeInfo);

// 用于iOS和Android应用的打包
export const iOSOrAndroidAppPublish = commonRequest(
  service.frelease.iOSOrAndroidAppPublish
);

// 创建投产大窗口
export const createBigReleaseNode = commonRequest(
  service.frelease.createBigReleaseNode
);

// 查询投产大窗口
export const queryBigReleaseNodes = commonRequest(
  service.frelease.queryBigReleaseNodes
);

// 查询投产大窗口详情
export const queryBigReleaseNodeDetail = commonRequest(
  service.frelease.queryBigReleaseNodeDetail
);

// 查询会同联系人
export const queryContactInfo = commonRequest(
  service.frelease.queryContactInfo
);

// 编辑投产大窗口
export const updateBigReleasenode = commonRequest(
  service.frelease.updateBigReleasenode
);

// 查询投产大窗口-需求列表
export const queryRqrmntInfoList = commonRequest(
  service.frelease.queryRqrmntInfoList
);

// 查询投产大窗口-查询投产周期时间
export const queryByReleaseDate = commonRequest(
  service.frelease.queryByReleaseDate
);

// 查询投产大窗口-修改投产周期时间
export const updateBigReleaseDate = commonRequest(
  service.frelease.updateBigReleaseDate
);

// 查询投产意向任务
export const queryProWantTasks = commonRequest(
  service.frelease.queryProWantTasks
);

export const queryBatchInfoByTaskId = commonRequest(
  service.frelease.queryBatchInfoByTaskId
);

/* 修改批次、设置批次 */
export const addBatch = commonRequest(service.frelease.addBatch);

/* 查询提测重点 */
export const queryRqrmntInfoListByType = commonRequest(
  service.frelease.queryRqrmntInfoListByType
);

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

export const preview = commonRequest(service.fdocmanage.preview);

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

export const queryBatchInfoByAppId = commonRequest(
  service.frelease.queryBatchInfoByAppId
);

//根据投产窗口名称查询系统
export const queryReleaseSystem = commonRequest(
  service.frelease.queryReleaseSystem
);

export const queryRqrmntInfoTasks = commonRequest(
  service.frelease.queryRqrmntInfoTasks
);

export async function exportSpecialRqrmntInfoList(params) {
  return request(service.frelease.exportSpecialRqrmntInfoList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export const queryDeAutoAssets = commonRequest(
  service.frelease.queryDeAutoAssets
);

export const deAutoUpload = commonRequest(service.frelease.deAutoUpload);

export const queryPackageTags = commonRequest(
  service.frelease.queryPackageTags
);

export const setPackageTag = commonRequest(service.frelease.setPackageTag);

export const queryConfigApplication = commonRequest(
  service.frelease.queryConfigApplication
);

export const addConfigApplication = commonRequest(
  service.frelease.addConfigApplication
);

export const checkConfigApplication = commonRequest(
  service.frelease.checkConfigApplication
);

export const createConfig = commonRequest(service.frelease.createConfig);

export const deleteConfig = commonRequest(service.frelease.deleteConfig);

export const queryConfigSum = commonRequest(service.frelease.queryConfigSum);

export const confirmChanges = commonRequest(service.frelease.confirmChanges);

export const deleteReleaseNode = commonRequest(
  service.frelease.deleteReleaseNode
);

//系统投产信息查询（根据变更id）
export const querySystemDetailByProdId = commonRequest(
  service.frelease.querySystemDetailByProdId
);

// 获取审核通过的数据库文件路径
export const queryDbPath = commonRequest(service.frelease.queryDbPath);

// 数据库变更文件上传
export const uploadAssets = commonRequest(service.frelease.uploadAssets);

// 新增/修改审核记录
export const addReview = commonRequest(service.ftask.addReview);

// 查询变更应用介质目录可选项
export const queryProdDir = commonRequest(service.frelease.queryProdDir);

// 修改灰度变更应用介质目录
export const updateProdDir = commonRequest(service.frelease.updateProdDir);

// 修改应用卡片的部署平台
export const updateProdDeploy = commonRequest(
  service.frelease.updateProdDeploy
);

// 根据应用id查询部署平台
export const queryDeployTypeByAppId = commonRequest(
  service.frelease.queryDeployTypeByAppId
);

// 查询全量环境
export const queryEnvList = commonRequest(service.fenvconfig.envQuery);

// 对象存储修改组
export const updateAwsAssetGroupId = commonRequest(
  service.frelease.updateAwsAssetGroupId
);

// 查询对象存储用户所属组列表
export const queryAwsGroups = commonRequest(
  service.frelease.queryAwsConfigByGroupId
);

export const queryReleaseNode = commonRequest(
  service.frelease.queryReleaseNodes
);

// 查询当前组以及子组的所有人员
export const queryGroupPeople = commonRequest(service.fuser.queryByGroupId);

export const queryReleaseNodeByJob = commonRequest(
  service.frelease.queryDetailByTaskId
);

export const addJobReleaseNode = commonRequest(service.frelease.taskAdd);
