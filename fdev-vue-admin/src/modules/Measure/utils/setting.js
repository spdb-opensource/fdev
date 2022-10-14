import LocalStorage from '#/plugins/LocalStorage';
export function getMergeRequestPagination() {
  return LocalStorage.getItem('measure/mergeRequestPagination') || 5;
}
export function setMergeRequestPagination(pagination) {
  LocalStorage.set('measure/mergeRequestPagination', pagination);
}
export function seDetailPagination(pagination) {
  LocalStorage.set('measure/detail/pagination', pagination);
}
export function getDetailPagination() {
  return LocalStorage.getItem('measure/detail/pagination') || {};
}
export function setCodePagination(pagination) {
  LocalStorage.set('measure/code/pagination', pagination);
}
export function getCodePagination() {
  return LocalStorage.getItem('measure/code/pagination') || {};
}
