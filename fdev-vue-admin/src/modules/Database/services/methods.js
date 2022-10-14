import request from '@/utils/request.js';
import service from './api.js';

// 根据用户英文名查询应用信息
export async function queryAppByUser(params) {
  return request(service.fdatabase.queryAppByUser, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 导入模版下载
export async function downloadTemplate(params) {
  return request(service.fdatabase.downloadTemplate, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 数据字典字段类型查询
export async function queryFieldType(params) {
  return request(service.fdatabase.queryFieldType, {
    method: 'POST',
    data: params
  });
}

// 数据字典信息批量导入
export async function impDictRecords(params) {
  return request(service.fdatabase.impDictRecords, {
    method: 'POST',
    data: params
  });
}

// 数据字典信息新增、克隆
export async function addDictRecord(params) {
  return request(service.fdatabase.addDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}

// 数据字典信息修改
export async function updateDictRecord(params) {
  return request(service.fdatabase.updateDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}

// 数据字典信息查询(分页)
export async function queryDictRecordAll(params) {
  return request(service.fdatabase.queryDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}

// 数据字典信息查询(分页)
export async function queryDictRecord(params) {
  return request(service.fdatabase.queryDictRecord, {
    method: 'POST',
    data: { ...params }
  });
}

// 数据字典登记表信息查询（分页）
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

// 应用与数据库详情查询
export async function databaseQueryDetail(params) {
  return request(service.fdatabase.databaseQueryDetail, {
    method: 'POST',
    data: { ...params }
  });
}

// 应用负责人查询
export async function queryManager(params) {
  return request(service.fdatabase.queryManager, {
    method: 'POST',
    data: { ...params }
  });
}

// 表名查询
export async function queryName(params) {
  return request(service.fdatabase.queryName, {
    method: 'POST',
    data: { ...params }
  });
}

// 数据库类型查询接口
export async function queryDbType(params) {
  return request(service.fdatabase.queryDbType, {
    method: 'POST',
    data: { ...params }
  });
}

// 应用信息查询
export async function queryAppName(params) {
  return request(service.fdatabase.queryAppName, {
    method: 'POST',
    data: { ...params }
  });
}

// 应用库表信息新增
export async function add(params) {
  return request(service.fdatabase.add, {
    method: 'POST',
    data: { ...params }
  });
}

// 应用库表信息修改
export async function update(params) {
  return request(service.fdatabase.update, {
    method: 'POST',
    data: { ...params }
  });
}

// 应用库表信息删除
export async function deleteDatabase(params) {
  return request(service.fdatabase.deleteDatabase, {
    method: 'POST',
    data: params
  });
}

// 数据库信息查询（分页）
export async function queryInfo(params) {
  return request(service.fdatabase.queryInfo, {
    method: 'POST',
    data: { ...params }
  });
}

// 库名查询
export async function queryDbName(params) {
  return request(service.fdatabase.queryDbName, {
    method: 'POST',
    data: { ...params }
  });
}

// 导出 应用与库表关系信息
export async function download(params) {
  return request(service.fdatabase.download, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: { ...params }
  });
}

// schema文件上传
export async function upload(params) {
  return request(service.fdatabase.upload, {
    method: 'POST',
    data: params
  });
}

// 应用与数据库关系确认
export async function confirmAll(params) {
  return request(service.fdatabase.confirmAll, {
    method: 'POST',
    data: params
  });
}

// 数据字典登记表信息生成
export async function addUseRecord(params) {
  return request(service.fdatabase.addUseRecord, {
    method: 'POST',
    data: { ...params }
  });
}
