import request from '@/utils/request';
import service from './serviceMap';

export async function queryAppByUser(params) {
  return request(service.fdatabase.queryAppByUser, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function downloadTemplate(params) {
  return request(service.fdatabase.downloadTemplate, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
export async function queryFieldType(params) {
  return request(service.fdatabase.queryFieldType, {
    method: 'POST',
    data: params
  });
}
export async function impDictRecords(params) {
  return request(service.fdatabase.impDictRecords, {
    method: 'POST',
    data: params
  });
}
export async function addDictRecord(params) {
  return request(service.fdatabase.addDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}
export async function updateDictRecord(params) {
  return request(service.fdatabase.updateDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}
export async function queryDictRecordAll(params) {
  return request(service.fdatabase.queryDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}
export async function queryDictRecord(params) {
  return request(service.fdatabase.queryDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}
export async function queryUseRecord(params) {
  return request(service.fdatabase.queryUseRecord, {
    method: 'POST',
    data: { ...params }
  });
}
// 数据字典登记信息修改
export async function updateUseRecord(params) {
  return request(service.fdatabase.updateUseRecord, {
    method: 'POST',
    data: { ...params }
  });
}
// 和下面的 接口相同
export async function queryDatabaseNameById(params) {
  return request(service.fdatabase.querySystemNames, {
    method: 'POST',
    data: { ...params }
  });
}
// 和上面的 接口相同
export async function querySystemNames(params) {
  return request(service.fdatabase.querySystemNames, {
    method: 'POST',
    data: { ...params }
  });
}
// 查询数据字典登记表的表名
export async function queryUseRecordTable(params) {
  return request(service.fdatabase.queryUseRecordTable, {
    method: 'POST',
    data: { ...params }
  });
}
export async function databaseQueryDetail(params) {
  return request(service.fdatabase.databaseQueryDetail, {
    method: 'POST',
    data: { ...params }
  });
}
export async function queryManager(params) {
  return request(service.fdatabase.queryManager, {
    method: 'POST',
    data: { ...params }
  });
}
export async function queryName(params) {
  return request(service.fdatabase.queryName, {
    method: 'POST',
    data: { ...params }
  });
}

export async function queryDbType(params) {
  return request(service.fdatabase.queryDbType, {
    method: 'POST',
    data: { ...params }
  });
}

export async function queryAppName(params) {
  return request(service.fdatabase.queryAppName, {
    method: 'POST',
    data: { ...params }
  });
}

export async function add(params) {
  return request(service.fdatabase.add, {
    method: 'POST',
    data: { ...params }
  });
}

export async function update(params) {
  return request(service.fdatabase.update, {
    method: 'POST',
    data: { ...params }
  });
}

export async function deleteDatabase(params) {
  return request(service.fdatabase.deleteDatabase, {
    method: 'POST',
    data: params
  });
}

export async function queryInfo(params) {
  return request(service.fdatabase.queryInfo, {
    method: 'POST',
    data: { ...params }
  });
}

export async function queryDbName(params) {
  return request(service.fdatabase.queryDbName, {
    method: 'POST',
    data: { ...params }
  });
}

export async function download(params) {
  return request(service.fdatabase.download, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: { ...params }
  });
}

export async function upload(params) {
  return request(service.fdatabase.upload, {
    method: 'POST',
    data: params
  });
}
export async function confirmAll(params) {
  return request(service.fdatabase.confirmAll, {
    method: 'POST',
    data: params
  });
}
export async function addUseRecord(params) {
  return request(service.fdatabase.addUseRecord, {
    method: 'POST',
    data: { ...params }
  });
}
