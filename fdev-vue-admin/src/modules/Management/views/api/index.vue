<template>
  <!-- 等待层 -->
  <f-block page>
    <!-- :export-func="handleExportExcel" -->
    <Loading :visible="loading">
      <fdev-table
        ref="table"
        row-key="id"
        title="接口权限登记表"
        titleIcon="list_s_f"
        :data="tableLists"
        :columns="columns"
        class="my-sticky-column-table"
        :pagination.sync="pagination"
        @request="pageDemandList"
        :on-search="getApiRolesList"
        :visible-columns="visibleColumns"
        :onSelectCols="changSelect"
        :export-func="handleExportExcel"
      >
        <template #top-right>
          <span>
            <fdev-tooltip
              v-if="isDisableBtn(addButton)"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ getErrorMsg(addButton) }}</span>
            </fdev-tooltip>
            <fdev-btn
              :disable="isDisableBtn(addButton)"
              normal
              ficon="add"
              label="新增"
              @click="openAddDlg"
            />
          </span>
        </template>
        <template v-slot:top-bottom>
          <f-formitem
            label="接口路径/功能说明"
            class="col-4"
            label-style="width:118px"
            value-style="width:280px"
            bottom-page
          >
            <fdev-input
              use-input
              use-chips
              v-model="searchObj.selectKey"
              @blur="getApiRolesList"
              clearable
              @clear="getApiRolesList"
              @keyup.enter="getApiRolesList()"
            >
            </fdev-input>
          </f-formitem>
          <f-formitem
            label="角色"
            class="col-4  q-pl-md"
            label-style="width:91px;margin-left:11px"
            value-style="width:280px"
            bottom-page
          >
            <fdev-select
              multiple
              use-input
              clearable
              ref="roleIds"
              v-model="searchObj.roleIds"
              :options="roleIdOptions"
              option-label="name"
              option-value="id"
              @filter="roleIdInputFilter"
            />
          </f-formitem>
          <f-formitem
            label="状态"
            class="col-4 q-pl-md"
            bottom-page
            label-style="width:91px;margin-left:11px"
            value-style="width:280px"
          >
            <fdev-select
              clearable
              ref="status"
              v-model="searchObj.status"
              :options="statusOptions"
            >
            </fdev-select>
          </f-formitem>
        </template>
        <template v-slot:body-cell-roleIds="props">
          <fdev-td
            :title="getRolesName(props.row.roles)"
            class="text-ellipsis "
          >
            {{ getRolesName(props.row.roles) }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ getRolesName(props.row.roles) }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <template v-slot:body-cell-status="props">
          <fdev-td>
            <div
              v-if="props.row.status"
              :title="getStatusName(props.row.status)"
              class="status-style"
            >
              <span
                :class="
                  props.row.status === 'using' ? 'colorusing' : 'colordel'
                "
                class="status-img q-mr-xs"
              />
              {{ getStatusName(props.row.status) }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operation="props">
          <fdev-td :auto-width="true" class="td-padding">
            <div class="opEdit ">
              <span v-if="props.row.recoverButton != '2'">
                <fdev-tooltip
                  v-if="isDisableBtn(props.row.recoverButton)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span>{{ getErrorMsg(props.row.recoverButton) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  :disable="isDisableBtn(props.row.recoverButton)"
                  flat
                  label="恢复"
                  @click="openEditDlg(props.row, '2')"
                />
              </span>
              <span
                v-if="
                  props.row.recoverButton != '2' &&
                    props.row.updateButton != '2'
                "
                class="lflex q-mx-xs"
              ></span>
              <span v-if="props.row.updateButton != '2'">
                <fdev-tooltip
                  v-if="isDisableBtn(props.row.updateButton)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span>{{ getErrorMsg(props.row.updateButton) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  :disable="isDisableBtn(props.row.updateButton)"
                  flat
                  label="编辑"
                  @click="openEditDlg(props.row, '1')"
                />
              </span>
              <span
                v-if="
                  props.row.updateButton != '2' && props.row.deleteButton != '2'
                "
                class="lflex q-mx-xs"
              >
              </span>
              <span v-if="props.row.deleteButton != '2'">
                <fdev-tooltip
                  v-if="isDisableBtn(props.row.deleteButton)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span>{{ getErrorMsg(props.row.deleteButton) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="删除"
                  :disable="isDisableBtn(props.row.deleteButton)"
                  @click="delApiRoleById(props.row)"
                />
              </span>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <addDlg v-model="isAddDlg" :roles="roles" @close="handleApplyClose" />
    <editDlg
      v-model="isEditDlg"
      :dlgType="dtyle"
      :roles="roles"
      :getParams="curItem"
      @close="handleEditClose"
    />
  </f-block>
</template>
<script>
import Loading from '@/components/Loading';
import { mapState } from 'vuex';
import { perform } from '@/modules/Management/utils/constants';
import {
  getTableCol,
  setTableCol,
  setTableFilter,
  getTableFilter,
  setApiRolePagination,
  getApiRoleEvapagination
} from '@/modules/Management/utils/setting';
import {
  successNotify,
  resolveResponseError,
  exportExcel
} from '@/utils/utils';
import {
  exportApiRoleExcel,
  queryApiRole,
  deleteApiRoleById
} from '@/modules/Management/services/methods';
import { queryRole } from '@/services/api';
import addDlg from './components/addDlg';
import editDlg from './components/editDlg';

export default {
  name: 'ApiRole',
  components: { Loading, addDlg, editDlg },
  //离开页面之前保存数据
  beforeRouteLeave(to, from, next) {
    setTableFilter('apiRole/listFilter', JSON.stringify(this.searchObj));
    next();
  },
  watch: {
    visibleColumns(val) {
      setTableCol('apiRole/listColumns', val);
    },
    'searchObj.roleIds': {
      handler(val) {
        if (this.loading) return;
        this.getApiRolesList();
      }
    },
    'searchObj.status': {
      handler(val) {
        if (this.loading) return;
        this.getApiRolesList();
      }
    },
    'pagination.rowsPerPage'(val) {
      setApiRolePagination(val);
    }
  },
  data() {
    return {
      loading: false,
      ...perform,
      pagination: {
        sortBy: '', //排序
        descending: false,
        page: 1, //页码
        rowsPerPage: getApiRoleEvapagination(), //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },
      columns: [
        {
          name: 'interfacePath',
          label: '接口路径',
          field: 'interfacePath',
          copy: true
        },
        {
          name: 'roleIds',
          label: '角色',
          field: 'roleIds'
        },
        {
          name: 'functionDesc',
          label: '接口功能说明',
          field: 'functionDesc',
          copy: true
        },
        {
          name: 'status',
          label: '状态',
          field: 'status'
        },
        {
          name: 'operation',
          label: '操作',
          field: 'operation',
          required: true,
          align: 'left'
        }
      ],
      tableLists: [],
      searchObj: {
        selectKey: '', //接口路径/功能说明
        roleIds: [], //角色
        status: { label: '使用中', value: 'using' } //状态
      },
      roles: [],
      roleIdOptions: [], //牵头小组下拉选项
      statusOptions: [
        { label: '使用中', value: 'using' },
        { label: '已废弃', value: 'deleted' }
      ],
      visibleColumns: this.visibleColumnApiRoleOptions,
      isAddDlg: false,
      isEditDlg: false,
      curItem: {},
      dtyle: '1', //1 为编辑，2为恢复
      addButton: '1' //新增按钮，true亮，false置灰
    };
  },
  validations: {},
  computed: {
    ...mapState('user', ['currentUser'])
  },
  methods: {
    async getRole() {
      const res = await queryRole({ status: '1' });
      if (res) {
        this.roles = res;
        this.roleIdOptions = this.roles.filter(item => {
          return item;
        });
      }
    },
    getParam() {
      let queryParam = {};
      //接口路径/功能说明
      if (this.searchObj.selectKey)
        queryParam.selectKey = this.searchObj.selectKey;
      //角色
      if (this.searchObj.roleIds && this.searchObj.roleIds.length > 0) {
        queryParam.roleIds = [];
        for (let i = 0; i < this.searchObj.roleIds.length; i++) {
          queryParam.roleIds.push(this.searchObj.roleIds[i].id);
        }
      }
      //工单状态
      if (this.searchObj.status)
        queryParam.status = this.searchObj.status.value;
      return queryParam;
    },
    async handleExportExcel() {
      let queryParam = this.getParam();
      let res = await resolveResponseError(() =>
        exportApiRoleExcel(queryParam)
      );
      exportExcel(res);
    },
    async getApiRolesList() {
      this.loading = true;
      let queryParam = this.getParam();
      queryParam.pageSize = this.pagination.rowsPerPage;
      queryParam.currentPage = this.pagination.page;
      if (this.pagination.sortBy) {
        queryParam.sortBy = this.pagination.sortBy;
        queryParam.descending = this.pagination.descending;
      }
      //console.log('queryParam', JSON.stringify(queryParam));
      const res = await resolveResponseError(() => queryApiRole(queryParam));
      this.loading = false;
      //console.log('res', JSON.stringify(res));
      //测试代码
      this.tableLists = res.records;
      this.pagination.rowsNumber = res.count;
      this.addButton = res.addButton; //0可点击，1当前用户权限不足
    },
    pageDemandList(props) {
      let {
        page,
        rowsPerPage,
        rowsNumber,
        sortBy,
        descending
      } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.pagination.descending = descending;
      this.pagination.sortBy = sortBy; //排序
      this.getApiRolesList();
    },
    roleIdInputFilter(val, update) {
      update(() => {
        this.roleIdOptions = this.roles.filter(
          tag => tag.name.indexOf(val) > -1
        );
      });
    },
    changSelect(clos) {
      this.visibleColumns = clos;
    },
    openAddDlg() {
      this.isAddDlg = true;
    },
    openEditDlg(item, type) {
      this.curItem = item;
      this.isEditDlg = true;
      this.dtyle = type;
    },
    getErrorMsg(button) {
      if (button == '1') {
        return '当前用户权限不足';
      } else {
        return '';
      }
    },
    isDisableBtn(button) {
      if (button == '1') {
        return true;
      } else {
        return false;
      }
    },
    getRolesName(lists) {
      if (lists && lists.length > 0) {
        let strKey = '';
        for (let i = 0; i < lists.length; i++) {
          strKey += `${lists[i].name} 、`;
        }
        strKey = strKey.slice(0, strKey.length - 2);
        return strKey;
      }
      return '';
    },
    getStatusName(val) {
      for (let i = 0; i < this.statusOptions.length; i++) {
        if (val == this.statusOptions[i].value) {
          return this.statusOptions[i].label;
        }
      }
      return '';
    },
    handleApplyClose() {
      this.isAddDlg = false;
      this.getApiRolesList();
    },
    handleEditClose() {
      this.isEditDlg = false;
      this.getApiRolesList();
    },
    async delApiRoleById(row) {
      this.$q
        .dialog({
          title: `确认删除`,
          message: `是否确认删除此数据？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await resolveResponseError(() => deleteApiRoleById({ id: row.id }));
          successNotify('删除成功!');
          this.getApiRolesList();
        });
    }
  },
  async created() {
    let filterObj = getTableFilter('apiRole/listFilter');
    if (filterObj) {
      this.searchObj = JSON.parse(filterObj);
    }
    //初始化列表数据;
    this.getApiRolesList();
    //获取角色
    this.getRole();
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumnApiRoleOptions;
    this.visibleColumns = getTableCol('apiRole/listColumns');
    if (!this.visibleColumns || this.visibleColumns.length <= 2) {
      this.visibleColumns = tempVisibleColumns;
    }
  }
};
</script>
<style lang="stylus" scoped>
.opEdit {
  display: flex;
  align-items: center;
  vertical-align: center;
}
.lflex{
  border-left:1px solid #DDDDDD;
  height: 14px;
}
.status-img {
  width: 8px;
  height: 8px;
  border-radius: 4px;
  background: #fff;
}
.status-style{
  display: flex;
  align-items: center;
}
.colorusing{
  background-image: linear-gradient(90deg, #FD8D00 0%, rgba(253,141,0,0.50) 100%);
}
.colordel{
  background-image: linear-gradient(270deg, rgba(153,153,153,0.50) 0%, #999999 100%);
}
</style>
