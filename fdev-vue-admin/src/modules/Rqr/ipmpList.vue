<template>
  <f-block page>
    <fdev-table
      row-key="id"
      title="实施单元列表"
      titleIcon="list_s_f"
      :data="data"
      :columns="ipmpListColumn"
      class="my-sticky-column-table"
      :loading="loading"
      :on-search="findIpmpList"
      @request="pageList"
      :pagination.sync="pagination"
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
          label="实施单元内容/编号"
          class="col-4 q-pr-md"
          label-style="width:110px"
          bottom-page
        >
          <fdev-input
            use-input
            v-model="params.devKeyword"
            @keyup.enter="findIpmpList()"
            clearable
            @clear="findIpmpList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="需求名称/编号"
          class="col-4 q-pr-md"
          label-style="width:110px"
          bottom-page
        >
          <fdev-input
            use-input
            v-model="params.demandKeyWord"
            @keyup.enter="findIpmpList()"
            clearable
            @clear="findIpmpList()"
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
            @input="findIpmpList()"
            :options="demandTypeOptions"
          />
        </f-formitem>
        <f-formitem
          label="牵头小组"
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
            @input="findIpmpList()"
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
          label="牵头人"
          class="col-4 q-pr-md"
          label-style="width:110px"
          bottom-page
        >
          <fdev-select
            use-input
            v-model="params.demandLeader"
            :options="userOptions"
            @filter="userFilter"
            @input="findIpmpList()"
            option-label="user_name_cn"
            option-value="user_name_en"
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
        <f-formitem
          label="实施状态"
          class="col-4"
          bottom-page
          label-style="width:110px"
        >
          <fdev-select
            ref="status"
            multiple
            v-model="params.status"
            @input="findIpmpList($event)"
            :options="ipmpStatusOptions"
          >
          </fdev-select>
        </f-formitem>
      </template>
      <!-- 科技类型的实施单元  对应多个  需求 所以科技类型的 实施单元无法跳转到实施单元详情 -->
      <template v-slot:body-cell-implUnitNum="props">
        <fdev-td class="td-desc" :title="props.row && props.row.implUnitNum">
          <router-link
            v-if="props.row && props.row.implUnitNum"
            :to="
              `/rqrmn/unitDetail/${props.row.implUnitNum}/${props.row
                .demandId || 'noDemandInfo'}`
            "
            class="link"
          >
            {{ props.row.implUnitNum }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.implUnitNum }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <div v-else>{{ props.row.implUnitNum }}</div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-implContent="props">
        <fdev-td class="td-desc" :title="props.row && props.row.implContent">
          <router-link
            v-if="props.row && props.row.implUnitNum"
            :to="
              `/rqrmn/unitDetail/${props.row.implUnitNum}/${props.row
                .demandId || 'noDemandInfo'}`
            "
            class="link"
          >
            {{ props.row.implContent }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.implContent }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <div v-else>{{ props.row.implContent }}</div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-informationNum="props">
        <fdev-td class="td-desc" :title="props.row && props.row.informationNum">
          <router-link
            v-if="props.row && props.row.demandId"
            :to="`/rqrmn/rqrProfile/${props.row.demandId}`"
            class="link"
          >
            {{ props.row.informationNum }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.informationNum }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-implStatusName="props">
        <fdev-td class="td-desc" :title="props.row && props.row.implStatusName">
          <f-status-color
            :gradient="props.row.implStatusName | statusFilter"
          ></f-status-color>
          {{ props.row.implStatusName || '-' }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-informationTitle="props">
        <fdev-td
          class="td-desc"
          :title="props.row && props.row.informationTitle"
        >
          <router-link
            v-if="props.row && props.row.demandId"
            :to="`/rqrmn/rqrProfile/${props.row.demandId}`"
            class="link"
          >
            {{ props.row.informationTitle }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.informationTitle }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- fdev实施计划变更原因分类： -->
      <template v-slot:body-cell-implChangeTypeName="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            v-if="props.row && props.row.implChangeTypeName"
            :title="
              props.row.implChangeTypeName &&
                props.row.implChangeTypeName.join('，')
            "
          >
            <span>{{
              props.row.implChangeTypeName &&
                props.row.implChangeTypeName.join('，')
            }}</span>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 确认延期阶段 -->
      <template v-slot:body-cell-confirmDelayStage="props">
        <fdev-td class="text-ellipsis">
          <div
            class="text-ellipsis"
            :title="getConfirmDelayStageName(props.row.confirmDelayStage)"
          >
            <span>{{
              getConfirmDelayStageName(props.row.confirmDelayStage)
            }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 业务邮件确认 -->
      <template v-slot:body-cell-businessEmail="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            v-if="
              props.row &&
                props.row.businessEmail &&
                props.row.businessEmail.length > 0
            "
            :title="
              props.row.businessEmail &&
                props.row.businessEmail.map(v => v.businessEmailName).join('，')
            "
          >
            <span v-for="(item, index) in props.row.businessEmail" :key="index">
              <a @click="download(item.businessEmailPath)" class="link">
                {{ item.businessEmailName }}
              </a>
            </span>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 操作列 -->
      <template v-slot:body-cell-operation="props">
        <fdev-td :auto-width="true" class="td-padding">
          <div class="border-right">
            <div class="inline-block" style="display: inline-block;">
              <fdev-tooltip
                position="top"
                v-if="props.row.adjustDateButton === '1'"
              >
                仅本实施单元牵头人可操作
              </fdev-tooltip>
              <fdev-tooltip position="top" v-if="!props.row.adjustDateButton">
                仅业务实施单元可执行此操作
              </fdev-tooltip>
              <fdev-btn
                flat
                label="调整排期"
                class="q-mr-sm"
                :disable="
                  props.row.adjustDateButton === '1' ||
                    !props.row.adjustDateButton
                "
                @click="clickAdjustDate(props.row)"
              />
            </div>
            <div class="inline-block" style="display: inline-block;">
              <fdev-tooltip
                position="top"
                v-if="props.row.confirmDelayButton !== '0'"
              >
                {{ getToolTipMsg(props.row.confirmDelayButton) }}
              </fdev-tooltip>
              <fdev-btn
                flat
                label="确认延期"
                class="q-mr-sm"
                @click="clickConfirmDelay(props.row)"
                :disable="props.row.confirmDelayButton !== '0'"
              />
            </div>
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
        <f-formitem label="项目编号">
          <fdev-input v-model="moreParamsData.prjNum" clearable />
        </f-formitem>
        <f-formitem label="本组/本组及子组">
          <fdev-select
            v-model="moreParamsData.groupQueryType"
            :options="groupQueryTypeOptions"
            clearable
          />
        </f-formitem>
        <f-formitem label="调整排期阶段">
          <fdev-select
            ref="moreParamsData.applyStage"
            v-model="moreParamsData.applyStage"
            :options="applyStageOptions"
            clearable
          >
          </fdev-select>
        </f-formitem>
      </div>
      <template #btnSlot>
        <fdev-btn label="清空" outline dialog @click="resetMoreSearch"/>
        <fdev-btn label="查询" dialog @click="findMoreList"
      /></template>
    </f-dialog>
    <AdjustDateDialog
      v-if="adjustDateDialogOpen"
      v-model="adjustDateDialogOpen"
      :implUnitNum="implUnitNum"
      :implUnitId="implUnitId"
      @close="handleAdjustDateClose"
    ></AdjustDateDialog>
    <ConfirmDelayDialog
      v-if="confirmDelayDialogOpen"
      v-model="confirmDelayDialogOpen"
      :implUnitNum="implUnitNum"
      :implUnitId="implUnitId"
      @close="handleConfirmDelayClose"
    ></ConfirmDelayDialog>
  </f-block>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import { implStatusColorMap } from './model';
import {
  exportExcel,
  deepClone,
  getGroupFullName,
  formatOption,
  resolveResponseError
} from '@/utils/utils';
// import { formatUser } from '@/modules/User/utils/model';
import { ipmpListColumn } from '@/modules/Rqr/utils/constants.js';
import {
  queryIpmpUnitList,
  exportIpmpUnitList
} from '@/modules/Rqr/services/methods.js';
import SessionStorage from '#/plugins/SessionStorage';
import AdjustDateDialog from '@/modules/Rqr/Components/AdjustDateDialog';
import ConfirmDelayDialog from '@/modules/Rqr/Components/ConfirmDelayDialog';
export default {
  components: {
    AdjustDateDialog,
    ConfirmDelayDialog
  },
  name: 'ipmpList',
  data() {
    return {
      groupQueryType: { value: 0, label: '本组' }, //不包含子组
      groupQueryTypeOptions: [
        { value: 0, label: '本组' },
        { value: 1, label: '本组及子组' }
      ],

      moreSearch: false,
      saveUserSearchData: ['params', 'pagination', 'recordScroll'],
      loading: false,
      data: [],
      // 小组
      groups: [],
      groupOptions: [],
      deepCloneGroups: [],
      userOptions: null,
      demandTypeOptions: [
        { label: '全部', value: '' },
        { label: '业务', value: 'business' },
        { label: '科技内部', value: 'tech' }
      ],
      ipmpListColumn: ipmpListColumn,
      pagination: {
        page: 1, //页码
        rowsPerPage: 5, //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },
      params: {
        devKeyword: null, //实施单元
        demandKeyWord: null, //需求
        demandType: null, //类型
        group: null, //小组id
        demandLeader: null, //牵头人
        prjNum: null, //项目编号
        applyStage: null,
        groupQueryType: null, //本组及子组
        status: null //实施单元状态
      },
      moreParamsData: {
        applyStage: null, //调整排期阶段
        groupQueryType: null,
        prjNum: null
      },
      searchParams: {},
      recordScroll: 0,
      applyStageOptions: [
        { label: '全部', value: 'all' },
        { label: '未申请', value: 'noApply' },
        { label: '申请中', value: 'applying' },
        { label: '已完成', value: 'applied' }
      ],
      adjustDateDialogOpen: false,
      confirmDelayDialogOpen: false,
      implUnitNum: '',
      implUnitId: '',
      ipmpStatusOptions: [
        { label: '评估中', value: '评估中' },
        { label: '待实施', value: '待实施' },
        { label: '开发中', value: '开发中' },
        { label: '业务测试中', value: '业务测试中' },
        { label: '业务测试完成', value: '业务测试完成' },
        { label: '已投产', value: '已投产' },
        { label: '已撤销', value: '已撤销' },
        { label: '暂缓', value: '暂缓' },
        { label: '暂存', value: '暂存' }
      ]
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
    //查询列表数据
    this.findIpmpList();
    //获取 查询条件的下拉
    this.getData();
    this.loading = false;
  },
  filters: {
    statusFilter(val) {
      return implStatusColorMap[val];
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
        this.params.applyStage != null || this.params.groupQueryType != null
      );
    }
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    // ...mapActions('user', {
    //   queryUser: 'fetch'
    // }),
    ...mapActions('jobForm', {
      downExcel: 'downExcel'
    }),
    ...mapActions('dashboard', ['queryUserCoreData']),
    getConfirmDelayStageName(row) {
      if (row && row.length > 0) {
        let confirmDelayStageNameList = [];
        let list = [
          { label: '提交用户测试延期', value: 'testStartDelay' },
          { label: '启动延期', value: 'developDelay' },
          { label: '提交测试完成延期', value: 'testFinishDelay' },
          { label: '投产延期', value: 'productDelay' }
        ];
        list.forEach(val => {
          row.filter(item => {
            if (val.value === item) {
              confirmDelayStageNameList.push(val.label);
            }
          });
        });
        return confirmDelayStageNameList.join('，');
      } else {
        return '-';
      }
    },
    getCacheData() {
      this.saveUserSearchData.forEach(x => {
        let val = SessionStorage.getItem(this.$route.fullPath + '_' + x);
        val && (this[x] = val);
      });
    },
    pageList(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.findIpmpList();
    },
    beforeShow() {
      this.moreParamsData.applyStage = this.params.applyStage;
      this.moreParamsData.groupQueryType = this.params.groupQueryType;
      this.moreParamsData.prjNum = this.params.prjNum;
    },
    resetMoreSearch() {
      this.moreSearch = false;
      this.moreParamsData.applyStage = this.params.applyStage = null;
      this.moreParamsData.groupQueryType = this.params.groupQueryType = null;
      this.moreParamsData.prjNum = this.params.prjNum = null;
      this.findIpmpList();
    },
    findMoreList() {
      this.moreSearch = false;
      this.params.applyStage = this.moreParamsData.applyStage;
      this.params.groupQueryType = this.moreParamsData.groupQueryType;
      this.params.prjNum = this.moreParamsData.prjNum;
      this.findIpmpList();
    },
    async findIpmpList() {
      let status = [];
      if (this.params.status && this.params.status.length > 0) {
        this.params.status.map(item => {
          status.push(item.value);
        });
      }
      this.loading = true;
      this.searchParams.size = this.pagination.rowsPerPage;
      this.searchParams.index = this.pagination.page;

      this.searchParams.keyword = this.params.devKeyword; //实施单元内容/编号
      this.searchParams.demandKey = this.params.demandKeyWord; //需求
      this.searchParams.implUnitType =
        this.params.demandType && this.params.demandType.value; //类型
      this.searchParams.groupIds =
        this.params.group && this.params.group.map(item => item.id); //小组id
      this.searchParams.implLeader =
        this.params.demandLeader && this.params.demandLeader.user_name_en; //牵头人
      this.searchParams.prjNum = this.params.prjNum; //项目编号
      this.searchParams.groupQueryType =
        (this.params.groupQueryType &&
          String(this.params.groupQueryType.value)) ||
        '0';
      this.searchParams.applyStage =
        this.params.applyStage && this.params.applyStage.value;
      this.searchParams.implStatusNameList = status;
      const res = await queryIpmpUnitList(this.searchParams);
      this.data = res.data;
      // 设置数据总条数
      this.pagination.rowsNumber = res.count;
      this.loading = false;
    },
    async getData() {
      Promise.all([
        // 小组
        this.fetchGroup(),
        //用户
        this.queryUserCoreData({ status: '0' })
      ]).then(() => {
        // 小组数据处理
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
        // 用户数据处理
        this.users = this.userList.slice(0);
        // this.users = this.userList.map(user =>
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
      let res = await resolveResponseError(() => exportIpmpUnitList(param));
      exportExcel(res);
    },
    getToolTipMsg(row) {
      if (!row) {
        return '仅业务实施单元可执行此操作';
      }
      const obj1 = {
        '1': '仅本实施单元牵头人可操作',
        '2': '该实施单元未延期'
      };
      return obj1[row];
    },
    //调整排期
    clickAdjustDate(row) {
      this.adjustDateDialogOpen = true;
      this.implUnitNum = row.implUnitNum;
      this.implUnitId = row.id;
    },
    clickConfirmDelay(row) {
      this.confirmDelayDialogOpen = true;
      this.implUnitNum = row.implUnitNum;
      this.implUnitId = row.id;
    },

    async download(path) {
      let param = {
        path: path,
        moduleName: 'fdev-demand'
      };
      await this.downExcel(param);
    },
    handleAdjustDateClose() {
      this.adjustDateDialogOpen = false;
      this.findIpmpList();
    },
    handleConfirmDelayClose() {
      this.confirmDelayDialogOpen = false;
      this.findIpmpList();
    }
  }
};
</script>
<style scoped lang="stylus">
.td-desc
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
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
