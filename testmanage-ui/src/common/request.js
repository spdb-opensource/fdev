import axios from 'axios';
import { Notification, Message } from 'element-ui';
import hash from 'hash.js';
import router from '@/router';
let isExpirys = false;
axios.defaults.timeout = 1000 * 60 * 30;
import utils from './globalVar';
import './track';
if (process.env.NODE_ENV === 'production') {
  axios.defaults.baseURL = utils.contextPath;
  // TODO offline support
  // isExpirys = true;
}
axios.defaults.baseURL = utils.contextPath;
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

const cachedSave = (response, hashcode) => {
  /**
   * store response data in sessionStorage
   * Does not support data other than json, Cache only json
   */
  const contentType = response.headers['content-type'];
  if (contentType && contentType.match(/application\/json/i)) {
    // All data is saved as text
    // sessionStorage.set(hashcode, text);
    // sessionStorage.set(`${hashcode}:timestamp`, Date.now());
  }
  return response;
};

const resolveResponse = response => {
  // if(response.headers && response.headers.content-disposition) {
  //     return response.data;
  // } else {
  if (response.headers['content-disposition']) {
    return response;
  }
  const respData = response.data;

  let { msg, code, data } = respData;

  data = data === null ? {} : data;
  if (code === 'AAAAAAA') {
    return data;
  }
  if (code === 'COM9999') {
    router.push('./login');
    Message({
      showClose: true,
      message: msg,
      type: 'error'
    });
  } else if (code === 'COM9998') {
    sessionStorage.setItem('batchErrorMsg', msg);
    Message({
      showClose: true,
      message: '导入异常，详细请查看文本',
      type: 'error'
    });
    throw new Error(msg);
  } else {
    Message({
      showClose: true,
      message: msg,
      type: 'error'
    });
    throw new Error(msg);
  }
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
  // url = url.replace(/frelease/, 'frelease-mjy')
  /**
   * Produce fingerprints based on url and parameters
   * Maybe url has the same parameters
   */
  const fingerprint = url + (options.body ? JSON.stringify(options.body) : '');
  const hashcode = hash
    .sha256()
    .update(fingerprint)
    .digest('hex');

  const defaultOptions = {
    credentials: 'include',
    headers: {
      //userToken:"123"
      userToken: localStorage.getItem('userToken'),
      // Authorization: localStorage.getItem('fdev-vue-admin-jwt'),
      // Authorization: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWVfZW4iOiJjLXdhbmdyci0xNTk0MDE3OTkwODM0In0.vkiAglAtJ-SHE0p6rtrFqxrCGmiY-fjHC8hEoNwn7iA'
      source: 'back'
    }
  };
  const newOptions = { ...defaultOptions, ...options };
  if (
    newOptions.method === 'POST' ||
    newOptions.method === 'PUT' ||
    newOptions.method === 'DELETE'
  ) {
    if (!(newOptions.body instanceof FormData)) {
      newOptions.headers = {
        Accept: 'application/json',
        'Content-Type': 'multipart/form-data;',
        ...newOptions.headers
      };
      newOptions.body = JSON.stringify(newOptions.body);
    } else {
      // newOptions.body is FormData
      newOptions.headers = {
        Accept: 'application/json',
        ...newOptions.headers
      };
    }
  }

  const expirys = options.expirys && 60;
  // options.expirys !== false, return the cache,
  if (options.expirys === false) {
    const cached = sessionStorage.getItem(hashcode);
    const whenCached = sessionStorage.getItem(`${hashcode}:timestamp`);
    if (cached !== null && whenCached !== null) {
      const age = (Date.now() - whenCached) / 1000;
      if (age < expirys) {
        const response = new Response(new Blob([cached]));
        return response.json();
      }
      sessionStorage.remove(hashcode);
      sessionStorage.remove(`${hashcode}:timestamp`);
    }
  }
  axios.defaults.withCredentials = undefined;
  return axios(url, newOptions)
    .then(response => cachedSave(response, hashcode))
    .then(response => resolveResponse(response))
    .catch(e => {
      let response = e.response;
      if (response) {
        const { status, statusText } = response;
        const errortext = codemsg[status] || statusText;
        // let message = `请求错误  ${status}: ${request.responseURL}`;
        let message = response.data.message;
        if (status === 401) {
          message = '您已在其他地方登录,请重新登录!';
        }
        let urlHash = window.location.hash;
        if (urlHash.split('?')[0] != '#/login') {
          Notification.error({
            message: message,
            color: 'negative',
            icon: 'wifi',
            position: 'top-right'
          });
        }
        const error = new Error(errortext);
        error.name = status;
        error.response = response;
        throw error;
      } else {
        throw e;
      }
    });
}
