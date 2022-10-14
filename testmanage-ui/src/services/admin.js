import request from '@/common/request';
import service from './serviceMap';

//查询当前用户可审核的案例列表
export async function queryTestcaseByStatusAndUserEnName(params) {
  return request(service.tcase.queryTestcaseByStatusAndUserEnName, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//案例废弃批准
export async function updateToDiscardCase(params) {
  return request(service.tcase.updateToDiscardCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//案例通用批准
export async function updateToGeneralCase(params) {
  return request(service.tcase.updateToGeneralCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//待生效案例同意生效
export async function agreeEffect(params) {
  return request(service.tcase.agreeEffect, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//待生效案例拒绝生效
export async function rejectEffect(params) {
  return request(service.tcase.rejectEffect, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//案例同意通过
export async function agreeThrough(params) {
  return request(service.tcase.agreeThrough, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//案例拒绝通过
export async function rejectThrough(params) {
  return request(service.tcase.rejectThrough, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//查询所有系统名称
export async function queryAllSystem(params) {
  return request(service.tadmin.queryAllSystem, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//根据ID查询功能模块
export async function queryFuncMenuBySysId(params) {
  return request(service.tadmin.queryFuncMenuBySysId, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 多条件查询
export async function queryTestcaseByOption(params) {
  return request(service.tcase.queryTestcaseByOption, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function countTestcase(params) {
  return request(service.tcase.countTestcase, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function querySitMsgList(params) {
  return request(service.torder.querySitMsgList, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function countSitMsgList(params) {
  return request(service.torder.countSitMsgList, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function querySitMsgDetail(params) {
  return request(service.torder.querySitMsgDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 根据工单号查询工单详情
export async function queryOrderByNo(params) {
  return request(service.torder.queryOrderByNo, {
    method: 'post',
    data: params
  });
}
export async function batchUpdateNotiftStatus(params) {
  return request(service.tuser.batchUpdateNotiftStatus, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryRole(params) {
  return request(service.fuser.queryRole, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryUser(params) {
  return request(service.fuser.queryUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function update(params) {
  return request(service.tuser.update, {
    method: 'post',
    data: {
      ...params
    }
  });
}
