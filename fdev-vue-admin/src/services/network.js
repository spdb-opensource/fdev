import request from '@/utils/request';
import service from './serviceMap';

// 网络审核查询
export async function queryApprovalList(params) {
  return request(service.fuser.queryApprovalList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 网络审核更新状态
export async function updateApprovalStatus(params) {
  return request(service.fuser.updateApprovalStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
