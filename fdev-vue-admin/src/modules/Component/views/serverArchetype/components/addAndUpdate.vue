<template>
  <f-dialog :value="value" @input="$emit('input', $event)" right :title="title">
    <f-formitem label="骨架英文名" diaS required>
      <fdev-input
        ref="archetypeDialogModel.name_en"
        type="text"
        :readonly="isReadonly"
        v-model="$v.archetypeDialogModel.name_en.$model"
        :rules="[
          () => $v.archetypeDialogModel.name_en.required || '请输入骨架英文名',
          () =>
            $v.archetypeDialogModel.name_en.examine || '只能输入英文、数字、.-_'
        ]"
      />
    </f-formitem>
    <f-formitem label="骨架中文名" diaS required>
      <fdev-input
        ref="archetypeDialogModel.name_cn"
        type="text"
        v-model="$v.archetypeDialogModel.name_cn.$model"
        :rules="[
          () => $v.archetypeDialogModel.name_cn.required || '请输入骨架中文名',
          () => $v.archetypeDialogModel.name_cn.examine || '至少包含一个中文'
        ]"
      />
    </f-formitem>
    <f-formitem label="骨架管理员" diaS required>
      <fdev-select
        multiple
        use-input
        emit-value
        map-options
        :option-value="opt => opt"
        :options="managerOptions"
        option-label="user_name_cn"
        ref="archetypeDialogModel.manager_id"
        @filter="managerFilter"
        v-model="$v.archetypeDialogModel.manager_id.$model"
        :rules="[
          () =>
            $v.archetypeDialogModel.manager_id.required || '请选择骨架管理员'
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
    <f-formitem label="所属小组" diaS required>
      <fdev-select
        use-input
        :option-value="opt => opt"
        :options="groupOptions"
        option-label="fullName"
        ref="archetypeDialogModel.groupObj"
        @filter="groupsFilter"
        v-model="$v.archetypeDialogModel.groupObj.$model"
        :rules="[
          () => $v.archetypeDialogModel.groupObj.required || '请选择所属小组'
        ]"
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
    <f-formitem label="groupID" diaS required>
      <fdev-input
        ref="archetypeDialogModel.groupId"
        type="text"
        :readonly="isReadonly"
        v-model="$v.archetypeDialogModel.groupId.$model"
        :rules="[
          () => $v.archetypeDialogModel.groupId.required || '请输入groupID'
        ]"
      />
    </f-formitem>
    <f-formitem label="artifactId" diaS required>
      <fdev-input
        ref="archetypeDialogModel.artifactId"
        type="text"
        :readonly="isReadonly"
        v-model="$v.archetypeDialogModel.artifactId.$model"
        :rules="[
          () =>
            $v.archetypeDialogModel.artifactId.required || '请输入artifactId'
        ]"
      />
    </f-formitem>
    <f-formitem label="骨架类型" diaS required>
      <fdev-select
        emit-value
        map-options
        input-debounce="0"
        ref="archetypeDialogModel.type"
        option-value="value"
        option-label="label"
        :options="archetypeTypes"
        v-model="$v.archetypeDialogModel.type.$model"
        :rules="[
          () => $v.archetypeDialogModel.type.required || '请选择骨架类型'
        ]"
      />
    </f-formitem>
    <f-formitem label="wiki地址(选填)" diaS>
      <fdev-input
        ref="archetypeDialogModel.wiki_url"
        type="text"
        v-model="$v.archetypeDialogModel.wiki_url.$model"
        :rules="[
          () => $v.archetypeDialogModel.wiki_url.examine || '地址格式不正确'
        ]"
      />
    </f-formitem>
    <f-formitem label="gitlab url 地址" diaS required>
      <fdev-input
        ref="archetypeDialogModel.gitlab_url"
        type="text"
        dense
        :readonly="isReadonly"
        v-model="$v.archetypeDialogModel.gitlab_url.$model"
        :rules="[
          () =>
            $v.archetypeDialogModel.gitlab_url.required ||
            '请输入gitlab url 地址',
          () => $v.archetypeDialogModel.gitlab_url.examine || '地址格式不正确'
        ]"
      />
    </f-formitem>
    <f-formitem diaS label="sonar扫描卡点" class="edit-form-cells">
      <fdev-radio
        disable
        val="1"
        v-model="$v.archetypeDialogModel.sonar_scan_switch.$model"
        label="开"
      />
      <fdev-radio
        disable
        val="0"
        v-model="$v.archetypeDialogModel.sonar_scan_switch.$model"
        label="关"
        class="q-ml-lg"
      />
    </f-formitem>
    <f-formitem diaS label="是否涉及内测" class="edit-form-cells">
      <fdev-radio
        val="1"
        v-model="$v.archetypeDialogModel.isTest.$model"
        label="是"
      />
      <fdev-radio
        val="0"
        v-model="$v.archetypeDialogModel.isTest.$model"
        label="否"
        class="q-ml-lg"
      />
    </f-formitem>
    <f-formitem label="项目编码格式" diaS required>
      <fdev-select
        emit-value
        map-options
        :options="['UTF-8', 'GBK']"
        ref="archetypeDialogModel.encoding"
        v-model="$v.archetypeDialogModel.encoding.$model"
        :rules="[
          () =>
            $v.archetypeDialogModel.encoding.required || '请选择项目编码格式'
        ]"
      />
    </f-formitem>
    <f-formitem label="环境配置文件路径" diaS>
      <fdev-input
        ref="archetypeDialogModel.application_path"
        type="text"
        :rules="[() => true]"
        v-model="$v.archetypeDialogModel.application_path.$model"
      />
    </f-formitem>

    <f-formitem label="骨架描述" diaS required>
      <fdev-input
        ref="archetypeDialogModel.desc"
        type="textarea"
        v-model="$v.archetypeDialogModel.desc.$model"
        :rules="[
          () => $v.archetypeDialogModel.desc.required || '请输入骨架描述'
        ]"
      />
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn
        @click="handleArchetypeFormAllTip"
        label="确定"
        :loading="globalLoading[`componentForm/${method}`]"
        dialog
      />
    </template>
  </f-dialog>
</template>

<script>
import { deepClone, validate, successNotify } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { archetypeDialogModel } from '@/modules/Component/utils/constants.js';

export default {
  name: 'ArchetypeDialog',
  data() {
    return {
      archetypeDialogModel: archetypeDialogModel(),
      managerOptions: [],
      groupOptions: []
    };
  },
  validations() {
    const archetypeDialogModel = {
      archetypeDialogModel: {
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
        sonar_scan_switch: {},
        isTest: {},
        groupId: {
          required
        },
        artifactId: {
          required
        },
        manager_id: {
          required
        },
        groupObj: {
          required
        },
        type: {
          required
        },
        wiki_url: {
          examine(val) {
            if (!val) {
              return true;
            }
            const reg = /^http:\/\/([\w\-.:/%]*)([\w-])$/;
            return reg.test(val);
          }
        },
        encoding: {
          required
        },
        application_path: {}
      }
    };
    return archetypeDialogModel;
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    method: String,
    data: Object
  },
  watch: {
    value(val) {
      this.groupOptions = this.groups;
      this.archetypeDialogModel = archetypeDialogModel();
      if (this.method === 'updateArchetype') {
        this.archetypeDialogModel = deepClone(this.data);
      }
    }
  },
  computed: {
    ...mapState('componentForm', ['archetypeTypes']),
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
    ...mapGetters('user', ['isLoginUserList']),
    title() {
      return this.method === 'updateArchetype' ? '信息维护' : '骨架录入';
    },
    isReadonly() {
      return this.method === 'updateArchetype' ? true : false;
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
    ...mapActions('componentForm', [
      'updateArchetype',
      'addArchetype',
      'queryArchetypeTypes'
    ]),
    ...mapActions('user', ['fetch']),
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
    groupsFilter(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.groupOptions = this.groups.filter(
          group => group.fullName.indexOf(needle) > -1
        );
      });
    },
    handleArchetypeFormAllTip() {
      this.$v.archetypeDialogModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('archetypeDialogModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.archetypeDialogModel.$invalid) {
        return;
      }

      this.handleArchetypeDialogModel();
    },
    async handleArchetypeDialogModel() {
      const parasm = {
        ...this.archetypeDialogModel,
        group: this.archetypeDialogModel.groupObj.id
      };
      await this[this.method](parasm);
      const msg = this.method === 'updateArchetype' ? '更新成功' : '录入成功';
      successNotify(msg);
      this.$emit('input', false);
      this.$emit('click');
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
  },
  created() {
    this.fetch();
    this.queryArchetypeTypes();
  }
};
</script>
<style lang="stylus" scoped>
.edit-form-cells {
  padding-bottom: 20px;
}
</style>
