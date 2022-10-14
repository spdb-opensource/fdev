import LocalStorage from '#/plugins/LocalStorage';

export function setPagination(pagination) {
  LocalStorage.set('environment/model_env/pagination', pagination);
}

export function getPagination() {
  return LocalStorage.getItem('environment/model_env/pagination') || {};
}

export function setModelMessagePagination(pagination) {
  LocalStorage.set('environment/modelMessage/pagination', pagination);
}

export function getModelMessagePagination() {
  return LocalStorage.getItem('environment/modelMessage/pagination') || 5;
}

export function getDeployMessagePagination() {
  return LocalStorage.getItem('environment/deployMessage/pagination') || {};
}

export function setDeployMessagePagination(pagination) {
  LocalStorage.set('environment/deployMessage/pagination', pagination);
}

export function setVisibleColumns(visibleColumns) {
  LocalStorage.set('envModel/modelEvnList/visibleColumns', visibleColumns);
}

export function getVisibleColumns() {
  return LocalStorage.getItem('envModel/modelEvnList/visibleColumns');
}
