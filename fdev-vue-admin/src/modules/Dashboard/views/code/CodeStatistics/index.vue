<template>
  <f-block>
    <Loading
      :visible="loading['dashboard/queryGitlabCommitInfo'] || showLoading"
    >
      <fdev-splitter
        v-model="splitter"
        class="splitter q-pt-md"
        :horizontal="!$q.platform.is.desktop"
        :disable="!$q.platform.is.desktop"
      >
        <!--机构树 -->
        <template v-slot:before>
          <fdev-splitter v-model="innerSplitter" disable horizontal>
            <template v-slot:before>
              <div class="q-pa-md tree-group">
                <fdev-tree
                  :nodes="nodes"
                  node-key="id"
                  :expanded.sync="expanded"
                  ref="tree"
                  accordion
                  :selected.sync="selected"
                  selected-color="white"
                >
                  <template v-slot:default-header="prop">
                    <div class="node col row items-center">
                      <span class="q-ml-xs">{{ prop.node.label }}</span>
                    </div>
                  </template>
                  <template v-slot:header-root="prop">
                    {{ prop.node.label }}
                  </template>
                </fdev-tree>
              </div>
            </template>
            <template v-slot:after>
              <div class="q-pa-md">
                <div class="data-none row flex-center"></div>
              </div>
            </template>
          </fdev-splitter>
        </template>
        <!-- 表格 -->
        <template v-slot:after>
          <div class="q-px-md">
            <fdev-table
              :data="tableUsers"
              :columns="userColumns"
              row-key="id"
              title="代码统计列表"
              :pagination.sync="pagination"
              :visible-columns="visibleColumns"
              :onSelectCols="updateVisibleColumns"
              @request="changePagination"
              class="my-sticky-column-table"
              titleIcon="dashboard_s_f"
              :exportFunc="handleDownload"
            >
              <!-- 操作按钮  -->
              <template v-slot:top-right>
                <fdev-btn
                  label="代码统计规则"
                  normal
                  ficon="eye"
                  @click="codeRuleOpenHandle"
                />
              </template>
              <!-- 筛选条件 -->
              <template v-slot:top-bottom>
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label-style="width:60px"
                  label="开始日期"
                >
                  <f-date
                    :options="startTimeOptions"
                    :value="time.start_date"
                    @input="onTableRequest($event, 'start')"
                  />
                </f-formitem>
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label-style="width:60px"
                  label="结束日期"
                >
                  <f-date
                    :options="endTimeOptions"
                    :value="time.end_date"
                    @input="onTableRequest($event, 'end')"
                  />
                </f-formitem>
                <!-- 公司 -->
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label-style="width:60px"
                  label="公司"
                >
                  <fdev-select
                    use-input
                    map-options
                    emit-value
                    option-label="label"
                    option-value="value"
                    :value="tableCompany"
                    :options="tableCompanies"
                    @input="onTableRequest($event, 'company')"
                /></f-formitem>
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label="地区"
                  label-style="width:60px"
                  v-if="isCompany"
                >
                  <fdev-select
                    use-input
                    :value="tableArea"
                    :options="tableAreaList"
                    map-options
                    emit-value
                    option-label="label"
                    option-value="value"
                    @input="onTableRequest($event, 'area')"
                /></f-formitem>
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label-style="width:60px"
                  label="角色"
                >
                  <fdev-select
                    use-input
                    :value="tableRole"
                    :options="tableRoles"
                    map-options
                    @input="onTableRequest($event, 'role')"
                    emit-value
                    option-label="label"
                    option-value="value"
                /></f-formitem>
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label-style="width:60px"
                  label="在职"
                >
                  <fdev-select
                    use-input
                    map-options
                    emit-value
                    option-label="label"
                    option-value="value"
                    :value="tableIsLeave"
                    :options="isLeaveOptions"
                    @input="onTableRequest($event, 'isLeave')"
                  />
                </f-formitem>
              </template>
              <!-- 姓名 -->
              <template v-slot:body-cell-nickName="props">
                <fdev-td>
                  <router-link
                    :to="`/user/list/${props.row.userId}`"
                    class="link"
                  >
                    {{ props.value }}
                  </router-link>
                </fdev-td>
              </template>
              <!-- 角色 -->
              <template v-slot:body-cell-rolename="props">
                <fdev-td class="td-width" :title="props.value">
                  {{ props.value
                  }}<fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ props.value }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </fdev-td>
              </template>
              <!-- 详情 -->
              <template v-slot:body-cell-detail="props">
                <fdev-td v-if="props.row.userName">
                  <router-link
                    :to="{
                      path: `/dashboard/code/detail/${props.row.userName}`,
                      query: { time: time }
                    }"
                    class="link"
                  >
                    详情
                  </router-link>
                </fdev-td>
                <fdev-td v-else>
                  -
                </fdev-td>
              </template>
            </fdev-table>
          </div>
        </template>
      </fdev-splitter>
      <!-- 代码统计规弹窗 -->
      <f-dialog :confirm="false" title="代码统计规则" v-model="codeRuleOpen">
        <div class="scroll-thin" style="max-width:500px">
          <div>
            <p class="text">代码统计规则</p>
            <div
              v-for="item in ruleList.ruleContent"
              :key="item"
              class="fontStyle"
            >
              {{ item }}
            </div>
          </div>
          <div>
            <p class="q-pt-md text">常见问题</p>
            <div
              v-for="item in ruleList.question"
              :key="item"
              class="fontStyle"
            >
              {{ item }}
            </div>
          </div>
        </div>
      </f-dialog>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { setSplitter, getSplitter } from '@/modules/User/utils/setting';
import { formatSelectDisplay, deepClone } from '@/utils/utils';
import {
  getCodePagination,
  setCodePagination
} from '@/modules/Dashboard/utils/setting';
import axios from 'axios';
export default {
  name: 'CodeStatistics',
  components: { Loading },
  data() {
    return {
      showLoading: false,
      selectId: '',
      tableUsers: [],
      tableUsersImport: [],
      model: {},
      userColumns: [
        { name: 'nickName', label: '姓名', align: 'left', field: 'nickName' },
        {
          name: 'userName',
          label: 'git用户名',
          align: 'left',
          field: 'userName'
        },
        {
          name: 'companyname',
          label: '公司',
          align: 'left',
          field: 'companyname',
          copy: true
        },
        {
          name: 'rolename',
          label: '角色',
          align: 'left',
          field: 'rolename',
          copy: true
        },
        {
          name: 'groupname',
          label: '小组',
          align: 'left',
          field: 'groupname',
          copy: true
        },
        {
          name: 'total',
          label: '总行数',
          align: 'left',
          field: 'total'
        },
        {
          name: 'additions',
          label: '新增行数',
          align: 'left',
          field: 'additions'
        },
        {
          name: 'deletions',
          label: '删除行数',
          align: 'left',
          field: 'deletions'
        },
        {
          name: 'committedDate',
          label: '提交时间',
          align: 'left',
          field: 'committedDate'
        },
        {
          name: 'startDate',
          label: '开始时间',
          align: 'left',
          field: 'startDate'
        },
        { name: 'endDate', label: '结束时间', align: 'left', field: 'endDate' },
        { name: 'detail', label: '详情', align: 'left', field: 'detail' }
      ],

      nodes: [
        {
          label: '浦发银行',
          id: 'root',
          header: 'root',
          selectable: false,
          children: []
        }
      ],
      expanded: ['root'],
      selected: '',

      splitter: getSplitter() || 30,
      innerSplitter: 70,

      currentGroups: [],
      isLeaveOptions: [
        { label: '在职', value: '0' },
        { label: '离职', value: '1' }
      ],
      pagination: {
        page: 1,
        rowsPerPage: getCodePagination().rowsPerPage || 5,
        rowsNumber: 5
      },
      dowmloadLoading: false,
      codeRuleOpen: false,
      ruleList: {
        ruleContent: [],
        question: []
      }
    };
  },
  filters: {
    filterRole(val) {
      let roles = val.map(item => item.name);
      return roles.join(',');
    }
  },
  watch: {
    groups() {
      this.currentGroups = deepClone(this.groups);
    },
    currentGroups() {
      let root = [];
      let group = [];

      group = this.groups;
      root = this.currentGroups.filter(group => !group.parent);
      this.nodes[0].children = this.appendNode(
        root,
        group.filter(group => group.id && group.parent)
      );
    },
    'pagination.rowsPerPage': function(val) {
      setCodePagination({ rowsPerPage: val });
    },
    selected(val) {
      if (!val) {
        this.tableUsers = [];
        this.selectId = '';
        this.onTableRequest();
      } else {
        this.selectId = val;
        this.onTableRequest();
      }
    },
    splitter(val) {
      setSplitter(val);
    }
  },
  computed: {
    ...mapState('userActionSaveDashboard/codeStatistic/statistics', [
      'time',
      'tableCompany',
      'tableArea',
      'tableRole',
      'tableIsLeave',
      'visibleColumns'
    ]),
    ...mapState('userForm', {
      groupPeople: 'groupPeople',
      abandonGroups: 'abandonGroups',
      groups: 'groupsAll',
      companies: 'companies',
      roles: 'roles',
      areaList: 'areaList'
    }),
    ...mapState('global', ['loading']),
    ...mapState('dashboard', ['gitlabCommitInfo']),
    ...mapGetters('authorized', ['checkPermissions', 'checkTransPermissions']),
    // 公司下拉列表
    tableCompanies() {
      return this.wrapTotal(this.companies, '全部');
    },
    // 地区下拉列表
    tableAreaList() {
      const areas = this.areaList.map(area => {
        return { label: area.name, value: area.name };
      });
      return this.wrapTotal(areas, '全部');
    },
    // 角色下拉列表
    tableRoles() {
      const roles = this.roles.map(role => {
        return { label: role.name, value: role.name };
      });
      return this.wrapTotal(roles, '全部');
    },
    isCompany() {
      const { tableCompany, companies } = this;
      const company = companies.find(item => item.id === tableCompany);
      return company && company.label === '浦发';
    },
    group() {
      let group = this.currentGroups.find(group => group.id === this.selected);
      return group || {};
    },
    params() {
      return {
        groupId: this.selectId,
        companyId: this.tableCompany,
        roleName: this.tableRole,
        status: this.tableIsLeave,
        area: this.isCompany ? this.tableArea : '',
        startDate: this.time.start_date || '',
        endDate: this.time.end_date || '',
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage
      };
    }
  },
  methods: {
    ...mapMutations('userActionSaveDashboard/codeStatistic/statistics', [
      'timeStartDate',
      'timeEndDate',
      'updateCompany',
      'updateArea',
      'updateRole',
      'updateIsLeave',
      'updateVisibleColumns'
    ]),
    ...mapActions('userForm', {
      queryGroupPeople: 'queryGroupPeople',
      fetchGroup: 'fetchGroupAll',
      fetchCompany: 'fetchCompany',
      fetchRole: 'fetchRole',
      queryArea: 'queryArea'
    }),
    ...mapActions('dashboard', ['queryGitlabCommitInfo']),
    formatJson(filterVal) {
      return this.tableUsersImport.map(row => {
        return filterVal.map(col => {
          return row[col];
        });
      });
    },

    async handleDownload() {
      this.tableUsersImport = [];
      await this.queryGitlabCommitInfo({
        groupId: this.selectId,
        companyId: this.tableCompany,
        roleName: this.tableRole,
        status: this.tableIsLeave,
        area: this.isCompany ? this.tableArea : '',
        startDate: this.time.start_date || '',
        endDate: this.time.end_date || '',
        page: this.pagination.page,
        per_page: 0
      });
      this.tableUsersImport = this.gitlabCommitInfo.result;
      let _this = this;
      _this.dowmloadLoading = true;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = [
          '姓名',
          'git用户名',
          '公司',
          '角色',
          '小组',
          '总行数',
          '开始时间',
          '结束时间'
        ];
        const filterVal = [
          'nickName',
          'userName',
          'companyname',
          'rolename',
          'groupname',
          'total',
          'startDate',
          'endDate'
        ];
        const data = _this.formatJson(filterVal);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: 'Git代码提交汇总',
          bookType: 'xlsx'
        });
        _this.dowmloadLoading = false;
      });
    },
    changePagination(props) {
      let { page, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.onTableRequest();
    },
    async onTableRequest(val, type) {
      const selectedQuery = {
        start: this.timeStartDate,
        end: this.timeEndDate,
        company: this.updateCompany,
        area: this.updateArea,
        role: this.updateRole,
        isLeave: this.updateIsLeave
      };
      if (selectedQuery[type]) {
        selectedQuery[type](val);
      }
      const paramsValue = Array.from(new Set(Object.values(this.params)));
      if (paramsValue.length === 1 && !paramsValue[0]) {
        this.tableUsers = [];
        return;
      }
      this.init();
    },
    async init() {
      await this.queryGitlabCommitInfo(this.params);
      this.tableUsers = this.gitlabCommitInfo.result;
      this.pagination.rowsNumber = this.gitlabCommitInfo.total;
    },
    codeRuleOpenHandle() {
      this.codeRuleOpen = true;
    },
    getUserDetail(data) {
      this.$router.push({ path: '/dashboard/code/detail' });
      sessionStorage.setItem('codeStatisticsDetail', JSON.stringify(data));
    },
    formatSelectDisplay,
    // 开始时间可选范围
    startTimeOptions(date) {
      if (this.time.end_date) {
        return date < this.time.end_date.replace(/-/g, '/');
      }
      return true;
    },
    //结束时间可选范围
    endTimeOptions(date) {
      this.time.start_date = this.time.start_date ? this.time.start_date : '';
      return date > this.time.start_date.replace(/-/g, '/');
    },
    appendNode(parent, set, depth) {
      // 当参数设置depth=2，限制层级最多为4级
      if (!Array.isArray(parent) || !Array.isArray(set)) {
        return [];
      }
      if (parent.length === 0 || set.length === 0) {
        return [];
      }
      if (depth === 0) {
        return parent;
      }

      const child = parent.reduce((pre, next) => {
        const nodes = set.filter(group => group.parent === next.id);
        nodes.forEach(node => (node.header = 'nodes'));

        next.children = nodes;
        return pre.concat(nodes);
      }, []);

      if (child.length > 0) {
        this.appendNode(child, set, --depth);
      }

      return parent;
    },
    wrapTotal(options, name) {
      return [{ label: name, value: '' }].concat(options);
    },
    //查询条件：公司/角色/是否在职
    requestConditions(val, type) {
      if (type === 'isLeave') {
        this.updateTableIsLeave(val);
      } else if (type === 'role') {
        this.updatetableRole(val);
      } else if (type === 'company') {
        this.updateTableCompany(val);
      }
      this.onTableRequest();
    }
  },
  async created() {
    this.showLoading = true;
    axios
      .get('/fdev-configserver/myapp/default/master/gitwork-rule.json')
      .then(res => {
        this.ruleList.question = res.data.question.split('/n');
        this.ruleList.ruleContent = res.data.ruleContent.split('/n');
      })
      .catch(err => {});
    if (sessionStorage.getItem('nodes')) {
      const { nodes, selected, expanded } = JSON.parse(
        sessionStorage.getItem('nodes')
      );
      this.nodes = nodes;
      this.selected = selected;
      this.expanded = expanded;
      this.tableUsers = this.gitlabCommitInfo.result;
      this.pagination.rowsNumber = this.gitlabCommitInfo.total;
    } else {
      await this.init();
    }
    await this.fetchRole();
    await this.fetchGroup({ status: '1' });
    this.currentGroups = deepClone(this.groups);
    this.fetchCompany();
    this.queryArea();
    this.showLoading = false;
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('nodes');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem(
      'nodes',
      JSON.stringify({
        nodes: this.nodes,
        selected: this.selected,
        expanded: this.expanded
      })
    );
    next();
  }
};
</script>

<style lang="stylus" scoped>

.node
  .group-operation
    position: relative;
    opacity: 0.1;
  &:hover
    .group-operation
      opacity: 0.9;

.tree-group
  .q-tree__node-header
    transition: .4s;
    &.q-tree__node--selected
      color: #ffffff;
      background: #0663BE;
      &:before
        border-color: #0663BE !important;
      .node-badge
        background: #ffffff !important;
        color: #0663BE !important;
      .group-operation
        opacity: 1;

.splitter
  min-height: calc(100vh - 299px);
.td-width
	max-width 150px
	overflow hidden
	text-overflow ellipsis
  wihte-space nowrap
.text
  font-family: PingFangSC-Regular;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight:bold
.fontStyle
  font-size 14px
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
</style>
