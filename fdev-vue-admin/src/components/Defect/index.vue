<template>
  <div>
    <Loading :visible="loading">
      <div>
        <fdev-table
          titleIcon="list_s_f"
          title="SIT缺陷列表"
          :data="defectList"
          :columns="columns"
          row-key="id"
          :filter="searchValueFilter"
          :filter-method="defectFilter"
          :pagination.sync="pagination"
          class="my-sticky-column-table"
          :visible-columns="visibleCols"
          :on-select-cols="saveVisibleColumns"
          no-export
        >
          <template v-slot:top-bottom v-if="needSelect">
            <f-formitem label="搜索条件">
              <fdev-select
                multiple
                use-input
                hide-dropdown-icon
                placeholder="输入关键字，回车区分"
                :value="termsApp.searchValue"
                @input="saveSearchValueApp($event)"
                @new-value="addTerm"
                ref="terms"
              >
                <template v-slot:append>
                  <f-icon
                    name="search"
                    class="cursor-pointer"
                    @click="setSelect()"
                  />
                </template>
              </fdev-select>
            </f-formitem>
          </template>
          <template v-slot:top-right>
            <fdev-toggle
              :value="termsApp.showDownFile"
              color="green"
              label="显示归档缺陷"
              @input="
                saveShowDownFile($event);
                toggleShow();
              "
              left-label
              v-if="needSelect"
            />
          </template>
          <template v-slot:body-cell-task_name="props">
            <fdev-td class="text-ellipsis">
              <span v-if="jobName" :title="jobName"> {{ jobName }} </span>
              <span v-if="!jobName && !props.row.task_name" title="-"> - </span>
              <router-link :to="`/job/list/${props.row.task_id}`" class="link">
                <span :title="props.row.taskName">{{
                  props.row.task_name
                }}</span>
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.task_name }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </fdev-td>
          </template>

          <template v-slot:body-cell-summary="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              <span class="link">{{ props.value }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>

          <template v-slot:body-cell-description="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>

          <template v-slot:body-cell-priority="props">
            <fdev-td :title="props.value" class="text-ellipsis">
              <f-status-color
                :color="props.value | statusFilter"
              ></f-status-color>
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-status="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-handler="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-developer_cn="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-flaw_source="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-flaw_type="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-date_submitted="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-reporter="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-reason="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>

          <template v-slot:body-cell-stage="props">
            <fdev-td
              :title="props.value"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.value }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-solve_time="props">
            <fdev-td
              :title="props.row.solve_time | filterSecondToDay"
              class="text-ellipsis cursor-pointer"
              @click="popDefect(props.row)"
            >
              {{ props.row.solve_time | filterSecondToDay }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-operation="props">
            <fdev-td class="hover">
              <div
                class="q-gutter-x-sm row no-wrap"
                v-if="isHandler(props.row.handler_en_name)"
              >
                <fdev-btn
                  flat
                  :label="item"
                  v-for="(item, index) in props.value"
                  :key="index"
                  :disable="props.row.auditFlag == 0"
                  @click="changeStatus(props.row, item)"
                />
                <fdev-tooltip v-if="props.row.auditFlag == 0"
                  >待审核,不可操作!</fdev-tooltip
                >
                <!-- 待审核所有 按钮不可操作 -->
              </div>
              <div
                v-else
                class="q-gutter-x-sm row no-wrap"
                :title="'该缺陷当前处理人为' + props.row.handler"
              >
                <fdev-btn
                  flat
                  :label="item"
                  disable
                  v-for="(item, index) in operation[status[props.row.status]]"
                  :key="index"
                  @click.stop="changeStatus(props.row, item)"
                />
              </div>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>

    <!-- 分配开发人员 -->
    <f-dialog
      title="分配开发人员"
      v-model="confirmModalOpen"
      @shake="confirmToClose('confirmModalOpen')"
    >
      <fdev-select
        v-model="distriUser"
        :options="filterAssignList"
        option-label="user_name_cn"
        option-value="user_name_en"
        use-input
        @filter="userInputFilter"
      >
        <template v-slot:option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label :title="scope.opt.user_name_cn">
                {{ scope.opt.user_name_cn }}
              </fdev-item-label>
              <fdev-item-label caption :title="scope.opt.user_name_en">
                {{ scope.opt.user_name_en }}
              </fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </template>
      </fdev-select>
      <template v-slot:btnSlot>
        <fdev-btn
          outline
          dialog
          label="取消"
          @click="confirmModalOpen = false"
        />
        <fdev-btn
          dialog
          label="确定"
          :loading="userNameLoading"
          @click="assignUserName"
          :disable="!distriUser"
        />
      </template>
    </f-dialog>

    <!-- 打开  重新打开 -->
    <f-dialog v-model="openDefectDialogOpened" title="请填写">
      <fdev-form
        class="q-gutter-y-diaLine"
        ref="qform1"
        @submit="handleOpenDefectDialogOpened"
      >
        <f-formitem label="开发责任人">
          <fdev-select
            use-input
            @filter="devManagerFilter"
            :options="devManagerOptions"
            option-label="user_name_cn"
            option-value="user_name_en"
            ref="taskModel.devManager"
            v-model="$v.taskModel.devManager.$model"
            :rules="[
              () => $v.taskModel.devManager.required || '请选择开发责任人'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.user_name_en">
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="任务"
          v-if="(showTask == '' || showTask == null) && taskOptions.length > 0"
        >
          <fdev-select
            @input="taskChange"
            :options="taskOptions"
            option-label="label"
            option-value="value"
            map-options
            emit-value
            ref="taskModel.taskNo"
            v-model="taskModel.taskNo"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.label">
                    {{ scope.opt.label }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <!-- <fdev-btn
          label="确定"
          @click="handleStep"
          :loading="statusLoading"
          type="submit"
          class="dialog-fdev-btn"
        /> -->
      </fdev-form>

      <template #btnSlot>
        <fdev-btn
          dialog
          @click="$refs['qform1'].submit()"
          label="确定"
          :loading="statusLoading"
        />
      </template>
    </f-dialog>

    <!-- 已修复 -->
    <f-dialog v-model="defectDialogOpened" title="请填写">
      <div class="q-gutter-y-diaLine">
        <f-formitem label="开发责任人">
          <fdev-select
            use-input
            @filter="devManagerFilter"
            :options="devManagerOptions"
            option-label="user_name_cn"
            option-value="user_name_en"
            v-model="listModel.devManager"
            :rules="[() => !!listModel.devManager || '请选择开发责任人']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.user_name_en">
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="开发原因分析" full-width>
          <fdev-input
            type="textarea"
            v-model="listModel.reason"
            filled
            :rules="[() => !!listModel.reason || '请填写开发原因分析']"
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :loading="statusLoading"
          @click="handleDefectDialogOpened"
        />
      </template>
    </f-dialog>

    <!--  缺陷详情-->
    <f-dialog v-model="announcement" right title="缺陷详情">
      <div class="q-gutter-y-diaLine rdia-dc-w row justify-between">
        <f-formitem label="项目名称">
          <div class="ellipsis-2-lines" :title="defect.project_name || '-'">
            {{ defect.project_name || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="优先级">
          <div
            class="ellipsis-2-lines"
            :title="priority[defect.priority] || '-'"
          >
            {{ priority[defect.priority] || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="系统名称">
          <div class="ellipsis-2-lines" :title="defect.system_name || '-'">
            {{ defect.system_name || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="严重性">
          <div
            class="ellipsis-2-lines"
            :title="ponderance[defect.severity] || '-'"
          >
            {{ ponderance[defect.severity] || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="功能模块">
          <div class="ellipsis-2-lines" :title="defect.function_module || '-'">
            {{ defect.function_module || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="缺陷来源">
          <div class="ellipsis-2-lines" :title="defect.flaw_source || '-'">
            {{ defect.flaw_source || '-' }}
            <f-icon
              name="edit"
              class="cursor-pointer text-primary"
              @click="flawSourceDialog = true"
            />
          </div>
        </f-formitem>
        <f-formitem label="实施单元编号">
          <div class="ellipsis-2-lines" :title="defect.redmine_id || '-'">
            {{ defect.redmine_id || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="预计修复时间">
          <div class="ellipsis-2-lines" :title="defect.plan_fix_date || '-'">
            {{ defect.plan_fix_date || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="工单号">
          <div class="ellipsis-2-lines" :title="defect.workNo || '-'">
            {{ defect.workNo || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="缺陷类型">
          <div class="ellipsis-2-lines" :title="defect.flaw_type || '-'">
            {{ defect.flaw_type || '-' }}
            <f-icon
              name="edit"
              class="cursor-pointer text-primary"
              @click="flawTypeDialog = true"
              v-if="defect.handler_en_name === currentUser.user_name_en"
            />
          </div>
        </f-formitem>
        <f-formitem label="系统版本">
          <div class="ellipsis-2-lines" :title="defect.system_version || '-'">
            {{ defect.system_version || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="缺陷编号">
          <div class="ellipsis-2-lines" :title="defect.id || '-'">
            {{ defect.id || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="归属阶段">
          <div class="ellipsis-2-lines" :title="defect.stage || '-'">
            {{ defect.stage || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="状态">
          <div class="ellipsis-2-lines" :title="status[defect.status] || '-'">
            {{ status[defect.status] || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="开发责任人">
          <div class="ellipsis-2-lines" :title="defect.developer_cn || '-'">
            {{ defect.developer_cn || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="创建时间">
          <div class="ellipsis-2-lines" :title="defect.date_submitted || '-'">
            {{ defect.date_submitted || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="分派给">
          <div class="ellipsis-2-lines" :title="defect.handler || '-'">
            {{ defect.handler || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="报告人">
          <div class="ellipsis-2-lines" :title="defect.reporter || '-'">
            {{ defect.reporter || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="文件列表">
          <div v-if="defect.files && defect.files.length > 0">
            <div
              v-for="item in defect.files"
              :key="item.file_id"
              class="cursor-pointer"
              @click="preview(item)"
            >
              <span class="link">{{ item.name }}</span>
              <fdev-tooltip anchor="top middle" self="center middle">
                <span>点击预览</span>
              </fdev-tooltip>
            </div>
          </div>
          <span v-else></span>
        </f-formitem>
        <f-formitem label="摘要">
          <div class="ellipsis-2-lines" :title="defect.summary || '-'">
            {{ defect.summary || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="描述">
          <div class="ellipsis-2-lines" :title="defect.description || '-'">
            {{ defect.description || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="开发原因分析">
          <div class="ellipsis-2-lines" :title="defect.reason || '-'">
            {{ defect.reason || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="重新打开原因">
          <div class="ellipsis-2-lines" :title="defect.reopen_reason || '-'">
            {{ defect.reopen_reason || '-' }}
          </div>
        </f-formitem>
        <f-formitem label="解决时长">
          <span
            v-if="defect.solve_time"
            :title="defect.solve_time | filterSecondToDay"
            class="ellipsis-2-lines"
          >
            {{ defect.solve_time | filterSecondToDay }}天
          </span>
          <span v-else title="-">-</span>
        </f-formitem>
      </div>
    </f-dialog>

    <!-- 文件预览 -->
    <f-dialog right v-model="filePreviewDialog" title="文件预览">
      <div>
        <div
          v-html="filePreview"
          :class="{ 'q-pa-lg': !filePreview.includes('<image') }"
        />
      </div>
    </f-dialog>
    <!-- 修改缺陷来源 -->
    <f-dialog v-model="flawSourceDialog" title="修改缺陷来源">
      <fdev-select
        clearable
        map-options
        emit-value
        option-label="label"
        option-value="label"
        :options="defectFroms"
        v-model="defect.flaw_source"
      >
      </fdev-select>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          outline
          label="取消"
          @click="flawSourceDialog = false"
        ></fdev-btn>
        <fdev-btn dialog label="确定" @click="updateSource" />
      </template>
    </f-dialog>
    <!-- 修改缺陷类型 -->
    <f-dialog v-model="flawTypeDialog" title="修改缺陷类型">
      <fdev-select
        input-debounce="0"
        map-options
        emit-value
        option-label="label"
        option-value="label"
        :options="defectTypes"
        v-model="defect.flaw_type"
      />
      <template v-slot:btnSlot>
        <fdev-btn
          outline
          dialog
          label="取消"
          @click="flawTypeDialog = false"
        ></fdev-btn>
        <fdev-btn dialog label="确定" @click="updateType" />
      </template>
    </f-dialog>
    <!-- 拒绝 -->
    <f-dialog v-model="refuseDialogOpened" title="请确认后填写原因">
      <div class="q-gutter-y-diaLine">
        <f-formitem label="请确认缺陷来源是否正确">
          <fdev-select
            use-input
            :options="defectFrom"
            option-label="label"
            option-value="value"
            map-options
            emit-value
            v-model="flawSource"
            :rules="[() => !!flawSource || '请填写缺陷原因']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="开发原因分析">
          <fdev-input
            type="textarea"
            v-model="listModel.reason"
            :rules="[() => !!listModel.reason || '请填写开发原因分析']"
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          label="确定"
          :loading="statusLoading"
          dialog
          @click="handleRefuseDialogSubmit"
        />
      </template>
    </f-dialog>
    <!-- 延期修复 -->
    <f-dialog v-model="delayDialogOpened" title="请填写">
      <div class="q-gutter-y-diaLine">
        <f-formitem label="开发原因分析">
          <fdev-input
            type="textarea"
            v-model="delayReason"
            :rules="[() => !!delayReason || '请填写开发原因分析']"
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :loading="statusLoading"
          @click="handleDelayDialogSubmit"
        />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import { perform, validate, successNotify } from '@/utils/utils';
import { listModel, taskModel } from './model';
import { required } from 'vuelidate/lib/validators';
import { priorityMapColor } from '../../modules/Rqr/model';
import { queryOrderInfoByNo } from '@/modules/HomePage/services/methods.js';
export default {
  name: 'Defect',
  components: { Loading },
  data() {
    return {
      orderType: '', //安全 功能
      showTask: '',
      taskOptions: [],
      delayReason: '',
      flawSource: null,
      ...perform,
      pagination: {},
      terms: [],
      loading: false,
      filter: '',
      confirmModalOpen: false,
      distriUser: null,
      assignRow: {},
      filterAssignList: [],
      defectDialogOpened: false,
      refuseDialogOpened: false,
      delayDialogOpened: false,
      openDefectDialogOpened: false, //打开  重新打开
      devManagerOptions: [],
      listModel: listModel(),
      taskModel: taskModel(),
      announcement: false,
      newMsgList: [],
      defect: {},
      filePreview: '',
      filePreviewDialog: false,
      showDownFile: false,
      flawTypeDialog: false,
      flawSourceDialog: false
    };
  },
  validations: {
    taskModel: {
      devManager: {
        required
      }
    }
  },
  props: {
    needSelect: {
      type: Boolean,
      default: true
    },
    defectList: {
      type: Array
    },
    isLoginUserList: {
      type: Array
    },
    userNameLoading: {
      type: Boolean,
      default: false
    },
    statusLoading: {
      type: Boolean,
      default: false
    },
    jobName: {
      type: String,
      default: ''
    }
  },
  watch: {
    pagination(val) {
      this.saveCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('jobForm', ['taskAllData']),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('dashboard', ['issueDetailById']),
    ...mapState('userActionSaveHomePage/myDefect', [
      'termsApp',
      'visibleCols',
      'currentPage'
    ]),
    ...mapGetters('userActionSaveHomePage/myDefect', ['searchValueFilter']),
    //拒绝 缺陷来源
    defectFrom() {
      if (this.orderType == 'security') {
        // 安全测试
        return [
          { value: '水平越权漏洞', label: '水平越权漏洞' },
          { value: '重放攻击漏洞', label: '重放攻击漏洞' },
          { value: '安全功能设计机制不健全', label: '安全功能设计机制不健全' }
        ];
      } else {
        // 功能测试
        return this.flawSourceList;
      }
    },
    //修改 缺陷来源
    defectFroms() {
      if (this.orderType == 'security') {
        // 安全测试
        return [
          { value: '水平越权漏洞', label: '水平越权漏洞' },
          { value: '重放攻击漏洞', label: '重放攻击漏洞' },
          { value: '安全功能设计机制不健全', label: '安全功能设计机制不健全' }
        ];
      } else {
        // 功能测试
        return this.flawSourceLists;
      }
    },
    // 缺陷类型
    defectTypes() {
      if (this.orderType == 'security') {
        // 安全测试
        return [{ value: '安全缺陷', label: '安全缺陷' }];
      } else {
        // 功能测试
        return this.defectOptions;
      }
    },
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    },
    columns() {
      const columns = [
        {
          name: 'task_name',
          label: '任务名称',
          align: 'left',
          field: row => (this.jobName ? this.jobName : row.task_name)
        },
        {
          name: 'summary',
          label: '摘要',
          align: 'left',
          field: 'summary'
        },
        {
          name: 'description',
          label: '描述',
          align: 'left',
          field: 'description'
        },
        {
          name: 'priority',
          label: '优先级',
          align: 'left',
          sortable: true,
          field: row => this.priority[row.priority],
          sort: (a, b) => {
            return this.sortPriority(a, b);
          }
        },
        {
          name: 'status',
          label: '状态',
          align: 'left',
          sortable: true,
          field: row => this.status[row.status],
          sort: (a, b) => {
            return this.sortStatus(a, b);
          }
        },
        {
          name: 'handler',
          label: '指派人',
          align: 'left',
          field: 'handler'
        },
        {
          name: 'developer_cn',
          label: '开发责任人',
          align: 'left',
          field: 'developer_cn'
        },
        {
          name: 'flaw_source',
          label: '缺陷来源',
          align: 'left',
          field: 'flaw_source',
          sortable: true,
          sort: (a, b) => {
            return this.sortSource(a, b);
          }
        },
        {
          name: 'flaw_type',
          label: '缺陷类型',
          align: 'left',
          field: 'flaw_type',
          sortable: true,
          sort: (a, b) => {
            return this.sortType(a, b);
          }
        },
        {
          name: 'date_submitted',
          label: '提出时间',
          align: 'left',
          sortable: true,
          field: 'date_submitted'
        },
        {
          name: 'reporter',
          label: '提出人',
          align: 'left',
          field: 'reporter'
        },
        {
          name: 'reason',
          label: '开发原因分析',
          align: 'left',
          field: 'reason'
        },
        {
          name: 'reopen_reason',
          label: '重新打开原因',
          align: 'left',
          field: 'reopen_reason',
          copy: true
        },
        {
          name: 'stage',
          label: '归属阶段',
          align: 'left',
          field: 'stage'
        },
        {
          name: 'solve_time',
          label: '解决时长（天）',
          align: 'left',
          field: 'solve_time'
        }
      ];
      columns.push({
        name: 'operation',
        label: '操作',
        align: 'left',
        field: row => this.operation[this.status[row.status]]
      });
      return columns;
    }
  },
  filters: {
    statusFilter(val) {
      return priorityMapColor[val];
    },
    filterSecondToDay(second) {
      return Number(second) ? (second / 60 / 60 / 24).toFixed(3) : '-';
    }
  },
  methods: {
    ...mapActions('jobForm', ['queryTasks']),
    ...mapActions('user', {
      updateFdevMantis: 'updateFdevMantis',
      updateAssignUser: 'updateAssignUser',
      fetch: 'fetch'
    }),
    ...mapActions('dashboard', ['queryIssueDetailById']),
    ...mapMutations('userActionSaveHomePage/myDefect', [
      'saveSearchValueApp',
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveShowDownFile'
    ]),
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },

    handleStep() {
      this.$v.taskModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('taskModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.taskModel.$invalid) {
        return;
      }
    },
    // 点击搜索按钮,进行模糊搜索
    setSelect() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    async changeStatus(defect, item) {
      if (item === '已修复') {
        this.handleDevManagerOptions();
        this.assignRow = defect;
        this.defectDialogOpened = true;
        this.listModel = listModel();
        if (defect.developer) {
          this.listModel.devManager = {
            user_name_en: defect.developer,
            user_name_cn: defect.developer_cn
          };
        }
      } else if (item === '拒绝') {
        this.assignRow = defect;
        this.flawSource = defect.flaw_source;
        let res = await queryOrderInfoByNo({
          workNo: defect.workNo
        });
        this.orderType = res.orderType;
        this.refuseDialogOpened = true;
      } else if (item === '延迟修复') {
        this.assignRow = defect;
        this.delayDialogOpened = true;
      } else if (item === '分配') {
        this.assignRow = defect;
        this.confirmModalOpen = true;
      } else if (item === '打开' || item === '重新打开') {
        this.taskModel.taskNo = '';
        if (defect.task_id != '' || defect.task_id != null) {
          this.taskModel.taskNo = defect.task_id;
        }
        this.openDefectDialogOpened = true;
        this.showTask = defect.task_id;
        this.handleDevManagerOptions();
        this.taskModel = taskModel();
        if (defect.developer) {
          this.taskModel.devManager = {
            user_name_en: defect.developer,
            user_name_cn: defect.developer_cn
          };
        }
        await this.queryTasks({
          workNo: defect.workNo
        });
        this.taskAllData.taskList = this.taskAllData.taskList || [];
        this.taskOptions = this.taskAllData.taskList.map(item => {
          return {
            label: item.name,
            value: item.id
          };
        });
        this.assignRow = defect;
      } else {
        return this.$q
          .dialog({
            title: '缺陷状态更改',
            message: '确定更改此缺陷的状态吗？',
            ok: '确定',
            cancel: '取消'
          })
          .onOk(async () => {
            defect.status = this.statusName[item];
            this.$emit('update-status', defect);
          });
      }
    },
    defectFilter(rows, terms, cols, cellValue) {
      const lowerTerms = this.termsApp.searchValue.map(item =>
        item.toLowerCase()
      );
      return rows.filter(row => {
        return lowerTerms.every(term => {
          if (term.startsWith('__') || term === '') {
            return true;
          }
          return cols.some(col => {
            let value = '';
            if (Array.isArray(cellValue(col, row))) {
              value = cellValue(col, row).map(user => user.user_name_cn);
              return (
                value
                  .toString()
                  .toLowerCase()
                  .indexOf(term) > -1
              );
            } else {
              return (
                (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1
              );
            }
          });
        });
      });
    },
    sortSource(a, b) {
      let order = [
        '需规问题',
        '功能实现不完整',
        '功能实现错误',
        '历史遗留问题',
        '优化建议',
        '后台问题',
        '打包问题',
        '数据问题',
        '环境问题',
        '其他原因',
        '业务需求问题'
      ];
      return order.indexOf(a) - order.indexOf(b);
    },
    sortType(a, b) {
      let order = [
        '需求问题',
        '环境问题',
        '数据问题',
        '文档问题',
        '应用缺陷',
        '其他问题'
      ];
      return order.indexOf(a) - order.indexOf(b);
    },
    sortPriority(a, b) {
      let order = ['无', '低', '中', '高', '紧急', '非常紧急'];
      return order.indexOf(a) - order.indexOf(b);
    },
    sortStatus(a, b) {
      let order = [
        '新建',
        '打开',
        '重新打开',
        '延迟修复',
        '已修复',
        '待确认',
        '拒绝',
        '确认拒绝',
        '关闭',
        '已关闭'
      ];
      return order.indexOf(a) - order.indexOf(b);
    },
    isHandler(handler_en_name) {
      return handler_en_name === this.currentUser.user_name_en;
    },
    userInputFilter(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.filterAssignList = this.isLoginUserList.filter(user => {
          return (
            user.user_name_cn.toLowerCase().includes(needle) ||
            user.user_name_en.toLowerCase().includes(needle)
          );
        });
      });
    },
    async assignUserName() {
      //重新打开：51 当为重新打开时仍然送50
      if (this.assignRow.status === '51') {
        this.assignRow.status = '50';
      }
      let user = { ...this.assignRow };
      user.user_name = this.distriUser.user_name_cn;
      user.handler_en_name = this.distriUser.user_name_en;
      await this.assignUser(user);
      this.distriUser = null;
      this.confirmModalOpen = false;
      this.$emit('init');
    },
    devManagerFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.devManagerOptions = this.userList
          .map(user => {
            return {
              user_name_en: user.user_name_en,
              user_name_cn: user.user_name_cn
            };
          })
          .filter(
            v =>
              v.user_name_cn.toLowerCase().includes(needle) ||
              v.user_name_en.toLowerCase().includes(needle)
          );
      });
    },
    taskChange(val) {
      this.taskAllData.taskList.find(item => {
        if (val == item.id) {
          this.assignRow.handler = item.developer[0].user_name_cn;
          this.assignRow.handler_en_name = item.developer[0].user_name_en;
          this.assignRow.app_name = item.project_name;
        }
      });
    },
    async updateTypeStatus(defect) {
      await this.updateFdevMantis(defect);
      successNotify('修改缺陷类型成功');
    },
    async updateSourceStatus(defect) {
      await this.updateFdevMantis(defect);
      successNotify('修改缺陷来源成功');
    },
    async assignUser(defect) {
      await this.updateAssignUser(defect);
      successNotify('分配成功');
    },
    async handleOpenDefectDialogOpened() {
      let taskNo = '';
      if (this.assignRow.task_id != '' || this.assignRow.task_id != null) {
        taskNo = this.assignRow.task_id;
      } else {
        taskNo = this.taskModel.taskNo;
      }
      this.assignRow = {
        ...this.assignRow,
        task_id: taskNo,
        developer: this.taskModel.devManager
          ? this.taskModel.devManager.user_name_en
          : '',
        developer_cn: this.taskModel.devManager
          ? this.taskModel.devManager.user_name_cn
          : '',
        status: this.statusName['重新打开']
          ? this.statusName['打开']
          : this.statusName['打开']
      };
      this.openDefectDialogOpened = false;
      this.$emit('update-status', this.assignRow);
    },
    async handleDefectDialogOpened() {
      this.assignRow.developer = this.listModel.devManager.user_name_en;
      this.assignRow.developer_cn = this.listModel.devManager.user_name_cn;
      this.assignRow.status = this.statusName['已修复'];
      this.assignRow.reason = this.listModel.reason;
      this.defectDialogOpened = false;
      this.$emit('update-status', this.assignRow);
    },
    // 拒绝提示
    async handleRefuseDialogSubmit() {
      if (!this.flawSource) {
        successNotify('请选择缺陷原因!');
        return;
      }
      if (!this.listModel.reason) {
        successNotify('请填写开发原因分析!');
        return;
      }
      this.assignRow.flaw_source = this.flawSource;
      this.assignRow.status = this.statusName['拒绝'];
      this.assignRow.reason = this.listModel.reason;
      this.refuseDialogOpened = false;
      this.$emit('update-status', this.assignRow);
    },
    //延迟修复
    async handleDelayDialogSubmit() {
      this.assignRow.status = this.statusName['延迟修复'];
      this.assignRow.reason = this.delayReason;
      this.delayDialogOpened = false;
      this.$emit('update-status', this.assignRow);
    },
    confirmToClose(key) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this[key] = false;
        });
    },
    async popDefect(row) {
      let res = await queryOrderInfoByNo({
        workNo: row.workNo
      });
      this.orderType = res.orderType;
      this.defect = row;
      this.announcement = true;
      await this.queryIssueDetailById({ id: row.id });
      this.$set(this.defect, 'files', this.issueDetailById.files);
    },
    preview(file) {
      this.filePreviewDialog = true;
      if (file.file_type.includes('image')) {
        let type = file.file_type.includes('image/jpeg') ? 'jpeg' : 'png';
        let content = file.content;
        this.filePreview = `<image style='width:560px;' src='data:image/${type};base64,${content}'/>`;
      } else {
        this.filePreview = file.content.replace(/\n/g, '</br>');
      }
    },
    toggleShow() {
      this.$emit('changeShow', this.termsApp.showDownFile);
    },
    async updateType() {
      //重新打开：51 当为重新打开时仍然送50
      if (this.defect.status === '51') {
        this.defect.status = '50';
      }
      this.flawTypeDialog = false;
      this.$emit('update-status', this.defect);
    },
    async updateSource() {
      //重新打开：51 当为重新打开时仍然送50
      if (this.defect.status === '51') {
        this.defect.status = '50';
      }
      this.flawSourceDialog = false;
      this.$emit('update-sourcestatus', this.defect);
    },
    async handleDevManagerOptions() {
      if (this.userList.length === 0) {
        await this.fetch();
      }
      this.devManagerOptions = this.userList.map(user => {
        return {
          user_name_en: user.user_name_en,
          user_name_cn: user.user_name_cn
        };
      });
    }
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns([
        'task_name',
        'summary',
        'description',
        'priority',
        'status',
        'handler',
        'date_submitted',
        'reporter',
        'operation'
      ]);
    }
  }
};
</script>
<style lang="stylus" scoped>
.btn {
  width: 4.5rem;
  margin-right: 2px;
}

.link {
  color: #2196f3;
}

.hover {
  cursor: auto;
}

.dialog-fdev-btn {
  min-width: 74px;
  max-width: 74px;
  width: 74px;
  height: 36px;
  float: right;
  margin-top: 20px;
}

>>> label.q-field--with-bottom {
  padding-bottom: 0px;
}
</style>
