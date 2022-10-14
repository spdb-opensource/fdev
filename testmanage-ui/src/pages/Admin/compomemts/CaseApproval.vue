<template>
  <div class="page-container">
    <el-form
      :model="caseModel"
      :rules="caseRules"
      ref="caseModel"
      :inline="true"
      class="demo-form-inline"
      label-width="80px"
    >
      <el-row>
        <el-col :span="5" :offset="1">
          <el-form-item>
            <el-input
              v-model="testcaseNo"
              placeholder="请输入案例编号"
              suffix-icon="XXX"
              clearable
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5" :offset="1">
          <el-form-item prop="systemName">
            <el-select
              v-model="caseModel.systemName"
              @change="getValue"
              placeholder="请选择系统名称"
              filterable
              clearable
            >
              <el-option
                v-for="(item, index) in systemList"
                :key="index"
                :label="item.sys_module_name"
                :value="item.sys_id"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5" :offset="1">
          <el-form-item>
            <el-input
              v-model="testcaseName"
              placeholder="请输入案例名称"
              clearable
            ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="5" :offset="1">
          <el-form-item>
            <el-input
              v-model="work_no"
              placeholder="请输入工单编号"
              suffix-icon="xxx"
              clearable
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5" :offset="1">
          <el-form-item prop="funcMenu">
            <el-select
              v-model="caseModel.funcMenu"
              placeholder="请选择功能模块"
              filterable
              clearable
            >
              <el-option
                v-for="(item, index) in funcMenuList"
                :key="index"
                :label="item.func_model_name"
                :value="item.func_id"
              >
              </el-option>
            </el-select>
          </el-form-item>
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
                v-for="(item, index) in userList"
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
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" @click="clickQuery()">
              查询
            </el-button>
            <el-button
              type="primary"
              @click="updateToGeneralCaseList('all')"
              v-if="userRole > 20 && typeApproval === 'generalApproval'"
            >
              批量通用
            </el-button>
            <el-button
              type="primary"
              @click="updateToDiscardCaseList('all')"
              v-if="userRole > 20 && typeApproval === 'discardApproval'"
            >
              批量废弃
            </el-button>
            <el-button
              type="primary"
              @click="agreeThrough('all')"
              v-if="userRole > 20 && typeApproval === 'passApproval'"
            >
              批量通过
            </el-button>
            <el-button
              type="primary"
              @click="rejectThrough('all')"
              v-if="userRole > 20 && typeApproval === 'passApproval'"
            >
              批量拒绝
            </el-button>
            <el-button
              type="primary"
              @click="agreeEffect('all')"
              v-if="userRole > 20 && typeApproval === 'effectiveApproval'"
            >
              批量通过
            </el-button>
            <el-button
              type="primary"
              @click="rejectEffect('all')"
              v-if="userRole > 20 && typeApproval === 'effectiveApproval'"
            >
              批量拒绝
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-table
      :data="caseApprovalData"
      style="width: 100%;margin-bottom: 20px;color:black"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      :header-cell-style="{ color: '#545c64' }"
      ref="table"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="testcaseNo" label="案例编号" width="150">
        <template slot-scope="scope">
          <el-link type="primary" @click="testcaseDetail(scope.row)">
            {{ scope.row.testcaseNo }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="testcaseName" label="案例名称"></el-table-column>
      <el-table-column
        prop="testcaseType"
        label="案例类型"
        width="80"
        :formatter="formatterTestcaseType"
      ></el-table-column>
      <el-table-column
        prop="testcaseNature"
        label="案例性质"
        width="80"
        :formatter="formatterTestcaseNature"
      ></el-table-column>
      <el-table-column
        prop="testcaseFuncName"
        label="功能模块"
        width="110"
      ></el-table-column>
      <el-table-column prop="funcationPoint" label="功能点"></el-table-column>
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
      <el-table-column label="操作" width="90">
        <template slot-scope="scope">
          <el-button
            @click="updateToGeneralCaseList(scope.row.testcaseNo)"
            type="text"
            size="small"
            v-if="userRole > 20 && typeApproval === 'generalApproval'"
          >
            通用
          </el-button>
          <el-button
            @click="updateToDiscardCaseList(scope.row.testcaseNo)"
            type="text"
            size="small"
            v-if="userRole > 20 && typeApproval === 'discardApproval'"
          >
            废弃
          </el-button>
          <el-button
            @click="agreeThrough(scope.row.testcaseNo)"
            type="text"
            size="small"
            v-if="userRole > 20 && typeApproval === 'passApproval'"
            >通过</el-button
          >
          <el-button
            @click="rejectThrough(scope.row.testcaseNo)"
            type="text"
            size="small"
            v-if="userRole > 20 && typeApproval === 'passApproval'"
            >拒绝</el-button
          >
          <el-button
            @click="agreeEffect(scope.row.testcaseNo)"
            type="text"
            size="small"
            v-if="userRole > 20 && typeApproval === 'effectiveApproval'"
            >通过</el-button
          >
          <el-button
            @click="rejectEffect(scope.row.testcaseNo)"
            type="text"
            size="small"
            v-if="userRole > 20 && typeApproval === 'effectiveApproval'"
            >拒绝</el-button
          >
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
        :pager-count="5"
        layout="sizes, prev, pager, next, jumper"
        :total="total"
      ></el-pagination>
    </div>

    <!-- 案例详情弹窗 -->
    <el-dialog
      title="案例详情页"
      :visible.sync="caseDetails"
      width="60%"
      class="abow_dialog"
    >
      <div>
        <el-form
          :inline="true"
          :model="caseDetailsModel"
          :label-width="labelWidth"
          size="mini"
        >
          <el-row>
            <el-col :span="12">
              <el-row>
                <el-form-item label="案例编号 ：">
                  <el-input
                    v-model="caseDetailsModel.testcaseNo"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例名称 ：">
                  <el-input
                    v-model="caseDetailsModel.testcaseName"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例类型 ：">
                  <el-select
                    v-model="caseDetailsModel.testcaseType"
                    placeholder="请选择"
                    class="detailsDialog"
                    disabled
                  >
                    <el-option label="页面" value="1"></el-option>
                    <el-option label="功能" value="2"></el-option>
                    <el-option label="流程" value="3"></el-option>
                    <el-option label="链接" value="4"></el-option>
                    <el-option label="接口" value="5"></el-option>
                    <el-option label="批量" value="6"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="优先级 ：">
                  <el-select
                    v-model="caseDetailsModel.testcasePriority"
                    placeholder="请选择"
                    class="detailsDialog"
                    disabled
                  >
                    <el-option label="高" value="1"></el-option>
                    <el-option label="中" value="2"></el-option>
                    <el-option label="低" value="3"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例性质 ：">
                  <el-select
                    v-model="caseDetailsModel.testcaseNature"
                    placeholder="请选择"
                    class="detailsDialog"
                    disabled
                  >
                    <el-option label="正案例" value="1"></el-option>
                    <el-option label="反案例" value="2"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
            </el-col>

            <el-col :span="12">
              <el-row>
                <el-form-item label="系统名称 ：">
                  <el-input
                    v-model="caseDetailsModel.systemName"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="功能模块 ：">
                  <el-select
                    v-model="caseDetailsModel.testcaseFuncName"
                    class="detailsDialog"
                    disabled
                  >
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="功能点 ：">
                  <el-input
                    type="textarea"
                    v-model="caseDetailsModel.funcationPoint"
                    class="detailsDialogFunction"
                    :rows="6"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
            </el-col>
            <el-col :span="24">
              <el-row>
                <el-form-item label="前置条件 ：">
                  <el-input
                    type="textarea"
                    :rows="2"
                    class="detailsDialogInput"
                    v-model="caseDetailsModel.testcasePre"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例描述 ：">
                  <el-input
                    type="textarea"
                    :rows="7"
                    class="detailsDialogInput"
                    v-model="caseDetailsModel.testcaseDescribe"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="预期结果 ：">
                  <el-input
                    type="textarea"
                    :rows="6"
                    class="detailsDialogInput"
                    v-model="caseDetailsModel.expectedResult"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="caseDetails = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { rules } from '../model';
import { validate } from '@/common/utlis';
export default {
  name: 'CaseApproval',
  data() {
    return {
      caseModel: {
        systemName: '',
        funcMenu: ''
      },
      caseApprovalData: [],
      //用户权限
      userRole: sessionStorage.getItem('Trole'),
      testcaseNo: '',
      // systemName: '',
      testcaseName: '',
      testcaseType: '',
      // funcMenu: '',
      work_no: '',
      testcasePeople: '',
      currentPage: 1,
      pagesize: 5,
      total: 0,
      caseDetailsModel: {},
      caseDetails: false,
      labelWidth: '100px',
      TypeApproval: {
        generalApproval: '21',
        discardApproval: '21',
        passApproval: '10',
        effectiveApproval: '20'
      }
    };
  },
  props: {
    typeApproval: {
      type: String,
      required: true
    }
  },
  computed: {
    ...mapState('adminForm', [
      'systemList',
      'detailCase',
      'funcMenuList',
      'testcaseData',
      'testcaseCount',
      'userList'
    ]),
    caseRules() {
      return this.rules(this.caseModel);
    }
  },
  methods: {
    ...mapActions('menu', ['getUserApprovalList']),
    ...mapActions('adminForm', [
      'updateToGeneralCase',
      'queryAllSystem',
      'QueryDetailByTestcaseNo',
      'queryFuncMenuBySysId',
      'queryTestcaseByOption',
      'queryAllUserName',
      'agreeCaseThrough',
      'rejectCaseThrough',
      'agreeCaseEffect',
      'rejectCaseEffect',
      'updateToDiscardCase'
    ]),
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
    async clickQuery() {
      if (!this.caseModel.systemName && !this.caseModel.funcMenu) {
        this.$refs['caseModel'].clearValidate();
      } else {
        await validate(this.$refs['caseModel']);
      }
      this.queryList(1);
    },
    async queryList(val) {
      this.currentPage = val;
      await this.queryTestcaseByOption({
        testcaseStatus: this.TypeApproval[this.typeApproval],
        testcaseNo: this.testcaseNo ? this.testcaseNo : null,
        systemName: this.caseModel.systemName
          ? this.caseModel.systemName
          : null,
        testcaseName: this.testcaseName ? this.testcaseName : null,
        testcaseFuncId: this.caseModel.funcMenu
          ? this.caseModel.funcMenu
          : null,
        work_no: this.work_no ? this.work_no : null,
        testcasePeople: this.testcasePeople ? this.testcasePeople : null,
        pagesize: this.pagesize,
        pageNum: this.currentPage ? this.currentPage : null
      });
      this.caseApprovalData = this.testcaseData.list;
      this.total = this.testcaseData.total;
    },
    async getValue() {
      if (this.caseModel.systemName) {
        await this.queryFuncMenuBySysId({ sys_id: this.caseModel.systemName });
        this.caseModel.funcMenu = null;
      }
    },
    handleSizeChange: function(size) {
      this.pagesize = size;
      this.queryListAll();
    },
    //下一页
    handleCurrentChange(val) {
      this.currentPage = val;
      this.queryList(this.currentPage);
    },
    //查询当前用户可审批的案例
    async queryListAll() {
      await this.queryTestcaseByOption({
        testcaseStatus: this.TypeApproval[this.typeApproval],
        testcaseNo: this.testcaseNo ? this.testcaseNo : null,
        systemName: this.caseModel.systemName
          ? this.caseModel.systemName
          : null,
        testcaseName: this.testcaseName ? this.testcaseName : null,
        testcaseFuncId: this.caseModel.funcMenu
          ? this.caseModel.funcMenu
          : null,
        work_no: this.work_no ? this.work_no : null,
        testcasePeople: this.testcasePeople ? this.testcasePeople : null,
        pageNum: this.currentPage,
        pagesize: this.pagesize
      });
      this.caseApprovalData = this.testcaseData.list;
      this.total = this.testcaseData.total;
    },
    // 通用审批页面 通用功能
    async updateToGeneralCaseList(testcaseNo) {
      let testcaseNos = [];
      if (testcaseNo !== 'all') {
        testcaseNos = [testcaseNo];
      } else {
        if (this.$refs.table.selection.length === 0) {
          this.$message({
            showClose: true,
            type: 'warning',
            message: '请选择案例!'
          });
          return;
        }
        testcaseNos = this.$refs.table.selection.map(item => item.testcaseNo);
      }
      await this.updateToGeneralCase({
        testcaseNos: testcaseNos,
        testcaseStatus: '30'
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '生效案例已通用!'
      });
      if (
        this.pagesize * (this.currentPage - 1) + 1 >= this.total &&
        this.total !== 1
      ) {
        this.currentPage -= 1;
        this.queryListAll();
      } else {
        this.queryListAll();
      }
    },

    // 废弃审批页面 废弃功能
    async updateToDiscardCaseList(testcaseNo) {
      let testcaseNos = [];
      if (testcaseNo !== 'all') {
        testcaseNos = [testcaseNo];
      } else {
        if (this.$refs.table.selection.length === 0) {
          this.$message({
            showClose: true,
            type: 'warning',
            message: '请选择案例!'
          });
          return;
        }
        testcaseNos = this.$refs.table.selection.map(item => item.testcaseNo);
      }
      await this.updateToDiscardCase({
        testcaseNos: testcaseNos,
        testcaseStatus: '40'
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '该生效案例已废弃!'
      });
      if (
        this.pagesize * (this.currentPage - 1) + 1 >= this.total &&
        this.total !== 1
      ) {
        this.currentPage -= 1;
        this.queryListAll();
      } else {
        this.queryListAll();
      }
    },

    // 评审审批页面 通过功能
    async agreeThrough(testcaseNo) {
      let testcaseNos = [];
      if (testcaseNo !== 'all') {
        testcaseNos = [testcaseNo];
      } else {
        if (this.$refs.table.selection.length === 0) {
          this.$message({
            showClose: true,
            type: 'warning',
            message: '请选择案例!'
          });
          return;
        }
        testcaseNos = this.$refs.table.selection.map(item => item.testcaseNo);
      }
      await this.agreeCaseThrough({
        testcaseNos: testcaseNos,
        testcaseStatus: '11'
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '审核通过!'
      });
      this.getUserApprovalList();
      if (
        this.pagesize * (this.currentPage - 1) + 1 >= this.total &&
        this.total !== 1
      ) {
        this.currentPage -= 1;
        this.queryListAll();
      } else {
        this.queryListAll();
      }
    },

    // 评审审批页面 拒绝功能
    async rejectThrough(testcaseNo) {
      let testcaseNos = [];
      if (testcaseNo !== 'all') {
        testcaseNos = [testcaseNo];
      } else {
        if (this.$refs.table.selection.length === 0) {
          this.$message({
            showClose: true,
            type: 'warning',
            message: '请选择案例!'
          });
          return;
        }
        testcaseNos = this.$refs.table.selection.map(item => item.testcaseNo);
      }
      await this.rejectCaseThrough({
        testcaseNos: testcaseNos,
        testcaseStatus: '12'
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '审核拒绝!'
      });
      this.getUserApprovalList();
      if (
        this.pagesize * (this.currentPage - 1) + 1 >= this.total &&
        this.total !== 1
      ) {
        this.currentPage -= 1;
        this.queryListAll();
      } else {
        this.queryListAll();
      }
    },

    // 生效审批页面 通过功能
    async agreeEffect(testcaseNo) {
      let testcaseNos = [];
      if (testcaseNo !== 'all') {
        testcaseNos = [testcaseNo];
      } else {
        if (this.$refs.table.selection.length === 0) {
          this.$message({
            showClose: true,
            type: 'warning',
            message: '请选择案例!'
          });
          return;
        }
        testcaseNos = this.$refs.table.selection.map(item => item.testcaseNo);
      }
      await this.agreeCaseEffect({
        testcaseNos: testcaseNos,
        testcaseStatus: '21'
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '生效通过!'
      });
      this.getUserApprovalList();
      if (
        this.pagesize * (this.currentPage - 1) + 1 >= this.total &&
        this.total !== 1
      ) {
        this.currentPage -= 1;
        this.queryListAll();
      } else {
        this.queryListAll();
      }
    },

    // 生效审批页面 拒绝功能
    async rejectEffect(testcaseNo) {
      let testcaseNos = [];
      if (testcaseNo !== 'all') {
        testcaseNos = [testcaseNo];
      } else {
        if (this.$refs.table.selection.length === 0) {
          this.$message({
            showClose: true,
            type: 'warning',
            message: '请选择案例!'
          });
          return;
        }
        testcaseNos = this.$refs.table.selection.map(item => item.testcaseNo);
      }
      await this.rejectCaseEffect({
        testcaseNos: testcaseNos,
        testcaseStatus: '22'
      });
      this.$message({
        showClose: true,
        type: 'success',
        message: '生效拒绝!'
      });
      this.getUserApprovalList();
      if (
        this.pagesize * (this.currentPage - 1) + 1 >= this.total &&
        this.total !== 1
      ) {
        this.currentPage -= 1;
        this.queryListAll();
      } else {
        this.queryListAll();
      }
    },

    // 案例详情弹框
    async testcaseDetail(row) {
      this.caseDetails = true;
      await this.QueryDetailByTestcaseNo({
        testcaseNo: row.testcaseNo
      });
      this.caseDetailsModel = this.detailCase ? this.detailCase : {};
    }
  },

  mounted() {
    this.queryListAll();
    this.queryAllSystem();
    // 查询案例编写人
    this.queryAllUserName();
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

.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.abow_dialog >>> .el-dialog__header {
  background: #409eff;
  padding: 15px 20px 10px;
}

.abow_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.abow_dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.abow_dialog >>> .el-dialog {
  margin: 0 auto !important;
  height: 95%;
  overflow: hidden;
}

.abow_dialog >>> .el-dialog__body {
  position: absolute;
  left: 10px;
  top: 60px;
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

.detailsDialogInput {
  width: 380%;
}

.detailsDialogInput >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
  color: #000 !important;
}

.detailsDialog >>> .el-input__inner {
  color: #000 !important;
}

.detailsDialogFunction {
  width: 110%;
}
.detailsDialogFunction >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
  color: #000 !important;
}
</style>
