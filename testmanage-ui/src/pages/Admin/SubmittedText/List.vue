<template>
  <div class="page-container">
    <el-tabs v-model="tab" type="card" @tab-click="handleClick">
      <el-tab-pane label="提测信息" name="List"></el-tab-pane>
      <el-tab-pane label="提测打回报表" name="RevertChart"></el-tab-pane>
    </el-tabs>
    <div v-show="showList">
      <div class="form">
        <el-form
          :inline="true"
          class="demo-form-inline"
          :model="searchOrder"
          ref="searchOrder"
        >
          <el-form-item>
            <el-input
              class="input-order-name"
              v-model="workNo"
              placeholder="需求名称/需求编号/实施单元/任务名/工单名"
              clearable
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-select
              v-model="groupId"
              placeholder="请选择提测小组"
              :filter-method="filterMethod"
              filterable
              multiple
              clearable
              collapse-tags
            >
              <el-option
                v-for="item in groupAllOption"
                :key="item.value"
                :label="
                  item.fullName.length > 6
                    ? item.fullName.substring(0, 6) + '...'
                    : item.fullName
                "
                :value="item.id"
              >
                <span> {{ item.fullName }} </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select
              v-model="orderGroupId"
              placeholder="请选择工单小组"
              :filter-method="filterOrderGroupIdMethod"
              filterable
              multiple
              clearable
              collapse-tags
            >
              <el-option
                v-for="item in groupAllOrderGroupId"
                :key="item.value"
                :label="
                  item.fullName.length > 6
                    ? item.fullName.substring(0, 6) + '...'
                    : item.fullName
                "
                :value="item.id"
              >
                <span> {{ item.fullName }} </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select
              v-model="tester"
              placeholder="请选择测试人员"
              @keyup.enter.native="query()"
              filterable
              clearable
            >
              <el-option
                v-for="(item, index) in userList"
                :key="index"
                :value="item.user_name_en"
                :label="item.user_name_cn"
              >
                <span style="float: left">{{ item.user_name_cn }}</span>
                <span style="float: right">{{ item.user_name_en }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
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
          </el-form-item>
          <el-form-item>
            <el-select
              class="select"
              v-model="stage"
              placeholder="请选择工单状态"
              clearable
            >
              <el-option
                v-for="(item, index) in doneList"
                :key="index"
                :value="item.stage"
                :label="item.stageCnName"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select
              class="select"
              v-model="orderType"
              placeholder="请选择工单类型"
            >
              <el-option
                v-for="item in orderTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-switch v-model="isIncludeChildren" active-text="包含子组">
            </el-switch>
          </el-form-item>
          <el-form-item>
            <el-button @click="query" type="primary">
              查询
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="taskName" label="工单名称">
          <template slot-scope="scope">
            <el-tooltip
              class="item"
              effect="dark"
              :content="scope.row.taskName"
              placement="bottom-start"
            >
              <el-link
                type="primary"
                @click="linkTo(scope.row.id)"
                class="td-width"
              >
                {{ scope.row.taskName }}
              </el-link>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="fdevTaskName" label="提测任务" />
        <el-table-column prop="groupName" label="提测小组" />
        <el-table-column prop="orderGroupName" label="工单小组" />
        <el-table-column prop="rqrNo" label="需求编号/需求名称" width="230">
          <template slot-scope="scope">
            <el-tooltip
              class="item"
              effect="dark"
              :disabled="!scope.row.rqrNo"
              :content="scope.row.rqrNo"
              placement="bottom-start"
            >
              <span class="td-width width">{{ scope.row.rqrNo }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="stage"
          label="工单状态"
          :formatter="formatStatus"
        />
        <el-table-column prop="orderType" label="工单类型" width="130">
          <template slot-scope="scope">
            {{ showOrderType(scope.row.orderType) }}
          </template>
        </el-table-column>
        <el-table-column prop="testers" label="测试人员" />
        <el-table-column prop="developer" label="开发人员" />
        <el-table-column prop="testReason" label="测试原因" />
        <el-table-column prop="createTime" label="提测日期" width="150" />
      </el-table>

      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.current_page"
          :page-sizes="[5, 10, 15, 20, 25, 50, 100]"
          :page-size="pagination.page_size"
          layout="sizes, prev, pager, next, jumper"
          :total="pagination.total"
        />
      </div>
    </div>
    <RevertChart v-show="!showList" />
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import RevertChart from '../compomemts/RevertChart';
import { getUserListByRole } from '@/common/utlis';
import { orderTypeList } from '../../WorkOrder/model';
export default {
  name: 'SubmittedText',
  components: { RevertChart },
  data() {
    return {
      groupAllOption: [],
      groupAllOrderGroupId: [],
      isIncludeChildren: false, //默认不查子组
      orderTypeList: orderTypeList,
      orderType: 'function',
      doneList: [
        { stage: '0', stageCnName: '待分配' },
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
      stage: '',
      workNo: '',
      groupId: [],
      orderGroupId: [],
      testers: '',
      searchOrder: {
        date: ''
      },
      tester: '',
      tableData: [],
      loading: false,
      pagination: {
        current_page: 1,
        page_size: 10,
        total: 0
      },
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
      showList: true,
      userList: [],
      tab: 'List'
    };
  },
  computed: {
    ...mapState('adminForm', ['groupAll', 'sitMsgList', 'sitMsgCount'])
  },
  methods: {
    ...mapActions('adminForm', [
      'queryAllUserName',
      'queryAllGroup',
      'querySitMsgList',
      'countSitMsgList'
    ]),
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
    filterOrderGroupIdMethod(data) {
      if (data) {
        this.groupAllOrderGroupId = this.groupAll.filter(item => {
          if (
            item.fullName.indexOf(data) > -1 ||
            item.fullName.toLowerCase().indexOf(data.toLowerCase()) > -1
          ) {
            return true;
          }
        });
      } else {
        this.groupAllOrderGroupId = this.groupAll;
      }
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
    formatStatus(row, column) {
      if (row.stage === '0') {
        return '待分配';
      } else if (row.stage === '1') {
        return '开发中';
      } else if (row.stage === '2') {
        return 'SIT';
      } else if (row.stage === '3') {
        return 'UAT';
      } else if (row.stage === '4') {
        return '已投产';
      } else if (row.stage === '6') {
        return 'UAT(含风险)';
      } else if (row.stage === '8') {
        return '撤销';
      } else if (row.stage === '9') {
        return '分包测试（内测完成）';
      } else if (row.stage === '10') {
        return '分包测试（含风险）';
      } else if (row.stage === '11') {
        return '已废弃';
      } else if (row.stage === '12') {
        return '安全测试(内测完成)';
      } else if (row.stage === '13') {
        return '安全测试(含风险)';
      } else if (row.stage === '14') {
        return '安全测试(不涉及)';
      } else {
      }
    },
    handleSizeChange(size) {
      this.pagination.page_size = size;
      this.init();
    },
    handleCurrentChange(size) {
      this.pagination.current_page = size;
      this.init();
    },
    linkTo(id) {
      this.$router.push(`/sitMsg/${id}`);
    },
    async init() {
      const date = this.searchOrder.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      const params = {
        currentPage: this.pagination.current_page,
        pageSize: this.pagination.page_size,
        workNo: this.workNo,
        // 新增的搜索条件
        groupId: this.groupId,
        orderGroupId: this.orderGroupId,
        tester: this.tester,
        startDate: startDate,
        endDate: endDate,
        stage: this.stage,
        isIncludeChildren: this.isIncludeChildren
      };
      this.loading = true;
      Promise.all([this.countSitMsgList(params), this.querySitMsgList(params)])
        .then(() => {
          this.pagination.total = this.sitMsgCount / 1;
          this.tableData = this.sitMsgList;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    // 查询
    async query() {
      const date = this.searchOrder.date;
      let startDate = '';
      let endDate = '';
      if (date) {
        startDate = date[0];
        endDate = date[1];
      }
      const params = {
        currentPage: 1,
        pageSize: this.pagination.page_size,
        workNo: this.workNo,
        // 新增的搜索条件
        groupId: this.groupId,
        orderGroupId: this.orderGroupId,
        tester: this.tester,
        startDate: startDate,
        endDate: endDate,
        stage: this.stage,
        orderType: this.orderType,
        isIncludeChildren: this.isIncludeChildren
      };
      this.loading = true;
      Promise.all([this.countSitMsgList(params), this.querySitMsgList(params)])
        .then(() => {
          this.pagination.total = this.sitMsgCount / 1;
          this.tableData = this.sitMsgList;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleClick(item) {
      if (item.name === 'List') {
        this.showList = true;
      } else {
        this.showList = false;
      }
    }
  },
  async mounted() {
    this.init();
    await this.queryAllUserName();
    this.userList = await getUserListByRole([
      '测试人员',
      '玉衡-测试管理员',
      '玉衡超级管理员'
    ]);
    if (this.groupAll.length <= 1) {
      await this.queryAllGroup({ status: 1 });
    }
    this.groupAllOption = this.groupAll;
    this.groupAllOrderGroupId = this.groupAll;
  },

  created() {
    if (sessionStorage.getItem('searchModel')) {
      const { workNo, groupId, orderGroupId, tester, date, stage } = JSON.parse(
        sessionStorage.getItem('searchModel')
      );
      this.workNo = workNo;
      this.groupId = groupId;
      this.orderGroupId = orderGroupId;
      this.tester = tester;
      this.searchOrder.date = date;
      this.stage = stage;
    }
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('searchModel');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem(
      'searchModel',
      JSON.stringify({
        workNo: this.workNo,
        groupId: this.groupId,
        orderGroupId: this.orderGroupId,
        tester: this.tester,
        date: this.searchOrder.date,
        stage: this.stage
      })
    );
    next();
  }
};
</script>

<style scoped>
.table {
  width: 100%;
}
.pagination {
  text-align: right;
  margin-top: 15px;
}
.table {
  width: 550px;
  border-radius: 5px;
  border-collapse: collapse;
  border: 1px solid #dcdfe6;
  box-sizing: border-box;
}
.table td {
  padding: 10px 10px;
  text-align: left;
  border: 1px solid #dcdfe6;
}
.table tr td:nth-of-type(2n-1) {
  width: 30%;
  font-weight: 600;
  color: black;
  background: #f2f6fc;
}
.form {
  width: 100%;
  display: flex;
  margin-bottom: 20px;
}
.td-width {
  overflow: hidden;
  text-overflow: ellipsis;
  word-wrap: none;
  display: inline-block;
  white-space: nowrap;
}
.width {
  width: 100%;
}
</style>
