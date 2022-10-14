import request from '@/common/request';
import service from './serviceMap';
export async function queryMessageRecord(params) {
  return request(service.torder.queryMessageRecord, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryMessageCountImpl(params) {
  return request(service.torder.queryMessageCountImpl, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryUserApprovalList(params) {
  return request(service.tcase.queryUserApprovalList, {
    method: 'post',
    data: {
      ...params
    }
  });
}
