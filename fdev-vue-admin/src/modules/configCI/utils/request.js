import axios from 'axios';
import LocalStorage from '#/plugins/LocalStorage';
//下载流文件
export function requestStream(url, totleSize, dialog) {
  axios({
    method: 'get',
    headers: {
      Authorization: LocalStorage.getItem('fdev-vue-admin-jwt')
    },
    url, // 你自己的下载地址
    onDownloadProgress: function(progressEvent) {
      let percentage = Number(
        ((progressEvent.loaded / totleSize) * 100).toFixed(1)
      );
      let msg = percentage === 100 ? '下载完成' : `下载中... ${percentage}%`;
      dialog.update({
        message: msg
      });
    },
    responseType: 'blob' // responseType需要根据接口响应的数据类型去设置
  }).then(res => {
    let { data, headers } = res;
    const fileName = headers['content-disposition'].split('=')[1];
    let blob = new Blob([data], {
      type: 'application/octet-stream' // 下载的文件类型格式（二进制流，不知道下载文件类型可以设置为这个）, 具体请查看HTTP Content-type 对照表
    });
    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
      // IE下对blob的兼容处理
      window.navigator.msSaveOrOpenBlob(blob, fileName);
      return;
    }
    let url = URL.createObjectURL(blob);
    let a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.setAttribute('download', fileName); // 设置下载的文件名
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a); //下载完成移除dom元素
    URL.revokeObjectURL(url); //释放掉blob对象
  });
}
