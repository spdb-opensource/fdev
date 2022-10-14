<template>
  <div class="page-container">
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item>
        <el-input
          v-model="taskName"
          placeholder="任务名称"
          maxlength="90"
          clearable
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">查询</el-button>
        <el-button
          type="primary"
          v-if="workList.length > 1 && isNew"
          @click="workOrderSplit"
          >工单拆分</el-button
        >
      </el-form-item>
    </el-form>

    <div class="card-container">
      <div class="title">
        任务列表
        <el-tag type="success">查询到数据条数为：{{ total }}</el-tag>
      </div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in workList" :key="index">
          <el-card class="box-card" :body-style="{ padding: '15px 0 0' }">
            <div class="card-body">
              <el-row class="text-grey-2 margin-top-5">
                <el-tooltip
                  effect="dark"
                  :content="item.taskname"
                  placement="top-start"
                >
                  <el-col :span="24" style="height:20px"
                    ><el-link
                      type="primary"
                      :underline="false"
                      @click="taskDetail(item)"
                      ><el-col class="el-card-showLog">{{
                        item.taskname
                      }}</el-col></el-link
                    ></el-col
                  >
                </el-tooltip>
              </el-row>
              <el-row class="text-grey-2 fontsize-14">
                <el-col :span="12">任务编号</el-col>
                <el-tooltip
                  effect="dark"
                  :content="item.taskno"
                  placement="top-start"
                >
                  <el-col :span="12" class="cardEllipsis">{{
                    item.taskno
                  }}</el-col>
                </el-tooltip>
              </el-row>
              <el-row class="text-grey-2 fontsize-14">
                <el-col :span="12">需求编号/实施单元</el-col>
                <el-tooltip
                  effect="dark"
                  :content="item.taskunit"
                  placement="top-start"
                >
                  <el-col :span="12" class="cardEllipsis">{{
                    item.taskunit
                  }}</el-col>
                </el-tooltip>
              </el-row>
              <el-row class="text-grey-2 fontsize-14">
                <el-col :span="12">工单负责人</el-col>
                <el-col :span="12" v-if="childrenItem.workManager">{{
                  childrenItem.workManager.user_name_cn
                }}</el-col>
              </el-row>
              <el-row class="text-grey-2 fontsize-14">
                <el-col :span="12">工单组长</el-col>
                <el-col :span="12" class="cardEllipsis">
                  <span
                    v-for="(user, index) in childrenItem.groupLeader"
                    :key="index"
                  >
                    <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                  </span>
                </el-col>
              </el-row>
              <el-row class="text-grey-2 fontsize-14">
                <el-col :span="12">测试人员</el-col>
                <el-col :span="12" class="cardEllipsis">
                  <span
                    v-for="(user, index) in childrenItem.testers"
                    :key="index"
                  >
                    <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                  </span>
                </el-col>
              </el-row>
            </div>
            <div class="divider"></div>
            <el-row class="card-footer">
              <!-- <el-col :span="6">
								<el-tag type="success" v-if="item.stage == '0'">待分配</el-tag>
								<el-tag v-if="item.stage == '1'">开发中</el-tag>
								<el-tag type="warning" v-if="item.stage == '2'">SIT</el-tag>
								<el-tag type="warning" v-if="item.stage == '3'">UAT</el-tag>
								<el-tag type="danger" v-if="item.stage == '4'">已投产</el-tag>
							</el-col> -->
            </el-row>
          </el-card>
        </el-col>
      </el-row>
      <el-row class="el-row1 page">
        <el-col :offset="17" :span="7">
          <el-pagination
            :hide-on-single-page="true"
            background
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-size="pageSize"
            layout="prev, pager, next"
            :total="total"
          >
          </el-pagination>
        </el-col>
      </el-row>
    </div>

    <!-- 任务详情 -->
    <el-dialog
      title="任 务 详 情"
      :visible.sync="taskListDetailDialogShow"
      width="45%"
      class="abow_dialog"
      center
    >
      <el-form :model="taskListDetailOrder" label-width="40%" size="mini">
        <el-form-item label="任务名称：" class="text-grey-1">
          <el-col :span="22">{{ taskListDetailOrder.taskname }}</el-col>
        </el-form-item>
        <el-form-item label="任务编号：" class="text-grey-1">
          <el-col :span="22">{{ taskListDetailOrder.taskno }}</el-col>
        </el-form-item>
        <el-form-item label="需求编号/实施单元编号：" class="text-grey-1">
          <el-col :span="22">{{ taskListDetailOrder.taskunit }}</el-col>
        </el-form-item>
        <el-form-item label="工单号：" class="text-grey-1">
          <el-col :span="22">{{ taskListDetailOrder.workno }}</el-col>
        </el-form-item>
        <el-form-item label="工单负责人：" class="text-grey-1">
          <el-col :span="22" v-if="childrenItem.workManager">{{
            childrenItem.workManager.user_name_cn
          }}</el-col>
        </el-form-item>
        <el-form-item label="工单组长：" class="text-grey-1">
          <el-col :span="22">
            <span
              v-for="(user, index) in childrenItem.groupLeader"
              :key="index"
            >
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </el-col>
        </el-form-item>
        <el-form-item label="测试人员：" class="text-grey-1">
          <el-col :span="22">
            <span v-for="(user, index) in childrenItem.testers" :key="index">
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </el-col>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="closeTaskListDialogShow"
          >确定</el-button
        >
      </div>
    </el-dialog>
    <Dialog title="工单拆分" :visible.sync="dialogSplit">
      <el-form
        label-position="left"
        label-width="50%"
        :model="workOrderSplitModel"
        :rules="workOrderSplitModelRules"
        ref="workOrderSplitModel"
      >
        <t-select
          style="height:70px"
          label="任务"
          :options="taskOptions"
          multiple
          v-model="workOrderSplitModel.taskNo"
          placeholder="请选择任务"
          prop="taskNo"
        />
        <p>温馨提示：被选中的任务将组合成新工单</p>
      </el-form>
      <el-button
        type="primary"
        slot="footer"
        class="full-width"
        :loading="loadingBtn"
        @click="splitOrder('workOrderSplitModel')"
      >
        确定
      </el-button>
    </Dialog>
  </div>
</template>
<script>
import { queryTaskListImpl, queryTaskListCountImpl } from '@/services/order';
import { mapState, mapActions } from 'vuex';
import { rules } from './model';
import { validate } from '@/common/utlis';
export default {
  name: 'TaskList',
  data() {
    return {
      loadingBtn: false,
      isNew: false,
      dialogSplit: false,
      workOrderSplitModel: {
        taskNo: []
      },
      taskOptions: [],
      userRole: sessionStorage.getItem('Trole'),
      taskListDetailDialogShow: false,
      taskListDetailOrder: {},
      taskName: '',
      workNo: '',
      //任务列表
      workList: [],
      currentPage: 1,
      pageSize: 8,
      total: 0,
      childrenItem: {}
    };
  },
  computed: {
    ...mapState('workOrderForm', ['taskAllData']),
    workOrderSplitModelRules() {
      return this.rules(this.workOrderSplitModel);
    }
  },
  created() {
    const workOrderNo = this.$route.query.item.workOrderNo;
    if (workOrderNo) {
      window.sessionStorage.setItem('workOrderNoTaskList', workOrderNo);
    }
    this.workNo = window.sessionStorage.getItem('workOrderNoTaskList');
    this.queryTaskList(this.workNo, this.taskName, this.pageSize, 1);
    const childrenItem = JSON.parse(
      window.sessionStorage.getItem('childrenItem')
    );
    this.childrenItem = childrenItem;
    this.queryTaskCount();
  },
  methods: {
    ...mapActions('workOrderForm', ['queryTasks', 'splitWorkOrder']),
    async splitOrder(workOrderSplitModel) {
      await validate(this.$refs[workOrderSplitModel]);
      if (this.workOrderSplitModel.taskNo.length < this.taskOptions.length) {
        this.loadingBtn = true;
        await this.splitWorkOrder({
          workNo: this.workNo,
          taskIds: this.workOrderSplitModel.taskNo
        });
        this.$message({
          type: 'success',
          message: '拆分成功!'
        });
        this.workNo = window.sessionStorage.getItem('workOrderNoTaskList');
        this.queryTaskList(this.workNo, this.taskName, this.pageSize, 1);
        const childrenItem = JSON.parse(
          window.sessionStorage.getItem('childrenItem')
        );
        this.childrenItem = childrenItem;
        this.queryTaskCount();
        this.loadingBtn = false;
      } else {
        this.$message({
          type: 'warning',
          message: '只有一个任务时不能进行拆分!'
        });
      }
      this.dialogSplit = false;
    },
    rules(model, notNecessary = []) {
      const keys = Object.keys(model);
      const rulesObj = {};
      keys.forEach(key => {
        if (rules[key] && !notNecessary.includes(key)) {
          rulesObj[key] = rules[key];
        }
      });
      return rulesObj;
    },
    async workOrderSplit() {
      this.workOrderSplitModel.taskNo = '';
      this.taskOptions = [];
      this.dialogSplit = true;
      //窗口打开先通过工单id查任务
      await this.queryTasks({
        workNo: this.workNo
      });
      this.taskOptions = this.taskAllData.taskList.map(item => {
        return { label: item.name, value: item.id };
      });
    },
    onSubmit() {
      this.queryTaskList(this.workNo, this.taskName, this.pageSize, 1);
      this.queryTaskCount();
    },
    //查询任务总数
    queryTaskCount() {
      queryTaskListCountImpl({
        workNo: this.workNo,
        taskName: this.taskName
      }).then(res => {
        this.total = res.total;
        this.isNew = res.isNew;
      });
    },
    //查询任务列表
    queryTaskList(workNo, taskName, pageSize, currentPage) {
      queryTaskListImpl({
        workNo: workNo,
        taskName: taskName,
        pageSize: pageSize,
        currentPage: currentPage
      }).then(res => {
        this.workList = res;
      });
    },
    //换页
    handleCurrentChange(val) {
      this.queryTaskList(this.workNo, this.taskName, this.pageSize, val);
    },
    //任务详情。。。任务name点击事件
    taskDetail(item) {
      this.taskListDetailDialogShow = true;
      this.taskListDetailOrder.taskname = item.taskname;
      this.taskListDetailOrder.taskunit = item.taskunit;
      this.taskListDetailOrder.workno = item.workno;
      this.taskListDetailOrder.taskno = item.taskno;
      this.taskListDetailOrder.developer = item.developer;
      this.taskListDetailOrder.spdbmanager = item.spdbmanager;
      // 缓存里取工单责任人
      this.taskListDetailOrder.workManager = this.childrenItem.workManager;
      // 缓存里取工单组长
      this.taskListDetailOrder.groupLeader = this.childrenItem.groupLeader;
      // 缓存里取测试人员
      this.taskListDetailOrder.testers = this.childrenItem.testers;
      let list = [
        item.field1,
        item.field2,
        item.field3,
        item.field4,
        item.field5
      ];
      let remark = list.reduce((result, currentVal, index) => {
        if (currentVal) {
          result = result + currentVal + ';';
        }
        return result;
      }, '');
      this.taskListDetailOrder.fields = remark.substr(0, remark.length - 1);
    },
    //关闭任务详情页面
    closeTaskListDialogShow() {
      this.taskListDetailDialogShow = false;
    }
  }
};
</script>
<style scoped>
.el-card-showLog {
  width: 210px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.cardEllipsis {
  width: 50%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.title {
  font-size: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
  text-align: left;
}
.box-card {
  margin-top: 20px;
  margin-bottom: 20px;
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
</style>
