<template>
  <f-block>
    <!-- 查询条件 -->
    <div>
      <!-- 直接选择人员-->
      <div class="q-pb-sm text-center">
        <fdev-select
          use-input
          :options="usersOptions"
          option-label="user_name_cn"
          option-value="id"
          v-model="inputValue"
          class="input-width"
          @filter="filteredUsers"
          @input="selectUser"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.user_name_cn">
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.user_name_en">
                  {{ scope.opt.user_name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </div>
      <fdev-separator />
      <!-- 所属小组 -->
      <GroupSelectTree
        v-if="groupData && groupData.length > 0"
        :dataSource="groupData"
        @selectGroup="selectGroup"
        @clickGroup="clickGroup"
        @resetTree="resetTree"
        class="q-mt-sm"
      />
      <!-- 所属公司 -->
      <div class="q-pt-md">
        <f-icon name="team" class="text-primary" style="margin-right:8px" />
        <span class="label-style">所属公司(默认全选)：</span>
        <span
          class="text-primary q-ml-md float-right cursor-pointer"
          @click="resetCompany"
        >
          重置
        </span>
        <span
          v-if="company.length > 0"
          class="text-primary float-right cursor-pointer"
          @click="closeCompany = !closeCompany"
        >
          {{ closeCompany ? '收起' : '展开' }}
        </span>
        <div :class="closeCompany ? 'user-wrapper' : 'user-wrapper-close'">
          <div class="col q-mt-xs" v-if="company.length > 0">
            <button
              v-for="(item, index) in company"
              :key="index"
              class="q-mr-md q-mb-sm btn"
              :class="{
                'text-white bg-primary': item.selected === true
              }"
              @click="selectCompany(item)"
            >
              {{ item.name }}
            </button>
          </div>
        </div>
      </div>
      <fdev-separator />
      <!-- 角色(默认全选) -->
      <div class="q-mt-md">
        <f-icon name="person_o" class="text-primary" style="margin-right:8px" />
        <span class="label-style">角色(默认全选)：</span>
        <div class="q-mt-xs">
          <button
            v-for="(item, index) in options"
            :key="index"
            class="q-mr-md btn"
            :class="{
              'text-white bg-primary': roles.indexOf(item.value) > -1
            }"
            @click="selectRole(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
      <fdev-separator />
      <!-- 人员 -->
      <div class="q-my-md">
        <f-icon name="mine" class="text-primary" style="margin-right:8px" />
        <span class="label-style inline-block">人员：</span>
        <span
          v-if="users.length > 0"
          class="text-primary q-ml-md float-right cursor-pointer"
          @click="selectAll"
        >
          全选
        </span>
        <span
          v-if="users.length > 0"
          class="text-primary float-right cursor-pointer"
          @click="closeUsers = !closeUsers"
        >
          {{ closeUsers ? '收起' : '展开' }}
        </span>
        <div :class="closeUsers ? 'user-wrapper' : 'user-wrapper-close'">
          <div class="col q-mt-xs" v-if="users.length > 0">
            <button
              v-for="(item, index) in users"
              :key="index"
              class="q-mr-md q-mb-sm btn"
              :class="{
                'text-white bg-primary': item.selected === true
              }"
              @click="selectUser(item)"
            >
              {{ item.user_name_cn }}
            </button>
          </div>
          <div class="col q-ml-sm" v-else>暂无</div>
        </div>
      </div>
      <!-- 已选人员 -->
      <div>
        <span class="text-grey-9 inline-block">已选人员：</span>
        <span
          class="text-primary q-ml-md float-right cursor-pointer"
          @click="clearAll"
          v-if="userSelected.length > 0"
        >
          清除
        </span>
        <div>
          <button
            v-for="(item, index) in userSelected"
            :key="index"
            class="q-mr-md q-mb-sm btn"
            :class="{
              'text-white bg-primary': item.selected === true
            }"
            @click="selectUser(item)"
          >
            {{ item.user_name_cn }}
          </button>
        </div>
        <!-- 查询按钮 -->
        <div class="text-center">
          <fdev-btn dialog label="查询" @click="initTable" ficon="search" />
        </div>
      </div>
    </div>
    <!-- 表格数据 -->
    <Loading :visible="loading">
      <!-- 标题、下载按钮、重要程度开关 -->
      <div class="row no-wrap q-pt-sm">
        <!-- 标题 -->
        <div class="title">
          <f-icon
            name="detail_s_f"
            class="text-primary"
            style="margin-right:8px"
          />
          人员任务数量统计
          <f-icon name="help_c_o" class="cursor-pointer text-primary" />
          <fdev-tooltip
            >1:此统计表中的任务数量是基于挂载了需求的任务进行统计的<br />
            2:每列 “数字1+数字2” 格式表示 业务个数+科技个数</fdev-tooltip
          >
        </div>
        <fdev-space />
        <!-- 重要程度 -->
        <fdev-btn-toggle
          :value="groupType"
          @input="updateGroupType($event)"
          :options="groupTypeOptions"
        />
        <!-- 下载按钮 -->
        <span>
          <fdev-tooltip v-if="taskStatisList.length === 0">
            暂无可用数据
          </fdev-tooltip>
          <fdev-btn
            class="q-ml-md"
            ficon="download"
            label="下载"
            normal
            @click="downloadExcel"
            :loading="btnLoading"
            :disable="taskStatisList.length === 0"
          />
        </span>
      </div>
      <!-- 表格 -->
      <div class="scroll q-mb-lg">
        <!-- 有数据时 -->
        <table
          width="100%"
          border="1"
          bordercolor="#DDDDDD"
          class="table-style"
          id="perTaskNumTable"
        >
          <thead>
            <tr>
              <td rowspan="2">序号</td>
              <td rowspan="2">姓名</td>
              <td rowspan="2">账号</td>
              <td rowspan="2">组/模板</td>
              <td rowspan="2">公司</td>
              <td colspan="6">任务数</td>
              <td rowspan="2">
                合计任务数
                <span @click="sort = !sort" class="cursor">
                  {{
                    sort === true
                      ? sortOption[0]
                      : sort === false
                      ? sortOption[1]
                      : sortOption[2]
                  }}
                </span>
              </td>
            </tr>
            <tr>
              <td>
                待实施
                <span @click="sortByTodo = !sortByTodo" class="cursor">
                  {{
                    sortByTodo === true
                      ? sortOption[0]
                      : sortByTodo === false
                      ? sortOption[1]
                      : sortOption[2]
                  }}
                </span>
              </td>
              <td>
                开发阶段
                <span @click="sortByDev = !sortByDev" class="cursor">
                  {{
                    sortByDev === true
                      ? sortOption[0]
                      : sortByDev === false
                      ? sortOption[1]
                      : sortOption[2]
                  }}
                </span>
              </td>
              <td>
                SIT
                <span @click="sortBySit = !sortBySit" class="cursor">
                  {{
                    sortBySit === true
                      ? sortOption[0]
                      : sortBySit === false
                      ? sortOption[1]
                      : sortOption[2]
                  }}
                </span>
              </td>
              <td>
                UAT
                <span @click="sortByUat = !sortByUat" class="cursor">
                  {{
                    sortByUat === true
                      ? sortOption[0]
                      : sortByUat === false
                      ? sortOption[1]
                      : sortOption[2]
                  }}
                </span>
              </td>
              <td>
                REL
                <span @click="sortByRel = !sortByRel" class="cursor">
                  {{
                    sortByRel === true
                      ? sortOption[0]
                      : sortByRel === false
                      ? sortOption[1]
                      : sortOption[2]
                  }}
                </span>
              </td>
              <td>
                暂缓
                <span
                  @click="sortByDeferTask = !sortByDeferTask"
                  class="cursor"
                >
                  {{
                    sortByDeferTask === true
                      ? sortOption[0]
                      : sortByDeferTask === false
                      ? sortOption[1]
                      : sortOption[2]
                  }}
                </span>
              </td>
            </tr>
          </thead>
          <!--具体的数据值-->
          <tr v-for="(item, index) in taskStatisList" :key="index">
            <td class="text-center">{{ item.num }}</td>
            <td class="text-center">{{ item.name }}</td>
            <td class="text-center">{{ item.account }}</td>
            <td class="text-center">{{ item.group }}</td>
            <td class="text-center">{{ item.company }}</td>
            <td class="text-center">{{ item.todo }}</td>
            <td class="text-center">{{ item.develop }}</td>
            <td class="text-center">{{ item.sit }}</td>
            <td class="text-center">{{ item.uat }}</td>
            <td class="text-center">{{ item.rel }}</td>
            <td class="text-center">{{ item.deferTask }}</td>
            <td class="text-center">{{ item.sum }}</td>
          </tr>
        </table>

        <!-- 无数据时 -->
        <div v-if="taskStatisList.length === 0" class="column items-center">
          <f-image name="no_data" class="q-mt-xl" />
        </div>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import GroupSelectTree from '@/components/UI/GroupSelectTree';
import { appendNode } from '@/utils/utils';
import { mapState, mapActions, mapMutations, mapGetters } from 'vuex';

export default {
  components: { GroupSelectTree, Loading },
  data() {
    return {
      groupTreeData: [], //树
      sort: Boolean,
      sortByTodo: Boolean,
      sortByDev: Boolean,
      sortBySit: Boolean,
      sortByUat: Boolean,
      sortByRel: Boolean,
      sortByDeferTask: Boolean,
      sortOption: ['↑', '↓', '↕'],
      taskStatisList: [],
      company: [],
      users: [],
      closeCompany: false,
      closeUsers: false,

      options: [
        { label: '创建者', value: 'creator' },
        { label: '开发人员', value: 'developer' },
        { label: '测试人员', value: 'tester' },
        { label: '行内项目负责人', value: 'spdb_master' },
        { label: '任务负责人', value: 'master' }
      ],
      open: false,
      userList: [],
      inputValue: null,
      usersOptions: [],
      list: [],
      loading: false,
      groupTypeOptions: [
        { label: '全部', value: '0' },
        { label: '重要', value: '1' }
      ],
      priority: false,
      btnLoading: false
    };
  },
  watch: {
    selectedGroups(val) {
      this.filterByGroupOrCompany();
    },
    selectedCompanyList(val) {
      this.filterByGroupOrCompany();
    },
    async groupType(val) {
      this.priority = val === '1' ? true : false;
      this.initTable();
    },
    sort(val) {
      if (val === true) {
        //升序排列
        this.list.sort(this.ascend('sort'));
      } else if (val === false) {
        this.list.sort(this.descend('sort'));
      }
      this.taskStatisList = this.sumAnalyze(this.list);
    },
    sortByTodo(val) {
      if (val === true) {
        //升序排列
        this.list.sort(this.ascend('sortByTodo'));
      } else if (val === false) {
        this.list.sort(this.descend('sortByTodo'));
      }
      this.taskStatisList = this.sumAnalyze(this.list);
    },
    sortByDev(val) {
      if (val === true) {
        //升序排列
        this.list.sort(this.ascend('sortByDev'));
      } else if (val === false) {
        this.list.sort(this.descend('sortByDev'));
      }
      this.taskStatisList = this.sumAnalyze(this.list);
    },
    sortBySit(val) {
      if (val === true) {
        //升序排列
        this.list.sort(this.ascend('sortBySit'));
      } else if (val === false) {
        this.list.sort(this.descend('sortBySit'));
      }
      this.taskStatisList = this.sumAnalyze(this.list);
    },
    sortByUat(val) {
      if (val === true) {
        //升序排列
        this.list.sort(this.ascend('sortByUat'));
      } else if (val === false) {
        this.list.sort(this.descend('sortByUat'));
      }
      this.taskStatisList = this.sumAnalyze(this.list);
    },
    sortByRel(val) {
      if (val === true) {
        //升序排列
        this.list.sort(this.ascend('sortByRel'));
      } else if (val === false) {
        this.list.sort(this.descend('sortByRel'));
      }
      this.taskStatisList = this.sumAnalyze(this.list);
    },
    sortByDeferTask(val) {
      if (val === true) {
        //升序排列
        this.list.sort(this.ascend('sortByDeferTask'));
      } else if (val === false) {
        this.list.sort(this.descend('sortByDeferTask'));
      }
      this.taskStatisList = this.sumAnalyze(this.list);
    }
  },
  computed: {
    ...mapState('userActionSaveDashboard/projectTeamManage/userTaskAnalysis', [
      'userSelected',
      'roles',
      'selectedCompanyList',
      'selectedGroups',
      'groupsTree',
      'groupType'
    ]),
    ...mapGetters(
      'userActionSaveDashboard/projectTeamManage/userTaskAnalysis',
      ['groupData']
    ),
    ...mapState('userForm', {
      groups: 'groups',
      companies: 'companies'
    }),
    ...mapState('dashboard', {
      usersList: 'userList',
      taskStatis: 'taskStatis'
    }),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    }
  },
  methods: {
    ...mapMutations(
      'userActionSaveDashboard/projectTeamManage/userTaskAnalysis',
      [
        'updateUserSelected',
        'updateRoles',
        'updateSelectedCompanyList',
        'updateSelectedGroups',
        'updateGroupsTree',
        'updateGroupType'
      ]
    ),
    ...mapActions('userForm', ['fetchGroup', 'fetchCompany']),
    ...mapActions('dashboard', ['queryUserCoreData', 'queryTaskStatis']),
    // 升序
    ascend(sort) {
      return function(a, b) {
        return a[sort] - b[sort];
      };
    },
    // 降序
    descend(sort) {
      return function(a, b) {
        return b[sort] - a[sort];
      };
    },
    // 总合计
    sumAnalyze(data) {
      let num = 1;
      data.forEach(item => {
        item.num = num++;
      });
      return data;
    },
    //选择组方法
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击方法
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('userTaskTree', JSON.stringify(tree));
    },
    //重置整棵树：
    resetTree(tree) {
      this.updateSelectedGroups(['5c81c4d0d3e2a1126ce30049']);
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    appendNode(parent, set) {
      return appendNode(parent, set);
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: item.id === '5c81c4d0d3e2a1126ce30049' ? true : false,
          children: this.addAttribute(item.children)
        };
      });
    },
    resetCompany() {
      this.updateSelectedCompanyList([]);
      this.company.forEach(item => (item.selected = false));
    },
    // 下载
    downloadExcel() {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('perTaskNumTable', '人员任务数量统计.xlsx');
      });
      this.btnLoading = false;
    },

    /* 根据小组公司筛选 */
    filterByGroupOrCompany() {
      if (this.selectedCompanyList.length === 0) {
        this.users = this.userList
          .filter(user => {
            return this.selectedGroups.indexOf(user.group_id) > -1;
          })
          .sort((a, b) => {
            return a.user_name_en.localeCompare(b.user_name_en);
          });
      } else {
        this.users = this.userList
          .filter(user => {
            return (
              this.selectedGroups.indexOf(user.group_id) > -1 &&
              this.selectedCompanyList.indexOf(user.company_id) > -1
            );
          })
          .sort((a, b) => {
            return a.user_name_en.localeCompare(b.user_name_en);
          });
      }
    },
    /* 选择人员 */
    selectUser(user) {
      user.selected = !user.selected;
      let userSelected = this.userList.filter(item => item.selected === true);
      this.updateUserSelected(userSelected);
      this.inputValue = null;
    },
    async initTable() {
      this.loading = true;
      if (this.userSelected.length === 0) {
        this.taskStatisList = [];
        this.loading = false;
        return;
      }
      this.priority = this.groupType === '1' ? true : false;
      await this.queryTaskStatis({
        ids: this.userSelected.map(item => item.id),
        roles: this.roles,
        priority: this.priority
      });
      this.taskStatisList = this.sumAnalyze(this.taskStatis);
      this.loading = false;
      this.list = this.taskStatisList.map(item => {
        item.sort = item.sum.split('=')[1];
        item.sortByTodo = item.todo.split('=')[1];
        item.sortByDev = item.develop.split('=')[1];
        item.sortBySit = item.sit.split('=')[1];
        item.sortByUat = item.uat.split('=')[1];
        item.sortByRel = item.rel.split('=')[1];
        item.sortByDeferTask = item.deferTask.split('=')[1];
        return item;
      });
    },
    selectCompany(company) {
      company.selected = !company.selected;
      this.updateSelectedCompanyList([]);
      this.company.forEach(item => {
        if (item.selected === true) {
          this.selectedCompanyList.push(item.id);
        }
      });
    },
    selectRole(role) {
      const hadSelected = this.roles.indexOf(role);
      if (hadSelected > -1) {
        let roles = this.roles.filter(item => item !== role);
        this.updateRoles(roles);
      } else {
        this.roles.push(role);
      }
    },
    filteredUsers(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.usersOptions = this.userList.filter(
          v =>
            v.user_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    /* 全选 */
    selectAll() {
      let flag = true;
      this.users.forEach(item => {
        if (item.selected === false) {
          flag = false;
        }
        item.selected = true;
      });
      if (flag) {
        return;
      }
      let userSelected = this.userList.filter(item => item.selected === true);
      this.updateUserSelected(userSelected);
    },
    clearAll() {
      this.userList.forEach(item => (item.selected = false));
      this.updateUserSelected([]);
    },
    setAttribute() {
      this.company = this.companies.map(item => {
        return {
          ...item,
          selected: false
        };
      });
    },
    // 原进原出
    saveData() {
      let item = JSON.parse(sessionStorage.getItem('userTaskTree'));
      if (item) {
        this.updateGroupsTree(item);
      }
    }
  },
  async created() {
    this.loading = true;
    await this.fetchGroup();
    await this.fetchCompany();
    this.setAttribute();
    await this.queryUserCoreData();
    this.userList = [];
    this.usersList.forEach(item => {
      if (item.status === '0') {
        this.userList.push({ ...item, selected: false });
      }
    });
    this.filterByGroupOrCompany();
    this.initTable();
    this.loading = false;
    this.userSelected.forEach(item => {
      this.users.forEach(val => {
        if (val.user_name_cn == item.user_name_cn) {
          val.selected = true;
        }
      });
    });
    this.selectedCompanyList.forEach(item => {
      this.company.forEach(val => {
        if (val.id == item) {
          val.selected = true;
        }
      });
    });
  }
};
</script>

<style lang="stylus" scoped>
tr
  td, th
    text-align center
    padding 5px
    max-width 300px
.indent
  text-indent 3em
button
  border none
  outline none
  background none
  cursor pointer
.user-wrapper-close
  height 31px
  overflow hidden
.input-width
  width 300px
  margin 0 auto
.btn
  cursor pointer
  color #757575
  &:hover
      background #ebeef5
.cursor
  cursor pointer
.select-group
  position absolute
  right 60px
  top .3em
.float-switch{
  display: flex;
  align-items: center;
  margin-top: 6px;
  margin-left: 20px;
}
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
.table-style
  border-collapse:collapse !important
  margin-top:26px;
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
tr
  td, th
    text-align center
    padding 8px
.label-style
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
  font-weight:600
</style>
