import LocalStorage from '#/plugins/LocalStorage';

export function setAutomationPagination(pagination) {
  if (!pagination.toString()) {
    pagination = 5;
  }
  LocalStorage.set('automation/pagination', pagination);
}

export function getAutomationPagination() {
  return LocalStorage.getItem('automation/pagination');
}
export function setScriptPramsPagination(pagination) {
  if (!pagination.toString()) {
    pagination = 5;
  }
  LocalStorage.set('scriptPrams/pagination', pagination);
}

export function getScriptPramsPagination() {
  return LocalStorage.getItem('scriptPrams/pagination');
}
export function setModuleTypePagination(pagination) {
  if (!pagination.toString()) {
    pagination = 5;
  }
  LocalStorage.set('moduleType/pagination', pagination);
}

export function getModuleTypePagination() {
  return LocalStorage.getItem('moduleType/pagination');
}
export function setSelector(selector) {
  LocalStorage.set('release/selector', selector);
}

export function getSelector() {
  return LocalStorage.getItem('release/selector');
}

export function getChangesPlansSelector() {
  return LocalStorage.getItem('changesPlans/selector');
}

export function setEditOpened(editOpened) {
  LocalStorage.set('release/editOpened', editOpened);
}

export function getEditOpened() {
  return LocalStorage.getItem('release/editOpened');
}

export function setPagination(pagination) {
  LocalStorage.set('release/pagination', pagination);
}

export function setPlansPagination(pagination) {
  LocalStorage.set('changesPlans/pagination', pagination);
}

export function getPlansPagination() {
  return LocalStorage.getItem('changesPlans/pagination') || {};
}

export function getPagination() {
  return LocalStorage.getItem('release/pagination') || {};
}

export function getDataBasePagination() {
  return LocalStorage.getItem('dataBase/pagination');
}

export function setDataBasePagination(pagination) {
  if (!pagination.toString()) {
    pagination = 5;
  }
  LocalStorage.set('dataBase/pagination', pagination);
}

export function setTableCol(list, visibleColumns) {
  LocalStorage.set(list + '/tableCol', visibleColumns);
}

export function getTableCol(list) {
  return LocalStorage.getItem(list + '/tableCol');
}

export function setBigReleasePagination(pagination) {
  LocalStorage.set('bigRelease/pagination', pagination);
}

export function getBigReleasePagination() {
  return LocalStorage.getItem('bigRelease/pagination');
}

export function getBigReleaseContactPagination() {
  return LocalStorage.getItem('bigReleaseContact/pagination');
}

export function setBigReleaseContactPagination(pagination) {
  LocalStorage.set('bigReleaseContact/pagination', pagination);
}

export function getBigReleaseTestPagination() {
  return LocalStorage.getItem('bigReleaseTest/pagination');
}

export function setBigReleaseTestPagination(pagination) {
  LocalStorage.set('bigReleaseTest/pagination', pagination);
}

export function getBigReleaseSecurityPagination() {
  return LocalStorage.getItem('bigReleaseSecurity/pagination');
}

export function setBigReleaseSecurityPagination(pagination) {
  LocalStorage.set('bigReleaseSecurity/pagination', pagination);
}

export function setProductionPagination(pagination) {
  LocalStorage.set('release/productionPagination', pagination);
}

export function getProductionPagination() {
  return LocalStorage.getItem('release/productionPagination') || 5;
}
