import request from '@/utils/request';
import service from './serviceMap';

export async function queryReleaseNodeByJob(params) {
  return request(service.frelease.queryDetailByTaskId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryReleaseNode(params) {
  return request(service.frelease.queryReleaseNodes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function update(params) {
  return request(service.fuser.productionUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addJobReleaseNode(params) {
  return request(service.frelease.taskAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
