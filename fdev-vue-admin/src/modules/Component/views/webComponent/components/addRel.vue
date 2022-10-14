<template>
  <f-dialog
    title="新增投产窗口"
    right
    f-sc
    :value="value"
    @input="$emit('input', $event)"
    @shake="confirmToClose"
  >
    <div>
      <!-- 组件名称 -->
      <f-formitem required diaS label="组件名称">
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

      <!-- 当前优化需求类型 -->
      <f-formitem required diaS label="当前优化需求类型">
        <fdev-select
          use-input
          emit-value
          map-options
          option-value="id"
          :options="needTypeList"
          option-label="needTypeName"
          ref="optimizeModel.issue_type"
          @input="getVerAndBranch"
          v-model="$v.optimizeModel.issue_type.$model"
          :rules="[
            () => $v.optimizeModel.issue_type.required || '请选择需求类型'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.needTypeName">
                  {{ scope.opt.needTypeName }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- 预设版本号 -->
      <f-formitem required diaS label="预设版本号">
        <fdev-input
          ref="optimizeModel.predict_version"
          v-model="$v.optimizeModel.predict_version.$model"
          :rules="[
            () =>
              $v.optimizeModel.predict_version.required || '请输入预设版本号',
            () =>
              $v.optimizeModel.predict_version.examine ||
              '请输入正确的格式:*.x.x, 只能输入数字'
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

      <!-- 版本管理员 -->
      <f-formitem required diaS label="版本管理员">
        <fdev-select
          multiple
          use-input
          emit-value
          map-options
          :options="developerList"
          @filter="userFilter"
          option-label="user_name_cn"
          ref="optimizeModel.manager"
          v-model="$v.optimizeModel.manager.$model"
          :rules="[
            () => $v.optimizeModel.manager.required || '请选择版本管理员'
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
          mask="YYYY/MM/DD"
          v-model="$v.optimizeModel.due_date.$model"
          :rules="[
            () => $v.optimizeModel.due_date.required || '请输入计划完成日期'
          ]"
          :options="dateOptions"
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
import {
  needTypeList,
  WebOptimizeDialogModel
} from '@/modules/Component/utils/constants.js';
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import { mapGetters, mapState, mapActions } from 'vuex';
export default {
  name: 'OptimizeDialog',
  data() {
    return {
      optimizeModel: WebOptimizeDialogModel(),
      users: [],
      managerOptions: [],
      needTypeList,
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
      issue_type: {
        required
      },
      manager: { required },
      component: {},
      title: {
        required
      },
      desc: {
        required
      },
      predict_version: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^(\d{1,})\.((?!0)\d{2}|\d{1})\.((?!0)\d{2}|\d{1})$/;
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
      if (val) {
        await this.fetch();
        this.optimizeModel.component = this.component;
        this.optimizeModel.component_id = this.component_id;
        this.managerOptions = this.developerOptions;
      } else {
        this.optimizeModel = WebOptimizeDialogModel();
      }
    },
    'optimizeModel.predict_version': {
      handler(val) {
        if (!val) {
          return;
        }
        this.optimizeModel.feature_branch =
          this.optimizeModel.issue_type === '0'
            ? `release${val}`
            : `release${val}_fix`;
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('componentForm', [
      'mpassDefaultBranchAndVersion',
      'branchAndVersion'
    ]),
    loading() {
      if (this.$route.path.includes('web')) {
        return 'componentForm/addMpassReleaseIssue';
      }
      return 'componentForm/addReleaseIssue';
    },
    developerOptions() {
      return this.isLoginUserList.map(user => {
        return {
          user_name_cn: user.user_name_cn,
          id: user.id,
          user_name_en: user.user_name_en
        };
      });
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryMpassDefaultBranchAndVersion',
      'addMpassReleaseIssue',
      'defaultBranchAndVersion',
      'addReleaseIssue'
    ]),
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

    async getVerAndBranch() {
      if (this.$route.path.includes('web')) {
        await this.queryMpassDefaultBranchAndVersion({
          component_id: this.component_id,
          issue_type: this.optimizeModel.issue_type
        });
        this.optimizeModel.predict_version = this.mpassDefaultBranchAndVersion.predict_version;
        this.optimizeModel.feature_branch = this.mpassDefaultBranchAndVersion.feature_branch;
      } else {
        await this.defaultBranchAndVersion({
          component_id: this.component_id,
          issue_type: this.optimizeModel.issue_type
        });
        this.optimizeModel.predict_version = this.branchAndVersion.predict_version;
        this.optimizeModel.feature_branch = this.branchAndVersion.feature_branch;
      }
    },
    /* 提交优化 */
    async handleOptimize() {
      this.$route.path.includes('web')
        ? await this.addMpassReleaseIssue(this.optimizeModel)
        : await this.addReleaseIssue(this.optimizeModel);
      successNotify('新增投产窗口成功');
      this.$emit('refresh', this.value);
      this.$emit('input', false);
    },
    userFilter(val, update) {
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
