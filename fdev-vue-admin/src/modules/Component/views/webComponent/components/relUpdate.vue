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
      <!-- 标题 -->
      <f-formitem required diaS label="标题">
        <fdev-input
          ref="relComponentModel.title"
          label="标题"
          v-model="$v.relComponentModel.title.$model"
          :rules="[() => $v.relComponentModel.title.required || '请输入标题']"
        />
      </f-formitem>

      <!-- 版本管理员 -->
      <f-formitem required diaS label="版本管理员">
        <fdev-select
          multiple
          use-input
          emit-value
          map-options
          :option-value="opt => opt"
          :options="managerOptions"
          option-label="user_name_cn"
          ref="relComponentModel.manager"
          @filter="managerFilter"
          v-model="$v.relComponentModel.manager.$model"
          :rules="[
            () => $v.relComponentModel.manager.required || '请选择版本管理员'
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

      <!-- 当前优化需求类型 -->
      <f-formitem required diaS label="当前优化需求类型">
        <fdev-select
          use-input
          emit-value
          readonly
          map-options
          option-value="id"
          :options="needTypeList"
          option-label="needTypeName"
          ref="relComponentModel.issue_type"
          v-model="$v.relComponentModel.issue_type.$model"
          :rules="[
            () =>
              $v.relComponentModel.issue_type.required || '请选择优化需求类型'
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

      <!-- release分支 -->
      <f-formitem required diaS label="release分支">
        <fdev-input
          ref="relComponentModel.feature_branch"
          readonly
          v-model="$v.relComponentModel.feature_branch.$model"
          :rules="[
            () =>
              $v.relComponentModel.feature_branch.required ||
              '请输入release分支'
          ]"
        />
      </f-formitem>

      <!-- 预设版本号 -->
      <f-formitem required diaS label="预设版本号">
        <fdev-input
          ref="relComponentModel.predict_version"
          readonly
          v-model="$v.relComponentModel.predict_version.$model"
          :rules="[
            () =>
              $v.relComponentModel.predict_version.required ||
              '请输入预设版本号'
          ]"
        />
      </f-formitem>

      <!-- 计划完成日期 -->
      <f-formitem required diaS label="计划完成日期">
        <f-date
          ref="relComponentModel.due_date"
          v-model="relComponentModel.due_date"
          mask="YYYY/MM/DD"
          :rules="[
            () => $v.relComponentModel.due_date.required || '请选择计划完成日期'
          ]"
        />
      </f-formitem>

      <!-- 组件描述 -->
      <f-formitem required diaS label="组件描述">
        <fdev-input
          ref="relComponentModel.desc"
          type="textarea"
          v-model="$v.relComponentModel.desc.$model"
          :rules="[
            () => $v.relComponentModel.desc.required || '请输入组件描述'
          ]"
        />
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        :loading="globalLoading[loading]"
        @click="handleDialogFormAllTip"
      />
    </template>
  </f-dialog>
</template>

<script>
import { validate, successNotify, deepClone } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  relComponentModel,
  needTypeList
} from '@/modules/Component/utils/constants.js';
import { mapGetters, mapActions, mapState } from 'vuex';

export default {
  name: 'RelUpdateDialog',
  props: ['value', 'data'],
  data() {
    return {
      relComponentModel: relComponentModel(),
      options: [],
      managerOptions: [],
      showLoading: false,
      needTypeList: needTypeList
    };
  },
  validations() {
    const relComponentModel = {
      relComponentModel: {
        title: {
          required
        },
        desc: {
          required
        },
        feature_branch: {
          required
        },
        predict_version: {
          required
        },
        manager: {
          required
        },
        issue_type: {
          required
        },
        due_date: {
          required
        }
      }
    };
    return relComponentModel;
  },
  watch: {
    async value(val) {
      this.showLoading = true;
      this.managerOptions = this.userList;
      this.relComponentModel = deepClone(this.data);
      await this.fetch();
      this.showLoading = false;
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', ['isLoginUserList']),
    loading() {
      if (this.$route.path.includes('web')) {
        return 'componentForm/updateMpassReleaseIssue';
      }
      return 'componentForm/updateReleaseIssue';
    },
    userList() {
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
    ...mapActions('user', ['fetch']),
    ...mapActions('componentForm', [
      'updateMpassReleaseIssue',
      'updateReleaseIssue'
    ]),
    handleDialogFormAllTip() {
      this.$v.relComponentModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('relComponentModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.relComponentModel.$invalid) {
        return;
      }
      this.handleDialog();
    },
    async handleDialog() {
      this.$route.path.includes('web')
        ? await this.updateMpassReleaseIssue(this.relComponentModel)
        : await this.updateReleaseIssue(this.relComponentModel);
      successNotify('信息维护成功');
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
