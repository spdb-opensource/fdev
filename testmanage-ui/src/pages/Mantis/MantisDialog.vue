<template>
  <el-dialog
    :title="mantisTitle"
    :visible.sync="openDialog"
    :before-close="closeDialog"
    width="65%"
    class="abow_dialog"
  >
    <el-form
      :inline="true"
      :model="dialogModel"
      :label-width="labelWidth"
      size="mini"
    >
      <el-row>
        <el-col :span="12">
          <el-form-item label="项目名称">
            <el-select
              v-model="dialogModel.project_name"
              class="dialogMantis"
              placeholder
              disabled
            >
            </el-select>
          </el-form-item>
          <el-form-item label="任务">
            <el-select
              v-model="task_no"
              class="dialogMantis"
              placeholder
              disabled
            >
            </el-select>
          </el-form-item>
          <el-form-item label="应用名称">
            <el-select
              v-model="dialogModel.app_name"
              class="dialogMantis"
              placeholder
              disabled
            >
            </el-select>
          </el-form-item>
          <el-form-item label="系统名称">
            <el-input
              v-model="dialogModel.system_name"
              autocomplete="off"
              class="dialogMantis"
              suffix-icon="xxx"
              disabled
            ></el-input>
          </el-form-item>
          <el-form-item label="功能模块">
            <el-select
              v-model="dialogModel.function_module"
              placeholder
              class="dialogMantis"
              disabled
            ></el-select>
          </el-form-item>
          <el-form-item label="需求编号/实施单元">
            <el-input
              v-model="dialogModel.redmine_id"
              autocomplete="off"
              class="dialogMantis"
              suffix-icon="xxx"
              disabled
            ></el-input>
          </el-form-item>
          <el-form-item label="工单号">
            <el-input
              v-model="dialogModel.workNo"
              autocomplete="off"
              class="dialogMantis"
              suffix-icon="xxx"
              disabled
            ></el-input>
          </el-form-item>
          <el-form-item label="系统版本">
            <el-select
              v-model="dialogModel.system_version"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in optionSV"
                :key="index"
                :label="item.lalel"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="归属阶段">
            <el-select
              v-model="dialogModel.stage"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in optionBS"
                :key="index"
                :label="item.lalel"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="开发人责任人">
            <el-select
              v-model="dialogModel.developer"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in developerList"
                :key="index"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="分派给">
            <el-select
              v-model="dialogModel.handler"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in handlerList"
                :key="index"
                :value="item.user_name_en"
                :label="item.user_name_cn"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="缺陷提出人员">
            <el-input
              v-model="dialogModel.reporter"
              autocomplete="off"
              class="dialogMantis"
              suffix-icon="xxx"
              disabled
            ></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="所属小组">
            <el-select
              v-model="dialogModel.fdev_group_name"
              class="dialogMantis"
              placeholder
              disabled
            >
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select
              v-model="dialogModel.priority"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in optionPriority"
                :key="index"
                :label="item.value"
                :value="item.label"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="严重性">
            <el-select
              v-model="dialogModel.severity"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in optionSeverity"
                :key="index"
                :label="item.value"
                :value="item.label"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="缺陷来源">
            <el-select
              v-model="dialogModel.flaw_source"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in optionDS"
                :key="index"
                :label="item.lalel"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="预计修复时间">
            <el-date-picker
              v-model="dialogModel.plan_fix_date"
              type="date"
              value-format="yyyy-MM-dd"
              style="width:90%"
              class="dialogMantis"
              disabled
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="缺陷类型">
            <el-select
              v-model="dialogModel.flaw_type"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in optionDT"
                :key="index"
                :label="item.value"
                :value="item.label"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="缺陷编号">
            <el-input
              v-model="dialogModel.id"
              autocomplete="off"
              class="dialogMantis"
              suffix-icon="xxx"
              disabled
            ></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="dialogModel.status"
              class="dialogMantis"
              placeholder
              disabled
            >
              <el-option
                v-for="(item, index) in optionStatusQuery"
                :key="index"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="dialogModel.date_submitted"
              type="date"
              value-format="yyyy-MM-dd"
              style="width:90%"
              class="dialogMantis"
              disabled
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="文件列表 ">
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
        <el-col :span="24">
          <el-row>
            <el-form-item label="解决时长" prop="solve_time">
              <el-input
                v-model="dialogModel.solve_time"
                autocomplete="off"
                class="dialogMantis"
                suffix-icon="xxx"
                disabled
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="摘要" prop="summary">
              <el-input
                type="textarea"
                :rows="2"
                class="detailsDialogInput"
                v-model="dialogModel.summary"
                disabled
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="描述" prop="description">
              <el-input
                type="textarea"
                :rows="3"
                class="detailsDialogInput"
                v-model="dialogModel.description"
                disabled
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="开发原因分析" prop="reason">
              <el-input
                type="textarea"
                :rows="2"
                class="detailsDialogInput"
                v-model="dialogModel.reason"
                disabled
              ></el-input>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="重新打开原因" prop="reopen_reason">
              <el-input
                type="textarea"
                :rows="2"
                class="detailsDialogInput"
                v-model="dialogModel.reopen_reason"
                disabled
              ></el-input>
            </el-form-item>
          </el-row>
        </el-col>
      </el-row>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="closeDialog">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { exportExcel, getUserListByRole } from '@/common/utlis';
import {
  optionDT,
  optionBS,
  optionStatusQuery,
  optionSV,
  optionPriority,
  optionSeverity,
  optionDS
} from './model.js';

export default {
  name: 'MantisDialog',
  data() {
    return {
      labelWidth: '150px',
      developerList: [],
      handlerList: [],
      optionDT,
      optionBS,
      optionStatusQuery,
      optionSV,
      optionPriority,
      optionSeverity,
      optionDS
    };
  },
  props: {
    mantisTitle: {
      type: String,
      required: true
    },
    openDialog: {
      type: Boolean,
      default: false
    },
    task_no: {
      type: String
    },
    dialogModel: {
      type: Object,
      required: true
    },
    filesContent: {
      type: Array,
      required: true
    }
  },

  methods: {
    closeDialog() {
      this.$emit('mantisClose');
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
    }
  },
  async created() {
    // 查询开发人员
    let userList = await getUserListByRole();
    this.developerList = userList.map(item => {
      return { value: item.user_name_en, label: item.user_name_cn };
    });
    // 查询分派给
    this.handlerList = userList;
  }
};
</script>

<style scoped>
.dialogMantis >>> .el-input__inner {
  color: #000 !important;
}
.detailsDialogInput {
  width: 380%;
}
.detailsDialogInput >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
  color: #000 !important;
}
</style>
