<template>
  <f-dialog
    right
    f-sc
    title="新增优化需求"
    :value="value"
    @input="$emit('input', $event)"
    @shake="confirmToClose"
  >
    <div>
      <!-- 组件名称 -->
      <f-formitem diaS label="组件名称">
        <fdev-input
          ref="optimizeModel.component"
          disable
          v-model="$v.optimizeModel.component.$model"
          :rules="[() => true]"
        />
      </f-formitem>

      <!-- 标题 -->
      <f-formitem required diaS label="标题">
        <fdev-input
          ref="optimizeModel.title"
          v-model="$v.optimizeModel.title.$model"
          :rules="[() => $v.optimizeModel.title.required || '请输入标题']"
        />
      </f-formitem>

      <!-- 目标版本 -->
      <f-formitem
        required
        diaS
        label="目标版本"
        help="第一段不限制长度，第二、三段最多两位"
      >
        <fdev-input
          ref="optimizeModel.target_version"
          v-model="$v.optimizeModel.target_version.$model"
          :rules="[
            () => $v.optimizeModel.target_version.required || '请输入目标版本',
            () =>
              $v.optimizeModel.target_version.examine ||
              '请输入正确的格式:*.x.x, 只能输入数字并不能以0开头'
          ]"
        />
      </f-formitem>

      <!-- 开发分支名 -->
      <f-formitem required diaS label="开发分支名">
        <fdev-input
          ref="feature_branch"
          disable=""
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

      <!-- 计划完成日期 -->
      <f-formitem required diaS label="计划完成日期">
        <f-date
          ref="optimizeModel.due_date"
          v-model="$v.optimizeModel.due_date.$model"
          mask="YYYY/MM/DD"
          :options="dateOptions"
          :rules="[val => !!val || '请选择变更日期']"
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
        :loading="globalLoading['componentForm/optimizeComponent']"
        @click="handleOptimizeTipAll"
      />
    </template>
  </f-dialog>
</template>

<script>
import { optimizeModel } from '@/modules/Component/utils/constants.js';
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import { mapGetters, mapState, mapActions } from 'vuex';
export default {
  name: 'OptimizeDialog',
  data() {
    return {
      optimizeModel: optimizeModel(),
      users: [],
      developerList: []
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
      component_id: {
        required
      },
      assignee: {
        required
      },
      component: {},
      title: {
        required
      },
      desc: {
        required
      },
      target_version: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^(?!0)(\d{1,})\.((?!0)\d{2}|\d{1})\.((?!0)\d{2}|\d{1})$/;
          return reg.test(val);
        }
      },
      due_date: {
        required
      }
    }
  },
  props: {
    value: Boolean,
    component: String,
    component_id: String
  },
  watch: {
    async value(val) {
      if (val === false) {
        this.optimizeModel = optimizeModel();
      } else {
        this.optimizeModel.component = this.component;
        this.optimizeModel.component_id = this.component_id;
        await this.fetch();
      }
    },
    'optimizeModel.target_version': {
      handler(val) {
        if (!val) {
          return;
        }
        this.optimizeModel.feature_branch =
          this.componentDetail.type === '0'
            ? `${val}-SNAPSHOT`
            : `${this.componentDetail.name_en}-${val}-SNAPSHOT`;
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
    ...mapState('componentForm', ['componentDetail']),
    developerOptions() {
      return this.userList.filter(user => {
        return user.role.some(role => {
          return role.name === '开发人员';
        });
      });
    }
  },
  methods: {
    ...mapActions('componentForm', ['optimizeComponent']),
    ...mapActions('user', ['fetch']),

    handleOptimizeTipAll() {
      this.$v.optimizeModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('optimizeModel') > -1;
      });
      validate(
        Keys.map(key => {
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
      await this.optimizeComponent(this.optimizeModel);
      successNotify('新增优化需求成功');
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
    dateOptions(date) {
      const today = moment(new Date()).format('YYYY/MM/DD');
      return date >= today;
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
