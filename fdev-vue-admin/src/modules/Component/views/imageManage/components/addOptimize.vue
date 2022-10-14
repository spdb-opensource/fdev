<template>
  <f-dialog
    right
    f-sc
    title="新增优化需求"
    :value="value"
    @input="$emit('input', $event)"
  >
    <div>
      <!-- 镜像名称 -->
      <f-formitem required diaS label="镜像名称">
        <fdev-input
          disable
          v-model="baseImageDetail.name"
          :rules="[() => $v.imageIssueModel.name.required || '请输入镜像名称']"
        />
      </f-formitem>

      <!-- 标题 -->
      <f-formitem required diaS label="标题">
        <fdev-input
          ref="imageIssueModel.title"
          v-model="$v.imageIssueModel.title.$model"
          :rules="[() => $v.imageIssueModel.title.required || '请输入标题']"
        />
      </f-formitem>

      <!-- 开发分支 -->
      <f-formitem required diaS label="开发分支">
        <fdev-input
          ref="imageIssueModel.branch"
          v-model="$v.imageIssueModel.branch.$model"
          :rules="[
            () => $v.imageIssueModel.branch.required || '请输入开发分支'
          ]"
          :prefix="branchStart"
        >
          <!-- <fdev-input
            v-show="
              $v.imageIssueModel.branch.$model.length > 0 || isFocus
            "
            type="text"
            dense
            disable
            class="branch-start"
            v-model="branchStart"
          ></fdev-input> -->
        </fdev-input>
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
          ref="imageIssueModel.assignee"
          v-model="$v.imageIssueModel.assignee.$model"
          @filter="userFilter"
          :rules="[
            () => $v.imageIssueModel.assignee.required || '请选择开发人员'
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
          mask="YYYY/MM/DD"
          ref="imageIssueModel.due_date"
          v-model="$v.imageIssueModel.due_date.$model"
          :options="dateOptions"
          :rules="[
            () => $v.imageIssueModel.due_date.required || '请输入计划完成日期'
          ]"
        />
      </f-formitem>

      <!-- 需求描述 -->
      <f-formitem required diaS label="需求描述">
        <fdev-input
          type="textarea"
          ref="imageIssueModel.desc"
          v-model="$v.imageIssueModel.desc.$model"
          :rules="[() => $v.imageIssueModel.desc.required || '请填写需求描述']"
        />
      </f-formitem>
    </div>

    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        :loading="globalLoading['componentForm/AddOptimizeBaseImageIssue']"
        @click="handleOptimize"
      />
    </template>
  </f-dialog>
</template>

<script>
import { imageIssueModel } from '@/modules/Component/utils/constants.js';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import { mapGetters, mapState, mapActions } from 'vuex';
import moment from 'moment';
export default {
  name: 'OptimizeDialog',
  data() {
    return {
      disabled: true,
      isShow: true,
      imageIssueModel: imageIssueModel(),
      users: [],
      developerList: [],
      isFocus: false,
      style: { 'padding-left': '30px' },
      branchStart: 'dev-'
    };
  },
  validations: {
    imageIssueModel: {
      title: {
        required
      },
      branch: {
        required
      },
      desc: {
        required
      },
      assignee: {
        required
      },
      due_date: {
        required
      }
    }
  },
  props: {
    value: Boolean
  },
  watch: {
    async value(val) {
      if (val === false) {
        this.imageIssueModel = imageIssueModel();
      } else {
        await this.fetch();
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', ['baseImageDetail']),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    developerOptions() {
      return this.userList.filter(user => {
        return user.role.some(role => {
          return role.name === '开发人员';
        });
      });
    }
  },
  methods: {
    ...mapActions('componentForm', ['AddOptimizeBaseImageIssue']),
    ...mapActions('user', ['fetch']),

    /* 提交优化 */
    async handleOptimize() {
      this.$v.imageIssueModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('imageIssueModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.imageIssueModel.$invalid) {
        return;
      }
      let { branch } = this.imageIssueModel;
      await this.AddOptimizeBaseImageIssue({
        ...this.imageIssueModel,
        name: this.baseImageDetail.name,
        branch: this.branchStart + branch
      });
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
    }
    // confirmToClose() {
    //   this.$q
    //     .dialog({
    //       title: '关闭弹窗',
    //       message: '关闭弹窗后数据将会丢失，确认要关闭？',
    //       cancel: true,
    //       persistent: true
    //     })
    //     .onOk(() => {
    //       this.$emit('input', false);
    //     });
    // }
  }
};
</script>
