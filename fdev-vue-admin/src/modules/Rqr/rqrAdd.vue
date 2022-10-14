<template>
  <f-block>
    <Loading :visible="loading">
      <!-- 步进器组件 -->
      <fdev-stepper
        flat
        animated
        header-nav
        ref="stepper"
        v-model="step"
        alternative-labels
        transition-prev="fade"
        transition-next="fade"
        v-if="step < 4"
      >
        <!-- 第一步 -->
        <fdev-step
          :name="1"
          prefix="1"
          active-prefix="1"
          title="选择需求类型"
          :done="step > 1"
          :header-nav="step > 1"
        >
          <div class="row justify-center q-mb-xl">
            <div class="col-12">
              <div class="step1-form">
                <f-formitem
                  label="选择需求类型"
                  required
                  label-style="width: 112px"
                >
                  <fdev-select
                    v-model="demand"
                    :options="demandList"
                    @input="handleStep1"
                    :rules="[val => !!val || '请选择需求类型']"
                  />
                </f-formitem>
              </div>
            </div>
          </div>
        </fdev-step>
        <!-- 第二步 -->
        <fdev-step
          :name="2"
          prefix="2"
          active-prefix="2"
          :title="title"
          :done="step > 2"
          :header-nav="step > 2"
        >
          <fdev-form>
            <div class="items-start q-gutter-sm q-mt-sm">
              <!-- 基础信息 -->
              <div>
                <common-title icon-name="list_s_f" label="基础信息" />
                <div class="row justify-between mb-12 ml-20 m-87">
                  <!-- 需求名称、需求标题 -->
                  <f-formitem
                    required
                    :class="isDailyFun() ? 'full-width' : ''"
                    :label="isIpmpSyncNameFun()"
                    v-if="!isTechFun()"
                    label-style="width: 112px"
                  >
                    <fdev-input
                      maxlength="200"
                      ref="demandModel.oa_contact_name"
                      v-model="demandModel.oa_contact_name"
                      :rules="[v => !!v || `请输入${isIpmpSyncNameFun()}`]"
                    >
                    </fdev-input>
                  </f-formitem>
                  <!-- 需求编号 -->
                  <f-formitem
                    label="需求编号"
                    required
                    v-if="isBusinessFun()"
                    label-style="width: 112px"
                  >
                    <!-- v => oaRealNoRegRule(v) || '输入格式不正确', -->
                    <fdev-input
                      ref="demandModel.oa_real_no"
                      v-model="demandModel.oa_real_no"
                      @blur="checkOANum"
                      :rules="[
                        v => !!v || '请输入需求编号',
                        v =>
                          oaRealNoUseRule(v) ||
                          '当前需求编号已被使用，请重新申请'
                      ]"
                    />
                  </f-formitem>
                  <!-- 需求说明书名称 -->
                  <f-formitem
                    label="需求说明书名称"
                    required
                    v-if="isTechFun()"
                    label-style="width: 112px"
                  >
                    <fdev-input
                      maxlength="200"
                      ref="demandModel.demand_instruction"
                      v-model="demandModel.demand_instruction"
                      :rules="[v => !!v || '请输入需求书名称']"
                    />
                  </f-formitem>
                  <!-- 是否涉及UI审核 -->
                  <f-formitem
                    label="是否涉及UI审核"
                    v-if="isBusinessFun()"
                    label-style="width: 112px"
                  >
                    <fdev-select
                      class="select"
                      hint=""
                      v-model="demandModel.ui_verify"
                      :options="uiOptions"
                    />
                  </f-formitem>
                  <!-- 需求说明 -->
                  <f-formitem
                    :required="isDailyFun()"
                    label="需求说明"
                    :class="isDailyFun() ? 'full-width' : ''"
                    label-style="width: 112px"
                  >
                    <fdev-input
                      ref="demandModel.demand_desc"
                      v-model="demandModel.demand_desc"
                      type="textarea"
                      :rules="[
                        v => (isDailyFun() ? !!v || '请输入需求说明' : true)
                      ]"
                    />
                  </f-formitem>

                  <!-- 优先级 -->
                  <f-formitem
                    label="优先级"
                    v-if="!isDailyFun()"
                    label-style="width: 112px"
                  >
                    <fdev-select
                      hint=""
                      class="select"
                      v-model="demandModel.priority"
                      :options="priorities"
                    />
                  </f-formitem>
                  <!-- 需求属性 -->
                  <f-formitem label="需求属性" label-style="width: 112px">
                    <fdev-select
                      ref="demandModel.demand_property"
                      v-model="demandModel.demand_property"
                      option-label="name"
                      :options="propertyOptions"
                      hint=""
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label :title="scope.opt.name">{{
                              scope.opt.name
                            }}</fdev-item-label>
                            <fdev-item-label
                              :title="scope.opt.fullName"
                              caption
                            >
                              {{ scope.opt.fullName }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </f-formitem>
                  <!-- 科技类型 -->
                  <f-formitem
                    label="科技类型"
                    v-if="isTechFun()"
                    label-style="width: 112px"
                  >
                    <fdev-select
                      hint=""
                      class="select"
                      v-model="demandModel.tech_type"
                      :options="techTypes"
                      @input="onChangeTechType"
                    />
                  </f-formitem>
                  <f-formitem
                    label="需求标签"
                    full-width
                    label-style="width: 112px"
                  >
                    <div class="row q-gutter-sm">
                      <div v-for="(item, index) in optionsList" :key="index">
                        <fdev-checkbox
                          keep-color
                          v-model="item.flag"
                          :color="item.color"
                          ><span
                            class="labelSty"
                            :style="'background:' + item.color"
                          >
                            {{ item.value }}
                          </span></fdev-checkbox
                        >
                      </div>
                    </div>
                  </f-formitem>
                  <!-- 备注 -->
                  <f-formitem
                    required
                    label="备注"
                    v-if="isTechFun() && isOtherType(demandModel.tech_type)"
                    label-style="width: 112px"
                  >
                    <fdev-input
                      ref="demandModel.tech_type_desc"
                      v-model="demandModel.tech_type_desc"
                      :rules="[
                        v => techTypeDescRule(v) || '请输入科技类型备注'
                      ]"
                    />
                  </f-formitem>
                </div>
              </div>
              <!-- 安排与实施 -->
              <div>
                <common-title
                  width="18"
                  height="18"
                  x="2"
                  y="2"
                  icon-name="bell_s_f"
                  label="安排与实施"
                />
                <div class="row justify-between mb-12 ml-20 m-87">
                  <!-- 牵头人 -->
                  <f-formitem
                    label="牵头人"
                    required
                    label-style="width: 112px"
                  >
                    <fdev-select
                      multiple
                      use-input
                      ref="demandModel.demand_leader"
                      v-model="demandModel.demand_leader"
                      :options="userFilterOptions"
                      @filter="userFilter"
                      :rules="[v => !demandLeaderIsEmpty(v) || '请选择牵头人']"
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
                              :title="
                                `${scope.opt.user_name_en}--${scope.opt.group}`
                              "
                              caption
                            >
                              {{ scope.opt.user_name_en }}--{{
                                scope.opt.group
                              }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </f-formitem>
                  <!-- 牵头小组 -->
                  <f-formitem
                    label="牵头小组"
                    required
                    label-style="width: 112px"
                  >
                    <fdev-select
                      use-input
                      ref="demandModel.demand_leader_group"
                      v-model="demandModel.demand_leader_group"
                      :options="leaderGroupOptions"
                      @filter="leaderGroupFilter"
                      option-label="name"
                      :rules="[v => !leaderGroupIsEmpty(v) || '请选择牵头小组']"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label :title="scope.opt.name">{{
                              scope.opt.name
                            }}</fdev-item-label>
                            <fdev-item-label
                              :title="scope.opt.fullName"
                              caption
                            >
                              {{ scope.opt.fullName }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </f-formitem>
                  <!-- 优先级 -->
                  <f-formitem
                    label="优先级"
                    v-if="isDailyFun()"
                    label-style="width: 112px"
                  >
                    <fdev-select
                      hint=""
                      class="select"
                      v-model="demandModel.priority"
                      :options="priorities"
                    />
                  </f-formitem>
                  <!-- 受理日期 -->
                  <f-formitem
                    label="受理日期"
                    v-if="!isBusinessFun()"
                    label-style="width: 112px"
                  >
                    <f-date
                      v-model="demandModel.accept_date"
                      mask="YYYY-MM-DD"
                      :clearable="true"
                      :hide-dropdown-icon="true"
                      hint=""
                    >
                    </f-date>
                  </f-formitem>
                </div>
              </div>
              <!-- 评估安排 -->
              <div>
                <common-title
                  width="18"
                  height="18"
                  x="2"
                  y="2"
                  icon-name="bell_s_f"
                  label="评估安排"
                />
                <div class="row mb-12 ml-20 m-87">
                  <!-- 涉及小组 -->
                  <f-formitem
                    label="涉及小组"
                    required
                    label-style="width: 112px"
                  >
                    <fdev-select
                      class="select"
                      multiple
                      use-input
                      ref="demandModel.relate_part"
                      v-model="demandModel.relate_part"
                      :options="relatedFilterGroup"
                      @filter="relatedGroupFilter"
                      @add="addRelatedGroups"
                      @remove="removeRelatedGroup"
                      :rules="[v => !relatePartisEmpty(v) || '请选择涉及小组']"
                    >
                      <template v-slot:selected-item="scope">
                        <fdev-chip
                          dense
                          :removable="true"
                          @remove="scope.removeAtIndex(scope.index)"
                          :tabindex="scope.tabindex"
                        >
                          {{ scope.opt.name }}
                        </fdev-chip>
                      </template>
                      <template v-slot:option="scope">
                        <fdev-item
                          :clickable="
                            scope.opt.id !==
                              demandModel.demand_leader_group.id ||
                              !demandModel.relate_part
                                .map(part => part.id)
                                .includes(scope.opt.id)
                          "
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label :title="scope.opt.name">{{
                              scope.opt.name
                            }}</fdev-item-label>
                            <fdev-item-label
                              :title="scope.opt.fullName"
                              caption
                            >
                              {{ scope.opt.fullName }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </f-formitem>
                </div>
              </div>
              <!-- 评估人员 -->
              <div v-if="greatKey.length !== 0">
                <common-title
                  width="18"
                  height="18"
                  x="2"
                  y="2"
                  icon-name="bell_s_f"
                  label="评估人员"
                />
                <div
                  class="row justify-between mb-12 ml-20 m-87"
                  v-if="greatKey.length !== 0"
                >
                  <div v-for="(u, i) in greatKey" :key="i">
                    <f-formitem
                      :label="u.group.label"
                      required
                      label-style="width: 112px"
                    >
                      <fdev-select
                        multiple
                        use-input
                        use-chips
                        :ref="`demandModel${i}`"
                        v-model="u.users"
                        :options="u.options"
                        @focus="handleInputFocus(u, i)"
                        @filter="relatedUsersFilter"
                        :rules="[v => !isExitEmpty(v) || '请选择评估人员']"
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
                                :title="
                                  `${scope.opt.user_name_en}--${
                                    scope.opt.group
                                  }`
                                "
                                caption
                              >
                                {{ scope.opt.user_name_en }}--{{
                                  scope.opt.group
                                }}
                              </fdev-item-label>
                            </fdev-item-section>
                          </fdev-item>
                        </template>
                      </fdev-select>
                    </f-formitem>
                  </div>
                </div>
              </div>
            </div>
            <fdev-stepper-navigation>
              <div class="row justify-center">
                <fdev-btn
                  label="上一步"
                  outline
                  class="mr-32"
                  @click="handleStepPrevious"
                />
                <fdev-btn label="需求录入" @click="handleStep2AllTip" />
              </div>
            </fdev-stepper-navigation>
          </fdev-form>
        </fdev-step>
        <!-- 第三步 -->
        <fdev-step
          :name="3"
          prefix="3"
          active-prefix="3"
          title="确认信息"
          :done="step > 3"
          :header-nav="step > 3"
        >
          <div class="row border mb-32">
            <!-- 需求编号 -->
            <f-formitem
              v-if="isBusinessFun()"
              label="需求编号"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              <div class="ellipsis break-word" :title="demandModel.oa_real_no">
                {{ demandModel.oa_real_no }}
              </div>
            </f-formitem>
            <!-- 需求名称、需求标题 -->
            <f-formitem
              v-if="!isTechFun()"
              :label="isIpmpSyncNameFun()"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              <div
                class="ellipsis break-word"
                :title="demandModel.oa_contact_name"
              >
                {{ demandModel.oa_contact_name }}
              </div>
            </f-formitem>
            <!-- 需求书名称 -->
            <f-formitem
              label="需求书名称"
              bottom-page
              class="col-4"
              v-if="isTechFun()"
              label-style="width: 112px; color: #999"
            >
              <div
                class="ellipsis break-word"
                :title="demandModel.demand_instruction"
              >
                {{ demandModel.demand_instruction }}
              </div>
            </f-formitem>
            <!-- 科技类型 -->
            <f-formitem
              v-if="isTechFun()"
              label="科技类型"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              {{ demandModel.tech_type }}
            </f-formitem>
            <!-- 备注 -->
            <f-formitem
              v-if="isTechFun() && isOtherType(demandModel.tech_type)"
              label="备注"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              {{ demandModel.tech_type_desc }}
            </f-formitem>
            <!-- 需求说明 -->
            <f-formitem
              label="需求说明"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              <div class="ellipsis break-word" :title="demandModel.demand_desc">
                {{ demandModel.demand_desc }}
              </div>
            </f-formitem>
            <!-- 优先级 -->
            <f-formitem
              label="优先级"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              {{ demandModel.priority.label }}
            </f-formitem>
            <!-- 需求属性 -->
            <f-formitem
              label="需求属性"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              {{ demandModel.demand_property.name }}
            </f-formitem>
            <!-- 是否涉及UI审核 -->
            <f-formitem
              label="是否涉及UI审核"
              bottom-page
              class="col-4"
              v-if="isBusinessFun()"
              label-style="width: 112px; color: #999"
            >
              {{ demandModel.ui_verify.label }}
            </f-formitem>
            <!-- 受理日期 -->
            <f-formitem
              label="受理日期"
              bottom-page
              class="col-4"
              v-if="!isBusinessFun()"
              label-style="width: 112px; color: #999"
            >
              {{ demandModel.accept_date }}
            </f-formitem>
            <!-- 牵头小组 -->
            <f-formitem
              label="牵头小组"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              {{
                demandModel.demand_leader_group &&
                  demandModel.demand_leader_group.name
              }}
            </f-formitem>
            <!-- 牵头人 -->
            <f-formitem
              label="牵头人"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              {{ demandModel.demand_leader | demandLeaders }}
            </f-formitem>
            <!-- 涉及小组 -->
            <f-formitem
              label="涉及小组"
              bottom-page
              class="col-4"
              label-style="width: 112px; color: #999"
            >
              <div class="ellipsis break-word" :title="relatedParts">
                {{ relatedParts }}
              </div>
            </f-formitem>
            <div>
              <f-formitem
                bottom-page
                class="col-4"
                v-for="(item, index) in greatKey"
                :key="index"
                :label="`${item.group.name}评估人员`"
                label-style="width: 112px; color: #999"
              >
                {{ item | relatedUsers }}
              </f-formitem>
            </div>
          </div>
          <fdev-stepper-navigation>
            <div class="row justify-center ">
              <fdev-btn
                class="mr-32"
                label="上一步"
                outline
                @click="handleStepPrevious"
              />
              <fdev-btn label="确认" @click="conformSubmit" />
            </div>
          </fdev-stepper-navigation>
        </fdev-step>
      </fdev-stepper>
    </Loading>
  </f-block>
</template>
<script>
import {
  createDemandModel,
  demandList,
  demandAvailables,
  demandAssessWays,
  futureAssess,
  priorities,
  queryUserOptionsParams,
  uiOptions
} from './model';
import {
  formatOption,
  successNotify,
  errorNotify,
  baseUrl,
  exportExcel,
  deepClone
} from '@/utils/utils';
import { mapState, mapGetters, mapActions } from 'vuex';
import axios from 'axios';
import moment from 'moment';
import Loading from '@/components/Loading';
import { queryTechType } from '@/modules/Rqr/services/methods';
import commonTitle from '@/modules/Rqr/Components/commonTitle';
import { formValidate } from '@/modules/Rqr/views/rqrAdd/validate';
export default {
  name: 'rqrAdd',
  components: { Loading, commonTitle },
  mixins: [formValidate],
  data() {
    return {
      step: 1,
      loading: false,
      demandList,
      demandAvailables,
      demandAssessWays,
      futureAssess,
      priorities,
      uiOptions,
      techTypes: [],
      demand: '',
      demandType: '',
      title: '录入基本信息',
      demandModel: createDemandModel(),
      groups: [],
      leaderGroupOptions: [],
      relatedFilterGroup: [],
      users: [],
      userOptions: [],
      userFilterOptions: [],
      greatKey: [],
      fileContent: '',
      focusInputIndex: 0,
      helpTipWords: `需求编号规则应为以下三类:
                UT-WLaR-2020-0001：UT-部门简写-年份-序号 ->
                UT-ZZZZ-YYYY-XXXX
                0001-2020-L-193-000015：0001-年份-L-类别-序号 ->
                0001-YYYY-L-ZZZ-XXXXXX
                后面不能有M001、N001、001之类的序号。`,
      propertyOptions: [
        {
          id: 'advancedResearch',
          name: '预研',
          fullName: '预备研发，通常为探索性、调研性、试错性等科技工作'
        },
        {
          id: 'keyPoint',
          name: '重点',
          fullName: '部门内外部督办类，战略类等开发需求'
        },
        {
          id: 'routine',
          name: '常规',
          fullName: '日常需求'
        }
      ],
      optionsList: []
    };
  },
  watch: {
    step(val) {
      if (val === 1) {
        this.demand = '';
        this.title = '录入基本信息';
      }
    },
    'demandModel.demand_leader_group': {
      handler: function(newValue, oldValue) {
        if (!newValue) return;
        //涉及板块不为空时，更换牵头板块的事件
        if (this.demandModel.relate_part.length !== 0) {
          //如果原先牵头板块为不为空并且是添加在涉及板块中的，那么将原先牵头板块从涉及板块中删除
          if (oldValue) {
            const oldRef = this.demandModel.relate_part.findIndex(
              part => part.id === oldValue.id
            );
            if (oldRef > -1) {
              this.demandModel.relate_part.splice(oldRef, 1);
              this.greatKey.splice(oldRef, 1);
            }
            //原先牵头板块不在涉及板块中，进入下一步
          }
          //原先牵头小组为空，进入下一步
          //判断新选择的板块在不在涉及板块中，在则不做处理，不在则将其添加进涉及板块
          const newRef = this.demandModel.relate_part.findIndex(
            part => part.id === newValue.id
          );
          if (newRef === -1) {
            this.demandModel.relate_part.unshift(newValue);
            this.greatKey.unshift({
              group: newValue,
              users: [],
              options: this.userOptionsFilter(newValue.id),
              optionsClone: this.userOptionsFilter(newValue.id)
            });
          } else {
            //删掉，再从前插入...此步是为了让牵头小组出现在第一位
            this.demandModel.relate_part.unshift(
              ...this.demandModel.relate_part.splice(newRef, 1)
            );
            this.greatKey.unshift(...this.greatKey.splice(newRef, 1));
          }
          //涉及板块为空时，更换牵头小组无影响，直接将牵头小组添加进设计板块中
        } else {
          this.demandModel.relate_part.push(newValue);
          this.greatKey.push({
            group: newValue,
            users: [],
            options: this.userOptionsFilter(newValue.id),
            optionsClone: this.userOptionsFilter(newValue.id)
          });
        }
      }
    }
  },
  computed: {
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('demandsForm', {
      newDemandData: 'newDemandData',
      evaluateExcel: 'evaluateExcel',
      oaNOStatus: 'oaNOStatus',
      modelExcel: 'modelExcel',
      demandLabelList: 'demandLabelList'
    }),
    ...mapState('dashboard', ['userList']),
    ...mapGetters('user', ['isLoginUserList']),
    relatedParts: function() {
      if (this.demandModel.relate_part) {
        return this.demandModel.relate_part.map(part => part.name).join(',');
      } else {
        return '';
      }
    },
    helpTip() {
      if (this.demandType === '1') return this.helpTipWords;
      return '';
    }
  },
  filters: {
    demandLeaders(leaders) {
      return leaders.map(leader => leader.user_name_cn).join(',');
    },
    relatedUsers(keys) {
      return keys.users.map(user => user.user_name_cn).join(',');
    }
  },
  methods: {
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup'
    }),
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    ...mapActions('demandsForm', {
      save: 'save',
      importDemandExcel: 'importDemandExcel',
      exportModelExcel: 'exportModelExcel',
      queryByTypes: 'queryByTypes'
    }),
    ...mapActions('dashboard', ['queryUserCoreData']),
    // 业务需求
    isBusinessFun() {
      return this.demandType === '1';
    },
    // 科技需求
    isTechFun() {
      return this.demandType === '0';
    },
    // 日常需求
    isDailyFun() {
      return this.demandType === '2';
    },
    // 根据需求类型区分需求标题、需求名称
    isIpmpSyncNameFun() {
      return this.demandType === '1' ? '需求标题' : '需求名称';
    },
    // 需求第二步名称转换
    demandTypeNameHookFun(demandType) {
      const type = {
        0: '录入科技内部需求',
        1: '录入业务需求',
        2: '录入日常需求'
      };
      return type[demandType];
    },
    // 需求字段数据转换
    demandTypeHookFun(demandType) {
      const type = {
        0: 'tech',
        1: 'business',
        2: 'daily'
      };
      return type[demandType];
    },
    async handleStep1(val) {
      this.demandType = val.value;
      this.title = this.demandTypeNameHookFun(this.demandType);
      this.step = 2;
      this.loading = true;
      await this.initQuery();
    },
    async handleStep2AllTip(val) {
      if (this.formValidateFun(this.demandType)) return;
      //录入涉及板块
      this.demandModel.relate_part_detail = this.greatKey.map(item => {
        return {
          part_id: item.group.id,
          part_name: item.group.name || item.group.lable,
          assess_user_all: item.users.map(user => {
            return {
              id: user.id,
              user_name_cn: user.user_name_cn,
              user_name_en: user.user_name_en
            };
          })
        };
      });
      this.step = 3;
    },
    handleStepPrevious() {
      if (this.step === 2) {
        this.title = '录入基本信息';
        this.demandType = '';
      }
      this.$refs.stepper.previous();
    },
    async initQuery() {
      let params = {
        company_id: queryUserOptionsParams.company_id,
        status: queryUserOptionsParams.status
      };
      Promise.all([
        // 查询小组
        this.queryGroup(),
        // 查用户
        this.queryUserCoreData(params)
      ]).then(() => {
        // 处理小组数据
        //筛选出1 2 3级小组    新增编辑需求时 牵头小组和涉及小组不能选四级小组
        let threeGroups = [];
        threeGroups = this.groupsData.filter(item => {
          return item.level < 4;
        });
        this.groups = formatOption(threeGroups);
        this.leaderGroupOptions = this.groups.slice(0);
        // 处理用户数据
        this.users = this.userList.map(user =>
          formatOption(user, 'user_name_cn')
        );
        this.userOptions = this.users.slice(0);
        this.userFilterOptions = this.userOptions;
        this.loading = false;
      });
    },
    userOptionsFilter(group_id) {
      const myUser = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      return myUser;
    },
    userFilter(val, update, abort) {
      // if (this.demandType === '1') {
      //如果为业务需求，则牵头人下拉为全量行内人员
      this.userOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      // }
      //输入中文名和账号名过滤
      update(() => {
        this.userFilterOptions = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
      //遍历获取小组名
      for (let key in this.userFilterOptions) {
        let userGroup = this.groups.filter(
          item => this.userFilterOptions[key].group_id == item.id
        );
        this.userFilterOptions[key].group =
          userGroup.length && userGroup[0].name;
      }
    },
    relatedGroupFilter(val, update, abort) {
      //涉及板块选择框输入过滤
      update(() => {
        this.relatedFilterGroup = this.groups.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    //牵头板块选择框输入过滤
    leaderGroupFilter(val, update, abort) {
      update(() => {
        this.leaderGroupOptions = this.groups.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    //增加涉及板块，添加节点，动态生成选择框，节点包含板块信息和用来绑定选择人员的users数组
    addRelatedGroups({ index, value }) {
      if (value.id === this.demandModel.demand_leader_group.id) {
        this.greatKey.unshift({
          group: value,
          users: [],
          options: this.userOptionsFilter(value.id),
          optionsClone: this.userOptionsFilter(value.id)
        });
      } else {
        this.greatKey.push({
          group: value,
          users: [],
          options: this.userOptionsFilter(value.id),
          optionsClone: this.userOptionsFilter(value.id)
        });
      }
    },
    removeRelatedGroup(detail) {
      //从涉及板块选择框中删除板块时，将相应的节点删除
      const index = this.greatKey.findIndex(item => {
        return item.group.id === detail.value.id;
      });
      this.greatKey.splice(index, 1);
    },
    async handleExportModelExcel() {
      await this.exportModelExcel();
      exportExcel(this.modelExcel);
    },
    handleSelectFile() {
      this.$refs.fileInput.click();
    },
    async uploadFile(event) {
      const formData = new FormData();
      const fileName = this.$refs.fileInput.files[0].name.split('.')[1];
      const fileSize = this.$refs.fileInput.files[0].size;
      const file = this.$refs.fileInput.files[0];
      if (fileName === 'xlsx' && fileSize > 0 && fileSize <= 5242880) {
        formData.append('file', file);
        const config = {
          headers: {
            'Content-Type': 'multipart/form-data',
            Accept: 'application/json',
            Authorization: this.$q.localStorage.getItem('fdev-vue-admin-jwt')
          }
        };
        axios
          .post(
            `${baseUrl}fdemand/api/demand/importDemandExcel`,
            formData,
            config
          )
          .then(async res => {
            const { code, data } = res.data;
            if (code !== 'AAAAAAA') {
              errorNotify('上传失败，请重试!');
              return;
            }
            this.fileContent = data;
            //对OA收件日期进行转换
            if (typeof this.fileContent.oa_receive_date === 'number') {
              this.fileContent.oa_receive_date = moment(
                this.fileContent.oa_receive_date
              ).format('YYYY-MM-DD');
            } else {
              const date = moment(this.fileContent.oa_receive_date).format(
                'YYYY-MM-DD'
              );
              if (date === 'Invalid date') {
                this.fileContent.oa_receive_date = '';
              } else {
                this.fileContent.oa_receive_date = date;
              }
            }
            //对需求方期望投产日期进行转换
            if (typeof this.fileContent.respect_product_date === 'number') {
              this.fileContent.respect_product_date = moment(
                this.fileContent.respect_product_date
              ).format('YYYY-MM-DD');
            } else {
              const date = moment(this.fileContent.respect_product_date).format(
                'YYYY-MM-DD'
              );
              if (date === 'Invalid date') {
                this.fileContent.respect_product_date = '';
              } else {
                this.fileContent.respect_product_date = date;
              }
            }
            if (this.fileContent.demand_available !== '') {
              this.fileContent.demand_available = this.demandAvailables.find(
                item => item.label === this.fileContent.demand_available
              );
            }
            if (this.fileContent.demand_assess_way !== '') {
              this.fileContent.demand_assess_way = this.demandAssessWays.find(
                item => item.label === this.fileContent.demand_assess_way
              );
            }
            if (this.fileContent.future_assess !== '') {
              this.fileContent.future_assess = this.futureAssess.find(
                item => item.label === this.fileContent.future_assess
              );
            }
            for (let key in this.fileContent) {
              if (Reflect.has(this.demandModel, key)) {
                this.demandModel[key] = this.fileContent[key];
              }
            }
            this.checkOANum();
          })
          .catch(err => {
            errorNotify('上传失败，请重试!');
          });
      } else {
        this.$refs.fileInput.value = null;
        errorNotify('该文件不能被上传或文件大小不被允许');
      }
    },
    onChangeTechType(data) {
      if (this.isOtherType(data)) {
        this.demandModel.tech_type_desc = '';
      }
    },
    isOtherType(key) {
      if (key && key.indexOf('其他') !== -1) {
        return true;
      } else return false;
    },
    //期望投产日期设置在当天后
    dateOptionsFn(date) {
      return date >= moment(new Date()).format('YYYY/MM/DD');
    },
    //当前获取焦点的涉及板块人员选择框，以其所属板块id和输入，过滤选择框选项
    relatedUsersFilter(val, update, abort) {
      //则牵头人下拉为全量行内人员
      this.userOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      update(() => {
        this.greatKey[this.focusInputIndex].options = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
      //遍历获取小组名
      for (let key in this.greatKey[this.focusInputIndex].options) {
        let userGroup = this.groups.filter(
          item =>
            this.greatKey[this.focusInputIndex].options[key].group_id == item.id
        );
        this.greatKey[this.focusInputIndex].options[key].group =
          userGroup.length && userGroup[0].name;
      }
    },
    //全局变量记录获得焦点的下标
    handleInputFocus(item, index) {
      this.focusInputIndex = parseInt(index);
    },
    conformSubmit() {
      //若用户未输入oa联系单编号时;
      if (!this.demandModel.oa_real_no && this.demandType == '1') {
        this.$q
          .dialog({
            title: '温馨提示',
            message: '您未输入OA联系单编号，系统将自动为该需求分配需求编号',
            cancel: true,
            persistent: true
          })
          .onOk(() => {
            this.handleStep3();
          });
      } else {
        //若输入oa联系单编号，则需求编号与oa联系单编号保持相同。
        this.demandModel.oa_contact_no = this.demandModel.oa_real_no;
        this.handleStep3();
      }
    },
    setModel(origin, target, keys) {
      keys.forEach(key => {
        if (Array.isArray(key)) {
          const [prop1, prop2] = key;
          if (origin[prop1]) {
            Reflect.set(target, prop1, origin[prop1][prop2]);
          } else {
            Reflect.set(target, prop1, '');
          }
        } else {
          Reflect.set(target, key, origin[key]);
        }
      });
    },
    async handleStep3() {
      this.loading = true;
      let params = this.concatParamsFun();
      await this.save(params);
      this.loading = false;
      successNotify(this.successTipFun());
      this.$router.push('/rqrmn/list');
    },
    // 不同需求类型参数组装
    concatParamsFun() {
      let label = [];
      this.optionsList.map(item => {
        if (item.flag) {
          label.push(item.code);
        }
      });
      const addDemandModel = {
        demand_type: this.demandTypeHookFun(this.demandType), // 需求类型
        demand_leader_group_cn: this.demandModel.demand_leader_group.name,
        demand_leader_group: this.demandModel.demand_leader_group.id, // 牵头小组id
        priority: this.demandModel.priority.value,
        demand_property: this.demandModel.demand_property
          ? this.demandModel.demand_property.id
          : '', //需求属性
        tech_type: this.demandModel.tech_type, // 科技类型
        tech_type_desc: this.demandModel.tech_type_desc, // 备注
        relate_part: this.demandModel.relate_part.map(part => part.id), // 涉及小组
        relate_part_detail: this.demandModel.relate_part_detail,
        demand_desc: this.demandModel.demand_desc, // 需求说明
        demand_leader_all: this.demandModel.demand_leader.map(user => {
          return {
            id: user.id,
            user_name_cn: user.user_name_cn,
            user_name_en: user.user_name_en
          };
        }),
        demand_label: label
      };
      if (this.isTechFun()) {
        const keys = ['demand_instruction', 'accept_date'];
        this.setModel(this.demandModel, addDemandModel, keys);
      }
      if (this.isBusinessFun()) {
        const keys = [
          //OA联系单编号和需求编号 是否涉及UI审核
          'oa_contact_no',
          'oa_contact_name',
          ['ui_verify', 'value']
        ];
        this.setModel(this.demandModel, addDemandModel, keys);
      }
      if (this.isDailyFun()) {
        addDemandModel.oa_contact_name = this.demandModel.oa_contact_name;
        addDemandModel.accept_date = this.demandModel.accept_date || '';
      }
      return addDemandModel;
    },
    // 成功提示语
    successTipFun() {
      const tip = {
        '0': `科技需求录入成功！需求编号为${this.newDemandData}`,
        '1': '业务需求录入成功',
        '2': '日常需求录入成功'
      };
      return tip[this.demandType];
    },
    async getDemandLabel() {
      await this.queryByTypes({ types: ['demandLabel'] });
      this.optionsList = deepClone(this.demandLabelList);
      this.optionsList.map(item => {
        this.$set(item, 'flag', false);
      });
    },
    async getTechOps() {
      const response = await queryTechType({});
      if (response && response.length > 0) {
        this.techTypes = [];
        response.forEach(item => {
          this.techTypes.push(item.techType);
        });
      }
    },
    getCurrentDate() {
      let current, Y, M, D;
      current = new Date();
      Y = current.getFullYear();
      M = current.getMonth() + 1;
      D = current.getDate();
      M = M < 10 ? '0' + M : M;
      D = D < 10 ? '0' + D : D;
      return `${Y}-${M}-${D}`;
    }
  },
  created() {
    if (!this.currentUser.role.some(role => role.name === '需求管理员')) {
      errorNotify('当前用户无权限新增需求，只有需求管理员才可执行此操作!');
      this.$router.push('/rqrmn/list');
    }
    this.getTechOps();
    this.getDemandLabel();
    return (this.demandModel.accept_date = this.getCurrentDate());
  }
};
</script>
<style lang="stylus" scoped>
@import '~#/css/variables.styl';
.step1-form {
  max-width: 500px;
  margin: 32px auto 0;
}
.pre-style
  display block
  line-height 1.8em
.form {
  max-width: 820px;
  margin: 0 auto;

  @media screen and (max-width: $sizes.sm) {
    margin-left: -24px;
    margin-right: -24px;
  }
}

.mydiv {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}

.myspan {
  font-size: 16px;
}

.pre-style
  display block
  line-height 1.8em

.mybtn {
  position: absolute;
  top: 50px;
  left: 600px;
}
.relative {
  position: relative;
}
.step1-form
  margin-top 56px
.m-87
  margin-right 87px
.break-word
  word-break break-all
.border
  border 1px solid #BBBBBB
  border-radius: 8px
  padding 28px
  margin-top 8px
.ml-20
  margin-left 20px
.mt-32
  margin-top 32px
.mr-32
  margin-right 32px
.mb-32
  margin-bottom 32px
.q-stepper__nav
  margin 32px 0 16px 0
  padding-top 0
>>> .q-stepper__step-inner
  padding 0
>>> .q-stepper__header--alternative-labels .q-stepper__dot
  width 32px
  height 32px
.ml-20
  margin-left 20px
.mb-12
  margin-bottom 12px
.labelSty
  color #fff;
  padding 2px 5px;
  border-radius 3px;
</style>
