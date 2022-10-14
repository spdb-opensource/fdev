import SessionStorage from '#/plugins/SessionStorage';

export const SAVE_USER_CFG_MIXIN = {
  created() {
    this.saveUserCfg_.forEach(x => {
      let val = SessionStorage.getItem(this.$route.fullPath + '_' + x);
      val && (this[x] = val);
    });
  },
  beforeRouteLeave(to, from, next) {
    this.saveUserCfg_.forEach(x => {
      let val = [undefined, null].includes(this[x]) ? '' : this[x];
      SessionStorage.set(from.fullPath + '_' + x, val);
      next();
    });
  }
};
