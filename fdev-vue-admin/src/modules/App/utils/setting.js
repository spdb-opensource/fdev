import LocalStorage from '#/plugins/LocalStorage';

export function setPagination(pagination) {
  LocalStorage.set('app/pagination', pagination);
}

export function getPagination() {
  return LocalStorage.getItem('app/pagination') || {};
}

export function setCIPagination(pagination) {
  LocalStorage.set('app/ci/pagination', pagination);
}

export function getCIPagination() {
  return LocalStorage.getItem('app/ci/pagination') || {};
}
