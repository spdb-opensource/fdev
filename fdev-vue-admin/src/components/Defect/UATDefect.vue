<template>
  <div>
    <Loading :visible="loading">
      <fdev-table
        class="my-sticky-column-table"
        :data="defectList"
        :columns="columns"
        row-key="id"
        :filter="searchValueFilter"
        :filter-method="defectFilter"
        :pagination.sync="pagination"
        :visible-columns="uatVisibleCols"
        no-export
        titleIcon="list_s_f"
        title="UAT缺陷列表"
        :on-select-cols="saveUATVisibleColumns"
      >
        <template v-slot:top-bottom v-if="needSelect">
          <f-formitem label="搜索条件">
            <fdev-select
              multiple
              use-input
              placeholder="输入关键字，回车区分"
              hide-dropdown-icon
              :value="UATtermsApp.searchValue"
              @input="saveUATSearchValueApp($event)"
              @new-value="addTerm"
              class="table-head-input"
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
            :value="UATtermsApp.showDownFile"
            label="显示归档缺陷"
            @input="
              saveUATShowDownFile($event);
              toggleShow();
            "
            left-label
            v-if="needSelect"
          />
        </template>

        <!-- 任务名 -->
        <template v-slot:body-cell-taskName="props">
          <fdev-td class="text-ellipsis">
            <span v-if="jobName" :title="jobName"> {{ jobName }} </span>
            <span v-if="!jobName && !props.row.taskName" title="-"> - </span>
            <router-link
              :to="`/job/list/${props.row.taskId}`"
              class="link text-ellipsis"
              v-if="!jobName"
            >
              <span :title="props.row.taskName">{{ props.row.taskName }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.taskName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <!-- 摘要 -->
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
          <fdev-td
            :title="props.value"
            class="text-ellipsis cursor-pointer"
            @click="popDefect(props.row)"
          >
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

        <template v-slot:body-cell-assignee="props">
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

        <template v-slot:body-cell-defectSource="props">
          <fdev-td
            :title="defectSource[props.value]"
            class="text-ellipsis cursor-pointer"
            @click="popDefect(props.row)"
          >
            {{ defectSource[props.value] }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-defectType="props">
          <fdev-td
            :title="props.value"
            class="text-ellipsis cursor-pointer"
            @click="popDefect(props.row)"
          >
            {{ props.value }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-created="props">
          <fdev-td
            :title="props.value"
            class="text-ellipsis cursor-pointer"
            @click="popDefect(props.row)"
          >
            {{ props.value }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-creator="props">
          <fdev-td
            :title="props.value"
            class="text-ellipsis cursor-pointer"
            @click="popDefect(props.row)"
          >
            {{ props.value }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-defectCause="props">
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

        <template v-slot:body-cell-belongStage="props">
          <fdev-td
            :title="props.value"
            class="text-ellipsis cursor-pointer"
            @click="popDefect(props.row)"
          >
            {{ props.value }}
          </fdev-td>
        </template>

        <!-- <template v-slot:body-cell-solve_time="props">
            <fdev-td class="text-ellipsis cursor-pointer" @click="popDefect(props.row)">
              {{ props.value | filterSecondToDay }}
            </fdev-td>
          </template> -->

        <template v-slot:body-cell-operation="props">
          <fdev-td auto-width class="hover">
            <div
              class="q-gutter-x-sm row no-wrap"
              :title="
                !isHandler(props.row.assigneeEn) &&
                  '该缺陷当前处理人为' + props.row.assignee
              "
            >
              <fdev-btn
                flat
                :label="item"
                :disable="!isHandler(props.row.assigneeEn)"
                v-for="(item, index) in props.value"
                :key="index"
                @click="changeStatus(props.row, item)"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <!-- 分配 -->
    <f-dialog
      v-model="confirmModalOpen"
      @shake="confirmToClose('confirmModalOpen')"
      title="指定受理人"
    >
      <fdev-select
        v-model="distriUser"
        :options="filterAssignList"
        option-label="user_name_cn"
        option-value="user_name_en"
        use-input
        @filter="userInputFilter"
        class="form-width"
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
          dialog
          outline
          label="取消"
          @click="confirmModalOpen = false"
        />
        <div>
          <fdev-btn
            dialog
            label="确定"
            :loading="userNameLoading"
            @click="assignUserName"
            :disable="!distriUser"
          />
          <fdev-tooltip position="top" v-if="!distriUser">
            请选择受理人
          </fdev-tooltip>
        </div>
      </template>
    </f-dialog>
    <!-- false 不会显示 -->
    <!-- <f-dialog
      title="受理人"
      v-model="confirmSlrModalOpen"
      @shake="confirmToClose('confirmSlrModalOpen')"
    >
      <fdev-select
        v-model="slrUser"
        :options="filterAssignList"
        option-label="user_name_cn"
        option-value="user_name_en"
        use-input
        @filter="userInputFilter"
        class="form-width"
      >
        <template v-slot:option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label>
                {{ scope.opt.user_name_cn }}
              </fdev-item-label>
              <fdev-item-label caption>
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
          @click="confirmSlrModalOpen = false"
        />
        <fdev-btn
          dialog
          label="确定"
          :loading="userNameLoading"
          @click="assignSlrUserName"
          :disable="!slrUser"
        />
      </template>
    </f-dialog> -->
    <!--点击 打开 重新打开 -->
    <f-dialog right v-model="openDefectDialogOpened" title="请填写">
      <fdev-form
        class="q-gutter-y-diaLine rdia-dc-w row justify-between"
        ref="qform1"
        @submit="handleOpenDefectDialogOpened"
        @validation-error="validationError('1')"
      >
        <f-formitem label="受理人">
          <fdev-select
            input-debounce="0"
            use-input
            @filter="devManagerFilter"
            :options="devManagerOptions"
            option-label="user_name_cn"
            option-value="user_name_en"
            ref="taskModel.devManager"
            v-model="taskModel.devManager"
            :rules="[() => taskModel.devManager || '请选择受理人']"
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
        <f-formitem label="任务" required>
          <fdev-select
            @input="taskChange"
            :options="UATtaskAllData"
            option-label="name"
            option-value="id"
            map-options
            emit-value
            ref="taskModel.taskNo"
            v-model="taskModel.taskNo"
            :rules="[() => !!taskModel.taskNo || '请选择任务']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="开发人员">
          <fdev-select
            use-input
            @filter="devManagerFilter"
            :options="devManagerOptions"
            option-label="user_name_cn"
            option-value="user_name_en"
            ref="taskModel.devManager"
            v-model="developer"
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
        <f-formitem label="优先级">
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('priority')"
            v-model="defectItem.priority"
            :rules="[val => (!!val && val != -1) || '请选择优先级']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="实施单元编号" required>
          <fdev-input
            v-model="defectItem.unitNo"
            autofocus
            :rules="[
              val => {
                if (!val) {
                  return '实施单元编号不能为空';
                }
                return true;
              }
            ]"
          />
          <div class="margin-top-10">
            格式应为“实施单元2019XXXXxxx-xxx” （X为模块名称x为数字）！
          </div>
        </f-formitem>
        <f-formitem label="严重性">
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('orderSeverity')"
            v-model="defectItem.orderSeverity"
            :rules="[val => (!!val && val != -1) || '请选择严重性']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="缺陷来源">
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('defectSource')"
            v-model="defectItem.defectSource"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="缺陷类型" required>
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('defectType')"
            v-model="defectItem.defectType"
            :rules="[val => (!!val && val != -1) || '请选择缺陷类型']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="归属阶段">
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('belongStage')"
            v-model="defectItem.belongStage"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="摘要">
          <fdev-input
            v-model="defectItem.summary"
            :rules="[val => !!val || `摘要不能为空`]"
          />
        </f-formitem>
        <f-formitem label="描述">
          <fdev-input v-model="defectItem.description" />
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
    <f-dialog
      @before-hide="defectHide"
      v-model="defectDialogOpened"
      title="请填写"
      right
    >
      <fdev-form
        class="q-gutter-y-diaLine rdia-dc-w row justify-between"
        ref="qform2"
        @submit="handleDefectDialogOpened"
        @validation-error="validationError('2')"
      >
        <!-- <field label="受理人"  >
          <fdev-select
            input-debounce="0"
            
            
            use-input
            @filter="devManagerFilter"
            :options="devManagerOptions"
            option-label="user_name_cn"
            option-value="user_name_en"
            v-model="listModel.devManager"
            :rules="[() => !!listModel.devManager || '请选择受理人']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </field> -->
        <f-formitem label="开发人员">
          <fdev-select
            input-debounce="0"
            use-input
            @filter="devManagerFilter"
            :options="devManagerOptions"
            option-label="user_name_cn"
            option-value="user_name_en"
            ref="taskModel.devManager"
            v-model="developer"
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
        <f-formitem label="实施单元编号">
          <fdev-input
            v-model="defectItem.unitNo"
            autofocus
            :rules="[
              val => {
                if (!val) {
                  return '实施单元编号不能为空';
                }
                return true;
              }
            ]"
          />
          <div class="margin-top-10">
            格式应为“实施单元2019XXXXxxx-xxx” （X为模块名称x为数字）！
          </div>
        </f-formitem>
        <f-formitem label="归属阶段">
          <fdev-select
            use-input
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="stageOption"
            v-model="defectItem.belongStage"
            :rules="[val => (!!val && val != -1) || '请选择归属阶段']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="缺陷类型">
          <fdev-select
            input-debounce="0"
            use-input
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="defectOptions"
            v-model="defectItem.defectType"
            :rules="[val => (!!val && val != -1) || '请选择缺陷类型']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="迭代批次">
          <fdev-select
            map-options
            option-label="label"
            option-value="value"
            emit-value
            :options="iterationOptions"
            v-model="defectItem.iterations"
            :rules="[val => (!!val && val != -1) || '请选择迭代批次']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="优先级">
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('priority')"
            v-model="defectItem.priority"
            :rules="[val => (!!val && val != -1) || '请选择优先级']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="严重性">
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('orderSeverity')"
            v-model="defectItem.orderSeverity"
            :rules="[val => (!!val && val != -1) || '请选择严重性']"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="缺陷来源">
          <fdev-select
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :options="selectOption('defectSource')"
            v-model="defectItem.defectSource"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="摘要">
          <fdev-input
            v-model="defectItem.summary"
            :rules="[val => !!val || `摘要不能为空`]"
          />
        </f-formitem>
        <f-formitem label="描述">
          <fdev-input v-model="defectItem.description" />
        </f-formitem>
        <f-formitem label="开发原因分析">
          <fdev-input
            type="textarea"
            v-model="listModel.reason"
            :rules="[() => !!listModel.reason || '请填写开发原因分析']"
          />
        </f-formitem>
        <!-- <fdev-btn
          label="确定"
          :loading="statusLoading"
          type="submit"
          class="dialog-fdev-btn"
        /> -->
      </fdev-form>

      <template #btnSlot>
        <fdev-btn
          dialog
          @click="$refs['qform2'].submit()"
          label="确定"
          :loading="statusLoading"
        />
      </template>
    </f-dialog>
    <!-- 缺陷详情 -->
    <f-dialog title="缺陷详情" f-dc v-model="announcement" v-if="announcement">
      <f-formitem label="任务名称">
        <div class="ellipsis-2-lines" :title="defect.taskName || '-'">
          {{ defect.taskName || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="优先级">
        <div
          class="ellipsis-2-lines"
          :title="UATpriority[defect.priority] || '-'"
        >
          {{ UATpriority[defect.priority] || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="缺陷来源">
        <div
          class="ellipsis-2-lines"
          :title="defectSource[defect.defectSource] || '-'"
        >
          {{ defectSource[defect.defectSource] || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="实施单元编号">
        <div class="ellipsis-2-lines" :title="defect.unitNo || '-'">
          {{ defect.unitNo || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="缺陷类型">
        <div
          class="ellipsis-2-lines"
          :title="defectType[defect.defectType] || '-'"
        >
          {{ defectType[defect.defectType] || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="归属阶段">
        <div
          class="ellipsis-2-lines"
          :title="belongStage[defect.belongStage] || '-'"
        >
          {{ belongStage[defect.belongStage] || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="状态">
        <div class="ellipsis-2-lines" :title="UATstatus[defect.status] || '-'">
          {{ UATstatus[defect.status] || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="开发人员">
        <div class="ellipsis-2-lines" :title="defect.develop || '-'">
          {{ defect.develop || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="创建时间">
        <div
          class="ellipsis-2-lines"
          :title="defect.created.substr(0, 19).replace('T', ' ')"
        >
          {{ defect.created.substr(0, 19).replace('T', ' ') }}
        </div>
      </f-formitem>
      <f-formitem label="受理人">
        <div class="ellipsis-2-lines" :title="defect.assignee || '-'">
          {{ defect.assignee || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="报告人">
        <div class="ellipsis-2-lines" :title="defect.creator || '-'">
          {{ defect.creator || '-' }}
        </div>
      </f-formitem>
      <f-formitem label="缺陷编号">
        <div class="ellipsis-2-lines" :title="defect.issueKey || '-'">
          {{ defect.issueKey || '-' }}
        </div>
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
        <div class="ellipsis-2-lines" :title="defect.defectCause || '-'">
          {{ defect.defectCause || '-' }}
        </div>
      </f-formitem>
    </f-dialog>
    <!-- prview  无调用  不显示 -->
    <f-dialog v-model="filePreviewDialog">
      <f-block view="Lhh lpR fff" container class="bg-white dialog-height">
        <fdev-header bordered>
          <fdev-bar>
            <fdev-space />
            <fdev-btn flat v-close-popup round icon="close" />
          </fdev-bar>
        </fdev-header>
        <div>
          <div
            v-html="filePreview"
            :class="{ 'q-pa-lg': !filePreview.includes('<image') }"
          />
        </div>
      </f-block>
    </f-dialog>
    <!--flexSelect 无调用 不显示 -->
    <f-dialog v-model="flexSelectDialog">
      <fdev-card style="min-width: 400px">
        <fdev-card-section>
          <div class="text-h6">修改{{ flexType1 | filterType }}</div>
        </fdev-card-section>
        <fdev-select
          class="q-mx-md"
          map-options
          emit-value
          option-label="label"
          option-value="value"
          :options="selectOption(flexType1)"
          v-model="chooseType"
        >
        </fdev-select>
        <div
          v-if="flexType1 == 'defectType' && chooseType == '-1'"
          class="redMsg"
        >
          缺陷类型不能为空
        </div>
        <fdev-card-actions align="right" class="text-primary">
          <fdev-btn
            flat
            label="取消"
            @click="flexSelectDialog = false"
          ></fdev-btn>
          <fdev-btn flat label="确定" @click="updateSource" />
        </fdev-card-actions>
      </fdev-card>
    </f-dialog>
    <!-- flexDate 无调用  不显示 -->
    <f-dialog v-model="flexDialog" persistent title="修改">
      <div v-if="!(flexType == 'defectCause')">
        <fdev-input
          v-if="flexType === 'unitNo'"
          v-model="chooseType"
          autofocus
          :rules="[
            val => {
              if (!val) {
                return '实施单元编号不能为空';
              }
              return true;
            }
          ]"
        />
        <fdev-input
          v-else
          v-model="chooseType"
          autofocus
          :rules="[val => !!val || `${filterType(flexType)}不能为空`]"
        />
        <div v-if="flexType === 'unitNo'" class="description margin-top-10">
          格式应为“实施单元2019XXXXxxx-xxx” （X为模块名称x为数字）！
        </div>
      </div>
      <div v-else>
        <fdev-input
          v-model="chooseType"
          type="textarea"
          :rules="[val => !!val || `${filterType(flexType)}不能为空`]"
        />
      </div>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="确定" @click="updateType1" />
      </template>
    </f-dialog>
    <!-- 拒绝 -->
    <f-dialog v-model="refuseDialogOpened" title="请确认后填写原因">
      <fdev-form
        ref="qform3"
        class="bg-white q-pa-md"
        @submit="handleRefuseDialogSubmit"
      >
        <f-formitem label="请确认缺陷来源是否正确" class="">
          <fdev-select
            map-options
            emit-value
            :options="flawSourceLists"
            option-label="label"
            option-value="value"
            v-model="flawSource"
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
        <div style="height:20px"></div>
        <f-formitem label="开发原因分析">
          <fdev-input type="textarea" v-model="listModel.reason" />
        </f-formitem>
        <!-- <div class="text-right">
          <fdev-btn
            label="确定"
            :loading="statusLoading"
            type="submit"
            color="primary"
            class="dialog-fdev-btn"
          />
        </div> -->
      </fdev-form>

      <template #btnSlot>
        <fdev-btn
          dialog
          @click="$refs['qform3'].submit()"
          label="确定"
          :loading="statusLoading"
        />
      </template>
    </f-dialog>
    <!-- 延期 -->
    <f-dialog v-model="delayDialogOpened" title="请填写">
      <fdev-form ref="qform4" @submit="handleDelayDialogSubmit">
        <f-formitem label="开发原因分析">
          <fdev-input
            type="textarea"
            v-model="delayReason"
            dialog-fdev-btn
            :rules="[() => !!delayReason || '请填写开发原因分析']"
          />
        </f-formitem>
      </fdev-form>
      <template #btnSlot>
        <fdev-btn dialog @click="$refs['qform4'].submit()" label="确定" />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { priorityMapColor } from '../../modules/Rqr/model';
import Loading from '@/components/Loading';
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import { perform, successNotify } from '@/utils/utils';
import { listModel, taskModel } from './model';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'Defect',
  components: { Loading },
  data() {
    return {
      developer: {},
      defectItem: {},
      chooseType: '',
      showTask: '',
      delayReason: '',
      flawSource: null,
      ...perform,
      pagination: {},
      terms: [],
      loading: false,
      filter: '',
      confirmModalOpen: false,
      confirmSlrModalOpen: false,
      distriUser: null,
      slrUser: null,
      assignRow: {},
      filterAssignList: [],
      defectDialogOpened: false,
      refuseDialogOpened: false,
      delayDialogOpened: false,
      openDefectDialogOpened: false,
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
      flawSourceDialog: false,
      flexDialog: false,
      flexType: '',
      ascription: '-1',
      redmine_id: '',
      defectType1: '-1',
      iteration: '',
      flexType1: '',
      flexSelectDialog: false,
      updateStatu: ''
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
      default: false
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
    },
    'taskModel.devManager'(user) {
      this.queryTestTask({
        id: user.id
      });
    }
  },
  computed: {
    ...mapState('jobForm', ['UATtaskAllData']),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('dashboard', ['issueDetailById']),
    ...mapState('userActionSaveHomePage/myDefect', [
      'UATtermsApp',
      'uatVisibleCols',
      'currentPage'
    ]),
    ...mapGetters('userActionSaveHomePage/myDefect', ['searchValueFilter']),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    },
    columns() {
      const columns = [
        {
          name: 'taskName',
          label: '任务名称',
          align: 'left',
          field: row => (this.jobName ? this.jobName : row.taskName),
          copy: true
        },
        {
          name: 'summary',
          label: '摘要',
          align: 'left',
          field: 'summary',
          copy: true
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
          field: row => this.UATpriority[row.priority] || '无',
          sort: (a, b) => {
            return this.sortPriority(a, b);
          }
        },
        {
          name: 'status',
          label: '状态',
          align: 'left',
          sortable: true,
          field: row => this.UATstatus[row.status],
          sort: (a, b) => {
            return this.sortStatus(a, b);
          }
        },
        {
          name: 'assignee',
          label: '受理人',
          align: 'left',
          field: 'assignee'
        },
        {
          name: 'develop',
          label: '开发人员',
          align: 'left',
          field: 'develop'
        },
        {
          name: 'unitNo',
          label: '对应实施单元编号',
          align: 'left',
          field: 'unitNo'
        },
        {
          name: 'orderSeverity',
          label: '严重程度',
          align: 'left',
          field: row => this.severity[row.orderSeverity]
        },
        {
          name: 'iterations',
          label: '迭代批次',
          align: 'left',
          field: row => {
            let obj = this.iterationOptions.find(
              ele => ele.value === row.iterations
            );
            return obj ? obj.label : '无';
          }
        },
        {
          name: 'defectSource',
          label: '缺陷来源',
          align: 'left',
          field: 'defectSource',
          sortable: true,
          sort: (a, b) => {
            return this.sortSource(a, b);
          }
        },
        {
          name: 'defectType',
          label: '缺陷类型',
          align: 'left',
          field: row => this.defectType[row.defectType],
          sortable: true,
          sort: (a, b) => {
            return this.sortType(a, b);
          }
        },
        {
          name: 'created',
          label: '提出时间',
          align: 'left',
          sortable: true,
          field: row => row.created.substr(0, 19).replace('T', ' ')
        },
        {
          name: 'creator',
          label: '提出人',
          align: 'left',
          field: 'creator'
        },
        {
          name: 'defectCause',
          label: '开发原因分析',
          align: 'left',
          field: 'defectCause',
          copy: true
        },
        {
          name: 'belongStage',
          label: '归属阶段',
          align: 'left',
          field: row => this.belongStage[row.belongStage]
        }
      ];
      columns.push({
        name: 'operation',
        label: '操作',
        align: 'left',
        field: row => this.UAToperation[this.UATstatus[row.status]]
      });
      return columns;
    }
  },
  filters: {
    // filterSecondToDay(second) {
    //   return second ? (second / 60 / 60 / 24).toFixed(3) : '';
    // },
    statusFilter(val) {
      return priorityMapColor[val];
    },
    filterType(type) {
      let name = '';
      switch (type) {
        case 'unitNo':
          name = '实施单元编号';
          break;
        case 'developer_cn':
          name = '开发人员';
          break;
        case 'priority':
          name = '优先级';
          break;
        case 'defectSource':
          name = '缺陷来源';
          break;
        case 'defectType':
          name = '缺陷类型';
          break;
        case 'belongStage':
          name = '归属阶段';
          break;
        case 'description':
          name = '描述';
          break;
        case 'summary':
          name = '摘要';
          break;
        default:
          name = '';
      }
      return name;
    }
  },
  methods: {
    ...mapActions('jobForm', [
      'queryTestTask',
      'saveTaskAndJiraIssues',
      'updateJiraIssues',
      'putJiraIssues'
    ]),
    ...mapActions('user', ['fetch']),
    ...mapActions('dashboard', ['queryIssueDetailById']),
    ...mapMutations('userActionSaveHomePage/myDefect', [
      'saveUATSearchValueApp',
      'saveUATVisibleColumns',
      'saveCurrentPage',
      'saveUATShowDownFile'
    ]),
    validationError(num) {
      let ref = this.$refs[`qform${num}`];
      let arr = ref.getValidationComponents();
      arr.forEach(item => {
        item.validate();
      });
    },
    async changeInfo() {
      await this.putJiraIssues(this.defect);
      this.announcement = false;
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    filterType(type) {
      let name = '';
      switch (type) {
        case 'unitNo':
          name = '实施单元编号';
          break;
        case 'developer_cn':
          name = '开发人员';
          break;
        case 'defectCause':
          name = '开发原因';
          break;
        case 'priority':
          name = '优先级';
          break;
        case 'defectSource':
          name = '缺陷来源';
          break;
        case 'defectType':
          name = '缺陷类型';
          break;
        case 'belongStage':
          name = '归属阶段';
          break;
        default:
          name = '';
      }
      return name;
    },
    selectOption(val) {
      let option = [];
      switch (val) {
        case 'priority':
          option = this.UATpriorityOptions;
          break;
        case 'defectSource':
          option = this.flawSourceLists;
          break;
        case 'orderSeverity':
          option = this.severityOption;
          break;
        case 'defectType':
          option = this.defectOptions;
          break;
        case 'belongStage':
          option = this.stageOption;
          break;
        default:
          option = [];
      }
      return option;
    },
    //关闭缺陷详情弹框并刷新缺陷状态
    closeAnnouncement() {
      // this.$emit('updateDate');
      this.announcement = false;
    },
    //修改弹框
    flexDate(type) {
      this.flexDialog = true;
      this.flexType = type;
      this.chooseType = this.defect[type];
    },
    flexSelect(type) {
      this.flexSelectDialog = true;
      this.flexType1 = type;
      this.chooseType = this.defect[type];
    },
    // 点击搜索按钮,进行模糊搜索
    setSelect() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    async changeStatus(defect, item) {
      let status = defect.status;
      if (item === '已修复') {
        this.handleDevManagerOptions();
        this.defectItem = JSON.parse(JSON.stringify(defect));
        this.defectDialogOpened = true;
        this.listModel = listModel();
        if (status == '1') {
          //打开状态下的已修复
          this.updateStatu = '11';
        } else if (status == '10310') {
          //延迟修复状态下的已修复
          this.updateStatu = '131';
        } else if (status == '10307') {
          //重新打开状态下的已修复
          this.updateStatu = '121';
        }
        if (defect.assignee) {
          this.listModel.devManager = {
            user_name_en: defect.assigneeEn,
            user_name_cn: defect.assignee
          };
        }
        this.developer = {
          user_name_en: defect.developEn,
          user_name_cn: defect.develop
        };
      } else if (item === '拒绝') {
        if (status == '1') {
          //打开状态下的拒绝
          this.updateStatu = '141';
        } else if (status == '10013') {
          //新建状态下的拒绝
          this.updateStatu = '31';
        }
        this.assignRow = defect;
        this.flawSource = defect.defectSource;
        this.refuseDialogOpened = true;
      } else if (item === '重新打开') {
        if (status == '10015') {
          //已修复状态下的重新打开
          this.updateStatu = '101';
        } else if (status == '10737') {
          //遗留状态下的重新打开
          this.updateStatu = '201';
        }
        this.assignRow = defect;
        this.openAgain();
      } else if (item === '遗留') {
        if (status == '1') {
          //打开状态下的遗留
          this.updateStatu = '161';
        } else if (status == '10307') {
          //重新打开状态下的遗留
          this.updateStatu = '191';
        }
        this.assignRow = defect;
        this.legacy();
      } else if (item === '延迟修复') {
        //只有打开状态下才有延迟修复
        this.updateStatu = '51';
        this.assignRow = defect;
        this.delayDialogOpened = true;
      } else if (item === '分配') {
        this.assignRow = defect;
        this.confirmModalOpen = true;
        this.updateStatu = '';
      } else if (item === '打开') {
        //只有新建状态下才有打开
        this.updateStatu = '91';
        this.openDefectDialogOpened = true;
        this.taskModel.taskNo = '';
        if (defect.task_id != '' || defect.task_id != null) {
          this.taskModel.taskNo = defect.task_id;
        }
        this.showTask = defect.task_id;
        this.handleDevManagerOptions();
        this.taskModel = taskModel();
        if (defect.assignee) {
          this.taskModel.devManager = {
            user_name_en: this.currentUser.user_name_en,
            id: this.currentUser.id,
            user_name_cn: this.currentUser.user_name_cn
          };
        }
        this.developer = {
          user_name_en: defect.developEn,
          user_name_cn: defect.develop
        };
        this.defectItem = JSON.parse(JSON.stringify({ ...defect }));
      } else {
        return this.$q
          .dialog({
            title: '缺陷状态更改',
            message: '确定更改此缺陷的状态吗？',
            ok: '确定',
            cancel: '取消'
          })
          .onOk(async () => {
            defect.status = this.UATstatusName[item];
            this.$emit('update-status', defect);
          });
      }
    },
    defectFilter(rows, terms, cols, cellValue) {
      const lowerTerms = this.UATtermsApp.searchValue.map(item =>
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
      let order = ['最高', '高', '中', '低', '最低'];
      return order.indexOf(a) - order.indexOf(b);
    },
    sortStatus(a, b) {
      let order = [
        '新建',
        '打开',
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
      return (
        handler_en_name.toLowerCase() ===
        this.currentUser.user_name_en.toLowerCase()
      );
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
    async assignSlrUserName() {
      if (!this.slrUser) return;
      this.defect = {
        ...this.defect,
        assigneeEn: this.slrUser.user_name_en,
        assignee: this.slrUser.user_name_cn
      };
      this.slrUser = null;
      this.confirmSlrModalOpen = false;
    },
    async assignUserName() {
      if (!this.distriUser) return;
      await this.updateStatus({
        ...this.assignRow,
        assigneeEn: this.distriUser.user_name_en,
        assignee: this.distriUser.user_name_cn,
        status: this.updateStatus
      });
      this.distriUser = null;
      this.confirmModalOpen = false;
    },
    devManagerFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.devManagerOptions = this.userList
          .map(user => {
            return {
              user_name_en: user.user_name_en,
              id: user.id,
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
    taskChange(val) {},
    async handleOpenDefectDialogOpened() {
      let taskNo = '';
      if (this.defectItem.task_id != '' || this.defectItem.task_id != null) {
        taskNo = this.defectItem.task_id;
      }
      if (this.defectItem.task_id == '' || this.defectItem.task_id == null) {
        taskNo = this.taskModel.taskNo;
      }
      this.defectItem = {
        ...this.defectItem,
        task_id: taskNo,
        assigneeEn: this.taskModel.devManager
          ? this.taskModel.devManager.user_name_en
          : '',
        assignee: this.taskModel.devManager
          ? this.taskModel.devManager.user_name_cn
          : '',
        developEn: this.developer.user_name_en,
        develop: this.developer.user_name_cn,
        status: this.updateStatu
      };
      let taskId = this.defectItem.task_id;
      await this.saveTaskAndJiraIssues({
        issueKey: this.defectItem.issueKey,
        issueId: this.defectItem.id,
        taskId,
        taskName: this.UATtaskAllData.find(ele => ele.id == taskId).name,
        rqrmnt_no: this.UATtaskAllData.find(ele => ele.id == taskId).rqrmnt_no
      });
      await this.updateStatus({
        ...this.defectItem,
        status: this.updateStatu
      });
      this.openDefectDialogOpened = false;
    },
    defectHide() {},
    async handleDefectDialogOpened() {
      await this.updateStatus({
        ...this.defectItem,
        assigneeEn: this.listModel.devManager.user_name_en,
        assignee: this.listModel.devManager.user_name_cn,
        developEn: this.developer.user_name_en,
        develop: this.developer.user_name_cn,
        status: this.updateStatu,
        defectCause: this.listModel.reason
      });
      this.defectDialogOpened = false;
    },
    // 拒绝提示
    async handleRefuseDialogSubmit() {
      if (!this.flawSource) {
        successNotify('请选择缺陷来源!');
        return;
      }
      if (!this.listModel.reason) {
        successNotify('请填写开发原因分析!');
        return;
      }
      await this.updateStatus({
        ...this.assignRow,
        defectSource: this.flawSource,
        status: this.updateStatu,
        defectCause: this.listModel.reason
      });
      this.refuseDialogOpened = false;
    },
    //延迟修复
    async handleDelayDialogSubmit() {
      await this.updateStatus({
        ...this.assignRow,
        status: this.updateStatu,
        defectCause: this.delayReason
      });
      this.delayDialogOpened = false;
    },
    //重新打开
    async openAgain() {
      await this.updateStatus({
        ...this.assignRow,
        status: this.updateStatu
      });
    },
    //遗留修复
    async legacy() {
      await this.updateStatus({
        ...this.assignRow,
        status: this.updateStatu
      });
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
      this.assignRow = row;
      this.defect = row;
      this.announcement = true;
    },
    preview(file) {
      this.filePreviewDialog = true;
      if (file.file_type.includes('image')) {
        let type = file.file_type.includes('image/jpeg') ? 'jpeg' : 'png';
        this.filePreview = `<image style='width:560px;' src='data:image/${type};base64,${
          file.content
        }'/>`;
      } else {
        this.filePreview = file.content.replace(/\n/g, '</br>');
      }
    },
    toggleShow() {
      this.$emit('UATchangeShow', this.UATtermsApp.showDownFile);
    },
    async updateStatus(defect) {
      await this.updateJiraIssues(defect);
      this.$emit('update-status', defect);
    },
    async updateType() {
      this.$emit('update-status', this.defect);
      this.flawTypeDialog = false;
    },
    async updateType1() {
      if (this.flexType == 'summary' && this.chooseType == '') return;
      let obj = {
        ...this.defect,
        [this.flexType]: this.chooseType
      };
      this.defect = obj;
      this.flexDialog = false;
      this.flexType = '';
    },
    async updateSource() {
      if (
        (this.flexType1 == 'orderSeverity' && this.chooseType == '-1') ||
        (this.flexType1 == 'defectType' && this.chooseType == '-1')
      ) {
        return;
      }
      let obj = {
        ...this.defect,
        [this.flexType1]: this.chooseType
      };
      this.defect = obj;
      this.flexSelectDialog = false;
    },
    async handleDevManagerOptions() {
      if (this.userList.length === 0) {
        await this.fetch();
      }
      this.devManagerOptions = this.userList.map(user => {
        return {
          user_name_en: user.user_name_en,
          id: user.id,
          user_name_cn: user.user_name_cn
        };
      });
    }
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.uatVisibleCols.toString() || this.uatVisibleCols.length <= 2) {
      this.saveUATVisibleColumns([
        'taskName',
        'summary',
        'description',
        'priority',
        'status',
        'assignee',
        'created',
        'creator',
        'operation'
      ]);
    }
  }
};
</script>
<style lang="stylus" scoped>
.form-width {
  margin: 0 15px;
}

.width-select {
  min-width: 150px;
}

.col-width {
  width: 50%;
  display: inline-block;
}

.dialog-width {
  max-width: 800px;
}

.dialog-height {
  min-height: 476px;
  height: 0;
}

.link {
  color: #2196f3;
}

.hover {
  cursor: auto;
}

.description {
  color: #6b778c;
  font-size: 12px;
  line-height: 1.66666666666667;
  margin: 5px 0 0 0;
}

/deep/.require .term {
  position: relative;
}

/deep/.require .term::before {
  content: '*';
  position: absolute;
  right: 10px;
  top: 0;
  text-indent: initial;
  color: #de350b;
  line-height: 1;
  font-size: 12px;
}

.redMsg {
  padding: 10px 20px;
  color: red;
  font-size: 12px;
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

.margin-top-10 {
  margin-top: 10px;
}

.transform-rotate {
  transform: rotate(45deg);
  margin-left: 10px;
}
</style>
