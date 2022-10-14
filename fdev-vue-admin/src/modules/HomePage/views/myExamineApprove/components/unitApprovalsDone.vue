<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      no-export
      :pagination.sync="pagination"
      title-icon="check_s_f"
      title="审批已完成列表"
      class="my-sticky-column-table"
      :visible-columns="visibleColsDone"
      :onSelectCols="saveVisibleColumnsDone"
      @request="pageTableRequest"
      :on-search="findMyApproveList"
    >
      <template #top-right>
        <span>
          <fdev-btn normal label="查询全部" @click="jumpToApproveList" />
        </span>
      </template>
      <template v-slot:top-bottom>
        <f-formitem label="搜索条件" class="q-pr-md">
          <fdev-input
            :value="keywords"
            placeholder="请输入研发单元编号/内容"
            clearable
            @input="saveKeywords($event)"
            @keyup.enter="findMyApproveList()"
            @clear="clearKeywords()"
            ><template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="findMyApproveList"
              />
            </template>
          </fdev-input>
        </f-formitem>
        <f-formitem label="审批类型" class="q-pr-md">
          <fdev-select
            :value="approveTypeDone"
            :options="approveOptions"
            @input="saveDoneType($event), init()"
            @clear="saveDoneType('')"
            clearable
          >
          </fdev-select>
        </f-formitem>
      </template>
      <!-- 研发单元编号跳转 -->
      <template v-slot:body-cell-fdevUnitNo="props">
        <fdev-td :title="props.row.fdevUnitNo">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.demandId && props.row.fdevUnitNo"
              class="link"
              :to="{
                path: '/rqrmn/devUnitDetails',
                query: {
                  demandId: props.row.demandId,
                  dev_unit_no: props.row.fdevUnitNo
                }
              }"
            >
              {{ props.row.fdevUnitNo }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.fdevUnitNo }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.fdevUnitNo }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 需求名称跳转 -->
      <template v-slot:body-cell-demandName="props">
        <fdev-td :title="props.row.demandName">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.demandId && props.row.demandName"
              :to="`/rqrmn/rqrProfile/${props.row.demandId}`"
              class="link"
            >
              {{ props.row.demandName }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.demandName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.demandName || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 需求编号跳转 -->
      <template v-slot:body-cell-demandNo="props">
        <fdev-td :title="props.row.demandNo">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.demandId && props.row.demandName"
              :to="`/rqrmn/rqrProfile/${props.row.demandId}`"
              class="link"
            >
              {{ props.row.demandNo }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.demandNo }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.demandNo || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 研发单元状态 -->
      <template v-slot:body-cell-fdevUnitState="props">
        <fdev-td class="text-ellipsis">
          <div class="row no-wrap items-center">
            <f-status-color
              :gradient="
                devUnitStatus(
                  props.row.fdevUnitSpecialState,
                  props.row.fdevUnitState
                ) | statusFilter
              "
            ></f-status-color>

            <span
              :title="
                devUnitStatus(
                  props.row.fdevUnitSpecialState,
                  props.row.fdevUnitState
                )
              "
            >
              {{
                devUnitStatus(
                  props.row.fdevUnitSpecialState,
                  props.row.fdevUnitState
                )
              }}
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 研发单元牵头人 -->
      <template v-slot:body-cell-fdevUnitLeaderInfo="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            :title="props.value.map(v => v.user_name_cn).join('，')"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-if="item.id"
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
              >
                {{ item.user_name_cn }}
              </router-link>
              <span v-else class="span-margin">{{ item.user_name_cn }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 申请人 -->
      <template v-slot:body-cell-applicantName="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.applicantId"
                :to="{ path: `/user/list/${props.row.applicantId}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 审批状态 -->
      <template v-slot:body-cell-approveState="props">
        <fdev-td class="text-ellipsis">
          <div class="row no-wrap items-center">
            <f-status-color
              :gradient="
                approveStatus(props.row.approveState) | approveStatusFilter
              "
            ></f-status-color>

            <span :title="approveStatus(props.row.approveState)">
              {{ approveStatus(props.row.approveState) }}
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 审批人 -->
      <template v-slot:body-cell-approverName="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.approverId"
                :to="{ path: `/user/list/${props.row.approverId}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { doneReUnitColumns } from '@/modules/HomePage/utils/constants';
import { devStatusColorMap } from '@/modules/Rqr/model';

export default {
  name: 'unitApprovalsDone',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {
        page: 1, //页码
        rowsPerPage: 5, //每页数据大小
        rowsNumber: 0
      },
      rowSelected: [],
      columns: doneReUnitColumns(),
      eventDoneNum: 0,
      eventNum: {
        todo: 0,
        done: 0
      },
      rejectOpen: false,
      approveRejectReason: '',
      rejectId: '',
      approveOptions: [
        {
          label: '开发审批',
          value: '0'
        },
        {
          label: '超期审批',
          value: '1'
        },
        {
          label: '开发审批&超期审批',
          value: '2'
        }
      ]
    };
  },

  props: ['label', 'name'],
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentDonePage(val);
      },
      deep: true
    }
  },
  computed: {
    ...mapState('userForm', ['todosList']),
    ...mapState('demandsForm', ['myApproveList']),
    ...mapState('userActionSaveHomePage/myUnitPage', [
      'keywords',
      'approveTypeDone',
      'visibleColsDone',
      'currentDonePage'
    ])
  },
  filters: {
    statusFilter(val) {
      return devStatusColorMap[val];
    },
    approveStatusFilter(val) {
      const obj2 = {
        待审批:
          'linear-gradient(270deg, rgba(36,200,249,0.50) 0%, #24C8F9 100%)',
        通过: 'linear-gradient(270deg, rgba(77,187,89,0.50) 0%, #4DBB59 100%)',
        拒绝: 'linear-gradient(270deg, rgba(176,190,197,0.50) 0%, #B0BEC5 100%)'
      };
      return obj2[val];
    }
  },
  methods: {
    ...mapActions('userForm', ['queryTodos', 'updateLabelById']),
    ...mapActions('demandsForm', [
      'queryMyApproveList',
      'approvePass',
      'approveReject'
    ]),
    ...mapMutations('userActionSaveHomePage/myUnitPage', [
      'saveKeywords',
      'saveDoneType',
      'saveVisibleColumnsDone',
      'saveCurrentDonePage'
    ]),

    clearKeywords() {
      this.saveKeywords('');
      this.init();
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
    },
    approveStatus(status) {
      const obj1 = {
        wait: '待审批',
        pass: '通过',
        reject: '拒绝'
      };
      return obj1[status];
    },

    //查询全部跳转到研发单元审批列表
    jumpToApproveList() {
      this.$router.push('/rqrmn/rdUnitApprovalList');
    },
    findMyApproveList() {
      this.init();
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.init();
    },
    async init() {
      this.loading = true;
      let params = {
        pageSize: this.pagination.rowsPerPage, //页码
        pageNum: this.pagination.page,
        keyword: this.keywords,
        code: 'done',
        type: this.approveTypeDone ? this.approveTypeDone.value : ''
      };
      await this.queryMyApproveList(params);
      // 设置数据总条数
      this.pagination.rowsNumber = this.myApproveList.count;
      this.tableData = this.myApproveList.approveList;
      this.eventNum.todo = this.myApproveList.waitCount
        ? this.myApproveList.waitCount
        : 0;
      this.eventNum.done = this.myApproveList.doneCount
        ? this.myApproveList.doneCount
        : 0;
      this.$emit('input', this.eventNum);
      this.loading = false;
    }
  },
  created() {
    this.pagination = this.currentDonePage;
    this.init();
  },
  mounted() {
    const tempVisibleColumns = this.visibleColsDone;
    if (!this.visibleCols || this.visibleColsDone.length <= 1) {
      this.saveVisibleColumnsDone(tempVisibleColumns);
    }
  }
};
</script>

<style lang="stylus" scoped>
.border-right button:after
  content: '';
  border-right: 1px solid #DDDDDD;
  display: inline-block;
  height: 14px;
  width: 1px;
  position: absolute;
  right: -5px;
  top: 11px;
.border-right .inline-block:last-child button:after
  display:none !important
</style>
