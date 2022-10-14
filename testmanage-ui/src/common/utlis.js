import { queryAllUserName } from '@/services/casebase';
//关联文档树
export function handleDocDataRqr(docData, doc) {
  docData.forEach((item, index) => {
    item.children = doc[index].reduce((total, current, index) => {
      total.push({
        label: current.name,
        header: 'level-' + index,
        id: current.doc,
        path: current.path
      });
      return total;
    }, []);
  });
}

export function exportExcel(response, filename, format) {
  /* 文件名 */
  if (!filename) {
    const resFilename = response.headers['content-disposition'];
    if (resFilename.includes('UTF-8')) {
      filename = decodeURI(resFilename.substring(resFilename.indexOf('=') + 8));
    } else {
      filename = decodeURI(resFilename.substring(resFilename.indexOf('=') + 1));
    }
  }
  /* 文件格式 */
  format = format ? format : response.headers['content-type'];
  let file = response.data;

  if (!file.size) {
    file = new Blob([file], { type: format });
  }

  const reader = new FileReader();
  reader.readAsText(file, 'utf-8');
  reader.onload = () => {
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
    }
  };
}

export function isIE() {
  if (!!window.ActiveXObject || 'ActiveXObject' in window) {
    return true;
  } else {
    return false;
  }
}

export const baseUrl = window.location.origin + '/';
// export const baseUrl = 'xxx/';
export function dateFormat(time, formatType) {
  var date = new Date(time);
  var year = date.getFullYear();
  var month =
    date.getMonth() + 1 < 10
      ? '0' + (date.getMonth() + 1)
      : date.getMonth() + 1;
  var day = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
  if (formatType == '/') {
    return year + '/' + month + '/' + day;
  } else {
    return year + '-' + month + '-' + day;
  }
}

// fdev小组处理
export function formatGroup(group) {
  return {
    ...group,
    id: group.id,
    eName: group.name_en,
    parent: group.parent_id
  };
}

export function formatOption(obj, name) {
  name = name || 'name';
  if (Array.isArray(obj)) {
    return obj.map(each => flatOption(each, name));
  }
  return flatOption(obj, name);
}

function flatOption(obj, name) {
  if (!obj) {
    return {
      label: '',
      value: ''
    };
  }
  return {
    ...obj,
    label: obj[name],
    value: obj.id
  };
}

let groupObj = {};
let parentGroupObj = {};
export function getGroupFullName(groupList, groupId, hasParent, labelAdd) {
  let groupArr = groupList.filter(item => {
    return item.id == groupId;
  });
  groupObj = groupArr.length > 0 ? groupArr[0] : {};
  if (groupObj.parent_id) {
    let parentGroupArr = groupList.filter(item => {
      return item.id == groupObj.parent_id;
    });
    parentGroupObj = parentGroupArr.length > 0 ? parentGroupArr[0] : {};
    groupObj.newLabel = parentGroupObj
      ? parentGroupObj.label + '-' + groupObj.label
      : groupObj.label;
    if (parentGroupObj.parent_id) {
      parentGroupObj.newLabel = parentGroupObj.label + '-' + groupObj.name;
      labelAdd = labelAdd
        ? parentGroupObj.newLabel + '-' + labelAdd
        : parentGroupObj.newLabel;
      return getGroupFullName(
        groupList,
        parentGroupObj.parent_id,
        'hasParent',
        labelAdd
      );
    } else {
      groupObj.newLabel = labelAdd
        ? groupObj.newLabel + '-' + labelAdd
        : groupObj.newLabel;
      return groupObj.newLabel;
    }
  } else {
    if (!hasParent) {
      groupObj.newLabel = groupObj.name;
    } else if (labelAdd) {
      groupObj.newLabel = `${groupObj.name}-${labelAdd}`;
    }
    return groupObj.newLabel;
  }
}

export function deepClone(obj) {
  let objClone = Array.isArray(obj) ? [] : {};
  if (obj && typeof obj === 'object') {
    for (let key in obj) {
      // 判断obj子元素是否为对象， 如果是，递归复制
      if (obj.hasOwnProperty(key)) {
        objClone[key] = JSON.parse(JSON.stringify(obj[key]));
      } else {
        objClone[key] = obj[key];
      }
    }
  }
  return objClone;
}

export const stageMap = {
  0: '未分配',
  1: '开发中',
  2: 'SIT',
  3: 'UAT',
  4: '已投产',
  5: 'SIT UAT并行',
  6: 'UAT(含风险)',
  7: 'UAT提测',
  8: '无效',
  9: '分包测试',
  10: '分包测试(含风险)',
  11: '已废弃',
  12: '安全测试(内测完成)',
  13: '安全测试(含风险)',
  14: '安全测试(不涉及)'
};

export function validate(ref) {
  return new Promise((resolve, reject) => {
    ref.validate(valid => {
      if (valid) {
        resolve(valid);
      }
    });
  });
}
export function getUserRole() {
  let currentUser = JSON.parse(sessionStorage.getItem('userInfo'));
  if (
    currentUser.role.some(role => {
      return role.name === '玉衡超级管理员';
    })
  ) {
    return 50;
  }
  if (
    currentUser.role.some(role => {
      return role.name === '玉衡-测试管理员' || role.name === '玉衡-审批人员';
    })
  ) {
    return 40;
  }

  if (
    currentUser.role.some(role => {
      return role.name === '玉衡-测试组长' || role.name === '玉衡-安全测试组长';
    })
  ) {
    return 30;
  }

  if (
    currentUser.role.some(role => {
      return role.name === '测试人员';
    })
  ) {
    return 20;
  }

  return 10;
}

/**
  @param {Array} [roles]  用户角色数组类似 ['测试人员','开发人员']
  @param {Object} {userList}  用户list  默认是全体用户可不传
*/
export async function getUserListByRole(roleList, userList) {
  const allUserList = userList ? userList : await queryAllUserName();
  if (!roleList || (Array.isArray(roleList) && roleList.length === 0)) {
    return removal(allUserList);
  }
  let userFilterList = [];
  roleList.forEach(roleName => {
    userFilterList = userFilterList.concat(
      allUserList.filter(user => {
        return user.role.some(currentRole => {
          return currentRole.name === roleName;
        });
      })
    );
  });
  return removal(userFilterList);
}

// 去重
function removal(arr) {
  return arr.reduce((total, current) => {
    if (
      !total.some(item => {
        return item.id === current.id;
      }) &&
      current.user_name_en !== 'admin'
    ) {
      total.push(current);
    }
    return total;
  }, []);
}
