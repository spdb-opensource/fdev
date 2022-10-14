<template>
  <div class="chart-container ">
    <el-container class="chart-con ">
      <strong class="title">缺陷及生产问题趋势</strong>
      <el-header>
        <div id="divFlex">
          <div id="divOne">
            <el-form :inline="true" class="header">
              <el-form-item prop="type">
                <t-select
                  v-model="type"
                  filterable
                  clearable
                  placeholder="请选择"
                  :options="orderOptions"
                  prop="label"
                  option-label="label"
                  option-value="value"
                  @input="typeChange"
                />
              </el-form-item>
            </el-form>
          </div>
          <el-form
            id="form"
            :inline="true"
            ref="searchRulesChart"
            :model="searchRulesChart"
            :rules="rule"
          >
            <el-form-item prop="date">
              <el-date-picker
                v-if="type !== 'month'"
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
                v-if="type == 'month'"
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
            <el-form-item style="margin-left:16px !important">
              <el-select
                filterable
                multiple
                placeholder="请选择小组"
                v-model="fdev_group_id"
                :filter-method="filterMethod"
                clearable
                collapse-tags
              >
                <el-option
                  v-for="item in groupAllOption"
                  :label="
                    item.fullName.length > 6
                      ? item.fullName.substring(0, 6) + '...'
                      : item.fullName
                  "
                  :value="item.id"
                  :key="item.id"
                >
                  <span> {{ item.fullName }} </span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-button type="primary" @click="dateChange" class="margin-left"
              >查询</el-button
            >
            <el-button type="primary" @click="exportMantis" class="margin-left"
              >导出</el-button
            >
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
          </el-form>
        </div>
      </el-header>
      <el-main style="padding:10px">
        <el-table
          :data="
            reportData.slice(
              (currentPage - 1) * pagesize,
              currentPage * pagesize
            )
          "
          style="color:black"
          tooltip-effect="dark"
          v-loading="showLoading"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column v-if="colShow[0]" prop="xMsg" label="日期">
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                placement="top-start"
                class="tooltip"
                :content="scope.row.xMsg"
              >
                <div>{{ scope.row.xMsg }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="colShow[1]" prop="orderNum" label="工单数" />
          <el-table-column v-if="colShow[2]" prop="sumExe" label="总执行数" />
          <el-table-column
            v-if="colShow[3]"
            prop="testMantis"
            label="缺陷总数"
          />

          <el-table-column
            v-if="colShow[4]"
            prop="effectiveIssue"
            label="有效缺陷"
          />
          <el-table-column
            v-if="colShow[5]"
            prop="ineffectiveIssue"
            label="无效缺陷"
          />
          <el-table-column
            v-if="colShow[6]"
            prop="proIssueDev"
            label="开发相关生产问题"
          />
          <el-table-column
            v-if="colShow[7]"
            prop="developMantis"
            label="开发相关缺陷"
          />
          <el-table-column
            v-if="colShow[8]"
            prop="proIssueSit"
            label="内测相关生产问题"
          />

          <el-table-column
            v-if="colShow[9]"
            prop="rqrNum"
            label="业务需求问题"
          />
          <el-table-column
            v-if="colShow[10]"
            prop="rqrRuleNum"
            label="需规问题"
            fixed="left"
          />
          <el-table-column
            v-if="colShow[11]"
            prop="funcLackNum"
            label="功能实现不完整"
          />
          <el-table-column
            v-if="colShow[12]"
            prop="funcErrNum"
            label="功能实现错误"
          />
          <el-table-column
            v-if="colShow[13]"
            prop="historyNum"
            label="历史遗留问题"
          />
          <el-table-column
            v-if="colShow[14]"
            prop="optimizeNum"
            label="优化建议"
          />
          <el-table-column v-if="colShow[15]" prop="backNum" label="后台问题" />
          <el-table-column
            v-if="colShow[16]"
            prop="packageNum"
            label="打包问题"
          />
          <el-table-column v-if="colShow[17]" prop="dataNum" label="数据问题" />
          <el-table-column v-if="colShow[18]" prop="envNum" label="环境问题" />
          <el-table-column
            v-if="colShow[19]"
            prop="otherNum"
            label="其他原因"
          />
          <el-table-column
            v-if="colShow[20]"
            prop="effectiveIssueRate"
            label="有效缺陷率"
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
            :total="reportData.length"
          ></el-pagination>
        </div>
      </el-main>
      <el-divider></el-divider>
      <div>
        <strong>缺陷趋势图</strong>

        <div v-loading="showLoading">
          <div class="relative">
            <div id="devQuality"></div>
          </div>
        </div>
      </div>
    </el-container>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { rules, searchOrder } from './model';
import { orderOptions } from './model';
import { validate } from '@/common/utlis';

export default {
  name: 'WorkChart',
  data() {
    return {
      groupAllOption: [],
      fdev_group_id: [],
      rule1: {
        date: [{ required: true, message: '请输入时间范围', trigger: 'blur' }]
      },
      rule2: {
        dateMonth: [
          { required: true, message: '请输入时间范围', trigger: 'blur' }
        ]
      },
      userEnName: '' + sessionStorage.getItem('user_en_name'),
      timeOptionRange: '',
      currentPage: 1,
      pagesize: 5,
      spanArr: [],
      type: 'day',
      dataChart1: [],
      searchRulesChart: {
        date: [],
        dateMonth: []
      },
      showLoading: false,
      searchOrder: searchOrder(),
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
      orderOptions: orderOptions,
      reportData: [],
      useParam: {},
      colShow: [],
      selOpts: [0, 1, 2, 3, 4, 5, 6, 7, 20],
      headers: [
        '日期',
        '工单数',
        '总执行数',
        '缺陷总数',
        '有效缺陷',
        '无效缺陷',
        '开发相关生产问题',
        '开发相关缺陷',
        '内测相关生产问题',
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
      ]
    };
  },
  computed: {
    ...mapState('chartsForm', [
      'weekWinnerData',
      'teamDefectChart',
      'defectClassifyChart',
      'exportExcelWeek',
      'exportAllExcelWeek',
      'defectList'
    ]),
    ...mapState('adminForm', ['groupAll']),
    rule() {
      if (this.type !== 'month') {
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
            switch (this.type) {
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
      if (this.type === 'month') {
        item = 'monthrange';
      } else {
        item = 'daterange';
      }
      return item;
    },
    valFormat() {
      let item = '';
      if (this.type === 'month') {
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
  watch: {
    selOpts(val) {
      this.colShow = new Array(20).fill(false);
      val.forEach((el, index) => {
        this.$set(this.colShow, el, true);
      });
    }
  },
  methods: {
    ...mapActions('chartsForm', [
      'queryAllReportImpl',
      'testTeamBiweekly',
      'listOfDevelopDefects',
      'teamDefectClassify',
      'defectClassify',
      'exportAllExcelWeekReport',
      'getDefectList',
      'exportMantisStatement'
    ]),
    ...mapActions('adminForm', ['queryAllGroup']),
    filterMethod(data) {
      if (data) {
        this.groupAllOption = this.groupAll.filter(item => {
          if (
            item.fullName.indexOf(data) > -1 ||
            item.fullName.toLowerCase().indexOf(data.toLowerCase()) > -1
          ) {
            return true;
          }
        });
      } else {
        this.groupAllOption = this.groupAll;
      }
    },
    async dateChange() {
      await validate(this.$refs['searchRulesChart']);
      this.showLoading = true;
      let param = this.getParam();
      this.reportData = [];
      try {
        await this.getDefectList(param);
      } catch (e) {
        this.showLoading = false;
      }
      let length = this.defectList.xMsg.length;
      for (let i = 0; i < length; i++) {
        let obj = {};
        Object.keys(this.defectList).forEach(item => {
          obj[item] = this.defectList[item][i];
        });
        this.reportData.push(obj);
      }
      const res = this.defectList;
      this.dataChart1 = res;
      this.dataChart1.testMantis = res.testMantis;
      this.dataChart1.sumExe = this.dataChart1.sumExe.map(item => {
        return (item = item / 100);
      });
      this.initDevQuality();
      this.showLoading = false;
    },
    getParam() {
      let param = {};
      let num = 0;
      let time = 0;
      let day = 3600 * 1000 * 24;
      let week = 3600 * 1000 * 24 * 7;
      let month = day * 30;
      if (this.type === 'day') {
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
          dateType: '0',
          startDate: this.searchRulesChart.date[0],
          times: num,
          groupIds: this.fdev_group_id
        };
      } else if (this.type === 'week') {
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
          dateType: '1',
          startDate: this.searchRulesChart.date[0],
          times: num,
          groupIds: this.fdev_group_id
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
        }
        param = {
          dateType: '2',
          startDate: this.searchRulesChart.dateMonth[0],
          times: num,
          groupIds: this.fdev_group_id
        };
      }
      return param;
    },
    typeChange() {
      this.timeChartOptionRange = null;
      this.searchRulesChart.date = [];
      this.searchRulesChart.dateMonth = [];
      this.$refs['searchRulesChart'].clearValidate();
    },
    initDevQuality() {
      let devQualityChart = this.$echarts.init(
        document.getElementById('devQuality')
      );
      devQualityChart.setOption({
        title: {
          text: '',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: [
            '缺陷总数',
            '有效缺陷',
            '无效缺陷',
            '开发相关缺陷',
            '内测相关生产问题',
            '开发相关生产问题',
            '工单数',
            '总执行数/100',
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
          top: '10%',
          right: '0px',
          selected: {
            需规问题: false,
            业务需求问题: false,
            '总执行数/100': false,
            环境问题: false,
            其他原因: false,
            功能实现错误: false,
            工单数: false,
            优化建议: false,
            后台问题: false,
            功能实现不完整: false,
            历史遗留问题: false,
            数据问题: false,
            无效缺陷: false,
            打包问题: false
          },
          type: 'scroll'
        },
        grid: {
          left: '3%',
          right: '15%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.dataChart1.xMsg,
          axisLabel: {
            interval: 0,
            rotate: 30,
            showMinLabel: true,
            showMaxLabel: true
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '缺陷总数',
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
            data: this.dataChart1.testMantis
          },
          {
            name: '有效缺陷',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#009FCC',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'triangle',
            symbolSize: 12,
            data: this.dataChart1.effectiveIssue
          },
          {
            name: '无效缺陷',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#FFBB00',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'triangle',
            symbolSize: 12,
            data: this.dataChart1.ineffectiveIssue
          },
          {
            name: '开发相关缺陷',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#00DD77',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'rect',
            symbolSize: 12,
            data: this.dataChart1.developMantis
          },

          {
            name: '内测相关生产问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#99BBFF',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'diamond',
            symbolSize: 12,
            show: false,
            data: this.dataChart1.proIssueSit
          },
          {
            name: '开发相关生产问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#9F88FF',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'rect',
            symbolSize: 12,
            data: this.dataChart1.proIssueDev
          },
          {
            name: '工单数',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#666',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'triangle',
            symbolSize: 12,
            data: this.dataChart1.orderNum
          },
          {
            name: '总执行数/100',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#008844',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'rect',
            symbolSize: 12,
            data: this.dataChart1.sumExe
          },
          {
            name: '业务需求问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#007799',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'diamond',
            symbolSize: 12,
            show: false,
            data: this.dataChart1.rqrNum
          },
          {
            name: '需规问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#bfbfbf',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'triangle',
            symbolSize: 12,
            data: this.dataChart1.rqrRuleNum
          },
          {
            name: '功能实现不完整',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#55AA00',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'triangle',
            symbolSize: 12,
            data: this.dataChart1.funcLackNum
          },
          {
            name: '环境问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#886600',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'triangle',
            symbolSize: 12,
            data: this.dataChart1.envNum
          },
          {
            name: '其他原因',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#880000',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'diamond',
            symbolSize: 12,
            show: false,
            data: this.dataChart1.otherNum
          },
          {
            name: '功能实现错误',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#444',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'rect',
            symbolSize: 12,
            data: this.dataChart1.funcErrNum
          },

          {
            name: '优化建议',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#CC6600',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'diamond',
            symbolSize: 12,
            show: false,
            data: this.dataChart1.optimizeNum
          },
          {
            name: '后台问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#BBBB00',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'rect',
            symbolSize: 12,
            data: this.dataChart1.backNum
          },

          {
            name: '历史遗留问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#AAA',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'diamond',
            symbolSize: 12,
            show: false,
            data: this.dataChart1.historyNum
          },
          {
            name: '数据问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#FFAA33',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'rect',
            symbolSize: 12,
            data: this.dataChart1.dataNum
          },

          {
            name: '打包问题',
            type: 'line',
            itemStyle: {
              normal: {
                color: '#00DD00',
                label: {
                  show: true,
                  position: 'right'
                }
              }
            },
            symbol: 'triangle',
            symbolSize: 12,
            data: this.dataChart1.packageNum
          }
        ]
      });
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
    handleSizeChange: function(size) {
      this.pagesize = size;
      this.getSpanArr();
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    //导出查询
    async exportMantis() {
      await validate(this.$refs['searchRulesChart']);
      this.useParam = this.getParam();
      await this.exportMantisStatement({ ...this.useParam }).catch(err => {
        throw new Error(err);
      });
      this.$message({
        type: 'success',
        message: '导出成功!'
      });
    }
  },
  async beforeMount() {
    this.colShow = new Array(20).fill(false);
    [0, 1, 2, 3, 4, 5, 6, 7, 20].forEach(item => {
      this.colShow[item] = true;
    });
    if (this.groupAll.length <= 1) {
      await this.queryAllGroup({ status: 1 });
    }
    this.groupAllOption = this.groupAll;
  }
};
</script>
<style scoped>
body {
  margin: 0;
}
.title {
  font-size: 20px;
  margin-bottom: 15px;
  text-align: left;
}
.bg-gray {
  background-color: #e0e0e0;
}
.bg-white {
  background-color: white;
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
.margin-left {
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
.winner-style .el-table td,
.winner-style .el-table th {
  padding: 6px 0;
}
.chart-main {
  padding: 0px;
  margin-top: 10px;
  overflow: hidden;
}
.chart-frame {
  padding-top: 10px;
  width: 100%;
  height: 400px;
}
.chart-frame-test {
  padding-top: 10px;
  width: 100%;
  height: 400px;
}

#devQuality {
  width: 100%;
  height: 50vh;
}
.relative {
  position: relative;
}
#divFlex {
  display: flex;
}

#divOne {
  margin-right: 15px;
}
.el-form-item {
  margin: 0 !important;
}
#form {
  margin-bottom: 20px;
  display: flex;
}
.item1 {
  float: right;
}
.headerD {
  height: auto !important;
}
.chart-frame-test {
  padding-top: 10px;
  width: 100%;
  height: 400px;
}
.bg-white {
  background-color: white;
}
</style>
