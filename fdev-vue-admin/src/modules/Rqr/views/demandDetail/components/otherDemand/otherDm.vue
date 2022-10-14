<template>
  <div>
    <Loading :visible="loading">
      <fdev-table
        class="my-sticky-column-table"
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        :visible-columns="visibleColumns"
        :onSelectCols="changSelect"
        noExport
      >
        <template v-slot:top-right>
          <fdev-btn
            v-if="isAdd()"
            ficon="add"
            dialog
            label="新增"
            normal
            @click="addOtherDmTask"
          />
        </template>
        <template v-slot:top-left>
          <div class="row">
            <f-formitem
              profile
              label-style="width:38px;"
              value-style="width:160px;"
              label="小组"
              class="q-mr-lt"
            >
              <fdev-select
                use-input
                v-model="tableGroup"
                :options="tableGroups"
                @filter="groupInputFilter"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label>{{ scope.opt.name }}</fdev-item-label>
                      <fdev-item-label caption>
                        <span class="te-desc ellipsis">
                          {{ scope.opt.fullName }}
                        </span>
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem
              profile
              label-style="width:38px;"
              value-style="width:160px;"
              label="状态"
            >
              <fdev-select v-model="tableStatus" :options="tableStatusList" />
            </f-formitem>
          </div>
        </template>
        <template v-slot:body-cell-taskNum="props">
          <fdev-td>
            <div
              v-if="props.row.taskNum"
              :title="props.row.taskNum"
              class="text-ellipsis"
            >
              <router-link
                :to="
                  `/rqrmn/ODTaskDetail/${props.row.taskNum}/${demandModel.id}`
                "
                class="link"
              >
                {{ props.row.taskNum || '-' }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.taskNum || '-' }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-taskName="props">
          <fdev-td>
            <div
              v-if="props.row.taskName"
              :title="props.row.taskName"
              class="text-ellipsis"
            >
              {{ props.row.taskName || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.taskName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-planPrjName="props">
          <fdev-td>
            <div
              v-if="props.row.planPrjName"
              :title="props.row.planPrjName"
              class="text-ellipsis"
            >
              {{ props.row.planPrjName || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.planPrjName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-headerUnitName="props">
          <fdev-td>
            <div
              v-if="props.row.headerUnitName"
              :title="props.row.headerUnitName"
              class="text-ellipsis"
            >
              {{ props.row.headerUnitName || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.headerUnitName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-headerTeamName="props">
          <fdev-td>
            <div
              v-if="props.row.headerTeamName"
              :title="props.row.headerTeamName"
              class="text-ellipsis"
            >
              {{ props.row.headerTeamName || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.headerTeamName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-leaderGroup="props">
          <fdev-td>
            <div
              v-if="props.row.leaderGroup"
              :title="props.row.leaderGroup"
              class="text-ellipsis"
            >
              {{ props.row.leaderGroupName || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.leaderGroupName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-taskLeaderName="props">
          <fdev-td>
            <div
              :title="props.row.taskLeaderName"
              class="text-ellipsis"
              v-show="props.row.taskLeaderName"
            >
              <router-link
                :to="`/user/list/${props.row.taskLeaderId}`"
                class="link"
              >
                {{ props.row.taskLeaderName }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.taskLeaderName || '-' }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-status="props">
          <fdev-td>
            <div
              v-if="props.row.status"
              :title="props.row.status"
              class="status-style"
            >
              <span
                class="status-img q-mr-xs"
                :style="{ background: getStatusColor(props.row.status) }"
              />
              {{ getStatusName(props.row.status) }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operation="props">
          <fdev-td :auto-width="true" class="td-padding">
            <div class="opEdit ">
              <span>
                <fdev-tooltip
                  v-if="userIsUpdate(props.row.isUpdate, props.row.status)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span>{{
                    showUpdateMsg(props.row.isUpdate, props.row.status)
                  }}</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="编辑"
                  :disable="userIsUpdate(props.row.isUpdate, props.row.status)"
                  @click="updateOtherDmTask(props.row)"
                />
              </span>
              <span class="lflex q-mx-xs"> </span>
              <span>
                <fdev-tooltip
                  v-if="userIsDelete(props.row.isDelete, props.row.status)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span
                    >{{ showErrorMsg(props.row.isDelete, props.row.status) }}
                  </span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="删除"
                  :disable="userIsDelete(props.row.isDelete, props.row.status)"
                  @click="deleteOtherDmTask(props.row)"
                />
              </span>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
      <addOtherDmTaskDlg
        v-if="users"
        :leadGroups="leadGroups"
        :userLists="users"
        v-model="addFlag"
        @close="closeAddDlg"
        :demandDetail="demandModel"
      ></addOtherDmTaskDlg>
      <updateOtherDmTaskDlg
        v-if="users"
        :userLists="users"
        :leadGroups="leadGroups"
        :demandDetail="demandModel"
        :taskNum="unitDetail.taskNum"
        v-model="editFlag"
        @close="closeEditDlg"
      ></updateOtherDmTaskDlg>
    </Loading>
  </div>
</template>
<script>
// 引入组件
import { mapState, mapActions, mapGetters } from 'vuex';
import { perform } from '@/modules/Rqr/model';
import { formatUser } from '@/modules/User/utils/model';
import {
  queryOtherDemandTaskList,
  deleteOtherDemandTask
} from '@/modules/Rqr/services/methods';
import {
  setOtherDTPagination,
  getOtherDTPagination,
  getTableCol,
  setTableCol
} from '@/modules/Rqr/setting';
import { deepClone, wrapOptionsTotal, formatOption } from '@/utils/utils';
import Loading from '@/components/Loading';
import addOtherDmTaskDlg from './addOtherDmTaskDlg';
import updateOtherDmTaskDlg from './updateOtherDmTaskDlg';
export default {
  name: 'otherDm',
  props: {
    demandModel: {
      type: Object
    },
    isDemandManager: {
      type: Boolean,
      default: false
    },
    isDemandLeader: {
      type: Boolean,
      default: false
    },
    isIncludeCurrentUser: {
      type: Boolean,
      default: false
    }
  },
  components: { Loading, addOtherDmTaskDlg, updateOtherDmTaskDlg },
  watch: {
    pagination(val) {
      setOtherDTPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    visibleColumns(val) {
      setTableCol('rqr/OtherDmTask', val);
    },
    tableGroup: {
      deep: true,
      handler(val) {
        this.getOtherDmTasks();
      }
    },
    tableStatus: {
      deep: true,
      handler(val) {
        this.getOtherDmTasks();
      }
    }
  },
  data() {
    return {
      loading: false,
      ...perform,
      deepCloneGroups: [],
      tableGroup: {
        label: '',
        value: ''
      },
      leadGroups: [],
      tableGroups: [],
      tableStatus: {
        label: '全部',
        value: 'total'
      },
      tableStatusList: [
        { label: '全部', value: 'total' },
        { label: '未开始', value: 'notStart' },
        { label: '进行中', value: 'going' },
        { label: '已完成', value: 'done' }
      ],
      tableData: [],
      columns: [
        {
          name: 'taskNum',
          label: '任务编号',
          field: 'taskNum',
          sortable: true,
          required: true
        },
        {
          name: 'taskName',
          label: '任务名称',
          field: 'taskName',
          sortable: true
        },
        {
          name: 'planPrjName',
          label: '项目/任务集名称',
          field: 'planPrjName',
          sortable: true
        },
        {
          name: 'headerUnitName',
          label: '牵头单位',
          field: 'headerUnitName',
          sortable: true
        },
        {
          name: 'headerTeamName',
          label: '牵头团队',
          field: 'headerTeamName',
          sortable: true
        },
        {
          name: 'leaderGroup',
          label: '牵头小组',
          field: 'leaderGroup',
          sortable: true
        },
        {
          name: 'taskLeaderName',
          label: '任务牵头人',
          field: 'taskLeaderName',
          sortable: true
        },
        {
          name: 'status',
          label: '状态',
          field: 'status',
          sortable: true
        },
        {
          name: 'planStartDate',
          label: '计划启动日期',
          field: 'planStartDate',
          sortable: true
        },
        {
          name: 'planDoneDate',
          label: '计划完成日期',
          field: 'planDoneDate',
          sortable: true
        },
        {
          name: 'actualStartDate',
          label: '实际启动日期',
          field: 'actualStartDate',
          sortable: true
        },
        {
          name: 'actualDoneDate',
          label: '实际完成日期',
          field: 'actualDoneDate',
          sortable: true
        },
        {
          name: 'operation',
          label: '操作',
          field: 'operation',
          required: true,
          align: 'center'
        }
      ],
      pagination: getOtherDTPagination(),
      visibleColumns: this.visibleColumnOtherDmTaskOptions,
      editFlag: false,
      addFlag: false,
      unitDetail: {},
      editFlaTip: '',
      users: null
    };
  },
  computed: {
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('userForm', ['groups']),
    ...mapState('user', {
      currentUser: 'currentUser'
    })
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    groupInputFilter(val, update) {
      update(() => {
        this.tableGroups = this.deepCloneGroups.filter(tag =>
          tag.label.includes(val)
        );
      });
    },
    isAdd() {
      if (
        (this.isDemandManager || this.isDemandLeader) &&
        this.demandModel.demand_status_normal != '8'
      ) {
        return true;
      } else {
        return false;
      }
    },
    closeAddDlg(flag) {
      this.addFlag = false;
      if (flag) {
        this.getOtherDmTasks();
      }
    },
    closeEditDlg(flag) {
      this.editFlag = false;
      if (flag) {
        this.getOtherDmTasks();
      }
    },
    //获取其他需求任务列表
    async getOtherDmTasks() {
      this.loading = true;
      let params = { demandId: this.demandModel.id };
      params.size = this.pagination.rowsPerPage; //非必须每页条数，不传或0默认查全部
      params.index = this.pagination.page; //页码
      if (this.tableGroup && this.tableGroup.value) {
        //牵头小组
        params.leaderGroup = this.tableGroup.value;
      }
      if (this.tableStatus && this.tableStatus.value) {
        //实施状态
        if (this.tableStatus.value === 'total') {
          params.status = '';
        } else {
          params.status = this.tableStatus.value;
        }
      }
      const res = await queryOtherDemandTaskList(params);
      if (res) {
        this.tableData = res.data || [];
        this.pagination.rowsNumber = res.count;
      }

      this.loading = false;
    },
    async updateOtherDmTask(row) {
      await this.getUserList();
      this.unitDetail = row;
      this.editFlag = true;
    },
    async addOtherDmTask() {
      await this.getUserList();
      this.addFlag = true;
    },
    formate(arr) {
      return arr.map(item => item.user_name_cn).join(',');
    },
    getStatusName(val) {
      if (val === 'notStart') return '未开始';
      else if (val === 'going') return '进行中';
      else if (val === 'done') return '已完成';
      else if (val === 'delete') return '删除';
      else return '-';
    },
    getStatusColor(val) {
      //实施状态 notStart=未开始 going=进行中 done=已完成 delete=删除
      const color = {
        notStart: '#78909C',
        going: '#33A1FB',
        done: '#60BD62',
        delete: '#F46865'
      };
      return color[val];
    },
    async deleteOtherDmTask(row) {
      return this.$q
        .dialog({
          title: '删除此任务',
          message: `确定删除此任务吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          let params = { taskNum: row.taskNum };
          await deleteOtherDemandTask(params);
          this.getOtherDmTasks();
        });
    },
    changSelect(clos) {
      this.visibleColumns = clos;
    },
    showUpdateMsg(val, status) {
      if (this.demandModel.demand_status_normal == '8') {
        return '已归档阶段不可编辑';
      } else if (status == 'done') {
        return '当前任务状态不可编辑';
      } else if (!val) {
        //需求管理员、需求牵头人、属于自己的任务负责人、厂商负责人
        return '当前用户不可编辑';
      } else {
        return '';
      }
    },
    userIsUpdate(val, status) {
      if (this.demandModel.demand_status_normal == '8') {
        return true;
      } else if (status == 'done') {
        return true;
      } else if (!val) {
        //需求管理员、需求牵头人、属于自己的任务负责人、厂商负责人
        return true;
      } else {
        return false;
      }
    },
    async getUserList() {
      await this.queryUser();
      this.users = this.isLoginUserList.map(user =>
        formatOption(formatUser(user), 'name')
      );
    },
    showErrorMsg(val, status) {
      if (this.demandModel.demand_status_normal == '8') {
        return '已归档阶段不可删除';
      } else if (status === 'done') {
        return '当前任务状态不可删除';
      } else if (!val || val === '') {
        return '';
      } else {
        return val;
      }
    },
    userIsDelete(val, status) {
      //权限：需求牵头人
      //说明：若其他需求任务下无研发单元，则可以删除此数据。
      if (this.demandModel.demand_status_normal == '8') {
        return true;
      } else if (status === 'done') {
        return true;
      } else if (!val || val === '') {
        return false;
      } else {
        return true;
      }
    }
  },

  async created() {
    await this.fetchGroup();
    this.deepCloneGroups = deepClone(this.groups);
    this.leadGroups = formatOption(this.groups);
    this.tableGroups = wrapOptionsTotal(this.deepCloneGroups);

    this.getOtherDmTasks();
  },

  mounted() {
    const tempVisibleColumns = this.visibleColumnOtherDmTaskOptions;
    this.visibleColumns = getTableCol('rqr/OtherDmTask');
    if (!this.visibleColumns || this.visibleColumns.length <= 2) {
      this.visibleColumns = tempVisibleColumns;
    }
  }
};
</script>
<style lang="stylus" scoped>
.overflow {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 250px;
  display: inline-block;
}

.q-chip {
  background: white;
}
.btn {
  width: 30px;
}

.td-desc {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.opEdit {
  display: flex;
  align-items: center;
  vertical-align: center;
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
.lflex{
  border-left:1px solid #DDDDDD;
  height: 14px;
}
</style>
