import request from '@/common/request';
import service from './serviceMap';

export async function queryAssignList(params) {
  return request(service.tuser.queryAssignList, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAllOption(params) {
  return request(service.tuser.queryAllOption, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function assignUpdate(params) {
  return request(service.tuser.assignUpdate, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryDevelopGroup(params) {
  return request(service.tuser.queryDevelopGroup, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryUserCoreData(params) {
  return request(service.tuser.queryUserCoreData, {
    method: 'post',
    data: {
      ...params
    }
  });
}
