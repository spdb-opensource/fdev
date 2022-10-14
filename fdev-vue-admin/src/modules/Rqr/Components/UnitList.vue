<template>
  <Loading :visible="loading">
    <!-- <div class="q-gutter-x-xs q-gutter-y-lg">
      <fdev-chip square>
        <fdev-avatar
          square
          size="20px"
          color="orange-3"
          text-color="white"
        />待实施
      </fdev-chip>
      <fdev-chip square>
        <fdev-avatar square size="20px" color="blue-4" text-color="white" />开发中
      </fdev-chip>
      <fdev-chip square>
        <fdev-avatar square size="20px" color="purple-5" text-color="white" />SIT
      </fdev-chip>
      <fdev-chip square>
        <fdev-avatar square size="20px" color="lime-4" text-color="white" />UAT
      </fdev-chip>
      <fdev-chip square>
        <fdev-avatar
          square
          size="20px"
          color="blue-grey-5"
          text-color="white"
        />REL
      </fdev-chip>
      <fdev-chip square>
        <fdev-avatar square size="20px" color="green-8" text-color="white" />已投产
      </fdev-chip>
    </div> -->
    <!-- <div class="q-pa-md"> -->
    <fdev-table
      class="my-sticky-column-table"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      :visible-columns="visibleColumns"
      :onSelectCols="changSelect"
      @request="pageUnitList"
      titleIcon="list_s_f"
      title="实施单元列表"
      noExport
    >
      <template v-slot:top-right>
        <!-- <fdev-select
          :display-value="
            tableGroup.value == 'total'
              ? '小组'
              : formatSelectDisplay(tableGroups, tableGroup)
          "
          color="primary"
          v-model="tableGroup"
          :options="tableGroups"
          borderless
          options-dense
          class="table-head-input q-mr-sm"
        />

        <fdev-select
          :display-value="
            tableStatu.value == 'total'
              ? '状态'
              : formatSelectDisplay(tableStatus, tableStatu)
          "
          color="primary"
          v-model="tableStatu"
          :options="tableStatus"
          borderless
          options-dense
          class="table-head-input q-mr-sm"
        /> -->
      </template>
      <template v-slot:body-cell-implUnitNum="props">
        <fdev-td class="td-desc" :title="props.row && props.row.implUnitNum">
          <router-link
            v-if="props.row && props.row.implUnitNum"
            :to="`/rqrmn/unitDetail/${props.row.implUnitNum}/${demandModel.id}`"
            class="link"
          >
            {{ props.row.implUnitNum }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.implUnitNum }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-usedSysCode="props">
        <fdev-td class="td-desc">
          {{ props.row.usedSysCode | fileterCode }}
        </fdev-td>
      </template>
      <!-- <template v-slot:body-cell-operation="props">
        <fdev-td :auto-width="true" class="td-padding">
          <div class="inline-block">
            <fdev-btn
              color="primary"
              label="新增研发单元"
              size="sm"
              v-if="
                (isDemandManager || isDemandLeader || isIncludeCurrentUser) &&
                  !(
                    demandModel.demand_status_normal > 7 ||
                    demandModel.demand_status_special === 1 ||
                    props.row.implStatusName === '已投产' ||
                    props.row.implStatusName === '已撤销' ||
                    props.row.implStatusName === '暂缓' ||
                    props.row.implStatusName === '暂存'
                  ) &&
                  props.row.leaderFlag !== '3'
              "
              @click="toJobAddPage(props.row)"
            />
          </div>
        </fdev-td>
      </template> -->
      <template v-slot:body-cell-operation="props">
        <fdev-td :auto-width="true">
          <div class="flex no-wrap">
            <div>
              <fdev-tooltip
                v-if="
                  !(isDemandManager || isUnitLeader(props.row)) ||
                    props.row.leaderFlag === '3' ||
                    (!!props.row.usedSysCode &&
                      props.row.usedSysCode !== 'ZH-0748' &&
                      props.row.usedSysCode !== 'stockUnit') ||
                    props.row.implStatusName === '已撤销' ||
                    props.row.implStatusName === '暂缓' ||
                    props.row.implStatusName === '暂存'
                "
                anchor="top middle"
                self="center middle"
                :offest="[-20, 0]"
              >
                <span>{{ getTip(props.row) }}</span>
              </fdev-tooltip>
              <fdev-btn
                flat
                label="编辑"
                v-if="demandModel.demand_type === 'business'"
                :disable="
                  !(isDemandManager || isUnitLeader(props.row)) ||
                    props.row.leaderFlag === '3' ||
                    (!!props.row.usedSysCode &&
                      props.row.usedSysCode !== 'ZH-0748' &&
                      props.row.usedSysCode !== 'stockUnit') ||
                    props.row.implStatusName === '已撤销' ||
                    props.row.implStatusName === '暂缓' ||
                    props.row.implStatusName === '暂存'
                "
                @click="updateUnit(props.row)"
              />
            </div>
            <fdev-btn
              flat
              label="向IPMP同步"
              class="q-ml-sm"
              v-if="props.row.isShowSync"
              @click="toIPMPDialog(props.row)"
            />
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <!-- </div> -->
    <ImplUnitAdd
      v-if="implUnitDialogOpen"
      v-model="implUnitDialogOpen"
      :demandType="demandType"
      :demandId="demandId"
      :groupId="groupId"
      :operateType="operateType"
      :uiVerify="uiVerify"
      :isDemandAdmin="isDemandManager"
      :isDemandLeader="isDemandLeader"
      :isPartsLeader="isIncludeCurrentUser"
      :isLimited="isLimited"
      :currentUserGroupId="currentUserGroupId"
      :upImplUnitModel="upImplUnitModel"
      :relate_part_detail="demandModel.relate_part_detail"
      :currentUser="currentUser"
      @refImplUnitList="refImplUnitList"
    ></ImplUnitAdd>
    <f-dialog v-model="editFlag" title="去编辑信息">
      <span>
        {{ editFlaTip }}
      </span>
      <template v-slot:btnSlot>
        <fdev-btn flat label="去编辑" @click="goRqrEdit()" v-close-popup />
      </template>
    </f-dialog>
    <f-dialog v-model="toIPMPFlag" title="同步IPMP">
      <span>
        是否向IPMP同步实际日期？
      </span>
      <template v-slot:btnSlot>
        <fdev-btn flat label="同步" @click="toIPMP" />
      </template>
    </f-dialog>
    <f-dialog v-model="unitCheck" title="实施单元核算">
      <div>
        请前往<a href="#" @click="goIpmp">IPMP</a>发起【{{
          checkImplUnitNum
        }}】核算
      </div>
    </f-dialog>
    <update-unit-detail
      v-if="assessmentDialogShow"
      :showFlag="assessmentDialogShow"
      :data="unitDetail"
      @updateSuccess="updateSucc"
    />
  </Loading>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import {
  perform,
  normalStatus,
  specialStatus,
  roleName,
  createReqImplUnitModel
} from '../model';
import {
  setUnitListPagination,
  getUnitListPagination,
  getTableCol,
  setTableCol
} from '../setting';
import {
  formatSelectDisplay,
  deepClone,
  resolveResponseError,
  successNotify
  // wrapOptionsTotal
} from '@/utils/utils';
import Loading from '@/components/Loading';
import ImplUnitAdd from './ImplUnitAdd';
import UpdateUnitDetail from './updateUnitDetail';
import { syncIpmpUnit } from '../services/methods.js';
export default {
  name: 'UnitList',
  components: { Loading, ImplUnitAdd, UpdateUnitDetail },
  props: {
    demandModel: {
      type: Object,
      default: () => {}
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
  data() {
    return {
      demand_id: '',
      ...perform,
      loading: false,
      tableData: [],
      tableGroup: {
        label: '全部',
        value: 'total'
      },
      tableStatu: {
        label: '全部',
        value: 'total'
      },
      tableStatus: [
        { label: '全部', value: 'total' },
        // { label: '预评估', value: 0 },
        { label: '评估中', value: 1 },
        { label: '待实施', value: 2 },
        { label: '开发中', value: 3 },
        { label: 'SIT', value: 4 },
        { label: 'UAT', value: 5 },
        { label: 'REL', value: 6 },
        { label: '已投产', value: 7 },
        { label: '已归档', value: 8 },
        { label: '暂缓', value: 'defer' }
        // { label: '已撤销', value: 9 }
      ],
      tableGroups: [],
      columns: [
        {
          name: 'implUnitNum',
          label: 'IPMP实施单元编号',
          field: 'implUnitNum',
          required: true,
          copy: true
        },
        {
          name: 'implStatusName',
          label: '实施状态',
          field: 'implStatusName'
        },
        {
          name: 'implContent',
          label: '实施单元内容',
          field: 'implContent',
          copy: true
        },
        {
          name: 'implLeaderName',
          label: '实施牵头人中文姓名',
          field: 'implLeaderName',
          copy: true
        },
        {
          name: 'implLeader',
          label: '实施牵头人域账号',
          field: 'implLeader',
          copy: true
        },
        {
          name: 'headerUnitName',
          label: '牵头单位',
          field: 'headerUnitName',
          copy: true
        },
        {
          name: 'headerTeamName',
          label: '牵头团队',
          field: 'headerTeamName',
          copy: true
        },
        {
          name: 'usedSysCode',
          label: '评估状态',
          field: 'usedSysCode',
          copy: true
        },
        {
          name: 'testLeaderName',
          label: '测试牵头人中文姓名',
          field: 'testLeaderName'
        },
        {
          name: 'testLeader',
          label: '测试牵头人域账号 ',
          field: 'testLeader'
        },

        {
          name: 'testLeaderEmail',
          label: '测试牵头人邮箱',
          field: 'testLeaderEmail',
          copy: true
        },
        {
          name: 'implDelayTypeName',
          label: '实施延期原因分类',
          field: 'implDelayTypeName'
        },
        {
          name: 'implDelayReason',
          label: '实施延期原因',
          field: 'implDelayReason',
          copy: true
        },
        {
          name: 'planDevelopDate',
          label: '计划启动开发日期',
          field: 'planDevelopDate'
        },
        {
          name: 'planInnerTestDate',
          label: '计划提交内测日期',
          field: 'planInnerTestDate'
        },
        {
          name: 'planTestStartDate',
          label: '计划提交用户测试日期',
          field: 'planTestStartDate'
        },
        {
          name: 'planTestFinishDate',
          label: '计划用户测试完成日期',
          field: 'planTestFinishDate'
        },
        {
          name: 'planProductDate',
          label: '计划投产日期',
          field: 'planProductDate'
        },
        {
          name: 'acturalDevelopDate',
          label: '实际启动开发日期',
          field: 'acturalDevelopDate'
        },
        {
          name: 'actualInnerTestDate',
          label: '实际提交内测日期',
          field: 'actualInnerTestDate'
        },
        {
          name: 'acturalTestStartDate',
          label: '实际提交用户测试日期',
          field: 'acturalTestStartDate'
        },
        {
          name: 'acturalTestFinishDate',
          label: '实际用户测试完成日期',
          field: 'acturalTestFinishDate'
        },
        {
          name: 'acturalProductDate',
          label: '实际投产日期',
          field: 'acturalProductDate'
        },
        {
          name: 'relateSysName',
          label: '涉及系统名称',
          field: 'relateSysName',
          copy: true
        },
        {
          name: 'expectOwnWorkload',
          label: '预期行内人员工作量',
          field: 'expectOwnWorkload'
        },
        {
          name: 'expectOutWorkload',
          label: '预期公司人员工作量',
          field: 'expectOutWorkload'
        },
        {
          name: 'prjNum',
          label: '项目编号',
          field: 'prjNum',
          copy: true
        },
        {
          name: 'leaderGroupName',
          label: '牵头小组',
          field: 'leaderGroupName',
          copy: true
        },
        {
          name: 'cloudFlagName',
          label: '是否上云',
          field: 'cloudFlagName'
        },
        {
          name: 'techSchemeNo',
          label: '技术方案编号',
          field: 'techSchemeNo'
        },
        {
          name: 'checkerUserNames',
          label: '审核人',
          field: 'checkerUserNames',
          copy: true
        },
        {
          name: 'operation',
          label: '操作',
          field: 'operation',
          required: true
        }
      ],
      pagination: {
        sortBy: '', //排序
        page: 1, //页码
        rowsPerPage: getUnitListPagination(), //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },
      visibleColumns: this.visibleColumnDevelopNoOptions,
      implInfo: {},
      addJobBtnDisabled: true,
      addJobBtnDisabledDesc: '',
      // 研发单元弹窗部分
      demandType: '',
      demandId: '',
      groupId: '',
      operateType: 'update',
      uiVerify: false,
      isLimited: false,
      currentUserGroupId: '',
      upImplUnitModel: {},
      demandModel_new: {},
      isDemandAdmin: false,
      isPartsLeader: false,
      implUnitDialogOpen: false,
      editFlag: false,
      assessmentDialogShow: false,
      unitDetail: {},
      editFlaTip: '',
      toIPMPFlag: false,
      toIPMPDetail: {},
      unitCheck: false,
      checkImplUnitNum: '',
      checkUrl: ''
    };
  },
  watch: {
    'pagination.rowsPerPage'(val) {
      setUnitListPagination(val);
    },
    visibleColumns(val) {
      setTableCol('rqr/DevelopNo', val);
    },
    tableGroup: {
      deep: true,
      handler(val) {
        this.filterTable();
      }
    },
    tableStatu: {
      deep: true,
      handler(val) {
        this.filterTable();
      }
    },
    demandModel: {
      deep: true,
      handler(val) {
        if (Object.keys(val).length) {
          //需求状态为暂缓中或恢复中，不能新增任务
          if (
            val.demand_status_special == specialStatus.staying ||
            val.demand_status_special == specialStatus.inRecovery
          ) {
            this.addJobBtnDisabled = true;
            this.addJobBtnDisabledDesc =
              '该实施单元处于暂缓状态，请解除暂缓状态后继续操作';
          }
        }
      }
    }
  },
  computed: {
    ...mapState('demandsForm', ['ipmpUnitListTable']),
    ...mapState('userForm', ['groups']),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(1, columns.length - 1);
    }
  },
  methods: {
    //...mapActions('userForm', ['fetchGroup']),
    ...mapActions('demandsForm', ['queryIpmpUnitByDemandId']),
    exportExcel() {},
    formatSelectDisplay,
    addJobBtnDisabledUnit(row) {
      return (
        row.implement_unit_status_normal == normalStatus.assessmenting ||
        row.implement_unit_status_normal > normalStatus.done ||
        row.implement_unit_status_special == specialStatus.staying ||
        row.implement_unit_status_special == specialStatus.inRecovery
      );
    },
    refImplUnitList() {
      this.implUnitDialogOpen = false;
    },
    //判断涉及小组及涉及小组评估人
    isEditMessage() {
      if (this.demandModel.relate_part_detail.length === 0) {
        return true;
      } else {
        return !this.demandModel.relate_part_detail.some(
          item => item.assess_user.length > 0
        );
      }
    },
    toJobAddPage(row) {
      //若需求无牵头小组或实施牵头人，涉及小组及涉及小组评估人
      if (
        this.demandModel.demand_leader_group == '' ||
        this.demandModel.demand_leader.length === 0 ||
        this.demandModel.relate_part.length === 0 ||
        this.isEditMessage()
      ) {
        this.editFlag = true;
        this.editFlaTip =
          '需求牵头小组、实施牵头人、涉及小组及涉及小组评估人信息不全，请前往编辑页面补充完整';
      } else {
        this.demandId = this.demandModel.id;
        this.demandType = this.demandModel.demand_type;
        this.operateType = 'add';
        this.uiVerify = this.demandModel.ui_verify;
        this.groupId = this.demandModel.demand_leader_group;
        this.upImplUnitModel = deepClone(createReqImplUnitModel());
        this.$set(
          this.upImplUnitModel,
          'ipmp_implement_unit_no',
          row.implUnitNum
        );
        this.implUnitDialogOpen = true;
      }
    },
    goRqrEdit() {
      this.$router.push(`/rqrmn/rqrEdit/${this.demandModel.id}`);
    },
    openUnitDetail(info) {
      info.implement_leader = [];
      if (info.implement_leader_all) {
        info.implement_leader_all.forEach(item => {
          info.implement_leader.push(item.user_name_cn);
        });
      }
      this.implInfo = info;
    },
    async init() {
      this.loading = true;
      await this.queryIpmpUnitByDemandId({
        demandId: this.demand_id,
        isTech: this.demandModel.demand_type,
        index: this.pagination.page,
        size: this.pagination.rowsPerPage
      });
      this.pagination.rowsNumber = this.ipmpUnitListTable.count;
      this.tableData = this.ipmpUnitListTable.data;
      this.loading = false;
    },
    isUnitLeader(row) {
      if (!row.implLeader) return false;
      let arr = row.implLeader.split(',');
      return arr.some(item => {
        return (
          item.toLowerCase() === this.currentUser.user_name_en.toLowerCase()
        );
      });
    },
    getTip(row) {
      let tip = '';
      if (!(this.isDemandManager || this.isUnitLeader(row))) {
        tip = '当前用户不为本实施单元牵头人、需求管理员， 不允许修改';
      } else if (row.leaderFlag === '3') {
        tip = '实施牵头人均不是fdev人员， 不允许修改';
      } else if (
        !!row.usedSysCode &&
        row.usedSysCode !== 'ZH-0748' &&
        row.usedSysCode !== 'stockUnit'
      ) {
        tip = '该实施单元不属于 fdev平台, 不允许修改';
      } else if (
        row.implStatusName === '已撤销' ||
        row.implStatusName === '暂缓' ||
        row.implStatusName === '暂存'
      ) {
        tip = '实施单元状态为已撤销、暂缓、暂存，不允许修改';
      }
      return tip;
    },
    updateUnit(row) {
      this.unitDetail = row;
      this.assessmentDialogShow = true;
    },
    updateSucc() {
      this.assessmentDialogShow = false;
      this.init();
    },
    changSelect(clos) {
      this.visibleColumns = clos;
    },
    pageUnitList(val) {
      this.pagination.page = val.pagination.page;
      this.pagination.rowsPerPage = val.pagination.rowsPerPage;
      this.init();
    },
    toIPMPDialog(row) {
      this.toIPMPFlag = true;
      this.toIPMPDetail = row;
    },
    //同步IPMP
    async toIPMP() {
      this.toIPMPFlag = false;
      try {
        let response = await resolveResponseError(() =>
          syncIpmpUnit({ implUnitNum: this.toIPMPDetail.implUnitNum })
        );
        if (response.errorCode === '30') {
          this.unitCheck = true;
          this.checkImplUnitNum = response.implUnitNum;
          this.checkUrl = response.url;
        } else {
          successNotify('同步IPMP成功！');
          this.toIPMPDetail = {};
          this.init();
        }
      } catch (error) {
        this.toIPMPDetail = {};
        throw error;
      }
    },
    //新窗口打开IPMP
    goIpmp() {
      window.open(this.checkUrl);
    }
  },
  filters: {
    implementStatusNormalFiler(val) {
      if (val == 0) {
        return (val = '预评估');
      } else if (val == 1) {
        return (val = '评估中');
      } else if (val == 2) {
        return (val = '待实施');
      } else if (val == 3) {
        return (val = '开发中');
      } else if (val == 4) {
        return (val = 'SIT');
      } else if (val == 5) {
        return (val = 'UAT');
      } else if (val == 6) {
        return (val = 'REL');
      } else if (val == 7) {
        return (val = '已投产');
      } else if (val == 8) {
        return (val = '已归档');
      } else if (val == 9) {
        return (val = '已撤销');
      }
    },
    implementStatusSpecialFilter(val) {
      if (val == 1) {
        return (val = '暂缓');
      } else if (val == 2) {
        return (val = '暂缓');
      } else if (val == 3) {
        return (val = '恢复完成');
      }
    },
    leader_all_user_names(val) {
      return val.map(leader => leader.user_name_cn).join('，');
    },
    fileterCode(val) {
      let model = '';
      if (val === 'ZH-0748') {
        model = 'fdev评估';
      } else if (val === 'stockUnit') {
        model = 'ipmp评估';
      } else {
        model = '未评估';
      }
      return model;
    }
  },
  async created() {
    this.demand_id = this.$route.params.id;
    this.init();
    // await this.fetchGroup();
    // this.deepCloneGroups = deepClone(this.groups);
    // this.deepCloneGroups.forEach((group, index) => {
    //   this.$set(this.deepCloneGroups[index], 'label', group.fullName);
    // });
    // this.tableGroups = wrapOptionsTotal(this.deepCloneGroups);
    this.addJobBtnDisabled = this.currentUser.role.every(
      role =>
        role.name !== roleName.spdbHead && role.name !== roleName.companyHead
    );
    if (this.addJobBtnDisabled) {
      this.addJobBtnDisabledDesc = '只有行内/厂商负责人可执行此操作';
    }
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumnDevelopNoOptions;
    this.visibleColumns = getTableCol('rqr/DevelopNo');
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

.width-select {
  min-width: 150px;
}

.btn {
  width: 30px;
}

.btn-last {
  width: 60px;
}

.td-width {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.td-desc {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.a-link {
  color: #2196f3;
  cursor: pointer;
}
.inline-block {
  display: inline-block;
}
</style>
