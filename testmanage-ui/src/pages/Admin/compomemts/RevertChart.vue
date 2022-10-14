<template>
  <div class="chart-container">
    <el-container class="chart-con">
      <el-header class="head">
        <el-form
          class="header flexDiv"
          :inline="true"
          :model="searchOrder"
          :rules="searchRules"
          ref="searchOrder"
        >
          <el-col class="mgr-5">
            <el-form-item prop="date">
              <el-date-picker
                v-model="searchOrder.date"
                value-format="yyyy-MM-dd"
                format="yyyy-MM-dd"
                :picker-options="pickerOptions"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              ></el-date-picker>
            </el-form-item>
          </el-col>

          <el-col>
            <el-form-item>
              <t-select
                prop="label"
                :options="fdevDevelopers"
                v-model="fdevUserName"
                filterable
                placeholder="请选择 fdev 开发人员"
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
          </el-col>
          <el-col>
            <el-form-item>
              <el-input
                v-model="taskName"
                placeholder="请输入任务名"
                clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col>
            <el-form-item>
              <t-select
                :options="revertOptions"
                v-model="backReason"
                placeholder="请选择打回原因"
                clearable
                filterable
                prop="label"
              />
            </el-form-item>
          </el-col>
          <el-col>
            <el-form-item>
              <t-select
                prop="label"
                :options="fdevGroups"
                v-model="groupId"
                filterable
                placeholder="请选择 fdev 组名"
                clearable
                option-label="name"
                option-value="id"
              >
                <template v-slot:options="item">
                  <span class="option-left">{{ item.name }}</span>
                </template>
              </t-select>
            </el-form-item>
          </el-col>
          <el-col>
            <el-form-item>
              <el-switch v-model="childGroupFlag" active-text="包含子组">
              </el-switch>
            </el-form-item>
          </el-col>
          <el-col>
            <el-form-item class="form-button widthSetBtn">
              <el-button
                type="primary"
                @click="search('searchOrder')"
                class="queryBtn"
                >查询</el-button
              >
              <el-button
                type="primary"
                @click="exportExcelRevert('searchOrder')"
                >导出报表</el-button
              >
            </el-form-item>
          </el-col>
        </el-form>
      </el-header>
      <el-main style="padding:10px" class="bg-white">
        <el-table
          v-loading="loading"
          :data="
            revertTable.slice(
              (currentPage - 1) * pagesize,
              currentPage * pagesize
            )
          "
          stripe
          :header-cell-style="{ color: '#545c64' }"
          :highlight-currunt-row="true"
        >
          <el-table-column prop="groupName" label="小组"></el-table-column>
          <el-table-column prop="mainTaskName" label="任务名"></el-table-column>
          <el-table-column
            prop="fstSitDate"
            label="首次提测时间"
          ></el-table-column>
          <el-table-column
            prop="fnlRollbackDate"
            label="最新打回时间"
          ></el-table-column>
          <el-table-column
            prop="rollbackNum"
            label="打回次数"
          ></el-table-column>
          <el-table-column prop="developer" label="开发人员"></el-table-column>
          <el-table-column prop="testers" label="测试人员"></el-table-column>
          <el-table-column type="expand" label="详情">
            <template slot-scope="props">
              <el-table
                :header-cell-style="{
                  color: '#545c64',
                  background: 'rgba(242,253,254,1)'
                }"
                :cell-style="{ background: 'rgba(242,253,254,1)' }"
                :data="props.row.detail"
                style="width: 100%"
                stripe
              >
                <el-table-column prop="date" label="打回日期" width="240" />
                <el-table-column
                  prop="rollbackOpr"
                  label="执行人"
                  width="240"
                />
                <el-table-column prop="reason" label="原因" />
                <el-table-column prop="detailInfo" label="详细说明">
                  <template slot-scope="scope">
                    <el-tooltip
                      effect="dark"
                      :content="scope.row.detailInfo"
                      placement="top-start"
                      v-if="scope.row.detailInfo"
                    >
                      <div class="ellipsis">
                        {{ scope.row.detailInfo }}
                      </div>
                    </el-tooltip>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
              </el-table>
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
            :total="revertTable.length"
          ></el-pagination>
        </div>
      </el-main>
    </el-container>
  </div>
</template>
<script>
import { queryFdevGroup, queryFdevUser } from '@/services/userchart';
import {
  formatGroup,
  formatOption,
  exportExcel,
  validate
} from '@/common/utlis';
import { revertOptions } from '@/pages/WorkOrder/model.js';
import { mapState, mapActions } from 'vuex';
import { rules, searchOrder } from '@/pages/Charts/model';
export default {
  name: 'RevertChart',
  data() {
    return {
      currentPage: 1,
      pagesize: 10,
      loading: false,
      childGroupFlag: false,
      taskName: '',
      groupId: '',
      backReason: '',
      fdevUserName: '',
      fdevDevelopers: [],
      fdevGroups: [],
      revertOptions,
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
      }
    };
  },
  computed: {
    ...mapState('chartsForm', ['revertTable', 'exportRollback']),
    searchRules() {
      return this.rules(this.searchOrder);
    }
  },
  methods: {
    ...mapActions('chartsForm', [
      'queryRollbackReport',
      'exportRollbackReport'
    ]),
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
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    async search(searchOrder) {
      await validate(this.$refs[searchOrder]);
      this.loading = true;
      const date = this.searchOrder.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      let childGroupFlag = '';
      if (this.groupId !== '') {
        if (this.childGroupFlag === false) {
          childGroupFlag = '0';
        }
        if (this.childGroupFlag === true) {
          childGroupFlag = '1';
        }
      } else {
        childGroupFlag = '0';
        this.childGroupFlag = false;
      }
      await this.queryRollbackReport({
        mainTaskName: this.taskName,
        groupId: this.groupId,
        reason: this.backReason.toString(),
        startDate,
        endDate,
        childGroupFlag,
        developer: this.fdevUserName
      });
      this.loading = false;
    },
    async exportExcelRevert(searchOrder) {
      await validate(this.$refs[searchOrder]);
      const date = this.searchOrder.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      let childGroupFlag = '';
      if (this.groupId !== '') {
        if (this.childGroupFlag === false) {
          childGroupFlag = '0';
        }
        if (this.childGroupFlag === true) {
          childGroupFlag = '1';
        }
      } else {
        childGroupFlag = '0';
        this.childGroupFlag = false;
      }
      await this.exportRollbackReport({
        mainTaskName: this.taskName,
        groupId: this.groupId,
        reason: this.backReason.toString(),
        startDate,
        endDate,
        childGroupFlag,
        developer: this.fdevUserName
      });
      exportExcel(this.exportRollback);
      this.$message({
        type: 'success',
        message: '全部导出成功!'
      });
    }
  },
  async mounted() {
    // 查询fdev小组
    let response = await queryFdevGroup();
    let groupsList = formatOption(
      response.map(group => formatGroup(group)),
      'name'
    );
    this.fdevGroups = groupsList.filter(item => item.status === '1');

    // 查询fdev开发人员
    this.fdevDevelopers = await queryFdevUser();
  }
};
</script>
<style scoped>
body {
  margin: 0;
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
  margin: 0 auto;
}
.header {
  margin-top: 15px;
  margin-left: 20px;
}
.head {
  height: 70px;
}
.mgr-5 {
  margin-right: 5px;
}
.form-button >>> .el-button {
  padding: 11px 9px;
}
.pagination {
  margin-top: 20px;
  padding: 20px 0;
  text-align: right;
}
.widthSetBtn {
  width: 120%;
}
.flexDiv {
  display: flex;
}
</style>
