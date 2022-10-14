import LocalStorage from '#/plugins/LocalStorage';

//kf网络开通申请
export function setPagination(pagination) {
  LocalStorage.set('network/pagination', pagination);
}

export function getPagination() {
  return LocalStorage.getItem('network/pagination') || {};
}

//kf网络关闭申请
export function setClosePagination(pagination) {
  LocalStorage.set('network/Close/pagination', pagination);
}

export function getClosePagination() {
  return LocalStorage.getItem('network/Close/pagination') || {};
}

//kf网络关闭提醒
export function setRemindPagination(pagination) {
  LocalStorage.set('network/Remind/pagination', pagination);
}

export function getRemindePagination() {
  return LocalStorage.getItem('network/Remind/pagination') || {};
}

//vdi网段迁移
export function setVDIPagination(pagination) {
  LocalStorage.set('network/VDI/pagination', pagination);
}

export function getVDIPagination() {
  return LocalStorage.getItem('network/VDI/pagination') || {};
}

//vm网段迁移
export function setVMPagination(pagination) {
  LocalStorage.set('network/VM/pagination', pagination);
}

export function getVMPagination() {
  return LocalStorage.getItem('network/VM/pagination') || {};
}

export function setTableCol(tableName, visibleColumns) {
  LocalStorage.set(tableName + '/tableCol', visibleColumns);
}

export function getTableCol(tableName) {
  return LocalStorage.getItem(tableName + '/tableCol');
}

export function setTableFilter(tableName, filterObj) {
  LocalStorage.set(tableName, filterObj);
}

export function getTableFilter(tableName) {
  return LocalStorage.getItem(tableName);
}

export function setCodeToolPagination(pagination) {
  LocalStorage.set('codeTool/listPagination', pagination);
}

export function getCodeToolEvapagination() {
  return LocalStorage.getItem('codeTool/listPagination') || 10;
}
