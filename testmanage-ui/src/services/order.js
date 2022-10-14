import request from '@/common/request';
import service from './serviceMap';

// 安全工单导出查询交易清单
export async function queryLastTransFilePath(params) {
  return request(service.torder.queryLastTransFilePath, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function querySplitOrderList(params) {
  return request(service.torder.querySplitOrderList, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function refuseTask(params) {
  return request(service.torder.refuseTask, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryWorkOrderName(params) {
  return request(service.torder.queryWorkOrderName, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryFdevTaskByWorkNo(params) {
  return request(service.torder.queryFdevTaskByWorkNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function splitWorkOrder(params) {
  return request(service.torder.splitWorkOrder, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function mergeWorkOrder(params) {
  return request(service.torder.mergeWorkOrder, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//根据工单编号和任务id查询任务
export async function queryTasks(params) {
  return request(service.torder.queryTasks, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//查询同一实施单元下的工单列表
export async function queryMergeOrderList(params) {
  return request(service.torder.queryMergeOrderList, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 新增工单验证名字是否重复
export async function verifyOrderName(params) {
  return request(service.torder.verifyOrderName, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 工单详情查询相关文档
export async function queryTaskDoc(params) {
  return request(service.ftask.queryTaskDoc, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// sit缺陷页面点击案例编号跳转到plan页面 所需的主任务,测试人员,阶段，实施单元编号
export async function queryTaskNameTestersByNo(params) {
  return request(service.tcase.queryTaskNameTestersByNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 提测打回工单
export async function workOrderRollback(params) {
  return request(service.torder.workOrderRollback, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryPlanOrderList(params) {
  return request(service.torder.queryPlanOrderList, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function oauthLogin(params) {
  return request(service.tuser.oauthLogin, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAssignOrderImpl(params) {
  return request(service.torder.queryAssignOrderImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryMyOrderImpl(params) {
  return request(service.torder.queryMyOrderImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function iWantToassignImpl(params) {
  return request(service.torder.iWantToassignImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryTotalImpl(params) {
  return request(service.torder.queryTotalImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAllUserImpl(params) {
  return request(service.tuser.queryAllUserImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function submitAddOrderFormImpl(params) {
  return request(service.torder.submitAddOrderFormImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function submitOrderFormImpl(params) {
  return request(service.torder.submitOrderFormImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function updateOrderImpl(params) {
  return request(service.torder.updateOrderImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryHistoryOrderImpl(params) {
  return request(service.torder.queryHistoryOrderImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryHistoryCountImpl(params) {
  return request(service.torder.queryHistoryCountImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryQueryOrderImpl(params) {
  return request(service.torder.queryQueryOrderImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryQueryCountImpl(params) {
  return request(service.torder.queryQueryCountImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryTaskListImpl(params) {
  return request(service.torder.queryTaskListImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryTaskListCountImpl(params) {
  return request(service.torder.queryTaskListCountImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryMessageImpl(params) {
  return request(service.torder.queryMessageImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryMessageCount(params) {
  return request(service.torder.queryMessageCount, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function ignoreMessageImpl(params) {
  return request(service.torder.ignoreMessageImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function ignoreAllMessageImpl(params) {
  return request(service.torder.ignoreAllMessageImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function passAduit(params) {
  return request(service.torder.passAduit, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function refuseAduit(params) {
  return request(service.torder.refuseAduit, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAssessorList(params) {
  return request(service.tuser.queryAssessorList, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function rollBackWorkOrder(params) {
  return request(service.torder.rollBackWorkOrder, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAduitOrder(params) {
  return request(service.torder.queryAduitOrder, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAduitOrderCount(params) {
  return request(service.torder.queryAduitOrderCount, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function selectNameImpl(params) {
  return request(service.tuser.selectNameImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 查询工单状态
export async function queryWorkOrderStageList(params) {
  return request(service.torder.queryWorkOrderStageList, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 查询工单名（模糊匹配）
export async function queryWorkOrderList(params) {
  return request(service.torder.queryWorkOrderList, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 查询废弃工单
export async function queryWasteOrder(params) {
  return request(service.torder.queryWasteOrder, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 查询废弃工单数量
export async function queryWasteOrderCount(params) {
  return request(service.torder.queryWasteOrderCount, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function sendSitDoneMail(params) {
  return request(service.torder.sendSitDoneMail, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryRqrFilesByOrderNo(params) {
  return request(service.torder.queryRqrFilesByOrderNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function preview(params) {
  return request(service.fdocmanage.preview, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryOrderByOrderNo(params) {
  return request(service.torder.queryOrderByOrderNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function exportUserAllOrder(params) {
  return request(service.torder.exportUserAllOrder, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 上传文档
export async function uploadDocFile(params) {
  return request(service.torder.uploadDocFile, {
    method: 'POST',
    data: params
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

export async function queryOrderInfoByNo(params) {
  return request(service.torder.queryOrderInfoByNo, {
    method: 'post',
    data: params
  });
}

export async function qualityReportExport(params) {
  return request(service.torder.qualityReportExport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function qualityReportNewUnitExport(params) {
  return request(service.torder.qualityReportNewUnitExport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function querySubmitTimelyAndMantis(params) {
  return request(service.torder.querySubmitTimelyAndMantis, {
    method: 'post',
    data: params
  });
}
