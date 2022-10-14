<template>
  <div>
    <f-block class="to-bottom q-pt-llg ">
      <div class="row no-wrap ">
        <!-- 开始日期 -->
        <f-formitem
          profile
          label="选择日期"
          label-style="width:56px;line-height:14px;margin-right:32px"
          value-style="width:161px"
          class="q-mr-sm"
          ><f-date
            v-model="time.start_date"
            :options="startTimeOptions"
            ref="time.start_date"
            :rules="[val => !!val || '请选择开始日期']"
        /></f-formitem>
        <!-- 结束日期 -->
        <f-formitem
          profile
          label="至"
          label-style="width:14px;line-height:14px;margin-right:15px;margin-left:5px;"
          value-style="width:161px"
          ><f-date
            v-model="time.end_date"
            :options="endTimeOptions"
            ref="time.end_date"
            :rules="[val => !!val || '请选择结束日期']"
        /></f-formitem>
        <!-- 团队 -->
        <f-formitem
          profile
          label="团队"
          label-style="width:56px;margin-left:19px;line-height:14px;margin-right: 32px;"
          value-style="width:200px"
          ><fdev-select
            use-input
            @filter="filterGroups"
            option-label="name"
            option-value="id"
            v-model="$v.groupObj.$model"
            ref="groupObj"
            multiple
            :rules="[val => !!val.length || '请选择团队']"
            :options="groupList"
            placeholder="请选择"
            ><template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">
                    {{ scope.opt.name }}</fdev-item-label
                  >
                  <fdev-item-label :title="scope.opt.fullName" caption>
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template></fdev-select
          ></f-formitem
        >
        <!-- 是否包含子组 -->
        <f-formitem
          label="包含子组"
          label-style="width:56px;margin-left:10px"
          value-style="width:60px"
          ><fdev-toggle v-model="includeChild" left-label size="lg"
        /></f-formitem>
        <fdev-space class="q-pl-xs" />
        <!-- 查询/导出按钮 -->
        <div class="row no-wrap">
          <fdev-btn
            ficon="search"
            dialog
            label="查询"
            @click="searchFunc"
            style="width:80px"
            v-forbidMultipleClick
          />
          <fdev-btn
            ficon="exit"
            class="q-ml-md"
            dialog
            label="导出任务详情"
            style="width:136px"
            @click="exportDetailFunc"
            v-forbidMultipleClick
          />
        </div>
      </div>
      <div class="row no-wrap">
        <!-- 需求任务类型选择 -->
        <f-formitem
          label="需求类型"
          label-style="width:56px;margin-right:32px"
          value-style="width:366px"
        >
          <fdev-select
            ref="demandType"
            v-model="demandType"
            :options="demandTypeOptions"
            multiple
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :rules="[val => !!val.length || '需求类型不能为空']"
          />
        </f-formitem>
        <f-formitem
          clearable
          label="任务类型"
          label-style="width:56px;margin-left:19px;margin-right:32px"
          value-style="width:200px"
        >
          <fdev-select
            v-model="taskType"
            :options="taskTypeOptions"
            multiple
            map-options
            emit-value
            option-label="label"
            option-value="value"
            option-disable="inactive"
          />
        </f-formitem>
      </div>
    </f-block>
    <f-block class="data-block">
      <div class="row">
        <fdev-tabs v-model="tab">
          <fdev-tab name="group" label="板块维度任务吞吐量" />
          <!-- <fdev-tab name="team" label="小组维度任务吞吐量" /> -->
        </fdev-tabs>
        <fdev-space />
        <!-- 导出按钮 -->
        <fdev-btn
          ficon="exit"
          normal
          class="q-mb-xs"
          label="导出"
          @click="exportFunc"
          :loading="btnLoading"
        />
      </div>
      <fdev-separator class="separator" />
      <!-- 展示的表格数据 -->
      <Loading :visible="tableLoading">
        <!-- 有数据时-->
        <components
          v-if="initTaskData"
          :render="render"
          :ref="tab"
          :is="tab === 'group' ? 'newGroupDimTable' : 'teamDimensionTable'"
          :dataSource="initTaskData"
          :taskType="initTaskType"
          :demandType="initDemandType"
          :includeChild="includeChild"
        />
        <!-- 无数据时 -->
        <div v-else class="column items-center">
          <f-image name="no_data" class="q-mt-xl" />
        </div>
      </Loading>
    </f-block>
  </div>
</template>
<script>
import moment from 'moment';
import { exportExcel } from '@/modules/Measure/utils/utils';
import { deepClone, resolveResponseError } from '@/utils/utils';
import { mapActions, mapState } from 'vuex';
import Loading from '@/components/Loading';
import { required } from 'vuelidate/lib/validators';
import {
  queryTaskThroughputStatistics,
  exportTaskThroughputDetail
} from '@/modules/Measure/services/methods.js';
import { demandOptions, taskOptions } from '@/modules/Measure/utils/constants';
import newGroupDimTable from '@/modules/Measure/views/staticReport/development/newGroupDimTable'; // 板块维度表格(公共组度量需求改造后)
//import groupDimensionTable from '@/modules/Measure/views/staticReport/development/groupDimensionTable'; // 板块维度表格
import teamDimensionTable from '@/modules/Measure/views/staticReport/development/teamDimensionTable'; // 小组维度表格

import SessionStorage from '#/plugins/SessionStorage';

export default {
  components: { newGroupDimTable, teamDimensionTable, Loading },
  data() {
    return {
      initTaskType: [],
      initDemandType: [],
      initTaskData: null,
      saveUserSearchData: [
        'time',
        'groupObj',
        'includeChild',
        'demandType',
        'taskType'
      ],
      time: {
        start_date: moment(new Date() - 24 * 60 * 60 * 30000).format(
          'YYYY-MM-DD'
        ), //开始时间
        end_date: moment(new Date()).format('YYYY-MM-DD') //结束时间
      }, //默认展示当前日期~往前30天数据
      cloneGroups: [],
      groupList: [], //下拉团队选项
      groupObj: [], //团队
      includeChild: false,
      demandType: ['business', 'tech'],
      taskType: [],
      tab: 'group', //团队维度：‘group’；小组维度：‘team’
      taskData: null, //返回的任务信息数据
      taskTypeOptions: taskOptions(),
      demandTypeOptions: demandOptions(),
      render: false,
      btnLoading: false, //下载按钮缓冲
      tableLoading: false //表格缓冲
    };
  },
  validations: {
    groupObj: {
      required
    },
    time: {
      end_date: {
        required
      },
      start_date: { required }
    }
  },
  watch: {
    demandType(val) {
      if (val.length == 1 && val.includes('daily')) {
        this.taskType = [2]; //选中日常任务
        this.taskTypeOptions[0].inactive = true;
        this.taskTypeOptions[1].inactive = true;
        this.taskTypeOptions[2].inactive = false;
      } else if (!val.length) {
        this.taskType = [];
        this.taskTypeOptions.map(val => (val.inactive = true));
      } else {
        this.taskTypeOptions.map(val => (val.inactive = false));
      }
    }
  },
  computed: {
    ...mapState('userForm', ['groups']),
    ...mapState('user', ['currentUser'])
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    // 初始化
    async init() {
      // 获取小组信息
      await this.fetchGroup();
      this.cloneGroups = deepClone(this.groups);
      this.groupList = this.cloneGroups;
      this.searchFunc();
    },
    // 查询
    async searchFunc() {
      let params = {
        startDate: this.time.start_date,
        endDate: this.time.end_date,
        groupIds: this.groupObj.map(item => item.id),
        includeChild: this.includeChild,
        demandType: this.demandType ? this.demandType : [],
        taskType: this.taskType
      };
      if (this.checkGroupEmpty()) {
        this.tableLoading = true;
        try {
          this.taskData = await resolveResponseError(() =>
            queryTaskThroughputStatistics(params)
          );
          this.tableLoading = false;
        } catch (e) {
          this.tableLoading = false;
        }
        this.initTaskData = this.taskData;
        this.initDemandType = this.demandType;
        this.initTaskType = this.taskType;
      }
      this.saveUserSearchData.forEach(x => {
        let val = [undefined, null].includes(this[x]) ? '' : this[x];
        SessionStorage.set(this.$route.fullPath + '_' + x, val);
      });
    },
    // 导出任务详情
    async exportDetailFunc() {
      if (this.checkGroupEmpty()) {
        let data = await resolveResponseError(() =>
          exportTaskThroughputDetail({
            startDate: this.time.start_date,
            endDate: this.time.end_date,
            groupIds: this.groupObj.map(item => item.id),
            includeChild: this.includeChild,
            taskType: this.taskType,
            demandType: this.demandType ? this.demandType : []
          })
        );
        exportExcel(data);
      }
    },
    // 导出
    exportFunc() {
      this.btnLoading = true;
      this.tab === 'group'
        ? this.$refs.group.childExport()
        : this.$refs.team.childExport();
      this.btnLoading = false;
    },
    // 开始日期范围控制
    startTimeOptions(date) {
      if (this.time.end_date) {
        return date < this.time.end_date.replace(/-/g, '/');
      }
      return true;
    },
    // 结束日期范围控制
    endTimeOptions(date) {
      this.time.start_date = this.time.start_date ? this.time.start_date : '';
      return date > this.time.start_date.replace(/-/g, '/');
    },
    filterGroups(val, update) {
      update(() => {
        this.groupList = this.cloneGroups.filter(v => {
          return v.fullName.includes(val) || v.name.includes(val);
        });
      });
    },
    // 校验团队是否输入
    checkGroupEmpty() {
      this.$v.$touch();
      this.$refs[('groupObj', 'time.end_date', 'time.start_date')].validate();
      if (this.$v.$invalid || !this.$refs['demandType'].validate()) {
        return false;
      }
      return true;
    },
    getCacheData() {
      this.saveUserSearchData.forEach(x => {
        let val = SessionStorage.getItem(this.$route.fullPath + '_' + x);
        val && (this[x] = val);
      });
    }
  },
  created() {
    this.tableLoading = true;
    // 默认查询用户所在组数据
    this.groupObj = [
      { id: this.currentUser.group_id, name: this.currentUser.group.name }
    ];
    this.getCacheData();
    this.init();
  }
  // beforeRouteLeave(to, from, next) {
  //   this.saveUserSearchData.forEach(x => {
  //     let val = [undefined, null].includes(this[x]) ? '' : this[x];
  //     SessionStorage.set(from.fullPath + '_' + x, val);
  //     next();
  //   });
  // }
};
</script>
<style lang="stylus" scoped>
.to-bottom
  margin-bottom:10px
  padding-bottom:28px
.data-block
  min-height:300px
.separator
  margin 0
  background-color alpha(#ECEFF1, 1)
</style>
