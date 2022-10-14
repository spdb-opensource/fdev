<template>
  <div class="page-container">
    <el-form :inline="true" class="demo-form-inline" label-width="80px">
      <el-row>
        <el-col :span="4">
          <el-select
            class="el-form-item"
            multiple
            filterable
            placeholder="所属小组"
            v-model="fdev_group_id"
            clearable
            collapse-tags
            :filter-method="filterMethod"
          >
            <el-option
              v-for="item in groupAllOption"
              :label="
                item.fullName.length > 6
                  ? item.fullName.substring(0, 6) + '...'
                  : item.fullName
              "
              :value="item.id"
              :key="item.id"
            >
              <span> {{ item.fullName }} </span>
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <t-select
            prop="workNo"
            filterable
            :options="selectNames"
            placeholder="工单名"
            v-model="workNo"
            option-label="mainTaskName"
            option-value="workOrderNo"
            :full-width="false"
            :remote-method="remoteMethod"
            :loading="loading"
            remote
            clearable
            @keyup.enter.native="queryBtn()"
            @input="workNoChange"
          >
            <template v-slot:options="item">
              <el-tooltip
                effect="dark"
                :content="item.mainTaskName"
                placement="right-end"
                :visible-arrow="false"
              >
                <div slot="content" class="elTooltip">
                  {{ item.mainTaskName }}
                </div>
                <span>{{
                  item.mainTaskName.length > 10
                    ? item.mainTaskName.substring(0, 10) + '...'
                    : item.mainTaskName
                }}</span>
              </el-tooltip>
            </template>
          </t-select>
        </el-col>
        <el-col :span="4">
          <t-select
            prop="status"
            filterable
            :options="optionStatusQuery"
            placeholder="状态"
            v-model="status"
            option-label="label"
            option-value="value"
            :full-width="false"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <template v-slot:options="item">
              <span>{{ item.label }}</span>
            </template>
          </t-select>
        </el-col>
        <el-col :span="4">
          <el-input
            class="el-input-workid"
            prop="status"
            filterable
            placeholder="缺陷编号"
            v-model="id"
            @keyup.enter.native="queryBtn()"
            clearable
          />
        </el-col>
        <el-col :span="4">
          <el-input
            class="el-input-workid el-input-left"
            prop="redmine_id"
            filterable
            placeholder="需求编号"
            v-model="redmine_id"
            @keyup.enter.native="queryBtn()"
            clearable
          />
        </el-col>

        <el-col :span="4" class="defect-switch">
          <el-switch
            v-model="defectFlag"
            active-text="显示归档缺陷"
            inactive-text=""
          >
          </el-switch>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="4">
          <t-select
            prop="project_name"
            filterable
            :options="mantisProjects"
            placeholder="项目"
            v-model="project_name"
            option-label="name"
            option-value="name"
            :full-width="false"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.name }}</span>
            </template>
          </t-select>
        </el-col>
        <el-col :span="4">
          <t-select
            prop="reporter"
            filterable
            :options="reporterList"
            placeholder="缺陷提出人员"
            v-model="reporter"
            option-label="user_name_cn"
            option-value="user_name_en"
            :full-width="false"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_name_en }}</span>
            </template>
          </t-select>
        </el-col>
        <el-col :span="4">
          <t-select
            prop="app_name"
            filterable
            :options="appList"
            placeholder="应用英文名"
            v-model="app_name"
            option-label="name_en"
            option-value="name_en"
            :full-width="false"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <template v-slot:options="item">
              <span>{{ item.name_en }}</span>
            </template>
          </t-select>
        </el-col>
        <el-col :span="4">
          <t-select
            prop="handler"
            filterable
            :options="handlerList"
            placeholder="缺陷对应开发负责人"
            v-model="handler"
            option-label="user_name_cn"
            option-value="user_en_name"
            :full-width="false"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_en_name }}</span>
            </template>
          </t-select>
        </el-col>
        <el-col :span="8">
          <el-tooltip
            content="请选择缺陷提出时间范围"
            placement="bottom"
            effect="light"
          >
            <el-date-picker
              v-model="searchDate"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              :picker-options="pickerOptions"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            >
            </el-date-picker>
          </el-tooltip>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="4">
          <t-select
            prop="openTimes"
            filterable
            :options="mantisTypeOptions"
            placeholder="缺陷打开类型"
            v-model="openTimes"
            option-label="label"
            option-value="value"
            :full-width="false"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <template v-slot:options="item">
              <span>{{ item.label }}</span>
            </template>
          </t-select>
        </el-col>
        <el-col :span="4">
          <t-select
            class="el-form-item"
            :options="auditFlagOption"
            placeholder="待审批状态"
            v-model="auditFlag"
            option-label="label"
            option-value="value"
            :full-width="false"
            @keyup.enter.native="queryBtn()"
            clearable
          >
            <template v-slot:options="item">
              <span>{{ item.label }}</span>
            </template>
          </t-select>
        </el-col>
        <el-col :span="4">
          <el-form-item>
            <el-switch
              v-model="isIncludeChildren"
              active-text="展示子组"
              :active-value="true"
              :inactive-value="false"
            >
            </el-switch>
            <el-button type="primary" @click="queryBtnReId()">查询</el-button>
          </el-form-item>
        </el-col>
        <el-col :span="2">
          <el-form-item>
            <el-button type="primary" @click="exportBtn()">导出</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-table
      v-loading="tableLoading"
      stripe
      :data="mantisTableData"
      tooltip-effect="dark"
      style="width: 100%;color:black"
      :header-cell-style="{ color: '#545c64' }"
    >
      <el-table-column
        prop="planlist_testcase_id"
        label="案例关系编号"
        width="120"
      >
        <template slot-scope="scope">
          <el-link
            type="primary"
            :underline="false"
            @click="goPlanTestCase(scope.row)"
            >{{ scope.row.planlist_testcase_id }}</el-link
          >
        </template>
      </el-table-column>
      <el-table-column prop="redmine_id" label="需求编号"></el-table-column>
      <el-table-column
        prop="fdev_group_name"
        label="所属小组"
      ></el-table-column>
      <el-table-column prop="app_name" label="应用英文名"></el-table-column>
      <el-table-column prop="project_name" label="项目"></el-table-column>
      <el-table-column
        prop="summary"
        label="缺陷摘要"
        width="150"
      ></el-table-column>
      <el-table-column prop="flaw_source" label="缺陷来源"></el-table-column>
      <el-table-column prop="developer_cn" label="开发责任人"></el-table-column>
      <el-table-column prop="reporter" label="缺陷提出人员"></el-table-column>
      <el-table-column prop="handler" label="分派给"></el-table-column>
      <el-table-column
        prop="status"
        label="状态"
        sortable
        :sort-by="['status']"
        :formatter="formatterStatus"
      ></el-table-column>
      <el-table-column prop="solve_time" label="解决时长（天）">
        <template slot-scope="scope">
          <span>
            {{
              scope.row.solve_time
                ? (scope.row.solve_time / 60 / 60 / 24).toFixed(3)
                : ''
            }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template slot-scope="scope">
          <el-tooltip class="item" effect="dark" content="详情" placement="top">
            <i
              class="el-icon-document tabel-icon"
              @click="detailData(scope.$index, scope.row)"
              >&nbsp;</i
            >
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            content="编辑"
            v-if="deletePower(scope.row) && scope.row.status !== '90'"
            placement="top"
          >
            <i
              class="el-icon-edit tabel-icon"
              @click="editData(scope.$index, scope.row)"
              >&nbsp;</i
            >
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            content="删除"
            placement="top"
            v-if="deletePower(scope.row) && scope.row.status !== '90'"
          >
            <i
              class="el-icon-delete tabel-icon"
              @click="deleteData(scope.$index, scope.row)"
              >&nbsp;</i
            >
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            content="文件编辑"
            v-if="deletePower(scope.row) && scope.row.status !== '90'"
            placement="top"
          >
            <i
              class="el-icon-folder-opened"
              @click="editFiles(scope.$index, scope.row)"
            ></i>
          </el-tooltip>
          <!-- 审批 -->
          <!-- auditFlag 0 待审核-->
          <el-tooltip
            class="item"
            effect="dark"
            content="审批"
            placement="top"
            v-if="isShowAuditBtn && scope.row.auditFlag == '0'"
          >
            <i
              class="el-icon-news tabel-icon"
              @click="showAuditDialog(scope.$index, scope.row)"
              >&nbsp;</i
            >
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
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
    <!-- 审核确认弹框 -->
    <el-dialog title="提示" :visible.sync="dialogVisible" width="30%">
      <span
        >{{ mantisInfo.handler }}希望修改缺陷状态,是否确认将该缺陷状态更改为{{
          getChineseName(mantisInfo.wantStatus)
        }}?</span
      >
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="auditLoading" @click="audit('0')"
          >拒绝</el-button
        >
        <el-button type="primary" :loading="auditLoading" @click="audit('1')"
          >确定</el-button
        >
      </span>
    </el-dialog>

    <!-- 缺陷详情弹窗 -->
    <MantisDialog
      :mantisTitle="detailTitle"
      :openDialog="mantisDetailDialog"
      :task_no="task_name"
      :dialogModel="mantisDialogDetail"
      @mantisClose="closeDialogDetail"
      :filesContent="filesContent"
    />

    <!-- 编辑缺陷弹窗 -->
    <Dialog
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
            <el-form-item label="项目名称" prop="project_id">
              <el-select
                v-model="mantisDialogEdit.project_id"
                class="dialogMantis"
                placeholder
                filterable
                clearable
                disabled
              >
                <el-option
                  v-for="(item, index) in projectList"
                  :key="index"
                  :label="item.name"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="任务">
              <el-select
                v-model="task_no"
                class="dialogMantis"
                placeholder
                filterable
                clearable
                @change="taskChange(mantisDialogEdit)"
              >
                <el-option
                  v-for="(item, index) in taskOptions"
                  :key="index"
                  :value="item.value"
                  :label="item.label"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="应用英文名">
              <el-select
                v-model="mantisDialogEdit.app_name"
                class="dialogMantis"
                placeholder
                filterable
                disabled
              >
                <el-option
                  v-for="(item, index) in workNoAppLists"
                  :key="index"
                  :label="item.label"
                  :value="item.label"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="系统名称">
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
            <el-form-item label="需求编号/实施单元">
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
              <el-select
                v-model="mantisDialogEdit.system_version"
                class="dialogMantis"
                placeholder
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
            <el-form-item label="归属阶段" prop="stage">
              <el-select
                v-model="mantisDialogEdit.stage"
                class="dialogMantis"
                placeholder
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
            <el-form-item label="开发责任人" prop="developer">
              <el-select
                v-model="mantisDialogEdit.developer"
                class="dialogMantis"
                placeholder
                filterable
                value-key="user_name_en"
                clearable
              >
                <el-option
                  v-for="(item, index) in developerList"
                  :key="index"
                  :value="item"
                  :label="item.user_name_cn"
                >
                  <span style="float: left">{{ item.user_name_cn }}</span>
                  <span style="float: right">{{ item.user_name_en }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="分派给" prop="handler_en_name">
              <el-select
                v-model="mantisDialogEdit.handler_en_name"
                class="dialogMantis"
                @change="getHandlerCn"
                placeholder
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
          </el-col>

          <el-col :span="12">
            <el-form-item label="所属小组">
              <el-select
                v-model="mantisDialogEdit.fdev_group_name"
                class="dialogMantis"
                placeholder
                disabled
              >
              </el-select>
            </el-form-item>
            <el-form-item label="优先级" prop="priority">
              <el-select
                v-model="mantisDialogEdit.priority"
                class="dialogMantis"
                placeholder
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
            <el-form-item label="严重性" prop="severity">
              <el-select
                v-model="mantisDialogEdit.severity"
                class="dialogMantis"
                placeholder
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
            <el-form-item label="缺陷来源" prop="flaw_source">
              <el-select
                v-model="mantisDialogEdit.flaw_source"
                class="dialogMantis"
                placeholder
                filterable
                clearable
              >
                <el-option
                  v-for="(item, index) in optionDSS"
                  :key="index"
                  :label="item.lalel"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="预计修复时间" prop="plan_fix_date">
              <el-date-picker
                v-model="mantisDialogEdit.plan_fix_date"
                type="date"
                value-format="yyyy-MM-dd"
                style="width:90%"
                class="dialogMantis"
                :picker-options="expireTimeOption"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="缺陷类型" prop="flaw_type">
              <el-select
                v-model="mantisDialogEdit.flaw_type"
                class="dialogMantis"
                placeholder
                filterable
                clearable
              >
                <el-option
                  v-for="(item, index) in optionDTS"
                  :key="index"
                  :label="item.lalel"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select
                v-model="mantisDialogEdit.status"
                class="dialogMantis"
                @change="getStatus"
                placeholder
                filterable
              >
                <el-option
                  v-for="(item, index) in optionStatusEdit"
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
            <el-form-item label="缺陷提出人员">
              <el-select
                v-model="mantisDialogEdit.reporter"
                class="dialogMantis"
                placeholder
                filterable
                clearable
                disabled
              >
                <el-option
                  v-for="(item, index) in reporterList"
                  :key="index"
                  :value="item.user_name_en"
                  :label="item.user_name_cn"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-row>
              <el-form-item label="摘要" prop="summary">
                <el-input
                  type="textarea"
                  :rows="2"
                  class="detailsDialogInput"
                  v-model="mantisDialogEdit.summary"
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item
                v-if="showReopenReason"
                label="重新打开原因"
                prop="reopen_reason"
              >
                <el-input
                  type="textarea"
                  :rows="2"
                  class="detailsDialogInput"
                  v-model="mantisDialogEdit.reopen_reason"
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
                ></el-input>
              </el-form-item>
            </el-row>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="mantisEditDialog = false">关 闭</el-button>
        <el-button
          type="primary"
          :loading="loadingBtn"
          @click="sbimitMantisEdit('mantisDialogEdit')"
          >提 交</el-button
        >
      </div>
    </Dialog>

    <!-- 编辑文件弹窗 -->
    <el-dialog
      title="文件编辑"
      :visible.sync="filesDialogEdit"
      class="files_dialog"
      width="40%"
      :before-close="handleClose"
    >
      <el-form :inline="true" label-width="120px">
        <el-form-item label="文件列表: " class="filesEditCss">
          <ul class="filesListUl">
            <li
              v-for="(file, index) in filesContent"
              :key="index"
              class="filesList"
            >
              <i
                class="el-icon-delete deleteFile"
                @click="deleteFileData(file, index)"
              ></i>
              <i
                class="el-icon-download deleteFile"
                @click="downloadFileData(file)"
              ></i>
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
        <div></div>
        <el-form-item label="新增文件: " class="filesEditCss">
          <el-upload
            :action="actionUrl"
            multiple
            accept=".txt, .jpg, .png"
            ref="upload"
            :before-remove="beforeRemove"
            :on-remove="handleRemove"
            :auto-upload="false"
            :file-list="fileList"
            :on-change="test"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">只能上传txt/jpg/png文件</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileAdd()">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 文件预览弹框 -->
    <el-dialog :visible.sync="filePreviewDialog" class="filePreviewDialog">
      <div v-html="filePreview">{{ filePreview }}</div>
    </el-dialog>
  </div>
</template>

<script>
import { exportExcel } from '@/common/utlis';
import { mapActions, mapState } from 'vuex';
import MantisDialog from './MantisDialog';
import { queryOrderInfoByNo } from '@/services/order.js';
import {
  checkAuditAuthority,
  queryAuditMantisInfo
} from '@/services/mantis.js';

import {
  optionDT,
  optionDTSA,
  optionBS,
  optionStatusQuery,
  optionSV,
  optionPriority,
  optionSeverity,
  optionDS,
  securityDefectFrom as optionDSA,
  optionStatusEdit,
  mantisDialogRules,
  mantisTypeOptions
} from './model.js';
import { auditMantis } from '@/services/mantis.js';
export default {
  name: 'GeneralApproval',
  components: {
    MantisDialog
  },
  data() {
    return {
      groupAllOption: [],
      mantisInfo: {},
      orderType: '',
      auditLoading: false,
      dialogVisible: false,
      rowData: {},
      auditFlag: '', //审批状态
      auditFlagOption: [
        { label: '全部', value: '' },
        { label: '待审批', value: '0' },
        { label: '已审批', value: '1' }
      ], //审批状态 0-待审批，1-已审批
      openTimes: '',
      mantisTypeOptions: mantisTypeOptions,
      task_name: '',
      task_no: '',
      workNoAppLists: [],
      taskOptions: [],
      id: '',
      labelWidth: '150px',
      tableLoading: false,
      selectNames: [],
      // 模糊查询工单名
      workNoNameList: [],
      showReopenReason: false,
      loading: false,
      defectFlag: false,
      filePreview: null,
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
      expireTimeOption: {
        disabledDate(time) {
          return time.getTime() <= Date.now() - 24 * 60 * 60 * 1000;
        }
      },
      userRole: sessionStorage.getItem('Trole'),
      userName: sessionStorage.getItem('TuserName'),
      userEnName: sessionStorage.getItem('user_en_name'),
      userMantisToken: sessionStorage.getItem('mantisToken'),
      addFileId: '',
      filesContent: [],
      fdev_group_id: [],
      project_name: '',
      workNo: '',
      flaw_type: '',
      stage: '',
      searchDate: ['', ''],
      reporter: '',
      redmine_id: '',
      handler: '',
      app_name: '',
      handler_en_name: '',
      status: '',
      projectList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reporterPower: '',
      loadingBtn: false,
      optionDT,
      optionDTSA,
      optionBS,
      developerList: [],
      handlerList: [],
      reporterList: [
        {
          user_name_en: '',
          user_name_cn: ''
        }
      ],
      optionStatusQuery,
      optionStatusEdit,
      mantisTableData: [],
      mantisDetailDialog: false,
      mantisDialogDetail: {},
      mantisEditDialog: false,
      mantisDialogEdit: {},
      filesDialogEdit: false,

      optionSV,
      optionPriority,
      optionSeverity,
      optionDS,
      optionDSA,
      actionUrl: '',
      fileList: [],
      mantisDialogRules,
      filePreviewDialog: false,
      fileTypeArray: [],
      judgeFileType: true,
      getHandlerCnName: '',
      initStatus: 0,
      detailTitle: '缺陷详情',
      isIncludeChildren: false
    };
  },
  computed: {
    ...mapState('adminForm', ['groupAll']),
    ...mapState('workOrderForm', ['taskAllData']),
    ...mapState('mantisForm', [
      'mantisPlanId',
      'mantisList',
      'issueDetailList',
      'mantisTableDataList',
      'workOrderList',
      'mantisProjects',
      'developerArr',
      'reporterArr',
      'fdevDevelopList',
      'fdevStatusGroup',
      'appList',
      'workNoAppList',
      'fdevTaskDetail'
    ]),
    isShowAuditBtn() {
      // 玉衡 审批人员展示
      let currentUser = JSON.parse(sessionStorage.getItem('userInfo'));
      return currentUser.role.some(item => item.name == '玉衡-审批人员');
    },
    optionDTS() {
      if (this.orderType == 'security') {
        return optionDTSA;
      } else {
        return optionDT;
      }
    },
    optionDSS() {
      if (this.orderType == 'security') {
        return optionDSA;
      } else {
        return optionDS;
      }
    },
    startDate() {
      if (this.searchDate === null) {
        return '';
      } else {
        return this.searchDate[0];
      }
    },
    endDate() {
      if (this.searchDate === null) {
        return '';
      } else {
        return this.searchDate[1];
      }
    },
    includeCloseFlag() {
      if (this.defectFlag === false) {
        return '1';
      } else {
        return '0';
      }
    }
  },
  methods: {
    ...mapActions('adminForm', ['queryAllGroup']),
    ...mapActions('workOrderForm', ['queryTasks']),
    ...mapActions('mantisForm', [
      'queryPlanIdByPlanlistTestcaseId',
      'deleteFile',
      'addFile',
      'exportMantisList',
      'queryIssueDetail',
      'deleteMantis',
      'update',
      'query',
      'countIssue',
      'queryWorkOrderList',
      'queryMantisProjects',
      'queryDevelopList',
      'queryAllUserName',
      'queryFdevUser',
      'queryGroup',
      'queryApps',
      'queryAppByWorkNo',
      'queryFdevTaskDetail'
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
    getChineseName(type) {
      let statusName = {
        '10': '新建',
        '20': '拒绝',
        '30': '确认拒绝',
        '40': '延迟修复',
        '50': '打开',
        '51': '重新打开',
        '60': '待确认',
        '70': '已关闭',
        '80': '已修复',
        '90': '关闭',
        '110': '遗留'
      };
      return statusName[type];
    },
    async audit(auditFlag) {
      let row = this.rowData;
      this.auditLoading = true;
      try {
        await auditMantis({ id: row.id, auditFlag });
      } finally {
        this.auditLoading = false;
        this.dialogVisible = false;
      }
      this.queryBtn();
    },
    async showAuditDialog(index, row) {
      await checkAuditAuthority({ id: String(row.id) });
      let res = await queryAuditMantisInfo({ id: String(row.id) });
      this.mantisInfo = res;
      this.rowData = row;
      this.dialogVisible = true;
    },
    workNoChange() {
      sessionStorage.removeItem('workOrderInfo');
    },
    // 点击案例编号跳转到执行计划页面
    async goPlanTestCase(row) {
      const { workNo, planlist_testcase_id } = row;
      const planlistTestcaseId = planlist_testcase_id;
      await this.queryPlanIdByPlanlistTestcaseId({
        planlistTestcaseId,
        workNo
      });
      this.$router.push({
        name: 'Plan',
        query: {
          workOrderNo: workNo,
          planlist_testcase_id,
          planId: this.mantisPlanId
        }
      });
    },
    closeDialogDetail() {
      this.mantisDetailDialog = false;
    },

    // 编辑、删除权限控制
    deletePower(params) {
      let { reporter_en_name } = params;
      let reporterList = [reporter_en_name];
      return reporterList.indexOf(this.userEnName) > -1 || this.userRole > 30;
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

    deleteFileData(file, index) {
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          await this.deleteFile({
            id: file.id,
            file_id: file.file_id
          });
          this.filesContent.splice(index, 1);
          this.$message({
            showClose: true,
            type: 'success',
            message: '删除成功!'
          });
        })
        .catch(() => {
          this.$message({
            showClose: true,
            type: 'info',
            message: '已取消删除'
          });
        });
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

    // 文件上传
    async submitFileAdd() {
      this.fileTypeArray = [];
      this.judgeFileType = true;
      this.fileList.forEach(item => {
        this.fileTypeArray.push(item.raw.type);
      });
      let typeList = ['text/plain', 'image/jpeg', 'image/jpg', 'image/png'];
      for (var i = 0; i < this.fileTypeArray.length; i++) {
        if (typeList.indexOf(this.fileTypeArray[i]) == -1) {
          this.judgeFileType = false;
        }
      }
      let res = [];
      if (this.fileList.length !== 0) {
        res = await this.getFile();
      }
      if (this.judgeFileType) {
        await this.addFile({
          id: this.addFileId,
          mantis_token: this.userMantisToken,
          files: res
        });
        this.$message({
          showClose: true,
          message: '交易执行成功',
          type: 'success'
        });
        this.filesDialogEdit = false;
        this.$refs.upload.clearFiles();
      } else {
        this.$message({
          showClose: true,
          type: 'error',
          message: '文件上传失败！只能上传txt/jpg/png文件!'
        });
      }
    },

    handleClose() {
      this.filesDialogEdit = false;
      this.$refs.upload.clearFiles();
    },

    //状态转译
    formatterStatus(row, column) {
      if (row.status === '10') {
        return '新建';
      } else if (row.status === '20') {
        return '拒绝';
      } else if (row.status === '30') {
        return '确认拒绝';
      } else if (row.status === '40') {
        return '延迟修复';
      } else if (row.status === '50') {
        return '打开';
      } else if (row.status === '80') {
        return '已修复';
      } else if (row.status === '90') {
        return '关闭';
      } else {
        return '';
      }
    },

    async queryBtnReId() {
      this.tableLoading = true;
      this.currentPage = 1;
      await this.queryListAll();
    },
    //页面查询功能
    async queryBtn() {
      if (
        this.$router.currentRoute.query.workOrderInfo ||
        this.$router.currentRoute.query.id
      ) {
        this.id = this.$router.currentRoute.query.id;
      }
      this.tableLoading = true;
      this.currentPage = 1;
      await this.queryListAll();
    },

    // 页面导出功能
    exportBtn() {
      this.$confirm('此操作将全部导出文件, 是否继续?', '提示', {
        confirmButtonText: '全部导出',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.exportMantisList({
          fdev_group_id: this.fdev_group_id,
          isIncludeChildren: this.isIncludeChildren,
          workNo: this.workNo,
          startDate: this.startDate,
          endDate: this.endDate,
          reporter: this.reporter,
          handler: this.handler,
          app_name: this.app_name,
          status: this.status,
          project_name: this.project_name,
          includeCloseFlag: this.includeCloseFlag,

          redmine_id: this.redmine_id
        });
        let res = this.mantisList;
        let fileName = res.headers['content-disposition'];
        const fileType = res.headers['content-type'];
        fileName = fileName.substring(fileName.indexOf('=') + 1);
        const blob = new Blob([res.data], { type: fileType });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        URL.revokeObjectURL(url);
        document.body.removeChild(link);
        this.$message({
          type: 'success',
          message: '全部导出成功!'
        });
      });
    },

    async detailData(index, row) {
      this.mantisDetailDialog = true;
      await this.queryIssueDetail({
        id: row.id
      });
      this.mantisDialogDetail = this.issueDetailList;
      this.filesContent = this.issueDetailList.files;
      //查询任务详情拿回任务id
      this.task_name = '';
      if (row.task_id != '' && row.task_id != null) {
        await this.queryFdevTaskDetail({ id: row.task_id });
        this.task_name = this.fdevTaskDetail.name;
      }
    },
    async taskChange(mantisDialogEdit) {
      await this.queryTasks({
        workNo: mantisDialogEdit.workNo,
        taskNo: this.task_no
      });
      this.workNoAppLists = this.taskAllData.taskList.map(item => {
        return { label: item.project_name, value: item.project_id };
      });
      let app_name = this.taskAllData.taskList.map(item => {
        return item.project_name;
      });
      const set = new Set(app_name);
      let appNameArray = Array.from(set);
      this.mantisDialogEdit.app_name = appNameArray.splice(',');
    },
    async editData(index, row) {
      this.task_no = '';
      this.showReopenReason = false;
      //查任务列表
      await this.queryTasks({
        workNo: row.workNo
      });
      let workNoAppLists = this.taskAllData.taskList.map(item => {
        return { label: item.project_name, value: item.project_id };
      });
      var workNoAppListsTmp = {};
      this.workNoAppLists = workNoAppLists.reduce((item, index) => {
        workNoAppListsTmp[index.value]
          ? ''
          : (workNoAppListsTmp[index.value] = true && item.push(index));
        return item;
      }, []);
      const app_name = [];
      app_name.push(row.app_name);
      this.mantisDialogEdit.app_name = app_name;
      this.taskOptions = this.taskAllData.taskList.map(item => {
        return { label: item.name, value: item.id };
      });
      //查询任务详情拿回任务id
      if (row.task_id != '') {
        await this.queryFdevTaskDetail({ id: row.task_id });
        this.task_no = this.fdevTaskDetail.id;
      }
      await this.queryAppByWorkNo({ workNo: row.workNo });
      this.mantisDialogEdit = Object.assign({}, row);
      this.mantisDialogEdit.developer = {
        user_name_cn: row.developer_cn,
        user_name_en: row.developer
      };

      if (this.mantisDialogEdit) {
        this.initStatus = this.mantisDialogEdit.status;
        if (this.mantisDialogEdit.status == 80) {
          this.optionStatusEdit = [
            { value: '10', label: '新建', disabled: true },
            { value: '20', label: '拒绝', disabled: true },
            { value: '30', label: '确认拒绝', disabled: true },
            { value: '40', label: '延迟修复', disabled: true },
            { value: '50', label: '打开' },
            { value: '80', label: '已修复', disabled: true },
            { value: '90', label: '关闭' }
          ];
        } else if (this.mantisDialogEdit.status == 20) {
          this.optionStatusEdit = [
            { value: '10', label: '新建', disabled: true },
            { value: '20', label: '拒绝', disabled: true },
            { value: '30', label: '确认拒绝' },
            { value: '40', label: '延迟修复', disabled: true },
            { value: '50', label: '打开' },
            { value: '80', label: '已修复', disabled: true },
            { value: '90', label: '关闭', disabled: true }
          ];
        } else {
          this.optionStatusEdit = [
            { value: '10', label: '新建', disabled: true },
            { value: '20', label: '拒绝', disabled: true },
            { value: '30', label: '确认拒绝', disabled: true },
            { value: '40', label: '延迟修复', disabled: true },
            { value: '50', label: '打开', disabled: true },
            { value: '80', label: '已修复', disabled: true },
            { value: '90', label: '关闭', disabled: true }
          ];
        }
      }
      this.getHandlerCnName = row.handler;
      let res = await queryOrderInfoByNo({
        workNo: row.workNo
      });
      this.orderType = res.orderType;
      this.mantisEditDialog = true;
      this.loadingBtn = false;
    },

    deleteData(index, row) {
      (this.userMantisToken = sessionStorage.getItem('mantisToken')),
        this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(async () => {
            await this.deleteMantis({
              id: row.id,
              mantis_token: this.userMantisToken
            });
            this.mantisTableData.splice(index, 1);
            this.$message({
              showClose: true,
              type: 'success',
              message: '删除成功!'
            });
            if (
              this.pageSize * (this.currentPage - 1) + 1 >= this.total &&
              this.total !== 1
            ) {
              this.currentPage -= 1;
              this.queryListAll();
            } else {
              this.queryListAll();
            }
          })
          .catch(() => {
            this.$message({
              showClose: true,
              type: 'info',
              message: '已取消删除'
            });
          });
    },

    async editFiles(index, row) {
      this.filesDialogEdit = true;
      await this.queryIssueDetail({
        id: row.id
      });
      this.filesContent = this.issueDetailList.files;
      this.addFileId = row.id;
    },

    sbimitMantisEdit(mantisDialogEdit) {
      (this.userMantisToken = sessionStorage.getItem('mantisToken')),
        this.$refs[mantisDialogEdit].validate(async valid => {
          if (valid) {
            if (this.initStatus == 20 && this.mantisDialogEdit.status == 30) {
              let checkArr = ['数据问题', '环境问题', '其他原因'];
              let message =
                '缺陷来源必须选择“数据问题”，“环境问题”，“其他原因”之一！';
              if (this.orderType == 'security') {
                checkArr = ['其他原因'];
                message = '缺陷来源必须选择“其他原因”！';
              }
              if (checkArr.indexOf(this.mantisDialogEdit.flaw_source) < 0) {
                this.$message({
                  showClose: true,
                  message: message,
                  type: 'error'
                });
                return;
              }
            }
            this.loadingBtn = true;
            await this.update({
              task_no: this.mantisDialogEdit.task_id
                ? this.fdevTaskDetail.id
                : this.task_no,
              project_id: this.mantisDialogEdit.project_id,
              system_version: this.mantisDialogEdit.system_version,
              stage: this.mantisDialogEdit.stage,
              developer: this.mantisDialogEdit.developer.user_name_en, // 新增中文名
              developer_cn: this.mantisDialogEdit.developer.user_name_cn, // 新增中文名
              handler: this.getHandlerCnName,
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
              reopen_reason: this.mantisDialogEdit.reopen_reason, //缺陷重新打开原因
              reason: this.mantisDialogEdit.reason
                ? this.mantisDialogEdit.reason
                : '',
              mantis_token: this.userMantisToken,
              reporter: this.mantisDialogEdit.reporter,
              reporter_en_name: this.mantisDialogEdit.reporter_en_name,
              workNo: this.mantisDialogEdit.workNo,
              redmine_id: this.mantisDialogEdit.redmine_id,
              system_name: this.mantisDialogEdit.system_name,
              user_name_cn: this.userName,
              appName_en: Array.isArray(this.mantisDialogEdit.app_name)
                ? this.mantisDialogEdit.app_name.join()
                : this.mantisDialogEdit.app_name
            });
            this.$message({
              showClose: true,
              message: '交易执行成功',
              type: 'success'
            });
            this.mantisEditDialog = false;
            this.$refs[mantisDialogEdit].resetFields();
            this.queryListAll();
            this.loadingBtn = false;
          } else {
            return false;
          }
        });
    },

    handleSizeChange: function(size) {
      this.pageSize = size;
      this.tableLoading = true;
      this.queryListAll();
    },
    handleCurrentChange(val) {
      this.tableLoading = true;
      this.currentPage = val;
      this.queryListAll();
    },

    //查询总数
    async queryListAll() {
      await this.query({
        pageSize: String(this.pageSize),
        currentPage: String(this.currentPage),
        fdev_group_id: this.fdev_group_id,
        isIncludeChildren: this.isIncludeChildren,
        workNo: this.workNo,
        startDate: this.startDate,
        endDate: this.endDate,
        reporter: this.reporter,
        handler: this.handler,
        app_name: this.app_name,
        project_name: this.project_name,
        status: this.status,
        includeCloseFlag: this.includeCloseFlag,
        id: this.id,
        redmine_id: this.redmine_id,
        openTimes: this.openTimes,
        auditFlag: this.auditFlag
      });
      this.mantisTableData = this.mantisTableDataList.issues;
      this.total = this.mantisTableDataList.total;
      this.tableLoading = false;
    },

    // 案例详情弹框
    async testcaseDetail(row) {
      this.caseDetails = true;
      let res = await QueryDetailByTestcaseNo({
        testcaseNo: row.testcaseNo
      });
      this.caseDetailsModel = res === null ? {} : res;
    },

    //文件上传相关操作
    beforeRemove(file, fileList) {
      return this.$confirm(`确定移除 ${file.name}？`);
    },
    handleRemove(file, fileList) {
      this.fileList = fileList;
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
        // this.fileList.forEach(item => {
        //   this.fileTypeArray.push(item.raw.type);
        // });
      });
    },
    test(file, files) {
      this.fileList = files;
      if (file.raw.type.length === 0) {
        alert('文件类型不合法，请移除该文件');
        return false;
      }
    },

    // 获取分派给handler选择框的值
    getHandlerCn(val) {
      var obj = {};
      obj = this.handlerList.find(function(item) {
        return item.user_en_name === val;
      });
      this.getHandlerCnName = obj.user_name_cn;
    },
    // 获取状态选择框的值
    getStatus(val) {
      this.showReopenReason = val == '50' ? true : false; //50表示打开状态
      var obj = {};
      obj = this.optionStatusEdit.find(function(item) {
        return item.value === val;
      });
      this.deliveryStatus = obj.label;
    },
    remoteMethod(query) {
      if (query !== '') {
        this.loading = true;
        setTimeout(() => {
          this.loading = false;
          this.selectNames = this.workNoNameList.filter(item => {
            if (item.mainTaskName) {
              return (
                item.mainTaskName.toLowerCase().indexOf(query.toLowerCase()) >
                -1
              );
            }
          });
        }, 200);
      } else {
        this.selectNames = [];
      }
    }
  },
  async created() {
    this.reporter = localStorage.getItem('user_en_name');
    if (
      this.$router.currentRoute.query.workOrderInfo ||
      this.$router.currentRoute.query.id
    ) {
      this.reporter = '';
      this.defectFlag = true;
      this.id = this.$router.currentRoute.query.id;
      if (JSON.parse(sessionStorage.getItem('workOrderInfo'))) {
        this.selectNames = JSON.parse(sessionStorage.getItem('workOrderInfo'));
        this.workNo = this.selectNames[0].workOrderNo;
      }
      this.queryBtn();
      await this.queryMantisProjects();
      this.projectList = this.mantisProjects;
    }
  },
  async mounted() {
    this.queryApps();
    // 缺陷提出人员默认为当前用户
    await this.queryWorkOrderList();
    this.workNoNameList = this.workOrderList.map(item => {
      return {
        workOrderNo: item.workOrderNo,
        mainTaskName: item.mainTaskName
      };
    });
    this.queryBtn();
    //查询mantis项目
    await this.queryMantisProjects();
    this.projectList = this.mantisProjects;
    // 查询开发人员
    await this.queryDevelopList();
    this.developerList = this.developerArr;
    // 查询缺陷提出人员
    await this.queryAllUserName();
    this.reporterList = this.reporterArr;
    // 查询分派给
    await this.queryFdevUser();
    let fdevDevelopList = this.fdevDevelopList.map(item => {
      return {
        user_name_cn: item.user_name_cn,
        user_en_name: item.user_name_en
      };
    });
    this.handlerList = fdevDevelopList;
    await this.queryGroup({ status: '1' });
    if (this.groupAll.length <= 1) {
      await this.queryAllGroup({ status: 1 });
    }
    this.groupAllOption = this.groupAll;
  }
};
</script>

<style scoped>
body {
  margin: 0;
}
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

.files_dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.files_dialog >>> .el-dialog__header {
  background: #409eff;
  padding: 15px 20px 10px;
}

.files_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.files_dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.files_dialog >>> .el-dialog {
  margin: 0 auto !important;
  height: 55%;
  overflow: hidden;
}

.files_dialog >>> .el-dialog__body {
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

.files_dialog >>> .el-dialog__footer {
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
.dialogMantis >>> .el-input__inner {
  color: #000 !important;
}
.filesList {
  list-style-type: none;
  cursor: pointer;
}
.filesListUl {
  margin-top: -5px;
}
.filesEditCss >>> .el-form-item__label {
  font-size: 16px;
  font-weight: 500;
  font-family: '微软雅黑';
}

.filesEditCss >>> .el-icon-close:before {
  color: gray;
  font-size: 16px;
  font-weight: 500;
}
.deleteFile {
  margin-right: 10px;
}
.defect-switch {
  margin-top: 10px;
  padding-left: 5px;
}
.el-input-workid {
  width: 200px;
}
.el-input-left {
  padding-left: 5px;
}
.elTooltip {
  max-width: 300px;
}
/deep/ .fdev_group_id .el-select__tags-text {
  max-width: 90px;
  display: inline-block;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
</style>
