<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="tableUsers"
        :columns="columns"
        row-key="id"
        :pagination.sync="pagination"
        :visible-columns="visibleColumns"
        :onSelectCols="updateVisibleColumns()"
        @request="onTableRequest"
        titleIcon="member_s_f"
        title="用户列表"
        class="my-sticky-first-column-table"
        :export-func="handleDownload"
      >
        <template v-slot:top-right>
          <div v-show="hasMoreSearch">
            <div class="text-warning text-subtitle4 row items-center">
              <f-icon
                name="alert_t_f"
                :width="14"
                :height="14"
                class="q-mr-xs cursor-pointer tip"
              />
              <fdev-tooltip position="top" target=".tip">
                高级搜索内有更多的查询条件未清除
              </fdev-tooltip>
              有查询条件未清除哦->
            </div>
          </div>
          <fdev-btn
            normal
            label="高级搜索"
            @click="moreSearch = true"
            class="q-ml-sm"
            ficon="search"
          />
          <span>
            <fdev-btn
              ficon="exit"
              label="全量导出"
              normal
              :disable="dowmloadLoading"
              @click="handleDownloadAll()"
            >
            </fdev-btn>
            <fdev-tooltip position="top" v-if="dowmloadLoading">
              正在导出，请稍后
            </fdev-tooltip>
          </span>
        </template>
        <template v-slot:top-bottom>
          <f-formitem
            label="中文名称/英文名称/角色/标签"
            class="col-4 q-pr-md"
            label-style="width:100px"
            bottom-page
          >
            <fdev-select
              placeholder="请输入"
              use-input
              multiple
              hide-dropdown-icon
              :value="terms"
              @input="updateTerms($event)"
              @new-value="addTerm"
              ref="terms"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="cursor-pointer"
                  @click="addNewValue()"
                />
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="公司"
            v-show="selector.indexOf('company') >= 0"
            class="col-4 q-pr-md"
            label-style="width:100px"
            bottom-page
          >
            <fdev-select
              :display-value="
                !tableCompany || tableCompany.value == 'total'
                  ? '全部'
                  : formatSelectDisplay(tableCompanies, tableCompany)
              "
              :value="tableCompany"
              @input="updateTableCompany($event)"
              use-input
              @filter="companyInputFilter"
              :options="tableCompanies"
            />
          </f-formitem>
          <f-formitem
            label="所属小组"
            v-show="selector.indexOf('group') >= 0"
            class="col-4 q-pr-md"
            label-style="width:100px"
            bottom-page
          >
            <fdev-select
              :display-value="
                !tableGroup || tableGroup.value == 'total'
                  ? '全部'
                  : formatSelectDisplay(tableGroups, tableGroup)
              "
              :value="tableGroup"
              use-input
              @input="updateTableGroup($event)"
              @filter="groupInputFilter"
              :options="tableGroups"
            />
          </f-formitem>
          <f-formitem
            label="在职/离职"
            class="col-4 q-pr-md"
            label-style="width:100px"
            bottom-page
          >
            <fdev-select
              :display-value="
                !tableIsLeave || tableIsLeave.value == 'total'
                  ? '全部'
                  : formatSelectDisplay(isLeaveOptions, tableIsLeave)
              "
              :value="tableIsLeave"
              @input="updateTableIsLeave($event)"
              :options="isLeaveOptions"
            />
          </f-formitem>
          <f-formitem
            label="地域"
            class="col-4 q-pr-md"
            label-style="width:100px"
            bottom-page
          >
            <fdev-select
              :value="tableArea"
              @input="updateTableArea($event)"
              :options="tableAreaOptions"
              option-label="name"
              option-value="id"
              map-options
            />
          </f-formitem>
          <f-formitem
            label="所属条线"
            class="col-4 q-pr-md"
            label-style="width:100px"
            bottom-page
          >
            <fdev-select
              ref="tableSection"
              :value="tableSection"
              @input="updateTableSection($event)"
              :options="sectionAll"
              option-label="sectionNameCn"
              option-value="id"
              map-options
              clearable
            />
          </f-formitem>
        </template>

        <template v-slot:body-cell-name="props">
          <fdev-td :title="props.row.name || '-'">
            <router-link :to="`/user/list/${props.row.id}`" class="link">
              {{ props.row.name || '-' }}
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-role="props">
          <fdev-td :title="props.row.role_label">
            <div class="ellipsis">{{ props.row.role_label }}</div>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.role_label }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <template v-slot:body-cell-label="props">
          <fdev-td :title="props.row.label">
            <div class="ellipsis">{{ props.row.label || '-' }}</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-function="props">
          <fdev-td :title="props.row.function[0] && props.row.function[0].name">
            <span>
              {{ (props.row.function[0] && props.row.function[0].name) || '-' }}
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-area="props">
          <fdev-td :title="props.row.area[0] && props.row.area[0].name">
            <span v-if="props.row.area[0]">
              {{ props.row.area[0].name }}
            </span>
            <span v-else>{{ '-' }}</span>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <f-dialog
      title="更多查询条件"
      right
      v-model="moreSearch"
      @before-close="moreSearchCancel"
      @before-show="beforeShow"
    >
      <div class="q-gutter-y-lg">
        <f-formitem label="职能" diaS>
          <fdev-select
            ref="tableFunction"
            :value="tableFunction"
            @input="updateTableFunction($event)"
            :options="functionList"
            option-label="name"
            option-value="id"
            map-options
            clearable
          />
        </f-formitem>
      </div>

      <template #btnSlot>
        <fdev-btn label="清空" outline dialog @click="resetMoreSearch"/>
        <fdev-btn label="取消" outline dialog @click="moreSearchCancel"/>
        <fdev-btn label="查询" dialog @click="moreSearchClick"
      /></template>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import { required, email, integer, maxLength } from 'vuelidate/lib/validators';

import Loading from '@/components/Loading';
import moment from 'moment';
import {
  // setEditOpened,
  // getEditOpened,
  setPagination,
  getPagination
} from '@/modules/User/utils/setting';
import {
  createUserModel,
  educationOptions,
  isLeaveOptions,
  isSpdbMacOptions,
  findAuthority,
  is_party_memberOptions,
  createMemory,
  moreSearchMap
} from '@/modules/User/utils/model';
import {
  formatSelectDisplay,
  validate,
  successNotify,
  errorNotify,
  deepClone,
  wrapOptionsTotal
} from '@/utils/utils';

export default {
  name: 'List',
  components: { Loading },
  data() {
    return {
      mostate: false,
      dowmloadLoading: false,
      columns: [
        { name: 'name', label: '姓名', align: 'left', field: 'name' },
        {
          name: 'email',
          label: '邮箱',
          field: 'email',
          sortable: true,
          align: 'left'
        },
        {
          name: 'telephone',
          label: '手机号',
          field: 'telephone',
          sortable: true,
          align: 'left'
        },
        {
          name: 'group',
          label: '小组',
          field: row => row.group.name,
          sortable: true,
          align: 'left',
          copy: true
        },
        {
          name: 'role',
          label: '角色',
          field: row => row.role_label,
          align: 'left'
        },

        {
          name: 'function',
          label: '职能',
          field: row => row.function,
          align: 'left'
        },
        {
          name: 'area',
          label: '地域',
          field: row => row.area,
          align: 'left'
        },

        {
          name: 'sectionInfo',
          label: '条线',
          field: row => (row.sectionInfo ? row.sectionInfo.sectionNameCn : '-'),
          align: 'left'
        },
        {
          name: 'work_num',
          label: '工号',
          field: 'work_num',
          align: 'left'
        },
        {
          name: 'create_date',
          label: '入职时间',
          field: 'create_date',
          sortable: true,
          align: 'left'
        },
        {
          name: 'start_time',
          label: '开始工作时间',
          field: 'start_time',
          sortable: true,
          align: 'left'
        },
        {
          name: 'leave_date',
          label: '离职/换岗时间',
          field: 'leave_date',
          sortable: true,
          align: 'left'
        },
        {
          name: 'git_user',
          label: 'gitlab账号',
          field: 'git_user',
          align: 'left'
        },
        {
          name: 'company',
          label: '公司',
          field: row => row.company.name,
          sortable: true
        },
        {
          name: 'phone_type',
          label: '手机型号',
          field: 'phone_type',
          sortable: true
        },
        {
          name: 'phone_mac',
          label: '手机mac地址',
          field: 'phone_mac',
          sortable: true
        },
        {
          name: 'vm_ip',
          label: '虚机ip地址',
          field: 'vm_ip',
          sortable: true
        },
        {
          name: 'vm_name',
          label: '虚机名',
          field: 'vm_name',
          sortable: true
        },
        {
          name: 'vm_user_name',
          label: '虚机用户名/域用户',
          field: 'vm_user_name',
          sortable: true
        },
        {
          name: 'label',
          label: '标签',
          field: row => row.label,
          sortable: true,
          align: 'left'
        }
      ],
      selector: ['company', 'group'],
      filter: '',
      childrenIdresult: {},
      selection: 'multiple',
      rowSelected: [],
      addUserModalOpened: false,
      updateUserModalOpened: false,
      // editOpened: getEditOpened() || false,
      tableCompanies: [],
      deepCloneCompany: [],
      tableGroups: [],
      tableUsers: [],
      totalPageUserData: [],
      tableRoles: [],
      tableAreaOptions: [],
      userModel: createUserModel(),
      updateUserModel: createUserModel(),
      fdevAccount: '',
      moreSearch: false,
      memory: createMemory(),
      moreSearchMap,
      loading: false,
      filteredTags: [],
      permissions: [],
      filterGroup: [],
      // confirmModalOpen: false,

      groupsCopy: [],
      validated: [],
      emailAppend: ['xxx', 'xxx'],

      companiesOptions: [],
      is_party_memberOptions: is_party_memberOptions,

      newRoles: [],
      filterRoles: [],
      envManagerRole: {},
      baseArchitectureRole: {},
      deepCloneGroups: [],

      isLeaveOptions: isLeaveOptions,
      isSpdbMacOptions: isSpdbMacOptions,
      educationOptions: educationOptions,
      pagination: {
        sortBy: '',
        descending: false,
        page: 1,
        rowsPerPage: getPagination().rowsPerPage || 5,
        rowsNumber: 10
      }
    };
  },
  validations: {
    userModel: {
      name: {
        required,
        onlyWord(val) {
          if (!val) {
            return true;
          }
          let re = new RegExp(/[\u4e00-\u9fa5]/gm);
          return re.test(val.replace(/(^\s*)|(\s*$)/g, ''));
        }
      },
      git_user_id: {
        required(val) {
          if (this.newUserIsNeedGitlabId) {
            return !!val.trim();
          }
          return true;
        },
        integer
      },
      area_id: {
        required(val) {
          if (this.isSpdbUser(this.userModel)) {
            return !!val;
          }
          return true;
        }
      },
      telephone: {
        required,
        integer,
        maxLength: maxLength(11)
      },
      phone_type: {
        required
      },
      phone_mac: {
        required
      },
      is_spdb_mac: {
        required
      },
      vm_ip: {
        required
      },
      vm_name: {
        required
      },
      vm_user_name: {
        required
      },
      group: {
        required
      },
      role: {
        required
      },
      auth: {
        required
      },
      company: {
        required
      },
      isLeave: {
        required
      },
      tagSelected: {},
      email: {
        required,
        email
      },
      email_pre: {
        required,
        ldapLogin(val) {
          if (!val) {
            return true;
          }
          return this.userModel.email_append === 'xxx';
        }
      },
      email_append: {
        required
      },
      create_date: {
        required
      },
      leave_date: {},
      remark: {},
      start_time: {
        required
      },
      education: {},
      rank_id: {
        required(val) {
          if (!this.isSpdbUser(this.userModel)) {
            return !!val;
          }
          return true;
        }
      },
      function_id: {
        required
      }
    },
    updateUserModel: {
      area_id: {
        required(val) {
          if (this.isSpdbUser(this.updateUserModel)) {
            return !!val;
          }
          return true;
        }
      },
      name: {
        required
      },
      email: {
        required
      },
      telephone: {
        required,
        integer,
        maxLength: maxLength(11)
      },
      phone_type: {
        required
      },
      phone_mac: {
        required
      },
      is_spdb_mac: {
        required
      },
      vm_ip: {
        required
      },
      vm_name: {
        required
      },
      vm_user_name: {
        required
      },
      group: {
        required
      },
      role: {
        required,
        cannotModifyEnvRole(roles) {
          return this.roleRules(roles, '环境配置管理员', this.envManagerRole);
        },
        cannotModifyBaseRole(roles) {
          return this.roleRules(
            roles,
            '基础架构管理员',
            this.baseArchitectureRole
          );
        }
      },
      auth: {
        required
      },
      company: {
        required
      },
      isLeave: {
        required
      },
      tagSelected: {},
      create_date: {
        required
      },
      leave_date: {},
      remark: {},
      start_time: {
        required
      },
      education: {},
      rank_id: {
        required(val) {
          if (!this.isSpdbUser(this.updateUserModel)) {
            return !!val;
          }
          return true;
        }
      },
      function_id: {
        required
      }
    }
  },

  watch: {
    // editOpened(val) {
    //   setEditOpened(val);
    // },
    'pagination.rowsPerPage'(val) {
      setPagination({
        rowsPerPage: val
      });
    },
    'userModel.email_pre': {
      deep: true,
      handler: function(val) {
        if (val) {
          this.$v.userModel.email.$model =
            val + this.$v.userModel.email_append.$model;
        }
        this.fdevAccount = this.$v.userModel.email_pre.$model;
      }
    },
    'userModel.email_append': {
      deep: true,
      handler: function(val) {
        if (val) {
          if (this.$refs.userModel) this.$refs.userModel.reset();
          this.$v.userModel.email.$model =
            this.$v.userModel.email_pre.$model + val;
        } else {
          this.$v.userModel.email_append.$model = 'xxx';
        }
        if (this.$v.userModel.email_append.$model === 'xxx') {
          this.$v.userModel.company.$model = this.companies.find(
            com => com.name === '浦发'
          );
        } else {
          this.$v.userModel.company.$model = '';
          this.companiesOptions = this.companies.filter(company => {
            return company.name !== '浦发';
          });
        }
      }
    },
    queryParam: {
      deep: true,
      handler(val) {
        this.onTableRequest({ pagination: this.pagination });
      }
    }
  },
  computed: {
    ...mapState('userActionSaveUser/userQuery', [
      'terms',
      'tableCompany',
      'tableGroup',
      'tableRole',
      'tableIsLeave',
      'tableIsParty',
      'tableArea',
      'visibleColumns',
      'tableFunction',
      'tableSection'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      users: 'list',
      currentUser: 'currentUser',
      simulateUserAuth: 'simulateUserAuth'
    }),
    ...mapState('authorized', {
      auths: 'list'
    }),
    ...mapState('managementForm', ['sectionAll']),
    ...mapState('userForm', [
      'groups',
      'companies',
      'roles',
      'isLeaves',
      'tags',
      'childGroup',
      'areaList',
      'functionList',
      'rankList',
      'userInPage',
      'reBuildGroupName'
    ]),
    ...mapGetters('userForm', ['wrapTotal']),
    ...mapGetters('authorized', ['checkPermissions', 'checkTransPermissions']),

    queryParam() {
      const search = this.terms;
      let status = '',
        company_id = '',
        group_id = '',
        is_party_member = '',
        area_id = '',
        permission_id = '',
        function_id = '',
        section = '';

      if (this.tableCompany && this.tableCompany.value !== 'total') {
        company_id = this.tableCompany.value;
      }
      if (this.tableGroup && this.tableGroup.value !== 'total') {
        group_id = this.tableGroup.value;
      }
      if (this.tableIsLeave && this.tableIsLeave.value !== 'total') {
        status = this.tableIsLeave.value;
      }
      if (this.tableIsParty && this.tableIsParty.value !== 'total') {
        is_party_member = this.tableIsParty.value;
      }
      if (this.tableArea && this.tableArea.id !== 'total') {
        area_id = this.tableArea.id;
      }
      if (this.tableFunction && this.tableFunction.id !== 'total') {
        function_id = this.tableFunction.id;
      }
      if (this.tableSection && this.tableSection.id !== 'total') {
        section = this.tableSection.id;
      }
      if (this.tableRole && this.tableRole.value !== 'total') {
        permission_id = this.tableRole.value;
      }

      return {
        company_id,
        group_id,
        permission_id,
        status,
        is_party_member,
        area_id,
        function_id,
        section,
        search
      };
    },
    hasMoreSearch() {
      return !this.moreSearch && !!this.tableFunction;
    },
    isAdmin() {
      return (
        this.currentUser.user_name_en === 'admin' ||
        findAuthority(this.currentUser) === 'admin'
      );
    },
    newUserIsNeedGitlabId() {
      if (!this.userModel.group) {
        return true;
      }
      return (
        this.userModel.group.name !== '测试中心' &&
        this.userModel.group.name !== '业务部门' &&
        this.userModel.group.name !== '规划部门'
      );
    }
  },
  methods: {
    ...mapMutations('userActionSaveUser/userQuery', [
      'updateTerms',
      'updateTableCompany',
      'updateTableGroup',
      'updateTableRole',
      'updateTableIsLeave',
      'updateVisibleColumns',
      'updateTableIsParty',
      'updateTableArea',
      'updateTableFunction',
      'updateTableSection'
    ]),
    ...mapActions('managementForm', ['queryAllSection']),
    ...mapActions('user', {
      fetchUser: 'fetch',
      addUser: 'add',
      removeUser: 'remove',
      updateUser: 'update',
      fetchCurrent: 'fetchCurrent',
      simulateUser: 'simulateUser'
    }),
    ...mapActions('userForm', [
      'fetchGroup',
      'fetchCompany',
      'fetchRole',
      'fetchAuth',
      'fetchTag',
      'addTag',
      'removeTag',
      'queryChildGroupById',
      'queryArea',
      'queryFunction',
      'queryRank',
      'queryUserPagination'
    ]),
    ...mapActions('authorized', {
      fetchAuth: 'fetch'
    }),
    formatJson(filterVal, appData) {
      return appData.map(row => {
        return filterVal.map(col => {
          if (col === 'group' || col === 'company') {
            return row[col].name;
          } else if (col === 'function' || col === 'role' || col === 'area') {
            let functionList = row[col].map(item => item.name);
            return functionList.join(' ');
          } else {
            return row[col];
          }
        });
      });
    },
    handleDownload(type) {
      let _this = this;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = [
          '姓名',
          '域名',
          '邮箱',
          '手机号',
          '小组',
          '角色',
          '职能',
          '地域',
          '学历',
          '入职时间',
          '开始工作时间',
          '离职/换岗时间',
          'gitlab账号',
          '公司',
          '手机型号',
          '手机mac地址',
          '虚机ip地址',
          '虚机名',
          '虚机用户名/域用户',
          '标签'
        ];
        const filterVal = [
          'name',
          'user_name_en',
          'email',
          'telephone',
          'group',
          'role',
          'function',
          'area',
          'education',
          'create_date',
          'start_time',
          'leave_date',
          'git_user',
          'company',
          'phone_type',
          'phone_mac',
          'vm_ip',
          'vm_name',
          'vm_user_name',
          'label'
        ];
        const data = _this.formatJson(
          filterVal,
          type === '全量' ? this.totalPageUserData : this.tableUsers
        );
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '用户列表',
          bookType: 'xlsx'
        });
      });
    },
    //全量导出
    async handleDownloadAll() {
      this.dowmloadLoading = true;
      let { page, sortBy, descending } = this.pagination;
      this.pagination.page = page;
      this.pagination.sortBy = sortBy;
      this.pagination.descending = descending;
      //sortBy被q-table清空时的处理
      if (!sortBy) {
        sortBy = this.sortBy;
        descending = !descending;
      }
      await this.queryUserPagination({
        ...this.queryParam,
        page,
        per_page: 0
      });
      this.totalPageUserData = this.userInPage.list.slice(0);
      await this.handleDownload('全量');
      this.dowmloadLoading = false;
    },
    async onTableRequest({ pagination }) {
      let { page, rowsPerPage, sortBy, descending } = pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.pagination.sortBy = sortBy;
      this.pagination.descending = descending;
      await this.queryUserPagination({
        ...this.queryParam,
        page,
        per_page: rowsPerPage
      });
      this.tableUsers = this.userInPage.list.slice(0);
      if (!sortBy) {
        sortBy = this.pagination.sortBy;
        descending = !descending;
      }
      if (!descending) {
        this.tableUsers = this.tableUsers.reverse();
      }
      this.tableUsers.forEach((item, index) => {
        let role_label = '';
        item.role_label = '';
        item.role.forEach(ele => {
          role_label += ele.name + ',';
        });
        item.role_label = role_label.substring(0, role_label.length - 1);
        let label = '';
        item.label = '';
        item.user_label.forEach(ele => {
          label += ele.name + ',';
        });
        item.label = label.substring(0, label.length - 1);
      });
      this.pagination.rowsNumber = this.userInPage.total;
    },

    formatSelectDisplay,

    beforeShow() {
      for (const key in this.memory) {
        this.memory[key] = this[key];
      }
    },
    stateDiff() {
      for (const key in this.memory) {
        this[key] !== this.memory[key] &&
          this[this.moreSearchMap[key]](this.memory[key]);
      }
    },
    moreSearchCancel() {
      // 与弹窗的初始值进行对比赋值，还原
      this.stateDiff();
      this.moreSearch = false;
    },
    moreSearchClick() {
      this.moreSearch = false;
      this.onTableRequest({ pagination: this.pagination });
    },
    resetMoreSearch() {
      this.tableFunction !== '' && this.updateTableFunction('');
      this.moreSearch = false;
      this.$nextTick(() =>
        this.onTableRequest({ pagination: this.pagination })
      );
    },
    simulate() {
      let name = this.rowSelected[0].user_name_cn;
      this.$q
        .dialog({
          title: '木偶人',
          message: `将模拟该用户(${name})进行后续操作，是否确认？`,
          ok: '确认',
          cancel: '取消'
        })
        .onOk(async () => {
          let user_name_en = this.rowSelected[0].user_name_en;
          await this.simulateUser({ user_name_en });
          successNotify(
            `模拟用户(${this.rowSelected[0].user_name_cn})登录成功`
          );
        });
    },
    roleRules(roles, label, oldRole) {
      if (findAuthority(this.currentUser) === 'admin') {
        let role = roles.find(role => {
          return role.label === label;
        });
        if ((oldRole && !role) || (!oldRole && role)) {
          return false;
        } else {
          return true;
        }
      } else {
        return true;
      }
    },
    async handleAddTag(val, done) {
      if (!this.checkTransPermissions(['/api/label/add'])) {
        errorNotify('无权限新增标签');
        return;
      }
      if (val.length > 0) {
        if (!this.tags.some(tag => tag.label === val)) {
          await this.addTag({
            name: val
          });
          let tag = this.tags.find(tag => tag.label === val);
          done(tag);
        }
      }
    },
    // 当前登录人员不是超级管理员时，新增和修改的用户权限只能是user
    initPermission() {
      let user = this.currentUser;
      let name = user.auth.name_en;
      if (name != 'admin') {
        for (var index in this.auths) {
          let item = this.auths[index];
          if (item.name_en == 'user' || item.name_en === 'group_admin') {
            this.permissions.push(item);
          }
        }
      } else {
        this.permissions = this.auths;
      }
    },

    handleUpdateUserAllTip() {
      this.$v.updateUserModel.$touch();
      // 获取要校验的dom并传入validate，此步骤要求每个ref唯一
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('updateUserModel') > -1;
      });
      // validate(
      //   Keys.map(key => {
      //     return this.$refs[key];
      //   })
      // );

      // // 只要有校验未通过的，$invalid会被validate设为true
      // if (this.$v.updateUserModel.$invalid) {
      //   return;
      // }
      validate(Keys.map(key => this.$refs[key]));
      if (this.$v.updateUserModel.$invalid) {
        let _this = this;
        let validateRes = Keys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.updateUserModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      return true;
    },
    async handleAddUser() {
      let params = {
        ...this.userModel,
        is_spdb: this.isSpdbUser(this.userModel) ? true : false
      };
      await this.addUser(params);
      successNotify('添加成功');
      await this.onTableRequest({ pagination: this.pagination });
      this.addUserModalOpened = false;
    },
    async handleUpdateUser() {
      if (this.handleUpdateUserAllTip()) {
        let params = {
          ...this.updateUserModel,
          is_spdb: this.isSpdbUser(this.updateUserModel) ? true : false
        };

        await this.updateUser(params);
        successNotify('修改成功');
        if (this.currentUser.id === this.updateUserModel.id) {
          await this.fetchCurrent();
        }
        await this.onTableRequest({ pagination: this.pagination });
        this.updateUserModalOpened = false;
        this.rowSelected = [];
      }
    },

    async handleRemoveUser() {
      // 删除弹窗
      this.$q
        .dialog({
          title: '删除用户',
          message: '您确定要删除该用户么？',
          ok: '确认',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.removeUser(this.rowSelected);
          successNotify('删除成功');
          this.rowSelected = [];
          await this.onTableRequest({ pagination: this.pagination });
        });
    },

    getChildGroupId(groupIds, parentGroupId) {
      //获取子组id
      let childIds = groupIds.filter(
        groupId => groupId.parent_id === parentGroupId
      );
      for (var i = 0; i < childIds.length; i++) {
        this.childrenIdresult[childIds[i].name] = childIds[i].id;
      }
      if (childIds.length > 0) {
        childIds.forEach(childId => {
          this.getChildGroupId(groupIds, childId.id);
        });
      }
      return this.childrenIdresult;
    },

    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },

    tagFilter(val, update) {
      update(() => {
        if (val === '') {
          this.filteredTags = this.tags;
        } else {
          const term = val.toLowerCase();
          this.filteredTags = this.tags.filter(
            tag => tag.label.toLowerCase().indexOf(term) > -1
          );
        }
      });
    },

    async handleRemoveTag(opt, model) {
      if (opt.count !== 0) {
        return this.$q
          .dialog({
            title: `删除标签`,
            message: `有 ${opt.count} 个人属于 ${opt.label} , 确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this.removeTag(opt);
            this.filteredTags = this.tags;
            successNotify('标签删除成功');
            let ind = model.tagSelected.findIndex(tag => tag.id === opt.id);
            if (ind > -1) {
              model.tagSelected.splice(ind, 1);
            }
          });
      } else {
        return this.$q
          .dialog({
            title: `删除标签`,
            message: `确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this.removeTag(opt);
            this.filteredTags = this.tags;
            successNotify('标签删除成功');
            let ind = model.tagSelected.findIndex(tag => tag.id === opt.id);
            if (ind > -1) {
              model.tagSelected.splice(ind, 1);
            }
          });
      }
    },

    groupInputFilter(val, update) {
      update(() => {
        this.tableGroups = this.filterGroup.filter(
          tag => tag.label.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },

    filterUsers() {
      let deepCloneTableUser = deepClone(this.tableUsers);
      this.tableUsers = deepCloneTableUser.filter(item => item.isLeave === '0');
    },
    addNewValue() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    roleInputFilter(val, update) {
      update(() => {
        if (this.currentUser.name === 'admin') {
          this.newRoles = this.roles;
        } else {
          this.newRoles = this.roles.filter(role => {
            return role.label !== '卡点管理员';
          });
        }
        this.newRoles = this.newRoles.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    companyInputFilter(val, update) {
      update(() => {
        this.tableCompanies = this.deepCloneCompany.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    timeOptions(date) {
      const now = moment(new Date()).format('YYYY/MM/DD');
      return date < now;
    },
    async querySelectItem() {
      this.queryArea();
      this.queryFunction();
      this.queryRank();
      this.fetchTag();
      return;
    },
    isSpdbUser({ company }) {
      return company && company.name === '浦发';
    },
    OptionsTotal(options) {
      return [
        {
          name: '全部',
          id: 'total'
        },
        ...options
      ];
    }
  },
  async created() {
    this.loading = true;
    this.fetchCompany().then(() => {
      this.tableCompanies = this.wrapTotal('companies');
      this.deepCloneCompany = wrapOptionsTotal(this.companies);
    });
    this.filterUsers();
    await this.onTableRequest({ pagination: this.pagination });
    this.queryChildGroupById({ id: this.currentUser.group.id });

    await this.fetchGroup();
    this.deepCloneGroups = deepClone(this.groups);
    this.deepCloneGroups.forEach((group, index) => {
      this.$set(this.deepCloneGroups[index], 'label', group.fullName);
    });
    this.tableGroups = wrapOptionsTotal(this.deepCloneGroups);
    this.filterGroup = wrapOptionsTotal(this.deepCloneGroups);
    this.loading = false;
    //this.initPermission();
    this.tableRoles = wrapOptionsTotal(this.auths);
    await this.fetchTag();
    await this.queryArea();
    this.tableAreaOptions = this.OptionsTotal(this.areaList);
    await this.queryFunction();
    await this.queryAllSection();
  }
};
</script>

<style lang="stylus" scoped>

.link
  i
    visibility hidden
  &:hover
    i
      visibility visible
// .min-length
//   width 330px
</style>
