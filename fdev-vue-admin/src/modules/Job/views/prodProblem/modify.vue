<template>
  <div>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md">
        <fdev-form @submit.prevent="handelAdd">
          <div class="padding-card">
            <span class="q-ml-lg font-type"> 需求基本信息</span>
            <fdev-card-section class="two-col">
              <f-formitem label="fdev任务编号">
                <fdev-input
                  ref="proProblemModel.task_no"
                  type="text"
                  class="input q-mb-md"
                  v-model="proProblemModel.task_no"
                  readonly
                >
                </fdev-input>
              </f-formitem>
              <f-formitem label="需求名称">
                <fdev-input
                  ref="proProblemModel.requirement_name"
                  type="text"
                  class="input"
                  clearable
                  v-model="$v.proProblemModel.requirement_name.$model"
                  :rules="[
                    () =>
                      $v.proProblemModel.requirement_name.required ||
                      '请输入需求名称'
                  ]"
                >
                </fdev-input>
              </f-formitem>
              <f-formitem label="所属小组">
                <fdev-select
                  ref="proProblemModel.module"
                  type="text"
                  class="input"
                  use-input
                  v-model="$v.proProblemModel.module.$model"
                  :options="filterGroup"
                  @filter="groupInputFilter"
                  :rules="[
                    () => $v.proProblemModel.module.required || '请选择所属小组'
                  ]"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.name">{{
                          scope.opt.name
                        }}</fdev-item-label>
                        <fdev-item-label :title="scope.opt.fullName" caption>
                          {{ scope.opt.fullName }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="所属窗口">
                <fdev-input
                  ref="proProblemModel.release_node"
                  type="text"
                  class="input"
                  clearable
                  v-model="proProblemModel.release_node"
                >
                </fdev-input>
              </f-formitem>
              <f-formitem label="是否涉及紧急需求">
                <fdev-select
                  ref="proProblemModel.is_involve_urgency"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.is_involve_urgency.$model"
                  :options="urgencyOptins"
                  :rules="[
                    () =>
                      $v.proProblemModel.is_involve_urgency.required ||
                      '请选择是否涉及紧急需求'
                  ]"
                >
                </fdev-select>
              </f-formitem>
              <div class="q-mt-lg q-mb-md font-type">问题基本信息</div>
              <f-formitem label="发生日期">
                <f-date
                  mask="YYYY/MM/DD"
                  ref="proProblemModel.occurred_time"
                  :rules="[
                    () =>
                      $v.proProblemModel.occurred_time.required ||
                      '请输入发生日期'
                  ]"
                  v-model="$v.proProblemModel.occurred_time.$model"
                />
              </f-formitem>
              <f-formitem label="问题首次出现时间">
                <fdev-select
                  ref="proProblemModel.first_occurred_time"
                  type="text"
                  v-model="$v.proProblemModel.first_occurred_time.$model"
                  clearable
                  :rules="[
                    () =>
                      $v.proProblemModel.first_occurred_time.required ||
                      '请输入问题首次出现时间'
                  ]"
                  mask="date"
                  :hide-dropdown-icon="true"
                >
                  <template v-slot:append>
                    <f-icon
                      name="calendar"
                      class="cursor-pointer fdev-date-first_occurred_time"
                    />
                    <fdev-popup-proxy
                      target=".fdev-date-first_occurred_time"
                      ref="proProblemModel_first_occurred_time"
                      transition-show="scale"
                      transition-hide="scale"
                    >
                      <div class="q-gutter-lg row items-start">
                        <fdev-date
                          v-model="
                            $v.proProblemModel.first_occurred_time.$model
                          "
                          mask="YYYY-MM-DD HH:mm:ss"
                        />
                        <fdev-time
                          v-model="
                            $v.proProblemModel.first_occurred_time.$model
                          "
                          mask="YYYY-MM-DD HH:mm:ss"
                          @input="
                            () =>
                              $refs.proProblemModel_first_occurred_time.hide()
                          "
                          with-seconds
                        />
                      </div>
                    </fdev-popup-proxy>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="定位时间">
                <fdev-select
                  ref="proProblemModel.location_time"
                  type="text"
                  v-model="$v.proProblemModel.location_time.$model"
                  clearable
                  :rules="[
                    () =>
                      $v.proProblemModel.location_time.required ||
                      '请输入定位时间'
                  ]"
                  mask="date"
                  :hide-dropdown-icon="true"
                >
                  <template v-slot:append>
                    <f-icon
                      name="calendar"
                      class="cursor-pointer fdev-date-location_time"
                    />
                    <fdev-popup-proxy
                      target=".fdev-date-location_time"
                      ref="proProblemModel_location_time"
                      transition-show="scale"
                      transition-hide="scale"
                    >
                      <div class="q-gutter-lg row items-start">
                        <fdev-date
                          v-model="$v.proProblemModel.location_time.$model"
                          mask="YYYY-MM-DD HH:mm:ss"
                        />
                        <fdev-time
                          v-model="$v.proProblemModel.location_time.$model"
                          mask="YYYY-MM-DD HH:mm:ss"
                          @input="
                            () => $refs.proProblemModel_location_time.hide()
                          "
                          with-seconds
                        />
                      </div>
                    </fdev-popup-proxy>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="修复时间">
                <fdev-select
                  ref="proProblemModel.repair_time"
                  type="text"
                  v-model="$v.proProblemModel.repair_time.$model"
                  clearable
                  :rules="[
                    () =>
                      $v.proProblemModel.repair_time.required ||
                      '请输入修复时间'
                  ]"
                  mask="date"
                  :hide-dropdown-icon="true"
                >
                  <template v-slot:append>
                    <f-icon
                      name="calendar"
                      class="cursor-pointer fdev-date-repair_time"
                    />
                    <fdev-popup-proxy
                      target=".fdev-date-repair_time"
                      ref="proProblemModel_repair_time"
                      transition-show="scale"
                      transition-hide="scale"
                    >
                      <div class="q-gutter-lg row items-start">
                        <fdev-date
                          v-model="$v.proProblemModel.repair_time.$model"
                          mask="YYYY-MM-DD HH:mm:ss"
                        />
                        <fdev-time
                          v-model="$v.proProblemModel.repair_time.$model"
                          mask="YYYY-MM-DD HH:mm:ss"
                          with-seconds
                          @input="
                            () => $refs.proProblemModel_repair_time.hide()
                          "
                        />
                      </div>
                    </fdev-popup-proxy>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="发现阶段">
                <fdev-select
                  ref="proProblemModel.discover_stage"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.discover_stage.$model"
                  :options="discover_stage"
                  :rules="[
                    () =>
                      $v.proProblemModel.discover_stage.required ||
                      '请选择发现阶段'
                  ]"
                >
                </fdev-select>
              </f-formitem>
              <f-formitem label="问题类型">
                <fdev-select
                  multiple
                  use-input
                  ref="proProblemModel.issue_type"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.issue_type.$model"
                  :options="typeOptions"
                  @filter="issueTypeFilter"
                  :rules="[
                    () =>
                      $v.proProblemModel.issue_type.required || '请选择问题类型'
                  ]"
                >
                </fdev-select>
              </f-formitem>
              <f-formitem label="生产问题级别">
                <fdev-select
                  ref="proProblemModel.issue_level"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.issue_level.$model"
                  :options="levelOptions"
                  :rules="[
                    () =>
                      $v.proProblemModel.issue_level.required ||
                      '请选择生产问题级别'
                  ]"
                >
                </fdev-select>
              </f-formitem>
              <f-formitem
                help="生产问题造成的生产环境故障现象"
                label="问题现象"
              >
                <fdev-input
                  ref="proProblemModel.problem_phenomenon"
                  type="textarea"
                  class="input"
                  v-model="$v.proProblemModel.problem_phenomenon.$model"
                  :rules="[
                    () =>
                      $v.proProblemModel.problem_phenomenon.required ||
                      '请填写问题现象'
                  ]"
                >
                </fdev-input>
              </f-formitem>
              <f-formitem label="问题原因">
                <fdev-input
                  ref="proProblemModel.issue_reason"
                  type="textarea"
                  class="input"
                  v-model="$v.proProblemModel.issue_reason.$model"
                  :rules="[
                    () =>
                      $v.proProblemModel.issue_reason.required ||
                      '请填写问题原因'
                  ]"
                >
                </fdev-input>
              </f-formitem>
              <f-formitem
                help="责任人对生产问题采取的解决方案及之后为规避类似问题需要注意的要点等"
                label="改进措施"
              >
                <fdev-input
                  ref="proProblemModel.improvement_measures	"
                  type="textarea"
                  class="input"
                  v-model="$v.proProblemModel.improvement_measures.$model"
                  :rules="[
                    () =>
                      $v.proProblemModel.improvement_measures.required ||
                      '请填写改进措施'
                  ]"
                >
                </fdev-input>
              </f-formitem>
              <f-formitem
                help="生产问题造成的关联影响及具体牵涉范围"
                label="影响范围"
              >
                <fdev-input
                  ref="proProblemModel.influence_area"
                  type="textarea"
                  class="input"
                  v-model="$v.proProblemModel.influence_area.$model"
                  :rules="[
                    () =>
                      $v.proProblemModel.influence_area.required ||
                      '请填写影响范围'
                  ]"
                >
                </fdev-input>
              </f-formitem>

              <f-formitem label="开发责任人">
                <fdev-select
                  use-input
                  multiple
                  ref="proProblemModel.dev_responsible"
                  class="input"
                  v-model="proProblemModel.dev_responsible"
                  :options="userOptionsFilter"
                  @filter="userFilter"
                  option-label="user_name_cn"
                  option-value="user_name_en"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.user_name_cn">
                          {{ scope.opt.user_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          :title="scope.opt.user_name_en"
                          caption
                        >
                          {{ scope.opt.user_name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="代码审核责任人">
                <fdev-select
                  use-input
                  multiple
                  ref="proProblemModel.audit_responsible"
                  type="text"
                  class="input"
                  v-model="proProblemModel.audit_responsible"
                  :options="userOptionsFilter"
                  @filter="userFilter"
                  option-label="user_name_cn"
                  option-value="user_name_en"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.user_name_cn">
                          {{ scope.opt.user_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          :title="scope.opt.user_name_en"
                          caption
                        >
                          {{ scope.opt.user_name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="内测责任人">
                <fdev-select
                  use-input
                  multiple
                  ref="proProblemModel.test_responsible"
                  type="text"
                  class="input"
                  v-model="proProblemModel.test_responsible"
                  :options="userOptionsFilter"
                  @filter="userFilter"
                  option-label="user_name_cn"
                  option-value="user_name_en"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.user_name_cn">
                          {{ scope.opt.user_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          :title="scope.opt.user_name_en"
                          caption
                        >
                          {{ scope.opt.user_name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="牵头任务责任人">
                <fdev-select
                  use-input
                  multiple
                  ref="proProblemModel.task_responsible"
                  type="text"
                  class="input"
                  v-model="proProblemModel.task_responsible"
                  :options="userOptionsFilter"
                  @filter="userFilter"
                  option-label="user_name_cn"
                  option-value="user_name_en"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.user_name_cn">
                          {{ scope.opt.user_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          :title="scope.opt.user_name_en"
                          caption
                        >
                          {{ scope.opt.user_name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem
                help="是否造成生产环境问题及对生产环境的影响"
                label="是否产生生产问题"
              >
                <fdev-select
                  ref="proProblemModel.is_trigger_issue"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.is_trigger_issue.$model"
                  :options="triggerOptions"
                  :rules="[
                    () =>
                      $v.proProblemModel.is_trigger_issue.required ||
                      '请选择是否产生生产问题'
                  ]"
                >
                </fdev-select>
              </f-formitem>

              <f-formitem label="能否UAT复现">
                <fdev-select
                  ref="proProblemModel.is_uat_replication"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.is_uat_replication.$model"
                  :options="uatOptions"
                  :rules="[
                    () =>
                      $v.proProblemModel.is_uat_replication.required ||
                      '请选择能否UAT复现'
                  ]"
                >
                </fdev-select>
              </f-formitem>
              <f-formitem label="能否REL复现">
                <fdev-select
                  ref="proProblemModel.is_rel_replication"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.is_rel_replication.$model"
                  :options="relOptions"
                  :rules="[
                    () =>
                      $v.proProblemModel.is_rel_replication.required ||
                      '请选择能否REL复现'
                  ]"
                >
                </fdev-select>
              </f-formitem>
              <f-formitem label="能否灰度复现">
                <fdev-select
                  ref="proProblemModel.is_gray_replication"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.is_gray_replication.$model"
                  :options="grayOptions"
                  :rules="[
                    () =>
                      $v.proProblemModel.is_gray_replication.required ||
                      '请选择能否灰度复现'
                  ]"
                >
                </fdev-select>
              </f-formitem>

              <f-formitem label="处理状态">
                <fdev-select
                  ref="proProblemModel.deal_status"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.deal_status.$model"
                  :options="dealStatusOptions"
                  :rules="[
                    () =>
                      $v.proProblemModel.deal_status.required ||
                      '请选择处理状态'
                  ]"
                >
                </fdev-select>
              </f-formitem>
              <div class="q-mt-lg q-mb-md font-type">评审信息</div>
              <f-formitem label="评审时间">
                <fdev-select
                  ref="proProblemModel.reviewer_time"
                  type="text"
                  v-model="$v.proProblemModel.reviewer_time.$model"
                  clearable
                  :rules="[
                    () =>
                      $v.proProblemModel.reviewer_time.required ||
                      '请输入评审时间'
                  ]"
                  mask="date"
                  :hide-dropdown-icon="true"
                >
                  <template v-slot:append>
                    <f-icon
                      name="calendar"
                      class="cursor-pointer fdev-date-reviewer_time"
                    />
                    <fdev-popup-proxy
                      target=".fdev-date-reviewer_time"
                      ref="proProblemModel_reviewer_time"
                      transition-show="scale"
                      transition-hide="scale"
                    >
                      <div class="q-gutter-lg row items-start">
                        <fdev-date
                          v-model="$v.proProblemModel.reviewer_time.$model"
                          mask="YYYY-MM-DD HH:mm:ss"
                        />
                        <fdev-time
                          v-model="$v.proProblemModel.reviewer_time.$model"
                          mask="YYYY-MM-DD HH:mm:ss"
                          @input="
                            () => $refs.proProblemModel_reviewer_time.hide()
                          "
                          with-seconds
                        />
                      </div>
                    </fdev-popup-proxy>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="评审人">
                <fdev-select
                  use-input
                  multiple
                  ref="proProblemModel.reviewer"
                  class="input"
                  v-model="$v.proProblemModel.reviewer.$model"
                  :options="userOptionsFilter"
                  @filter="userFilter"
                  option-label="user_name_cn"
                  option-value="user_name_en"
                  :rules="[
                    () => $v.proProblemModel.reviewer.required || '请选择评审人'
                  ]"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.user_name_cn">
                          {{ scope.opt.user_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          :title="scope.opt.user_name_en"
                          caption
                        >
                          {{ scope.opt.user_name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem
                help="生产问题评审会议中由专家组给出的建议'"
                label="评审意见"
                optional
              >
                <fdev-input
                  ref="proProblemModel.reviewer_comment	"
                  type="textarea"
                  class="input"
                  v-model="proProblemModel.reviewer_comment"
                >
                </fdev-input>
              </f-formitem>

              <f-formitem label="评审状态">
                <fdev-select
                  ref="proProblemModel.reviewer_status"
                  type="text"
                  class="input"
                  v-model="$v.proProblemModel.reviewer_status.$model"
                  :options="reviewerOptions"
                  :rules="[
                    () =>
                      $v.proProblemModel.reviewer_status.required ||
                      '请选择评审状态'
                  ]"
                >
                </fdev-select>
              </f-formitem>

              <div class="column row">
                <div
                  v-for="(item, index) in proProblemModel.responsibility_list"
                  :key="index"
                >
                  <div class="row justify-center btn-margin">
                    <fdev-btn
                      color="primary"
                      label="添加问责相关内容"
                      flat
                      class="tip-msg"
                      v-if="proProblemModel.responsibility_list.length < 3"
                      @click="add"
                    />
                    <fdev-btn
                      color="primary"
                      label="问责相关内容最多可填三组"
                      flat
                      class="tip-msg"
                      v-else
                      disable
                    />
                    <fdev-btn
                      class="tip-msg"
                      color="red"
                      label="删除"
                      flat
                      :disable="
                        proProblemModel.responsibility_list.length === 1
                      "
                      @click="handelDelete(index)"
                    />
                  </div>
                  <f-formitem
                    help="如涉及问责，填写被问责人的姓名"
                    label="问责人"
                    optional
                  >
                    <fdev-select
                      use-input
                      :ref="`proProblemModel.${index}.responsible`"
                      type="text"
                      class="input"
                      clearable
                      v-model="item.responsible"
                      :options="userOptionsFilter"
                      @filter="userFilter"
                      option-label="user_name_cn"
                      option-value="user_name_en"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label :title="scope.opt.user_name_cn">
                              {{ scope.opt.user_name_cn }}
                            </fdev-item-label>
                            <fdev-item-label
                              :title="scope.opt.user_name_en"
                              caption
                            >
                              {{ scope.opt.user_name_en }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </f-formitem>
                  <f-formitem label="问责类型">
                    <fdev-select
                      use-input
                      ref="`proProblemModel.${index}.responsibility_type`"
                      type="text"
                      class="input"
                      clearable
                      v-model="item.responsibility_type"
                      :options="responsibilityTypeOptions"
                    >
                    </fdev-select>
                  </f-formitem>
                  <f-formitem label="问责内容">
                    <fdev-input
                      ref="`proProblemModel.${index}.responsibility_content`"
                      type="textarea"
                      class="input"
                      v-model="item.responsibility_content"
                    >
                    </fdev-input>
                  </f-formitem>
                </div>
              </div>
              <div class="column row">
                <div
                  v-for="(item, index) in $v.proProblemModel
                    .backlog_schedule_list.$each.$iter"
                  :key="index"
                >
                  <div class="row justify-center btn-margin">
                    <fdev-btn
                      color="primary"
                      label="添加待办事项相关内容"
                      flat
                      class="tip-msg"
                      v-if="proProblemModel.backlog_schedule_list.length < 3"
                      @click="addSchedule"
                    />
                    <fdev-btn
                      v-else
                      color="primary"
                      label="待办事项相关内容最多可填三组"
                      flat
                      class="tip-msg"
                      disable
                    />
                    <fdev-btn
                      color="red"
                      label="删除"
                      flat
                      class="tip-msg"
                      :disable="
                        proProblemModel.backlog_schedule_list.length === 1
                      "
                      @click="deleteSchedule(index)"
                    />
                  </div>
                  <f-formitem label="待办事项">
                    <fdev-input
                      :ref="`proProblemModel.${index}.backlog_schedule`"
                      type="textarea"
                      class="input"
                      v-model="item.backlog_schedule.$model"
                    >
                    </fdev-input>
                  </f-formitem>
                  <f-formitem label="待办事项负责人">
                    <fdev-select
                      use-input
                      :ref="
                        `proProblemModel.${index}.backlog_schedule_reviewer`
                      "
                      type="text"
                      class="input"
                      clearable
                      v-model="item.backlog_schedule_reviewer.$model"
                      :options="userOptionsFilter"
                      @filter="userFilter"
                      option-label="user_name_cn"
                      option-value="user_name_en"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label :title="scope.opt.user_name_cn">
                              {{ scope.opt.user_name_cn }}
                            </fdev-item-label>
                            <fdev-item-label
                              :title="scope.opt.user_name_en"
                              caption
                            >
                              {{ scope.opt.user_name_en }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </f-formitem>

                  <f-formitem label="待办事项当前完成情况">
                    <fdev-select
                      use-input
                      :ref="
                        `proProblemModel.${index}.backlog_schedule_current_completion`
                      "
                      type="text"
                      class="input"
                      clearable
                      v-model="item.backlog_schedule_current_completion.$model"
                      :options="completionOptions"
                    >
                    </fdev-select>
                  </f-formitem>
                  <f-formitem label="待办事项完成时间">
                    <f-date
                      mask="YYYY/MM/DD"
                      :ref="
                        `proProblemModel.${index}.backlog_schedule_complete_time`
                      "
                      v-model="item.backlog_schedule_complete_time.$model"
                    />
                  </f-formitem>
                  <f-formitem label="待办事项完成百分比">
                    <fdev-input
                      :ref="
                        `proProblemModel.${index}.backlog_schedule_complete_percentage`
                      "
                      type="text"
                      class="input"
                      clearable
                      v-model="item.backlog_schedule_complete_percentage.$model"
                      :rules="[
                        () =>
                          item.backlog_schedule_complete_percentage
                            .numberScoped || '请输入0-100的数字'
                      ]"
                    >
                    </fdev-input>
                  </f-formitem>
                </div>
              </div>
              <div class="font-type">应急信息</div>
              <f-formitem label="应急过程">
                <fdev-input
                  ref="proProblemModel.emergency_process"
                  type="textarea"
                  class="input"
                  v-model="$v.proProblemModel.emergency_process.$model"
                  :rules="[
                    () =>
                      $v.proProblemModel.emergency_process.required ||
                      '请填写应急过程'
                  ]"
                >
                </fdev-input>
              </f-formitem>
              <f-formitem label="应急负责人">
                <fdev-select
                  use-input
                  multiple
                  ref="proProblemModel.emergency_responsible"
                  class="input"
                  v-model="$v.proProblemModel.emergency_responsible.$model"
                  :options="userOptionsFilter"
                  @filter="userFilter"
                  option-label="user_name_cn"
                  option-value="user_name_en"
                  :rules="[
                    () =>
                      $v.proProblemModel.emergency_responsible.required ||
                      '请选择应急负责人'
                  ]"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.user_name_cn">
                          {{ scope.opt.user_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          :title="scope.opt.user_name_en"
                          caption
                        >
                          {{ scope.opt.user_name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <div class="font-type">其他</div>
              <f-formitem label="备注">
                <fdev-input
                  ref="proProblemModel.remark"
                  type="textarea"
                  class="input"
                  v-model="proProblemModel.remark"
                >
                </fdev-input>
              </f-formitem>
              <div class="column row">
                <f-formitem label="附件列表" class="file-field">
                  <ul>
                    <li
                      v-for="(item, index) in issueFilesList"
                      :key="index"
                      class="file-list"
                    >
                      <fdev-btn
                        label="删除"
                        color="red"
                        flat
                        @click="deleteFiles(item.file_id, index)"
                      >
                      </fdev-btn>
                      <fdev-btn
                        label="下载"
                        color="primary"
                        flat
                        @click="downloadFiles(item.file_id)"
                      ></fdev-btn>
                      {{ item.name }}
                    </li>
                  </ul>
                </f-formitem>
                <f-formitem label="上传附件" class="file-field">
                  <input type="file" class="file q-mt-xs" value="选择附件" />
                  <div>
                    <fdev-btn
                      ficon="upload"
                      :loading="fileLoading"
                      dialog
                      normal
                      label="上传"
                      @click="uploadFile()"
                    />
                  </div>
                </f-formitem>
              </div>
            </fdev-card-section>
          </div>
          <div class="row justify-center btn-margin">
            <fdev-btn
              type="submit"
              dialog
              label="提交"
              :loading="globalLoading['jobForm/updateProIssue']"
              style="margin-right:20px"
            />
            <fdev-btn label="返回" outline dialog @click="goBack" />
          </div>
        </fdev-form>
      </fdev-card>
    </Loading>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import {
  createProProblemModel,
  issueTypeList,
  urgencyOptins
} from '../../utils/constants';
import { required } from 'vuelidate/lib/validators';
import {
  validate,
  formatOption,
  deepClone,
  exportExcel,
  errorNotify,
  successNotify
} from '@/utils/utils';

export default {
  name: 'ModifyProductionProblem',
  components: {
    Loading
  },
  data() {
    return {
      loading: false,
      fileLoading: false,
      proProblemModel: createProProblemModel(),
      filterGroup: [],
      deepCloneGroups: [],
      triggerOptions: ['是', '否'],
      typeOptions: issueTypeList,
      discover_stage: ['灰度', '灰度重发', '生产', '生产重发'],
      users: [],
      userOptions: [],
      userOptionsFilter: [],
      uatOptions: ['是', '否', '不涉及'],
      relOptions: ['是', '否', '不涉及'],
      grayOptions: ['是', '否', '不涉及'],
      urgencyOptins: urgencyOptins,
      dealStatusOptions: ['未修复', '修复中', '修复完成'],
      levelOptions: ['流程规范性错误', '一般错误', '复杂错误'],
      reviewerOptions: ['未评审', '已评审'],
      responsibilityTypeOptions: ['通报', '罚款', '降职', '离场', '其他'],
      completionOptions: ['进行中', '已办结'],
      issueFilesList: []
    };
  },
  validations: {
    proProblemModel: {
      requirement_name: {
        required
      },
      module: {
        required
      },
      reviewer_time: {
        required
      },
      reviewer: {
        required
      },
      location_time: {
        required
      },
      first_occurred_time: {
        required
      },
      repair_time: {
        required
      },
      emergency_process: {
        required
      },
      emergency_responsible: {
        required
      },
      occurred_time: {
        required
      },
      is_trigger_issue: {
        required
      },
      issue_type: {
        required
      },
      problem_phenomenon: {
        required
      },
      influence_area: {
        required
      },
      issue_reason: {
        required
      },
      discover_stage: {
        required
      },
      is_uat_replication: {
        required
      },
      is_rel_replication: {
        required
      },
      is_gray_replication: {
        required
      },
      is_involve_urgency: {
        required
      },
      deal_status: {
        required
      },
      issue_level: {
        required
      },
      reviewer_status: {
        required
      },
      improvement_measures: {
        required
      },
      backlog_schedule_list: {
        $each: {
          backlog_schedule_complete_percentage: {
            numberScoped(val) {
              if (!val) {
                return true;
              }
              let reg = new RegExp(/^(100|[0-9]\d|\d)$/);
              return reg.test(val);
            }
          },
          backlog_schedule: {},
          backlog_schedule_reviewer: {},
          backlog_schedule_complete_time: {},
          backlog_schedule_current_completion: {}
        }
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('dashboard', {
      usersList: 'userList'
    }),
    ...mapState('appForm', {
      proIssueList: 'proIssueDate'
    }),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('jobForm', [
      'proIssueDateById',
      'proIssueUpdate',
      'issueFilesDate',
      'fileDownloadDate'
    ])
  },
  methods: {
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapActions('jobForm', [
      'queryProIssueById',
      'updateProIssue',
      'queryIssueFiles',
      'fileDownload',
      'deleteIssueFile',
      'addFile'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    async uploadFile() {
      if (document.querySelector('input[type=file]').files[0]) {
        let fileSize =
          document.querySelector('input[type=file]').files[0].size /
          (1024 * 1024);
        if (fileSize > 2) {
          errorNotify('文件上传大小不能超过2M');
        } else {
          let file = document.querySelector('input[type=file]').files[0];
          this.choosedFiles = [];
          const res = await this.getBase64(file);
          this.choosedFiles.push({ name: file.name, content: res });
          this.fileLoading = true;
          try {
            await this.addFile({
              files: this.choosedFiles,
              id: this.$route.params.id
            });
          } finally {
            this.fileLoading = false;
          }
          await this.queryIssueFiles({ id: this.$route.params.id });
          this.issueFilesList = this.issueFilesDate;
          this.fileLoading = false;
        }
      }
    },
    add() {
      this.proProblemModel.responsibility_list.push({
        responsible: '',
        responsibility_type: '',
        responsibility_content: ''
      });
    },
    handelDelete(index) {
      this.proProblemModel.responsibility_list.splice(index, 1);
    },
    addSchedule() {
      this.proProblemModel.backlog_schedule_list.push({
        backlog_schedule: '',
        backlog_schedule_reviewer: '',
        backlog_schedule_complete_time: '',
        backlog_schedule_current_completion: '',
        backlog_schedule_complete_percentage: ''
      });
    },
    deleteSchedule(index) {
      this.proProblemModel.backlog_schedule_list.splice(index, 1);
    },
    async handelAdd() {
      this.$v.proProblemModel.$touch();
      let problensKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('proProblemModel') > -1
      );
      validate(problensKeys.map(key => this.$refs[key]));

      if (this.$v.proProblemModel.$invalid) {
        errorNotify('请补充所有必填字段');
        return;
      }
      await this.addProductionProblem();
    },

    async addProductionProblem() {
      let responsibilityList = deepClone(
        this.proProblemModel.responsibility_list
      );
      responsibilityList.map(responsibility => {
        if (responsibility.responsible) {
          responsibility.responsible = responsibility.responsible.user_name_en;
        }
        return responsibility;
      });
      let scheduleList = deepClone(this.proProblemModel.backlog_schedule_list);
      scheduleList.map(schedule => {
        if (schedule.backlog_schedule_reviewer) {
          schedule.backlog_schedule_reviewer =
            schedule.backlog_schedule_reviewer.user_name_en;
        }
        return schedule;
      });
      let addProblemModel = {
        ...this.proProblemModel,
        module: this.proProblemModel.module.name
          ? this.proProblemModel.module.name
          : this.proProblemModel.module,
        responsibility_list: responsibilityList,
        backlog_schedule_list: scheduleList,
        orfanizer: this.currentUser.user_name_en,
        files: []
      };
      if (
        !(
          this.proProblemModel.dev_responsible.length > 0 ||
          this.proProblemModel.audit_responsible.length > 0 ||
          this.proProblemModel.test_responsible.length > 0 ||
          this.proProblemModel.task_responsible.length > 0
        )
      ) {
        errorNotify(
          '[ 开发, 代码审核, 内测, 牵头任务 ]相关责任人,需至少填一项'
        );
      } else {
        await this.updateProIssue(addProblemModel);
        successNotify('修改成功');
        window.history.back();
      }
    },
    groupInputFilter(val, update) {
      update(() => {
        this.filterGroup = this.deepCloneGroups.filter(
          tag => tag.fullName.indexOf(val) > -1
        );
      });
    },
    issueTypeFilter(val, update) {
      update(() => {
        this.typeOptions = issueTypeList.filter(tag => tag.indexOf(val) > -1);
      });
    },
    async userFilter(val, update, abort) {
      update(() => {
        this.userOptionsFilter = this.usersList.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.includes(val.toLowerCase())
        );
      });
    },
    goBack() {
      window.history.back();
    },
    //附件相关操作
    beforeUpload(files) {
      this.getFileData = [];
      new Promise((resolve, reject) => {
        files.forEach(async (file, index) => {
          const res = await this.getBase64(file);
          this.getFileData.push({ name: file.name, content: res });
          if (index === files.length - 1) {
            resolve(this.getFileData);
          }
        });
      });
      setTimeout(() => {
        this.addFile({ files: this.getFileData, id: this.$route.params.id });
      }, 100);
    },
    getBase64(file) {
      //把上传内容转成base64编码
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
    async deleteFiles(id, index) {
      return this.$q
        .dialog({
          title: '删除附件',
          message: '请确认是否删除该文件',
          ok: '删除',
          cancel: '再想想'
        })
        .onOk(async () => {
          await this.deleteIssueFile({ id: id });
          this.issueFilesList.splice(index, 1);
        });
    },
    async downloadFiles(id) {
      await this.fileDownload({ id: id });
      exportExcel(this.fileDownloadDate);
    }
  },
  async created() {
    this.loading = !true;

    await this.queryProIssueById({ id: this.$route.params.id });
    this.proProblemModel = this.proIssueDateById;

    this.proProblemModel.dev_responsible =
      this.proIssueDateById.dev_responsible || [];
    this.proProblemModel.emergency_responsible =
      this.proIssueDateById.emergency_responsible || [];
    this.proProblemModel.reviewer = this.proIssueDateById.reviewer || [];
    this.proProblemModel.task_responsible =
      this.proIssueDateById.task_responsible || [];
    this.proProblemModel.test_responsible =
      this.proIssueDateById.test_responsible || [];
    this.proProblemModel.audit_responsible =
      this.proIssueDateById.audit_responsible || [];
    await this.queryIssueFiles({ id: this.$route.params.id });
    this.issueFilesList = this.issueFilesDate;

    this.loading = false;
    await this.queryUserCoreData();
    this.userOptionsFilter = this.usersList;
    await this.fetchGroup();
    this.groups = formatOption(this.groupsData);
    this.deepCloneGroups = deepClone(this.groups);
    this.filterGroup = this.groups;
  }
};
</script>

<style lang="stylus" scoped>

.padding-card
  padding 0 10px
.two-col
  display:flex;
  flex-wrap:wrap
  justify-content: space-between;
.q-pb-sm >>> .q-checkbox__inner
  margin 0 auto!important
.form .input-padding
  padding-bottom 20px
.q-card__section .row .col .q-field
  min-width 85%
.divAdd
  margin 0 auto
  color #027be3
.file-field
 margin-top 20px
.file-field >>> .col
  display -webkit-box!important
.file-list
 list-style-type none
 margin -8px 0 10px -40px
.q-fab >>> .q-btn--fab,
.q-fab >>> .q-btn__wrapper
  width 46px!important
  height 46px!important
  min-width 46px!important
  min-height 46px!important
  max-width 46px!important
  max-height 46px!important
  overflow hide
  padding 0px!important
  margin-right auto!important
.tip-msg
  margin-left 30px
.select-tip
 position absolute!important
.pre-style
  display block
  line-height 1.8em
.select-tip
 position absolute
.input-display
 position relative
.font-type
 font-size 16px
 width:100%
.q-date__calendar-item
  width 3.5625vw;
  height 1.5625vw;
  border-radius 50%;
>>> .items-start label
  padding-bottom:20px
</style>
