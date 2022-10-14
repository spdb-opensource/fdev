<template>
  <f-dialog
    title="新增开发分支"
    right
    f-sc
    :value="value"
    @input="$emit('input', $event)"
  >
    <div>
      <!-- 开发分支名 -->
      <f-formitem required diaS label="开发分支名">
        <fdev-input
          ref="optimizeModel.feature_branch"
          v-model="$v.optimizeModel.feature_branch.$model"
          :rules="[
            () =>
              $v.optimizeModel.feature_branch.required || '请输入拉取的分支名',
            () => $v.optimizeModel.feature_branch.examine || '不能输入中文'
          ]"
        />
      </f-formitem>

      <!-- 开发人员 -->
      <f-formitem required diaS label="开发人员">
        <fdev-select
          use-input
          emit-value
          map-options
          option-value="id"
          :options="developerList"
          option-label="user_name_cn"
          ref="optimizeModel.assignee"
          v-model="$v.optimizeModel.assignee.$model"
          @filter="userFilter"
          :rules="[
            () => $v.optimizeModel.assignee.required || '请选择开发人员'
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

      <!-- 计划SIT日期 -->
      <f-formitem v-if="isBusinessCom" required diaS label="计划SIT日期">
        <f-date
          ref="optimizeModel.sit_date"
          mask="YYYY/MM/DD"
          v-model="$v.optimizeModel.sit_date.$model"
          :options="optionsSIT"
          :rules="[
            () => $v.optimizeModel.sit_date.myRequired || '请输入计划SIT日期'
          ]"
        />
      </f-formitem>

      <!-- 计划UAT日期 -->
      <f-formitem v-if="isBusinessCom" required diaS label="计划UAT日期">
        <f-date
          ref="optimizeModel.uat_date"
          :options="optionsUAT"
          mask="YYYY/MM/DD"
          v-model="$v.optimizeModel.uat_date.$model"
          :rules="[
            () => $v.optimizeModel.uat_date.myRequired || '请输入计划UAT日期'
          ]"
        />
      </f-formitem>

      <!-- 计划完成日期 -->
      <f-formitem required diaS label="计划完成日期">
        <f-date
          ref="optimizeModel.due_date"
          :options="optionsDue"
          mask="YYYY/MM/DD"
          v-model="$v.optimizeModel.due_date.$model"
          :rules="[
            () => $v.optimizeModel.due_date.required || '请输入计划完成日期'
          ]"
        />
      </f-formitem>

      <!-- 需求描述 -->
      <f-formitem required diaS label="需求描述">
        <fdev-input
          type="textarea"
          ref="optimizeModel.desc"
          v-model="$v.optimizeModel.desc.$model"
          :rules="[() => $v.optimizeModel.desc.required || '请填写需求描述']"
        />
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        :loading="globalLoading[loading]"
        @click="handleOptimizeTipAll"
      />
    </template>
  </f-dialog>
</template>

<script>
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import { mapGetters, mapState, mapActions } from 'vuex';
import {
  ComponentModelType,
  WebAddDevDialogModel
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'WebAddDevDialog',
  data() {
    return {
      optimizeModel: WebAddDevDialogModel(),
      users: [],
      developerList: [],
      nowDate: moment().format('YYYY/MM/DD')
    };
  },
  validations: {
    optimizeModel: {
      feature_branch: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /[\u4E00-\u9FA5]/;
          return !reg.test(val);
        }
      },
      // component_id: {
      //   required
      // },
      assignee: {
        required
      },
      // component: {},
      // title: {
      //   required
      // },
      desc: {
        required
      },
      // target_version: {
      //   required,
      //   examine(val) {
      //     if (!val) {
      //       return true;
      //     }
      //     const reg = /^(?!0)(\d{1,})\.((?!0)\d{2}|\d{1})\.((?!0)\d{2}|\d{1})$/;
      //     return reg.test(val);
      //   }
      // },
      due_date: {
        required
      },
      sit_date: {
        myRequired(val) {
          if (this.isBusinessCom) {
            return required(val);
          }
          return true;
        }
      },
      uat_date: {
        myRequired(val) {
          if (this.isBusinessCom) {
            return required(val);
          }
          return true;
        }
      }
    }
  },
  props: {
    value: Boolean,
    issue_id: String,
    componentType: {
      default: '',
      type: String
    }
  },
  watch: {
    async value(val) {
      if (val === false) {
        this.optimizeModel = WebAddDevDialogModel();
      } else {
        await this.fetch();
        this.optimizeModel.issue_id = this.issue_id;
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    loading() {
      if (this.$route.path.includes('web')) {
        return 'componentForm/addMpassDevIssue';
      }
      return 'componentForm/addDevIssue';
    },
    developerOptions() {
      return this.userList.filter(user => {
        return user.role.some(role => {
          return role.name === '开发人员';
        });
      });
    },
    isBusinessCom() {
      return this.componentType === ComponentModelType.Business;
    }
  },
  methods: {
    ...mapActions('componentForm', ['addMpassDevIssue', 'addDevIssue']),
    ...mapActions('user', ['fetch']),
    // setOptionsSit(date, callback) {
    //   callback(new Date(date).getTime() >= new Date(this.nowDate).getTime());
    // },
    optionsSIT(date) {
      return new Date(date).getTime() >= new Date(this.nowDate).getTime();
    },
    // setOptionsUat(date, callback) {
    //   callback(
    //     new Date(date).getTime() >=
    //       new Date(this.optimizeModel.sit_date).getTime()
    //   );
    // },
    optionsUAT(date) {
      return (
        new Date(date).getTime() >=
        new Date(this.optimizeModel.sit_date).getTime()
      );
    },
    // setOptionsDue(date, callback) {
    //   if (this.isBusinessCom) {
    //     callback(
    //       new Date(date).getTime() >=
    //         new Date(this.optimizeModel.uat_date).getTime()
    //     );
    //   } else {
    //     callback(date >= this.nowDate);
    //   }
    // },
    optionsDue(date) {
      if (this.isBusinessCom) {
        return (
          new Date(date).getTime() >=
          new Date(this.optimizeModel.uat_date).getTime()
        );
      } else {
        return date >= this.nowDate;
      }
    },
    handleOptimizeTipAll() {
      this.$v.optimizeModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('optimizeModel') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key].$refs.date) {
            return this.$refs[key].$children[0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.optimizeModel.$invalid) {
        return;
      }
      this.handleOptimize();
    },
    /* 提交优化 */
    async handleOptimize() {
      this.$route.path.includes('web')
        ? await this.addMpassDevIssue(this.optimizeModel)
        : await this.addDevIssue(this.optimizeModel);
      successNotify('新增开发人员成功');
      this.$emit('refresh', this.value);
      this.$emit('input', false);
    },
    userFilter(val, update, abort) {
      const needle = val.toLowerCase();
      update(() => {
        this.developerList = this.developerOptions.filter(
          user =>
            user.user_name_cn.indexOf(needle) > -1 ||
            user.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
      if (this.developerList.length === 0) {
        this.developerList = this.developerOptions;
      }
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
    }
  }
};
</script>
