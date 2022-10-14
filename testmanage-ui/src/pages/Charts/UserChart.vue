<template>
  <div class="chart-container">
    <el-container class="chart-con">
      <strong class="title">测试人员工作量统计表</strong>
      <el-header class="headerUser">
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
              :picker-options="pickerOptions"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <t-select
              v-model="groupId"
              filterable
              clearable
              placeholder="请选择组名"
              :options="groupLists"
              prop="label"
              option-label="label"
              option-value="value"
            />
          </el-form-item>
          <el-form-item>
            <t-select
              prop="label"
              :options="userOptions"
              v-model="user"
              filterable
              placeholder="请选择用户"
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
            <el-switch v-model="childGroupFlag" active-text="包含子组">
            </el-switch>
          </el-form-item>
          <el-form-item class="shrinkItem">
            <el-button
              type="primary"
              @click="search('searchOrder')"
              class="queryBtn"
              >查询</el-button
            >
            <el-button
              type="primary"
              @click="exportExcelUserList('searchOrder')"
              :loading="exportlAllUserLoading"
              :disabled="exportlAllUserLoading"
              >导出报表</el-button
            >
            <el-button
              type="primary"
              @click="exportUserWorkDetail('searchOrder')"
              :loading="exportlUserLoading"
              :disabled="exportlUserLoading"
              >导出个人日报</el-button
            >
          </el-form-item>
        </el-form>
      </el-header>
      <el-main style="padding:10px" class="bg-white">
        <div v-if="showRqrFlag">
          <div>{{ groupFilter() }} 小组工单信息概览</div>
          <ul class="table-tip">
            <li>
              <span class="span-label">当前日期内测完成工单:</span
              >{{ groupOrder.dayOrderDone }}
            </li>
            <li>
              <span class="span-label">当前日期提交内测工单:</span
              >{{ groupOrder.dayOrderSubmit }}
            </li>
            <li>
              <span class="span-label">组内待启动工单:</span
              >{{ groupOrder.noPlanOrder }}
            </li>
            <li>
              <span class="span-label">组内待分配工单:</span>
              {{ groupOrder.noTesterOrder }}
            </li>
          </ul>
          <hr />
        </div>
        <el-table
          border
          :stripe="true"
          @expand-change="getDetail"
          :expand-row-keys="expands"
          :row-key="getRowKey"
          :lazy="true"
          v-loading="loading"
          :data="
            userChartTable.slice(
              (currentPage - 1) * pagesize,
              currentPage * pagesize
            )
          "
          style="color:black; width: 100%"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column prop="user_name_en" label="用户名" />
          <el-table-column prop="user_name" label="姓名" />
          <el-table-column prop="group_name" label="小组" />
          <el-table-column prop="add_num" sortable label="编写案例数" />
          <el-table-column prop="exe_num" sortable label="案例执行数" />
          <el-table-column prop="delete_num" sortable label="当前删除案例数" />
          <el-table-column prop="modify_num" sortable label="当前修改案例数" />
          <el-table-column prop="block_num" sortable label="案例执行阻塞数" />
          <el-table-column prop="fail_num" sortable label="案例执行失败数" />
          <el-table-column prop="issue_num" sortable label="有效缺陷总数" />
          <el-table-column label="工单详情" type="expand">
            <el-table
              border
              :stripe="true"
              :data="rowData"
              v-loading="detailLoading"
              @cell-click="openTaskList"
              :cell-style="cellStyle"
            >
              <el-table-column
                width="120"
                style="color:red"
                prop="workNo"
                label="工单号"
              />
              <el-table-column prop="mainTaskName" label="主任务名">
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
              <el-table-column prop="taskCount" label="承接提测任务数" />
              <el-table-column prop="dayCreate" label="当期新增案例数" />
              <el-table-column prop="dayExe" label="当期案例执行数" />
              <el-table-column prop="dayDelete" label="当期删除案例数" />
              <el-table-column prop="dayUpdate" label="当期案例修改数" />
              <el-table-column prop="dayBlock" label="当期执行阻塞数" />
              <el-table-column prop="dayFail" label="当期执行失败数" />
              <el-table-column
                prop="unSolveIssue"
                label="当期未解决缺陷数（有效）"
              />
              <el-table-column
                prop="dayIssueNum"
                label="当期新增缺陷数（有效）"
              />
            </el-table>
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
            :total="userChartTable.length"
          ></el-pagination>
        </div>
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
import { countUserTestCaseByTime } from '@/services/userchart';

import { rules, searchOrder } from './model';
import { validate, exportExcel, getUserListByRole } from '@/common/utlis';
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
      showCallBack: false,
      isOtherGroup: '',
      loading: false,
      searchOrder: searchOrder(),
      winnerListShow: false,
      winnerListTable: [],
      userChartTable: [],
      startTime: '',
      endTime: '',
      endDate: '',
      startDate: '',
      start: '',
      currentPage: 1,
      pagesize: 10,
      groupId: '',
      groupNames: [],
      groupLists: [],
      pieSelects: [],
      testcase_sum: '',
      execute_testcase_sum: '',
      add_testcase_sum: '',
      unexecute_testcase_sum: '',
      all_issue_num: '',
      searchGroupName: '',
      pieGroupNames: '',
      pieSelect: ['执行案例总数', '编写案例总数', '有效缺陷总数'],
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
      userList: [],
      userOptions: [],
      allUserList: [],
      user: '',
      expands: [],
      getRowKey: row => row.user_name_en,
      rowData: [],
      showRqrFlag: false,
      detailLoading: false,
      exportlUserLoading: false,
      exportlAllUserLoading: false,
      childGroupFlag: false
    };
  },
  filters: {
    filterProcess(val) {
      return Number(val * 100).toFixed(0) + '%';
    }
  },
  watch: {
    showRqrFlag(val) {
      if (val) {
        this.groupFilter();
      }
    },
    user(val) {
      if (!val) {
        this.userOptions = this.userList;
      }
    },
    groupId(val) {
      this.user = '';
      if (!val) {
        this.userOptions = this.allUserList;
        return;
      }
      this.userList = this.allUserList.filter(user => {
        return user.group_id === val;
      });
      this.userOptions = this.userList;
    }
  },

  computed: {
    ...mapState('chartsForm', [
      'groupList',
      'rowDatas',
      'groupOrder',
      'countUserTestCaseChart',
      'exportPersonal',
      'exportExcelList'
    ]),
    ...mapState('workOrderForm', ['fdevTaskByWorkNo']),
    searchRules() {
      return this.rules(this.searchOrder);
    }
  },
  methods: {
    ...mapActions('chartsForm', [
      'queryGroupName',
      'queryOrderInfoByUser',
      'queryGroupInfo',
      'countCaseTestByGroup',
      'exportPersonalDimensionReport',
      'exportExcelUser'
    ]),
    ...mapActions('workOrderForm', ['queryFdevTaskByWorkNo']),
    async openTaskList(item, column) {
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
    handleSizeChange: function(size) {
      this.pagesize = size;
    },
    handleCurrentChange: function(currentPage) {
      this.currentPage = currentPage;
    },
    remove(array, x) {
      array.splice(array.indexOf(x), 1);
    },
    groupFilter() {
      let groupId = '';
      if (this.groupId) {
        groupId = this.groupId;
      } else {
        if (this.user) {
          let checkedUser = this.allUserList.find(user => {
            return user.user_name_en === this.user;
          });
          groupId = checkedUser.group_id;
        } else {
          return;
        }
      }
      let group = this.groupLists.filter(group => {
        return group.value === groupId;
      });
      if (group[0]) {
        return group[0].label;
      }
    },
    async getDetail(row, expandedRow) {
      let { user_name_en } = row;
      if (expandedRow.length > 0) {
        this.expands = [user_name_en];
      } else {
        this.expands = [];
        return;
      }
      const date = this.searchOrder.date;
      let params = {
        user_en_name: user_name_en,
        startDate: date[0],
        endDate: date[1]
      };
      this.detailLoading = true;
      await this.queryOrderInfoByUser(params);
      this.rowData = this.rowDatas;
      this.detailLoading = false;
    },
    async search(searchOrder) {
      await validate(this.$refs[searchOrder]);
      this.loading = true;
      // 关闭清空二级表格数据
      this.expands = [];
      this.rowData = [];
      const date = this.searchOrder.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      let groupId = '';
      if (!this.groupId && this.user) {
        let checkedUser = this.allUserList.find(user => {
          return user.user_name_en === this.user;
        });
        groupId = checkedUser.groupId;
      }
      let params = {
        startDate: startDate,
        endDate: endDate,
        groupId: this.groupId ? this.groupId : groupId,
        username: this.user,
        isParent: this.childGroupFlag
      };

      return countUserTestCaseByTime(params).then(res => {
        let arr = [];
        arr = res;
        this.userChartTable = arr;
        this.currentPage = 1;
        this.loading = false;
        return arr;
      });
    },

    //初始化下拉选项
    async groupNameData() {
      await this.queryGroupName();
      let arr = [];
      let len = '';
      let i = '';
      for (i = 0, len = this.groupList.length; i < len; i++) {
        arr.push(this.groupList[i]);
      }
      this.searchGroupName = this.pieSelect[0];
      this.groupLists = arr.map(item => {
        return { value: item.id, label: item.fullName };
      });
      this.pieSelects = this.pieSelect.map(item => {
        return { value: item, label: item };
      });
    },
    userStatus(row, column) {
      if (row.status === '0') {
        return '是';
      } else {
        return '否';
      }
    },
    //全部导出
    async exportExcelUserList(searchOrder) {
      await validate(this.$refs[searchOrder]);
      const date = this.searchOrder.date;
      this.exportlAllUserLoading = true;
      await this.exportExcelUser({
        startDate: date[0],
        endDate: date[1],
        groupId: this.groupId,
        username: this.user,
        isParent: this.childGroupFlag
      }).catch(e => {
        this.exportlAllUserLoading = false;
      });
      exportExcel(this.exportExcelList);

      this.$message({
        type: 'success',
        message: '全部导出成功!'
      });
      this.exportlAllUserLoading = false;
    },
    async exportUserWorkDetail(searchOrder) {
      if (!this.groupId && !this.user) {
        this.$message({
          type: 'error',
          message: '请选择小组或者用户!'
        });
        return;
      }
      await validate(this.$refs[searchOrder]);
      this.exportlUserLoading = true;
      const date = this.searchOrder.date;
      let groupId = '';
      if (!this.groupId) {
        let checkedUser = this.allUserList.find(user => {
          return user.user_name_en === this.user;
        });
        groupId = checkedUser.groupId;
      }
      await this.exportPersonalDimensionReport({
        startDate: date[0],
        endDate: date[1],
        groupId: this.groupId ? this.groupId : groupId,
        user_en_name: this.user,
        isParent: this.childGroupFlag
      }).catch(e => {
        this.exportlUserLoading = false;
      });
      exportExcel(this.exportPersonal);
      this.$message({
        type: 'success',
        message: '全部导出成功!'
      });
      this.exportlUserLoading = false;
    }
  },
  async mounted() {
    await this.groupNameData();
    getUserListByRole(['测试人员', '玉衡-测试管理员', '玉衡-测试组长']).then(
      res => {
        this.userOptions = res;
        this.allUserList = res;
        this.userList = res;
      }
    );
  }
};
</script>
<style scoped>
body {
  margin: 0;
}
.title {
  font-size: 20px;
  margin-left: 10px;
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
  width: 100%;
  /* height:400px; */
}
.chart-frame-test {
  padding-top: 10px;
  width: 100%;
  /* height:1000px; */
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
  margin-left: 10px;
  display: flex;
}
.pie-frame {
  padding-top: 10px;
  width: 100%;
  height: 350px;
}
.pie-search {
  height: 50px;
  text-align: right;
  padding: 10px;
}
.table-tip > li {
  width: 20%;
  display: inline-block;
}
.el-table__body >>> .el-table__expanded-cell {
  background-color: #e0e0e0 !important;
}
.span-label {
  color: #676767;
  padding-right: 15px;
}
.headerUser {
  height: auto !important;
}
.shrinkItem {
  flex-shrink: 0;
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
