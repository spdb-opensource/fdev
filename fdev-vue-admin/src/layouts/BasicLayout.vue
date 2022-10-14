<template>
  <Authorized
    :authority="basicRoute.meta.authority"
    :noMatch="basicRoute.meta.noMatch"
  >
    <fdev-layout view="hHh Lpr fFf">
      <fdev-header>
        <Header>
          <template v-if="menuList.length > 0">
            <fdev-btn
              v-for="menu in menuList"
              menu
              :key="menu.menuId"
              @click="fstMenuClick(menu)"
              :class="
                `fst-menu${
                  selectFstMenuId === menu.menuId ? '--selected' : ''
                } q-mr-xs`
              "
            >
              <div class="row no-wrap items-center">
                <f-image
                  :name="menu.nameEn"
                  menu
                  :style="
                    menu.nameEn === 'flowAndTool'
                      ? 'width:18px;height:18px;margin:5px'
                      : ''
                  "
                />
                <span class="text-no-wrap">{{ menu.nameCn }}</span>
              </div>
            </fdev-btn>
          </template>
        </Header>
      </fdev-header>

      <fdev-drawer v-model="isOpen" :width="226" :mini="miniState">
        <div class="row no-wrap fit">
          <SiderMenu
            class="q-mini-drawer-hide"
            :menuData="userMenus"
            :check="checkPermissions"
            :checkRole="checkRole"
            :checkAdmin="checkAdmin"
          />
          <div
            @mouseenter="ishover = true"
            @mouseleave="ishover = false"
            @click="updateMiniState(!miniState)"
            class="cursor-pointer full-height side-menu-icon-col row items-center"
          >
            <f-image :name="sideIcon" menu class="side-icon" />
          </div>
        </div>
      </fdev-drawer>

      <fdev-page-container class="page-style" :class="!isOpen ? 'q-pl-md' : ''">
        <fdev-page class="page" :class="!isOpen ? 'q-pt-md' : ''">
          <div class="row items-center no-wrap page-head" v-if="isOpen">
            <fdev-btn
              ficon="exit"
              @click="goBack"
              flat
              label="返回上一级"
              class="back-btn q-mr-lg"
            />
            <fdev-breadcrumbs
              class="text-primary full-width"
              active-color="grey-4"
            >
              <template v-slot:separator>
                <f-icon
                  :width="14"
                  :height="14"
                  name="arrow_r_o"
                  class="text-grey-3"
                />
              </template>
              <fdev-breadcrumbs-el
                v-for="(breadCrumb, i) in breadCrumbList"
                :key="i"
                :to="breadCrumb.path"
                :label="breadCrumb.nameCn"
              />
            </fdev-breadcrumbs>
          </div>

          <Authorized :authority="$route.meta.authority">
            <router-view />
            <template v-slot:exception>
              <Exception403 />
            </template>
          </Authorized>
        </fdev-page>
      </fdev-page-container>
    </fdev-layout>
  </Authorized>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex';
import openURL from '#/utils/open-url';
import Header from './Header';
import SiderMenu from './SiderMenu';
import Authorized from '@/components/Authorized';
import Exception403 from '@/views/Exception/403.vue';
import store from '@/views/.storee';

export default {
  name: 'BasicLayout',
  components: { Header, SiderMenu, Authorized, Exception403 },
  data() {
    return {
      // isOpen: this.$q.platform.is.desktop,
      isOpen: true,
      // miniState: false,
      ishover: false,
      breadCrumbList: [],
      selectMenu: null,
      selectSecMenu: null,
      secMenus: []
    };
  },
  computed: {
    ...mapState('menu', {
      menuList: 'menuList',
      menuData: 'menuData',
      miniState: 'miniState'
    }),
    ...mapGetters('user', {
      checkRole: 'checkRole',
      checkAdmin: 'checkAdmin'
    }),
    ...mapGetters('authorized', {
      checkPermissions: 'checkPermissions'
    }),
    basicRoute() {
      return this.$route.matched.find(route => route.name === 'app');
    },
    sideIcon() {
      return `${this.miniState ? 'unfold' : 'packup'}${
        this.ishover ? '_hover' : ''
      }`;
    },
    selectFstMenuId() {
      return this.selectMenu ? this.selectMenu.menuId : null;
    },
    selectSecMenuId() {
      return this.selectSecMenu ? this.selectSecMenu.menuId : null;
    },
    userMenus() {
      if (!this.selectMenu) return [];
      // data: 一级菜单的集合
      // list: 二级菜单的集合
      let data = [
          ...this.menuData.filter(
            menu => menu.fstMenu === this.selectMenu.nameEn
          )
        ],
        list = this.menuList.find(
          menu => menu.level === '1' && menu.nameEn === this.selectMenu.nameEn
        ).childrenList;
      let secMenus;
      if (data.length === 1) {
        data = data[0].children;
      }
      // data: 二级菜单的集合
      // list: 二级菜单的集合
      let length = data.length;
      for (let i = 0; i < length; i++) {
        const each = data[i];
        secMenus = [];
        const exist = list.some(item => {
          const l = item.childrenList;
          if (each.name === item.nameEn) {
            let d = each;
            d.nameCn = item.nameCn;
            d.sort = item.sort;
            if (!Array.isArray(l) || l.length === 0) {
              delete d.children;
              secMenus = d;
            } else {
              secMenus = this.core(l, d.children);
            }
            return true;
          }
          return false;
        });
        if (!exist) {
          data.splice(i, 1);
          length--;
          i--;
        } else {
          secMenus.length > 0 && (each.children = secMenus);
        }
      }

      return data.sort((a, b) => a.sort - b.sort);
    }
  },
  methods: {
    ...mapMutations('dashboard', ['resetChart']),
    ...mapMutations('menu', ['updateMiniState']),
    core(list, data) {
      // list: 三级菜单
      // data: 三级菜单
      data = data
        .filter(menu => {
          const target_list = list.find(l => l.nameEn === menu.name);
          if (target_list) {
            // 设置 中文名和 sort
            menu.nameCn = target_list.nameCn;
            menu.sort = target_list.sort;
            let children_data = menu.children,
              children_list = target_list.childrenList;
            if (
              Array.isArray(children_data) &&
              Array.isArray(children_list) &&
              children_data.length > 0 &&
              children_list.length > 0
            ) {
              children_data = children_data
                .filter(child_data => {
                  const child_list = children_list.find(
                    c_l => c_l.nameEn === child_data.name
                  );
                  if (child_list) {
                    // 设置 中文名和 sort
                    child_data.nameCn = child_list.nameCn;
                    child_data.sort = child_list.sort;
                    return true;
                  }
                  return false;
                })
                .sort((a, b) => a.sort - b.sort);
            }
            return true;
          }
          return false;
        })
        .sort((a, b) => a.sort - b.sort);

      return data;
    },
    openURL,
    toggleLeftDrawer() {
      if (this.$q.platform.is.desktop) {
        // this.miniState = !this.miniState;
        this.updateMiniState(!this.miniState);
        this.resetChart(this.miniState);
      } else {
        this.isOpen = !this.isOpen;
      }
    },
    goBack() {
      this.$router.back();
    },
    searchRealRoute(arr, target) {
      let res;
      arr.some(menu => {
        if (target.startsWith(menu.path)) {
          if (target === menu.path) {
            res = menu;
            return true;
          }
          if (menu.children) {
            res = this.searchRealRoute(menu.children, target);
            return true;
          }
          return false;
        }
        return false;
      });
      return res;
    },
    convert2BreadcrumbList(route) {
      this.$nextTick(() => {
        const breadCrumbList = [];
        let fstMenuName, secMenuName, fstMenuObj;
        route.matched.forEach(matched => {
          // 将面包屑展示的中文名替换为后端配的真实路由的中文名
          if (matched.path !== '' && !!matched.meta.fstMenu) {
            !fstMenuName && (fstMenuName = matched.meta.fstMenu);
            secMenuName = matched.meta.secMenu;
            // 纯二级菜单读取接口数据里的中文名即可
            if (!secMenuName) {
              matched.meta.nameCn = this.menuList.find(
                m => m.nameEn === fstMenuName
              ).nameCn;
            } else {
              // 非二级菜单递归获取真实路由的中文名
              fstMenuObj = this.userMenus.find(
                menu =>
                  fstMenuName === menu.fstMenu && secMenuName === menu.secMenu
              );
              // 若 fstMenuObj 不存在，matched 里的 nameCn 不需要改，直接用本地的中文名
              if (fstMenuObj) {
                // 路径若完全匹配上，直接设置中文名
                if (matched.path === fstMenuObj.path) {
                  matched.meta.nameCn = fstMenuObj.nameCn;
                } else {
                  // 没匹配上则递归获取中文名
                  const res = this.searchRealRoute(
                    fstMenuObj.children,
                    matched.path
                  );
                  res !== void 0 && (matched.meta.nameCn = res.nameCn);
                }
              }
            }

            const keys = Object.keys(route.params);
            let { path, name } = matched;
            if (keys.length > 0) {
              keys.forEach(key => {
                const reg = new RegExp(':' + key);
                path = path.replace(reg, route.params[key]);
              });
            }
            breadCrumbList.push({
              name,
              path,
              ...matched.meta
            });
          }
        });
        this.breadCrumbList = breadCrumbList.filter(
          v => v.path && !v.hideInBreadcrumb
        );
      });

      const { fstMenu, secMenu } = route.meta;
      if (!fstMenu) {
        this.selectMenu = null;
        this.secMenus = null;
        this.selectSecMenu = null;
        this.isOpen = false;
        return;
      }
      !this.isOpen && (this.isOpen = true);
      this.selectMenu = this.menuList.find(menu => menu.nameEn === fstMenu);
      const { childrenList } = this.selectMenu;
      this.secMenus =
        Array.isArray(childrenList) && childrenList.length > 0
          ? childrenList
          : null;
      this.selectSecMenu = this.secMenus
        ? this.secMenus.find(menu => menu.nameEn === secMenu)
        : null;
    },
    fstMenuClick(menu) {
      const { path } = menu;
      let childrenList = menu.childrenList;
      if (Array.isArray(childrenList) && childrenList.length > 0) {
        while (childrenList[0].childrenList.length > 0) {
          childrenList = childrenList[0].childrenList;
        }
        this.$router.push(childrenList[0].path);
      } else {
        menu.nameEn === 'toolNavigation'
          ? window.open('xxx/eff-cli-tools/#/')
          : this.$router.push(path);
      }
    },
    async updateMenu() {
      await this.$store.dispatch('menu/queryMenu');
      await this.$store.dispatch('menu/initMenu', null, { root: true });
      this.convert2BreadcrumbList(this.$route);
    }
  },
  async created() {
    this.$root.$on('updateMenu', this.updateMenu);
    this.$root.$on('toggleLeftDrawer', this.toggleLeftDrawer);
    await this.updateMenu();
    this.$watch('$route', route => {
      this.convert2BreadcrumbList(route);
    });
  },
  async beforeRouteEnter(to, from, next) {
    await store.dispatch('user/fetchCurrent');
    next();
  }
};
</script>

<style lang="stylus" scoped>
// .layout
//   min-width 1440px
.page-head
  height 54px
.side-icon
  width 12px
  height 37px
.page-style
  background #f4f6fd
.side-menu-icon-col
  background #f4f6fd
  width 16px
  border-left 2px solid #E1E1E1
.side-menu-icon-col:hover
  border-left 2px solid #0663BE
.page
  margin-right 16px
  padding-bottom 16px
.back-btn
  color #666 !important
  &:hover
    color #0663BE !important
>>>
  .q-breadcrumbs .q-breadcrumbs__el:hover
    color #0663BE
.fst-menu
  &:hover
    background rgba(0,0,0,0.10)
  &--selected
    background #0378EA
</style>
