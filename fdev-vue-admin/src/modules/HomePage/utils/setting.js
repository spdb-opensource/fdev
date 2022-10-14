import LocalStorage from '#/plugins/LocalStorage';

export function setPagination(pagination) {
  LocalStorage.set('user/account/pagination', pagination);
}

export function getPagination() {
  return LocalStorage.getItem('user/account/pagination') || {};
}

export function setProductionPagination(pagination) {
  LocalStorage.set('user/account/productionPagination', pagination);
}

export function getProductionPagination() {
  return LocalStorage.getItem('user/account/productionPagination') || {};
}

export function setTableCol(list, visibleColumns) {
  LocalStorage.set(list + '/tableCol', visibleColumns);
}

export function getTableCol(list) {
  return LocalStorage.getItem(list + '/tableCol');
}

export function getWeblistVisible() {
  return LocalStorage.getItem('frame/weblist');
}
export function setWeblistVisible(columns) {
  return LocalStorage.set('frame/weblist', columns);
}
export function getWebPagination() {
  return LocalStorage.getItem('frame/webPagination') || {};
}
export function setWebPagination(pagination) {
  return LocalStorage.set('frame/webPagination', pagination);
}

export function setArchetypePagination(pagination) {
  return LocalStorage.set('frame/pagination', pagination);
}
export function getArchetypePagination() {
  return LocalStorage.getItem('frame/pagination') || {};
}
export function setVisibleColumns(visibleColumns) {
  LocalStorage.set('component/web/archetype/visibleColumns', visibleColumns);
}
export function getVisibleColumns() {
  return LocalStorage.getItem('component/web/archetype/visibleColumns');
}

export function setMyApprovePagination(pagination) {
  LocalStorage.set('HomePage/findMyApproveList', pagination);
}

export function getMyApprovePagination() {
  return LocalStorage.getItem('HomePage/findMyApproveList') || 10;
}
