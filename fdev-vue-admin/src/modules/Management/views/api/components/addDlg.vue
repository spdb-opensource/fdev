<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    dense
    title="新增"
  >
    <div>
      <f-formitem
        class="hideInput"
        label="接口路径"
        required
        bottom-page
        label-style="width:119px"
      >
        <fdev-input
          v-model="$v.createModel.interfacePath.$model"
          ref="createModel.interfacePath"
          :rules="[
            () => $v.createModel.interfacePath.required || '请输入接口路径'
          ]"
        />
      </f-formitem>
      <span class="tipMsg">例：/fdemand/api/demand/add</span>
      <f-formitem label="角色" bottom-page required label-style="width:119px">
        <fdev-select
          multiple
          ref="createModel.roleIds"
          :options="roleIdOptions"
          option-label="name"
          option-value="id"
          v-model="$v.createModel.roleIds.$model"
          :rules="[() => $v.createModel.roleIds.required || '请选择角色id集合']"
        />
      </f-formitem>
      <f-formitem
        required
        label="接口功能说明"
        bottom-page
        label-style="width:119px"
      >
        <fdev-input
          ref="createModel.functionDesc"
          type="textarea"
          v-model="$v.createModel.functionDesc.$model"
          :rules="[
            () => $v.createModel.functionDesc.required || '请输入接口功能说明'
          ]"
        />
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn outline dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog label="确定" :loading="loading" @click="submitForm" />
    </template>
  </f-dialog>
</template>

<script>
import { mapState } from 'vuex';
import { successNotify, errorNotify, validate } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import { createApiRoleModel } from '@/modules/Management/utils/constants';
import { addApiRole } from '@/modules/Management/services/methods';

export default {
  name: 'updateEvaMgtDlg',
  data() {
    return {
      createModel: createApiRoleModel(),
      roleIdOptions: [],
      loading: false
    };
  },
  validations: {
    createModel: {
      interfacePath: {
        required
      },
      roleIds: {
        required
      },
      functionDesc: {
        required
      }
    }
  },
  props: {
    value: {
      type: Boolean
    },
    roles: {
      type: Array
    }
  },
  watch: {
    value(val) {
      if (val) {
        this.createModel = createApiRoleModel();
      }
    },
    roles(val) {
      if (val) {
        this.roleIdOptions = val.filter(item => {
          return item;
        });
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser'])
  },
  methods: {
    verifyModel() {
      try {
        let unitModuleKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.indexOf('createModel') > -1;
        });
        validate(
          unitModuleKeys.map(key => {
            if (this.$refs[key] instanceof Array) {
              return this.$refs[key][0];
            }
            if (
              this.$refs[key].$children.length > 0 &&
              this.$refs[key].$children[0].$children.length > 0 &&
              this.$refs[key].$children[0].validate
            ) {
              return this.$refs[key].$children[0].validate();
            }
            return this.$refs[key].validate();
          })
        );
        const _this = this;
        if (this.$v.createModel.$invalid) {
          const validateRes = unitModuleKeys.every(item => {
            if (item.indexOf('.') === -1) {
              return true;
            }
            const itemArr = item.split('.');
            return !_this.$v.createModel[itemArr[1]].$invalid;
          });
          if (!validateRes) {
            return false;
          }
        }
      } catch (error) {
        return false;
      }
      return true;
    },
    submitForm() {
      if (!this.verifyModel()) {
        return;
      }
      this.saveEvaMgt();
    },
    // 新增
    saveEvaMgt() {
      let params = {
        interfacePath: this.createModel.interfacePath, //接口路径
        functionDesc: this.createModel.functionDesc //功能描述
      };
      //角色id集合
      if (this.createModel.roleIds && this.createModel.roleIds.length > 0) {
        params.roleIds = [];
        for (let i = 0; i < this.createModel.roleIds.length; i++) {
          params.roleIds.push(this.createModel.roleIds[i].id);
        }
      }
      this.loading = true;
      addApiRole(params)
        .then(response => {
          this.loading = false;
          if (response.fdevStatus !== 'error') {
            successNotify('新增成功!');
            this.$emit('close', true);
          } else {
            let { code, msg } = response;
            if (code === '502' || code === '503') {
              msg = '系统维护中，请稍后重试...';
            }
            errorNotify(msg);

            const error = new Error(msg);
            error.name = code;
            error.response = response;
            throw error;
          }
        })
        .catch(e => {
          this.loading = false;
        });
    },
    confirmToClose() {
      this.$emit('close', false);
    }
  },
  async created() {}
};
</script>

<style lang="stylus" scoped>
>>> .hideInput .q-field--with-bottom {
    padding-bottom: 0px !important;
}
>>> .hideInput .q-field__bottom.row.items-start.q-field__bottom--animated{
  display: none;
}
.tipMsg{
  font-family: PingFangSC-Regular;
  font-size: 12px;
  color: #EF5350;
  text-align: right;
  line-height: 32px;
  font-weight: 400;
  margin-left: 136px;
}
.dialog-wrapper
  margin-top 50px
  box-sizing border-box
  max-height calc(100vh - 98px)
  overflow auto
.file-wrapper
  display inline-block
  width calc(100% - 20px)
  margin 0
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  vertical-align: bottom;
.icon
  cursor pointer
  padding 3px
  border-radius 50%
.icon:hover
  background #BBBBBB
.task-type{
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
}
</style>
