import _st from 'spdb-tracking';
import router from '../router';
import utils from './globalVar';

function getData() {
  const obj = {
    user_name: sessionStorage.getItem('TuserName'),
    menu: router.currentRoute.path,
    page: router.currentRoute.meta.name
  };
  return {
    data: obj
  };
}
_st.setPreprocessor(getData);

_st.setConfig({
  trackerUrl: `${utils.contextPath}ffootprint/`,
  trackingEnabled: true,
  blurTrackingEnabled: false,
  scrollTrackingEnabled: false,
  pageTrackingEnabled: false,
  changeTrackingEnabled: false,
  channel: 'ftms'
});
