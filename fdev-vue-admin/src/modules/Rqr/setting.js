import LocalStorage from '#/plugins/LocalStorage';

export function setPagination(pagination) {
  LocalStorage.set('rqr/pagination', pagination);
}

export function getPagination(pagination) {
  LocalStorage.getItem('rqr/pagination', pagination);
}

export function getUnitNoPagination() {
  return LocalStorage.getItem('rqr/UnitNo/pagination') || {};
}
export function setUnitNoPagination(pagination) {
  LocalStorage.set('rqr/UnitNo/pagination', pagination);
}

export function getUnitListPagination() {
  return LocalStorage.getItem('rqr/UnitList/pagination') || 10;
}
export function setUnitListPagination(pagination) {
  LocalStorage.set('rqr/UnitList/pagination', pagination);
}
// 加文件
export function getOtherDTPagination() {
  return LocalStorage.getItem('rqr/OtherDmTask/pagination') || {};
}
export function setOtherDTPagination(pagination) {
  LocalStorage.set('rqr/OtherDmTask/pagination', pagination);
}

export function getTaskPagination() {
  return LocalStorage.getItem('rqr/Task/pagination') || {};
}
export function setTaskPagination(pagination) {
  LocalStorage.set('rqr/Task/pagination', pagination);
}

// export function getTabPagination() {
//   return LocalStorage.getItem('frame/tab/pagination') || {};
// }
// export function setProductionPagination(pagination) {
//   LocalStorage.set('user/account/productionPagination', pagination);
// }

// export function getProductionPagination() {
//   return LocalStorage.getItem('user/account/productionPagination') || {};
// }

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

export function setDemandPagination(pagination) {
  LocalStorage.set('rqr/findDemandList', pagination);
}

export function getDemandleagination() {
  return LocalStorage.getItem('rqr/findDemandList') || 10;
}

export function setEvaPagination(pagination) {
  LocalStorage.set('rqr/findEvaList', pagination);
}

export function getEvapagination() {
  return LocalStorage.getItem('rqr/findEvaList') || 10;
}
export function setApprovePagination(pagination) {
  LocalStorage.set('rqr/findApproveList', pagination);
}

export function getApproveleagination() {
  return LocalStorage.getItem('rqr/findApproveList') || 10;
}
