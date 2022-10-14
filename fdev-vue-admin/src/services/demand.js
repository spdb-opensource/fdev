import request from '@/utils/request';
import service from './serviceMap';
export async function queryDemandFile(params) {
  return request(service.fdemand.queryDemandFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryByFdevNoAndDemandId(params) {
  return request(service.fdemand.queryByFdevNoAndDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function save(params) {
  return request(service.fdemand.save, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryDemandList(params) {
  return request(service.fdemand.queryDemandList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//更新
export async function update(params) {
  return request(service.fdemand.update, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//撤销
export async function deleteRqr(params) {
  return request(service.fdemand.deleteRqr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryPaginationByDemandId(params) {
  return request(service.fdemand.queryPaginationByDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDemandInfoDetail(params) {
  return request(service.fdemand.queryDemandInfoDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//归档
export async function fileRqr(params) {
  return request(service.fdemand.fileRqr, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteUnitById(params) {
  return request(service.fdemand.deleteUnitById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDemandDoc(params) {
  return request(service.fdemand.queryDemandDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function search(params) {
  return request(service.fdemand.search, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addImplementUnit(params) {
  return request(service.fdemand.addImplementUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function supplyImplementUnit(params) {
  return request(service.fdemand.supplyImplementUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateImplementUnit(params) {
  return request(service.fdemand.updateImplementUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryByGroupId(params) {
  return request(service.fdemand.queryByGroupId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryUnitByIpmpTaskId(params) {
  return request(service.fdemand.queryUnitByIpmpTaskId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addIpmpTask(params) {
  return request(service.fdemand.addIpmpTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addUnit(params) {
  return request(service.fdemand.addUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 下载文件
export async function exportExcelData(params) {
  return request(service.fdocmanage.exportExcelData, {
    method: 'GET',
    responseType: 'arraybuffer',
    params: {
      ...params
    }
  });
}

export async function updateDemandDoc(params) {
  return request(service.fdemand.updateDemandDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function defer(params) {
  return request(service.fdemand.defer, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function exportAssessExcel(params) {
  return request(service.fdemand.exportAssessExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function recover(params) {
  return request(service.fdemand.recover, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//任务新增查询实施单元
export async function queryAvailableIpmpUnit(params) {
  return request(service.fdemand.queryAvailableIpmpUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function exportDemandsExcel(params) {
  return request(service.fdemand.exportDemandsExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function exportModelExcel(params) {
  return request(service.fdemand.exportModelExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

export async function assess(params) {
  return request(service.fdemand.assess, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteDemandDoc(params) {
  return request(service.fdemand.deleteDemandDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryImplByGroupAndDemandId(params) {
  return request(service.fdemand.queryImplByGroupAndDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateByRecover(params) {
  return request(service.fdemand.updateByRecover, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteById(params) {
  return request(service.fdemand.deleteById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function getDesignInfo(params) {
  return request(service.fdemand.getDesignInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateDesignStage(params) {
  return request(service.fdemand.updateDesignStage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateDesignRemark(params) {
  return request(service.fdemand.updateDesignRemark, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function importDemandExcel(params) {
  return request(service.fdemand.importDemandExcel, {
    method: 'POST',
    params: {
      params
    }
  });
}

export async function queryDemandByOaContactNo(params) {
  return request(service.fdemand.queryDemandByOaContactNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateImpl(params) {
  return request(service.fdemand.updateImpl, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryPartInfo(params) {
  return request(service.fdemand.queryPartInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询实施单元
export function queryIpmpUnitByDemandId(params) {
  return request(service.fdemand.queryIpmpUnitByDemandId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据实施单元id查看实施单元详情
export async function queryIpmpUnitById(params) {
  return request(service.fdemand.queryIpmpUnitById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//修改实施单元
export async function updateIpmpUnit(params) {
  return request(service.fdemand.updateIpmpUnit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询项目和任务集信息
export async function queryIpmpProject(params) {
  return request(service.fdemand.queryIpmpProject, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询牵头单位和团队信息
export async function queryIpmpLeadTeam(params) {
  return request(service.fuser.queryIpmpLeadTeam, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询ipmp人员信息
export async function queryIpmpUser(params) {
  return request(service.fuser.queryIpmpUser, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询研发单元详情
export async function queryFdevImplUnitDetail(params) {
  return request(service.fdemand.queryFdevImplUnitDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//通过实施单元编号查询任务
export async function queryTaskByIpmpUnitNo(params) {
  return request(service.fdemand.queryTaskByIpmpUnitNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//挂载
export async function mount(params) {
  return request(service.fdemand.mount, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查看实施单元下的研发单元
export async function queryPaginationByIpmpUnitNo(params) {
  return request(service.fdemand.queryPaginationByIpmpUnitNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查看研发单元下的任务
export async function queryDetailByUnitNo(params) {
  return request(service.fdemand.queryDetailByUnitNo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除研发单元
export async function deleteDevUnitById(params) {
  return request(service.fdemand.deleteById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//判断评估完成按钮是否置灰
export async function assessButton(params) {
  return request(service.fdemand.assessButton, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//获取研发单元剩余工作量
export async function getWorker(params) {
  return request(service.fdemand.getWorker, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
