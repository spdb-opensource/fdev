import LocalStorage from '#/plugins/LocalStorage';

// export function setProductionPagination(pagination) {
//   LocalStorage.set('dashboard/productionPagination', pagination);
// }

// export function getProductionPagination() {
//   return LocalStorage.getItem('dashboard/productionPagination') || 5;
// }

// export function getMergeRequestPagination() {
//   return LocalStorage.getItem('dashboard/mergeRequestPagination') || 5;
// }

// export function setMergeRequestPagination(pagination) {
//   LocalStorage.set('dashboard/mergeRequestPagination', pagination);
// }
export function setOptPagination(pagination) {
  LocalStorage.set('dashboard/userOptimize/pagination', pagination);
}

export function getOptPagination() {
  return LocalStorage.getItem('dashboard/userOptimize/pagination') || {};
}

export function setRqrPagination(pagination) {
  LocalStorage.set('dashboard/rqrDelay/pagination', pagination);
}

export function getRqrPagination() {
  return LocalStorage.getItem('dashboard/rqrDelay/pagination') || {};
}

export function setFramePagination(pagination) {
  LocalStorage.set('dashboard/frame/pagination', pagination);
}

export function getFramePagination() {
  return LocalStorage.getItem('dashboard/frame/pagination') || {};
}

export function setRqrTableCol(visibleColumns) {
  LocalStorage.set('dashboard/frame/rqr/visibleColumns', visibleColumns);
}

export function getRqrTableCol() {
  return LocalStorage.getItem('dashboard/frame/rqr/visibleColumns');
}
export function setCodePagination(pagination) {
  LocalStorage.set('dashboard/code/pagination', pagination);
}

export function getCodePagination() {
  return LocalStorage.getItem('dashboard/code/pagination') || {};
}

// export function seDetailPagination(pagination) {
//   LocalStorage.set('dashboard/detail/pagination', pagination);
// }

// export function getDetailPagination() {
//   return LocalStorage.getItem('dashboard/detail/pagination') || {};
// }
