import request from '@/common/request';
import service from './serviceMap';

// 根据工单编号查询计划（1）
export async function queryByworkNo(params) {
  return request(service.tplan.queryByworkNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//查询当前计划下面的所有案例状态（2）
export async function queryByworkNoStatus(params) {
  return request(service.tplan.queryByworkNoStatus, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//新增计划（3）
export async function addPlan(params) {
  return request(service.tplan.addPlan, {
    method: 'post',
    data: {
      ...params
    }
  });
}

//修改计划（4）
export async function updateByPrimaryKey(params) {
  return request(service.tplan.updateByPrimaryKey, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 查询是否是唯一
export async function selectByWorkNoByPlanNameCount(params) {
  return request(service.tplan.selectByWorkNoByPlanNameCount, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 根据工单号查询所有应用
export async function queryAppByWorkNo(params) {
  return request(service.tplan.queryAppByWorkNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}
