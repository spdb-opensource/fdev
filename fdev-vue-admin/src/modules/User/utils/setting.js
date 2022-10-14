import LocalStorage from '#/plugins/LocalStorage';

export function setEditOpened(editOpened) {
  LocalStorage.set('user/editOpened', editOpened);
}

export function getEditOpened() {
  return LocalStorage.getItem('user/editOpened');
}

export function setPagination(pagination) {
  LocalStorage.set('user/pagination', pagination);
}

export function getPagination() {
  return LocalStorage.getItem('user/pagination') || {};
}

export function setSplitter(splitter) {
  LocalStorage.set('user/splitter', splitter);
}

export function getSplitter() {
  return LocalStorage.getItem('user/splitter');
}
