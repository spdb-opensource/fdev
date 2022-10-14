import request from '@/utils/request.js';
import service from './api.js';

/* 查询当前用户*/
export async function queryCurrent() {
  return request(service.fuser.currentUser, {
    method: 'POST',
    data: {}
  });
}

// 环境列表查询
export async function queryBaseImage(params) {
  return request(service.fcomponent.queryBaseImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 环境列表详情查询
export async function queryBaseImageDetail(params) {
  return request(service.fcomponent.queryBaseImageDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增基础镜像优化需求
export async function AddOptimizeBaseImageIssue(params) {
  return request(service.fcomponent.AddOptimizeBaseImageIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 基础镜像录入
export async function addBaseImage(params) {
  return request(service.fcomponent.addBaseImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 历史版本列表查询
export async function queryBaseImageRecord(params) {
  return request(service.fcomponent.queryBaseImageRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 镜像详情
export async function queryBaseImageIssueDetail(params) {
  return request(service.fcomponent.queryBaseImageIssueDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询不同阶段历史版本
export async function queryImageIssueRecord(params) {
  return request(service.fcomponent.queryImageIssueRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取详情
export async function queryBaseImageIssue(params) {
  return request(service.fcomponent.queryBaseImageIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 改变stage的步骤
export async function changeImageStage(params) {
  return request(service.fcomponent.changeImageStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件查询
export async function queryComponent(params) {
  return request(service.fcomponent.queryComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件录入
export async function addComponent(params) {
  return request(service.fcomponent.addComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件更新
export async function updateComponent(params) {
  return request(service.fcomponent.updateComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改组件历史版本
export async function updateComponentHistary(params) {
  return request(service.fcomponent.updateComponentHistary, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件优化 新增
export async function optimizeComponent(params) {
  return request(service.fcomponent.optimizeComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改需求阶段状态
export async function changeStage(params) {
  return request(service.fcomponent.changeStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 不同版本下的优化需求 queryIssueRecord
export async function queryIssueRecord(params) {
  return request(service.fcomponent.queryIssueRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件优化 传入id查询所有组件优化需求
export async function queryComponentIssues(params) {
  return request(service.fcomponent.queryComponentIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件优化详情 传入id查询所有组件优化需求
export async function queryComponentIssueDetail(params) {
  return request(service.fcomponent.queryComponentIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件打包发布
export async function packageLog(params) {
  return request(service.fcomponent.package, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 传入父组件id，查询gitlab地址
export async function queryComponentDetail(params) {
  return request(service.fcomponent.queryComponentDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 扫描应用组件
export async function scanApplication(params) {
  return request(service.fcomponent.scanApplication, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 镜像版本正式发布
export async function relasePackage(params) {
  return request(service.fcomponent.relasePackage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 扫描应用组件
export async function scanComponent(params) {
  return request(service.fcomponent.scanComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询组件历史版本
export async function queryComponentHistory(params) {
  return request(service.fcomponent.queryComponentHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件详情-使用版本-列表查询
export async function queryUsingStatusList(params) {
  return request(service.fcomponent.queryApplicatons, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 应用维度-列表查询
export async function queryComponentsByApplicaton(params) {
  return request(service.fcomponent.queryComponents, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件维度-列表查询
export async function queryApplicatonsByComponent(params) {
  return request(service.fcomponent.queryApplicatons, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新增组件
export async function createComponent(params) {
  return request(service.fcomponent.createComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改基础镜像
export async function updateBaseImage(params) {
  return request(service.fcomponent.updateBaseImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 将历史版本的invalid恢复成正式版本
export async function recoverInvalidRecord(params) {
  return request(service.fcomponent.recoverInvalidRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 试用发布打包
export async function packageTag(params) {
  return request(service.fcomponent.packageTag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改镜像历史版本详情
export async function updateBaseImageRecord(params) {
  return request(service.fcomponent.updateBaseImageRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取邮件内容
export async function mailContent(params) {
  return request(service.fcomponent.mailContent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 元数据
export async function queryMetaData(params) {
  return request(service.fcomponent.queryMetaData, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 历史扫描
export async function scanHistory(params) {
  return request(service.fcomponent.scanComponentHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//骨架查询
export async function queryArchetypes(params) {
  return request(service.fcomponent.queryArchetypes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//我的骨架查询--后端
export async function queryMyArchetypes(params) {
  return request(service.fcomponent.queryMyArchetypes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//我的骨架查询--前端
export async function queryMyMpassArchetypes(params) {
  return request(service.fcomponent.queryMyMpassArchetypes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//骨架详情查询
export async function queryArchetypeDetail(params) {
  return request(service.fcomponent.queryArchetypeDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//骨架-信息维护
export async function updateArchetype(params) {
  return request(service.fcomponent.updateArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询骨架历史版本--后端骨架
export async function queryArchetypeHistory(params) {
  return request(service.fcomponent.queryArchetypeHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询骨架历史版本--前端骨架
export async function queryMpassArchetypeHistory(params) {
  return request(service.fcomponent.queryMpassArchetypeHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//骨架-信息录入
export async function addArchetype(params) {
  return request(service.fcomponent.addArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架历史扫描
export async function scanArchetypeHistory(params) {
  return request('/fcomponent/api/archetype/scanArchetypeHistory', {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架-新增需求
export async function optimizeArchetype(params) {
  return request(service.fcomponent.optimizeArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架优化列表
export async function queryArchetypeIssues(params) {
  return request(service.fcomponent.queryArchetypeIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架--下一阶段
export async function archetypeChangeStage(params) {
  return request(service.fcomponent.archetypeChangeStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架--发布
export async function archetypePackage(params) {
  return request(service.fcomponent.archetypePackage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架--发布历史
export async function queryArchetypeIssueRecord(params) {
  return request(service.fcomponent.queryArchetypeIssueRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架--需求详情
export async function queryArchetypeIssueDetail(params) {
  return request(service.fcomponent.queryArchetypeIssueDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

/* 修改骨架历史版本 */
export async function updateArchetypeHistory(params) {
  return request(service.fcomponent.updateArchetypeHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询骨架模板
export async function queryConfigTemplate(params) {
  return request(service.fcomponent.queryConfigTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 保存骨架模板
export async function saveConfigTemplate(params) {
  return request(service.fcomponent.saveConfigTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询骨架集成的组件列表
export async function queryComponentByArchetype(params) {
  return request(service.fcomponent.queryComponentByArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询骨架集成的应用列表
export async function queryApplicationsByArchetype(params) {
  return request(service.fcomponent.queryApplicationsByArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 扫描使用骨架的应用
export async function scanArchetype(params) {
  return request(service.fcomponent.scanArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件-处理-查询发布日志
export async function queryReleaseLog(params) {
  return request(service.fcomponent.queryReleaseLog, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件-处理-查询发布日志
export async function archetypeReleaseLog(params) {
  return request(service.fcomponent.archetypeReleaseLog, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询所有骨架类型
export async function queryArchetypeTypes(params) {
  return request(service.fcomponent.queryArchetypeTypes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 我的组件查询--后端
export async function queryMyComponent(params) {
  return request(service.fcomponent.queryMyComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 我的组件查询--前端
export async function queryMyMpassComponents(params) {
  return request(service.fcomponent.queryMyMpassComponents, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架打包时查询第一个开发版本
export async function queryArchetypeFirstVersion(params) {
  return request(service.fcomponent.queryArchetypeFirstVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 组件打包时查询第一个开发版本
export async function queryComponentFirstVersion(params) {
  return request(service.fcomponent.queryComponentFirstVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询组件依赖树
export async function queryDependencyTree(params) {
  return request(service.fcomponent.queryDependencyTree, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 废弃组件
export async function destroyComponentIssue(params) {
  return request(service.fcomponent.destroyComponentIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 废弃骨架
export async function destroyArchetypeIssue(params) {
  return request(service.fcomponent.destroyArchetypeIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 废弃镜像
export async function destroyBaseImageIssue(params) {
  return request(service.fcomponent.destroyBaseImageIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询集成此组件的骨架列表
export async function queryFrameByComponent(params) {
  return request(service.fcomponent.queryFrameByComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 导出组件的应用使用现状
export async function exportExcelByComponent(params) {
  return request(service.fcomponent.exportExcelByComponent, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 前端组件库
export async function queryMpassComponents(params) {
  return request(service.fcomponent.queryMpassComponents, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// mpass组件信息录入
export async function addMpassComponent(params) {
  return request(service.fcomponent.addMpassComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// mpass组件信息更新
export async function updateMpassComponent(params) {
  return request(service.fcomponent.updateMpassComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询应用集成的mpass组件列表（应用维度查询）
export async function queryWebcomByApplication(params) {
  return request(service.fcomponent.queryWebcomByApplication, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询mpass组件使用现状（组件维度查询）
export async function queryWebcomByComponent(params) {
  return request(service.fcomponent.queryWebcomByComponent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 扫描应用使用mpass组件情况（应用维度扫描）
export async function scanMpassComByApp(params) {
  return request(service.fcomponent.scanMpassComByApp, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 扫描mpass组件使用现状(组件维度扫描)
export async function scanAppByMpassCom(params) {
  return request(service.fcomponent.scanAppByMpassCom, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询mpass组件详情信息
export async function queryMpassComponentDetail(params) {
  return request(service.fcomponent.queryMpassComponentDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询mpass组件历史版本列表
export async function queryMpassComHistory(params) {
  return request(service.fcomponent.queryMpassComHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 扫描mpass组件历史版本
export async function scanMpassComHistory(params) {
  return request(service.fcomponent.scanMpassComHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改mpass组件历史版本信息
export async function updateMpassComHistory(params) {
  return request(service.fcomponent.updateMpassComHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询组件优化需求列表（查询窗口列表）
export async function queryMpassReleaseIssue(params) {
  return request(service.fcomponent.queryMpassReleaseIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询组件开发优化需求列表
export async function queryMpassDevIssue(params) {
  return request(service.fcomponent.queryMpassDevIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 根据优化需求类型和组件名称回填分支和预设版本
export async function queryMpassDefaultBranchAndVersion(params) {
  return request(service.fcomponent.queryMpassDefaultBranchAndVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新增mpass组件优化需求（新增窗口）
export async function addMpassReleaseIssue(params) {
  return request(service.fcomponent.addMpassReleaseIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新增mpass组件开发优化
export async function addMpassDevIssue(params) {
  return request(service.fcomponent.addMpassDevIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询组件开发优化需求详情
export async function queryMpassDevIssueDetail(params) {
  return request(service.fcomponent.queryMpassDevIssueDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询优化需求打包版本
export async function queryMpassIssueRecord(params) {
  return request(service.fcomponent.queryMpassIssueRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 优化需求打包发布
export async function devPackage(params) {
  return request(service.fcomponent.devPackage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改优化需求阶段
export async function changeMpassStage(params) {
  return request(service.fcomponent.changeMpassStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// release阶段发布
export async function releasePackage(params) {
  return request(service.fcomponent.releasePackage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询mpass骨架列表信息
export async function queryMpassArchetypes(params) {
  return request(service.fcomponent.queryMpassArchetypes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// mpass骨架录入
export async function addMpassArchetype(params) {
  return request(service.fcomponent.addMpassArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改mpas骨架信息
export async function updateMpassArchetype(params) {
  return request(service.fcomponent.updateMpassArchetype, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询mpass骨架详情
export async function queryMpassArchetypeDetail(params) {
  return request(service.fcomponent.queryMpassArchetypeDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询mpass骨架优化需求
export async function queryMpassArchetypeIssue(params) {
  return request(service.fcomponent.queryMpassArchetypeIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新增mpass骨架优化需求
export async function addMpassArchetypeIssue(params) {
  return request(service.fcomponent.addMpassArchetypeIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询mpass骨架优化需求详情
export async function queryMpassArchetypeIssueDetail(params) {
  return request(service.fcomponent.queryMpassArchetypeIssueDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 骨架优化需求打包
export async function mpassArchetypePackage(params) {
  return request(service.fcomponent.mpassArchetypePackage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询骨架优化需求打包tag记录
export async function queryIssueTag(params) {
  return request(service.fcomponent.queryIssueTag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改当前优化需求阶段
export async function changeMpassArchetypeStage(params) {
  return request(service.fcomponent.changeMpassArchetypeStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新mpass组件优化需求（更新窗口信息）
export async function updateMpassReleaseIssue(params) {
  return request(service.fcomponent.updateMpassReleaseIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// Mpass投产窗口和开发需求废弃
export async function destroyIssue(params) {
  return request(service.fcomponent.destroyIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新mpass组件开发优化
export async function updateMpassDevIssue(params) {
  return request(service.fcomponent.updateMpassDevIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询组件优化需求详情（查询窗口详情）
export async function queryMpassReleaseIssueDetail(params) {
  return request(service.fcomponent.queryMpassReleaseIssueDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询使用此镜像的应用列表
export async function queryApplicationByImage(params) {
  return request(service.fcomponent.queryApplicationByImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 扫描应用使用镜像情况
export async function scanImage(params) {
  return request(service.fcomponent.scanImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询使用此镜像的骨架列表
export async function queryFrameByImage(params) {
  return request(service.fcomponent.queryFrameByImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询可迁移窗口
export async function queryTransgerReleaseIssue(params) {
  return request(service.fcomponent.queryTransgerReleaseIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 开发分支窗口迁移
export async function devIssueTransger(params) {
  return request(service.fcomponent.devIssueTransger, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 后端组件-优化需求废弃
export async function destroyIssueServer(params) {
  return request(service.fcomponent.destroyIssueServer, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 后端组件-组件开发打包
export async function devPackageServer(params) {
  return request(service.fcomponent.devPackageServer, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 后端组件-新增多模块组件开发优化-新增开发分支
export async function addDevIssue(params) {
  return request(service.fcomponent.addDevIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 后端组件-更新组件优化需求窗口
export async function updateReleaseIssue(params) {
  return request(service.fcomponent.updateReleaseIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 后端组件-新增组件优化需求
export async function addReleaseIssue(params) {
  return request(service.fcomponent.addReleaseIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 后端组件-查询多模块组件预设版本号及分支名
export async function defaultBranchAndVersion(params) {
  return request(service.fcomponent.defaultBranchAndVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 后端组件-release发布
export async function releasePackageServer(params) {
  return request(service.fcomponent.releasePackageServer, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询后端组件打包记录
export async function queryMultiIssueRecord(params) {
  return request(service.fcomponent.queryMultiIssueRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
