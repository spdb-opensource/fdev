import request from '@/common/request';
import service from './serviceMap';
export async function Login(params) {
  return request(service.tuser.Login, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function logout(params) {
  return request(service.tuser.logout, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function OauthGetToken(params) {
  return request(service.tuser.OauthGetToken, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function oauthGetAuthorization(params) {
  return request(service.tuser.oauthGetAuthorization, {
    method: 'post',
    data: {
      ...params
    }
  });
}
