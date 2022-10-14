<template>
  <div>
    <div class="row ml20">
      <div class="col">
        <f-formitem label="任务名称" label-style="width:112px" required>
          <fdev-input
            ref="jobModel.name"
            v-model="$v.jobModel.name.$model"
            type="text"
            autofocus
            @blur="judgeTaskName()"
            :rules="[
              () => $v.jobModel.name.required || '请输入任务名称',
              () => $v.jobModel.name.judge || '任务名已存在'
            ]"
          />
        </f-formitem>
        <f-formitem label="实施模式" label-style="width:112px" required>
          <div align="left">
            <fdev-select
              ref="jobModel.jobType"
              v-model="$v.jobModel.jobType.$model"
              :options="jobTypes"
              option-label="label"
              option-value="value"
              map-options
              emit-value
              :rules="[() => $v.jobModel.jobType.required || '请选择实施模式']"
            />
          </div>
        </f-formitem>
        <f-formitem
          label="JIRA用户故事ID"
          label-style="width:112px"
          required
          v-if="jobModel.jobType === '0'"
        >
          <fdev-input
            ref="jobModel.jiraId"
            v-model="$v.jobModel.jiraId.$model"
            @blur="judgeStory"
            :rules="[
              () => $v.jobModel.jiraId.isRequired || '请输入用户故事ID',
              () => $v.jobModel.jiraId.isLegal || '请输入正确的用户故事ID'
            ]"
          />
        </f-formitem>
        <f-formitem label="小组" label-style="width:112px" required>
          <fdev-select
            use-input
            ref="jobModel.group"
            v-model="$v.jobModel.group.$model"
            :options="filterGroup"
            option-label="name"
            @filter="groupInputFilter"
            :rules="[() => $v.jobModel.group.required || '请选择小组']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.fullName">
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="研发单元编号" label-style="width:112px">
          <fdev-select
            use-input
            readonly
            ref="jobModel.fdev_implement_unit_no"
            v-model="$v.jobModel.fdev_implement_unit_no.$model"
            option-label="rqrmntNum"
            option-value="_id"
            hint=""
            :rules="[
              () =>
                $v.jobModel.fdev_implement_unit_no.required ||
                '研发单元编号不能为空'
            ]"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="需求名称" label-style="width:112px">
          <fdev-input readonly v-model="jobModel.oa_contact_name" hint="">
          </fdev-input>
        </f-formitem>

        <f-formitem label="任务负责人" required label-style="width:112px">
          <fdev-select
            use-input
            multiple
            ref="jobModel.partyB"
            v-model="$v.jobModel.partyB.$model"
            :options="userOptionsFilter('厂商项目负责人', '行内项目负责人')"
            @filter="userFilter"
            :rules="[() => $v.jobModel.partyB.required || '请选择任务负责人']"
            option-label="user_name_cn"
            option-value="id"
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
        <f-formitem label="行内项目负责人" required label-style="width:112px">
          <fdev-select
            use-input
            multiple
            ref="jobModel.partyA"
            v-model="$v.jobModel.partyA.$model"
            :options="userOptionsFilter('行内项目负责人')"
            @filter="userFilter"
            :rules="[
              () => $v.jobModel.partyA.required || '请选择行内项目负责人'
            ]"
            option-label="user_name_cn"
            option-value="id"
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
        <f-formitem label="开发人员" label-style="width:112px">
          <fdev-select
            use-input
            multiple
            ref="jobModel.developer"
            v-model="jobModel.developer"
            :options="userOptionsFilter('开发人员')"
            @filter="userFilter"
            hint=""
            option-label="user_name_cn"
            option-value="id"
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

        <f-formitem label="开发方向" label-style="width:112px">
          <fdev-select
            ref="jobModel.direction"
            v-model="jobModel.direction"
            :options="directionOptions"
            clearable
            option-value="value"
            option-label="label"
            map-options
            emit-value
            :rules="[() => true]"
          />
        </f-formitem>

        <f-formitem
          label="任务难度描述"
          label-style="width:112px"
          required
          v-if="jobModel.direction"
          :rules="[() => true]"
        >
          <fdev-select
            ref="jobModel.difficulty"
            v-model="$v.jobModel.difficulty.$model"
            :options="difficultyOptions"
            option-value="value"
            option-label="label"
            map-options
            emit-value
            :rules="[
              () => $v.jobModel.difficulty.required || '请选择任务难度描述'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section :title="scope.opt.label">
                  {{ scope.opt.label }}
                </fdev-item-section>
                <fdev-item-section side>
                  <fdev-rating
                    :value="scope.opt.value"
                    size="1em"
                    :max="scope.opt.value"
                    color="yellow-8"
                    no-reset
                    icon="star_border"
                    readonly
                    icon-selected="star"
                  />
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem
          label="任务难度"
          label-style="width:112px"
          v-if="jobModel.direction"
          class="q-mb-md"
        >
          <fdev-rating
            :value="$v.jobModel.difficulty.$model"
            size="2em"
            :max="6"
            color="yellow-8"
            no-reset
            icon="star_border"
            readonly
            icon-selected="star"
          />
        </f-formitem>
      </div>
      <div class="col">
        <f-formitem label="计划启动日期" required label-style="width:112px">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            ref="jobModel.devStartDate"
            v-model="$v.jobModel.devStartDate.$model"
            :rules="[
              () => $v.jobModel.devStartDate.required || '计划启动日期不能为空'
            ]"
            :options="devStartDateOptions"
          />
        </f-formitem>

        <f-formitem label="计划提交内测日期" required label-style="width:112px">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            ref="jobModel.sitStartDate"
            v-model="$v.jobModel.sitStartDate.$model"
            :rules="[
              () =>
                $v.jobModel.sitStartDate.required || '计划提交内测日期不能为空'
            ]"
            :options="sitStartDateOptions"
          />
        </f-formitem>

        <f-formitem
          label="计划提交用户测试日期"
          required
          label-style="width:112px"
        >
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            ref="jobModel.uatStartDate"
            v-model="$v.jobModel.uatStartDate.$model"
            :rules="[
              () =>
                $v.jobModel.uatStartDate.required ||
                '计划提交用户测试日期不能为空'
            ]"
            :options="uatStartDateOptions"
          />
        </f-formitem>

        <f-formitem
          label="计划用户测试完成日期"
          required
          label-style="width:112px"
        >
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            v-model="$v.jobModel.relStartDate.$model"
            ref="jobModel.relStartDate"
            :rules="[
              () =>
                $v.jobModel.relStartDate.required ||
                '计划用户测试完成日期不能为空'
            ]"
            :options="relStartDateOptions"
          />
        </f-formitem>

        <f-formitem label="计划投产日期" required label-style="width:112px">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            ref="jobModel.productionDate"
            v-model="$v.jobModel.productionDate.$model"
            :rules="[
              () =>
                $v.jobModel.productionDate.required || '计划投产日期不能为空'
            ]"
            :options="productionDateOptions"
          />
        </f-formitem>
        <f-formitem
          label="投产意向窗口"
          class="row-mb"
          label-style="width:112px"
        >
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            ref="jobModel.proWantWindow"
            v-model="jobModel.proWantWindow"
            :options="proWantWindowOptions"
            :rules="[() => true]"
          />
        </f-formitem>
        <f-formitem label="任务描述" label-style="width:112px">
          <fdev-input
            ref="jobModel.desc"
            v-model="jobModel.desc"
            type="textarea"
            hint=""
          />
        </f-formitem>
      </div>
    </div>
    <!-- 创建分支 -->
    <div class="row boxStyle">
      <div class="row items-center line-title">
        <f-icon name="branch_s_f" class="titleimg q-ml-md"></f-icon>
        <span class="titlename">创建分支</span>
      </div>
    </div>
    <div class="ml20">
      <f-formitem
        label="应用类型"
        :label-col="4"
        label-style="width:112px"
        value-style="width:70%;"
      >
        <fdev-radio
          class="mr60"
          v-model="jobModel.type"
          val="application"
          label="应用"
        />
        <fdev-radio
          class="mr60"
          v-model="jobModel.type"
          val="components"
          label="组件"
        />
        <fdev-radio
          class="mr60"
          v-model="jobModel.type"
          val="archetype"
          label="骨架"
        />
        <fdev-radio v-model="jobModel.type" val="image" label="镜像" />
      </f-formitem>
    </div>
    <div class="row ml20 mt20">
      <div class="col-6">
        <f-formitem
          required
          label="镜像名称"
          :label-col="4"
          label-style="width:112px"
          v-show="jobModel.type === 'image'"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="$v.jobModel.application.$model"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_cn"
            option-value="name"
            :rules="[
              val => $v.jobModel.application.required || '请选择镜像名称'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name_cn">{{
                    scope.opt.name_cn
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          required
          v-show="jobModel.type === 'application'"
          label="应用名称"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="$v.jobModel.application.$model"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_zh"
            option-value="name_en"
            :rules="[
              val => $v.jobModel.application.required || '请选择应用名称'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name_en">{{
                    scope.opt.name_en
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.name_zh">{{
                    scope.opt.name_zh
                  }}</fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          required
          v-show="jobModel.type === 'components'"
          label="组件名称"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="$v.jobModel.application.$model"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_cn"
            option-value="name_en"
            :rules="[
              val => $v.jobModel.application.required || '请选择组件名称'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name_en">{{
                    scope.opt.name_en
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.name_cn">{{
                    scope.opt.name_cn
                  }}</fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          required
          v-show="jobModel.type === 'archetype'"
          label="骨架名称"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="$v.jobModel.application.$model"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_cn"
            option-value="name_en"
            :rules="[
              val => $v.jobModel.application.required || '请选择骨架名称'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name_en">{{
                    scope.opt.name_en
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.name_cn">{{
                    scope.opt.name_cn
                  }}</fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
      </div>
      <!-- 第二列版本号 -->
      <div
        v-if="jobModel.type === 'components' && jobModel.application"
        class="col-6"
      >
        <f-formitem
          required
          label="预设版本号"
          :label-col="4"
          help="第一段不限制长度，第二、三段最多两位"
          label-style="width:112px"
        >
          <fdev-input
            ref="jobModel.versionNum"
            v-model="jobModel.versionNum"
            :rules="[
              val => $v.jobModel.versionNum.required || '请输入预设版本号',
              () =>
                $v.jobModel.versionNum.examine ||
                '请输入正确的格式:*.x.x, 只能输入数字并不能以0开头'
            ]"
            type="text"
          />
        </f-formitem>
      </div>
      <div
        v-if="
          jobModel.type === 'archetype' &&
            jobModel.application &&
            jobModel.application.archetype_type === 'back'
        "
        class="col-6"
      >
        <f-formitem
          required
          label="预设版本号"
          :label-col="4"
          help="第一段不限制长度，第二、三段最多两位"
          label-style="width:112px"
        >
          <fdev-input
            ref="jobModel.versionNum"
            v-model="jobModel.versionNum"
            type="text"
            :rules="[
              val => $v.jobModel.versionNum.required || '请输入预设版本号',
              () =>
                $v.jobModel.versionNum.examine ||
                '请输入正确的格式:*.x.x, 只能输入数字并不能以0开头'
            ]"
          />
        </f-formitem>
      </div>
    </div>
    <!-- 分支 -->
    <div class="ml20">
      <div>
        <f-formitem
          label="分支类型"
          :label-col="4"
          label-style="width:112px"
          value-style="width:70%;"
        >
          <fdev-radio
            class="mr60"
            v-model="jobModel.featureType"
            val="1"
            label="新增"
          />
          <fdev-radio
            class="mr60"
            v-model="jobModel.featureType"
            val="2"
            label="关联"
          />
        </f-formitem>
      </div>
      <div class="row mt20">
        <f-formitem
          required
          label="feature分支"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-input
            ref="jobModel.feature"
            v-model="$v.jobModel.feature.$model"
            v-if="jobModel.type === 'image' && jobModel.featureType == '1'"
            :prefix="branchStart"
            type="text"
            :rules="[
              val => $v.jobModel.feature.required || '请输入feature分支',
              val => $v.jobModel.feature.noWord || '不能输入中文/中文字符'
            ]"
          />
          <fdev-input
            ref="jobModel.feature"
            v-model="$v.jobModel.feature.$model"
            v-if="
              (jobModel.type === 'archetype' ||
                jobModel.type === 'components' ||
                jobModel.type === 'application') &&
                jobModel.featureType == '1'
            "
            type="text"
            :rules="[
              val => $v.jobModel.feature.required || '请输入feature分支',
              val => $v.jobModel.feature.noWord || '不能输入中文/中文字符'
            ]"
          />
          <fdev-select
            v-if="jobModel.featureType == '2'"
            use-input
            ref="jobModel.feature"
            v-model="$v.jobModel.feature.$model"
            :options="branchOptions"
            @filter="branchFilter"
            :rules="[
              () => $v.jobModel.feature.required || '请选择分支名',
              () => $v.jobModel.feature.noWord || '不能输入中文/中文字符'
            ]"
          />
        </f-formitem>
      </div>
    </div>
    <!-- 投产信息-->
    <div class="row boxStyle">
      <div class="row items-center line-title">
        <f-icon name="bell_s_f" class="titleimg q-ml-md"></f-icon>
        <span class="titlename">投产信息</span>
      </div>
    </div>
    <div class="row ml20">
      <div class="col">
        <f-formitem
          label="是否涉及数据库变更"
          label-style="width:112px"
          class="q-mb-md"
        >
          <fdev-radio
            val="否"
            v-model="jobModel.review.db"
            label="否"
            class="mr60"
          />
          <fdev-radio val="是" v-model="jobModel.review.db" label="是" />
        </f-formitem>
        <f-formitem
          label="相关库表变更是否涉及通知数据仓库等关联供数系统配套改造"
          label-style="width:200px"
          class="q-mb-lg field-height"
          v-if="isShowFirst"
        >
          <fdev-radio
            val="否"
            v-model="jobModel.system_remould"
            label="否"
            class="mr60"
          />
          <fdev-radio val="是" v-model="jobModel.system_remould" label="是" />
        </f-formitem>
        <f-formitem
          label="是否涉及在库表关联应用暂停服务期间实施数据库变更操作"
          label-style="width:200px"
          class="q-mb-md field-height"
          v-if="isShowFirst"
        >
          <fdev-radio
            val="否"
            v-model="jobModel.impl_data"
            label="否"
            class="mr60"
          />
          <fdev-radio val="是" v-model="jobModel.impl_data" label="是" />
        </f-formitem>
        <f-formitem
          label="涉及其它系统变更"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-input
            use-input
            multiple
            hide-dropdown-icon
            v-model="jobModel.review.other_system"
            @new-value="addReviewTerm"
            ref="jobModel.review.other_system"
            hint=""
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="是否涉及特殊情况"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-select
            use-input
            multiple
            option-label="label"
            option-value="value"
            emit-value
            map-options
            ref="jobModel.review.specialCase"
            v-model="jobModel.review.specialCase"
            :options="specialCaseOptions"
          />
        </f-formitem>
      </div>
      <div class="col">
        <f-formitem
          label="公共配置文件更新"
          :label-col="4"
          class="q-mb-md"
          label-style="width:112px"
        >
          <fdev-radio
            :val="false"
            v-model="jobModel.review.commonProfile"
            label="否"
            class="mr60"
          />
          <fdev-radio
            :val="true"
            v-model="jobModel.review.commonProfile"
            label="是"
          />
        </f-formitem>

        <f-formitem label="是否涉及安全测试" required label-style="width:112px">
          <fdev-select
            use-input
            :options="securityTestOptions"
            ref="jobModel.review.securityTest"
            v-model="$v.jobModel.review.securityTest.$model"
            :rules="[
              () =>
                $v.jobModel.review.securityTest.required ||
                '请选择是否涉及安全测试'
            ]"
          >
          </fdev-select>
        </f-formitem>
      </div>
    </div>
  </div>
</template>

<script>
import {
  DemandType,
  tipContent,
  directionOptions,
  specialCaseOptions,
  securityTestOptions,
  difficultyOptions
} from '../utils/constants';
import {
  validate,
  deepClone,
  resolveResponseError,
  formatOption
} from '@/utils/utils';
import { mapState, mapActions, mapMutations } from 'vuex';
import moment from 'moment';
import { required, minLength } from 'vuelidate/lib/validators';
import { checkComponentV, checkArchetypeV } from '@/services/job';
export default {
  props: {
    jobModel: {
      default: () => {},
      type: Object
    },
    allGroups: {
      default: () => [],
      type: Array
    },
    allUsers: {
      default: () => [],
      type: Array
    }
  },
  data() {
    return {
      users: this.allUsers,
      branchOptions: [],
      //jobModel: this.devJob,
      usersList: [],
      filterGroup: [], // 小组相关
      jobTypes: [{ label: '敏捷', value: '0' }, { label: '稳态', value: '1' }],
      userOptions: [],
      judgeResultTip: true,
      isExistJiraStoryTip: true,
      groups: this.allGroups,
      directionOptions: directionOptions,
      securityTestOptions: securityTestOptions,
      specialCaseOptions: specialCaseOptions,
      initJobObj: {},
      applications: [],
      applicationOptions: [],
      branchStart: 'dev-',
      componentFirst: true,
      archetFist: true,
      imageFist: true
    };
  },
  validations: {
    jobModel: {
      name: {
        required,
        judge(val) {
          if (!val) return true;
          return this.judgeResultTip;
        }
      },
      partyA: {
        required,
        minLength: minLength(1)
      },
      partyB: {
        required,
        minLength: minLength(1)
      },
      group: {
        required
      },
      fdev_implement_unit_no: {
        required
      },
      devStartDate: {
        required
      },
      sitStartDate: {
        required
      },
      uatStartDate: {
        required
      },
      uatEndDate: {
        required
      },
      relStartDate: {
        required
      },
      productionDate: {
        required
      },
      direction: {},
      difficulty: {
        required(val) {
          if (!this.jobModel.direction) return true;
          return !!val;
        }
      },
      jobType: {
        required
      },
      jiraId: {
        isRequired(val) {
          if (this.jobModel.jobType === '0') {
            return Boolean(val);
          }
          return true;
        },
        isLegal() {
          if (this.jobModel.jobType === '0') {
            return this.isExistJiraStoryTip;
          } else {
            return true;
          }
        }
      },
      review: {
        securityTest: {
          required
        }
      },
      application: {
        required
      },
      versionNum: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^(?!0)(\d{1,})\.((?!0)\d{2}|\d{1})\.((?!0)\d{2}|\d{1})$/;
          return reg.test(val);
        }
      },
      feature: {
        required,
        noWord(val) {
          if (!val) {
            return true;
          }
          let reg = new RegExp(
            /[\u4e00-\u9fa5]|[\\，\\？\\；\\《\\》\\。\\（\\）\\；\\”\\“\\！\\‘\\’\\、\\【\\】\\－]/gm
          );
          return !reg.test(val);
        }
      }
      // versionNum: {
      //   onlyNum(val) {
      //     if (!val) {
      //       return true;
      //     }
      //     let reg = /^(\d+)(\.\d+)*/;
      //     return reg.test(val * 1);
      //   }
      // }
    }
  },
  watch: {
    'jobModel.productionDate': {
      deep: true,
      handler(val) {
        this.jobModel.proWantWindow = val;
      }
    },
    'jobModel.type': {
      deep: true,
      handler(val) {
        this.queryApplicatioin(val);
      }
    },
    'jobModel.featureType': {
      deep: true,
      handler(val) {
        this.jobModel.feature = '';
        if (val == '2' && this.jobModel.application) {
          this.getBranchList(this.jobModel.application);
        }
      }
    },
    'jobModel.application': {
      deep: true,
      handler: function(val) {
        if (val && this.jobModel.featureType == '2') {
          //送应用类型和Gitlabid获取分支列表
          this.getBranchList(val);
        } else {
          this.saveBranchList([]);
        }
      }
    }
  },
  computed: {
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('componentForm', ['imageManageList']),
    ...mapState('appForm', {
      appList: 'vueAppData'
    }),
    ...mapState('jobForm', [
      'branchList',
      'judgeResult',
      'isExistJiraStory',
      'componentList',
      'archetList'
    ]),
    isShowFirst() {
      return this.jobModel.review.db === '是';
    },
    difficulty() {
      return difficultyOptions[this.jobModel.direction];
    },
    difficultyOptions() {
      return this.difficulty.map((item, i) => {
        return { label: item, value: i + 1 };
      });
    },
    isBusiness() {
      return this.jobModel.demand_type === DemandType.Business;
    },
    tip() {
      if (this.isBusiness) {
        return tipContent.BusinessTip;
      } else {
        return tipContent.TechnologyTip;
      }
    }
  },
  methods: {
    ...mapActions('appForm', ['queryApps']),
    ...mapActions('componentForm', ['queryBaseImage']),
    ...mapActions('appForm', ['queryApps']),
    ...mapActions('jobForm', [
      'queryProjectBranchList',
      'taskNameJudge',
      'queryJiraStoryByKey',
      'queryAllComponents',
      'queryAllArchetypeTypes'
    ]),
    ...mapMutations('jobForm', {
      saveBranchList: 'saveBranchList'
    }),
    getBranchList(val) {
      let gitlabId = '';
      if (this.jobModel.type == 'application') {
        gitlabId = val.gitlab_project_id;
      } else {
        gitlabId = val.gitlab_id;
      }
      this.queryProjectBranchList({
        gitlabProjectId: gitlabId + '',
        applicationType: this.jobModel.type
      });
      this.branchOptions = this.branchList.slice(0);
      this.jobModel.feature = '';
    },
    //分支过滤
    branchFilter(val, update, abort) {
      update(() => {
        this.branchOptions = this.branchList.filter(
          branchName => branchName.indexOf(val) > -1
        );
      });
    },
    /* 判断任务名是否重名，重名状态为false*/
    async judgeTaskName(val) {
      if (!this.jobModel.name.trim()) return;
      await this.taskNameJudge({
        taskName: this.jobModel.name
      });
      this.judgeResultTip = this.judgeResult;
      this.$v.jobModel.name.$touch();
      validate([this.$refs['jobModel.name']]);
    },
    /* 判断用户故事id*/
    async judgeStory(val) {
      if (!this.jobModel.jiraId.trim()) return;
      await this.queryJiraStoryByKey({
        storyId: this.jobModel.jiraId
      });
      this.isExistJiraStoryTip = this.isExistJiraStory.result;
      this.$v.jobModel.jiraId.$touch();
      validate([this.$refs['jobModel.jiraId']]);
    },
    async applicationFilter(val, update, abort) {
      if (this.jobModel.type === 'application') {
        update(() => {
          this.applications = this.applicationOptions.filter(
            application =>
              application.name_en.toLowerCase().includes(val.toLowerCase()) ||
              application.name_zh.includes(val)
          );
        });
      } else if (this.jobModel.type === 'image') {
        update(() => {
          this.applications = this.applicationOptions.filter(
            application =>
              application.name.toLowerCase().includes(val.toLowerCase()) ||
              application.name_cn.includes(val)
          );
        });
      } else if (this.jobModel.type === 'components') {
        update(() => {
          this.applications = this.applicationOptions.filter(
            application =>
              application.name_en.toLowerCase().includes(val.toLowerCase()) ||
              application.name_cn.includes(val)
          );
        });
      } else if (this.jobModel.type === 'archetype') {
        update(() => {
          this.applications = this.applicationOptions.filter(
            application =>
              application.name_en.toLowerCase().includes(val.toLowerCase()) ||
              application.name_cn.includes(val)
          );
        });
      }
      if (!this.jobModel.applications) {
        this.saveBranchList([]);
      }
    },
    async userFilter(val, update, abort) {
      update(() => {
        this.users = this.userOptions.filter(
          user =>
            user.user_name_cn.includes(val) ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    userOptionsFilter(...param) {
      let myuser = this.users.filter(item => {
        let flag = false;
        let role_labels = [];
        item.role.forEach(ele => {
          role_labels.push(ele);
        });
        param.forEach(r => {
          if (role_labels.includes(r)) {
            flag = true;
          }
        });
        return flag;
      });
      return myuser;
    },
    groupInputFilter(val, update) {
      update(() => {
        this.filterGroup = this.groups.filter(tag =>
          tag.fullName.includes(val)
        );
      });
    },
    formatDate(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYY/MM/DD');
    },
    devStartDateOptions(date) {
      let afterDate = this.formatDate(this.initJobObj.devStartDate);
      return date <= afterDate;
    },
    sitStartDateOptions(date) {
      let beforeDate = this.jobModel.devStartDate;
      let afterDate = this.formatDate(
        this.initJobObj.sitStartDate ||
          this.jobModel.uatStartDate ||
          this.jobModel.relStartDate ||
          this.jobModel.productionDate ||
          this.jobModel.proWantWindow
      );
      if (afterDate && beforeDate) {
        return date <= afterDate && date >= beforeDate;
      } else if (beforeDate) {
        return date >= beforeDate;
      } else {
        return date <= afterDate;
      }
    },
    uatStartDateOptions(date) {
      let beforeDate = this.jobModel.sitStartDate || this.jobModel.devStartDate;
      let afterDate = this.formatDate(
        this.initJobObj.uatStartDate ||
          this.jobModel.relStartDate ||
          this.jobModel.productionDate ||
          this.jobModel.proWantWindow
      );
      if (afterDate && beforeDate) {
        return date <= afterDate && date >= beforeDate;
      } else if (beforeDate) {
        return date >= beforeDate;
      } else {
        return date <= afterDate;
      }
    },
    relStartDateOptions(date) {
      let beforeDate = this.jobModel.uatStartDate || this.jobModel.sitStartDate;
      let afterDate = this.formatDate(
        this.initJobObj.relStartDate ||
          this.jobModel.productionDate ||
          this.jobModel.proWantWindow
      );
      if (afterDate && beforeDate) {
        return date <= afterDate && date >= beforeDate;
      } else if (beforeDate) {
        return date >= beforeDate;
      } else {
        return date <= afterDate;
      }
    },
    productionDateOptions(date) {
      let beforeDate =
        this.jobModel.relStartDate ||
        this.jobModel.uatStartDate ||
        this.jobModel.sitStartDate;
      let afterDate = this.formatDate(
        this.initJobObj.productionDate || this.jobModel.proWantWindow
      );
      if (afterDate && beforeDate) {
        return date <= afterDate && date >= beforeDate;
      } else if (beforeDate) {
        return date >= beforeDate;
      } else {
        return date <= afterDate;
      }
    },
    proWantWindowOptions(date) {
      const beforeDate =
        this.jobModel.productionDate ||
        this.jobModel.relStartDate ||
        this.jobModel.uatStartDate ||
        this.jobModel.sitStartDate ||
        this.jobModel.devStartDate;
      const afterDate = this.formatDate(this.initJobObj.proWantWindow);
      if (afterDate && beforeDate) {
        return date <= afterDate && date >= beforeDate;
      } else if (beforeDate) {
        return date >= beforeDate;
      } else {
        return date <= afterDate;
      }
    },
    addReviewTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    validateJob() {
      this.$v.jobModel.$touch();
      let branchKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('jobModel') > -1
      );
      validate(branchKeys.map(key => this.$refs[key]));
      if (this.$v.jobModel.$invalid) {
        return;
      }
    },
    queryApplicatioin(val) {
      this.jobModel.application = null;
      this.jobModel.versionNum = '';
      switch (val) {
        case 'application':
          this.applications = formatOption(this.appList, 'name_en');
          this.applicationOptions = this.applications.slice(0);
          break;
        case 'components':
          if (this.componentFirst) {
            this.componentFirst = false;
            this.queryComponent();
          } else {
            this.applications = formatOption(this.componentList, 'name_en');
            this.applicationOptions = this.applications.slice(0);
          }
          break;
        case 'archetype':
          if (this.archetFist) {
            this.archetFist = false;
            this.queryArchet();
          } else {
            this.applications = formatOption(this.archetList, 'name_en');
            this.applicationOptions = this.applications.slice(0);
          }
          break;
        case 'image':
          if (this.imageFist) {
            this.imageFist = false;
            this.queryImage();
          } else {
            this.applications = formatOption(this.imageManageList, 'name_en');
            this.applicationOptions = this.applications.slice(0);
          }
          break;
        default:
          break;
      }
    },
    async queryAPP() {
      await this.queryApps();
      this.$nextTick();
      this.applications = formatOption(this.appList, 'name_en');
      this.applicationOptions = this.applications.slice(0);
    },
    async queryComponent() {
      await this.queryAllComponents();
      this.$nextTick();
      this.applications = formatOption(this.componentList, 'name_en');
      this.applicationOptions = this.applications.slice(0);
    },
    async queryArchet() {
      await this.queryAllArchetypeTypes();
      this.$nextTick();
      this.applications = formatOption(this.archetList, 'name_en');
      this.applicationOptions = this.applications.slice(0);
    },
    async queryImage() {
      await this.queryBaseImage();
      this.$nextTick();
      this.applications = formatOption(this.imageManageList, 'name_en');
      this.applicationOptions = this.applications.slice(0);
    },
    //校验骨架版本号
    async checkArchetypeVersion() {
      if (!this.jobModel.versionNum) return;
      let params = {
        archetype_id: this.jobModel.application.id,
        target_version: this.jobModel.versionNum
      };
      await resolveResponseError(() => checkArchetypeV(params));
    },
    //校验组件版本号
    async checkComponentVersion() {
      if (!this.jobModel.versionNum) {
        return;
      }
      let params = {
        type: this.jobModel.application.component_type,
        component_id: this.jobModel.application.id,
        target_version: this.jobModel.versionNum
      };
      await resolveResponseError(() => checkComponentV(params));
    }
  },
  async created() {
    this.userOptions = this.users.slice(0);
    this.filterGroup = this.groups.slice(0);
    this.initJobObj = deepClone(this.jobModel);
    this.queryAPP();
  }
};
</script>

<style lang="stylus" scoped>
>>> .titleimg
    width: 16px;
    height: 16px;
    color: #0663BE;
    border-radius: 4px;
    margin-top -4px
    margin-right:10px
>>> .titlename
    font-size: 14px;
    color: #333333;
    line-height: 22px;
    font-weight 600
.boxStyle
  height: 54px;
  background: rgba(30,84,213,0.05);
  border-radius: 2px;
  border-radius: 2px;
  margin-bottom: 32px;
  margin-top: 12px;
.ml20
  margin-left: 20px;
.mr60
  margin-right: 60px;
.mt20
  margin-top: 20px;
</style>
