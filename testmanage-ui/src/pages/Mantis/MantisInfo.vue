<template>
  <div class="page-container">
    <div class="headerCSS">
      <span>缺陷详情</span>
    </div>
    <el-form :model="mantisDialogDetail" label-width="50%" class="container">
      <el-row>
        <el-col :span="12">
          <el-form-item label="项目名称 ： ">
            <el-col>{{ mantisDialogDetail.project_name }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="系统名称 ： ">
            <el-col>{{ mantisDialogDetail.system_name }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="功能模块 ： ">
            <el-col>{{ mantisDialogDetail.function_module }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="需求编号/实施单元编号 ： ">
            <el-col>{{ mantisDialogDetail.redmine_id }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="工单号 ： ">
            <el-col>{{ mantisDialogDetail.workNo }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="系统版本 ： ">
            <el-col>{{ mantisDialogDetail.system_version }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="归属阶段 ： ">
            <el-col>{{ mantisDialogDetail.stage }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开发人责任人： ">
            <el-col>{{ mantisDialogDetail.developer }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="分派给 ： ">
            <el-col>{{ mantisDialogDetail.handler }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报告人 ： ">
            <el-col>{{ mantisDialogDetail.reporter }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="优先级 ：">
            <el-col>{{ mantisDialogDetail.priority | formatPriority }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="严重性 ： ">
            <el-col>{{ mantisDialogDetail.severity | formatSeverity }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="缺陷来源 ： ">
            <el-col>{{ mantisDialogDetail.flaw_source }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计修复时间 ： ">
            <el-col>{{ mantisDialogDetail.plan_fix_date }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="缺陷类型 ： ">
            <el-col>{{ mantisDialogDetail.flaw_type }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="缺陷编号 ： ">
            <el-col>{{ mantisDialogDetail.id }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="状态 ： ">
            <el-col>{{ mantisDialogDetail.status | formatStatus }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="创建时间 ： ">
            <el-col>{{ mantisDialogDetail.date_submitted }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="文件列表 ： ">
            <ul class="filesListUl">
              <li
                v-for="(file, index) in filesContent"
                :key="index"
                class="filesList"
              >
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="点击下载"
                  placement="top"
                >
                  <i
                    class="el-icon-download deleteFile"
                    @click="downloadFileData(file)"
                  ></i>
                </el-tooltip>
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="点击预览"
                  placement="top"
                >
                  <span @click="clickFile(file)">{{ file.name }}</span>
                </el-tooltip>
              </li>
            </ul>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="摘要 ： ">
            <el-col>{{ mantisDialogDetail.summary }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="描述 ： ">
            <el-col>{{ mantisDialogDetail.description }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开发原因分析 ： ">
            <el-col>{{ mantisDialogDetail.reason }}</el-col>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="重新打开原因 ： ">
            <el-col>{{ mantisDialogDetail.reopen_reason }}</el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-form-item>
          <el-button type="primary" @click="editData(mantisDialogDetail)"
            >编辑</el-button
          >
        </el-form-item>
      </el-row>
    </el-form>
    <!-- 文件预览弹框 -->
    <el-dialog :visible.sync="filePreviewDialog" class="filePreviewDialog">
      <div v-html="filePreview">{{ filePreview }}</div>
    </el-dialog>
    <!-- 编辑缺陷弹窗 -->
    <el-dialog
      title="编辑缺陷页"
      :visible.sync="mantisEditDialog"
      width="62%"
      class="abow_dialog"
    >
      <el-form
        :inline="true"
        :model="mantisDialogEdit"
        :label-width="labelWidth"
        :rules="mantisDialogRules"
        ref="mantisDialogEdit"
        size="mini"
      >
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="project_name">
              <t-select
                filterable
                class="dialogMantis"
                :options="projectList"
                v-model="mantisDialogEdit.project_id"
                option-label="name"
                option-value="id"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.name }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="系统名称aaa">
              <el-input
                v-model="mantisDialogEdit.system_name"
                autocomplete="off"
                class="dialogMantis"
                suffix-icon="xxx"
                disabled
              ></el-input>
            </el-form-item>
            <el-form-item label="功能模块">
              <el-select
                v-model="mantisDialogEdit.function_module"
                placeholder
                class="dialogMantis"
                disabled
                filterable
                clearable
              ></el-select>
            </el-form-item>
            <el-form-item label="需求编号/实施单元编号">
              <el-input
                v-model="mantisDialogEdit.redmine_id"
                autocomplete="off"
                class="dialogMantis"
                suffix-icon="xxx"
                disabled
              ></el-input>
            </el-form-item>
            <el-form-item label="工单号">
              <el-input
                v-model="mantisDialogEdit.workNo"
                autocomplete="off"
                class="dialogMantis"
                suffix-icon="xxx"
                disabled
              ></el-input>
            </el-form-item>
            <el-form-item label="系统版本" prop="system_version">
              <t-select
                filterable
                class="dialogMantis"
                :options="optionSV"
                v-model="mantisDialogEdit.system_version"
                option-label="lalel"
                option-value="value"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.lalel }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="归属阶段" prop="stage">
              <t-select
                filterable
                class="dialogMantis"
                :options="optionBS"
                v-model="mantisDialogEdit.stage"
                option-label="lalel"
                option-value="value"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.lalel }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="开发人责任人">
              <t-select
                filterable
                class="dialogMantis"
                :options="developerList"
                v-model="mantisDialogEdit.developer"
                option-label="label"
                option-value="value"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.label }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="分派给" prop="handler_en_name">
              <t-select
                filterable
                class="dialogMantis"
                :options="handlerList"
                v-model="mantisDialogEdit.handler_en_name"
                option-label="user_name_cn"
                option-value="user_en_name"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span class="option-left">{{ item.user_name_cn }}</span>
                  <span class="option-right">{{ item.user_en_name }}</span>
                </template>
              </t-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <t-select
                filterable
                class="dialogMantis"
                :options="optionPriority"
                v-model="mantisDialogEdit.priority"
                option-label="value"
                option-value="label"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.value }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="严重性" prop="severity">
              <t-select
                filterable
                class="dialogMantis"
                :options="optionSeverity"
                v-model="mantisDialogEdit.severity"
                option-label="value"
                option-value="label"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.value }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="缺陷来源" prop="flaw_source">
              <t-select
                filterable
                class="dialogMantis"
                :options="optionDSS"
                v-model="mantisDialogEdit.flaw_source"
                option-label="lalel"
                option-value="value"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.lalel }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="预计修复时间" prop="plan_fix_date">
              <el-date-picker
                v-model="mantisDialogEdit.plan_fix_date"
                type="date"
                value-format="yyyy-MM-dd"
                style="width:90%"
                class="dialogMantis"
                :picker-options="expireTimeOption"
                :disabled="this.reporterPower"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="缺陷类型" prop="flaw_type">
              <t-select
                filterable
                class="dialogMantis"
                :options="optionDTS"
                v-model="mantisDialogEdit.flaw_type"
                option-label="lalel"
                option-value="value"
                :full-width="false"
                clearable
                :disabled="this.reporterPower"
              >
                <template v-slot:options="item">
                  <span>{{ item.lalel }}</span>
                </template>
              </t-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select
                v-model="mantisDialogEdit.status"
                class="dialogMantis"
                placeholder
                filterable
                clearable
                :disabled="this.reporterPower && this.handlerPower"
              >
                <el-option
                  v-for="(item, index) in optionStatus"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                  :disabled="item.disabled"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="缺陷编号" prop="id">
              <el-input
                v-model="mantisDialogEdit.id"
                autocomplete="off"
                class="dialogMantis"
                suffix-icon="xxx"
                disabled
              ></el-input>
            </el-form-item>

            <el-form-item label="创建时间">
              <el-date-picker
                v-model="mantisDialogEdit.date_submitted"
                type="date"
                value-format="yyyy-MM-dd"
                style="width:90%"
                class="dialogMantis"
                disabled
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="报告人">
              <t-select
                filterable
                class="dialogMantis"
                :options="reporterList"
                v-model="mantisDialogEdit.reporter"
                option-label="user_name_cn"
                option-value="user_en_name"
                :full-width="false"
                clearable
                disabled
              >
                <template v-slot:options="item">
                  <span>{{ item.user_name_cn }}</span>
                </template>
              </t-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-row>
              <el-form-item label="摘要" prop="summary">
                <el-input
                  type="textarea"
                  :rows="1"
                  class="detailsDialogInput"
                  v-model="mantisDialogEdit.summary"
                  :disabled="this.reporterPower"
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="描述" prop="description">
                <el-input
                  type="textarea"
                  :rows="3"
                  class="detailsDialogInput"
                  v-model="mantisDialogEdit.description"
                  :disabled="this.reporterPower"
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="开发原因分析" prop="reason">
                <el-input
                  type="textarea"
                  :rows="2"
                  class="detailsDialogInput"
                  v-model="mantisDialogEdit.reason"
                  maxlength="50"
                  :disabled="this.reporterPower && this.handlerPower"
                ></el-input>
              </el-form-item>
            </el-row>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="mantisEditDialog = false">关 闭</el-button>
        <el-button type="primary" @click="sbimitMantisEdit('mantisDialogEdit')"
          >提 交</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { exportExcel } from '@/common/utlis';
import { mapActions, mapState } from 'vuex';
import {
  optionSV,
  optionDT,
  optionDTSA,
  optionBS,
  optionPriority,
  optionSeverity,
  optionDS,
  optionDSA,
  optionStatus
} from './model.js';
export default {
  name: 'MantisInfo',
  data() {
    return {
      expireTimeOption: {
        disabledDate(time) {
          return time.getTime() <= Date.now() - 24 * 60 * 60 * 1000;
        }
      },
      filePreview: null,
      MantisId: '',
      mantisDialogDetail: {},
      filesContent: [],
      filePreviewDialog: false,
      mantisDialogEdit: {},
      mantisEditDialog: false,
      labelWidth: '150px',
      projectList: [],
      developerList: [],
      handlerList: [],
      reporterPower: true,
      handlerPower: true,
      reporterList: {
        user_en_name: '',
        user_name_cn: ''
      },
      userEnName: localStorage.getItem('user_en_name'),
      userMantisToken: localStorage.getItem('mantisToken'),
      TuserName: localStorage.getItem('TuserName'),
      reporter_en_name: '',
      handler_en_name: '',
      mantisDialogRules: {
        project_name: [
          { required: true, message: '请选择项目名称', trigger: 'blur' }
        ],
        system_version: [
          { required: true, message: '请选择系统版本', trigger: 'blur' }
        ],
        stage: [{ required: true, message: '请选择归属阶段', trigger: 'blur' }],
        handler: [
          { required: true, message: '请输入分派人员', trigger: 'blur' }
        ],
        handler_en_name: [
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
        id: [{ required: true, message: '请输入缺陷编号', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'blur' }],
        summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
        description: [
          { required: true, message: '请输入描述', trigger: 'blur' }
        ]
      },
      optionSV,
      optionBS,
      optionPriority,
      optionSeverity,
      optionDS,
      optionDSA,
      optionDT,
      optionDTSA,
      optionStatus
    };
  },
  computed: {
    ...mapState('mantisForm', [
      'mantisInfo',
      'mantisProjects',
      'reporterArr',
      'developerArr'
    ]),
    optionDTS() {
      if (
        this.mantisDialogEdit &&
        this.mantisDialogEdit.orderType == 'security'
      ) {
        return optionDTSA;
      } else {
        return optionDT;
      }
    },
    optionDSS() {
      if (
        this.mantisDialogEdit &&
        this.mantisDialogEdit.orderType == 'security'
      ) {
        return optionDSA;
      } else {
        return optionDS;
      }
    }
  },
  methods: {
    ...mapActions('mantisForm', [
      'queryMantisInfo',
      'update',
      'queryMantisProjects',
      'queryAllUserName',
      'queryDevelopList'
    ]),
    async queryMantisInfoFun() {
      await this.queryMantisInfo({
        id: this.MantisId
      });
      this.mantisDialogDetail = this.mantisInfo;
      this.filesContent = this.mantisInfo.files;
      this.reporter_en_name = this.mantisInfo.reporter_en_name;
      this.handler_en_name = this.mantisInfo.handler_en_name;
    },
    downloadFileData(file) {
      const fileContent = file.content; //文件内容
      const type = file.file_type; // 文件下载格式
      const fileName = file.name; // 文件名
      let blob = new Blob([fileContent], { type: type }); // text格式,直接转blob
      if (type.includes('image')) {
        // 判断是否是图片类型
        var bstr = window.atob(fileContent),
          n = bstr.length,
          u8arr = new Uint8Array(n);
        while (n--) {
          u8arr[n] = bstr.charCodeAt(n);
        }
        blob = new Blob([u8arr], { type: type }); //把转码后的图片转成blob
      }
      const res = {
        data: blob
      };
      exportExcel(res, fileName, type);
    },
    clickFile(file) {
      this.filePreviewDialog = true;
      // let model = window.open("", "model");
      if (file.file_type.includes('image')) {
        let type = file.file_type.includes('image/jpeg') ? 'jpeg' : 'png';
        // model.document.write(
        this.filePreview = `<image style='width:650px;' src='data:image/${type};base64,${file.content}'/>`;
        // );
      } else {
        this.filePreview = file.content.replace(/\n/g, '</br>');
      }
    },
    editData(row) {
      this.mantisEditDialog = true;
      this.mantisDialogEdit = Object.assign({}, row);
      this.optionStatus = [
        { value: '10', label: '新建', disabled: this.handlerSelectPower() },
        { value: '20', label: '拒绝' },
        { value: '30', label: '确认拒绝', disabled: this.handlerSelectPower() },
        { value: '40', label: '延迟修复' },
        { value: '50', label: '打开' },
        { value: '80', label: '已修复' },
        { value: '90', label: '关闭', disabled: this.handlerSelectPower() }
      ];
      if (this.reporter_en_name == this.userEnName) {
        this.reporterPower = false;
      } else if (
        this.reporter_en_name != this.userEnName &&
        this.handler_en_name == this.userEnName
      ) {
        this.handlerPower = false;
      }
    },
    handlerSelectPower() {
      if (
        this.reporter_en_name != this.userEnName &&
        this.handler_en_name == this.userEnName
      ) {
        return true;
      }
    },
    async sbimitMantisEdit(mantisDialogEdit) {
      this.$refs[mantisDialogEdit].validate(async valid => {
        if (valid) {
          let i = '';
          let len = '';
          for (i = 0, len = this.handlerList.length; i < len; i++) {
            if (
              this.mantisDialogEdit.handler_en_name ==
              this.handlerList[i].user_en_name
            ) {
              this.mantisDialogEdit.handler = this.handlerList[i].user_name_cn;
            }
          }
          for (i = 0, len = this.reporterList.length; i < len; i++) {
            if (
              this.mantisDialogEdit.reporter_en_name ==
              this.reporterList[i].user_en_name
            ) {
              this.mantisDialogEdit.reporter = this.reporterList[
                i
              ].user_name_cn;
            }
          }

          await this.update({
            project_id: this.mantisDialogEdit.project_id,
            system_version: this.mantisDialogEdit.system_version,
            stage: this.mantisDialogEdit.stage,
            developer: this.mantisDialogEdit.developer,
            handler: this.mantisDialogEdit.handler,
            handler_en_name: this.mantisDialogEdit.handler_en_name,
            priority: this.mantisDialogEdit.priority,
            severity: this.mantisDialogEdit.severity,
            flaw_source: this.mantisDialogEdit.flaw_source,
            plan_fix_date: this.mantisDialogEdit.plan_fix_date,
            flaw_type: this.mantisDialogEdit.flaw_type,
            id: this.mantisDialogEdit.id,
            status: this.mantisDialogEdit.status,
            date_submitted: this.mantisDialogEdit.date_submitted,
            summary: this.mantisDialogEdit.summary,
            description: this.mantisDialogEdit.description,
            reason: this.mantisDialogEdit.reason
              ? this.mantisDialogEdit.reason
              : '',
            mantis_token: this.userMantisToken,
            reporter: this.mantisDialogEdit.reporter,
            reporter_en_name: this.mantisDialogEdit.reporter_en_name,
            workNo: this.mantisDialogEdit.workNo,
            redmine_id: this.mantisDialogEdit.redmine_id,
            user_name_cn: this.TuserName,
            system_name: this.mantisDialogEdit.system_name
          });
          this.$message({
            showClose: true,
            message: '交易执行成功',
            type: 'success'
          });
          this.mantisEditDialog = false;
          this.$refs[mantisDialogEdit].resetFields();
          await this.queryMantisInfoFun();
        } else {
          return false;
        }
      });
    }
  },
  async mounted() {
    this.MantisId = this.$route.query.id;
    await this.queryMantisInfoFun();
    //查询mantis项目
    await this.queryMantisProjects();
    this.projectList = this.mantisProjects;
    // 查询开发人员
    await this.queryDevelopList();
    this.developerList = this.developerArr.map(item => {
      return { value: item.user_name_cn, label: item.user_name_cn };
    });
    // 查询报告人，分派给
    await this.queryAllUserName();
    this.reporterList = this.reporterArr;
    this.handlerList = this.reporterArr;
  },

  filters: {
    formatSeverity(val) {
      if (val === '10') {
        return '新功能';
      } else if (val === '20') {
        return '细节';
      } else if (val === '30') {
        return '文字';
      } else if (val === '40') {
        return '小调整';
      } else if (val === '50') {
        return '小错误';
      } else if (val === '60') {
        return '很严重';
      } else if (val === '70') {
        return '崩溃';
      } else if (val === '80') {
        return '宕机';
      } else {
        //return "接口";
      }
    },
    formatPriority(val) {
      if (val === '10') {
        return '无';
      } else if (val === '20') {
        return '低';
      } else if (val === '30') {
        return '中';
      } else if (val === '40') {
        return '高';
      } else if (val === '50') {
        return '紧急';
      } else if (val === '60') {
        return '非常紧急';
      } else {
        //return "接口";
      }
    },
    formatStatus(val) {
      if (val === '10') {
        return '新建';
      } else if (val === '20') {
        return '拒绝';
      } else if (val === '30') {
        return '确认拒绝';
      } else if (val === '40') {
        return '延迟修复';
      } else if (val === '50') {
        return '打开';
      } else if (val === '80') {
        return '已修复';
      } else if (val === '90') {
        return '关闭';
      } else {
        //return "接口";
      }
    }
  }
};
</script>

<style scoped>
.headerCSS {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 30px;
}
.container {
  width: 70%;
  margin: 0 auto;
  text-align: left;
}
.filesListUl {
  margin-top: -5px;
}
.filesList {
  list-style-type: none;
  cursor: pointer;
}
.deleteFile {
  margin-right: 10px;
}
</style>
