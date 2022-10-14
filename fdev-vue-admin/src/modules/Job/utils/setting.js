import LocalStorage from '#/plugins/LocalStorage';

export function setCollapsible(collapsible) {
  LocalStorage.set('job/collapsible', collapsible);
}

export function getCollapsible() {
  return LocalStorage.getItem('job/collapsible');
}

export function setToggleFile(toggleFile) {
  LocalStorage.set('job/toggleFile', toggleFile);
}

export function getToggleFile() {
  return LocalStorage.getItem('job/toggleFile');
}

export function setPagination(pagination) {
  LocalStorage.set('job/pagination', pagination);
}

export function getPagination() {
  return LocalStorage.getItem('job/pagination') || {};
}
export function setVisibleColumns(list, visibleColumns) {
  LocalStorage.set(list + '/testMsgs', visibleColumns);
}

export function getVisibleColumns(list) {
  return LocalStorage.getItem(list + '/testMsgs');
}

export function setDelayTaskPagination(pagination) {
  LocalStorage.set('job/delayTask/pagination', pagination);
}

export function getDelayTaskPagination() {
  return LocalStorage.getItem('job/delayTask/pagination') || {};
}
