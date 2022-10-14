<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        title="应用列表"
        class="my-sticky-column-table"
        :data="currentPageAppData"
        :columns="columns"
        :pagination.sync="pagination"
        @request="onTableRequest"
        row-key="name"
        :visible-columns="visibleColumns"
        :onSelectCols="updateVisibleColumns"
        title-icon="list_s_f"
        :export-func="handleDownload"
      >
        <template v-slot:top-right>
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
          <f-formitem label="所属系统" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              use-input
              :value="system"
              @input="updateSystem($event)"
              :options="filterappSystem"
              @filter="appsystemInputFilter"
              option-label="name"
              option-value="id"
          /></f-formitem>
          <f-formitem label="中/英文名" class="col-4 q-pr-sm" bottom-page>
            <fdev-input
              :value="terms"
              @input="updateTerms($event)"
              @keyup.enter.native="pageQuery"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="cursor-pointer"
                  @click="pageQuery"
                />
              </template>
            </fdev-input>
          </f-formitem>
          <f-formitem
            label="标签/重要度(以逗号隔开)"
            class="col-4 q-pr-sm"
            bottom-page
          >
            <fdev-input
              :value="label"
              @input="updateLabel($event)"
              @keyup.enter.native="pageQuery"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="cursor-pointer"
                  @click="pageQuery"
                />
              </template>
            </fdev-input>
          </f-formitem>
          <f-formitem label="状态" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              :value="appType"
              @input="updateAppType($event)"
              :options="appTypeOptions"
              option-value="val"
              option-label="label"
              map-options
              emit-value
            />
          </f-formitem>
          <f-formitem label="小组" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              use-input
              @input="updateSelectGroup($event)"
              :value="selectGroup"
              :options="showGroupData"
              @filter="groupFilter"
              option-value="id"
              option-label="name"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name">{{
                      scope.opt.name
                    }}</fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.fullName">
                      {{ scope.opt.fullName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem label="应用类型" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              @input="updateTypeName($event)"
              :value="typeName"
              :options="appTypeData"
              use-input
            />
          </f-formitem>
          <f-formitem
            label="应用负责人/行内负责人"
            class="col-4 q-pr-sm"
            bottom-page
          >
            <fdev-select
              use-input
              @input="updateChargePerson($event)"
              :value="chargePerson"
              :options="users"
              option-value="id"
              option-label="name"
              map-options
              emit-value
              @filter="userFilter"
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
          </f-formitem>
        </template>

        <template v-slot:body-cell-system="props">
          <fdev-td class="text-ellipsis" :title="props.value"
            >{{ props.value || '-' }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-name_zh="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            <router-link :to="`/app/list/${props.row.id}`" class="link">
              {{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-name_en="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            <router-link :to="`/app/list/${props.row.id}`" class="link">
              {{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-interface="props">
          <fdev-td class="cursor-pointer text-primary" title="接口列表">
            <router-link
              :to="`/app/list/${props.row.id + '-interface'}`"
              class="link"
            >
              接口列表
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-hangnei="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.spdb_managers | spdbManagers"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-if="item.id"
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
              >
                {{ item.user_name_cn }}
              </router-link>
              <span v-else class="span-margin" :title="item.user_name_cn">
                {{ item.user_name_cn || '-' }}
              </span>
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-yingyong="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.dev_managers | devManagers"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-if="item.id"
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
              >
                {{ item.user_name_cn }}
              </router-link>
              <span v-else class="span-margin">
                {{ item.user_name_cn || '-' }}
              </span>
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-label="props">
          <fdev-td>
            <fdev-chip
              v-for="(item, index) in props.value"
              :key="index"
              color="teal"
              text-color="white"
            >
              {{ item }}
            </fdev-chip>
          </fdev-td>
        </template>
        <template v-slot:body-cell-git="props">
          <fdev-td>
            <div class="text-ellipsis" :title="props.row.git">
              <a :href="props.row.git" target="_blank" class="text-primary">
                {{ props.row.git }}
              </a>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { formatOption } from '@/utils/utils';
import { setPagination, getPagination } from '@/modules/App/utils/setting.js';
import { appListColumns } from '@/modules/App/utils/constants';
export default {
  name: 'AppList',
  components: { Loading },
  data() {
    return {
      loading: false,
      dowmloadLoading: false,
      currentPageAppData: [],
      totalPageAppData: [],
      selected: [],
      filter: '',
      appTypeOptions: [{ label: '使用', val: '' }, { label: '废弃', val: '0' }],
      pagination: {
        sortBy: 'group',
        descending: false,
        page: 1,
        rowsPerPage: getPagination().rowsPerPage || 5,
        rowsNumber: 10
      },
      users: [],
      chargeOptions: [],
      groupsData: [],
      showGroupData: [],
      filterappSystem: []
    };
  },
  filters: {
    spdbManagers(val) {
      return val.length === 0
        ? '-'
        : val.map(item => item.user_name_cn).join(',');
    },
    devManagers(val) {
      return val.length === 0
        ? '-'
        : val.map(item => item.user_name_cn).join(',');
    }
  },
  watch: {
    'pagination.rowsPerPage': function(val) {
      setPagination({ rowsPerPage: val });
    },
    system(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.pageQuery();
      }
    },
    terms(val) {
      if (val === '') {
        this.pageQuery();
      }
    },
    label(val) {
      if (val === '') {
        this.pageQuery();
      }
    },
    appType(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.pageQuery();
      }
    },
    selectGroup(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.pageQuery();
      }
    },
    typeName(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.pageQuery();
      }
    },
    chargePerson(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.pageQuery();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveApp/appList', [
      'system',
      'terms',
      'label',
      'appType',
      'selectGroup',
      'typeName',
      'chargePerson',
      'visibleColumns'
    ]),
    ...mapState('user', {
      userList: 'list'
    }),
    ...mapState('appForm', [
      'appData',
      'rowsNumber',
      'appTypeData',
      'sortBy',
      'appSystemList'
    ]),
    ...mapState('userForm', ['groups']),
    ...mapGetters('user', ['isLoginUserList']),
    columns() {
      const columns = appListColumns();
      if (this.appType === '') {
        columns.push({ name: 'interface', label: '接口列表', align: 'left' });
      }
      return columns;
    }
  },
  methods: {
    ...mapMutations('userActionSaveApp/appList', [
      'updateSystem',
      'updateTerms',
      'updateLabel',
      'updateAppType',
      'updateSelectGroup',
      'updateTypeName',
      'updateChargePerson',
      'updateVisibleColumns'
    ]),

    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('appForm', {
      queryPagination: 'queryPagination',
      queryAppType: 'queryAppType',
      queryAppSystem: 'queryAppSystem'
    }),
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup'
    }),
    appsystemInputFilter(val, update) {
      update(() => {
        this.filterappSystem = this.appSystemList.filter(
          tag => tag.name.indexOf(val) > -1
        );
      });
    },
    async pageQuery() {
      this.loading = true;
      await this.onTableRequest({ pagination: this.pagination });
      this.loading = false;
    },
    async onTableRequest(props) {
      let { page, rowsPerPage, sortBy, descending } = props.pagination;
      //sortBy被q-table清空时的处理
      if (!sortBy) {
        sortBy = this.sortBy;
        descending = !descending;
      }
      let params = {
        size: rowsPerPage,
        index: page,
        keyword: this.terms,
        status: this.appType,
        groupId: this.selectGroup ? this.selectGroup.id : '',
        typeId: this.typeName ? this.typeName.value : '',
        label: this.label ? this.label.split(',').filter(e => !!e) : [],
        user_id: this.chargePerson,
        sortBy,
        descending,
        system: this.system ? this.system.id : ''
      };
      await this.queryPagination(params);
      this.currentPageAppData = this.appData;
      //手动更新rowsNumber
      this.pagination.rowsNumber = this.rowsNumber;
      this.pagination.sortBy = sortBy;
      this.pagination.descending = descending;
      this.pagination = {
        ...this.pagination,
        page,
        rowsPerPage
      };
    },
    userFilter(val, update, abort) {
      update(() => {
        this.users = this.chargeOptions.filter(
          user =>
            user.name.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    groupFilter(val, update, abort) {
      update(() => {
        this.showGroupData = this.groupsData.filter(
          item => item.fullName.indexOf(val) > -1
        );
      });
    },
    formatJson(filterVal, appData) {
      return appData.map(row => {
        return filterVal.map(col => {
          if (col === 'group') {
            return row[col].name;
          } else if (col === 'spdb_managers' || col === 'dev_managers') {
            let nameList = row[col].map(item => item.user_name_cn);
            return nameList.join(' ');
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
          '应用中文名',
          '应用英文名',
          '应用所属系统',
          '小组',
          '应用类型',
          '行内应用负责人',
          '应用负责人',
          '标签/重要度'
        ];
        const filterVal = [
          'name_zh',
          'name_en',
          'systemName_cn',
          'group',
          'type_name',
          'spdb_managers',
          'dev_managers',
          'label'
        ];
        const data = _this.formatJson(
          filterVal,
          type === '全量' ? this.totalPageAppData : this.currentPageAppData
        );
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '应用列表',
          bookType: 'xlsx'
        });
      });
    },
    // 全量导出
    async handleDownloadAll() {
      this.dowmloadLoading = true;
      let { page, sortBy, descending } = this.pagination;
      //sortBy被q-table清空时的处理
      if (!sortBy) {
        sortBy = this.sortBy;
        descending = !descending;
      }
      let params = {
        size: 0,
        index: page,
        keyword: this.terms,
        status: this.appType,
        groupId: this.selectGroup ? this.selectGroup.id : '',
        typeId: this.typeName ? this.typeName.value : '',
        label: this.label ? this.label.split(',').filter(e => !!e) : [],
        user_id: this.chargePerson,
        sortBy,
        descending,
        system: this.system ? this.system.id : ''
      };
      await this.queryPagination(params);
      this.totalPageAppData = this.appData;
      await this.handleDownload('全量');
      this.dowmloadLoading = false;
    }
  },
  async created() {
    await this.queryAppSystem();
    await this.queryGroup();
    //拿到完整的label
    this.groupsData = formatOption(this.groups);
    this.showGroupData = this.groupsData.slice(0);
    await this.queryAppType();
    this.loading = true;
    await this.onTableRequest({
      pagination: this.pagination
    });
    this.loading = false;
    await this.fetchUser();
    //搜索框选项过滤
    this.users = this.isLoginUserList.filter(item => {
      let flag = false;
      const roleArr = item.role;
      roleArr.forEach(element => {
        if (
          element.name === '行内项目负责人' ||
          element.name === '厂商项目负责人'
        ) {
          flag = true;
        }
      });
      return flag;
    });
    this.chargeOptions = this.users.slice(0);
  }
};
</script>
<style lang="stylus" scoped>
.span-margin
  margin-right 25px
  .td-desc
    max-width 300px
    overflow hidden
    text-overflow ellipsis
</style>
