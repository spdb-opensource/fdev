<template>
  <f-dialog
    :value="value"
    title="新增优化需求"
    @input="$emit('input', $event)"
    right
  >
    <Loading :visible="showLoading">
      <f-formitem label="标题" diaS required>
        <fdev-input
          ref="optimizeModel.title"
          type="text"
          v-model="$v.optimizeModel.title.$model"
          :rules="[() => $v.optimizeModel.title.required || '请输入标题']"
        />
      </f-formitem>
      <f-formitem label="开发分支名" diaS required>
        <fdev-input
          ref="feature_branch"
          type="text"
          v-model="$v.optimizeModel.feature_branch.$model"
          :rules="[
            () =>
              $v.optimizeModel.feature_branch.required || '请输入拉取的分支名',
            () => $v.optimizeModel.feature_branch.examine || '不能输入中文'
          ]"
        />
      </f-formitem>
      <f-formitem label="开发人员" diaS required>
        <fdev-select
          fill-input
          use-input
          emit-value
          map-options
          option-value="id"
          input-debounce="0"
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
      <f-formitem label="计划完成日期" diaS required>
        <f-date
          mask="YYYY/MM/DD"
          v-model="$v.optimizeModel.due_date.$model"
          :options="dateOptions"
          :rules="[
            () => $v.optimizeModel.due_date.required || '请输入计划完成日期'
          ]"
        />
      </f-formitem>
      <f-formitem label="需求描述" diaS required>
        <fdev-input
          type="textarea"
          ref="optimizeModel.desc"
          v-model="$v.optimizeModel.desc.$model"
          :rules="[() => $v.optimizeModel.desc.required || '请填写需求描述']"
        />
      </f-formitem>
    </Loading>
    <template v-slot:btnSlot>
      <fdev-btn
        label="确定"
        :loading="globalLoading['componentForm/addMpassArchetypeIssue']"
        dialog
        @click="handleOptimizeTipAll"
      />
    </template>
  </f-dialog>
</template>

<script>
import {
  needTypeList,
  WebArchetypeOptimizeDialogModel
} from '@/modules/Component/utils/constants.js';
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import { mapGetters, mapState, mapActions } from 'vuex';
import Loading from '@/components/Loading';

export default {
  name: 'OptimizeDialog',
  components: { Loading },
  data() {
    return {
      optimizeModel: WebArchetypeOptimizeDialogModel(),
      users: [],
      needTypeList,
      developerList: [],
      showLoading: false
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
      assignee: { required },
      title: {
        required
      },
      desc: {
        required
      },
      due_date: {
        required
      }
    }
  },
  props: {
    value: Boolean,
    component: String,
    archetype_id: String
  },
  watch: {
    async value(val) {
      this.showLoading = true;
      if (val) {
        await this.fetch();
        this.optimizeModel.component = this.component;
        this.optimizeModel.archetype_id = this.archetype_id;
      } else {
        this.optimizeModel = WebArchetypeOptimizeDialogModel();
      }
      this.showLoading = false;
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('componentForm', [
      'mpassComDetail',
      'mpassDefaultBranchAndVersion'
    ]),
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
    ...mapActions('componentForm', ['addMpassArchetypeIssue']),
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
      await this.addMpassArchetypeIssue(this.optimizeModel);
      successNotify('新增优化需求成功');
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
