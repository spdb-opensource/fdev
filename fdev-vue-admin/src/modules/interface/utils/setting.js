import LocalStorage from '#/plugins/LocalStorage';

export function setPagination(pagination) {
  LocalStorage.set('interfaceScan/pagination', pagination);
}
export function setApplyPagination(pagination) {
  LocalStorage.set('interfaceApply/pagination', pagination);
}
export function getPagination() {
  return LocalStorage.getItem('interfaceScan/pagination') || {};
}
export function getApplyPagination() {
  return LocalStorage.getItem('interfaceApply/pagination') || {};
}
export function setInvokePagination(pagination) {
  LocalStorage.set('invokeRelationship/pagination', pagination);
}

export function getInvokePagination() {
  return LocalStorage.getItem('invokeRelationship/pagination') || {};
}

export function getStatisticsPagination() {
  return LocalStorage.getItem('interfaceStatistics/pagination') || {};
}

export function setStatisticsPagination(pagination) {
  LocalStorage.set('interfaceStatistics/pagination', pagination);
}

export function getYapiPagination() {
  return LocalStorage.getItem('yapiPagination/pagination') || {};
}

export function setYapiPagination(pagination) {
  LocalStorage.set('yapiPagination/pagination', pagination);
}

export function getYapiInterfacePagination() {
  return LocalStorage.getItem('yapiInterfacePagination/pagination') || {};
}

export function setYapiInterfacePagination(pagination) {
  LocalStorage.set('yapiInterfacePagination/pagination', pagination);
}
