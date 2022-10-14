<template>
  <fdev-dialog
    :value="value"
    transition-show="slide-up"
    transition-hide="slide-down"
    class="dialog"
    @input="$emit('input', $event)"
    @show="initQuery()"
    persistent
    @shake="confirmToClose"
  >
    <fdev-layout view="Lhh lpR fff" container class="bg-white dialog-width">
      <fdev-header bordered class="bg-primary">
        <fdev-toolbar>
          <fdev-toolbar-title>
            {{ demand_type === 'tech' ? '科技需求修改' : '业务需求修改' }}
          </fdev-toolbar-title>
          <fdev-btn flat v-close-popup round dense icon="close" />
        </fdev-toolbar>
      </fdev-header>
      <fdev-page-container>
        <fdev-page padding>
          <div class="q-pa-md items-start q-gutter-md q-px-lg">
            <fdev-form @submit.prevent="handleOptimize">
              <fdev-card class="card">
                <fdev-card-section class="q-pt-none">
                  <div class="title">基础数据信息</div>
                </fdev-card-section>
                <fdev-card-section v-if="demand_type !== 'tech'">
                  <fdev-input
                    label="OA联系单编号"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.oa_contact_no"
                  />
                  <fdev-input
                    label="OA联系单名称"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.oa_contact_name"
                  />
                  <Field label="OA收件日期" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      outlined
                      clearable
                      readonly
                      v-model="updateDialogModel.oa_receive_date"
                      type="text"
                      dense
                      mask="YYYY-MM-DD"
                      :hide-dropdown-icon="true"
                    >
                      <template v-slot:append>
                        <fdev-icon name="event" class="cursor-pointer" />
                      </template>
                    </fdev-select>
                  </Field>
                  <fdev-input
                    label="需求提出部门"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.propose_demand_dept"
                  />
                  <fdev-input
                    label="需求联系人"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.propose_demand_user"
                  />
                  <fdev-input
                    label="需求说明书名称"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.demand_instruction"
                  />
                  <fdev-input
                    class="q-mb-sm q-mt-sm"
                    label="对应需求计划编号"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.demand_plan_no"
                  />
                  <fdev-input
                    label="需求计划名称"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.demand_plan_name"
                  />
                  <fdev-input
                    label="设计稿审核状态"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.design_status"
                  />
                  <field label="需求背景" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="textarea"
                      class="textarea"
                      dense
                      readonly
                      v-model="updateDialogModel.demand_background"
                    />
                  </field>
                  <field label="前期沟通情况" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="textarea"
                      class="textarea"
                      dense
                      readonly
                      v-model="updateDialogModel.former_communication"
                    />
                  </field>
                  <field label="需求是否可行" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="需求是否可行"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.demand_available"
                    />
                  </field>
                  <fdev-input
                    label="需求评估方式"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.demand_assess_way"
                  />
                  <fdev-input
                    label="需求备案编号"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.demand_record_no"
                  />
                  <field label="未来是否纳入后评估" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="未来是否纳入后评估"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.future_assess"
                    />
                  </field>
                  <field label="评审人" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="评审人"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.review_user"
                    />
                  </field>
                  <Field label="需求期望投产日期" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      outlined
                      clearable
                      readonly
                      v-model="updateDialogModel.respect_product_date"
                      type="text"
                      dense
                      mask="YYYY-MM-DD"
                      :hide-dropdown-icon="true"
                    >
                      <template v-slot:append>
                        <fdev-icon name="event" class="cursor-pointer" />
                      </template>
                    </fdev-select>
                  </Field>
                  <field label="需求可行性评估意见" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="需求可行性评估意见"
                      type="textarea"
                      class="textarea"
                      dense
                      readonly
                      v-model="updateDialogModel.available_assess_idea"
                    />
                  </field>
                </fdev-card-section>
                <fdev-card-section v-if="demand_type === 'tech'">
                  <fdev-input
                    label="需求名称"
                    class="q-mb-sm q-mt-sm"
                    type="text"
                    dense
                    readonly
                    v-model="updateDialogModel.demand_instruction"
                  />
                  <field label="需求是否可行" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.demand_available"
                    />
                  </field>
                  <field label="需求说明" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="textarea"
                      class="textarea"
                      dense
                      readonly
                      v-model="updateDialogModel.demand_desc"
                    />
                  </field>
                </fdev-card-section>
              </fdev-card>
              <fdev-card class="card">
                <fdev-card-section>
                  <div class="title">安排与实施</div>
                </fdev-card-section>
                <fdev-card-section v-if="demand_type !== 'tech'">
                  <field label="牵头小组" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select-width"
                      dense
                      use-input
                      :disable="!isRqrManager"
                      :options="leaderGroupOptions"
                      @filter="leaderGroupFilter"
                      option-label="fullName"
                      ref="updateDialogModel.demand_leader_group"
                      v-model="$v.updateDialogModel.demand_leader_group.$model"
                      :rules="[
                        () =>
                          $v.updateDialogModel.demand_leader_group.required ||
                          '请选择牵头小组'
                      ]"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label>{{
                              scope.opt.name
                            }}</fdev-item-label>
                            <fdev-item-label caption>
                              {{ scope.opt.fullName }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </field>
                  <field label="牵头人" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select-width"
                      dense
                      multiple
                      use-input
                      use-chips
                      :disable="!isRqrManager"
                      :options="userFilterOptions"
                      ref="updateDialogModel.demand_leader"
                      @filter="userFilter"
                      v-model="$v.updateDialogModel.demand_leader.$model"
                      :rules="[
                        () =>
                          $v.updateDialogModel.demand_leader.required ||
                          '请选择牵头人'
                      ]"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
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
                  </field>
                  <field label="规划联系人" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      :disable="!isRqrManager"
                      ref="updateDialogModel.plan_user"
                      v-model="$v.updateDialogModel.plan_user.$model"
                      :rules="[
                        () =>
                          $v.updateDialogModel.plan_user.required ||
                          '请输入规划联系人'
                      ]"
                    />
                  </field>
                  <field label="优先级" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select-width"
                      dense
                      filled
                      fill-input
                      emit-value
                      map-options
                      clearable
                      input-debounce="0"
                      :options="priorityOptions"
                      option-label="label"
                      option-value="value"
                      :disable="!isRqrManager"
                      ref="updateDialogModel.priority"
                      v-model="$v.updateDialogModel.priority.$model"
                      :rules="[
                        () =>
                          $v.updateDialogModel.priority.required ||
                          '请选择优先级'
                      ]"
                    />
                  </field>
                  <Field label="受理日期" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      outlined
                      clearable
                      :disable="!isRqrManager"
                      ref="updateDialogModel.accept_date"
                      v-model="$v.updateDialogModel.accept_date.$model"
                      type="text"
                      dense
                      :rules="[
                        () =>
                          $v.updateDialogModel.accept_date.required ||
                          '请输入受理日期'
                      ]"
                      mask="YYYY-MM-DD"
                      :hide-dropdown-icon="true"
                    >
                      <template v-slot:append>
                        <fdev-icon name="event" class="cursor-pointer">
                          <fdev-popup-proxy
                            ref="qDateProxyUat"
                            transition-show="scale"
                            transition-hide="scale"
                          >
                            <fdev-date
                              @input="() => $refs.qDateProxyUat.hide()"
                              v-model="$v.updateDialogModel.accept_date.$model"
                              mask="YYYY-MM-DD"
                            />
                          </fdev-popup-proxy>
                        </fdev-icon>
                      </template>
                    </fdev-select>
                  </Field>
                  <Field label="计划启动日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.plan_start_date"
                    />
                  </Field>
                  <Field label="计划提测日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.plan_test_date"
                    />
                  </Field>
                  <Field label="计划投产日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.plan_product_date"
                    />
                  </Field>
                  <field label="实际启动日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="实际启动日期"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.real_start_date"
                    />
                  </field>
                  <field label="实际提测日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="实际提测日期"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.real_test_date"
                    />
                  </field>
                  <field label="实际投产日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="实际投产日期"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.real_product_date"
                    />
                  </field>
                </fdev-card-section>
                <fdev-card-section v-if="demand_type === 'tech'">
                  <field label="牵头小组" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select-width"
                      dense
                      use-input
                      :disable="!isRqrManager"
                      :options="leaderGroupOptions"
                      @filter="leaderGroupFilter"
                      option-label="fullName"
                      ref="updateDialogModel.demand_leader_group"
                      v-model="$v.updateDialogModel.demand_leader_group.$model"
                      :rules="[
                        () =>
                          $v.updateDialogModel.demand_leader_group.required ||
                          '请选择牵头小组'
                      ]"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label>{{
                              scope.opt.name
                            }}</fdev-item-label>
                            <fdev-item-label caption>
                              {{ scope.opt.fullName }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </field>
                  <field label="牵头人" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select-width"
                      dense
                      multiple
                      use-input
                      use-chips
                      :disable="!isRqrManager"
                      :options="userFilterOptions"
                      ref="updateDialogModel.demand_leader"
                      @filter="userFilter"
                      v-model="$v.updateDialogModel.demand_leader.$model"
                      :rules="[
                        () =>
                          $v.updateDialogModel.demand_leader.required ||
                          '请选择牵头人'
                      ]"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
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
                  </field>
                  <field label="优先级" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select-width"
                      dense
                      filled
                      fill-input
                      emit-value
                      map-options
                      clearable
                      input-debounce="0"
                      :options="priorityOptions"
                      option-label="label"
                      option-value="value"
                      :disable="!isRqrManager"
                      ref="updateDialogModel.priority"
                      v-model="updateDialogModel.priority"
                      :rules="[
                        () =>
                          $v.updateDialogModel.priority.required ||
                          '请选择优先级'
                      ]"
                    />
                  </field>
                  <Field label="受理日期" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      outlined
                      clearable
                      :disable="!isRqrManager"
                      ref="updateDialogModel.accept_date"
                      v-model="$v.updateDialogModel.accept_date.$model"
                      type="text"
                      dense
                      :rules="[
                        () =>
                          $v.updateDialogModel.accept_date.required ||
                          '请输入受理日期'
                      ]"
                      mask="date"
                      :hide-dropdown-icon="true"
                    >
                      <template v-slot:append>
                        <fdev-icon name="event" class="cursor-pointer">
                          <fdev-popup-proxy
                            ref="qDateProxyUat"
                            transition-show="scale"
                            transition-hide="scale"
                          >
                            <fdev-date
                              @input="() => $refs.qDateProxyUat.hide()"
                              v-model="$v.updateDialogModel.accept_date.$model"
                              mask="YYYY-MM-DD"
                            />
                          </fdev-popup-proxy>
                        </fdev-icon>
                      </template>
                    </fdev-select>
                  </Field>
                  <Field label="计划启动日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.plan_start_date"
                    />
                  </Field>
                  <Field label="计划提测日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.plan_test_date"
                    />
                  </Field>
                  <Field label="计划投产日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.plan_product_date"
                    />
                  </Field>
                  <field label="实际启动日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="实际启动日期"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.real_start_date"
                    />
                  </field>
                  <field label="实际提测日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="实际提测日期"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.real_test_date"
                    />
                  </field>
                  <field label="实际投产日期" class="q-mb-sm q-mt-sm">
                    <fdev-input
                      outlined
                      lable="实际投产日期"
                      type="text"
                      dense
                      readonly
                      v-model="updateDialogModel.real_product_date"
                    />
                  </field>
                </fdev-card-section>
              </fdev-card>
              <fdev-card class="card">
                <fdev-card-section class="q-pt-none">
                  <div class="title">评估安排</div>
                </fdev-card-section>
                <fdev-card-section v-if="demand_type !== 'tech'">
                  <field
                    label="实施团队可行性评估补充意见"
                    class="q-mb-sm q-mt-sm"
                  >
                    <fdev-input
                      outlined
                      lable="实施团队可行性评估补充意见"
                      type="textarea"
                      dense
                      readonly
                      v-model="updateDialogModel.extra_idea"
                    />
                  </field>
                  <fdev-toggle
                    v-model="ui_verify"
                    :disable="checkUIdesign"
                    color="green"
                    label="是否涉及UI设计审核1:"
                    left-label
                  />
                  <Field label="涉及板块" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select"
                      multiple
                      use-input
                      dense
                      outlined
                      ref="updateDialogModel.relate_part"
                      v-model="updateDialogModel.relate_part"
                      :disable="!isRqrManager"
                      :options="relate_partOptions"
                      @filter="relateGroupFilter"
                      @add="addRelatedGroups"
                      @remove="removeRelatedGroup"
                      :rules="[
                        () =>
                          $v.updateDialogModel.relate_part.required ||
                          '请选择涉及板块'
                      ]"
                    >
                      <template v-slot:selected-item="scope">
                        <fdev-chip
                          dense
                          :removable="scope.opt.clickable"
                          @remove="scope.removeAtIndex(scope.index)"
                          :tabindex="scope.tabindex"
                        >
                          {{ scope.opt.name }}
                        </fdev-chip>
                      </template>
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label>{{
                              scope.opt.name
                            }}</fdev-item-label>
                            <fdev-item-label caption>
                              {{ scope.opt.fullName }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </Field>
                  <div class="row" v-if="greatKey.length !== 0">
                    <div
                      class="col-12"
                      v-for="(item, index) in $v.greatKey.$each.$iter"
                      :key="index"
                      style="margin-bottom:10px"
                    >
                      <field
                        :label="item.group.$model.label"
                        class="q-mb-sm q-mt-sm"
                      >
                        <fdev-select
                          class="select-width"
                          dense
                          multiple
                          use-input
                          use-chips
                          :options="partNameFilterOptions"
                          :ref="`updateDialogModel${index}`"
                          @filter="partNameFilter"
                          @focus="handleInputFocus(item)"
                          v-model="item.users.$model"
                          option-label="user_name_cn"
                          option-value="id"
                          :rules="[
                            () => item.users.required || '请选择涉及评估人'
                          ]"
                        >
                          <template v-slot:option="scope">
                            <fdev-item
                              v-bind="scope.itemProps"
                              v-on="scope.itemEvents"
                            >
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
                      </field>
                    </div>
                  </div>
                </fdev-card-section>
                <fdev-card-section v-if="demand_type === 'tech'">
                  <field
                    label="实施团队可行性评估补充意见"
                    class="q-mb-sm q-mt-sm"
                  >
                    <fdev-input
                      outlined
                      lable="实施团队可行性评估补充意见"
                      type="textarea"
                      dense
                      readonly
                      v-model="updateDialogModel.extra_idea"
                    />
                  </field>
                  <Field label="涉及板块" class="q-mb-sm q-mt-sm">
                    <fdev-select
                      class="select"
                      multiple
                      use-input
                      use-chips
                      dense
                      outlined
                      ref="updateDialogModel.relate_part"
                      v-model="updateDialogModel.relate_part"
                      :disable="!isRqrManager"
                      :options="relate_partOptions"
                      @filter="relateGroupFilter"
                      @add="addRelatedGroups"
                      @remove="removeRelatedGroup"
                      :rules="[
                        () =>
                          $v.updateDialogModel.relate_part.required ||
                          '请选择涉及板块'
                      ]"
                    >
                      <template v-slot:selected-item="scope">
                        <fdev-chip
                          dense
                          :removable="scope.opt.clickable"
                          @remove="scope.removeAtIndex(scope.index)"
                          :tabindex="scope.tabindex"
                        >
                          {{ scope.opt.name }}
                        </fdev-chip>
                      </template>
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label>{{
                              scope.opt.name
                            }}</fdev-item-label>
                            <fdev-item-label caption>
                              {{ scope.opt.fullName }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </Field>
                  <div class="row" v-if="greatKey.length !== 0">
                    <div
                      class="col-12"
                      v-for="(item, index) in $v.greatKey.$each.$iter"
                      :key="index"
                      style="margin-bottom:10px"
                    >
                      {{ item.users }}
                      <field
                        :label="item.group.$model.label"
                        class="q-mb-sm q-mt-sm"
                      >
                        <fdev-select
                          class="select-width"
                          dense
                          use-input
                          use-chips
                          multiple
                          :options="partNameFilterOptions"
                          :ref="`updateDialogModel${index}`"
                          @filter="partNameFilter"
                          @focus="handleInputFocus(item)"
                          v-model="item.users.$model"
                          :rules="[
                            () => item.users.required || '请选择涉及评估人'
                          ]"
                        >
                          <template v-slot:option="scope">
                            <fdev-item
                              v-bind="scope.itemProps"
                              v-on="scope.itemEvents"
                            >
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
                      </field>
                    </div>
                  </div>
                </fdev-card-section>
              </fdev-card>

              <div cls="submit-div">
                <fdev-btn
                  label="提交"
                  class="full-width"
                  color="primary"
                  type="submit"
                  @click="handleOptimizeAllTip"
                >
                </fdev-btn>
              </div>
            </fdev-form>
          </div>
        </fdev-page>
      </fdev-page-container>
    </fdev-layout>
  </fdev-dialog>
</template>

<script>
import Field from '@/components/Field';
import {
  updateDialogModel,
  priorityOptions,
  queryUserOptionsParams
} from '../model';
import { required } from 'vuelidate/lib/validators';
import { formatUser } from '@/modules/User/utils/model';
import { formatOption, validate, successNotify } from '@/utils/utils';
import { mapState, mapGetters, mapActions } from 'vuex';
export default {
  name: 'updateDialog',
  components: { Field },
  data() {
    return {
      demand_type: '',
      priorityOptions: priorityOptions,
      updateDialogModel: updateDialogModel(),
      ui_verify: false,
      userOptions: [],
      leaderGroupOptions: [],
      groups: [],
      userFilterOptions: [],
      users: [],
      greatKey: [],
      relate_partOptions: [],
      partNameFilterOptions: [],
      filterUserGroupId: ''
    };
  },
  validations: {
    updateDialogModel: {
      demand_leader_group: {
        required
      },
      demand_leader: {
        required
      },
      plan_user: {
        required
      },
      priority: {
        required
      },
      accept_date: {
        required
      },
      relate_part: {
        required
      }
    },
    greatKey: {
      required,
      $each: {
        group: {
          required
        },
        users: {
          required
        }
      }
    }
  },

  props: {
    value: {
      type: Boolean,
      default: false
    },
    rqrId: {
      type: String,
      default: () => ''
    }
  },
  /* 
    别删除  过后再改  
    watch: {
    'updateDialogModel.demand_leader_group': {
      //监听demand_leader_group变化，执行下面handler函数
      handler: function(val, oldVal) {
        //当demand_leader.length===0，说明那个牵头人选择框此时还没有进行选择
        if (this.updateDialogModel.demand_leader.length === 0) {
          //执行userOptionsFilter()这个方法，并赋给中间数组userOptions。
          this.userOptions = this.userOptionsFilter();
          //现在这个userFilterOptions对应的选项变为已选小组下的人
          this.userFilterOptions = this.userOptions;
        } else {
          //当那个id不等于第一个选择人组id
          if ( this.updateDialogModel.demand_leader[0] && val.id !== this.updateDialogModel.demand_leader[0].group_id) {
            this.updateDialogModel.demand_leader = []; //清空选择的牵头人
            this.userOptions = this.userOptionsFilter(); //又去做一次筛选
            this.userFilterOptions = this.userOptions; //然后赋给userFilterOptions
          }
        }
        if (this.updateDialogModel.relate_part.length !== 0) {
          if (oldVal !== '') {
            const oldRef = this.updateDialogModel.relate_part.findIndex(
              part => part === oldVal.id
            );
            if (oldRef > -1) {
              this.updateDialogModel.relate_part.splice(oldRef, 1);
              this.greatKey.splice(oldRef, 1);
            }
          }
          const newRef = this.updateDialogModel.relate_part.findIndex(
            part => part === val.id
          );
          if (newRef === -1) {
            this.updateDialogModel.relate_part.push(val);
            this.greatKey.push({
              group: val,
              users: []
            });
          }
        } else {
          this.updateDialogModel.relate_part.push(val);
          this.greatKey.push({
            group: val,
            users: []
          });
        }
      }
    },
    'updateDialogModel.demand_leader': {
      handler: function(val) {
        if (val.length === 1) {
          this.updateDialogModel.demand_leader_group = this.leaderGroupOptions.find(
            g => {
              return g.id === val[0].group_id;
            }
          );
          this.userOptions = this.userOptionsFilter();
          this.userFilterOptions = this.userOptions;
        }
      }
    }
  }, */

  computed: {
    ...mapState('userForm', {
      groupsData: 'groups',
      userInPage: 'userInPage'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('demandsForm', ['demandInfoDetail']),
    ...mapGetters('user', ['isLoginUserList', 'isDemandManager']),
    isRqrManager() {
      return this.isDemandManager;
    },
    checkUIdesign() {
      let design_status = this.updateDialogModel.design_status;
      return (
        design_status === 'auditIn' ||
        design_status === 'auditPassNot' ||
        design_status === 'completedAudit'
      );
    }
  },

  methods: {
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup',
      queryUserPagination: 'queryUserPagination'
    }),
    ...mapActions('demandsForm', {
      update: 'update',
      queryDemandInfoDetail: 'queryDemandInfoDetail'
    }),
    async initQuery() {
      await Promise.all([
        (async () => {
          await this.queryGroup();
          this.groups = formatOption(this.groupsData);
          this.leaderGroupOptions = this.groups.slice(0);
          await this.queryUserPagination(queryUserOptionsParams);
          this.users = this.userInPage.list.map(user =>
            formatOption(formatUser(user), 'name')
          );
          this.userOptions = this.users.slice(0);
          this.userFilterOptions = this.userOptions;
        })(),
        this.init()
      ]);
      //将String类型的updateDialogModel.relate_part转化为object
      this.updateDialogModel.relate_part = this.groups.filter(
        group => this.updateDialogModel.relate_part.indexOf(group.id) !== -1
      );
      /*  .map(group => {
          return {
            ...group,
            clickable: group.name !== this.updateDialogModel.demand_leader_group
          };
        }); */
      //初始化页面时，将回显的updateDialogModel.relate_part给this.greatKey
      /* this.greatKey = this.updateDialogModel.relate_part.map(group => ({
        group,
        users: []
      })); */
      this.greatKey = this.updateDialogModel.relate_part_detail.map(
        relate_part => ({
          group: this.groups.filter(
            group => group.id === relate_part.part_id
          )[0],
          // users: relate_part.assess_user_all.map(user => user.id)
          users: this.findUserByUserId(
            relate_part.assess_user_all.map(user => user.id)
          )
        })
      );
    },
    userOptionsFilter(group_id) {
      if (group_id) {
        const myUser = this.users.filter(item => item.group_id === group_id);
        return myUser;
      }
      const selectedGroup = this.updateDialogModel.demand_leader_group;
      let myUser = this.users.filter(item => {
        if (selectedGroup) {
          return item.group_id === selectedGroup.id;
        }
        return true;
      });
      return myUser;
    },
    userFilter(val, update, abort) {
      update(() => {
        this.userFilterOptions = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    relateGroupFilter(val, update, abort) {
      update(() => {
        this.relate_partOptions = this.groups
          .filter(group => group.fullName.indexOf(val) > -1)
          .map(opt => {
            return {
              ...opt,
              clickable: opt.name !== this.updateDialogModel.demand_leader_group
            };
          })
          .filter(opt => opt.clickable);
      });
    },
    partNameFilter(val, update, abort) {
      update(() => {
        this.partNameFilterOptions = this.users.filter(
          user =>
            user.group_id === this.filterUserGroupId &&
            (user.user_name_cn.indexOf(val) > -1 ||
              user.user_name_en.toLowerCase().includes(val.toLowerCase()))
        );
      });
    },
    handleInputFocus(item) {
      this.filterUserGroupId = item.group.$model.id;
      this.partNameFilterOptions = this.userOptionsFilter(
        this.filterUserGroupId
      );
    },
    leaderGroupFilter(val, update, abort) {
      update(() => {
        this.leaderGroupOptions = this.groups.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    addRelatedGroups(group) {
      this.greatKey.push({
        group: group.value,
        users: []
      });
    },
    removeRelatedGroup(detail) {
      const index = this.greatKey.findIndex(item => {
        return item.group.id === detail.value.id;
      });
      this.greatKey.splice(index, 1);
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.$emit('input', false);
        });
    },
    handleOptimizeAllTip() {
      this.$v.greatKey.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('updateDialogModel') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.updateDialogModel.$invalid) {
        return;
      }
      if (this.$v.greatKey.$invalid) {
        return;
      }
      this.updateDialogModel.relate_part_detail = this.greatKey.map(item => {
        return {
          part_id: item.group.id,
          part_name: item.group.name,
          assess_user_all: item.users.map(user => {
            return {
              id: user.id,
              user_name_cn: user.user_name_cn,
              user_name_en: user.user_name_en
            };
          })
        };
      });
    },
    async handleOptimize() {
      this.$q
        .dialog({
          title: `修改确认`,
          message: `确认要修改本条需求信息吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          let params;
          if (this.demand_type === 'tech') {
            params = {
              ...this.updateDialogModel,
              id: this.rqrId,
              demand_leader_group: this.updateDialogModel.demand_leader_group,
              demand_leader: this.updateDialogModel.demand_leader,
              demand_leader_group_cn: this.updateDialogModel.demand_leader_group
                .name,
              priority: this.updateDialogModel.priority,
              relate_part: this.updateDialogModel.relate_part.name,
              relate_part_detail: this.updateDialogModel.relate_part_detail
            };
            Reflect.set(
              params,
              'demand_leader_all',
              this.updateDialogModel.demand_leader.map(user => {
                return {
                  id: user.id,
                  user_name_cn: user.user_name_cn,
                  user_name_en: user.user_name_en
                };
              })
            );
          } else {
            params = {
              ...this.updateDialogModel,
              id: this.rqrId,
              demand_leader_group: this.updateDialogModel.demand_leader_group,
              demand_leader_group_cn: this.updateDialogModel.demand_leader_group
                .name,
              demand_leader: this.updateDialogModel.demand_leader,
              priority: this.updateDialogModel.priority,
              accept_date: this.updateDialogModel.accept_date,
              relate_part: this.updateDialogModel.relate_part.name,
              relate_part_detail: this.updateDialogModel.relate_part_detail
            };
            Reflect.set(
              params,
              'demand_leader_all',
              this.updateDialogModel.demand_leader.map(user => {
                return {
                  id: user.id,
                  user_name_cn: user.user_name_cn,
                  user_name_en: user.user_name_en
                };
              })
            );
          }
          await this.update(params);
          this.updateDialogModel = {};
          successNotify('修改成功');
        });
    },
    async init() {
      await this.queryDemandInfoDetail({ id: this.rqrId });
      if (this.demandInfoDetail.demand_available) {
        if (this.demandInfoDetail.demand_available == '0') {
          this.demandInfoDetail.demand_available = '可行';
        } else if (this.demandInfoDetail.demand_available == '1') {
          this.demandInfoDetail.demand_available = '部分可行';
        } else {
          this.demandInfoDetail.demand_available = '不可行';
        }
      }
      if (this.demandInfoDetail.future_assess == '0') {
        this.demandInfoDetail.future_assess = '纳入后评估';
      } else {
        this.demandInfoDetail.future_assess = '不纳入后评估';
      }
      if (this.demandInfoDetail.demand_assess_way == '0') {
        this.demandInfoDetail.demand_assess_way = '业务需求评审';
      } else {
        this.demandInfoDetail.demand_assess_way = '科技需求备案';
      }
      this.updateDialogModel = this.demandInfoDetail;
      //
      this.updateDialogModel.demand_leader_group = this.findGroupByGroupId(
        this.updateDialogModel.demand_leader_group
      );
      this.updateDialogModel.demand_leader = this.findUserByUserId(
        this.updateDialogModel.demand_leader_all.map(leader => leader.id)
      );
      this.demand_type = this.demandInfoDetail.demand_type;
    },
    findUserByUserId(userId) {
      if (Array.isArray(userId)) {
        return userId.map(id => this.findUserByUserId(id));
      }
      return this.users.find(user => user.id === userId);
    },
    findGroupByGroupId(groupId) {
      if (Array.isArray(groupId)) {
        return groupId.map(id => this.findUserByUserId(id));
      }
      return this.groups.find(group => group.id === groupId);
    }
  }
};
</script>
<style lang="stylus" scoped>


.q-layout-padding {
  padding: 16px 26px 16px 20px;
}

.dialog {
  width: 93%;
}

.title {
  font-size: 18px;
}

.card {
  padding: 10px;
}

.select-width {
  max-width: 248px;
}
</style>
