<template>
  <div class="chart-container bg-gray">
    <el-container class="chart-con">
      <strong class="title">工单信息统计表</strong>
      <el-header class="bg-white">
        <el-form
          class="header"
          :inline="true"
          :model="searchOrder"
          :rules="searchRules"
          ref="searchOrder"
        >
          <el-form-item prop="date">
            <el-date-picker
              v-model="searchOrder.date"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :picker-options="pickerOptions"
            >
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <t-select
              v-model="groupId"
              filterable
              clearable
              placeholder="请选择组名"
              :options="groupList"
              prop="label"
              option-label="fullName"
              option-value="id"
            />
          </el-form-item>
          <el-form-item>
            <t-select
              prop="label"
              :options="testers"
              v-model="tester"
              filterable
              placeholder="请选择测试人员"
              clearable
              option-label="user_name_cn"
              option-value="user_name_en"
            >
              <template v-slot:options="item">
                <span class="option-left">{{ item.user_name_cn }}</span>
                <span class="option-right">{{ item.user_name_en }}</span>
              </template>
            </t-select>
          </el-form-item>

          <el-form-item>
            <el-input
              class="select"
              v-model="orderNameOrNo"
              placeholder="任务名/工单编号"
              maxlength="90"
              clearable
            ></el-input>
          </el-form-item>
          <el-form-item>
            <t-select
              v-model="doneList.stage"
              filterable
              clearable
              placeholder="请选择工单状态"
              :options="doneList"
              prop="label"
              option-label="stageCnName"
              option-value="stage"
            />
          </el-form-item>
          <!-- <el-form-item>
            <el-select v-model="orderType" placeholder="工单类型">
              <el-option
                v-for="item in orderTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item>
            <el-switch v-model="childGroupFlag" active-text="包含子组">
            </el-switch>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="search('searchOrder')"
              class="queryBtn"
              >查询</el-button
            >
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="exportOrderDimensionList('searchOrder')"
              >导出</el-button
            >
          </el-form-item>
          <el-form-item>
            <el-select
              v-model="selOpts"
              placeholder="请选择"
              collapse-tags
              multiple
              class="margin-left"
            >
              <el-option
                v-for="(item, index) in headers"
                :label="item"
                :value="index"
                :key="item"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-header>
      <el-main style="padding:10px" class="bg-white">
        <el-table
          v-loading="loading"
          :data="
            workChartTable.slice(
              (currentPage - 1) * pagesize,
              currentPage * pagesize
            )
          "
          style="color:black;width: 100%"
          :header-cell-style="{ color: '#545c64' }"
          @cell-click="openTaskList"
          :cell-style="cellStyle"
        >
          <el-table-column v-if="colShow[0]" prop="workNo" label="工单编号" />
          <el-table-column
            v-if="colShow[1]"
            prop="mainTaskName"
            label="主任务名"
          >
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                placement="top-start"
                class="tooltip"
                :content="scope.row.mainTaskName"
              >
                <div>{{ scope.row.mainTaskName }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column
            v-if="colShow[2]"
            prop="groupName"
            label="小组名称"
          />
          <el-table-column prop="orderType" label="工单类型">
            <template slot-scope="scope">
              {{ showOrderType(scope.row.orderType) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="workStage"
            label="工单状态"
            :formatter="formatStatus"
            v-if="colShow[3]"
          />
          <el-table-column
            v-if="colShow[4]"
            prop="mainTaskName"
            label="测试人员"
          >
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                placement="top-start"
                class="tooltip"
                :content="scope.row.testers"
              >
                <div>{{ scope.row.testers }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="colShow[5]" prop="percentage" label="测试进度">
            <template slot-scope="scope">
              <div>
                {{ scope.row.percentage + '%' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
            v-if="colShow[6]"
            prop="caseCount"
            label="案例总数"
          />

          <el-table-column
            v-if="colShow[7]"
            prop="caseExecute"
            label="案例执行总数"
          />
          <el-table-column
            v-if="colShow[8]"
            prop="caseNoExecute"
            label="未执行案例总数"
          />
          <el-table-column
            v-if="colShow[9]"
            prop="casePass"
            label="案例通过数"
          />
          <el-table-column
            v-if="colShow[10]"
            prop="caseFailure"
            label="案例失败数"
          />
          <el-table-column
            v-if="colShow[11]"
            prop="caseBlock"
            label="案例阻塞数"
          />
          <el-table-column
            v-if="colShow[12]"
            prop="caseMantis"
            label="有效缺陷总数"
          />
          <el-table-column
            v-if="colShow[13]"
            prop="developer"
            label="缺陷开发责任人"
          />
          <el-table-column
            v-if="colShow[14]"
            prop="rqrNum"
            label="业务需求问题"
          />
          <el-table-column
            v-if="colShow[15]"
            prop="rqrRuleNum"
            label="需规问题"
          />
          <el-table-column
            v-if="colShow[16]"
            prop="funcLackNum"
            label="功能实现不完整"
          />
          <el-table-column
            v-if="colShow[17]"
            prop="funcErrNum"
            label="功能实现错误"
          />
          <el-table-column
            v-if="colShow[18]"
            prop="historyNum"
            label="历史遗留问题"
          />
          <el-table-column
            v-if="colShow[19]"
            prop="optimizeNum"
            label="优化建议"
          />
          <el-table-column v-if="colShow[20]" prop="backNum" label="后台问题" />
          <el-table-column
            v-if="colShow[21]"
            prop="packageNum"
            label="打包问题"
          />
        </el-table>
        <div class="pagination">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 50, 100, 200, 500]"
            :page-size="pagesize"
            layout="sizes, prev, pager, next, jumper"
            :total="workChartTable.length"
          ></el-pagination>
        </div>
      </el-main>

      <el-main class="chart-main">
        <div id="divFlex">
          <el-form
            id="form"
            :inline="true"
            ref="searchRulesChart"
            :model="searchRulesChart"
            :rules="rule"
          >
            <el-form-item prop="type">
              <t-select
                v-model="searchRulesChart.type"
                filterable
                placeholder="请选择"
                :options="orderOptions"
                prop="label"
                option-label="label"
                option-value="value"
                @input="typeChange"
              />
            </el-form-item>
            <el-form-item prop="date">
              <el-date-picker
                v-if="searchRulesChart.type !== 'month'"
                v-model="searchRulesChart.date"
                value-format="yyyy-MM-dd"
                :format="valFormat"
                :type="dateType"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :picker-options="pickerOptionsCharts"
              ></el-date-picker>
            </el-form-item>
            <el-form-item prop="dateMonth">
              <el-date-picker
                v-if="searchRulesChart.type == 'month'"
                v-model="searchRulesChart.dateMonth"
                value-format="yyyy-MM-dd"
                format="yyyy-MM"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
                :picker-options="pickerOptionsChartsM"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <t-select
                v-model="group_id"
                filterable
                clearable
                placeholder="请选择组名"
                :options="groupList"
                prop="label"
                option-label="fullName"
                option-value="id"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="dateChange" class="margin-left"
                >查询</el-button
              >
            </el-form-item>
          </el-form>
        </div>
        <el-row :gutter="10">
          <el-col :span="15" class="bar-main">
            <div
              id="discountChart"
              class="chart-frame bg-white"
              v-loading="barChartLoading"
            ></div>
          </el-col>
          <el-col :span="9" class="bar-main">
            <div
              id="myPie"
              class="chart-frame bg-white"
              v-loading="loading"
            ></div>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
    <!-- 任务列表窗口 -->
    <TaskListDialog
      :openDialog="taskListDialog"
      :dialogModel="taskListDialogDetail"
      @taskListClose="closeDialogTaskList"
      :isShowOpt="false"
    />
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { validate, exportExcel, getUserListByRole } from '@/common/utlis';
import { rules, searchOrder, orderOptions } from './model';
import { orderTypeList } from '../WorkOrder/model';
import TaskListDialog from '@/pages/WorkOrder/Compoments/TaskListDialog.vue';

export default {
  name: 'WorkChart',
  components: {
    TaskListDialog
  },
  data() {
    return {
      taskListDialog: false,
      callBackworkNo: '',
      taskListDialogDetail: [],
      orderTypeList: orderTypeList,
      orderType: 'function',
      rule1: {
        type: [{ required: true, message: '请选择时间维度', trigger: 'blur' }],
        date: [{ required: true, message: '请输入时间范围', trigger: 'blur' }]
      },
      rule2: {
        type: [{ required: true, message: '请选择时间维度', trigger: 'blur' }],
        dateMonth: [
          { required: true, message: '请输入时间范围', trigger: 'blur' }
        ]
      },
      orderOptions: orderOptions,
      doneList: [
        { stage: '1', stageCnName: '开发中' },
        { stage: '2', stageCnName: 'SIT' },
        { stage: '3', stageCnName: 'UAT' },
        { stage: '4', stageCnName: '已投产' },
        { stage: '6', stageCnName: 'UAT(含风险)' },
        { stage: '9', stageCnName: '分包测试(内测完成)' },
        { stage: '10', stageCnName: '分包测试(含风险)' },
        { stage: '12', stageCnName: '安全测试(内测完成)' },
        { stage: '13', stageCnName: '安全测试(含风险)' },
        { stage: '14', stageCnName: '安全测试(不涉及)' }
      ],
      searchOrder: searchOrder(),
      loading: false,
      barChartLoading: false,
      disCountchartTitle: '',
      userEnName: '' + sessionStorage.getItem('user_en_name'),
      // date: '',
      startTime: '',
      endTime: '',
      groupId: '',
      group_id: '',
      orderNameOrNo: '',
      start: '',
      currentPage: 1,
      pagesize: 10,
      groupNames: ['1', '2', '3', '4'],
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      searchRulesChart: {
        type: 'day',
        date: [],
        dateMonth: []
      },
      tester: '',
      selOpts: [0, 1, 2, 3, 4, 5, 6, 12],
      colShow: [],
      headers: [
        '工单编号',
        '主任务名称',
        '小组名称',
        '工单状态',
        '测试人员',
        '测试进度',
        '案例总数',
        '案例执行总数',
        '未执行案例总数',
        '案例通过数',
        '案例失败数',
        '案例阻塞数',
        '有效缺陷总数',
        '缺陷开发责任人',
        '业务需求问题',
        '需规问题',
        '功能实现不完整',
        '功能实现错误',
        '历史遗留问题',
        '优化建议',
        '后台问题',
        '打包问题'
      ],
      childGroupFlag: false,
      testers: []
    };
  },
  methods: {
    ...mapActions('chartsForm', [
      'selWork',
      'workLineData',
      'workPieData',
      'queryGroupName',
      'exportOrderDimension',
      'queryDiscountChart'
    ]),
    ...mapActions('adminForm', ['queryAllUserName']),
    ...mapActions('workOrderForm', ['queryFdevTaskByWorkNo']),
    async openTaskList(item, column) {
      // 只有点击  工单编号才弹框
      if (column.property != 'workNo') return;
      this.taskListDialog = true;
      await this.queryFdevTaskByWorkNo({ workNo: item.workNo });
      this.callBackworkNo = item.workOrderNo;
      this.taskListDialogDetail = this.fdevTaskByWorkNo;
    },
    closeDialogTaskList() {
      this.taskListDialog = false;
      this.dialogModel = [];
    },
    cellStyle({ column }) {
      if (column.property == 'workNo') {
        return {
          color: '#409eff',
          cursor: 'pointer'
        };
      }
    },
    rules(model, notNecessary = []) {
      const keys = Object.keys(model);
      const rulesObj = {};

      keys.forEach(key => {
        if (rules[key] && !notNecessary.includes[key]) {
          rulesObj[key] = rules[key];
        }
      });

      return rulesObj;
    },
    showOrderType(val) {
      if (val == 'function') {
        return '功能测试';
      } else if (val == 'security') {
        return '安全测试';
      } else {
        return '-';
      }
    },
    //工单状态
    formatStatus(row, column) {
      if (row.workStage === '0') {
        return '待分配';
      } else if (row.workStage === '1') {
        return '开发中';
      } else if (row.workStage === '2') {
        return 'SIT';
      } else if (row.workStage === '3') {
        return 'UAT';
      } else if (row.workStage === '4') {
        return '已投产';
      } else if (row.workStage === '6') {
        return 'UAT(含风险)';
      } else if (row.workStage === '8') {
        return '撤销';
      } else if (row.workStage === '9') {
        return '分包测试（内测完成）';
      } else if (row.workStage === '10') {
        return '分包测试（含风险）';
      } else if (row.workStage === '11') {
        return '已废弃';
      } else if (row.workStage === '12') {
        return '安全测试（含风险）';
      } else if (row.workStage === '13') {
        return '安全测试（内测完成）';
      } else {
        //return "接口"1;
      }
    },
    handleSizeChange: function(size) {
      this.pagesize = size;
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    async search(searchOrder) {
      this.currentPage = 1;
      await validate(this.$refs[searchOrder]);
      this.loading = true;
      const date = this.searchOrder.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      try {
        await this.selWork({
          startTime: startDate,
          endTime: endDate,
          groupId: this.groupId,
          orderNameOrNo: this.orderNameOrNo,
          stage: this.doneList.stage,
          tester: this.tester,
          isParent: this.childGroupFlag,
          orderType: this.orderType
        });
        await this.drawPie();
      } catch (err) {
        this.loading = false;
      }

      this.loading = false;
    },
    // 初始化下拉选项
    async groupNameData() {
      await this.queryGroupName({});
    },
    //全部导出
    async exportOrderDimensionList(searchOrder) {
      await validate(this.$refs[searchOrder]);
      const date = this.searchOrder.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      await this.exportOrderDimension({
        startTime: startDate,
        endTime: endDate,
        groupId: this.groupId,
        orderNameOrNo: this.orderNameOrNo,
        stage: this.doneList.stage,
        tester: this.tester,
        isParent: this.childGroupFlag,
        orderType: this.orderType
      });
      exportExcel(this.exportOrder);
      this.$message({
        type: 'success',
        message: '全部导出成功!'
      });
    },
    typeChange() {
      this.timeChartOptionRange = null;
      this.searchRulesChart.date = [];
      this.searchRulesChart.dateMonth = [];
      this.$refs['searchRulesChart'].clearValidate();
    },
    async dateChange() {
      this.barChartLoading = true;
      await validate(this.$refs['searchRulesChart']);
      let param = {};
      let num = 0;
      let time = 0;
      let day = 3600 * 1000 * 24;
      let week = 3600 * 1000 * 24 * 7;
      let month = day * 30;
      if (this.searchRulesChart.type === 'day') {
        if (
          Array.isArray(this.searchRulesChart.date) &&
          this.searchRulesChart.date.length > 1
        ) {
          time =
            new Date(this.searchRulesChart.date[1]).getTime() -
            new Date(this.searchRulesChart.date[0]).getTime();
          num = Number(time / day) + 1;
        }
        param = {
          type: '0',
          startDate: this.searchRulesChart.date[0],
          times: num
        };
      } else if (this.searchRulesChart.type === 'week') {
        if (
          Array.isArray(this.searchRulesChart.date) &&
          this.searchRulesChart.date.length > 1
        ) {
          time =
            new Date(this.searchRulesChart.date[1]).getTime() -
            new Date(this.searchRulesChart.date[0]).getTime() +
            day;
          num = Number(time / week);
        }
        param = {
          type: '1',
          startDate: this.searchRulesChart.date[0],
          times: num
        };
      } else {
        if (
          Array.isArray(this.searchRulesChart.dateMonth) &&
          this.searchRulesChart.dateMonth.length > 1
        ) {
          time =
            new Date(this.searchRulesChart.dateMonth[1]).getTime() -
            new Date(this.searchRulesChart.dateMonth[0]).getTime();
          num = Math.floor(Number(time / month)) + 1;
          param = {
            type: '2',
            startDate: this.searchRulesChart.dateMonth[0],
            times: num
          };
        }
      }
      param.groupId = this.group_id || '';
      await this.queryDiscountChart(param);
      this.initDiscountChart();
      this.barChartLoading = false;
    },
    async initDiscountChart() {
      this.barChartLoading = true;
      let discountChartChart = this.$echarts.init(
        document.getElementById('discountChart')
      );
      if (
        this.searchRulesChart.date.length == 0 &&
        this.searchRulesChart.dateMonth == 0
      ) {
        this.disCountchartTitle = ' ';
      } else if (this.searchRulesChart.date.length > 0) {
        this.disCountchartTitle =
          this.searchRulesChart.date[0] +
          '至' +
          this.searchRulesChart.date[1] +
          '期';
      } else if (this.searchRulesChart.dateMonth.length > 0) {
        this.disCountchartTitle =
          this.searchRulesChart.dateMonth[0].substr(0, 7) +
          '至' +
          this.searchRulesChart.dateMonth[1].substr(0, 7);
      }
      discountChartChart.setOption({
        title: {
          text: '工单趋势' + this.disCountchartTitle,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['新建工单数量', '进行中的工单数量'],
          orient: 'vertical',
          top: '10%',
          right: '0px'
        },
        grid: {
          left: '15%',
          right: '22%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.disCountChartData.stage,
          axisLabel: {
            interval: 0,
            rotate: 41,
            showMinLabel: true,
            showMaxLabel: true
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '新建工单数量',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#999',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'diamond',
            symbolSize: 12,
            show: false,
            data: this.disCountChartData.newWorkOrder
          },
          {
            name: '进行中的工单数量',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#00008B',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'rect',
            symbolSize: 12,
            data: this.disCountChartData.processWorkOrder
          }
        ]
      });
      this.barChartLoading = false;
    },
    async drawPie() {
      this.loading = true;
      let myPie = this.$echarts.init(document.getElementById('myPie'));
      const date = this.searchOrder.date;
      // const groupLeader = this.groupLeader;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      await this.workPieData({
        startDate: startDate,
        endDate: endDate,
        // groupLeader:groupLeader
        groupId: this.groupId,
        orderNameOrNo: this.orderNameOrNo,
        isParent: this.childGroupFlag
      });
      let res = this.workPieDataChart;
      let arr = res;
      let leg = [];
      let workStage = [
        '分包测试（含风险）',
        '分包测试（内测完成）',
        'UAT(含风险)',
        '已投产',
        'UAT',
        'SIT',
        '开发中',
        '待分配'
      ];
      let alldata = [];
      let i = '';
      let len = '';
      for (i = 0, len = arr.length; i < len; i++) {
        leg.push(workStage[i]);
        let temp = {
          name: workStage[i],
          value: arr[i].stageCount
        };
        alldata.push(temp);
      }
      myPie.setOption({
        title: {
          text: '工单阶段分布图',
          x: 'center'
        },
        tooltip: {
          trrigger: 'item',
          formatter: '{a}<br/>{b}:{c}({d}%)'
        },
        legend: {
          orient: 'vertical',
          x: 'right',
          data: leg
        },
        series: [
          {
            name: '总数',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              normal: {
                show: false,
                position: 'center'
              },
              emphasis: {
                show: true,
                textStyle: {
                  fontSize: '30',
                  fontWeight: 'bold'
                }
              }
            },
            lableLine: {
              normal: {
                show: false
              }
            },
            data: alldata
          }
        ]
      });
      this.loading = false;
    }
  },
  async mounted() {
    await this.groupNameData();
    await this.queryAllUserName();
    this.testers = await getUserListByRole(['测试人员']);
  },
  watch: {
    selOpts(val) {
      this.colShow = new Array(22).fill(false);
      val.forEach((el, index) => {
        this.$set(this.colShow, el, true);
      });
    }
  },
  computed: {
    ...mapState('chartsForm', [
      'workChartTable',
      'workLineDataChart',
      'workPieDataChart',
      'groupList',
      'exportOrder',
      'disCountChartData'
    ]),
    ...mapState('workOrderForm', ['fdevTaskByWorkNo']),
    rule() {
      if (this.searchRulesChart.type !== 'month') {
        return this.rule1;
      } else {
        return this.rule2;
      }
    },
    pickerOptionsChartsM() {
      return {
        onPick: time => {
          if (time.minDate && !time.maxDate) {
            this.timeChartOptionRange = time.minDate;
          }
          if (time.maxDate) {
            this.timeChartOptionRange = null;
          }
        },
        disabledDate: time => {
          let timeChartOptionRange = this.timeChartOptionRange;
          if (timeChartOptionRange) {
            let threeMonths = 60 * 60 * 1000 * 24 * 360;
            return (
              time.getTime() > timeChartOptionRange.getTime() + threeMonths ||
              time.getTime() < timeChartOptionRange.getTime() - threeMonths
            );
          }
        }
      };
    },
    pickerOptionsCharts() {
      return {
        onPick: time => {
          if (time.minDate && !time.maxDate) {
            this.timeChartOptionRange = time.minDate;
          }
          if (time.maxDate) {
            this.timeChartOptionRange = null;
          }
        },
        disabledDate: time => {
          let timeChartOptionRange = this.timeChartOptionRange;
          let day = 3600 * 1000 * 24 * 12;
          let oneDay = 3600 * 1000 * 24;
          let week = 3600 * 1000 * 24 * 7;
          if (timeChartOptionRange) {
            switch (this.searchRulesChart.type) {
              case 'day': {
                return (
                  time.getTime() >= timeChartOptionRange.getTime() + day ||
                  time.getTime() < timeChartOptionRange.getTime() ||
                  time.getTime() > Date.now()
                );
                break;
              }
              case 'week': {
                let item = timeChartOptionRange.getTime() - oneDay;
                let res = [];
                for (let i = 0; i < 12; i++) {
                  item = item + week;
                  res.push(item);
                }
                return (
                  res.indexOf(time.getTime()) === -1 ||
                  time.getTime() > Date.now()
                );
                break;
              }
              default:
                break;
            }
          }
          return time.getTime() > Date.now();
        }
      };
    },
    dateType() {
      let item = '';
      if (this.searchRulesChart.type === 'month') {
        item = 'monthrange';
      } else {
        item = 'daterange';
      }
      return item;
    },
    valFormat() {
      let item = '';
      if (this.searchRulesChart.type === 'month') {
        item = 'yyyy-MM';
      } else {
        item = 'yyyy-MM-dd';
      }
      return item;
    },
    searchRules() {
      return this.rules(this.searchOrder);
    }
  },
  beforeMount() {
    this.colShow = new Array(22).fill(false);
    [0, 1, 2, 3, 4, 5, 6, 12].forEach(item => {
      this.colShow[item] = true;
    });
  }
};
</script>
<style scoped>
body {
  margin: 0;
}
.title {
  font-size: 20px;
  margin-left: 20px;
  text-align: left;
}
.chart-container {
  width: 100%;
  margin: -20px auto;
  text-align: left;
  padding: 20px 0;
}
.chart-con {
  width: 86%;
  margin: 0 auto;
}
.chart-frame {
  padding-top: 10px;
  width: 100%px;
  height: 400px;
}
.queryBtn {
  margin-left: 15px;
}
.bar-main {
  padding: 0px;
}
.chart-main {
  padding: 0px;
  margin-top: 10px;
  overflow: hidden;
}
.pagination {
  margin-top: 20px;
  padding: 20px 0;
  text-align: right;
}
.header {
  margin-top: 15px;
  margin-left: 20px;
  display: flex;
  flex-wrap: wrap;
}
.select {
  width: 200px;
}
#divFlex {
  display: flex;
}

#divOne {
  margin-right: 15px;
}
#form {
  margin-bottom: 20px;
  display: flex;
}
#discountChart {
  height: 50vh;
  width: 100%;
}
</style>
<style>
.tooltip {
  width: 100% !important;
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
