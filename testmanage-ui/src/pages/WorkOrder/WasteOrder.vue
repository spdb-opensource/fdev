<template>
  <div class="page-container">
    <orderMenuTab />
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item>
        <el-input
          v-model="formInline.taskName"
          class="input-style input-order-name"
          placeholder="需求名称/需求编号/实施单元/任务名/工单名"
          maxlength="90"
          clearable
          @keyup.enter.native="onSubmit()"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-select v-model="formInline.orderType" placeholder="工单类型">
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
        <el-select
          v-model="formInline.userEnName"
          placeholder="请选择测试人员"
          filterable
          clearable
        >
          <el-option
            v-for="item in selectNames"
            :key="item.index"
            :value="item.user_name_en"
            :label="item.user_name_cn"
          >
            <span style="float: left">{{ item.user_name_cn }}</span>
            <span style="float: right">{{ item.user_name_en }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit()">查询</el-button>
      </el-form-item>
    </el-form>

    <div class="card-container">
      <div class="title">
        废弃工单
      </div>
      <el-row :gutter="20">
        <el-col
          :span="6"
          v-for="(item, index) in wasteList"
          :key="index"
          style="height: 280px;"
        >
          <el-card class="box-card" :body-style="{ padding: '12px 0 0' }">
            <div>
              <el-row :span="24" class="marginbt8">
                <div class="headerStyle">
                  <div
                    :class="'chip' + item.orderType"
                    style="text-align: center;"
                  >
                    <div class="textStyle">
                      {{ orderTypeText(item.orderType) }}
                    </div>
                  </div>
                  <!-- 状态（加小圆点） -->
                  <div class="headerStyle buttonMr10">
                    <div
                      :class="'point-' + orderStageOther(item.stage).type"
                      class="pointMl16"
                    ></div>
                    <span
                      class="fontsize-12 weight pointMl4"
                      :class="'text-' + orderStageOther(item.stage).type"
                    >
                      {{ orderStageOther(item.stage).label }}
                    </span>
                  </div>
                </div>
              </el-row>
              <div class="divider"></div>

              <div class="ellipsis paddingWay">
                <el-tooltip
                  effect="dark"
                  placement="top-start"
                  :visible-arrow="false"
                >
                  <div slot="content" class="elTooltip">
                    {{ item.mainTaskName }}
                  </div>
                  <el-link
                    :underline="false"
                    type="primary"
                    class="fontweight"
                    @click.stop="wasteDetail(item)"
                  >
                    {{ item.mainTaskName }}
                  </el-link>
                </el-tooltip>
              </div>

              <div class="padding-10">
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">需求名称：</el-col>
                  <el-tooltip
                    effect="dark"
                    :content="item.demandName"
                    placement="top-start"
                  >
                    <el-col :span="12" class="cardEllipsis">{{
                      item.demandName
                    }}</el-col>
                  </el-tooltip>
                </el-row>
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">测试负责人：</el-col>
                  <el-col :span="12" v-if="item.workManager">{{
                    item.workManager.user_name_cn
                  }}</el-col>
                </el-row>
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">测试小组长：</el-col>
                  <el-tooltip
                    effect="dark"
                    :content="getContent(item.groupLeader)"
                    placement="top-start"
                  >
                    <el-col :span="12" class="cardEllipsis">
                      <span
                        v-for="(user, index) in item.groupLeader"
                        :key="index"
                      >
                        <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                      </span>
                    </el-col>
                  </el-tooltip>
                </el-row>
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">测试人员：</el-col>
                  <el-tooltip
                    effect="dark"
                    :content="getContent(item.testers)"
                    placement="top-start"
                  >
                    <el-col :span="12" class="cardEllipsis">
                      <span v-for="(user, index) in item.testers" :key="index">
                        <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                      </span>
                    </el-col>
                  </el-tooltip>
                </el-row>
              </div>
            </div>
            <div class="divider"></div>
            <el-row class="card-footer">
              <el-col :span="24" class="text-right" v-if="item.stage == '11'">
                <el-button-group>
                  <el-button type="primary" size="small" @click="exePlan(item)">
                    执行计划<span v-if="item.hasCaseFlag === '1'" class="icon"
                      >★</span
                    >
                  </el-button>
                  <el-button
                    type="primary"
                    v-if="item.workOrderFlag != '0'"
                    size="small"
                    @click="openTaskList(item)"
                  >
                    任务列表
                    <span v-if="item.hasTaskFlag === '1'" class="icon">★</span>
                  </el-button>
                </el-button-group>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
      <el-row>
        <el-col :offset="17" :span="7">
          <el-pagination
            :hide-on-single-page="true"
            background
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-size="pageSize"
            layout="prev, pager, next"
            :pager-count="5"
            :total="total"
          >
          </el-pagination>
        </el-col>
      </el-row>
    </div>

    <!-- 工单详情 -->

    <el-dialog
      title="工 单 详 情"
      :visible.sync="wasteDetailOrderDialogShow"
      class="abow_dialog"
      center
    >
      <div v-loading="showLoading">
        <el-form :model="wasteDetailOrder" label-width="40%" size="mini">
          <el-form-item label="工单名称：" class="text-grey-1">
            <el-col :span="22">{{ wasteDetailOrder.mainTaskName }}</el-col>
          </el-form-item>
          <el-form-item label="工单编号：" class="text-grey-1">
            <el-col :span="22">{{ wasteDetailOrder.workOrderNo }}</el-col>
          </el-form-item>
          <el-form-item label="需求名称：" class="text-grey-1">
            <el-col :span="22">{{ wasteDetailOrder.demandName }}</el-col>
          </el-form-item>
          <el-form-item label="需求编号：" class="text-grey-1">
            <el-col :span="22">{{ wasteDetailOrder.demandNo }}</el-col>
          </el-form-item>
          <el-form-item label="实施单元：" class="text-grey-1">
            <el-col :span="22">{{ wasteDetailOrder.unit }}</el-col>
          </el-form-item>
          <el-form-item label="所属小组 ：" class="text-grey-1">
            <el-col :span="22">{{ wasteDetailOrder.groupName }}</el-col>
          </el-form-item>
          <el-form-item label="工单负责人：">
            <el-col :span="22" v-if="wasteDetailOrder.workManager">{{
              wasteDetailOrder.workManager.user_name_cn
            }}</el-col>
          </el-form-item>
          <el-form-item label="测试小组长：">
            <span
              :span="22"
              v-for="(user, index) in wasteDetailOrder.groupLeader"
              :key="index"
            >
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </el-form-item>
          <el-form-item label="测试人员：">
            <span
              :span="22"
              v-for="(user, index) in wasteDetailOrder.testers"
              :key="index"
            >
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </el-form-item>
          <el-form-item label="计划SIT开始时间：">
            <el-col :span="22">{{ wasteDetailOrder.planSitDate }}</el-col>
          </el-form-item>
          <el-form-item label="计划UAT开始时间：">
            <el-col :span="22">{{ wasteDetailOrder.planUatDate }}</el-col>
          </el-form-item>
          <el-form-item label="计划投产开始时间：">
            <el-col :span="22">{{ wasteDetailOrder.planProDate }}</el-col>
          </el-form-item>
          <el-form-item label="备注：" class="text-grey-1">
            <el-col :span="22">{{ wasteDetailOrder.remark }}</el-col>
          </el-form-item>
          <el-form-item label="测试阶段：" class="text-grey-1">
            <el-col :span="22" v-if="wasteDetailOrder.stage == '11'"
              >已废弃</el-col
            >
          </el-form-item>
          <el-form-item label="任务关联文档：">
            <el-tree
              :default-expand-all="true"
              class="tree tree-style"
              :data="docData"
              ref="tree"
              highlight-current
              :props="props"
              @node-click="handNodeClick"
              :filter-node-method="filterNode"
            >
              <div slot-scope="{ node, data }" class="ellipsis">
                <el-tooltip
                  effect="dark"
                  :content="node.label"
                  placement="top-start"
                  class="tooltip"
                  v-if="data.path"
                >
                  <el-link :underline="false" type="primary">
                    <span>{{ node.label }}</span>
                  </el-link>
                </el-tooltip>
                <span v-else>{{ node.label }}</span>
              </div>
            </el-tree>
          </el-form-item>
          <el-form-item label="需求关联文档：">
            <el-tree
              :default-expand-all="true"
              class="tree tree-style"
              :data="docDataRqr"
              ref="tree"
              highlight-current
              :props="props"
              @node-click="downloadRqrDoc"
              :filter-node-method="filterNode"
            >
              <div slot-scope="{ node, data }" class="ellipsis">
                <el-tooltip
                  effect="dark"
                  :content="node.label"
                  placement="top-start"
                  class="tooltip"
                  v-if="data.id"
                >
                  <el-link :underline="false" type="primary">
                    <span>{{ node.label }}</span>
                  </el-link>
                </el-tooltip>
                <span v-else>{{ node.label }}</span>
              </div>
            </el-tree>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="closeHistoryDia" class="modifyBtn"
          >确 定</el-button
        >
      </div>
    </el-dialog>
    <!-- 任务列表窗口 -->
    <TaskListDialog
      :openDialog="taskListDialog"
      :dialogModel="taskListDialogDetail"
      @taskListClose="closeDialogTaskList"
    />
  </div>
</template>
<script>
import TaskListDialog from './Compoments/TaskListDialog';
import { queryWasteOrder, queryWasteOrderCount } from '@/services/order';
import { handleDocDataRqr, getUserListByRole } from '@/common/utlis';
import orderMenuTab from './orderMenuTab.vue';
import { mapActions, mapState } from 'vuex';
import { orderTypeList, orderStageOther } from './model';
export default {
  name: 'WasteOrder',
  components: {
    orderMenuTab,
    TaskListDialog
  },
  data() {
    return {
      orderTypeList: orderTypeList,
      taskListDialogDetail: [],
      taskListDialog: false,
      props: {
        children: 'children',
        label: 'label'
      },
      docData: [],
      docDataRqr: [
        { label: '需求说明书', children: [] },
        { label: '需求规格说明书', children: [] },
        { label: '其它相关材料', children: [] }
      ],
      rqrDocData: [],
      username: sessionStorage.getItem('user_en_name'),
      userRole: sessionStorage.getItem('Trole'),
      isAssessor: sessionStorage.getItem('isAssessor'),
      activeName: 'five',
      wasteDetailOrderDialogShow: false,
      wasteDetailOrder: {
        mainTaskName: '',
        mainTaskNo: '',
        demandNo: '',
        demandName: '',
        workOrderNo: '',
        stage: '',
        workManager: '',
        groupLeader: '',
        testers: '',
        planSitDate: '',
        planUatDate: '',
        planProDate: '',
        remark: '',
        approver: '',
        groupName: '',
        orderType: ''
      },
      value1: [],
      formInline: {
        taskName: '',
        orderType: 'function',
        userEnName: ''
      },
      wasteList: [],
      currentPage: JSON.parse(sessionStorage.getItem('cacheWastePage')) || 1,
      pageSize: 8,
      total: 0,
      loading: false,
      selectNames: [],
      groupList: [],
      showLoading: false
    };
  },
  computed: {
    ...mapState('workOrderForm', [
      'childTaskList',
      'taskDoc',
      'rqrFilesByOrderNo',
      'fdevTaskByWorkNo'
    ])
  },
  filters: {
    testerFilter(users) {
      if (!users) {
        return;
      }
      let userNames = [];
      users.forEach(user => {
        userNames.push(user.name);
      });
      return userNames.join(',');
    }
  },
  created() {
    if (JSON.parse(sessionStorage.getItem('pageInfoWaste'))) {
      this.formInline = JSON.parse(sessionStorage.getItem('pageInfoWaste'));
    }
    this.queryWasteOrder_(
      this.formInline.taskName,
      this.formInline.userEnName,
      this.pageSize,
      this.currentPage,
      this.formInline.orderType
    );
    this.queryWasteOrderCount_();
  },
  methods: {
    ...mapActions('workOrderForm', [
      'queryWorkOrderStageList',
      'downExcel',
      'queryRqrFilesByOrderNo',
      'queryTaskDoc',
      'queryTaskList',
      'queryFdevTaskByWorkNo'
    ]),
    orderStageOther(stage) {
      return orderStageOther[stage.trim()];
    },
    orderTypeText(type) {
      if (type == 'function') {
        return '功能测试';
      } else if (type == 'security') {
        return '安全测试';
      } else {
        return '';
      }
    },
    async openTaskList(item) {
      this.taskListDialog = true;
      await this.queryFdevTaskByWorkNo({ workNo: item.workOrderNo });
      this.taskListDialogDetail = this.fdevTaskByWorkNo;
    },
    closeDialogTaskList() {
      this.taskListDialog = false;
    },
    async handNodeClick(data) {
      if (data.path) {
        let param = {
          path: data.path,
          moduleName: 'fdev-task'
        };
        await this.downExcel(param);
        this.$message({
          showClose: true,
          message: '下载成功!',
          type: 'success'
        });
      }
    },
    async downloadRqrDoc(data) {
      if (data.id) {
        let param = {
          path: data.id,
          moduleName: 'fdev-demand'
        };
        await this.downExcel(param);
        this.$message({
          showClose: true,
          message: '下载成功!',
          type: 'success'
        });
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    onSubmit() {
      this.queryWasteOrder_(
        this.formInline.taskName,
        this.formInline.userEnName,
        this.pageSize,
        1,
        this.formInline.orderType
      );
      this.queryWasteOrderCount_();
    },
    //换页
    handleCurrentChange(val) {
      this.currentPage = val;
      this.queryWasteOrder_(
        this.formInline.taskName,
        this.formInline.userEnName,
        this.pageSize,
        val,
        this.formInline.orderType
      );
    },
    async getDoc() {
      this.showLoading = true;
      let docList = [];
      this.docData = [];
      if (
        this.wasteDetailOrder.mainTaskNo ||
        this.wasteDetailOrder.workOrderNo
      ) {
        try {
          await this.queryTaskDoc({
            id: this.wasteDetailOrder.mainTaskNo
              ? this.wasteDetailOrder.mainTaskNo
              : this.wasteDetailOrder.workOrderNo
          });
        } catch (e) {
          this.showLoading = false;
        }
        if (
          this.taskDoc &&
          Array.isArray(this.taskDoc) &&
          this.taskDoc.length > 0
        ) {
          this.docData.push({
            label: this.wasteDetailOrder.mainTaskName,
            children: this.taskDoc
          });

          docList.push(this.taskDoc);
        }
        if (this.wasteDetailOrder.orderType == 'security') {
          try {
            await this.queryLastTransFilePath({
              workNo: this.wasteDetailOrder.workOrderNo
            });
          } catch (e) {
            this.showLoading = false;
          }
          if (this.filePath) {
            let securityDoc = [
              {
                name: this.filePath.substring(
                  this.filePath.lastIndexOf('/') + 1
                ),
                path: this.filePath
              }
            ];
            this.docData.push({
              label: this.wasteDetailOrder.mainTaskName,
              children: securityDoc
            });
            docList.push(securityDoc);
          }
        }
        for (let i = 0; i < this.childTaskList.length; i++) {
          try {
            await this.queryTaskDoc({
              id: this.childTaskList[i].taskno
            });
          } catch (e) {
            this.showLoading = false;
          }
          if (
            this.taskDoc &&
            Array.isArray(this.taskDoc) &&
            this.taskDoc.length > 0
          ) {
            docList.push(this.taskDoc);
            this.docData.push({
              label: this.childTaskList[i].taskname,
              children: this.taskDoc
            });
          }
        }
        handleDocDataRqr(this.docData, docList, false);
      }
      this.showLoading = false;
    },
    async getDocRqr() {
      this.showLoading = true;
      try {
        await this.queryRqrFilesByOrderNo({
          workNo: this.wasteDetailOrder.workOrderNo
        });
      } catch (e) {
        this.showLoading = false;
      }
      handleDocDataRqr(this.docDataRqr, [
        this.rqrFilesByOrderNo.rqrmntInstruction,
        this.rqrFilesByOrderNo.rqrmntRule,
        this.rqrFilesByOrderNo.otherDoc
      ]);
      this.showLoading = false;
    },
    //历史工单详情
    async wasteDetail(row) {
      this.init();
      this.wasteDetailOrderDialogShow = true;
      this.wasteDetailOrder.mainTaskName = row.mainTaskName;
      this.wasteDetailOrder.mainTaskNo = row.mainTaskNo;
      this.wasteDetailOrder.demandNo = row.demandNo;
      this.wasteDetailOrder.demandName = row.demandName;
      this.wasteDetailOrder.workOrderNo = row.workOrderNo;
      this.wasteDetailOrder.unit = row.unit;
      this.wasteDetailOrder.stage = row.stage;
      this.wasteDetailOrder.workManager = row.workManager;
      this.wasteDetailOrder.groupLeader = row.groupLeader;
      this.wasteDetailOrder.testers = row.testers;
      this.wasteDetailOrder.planSitDate = row.planSitDate;
      this.wasteDetailOrder.planUatDate = row.planUatDate;
      this.wasteDetailOrder.planProDate = row.planProDate;
      this.wasteDetailOrder.remark = row.remark;
      this.wasteDetailOrder.groupName = row.groupName;
      let param = {
        currentPage: 1,
        pageSize: 50,
        taskName: '',
        workNo: row.workOrderNo
      };
      await this.queryTaskList(param);
      await this.getDocRqr();
      await this.getDoc();
    },
    //查询废弃工单总数
    queryWasteOrderCount_() {
      queryWasteOrderCount({
        taskName: this.formInline.taskName,
        userRole: this.userRole,
        orderType: this.formInline.orderType,
        testerName: this.formInline.userEnName
          ? this.formInline.userEnName
          : null
      }).then(res => {
        this.total = res.total;
      });
    },
    //查询废弃工单
    queryWasteOrder_(taskName, userEnName, pageSize, currentPage, orderType) {
      queryWasteOrder({
        taskName: taskName,
        testerName: userEnName ? userEnName : null,
        pageSize: pageSize,
        currentPage: currentPage,
        userRole: this.userRole,
        orderType: orderType
      }).then(res => {
        this.wasteList = res;
      });
    },
    //关闭历史详情页面
    closeHistoryDia() {
      this.wasteDetailOrderDialogShow = false;
    },
    init() {
      this.rqrDocData = [];
    },
    /**
     * 跳转任务列表
     * */
    goTaskList(item) {
      window.sessionStorage.setItem('childrenItem', JSON.stringify(item));
      var task = { workOrderNo: item.workOrderNo };
      this.$router.push({ name: 'TaskList', query: { item: task } });
    },
    exePlan(item) {
      let i = '';
      let testersArr = [];
      let testersStr = '';
      const testers = item.testers;
      let isSelf = false;
      for (i = 0; i < testers.length; i++) {
        if (testers[i].user_name_en === this.username) {
          isSelf = true;
        }
        testersArr.push(testers[i].user_name_cn);
      }
      if (item.workManager.user_name_en === this.username) {
        isSelf = true;
      }
      item.groupLeader.forEach(user => {
        if (user.user_name_en === this.username) {
          isSelf = true;
        }
      });
      testersStr = testersArr.toString();
      this.$router.push({
        name: 'Plan',
        query: {
          workOrderNo: item.workOrderNo,
          isWaste: true,
          isSelf: isSelf,
          orderType: item.orderType
        }
      });
      const workOrderNoData = {
        stage: item.stage,
        workOrderNo: item.workOrderNo,
        testers: testersStr,
        mainTaskName: item.mainTaskName,
        unitNo: item.unit,
        planPower: this.planPower,
        workNo: item.workOrderNo
      };
      sessionStorage.setItem(
        'planWorkOrderNo',
        JSON.stringify(workOrderNoData)
      );
    },
    getContent(val) {
      let res = [];
      val.forEach(item => {
        res.push(item.user_name_cn);
      });
      return res.join(' ');
    }
  },
  async mounted() {
    // 查询内测人员
    this.selectNames = await getUserListByRole(['测试人员']);
  },
  watch: {},
  // 缓存页码
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('cacheWastePage');
      sessionStorage.removeItem('pageInfoWaste');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem('cacheWastePage', this.currentPage);
    sessionStorage.setItem('pageInfoWaste', JSON.stringify(this.formInline));
    next();
  }
};
</script>
<style scoped>
.tree-style >>> .is-leaf + .el-tree-node__label {
  color: #2196f3 !important;
  text-overflow: ellipsis !important;
  overflow: hidden !important;
}
.el-link {
  display: inline !important;
}
.title {
  font-size: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
  text-align: left;
}
.box-card {
  margin-top: 20px;
}
.card-footer {
  padding: 10px 10px;
}
.card-container {
  margin-bottom: 20px;
}
.cardEllipsis {
  width: 50%;
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
.abow_dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.abow_dialog >>> .el-dialog {
  margin: 0 auto !important;
  height: 90%;
  overflow: hidden;
}
.abow_dialog >>> .el-dialog__body {
  position: absolute;
  left: 10px;
  top: 70px;
  bottom: 60px;
  right: 0;
  padding: 0;
  z-index: 1;
  overflow: hidden;
  overflow-y: auto;
}
.abow_dialog >>> .el-dialog__footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
}
.text-right .icon {
  position: absolute;
  top: 2px;
  color: yellow;
}
.input-style {
  width: 260px;
}
.input-style >>> input {
  padding-right: 15px !important;
}
.rqr-list {
  color: #2196f3 !important;
  cursor: pointer;
}
.norqr-list {
  margin-left: 23px;
}
.bottomItem {
  margin-bottom: 0 !important;
}
.dropdown-position {
  margin-right: 20px;
  margin-top: 5px;
}
.input-order-name {
  width: 315px;
}
.elTooltip {
  max-width: 300px;
}
.pointMl16 {
  margin-left: 16px;
}
.pointMl4 {
  margin-left: 4px;
}
.headerStyle {
  display: flex;
  align-items: center;
}
.buttonMr10 {
  margin-right: 10px;
}
.point-success {
  background-image: linear-gradient(
    270deg,
    rgba(140, 188, 72, 0.5) 0%,
    #8cbc48 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.point-primary {
  background-image: linear-gradient(
    270deg,
    rgba(64, 158, 255, 0.5) 0%,
    #409eff 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.point-warning {
  background-image: linear-gradient(
    270deg,
    rgba(230, 162, 60, 0.5) 0%,
    #e6a23c 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.point-danger {
  background-image: linear-gradient(
    270deg,
    rgba(245, 108, 108, 0.5) 0%,
    #f56c6c 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.chipsecurity {
  background: #fd8d00;
  border-radius: 0 11px 11px 0;
  border-radius: 0px 11px 11px 0px;
  width: 92px;
  height: 22px;
}
.chipfunction {
  background: #4dbb59;
  border-radius: 0 11px 11px 0;
  border-radius: 0px 11px 11px 0px;
  width: 92px;
  height: 22px;
}
.textStyle {
  font-family: PingFangSC-Semibold;
  font-size: 12px;
  color: #ffffff;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 600;
}
.marginbt8 {
  margin-bottom: 8px;
}
.padding-10 {
  padding: 8px 10px 10px 10px;
}
.paddingWay {
  padding-left: 10px;
  padding-top: 7px;
}
.fontweight {
  font-weight: 600;
}
</style>
