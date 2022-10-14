import LocalStorage from '#/plugins/LocalStorage';

// use localStorage to store the authority info, which might be sent from server in actual project.
export function getAuthority(str) {
  // return localStorage.getItem('fdev-vue-admin-authority') || ['admin', 'user'];
  const authorityString =
    typeof str === 'undefined'
      ? LocalStorage.getItem('fdev-vue-admin-authority')
      : str;
  // authorityString could be admin, "admin", ["admin"]
  let authority;
  try {
    authority = JSON.parse(authorityString);
  } catch (e) {
    authority = authorityString;
  }
  if (typeof authority === 'string') {
    return [authority];
  }
  return authority || ['guest'];
}

export function setAuthority(authority) {
  const proAuthority = typeof authority === 'string' ? [authority] : authority;
  return LocalStorage.set('fdev-vue-admin-authority', proAuthority);
}
