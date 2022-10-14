<template>
  <div>
    <slot v-if="check()" />
    <slot v-else name="exception" />
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex';
import { stringify } from 'qs';
import router from '@/router/index.js';

export default {
  name: 'Authorized',
  props: {
    authority: Array, // vip
    transAuthority: Array,
    roleAuthority: Array,
    includeMe: Array,
    noMatch: {}, // /login
    or: Boolean,
    isUserModel: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapGetters('authorized', ['checkPermissions', 'checkTransPermissions']),
    ...mapState('user', ['currentUser'])
  },
  data() {
    return {};
  },
  methods: {
    check() {
      //后续增加卡点管理员权限可能有用
      let isChecked = true;
      let isTransChecked = true;
      let isRoleChecked = true;
      let includeMe = true;

      if (this.authority) {
        isChecked = this.checkPermissions(this.authority);
      }
      if (this.transAuthority) {
        isTransChecked = this.checkTransPermissions(this.transAuthority);
      }
      if (this.roleAuthority) {
        isRoleChecked = this.checkRoles();
      }
      if (this.includeMe) {
        includeMe = this.includeMe.indexOf(this.currentUser.id) > -1;
      }

      if (this.or) {
        return isChecked && isTransChecked && (isRoleChecked || includeMe);
      }

      if (isChecked && isTransChecked && isRoleChecked && includeMe) {
        return true;
      }

      if (typeof this.noMatch === 'function') {
        return this.noMatch();
      }
      if (Object.prototype.toString.call(this.noMatch) === '[object String]') {
        return router.push({
          path:
            this.noMatch +
            '?' +
            stringify({
              ...this.$route.query,
              redirect: this.$route.query.redirect || this.$route.path
            })
        });
      }
      return false;
    },
    checkRoles() {
      let current = this.roleAuthority;
      let authority = [];
      this.currentUser.role.forEach(ele => {
        authority.push(ele.name);
      });
      if (authority.indexOf(current) >= 0) {
        return true;
      }
      if (Array.isArray(current)) {
        for (let i = 0; i < current.length; i += 1) {
          const element = current[i];
          if (authority.indexOf(element) >= 0) {
            return true;
          }
        }
      }
      return false;
    }
  },
  created() {},
  mounted() {}
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';
</style>
