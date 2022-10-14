import request from '@/common/request';
import service from './serviceMap';
//根据工单号查询所有相关联系人和计划信息
export async function queryRelativePeople(params) {
  return request(service.tcase.queryRelativePeople, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//案例详细信息
export async function QueryDetailByTestcaseNo(params) {
  return request(service.tcase.QueryDetailByTestcaseNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//根据上级显示下级功能菜单
export async function queryMenuBySysIdAndLever(params) {
  return request(service.tadmin.queryMenuBySysIdAndLever, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//弹框加载系统名称
export async function QueryAllSystem(params) {
  return request(service.tadmin.QueryAllSystem, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//计划下案例总数
export async function QueryTestCount(params) {
  return request(service.tcase.QueryTestCount, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//新增案例信息
export async function AddTestcase(params) {
  return request(service.tcase.AddTestcase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//编辑案例信息
export async function UpdateTestcaseByTestcaseNo(params) {
  return request(service.tcase.UpdateTestcaseByTestcaseNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//复制案例信息
export async function CopyTestcaseToOtherPlan(params) {
  return request(service.tcase.CopyTestcaseToOtherPlan, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//案例提交审核
export async function UpdateTestcaseByStatusWaitPass(params) {
  return request(service.tcase.UpdateTestcaseByStatusWaitPass, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//案例提交生效
export async function UpdateTestcaseByStatusWaitEffect(params) {
  return request(service.tcase.UpdateTestcaseByStatusWaitEffect, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//删除案例
export async function DeleteTestcaseByTestcaseNo(params) {
  return request(service.tcase.DeleteTestcaseByTestcaseNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//根据计划查询案例
export async function QueryTestcaseByPlanId(params) {
  return request(service.tcase.QueryTestcaseByPlanId, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 根据计划查询案例下面的案例状态信息 */
export async function queryPlanAllStatus(params) {
  return request(service.tcase.queryPlanAllStatus, {
    method: 'post',
    data: {
      ...params
    }
  });
}

/* ASP 多条件查询案例 */
export async function ASPQueryTestCase(params) {
  return request(service.tcase.ASPQueryTestCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

/* 批量删除 */
export async function delBatchRelationCase(params) {
  return request(service.tcase.delBatchRelationCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

/* 批量提交审批 */
export async function batchCommitAudit(params) {
  return request(service.tcase.batchCommitAudit, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 批量生效 */
export async function batchEffectAudit(params) {
  return request(service.tcase.batchEffectAudit, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 批量执行 */
export async function batchExecuteTestcase(params) {
  return request(service.tcase.batchExecuteTestcase, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 批量复制 */
export async function batchCopyTestcaseToOtherPlan(params) {
  return request(service.tcase.batchCopyTestcaseToOtherPlan, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 批量复用 */
export async function batchRepeatedRelation(params) {
  return request(service.tcase.batchRepeatedRelation, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 根据功能菜单id查询案例 */
export async function queryTestcaseByFuncId(params) {
  return request(service.tcase.queryTestcaseByFuncId, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 复用页面单个复用 updateToGeneralCase
export async function repeatedTestcaseForPlans(params) {
  return request(service.tcase.repeatedTestcaseForPlans, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function exportExcelTestcase(params) {
  return request(service.tcase.exportExcelTestcase, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 查询所有案例,不用分页
export async function queryAllTestcaseByPlanId(params) {
  return request(service.tcase.queryAllTestcaseByPlanId, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//  下载模版
export async function downloadTemplate(params) {
  return request(service.tcase.downloadTemplate, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 导入案例
export async function batchAdd(params) {
  return request(service.tcase.batchAdd, {
    method: 'POST',
    data: params
  });
}
