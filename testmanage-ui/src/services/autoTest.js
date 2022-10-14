import request from '@/common/request';
import service from './serviceMap';

export async function queryMenu(params) {
  return request(service.tauto.queryMenu, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addMenu(params) {
  return request(service.tauto.addMenu, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteMenu(params) {
  return request(service.tauto.deleteMenu, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateMenu(params) {
  return request(service.tauto.updateMenu, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addCase(params) {
  return request(service.tauto.addCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryCase(params) {
  return request(service.tauto.queryCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteCase(params) {
  return request(service.tauto.deleteCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateCase(params) {
  return request(service.tauto.updateCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function genFile(params) {
  return request(service.tauto.genFile, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function excuteCase(params) {
  return request(service.tauto.excuteCase, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addCaseDetail(params) {
  return request(service.tauto.addCaseDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryCaseDetail(params) {
  return request(service.tauto.queryCaseDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteCaseDetail(params) {
  return request(service.tauto.deleteCaseDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateCaseDetail(params) {
  return request(service.tauto.updateCaseDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addData(params) {
  return request(service.tauto.addData, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteData(params) {
  return request(service.tauto.deleteData, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateData(params) {
  return request(service.tauto.updateData, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryData(params) {
  return request(service.tauto.queryData, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addAssert(params) {
  return request(service.tauto.addAssert, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteAssert(params) {
  return request(service.tauto.deleteAssert, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateAssert(params) {
  return request(service.tauto.updateAssert, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryAssert(params) {
  return request(service.tauto.queryAssert, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addModule(params) {
  return request(service.tauto.addModule, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteModule(params) {
  return request(service.tauto.deleteModule, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateModule(params) {
  return request(service.tauto.updateModule, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryModule(params) {
  return request(service.tauto.queryModule, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addUser(params) {
  return request(service.tauto.addUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteUser(params) {
  return request(service.tauto.deleteUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateUser(params) {
  return request(service.tauto.updateUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryUser(params) {
  return request(service.tauto.queryUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addElementDic(params) {
  return request(service.tauto.addElementDic, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteElementDic(params) {
  return request(service.tauto.deleteElementDic, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateElementDic(params) {
  return request(service.tauto.updateElementDic, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryElementDic(params) {
  return request(service.tauto.queryElementDic, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addElement(params) {
  return request(service.tauto.addElement, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteElement(params) {
  return request(service.tauto.deleteElement, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateElement(params) {
  return request(service.tauto.updateElement, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryElement(params) {
  return request(service.tauto.queryElement, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function addModuleDetail(params) {
  return request(service.tauto.addModuleDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteModuleDetail(params) {
  return request(service.tauto.deleteModuleDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function updateModuleDetail(params) {
  return request(service.tauto.updateModuleDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryModuleDetail(params) {
  return request(service.tauto.queryModuleDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryCaseDetailByTestCaseNo(params) {
  return request(service.tauto.queryCaseDetailByTestCaseNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryModuleDetailByModuleId(params) {
  return request(service.tauto.queryModuleDetailByModuleId, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryDataByTestCaseNo(params) {
  return request(service.tauto.queryDataByTestCaseNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryAssertByTestCaseNo(params) {
  return request(service.tauto.queryAssertByTestCaseNo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryDataByModuleId(params) {
  return request(service.tauto.queryDataByModuleId, {
    method: 'post',
    data: {
      ...params
    }
  });
}
