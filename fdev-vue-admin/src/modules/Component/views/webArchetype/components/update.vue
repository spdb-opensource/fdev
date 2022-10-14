<template>
  <f-dialog right :value="value" @input="$emit('input', $event)" :title="title">
    <f-formitem required label="骨架英文名" diaS>
      <fdev-input
        ref="archetypeDialogModel.name_en"
        type="text"
        :disable="method == 'updateMpassArchetype' ? true : false"
        v-model="$v.archetypeDialogModel.name_en.$model"
        :rules="[
          () => $v.archetypeDialogModel.name_en.required || '请输入骨架英文名',
          () =>
            $v.archetypeDialogModel.name_en.examine || '只能输入英文、数字、.-_'
        ]"
      />
    </f-formitem>
    <f-formitem required label="骨架中文名" diaS>
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
    <f-formitem required label="骨架管理员" diaS>
      <fdev-select
        multiple
        use-input
        fill-input
        emit-value
        map-options
        :option-value="opt => opt"
        input-debounce="0"
        :options="managerOptions"
        option-label="user_name_cn"
        ref="archetypeDialogModel.manager"
        @filter="managerFilter"
        v-model="$v.archetypeDialogModel.manager.$model"
        :rules="[
          () => $v.archetypeDialogModel.manager.required || '请选择骨架管理员'
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
    <f-formitem required label="所属小组" diaS>
      <fdev-select
        use-input
        :option-value="opt => opt"
        input-debounce="0"
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
    <f-formitem required label="地址" diaS>
      <fdev-input
        ref="archetypeDialogModel.gitlab_url"
        type="text"
        :disable="method == 'updateMpassArchetype' ? true : false"
        v-model="$v.archetypeDialogModel.gitlab_url.$model"
        :rules="[
          () =>
            $v.archetypeDialogModel.gitlab_url.required ||
            '请输入gitlab url 地址',
          () => $v.archetypeDialogModel.gitlab_url.examine || '地址格式不正确'
        ]"
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
    <f-formitem required label="骨架描述" diaS>
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
        dialog
        :loading="globalLoading[`componentForm/${method}`]"
      />
    </template>
  </f-dialog>
</template>

<script>
import { deepClone, validate, successNotify } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { WebArchetypeDialogModel } from '@/modules/Component/utils/constants.js';

export default {
  name: 'ArchetypeDialog',
  data() {
    return {
      archetypeDialogModel: WebArchetypeDialogModel(),
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
        isTest: {},
        manager: {
          required
        },
        groupObj: {
          required
        }
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
      this.archetypeDialogModel = WebArchetypeDialogModel();
      if (this.method === 'updateMpassArchetype') {
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
      return this.method === 'updateMpassArchetype' ? '信息维护' : '骨架录入';
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
      'updateMpassArchetype',
      'addMpassArchetype',
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
      const msg =
        this.method === 'updateMpassArchetype' ? '更新成功' : '录入成功';
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
.edit-form-cells
  padding-bottom 20px
.select-width
 max-width 248px
</style>
