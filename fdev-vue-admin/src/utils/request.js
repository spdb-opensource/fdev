import axios from 'axios';
import Notify from '#/plugins/Notify';
import LocalStorage from '#/plugins/LocalStorage';
import store from '@/views/.storee';
import { baseUrl, errorNotify } from './utils';
let isExpirys = false;
axios.defaults.timeout = 1000 * 60 * 30;
if (process.env.NODE_ENV === 'production') {
  axios.defaults.baseURL = baseUrl;
  // TODO offline support
  // isExpirys = true;
}
// axios.defaults.baseURL = 'xxx/';

const codemsg = {
  200: '服务器成功返回请求的数据。',
  201: '新建或修改数据成功。',
  202: '一个请求已经进入后台排队（异步任务）。',
  204: '删除数据成功。',
  400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
  401: '用户没有权限（令牌、用户名、密码错误）。',
  403: '用户得到授权，但是访问是被禁止的。',
  404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
  406: '请求的格式不可得。',
  410: '请求的资源被永久删除，且不会再得到的。',
  422: '当创建一个对象时，发生一个验证错误。',
  500: '服务器发生错误，请检查服务器。',
  502: '网关错误。',
  503: '服务不可用，服务器暂时过载或维护。',
  504: '网关超时。'
};

const resolveResponse = response => {
  const respData = response.data;
  let { msg, code, data } = respData;
  data = data === null ? {} : data;

  if (code === 'AAAAAAA') {
    return data;
  }
  if (code === 'USR0011') {
    store.dispatch('login/logout');
    errorNotify('用户不存在');
  }
  if (!code && respData.byteLength !== 0) {
    return response;
  }
  return {
    ...data,
    code,
    msg,
    fdevStatus: 'error',
    originalData: data
  };
};

/**
 * Requests a URL, returning a promise.
 *
 * @param  {string} url       The URL we want to request
 * @param  {object} [option] The options we want to pass to "fetch"
 */
export default function request(url, option) {
  const options = {
    expirys: isExpirys,
    ...option
  };

  const defaultOptions = {
    credentials: 'include',
    headers: {
      Authorization: LocalStorage.getItem('fdev-vue-admin-jwt')
    }
  };
  const newOptions = {
    ...defaultOptions,
    ...options
  };
  if (option.data && option.data.ldapRegister) {
    newOptions.headers = {
      ldap: 'ldap'
    };
  }
  if (
    newOptions.method === 'POST' ||
    newOptions.method === 'PUT' ||
    newOptions.method === 'DELETE'
  ) {
    if (!(newOptions.data instanceof FormData)) {
      newOptions.headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json; charset=utf-8',
        ...newOptions.headers
      };
    } else {
      // newOptions.body is FormData
      newOptions.headers = {
        Accept: 'application/json',
        'Content-Type': 'multipart/form-data',
        ...newOptions.headers
      };
    }
  }

  return axios(url, newOptions)
    .then(response => resolveResponse(response))
    .catch(e => {
      const response = e.response;
      const { status, statusText, request } = response;
      const errortext = codemsg[status] || statusText;
      let message;
      if (status === 401) {
        message = '您已在其他地方登录,请重新登录!';
      } else if (status === 502 || status === 503) {
        message = '系统维护中，请稍后重试...';
      }
      let urlHash = window.location.hash;
      if (urlHash.split('?')[0] != '#/login' && urlHash != '#/') {
        message =
          response.data && response.data.message
            ? response.data.message
            : `请求错误 ${status}: ${request.responseURL}`;
        Notify.create({
          message,
          color: 'negative',
          icon: 'wifi',
          position: 'top-right'
        });
      }

      if (status === 401) {
        store.dispatch('login/logout');
      }

      const error = new Error(errortext);
      error.name = status;
      error.response = response;
      throw error;

      /* // environment should not be used
            if (status === 403) {
              router.push('/exception/403');
              return;
            }
            if (status <= 504 && status >= 500) {
              router.push('/exception/500');
              return;
            }
            if (status >= 404 && status < 422) {
              router.push('/exception/404');
            } */
    });
}
