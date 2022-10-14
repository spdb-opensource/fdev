<template>
  <Loading :visible="stepLoading">
    <f-block>
      <fdev-stepper
        v-model="step"
        ref="stepper"
        flat
        alternative-labels
        class="stepper"
        animated
        transition-prev="fade"
        transition-next="fade"
        v-if="step < 3"
      >
        <fdev-step
          :name="1"
          prefix="1"
          active-prefix="1"
          title="同步单元编号"
          icon="settings"
          :done="step > 1"
        >
          <fdev-card-section class="form q-pa-md">
            <div class="formrow row justify-center">
              <div class="col-8" align="center">
                <fdev-select
                  use-input
                  transition-show="jump-down"
                  transition-hide="jump-down"
                  v-model="term"
                  option-label="fdev_implement_unit_no"
                  :options="filteredUnits"
                  label="需求名称、需求编号、FDEV研发单元编号、研发单元内容"
                  @filter="unitFilter"
                >
                  <template v-slot:prepend>
                    <f-icon
                      name="search"
                      class="text-primary q-mr-sm"
                      :width="16"
                      :height="16"
                    />
                  </template>
                  <template v-slot:option="scope">
                    <fdev-item
                      v-bind="scope.itemProps"
                      v-on="scope.itemEvents"
                      @click="() => handleStep1(scope.opt, scope.opt.disable)"
                      :disabled="scope.opt.disable"
                    >
                      <fdev-item-section>
                        <fdev-item-label>
                          <span>
                            <span class="text-grey-9 q-pr-sm"
                              >研发单元名称:</span
                            >
                            {{ scope.opt.implement_unit_content }}
                          </span>
                        </fdev-item-label>
                        <fdev-item-label caption>
                          <span
                            class="q-pr-lg"
                            v-show="scope.opt.oa_contact_no"
                          >
                            <span class="text-grey-9">需求编号:</span>
                            {{ scope.opt.oa_contact_no }}
                          </span>
                          <span v-show="scope.opt.oa_contact_name">
                            <span class="text-grey-9">需求名称:</span>
                            {{ scope.opt.oa_contact_name }}
                          </span>
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
                  <template v-slot:no-option>
                    <fdev-item>
                      <fdev-item-section>
                        <span>该需求暂无研发单元，请联系需求负责人创建</span>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </div>
            </div>
          </fdev-card-section>
        </fdev-step>

        <fdev-step
          :name="2"
          prefix="2"
          active-prefix="2"
          title="录入基本信息"
          icon="assignment"
          :done="step > 2"
        >
          <fdev-form @submit.prevent="handleStep2">
            <div class="row boxStyle full-width">
              <div class="row items-center line-title">
                <f-icon name="basic_msg_s_f" class="titleimg q-ml-md" />
                <span class="titlename">基本信息</span>
              </div>
            </div>
            <!-- 任务类型 -->
            <div class="row ml20 mr119">
              <f-formitem
                class="radio-box"
                label="任务类型"
                label-style="width:112px"
              >
                <!-- 根据需求类型加展示 -->
                <div class="row mrbt20">
                  <!-- 开发任务 -->
                  <div
                    class="row items-center"
                    v-if="taskTypeRqr.includes('develop')"
                  >
                    <fdev-radio
                      val="0"
                      v-model="jobModel.taskType"
                      label="开发任务"
                    />
                    <f-icon
                      name="help_c_o"
                      width="20px"
                      class="text-primary cursor-pointer mr60 q-pl-xs"
                    />
                    <fdev-tooltip position="top" :offest="[0, 0]">
                      需创建分支并含测试流程的任务
                    </fdev-tooltip>
                  </div>
                  <!-- 无代码任务 -->
                  <div
                    class="row items-center"
                    v-if="taskTypeRqr.includes('nocode')"
                  >
                    <fdev-radio
                      val="1"
                      v-model="jobModel.taskType"
                      label="无代码任务"
                    />
                    <f-icon
                      name="help_c_o"
                      width="20px"
                      class="text-primary cursor-pointer mr60 q-pl-xs"
                    />
                    <fdev-tooltip position="top" :offest="[0, 0]">
                      不需创建分支
                    </fdev-tooltip>
                  </div>
                  <!-- 日常任务 -->
                  <div
                    class="row items-center"
                    v-if="taskTypeRqr.includes('daily')"
                  >
                    <fdev-radio
                      val="2"
                      v-model="jobModel.taskType"
                      label="日常任务"
                    />
                    <f-icon
                      name="help_c_o"
                      width="20px"
                      class="text-primary cursor-pointer q-pl-xs"
                    />
                    <fdev-tooltip position="top" :offest="[0, 0]">
                      不含测试流程的任务
                    </fdev-tooltip>
                  </div>
                </div>
              </f-formitem>
            </div>
            <!-- 开发任务分为是否简单需求任务 -->
            <div class="row ml20 mr119" v-if="jobModel.taskType == '0'">
              <f-formitem
                class="radio-box"
                label="是否简单需求任务"
                label-style="width:112px"
              >
                <div class="row mrbt20">
                  <div class="row items-center">
                    <fdev-radio
                      val="0"
                      v-model="jobModel.simpleDemand"
                      label="是"
                    />
                    <f-icon
                      name="help_c_o"
                      width="20px"
                      class="text-primary cursor-pointer mr60 q-pl-xs"
                    />
                    <fdev-tooltip position="top" :offest="[0, 0]">
                      可以跳过功能测试的任务
                    </fdev-tooltip>
                  </div>
                  <fdev-radio
                    val="1"
                    v-model="jobModel.simpleDemand"
                    label="否"
                  />
                </div>
              </f-formitem>
            </div>

            <div v-if="jobModel.taskType == '2'">
              <AddNormalTask
                ref="addNormalTask"
                :jobModel="jobModel"
                :allGroups="groups"
                :allUsers="users"
              ></AddNormalTask>
            </div>
            <div v-if="jobModel.taskType == '0'">
              <AddDevTask
                ref="addDevTask"
                :jobModel="jobModel"
                :allGroups="groups"
                :allUsers="users"
              ></AddDevTask>
            </div>
            <!-- 无代码任务 -->
            <div v-if="jobModel.taskType == '1'">
              <AddNoCodeTask
                ref="addNoCodeTask"
                :jobModel="jobModel"
                :allGroups="groups"
                :allUsers="users"
              ></AddNoCodeTask>
            </div>
            <div class="row justify-center mt32" v-if="!step2End">
              <fdev-btn
                outline
                label="上一步"
                class="mr32"
                @click="handleStepPrevious"
                :disable="loading"
              />
              <fdev-btn
                type="submit"
                @click="handleStep2AllTip"
                label="确认"
                :loading="globalLoading['jobForm/addJob']"
              />
            </div>
          </fdev-form>
        </fdev-step>
      </fdev-stepper>

      <div v-else class="col row items-center">
        <Result type="success" class="col q-my-xl">
          <template v-slot:title>
            新增成功
          </template>
          <!-- <template v-slot:description>
          描述信息
        </template> -->
          <template v-slot:actions>
            <div class="row justify-center q-gutter-x-sm">
              <fdev-btn flat @click="goList" label="返回列表" />
              <fdev-btn flat :to="`/job/list/${result.id}`" label="查看项目" />
            </div>
          </template>
        </Result>
      </div> </f-block
  ></Loading>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import Result from '@/components/Result';
import AddDevTask from '../../components/AddDevTask';
import AddNoCodeTask from '../../components/AddNoCodeTask';
import AddNormalTask from '../../components/AddNormalTask';
import Loading from '@/components/Loading';
import {
  createJobModel,
  applicationTypes,
  componentTypes,
  archetTypes
} from '../../utils/constants';
import {
  formatOption,
  deepClone,
  errorNotify,
  getGroupFullName
} from '@/utils/utils';
import moment from 'moment';

export default {
  name: 'Add',
  components: {
    Result,
    AddNormalTask,
    AddDevTask,
    AddNoCodeTask,
    Loading
  },
  data() {
    return {
      radioFlag: '',
      otherTaskIdToPush: '',
      ipmpUnitCloneList: [],
      step: 1,
      jobModel: createJobModel(),
      loading: false,
      addModel: {},
      filteredUnits: [],
      term: null,
      step2End: false,
      users: [],
      groups: [],
      result: {},
      continueJob: false,
      branchResult: {},
      filterGroup: [], // 小组相关
      color: 'primary',
      stepLoading: false
    };
  },

  watch: {},
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser',
      userList: 'userList'
    }),
    ...mapGetters('user', ['isLoginUserList', 'isKaDianManager']),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('jobForm', [
      'jobs',
      'branchData',
      'jobProfile',
      'judgeResult',
      'noCodeTask',
      'rqrmntsList',
      'taskTypeRqr',
      'isExistJiraStory',
      'userInfoList' //查询所有用户信息（包括角色）
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('demandsForm', ['ipmpUnitList', 'implementUnitData'])
  },
  methods: {
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    ...mapActions('jobForm', {
      addJob: 'addJob',
      queryJob: 'fetchJob',
      queryJobProfile: 'queryJobProfile',
      taskNameJudge: 'taskNameJudge',
      nocodeTask: 'nocodeTask',
      queryJiraStoryByKey: 'queryJiraStoryByKey',
      queryAddTaskType: 'queryAddTaskType',
      getAllUserAndRole: 'getAllUserAndRole' //查询所有用户信息（包括角色）
    }),
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup'
    }),
    ...mapActions('demandsForm', [
      'queryAvailableIpmpUnit',
      'queryPaginationByDemandId'
    ]),
    goList() {
      this.$router.push('/job/list');
    },
    formatDate(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYY/MM/DD');
    },
    async handleFocus() {
      this.stepLoading = true;
      try {
        await this.queryAvailableIpmpUnit();
      } catch (e) {
        this.stepLoading = false;
      }
      this.ipmpUnitCloneList =
        this.ipmpUnitList.length > 0 ? deepClone(this.ipmpUnitList) : [];
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
      this.stepLoading = false;
    },
    getGroupFullName,
    async initQuery() {
      Promise.all([
        //查询小组
        this.queryGroup(),
        //查询用户
        // this.queryUser()
        this.getAllUserAndRole({ status: '0' })
      ]).then(() => {
        //处理小组数据
        this.groups = formatOption(this.groupsData);
        this.currentUser.groupFullName = getGroupFullName(
          this.groups,
          this.currentUser.group.id
        );
        this.filterGroup = this.groups.slice(0);
        //处理用户数据
        // this.users = this.isLoginUserList.map(user =>
        //   formatOption(formatUser(user), 'name')
        // );
        this.users = this.userInfoList.slice(0);
        // 展示默认值
        this.jobModel.group = this.filterGroup.find(item =>
          item.fullName.includes(this.currentUser.groupFullName)
        );
        this.jobModel.desc = '';
        this.step = 2;
        this.stepLoading = false;
      });
    },
    handerRadioType() {
      // handerRadioType;  查需求可新建的任务类型
      // 当前需求可建任务类型，develop-可建开发任务，daily-可建日常任务
      //版本一 只能建 开发 或者  日常 三种情况
      // 三种情况
      // 1 ['develop','daily'] 1
      // 2 ['develop'] -1
      // 3 ['daily'] 2
      // 老逻辑
      // if (this.taskTypeRqr.length > 1) {
      //   // ['develop','daily']
      //   this.radioFlag = 1;
      //   this.jobModel.taskType = '0'; //开发
      // } else if (this.taskTypeRqr.indexOf('develop') > -1) {
      //   // ['develop']
      //   this.radioFlag = -1;
      //   this.jobModel.taskType = '0'; //开发
      // } else if (this.taskTypeRqr.indexOf('daily') > -1) {
      //   // ['daily']
      //   this.radioFlag = 2;
      //   this.jobModel.taskType = '2'; //日常
      // } else {
      //   return;
      // }
      //规则 开发 无代码成对出现
      // 版本 二
      // 1 ['develop','daily','nocode'] 1
      // 2 ['develop','nocode'] 1
      // 3 ['daily'] 2
      if (this.taskTypeRqr.includes('develop')) {
        this.jobModel.taskType = '0'; //开发
      } else {
        this.jobModel.taskType = '2'; //日常
      }
    },
    async handleStep1(val, disable) {
      this.stepLoading = true;
      this.jobModel.devStartDate = val.plan_start_date
        ? val.plan_start_date.replace(/-/g, '/')
        : '';
      this.jobModel.sitStartDate = val.plan_inner_test_date
        ? val.plan_inner_test_date.replace(/-/g, '/')
        : '';
      this.jobModel.uatStartDate = val.plan_test_date
        ? val.plan_test_date.replace(/-/g, '/')
        : '';
      this.jobModel.relStartDate = this.jobModel.uatEndDate = val.plan_test_finish_date
        ? val.plan_test_finish_date.replace(/-/g, '/')
        : '';
      this.jobModel.productionDate = val.plan_product_date
        ? val.plan_product_date.replace(/-/g, '/')
        : '';
      this.jobModel.proWantWindow = this.jobModel.productionDate;
      for (let key in val) {
        if (val[key]) {
          this.jobModel[key] = val[key];
        }
      }
      try {
        // 查需求可新建的任务类型  taskTypeRqr
        await this.queryAddTaskType({
          demandId: val.demand_id,
          unitNo: val.fdev_implement_unit_no
        });
        this.handerRadioType();
        // 查询小组和人员
        await this.initQuery();
      } catch (e) {
        this.stepLoading = false;
      }

      // this.initJobObj = deepClone(this.jobModel);
    },
    handleStep2AllTip() {
      if (this.jobModel.taskType === '0') {
        this.$refs.addDevTask.validateJob();
      } else if (this.jobModel.taskType === '1') {
        this.$refs.addNoCodeTask.validateJob();
      } else if (this.jobModel.taskType === '2') {
        this.$refs.addNormalTask.validateJob();
      } else {
        return;
      }
    },
    async unitFilter(val, update, abort) {
      val = val.trim();
      update(() => {
        this.ipmpUnitCloneList.forEach(tag => {
          if (tag.tooltip) {
            tag.disable = true;
          }
        });
        this.filteredUnits = this.ipmpUnitCloneList.filter(user => {
          return (
            (user.implement_unit_content &&
              user.implement_unit_content.includes(val)) ||
            user.fdev_implement_unit_no
              .toLowerCase()
              .includes(val.toLowerCase()) ||
            (user.oa_contact_no &&
              user.oa_contact_no.toLowerCase().includes(val.toLowerCase())) ||
            (user.oa_contact_name &&
              user.oa_contact_name.toLowerCase().includes(val.toLowerCase()))
          );
        });
      });
    },
    //新增任务
    async handleStep2(e) {
      // 无用代码
      // if (!this.$v.jobModel.difficulty.required) {
      //   return;
      // }
      // TODO,新增任务拆分
      // step-任务基本信息保存
      if (this.jobModel.taskType === '0') {
        this.addModel = deepClone(this.$refs.addDevTask.jobModel);
      } else if (this.jobModel.taskType === '1') {
        this.addModel = deepClone(this.$refs.addNoCodeTask.jobModel);
      } else if (this.jobModel.taskType === '2') {
        this.addModel = deepClone(this.$refs.addNormalTask.jobModel);
      } else {
        return;
      }
      let addTaskMolde = {
        tag: this.addModel.tag,
        redmine_id: this.addModel.unitNo,
        name: this.addModel.name,
        group: this.addModel.group.id,
        master: this.addModel.partyB.map(each => each.id),
        spdb_master: this.addModel.partyA.map(each => each.id),
        developer: this.addModel.developer.map(each => each.id),
        tester: this.addModel.tester.map(each => each.id),

        plan_start_time: this.addModel.devStartDate,
        plan_inner_test_time: this.addModel.sitStartDate,
        plan_uat_test_start_time: this.addModel.uatStartDate,
        plan_uat_test_stop_time: this.addModel.relStartDate,
        plan_rel_test_time: this.addModel.relStartDate,
        plan_fire_time: this.addModel.productionDate,
        proWantWindow: this.addModel.proWantWindow,
        desc: this.addModel.desc,
        doc: this.addModel.doc.map(each => each.id),
        review: {
          data_base_alter: [this.addModel.review.db],
          commonProfile: this.addModel.review.commonProfile,
          other_system: Array.isArray(this.addModel.review.other_system)
            ? this.addModel.review.other_system
            : [this.addModel.review.other_system],
          specialCase: this.addModel.review.specialCase,
          securityTest: this.addModel.review.securityTest
        },
        system_remould: this.addModel.system_remould,
        system_remould_notify: this.addModel.system_remould_notify,
        impl_data: this.addModel.impl_data,
        db_rele_app: this.addModel.db_rele_app,
        difficulty_desc: this.addModel.direction
          ? this.addModel.direction + this.addModel.difficulty
          : '',
        fdev_implement_unit_no: this.addModel.fdev_implement_unit_no,
        rqrmnt_no: this.addModel.demand_id,
        project_id: this.addModel.application
          ? this.addModel.application.id
          : '',
        versionNum: this.addModel.versionNum ? this.addModel.versionNum : '',
        project_name: this.addModel.application
          ? this.addModel.application.name_en
          : '',
        feature_branch: this.addModel.feature
          ? this.jobModel.type === 'image' && this.jobModel.featureType == '1'
            ? 'dev-' + this.addModel.feature
            : this.addModel.feature
          : '',
        branchType: this.addModel.featureType
      };
      let applicationType = '';
      if (this.jobModel.application) {
        if (this.jobModel.type === 'application') {
          applicationType =
            applicationTypes[this.jobModel.application.type_name];
        } else if (this.jobModel.type === 'components') {
          applicationType =
            componentTypes[this.jobModel.application.component_type];
        } else if (this.jobModel.type === 'archetype') {
          applicationType =
            archetTypes[this.jobModel.application.archetype_type];
        } else if (this.jobModel.type === 'image') {
          applicationType = 'image';
        }
      }
      if (this.jobModel.taskType === '0') {
        addTaskMolde = {
          ...addTaskMolde,
          storyId: this.addModel.jiraId,
          applicationType,
          simpleDemand: this.jobModel.simpleDemand //开发任务区分是否简单任务类型
        };
      }

      if (this.jobModel.taskType === '1') {
        addTaskMolde = {
          ...addTaskMolde,
          project_name: '',
          project_id: '',
          feature_branch: '',
          storyId: this.addModel.jiraId,
          taskType: 1
        };
      }
      if (this.jobModel.taskType == '2') {
        addTaskMolde = {
          ...addTaskMolde,
          taskType: 2,
          applicationType
        };
      }
      try {
        await this.addJob(addTaskMolde);
        this.result = this.jobProfile;
        this.step2End = true;
        this.loading = false;
        this.step = 3;
      } catch {
        this.loading = false;
      }
    },
    handleStepPrevious() {
      this.$refs.stepper.previous();
    },
    async handleStepNext(step, noCode) {
      //3 新建分支 ，5都不做(成功)
      if (step == 5 && this.continueJob && noCode == 0) {
        this.$router.push({ path: '/job/list' });
      }
      if (step == 6 && noCode == 1) {
        this.otherTaskIdToPush = this.jobProfile.id;
        // 点击其他任务 请求  参数id,taskType  任务id  任务类型taskType  1为无代码变更
        const params = {
          id: this.otherTaskIdToPush,
          taskType: 1
        };
        await this.nocodeTask(params);
        this.$router.push({
          path: '/job/list/' + this.otherTaskIdToPush,
          query: { noCode: noCode, tab: 'manage' }
        });
      }
      if (this.step2End) {
        //基本信息录入
        this[`step${step}Disable`] = false;
        this.step = parseInt(step);
      }
    },
    unitFilted(val) {
      val = val.trim();
      this.filteredUnits = this.ipmpUnitList.filter(user => {
        return user.fdev_implement_unit_no
          .toLowerCase()
          .includes(val.toLowerCase());
      });
    }
  },
  async created() {
    this.handleFocus();
    //入口 任务详情——生产问题
    if (this.$route.query.name && this.$route.query.unitNo) {
      this.unitFilted(this.$route.query.unitNo);
      this.handleStep1(this.filteredUnits[0]);
      this.jobModel.name = this.$route.query.name;
    }
    //入口我的——生产问题
    if (this.$route.query.name && this.$route.query.taskNo) {
      await this.queryJobProfile({ id: this.$route.query.taskNo });
      this.unitFilted(this.jobProfile.fdev_implement_unit_no);
      this.handleStep1(this.filteredUnits[0]);
      this.jobModel.name = this.$route.query.name;
    }
    // 放开权限所有人可以新增
    // let role_names = this.currentUser.role.map(each => each.name);
    // if (
    //   !role_names.includes('行内项目负责人') &&
    //   !role_names.includes('厂商项目负责人') &&
    //   !this.isKaDianManager
    // ) {
    //   errorNotify(
    //     '当前用户无权限新建任务，只有行内/厂商项目负责人才可执行此操作!'
    //   );
    //   this.$router.push('/job/list');
    //   return;
    // }
    //从任务详情处过来
    const id = this.$route.params.id;
    if (id) {
      // 未完成任务继续创建
      this.continueJob = true;
      await this.queryJob({ id: id });
      this.jobModel = this.jobs[0];
      let jobManager = this.jobModel.partyB
        .concat(this.jobModel.partyA)
        .concat(this.jobModel.creator);
      let manager = jobManager.find(manager => {
        return manager.id === this.currentUser.id;
      });
      if (!manager && !this.isKaDianManager) {
        errorNotify('无权限继续该任务');
        return;
      }
      if (this.jobModel.stage.value === 'create-info') {
        this.step = 2;
        this.jobModel.taskType = '0';
      }
    }
    const unitData = this.$route.params.unitData;
    if (unitData && Object.keys(unitData).length) {
      this.handleStep1(unitData);
    }
  }
};
</script>

<style lang="stylus" scoped>
  @import '~#/css/variables.styl';
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
  .titleStyle
    font-weight: 600;
    font-size: 14px;
    color: #333333;
    letter-spacing: 0;
    line-height: 14px;
  .mr119
    margin-right: 119px;
  .ml20
    margin-left:20px;
  .mt32
    margin-top: 32px;
  .mr60
    margin-right: 60px;
  .mr32
    margin-right: 32px;
  .mrbt20
    margin-bottom: 20px;
  .formrow
    margin 15px
  .form
    max-width 820px
    margin 0 auto
    @media screen and (max-width: $sizes.sm)
      margin-left -24px
      margin-right -24px
.field-height
  height 50px
.rules
  line-height: 1;
  font-size: 11px;
  line-height: 1;
  padding: 8px 12px;
  min-height 12px
  box-sizing: content-box;
.title
  font-size: 18px
  padding 10px
/deep/ .q-date__actions {
    position: absolute;
    background: black;
    opacity: 0.8;
    color: #fff;
    padding: 16px;
    width: 100%;
}
/deep/ .q-stepper--horizontal .q-stepper__step-inner{
  padding:0px;
}
.margin-o-auto
  margin:0 auto
.row-mb
  margin-bottom 32px
>>> .q-stepper__header--alternative-labels .q-stepper__dot
  width 32px
  height 32px
.radio-box >>> .input-md
  width auto
</style>
