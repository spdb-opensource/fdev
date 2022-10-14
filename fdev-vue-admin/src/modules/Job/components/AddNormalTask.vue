<template>
  <div>
    <div class="row ml20">
      <div class="col">
        <f-formitem label="任务名称" required label-style="width:112px">
          <fdev-input
            ref="jobModel.name"
            v-model="$v.jobModel.name.$model"
            type="text"
            autofocus
            @blur="judgeTaskName"
            :rules="[
              () => $v.jobModel.name.required || '请输入任务名称',
              () => $v.jobModel.name.judge || '任务名已存在'
            ]"
          />
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
      </div>
      <div class="col">
        <f-formitem label="小组" required label-style="width:112px">
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

        <f-formitem label="计划完成日期" required label-style="width:112px">
          <f-date
            mask="YYYY/MM/DD"
            :disableTips="tip"
            ref="jobModel.productionDate"
            v-model="$v.jobModel.productionDate.$model"
            :rules="[
              () =>
                $v.jobModel.productionDate.required || '计划完成日期不能为空'
            ]"
            :options="productionDateOptions"
          />
        </f-formitem>
        <f-formitem label="任务描述" optional label-style="width:112px">
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
      <div class="row items-center">
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
          label="镜像名称"
          :label-col="4"
          label-style="width:112px"
          v-show="jobModel.type === 'image'"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="jobModel.application"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_cn"
            @input="showFeature()"
            option-value="name"
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
          v-show="jobModel.type === 'application'"
          label="应用名称"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="jobModel.application"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_zh"
            @input="showFeature()"
            option-value="name_en"
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
          v-show="jobModel.type === 'components'"
          label="组件名称"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="jobModel.application"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_cn"
            option-value="name_en"
            @input="showFeature()"
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
          v-show="jobModel.type === 'archetype'"
          label="骨架名称"
          :label-col="4"
          label-style="width:112px"
        >
          <fdev-select
            use-input
            ref="jobModel.application"
            v-model="jobModel.application"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_cn"
            option-value="name_en"
            @input="showFeature()"
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
          :label-col="6"
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
    <div class="ml20 mt20" v-if="showFlag">
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
          :label-col="6"
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
  </div>
</template>

<script>
import {
  deepClone,
  validate,
  resolveResponseError,
  formatOption
} from '@/utils/utils';
import { required, minLength } from 'vuelidate/lib/validators';
import { mapState, mapActions, mapMutations } from 'vuex';
import { DemandType, tipContent } from '../utils/constants';
import { checkComponentV, checkArchetypeV } from '@/services/job';
import moment from 'moment';
export default {
  data() {
    return {
      showFlag: false,
      branchOptions: [],
      users: this.allUsers,
      usersList: [],
      filterGroup: [], // 小组相关
      userOptions: [],
      groups: this.allGroups,
      initJobObj: {},
      applications: [],
      applicationOptions: [],
      judgeResultTip: true,
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
      productionDate: {
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
    }
  },
  computed: {
    ...mapState('appForm', {
      appList: 'vueAppData'
    }),
    ...mapState('componentForm', ['imageManageList']),
    ...mapState('jobForm', [
      'branchList',
      'judgeResult',
      'componentList',
      'archetList'
    ]),
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
  watch: {
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
  props: {
    allGroups: {
      default: () => [],
      type: Array
    },
    allUsers: {
      default: () => [],
      type: Array
    },
    jobModel: {
      default: () => {},
      type: Object
    }
  },
  methods: {
    ...mapActions('appForm', {
      queryApps: 'queryApps'
    }),
    ...mapActions('componentForm', ['queryBaseImage']),
    ...mapActions('jobForm', [
      'queryProjectBranchList',
      'taskNameJudge',
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
    //选择任务后展示分支
    showFeature() {
      if (this.jobModel.application) {
        this.showFlag = true;
      } else {
        this.showFlag = false;
        this.jobModel.feature = '';
      }
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
    formatDate(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYY/MM/DD');
    },
    devStartDateOptions(date) {
      let afterDate =
        this.formatDate(this.initJobObj.devStartDate) >=
        this.jobModel.productionDate
          ? this.jobModel.productionDate
          : this.formatDate(this.initJobObj.devStartDate);
      return date <= afterDate;
    },
    productionDateOptions(date) {
      let beforeDate = this.jobModel.devStartDate;
      let afterDate = this.formatDate(this.initJobObj.productionDate);
      if (afterDate && beforeDate) {
        return date <= afterDate && date >= beforeDate;
      } else if (beforeDate) {
        return date >= beforeDate;
      } else {
        return date <= afterDate;
      }
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
      this.showFlag = false;
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
    if (this.jobModel.application) {
      this.showFlag = true;
    }
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
.titleStyle
  font-weight: 600;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
.ml20
  margin-left: 20px;
.mt20
  margin-top: 20px;
.mr60
  margin-right: 60px;
.errMsg
  height 20px
  font-size 12px
  line-height 20px
  color #ef5350
</style>
