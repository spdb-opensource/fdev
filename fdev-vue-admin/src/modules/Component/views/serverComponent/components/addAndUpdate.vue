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
          :readonly="isReadonly"
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
          :option-value="opt => opt"
          :options="managerOptions"
          option-label="user_name_cn"
          ref="componentModel.manager_id"
          @filter="managerFilter"
          v-model="$v.componentModel.manager_id.$model"
          :rules="[
            () => $v.componentModel.manager_id.required || '请选择组件管理员'
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

      <!-- 所属小组 -->
      <f-formitem required diaS label="所属小组">
        <fdev-select
          use-input
          emit-value
          map-options
          option-value="id"
          :options="filterGroup"
          option-label="fullName"
          ref="componentModel.group"
          @filter="groupInputFilter"
          v-model="$v.componentModel.group.$model"
          :rules="[() => $v.componentModel.group.required || '请选择所属小组']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name">
                  {{ scope.opt.name }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.fullName">
                  {{ scope.opt.fullName }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- maven坐标groupID -->
      <f-formitem required diaS label="maven坐标groupID">
        <fdev-input
          ref="componentModel.groupId"
          :readonly="isReadonly"
          v-model="$v.componentModel.groupId.$model"
          :rules="[
            () => $v.componentModel.groupId.required || '请输入maven坐标groupID'
          ]"
        />
      </f-formitem>

      <!-- maven坐标artifactId -->
      <f-formitem required diaS label="maven坐标artifactId">
        <fdev-input
          ref="componentModel.artifactId"
          :readonly="isReadonly"
          v-model="$v.componentModel.artifactId.$model"
          :rules="[
            () =>
              $v.componentModel.artifactId.required ||
              '请输入maven坐标 artifactId'
          ]"
        />
      </f-formitem>

      <!-- 组件类型 -->
      <f-formitem required diaS label="组件类型" v-if="data !== 0">
        <fdev-select
          emit-value
          map-options
          :options="typeOptions"
          :disable="loading === 'updateComponent'"
          option-label="label"
          option-value="value"
          ref="componentModel.type"
          v-model="$v.componentModel.type.$model"
          :rules="[() => $v.componentModel.type.required || '请选择组件类型']"
        />
      </f-formitem>

      <!-- 组件类型为‘多模块组件子模块’时，显示 -->
      <!-- 父组件名称 -->
      <f-formitem required diaS label="父组件名称" v-if="disable">
        <fdev-select
          use-input
          emit-value
          map-options
          :options="parentComponentsOptions"
          option-value="id"
          option-label="name_cn"
          ref="componentModel.parentId"
          v-model="$v.componentModel.parentId.$model"
          @input="findGitlabURL"
          @filter="parentFilter"
          :rules="[
            () => $v.componentModel.parentId.required || '请选择父组件名称'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_cn">
                  {{ scope.opt.name_cn }}
                </fdev-item-label>
                <fdev-item-label caption="" :title="scope.opt.name_en">
                  {{ scope.opt.name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- 组件来源 -->
      <f-formitem required diaS label="组件来源" v-if="data !== 0">
        <fdev-select
          emit-value
          map-options
          :readonly="isReadonly"
          :options="sourceOptions"
          option-value="value"
          :disable="disable"
          option-label="label"
          ref="componentModel.source"
          v-model="$v.componentModel.source.$model"
          :rules="[() => $v.componentModel.source.required || '请选择组件来源']"
        />
      </f-formitem>

      <!-- 推荐版本 -->
      <f-formitem
        :required="!!(data && data.recommond_version) || !data"
        diaS
        label="推荐版本"
        v-if="componentModel.source === '1'"
      >
        <fdev-input
          hint=""
          v-model="$v.componentModel.recommond_version.$model"
          ref="componentModel.recommond_version"
          :rules="[
            () =>
              $v.componentModel.recommond_version.examine || '只能输入数字和.'
          ]"
        />
      </f-formitem>

      <!-- jdk版本 -->
      <f-formitem required diaS label="jdk版本" v-if="data === 0">
        <fdev-select
          emit-value
          map-options
          :options="jdkVersionOptions"
          option-value="value"
          ref="componentModel.jdk_version"
          option-label="label"
          v-model="$v.componentModel.jdk_version.$model"
          :rules="[
            () => $v.componentModel.jdk_version.required || '请选择jdk版本'
          ]"
        />
      </f-formitem>

      <!-- jdk版本 -->
      <f-formitem diaS label="jdk版本" v-else>
        <fdev-select
          emit-value
          map-options
          :options="jdkVersionOptions"
          option-value="value"
          option-label="label"
          v-model="$v.componentModel.jdk_version.$model"
          :rules="[() => true]"
        />
      </f-formitem>

      <!-- wiki地址(选填) -->
      <f-formitem diaS label="wiki地址(选填)">
        <fdev-input
          ref="componentModel.wiki_url"
          v-model="$v.componentModel.wiki_url.$model"
          :rules="[
            () => $v.componentModel.wiki_url.examine || '地址格式不正确'
          ]"
        />
      </f-formitem>

      <!-- gitlab url 地址 -->
      <f-formitem
        required
        diaS
        label="gitlab url 地址"
        v-if="data !== 0 && componentModel.source === '0'"
      >
        <fdev-input
          ref="componentModel.gitlab_url"
          :readonly="isReadonly"
          v-model="$v.componentModel.gitlab_url.$model"
          :rules="[
            () =>
              $v.componentModel.gitlab_url.required || '请输入gitlab url 地址',
            () => $v.componentModel.gitlab_url.examine || '地址格式不正确'
          ]"
        />
      </f-formitem>

      <f-formitem diaS label="sonar扫描卡点" class="edit-form-cells">
        <fdev-radio
          disable
          val="1"
          v-model="$v.componentModel.sonar_scan_switch.$model"
          label="开"
        />
        <fdev-radio
          disable
          val="0"
          v-model="$v.componentModel.sonar_scan_switch.$model"
          label="关"
          class="q-ml-lg"
        />
      </f-formitem>
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

      <!-- 组件代码根路径(选填) -->
      <f-formitem
        diaS
        label="组件代码根路径(选填)"
        v-if="data !== 0 && componentModel.source === '0'"
      >
        <fdev-input
          ref="componentModel.root_dir"
          v-model="$v.componentModel.root_dir.$model"
          :rules="[() => true]"
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
        @click="handleDialogFormAllTip"
        :loading="globalLoading[`componentForm/${loading}`]"
      />
    </template>
  </f-dialog>
</template>

<script>
import { validate, successNotify, deepClone } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  componentModel,
  typeOptions,
  jdkVersionOptions,
  sourceOptions
} from '@/modules/Component/utils/constants.js';
import { mapGetters, mapActions, mapState } from 'vuex';

export default {
  name: 'UpdateDialog',
  props: ['value', 'data'],
  data() {
    return {
      componentModel: componentModel(),
      options: [],
      typeOptions: typeOptions,
      parentComponentsOptions: [],
      managerOptions: [],
      sourceOptions: sourceOptions,
      showLoading: false,
      filterGroup: []
    };
  },
  validations() {
    const componentModel = {
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
        desc: {
          required
        },
        gitlab_url: {
          required,
          examine(val) {
            if (!val) {
              return true;
            }
            const reg = /^http:\/\/([\w\-.:/%]*)([\w-])$/;
            return reg.test(val);
          }
        },
        groupId: {
          required
        },
        artifactId: {
          required
        },
        manager_id: {
          required
        },
        group: {
          required
        },
        type: {
          required
        },
        parentId: {},
        root_dir: {},
        jdk_version: {},
        sonar_scan_switch: {},
        isTest: {},
        source: {
          required
        },
        recommond_version: {
          examine(val) {
            if (this.componentModel.source === '1') {
              if ((this.data && this.data.recommond_version) || !this.data) {
                // 组件信息维护，推荐版本有数据；组件新增，必填
                const reg = /^[0-9.]+$/;
                return reg.test(val);
              } else {
                return true;
              }
            }
            return true;
          }
        },
        wiki_url: {
          examine(val) {
            if (!val) {
              return true;
            }
            const reg = /^http:\/\/([\w\-.:/%]*)([\w-])$/;
            return reg.test(val);
          }
        }
      }
    };
    if (this.componentModel.type === '2') {
      componentModel.componentModel.parentId = {
        required
      };
    }
    if (this.data === 0) {
      componentModel.componentModel.type = {};
      componentModel.componentModel.gitlab_url = {};
      componentModel.componentModel.source = {};
      componentModel.componentModel.jdk_version = {
        required
      };
    }
    if (this.componentModel.source !== '0') {
      componentModel.componentModel.gitlab_url = {};
    }
    return componentModel;
  },
  watch: {
    async value(val) {
      this.showLoading = true;
      this.filterGroup = this.groups;
      if (val === true) {
        this.managerOptions = this.userList;
        this.parentComponentsOptions = this.parentComponents;
        if (this.data) {
          this.data.recommond_version = this.data.recommond_version || '';
        }
        this.componentModel =
          this.data !== null && this.data !== 0
            ? deepClone(this.data)
            : componentModel();
        await this.fetch();
        await this.fetchGroup();
        this.filterGroup = this.groups.slice(0);
      } else {
        this.componentModel = componentModel();
      }
      this.showLoading = false;
    },
    'componentModel.source'(val) {
      if (val === '0') {
        this.componentModel.recommond_version = '';
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('componentForm', {
      detail: 'componentDetail'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapGetters('componentForm', ['parentComponents']),
    loading() {
      if (this.data === 0) {
        return 'createComponent';
      } else if (!this.data) {
        return 'addComponent';
      } else {
        return 'updateComponent';
      }
    },
    disable() {
      return this.componentModel.type === '2';
    },
    jdkVersionOptions() {
      if (this.data === 0) {
        return jdkVersionOptions.slice(0, 2);
      } else {
        return jdkVersionOptions;
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
    isReadonly() {
      if (this.data === 0 || !this.data) {
        return false;
      } else {
        return true;
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
    }
  },
  methods: {
    ...mapActions('user', ['fetch']),
    ...mapActions('componentForm', [
      'updateComponent',
      'addComponent',
      'queryComponentDetail',
      'createComponent'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    handleDialogFormAllTip() {
      this.$v.componentModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('componentModel') > -1;
      });
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
    async handleDialog() {
      if (this.data && this.data !== 0) {
        await this.updateComponent(this.componentModel);
        // 修改推荐版本字段后，刷新历史版本
        if (
          this.data.recommond_version !== this.componentModel.recommond_version
        ) {
          this.$emit('history');
        }
        successNotify('信息维护成功');
      } else if (this.data === 0) {
        await this.createComponent(this.componentModel);
        successNotify('新增成功');
      } else {
        await this.addComponent(this.componentModel);
        successNotify('录入成功');
      }
      this.$emit('click');
      this.$emit('input', false);
    },
    /* 选择父组件后，查询gitlab URL地址 */
    async findGitlabURL(id) {
      await this.queryComponentDetail({
        id: this.componentModel.parentId
      });
      this.componentModel.gitlab_url = this.detail.gitlab_url;
      this.componentModel.source = this.detail.source;
    },
    parentFilter(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.parentComponentsOptions = this.parentComponents.filter(
          v =>
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
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
        this.filterGroup = this.groups.filter(
          tag => tag.fullName.indexOf(val) > -1
        );
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

<style lang="stylus" scoped>
.edit-form-cells {
  padding-bottom: 20px;
}
</style>
