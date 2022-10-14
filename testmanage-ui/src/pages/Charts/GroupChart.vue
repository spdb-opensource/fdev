<template>
  <div class="chart-container ">
    <el-container class="chart-con ">
      <strong class="title">小组工作量统计表</strong>
      <el-header class="headerD">
        <el-form
          :inline="true"
          :model="searchOrder"
          :rules="searchRules"
          class="header"
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
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-select
              v-model="groupIdList"
              placeholder="请选择组名"
              :filter-method="filterMethod"
              collapse-tags
              multiple
              clearable
              filterable
            >
              <el-option
                v-for="item in groupAllOption"
                :label="
                  item.fullName.length > 5
                    ? item.fullName.substring(0, 5) + '...'
                    : item.fullName
                "
                :value="item.id"
                :key="item.id"
              >
                <span> {{ item.fullName }} </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-switch v-model="childGroupFlag" active-text="包含子组">
            </el-switch>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="queryDailyReport('searchOrder')"
              class="margin-left"
              >查询</el-button
            >
            <el-button type="primary" @click="download" class="margin-left"
              >导出</el-button
            >
            <el-button
              type="primary"
              class="margin-left"
              v-show="!showDailyReport"
              @click="returnDailyReport"
              >返回日报总表</el-button
            >
          </el-form-item>
          <el-form-item class="item1">
            <el-select
              v-model="selOpts"
              placeholder="请选择"
              collapse-tags
              multiple
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
      <el-main style="padding:10px">
        <!-- 日报总表 -->
        <div class="all-list" v-show="showDailyReport">
          <el-table
            v-loading="loading"
            :data="
              dailyReport.slice(
                (currentPage - 1) * pagesize,
                currentPage * pagesize
              )
            "
            style="color:black"
            :header-cell-style="{ color: '#545c64' }"
          >
            <el-table-column
              prop="groupName"
              align="center"
              label="组名称"
              width="150"
              sortable
              v-if="colShow[0]"
            >
              <template slot-scope="scope">
                <el-link
                  type="primary"
                  :underline="false"
                  @click="
                    queryDailyGroupReport(
                      scope.row.groupId,
                      scope.row.groupName
                    )
                  "
                  >{{ scope.row.groupName }}</el-link
                >
              </template>
            </el-table-column>
            <el-table-column
              sortable
              v-if="colShow[1]"
              prop="countUser"
              label="总人数"
            >
              <template slot-scope="scope">
                {{ scope.row.countUser ? scope.row.countUser : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              sortable
              v-if="colShow[2]"
              prop="sumOrder"
              label="工单总数"
            >
              <template slot-scope="scope">
                {{ scope.row.sumOrder ? scope.row.sumOrder : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[3]"
              prop="sumOrderAvg"
              label="人均工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.sumOrderAvg ? scope.row.sumOrderAvg : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[4]"
              prop="devOrder"
              label="开发中工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.devOrder ? scope.row.devOrder : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[5]"
              prop="alreadyOrder"
              label="当前完成工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.alreadyOrder ? scope.row.alreadyOrder : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[6]"
              prop="createOrder"
              label="新建工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.createOrder ? scope.row.createOrder : 0 }}
              </template>
            </el-table-column>
            <!--  -->
            <el-table-column
              v-if="colShow[7]"
              prop="underwayOrder"
              label="进行中工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.underwayOrder }}
              </template>
            </el-table-column>
            <el-table-column
              sortable
              v-if="colShow[8]"
              prop="underwayOrderAvg"
              label="人均进行中工单数"
            >
              <template slot-scope="scope">
                {{
                  scope.row.underwayOrderAvg ? scope.row.underwayOrderAvg : 0
                }}
              </template>
            </el-table-column>
            <el-table-column
              sortable
              v-if="colShow[9]"
              prop="dayOrderDone"
              label="当前日期内测完成工单数"
            >
              <template slot-scope="scope">
                {{ scope.row.dayOrderDone ? scope.row.dayOrderDone : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[10]"
              prop="dayOrderSubmit"
              label="当前日期提交内测工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.dayOrderSubmit }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[11]"
              prop="noPlanOrder"
              label="组内待启动工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.noPlanOrder }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[12]"
              prop="noTesterOrder"
              label="组内待分配工单数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.noTesterOrder }}
              </template>
            </el-table-column>

            <el-table-column
              v-if="colShow[13]"
              prop="sumCase"
              label="案例总数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.sumCase }}
              </template>
            </el-table-column>
            <!--  -->

            <el-table-column
              v-if="colShow[14]"
              prop="sumExe"
              label="总执行数"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.sumExe ? scope.row.sumExe : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[15]"
              prop="dayCreate"
              label="编写案例数"
              sortable
              width="120"
            >
              <template slot-scope="scope">
                {{ scope.row.dayCreate ? scope.row.dayCreate : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              v-if="colShow[16]"
              prop="dayExe"
              label="案例执行数"
              sortable
              width="120"
            >
              <template slot-scope="scope">
                {{ scope.row.dayExe ? scope.row.dayExe : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="dayCreateAvg"
              label="人均编写案例数"
              v-if="colShow[17]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.dayCreateAvg ? scope.row.dayCreateAvg : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="dayExeAvg"
              label="人均案例执行数"
              v-if="colShow[18]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.dayExeAvg ? scope.row.dayExeAvg : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="unExeCase"
              label="待测案例总数"
              v-if="colShow[19]"
              sortable
              width="130"
            >
              <template slot-scope="scope">
                {{ scope.row.unExeCase ? scope.row.unExeCase : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="issueNum"
              label="缺陷总数"
              v-if="colShow[20]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.issueNum ? scope.row.issueNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="issueRate"
              label="缺陷率"
              v-if="colShow[21]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.issueRate ? scope.row.issueRate + '%' : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="effectiveIssue"
              label="有效新增缺陷数"
              v-if="colShow[22]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.effectiveIssue ? scope.row.effectiveIssue : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="ineffectiveIssue"
              label="无效新增缺陷数"
              v-if="colShow[23]"
              sortable
            >
              <template slot-scope="scope">
                {{
                  scope.row.ineffectiveIssue ? scope.row.ineffectiveIssue : 0
                }}
              </template>
            </el-table-column>
            <el-table-column
              prop="developMantis"
              label="开发相关缺陷数"
              v-if="colShow[24]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.developMantis ? scope.row.developMantis : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="developMantisAvg"
              sortable
              label="平均开发缺陷数"
              v-if="colShow[25]"
            >
              <template slot-scope="scope">
                {{
                  scope.row.developMantisAvg ? scope.row.developMantisAvg : 0
                }}
              </template>
            </el-table-column>
            <el-table-column
              prop="solveIssue"
              label="已修复缺陷"
              v-if="colShow[26]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.solveIssue ? scope.row.solveIssue : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="unSolveIssue"
              label="未修复缺陷"
              v-if="colShow[27]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.unSolveIssue ? scope.row.unSolveIssue : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="rqrNum"
              label="业务需求问题"
              v-if="colShow[28]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.rqrNum ? scope.row.rqrNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="rqrRuleNum"
              label="需规问题数"
              v-if="colShow[29]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.rqrRuleNum ? scope.row.rqrRuleNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="funcLackNum"
              label="功能实现不完整数"
              v-if="colShow[30]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.funcLackNum ? scope.row.funcLackNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="funcErrNum"
              label="功能实现错误数"
              v-if="colShow[31]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.funcErrNum ? scope.row.funcErrNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="historyNum"
              label="历史遗留问题数"
              v-if="colShow[32]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.historyNum ? scope.row.historyNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="optimizeNum"
              label="优化建议数"
              v-if="colShow[33]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.optimizeNum ? scope.row.optimizeNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="backNum"
              label="后台问题数"
              v-if="colShow[34]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.backNum ? scope.row.backNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="packageNum"
              label="打包问题数"
              v-if="colShow[35]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.packageNum ? scope.row.packageNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="dataNum"
              label="数据问题数"
              v-if="colShow[36]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.dataNum ? scope.row.dataNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="envNum"
              label="环境问题数"
              v-if="colShow[37]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.envNum ? scope.row.envNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="otherNum"
              label="其他原因数"
              v-if="colShow[38]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.otherNum ? scope.row.otherNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="sumProIssue"
              label="生产问题总数"
              v-if="colShow[39]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.sumProIssue ? scope.row.sumProIssue : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="develpoProIssue"
              label="开发相关生产问题数"
              v-if="colShow[40]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.develpoProIssue ? scope.row.develpoProIssue : 0 }}
              </template>
            </el-table-column>
            <el-table-column
              prop="sitProIssue"
              label="内测相关生产问题数"
              v-if="colShow[41]"
              sortable
            >
              <template slot-scope="scope">
                {{ scope.row.sitProIssue ? scope.row.sitProIssue : 0 }}
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-sizes="[5, 10, 50, 100, 200, 500]"
              :page-size="pagesize"
              layout="sizes, prev, pager, next, jumper"
              :total="dailyReport.length"
            ></el-pagination>
          </div>
        </div>
        <!-- 日报组表 -->
        <div class="group-list" v-show="!showDailyReport">
          <el-table
            v-loading="loading"
            :data="
              dailyGroupReport.slice(
                (groupCurrentPage - 1) * groupPagesize,
                groupCurrentPage * groupPagesize
              )
            "
            style="color:black"
            :header-cell-style="{ color: '#545c64' }"
          >
            <el-table-column prop="groupName" label="组名称" fixed="left">
              <template slot-scope="scope">
                {{ scope.row.groupName }}
              </template>
            </el-table-column>
            <el-table-column prop="mainTaskName" width="200" label="主任务名称">
              <template slot-scope="scope">
                {{ scope.row.mainTaskName }}
              </template>
            </el-table-column>
            <el-table-column prop="workStage" label="工单状态">
              <template slot-scope="scope">
                {{ scope.row.workStage ? scope.row.workStage : '' }}
              </template>
            </el-table-column>
            <el-table-column prop="testers" label="测试人员">
              <template slot-scope="scope">
                {{ scope.row.testers ? scope.row.testers : '' }}
              </template>
            </el-table-column>
            <el-table-column prop="developer" label="开发人员">
              <template slot-scope="scope">
                {{ scope.row.developer ? scope.row.developer : '' }}
              </template>
            </el-table-column>
            <el-table-column prop="sumCase" label="案例总数">
              <template slot-scope="scope">
                {{ scope.row.sumCase ? scope.row.sumCase : 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="dayCase" label="当日编写案例数">
              <template slot-scope="scope">
                {{ scope.row.dayCase ? scope.row.dayCase : 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="sumExe" label="执行案例总数">
              <template slot-scope="scope">
                {{ scope.row.sumExe ? scope.row.sumExe : 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="dayExe" label="当日执行案例数">
              <template slot-scope="scope">
                {{ scope.row.dayExe ? scope.row.dayExe : 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="issueNum" label="缺陷总数">
              <template slot-scope="scope">
                {{ scope.row.issueNum ? scope.row.issueNum : 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="solveIssue" label="已修复缺陷">
              <template slot-scope="scope">
                {{ scope.row.solveIssue ? scope.row.solveIssue : 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="unSolveIssue" label="未修复缺陷">
              <template slot-scope="scope">
                {{ scope.row.unSolveIssue ? scope.row.unSolveIssue : 0 }}
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              @size-change="handleGroupSizeChange"
              @current-change="handleGroupCurrentChange"
              :current-page="groupCurrentPage"
              :page-sizes="[5, 10, 50, 100, 200, 500]"
              :page-size="groupPagesize"
              layout="sizes, prev, pager, next, jumper"
              :total="dailyGroupReport.length"
            ></el-pagination>
          </div>
        </div>
        <div
          id="idgroupChart"
          v-loading="loading"
          class="chart-frame-test bg-white"
        ></div>
      </el-main>
    </el-container>
  </div>
</template>
<script>
import { mapActions, mapState } from 'vuex';
import { orderOptions } from './model';
import { exportExcel } from '@/common/utlis';

export default {
  name: 'DailyChart',
  data() {
    return {
      groupAllOption: [],
      showDailyReport: true,
      searchOrder: {
        date: ''
      },
      currentPage: 1,
      pagesize: 10,
      groupCurrentPage: 1,
      groupPagesize: 10,
      monthRange: new Date(),
      pickerOptions: {
        onPick: time => {
          if (time.minDate && !time.maxDate) {
            this.timeOptionRange = time.minDate;
          }
          if (time.maxDate) {
            this.timeOptionRange = null;
          }
        },
        disabledDate: time => {
          let timeOptionRange = this.timeOptionRange;
          let seven = 3600 * 1000 * 24 * 91;
          if (timeOptionRange) {
            return (
              time.getTime() > timeOptionRange.getTime() + seven ||
              time.getTime() < timeOptionRange.getTime() - seven ||
              time.getTime() > Date.now()
            );
          }
          return time.getTime() > Date.now();
        }
      },
      pickerOptionsMonthCharts: {
        onPick: time => {
          if (time.minDate && !time.maxDate) {
            this.monthRange = time.minDate;
          }
          if (time.maxDate) {
            this.monthRange = null;
          }
        },
        disabledDate: time => {
          if (this.monthRange) {
            return time.getTime() > Date.now();
          }
          return time.getTime() > Date.now();
        }
      },
      searchRules: {
        date: [{ required: true, message: '请输入时间范围', trigger: 'blur' }]
      },
      groupId: '',
      groupName: '',
      loading: false,
      orderOptions: orderOptions,
      type: 'day',
      devQualityList: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      date: '',
      dateMonth: '',
      dataChart1: {},
      dataChart2: {},
      num: 0,
      showItem: false,
      showLoading: false,
      colShow: [],
      headers: [
        '组名称',
        '总人数',
        '工单总数',
        '人均工单数',
        '开发中工单数',
        '当前完成工单数',
        '新建工单',
        '进行中工单数',
        '人均进行中工单数',
        '当前日期内测完成工单',
        '当前日期提交内测工单',
        '组内待启动工单',
        '组内待分配工单',
        '案例总数',
        '总执行数',
        '编写案例数',
        '案例执行数',
        '人均编写案例数',
        '人均案例执行数',
        '待测案例总数',
        '缺陷总数',
        '缺陷率',
        '有效新增缺陷数',
        '无效新增缺陷数',
        '开发相关缺陷',
        '平均开发缺陷',
        '已修复缺陷',
        '未修复缺陷',
        '业务需求问题数',
        '需规问题数',
        '功能实现不完整数',
        '功能实现错误数',
        '历史遗留问题数',
        '优化建议数',
        '后台问题数',
        '打包问题数',
        '数据问题数',
        '环境问题数',
        '其他原因数',
        '生产问题总数',
        '开发相关生产问题',
        '内测相关生产问题'
      ],
      selOpts: [0, 1, 2, 13, 14, 15, 16, 19, 20],
      groupIdList: [],
      childGroupFlag: false
    };
  },
  computed: {
    ...mapState('reportForms', [
      'dailyReport',
      'dailyGroupReport',
      'searchOrderDate'
    ]),
    ...mapState('chartsForm', ['groupList', 'groupData']),
    allGroup() {
      let list = [];
      this.groupList.forEach(item => {
        list.push(item.id);
      });
      return list;
    }
  },
  watch: {
    selOpts(val) {
      this.colShow = new Array(42).fill(false);
      val.forEach((el, index) => {
        this.$set(this.colShow, el, true);
      });
    }
  },
  methods: {
    ...mapActions('reportForms', ['getDayTotalReport', 'getDayGroupReport']),
    ...mapActions('chartsForm', ['exportGroupStatement', 'queryGroupName']),
    handleSizeChange: function(size) {
      this.pagesize = size;
    },
    filterMethod(data) {
      if (data) {
        this.groupAllOption = this.groupList.filter(item => {
          if (
            item.fullName.indexOf(data) > -1 ||
            item.fullName.toLowerCase().indexOf(data.toLowerCase()) > -1
          ) {
            return true;
          }
        });
      } else {
        this.groupAllOption = this.groupList;
      }
    },
    initGroupChart() {
      this.loading = true;
      let groupChart = this.$echarts.init(
        document.getElementById('idgroupChart')
      );
      let xAxis = [];
      let mainData = {
        funcLackNum: [],
        rqrRuleNum: [],
        rqrNum: [],
        funcErrNum: [],
        historyNum: [],
        optimizeNum: [],
        backNum: [],
        packageNum: [],
        dataNum: [],
        envNum: [],
        otherNum: []
      };
      this.dailyReport.forEach(item => {
        xAxis.push(item.groupName);
        mainData.funcLackNum.push(item.funcLackNum);
        mainData.rqrNum.push(item.rqrNum);
        mainData.rqrRuleNum.push(item.rqrRuleNum);
        mainData.funcErrNum.push(item.funcErrNum);
        mainData.historyNum.push(item.historyNum);
        mainData.optimizeNum.push(item.optimizeNum);
        mainData.backNum.push(item.backNum);
        mainData.packageNum.push(item.packageNum);
        mainData.dataNum.push(item.dataNum);
        mainData.envNum.push(item.envNum);
        mainData.otherNum.push(item.otherNum);
      });
      let checkArr = ['环境问题', '其他原因', '数据问题'];
      let text =
        this.searchOrder.date.length > 0
          ? `小组缺陷趋势${this.searchOrder.date[0]}至${this.searchOrder.date[1]}期`
          : `小组缺陷趋势`;
      groupChart.setOption({
        title: {
          text,
          x: 'center'
        },
        legend: {
          data: [
            '业务需求问题',
            '需规问题',
            '功能实现不完整',
            '功能实现错误',
            '历史遗留问题',
            '优化建议',
            '后台问题',
            '打包问题',
            '数据问题',
            '环境问题',
            '其他原因'
          ],
          orient: 'vertical',
          x: 'right',
          top: 'middle'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(param) {
            let leftData = [];
            let rightData = [];
            let leftElement = '';
            let rightElement = '';
            param.forEach((item, index) => {
              if (checkArr.indexOf(item.seriesName) !== -1) {
                rightData.push(`<div>${item.seriesName} ${item.value}</div>`);
              } else {
                leftData.push(`<div>${item.seriesName} ${item.value}</div>`);
              }
            });
            leftElement = leftData.join('<br>');
            rightElement = rightData.join('<br>');
            return `
            <div>
            <p style="text-align:center">${param[0].name} </p>
            <div style="display:flex">
              <div style="margin-right:10px;">
              <p>(有效缺陷)</p>
              <div>${leftElement}</div>
              </div>
              <div><p>(无效缺陷)</p>
              <div>${rightElement}</div>
              </div>
            </div>
            </div>`;
          }
        },

        yAxis: {
          type: 'value'
        },
        xAxis: {
          data: xAxis,
          axisLabel: {
            interval: 0,
            rotate: 30,
            showMinLabel: true,
            showMaxLabel: true
          }
        },
        series: [
          {
            name: '业务需求问题',
            type: 'bar',
            stack: '有效缺陷',
            data: mainData.rqrNum
          },
          {
            name: '需规问题',
            type: 'bar',
            stack: '有效缺陷',
            data: mainData.rqrRuleNum
          },
          {
            name: '功能实现不完整',
            type: 'bar',
            stack: '有效缺陷',
            data: mainData.funcLackNum
          },
          {
            name: '功能实现错误',
            type: 'bar',
            stack: '有效缺陷',
            data: mainData.funcErrNum
          },
          {
            name: '历史遗留问题',
            stack: '有效缺陷',
            type: 'bar',
            data: mainData.historyNum
          },

          {
            name: '优化建议',
            stack: '有效缺陷',
            type: 'bar',
            data: mainData.optimizeNum
          },

          {
            name: '后台问题',
            stack: '有效缺陷',
            type: 'bar',
            data: mainData.backNum
          },
          {
            name: '打包问题',
            stack: '有效缺陷',
            type: 'bar',
            data: mainData.packageNum
          },

          {
            name: '数据问题',
            stack: '无效缺陷',
            type: 'bar',
            data: mainData.dataNum
          },
          {
            name: '环境问题',
            type: 'bar',
            stack: '无效缺陷',
            data: mainData.envNum
          },
          {
            name: '其他原因',
            stack: '无效缺陷',
            type: 'bar',
            data: mainData.otherNum
          }
        ]
      });
      this.loading = false;
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    handleGroupSizeChange: function(size) {
      this.groupPagesize = size;
    },
    handleGroupCurrentChange: function(currentPage) {
      this.groupCurrentPage = currentPage;
    },
    queryDailyReport(searchOrder) {
      this.$refs[searchOrder].validate(async valid => {
        if (valid) {
          this.loading = true;
          const date = this.searchOrder.date;
          await this.getDayTotalReport({
            startDate: date[0],
            endDate: date[1],
            group:
              this.groupIdList.length > 0 ? this.groupIdList : this.allGroup,
            isParent: this.childGroupFlag
          });
          this.initGroupChart();
          this.loading = false;
        }
      });
    },
    async queryDailyGroupReport(groupId, groupName) {
      const date = this.searchOrder.date;
      this.groupId = groupId;
      this.groupName = groupName;
      this.loading = true;
      await this.getDayGroupReport({
        startDate: date[0],
        endDate: date[1],
        groupId,
        isParent: this.childGroupFlag
      });
      this.loading = false;
      this.showDailyReport = false;
    },
    returnDailyReport() {
      this.showDailyReport = true;
    },
    // 下载导出组维度报表
    async download() {
      this.$refs['searchOrder'].validate(async valid => {
        if (valid) {
          const date = this.searchOrder.date;
          await this.exportGroupStatement({
            group:
              this.groupIdList.length > 0 ? this.groupIdList : this.allGroup,
            startDate: date[0],
            endDate: date[1],
            isParent: this.childGroupFlag
          });
          exportExcel(this.groupData);
          this.$message({
            type: 'success',
            message: '全部导出成功!'
          });
        }
      });
    }
  },
  beforeMount() {
    this.colShow = new Array(42).fill(false);
    [0, 1, 2, 13, 14, 15, 16, 19, 20].forEach(item => {
      this.colShow[item] = true;
    });
  },
  async mounted() {
    this.searchOrder.date = this.searchOrderDate;
    await this.initGroupChart();

    if (this.groupList.length <= 1) {
      await this.queryGroupName();
    }
    this.groupAllOption = this.groupList;
  }
};
</script>

<style scoped>
.chart-container {
  width: 100%;
  margin: -20px auto;
  text-align: left;
  padding: 20px 0;
}
.title {
  font-size: 20px;
  margin-bottom: 10px;
  text-align: left;
}
.chart-con {
  width: 86%;
  margin: 0 auto;
}
.margin-left {
  margin-left: 15px;
}
.pagination {
  margin-top: 20px;
  padding: 20px 0;
  text-align: right;
}
#idgroupChart {
  width: 100%;
  height: 50vh;
}
.el-table .caret-wrapper {
  width: auto !important;
}
</style>
