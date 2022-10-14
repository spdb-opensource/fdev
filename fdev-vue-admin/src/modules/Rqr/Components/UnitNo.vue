<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      :data="tableData"
      :columns="columns"
      row-key="id"
      :pagination.sync="pagination"
      :visible-columns="visibleColumns"
      title="研发单元列表"
      title-icon="list_s_f"
      no-export
    >
      <template v-slot:top-bottom>
        <f-formitem label="小组" label-style="width:auto">
          <fdev-select v-model="tableGroup" :options="tableGroups" />
        </f-formitem>
        <f-formitem label="状态" label-style="width:auto;margin-left:10px">
          <fdev-select v-model="tableStatu" :options="tableStatus" />
        </f-formitem>
      </template>
      <!-- 研发单元编号 -->
      <template v-slot:body-cell-fdev_implement_unit_no="props">
        <fdev-td>
          <div
            v-if="props.row.fdev_implement_unit_no"
            :title="props.row.fdev_implement_unit_no"
            class="a-link text-ellipsis"
            @click="openUnitDetail(props.row)"
          >
            {{ props.row.fdev_implement_unit_no || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.fdev_implement_unit_no || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 实施单元编号 -->
      <template v-slot:body-cell-ipmp_implement_unit_no="props">
        <fdev-td>
          <!-- isNullFun have_link -->
          <div
            :title="props.row.ipmp_implement_unit_no"
            class="text-ellipsis"
            v-if="
              props.row.ipmp_implement_unit_no && isLinkFun(props.row.have_link)
            "
          >
            <router-link
              :to="
                `/rqrmn/unitDetail/${props.row.ipmp_implement_unit_no}/${
                  demandModel.id
                }`
              "
              class="link"
            >
              <span :title="props.row.ipmp_implement_unit_no">
                {{ props.row.ipmp_implement_unit_no }}
              </span>
            </router-link>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.ipmp_implement_unit_no }}
              </fdev-banner>
            </fdev-popup-proxy>
          </div>
          <div
            class="text-ellipsis"
            :title="props.row.ipmp_implement_unit_no"
            v-else-if="
              props.row.ipmp_implement_unit_no &&
                !isLinkFun(props.row.have_link)
            "
          >
            {{ props.row.ipmp_implement_unit_no }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.ipmp_implement_unit_no }}
              </fdev-banner>
            </fdev-popup-proxy>
          </div>
          <div v-else>
            -
          </div>
        </fdev-td>
      </template>
      <!-- 研发单元牵头人 -->
      <template v-slot:body-cell-implement_leader_all="props">
        <fdev-td>
          <div
            :title="
              props.row.implement_leader_all
                .map(item => item.user_name_cn)
                .join(',')
            "
            class="text-ellipsis"
            v-show="formate(props.row.implement_leader_all)"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link :to="`/user/list/${item.id}`" class="link">
                {{ item.user_name_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{
                      props.row.implement_leader_all
                        .map(item => item.user_name_cn)
                        .join(' ')
                    }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </span>
          </div>
          <span v-show="!formate(props.row.implement_leader_all)">-</span>
        </fdev-td>
      </template>
      <!-- 涉及UI还原审核 -->
      <template v-slot:body-cell-ui_verify="props">
        <fdev-td>
          {{ props.row.ui_verify ? '涉及' : '不涉及' }}
        </fdev-td>
      </template>
      <!-- 状态 -->
      <template v-slot:body-cell-implement_unit_status_normal="props">
        <fdev-td>
          <span
            :title="
              devUnitStatus(
                props.row.implement_unit_status_special,
                props.row.implement_unit_status_normal
              )
            "
          >
            {{
              devUnitStatus(
                props.row.implement_unit_status_special,
                props.row.implement_unit_status_normal
              )
            }}
          </span>
        </fdev-td>
      </template>
      <!-- 所属小组 -->
      <template v-slot:body-cell-group_cn="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.row.group_cn">
            {{ props.row.group_cn || '-' }}
          </div>
        </fdev-td>
      </template>
      <!-- 研发单元创建人 -->
      <template v-slot:body-cell-create_user_all="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            :title="
              props.row.create_user_all &&
                props.row.create_user_all.user_name_cn
            "
            v-if="props.row.create_user_all"
          >
            <router-link
              :to="`/user/list/${props.row.create_user_all.id}`"
              class="link"
            >
              {{ props.row.create_user_all.user_name_cn }}
            </router-link>
          </div>
          <div v-else>
            -
          </div>
        </fdev-td>
      </template>
      <!-- 操作列 -->
      <template v-slot:body-cell-operate="props">
        <fdev-td :auto-width="true" class="td-padding">
          <div class="q-gutter-sm flex row no-wrap">
            <div>
              <fdev-tooltip v-if="addJobBtnDisabledUnit(props.row)">
                {{ addJobBtnDescByStatus(props.row) }}
              </fdev-tooltip>
              <fdev-btn
                label="新增任务"
                flat
                :disable="addJobBtnDisabledUnit(props.row)"
                @click="toJobAddPage(props.row)"
              />
            </div>
            <div v-if="props.row.mount_flag">
              <fdev-btn
                @click="btnGroupFun('mountBtn', props)"
                label="挂载"
                flat
              />
            </div>
            <div>
              <fdev-tooltip
                v-if="props.row.update_flag && props.row.update_flag.msg"
              >
                {{ props.row.update_flag && props.row.update_flag.msg }}
              </fdev-tooltip>
              <fdev-btn
                :disable="props.row.update_flag && !props.row.update_flag.flag"
                flat
                label="编辑"
                @click="btnGroupFun('updateBtn', props)"
              />
            </div>
            <div>
              <fdev-tooltip v-if="props.row.del_flag !== 0">
                {{ operationColPermission('deleteTipWord', props) }}
              </fdev-tooltip>
              <fdev-btn
                :disable="props.row.del_flag !== 0"
                class="text-negative"
                @click="btnGroupFun('deleteBtn', props)"
                flat
                label="删除"
              />
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <ImplUnitProfile v-model="openUnitProfileDiaglog" :implInfo="implInfo" />
    <!-- 挂载研发单元 -->
    <mounted-dev-unit
      v-if="mountDialogOpen"
      :mountIpmp="mountDetails"
      @refreshMount="refreshMount"
    />
    <!-- 编辑研发单元 -->
    <dev-unit
      v-model="implUnitDialogOpen"
      :demandType="demandType"
      :demandId="demandId"
      :groupId="groupId"
      :operateType="operateType"
      :uiVerify="uiVerify"
      :isDemandAdmin="isDemandAdmin"
      :isDemandLeader="isDemandLeader"
      :isPartsLeader="isPartsLeader"
      :isLimited="isLimited"
      :currentUserGroupId="currentUserGroupId"
      :upImplUnitModel="upImplUnitModel"
      :relate_part_detail="demandModel_new.relate_part_detail"
      :currentUser="currentUser"
      @refImplUnitList="refImplUnitList"
    ></dev-unit>
  </Loading>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import {
  perform,
  normalStatus,
  specialStatus,
  roleName,
  createDemandModel,
  evaluateTableColumns
} from '../model';
import {
  setUnitNoPagination,
  getUnitNoPagination,
  getTableCol,
  setTableCol
} from '../setting';
import {
  formatSelectDisplay,
  deepClone,
  wrapOptionsTotal
} from '@/utils/utils';
import { successNotify } from '@/utils/utils';
import Loading from '@/components/Loading';
import ImplUnitProfile from './ImplUnitProfile';
import mountedDevUnit from './Mount';
import devUnit from './ImplUnitAdd';
import { deleteDevUnitById } from '@/services/demand.js';

export default {
  name: 'UnitNo',
  components: { Loading, ImplUnitProfile, devUnit, mountedDevUnit },
  props: {
    demandModel: {
      type: Object,
      default: () => {}
    },
    unitNum: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      openUnitProfileDiaglog: false,
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
      columns: evaluateTableColumns,
      pagination: getUnitNoPagination(),
      visibleColumns: this.visibleColumnUnitNoOptions,
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
      demandModel_new: createDemandModel(),
      isDemandAdmin: false,
      isDemandLeader: false,
      isPartsLeader: false,
      implUnitDialogOpen: false,
      mountDialogOpen: false,
      mountDetails: null,
      isIpmpmentUnitleader: false, //研发单元对应的实施单元牵头人
      ismountBtn: false,
      isImplUnitLeader: false, //研发单元牵头人
      ipmpDemandId: ''
    };
  },
  watch: {
    pagination(val) {
      setUnitNoPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    visibleColumns(val) {
      setTableCol('rqr/UnitNo', val);
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
        if (val.id) this.init();
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
    },
    tableData: {
      deep: true,
      handler(val) {
        //如果为科技需求则不展示UI是否涉及还原审核
        if (this.demandModel.demand_type === 'tech') {
          let idx = -1;
          this.columns.map((item, index) => {
            if (item.name === 'ui_verify') {
              idx = index;
            }
          });
          if (idx > 0) {
            this.columns.splice(idx, 1);
          }
        }
      }
    }
  },
  computed: {
    ...mapState('demandsForm', ['implementUnitData', 'ipmpUnitNoList']),
    ...mapState('userForm', ['groups']),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    }
  },
  methods: {
    ...mapActions('demandsForm', {
      queryPaginationByDemandId: 'queryPaginationByDemandId',
      queryPaginationByIpmpUnitNo: 'queryPaginationByIpmpUnitNo'
    }),
    ...mapActions('userForm', ['fetchGroup']),
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
    addJobBtnDescByStatus(row) {
      //评估中 已归档 已撤销 暂缓中 恢复中
      if (row.implement_unit_status_normal == normalStatus.assessmenting) {
        return '该实施单元状态为评估中，无法关联，请联系需求牵头人完成评估';
      } else if (row.implement_unit_status_normal == normalStatus.archived) {
        return '该实施单元已归档';
      } else if (row.implement_unit_status_normal == normalStatus.undo) {
        return '该实施单元已被撤销，无法操作';
      } else if (
        row.implement_unit_status_special == specialStatus.staying ||
        row.implement_unit_status_special == specialStatus.inRecovery
      ) {
        return '该实施单元处于暂缓状态，请解除暂缓状态后继续操作';
      }
    },
    toJobAddPage(row) {
      this.$router.push({
        name: 'add',
        params: {
          unitData: row
        }
      });
    },
    openUnitDetail(info) {
      this.$router.push({
        path: '/rqrmn/devUnitDetails',
        query: { id: info.id, dev_unit_no: info.fdev_implement_unit_no }
      });
      // info.implement_leader = [];
      // if (info.implement_leader_all) {
      //   info.implement_leader_all.forEach(item => {
      //     info.implement_leader.push(item.user_name_cn);
      //   });
      // }
      // this.openUnitProfileDiaglog = true;
      // this.implInfo = info;
    },
    refImplUnitList(val) {
      this.implUnitDialogOpen = false;
      // if (val !== 'close') this.init();
      if (val !== 'close') this.$emit('refresh');
    },
    filterTable() {
      this.tableData = this.implementUnitData.data;
      if (this.tableGroup.value != 'total') {
        this.tableData = this.tableData.filter(item => {
          return item.group == this.tableGroup.id;
        });
      }
      if (
        this.tableStatu.value != 'total' &&
        this.tableStatu.value != 'defer'
      ) {
        this.tableData = this.tableData.filter(item => {
          return (
            item.implement_unit_status_normal == this.tableStatu.value &&
            item.implement_unit_status_special != 1 &&
            item.implement_unit_status_special != 2
          );
        });
      } else if (
        this.tableStatu.value != 'total' &&
        this.tableStatu.value == 'defer'
      ) {
        this.tableData = this.tableData.filter(item => {
          return (
            item.implement_unit_status_special == 1 ||
            item.implement_unit_status_special == 2
          );
        });
      }
    },
    operationColPermission(btnType, { row }) {
      let btnFn = {
        deleteTipWord() {
          const deleteTipMap = {
            1: '请先恢复需求正常状态再行删除',
            2: '该研发单元已挂载任务，无法删除',
            3: '请联系需求牵头人、需求管理员、实施单元牵头人删除',
            4: '您无权删除该研发单元'
          };
          if (row.del_flag < 5) {
            return deleteTipMap[row.del_flag];
          }
        }
      };
      return btnFn[btnType]();
    },
    refreshMount(val) {
      this.mountDialogOpen = false;
      if (val) this.init();
    },
    btnGroupFun(btnType, props) {
      let { row } = props;
      let btnObj = {
        // 挂载操作
        mountBtn: () => {
          this.mountDetails = row;
          this.mountDialogOpen = true;
        },
        // 修改操作
        updateBtn: () => {
          this.demandId = row.demand_id;
          this.demandType = row.demand_type;
          this.operateType = 'update';
          this.uiVerify = row.uiVerify;
          this.groupId = row.group;
          this.upImplUnitModel = deepClone(row);
          if (this.demand_type === 'business') {
            Reflect.set(this.upImplUnitModel, 'task_no', '');
            Reflect.set(this.upImplUnitModel, 'task_name', '');
            Reflect.set(this.upImplUnitModel, 'ipmp_implement_unit_no', '');
          }
          if (row.implement_unit_status_special === 2) {
            this.isLimited = true;
          } else {
            this.isLimited = false;
          }

          this.$set(
            this.upImplUnitModel,
            'ipmp_implement_unit_no',
            row.ipmp_implement_unit_no
          );
          this.implUnitDialogOpen = true;
        },
        // 删除操作
        deleteBtn: () => {
          this.$q
            .dialog({
              title: `删除确认`,
              message: `确认要删除本条研发单元信息吗？`,
              ok: '确定',
              cancel: '取消'
            })
            .onOk(() => {
              deleteDevUnitById({ ids: [row.id] }).then(res => {
                // this.init();
                successNotify('删除成功！');
                this.$emit('refresh');
              });
            });
        }
      };
      return btnObj[btnType]();
    },
    // 修改研发单元更新研发单元列表
    beforeCloseFun() {
      this.openDevUnitDialog = false;
    },
    async init() {
      // 如果需求id不存在 不发接口，等watch响应后在发
      if (!this.demandModel.id) return;
      this.loading = true;
      if (this.unitNum) {
        await this.queryPaginationByIpmpUnitNo({
          ipmp_unit_no: this.unitNum,
          demand_id: this.demandModel.id
        });
        this.tableData = this.ipmpUnitNoList.data;
      } else {
        await this.queryPaginationByDemandId({
          demand_id: this.demand_id
        });
        this.tableData = this.implementUnitData.data;
      }
      this.loading = false;
    },
    formate(arr) {
      return arr.map(item => item.user_name_cn).join(',');
    },
    isLinkFun(arg) {
      return arg === 1;
    },
    devUnitStatus(special, normal) {
      const obj1 = {
        1: '评估中',
        2: '待实施',
        3: '开发中',
        4: 'sit',
        5: 'uat',
        6: 'rel',
        7: '已投产',
        8: '已归档',
        9: '已撤销'
      };
      let obj2 = {
        1: '暂缓中',
        2: '恢复中',
        3: '恢复完成'
      };
      if (special && special !== 3) return obj2[special];
      if (normal) return obj1[normal];
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
    }
  },
  async created() {
    this.demand_id = this.$route.params.id;
    await this.fetchGroup();
    this.deepCloneGroups = deepClone(this.groups);
    this.deepCloneGroups.forEach((group, index) => {
      this.$set(this.deepCloneGroups[index], 'label', group.fullName);
    });
    this.tableGroups = wrapOptionsTotal(this.deepCloneGroups);
    this.addJobBtnDisabled = this.currentUser.role.every(
      role =>
        role.name !== roleName.spdbHead && role.name !== roleName.companyHead
    );
    if (this.addJobBtnDisabled) {
      this.addJobBtnDisabledDesc = '只有行内/厂商负责人可执行此操作';
    }
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumnUnitNoOptions;
    this.visibleColumns = getTableCol('rqr/UnitNo');
    if (!this.visibleColumns || this.visibleColumns.length <= 2) {
      this.visibleColumns = tempVisibleColumns;
    }
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.a-link {
  color: #2196f3;
  cursor: pointer;
}
.wd200
  width 200px
</style>
