import LocalStorage from '#/plugins/LocalStorage';

export function setAnnounceVisible(visibleColumns) {
  LocalStorage.set('notices/announce/visibleColumns', visibleColumns);
}

export function getAnnounceVisible() {
  return LocalStorage.getItem('notices/announce/visibleColumns');
}

export function setNewVisible(visibleColumns) {
  LocalStorage.set('notices/message/visibleColumns', visibleColumns);
}

export function getNewVisible() {
  return LocalStorage.getItem('notices/message/visibleColumns');
}

export function setOldVisible(visibleColumns) {
  LocalStorage.set('notices/message/oldVisibleColumns', visibleColumns);
}

export function getOldVisible() {
  return LocalStorage.getItem('notices/message/oldVisibleColumns');
}
