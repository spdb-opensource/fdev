import LocalStorage from '#/plugins/LocalStorage';

export function setPagination(pagination) {
  LocalStorage.set('defect/pagination', pagination);
}

export function getPagination() {
  return LocalStorage.getItem('defect/pagination') || {};
}

export function setTableCol(list, visibleColumns) {
  LocalStorage.set(list + '/tableCol', visibleColumns);
}

export function getTableCol(list) {
  return LocalStorage.getItem(list + '/tableCol');
}
