<!-- login -->
<template>
  <div class="wrapper">
    <form @submit.prevent="handleLogin" class="form q-py-lg">
      <f-block block class="fit column items-center">
        <div class="fdev-logo q-mb-xl">
          <img src="@/assets/images/login/fdev_logo.png" />
        </div>

        <fdev-input
          ref="userName"
          v-model="$v.userName.$model"
          placeholder="账号"
          class="item-width"
          autofocus
          :rules="[() => !$v.userName.$error || '请输入账号']"
        >
          <template v-slot:prepend>
            <img
              class="icon-margin"
              src="@/assets/images/login/icon-account.png"
            />
          </template>
        </fdev-input>

        <fdev-input
          ref="password"
          v-model="$v.password.$model"
          type="password"
          placeholder="密码"
          class="q-mb-lg item-width"
          :rules="[() => !$v.password.$error || '请输入密码']"
        >
          <template v-slot:prepend>
            <img
              class="icon-margin"
              src="@/assets/images/login/icon-password.png"
            />
          </template>
        </fdev-input>

        <fdev-btn type="submit" class="item-width q-mb-xl" label="登录" />
      </f-block>
    </form>

    <ldap-register-user
      v-if="showLdapDialog"
      :value="showLdapDialog"
      @input="changeValue"
    />
  </div>
</template>

<script>
import LdapRegisterUser from '../components/LdapRegisterUser';
import { required } from 'vuelidate/lib/validators';
import { mapMutations, mapState, mapActions } from 'vuex';
import { validate } from '@/utils/utils';
import LocalStorage from '#/plugins/LocalStorage';
import SessionStorage from '#/plugins/SessionStorage';

export default {
  name: 'Login',
  components: { LdapRegisterUser },
  data() {
    return {
      userName: '',
      password: '',
      showLdapDialog: false
    };
  },
  validations: {
    userName: {
      required
    },
    password: {
      required
    }
  },
  computed: {
    ...mapState('login', {
      ldapUserInfo: 'ldapUserInfo'
    })
  },
  methods: {
    ...mapActions('login', ['login']),
    ...mapMutations('login', ['changeLoginStatus']),
    ...mapMutations('authorized', ['reloadAuthorized']),
    async handleLogin() {
      this.$v.$touch();
      validate(this.$refs);
      if (this.$v.$invalid) {
        return;
      }
      await this.login({
        type: 'account',
        userName: this.userName,
        password: this.password
      });
      if (
        this.ldapUserInfo.is_once_login &&
        (this.ldapUserInfo.is_once_login === '4' ||
          !this.ldapUserInfo.git_token)
      ) {
        this.showLdapDialog = true;
        SessionStorage.set(
          'ldapInfo',
          JSON.stringify({
            userName: this.userName,
            password: this.password,
            is_spdb: this.ldapUserInfo.is_spdb,
            git_token: this.ldapUserInfo.git_token,
            is_once_login: this.ldapUserInfo.is_once_login
          })
        );
      }
      SessionStorage.set(
        'userInfo',
        JSON.stringify({
          userName: this.userName,
          password: this.password
        })
      );
    },
    changeValue(val) {
      this.showLdapDialog = val;
    }
  },
  created() {
    this.changeLoginStatus({
      status: false,
      currentAuthority: 'guest',
      token: null
    });
    this.reloadAuthorized(null);
    LocalStorage.set('admin-user', '');
    LocalStorage.set('fdev-vue-admin-imitate', '');
  }
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';

.wrapper
  height 100vh
  background url('../../../assets/images/login/bg_login.png') no-repeat center
  background-size cover
  &::after
    content ''
    position absolute
    top 10.2%
    left 4%
    width 240px
    height 90px
    background url('../../../assets/images/login/spdb_logo.png') no-repeat center
    background-size contain
  .form
    border-radius 3px
    width 430px
    position absolute
    top 45%
    right 10%
    transform translateY(-50%)
    .fdev-logo
      height 45px
      text-align center
      margin-top 30px
      img
        width 100px
    .item-width
      width: 350px
      .icon-margin
        margin-right 8px
        width 17.9px

@media screen and (max-width: $sizes.sm)
  .form
    width 95%
</style>
