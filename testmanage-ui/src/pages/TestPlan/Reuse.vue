<template>
  <div class="page-container">
    <el-container>
      <el-scrollbar>
        <div class="aside">
          <el-tree
            class="tree"
            :data="asideData"
            ref="tree"
            highlight-current
            :props="props"
            @node-click="handNodeClick"
          ></el-tree>
        </div>
      </el-scrollbar>
      <el-container>
        <el-header>
          <el-form :inline="true" class="demo-form-inline" label-width="90px">
            <el-row>
              <el-col :span="5" :offset="2">
                <el-form-item>
                  <el-input
                    v-model="functionPoint"
                    suffix-icon="xxx"
                    placeholder="请输入功能点"
                    clearable
                    @keyup.enter.native="clickQuery()"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="5" :offset="1">
                <el-form-item>
                  <el-select
                    v-model="testcaseType"
                    placeholder="请选择案例类型"
                    clearable
                    @keyup.enter.native="clickQuery()"
                  >
                    <el-option label="页面" value="1"></el-option>
                    <el-option label="功能" value="2"></el-option>
                    <el-option label="流程" value="3"></el-option>
                    <el-option label="链接" value="4"></el-option>
                    <el-option label="接口" value="5"></el-option>
                    <el-option label="批量" value="6"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5" :offset="1">
                <el-form-item>
                  <el-input
                    v-model="workNo"
                    suffix-icon="xxx"
                    placeholder="请输入工单号"
                    clearable
                    @keyup.enter.native="clickQuery()"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="5" :offset="2">
                <el-form-item>
                  <el-select
                    v-model="testcaseNature"
                    placeholder="请选择案例性质"
                    clearable
                    @keyup.enter.native="clickQuery()"
                  >
                    <el-option label="正案例" value="1"></el-option>
                    <el-option label="反案例" value="2"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5" :offset="1">
                <div class="block">
                  <el-date-picker
                    v-model="testcaseDate"
                    type="date"
                    placeholder="请选择创建日期"
                    value-format="yyyy-MM-dd"
                  ></el-date-picker>
                </div>
              </el-col>
              <el-col :span="5" :offset="1">
                <el-form-item>
                  <el-select
                    v-model="testcasePeople"
                    placeholder="请选择案例编写人"
                    filterable
                    clearable
                  >
                    <el-option
                      v-for="(item, index) in testcasePeopleList"
                      :key="index"
                      :label="item.user_name_cn"
                      :value="item.user_name_en"
                    >
                      <span style="float: left">{{ item.user_name_cn }}</span>
                      <span style="float: right">{{ item.user_name_en }}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="4" :offset="1">
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="clickQuery()"
                    class="queryBtn"
                    >查 询</el-button
                  >
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-header>
        <el-main>
          <el-table
            stripe
            :data="ReuseTable"
            tooltip-effect="dark"
            style="width: 100%;color:black"
            :header-cell-style="{ color: '#545c64' }"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="30"></el-table-column>
            <el-table-column
              fixed
              prop="necessaryFlag"
              label="必测"
              width="70"
              :formatter="formatterNecessaryFlag"
              :render-header="renderHeader"
            ></el-table-column>
            <el-table-column
              fixed
              prop="testcaseNo"
              label="案例编号"
              width="150"
            ></el-table-column>
            <el-table-column
              prop="testcaseName"
              label="案例名称"
              width="200"
            ></el-table-column>
            <el-table-column
              prop="testcaseType"
              label="案例类型"
              width="80"
              :formatter="formatterTestcaseType"
            ></el-table-column>
            <el-table-column
              prop="testcaseFuncName"
              label="功能模块"
              width="100"
            ></el-table-column>
            <el-table-column
              prop="funcationPoint"
              label="功能点"
              width="150"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="testcaseDescribe"
              label="案例描述"
              width="180"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="expectedResult"
              label="期望结果"
              width="180"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="userName"
              label="案例编写人"
              width="90"
            ></el-table-column>
            <el-table-column
              prop="testcaseDate"
              label="创建时间"
              width="95"
            ></el-table-column>
            <el-table-column
              fixed="right"
              label="操作"
              width="100"
              align="center"
            >
              <template slot-scope="scope">
                <el-button
                  v-if="!scope.row.isEgdit"
                  type="text"
                  size="small"
                  @click="handleReuse(scope.$index, scope.row)"
                  >复用</el-button
                >
                <el-tooltip
                  effect="dark"
                  content="设为必测"
                  placement="top"
                  v-if="setMustPower(scope.row)"
                >
                  <el-button
                    size="medium"
                    icon="el-icon-star-on"
                    circle
                    v-if="setMustPower(scope.row)"
                    @click="setMustClick(scope.row)"
                  ></el-button>
                </el-tooltip>
                <el-tooltip
                  effect="dark"
                  content="取消必测"
                  placement="top"
                  v-if="cancelMustPower(scope.row)"
                >
                  <el-button
                    size="medium"
                    icon="el-icon-star-off"
                    circle
                    v-if="cancelMustPower(scope.row)"
                    @click="cancelMustClick(scope.row)"
                  ></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <el-row class="operateBtn" :gutter="20">
            <el-col :span="6" :offset="6">
              <el-button type="primary" @click="batchBtn()"
                >批 量 复 用</el-button
              >
            </el-col>
            <el-col :span="12">
              <el-button type="primary" @click="returnBtn">返 回</el-button>
            </el-col>
          </el-row>
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
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';

export default {
  name: 'Reuse',
  data() {
    return {
      planId: '',
      asideData: [],
      props: {
        children: 'children',
        label: 'label'
      },
      count: 1,
      ReuseTable: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      treeLeafList: [],
      options: [
        {
          planId: '选项1',
          planName: '选项1'
        }
      ],
      selectValue: '',
      multipleSelection: [],
      functionPoint: '',
      testcasePeople: '',
      testcasePeopleList: [],
      testcaseStatus: '',
      testcaseType: '',
      testcaseNature: '',
      workNo: '',
      testcaseDate: '',
      getTreeNodeDate: '',
      newNecessaryFlag: ''
    };
  },
  computed: {
    ...mapState('testCaseForm', [
      'allUserNameList',
      'countTest',
      'list',
      'testcase'
    ])
  },
  methods: {
    ...mapActions('testCaseForm', [
      'queryAllUserName',
      'changeNecessary',
      'countTestcaseByFuncId',
      'listAll',
      'queryTestcaseByFuncId',
      'batchRepeatedRelation',
      'repeatedTestcaseForPlans'
    ]),
    // 操作图片显示控制
    setMustPower(params) {
      if (params.necessaryFlag == 1) {
        return false;
      } else {
        return true;
      }
    },
    cancelMustPower(params) {
      if (params.necessaryFlag != 1) {
        return false;
      } else {
        return true;
      }
    },
    renderHeader(h, { column }) {
      return h('span', [
        h('span', column.label),
        h('el-checkbox', {
          style: 'margin-left: 5px;',
          on: {
            change: this.change
          }
        })
      ]);
    },
    change(val) {
      if (val == true) {
        this.newNecessaryFlag = '1';
      } else {
        this.newNecessaryFlag = null;
      }
      this.queryList(1);
      this.queryTotal();
    },
    // 点击复用，取消复用前端更新table数据 解决发请求 用户等待时间长的问题
    changeNecessaryFlag(testcaseNo, necessaryFlag) {
      let cacheTableData = this.ReuseTable;
      // 遍历数组 改变necessaryFlag的值
      for (var i in cacheTableData) {
        if (cacheTableData[i]['testcaseNo'] == testcaseNo) {
          cacheTableData[i]['necessaryFlag'] = necessaryFlag;
          this.ReuseTable = cacheTableData;
          return;
        }
      }
    },
    // 设为必测
    async setMustClick(row) {
      await this.changeNecessary({
        testcaseNo: row.testcaseNo,
        necessaryFlag: '1'
      });
      // this.queryListAll(); 请求交易时间长
      this.changeNecessaryFlag(row.testcaseNo, '1');
    },
    async cancelMustClick(row) {
      await this.changeNecessary({
        testcaseNo: row.testcaseNo,
        necessaryFlag: '0'
      });
      if (this.pageSize * (this.currentPage - 1) + 1 >= this.total) {
        this.currentPage -= 1;
        if (this.currentPage == 0) {
          this.currentPage = 1;
        }
      }
      this.queryTotal();
      // this.queryListAll(); 请求交易时间长
      this.changeNecessaryFlag(row.testcaseNo, '0');
    },
    handNodeClick(a, b, c) {
      if (!b.data.func_id) return;
      this.queryTestcastByFuncId(b.data.func_id);
      this.getTreeNodeDate = b.data.func_id;
    },
    returnBtn() {
      if (sessionStorage.getItem('workNoAndTestcaseIdAndPlanId')) {
        const { workOrderNo, planlist_testcase_id, planId } = JSON.parse(
          sessionStorage.getItem('workNoAndTestcaseIdAndPlanId')
        );
        this.$router.push({
          name: 'Plan',
          query: { workOrderNo, planlist_testcase_id, planId }
        });
      } else {
        this.$router.push('/Plan');
      }
    },
    handleSizeChange: function(size) {
      this.pageSize = size;
      this.queryListAll();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.queryList(this.currentPage);
    },
    handleSelectionChange(rows) {
      this.multipleSelection = rows;
    },

    //页面点击查询
    clickQuery() {
      this.queryList(1);
      this.queryTotal();
    },
    async queryList(val) {
      this.currentPage = val;
      await this.queryTestcaseByFuncId({
        necessaryFlag: this.newNecessaryFlag ? this.newNecessaryFlag : null,
        funcationPoint: this.functionPoint ? this.functionPoint : null,
        testcasePeople: this.testcasePeople ? this.testcasePeople : null,
        testcaseStatus: '21',
        testcaseType: this.testcaseType ? this.testcaseType : null,
        testcaseNature: this.testcaseNature ? this.testcaseNature : null,
        workNo: this.workNo ? this.workNo : null,
        testcaseDate: this.testcaseDate ? this.testcaseDate : null,
        testcaseFuncId: this.getTreeNodeDate ? this.getTreeNodeDate : null,
        pageSize: this.pageSize,
        currentPage: this.currentPage ? this.currentPage : null
      });
      this.ReuseTable = this.testcase;
    },

    // 表格内容格式化
    formatterTestcaseType(row, column) {
      if (row.testcaseType === '1') {
        return '页面';
      } else if (row.testcaseType === '2') {
        return '功能';
      } else if (row.testcaseType === '3') {
        return '流程';
      } else if (row.testcaseType === '4') {
        return '链接';
      } else if (row.testcaseType === '5') {
        return '接口';
      } else if (row.testcaseType === '6') {
        return '批量';
      } else {
        return '';
      }
    },
    //案例性质
    formatterTestcaseNature(row, column) {
      if (row.testcaseNature === '2') {
        return '反案例';
      } else if (row.testcaseNature === '1') {
        return '正案例';
      } else {
        return '';
      }
    },

    formatterTestcaseStatus(row, column) {
      if (row.testcaseStatus === '0') {
        return '新建';
      } else if (row.testcaseStatus === '10') {
        return '待评审';
      } else if (row.testcaseStatus === '11') {
        return '审核通过';
      } else if (row.testcaseStatus === '12') {
        return '审核拒绝';
      } else if (row.testcaseStatus === '20') {
        return '待生效';
      } else if (row.testcaseStatus === '21') {
        return '生效通过';
      } else if (row.testcaseStatus === '22') {
        return '生效拒绝';
      } else if (row.testcaseStatus === '30') {
        return '通用';
      } else if (row.testcaseStatus === '40') {
        return '废弃';
      } else {
      }
    },
    formatterNecessaryFlag(row, colum) {
      if (row.necessaryFlag == 1) {
        return (
          <i
            class="el-icon-star-on"
            style="color:#f7ba2a;font-size:15px;margin-left:10px"
          ></i>
        );
      } else if (row.necessaryFlag == null) {
        return '';
      } else {
        return '';
      }
    },

    // 查询系统列表
    async initlistAll() {
      await this.listAll({});
      let res = this.list;
      var map = {};
      var list = [];
      for (var i = 0; i < res.length; i++) {
        if (res[i].func_id)
          map[res[i].func_id] = {
            label: res[i].func_model_name,
            children: [],
            parent_id: res[i].parent_id,
            sys_id: res[i].sys_id,
            func_id: res[i].func_id
          };
        else
          map[res[i].sys_id] = {
            label: res[i].sys_module_name,
            children: [],
            parent_id: -1,
            sys_id: res[i].sys_id
          };
      }
      for (var field in map) {
        var obj = map[field];
        var parent_id = obj.parent_id;
        if (parent_id == 0) parent_id = obj.sys_id;
        //delete obj.parent_id;delete obj.sys_id;
        if (parent_id == -1) {
          list[list.length] = obj;
        } else {
          map[parent_id].children[map[parent_id].children.length] = obj;
        }
      }
      this.asideData = list;
    },

    async queryTotal() {
      await this.countTestcaseByFuncId({
        necessaryFlag: this.newNecessaryFlag ? this.newNecessaryFlag : null,
        funcationPoint: this.functionPoint ? this.functionPoint : null,
        testcasePeople: this.testcasePeople ? this.testcasePeople : null,
        testcaseStatus: '21',
        testcaseType: this.testcaseType ? this.testcaseType : null,
        testcaseNature: this.testcaseNature ? this.testcaseNature : null,
        workNo: this.workNo ? this.workNo : null,
        testcaseDate: this.testcaseDate ? this.testcaseDate : null,
        testcaseFuncId: this.getTreeNodeDate ? this.getTreeNodeDate : null
      });
      this.total = this.countTest;
    },

    //页面表格数据查询
    async queryListAll() {
      await this.queryTestcaseByFuncId({
        necessaryFlag: this.newNecessaryFlag ? this.newNecessaryFlag : null,
        funcationPoint: this.functionPoint ? this.functionPoint : null,
        testcasePeople: this.testcasePeople ? this.testcasePeople : null,
        testcaseStatus: '21',
        testcaseType: this.testcaseType ? this.testcaseType : null,
        testcaseNature: this.testcaseNature ? this.testcaseNature : null,
        workNo: this.workNo ? this.workNo : null,
        testcaseDate: this.testcaseDate ? this.testcaseDate : null,
        testcaseFuncId: this.getTreeNodeDate ? this.getTreeNodeDate : null,
        pageSize: this.pageSize,
        currentPage: this.currentPage ? this.currentPage : null
      });
      this.ReuseTable = this.testcase;
    },

    //树形图点击
    async queryTestcastByFuncId(param) {
      await this.queryTestcaseByFuncId({
        testcaseStatus: '21',
        testcaseFuncId: param,
        currentPage: 1,
        pageSize: this.pageSize
      });
      this.ReuseTable = this.testcase;
      await this.countTestcaseByFuncId({
        funcationPoint: this.functionPoint ? this.functionPoint : null,
        testcasePeople: this.testcasePeople ? this.testcasePeople : null,
        testcaseStatus: '21',
        testcaseType: this.testcaseType ? this.testcaseType : null,
        testcaseNature: this.testcaseNature ? this.testcaseNature : null,
        workNo: this.workNo ? this.workNo : null,
        testcaseDate: this.testcaseDate ? this.testcaseDate : null,
        testcaseFuncId: param
      });
      this.total = this.countTest;
    },

    //单个复用
    async handleReuse(index, row) {
      await this.repeatedTestcaseForPlans({
        testcaseNo: this.ReuseTable[index].testcaseNo,
        planId: this.planId,
        workNo: sessionStorage.getItem('repeatWorkNo')
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '该案例复用成功!'
      });
    },

    //批量复用
    batchBtn() {
      if (this.multipleSelection.length <= 0) {
        this.$confirm('请选择要操作的案例', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        return;
      }
      this.$confirm('此操作将批量复用, 是否继续?', '提示', {
        confirmButtonText: '批量复用',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.batchRepeatedRelation({
          testcaseNo: this.multipleSelection,
          planId: this.planId,
          workNo: sessionStorage.getItem('repeatWorkNo')
        });
        this.$message({
          showClose: true,
          type: 'success',
          message: '批量复用成功!'
        });
      });
    }
  },
  async mounted() {
    const planIdData = JSON.parse(sessionStorage.getItem('planId'));
    this.planId = planIdData.planId;

    // 查询当前页,页面表格数据
    this.queryListAll();
    // 查询总数
    this.queryTotal();

    // 查询系统列表
    this.initlistAll();

    // 查询案例编写人
    await this.queryAllUserName();
    this.testcasePeopleList = this.allUserNameList;
  }
};
</script>

<style scoped>
.aside {
  width: 20vw;
  height: 100vh;
  overflow: auto;
  margin-left: 10%;
}
.page-container {
  width: 100%;
}
.el-scrollbar {
  height: 100%;
}
.el-scrollbar__wrap {
  overflow: scroll;
  overflow-x: auto;
  overflow-y: auto;
}
.tree >>> .el-tree-node__content {
  height: 30px;
}

.el-table {
  margin-top: 3%;
}
.operateBtn {
  margin-top: 30px;
}
.operateBtn .el-button {
  width: 150px;
}
.pagination {
  margin-top: 3%;
  margin-right: 5%;
}
.pagination >>> .el-pagination {
  float: right !important;
}

.batchCopy_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.batchCopy_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.batchCopy_dialog >>> .el-dialog__header {
  background: #409eff;
}

.batchCopy_dialog >>> .el-row {
  margin-top: 30px;
}

.el-main {
  margin-top: 20px;
}
.el-button >>> .el-icon-star-on {
  font-size: 18px;
  color: #f7ba2a;
}
</style>
