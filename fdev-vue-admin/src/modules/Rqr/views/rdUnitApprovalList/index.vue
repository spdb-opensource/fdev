<template>
  <Loading :visible="loading">
    <f-block>
      <fdev-table
        :data="tableData"
        :columns="columns"
        row-key="id"
        :pagination.sync="pagination"
        :visible-columns="visibleColumns"
        :onSelectCols="updateVisibleColumns"
        @request="pageTableRequest"
        :loading="loading"
        titleIcon="list_s_f"
        title="研发单元审批列表"
        class="my-sticky-column-table"
        :on-search="findApproveList"
        :export-func="handleDownload"
      >
        <template v-slot:top-right>
          <fdev-btn
            normal
            label="高级搜索"
            @click="moreSearch = true"
            ficon="search"
          />
        </template>
        <template v-slot:top-bottom>
          <f-formitem
            label="研发单元编号/内容"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-input
              v-model="params.fdevUnitKey"
              @keyup.enter="handleEnterFun()"
              clearable
              @clear="clearFdevUnitKeyFun()"
            >
            </fdev-input>
          </f-formitem>
          <f-formitem
            label="需求名称/编号"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-input
              v-model="params.demandKey"
              @keyup.enter="handleEnterFun()"
              clearable
              @clear="clearDemandKeyFun()"
            >
            </fdev-input>
          </f-formitem>

          <f-formitem
            label="牵头小组"
            class="col-4"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              use-input
              multiple
              ref="groupIds"
              v-model="params.groupIds"
              @input="handleEnterFun"
              :options="groupOptions"
              @filter="groupInputFilter"
              option-label="name"
              option-value="label"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name">{{
                      scope.opt.name
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.label" caption>
                      {{ scope.opt.label }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:110px"
            bottom-page
            label="牵头人"
          >
            <fdev-select
              use-input
              multiple
              @filter="userFilter"
              :options="userOptions"
              option-label="user_name_cn"
              option-value="id"
              v-model="params.fdevUnitLeaderIds"
              @input="handleEnterFun"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">{{
                      scope.opt.user_name_cn
                    }}</fdev-item-label>
                    <fdev-item-label
                      caption
                      :title="
                        `${scope.opt.user_name_en}--${scope.opt.groupName}`
                      "
                    >
                      {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="状态"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              ref="approveStates"
              multiple
              v-model="params.approveStates"
              @input="handleEnterFun"
              :options="approveStatusOptions"
            >
            </fdev-select>
          </f-formitem>
          <f-formitem
            class="col-4"
            label-style="width:110px"
            bottom-page
            label="审批人"
          >
            <fdev-select
              use-input
              multiple
              @filter="approverIdsFilterUser"
              :options="approverIdsOptions"
              option-label="user_name_cn"
              option-value="id"
              v-model="params.approverIds"
              @input="handleEnterFun()"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">{{
                      scope.opt.user_name_cn
                    }}</fdev-item-label>
                    <fdev-item-label
                      caption
                      :title="
                        `${scope.opt.user_name_en}--${scope.opt.groupName}`
                      "
                    >
                      {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
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
          <fdev-td>
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
      <f-dialog title="更多查询条件" v-model="moreSearch">
        <div class="q-gutter-y-lg">
          <f-formitem label="审批类型">
            <fdev-select
              v-model="params.type"
              :options="approveOptions"
              @input="
                handleEnterFun();
                moreSearch = false;
              "
              clearable
            >
            </fdev-select>
          </f-formitem>
        </div>
      </f-dialog>
    </f-block>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';

import { formatUser } from '@/modules/User/utils/model';
import { devStatusColorMap } from '@/modules/Rqr/model';
import {
  rdunitColums,
  approveStatusOptions
} from '@/modules/Rqr/utils/constants';
import {
  deepClone,
  formatOption,
  //getGroupFullName,
  resolveResponseError,
  exportExcel
} from '@/utils/utils';
import SessionStorage from '#/plugins/SessionStorage';
import { exportApproveList } from '@/modules/Rqr/services/methods';
export default {
  name: 'List',
  components: { Loading },
  data() {
    return {
      groupOptions: [],
      groups: [],
      deepCloneGroups: [],
      columns: rdunitColums,
      approveStatusOptions: approveStatusOptions,
      userOptions: [],
      approverIdsOptions: [],
      tableData: [],
      loading: false,
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      params: {
        fdevUnitKey: '',
        demandKey: '',
        type: '',
        groupIds: [],
        fdevUnitLeaderIds: [],
        approveStates: [],
        approverIds: []
      },
      saveUserSearchData: ['params', 'pagination'],
      searchParams: {},
      moreSearch: false,
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

  computed: {
    ...mapState('userActionSaveDemands/rdUnitApprovalList', ['visibleColumns']),
    ...mapState('dashboard', ['userList']),
    ...mapState('demandsForm', ['approveList']),
    ...mapState('userForm', {
      groupsData: 'groups'
    })
  },
  filters: {
    statusFilter(val) {
      return devStatusColorMap[val];
    },
    //颜色
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
    ...mapMutations('userActionSaveDemands/rdUnitApprovalList', [
      'updateVisibleColumns'
    ]),
    ...mapActions('demandsForm', ['queryApproveList']),
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapActions('userForm', ['fetchGroup']),
    getCacheData() {
      this.saveUserSearchData.forEach(x => {
        let val = SessionStorage.getItem(this.$route.fullPath + '_' + x);
        val && (this[x] = val);
      });
    },
    // 需求名称/编号回车事件
    handleEnterFun() {
      this.queryReUnitApproval();
    },
    // 清除需求名称/编号
    clearDemandKeyFun() {
      this.queryReUnitApproval();
    },
    // 清除研发单元
    clearFdevUnitKeyFun() {
      this.queryReUnitApproval();
    },

    //研发单元状态匹配
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
    //状态转换
    approveStatus(status) {
      const obj1 = {
        wait: '待审批',
        pass: '通过',
        reject: '拒绝'
      };
      return obj1[status];
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数

      this.queryReUnitApproval();
    },
    //后端导出
    async handleDownload() {
      let param = {
        ...this.searchParams,
        size: 0
      };
      let res = await resolveResponseError(() => exportApproveList(param));
      exportExcel(res);
    },
    //牵头人过滤
    userFilter(val, update, abort) {
      update(() => {
        this.userOptions = this.users.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },
    //审批人过滤
    approverIdsFilterUser(val, update, abort) {
      update(() => {
        this.approverIdsOptions = this.users.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },
    //小组
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(tag =>
          tag.label.includes(val)
        );
      });
    },
    findApproveList() {
      this.queryReUnitApproval();
    },
    //查询审批列表
    async queryReUnitApproval() {
      this.loading = true;
      this.searchParams.pageSize = this.pagination.rowsPerPage;
      this.searchParams.pageNum = this.pagination.page;

      this.searchParams.fdevUnitKey = this.params.fdevUnitKey;
      this.searchParams.demandKey = this.params.demandKey;
      this.searchParams.groupIds =
        this.params.groupIds && this.params.groupIds.map(item => item.id);
      this.searchParams.fdevUnitLeaderIds =
        this.params.fdevUnitLeaderIds &&
        this.params.fdevUnitLeaderIds.map(item => item.id);
      this.searchParams.approverIds =
        this.params.approverIds && this.params.approverIds.map(item => item.id);
      this.searchParams.approveStates =
        this.params.approveStates &&
        this.params.approveStates.map(item => item.value);
      this.searchParams.type = this.params.type ? this.params.type.value : '';
      try {
        await this.queryApproveList(this.searchParams);
        // 设置数据总条数
        this.pagination.rowsNumber = this.approveList.count;
        this.tableData = this.approveList.approveList;
        this.loading = false;
      } catch (er) {
        this.loading = false;
      }
    }
  },

  async created() {
    //获取缓存数据
    this.getCacheData();
    // 查询列表数据
    await this.queryReUnitApproval(this.Param);
    // 获取小组和用户
    Promise.all([
      this.fetchGroup(),
      this.queryUserCoreData({ status: '0' })
    ]).then(() => {
      // 处理小组数据
      this.deepCloneGroups = deepClone(this.groupsData);
      this.deepCloneGroups.forEach((group, index) => {
        this.$set(this.deepCloneGroups[index], 'label', group.fullName);
      });
      this.groupOptions = this.deepCloneGroups;
      // 处理用户数据
      let users = this.userList;
      this.users = users.map(user => formatOption(formatUser(user), 'name'));
      this.userOptions = this.users.slice(0);
      this.approverIdsOptions = this.users.slice(0);
    });
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumns;
    if (!this.visibleColumns || this.visibleColumns.length <= 1) {
      this.updateVisibleColumns(tempVisibleColumns);
    }
  },
  beforeRouteLeave(to, from, next) {
    this.saveUserSearchData.forEach(x => {
      let val = [undefined, null].includes(this[x]) ? '' : this[x];
      SessionStorage.set(from.fullPath + '_' + x, val);
      next();
    });
  }
};
</script>

<style lang="stylus" scoped></style>
