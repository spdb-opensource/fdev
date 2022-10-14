import LocalStorage from '#/plugins/LocalStorage';

export function setShownNotice(name_en, shownNotice) {
  LocalStorage.set(name_en + '/notice/shownNotice', shownNotice);
}
export function getShownNotice(name_en) {
  return LocalStorage.getItem(name_en + '/notice/shownNotice') || [];
}
