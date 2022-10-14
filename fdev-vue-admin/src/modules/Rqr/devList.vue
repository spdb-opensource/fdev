<template>
  <f-block page>
    <fdev-table
      row-key="id"
      title="研发单元列表"
      titleIcon="list_s_f"
      :data="data"
      :columns="devListColumn"
      class="my-sticky-column-table"
      :loading="loading"
      @request="pageList"
      :pagination.sync="pagination"
      :on-search="findDevList"
      :export-func="handleExportExcel"
    >
      <template #top-right>
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
      </template>
      <template v-slot:top-bottom>
        <f-formitem
          label="研发单元内容/编号"
          class="col-4 q-pr-md"
          label-style="width:110px"
          bottom-page
        >
          <fdev-input
            use-input
            v-model="params.devKeyword"
            @keyup.enter="findDevList()"
            clearable
            @clear="findDevList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="所属小组"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:110px"
        >
          <fdev-select
            use-input
            multiple
            v-model="params.group"
            :options="groupOptions"
            @filter="groupInputFilter"
            option-label="name"
            option-value="id"
            @input="findDevList()"
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
          label="状态"
          class="col-4 "
          bottom-page
          label-style="width:110px"
        >
          <fdev-select
            multiple
            v-model="params.status"
            @input="findDevList()"
            :options="statusOptions"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="牵头人"
          class="col-4 q-pr-md"
          label-style="width:110px"
          bottom-page
        >
          <fdev-select
            use-input
            multiple
            v-model="params.demandLeader"
            :options="userOptions"
            @filter="userFilter"
            @input="findDevList()"
            option-label="user_name_cn"
            option-value="id"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label
                    :title="`${scope.opt.user_name_en}--${scope.opt.groupName}`"
                    caption
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template></fdev-select
          >
        </f-formitem>
        <!-- 需求名称/编号 -->
        <f-formitem
          label="需求名称/编号"
          class="col-4 q-pr-md"
          label-style="width:110px"
          bottom-page
        >
          <fdev-input
            use-input
            v-model="params.demandKey"
            @keyup.enter="findDevList()"
            clearable
            @clear="findDevList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="类型"
          class="col-4 "
          bottom-page
          label-style="width:110px"
        >
          <fdev-select
            v-model="params.demandType"
            @input="findDevList()"
            :options="demandTypeOptions"
          />
        </f-formitem>
      </template>
      <template v-slot:body-cell-fdev_implement_unit_no="props">
        <fdev-td :title="props.value">
          <div class="text-ellipsis">
            <f-icon
              v-if="props.row.delayFlag == true"
              name="alert_t_f"
              style="color:red"
              title="延期告警！"
            />
            <router-link
              v-if="props.row.demand_id && props.row.fdev_implement_unit_no"
              class="link"
              :to="{
                path: '/rqrmn/devUnitDetails',
                query: {
                  demandId: props.row.demand_id,
                  dev_unit_no: props.row.fdev_implement_unit_no
                }
              }"
            >
              {{ props.row.fdev_implement_unit_no }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.fdev_implement_unit_no }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.fdev_implement_unit_no }}</span>
          </div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-implement_unit_content="props">
        <fdev-td :title="props.value">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.demand_id && props.row.fdev_implement_unit_no"
              class="link"
              :to="{
                path: '/rqrmn/devUnitDetails',
                query: {
                  demandId: props.row.demand_id,
                  dev_unit_no: props.row.fdev_implement_unit_no
                }
              }"
            >
              {{ props.row.implement_unit_content }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.implement_unit_content }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.implement_unit_content }}</span>
          </div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-demand_no="props">
        <fdev-td :title="props.row.demand_no">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.demand_id && props.row.demand_name"
              :to="`/rqrmn/rqrProfile/${props.row.demand_id}`"
              class="link"
            >
              {{ props.row.demand_no }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.demand_no }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.demand_no || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-demand_name="props">
        <fdev-td :title="props.row.demand_name">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.demand_id && props.row.demand_name"
              :to="`/rqrmn/rqrProfile/${props.row.demand_id}`"
              class="link"
            >
              {{ props.row.demand_name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.demand_name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.demand_name || '-' }}</span>
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-create_user_all="props">
        <fdev-td
          :title="
            props.row.create_user_all && props.row.create_user_all.user_name_cn
          "
        >
          <router-link
            :to="{
              path: `/user/list/${props.row.id && props.row.create_user_all.id}`
            }"
            class="link"
          >
            {{
              props.row.create_user_all &&
                props.row.create_user_all.user_name_cn
            }}
          </router-link>
        </fdev-td>
      </template>
      <template v-slot:body-cell-implement_leader_all="props">
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
      <template v-slot:body-cell-ipmp_implement_unit_no="props">
        <fdev-td :title="props.row.ipmp_implement_unit_no">
          <div
            v-if="
              props.row.ipmp_implement_unit_no && props.row.have_link == '1'
            "
            class="text-ellipsis"
          >
            <span
              @click="goToDetail(props.row)"
              class="normal-link text-ellipsis"
            >
              <span class="text-ellipsis">
                {{ props.row.ipmp_implement_unit_no }}
              </span>
            </span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.ipmp_implement_unit_no }}
              </fdev-banner>
            </fdev-popup-proxy>
          </div>
          <div class="text-ellipsis" v-else>
            {{ props.row.ipmp_implement_unit_no || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.ipmp_implement_unit_no }}
              </fdev-banner>
            </fdev-popup-proxy>
          </div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-other_demand_task_num="props">
        <fdev-td :title="props.row.other_demand_task_num">
          <div
            class="text-ellipsis td-desc"
            v-if="props.row.other_demand_task_num"
          >
            <span
              @click="goToDetail(props.row)"
              class="normal-link text-ellipsis"
            >
              <span class="text-ellipsis">
                {{ props.row.other_demand_task_num }}
              </span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.other_demand_task_num }}
                </fdev-banner>
              </fdev-popup-proxy>
            </span>
          </div>
          <span v-else>
            {{ props.row.other_demand_task_num || '-' }}
          </span>
        </fdev-td>
      </template>
      <!-- 涉及UI还原审核 -->
      <template v-slot:body-cell-ui_verify="props">
        <fdev-td :title="props.row.ui_verify ? '涉及' : '不涉及'">
          {{ props.row.ui_verify ? '涉及' : '不涉及' }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-implement_unit_status_normal="props">
        <fdev-td>
          <div class="row no-wrap items-center">
            <f-status-color
              :gradient="
                devUnitStatus(
                  props.row.demand_type,
                  props.row.implement_unit_status_special,
                  props.row.implement_unit_status_normal
                ) | statusFilter
              "
            ></f-status-color>

            <span
              :title="
                devUnitStatus(
                  props.row.demand_type,
                  props.row.implement_unit_status_special,
                  props.row.implement_unit_status_normal
                )
              "
            >
              {{
                devUnitStatus(
                  props.row.demand_type,
                  props.row.implement_unit_status_special,
                  props.row.implement_unit_status_normal
                )
              }}
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog
      title="更多查询条件"
      v-model="moreSearch"
      @before-show="beforeShow"
    >
      <div class="q-gutter-y-lg">
        <f-formitem
          bottom-page
          label="实施单元编号"
          label-style="width:150px"
          help="查询业务/科技需求下的研发单元"
        >
          <fdev-input
            use-input
            v-model="moreParamsData.ipmpNo"
            clearable
            @keyup.enter="findMoreList()"
            @clear="findDevList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          bottom-page
          label="其他需求任务编号"
          label-style="width:150px"
          help="查询日常需求下的研发单元"
        >
          <fdev-input
            use-input
            v-model="moreParamsData.otherNo"
            clearable
            @keyup.enter="findMoreList()"
            @clear="findDevList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="延期选项"
          label-style="width:150px;"
          class="delay-type"
          bottom-page
        >
          <fdev-select
            v-model="moreParamsData.delayType"
            :options="delayTypeOptions"
            clearable
          >
          </fdev-select>
        </f-formitem>

        <f-formitem
          label="本组/本组及子组"
          label-style="width:150px;margin-right:0"
        >
          <fdev-select
            v-model="moreParamsData.groupQueryType"
            :options="groupQueryTypeOptions"
            clearable
          />
        </f-formitem>
      </div>

      <template #btnSlot>
        <fdev-btn label="清空" outline dialog @click="resetMoreSearch"/>
        <fdev-btn label="查询" dialog @click="findMoreList"
      /></template>
    </f-dialog>
  </f-block>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import { devStatusColorMap } from './model';
import {
  exportExcel,
  deepClone,
  getGroupFullName,
  formatOption,
  resolveResponseError
} from '@/utils/utils';
// import { formatUser } from '@/modules/User/utils/model';
import {
  devListColumn,
  statusOptions,
  delayTypeList
} from '@/modules/Rqr/utils/constants.js';
import {
  queryFdevUnitList,
  exportFdevUnitList
} from '@/modules/Rqr/services/methods.js';
import SessionStorage from '#/plugins/SessionStorage';

export default {
  name: 'devList',
  data() {
    return {
      groupQueryTypeOptions: [
        { value: '0', label: '本组' },
        { value: '1', label: '本组及子组' }
      ],
      saveUserSearchData: ['params', 'pagination', 'recordScroll'],
      loading: false,
      data: [],
      // 小组
      groups: [],
      groupOptions: [],
      deepCloneGroups: [],
      // 状态
      statusOptions: statusOptions,
      userOptions: null,
      demandTypeOptions: [
        { label: '全部', value: '' },
        { label: '业务', value: 'business' },
        { label: '科技内部', value: 'tech' },
        { label: '日常', value: 'daily' }
      ],
      delayTypeOptions: delayTypeList,
      devListColumn: devListColumn,
      pagination: {
        page: 1, //页码
        rowsPerPage: 5, //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },
      params: {
        groupQueryType: null,
        devKeyword: '',
        group: null, //小组id
        status: null,
        demandLeader: null,
        demandKey: '',
        demandType: null,
        ipmpNo: null,
        otherNo: null,
        delayType: null
      },
      moreParamsData: {
        ipmpNo: null,
        otherNo: null,
        delayType: null,
        groupQueryType: null
      },
      searchParams: {},
      moreSearch: !true, //是否显示高级搜索
      recordScroll: 0
    };
  },
  beforeRouteEnter(to, from, next) {
    //只有初始化的时候滚动 查询不滚动
    let flag = true;
    next(vm => {
      // data指的是页面加载完成
      vm.$watch('loading', () => {
        if (flag) {
          flag = false;
          // from.path == '/'; //代表刷新页面
          if (from.path != '/') {
            window.scrollTo(0, vm.recordScroll);
          }
        }
      });
    });
  },
  beforeRouteLeave(to, from, next) {
    this.recordScroll = window.scrollY;
    this.saveUserSearchData.forEach(x => {
      let val = [undefined, null].includes(this[x]) ? '' : this[x];
      SessionStorage.set(from.fullPath + '_' + x, val);
      next();
    });
  },
  created() {
    // 获取缓存数据
    this.getCacheData();
    this.loading = true;
    //查询表格数据
    this.findDevList();
    //获取 查询条件的下拉
    this.getData();
    this.loading = false;
  },
  filters: {
    statusFilter(val) {
      return devStatusColorMap[val];
    }
  },
  computed: {
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('dashboard', ['userList']),
    // ...mapGetters('user', ['isLoginUserList']),
    hasMoreSearch() {
      return (
        (this.params.otherNo != null && this.params.otherNo != '') ||
        (this.params.ipmpNo != null && this.params.ipmpNo != '') ||
        this.params.delayType != null ||
        this.params.groupQueryType != null
      );
    }
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    // ...mapActions('user', {
    //   queryUser: 'fetch'
    // }),
    ...mapActions('dashboard', ['queryUserCoreData']),
    getCacheData() {
      this.saveUserSearchData.forEach(x => {
        let val = SessionStorage.getItem(this.$route.fullPath + '_' + x);
        val && (this[x] = val);
      });
    },
    goToDetail(data) {
      let type = data.demand_type;
      let num = data.ipmp_implement_unit_no;
      let demandId = data.demand_id;
      let otherDemandTaskNum = data.other_demand_task_num;
      if (type == 'daily') {
        this.$router.push(
          `/rqrmn/ODTaskDetail/${otherDemandTaskNum}/${demandId}`
        );
      } else {
        this.$router.push(
          `/rqrmn/unitDetail/${num}/${demandId || 'noDemandInfo'}`
        );
      }
    },
    devUnitStatus(type, special, normal) {
      const obj1 = {
        1: '评估中',
        2: type == 'daily' ? '未开始' : '待实施',
        3: type == 'daily' ? '进行中' : '开发中',
        4: 'sit',
        5: 'uat',
        6: 'rel',
        7: type == 'daily' ? '已完成' : '已投产',
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
    beforeShow() {
      this.moreParamsData.ipmpNo = this.params.ipmpNo;
      this.moreParamsData.otherNo = this.params.otherNo;
      this.moreParamsData.delayType = this.params.delayType;
      this.moreParamsData.groupQueryType = this.params.groupQueryType;
    },
    resetMoreSearch() {
      this.moreSearch = false;
      this.moreParamsData.ipmpNo = this.params.ipmpNo = null;
      this.moreParamsData.otherNo = this.params.otherNo = null;
      this.moreParamsData.delayType = this.params.delayType = null;
      this.moreParamsData.groupQueryType = this.params.groupQueryType = null;
      this.findDevList();
    },
    findMoreList() {
      this.moreSearch = false;
      this.params.ipmpNo = this.moreParamsData.ipmpNo;
      this.params.otherNo = this.moreParamsData.otherNo;
      this.params.delayType = this.moreParamsData.delayType;
      this.params.groupQueryType = this.moreParamsData.groupQueryType;

      this.findDevList();
    },
    pageList(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.findDevList();
    },
    async findDevList() {
      this.loading = true;
      this.searchParams.size = this.pagination.rowsPerPage;
      this.searchParams.index = this.pagination.page;
      this.searchParams.keyword = this.params.devKeyword;
      this.searchParams.groupIds =
        this.params.group && this.params.group.map(item => item.id);
      this.searchParams.states =
        this.params.status && this.params.status.map(item => item.value);
      this.searchParams.userIds =
        this.params.demandLeader &&
        this.params.demandLeader.map(item => item.id);
      this.searchParams.demandKey = this.params.demandKey;
      this.searchParams.demandType =
        this.params.demandType && this.params.demandType.value;
      this.searchParams.ipmpUnitNo = this.params.ipmpNo;
      this.searchParams.otherDemandTaskNum = this.params.otherNo;
      this.searchParams.groupQueryType =
        (this.params.groupQueryType && this.params.groupQueryType.value) || '0';
      this.searchParams.dateType = this.getDelayTypeObj(this.params.delayType);

      const res = await queryFdevUnitList(this.searchParams);
      this.data = res.data;
      // 设置数据总条数
      this.pagination.rowsNumber = res.count;
      this.loading = false;
    },
    getDelayTypeObj(type) {
      // 启动延期
      // 提交内测延期
      // 提交用户测试延期
      // 用户测试完成延期
      // 投产延期
      if ((type && !type.value) || !type) return null;
      const obj = {
        plan_start_date: {
          dateTypePlan: 'plan_start_date',
          dateTypeReal: 'real_start_date'
        },
        plan_inner_test_date: {
          dateTypePlan: 'plan_inner_test_date',
          dateTypeReal: 'real_inner_test_date'
        },
        plan_test_date: {
          dateTypePlan: 'plan_test_date',
          dateTypeReal: 'real_test_date'
        },
        plan_test_finish_date: {
          dateTypePlan: 'plan_test_finish_date',
          dateTypeReal: 'real_test_finish_date'
        },
        plan_product_date: {
          dateTypePlan: 'plan_product_date',
          dateTypeReal: 'real_product_date'
        }
      };

      return obj[type.value];
    },
    async getData() {
      Promise.all([
        // 小组
        this.fetchGroup(),
        //用户
        // this.queryUser()
        this.queryUserCoreData({ status: '0' })
      ]).then(() => {
        //处理小组数据
        this.groups = formatOption(this.groupsData);
        this.deepCloneGroups = deepClone(this.groups);
        this.deepCloneGroups.map(item => {
          let groupFullName = getGroupFullName(this.groups, item.id);
          item.label = groupFullName;
        });

        this.groupOptions = this.deepCloneGroups
          .concat([{ label: '小组', id: '' }])
          .filter(item => item.id);
        this.groupOptions.sort((a, b) => {
          return a.label.localeCompare(b.label, 'zh-CN');
        });
        //处理用户数据
        this.users = this.userList.slice(0);
        // this.users = this.isLoginUserList.map(user =>
        //   formatOption(formatUser(user), 'name')
        // );
      });
    },
    // 所属小组
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(tag =>
          tag.label.includes(val)
        );
      });
    },
    userFilter(val, update, abort) {
      update(() => {
        this.userOptions = this.users.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },
    async handleExportExcel() {
      let param = {
        ...this.searchParams,
        size: 0
      };
      let res = await resolveResponseError(() => exportFdevUnitList(param));
      exportExcel(res);
    }
  }
};
</script>
<style scoped lang="stylus">
.normal-link
  color #0663be
  cursor pointer;
.td-desc
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
.delay-type >>>
  .q-mr-md
    margin-right 0
//  >>> th:last-child, >>> td:last-child
//     position: relative !important;
//     box-shadow: none !important;
</style>
