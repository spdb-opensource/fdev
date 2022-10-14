import { getGroupFullName } from '@/utils/utils';

// 将一个用户列表的中文名用逗号拼接成一个字符串
export function jointNameCn(userList) {
  if (Array.isArray(userList)) {
    return userList.map(user => user.user_name_cn).join(',');
  }
}

function formatJson(filterVal, appData, groups, dictObj, key) {
  return appData.map(row => {
    return filterVal.map(col => {
      // table特殊列特殊处理
      if (col === 'group') {
        return getGroupFullName(groups, row[col]);
      } else if (col === key) {
        let nameList = row[col].map(item => item.user_name_cn);
        return nameList.join(' ');
      } else if (dictObj && dictObj[col]) {
        return dictObj[col][row[col]].label;
      } else {
        return row[col];
      }
    });
  });
}

export function handleDownload(columns, list, groups, dictObj, key, title) {
  let tHeader = [];
  let filterVal = [];
  columns.forEach(item => {
    if (item.name !== 'btn') {
      tHeader.push(item.label);
      filterVal.push(item.name);
    }
  });
  import('@/utils/exportExcel').then(excel => {
    const data = formatJson(filterVal, list, groups, dictObj, key);
    excel.export_json_to_excel({
      header: tHeader,
      data,
      filename: title,
      bookType: 'xlsx'
    });
  });
}
