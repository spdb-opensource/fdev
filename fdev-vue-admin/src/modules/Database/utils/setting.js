import LocalStorage from '#/plugins/LocalStorage';

export function setPagination(pagination) {
  LocalStorage.set('database/pagination', pagination);
}
export function getPagination() {
  return LocalStorage.getItem('database/pagination') || { rowsPerPage: 5 };
}
export function setAddPagination(pagination) {
  LocalStorage.set('databaseadd/pagination', pagination);
}
export function getAddPagination() {
  return LocalStorage.getItem('databaseadd/pagination') || { rowsPerPage: 5 };
}
