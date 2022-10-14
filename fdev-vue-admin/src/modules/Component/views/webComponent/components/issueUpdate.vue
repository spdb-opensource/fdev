<template>
  <f-dialog
    title="信息维护"
    right
    f-sc
    :value="value"
    @input="$emit('input', $event)"
    @shake="confirmToClose"
  >
    <div>
      <!-- 开发人员 -->
      <f-formitem required diaS label="开发人员">
        <fdev-select
          use-input
          emit-value
          map-options
          :options="managerOptions"
          option-label="user_name_cn"
          option-value="id"
          ref="devComponentModel.assignee"
          @filter="managerFilter"
          v-model="$v.devComponentModel.assignee.$model"
          :rules="[
            () => $v.devComponentModel.assignee.required || '请选择开发人员'
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

      <!-- 当前阶段 -->
      <f-formitem required diaS label="当前阶段">
        <fdev-select
          use-input
          emit-value
          readonly
          map-options
          :options="webStageOptions"
          option-label="label"
          option-value="value"
          ref="devComponentModel.stage"
          v-model="$v.devComponentModel.stage.$model"
          :rules="[
            () => $v.devComponentModel.stage.required || '请选择当前阶段'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.label">
                  {{ scope.opt.label }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- 开发分支 -->
      <f-formitem required diaS label="开发分支">
        <fdev-input
          ref="devComponentModel.feature_branch"
          readonly
          v-model="$v.devComponentModel.feature_branch.$model"
          :rules="[
            () =>
              $v.devComponentModel.feature_branch.required || '请输入开发分支'
          ]"
        />
      </f-formitem>

      <!-- 计划SIT日期 -->
      <f-formitem required diaS label="计划SIT日期" v-if="isBusinessCom">
        <FDate
          ref="devComponentModel.sit_date"
          @setOptions="setOptionsSit"
          v-model="$v.devComponentModel.sit_date.$model"
          :rules="[
            () => $v.devComponentModel.sit_date.required || '请输入计划SIT日期'
          ]"
        />
      </f-formitem>

      <!-- 计划UAT日期 -->
      <f-formitem required diaS label="计划SIT日期" v-if="isBusinessCom">
        <FDate
          ref="devComponentModel.uat_date"
          @setOptions="setOptionsUat"
          v-model="$v.devComponentModel.uat_date.$model"
          :rules="[
            () => $v.devComponentModel.uat_date.required || '请输入计划UAT日期'
          ]"
        />
      </f-formitem>

      <!-- 计划完成日期 -->
      <f-formitem required diaS label="计划完成日期">
        <FDate
          ref="devComponentModel.due_date"
          @setOptions="setOptionsDue"
          v-model="$v.devComponentModel.due_date.$model"
          :rules="[
            () => $v.devComponentModel.due_date.required || '请选择计划完成日期'
          ]"
        />
      </f-formitem>

      <!-- 需求描述 -->
      <f-formitem required diaS label="需求描述">
        <fdev-input
          ref="devComponentModel.desc"
          type="textarea"
          v-model="$v.devComponentModel.desc.$model"
          :rules="[
            () => $v.devComponentModel.desc.required || '请输入需求描述'
          ]"
        />
      </f-formitem>
    </div>

    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        :loading="globalLoading['componentForm/updateMpassDevIssue']"
        @click="handleDialogFormAllTip"
      />
    </template>
  </f-dialog>
</template>

<script>
import { validate, successNotify, deepClone } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import { mapGetters, mapActions, mapState } from 'vuex';
import FDate from '@/components/Date';
import moment from 'moment';
import {
  devComponentModel,
  ComponentModelType,
  webStageOptions
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'RelUpdateDialog',
  components: { FDate },
  props: ['value', 'data', 'componentType'],
  data() {
    return {
      devComponentModel: devComponentModel(),
      options: [],
      managerOptions: [],
      showLoading: false,
      webStageOptions: webStageOptions,
      nowDate: moment().format('YYYY/MM/DD')
    };
  },
  validations() {
    const devComponentModel = {
      devComponentModel: {
        title: {
          required
        },
        desc: {
          required
        },
        feature_branch: {
          required
        },
        assignee: {
          required
        },
        stage: {
          required
        },
        due_date: {
          required
        },
        sit_date: {
          required
        },
        uat_date: {
          required
        }
      }
    };
    return devComponentModel;
  },
  watch: {
    async value(val) {
      this.showLoading = true;
      await this.fetch();
      this.managerOptions = this.userList;
      this.devComponentModel = {
        ...devComponentModel(),
        ...deepClone(this.data)
      };
      this.showLoading = false;
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', ['isLoginUserList']),
    userList() {
      return this.isLoginUserList.map(user => {
        return {
          user_name_cn: user.user_name_cn,
          id: user.id,
          user_name_en: user.user_name_en
        };
      });
    },
    isBusinessCom() {
      return this.componentType === ComponentModelType.Business;
    }
  },
  methods: {
    ...mapActions('user', ['fetch']),
    ...mapActions('componentForm', ['updateMpassDevIssue']),
    handleDialogFormAllTip() {
      this.$v.devComponentModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('devComponentModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.devComponentModel.$invalid) {
        return;
      }
      this.handleDialog();
    },
    setOptionsSit(date, callback) {
      callback(new Date(date).getTime() >= new Date(this.nowDate).getTime());
    },
    setOptionsUat(date, callback) {
      callback(
        new Date(date).getTime() >=
          new Date(this.devComponentModel.sit_date).getTime()
      );
    },
    setOptionsDue(date, callback) {
      if (this.isBusinessCom) {
        callback(
          new Date(date).getTime() >=
            new Date(this.devComponentModel.uat_date).getTime()
        );
      } else {
        callback(date >= this.nowDate);
      }
    },
    async handleDialog() {
      await this.updateMpassDevIssue(this.devComponentModel);
      successNotify('信息维护成功');
      this.$emit('click');
      this.$emit('input', false);
    },
    managerFilter(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.managerOptions = this.userList.filter(
          user =>
            user.user_name_cn.indexOf(needle) > -1 ||
            user.user_name_en.toLowerCase().indexOf(needle) > -1
        );
        if (this.managerOptions.length === 0) {
          this.managerOptions = this.userList;
        }
      });
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
