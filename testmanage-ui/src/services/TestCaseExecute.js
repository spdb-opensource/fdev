// 案例执行
import request from '@/common/request';
import service from './serviceMap';
/* 根据案例编号查询案例，案例展示 wrj  一 */
export async function queryByPlanIdAll(params) {
  return request(service.tcase.queryByPlanIdAll, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 根据案例编号查询案例，案例展示 wrj  二*/
export async function testCaseDetailByTestcaseNoAndplanId(params) {
  return request(service.tcase.testCaseDetailByTestcaseNoAndplanId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 根据  三*/
export async function updatePlanlistTestcaseRelation(params) {
  return request(service.tcase.updatePlanlistTestcaseRelation, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 链接mantis接口
export async function add(params) {
  return request(service.tplan.add, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 查询mantis项目接口
export async function queryMantisProjects(params) {
  return request(service.tplan.queryMantisProjects, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 查询mantis 开发人责任人
export async function queryDevelopList(params) {
  return request(service.tuser.queryDevelopList, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// plan页面点击mantis 查询能否打开提交mantis弹窗
export async function isTestcaseAddIssue(params) {
  return request(service.tplan.isTestcaseAddIssue, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 发送案例评审邮件
export async function sendCaseEmail(params) {
  return request(service.tcase.sendCaseEmail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 删除计划
export async function delPlan(params) {
  return request(service.tplan.delPlan, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 确定行拖拽案例排序
export async function testCaseCustomSort(params) {
  return request(service.tcase.testCaseCustomSort, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 废弃工单目标工单名称
export async function queryUserValidOrder(params) {
  return request(service.torder.queryUserValidOrder, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 废弃工单确认迁移
export async function movePlanOrCase(params) {
  return request(service.torder.movePlanOrCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 提交测试
export async function sendStartUatMail(params) {
  return request(service.torder.sendStartUatMail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 查找业测邮件信息
export async function queryUatMailInfo(params) {
  return request(service.torder.queryUatMailInfo, {
    method: 'post',
    data: {
      ...params
    }
  });
}
