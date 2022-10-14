<template>
  <div>
    <Loading :visible="showLoading">
      <fdev-splitter
        v-model="splitter"
        class="splitter"
        :horizontal="!$q.platform.is.desktop"
        :disable="!$q.platform.is.desktop"
      >
        <!--机构树 -->
        <template v-slot:before>
          <fdev-splitter v-model="innerSplitter" disable horizontal>
            <template v-slot:before>
              <div class="q-pr-md tree-group">
                <fdev-tree
                  :nodes="nodes"
                  node-key="id"
                  :expanded.sync="expanded"
                  ref="tree"
                  accordion
                  :selected.sync="selected"
                  selected-color="blue-9"
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
              titleIcon="list_s_f"
              no-export
            >
              <!-- 操作按钮  -->
              <template v-slot:top-right>
                <fdev-btn
                  label="代码统计规则"
                  normal
                  ficon="eye"
                  @click="codeRuleOpen = true"
                />
                <fdev-btn
                  label="导出"
                  normal
                  ficon="exit"
                  :loading="downloadLoading"
                  @click="handleDownload"
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
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label-style="width:60px"
                  label="统计范围"
                >
                  <fdev-select
                    multiple
                    map-options
                    clearable
                    emit-value
                    option-label="label"
                    option-value="value"
                    :value="tableAppType"
                    :options="appOptions"
                    @input="onTableRequest($event, 'tableAppType')"
                  />
                </f-formitem>
                <f-formitem
                  class="col-6 q-pr-md"
                  bottom-page
                  label-style="width:60px"
                  label="是否包含子组"
                >
                  <div>
                    <fdev-toggle
                      v-model="tableContainSubGroup"
                      left-label
                      :true-value="true"
                      :false-value="false"
                      @input="onTableRequest($event, 'includeChild')"
                    />
                    <span class="text-grey-8">
                      {{ tableContainSubGroup ? '是' : '否' }}
                    </span>
                  </div>
                </f-formitem>
              </template>
              <!-- 姓名 -->
              <template v-slot:body-cell-userNameCn="props">
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
              <template v-slot:body-cell-role="props">
                <fdev-td class="td-width" :title="showTitleNames(props.value)">
                  <span v-for="(item, index) in props.value" :key="index">{{
                    item + ','
                  }}</span>
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ showTitleNames(props.value) }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </fdev-td>
              </template>
              <!-- 详情 -->
              <template v-slot:body-cell-detail="props">
                <fdev-td v-if="props.row.userNameEn">
                  <router-link
                    :to="{
                      path: `/staticReport/codeRelate/detail/${
                        props.row.email
                      }`,
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
          <!-- 统计规则 -->
          <div>
            <div class="row no-wrap">
              <f-icon
                name="notepad"
                class="text-primary"
                style="margin-right:8px"
              />
              <p class="text">代码统计规则</p>
            </div>
            <div
              v-for="item in ruleList.ruleContent"
              :key="item"
              class="fontStyle q-pb-sm"
            >
              {{ item }}
            </div>
          </div>
          <!-- 常见问题 -->
          <div class="q-pt-md">
            <div class="row no-wrap">
              <f-icon
                name="help_c_o"
                class="text-primary"
                style="margin-right:8px"
              />
              <p class="text">常见问题</p>
            </div>

            <div
              v-for="item in ruleList.question"
              :key="item"
              class="fontStyle q-pb-sm"
            >
              {{ item }}
            </div>
          </div>
        </div>
      </f-dialog>
    </Loading>
  </div>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { setSplitter, getSplitter } from '@/modules/User/utils/setting';
import { deepClone, appendNode } from '@/utils/utils';
import { appOptions } from '@/modules/Measure/utils/constants';
import { exportExcel, resolveResponseError } from '@/utils/utils';
import { exportCommitStatistics } from '@/modules/Measure/services/methods.js';
import {
  getCodePagination,
  setCodePagination
} from '@/modules/Measure/utils/setting';
import axios from 'axios';
export default {
  name: 'CodeStatistics',
  components: { Loading },
  data() {
    return {
      tableContainSubGroup: false,
      appOptions: appOptions(),
      showLoading: false,
      selectId: '',
      tableUsers: [],
      tableUsersImport: [], //表格导出数据
      userColumns: [
        {
          name: 'userNameCn',
          label: '姓名',
          align: 'left',
          field: 'userNameCn'
        },
        {
          name: 'userNameEn',
          label: 'git用户名',
          align: 'left',
          field: 'userNameEn'
        },
        {
          name: 'company',
          label: '公司',
          align: 'left',
          field: 'company',
          copy: true
        },
        {
          name: 'role',
          label: '角色',
          align: 'left',
          field: 'role',
          copy: true
        },
        {
          name: 'group',
          label: '小组',
          align: 'left',
          field: 'group',
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
      ], //树的根节点
      expanded: ['root'], //展开
      selected: '',
      splitter: getSplitter() || 30,
      innerSplitter: 70,
      currentGroups: [],
      isLeaveOptions: [
        { label: '全部', value: '' },
        { label: '在职', value: '0' },
        { label: '离职', value: '1' }
      ],
      pagination: {
        page: 1,
        rowsPerPage: getCodePagination().rowsPerPage || 5,
        rowsNumber: 5
      }, //分页
      downloadLoading: false,
      codeRuleOpen: false,
      ruleList: {
        ruleContent: [],
        question: []
      } //代码统计规则
    };
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
    // 分页做原进原出
    'pagination.rowsPerPage': function(val) {
      setCodePagination({ rowsPerPage: val });
    },
    // 左侧树选择时重新获取数据
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
    // 分隔线
    splitter(val) {
      setSplitter(val);
    }
  },
  computed: {
    ...mapState('userActionSaveMeasure/codeStatistics', [
      'time',
      'tableCompany',
      'tableArea',
      'tableRole',
      'tableIsLeave',
      // 'tableContainSubGroup',
      'visibleColumns',
      'tableAppType'
    ]),
    ...mapState('userForm', {
      groups: 'groupsAll',
      companies: 'companies',
      roles: 'roles',
      areaList: 'areaList'
    }),
    ...mapState('global', ['loading']),
    ...mapState('measureForm', ['gitlabCommitInfo']),
    // 公司下拉列表
    tableCompanies() {
      return this.wrapTotal(this.companies, '全部');
    },
    // 地区下拉列表
    tableAreaList() {
      const areas = this.areaList.map(area => {
        return { label: area.name, value: area.id };
      });
      return this.wrapTotal(areas, '全部');
    },
    // 角色下拉列表
    tableRoles() {
      const roles = this.roles.map(role => {
        return { label: role.name, value: role.id };
      });
      return this.wrapTotal(roles, '全部');
    },
    // 判断是否是浦发的人员
    isCompany() {
      const { tableCompany, companies } = this;
      const company = companies.find(item => item.id === tableCompany);
      return company && company.label === '浦发';
    },
    group() {
      let group = this.currentGroups.find(group => group.id === this.selected);
      return group || {};
    },
    // 查询参数
    params() {
      return {
        startDate: this.time.start_date || '',
        endDate: this.time.end_date || '',
        includeChild: this.tableContainSubGroup,
        user: {
          groupIds: this.selectId
            ? [this.selectId]
            : ['5c81c4d0d3e2a1126ce30049'],
          roleId: this.tableRole ? [this.tableRole] : [],
          companyId: this.tableCompany,
          status: this.tableIsLeave,
          area: this.isCompany ? this.tableArea : ''
        },
        page: {
          index: this.pagination.page,
          size: this.pagination.rowsPerPage
        },
        statisticRange: this.tableAppType
      };
    }
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/codeStatistics', [
      'timeStartDate',
      'timeEndDate',
      'updateCompany',
      'updateArea',
      'updateRole',
      'updateIsLeave',
      'updateIsContainSubGroup',
      'updateVisibleColumns',
      'updateAppType'
    ]),
    ...mapActions('userForm', {
      fetchGroup: 'fetchGroupAll',
      fetchCompany: 'fetchCompany',
      fetchRole: 'fetchRole',
      queryArea: 'queryArea'
    }),
    ...mapActions('measureForm', ['queryCommitStatistics']),
    // 处理导出数据格式
    formatJson(filterVal) {
      return this.tableUsersImport.map(row => {
        return filterVal.map(col => {
          return row[col];
        });
      });
    },
    // 导出功能
    async handleDownload() {
      this.tableUsersImport = [];
      let _this = this;
      _this.downloadLoading = true;
      let result = await resolveResponseError(() =>
        exportCommitStatistics({
          startDate: _this.time.start_date || '',
          endDate: _this.time.end_date || '',
          statisticRange: _this.tableAppType,
          includeChild: _this.tableContainSubGroup,
          user: {
            groupIds: _this.selectId
              ? [_this.selectId]
              : ['5c81c4d0d3e2a1126ce30049'],
            roleId: _this.tableRole ? [_this.tableRole] : [],
            companyId: _this.tableCompany,
            status: _this.tableIsLeave,
            area: _this.isCompany ? _this.tableArea : ''
          }
        })
      );
      exportExcel(result, 'Git代码提交汇总.xlsx');
      _this.downloadLoading = false;
    },
    // 分页查询
    changePagination(props) {
      let { page, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.onTableRequest();
    },
    // 查询条件修改触发更新并更新查询条件
    async onTableRequest(val, type) {
      const selectedQuery = {
        start: this.timeStartDate,
        end: this.timeEndDate,
        company: this.updateCompany,
        area: this.updateArea,
        role: this.updateRole,
        isLeave: this.updateIsLeave,
        tableAppType: this.updateAppType
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
    // 初始化
    async init() {
      this.showLoading = true;
      await this.queryCommitStatistics(this.params);
      this.tableUsers = this.gitlabCommitInfo.data;
      this.pagination.rowsNumber = this.gitlabCommitInfo.total;
      this.showLoading = false;
    },
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
    appendNode(parent, set) {
      return appendNode(parent, set);
    },
    // 给下拉框增加“全部”选项
    wrapTotal(options, name) {
      return [{ label: name, value: '' }].concat(options);
    },
    //展示角色
    showTitleNames(names) {
      let sName = '';
      if (names.length > 0) {
        for (let i = 0; i < names.length; i++) {
          sName += `${names[i]} , `;
        }
        sName = sName.substring(0, sName.length - 1);
      }
      return sName;
    },
    // 离开页面时做原进原出
    saveData() {
      sessionStorage.setItem(
        'nodes',
        JSON.stringify({
          nodes: this.nodes,
          selected: this.selected,
          expanded: this.expanded
        })
      );
    }
  },
  async created() {
    this.showLoading = true;
    //获取代码统计规则
    axios
      .get('/fdev-configserver/myapp/default/master/gitwork-rule.json')
      .then(res => {
        this.ruleList.question = res.data.question.split('/n');
        this.ruleList.ruleContent = res.data.ruleContent.split('/n');
      })
      .catch(err => {});
    // 获取小组
    await this.fetchGroup({ status: '1' });
    this.currentGroups = deepClone(this.groups);
    // 树原进原出赋值
    if (sessionStorage.getItem('nodes')) {
      const { nodes, selected, expanded } = JSON.parse(
        sessionStorage.getItem('nodes')
      );
      this.nodes = nodes;
      //如果没有选小组则默认选择“互联网”
      this.selected = selected ? selected : '5c81c4d0d3e2a1126ce30049';

      this.expanded = expanded;
      this.tableUsers = this.gitlabCommitInfo.result;
      this.pagination.rowsNumber = this.gitlabCommitInfo.total;
    } else {
      //如果没有选小组则默认选择“互联网”
      if (!this.selected) {
        this.selected = '5c81c4d0d3e2a1126ce30049';
      }
      await this.init();
    }
    // 查询角色
    await this.fetchRole();
    // 获取公司列表
    this.fetchCompany();
    //查询地区
    this.queryArea();
    this.showLoading = false;
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

.splitter
  min-height: calc(100vh - 299px);
.td-width
	max-width 150px
	overflow hidden
	text-overflow ellipsis
  wihte-space nowrap
.text
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight:600
.fontStyle
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
</style>
