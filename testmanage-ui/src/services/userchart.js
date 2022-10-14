import request from '@/common/request';
import service from './serviceMap';
export async function selWork(params) {
  return request(service.torder.selWork, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function countUserTestCaseByTime(params) {
  return request(service.tuser.countUserTestCaseByTime, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function workPieData(params) {
  return request(service.torder.workPieData, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function workLineData(params) {
  return request(service.torder.workLineData, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function getGroupChartData(params) {
  return request(service.tuser.getGroupChartData, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 日报总报表
export async function queryDayTotalReport(params) {
  return request(service.torder.queryDayTotalReport, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 日报组表
export async function queryDayGroupReport(params) {
  return request(service.torder.queryDayGroupReport, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAllReportImpl(params) {
  return request(service.tcase.queryAllReportImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function exportExcelUser(params) {
  return request(service.tuser.exportExcelUser, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function exportExcelWeekReport(params) {
  return request(service.tcase.exportExcelWeekReport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function exportAllExcelWeekReport(params) {
  return request(service.tcase.exportAllExcelWeekReport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function exportExcelReport(params) {
  return request(service.torder.exportExcelReport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 导出工单维度报表
export async function exportOrderDimension(params) {
  return request(service.torder.exportOrderDimension, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function queryPersonReportImpl(params) {
  return request(service.tcase.queryPersonReportImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryGroupName(params) {
  return request(service.tuser.queryGroupName, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// sit 测试报告详情总数
export async function queryUpSitOrderCount(params) {
  return request(service.torder.queryUpSitOrderCount, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// sit 测试报告当前页
export async function queryUpSitOrder(params) {
  return request(service.torder.queryUpSitOrder, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// sit 测试报告 打印内容
export async function exportSitReportData(params) {
  return request(service.torder.exportSitReportData, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 已完成工单报表
export async function getCompletedWorkNoData(params) {
  return request(service.torder.getCompletedWorkNoData, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 已完成工单报表导出excel
export async function exportExcelFinishedOrder(params) {
  return request(service.torder.exportExcelFinishedOrder, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// fdev小组
export async function queryFdevGroup(params) {
  return request(service.fuser.queryFdevGroup, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// fdev小组-可根据条件查询在使用的小组
export async function queryGroup(params) {
  return request(service.fuser.queryGroup, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 零售组纬度/测试大组纬度柱状图
export async function countCaseTestByGroup(params) {
  return request(service.tuser.countCaseTestByGroup, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// fdev人员
export async function queryFdevUser(params) {
  return request(service.fuser.queryFdevUser, {
    method: 'post',
    data: {
      ...params,
      status: 0
    }
  });
}
// 提测打回报表
export async function queryRollbackReport(params) {
  return request(service.torder.queryRollbackReport, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 导出提测打回报表
export async function exportRollbackReport(params) {
  return request(service.torder.exportRollbackReport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 缺陷报表测试团队双周报
export async function testTeamBiweekly(params) {
  return request(service.tcase.testTeamBiweekly, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 日报报表 单组导出
export async function exportDayGroupReport(params) {
  return request(service.torder.exportDayGroupReport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 缺陷报表龙虎榜
export async function listOfDevelopDefects(params) {
  return request(service.tcase.listOfDevelopDefects, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 缺陷报表柱状图
export async function defectClassify(params) {
  return request(service.tcase.defectClassify, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 缺陷报表分布图
export async function teamDefectClassify(params) {
  return request(service.tcase.teamDefectClassify, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 日报报表 单组导出
export async function queryGroupInfo(params) {
  return request(service.torder.queryGroupInfo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryOrderInfoByUser(params) {
  return request(service.torder.queryOrderInfoByUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function exportPersonalDimensionReport(params) {
  return request(service.torder.exportPersonalDimensionReport, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 缺陷纬度总查询
export async function getDefectList(params) {
  return request(service.torder.qualityList, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function exportGroupStatement(params) {
  return request(service.torder.exportGroupStatement, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function exportMantisStatement(params) {
  return request(service.torder.exportMantisStatement, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function queryDiscountChart(params) {
  return request(service.torder.queryDiscountChart, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//质量报表查询
export async function queryQualityReport(params) {
  return request(service.torder.queryQualityReport, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//质量报表详情查询
export async function qualityReportNewUnit(params) {
  return request(service.torder.qualityReportNewUnit, {
    method: 'post',
    data: {
      ...params
    }
  });
}
