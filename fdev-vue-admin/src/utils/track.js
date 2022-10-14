import _st from 'spdb-tracking';
import store from '@/views/.storee';
import router from '@/router/index.js';
import { baseUrl } from './utils';

function getData() {
  const obj = {
    user_name: store.state.user.currentUser.name,
    menu: router.currentRoute.path,
    page: router.currentRoute.meta.name
  };
  return {
    data: obj
  };
}
_st.setPreprocessor(getData);
_st.setConfig({
  trackerUrl: `${baseUrl}ffootprint/`,
  trackingEnabled: true,
  blurTrackingEnabled: false,
  scrollTrackingEnabled: false,
  pageTrackingEnabled: false,
  changeTrackingEnabled: false,
  channel: 'fdev'
});
