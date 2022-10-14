export function exportExcel(response, filename, format) {
  /* 文件格式 */
  format = format ? format : response.headers['content-type'];
  let file = response.data;

  if (!file.size) {
    file = new Blob([file], { type: format });
  }

  const reader = new FileReader();
  reader.readAsText(file, 'utf-8');
  reader.onload = () => {
    try {
      JSON.parse(reader.result);
    } catch {
      return;
    } finally {
      /* 文件名 */
      if (!filename) {
        const resFilename = response.headers['content-disposition'];
        filename = decodeURI(
          resFilename.substring(resFilename.indexOf('=') + 1)
        );
      }

      if (isIE()) {
        window.navigator.msSaveOrOpenBlob(file, filename);
      } else {
        const link = document.createElement('a');
        const URL = window.URL.createObjectURL(file);

        link.href = URL;
        link.download = filename;
        document.body.appendChild(link);

        link.click();

        window.URL.revokeObjectURL(link.href);
        document.body.removeChild(link);
        // successNotify('下载成功！');
      }
    }
  };
}
export function isIE() {
  return !!window.ActiveXObject;
}
