<template>
  <f-dialog
    v-model="updateModalOpened"
    transition-show="slide-up"
    transition-hide="slide-down"
    persistent
    right
    title="修改任务"
    @shake="confirmToClose"
  >
    <Loading :visible="updateLoading">
      <div class="rdia-dc-w row justify-between">
        <f-formitem label="任务名称">
          <fdev-input v-model="jobModel.name" disable type="text" hint=""
        /></f-formitem>
        <f-formitem
          label="所属应用"
          v-if="taskType != 2 || jobModel.project_name"
        >
          <fdev-input
            v-model="jobModel.project_name"
            disable
            type="text"
            hint=""
        /></f-formitem>
        <f-formitem label="所属小组">
          <fdev-select
            ref="jobModel.group"
            v-model="jobModel.group"
            :options="groupList"
            option-value="id"
            option-label="name"
            @filter="groupFilter"
            hint=""
            use-input
            :rules="[() => $v.jobModel.group.required || '请输入所属小组']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.label" caption>
                    {{ scope.opt.label }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template></fdev-select
          >
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="分支">
          <fdev-input
            v-model="jobModel.feature_branch"
            disable
            type="text"
            hint=""
        /></f-formitem>
        <f-formitem label="研发单元">
          <fdev-select
            use-input
            ref="jobModel.fdev_implement_unit_no"
            v-model="jobModel.fdev_implement_unit_no"
            :options="filterRqrmnt"
            option-label="fdev_implement_unit_no"
            option-value="fdev_implement_unit_no"
            @filter="rqrmntInputFilter"
            :disable="taskType == 2 || canNotModify"
            map-options
            emit-value
            :rules="[
              () =>
                $v.jobModel.fdev_implement_unit_no.required || '请选择研发单元'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item
                v-bind="scope.itemProps"
                v-on="scope.itemEvents"
                :disabled="scope.opt.disable"
              >
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.fdev_implement_unit_no">
                    {{ scope.opt.fdev_implement_unit_no }}
                  </fdev-item-label>
                  <fdev-item-label
                    caption
                    :title="scope.opt.implement_unit_content"
                  >
                    {{ scope.opt.implement_unit_content }}
                  </fdev-item-label>
                </fdev-item-section>
                <template v-if="scope.opt.disable">
                  <fdev-tooltip
                    anchor="top middle"
                    self="bottom middle"
                    :offest="[10, 0]"
                  >
                    {{ scope.opt.tooltip }}
                  </fdev-tooltip>
                </template>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <!-- 显示字段修改 -->
        <f-formitem
          v-if="
            taskType == 2 ||
              (job.demand && job.demand.demand_type === 'business')
          "
          label="计划启动日期"
          required
        >
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.plan_start_time"
            :disable="job.demand && job.demand.demand_type !== 'business'"
            hint=""
            ref="jobModel.plan_start_time"
            :rules="[
              () =>
                $v.jobModel.plan_start_time.required || '计划启动日期不能为空'
            ]"
            :options="planStartDateOptions"
          />
        </f-formitem>
        <f-formitem label="实际启动日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.start_time"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem
          v-if="
            taskType != 2 && job.demand && job.demand.demand_type === 'business'
          "
          label="计划提交内测日期"
          required
        >
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.plan_inner_test_time"
            hint=""
            ref="jobModel.plan_inner_test_time"
            :rules="[
              () =>
                $v.jobModel.plan_inner_test_time.required ||
                '计划提交内测日期不能为空'
            ]"
            :options="planTestDateOptions"
          />
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际提交内测日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.start_inner_test_time"
            disable
            hint=""
          />
          <fdev-tooltip anchor="top middle">
            提交内测后生成，用户不可编辑
          </fdev-tooltip>
        </f-formitem>
        <f-formitem
          v-if="
            taskType != 2 && job.demand && job.demand.demand_type === 'business'
          "
          label="计划提交用户测试日期"
          required
        >
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.plan_uat_test_start_time"
            hint=""
            ref="jobModel.plan_uat_test_start_time"
            :rules="[
              () =>
                $v.jobModel.plan_uat_test_start_time.required ||
                '计划提交用户测试日期不能为空'
            ]"
            :options="planUatTestDateOptions"
          />
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际提交用户测试日期">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            :disable="jobModel.stage.code < 1"
            v-model="jobModel.start_uat_test_time"
            :options="uatStartDateOptions"
            :rules="[
              val =>
                !jobProfile.start_uat_test_time ||
                !!val ||
                '实际提交用户测试日期不能为空'
            ]"
          >
          </f-date>
          <fdev-tooltip anchor="top middle" v-if="jobModel.stage.code < 1">
            任务尚未提交内测不可修改,请先提交内测
          </fdev-tooltip>
        </f-formitem>
        <f-formitem
          v-if="
            taskType != 2 && job.demand && job.demand.demand_type === 'business'
          "
          label="计划用户测试完成日期"
          required
        >
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.plan_rel_test_time"
            hint=""
            ref="jobModel.plan_rel_test_time"
            :rules="[
              () =>
                $v.jobModel.plan_rel_test_time.required ||
                '计划用户测试完成日期不能为空'
            ]"
            :options="planRelTestDateOptions"
          />
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际用户测试完成日期">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            v-model="jobModel.start_rel_test_time"
            :disable="jobModel.stage.code < 1"
            :options="relStartDateOptions"
            hint=""
          >
          </f-date>
          <fdev-tooltip anchor="top middle" v-if="jobModel.stage.code < 1">
            任务尚未提交内测不可修改,请先提交内测
          </fdev-tooltip>
        </f-formitem>
        <!-- 有时间必填 -->
        <f-formitem label="上线确认书到达日期">
          <!-- 上线确认书 未打开           不能选择时间
                         打开回显的有时间  时间必填
           -->
          <f-date
            :disable="job.confirmBtn != 1"
            mask="YYYY-MM-DD"
            ref="jobModel.confirmFileDate"
            v-model="jobModel.confirmFileDate"
            :rules="[
              val =>
                !jobProfile.confirmFileDate ||
                !!val ||
                '上线确认书到达日期不能为空'
            ]"
            hint=""
          >
          </f-date>
          <fdev-tooltip anchor="top middle" v-if="job.confirmBtn != 1">
            请打开"上线确认书"开关后再修改
          </fdev-tooltip>
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="实际投产日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.fire_time"
            disable
            hint=""
          />
          <fdev-tooltip anchor="top middle">
            确认投产后生成，用户不可编辑
          </fdev-tooltip>
        </f-formitem>
        <f-formitem
          v-if="
            taskType == 2 ||
              (job.demand && job.demand.demand_type === 'business')
          "
          label="计划完成日期"
          required
        >
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.plan_fire_time"
            :disable="job.demand && job.demand.demand_type !== 'business'"
            hint=""
            ref="jobModel.plan_fire_time"
            :rules="[
              () =>
                $v.jobModel.plan_fire_time.required || '计划完成日期不能为空'
            ]"
            :options="planFireDateOptions"
          />
        </f-formitem>
        <f-formitem label="实际完成日期">
          <f-date
            mask="YYYY/MM/DD"
            v-model="jobModel.fire_time"
            disable
            hint=""
          />
          <fdev-tooltip anchor="top middle">
            确认完成后生成，用户不可编辑
          </fdev-tooltip>
        </f-formitem>
        <f-formitem v-if="taskType != 2" label="投产意向窗口" required>
          <f-date
            mask="YYYY/MM/DD"
            ref="jobModel.proWantWindow"
            v-model="jobModel.proWantWindow"
            :rules="[
              () => $v.jobModel.proWantWindow.required || '投产意向窗口不能为空'
            ]"
          />
        </f-formitem>
        <f-formitem label="任务负责人" required>
          <fdev-select
            use-input
            multiple
            ref="jobModel.partyB"
            v-model="jobModel.partyB"
            :options="
              userOptionsFilter(
                ['厂商项目负责人', '行内项目负责人'],
                job.partyB
              )
            "
            @filter="userFilter"
            :rules="[() => $v.jobModel.partyB.required || '任务负责人不能为空']"
            option-label="user_name_cn"
            option-value="id"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label :title="scope.opt.user_name_en" caption>
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem label="行内项目负责人" required>
          <fdev-select
            use-input
            multiple
            ref="jobModel.partyA"
            v-model="jobModel.partyA"
            :options="userOptionsFilter(['行内项目负责人'], job.partyA)"
            @filter="userFilter"
            :rules="[
              () => $v.jobModel.partyA.required || '行内项目负责人不能为空'
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
                  <fdev-item-label :title="scope.opt.user_name_en" caption>
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem label="开发人员">
          <fdev-select
            use-input
            multiple
            hint=""
            ref="jobModel.developer"
            v-model="jobModel.developer"
            :options="userOptionsFilter(['开发人员'], job.developer)"
            @filter="userFilter"
            option-label="user_name_cn"
            option-value="id"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label :title="scope.opt.user_name_en" caption>
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem
          label="开发方向"
          :optional="!job.direction"
          v-if="isSpdbManager && taskType != 2"
        >
          <fdev-tooltip v-if="job.stage.code > 2" anchor="top middle">
            任务进入rel阶段，不能修改
          </fdev-tooltip>
          <fdev-select
            hint=""
            ref="jobModel.direction"
            v-model="jobModel.direction"
            :options="directionOptions"
            :clearable="!job.direction"
            option-value="value"
            option-label="label"
            map-options
            :disable="job.stage.code > 2"
            emit-value
          />
        </f-formitem>

        <f-formitem
          label="任务难度描述"
          v-if="jobModel.direction && isSpdbManager && taskType != 2"
        >
          <fdev-tooltip v-if="job.stage.code > 2" anchor="top middle">
            任务进入rel阶段，不能修改
          </fdev-tooltip>

          <fdev-select
            :disable="job.stage.code > 2"
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

        <f-formitem label="任务难度" v-if="jobModel.direction && taskType != 2">
          <fdev-rating
            :value="$v.jobModel.difficulty.$model"
            size="2em"
            :max="6"
            color="yellow-8"
            no-reset
            icon="star_border"
            disable
            hint=""
            icon-selected="star"
          />
        </f-formitem>

        <f-formitem
          label="修改原因"
          v-show="difficultyIsChange()"
          v-if="jobModel.direction && taskType != 2"
          required
        >
          <fdev-input
            ref="jobModel.modify_reason"
            v-model="$v.jobModel.modify_reason.$model"
            type="textarea"
            :rules="[
              () =>
                $v.jobModel.modify_reason.required ||
                '请输入任务难度描述的修改原因'
            ]"
          />
        </f-formitem>

        <f-formitem label="任务描述">
          <fdev-input
            ref="jobModel.desc"
            v-model="jobModel.desc"
            type="textarea"
            hint=""
          />
        </f-formitem>

        <f-formitem
          label="是否涉及数据库变更"
          v-if="jobModel.review.db[0] && taskType != 2"
        >
          <div class="text-left padding-form">
            <fdev-radio
              val="否"
              v-model="jobModel.review.db[0].name"
              label="否"
              class="q-pr-lg"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
            <fdev-radio
              val="是"
              v-model="jobModel.review.db[0].name"
              label="是"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
          </div>
        </f-formitem>
        <!-- A -->
        <f-formitem
          label="相关库表变更是否涉及通知数据仓库等关联供数系统配套改造"
          v-if="isShowFirst && taskType != 2"
        >
          <div class="padding-form">
            <fdev-radio
              val="否"
              v-model="jobModel.system_remould"
              label="否"
              class="q-pr-lg"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
            <fdev-radio
              val="是"
              v-model="jobModel.system_remould"
              label="是"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
          </div>
        </f-formitem>
        <!-- C -->
        <f-formitem
          label="是否涉及在库表关联应用暂停服务期间实施数据库变更操作"
          v-if="isShowFirst && taskType != 2"
        >
          <div class="padding-form">
            <fdev-radio
              val="否"
              v-model="jobModel.impl_data"
              label="否"
              class="q-pr-lg"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
            <fdev-radio
              val="是"
              v-model="jobModel.impl_data"
              label="是"
              :disable="
                reviewRecordStatus === '初审中' ||
                  reviewRecordStatus === '复审中'
              "
            />
          </div>
        </f-formitem>

        <f-formitem label="公共配置文件更新" v-if="taskType != 2">
          <div class="padding-form">
            <fdev-radio
              :val="false"
              v-model="jobModel.review.commonProfile"
              label="否"
              class="q-pr-lg"
            />
            <fdev-radio
              :val="true"
              v-model="jobModel.review.commonProfile"
              label="是"
            />
          </div>
        </f-formitem>
        <f-formitem label="其他系统变更" v-if="taskType != 2">
          <fdev-input
            use-input
            multiple
            v-if="
              Array.isArray(jobModel.review.other_system) &&
                jobModel.review.other_system.length > 0
            "
            hint=""
            hide-dropdown-icon
            v-model="jobModel.review.other_system[0].name"
            @new-value="addReviewTerm"
            ref="jobModel.review.other_system"
          />
          <fdev-input
            use-input
            multiple
            v-else
            hint=""
            hide-dropdown-icon
            v-model="jobModel.review.other_system"
            @new-value="addReviewTerm"
            ref="jobModel.review.other_system"
          />
        </f-formitem>
        <f-formitem label="是否涉及特殊情况" v-if="taskType != 2">
          <fdev-select
            multiple
            option-label="label"
            option-value="value"
            map-options
            emit-value
            hint=""
            ref="jobModel.review.specialCase"
            v-model="jobModel.review.specialCase"
            :options="specialCaseOptions"
          />
        </f-formitem>
        <f-formitem label="是否涉及安全测试" v-if="taskType != 2">
          <fdev-select
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
        </f-formitem></div
    ></Loading>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        @click="modifyTask"
        :loading="globalLoading['jobForm/updateJob']"
        label="修改任务"
      />
    </template>
  </f-dialog>
</template>

<script>
import { deepClone, validate, successNotify } from '@/utils/utils';
import { mapState, mapActions, mapGetters } from 'vuex';
import { formatMyJob } from '@/modules/Job/utils/utils';
import {
  createJobModel,
  directionOptions,
  specialCaseOptions,
  securityTestOptions,
  tipContent,
  difficultyOptions
} from '@/modules/Job/utils/constants';
import { required } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';
import moment from 'moment';
export default {
  name: 'updateTaskDetail',
  props: ['id', 'job', 'taskType', 'updateLoading'],
  components: {
    Loading
  },
  data() {
    return {
      users: [],
      userOptions: [],
      canNotModify: false,
      jobModel: createJobModel(),
      updateModalOpened: false,
      ipmpUnitCloneList: [],
      oldDbModel: '',
      filterRqrmnt: [],
      groupList: this.groups,
      directionOptions,
      specialCaseOptions,
      securityTestOptions
    };
  },
  watch: {
    'jobModel.difficulty'(val) {
      this.difficultyIsChange();
      this.jobModel.modify_reason = '';
    },
    'jobModel.direction'(val) {
      this.difficultyIsChange();
      this.jobModel.modify_reason = '';
    }
    // 'jobModel.plan_start_time'(val) {
    //   if (!val) {
    //     this.jobModel.plan_start_time = this.job.plan_start_time;
    //   }
    // },
    // 'jobModel.plan_inner_test_time'(val) {
    //   if (!val) {
    //     this.jobModel.plan_inner_test_time = this.job.plan_inner_test_time;
    //   }
    // },
    // 'jobModel.plan_uat_test_start_time'(val) {
    //   if (!val) {
    //     this.jobModel.plan_uat_test_start_time = this.job.plan_uat_test_start_time;
    //   }
    // },
    // 'jobModel.plan_rel_test_time'(val) {
    //   if (!val) {
    //     this.jobModel.plan_rel_test_time = this.job.plan_rel_test_time;
    //   }
    // },
    // 'jobModel.plan_fire_time'(val) {
    //   if (!val) {
    //     this.jobModel.plan_fire_time = this.job.plan_fire_time;
    //   }
    // }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('demandsForm', ['ipmpUnitListNew', 'fdevNOinfo']),
    ...mapState('jobForm', [
      'jobProfile',
      'reviewRecordStatus',
      'userInfoList' /*查询所有用户信息（包括角色）*/
    ]),
    ...mapState('userForm', ['groups']),
    difficulty() {
      return this.jobModel.direction
        ? difficultyOptions[this.jobModel.direction]
        : [];
    },
    difficultyOptions() {
      return this.difficulty.map((item, i) => {
        return { label: item, value: i + 1 };
      });
    },
    isShowFirst() {
      return this.jobModel.review.db[0]
        ? this.jobModel.review.db[0].name === '是'
        : false;
    },
    tip() {
      if (this.isBusiness) {
        return tipContent.BusinessTip;
      } else {
        return tipContent.TechnologyTip;
      }
    }
  },
  validations: {
    jobModel: {
      proWantWindow: {
        required
      },
      plan_start_time: {
        required
      },
      plan_inner_test_time: {
        required
      },
      plan_uat_test_start_time: {
        required
      },
      plan_rel_test_time: {
        required
      },
      plan_fire_time: {
        required
      },
      partyB: {
        required
      },
      partyA: {
        required
      },
      group: {
        required
      },
      fdev_implement_unit_no: {
        required
      },
      difficulty: {
        required(val) {
          if (!this.job.direction) return true;
          return !!val;
        }
      },
      modify_reason: {
        required(val) {
          if (this.difficultyIsChange && this.job.direction) {
            return val ? !!val.trim() : false;
          }
          return true;
        }
      },
      review: {
        securityTest: {
          required
        }
      }
    }
  },
  methods: {
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('demandsForm', [
      'queryAvailableIpmpUnitNew',
      'queryByFdevNoAndDemandId'
    ]),
    ...mapActions('jobForm', [
      'updateJob',
      'queryReviewRecordStatus',
      'getAllUserAndRole' /*查询所有用户信息（包括角色）*/
    ]),
    // 如果修改开发方向或开发难度展示修改原因
    difficultyIsChange() {
      let result = this.jobModel.direction + this.jobModel.difficulty;
      return this.job.difficulty_desc && result != this.job.difficulty_desc;
    },
    async getUserOptions() {
      await this.getAllUserAndRole({ status: '0' });
      this.users = this.userInfoList.slice(0);
      this.userOptions = this.userInfoList.slice(0);
    },
    //修改任务
    async handUpdateModalOpened() {
      this.updateModalOpened = true;
      await this.getUserOptions();
      //查任务所属研发单元的状态
      await this.queryByFdevNoAndDemandId({
        demand_id: this.job.rqrmnt_no,
        fdev_implement_unit_no: this.job.fdev_implement_unit_no
      });
      this.getCanNotModify();
      //查询任务最新状态\
      this.queryReviewRecordStatus({ id: this.id });
      await this.$nextTick();
      await this.queryAvailableIpmpUnitNew();
      this.jobModel = deepClone(formatMyJob(this.job, this.users));
      this.jobModel.review.commonProfile = this.jobModel.review.commonProfile
        ? this.jobModel.review.commonProfile
        : false;
      this.jobModel.review.securityTest = this.jobModel.review.securityTest
        ? this.jobModel.review.securityTest
        : '不涉及';
      this.jobModel.review.specialCase = this.jobModel.review.specialCase
        ? this.jobModel.review.specialCase
        : ['不涉及'];
      this.jobModel.fdev_implement_unit_no = this.jobModel.fdev_implement_unit_no;
      //开发原因、难度描述
      if (this.jobModel.difficulty_desc) {
        const [direction, difficulty] = this.jobModel.difficulty_desc.split('');
        this.jobModel.direction = direction;
        this.jobModel.difficulty = Number(difficulty);
      }
      this.ipmpUnitCloneList =
        this.ipmpUnitListNew.length > 0 ? deepClone(this.ipmpUnitListNew) : [];
      this.ipmpUnitCloneList.map(val => {
        if (val.implement_unit_status_normal === 1) {
          this.$set(val, 'disable', true);
          this.$set(
            val,
            'tooltip',
            '该研发单元状态为评估中，无法关联，请联系需求管理员/牵头人完成评估'
          );
        } else if (val.implement_unit_status_normal === 8) {
          this.$set(val, 'disable', true);
          this.$set(val, 'tooltip', '该研发单元已归档');
        } else if (val.implement_unit_status_normal === 9) {
          this.$set(val, 'disable', true);
          this.$set(val, 'tooltip', '该研发单元已撤销');
        } else if (
          val.implement_unit_status_special === 1 ||
          val.implement_unit_status_special === 2
        ) {
          this.$set(val, 'disable', true);
          this.$set(val, 'tooltip', '该研发单元处于暂缓状态');
        }
      });
      this.jobModel.proWantWindow = this.jobModel.proWantWindow
        ? this.jobModel.proWantWindow
        : this.jobModel.productionDate;
      if (!this.jobModel.review.db[0]) {
        this.jobModel.review.db = [
          {
            name: '否'
          }
        ];
      }
      this.oldDbModel = this.jobModel.review.db[0].name;
    },
    getCanNotModify() {
      let normalStatus = [1, 8, 9];
      let specialStatus = [1, 2];
      let fdevNormal = this.fdevNOinfo.implement_unit_info
        .implement_unit_status_normal;
      let fdevSpecial = this.fdevNOinfo.implement_unit_info
        .implement_unit_status_special;
      this.canNotModify = fdevSpecial
        ? specialStatus.indexOf(fdevSpecial) > -1
        : normalStatus.indexOf(fdevNormal) > -1;
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
          this.updateModalOpened = false;
        });
    },
    //筛选所属小组
    groupFilter(val, update, abort) {
      update(() => {
        if (
          this.groups &&
          Array.isArray(this.groups) &&
          this.groups.length > 0
        ) {
          this.groupList = this.groups.filter(group => {
            return group.label.includes(val);
          });
        }
      });
    },
    //筛选研发单元
    rqrmntInputFilter(val, update) {
      update(() => {
        this.ipmpUnitCloneList.forEach(tag => {
          if (tag.tooltip) {
            tag.disable = true;
          }
        });
        this.filterRqrmnt = this.ipmpUnitCloneList.filter(tag => {
          return (
            (tag.implement_unit_content &&
              tag.implement_unit_content.includes(val)) ||
            tag.fdev_implement_unit_no.toLowerCase().includes(val.toLowerCase())
          );
        });
      });
    },
    //计划启动日期控制
    planStartDateOptions(date) {
      date = moment(date).valueOf();
      //任务对应的研发单元计划启动日期
      const impDate =
        this.job.implement_unit_info &&
        this.job.implement_unit_info.plan_start_date
          ? moment(this.job.implement_unit_info.plan_start_date).valueOf()
          : '';
      let time =
        this.jobModel.plan_inner_test_time ||
        this.jobModel.plan_uat_test_start_time ||
        this.jobModel.plan_rel_test_time ||
        this.jobModel.plan_fire_time;
      if (impDate) {
        if (time && impDate >= moment(time).valueOf()) {
          //如果研发单元计划时间 大于选择的任务计划时间  取选择的任务时间
          return date <= moment(time).valueOf();
        }
        return date <= impDate;
      }
      return time ? date <= moment(time).valueOf() : true;
    },
    //计划提交内测日期控制
    planTestDateOptions(date) {
      date = moment(date).valueOf();
      //任务对应的研发单元计划提交内测日期
      const impDate =
        this.job.implement_unit_info &&
        this.job.implement_unit_info.plan_inner_test_date
          ? moment(this.job.implement_unit_info.plan_inner_test_date).valueOf()
          : '';
      let beforeTime = this.jobModel.plan_start_time;
      let afterTime =
        this.jobModel.plan_uat_test_start_time ||
        this.jobModel.plan_rel_test_time ||
        this.jobModel.plan_fire_time;
      if (impDate) {
        if (afterTime && impDate >= moment(afterTime).valueOf()) {
          //如果研发单元计划时间 大于选择的任务计划时间  取选择的任务时间
          return (
            date >= moment(beforeTime).valueOf() &&
            date <= moment(afterTime).valueOf()
          );
        }
        return date >= moment(beforeTime).valueOf() && date <= impDate;
      }
      return (
        date >= moment(beforeTime).valueOf() &&
        (afterTime ? date <= moment(afterTime).valueOf() : true)
      );
    },
    //计划提交用户测试日期控制
    planUatTestDateOptions(date) {
      date = moment(date).valueOf();
      //任务对应的研发单元计划提交用户测试日期
      const impDate =
        this.job.implement_unit_info &&
        this.job.implement_unit_info.plan_test_date
          ? moment(this.job.implement_unit_info.plan_test_date).valueOf()
          : '';
      let beforeTime =
        this.jobModel.plan_inner_test_time || this.jobModel.plan_start_time;
      let afterTime =
        this.jobModel.plan_rel_test_time || this.jobModel.plan_fire_time;
      if (impDate) {
        if (afterTime && impDate >= moment(afterTime).valueOf()) {
          //如果研发单元计划时间 大于选择的任务计划时间  取选择的任务时间
          return (
            date >= moment(beforeTime).valueOf() &&
            date <= moment(afterTime).valueOf()
          );
        }
        return date >= moment(beforeTime).valueOf() && date <= impDate;
      }
      return (
        date >= moment(beforeTime).valueOf() &&
        (afterTime ? date <= moment(afterTime).valueOf() : true)
      );
    },
    //计划用户测试完成日期
    planRelTestDateOptions(date) {
      date = moment(date).valueOf();
      //任务对应的研发单元计用户测试完成日期
      const impDate =
        this.job.implement_unit_info &&
        this.job.implement_unit_info.plan_test_finish_date
          ? moment(this.job.implement_unit_info.plan_test_finish_date).valueOf()
          : '';
      let beforeTime =
        this.jobModel.plan_uat_test_start_time ||
        this.jobModel.plan_inner_test_time ||
        this.jobModel.plan_start_time;
      let afterTime = this.jobModel.plan_fire_time;
      if (impDate) {
        if (afterTime && impDate >= moment(afterTime).valueOf()) {
          //如果研发单元计划时间 大于选择的任务计划时间  取选择的任务时间
          return (
            date >= moment(beforeTime).valueOf() &&
            date <= moment(afterTime).valueOf()
          );
        }
        return date >= moment(beforeTime).valueOf() && date <= impDate;
      }
      return (
        date >= moment(beforeTime).valueOf() &&
        (afterTime ? date <= moment(afterTime).valueOf() : true)
      );
    },
    //计划完成日期
    planFireDateOptions(date) {
      date = moment(date).valueOf();
      //任务对应的研发单元计划投产日期
      const impDate =
        this.job.implement_unit_info &&
        this.job.implement_unit_info.plan_product_date
          ? moment(this.job.implement_unit_info.plan_product_date).valueOf()
          : '';
      let beforeTime =
        this.jobModel.plan_rel_test_time ||
        this.jobModel.plan_uat_test_start_time ||
        this.jobModel.plan_inner_test_time ||
        this.jobModel.plan_start_time;
      if (impDate) {
        return date >= moment(beforeTime).valueOf() && date <= impDate;
      }
      return date >= moment(beforeTime).valueOf();
    },
    //开始Uat的时间控制
    uatStartDateOptions(date) {
      date = moment(date).valueOf();
      let currentDate = new Date().getTime();
      return (
        date <= currentDate &&
        date >= moment(this.jobModel.start_inner_test_time).valueOf()
      );
    },
    //开始Uat的时间控制
    relStartDateOptions(date) {
      date = moment(date).valueOf();
      let currentDate = new Date().getTime();
      if (this.jobModel.start_uat_test_time) {
        return (
          date <= currentDate &&
          date >= moment(this.jobModel.start_uat_test_time).valueOf() &&
          date >= moment(this.jobModel.start_inner_test_time).valueOf()
        );
      } else {
        return (
          date <= currentDate &&
          date >= moment(this.jobModel.start_inner_test_time).valueOf()
        );
      }
    },
    // 过滤人员
    userOptionsFilter(param, thisManager = []) {
      thisManager = thisManager === null ? [] : thisManager;
      let userOptions = this.users.filter(item => {
        let flag = false;
        let roleLabels = [];
        item.role.forEach(ele => {
          roleLabels.push(ele);
        });
        param.forEach(roleName => {
          if (roleLabels.includes(roleName)) {
            flag = true;
          }
        });
        thisManager.forEach(manager => {
          if (manager.id === item.id) {
            flag = true;
          }
        });
        return flag;
      });
      return userOptions;
    },
    //人员过滤
    async userFilter(val, update, abort) {
      update(() => {
        this.users = this.userOptions.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },
    isSpdbManager() {
      const { partyA } = this.job;
      return partyA.some(user => user.id === this.currentUser.id);
    },
    addReviewTerm(val, done) {
      if (val.length > 0) {
        val = { name: val, label: val, audit: false };
        done(val);
      }
    },
    async modifyTask() {
      if (
        this.jobProfile.start_uat_test_time &&
        !this.jobModel.start_uat_test_time
      )
        return;
      this.$v.jobModel.$touch();
      let jobModelKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('jobModel') > -1;
      });
      validate(
        jobModelKeys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.jobModel.$invalid) {
        let _this = this;
        let validateRes = jobModelKeys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.jobModel[itemArr[1]]
            ? _this.$v.jobModel[itemArr[1]].$invalid == false
            : true;
        });
        if (!validateRes) {
          return;
        }
      }
      // 上线确认书到达时间 有值 , 时间必填
      if (this.jobProfile.confirmFileDate && !this.jobModel.confirmFileDate) {
        return;
      }
      //后端传值字段转换
      this.jobModel.start_rel_test_time = this.jobModel.start_rel_test_time
        ? this.jobModel.start_rel_test_time
        : '';
      let other_system = this.jobModel.review.other_system;
      this.jobModel.review.other_system = Array.isArray(other_system)
        ? other_system
        : [{ name: other_system }];
      this.jobModel.confirmFileDate = this.jobModel.confirmFileDate;
      const params = {
        ...this.jobModel,
        group: this.jobModel.group.id,
        difficulty_desc: this.jobModel.direction
          ? this.jobModel.direction + this.jobModel.difficulty
          : '',
        modify_reason: this.difficultyIsChange
          ? this.jobModel.modify_reason
          : this.job.modify_reason
      };
      await this.updateJob(params);
      this.updateModalOpened = false;
      successNotify('修改成功');
      this.$emit('updateSuccess');
    }
  }
};
</script>
