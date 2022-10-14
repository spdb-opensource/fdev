<template>
  <div>
    <!-- 搜索条件 -->
    <f-block>
      <fdev-tabs
        align="left"
        v-model="type"
        class="text-grey-7"
        active-color="primary"
        indicator-color="primary"
      >
        <fdev-tab name="demand" label="设计稿审核" />
        <fdev-tab name="task" label="设计还原审核" />
      </fdev-tabs>
      <!-- 维度查询、重置搜索按钮 -->
      <div class="div1">
        <f-icon
          name="search_s_f"
          class="text-primary mt10 transformf"
          :width="18"
          :height="18"
        ></f-icon>
        <span class="span1">维度查询</span>
        <div class="btnDiv">
          <fdev-btn
            normal
            label="重置"
            @click="reset"
            ficon="refresh_c_o"
            class="q-mr-md"
          />
          <fdev-btn label="查询" dialog @click="query" ficon="search" />
        </div>
      </div>
      <!-- 时间查询-->
      <div class="row no-wrap mt10">
        <f-formitem label="开始日期" label-style="width:128px" class="col-6">
          <f-date
            ref="startTime"
            v-model="startTime"
            mask="YYYY-MM-DD"
            @input="changeStart"
            :rules="[val => !!val || '开始日期不能为空']"
          />
        </f-formitem>
        <f-formitem label="结束日期" label-style="width:128px" class="col-6">
          <f-date
            ref="endTime"
            v-model="endTime"
            mask="YYYY-MM-DD"
            :options="endTimefilter"
            @input="changeEnd"
            :rules="[val => !!val || '结束日期不能为空']"
          />
        </f-formitem>
      </div>
      <!-- 查询项目、审核人员-->
      <div class="row no-wrap">
        <f-formitem label="查询项目" label-style="width:128px" class="col-6">
          <fdev-select
            v-model="groupParent"
            :options="parentGroupList"
            option-value="id"
            option-label="name"
            @filter="groupFilter"
            @input="handleInputFun"
            use-input
          />
        </f-formitem>
        <f-formitem label="审核人员" label-style="width:128px" class="col-6">
          <fdev-select
            v-model="reviewer"
            :options="userList"
            option-value="id"
            option-label="user_name_cn"
            @filter="userFilter"
            use-input
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
      </div>
      <!-- 项目小组 -->
      <div class="row no-wrap mt20">
        <f-formitem label="项目小组" full-width label-style="width:128px">
          <fdev-checkbox label="全部" v-model="allCheck" class="q-pr-md" />
          <fdev-checkbox
            v-for="(group, index) in childList"
            :key="index"
            :val="group.id"
            :label="group.name"
            v-model="groupCheckedArr"
            class="q-pr-md"
          />
        </f-formitem>
      </div>
      <!-- 高级筛选 -->
      <div class="mt20">
        <f-formitem
          label="高级选项"
          label-style="width:128px"
          v-if="type == 'task'"
        >
          <fdev-toggle
            v-model="showQuery"
            size="lg"
            class="q-mr-lg"
            @input="getCurToggleStateFun"
          />
          <fdev-btn
            flat
            ficon="add"
            label="添加筛选条件"
            v-show="showQuery && customList.length < conditionOptions.length"
            @click="addItemFun"
          />
        </f-formitem>
        <div
          class="ml66 relative-position"
          :class="customList.length > 1 ? 'left-border' : ''"
          v-show="ifShow"
          v-if="type == 'task'"
        >
          <div class="absolute-btn" v-if="customList.length > 1">
            <div
              :class="this.active ? 'de-active' : 'active'"
              @click="handleToggleFun('and')"
            >
              且
            </div>
            <div
              :class="!this.active ? 'de-active' : 'active'"
              @click="handleToggleFun('or')"
            >
              或
            </div>
          </div>
          <div
            v-for="i in customList.length"
            :key="i"
            class="row no-wrap full-width q-mt-md pl72"
          >
            <fdev-select
              class="width280 q-mr-md"
              @input="handleSelectedFun(i)"
              v-model="customList[i - 1].firstSelected"
              :options="customList[i - 1].firstOptions"
              option-value="value"
              option-label="label"
            />
            <fdev-select
              class="width144 q-mr-md"
              @input="handleSelectedFun(0)"
              v-model="customList[i - 1].secondSelected"
              :options="customList[i - 1].secondOptions"
              option-value="value"
              option-label="label"
            />
            <fdev-select
              class="width280"
              @input="handleSelectedFun(0)"
              v-model="customList[i - 1].thirdSelected"
              :options="customList[i - 1].thirdOptions"
              option-value="value"
              option-label="label"
            />
            <fdev-btn
              flat
              class="scale q-ml-llg"
              ficon="delete_o"
              label=""
              @click="delItemFun(i - 1)"
            />
            <fdev-space />
            <fdev-btn
              flat
              class="align-base"
              :style="setHideFun(i)"
              ficon="arrow_u_o"
              label="收起"
              @click="hiddenFun"
            />
          </div>
        </div>
      </div>
      <!-- 任务进度详情 -->
      <div class="div1 row items-center">
        <f-icon
          name="detail_s_f"
          class="text-primary transformf"
          :width="18"
          :height="18"
        ></f-icon>
        <span class="span1" v-if="type == 'task'"
          >任务进度详情<f-icon
            name="help_c_o"
            class="text-primary cursor-pointer q-ml-xs"
            @click="helpDailog()"
            :width="16"
            :height="16"
          ></f-icon
        ></span>
        <span class="span1" v-if="type == 'demand'"
          >需求进度详情<f-icon
            name="help_c_o"
            class="text-primary cursor-pointer q-ml-xs"
            @click="helpDailog()"
            :width="16"
            :height="16"
          ></f-icon
        ></span>
      </div>
      <!-- 表格等切换 -->
      <div class="margin20 row no-warp">
        <fdev-btn-toggle
          v-model="model"
          :options="[
            { label: '表格视图', value: 'tableView' },
            { label: '进度条视图', value: 'planlView' }
          ]"
        />
        <fdev-space />
        <template v-if="model == 'tableView'">
          <fdev-btn
            flat
            class="download-btn q-mr-md"
            ficon="download"
            label="下载列表"
            @click="downloadExcel()"
          />
          <fdev-select
            flat
            multiple
            class="selected q-ml-md"
            v-model="visibleColumns"
            option-value="val"
            option-label="label"
            :options="columnsOptions"
            display-value="选择列"
          />
          <!-- <table-top @colListFun="reciveFun"></table-top> -->
        </template>
        <template v-if="model == 'planlView'">
          <div
            class="row items-center flex-end"
            v-for="(item, index) in tableMark"
            :key="index"
          >
            <span class="square" :class="item.color"></span>
            <span class="mb2">{{ item.text }}</span>
          </div>
        </template>
      </div>
      <!-- 下方表格及数据 -->
      <!-- 无数据时 -->
      <div v-if="reviewList.length === 0" class="flex-center nodata-style">
        <f-image name="no_data" />
      </div>
      <div v-if="reviewList.length !== 0">
        <div v-if="showMain">
          <div ref="tableView" v-if="model == 'tableView'">
            <TableView
              :reviewList="reviewList"
              :tabType="type"
              :visibleColumns="visibleColumns"
            />
          </div>
          <div ref="planlView" v-if="model == 'planlView'">
            <PlanView
              :cols="cols"
              :reviewList="reviewList"
              :mainData="mainData"
              :searchObj="searchObj"
              :showTask="showTask"
            />
          </div>
        </div>
      </div>
      <f-dialog right v-model="helpFlag" :title="helpTitle">
        <div class="divWidth">
          <p class="textTitle">审核轮数:</p>
          <p class="pstyle">
            审核记录中有完成记录代表一轮，如果有未通过审核记录，记录中有几条的阶段是审核未通过，那就再加几轮。
          </p>
          <div v-if="type == 'task'">
            <p class="textTitle">审核反馈-开发上传:</p>
            <div class="pstyle">每次审核未通过后开发修改耗时天数。<br /></div>
            <div class="pstyle">
              审核记录中如果没有未通过审核记录，没有修改过程。<br />
            </div>
            <div class="pstyle">
              如果有未通过审核记录，取每次审核未通过时间和紧跟着的开发上传截图时间的相差天数。<br />
            </div>
            <div class="pstyle">
              如果开发尚未上传，取和查询结束日期(如果结束日期晚于当前日期，取当前日期)的相差天数。<br />
            </div>
            <div class="pstyle">
              如果选择的关系是小于，需要每一个耗时天数都小于筛查条件值；如果是等于或大于，其中一个耗时天数等于或大于筛查条件值即可。
            </div>
            <p class="textTitle mrt16">开发上传-审核反馈:</p>
            <div class="pstyle">
              每次开发上传截图后审核人员完成审核耗时天数。
            </div>
            <div class="pstyle">
              审核记录中如果没有未通过审核记录且有审核完成记录，取完成审核时间和指定审核人员时间的相差天数。<br />
            </div>
            <div class="pstyle">
              如果既没有未通过记录也没有审核完成，说明一直未审核，取和查询结束日期(如果结束日期晚于当前日期，取当前日期)的相差天数。<br />
            </div>
            <div class="pstyle">
              如果有未通过审核记录，取其中每次开发上传截图时间和紧跟着的审核不通过时间的相差天数。<br />
            </div>
            <div class="pstyle">
              如果未通过审核记录中，最后一次是开发上传截图且没有审核完成记录，说明到当前时间尚未审核，取和查询结束日期(如果结束日期晚于当前日期，取当前日期)的相差天数。<br />
            </div>
            <div class="pstyle">
              如果选择的关系是小于，需要每一个耗时天数都小于筛查条件值；如果是等于或大于，其中一个耗时天数等于或大于筛查条件值即可。
            </div>

            <p class="textTitle mrt16">卡点状态:</p>
            <p class="pstyle">
              查询任务所属研发单元的信息，如果研发单元涉及UI还原审核，状态为正常，如果不涉及UI还原审核，状态为失败。
            </p>
          </div>
          <p class="textTitle">完成情况：</p>
          <p class="pstyle">
            如果当前审核阶段是已完成，且审核完成时间早于查询结束时间，完成情况为通过，如果当前审核状态是分配中/审核中/修改中，完成情况为未通过。
          </p>
          <p class="textTitle">当前阶段:</p>
          <div class="pstyle">
            分配中：开发发起ui还原性申请，未分配审核人员。<br />
          </div>
          <div class="pstyle">
            审核中：分配审核人员后，但未审核完成，且分配时间早于查询结束时间；开发人员二次及多次（非第一次）上传文件后，未审核完成，且记录中最后一条数据是开发上传审核文件，并且上传时间早于查询结束时间。<br />
          </div>
          <div class="pstyle">
            修改中：ui审核拒绝后，开发人员未上传材料，且审核记录中最后一条数据是审核未通过，并且审核不通过时间早于查询结束时间。<br />
          </div>
          <div class="pstyle">
            完成：审核通过，且审核完成时间早于查询结束时间。<br />
          </div>
        </div>
      </f-dialog>
    </f-block>
  </div>
</template>
<script>
import { mapState, mapMutations, mapActions } from 'vuex';
import { deepClone, exportExcel } from '@/utils/utils';
import moment from 'moment';
import PlanView from './components/PlanView';
import TableView from './components/TableView';
// import myTable from './table';
// import tableTop from './table/top.vue';
import {
  DesignType,
  StageType,
  cunstomQuery,
  dayOptions,
  timeOptions,
  kdResultOptions,
  doneResultOptions,
  currentOptions,
  tableMark,
  filterSArray,
  conditionOptions,
  //表格选择列数据
  columnsTaskOptions,
  columnsDemandOptions,
  visibleColumns,
  taskText
} from '@/modules/Dashboard/utils/constants';

export default {
  name: 'UIdesign',
  components: {
    PlanView,
    TableView
    // tableTop,
    // myTable
  },
  data() {
    return {
      childList: [],
      visibleColumns,
      taskText,
      helpFlag: false,
      startTime: '',
      endTime: '',
      showQuery: false,
      searchObj: {
        time: {}
      },
      reviewers: [],
      userList: [],
      customList: [], // 隐藏会清空
      cloneCustomList: [], // 显示高级筛选用于恢复数据
      andOrState: '',
      cloneAndOrState: '',
      active: false,
      ifShow: false,
      groupParent: null,
      parentGroupList: [],
      groupList: [],
      reviewer: {
        id: '',
        user_name_cn: '全部'
      },
      model: 'tableView',
      type: 'demand',
      groupCheckedArr: [],
      cols: [],
      limitOpt: {
        min: '',
        max: ''
      },
      mainData: [],
      showTask: false,
      showMain: true,
      allCheck: true,
      searchParam: {},
      flag: false,
      nowDate: moment().format('YYYY/MM/DD'),
      designType: DesignType,
      selection: [],
      conditionOptions,
      tableMark,
      selectedColList: [] // 选择列控制的数据
    };
  },
  computed: {
    ...mapState('user', ['currentUser', 'list']),
    ...mapState('userForm', ['roles']),
    ...mapState('dashboard', [
      'reviewList',
      'childGroupList',
      'downloadTable',
      'downloadDemandTable'
    ]),
    helpTitle() {
      let helpTitle = '';
      if (this.type == 'task') {
        helpTitle = '任务进度详情帮助';
      }
      if (this.type == 'demand') {
        helpTitle = '需求进度详情帮助';
      }
      return helpTitle;
    },
    columnsOptions() {
      let option = [];
      if (this.type == 'task') {
        option = visibleColumns.concat(columnsTaskOptions);
      }
      if (this.type == 'demand') {
        option = visibleColumns.concat(columnsDemandOptions);
      }
      return option;
    }
  },
  watch: {
    allCheck(val) {
      if (val) {
        this.groupCheckedArr = this.childList.map(item => {
          return item.id;
        });
      } else {
        if (!this.flag) {
          this.groupCheckedArr = [];
        }
      }
    },
    type(n) {
      this.cacheColFun(n);
      this.query();
    },
    groupCheckedArr(val) {
      this.flag = false;
      let mapLength = this.childList.map(item => {
        return item.id;
      }).length;
      if (val.length === mapLength) {
        this.allCheck = true;
      } else {
        this.allCheck = false;
        this.flag = true;
      }
    },
    model(val) {
      if (val) {
        this.query();
      }
    },
    visibleColumns(val) {
      sessionStorage.setItem(
        `${this.type}Col`,
        JSON.stringify(this.visibleColumns)
      );
    }
  },
  methods: {
    ...mapMutations('dashboard', ['saveGroupById']),
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('userForm', {
      fetchRole: 'fetchRole'
    }),
    ...mapActions('dashboard', [
      'queryReviewList',
      'queryReviewListDemand',
      'queryGroupById',
      'downLoadReviewList',
      'downLoadDemandReviewList'
    ]),
    helpDailog() {
      this.helpFlag = true;
    },
    mkcloumn(visibleColumns) {
      let colmap = {};
      visibleColumns.forEach(item => (colmap[item.val] = item.label));
      return colmap;
    },
    async downloadExcel() {
      if (this.type == 'demand') {
        let fixdemand = [
          { label: '项目小组', val: 'groupName' },
          { label: '数字', val: 'num' },
          { label: '需求名称', val: 'oa_contact_name' }
        ];
        let downDemadParam = {
          ...this.searchParam,
          searchList: this.processparam(this.customList),
          logicalOperator: this.andOrState,
          columnMap: this.mkcloumn(fixdemand.concat(this.visibleColumns))
        };
        await this.downLoadDemandReviewList(downDemadParam);
        exportExcel(this.downloadDemandTable);
      }
      if (this.type == 'task') {
        let fixtask = [
          { label: '项目小组', val: 'groupName' },
          { label: '数字', val: 'num' },
          { label: '需求编号', val: 'demandNo' },
          { label: '任务名称', val: 'name' },
          { label: '卡点状态', val: 'positionStatus' }
        ];
        let downTaskParam = {
          ...this.searchParam,
          searchList: this.processparam(this.customList),
          logicalOperator: this.andOrState,
          columnMap: this.mkcloumn(fixtask.concat(this.visibleColumns))
        };
        await this.downLoadReviewList(downTaskParam);
        exportExcel(this.downloadTable);
      }
    },
    endTimefilter(data) {
      return (
        data > moment(this.startTime).format('YYYY/MM/DD') &&
        data <
          moment(this.startTime)
            .add(3, 'month')
            .format('YYYY/MM/DD')
      );
    },
    filterRange(from) {
      let range = 3600 * 24 * 1000 * 31;
      let minTime =
        new Date(`${from.year}/${from.month}/${from.day}`).getTime() - range;
      let maxTime =
        new Date(`${from.year}/${from.month}/${from.day}`).getTime() + range;
      let minDate = moment(minTime).format('YYYY/MM/DD');
      let maxDate = moment(maxTime).format('YYYY/MM/DD');
      this.limitOpt = {
        min: minDate,
        max: maxDate
      };
    },
    filterDate(options) {
      if (this.limitOpt.min === '') {
        return true;
      }
      return options <= this.limitOpt.max && options >= this.limitOpt.min;
    },
    reset() {
      this.getMonthDate();
      this.reviewer = {
        id: '',
        user_name_cn: '全部'
      };
      this.groupCheckedArr = [];
      this.type = 'demand';
      this.showMain = false;
      this.allCheck = false;
    },
    async query() {
      this.showMain = true;
      this.searchParam = {
        reviewer: this.reviewer ? this.reviewer.id : '',
        internetChildGroupId: this.groupParent ? this.groupParent.id : '',
        group: this.groupCheckedArr,
        groupParent: this.groupParent,
        type: this.type,
        searchList: this.customList,
        startDate: this.startTime
          ? moment(this.startTime).format('YYYY-MM-DD')
          : '',
        endDate: this.endTime ? moment(this.endTime).format('YYYY-MM-DD') : ''
      };
      if (!this.endTime || !this.startTime) {
        let currentDate = new Date();
        let max = moment(currentDate)
          .endOf('month')
          .date();
        (this.startTime = this.endTime = moment(currentDate)
          .startOf('month')
          .format('YYYY/MM/DD')),
          (this.cols = this.getHeaderArr(1, max) || []);
      } else {
        this.cols =
          this.getHeaderArr(this.startTime.slice(-2), this.endTime.slice(-2)) ||
          [];
      }
      let param = {
        startDate: this.startTime
          ? moment(this.startTime).format('YYYY-MM-DD')
          : '',
        endDate: this.endTime ? moment(this.endTime).format('YYYY-MM-DD') : '',
        reviewer: this.reviewer ? this.reviewer.id : '',
        internetChildGroupId: this.groupParent ? this.groupParent.id : '',
        group: this.groupCheckedArr,
        searchList: this.processparam(this.customList),
        logicalOperator: this.andOrState
      };
      if (this.type === 'task') {
        await this.queryReviewList(param);
        this.showTask = true;
      } else {
        await this.queryReviewListDemand(param);
        this.showTask = false;
      }
      await this.$nextTick();
      if (
        (this.reviewList && this.reviewList.length === 0) ||
        this.model == 'tableView'
      )
        return;
      let length = document
        .getElementsByClassName('col-item')[0]
        .getBoundingClientRect().width;
      setTimeout(async () => {
        this.mainData = this.getContent();
        await this.$nextTick();
        let oneDay = 3600 * 24 * 1000;
        this.mainData.forEach(item => {
          if (item && Array.isArray(item)) {
            item.forEach(val => {
              if (val && Array.isArray(val) && val.length > 0) {
                val.forEach((el, index) => {
                  let marginLeft = 0;
                  // bool判断首个时间与startDate的大小
                  let bool =
                    new Date(el.startDate).getTime() -
                      new Date(this.searchObj.time.from).getTime() >=
                    0;
                  if (bool) {
                    marginLeft = Math.round(
                      ((new Date(el.startDate).getTime() -
                        new Date(this.searchObj.time.from).getTime()) /
                        oneDay) *
                        64
                    );
                  }
                  if (el.type === StageType.Assign) {
                    document
                      .getElementById(val[0].id + '-' + el.type + '-' + el.keep)
                      .setAttribute(
                        'style',
                        `width:${el.keep *
                          length}px;background:#F9816E;margin-left:${marginLeft}px`
                      );
                  } else {
                    document
                      .getElementById(val[0].id + '-' + el.type + '-' + el.keep)
                      .setAttribute(
                        'style',
                        `width:${el.keep * length}px;background:${
                          el.type === StageType.Examine
                            ? '#FFB64C'
                            : el.type === StageType.Suggest
                            ? '#5FACFB'
                            : '#5CC49E'
                        };`
                      );
                  }
                });
              }
            });
          }
        });
      }, 500);
    },
    processparam(list) {
      let returnlist = [];
      for (let i = 0; i < list.length; i++) {
        let searchMap = {
          searchKey: '',
          relationalOperator: '',
          searchValue: ''
        };
        if (
          list[i].firstSelected &&
          list[i].secondSelected &&
          list[i].thirdSelected.value
        ) {
          searchMap.searchKey = list[i].firstSelected.value;
          searchMap.relationalOperator = list[i].secondSelected.value;
          searchMap.searchValue = list[i].thirdSelected.value;
        }
        returnlist[i] = searchMap;
      }
      return returnlist;
    },
    //筛选组
    async handleInputFun(v) {
      if (v === null) {
        this.childList = [];
        this.groupCheckedArr = [];
      } else {
        await this.queryGroupById({
          id: v.id
        });
        this.childList = this.childGroupList;
        this.groupCheckedArr = this.childList.map(item => {
          return item.id;
        });
      }
    },
    groupFilter(val, update, abort) {
      update(() => {
        if (
          this.groupList &&
          Array.isArray(this.groupList) &&
          this.groupList.length > 0
        ) {
          this.parentGroupList = this.groupList.filter(group => {
            return group.name.indexOf(val) > -1;
          });
        }
      });
    },
    //筛选人
    userFilter(val, update, abort) {
      update(() => {
        if (
          this.reviewers &&
          Array.isArray(this.reviewers) &&
          this.reviewers.length > 0
        ) {
          this.userList = this.reviewers.filter(user => {
            return (
              user.user_name_cn.indexOf(val) > -1 ||
              (user.user_name_en &&
                user.user_name_en.toLowerCase().includes(val.toLowerCase()))
            );
          });
        }
      });
    },
    /*
     * 将模块下的任务计算为
    {
      startDate:该阶段的开始时间
      endDate：该阶段的结束时间
      type：类型
      id：任务id
    }
    分析所有情况下designMap的数据，目的是得到一个[ [ [],[],[] ], [], []]  结构的包含任务所有阶段的数组
     */
    getContent() {
      let res = new Array(this.reviewList.length).fill([]);
      this.reviewList.forEach((list, index) => {
        let taskList = [];
        let arr = ['finishedList', 'unfinishedList'];
        arr.forEach(listName => {
          list[listName].forEach((item, index1) => {
            let taskInfo = [];
            // 分配阶段
            if (item.designMap.fixing) {
              taskInfo.push({
                startDate: this.getSliceDate(item.designMap.wait_allot[0].time),
                endDate: this.getSliceDate(item.designMap.fixing[0].time),
                type: StageType.Assign,
                id: item.id
              });
            } else if (item.designMap.wait_allot) {
              // 还没有fixing阶段
              taskInfo.push({
                startDate: this.getSliceDate(item.designMap.wait_allot[0].time),
                endDate: this.nowDate,
                type: StageType.Assign,
                id: item.id
              });
            }

            // 审核阶段
            if (item.designMap.fixing && Array.isArray(item.designMap.fixing)) {
              // 有fixing和finished
              if (item.designMap.finished) {
                if (
                  item.designMap.nopass &&
                  Array.isArray(item.designMap.nopass)
                ) {
                  taskInfo.push({
                    type: StageType.Examine,
                    startDate: this.getSliceDate(item.designMap.fixing[0].time),
                    endDate: this.getSliceDate(item.designMap.nopass[0].time),
                    id: item.id
                  });
                } else {
                  taskInfo.push({
                    type: StageType.Examine,
                    startDate: this.getSliceDate(item.designMap.fixing[0].time),
                    endDate: this.getSliceDate(item.designMap.finished[0].time),
                    id: item.id
                  });
                }
              } else {
                if (!item.designMap.nopass) {
                  // 有fixing没有nopass  没有finished
                  taskInfo.push({
                    type: StageType.Examine,
                    startDate: this.getSliceDate(item.designMap.fixing[0].time),
                    endDate: this.nowDate,
                    id: item.id
                  });
                } else {
                  // 有fixing有nopass  没有finished
                  taskInfo.push({
                    type: StageType.Examine,
                    startDate: this.getSliceDate(item.designMap.fixing[0].time),
                    endDate: this.getSliceDate(item.designMap.nopass[0].time),
                    id: item.id
                  });
                }
              }
            }
            // 修改阶段
            if (item.designMap.nopass && Array.isArray(item.designMap.nopass)) {
              // 有nopass也finished
              item.designMap.nopass.forEach((design, index) => {
                if (typeof item.designMap.nopass[index + 1] !== 'undefined') {
                  // 非最后一个nopass阶段
                  if (design.stage === 'load_nopass') {
                    taskInfo.push({
                      startDate: this.getSliceDate(design.time),
                      endDate: this.getSliceDate(
                        item.designMap.nopass[index + 1].time
                      ),
                      type: StageType.Suggest,
                      id: item.id
                    });
                  } else {
                    taskInfo.push({
                      startDate: this.getSliceDate(design.time),
                      endDate: this.getSliceDate(
                        item.designMap.nopass[index + 1].time
                      ),
                      type: StageType.Examine,
                      id: item.id
                    });
                  }
                } else {
                  // 最后一个nopass阶段
                  if (item.designMap.finished) {
                    // finished
                    if (this.showTask) {
                      taskInfo.push({
                        startDate: this.getSliceDate(design.time),
                        endDate: this.getSliceDate(
                          item.designMap.finished[0].time
                        ),
                        type: StageType.Examine,
                        id: item.id
                      });
                    }
                  } else {
                    // 没有finished
                    //  是load_upload审核阶段
                    if (design.stage === 'load_upload') {
                      if (this.showTask) {
                        taskInfo.push({
                          startDate: this.getSliceDate(design.time),
                          endDate: this.nowDate,
                          type: StageType.Examine,
                          id: item.id
                        });
                      } else {
                        taskInfo.push({
                          startDate: this.getSliceDate(design.time),
                          endDate: this.nowDate,
                          type: StageType.Finish,
                          id: item.id
                        });
                      }
                    } else {
                      //  是load_nopass修改阶段
                      taskInfo.push({
                        startDate: this.getSliceDate(design.time),
                        endDate: this.nowDate,
                        type: StageType.Suggest,
                        id: item.id
                      });
                    }
                  }
                }
              });
            }
            if (item.designMap.finished) {
              taskInfo.push({
                startDate: this.getSliceDate(item.designMap.finished[0].time),
                endDate: this.getSliceDate(item.designMap.finished[0].time),
                type: StageType.Finish,
                id: item.id,
                finished: true
              });
            }

            taskList.push(taskInfo);
          });
        });
        res[index] = taskList;
      });
      // 计算用于设置节假日阴影div所需的几个样式
      let divHeight = document.getElementById('mainDiv')
        ? document.getElementById('mainDiv').offsetHeight
        : null;
      let divLeft = document.getElementsByClassName('flexDiv')[0]
        ? document.getElementsByClassName('flexDiv')[0].offsetLeft
        : null;
      // let divTop = document.getElementsByClassName('div3')[0].offsetHeight;
      document.getElementById('subDiv')
        ? document.getElementById('subDiv').setAttribute(
            'style',
            `
          position: absolute;
          width: 100%;
          height: ${divHeight}px;
          padding-left: ${divLeft}px;
          overflow-x: scroll;
          overflow-y: hidden;
          white-space: nowrap;
        `
          )
        : null;
      return this.getStages(res) || [];
    },
    // 计算实际需要在图标中展示的阶段，过滤掉搜索范围内的阶段
    getStages(res) {
      res.forEach(listName => {
        listName.forEach(task => {
          for (let i = task.length - 1; i >= 0; i--) {
            // 遍历每个任务对应计算出来的阶段数组，当搜索开始日期大于阶段开始日期，将该阶段的开始日期保存为搜索开始日期，即去除搜索开始日期之前的不展示的阶段
            if (this.searchObj.time.from > task[i].startDate) {
              task[i].startDate = this.searchObj.time.from;
            }
            // 当搜索结束日期小于阶段结束日期，将该阶段的结束日期保存为搜索结束日期，即去除搜索结束日期之后的不展示的阶段
            if (this.searchObj.time.to < task[i].endDate) {
              task[i].endDate = this.searchObj.time.to;
            }
            // 计算每个阶段的持续时间keep
            task[i].keep = this.getDay(task[i].startDate, task[i].endDate);
            // 当阶段结束日期小于搜索开始日期或者阶段开始日期大于搜索结束日期时，将该阶段的keep值设为0，即去除这些不需要展示的阶段
            if (
              task[i].endDate < this.searchObj.time.from ||
              task[i].startDate > this.searchObj.time.to
            ) {
              task[i].keep = 0;
            }
          }
        });
      });
      return res;
    },
    /**
      @param {String} start  开始日期
      @param {String} end 结束日期
      注：当天进行的操作 比如分配阶段-审核阶段 是当天完成，减出来的结果是0
      不好展示0，所以默认计算为0的话设置0.1天
     */
    getDay(start, end) {
      if (start && end) {
        let oneDay = 3600 * 24 * 1000;
        let timeDistance = Math.floor(
          (new Date(end.substr(0, 10)).getTime() -
            new Date(start.substr(0, 10)).getTime()) /
            oneDay
        );
        if (timeDistance <= 0) {
          // 如果间隔时间为0 默认0.1天
          return 0.1 + Math.random() / 1000;
        } else {
          return timeDistance;
        }
      } else {
        return 0;
      }
    },
    getSliceDate(date) {
      return moment(date).format('YYYY/MM/DD');
    },
    // 计算这天是否是节假日
    isHoliday(day) {
      let dayDate = this.searchObj.time.from.slice(-2);
      let date = null;
      let year = null;
      let month = null;
      if (parseInt(day) < parseInt(dayDate)) {
        // 下个月的
        year = moment(this.searchObj.time.to).year();
        month = moment(this.searchObj.time.to).month();
      } else {
        year = moment(this.searchObj.time.from).year();
        month = moment(this.searchObj.time.from).month();
      }
      date = moment({
        years: year,
        months: month,
        date: day
      });
      return date.day() === 0 || date.day() === 6;
    },
    getDate(date) {
      return new Date(date);
    },
    /**
     * @param {String} id  任务id
     * 通过任务id从计算得到的阶段数组中拿到该任务对应的那个数组
     */
    getItemData(id) {
      let res = [];
      this.mainData.forEach(item => {
        if (item && Array.isArray(item)) {
          item.forEach(val => {
            if (val && Array.isArray(val) && val.length > 0) {
              if (val[0].id && val[0].id === id) {
                res = val;
              }
            }
          });
        }
      });
      return res;
    },
    // 计算头部日期数组 start:开始日期 end：结束日期
    getHeaderArr(start, end) {
      let res = [];
      let startNum = Number(start);
      let endNum = Number(end);
      let startMonth = new Date(this.searchObj.time.from).getMonth();
      let endMonth = new Date(this.searchObj.time.to).getMonth();
      if (startNum < endNum && startMonth === endMonth) {
        for (let i = startNum; i <= endNum; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
      } else if (
        endMonth - startMonth === 1 ||
        endMonth - startMonth + 12 === 1
      ) {
        let monthDatNumber = this.getMonthDayNumber(this.searchObj.time.from);
        for (let i = startNum; i <= monthDatNumber; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
        for (let i = 1; i <= endNum; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
      } else if (
        endMonth - startMonth === 2 ||
        endMonth - startMonth + 12 === 2
      ) {
        let monthDatNumber = this.getMonthDayNumber(this.searchObj.time.from);
        let day1 = this.getMonthDayNumber(
          moment(this.searchObj.time.from)
            .add(1, 'month')
            .format('YYYY/MM/DD')
        );
        for (let i = startNum; i <= monthDatNumber; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
        for (let i = 1; i <= day1; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
        for (let i = 1; i <= endNum; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
      } else if (
        endMonth - startMonth === 3 ||
        endMonth - startMonth + 12 === 3
      ) {
        let monthDatNumber = this.getMonthDayNumber(this.searchObj.time.from);
        let day1 = this.getMonthDayNumber(
          moment(this.searchObj.time.from)
            .add(1, 'month')
            .format('YYYY/MM/DD')
        );
        let day2 = this.getMonthDayNumber(
          moment(this.searchObj.time.from)
            .add(2, 'month')
            .format('YYYY/MM/DD')
        );
        for (let i = startNum; i <= monthDatNumber; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
        for (let i = 1; i <= day1; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
        for (let i = 1; i <= day2; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
        for (let i = 1; i <= endNum; i++) {
          if (i < 10) {
            res.push('0' + i);
          } else {
            res.push(i);
          }
        }
      }
      return res;
    },
    // 获取日期的当前月的天数
    getMonthDayNumber(val) {
      let daysNumber = moment(val)
        .endOf('month')
        .date();
      return daysNumber;
    },
    toDesignReview(id) {
      this.$router.push(`/job/list/${id}/design`);
    },
    /**
     * 高级筛选功能逻辑 开始
     */
    async addItemFun() {
      this.customList.push(cunstomQuery());
      await this.$nextTick();
      this.disableItemFun();
    },
    async delItemFun(i) {
      this.customList.splice(i, 1);
      await this.$nextTick();
      this.disableItemFun();
    },
    getCurToggleStateFun(val) {
      if (val) this.showFun();
      if (!val) this.hiddenFun();
    },
    hiddenFun() {
      this.ifShow = false;
      this.showQuery = false;
      this.cloneCustomList = JSON.parse(JSON.stringify(this.customList));
      this.customList = [];
      this.andOrState = '';
    },
    showFun() {
      this.ifShow = true;
      this.customList = JSON.parse(JSON.stringify(this.cloneCustomList));
      this.andOrState = this.cloneAndOrState ? this.cloneAndOrState : 'and';
    },
    disableItemFun() {
      let firstOptBan = [],
        secondOptBan = [],
        thirdOptBan = [];
      this.customList.forEach(item => {
        if (item.firstSelected) firstOptBan.push(item.firstSelected.value);
        if (item.secondSelected) secondOptBan.push(item.secondSelected.value);
        if (item.thirdSelected) thirdOptBan.push(item.thirdSelected.value);
      });
      this.customList.forEach(ctem => {
        this.addDisableFun(ctem.firstOptions, firstOptBan);
        // this.addDisableFun(ctem.secondOptions, secondOptBan);
        // this.addDisableFun(ctem.thirdOptions, thirdOptBan);
      });
    },
    addDisableFun(opts, disableArr) {
      opts.forEach(it => {
        if (disableArr.includes(it.value)) this.$set(it, 'disable', true);
        if (!disableArr.includes(it.value)) this.$set(it, 'disable', false);
      });
    },
    oneToThirdOptions(arg) {
      let stageType = {
        uploadToReturn: dayOptions,
        returnToUpload: dayOptions,
        checkCount: timeOptions,
        positionStatus: kdResultOptions,
        finshFlag: doneResultOptions,
        currentStage: currentOptions
      };
      return stageType[arg];
    },
    // 高级删选 分辨率不同下导致删选条件输入框错位修复
    setHideFun(i) {
      if (i === 1) return '';
      return { visibility: 'hidden' };
    },
    handleSelectedFun(index) {
      if (index && this.customList[index - 1].firstSelected.value) {
        //动态获取第三个下拉选项的options
        this.customList[index - 1].thirdOptions = this.oneToThirdOptions(
          this.customList[index - 1].firstSelected.value
        );
        //动态获取第二个下拉选项的options
        this.customList[index - 1].secondOptions = filterSArray(
          this.customList[index - 1].firstSelected.value
        );
        //改变第一个下拉框自动清空其余两个下拉选项
        this.customList[index - 1].secondSelected = null;
        this.customList[index - 1].thirdSelected = null;
      }
      this.disableItemFun();
    },
    handleToggleFun(arg) {
      this.active = !this.active;
      this.andOrState = arg;
      this.cloneAndOrState = arg;
    },
    /**
     * 高级筛选功能逻辑 结束
     */
    //获取某个月的第一天和最后一天
    getMonthDate(val) {
      let data = val ? val : new Date();
      if (val) {
        this.startTime = moment(data).format('YYYY-MM-DD');
        this.endTime = moment(data)
          .add(1, 'month')
          .format('YYYY-MM-DD');
      } else {
        this.startTime = moment(data)
          .startOf('month')
          .format('YYYY-MM-DD');
        this.endTime = moment(data)
          .endOf('month')
          .format('YYYY-MM-DD');
      }
      this.searchObj.time = {
        from: moment(this.startTime).format('YYYY/MM/DD') || '',
        to: moment(this.endTime).format('YYYY/MM/DD') || ''
      };
    },
    changeStart() {
      this.getMonthDate(this.startTime);
    },
    changeEnd() {
      this.searchObj.time.to = moment(this.endTime).format('YYYY/MM/DD') || '';
    },
    async reciveFun(v) {
      this.selectedColList = v;
    },
    /**
     * 缓存当前类型选择列
     * 监听 visibleColumns 选择变化则缓存到session
     * 跳转链接 初始化调用 cacheColFun 函数
     * 切换类型 调用 cacheColFun 函数
     */
    cacheColFun(n) {
      let store = sessionStorage.getItem(`${n}Col`);
      this.visibleColumns = store
        ? JSON.parse(store)
        : JSON.parse(JSON.stringify(visibleColumns));
    }
  },
  async mounted() {
    // 监听页面刷新 清空session
    window.addEventListener('beforeunload', e => {
      sessionStorage.removeItem('searchParam');
      sessionStorage.removeItem('demandCol');
      sessionStorage.removeItem('taskCol');
    });
    await this.fetchRole({ name: 'UI团队设计师' });
    await this.fetchUser({
      role_id: [this.roles[0].id],
      status: '0'
    });
    this.reviewers = deepClone(this.list);
    this.userList = this.reviewers;
    this.userList.unshift({
      id: '',
      user_name_cn: '全部'
    });
    await this.queryGroupById();
    this.groupList = deepClone(this.childGroupList);
    this.parentGroupList = this.groupList;
    let data = JSON.parse(sessionStorage.getItem('searchParam'));
    if (data && JSON.stringify(data) !== '{}') {
      if (data.startDate && data.endDate) {
        this.startTime = data.startDate;
        this.endTime = data.endDate;
        this.searchObj.time = {
          from: moment(data.startDate).format('YYYY/MM/DD') || '',
          to: moment(data.endDate).format('YYYY/MM/DD') || ''
        };
      } else {
        this.getMonthDate();
      }
      this.reviewer = this.userList.find(item => {
        return item.id === data.reviewer;
      });
      if (data && data.groupParent) this.groupParent = data.groupParent;
      this.groupCheckedArr = data.group || [];
      if (this.groupParent) {
        await this.queryGroupById({
          id: this.groupParent.id
        });
        this.childList = this.childGroupList;
      }
      let mapLength = this.childList.length;
      if (this.groupCheckedArr.length === mapLength) {
        this.allCheck = true;
      } else {
        this.allCheck = false;
      }
      this.type = data.type || 'demand';
      // 高级删选查询条件
      if (data && data.searchList && JSON.stringify(data.searchList) !== '[]') {
        this.cloneCustomList = data.searchList;
        this.getCurToggleStateFun((this.showQuery = true));
      }
    } else {
      this.groupParent = this.parentGroupList
        ? this.childGroupList.filter(item => {
            return item.name == '零售金融组';
          })[0]
        : '';
      if (this.groupParent) {
        await this.queryGroupById({
          id: this.groupParent.id
        });
        this.childList = this.childGroupList;
        this.groupCheckedArr = this.childList.map(item => {
          return item.id;
        });
      }
      this.getMonthDate();
    }
    this.cacheColFun(this.type);
    this.query();
  },
  // 原进原出
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem('searchParam', JSON.stringify(this.searchParam));
    next();
  }
};
</script>
<style lang="stylus" scoped>
.square
  display inline-block
  width 16px
  height 16px
  line-height 16px
  margin-right 4px
  border-radius: 2px;
  border-radius: 2px;
.square-allot
  background: #F9816E;
.square-fixing
  background: #FFB64C;
.square-nopass
  background: #5FACFB;
.square-finish
  background: #5CC49E;
.col-item
  display: inline-block;
  height: 14px;
  flex:1;
.divTask
  flex:1;
  text-align:center;
  padding-top: 20px;
  display:flex;
  overflow: hidden;
.divPass
  text-align:center;
  padding: 20px 0;
  min-width: 8%;
  display: flex;
  flex-direction: column;
  justify-content: center;
.box
  width: 30px;
  height: 15px;
.divBox
  width:0px;
  height: 16px;
  transition: all .5s;
.taskName
  min-width: 18%;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0 3.4% 0 4%;
  max-width: 18%;
  cursor: pointer;
  color: var(--q-color-primary);
.taskNameRqr
  min-width: 18%;
  max-width: 18%;
  padding: 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  color: var(--q-color-primary);
  margin-right: 1%;
.link
  cursor: pointer;
  color: var(--q-color-primary);
.div1
    height: 36px;
    margin-top: 17px;
.btnDiv
  float:right;
.span1
  font-size: 16px;
  padding-left: 12px;
.mr140
  margin-right:140px;
.mt20
  margin-top:20px;
>>>.q-checkbox__bg
  top: 0%;
  left: 0%;
  width: 100%;
  height: 100%;
>>>.q-checkbox__inner
  width:16px;
  height:16px;
  min-width:16px;
.mb2
  margin-right: 16px;
  margin-bottom:-2px;
.div3
  display: flex;    justify-content: space-between;


.div5{
  margin-left:5%;
}
.div6{
margin:0 8% 0 9%;
}
.divRqr{
margin:0 7% 0 9%;
}
.div7{
  display:flex;    padding: 0 20px;
    border-bottom: 10px solid #fff;
}
.divPosition {
  z-index: 999;
  position: relative;
}
.div8{
  background: #bdbdbd82;writing-mode: vertical-lr;padding: 10px; letter-spacing: 5px;width: 4%;    text-align: center;
}
.div9{
  display: flex;
    flex-direction: column;    width: 96%;
    overflow: hidden;
}
.div10{
  width:100%;display:flex;border-bottom: 1px solid #bdbdbd82;
}
.div10Rqr{
  width:100%;display:flex;
}
.div11{
  padding-top: 10px;color: #1976d2;
}
.div12 {
  flex:1;display: flex;
    flex-direction: column; width: 96%;
}
.div13 {
  display:flex;flex:1;width: 72%;
}
.div14{
display:flex;width:100%;
}
.div15{
  padding-top: 10px;color: #1976d2;
}
.div16{
  flex:1;display: flex;
    flex-direction: column;    width: 96%;
}
.div17{
  display:flex;flex:1;    width: 72%;
}
.flexDiv{
  flex: 1;
    display: flex;
    overflow: hidden;
}
.q-field--auto-height >>> .q-field--dense >>> .q-field__native {
  min-height: 30px !important;
}
.btn-height {
  height: 30px !important;
}
.q-field--auto-height .q-field__native {
  min-height: 30px !important;
}
.overflow {
  word-break: break-all;
  -webkit-line-clamp: 1;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  display: -webkit-box !important;
  overflow: hidden;
  line-height: 16px;
  max-height: 16px;
}
.nodata-style{
  display: flex;
  align-items: center;
  align-content: center;
  text-align: center;
}
.selected
    width 70px
    height 36px
    outline none
    transform translateY(8px)
.margin20
  margin: 14px 0px 20px 0;
.mt10
  margin-top: 10px;
.flex-end
  align-items: flex-end;
.left-border
  border-left: 1px solid #DDD
.ml66
  margin-left: 66px
.pl72
  padding-left: 72px
.width280
  width: 280px
  height: 36px
.scale
  transform scale(1.25)
.width144
  width: 144px
  height: 36px
.active,.de-active
  width: 32px
  height: 32px
  line-height: 32px
  text-align: center
  cursor: pointer
.active
  color: #fff
  background: #0663BE
.de-active
  border: 1px solid #ccc
  color: #000
  background: #ffffff
.align-base >>> .q-btn__content
  align-items: flex-start
  transform: translateY(-3px);
.align-base >>> .block
  transform: translateY(-3px);
.absolute-btn
  position: absolute
  left: 0
  top: 50%
  transform: translate(-50%, -50%);
  border-radius 2px
  overflow: hidden
.download-btn >>> .q-btn__content
  align-items: flex-end
.download-btn >>> .block
  transform: translateY(3px)
.textTitle
  font-weight:600
.pstyle{text-indent:25px}
.divWidth
  width:500px;
.mrt16
  margin-top: 16px;
.text-grey-7 >>> .q-tabs__content
  border-bottom 1px solid #ddd
.transformf
  transform: translate(1px, 1px);
</style>
