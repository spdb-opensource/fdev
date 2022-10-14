<template>
  <div :class="[classFlag ? 'notTouchMessage' : '', 'noprint']">
    <el-menu
      :default-active="activeNav"
      class="el-menu-demo"
      mode="horizontal"
      background-color="#545c64"
      text-color="#fff"
      active-text-color="#ffd04b"
      @select="handleSelect"
    >
      <template v-for="(item, menuIndex) in meunList">
        <component
          :is="item.component"
          :index="item.path"
          :id="item.id"
          :key="menuIndex"
        >
          <template slot="title">
            {{ item.name }}
            <el-badge
              v-if="
                item.name === '案例' &&
                  (waitEffect || totalCase || waitApproval)
              "
              :value="waitEffect || totalCase || waitApproval"
              class="notice"
              :max="99"
            />
            <i
              class="el-icon-refresh"
              @click.stop="clickMenuUpdateFlag(item)"
              v-if="item.updateMsg"
            />
          </template>
          <template v-if="item.children">
            <template v-for="(child, childIndex) in item.children">
              <component
                :is="child.component"
                :index="child.path"
                :slot="child.slot"
                :disabled="child.disabled"
                :key="childIndex"
              >
                <span :class="[classFlag ? 'menuItemColor' : '']"
                  >{{ child.name }}
                  <span v-if="child.name === '测试管理'">
                    <el-badge
                      v-if="totalCase"
                      :value="totalCase"
                      class="notice"
                      :max="99"
                    />
                  </span>
                </span>
                <template v-if="child.children">
                  <template v-for="(thild, thildIndex) in child.children">
                    <component
                      :is="thild.component"
                      :index="thild.path"
                      :slot="thild.slot"
                      :key="thildIndex"
                    >
                      <span
                        >{{ thild.name }}
                        <span v-if="thild.name === '案例审批'">
                          <el-badge
                            v-if="totalCase"
                            :value="totalCase"
                            class="notice"
                            :max="99"
                          />
                        </span>
                        <span v-if="thild.name === '评审审批'">
                          <el-badge
                            v-if="waitApproval"
                            :value="waitApproval"
                            class="notice"
                            type="primary"
                            :max="99"
                          />
                        </span>
                        <span v-if="thild.name === '生效审批'">
                          <el-badge
                            v-if="waitEffect"
                            :value="waitEffect"
                            class="notice"
                            type="primary"
                            :max="99"
                          />
                        </span>
                      </span>
                    </component>
                  </template>
                </template>
              </component>
            </template>
          </template>
        </component>
      </template>
    </el-menu>
  </div>
</template>
<script>
import { mapActions, mapState } from 'vuex';
import axios from 'axios';
import introJs from 'intro.js';
import 'intro.js/introjs.css';
export default {
  name: 'Menu',
  data() {
    return {
      classFlag: '',
      userRole: sessionStorage.getItem('Trole'),
      groupId: sessionStorage.getItem('group_id'),
      activeNav: 'TestOrder',
      navMeunList: [
        {
          component: 'el-submenu',
          path: 'WorkOrder',
          name: '工单',
          children: [
            { component: 'el-menu-item', path: 'TestOrder', name: '工单管理' },
            {
              component: 'el-menu-item',
              path: 'AssignOrder',
              name: '分配管理'
            },
            {
              component: 'el-menu-item',
              path: 'testReportSIT',
              name: 'SIT测试报告'
            },
            { component: 'el-menu-item', path: 'sitMsg', name: '提测信息' }
          ]
        },
        {
          component: 'el-submenu',
          path: 'Case',
          name: '案例',
          children: [
            {
              component: 'el-menu-item',
              path: 'FunctionMenu',
              name: '功能菜单'
            },
            { component: 'el-menu-item', path: 'CaseBase', name: '案例库' },
            {
              component: 'el-submenu',
              path: 'caseApproval',
              children: [
                { component: 'template', slot: 'title', name: '案例审批' },
                {
                  component: 'el-menu-item',
                  path: 'GeneralApproval',
                  name: '通用审批'
                },
                {
                  component: 'el-menu-item',
                  path: 'DiscardApproval',
                  name: '废弃审批'
                },
                {
                  component: 'el-menu-item',
                  path: 'PassApproval',
                  name: '评审审批'
                },
                {
                  component: 'el-menu-item',
                  path: 'EffectiveApproval',
                  name: '生效审批'
                }
              ]
            }
          ]
        },
        {
          component: 'el-submenu',
          path: 'defectManage',
          name: '缺陷',
          children: [
            // {component: 'template', path:'', slot: 'title', name:'缺陷管理'},
            { component: 'el-menu-item', path: 'MantisIssue', name: 'sit缺陷' },
            {
              component: 'el-menu-item',
              path: 'uatIssue',
              name: 'uat缺陷',
              disabled: 'disabled'
            },
            {
              component: 'el-menu-item',
              path: 'ProductionsProblem',
              name: '生产问题'
            }
          ]
        },
        {
          component: 'el-submenu',
          path: 'chart',
          name: '监控',
          children: [
            {
              component: 'el-menu-item',
              path: 'UserChart',
              name: '人员维度报表'
            },
            {
              component: 'el-menu-item',
              path: 'DefectChart',
              name: '缺陷维度报表'
            },
            {
              component: 'el-menu-item',
              path: 'OrderChart',
              name: '工单维度报表'
            },
            {
              component: 'el-menu-item',
              path: 'GroupChart',
              name: '小组维度报表'
            },
            {
              component: 'el-menu-item',
              path: 'QualityChart',
              name: '质量分析'
            }
          ]
        },
        {
          component: 'el-submenu',
          path: 'dataMenu',
          name: '数据工厂',
          children: [
            // {component: 'template', path:'', slot: 'title', name:'数据菜单'},
            { component: 'el-menu-item', path: 'TestData', name: '测试数据' },
            {
              component: 'el-menu-item',
              path: 'SystemContact',
              name: '系统联系人'
            }
          ]
        },
        {
          component: 'el-submenu',
          path: 'auto',
          name: '自动化',
          children: [
            // {component: 'template', path:'', slot: 'title', name:'数据菜单'},

            {
              component: 'el-menu-item',
              path: 'TestCase',
              name: '案例维护'
            },
            {
              component: 'el-menu-item',
              path: 'ComponentData',
              name: '组件维护'
            },
            {
              component: 'el-submenu',
              path: 'BasicData',
              children: [
                { component: 'template', slot: 'title', name: '基础数据维护' },
                {
                  component: 'el-menu-item',
                  path: 'MenuInfo',
                  name: '菜单项维护'
                },
                {
                  component: 'el-menu-item',
                  path: 'TestDataAuto',
                  name: '测试数据维护'
                },
                {
                  component: 'el-menu-item',
                  path: 'AssertData',
                  name: '断言数据维护'
                },

                {
                  component: 'el-menu-item',
                  path: 'ElementPosition',
                  name: '元素定位维护'
                },
                {
                  component: 'el-menu-item',
                  path: 'ElementDictionary',
                  name: '元素字典维护'
                },
                {
                  component: 'el-menu-item',
                  path: 'UserInfo',
                  name: '用户信息维护'
                }
              ]
            }
          ]
        },
        {
          component: 'el-submenu',
          path: 'Set',
          name: '设置',
          children: [
            //  {component: 'template', path:'', slot: 'title', name:'测试管理'},
            { component: 'el-menu-item', path: 'UserAdmin', name: '用户管理' },
            { component: 'el-menu-item', path: 'Notice', name: '公告' },
            { component: 'el-menu-item', path: 'About', name: '关于' }
          ]
        }
      ],
      navConfig: {
        TestOrder: 'TestOrder',
        CaseBase: 'CaseBase',
        UserAdmin: 'UserAdmin',
        FunctionMenu: 'FunctionMenu',
        GeneralApproval: 'GeneralApproval',
        DiscardApproval: 'DiscardApproval',
        PassApproval: 'PassApproval',
        EffectiveApproval: 'EffectiveApproval',
        Notice: 'Notice',
        About: 'About',
        testReportSIT: 'testReportSIT',
        Message: 'Message',
        UserChart: 'UserChart',
        DefectChart: 'DefectChart',
        OrderChart: 'OrderChart',
        GroupChart: 'GroupChart',
        QualityChart: 'QualityChart',
        WeekChart: 'WeekChart',
        WorkChart: 'WorkChart',
        completedWorkNoChart: 'completedWorkNoChart',
        personChart: 'personChart',
        DailyChart: 'DailyChart',
        RevertChart: 'RevertChart',
        MantisIssue: 'MantisIssue',
        ProductionsProblem: 'ProductionsProblem',
        TestData: 'TestData',
        SystemContact: 'SystemContact',
        AssignOrder: 'AssignOrder',
        sitMsg: 'sitMsg',
        MenuInfo: 'MenuInfo',
        TestCase: 'TestCase',
        CaseDetail: 'CaseDetail',
        TestDataAuto: 'TestDataAuto',
        AssertData: 'AssertData',
        ComponentData: 'ComponentData',
        ComponentDetail: 'ComponentDetail',
        ElementPosition: 'ElementPosition',
        ElementDictionary: 'ElementDictionary',
        UserInfo: 'UserInfo',
        DefectChart: 'DefectChart',
        OrderChart: 'OrderChart',
        GroupChart: 'GroupChart',
        BasicData: 'BasicData'
      },
      orderMenu: ['HistoryOrder', 'QueryOrder', 'ApprovalOrder', 'WasteOrder'],
      updateMenuList: [],
      meunList: [],
      user_en_name: '',
      showAuto: false
    };
  },
  watch: {
    $route: {
      handler: function(to, from) {
        this.classFlag =
          this.$router.currentRoute.path.substring(1) === 'message';
        var href = window.location.href;
        href = href.split('/#/')[1] || '';
        let userRole = sessionStorage.getItem('Trole');
        if (userRole && this.orderMenu.includes(href)) {
          href = 'TestOrder'; //工单查询
        } else if (href.includes('userProfile')) {
          href = 'UserAdmin';
        } else if (href.includes('Plan')) {
          href = 'TestOrder';
        } else if (from) {
          let navActive = from.name in this.navConfig ? from.name : 'TestOrder';
          href = to.name in this.navConfig ? to.name : navActive;
          sessionStorage.setItem('menuKey', href);
        }
        this.activeNav = href;
      },
      immediate: true
    },
    updateMenuList: {
      handler: function() {
        this.menuWidthupdateMsg();
      },
      deep: true
    }
  },
  computed: {
    waitEffect() {
      return this.userApprovalList.waitEffect;
    },
    waitApproval() {
      return this.userApprovalList.waitApproval;
    },
    ...mapState('menu', ['userApprovalList']),
    totalCase() {
      return this.waitApproval + this.waitEffect;
    }
  },
  methods: {
    ...mapActions('menu', ['getUserApprovalList']),
    handleSelect(key, keypath) {
      sessionStorage.setItem('menuKey', key);
      this.$router.push('/' + key);
    },
    clickMenuUpdateFlag(menu) {
      let { id, updateMsg, name } = menu;
      introJs()
        .setOptions({
          showStepNumbers: false,
          positionPrecedence: ['bottom', 'top'],
          doneLabel: '知道啦,不再显示',
          showProgress: false,
          overlayOpacity: 0.65,
          showBullets: false,
          steps: [
            {
              element: document.querySelectorAll(`#${id}`)[0],
              intro: updateMsg,
              position: 'right'
            }
          ]
        })
        .oncomplete(() => {
          // 点击过的菜单提示 不再显示
          let tuiCheckedUpdateMenuWithUser = localStorage.getItem(
            `tuiCheckedUpdateMenuWithUser_${this.user_en_name}`
          );
          tuiCheckedUpdateMenuWithUser = tuiCheckedUpdateMenuWithUser || '{}';
          let checkedUpdateMenu = JSON.parse(tuiCheckedUpdateMenuWithUser)
            .checkedUpdateMenu;
          checkedUpdateMenu = checkedUpdateMenu || [];
          checkedUpdateMenu.push(name);
          tuiCheckedUpdateMenuWithUser = {
            user_en_name: this.user_en_name,
            checkedUpdateMenu: checkedUpdateMenu,
            updateMenuTime: this.latestUpdateTime
          };
          localStorage.setItem(
            `tuiCheckedUpdateMenuWithUser_${this.user_en_name}`,
            JSON.stringify(tuiCheckedUpdateMenuWithUser)
          );
          this.updateMenuList = this.updateMenuList.filter(menu => {
            return menu.name !== name;
          });
        })
        .start();
    },
    // navMeunList
    menuWidthupdateMsg() {
      this.showAuto =
        sessionStorage.getItem('groupName') === '互联网-公共研发组-效能' ||
        sessionStorage.getItem('group_id') === '12' ||
        JSON.parse(sessionStorage.getItem('userRole')).some(
          item => item.id === '5fae1bcf398c09001299a5f1'
        )
          ? true
          : false;
      let navMeunList = this.navMeunList.filter(menu => {
        let { path } = menu;
        if (!this.showAuto) {
          return path !== 'auto';
        } else {
          return path !== '';
        }
      });
      this.meunList = navMeunList.map(menu => {
        let { path } = menu;
        let id = path;

        this.updateMenuList.forEach(updateMenu => {
          if (menu.name === updateMenu.name) {
            menu = {
              ...menu,
              id,
              updateMsg: updateMenu.updateMsg
            };
          }
        });
        return menu;
      });
    }
  },
  mounted() {
    let nowKey = sessionStorage.getItem('menuKey');

    this.menuWidthupdateMsg();
    if (this.navConfig[nowKey]) {
      this.activeNav = this.navConfig[nowKey];
    } else {
      this.activeNav = 'TestOrder';
    }
    this.user_en_name = sessionStorage.getItem('user_en_name');

    axios
      .get(`fdev-configserver/myapp/default/master/ftms-release-log.json`)
      .then(res => {
        this.updateMenuList = res.data.updateMenuList;
        let tuiCheckedUpdateMenuWithUser = localStorage.getItem(
          `tuiCheckedUpdateMenuWithUser_${this.user_en_name}`
        );
        tuiCheckedUpdateMenuWithUser = tuiCheckedUpdateMenuWithUser || '{}';
        let checkedUpdateMenu = JSON.parse(tuiCheckedUpdateMenuWithUser)
          .checkedUpdateMenu;
        let updateMenuTime = JSON.parse(tuiCheckedUpdateMenuWithUser)
          .updateMenuTime;
        let user_en_name = JSON.parse(tuiCheckedUpdateMenuWithUser)
          .user_en_name;
        this.latestUpdateTime = res.data.updateTime;
        if (
          updateMenuTime === this.latestUpdateTime &&
          this.user_en_name === user_en_name
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
          localStorage.setItem(
            `tuiCheckedUpdateMenuWithUser_${this.user_en_name}`,
            JSON.stringify(obj)
          );
        }
      })
      .catch(err => {
        throw err;
      });
    this.getUserApprovalList();
  }
};
</script>
<style scoped>
.menu-bg {
  background: #454545;
}
a {
  text-decoration: none;
}
.el-menu-demo .el-menu-item {
  font-size: 15px;
}
.el-submenu >>> .el-submenu__title {
  font-size: 15px !important;
  padding: 0 10px !important;
}
.el-menu-item {
  padding: 0 10px;
}
.menuItemColor {
  color: white !important;
}

.el-menu {
  display: flex;
}

.el-submenu {
  flex: 1;
}
.notice {
  margin-top: -15px;
}
.noprint {
  display: block;
}
@media print {
  .noprint {
    display: none;
  }
  .print {
    display: block;
  }
}
</style>
<style>
.noprint .el-badge__content {
  border: 1px solid #544c64 !important;
}
.notice .el-badge__content {
  border: 1px solid #544c64 !important;
}
.introjs-helperLayer {
  background-color: rgba(255, 255, 255, 0.3) !important;
}
.introjs-skipbutton {
  color: #434343;
  border-color: #676767;
}
.introjs-tooltiptext {
  color: #434343;
}
.introjs-tooltip {
  width: 100%;
  min-width: 400px;
}
</style>
