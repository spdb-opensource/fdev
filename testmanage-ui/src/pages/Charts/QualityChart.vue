<template>
  <div class="chart-container ">
    <el-container class="chart-con ">
      <strong class="title">质量分析</strong>
      <el-header class="headerUser">
        <el-form
          :inline="true"
          :model="searchQuality"
          :rules="searchRules"
          class="header"
          ref="searchQuality"
        >
          <el-form-item prop="date">
            <el-date-picker
              size="medium"
              v-model="date"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              :picker-options="pickerOptions"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item prop="selectList">
            <el-cascader
              :clearable="true"
              :options="optionsList"
              :props="{
                multiple: true,
                checkStrictly: true,
                label: 'name',
                value: 'id',
                emitPath: false
              }"
              size="medium"
              v-model="searchQuality.selectList"
              placeholder="请选择组名"
              collapse-tags
              :show-all-levels="false"
            >
            </el-cascader>
          </el-form-item>
          <el-form-item prop="isParent">
            <el-switch
              v-model="isParent"
              active-text="展示子组"
              :active-value="1"
              :inactive-value="0"
            >
            </el-switch>
          </el-form-item>
          <el-form-item>
            <el-button type="text" @click="resetSearchList">
              重置
            </el-button>
            <el-button type="primary" @click="queryQuality('searchQuality')">
              查询
            </el-button>
            <el-button type="primary" @click="exportQualityList()">
              导出
            </el-button>
          </el-form-item>
        </el-form>
      </el-header>
      <!-- 质量分析列表 -->
      <el-main style="padding:10px;">
        <el-table
          v-loading="loading"
          :data="
            qualityReport.slice(
              (currentPage - 1) * pagesize,
              currentPage * pagesize
            )
          "
          style="color:black"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column prop="name" label="组名" width="170">
          </el-table-column>
          <el-table-column label="提测准时率" width="170">
            <template slot-scope="scope">
              <el-link type="primary" :underline="false">
                <div
                  @click="
                    showTimelyDialog(
                      scope.row.fdevGroupId,
                      QualityTableData.TimelyRateList.type
                    )
                  "
                >
                  {{ (scope.row.timelyRate * 1000) / 10 }}%
                </div>
              </el-link>
              <!-- 二进制十进制转换出现浮点精确问题，取整了 -->
            </template>
          </el-table-column>
          <el-table-column label="冒烟测试通过率">
            <template slot-scope="scope">
              <el-link type="primary" :underline="false">
                <div
                  @click="
                    showTimelyDialog(
                      scope.row.fdevGroupId,
                      QualityTableData.SmokeRateList.type
                    )
                  "
                >
                  {{ (scope.row.smokeRate * 1000) / 10 }}%
                </div>
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="缺陷reopen率">
            <template slot-scope="scope">
              <el-link type="primary" :underline="false">
                <div
                  @click="
                    showTimelyDialog(
                      scope.row.fdevGroupId,
                      QualityTableData.ReopenRateList.type
                    )
                  "
                >
                  {{ (scope.row.reopenRate * 1000) / 10 }}%
                </div>
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="mantisRate" label="缺陷密度">
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="
                  `(有效缺陷数:${scope.row.sumMantis}/案例执行总数:${scope.row.exeTime})`
                "
                placement="top-start"
                class="tooltip-div"
              >
                <span>{{ scope.row.mantisRate }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="normalAvgTime" label="缺陷平均解决时长">
            <template slot-scope="scope">
              <el-link type="primary" :underline="false">
                <div
                  @click="
                    showTimelyDialog(
                      scope.row.fdevGroupId,
                      QualityTableData.NormalAvgList.type
                    )
                  "
                >
                  {{ scope.row.normalAvgTime }}
                </div>
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="sevAvgTime" label="严重缺陷解决时长">
            <template slot-scope="scope">
              <el-link type="primary" :underline="false">
                <div
                  @click="
                    showTimelyDialog(
                      scope.row.fdevGroupId,
                      QualityTableData.SevAvgList.type
                    )
                  "
                >
                  {{ scope.row.sevAvgTime }}
                </div>
              </el-link>
            </template>
          </el-table-column>
        </el-table>
        <div class="table-text">
          备注：1、提测准时率=提测准时任务数/应提测工单的任务总数*100%。
          <div class="text-content">
            2、冒烟测试通过率 =
            冒烟测试通过任务数/提交测试任务总数*100%(被不同人或不同日期或不同原因多次打回的记录多次)；
          </div>
        </div>
        <div class="pagination">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 11, 50, 100, 200, 500]"
            :page-size="pagesize"
            layout="sizes, prev, pager, next, jumper"
            :total="qualityReport.length"
          >
          </el-pagination>
        </div>
      </el-main>
      <el-row :gutter="30">
        <el-col
          element-loading-background="#FFFFFF"
          class="bar-main"
          v-loading="chartLoading"
        >
          <div class="relative">
            <!-- 提测准时率 -->
            <div id="timelyRate"></div>
          </div>
          <div class="relative">
            <!-- 提测任务数 -->
            <div id="taskCount"></div>
          </div>
          <div class="relative">
            <!-- 缺陷数 -->
            <div id="countMantis"></div>
          </div>
        </el-col>
        <el-col class="bar-main">
          <div class="relative">
            <div id="devQuality" v-loading="loading"></div>
          </div>
          <div class="chart-text">
            备注：1、现有缺陷率 = 玉衡登记BUG数/玉衡执行案例数；
            <div class="text-content">
              2、reopen率=再次打开缺陷数/有效缺陷总数；
            </div>
            <div class="text-content">
              3、reopen方式为缺陷被记录打开两次以及两次以上；
            </div>
            <div class="text-content">
              4、冒烟通过率=冒烟通过任务数/已提交内测任务总数*100%。
            </div>
          </div>
        </el-col>
        <el-col class="bar-main">
          <div class="relative">
            <div id="smokeText" v-loading="loading"></div>
          </div>
          <div class="chart-text">
            备注：1、各组严重缺陷解决平均时长：各组平均解决时长=缺陷解决总时长/严重缺陷总数；
            <div class="text-content">
              2、各组严重缺陷选用类型——严重性：很严重、崩溃，宕机或优先级：高、紧急、非常紧急；
            </div>
            <div class="text-content">
              3、各组缺陷解决平均时长： 各组平均解决时长 =
              缺陷解决总时长/缺陷总数；
            </div>
            <div class="text-content">4、各组缺陷选用类型： 有效缺陷；</div>
            <div class="text-content">
              5、时间来源： 缺陷创建时间、 缺陷最后修复时间。
            </div>
          </div>
        </el-col>
      </el-row>
      <Dialog
        :title="title"
        width="80%"
        :visible="showContent"
        :beforeClose="() => (showContent = false)"
        closeOnClickModal
      >
        <el-button
          style="float:right"
          type="primary"
          @click="qualityReportNewUnitExport()"
        >
          导出
        </el-button>
        <el-table
          :data="
            tableInfo.slice(
              (currentDialogPage - 1) * pageDialogsize,
              currentDialogPage * pageDialogsize
            )
          "
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column
            v-for="(item, index) in list"
            :label="item.label"
            :prop="item.prop"
            :key="index"
            :show-overflow-tooltip="item.prop !== 'planSitDate'"
            :width="item.prop === 'planSitDate' ? '130px' : ''"
          >
            <template slot-scope="scope">
              <span v-if="item.prop === 'priority'">{{
                formatPriority(scope.row[item.prop])
              }}</span>
              <span v-else-if="item.prop === 'severity'">{{
                formatSeverity(scope.row[item.prop])
              }}</span>
              <span v-else-if="item.prop === 'solveTime'">{{
                scope.row[item.prop] + '天'
              }}</span>
              <span v-else-if="item.prop === 'reason'">{{
                scope.row[item.prop] | reasonType
              }}</span>
              <span v-else>{{ scope.row[item.prop] }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div
          v-if="QualityTableData.TimelyRateList.title === title"
          class="bottomDiv"
        >
          <div>准时提测数：{{ submitCountObj.timely }}</div>
          <div>未准时提测数：{{ submitCountObj.notTimely }}</div>
          <div>应提测任务总数：{{ submitCountObj.total }}</div>
          <div>
            提测准时率：{{
              submitCountObj.total !== 0
                ? (
                    (submitCountObj.timely / submitCountObj.total) *
                    100
                  ).toFixed(2) + '%'
                : 0 + '%'
            }}
          </div>
        </div>
        <div class="pagination">
          <el-pagination
            @size-change="handleDialogSizeChange"
            @current-change="handleDialogCurrentChange"
            :current-page="currentDialogPage"
            :page-sizes="[5, 10, 50, 100, 200, 500]"
            :page-size="pageDialogsize"
            layout="sizes, prev, pager, next, jumper"
            :total="tableInfo.length"
          >
          </el-pagination>
        </div>
      </Dialog>
    </el-container>
  </div>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import { deepClone, validate } from '@/common/utlis';
import { orderOptions, QualityTableData } from './model';
import { optionPriority, optionSeverity } from '../Mantis/model';
import { exportExcel } from '@/common/utlis';
import {
  qualityReportExport,
  qualityReportNewUnitExport,
  querySubmitTimelyAndMantis
} from '@/services/order.js';
import moment from 'moment';
export default {
  name: 'QualityChart',
  data() {
    return {
      chartLoading: false,
      date: [
        moment(new Date())
          .subtract(1, 'months')
          .format('YYYY-MM-DD'),
        moment().format('YYYY-MM-DD')
      ],
      timeOptionRange: '',
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
          let seven = 60 * 60 * 24 * 182 * 1000;
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
      loading: false,
      isParent: '0',
      currentPage: 1,
      pagesize: 11,
      currentDialogPage: 1,
      pageDialogsize: 10,
      fdevGroups: [],
      searchQuality: {
        selectList: []
      },
      optionsList: [],
      submitCountObj: null,
      tableInfo: [],
      qualityList: [],
      xAxisList: [],
      yAxisList: [],
      resetList: [
        '5c81c56cd3e2a1126ce3004b',
        '5e5885cd13149c000c4a2777',
        '123asdasb3241ad13adada13',
        '5d3e93ce606eeb000a22d320',
        '5d3e93ed606eeb000a22d321',
        '5d3e93f5606eeb000a22d322',
        '5d3e93fe606eeb000a22d323',
        '5f9bc3479513dd000c0a1e68',
        '5d3e9418606eeb000a22d326',
        '5d6c84ce054583000aa4e9af',
        '5f9a60899513dd000c0a1b4c'
      ], //默认大组id
      orderOptions: orderOptions,
      searchRules: {
        selectList: [
          {
            type: 'array',
            required: true,
            message: '请选择小组',
            trigger: 'change'
          }
        ]
      },
      showContent: false,
      list: [],
      title: '',
      optionPriority,
      optionSeverity,
      QualityTableData,
      // 折线图 任务数 缺陷数 提测准确率
      legendData: [], //['A', 'B', 'C'],
      xAxisData: [], //['2021-12-01', '2021-12-08', '2021-12-15', '2021-12-22'],
      seriesData: {
        timelyRate: [], //纵坐标
        taskCount: [], //纵坐标
        countMantis: [] //纵坐标
      }
    };
  },
  computed: {
    ...mapState('chartsForm', [
      'defectList',
      'qualityReport',
      'fdevGroupList',
      'qualityReportDetail'
    ])
  },
  methods: {
    ...mapActions('chartsForm', [
      'queryQualityReport',
      'queryFdevGroup',
      'qualityReportNewUnit'
    ]),
    async querySubmitTimelyAndMantis(params) {
      this.chartLoading = true;
      let res = await querySubmitTimelyAndMantis(params);
      this.xAxisData = [];
      this.legendData = [];
      this.seriesData = {
        timelyRate: [], //纵坐标
        taskCount: [], //纵坐标
        countMantis: [] //纵坐标
      };
      this.xAxisData = res.map(item => Object.keys(item)).flat(); //横坐标 日期
      this.legendData = res[0][this.xAxisData[0]].map(item => item.groupName);
      let l = this.legendData.length;
      for (let i = 0; i < l; i++) {
        this.seriesData.timelyRate.push({
          name: this.legendData[i],
          data: res.map((item, index) => {
            if (
              item[this.xAxisData[index]][i].groupName == this.legendData[i]
            ) {
              return (item[this.xAxisData[index]][i].timelyRate * 100).toFixed(
                2
              );
            }
          }),
          type: 'line',
          itemStyle: {
            normal: {
              label: {
                show: true
              }
            }
          },
          symbolSize: 5,
          show: false
        });
        this.seriesData.taskCount.push({
          name: this.legendData[i],
          data: res.map((item, index) => {
            if (
              item[this.xAxisData[index]][i].groupName == this.legendData[i]
            ) {
              return item[this.xAxisData[index]][i].taskCount;
            }
          }),
          type: 'line',
          itemStyle: {
            normal: {
              label: {
                show: true
              }
            }
          },
          symbolSize: 5,
          show: false
        });
        this.seriesData.countMantis.push({
          name: this.legendData[i],
          data: res.map((item, index) => {
            if (
              item[this.xAxisData[index]][i].groupName == this.legendData[i]
            ) {
              return item[this.xAxisData[index]][i].countMantis;
            }
          }),
          type: 'line',
          itemStyle: {
            normal: {
              label: {
                show: true
              }
            }
          },
          symbolSize: 5,
          show: false
        });
      }
      // 查询指定时间段内每个时间节点内提测准时率、提测任务数,缺陷数
      this.initReport('timelyRate');
      this.initReport('taskCount');
      this.initReport('countMantis');
    },
    initReport(type) {
      this.$echarts.init(document.getElementById(type)).dispose();
      let devQualityChart = this.$echarts.init(document.getElementById(type));
      devQualityChart.setOption({
        title: {
          text: this.getReportName(type),
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          //formatter: '{b0} <br/> {a0}: {c0}天 <br/> {a1}: {c1}天' //格式化提示框，其中a,b,c为echarts官方规定模板系列名，属性名和属性值。
          formatter: function(params) {
            var str = `${params[0].name}`;
            params.forEach((item, index) => {
              str = `${str} <br/>${item.marker} ${item.seriesName}：${
                item.value
              }${type == 'timelyRate' ? '%' : ''}`;
            });
            return str;
          }
        },
        legend: {
          data: this.legendData,
          top: '10%',
          right: '0',
          orient: 'vertical',
          type: 'scroll'
        },
        grid: {
          left: '5%',
          right: '15%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.xAxisData,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          name: type == 'timelyRate' ? '百分比(%)' : '',
          type: 'value',
          axisLabel: {
            formatter: `{value}`
          }
        },
        series: this.seriesData[type]
      });
      this.chartLoading = false;
    },
    getReportName(type) {
      let name = {
        timelyRate: '提测准时率',
        taskCount: '提测任务数',
        countMantis: '缺陷数'
      };
      return name[type];
    },
    initsolveTime() {
      let smokeTextChart = this.$echarts.init(
        document.getElementById('smokeText')
      );
      smokeTextChart.setOption({
        title: {
          text: '解决时长',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          //formatter: '{b0} <br/> {a0}: {c0}天 <br/> {a1}: {c1}天' //格式化提示框，其中a,b,c为echarts官方规定模板系列名，属性名和属性值。
          formatter: function(params) {
            var str = `${params[0].name}`;
            params.forEach((item, index) => {
              str = `${str} <br/>${item.marker} ${item.seriesName}：${item.value}天`;
            });
            return str;
          }
        },
        legend: {
          data: ['严重缺陷解决平均时长', '缺陷解决平均时长'],
          top: '10%',
          right: '0',
          selected: {},
          orient: 'vertical',
          type: 'scroll'
        },
        grid: {
          left: '5%',
          right: '15%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.xAxisList,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '天数(天)'
        },
        series: [
          {
            name: '严重缺陷解决平均时长',
            type: 'bar',
            itemStyle: {
              normal: {
                color: '#2f4554',
                label: {
                  show: true
                }
              }
            },
            show: false,
            data: this.yAxisList.sevAvgTime
          },
          {
            name: '缺陷解决平均时长',
            type: 'bar',
            itemStyle: {
              normal: {
                color: '#61a0a8',
                label: {
                  show: true
                }
              }
            },
            data: this.yAxisList.normalAvgTime
          }
        ]
      });
    },
    async exportQualityList() {
      const date = this.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      let params = {
        startDate: startDate ? startDate : '',
        endDate: endDate ? endDate : '',
        groupId:
          this.searchQuality.selectList == this.resetList
            ? this.resetList
            : this.searchQuality.selectList,
        isParent: this.isParent
      };
      let res = await qualityReportExport(params);
      exportExcel(res);
      this.$message({
        type: 'success',
        message: '导出成功!'
      });
    },
    async qualityReportNewUnitExport(params) {
      let res = await qualityReportNewUnitExport(
        this.qualityReportNewUnitDetail
      );
      exportExcel(res);
      this.$message({
        type: 'success',
        message: '导出成功!'
      });
    },
    async queryQuality(searchQuality) {
      await validate(this.$refs[searchQuality]);
      this.loading = true;
      await this.init();
      this.currentPage = 1;
      this.initChart();
      this.loading = false;
    },
    handleSizeChange: function(size) {
      this.pagesize = size;
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    handleDialogSizeChange: function(size) {
      this.pageDialogsize = size;
    },
    handleDialogCurrentChange: function(currentPage) {
      this.currentDialogPage = currentPage;
    },
    async showTimelyDialog(id, type) {
      this.pageDialogsize = 10;
      this.currentDialogPage = 1;
      const date = this.date;
      let startDate = '';
      let endDate = '';
      let dialogData = {};
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      let detailParams = {
        startDate: startDate ? startDate : '',
        endDate: endDate ? endDate : '',
        fdevGroupId: id,
        isParent: this.isParent
      };
      // timelyRate -提测准时率，
      // smokeRate -冒烟测试通过率，
      // reopenRate -缺陷reopen率，
      // mantisRate -缺陷密度，
      // normalAvgTime -缺陷平均解决时长，
      // sevAvgTime -严重缺陷解决时长
      let Dtype = {
        0: 'timelyRate',
        1: 'smokeRate',
        2: 'reopenRate',
        3: 'normalAvgTime',
        4: 'sevAvgTime',
        5: 'mantisRate'
      };
      this.qualityReportNewUnitDetail = {
        ...detailParams,
        reportType: Dtype[type]
      };
      await this.qualityReportNewUnit(detailParams);

      switch (type) {
        case this.QualityTableData.TimelyRateList.type: {
          this.tableInfo = this.qualityReportDetail.submitInfo;
          dialogData = this.QualityTableData.TimelyRateList;
          this.submitCountObj = this.qualityReportDetail.submitCount;
          break;
        }
        case this.QualityTableData.SmokeRateList.type: {
          this.tableInfo = this.qualityReportDetail.rollBackInfo;
          dialogData = this.QualityTableData.SmokeRateList;
          break;
        }
        case this.QualityTableData.ReopenRateList.type: {
          this.tableInfo = this.qualityReportDetail.reopenIssue;
          dialogData = this.QualityTableData.ReopenRateList;
          break;
        }
        case this.QualityTableData.NormalAvgList.type: {
          this.tableInfo = this.qualityReportDetail.solveTimeRecord;
          dialogData = this.QualityTableData.NormalAvgList;
          break;
        }
        case this.QualityTableData.SevAvgList.type: {
          this.tableInfo = this.qualityReportDetail.severeSolveTimeRecord;
          dialogData = this.QualityTableData.SevAvgList;
          break;
        }
      }
      this.list = dialogData.list;
      this.title = dialogData.title;
      this.showContent = true;
    },
    formatPriority(val) {
      let res =
        this.optionPriority.find(item => {
          return item.label === String(val);
        }) || {};
      return res.value || '';
    },
    formatSeverity(val) {
      let res =
        this.optionSeverity.find(item => {
          return item.label === String(val);
        }) || {};
      return res.value || '';
    },
    //把原数组转成符合树状结构的数组
    arrayToTree(data) {
      let result = [];
      if (!Array.isArray(data)) {
        return result;
      }
      data.forEach(item => {
        delete item.children;
      });
      let map = {};
      data.forEach(item => {
        map[item.id] = item;
      });
      data.forEach(item => {
        let parent = map[item.parent_id];
        if (parent) {
          (parent.children || (parent.children = [])).push(item);
        } else {
          result.push(item);
        }
      });
      return result;
    },
    //生成符合本页echarts坐标结构的数组
    getChartAxis() {
      let qualityList = deepClone(this.qualityReport);
      this.xAxisList = qualityList.map(item => {
        return item.name;
      });
      this.yAxisList.smokeRate = qualityList.map(item => {
        return (item.smokeRate * 1000) / 10;
      });
      this.yAxisList.reopenRate = qualityList.map(item => {
        return (item.reopenRate * 1000) / 10;
      });
      this.yAxisList.sevAvgTime = qualityList.map(item => {
        return item.sevAvgTime.slice(0, 5);
      });
      this.yAxisList.normalAvgTime = qualityList.map(item => {
        return item.normalAvgTime.slice(0, 5);
      });
      this.yAxisList.mantisRate = qualityList.map(item => {
        return (item.mantisRate * 1000) / 10;
      });
    },
    resetSearchList() {
      this.searchQuality.selectList = this.resetList;
    },
    initChart() {
      this.getChartAxis();
      this.initDevQuality();
      this.initsolveTime();
    },

    initDevQuality() {
      let devQualityChart = this.$echarts.init(
        document.getElementById('devQuality')
      );
      devQualityChart.setOption({
        title: {
          text: '缺陷密度,缺陷reopen率,冒烟测试通过率——折线图',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          //格式化提示框，其中a,b,c为echarts官方规定模板系列名，属性名和属性值。
          // formatter: '{b0} <br/> {a0}: {c0} <br/> {a1}: {c1}%<br/> {a2}: {c2}%'
          formatter: function(params) {
            var str = `${params[0].name}`;
            params.forEach((item, index) => {
              str = `${str} <br/> ${item.marker}${item.seriesName}：${item.value}%`;
            });
            return str;
          }
        },
        legend: {
          data: ['缺陷密度', '缺陷reopen率', '冒烟测试通过率'],
          top: '10%',
          right: '0',
          selected: {},
          orient: 'vertical',
          type: 'scroll'
        },
        grid: {
          left: '5%',
          right: '15%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.xAxisList,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '缺陷密度',
            type: 'line',
            itemStyle: {
              normal: {
                label: {
                  show: true
                }
              }
            },
            symbolSize: 5,
            show: false,
            data: this.yAxisList.mantisRate
          },
          {
            name: '缺陷reopen率',
            type: 'line',
            itemStyle: {
              normal: {
                label: {
                  show: true
                }
              }
            },
            symbolSize: 5,
            show: false,
            data: this.yAxisList.reopenRate
          },
          {
            name: '冒烟测试通过率',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#A9A9A9',
                label: {
                  show: true
                }
              }
            },
            symbolSize: 5,
            show: false,
            data: this.yAxisList.smokeRate
          }
        ]
      });
    },
    async init() {
      const date = this.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      let params = {
        startDate: startDate ? startDate : '',
        endDate: endDate ? endDate : '',
        groupId:
          this.searchQuality.selectList == this.resetList
            ? this.resetList
            : this.searchQuality.selectList,
        isParent: this.isParent
      };

      let querySubmitTimelyAndMantisParams = {
        startDate: startDate ? startDate : '',
        endDate: endDate ? endDate : '',
        groupIds:
          this.searchQuality.selectList == this.resetList
            ? this.resetList
            : this.searchQuality.selectList,
        isIncludeChildren: this.isParent == 1 ? true : false
      };
      this.querySubmitTimelyAndMantis(querySubmitTimelyAndMantisParams);
      await this.queryQualityReport(params);
    }
  },
  async created() {
    //查询fdev小组
    this.loading = true;
    await this.queryFdevGroup();
    this.fdevGroups = this.fdevGroupList.filter(item => item.status === '1');
    this.optionsList = this.arrayToTree(this.fdevGroups);
    this.searchQuality.selectList = deepClone(this.resetList);
    this.getChartAxis();
    await this.init();
    await this.initChart();
    this.loading = false;
  },
  async mounted() {
    await this.initChart();
  },
  filters: {
    reasonType(val) {
      let obj = {
        '1': '文档不规范',
        '2': '文档缺失',
        '3': '冒烟测试不通过'
      };
      return obj[val];
    }
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
.switch {
  padding-top: 9px;
}
.el-pagination {
  margin-top: 30px;
  text-align: right;
}
#devQuality,
#devReopen,
#smokeText,
#solveTime,
#timelyRate,
#taskCount,
#countMantis {
  width: 100%;
  height: 50vh;
}
.table-text {
  margin: 20px 0 0 8px;
  font-size: 15px;
}
.chart-text {
  margin: 15px 0 20px 60px;
  font-size: 15px;
}
.text-content {
  margin-left: 45px;
}
.headerUser {
  height: auto !important;
}
.tooltip-div {
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bottomDiv {
  display: flex;
  padding-top: 20px;
  color: black;
  justify-content: space-between;
}
</style>
