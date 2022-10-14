<template>
  <div class="page-container">
    <orderMenuTab />
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item>
        <el-input
          v-model="taskName"
          class="input-style input-order-name"
          placeholder="需求名称/需求编号/实施单元/任务名/工单名"
          clearable
          @keyup.enter.native="onSubmit"
        ></el-input>
      </el-form-item>
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
      <el-form-item>
        <el-button type="primary" @click="onSubmit">查询</el-button>
      </el-form-item>
    </el-form>
    <el-main style="padding:10px" class="bg-white">
      <el-table
        :data="aduitOrder"
        style="color:black"
        :header-cell-style="{ color: '#545c64' }"
      >
        <el-table-column prop="workOrderNo" label="工单编号">
          <template slot-scope="scope">
            <el-link
              type="primary"
              :underline="false"
              @click="orderDetail(scope.row)"
            >
              {{ scope.row.workOrderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="mainTaskName" label="工单名称" />
        <el-table-column prop="demandNo" label="需求编号" />
        <el-table-column prop="demandName" label="需求名称" />
        <el-table-column prop="orderType" label="工单类型">
          <template slot-scope="scope">
            {{ handleType(scope.row.orderType) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="stage"
          label="工单状态"
          :formatter="formatStatus"
        />
        <el-table-column prop="workManager.user_name_cn" label="工单负责人" />>
        <el-table-column prop="groupLeader" label="工单组长">
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
        <el-table-column prop="field2" label="审批人">
          <template slot-scope="scope">
            <span v-for="(user, index) in scope.row.field2" :key="index">
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150px">
          <template slot-scope="scope">
            <el-button type="text" @click="passMethod(scope.$index, aduitOrder)"
              >审批通过</el-button
            >
            <el-button
              type="text"
              @click="refuseMethod(scope.$index, aduitOrder)"
              >审批拒绝</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-main>
    <div class="pagination">
      <el-pagination
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="prev, pager, next"
        :total="total"
      ></el-pagination>
    </div>
    <el-dialog
      title="工 单 详 情"
      :visible.sync="detailOrderDialogShow"
      width="40%"
      class="abow_dialog"
      center
    >
      <el-form :model="detailOrder" label-width="40%" size="mini">
        <el-form-item label="测试任务名称 ： " class="text-grey-1">
          <el-col :span="22">{{ detailOrder.mainTaskName }}</el-col>
        </el-form-item>
        <el-form-item label="测试任务编号 ： " class="text-grey-1">
          <el-col :span="22">{{ detailOrder.mainTaskNo }}</el-col>
        </el-form-item>
        <el-form-item label="需求名称 ： " class="text-grey-1">
          <el-col :span="22">{{ detailOrder.demandName }}</el-col>
        </el-form-item>
        <el-form-item label="工单编号 ：" class="text-grey-1">
          <el-col :span="22">{{ detailOrder.workOrderNo }}</el-col>
        </el-form-item>
        <el-form-item label="实施单元 ：" class="text-grey-1">
          <el-col :span="22">{{ detailOrder.unit }}</el-col>
        </el-form-item>

        <el-form-item label="工单负责人 ：">
          <el-col :span="22">{{ detailOrder.workManager.user_name_cn }}</el-col>
        </el-form-item>
        <el-form-item label="测试小组长 ：">
          <span v-for="(user, index) in detailOrder.groupLeader" :key="index">
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </el-form-item>
        <el-form-item label="测试人员 ： ">
          <span v-for="(user, index) in detailOrder.testers" :key="index">
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </el-form-item>
        <el-form-item label="计划SIT开始时间 ：">
          <el-col :span="22">{{ detailOrder.planSitDate }}</el-col>
        </el-form-item>
        <el-form-item label="计划UAT开始时间 ：">
          <el-col :span="22">{{ detailOrder.planUatDate }}</el-col>
        </el-form-item>
        <el-form-item label="计划投产开始时间 ：">
          <el-col :span="22">{{ detailOrder.planProDate }}</el-col>
        </el-form-item>
        <el-form-item label="备注 ：" class="text-grey-1">
          <el-col :span="22">{{ detailOrder.remark }}</el-col>
        </el-form-item>
        <el-form-item label="测试阶段 ：" class="text-grey-1">
          <el-col :span="22" v-if="detailOrder.stage == '0'">待分配</el-col>
          <el-col :span="22" v-if="detailOrder.stage == '1'">开发中</el-col>
          <el-col :span="22" v-if="detailOrder.stage == '2'">SIT</el-col>
          <el-col :span="22" v-if="detailOrder.stage == '3'">UAT</el-col>
          <el-col :span="22" v-if="detailOrder.stage == '4'">已投产</el-col>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="closeDetailDia">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import orderMenuTab from './orderMenuTab';
import {
  passAduit,
  refuseAduit,
  queryAduitOrder,
  queryAduitOrderCount
} from '@/services/order';
import { orderTypeList } from './model';
import { stageMap } from '@/common/utlis';
export default {
  name: 'ApprovalOrder',
  components: { orderMenuTab },
  data() {
    return {
      orderType: 'function',
      orderTypeList: orderTypeList,
      userRole: sessionStorage.getItem('Trole'),
      isAssessor: sessionStorage.getItem('isAssessor'),
      userEnName: sessionStorage.getItem('user_en_name'),
      activeName: 'four',
      aduitOrder: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      multipleSelection: [],
      taskName: '',
      detailOrderDialogShow: false,
      detailOrder: {
        workOrderNo: '',
        mainTaskNo: '',
        mainTaskName: '',
        testers: '',
        stage: '',
        demandNo: '',
        planSitDate: '',
        planUatDate: '',
        planProDate: '',
        workOrderFlag: '',
        workManager: '',
        groupLeader: '',
        remark: '',
        createTime: '',
        field2: ''
      }
    };
  },

  created() {},
  methods: {
    //工单状态
    formatStatus(row, column) {
      return stageMap[row.stage];
    },
    handleType(val) {
      if (val == 'function') {
        return '功能测试';
      } else if (val == 'security') {
        return '安全测试';
      } else {
        return '';
      }
    },
    handleClick(tab, event) {
      if (this.userRole >= 20) {
        if (tab.index == 0) {
          this.$router.push({ path: '/Torder' }); //我的工单
        } else if (tab.index == 1) {
          this.$router.push({ path: '/HistoryOrder' }); //历史工单
        } else if (tab.index == 2) {
          this.$router.push({ path: '/QueryOrder' }); //工单查询
        } else if (tab.index == 3 && this.isAssessor === 'true') {
          this.$router.push({ path: '/ApprovalOrder' }); //审批工单
        } else if (tab.index == 3 && this.isAssessor === 'false') {
          this.$router.push({ path: '/WasteOrder' }); //审批工单
        } else if (tab.index == 4) {
          this.$router.push({ path: '/WasteOrder' }); // 废弃工单
        }
      } else {
        if (tab.index == 0) {
          this.$router.push({ path: '/QueryOrder' }); //我的工单
        } else if (tab.index == 1) {
          this.$router.push({ path: '/ApprovalOrder' }); //历史工单
        }
      }
    },
    onSubmit() {
      this.queryAduitOrder(
        this.userEnName,
        this.userRole,
        this.pageSize,
        1,
        this.taskName,
        this.orderType
      );
      this.queryAduitOrderCount();
    },
    queryAduitOrder(
      userEnName,
      userRole,
      pageSize,
      currentPage,
      taskName,
      orderType
    ) {
      queryAduitOrder({
        user_en_name: userEnName,
        userRole: userRole,
        pageSize: pageSize,
        currentPage: currentPage,
        taskName: taskName,
        orderType: orderType
      }).then(res => {
        this.aduitOrder = res;
      });
    },
    //查询总数
    queryAduitOrderCount() {
      queryAduitOrderCount({
        user_en_name: this.userEnName,
        userRole: this.userRole,
        taskName: this.taskName,
        orderType: this.orderType
      }).then(res => {
        this.total = res.total;
      });
    },
    handleCurrentChange(val) {
      this.queryAduitOrder(
        this.userEnName,
        this.userRole,
        this.pageSize,
        val,
        this.taskName,
        this.orderType
      );
    },
    //审核通过
    passMethod(index, row) {
      let i = '';
      let groupLeaderArr = [];
      let groupLeaderStr = '';
      let testersArr = [];
      let testersStr = '';
      const groupLeader = this.aduitOrder[index].groupLeader;
      const testers = this.aduitOrder[index].testers;
      for (i = 0; i < groupLeader.length; i++) {
        groupLeaderArr.push(groupLeader[i].user_name_enuser_name_en);
      }
      groupLeaderStr = groupLeaderArr.toString();
      for (i = 0; i < testers.length; i++) {
        testersArr.push(testers[i].user_name_en);
      }
      testersStr = testersArr.toString();
      this.$confirm('此操作将审批通过, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          passAduit({
            workOrderNo: this.aduitOrder[index].workOrderNo,
            groupLeader: groupLeaderStr,
            testers: testersStr
          }).then(res => {
            this.onSubmit();
            this.$message({
              type: 'success',
              message: '已审批通过!'
            });
          });
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消通过'
          });
        });
    },
    //审核拒绝
    refuseMethod(index, row) {
      this.$confirm('此操作将审批拒绝, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          refuseAduit({
            workOrderNo: this.aduitOrder[index].workOrderNo
          }).then(res => {
            this.onSubmit();
            this.$message({
              type: 'success',
              message: '已审批拒绝!'
            });
          });
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消拒绝'
          });
        });
    },
    orderDetail(row) {
      this.detailOrderDialogShow = true;
      (this.detailOrder.mainTaskName = row.mainTaskName),
        (this.detailOrder.mainTaskNo = row.mainTaskNo),
        (this.detailOrder.demandName = row.demandName),
        (this.detailOrder.workOrderNo = row.workOrderNo),
        (this.detailOrder.unit = row.unit),
        (this.detailOrder.stage = row.stage),
        (this.detailOrder.workManager = row.workManager),
        (this.detailOrder.groupLeader = row.groupLeader),
        (this.detailOrder.testers = row.testers),
        (this.detailOrder.planSitDate = row.planSitDate),
        (this.detailOrder.planUatDate = row.planUatDate),
        (this.detailOrder.planProDate = row.planProDate),
        (this.detailOrder.remark = row.remark);
    },
    closeDetailDia() {
      this.detailOrderDialogShow = false;
    }
  },
  mounted() {
    this.onSubmit();
  }
};
</script>

<style scoped>
.title {
  font-size: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
  text-align: left;
}
.el-card-showLog {
  width: 210px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.box-card {
  margin-top: 20px;
}
.card-body {
  padding: 0 20px 20px 20px;
}
.card-footer {
  padding: 10px 20px;
}
.card-container {
  margin-bottom: 20px;
}
.cardEllipsis {
  width: 58%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}
.abow_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}
.abow_dialog >>> .el-dialog__header {
  background: #409eff;
}
.el-dialog .el-form {
  max-height: 60vh;
  overflow: auto;
}
.pagination {
  margin-top: 20px;
  padding: 20px 0;
  margin-right: 50px;
  text-align: right;
}
.input-style {
  width: 260px;
}
.input-style >>> input {
  padding-right: 15px !important;
}
.input-order-name {
  width: 315px;
}
.el-button + .el-button {
  margin-left: 0px;
}
</style>
