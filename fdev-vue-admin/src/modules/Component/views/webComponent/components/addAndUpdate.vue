<template>
  <f-dialog
    :title="title"
    right
    f-sc
    :value="value"
    @input="$emit('input', $event)"
    @shake="confirmToClose"
  >
    <div>
      <!-- 组件英文名 -->
      <f-formitem required diaS label="组件英文名">
        <fdev-input
          ref="componentModel.name_en"
          :disable="title == '信息维护' ? true : false"
          v-model="$v.componentModel.name_en.$model"
          :rules="[
            () => $v.componentModel.name_en.required || '请输入组件英文名',
            () => $v.componentModel.name_en.examine || '只能输入英文、数字、.-_'
          ]"
        />
      </f-formitem>

      <!-- 组件中文名 -->
      <f-formitem required diaS label="组件中文名">
        <fdev-input
          ref="componentModel.name_cn"
          v-model="$v.componentModel.name_cn.$model"
          :rules="[
            () => $v.componentModel.name_cn.required || '请输入组件中文名',
            () => $v.componentModel.name_cn.examine || '至少包含一个中文'
          ]"
        />
      </f-formitem>

      <!-- 组件管理员 -->
      <f-formitem required diaS label="组件管理员">
        <fdev-select
          multiple
          use-input
          emit-value
          map-options
          :options="managerOptions"
          @filter="managerFilter"
          option-label="user_name_cn"
          ref="componentModel.manager"
          v-model="$v.componentModel.manager.$model"
          :rules="[
            () => $v.componentModel.manager.required || '请选择组件管理员'
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

      <!-- 组件类型 -->
      <f-formitem required diaS label="组件类型">
        <fdev-select
          emit-value
          map-options
          :options="webTypeOptions"
          option-label="label"
          option-value="value"
          ref="componentModel.type"
          v-model="$v.componentModel.type.$model"
          :rules="[() => $v.componentModel.type.required || '请选择组件类型']"
        />
      </f-formitem>

      <!-- 组件来源 -->
      <f-formitem required diaS label="组件来源">
        <fdev-select
          emit-value
          map-options
          :options="webSourceOptions"
          :disable="title == '信息维护' ? true : false"
          option-label="label"
          option-value="value"
          ref="componentModel.source"
          v-model="$v.componentModel.source.$model"
          :rules="[() => $v.componentModel.source.required || '请选择组件来源']"
        />
      </f-formitem>

      <!-- 所属小组 -->
      <f-formitem required diaS label="所属小组">
        <fdev-select
          use-input
          emit-value
          map-options
          option-value="id"
          option-label="fullName"
          ref="componentModel.group"
          v-model="$v.componentModel.group.$model"
          :options="filterGroup"
          @filter="groupInputFilter"
          :rules="[() => $v.componentModel.group.required || '请选择所属小组']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name">
                  {{ scope.opt.name }}
                </fdev-item-label>
                <fdev-item-label caption="" :title="scope.opt.fullName">
                  {{ scope.opt.fullName }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- 技术栈 -->
      <f-formitem required diaS label="技术栈" v-if="isBusinessCom">
        <fdev-input
          ref="componentModel.skillstack"
          v-model="$v.componentModel.skillstack.$model"
          :rules="[
            () => $v.componentModel.skillstack.myRequired || '请输入组件技术栈'
          ]"
        />
      </f-formitem>

      <!-- 业务领域 -->
      <f-formitem required diaS label="业务领域" v-if="isBusinessCom">
        <fdev-input
          ref="componentModel.businessarea"
          v-model="$v.componentModel.businessarea.$model"
          :rules="[
            () =>
              $v.componentModel.businessarea.myRequired || '请输入组件业务领域'
          ]"
        />
      </f-formitem>

      <!-- npm坐标name -->
      <f-formitem required diaS label="npm坐标name">
        <fdev-input
          ref="componentModel.npm_name"
          v-model="$v.componentModel.npm_name.$model"
          :rules="[
            () => $v.componentModel.npm_name.required || '请输入组件npm坐标name'
          ]"
        />
      </f-formitem>

      <!-- npm坐标group(选填) -->
      <f-formitem diaS label="npm坐标group(选填)">
        <fdev-input
          ref="componentModel.npm_group"
          v-model="$v.componentModel.npm_group.$model"
          :rules="[() => true]"
        />
      </f-formitem>

      <!-- gitlab地址 -->
      <f-formitem
        required
        diaS
        label="gitlab地址"
        v-if="componentModel.source === '0'"
      >
        <fdev-input
          ref="componentModel.gitlab_url"
          :disable="disable"
          v-model="$v.componentModel.gitlab_url.$model"
          :rules="[
            () =>
              $v.componentModel.gitlab_url.myRequired || '请输入组件gitlab地址'
          ]"
        />
      </f-formitem>

      <!-- 项目根路径(选填) -->
      <f-formitem
        diaS
        label="项目根路径(选填)"
        v-if="componentModel.source === '0'"
      >
        <fdev-input
          ref="componentModel.root_dir"
          v-model="$v.componentModel.root_dir.$model"
          :rules="[() => true]"
        />
      </f-formitem>
      <!-- 是否涉及内测 -->
      <f-formitem diaS label="是否涉及内测" class="edit-form-cells">
        <fdev-radio
          val="1"
          v-model="$v.componentModel.isTest.$model"
          label="是"
        />
        <fdev-radio
          val="0"
          v-model="$v.componentModel.isTest.$model"
          label="否"
          class="q-ml-lg"
        />
      </f-formitem>
      <!-- 组件描述 -->
      <f-formitem required diaS label="组件描述">
        <fdev-input
          ref="componentModel.desc"
          type="textarea"
          v-model="$v.componentModel.desc.$model"
          :rules="[() => $v.componentModel.desc.required || '请输入组件描述']"
        />
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        label="确定"
        dialog
        :loading="globalLoading[`componentForm/${thisLoading}`]"
        @click="handleSubmit"
      />
    </template>
  </f-dialog>
</template>

<script>
import {
  webTypeOptions,
  webSourceOptions,
  createWebComModel
} from '@/modules/Component/utils/constants.js';
import { required } from 'vuelidate/lib/validators';
import { mapGetters, mapActions, mapState } from 'vuex';
import { deepClone, validate, successNotify } from '@/utils/utils';
import { ComponentModelType } from '@/modules/Component/utils/constants.js';

export default {
  props: ['value', 'data'],
  data() {
    return {
      componentModel: createWebComModel(),
      managerOptions: [],
      webTypeOptions,
      webSourceOptions,
      filterGroup: [],
      deepCloneGroups: [],
      showLoading: false
    };
  },
  validations: {
    componentModel: {
      name_en: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^[\w.-]+$/;
          return reg.test(val);
        }
      },
      name_cn: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /[\u4E00-\u9FA5]/gm;
          return reg.test(val);
        }
      },
      isTest: {},
      manager: { required },
      type: { required },
      source: { required },
      group: { required },
      npm_name: { required },
      npm_group: {},
      gitlab_url: {
        myRequired(val) {
          return this.componentModel.source === '0' ? required(val) : true;
        }
      },
      root_dir: {},
      desc: { required },
      skillstack: {
        myRequired(val) {
          return this.isBusinessCom ? required(val) : true;
        }
      },
      businessarea: {
        myRequired(val) {
          return this.isBusinessCom ? required(val) : true;
        }
      }
    }
  },
  computed: {
    ...mapGetters('user', ['isLoginUserList']),
    // ...mapState('userForm', ['groups']),
    ...mapState({
      groups(state) {
        return state.userForm.groups.map(group => {
          return {
            ...group
          };
        });
      }
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    thisLoading() {
      if (this.data && this.data !== 0) {
        return 'updateMpassComponent';
      } else {
        return 'addMpassComponent';
      }
    },
    title() {
      if (this.data === 0) {
        return '组件新增';
      } else if (!this.data) {
        return '组件录入';
      } else {
        return '信息维护';
      }
    },
    userList() {
      return this.isLoginUserList.map(user => {
        return {
          user_name_cn: user.user_name_cn,
          id: user.id,
          user_name_en: user.user_name_en
        };
      });
    },
    disable() {
      return !!this.data;
    },
    isBusinessCom() {
      return this.componentModel.type === ComponentModelType.Business;
    }
  },
  watch: {
    async value(val) {
      this.showLoading = true;
      this.filterGroup = this.groups;
      if (val) {
        await this.fetch();
        this.managerOptions = this.userList;
        if (this.data) {
          this.componentModel = {
            ...this.componentModel,
            ...deepClone(this.data)
          };
        }
      } else {
        this.componentModel = createWebComModel();
      }
      this.showLoading = false;
    }
  },
  methods: {
    ...mapActions('user', ['fetch']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('componentForm', [
      'addMpassComponent',
      'updateMpassComponent'
    ]),
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
    },
    async handleDialog() {
      if (this.data && this.data !== 0) {
        let params = this.componentModel;
        if (!this.isBusinessCom) {
          params = {
            ...this.componentModel,
            skillstack: '',
            businessarea: ''
          };
        }
        await this.updateMpassComponent(params);
        successNotify('信息维护成功');
      } else {
        await this.addMpassComponent(this.componentModel);
        successNotify('录入成功');
      }
      this.$emit('click');
      this.$emit('input', false);
    },
    handleSubmit() {
      const Keys = Object.keys(this.$refs).filter(
        key => key.indexOf('componentModel') > -1
      );
      this.$v.componentModel.$touch();
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.componentModel.$invalid) {
        return;
      }
      this.handleDialog();
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
    groupInputFilter(val, update) {
      update(() => {
        this.filterGroup = this.groups.filter(group => {
          return (
            group.name.indexOf(val) > -1 || group.fullName.indexOf(val) > -1
          );
        });
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
.edit-form-cells
  padding-bottom 20px
</style>
