import request from '@/utils/request';
import service from './api';

export async function queryDemandFile(params) {
  return request(service.fdemand.queryDemandFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryByFdevNoAndDemandId(params) {
  return request(service.fdemand.queryByFdevNoAndDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function save(params) {
  return request(service.fdemand.save, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryDemandList(params) {
  return request(service.fdemand.queryDemandList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//更新
export async function update(params) {
  return request(service.fdemand.update, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//撤销
export async function deleteRqr(params) {
  return request(service.fdemand.deleteRqr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryPaginationByDemandId(params) {
  return request(service.fdemand.queryPaginationByDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDemandInfoDetail(params) {
  return request(service.fdemand.queryDemandInfoDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//归档
export async function fileRqr(params) {
  return request(service.fdemand.fileRqr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteUnitById(params) {
  return request(service.fdemand.deleteUnitById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDemandDoc(params) {
  return request(service.fdemand.queryDemandDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function search(params) {
  return request(service.fdemand.search, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addImplementUnit(params) {
  return request(service.fdemand.addImplementUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function supplyImplementUnit(params) {
  return request(service.fdemand.supplyImplementUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateImplementUnit(params) {
  return request(service.fdemand.updateImplementUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryByGroupId(params) {
  return request(service.fdemand.queryByGroupId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryUnitByIpmpTaskId(params) {
  return request(service.fdemand.queryUnitByIpmpTaskId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addIpmpTask(params) {
  return request(service.fdemand.addIpmpTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addUnit(params) {
  return request(service.fdemand.addUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 下载文件
export async function exportExcelData(params) {
  return request(service.fdemand.exportExcelData, {
    method: 'GET',
    responseType: 'arraybuffer',
    params: {
      ...params
    }
  });
}

export async function updateDemandDoc(params) {
  return request(service.fdemand.updateDemandDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function defer(params) {
  return request(service.fdemand.defer, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function exportAssessExcel(params) {
  return request(service.fdemand.exportAssessExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function recover(params) {
  return request(service.fdemand.recover, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//任务新增查询实施单元
export async function queryAvailableIpmpUnit(params) {
  return request(service.fdemand.queryAvailableIpmpUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//任务新增查询研发单元
export async function queryAvailableIpmpUnitNew(params) {
  return request(service.fdemand.queryAvailableIpmpUnitNew, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function exportDemandsExcel(params) {
  return request(service.fdemand.exportDemandsExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function exportModelExcel(params) {
  return request(service.fdemand.exportModelExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function assess(params) {
  return request(service.fdemand.assess, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteDemandDoc(params) {
  return request(service.fdemand.deleteDemandDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryImplByGroupAndDemandId(params) {
  return request(service.fdemand.queryImplByGroupAndDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateByRecover(params) {
  return request(service.fdemand.updateByRecover, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteById(params) {
  return request(service.fdemand.deleteById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function getDesignInfo(params) {
  return request(service.fdemand.getDesignInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateDesignStage(params) {
  return request(service.fdemand.updateDesignStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateDesignRemark(params) {
  return request(service.fdemand.updateDesignRemark, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function importDemandExcel(params) {
  return request(service.fdemand.importDemandExcel, {
    method: 'POST',
    params: {
      params
    }
  });
}

export async function queryDemandByOaContactNo(params) {
  return request(service.fdemand.queryDemandByOaContactNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateImpl(params) {
  return request(service.fdemand.updateImpl, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryPartInfo(params) {
  return request(service.fdemand.queryPartInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询实施单元
export function queryIpmpUnitByDemandId(params) {
  return request(service.fdemand.queryIpmpUnitByDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据实施单元id查看实施单元详情
export async function queryIpmpUnitById(params) {
  return request(service.fdemand.queryIpmpUnitById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//修改实施单元
export async function updateIpmpUnit(params) {
  return request(service.fdemand.updateIpmpUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询项目和任务集信息
export async function queryIpmpProject(params) {
  return request(service.fdemand.queryIpmpProject, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询牵头单位和团队信息
export async function queryIpmpLeadTeam(params) {
  return request(service.fdemand.queryIpmpLeadTeam, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询ipmp人员信息
export async function queryIpmpUser(params) {
  return request(service.fdemand.queryIpmpUser, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询研发单元详情
export async function queryFdevImplUnitDetail(params) {
  return request(service.fdemand.queryFdevImplUnitDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//通过实施单元编号查询任务
export async function queryTaskByIpmpUnitNo(params) {
  return request(service.fdemand.queryTaskByIpmpUnitNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//挂载
export async function mount(params) {
  return request(service.fdemand.mount, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查看实施单元下的研发单元
export async function queryPaginationByIpmpUnitNo(params) {
  return request(service.fdemand.queryPaginationByIpmpUnitNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查看研发单元下的任务
export async function queryDetailByUnitNo(params) {
  return request(service.fdemand.queryDetailByUnitNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除研发单元
export async function deleteDevUnitById(params) {
  return request(service.fdemand.deleteById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//判断评估完成按钮是否置灰
export async function assessButton(params) {
  return request(service.fdemand.assessButton, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//获取研发单元剩余工作量
export async function getWorker(params) {
  return request(service.fdemand.getWorker, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询科技类型
export async function queryTechType(params) {
  return request(service.fdemand.queryTechType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询需求下当前登录人是否有牵头的实施单元未核算
export async function queryIpmpUnitIsCheck(params) {
  return request(service.fdemand.queryIpmpUnitIsCheck, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增其他需求任务
export async function addOtherDemandTask(params) {
  return request(service.fdemand.addOtherDemandTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//同步IPMP
export async function syncIpmpUnit(params) {
  return request(service.fdemand.syncIpmpUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//修其他需求任务
export async function updateDemandTask(params) {
  return request(service.fdemand.updateDemandTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除其他需求任务
export async function deleteOtherDemandTask(params) {
  return request(service.fdemand.deleteOtherDemandTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询其他需求任务详情
export async function queryOtherDemandTask(params) {
  return request(service.fdemand.queryOtherDemandTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询其他需求任务列表
export async function queryOtherDemandTaskList(params) {
  return request(service.fdemand.queryOtherDemandTaskList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 判断当前用户是否展示新增研发单元按钮
// isShowAdd
export async function isShowAdd(params) {
  return request(service.fdemand.isShowAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询超期分类列表
export async function getOverdueTypeSelect(params) {
  return request(service.fdemand.getOverdueTypeSelect, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询评估需求列表
export async function queryEvaMgtList(params) {
  return request(service.fdemand.queryEvaMgtList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 添加评估需求
export async function saveEvaMgt(params) {
  return request(service.fdemand.saveEvaMgt, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改评估需求
export async function updateEvaMgt(params) {
  return request(service.fdemand.updateEvaMgt, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 导出评估需求列表
export async function exportEvaMgtList(params) {
  return request(service.fdemand.exportEvaMgtList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryById(params) {
  return request(service.fdemand.queryById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deleteData(params) {
  return request(service.fdemand.delete, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function exportExcel(params) {
  return request(service.fdemand.export, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

//查询基础用户信息（不关联公司、小组、角色、标签等详细信息）
export async function queryUserCoreData(params) {
  return request(service.fdemand.queryUserCoreData, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryIpmpUnitList(params) {
  return request(service.fdemand.queryIpmpUnitList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function exportIpmpUnitList(params) {
  return request(service.fdemand.exportIpmpUnitList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function queryFdevUnitList(params) {
  return request(service.fdemand.queryFdevUnitList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function exportFdevUnitList(params) {
  return request(service.fdemand.exportFdevUnitList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function getSchemeReview(params) {
  return request(service.fdemand.getSchemeReview, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getCloudCheckers(params) {
  return request(service.fdemand.getCloudCheckers, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询研发单元审批列表
export async function queryApproveList(params) {
  return request(service.fdemand.queryApproveList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//导出研发单元审批列表
export async function exportApproveList(params) {
  return request(service.fdemand.exportApproveList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

//研发单元审批通过
export async function approvePass(params) {
  return request(service.fdemand.approvePass, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//研发单元审批拒绝
export async function approveReject(params) {
  return request(service.fdemand.approveReject, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询评估完成时间和XY值
export async function queryDemandAssessDate(params) {
  return request(service.fdemand.queryDemandAssessDate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//请超期审批
export async function applyApprove(params) {
  return request(service.fdemand.applyApprove, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询我的审批列表
export async function queryMyApproveList(params) {
  return request(service.fdemand.queryMyApproveList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据字典类型查询字典
export async function queryByTypes(params) {
  return request(service.fdemand.queryByTypes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//内测选项列表
export async function queryInnerTestTab(params) {
  return request(service.fdemand.queryInnerTestTab, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//确认延期
export async function confirmDelay(params) {
  return request(service.fdemand.confirmDelay, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//创建提测单
export async function addTestOrder(params) {
  return request(service.fdemand.addTestOrder, {
    method: 'POST',
    'Content-Type': 'multipart/form-data',
    Accept: 'application/json',
    data: params
  });
}

//修改提测单
export async function updateTestOrder(params) {
  return request(service.fdemand.updateTestOrder, {
    method: 'POST',
    'Content-Type': 'multipart/form-data',
    Accept: 'application/json',
    data: params
  });
}

//查询提测单列表
export async function queryTestOrderList(params) {
  return request(service.fdemand.queryTestOrderList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//调整排期
export async function adjustDate(params) {
  return request(service.fdemand.adjustDate, {
    method: 'POST',
    'Content-Type': 'multipart/form-data',
    Accept: 'application/json',
    data: params
  });
}

//确认提交测试单
export async function submitTestOrder(params) {
  return request(service.fdemand.submitTestOrder, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除测试单
export async function deleteTestOrder(params) {
  return request(service.fdemand.deleteTestOrder, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询提测单详情
export async function queryTestOrderDetail(params) {
  return request(service.fdemand.queryTestOrderDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询提测单复制权限
export async function queryCopyFlag(params) {
  return request(service.fdemand.queryCopyFlag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//提测单附件上传
export async function uploadTestOrderFile(params) {
  return request(service.fdemand.uploadTestOrderFile, {
    method: 'POST',
    data: params
  });
}

//删除文件
export async function deleteEmailFile(params) {
  return request(service.fdemand.deleteEmailFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//提测单附件下载
export async function testOrderDownload(params) {
  return request(service.fdocmanage.testOrderDownload, {
    method: 'GET',
    responseType: 'arraybuffer',
    params: {
      ...params
    }
  });
}

//提测单附件删除
export async function deleteTestOrderFile(params) {
  return request(service.fdemand.deleteTestOrderFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//提测单列表
export async function queryTestOrderFile(params) {
  return request(service.fdemand.queryTestOrderFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//测试经理
export async function getTestManagerInfo(params) {
  return request(service.fdemand.getTestManagerInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//提测单研发单元列表
export async function queryFdevUnitListByDemandId(params) {
  return request(service.fdemand.queryFdevUnitListByDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//获取小组的第三级小组
export async function getThreeLevelGroup(params) {
  return request(service.fdemand.getThreeLevelGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//获取需求列表
export async function queryTechBusinessForSelect(params) {
  return request(service.fdemand.queryTechBusinessForSelect, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//手动确认需求评估完成
export async function confirmFinish(params) {
  return request(service.fdemand.confirmFinish, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//取消暂缓接口
export async function cancelDefer(params) {
  return request(service.fdemand.cancelDefer, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//修改定稿日期
export async function updateFinalDate(params) {
  return request(service.fdemand.updateFinalDate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//列表展示定稿日期修改申请
export async function queryRqrApproveList(params) {
  return request(service.fdemand.queryRqrApproveList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查看我的需求评估定稿审批列表
export async function queryMyList(params) {
  return request(service.fdemand.queryMyList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//拒绝审批
export async function refuse(params) {
  return request(service.fdemand.refuse, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//同意审批
export async function agree(params) {
  return request(service.fdemand.agree, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询待审批及已完成数据
export async function queryCount(params) {
  return request(service.fdemand.queryCount, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询实施单元
export async function getImplUnitRelatSpFlag(params) {
  return request(service.fdemand.getImplUnitRelatSpFlag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
