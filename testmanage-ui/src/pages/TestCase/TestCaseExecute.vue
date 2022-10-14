<template>
  <div class="body">
    <div class="carouseldiv">
      <el-carousel
        :interval="100000"
        arrow="always"
        :autoplay="autoPlay"
        height="400px"
        indicator-position="none"
      >
        <el-carousel-item v-for="(item, index) in testCasesList" :key="index">
          <el-container>
            <el-main>
              <el-card class="box-card">
                <div slot="header" class="clearfix">
                  <span>{{ item.testcaseName }}/</span>
                  <span>{{
                    item.testcaseNature == 1 ? '正案例' : '反案例'
                  }}</span>
                  <el-button
                    style="float: right; padding: 3px 0"
                    type="text"
                    @click="closePage"
                    >返回计划</el-button
                  >
                </div>
                <div>功能点: {{ item.funcationPoint }}</div>
                <div>案例描述: {{ item.testcaseDescribe }}</div>
                <div>{{ item.systemName }} {{ item.testcaseFuncName }}</div>
                <div class="status-button">
                  <!-- <el-button>批量</el-button> -->
                  <el-button
                    type="success"
                    @click="onclickUpdateExecuteStatus(item, 1, index)"
                    >通过</el-button
                  >
                  <el-button
                    type="warning"
                    @click="onclickUpdateExecuteStatus(item, 2, index)"
                    >阻塞</el-button
                  >
                  <el-button
                    type="danger"
                    @click="onclickUpdateExecuteStatus(item, 3, index)"
                    >失败</el-button
                  >
                  <el-button type="primary" @click="clickMantis(item)"
                    >提交Mantis</el-button
                  >
                </div>
              </el-card>
            </el-main>
            <el-aside width="300px">
              <el-input
                type="textarea"
                :rows="18"
                placeholder="请输入备注"
                v-model="item.remark"
              ></el-input>
            </el-aside>
          </el-container>
        </el-carousel-item>
      </el-carousel>
    </div>
    <div class="tagdiv">
      <el-tag
        v-for="(tag, index) in testCasesListTag"
        :key="index"
        :class="{
          green: tag.testcaseExecuteResult == 1,
          yellow: tag.testcaseExecuteResult == 2,
          red: tag.testcaseExecuteResult == 3
        }"
        @click="clicktag(tag)"
        effect="plain"
      >
        {{ tag.testcaseNo }}
        <!-- <span class="bottomTag">{{tag.testcaseNo}}</span> -->
      </el-tag>
    </div>
    <el-badge class="retreat">
      <el-button
        class="share-button"
        icon="el-icon-share"
        type="primary"
        @click="closePage()"
        >返回计划</el-button
      >
    </el-badge>

    <!-- mantis 提交弹框 -->
    <el-dialog
      title="编辑缺陷页面"
      :visible.sync="mantisDialog"
      width="62%"
      class="abow_dialog"
      :before-close="handleClose"
    >
      <el-form
        :inline="true"
        :model="mantisDialogAdd"
        :label-width="labelWidth"
        :rules="mantisDialogRules"
        ref="mantisDialogAdd"
        size="mini"
      >
        <el-row>
          <el-col :span="12">
            <el-row>
              <el-form-item label="项目名称" prop="project">
                <el-select
                  v-model="mantisDialogAdd.project"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in projectList"
                    :key="index"
                    :label="item.name"
                    :value="item.id"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="系统名称">
                <el-input
                  v-model="mantisDialogAdd.system_name"
                  autocomplete="off"
                  class="dialogMantis"
                  suffix-icon="xxx"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="功能模块">
                <el-select
                  v-model="mantisDialogAdd.function_module"
                  class="dialogMantis"
                  disabled
                ></el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="实施单元编号">
                <el-input
                  v-model="mantisDialogAdd.redmine_id"
                  autocomplete="off"
                  class="dialogMantis"
                  suffix-icon="xxx"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="工单号">
                <el-input
                  v-model="mantisDialogAdd.workNo"
                  autocomplete="off"
                  class="dialogMantis"
                  suffix-icon="xxx"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="系统版本" prop="system_version">
                <el-select
                  v-model="mantisDialogAdd.system_version"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionSV"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="归属阶段" prop="stage">
                <el-select
                  v-model="mantisDialogAdd.stage"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionBS"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="开发人责任人" prop="developer">
                <el-select
                  v-model="mantisDialogAdd.developer"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in developerList"
                    :key="index"
                    :value="item"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="分派给" prop="handler">
                <el-select
                  v-model="mantisDialogAdd.handler"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in handlerList"
                    :key="index"
                    :value="item.user_en_name"
                    :label="item.user_name_cn"
                  >
                    <span style="float: left">{{ item.user_name_cn }}</span>
                    <span style="float: right">{{ item.user_en_name }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-row>
          </el-col>

          <el-col :span="12">
            <el-row>
              <el-form-item label="优先级" prop="priority">
                <el-select
                  v-model="mantisDialogAdd.priority"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionPriority"
                    :key="index"
                    :label="item.value"
                    :value="item.label"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="严重性" prop="severity">
                <el-select
                  v-model="mantisDialogAdd.severity"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionSeverity"
                    :key="index"
                    :label="item.value"
                    :value="item.label"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="缺陷来源" prop="flaw_source">
                <el-select
                  v-model="mantisDialogAdd.flaw_source"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionDS"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="预计修复时间" prop="plan_fix_date">
                <el-date-picker
                  v-model="mantisDialogAdd.plan_fix_date"
                  type="date"
                  value-format="yyyy-MM-dd"
                  placeholder="请选择"
                  :picker-options="expireTimeOption"
                  style="width:90%"
                ></el-date-picker>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="缺陷类型" prop="flaw_type">
                <el-select
                  v-model="mantisDialogAdd.flaw_type"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionDT"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="上传文件 " class="filesAddCss">
                <el-upload
                  class="upload-demo"
                  :action="actionUrl"
                  :before-remove="beforeRemove"
                  accept=".txt, .jpg, .png"
                  :auto-upload="false"
                  multiple
                  :on-change="test"
                  ref="upload"
                  :file-list="fileList"
                >
                  <el-button size="small" type="primary">点击上传</el-button>
                  <div slot="tip" class="el-upload__tip">
                    只能上传txt/jpg/png文件
                  </div>
                </el-upload>
              </el-form-item>
            </el-row>
          </el-col>
          <el-col :span="24">
            <el-row>
              <el-form-item label="摘要" prop="summary">
                <el-input
                  type="textarea"
                  :rows="1"
                  maxlength="100"
                  class="inputWidth"
                  v-model="mantisDialogAdd.summary"
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="描述" prop="description">
                <el-input
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  class="inputWidth"
                  v-model="mantisDialogAdd.description"
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="开发原因分析">
                <el-input
                  type="textarea"
                  :rows="2"
                  maxlength="200"
                  show-word-limit
                  class="inputWidth"
                  v-model="mantisDialogAdd.reason"
                ></el-input>
              </el-form-item>
            </el-row>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelMantisAdd('mantisDialogAdd')">取 消</el-button>
        <el-button type="primary" @click="submitMantisAdd('mantisDialogAdd')"
          >提 交</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
// 案例执行 - 废弃页面  特此说明

import 'element-ui/lib/theme-chalk/index.css';
import { createMantisModel } from './model';
import { mapActions, mapState } from 'vuex';

export default {
  name: 'TestCaseExecute',
  data() {
    return {
      userMantisToken: sessionStorage.getItem('mantisToken'),
      expireTimeOption: {
        disabledDate(time) {
          return time.getTime() <= Date.now() - 24 * 60 * 60 * 1000;
        }
      },
      textarea: '111',
      //轮播数据
      testCasesList: [],
      funcationPoint: [],

      autoPlay: false,
      //标签按钮数据
      testCasesListTag: [],
      // 全局计划id
      planId: 0,
      // 全局计划id
      selStatus: 0,
      // 判断是否存在
      addStatus: false,
      mantisDialog: false,
      mantisDialogAdd: createMantisModel(),
      project: {},
      projectList: [],
      developerList: [],
      handlerList: [],
      labelWidth: '110px',
      optionSV: [
        { value: '内测版本', label: '内测版本' },
        { value: '业务版本', label: '业务版本' },
        { value: '准生产版本', label: '准生产版本' },
        { value: '灰度版本', label: '灰度版本' },
        { value: '生产版本', label: '生产版本' }
      ],
      optionBS: [
        { value: '编码阶段', label: '编码阶段' },
        { value: '集成阶段', label: '集成阶段' },
        { value: '设计阶段', label: '设计阶段' },
        { value: '需求阶段', label: '需求阶段' }
      ],
      optionPriority: [
        { value: '无', label: '10' },
        { value: '低', label: '20' },
        { value: '中', label: '30' },
        { value: '高', label: '40' },
        { value: '紧急', label: '50' },
        { value: '非常紧急', label: '60' }
      ],
      optionSeverity: [
        { value: '新功能', label: '10' },
        { value: '细节', label: '20' },
        { value: '文字', label: '30' },
        { value: '小调整', label: '40' },
        { value: '小错误', label: '50' },
        { value: '很严重', label: '60' },
        { value: '崩溃', label: '70' },
        { value: '宕机', label: '80' }
      ],
      optionDS: [
        { value: '需规问题', label: '需规问题' },
        { value: '功能实现不完整', label: '功能实现不完整' },
        { value: '功能实现错误', label: '功能实现错误' },
        { value: '历史遗留问题', label: '历史遗留问题' },
        { value: '优化建议', label: '优化建议' },
        { value: '后台问题', label: '后台问题' },
        { value: '打包问题', label: '打包问题' },
        { value: '数据问题', label: '数据问题' },
        { value: '环境问题', label: '环境问题' },
        { value: '其他原因', label: '其他原因' }
      ],
      optionDT: [
        { value: '需求问题', label: '需求问题' },
        { value: '环境问题', label: '环境问题' },
        { value: '数据问题', label: '数据问题' },
        { value: '文档问题', label: '文档问题' },
        { value: '应用缺陷', label: '应用缺陷' },
        { value: '其他问题', label: '其他问题' }
      ],
      unitNo: '',
      workNo: '',
      tuserName: '',
      developer: '',
      handler: '',
      summary: '',
      description: '',
      reason: '',
      actionUrl: '',
      fileList: [],
      Basedata: [],
      fileName: {},
      getFileData: [],
      mantisDialogRules: {
        project: [
          { required: true, message: '请选择项目名称', trigger: 'blur' }
        ],
        system_version: [
          { required: true, message: '请选择系统版本', trigger: 'blur' }
        ],
        stage: [{ required: true, message: '请选择归属阶段', trigger: 'blur' }],
        developer: [
          { required: true, message: '请选择开发人责任人', trigger: 'blur' }
        ],
        handler: [
          { required: true, message: '请输入分派人员', trigger: 'blur' }
        ],
        priority: [
          { required: true, message: '请选择优先级', trigger: 'blur' }
        ],
        severity: [
          { required: true, message: '请选择严重性', trigger: 'blur' }
        ],
        flaw_source: [
          { required: true, message: '请选择缺陷来源', trigger: 'blur' }
        ],
        plan_fix_date: [
          { required: true, message: '请选择预计修复时间', trigger: 'blur' }
        ],
        flaw_type: [
          { required: true, message: '请选择缺陷类型', trigger: 'blur' }
        ],
        summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
        description: [
          { required: true, message: '请输入描述', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    ...mapState('testCaseForm', [
      'testCasesArr',
      'testCaseDetailArr',
      'updatePlanArr'
    ]),
    ...mapState('mantisForm', ['reporterArr', 'developerArr', 'mantisProjects'])
  },
  methods: {
    ...mapActions('testCaseForm', [
      'queryByPlanIdAll',
      'testCaseDetailByTestcaseNoAndplanId',
      'updatePlanlistTestcaseRelation',
      'add'
    ]),
    ...mapActions('mantisForm', [
      'queryMantisProjects',
      'queryDevelopList',
      'queryAllUserName'
    ]),
    async initqueryByPlanIdAll(planId) {
      await this.queryByPlanIdAll({
        planId: this.planId
      });
      this.testCasesListTag = this.testCasesArr;
      if (this.selStatus === 0) {
        this.testCasesList = this.testCasesArr;
      }
    },

    //根据案例编号查询案例( 二 )
    async clicktag(tag) {
      this.addStatus = true;
      await this.testCaseDetailByTestcaseNoAndplanId({
        testcaseNo: tag.testcaseNo,
        planId: tag.planId
      });
      let res = this.testCaseDetailArr;
      this.testCasesList.forEach(item => {
        if (item.testcaseNo === tag.testcaseNo) {
          this.addStatus = false;
          this.$message({
            showClose: true,
            message: '已存在' + res[0].testcaseNo,
            type: 'error'
          });
          return;
        }
      });
      if (this.addStatus) {
        this.testCasesList.push(res[0]);
        this.$message({
          showClose: true,
          message: '添加成功' + res[0].testcaseNo,
          type: 'success'
        });
      }
    },
    //修改案例状态 根据 关系id 和案例状态 ( 三 )
    async onclickUpdateExecuteStatus(item, testcaseExecuteResult, index) {
      await this.updatePlanlistTestcaseRelation({
        testcaseNo: item.testcaseNo,
        planlistTestcaseId: item.planlistTestcaseId,
        testcaseExecuteResult: testcaseExecuteResult,
        remark: item.remark
      });
      this.selStatus = 10;
      this.testCasesListTag = this.updatePlanArr;
      this.testCasesList.splice(index, 1);
      await this.initqueryByPlanIdAll(item.planId);
      this.$message({
        showClose: true,
        message: this.updatePlanArr.msg,
        type: 'success'
      });
      // 操作下一个
    },
    //返回計劃
    closePage() {
      this.$router.push('/Plan');
    },

    async clickMantis(item) {
      this.userMantisToken = sessionStorage.getItem('mantisToken');
      //查询mantis项目
      this.projectList = await this.queryMantisProjects();
      // 查询开发人员
      this.developerList = await this.queryDevelopList();
      // 查询分派给
      this.handlerList = await this.queryAllUserName();
      this.mantisDialog = true;
      let getSystemName = item.systemName;
      let getTestcaseFuncName = item.testcaseFuncName;
      this.mantisDialogAdd.system_name = getSystemName;
      this.mantisDialogAdd.function_module = getTestcaseFuncName;
      this.mantisDialogAdd.redmine_id = this.unitNo;
      this.mantisDialogAdd.workNo = this.workNo;
    },
    handleClose() {
      this.mantisDialog = false;
    },

    async submitMantisAdd(mantisDialogAdd) {
      let res = [];
      if (this.fileList.length !== 0) {
        res = await this.getFile();
      }
      this.$refs[mantisDialogAdd].validate(async valid => {
        if (valid) {
          await this.add({
            project: this.mantisDialogAdd.project,
            system_name: this.mantisDialogAdd.system_name,
            function_module: this.mantisDialogAdd.function_module,
            redmine_id: this.mantisDialogAdd.redmine_id,
            workNo: this.mantisDialogAdd.workNo,
            system_version: this.mantisDialogAdd.system_version,
            stage: this.mantisDialogAdd.stage,
            developer: this.mantisDialogAdd.developer,
            handler: this.mantisDialogAdd.handler,
            priority: this.mantisDialogAdd.priority,
            severity: this.mantisDialogAdd.severity,
            flaw_source: this.mantisDialogAdd.flaw_source,
            plan_fix_date: this.mantisDialogAdd.plan_fix_date,
            flaw_type: this.mantisDialogAdd.flaw_type,
            summary: this.mantisDialogAdd.summary,
            description: this.mantisDialogAdd.description,
            reason: this.mantisDialogAdd.reason
              ? this.mantisDialogAdd.reason
              : '',
            files: res,
            mantis_token: this.userMantisToken
          });
          this.$message({
            showClose: true,
            message: '交易执行成功',
            type: 'success'
          });
          this.mantisDialog = false;
          this.$refs[mantisDialogAdd].resetFields();
          this.mantisDialogAdd = createMantisModel();
          this.$refs.upload.clearFiles();
        } else {
          return false;
        }
      });
    },

    cancelMantisAdd(mantisDialogAdd) {
      this.$refs[mantisDialogAdd].resetFields();
      this.mantisDialog = false;
      this.mantisDialogAdd = createMantisModel();
      this.$refs.upload.clearFiles();
    },

    //文件上传相关操作
    beforeRemove(file, fileList) {
      return this.$confirm(`确定移除 ${file.name}？`);
    },

    getBase64(file) {
      //把图片转成base64编码
      return new Promise(function(resolve, reject) {
        let reader = new FileReader();
        let imgResult = '';
        reader.readAsDataURL(file);
        reader.onload = function() {
          imgResult = reader.result;
        };
        reader.onerror = function(error) {
          reject(error);
        };
        reader.onloadend = function() {
          resolve(imgResult);
        };
      });
    },
    getFile() {
      return new Promise((resolve, reject) => {
        this.getFileData = [];
        this.fileList.forEach(async (file, index) => {
          const res = await this.getBase64(file.raw);
          this.getFileData.push({ name: file.name, content: res });
          if (index === this.fileList.length - 1) {
            resolve(this.getFileData);
          }
        });
      });
    },
    test(file, files) {
      this.fileList = files;
    }
  },
  mounted() {
    try {
      if (
        localStorage.getItem('userToken') == null ||
        localStorage.getItem('userToken') == ''
      ) {
        const urlToken = this.getUrlKey('token'); //fdev Token
        if (urlToken == null || urlToken == '') {
          this.$router.push({ path: '/Login' });
        } else {
          //this.oauthGetUser(urlToken);//第三方登陆获取用户信息
        }
      }
    } catch (error) {
      this.$router.push({ path: '/Login' });
    }

    //默认初始化
    this.$nextTick(() => {
      const planIdData = JSON.parse(sessionStorage.getItem('planId'));
      this.planId = planIdData.planId;
      this.initqueryByPlanIdAll(planIdData.planId);
    });
    //  拿实施单元编号，工单号
    const workOrderNoData = JSON.parse(
      sessionStorage.getItem('planWorkOrderNo')
    );
    this.unitNo = workOrderNoData.unitNo;
    this.workNo = workOrderNoData.workNo;

    sessionStorage.getItem('TuserName');
    this.tuserName = sessionStorage.getItem('TuserName');
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
  }
  //初始化当前计划下面的案例
};
</script>

<style scoped>
/* 绿色 */
.green {
  background-color: #67c23a;
}

/* 黄色 */
.yellow {
  background-color: #e6a23c;
}

/* 红色 */
.red {
  background-color: #f56c6c;
}

/*选项卡*/
.el-aside {
  height: 260px;
  margin: 0 auto;
  background-color: #d3dce6;
  color: #333;
  text-align: center;
  line-height: 100px;
}
.line {
  break-before: 2px;
  background-color: red;
}
.el-main {
  height: 350px;
  background-color: red;
  background-color: #e9eef3;
  color: #333;
  text-align: center;
  line-height: 30px;
  overflow: scroll;
}

/*卡片*/
.text {
  font-size: 8px;
}

.item {
  margin-bottom: 18px;
}

.clearfix {
  height: 10px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: '';
}

.clearfix:after {
  clear: both;
}

.box-card {
  height: 90%;
  margin: 0 auto;
  width: 90%;
}

/*备注*/
.el-col {
  border-radius: 4px;
  height: 80%;
}

.bg-purple-dark {
  background: #99a9bf;
}

.bg-purple {
  background: #d3dce6;
}

.bg-purple-light {
  background: #e5e9f2;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
}

.row-bg {
  padding: 10px 0;
  background-color: #f9fafc;
}

.status-button {
  margin-top: 20px;
}

.tagdiv {
  margin-left: 10px;
}

.tagdiv >>> .el-tag--plain {
  margin-right: 8px;
}

.carouseldiv {
  height: 400px;
  width: 100%;
}

.el-carousel {
  margin-top: 70px;
  top: 1px;
  width: 100%;
  z-index: 10;
  position: fixed;
}
.textarea {
  overflow: scroll;
}
.retreat {
  top: 95px;
  position: fixed;
  z-index: 100;
  right: 400px;
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
  left: 0px;
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
.inputWidth {
  width: 680px;
}

.inputWidth >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
}

.dialogMantis >>> .el-input__inner {
  color: #000 !important;
}

.filesAddCss >>> .el-icon-close:before {
  color: gray;
  font-size: 16px;
  font-weight: 500;
}
</style>
