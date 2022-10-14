<template>
  <fdev-toolbar
    class="fdev-header row items-center justify-between q-pl-llg q-pr-sm no-wrap"
  >
    <div class="row items-center no-wrap">
      <f-image name="logo" menu class="logo q-mr-lg" />
      <slot />
    </div>
    <div class="row items-center no-wrap q-gutter-x-lg">
      <NoticeIcon />
      <div class="row items-center no-wrap">
        <f-image name="man_icon" menu class="user-icon" />
        <fdev-btn-dropdown
          menu
          class="fst-menu-hover q-ml-sm"
          :label="currentUser.name"
        >
          <fdev-list>
            <!-- 个人中心 -->
            <fdev-item clickable v-close-popup to="/account/center">
              <fdev-item-section side>
                <f-icon name="preson_c_o" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>个人中心</fdev-item-label>
              </fdev-item-section>
            </fdev-item>

            <!-- 用户手册 -->
            <fdev-item
              clickable
              v-close-popup
              v-forbidMultipleClick
              @click.native="openExternalWebsite('userGuide')"
            >
              <fdev-item-section side>
                <fdev-icon name="menu_book" style="font-size:18px" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>用户手册</fdev-item-label>
              </fdev-item-section>
            </fdev-item>

            <!-- ISSUES -->
            <fdev-item
              clickable
              v-close-popup
              @click="openExternalWebsite('fdevIssue')"
            >
              <fdev-item-section side>
                <f-icon name="copy" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>ISSUES</fdev-item-label>
              </fdev-item-section>
            </fdev-item>

            <!-- 常用网址 -->
            <fdev-item clickable v-close-popup to="/website">
              <fdev-item-section side>
                <f-icon name="tools" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>常用网址</fdev-item-label>
              </fdev-item-section>
            </fdev-item>

            <!-- 团队介绍 -->
            <fdev-item clickable v-close-popup to="/team">
              <fdev-item-section side>
                <f-icon name="team" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>团队介绍</fdev-item-label>
              </fdev-item-section>
            </fdev-item>

            <!-- 查看公告 -->
            <fdev-item clickable v-close-popup to="/notices/announce">
              <fdev-item-section side>
                <f-icon name="log_c_o" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>查看公告</fdev-item-label>
              </fdev-item-section>
            </fdev-item>

            <!-- 注销 -->
            <fdev-item
              clickable
              v-close-popup
              @click.native="handleCancel"
              v-if="isImitate"
            >
              <fdev-item-section side>
                <f-icon name="turn_off" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>注销</fdev-item-label>
              </fdev-item-section>
            </fdev-item>

            <!-- 退出登录 -->
            <fdev-item clickable v-close-popup @click.native="handleLogout">
              <fdev-item-section side>
                <f-icon name="exit" />
              </fdev-item-section>
              <fdev-item-section>
                <fdev-item-label>退出登录</fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </fdev-list>
        </fdev-btn-dropdown>
      </div>
    </div>
  </fdev-toolbar>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import NoticeIcon from '@/components/NoticeIcon';
import { externalWebsite } from '@/utils/utils';
import LocalStorage from '#/plugins/LocalStorage';

export default {
  name: 'Header',
  components: {
    NoticeIcon
  },
  data() {
    return {
      isImitate: false
    };
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    })
  },
  watch: {
    'currentUser.user_name_en'(val) {
      let token = LocalStorage.getItem('fdev-vue-admin-imitate');
      this.isImitate = token ? true : false;
    }
  },
  methods: {
    ...mapActions('login', {
      logout: 'logout'
    }),
    ...mapMutations('user', ['saveCurrentUser']),
    ...mapMutations('authorized', ['reloadAuthorized']),
    async handleLogout() {
      await this.logout();
    },
    handleCancel() {
      let adminMessage = LocalStorage.getItem('admin-user');
      this.saveCurrentUser(adminMessage);
      this.reloadAuthorized(null);
      LocalStorage.set('fdev-vue-admin-jwt', adminMessage.token);
      LocalStorage.set('fdev-vue-admin-imitate', '');
      this.$router.push('/user/list');
    },
    openExternalWebsite(site) {
      window.open(externalWebsite[site]);
    }
  },
  async created() {
    let token = LocalStorage.getItem('fdev-vue-admin-imitate');
    this.isImitate = token ? true : false;
  }
};
</script>

<style lang="stylus" scoped>
.fdev-header
  height 64px
.fst-menu-btn
  min-width 0px !important
.fst-menu-hover:hover
  background rgba(0,0,0,0.10)
.logo
  width 69px
  height 31px
</style>
