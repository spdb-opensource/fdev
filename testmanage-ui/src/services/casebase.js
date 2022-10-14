import request from '@/common/request';
import service from './serviceMap';

export async function queryTestcaseByFuncId(params) {
  return request(service.tcase.queryTestcaseByFuncId, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function exportCase(params) {
  return request(service.tcase.exportCase, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 查询案例编写人
export async function queryAllUserName(params) {
  return request(service.fuser.queryFdevUser, {
    method: 'post',
    data: {
      ...params,
      status: 0
    }
  });
}

// 案例库 查询总数
export async function countTestcaseByFuncId(params) {
  return request(service.tcase.countTestcaseByFuncId, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 案例库 设置必测、取消必测
export async function changeNecessary(params) {
  return request(service.tcase.changeNecessary, {
    method: 'post',
    data: {
      ...params
    }
  });
}
