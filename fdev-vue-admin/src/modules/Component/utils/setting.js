import LocalStorage from '#/plugins/LocalStorage';

export function getPagination() {
  return LocalStorage.getItem('component/pagination') || {};
}

export function setPagination(pagination) {
  return LocalStorage.set('component/pagination', pagination);
}
export function getWebPagination() {
  return LocalStorage.getItem('component/webPagination') || {};
}

export function setWebPagination(pagination) {
  return LocalStorage.set('component/webPagination', pagination);
}

export function getUsingPagination() {
  return LocalStorage.getItem('component/usingPagination') || {};
}

export function setUsingPagination(pagination) {
  return LocalStorage.set('component/usingPagination', pagination);
}
export function getArchetypeUsingStatus() {
  return LocalStorage.getItem('component/archetypeUsingStatus') || {};
}

export function setArchetypeUsingStatus(pagination) {
  return LocalStorage.set('component/archetypeUsingStatus', pagination);
}
export function getOptimizePagination() {
  return LocalStorage.getItem('component/optimizePagination') || {};
}

export function setOptimizePagination(pagination) {
  return LocalStorage.set('component/optimizePagination', pagination);
}

export function setTab(tab) {
  return LocalStorage.set('component/tab', tab);
}

export function getTab() {
  return LocalStorage.getItem('component/tab');
}

export function setArchetypeTab(tab) {
  return LocalStorage.set('archetype/tab', tab);
}

export function getArchetypeTab() {
  return LocalStorage.getItem('archetype/tab');
}
export function getImageTypeTab() {
  return LocalStorage.getItem('image/tab');
}
export function setImagetypeTab(tab) {
  return LocalStorage.set('image/tab', tab);
}
export function getEditOpened() {
  return LocalStorage.getItem('component/editOpened');
}

export function getWeblistVisible() {
  return LocalStorage.getItem('weblist/visible');
}

export function setWeblistVisible(columns) {
  return LocalStorage.set('weblist/visible', columns);
}

export function setWebTab(tab) {
  return LocalStorage.set('component/web/tab', tab);
}

export function getWebTab() {
  return LocalStorage.getItem('component/web/tab');
}

export function getImageArchetypeUsingStatus() {
  return LocalStorage.getItem('image/ImageArchetypeUsingStatus') || {};
}

export function setImageArchetypeUsingStatus(pagination) {
  return LocalStorage.set('image/ImageArchetypeUsingStatus', pagination);
}
