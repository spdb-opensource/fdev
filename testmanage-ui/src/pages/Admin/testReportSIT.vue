<template>
  <div class="page-container">
    <el-form :inline="true" class="demo-form-inline" label-width="120px">
      <el-col :span="6">
        <el-form-item class="task-style">
          <el-input
            v-model="taskName"
            placeholder="需求名称/需求编号/实施单元/任务名/工单名"
            clearable
            @keyup.enter.native="queryBtn()"
          ></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="4">
        <el-form-item>
          <el-input
            v-model="workNo"
            placeholder="请输入工单编号"
            clearable
            @keyup.enter.native="queryBtn()"
          ></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="4">
        <el-form-item>
          <el-select v-model="orderType" placeholder="工单类型">
            <el-option
              v-for="item in orderTypeList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="4">
        <el-form-item>
          <el-select
            v-model="tester"
            placeholder="请选择内测人员"
            @keyup.enter.native="queryBtn()"
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
      </el-col>
      <el-col :span="4">
        <el-form-item>
          <el-select
            v-model="done"
            placeholder="请选择是否显示归档"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <el-option
              v-for="(item, index) in doneList"
              :key="index"
              :value="item.value"
              :label="item.label"
            >
            </el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="2">
        <el-form-item>
          <el-button type="primary" @click="queryBtn()">查询</el-button>
        </el-form-item>
      </el-col>
    </el-form>

    <el-table
      v-loading="tableLoading"
      stripe
      :data="sitOrderData"
      @sort-change="changeTableSort"
      :default-sort="{ prop: 'data', order: '' }"
      tooltip-effect="dark"
      style="width: 100%;color:black"
      :header-cell-style="{ color: '#545c64' }"
    >
      <el-table-column prop="workOrderNo" label="工单编号" width="170">
        <template slot-scope="scope">
          <el-tooltip
            class="item"
            effect="dark"
            :content="scope.row.workOrderNo"
            placement="bottom-start"
          >
            <el-link
              class="td-width"
              type="primary"
              :underline="false"
              @click="goWorkOrderDetail(scope.row)"
              >{{ scope.row.workOrderNo }}</el-link
            >
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="mainTaskName" label="工单名称" width="130">
        <template slot-scope="scope">
          <el-tooltip
            class="item"
            effect="dark"
            :content="scope.row.mainTaskName"
            placement="bottom-start"
          >
            <span class="td-width">{{ scope.row.mainTaskName }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="orderType" label="工单类型" width="130">
        <template slot-scope="scope">
          {{ showOrderType(scope.row.orderType) }}
        </template>
      </el-table-column>
      <el-table-column prop="demandNo" label="需求编号">
        <template slot-scope="scope">
          <el-tooltip
            class="item"
            effect="dark"
            :content="scope.row.demandNo"
            placement="bottom-start"
          >
            <span class="td-width">{{ scope.row.demandNo }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="demandNo" label="需求名称">
        <template slot-scope="scope">
          <el-tooltip
            class="item"
            effect="dark"
            :content="scope.row.demandName"
            placement="bottom-start"
          >
            <span class="td-width">{{ scope.row.demandName }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column
        prop="workManager.user_name_cn"
        label="工单负责人"
        width="130"
        sortable="custom"
      ></el-table-column>
      <el-table-column prop="groupLeader" label="测试小组长">
        <template slot-scope="scope">
          <span v-for="(user, index) in scope.row.groupLeader" :key="index">
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="testers" label="测试人员">
        <template slot-scope="scope">
          <span v-for="(user, index) in scope.row.testers" :key="index">
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="remark" label="备注" width="120"></el-table-column>
      <el-table-column label="操作" width="80">
        <template slot-scope="scope" v-if="hasManager(scope.row)">
          <el-tooltip class="item" effect="dark" content="打印" placement="top">
            <i
              class="el-icon-printer"
              @click="printReport(scope.$index, scope.row)"
            />
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            v-if="scope.row.orderType == 'function'"
            :content="
              scope.row.sitFlag === '0'
                ? '该任务未走fdev提测，不支持上线提交业测'
                : '提交业测'
            "
            placement="top"
          >
            <i
              :class="[
                { opacityThre: scope.row.sitFlag === '0' },
                'el-icon-message'
              ]"
              @click="openMailDetails(scope.$index, scope.row)"
              >&nbsp;</i
            >
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <!-- 邮件详情编辑页 -->
    <Dialog
      title="邮件发送"
      :visible.sync="mailDetails"
      width="40%"
      class="abow_dialog"
    >
      <div>
        <el-form
          :inline="true"
          :model="mailInfo"
          label-width="200px"
          size="mini"
        >
          <el-row>
            <el-col>
              <el-form-item label="主任务名 ：">
                <el-input
                  v-model="mailInfo.mainTaskName"
                  autocomplete="off"
                  suffix-icon="xxx"
                  class="detailsDialog"
                  disabled
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-form-item label="需求编号/实施单元	：">
              <el-input
                v-model="mailInfo.unit"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
                disabled
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="是否涉及接口变动 ：">
              <el-input
                v-model="mailInfo.interfaceChange"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="是否有关联系统同步改造 ：">
              <el-input
                v-model="mailInfo.otherSystemChange"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="是否涉及数据库变动	：">
              <el-input
                v-model="mailInfo.databaseChange"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="是否涉及客户端更新	：">
              <el-input
                v-model="mailInfo.clientVersion"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="所属应用名称 ：">
              <el-input
                v-model="mailInfo.appName"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="计划投产日期	：">
              <el-date-picker
                class="el-select-temp"
                v-model="mailInfo.planProDate"
                value-format="yyyy/MM/dd"
                type="date"
              ></el-date-picker>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item style="width:100%" label="测试内容	：">
              <el-input
                v-model="mailInfo.repair_desc"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
                type="textarea"
                :rows="4"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="回归测试范围	：">
              <el-input
                v-model="mailInfo.regressionTestScope"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="开发人员	：">
              <el-input
                v-model="mailInfo.developer"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="测试环境	：">
              <el-input
                v-model="mailInfo.testEnv"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
                type="textarea"
                :rows="4"
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item style="width:100%" label="备注	：">
              <el-input
                v-model="uatRemark"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
                type="textarea"
                :rows="4"
              ></el-input>
            </el-form-item>
          </el-row>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="showLoading" @click="submitTest"
          >发 送</el-button
        >
        <el-button type="primary" @click="mailDetails = false">关 闭</el-button>
      </div>
    </Dialog>
    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[5, 10, 50, 100, 200, 500]"
        :page-size="pageSize"
        layout="sizes, prev, pager, next, jumper"
        :total="total"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { sendStartUatMail } from '@/services/TestCaseExecute';
import { getUserListByRole } from '@/common/utlis';
import { orderTypeList } from '../WorkOrder/model';
export default {
  name: 'testReportSIT',
  data() {
    return {
      orderTypeList: orderTypeList,
      orderType: 'function',
      tableLoading: false,
      taskName: '',
      workNo: '',
      tester: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      user_en_name: '',
      done: '0',
      sortManager: '',
      doneList: [
        { label: '全部', value: '' },
        { label: '工单已归档', value: '1' },
        { label: '工单未归档', value: '0' }
      ],
      mailDetails: false,
      mainTaskNo: '',
      uatRemark: '',
      mainWorkNo: '',
      showLoading: false,
      userList: []
    };
  },
  computed: {
    ...mapState('adminForm', [
      'sitOrderCount',
      'sitOrderData',
      'sitReport',
      'mailInfo'
    ])
  },
  methods: {
    ...mapActions('adminForm', [
      'queryUpSitOrderCount',
      'queryUpSitOrder',
      'exportSitReportData',
      'queryAllUserName',
      'sendStartUatMail',
      'queryUatMailInfo'
    ]),
    showOrderType(val) {
      if (val == 'function') {
        return '功能测试';
      } else if (val == 'security') {
        return '安全测试';
      } else {
        return '-';
      }
    },
    // 点击工单号跳转
    goWorkOrderDetail(row) {
      const mainTaskName = row.mainTaskName;
      this.$router.push({
        name: 'QueryOrder',
        query: { mainTaskName: mainTaskName }
      });
    },
    handleSizeChange: function(size) {
      this.pageSize = size;
      this.queryListAll(this.pageSize, this.currentPage, this.taskName);
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.queryListAll(this.pageSize, val, this.taskName);
    },
    async submitTest() {
      this.showLoading = true;
      let params = {
        mainTaskNo: this.mainTaskNo,
        workNo: this.mainWorkNo,
        uatRemark: this.uatRemark,
        ...this.mailInfo
      };
      sendStartUatMail(params)
        .then(res => {
          this.showLoading = false;
          this.$message({
            type: 'success',
            message: '提交业测成功!'
          });
          this.queryUpSitOrder({
            pageSize: this.pageSize,
            currentPage: this.currentPage,
            taskName: this.taskName,
            userEnName: this.tester,
            done: this.done,
            sortManager: this.sortManager,
            orderType: this.orderType
          });
          this.mailDetails = false;
        })
        .catch(err => {
          this.showLoading = false;
          this.mailDetails = false;
        });
    },
    async openMailDetails(index, row) {
      if (row.sitFlag === '0') return;
      this.mainTaskNo = row.mainTaskNo;
      this.mainWorkNo = row.workOrderNo;
      let param = { mainTaskNo: row.mainTaskNo, workNo: row.workOrderNo };
      await this.queryUatMailInfo(param);
      this.mailInfo.planProDate = this.mailInfo.planProDate.replace(/-/g, '/');
      this.uatRemark = '';
      this.mailDetails = true;
    },
    async printReport(index, row) {
      let i = '';
      let testersArr = [];
      let testersStr = '';
      let testersEnArr = [];
      let testersEnStr = '';
      const testers = row.testers;
      for (i = 0; i < testers.length; i++) {
        testersArr.push(testers[i].user_name_cn);
        testersEnArr.push(testers[i].user_name_en);
      }
      testersStr = testersArr.toString();
      testersEnStr = testersEnArr.toString();

      let groupLeaderArr = [];
      let groupLeaderStr = '';
      let groupLeaderEnArr = [];
      let groupLeaderEnStr = '';
      const groupLeader = row.groupLeader;
      for (i = 0; i < groupLeader.length; i++) {
        groupLeaderArr.push(groupLeader[i].user_name_cn);
        groupLeaderEnArr.push(groupLeader[i].user_name_en);
      }
      groupLeaderStr = groupLeaderArr.toString();
      groupLeaderEnStr = groupLeaderEnArr.toString();
      // 新增三个入参 workManagerEn groupLeaderEn testersEn
      let params = {
        workNo: row.workOrderNo,
        mainTaskName: row.mainTaskName,
        mainTaskNo: row.mainTaskNo,
        unitNo: row.unit,
        groupLeader: groupLeaderStr,
        testers: testersStr,
        workManagerEn: row.workManager.user_name_en,
        groupLeaderEn: groupLeaderEnStr,
        testersEn: testersEnStr
      };
      await this.exportSitReportData(params);
      const sitReportData = {
        workOrderNo: row.workOrderNo,
        mainTaskName: row.mainTaskName,
        mainTaskNo: row.mainTaskNo,
        unitNo: row.unit,
        groupLeader: groupLeaderStr,
        testers: testersStr,
        res: this.sitReport
      };
      sessionStorage.setItem('sitReportDataNo', JSON.stringify(sitReportData));
      sessionStorage.setItem('sitReportParams', JSON.stringify(params));
      this.$router.push({
        name: 'PrintContent',
        query: {
          workOrderNo: row.workOrderNo
        }
      });
    },
    hasManager(row) {
      // workManager obj  groupLeader,testers Array
      let { workManager, groupLeader, testers } = row;
      let rembers = [workManager, ...groupLeader, ...testers];
      return rembers.some(rember => {
        if (!rember) {
          return false;
        }
        return rember.user_name_en === this.user_en_name;
      });
    },
    async queryListAll(
      pageSize,
      currentPage,
      taskName,
      sortManager,
      orderType
    ) {
      await this.queryUpSitOrder({
        pageSize: pageSize,
        currentPage: currentPage,
        taskName: taskName,
        workOrderNo: this.workNo,
        userEnName: this.tester,
        done: this.done,
        sortManager: this.sortManager,
        orderType: this.orderType
      });
    },
    // 查询所有数据
    async queryCountUser(taskName) {
      await this.queryUpSitOrderCount({
        taskName: taskName,
        workOrderNo: this.workNo,
        userEnName: this.tester,
        done: this.done,
        sortManager: this.sortManager,
        orderType: this.orderType
      });
      this.total = this.sitOrderCount.total;
    },

    //页面查询功能
    async queryBtn() {
      this.tableLoading = true;
      this.currentPage = 1;
      this.sortManager = '';
      await this.queryListAll(
        this.pageSize,
        this.currentPage,
        this.taskName,
        this.sortManager
      );
      await this.queryCountUser(this.taskName);
      this.tableLoading = false;
    },
    // 表格按字段排序  后端实现
    async changeTableSort(column) {
      this.sortManager = column.order;
      if (this.sortManager === 'descending') {
        this.sortManager = '0';
        this.currentPage = 1;
        this.queryListAll(
          this.pageSize,
          this.currentPage,
          this.taskName,
          this.sortManager,
          this.orderType
        );
        this.queryCountUser(this.taskName);
      } else {
        this.sortManager = '1';
        this.currentPage = 1;
        this.queryListAll(
          this.pageSize,
          this.currentPage,
          this.taskName,
          this.sortManager,
          this.orderType
        );
        this.queryCountUser(this.taskName);
      }
    }
  },

  async mounted() {
    this.user_en_name = sessionStorage.getItem('user_en_name');
    this.tester = this.user_en_name;
    this.userList = await getUserListByRole(['测试人员']);
    this.tableLoading = true;
    await this.queryListAll(this.pageSize, this.currentPage, this.taskName);
    await this.queryCountUser(this.taskName);
    // 查询内测人员
    await this.queryAllUserName();
    this.tableLoading = false;
  }
};
</script>

<style scoped>
.pagination {
  margin-top: 3%;
}
.pagination >>> .el-pagination {
  float: right !important;
}
.task-style >>> .el-form-item__content {
  width: 146%;
}
.opacityThre {
  opacity: 0.4;
}
.td-width {
  overflow: hidden;
  text-overflow: ellipsis;
  word-wrap: none;
  display: inline-block;
  white-space: nowrap;
}
</style>
