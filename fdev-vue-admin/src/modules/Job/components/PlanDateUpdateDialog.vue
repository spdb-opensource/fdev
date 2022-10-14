<template>
  <f-dialog
    :value="value"
    :btnDisplay="closeBtn()"
    title="批量创建任务计划时间补充"
    :confirm="closeBtn()"
    @input="$emit('input', $event)"
    right
  >
    <div class="q-gutter-y-diaLine rdia-dc-w row justify-between">
      <f-formitem label="计划启动开发日期">
        <f-date
          mask="YYYY/MM/DD"
          :rules="[
            () => $v.jobModel.devStartDate.required || '请输入计划启动开发日期',
            () =>
              $v.jobModel.devStartDate.timeValidateBefore ||
              '计划启动开发日期,应该小于等于计划提交内测日期'
          ]"
          :options="startPlanOptions"
          v-model="$v.jobModel.devStartDate.$model"
        />
      </f-formitem>
      <f-formitem label="计划提交内测日期">
        <f-date
          mask="YYYY/MM/DD"
          :options="sitPlanOptions"
          v-model="$v.jobModel.sitStartDate.$model"
          :rules="[
            () => $v.jobModel.sitStartDate.required || '请输入计划提交内测日期',
            () =>
              $v.jobModel.sitStartDate.timeValidateBefore ||
              '计划提交内测日期,应该大于等于计划提交用户测试日期',
            () =>
              $v.jobModel.sitStartDate.timeValidateAfter ||
              '计划提交内测日期,应该小于等于计划提交用户测试日期'
          ]"
        />
      </f-formitem>
      <f-formitem label="计划提交用户测试日期">
        <f-date
          mask="YYYY/MM/DD"
          :options="uatPlanOptions"
          v-model="$v.jobModel.uatStartDate.$model"
          :rules="[
            () =>
              $v.jobModel.uatStartDate.required || '请输入计划提交用户测试日期',
            () =>
              $v.jobModel.uatStartDate.timeValidateBefore ||
              '计划提交用户测试日期,应该大于等于计划提交内测日期',
            () =>
              $v.jobModel.uatStartDate.timeValidateAfter ||
              '计划提交用户测试日期,应该小于等于计划用户测试完成日期'
          ]"
        />
      </f-formitem>
      <f-formitem label="计划用户测试完成日期">
        <f-date
          mask="YYYY/MM/DD"
          :rules="[
            () =>
              $v.jobModel.relStartDate.required || '请输入计划用户测试完成日期',
            () =>
              $v.jobModel.relStartDate.timeValidateAfter ||
              '计划用户测试完成日期,应该小于等于计划投产日期'
          ]"
          :options="relPlanOptions"
          v-model="$v.jobModel.relStartDate.$model"
        />
      </f-formitem>
      <f-formitem label="计划投产日期">
        <f-date
          mask="YYYY/MM/DD"
          :rules="[
            () => $v.jobModel.productionDate.required || '请输入计划投产日期',
            () =>
              $v.jobModel.productionDate.timeValidateAfter ||
              '计划投产日期,应该大于等于计划用户测试完成日期'
          ]"
          :options="proPlanOptions"
          @input="() => $refs.qDateProxyProduct.hide()"
          v-model="$v.jobModel.productionDate.$model"
        />
      </f-formitem>
      <f-formitem label="开发人员" optional>
        <fdev-select
          use-chips
          use-input
          multiple
          input-debounce="0"
          ref="jobModel.developer"
          v-model="jobModel.developer"
          :options="userOptionsFilter(['开发人员'], jobModel.developer)"
          @filter="userFilter"
          hint=""
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
      <f-formitem label="标签" optional>
        <fdev-select
          v-model="jobModel.tag"
          multiple
          use-input
          use-chips
          hide-dropdown-icon
          ref="select"
          @new-value="addSelect"
          class="table-head-input col-5 search q-pb-lg"
        />
      </f-formitem>
      <f-formitem label="任务描述" optional>
        <fdev-input
          ref="jobModel.desc"
          v-model="jobModel.desc"
          type="textarea"
          hint=""
        />
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        :loading="globalLoading['jobForm/updateJobDate']"
        label="确认"
        @click="handleUpdate"
      />
    </template>
  </f-dialog>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import { mapState, mapActions, mapGetters } from 'vuex';
import { formatUser } from '@/modules/User/utils/model';
import {
  validate,
  formatOption,
  successNotify,
  errorNotify,
  deepClone
} from '@/utils/utils';
import { createJobModel } from '../utils/constants';
export default {
  name: 'PlanDateUpdateDialog',
  data() {
    return {
      users: [],
      jobModel: createJobModel(),
      usersList: []
    };
  },
  validations: {
    jobModel: {
      devStartDate: {
        required,
        timeValidateBefore(value) {
          if (!this.jobModel.sitStartDate) {
            return true;
          }
          return this.timeValidate(this.jobModel.sitStartDate, value);
        }
      },
      sitStartDate: {
        required,
        timeValidateBefore(value) {
          return this.timeValidate(value, this.jobModel.devStartDate);
        },
        timeValidateAfter(value) {
          return this.timeValidate(this.jobModel.uatStartDate, value);
        }
      },
      uatStartDate: {
        required,
        timeValidateBefore(value) {
          return this.timeValidate(value, this.jobModel.sitStartDate);
        },
        timeValidateAfter(value) {
          return this.timeValidate(this.jobModel.relStartDate, value);
        }
      },
      relStartDate: {
        required,
        timeValidateBefore(value) {
          return this.timeValidate(value, this.jobModel.uatStartDate);
        },
        timeValidateAfter(value) {
          return this.timeValidate(this.jobModel.productionDate, value);
        }
      },
      productionDate: {
        required,
        timeValidateAfter(value) {
          return this.timeValidate(value, this.jobModel.relStartDate);
        }
      }
    }
  },
  props: {
    value: {
      type: Boolean,
      default: true
    },
    job: {
      default: () => {},
      type: Object
    }
  },
  watch: {
    job(val) {
      if (val) {
        this.jobModel = deepClone(val);
      }
    },
    developer(val) {
      this.jobModel.developer = this.usersList.filter(user => {
        return this.job.developer.some(developer => {
          return user.id === developer.id;
        });
      });
    },
    value(val) {
      if (!val) return;
      this.getUserOptions();
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    developer() {
      return { user: this.usersList, developer: this.job.developer };
    }
  },
  methods: {
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('jobForm', ['updateJobDate']),
    timeValidate(date, startDate) {
      if (!date || !startDate) {
        return true;
      }
      let start = new Date(startDate);
      let currentDate = new Date(date);
      return start <= currentDate;
    },
    /*任务管理页面必须补充批量创建任务计划时间*/
    closeBtn() {
      //任务改造 地址从  /job/list/${id}/manage 改为 /job/list/${id}?manage
      // return !this.$route.Path.includes('manage');
      return !this.$route.fullPath.includes('manage');
    },
    async confirm() {},
    async getUserOptions() {
      await this.fetchUser();
      let users = this.isLoginUserList;
      this.users = users.map(user => formatOption(formatUser(user), 'name'));
      this.userOptions = this.users.slice(0);
      this.usersList = this.userOptions;
    },
    /* 计划启动开发日期 */
    startPlanOptions(date) {
      const afterDate =
        this.jobModel.sitStartDate ||
        this.jobModel.uatStartDate ||
        this.jobModel.relStartDate ||
        this.jobModel.productionDate;
      if (!this.jobModel.devStartDate) {
        this.jobModel.devStartDate = afterDate ? afterDate : '';
      }
      return afterDate ? date <= afterDate : true;
    },
    /* 计划SIT测试日期 */
    sitPlanOptions(date) {
      const beforeDate = this.jobModel.devStartDate;
      const afterDate =
        this.jobModel.uatStartDate ||
        this.jobModel.relStartDate ||
        this.jobModel.productionDate;
      const dateSmallThanAfterDate = afterDate ? date <= afterDate : true;
      if (!this.jobModel.sitStartDate) {
        this.jobModel.sitStartDate = beforeDate ? beforeDate : '';
      }
      return beforeDate <= date && dateSmallThanAfterDate;
    },
    async userFilter(val, update, abort) {
      const needle = val.toLowerCase().trim();
      update(() => {
        this.users = this.userOptions.filter(
          user =>
            user.name.includes(needle) || user.user_name_en.includes(needle)
        );
      });
    },
    /* 计划UAT测试日期 */
    uatPlanOptions(date) {
      const beforeDate =
        this.jobModel.sitStartDate || this.jobModel.devStartDate;
      const afterDate =
        this.jobModel.relStartDate || this.jobModel.productionDate;
      const dateSmallThanAfterDate = afterDate ? date <= afterDate : true;
      if (!this.jobModel.uatStartDate) {
        this.jobModel.uatStartDate = beforeDate ? beforeDate : '';
      }
      return beforeDate <= date && dateSmallThanAfterDate;
    },
    /* 计划REL测试日期 */
    relPlanOptions(date) {
      const beforeDate =
        this.jobModel.uatStartDate ||
        this.jobModel.sitStartDate ||
        this.jobModel.devStartDate;
      const afterDate = this.jobModel.productionDate;
      const dateSmallThanAfterDate = afterDate ? date <= afterDate : true;
      if (!this.jobModel.relStartDate) {
        this.jobModel.relStartDate = beforeDate ? beforeDate : '';
      }
      return beforeDate <= date && dateSmallThanAfterDate;
    },
    /* 计划投产日期 */
    proPlanOptions(date) {
      const beforeDate =
        this.jobModel.relStartDate ||
        this.jobModel.uatStartDate ||
        this.jobModel.sitStartDate ||
        this.jobModel.devStartDate;
      if (!this.jobModel.productionDate) {
        this.jobModel.productionDate = beforeDate ? beforeDate : '';
      }
      return beforeDate <= date;
    },
    addSelect(val, done) {
      let newVal = val.replace(/(^\s*)|(\s*$)/g, '');
      this.jobModel.tag = this.jobModel.tag ? this.jobModel.tag : [];
      let notOnly = this.jobModel.tag.some(item => {
        return item.replace(/(^\s*)|(\s*$)/g, '') === newVal;
      });
      if (newVal.length > 0 && !notOnly) {
        done(newVal);
      }
    },
    userOptionsFilter(param, thisManager = []) {
      thisManager = thisManager === null ? [] : thisManager;
      let userOptions = this.users.filter(item => {
        let flag = false;
        let roleLabels = [];
        item.role.forEach(ele => {
          roleLabels.push(ele.name);
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
    async handleUpdate() {
      this.$v.jobModel.$touch();
      let jobKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('jobModel') > -1
      );
      validate(jobKeys.map(key => this.$refs[key]));
      if (this.$v.jobModel.$invalid) {
        return;
      }
      // 强制修改批量任务日期  只传我们修改的和必传的
      let params = {
        id: this.jobModel.id,
        plan_start_time: this.jobModel.devStartDate,
        plan_inner_test_time: this.jobModel.sitStartDate,
        plan_uat_test_start_time: this.jobModel.uatStartDate,
        plan_rel_test_time: this.jobModel.relStartDate,
        plan_fire_time: this.jobModel.productionDate,
        developer: this.jobModel.developer.map(ele => ele.id),
        tag: this.jobModel.tag,
        desc: this.jobModel.desc
      };
      try {
        await this.updateJobDate(params);
        successNotify('修改成功');
        this.$emit('closePlanDateUpdateDialog');
      } catch (e) {
        errorNotify('修改失败');
      }
    }
  }
};
</script>

<style lang="stylus" scoped>
.dialog-height
  height 400px!important
</style>
