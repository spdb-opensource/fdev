<template>
  <f-scrollarea class="q-pt-sm">
    <fdev-expansion-item
      v-for="menu in flatMenu"
      :label="menu.nameCn"
      :key="menu.nameCn + menu.sort"
      :default-opened="menu.opened"
      :header-class="{
        opened: menu.opened,
        'no-children': !menu.children || menu.children.length === 0
      }"
      :id="menu.id"
      group="menu"
      class="menu-list siderMenu-expansion"
      @click="secMenuClick(menu)"
      :expand-icon-class="
        !menu.children || menu.children.length === 0 ? 'no-expand' : ''
      "
    >
      <template v-slot:header>
        <fdev-item-section avatar>
          <!-- <f-icon
            :name="menu.icon"
            :width="16"
            :height="16"
            class="text-grey-3"
          /> -->
          <f-image
            menu
            :name="menu.fstMenu + '_' + menu.icon"
            class="sec-menu-icon"
          />
        </fdev-item-section>
        <fdev-item-section class="text-subtitle2">
          {{ menu.nameCn }}
        </fdev-item-section>
        <fdev-item-section side v-if="menu.updateSelf">
          <div class="row item-center">
            <fdev-badge @click.stop="clickMenuUpdateFlag(menu)">
              <fdev-icon name="autorenew" color="white" />
            </fdev-badge>
          </div>
        </fdev-item-section>
        <fdev-item-section side v-show="menu.update && !menu.updateSelf">
          <div class="row item-center">
            <fdev-badge>
              new
            </fdev-badge>
          </div>
        </fdev-item-section>
      </template>
      <div v-for="subMenu in menu.children" :key="subMenu.name">
        <fdev-item
          v-if="!subMenu.children"
          :to="subMenu.path"
          :id="subMenu.id"
          :inset-level="0.6"
          class="text-subtitle2 hover-bg"
        >
          {{ subMenu.nameCn }}
          <fdev-space />
          <fdev-badge
            v-if="subMenu.update"
            @click.prevent="clickMenuUpdateFlag(subMenu)"
            align="top"
          >
            <fdev-icon name="autorenew" color="white" />
          </fdev-badge>
        </fdev-item>
        <fdev-expansion-item
          :label="subMenu.nameCn"
          :key="subMenu.name"
          :default-opened="subMenu.opened"
          :header-inset-level="0.6"
          :id="subMenu.id"
          v-if="subMenu.children"
        >
          <template v-slot:header>
            <fdev-item-section class="text-subtitle2">
              {{ subMenu.nameCn }}
            </fdev-item-section>
            <fdev-item-section side v-if="subMenu.updateSelf">
              <div class="row item-center">
                <fdev-badge @click.stop="clickMenuUpdateFlag(subMenu)">
                  <fdev-icon name="autorenew" color="white" />
                </fdev-badge>
              </div>
            </fdev-item-section>
            <fdev-item-section
              side
              v-if="subMenu.update && !subMenu.updateSelf"
            >
              <div class="row item-center">
                <fdev-badge>
                  new
                </fdev-badge>
              </div>
            </fdev-item-section>
          </template>
          <fdev-item
            v-for="menu3 in subMenu.children"
            :key="menu3.name"
            :to="menu3.path"
            :id="menu3.id"
            :inset-level="0.9"
            class="text-subtitle2 hover-bg"
          >
            {{ menu3.nameCn }}
            <fdev-space />
            <fdev-badge
              v-if="menu3.update"
              @click.prevent="clickMenuUpdateFlag(menu3)"
              align="top"
            >
              <fdev-icon name="autorenew" color="white" />
            </fdev-badge>
          </fdev-item>
        </fdev-expansion-item>
      </div>
    </fdev-expansion-item>
  </f-scrollarea>
</template>

<script>
import axios from 'axios';
import introJs from 'intro.js';
import 'intro.js/introjs.css';
import LocalStorage from '#/plugins/LocalStorage';
import { mapState, mapGetters } from 'vuex';
import moment from 'moment';

export default {
  name: 'SiderMenu',
  components: {},
  props: {
    menuData: Array,
    check: Function,
    checkRole: Function,
    checkAdmin: Function
  },
  data() {
    return {
      envName: '',
      updateMenuList: [],
      latestUpdateTime: ''
    };
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', ['isKaDianManager']),
    flatMenu() {
      let menus = this.getSubMenu(this.menuData);
      return menus.map(menu => {
        let { path } = menu;
        let id = path.replace(/\//g, '_');
        this.updateMenuList ? this.updateMenuList : [];
        this.updateMenuList.forEach(updateMenu => {
          if (menu.name === updateMenu.name) {
            menu = {
              ...menu,
              id,
              updateSelf: true,
              updateMsg: updateMenu.updateMsg
            };
          }
        });
        if (!menu.children) {
          return menu;
        }
        let newMenu = menu.children.map(subMenu => {
          let { path } = subMenu;
          let id = path.replace(/\//g, '_');
          this.updateMenuList.forEach(updateMenu => {
            if (subMenu.name === updateMenu.name) {
              subMenu = {
                ...subMenu,
                id,
                updateSelf: true,
                updateMsg: updateMenu.updateMsg
              };
            }
          });
          // 存在三级菜单
          if (subMenu.children && subMenu.children.length > 0) {
            let newThirdMenu = subMenu.children.map(thirdMenu => {
              this.updateMenuList.forEach(updateMenu => {
                if (thirdMenu.name === updateMenu.name) {
                  // thirdMenu.id = id;
                  thirdMenu = {
                    ...thirdMenu,
                    id,
                    update: true,
                    updateMsg: updateMenu.updateMsg
                  };
                  subMenu.update = true;
                  menu.update = true;
                  updateMenu.id = id;
                }
              });
              return thirdMenu;
            });
            subMenu.children = newThirdMenu;
          } else {
            // 只有两级菜单
            this.updateMenuList.forEach(updateMenu => {
              if (subMenu.name === updateMenu.name) {
                subMenu = {
                  ...subMenu,
                  id,
                  update: true,
                  updateMsg: updateMenu.updateMsg
                };
                menu.update = true;
              }
            });
          }
          return subMenu;
        });
        menu.children = newMenu;
        return menu;
      });
    }
  },
  methods: {
    clickMenuUpdateFlag(menu) {
      let { id, updateMsg, name } = menu;
      let time = this.latestUpdateTime.substr(0, 8);
      let nowTime = moment().format('YYYYMMDDHHmm');
      // 是否已经发布投产
      let isPublished = this.latestUpdateTime < nowTime;
      let isPublishedStr = isPublished ? '' : '预告： ';
      let head = document.getElementsByTagName('head')[0];
      // 页面有滚动条且有滚动距离 scrollY > 0 生成的样式 top太低 看不见提示，所以强制修改
      if (scrollY > 0) {
        let offsetTop =
          document.querySelectorAll(`#${id}`)[0].offsetTop + 'px !important;';
        let style = document.createElement('style');
        style.type = 'text/css';
        style.id = 'tooltip-extra';
        let node = document.createTextNode(
          `.introjs-fixedTooltip{ top: ${offsetTop} }`
        );
        style.appendChild(node);
        head.appendChild(style);
      }
      introJs()
        .setOptions({
          showStepNumbers: false,
          positionPrecedence: ['right', 'top'],
          doneLabel: '知道啦,不再显示',
          showProgress: false,
          overlayOpacity: 0.65,
          tooltipPosition: 'auto',
          showBullets: false,
          steps: [
            {
              element: document.querySelectorAll(`#${id}`)[0],
              intro:
                `<div class="intro-tip-title">${isPublishedStr}${time}投产窗口更新功能</div>` +
                updateMsg,
              position: 'right'
            }
          ]
        })
        .onexit(() => {
          //移除添加的style 不然越来越多
          if (scrollY > 0) {
            head.removeChild(document.querySelectorAll('#tooltip-extra')[0]);
          }
        })
        .oncomplete(() => {
          // 点击过的菜单提示 不再显示
          let objCheckedUpdateMenuWithUser = LocalStorage.getItem(
            `objCheckedUpdateMenuWithUser_${this.currentUser.user_name_en}`
          );
          objCheckedUpdateMenuWithUser = objCheckedUpdateMenuWithUser || '{}';
          let checkedUpdateMenu = JSON.parse(objCheckedUpdateMenuWithUser)
            .checkedUpdateMenu;
          checkedUpdateMenu = checkedUpdateMenu || [];
          checkedUpdateMenu.push(name);
          objCheckedUpdateMenuWithUser = {
            user_name_en: this.currentUser.user_name_en,
            checkedUpdateMenu: checkedUpdateMenu,
            updateMenuTime: this.latestUpdateTime
          };
          LocalStorage.set(
            `objCheckedUpdateMenuWithUser_${this.currentUser.user_name_en}`,
            JSON.stringify(objCheckedUpdateMenuWithUser)
          );
          this.updateMenuList = this.updateMenuList.filter(menu => {
            return menu.name !== name;
          });
        })
        .start();
    },
    getSubMenu(menu, parent) {
      const { fullPath } = this.$route;
      return menu
        .filter(item => !!item.path)
        .filter(item => !(item.meta && item.meta.hideInMenu))
        .filter(item => this.check(item.authority))
        .filter(item => (this.checkAdmin() ? item : item.name !== 'FDEV'))
        .filter(item => {
          return this.checkRole(item.role);
        })
        .map(item => {
          const result = {
            ...item
          };
          // 添加id属性，自动化测试需要
          let path = result.path;
          let id = path.replace(/\//g, '_');
          result.id = id;
          if (fullPath.startsWith(item.path)) {
            result.opened = true;
            if (parent) {
              parent.opened = true;
            }
          }
          if (!result.children || result.children.length === 0) {
            delete result.children;
            return result;
          }
          if (result.children) {
            // Reduce memory usage
            result.children = this.getSubMenu(result.children, result);
          }
          return result;
        });
    },
    secMenuClick(menu) {
      if (!menu.children || menu.children.length === 0) {
        if (menu.toPath) {
          window.open(menu.toPath);
        } else {
          this.$router.push(menu.path);
        }
      }
    }
  },
  created() {
    if (window.location.origin === 'xxx') {
      this.envName = 'SIT测试环境';
    } else if (window.location.origin === 'xxx') {
      this.envName = 'UAT测试环境';
    } else if (window.location.origin === 'xxx') {
      this.envName = '准生产环境';
    } else if (window.location.origin === 'xxx') {
      this.envName = '';
    } else {
      this.envName = '本地测试环境';
    }
    axios
      .get(`fdev-configserver/myapp/default/master/release-log.json`)
      .then(res => {
        this.updateMenuList = res.data.updateMenuList;
        let objCheckedUpdateMenuWithUser = LocalStorage.getItem(
          `objCheckedUpdateMenuWithUser_${this.currentUser.user_name_en}`
        );
        objCheckedUpdateMenuWithUser = objCheckedUpdateMenuWithUser || '{}';
        let checkedUpdateMenu = JSON.parse(objCheckedUpdateMenuWithUser)
          .checkedUpdateMenu;
        let updateMenuTime = JSON.parse(objCheckedUpdateMenuWithUser)
          .updateMenuTime;
        let user_name_en = JSON.parse(objCheckedUpdateMenuWithUser)
          .user_name_en;
        this.latestUpdateTime = res.data.updateTime;
        if (
          updateMenuTime === this.latestUpdateTime &&
          this.currentUser.user_name_en === user_name_en
        ) {
          checkedUpdateMenu = checkedUpdateMenu || [];
          this.updateMenuList = this.updateMenuList.filter(updateMenu => {
            let isChecked = checkedUpdateMenu.some(checkedMenuName => {
              return checkedMenuName === updateMenu.name;
            });
            return !isChecked;
          });
        } else {
          let obj = {
            updateMenuTime: this.latestUpdateTime
          };
          LocalStorage.set(
            `objCheckedUpdateMenuWithUser_${this.currentUser.user_name_en}`,
            JSON.stringify(obj)
          );
        }
      })
      .catch(err => {
        throw err;
      });
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
.menu-list
  width 210px
  >>>
    .no-expand
      display none
    .no-children.opened .q-item__section--main
      color #0663BE
.sec-menu-icon
  width 16px
  height 16px
</style>
