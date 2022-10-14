import request from '@/utils/request.js';
import service from './api';

export const commonRequest = url => async (params = {}) => {
  const response = await request(url, {
    methods: 'POST',
    data: {
      ...params
    }
  });

  return response;
};

//查询通知
export async function queryMessage(params) {
  return request(service.fnotify.queryMessage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//更新消息未读状态
export async function updateNotifyStatus(params) {
  return request(service.fnotify.updateNotifyStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//发起公告
export async function sendAnnounce(params) {
  return request(service.fnotify.sendAnnounce, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询公告
export async function queryAnnounce(params) {
  return request(service.fnotify.queryAnnounce, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询用户未读消息
export async function queryMessageByType(params) {
  return request(service.fnotify.queryMessageByType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
