import LocalStorage from '#/plugins/LocalStorage';

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

export function setApiRolePagination(pagination) {
  LocalStorage.set('apiRole/listPagination', pagination);
}

export function getApiRoleEvapagination() {
  return LocalStorage.getItem('apiRole/listPagination') || 10;
}
